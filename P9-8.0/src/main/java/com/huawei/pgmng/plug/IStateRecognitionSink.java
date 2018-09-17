package com.huawei.pgmng.plug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IStateRecognitionSink extends IInterface {

    public static abstract class Stub extends Binder implements IStateRecognitionSink {
        private static final String DESCRIPTOR = "com.huawei.pgmng.plug.IStateRecognitionSink";
        static final int TRANSACTION_onStateChanged = 1;

        private static class Proxy implements IStateRecognitionSink {
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

            public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateType);
                    _data.writeInt(eventType);
                    _data.writeInt(pid);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStateRecognitionSink asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStateRecognitionSink)) {
                return new Proxy(obj);
            }
            return (IStateRecognitionSink) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onStateChanged(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onStateChanged(int i, int i2, int i3, String str, int i4) throws RemoteException;
}
