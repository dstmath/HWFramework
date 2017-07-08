package android.service.trust;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import java.util.List;

public interface ITrustAgentService extends IInterface {

    public static abstract class Stub extends Binder implements ITrustAgentService {
        private static final String DESCRIPTOR = "android.service.trust.ITrustAgentService";
        static final int TRANSACTION_onConfigure = 5;
        static final int TRANSACTION_onDeviceLocked = 3;
        static final int TRANSACTION_onDeviceUnlocked = 4;
        static final int TRANSACTION_onTrustTimeout = 2;
        static final int TRANSACTION_onUnlockAttempt = 1;
        static final int TRANSACTION_setCallback = 6;

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
                int i = Stub.TRANSACTION_onUnlockAttempt;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!successful) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onUnlockAttempt, _data, null, Stub.TRANSACTION_onUnlockAttempt);
                } finally {
                    _data.recycle();
                }
            }

            public void onTrustTimeout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onTrustTimeout, _data, null, Stub.TRANSACTION_onUnlockAttempt);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeviceLocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onDeviceLocked, _data, null, Stub.TRANSACTION_onUnlockAttempt);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeviceUnlocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onDeviceUnlocked, _data, null, Stub.TRANSACTION_onUnlockAttempt);
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
                    this.mRemote.transact(Stub.TRANSACTION_onConfigure, _data, null, Stub.TRANSACTION_onUnlockAttempt);
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
                    this.mRemote.transact(Stub.TRANSACTION_setCallback, _data, null, Stub.TRANSACTION_onUnlockAttempt);
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
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_onUnlockAttempt /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onUnlockAttempt(_arg0);
                    return true;
                case TRANSACTION_onTrustTimeout /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onTrustTimeout();
                    return true;
                case TRANSACTION_onDeviceLocked /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDeviceLocked();
                    return true;
                case TRANSACTION_onDeviceUnlocked /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDeviceUnlocked();
                    return true;
                case TRANSACTION_onConfigure /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onConfigure(data.createTypedArrayList(PersistableBundle.CREATOR), data.readStrongBinder());
                    return true;
                case TRANSACTION_setCallback /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCallback(android.service.trust.ITrustAgentServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
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

    void onTrustTimeout() throws RemoteException;

    void onUnlockAttempt(boolean z) throws RemoteException;

    void setCallback(ITrustAgentServiceCallback iTrustAgentServiceCallback) throws RemoteException;
}
