import java.io.IOException;
import java.io.InputStream;

public class SerialRead implements Runnable {
    InputStream in;

    public SerialRead(InputStream in){this.in = in;}

    @Override
    public void run()
    {
        byte[] buffer = new byte[1024];
        int len = -1;

        try
        {
            String cmd = "";
            //	buffer에 저장하고나서, 그 길이를 반환한다.
            while ((len = this.in.read(buffer)) > -1)
            {
                //	System.out.println(new String(buffer,0,len));
                //	new DataProc(new String(buffer,0,len));
                String s = new String(buffer,0,len);

                if (len != 0){
                    cmd = cmd + s;
                    if (s.endsWith("\n")){
                        if(cmd.length()>4){
                            int c = cmd.charAt(3);
                            System.out.print("("+c+")");
                        }
                        System.out.print(cmd);
                        cmd = "";
                    }


                }
            }
        }
        catch (IOException e) {e.printStackTrace();}
    }}
