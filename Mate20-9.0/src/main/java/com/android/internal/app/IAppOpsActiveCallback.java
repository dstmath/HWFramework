package com.android.internal.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAppOpsActiveCallback extends IInterface {

    public static abstract class Stub extends Binder implements IAppOpsActiveCallback {
        private static final String DESCRIPTOR = "com.android.internal.app.IAppOpsActiveCallback";
        static final int TRANSACTION_opActiveChanged = 1;

        private static class Proxy implements IAppOpsActiveCallback {
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

            public void opActiveChanged(int op, int uid, String packageName, boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(op);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeInt(active);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppOpsActiveCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppOpsActiveCallback)) {
                return new Proxy(obj);
            }
            return (IAppOpsActiveCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                opActiveChanged(data.readInt(), data.readInt(), data.readString(), data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void opActiveChanged(int i, int i2, String str, boolean z) throws RemoteException;
}
