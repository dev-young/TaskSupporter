package maple_tasks

import com.sun.jna.platform.win32.User32
import helper.BaseTaskManager
import helper.ConsumeEvent
import helper.HelperCore
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logI
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import toMat
import tornadofx.asObservable
import winActive
import winIsForeground
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.io.File
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.measureTimeMillis


class MapleTaskManager : BaseTaskManager() {
    var isHotkeyEnable = true
    val keyListener = ConsumeEvent().apply {
        setPressedListener {
            if (!isHotkeyEnable) return@setPressedListener true

            when (it.keyCode) {

                NativeKeyEvent.VC_F2 -> {
                    if (jobMap.isEmpty()) return@setPressedListener true
                    //pause, resume
                    toggle()
                    false
                }

                NativeKeyEvent.VC_F3 -> {
                    if (jobMap.isEmpty() && !isItemCheckerEnable) {
                        return@setPressedListener true
                    }
                    resetTask()
                    false
                }

                NativeKeyEvent.VC_F4 -> {
                    if (jobMap.isEmpty())
                        if(Settings.instance.enableF4OnlyForeground){
                            if (User32.INSTANCE.winIsForeground("GHelper")){
                                finishApp()
                            }
                        } else
                            finishApp()
                    else
                        resetTask()
                    false
                }

                NativeKeyEvent.VC_SPACE -> {
                    if (isItemCheckerEnable) {

                        if (mapleBaseTask == null)
                            mapleBaseTask = MapleBaseTask()

                        if (!User32.INSTANCE.winIsForeground(winTarget.value))
                            return@setPressedListener true

                        GlobalScope.launch {
                            mapleBaseTask?.findNextItem()
                        }
                        false
                    } else
                        true
                }

                NativeKeyEvent.VC_F1 -> {
                    if (checkMarketConditionEnable) {
                        if (!User32.INSTANCE.winIsForeground(winTarget.value)) {
                            logI("타겟 윈도우가 Foreground에 있지 않습니다.")
                            findMarketConditionTarget = HelperCore().getMousePos()
                            return@setPressedListener true
                        }
                        findMarketConditionInMouse()
                        false
                    } else if (isSimpleTaskEnable) {
                        if (!User32.INSTANCE.winIsForeground(winTarget.value)) {
                            logI("타겟 윈도우가 Foreground에 있지 않습니다.")
                            return@setPressedListener true
                        }
                        startSimpleTask(selectedSimpleTask.value)

                        false
                    } else
                        true
                }

                else -> {
                    true
                }
            }
        }
    }

    init {
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.getPackage().name).level = Level.OFF
        GlobalScreen.registerNativeHook()
        GlobalScreen.setEventDispatcher(ConsumeEvent.VoidDispatchService())
        GlobalScreen.addNativeKeyListener(keyListener)
        Settings.load()
    }

    override fun finishApp() {
        GlobalScope.launch(Dispatchers.IO) {
            GlobalScreen.removeNativeKeyListener(keyListener)
            GlobalScreen.unregisterNativeHook()
            println("훅 해제완료!")
            super.finishApp()
        }
    }


    private var mapleBaseTask: MapleBaseTask? = null
    private var additionalOptionTask: AdditionalOptionTask? = null
    var isItemCheckerEnable = false
    var isSimpleTaskEnable = false  // 간단한 작업 사용 여부
    var checkMarketConditionEnable = false  // F1버튼을 통해 시세파악기능 활성화 여부
    var checkUseSmartSearch = true  // F1버튼을 통해 시세파악기능 사용시 최근 목록 가져올지 여부 확인
    var checkAutoCalAndSales = true  // F1버튼을 통해 시세파악기능 사용시 자동으로 최적가 계산하여 등록할것인지 여부
    var winTarget = SimpleStringProperty()
    val goodItemList = arrayListOf<String>().asObservable()
    val marketItemList = arrayListOf<String>().asObservable()   //시세 목록
//    val marketItemList = arrayListOf<ItemInfo>().asObservable()   //시세 목록 // TODO: 이거 테스트 해보자

    val selectedSimpleTask = SimpleStringProperty()

    /**광클같은 간단한 작업 수행 */
    private fun startSimpleTask(simpleTask: String) {
        logI("$simpleTask 수행 시작")
        if (jobMap["simpleTask"] != null) {
            resetTask()
            return
        }
        runTask("simpleTask") {
            if (activateTargetWindow()) {
                if (mapleBaseTask == null)
                    mapleBaseTask = MapleBaseTask()


                mapleBaseTask?.apply {
                    when (simpleTask) {
                        SIMPLE_TASK_AUTOCLICK -> startAutoClick(winTarget.value)
                        SIMPLE_TASK_AUTOSPACE -> startAutoSend(winTarget.value, KeyEvent.VK_SPACE)
                        SIMPLE_TASK_AUTOSPACEANDENTER -> startAutoSpaceAndEnter(winTarget.value)
                        else -> {
                            logI("$simpleTask 알수없는 명령")
                        }
                    }

                }
            }


        }
    }

    fun test() {
        runTask("test") {
            MapleBaseTask().apply {
                val img = helper.createScreenCapture(Rectangle(0, 0, 500, 500))
                val mat = img.toMat()
                mat.clone().apply {
                    Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2GRAY)
                    Imgcodecs.imwrite("test0.png", this)
                }
                mat.clone().apply {
                    println(measureTimeMillis {
                        convertTo(this, -1, 2.0, -100.0)
                    })
                    Imgcodecs.imwrite("test1.png", this)
                }
                mat.clone().apply {
                    println(measureTimeMillis {
                        Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2GRAY)
                    })

                    Imgcodecs.imwrite("test2.png", this)
                }
                mat.clone().apply {
                    println(measureTimeMillis {
                        convertTo(this, -1, 2.0, -100.0)
                        Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2GRAY)
                    })
                    Imgcodecs.imwrite("test3.png", this)
                }
                mat.clone().apply {
                    println(measureTimeMillis {
                        convertTo(this, -1, 2.0, -100.0)
                        Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2GRAY)
                        convertTo(this, -1, 5.0, 0.0)
                    })

                    Imgcodecs.imwrite("test4.png", this)
                }
                mat.clone().apply {
                    println(measureTimeMillis {
                        convertTo(this, -1, 2.0, -100.0)
                        Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2GRAY)
                        convertTo(this, -1, 10.0, 0.0)
                    })

                    Imgcodecs.imwrite("test5.png", this)
                }

            }

        }

    }

    fun test2() {
        runTask("test2") {
            if (activateTargetWindow()) {
                LoginTask().apply {
                    logOut()
                }
            }

        }

    }

    fun activateTargetWindow(): Boolean {
        return User32.INSTANCE.winActive(winTarget.value)
    }

    override fun resetTask() {
        super.resetTask()
        Settings.load() // TODO: 이걸 여기서 사용하는게 뭔가 이상한것같다
        mapleBaseTask = null
    }

    /**@return 아이디,비번,설명,이미지파일 */
    fun loadAccountList(fileName: String = "AccountList"): List<List<String>> {
        val list = ArrayList<List<String>>()
        val file = File("$fileName.txt")
        if (file.exists()) {
            file.readLines().forEach {
//                    log(it)
                if (it.startsWith("//") || it.isEmpty()) {
                    //공백 혹은 주석처리된 line
                } else {
                    val s = it.split(" ", limit = 3)
                    if (s.size < 2) {
                        logI("올바르지 않은 형식입니다. -> $it")
                    } else {
                        if (s[0].contains('@')) {
                            val temp = s[2].split(" ", limit = 2)
                            list.add(arrayOf(s[0], s[1], temp[1], temp[0]).toList())    //id pw description fileName
                        } else
                            list.add(
                                arrayOf(
                                    s[0],
                                    s[1],
                                    s.let { if (it.size == 2) "" else it[2] },
                                    ""
                                ).toList()
                            )    //id pw description fileName
                    }

                }


            }

        }
        return list
    }

    fun login(id: String, pw: String, fileName: String, description: String) {
        runTask("login") {
            if (activateTargetWindow())
                LoginTask().login(id, pw, fileName, description)
        }
    }

    fun synthesizeItem() {
        runTask("syn") {
            if (activateTargetWindow())
                MeisterTask().synthesizeItemUntilBlank()
            Toolkit.getDefaultToolkit().beep()
        }
    }

    fun synthesizeItem(untilBlank: Boolean, maxSynCount: Int, maxTargetItemCount: Int) {
        runTask("syn") {
            if (activateTargetWindow())
                MeisterTask().synthesizeItemSmartly(untilBlank, maxSynCount, maxTargetItemCount, synMouseDelay.value)
            Toolkit.getDefaultToolkit().beep()
        }
    }

    fun buyItem(useItemList: Boolean, buyAll: Boolean, fileName: String = "", usePurchasedTab: Boolean) {
        runTask("buyItem") {
            if (activateTargetWindow()) {
                if (useItemList)
                    AuctionTask().buyItemListUntilEnd(fileName, usePurchasedTab = usePurchasedTab)
                else
                    AuctionTask().buyOneItemUntilEnd(buyAll, usePurchasedTab = usePurchasedTab)
            }
            Toolkit.getDefaultToolkit().beep()
        }
    }

    fun makeItemInfinitely(maxCount: Int) {
        runTask("makeItem") {
            if (activateTargetWindow()) {
                MeisterTask().makeItemInfinitely(maxCount)
                Toolkit.getDefaultToolkit().beep()
            }
        }
    }

    fun cubeItem(targetOptions: HashMap<String, Int>, count: Int = 1) {
        runTask("cube") {
            if (activateTargetWindow()) {
                UpgradeItemTask().useCube(targetOptions, count)
                Toolkit.getDefaultToolkit().beep()
            }
        }
    }

    fun upgradeItem(starforce: Boolean) {
        runTask("upgrade") {
            if (activateTargetWindow()) {
                if (starforce)
                    UpgradeItemTask().upgradeAndStarforce()
                else
                    UpgradeItemTask().upgradeUntilEnd()
                Toolkit.getDefaultToolkit().beep()
            }
        }
    }

    fun makeItem(type: String, name: String, extractIfNormal: Boolean = true) {
        runTask("makeItem") {
            if (activateTargetWindow())
                MeisterTask().apply {
                    val targetPosition = when (type) {
                        MEISTER_1 -> meisterPosition1
                        MEISTER_2 -> meisterPosition2
                        MEISTER_3 -> meisterPosition3
                        else -> null
                    }
                    if (targetPosition == null) {
                        logI("잘못된 타겟입니다.")
                        return@apply
                    }

                    moveCharacter(targetPosition)
                    if (extractIfNormal) {
                        makeItemAndExtractIfNormal(name)
                    } else {
                        makeItem(name)
                    }

                    Toolkit.getDefaultToolkit().beep()
                }
        }
    }

    fun extractItems(untilBlank: Boolean) {
        runTask("extract") {
            if (activateTargetWindow())
                MeisterTask().apply {
                    if (untilBlank) {
                        extractItemUntilBlank()
                    } else
                        extractItemAll()
                }
            Toolkit.getDefaultToolkit().beep()
        }
    }

    fun appraiseItems(untilBlank: Boolean) {
        runTask("appraise") {
            if (activateTargetWindow())
                MapleBaseTask().apply {
                    appraiseItems(untilBlank)
                    Toolkit.getDefaultToolkit().beep()
                }
        }
    }


    /**아이템 구매 및 15분마다 타임리스 제작하기 */
    fun buyItemAndMakeItem(
        fileName: String = "",
        usePurchasedTab: Boolean,
        name: String = "임리스 문라",
        waitingTime: Long = 900000
    ) {
        runTask("BuyAndMake") {
            if (activateTargetWindow()) {
                val auctionTask = AuctionTask()
                auctionTask.buyItemListUntilEnd(fileName, name, waitingTime, usePurchasedTab = usePurchasedTab)
            }

        }
    }

    /**첫번째 빈칸까지 아이템을 버린다. */
    fun dropItem(delay: Int) {
        runTask("dropItem") {
            if (activateTargetWindow()) {
                MapleBaseTask().apply {
                    dropItemUntilBlank(delay)
                }
                Toolkit.getDefaultToolkit().beep()
            }
        }
    }

    fun pressZ(time: Long) {
        runTask("getDropItem") {
            if (activateTargetWindow()) {
                delay(1000)
                MapleBaseTask().apply {
                    this.pressZ(time)
                }
                Toolkit.getDefaultToolkit().beep()
            }
        }
    }

    fun buyItemAndExtract(fileName: String, usePurchasedTab: Boolean) {
        runTask("BuyAndMakeExtract") {
            if (activateTargetWindow()) {
                val auctionTask = AuctionTask()
                auctionTask.buyItemListUntilEnd(fileName, extract = true, usePurchasedTab = usePurchasedTab)
            }

        }
    }

    fun checkAdditionalOption(untilBlank: Boolean, moveToEnd: Boolean) {
        runTask("additionalOption") {
            if (activateTargetWindow()) {
                if (additionalOptionTask == null)
                    additionalOptionTask = AdditionalOptionTask()

                val goodItems = additionalOptionTask?.checkItems(untilBlank, moveToEnd)
                goodItems?.let {
                    Platform.runLater {
                        goodItemList.clear()
                        goodItemList.addAll(it)
                        logI("기준에 충족되는 아이템 수: ${it.size}")
                    }

                }
                Toolkit.getDefaultToolkit().beep()
            }

        }
    }

    fun moveGoodItemsToEnd() {
        runTask("additionalOption") {
            if (activateTargetWindow()) {
                if (additionalOptionTask == null)
                    additionalOptionTask = AdditionalOptionTask()

                additionalOptionTask?.moveItemsToEnd()
            }

            Toolkit.getDefaultToolkit().beep()
        }
    }


    fun moveMouseToGoodItem(key: String) {
        runTask("additionalOption") {
            if (activateTargetWindow()) {
                additionalOptionTask?.let {
                    val index = goodItemList.indexOf(key)
                    val goodItems = additionalOptionTask?.goodItems
                    goodItems?.let {
                        val itemPoint = it[index]
                        additionalOptionTask?.helper?.smartClick(itemPoint, 15, 15, maxTime = 70)
                    }
                }


            }

        }

    }

    fun countEmptyInventory() {
        runTask("simpleTask") {
            if (activateTargetWindow()) {
                if (mapleBaseTask == null)
                    mapleBaseTask = MapleBaseTask()

                mapleBaseTask?.let {
                    it.openInventory()
                    val emptyList = arrayListOf<Point>()
                    val count = it.findItems(false, emptyList).size
                    logI("빈칸: ${emptyList.size}  사용중: $count  합: ${count.plus(emptyList.size)}")
                }
            }

        }

    }


    fun makeMarketConditionInfo(fileName: String, maxCount: Int) {
        runTask("marketCondition") {
            if (activateTargetWindow()) {
                val itemList: List<ItemInfo> =
                    if (maxCount == 0)
                        MarketConditionTask().makeInfo(fileName)
                    else
                        MarketConditionTask().makeInfo(fileName, maxCount)

                val itemManager = ItemManager()
                itemManager.addAll(itemList)
                Platform.runLater {
                    itemList.forEach {
                        marketItemList.add(it.getAllInfo())
                    }
                    logI("${marketItemList.size}개 찾음")
                }

                Toolkit.getDefaultToolkit().beep()
            }

        }
    }

    fun loadMarketCondition(filename: String) {
        runTask("marketCondition") {
            val itemManager = ItemManager()
            val list = itemManager.loadFromTxt(filename)
            Platform.runLater {
                list.forEach {
                    marketItemList.add(it.getAllInfo())
                }
                logI("${list.size}개 불러옴, 총 ${marketItemList.size}개")
            }

        }
    }

    fun saveMarketCondition(filename: String, overwriteDB: Boolean) {
        runTask("marketCondition") {
            val itemManager = ItemManager()
            itemManager.saveToDB(filename, overwriteDB)
            itemManager.saveToTxt(filename)
            logI("${marketItemList.size}개 저장")

        }
    }

    fun sortMarketCondition() {
        runTask("marketCondition") {
            val itemManager = ItemManager()
            val list = itemManager.getSortedList()

            Platform.runLater {
                marketItemList.clear()
                list.forEach {
                    marketItemList.add(it.getAllInfo())
                }
                logI("${marketItemList.size}개 정렬")
            }

        }
    }

    fun clearMarketCondition() {
        runTask("marketCondition") {
            val itemManager = ItemManager()
            itemManager.clear()
            Platform.runLater {
                marketItemList.clear()
            }

        }
    }

    var findMarketConditionTarget: Point? = null   //시세를 알아볼 아이템의 마우스 좌표

    /**마우스 좌표의 아이템의 시세를 파악한 뒤 관련 아이템들 목록을 뷰에 업데이트*/
    private fun findMarketConditionInMouse() {
        runTask("marketCondition") {
            if (activateTargetWindow()) {
                val task = MarketConditionTask()
                val point = task.helper.getMousePos()
                findMarketConditionTarget = point
                task.findMarketCondition(point, checkUseSmartSearch).let {
                    val itemInfo = it.first
                    Platform.runLater {
                        marketItemList.clear()
                        it.second.forEach {
                            val upgraded = if (it.isUpgraded == true) "강화된 " else ""
                            val targetOption = if (it.getGradeKey() == itemInfo!!.getGradeKey()) ">" else ""
                            marketItemList.add("$targetOption$upgraded[${it.getGradeKey()}]  ${it.getSimplePrice()}  [${it.dateTextSimple}]${it.option}   #${it.price}")
                        }

                    }
                    if (checkAutoCalAndSales) {
                        itemInfo?.let {
                            ItemManager().findBestPrice(itemInfo)?.let {
                                AuctionTask().sellItem(point, it.toString(), true)
                            } ?: logI("최적의 가격을 찾을 수 없습니다.")
                        }
                    }
                }


            }

        }
    }

    /**문자열에서 가격을 추출 후 */
    fun sellItem(str: String) {

        runTask("auctionTask") {
            var price = ""
            var findPrice = false
            for (i in str.lastIndex downTo str.lastIndex - 30) {
                if (str[i] == '#') {
                    findPrice = true
                    break
                } else {
                    price = str[i] + price
                }
            }
            if (!findPrice) {
                return@runTask
            }

            if (activateTargetWindow()) {
                findMarketConditionTarget?.let {
                    AuctionTask().sellItem(it, price)
                }
            }

        }


    }

    fun resaleItems(decreasePrice1: Long, pivotPrice: Long, decreasePrice2: Long, cancelFirst: Boolean) {
        runTask("auctionTask") {
            if (activateTargetWindow()) {
                AuctionTask().apply {
                    if (cancelFirst)
                        cancelSellingItem()
                    resaleItem(decreasePrice1, pivotPrice, decreasePrice2)
                }
                Toolkit.getDefaultToolkit().beep()
            }


        }
    }

    /**피로도 혹은 합성할 아이템이 없을때까지 계속 합성
     * 합성 한번 할때마다 추옵 확인 후 가장자리로 옮기는 작업 실시 */
    val synMouseDelay = SimpleIntegerProperty()
    fun synthesizeUtilEnd(maxSynCount: Int = 66, startWithCheck: Boolean = false) {
        runTask("auctionTask") {
            if (activateTargetWindow()) {
                if (additionalOptionTask == null) additionalOptionTask = AdditionalOptionTask()
                val meisterTask = MeisterTask()


                meisterTask.apply {

                    openInventory()
                    sortItems()

                    var targetItemCount = if (startWithCheck) {
                        //startWithCheck == true 인 경우 추옵확인 후 옮긴 뒤에 합성 시작
                        val goodItems = additionalOptionTask!!.checkItems(false, true)
                        Platform.runLater {
                            goodItemList.clear()
                            goodItemList.addAll(goodItems)
                            logI("기준에 충족되는 아이템 수: ${goodItemList.size}")
                        }
                        val itemCount = meisterTask.findItems(false).size
                        logI("현재 전체 아이템 수:$itemCount")
                        itemCount - goodItems.size
                    } else meisterTask.findItems(false).size

                    var remainSynCount = maxSynCount //남은 합성 횟수
                    var completeSynCount = 0

                    while (remainSynCount > 0 && targetItemCount > 1) {
                        //합성
                        logI("$targetItemCount 개의 아이템 합성여부 확인")
                        logI("남은 합성 횟수: $remainSynCount")
                        val synCount =
                            synthesizeItemSmartly(false, remainSynCount, targetItemCount, synMouseDelay.value)
                        remainSynCount -= synCount
                        completeSynCount += synCount
                        targetItemCount -= synCount

                        if (synCount == 0) {
                            //더이상 합성이 불가능할때
                            helper.sendEnter()
                            moveMouseLB()
                            helper.sendEnter()
                            helper.sendEnter()
                            clickCancelBtn()
                            additionalOptionTask!!.checkItems(true, true).let {
                                Platform.runLater {
                                    goodItemList.addAll(it)
                                    logI("기준에 충족되는 아이템 수: ${goodItemList.size}")
                                }
                            }
                            break
                        }

                        while (clickCancelBtn()) {
                            helper.sendEnter()
                            moveMouseLB(100)
                        }


                        //정렬
                        sortItems()

                        //추옵찾고 옮기기
                        val goodItems = additionalOptionTask!!.checkItems(false, true)
                        Platform.runLater {
                            goodItemList.clear()
                            goodItemList.addAll(goodItems)
                            logI("기준에 충족되는 아이템 수: ${goodItemList.size}")
                        }
                        val itemCount = findItems(false).size
                        logI("현재 전체 아이템 수:$itemCount")
                        targetItemCount = itemCount - goodItems.size


                    }

                    logI("작업 완료. $completeSynCount 회 합성 / 추옵 ${goodItemList.size} 개 발견")


                }
            }
        }
    }

    fun synthesizeUtilEndFast(maxSynCount: Int = 66) {
        fun log(message: String) {
            if (Settings.instance.logStepWhenSynthesizeUtilEndFast) {
                logI(message)
            }
        }
        runTask("meisterTask") {
            if (activateTargetWindow()) {
                if (additionalOptionTask == null) additionalOptionTask = AdditionalOptionTask()
                val optionTask = additionalOptionTask!!
                val meisterTask = MeisterTask()
                meisterTask.apply {
                    helper.smartClickWaitingDelay = 100

                    //인벤토리 가져오기 (빈칸 포함)
                    val inventory = getInventory()
                    log("인벤토리 가져오기 완료(${inventory.list.size}칸, 템:${inventory.getItemList().size}개)")

                    optionTask.goodItems.clear()
                    Platform.runLater { goodItemList.clear() }

                    //추옵 싹 확인하기
                    val removeTargets = arrayListOf<Inventory.Item>()
                    var lastItemInfo = ""
                    inventory.list.forEachIndexed { index, item ->
                        if (!item.isEmpty) {
                            optionTask.getOptions(item.point, lastItemInfo)?.let {
                                lastItemInfo = it.getUid()
                                if (optionTask.isOptionGood(it)) {
                                    optionTask.goodItems.add(item.point)
                                    Platform.runLater { goodItemList.add(it.getInfoText()) }
                                    removeTargets.add(item)
                                }
                            }
                        }
                    }

                    //좋은 추옵은 리스트에서 제외시키기
                    removeTargets.forEach {
                        inventory.removeItem(it)
                    }
                    log("${removeTargets.size}개 제거, 남은 인벤 칸:${inventory.list.size}")

                    openSynthesize()

                    moveMouseLB(30)
                    var synCount = 0
                    while (inventory.isNotEmpty() && synCount < maxSynCount) {
                        //리스트가 비었거나 합성횟수가 최대치에 도달했거나 피로도부족등의 이유로 진행이 불가능한경우 break

                        val pair = findSynItemWithFirst(inventory)
                        if (pair == null) {
                            inventory.removeFirstItem()
                            log("합성 가능한 대상이 없어서 첫번째 아이템을 제거합니다.")
                        } else {
                            val r = synthesizeItem(
                                pair.first.point,
                                pair.second.point,
                                Settings.instance.synthesizeMouseDelay
                            )
                            if (r) {
                                synCount++
                                val temp = pair.first.mat!!
                                pair.first.clear()
                                pair.second.clear()

                                val newItem = inventory.getFirstEmpty()!!
                                newItem.setMet(temp)
                                log("합성 성공: ${newItem.point.x},${newItem.point.y}")
                                //합성된 아이템 추옵 체크
                                optionTask.getOptions(newItem.point)?.let {
                                    val infoText = it.getInfoText()
                                    logI("합성결과: $infoText")
                                    if (optionTask.isOptionGood(it)) {
                                        optionTask.goodItems.add(newItem.point)
                                        Platform.runLater { goodItemList.add(infoText) }
                                        inventory.removeItem(newItem)
                                        log("좋은추옵 획득! inventory에서 제거 / 남은 인벤 칸:${inventory.list.size}")
                                    }
                                }

                            } else {
                                logI("합성 실패!")
                                clickOkBtn()//오류창 닫기
                                clickOkBtn()//오류창 닫기
                                break
                            }
                        }
                    }


                    moveMouseLB()
                    clickCancelBtn(true)    //합성창 닫기

                    logI("합성 횟수:$synCount  유효추옵:${goodItemList.size}개")

                    optionTask.goodItems.forEachIndexed { index, point ->
                        val destination = inventory.list.get(inventory.list.lastIndex - index).point
                        helper.apply {
                            smartClick(point, 10, 10, maxTime = 100)
                            delayRandom(50, 100)
                            smartClick(destination, 10, 10, maxTime = 100)
                            delayRandom(500, 540)
                        }
                    }
                    log("유효 추옵 옮기는 작업 완료")
                }

            }
        }
    }

    /**여러 계정으로 자동으로 로그인하며 물품 재등록 및 숙련도아이템 제작*/
    fun autoMakeAndResaleWithMultipleAccount(decreasePrice1: Long, pivotPrice: Long, decreasePrice2: Long) {
        runTask("loginTask") {
            if (activateTargetWindow()) {
                val loginTask = LoginTask()
                val meisterTask = MeisterTask()
                val auctionTask = AuctionTask()

                val accountList = loadAccountList("숙련도올릴계정")
                accountList.forEach { logI("${it[0]}${it[3]}  ${it[2]}번째") }
                run {
                    accountList.forEach {
                        val id = it[0]
                        val pw = it[1]
                        val temp = it[2].split(',', limit = 3)
                        val characterIndex = temp[0].toInt()
                        val type = temp[1]
                        val itemName = temp[2]
                        val fileName = it[3]

                        loginTask.login(id, pw, fileName, fileName)

                        if (loginTask.waitLoadingChannel()) {
                            loginTask.intoChannel(7)
                            logI("서버 선택")

                            loginTask.waitLoadingCharacter()
                            loginTask.selectCharacter(characterIndex)
                            logI("$characterIndex 번째 캐릭 선택")

                            if (loginTask.waitLoadingGame()) {
                                logI("로딩 완료")
                                delay(1000)
                                loginTask.clearAd()

                                //마이스터빌 이동
                                meisterTask.moveMeisterVill()
                                loginTask.waitLoadingGame()

                                //아이템 재등록
                                auctionTask.openAuction()
                                auctionTask.cancelSellingItem()
                                auctionTask.resaleItem(decreasePrice1, pivotPrice, decreasePrice2)
                                auctionTask.exitAuction()
                                logI("옥션 종료")
                                loginTask.waitLoadingGame()
                                logI("게임 로딩 완료")

                                meisterTask.apply {
                                    val targetPosition = when (type) {
                                        MEISTER_1 -> meisterPosition1
                                        MEISTER_2 -> meisterPosition2
                                        MEISTER_3 -> meisterPosition3
                                        else -> null
                                    }
                                    if (targetPosition == null) {
                                        logI("잘못된 타겟입니다.")
                                        return@apply
                                    }
                                    logI("이동 시작")
                                    moveCharacter(targetPosition)
                                    if (makeItem(itemName, 15)) {
                                        logI("제작 성공")
                                    } else {
                                        logI("제작 실패")
                                    }
                                }

                                if (it.toString() != accountList.last().toString())
                                    loginTask.logOut()

                            } else {
                                //거탐뜬 경우
                                logI("게임 로딩 실패")
                                return@run
                            }

                        } else {
                            // 월드선택창 접속 못하는 경우 ( 로그인 실패한경우 )
                            logI("로그인 실패: $id")
                            loginTask.helper.send(KeyEvent.VK_ESCAPE)
                            loginTask.helper.send(KeyEvent.VK_ESCAPE)
                            loginTask.waitLoadingLogin()

                        }


                    }
                }



                Toolkit.getDefaultToolkit().beep()
            }


        }
    }

    fun upgradeItem(fileName: String) {
        runTask("upgrade") {
            if (activateTargetWindow()) {
                UpgradeItemTask().apply {
                    runUpgradeTask(fileName)
                }
            }
        }
    }

    companion object {
        const val SIMPLE_TASK_AUTOCLICK = "마우스 광클"
        const val SIMPLE_TASK_AUTOSPACE = "스페이스바 광클"
        const val SIMPLE_TASK_AUTOSPACEANDENTER = "스페이스바, 엔터 광클"

        const val MEISTER_1 = "장비제작"
        const val MEISTER_2 = "장신구제작"
        const val MEISTER_3 = "연금술"
    }

}