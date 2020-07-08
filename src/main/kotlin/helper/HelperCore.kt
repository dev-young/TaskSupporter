package helper

import get
import moveMouseSmoothly
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
import java.io.File
import java.util.*
import kotlin.system.exitProcess


class HelperCore : Robot() {
    var smartClickRange = 5
    var smartClickTimeMin = 50
    var smartClickTimeMax = 500
    val random = Random()
    val dirPath = System.getProperty("user.dir") + "\\"
    val defaultAccuracy = 10    // 0~100 이미지서치 정확도

    fun simpleClick(x: Int, y: Int, count: Int = 1) {
        mouseMove(x, y)

        for (i in 1..count) {
            delay(80)
            mousePress(KeyEvent.BUTTON1_MASK)
            delay(10)
            mouseRelease(KeyEvent.BUTTON1_MASK)
        }
    }

    fun simpleClick(point: Point, count: Int = 1) {
        simpleClick(point.x, point.y, count)
    }

    /**거리에 따라 동작 속도를 다르게 하여 마치  마치 사람이 클릭하는 것 처럼 동작하는 함수
     * @param randomRangeX 랜덤 클릭 X범위
     * @param randomRangeY 랜덤 클릭 Y범위
     * 랜덤클릭은 (x, y) ~ (x+randomRangeX, y+randomRangeY) 사이의 범위에서 이루어진다.
     * */
    fun smartClick(x: Int, y: Int, randomRangeX: Int, randomRangeY: Int, minTime: Int, maxTime: Int) {
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
//        println(", 적용시간:$time")

        moveMouseSmoothly(startPoint.x, startPoint.y, rx, ry, time)
        simpleClick(rx, ry)
    }

    fun smartClick(point: Point, randomRangeX: Int, randomRangeY: Int, minTime: Int, maxTime: Int) {
        smartClick(point.x, point.y, randomRangeX, randomRangeY, minTime, maxTime)
    }

    fun smartClick(x: Int, y: Int, randomRangeX: Int, randomRangeY: Int) {
        smartClick(x, y, randomRangeX, randomRangeY, smartClickTimeMin, smartClickTimeMax)
    }

    fun smartClick(point: Point, randomRangeX: Int, randomRangeY: Int) {
        smartClick(point.x, point.y, randomRangeX, randomRangeY)
    }

    fun smartClick(x: Int, y: Int) {
        smartClick(x, y, 0, 0, smartClickTimeMin, smartClickTimeMax)
    }

    fun smartClick(point: Point) {
        smartClick(point.x, point.y)
    }

    /**
     *@param accuracy 0~100 사이의 정확성 (100인 경우 정확히 일치하는것만 찾는다) */
    fun imageSearch(leftTop: Point, width: Int, height: Int, imgName: String, accuracy: Int): Point? {
        var notFoundMMR = 1000000    // 적당한 값을 입력해야하는데 여러 수치를 테스트해본 결과 500000이 적당한듯 하다.
        notFoundMMR -= (notFoundMMR / 100 * accuracy)
        val bi = createScreenCapture(Rectangle(leftTop.x, leftTop.y, width, height))
        var source = bi.toMat()
        var template = Imgcodecs.imread(imgName)

        val outputImage = Mat()
        val machMethod = Imgproc.TM_SQDIFF
        try {
            Imgproc.matchTemplate(source, template, outputImage, machMethod)
        } catch (e: Exception) {
            print("[Error] 2" + e.message)
            println("사진 파일 확인 필요.")
            return null
        }

        val mmr = Core.minMaxLoc(outputImage)
//        println("정확도: " + mmr.minVal + ",  " + mmr.maxVal)
        if (mmr.minVal > notFoundMMR) {
//            println("이미지 찾기 실패")
            return null
        }

        val matchLoc: org.opencv.core.Point = mmr.minLoc
        return Point(leftTop.x + matchLoc.x.toInt(), leftTop.y + matchLoc.y.toInt())
    }

    fun imageSearch(leftTop: Point, width: Int, height: Int, imgName: String): Point? {
        return imageSearch(leftTop, width, height, imgName, defaultAccuracy)
    }

    fun imageSearch(imgName: String): Point? {
        return imageSearch(Point(0, 0), 1920, 1080, imgName, defaultAccuracy)
    }

    fun soundBeep() {
        // TODO: 기능 추가
        println("beep!!")
    }

    fun showDialog(msg: String) {
        // TODO: 기능 추가
        println("dialog: $msg")
    }

    fun copyToClipboard(str: String) {

    }

    fun paste() {
        // TODO: 기능 추가
    }

    fun activeWindow(name: String) {
        // TODO: 기능 추가
    }

    fun getMousePos(): Point {
        val pf = MouseInfo.getPointerInfo()
        return pf.location
    }

    fun exitApp() {
        exitProcess(0)
    }

    fun delayRandom(min: Int, max: Int) {
        delay(random.get(min, max))
    }


    enum class HelperState {
        PLAY,
        PAUSE,
        EXIT
    }

}