package android.media;

import android.os.RemoteException;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class MediaScannerUtils extends EasyInvokeUtils {
    MethodObject<Void> prescan;
    MethodObject<Void> processDirectory;

    @InvokeMethod(methodObject = "prescan")
    public void prescan(MediaScanner scanner, String filePath, boolean prescanFiles) throws RemoteException {
        invokeMethod(this.prescan, scanner, filePath, Boolean.valueOf(prescanFiles));
    }

    @InvokeMethod(methodObject = "processDirectory")
    public void processDirectory(MediaScanner scanner, String path, MediaScannerClient client) throws RemoteException {
        invokeMethod(this.processDirectory, scanner, path, client);
    }
}
