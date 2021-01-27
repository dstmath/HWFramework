package android.content.pm.dex;

import android.content.pm.dex.ISnapshotRuntimeProfileCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IArtManager extends IInterface {
    boolean isRuntimeProfilingEnabled(int i, String str) throws RemoteException;

    void snapshotRuntimeProfile(int i, String str, String str2, ISnapshotRuntimeProfileCallback iSnapshotRuntimeProfileCallback, String str3) throws RemoteException;

    public static class Default implements IArtManager {
        @Override // android.content.pm.dex.IArtManager
        public void snapshotRuntimeProfile(int profileType, String packageName, String codePath, ISnapshotRuntimeProfileCallback callback, String callingPackage) throws RemoteException {
        }

        @Override // android.content.pm.dex.IArtManager
        public boolean isRuntimeProfilingEnabled(int profileType, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IArtManager {
        private static final String DESCRIPTOR = "android.content.pm.dex.IArtManager";
        static final int TRANSACTION_isRuntimeProfilingEnabled = 2;
        static final int TRANSACTION_snapshotRuntimeProfile = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IArtManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IArtManager)) {
                return new Proxy(obj);
            }
            return (IArtManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "snapshotRuntimeProfile";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "isRuntimeProfilingEnabled";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                snapshotRuntimeProfile(data.readInt(), data.readString(), data.readString(), ISnapshotRuntimeProfileCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean isRuntimeProfilingEnabled = isRuntimeProfilingEnabled(data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(isRuntimeProfilingEnabled ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IArtManager {
            public static IArtManager sDefaultImpl;
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

            @Override // android.content.pm.dex.IArtManager
            public void snapshotRuntimeProfile(int profileType, String packageName, String codePath, ISnapshotRuntimeProfileCallback callback, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(profileType);
                    _data.writeString(packageName);
                    _data.writeString(codePath);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().snapshotRuntimeProfile(profileType, packageName, codePath, callback, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.dex.IArtManager
            public boolean isRuntimeProfilingEnabled(int profileType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(profileType);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRuntimeProfilingEnabled(profileType, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IArtManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IArtManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
