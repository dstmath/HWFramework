package android.service.trust;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface ITrustAgentServiceCallback extends IInterface {
    void addEscrowToken(byte[] bArr, int i) throws RemoteException;

    void grantTrust(CharSequence charSequence, long j, int i) throws RemoteException;

    void isEscrowTokenActive(long j, int i) throws RemoteException;

    void onConfigureCompleted(boolean z, IBinder iBinder) throws RemoteException;

    void removeEscrowToken(long j, int i) throws RemoteException;

    void revokeTrust() throws RemoteException;

    void setManagingTrust(boolean z) throws RemoteException;

    void showKeyguardErrorMessage(CharSequence charSequence) throws RemoteException;

    void unlockUserWithToken(long j, byte[] bArr, int i) throws RemoteException;

    public static class Default implements ITrustAgentServiceCallback {
        @Override // android.service.trust.ITrustAgentServiceCallback
        public void grantTrust(CharSequence message, long durationMs, int flags) throws RemoteException {
        }

        @Override // android.service.trust.ITrustAgentServiceCallback
        public void revokeTrust() throws RemoteException {
        }

        @Override // android.service.trust.ITrustAgentServiceCallback
        public void setManagingTrust(boolean managingTrust) throws RemoteException {
        }

        @Override // android.service.trust.ITrustAgentServiceCallback
        public void onConfigureCompleted(boolean result, IBinder token) throws RemoteException {
        }

        @Override // android.service.trust.ITrustAgentServiceCallback
        public void addEscrowToken(byte[] token, int userId) throws RemoteException {
        }

        @Override // android.service.trust.ITrustAgentServiceCallback
        public void isEscrowTokenActive(long handle, int userId) throws RemoteException {
        }

        @Override // android.service.trust.ITrustAgentServiceCallback
        public void removeEscrowToken(long handle, int userId) throws RemoteException {
        }

        @Override // android.service.trust.ITrustAgentServiceCallback
        public void unlockUserWithToken(long handle, byte[] token, int userId) throws RemoteException {
        }

        @Override // android.service.trust.ITrustAgentServiceCallback
        public void showKeyguardErrorMessage(CharSequence message) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITrustAgentServiceCallback {
        private static final String DESCRIPTOR = "android.service.trust.ITrustAgentServiceCallback";
        static final int TRANSACTION_addEscrowToken = 5;
        static final int TRANSACTION_grantTrust = 1;
        static final int TRANSACTION_isEscrowTokenActive = 6;
        static final int TRANSACTION_onConfigureCompleted = 4;
        static final int TRANSACTION_removeEscrowToken = 7;
        static final int TRANSACTION_revokeTrust = 2;
        static final int TRANSACTION_setManagingTrust = 3;
        static final int TRANSACTION_showKeyguardErrorMessage = 9;
        static final int TRANSACTION_unlockUserWithToken = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustAgentServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustAgentServiceCallback)) {
                return new Proxy(obj);
            }
            return (ITrustAgentServiceCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "grantTrust";
                case 2:
                    return "revokeTrust";
                case 3:
                    return "setManagingTrust";
                case 4:
                    return "onConfigureCompleted";
                case 5:
                    return "addEscrowToken";
                case 6:
                    return "isEscrowTokenActive";
                case 7:
                    return "removeEscrowToken";
                case 8:
                    return "unlockUserWithToken";
                case 9:
                    return "showKeyguardErrorMessage";
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
            CharSequence _arg0;
            CharSequence _arg02;
            if (code != 1598968902) {
                boolean _arg03 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        grantTrust(_arg0, data.readLong(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        revokeTrust();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        setManagingTrust(_arg03);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        onConfigureCompleted(_arg03, data.readStrongBinder());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        addEscrowToken(data.createByteArray(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        isEscrowTokenActive(data.readLong(), data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        removeEscrowToken(data.readLong(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        unlockUserWithToken(data.readLong(), data.createByteArray(), data.readInt());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        showKeyguardErrorMessage(_arg02);
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
        public static class Proxy implements ITrustAgentServiceCallback {
            public static ITrustAgentServiceCallback sDefaultImpl;
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

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void grantTrust(CharSequence message, long durationMs, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (message != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(durationMs);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().grantTrust(message, durationMs, flags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void revokeTrust() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().revokeTrust();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void setManagingTrust(boolean managingTrust) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(managingTrust ? 1 : 0);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setManagingTrust(managingTrust);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void onConfigureCompleted(boolean result, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result ? 1 : 0);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConfigureCompleted(result, token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void addEscrowToken(byte[] token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(token);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addEscrowToken(token, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void isEscrowTokenActive(long handle, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().isEscrowTokenActive(handle, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void removeEscrowToken(long handle, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeEscrowToken(handle, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void unlockUserWithToken(long handle, byte[] token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeByteArray(token);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unlockUserWithToken(handle, token, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.trust.ITrustAgentServiceCallback
            public void showKeyguardErrorMessage(CharSequence message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (message != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showKeyguardErrorMessage(message);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITrustAgentServiceCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITrustAgentServiceCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
