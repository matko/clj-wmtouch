import com.sun.jna.*;
import com.sun.jna.win32.*;
import com.sun.jna.platform.win32.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CWPStruct extends Structure {
    public WinDef.LPARAM lparam;
    public WinDef.WPARAM wparam;
    public int msg;
    public WinDef.HWND handle;

    protected List getFieldOrder() {
	return Arrays.asList(new String[] { "lparam", "wparam", "msg", "handle" });
    }
}
