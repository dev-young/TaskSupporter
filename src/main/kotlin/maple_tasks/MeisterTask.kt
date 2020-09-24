package maple_tasks

import helper.HelperCore
import leftTop
import logI
import moveMouseSmoothly
import winGetPos
import java.awt.Point
import java.awt.event.KeyEvent

class MeisterTask : MapleBaseTask() {

    val imgpathMakebtn = "img\\meister\\makeBtn.png"
    val imgpathOkBtn = "img\\meister\\okBtn.png"
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
    suspend fun synthesizeItemSmartly() {
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

    /**아이템을 검색후 제작한다. */
    suspend fun makeItem(itemName: String): Boolean {
        val searchBtn = openProductionSkill()
        if (searchBtn == null) {
            logI("전문기술 창을 열 수 없습니다.")
            return false
        }

        val searchArea = Point(searchBtn.x - 50, searchBtn.y + 2)
        val searchedItem = Point(searchBtn.x - 10, searchBtn.y + 45)
        helper.apply {
            smartClickTimeMax = 200
            smartClick(searchArea, 10, 5)
            simpleClick()
            delayRandom(200, 300)
            clearText()

            delayRandom(100, 200)
            copyToClipboard(itemName)
            delayRandom(100, 200)
            paste()

            smartClick(searchBtn, 5, 5)
            simpleClick()
            sendEnter()

            clickExpandBtn(searchBtn)
            delayRandom(100, 150)

            moveMouseSmoothly(searchBtn, 100)

            clickExpandBtn(searchBtn)
            delayRandom(100, 150)

            smartClick(searchedItem, 10, 4)
            simpleClick()
            delayRandom(100, 150)

            moveMouseSmoothly(searchBtn, 100)

            return makeItem()

        }
    }

    /**제작하기 버튼을 눌러 제작을 한 뒤 완료 버튼까지 누른다. (전문기술 창을 열고 사용해야한다.)*/
    suspend fun makeItem(): Boolean {

        helper.apply {

            val point = imageSearchAndClick(imgpathMakebtn, maxTime = 150) ?: return false
            delayRandom(20, 40)
            simpleClick()
            delayRandom(200, 250)
            val okBtn = imageSearchAndClick(imgpathOkBtn, maxTime = 150)
            if (okBtn == null) {
                logI("확인버튼 찾을 수 없음")
                moveMouseSmoothly(Point(0, 0))
            }
            delayRandom(2800, 2950)
            imageSearchAndClick(imgpathOkBtn, maxTime = 150)

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
        openInventory()
        //분해창 열기
        openExtract()

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
                    delayRandom(2800, 2950)
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

    suspend fun clickOkBtn() {
        val ok = helper.imageSearchAndClick(imgpathOkBtn, maxTime = 200)
        if (ok == null) {
            logI("확인버튼을 찾을 수 없습니다.")
        } else {
            helper.simpleClick()
        }
    }

    suspend fun clickCancelBtn() {
        val cancel = helper.imageSearchAndClick(imgpathCancelBtn)
        if (cancel == null) {
            logI("취소버튼을 찾을 수 없습니다.")
        } else {
//            helper.simpleClick()
        }
    }

    /**합성창을 연다.*/
    suspend fun openSynthesize(moveWindow: Boolean = true) {
        helper.apply {
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
                    val synthesizeWindowTitle = Point(okBtn.x, okBtn.y - 165)
                    val dragDestination = if (mesoBtn.x - windowPos.x < 220){
                        Point(mesoBtn.x + 760, mesoBtn.y - 286)
                    } else Point(mesoBtn.x - 160, mesoBtn.y - 286)

                    smartDrag(synthesizeWindowTitle, dragDestination)
                }
            }

        }
    }

    /**분해창을 연다.
     * @param moveWindow 분해창을 인벤토리 옆으로 이동시킨다.*/
    suspend fun openExtract(moveWindow: Boolean = true) {
        helper.apply {
            val window = imageSearch(imgpathExtractWindow)
            val mesoBtn = findInventory()
            if (window == null) {

                var extractBtn = imageSearchAndClick(imgpathExtractBtn1, maxTime = 100)
                    ?: imageSearchAndClick(imgpathExtractBtn2, maxTime = 100)
                if (extractBtn == null) {
                    logI("분해버튼을 찾을 수 없습니다.")
                    return
                }
                simpleClick()
                delayRandom(200, 300)
            }
            if (moveWindow) {
                val okBtn = imageSearch(imgpathSynthesizeOKBtn)
                if (okBtn == null || mesoBtn == null) {
                    logI("분해창을 찾지 못해 옮기기에 실패했습니다")
                } else {
                    val windowPos = helper.user32.winGetPos().leftTop()
                    val extractWindowTitle = Point(okBtn.x, okBtn.y - 173)
//                    val dragDestination = Point(mesoBtn.x - 160, mesoBtn.y - 286)
                    val dragDestination = if (mesoBtn.x - windowPos.x < 220){
                        Point(mesoBtn.x + 770, mesoBtn.y - 286)
                    } else Point(mesoBtn.x - 160, mesoBtn.y - 286)
                    smartDrag(extractWindowTitle, dragDestination)
                }
            }

        }
    }

    suspend fun extractItem(item: Point) {
        extractItem(listOf(item))
    }

    private suspend fun clickExpandBtn(searchBtn: Point) {
        helper.imageSearchAndClick(Point(searchBtn.x - 180, searchBtn.y), 100, 60, imgpathExpandBtn, 90.0)
    }

    /**합성/분해 확인 버튼 */
    private suspend fun findSynOkBtn(moveMouse: Boolean = false): Point? {
        val p = helper.imageSearch(imgpathSynthesizeOKBtn)
        if (p == null) {
            logI("합성창을 찾을 수 없습니다.")
        } else {
            if (moveMouse)
                helper.moveMouseSmoothly(p, 100)

        }
        return p
    }

}