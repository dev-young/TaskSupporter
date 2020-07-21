package maple_tasks

import helper.HelperCore
import logI
import moveMouseSmoothly
import java.awt.Point
import java.awt.event.KeyEvent
import kotlin.math.log

class MeisterTask : MapleBaseTask() {

    val imgpathMakebtn = "img\\meister\\makeBtn.png"
    val imgpathOkBtn = "img\\meister\\okBtn.png"
    val imgpathCancelBtn = "img\\meister\\cancelBtn.png"
    val imgpathExpandBtn = "img\\meister\\expandBtn.png"
    val imgpathSynthesizeOKBtn = "img\\synthesizeOK.png"
    val imgpathExtractBtn1 = "img\\meister\\extractBtn2.png"
    val imgpathExtractBtn2 = "img\\meister\\extractBtn.png"

    /**빈칸이 나오거나 모든 아이템을 합성할때까지 합성을 반복한다. */
    suspend fun synthesizeItemSmartly() {
        logI("합성 시작!")
        HelperCore().apply {

            smartClickTimeMin = 100
            smartClickTimeMax = 300
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
                if (checkEmptyOrDisable(Point(vx, vy))) {
                    logI("합성 완료 (${i/2}회 수행)")
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
            logI("합성 완료 (${repeatCount/2}회 수행)")
            soundBeep()
            return
        }
    }

    suspend fun makeItemInfinitely(maxCount: Int) {
        logI("제작 시작!")
        helper.apply {
            var successCounter = 0
            while(maxCount == 0 || successCounter != maxCount) {
                kotlinx.coroutines.delay(1)
                val point = imageSearchAndClick(imgpathMakebtn)
                point?.let {
                    delayRandom(20,40)
                    simpleClick()
                    delayRandom(600, 700)
                    val okBtn = imageSearchAndClick(imgpathOkBtn)
                    if (okBtn== null){
                        logI("확인버튼 찾을 수 없음")
                        moveMouseSmoothly(Point(0, 0))
                    }
                    delayRandom(4000, 4500)
                    imageSearchAndClick(imgpathOkBtn)
                    successCounter++
                    logI("장비 제작 성공 ($successCounter)")
                }

                kotlinx.coroutines.delay(500)
            }
        }
    }

    /**장비 제작후 일반등급 아이템이면 분해한다. */
    suspend fun makeItemAndExtractIfNormal(itemName: String){
        val success = makeItem(itemName)
        if(success) {
            helper.apply {
                //신규 아이템 표시 제거
                openInventory()
                delayRandom(100, 200)
                closeInventory()
                delayRandom(100, 200)
                openInventory()
                delayRandom(100, 200)

                val lastItem = findLastItem()
                if(lastItem == null) {

                } else {
                    if(checkItemIsNormal(lastItem)){
                        //일반 아이템인 경우 분해
                        extractItem(lastItem)

                        //분해창 닫기
                        clickCancelBtn()
                    }
                }
            }
        }
    }

    suspend fun loopMakeItemAndExtractIfNormal(itemName: String){
        val success = makeItem(itemName)
        if(success) {
            helper.apply {
                //신규 아이템 표시 제거
                openInventory()
                delayRandom(100, 200)
                closeInventory()
                delayRandom(100, 200)
                openInventory()
                delayRandom(100, 200)

                val lastItem = findLastItem()
                if(lastItem == null) {

                } else {
                    if(checkItemIsNormal(lastItem)){
                        //일반 아이템인 경우 분해
                        extractItem(lastItem)

                        //분해창 닫기
                        clickCancelBtn()
                    }
                }
            }
        }
    }

    suspend fun makeItem(itemName: String): Boolean {
        val searchBtn = openProductionSkill()
        if(searchBtn == null) {
            logI("전문기술 창을 열 수 없습니다.")
            return false
        }

        val searchArea = Point(searchBtn.x-50, searchBtn.y+2)
        val searchedItem = Point(searchBtn.x-10, searchBtn.y+45)
        helper.apply {
            smartClickTimeMax = 200
            smartClick(searchArea, 10, 5)
            simpleClick()
            delayRandom(200, 300)
            clearText()
            delayRandom(100, 200)
            clearText()

            delayRandom(100, 200)
            copyToClipboard(itemName)
            delayRandom(100, 200)
            paste()

            smartClick(searchBtn, 5, 5)
            simpleClick()

            clickExpandBtn(searchBtn)
            delayRandom(100, 150)

            moveMouseSmoothly(searchBtn, 100)

            clickExpandBtn(searchBtn)
            delayRandom(100, 150)

            smartClick(searchedItem, 10,4)
            simpleClick()
            delayRandom(100, 150)

            moveMouseSmoothly(searchBtn, 100)

            val point = imageSearchAndClick(imgpathMakebtn)
            if(point == null) {
                logI("제작 불가능한 아이템 입니다.")
                return false
            }
            point?.let {
                delayRandom(20,40)
                simpleClick()
                delayRandom(600, 700)
                val okBtn = imageSearchAndClick(imgpathOkBtn)
                if (okBtn== null){
                    logI("확인버튼 찾을 수 없음")
                    moveMouseSmoothly(Point(0, 0), 100)
                }
                delayRandom(4000, 4500)
                imageSearchAndClick(imgpathOkBtn)
            }

        }
        return true
    }

    /**제작하기 버튼을 눌러 제작을 한 뒤 완료 버튼까지 누른다. (전문기술 창을 열고 사용해야한다.)*/
    suspend fun makeItem(): Boolean {

        helper.apply {

            val point = imageSearchAndClick(imgpathMakebtn)
            if(point == null) {
                logI("제작 불가능한 아이템 입니다.")
                return false
            }
            point?.let {
                delayRandom(20,40)
                simpleClick()
                delayRandom(600, 700)
                val okBtn = imageSearchAndClick(imgpathOkBtn)
                if (okBtn== null){
                    logI("확인버튼 찾을 수 없음")
                    moveMouseSmoothly(Point(0, 0), 100)
                }
                delayRandom(4000, 4500)
                imageSearchAndClick(imgpathOkBtn)
            }

        }
        return true
    }

    /**아이템 분해하기*/
    suspend fun extractItem(itemPosList: List<Point>) {
        logI("${itemPosList.size}개의 아이템 분해 시작")
        //인벤토리 열기
        openInventory()
        //분해창 열기
        openExtract()

        var maxCount = 5    //한번에 분해할 수 있는 아이템 최대 수

        //아이템 클릭
        itemPosList.forEachIndexed { index, point ->
            helper.apply {
                smartClickTimeMax = 100
                smartClick(point, 10, 10, keyCode = KeyEvent.BUTTON3_MASK)
                simpleClick(KeyEvent.BUTTON3_MASK)
                if((index+1)%maxCount == 0 || index == itemPosList.lastIndex) {
                    //분해창 가득 찼거나 마지막 남은 아이템이 없을때
                    clickOkBtn()
                    delayRandom(100, 150)
                    sendEnter()
                    delayRandom(4000, 4100)
                    sendEnter()
                    sendEnter()
                    delayRandom(200, 300)
                }
            }
        }



    }

    suspend fun clickOkBtn() {
        val ok = helper.imageSearchAndClick(imgpathOkBtn)
        if(ok == null) {
            logI("확인버튼을 찾을 수 없습니다.")
        } else {
            helper.simpleClick()
        }
    }

    suspend fun clickCancelBtn() {
        val cancel = helper.imageSearchAndClick(imgpathCancelBtn)
        if(cancel == null) {
            logI("취소버튼을 찾을 수 없습니다.")
        } else {
            helper.simpleClick()
        }
    }

    /**분해창을 연다.
     * @param moveWindow 분해창을 인벤토리 옆으로 이동시킨다.*/
    suspend fun openExtract(moveWindow: Boolean = true) {
        helper.apply {
            var extractBtn = imageSearch(imgpathExtractBtn1) ?: imageSearch(imgpathExtractBtn2)
            if(extractBtn == null) {
                logI("분해버튼을 찾을 수 없습니다.")
                return
            }
            smartClick(extractBtn)
            simpleClick()
            delayRandom(200, 300)

            if (moveWindow) {
                val okBtn = imageSearch(imgpathSynthesizeOKBtn)
                val mesoBtn = findInventory()
                if(okBtn == null || mesoBtn == null) {
                    logI("분해창을 찾지 못해 옮기기에 실패했습니다")
                } else {
                    val extractWindowTitle = Point(okBtn.x, okBtn.y-173)
                    val dragDestination = Point(mesoBtn.x-180, mesoBtn.y-386)
                    smartDrag(extractWindowTitle, dragDestination)
                }
            }

        }
    }

    suspend fun extractItem(item: Point) {
        extractItem(listOf(item))
    }

    private suspend fun clickExpandBtn(searchBtn: Point) {
        helper.imageSearchAndClick(Point(searchBtn.x-180, searchBtn.y), 100, 60, imgpathExpandBtn, 90.0)
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