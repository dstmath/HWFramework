package com.huawei.dmsdpsdk2;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IDMSDPListener extends IInterface {
    void onDeviceChange(DMSDPDevice dMSDPDevice, int i, Map map) throws RemoteException;

    void onDeviceServiceChange(DMSDPDeviceService dMSDPDeviceService, int i, Map map) throws RemoteException;

    public static abstract class Stub extends Binder implements IDMSDPListener {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.IDMSDPListener";
        static final int TRANSACTION_onDeviceChange = 1;
        static final int TRANSACTION_onDeviceServiceChange = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDMSDPListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDMSDPListener)) {
                return new Proxy(obj);
            }
            return (IDMSDPListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DMSDPDevice _arg0;
            DMSDPDeviceService _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = DMSDPDevice.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onDeviceChange(_arg0, data.readInt(), data.readHashMap(getClass().getClassLoader()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = DMSDPDeviceService.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onDeviceServiceChange(_arg02, data.readInt(), data.readHashMap(getClass().getClassLoader()));
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDMSDPListener {
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPListener
            public void onDeviceChange(DMSDPDevice device, int state, Map info) throws RemoteException {
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
                    _data.writeMap(info);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPListener
            public void onDeviceServiceChange(DMSDPDeviceService deviceService, int state, Map info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceService != null) {
                        _data.writeInt(1);
                        deviceService.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    _data.writeMap(info);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
