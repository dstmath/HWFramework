package android.os;

public interface IDeviceIdleController extends IInterface {

    public static abstract class Stub extends Binder implements IDeviceIdleController {
        private static final String DESCRIPTOR = "android.os.IDeviceIdleController";
        static final int TRANSACTION_addPowerSaveTempWhitelistApp = 14;
        static final int TRANSACTION_addPowerSaveTempWhitelistAppForMms = 15;
        static final int TRANSACTION_addPowerSaveTempWhitelistAppForSms = 16;
        static final int TRANSACTION_addPowerSaveWhitelistApp = 1;
        static final int TRANSACTION_exitIdle = 17;
        static final int TRANSACTION_forceIdle = 20;
        static final int TRANSACTION_getAppIdTempWhitelist = 11;
        static final int TRANSACTION_getAppIdUserWhitelist = 10;
        static final int TRANSACTION_getAppIdWhitelist = 9;
        static final int TRANSACTION_getAppIdWhitelistExceptIdle = 8;
        static final int TRANSACTION_getFullPowerWhitelist = 7;
        static final int TRANSACTION_getFullPowerWhitelistExceptIdle = 6;
        static final int TRANSACTION_getIdleStateDetailed = 21;
        static final int TRANSACTION_getLightIdleStateDetailed = 22;
        static final int TRANSACTION_getSystemPowerWhitelist = 4;
        static final int TRANSACTION_getSystemPowerWhitelistExceptIdle = 3;
        static final int TRANSACTION_getUserPowerWhitelist = 5;
        static final int TRANSACTION_isPowerSaveWhitelistApp = 13;
        static final int TRANSACTION_isPowerSaveWhitelistExceptIdleApp = 12;
        static final int TRANSACTION_registerMaintenanceActivityListener = 18;
        static final int TRANSACTION_removePowerSaveWhitelistApp = 2;
        static final int TRANSACTION_unregisterMaintenanceActivityListener = 19;

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
                    this.mRemote.transact(Stub.TRANSACTION_addPowerSaveWhitelistApp, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_removePowerSaveWhitelistApp, _data, _reply, 0);
                    _reply.readException();
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
                    this.mRemote.transact(Stub.TRANSACTION_getSystemPowerWhitelistExceptIdle, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSystemPowerWhitelist, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getUserPowerWhitelist, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getFullPowerWhitelistExceptIdle, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getFullPowerWhitelist, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAppIdWhitelistExceptIdle, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAppIdWhitelist, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAppIdUserWhitelist, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAppIdTempWhitelist, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isPowerSaveWhitelistExceptIdleApp, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_isPowerSaveWhitelistApp, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_addPowerSaveTempWhitelistApp, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_addPowerSaveTempWhitelistAppForMms, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_addPowerSaveTempWhitelistAppForSms, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_exitIdle, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerMaintenanceActivityListener, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterMaintenanceActivityListener, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_forceIdle, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getIdleStateDetailed, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getLightIdleStateDetailed, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
            String[] _result;
            int[] _result2;
            boolean _result3;
            long _result4;
            int _result5;
            switch (code) {
                case TRANSACTION_addPowerSaveWhitelistApp /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    addPowerSaveWhitelistApp(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removePowerSaveWhitelistApp /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    removePowerSaveWhitelistApp(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSystemPowerWhitelistExceptIdle /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSystemPowerWhitelistExceptIdle();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_getSystemPowerWhitelist /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSystemPowerWhitelist();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_getUserPowerWhitelist /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUserPowerWhitelist();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_getFullPowerWhitelistExceptIdle /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getFullPowerWhitelistExceptIdle();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_getFullPowerWhitelist /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getFullPowerWhitelist();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_getAppIdWhitelistExceptIdle /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppIdWhitelistExceptIdle();
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_getAppIdWhitelist /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppIdWhitelist();
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_getAppIdUserWhitelist /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppIdUserWhitelist();
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_getAppIdTempWhitelist /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppIdTempWhitelist();
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_isPowerSaveWhitelistExceptIdleApp /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isPowerSaveWhitelistExceptIdleApp(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_addPowerSaveWhitelistApp : 0);
                    return true;
                case TRANSACTION_isPowerSaveWhitelistApp /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isPowerSaveWhitelistApp(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_addPowerSaveWhitelistApp : 0);
                    return true;
                case TRANSACTION_addPowerSaveTempWhitelistApp /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    addPowerSaveTempWhitelistApp(data.readString(), data.readLong(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addPowerSaveTempWhitelistAppForMms /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = addPowerSaveTempWhitelistAppForMms(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case TRANSACTION_addPowerSaveTempWhitelistAppForSms /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = addPowerSaveTempWhitelistAppForSms(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case TRANSACTION_exitIdle /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    exitIdle(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerMaintenanceActivityListener /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerMaintenanceActivityListener(android.os.IMaintenanceActivityListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_addPowerSaveWhitelistApp : 0);
                    return true;
                case TRANSACTION_unregisterMaintenanceActivityListener /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterMaintenanceActivityListener(android.os.IMaintenanceActivityListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forceIdle /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = forceIdle();
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_getIdleStateDetailed /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getIdleStateDetailed();
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_getLightIdleStateDetailed /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getLightIdleStateDetailed();
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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

    String[] getSystemPowerWhitelist() throws RemoteException;

    String[] getSystemPowerWhitelistExceptIdle() throws RemoteException;

    String[] getUserPowerWhitelist() throws RemoteException;

    boolean isPowerSaveWhitelistApp(String str) throws RemoteException;

    boolean isPowerSaveWhitelistExceptIdleApp(String str) throws RemoteException;

    boolean registerMaintenanceActivityListener(IMaintenanceActivityListener iMaintenanceActivityListener) throws RemoteException;

    void removePowerSaveWhitelistApp(String str) throws RemoteException;

    void unregisterMaintenanceActivityListener(IMaintenanceActivityListener iMaintenanceActivityListener) throws RemoteException;
}
