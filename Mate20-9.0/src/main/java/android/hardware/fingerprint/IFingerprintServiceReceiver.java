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
        static final int TRANSACTION_onEnumerated = 7;
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(acquiredInfo);
                    _data.writeInt(vendorCode);
                    this.mRemote.transact(2, _data, null, 1);
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
                        _data.writeInt(1);
                        fp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAuthenticationFailed(long deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onError(long deviceId, int error, int vendorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(error);
                    _data.writeInt(vendorCode);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeInt(remaining);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onEnumerated(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeInt(remaining);
                    this.mRemote.transact(7, _data, null, 1);
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
            Fingerprint _arg1;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        onEnrollResult(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        onAcquired(data.readLong(), data.readInt(), data.readInt());
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _arg0 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg1 = Fingerprint.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        onAuthenticationSucceeded(_arg0, _arg1, data.readInt());
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        onAuthenticationFailed(data.readLong());
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        onError(data.readLong(), data.readInt(), data.readInt());
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRemoved(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        onEnumerated(data.readLong(), data.readInt(), data.readInt(), data.readInt());
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

    void onAcquired(long j, int i, int i2) throws RemoteException;

    void onAuthenticationFailed(long j) throws RemoteException;

    void onAuthenticationSucceeded(long j, Fingerprint fingerprint, int i) throws RemoteException;

    void onEnrollResult(long j, int i, int i2, int i3) throws RemoteException;

    void onEnumerated(long j, int i, int i2, int i3) throws RemoteException;

    void onError(long j, int i, int i2) throws RemoteException;

    void onRemoved(long j, int i, int i2, int i3) throws RemoteException;
}
