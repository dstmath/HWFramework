package ohos.bundle;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import ohos.appexecfwk.utils.AppLog;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class IInstallFactory implements IRemoteBroker {
    private static final String DESCRIPTOR = "OHOS.Appexecfwk.IInstallFactory";
    private static final int INSTALL = 1;
    private static final int OPEN_STREAM = 0;
    private final IRemoteObject remote;

    public IInstallFactory(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public FileOutputStream openStream(String str, long j) throws RemoteException {
        FileOutputStream fileOutputStream = null;
        if (this.remote == null) {
            AppLog.e("BundleInstaller::uninstall remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DESCRIPTOR) || !obtain.writeString(str) || !obtain.writeLong(j)) {
            return null;
        }
        try {
            if (!this.remote.sendRequest(0, obtain, obtain2, messageOption)) {
                AppLog.w("BundleManager::writeSession sendRequest failed", new Object[0]);
                return null;
            }
            FileDescriptor readFileDescriptor = obtain2.readFileDescriptor();
            if (readFileDescriptor != null) {
                fileOutputStream = new FileOutputStream(readFileDescriptor);
                AppLog.w("BundleManager::writeSession fos length", new Object[0]);
            }
            reclaimParcel(obtain, obtain2);
            return fileOutputStream;
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    public boolean install(InstallerCallback installerCallback) throws RemoteException {
        if (this.remote == null) {
            AppLog.e("BundleInstaller::uninstall remote is null", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DESCRIPTOR) || !obtain.writeRemoteObject(installerCallback)) {
            return false;
        }
        try {
            if (!this.remote.sendRequest(1, obtain, obtain2, messageOption)) {
                AppLog.w("BundleManager::install sendRequest failed", new Object[0]);
                return false;
            }
            boolean readBoolean = obtain2.readBoolean();
            if (!readBoolean) {
                AppLog.w("BundleManager::install handle failed", new Object[0]);
            }
            reclaimParcel(obtain, obtain2);
            return readBoolean;
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    private void reclaimParcel(MessageParcel messageParcel, MessageParcel messageParcel2) {
        messageParcel.reclaim();
        messageParcel2.reclaim();
    }
}
