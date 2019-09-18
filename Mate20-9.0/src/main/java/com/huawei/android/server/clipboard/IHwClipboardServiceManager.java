package com.huawei.android.server.clipboard;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.content.IOnPrimaryClipGetedListener;

public interface IHwClipboardServiceManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwClipboardServiceManager {
        private static final String DESCRIPTOR = "com.huawei.android.server.clipboard.IHwClipboardServiceManager";
        static final int TRANSACTION_addPrimaryClipGetedListener = 1;
        static final int TRANSACTION_removePrimaryClipGetedListener = 2;
        static final int TRANSACTION_setGetWaitTime = 3;

        private static class Proxy implements IHwClipboardServiceManager {
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

            public void addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setGetWaitTime(int waitTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(waitTime);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwClipboardServiceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwClipboardServiceManager)) {
                return new Proxy(obj);
            }
            return (IHwClipboardServiceManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        addPrimaryClipGetedListener(IOnPrimaryClipGetedListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        removePrimaryClipGetedListener(IOnPrimaryClipGetedListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setGetWaitTime(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addPrimaryClipGetedListener(IOnPrimaryClipGetedListener iOnPrimaryClipGetedListener, String str) throws RemoteException;

    void removePrimaryClipGetedListener(IOnPrimaryClipGetedListener iOnPrimaryClipGetedListener) throws RemoteException;

    void setGetWaitTime(int i) throws RemoteException;
}
