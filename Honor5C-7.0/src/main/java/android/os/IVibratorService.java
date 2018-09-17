package android.os;

public interface IVibratorService extends IInterface {

    public static abstract class Stub extends Binder implements IVibratorService {
        private static final String DESCRIPTOR = "android.os.IVibratorService";
        static final int TRANSACTION_cancelVibrate = 4;
        static final int TRANSACTION_hasVibrator = 1;
        static final int TRANSACTION_hwVibrate = 5;
        static final int TRANSACTION_vibrate = 2;
        static final int TRANSACTION_vibratePattern = 3;

        private static class Proxy implements IVibratorService {
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

            public boolean hasVibrator() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hasVibrator, _data, _reply, 0);
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

            public void vibrate(int uid, String opPkg, long milliseconds, int usageHint, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    _data.writeLong(milliseconds);
                    _data.writeInt(usageHint);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_vibrate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void vibratePattern(int uid, String opPkg, long[] pattern, int repeat, int usageHint, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    _data.writeLongArray(pattern);
                    _data.writeInt(repeat);
                    _data.writeInt(usageHint);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_vibratePattern, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelVibrate(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_cancelVibrate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hwVibrate(int uid, String opPkg, int usageHint, IBinder token, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    _data.writeInt(usageHint);
                    _data.writeStrongBinder(token);
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_hwVibrate, _data, _reply, 0);
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

        public static IVibratorService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVibratorService)) {
                return new Proxy(obj);
            }
            return (IVibratorService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_hasVibrator /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = hasVibrator();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVibrator : 0);
                    return true;
                case TRANSACTION_vibrate /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    vibrate(data.readInt(), data.readString(), data.readLong(), data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_vibratePattern /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    String readString = data.readString();
                    vibratePattern(_arg0, _arg1, data.createLongArray(), data.readInt(), data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelVibrate /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelVibrate(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_hwVibrate /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    hwVibrate(data.readInt(), data.readString(), data.readInt(), data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void cancelVibrate(IBinder iBinder) throws RemoteException;

    boolean hasVibrator() throws RemoteException;

    void hwVibrate(int i, String str, int i2, IBinder iBinder, int i3) throws RemoteException;

    void vibrate(int i, String str, long j, int i2, IBinder iBinder) throws RemoteException;

    void vibratePattern(int i, String str, long[] jArr, int i2, int i3, IBinder iBinder) throws RemoteException;
}
