package ohos.bundle;

import android.os.SystemClock;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class BundleInstaller implements IBundleInstaller {
    private static final int CLOSE_INSTALL_FACTORY = 5;
    private static final int CREATE_INSTALL_FACTORY = 4;
    private static final int DEFAULT_BUFFER_SIZE = 65536;
    private static final String DESCRIPTOR = "OHOS.Appexecfwk.IBundleInstaller";
    private static final String HAP_SUFFIX = ".hap";
    private static final int INSTALL = 0;
    private static final int INSTALL_REPLACE_EXISTING = 1;
    private static final int UNINSTALL = 1;
    private Context appContext;
    private final IRemoteObject remote;

    public BundleInstaller(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.bundle.IBundleInstaller
    public void setContext(Context context) {
        this.appContext = context;
    }

    @Override // ohos.bundle.IBundleInstaller
    public boolean install(List<String> list, InstallParam installParam, InstallerCallback installerCallback) throws RemoteException {
        if (list == null || list.isEmpty()) {
            AppLog.i("BundleManager::install failed: bundlePaths is empty", new Object[0]);
            return false;
        } else if (installerCallback == null) {
            AppLog.i("BundleManager::install failed: install callback is null", new Object[0]);
            return false;
        } else {
            AppLog.d("BundleInstaller::install kit startTime is %{private}dms", Long.valueOf(SystemClock.uptimeMillis()));
            ArrayList<String> checkBundlePath = checkBundlePath(list);
            if (checkBundlePath.isEmpty()) {
                AppLog.i("BundleManager::install failed: bundlePath not find hap file", new Object[0]);
                return false;
            }
            InstallFactory createInstallFactory = createInstallFactory(installParam);
            if (createInstallFactory == null) {
                AppLog.i("BundleInstaller::install create install factory failed", new Object[0]);
                return false;
            }
            Iterator<String> it = checkBundlePath.iterator();
            while (it.hasNext()) {
                if (!writeFileToStreamer(createInstallFactory, it.next())) {
                    closeInstallFactory(createInstallFactory);
                    return false;
                }
            }
            boolean install = createInstallFactory.install(installerCallback);
            if (!install) {
                closeInstallFactory(createInstallFactory);
            }
            return install;
        }
    }

    @Override // ohos.bundle.IBundleInstaller
    public boolean uninstall(String str, InstallParam installParam, InstallerCallback installerCallback) throws RemoteException {
        if (str == null || str.isEmpty()) {
            AppLog.e("BundleInstaller::uninstall failed: bundleName is empty", new Object[0]);
            return false;
        } else if (installerCallback == null) {
            AppLog.e("BundleInstaller::uninstall failed: callback is null", new Object[0]);
            return false;
        } else {
            AppLog.d("BundleInstaller::uninstall kit start %{private}s, and startTime is %{private}dms", str, Long.valueOf(SystemClock.uptimeMillis()));
            if (this.remote == null) {
                AppLog.e("BundleInstaller::uninstall remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (!obtain.writeInterfaceToken(DESCRIPTOR) || !obtain.writeString(str)) {
                return false;
            }
            obtain.writeSequenceable(installParam);
            if (!obtain.writeRemoteObject(installerCallback)) {
                return false;
            }
            try {
                if (!this.remote.sendRequest(1, obtain, obtain2, messageOption)) {
                    AppLog.w("BundleInstaller::uninstall sendRequest failed", new Object[0]);
                    return false;
                }
                reclaimParcel(obtain, obtain2);
                return true;
            } finally {
                reclaimParcel(obtain, obtain2);
            }
        }
    }

    private InstallFactory createInstallFactory(InstallParam installParam) throws RemoteException {
        if (this.remote == null) {
            AppLog.e("BundleInstaller::createInstallFactory remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            return null;
        }
        obtain.writeSequenceable(installParam);
        try {
            if (!this.remote.sendRequest(4, obtain, obtain2, messageOption)) {
                AppLog.w("BundleInstaller::createInstallFactory sendRequest failed", new Object[0]);
                return null;
            } else if (!obtain2.readBoolean()) {
                AppLog.w("BundleInstaller::create tmp install dir failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                IRemoteObject readRemoteObject = obtain2.readRemoteObject();
                if (readRemoteObject == null) {
                    AppLog.w("BundleInstaller::createInstallFactory service return installer failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                InstallFactory installFactory = new InstallFactory(readRemoteObject);
                installFactory.setFactoryId(readInt);
                reclaimParcel(obtain, obtain2);
                return installFactory;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    private void closeInstallFactory(InstallFactory installFactory) throws RemoteException {
        AppLog.d("BundleInstaller::closeInstallFactory start", new Object[0]);
        if (this.remote == null) {
            AppLog.e("BundleInstaller::closeInstallFactory remote is null", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (obtain.writeInterfaceToken(DESCRIPTOR) && obtain.writeInt(installFactory.getFactoryId())) {
            try {
                if (!this.remote.sendRequest(5, obtain, obtain2, messageOption)) {
                    AppLog.w("BundleInstaller::closeInstallFactory sendRequest failed", new Object[0]);
                }
            } finally {
                reclaimParcel(obtain, obtain2);
            }
        }
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    private boolean writeFileToStreamer(InstallFactory installFactory, String str) {
        Throwable th;
        IOException e;
        if (installFactory == null || str == null) {
            return false;
        }
        File file = new File(str);
        String name = file.getName();
        if (name.isEmpty()) {
            AppLog.e("BundleInstaller::writeFileToStreamer get file name failed", new Object[0]);
            return false;
        }
        FileOutputStream openStream = installFactory.openStream(name, 0);
        if (openStream == null) {
            AppLog.e("BundleInstaller::writeFileToStreamer open file(%{private}s) stream failed", name);
            return false;
        }
        Closeable closeable = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                byte[] bArr = new byte[65536];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read == -1) {
                        safeCloseStream(openStream);
                        safeCloseStream(fileInputStream);
                        return true;
                    } else if (read > 0) {
                        openStream.write(bArr, 0, read);
                    }
                }
            } catch (FileNotFoundException unused) {
                closeable = fileInputStream;
                AppLog.e("BundleInstaller::writeFileToStreamer file:%{private}s not found", str);
                safeCloseStream(openStream);
                safeCloseStream(closeable);
                return false;
            } catch (IOException e2) {
                e = e2;
                closeable = fileInputStream;
                try {
                    AppLog.e("BundleInstaller::writeFileToStreamer file:%{private}s io exception: %{public}s", str, e.getMessage());
                    safeCloseStream(openStream);
                    safeCloseStream(closeable);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    safeCloseStream(openStream);
                    safeCloseStream(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = fileInputStream;
                safeCloseStream(openStream);
                safeCloseStream(closeable);
                throw th;
            }
        } catch (FileNotFoundException unused2) {
            AppLog.e("BundleInstaller::writeFileToStreamer file:%{private}s not found", str);
            safeCloseStream(openStream);
            safeCloseStream(closeable);
            return false;
        } catch (IOException e3) {
            e = e3;
            AppLog.e("BundleInstaller::writeFileToStreamer file:%{private}s io exception: %{public}s", str, e.getMessage());
            safeCloseStream(openStream);
            safeCloseStream(closeable);
            return false;
        }
    }

    private void safeCloseStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
                AppLog.i("safeCloseStream failure", new Object[0]);
            }
        }
    }

    private ArrayList<String> checkBundlePath(List<String> list) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (list.size() != 1) {
            for (String str : list) {
                File file = new File(str);
                if (!isIllegaHapFile(file)) {
                    arrayList.clear();
                    return arrayList;
                }
                arrayList.add(getFormattedPath(file));
            }
            return arrayList;
        }
        File file2 = new File(list.get(0));
        if (!file2.isDirectory()) {
            if (isIllegaHapFile(file2)) {
                arrayList.add(getFormattedPath(file2));
            }
            return arrayList;
        }
        File[] listFiles = file2.listFiles();
        if (!(listFiles == null || listFiles.length == 0)) {
            for (File file3 : listFiles) {
                if (isIllegaHapFile(file3)) {
                    arrayList.add(getFormattedPath(file3));
                }
            }
        }
        return arrayList;
    }

    private boolean isIllegaHapFile(File file) {
        if (file.exists() && file.isFile() && file.canRead() && file.length() > 0 && file.getName().endsWith(HAP_SUFFIX)) {
            return true;
        }
        return false;
    }

    private String getFormattedPath(File file) {
        String str;
        IOException e;
        if (file == null || !file.exists()) {
            AppLog.e("BundleInstaller::getFormattedPath file is null or empty", new Object[0]);
            return "";
        }
        try {
            str = file.getCanonicalPath();
            try {
                if (this.appContext == null) {
                    return str;
                }
                File dataDir = this.appContext.getDataDir();
                if (dataDir == null) {
                    return "";
                }
                String canonicalPath = dataDir.getCanonicalPath();
                AppLog.d("BundleInstaller::getFormattedPath appPath = %{private}s", canonicalPath);
                if (str.startsWith(canonicalPath)) {
                    return str;
                }
                return canonicalPath + str;
            } catch (IOException e2) {
                e = e2;
                AppLog.e("BundleInstaller::getFormattedPath exception: %{public}s", e.getMessage());
                return str;
            }
        } catch (IOException e3) {
            e = e3;
            str = "";
            AppLog.e("BundleInstaller::getFormattedPath exception: %{public}s", e.getMessage());
            return str;
        }
    }

    private void reclaimParcel(MessageParcel messageParcel, MessageParcel messageParcel2) {
        messageParcel.reclaim();
        messageParcel2.reclaim();
    }
}
