package android.content;

import android.accounts.Account;
import android.content.ISyncStatusObserver;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IContentService extends IInterface {

    public static abstract class Stub extends Binder implements IContentService {
        private static final String DESCRIPTOR = "android.content.IContentService";
        static final int TRANSACTION_addPeriodicSync = 15;
        static final int TRANSACTION_addStatusChangeListener = 34;
        static final int TRANSACTION_cancelRequest = 9;
        static final int TRANSACTION_cancelSync = 7;
        static final int TRANSACTION_cancelSyncAsUser = 8;
        static final int TRANSACTION_getCache = 37;
        static final int TRANSACTION_getCurrentSyncs = 24;
        static final int TRANSACTION_getCurrentSyncsAsUser = 25;
        static final int TRANSACTION_getIsSyncable = 17;
        static final int TRANSACTION_getIsSyncableAsUser = 18;
        static final int TRANSACTION_getMasterSyncAutomatically = 22;
        static final int TRANSACTION_getMasterSyncAutomaticallyAsUser = 23;
        static final int TRANSACTION_getPeriodicSyncs = 14;
        static final int TRANSACTION_getSyncAdapterPackagesForAuthorityAsUser = 28;
        static final int TRANSACTION_getSyncAdapterTypes = 26;
        static final int TRANSACTION_getSyncAdapterTypesAsUser = 27;
        static final int TRANSACTION_getSyncAutomatically = 10;
        static final int TRANSACTION_getSyncAutomaticallyAsUser = 11;
        static final int TRANSACTION_getSyncStatus = 30;
        static final int TRANSACTION_getSyncStatusAsUser = 31;
        static final int TRANSACTION_isSyncActive = 29;
        static final int TRANSACTION_isSyncPending = 32;
        static final int TRANSACTION_isSyncPendingAsUser = 33;
        static final int TRANSACTION_notifyChange = 3;
        static final int TRANSACTION_putCache = 36;
        static final int TRANSACTION_registerContentObserver = 2;
        static final int TRANSACTION_removePeriodicSync = 16;
        static final int TRANSACTION_removeStatusChangeListener = 35;
        static final int TRANSACTION_requestSync = 4;
        static final int TRANSACTION_resetTodayStats = 38;
        static final int TRANSACTION_setIsSyncable = 19;
        static final int TRANSACTION_setMasterSyncAutomatically = 20;
        static final int TRANSACTION_setMasterSyncAutomaticallyAsUser = 21;
        static final int TRANSACTION_setSyncAutomatically = 12;
        static final int TRANSACTION_setSyncAutomaticallyAsUser = 13;
        static final int TRANSACTION_sync = 5;
        static final int TRANSACTION_syncAsUser = 6;
        static final int TRANSACTION_unregisterContentObserver = 1;

        private static class Proxy implements IContentService {
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

            public void unregisterContentObserver(IContentObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerContentObserver(Uri uri, boolean notifyForDescendants, IContentObserver observer, int userHandle, int targetSdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(notifyForDescendants);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userHandle);
                    _data.writeInt(targetSdkVersion);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyChange(Uri uri, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int userHandle, int targetSdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(observerWantsSelfNotifications);
                    _data.writeInt(flags);
                    _data.writeInt(userHandle);
                    _data.writeInt(targetSdkVersion);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestSync(Account account, String authority, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sync(SyncRequest request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void syncAsUser(SyncRequest request, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelSync(Account account, String authority, ComponentName cname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(1);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelSyncAsUser(Account account, String authority, ComponentName cname, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(1);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelRequest(SyncRequest request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getSyncAutomatically(Account account, String providerName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    this.mRemote.transact(10, _data, _reply, 0);
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

            public boolean getSyncAutomaticallyAsUser(Account account, String providerName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    _data.writeInt(userId);
                    this.mRemote.transact(11, _data, _reply, 0);
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

            public void setSyncAutomatically(Account account, String providerName, boolean sync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    _data.writeInt(sync);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSyncAutomaticallyAsUser(Account account, String providerName, boolean sync, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    _data.writeInt(sync);
                    _data.writeInt(userId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PeriodicSync> getPeriodicSyncs(Account account, String providerName, ComponentName cname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (cname != null) {
                        _data.writeInt(1);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PeriodicSync.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPeriodicSync(Account account, String providerName, Bundle extras, long pollFrequency) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(pollFrequency);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePeriodicSync(Account account, String providerName, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
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

            public int getIsSyncable(Account account, String providerName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getIsSyncableAsUser(Account account, String providerName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    _data.writeInt(userId);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIsSyncable(Account account, String providerName, int syncable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    _data.writeInt(syncable);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMasterSyncAutomatically(boolean flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMasterSyncAutomaticallyAsUser(boolean flag, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    _data.writeInt(userId);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getMasterSyncAutomatically() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(22, _data, _reply, 0);
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

            public boolean getMasterSyncAutomaticallyAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(23, _data, _reply, 0);
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

            public List<SyncInfo> getCurrentSyncs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(SyncInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<SyncInfo> getCurrentSyncsAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(SyncInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SyncAdapterType[] getSyncAdapterTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return (SyncAdapterType[]) _reply.createTypedArray(SyncAdapterType.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SyncAdapterType[] getSyncAdapterTypesAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return (SyncAdapterType[]) _reply.createTypedArray(SyncAdapterType.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(authority);
                    _data.writeInt(userId);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSyncActive(Account account, String authority, ComponentName cname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(1);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, _reply, 0);
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

            public SyncStatusInfo getSyncStatus(Account account, String authority, ComponentName cname) throws RemoteException {
                SyncStatusInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(1);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SyncStatusInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SyncStatusInfo getSyncStatusAsUser(Account account, String authority, ComponentName cname, int userId) throws RemoteException {
                SyncStatusInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(1);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SyncStatusInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSyncPending(Account account, String authority, ComponentName cname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(1);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(32, _data, _reply, 0);
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

            public boolean isSyncPendingAsUser(Account account, String authority, ComponentName cname, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(1);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(33, _data, _reply, 0);
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

            public void addStatusChangeListener(int mask, ISyncStatusObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mask);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeStatusChangeListener(ISyncStatusObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void putCache(String packageName, Uri key, Bundle value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (key != null) {
                        _data.writeInt(1);
                        key.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (value != null) {
                        _data.writeInt(1);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getCache(String packageName, Uri key, int userId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (key != null) {
                        _data.writeInt(1);
                        key.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetTodayStats() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(38, _data, _reply, 0);
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

        public static IContentService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IContentService)) {
                return new Proxy(obj);
            }
            return (IContentService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v18, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v22, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v45, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v50, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v67, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v71, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v75, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v79, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v83, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v87, resolved type: android.os.Bundle} */
        /* JADX WARNING: type inference failed for: r2v0 */
        /* JADX WARNING: type inference failed for: r2v12 */
        /* JADX WARNING: type inference failed for: r2v15 */
        /* JADX WARNING: type inference failed for: r2v26 */
        /* JADX WARNING: type inference failed for: r2v29 */
        /* JADX WARNING: type inference failed for: r2v33 */
        /* JADX WARNING: type inference failed for: r2v37 */
        /* JADX WARNING: type inference failed for: r2v41 */
        /* JADX WARNING: type inference failed for: r2v54 */
        /* JADX WARNING: type inference failed for: r2v58 */
        /* JADX WARNING: type inference failed for: r2v62 */
        /* JADX WARNING: type inference failed for: r2v91, types: [android.net.Uri] */
        /* JADX WARNING: type inference failed for: r2v95 */
        /* JADX WARNING: type inference failed for: r2v96 */
        /* JADX WARNING: type inference failed for: r2v97 */
        /* JADX WARNING: type inference failed for: r2v98 */
        /* JADX WARNING: type inference failed for: r2v99 */
        /* JADX WARNING: type inference failed for: r2v100 */
        /* JADX WARNING: type inference failed for: r2v101 */
        /* JADX WARNING: type inference failed for: r2v102 */
        /* JADX WARNING: type inference failed for: r2v103 */
        /* JADX WARNING: type inference failed for: r2v104 */
        /* JADX WARNING: type inference failed for: r2v105 */
        /* JADX WARNING: type inference failed for: r2v106 */
        /* JADX WARNING: type inference failed for: r2v107 */
        /* JADX WARNING: type inference failed for: r2v108 */
        /* JADX WARNING: type inference failed for: r2v109 */
        /* JADX WARNING: type inference failed for: r2v110 */
        /* JADX WARNING: type inference failed for: r2v111 */
        /* JADX WARNING: type inference failed for: r2v112 */
        /* JADX WARNING: type inference failed for: r2v113 */
        /* JADX WARNING: type inference failed for: r2v114 */
        /* JADX WARNING: type inference failed for: r2v115 */
        /* JADX WARNING: type inference failed for: r2v116 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri _arg0;
            Uri _arg02;
            Account _arg03;
            Account _arg04;
            Account _arg05;
            Account _arg06;
            Account _arg07;
            Bundle _arg2;
            Account _arg08;
            Account _arg09;
            Account _arg010;
            Account _arg011;
            Account _arg012;
            Account _arg013;
            Uri _arg1;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg014 = false;
                ? _arg22 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterContentObserver(IContentObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        registerContentObserver(_arg0, data.readInt() != 0, IContentObserver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        notifyChange(_arg02, IContentObserver.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0, data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg03 = null;
                        }
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        requestSync(_arg03, _arg12, _arg22);
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = SyncRequest.CREATOR.createFromParcel(parcel);
                        }
                        sync(_arg22);
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = SyncRequest.CREATOR.createFromParcel(parcel);
                        }
                        syncAsUser(_arg22, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg04 = null;
                        }
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        cancelSync(_arg04, _arg13, _arg22);
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg05 = null;
                        }
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        cancelSyncAsUser(_arg05, _arg14, _arg22, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = SyncRequest.CREATOR.createFromParcel(parcel);
                        }
                        cancelRequest(_arg22);
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result = getSyncAutomatically(_arg22, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result2 = getSyncAutomaticallyAsUser(_arg22, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        Account _arg015 = _arg22;
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg014 = true;
                        }
                        setSyncAutomatically(_arg015, _arg15, _arg014);
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        Account _arg016 = _arg22;
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg014 = true;
                        }
                        setSyncAutomaticallyAsUser(_arg016, _arg16, _arg014, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg06 = null;
                        }
                        String _arg17 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        List<PeriodicSync> _result3 = getPeriodicSyncs(_arg06, _arg17, _arg22);
                        reply.writeNoException();
                        parcel2.writeTypedList(_result3);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg07 = null;
                        }
                        String _arg18 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        addPeriodicSync(_arg07, _arg18, _arg2, data.readLong());
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg08 = null;
                        }
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        removePeriodicSync(_arg08, _arg19, _arg22);
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        int _result4 = getIsSyncable(_arg22, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        int _result5 = getIsSyncableAsUser(_arg22, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        setIsSyncable(_arg22, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = true;
                        }
                        setMasterSyncAutomatically(_arg014);
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = true;
                        }
                        setMasterSyncAutomaticallyAsUser(_arg014, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result6 = getMasterSyncAutomatically();
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result7 = getMasterSyncAutomaticallyAsUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<SyncInfo> _result8 = getCurrentSyncs();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result8);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<SyncInfo> _result9 = getCurrentSyncsAsUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result9);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        SyncAdapterType[] _result10 = getSyncAdapterTypes();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result10, 1);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        SyncAdapterType[] _result11 = getSyncAdapterTypesAsUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result11, 1);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result12 = getSyncAdapterPackagesForAuthorityAsUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result12);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg09 = null;
                        }
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result13 = isSyncActive(_arg09, _arg110, _arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg010 = null;
                        }
                        String _arg111 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        SyncStatusInfo _result14 = getSyncStatus(_arg010, _arg111, _arg22);
                        reply.writeNoException();
                        if (_result14 != null) {
                            parcel2.writeInt(1);
                            _result14.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg011 = null;
                        }
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        SyncStatusInfo _result15 = getSyncStatusAsUser(_arg011, _arg112, _arg22, data.readInt());
                        reply.writeNoException();
                        if (_result15 != null) {
                            parcel2.writeInt(1);
                            _result15.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg012 = null;
                        }
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result16 = isSyncPending(_arg012, _arg113, _arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg013 = null;
                        }
                        String _arg114 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result17 = isSyncPendingAsUser(_arg013, _arg114, _arg22, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        addStatusChangeListener(data.readInt(), ISyncStatusObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeStatusChangeListener(ISyncStatusObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        putCache(_arg017, _arg1, _arg22, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg018 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = Uri.CREATOR.createFromParcel(parcel);
                        }
                        Bundle _result18 = getCache(_arg018, _arg22, data.readInt());
                        reply.writeNoException();
                        if (_result18 != null) {
                            parcel2.writeInt(1);
                            _result18.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        resetTodayStats();
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

    void addPeriodicSync(Account account, String str, Bundle bundle, long j) throws RemoteException;

    void addStatusChangeListener(int i, ISyncStatusObserver iSyncStatusObserver) throws RemoteException;

    void cancelRequest(SyncRequest syncRequest) throws RemoteException;

    void cancelSync(Account account, String str, ComponentName componentName) throws RemoteException;

    void cancelSyncAsUser(Account account, String str, ComponentName componentName, int i) throws RemoteException;

    Bundle getCache(String str, Uri uri, int i) throws RemoteException;

    List<SyncInfo> getCurrentSyncs() throws RemoteException;

    List<SyncInfo> getCurrentSyncsAsUser(int i) throws RemoteException;

    int getIsSyncable(Account account, String str) throws RemoteException;

    int getIsSyncableAsUser(Account account, String str, int i) throws RemoteException;

    boolean getMasterSyncAutomatically() throws RemoteException;

    boolean getMasterSyncAutomaticallyAsUser(int i) throws RemoteException;

    List<PeriodicSync> getPeriodicSyncs(Account account, String str, ComponentName componentName) throws RemoteException;

    String[] getSyncAdapterPackagesForAuthorityAsUser(String str, int i) throws RemoteException;

    SyncAdapterType[] getSyncAdapterTypes() throws RemoteException;

    SyncAdapterType[] getSyncAdapterTypesAsUser(int i) throws RemoteException;

    boolean getSyncAutomatically(Account account, String str) throws RemoteException;

    boolean getSyncAutomaticallyAsUser(Account account, String str, int i) throws RemoteException;

    SyncStatusInfo getSyncStatus(Account account, String str, ComponentName componentName) throws RemoteException;

    SyncStatusInfo getSyncStatusAsUser(Account account, String str, ComponentName componentName, int i) throws RemoteException;

    boolean isSyncActive(Account account, String str, ComponentName componentName) throws RemoteException;

    boolean isSyncPending(Account account, String str, ComponentName componentName) throws RemoteException;

    boolean isSyncPendingAsUser(Account account, String str, ComponentName componentName, int i) throws RemoteException;

    void notifyChange(Uri uri, IContentObserver iContentObserver, boolean z, int i, int i2, int i3) throws RemoteException;

    void putCache(String str, Uri uri, Bundle bundle, int i) throws RemoteException;

    void registerContentObserver(Uri uri, boolean z, IContentObserver iContentObserver, int i, int i2) throws RemoteException;

    void removePeriodicSync(Account account, String str, Bundle bundle) throws RemoteException;

    void removeStatusChangeListener(ISyncStatusObserver iSyncStatusObserver) throws RemoteException;

    void requestSync(Account account, String str, Bundle bundle) throws RemoteException;

    void resetTodayStats() throws RemoteException;

    void setIsSyncable(Account account, String str, int i) throws RemoteException;

    void setMasterSyncAutomatically(boolean z) throws RemoteException;

    void setMasterSyncAutomaticallyAsUser(boolean z, int i) throws RemoteException;

    void setSyncAutomatically(Account account, String str, boolean z) throws RemoteException;

    void setSyncAutomaticallyAsUser(Account account, String str, boolean z, int i) throws RemoteException;

    void sync(SyncRequest syncRequest) throws RemoteException;

    void syncAsUser(SyncRequest syncRequest, int i) throws RemoteException;

    void unregisterContentObserver(IContentObserver iContentObserver) throws RemoteException;
}
