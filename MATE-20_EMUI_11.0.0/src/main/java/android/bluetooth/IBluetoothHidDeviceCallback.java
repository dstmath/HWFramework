package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBluetoothHidDeviceCallback extends IInterface {
    void onAppStatusChanged(BluetoothDevice bluetoothDevice, boolean z) throws RemoteException;

    void onConnectionStateChanged(BluetoothDevice bluetoothDevice, int i) throws RemoteException;

    void onGetReport(BluetoothDevice bluetoothDevice, byte b, byte b2, int i) throws RemoteException;

    void onInterruptData(BluetoothDevice bluetoothDevice, byte b, byte[] bArr) throws RemoteException;

    void onSetProtocol(BluetoothDevice bluetoothDevice, byte b) throws RemoteException;

    void onSetReport(BluetoothDevice bluetoothDevice, byte b, byte b2, byte[] bArr) throws RemoteException;

    void onVirtualCableUnplug(BluetoothDevice bluetoothDevice) throws RemoteException;

    public static class Default implements IBluetoothHidDeviceCallback {
        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onAppStatusChanged(BluetoothDevice device, boolean registered) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onConnectionStateChanged(BluetoothDevice device, int state) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onSetProtocol(BluetoothDevice device, byte protocol) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onVirtualCableUnplug(BluetoothDevice device) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBluetoothHidDeviceCallback {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothHidDeviceCallback";
        static final int TRANSACTION_onAppStatusChanged = 1;
        static final int TRANSACTION_onConnectionStateChanged = 2;
        static final int TRANSACTION_onGetReport = 3;
        static final int TRANSACTION_onInterruptData = 6;
        static final int TRANSACTION_onSetProtocol = 5;
        static final int TRANSACTION_onSetReport = 4;
        static final int TRANSACTION_onVirtualCableUnplug = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothHidDeviceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothHidDeviceCallback)) {
                return new Proxy(obj);
            }
            return (IBluetoothHidDeviceCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onAppStatusChanged";
                case 2:
                    return "onConnectionStateChanged";
                case 3:
                    return "onGetReport";
                case 4:
                    return "onSetReport";
                case 5:
                    return "onSetProtocol";
                case 6:
                    return "onInterruptData";
                case 7:
                    return "onVirtualCableUnplug";
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
            BluetoothDevice _arg0;
            BluetoothDevice _arg02;
            BluetoothDevice _arg03;
            BluetoothDevice _arg04;
            BluetoothDevice _arg05;
            BluetoothDevice _arg06;
            BluetoothDevice _arg07;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onAppStatusChanged(_arg0, data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onConnectionStateChanged(_arg02, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onGetReport(_arg03, data.readByte(), data.readByte(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        onSetReport(_arg04, data.readByte(), data.readByte(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        onSetProtocol(_arg05, data.readByte());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        onInterruptData(_arg06, data.readByte(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        onVirtualCableUnplug(_arg07);
                        reply.writeNoException();
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
        public static class Proxy implements IBluetoothHidDeviceCallback {
            public static IBluetoothHidDeviceCallback sDefaultImpl;
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

            @Override // android.bluetooth.IBluetoothHidDeviceCallback
            public void onAppStatusChanged(BluetoothDevice device, boolean registered) throws RemoteException {
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
                    _data.writeInt(registered ? 1 : 0);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAppStatusChanged(device, registered);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHidDeviceCallback
            public void onConnectionStateChanged(BluetoothDevice device, int state) throws RemoteException {
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
                    _data.writeInt(state);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onConnectionStateChanged(device, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHidDeviceCallback
            public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) throws RemoteException {
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
                    _data.writeByte(type);
                    _data.writeByte(id);
                    _data.writeInt(bufferSize);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onGetReport(device, type, id, bufferSize);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHidDeviceCallback
            public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) throws RemoteException {
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
                    _data.writeByte(type);
                    _data.writeByte(id);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSetReport(device, type, id, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHidDeviceCallback
            public void onSetProtocol(BluetoothDevice device, byte protocol) throws RemoteException {
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
                    _data.writeByte(protocol);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSetProtocol(device, protocol);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHidDeviceCallback
            public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) throws RemoteException {
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
                    _data.writeByte(reportId);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onInterruptData(device, reportId, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHidDeviceCallback
            public void onVirtualCableUnplug(BluetoothDevice device) throws RemoteException {
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
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onVirtualCableUnplug(device);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBluetoothHidDeviceCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBluetoothHidDeviceCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
