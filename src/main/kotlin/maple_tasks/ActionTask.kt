package maple_tasks

import moveMouseSmoothly
import java.awt.Point
import java.awt.event.KeyEvent
import java.util.*

class ActionTask : MapleBaseTask() {

    private var firstItemInAuction: Point? = null   //경매장 첫번째 아이템 위치

    suspend fun auctionTest() {
        helper.apply {
            var buyCount = 0
            while (isAuctionAvailable()) {
                clickSearch()
                delayRandom(50, 100)
                if (isResultExist()) {
                    buyFirstItem(false)
                    buyCount++

                    while (isResultExist()) {
                        delayRandom(900, 1200)

                        buyFirstItem(false)
                        buyCount++
                    }
//                    delayRandom(500, 1000)

                } else {
                    delayRandom(4700, 4700)
                }

            }

            println("옥션 종료 시간: ${Date()}, 구매횟수: $buyCount")


        }

    }

    private suspend fun clickSearch() {
        helper.apply {
//            moveMouseSmoothly(Point(100, 300), 50)
            var point = imageSearch("img\\auction\\searchBtn.png", 0.1)
            if (point == null) {
                val current = getMousePos().location
                moveMouseSmoothly(Point(current.x+100, current.y-60), 10)
                point = imageSearch("img\\auction\\searchBtn.png", 0.1)
            }
            point?.let {
//                println("검색버튼 찾음")
                smartClick(it, 20, 20, 80, 160)
                delayRandom(200, 400)
                keyPress(KeyEvent.VK_ENTER)
                delayRandom(150, 300)
                keyPress(KeyEvent.VK_ENTER)
                keyPress(KeyEvent.VK_ENTER)

            }
        }
    }


    private suspend fun buyFirstItem(buyAll: Boolean = false) {
        firstItemInAuction?.let {
            helper.apply {
                smartClick(it, 30,10, 80, 200)
                simpleClick(it)

                val point = imageSearch("img\\auction\\buyBtn.png") ?: return
//                smartClick(point)
                smartClick(point, 50,20, 80, 200)
                simpleClick(point)

                if (buyAll) {
                    keyPress(KeyEvent.VK_9)
                    delay(50)
                    keyPress(KeyEvent.VK_9)
                    delay(50)
                    keyPress(KeyEvent.VK_9)
                    delay(50)
                    keyPress(KeyEvent.VK_9)
                }

                delayRandom(50, 150)
                keyPress(KeyEvent.VK_ENTER)
                delayRandom(50, 150)
                keyPress(KeyEvent.VK_ENTER)
                delayRandom(50, 150)
                keyPress(KeyEvent.VK_ENTER)
                delayRandom(50, 150)
                keyPress(KeyEvent.VK_ENTER)


                println("구매 시간: ${Date()}")

            }
        }

    }

    private fun isResultExist(): Boolean {
        helper.apply {
            val point = imageSearch("img\\auction\\searchResult.png") ?: return false
            firstItemInAuction = Point(point.x + 20, point.y + 70)

            imageSearch("img\\auction\\emptyResult.png") ?: return true

        }
        return false
    }

    private fun isAuctionAvailable(): Boolean {
        helper.apply {
            val point = imageSearch("img\\auction\\auctionLogo.png") ?: return false
        }
        return true
    }

}