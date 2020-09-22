package maple_tasks

import com.sun.jna.platform.win32.User32
import helper.BaseTaskManager
import helper.ConsumeEvent
import helper.HelperCore
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logI
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import tornadofx.asObservable
import winActive
import winIsForeground
import java.awt.Point
import java.awt.event.KeyEvent
import java.io.File
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger


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
    }


    private var mapleBaseTask: MapleBaseTask? = null
    private var additionalOptionTask: AdditionalOptionTask? = null
    var isItemCheckerEnable = false
    var isSimpleTaskEnable = false  // 간단한 작업 사용 여부
    var checkMarketConditionEnable = false  // F1버튼을 통해 시세파악기능 활성화 여부
    var winTarget = SimpleStringProperty()
    val goodItemList = arrayListOf<String>().asObservable()
    val marketItemList = arrayListOf<String>().asObservable()   //시세 목록

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
            if (activateTargetWindow()) {
                AuctionTask().clickCompletePurchasedTab()
            }


        }

    }

    fun test2() {
        runTask("test2") {
            if (activateTargetWindow()) {
                AuctionTask().exitAuction()
            }
        }

    }

    fun activateTargetWindow(): Boolean {
        return User32.INSTANCE.winActive(winTarget.value)
    }

    override fun resetTask() {
        super.resetTask()
        mapleBaseTask = null
    }

    fun loadAccountList(): List<List<String>> {
        val list = ArrayList<List<String>>()
        val file = File("AccountList.txt")
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
                        list.add(s)
                    }

                }


            }

        }
        return list
    }

    fun login(id: String, pw: String) {
        runTask("login") {
            if (activateTargetWindow())
                LoginTask().login(id, pw)
        }
    }

    fun synthesizeItem() {
        runTask("syn") {
            if (activateTargetWindow())
                MeisterTask().synthesizeItemSmartly()
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

        }
    }

    fun makeItemInfinitely(maxCount: Int) {
        runTask("makeItem") {
            if (activateTargetWindow())
                MeisterTask().makeItemInfinitely(maxCount)
        }
    }

    fun cubeItem(targetOptions: HashMap<String, Int>, count: Int = 1) {
        runTask("cube") {
            if (activateTargetWindow())
                UpgradeItemTask().useCube(targetOptions, count)
        }
    }

    fun upgradeItem() {
        runTask("upgrade") {
            if (activateTargetWindow())
                UpgradeItemTask().upgradeAndStarforce()
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
        }
    }

    fun appraiseItems(untilBlank: Boolean) {
        runTask("appraise") {
            if (activateTargetWindow())
                MapleBaseTask().apply {
                    appraiseItems(untilBlank)
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

    fun checkAdditionalOption(untilBlank: Boolean) {
        runTask("additionalOption") {
            if (activateTargetWindow()) {
                if (additionalOptionTask == null)
                    additionalOptionTask = AdditionalOptionTask()

                val goodItems = additionalOptionTask?.checkItems(untilBlank)
                goodItems?.let {
                    Platform.runLater {
                        goodItemList.clear()
                        goodItemList.addAll(it)
                    }

                }
            }

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


    fun makeMarketConditionInfo(fileName: String, maxCount: Int) {
        runTask("marketCondition") {
            if (activateTargetWindow()) {
                val itemList: List<AdditionalOptionTask.ItemInfo> =
                    if (maxCount == 0)
                        MarketConditionTask().makeInfo(fileName)
                    else
                        MarketConditionTask().makeInfo(fileName, maxCount)

                val itemManager = AdditionalOptionTask.ItemManager()
                itemManager.addAll(itemList)
                Platform.runLater {
                    itemList.forEach {
                        marketItemList.add(it.getAllInfo())
                    }
                    logI("${marketItemList.size}개 찾음")
                }
            }

        }
    }

    fun loadMarketCondition(filename: String) {
        runTask("marketCondition") {
            val itemManager = AdditionalOptionTask.ItemManager()
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
            val itemManager = AdditionalOptionTask.ItemManager()
            itemManager.saveToDB(filename, overwriteDB)
            itemManager.saveToTxt(filename)
            logI("${marketItemList.size}개 저장")

        }
    }

    fun sortMarketCondition() {
        runTask("marketCondition") {
            val itemManager = AdditionalOptionTask.ItemManager()
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
            val itemManager = AdditionalOptionTask.ItemManager()
            itemManager.clear()
            Platform.runLater {
                marketItemList.clear()
            }

        }
    }

    var findMarketConditionTarget : Point? = null   //시세를 알아볼 아이템의 마우스 좌표
    /**마우스 좌표의 아이템의 시세를 파악한 뒤 관련 아이템들 목록을 뷰에 업데이트*/
    private fun findMarketConditionInMouse() {
        runTask("marketCondition") {
            if (activateTargetWindow()) {
                val task = MarketConditionTask()
                val point = task.helper.getMousePos()
                findMarketConditionTarget = point
                task.findMarketCondition(point).let {
                    Platform.runLater {
                        marketItemList.clear()
                        it.forEach {
                            marketItemList.add("${it.name}[${it.getGradeKey()}][${it.priceText}][${it.dateTextSimple}]${it.option}   #${it.price}")
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
            for (i in str.lastIndex downTo str.lastIndex-30) {
                if(str[i] == '#'){
                    findPrice = true
                    break
                } else {
                    price = str[i] + price
                }
            }
            if(!findPrice) {
                return@runTask
            }

            if (activateTargetWindow()) {
                findMarketConditionTarget?.let {
                    AuctionTask().sellItem(it, price)
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