package com.huawei.msdp.devicestatus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMSDPDeviceStatusChangedCallBack extends IInterface {

    public static abstract class Stub extends Binder implements IMSDPDeviceStatusChangedCallBack {
        private static final String DESCRIPTOR = "com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack";
        static final int TRANSACTION_onDeviceStatusChanged = 1;

        private static class Proxy implements IMSDPDeviceStatusChangedCallBack {
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

            public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMSDPDeviceStatusChangedCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IMSDPDeviceStatusChangedCallBack)) {
                return (IMSDPDeviceStatusChangedCallBack) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    HwMSDPDeviceStatusChangeEvent _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() == 0) {
                        _arg0 = null;
                    } else {
                        _arg0 = (HwMSDPDeviceStatusChangeEvent) HwMSDPDeviceStatusChangeEvent.CREATOR.createFromParcel(data);
                    }
                    onDeviceStatusChanged(_arg0);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent hwMSDPDeviceStatusChangeEvent) throws RemoteException;
}
