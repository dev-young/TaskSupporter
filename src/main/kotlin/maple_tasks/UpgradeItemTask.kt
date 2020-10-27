package maple_tasks

import get
import logI
import moveMouseSmoothly
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import toMat
import java.awt.Point
import java.awt.Rectangle
import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

class UpgradeItemTask : MapleBaseTask() {

    companion object {
        var cubeDelayMin = 1500 // 최소 1500 이상
        var cubeDelayMax = 1600

        const val STR = "STR"
        const val DEX = "DEX"
        const val LUK = "LUK"
        const val INT = "INT"
        const val ALL = "ALL"
        const val HP = "HP"
        const val ATT = "attack"
        const val SPELL = "spell"
        const val DMG = "damage"
        const val CRITICAL = "critical"
    }

    private var resultWindowLeftTop: Point? = null
    private var oneMoreCubeLeftTop: Point? = null
    private var finishCubeLeftTop: Point? = null

    private val optionNameTemplates = hashMapOf<String, Mat>()
    private val optionValueTemplates = hashMapOf<Int, Mat>()


    init {
        initCubeOptionTemplates()
    }

    suspend fun upgradeUntilEnd() {
        helper.apply {

            moveMouseLB(30)
            while (true) {

                //스타포스 강화인경우 나가기
                imageSearch("img\\upgrade\\starforce.png", 80.0)?.let { return }


                //확인 클릭
                imageSearchAndClick("img\\upgrade\\ok.png", 80.0, maxTime = 55)?.let {
                    simpleClick()
                    simpleClick()
                    moveMouseLB(20)
                }


                //강화 클릭
                imageSearch("img\\upgrade\\upgrade.png", 80.0)?.let {
                    imageSearch("img\\upgrade\\starforce.png", 80.0)?.let { return }
                    smartClick(it, 2, 2, maxTime = 55)
                    simpleClick()
                    moveMouseLB(20)
                }

                //끝인지 확인
                if (imageSearch("img\\upgrade\\end.png", 80.0) != null)
                    break

                delayRandom(10, 30)
            }
        }
    }

    suspend fun upgradeAndStarforce(starforceCount: Int = 0) {
        helper.apply {

            moveMouseLB(30)
            while (true) {

                //스타캐치 해체 클릭
                imageSearch("img\\upgrade\\starCatch.png", 80.0)?.let {
                    smartClick(it, 2, 2, maxTime = 55)
                    moveMouseLB(20)
                }


                //확인 클릭
                imageSearchAndClick("img\\upgrade\\ok.png", 80.0, maxTime = 55)?.let {
                    simpleClick()
                    simpleClick()
                    moveMouseLB(20)
                }


                //강화 클릭
                imageSearch("img\\upgrade\\upgrade.png", 80.0)?.let {
                    if (imageSearch("img\\upgrade\\starCatch.png", 80.0) == null) {

                        // 현재 스타포스가 10 이상인지 확인
                        if (imageSearch("img\\upgrade\\10star.png", 80.0) != null) {
                            return
                        }

                        smartClick(it, 2, 2, maxTime = 55)
                        simpleClick()
                        moveMouseLB(20)
                    }
                }

                //끝인지 확인
                if (imageSearch("img\\upgrade\\end.png", 80.0) != null)
                    break

                delayRandom(10, 30)
            }
        }

    }

    /**인벤토리를 열어서 강화창을 열었을때 강화창의 오른쪽에 인벤토리가 있어야한다.*/
    suspend fun upgradeItem(itemPoint: Point, scrollNumber: Int, starforce:Boolean): Boolean {

        openUpgradeWindow()

        openInventory()

        helper.apply {
            smartClickTimeMin = 50
            smartClickTimeMax = 100

            for (i in 1..50){
                imageSearch("img\\upgrade\\end.png")?.let {
                    smartClick(itemPoint,5,5)
                    delayRandom(50, 100)
                    smartClick(it, 2,2)
                } ?: break
            }

            moveMouseLB()

            //스타포스 강화해야하는 아이템인 경우 true 반환
            imageSearch("img\\upgrade\\starforce.png")?.let {
                logI("주문서 강화 불가능")
                if(starforce) upgradeAndStarforce()

                return true
            }

            //주문서 선택
            if(selectUpgradeScroll(scrollNumber)) {
                moveMouseLB()

                //강화 시작
                if(starforce)
                    upgradeAndStarforce()
                else
                    upgradeUntilEnd()

                moveMouseLB(300)
            }


        }
        return true
    }

    /**강화창이 열려있을때만 사용, 주문서를 찾고 선택한다.*/
    private suspend fun selectUpgradeScroll(scrollNumber: Int): Boolean {
        helper.apply {
            logI("$scrollNumber 번 스크롤 선택 시작")
            imageSearch("img\\upgrade\\upgradeScroll.png")?.let {
                val diff = 30   //주문서 버튼 간격
                val first = Point(it.x+250, it.y+70)
                val second = Point(it.x+250, it.y+70+diff)
                val third = Point(it.x+250, it.y+70+diff+diff)

                val scrollTime = scrollNumber - 3
                if(scrollTime > 0){
                    moveMouseSmoothly(first, 300)
                    delayRandom(100, 200)
                    mouseWheelSmoothly(scrollTime, 100)
                    delayRandom(150, 200)
                    smartClick(third, 3,3)
                } else {
                    when (scrollNumber) {
                        1 -> smartClick(first, 3,3)
                        2 -> smartClick(second, 3,3)
                        3 -> smartClick(third, 3,3)
                    }
                }

                for (i in 1..50) {
                    if(imageSearch("img\\upgrade\\upgrade.png") != null) return true
                    simpleClick()
                    delayRandom(200, 300)
                }

                return false
            }
        }

        return false
    }

    private suspend fun openUpgradeWindow() {

        helper.apply {
            moveMouseLB(20)

            //인벤토라 닫고
            closeInventory()
            moveMouseLB(20)

            //혹시 강화창에 장비 올라가있을지도 모르니 취소버튼 클릭
            imageSearchAndClick("img\\upgrade\\cancel.png", 80.0, maxTime = 55)?.let {
                simpleClick()
                simpleClick()
                moveMouseLB(20)
            }

            while (imageSearch("img\\upgrade\\end.png") == null) {
                //강화창여는 버튼 클릭
                openInventory()

                moveMouseLB()
                imageSearchAndClick("img\\upgrade\\startUpgrade1.png")
                    ?: imageSearchAndClick("img\\upgrade\\startUpgrade2.png")

                moveMouseLB()
            }


        }
    }

    /**파일에 적힌 주문서 순서를 바탕으로 아이템들을 찾은뒤 해당 아이템들을 강화한다. */
    suspend fun runUpgradeTask(fileName:String){
        val scrollList = loadUpgradeFile(fileName)
        logI("순서: $scrollList")
        val itemList = findItems(untilBlank = false)


        scrollList.forEachIndexed {index, pair ->
            if(index < itemList.size){
                val item = itemList[index]
                upgradeItem(item, pair.first, pair.second)

            }
        }
    }

    private fun loadUpgradeFile(fileName: String): ArrayList<Pair<Int, Boolean>> {
        val list = ArrayList<Pair<Int, Boolean>>()
        val file = File("$fileName")
        if (file.exists()) {
            file.readLines().forEach {
                if (it.startsWith("//") || it.isEmpty()) {
                    //공백 혹은 주석처리된 line
                } else {
                    var starforce = false
                    if(it.contains('*')) {
                        starforce = true
                    }
                    val num = it.replace("*", "").toIntOrNull()?:0
                    if (num > 0)
                        list.add(Pair(num, starforce))
                }
            }

        }
        return list
    }


    fun initCubeOptionTemplates() {
        if (optionNameTemplates.isEmpty()) {
            optionNameTemplates[STR] = Imgcodecs.imread("img\\cube\\str.png")
            optionNameTemplates[DEX] = Imgcodecs.imread("img\\cube\\dex.png")
            optionNameTemplates[LUK] = Imgcodecs.imread("img\\cube\\luc.png")
            optionNameTemplates[INT] = Imgcodecs.imread("img\\cube\\int.png")
            optionNameTemplates[ALL] = Imgcodecs.imread("img\\cube\\all.png")
            optionNameTemplates[HP] = Imgcodecs.imread("img\\cube\\hp.png")
            optionNameTemplates[ATT] = Imgcodecs.imread("img\\cube\\att.png")
            optionNameTemplates[SPELL] = Imgcodecs.imread("img\\cube\\spell.png")
            optionNameTemplates[DMG] = Imgcodecs.imread("img\\cube\\dmg.png")
            optionNameTemplates[CRITICAL] = Imgcodecs.imread("img\\cube\\critical.png")
            optionValueTemplates[8] = Imgcodecs.imread("img\\cube\\8per.png")
            optionValueTemplates[6] = Imgcodecs.imread("img\\cube\\6per.png")
            optionValueTemplates[4] = Imgcodecs.imread("img\\cube\\4per.png")
            optionValueTemplates[3] = Imgcodecs.imread("img\\cube\\3per.png")
            optionValueTemplates[2] = Imgcodecs.imread("img\\cube\\2per.png")

            val reoveTargetList = arrayListOf<Any>()
            optionValueTemplates.forEach { t, u ->
                // 없는 파일 템플릿에서 제거
                if (!u.isContinuous)
                    reoveTargetList.add(t)
            }
            reoveTargetList.forEach {
                optionValueTemplates.remove(it)
            }

            reoveTargetList.clear()
            optionNameTemplates.forEach { t, u ->
                // 없는 파일 템플릿에서 제거
                if (!u.isContinuous)
                    reoveTargetList.add(t)
            }
            reoveTargetList.forEach {
                optionNameTemplates.remove(it)
            }

            optionValueTemplates.forEach { (t, _) -> println(t) }
            optionValueTemplates.forEach { (t, _) -> println(t) }

        }
    }

    suspend fun runCubeTask(targetOptionsList: List<Map<String, Int>>) {
        logI("큐브 작업 시작")
        targetOptionsList.forEach { logI(it.toString()) }

        helper.apply {
            var isInventoryExpanded = isInventoryExpanded()
            var vx: Int //현재 아이템 x
            var vy: Int  //현재 아이템 y
            val point: Point = findFirstItemInInventory() ?: return soundBeep()
            point.let {
                vx = it.x + 2
                vy = it.y + 2
                logI("첫째칸 좌상단 위치: $vx, $vy")
//                moveMouseSmoothly(Point(vx, vy), 50)
            }

            var usedCubeCounter = 0

            for (i in 1..targetOptionsList.size) {
                if (isEmptyOrDisable(Point(vx, vy))) {
                    logI("큐브 ${usedCubeCounter}개 사용 (${i - 1}회 완료)")
                    soundBeep()
                    return
                }

                startCube(Point(vx, vy))
                usedCubeCounter++
                delayRandom(100, 200)

                val targetOptions = targetOptionsList[i - 1]
                while (!checkOption(targetOptions) && !checkCubeDisable()) {
                    val delay = random.get(cubeDelayMin, cubeDelayMax) - 1500L
                    if (delay > 0)
                        kotlinx.coroutines.delay(delay)
                    if (oneMoreCube())
                        usedCubeCounter++
                    kotlinx.coroutines.delay(1500)
                }

                clickFinishCube()

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


            logI("큐브 ${usedCubeCounter}개 사용 (${targetOptionsList.size}회 완료)")
            soundBeep()
        }
    }

    suspend fun useCube(itemCount: Int) {
        val targetOptionsList = arrayListOf<Map<String, Int>>()

        for (i in 1..itemCount) {
            val targetOptions = hashMapOf<String, Int>()
            targetOptions[STR] = 9
            targetOptions[DEX] = 9
            targetOptions[LUK] = 9
            targetOptions[INT] = 9
            targetOptions[HP] = 12
            targetOptions[ATT] = 9
            targetOptions[SPELL] = 9

            targetOptionsList.add(targetOptions)
        }

        runCubeTask(targetOptionsList)
    }

    suspend fun useCube(targetOptions: HashMap<String, Int>, itemCount: Int) {
        val targetOptionsList = arrayListOf<Map<String, Int>>()

        val removeTarget = arrayListOf<String>()
        targetOptions.forEach { (t, u) ->
            if (u == 0)
                removeTarget.add(t)
        }
        removeTarget.forEach {
            targetOptions.remove(it)
        }

        val count = if (itemCount == 0) 128 else itemCount
        for (i in 1..count) {
            targetOptionsList.add(targetOptions)
        }

        runCubeTask(targetOptionsList)
    }

    private fun checkCubeDisable(): Boolean {
        helper.apply {
            resultWindowLeftTop?.let {
                val disable = imageSearch(
                    Point(it.x + 110, it.y + 95),
                    70,
                    30,
                    "img\\cube\\cubeDisable.png"
                )
                if (disable != null) {
//                    logI("checkCubeDisable true")
                    return true
                }


            }
        }
//        logI("checkCubeDisable false")
        return false
    }

    private suspend fun clickFinishCube() {
        helper.smartClick(finishCubeLeftTop!!, 20, 5)
        helper.simpleClick()
    }

    /**큐브 결과를 확인하여 원하는 옵션이 나왔나 확인한다. */
    fun checkOption(options: Map<String, Int>): Boolean {
        if (resultWindowLeftTop == null) return false
        val startPoint = resultWindowLeftTop!!
        val resultOption = hashMapOf<String, Int>()
        helper.apply {
            val option1LT = Point(startPoint.x + 2, startPoint.y + 28)    //첫줄 좌상단
            val option2LT = Point(startPoint.x + 2, startPoint.y + 40)    //둘째줄 좌상단
            val option3LT = Point(startPoint.x + 2, startPoint.y + 50)    //셋째줄 좌상단

            val sourceList = listOf(
                createScreenCapture(Rectangle(option1LT.x, option1LT.y, 150, 18)).toMat(),
                createScreenCapture(Rectangle(option2LT.x, option2LT.y, 150, 18)).toMat(),
                createScreenCapture(Rectangle(option3LT.x, option3LT.y, 150, 20)).toMat()
            )

            var normalAttack = 0
            var normalSpell = 0

            sourceList.forEachIndexed { _, source ->
                kotlin.run {
                    optionNameTemplates.forEach { (name, template) ->
                        if (imageSearchReturnBoolean(source, template)) {
                            kotlin.run {
                                optionValueTemplates.forEach { (value, template) ->
                                    if (imageSearchReturnBoolean(source, template)) {
                                        val v = if (name == CRITICAL) {
                                            value   //레어,에픽인 경우 크리티컬 수치는 렙제와 상관없이 일정하다.
                                        } else {
                                            when (value) {
                                                2 -> 3
                                                4 -> 6
                                                else -> value
                                            }
                                        }
                                        resultOption[name] = resultOption[name]?.plus(v) ?: v


//                                logI("${index+1}줄 값 추가! $name = ${resultOption[name]}  (+$value)")
                                        return@run
                                    }
                                }
                                //옵션이 공% 가 아니라 그냥 공격력+ 인 경우
                                if (name == ATT)
                                    normalAttack++

                                //옵션이 마% 가 아니라 그냥 마력+ 인 경우
                                else if (name == SPELL)
                                    normalSpell++

                            }
                            return@run
                        }
                    }
                }

            }

            resultOption[ALL]?.let {
                resultOption[STR] = resultOption[STR]?.plus(it) ?: it
                resultOption[DEX] = resultOption[DEX]?.plus(it) ?: it
                resultOption[LUK] = resultOption[LUK]?.plus(it) ?: it
                resultOption[INT] = resultOption[INT]?.plus(it) ?: it
            }

            /**DMG 값은 1감소시킨 뒤 공마에 추가한다. */
            resultOption[DMG]?.let {
                val v = it - 1
                resultOption[ATT] = resultOption[ATT]?.plus(v) ?: v
                resultOption[SPELL] = resultOption[SPELL]?.plus(v) ?: v
            }

            /**일반 공,마력은 수치에 상관없이 해당 %값을 1만큼 증가시킨다.*/
            if (normalAttack > 0)
                resultOption[ATT] = resultOption[ATT]?.plus(normalAttack) ?: normalAttack
            if (normalSpell > 0)
                resultOption[SPELL] = resultOption[SPELL]?.plus(normalSpell) ?: normalSpell

            var result = false
            resultOption.forEach { t, u ->
                options[t]?.let {
                    if (it <= u) {
                        result = true
                    }
                }
            }
            if (resultOption.isNotEmpty())
                logI("옵션: $resultOption  결과:$result")

            return result
        }
    }

    /**큐브 한번 더 사용하기 */
    private suspend fun oneMoreCube(): Boolean {
        if (oneMoreCubeLeftTop == null) {
            logI("큐브 창을 못찾았습니다.")
            return false
        }

        helper.apply {
            smartClick(oneMoreCubeLeftTop!!, 50, 5)
            simpleClick()

            sendEnter()
            delayRandom(10, 30)
            sendEnter()
            delayRandom(10, 30)
            sendEnter()
            delayRandom(10, 30)
            sendEnter()
            delayRandom(10, 30)
        }
        return true
    }


    /**소비창에서 수상한 큐브 사용하여 point에 있는 장비에 사용
     * @param itemPoint 아이템의 좌상단 좌표]*/
    suspend fun startCube(itemPoint: Point): Boolean {

        helper.apply {
            //소비창 클릭
            val point = clickConsumeTab()
            delayRandom(200, 300)
//        helper.moveMouseSmoothly(point)
            //큐브 더블클릭
            val cubePoint = imageSearch("img\\cube\\cube.png")
            if (cubePoint == null) {
                logI("큐브를 찾을 수 없습니다.")
                return false
            }
            moveMouseSmoothly(cubePoint, random.get(50, 100))
            delayRandom(20, 40)
            simpleClick(cubePoint, 2)
            delayRandom(100, 200)
            smartClick(itemPoint, 15, 15)

            sendEnter()
            delayRandom(10, 30)
            sendEnter()
            delayRandom(10, 30)
            sendEnter()
            delayRandom(10, 30)
            sendEnter()
            delayRandom(10, 30)

            kotlinx.coroutines.delay(1500)

            println("소요시간" + measureTimeMillis {
                resultWindowLeftTop = findResultWindow()
                resultWindowLeftTop?.let {
                    logI("결과창 좌표: $it")
                    oneMoreCubeLeftTop = Point(it.x + 20, it.y + 105)
                    finishCubeLeftTop = Point(it.x + 138, it.y + 105)
                }
            })


        }

        return true
    }

    fun findResultWindow(): Point? {
        helper.apply {
            return imageSearch("img\\cube\\result.png")
        }
    }

}