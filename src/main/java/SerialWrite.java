import java.io.*;

public class SerialWrite implements Runnable {
    OutputStream out;

    public SerialWrite(OutputStream out) {
        this.out = out;
    }

    @Override
    public void run() {
        String s;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (!(s = br.readLine()).isEmpty()) {
                out.write(s.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
