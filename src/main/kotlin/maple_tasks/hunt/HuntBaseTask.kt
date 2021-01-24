package maple_tasks.hunt

import getHeight
import helper.HWKey
import helper.HelperCore
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logH
import maple_tasks.MapleBaseTask
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import toMat
import width
import winGetPos
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit

open class HuntBaseTask {
    class HuntRange(
        val left: SimpleIntegerProperty,
        val right: SimpleIntegerProperty,
        val top: SimpleIntegerProperty = SimpleIntegerProperty(10),
        val bottom: SimpleIntegerProperty = SimpleIntegerProperty(100),
        val zenDelay: SimpleLongProperty = SimpleLongProperty(7500), //젠 딜레이
        val name: String = ""
    )

    class CheckTargets(
        val violeta: SimpleBooleanProperty = SimpleBooleanProperty(true),
        val rune: SimpleBooleanProperty = SimpleBooleanProperty(true),
        val rune2: SimpleBooleanProperty = SimpleBooleanProperty(false),
        val bounty: SimpleBooleanProperty = SimpleBooleanProperty(false),
        val boss: SimpleBooleanProperty = SimpleBooleanProperty(true),
        val stony: SimpleBooleanProperty = SimpleBooleanProperty(true),
        val event: SimpleBooleanProperty = SimpleBooleanProperty(false),
        var delay: SimpleLongProperty = SimpleLongProperty(4000)
    )

    class Buff(
        var keyCode: Int,
        var delay: Int,
        var delayAfter: Int = 900,
        var delayBefore: Int = 600,
        var withStop: Boolean = true
    ) {
        var lastUse: Long = 0

        suspend fun useIfEnable(huntBaseTask: HuntBaseTask, current: Long = System.currentTimeMillis()) {
            if (current - lastUse > delay * 1000) {
                if (withStop)
                    huntBaseTask.releaseDirection()
                huntBaseTask.helper.delayRandom(delayBefore, delayBefore + 50)
                huntBaseTask.send(keyCode)
                lastUse = current
                huntBaseTask.helper.delayRandom(delayAfter, delayAfter + 50)
            }
        }

    }

    val helper: HelperCore = HelperCore(true)
    var zenTime = System.currentTimeMillis()

    suspend open fun waitZen(zenDelay: Long) {
        val current = System.currentTimeMillis()
        val nextZenTime = zenTime + zenDelay
        if (nextZenTime > current) {
            (nextZenTime - current).let {
                logH("$it")
                delay(it + helper.random.nextInt(1000))
            }
            zenTime = nextZenTime
        } else {
            zenTime += ((current - zenTime) / zenDelay).toInt() * zenDelay
        }

    }

    val characterMat = Imgcodecs.imread("img\\myChar.png")
    val runeMat = Imgcodecs.imread("img\\hunt\\rune.png")
    val rune2Mat = Imgcodecs.imread("img\\hunt\\rune2.png")
    val bountyMat = Imgcodecs.imread("img\\hunt\\bounty.png")
    val violetaMat = Imgcodecs.imread("img\\hunt\\violeta.png")
    val bossMat = Imgcodecs.imread("img\\hunt\\boss.png")
    val stonyMat = Imgcodecs.imread("img\\hunt\\stony.png")
    val eventMat = Imgcodecs.imread("img\\hunt\\eventComplete.png")
    val mapStart = Point(10, 80)
    var mapWidth = 250
    var mapHeight = 100

    var jumpKey = HWKey.VK_ALT
    var jumpKey2 = HWKey.VK_END
    var ropeConnectKey = HWKey.VK_V

    /**상대위치 반환*/
    fun getCharacterPos(): Point? {
        helper.apply {
            val rect = user32.winGetPos()
            val mapLeftTop = Point(rect.left + mapStart.x, rect.top + mapStart.y)
            return imageSearch(mapLeftTop, mapWidth, mapHeight, characterMat, 95.0)?.let {
                Point(
                    it.x - rect.left,
                    it.y - rect.top
                )
            }
        }
    }

    fun checkVioleta(source: Mat): Boolean {
        //4분할 화면중 오른쪽 아래
        val halfRB = source.rowRange(source.rows() / 2, source.rows()).colRange(source.cols() / 2, source.cols())
        return helper.imageSearchReturnBoolean(halfRB, violetaMat, 95.0)
    }

    suspend fun startCheck(checkTargets: CheckTargets) {
        while (true) {
            delay(checkTargets.delay.value)
            helper.apply {
                val rect = user32.winGetPos()
                val source = createScreenCapture(rect.toRectangle()).toMat()
                val map =
                    source.rowRange(mapStart.y, mapStart.y + mapHeight).colRange(mapStart.x, mapStart.x + mapWidth)
                checkTargets.apply {
                    if (violeta.value && checkVioleta(source)) {
                        Toolkit.getDefaultToolkit().beep()
                        kotlinx.coroutines.delay(850)
                        Toolkit.getDefaultToolkit().beep()
                        kotlinx.coroutines.delay(850)
                        Toolkit.getDefaultToolkit().beep()
                        logH("비올레타")
                    }
                    if (rune.value && helper.imageSearchReturnBoolean(map, runeMat, 95.0)) {
                        Toolkit.getDefaultToolkit().beep()
                        logH("룬")
                    }
                    if (rune2.value && helper.imageSearchReturnBoolean(source, rune2Mat, 35.0)) {
                        Toolkit.getDefaultToolkit().beep()
                        logH("룬2")
                    }
                    if (bounty.value && helper.imageSearchReturnBoolean(map, bountyMat, 95.0)) {
                        Toolkit.getDefaultToolkit().beep()
                        logH("현상금/불늑")
                    }
                    if (boss.value && helper.imageSearchReturnBoolean(source, bossMat, 95.0)) {
                        Toolkit.getDefaultToolkit().beep()
                        logH("엘보")
                    }
                    if (stony.value && helper.imageSearchReturnBoolean(source, stonyMat, 95.0)) {
                        Toolkit.getDefaultToolkit().beep()
                        logH("석화")
                    }
                    if (event.value && helper.imageSearchReturnBoolean(source, eventMat, 95.0)) {
                        Toolkit.getDefaultToolkit().beep()
                        logH("코인 완료")
                    }
                }

            }
        }
    }

    suspend fun send(HWKey: Int, delayMin: Int = 45, delayMax: Int = 65) {
        helper.keyPress(HWKey)
        helper.delayRandom(delayMin, delayMax)
        helper.keyRelease(HWKey)
    }

    open suspend fun jump() {
        send(jumpKey)
    }

    open suspend fun doubleJump(delayMin: Int = 150, delayMax: Int = 210) {
        send(jumpKey)
        helper.delayRandom(delayMin, delayMax)
        send(jumpKey)
    }

    open suspend fun doubleJump2(delayMin: Int = 50, delayMax: Int = 80) {
        send(jumpKey)
        helper.delayRandom(delayMin, delayMax)
        send(jumpKey2)
    }

    open suspend fun ropeConnect(finishDelay: Long = 0) {
        send(ropeConnectKey)
        if (finishDelay > 0) {
            delay(finishDelay)
            send(ropeConnectKey)
        }
    }

    open suspend fun downJump(afterDelay: Int = 0) {
        helper.apply {
            downPress()
            delayRandom(200, 300)
            send(jumpKey)
            delayRandom(100, 200)
            downRelease()
            delayRandom(afterDelay, afterDelay + 100)
        }
    }

    fun leftPress() {
        helper.keyPress(HWKey.VK_LEFT)
    }

    fun leftRelease() {
        helper.keyRelease(HWKey.VK_LEFT)
    }

    fun rightPress() {
        helper.keyPress(HWKey.VK_RIGHT)
    }

    fun rightRelease() {
        helper.keyRelease(HWKey.VK_RIGHT)
    }

    fun upPress() {
        helper.keyPress(HWKey.VK_UP)
    }

    fun upRelease() {
        helper.keyRelease(HWKey.VK_UP)
    }

    fun downPress() {
        helper.keyPress(HWKey.VK_DOWN)
    }

    fun downRelease() {
        helper.keyRelease(HWKey.VK_DOWN)
    }


    private fun releaseDirection() {
        upRelease()
        downRelease()
        leftRelease()
        rightRelease()
    }

}