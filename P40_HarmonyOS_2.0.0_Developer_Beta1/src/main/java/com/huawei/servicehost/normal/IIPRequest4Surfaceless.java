package com.huawei.servicehost.normal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.huawei.servicehost.ImageDescriptor;

public interface IIPRequest4Surfaceless extends IInterface {
    void setPreview1Surface(Surface surface) throws RemoteException;

    void uselessFunction(ImageDescriptor imageDescriptor) throws RemoteException;

    public static class Default implements IIPRequest4Surfaceless {
        @Override // com.huawei.servicehost.normal.IIPRequest4Surfaceless
        public void setPreview1Surface(Surface val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4Surfaceless
        public void uselessFunction(ImageDescriptor val) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPRequest4Surfaceless {
        private static final String DESCRIPTOR = "com.huawei.servicehost.normal.IIPRequest4Surfaceless";
        static final int TRANSACTION_setPreview1Surface = 1;
        static final int TRANSACTION_uselessFunction = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPRequest4Surfaceless asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPRequest4Surfaceless)) {
                return new Proxy(obj);
            }
            return (IIPRequest4Surfaceless) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Surface _arg0;
            ImageDescriptor _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (Surface) Surface.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                setPreview1Surface(_arg0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = ImageDescriptor.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                uselessFunction(_arg02);
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
        public static class Proxy implements IIPRequest4Surfaceless {
            public static IIPRequest4Surfaceless sDefaultImpl;
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

            @Override // com.huawei.servicehost.normal.IIPRequest4Surfaceless
            public void setPreview1Surface(Surface val) throws RemoteException {
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
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPreview1Surface(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4Surfaceless
            public void uselessFunction(ImageDescriptor val) throws RemoteException {
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
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().uselessFunction(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIPRequest4Surfaceless impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPRequest4Surfaceless getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
