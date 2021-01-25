package com.huawei.msdp.movement;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMSDPMovementStatusChangeCallBack extends IInterface {
    void onActivityChanged(int i, HwMSDPMovementChangeEvent hwMSDPMovementChangeEvent) throws RemoteException;

    public static abstract class Stub extends Binder implements IMSDPMovementStatusChangeCallBack {
        private static final String DESCRIPTOR = "com.huawei.msdp.movement.IMSDPMovementStatusChangeCallBack";
        static final int TRANSACTION_onActivityChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMSDPMovementStatusChangeCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMSDPMovementStatusChangeCallBack)) {
                return new Proxy(obj);
            }
            return (IMSDPMovementStatusChangeCallBack) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwMSDPMovementChangeEvent _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = HwMSDPMovementChangeEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onActivityChanged(_arg0, _arg1);
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
        public static class Proxy implements IMSDPMovementStatusChangeCallBack {
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

            @Override // com.huawei.msdp.movement.IMSDPMovementStatusChangeCallBack
            public void onActivityChanged(int type, HwMSDPMovementChangeEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
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
