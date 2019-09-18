package android.media.projection;

import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionWatcherCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMediaProjectionManager extends IInterface {

    public static abstract class Stub extends Binder implements IMediaProjectionManager {
        private static final String DESCRIPTOR = "android.media.projection.IMediaProjectionManager";
        static final int TRANSACTION_addCallback = 6;
        static final int TRANSACTION_createProjection = 2;
        static final int TRANSACTION_getActiveProjectionInfo = 4;
        static final int TRANSACTION_hasProjectionPermission = 1;
        static final int TRANSACTION_isValidMediaProjection = 3;
        static final int TRANSACTION_removeCallback = 7;
        static final int TRANSACTION_stopActiveProjection = 5;

        private static class Proxy implements IMediaProjectionManager {
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

            public boolean hasProjectionPermission(int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IMediaProjection createProjection(int uid, String packageName, int type, boolean permanentGrant) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeInt(type);
                    _data.writeInt(permanentGrant);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return IMediaProjection.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isValidMediaProjection(IMediaProjection projection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(projection != null ? projection.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MediaProjectionInfo getActiveProjectionInfo() throws RemoteException {
                MediaProjectionInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MediaProjectionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopActiveProjection() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addCallback(IMediaProjectionWatcherCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeCallback(IMediaProjectionWatcherCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
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

        public static IMediaProjectionManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaProjectionManager)) {
                return new Proxy(obj);
            }
            return (IMediaProjectionManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg3 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = hasProjectionPermission(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg1 = data.readString();
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        IMediaProjection _result2 = createProjection(_arg0, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = isValidMediaProjection(IMediaProjection.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        MediaProjectionInfo _result4 = getActiveProjectionInfo();
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        stopActiveProjection();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        addCallback(IMediaProjectionWatcherCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        removeCallback(IMediaProjectionWatcherCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addCallback(IMediaProjectionWatcherCallback iMediaProjectionWatcherCallback) throws RemoteException;

    IMediaProjection createProjection(int i, String str, int i2, boolean z) throws RemoteException;

    MediaProjectionInfo getActiveProjectionInfo() throws RemoteException;

    boolean hasProjectionPermission(int i, String str) throws RemoteException;

    boolean isValidMediaProjection(IMediaProjection iMediaProjection) throws RemoteException;

    void removeCallback(IMediaProjectionWatcherCallback iMediaProjectionWatcherCallback) throws RemoteException;

    void stopActiveProjection() throws RemoteException;
}
