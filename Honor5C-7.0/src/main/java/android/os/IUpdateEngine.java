package android.os;

public interface IUpdateEngine extends IInterface {

    public static abstract class Stub extends Binder implements IUpdateEngine {
        private static final String DESCRIPTOR = "android.os.IUpdateEngine";
        static final int TRANSACTION_applyPayload = 1;
        static final int TRANSACTION_applyUpdateZip = 2;
        static final int TRANSACTION_bind = 3;
        static final int TRANSACTION_cancel = 6;
        static final int TRANSACTION_getProgress = 7;
        static final int TRANSACTION_getStatus = 8;
        static final int TRANSACTION_resetStatus = 9;
        static final int TRANSACTION_resume = 5;
        static final int TRANSACTION_setSlot = 10;
        static final int TRANSACTION_suspend = 4;

        private static class Proxy implements IUpdateEngine {
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

            public void applyPayload(String url, long payload_offset, long payload_size, String[] headerKeyValuePairs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(url);
                    _data.writeLong(payload_offset);
                    _data.writeLong(payload_size);
                    _data.writeStringArray(headerKeyValuePairs);
                    this.mRemote.transact(Stub.TRANSACTION_applyPayload, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyUpdateZip() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_applyUpdateZip, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean bind(IUpdateEngineCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_bind, _data, _reply, 0);
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

            public void suspend() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_suspend, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resume() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_resume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_cancel, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getProgress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getProgress, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStatus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_resetStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSlot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_setSlot, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUpdateEngine asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUpdateEngine)) {
                return new Proxy(obj);
            }
            return (IUpdateEngine) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            switch (code) {
                case TRANSACTION_applyPayload /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    applyPayload(data.readString(), data.readLong(), data.readLong(), data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_applyUpdateZip /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    applyUpdateZip();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_bind /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = bind(android.os.IUpdateEngineCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_applyPayload : 0);
                    return true;
                case TRANSACTION_suspend /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    suspend();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resume /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    resume();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancel /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancel();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getProgress /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getProgress();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getStatus /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getStatus();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_resetStatus /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    resetStatus();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSlot /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setSlot();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_applyPayload : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void applyPayload(String str, long j, long j2, String[] strArr) throws RemoteException;

    void applyUpdateZip() throws RemoteException;

    boolean bind(IUpdateEngineCallback iUpdateEngineCallback) throws RemoteException;

    void cancel() throws RemoteException;

    int getProgress() throws RemoteException;

    int getStatus() throws RemoteException;

    void resetStatus() throws RemoteException;

    void resume() throws RemoteException;

    boolean setSlot() throws RemoteException;

    void suspend() throws RemoteException;
}
