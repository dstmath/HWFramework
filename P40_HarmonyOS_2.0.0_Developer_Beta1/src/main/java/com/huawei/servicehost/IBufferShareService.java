package com.huawei.servicehost;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.huawei.servicehost.IImageAllocator;
import com.huawei.servicehost.IImageConsumer;
import com.huawei.servicehost.IImageProducer;

public interface IBufferShareService extends IInterface {
    IImageConsumer createImageConsumer(int i) throws RemoteException;

    IImageProducer createImageProducer(Surface surface) throws RemoteException;

    int getDefaultUsage() throws RemoteException;

    IImageAllocator getImageAllocator() throws RemoteException;

    public static class Default implements IBufferShareService {
        @Override // com.huawei.servicehost.IBufferShareService
        public IImageConsumer createImageConsumer(int type) throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.IBufferShareService
        public IImageProducer createImageProducer(Surface val) throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.IBufferShareService
        public IImageAllocator getImageAllocator() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.IBufferShareService
        public int getDefaultUsage() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBufferShareService {
        private static final String DESCRIPTOR = "com.huawei.servicehost.IBufferShareService";
        static final int TRANSACTION_createImageConsumer = 1;
        static final int TRANSACTION_createImageProducer = 2;
        static final int TRANSACTION_getDefaultUsage = 4;
        static final int TRANSACTION_getImageAllocator = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBufferShareService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBufferShareService)) {
                return new Proxy(obj);
            }
            return (IBufferShareService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Surface _arg0;
            IBinder iBinder = null;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IImageConsumer _result = createImageConsumer(data.readInt());
                reply.writeNoException();
                if (_result != null) {
                    iBinder = _result.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (Surface) Surface.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                IImageProducer _result2 = createImageProducer(_arg0);
                reply.writeNoException();
                if (_result2 != null) {
                    iBinder = _result2.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                IImageAllocator _result3 = getImageAllocator();
                reply.writeNoException();
                if (_result3 != null) {
                    iBinder = _result3.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = getDefaultUsage();
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
        public static class Proxy implements IBufferShareService {
            public static IBufferShareService sDefaultImpl;
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

            @Override // com.huawei.servicehost.IBufferShareService
            public IImageConsumer createImageConsumer(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createImageConsumer(type);
                    }
                    _reply.readException();
                    IImageConsumer _result = IImageConsumer.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IBufferShareService
            public IImageProducer createImageProducer(Surface val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (val != null) {
                        _data.writeInt(1);
                        val.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createImageProducer(val);
                    }
                    _reply.readException();
                    IImageProducer _result = IImageProducer.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IBufferShareService
            public IImageAllocator getImageAllocator() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImageAllocator();
                    }
                    _reply.readException();
                    IImageAllocator _result = IImageAllocator.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IBufferShareService
            public int getDefaultUsage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultUsage();
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

        public static boolean setDefaultImpl(IBufferShareService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBufferShareService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
