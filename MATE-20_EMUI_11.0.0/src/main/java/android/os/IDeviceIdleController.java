package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.IMaintenanceActivityListener;

public interface IDeviceIdleController extends IInterface {
    @UnsupportedAppUsage
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

    String[] getRemovedSystemPowerWhitelistApps() throws RemoteException;

    String[] getSystemPowerWhitelist() throws RemoteException;

    String[] getSystemPowerWhitelistExceptIdle() throws RemoteException;

    String[] getUserPowerWhitelist() throws RemoteException;

    boolean isPowerSaveWhitelistApp(String str) throws RemoteException;

    boolean isPowerSaveWhitelistExceptIdleApp(String str) throws RemoteException;

    boolean registerMaintenanceActivityListener(IMaintenanceActivityListener iMaintenanceActivityListener) throws RemoteException;

    void removePowerSaveWhitelistApp(String str) throws RemoteException;

    void removeSystemPowerWhitelistApp(String str) throws RemoteException;

    void resetPreIdleTimeoutMode() throws RemoteException;

    void restoreSystemPowerWhitelistApp(String str) throws RemoteException;

    int setPreIdleTimeoutMode(int i) throws RemoteException;

    void unregisterMaintenanceActivityListener(IMaintenanceActivityListener iMaintenanceActivityListener) throws RemoteException;

    public static class Default implements IDeviceIdleController {
        @Override // android.os.IDeviceIdleController
        public void addPowerSaveWhitelistApp(String name) throws RemoteException {
        }

        @Override // android.os.IDeviceIdleController
        public void removePowerSaveWhitelistApp(String name) throws RemoteException {
        }

        @Override // android.os.IDeviceIdleController
        public void removeSystemPowerWhitelistApp(String name) throws RemoteException {
        }

        @Override // android.os.IDeviceIdleController
        public void restoreSystemPowerWhitelistApp(String name) throws RemoteException {
        }

        @Override // android.os.IDeviceIdleController
        public String[] getRemovedSystemPowerWhitelistApps() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public String[] getSystemPowerWhitelistExceptIdle() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public String[] getSystemPowerWhitelist() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public String[] getUserPowerWhitelist() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public String[] getFullPowerWhitelistExceptIdle() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public String[] getFullPowerWhitelist() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public int[] getAppIdWhitelistExceptIdle() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public int[] getAppIdWhitelist() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public int[] getAppIdUserWhitelist() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public int[] getAppIdTempWhitelist() throws RemoteException {
            return null;
        }

        @Override // android.os.IDeviceIdleController
        public boolean isPowerSaveWhitelistExceptIdleApp(String name) throws RemoteException {
            return false;
        }

        @Override // android.os.IDeviceIdleController
        public boolean isPowerSaveWhitelistApp(String name) throws RemoteException {
            return false;
        }

        @Override // android.os.IDeviceIdleController
        public void addPowerSaveTempWhitelistApp(String name, long duration, int userId, String reason) throws RemoteException {
        }

        @Override // android.os.IDeviceIdleController
        public long addPowerSaveTempWhitelistAppForMms(String name, int userId, String reason) throws RemoteException {
            return 0;
        }

        @Override // android.os.IDeviceIdleController
        public long addPowerSaveTempWhitelistAppForSms(String name, int userId, String reason) throws RemoteException {
            return 0;
        }

        @Override // android.os.IDeviceIdleController
        public void exitIdle(String reason) throws RemoteException {
        }

        @Override // android.os.IDeviceIdleController
        public boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) throws RemoteException {
            return false;
        }

        @Override // android.os.IDeviceIdleController
        public void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) throws RemoteException {
        }

        @Override // android.os.IDeviceIdleController
        public int setPreIdleTimeoutMode(int Mode) throws RemoteException {
            return 0;
        }

        @Override // android.os.IDeviceIdleController
        public void resetPreIdleTimeoutMode() throws RemoteException {
        }

        @Override // android.os.IDeviceIdleController
        public int forceIdle() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDeviceIdleController {
        private static final String DESCRIPTOR = "android.os.IDeviceIdleController";
        static final int TRANSACTION_addPowerSaveTempWhitelistApp = 17;
        static final int TRANSACTION_addPowerSaveTempWhitelistAppForMms = 18;
        static final int TRANSACTION_addPowerSaveTempWhitelistAppForSms = 19;
        static final int TRANSACTION_addPowerSaveWhitelistApp = 1;
        static final int TRANSACTION_exitIdle = 20;
        static final int TRANSACTION_forceIdle = 25;
        static final int TRANSACTION_getAppIdTempWhitelist = 14;
        static final int TRANSACTION_getAppIdUserWhitelist = 13;
        static final int TRANSACTION_getAppIdWhitelist = 12;
        static final int TRANSACTION_getAppIdWhitelistExceptIdle = 11;
        static final int TRANSACTION_getFullPowerWhitelist = 10;
        static final int TRANSACTION_getFullPowerWhitelistExceptIdle = 9;
        static final int TRANSACTION_getRemovedSystemPowerWhitelistApps = 5;
        static final int TRANSACTION_getSystemPowerWhitelist = 7;
        static final int TRANSACTION_getSystemPowerWhitelistExceptIdle = 6;
        static final int TRANSACTION_getUserPowerWhitelist = 8;
        static final int TRANSACTION_isPowerSaveWhitelistApp = 16;
        static final int TRANSACTION_isPowerSaveWhitelistExceptIdleApp = 15;
        static final int TRANSACTION_registerMaintenanceActivityListener = 21;
        static final int TRANSACTION_removePowerSaveWhitelistApp = 2;
        static final int TRANSACTION_removeSystemPowerWhitelistApp = 3;
        static final int TRANSACTION_resetPreIdleTimeoutMode = 24;
        static final int TRANSACTION_restoreSystemPowerWhitelistApp = 4;
        static final int TRANSACTION_setPreIdleTimeoutMode = 23;
        static final int TRANSACTION_unregisterMaintenanceActivityListener = 22;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "addPowerSaveWhitelistApp";
                case 2:
                    return "removePowerSaveWhitelistApp";
                case 3:
                    return "removeSystemPowerWhitelistApp";
                case 4:
                    return "restoreSystemPowerWhitelistApp";
                case 5:
                    return "getRemovedSystemPowerWhitelistApps";
                case 6:
                    return "getSystemPowerWhitelistExceptIdle";
                case 7:
                    return "getSystemPowerWhitelist";
                case 8:
                    return "getUserPowerWhitelist";
                case 9:
                    return "getFullPowerWhitelistExceptIdle";
                case 10:
                    return "getFullPowerWhitelist";
                case 11:
                    return "getAppIdWhitelistExceptIdle";
                case 12:
                    return "getAppIdWhitelist";
                case 13:
                    return "getAppIdUserWhitelist";
                case 14:
                    return "getAppIdTempWhitelist";
                case 15:
                    return "isPowerSaveWhitelistExceptIdleApp";
                case 16:
                    return "isPowerSaveWhitelistApp";
                case 17:
                    return "addPowerSaveTempWhitelistApp";
                case 18:
                    return "addPowerSaveTempWhitelistAppForMms";
                case 19:
                    return "addPowerSaveTempWhitelistAppForSms";
                case 20:
                    return "exitIdle";
                case 21:
                    return "registerMaintenanceActivityListener";
                case 22:
                    return "unregisterMaintenanceActivityListener";
                case 23:
                    return "setPreIdleTimeoutMode";
                case 24:
                    return "resetPreIdleTimeoutMode";
                case 25:
                    return "forceIdle";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        addPowerSaveWhitelistApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        removePowerSaveWhitelistApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        removeSystemPowerWhitelistApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        restoreSystemPowerWhitelistApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result = getRemovedSystemPowerWhitelistApps();
                        reply.writeNoException();
                        reply.writeStringArray(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result2 = getSystemPowerWhitelistExceptIdle();
                        reply.writeNoException();
                        reply.writeStringArray(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result3 = getSystemPowerWhitelist();
                        reply.writeNoException();
                        reply.writeStringArray(_result3);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result4 = getUserPowerWhitelist();
                        reply.writeNoException();
                        reply.writeStringArray(_result4);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result5 = getFullPowerWhitelistExceptIdle();
                        reply.writeNoException();
                        reply.writeStringArray(_result5);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result6 = getFullPowerWhitelist();
                        reply.writeNoException();
                        reply.writeStringArray(_result6);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result7 = getAppIdWhitelistExceptIdle();
                        reply.writeNoException();
                        reply.writeIntArray(_result7);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result8 = getAppIdWhitelist();
                        reply.writeNoException();
                        reply.writeIntArray(_result8);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result9 = getAppIdUserWhitelist();
                        reply.writeNoException();
                        reply.writeIntArray(_result9);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result10 = getAppIdTempWhitelist();
                        reply.writeNoException();
                        reply.writeIntArray(_result10);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPowerSaveWhitelistExceptIdleApp = isPowerSaveWhitelistExceptIdleApp(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isPowerSaveWhitelistExceptIdleApp ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPowerSaveWhitelistApp = isPowerSaveWhitelistApp(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isPowerSaveWhitelistApp ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        addPowerSaveTempWhitelistApp(data.readString(), data.readLong(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        long _result11 = addPowerSaveTempWhitelistAppForMms(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result11);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        long _result12 = addPowerSaveTempWhitelistAppForSms(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result12);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        exitIdle(data.readString());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerMaintenanceActivityListener = registerMaintenanceActivityListener(IMaintenanceActivityListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerMaintenanceActivityListener ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterMaintenanceActivityListener(IMaintenanceActivityListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = setPreIdleTimeoutMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        resetPreIdleTimeoutMode();
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = forceIdle();
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDeviceIdleController {
            public static IDeviceIdleController sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.os.IDeviceIdleController
            public void addPowerSaveWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addPowerSaveWhitelistApp(name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public void removePowerSaveWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removePowerSaveWhitelistApp(name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public void removeSystemPowerWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeSystemPowerWhitelistApp(name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public void restoreSystemPowerWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreSystemPowerWhitelistApp(name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public String[] getRemovedSystemPowerWhitelistApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRemovedSystemPowerWhitelistApps();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public String[] getSystemPowerWhitelistExceptIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemPowerWhitelistExceptIdle();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public String[] getSystemPowerWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemPowerWhitelist();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public String[] getUserPowerWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserPowerWhitelist();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public String[] getFullPowerWhitelistExceptIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFullPowerWhitelistExceptIdle();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public String[] getFullPowerWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFullPowerWhitelist();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public int[] getAppIdWhitelistExceptIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppIdWhitelistExceptIdle();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public int[] getAppIdWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppIdWhitelist();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public int[] getAppIdUserWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppIdUserWhitelist();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public int[] getAppIdTempWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppIdTempWhitelist();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public boolean isPowerSaveWhitelistExceptIdleApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPowerSaveWhitelistExceptIdleApp(name);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public boolean isPowerSaveWhitelistApp(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPowerSaveWhitelistApp(name);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public void addPowerSaveTempWhitelistApp(String name, long duration, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeLong(duration);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addPowerSaveTempWhitelistApp(name, duration, userId, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public long addPowerSaveTempWhitelistAppForMms(String name, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addPowerSaveTempWhitelistAppForMms(name, userId, reason);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public long addPowerSaveTempWhitelistAppForSms(String name, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addPowerSaveTempWhitelistAppForSms(name, userId, reason);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public void exitIdle(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().exitIdle(reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerMaintenanceActivityListener(listener);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterMaintenanceActivityListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public int setPreIdleTimeoutMode(int Mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(Mode);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPreIdleTimeoutMode(Mode);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public void resetPreIdleTimeoutMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetPreIdleTimeoutMode();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDeviceIdleController
            public int forceIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().forceIdle();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDeviceIdleController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDeviceIdleController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
