import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyEvent.getKeyText
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.awt.AWTException
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Robot
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

fun NativeKeyEvent.print() {
    //        System.out.println(keyCode + " " + keyChar);
    val text = getKeyText(keyCode)
    println(text)
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
    println("A screenshot is captured to: " + file.path)
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

fun Robot.moveMouseSmoothly(point: Point) {
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
                Thread.sleep(dt.toLong())
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