package ohos.systemrestore.innerkits;

import java.io.File;
import ohos.app.Context;
import ohos.systemrestore.ISystemRestoreProgressListener;
import ohos.systemrestore.SystemRestoreException;
import ohos.systemrestore.interfaces.impl.SystemRestoreController;

public final class SystemRestoreInnerKit {
    public static boolean rebootAndCleanCache(Context context) throws SystemRestoreException {
        return SystemRestoreController.getInstance().rebootRestoreCache(context);
    }

    public static boolean rebootAndCleanUserData(Context context) throws SystemRestoreException {
        return SystemRestoreController.getInstance().rebootRestoreUserData(context);
    }

    public static void verifyUpdatePackage(File file, ISystemRestoreProgressListener iSystemRestoreProgressListener, File file2) throws SystemRestoreException {
        SystemRestoreController.getInstance().verifyUpdatePackage(file, iSystemRestoreProgressListener, file2, false);
    }

    public static void rebootAndInstallUpdatePackage(Context context, File file) throws SystemRestoreException {
        SystemRestoreController.getInstance().rebootAndInstallUpdatePackage(context, file);
    }
}
