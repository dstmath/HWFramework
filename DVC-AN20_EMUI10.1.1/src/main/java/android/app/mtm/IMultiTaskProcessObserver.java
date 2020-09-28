package android.app.mtm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMultiTaskProcessObserver extends IInterface {
    void onForegroundActivitiesChanged(int i, int i2, boolean z) throws RemoteException;

    void onProcessDied(int i, int i2) throws RemoteException;

    void onProcessStateChanged(int i, int i2, int i3) throws RemoteException;

    public static class Default implements IMultiTaskProcessObserver {
        @Override // android.app.mtm.IMultiTaskProcessObserver
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
        }

        @Override // android.app.mtm.IMultiTaskProcessObserver
        public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
        }

        @Override // android.app.mtm.IMultiTaskProcessObserver
        public void onProcessDied(int pid, int uid) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMultiTaskProcessObserver {
        private static final String DESCRIPTOR = "android.app.mtm.IMultiTaskProcessObserver";
        static final int TRANSACTION_onForegroundActivitiesChanged = 1;
        static final int TRANSACTION_onProcessDied = 3;
        static final int TRANSACTION_onProcessStateChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMultiTaskProcessObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMultiTaskProcessObserver)) {
                return new Proxy(obj);
            }
            return (IMultiTaskProcessObserver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onForegroundActivitiesChanged(data.readInt(), data.readInt(), data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onProcessStateChanged(data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onProcessDied(data.readInt(), data.readInt());
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
        public static class Proxy implements IMultiTaskProcessObserver {
            public static IMultiTaskProcessObserver sDefaultImpl;
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

            @Override // android.app.mtm.IMultiTaskProcessObserver
            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(foregroundActivities ? 1 : 0);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onForegroundActivitiesChanged(pid, uid, foregroundActivities);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskProcessObserver
            public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(procState);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onProcessStateChanged(pid, uid, procState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskProcessObserver
            public void onProcessDied(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onProcessDied(pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMultiTaskProcessObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMultiTaskProcessObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
