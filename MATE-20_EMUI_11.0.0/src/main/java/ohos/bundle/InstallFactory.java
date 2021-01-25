package ohos.bundle;

import java.io.FileOutputStream;
import ohos.appexecfwk.utils.AppLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class InstallFactory {
    private int factoryId;
    private IInstallFactory installFactory;

    public InstallFactory(IRemoteObject iRemoteObject) {
        this.installFactory = new IInstallFactory(iRemoteObject);
    }

    public void setFactoryId(int i) {
        this.factoryId = i;
    }

    public int getFactoryId() {
        return this.factoryId;
    }

    public FileOutputStream openStream(String str, long j) {
        try {
            return this.installFactory.openStream(str, j);
        } catch (RemoteException e) {
            AppLog.e("InstallFactory openStream exception throw for:%{private}s", e.getMessage());
            return null;
        }
    }

    public boolean install(InstallerCallback installerCallback) {
        try {
            return this.installFactory.install(installerCallback);
        } catch (RemoteException e) {
            AppLog.e("InstallFactory install exception throw for:%{private}s", e.getMessage());
            return false;
        }
    }
}
