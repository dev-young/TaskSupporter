import kotlinx.coroutines.*
import maple_tasks.ActionTask
import maple_tasks.MeisterTask
import maple_tasks.PauseableDispatcher
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import org.opencv.core.Core
import java.io.File
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

class MainTask : NativeKeyListener {
    init {
    }

    fun run() {
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.getPackage().name).level = Level.OFF
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(this)

        System.load(File("").absolutePath + "\\libs\\${Core.NATIVE_LIBRARY_NAME}.dll")


        println("f2: 일시정지")
        println("f3: 작업초기화")
        println("f4: 종료 (작업중인경우 작업 초기화)")
        println("f5: 합성하기")
        println("f6: 경매장 무한 검색 및 구매 (테스트중)")
        println("f7: 카운팅 테스트")

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
                val j = GlobalScope.launch(dispatcher) {
                    if (this.isActive) {
                        MeisterTask().synthesizeItemSmartly()
                    }
                }
                jobMap[nativeKeyEvent.keyCode] = j
            }

            NativeKeyEvent.VC_F6 -> {
                jobMap[nativeKeyEvent.keyCode]?.cancel()
                val j = GlobalScope.launch(dispatcher) {
                    if (this.isActive) {
                        ActionTask().auctionTest()
                    }
                }
                jobMap[nativeKeyEvent.keyCode] = j

            }

            NativeKeyEvent.VC_F7 -> {
                startTestTask(nativeKeyEvent.keyCode)
            }


            else -> {
            }
        }
    }

    private fun finishAllTask() {
        println("모든 작업 취소")
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
                    println(it)
                    delay(1000)
                }
            }
        }
        jobMap[keyCode] = j
    }

    override fun nativeKeyReleased(p0: NativeKeyEvent?) {
    }

}