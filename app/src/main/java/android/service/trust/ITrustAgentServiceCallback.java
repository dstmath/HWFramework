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
        static final int TRANSACTION_grantTrust = 1;
        static final int TRANSACTION_onConfigureCompleted = 4;
        static final int TRANSACTION_revokeTrust = 2;
        static final int TRANSACTION_setManagingTrust = 3;

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
                        _data.writeInt(Stub.TRANSACTION_grantTrust);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(durationMs);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_grantTrust, _data, null, Stub.TRANSACTION_grantTrust);
                } finally {
                    _data.recycle();
                }
            }

            public void revokeTrust() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_revokeTrust, _data, null, Stub.TRANSACTION_grantTrust);
                } finally {
                    _data.recycle();
                }
            }

            public void setManagingTrust(boolean managingTrust) throws RemoteException {
                int i = Stub.TRANSACTION_grantTrust;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!managingTrust) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setManagingTrust, _data, null, Stub.TRANSACTION_grantTrust);
                } finally {
                    _data.recycle();
                }
            }

            public void onConfigureCompleted(boolean result, IBinder token) throws RemoteException {
                int i = Stub.TRANSACTION_grantTrust;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_onConfigureCompleted, _data, null, Stub.TRANSACTION_grantTrust);
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
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_grantTrust /*1*/:
                    CharSequence charSequence;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence = null;
                    }
                    grantTrust(charSequence, data.readLong(), data.readInt());
                    return true;
                case TRANSACTION_revokeTrust /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    revokeTrust();
                    return true;
                case TRANSACTION_setManagingTrust /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    setManagingTrust(_arg0);
                    return true;
                case TRANSACTION_onConfigureCompleted /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onConfigureCompleted(data.readInt() != 0, data.readStrongBinder());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void grantTrust(CharSequence charSequence, long j, int i) throws RemoteException;

    void onConfigureCompleted(boolean z, IBinder iBinder) throws RemoteException;

    void revokeTrust() throws RemoteException;

    void setManagingTrust(boolean z) throws RemoteException;
}
