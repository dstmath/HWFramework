package com.huawei.pgmng.plug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IStateRecognitionSink extends IInterface {

    public static abstract class Stub extends Binder implements IStateRecognitionSink {
        private static final String DESCRIPTOR = "com.huawei.pgmng.plug.IStateRecognitionSink";
        static final int TRANSACTION_onPowerOverUsing = 2;
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

            public void onPowerOverUsing(String module, int resourceType, long stats_duration, long hold_time, String extend) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(module);
                    _data.writeInt(resourceType);
                    _data.writeLong(stats_duration);
                    _data.writeLong(hold_time);
                    _data.writeString(extend);
                    this.mRemote.transact(2, _data, null, 1);
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
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        onStateChanged(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        onPowerOverUsing(data.readString(), data.readInt(), data.readLong(), data.readLong(), data.readString());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onPowerOverUsing(String str, int i, long j, long j2, String str2) throws RemoteException;

    void onStateChanged(int i, int i2, int i3, String str, int i4) throws RemoteException;
}
