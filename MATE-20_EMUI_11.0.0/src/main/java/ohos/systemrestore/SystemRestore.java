package ohos.systemrestore;

import java.io.File;
import ohos.app.Context;
import ohos.systemrestore.interfaces.impl.SystemRestoreController;

public class SystemRestore {
    public static boolean rebootAndCleanCache(Context context) throws SystemRestoreException {
        return SystemRestoreController.getInstance().rebootRestoreCache(context);
    }

    public static boolean rebootAndCleanUserData(Context context) throws SystemRestoreException {
        return SystemRestoreController.getInstance().rebootRestoreUserData(context);
    }

    public static void verifyUpgradePackage(File file, ISystemRestoreProgressListener iSystemRestoreProgressListener, File file2) throws SystemRestoreException {
        SystemRestoreController.getInstance().verifyUpdatePackage(file, iSystemRestoreProgressListener, file2, false);
    }

    public static void rebootAndInstallUpgradePackage(Context context, File file) throws SystemRestoreException {
        SystemRestoreController.getInstance().rebootAndInstallUpdatePackage(context, file);
    }
}
