package com.huawei.android.app;

import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.os.IBackupSessionCallback;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import java.util.ArrayList;
import java.util.List;

public class PackageManagerEx {
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String TAG = "PackageManagerEx";
    private static final int TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED = 1008;
    private static final int TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP = 1009;
    public static final int TRANSACTION_CODE_FILE_BACKUP_EXECUTE_TASK = 1019;
    public static final int TRANSACTION_CODE_FILE_BACKUP_FINISH_SESSION = 1020;
    public static final int TRANSACTION_CODE_FILE_BACKUP_START_SESSION = 1018;
    public static final int TRANSACTION_CODE_GET_HDB_KEY = 1011;
    public static final int TRANSACTION_CODE_GET_IM_AND_VIDEO_APP_LIST = 1022;
    public static final int TRANSACTION_CODE_GET_MAX_ASPECT_RATIO = 1013;
    private static final int TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST = 1007;
    public static final int TRANSACTION_CODE_GET_PUBLICITY_DESCRIPTOR = 1015;
    public static final int TRANSACTION_CODE_GET_PUBLICITY_INFO_LIST = 1014;
    public static final int TRANSACTION_CODE_GET_SCAN_INSTALL_LIST = 1017;
    private static final int TRANSACTION_CODE_IS_NOTIFICATION_SPLIT = 1021;
    public static final int TRANSACTION_CODE_SCAN_INSTALL_APK = 1016;
    public static final int TRANSACTION_CODE_SET_HDB_KEY = 1010;
    public static final int TRANSACTION_CODE_SET_MAX_ASPECT_RATIO = 1012;
    private static final Singleton<IPackageManager> gDefault = new Singleton<IPackageManager>() {
        protected IPackageManager create() {
            return Stub.asInterface(ServiceManager.getService("package"));
        }
    };

    public static List<String> getPreinstalledApkList() {
        List<String> list = new ArrayList();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
        } catch (Exception e) {
            Log.e(TAG, "failed to getPreinstalledApkList");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return list;
    }

    private static IPackageManager getDefault() {
        return (IPackageManager) gDefault.get();
    }

    public static boolean checkGmsCoreUninstalled() {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to checkGmsCoreUninstalled");
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        return res;
    }

    public static void deleteGmsCoreFromUninstalledDelapp() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to deleteGmsCoreFromUninstalledDelapp");
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public static void setHdbKey(String key) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeString(key);
            getDefault().asBinder().transact(TRANSACTION_CODE_SET_HDB_KEY, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to setHdbKey");
        } finally {
            reply.recycle();
            data.recycle();
        }
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
        } finally {
            reply.recycle();
            data.recycle();
        }
        return res;
    }

    public static boolean setApplicationMaxAspectRatio(String packageName, float ar) {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeString(packageName);
            data.writeFloat(ar);
            getDefault().asBinder().transact(TRANSACTION_CODE_SET_MAX_ASPECT_RATIO, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to set Application max aspect ratio");
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        return res;
    }

    public static float getApplicationMaxAspectRatio(String packageName) {
        float res = 0.0f;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeString(packageName);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_MAX_ASPECT_RATIO, data, reply, 0);
            reply.readException();
            res = reply.readFloat();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get Application max aspect ratio");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return res;
    }

    public static ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() {
        ParcelFileDescriptor pd = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_PUBLICITY_DESCRIPTOR, data, reply, 0);
            reply.readException();
            pd = reply.readFileDescriptor();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getHwPublicityAppParcelFileDescriptor");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return pd;
    }

    public static List<String> getHwPublicityAppList() {
        List<String> list = new ArrayList();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_PUBLICITY_INFO_LIST, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getHwPublicityAppList");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return list;
    }

    public static boolean isNotificationAddSplitButton(String pkgName) {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeString(pkgName);
            getDefault().asBinder().transact(TRANSACTION_CODE_IS_NOTIFICATION_SPLIT, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get notification is split by RemoteException");
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        return res;
    }

    public static int startBackupSession(IBackupSessionCallback callback) {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = -1;
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            if (callback != null) {
                iBinder = callback.asBinder();
            }
            data.writeStrongBinder(iBinder);
            getDefault().asBinder().transact(TRANSACTION_CODE_FILE_BACKUP_START_SESSION, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to startBackupSession");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    public static int executeBackupTask(int sessionId, String taskCmd) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = -1;
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeInt(sessionId);
            data.writeString(taskCmd);
            getDefault().asBinder().transact(TRANSACTION_CODE_FILE_BACKUP_EXECUTE_TASK, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to executeBackupTask");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    public static int finishBackupSession(int sessionId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = -1;
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeInt(sessionId);
            getDefault().asBinder().transact(TRANSACTION_CODE_FILE_BACKUP_FINISH_SESSION, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to finishBackupSession");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    public static boolean scanInstallApk(String apkFile) {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeString(apkFile);
            getDefault().asBinder().transact(TRANSACTION_CODE_SCAN_INSTALL_APK, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to scanInstallApk");
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        return res;
    }

    public static List<String> getScanInstallList() {
        List<String> list = new ArrayList();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_SCAN_INSTALL_LIST, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getScanInstallList");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return list;
    }

    public static List<String> getSupportSplitScreenApps() {
        List<String> list = new ArrayList();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_IM_AND_VIDEO_APP_LIST, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getHwPublicityAppList");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return list;
    }
}
