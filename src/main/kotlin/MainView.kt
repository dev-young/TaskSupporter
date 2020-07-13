import BaseTaskManager.Companion.STATE_IDEL
import BaseTaskManager.Companion.STATE_PAUSED
import BaseTaskManager.Companion.STATE_WORKING
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import maple_tasks.MapleTaskManager
import tornadofx.*
import java.io.File

class MainView : View() {
    private val defaultItemSpacing = 8.0
    private lateinit var infoLabel: Label
    override val root = borderpane {

        top = checkbox {
            paddingHorizontal = defaultItemSpacing
            paddingVertical = 5
            text = "항상 위에 표시"
            isSelected = true
            action { primaryStage.isAlwaysOnTop = isSelected }
        }

        center = tabpane {
            selectionModel.selectedItemProperty().addListener { observable, oldTab, newTab ->
                logI("선택된 탭: ${newTab.text}")
            }

            tab("로그인") {
                isClosable = false
                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing
                    val accountList = loadAccountList()
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
                    spacing = defaultItemSpacing
                    button("자동합성") {
                        action {
                            runAsync {
                                taskManager.synthesizeItem()
                            }
                        }
                    }
                    button("자동분해") {
                        action {
                            infoLabel
                        }
                    }

                    button("반복제작") {
                        action {
                            taskManager.makeItemInfinitely()
                        }
                    }

                    checkbox {
                        text = "스페이스바로 아이템 확인하기 (F3:위치 초기화)"
                        action {
                            taskManager.isItemCheckerEnable = isSelected
                        }
                    }

                }
            }

            tab("경매장") {
                isClosable = false
                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing
                    var buyAll: CheckBox? = null
                    val useItemList = checkbox {
                        text = "ItemList.txt 사용"
                        action {
                            buyAll?.let {
                                it.isDisable = isSelected
                            }
                        }
                    }

                    buyAll = checkbox {
                        text = "구매수량 최대치 입력"
                    }

                    button("아이템 검색 및 구매") {
                        action {
                            taskManager.buyItem(useItemList.isSelected, buyAll.isSelected)
                        }
                    }
                }
            }

            tab("강화/큐브") {
                isClosable = false

                vbox {
                    paddingAll = defaultItemSpacing
                    spacing = defaultItemSpacing
                    button("수큐 에픽 띄우기")
                }
            }
        }

        infoLabel = label(STATE_IDEL) {
            paddingAll = defaultItemSpacing
            useMaxWidth = true
        }

        bottom = infoLabel
    }

    val taskManager = MapleTaskManager()

    private fun loadAccountList(): List<List<String>> {
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

    init {
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