package android.content.pm;

import android.content.IntentSender;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageInstaller.SessionParams;
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

            public int createSession(SessionParams params, String installerPackageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_createSession);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(installerPackageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_createSession, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_createSession);
                        appIcon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateSessionAppIcon, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updateSessionAppLabel, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_abandonSession, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_openSession, _data, _reply, 0);
                    _reply.readException();
                    IPackageInstallerSession _result = android.content.pm.IPackageInstallerSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SessionInfo getSessionInfo(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SessionInfo sessionInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(Stub.TRANSACTION_getSessionInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        sessionInfo = (SessionInfo) SessionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        sessionInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return sessionInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAllSessions(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getAllSessions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getMySessions(String installerPackageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(installerPackageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getMySessions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallback(IPackageInstallerCallback callback, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallback(IPackageInstallerCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uninstall(String packageName, String callerPackageName, int flags, IntentSender statusReceiver, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(callerPackageName);
                    _data.writeInt(flags);
                    if (statusReceiver != null) {
                        _data.writeInt(Stub.TRANSACTION_createSession);
                        statusReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_uninstall, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPermissionsResult(int sessionId, boolean accepted) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (accepted) {
                        i = Stub.TRANSACTION_createSession;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setPermissionsResult, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParceledListSlice _result;
            switch (code) {
                case TRANSACTION_createSession /*1*/:
                    SessionParams sessionParams;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        sessionParams = (SessionParams) SessionParams.CREATOR.createFromParcel(data);
                    } else {
                        sessionParams = null;
                    }
                    int _result2 = createSession(sessionParams, data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updateSessionAppIcon /*2*/:
                    Bitmap bitmap;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        bitmap = null;
                    }
                    updateSessionAppIcon(_arg0, bitmap);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateSessionAppLabel /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateSessionAppLabel(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_abandonSession /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    abandonSession(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_openSession /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    IPackageInstallerSession _result3 = openSession(data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                    return true;
                case TRANSACTION_getSessionInfo /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    SessionInfo _result4 = getSessionInfo(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_createSession);
                        _result4.writeToParcel(reply, TRANSACTION_createSession);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllSessions /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAllSessions(data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_createSession);
                        _result.writeToParcel(reply, TRANSACTION_createSession);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getMySessions /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMySessions(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_createSession);
                        _result.writeToParcel(reply, TRANSACTION_createSession);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_registerCallback /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(android.content.pm.IPackageInstallerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterCallback /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallback(android.content.pm.IPackageInstallerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uninstall /*11*/:
                    IntentSender intentSender;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    String _arg1 = data.readString();
                    int _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        intentSender = (IntentSender) IntentSender.CREATOR.createFromParcel(data);
                    } else {
                        intentSender = null;
                    }
                    uninstall(_arg02, _arg1, _arg2, intentSender, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setPermissionsResult /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPermissionsResult(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void abandonSession(int i) throws RemoteException;

    int createSession(SessionParams sessionParams, String str, int i) throws RemoteException;

    ParceledListSlice getAllSessions(int i) throws RemoteException;

    ParceledListSlice getMySessions(String str, int i) throws RemoteException;

    SessionInfo getSessionInfo(int i) throws RemoteException;

    IPackageInstallerSession openSession(int i) throws RemoteException;

    void registerCallback(IPackageInstallerCallback iPackageInstallerCallback, int i) throws RemoteException;

    void setPermissionsResult(int i, boolean z) throws RemoteException;

    void uninstall(String str, String str2, int i, IntentSender intentSender, int i2) throws RemoteException;

    void unregisterCallback(IPackageInstallerCallback iPackageInstallerCallback) throws RemoteException;

    void updateSessionAppIcon(int i, Bitmap bitmap) throws RemoteException;

    void updateSessionAppLabel(int i, String str) throws RemoteException;
}
