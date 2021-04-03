package maple_tasks

import changeBlackAndWhite
import changeContract
import get
import getHeight
import logI
import moveMouseSmoothly
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import toMat
import winGetPos
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


open class AdditionalOptionTask : MapleBaseTask() {

    companion object {
        var cubeDelayMin = 1500 // 최소 1500 이상
        var cubeDelayMax = 1600

        const val BEGINNER = "초보자"
        const val COMMON = "공용"
        const val WORRIER = "전사"
        const val MAGICIAN = "법사"
        const val ARCHER = "궁수"
        const val THIEF = "도적"
        const val PIRATE = "해적"

        const val HAT = "모자"
        const val TOP = "상의"
        const val BOTTOM = "하의"
        const val FACE = "얼장"
        const val EYE = "눈장"
        const val EAR = "귀고리"
        const val BELT = "벨트"
        const val POCKET = "포켓"

        //직업별 아이템 배열
        val JOBS = arrayOf(WORRIER, MAGICIAN, ARCHER, THIEF, PIRATE)
        val CATES = arrayOf(HAT, TOP, BOTTOM)
    }

    val goodItems = arrayListOf<Point>()
    val baseImgPath = "img\\additionalOption"

    private val jobPointer =
        Pair(Imgcodecs.imread("$baseImgPath\\jobWhite.png"), Imgcodecs.imread("$baseImgPath\\jobRed.png"))

    private val categoryTemplates = hashMapOf<String, Mat>().apply {
        this[HAT] = Imgcodecs.imread("$baseImgPath\\hat.png")
        this[TOP] = Imgcodecs.imread("$baseImgPath\\top.png")
        this[BOTTOM] = Imgcodecs.imread("$baseImgPath\\bottom.png")
        this[FACE] = Imgcodecs.imread("$baseImgPath\\face.png")
        this[EYE] = Imgcodecs.imread("$baseImgPath\\eye.png")
        this[EAR] = Imgcodecs.imread("$baseImgPath\\ear.png")
        this[BELT] = Imgcodecs.imread("$baseImgPath\\belt.png")
        this[POCKET] = Imgcodecs.imread("$baseImgPath\\pocket.png")

        values.forEach {
            it.changeContract()
        }
    }

    private val optionNameTemplates = linkedMapOf<String, Mat>().apply {
        this[UpgradeItemTask.STR] = Imgcodecs.imread("$baseImgPath\\str.png")
        this[UpgradeItemTask.DEX] = Imgcodecs.imread("$baseImgPath\\dex.png")
        this[UpgradeItemTask.LUK] = Imgcodecs.imread("$baseImgPath\\luk.png")
        this[UpgradeItemTask.INT] = Imgcodecs.imread("$baseImgPath\\int.png")
        this[UpgradeItemTask.HP] = Imgcodecs.imread("$baseImgPath\\hp.png")
        this[UpgradeItemTask.ATT] = Imgcodecs.imread("$baseImgPath\\att.png")
        this[UpgradeItemTask.SPELL] = Imgcodecs.imread("$baseImgPath\\spell.png")
        this[UpgradeItemTask.ALL] = Imgcodecs.imread("$baseImgPath\\all.png")
//        this[UpgradeItemTask.DMG] = Imgcodecs.imread("$baseImgPath\\dmg.png")

        values.forEach {
            it.changeContract()
        }
    }
    private val optionValueTemplates = hashMapOf<Int, Mat>().apply {
        this[-1] = Imgcodecs.imread("$baseImgPath\\end.png")
        this[0] = Imgcodecs.imread("$baseImgPath\\0.png")
        this[1] = Imgcodecs.imread("$baseImgPath\\1.png")
        this[2] = Imgcodecs.imread("$baseImgPath\\2.png")
        this[3] = Imgcodecs.imread("$baseImgPath\\3.png")
        this[4] = Imgcodecs.imread("$baseImgPath\\4.png")
        this[5] = Imgcodecs.imread("$baseImgPath\\5.png")
        this[6] = Imgcodecs.imread("$baseImgPath\\6.png")
        this[7] = Imgcodecs.imread("$baseImgPath\\7.png")
        this[8] = Imgcodecs.imread("$baseImgPath\\8.png")
        this[9] = Imgcodecs.imread("$baseImgPath\\9.png")

        values.forEach {
            it.changeContract()
        }
    }

    private val addTemplate = Imgcodecs.imread("$baseImgPath\\add.png").apply {
        this.changeContract()
    }

    suspend fun checkItems(untilBlank: Boolean, moveToEnd: Boolean): ArrayList<String> {
        val blankList = arrayListOf<Point>()
        val items = findItems(untilBlank, blankList)
        val goodItemsInfo = arrayListOf<String>()
        goodItems.clear()

        helper.apply {
            moveMouseLB()
            items.reverse()
            var last = ""
            val unsortedList = arrayListOf<Pair<ItemInfo, Point>>()
            items.forEach { item ->
                getOptions(item, last)?.let {
                    last = it.getUid()
                    if (isOptionGood(it)) {
                        unsortedList.add(Pair(it, item))
                    }
                }

            }
            unsortedList.sortByDescending { it.first.getGrade().second }
            unsortedList.forEach {
                goodItems.add(it.second)
                goodItemsInfo.add(it.first.getInfoText())
            }

            if (moveToEnd) {
                if (blankList.isEmpty())
                    findItems(untilBlank, blankList)
                moveItemsToEnd(blankList)
            }
        }

        return goodItemsInfo

    }

    /**전달받은 아이템 목록을 인벤토리 제일 뒷쪽으로 이동시킨다. */
    suspend fun moveItemsToEnd(blankList: ArrayList<Point>, itemList: ArrayList<Point> = goodItems) {
        helper.apply {
            itemList.forEach {
                if (blankList.isNotEmpty()) {
                    while (!isItemEmpty(it)) {
                        smartClick(it, 10, 10, maxTime = 100)
                        smartClick(blankList.last(), 10, 10, maxTime = 100)
                        delayRandom(400, 440)
                    }
                    blankList.removeAt(blankList.lastIndex)
                }
            }
        }
    }

    /**전달받은 아이템 목록을 인벤토리 제일 뒷쪽으로 이동시킨다. */
    suspend fun moveItemsToEnd() {
        val blankList = arrayListOf<Point>()
        findItems(false, blankList)

        moveItemsToEnd(blankList)
        logI("이동한 아이템 수: ${goodItems.size}}")


    }

    //아이템 정보를 나누는 칸막이 (불투명하다)
    private val infoDivider = Imgcodecs.imread("$baseImgPath\\infoDivider.png")
    private val jobBeginner1 = Imgcodecs.imread("$baseImgPath\\jobBeginner1.png")
    private val jobBeginner2 = Imgcodecs.imread("$baseImgPath\\jobBeginner2.png")
    suspend fun getOptions(item: Point, beforeUid: String = ""): ItemInfo? {
        helper.apply {

            var isUpgraded: Boolean
            var reqLev: Int? = null
            var job = ""

            /**아이템 정보가 담김 화면을 캡쳐한다. */
            suspend fun getTotalInfoMat(moveDelay: Long): Mat? {
                val r = random.get(0, 5)
                moveMouseSmoothly(item, 10)
                kotlinx.coroutines.delay(moveDelay)

                var source: Mat
                return user32.winGetPos().let { win ->
                    val maxInfoWidth = 525  // 실제로는 520
                    var infoWidth = maxInfoWidth //아이템 정보창의 최대 넓이 (비교대상이 있는 경우 최대 넓이)
                    //마우스포인트 주변을 캡쳐하여 아이템 정보 확인
                    val mouse = getMousePos()
                    val startX = mouse.let {
                        val limitX = win.right - infoWidth //마우스위 x값이 이 수치를 넘어가면 아이템 정보를 가리고있는것이다.
                        if (mouse.x < limitX) {
                            //마우스가 아이템 정보 왼쪽에 있는 경우
                            infoWidth = 270 //왼쪽에 있는경우 270의 넓이만큼만 찾으면 아이템의 정보가 있다.
                            mouse.x - 5
                        } else {
                            //마우스가 아이템 정보 표시되는 범위 내에 있는경우
                            limitX - 5
                        }
                    }

                    source = createScreenCapture(Rectangle(startX, win.top, infoWidth + 3, win.getHeight())).toMat()
//                    if (Settings.instance.saveCapturedMatWhenCheckOption)
//                        Imgcodecs.imwrite("capture${Date().time} ${item.x} ${item.y}.png", source)

                    //일단 캡쳐된 이미지에 온전히 아이템 정보가 담겨있는지 확인하기 위해 infoDivider를 사용
                    val pointInSource = let {
                        if (infoWidth == maxInfoWidth) {
                            //이미지넓이가 maxInfoWidth 인 경우 왼쪽편부터 살펴본 뒤
                            // 없으면 마우스 기준으로 오른편
                            // 거기에도 없으면 오른편에서 찾는다.
                            val leftSide = source.colRange(0, 270)
                            val leftSideAtMouse = (mouse.x - startX - 5).let {
                                val startRange = if (it < 0) 0 else it
                                val endRange = (startRange + 270).let { if (it > source.cols()) source.cols() else it }
                                source.colRange(startRange, endRange)
                            }
                            val rightSide = source.colRange(250, source.cols())
                            if (imageSearchReturnBoolean(leftSide, infoDivider, 95.0))
                                source = leftSide
                            else if (leftSideAtMouse.cols() >= infoDivider.cols()
                                && imageSearchReturnBoolean(leftSideAtMouse, infoDivider, 95.0)
                            ) {
                                source = leftSideAtMouse
                            } else if (imageSearchReturnBoolean(rightSide, infoDivider, 95.0))
                                source = rightSide
                            else
                                return@let null
                        } else {
                            if (!imageSearchReturnBoolean(source, infoDivider, 95.0)) {
                                return@let null
                            }
                        }
                        imageSearch(source, jobBeginner1) ?: imageSearch(source, jobBeginner2).also { job = COMMON }
                    }

                    if (Settings.instance.saveCapturedMatWhenCheckOption)
                        Imgcodecs.imwrite("${Date().time} ${item.x} ${item.y}.png", source)

                    pointInSource?.let {
                        val startCol = it.x - 20
                        val endCol = startCol + 240
                        val startRow = it.y - 193
                        val endRow = (startRow + 440).let { if (it > source.rows()) source.rows() else it }
                        try {
                            source.colRange(startCol, endCol).rowRange(startRow, endRow)
                        } catch (e: Exception) {
                            if (Settings.instance.saveErrorWhenCheckOption)
                                Imgcodecs.imwrite("error $startCol $endCol $startRow $endRow.png", source)
                            logI(e.message)
                            null
                        }


                    } ?: let {
                        null
                    }
                }
            }

            //렉으로 인해 인식 실패할 가능성이 있으므로 인식 실패시 19번 더 시도
            val totalInfoMat = getTotalInfoMat(Settings.instance.delayOnCheckOptions) ?: let {
                simpleClick(KeyEvent.BUTTON3_MASK)  //
                var mat = getTotalInfoMat(50)
                for (i in 1..19) if (mat != null) break else mat = getTotalInfoMat(100)
                mat
            }
            if (totalInfoMat == null) logI("$item 인식 실패")
            totalInfoMat?.let {
                //이름에 강화표시 있나 확인
                it.rowRange(0, 75).let {
//                    Imgcodecs.imwrite("name.png", it)
                    isUpgraded = checkUpgraded(it)
                }

                //140제,150제 여부 확인 (둘다 아닐경우 null)
                it.rowRange(128, 139).colRange(140, 215).let {
                    reqLev = checkRequireLevel(it)
//                    Imgcodecs.imwrite("ReqLev.png", it)
                }

                val infoImg = it.rowRange(190, it.rows())
                if (job.isEmpty()) {
                    val jobView = infoImg.rowRange(3, 13).colRange(20, 230)
                    job = checkJob(jobView)
                }
                infoImg.changeContract()
                kotlinx.coroutines.delay(1)
//                Imgcodecs.imwrite("test.png", infoImg)
                val resultOption = check(infoImg)
                val category = checkCategory(infoImg)
                val result = ItemInfo(job, category, resultOption).apply {
                    this.isUpgraded = isUpgraded
                    this.reqLev = reqLev
                }
                if (beforeUid == result.getUid()) {
                    if (Settings.instance.logFindSameOptionWhenCheckOption)
                        logI("옵션 확인중 중복 옵션이 측정됨 (해당 위치의 아이템을 다시 확인합니다.)")
                    if (Settings.instance.saveErrorWhenCheckOption)
                        Imgcodecs.imwrite("${item.x} ${item.y} same.png", totalInfoMat)
                    return getOptions((item))// 재귀시에는 beforeUid 값을 주지 않기때문에 한번만 더 반복하고 리턴한다.
                }
                return result

            }
            kotlinx.coroutines.delay(1)
            return null
        }
    }

    private val upgradeTemplate = Imgcodecs.imread("$baseImgPath\\upgraded.png").apply { changeBlackAndWhite() }

    /**아이템 강화가 적용되었는지 판단하여 반환 */
    private fun checkUpgraded(nameSource: Mat): Boolean {
        nameSource.changeBlackAndWhite()
        return helper.imageSearchReturnBoolean(nameSource, upgradeTemplate)
    }

    /**레벨제한이 150 이상인지 판단*/
    private val reqLev150 = Imgcodecs.imread("$baseImgPath\\req150.png").apply { changeBlackAndWhite() }
    private val reqLev140 = Imgcodecs.imread("$baseImgPath\\req140.png").apply { changeBlackAndWhite() }
    private fun checkRequireLevel(source: Mat): Int? {
        source.changeBlackAndWhite()
        if (helper.imageSearchReturnBoolean(source, reqLev150))
            return 150
        if (helper.imageSearchReturnBoolean(source, reqLev140))
            return 140
        return null
    }

    private fun checkCategory(infoImg: Mat): String {
        var result = ""
        helper.apply {
            run {
                categoryTemplates.forEach { (category, tem) ->
                    if (imageSearchReturnBoolean(infoImg, tem, 90.0)) {
                        result = category
                        return@run
                    }
                }
            }

        }

        return result
    }

    private fun checkJob(jobView: Mat): String {
        helper.apply {
            val p = imageSearch(jobView, jobPointer.first, 95.0) ?: imageSearch(jobView, jobPointer.second, 95.0)
            if (p == null) {
                logI("직업을 식별할 수 없습니다.")
                return ""
            } else {
                val x = p.x
//                logI(x)
                return when {
                    x < 70 -> {
                        WORRIER
                    }
                    x < 115 -> {
                        MAGICIAN
                    }
                    x < 150 -> {
                        ARCHER
                    }
                    x < 183 -> {
                        THIEF
                    }
                    x < 210 -> {
                        PIRATE
                    }
                    else -> {
                        logI("직업을 식별할 수 없습니다.")
                        ""
                    }
                }
            }

        }
    }

    fun isOptionGood(itemInfo: ItemInfo): Boolean {
        return isOptionGood(itemInfo.job, itemInfo.category, itemInfo.option)
    }

    /**추가옵션 값을 확인후 유효한지 여부를 판단한다. */
    private fun isOptionGood(job: String, category: String, option_: java.util.HashMap<String, Int>): Boolean {
        val option = option_.clone() as HashMap<String, Int>
        //targetOptions: 아이템의 직업군, 종류에 따라 목표 옵션을 지정한다.
        val targetOptions = hashMapOf<String, Int>().apply {
            if (category == FACE) {
                this[UpgradeItemTask.STR] = 50
                this[UpgradeItemTask.DEX] = 50
                this[UpgradeItemTask.LUK] = 50
                this[UpgradeItemTask.INT] = 50
                this[UpgradeItemTask.HP] = 2300
            } else if (category == EYE) {
                this[UpgradeItemTask.STR] = 50
                this[UpgradeItemTask.DEX] = 50
                this[UpgradeItemTask.LUK] = 50
                this[UpgradeItemTask.INT] = 50
                this[UpgradeItemTask.HP] = 2100
            } else {
                this[UpgradeItemTask.STR] = 70
                this[UpgradeItemTask.DEX] = 70
                this[UpgradeItemTask.LUK] = 70
                this[UpgradeItemTask.INT] = 70
                this[UpgradeItemTask.HP] = 3150
            }

            if (category == EAR) {
                this[UpgradeItemTask.HP] = 2700
            } else if (category == POCKET) {
                this[UpgradeItemTask.DEX] = 80
                this[UpgradeItemTask.LUK] = 75
                this[UpgradeItemTask.INT] = 80
                this[UpgradeItemTask.HP] = 2900
            }

            when (job) {
                WORRIER -> {
                    this.remove(UpgradeItemTask.DEX)
                    this.remove(UpgradeItemTask.LUK)
                    this.remove(UpgradeItemTask.INT)
                }

                MAGICIAN -> {
                    this.remove(UpgradeItemTask.STR)
                    this.remove(UpgradeItemTask.LUK)
                    this.remove(UpgradeItemTask.DEX)
                    this.remove(UpgradeItemTask.HP)
                }

                ARCHER -> {
                    this.remove(UpgradeItemTask.STR)
                    this.remove(UpgradeItemTask.LUK)
                    this.remove(UpgradeItemTask.INT)
                    this.remove(UpgradeItemTask.HP)
                }

                THIEF -> {
                    this.remove(UpgradeItemTask.STR)
                    this.remove(UpgradeItemTask.DEX)
                    this.remove(UpgradeItemTask.INT)
                    this.remove(UpgradeItemTask.HP)
                }

                PIRATE -> {
                    this.remove(UpgradeItemTask.LUK)
                    this.remove(UpgradeItemTask.INT)
                    this.remove(UpgradeItemTask.HP)
                }
            }

        }

        if(job != COMMON) {
            option.forEach { t, u ->
                if(u > 49) {
                    //깡추가 50이 넘는 경우 6 추가하여 계산
                    option[t] = u+3
                }
            }
        }


        option[UpgradeItemTask.ALL]?.let {
            val state = it * 10
            option[UpgradeItemTask.STR] = option[UpgradeItemTask.STR]?.plus(state) ?: state
            option[UpgradeItemTask.DEX] = option[UpgradeItemTask.DEX]?.plus(state) ?: state
            option[UpgradeItemTask.LUK] = option[UpgradeItemTask.LUK]?.plus(state) ?: state
            option[UpgradeItemTask.INT] = option[UpgradeItemTask.INT]?.plus(state) ?: state
        }

        option[UpgradeItemTask.SPELL]?.let {
//            val state = it * 4    // 인트가 비싸지면 사용
            val state = it * 2  // INT50 마력5  같은 경우를 제외시키기 위해 일부러 배율을 2로 낮춘다
            option[UpgradeItemTask.INT] = option[UpgradeItemTask.INT]?.plus(state) ?: state
        }

        option[UpgradeItemTask.ATT]?.let {
            val state = it * 3
            val lowState = it * 2   //덱스같은 인기 없는 스텟을 위의 인트와 같은 이유로 배율을 낮게 곱하여 계산
            option[UpgradeItemTask.STR] = option[UpgradeItemTask.STR]?.plus(state) ?: state
            option[UpgradeItemTask.DEX] = option[UpgradeItemTask.DEX]?.plus(state) ?: state
            /**추후 덱스가 비싸지면 그냥 state 사용*/
            option[UpgradeItemTask.LUK] = option[UpgradeItemTask.LUK]?.plus(state) ?: state
            option[UpgradeItemTask.HP] = option[UpgradeItemTask.HP]?.plus(it * 140) ?: it * 140
        }

        var result = false
        kotlin.run {
            option.forEach { (t, u) ->
                targetOptions[t]?.let {
                    if (it <= u) {
                        result = true
                        return@run
                    }
                }
            }
        }

        return result
    }

    /**이미지를 확인후 추가옵션을 반환한다. */
    fun check(infoImg: Mat): LinkedHashMap<String, Int> {
        val resultOption = linkedMapOf<String, Int>()

        helper.apply {
            optionNameTemplates.forEach { (optionName, nt) ->
                imageSearch(infoImg, nt, 20.0)?.let {
                    val rowRange = infoImg.rowRange(it.y, it.y + 10)
                    imageSearch(rowRange, addTemplate)?.let {
                        val optionMat = rowRange.colRange(it.x + 6, it.x + 45)
                        val optionWidth = 6
                        var value = 0
                        for (i in 0..5) {
                            val start = 0 + (i * optionWidth)
                            val targetMat = optionMat.colRange(start, start + optionWidth + 1)
                            var find = false
                            kotlin.run {
                                optionValueTemplates.forEach { (v, vt) ->
//                                    logI("$optionName, $value 시도")
                                    if (imageSearchReturnBoolean(targetMat, vt, 95.0)) {
                                        find = true
                                        if (v < 0)
                                            find = false
                                        else {
//                                            Imgcodecs.imwrite("$optionName $i.jpg", targetMat)
                                            value = value * 10 + v
                                        }

                                        return@run
                                    }
                                }
                            }

                            if (!find)
                                break
                        }
                        if (value > 0) {
                            resultOption[optionName] = value
//                            logI("$optionName:$value")
                        }
                    }
                }
            }
        }

        return resultOption
    }

}