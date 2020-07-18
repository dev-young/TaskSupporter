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
import winIsForeground
import winMove
import java.awt.Point
import java.io.File
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.measureNanoTime


class MapleTaskManager : BaseTaskManager() {
    var isHotkeyEnable = true
    val keyListener = ConsumeEvent().apply {
        setPressedListener {
            if (!isHotkeyEnable) return@setPressedListener true

            when (it.keyCode) {

                NativeKeyEvent.VC_F2 -> {
                    if(jobMap.isEmpty()) return@setPressedListener true
                    //pause, resume
                    toggle()
                    false
                }

                NativeKeyEvent.VC_F3 -> {
                    if(jobMap.isEmpty() && !isItemCheckerEnable) {
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

                        if(!User32.INSTANCE.winIsForeground(winTarget.value))
                            return@setPressedListener true

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

    fun cubeItem(targetOptions: HashMap<String, Int>, count:Int = 1) {
        runTask("cube") {
            if(activateTargetWindow())
                UpgradeItemTask().useCube(targetOptions, count)
        }
    }

    fun test() {


    }


    fun activateTargetWindow(): Boolean {
        return User32.INSTANCE.winActive(winTarget.value)
    }

    override fun resetTask() {
        super.resetTask()
        mapleBaseTask = null
    }

}