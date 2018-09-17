package com.huawei.IntelliServer.intellilib;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIntelliListener extends IInterface {

    public static abstract class Stub extends Binder implements IIntelliListener {
        private static final String DESCRIPTOR = "com.huawei.IntelliServer.intellilib.IIntelliListener";
        static final int TRANSACTION_onErr = 2;
        static final int TRANSACTION_onEvent = 1;

        private static class Proxy implements IIntelliListener {
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

            public void onEvent(IntelliAlgoResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onErr(int err) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(err);
                    this.mRemote.transact(2, _data, _reply, 0);
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

        public static IIntelliListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IIntelliListener)) {
                return (IIntelliListener) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    IntelliAlgoResult _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() == 0) {
                        _arg0 = null;
                    } else {
                        _arg0 = (IntelliAlgoResult) IntelliAlgoResult.CREATOR.createFromParcel(data);
                    }
                    onEvent(_arg0);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onErr(data.readInt());
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

    void onErr(int i) throws RemoteException;

    void onEvent(IntelliAlgoResult intelliAlgoResult) throws RemoteException;
}
