import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.File;
import java.io.OutputStream;

public class JSerial {

    public OutputStream out;
    public void connect(String port) {
        CommPort commPort;
        SerialPort serialPort = null;
        System.load(new File("").getAbsolutePath() + "\\libs\\rxtxSerial.dll");

        try {
            CommPortIdentifier com = CommPortIdentifier.getPortIdentifier(port);

            //	com포트를 확인하는 작업
            if (com.isCurrentlyOwned())
                System.out.println("Error : " + port + "포트를 사용중입니다.");

                //	포트가 열려있으면
            else {
                commPort = com.open(this.getClass().getName(), 2000);

                //	획득한 포트를 객체가 사용할 수 있는지 여부 확인
                if (commPort instanceof SerialPort)    //	commPort가 SerialPort로 사용할 수 있는지 확인
                {
                    serialPort = (SerialPort) commPort;

                    //	정상적으로 포트를 사용할 수 있을 경우
                    //	포트에 필요한 정보를 입력해 준다.
                    serialPort.setSerialPortParams(
                            9600,                        //	바운드레이트
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);    //	오류제어 비트
                }
                System.out.println("comport성공");
                out = serialPort.getOutputStream();

            }
        }    //	end try
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
