package com.android.server.cota;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.cota.aidl.ICotaCallBack;
import com.huawei.cota.aidl.ICotaInterface;

public class CotaService extends Service {
    private static ICotaCallBack mICotaCallBack;
    private final ICotaInterface.Stub mBinder = new ICotaInterface.Stub() {
        /* class com.android.server.cota.CotaService.AnonymousClass1 */

        @Override // com.huawei.cota.aidl.ICotaInterface
        public boolean registerCallBack(ICotaCallBack callback, String packageName) throws RemoteException {
            if (callback == null) {
                return false;
            }
            CotaService cotaService = CotaService.this;
            ICotaCallBack unused = CotaService.mICotaCallBack = callback;
            return true;
        }

        @Override // com.huawei.cota.aidl.ICotaInterface
        public boolean unregisterCallBack(ICotaCallBack callback, String packageName) throws RemoteException {
            CotaService cotaService = CotaService.this;
            ICotaCallBack unused = CotaService.mICotaCallBack = null;
            return true;
        }

        @Override // com.huawei.cota.aidl.ICotaInterface
        public void startInstallApks() throws RemoteException {
            CotaInstallImpl.getInstance().doInstall();
        }

        @Override // com.huawei.cota.aidl.ICotaInterface
        public int getApksInstallStatus() throws RemoteException {
            return CotaInstallImpl.getInstance().doGetStatus();
        }

        @Override // com.huawei.cota.aidl.ICotaInterface
        public void startAutoInstall(String apkInstallConfig, String removableApkInstallConfig, String strMccMnc) {
            if (CotaService.this.checkCallingPermission("com.huawei.permission.COTA_APPS_NO_REBOOT") != -1) {
                CotaInstallImpl.getInstance().doStartAutoInstall(apkInstallConfig, removableApkInstallConfig, strMccMnc);
                return;
            }
            throw new SecurityException("Requires com.huawei.permission.COTA_APPS_NO_REBOOT");
        }
    };

    public static ICotaCallBack getICotaCallBack() {
        return mICotaCallBack;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
