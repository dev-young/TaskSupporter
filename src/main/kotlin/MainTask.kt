import maple_tasks.ActionTask
import maple_tasks.MapleBaseTask
import maple_tasks.MeisterTask
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import org.opencv.core.Core
import java.awt.event.KeyEvent
import java.io.File
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

class MainTask: NativeKeyListener {
    init {
        println("시작!")
    }

    fun run(){
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.getPackage().name).level = Level.OFF
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(this)

        System.load(File("").absolutePath + "\\libs\\${Core.NATIVE_LIBRARY_NAME}.dll")
    }

    override fun nativeKeyTyped(p0: NativeKeyEvent?) {
    }

    override fun nativeKeyPressed(nativeKeyEvent: NativeKeyEvent) {
        when (nativeKeyEvent.keyCode) {

            NativeKeyEvent.VC_F2 -> {
                MapleBaseTask().apply {
                    helper.smartClick(1000, 500)
                    helper.keyPress(KeyEvent.VK_ENTER)
                }
            }

            NativeKeyEvent.VC_F3 -> {
                val t = Thread(Runnable {
                    for (i in 1..100){
                        Thread.sleep(1000)
                        println(i)
                    }

                })
                t.start()
            }

            NativeKeyEvent.VC_F4 -> {
                exitProcess(0)
            }


            NativeKeyEvent.VC_F5 -> {

                val t = Thread(Runnable {
                    MeisterTask().synthesizeItemSmartly()
                })
                t.start()

            }

            NativeKeyEvent.VC_F6 -> {
                val t = Thread(Runnable {
                    ActionTask().auctionTest()
                })
                t.start()

            }


            else -> {
            }
        }
    }

    override fun nativeKeyReleased(p0: NativeKeyEvent?) {
    }

}