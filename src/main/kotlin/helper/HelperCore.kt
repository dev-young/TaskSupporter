package helper

import get
import kotlinx.coroutines.delay
import log
import moveMouseSmoothly
import org.jnativehook.keyboard.NativeKeyEvent
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import toMat
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.KeyEvent
import java.util.*
import kotlin.system.exitProcess


class HelperCore : Robot() {
    val random = Random()

    val dirPath = System.getProperty("user.dir") + "\\"

    var smartClickRange = 5
    var smartClickTimeMin = 50
    var smartClickTimeMax = 500
    var defaultClickKey = KeyEvent.BUTTON1_MASK //마우스 클릭시 KeyCode (BUTTON1_MASK == 왼쪽버튼)

    val defaultAccuracy = 10.0    // 0~100 이미지서치 정확도
    var searchedImgWidth = 0

    var searchedImgHeight = 0

    private fun setSearchedImgSize(width: Int = 0, height: Int = 0){
        searchedImgWidth = width
        searchedImgHeight = height
    }

    fun sendEnter(){
        // TODO: keyPress만 할 경우 계속 누르고 있는지 체크 필요하다.
        keyPress(KeyEvent.VK_ENTER)
        keyRelease(KeyEvent.VK_ENTER)
    }

    suspend fun simpleClick(x: Int, y: Int, count: Int = 1, keyCode:Int = defaultClickKey) {
        mouseMove(x, y)

        repeat(count){
            simpleClick(keyCode)
        }
//        log("mouseClicked: $x, $y / $count times / ${NativeKeyEvent.getKeyText(keyCode)}")
        kotlinx.coroutines.delay(1)
    }

    suspend fun simpleClick(point: Point, count: Int = 1, keyCode:Int = defaultClickKey) {
        simpleClick(point.x, point.y, count, keyCode)
    }

    suspend fun simpleClick(keyCode:Int = defaultClickKey) {
        kotlinx.coroutines.delay(80)
        mousePress(keyCode)
        kotlinx.coroutines.delay(10)
        mouseRelease(keyCode)
    }

    /**거리에 따라 동작 속도를 다르게 하여 마치  마치 사람이 클릭하는 것 처럼 동작하는 함수
     * @param randomRangeX 랜덤 클릭 X범위
     * @param randomRangeY 랜덤 클릭 Y범위
     * 랜덤클릭은 (x, y) ~ (x+randomRangeX, y+randomRangeY) 사이의 범위에서 이루어진다.
     * */
    suspend fun smartClick(x: Int, y: Int, randomRangeX: Int, randomRangeY: Int, minTime: Int, maxTime: Int, keyCode:Int = defaultClickKey) {
        val startPoint = getMousePos()
        val rx = (if (randomRangeX > 0) random.nextInt(randomRangeX) else 0) + x
        val ry = (if (randomRangeY > 0) random.nextInt(randomRangeY) else 0) + y
        val d = Point.distance(startPoint.x.toDouble(), startPoint.y.toDouble(), x.toDouble(), y.toDouble())
        val r = ((d / maxTime) * 100).toInt().let { random.get(it, it + 10) }
        val time = if (r < 100) minTime + ((maxTime - minTime) * r / 100) else maxTime
//        print("temp:$temp, 배율:$r%, ")
//        print("거리:$d, 시간:$time")
//        if(time > maxTime) time = maxTime.toDouble()
//        else if(time < minTime) time = minTime.toDouble()
//        log(", 적용시간:$time")

        moveMouseSmoothly(startPoint.x, startPoint.y, rx, ry, time)
        simpleClick(rx, ry, keyCode = keyCode)
    }

    suspend fun smartClick(point: Point, randomRangeX: Int, randomRangeY: Int, minTime: Int, maxTime: Int, keyCode:Int = defaultClickKey) {
        smartClick(point.x, point.y, randomRangeX, randomRangeY, minTime, maxTime, keyCode)
    }

    suspend fun smartClick(x: Int, y: Int, randomRangeX: Int, randomRangeY: Int, keyCode:Int = defaultClickKey) {
        smartClick(x, y, randomRangeX, randomRangeY, smartClickTimeMin, smartClickTimeMax, keyCode)
    }

    suspend fun smartClick(point: Point, randomRangeX: Int, randomRangeY: Int, keyCode:Int = defaultClickKey) {
        smartClick(point.x, point.y, randomRangeX, randomRangeY, keyCode)
    }

    suspend fun smartClick(x: Int, y: Int, keyCode:Int = defaultClickKey) {
        smartClick(x, y, smartClickRange, smartClickRange, smartClickTimeMin, smartClickTimeMax, keyCode)
    }

    suspend fun smartClick(point: Point, keyCode:Int = defaultClickKey) {
        smartClick(point.x, point.y, keyCode)
    }

    /**
     *@param accuracy 0~100 사이의 정확성 (100인 경우 정확히 일치하는것만 찾고 0인 경우에는 일치하지 않더라도 가장 비슷한 위치를 찾는다.) */
    fun imageSearch(leftTop: Point, width: Int, height: Int, imgName: String, accuracy: Double): Point? {
        var notFoundMMR = 1000000    // 적당한 값을 입력해야하는데 여러 수치를 테스트해본 결과 500000이 적당한듯 하다.
        notFoundMMR -= (notFoundMMR / 100 * accuracy).toInt()
        val bi = createScreenCapture(Rectangle(leftTop.x, leftTop.y, width, height))
        var source = bi.toMat()
        var template = Imgcodecs.imread(imgName)
        setSearchedImgSize(template.cols(), template.rows())

        val outputImage = Mat()
        val machMethod = Imgproc.TM_SQDIFF
        try {
            Imgproc.matchTemplate(source, template, outputImage, machMethod)
        } catch (e: Exception) {
//            print("[Error] 2" + e.message)
            log("사진 파일 확인 필요. error: ${e.message}")
            return null
        }

        val mmr = Core.minMaxLoc(outputImage)
//        log("정확도: " + mmr.minVal + ",  " + mmr.maxVal + ", notFoundMMR: " + notFoundMMR)
        if (accuracy > 0 && mmr.minVal > notFoundMMR) {
//            log("$imgName 찾기 실패")
            return null
        }

        val matchLoc: org.opencv.core.Point = mmr.minLoc
        return Point(leftTop.x + matchLoc.x.toInt(), leftTop.y + matchLoc.y.toInt())
    }

    fun imageSearch(leftTop: Point, width: Int, height: Int, imgName: String): Point? {
        return imageSearch(leftTop, width, height, imgName, defaultAccuracy)
    }

    fun imageSearch(imgName: String, accuracy: Double): Point? {
        return imageSearch(Point(0, 0), 1920, 1080, imgName, accuracy)
    }

    fun imageSearch(imgName: String): Point? {
        return imageSearch(imgName, defaultAccuracy)
    }

    /**이미지를 찾고 찾은 이미지 범위 내에서 클릭을 한다.
     * 이미지를 찾은경우 찾은 좌상단 좌표를 반환하고 못찾으면 null 반환*/
    suspend fun imageSearchAndClick(imgName: String, clickCount: Int = 1, minTime: Int = smartClickTimeMin, maxTime: Int = smartClickTimeMax, keyCode: Int = defaultClickKey): Point? {
        val point = imageSearch(imgName) ?: return null
        smartClick(point, searchedImgWidth, searchedImgHeight, minTime, maxTime, keyCode)
        return point
    }

    fun soundBeep() {
        TODO("Not yet implemented")
        log("beep!!")
    }

    fun showDialog(msg: String) {
        TODO("Not yet implemented")
        log("dialog: $msg")
    }

    fun copyToClipboard(str: String) {

    }

    fun paste() {
        TODO("Not yet implemented")
    }

    fun activeWindow(name: String) {
        TODO("Not yet implemented")
    }

    fun getMousePos(): Point {
        val pf = MouseInfo.getPointerInfo()
        return pf.location
    }

    fun exitApp() {
        exitProcess(0)
    }

    suspend fun delayRandom(min: Int, max: Int) {
        delay(random.get(min, max).toLong())
    }


    enum class HelperState {
        PLAY,
        PAUSE,
        EXIT
    }

}