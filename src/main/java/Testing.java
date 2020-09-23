import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.PointerByReference;

public class Testing {
    private static final int MAX_TITLE_LENGTH = 1024;

    public static void main(String[] args) throws Exception {
        Thread.sleep(1000);
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];

        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
        System.out.println("Active window title: " + Native.toString(buffer));
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        System.out.println("rect = " + rect);

//        String s = "!@#";
//        System.out.println(s, "%s test");

//        PointerByReference pointer = new PointerByReference();
//        User32DLL.GetWindowThreadProcessId(User32DLL.GetForegroundWindow(), pointer);
//        Pointer process = Kernel32.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ, false, pointer.getValue());
//        Psapi.GetModuleBaseNameW(process, null, buffer, MAX_TITLE_LENGTH);
//        System.out.println("Active window process: " + Native.toString(buffer));
    }

}
