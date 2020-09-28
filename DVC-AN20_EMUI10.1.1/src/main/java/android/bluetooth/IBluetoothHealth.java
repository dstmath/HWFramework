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
    boolean connectChannelToSink(BluetoothDevice bluetoothDevice, BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, int i) throws RemoteException;

    boolean connectChannelToSource(BluetoothDevice bluetoothDevice, BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration) throws RemoteException;

    boolean disconnectChannel(BluetoothDevice bluetoothDevice, BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, int i) throws RemoteException;

    List<BluetoothDevice> getConnectedHealthDevices() throws RemoteException;

    int getHealthDeviceConnectionState(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getHealthDevicesMatchingConnectionStates(int[] iArr) throws RemoteException;

    ParcelFileDescriptor getMainChannelFd(BluetoothDevice bluetoothDevice, BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration) throws RemoteException;

    boolean registerAppConfiguration(BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, IBluetoothHealthCallback iBluetoothHealthCallback) throws RemoteException;

    boolean unregisterAppConfiguration(BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration) throws RemoteException;

    public static class Default implements IBluetoothHealth {
        @Override // android.bluetooth.IBluetoothHealth
        public boolean registerAppConfiguration(BluetoothHealthAppConfiguration config, IBluetoothHealthCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothHealth
        public boolean unregisterAppConfiguration(BluetoothHealthAppConfiguration config) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothHealth
        public boolean connectChannelToSource(BluetoothDevice device, BluetoothHealthAppConfiguration config) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothHealth
        public boolean connectChannelToSink(BluetoothDevice device, BluetoothHealthAppConfiguration config, int channelType) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothHealth
        public boolean disconnectChannel(BluetoothDevice device, BluetoothHealthAppConfiguration config, int id) throws RemoteException {
            return false;
        }

        @Override // android.bluetooth.IBluetoothHealth
        public ParcelFileDescriptor getMainChannelFd(BluetoothDevice device, BluetoothHealthAppConfiguration config) throws RemoteException {
            return null;
        }

        @Override // android.bluetooth.IBluetoothHealth
        public List<BluetoothDevice> getConnectedHealthDevices() throws RemoteException {
            return null;
        }

        @Override // android.bluetooth.IBluetoothHealth
        public List<BluetoothDevice> getHealthDevicesMatchingConnectionStates(int[] states) throws RemoteException {
            return null;
        }

        @Override // android.bluetooth.IBluetoothHealth
        public int getHealthDeviceConnectionState(BluetoothDevice device) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerAppConfiguration";
                case 2:
                    return "unregisterAppConfiguration";
                case 3:
                    return "connectChannelToSource";
                case 4:
                    return "connectChannelToSink";
                case 5:
                    return "disconnectChannel";
                case 6:
                    return "getMainChannelFd";
                case 7:
                    return "getConnectedHealthDevices";
                case 8:
                    return "getHealthDevicesMatchingConnectionStates";
                case 9:
                    return "getHealthDeviceConnectionState";
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
            BluetoothHealthAppConfiguration _arg0;
            BluetoothHealthAppConfiguration _arg02;
            BluetoothDevice _arg03;
            BluetoothHealthAppConfiguration _arg1;
            BluetoothDevice _arg04;
            BluetoothHealthAppConfiguration _arg12;
            BluetoothDevice _arg05;
            BluetoothHealthAppConfiguration _arg13;
            BluetoothDevice _arg06;
            BluetoothHealthAppConfiguration _arg14;
            BluetoothDevice _arg07;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean registerAppConfiguration = registerAppConfiguration(_arg0, IBluetoothHealthCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerAppConfiguration ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean unregisterAppConfiguration = unregisterAppConfiguration(_arg02);
                        reply.writeNoException();
                        reply.writeInt(unregisterAppConfiguration ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean connectChannelToSource = connectChannelToSource(_arg03, _arg1);
                        reply.writeNoException();
                        reply.writeInt(connectChannelToSource ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean connectChannelToSink = connectChannelToSink(_arg04, _arg12, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(connectChannelToSink ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean disconnectChannel = disconnectChannel(_arg05, _arg13, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disconnectChannel ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        ParcelFileDescriptor _result = getMainChannelFd(_arg06, _arg14);
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result2 = getConnectedHealthDevices();
                        reply.writeNoException();
                        reply.writeTypedList(_result2);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result3 = getHealthDevicesMatchingConnectionStates(data.createIntArray());
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        int _result4 = getHealthDeviceConnectionState(_arg07);
                        reply.writeNoException();
                        reply.writeInt(_result4);
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
        public static class Proxy implements IBluetoothHealth {
            public static IBluetoothHealth sDefaultImpl;
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

            @Override // android.bluetooth.IBluetoothHealth
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
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerAppConfiguration(config, callback);
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

            @Override // android.bluetooth.IBluetoothHealth
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
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterAppConfiguration(config);
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

            @Override // android.bluetooth.IBluetoothHealth
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
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connectChannelToSource(device, config);
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

            @Override // android.bluetooth.IBluetoothHealth
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
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connectChannelToSink(device, config, channelType);
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

            @Override // android.bluetooth.IBluetoothHealth
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
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnectChannel(device, config, id);
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

            @Override // android.bluetooth.IBluetoothHealth
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
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMainChannelFd(device, config);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHealth
            public List<BluetoothDevice> getConnectedHealthDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConnectedHealthDevices();
                    }
                    _reply.readException();
                    List<BluetoothDevice> _result = _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHealth
            public List<BluetoothDevice> getHealthDevicesMatchingConnectionStates(int[] states) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(states);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHealthDevicesMatchingConnectionStates(states);
                    }
                    _reply.readException();
                    List<BluetoothDevice> _result = _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHealth
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
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHealthDeviceConnectionState(device);
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

        public static boolean setDefaultImpl(IBluetoothHealth impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBluetoothHealth getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
