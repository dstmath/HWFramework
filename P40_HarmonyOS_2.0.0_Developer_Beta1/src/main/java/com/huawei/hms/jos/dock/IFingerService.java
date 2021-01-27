package com.huawei.hms.jos.dock;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFingerService extends IInterface {
    void fingerAction(int i, String str, int i2, int i3) throws RemoteException;

    public static class Default implements IFingerService {
        @Override // com.huawei.hms.jos.dock.IFingerService
        public void fingerAction(int fingerDirection, String event, int rawX, int rawY) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFingerService {
        private static final String DESCRIPTOR = "com.huawei.hms.jos.dock.IFingerService";
        static final int TRANSACTION_fingerAction = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFingerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFingerService)) {
                return new Proxy(obj);
            }
            return (IFingerService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                fingerAction(data.readInt(), data.readString(), data.readInt(), data.readInt());
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
        public static class Proxy implements IFingerService {
            public static IFingerService sDefaultImpl;
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

            @Override // com.huawei.hms.jos.dock.IFingerService
            public void fingerAction(int fingerDirection, String event, int rawX, int rawY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerDirection);
                    _data.writeString(event);
                    _data.writeInt(rawX);
                    _data.writeInt(rawY);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fingerAction(fingerDirection, event, rawX, rawY);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFingerService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFingerService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
