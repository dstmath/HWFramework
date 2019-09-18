package android.os;

import android.os.IMaintenanceActivityListener;

public interface IDeviceIdleController extends IInterface {

    public static abstract class Stub extends Binder implements IDeviceIdleController {
        private static final String DESCRIPTOR = "android.os.IDeviceIdleController";
        static final int TRANSACTION_addPowerSaveTempWhitelistApp = 17;
        static final int TRANSACTION_addPowerSaveTempWhitelistAppForMms = 18;
        static final int TRANSACTION_addPowerSaveTempWhitelistAppForSms = 19;
        static final int TRANSACTION_addPowerSaveWhitelistApp = 1;
        static final int TRANSACTION_exitIdle = 20;
        static final int TRANSACTION_forceIdle = 23;
        static final int TRANSACTION_getAppIdTempWhitelist = 14;
        static final int TRANSACTION_getAppIdUserWhitelist = 13;
        static final int TRANSACTION_getAppIdWhitelist = 12;
        static final int TRANSACTION_getAppIdWhitelistExceptIdle = 11;
        static final int TRANSACTION_getFullPowerWhitelist = 10;
        static final int TRANSACTION_getFullPowerWhitelistExceptIdle = 9;
        static final int TRANSACTION_getIdleStateDetailed = 24;
        static final int TRANSACTION_getLightIdleStateDetailed = 25;
        static final int TRANSACTION_getRemovedSystemPowerWhitelistApps = 5;
        static final int TRANSACTION_getSystemPowerWhitelist = 7;
        static final int TRANSACTION_getSystemPowerWhitelistExceptIdle = 6;
        static final int TRANSACTION_getUserPowerWhitelist = 8;
        static final int TRANSACTION_isPowerSaveWhitelistApp = 16;
        static final int TRANSACTION_isPowerSaveWhitelistExceptIdleApp = 15;
        static final int TRANSACTION_registerMaintenanceActivityListener = 21;
        static final int TRANSACTION_removePowerSaveWhitelistApp = 2;
        static final int TRANSACTION_removeSystemPowerWhitelistApp = 3;
        static final int TRANSACTION_restoreSystemPowerWhitelistApp = 4;
        static final int TRANSACTION_unregisterMaintenanceActivityListener = 22;

        private static class Proxy implements IDeviceIdleController {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void addPowerSaveWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePowerSaveWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeSystemPowerWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreSystemPowerWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getRemovedSystemPowerWhitelistApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getSystemPowerWhitelistExceptIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getSystemPowerWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getUserPowerWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getFullPowerWhitelistExceptIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getFullPowerWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAppIdWhitelistExceptIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAppIdWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAppIdUserWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAppIdTempWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPowerSaveWhitelistExceptIdleApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPowerSaveWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPowerSaveTempWhitelistApp(String name, long duration, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeLong(duration);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long addPowerSaveTempWhitelistAppForMms(String name, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long addPowerSaveTempWhitelistAppForSms(String name, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void exitIdle(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int forceIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getIdleStateDetailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLightIdleStateDetailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDeviceIdleController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDeviceIdleController)) {
                return new Proxy(obj);
            }
            return (IDeviceIdleController) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        addPowerSaveWhitelistApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        removePowerSaveWhitelistApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeSystemPowerWhitelistApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        restoreSystemPowerWhitelistApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result = getRemovedSystemPowerWhitelistApps();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result2 = getSystemPowerWhitelistExceptIdle();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result2);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result3 = getSystemPowerWhitelist();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result3);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result4 = getUserPowerWhitelist();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result4);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result5 = getFullPowerWhitelistExceptIdle();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result5);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result6 = getFullPowerWhitelist();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result6);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result7 = getAppIdWhitelistExceptIdle();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result7);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result8 = getAppIdWhitelist();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result8);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result9 = getAppIdUserWhitelist();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result9);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result10 = getAppIdTempWhitelist();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result10);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = isPowerSaveWhitelistExceptIdleApp(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result12 = isPowerSaveWhitelistApp(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        addPowerSaveTempWhitelistApp(data.readString(), data.readLong(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result13 = addPowerSaveTempWhitelistAppForMms(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeLong(_result13);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result14 = addPowerSaveTempWhitelistAppForSms(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeLong(_result14);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        exitIdle(data.readString());
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result15 = registerMaintenanceActivityListener(IMaintenanceActivityListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterMaintenanceActivityListener(IMaintenanceActivityListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result16 = forceIdle();
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result17 = getIdleStateDetailed();
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result18 = getLightIdleStateDetailed();
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addPowerSaveTempWhitelistApp(String str, long j, int i, String str2) throws RemoteException;

    long addPowerSaveTempWhitelistAppForMms(String str, int i, String str2) throws RemoteException;

    long addPowerSaveTempWhitelistAppForSms(String str, int i, String str2) throws RemoteException;

    void addPowerSaveWhitelistApp(String str) throws RemoteException;

    void exitIdle(String str) throws RemoteException;

    int forceIdle() throws RemoteException;

    int[] getAppIdTempWhitelist() throws RemoteException;

    int[] getAppIdUserWhitelist() throws RemoteException;

    int[] getAppIdWhitelist() throws RemoteException;

    int[] getAppIdWhitelistExceptIdle() throws RemoteException;

    String[] getFullPowerWhitelist() throws RemoteException;

    String[] getFullPowerWhitelistExceptIdle() throws RemoteException;

    int getIdleStateDetailed() throws RemoteException;

    int getLightIdleStateDetailed() throws RemoteException;

    String[] getRemovedSystemPowerWhitelistApps() throws RemoteException;

    String[] getSystemPowerWhitelist() throws RemoteException;

    String[] getSystemPowerWhitelistExceptIdle() throws RemoteException;

    String[] getUserPowerWhitelist() throws RemoteException;

    boolean isPowerSaveWhitelistApp(String str) throws RemoteException;

    boolean isPowerSaveWhitelistExceptIdleApp(String str) throws RemoteException;

    boolean registerMaintenanceActivityListener(IMaintenanceActivityListener iMaintenanceActivityListener) throws RemoteException;

    void removePowerSaveWhitelistApp(String str) throws RemoteException;

    void removeSystemPowerWhitelistApp(String str) throws RemoteException;

    void restoreSystemPowerWhitelistApp(String str) throws RemoteException;

    void unregisterMaintenanceActivityListener(IMaintenanceActivityListener iMaintenanceActivityListener) throws RemoteException;
}
