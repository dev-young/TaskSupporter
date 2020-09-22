package maple_tasks

import changeContract
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import logI
import moveMouseSmoothly
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import toMat
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.io.*
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

    suspend fun checkItems(untilBlank: Boolean): ArrayList<String> {
        val blankList = arrayListOf<Point>()
        val items = findItems(untilBlank, blankList)
        val goodItemsInfo = arrayListOf<String>()
        goodItems.clear()

        helper.apply {
            items.forEach { item ->

                getOptions(item)?.let {
                    val optionStr = it.getInfoText()
                    if (isOptionGood(it)) {
                        goodItems.add(item)
                        goodItemsInfo.add(optionStr)
                    }
                }

            }
        }

        return goodItemsInfo

    }

    /**전달받은 아이템 목록을 인벤토리 제일 뒷쪽으로 이동시킨다. */
    suspend fun moveItemsToEnd() {
        val blankList = arrayListOf<Point>()
        val items = findItems(false, blankList)

        helper.apply {

            goodItems.forEach {
                if (blankList.isNotEmpty()) {
                    smartClick(it, 10, 10, maxTime = 100)
                    smartClick(blankList.last(), 10, 10, maxTime = 100)
                    blankList.removeAt(blankList.lastIndex)
                    delayRandom(400, 500)
                }
            }
        }


    }

    suspend fun getOptions(item: Point): ItemInfo? {
        helper.apply {
            moveMouseSmoothly(item, 30)
            delayRandom(10, 30)
            mousePress(KeyEvent.BUTTON3_MASK)
            delayRandom(30, 50)

            var job = ""
            val jobPoint =
                imageSearch("$baseImgPath\\jobBeginner1.png")
                    ?: imageSearch("$baseImgPath\\jobBeginner2.png")?.also { job = COMMON }
            jobPoint?.let {
                val temp = createScreenCapture(Rectangle(jobPoint.x - 20, jobPoint.y - 3, 240, 300))
//                    temp.toFile("test")
                val infoImg = temp.toMat()
                if (job.isEmpty()) {
                    val jobView = infoImg.rowRange(3, 13).colRange(20, 230)
//                        ul
                    job = checkJob(jobView)
                }
                infoImg.changeContract()
                mouseRelease(KeyEvent.BUTTON3_MASK)
//                    Imgcodecs.imwrite("test.png", infoImg)
                val resultOption = check(infoImg)
                val category = checkCategory(infoImg)
                return ItemInfo(job, category, resultOption)

            }
            mouseRelease(KeyEvent.BUTTON3_MASK)
            return null
        }
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

    class ItemManager {
        companion object {
            val categoryMap = linkedMapOf<String, SortedMap<String, ArrayList<ItemInfo>>>().apply {
                JOBS.forEach { job ->
                    CATES.forEach {
                        put("[$job][$it]", sortedMapOf())
                    }
                }
            }

            val itemMap = hashMapOf<String, ItemInfo>()
            val sortedList = arrayListOf<ItemInfo>()
        }

        fun add(info: ItemInfo) {
            if (!itemMap.contains(info.getUid())) {
                val cate1 = info.getCategory1()
                val gradeKey = info.getGradeKey()
                val gradeMap =
                    categoryMap.computeIfAbsent(cate1) { sortedMapOf(Pair(gradeKey, arrayListOf())) }
                val list = gradeMap.computeIfAbsent(gradeKey) { arrayListOf() }
                list.add(info)

                itemMap[info.getUid()] = info
            }
        }

        /**정보들을 가져와서 중복안되도록 저장한다. */
        fun addAll(infoList: Collection<ItemInfo>) {
            infoList.forEach { info ->
                add(info)
            }
        }

        fun getSortedList(): ArrayList<ItemInfo> {
            sortedList.clear()
            categoryMap.values.forEach { gradeMap ->
                gradeMap.values.forEach {
                    it.sortWith(kotlin.Comparator { o1, o2 -> -o1.price.compareTo(o2.price) })
                    sortedList.addAll(it)
                }
            }

            return sortedList
        }

        fun clear() {
            categoryMap.values.forEach { it.clear() }
            itemMap.clear()
            sortedList.clear()
        }


        fun search(info: ItemInfo): ArrayList<ItemInfo> {
            val result = arrayListOf<ItemInfo>()

            val cateList = arrayListOf<String>()
            if(info.category == BELT) {
                info.name = "쿰의벨"
                cateList.add(info.getCategory1())
                info.name = "골든클"
                cateList.add(info.getCategory1())
            } else
                cateList.add(info.getCategory1())
            val gradeKey = info.getGradeKey()

            cateList.forEach {cate1 ->
                val gradeMap = categoryMap[cate1]
                gradeMap?.let {
                    gradeMap.computeIfAbsent(gradeKey) { arrayListOf() }
                    val keys = gradeMap.keys.toList()
                    for (i in keys.indices) {
                        val key = keys[i]
                        if(key == gradeKey) {
                            intArrayOf(i-1, i, i+1).forEach {
                                if(it > -1 && it < keys.size) {
                                    gradeMap[keys[it]]?.let {
                                        it.sortWith(kotlin.Comparator { o1, o2 -> -o1.price.compareTo(o2.price) })
                                        result.addAll(it)
                                    }

                                }
                            }
                            break
                        }

                    }
                }
            }


            return result
        }

        fun loadFromTxt(fileName: String): MutableCollection<ItemInfo> {
            val reader = BufferedReader(
                InputStreamReader(
                    FileInputStream("$fileName DB"), "euc-kr"
                )
            )
            reader.forEachLine {
                val item = ItemInfo.fromJson(it)
                add(item)
            }


            return itemMap.values
        }

        fun saveToDB(fileName: String = "메이플시세목록", overwrite: Boolean) {
            if (itemMap.isEmpty()) return

            val file = File("$fileName DB")
            val bw = BufferedWriter(FileWriter(file, !overwrite))

            // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
            // 이미 파일이 존재할 경우 덮어쓰기로 저장
            try {
                itemMap.values.forEach {
                    bw.write(it.toDB())
                    bw.newLine()
                }
            } catch (e: FileNotFoundException) {
                logI("FileNotFound: $fileName")
            }

            bw.flush()
            bw.close()

        }

        fun saveToTxt(fileName: String = "메이플시세목록", saveDB: Boolean = true) {
            val file = File("$fileName.txt")
            val bw = BufferedWriter(FileWriter(file, false))

            // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
            // 이미 파일이 존재할 경우 덮어쓰기로 저장
            try {
                var lastCategory = ""
                var lastGrade = ""
                sortedList.forEach {
                    if (lastCategory != it.getCategory1()) {
                        bw.newLine()
                        bw.newLine()
                        lastCategory = it.getCategory1()
                        bw.write(lastCategory)
                        lastGrade = ""
                    }

                    if (lastGrade != it.getGradeKey()) {
                        bw.newLine()
                        lastGrade = it.getGradeKey()
                        bw.write("  [${it.getGrade().first} ${it.getGrade().second}]")
                        bw.write("  ")
                    }

                    bw.write("${it.getPriceAndDateAndOption()} > ")
                }
            } catch (e: FileNotFoundException) {
                logI("FileNotFound: $fileName")
            }

            bw.flush()
            bw.close()

        }
    }

    /**아이템 정보를 담는 class*/
    class ItemInfo(
        @Expose val job: String,
        @Expose val category: String,
        @Expose val option: LinkedHashMap<String, Int>
    ) : Serializable {
        @Expose
        var name = ""
        @Expose
        var price = 0L
        @Expose
        var priceText = ""
        @Expose
        var dateText = ""
        var dateTextSimple = ""

        private var gradeKey = ""
        @Expose
        private var grade: Pair<String, Int>? = null
        private var uid: String? = null

        fun getUid(): String {
            if (uid == null) {
                if (category == BELT)
                    uid = "$job>$name$category>$option>$price>$dateText"
                else
                    uid = "$job>$category>$option>$price>$dateText"
            }
            return uid!!
        }

        /**추옵이 몇급인지 반환
         * <"STR", 120> 반환*/
        fun getGrade(): Pair<String, Int> {
            if (grade != null) return grade!!

            val statOrder = hashMapOf(
                Pair(UpgradeItemTask.STR, 4),
                Pair(UpgradeItemTask.LUK, 3),
                Pair(UpgradeItemTask.DEX, 2),
                Pair(UpgradeItemTask.INT, 1)
            )
            val tempOptions = hashMapOf<String, Int>()
            when (job) {
                WORRIER -> {
                    var str = option[UpgradeItemTask.STR] ?: 0
                    str += (option[UpgradeItemTask.ALL] ?: 0) * 10
                    str += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0

                    if (str < 60) {
                        var hp = option[UpgradeItemTask.HP] ?: 0
                        hp += option[UpgradeItemTask.ATT]?.let { it * 140 } ?: 0

                        if (hp > 1500)
                            return Pair(UpgradeItemTask.HP, hp)
                        else
                            return Pair(UpgradeItemTask.STR, str)
                    } else {
                        return Pair(UpgradeItemTask.STR, str)
                    }
                }

                MAGICIAN -> {
                    var int = option[UpgradeItemTask.INT] ?: 0
                    int += (option[UpgradeItemTask.ALL] ?: 0) * 10
                    int += option[UpgradeItemTask.SPELL]?.let { it * 4 } ?: 0
                    return Pair(UpgradeItemTask.INT, int)
                }

                ARCHER -> {
                    var dex = option[UpgradeItemTask.DEX] ?: 0
                    dex += (option[UpgradeItemTask.ALL] ?: 0) * 10
                    dex += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0
                    return Pair(UpgradeItemTask.DEX, dex)
                }

                THIEF -> {
                    var luc = option[UpgradeItemTask.LUK] ?: 0
                    luc += (option[UpgradeItemTask.ALL] ?: 0) * 10
                    luc += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0
                    return Pair(UpgradeItemTask.LUK, luc)
                }

                PIRATE -> {
                    var dex = option[UpgradeItemTask.DEX] ?: 0
                    dex += (option[UpgradeItemTask.ALL] ?: 0) * 10
                    dex += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0

                    var str = option[UpgradeItemTask.STR] ?: 0
                    str += option[UpgradeItemTask.ALL]?.let { it * 10 } ?: 0
                    str += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0

                    if (dex - str > 2) {
                        return Pair(UpgradeItemTask.DEX, dex)
                    } else {
                        return Pair(UpgradeItemTask.STR, str)
                    }
                }
                else -> {
                    option.forEach { (t, u) ->
                        when (t) {
                            UpgradeItemTask.ALL -> {
                                val v = 10 * u
                                tempOptions[UpgradeItemTask.STR] = tempOptions[UpgradeItemTask.STR]?.let { it + v } ?: v
                                tempOptions[UpgradeItemTask.INT] = tempOptions[UpgradeItemTask.INT]?.let { it + v } ?: v
                                tempOptions[UpgradeItemTask.DEX] = tempOptions[UpgradeItemTask.DEX]?.let { it + v } ?: v
                                tempOptions[UpgradeItemTask.LUK] = tempOptions[UpgradeItemTask.LUK]?.let { it + v } ?: v
                            }
                            UpgradeItemTask.ATT -> {
                                val v = 4 * u
                                tempOptions[UpgradeItemTask.STR] = tempOptions[UpgradeItemTask.STR]?.let { it + v } ?: v
                                tempOptions[UpgradeItemTask.DEX] = tempOptions[UpgradeItemTask.DEX]?.let { it + v } ?: v
                                tempOptions[UpgradeItemTask.LUK] = tempOptions[UpgradeItemTask.LUK]?.let { it + v } ?: v
                                // TODO: hp 옵션
                            }
                            UpgradeItemTask.SPELL -> {
                                val v = 4 * u
                                tempOptions[UpgradeItemTask.INT] = tempOptions[UpgradeItemTask.INT]?.let { it + v } ?: v
                            }
                            UpgradeItemTask.HP -> {

                            }
                            else -> {
                                tempOptions[t] = tempOptions[t]?.let { it + u } ?: u
                            }
                        }
                    }
                    var max = 0
                    var optionName = ""
                    for ((t, u) in tempOptions) {
                        if (u > max) {
                            optionName = t
                            max = u
                        } else if (u == max && optionName != UpgradeItemTask.STR) {
                            //수치가 같을때 옵션 우선순위 적용
                            if (statOrder[t]!! > statOrder[optionName]!!) {
                                optionName = t
                            }
                        }
                    }
                    return Pair(optionName, max)

                }
            }
        }

        /**<스텟><값> 형태의 스트링 반환 (값의 자리수를 4자리로 맞춘다 -> 정렬을 위해서)*/
        fun getGradeKey(): String {
            if (gradeKey.isNullOrEmpty()) {
                gradeKey = "${getGrade().first} ${
                    getGrade().second.toString().let {
                        var temp = ""
                        val space = 4 - (it.length)
                        for (i in 1..space) {
                            temp += " "
                        }
                        temp + it
                    }
                }"
            }
            return gradeKey
        }

        fun getAllInfo(): String {
            return "${getCategory1()}[${getGradeKey()}][$priceText][${getSimpleDate()}]$option"
        }

        fun getCategory1(): String {
            if (job == COMMON)
                return if (category == BELT) "[$category][$name]"
                else "[$category]"
            return "[$job][$category]"
        }

        fun getInfoText(): String {
            return "${getCategory1()}[${getGrade().first} ${getGrade().second}]$option"
        }

        fun getSimpleDate(): String {
            if (dateTextSimple.isNullOrEmpty())
                dateTextSimple = dateText.substring(5).replace('-','.')
            return dateTextSimple
        }

        fun getPriceAndDateAndOption(): String {
            return "[$priceText][${getSimpleDate()}]$option"
        }

        fun toDB(): String {
            return gson.toJson(this)
        }

        companion object {
            private val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            fun fromJson(json: String): ItemInfo {
                return gson.fromJson(json, ItemInfo::class.java)
            }
        }

    }

}