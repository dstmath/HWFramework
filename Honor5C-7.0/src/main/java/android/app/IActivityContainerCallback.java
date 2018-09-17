package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IActivityContainerCallback extends IInterface {

    public static abstract class Stub extends Binder implements IActivityContainerCallback {
        private static final String DESCRIPTOR = "android.app.IActivityContainerCallback";
        static final int TRANSACTION_onAllActivitiesComplete = 2;
        static final int TRANSACTION_setVisible = 1;

        private static class Proxy implements IActivityContainerCallback {
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

            public void setVisible(IBinder container, boolean visible) throws RemoteException {
                int i = Stub.TRANSACTION_setVisible;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(container);
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setVisible, _data, null, Stub.TRANSACTION_setVisible);
                } finally {
                    _data.recycle();
                }
            }

            public void onAllActivitiesComplete(IBinder container) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(container);
                    this.mRemote.transact(Stub.TRANSACTION_onAllActivitiesComplete, _data, null, Stub.TRANSACTION_setVisible);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IActivityContainerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IActivityContainerCallback)) {
                return new Proxy(obj);
            }
            return (IActivityContainerCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg1 = false;
            switch (code) {
                case TRANSACTION_setVisible /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    setVisible(_arg0, _arg1);
                    return true;
                case TRANSACTION_onAllActivitiesComplete /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onAllActivitiesComplete(data.readStrongBinder());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAllActivitiesComplete(IBinder iBinder) throws RemoteException;

    void setVisible(IBinder iBinder, boolean z) throws RemoteException;
}
