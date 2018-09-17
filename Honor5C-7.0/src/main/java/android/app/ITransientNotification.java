package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITransientNotification extends IInterface {

    public static abstract class Stub extends Binder implements ITransientNotification {
        private static final String DESCRIPTOR = "android.app.ITransientNotification";
        static final int TRANSACTION_hide = 2;
        static final int TRANSACTION_show = 1;

        private static class Proxy implements ITransientNotification {
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

            public void show() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_show, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }

            public void hide() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hide, _data, null, Stub.TRANSACTION_show);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITransientNotification asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITransientNotification)) {
                return new Proxy(obj);
            }
            return (ITransientNotification) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_show /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    show();
                    return true;
                case TRANSACTION_hide /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    hide();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void hide() throws RemoteException;

    void show() throws RemoteException;
}
