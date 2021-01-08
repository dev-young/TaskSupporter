package maple_tasks

import changeBlackAndWhite
import helper.HelperCore
import kotlinx.coroutines.delay
import leftTop
import logI
import moveMouseSmoothly
import org.opencv.core.Mat
import winGetPos
import java.awt.Point
import java.awt.event.KeyEvent

class MeisterTask : MapleBaseTask() {

    val imgpathMoveToMeisterBtn = "img\\meister\\moveMeister.png"
    val imgpathMakebtn = "img\\meister\\makeBtn.png"
    val imgpathOkBtn = "img\\meister\\okBtn.png"
    val imgpathSynOkBtn = "img\\meister\\synOkBtn.png"
    val imgpathCancelBtn = "img\\meister\\cancelBtn.png"
    val imgpathExpandBtn = "img\\meister\\expandBtn.png"
    val imgpathSynthesizeOKBtn = "img\\synthesizeOK.png"
    val imgpathSynthesizeWindow = "img\\meister\\synthesizeWindow.png"
    val imgpathExtractWindow = "img\\meister\\extractWindow.png"
    val imgpathSynthesizeBtn1 = "img\\meister\\synthesizeBtn.png"
    val imgpathSynthesizeBtn2 = "img\\meister\\synthesizeBtn2.png"
    val imgpathExtractType = "img\\meister\\extractType.png"
    val imgpathExtractBtn1 = "img\\meister\\extractBtn2.png"
    val imgpathExtractBtn2 = "img\\meister\\extractBtn.png"

    val meisterPosition1 = Point(87, 158)   // 장비제작 위치 (미니맵 좌상단, 최대 확장시 상대좌표)
    val meisterPosition2 = Point(140, 158)   // 장신구제작 위치 (미니맵 좌상단, 최대 확장시 상대좌표)
    val meisterPosition3 = Point(60, 158)   // 연금술 위치 (미니맵 좌상단, 최대 확장시 상대좌표)

    /**빈칸이 나오거나 모든 아이템을 합성할때까지 합성을 반복한다. */
    suspend fun synthesizeItemUntilBlank() {
        logI("합성 시작!")
        HelperCore().apply {

            openSynthesize()
            smartClickTimeMin = 75
            smartClickTimeMax = 150
            var isInventoryExpanded = isInventoryExpanded()
            var vx: Int //현재 합성할 아이템 x
            var vy: Int  //현재 합성할 아이템 y
            val point: Point = findFirstItemInInventory() ?: return soundBeep()
            point.let {
                vx = it.x + 2
                vy = it.y + 2
                logI("첫째칸 좌상단 위치: $vx, $vy")
                moveMouseSmoothly(Point(vx, vy), 50)
            }

            //확인버튼 중앙 좌표 찾기
            val synOkBtn = findSynOkBtn(true) ?: return soundBeep()
            delay(100)

            val synItem = Pair(Point(), Point()) //합성아이템1,2 좌표 담을 변수
            synOkBtn.let {
                synItem.first.setLocation(it.x - 12, it.y - 54) //첫번째 합성칸 중앙 좌표
                synItem.second.setLocation(it.x + 58, it.y - 54)//두번째 합성칸 중앙 좌표
            }

            // 인벤토리 상태에 따라 반복 횟수 다르게 설정
            val repeatCount = if (isInventoryExpanded) 128 else 24
            for (i in 1..repeatCount) {
                if (isEmptyOrDisable(Point(vx, vy))) {
                    logI("합성 완료 (${i / 2}회 수행)")
                    soundBeep()
                    return
                }

                // 아이템 클릭
                smartClick(Point(vx, vy), 15, 15)

                if (i % 2 == 1) {
                    //첫번재 합성칸 클릭
                    smartClick(synItem.first, 6, 6)
                    // TODO: 아이템은 남았지만 합성이 더이상 불가능 한 경우 처리 (피로도부족, 합성의 돌 부족)
                } else {
                    // 두번째 합성칸 클릭 및 확인 클릭
                    smartClick(synItem.second, 6, 6)

                    smartClick(synOkBtn, 12, 4)
                    delayRandom(50, 100)
                    smartClick(synOkBtn, 12, 4)
                    sendEnter()
                    delayRandom(2800, 2950) // 합성 대기시간
                    sendEnter()
                    sendEnter()
                    delayRandom(100, 200)
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
            logI("합성 완료 (${repeatCount / 2}회 수행)")
            soundBeep()
            return
        }
    }

    /**
     * @param maxSynCount 합성을 최대 몇번 수행할지 지정 (0 = 끝까지 수행)
     * @param maxTargetItemCount 최대 몇개의 아이템중에서 합성할 아이템을 찾고 수행할지 지정 (0 = 끝까지 찾는다)
     * */
    suspend fun synthesizeItemSmartly(untilBlank: Boolean, maxSynCount: Int = 0, maxTargetItemCount: Int = 0, mouseDelay:Int = 50) :Int {
        logI("합성 시작!")
        HelperCore().apply {
            moveMouseLB()
            smartClickTimeMin = 45
            smartClickTimeMax = 55
            openSynthesize()

            //확인버튼 중앙 좌표 찾기
            val synOkBtn = findSynOkBtn(true) ?: return 0
            val synItem = Pair(Point(), Point()) //합성아이템1,2 좌표 담을 변수
            synOkBtn.let {
                synItem.first.setLocation(it.x - 12, it.y - 54) //첫번째 합성칸 중앙 좌표
                synItem.second.setLocation(it.x + 58, it.y - 54)//두번째 합성칸 중앙 좌표
            }

            val pairList = findSynItems(untilBlank, maxTargetItemCount)

            var synCount = 0
            pairList.forEach {
                if(synCount < maxSynCount || maxSynCount == 0) {
                    moveMouseSmoothly(it.first, mouseDelay)
                    smartClick(it.first, 15, 15)
                    moveMouseSmoothly(synItem.first, mouseDelay)
                    smartClick(synItem.first, 6, 6)
                    moveMouseSmoothly(it.second, mouseDelay)
                    smartClick(it.second, 15, 15)
                    moveMouseSmoothly(synItem.second, mouseDelay)
                    smartClick(synItem.second, 6, 6)
                    moveMouseSmoothly(Point(synItem.second.x-25, synItem.second.y), 100)

                    if(clickSynOkBtn(50)){
                        delayRandom(50, 100)
                        simpleClick()
                        sendEnter()
                    } else {
                        smartClick(it.first, 15, 15)
                        smartClick(synItem.first, 6, 6)
                        smartClick(it.second, 15, 15)
                        smartClick(synItem.second, 6, 6)
                        moveMouseSmoothly(Point(synItem.second.x-25, synItem.second.y), 100)

                        if(clickSynOkBtn(50)){
                            delayRandom(50, 100)
                            simpleClick()
                            sendEnter()
                        } else {
                            logI("더이상 합성을 진행할 수 없습니다. 진행 횟수: $synCount")
                            return synCount
                        }
                    }
                    moveMouseLB()
                    delayRandom(1500, 1700) // 합성 대기시간
                    var failCount = 0
                    while (!clickOkBtn(50)) {
                        delayRandom(50, 100)
                        failCount++
                        if(failCount > 30) {
                            logI("더이상 합성을 진행할 수 없습니다. 진행 횟수: $synCount")
                            return synCount
                        }
                    }
                    sendEnter()
                    sendEnter()
                    delayRandom(100, 150)

                    synCount++
                }
            }
            return synCount
        }
    }

    suspend fun makeItemInfinitely(maxCount: Int) {
        logI("제작 시작!")
        helper.apply {
            var successCounter = 0
            while (maxCount == 0 || successCounter != maxCount) {
                kotlinx.coroutines.delay(1)
                if (makeItem()) {
                    successCounter++
                    logI("제작 성공")
                }

                kotlinx.coroutines.delay(500)
            }
        }
    }

    /**장비 제작후 일반등급 아이템이면 분해한다. */
    suspend fun makeItemAndExtractIfNormal(itemName: String): Boolean {
        val success = makeItem(itemName)
        if (success) {
            helper.apply {
                //신규 아이템 표시 제거
                openInventory()
                delayRandom(100, 200)
                closeInventory()
                delayRandom(100, 200)
                openInventory()
                delayRandom(100, 200)

                val lastItem = findLastItem()
                if (lastItem == null) {
                } else {
                    if (checkItemIsNormal(lastItem)) {
                        //일반 아이템인 경우 분해
                        extractItem(lastItem)

                        //분해창 닫기
                        clickCancelBtn()
                    }
                }
            }
            return true
        } else {
            logI("제작 불가능한 아이템 입니다.")
            return false
        }
    }

    suspend fun loopMakeItemAndExtractIfNormal(itemName: String) {
        val success = makeItem(itemName)
        if (success) {
            helper.apply {
                //신규 아이템 표시 제거
                openInventory()
                delayRandom(100, 200)
                closeInventory()
                delayRandom(100, 200)
                openInventory()
                delayRandom(100, 200)

                val lastItem = findLastItem()
                if (lastItem == null) {

                } else {
                    if (checkItemIsNormal(lastItem)) {
                        //일반 아이템인 경우 분해
                        extractItem(lastItem)

                        //분해창 닫기
                        clickCancelBtn()
                    }
                }
            }
        }
    }

    /**아이템을 검색후 제작한다.
     * @param waitTimeMinute 만약 제작 대기시간이 있을경우 대기 시간을 지정할 수 있다. (0인경우 기다리지 않는다.)*/
    suspend fun makeItem(itemName: String, waitTimeMinute:Int = 0): Boolean {
        val searchBtn = openProductionSkill()
        if (searchBtn == null) {
            logI("전문기술 창을 열 수 없습니다.")
            return false
        }

        val searchArea = Point(searchBtn.x - 50, searchBtn.y + 2)
        val searchedItem = Point(searchBtn.x - 10, searchBtn.y + 45)
        helper.apply {
            smartClickTimeMax = 200
            copyToClipboard(itemName)

            smartClick(searchArea, 10, 5)
            simpleClick()
            delayRandom(300, 400)
            clearText()
            delayRandom(100, 200)
            paste()

            smartClick(searchBtn, 5, 5)
            simpleClick()
            sendEnter()

            clickExpandBtn(searchBtn)
            delayRandom(100, 150)

            moveMouseSmoothly(searchBtn, 200)

            clickExpandBtn(searchBtn)
            delayRandom(100, 150)

            smartClick(searchedItem, 10, 4)
            simpleClick()
            delayRandom(100, 150)

            moveMouseSmoothly(searchBtn, 200)

            return makeItem(waitTimeMinute)

        }
    }

    /**제작하기 버튼을 눌러 제작을 한 뒤 완료 버튼까지 누른다. (전문기술 창을 열고 사용해야한다.)*/
    suspend fun makeItem(waitMinute:Int = 0): Boolean {
        val maxWaitSec = waitMinute * 60
        helper.apply {
            var point = imageSearchAndClick(imgpathMakebtn, maxTime = 150)
            var tryCount = 0
            while (point == null) {
                tryCount++
                if(tryCount > maxWaitSec) return false
                kotlinx.coroutines.delay(1000)
                point = imageSearchAndClick(imgpathMakebtn, maxTime = 150)

            }
            delayRandom(20, 40)
            simpleClick()
            delayRandom(200, 250)
            imageSearchAndClickUntilFind(imgpathOkBtn, maxTime = 150, repeatCount = 10)?:let {
                logI("확인버튼 찾을 수 없음")
                moveMouseSmoothly(Point(0, 0))
                return false
            }
            delay(2000L)
            imageSearchAndClickUntilFind(imgpathOkBtn, repeatDelay = 400)?:return false

        }
        return true
    }

    /**인벤토리 빈칸이 나오기 전까지 있는 아이템들을 분해한다.*/
    suspend fun extractItemUntilBlank() {
        openInventory()

        val items = findItems()
        extractItem(items)
    }

    /**인벤토리에 보이는 모든 아이템을 분해한다. */
    suspend fun extractItemAll() {
        openInventory()

        val items = findItems(false)
        extractItem(items)
    }

    /**아이템 분해하기*/
    suspend fun extractItem(itemPosList: List<Point>) {
        logI("${itemPosList.size}개의 아이템 분해 시작")
        //인벤토리 열기
        if(openInventory()){
            //분해창 열기
            if(openExtract()){

            } else {
                logI("분해창 열기 실패!!")
            }
        } else {
            logI("인벤토리를 여는데 실패했습니다.")
        }


        var maxCount = getMasExtractSize()    //한번에 분해할 수 있는 아이템 최대 수

        val winPos = helper.user32.winGetPos().leftTop()
        //아이템 클릭
        itemPosList.forEachIndexed { index, point ->
            helper.apply {
                smartClick(point, 10, 10, keyCode = KeyEvent.BUTTON3_MASK, minTime = 1, maxTime = 20)
                simpleClick(KeyEvent.BUTTON3_MASK)
                sendEnter() //분해 불가능한 아이템 있을경우 넘어가기 위해 사용
                if ((index + 1) % maxCount == 0 || index == itemPosList.lastIndex) {
                    //분해창 가득 찼거나 마지막 남은 아이템이 없을때
                    moveMouseSmoothly(winPos, t = 20)
                    clickOkBtn()
                    delayRandom(100, 150)
                    sendEnter()
                    delayRandom(2000, 2150)
                    var failCount = 0
                    while (!clickOkBtn(100)) {
                        moveMouseLB()
                        delayRandom(200, 230)
                        failCount++
                        if(failCount > 20) {
                            logI("분해 완료 확인창 찾을 수 없습니다.")
                            return
                        }
                    }
                    sendEnter()
                    sendEnter()
                    delayRandom(200, 300)
                }
            }
        }

    }

    /**분해창 10칸인지 5칸인지 확인*/
    private fun getMasExtractSize(): Int {
        helper.imageSearch(imgpathExtractType)?.let {
            return 10
        }
        return 5
    }

    suspend fun clickOkBtn(time:Int = 200) : Boolean {

        val ok = helper.imageSearchAndClick(imgpathOkBtn, maxTime = time)
        if (ok == null) {
            return false
        } else {
            helper.simpleClick()
            return true
        }
    }

    suspend fun clickSynOkBtn(time:Int = 200) : Boolean {
        val ok = helper.imageSearch(imgpathSynOkBtn, accuracy = 80.0)
        if (ok == null) {
            return false
        } else {
            helper.smartClick(ok, 10, 5, maxTime = time)
            helper.simpleClick()
            return true
        }
    }

    /**@param untilEnd 취소버튼이 없어질때까지 클릭*/
    suspend fun clickCancelBtn(untilFinish:Boolean = false): Boolean {
        moveMouseLB(30)
        var cancel = helper.imageSearchAndClick(imgpathCancelBtn)
        if (cancel == null) {
            logI("취소버튼을 찾을 수 없습니다.")
            return false
        } else {
            if(untilFinish) {
                var tryCount = 0
                while (cancel != null && tryCount++ < 50) {
                    moveMouseLB(30)
                    cancel = helper.imageSearchAndClick(imgpathCancelBtn)
                    delay(100)
                }
            }
            return true
        }
    }

    /**합성창을 연다.*/
    suspend fun openSynthesize(moveWindow: Boolean = true) {
        helper.apply {
            moveMouseLB(50)
            val window = imageSearch(imgpathSynthesizeWindow)
            val mesoBtn = findInventory()
            if (window == null) {
                var synthesizeBtn = imageSearchAndClick(imgpathSynthesizeBtn1, maxTime = 100)
                    ?: imageSearchAndClick(imgpathSynthesizeBtn2, maxTime = 100)
                if (synthesizeBtn == null) {
                    logI("합성버튼을 찾을 수 없습니다.")
                    return
                }
                simpleClick()
                delayRandom(150, 200)
            }
            if (moveWindow) {
                val okBtn = imageSearch(imgpathSynthesizeOKBtn)
                if (okBtn == null || mesoBtn == null) {
                    logI("합성창을 찾지 못해 옮기기에 실패했습니다")
                } else {
                    val windowPos = helper.user32.winGetPos().leftTop()
                    val synthesizeWindowTitle = Point(okBtn.x, okBtn.y - 163)
                    val dragDestination = if (mesoBtn.x - windowPos.x < 220) {
                        Point(mesoBtn.x + 760, mesoBtn.y - 286)
                    } else Point(mesoBtn.x - 160, mesoBtn.y - 286)

                    smartDrag(synthesizeWindowTitle, dragDestination)
                    kotlinx.coroutines.delay(100)
                }
            }

        }
    }

    /**분해창을 연다.
     * @param moveWindow 분해창을 인벤토리 옆으로 이동시킨다.*/
    suspend fun openExtract(moveWindow: Boolean = true): Boolean {
        helper.apply {
            val mesoBtn = findInventory()
            var tryCount = 0
            while (imageSearch(imgpathExtractWindow) == null) {
                var extractBtn = imageSearchAndClick(imgpathExtractBtn1, maxTime = 100)
                    ?: imageSearchAndClick(imgpathExtractBtn2, maxTime = 100)
                if (extractBtn == null) {
                    logI("분해버튼을 찾을 수 없습니다.")
                    return false
                }
                simpleClick()
                delayRandom(800, 1000)
                moveMouseLB(100)
                tryCount++
                if(tryCount > 10) {
                    return false
                }
            }
            if (moveWindow) {
                val okBtn = imageSearch(imgpathSynthesizeOKBtn)
                if (okBtn == null || mesoBtn == null) {
                    logI("분해창을 찾지 못해 옮기기에 실패했습니다")
                } else {
                    val windowPos = helper.user32.winGetPos().leftTop()
                    val extractWindowTitle = Point(okBtn.x, okBtn.y - 173)
//                    val dragDestination = Point(mesoBtn.x - 160, mesoBtn.y - 286)
                    val dragDestination = if (mesoBtn.x - windowPos.x < 220) {
                        Point(mesoBtn.x + 770, mesoBtn.y - 286)
                    } else Point(mesoBtn.x - 160, mesoBtn.y - 286)
                    smartDrag(extractWindowTitle, dragDestination)
                    delayRandom(200, 300)
                }
            }

        }
        return true
    }

    suspend fun extractItem(item: Point) {
        extractItem(listOf(item))
    }

    private suspend fun clickExpandBtn(searchBtn: Point) {
        helper.imageSearchAndClick(Point(searchBtn.x - 180, searchBtn.y), 100, 60, imgpathExpandBtn, 90.0)
    }

    /**합성/분해 확인 버튼 */
    private suspend fun findSynOkBtn(moveMouse: Boolean = false): Point? {
        moveMouseLB(30)
        val p = helper.imageSearch(imgpathSynthesizeOKBtn)
        if (p == null) {
            logI("합성창을 찾을 수 없습니다.")
        } else {
            if (moveMouse)
                helper.moveMouseSmoothly(p, 100)

        }
        return p
    }

    /**합성 가능한 아이템 좌표의 페어를 반환
     * @param maxTargetItemCount 최대 몇개의 아이템중에서 합성할 아이템을 찾고 수행할지 지정 (0 = 끝까지 찾는다)
     * */
    suspend fun findSynItems(untilBlank: Boolean, maxTargetItemCount: Int = 0): ArrayList<Pair<Point, Point>> {
        val temp = Array(1){Mat()}
        val itemList = findItems(untilBlank, capturedImg = temp)
        val capturedImg = temp[0]
        capturedImg.changeBlackAndWhite()

        //인벤토리에서 찾은 아이템의 수가 maxTargetItemCount 보다 큰 경우 뒤에서부터 maxTargetItemCount 까지 제거
        if(maxTargetItemCount != 0 && itemList.size > maxTargetItemCount) {
            for (i in 1..(itemList.size - maxTargetItemCount)) {
                itemList.removeAt(maxTargetItemCount)
            }
        }

        val targetList = arrayListOf<Pair<Point, Point>>()  // 합성할 아이템들의 페어를 저장할 리스트

        //합성할 아이템들의 페어 리스트 구하기
        val tempList = arrayListOf<Point>()
        itemList.forEach {
            tempList.add(it)
            if(tempList.size == 2) {
                isItemSame(capturedImg, tempList[0], tempList[1]).let {pair ->
                    if(pair != null) {
                        targetList.add(pair)
                        tempList.clear()
                    } else {
                        tempList.removeAt(0)
                    }

                }

            }
        }

        return targetList



    }

    /**해당 이미지의 좌표 내에서 두 아이템이 같은지 반환환*/
    fun isItemSame(totalImg: Mat, item1: Point, item2: Point): Pair<Point, Point>? {
        val i1 = totalImg.colRange(item1.x, item1.x + 25).rowRange(item1.y, item1.y + 25)
        val i2 = totalImg.colRange(item2.x, item2.x + 25).rowRange(item2.y, item2.y + 25)

        return if (helper.imageSearchReturnBoolean(i1, i2, 80.0)) {
            Pair(item1, item2)
        } else null
    }

    suspend fun synthesizeItem(item1: Point, item2: Point, mouseDelay: Int): Boolean {
        val pair = Pair(item1, item2)
        //확인버튼 중앙 좌표 찾기
        val synOkBtn = findSynOkBtn(true) ?: return false
        val synItem = Pair(Point(), Point()) //합성아이템1,2 좌표 담을 변수
        synOkBtn.let {
            synItem.first.setLocation(it.x - 12, it.y - 54) //첫번째 합성칸 중앙 좌표
            synItem.second.setLocation(it.x + 58, it.y - 54)//두번째 합성칸 중앙 좌표
        }

        helper.apply {
            moveMouseSmoothly(pair.first, mouseDelay)
            smartClick(pair.first, 15, 15)
            moveMouseSmoothly(synItem.first, mouseDelay)
            smartClick(synItem.first, 6, 6)
            moveMouseSmoothly(pair.second, mouseDelay)
            smartClick(pair.second, 15, 15)
            moveMouseSmoothly(synItem.second, mouseDelay)
            smartClick(synItem.second, 6, 6)
            moveMouseSmoothly(Point(synItem.second.x-25, synItem.second.y), 100)

            if(clickSynOkBtn(mouseDelay)){
                delayRandom(50, 100)
                simpleClick()
                sendEnter()
            } else {
                smartClick(pair.first, 15, 15, maxTime = mouseDelay)
                smartClick(synItem.first, 6, 6, maxTime = mouseDelay)
                smartClick(pair.second, 15, 15, maxTime = mouseDelay)
                smartClick(synItem.second, 6, 6, maxTime = mouseDelay)
                moveMouseSmoothly(Point(synItem.second.x-25, synItem.second.y), 100)

                if(clickSynOkBtn(50)){
                    delayRandom(50, 100)
                    simpleClick()
                    sendEnter()
                } else {
                    logI("더이상 합성을 진행할 수 없습니다")
                    return false
                }
            }
            moveMouseLB()
            delayRandom(1500, 1700) // 합성 대기시간
            var failCount = 0
            while (!clickOkBtn(50)) {
                delayRandom(50, 100)
                failCount++
                if(failCount > 30) {
                    logI("더이상 합성을 진행할 수 없습니다.")
                    return false
                }
            }
            sendEnter()
            sendEnter()
            delayRandom(100, 150)

        }
        return true

    }

    /**전문기술창을 연 뒤 마이스터빌로 이동 */
    suspend fun moveMeisterVill() {

        openProductionSkill()

        helper.apply {
            moveMouseLB()
            imageSearchAndClick(imgpathMoveToMeisterBtn)
            simpleClick()
            moveMouseLB()

            clickOkBtn()
            moveMouseLB()
            clickOkBtn()

        }
    }

    /**인벤토리에서 첫번째 아이템과 합성 가능한 아이템을 찾는다.
     * @return 첫번째 아이템과 합성 가능한 아이템이 없는경우 null 반환*/
    fun findSynItemWithFirst(inventory: Inventory): Pair<Inventory.Item, Inventory.Item>? {
        val itemList = inventory.getItemList()

        if (itemList.size < 2) return null

        val first = itemList.removeAt(0)
        val firstMat = first.mat?:return null

        for (item in itemList) {
            val targetMat = item.mat?:return null
            if(helper.imageSearchReturnBoolean(firstMat, targetMat)){
                return Pair(first, item)
            }

        }

        return null
    }

    /**전달받은 인벤토리에서 합성 가능한 아이템들의 페어를 리스트형태로 반환한다. */
    fun findSynItems(inventory: Inventory): ArrayList<Pair<Inventory.Item, Inventory.Item>> {
        val itemList = inventory.getItemList()
        val result = arrayListOf<Pair<Inventory.Item, Inventory.Item>>()
        if (itemList.size < 2) return result

        while (itemList.isNotEmpty()) {
            val first = itemList.removeAt(0)
            val firstMat = first.mat?:return result

            var targetIdx : Int? = null
            for (i in itemList.indices) {
                val item = itemList[i]
                val targetMat = item.mat?:return result
                if(helper.imageSearchReturnBoolean(firstMat, targetMat)){
                    targetIdx = i
                    break
                }
            }
            targetIdx?.let {
                result.add(Pair(first, itemList.removeAt(targetIdx)))
            }
        }



        return result
    }


}