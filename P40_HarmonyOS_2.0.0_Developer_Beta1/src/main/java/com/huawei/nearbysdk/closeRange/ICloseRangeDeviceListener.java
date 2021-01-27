package com.huawei.nearbysdk.closeRange;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICloseRangeDeviceListener extends IInterface {
    void onDevice(CloseRangeResult closeRangeResult) throws RemoteException;

    public static abstract class Stub extends Binder implements ICloseRangeDeviceListener {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.closeRange.ICloseRangeDeviceListener";
        static final int TRANSACTION_onDevice = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICloseRangeDeviceListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICloseRangeDeviceListener)) {
                return new Proxy(obj);
            }
            return (ICloseRangeDeviceListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CloseRangeResult _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = CloseRangeResult.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onDevice(_arg0);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICloseRangeDeviceListener {
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

            @Override // com.huawei.nearbysdk.closeRange.ICloseRangeDeviceListener
            public void onDevice(CloseRangeResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
