package maple_tasks

import kotlinx.coroutines.delay
import logI
import moveMouseSmoothly
import java.awt.Point
import java.awt.event.KeyEvent
import java.io.File


class AuctionTask : MapleBaseTask() {
    private var defaultImgPath = "img\\auction"
    private var firstItemInAuction: Point? = null   //경매장 첫번째 아이템 위치

    private var lastSearchTime: Long = 0    //최근 검색 시간
    private var lastPurchaseTime: Long = 0       //최근 구매 시간
    private val searchDelay = 5000  //검색 딜레이 (5초)
    private val purchaseDelay = 1000 //구매 딜레이 (경매장에서 구매시 생기는 딜레이)

    private val purchaseSlotCount = 10  //구매 슬롯 수

    //여러개의 아이템 검색시 다음 아이템으로 넘어가기까지 필요한 검색 수 (결과가 없을경우에만 넘어감)
    private val noResultCountMax = 3

    /**한가지 아이템을 계속 구매*/
    suspend fun buyOneItemUntilEnd(buyAll: Boolean, usePurchasedTab: Boolean = true) {
        logI("아이템 구매 작업 시작! #####")
        helper.apply {
            var buyCount = 0
            var buyStack = 0 //구매슬롯 꽉찾는지 여부 판별을 위한 변수
            root@ while (isAuctionAvailable()) {
                val p = clickSearchTab()
                //혹시나 검색탭이 안눌리는 경우를 대비하여 한번 더 클릭
                p?.let {
                    delayRandom(50, 100)
                    simpleClick(it)
                }

                searchItem()
                delayRandom(50, 100)
                while (isResultExist()) {
                    val success = buyFirstItem(buyAll)
                    if (success) {
                        buyCount++
                        buyStack++
                        logI("구매 성공 ($buyCount)")
                    }

                    if (buyStack >= purchaseSlotCount || (!success && isPurchaseSlotFull())) {
                        // buyStack이 슬롯보다 크거나 구매슬롯이 꽉 찬 경우
                        logI("구매슬롯 가득참")
                        delayRandom(200, 400)
                        val success = getAllItems(usePurchasedTab)
                        if (success) {
                            logI("모두받기 완료.")
                            sendEnter()
                            buyStack = 0
                            break
                        } else {
                            logI("모두받기 실패.")
                            break@root
                        }
                    } else {
                        // 가득 안찼지만 구매 실패한 경우
                    }
                }

            }
            helper.soundBeep()
            logI("##### 아이템 구매 작업 종료 (구매횟수: $buyCount)")
        }

    }

    /**파일에 작성된 아이템들을 바탕으로 검색어 바꿔가며 구매
     * 장비 제작 및 일정 시간마다 특정 장비아이템 제작 */
    suspend fun buyItemListUntilEnd(
        filePath: String,
        itemName: String? = null,
        waitingTime: Long = 900000,
        extract: Boolean = false,
        usePurchasedTab: Boolean = true
    ) {
        val itemList = loadItemList(filePath) ?: let {
            logI("파일을 찾을 수 없습니다.")
            return
        }
        openAuction()

        var targetIndex = 0 //검색 대상 인덱스

        var buyCount = 0    //전체 구매 횟수
        var itemInInventoryCount = 0 //현재 수령한 아이템 수 (분해시 0으로 초기화)
        var buyStack = 0 //구매슬롯 꽉찾는지 여부 판별을 위한 변수
        var noResultItemCount = 0   // 해당 아이템 검색 결과가 없는 경우 (1개의 아이템에 대해 회만 증가한다.)
        logI("아이템 구매 작업 시작! #####")

        if (itemList.isEmpty()) itemList.add(arrayOf("", "", "", "", ""))

        var lastMakeTime = System.currentTimeMillis()
        val meisterTask = MeisterTask()

        itemList.forEach { logI(it.contentToString()) }
        helper.apply {
            root@ while (isAuctionAvailable() && itemList.isNotEmpty()) {
                val itemInfo = itemList[targetIndex]
                val targetCategory = itemInfo[0]
                val targetName = itemInfo[1]
                val targetPrice = itemInfo[2]
                val targetBuyAll = if (itemInfo.size > 3) itemInfo[3].contains("t") else false
                val targetClickReset = if (itemInfo.size > 4) !itemInfo[4].contains("f") else true
                var noResultCount = 2

                clickSearchTab()
                delayRandom(30, 50)
                clickCategory(targetCategory)
                delayRandom(30, 50)

                val success = inputItemInfo(targetName, targetPrice, targetClickReset)
                if (!success) {
                    logI("아이템 정보 입력을 실패했습니다. [$targetName, $targetPrice]")
                    logI("다음 아이템으로 건너뜁니다.")
                    targetIndex = (targetIndex + 1) % itemList.size
                    continue
                }
                delayRandom(30, 50)

                sub@ while (noResultCount < noResultCountMax) {
                    clickSearchTab()
                    delayRandom(30, 50)
                    searchItem()
                    delayRandom(50, 100)
                    while (isResultExist()) {
                        val success = buyFirstItem(targetBuyAll)
                        if (success) {
                            buyCount++
                            buyStack++
                            noResultCount = 0
                            noResultItemCount = 0
                            logI("구매 성공 ($buyCount) [ $targetName ]")
                        }

                        if (buyStack >= purchaseSlotCount || (!success && isPurchaseSlotFull())) {
                            logI("구매슬롯 가득참")
                            delayRandom(200, 400)
                            val success = getAllItems(usePurchasedTab)
                            itemInInventoryCount += buyStack
                            buyStack = 0
                            targetIndex = (targetIndex + itemList.size - 1) % itemList.size // 이전 아이템
                            if (success) {
                                logI("모두받기 완료.")
                                sendEnter() //완료창 종료
                                break@sub
                            } else {
                                logI("모두받기 실패.")
                                //분해하기 사용하는 경우
                                if (extract) {
                                    extractItem(meisterTask)
                                    itemInInventoryCount = 0
                                    break@sub
                                } else {
                                    break@root
                                }
                            }
                        } else {
                            // 가득 안찼지만 구매 실패한 경우 (메소 부족, 이미 팔린 물건)
                            // TODO: 메소부족시 현재 아이템을 구매 리스트에서 제외시키고 다음 아이템으로 넘어가기
                        }
                    }
                    //결과 없는 경우 엔터 클릭
                    delay(500L)
                    sendEnter()
                    noResultCount++
                }

                //아이템 제작 사용하는 경우
                itemName?.let {
                    //시간 확인 및 장비 제작
                    if (System.currentTimeMillis() - lastMakeTime > waitingTime) {
                        //지정한 시간이 지난경우 아이템 제작
                        exitAuction()
                        delayRandom(2000, 3000)
                        moveCharacter(meisterTask.meisterPosition1)
                        if (meisterTask.makeItemAndExtractIfNormal(itemName))
                            lastMakeTime = System.currentTimeMillis()

                        delayRandom(500, 700)
                        openAuction()
                    }
                }


                targetIndex = (targetIndex + 1) % itemList.size

                noResultItemCount++
                if (noResultItemCount >= itemList.size && buyStack > 0) {
                    //오랜시간 구매한 아이템이 없을때 수령가능한 템이 있을경우

                    itemInInventoryCount += buyStack
                    buyStack = 0
                    if (getAllItems(usePurchasedTab)) {
                        logI("모두받기 완료.")
                        sendEnter() //완료창 종료
                    } else {
                        logI("모두받기 실패.")
                        //분해하기 사용하는 경우
                        if (!extract) {
                            break@root
                        }
                    }

                    if (extract && (itemInInventoryCount > 10)) {
                        extractItem(meisterTask)
                        itemInInventoryCount = 0
                    }

                    noResultItemCount = 0
                }

            }


        }

        helper.soundBeep()
        logI("##### 아이템 구매 작업 종료 (구매횟수: $buyCount)")
    }

    private suspend fun extractItem(meisterTask: MeisterTask) {
        helper.apply {
            exitAuction()
            delayRandom(2000, 3000)
            meisterTask.extractItemUntilBlank()
            meisterTask.clickCancelBtn()
            send(KeyEvent.VK_ESCAPE)
            delayRandom(500, 700)
            openAuction()
        }
    }

    suspend fun openAuction() {
        while (!isAuctionAvailable()) {
            helper.apply {
                val menu = imageSearchAndClick("img\\menu.png", maxTime = 300)?.also {
                    delayRandom(1000, 1500)
                    //옥션 클릭
                    smartClick(Point(it.x - 60, it.y - 180), 60, 10, 100, 200)
                    delayRandom(2000, 3000)
                }

                if (menu == null) {
                    imageSearchAndClick("img\\menuCollapse.png", maxTime = 300)?.let {
                        delayRandom(1000, 1500)
                    }
                }

                kotlinx.coroutines.delay(300)

            }
        }
    }

    suspend fun exitAuction() {
        while (isAuctionAvailable()) {
            helper.apply {
                moveMouseOnForeground(1, 10)

                imageSearchAndClick("$defaultImgPath\\exit.png", maxTime = 200)?.let {
                    delayRandom(4000, 5000)
                }

                delayRandom(300, 500)
            }
        }

    }

    /**완료 탭으로 이동하여 '모두받기' 수행
     * 정상적으로 수행된 경우 true
     * 인벤토리가 가득 찼거나 수령할 수 없는 물건이 있을경우 경우 false
     * @param usePurchasedTab 구매완료 탭을 사용할지 여부*/
    private suspend fun getAllItems(usePurchasedTab: Boolean): Boolean {
        clickCompleteTab()

        helper.delayRandom(100, 200)
        if (usePurchasedTab) {
            clickCompletePurchasedTab()
        }
        clickGetAll()

        helper.apply {
            delayRandom(100, 200)
            sendEnter()

            delayRandom(100, 200)
            val success = waitGetAll()

            if (success) {
                delayRandom(100, 200)
                if (imageSearch("$defaultImgPath\\getAllComplete.png") != null) {
                    return true
                } else {
                    logI("인벤토리가 가득 찼습니다.")
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

                if (waitCount > 300)
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
    suspend fun searchItem() {
        helper.apply {
            val point = imageSearch("$defaultImgPath\\searchBtn.png", 0.1)
//                ?: Unit.let {
//                    val current = getMousePos().location
//                    moveMouseSmoothly(Point(current.x + 100, current.y - 60), 10)
//                    imageSearch("$defaultImgPath\\searchBtn.png", 0.1)
//                }

            if (point == null) {
                logI("검색버튼을 찾을 수 없습니다.")
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
                delayRandom(140, 180)
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

    /**파일로부터 구매할 아이템 목록을 가져온다.
     * Array<String> = {분류, 템이름, 가격, buyAll, reset}
     * 분류 = 방어구, 무기, 소비, 캐시, 기타
     * 템이름 = 공백없이 작성
     * buyAll = 구매시 갯수 입력할때 최대치로 할지 여부 (true, false)
     * reset = 초기화버튼 클릭 여부
     * */
    private fun loadItemList(filePath: String): ArrayList<Array<String>>? {
        val list = arrayListOf<Array<String>>()

        val file = File("$filePath.txt")
        if (file.exists()) {
            file.readLines().forEach {
//                    log(it)
                if (it.startsWith("//") || it.isEmpty()) {
                    //공백 혹은 주석처리된 line
                } else {
                    val s = it.split("/").toTypedArray()
                    if (s.size < 3) {
                        logI("올바르지 않은 형식입니다. -> $it")
                    } else {
                        s.apply {
                            forEachIndexed { index, s ->
                                this[index] = s.trim()
                            }
                        }
                        list.add(s)
                    }

                }


            }

        } else {
            return null
        }

        return list
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

    suspend fun clickSearchTab(): Point? {
        helper.apply {
            val point = imageSearchAndClick("$defaultImgPath\\buyTab.png", maxTime = 300)
            if (point == null) {
//                log("구매탭을 찾을 수 없습니다.")
                return null
            }
            simpleClick()
            return point
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

    suspend fun clickCompletePurchasedTab() {
        helper.apply {
            val point = imageSearchAndClick("$defaultImgPath\\completedPurchased.png", 95.0, maxTime = 200)
            if (point == null) {
                return
            }
            simpleClick()
        }
    }

    private suspend fun clickGetAll(): Boolean {
        helper.apply {
            val point = imageSearchAndClick("$defaultImgPath\\getAllBtn.png")
            if (point == null) {
                logI("모두받기 버튼을 찾을 수 없습니다.")
                return false
            }
            simpleClick()
            return true
        }
    }

    suspend fun clickCategory(category: String) {
        var imgName = when (category) {
            "방어", "방어구" -> "categoryDefence.png"
            "무기" -> "categoryWeapon.png"
            "소비" -> "categoryConsume.png"
            "캐시" -> "categoryCash.png"
            "기타" -> "categoryElse.png"
            else -> ""
        }

        if (imgName.isEmpty()) return
        helper.apply {
            imageSearchAndClick("$defaultImgPath\\$imgName", maxTime = 200) ?: return
            simpleClick()
        }

    }

    /**검색시 이름 및 가격을 입력한다. */
    suspend fun inputItemInfo(itemName: String, itemPrice: String, reset: Boolean = false, minPrice:String = "_"): Boolean {

        helper.apply {
            if (reset) {
                val p = imageSearchAndClick("$defaultImgPath\\resetConditionBtn.png", maxTime = 100)
                p?.let {
                    simpleClick()
                }
            }

            if (itemName != "_") {
                copyToClipboard(itemName)
                val pointName = imageSearch("$defaultImgPath\\itemName.png") ?: return false
                pointName.let {
                    val searchLT = Point(it.x, it.y-5)
                    it.setLocation(it.x + 120, it.y + 5)
                    inputText(it)

                    if(itemName.isNotEmpty())
                        while (imageSearch(searchLT, 200, 30,"$defaultImgPath\\itemNameEmpty.png", accuracy = 90.0) != null) {
                            logI("아이템명 입력 실패: $itemName 클립보드:${getStringFromClipboard()}")
                            copyToClipboard(itemName)
                            inputText(it)
                        }

                }
            }

            if (minPrice != "_") {
                copyToClipboard(minPrice)
                val pointPrice = imageSearch("$defaultImgPath\\itemPrice.png") ?: return false
                pointPrice.let {
                    val searchLT = Point(it.x+62, it.y) //빈칸을 검색좌표 시작점
                    it.setLocation(it.x + 83, it.y + 5)
                    inputText(it)
                    if(minPrice.isNotEmpty())
                        while (imageSearch(searchLT, 100, 20,"$defaultImgPath\\itemPriceEmpty.png", accuracy = 90.0) != null) {
                            logI("가격 입력 실패: $minPrice 클립보드:${getStringFromClipboard()}")
                            copyToClipboard(minPrice)
                            inputText(it)
//                            smartClick(pointPriceMin, randomRangeX = 5, randomRangeY = 3, minTime = 20, maxTime = 30)
                        }

                }
            }


            if (itemPrice != "_") {
                copyToClipboard(itemPrice)
                val pointPrice = imageSearch("$defaultImgPath\\itemPrice.png") ?: return false
                pointPrice.let {
                    val searchLT = Point(it.x+150, it.y) //빈칸을 검색좌표 시작점
                    it.setLocation(it.x + 170, it.y + 5)
                    val pointPriceMin = Point(it.x-80, it.y)
                    inputText(it)
//                    smartClick(pointPriceMin, randomRangeX = 5, randomRangeY = 3, minTime = 20, maxTime = 30)
                    if(itemPrice.isNotEmpty())
                        while (imageSearch(searchLT, 100, 20,"$defaultImgPath\\itemPriceEmpty.png", accuracy = 90.0) != null) {
                            logI("가격 입력 실패: $itemPrice 클립보드:${getStringFromClipboard()}")
                            copyToClipboard(itemPrice)
                            inputText(it)
//                            smartClick(pointPriceMin, randomRangeX = 5, randomRangeY = 3, minTime = 20, maxTime = 30)
                        }

                }
            }

            return true

        }

    }

    /**텍스트를 입력*/
    private suspend fun inputText(targetPoint: Point) {
        helper.apply {
            smartClick(targetPoint, randomRangeX = 20, randomRangeY = 3, minTime = 200, maxTime = 300)
            delayRandom(50, 100)
            simpleClick()
            moveMouseSmoothly(Point(targetPoint.x, targetPoint.y-100), t = 20)
//            delayRandom(20, 30)
            clearText()
//                    delayRandom(300, 330)
            paste()
            delay(80L)
            send(KeyEvent.VK_SPACE)
            send(KeyEvent.VK_SPACE)
            send(KeyEvent.VK_SPACE)
            send(KeyEvent.VK_SPACE)
            delay(40L)
        }
    }

}