package android.bluetooth;

import android.bluetooth.IBluetoothHealthCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.util.List;

public interface IBluetoothHealth extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothHealth {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothHealth";
        static final int TRANSACTION_connectChannelToSink = 4;
        static final int TRANSACTION_connectChannelToSource = 3;
        static final int TRANSACTION_disconnectChannel = 5;
        static final int TRANSACTION_getConnectedHealthDevices = 7;
        static final int TRANSACTION_getHealthDeviceConnectionState = 9;
        static final int TRANSACTION_getHealthDevicesMatchingConnectionStates = 8;
        static final int TRANSACTION_getMainChannelFd = 6;
        static final int TRANSACTION_registerAppConfiguration = 1;
        static final int TRANSACTION_unregisterAppConfiguration = 2;

        private static class Proxy implements IBluetoothHealth {
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

            public boolean registerAppConfiguration(BluetoothHealthAppConfiguration config, IBluetoothHealthCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unregisterAppConfiguration(BluetoothHealthAppConfiguration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean connectChannelToSource(BluetoothDevice device, BluetoothHealthAppConfiguration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean connectChannelToSink(BluetoothDevice device, BluetoothHealthAppConfiguration config, int channelType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(channelType);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disconnectChannel(BluetoothDevice device, BluetoothHealthAppConfiguration config, int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(id);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getMainChannelFd(BluetoothDevice device, BluetoothHealthAppConfiguration config) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<BluetoothDevice> getConnectedHealthDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<BluetoothDevice> getHealthDevicesMatchingConnectionStates(int[] states) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(states);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHealthDeviceConnectionState(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
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

        public static IBluetoothHealth asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothHealth)) {
                return new Proxy(obj);
            }
            return (IBluetoothHealth) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.bluetooth.BluetoothHealthAppConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.bluetooth.BluetoothHealthAppConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: android.bluetooth.BluetoothHealthAppConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.bluetooth.BluetoothHealthAppConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v18, resolved type: android.bluetooth.BluetoothHealthAppConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v22, resolved type: android.bluetooth.BluetoothHealthAppConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v28, resolved type: android.bluetooth.BluetoothDevice} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v33 */
        /* JADX WARNING: type inference failed for: r1v34 */
        /* JADX WARNING: type inference failed for: r1v35 */
        /* JADX WARNING: type inference failed for: r1v36 */
        /* JADX WARNING: type inference failed for: r1v37 */
        /* JADX WARNING: type inference failed for: r1v38 */
        /* JADX WARNING: type inference failed for: r1v39 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BluetoothDevice _arg0;
            BluetoothDevice _arg02;
            BluetoothDevice _arg03;
            BluetoothDevice _arg04;
            if (code != 1598968902) {
                ? _arg05 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean _result = registerAppConfiguration(_arg05, IBluetoothHealthCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean _result2 = unregisterAppConfiguration(_arg05);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean _result3 = connectChannelToSource(_arg0, _arg05);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean _result4 = connectChannelToSink(_arg02, _arg05, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean _result5 = disconnectChannel(_arg03, _arg05, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        }
                        ParcelFileDescriptor _result6 = getMainChannelFd(_arg04, _arg05);
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result7 = getConnectedHealthDevices();
                        reply.writeNoException();
                        reply.writeTypedList(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result8 = getHealthDevicesMatchingConnectionStates(data.createIntArray());
                        reply.writeNoException();
                        reply.writeTypedList(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        int _result9 = getHealthDeviceConnectionState(_arg05);
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean connectChannelToSink(BluetoothDevice bluetoothDevice, BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, int i) throws RemoteException;

    boolean connectChannelToSource(BluetoothDevice bluetoothDevice, BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration) throws RemoteException;

    boolean disconnectChannel(BluetoothDevice bluetoothDevice, BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, int i) throws RemoteException;

    List<BluetoothDevice> getConnectedHealthDevices() throws RemoteException;

    int getHealthDeviceConnectionState(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getHealthDevicesMatchingConnectionStates(int[] iArr) throws RemoteException;

    ParcelFileDescriptor getMainChannelFd(BluetoothDevice bluetoothDevice, BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration) throws RemoteException;

    boolean registerAppConfiguration(BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, IBluetoothHealthCallback iBluetoothHealthCallback) throws RemoteException;

    boolean unregisterAppConfiguration(BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration) throws RemoteException;
}
