package helper

import org.jnativehook.NativeInputEvent
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import java.util.*
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

class ConsumeEvent : NativeKeyListener {
    class VoidDispatchService : AbstractExecutorService() {
        private var running = false
        override fun shutdown() {
            running = false
        }

        override fun shutdownNow(): List<Runnable> {
            running = false
            return ArrayList(0)
        }

        override fun isShutdown(): Boolean {
            return !running
        }

        override fun isTerminated(): Boolean {
            return !running
        }

        @Throws(InterruptedException::class)
        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
            return true
        }

        override fun execute(r: Runnable) {
            r.run()
        }

        init {
            running = true
        }
    }



    private var pressedListener : ((e:NativeKeyEvent) -> Boolean)? = null
    /**@return os에 이벤트를 전달할건지 여부 */
    fun setPressedListener(listener: (e:NativeKeyEvent) -> Boolean){
        this.pressedListener = listener
    }


    private var releasedListener : ((e:NativeKeyEvent) -> Boolean)? = null
    /**@return os에 이벤트를 전달할건지 여부 */
    fun setReleasedListener(listener: (e:NativeKeyEvent) -> Boolean){
        this.releasedListener = listener
    }


    override fun nativeKeyPressed(e: NativeKeyEvent) {
        pressedListener?.invoke(e)?.let {
            if(!it) {
                try {
                    val f = NativeInputEvent::class.java.getDeclaredField("reserved")
                    f.isAccessible = true
                    f.setShort(e, 0x01.toShort())
//                    print("[ OK ]\n")
                } catch (ex: Exception) {
//                    print("[ !! ]\n")
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
        releasedListener?.invoke(e)?.let {
            if(!it) {
                try {
                    val f = NativeInputEvent::class.java.getDeclaredField("reserved")
                    f.isAccessible = true
                    f.setShort(e, 0x01.toShort())
//                    print("[ OK ]\n")
                } catch (ex: Exception) {
//                    print("[ !! ]\n")
                    ex.printStackTrace()
                }
            }
        }

    }

    override fun nativeKeyTyped(e: NativeKeyEvent) { /* Unimplemented */
    }
}