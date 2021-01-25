package ohos.systemrestore.interfaces.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;
import java.util.Optional;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.startup.utils.StartUpStringUtil;
import ohos.systemrestore.ISystemRestoreProgressListener;
import ohos.systemrestore.SystemRestoreException;
import ohos.systemrestore.bean.SystemRestoreZipFilePropBean;
import ohos.systemrestore.interfaces.ISystemRestoreController;
import ohos.systemrestore.interfaces.ISystemRestoreSystemAbility;

public class SystemRestoreController implements ISystemRestoreController {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, SystemRestoreController.class.getSimpleName());
    private static ISystemRestoreController instance;
    private ISystemRestoreSystemAbility sysRestoreSystemAbility = SystemRestoreSystemAbilityStub.asInterface();

    private SystemRestoreController() {
    }

    public static ISystemRestoreController getInstance() {
        ISystemRestoreController iSystemRestoreController;
        synchronized (SystemRestoreController.class) {
            if (instance == null) {
                instance = new SystemRestoreController();
            }
            iSystemRestoreController = instance;
        }
        return iSystemRestoreController;
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreController
    public boolean rebootRestoreCache(Context context) throws SystemRestoreException {
        abilityCanUse(context);
        this.sysRestoreSystemAbility.passedAuthenticationRestoreCache(context);
        StartUpStringUtil.printDebug(TAG, "rebootRestoreCache passed authentication and then get commands.");
        Optional<String> rebootRestoreCacheCommands = getRebootRestoreCacheCommands(context);
        if (!rebootRestoreCacheCommands.isPresent()) {
            return false;
        }
        return this.sysRestoreSystemAbility.rebootRestoreCache(rebootRestoreCacheCommands.get());
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreController
    public boolean rebootRestoreUserData(Context context) throws SystemRestoreException {
        abilityCanUse(context);
        this.sysRestoreSystemAbility.passedAuthenticationRestoreUserData(context);
        StartUpStringUtil.printDebug(TAG, "rebootRestoreUserData passed authentication and then get commands.");
        Optional<String> rebootRestoreUserDataCommands = getRebootRestoreUserDataCommands(context);
        if (!rebootRestoreUserDataCommands.isPresent()) {
            return false;
        }
        this.sysRestoreSystemAbility.sendRestoreUserDataBroadcast(context);
        return this.sysRestoreSystemAbility.rebootRestoreUserData(rebootRestoreUserDataCommands.get());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        r2.addSuppressed(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0029, code lost:
        throw r3;
     */
    @Override // ohos.systemrestore.interfaces.ISystemRestoreController
    public void verifyUpdatePackage(File file, ISystemRestoreProgressListener iSystemRestoreProgressListener, File file2, boolean z) throws SystemRestoreException {
        if (file != null) {
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                SystemRestoreZipFilePropBean systemRestoreZipFilePropBean = new SystemRestoreZipFilePropBean(randomAccessFile);
                systemRestoreZipFilePropBean.getEOCDProperty();
                systemRestoreZipFilePropBean.setEndOfCentralDirectory();
                systemRestoreZipFilePropBean.verifiedCertsFile(file2);
                systemRestoreZipFilePropBean.readAndVerifyFile(iSystemRestoreProgressListener, z);
                randomAccessFile.close();
            } catch (FileNotFoundException e) {
                StartUpStringUtil.printException(TAG, e);
                throw new SystemRestoreException("The upgrade file to be verified not found.");
            } catch (IOException e2) {
                StartUpStringUtil.printException(TAG, e2);
                throw new SystemRestoreException(e2.getMessage());
            }
        } else {
            HiLog.error(TAG, "verifyUpdatePackage update file is null.", new Object[0]);
            throw new SystemRestoreException("update file is null.");
        }
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreController
    public void rebootAndInstallUpdatePackage(Context context, File file) throws SystemRestoreException {
        if (file != null) {
            abilityCanUse(context);
            this.sysRestoreSystemAbility.passedAuthenticationInstallPackage(context);
            StartUpStringUtil.printDebug(TAG, "installUpdatePackage passed authentication and then get commands.");
            Optional<String> installUpdatePackageCommands = getInstallUpdatePackageCommands(file);
            if (installUpdatePackageCommands.isPresent()) {
                this.sysRestoreSystemAbility.installUpdatePackageCommands(installUpdatePackageCommands.get());
                return;
            }
            return;
        }
        HiLog.error(TAG, "update package is null.", new Object[0]);
        throw new SystemRestoreException("update package is null.");
    }

    private void abilityCanUse(Context context) throws SystemRestoreException {
        if (context != null) {
            ISystemRestoreSystemAbility iSystemRestoreSystemAbility = this.sysRestoreSystemAbility;
            if (iSystemRestoreSystemAbility != null) {
                StartUpStringUtil.printDebug(TAG, "before exec in controller", iSystemRestoreSystemAbility.toString());
            } else {
                HiLog.error(TAG, "get mIMSAbility is null.", new Object[0]);
                throw new SystemRestoreException("get ability is null.");
            }
        } else {
            HiLog.error(TAG, "get context is null.", new Object[0]);
            throw new SystemRestoreException("get context is null.");
        }
    }

    private Optional<String> getRebootRestoreCacheCommands(Context context) throws SystemRestoreException {
        return getRebootRestoreCommands(context, "wipe_cache");
    }

    private Optional<String> getRebootRestoreUserDataCommands(Context context) throws SystemRestoreException {
        return getRebootRestoreCommands(context, "wipe_data");
    }

    private Optional<String> getRebootRestoreCommands(Context context, String str) throws SystemRestoreException {
        if (StartUpStringUtil.isEmpty(str)) {
            return Optional.empty();
        }
        StartUpStringUtil.printDebug(TAG, "getRebootRestoreCommands wipeType begin", str);
        if (context.getApplicationInfo() == null) {
            HiLog.error(TAG, "get applicationInfo is null.", new Object[0]);
            throw new SystemRestoreException("get applicationInfo is null.");
        } else if (!StartUpStringUtil.isEmpty(context.getApplicationInfo().getName())) {
            StartUpStringUtil.printDebug(TAG, "merge string before in controller");
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("--reason=");
            stringBuffer.append(context.getApplicationInfo().getName());
            stringBuffer.append(LINE_SEPARATOR);
            stringBuffer.append("--");
            stringBuffer.append(str);
            stringBuffer.append(LINE_SEPARATOR);
            stringBuffer.append("--locale=");
            stringBuffer.append(Locale.getDefault().toLanguageTag());
            stringBuffer.append(LINE_SEPARATOR);
            StartUpStringUtil.printDebug(TAG, "rebootRestoreCache merge string in controller", stringBuffer.toString().replace(LINE_SEPARATOR, "  "));
            return Optional.of(stringBuffer.toString());
        } else {
            HiLog.error(TAG, "get application bundleName is null.", new Object[0]);
            throw new SystemRestoreException("get application bundleName is null.");
        }
    }

    private Optional<String> getInstallUpdatePackageCommands(File file) throws SystemRestoreException {
        if (file != null) {
            StartUpStringUtil.printDebug(TAG, "getInstallUpdatePackageCommands begin.");
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("--update_package=");
            try {
                stringBuffer.append(file.getCanonicalPath());
                stringBuffer.append(LINE_SEPARATOR);
                stringBuffer.append("--locale=");
                stringBuffer.append(Locale.getDefault().toLanguageTag());
                stringBuffer.append(LINE_SEPARATOR);
                stringBuffer.append("--security");
                stringBuffer.append(LINE_SEPARATOR);
                StartUpStringUtil.printDebug(TAG, "getInstallUpdatePackageCommands merge string in controller", stringBuffer.toString().replace(LINE_SEPARATOR, "  "));
                return Optional.of(stringBuffer.toString());
            } catch (IOException e) {
                StartUpStringUtil.printException(TAG, e);
                throw new SystemRestoreException("update file io exception");
            }
        } else {
            HiLog.error(TAG, "update file is null.", new Object[0]);
            throw new SystemRestoreException("update file is null");
        }
    }
}
