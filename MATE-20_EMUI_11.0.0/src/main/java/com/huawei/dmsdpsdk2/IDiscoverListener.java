package com.huawei.dmsdpsdk2;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IDiscoverListener extends IInterface {
    void onDeviceFound(DMSDPDevice dMSDPDevice) throws RemoteException;

    void onDeviceLost(DMSDPDevice dMSDPDevice) throws RemoteException;

    void onDeviceUpdate(DMSDPDevice dMSDPDevice, int i) throws RemoteException;

    void onStateChanged(int i, Map map) throws RemoteException;

    public static abstract class Stub extends Binder implements IDiscoverListener {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.IDiscoverListener";
        static final int TRANSACTION_onDeviceFound = 1;
        static final int TRANSACTION_onDeviceLost = 2;
        static final int TRANSACTION_onDeviceUpdate = 3;
        static final int TRANSACTION_onStateChanged = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDiscoverListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDiscoverListener)) {
                return new Proxy(obj);
            }
            return (IDiscoverListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DMSDPDevice _arg0;
            DMSDPDevice _arg02;
            DMSDPDevice _arg03;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = DMSDPDevice.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onDeviceFound(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = DMSDPDevice.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onDeviceLost(_arg02);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = DMSDPDevice.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                onDeviceUpdate(_arg03, data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onStateChanged(data.readInt(), data.readHashMap(getClass().getClassLoader()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDiscoverListener {
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

            @Override // com.huawei.dmsdpsdk2.IDiscoverListener
            public void onDeviceFound(DMSDPDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDiscoverListener
            public void onDeviceLost(DMSDPDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDiscoverListener
            public void onDeviceUpdate(DMSDPDevice device, int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(action);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDiscoverListener
            public void onStateChanged(int state, Map info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeMap(info);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
