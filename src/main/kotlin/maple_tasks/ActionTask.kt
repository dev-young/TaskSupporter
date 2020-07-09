package maple_tasks

import kotlinx.coroutines.delay
import log
import moveMouseSmoothly
import java.awt.Point
import java.awt.event.KeyEvent
import java.util.*

class ActionTask : MapleBaseTask() {
    private var defaultImgPath = "img\\auction"
    private var firstItemInAuction: Point? = null   //경매장 첫번째 아이템 위치

    private var lastSearchTime: Long = 0    //최근 검색 시간
    private var lastPurchaseTime: Long = 0       //최근 구매 시간
    private val searchDelay = 5000  //검색 딜레이 (5초)
    private val purchaseDelay = 1000 //구매 딜레이 (경매장에서 구매시 생기는 딜레이)

    private val purchaseSlotCount = 10  //구매 슬롯 수

   /**한가지 아이템을 계속 구매*/
    suspend fun buyOneItemUntilEnd(buyAll: Boolean) {
        log("아이템 구매 작업 시작! #####")
        helper.apply {
            var buyCount = 0
            var buyStack = 0 //구매슬롯 꽉찾는지 여부 판별을 위한 변수
            root@ while (isAuctionAvailable()) {
                clickBuyTab()
                searchItem()
                delayRandom(50, 100)
                while (isResultExist()) {
                    val success = buyFirstItem(buyAll)
                    if (success) {
                        buyCount++
                        buyStack++
                        log("구매 성공 ($buyCount)")
                    }

                    if( buyStack >= purchaseSlotCount || !success || isPurchaseSlotFull() ) {
                        log("구매슬롯 가득참")
                        val success = getAllItems()
                        if (success) {
                            log("모두받기 완료.")
                            buyStack = 0
                            break
                        } else {
                            log("모두받기 실패.")
                            break@root
                        }
                    }
                }

            }

            log("##### 아이템 구매 작업 종료 (구매횟수: $buyCount)")
        }

    }

    /**파일에 작성된 아이템들을 바탕으로 검색어 바꿔가며 구매*/
    suspend fun buyOneItemUntilEnd(){
        TODO()
    }

    /**완료 탭으로 이동하여 '모두받기' 수행
     * 정상적으로 수행된 경우 true
     * 인벤토리가 가득 찼거나 수령할 수 없는 물건이 있을경우 경우 false*/
    private suspend fun getAllItems(): Boolean {
        clickCompleteTab()

        helper.delayRandom(100, 200)
        clickGetAll()

        helper.apply {
            delayRandom(100, 200)
            sendEnter()

            delayRandom(100, 200)
            val success = waitGetAll()

            if(success){
                delayRandom(100, 200)
                if(imageSearch("$defaultImgPath\\getAllComplete.png") != null){
                    return true
                } else {
                    log("인벤토리가 가득 찼습니다.")
                }
            }

        }
        return false
    }

    /**물품 수령중 메시지가 사라질때까지 기다린다.
     * 성공적으로 완료되었을때 true
     * 너무 오래 진행될경우(5분 이상) false*/
    private suspend fun waitGetAll(): Boolean {
        helper.apply {
//            var point = imageSearch("$defaultImgPath\\purchaseComplete.png")

            var waitCount = 0
            //물품 수령중 알림이 사라질때까지 기다린다. (1초마다 확인)
            while (imageSearch("$defaultImgPath\\gettingItems.png") != null) {
                kotlinx.coroutines.delay(1000)
                waitCount++

                if(waitCount > 300)
                    return false
            }
        }

        return true
    }

    private fun isAuctionAvailable(): Boolean {
        helper.apply {
            val point = imageSearch("$defaultImgPath\\auctionLogo.png") ?: return false
        }
        return true
    }

    /**아이템을 검색한다.
     * 가장 최근 검색 시간을 비교하여 자동으로 남은 시간만큼 대기 후 클릭한다. */
    private suspend fun searchItem() {
        helper.apply {
            val point = imageSearch("$defaultImgPath\\searchBtn.png", 0.1)
//                ?: Unit.let {
//                    val current = getMousePos().location
//                    moveMouseSmoothly(Point(current.x + 100, current.y - 60), 10)
//                    imageSearch("$defaultImgPath\\searchBtn.png", 0.1)
//                }

            if (point == null) {
                log("검색버튼을 찾을 수 없습니다.")
            }

            point?.let {
//                log("검색버튼 찾음")

                val now = System.currentTimeMillis()
                val diff = (now - lastSearchTime)
                if (diff < searchDelay) {
                    val d = searchDelay - diff.toInt()
                    delayRandom(d, d + 200)
//                    log("delay $d")
                }

//                log("now $now, diff $diff")

                smartClick(it, 20, 20, 50, 150)
                simpleClick()
                sendEnter()
                delayRandom(40, 80)
                sendEnter()
                lastSearchTime = System.currentTimeMillis()

            }
        }
    }

    /**첫번째 아이템 구매 (성공시 true 실패시 false)
     * 최근 구매시간을 비교하여 자동으로 남은 시간만큼 대기 후 클릭한다. */
    private suspend fun buyFirstItem(buyAll: Boolean = false): Boolean {
        firstItemInAuction?.let {
            helper.apply {
                smartClick(it, 30, 10, 80, 200)
                simpleClick(it)

                val point = imageSearch("$defaultImgPath\\buyBtn.png") ?: return false

                val now = System.currentTimeMillis()
                val diff = (now - lastPurchaseTime)
                if (diff < purchaseDelay) {
                    delay(purchaseDelay - diff)
                }
                smartClick(point, 50, 20, 80, 200)
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
                sendEnter()
                sendEnter()
//                delayRandom(150, 150)
                kotlinx.coroutines.delay(150)

                if (isPurchased()) {
                    lastPurchaseTime = System.currentTimeMillis()
                    sendEnter() //완료창 확인
                    return true
                }
            }
        }
        return false
    }

    /**구매 완료 메시지를 체크하여 성공 여부 반환*/
    private fun isPurchased(): Boolean {
        helper.apply {
            val point = imageSearch("$defaultImgPath\\purchaseComplete.png") ?: return false
        }
        return true

    }

    /**구매 슬롯 꽉 찼는지 여부 반환 */
    private fun isPurchaseSlotFull(): Boolean {
        helper.apply {
            val point = imageSearch("$defaultImgPath\\purchaseSlotFull.png") ?: return false
        }
        return true

    }

    private fun isResultExist(): Boolean {
        helper.apply {
            val point = imageSearch("$defaultImgPath\\searchResult.png") ?: return false
            firstItemInAuction = Point(point.x + 20, point.y + 70)

            imageSearch("$defaultImgPath\\emptyResult.png") ?: return true

        }
        return false
    }

    suspend fun clickBuyTab() {
        helper.apply {
            val point = imageSearchAndClick("$defaultImgPath\\buyTab.png", maxTime = 300)
            if (point == null) {
//                log("구매탭을 찾을 수 없습니다.")
                return
            }
            simpleClick()
        }
    }

    suspend fun clickCompleteTab() {
        helper.apply {
            val point = imageSearchAndClick("$defaultImgPath\\completeTab.png", maxTime = 300)
            if (point == null) {
//                log("완료탭을 찾을 수 없습니다.")
                return
            }
            simpleClick()
        }
    }

    private suspend fun clickGetAll(): Boolean {
        helper.apply {
            val point = imageSearchAndClick("$defaultImgPath\\getAllBtn.png")
            if (point == null) {
                log("모두받기 버튼을 찾을 수 없습니다.")
                return false
            }
            simpleClick()
            return true
        }
    }

}