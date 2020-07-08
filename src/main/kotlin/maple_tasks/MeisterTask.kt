package maple_tasks

import moveMouseSmoothly
import java.awt.Point
import java.awt.event.KeyEvent

class MeisterTask : MapleBaseTask() {

    /**빈칸이 나오거나 모든 아이템을 합성할때까지 합성을 반복한다. */
    fun synthesizeItemSmartly() {
        helper.smartClickRange = 5
        helper.apply {
            var isInventoryExpanded = false
            var vx = 0 //현재 합성할 아이템 x
            var vy = 0  //현재 합성할 아이템 y
            val point: Point = findFirstItemFromCollapsed(true) ?: return
            point.let {
                vx = it.x
                vy = it.y
                findFirstItemFromExpanded(true)?.apply {
                    isInventoryExpanded = true
                    vx = x
                    vy = y
                }
            }

            //확인버튼 중앙 좌표 찾기
            val synOkBtn = findSynOkBtn(true) ?: return
            delay(100)

            val synItem = Pair(Point(), Point()) //합성아이템1,2 좌표 담을 변수
            synOkBtn.let {
                synItem.first.setLocation(it.x - 12, it.y - 54) //첫번째 합성칸 중앙 좌표
                synItem.second.setLocation(it.x + 58, it.y - 54)//두번째 합성칸 중앙 좌표
            }

            // 인벤토리 상태에 따라 반복 횟수 다르게 설정
            val repeatCount = if (isInventoryExpanded) 108 else 24
            for (i in 1..repeatCount) {
                if (checkEmptyOrDisable(Point(vx, vy))) {
                    soundBeep()
                    moveMouseSmoothly(Point(vx, vy))
                    return
                }

                // 아이템 클릭
                smartClick(Point(vx, vy), 20, 20)
                delayRandom(200, 300)

                if (i % 2 == 1) {
                    //첫번재 합성칸 클릭
                    smartClick(synItem.first, 10, 10)
                    delayRandom(200, 300)

                } else {
                    // 두번째 합성칸 클릭 및 확인 클릭
                    smartClick(synItem.second, 10, 10)
                    delayRandom(200, 300)
                    smartClick(synOkBtn, 12, 4)
                    delayRandom(50, 100)
                    smartClick(synOkBtn, 12, 4)
                    keyPress(KeyEvent.VK_ENTER)
                    delayRandom(2750, 2950) // 합성 대기시간
                    keyPress(KeyEvent.VK_ENTER)
                    delayRandom(100, 300)
                }

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
            return
        }
    }

    /**합성/분해 확인 버튼 */
    private fun findSynOkBtn(moveMouse: Boolean = false): Point? {
        val p = helper.imageSearch("img\\synthesizeOK.png")
        if (p == null) {
            println("합성창을 찾을 수 없습니다.")
        } else {
            if (moveMouse)
                helper.moveMouseSmoothly(p, 100)
        }
        return p
    }


}