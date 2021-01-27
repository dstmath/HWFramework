package com.huawei.softnet.connect;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDiscoveryCallback extends IInterface {
    void onDeviceFound(IDeviceDesc iDeviceDesc) throws RemoteException;

    void onDeviceLost(IDeviceDesc iDeviceDesc) throws RemoteException;

    public static class Default implements IDiscoveryCallback {
        @Override // com.huawei.softnet.connect.IDiscoveryCallback
        public void onDeviceFound(IDeviceDesc device) throws RemoteException {
        }

        @Override // com.huawei.softnet.connect.IDiscoveryCallback
        public void onDeviceLost(IDeviceDesc device) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDiscoveryCallback {
        private static final String DESCRIPTOR = "com.huawei.softnet.connect.IDiscoveryCallback";
        static final int TRANSACTION_onDeviceFound = 1;
        static final int TRANSACTION_onDeviceLost = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDiscoveryCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDiscoveryCallback)) {
                return new Proxy(obj);
            }
            return (IDiscoveryCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IDeviceDesc _arg0;
            IDeviceDesc _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = IDeviceDesc.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onDeviceFound(_arg0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = IDeviceDesc.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onDeviceLost(_arg02);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDiscoveryCallback {
            public static IDiscoveryCallback sDefaultImpl;
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

            @Override // com.huawei.softnet.connect.IDiscoveryCallback
            public void onDeviceFound(IDeviceDesc device) throws RemoteException {
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
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDeviceFound(device);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.connect.IDiscoveryCallback
            public void onDeviceLost(IDeviceDesc device) throws RemoteException {
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
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDeviceLost(device);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDiscoveryCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDiscoveryCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
