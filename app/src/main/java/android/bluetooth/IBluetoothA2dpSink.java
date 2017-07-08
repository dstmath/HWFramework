package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IBluetoothA2dpSink extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothA2dpSink {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothA2dpSink";
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_disconnect = 2;
        static final int TRANSACTION_getAudioConfig = 6;
        static final int TRANSACTION_getConnectedDevices = 3;
        static final int TRANSACTION_getConnectionState = 5;
        static final int TRANSACTION_getDevicesMatchingConnectionStates = 4;
        static final int TRANSACTION_getPriority = 8;
        static final int TRANSACTION_isA2dpPlaying = 9;
        static final int TRANSACTION_setPriority = 7;

        private static class Proxy implements IBluetoothA2dpSink {
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

            public boolean connect(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, 0);
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

            public boolean disconnect(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
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

            public List<BluetoothDevice> getConnectedDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getConnectedDevices, _data, _reply, 0);
                    _reply.readException();
                    List<BluetoothDevice> _result = _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(states);
                    this.mRemote.transact(Stub.TRANSACTION_getDevicesMatchingConnectionStates, _data, _reply, 0);
                    _reply.readException();
                    List<BluetoothDevice> _result = _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getConnectionState(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getConnectionState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BluetoothAudioConfig getAudioConfig(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    BluetoothAudioConfig bluetoothAudioConfig;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAudioConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bluetoothAudioConfig = (BluetoothAudioConfig) BluetoothAudioConfig.CREATOR.createFromParcel(_reply);
                    } else {
                        bluetoothAudioConfig = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bluetoothAudioConfig;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPriority(BluetoothDevice device, int priority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(priority);
                    this.mRemote.transact(Stub.TRANSACTION_setPriority, _data, _reply, 0);
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

            public int getPriority(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getPriority, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isA2dpPlaying(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isA2dpPlaying, _data, _reply, 0);
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

        public static IBluetoothA2dpSink asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothA2dpSink)) {
                return new Proxy(obj);
            }
            return (IBluetoothA2dpSink) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            BluetoothDevice bluetoothDevice;
            boolean _result;
            List<BluetoothDevice> _result2;
            int _result3;
            switch (code) {
                case TRANSACTION_connect /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result = connect(bluetoothDevice);
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_connect;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_disconnect /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result = disconnect(bluetoothDevice);
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_connect;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_getConnectedDevices /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getConnectedDevices();
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getDevicesMatchingConnectionStates /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDevicesMatchingConnectionStates(data.createIntArray());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getConnectionState /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result3 = getConnectionState(bluetoothDevice);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getAudioConfig /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    BluetoothAudioConfig _result4 = getAudioConfig(bluetoothDevice);
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_connect);
                        _result4.writeToParcel(reply, TRANSACTION_connect);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setPriority /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result = setPriority(bluetoothDevice, data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_connect;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_getPriority /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result3 = getPriority(bluetoothDevice);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_isA2dpPlaying /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result = isA2dpPlaying(bluetoothDevice);
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_connect;
                    }
                    reply.writeInt(i);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean connect(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean disconnect(BluetoothDevice bluetoothDevice) throws RemoteException;

    BluetoothAudioConfig getAudioConfig(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getConnectedDevices() throws RemoteException;

    int getConnectionState(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] iArr) throws RemoteException;

    int getPriority(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean isA2dpPlaying(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean setPriority(BluetoothDevice bluetoothDevice, int i) throws RemoteException;
}
