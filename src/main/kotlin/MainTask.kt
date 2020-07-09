import helper.HelperCore
import kotlinx.coroutines.*
import maple_tasks.ActionTask
import maple_tasks.MeisterTask
import helper.PauseableDispatcher
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import org.opencv.core.Core
import java.io.File
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

// TODO: 2020-07-09 핵심 기능(일시정지, 종료 등) 따로 부모클래스로 만들어두기
class MainTask : NativeKeyListener {
    init {
    }

    fun run() {
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.getPackage().name).level = Level.OFF
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(this)

        System.load(File("").absolutePath + "\\libs\\${Core.NATIVE_LIBRARY_NAME}.dll")


        log("")
        println("f2: 일시정지")
        println("f3: 작업초기화")
        println("f4: 종료 (작업중인경우 작업 초기화)")
        println("f5: 합성하기")
        println("f6: 경매장 무한 검색 및 구매 (1개씩 구매)")
        println("f7: 경매장 무한 검색 및 구매 (모두 구매)")
        println()

    }

    override fun nativeKeyTyped(p0: NativeKeyEvent?) {
    }

    private val dispatcher = PauseableDispatcher(Thread())
    private val jobMap = HashMap<Int, Job>()
    override fun nativeKeyPressed(nativeKeyEvent: NativeKeyEvent) {
        when (nativeKeyEvent.keyCode) {

            NativeKeyEvent.VC_F2 -> {
                //pause, resume
                dispatcher.toggle()
            }

            NativeKeyEvent.VC_F3 -> {
                finishAllTask()
            }

            NativeKeyEvent.VC_F4 -> {
                if(jobMap.isEmpty())
                    exitProcess(0)
                else
                    finishAllTask()
            }

            NativeKeyEvent.VC_F5 -> {
                jobMap[nativeKeyEvent.keyCode]?.cancel()
                jobMap[nativeKeyEvent.keyCode] = GlobalScope.launch(dispatcher) {
                    MeisterTask().synthesizeItemSmartly()
                }
            }

            NativeKeyEvent.VC_F6 -> {
                jobMap[nativeKeyEvent.keyCode]?.cancel()
                jobMap[nativeKeyEvent.keyCode] = GlobalScope.launch(dispatcher) {
                    ActionTask().buyOneItemUntilEnd(false)
                }

            }

            NativeKeyEvent.VC_F7 -> {
//                startTestTask(nativeKeyEvent.keyCode)

                jobMap[nativeKeyEvent.keyCode]?.cancel()
                jobMap[nativeKeyEvent.keyCode] = GlobalScope.launch(dispatcher) {
                    ActionTask().buyOneItemUntilEnd(true)
                }
            }


            else -> {
            }
        }
    }

    private fun finishAllTask() {
        log("모든 작업 취소")
        jobMap.values.forEach {
            it.cancel()
        }
        jobMap.clear()
        dispatcher.resume()

    }

    private fun startTestTask(keyCode: Int) {
        jobMap[keyCode]?.cancel()
        val j = GlobalScope.launch(dispatcher) {
            if (this.isActive) {
                repeat(100) {
                    log(it)
                    delay(1000)
                }
            }
        }
        jobMap[keyCode] = j
    }

    override fun nativeKeyReleased(p0: NativeKeyEvent?) {
    }

}