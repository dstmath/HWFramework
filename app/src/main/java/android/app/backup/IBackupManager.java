package android.app.backup;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IBackupManager extends IInterface {

    public static abstract class Stub extends Binder implements IBackupManager {
        private static final String DESCRIPTOR = "android.app.backup.IBackupManager";
        static final int TRANSACTION_acknowledgeFullBackupOrRestore = 16;
        static final int TRANSACTION_agentConnected = 3;
        static final int TRANSACTION_agentDisconnected = 4;
        static final int TRANSACTION_backupNow = 12;
        static final int TRANSACTION_beginRestoreSession = 25;
        static final int TRANSACTION_clearBackupData = 2;
        static final int TRANSACTION_dataChanged = 1;
        static final int TRANSACTION_fullBackup = 13;
        static final int TRANSACTION_fullRestore = 15;
        static final int TRANSACTION_fullTransportBackup = 14;
        static final int TRANSACTION_getAvailableRestoreToken = 29;
        static final int TRANSACTION_getConfigurationIntent = 21;
        static final int TRANSACTION_getCurrentTransport = 17;
        static final int TRANSACTION_getDataManagementIntent = 23;
        static final int TRANSACTION_getDataManagementLabel = 24;
        static final int TRANSACTION_getDestinationString = 22;
        static final int TRANSACTION_getTransportWhitelist = 19;
        static final int TRANSACTION_hasBackupPassword = 11;
        static final int TRANSACTION_isAppEligibleForBackup = 30;
        static final int TRANSACTION_isBackupEnabled = 9;
        static final int TRANSACTION_isBackupServiceActive = 28;
        static final int TRANSACTION_listAllTransports = 18;
        static final int TRANSACTION_opComplete = 26;
        static final int TRANSACTION_requestBackup = 31;
        static final int TRANSACTION_restoreAtInstall = 5;
        static final int TRANSACTION_selectBackupTransport = 20;
        static final int TRANSACTION_setAutoRestore = 7;
        static final int TRANSACTION_setBackupEnabled = 6;
        static final int TRANSACTION_setBackupPassword = 10;
        static final int TRANSACTION_setBackupProvisioned = 8;
        static final int TRANSACTION_setBackupServiceActive = 27;

        private static class Proxy implements IBackupManager {
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

            public void dataChanged(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_dataChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearBackupData(String transportName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(transportName);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_clearBackupData, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void agentConnected(String packageName, IBinder agent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(agent);
                    this.mRemote.transact(Stub.TRANSACTION_agentConnected, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void agentDisconnected(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_agentDisconnected, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreAtInstall(String packageName, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(token);
                    this.mRemote.transact(Stub.TRANSACTION_restoreAtInstall, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackupEnabled(boolean isEnabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (isEnabled) {
                        i = Stub.TRANSACTION_dataChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setBackupEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAutoRestore(boolean doAutoRestore) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (doAutoRestore) {
                        i = Stub.TRANSACTION_dataChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setAutoRestore, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackupProvisioned(boolean isProvisioned) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (isProvisioned) {
                        i = Stub.TRANSACTION_dataChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setBackupProvisioned, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBackupEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isBackupEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setBackupPassword(String currentPw, String newPw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(currentPw);
                    _data.writeString(newPw);
                    this.mRemote.transact(Stub.TRANSACTION_setBackupPassword, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasBackupPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hasBackupPassword, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void backupNow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_backupNow, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fullBackup(ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean allApps, boolean allIncludesSystem, boolean doCompress, String[] packageNames) throws RemoteException {
                int i = Stub.TRANSACTION_dataChanged;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(Stub.TRANSACTION_dataChanged);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(includeApks ? Stub.TRANSACTION_dataChanged : 0);
                    if (includeObbs) {
                        i2 = Stub.TRANSACTION_dataChanged;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (includeShared) {
                        i2 = Stub.TRANSACTION_dataChanged;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (doWidgets) {
                        i2 = Stub.TRANSACTION_dataChanged;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (allApps) {
                        i2 = Stub.TRANSACTION_dataChanged;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (allIncludesSystem) {
                        i2 = Stub.TRANSACTION_dataChanged;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!doCompress) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeStringArray(packageNames);
                    this.mRemote.transact(Stub.TRANSACTION_fullBackup, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fullTransportBackup(String[] packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    this.mRemote.transact(Stub.TRANSACTION_fullTransportBackup, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fullRestore(ParcelFileDescriptor fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(Stub.TRANSACTION_dataChanged);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_fullRestore, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acknowledgeFullBackupOrRestore(int token, boolean allow, String curPassword, String encryptionPassword, IFullBackupRestoreObserver observer) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    if (allow) {
                        i = Stub.TRANSACTION_dataChanged;
                    }
                    _data.writeInt(i);
                    _data.writeString(curPassword);
                    _data.writeString(encryptionPassword);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_acknowledgeFullBackupOrRestore, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCurrentTransport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentTransport, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] listAllTransports() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_listAllTransports, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getTransportWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTransportWhitelist, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String selectBackupTransport(String transport) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(transport);
                    this.mRemote.transact(Stub.TRANSACTION_selectBackupTransport, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent getConfigurationIntent(String transport) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Intent intent;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(transport);
                    this.mRemote.transact(Stub.TRANSACTION_getConfigurationIntent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        intent = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return intent;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDestinationString(String transport) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(transport);
                    this.mRemote.transact(Stub.TRANSACTION_getDestinationString, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent getDataManagementIntent(String transport) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Intent intent;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(transport);
                    this.mRemote.transact(Stub.TRANSACTION_getDataManagementIntent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        intent = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return intent;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDataManagementLabel(String transport) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(transport);
                    this.mRemote.transact(Stub.TRANSACTION_getDataManagementLabel, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IRestoreSession beginRestoreSession(String packageName, String transportID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(transportID);
                    this.mRemote.transact(Stub.TRANSACTION_beginRestoreSession, _data, _reply, 0);
                    _reply.readException();
                    IRestoreSession _result = android.app.backup.IRestoreSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void opComplete(int token, long result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeLong(result);
                    this.mRemote.transact(Stub.TRANSACTION_opComplete, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackupServiceActive(int whichUser, boolean makeActive) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(whichUser);
                    if (makeActive) {
                        i = Stub.TRANSACTION_dataChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setBackupServiceActive, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBackupServiceActive(int whichUser) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(whichUser);
                    this.mRemote.transact(Stub.TRANSACTION_isBackupServiceActive, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getAvailableRestoreToken(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getAvailableRestoreToken, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppEligibleForBackup(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_isAppEligibleForBackup, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int requestBackup(String[] packages, IBackupObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packages);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_requestBackup, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBackupManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBackupManager)) {
                return new Proxy(obj);
            }
            return (IBackupManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            ParcelFileDescriptor parcelFileDescriptor;
            String _result2;
            String[] _result3;
            Intent _result4;
            switch (code) {
                case TRANSACTION_dataChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    dataChanged(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearBackupData /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearBackupData(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_agentConnected /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    agentConnected(data.readString(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_agentDisconnected /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    agentDisconnected(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_restoreAtInstall /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    restoreAtInstall(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setBackupEnabled /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setBackupEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAutoRestore /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAutoRestore(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setBackupProvisioned /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    setBackupProvisioned(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isBackupEnabled /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isBackupEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dataChanged : 0);
                    return true;
                case TRANSACTION_setBackupPassword /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setBackupPassword(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dataChanged : 0);
                    return true;
                case TRANSACTION_hasBackupPassword /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasBackupPassword();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dataChanged : 0);
                    return true;
                case TRANSACTION_backupNow /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    backupNow();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_fullBackup /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    fullBackup(parcelFileDescriptor, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_fullTransportBackup /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    fullTransportBackup(data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_fullRestore /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    fullRestore(parcelFileDescriptor);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_acknowledgeFullBackupOrRestore /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    acknowledgeFullBackupOrRestore(data.readInt(), data.readInt() != 0, data.readString(), data.readString(), android.app.backup.IFullBackupRestoreObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCurrentTransport /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCurrentTransport();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_listAllTransports /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = listAllTransports();
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case TRANSACTION_getTransportWhitelist /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getTransportWhitelist();
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case TRANSACTION_selectBackupTransport /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = selectBackupTransport(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_getConfigurationIntent /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getConfigurationIntent(data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_dataChanged);
                        _result4.writeToParcel(reply, TRANSACTION_dataChanged);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getDestinationString /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDestinationString(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_getDataManagementIntent /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getDataManagementIntent(data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_dataChanged);
                        _result4.writeToParcel(reply, TRANSACTION_dataChanged);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getDataManagementLabel /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDataManagementLabel(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_beginRestoreSession /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    IRestoreSession _result5 = beginRestoreSession(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    return true;
                case TRANSACTION_opComplete /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    opComplete(data.readInt(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setBackupServiceActive /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    setBackupServiceActive(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isBackupServiceActive /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isBackupServiceActive(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dataChanged : 0);
                    return true;
                case TRANSACTION_getAvailableRestoreToken /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result6 = getAvailableRestoreToken(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result6);
                    return true;
                case TRANSACTION_isAppEligibleForBackup /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isAppEligibleForBackup(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dataChanged : 0);
                    return true;
                case TRANSACTION_requestBackup /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result7 = requestBackup(data.createStringArray(), android.app.backup.IBackupObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result7);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void acknowledgeFullBackupOrRestore(int i, boolean z, String str, String str2, IFullBackupRestoreObserver iFullBackupRestoreObserver) throws RemoteException;

    void agentConnected(String str, IBinder iBinder) throws RemoteException;

    void agentDisconnected(String str) throws RemoteException;

    void backupNow() throws RemoteException;

    IRestoreSession beginRestoreSession(String str, String str2) throws RemoteException;

    void clearBackupData(String str, String str2) throws RemoteException;

    void dataChanged(String str) throws RemoteException;

    void fullBackup(ParcelFileDescriptor parcelFileDescriptor, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, String[] strArr) throws RemoteException;

    void fullRestore(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void fullTransportBackup(String[] strArr) throws RemoteException;

    long getAvailableRestoreToken(String str) throws RemoteException;

    Intent getConfigurationIntent(String str) throws RemoteException;

    String getCurrentTransport() throws RemoteException;

    Intent getDataManagementIntent(String str) throws RemoteException;

    String getDataManagementLabel(String str) throws RemoteException;

    String getDestinationString(String str) throws RemoteException;

    String[] getTransportWhitelist() throws RemoteException;

    boolean hasBackupPassword() throws RemoteException;

    boolean isAppEligibleForBackup(String str) throws RemoteException;

    boolean isBackupEnabled() throws RemoteException;

    boolean isBackupServiceActive(int i) throws RemoteException;

    String[] listAllTransports() throws RemoteException;

    void opComplete(int i, long j) throws RemoteException;

    int requestBackup(String[] strArr, IBackupObserver iBackupObserver) throws RemoteException;

    void restoreAtInstall(String str, int i) throws RemoteException;

    String selectBackupTransport(String str) throws RemoteException;

    void setAutoRestore(boolean z) throws RemoteException;

    void setBackupEnabled(boolean z) throws RemoteException;

    boolean setBackupPassword(String str, String str2) throws RemoteException;

    void setBackupProvisioned(boolean z) throws RemoteException;

    void setBackupServiceActive(int i, boolean z) throws RemoteException;
}
