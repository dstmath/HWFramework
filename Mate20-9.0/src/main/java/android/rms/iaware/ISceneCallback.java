package android.rms.iaware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISceneCallback extends IInterface {

    public static abstract class Stub extends Binder implements ISceneCallback {
        private static final String DESCRIPTOR = "android.rms.iaware.ISceneCallback";
        static final int TRANSACTION_onSceneChanged = 1;

        private static class Proxy implements ISceneCallback {
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

            public void onSceneChanged(int scene, boolean start, int uid, int pid, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scene);
                    _data.writeInt(start);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeString(pkg);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            if (i == 1) {
                Parcel parcel = reply;
                data.enforceInterface(DESCRIPTOR);
                onSceneChanged(data.readInt(), data.readInt() != 0, data.readInt(), data.readInt(), data.readString());
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onSceneChanged(int i, boolean z, int i2, int i3, String str) throws RemoteException;
}
