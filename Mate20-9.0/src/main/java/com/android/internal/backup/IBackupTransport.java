package com.android.internal.backup;

import android.app.backup.RestoreDescription;
import android.app.backup.RestoreSet;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IBackupTransport extends IInterface {

    public static abstract class Stub extends Binder implements IBackupTransport {
        private static final String DESCRIPTOR = "com.android.internal.backup.IBackupTransport";
        static final int TRANSACTION_abortFullRestore = 26;
        static final int TRANSACTION_cancelFullBackup = 22;
        static final int TRANSACTION_checkFullBackupSize = 20;
        static final int TRANSACTION_clearBackupData = 10;
        static final int TRANSACTION_configurationIntent = 2;
        static final int TRANSACTION_currentDestinationString = 3;
        static final int TRANSACTION_dataManagementIntent = 4;
        static final int TRANSACTION_dataManagementLabel = 5;
        static final int TRANSACTION_finishBackup = 11;
        static final int TRANSACTION_finishRestore = 17;
        static final int TRANSACTION_getAvailableRestoreSets = 12;
        static final int TRANSACTION_getBackupQuota = 24;
        static final int TRANSACTION_getCurrentRestoreSet = 13;
        static final int TRANSACTION_getNextFullRestoreDataChunk = 25;
        static final int TRANSACTION_getRestoreData = 16;
        static final int TRANSACTION_getTransportFlags = 27;
        static final int TRANSACTION_initializeDevice = 8;
        static final int TRANSACTION_isAppEligibleForBackup = 23;
        static final int TRANSACTION_name = 1;
        static final int TRANSACTION_nextRestorePackage = 15;
        static final int TRANSACTION_performBackup = 9;
        static final int TRANSACTION_performFullBackup = 19;
        static final int TRANSACTION_requestBackupTime = 7;
        static final int TRANSACTION_requestFullBackupTime = 18;
        static final int TRANSACTION_sendBackupData = 21;
        static final int TRANSACTION_startRestore = 14;
        static final int TRANSACTION_transportDirName = 6;

        private static class Proxy implements IBackupTransport {
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

            public String name() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent configurationIntent() throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Intent) Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String currentDestinationString() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent dataManagementIntent() throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Intent) Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String dataManagementLabel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String transportDirName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long requestBackupTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int initializeDevice() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int performBackup(PackageInfo packageInfo, ParcelFileDescriptor inFd, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (packageInfo != null) {
                        _data.writeInt(1);
                        packageInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (inFd != null) {
                        _data.writeInt(1);
                        inFd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int clearBackupData(PackageInfo packageInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (packageInfo != null) {
                        _data.writeInt(1);
                        packageInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int finishBackup() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RestoreSet[] getAvailableRestoreSets() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return (RestoreSet[]) _reply.createTypedArray(RestoreSet.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getCurrentRestoreSet() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startRestore(long token, PackageInfo[] packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(token);
                    _data.writeTypedArray(packages, 0);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RestoreDescription nextRestorePackage() throws RemoteException {
                RestoreDescription _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (RestoreDescription) RestoreDescription.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRestoreData(ParcelFileDescriptor outFd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (outFd != null) {
                        _data.writeInt(1);
                        outFd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishRestore() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long requestFullBackupTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int performFullBackup(PackageInfo targetPackage, ParcelFileDescriptor socket, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (targetPackage != null) {
                        _data.writeInt(1);
                        targetPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (socket != null) {
                        _data.writeInt(1);
                        socket.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkFullBackupSize(long size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(size);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendBackupData(int numBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(numBytes);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelFullBackup() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppEligibleForBackup(PackageInfo targetPackage, boolean isFullBackup) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (targetPackage != null) {
                        _data.writeInt(1);
                        targetPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(isFullBackup);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getBackupQuota(String packageName, boolean isFullBackup) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(isFullBackup);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNextFullRestoreDataChunk(ParcelFileDescriptor socket) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (socket != null) {
                        _data.writeInt(1);
                        socket.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int abortFullRestore() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTransportFlags() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBackupTransport asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBackupTransport)) {
                return new Proxy(obj);
            }
            return (IBackupTransport) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: android.os.ParcelFileDescriptor} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v20, resolved type: android.os.ParcelFileDescriptor} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v26, resolved type: android.content.pm.PackageInfo} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v8 */
        /* JADX WARNING: type inference failed for: r3v15 */
        /* JADX WARNING: type inference failed for: r3v31 */
        /* JADX WARNING: type inference failed for: r3v35 */
        /* JADX WARNING: type inference failed for: r3v36 */
        /* JADX WARNING: type inference failed for: r3v37 */
        /* JADX WARNING: type inference failed for: r3v38 */
        /* JADX WARNING: type inference failed for: r3v39 */
        /* JADX WARNING: type inference failed for: r3v40 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PackageInfo _arg0;
            PackageInfo _arg02;
            if (code != 1598968902) {
                boolean _arg1 = false;
                ? _arg03 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = name();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        Intent _result2 = configurationIntent();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = currentDestinationString();
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        Intent _result4 = dataManagementIntent();
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = dataManagementLabel();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = transportDirName();
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        long _result7 = requestBackupTime();
                        reply.writeNoException();
                        reply.writeLong(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = initializeDevice();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (PackageInfo) PackageInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg03 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        }
                        int _result9 = performBackup(_arg0, _arg03, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (PackageInfo) PackageInfo.CREATOR.createFromParcel(data);
                        }
                        int _result10 = clearBackupData(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = finishBackup();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        RestoreSet[] _result12 = getAvailableRestoreSets();
                        reply.writeNoException();
                        reply.writeTypedArray(_result12, 1);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        long _result13 = getCurrentRestoreSet();
                        reply.writeNoException();
                        reply.writeLong(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = startRestore(data.readLong(), (PackageInfo[]) data.createTypedArray(PackageInfo.CREATOR));
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        RestoreDescription _result15 = nextRestorePackage();
                        reply.writeNoException();
                        if (_result15 != null) {
                            reply.writeInt(1);
                            _result15.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        }
                        int _result16 = getRestoreData(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        finishRestore();
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        long _result17 = requestFullBackupTime();
                        reply.writeNoException();
                        reply.writeLong(_result17);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (PackageInfo) PackageInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg03 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        }
                        int _result18 = performFullBackup(_arg02, _arg03, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = checkFullBackupSize(data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = sendBackupData(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        cancelFullBackup();
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (PackageInfo) PackageInfo.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result21 = isAppEligibleForBackup(_arg03, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        long _result22 = getBackupQuota(_arg04, _arg1);
                        reply.writeNoException();
                        reply.writeLong(_result22);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        }
                        int _result23 = getNextFullRestoreDataChunk(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = abortFullRestore();
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = getTransportFlags();
                        reply.writeNoException();
                        reply.writeInt(_result25);
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

    int abortFullRestore() throws RemoteException;

    void cancelFullBackup() throws RemoteException;

    int checkFullBackupSize(long j) throws RemoteException;

    int clearBackupData(PackageInfo packageInfo) throws RemoteException;

    Intent configurationIntent() throws RemoteException;

    String currentDestinationString() throws RemoteException;

    Intent dataManagementIntent() throws RemoteException;

    String dataManagementLabel() throws RemoteException;

    int finishBackup() throws RemoteException;

    void finishRestore() throws RemoteException;

    RestoreSet[] getAvailableRestoreSets() throws RemoteException;

    long getBackupQuota(String str, boolean z) throws RemoteException;

    long getCurrentRestoreSet() throws RemoteException;

    int getNextFullRestoreDataChunk(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    int getRestoreData(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    int getTransportFlags() throws RemoteException;

    int initializeDevice() throws RemoteException;

    boolean isAppEligibleForBackup(PackageInfo packageInfo, boolean z) throws RemoteException;

    String name() throws RemoteException;

    RestoreDescription nextRestorePackage() throws RemoteException;

    int performBackup(PackageInfo packageInfo, ParcelFileDescriptor parcelFileDescriptor, int i) throws RemoteException;

    int performFullBackup(PackageInfo packageInfo, ParcelFileDescriptor parcelFileDescriptor, int i) throws RemoteException;

    long requestBackupTime() throws RemoteException;

    long requestFullBackupTime() throws RemoteException;

    int sendBackupData(int i) throws RemoteException;

    int startRestore(long j, PackageInfo[] packageInfoArr) throws RemoteException;

    String transportDirName() throws RemoteException;
}
