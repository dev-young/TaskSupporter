package maple_tasks

import helper.HelperCore
import log
import moveMouseSmoothly
import java.awt.Point
import java.awt.event.KeyEvent

open class MapleBaseTask {
    val helper: HelperCore = HelperCore()

    val itemDistance = 42  // 아이템 간격

    var nextItemPoint: Point? = null
    var isNextItemInventoryExpanded = false
    var nextItemPosition = 0 // 현재 확인중인 아이템 순서

    var isNumberLock = helper.toolkit.getLockingKeyState(KeyEvent.VK_NUM_LOCK)

    /**해당 좌표의 아이템이 빈칸인지 확인*/
    fun checkEmpty(leftTop: Point): Boolean {
        val x1 = leftTop.x - 10
        val y1 = leftTop.y - 10
        helper.imageSearch(Point(x1, y1), 40, 40, "img\\emptyItem.png")?.let {
            return true
        }
        return false
    }

    /**해당 좌표의 아이템이 빈칸 혹은 사용불가능한 칸인지 확인*/
    fun checkEmptyOrDisable(leftTop: Point): Boolean {
        val x1 = leftTop.x - 10
        val y1 = leftTop.y - 10
        helper.imageSearch(Point(x1, y1), 40, 40, "img\\emptyItem.png")?.let {
            log("빈칸 발견!")
            return true
        }

        helper.imageSearch(Point(x1, y1), 40, 40, "img\\disableItem.png")?.let {
            log("사용할 수 없는 칸 확인")
            return true
        }

        return false
    }

    suspend fun scrollInventory(count: Int) {
        TODO("Not yet implemented")
    }

    /**수행시 인벤토리에 첫칸부터 수행될때마다 다음칸으로 마우스를 이동시킨다.*/
    suspend fun findNextItem(){
        HelperCore().apply {
            if(nextItemPoint == null) {
                isNextItemInventoryExpanded = isInventoryExpanded()
                nextItemPoint = findFirstItemInInventory() ?: return
                nextItemPoint!!.apply {
                    x += 10
                    y += 10
                }
            }

            moveMouseSmoothly(nextItemPoint!!)
            nextItemPosition++

            if (isNextItemInventoryExpanded) {
                //확장된 인벤토리인 경우
                if (nextItemPosition % 32 == 0) {
                    nextItemPoint?.apply {
                        setLocation(x+itemDistance, y-(itemDistance * 7))
                    }
                    return@apply
                }
            }
            nextItemPoint!!.apply {
                if (nextItemPosition % 4 == 0) {
                    setLocation(x - (itemDistance * 3), y + itemDistance)
                } else {
                    setLocation(x + itemDistance, y)
                }
            }


        }
        log(nextItemPoint.toString())
    }


    fun findInventory(expanded: Boolean): Point? {
        val img = if (expanded) "collapseBtn.png" else "meso.png"
        val p = helper.imageSearch("img\\$img")
        return p
    }

    fun isInventoryExpanded(): Boolean {
        val collapseBtn = helper.imageSearch("img\\collapseBtn.png")
        return collapseBtn != null
    }

    /**인벤토리에서 첫번째 아이템 확인 */
    suspend fun findFirstItemInInventory(moveMouse: Boolean = false): Point? {
        var point: Point? = null
        val collapseBtn = helper.imageSearch("img\\collapseBtn.png")
        if( collapseBtn == null) {
            val mesoBtn = helper.imageSearch("img\\meso.png")
            if(mesoBtn == null){
                log("인벤토리를 찾을 수 없습니다.")
                return null
            } else {
                mesoBtn.let {
                    it.x = it.x + 4
                    it.y = it.y - 252
                }
                point = mesoBtn
            }
        } else {
            collapseBtn.let {
                it.x = it.x - 160
                it.y = it.y - 340
            }
            point = collapseBtn
        }
        if(moveMouse)
            helper.moveMouseSmoothly(point, 100)

        return point
    }

    /**축소된 인벤토리에서 첫번째 아이템 확인 */
    suspend fun findFirstItemFromCollapsed(moveMouse: Boolean = false): Point? {
        var point: Point? = findInventory(false)
        point?.let {
            //첫번째 칸의 좌상단 좌표
            it.x = it.x + 4
            it.y = it.y - 252
//            log("첫째칸 좌상단 위치: $it")
            if (moveMouse)
                helper.moveMouseSmoothly(it, 100)
        }
        return point
    }

    /**확장된 인벤토리에서 첫번째 아이템 확인 */
    suspend fun findFirstItemFromExpanded(moveMouse: Boolean = false): Point? {
        var point: Point? = findInventory(true)
        point?.let {
            //첫번째 칸의 좌상단 좌표
            it.x = it.x - 160
            it.y = it.y - 340
//            log("첫째칸 좌상단 위치: $it")
            if (moveMouse)
                helper.moveMouseSmoothly(it, 100)
        }
        return point
    }

    /**텍스트를 입력을 할 수 있는 곳에서 텍스트를 지운다.*/
    fun clearText(){
        helper.apply {
            if(isNumberLock){
                toolkit.setLockingKeyState(KeyEvent.VK_NUM_LOCK, false)
                isNumberLock = false
            }

            autoDelay = 10
            send(KeyEvent.VK_HOME)
            keyPress(KeyEvent.VK_SHIFT)
            send(KeyEvent.VK_END)
            keyRelease(KeyEvent.VK_SHIFT)
            keyPress(KeyEvent.VK_DELETE)
            autoDelay = 0
        }
    }
}