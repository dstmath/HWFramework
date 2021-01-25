package ohos.systemrestore.interfaces;

import java.io.File;
import ohos.app.Context;
import ohos.systemrestore.ISystemRestoreProgressListener;
import ohos.systemrestore.SystemRestoreException;

public interface ISystemRestoreController {
    void rebootAndInstallUpdatePackage(Context context, File file) throws SystemRestoreException;

    boolean rebootRestoreCache(Context context) throws SystemRestoreException;

    boolean rebootRestoreUserData(Context context) throws SystemRestoreException;

    void verifyUpdatePackage(File file, ISystemRestoreProgressListener iSystemRestoreProgressListener, File file2, boolean z) throws SystemRestoreException;
}
