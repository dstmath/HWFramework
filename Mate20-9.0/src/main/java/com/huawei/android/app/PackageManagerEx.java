package com.huawei.android.app;

import android.content.pm.IPackageManager;
import android.os.IBackupSessionCallback;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.content.pm.IHwPackageManager;
import java.util.ArrayList;
import java.util.List;

public class PackageManagerEx {
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String TAG = "PackageManagerEx";
    public static final int TRANSACTION_CODE_FILE_BACKUP_EXECUTE_TASK = 1019;
    public static final int TRANSACTION_CODE_FILE_BACKUP_FINISH_SESSION = 1020;
    public static final int TRANSACTION_CODE_FILE_BACKUP_START_SESSION = 1018;
    public static final int TRANSACTION_CODE_GET_HDB_KEY = 1011;
    public static final int TRANSACTION_CODE_GET_IM_AND_VIDEO_APP_LIST = 1022;
    public static final int TRANSACTION_CODE_GET_MAX_ASPECT_RATIO = 1013;
    private static final int TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST = 1007;
    public static final int TRANSACTION_CODE_GET_PUBLICITY_DESCRIPTOR = 1015;
    public static final int TRANSACTION_CODE_GET_PUBLICITY_INFO_LIST = 1014;
    private static final int TRANSACTION_CODE_IS_NOTIFICATION_SPLIT = 1021;
    public static final int TRANSACTION_CODE_SET_HDB_KEY = 1010;
    public static final int TRANSACTION_CODE_SET_MAX_ASPECT_RATIO = 1012;
    private static final Singleton<IPackageManager> gDefault = new Singleton<IPackageManager>() {
        /* access modifiers changed from: protected */
        public IPackageManager create() {
            return IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        }
    };

    public static List<String> getPreinstalledApkList() {
        return HwPackageManager.getPreinstalledApkList();
    }

    private static IPackageManager getDefault() {
        return (IPackageManager) gDefault.get();
    }

    @Deprecated
    public static boolean checkGmsCoreUninstalled() {
        return false;
    }

    @Deprecated
    public static void deleteGmsCoreFromUninstalledDelapp() {
    }

    public static void setHdbKey(String key) {
        HwPackageManager.setHdbKey(key);
    }

    public static String getHdbKey() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String res = null;
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_HDB_KEY, data, reply, 0);
            reply.readException();
            res = reply.readString();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getHdbKey");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return res;
    }

    public static boolean setApplicationMaxAspectRatio(String packageName, float ar) {
        return HwPackageManager.setApplicationMaxAspectRatio(packageName, ar);
    }

    public static float getApplicationMaxAspectRatio(String packageName) {
        return HwPackageManager.getApplicationMaxAspectRatio(packageName);
    }

    public static boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) {
        return HwPackageManager.setApplicationAspectRatio(packageName, aspectName, ar);
    }

    public static float getApplicationAspectRatio(String packageName, String aspectName) {
        return HwPackageManager.getApplicationAspectRatio(packageName, aspectName);
    }

    public static ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() {
        return HwPackageManager.getHwPublicityAppParcelFileDescriptor();
    }

    public static List<String> getHwPublicityAppList() {
        return HwPackageManager.getHwPublicityAppList();
    }

    public static boolean isNotificationAddSplitButton(String pkgName) {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeString(pkgName);
            boolean z = false;
            getDefault().asBinder().transact(TRANSACTION_CODE_IS_NOTIFICATION_SPLIT, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            res = z;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get notification is split by RemoteException");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return res;
    }

    public static int startBackupSession(IBackupSessionCallback callback) {
        return HwPackageManager.startBackupSession(callback);
    }

    public static int executeBackupTask(int sessionId, String taskCmd) {
        return HwPackageManager.executeBackupTask(sessionId, taskCmd);
    }

    public static int finishBackupSession(int sessionId) {
        return HwPackageManager.finishBackupSession(sessionId);
    }

    public static boolean scanInstallApk(String apkFile) {
        return HwPackageManager.scanInstallApk(apkFile);
    }

    public static List<String> getScanInstallList() {
        return HwPackageManager.getScanInstallList();
    }

    public static List<String> getSupportSplitScreenApps() {
        List<String> list = new ArrayList<>();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_IM_AND_VIDEO_APP_LIST, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getHwPublicityAppList");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return list;
    }

    public static int getAppUseNotchMode(String packageName) {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms != null) {
            try {
                return pms.getAppUseNotchMode(packageName);
            } catch (RemoteException e) {
                Log.w(TAG, "getAppUseNotchMode RemoteException");
            }
        }
        return -1;
    }

    public static void setAppUseNotchMode(String packageName, int mode) {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms != null) {
            try {
                pms.setAppUseNotchMode(packageName, mode);
            } catch (RemoteException e) {
                Log.w(TAG, "setAppUseNotchMode RemoteException");
            }
        }
    }

    public static void setAppCanUninstall(String packageName, boolean canUninstall) {
        HwPackageManager.setAppCanUninstall(packageName, canUninstall);
    }
}
