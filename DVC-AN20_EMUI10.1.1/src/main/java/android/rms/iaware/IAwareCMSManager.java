package android.rms.iaware;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.List;

public class IAwareCMSManager {
    private static final String DESCRIPTOR = "android.rms.iaware.ICMSManager";
    private static final int TRANSACTION_DELETEAPPCMPTYPEINFO = 17;
    private static final int TRANSACTION_DELETECMPTYPEINFO = 15;
    private static final int TRANSACTION_GETALLAPPTYPEINFO = 11;
    private static final int TRANSACTION_GETCMPTYPELIST = 16;
    private static final int TRANSACTION_GETCONFIG = 6;
    private static final int TRANSACTION_GETCUSTCONFIG = 13;
    private static final int TRANSACTION_GETDEVICELEVEL = 27;
    private static final int TRANSACTION_INSERTCMPTYPEINFO = 14;
    private static final int TRANSACTION_ISIAWAREENABLED = 5;

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
        if (remote == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        AwareConfig result = null;
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
        if (remote == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        AwareConfig result = null;
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
        if (remote == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<AppTypeInfo> result = null;
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
