package maple_tasks

import moveMouseSmoothly
import java.awt.Point
import java.awt.event.KeyEvent
import java.util.*

class ActionTask : MapleBaseTask() {

    private var firstItemInAuction: Point? = null   //경매장 첫번째 아이템 위치

    fun auctionTest() {

        helper.apply {
            while (isAuctionAvailable()) {
                clickSearch()
                delayRandom(100, 200)
                if (isResultExist()) {
                    buyFirstItem(true)

                    while (isResultExist()) {
                        delayRandom(900, 1200)
                        buyFirstItem(true)
                    }
                    delayRandom(500, 1000)

                } else {
                    delayRandom(4700, 4700)
                }

            }

            println("옥션 종료 시간: ${Date()}")

        }

    }

    private fun clickSearch() {
        helper.apply {
            moveMouseSmoothly(Point(100, 300), 50)
            val point = imageSearch("img\\auction\\searchBtn.png")
            point?.let {
                smartClick(it, 50, 20)
                delayRandom(200, 400)
                keyPress(KeyEvent.VK_ENTER)
                delayRandom(150, 300)
                keyPress(KeyEvent.VK_ENTER)
                keyPress(KeyEvent.VK_ENTER)

            }
        }
    }


    private fun buyFirstItem(buyAll: Boolean = false) {
        firstItemInAuction?.let {
            helper.apply {
                smartClick(it)
                simpleClick(it)

                val point = imageSearch("img\\auction\\buyBtn.png") ?: return
                smartClick(point)
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