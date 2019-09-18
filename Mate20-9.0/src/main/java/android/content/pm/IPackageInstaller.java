package android.content.pm;

import android.content.IntentSender;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.IPackageInstallerSession;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageInstaller extends IInterface {

    public static abstract class Stub extends Binder implements IPackageInstaller {
        private static final String DESCRIPTOR = "android.content.pm.IPackageInstaller";
        static final int TRANSACTION_abandonSession = 4;
        static final int TRANSACTION_createSession = 1;
        static final int TRANSACTION_getAllSessions = 7;
        static final int TRANSACTION_getMySessions = 8;
        static final int TRANSACTION_getSessionInfo = 6;
        static final int TRANSACTION_openSession = 5;
        static final int TRANSACTION_registerCallback = 9;
        static final int TRANSACTION_setPermissionsResult = 12;
        static final int TRANSACTION_uninstall = 11;
        static final int TRANSACTION_unregisterCallback = 10;
        static final int TRANSACTION_updateSessionAppIcon = 2;
        static final int TRANSACTION_updateSessionAppLabel = 3;

        private static class Proxy implements IPackageInstaller {
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

            public int createSession(PackageInstaller.SessionParams params, String installerPackageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(installerPackageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateSessionAppIcon(int sessionId, Bitmap appIcon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (appIcon != null) {
                        _data.writeInt(1);
                        appIcon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateSessionAppLabel(int sessionId, String appLabel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(appLabel);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void abandonSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IPackageInstallerSession openSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return IPackageInstallerSession.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PackageInstaller.SessionInfo getSessionInfo(int sessionId) throws RemoteException {
                PackageInstaller.SessionInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PackageInstaller.SessionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAllSessions(int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getMySessions(String installerPackageName, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(installerPackageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallback(IPackageInstallerCallback callback, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(userId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallback(IPackageInstallerCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uninstall(VersionedPackage versionedPackage, String callerPackageName, int flags, IntentSender statusReceiver, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (versionedPackage != null) {
                        _data.writeInt(1);
                        versionedPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackageName);
                    _data.writeInt(flags);
                    if (statusReceiver != null) {
                        _data.writeInt(1);
                        statusReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPermissionsResult(int sessionId, boolean accepted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(accepted);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPackageInstaller asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageInstaller)) {
                return new Proxy(obj);
            }
            return (IPackageInstaller) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: android.graphics.Bitmap} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v1 */
        /* JADX WARNING: type inference failed for: r3v10, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r3v16 */
        /* JADX WARNING: type inference failed for: r3v22 */
        /* JADX WARNING: type inference failed for: r3v23 */
        /* JADX WARNING: type inference failed for: r3v24 */
        /* JADX WARNING: type inference failed for: r3v25 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            VersionedPackage _arg0;
            if (code != 1598968902) {
                boolean _arg1 = false;
                ? _arg12 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg12 = PackageInstaller.SessionParams.CREATOR.createFromParcel(data);
                        }
                        int _result = createSession(_arg12, data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = Bitmap.CREATOR.createFromParcel(data);
                        }
                        updateSessionAppIcon(_arg02, _arg12);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        updateSessionAppLabel(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        abandonSession(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IPackageInstallerSession _result2 = openSession(data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            _arg12 = _result2.asBinder();
                        }
                        reply.writeStrongBinder(_arg12);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        PackageInstaller.SessionInfo _result3 = getSessionInfo(data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result4 = getAllSessions(data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result5 = getMySessions(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallback(IPackageInstallerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterCallback(IPackageInstallerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = VersionedPackage.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        String _arg13 = data.readString();
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = IntentSender.CREATOR.createFromParcel(data);
                        }
                        uninstall(_arg0, _arg13, _arg2, _arg12, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setPermissionsResult(_arg03, _arg1);
                        reply.writeNoException();
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

    void abandonSession(int i) throws RemoteException;

    int createSession(PackageInstaller.SessionParams sessionParams, String str, int i) throws RemoteException;

    ParceledListSlice getAllSessions(int i) throws RemoteException;

    ParceledListSlice getMySessions(String str, int i) throws RemoteException;

    PackageInstaller.SessionInfo getSessionInfo(int i) throws RemoteException;

    IPackageInstallerSession openSession(int i) throws RemoteException;

    void registerCallback(IPackageInstallerCallback iPackageInstallerCallback, int i) throws RemoteException;

    void setPermissionsResult(int i, boolean z) throws RemoteException;

    void uninstall(VersionedPackage versionedPackage, String str, int i, IntentSender intentSender, int i2) throws RemoteException;

    void unregisterCallback(IPackageInstallerCallback iPackageInstallerCallback) throws RemoteException;

    void updateSessionAppIcon(int i, Bitmap bitmap) throws RemoteException;

    void updateSessionAppLabel(int i, String str) throws RemoteException;
}
