package com.huawei.onehopassistantsdk;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IOnehopMagicWindowCallback extends IInterface {
    void onReceive(int i, String str) throws RemoteException;

    void onReceiveBundle(int i, Bundle bundle) throws RemoteException;

    void onReceiveFile(int i, List<Uri> list) throws RemoteException;

    public static class Default implements IOnehopMagicWindowCallback {
        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
        public void onReceive(int msgType, String msg) throws RemoteException {
        }

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
        public void onReceiveBundle(int msgType, Bundle data) throws RemoteException {
        }

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
        public void onReceiveFile(int msgType, List<Uri> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOnehopMagicWindowCallback {
        private static final String DESCRIPTOR = "com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback";
        static final int TRANSACTION_onReceive = 1;
        static final int TRANSACTION_onReceiveBundle = 2;
        static final int TRANSACTION_onReceiveFile = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnehopMagicWindowCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnehopMagicWindowCallback)) {
                return new Proxy(obj);
            }
            return (IOnehopMagicWindowCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onReceive(data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onReceiveBundle(_arg0, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onReceiveFile(data.readInt(), data.createTypedArrayList(Uri.CREATOR));
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
        public static class Proxy implements IOnehopMagicWindowCallback {
            public static IOnehopMagicWindowCallback sDefaultImpl;
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

            @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
            public void onReceive(int msgType, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(msgType);
                    _data.writeString(msg);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceive(msgType, msg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
            public void onReceiveBundle(int msgType, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(msgType);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveBundle(msgType, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
            public void onReceiveFile(int msgType, List<Uri> uris) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(msgType);
                    _data.writeTypedList(uris);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveFile(msgType, uris);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOnehopMagicWindowCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOnehopMagicWindowCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
