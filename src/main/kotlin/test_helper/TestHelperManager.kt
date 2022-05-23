package test_helper

import helper.BaseTaskManager
import helper.ConsumeEvent
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.random.Random


class TestHelperManager : BaseTaskManager() {
    var isHotkeyEnable = true
    val keyListener = ConsumeEvent().apply {
        setPressedListener {
            if (!isHotkeyEnable) return@setPressedListener true

            when (it.keyCode) {

                NativeKeyEvent.VC_F1 -> {
                    startPast()
                    false
                }

                NativeKeyEvent.VC_F2 -> {
                    if (jobMap.isEmpty()) return@setPressedListener true
                    //pause, resume
                    toggle()
                    false
                }

                NativeKeyEvent.VC_F3 -> {
                    if (jobMap.isEmpty()) {
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

    val ignoreError = SimpleBooleanProperty(true)
    val skipPostSpace = SimpleBooleanProperty(true)
    val skipCloseBracket = SimpleBooleanProperty(true)
    val delayMin = SimpleLongProperty(10)
    val delayMax = SimpleLongProperty(100)
    val openBrackets = hashSetOf ('(','{','[')
    val random = Random(1L)
    fun startPast() {
        runTask("past") {
            val clipboard = getStringFromClipboard()
            try {
                if (skipPostSpace.value) {
                    var before = ' '
                    clipboard.forEach {
                        if (before == '\n' && it == ' ') {

                        } else {
                            delay(random.nextLong(delayMin.value, delayMax.value))
                            send(it)
                            if(skipCloseBracket.value && openBrackets.contains(it)){
                                sendDelete()
                            }
                            before = it
                        }

                    }
                } else {
                    clipboard.forEach {
                        delay(random.nextLong(delayMin.value, delayMax.value))
                        send(it)
                        if(skipPostSpace.value && openBrackets.contains(it)){
                            sendDelete()
                        }
                    }
                }

            } catch (e: Exception) {
                TestHelperView.log(e.message ?: "오류발생")
//                TestHelperView.log("사용할 수 없는 문자 발견")
            }

        }
    }


    private fun getStringFromClipboard(): String {
        val data = Toolkit.getDefaultToolkit().systemClipboard.getContents(this)
        return try {
            data.getTransferData(DataFlavor.stringFlavor) as String
        } catch (e: Exception) {
            ""
        }
    }

    private val robot = Robot()

    suspend fun sendDelete() {
        robot.keyPress(KeyEvent.VK_DELETE)
        robot.keyRelease(KeyEvent.VK_DELETE)
    }

    private suspend fun send(key: Char) {
        val keyCode: Int
        var isShift = false
        if (key in 'A'..'Z') {
            isShift = true
            keyCode = key.toInt()
        } else if (key in 'a'..'z') {
            keyCode = key.toInt() - 32
        } else {
            isShift = true
            keyCode = when (key) {
                '\'' -> {
                    isShift = false
                    KeyEvent.VK_QUOTE
                }
                '"' -> KeyEvent.VK_QUOTE
                '_' -> KeyEvent.VK_MINUS
                ':' -> KeyEvent.VK_SEMICOLON
                '<' -> KeyEvent.VK_COMMA
                '>' -> KeyEvent.VK_PERIOD
                '?' -> KeyEvent.VK_SLASH
                '!' -> KeyEvent.VK_1
                '@' -> KeyEvent.VK_2
                '#' -> KeyEvent.VK_3
                '$' -> KeyEvent.VK_4
                '%' -> KeyEvent.VK_5
                '^' -> KeyEvent.VK_6
                '&' -> KeyEvent.VK_7
                '*' -> KeyEvent.VK_8
                '(' -> KeyEvent.VK_9
                ')' -> KeyEvent.VK_0
                '+' -> KeyEvent.VK_EQUALS
                '{' -> KeyEvent.VK_OPEN_BRACKET
                '}' -> KeyEvent.VK_CLOSE_BRACKET
                else -> {
                    isShift = false
                    key.toInt()
                }
            }
        }

        if (ignoreError.get()) {
            try {
                if (isShift) {
                    robot.keyPress(KeyEvent.VK_SHIFT)
                    robot.keyPress(keyCode)
                    robot.keyRelease(keyCode)
                    robot.keyRelease(KeyEvent.VK_SHIFT)
                } else {
                    robot.keyPress(keyCode)
                    robot.keyRelease(keyCode)
                }
            } catch (e: Exception) {
                TestHelperView.log("$key : 변환 실패")
            }

        } else {
            if (isShift) {
                robot.keyPress(KeyEvent.VK_SHIFT)
                robot.keyPress(keyCode)
                robot.keyRelease(keyCode)
                robot.keyRelease(KeyEvent.VK_SHIFT)
            } else {
                robot.keyPress(keyCode)
                robot.keyRelease(keyCode)
            }
        }


    }

    override fun finishApp() {
        GlobalScope.launch(Dispatchers.IO) {
            GlobalScreen.removeNativeKeyListener(keyListener)
            GlobalScreen.unregisterNativeHook()
            println("훅 해제완료!")
            super.finishApp()
        }
    }

}