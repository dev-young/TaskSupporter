package helper

import JSerial
import winActive
import com.sun.jna.platform.win32.User32
import get
import getHeight
import kotlinx.coroutines.delay
import leftTop
import logI
import moveMouseSmoothly
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import toMat
import width
import winGetPos
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.util.*
import kotlin.math.abs
import kotlin.system.exitProcess


class HelperCore(var useHWKey:Boolean = false) : Robot() {
    companion object {
        val serial by lazy { Serial() }
    }
    val random = Random()

    val user32 = User32.INSTANCE

    val dirPath = System.getProperty("user.dir") + "\\"

    var smartClickRange = 5
    var smartClickTimeMin = 50
    var smartClickTimeMax = 500
    var smartClickWaitingDelay = 10 //스마트 클릭시 해당 위치로 마우스 이동후 몇 ms동안 멈춰있다가 클릭할지 지정
    var clickDuration = 15L   //클릭시 눌렀다 때는데 걸리는 시간 (ms)
    var defaultClickKey = KeyEvent.BUTTON1_MASK //마우스 클릭시 KeyCode (BUTTON1_MASK == 왼쪽버튼, BUTTON3_MASK == 오쪽버튼)

    val defaultAccuracy = 10.0    // 0~100 이미지서치 정확도
    var searchedImgWidth = 0    //가장 최근에 검색된 이미지의 넓이
    var searchedImgHeight = 0   //가장 최근에 검색된 이미지의 높이

    val toolkit = Toolkit.getDefaultToolkit()

    val pressedKeySet = hashSetOf<Int>()

    override fun keyPress(keycode: Int) {
        if(useHWKey && serial.isConnected){
            if(keycode > 127)
                serial.out.write(("kp+"+(keycode-128).toChar()).toByteArray())
            else
                serial.out.write(("kp"+keycode.toChar()).toByteArray())
            serial.out.flush()
        } else
            super.keyPress(keycode)
        pressedKeySet.add(keycode)
    }

    override fun keyRelease(keycode: Int) {
        if(useHWKey && serial.isConnected){
            if(keycode > 127)
                serial.out.write(("kr+"+(keycode-128).toChar()).toByteArray())
            else
                serial.out.write(("kr"+keycode.toChar()).toByteArray())

            serial.out.flush()
        } else
            super.keyRelease(keycode)
        pressedKeySet.remove(keycode)
    }

    fun releaseAll(){
        if(useHWKey && serial.isConnected){
            serial.out.write('k'.toInt())
            serial.out.flush()
        } else
            pressedKeySet.forEach { super.keyRelease(it) }
        pressedKeySet.clear()
    }

    private fun setSearchedImgSize(width: Int = 0, height: Int = 0) {
        searchedImgWidth = width
        searchedImgHeight = height
    }

    fun sendEnter() {
        // TODO: keyPress만 할 경우 계속 누르고 있는지 체크 필요하다.
        keyPress(VK_ENTER)
        keyRelease(VK_ENTER)
    }

    fun send(keyCode: Int) {
        keyPress(keyCode)
        keyRelease(keyCode)
    }

    suspend fun simpleClick(x: Int, y: Int, count: Int = 1, keyCode: Int = defaultClickKey) {
        mouseMove(x, y)

        repeat(count) {
            simpleClick(keyCode)
        }
//        log("mouseClicked: $x, $y / $count times / ${NativeKeyEvent.getKeyText(keyCode)}")
        kotlinx.coroutines.delay(1)
    }

    suspend fun simpleClick(point: Point, count: Int = 1, keyCode: Int = defaultClickKey) {
        simpleClick(point.x, point.y, count, keyCode)
    }

    suspend fun simpleClick(keyCode: Int = defaultClickKey) {
        kotlinx.coroutines.delay(80)
        mousePress(keyCode)
        kotlinx.coroutines.delay(clickDuration)
        mouseRelease(keyCode)
    }

    /**거리에 따라 동작 속도를 다르게 하여 마치  마치 사람이 클릭하는 것 처럼 동작하는 함수
     * @param randomRangeX 랜덤 클릭 X범위
     * @param randomRangeY 랜덤 클릭 Y범위
     * 랜덤클릭은 (x, y) ~ (x+randomRangeX, y+randomRangeY) 사이의 범위에서 이루어진다.
     * */
    suspend fun smartClick(
        x: Int,
        y: Int,
        randomRangeX: Int,
        randomRangeY: Int,
        minTime: Int,
        maxTime: Int,
        keyCode: Int = defaultClickKey
    ) {
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
        delay(smartClickWaitingDelay)
        simpleClick(rx, ry, keyCode = keyCode)
    }

    suspend fun smartClick(
        point: Point,
        randomRangeX: Int,
        randomRangeY: Int,
        minTime: Int = smartClickTimeMin,
        maxTime: Int = smartClickTimeMax,
        keyCode: Int = defaultClickKey
    ) {
        smartClick(point.x, point.y, randomRangeX, randomRangeY, minTime, maxTime, keyCode)
    }

    suspend fun smartClick(x: Int, y: Int, randomRangeX: Int, randomRangeY: Int, keyCode: Int = defaultClickKey) {
        smartClick(x, y, randomRangeX, randomRangeY, smartClickTimeMin, smartClickTimeMax, keyCode)
    }

    suspend fun smartClick(point: Point, randomRangeX: Int, randomRangeY: Int, keyCode: Int = defaultClickKey) {
        smartClick(point.x, point.y, randomRangeX, randomRangeY, keyCode)
    }

    suspend fun smartClick(x: Int, y: Int, keyCode: Int = defaultClickKey) {
        smartClick(x, y, smartClickRange, smartClickRange, smartClickTimeMin, smartClickTimeMax, keyCode)
    }

    suspend fun smartClick(point: Point, keyCode: Int = defaultClickKey) {
        smartClick(point.x, point.y, keyCode)
    }

    fun imageSearchReturnBoolean(source:Mat, template:Mat, accuracy: Double = 50.0, notFoundMMR_:Int = 1000000):Boolean {
        var notFoundMMR = notFoundMMR_    // 적당한 값을 입력해야하는데 여러 수치를 테스트해본 결과 500000이 적당한듯 하다.
        notFoundMMR -= (notFoundMMR / 100 * accuracy).toInt()
        val outputImage = Mat()
        Imgproc.matchTemplate(source, template, outputImage, Imgproc.TM_SQDIFF)
        try {

        } catch (e: Exception) {
//            print("[Error] 2" + e.message)
//            logI("사진 파일 확인 필요. error: ${e.message}")
            logI("오류 발생. (예상 원인: 이미지 파일 없음) ${e.message}")
            return false
        }

        val mmr = Core.minMaxLoc(outputImage)
//        log("정확도: " + mmr.minVal + ",  " + mmr.maxVal + ", notFoundMMR: " + notFoundMMR)
        if (accuracy > 0 && mmr.minVal > notFoundMMR) {
            return false
        }

//        val matchLoc: org.opencv.core.Point = mmr.minLoc
//        return Point(leftTop.x + matchLoc.x.toInt(), leftTop.y + matchLoc.y.toInt())
        return true
    }

    fun imageSearch(source:Mat, template:Mat, accuracy: Double = 50.0, notFoundMMR_:Int = 1000000):Point? {
        var notFoundMMR = notFoundMMR_    // 적당한 값을 입력해야하는데 여러 수치를 테스트해본 결과 500000이 적당한듯 하다.
        notFoundMMR -= (notFoundMMR / 100 * accuracy).toInt()
        val outputImage = Mat()
        try {
            Imgproc.matchTemplate(source, template, outputImage, Imgproc.TM_SQDIFF)
        } catch (e: Exception) {
//            print("[Error] 2" + e.message)
//            logI("사진 파일 확인 필요. error: ${e.message}")
            logI("오류 발생. (예상 원인: 이미지 파일 없음")
            return null
        }

        val mmr = Core.minMaxLoc(outputImage)
//        log("정확도: " + mmr.minVal + ",  " + mmr.maxVal + ", notFoundMMR: " + notFoundMMR)
        if (accuracy > 0 && mmr.minVal > notFoundMMR) {
            return null
        }

        val matchLoc: org.opencv.core.Point = mmr.minLoc
        return Point(matchLoc.x.toInt(), matchLoc.y.toInt())
    }

    /**
     *@param accuracy 0~100 사이의 정확성 (100인 경우 정확히 일치하는것만 찾고 0인 경우에는 일치하지 않더라도 가장 비슷한 위치를 찾는다.) */
    fun imageSearch(leftTop: Point, width: Int, height: Int, template: Mat, accuracy: Double): Point? {
        if(width == 0 || height == 0){
            return null
        }
        var notFoundMMR = 1000000    // 적당한 값을 입력해야하는데 여러 수치를 테스트해본 결과 500000이 적당한듯 하다.
        notFoundMMR -= (notFoundMMR / 100 * accuracy).toInt()
        val bi = createScreenCapture(Rectangle(leftTop.x, leftTop.y, width, height))
        var source = bi.toMat()
        setSearchedImgSize(template.cols(), template.rows())

        val outputImage = Mat()
        val machMethod = Imgproc.TM_SQDIFF
        try {
            Imgproc.matchTemplate(source, template, outputImage, machMethod)
        } catch (e: Exception) {
            logI("error: ${e.message}")
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

    fun imageSearch(leftTop: Point, width: Int, height: Int, imgName: String, accuracy: Double): Point? {
        var template = Imgcodecs.imread(imgName)
        return imageSearch(leftTop, width, height, template, accuracy)
    }

    fun imageSearch(leftTop: Point, width: Int, height: Int, imgName: String): Point? {
        return imageSearch(leftTop, width, height, imgName, defaultAccuracy)
    }

    /**@param findOnForeground 활성화된 윈도우 범위 내에서만 찾을지 여부*/
    fun imageSearch(template: Mat, accuracy: Double, findOnForeground : Boolean = true): Point? {
        return if(findOnForeground){
            val rect = user32.winGetPos()
            imageSearch(rect.leftTop(), rect.width(), rect.getHeight(), template, accuracy)
        } else {
            imageSearch(Point(0, 0), 1920, 1080, template, accuracy)
        }
    }

    fun imageSearch(imgName: String, accuracy: Double, findOnForeground : Boolean = true): Point? {
        val template = Imgcodecs.imread(imgName)
        return imageSearch(template, accuracy, findOnForeground)
    }

    fun imageSearch(imgName: String, findOnForeground : Boolean = true): Point? {
        return imageSearch(imgName, defaultAccuracy, findOnForeground)
    }

    /**이미지를 찾을때까지 반복하여 검색 (기본 5초간 검색)*/
    suspend fun imageSearchUntilFind(
        imgName: String,
        accuracy: Double = defaultAccuracy,
        repeatDelay: Int = 250,
        repeatCount: Int = 20
    ): Point? {
        val template = Imgcodecs.imread(imgName)
        var point = imageSearch(template, accuracy)
        val delay = repeatDelay.toLong()
        var tryCount = 0
        while (point == null){
            tryCount++
            point = imageSearch(template, accuracy)
            delay(delay)
            if(tryCount > repeatCount) return null
        }
        return point
    }


    /**이미지를 찾고 찾은 이미지 범위 내에서 클릭을 한다.
     * 이미지를 못찾은경우 찾을때까지 계속 반복한다. (최대 몇초간 반복할지 설정 가능)
     * 이미지를 찾은경우 찾은 좌상단 좌표를 반환하고 못찾으면 null 반환*/
    suspend fun imageSearchAndClickUntilFind(
        imgName: String,
        accuracy: Double = defaultAccuracy,
        minTime: Int = smartClickTimeMin,
        maxTime: Int = smartClickTimeMax,
        keyCode: Int = defaultClickKey,
        repeatDelay: Int = 250,
        repeatCount: Int = 20
    ): Point? {
        val point = imageSearchUntilFind(imgName, accuracy, repeatDelay, repeatCount)
        point?.let { smartClick(it, searchedImgWidth, searchedImgHeight, minTime, maxTime, keyCode) }
        return point
    }

    /**이미지를 찾고 찾은 이미지 범위 내에서 클릭을 한다.
     * 이미지를 찾은경우 찾은 좌상단 좌표를 반환하고 못찾으면 null 반환*/
    suspend fun imageSearchAndClick(
        imgName: String,
        accuracy: Double = defaultAccuracy,
        minTime: Int = smartClickTimeMin,
        maxTime: Int = smartClickTimeMax,
        keyCode: Int = defaultClickKey
    ): Point? {
        val point = imageSearch(imgName, accuracy) ?: return null
        smartClick(point, searchedImgWidth, searchedImgHeight, minTime, maxTime, keyCode)
        return point
    }

    /**이미지를 찾고 찾은 이미지 범위 내에서 클릭을 한다.
     * 이미지를 찾은경우 찾은 좌상단 좌표를 반환하고 못찾으면 null 반환*/
    suspend fun imageSearchAndClick(
        startPoint: Point,
        width: Int,
        height: Int,
        imgName: String,
        accuracy: Double = defaultAccuracy,
        minTime: Int = smartClickTimeMin,
        maxTime: Int = smartClickTimeMax,
        keyCode: Int = defaultClickKey
    ): Point? {
        val point = imageSearch(startPoint, width, height, imgName, accuracy) ?: return null
        smartClick(point, searchedImgWidth, searchedImgHeight, minTime, maxTime, keyCode)
        return point
    }

    fun soundBeep() {
        toolkit.beep()
    }

    fun showDialog(msg: String) {
        TODO("Not yet implemented")
        logI("dialog: $msg")
    }

    fun getStringFromClipboard() : String {
        val data = toolkit.systemClipboard.getContents(this)
        return try {
            data.getTransferData(DataFlavor.stringFlavor) as String
        } catch(e:Exception) {
            ""
        }
    }

    suspend fun copyToClipboard(str: String) {
        val selection = StringSelection(str)
        val clipboard: Clipboard = toolkit.systemClipboard
        var tryCount = 1
        clipboard.setContents(selection, selection)
        while(getStringFromClipboard() != str) {
            tryCount++
            delay(100L)

            if(tryCount > 100){
                logI("$str 을 클립보드에 복사하는데 실패했습니다.")
                return
            }
        }
//        logI("복사된 내용:${getStringFromClipboard()}, 대상:$str")

    }

    // Ctrl+V
    fun paste() {
        keyPress(VK_CONTROL)
        delay(20)
        keyPress(VK_V)
        delay(20)
        keyRelease(VK_CONTROL)
    }

    fun activeWindow(name: String) {
        user32.winActive(name)
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

    suspend fun smartDrag(from: Point, to: Point, keyCode: Int = defaultClickKey) {
        moveMouseSmoothly(from, 100)
        delay(150L)
        mousePress(keyCode)
        delay(250L)
        moveMouseSmoothly(to, random.get(100, 200))
        delay(250L)
        mouseRelease(keyCode)
    }

    /**현재 활성화된 윈도우 내에서 상대좌표로 이동 */
    suspend fun moveMouseOnForeground(point:Point){
        point.apply {
            val rect = user32.winGetPos()
            x += rect.left
            y += rect.top
        }
        moveMouseSmoothly(point, 100)
    }

    suspend fun moveMouseOnForeground(x: Int, y: Int){
        val rect = user32.winGetPos()
        moveMouseSmoothly(Point(x+rect.left, y+rect.top), 100)
    }

    suspend fun mouseWheelSmoothly(move:Int, delay:Long = 50) {
        var direction = 1
        if (move < 0) direction = -1
        for (i in 1..abs(move)) {
            mouseWheel(direction)
            delay(delay)
        }
    }


    enum class HelperState {
        PLAY,
        PAUSE,
        EXIT
    }

}