package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBluetoothManager extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothManager {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothManager";
        static final int TRANSACTION_bindBluetoothProfileService = 10;
        static final int TRANSACTION_disable = 8;
        static final int TRANSACTION_disableRadio = 14;
        static final int TRANSACTION_enable = 6;
        static final int TRANSACTION_enableNoAutoConnect = 7;
        static final int TRANSACTION_enableRadio = 13;
        static final int TRANSACTION_getAddress = 15;
        static final int TRANSACTION_getBluetoothGatt = 9;
        static final int TRANSACTION_getName = 16;
        static final int TRANSACTION_isBleAppPresent = 19;
        static final int TRANSACTION_isBleScanAlwaysAvailable = 17;
        static final int TRANSACTION_isEnabled = 5;
        static final int TRANSACTION_isRadioEnabled = 12;
        static final int TRANSACTION_registerAdapter = 1;
        static final int TRANSACTION_registerStateChangeCallback = 3;
        static final int TRANSACTION_unbindBluetoothProfileService = 11;
        static final int TRANSACTION_unregisterAdapter = 2;
        static final int TRANSACTION_unregisterStateChangeCallback = 4;
        static final int TRANSACTION_updateBleAppCount = 18;

        private static class Proxy implements IBluetoothManager {
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

            public IBluetooth registerAdapter(IBluetoothManagerCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerAdapter, _data, _reply, 0);
                    _reply.readException();
                    IBluetooth _result = android.bluetooth.IBluetooth.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterAdapter(IBluetoothManagerCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterAdapter, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerStateChangeCallback(IBluetoothStateChangeCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerStateChangeCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterStateChangeCallback(IBluetoothStateChangeCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterStateChangeCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isEnabled, _data, _reply, 0);
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

            public boolean enable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enable, _data, _reply, 0);
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

            public boolean enableNoAutoConnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enableNoAutoConnect, _data, _reply, 0);
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

            public boolean disable(boolean persist) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (persist) {
                        i = Stub.TRANSACTION_registerAdapter;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_disable, _data, _reply, 0);
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

            public IBluetoothGatt getBluetoothGatt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getBluetoothGatt, _data, _reply, 0);
                    _reply.readException();
                    IBluetoothGatt _result = android.bluetooth.IBluetoothGatt.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean bindBluetoothProfileService(int profile, IBluetoothProfileServiceConnection proxy) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(profile);
                    if (proxy != null) {
                        iBinder = proxy.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_bindBluetoothProfileService, _data, _reply, 0);
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

            public void unbindBluetoothProfileService(int profile, IBluetoothProfileServiceConnection proxy) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(profile);
                    if (proxy != null) {
                        iBinder = proxy.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unbindBluetoothProfileService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRadioEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isRadioEnabled, _data, _reply, 0);
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

            public boolean enableRadio() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enableRadio, _data, _reply, 0);
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

            public boolean disableRadio() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disableRadio, _data, _reply, 0);
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

            public String getAddress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAddress, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBleScanAlwaysAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isBleScanAlwaysAvailable, _data, _reply, 0);
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

            public int updateBleAppCount(IBinder b, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(b);
                    if (enable) {
                        i = Stub.TRANSACTION_registerAdapter;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_updateBleAppCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBleAppPresent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isBleAppPresent, _data, _reply, 0);
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
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            String _result2;
            switch (code) {
                case TRANSACTION_registerAdapter /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    IBluetooth _result3 = registerAdapter(android.bluetooth.IBluetoothManagerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                    return true;
                case TRANSACTION_unregisterAdapter /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterAdapter(android.bluetooth.IBluetoothManagerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerStateChangeCallback /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerStateChangeCallback(android.bluetooth.IBluetoothStateChangeCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterStateChangeCallback /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterStateChangeCallback(android.bluetooth.IBluetoothStateChangeCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isEnabled /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_enable /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enable();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_enableNoAutoConnect /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableNoAutoConnect();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_disable /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disable(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_getBluetoothGatt /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    IBluetoothGatt _result4 = getBluetoothGatt();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case TRANSACTION_bindBluetoothProfileService /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = bindBluetoothProfileService(data.readInt(), android.bluetooth.IBluetoothProfileServiceConnection.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_unbindBluetoothProfileService /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    unbindBluetoothProfileService(data.readInt(), android.bluetooth.IBluetoothProfileServiceConnection.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isRadioEnabled /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRadioEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_enableRadio /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableRadio();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_disableRadio /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableRadio();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_getAddress /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAddress();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_getName /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getName();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_isBleScanAlwaysAvailable /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isBleScanAlwaysAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case TRANSACTION_updateBleAppCount /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result5 = updateBleAppCount(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_isBleAppPresent /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isBleAppPresent();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerAdapter : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean bindBluetoothProfileService(int i, IBluetoothProfileServiceConnection iBluetoothProfileServiceConnection) throws RemoteException;

    boolean disable(boolean z) throws RemoteException;

    boolean disableRadio() throws RemoteException;

    boolean enable() throws RemoteException;

    boolean enableNoAutoConnect() throws RemoteException;

    boolean enableRadio() throws RemoteException;

    String getAddress() throws RemoteException;

    IBluetoothGatt getBluetoothGatt() throws RemoteException;

    String getName() throws RemoteException;

    boolean isBleAppPresent() throws RemoteException;

    boolean isBleScanAlwaysAvailable() throws RemoteException;

    boolean isEnabled() throws RemoteException;

    boolean isRadioEnabled() throws RemoteException;

    IBluetooth registerAdapter(IBluetoothManagerCallback iBluetoothManagerCallback) throws RemoteException;

    void registerStateChangeCallback(IBluetoothStateChangeCallback iBluetoothStateChangeCallback) throws RemoteException;

    void unbindBluetoothProfileService(int i, IBluetoothProfileServiceConnection iBluetoothProfileServiceConnection) throws RemoteException;

    void unregisterAdapter(IBluetoothManagerCallback iBluetoothManagerCallback) throws RemoteException;

    void unregisterStateChangeCallback(IBluetoothStateChangeCallback iBluetoothStateChangeCallback) throws RemoteException;

    int updateBleAppCount(IBinder iBinder, boolean z) throws RemoteException;
}
