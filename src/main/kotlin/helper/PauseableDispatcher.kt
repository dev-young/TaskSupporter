package helper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.isActive
import logI
import java.util.*
import kotlin.coroutines.CoroutineContext

class PauseableDispatcher (private val handler: Thread): CoroutineDispatcher(){
    private val queue: Queue<Runnable> = LinkedList()
    private var isPaused: Boolean = false
    fun isPaused(): Boolean {
        return isPaused
    }

    @Synchronized override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (isPaused) {
            isActive
            queue.add(block)
        } else {
            handler.run {
                if (GlobalScope.isActive) {
                    block.run()
                }
            }
        }
    }

    @Synchronized fun pause() {
        isPaused = true
    }

    @Synchronized fun resume() {
        isPaused = false
        runQueue()
    }

    private fun runQueue() {
        queue.iterator().let {
            while (it.hasNext()) {
                val block = it.next()
                it.remove()
                handler.run { block.run() }
            }
        }
    }

    fun toggle() {
        if(isPaused){
            logI("resume!")
            resume()
        }
        else{
            logI("pause!")
            pause()
        }

    }
}