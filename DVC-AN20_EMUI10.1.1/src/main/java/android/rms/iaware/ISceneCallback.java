package android.rms.iaware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISceneCallback extends IInterface {
    void onSceneChanged(int i, boolean z, int i2, int i3, String str) throws RemoteException;

    public static class Default implements ISceneCallback {
        @Override // android.rms.iaware.ISceneCallback
        public void onSceneChanged(int scene, boolean start, int uid, int pid, String pkg) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISceneCallback {
        private static final String DESCRIPTOR = "android.rms.iaware.ISceneCallback";
        static final int TRANSACTION_onSceneChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISceneCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISceneCallback)) {
                return new Proxy(obj);
            }
            return (ISceneCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onSceneChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onSceneChanged(data.readInt(), data.readInt() != 0, data.readInt(), data.readInt(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISceneCallback {
            public static ISceneCallback sDefaultImpl;
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

            @Override // android.rms.iaware.ISceneCallback
            public void onSceneChanged(int scene, boolean start, int uid, int pid, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scene);
                    _data.writeInt(start ? 1 : 0);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeString(pkg);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSceneChanged(scene, start, uid, pid, pkg);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISceneCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISceneCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
