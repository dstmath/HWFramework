package android.rms.iaware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwGameManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwGameManager {
        private static final String DESCRIPTOR = "android.rms.iaware.IHwGameManager";
        static final int TRANSACTION_noteGameProcessStarted = 1;
        static final int TRANSACTION_notifyGameScene = 2;

        private static class Proxy implements IHwGameManager {
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

            public boolean noteGameProcessStarted(int pid, int uid, IHwGameCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_noteGameProcessStarted, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int notifyGameScene(int gameScene, int cpuLevel, int gpuLevel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(gameScene);
                    _data.writeInt(cpuLevel);
                    _data.writeInt(gpuLevel);
                    this.mRemote.transact(Stub.TRANSACTION_notifyGameScene, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwGameManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwGameManager)) {
                return new Proxy(obj);
            }
            return (IHwGameManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_noteGameProcessStarted /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = noteGameProcessStarted(data.readInt(), data.readInt(), android.rms.iaware.IHwGameCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_noteGameProcessStarted : 0);
                    return true;
                case TRANSACTION_notifyGameScene /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = notifyGameScene(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean noteGameProcessStarted(int i, int i2, IHwGameCallback iHwGameCallback) throws RemoteException;

    int notifyGameScene(int i, int i2, int i3) throws RemoteException;
}
