package android.rms.iaware;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.List;

public class IAwareCMSManager {
    private static final String DESCRIPTOR = "android.rms.iaware.ICMSManager";
    private static final int TRANSACTION_configUpdate = 1;
    private static final int TRANSACTION_custConfigUpdate = 18;
    private static final int TRANSACTION_deleteAppCmpTypeInfo = 17;
    private static final int TRANSACTION_deleteCmpTypeInfo = 15;
    private static final int TRANSACTION_disableFeature = 3;
    private static final int TRANSACTION_enableFeature = 2;
    private static final int TRANSACTION_getAllAppTypeInfo = 11;
    private static final int TRANSACTION_getAppPreloadList = 12;
    private static final int TRANSACTION_getAppTypeInfo = 10;
    private static final int TRANSACTION_getCmpTypeList = 16;
    private static final int TRANSACTION_getConfig = 6;
    private static final int TRANSACTION_getCustConfig = 13;
    private static final int TRANSACTION_getDeviceLevel = 27;
    private static final int TRANSACTION_getDumpData = 7;
    private static final int TRANSACTION_getGameMode = 21;
    private static final int TRANSACTION_getInstalledGameList = 19;
    private static final int TRANSACTION_getStatisticsData = 8;
    private static final int TRANSACTION_getZipFiles = 9;
    private static final int TRANSACTION_insertCmpTypeInfo = 14;
    private static final int TRANSACTION_isAppRecognizedGame = 20;
    private static final int TRANSACTION_isFeatureEnabled = 4;
    private static final int TRANSACTION_isIAwareEnabled = 5;

    public static IBinder getICMSManager() {
        return ServiceManager.getService("IAwareCMSService");
    }

    public static boolean isIAwareEnabled(IBinder remote) throws RemoteException {
        boolean result = false;
        if (remote == null) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(5, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static AwareConfig getConfig(IBinder remote, String featureName, String configName) throws RemoteException {
        AwareConfig result = null;
        if (remote == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeString(featureName);
            data.writeString(configName);
            remote.transact(6, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = AwareConfig.CREATOR.createFromParcel(reply);
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static AwareConfig getCustConfig(IBinder remote, String featureName, String configName) throws RemoteException {
        AwareConfig result = null;
        if (remote == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeString(featureName);
            data.writeString(configName);
            remote.transact(13, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = AwareConfig.CREATOR.createFromParcel(reply);
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static List<AppTypeInfo> getAllAppTypeInfo(IBinder remote) throws RemoteException {
        List<AppTypeInfo> result = null;
        if (remote == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(11, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = reply.createTypedArrayList(AppTypeInfo.CREATOR);
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public static boolean insertCmpTypeInfo(IBinder remote, CmpTypeInfo info) throws RemoteException {
        boolean result = false;
        if (remote == null) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            if (info != null) {
                data.writeInt(1);
                info.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            remote.transact(14, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static boolean deleteCmpTypeInfo(IBinder remote, CmpTypeInfo info) throws RemoteException {
        boolean result = false;
        if (remote == null) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            if (info != null) {
                data.writeInt(1);
                info.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            remote.transact(15, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static List<CmpTypeInfo> getCmpTypeList(IBinder remote) throws RemoteException {
        if (remote == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(16, data, reply, 0);
            reply.readException();
            List<CmpTypeInfo> result = reply.createTypedArrayList(CmpTypeInfo.CREATOR);
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static boolean deleteAppCmpTypeInfo(IBinder remote, int userId, String pkgName) throws RemoteException {
        boolean result = false;
        if (remote == null) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(userId);
            data.writeString(pkgName);
            remote.transact(17, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static List<String> getInstalledGameList(IBinder remote) throws RemoteException {
        if (remote == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(19, data, reply, 0);
            reply.readException();
            List<String> result = reply.createStringArrayList();
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static boolean isAppRecognizedGame(IBinder remote, String packageName) throws RemoteException {
        boolean result = false;
        if (remote == null) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(20, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static int getDeviceLevel(IBinder remote) throws RemoteException {
        if (remote == null) {
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(27, data, reply, 0);
            reply.readException();
            int result = reply.readInt();
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }
}
