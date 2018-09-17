package android.service.dreams;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDreamService extends IInterface {

    public static abstract class Stub extends Binder implements IDreamService {
        private static final String DESCRIPTOR = "android.service.dreams.IDreamService";
        static final int TRANSACTION_attach = 1;
        static final int TRANSACTION_detach = 2;
        static final int TRANSACTION_wakeUp = 3;

        private static class Proxy implements IDreamService {
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

            public void attach(IBinder windowToken, boolean canDoze) throws RemoteException {
                int i = Stub.TRANSACTION_attach;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(windowToken);
                    if (!canDoze) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_attach, _data, null, Stub.TRANSACTION_attach);
                } finally {
                    _data.recycle();
                }
            }

            public void detach() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_detach, _data, null, Stub.TRANSACTION_attach);
                } finally {
                    _data.recycle();
                }
            }

            public void wakeUp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_wakeUp, _data, null, Stub.TRANSACTION_attach);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDreamService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDreamService)) {
                return new Proxy(obj);
            }
            return (IDreamService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg1 = false;
            switch (code) {
                case TRANSACTION_attach /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    attach(_arg0, _arg1);
                    return true;
                case TRANSACTION_detach /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    detach();
                    return true;
                case TRANSACTION_wakeUp /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    wakeUp();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void attach(IBinder iBinder, boolean z) throws RemoteException;

    void detach() throws RemoteException;

    void wakeUp() throws RemoteException;
}
