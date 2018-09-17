package android.hardware.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFingerprintServiceReceiver extends IInterface {

    public static abstract class Stub extends Binder implements IFingerprintServiceReceiver {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintServiceReceiver";
        static final int TRANSACTION_onAcquired = 2;
        static final int TRANSACTION_onAuthenticationFailed = 4;
        static final int TRANSACTION_onAuthenticationSucceeded = 3;
        static final int TRANSACTION_onEnrollResult = 1;
        static final int TRANSACTION_onError = 5;
        static final int TRANSACTION_onRemoved = 6;

        private static class Proxy implements IFingerprintServiceReceiver {
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
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeInt(remaining);
                    this.mRemote.transact(Stub.TRANSACTION_onEnrollResult, _data, null, Stub.TRANSACTION_onEnrollResult);
                } finally {
                    _data.recycle();
                }
            }

            public void onAcquired(long deviceId, int acquiredInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(acquiredInfo);
                    this.mRemote.transact(Stub.TRANSACTION_onAcquired, _data, null, Stub.TRANSACTION_onEnrollResult);
                } finally {
                    _data.recycle();
                }
            }

            public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    if (fp != null) {
                        _data.writeInt(Stub.TRANSACTION_onEnrollResult);
                        fp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_onAuthenticationSucceeded, _data, null, Stub.TRANSACTION_onEnrollResult);
                } finally {
                    _data.recycle();
                }
            }

            public void onAuthenticationFailed(long deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    this.mRemote.transact(Stub.TRANSACTION_onAuthenticationFailed, _data, null, Stub.TRANSACTION_onEnrollResult);
                } finally {
                    _data.recycle();
                }
            }

            public void onError(long deviceId, int error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(error);
                    this.mRemote.transact(Stub.TRANSACTION_onError, _data, null, Stub.TRANSACTION_onEnrollResult);
                } finally {
                    _data.recycle();
                }
            }

            public void onRemoved(long deviceId, int fingerId, int groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    this.mRemote.transact(Stub.TRANSACTION_onRemoved, _data, null, Stub.TRANSACTION_onEnrollResult);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFingerprintServiceReceiver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFingerprintServiceReceiver)) {
                return new Proxy(obj);
            }
            return (IFingerprintServiceReceiver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onEnrollResult /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onEnrollResult(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onAcquired /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onAcquired(data.readLong(), data.readInt());
                    return true;
                case TRANSACTION_onAuthenticationSucceeded /*3*/:
                    Fingerprint fingerprint;
                    data.enforceInterface(DESCRIPTOR);
                    long _arg0 = data.readLong();
                    if (data.readInt() != 0) {
                        fingerprint = (Fingerprint) Fingerprint.CREATOR.createFromParcel(data);
                    } else {
                        fingerprint = null;
                    }
                    onAuthenticationSucceeded(_arg0, fingerprint, data.readInt());
                    return true;
                case TRANSACTION_onAuthenticationFailed /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthenticationFailed(data.readLong());
                    return true;
                case TRANSACTION_onError /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onError(data.readLong(), data.readInt());
                    return true;
                case TRANSACTION_onRemoved /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRemoved(data.readLong(), data.readInt(), data.readInt());
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

    void onAuthenticationFailed(long j) throws RemoteException;

    void onAuthenticationSucceeded(long j, Fingerprint fingerprint, int i) throws RemoteException;

    void onEnrollResult(long j, int i, int i2, int i3) throws RemoteException;

    void onError(long j, int i) throws RemoteException;

    void onRemoved(long j, int i, int i2) throws RemoteException;
}
