package maple_tasks

import helper.BaseTaskManager
import helper.ConsumeEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

class MapleTaskManager : BaseTaskManager() {
    var isHotkeyEnable = true
    val keyListener = ConsumeEvent().apply {
        setPressedListener {
            if(!isHotkeyEnable) return@setPressedListener true
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
                    if(jobMap.isEmpty())
                        finishApp()
                    else
                        resetTask()
                    true
                }

                NativeKeyEvent.VC_SPACE -> {
                    if(isItemCheckerEnable){
                        if(mapleBaseTask == null)
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



    fun login(id: String, pw: String) {
        runTask("login") {
            val result = LoginTask().login(id, pw)
        }
    }

    fun synthesizeItem() {
        runTask("syn") {
            MeisterTask().synthesizeItemSmartly()
        }
    }

    fun buyItem(useItemList: Boolean, buyAll: Boolean) {
        runTask("buyItem") {
            if (useItemList)
                AuctionTask().buyItemListUntilEnd()
            else
                AuctionTask().buyOneItemUntilEnd(buyAll)
        }
    }

    fun makeItemInfinitely(maxCount: Int) {
        runTask("makeItem") {
            MeisterTask().makeItemInfinitely(maxCount)
        }
    }


    override fun resetTask() {
        super.resetTask()
        mapleBaseTask = null
    }

}