package maple_tasks

import changeBlackAndWhite
import changeContract
import logI
import moveMouseSmoothly
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import toMat
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.util.*
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

    suspend fun checkItems(untilBlank: Boolean, moveToEnd:Boolean): ArrayList<String> {
        val blankList = arrayListOf<Point>()
        val items = findItems(untilBlank, blankList)
        val goodItemsInfo = arrayListOf<String>()
        goodItems.clear()

        helper.apply {
            moveMouseLB()
            items.forEach { item ->

                getOptions(item)?.let {
                    val optionStr = it.getInfoText()
                    if (isOptionGood(it)) {
                        goodItems.add(item)
                        goodItemsInfo.add(optionStr)
                    }
                }

            }

            if(moveToEnd) {
                if(blankList.isEmpty())
                    findItems(untilBlank, blankList)
                moveItemsToEnd(blankList)
            }
        }

        return goodItemsInfo

    }

    /**전달받은 아이템 목록을 인벤토리 제일 뒷쪽으로 이동시킨다. */
    suspend fun moveItemsToEnd(blankList: ArrayList<Point>, itemList: ArrayList<Point> = goodItems){
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

    suspend fun getOptions(item: Point): ItemInfo? {
        helper.apply {
            moveMouseSmoothly(item, 15)
            delayRandom(10, 20)
            mousePress(KeyEvent.BUTTON3_MASK)
            delayRandom(30, 40)

            var isUpgraded : Boolean
            var job = ""
            val jobPoint =
                imageSearch("$baseImgPath\\jobBeginner1.png")
                    ?: imageSearch("$baseImgPath\\jobBeginner2.png")?.also { job = COMMON }
            jobPoint?.let {
                //createScreenCapture(Rectangle(jobPoint.x - 20, jobPoint.y - 3, 240, 300)
                val temp = createScreenCapture(Rectangle(jobPoint.x - 20, jobPoint.y - 193, 240, 440)).toMat()

                //이름에 강화표시 있나 확인
                temp.rowRange(0, 75).let {
//                    Imgcodecs.imwrite("name.png", it)
                    isUpgraded = checkUpgraded(it)
                }

                val infoImg = temp.rowRange(190, temp.rows())
                if (job.isEmpty()) {
                    val jobView = infoImg.rowRange(3, 13).colRange(20, 230)
                    job = checkJob(jobView)
                }
                infoImg.changeContract()
                mouseRelease(KeyEvent.BUTTON3_MASK)
//                Imgcodecs.imwrite("test.png", infoImg)
                val resultOption = check(infoImg)
                val category = checkCategory(infoImg)
                return ItemInfo(job, category, resultOption).apply { this.isUpgraded = isUpgraded }

            }
            mouseRelease(KeyEvent.BUTTON3_MASK)
            return null
        }
    }

    private val upgradeTemplate = Imgcodecs.imread("$baseImgPath\\upgraded.png").apply { changeBlackAndWhite() }
    /**아이템 강화가 적용되었는지 판단하여 반환 */
    private fun checkUpgraded(nameSource: Mat): Boolean {
        nameSource.changeBlackAndWhite()
        return helper.imageSearchReturnBoolean(nameSource, upgradeTemplate)
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

    private fun isOptionGood(itemInfo: ItemInfo): Boolean {
        return isOptionGood(itemInfo.job, itemInfo.category, itemInfo.option)
    }

    /**추가옵션 값을 확인후 유효한지 여부를 판단한다. */
    private fun isOptionGood(job: String, category: String, option: java.util.HashMap<String, Int>): Boolean {
        val targetOptions = hashMapOf<String, Int>().apply {
            if (category == FACE) {
                this[UpgradeItemTask.STR] = 40
                this[UpgradeItemTask.DEX] = 40
                this[UpgradeItemTask.LUK] = 40
                this[UpgradeItemTask.INT] = 40
                this[UpgradeItemTask.HP] = 2300
            } else if (category == EYE) {
                this[UpgradeItemTask.STR] = 40
                this[UpgradeItemTask.DEX] = 40
                this[UpgradeItemTask.LUK] = 40
                this[UpgradeItemTask.INT] = 40
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

        option[UpgradeItemTask.ALL]?.let {
            val state = it * 10
            option[UpgradeItemTask.STR] = option[UpgradeItemTask.STR]?.plus(state) ?: state
            option[UpgradeItemTask.DEX] = option[UpgradeItemTask.DEX]?.plus(state) ?: state
            option[UpgradeItemTask.LUK] = option[UpgradeItemTask.LUK]?.plus(state) ?: state
            option[UpgradeItemTask.INT] = option[UpgradeItemTask.INT]?.plus(state) ?: state
        }

        option[UpgradeItemTask.SPELL]?.let {
            val state = it * 4
            option[UpgradeItemTask.INT] = option[UpgradeItemTask.INT]?.plus(state) ?: state
        }

        option[UpgradeItemTask.ATT]?.let {
            val state = it * 4
            option[UpgradeItemTask.STR] = option[UpgradeItemTask.STR]?.plus(state) ?: state
            option[UpgradeItemTask.DEX] = option[UpgradeItemTask.DEX]?.plus(state) ?: state
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