package maple_tasks

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import java.util.*
import kotlin.coroutines.CoroutineContext

class PauseableDispatcher (private val handler: Thread): CoroutineDispatcher(){
    private val queue: Queue<Runnable> = LinkedList()
    private var isPaused: Boolean = false

    @Synchronized override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (isPaused) {
            queue.add(block)
        } else {
            handler.run { block.run() }
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
            println("resume!")
            resume()
        }
        else{
            println("pause!")
            pause()
        }

    }
}