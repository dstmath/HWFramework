package com.huawei.onehopassistantsdk;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback;
import java.util.List;

public interface IOnehopMagicWindowInterface extends IInterface {
    void registerCallback(IOnehopMagicWindowCallback iOnehopMagicWindowCallback) throws RemoteException;

    void send(int i, String str) throws RemoteException;

    void sendBundle(int i, Bundle bundle) throws RemoteException;

    void sendFile(int i, List<Uri> list) throws RemoteException;

    void unRegisterCallback(IOnehopMagicWindowCallback iOnehopMagicWindowCallback) throws RemoteException;

    public static class Default implements IOnehopMagicWindowInterface {
        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
        public void registerCallback(IOnehopMagicWindowCallback callback) throws RemoteException {
        }

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
        public void unRegisterCallback(IOnehopMagicWindowCallback callback) throws RemoteException {
        }

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
        public void send(int msgType, String msg) throws RemoteException {
        }

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
        public void sendBundle(int msgType, Bundle data) throws RemoteException {
        }

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
        public void sendFile(int msgType, List<Uri> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOnehopMagicWindowInterface {
        private static final String DESCRIPTOR = "com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface";
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_send = 3;
        static final int TRANSACTION_sendBundle = 4;
        static final int TRANSACTION_sendFile = 5;
        static final int TRANSACTION_unRegisterCallback = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnehopMagicWindowInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnehopMagicWindowInterface)) {
                return new Proxy(obj);
            }
            return (IOnehopMagicWindowInterface) iin;
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
                registerCallback(IOnehopMagicWindowCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                unRegisterCallback(IOnehopMagicWindowCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                send(data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                sendBundle(_arg0, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                sendFile(data.readInt(), data.createTypedArrayList(Uri.CREATOR));
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
        public static class Proxy implements IOnehopMagicWindowInterface {
            public static IOnehopMagicWindowInterface sDefaultImpl;
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

            @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
            public void registerCallback(IOnehopMagicWindowCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
            public void unRegisterCallback(IOnehopMagicWindowCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unRegisterCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
            public void send(int msgType, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(msgType);
                    _data.writeString(msg);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().send(msgType, msg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
            public void sendBundle(int msgType, Bundle data) throws RemoteException {
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
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendBundle(msgType, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface
            public void sendFile(int msgType, List<Uri> uris) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(msgType);
                    _data.writeTypedList(uris);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendFile(msgType, uris);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOnehopMagicWindowInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOnehopMagicWindowInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
