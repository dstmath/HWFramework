package android.hardware.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFingerprintServiceReceiver extends IInterface {
    void onAcquired(long j, int i, int i2) throws RemoteException;

    void onAuthenticationFailed(long j) throws RemoteException;

    void onAuthenticationSucceeded(long j, Fingerprint fingerprint, int i) throws RemoteException;

    void onEnrollResult(long j, int i, int i2, int i3) throws RemoteException;

    void onEnumerated(long j, int i, int i2, int i3) throws RemoteException;

    void onError(long j, int i, int i2) throws RemoteException;

    void onRemoved(long j, int i, int i2, int i3) throws RemoteException;

    public static class Default implements IFingerprintServiceReceiver {
        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onAuthenticationFailed(long deviceId) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onError(long deviceId, int error, int vendorCode) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onEnumerated(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFingerprintServiceReceiver {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintServiceReceiver";
        static final int TRANSACTION_onAcquired = 2;
        static final int TRANSACTION_onAuthenticationFailed = 4;
        static final int TRANSACTION_onAuthenticationSucceeded = 3;
        static final int TRANSACTION_onEnrollResult = 1;
        static final int TRANSACTION_onEnumerated = 7;
        static final int TRANSACTION_onError = 5;
        static final int TRANSACTION_onRemoved = 6;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onEnrollResult";
                case 2:
                    return "onAcquired";
                case 3:
                    return "onAuthenticationSucceeded";
                case 4:
                    return "onAuthenticationFailed";
                case 5:
                    return "onError";
                case 6:
                    return "onRemoved";
                case 7:
                    return "onEnumerated";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Fingerprint _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onEnrollResult(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onAcquired(data.readLong(), data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg0 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg1 = Fingerprint.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onAuthenticationSucceeded(_arg0, _arg1, data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onAuthenticationFailed(data.readLong());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readLong(), data.readInt(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onRemoved(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
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

        /* access modifiers changed from: private */
        public static class Proxy implements IFingerprintServiceReceiver {
            public static IFingerprintServiceReceiver sDefaultImpl;
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

            @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
            public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeInt(remaining);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEnrollResult(deviceId, fingerId, groupId, remaining);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
            public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(acquiredInfo);
                    _data.writeInt(vendorCode);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAcquired(deviceId, acquiredInfo, vendorCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
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
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthenticationSucceeded(deviceId, fp, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
            public void onAuthenticationFailed(long deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthenticationFailed(deviceId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
            public void onError(long deviceId, int error, int vendorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(error);
                    _data.writeInt(vendorCode);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(deviceId, error, vendorCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
            public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeInt(remaining);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRemoved(deviceId, fingerId, groupId, remaining);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
            public void onEnumerated(long deviceId, int fingerId, int groupId, int remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeInt(remaining);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEnumerated(deviceId, fingerId, groupId, remaining);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFingerprintServiceReceiver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFingerprintServiceReceiver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
