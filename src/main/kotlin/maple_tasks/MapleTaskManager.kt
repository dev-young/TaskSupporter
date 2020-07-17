package maple_tasks

import Testing
import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import helper.BaseTaskManager
import helper.ConsumeEvent
import helper.HelperCore
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import logI
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import winActive
import winMove
import java.awt.Point
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis


class MapleTaskManager : BaseTaskManager() {
    var isHotkeyEnable = true
    val keyListener = ConsumeEvent().apply {
        setPressedListener {
            if (!isHotkeyEnable) return@setPressedListener true

            when (it.keyCode) {

                NativeKeyEvent.VC_F2 -> {
                    //pause, resume
                    toggle()
                    false
                }

                NativeKeyEvent.VC_F3 -> {
                    resetTask()
                    false
                }

                NativeKeyEvent.VC_F4 -> {
                    if (jobMap.isEmpty())
                        finishApp()
                    else
                        resetTask()
                    true
                }

                NativeKeyEvent.VC_SPACE -> {
                    if (isItemCheckerEnable) {

                        if (mapleBaseTask == null)
                            mapleBaseTask = MapleBaseTask()
                        GlobalScope.launch {
                            mapleBaseTask?.findNextItem()
                        }
                        false
                    } else
                        true
                }

                NativeKeyEvent.VC_SPACE -> {
                    if (isItemCheckerEnable) {
                        if (mapleBaseTask == null)
                            mapleBaseTask = MapleBaseTask()
                        GlobalScope.launch {
                            mapleBaseTask?.findNextItem()
                        }
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
    var isItemCheckerEnable = false

    var winTarget = SimpleStringProperty()

    fun login(id: String, pw: String) {
        runTask("login") {
            if(activateTargetWindow())
                LoginTask().login(id, pw)
        }
    }

    fun synthesizeItem() {
        runTask("syn") {
            if(activateTargetWindow())
                MeisterTask().synthesizeItemSmartly()
        }
    }

    fun buyItem(useItemList: Boolean, buyAll: Boolean) {
        runTask("buyItem") {
            if(activateTargetWindow()){
                if (useItemList)
                    AuctionTask().buyItemListUntilEnd()
                else
                    AuctionTask().buyOneItemUntilEnd(buyAll)
            }

        }
    }

    fun makeItemInfinitely(maxCount: Int) {
        runTask("makeItem") {
            if(activateTargetWindow())
                MeisterTask().makeItemInfinitely(maxCount)
        }
    }

    private val MAX_TITLE_LENGTH = 1024
    private val user32 = User32.INSTANCE
    fun test() {
        User32.INSTANCE.apply {
            if(winActive("MapleStory"))
                winMove(Point(0, 0))
        }

    }

    fun test2() {

        logI("소요시간: " + measureNanoTime {
            val buffer = CharArray(MAX_TITLE_LENGTH * 2)
            val hwnd = User32.INSTANCE.GetForegroundWindow()
            User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH)
            println("Active window title: " + Native.toString(buffer))
            val rect = WinDef.RECT()
            User32.INSTANCE.GetWindowRect(hwnd, rect)
            println("rect = $rect")
        })

    }

    fun activateTargetWindow(): Boolean {
        return User32.INSTANCE.winActive(winTarget.value)
    }

    override fun resetTask() {
        super.resetTask()
        mapleBaseTask = null
    }

}