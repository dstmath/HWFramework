package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IAppClientMonitor extends IInterface {
    void startMonitor(List list) throws RemoteException;

    void stopMonitor() throws RemoteException;

    public static abstract class Stub extends Binder implements IAppClientMonitor {
        private static final String DESCRIPTOR = "huawei.android.security.IAppClientMonitor";
        static final int TRANSACTION_startMonitor = 1;
        static final int TRANSACTION_stopMonitor = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppClientMonitor asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppClientMonitor)) {
                return new Proxy(obj);
            }
            return (IAppClientMonitor) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                startMonitor(data.readArrayList(getClass().getClassLoader()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                stopMonitor();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAppClientMonitor {
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

            @Override // huawei.android.security.IAppClientMonitor
            public void startMonitor(List signatures) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeList(signatures);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IAppClientMonitor
            public void stopMonitor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
