package android.hardware.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFingerprintDaemonCallback extends IInterface {

    public static abstract class Stub extends Binder implements IFingerprintDaemonCallback {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintDaemonCallback";
        static final int TRANSACTION_onAcquired = 2;
        static final int TRANSACTION_onAuthenticated = 3;
        static final int TRANSACTION_onEnrollResult = 1;
        static final int TRANSACTION_onEnumerate = 6;
        static final int TRANSACTION_onError = 4;
        static final int TRANSACTION_onRemoved = 5;

        private static class Proxy implements IFingerprintDaemonCallback {
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

            public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeInt(remaining);
                    this.mRemote.transact(Stub.TRANSACTION_onEnrollResult, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAcquired(long deviceId, int acquiredInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(acquiredInfo);
                    this.mRemote.transact(Stub.TRANSACTION_onAcquired, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAuthenticated(long deviceId, int fingerId, int groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    this.mRemote.transact(Stub.TRANSACTION_onAuthenticated, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onError(long deviceId, int error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(error);
                    this.mRemote.transact(Stub.TRANSACTION_onError, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRemoved(long deviceId, int fingerId, int groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    this.mRemote.transact(Stub.TRANSACTION_onRemoved, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onEnumerate(long deviceId, int[] fingerIds, int[] groupIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeIntArray(fingerIds);
                    _data.writeIntArray(groupIds);
                    this.mRemote.transact(Stub.TRANSACTION_onEnumerate, _data, _reply, 0);
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

        public static IFingerprintDaemonCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFingerprintDaemonCallback)) {
                return new Proxy(obj);
            }
            return (IFingerprintDaemonCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onEnrollResult /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onEnrollResult(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onAcquired /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onAcquired(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onAuthenticated /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthenticated(data.readLong(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onError /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onError(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onRemoved /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRemoved(data.readLong(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onEnumerate /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onEnumerate(data.readLong(), data.createIntArray(), data.createIntArray());
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

    void onAcquired(long j, int i) throws RemoteException;

    void onAuthenticated(long j, int i, int i2) throws RemoteException;

    void onEnrollResult(long j, int i, int i2, int i3) throws RemoteException;

    void onEnumerate(long j, int[] iArr, int[] iArr2) throws RemoteException;

    void onError(long j, int i) throws RemoteException;

    void onRemoved(long j, int i, int i2) throws RemoteException;
}
