package helper

import kotlinx.coroutines.*
import logI
import org.opencv.core.Core
import java.io.File
import kotlin.system.exitProcess

open class BaseTaskManager {
    companion object {
        val STATE_IDEL = "실행중인 작업 없음"
        val STATE_PAUSED = "일시정지"
        val STATE_WORKING = "실행중"
        val STATE_COMPLETE = "완료"
    }

    private val dispatcher = PauseableDispatcher(Thread())
    val jobMap = HashMap<String, Job>()

    var errorListener: ((String) -> Unit)? = null

    var taskStateListener: ((String) -> Unit)? = null
    fun setOnTaskStateChangeListener(taskStateListener: ((String) -> Unit)){
        this.taskStateListener = taskStateListener
    }

    init {
        System.load(File("").absolutePath + "\\libs\\${Core.NATIVE_LIBRARY_NAME}.dll")
    }

    fun runTask(id:String, block: suspend CoroutineScope.() -> Unit){
        jobMap[id]?.cancel()
        jobMap[id] = GlobalScope.launch(dispatcher) {
            notifyTaskStateChanged(STATE_WORKING)
            block.invoke(this)
            delay(100)
            jobMap.remove(id)
            println("$id 실행 완료.  jobMapSize: ${jobMap.size}")
            notifyTaskStateChanged()
        }

    }

    fun toggle(){
        if(jobMap.isNotEmpty()){
            GlobalScope.launch(Dispatchers.Default) {
                dispatcher.toggle()
                notifyTaskStateChanged()
            }
        }
    }

    private fun notifyTaskStateChanged(state:String? = null) {
//        logI("jobMapSize: ${jobMap.size}")
        state?.let {
            taskStateListener?.invoke(it)
            return
        }
        if(dispatcher.isPaused()){
            taskStateListener?.invoke(STATE_PAUSED)
        } else {
            if(jobMap.isEmpty()){
                taskStateListener?.invoke(STATE_IDEL)
            } else {
                taskStateListener?.invoke(STATE_WORKING)
            }
        }

    }

    fun pause(){
        // TODO: 실행중인 작업이 있을때만 일시정지 되도록 수정하기
        dispatcher.pause()
        notifyTaskStateChanged()
    }

    fun resume(){
        dispatcher.resume()
        notifyTaskStateChanged()
    }

    open fun finishApp(){
        resetTask()
        exitProcess(0)
    }

    open fun resetTask() {
        if(jobMap.isEmpty()){
//            logI("진행중인 작업이 없습니다.")
        } else
            logI("모든 작업 취소")

        GlobalScope.launch(Dispatchers.Default) {
            jobMap.values.forEach {
                it.cancel()
            }
            jobMap.clear()
            dispatcher.resume()
            notifyTaskStateChanged()
        }

    }
}