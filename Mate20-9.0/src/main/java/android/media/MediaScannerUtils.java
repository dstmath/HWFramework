package android.media;

import android.os.RemoteException;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class MediaScannerUtils extends EasyInvokeUtils {
    private MethodObject<Void> prescan;
    private MethodObject<Void> processDirectory;

    public MethodObject<Void> getPrescan() {
        return this.prescan;
    }

    public void setPrescan(MethodObject<Void> prescan2) {
        this.prescan = prescan2;
    }

    public MethodObject<Void> getProcessDirectory() {
        return this.processDirectory;
    }

    public void setProcessDirectory(MethodObject<Void> processDirectory2) {
        this.processDirectory = processDirectory2;
    }

    @InvokeMethod(methodObject = "prescan")
    public void prescan(MediaScanner scanner, String filePath, boolean prescanFiles) throws RemoteException {
        invokeMethod(this.prescan, scanner, filePath, Boolean.valueOf(prescanFiles));
    }

    @InvokeMethod(methodObject = "processDirectory")
    public void processDirectory(MediaScanner scanner, String path, MediaScannerClient client) throws RemoteException {
        invokeMethod(this.processDirectory, scanner, path, client);
    }
}
