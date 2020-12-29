package maple_tasks

import com.sun.jna.platform.win32.User32
import helper.BaseTaskManager
import helper.ConsumeEvent
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import logH
import maple_tasks.hunt.AdelTask
import maple_tasks.hunt.HuntBaseTask
import maple_tasks.hunt.PathfinderTask
import maple_tasks.hunt.ShadowerTask
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import tornadofx.runLater
import winActive
import winIsForeground
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger


class MHHTaskManager : BaseTaskManager() {
    var isHotkeyEnable = true
    val keyListener = ConsumeEvent().apply {
        setPressedListener {
            if (!isHotkeyEnable) return@setPressedListener true
//            println("${it.keyCode} ${it.keyChar}")
            when (it.keyCode) {

                NativeKeyEvent.VC_F1 -> {
                    currentHuntTask.findCharacter()?.let {
                        logH("${it.x}, ${it.y}")
                        runLater { limitLeft.value = it.x }
                    }
                    false
                }

                NativeKeyEvent.VC_F2 -> {
                    currentHuntTask.findCharacter()?.let {
                        logH("${it.x}, ${it.y}")
                        runLater { limitRight.value = it.x }
                    }
                    false
                }

                NativeKeyEvent.VC_F3 -> {
                    if (jobMap.isEmpty()) return@setPressedListener true
                    //pause, resume
                    activateTargetWindow()
                    toggle()
                    releaseAllKey()
                    false
                }

                NativeKeyEvent.VC_F4 -> {
                    if (jobMap.isEmpty())
                        if (Settings.instance.enableF4OnlyForeground) {
                            if (User32.INSTANCE.winIsForeground("MHH")) {
                                finishApp()
                            } else return@setPressedListener true
                        } else
                            finishApp()
                    else
                        resetTask()
                    false
                }

                NativeKeyEvent.VC_SPACE -> {
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
        Settings.load()
    }

    override fun finishApp() {
        GlobalScope.launch(Dispatchers.IO) {
            GlobalScreen.removeNativeKeyListener(keyListener)
            GlobalScreen.unregisterNativeHook()
            println("훅 해제완료!")
            super.finishApp()
        }
    }


    private var currentHuntTask = HuntBaseTask()
    val winTarget = SimpleStringProperty()
    val fileName = SimpleStringProperty()
    val limitLeft = SimpleIntegerProperty(65)
    val limitRight = SimpleIntegerProperty(135)
    val limitTop = SimpleIntegerProperty(80)
    val limitBottom = SimpleIntegerProperty(150)
    val huntRange = HuntBaseTask.HuntRange(limitLeft, limitRight, limitTop, limitBottom)

    val checkTargets = HuntBaseTask.CheckTargets()

    val attackDelayMin = SimpleIntegerProperty(1000)

    fun activateTargetWindow(): Boolean {
        return User32.INSTANCE.winActive(winTarget.value)
    }

    private fun releaseAllKey() {
        currentHuntTask.helper.releaseAll()
    }


    override fun resetTask() {
        super.resetTask()
    }

    fun horizontalHuntAdel() {
        runTask("hunt") {
            if (activateTargetWindow()) {
                AdelTask(huntRange).apply {
                    currentHuntTask = this
                    startHorizontal()
                }
            }
        }
    }

    fun mushBirdHuntShadower() {
        runTask("hunt") {
            if (activateTargetWindow()) {
                ShadowerTask(huntRange, attackDelayMin).apply {
                    currentHuntTask = this
                    startHorizontal()
                }
            }
        }
    }

    fun mistyForestHuntPathfinder() {
        runTask("hunt") {
            if (activateTargetWindow()) {
                PathfinderTask(HuntBaseTask.HuntRange(limitLeft, limitRight), attackDelayMin).apply {
                    currentHuntTask = this
                    startMistyForest()
                }
            }
        }
    }

    fun saveRangeData() {

    }

    override fun runTask(id: String, block: suspend CoroutineScope.() -> Unit) {
        super.runTask("check") {
            if (activateTargetWindow()) {
                currentHuntTask.startCheck(checkTargets)
            }
        }
        super.runTask(id, block)
    }

}