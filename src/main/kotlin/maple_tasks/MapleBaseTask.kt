package maple_tasks

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import helper.HelperCore
import kotlinx.coroutines.delay
import logI
import moveMouseSmoothly
import winActive
import winIsForeground
import java.awt.Point
import java.awt.event.KeyEvent

open class MapleBaseTask {
    val helper: HelperCore = HelperCore()

    var hwnd = User32.INSTANCE.FindWindow("", "MapleStory")

    val itemDistance = 42  // 아이템 간격
    var inventoryKey = KeyEvent.VK_I
    var productionSkillKey = KeyEvent.VK_B

    var nextItemPoint: Point? = null
    var isNextItemInventoryExpanded = false //아이템 하나하나 확인하는 방식에서
    var nextItemPosition = 0 // 현재 확인중인 아이템 순서

    var isNumberLock = helper.toolkit.getLockingKeyState(KeyEvent.VK_NUM_LOCK)

    fun activateMaple(): Boolean {
        return User32.INSTANCE.winActive("MapleStory")
    }

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
        val x1 = leftTop.x - 7
        val y1 = leftTop.y - 7
        helper.imageSearch(Point(x1, y1), 39, 39, "img\\emptyItem.png")?.let {
            logI("빈칸 발견!")
            return true
        }

        helper.imageSearch(Point(x1, y1), 39, 39, "img\\disableItem.png")?.let {
            logI("사용할 수 없는 칸 확인")
            return true
        }

        return false
    }

    /**해당 좌표의 아이템이 일반 아이템인지 잠재능력이 있는 아이템인지 확인 확인
     * 아이템에 따라 정확히 판단이 어려운 경우가 있습니다. (아이템이 아이템 칸 테두리까지 넘어오는 이미지인 경우 잠재능력이 있다고 판단한다.)*/
    fun checkItemIsNormal(leftTop: Point): Boolean {
        val x1 = leftTop.x - 7
        val y1 = leftTop.y - 7
        helper.imageSearch(Point(x1, y1), 10, 39, "img\\normalBorder.png", accuracy = 90.9)?.let {
            helper.imageSearch(Point(x1, y1), 39, 10, "img\\normalBorder2.png", accuracy = 90.9)?.let {
//                logI("일반 테두리 발견!")
                return true
            }
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
        logI(nextItemPoint.toString())
    }


    fun findInventory(expanded: Boolean = false): Point? {
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
                logI("인벤토리를 찾을 수 없습니다.")
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
    suspend fun clearText(){
        helper.apply {
            keyPress(KeyEvent.VK_DELETE)
            send(KeyEvent.VK_HOME)
            repeat(50) {
                kotlinx.coroutines.delay(2)
                send(KeyEvent.VK_DELETE)
//                keyPress(KeyEvent.VK_DELETE)

            }
        }
    }

    fun clearText2(){
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

    /**소비 탭 클릭*/
    suspend fun clickConsumeTab(): Point? {
        helper.apply {
            return imageSearchAndClick("img\\consumeTab.png", 150)
        }
    }

    /**인벤토리창을 연다. */
    suspend fun openInventory(){
        val mesoBtn = findInventory()

        if (mesoBtn == null) {
            helper.send(inventoryKey)
            delay(100)
            openInventory()
        }
    }

    /**인벤토리창을 닫는다. */
    suspend fun closeInventory(){
        val mesoBtn = findInventory()

        if (mesoBtn != null) {
            helper.send(inventoryKey)
            delay(100)
            closeInventory()
        }
    }

    /**전문기술 창을 연다.*/
    suspend fun openProductionSkill(): Point? {
        val searchBtn = helper.imageSearch("img\\meister\\searchBtn.png")

        if (searchBtn == null) {
            helper.send(productionSkillKey)
            delay(3000) //전문기술창 로딩 (시간이 생각보다 오래 걸린다.)
            return openProductionSkill()
        } else {
            return searchBtn
        }
    }

    /**전문기술 창을 닫는다.*/
    suspend fun closeProductionSkill(){
        val searchBtn = helper.imageSearch("img\\meister\\searchBtn.png")

        if (searchBtn != null) {
            helper.send(productionSkillKey)
            delay(500)
            closeProductionSkill()
        }
    }

    /**마지막 아이템의 위치를 찾는다. */
    suspend fun findLastItem(): Point? {
        helper.apply {
            var vx: Int //현재 아이템 x
            var vy: Int  //현재 아이템 y
            val point: Point = findFirstItemInInventory() ?: return soundBeep().let {
                logI("인벤토리 첫칸을 찾는데 실패했습니다.")
                null }
            point.let {
                vx = it.x + 2
                vy = it.y + 2
                logI("첫째칸 좌상단 위치: $vx, $vy")
            }
            kotlinx.coroutines.delay(1)

            val isInventoryExpanded = isInventoryExpanded()
            val repeatCount = if (isInventoryExpanded) 128 else 24

            val itemPosList = arrayListOf<Point>()
            for (i in 1..repeatCount) {
                itemPosList.add(Point(vx, vy))
                if (isInventoryExpanded) {
                    //확장된 인벤토리인 경우
                    if (i % 32 == 0) {
                        vx += itemDistance
                        vy -= (itemDistance * 7)
                        continue
                    }
                }

                if (i % 4 == 0) {
                    vx -= (itemDistance * 3)
                    vy += itemDistance
                } else {
                    vx += itemDistance
                }
            }

            var lastPosition = 0
            kotlin.run {
                itemPosList.forEachIndexed { index, point ->
                    kotlinx.coroutines.delay(1)
                    logI("${index+1}번째 : 일반아이템? ${checkItemIsNormal(point)}")
//                moveMouseSmoothly(point)
                    if(checkEmptyOrDisable(point)) {
                        lastPosition = index-1
                        logI("lastPosition: $lastPosition")
                        return@run
                    }
                }
            }

            if(lastPosition < 0){
                logI("아이템을 찾을 수 없습니다.")
                return null
            }


            return itemPosList[lastPosition]
        }

    }

    /**광클*/
    suspend fun startAutoClick(windowTitle:String) {
        logI("광클 시작!")
        while (User32.INSTANCE.winIsForeground(windowTitle)){
            helper.simpleClick()
        }
        logI("광클 종료!")
    }

    /**광클*/
    suspend fun startAutoSend(windowTitle:String, keyCode:Int) {
        logI("광클 시작!")
        while (User32.INSTANCE.winIsForeground(windowTitle)){
            delay(10)
            helper.send(keyCode)
        }
        logI("광클 종료!")
    }

    suspend fun startAutoSpaceAndEnter(windowTitle:String) {
        logI("광클 시작!")
        while (User32.INSTANCE.winIsForeground(windowTitle)){
            delay(10)
            helper.send(KeyEvent.VK_SPACE)
            helper.sendEnter()
        }
        logI("광클 종료!")
    }
}