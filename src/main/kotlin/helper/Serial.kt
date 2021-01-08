package helper

import SerialRead
import gnu.io.CommPort
import gnu.io.CommPortIdentifier
import gnu.io.SerialPort
import java.io.File
import java.io.OutputStream

class Serial {
    init {
//        System.load(File("").absolutePath + "\\rxtxSerial.dll")dhrt
//        System.load(File("").absolutePath + "\\libs\\rxtxSerial.dll")
//        System.load(File("").absolutePath + "\\libs\\rxtxParallel.dll")
    }
    lateinit var out: OutputStream
    var isConnected = false
    fun connect(port: String) : Boolean {
        val commPort: CommPort
        var serialPort: SerialPort
        try {
            val com = CommPortIdentifier.getPortIdentifier(port)

            //	com포트를 확인하는 작업
            if (com.isCurrentlyOwned) println("Error : " + port + "포트를 사용중입니다.") else {
                commPort = com.open(this.javaClass.name, 2000)

                //	획득한 포트를 객체가 사용할 수 있는지 여부 확인
                //	commPort가 SerialPort로 사용할 수 있는지 확인
                if (commPort is SerialPort) {
                    serialPort = commPort
                    out = serialPort.outputStream
                    //	정상적으로 포트를 사용할 수 있을 경우
                    //	포트에 필요한 정보를 입력해 준다.
                    serialPort.setSerialPortParams(
                        9600,  //	바운드레이트
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE
                    ) //	오류제어 비트

//                    val input = serialPort.inputStream
//                    Thread(SerialRead(input)).start()
                }
                println("comport성공")
                isConnected = true
                return true



            }
        } //	end try
        catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}