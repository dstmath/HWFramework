package com.huawei.airsharing.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteCtrlEventProcessor extends IInterface {
    int process(int i, int i2, byte[] bArr) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteCtrlEventProcessor {
        private static final String DESCRIPTOR = "com.huawei.airsharing.api.IRemoteCtrlEventProcessor";
        static final int TRANSACTION_process = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteCtrlEventProcessor asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemoteCtrlEventProcessor)) {
                return new Proxy(obj);
            }
            return (IRemoteCtrlEventProcessor) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = process(data.readInt(), data.readInt(), data.createByteArray());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IRemoteCtrlEventProcessor {
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

            @Override // com.huawei.airsharing.api.IRemoteCtrlEventProcessor
            public int process(int eventType, int len, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventType);
                    _data.writeInt(len);
                    _data.writeByteArray(data);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
