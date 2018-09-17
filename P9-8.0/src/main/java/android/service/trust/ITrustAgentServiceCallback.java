package android.service.trust;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface ITrustAgentServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements ITrustAgentServiceCallback {
        private static final String DESCRIPTOR = "android.service.trust.ITrustAgentServiceCallback";
        static final int TRANSACTION_addEscrowToken = 5;
        static final int TRANSACTION_grantTrust = 1;
        static final int TRANSACTION_isEscrowTokenActive = 6;
        static final int TRANSACTION_onConfigureCompleted = 4;
        static final int TRANSACTION_removeEscrowToken = 7;
        static final int TRANSACTION_revokeTrust = 2;
        static final int TRANSACTION_setManagingTrust = 3;
        static final int TRANSACTION_unlockUserWithToken = 8;

        private static class Proxy implements ITrustAgentServiceCallback {
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void revokeTrust() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setManagingTrust(boolean managingTrust) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!managingTrust) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConfigureCompleted(boolean result, IBinder token) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void addEscrowToken(byte[] token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(token);
                    _data.writeInt(userId);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void isEscrowTokenActive(long handle, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void removeEscrowToken(long handle, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeInt(userId);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unlockUserWithToken(long handle, byte[] token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeByteArray(token);
                    _data.writeInt(userId);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    CharSequence _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
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
                    setManagingTrust(data.readInt() != 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onConfigureCompleted(data.readInt() != 0, data.readStrongBinder());
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
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addEscrowToken(byte[] bArr, int i) throws RemoteException;

    void grantTrust(CharSequence charSequence, long j, int i) throws RemoteException;

    void isEscrowTokenActive(long j, int i) throws RemoteException;

    void onConfigureCompleted(boolean z, IBinder iBinder) throws RemoteException;

    void removeEscrowToken(long j, int i) throws RemoteException;

    void revokeTrust() throws RemoteException;

    void setManagingTrust(boolean z) throws RemoteException;

    void unlockUserWithToken(long j, byte[] bArr, int i) throws RemoteException;
}
