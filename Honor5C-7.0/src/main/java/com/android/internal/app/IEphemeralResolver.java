package com.android.internal.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;

public interface IEphemeralResolver extends IInterface {

    public static abstract class Stub extends Binder implements IEphemeralResolver {
        private static final String DESCRIPTOR = "com.android.internal.app.IEphemeralResolver";
        static final int TRANSACTION_getEphemeralResolveInfoList = 1;

        private static class Proxy implements IEphemeralResolver {
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

            public void getEphemeralResolveInfoList(IRemoteCallback callback, int digestPrefix, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(digestPrefix);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_getEphemeralResolveInfoList, _data, null, Stub.TRANSACTION_getEphemeralResolveInfoList);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEphemeralResolver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEphemeralResolver)) {
                return new Proxy(obj);
            }
            return (IEphemeralResolver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_getEphemeralResolveInfoList /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    getEphemeralResolveInfoList(android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void getEphemeralResolveInfoList(IRemoteCallback iRemoteCallback, int i, int i2) throws RemoteException;
}
