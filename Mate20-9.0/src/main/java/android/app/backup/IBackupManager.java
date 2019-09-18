package android.app.backup;

import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.ISelectBackupTransportCallback;
import android.content.ComponentName;
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
        static final int TRANSACTION_acknowledgeFullBackupOrRestore = 17;
        static final int TRANSACTION_adbBackup = 14;
        static final int TRANSACTION_adbRestore = 16;
        static final int TRANSACTION_agentConnected = 4;
        static final int TRANSACTION_agentDisconnected = 5;
        static final int TRANSACTION_backupNow = 13;
        static final int TRANSACTION_beginRestoreSession = 29;
        static final int TRANSACTION_cancelBackups = 37;
        static final int TRANSACTION_clearBackupData = 2;
        static final int TRANSACTION_dataChanged = 1;
        static final int TRANSACTION_filterAppsEligibleForBackup = 35;
        static final int TRANSACTION_fullTransportBackup = 15;
        static final int TRANSACTION_getAvailableRestoreToken = 33;
        static final int TRANSACTION_getConfigurationIntent = 25;
        static final int TRANSACTION_getCurrentTransport = 19;
        static final int TRANSACTION_getDataManagementIntent = 27;
        static final int TRANSACTION_getDataManagementLabel = 28;
        static final int TRANSACTION_getDestinationString = 26;
        static final int TRANSACTION_getTransportWhitelist = 22;
        static final int TRANSACTION_hasBackupPassword = 12;
        static final int TRANSACTION_initializeTransports = 3;
        static final int TRANSACTION_isAppEligibleForBackup = 34;
        static final int TRANSACTION_isBackupEnabled = 10;
        static final int TRANSACTION_isBackupServiceActive = 32;
        static final int TRANSACTION_listAllTransportComponents = 21;
        static final int TRANSACTION_listAllTransports = 20;
        static final int TRANSACTION_opComplete = 30;
        static final int TRANSACTION_requestBackup = 36;
        static final int TRANSACTION_restoreAtInstall = 6;
        static final int TRANSACTION_selectBackupTransport = 23;
        static final int TRANSACTION_selectBackupTransportAsync = 24;
        static final int TRANSACTION_setAutoRestore = 8;
        static final int TRANSACTION_setBackupEnabled = 7;
        static final int TRANSACTION_setBackupPassword = 11;
        static final int TRANSACTION_setBackupProvisioned = 9;
        static final int TRANSACTION_setBackupServiceActive = 31;
        static final int TRANSACTION_updateTransportAttributes = 18;

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
                    this.mRemote.transact(1, _data, _reply, 0);
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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void initializeTransports(String[] transportNames, IBackupObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(transportNames);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
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
                    this.mRemote.transact(4, _data, _reply, 0);
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
                    this.mRemote.transact(5, _data, _reply, 0);
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
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackupEnabled(boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isEnabled);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAutoRestore(boolean doAutoRestore) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(doAutoRestore);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackupProvisioned(boolean isProvisioned) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isProvisioned);
                    this.mRemote.transact(9, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasBackupPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void backupNow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void adbBackup(ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean allApps, boolean allIncludesSystem, boolean doCompress, boolean doKeyValue, String[] packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(includeApks);
                    _data.writeInt(includeObbs);
                    _data.writeInt(includeShared);
                    _data.writeInt(doWidgets);
                    _data.writeInt(allApps);
                    _data.writeInt(allIncludesSystem);
                    _data.writeInt(doCompress);
                    _data.writeInt(doKeyValue);
                    _data.writeStringArray(packageNames);
                    this.mRemote.transact(14, _data, _reply, 0);
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
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void adbRestore(ParcelFileDescriptor fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acknowledgeFullBackupOrRestore(int token, boolean allow, String curPassword, String encryptionPassword, IFullBackupRestoreObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(allow);
                    _data.writeString(curPassword);
                    _data.writeString(encryptionPassword);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateTransportAttributes(ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, String dataManagementLabel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (transportComponent != null) {
                        _data.writeInt(1);
                        transportComponent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(name);
                    if (configurationIntent != null) {
                        _data.writeInt(1);
                        configurationIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(currentDestinationString);
                    if (dataManagementIntent != null) {
                        _data.writeInt(1);
                        dataManagementIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(dataManagementLabel);
                    this.mRemote.transact(18, _data, _reply, 0);
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
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName[] listAllTransportComponents() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return (ComponentName[]) _reply.createTypedArray(ComponentName.CREATOR);
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
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
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
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void selectBackupTransportAsync(ComponentName transport, ISelectBackupTransportCallback listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (transport != null) {
                        _data.writeInt(1);
                        transport.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent getConfigurationIntent(String transport) throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(transport);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent getDataManagementIntent(String transport) throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(transport);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return IRestoreSession.Stub.asInterface(_reply.readStrongBinder());
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
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackupServiceActive(int whichUser, boolean makeActive) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(whichUser);
                    _data.writeInt(makeActive);
                    this.mRemote.transact(31, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
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
                    boolean _result = false;
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] filterAppsEligibleForBackup(String[] packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packages);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int requestBackup(String[] packages, IBackupObserver observer, IBackupManagerMonitor monitor, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packages);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (monitor != null) {
                        iBinder = monitor.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelBackups() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(37, _data, _reply, 0);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v48, resolved type: android.content.ComponentName} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v17 */
        /* JADX WARNING: type inference failed for: r0v31, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r0v38 */
        /* JADX WARNING: type inference failed for: r0v56, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r0v66 */
        /* JADX WARNING: type inference failed for: r0v67 */
        /* JADX WARNING: type inference failed for: r0v68 */
        /* JADX WARNING: type inference failed for: r0v69 */
        /* JADX WARNING: type inference failed for: r0v70 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            Intent _arg2;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ? _arg02 = 0;
                boolean _arg8 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        dataChanged(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearBackupData(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        initializeTransports(data.createStringArray(), IBackupObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        agentConnected(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        agentDisconnected(data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        restoreAtInstall(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg8 = true;
                        }
                        setBackupEnabled(_arg8);
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg8 = true;
                        }
                        setAutoRestore(_arg8);
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg8 = true;
                        }
                        setBackupProvisioned(_arg8);
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = isBackupEnabled();
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = setBackupPassword(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = hasBackupPassword();
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        backupNow();
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        }
                        ParcelFileDescriptor _arg03 = _arg02;
                        boolean _arg1 = data.readInt() != 0;
                        boolean _arg22 = data.readInt() != 0;
                        boolean _arg3 = data.readInt() != 0;
                        boolean _arg4 = data.readInt() != 0;
                        boolean _arg5 = data.readInt() != 0;
                        boolean _arg6 = data.readInt() != 0;
                        boolean _arg7 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg8 = true;
                        }
                        adbBackup(_arg03, _arg1, _arg22, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        fullTransportBackup(data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        }
                        adbRestore(_arg02);
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        acknowledgeFullBackupOrRestore(data.readInt(), data.readInt() != 0, data.readString(), data.readString(), IFullBackupRestoreObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Intent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        String _arg32 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        updateTransportAttributes(_arg0, _arg12, _arg2, _arg32, _arg02, data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result4 = getCurrentTransport();
                        reply.writeNoException();
                        parcel2.writeString(_result4);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result5 = listAllTransports();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result5);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName[] _result6 = listAllTransportComponents();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result6, 1);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result7 = getTransportWhitelist();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result7);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result8 = selectBackupTransport(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result8);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        selectBackupTransportAsync(_arg02, ISelectBackupTransportCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        Intent _result9 = getConfigurationIntent(data.readString());
                        reply.writeNoException();
                        if (_result9 != null) {
                            parcel2.writeInt(1);
                            _result9.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result10 = getDestinationString(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result10);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        Intent _result11 = getDataManagementIntent(data.readString());
                        reply.writeNoException();
                        if (_result11 != null) {
                            parcel2.writeInt(1);
                            _result11.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result12 = getDataManagementLabel(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result12);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        IRestoreSession _result13 = beginRestoreSession(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result13 != null) {
                            _arg02 = _result13.asBinder();
                        }
                        parcel2.writeStrongBinder(_arg02);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        opComplete(data.readInt(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg8 = true;
                        }
                        setBackupServiceActive(_arg04, _arg8);
                        reply.writeNoException();
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result14 = isBackupServiceActive(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result15 = getAvailableRestoreToken(data.readString());
                        reply.writeNoException();
                        parcel2.writeLong(_result15);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result16 = isAppEligibleForBackup(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result17 = filterAppsEligibleForBackup(data.createStringArray());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result17);
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result18 = requestBackup(data.createStringArray(), IBackupObserver.Stub.asInterface(data.readStrongBinder()), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelBackups();
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void acknowledgeFullBackupOrRestore(int i, boolean z, String str, String str2, IFullBackupRestoreObserver iFullBackupRestoreObserver) throws RemoteException;

    void adbBackup(ParcelFileDescriptor parcelFileDescriptor, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8, String[] strArr) throws RemoteException;

    void adbRestore(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void agentConnected(String str, IBinder iBinder) throws RemoteException;

    void agentDisconnected(String str) throws RemoteException;

    void backupNow() throws RemoteException;

    IRestoreSession beginRestoreSession(String str, String str2) throws RemoteException;

    void cancelBackups() throws RemoteException;

    void clearBackupData(String str, String str2) throws RemoteException;

    void dataChanged(String str) throws RemoteException;

    String[] filterAppsEligibleForBackup(String[] strArr) throws RemoteException;

    void fullTransportBackup(String[] strArr) throws RemoteException;

    long getAvailableRestoreToken(String str) throws RemoteException;

    Intent getConfigurationIntent(String str) throws RemoteException;

    String getCurrentTransport() throws RemoteException;

    Intent getDataManagementIntent(String str) throws RemoteException;

    String getDataManagementLabel(String str) throws RemoteException;

    String getDestinationString(String str) throws RemoteException;

    String[] getTransportWhitelist() throws RemoteException;

    boolean hasBackupPassword() throws RemoteException;

    void initializeTransports(String[] strArr, IBackupObserver iBackupObserver) throws RemoteException;

    boolean isAppEligibleForBackup(String str) throws RemoteException;

    boolean isBackupEnabled() throws RemoteException;

    boolean isBackupServiceActive(int i) throws RemoteException;

    ComponentName[] listAllTransportComponents() throws RemoteException;

    String[] listAllTransports() throws RemoteException;

    void opComplete(int i, long j) throws RemoteException;

    int requestBackup(String[] strArr, IBackupObserver iBackupObserver, IBackupManagerMonitor iBackupManagerMonitor, int i) throws RemoteException;

    void restoreAtInstall(String str, int i) throws RemoteException;

    String selectBackupTransport(String str) throws RemoteException;

    void selectBackupTransportAsync(ComponentName componentName, ISelectBackupTransportCallback iSelectBackupTransportCallback) throws RemoteException;

    void setAutoRestore(boolean z) throws RemoteException;

    void setBackupEnabled(boolean z) throws RemoteException;

    boolean setBackupPassword(String str, String str2) throws RemoteException;

    void setBackupProvisioned(boolean z) throws RemoteException;

    void setBackupServiceActive(int i, boolean z) throws RemoteException;

    void updateTransportAttributes(ComponentName componentName, String str, Intent intent, String str2, Intent intent2, String str3) throws RemoteException;
}
