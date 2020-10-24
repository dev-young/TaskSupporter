package maple_tasks

import changeContract
import logI
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import toMat
import java.awt.Point
import java.awt.Rectangle
import java.io.File

class MarketConditionTask : AdditionalOptionTask() {
    private val auctionTask by lazy { AuctionTask() }
    private var defaultImgPath = "img\\auction"

    private var resultScreenRect: Rectangle? = null //검색 결과 캡쳐 범위
    private var nextPagePoint: Point? = null //다음 페이지 버튼 위치

    private val valueTemplates = linkedMapOf<Int, Mat>().apply {
        this[0] = Imgcodecs.imread("$defaultImgPath\\0.png")
        this[9] = Imgcodecs.imread("$defaultImgPath\\9.png")
        this[3] = Imgcodecs.imread("$defaultImgPath\\3.png")
        this[1] = Imgcodecs.imread("$defaultImgPath\\1.png")
        this[2] = Imgcodecs.imread("$defaultImgPath\\2.png")
        this[4] = Imgcodecs.imread("$defaultImgPath\\4.png")
        this[5] = Imgcodecs.imread("$defaultImgPath\\5.png")
        this[6] = Imgcodecs.imread("$defaultImgPath\\6.png")
        this[7] = Imgcodecs.imread("$defaultImgPath\\7.png")
        this[8] = Imgcodecs.imread("$defaultImgPath\\8.png")
        this[-1] = Imgcodecs.imread("$defaultImgPath\\comma.png")
        this[-2] = Imgcodecs.imread("$defaultImgPath\\dash.png")
        values.forEach {
            it.changeContract()
        }
    }

    suspend fun makeInfo(filePath: String, maxCount: Int = Int.MAX_VALUE): ArrayList<ItemInfo> {
        //아이템 정보 불러오기
        val itemList = loadItemList(filePath) ?: arrayListOf(arrayOf("", "", "", "", "", ""))
        itemList.forEach { logI(it.contentToString()) }
        val itemTotalList = arrayListOf<ItemInfo>() //아이템 시세 목록
        helper.apply {
            //아이템 정보 입력
            itemList.forEach { itemInfo ->
                val targetCategory = itemInfo[0]
                val targetName = itemInfo[1]
                val targetPrice = itemInfo[2]
                val potentialGrade = if (itemInfo.size > 3 && itemInfo[3].length == 1) itemInfo[3].toInt() else -1
                val targetClickReset = if (itemInfo.size > 4) itemInfo[4].contains("t") else false


                //시세탭 클릭
                clickMarketConditionTab()
                delayRandom(30, 50)
                auctionTask.clickCategory(targetCategory)
                delayRandom(30, 50)
                auctionTask.clickCategory(targetCategory)

                val success = auctionTask.inputItemInfo(
                    targetName,
                    "_",
                    targetClickReset,
                    minPrice = targetPrice,
                    potentialGrade = potentialGrade
                )
                if (success) {
                    delayRandom(30, 50)

                    //검색
                    auctionTask.searchItem()
                    delayRandom(200, 400)


                    val height = 55 // 각 아이템의 최상단간의 거리
                    val heightArr = IntArray(9)
                    heightArr.forEachIndexed { index, _ -> heightArr[index] = index * height + 19 }

                    var counter = 0 //가격정보 추출 횟수 (중복된 아이템을 카운트 할 수 있다)
                    var lastResult = ""
                    while (counter < maxCount) {
                        //결과 캡쳐
                        val priceDateList = arrayListOf<Pair<Pair<Long, String>, String>>()
                        captureResult()?.let { capture ->
                            for (i in 0 until 9) {

                                val startRow = heightArr[i]
                                val endRow = startRow + 11
                                val priceTem = capture.rowRange(startRow, endRow).colRange(0, 95)
                                val dateTem = capture.rowRange(startRow + 7, endRow + 7).colRange(294, 355)
                                priceTem.changeContract()
                                dateTem.changeContract()
//                                Imgcodecs.imwrite("testDate.png", dateTem)

                                //가격 정보 및 날짜 정보 추출
                                val price = getPrice(priceTem)
                                if (price.first > 0) {
                                    val date = getDate(dateTem)
                                    priceDateList.add(Pair(price, date))
                                    counter++
//                                    logI("${price.second} [$counter]")
                                } else {
                                    break
                                }

                                if (counter >= maxCount) break
                            }
                        }

                        //최근 가격 목록과 비교하여 목록이 일치하면 마지막 결과인걸로 판단하여 반복 종료
                        val result = priceDateList.toString()
                        if (lastResult == result || nextPagePoint == null) {
                            //마지막페이지
//                            logI("마지막페이지 확인 counter=$counter")
                            break
                        }
                        lastResult = result


                        //새로운 목록일 경우 아이템 가격정보에 맞춰 해당 아이템 옵션 확인 및 저장
                        val nextItemPoint = Point(nextPagePoint!!.x - 389, nextPagePoint!!.y + 59)
                        var last = ""
                        priceDateList.forEachIndexed { idx, priceDate ->
                            getOptions(nextItemPoint)?.let {
                                it.price = priceDate.first.first
                                it.priceText = priceDate.first.second
                                it.dateText = priceDate.second
                                it.name = targetName
                                itemTotalList.add(it)
                            }
                            nextItemPoint.y = nextItemPoint.y + height
                        }

                        if (priceDateList.size < 9) {
                            break
                        }


                        //다음 페이지 클릭
                        smartClick(nextPagePoint!!, randomRangeX = 2, randomRangeY = 2, maxTime = 100)
                        delayRandom(100, 150)
                    }


                } else {
                    logI("아이템 정보 입력을 실패했습니다. [$targetName, $targetPrice]")
                    logI("다음 아이템으로 건너뜁니다.")
                }
            }
        }

        return itemTotalList
    }

    private fun getPrice(mat: Mat): Pair<Long, String> {
        val blackPoint = mat[0, 0]
        var start = 35
        val width = mat.cols()
        val height = mat.rows() - 1

        //첫번재 칸 구하기
        for (i in 1..height)
            for (j in 1..width) {
                if (start <= j) break
                val dot = mat[i, j]
                if (dot != null && !dot.contentEquals(blackPoint)) start = j
            }

        start -= 3
        if (start < 0) start = 0

        val optionMat = mat.colRange(start, width)
        var optionWidth = 6 //첫 글자만 넓게 시작하기 위해서
        var value = 0L
        var strValue = ""
        var startCol = 0
//        Imgcodecs.imwrite("test.png", optionMat)

        //첫글자 정확한 위치 좌표 구하기
        val targetMat = optionMat.colRange(startCol, optionWidth + 4)
//        Imgcodecs.imwrite("test0.png", targetMat)
        kotlin.run {
            valueTemplates.forEach { (v, vt) ->
                val point = helper.imageSearch(targetMat, vt, 98.0)
                point?.let {
                    if (v < 0) {
                        //좌표 찾기 실패
                    } else {
                        value = value * 10 + v
                        strValue += v.toString()

                        startCol = it.x - 1
                    }
                    return@run
                }
            }
            //좌표 찾기 실패
        }

        for (i in 1..14) {
            startCol += optionWidth
            val targetMat = optionMat.colRange(startCol, startCol + optionWidth + 1)
//            Imgcodecs.imwrite("test$i.png", targetMat)
            var notFound = false
            kotlin.run {
                valueTemplates.forEach { (v, vt) ->
                    if (helper.imageSearchReturnBoolean(targetMat, vt, 98.0)) {
                        if (v == -1) {
                            startCol -= 3
                            strValue = "$strValue,"
                        } else {
                            value = value * 10 + v
                            strValue += v.toString()
                        }
//                        logI("$v 찾음")
                        return@run
                    }
                }
//                logI("끝!!!")
                notFound = true
            }
            if (notFound)
                break
        }
//        logI(strValue)


        return Pair(value, strValue)
    }

    private fun getDate(mat: Mat): String {

        var optionWidth = 6
        var strValue = ""
        var startCol = 0

        for (i in 0..9) {
            val targetMat = mat.colRange(startCol, startCol + optionWidth + 1)
//            Imgcodecs.imwrite("date$i.png", targetMat)
            var notFound = false
            startCol += optionWidth
            kotlin.run {
                valueTemplates.forEach { (v, vt) ->
                    if (helper.imageSearchReturnBoolean(targetMat, vt, 98.0)) {
                        if (v > -1) {
                            strValue += v.toString()
                        } else {
                            strValue = "$strValue-"
                        }
                        return@run
                    }
                }
                notFound = true
            }
            if (notFound)
                break
        }


        return strValue
    }

    private fun captureResult(): Mat? {
        helper.apply {
            if (resultScreenRect == null) {
                val point = imageSearch("$defaultImgPath\\searchResult.png") ?: return null
                resultScreenRect = Rectangle(point.x + 320, point.y + 50, 370, 500)
                nextPagePoint = Point(point.x + 401, point.y + 7)
            }

            return createScreenCapture(resultScreenRect).toMat()

        }
    }

    /**파일로부터 검색할 아이템 목록을 가져온다.
     * Array<String> = {분류, 템이름, 최소가격, 잠재등급, reset}
     * 분류 = 방어구, 무기, 소비, 캐시, 기타
     * 템이름 = 공백없이 작성
     * 잠재등급  = -1 ~ 5 (default = -1)
     * reset = 초기화버튼 클릭 여부 (default = false)
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
                        //각 항목 공백 제거
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

    suspend fun clickMarketConditionTab() {
        helper.apply {
            val point = imageSearchAndClick("$defaultImgPath\\marketCondition.png", maxTime = 200)
            if (point == null) {
                return
            }
            simpleClick()
        }
    }

    suspend fun findMarketCondition(
        itemPosition: Point,
        useSmartSearch: Boolean
    ): Pair<ItemInfo?, List<ItemInfo>> {
        val result = arrayListOf<ItemInfo>()
        var itemInfo: ItemInfo? = null
        getOptions(itemPosition)?.let {
            itemInfo = it
            val upgraded = if (it.isUpgraded == true) "강화된 " else ""
            logI("$upgraded${it.getInfoText()}")
            val im = ItemManager()
            result.addAll(if (useSmartSearch) im.smartSearch(it) else im.search(it))
        }

        return Pair(itemInfo, result)
    }
}
