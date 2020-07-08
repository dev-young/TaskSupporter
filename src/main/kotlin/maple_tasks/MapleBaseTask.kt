package maple_tasks

import helper.HelperCore
import moveMouseSmoothly
import java.awt.Point

open class MapleBaseTask {
    val helper: HelperCore = HelperCore()

    val itemDistance = 42  // 아이템 간격

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
            println("빈칸 발견!")
            return true
        }

        helper.imageSearch(Point(x1, y1), 40, 40, "img\\disableItem.png")?.let {
            println("사용할 수 없는 칸 확인")
            return true
        }

        return false
    }

    fun scrollInventory(count: Int) {
        helper.apply {
            var point = findInventory(false)
            point?.apply {
                x += 150
                y -= 35
                moveMouseSmoothly(this, 50)
                delayRandom(200, 300)
                for (i in 1..count) {
                    mouseWheel(1)
                    delayRandom(200, 300)
                }
            }
        }
    }


    fun findInventory(expanded: Boolean): Point? {
        val img = if (expanded) "collapseBtn.png" else "meso.png"
        val p = helper.imageSearch("img\\$img")
        if (p == null) {
            println("인벤토리를 찾을 찾을 수 없습니다.")
        }
        return p
    }

    /**축소된 인벤토리에서 첫번째 아이템 확인 */
    fun findFirstItemFromCollapsed(moveMouse: Boolean = false): Point? {
        var point: Point? = findInventory(false)
        point?.let {
            //첫번째 칸의 좌상단 좌표
            it.x = it.x + 4
            it.y = it.y - 252
            println("첫째칸 좌상단 위치: $it")
            if (moveMouse)
                helper.moveMouseSmoothly(it, 100)
        }
        return point
    }

    /**확장된 인벤토리에서 첫번째 아이템 확인 */
    fun findFirstItemFromExpanded(moveMouse: Boolean = false): Point? {
        var point: Point? = findInventory(true)
        point?.let {
            //첫번째 칸의 좌상단 좌표
            it.x = it.x - 160
            it.y = it.y - 340
            println("첫째칸 좌상단 위치: $it")
            if (moveMouse)
                helper.moveMouseSmoothly(it, 100)
        }
        return point
    }
}