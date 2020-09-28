package android.trustspace;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface ITrustSpaceManager extends IInterface {
    boolean addIntentProtectedApps(List<String> list, int i) throws RemoteException;

    List<String> getIntentProtectedApps(int i) throws RemoteException;

    int getProtectionLevel(String str) throws RemoteException;

    boolean isHwTrustSpace(int i) throws RemoteException;

    boolean isIntentProtectedApp(String str) throws RemoteException;

    boolean isSupportTrustSpace() throws RemoteException;

    boolean removeIntentProtectedApp(String str) throws RemoteException;

    boolean removeIntentProtectedApps(List<String> list, int i) throws RemoteException;

    boolean updateTrustApps(List<String> list, int i) throws RemoteException;

    public static class Default implements ITrustSpaceManager {
        @Override // android.trustspace.ITrustSpaceManager
        public boolean addIntentProtectedApps(List<String> list, int flags) throws RemoteException {
            return false;
        }

        @Override // android.trustspace.ITrustSpaceManager
        public boolean removeIntentProtectedApp(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.trustspace.ITrustSpaceManager
        public List<String> getIntentProtectedApps(int flags) throws RemoteException {
            return null;
        }

        @Override // android.trustspace.ITrustSpaceManager
        public boolean removeIntentProtectedApps(List<String> list, int flags) throws RemoteException {
            return false;
        }

        @Override // android.trustspace.ITrustSpaceManager
        public boolean updateTrustApps(List<String> list, int flag) throws RemoteException {
            return false;
        }

        @Override // android.trustspace.ITrustSpaceManager
        public boolean isIntentProtectedApp(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.trustspace.ITrustSpaceManager
        public int getProtectionLevel(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.trustspace.ITrustSpaceManager
        public boolean isSupportTrustSpace() throws RemoteException {
            return false;
        }

        @Override // android.trustspace.ITrustSpaceManager
        public boolean isHwTrustSpace(int userId) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITrustSpaceManager {
        private static final String DESCRIPTOR = "android.trustspace.ITrustSpaceManager";
        static final int TRANSACTION_addIntentProtectedApps = 1;
        static final int TRANSACTION_getIntentProtectedApps = 3;
        static final int TRANSACTION_getProtectionLevel = 7;
        static final int TRANSACTION_isHwTrustSpace = 9;
        static final int TRANSACTION_isIntentProtectedApp = 6;
        static final int TRANSACTION_isSupportTrustSpace = 8;
        static final int TRANSACTION_removeIntentProtectedApp = 2;
        static final int TRANSACTION_removeIntentProtectedApps = 4;
        static final int TRANSACTION_updateTrustApps = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustSpaceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustSpaceManager)) {
                return new Proxy(obj);
            }
            return (ITrustSpaceManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addIntentProtectedApps = addIntentProtectedApps(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(addIntentProtectedApps ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeIntentProtectedApp = removeIntentProtectedApp(data.readString());
                        reply.writeNoException();
                        reply.writeInt(removeIntentProtectedApp ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getIntentProtectedApps(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeIntentProtectedApps = removeIntentProtectedApps(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeIntentProtectedApps ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateTrustApps = updateTrustApps(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(updateTrustApps ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIntentProtectedApp = isIntentProtectedApp(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isIntentProtectedApp ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getProtectionLevel(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSupportTrustSpace = isSupportTrustSpace();
                        reply.writeNoException();
                        reply.writeInt(isSupportTrustSpace ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHwTrustSpace = isHwTrustSpace(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isHwTrustSpace ? 1 : 0);
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
        public static class Proxy implements ITrustSpaceManager {
            public static ITrustSpaceManager sDefaultImpl;
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

            @Override // android.trustspace.ITrustSpaceManager
            public boolean addIntentProtectedApps(List<String> packages, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packages);
                    _data.writeInt(flags);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addIntentProtectedApps(packages, flags);
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

            @Override // android.trustspace.ITrustSpaceManager
            public boolean removeIntentProtectedApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeIntentProtectedApp(packageName);
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

            @Override // android.trustspace.ITrustSpaceManager
            public List<String> getIntentProtectedApps(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIntentProtectedApps(flags);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.trustspace.ITrustSpaceManager
            public boolean removeIntentProtectedApps(List<String> packages, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packages);
                    _data.writeInt(flags);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeIntentProtectedApps(packages, flags);
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

            @Override // android.trustspace.ITrustSpaceManager
            public boolean updateTrustApps(List<String> packages, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packages);
                    _data.writeInt(flag);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateTrustApps(packages, flag);
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

            @Override // android.trustspace.ITrustSpaceManager
            public boolean isIntentProtectedApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isIntentProtectedApp(packageName);
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

            @Override // android.trustspace.ITrustSpaceManager
            public int getProtectionLevel(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProtectionLevel(packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.trustspace.ITrustSpaceManager
            public boolean isSupportTrustSpace() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportTrustSpace();
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

            @Override // android.trustspace.ITrustSpaceManager
            public boolean isHwTrustSpace(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHwTrustSpace(userId);
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

        public static boolean setDefaultImpl(ITrustSpaceManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITrustSpaceManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
