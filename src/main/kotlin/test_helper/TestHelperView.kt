package test_helper

import helper.BaseTaskManager
import helper.BaseTaskManager.Companion.STATE_PAUSED
import helper.BaseTaskManager.Companion.STATE_WORKING
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class TestHelperView : View() {
    companion object {
        val lastLogLabel = Label()

        fun log(msg:Any) {
            Platform.runLater {
                lastLogLabel.text = msg.toString()
            }
        }
    }

    private val defaultItemSpacing = 7.0
    private lateinit var infoLabel: Label
    val taskManager = TestHelperManager()

    override val root = borderpane {
        top = hbox {
            alignment = Pos.CENTER_LEFT
            spacing = defaultItemSpacing
            paddingHorizontal = defaultItemSpacing
            checkbox {
                text = "항상 위"
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
        center = hbox {
            alignment = Pos.CENTER_LEFT
            spacing = defaultItemSpacing
            paddingHorizontal = defaultItemSpacing

            checkbox("에러 무시 ", taskManager.ignoreError)
            checkbox("앞 공백 스킵 ", taskManager.skipPostSpace)
            checkbox(")}]스킵", taskManager.skipCloseBracket)

            label("  딜레이:")
            textfield(taskManager.delayMin) {
                maxWidth = 35.0
                alignment = Pos.CENTER
            }
            label("~")
            textfield(taskManager.delayMax) {
                maxWidth = 40.0
                alignment = Pos.CENTER
            }

        }
        bottom = vbox {
            label(BaseTaskManager.STATE_IDEL) {
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
            title = "요건 몰랐지?"
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