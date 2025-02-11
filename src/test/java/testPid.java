import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class testPid {

    @Test
    public void testPid() throws IOException, IllegalAccessException, NoSuchFieldException {
        Process p = Runtime.getRuntime().exec("ping www.baidu.com");
        Field f = p.getClass().getDeclaredField("handle");
        f.setAccessible(true);
        long handle = f.getLong(p);
        Kernel32 kernel = Kernel32.INSTANCE;
        WinNT.HANDLE winntHandle = new WinNT.HANDLE();
        winntHandle.setPointer(Pointer.createConstant(handle));
        int pid = kernel.GetProcessId(winntHandle);
        System.out.println("进程id="+pid);
        InputStream in = p.getInputStream();
        byte[] b = new byte[1024];
        int len = -1;
        try {
            while (true) {
                len = in.read(b);
                if(len>0){
                    System.out.print(new String(b, 0, len, "GBK"));
                }
                Thread.sleep(1000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
