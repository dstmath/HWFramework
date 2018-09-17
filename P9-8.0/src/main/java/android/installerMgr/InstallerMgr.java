package android.installerMgr;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AwareLog;

public class InstallerMgr {
    public static final int EVENT_FORK_NOTIFY = 2;
    public static final int EVENT_INSTALLED = 1;
    public static final int EVENT_INSTALLING = 0;
    public static final String KEY_BUNDLE_DEXOPT_PID = "dexopt_pid";
    public static final String KEY_BUNDLE_EVENT_ID = "eventId";
    public static final String KEY_BUNDLE_INSTALLER_NAME = "installer_name";
    public static final String KEY_BUNDLE_INSTALLER_UID = "installer_uid";
    public static final String KEY_BUNDLE_PACKAGE_NAME = "package_name";
    private static final String TAG = "InstallerMgr";
    private static InstallerMgr sInstallerMgr;
    private static Object sObject = new Object();
    private IBinder mAwareService;
    private InstallerInfo mInstallerInfo;
    private Object mLock = new Object();

    public static class InstallerInfo {
        public String installerPkgName;
        public int installerUid;
        public String pkgName;

        public InstallerInfo(String installerPkgName, String pkgName, int installerUid) {
            this.installerPkgName = installerPkgName;
            this.pkgName = pkgName;
            this.installerUid = installerUid;
        }
    }

    private InstallerMgr() {
    }

    public static InstallerMgr getInstance() {
        InstallerMgr installerMgr;
        synchronized (sObject) {
            if (sInstallerMgr == null) {
                sInstallerMgr = new InstallerMgr();
            }
            installerMgr = sInstallerMgr;
        }
        return installerMgr;
    }

    /* JADX WARNING: Missing block: B:10:0x003f, code:
            if (r2 == null) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:11:0x0041, code:
            if (r4 != null) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:12:0x0043, code:
            return;
     */
    /* JADX WARNING: Missing block: B:16:0x0047, code:
            r0 = new android.os.Bundle();
            r0.putInt(KEY_BUNDLE_EVENT_ID, r10);
            r0.putString(KEY_BUNDLE_INSTALLER_NAME, r2);
            r0.putString("package_name", r4);
            r0.putInt(KEY_BUNDLE_INSTALLER_UID, r3);
            notifiyEvent(r0);
     */
    /* JADX WARNING: Missing block: B:17:0x0067, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifiyInstallEvent(int eventId) {
        synchronized (this.mLock) {
            if (this.mInstallerInfo == null) {
                return;
            }
            String installerPkgName = this.mInstallerInfo.installerPkgName;
            String pkgName = this.mInstallerInfo.pkgName;
            int installerUid = this.mInstallerInfo.installerUid;
            AwareLog.d(TAG, "notifiyInstallEvent eventId = " + eventId + " pkgName = " + pkgName);
        }
    }

    private IBinder getAwareService() {
        return ServiceManager.getService("hwsysresmanager");
    }

    private void notifiyEvent(Bundle bundle) {
        if (this.mAwareService == null) {
            this.mAwareService = getAwareService();
            if (this.mAwareService == null) {
                return;
            }
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.rms.IHwSysResManager");
            data.writeBundle(bundle);
            this.mAwareService.transact(15016, data, reply, 0);
        } catch (RemoteException e) {
            AwareLog.e(TAG, "mAwareService ontransact " + e.getMessage());
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void installPackage(int eventId, String installerPkgName, String pkgName) {
        AwareLog.d(TAG, "installPackage eventId = " + eventId + " installerPkgName = " + installerPkgName + " pkgName = " + pkgName);
        synchronized (this.mLock) {
            if (eventId == 0) {
                this.mInstallerInfo = new InstallerInfo(installerPkgName, pkgName, 0);
                notifiyInstallEvent(eventId);
            } else if (eventId == 1) {
                notifiyInstallEvent(eventId);
                this.mInstallerInfo = null;
            } else {
                AwareLog.w(TAG, "installPackage unknown eventId = " + eventId);
            }
        }
    }
}
