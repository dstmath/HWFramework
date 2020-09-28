package com.huawei.servicehost;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.IIPListener;
import com.huawei.servicehost.IIPRequest;

public interface IImageProcessSession extends IInterface {
    IBinder createIPObject(String str) throws RemoteException;

    IIPRequest createIPRequest(String str) throws RemoteException;

    int process(IIPRequest iIPRequest, boolean z) throws RemoteException;

    void setIPListener(IIPListener iIPListener) throws RemoteException;

    public static class Default implements IImageProcessSession {
        @Override // com.huawei.servicehost.IImageProcessSession
        public IIPRequest createIPRequest(String type) throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.IImageProcessSession
        public int process(IIPRequest val, boolean isSync) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.IImageProcessSession
        public void setIPListener(IIPListener val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.IImageProcessSession
        public IBinder createIPObject(String type) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImageProcessSession {
        private static final String DESCRIPTOR = "com.huawei.servicehost.IImageProcessSession";
        static final int TRANSACTION_createIPObject = 4;
        static final int TRANSACTION_createIPRequest = 1;
        static final int TRANSACTION_process = 2;
        static final int TRANSACTION_setIPListener = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImageProcessSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImageProcessSession)) {
                return new Proxy(obj);
            }
            return (IImageProcessSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IIPRequest _result = createIPRequest(data.readString());
                reply.writeNoException();
                reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = process(IIPRequest.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                setIPListener(IIPListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _result3 = createIPObject(data.readString());
                reply.writeNoException();
                reply.writeStrongBinder(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IImageProcessSession {
            public static IImageProcessSession sDefaultImpl;
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

            @Override // com.huawei.servicehost.IImageProcessSession
            public IIPRequest createIPRequest(String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(type);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createIPRequest(type);
                    }
                    _reply.readException();
                    IIPRequest _result = IIPRequest.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IImageProcessSession
            public int process(IIPRequest val, boolean isSync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val != null ? val.asBinder() : null);
                    _data.writeInt(isSync ? 1 : 0);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().process(val, isSync);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IImageProcessSession
            public void setIPListener(IIPListener val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val != null ? val.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIPListener(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IImageProcessSession
            public IBinder createIPObject(String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(type);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createIPObject(type);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImageProcessSession impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImageProcessSession getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
