package com.mediatek.ims.impl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.ims.impl.IMtkMapconServiceCallback;

public interface IMtkMapconService extends IInterface {
    void notifyWifiOff(IMtkMapconServiceCallback iMtkMapconServiceCallback) throws RemoteException;

    public static class Default implements IMtkMapconService {
        @Override // com.mediatek.ims.impl.IMtkMapconService
        public void notifyWifiOff(IMtkMapconServiceCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkMapconService {
        private static final String DESCRIPTOR = "com.mediatek.ims.impl.IMtkMapconService";
        static final int TRANSACTION_notifyWifiOff = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkMapconService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkMapconService)) {
                return new Proxy(obj);
            }
            return (IMtkMapconService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                notifyWifiOff(IMtkMapconServiceCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IMtkMapconService {
            public static IMtkMapconService sDefaultImpl;
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

            @Override // com.mediatek.ims.impl.IMtkMapconService
            public void notifyWifiOff(IMtkMapconServiceCallback callback) throws RemoteException {
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
                    Stub.getDefaultImpl().notifyWifiOff(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkMapconService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkMapconService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
