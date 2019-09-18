package com.huawei.dubai;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.dubai.IDubaiListener;

public interface IDubaiService extends IInterface {

    public static abstract class Stub extends Binder implements IDubaiService {
        private static final String DESCRIPTOR = "com.huawei.dubai.IDubaiService";
        static final int TRANSACTION_requestQueryData = 1;

        private static class Proxy implements IDubaiService {
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

            public void requestQueryData(long start, long end, int mask, int flag, int top, IDubaiListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(start);
                    _data.writeLong(end);
                    _data.writeInt(mask);
                    _data.writeInt(flag);
                    _data.writeInt(top);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDubaiService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDubaiService)) {
                return new Proxy(obj);
            }
            return (IDubaiService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            if (i == 1) {
                Parcel parcel = reply;
                data.enforceInterface(DESCRIPTOR);
                requestQueryData(data.readLong(), data.readLong(), data.readInt(), data.readInt(), data.readInt(), IDubaiListener.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void requestQueryData(long j, long j2, int i, int i2, int i3, IDubaiListener iDubaiListener) throws RemoteException;
}
