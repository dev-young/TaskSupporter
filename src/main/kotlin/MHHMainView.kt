import com.sun.jna.platform.win32.User32
import helper.BaseTaskManager.Companion.STATE_IDEL
import helper.BaseTaskManager.Companion.STATE_PAUSED
import helper.BaseTaskManager.Companion.STATE_WORKING
import helper.HelperCore
import javafx.application.Platform
import javafx.beans.property.*
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maple_tasks.*
import tornadofx.*
import java.awt.Point
import java.awt.Toolkit
import java.lang.Exception

class MHHMainView : View() {
    companion object {
        val logList = arrayListOf<String>().asObservable()
        val lastLogLabel = Label()
    }

    private val defaultItemSpacing = 7.0
    private lateinit var infoLabel: Label
    val taskManager = MHHTaskManager()

    override val root = borderpane {
        top = hbox {

            spacing = defaultItemSpacing
            checkbox {
                paddingHorizontal = defaultItemSpacing
                paddingVertical = 5
                text = "항상 위에 표시"
                isSelected = false
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
                    taskManager.activateTargetWindow()
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
            tab("사냥") {
                isClosable = false

                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing

                    hbox {
                        textfield(taskManager.portName){
                            maxWidth = 80.0
                        }
                        button("연결") {
                            action {
                                taskManager.connectHW()
                            }
                        }
                        val keyCode = SimpleIntegerProperty(128)
                        textfield(keyCode){
                            maxWidth = 80.0
                        }
                        button("연결") {
                            action {
                                taskManager.keyTest(keyCode.value)
                            }
                        }
                    }

                    hbox {
                        spacing = defaultItemSpacing
                        vbox {
                            textfield(taskManager.fileName) {

                            }
                            hbox {
                                alignment = Pos.CENTER_RIGHT
                                button("불러오기") {
                                    action { taskManager.saveRangeData() }
                                    minWidth = 50.0
                                }

                            }
                        }

                        borderpane {

                            top = vbox {
                                alignment = Pos.CENTER
                                textfield(taskManager.limitTop) {
                                    maxWidth = 50.0
                                }
                            }

                            bottom = vbox {
                                alignment = Pos.CENTER
                                textfield(taskManager.limitBottom) {
                                    maxWidth = 50.0
                                }
                            }

                            left = textfield(taskManager.limitLeft) {
                                maxWidth = 50.0
                            }

                            right = textfield(taskManager.limitRight) {
                                maxWidth = 50.0
                            }

                            center = button("저장") {
                                action { taskManager.saveRangeData() }
                                minWidth = 50.0
                            }


                        }
                    }

                    hbox {
                        alignment = Pos.CENTER
                        label("공격1 딜레이")
                        textfield(taskManager.attackDelayMin)
                    }

                    hbox {
                        alignment = Pos.CENTER
                        label("리젠 주기")
                        textfield(taskManager.huntRange.zenDelay)
                    }

                    hbox {
                        alignment = Pos.CENTER
                        spacing = 9.0
                        checkbox("비올레타", taskManager.checkTargets.violeta)
                        checkbox("룬", taskManager.checkTargets.rune)
                        checkbox("현상금", taskManager.checkTargets.bounty)
                        checkbox("엘보", taskManager.checkTargets.boss)
                        checkbox("석화", taskManager.checkTargets.stony)
                        textfield(taskManager.checkTargets.delay) { maxWidth = 50.0 }

                    }

                    button("섀도어 본색1") {
                        action {
                            taskManager.shadowerLachelein1()
                        }
                    }


                    button("섀도어 머쉬버드숲") {
                        action {
                            taskManager.mushBirdHuntShadower()
                        }
                    }

                    button("아델 일자맵") {
                        action {
                            taskManager.horizontalHuntAdel()
                        }
                    }

                    button("패파 안개숲") {
                        action {
                            taskManager.mistyForestHuntPathfinder()
                        }
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
                        }
                    }

                    button("테스트2") {
                        action {
                            logI("")
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
                    longpress {
                        logList.clear()
                    }
                }

            }
        }

        bottom = vbox {
            label(STATE_IDEL) {
                paddingBottom = defaultItemSpacing
                paddingLeft = defaultItemSpacing
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
            title = "MHH"
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

    override fun onBeforeShow() {
        super.onBeforeShow()
        println("onBeforeShow")

        GlobalScope.launch(Dispatchers.Default) {
            delay(500)
            val hwnd = User32.INSTANCE.FindWindow(null, title).let {
                var h = it
                for (i in 1..100) {
                    if (h != null)
                        break
                    delay(100)
                    h = User32.INSTANCE.FindWindow(null, title)
                }
                h
            }
            val startPoint = Toolkit.getDefaultToolkit().screenSize.let {
                val w = if (it.width > 1920) 1920 else it.width
                val h = it.height
                User32.INSTANCE.winGetPos(hwnd).let {
                    Point(w - it.width(), h - it.getHeight() - 40)
                }

            }
            Platform.runLater { primaryStage.isAlwaysOnTop = false }
            User32.INSTANCE.winMove(startPoint, hwnd_ = hwnd)
            User32.INSTANCE.winActive(hwnd, 1)
        }
    }

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            taskManager.finishApp()
        }
        super.onDock()
    }


}