import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import javafx.application.Platform
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyEvent.getKeyText
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.awt.AWTException
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Robot
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

//val dateFormat = SimpleDateFormat("HH:mm:ss MM.dd")
val dateFormat = SimpleDateFormat("HH:mm:ss")

fun logI(message: Any?){
    val msg = "[${dateFormat.format(Date())}] $message"
    println(msg)
    Platform.runLater {
        MainView.logList.add(msg)
        MainView.lastLogLabel.text = msg
    }

}

fun NativeKeyEvent.print() {
    //        System.out.log(keyCode + " " + keyChar);
    val text = getKeyText(keyCode)
    logI(text)
}

fun BufferedImage.toMat(): Mat {

    val cast = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
    cast.let {
        it.graphics.drawImage(this, 0, 0, null)
    }

    val mat = Mat(cast.height, cast.width, CvType.CV_8UC3)
    val data = (cast.raster.dataBuffer as DataBufferByte).data
    mat.put(0, 0, data)
    return mat
}

fun BufferedImage.toFile(fileName: String) {
    val file = File("$fileName.png")
    ImageIO.write(this, "png", file)
    logI("A screenshot is captured to: " + file.path)
}

/**대비 증가시키기*/
fun Mat.changeContract(){
    convertTo(this, -1, 1.5, -110.0)
}

fun Mat.changeContract2(){
    convertTo(this,-1, 1.0, -180.0)
    convertTo(this,-1, 2.0, 0.0)
}

//흑과 백으로만 변경 (반투명한 배경위의 글씨를 읽을때 유용)
fun Mat.changeBlackAndWhite(){
    convertTo(this, -1, 2.0, -100.0)
    Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2GRAY)
    convertTo(this, -1, 10.0, 0.0)
}

fun Robot.moveMouseSmoothly(x1: Int, y1: Int, x2: Int, y2: Int, t: Int) {
    //t의 60% 정도로 n을 사용해야 t만큼의 시간동안 마우스가 움직인다.
    moveMouseSmoothly(x1, y1, x2, y2, t, (t * 0.6).toInt())
}

fun Robot.moveMouseSmoothly(point: Point, t: Int) {
    //t의 60% 정도로 n을 사용해야 t만큼의 시간동안 마우스가 움직인다.
    val pf = MouseInfo.getPointerInfo()
    moveMouseSmoothly(pf.location.x, pf.location.y, point.x, point.y, t, (t * 0.6).toInt())
}

suspend fun Robot.moveMouseSmoothly(point: Point) {
    val pf = MouseInfo.getPointerInfo()
    moveMouseSmoothly(pf.location.x, pf.location.y, point.x, point.y, 100, 60)
}

fun Robot.moveMouseSmoothly(x1: Int, y1: Int, x2: Int, y2: Int, t: Int, n: Int) {
    try {
        val dx = (x2 - x1) / n.toDouble()
        val dy = (y2 - y1) / n.toDouble()
        val dt = t / n.toDouble()

        val taskTime = measureTimeMillis {
            mouseMove((x1 + dx).toInt(), (y1 + dy).toInt())
            for (step in 2..n) {
//                kotlinx.coroutines.delay(dt.toLong())
                delay(dt.toInt())
                mouseMove((x1 + dx * step).toInt(), (y1 + dy * step).toInt())
            }
        }
//        print("이동에 걸린 시간: $taskTime")

    } catch (e: AWTException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}


fun Random.get(from: Int, to: Int): Int {
    val range = to - from
    if (range > 0)
        return from + nextInt(range)
    return from
}

val MAX_TITLE_LENGTH = 1024


fun User32.winExist(title: String): WinDef.HWND? {
    return FindWindow(null, title)
}

fun User32.winIsForeground(title: String): Boolean {
    val target = CharArray(MAX_TITLE_LENGTH * 2)
    GetWindowText(GetForegroundWindow(), target, MAX_TITLE_LENGTH)
    return title == Native.toString(target)
}

/**윈도우 활성화 (활성화가 완료될때까지 기다린다. 최대 2초)
 * @return 활성화에 실패하면 false 반환 */
fun User32.winActive(hwnd : WinDef.HWND): Boolean {

    val target = CharArray(MAX_TITLE_LENGTH * 2)
    GetWindowText(hwnd, target, MAX_TITLE_LENGTH)

    val current = CharArray(MAX_TITLE_LENGTH * 2)
    for(i in 1..200) {
        SetForegroundWindow(hwnd)
//            logI("not yet activate   current:${Native.toString(current)}")
        Thread.sleep(10)
        GetWindowText(GetForegroundWindow(), current, MAX_TITLE_LENGTH)

        if(target.contentEquals(current))
            return true
    }
    return false
}

fun User32.winActive(title : String): Boolean {
    val hwnd = FindWindow(null, title) ?: return false

    return winActive(hwnd)
}

fun User32.winMove( point: Point, title : String? = null, hwnd_: WinDef.HWND? = null)  {
    var hwnd = hwnd_

    if(title != null){
        hwnd = FindWindow(null, title)
    }

    if(hwnd == null)
        hwnd = GetForegroundWindow()

    val rect = WinDef.RECT()
    GetWindowRect(hwnd, rect)
    val w = rect.right - rect.left
    val h = rect.bottom - rect.top
    MoveWindow(hwnd, point.x, point.y, w, h, false)
}

fun User32.winGetPos(hwnd: WinDef.HWND): WinDef.RECT {
    val rect = WinDef.RECT()
    GetWindowRect(hwnd, rect)
    return rect
}

fun User32.winGetPos(): WinDef.RECT {
    val rect = WinDef.RECT()
    GetWindowRect(GetForegroundWindow(), rect)
    return rect
}

fun WinDef.RECT.leftTop() = Point(left, top)
fun WinDef.RECT.rightBottom() = Point(right, bottom)
fun WinDef.RECT.width() = right - left
fun WinDef.RECT.getHeight() = bottom - top
