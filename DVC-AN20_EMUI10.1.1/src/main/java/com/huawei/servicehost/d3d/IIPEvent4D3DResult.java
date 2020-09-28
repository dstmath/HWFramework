package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.d3d.IImage3D;

public interface IIPEvent4D3DResult extends IInterface {
    IImage3D getResult() throws RemoteException;

    void release() throws RemoteException;

    public static class Default implements IIPEvent4D3DResult {
        @Override // com.huawei.servicehost.d3d.IIPEvent4D3DResult
        public IImage3D getResult() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.d3d.IIPEvent4D3DResult
        public void release() throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPEvent4D3DResult {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.IIPEvent4D3DResult";
        static final int TRANSACTION_getResult = 1;
        static final int TRANSACTION_release = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPEvent4D3DResult asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPEvent4D3DResult)) {
                return new Proxy(obj);
            }
            return (IIPEvent4D3DResult) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IImage3D _result = getResult();
                reply.writeNoException();
                reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                release();
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
        public static class Proxy implements IIPEvent4D3DResult {
            public static IIPEvent4D3DResult sDefaultImpl;
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

            @Override // com.huawei.servicehost.d3d.IIPEvent4D3DResult
            public IImage3D getResult() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getResult();
                    }
                    _reply.readException();
                    IImage3D _result = IImage3D.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.IIPEvent4D3DResult
            public void release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().release();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIPEvent4D3DResult impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPEvent4D3DResult getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
