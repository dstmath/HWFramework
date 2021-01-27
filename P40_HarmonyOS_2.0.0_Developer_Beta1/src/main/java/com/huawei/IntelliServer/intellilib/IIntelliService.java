package com.huawei.IntelliServer.intellilib;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.IntelliServer.intellilib.IIntelliListener;

public interface IIntelliService extends IInterface {
    int isRegisted(int i) throws RemoteException;

    void registFaceId(int i, IntelliImgDescriptor intelliImgDescriptor) throws RemoteException;

    int registListener(int i, IIntelliListener iIntelliListener) throws RemoteException;

    void removeFaceId(int i) throws RemoteException;

    int unregistListener(int i, IIntelliListener iIntelliListener) throws RemoteException;

    public static abstract class Stub extends Binder implements IIntelliService {
        private static final String DESCRIPTOR = "com.huawei.IntelliServer.intellilib.IIntelliService";
        static final int TRANSACTION_isRegisted = 3;
        static final int TRANSACTION_registFaceId = 4;
        static final int TRANSACTION_registListener = 1;
        static final int TRANSACTION_removeFaceId = 5;
        static final int TRANSACTION_unregistListener = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIntelliService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIntelliService)) {
                return new Proxy(obj);
            }
            return (IIntelliService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IntelliImgDescriptor _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = registListener(data.readInt(), IIntelliListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = unregistListener(data.readInt(), IIntelliListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = isRegisted(data.readInt());
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = IntelliImgDescriptor.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                registFaceId(_arg0, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                removeFaceId(data.readInt());
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
        public static class Proxy implements IIntelliService {
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

            @Override // com.huawei.IntelliServer.intellilib.IIntelliService
            public int registListener(int type, IIntelliListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.IntelliServer.intellilib.IIntelliService
            public int unregistListener(int type, IIntelliListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.IntelliServer.intellilib.IIntelliService
            public int isRegisted(int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.IntelliServer.intellilib.IIntelliService
            public void registFaceId(int id, IntelliImgDescriptor image) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    if (image != null) {
                        _data.writeInt(1);
                        image.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.IntelliServer.intellilib.IIntelliService
            public void removeFaceId(int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
