package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageInstallerCallback extends IInterface {

    public static abstract class Stub extends Binder implements IPackageInstallerCallback {
        private static final String DESCRIPTOR = "android.content.pm.IPackageInstallerCallback";
        static final int TRANSACTION_onSessionActiveChanged = 3;
        static final int TRANSACTION_onSessionBadgingChanged = 2;
        static final int TRANSACTION_onSessionCreated = 1;
        static final int TRANSACTION_onSessionFinished = 5;
        static final int TRANSACTION_onSessionProgressChanged = 4;

        private static class Proxy implements IPackageInstallerCallback {
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

            public void onSessionCreated(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(Stub.TRANSACTION_onSessionCreated, _data, null, Stub.TRANSACTION_onSessionCreated);
                } finally {
                    _data.recycle();
                }
            }

            public void onSessionBadgingChanged(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(Stub.TRANSACTION_onSessionBadgingChanged, _data, null, Stub.TRANSACTION_onSessionCreated);
                } finally {
                    _data.recycle();
                }
            }

            public void onSessionActiveChanged(int sessionId, boolean active) throws RemoteException {
                int i = Stub.TRANSACTION_onSessionCreated;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!active) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onSessionActiveChanged, _data, null, Stub.TRANSACTION_onSessionCreated);
                } finally {
                    _data.recycle();
                }
            }

            public void onSessionProgressChanged(int sessionId, float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeFloat(progress);
                    this.mRemote.transact(Stub.TRANSACTION_onSessionProgressChanged, _data, null, Stub.TRANSACTION_onSessionCreated);
                } finally {
                    _data.recycle();
                }
            }

            public void onSessionFinished(int sessionId, boolean success) throws RemoteException {
                int i = Stub.TRANSACTION_onSessionCreated;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!success) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onSessionFinished, _data, null, Stub.TRANSACTION_onSessionCreated);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPackageInstallerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageInstallerCallback)) {
                return new Proxy(obj);
            }
            return (IPackageInstallerCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg1 = false;
            int _arg0;
            switch (code) {
                case TRANSACTION_onSessionCreated /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSessionCreated(data.readInt());
                    return true;
                case TRANSACTION_onSessionBadgingChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSessionBadgingChanged(data.readInt());
                    return true;
                case TRANSACTION_onSessionActiveChanged /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    onSessionActiveChanged(_arg0, _arg1);
                    return true;
                case TRANSACTION_onSessionProgressChanged /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSessionProgressChanged(data.readInt(), data.readFloat());
                    return true;
                case TRANSACTION_onSessionFinished /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    onSessionFinished(_arg0, _arg1);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onSessionActiveChanged(int i, boolean z) throws RemoteException;

    void onSessionBadgingChanged(int i) throws RemoteException;

    void onSessionCreated(int i) throws RemoteException;

    void onSessionFinished(int i, boolean z) throws RemoteException;

    void onSessionProgressChanged(int i, float f) throws RemoteException;
}
