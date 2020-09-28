package android.bluetooth;

import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBluetoothManager extends IInterface {
    boolean bindBluetoothProfileService(int i, IBluetoothProfileServiceConnection iBluetoothProfileServiceConnection) throws RemoteException;

    boolean disable(String str, boolean z) throws RemoteException;

    boolean enable(String str) throws RemoteException;

    boolean enableNoAutoConnect(String str) throws RemoteException;

    IBluetooth getAdapter() throws RemoteException;

    String getAddress() throws RemoteException;

    IBluetoothGatt getBluetoothGatt() throws RemoteException;

    String getName() throws RemoteException;

    int getState() throws RemoteException;

    boolean isBleAppPresent() throws RemoteException;

    boolean isBleScanAlwaysAvailable() throws RemoteException;

    boolean isEnabled() throws RemoteException;

    boolean isHearingAidProfileSupported() throws RemoteException;

    IBluetooth registerAdapter(IBluetoothManagerCallback iBluetoothManagerCallback) throws RemoteException;

    void registerStateChangeCallback(IBluetoothStateChangeCallback iBluetoothStateChangeCallback) throws RemoteException;

    void unbindBluetoothProfileService(int i, IBluetoothProfileServiceConnection iBluetoothProfileServiceConnection) throws RemoteException;

    void unregisterAdapter(IBluetoothManagerCallback iBluetoothManagerCallback) throws RemoteException;

    void unregisterStateChangeCallback(IBluetoothStateChangeCallback iBluetoothStateChangeCallback) throws RemoteException;

    int updateBleAppCount(IBinder iBinder, boolean z, String str) throws RemoteException;

    public static class Default implements IBluetoothManager {
        @Override // android.bluetooth.IBluetoothManager
        public IBluetooth registerAdapter(IBluetoothManagerCallback callback) throws RemoteException {
            return null;
        }

        @Override // android.bluetooth.IBluetoothManager
        public void unregisterAdapter(IBluetoothManagerCallback callback) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothManager
        public void registerStateChangeCallback(IBluetoothStateChangeCallback callback) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothManager
        public void unregisterStateChangeCallback(IBluetoothStateChangeCallback callback) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothManager
        public boolean isEnabled() throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothManager
        public boolean enable(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothManager
        public boolean enableNoAutoConnect(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothManager
        public boolean disable(String packageName, boolean persist) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothManager
        public int getState() throws RemoteException {
            return 0;
        }

        @Override // android.bluetooth.IBluetoothManager
        public IBluetoothGatt getBluetoothGatt() throws RemoteException {
            return null;
        }

        @Override // android.bluetooth.IBluetoothManager
        public boolean bindBluetoothProfileService(int profile, IBluetoothProfileServiceConnection proxy) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothManager
        public void unbindBluetoothProfileService(int profile, IBluetoothProfileServiceConnection proxy) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothManager
        public String getAddress() throws RemoteException {
            return null;
        }

        @Override // android.bluetooth.IBluetoothManager
        public String getName() throws RemoteException {
            return null;
        }

        @Override // android.bluetooth.IBluetoothManager
        public boolean isBleScanAlwaysAvailable() throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothManager
        public int updateBleAppCount(IBinder b, boolean enable, String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.bluetooth.IBluetoothManager
        public boolean isBleAppPresent() throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothManager
        public boolean isHearingAidProfileSupported() throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothManager
        public IBluetooth getAdapter() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBluetoothManager {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothManager";
        static final int TRANSACTION_bindBluetoothProfileService = 11;
        static final int TRANSACTION_disable = 8;
        static final int TRANSACTION_enable = 6;
        static final int TRANSACTION_enableNoAutoConnect = 7;
        static final int TRANSACTION_getAdapter = 19;
        static final int TRANSACTION_getAddress = 13;
        static final int TRANSACTION_getBluetoothGatt = 10;
        static final int TRANSACTION_getName = 14;
        static final int TRANSACTION_getState = 9;
        static final int TRANSACTION_isBleAppPresent = 17;
        static final int TRANSACTION_isBleScanAlwaysAvailable = 15;
        static final int TRANSACTION_isEnabled = 5;
        static final int TRANSACTION_isHearingAidProfileSupported = 18;
        static final int TRANSACTION_registerAdapter = 1;
        static final int TRANSACTION_registerStateChangeCallback = 3;
        static final int TRANSACTION_unbindBluetoothProfileService = 12;
        static final int TRANSACTION_unregisterAdapter = 2;
        static final int TRANSACTION_unregisterStateChangeCallback = 4;
        static final int TRANSACTION_updateBleAppCount = 16;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothManager)) {
                return new Proxy(obj);
            }
            return (IBluetoothManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerAdapter";
                case 2:
                    return "unregisterAdapter";
                case 3:
                    return "registerStateChangeCallback";
                case 4:
                    return "unregisterStateChangeCallback";
                case 5:
                    return "isEnabled";
                case 6:
                    return "enable";
                case 7:
                    return "enableNoAutoConnect";
                case 8:
                    return "disable";
                case 9:
                    return "getState";
                case 10:
                    return "getBluetoothGatt";
                case 11:
                    return "bindBluetoothProfileService";
                case 12:
                    return "unbindBluetoothProfileService";
                case 13:
                    return "getAddress";
                case 14:
                    return "getName";
                case 15:
                    return "isBleScanAlwaysAvailable";
                case 16:
                    return "updateBleAppCount";
                case 17:
                    return "isBleAppPresent";
                case 18:
                    return "isHearingAidProfileSupported";
                case 19:
                    return "getAdapter";
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
                boolean _arg1 = false;
                boolean _arg12 = false;
                IBinder iBinder = null;
                IBinder iBinder2 = null;
                IBinder iBinder3 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBluetooth _result = registerAdapter(IBluetoothManagerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterAdapter(IBluetoothManagerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        registerStateChangeCallback(IBluetoothStateChangeCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterStateChangeCallback(IBluetoothStateChangeCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEnabled = isEnabled();
                        reply.writeNoException();
                        reply.writeInt(isEnabled ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enable = enable(data.readString());
                        reply.writeNoException();
                        reply.writeInt(enable ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableNoAutoConnect = enableNoAutoConnect(data.readString());
                        reply.writeNoException();
                        reply.writeInt(enableNoAutoConnect ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean disable = disable(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(disable ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getState();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IBluetoothGatt _result3 = getBluetoothGatt();
                        reply.writeNoException();
                        if (_result3 != null) {
                            iBinder3 = _result3.asBinder();
                        }
                        reply.writeStrongBinder(iBinder3);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean bindBluetoothProfileService = bindBluetoothProfileService(data.readInt(), IBluetoothProfileServiceConnection.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(bindBluetoothProfileService ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        unbindBluetoothProfileService(data.readInt(), IBluetoothProfileServiceConnection.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getAddress();
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getName();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBleScanAlwaysAvailable = isBleScanAlwaysAvailable();
                        reply.writeNoException();
                        reply.writeInt(isBleScanAlwaysAvailable ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg02 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        int _result6 = updateBleAppCount(_arg02, _arg12, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBleAppPresent = isBleAppPresent();
                        reply.writeNoException();
                        reply.writeInt(isBleAppPresent ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHearingAidProfileSupported = isHearingAidProfileSupported();
                        reply.writeNoException();
                        reply.writeInt(isHearingAidProfileSupported ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IBluetooth _result7 = getAdapter();
                        reply.writeNoException();
                        if (_result7 != null) {
                            iBinder2 = _result7.asBinder();
                        }
                        reply.writeStrongBinder(iBinder2);
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
        public static class Proxy implements IBluetoothManager {
            public static IBluetoothManager sDefaultImpl;
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

            @Override // android.bluetooth.IBluetoothManager
            public IBluetooth registerAdapter(IBluetoothManagerCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerAdapter(callback);
                    }
                    _reply.readException();
                    IBluetooth _result = IBluetooth.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public void unregisterAdapter(IBluetoothManagerCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterAdapter(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public void registerStateChangeCallback(IBluetoothStateChangeCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerStateChangeCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public void unregisterStateChangeCallback(IBluetoothStateChangeCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterStateChangeCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public boolean isEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEnabled();
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

            @Override // android.bluetooth.IBluetoothManager
            public boolean enable(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enable(packageName);
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

            @Override // android.bluetooth.IBluetoothManager
            public boolean enableNoAutoConnect(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableNoAutoConnect(packageName);
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

            @Override // android.bluetooth.IBluetoothManager
            public boolean disable(String packageName, boolean persist) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(persist ? 1 : 0);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disable(packageName, persist);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public int getState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getState();
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

            @Override // android.bluetooth.IBluetoothManager
            public IBluetoothGatt getBluetoothGatt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBluetoothGatt();
                    }
                    _reply.readException();
                    IBluetoothGatt _result = IBluetoothGatt.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public boolean bindBluetoothProfileService(int profile, IBluetoothProfileServiceConnection proxy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(profile);
                    _data.writeStrongBinder(proxy != null ? proxy.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().bindBluetoothProfileService(profile, proxy);
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

            @Override // android.bluetooth.IBluetoothManager
            public void unbindBluetoothProfileService(int profile, IBluetoothProfileServiceConnection proxy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(profile);
                    _data.writeStrongBinder(proxy != null ? proxy.asBinder() : null);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unbindBluetoothProfileService(profile, proxy);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public String getAddress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAddress();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public String getName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getName();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothManager
            public boolean isBleScanAlwaysAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isBleScanAlwaysAvailable();
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

            @Override // android.bluetooth.IBluetoothManager
            public int updateBleAppCount(IBinder b, boolean enable, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(b);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateBleAppCount(b, enable, packageName);
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

            @Override // android.bluetooth.IBluetoothManager
            public boolean isBleAppPresent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isBleAppPresent();
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

            @Override // android.bluetooth.IBluetoothManager
            public boolean isHearingAidProfileSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHearingAidProfileSupported();
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

            @Override // android.bluetooth.IBluetoothManager
            public IBluetooth getAdapter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdapter();
                    }
                    _reply.readException();
                    IBluetooth _result = IBluetooth.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBluetoothManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBluetoothManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
