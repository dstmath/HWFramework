package android.service.trust;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.UserHandle;
import java.util.List;

public interface ITrustAgentService extends IInterface {

    public static abstract class Stub extends Binder implements ITrustAgentService {
        private static final String DESCRIPTOR = "android.service.trust.ITrustAgentService";
        static final int TRANSACTION_onConfigure = 6;
        static final int TRANSACTION_onDeviceLocked = 4;
        static final int TRANSACTION_onDeviceUnlocked = 5;
        static final int TRANSACTION_onEscrowTokenAdded = 8;
        static final int TRANSACTION_onEscrowTokenRemoved = 10;
        static final int TRANSACTION_onTokenStateReceived = 9;
        static final int TRANSACTION_onTrustTimeout = 3;
        static final int TRANSACTION_onUnlockAttempt = 1;
        static final int TRANSACTION_onUnlockLockout = 2;
        static final int TRANSACTION_setCallback = 7;

        private static class Proxy implements ITrustAgentService {
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

            public void onUnlockAttempt(boolean successful) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!successful) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onUnlockLockout(int timeoutMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeoutMs);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTrustTimeout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeviceLocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeviceUnlocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConfigure(List<PersistableBundle> options, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(options);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setCallback(ITrustAgentServiceCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onEscrowTokenAdded(byte[] token, long handle, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(token);
                    _data.writeLong(handle);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTokenStateReceived(long handle, int tokenState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeInt(tokenState);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onEscrowTokenRemoved(long handle, boolean successful) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    if (!successful) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustAgentService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustAgentService)) {
                return new Proxy(obj);
            }
            return (ITrustAgentService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onUnlockAttempt(data.readInt() != 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onUnlockLockout(data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onTrustTimeout();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onDeviceLocked();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onDeviceUnlocked();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onConfigure(data.createTypedArrayList(PersistableBundle.CREATOR), data.readStrongBinder());
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    setCallback(android.service.trust.ITrustAgentServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 8:
                    UserHandle _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _arg0 = data.createByteArray();
                    long _arg1 = data.readLong();
                    if (data.readInt() != 0) {
                        _arg2 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    onEscrowTokenAdded(_arg0, _arg1, _arg2);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    onTokenStateReceived(data.readLong(), data.readInt());
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    onEscrowTokenRemoved(data.readLong(), data.readInt() != 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onConfigure(List<PersistableBundle> list, IBinder iBinder) throws RemoteException;

    void onDeviceLocked() throws RemoteException;

    void onDeviceUnlocked() throws RemoteException;

    void onEscrowTokenAdded(byte[] bArr, long j, UserHandle userHandle) throws RemoteException;

    void onEscrowTokenRemoved(long j, boolean z) throws RemoteException;

    void onTokenStateReceived(long j, int i) throws RemoteException;

    void onTrustTimeout() throws RemoteException;

    void onUnlockAttempt(boolean z) throws RemoteException;

    void onUnlockLockout(int i) throws RemoteException;

    void setCallback(ITrustAgentServiceCallback iTrustAgentServiceCallback) throws RemoteException;
}
