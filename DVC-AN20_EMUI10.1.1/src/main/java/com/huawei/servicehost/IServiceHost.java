package com.huawei.servicehost;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.IBufferShareService;
import com.huawei.servicehost.IImageProcessService;
import com.huawei.servicehost.IServiceHostClient;

public interface IServiceHost extends IInterface {
    int connect(IServiceHostClient iServiceHostClient) throws RemoteException;

    int disconnect(IServiceHostClient iServiceHostClient) throws RemoteException;

    IBufferShareService getBufferShareService() throws RemoteException;

    IImageProcessService getImageProcessService() throws RemoteException;

    public static class Default implements IServiceHost {
        @Override // com.huawei.servicehost.IServiceHost
        public IBufferShareService getBufferShareService() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.IServiceHost
        public IImageProcessService getImageProcessService() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.IServiceHost
        public int connect(IServiceHostClient val) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.IServiceHost
        public int disconnect(IServiceHostClient val) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IServiceHost {
        private static final String DESCRIPTOR = "com.huawei.servicehost.IServiceHost";
        static final int TRANSACTION_connect = 3;
        static final int TRANSACTION_disconnect = 4;
        static final int TRANSACTION_getBufferShareService = 1;
        static final int TRANSACTION_getImageProcessService = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IServiceHost asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IServiceHost)) {
                return new Proxy(obj);
            }
            return (IServiceHost) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IBufferShareService _result = getBufferShareService();
                reply.writeNoException();
                if (_result != null) {
                    iBinder = _result.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                IImageProcessService _result2 = getImageProcessService();
                reply.writeNoException();
                if (_result2 != null) {
                    iBinder = _result2.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = connect(IServiceHostClient.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = disconnect(IServiceHostClient.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IServiceHost {
            public static IServiceHost sDefaultImpl;
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

            @Override // com.huawei.servicehost.IServiceHost
            public IBufferShareService getBufferShareService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBufferShareService();
                    }
                    _reply.readException();
                    IBufferShareService _result = IBufferShareService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IServiceHost
            public IImageProcessService getImageProcessService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImageProcessService();
                    }
                    _reply.readException();
                    IImageProcessService _result = IImageProcessService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IServiceHost
            public int connect(IServiceHostClient val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val != null ? val.asBinder() : null);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connect(val);
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

            @Override // com.huawei.servicehost.IServiceHost
            public int disconnect(IServiceHostClient val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val != null ? val.asBinder() : null);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnect(val);
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
        }

        public static boolean setDefaultImpl(IServiceHost impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IServiceHost getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
