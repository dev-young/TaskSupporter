import helper.BaseTaskManager.Companion.STATE_IDEL
import helper.BaseTaskManager.Companion.STATE_PAUSED
import helper.BaseTaskManager.Companion.STATE_WORKING
import javafx.application.Platform
import javafx.beans.property.*
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import maple_tasks.MapleTaskManager
import maple_tasks.MapleTaskManager.Companion.MEISTER_1
import maple_tasks.MapleTaskManager.Companion.MEISTER_2
import maple_tasks.MapleTaskManager.Companion.MEISTER_3
import maple_tasks.UpgradeItemTask
import maple_tasks.UpgradeItemTask.Companion.DEX
import maple_tasks.UpgradeItemTask.Companion.HP
import maple_tasks.UpgradeItemTask.Companion.INT
import maple_tasks.UpgradeItemTask.Companion.LUK
import maple_tasks.UpgradeItemTask.Companion.STR
import tornadofx.*
import java.lang.Exception

class MainView : View() {
    companion object {
        val logList = arrayListOf<String>().asObservable()
        val lastLogLabel = Label()
    }

    private val defaultItemSpacing = 8.0
    private lateinit var infoLabel: Label
    val taskManager = MapleTaskManager()

    override val root = borderpane {
        top = hbox {

            spacing = defaultItemSpacing
            checkbox {
                paddingHorizontal = defaultItemSpacing
                paddingVertical = 5
                text = "항상 위에 표시"
                isSelected = true
                action { primaryStage.isAlwaysOnTop = isSelected }
            }

            togglebutton {
                isSelected = true
                text = "단축키 활성화"
                action {
                    text = if (isSelected) "단축키 활성화" else "단축키 비활성"
                    taskManager.isHotkeyEnable = isSelected
                }
            }

            button {
                text = "일시정지"
                action {
                    taskManager.toggle()
                }
            }

            button {
                text = "초기화"
                action {
                    taskManager.resetTask()
                }
            }
        }

        center = tabpane {
            selectionModel.selectedItemProperty().addListener { observable, oldTab, newTab ->
//                logI("선택된 탭: ${newTab.text}")
            }

            tab("로그인") {
                isClosable = false
                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing
                    val accountList = taskManager.loadAccountList()
                    accountList.forEach {
                        hbox {
                            alignment = Pos.CENTER_LEFT
                            spacing = defaultItemSpacing
                            val id = it[0]
                            val pw = it[1]
                            val description = if (it.size == 3) it[2] else ""
                            button(id).setOnAction {
                                taskManager.login(id, pw)
                            }
                            label(description)
                        }
                    }
                }
            }

            tab("전문기술") {
                isClosable = false


                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = 10.0

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = 4.0

                        button("자동합성") {
                            action {
                                runAsync {
                                    taskManager.synthesizeItem()
                                }
                            }
                        }
                        button("자동감정") {
                            action {
                                taskManager.appraiseItems(true)
                            }
                        }
                    }

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = 4.0

                        button("자동분해") {
                            action {
                                taskManager.extractItems(true)
                            }
                        }
                        button("모두분해") {
                            action {
                                taskManager.extractItems(false)
                            }
                        }
                    }

                    vbox {
                        spacing = 4.0

                        val input = SimpleStringProperty()
                        hbox {
                            alignment = Pos.CENTER_LEFT
                            spacing = defaultItemSpacing
                            button("반복제작") {
                                action {
                                    taskManager.makeItemInfinitely(input.value.toInt())
                                }
                            }

                            textfield(input) {
                                text = "0"
                                maxWidth = 50.0
                            }

                            label("0=무한반복")
                        }

                        checkbox {
                            text = "스페이스바로 아이템 확인하기 (F3:위치 초기화)"
                            action {
                                taskManager.isItemCheckerEnable = isSelected
                            }
                        }

                    }

                    vbox {
                        spacing = 4.0
                        val name1 = SimpleStringProperty()
                        val name2 = SimpleStringProperty()
                        val name3 = SimpleStringProperty()
                        val extract1 = SimpleBooleanProperty()
                        val extract2 = SimpleBooleanProperty()
                        hbox {
                            alignment = Pos.CENTER_LEFT
                            spacing = 4.0
                            button(MEISTER_1) {
                                action {
                                    taskManager.makeItem(MEISTER_1, name1.value, extract1.value)
                                }
                                minWidth = 80.0
                            }

                            textfield(name1) {
                                text = "임리스 문라"
                                maxWidth = 100.0
                            }

                            checkbox("일반템인경우 분해하기", extract1) {
                                isSelected = true
                            }
                        }

                        hbox {
                            alignment = Pos.CENTER_LEFT
                            spacing = 4.0
                            button(MEISTER_2) {
                                action {
                                    taskManager.makeItem(MEISTER_2, name2.value, extract2.value)
                                }
                                minWidth = 80.0
                            }

                            textfield(name2) {
                                text = "리스 이어링"
                                maxWidth = 100.0
                            }

                            checkbox("일반템인경우 분해하기", extract2) {
                                isSelected = false
                            }
                        }

                        hbox {
                            alignment = Pos.CENTER_LEFT
                            spacing = 4.0
                            button(MEISTER_3) {
                                action {
                                    taskManager.makeItem(MEISTER_3, name3.value)
                                }
                                minWidth = 80.0
                            }

                            textfield(name3) {
                                text = ""
                                maxWidth = 100.0
                            }
                        }
                    }
                }
            }

            tab("추가옵션") {
                isClosable = false
                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = defaultItemSpacing

                        val untilBlank = SimpleBooleanProperty()
                        button ("추옵 확인") {
                            action {
                                taskManager.checkAdditionalOption(untilBlank.value)
                            }
                        }

                        checkbox("빈칸까지 수행", untilBlank) {
                            isSelected = false
                        }

                    }

                    listview(taskManager.goodItemList) {
                        useMaxWidth = true
                        prefHeight = 180.0
                        selectionModel.selectionMode = SelectionMode.SINGLE
                        onUserSelect {
                            taskManager.moveMouseToGoodItem(it)
                        }
                    }


                }
            }

            tab("경매장") {
                isClosable = false
                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing

                    val fileName = SimpleStringProperty()
                    var buyAll: CheckBox? = null
                    val useItemList = SimpleBooleanProperty()
                    val useCompletedPurchaseTab = SimpleBooleanProperty()
                    val waitingTime = SimpleLongProperty()
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = defaultItemSpacing
                        checkbox("파일로부터 목록 불러오기", useItemList) {
                            isSelected = true
                            action {
                                buyAll?.let {
                                    it.isDisable = isSelected
                                }
                            }
                        }

                        textfield(fileName) {
                            text = "ItemList"
                        }
                    }

                    buyAll = checkbox {
                        isDisable = true
                        text = "구매수량 최대치 입력"
                    }

                    checkbox("구매 완료탭 클릭하여 수령하기", useCompletedPurchaseTab) {
                        isSelected = true
                    }

                    button("아이템 검색 및 구매") {
                        action {
                            taskManager.buyItem(
                                useItemList.value,
                                buyAll.isSelected,
                                fileName.value,
                                useCompletedPurchaseTab.value
                            )
                        }
                    }

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = 4.0
                        button("아이템 구매 및 장비제작") {
                            action {
                                taskManager.buyItemAndMakeItem(
                                    fileName.value,
                                    useCompletedPurchaseTab.value,
                                    waitingTime = (waitingTime.value * 1000)
                                )
                            }
                        }

                        textfield(waitingTime) {
                            text = "899"
                        }
                        label("초마다 제작")
                    }

                    button("아이템 구매 및 분해") {
                        action {
                            taskManager.buyItemAndExtract(fileName.value, useCompletedPurchaseTab.value)
                        }
                    }
                }
            }

            tab("시세") {
                isClosable = false
                val fileName = SimpleStringProperty()
                val resultFileName = SimpleStringProperty()
                val maxCount = SimpleStringProperty()
                val overwriteDB = SimpleBooleanProperty()
                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = defaultItemSpacing

                        textfield(fileName) {
                            text = "시세조사대상"
                            maxWidth = 90.0
                        }

                        button ("시세정보 만들기") {
                            action {
                                taskManager.makeMarketConditionInfo(fileName.value, maxCount.value.toInt())
                            }
                        }

                        label("조사대상1개당 검색할 수:")
                        textfield(maxCount) {
                            text = "0"
                            maxWidth = 50.0
                        }
                        label("0:최대")

                    }

                    listview(taskManager.marketItemList) {
                        useMaxWidth = true
                        prefHeight = 150.0
                        selectionModel.selectionMode = SelectionMode.SINGLE
                        onUserSelect {
                            taskManager.sellItem(it)
                        }
                    }

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = defaultItemSpacing

                        textfield(resultFileName) {
                            text = "추옵시세표"
                            maxWidth = 90.0
                        }

                        button ("불러오기") {
                            action {
                                taskManager.loadMarketCondition(resultFileName.value)
                            }
                        }

                        button ("저장") {
                            action {
                                taskManager.saveMarketCondition(resultFileName.value, overwriteDB.value)
                            }
                        }

                        button ("정렬") {
                            action {
                                taskManager.sortMarketCondition()
                            }
                        }

                        button ("초기화") {
                            action {
                                taskManager.clearMarketCondition()
                            }
                        }

                        checkbox("DB덮어씌우기", overwriteDB) {
                            isSelected = false
                        }

                    }

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = 4.5

                        checkbox("F1버튼으로 해당 아이템 시세 확인하기") {
                            isSelected = false
                            action {
                                taskManager.checkMarketConditionEnable = isSelected
                            }
                        }

                        val price = SimpleStringProperty()

                        spacer {maxWidth = 15.0}

                        textfield(price) {
                            maxWidth = 90.0
                        }
                        button ("등록") {
                            action {
                                taskManager.sellItem("  #${price.value}0000")
                            }
                        }
                        button {
                            action {
                                price.set("")
                            }
                        }

                    }

                }
            }

            tab("큐브") {
                isClosable = false

                val str = SimpleIntegerProperty()
                val dex = SimpleIntegerProperty()
                val int = SimpleIntegerProperty()
                val luc = SimpleIntegerProperty()
                val hp = SimpleIntegerProperty()

                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = 5.0

                    val labelWidth = 40.0
                    val inputWidth = 45.0

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        label(STR) {
                            minWidth = labelWidth
                        }
                        textfield(str) {
                            text = "9"
                            maxWidth = inputWidth
                        }
                    }
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        label(DEX) {
                            minWidth = labelWidth
                        }
                        textfield(dex) {
                            text = "9"
                            maxWidth = inputWidth
                        }
                    }
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        label(INT) {
                            minWidth = labelWidth
                        }
                        textfield(int) {
                            text = "9"
                            maxWidth = inputWidth
                        }
                    }
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        label(LUK) {
                            minWidth = labelWidth
                        }
                        textfield(luc) {
                            text = "9"
                            maxWidth = inputWidth
                        }
                    }
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        label(HP) {
                            minWidth = labelWidth
                        }
                        textfield(hp) {
                            text = "12"
                            maxWidth = inputWidth
                        }
                    }

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = 5.0
                        button("초기화") {
                            action {
                                str.value = 9
                                dex.value = 9
                                int.value = 9
                                luc.value = 9
                                hp.value = 12
                            }
                        }
                        button("clear") {
                            action {
                                str.value = 0
                                dex.value = 0
                                int.value = 0
                                luc.value = 0
                                hp.value = 0
                            }
                        }
                        label("0: 해당 옵션 확인 안함")
                    }


                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = 5.0
                        val cubeCount = SimpleStringProperty()
                        button("큐브 작업 수행하기") {
                            action {
                                val targetOptions = hashMapOf<String, Int>()
                                targetOptions[STR] = str.let { it.value }
                                targetOptions[DEX] = dex.let { it.value }
                                targetOptions[LUK] = luc.let { it.value }
                                targetOptions[INT] = int.let { it.value }
                                targetOptions[HP] = hp.let { it.value }
//                                targetOptions[UpgradeItemTask.ATT] = 9
//                                targetOptions[UpgradeItemTask.SPELL] = 9
                                taskManager.cubeItem(targetOptions, cubeCount.value.toInt())
                            }
                        }

                        textfield(cubeCount) {
                            text = "1"
                            maxWidth = 40.0
                        }

                        label("0: 빈칸 나올때까지 수행")
                    }


                }
            }

            tab("강화") {
                isClosable = false

                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = 5.0

                    val labelWidth = 40.0
                    val inputWidth = 45.0

                    button("강화 및 스타포스") {
                        action {
                            taskManager.upgradeItem()
                        }
                    }


                }
            }

            tab("기타") {
                isClosable = false

                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = 5.0

                    val labelWidth = 40.0
                    val inputWidth = 45.0

                    checkbox {
                        text = "F1 버튼으로 아래 작업 수행"
                        action {
                            taskManager.isSimpleTaskEnable = isSelected
                        }
                    }

                    val tasks = listOf(
                        MapleTaskManager.SIMPLE_TASK_AUTOCLICK,
                        MapleTaskManager.SIMPLE_TASK_AUTOSPACE,
                        MapleTaskManager.SIMPLE_TASK_AUTOSPACEANDENTER
                    )
                    combobox(taskManager.selectedSimpleTask, tasks) {
                        selectionModelProperty().get().select(0)
                    }

                    val dropDelay = SimpleIntegerProperty()
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        button("첫번째 빈칸까지 아이템 버리기") {
                            action {
                                taskManager.dropItem(dropDelay.value)
                            }
                        }

                        textfield(dropDelay) {
                            text = "340"
                            maxWidth = inputWidth
                        }
                        label("delay")
                    }

                    val duration = SimpleLongProperty()
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        button("z버튼 누르고 있기") {
                            action {
                                taskManager.pressZ(duration.value)
                            }
                        }

                        textfield(duration) {
                            text = "40"
                            maxWidth = inputWidth
                        }
                        label("초간 지속")
                    }

                }
            }

            tab("설정") {
                isClosable = false

                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = defaultItemSpacing
                        textfield(taskManager.winTarget) {
                            text = "MapleStory"
                            maxWidth = 90.0
                        }

                        label("활성화")
                    }

                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = defaultItemSpacing

                        label("큐브 딜레이")

                        val min = SimpleStringProperty()
                        val max = SimpleStringProperty()
                        textfield(min) {
                            text = UpgradeItemTask.cubeDelayMin.toString()
                            maxWidth = 50.0
                        }
                        label("~")
                        textfield(max) {
                            text = UpgradeItemTask.cubeDelayMax.toString()
                            maxWidth = 50.0
                        }
                        button("적용") {
                            action {
                                UpgradeItemTask.cubeDelayMin = min.value.toInt()
                                UpgradeItemTask.cubeDelayMax = max.value.toInt()
                            }
                        }

                    }

                    button("테스트1") {
                        action {
                            logI("")
                            taskManager.test()
                        }
                    }

                    button("테스트2") {
                        action {
                            logI("")
                            taskManager.test2()
                        }
                    }

                    button {
                        text = "로그 초기화화"
                       action {
                            logList.clear()
                        }
                    }
                }
            }

            tab("로그") {
                isClosable = false

                listview(logList) {
                    useMaxWidth = true
                    prefHeight = 200.0
                    selectionModel.selectionMode = SelectionMode.SINGLE
                }
            }
        }

        bottom = vbox {
            label(STATE_IDEL) {
                paddingAll = defaultItemSpacing
                useMaxWidth = true
                infoLabel = this
            }

            add(lastLogLabel)
            lastLogLabel.apply {
                useMaxWidth = true
                paddingLeft = 5.0
            }

        }
    }


    init {
        root.titledpane {
            title = "GHelper"
            try {
                addStageIcon(Image("https://upload2.inven.co.kr/upload/2019/10/14/bbs/i15774573889.png"))
            } catch (e: Exception) {
                println(e.message)
            }
        }
        primaryStage.isAlwaysOnTop = true

        taskManager.setOnTaskStateChangeListener {
            infoLabel.apply {
                Platform.runLater {
                    style {
                        when (it) {
                            STATE_PAUSED -> {
                                backgroundColor += Color.RED
                                fontWeight = FontWeight.BOLD
                                textFill = Color.WHITE
                            }
                            STATE_WORKING -> {
                                backgroundColor += Color.BLUE
                                fontWeight = FontWeight.BOLD
                                textFill = Color.WHITE

                            }
                            else -> {
                                backgroundColor += Color.TRANSPARENT
                                fontWeight = FontWeight.NORMAL
                                textFill = Color.BLACK
                            }
                        }
                    }
                    text = it
                }
            }
        }


    }


    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            taskManager.finishApp()
        }
        super.onDock()
    }

}