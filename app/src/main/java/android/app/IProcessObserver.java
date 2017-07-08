package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IProcessObserver extends IInterface {

    public static abstract class Stub extends Binder implements IProcessObserver {
        private static final String DESCRIPTOR = "android.app.IProcessObserver";
        static final int TRANSACTION_onForegroundActivitiesChanged = 1;
        static final int TRANSACTION_onProcessDied = 3;
        static final int TRANSACTION_onProcessStateChanged = 2;

        private static class Proxy implements IProcessObserver {
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

            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
                int i = Stub.TRANSACTION_onForegroundActivitiesChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (!foregroundActivities) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onForegroundActivitiesChanged, _data, null, Stub.TRANSACTION_onForegroundActivitiesChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(procState);
                    this.mRemote.transact(Stub.TRANSACTION_onProcessStateChanged, _data, null, Stub.TRANSACTION_onForegroundActivitiesChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onProcessDied(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_onProcessDied, _data, null, Stub.TRANSACTION_onForegroundActivitiesChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IProcessObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IProcessObserver)) {
                return new Proxy(obj);
            }
            return (IProcessObserver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg2 = false;
            switch (code) {
                case TRANSACTION_onForegroundActivitiesChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    }
                    onForegroundActivitiesChanged(_arg0, _arg1, _arg2);
                    return true;
                case TRANSACTION_onProcessStateChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onProcessStateChanged(data.readInt(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onProcessDied /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onProcessDied(data.readInt(), data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onForegroundActivitiesChanged(int i, int i2, boolean z) throws RemoteException;

    void onProcessDied(int i, int i2) throws RemoteException;

    void onProcessStateChanged(int i, int i2, int i3) throws RemoteException;
}
