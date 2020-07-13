package maple_tasks

import BaseTaskManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

class MapleTaskManager : BaseTaskManager(), NativeKeyListener {

    init {
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.getPackage().name).level = Level.OFF
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(this)
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

    fun makeItemInfinitely() {
        runTask("makeItem") {
            MeisterTask().makeItemInfinitely()
        }
    }

    override fun nativeKeyTyped(p0: NativeKeyEvent?) {

    }

    override fun nativeKeyPressed(keyEvent: NativeKeyEvent) {
        when (keyEvent.keyCode) {

            NativeKeyEvent.VC_F2 -> {
                //pause, resume
                toggle()
            }

            NativeKeyEvent.VC_F3 -> {
                resetTask()
            }

            NativeKeyEvent.VC_F4 -> {
                if(jobMap.isEmpty())
                    finishApp()
                else
                    resetTask()
            }

            NativeKeyEvent.VC_F4 -> {
                if(jobMap.isEmpty())
                    finishApp()
                else
                    resetTask()
            }

            NativeKeyEvent.VC_SPACE -> {
                if(isItemCheckerEnable){
                    if(mapleBaseTask == null)
                        mapleBaseTask = MapleBaseTask()
                    GlobalScope.launch {
                        mapleBaseTask?.findNextItem()
                    }
                }
            }




            else -> {
            }
        }
    }

    override fun nativeKeyReleased(p0: NativeKeyEvent?) {

    }


    override fun resetTask() {
        super.resetTask()
        mapleBaseTask = null
    }

    override fun finishApp() {
        GlobalScreen.removeNativeKeyListener(this)
        GlobalScreen.unregisterNativeHook()
        super.finishApp()
    }

}