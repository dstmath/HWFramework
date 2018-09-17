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
                    this.mRemote.transact(Stub.TRANSACTION_name, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent configurationIntent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Intent intent;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_configurationIntent, _data, _reply, 0);
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

            public String currentDestinationString() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_currentDestinationString, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent dataManagementIntent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Intent intent;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dataManagementIntent, _data, _reply, 0);
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

            public String dataManagementLabel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dataManagementLabel, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_transportDirName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_requestBackupTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_initializeDevice, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_name);
                        packageInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (inFd != null) {
                        _data.writeInt(Stub.TRANSACTION_name);
                        inFd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_performBackup, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_name);
                        packageInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_clearBackupData, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_finishBackup, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAvailableRestoreSets, _data, _reply, 0);
                    _reply.readException();
                    RestoreSet[] _result = (RestoreSet[]) _reply.createTypedArray(RestoreSet.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentRestoreSet, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_startRestore, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RestoreDescription nextRestorePackage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    RestoreDescription restoreDescription;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_nextRestorePackage, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        restoreDescription = (RestoreDescription) RestoreDescription.CREATOR.createFromParcel(_reply);
                    } else {
                        restoreDescription = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return restoreDescription;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_name);
                        outFd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getRestoreData, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_finishRestore, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_requestFullBackupTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_name);
                        targetPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (socket != null) {
                        _data.writeInt(Stub.TRANSACTION_name);
                        socket.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_performFullBackup, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_checkFullBackupSize, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_sendBackupData, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_cancelFullBackup, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppEligibleForBackup(PackageInfo targetPackage, boolean isFullBackup) throws RemoteException {
                int i = Stub.TRANSACTION_name;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (targetPackage != null) {
                        _data.writeInt(Stub.TRANSACTION_name);
                        targetPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!isFullBackup) {
                        i = 0;
                    }
                    _data.writeInt(i);
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

            public long getBackupQuota(String packageName, boolean isFullBackup) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (isFullBackup) {
                        i = Stub.TRANSACTION_name;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getBackupQuota, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_name);
                        socket.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getNextFullRestoreDataChunk, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_abortFullRestore, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _result;
            Intent _result2;
            long _result3;
            int _result4;
            PackageInfo packageInfo;
            ParcelFileDescriptor parcelFileDescriptor;
            ParcelFileDescriptor parcelFileDescriptor2;
            switch (code) {
                case TRANSACTION_name /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = name();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_configurationIntent /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = configurationIntent();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_name);
                        _result2.writeToParcel(reply, TRANSACTION_name);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_currentDestinationString /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = currentDestinationString();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_dataManagementIntent /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = dataManagementIntent();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_name);
                        _result2.writeToParcel(reply, TRANSACTION_name);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_dataManagementLabel /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = dataManagementLabel();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_transportDirName /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = transportDirName();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_requestBackupTime /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = requestBackupTime();
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_initializeDevice /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = initializeDevice();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_performBackup /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        packageInfo = (PackageInfo) PackageInfo.CREATOR.createFromParcel(data);
                    } else {
                        packageInfo = null;
                    }
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _result4 = performBackup(packageInfo, parcelFileDescriptor, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_clearBackupData /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        packageInfo = (PackageInfo) PackageInfo.CREATOR.createFromParcel(data);
                    } else {
                        packageInfo = null;
                    }
                    _result4 = clearBackupData(packageInfo);
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_finishBackup /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = finishBackup();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getAvailableRestoreSets /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    RestoreSet[] _result5 = getAvailableRestoreSets();
                    reply.writeNoException();
                    reply.writeTypedArray(_result5, TRANSACTION_name);
                    return true;
                case TRANSACTION_getCurrentRestoreSet /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCurrentRestoreSet();
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_startRestore /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = startRestore(data.readLong(), (PackageInfo[]) data.createTypedArray(PackageInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_nextRestorePackage /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    RestoreDescription _result6 = nextRestorePackage();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_name);
                        _result6.writeToParcel(reply, TRANSACTION_name);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getRestoreData /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor2 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor2 = null;
                    }
                    _result4 = getRestoreData(parcelFileDescriptor2);
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_finishRestore /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    finishRestore();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestFullBackupTime /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = requestFullBackupTime();
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_performFullBackup /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        packageInfo = (PackageInfo) PackageInfo.CREATOR.createFromParcel(data);
                    } else {
                        packageInfo = null;
                    }
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _result4 = performFullBackup(packageInfo, parcelFileDescriptor, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_checkFullBackupSize /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = checkFullBackupSize(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_sendBackupData /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = sendBackupData(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_cancelFullBackup /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelFullBackup();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isAppEligibleForBackup /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        packageInfo = (PackageInfo) PackageInfo.CREATOR.createFromParcel(data);
                    } else {
                        packageInfo = null;
                    }
                    boolean _result7 = isAppEligibleForBackup(packageInfo, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result7 ? TRANSACTION_name : 0);
                    return true;
                case TRANSACTION_getBackupQuota /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getBackupQuota(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_getNextFullRestoreDataChunk /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor2 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor2 = null;
                    }
                    _result4 = getNextFullRestoreDataChunk(parcelFileDescriptor2);
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_abortFullRestore /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = abortFullRestore();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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
