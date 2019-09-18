package android.hardware.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IContextHubClientCallback extends IInterface {

    public static abstract class Stub extends Binder implements IContextHubClientCallback {
        private static final String DESCRIPTOR = "android.hardware.location.IContextHubClientCallback";
        static final int TRANSACTION_onHubReset = 2;
        static final int TRANSACTION_onMessageFromNanoApp = 1;
        static final int TRANSACTION_onNanoAppAborted = 3;
        static final int TRANSACTION_onNanoAppDisabled = 7;
        static final int TRANSACTION_onNanoAppEnabled = 6;
        static final int TRANSACTION_onNanoAppLoaded = 4;
        static final int TRANSACTION_onNanoAppUnloaded = 5;

        private static class Proxy implements IContextHubClientCallback {
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

            public void onMessageFromNanoApp(NanoAppMessage message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (message != null) {
                        _data.writeInt(1);
                        message.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onHubReset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNanoAppAborted(long nanoAppId, int abortCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    _data.writeInt(abortCode);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNanoAppLoaded(long nanoAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNanoAppUnloaded(long nanoAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNanoAppEnabled(long nanoAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNanoAppDisabled(long nanoAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IContextHubClientCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IContextHubClientCallback)) {
                return new Proxy(obj);
            }
            return (IContextHubClientCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NanoAppMessage _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = NanoAppMessage.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onMessageFromNanoApp(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onHubReset();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppAborted(data.readLong(), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppLoaded(data.readLong());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppUnloaded(data.readLong());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppEnabled(data.readLong());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppDisabled(data.readLong());
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

    void onHubReset() throws RemoteException;

    void onMessageFromNanoApp(NanoAppMessage nanoAppMessage) throws RemoteException;

    void onNanoAppAborted(long j, int i) throws RemoteException;

    void onNanoAppDisabled(long j) throws RemoteException;

    void onNanoAppEnabled(long j) throws RemoteException;

    void onNanoAppLoaded(long j) throws RemoteException;

    void onNanoAppUnloaded(long j) throws RemoteException;
}
