package android.content;

import android.accounts.Account;
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
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterContentObserver, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerContentObserver(Uri uri, boolean notifyForDescendants, IContentObserver observer, int userHandle) throws RemoteException {
                int i = Stub.TRANSACTION_unregisterContentObserver;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!notifyForDescendants) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_registerContentObserver, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyChange(Uri uri, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int userHandle) throws RemoteException {
                int i = Stub.TRANSACTION_unregisterContentObserver;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!observerWantsSelfNotifications) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(flags);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_notifyChange, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_requestSync, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sync, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_syncAsUser, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_cancelSync, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_cancelSyncAsUser, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_cancelRequest, _data, _reply, 0);
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
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    this.mRemote.transact(Stub.TRANSACTION_getSyncAutomatically, _data, _reply, 0);
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

            public boolean getSyncAutomaticallyAsUser(Account account, String providerName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getSyncAutomaticallyAsUser, _data, _reply, 0);
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

            public void setSyncAutomatically(Account account, String providerName, boolean sync) throws RemoteException {
                int i = Stub.TRANSACTION_unregisterContentObserver;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (!sync) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setSyncAutomatically, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSyncAutomaticallyAsUser(Account account, String providerName, boolean sync, int userId) throws RemoteException {
                int i = Stub.TRANSACTION_unregisterContentObserver;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (!sync) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setSyncAutomaticallyAsUser, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (cname != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getPeriodicSyncs, _data, _reply, 0);
                    _reply.readException();
                    List<PeriodicSync> _result = _reply.createTypedArrayList(PeriodicSync.CREATOR);
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(pollFrequency);
                    this.mRemote.transact(Stub.TRANSACTION_addPeriodicSync, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_removePeriodicSync, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    this.mRemote.transact(Stub.TRANSACTION_getIsSyncable, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getIsSyncableAsUser, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    _data.writeInt(syncable);
                    this.mRemote.transact(Stub.TRANSACTION_setIsSyncable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMasterSyncAutomatically(boolean flag) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (flag) {
                        i = Stub.TRANSACTION_unregisterContentObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setMasterSyncAutomatically, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMasterSyncAutomaticallyAsUser(boolean flag, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (flag) {
                        i = Stub.TRANSACTION_unregisterContentObserver;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setMasterSyncAutomaticallyAsUser, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getMasterSyncAutomatically, _data, _reply, 0);
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

            public boolean getMasterSyncAutomaticallyAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getMasterSyncAutomaticallyAsUser, _data, _reply, 0);
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

            public List<SyncInfo> getCurrentSyncs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentSyncs, _data, _reply, 0);
                    _reply.readException();
                    List<SyncInfo> _result = _reply.createTypedArrayList(SyncInfo.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentSyncsAsUser, _data, _reply, 0);
                    _reply.readException();
                    List<SyncInfo> _result = _reply.createTypedArrayList(SyncInfo.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSyncAdapterTypes, _data, _reply, 0);
                    _reply.readException();
                    SyncAdapterType[] _result = (SyncAdapterType[]) _reply.createTypedArray(SyncAdapterType.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSyncAdapterTypesAsUser, _data, _reply, 0);
                    _reply.readException();
                    SyncAdapterType[] _result = (SyncAdapterType[]) _reply.createTypedArray(SyncAdapterType.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSyncAdapterPackagesForAuthorityAsUser, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isSyncActive, _data, _reply, 0);
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

            public SyncStatusInfo getSyncStatus(Account account, String authority, ComponentName cname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SyncStatusInfo syncStatusInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getSyncStatus, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        syncStatusInfo = (SyncStatusInfo) SyncStatusInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        syncStatusInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return syncStatusInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SyncStatusInfo getSyncStatusAsUser(Account account, String authority, ComponentName cname, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SyncStatusInfo syncStatusInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getSyncStatusAsUser, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        syncStatusInfo = (SyncStatusInfo) SyncStatusInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        syncStatusInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return syncStatusInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSyncPending(Account account, String authority, ComponentName cname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isSyncPending, _data, _reply, 0);
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

            public boolean isSyncPendingAsUser(Account account, String authority, ComponentName cname, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    if (cname != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        cname.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isSyncPendingAsUser, _data, _reply, 0);
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

            public void addStatusChangeListener(int mask, ISyncStatusObserver callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mask);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addStatusChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeStatusChangeListener(ISyncStatusObserver callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeStatusChangeListener, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        key.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (value != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_putCache, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getCache(String packageName, Uri key, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (key != null) {
                        _data.writeInt(Stub.TRANSACTION_unregisterContentObserver);
                        key.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getCache, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri uri;
            Account account;
            String _arg1;
            Bundle bundle;
            SyncRequest syncRequest;
            ComponentName componentName;
            boolean _result;
            int _result2;
            List<SyncInfo> _result3;
            SyncAdapterType[] _result4;
            SyncStatusInfo _result5;
            String _arg0;
            Uri uri2;
            switch (code) {
                case TRANSACTION_unregisterContentObserver /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterContentObserver(android.database.IContentObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerContentObserver /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    registerContentObserver(uri, data.readInt() != 0, android.database.IContentObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifyChange /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    notifyChange(uri, android.database.IContentObserver.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestSync /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    requestSync(account, _arg1, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sync /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        syncRequest = (SyncRequest) SyncRequest.CREATOR.createFromParcel(data);
                    } else {
                        syncRequest = null;
                    }
                    sync(syncRequest);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_syncAsUser /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        syncRequest = (SyncRequest) SyncRequest.CREATOR.createFromParcel(data);
                    } else {
                        syncRequest = null;
                    }
                    syncAsUser(syncRequest, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelSync /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    cancelSync(account, _arg1, componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelSyncAsUser /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    cancelSyncAsUser(account, _arg1, componentName, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelRequest /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        syncRequest = (SyncRequest) SyncRequest.CREATOR.createFromParcel(data);
                    } else {
                        syncRequest = null;
                    }
                    cancelRequest(syncRequest);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSyncAutomatically /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result = getSyncAutomatically(account, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_unregisterContentObserver : 0);
                    return true;
                case TRANSACTION_getSyncAutomaticallyAsUser /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result = getSyncAutomaticallyAsUser(account, data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_unregisterContentObserver : 0);
                    return true;
                case TRANSACTION_setSyncAutomatically /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    setSyncAutomatically(account, data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSyncAutomaticallyAsUser /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    setSyncAutomaticallyAsUser(account, data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPeriodicSyncs /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    List<PeriodicSync> _result6 = getPeriodicSyncs(account, _arg1, componentName);
                    reply.writeNoException();
                    reply.writeTypedList(_result6);
                    return true;
                case TRANSACTION_addPeriodicSync /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    addPeriodicSync(account, _arg1, bundle, data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removePeriodicSync /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    removePeriodicSync(account, _arg1, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getIsSyncable /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result2 = getIsSyncable(account, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getIsSyncableAsUser /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result2 = getIsSyncableAsUser(account, data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setIsSyncable /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    setIsSyncable(account, data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setMasterSyncAutomatically /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMasterSyncAutomatically(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setMasterSyncAutomaticallyAsUser /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMasterSyncAutomaticallyAsUser(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getMasterSyncAutomatically /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMasterSyncAutomatically();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_unregisterContentObserver : 0);
                    return true;
                case TRANSACTION_getMasterSyncAutomaticallyAsUser /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMasterSyncAutomaticallyAsUser(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_unregisterContentObserver : 0);
                    return true;
                case TRANSACTION_getCurrentSyncs /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCurrentSyncs();
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case TRANSACTION_getCurrentSyncsAsUser /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCurrentSyncsAsUser(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case TRANSACTION_getSyncAdapterTypes /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSyncAdapterTypes();
                    reply.writeNoException();
                    reply.writeTypedArray(_result4, TRANSACTION_unregisterContentObserver);
                    return true;
                case TRANSACTION_getSyncAdapterTypesAsUser /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSyncAdapterTypesAsUser(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result4, TRANSACTION_unregisterContentObserver);
                    return true;
                case TRANSACTION_getSyncAdapterPackagesForAuthorityAsUser /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result7 = getSyncAdapterPackagesForAuthorityAsUser(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringArray(_result7);
                    return true;
                case TRANSACTION_isSyncActive /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result = isSyncActive(account, _arg1, componentName);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_unregisterContentObserver : 0);
                    return true;
                case TRANSACTION_getSyncStatus /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result5 = getSyncStatus(account, _arg1, componentName);
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_unregisterContentObserver);
                        _result5.writeToParcel(reply, TRANSACTION_unregisterContentObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSyncStatusAsUser /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result5 = getSyncStatusAsUser(account, _arg1, componentName, data.readInt());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_unregisterContentObserver);
                        _result5.writeToParcel(reply, TRANSACTION_unregisterContentObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isSyncPending /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result = isSyncPending(account, _arg1, componentName);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_unregisterContentObserver : 0);
                    return true;
                case TRANSACTION_isSyncPendingAsUser /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    _result = isSyncPendingAsUser(account, _arg1, componentName, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_unregisterContentObserver : 0);
                    return true;
                case TRANSACTION_addStatusChangeListener /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    addStatusChangeListener(data.readInt(), android.content.ISyncStatusObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeStatusChangeListener /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeStatusChangeListener(android.content.ISyncStatusObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_putCache /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        uri2 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri2 = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    putCache(_arg0, uri2, bundle, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCache /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        uri2 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri2 = null;
                    }
                    Bundle _result8 = getCache(_arg0, uri2, data.readInt());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(TRANSACTION_unregisterContentObserver);
                        _result8.writeToParcel(reply, TRANSACTION_unregisterContentObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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

    void notifyChange(Uri uri, IContentObserver iContentObserver, boolean z, int i, int i2) throws RemoteException;

    void putCache(String str, Uri uri, Bundle bundle, int i) throws RemoteException;

    void registerContentObserver(Uri uri, boolean z, IContentObserver iContentObserver, int i) throws RemoteException;

    void removePeriodicSync(Account account, String str, Bundle bundle) throws RemoteException;

    void removeStatusChangeListener(ISyncStatusObserver iSyncStatusObserver) throws RemoteException;

    void requestSync(Account account, String str, Bundle bundle) throws RemoteException;

    void setIsSyncable(Account account, String str, int i) throws RemoteException;

    void setMasterSyncAutomatically(boolean z) throws RemoteException;

    void setMasterSyncAutomaticallyAsUser(boolean z, int i) throws RemoteException;

    void setSyncAutomatically(Account account, String str, boolean z) throws RemoteException;

    void setSyncAutomaticallyAsUser(Account account, String str, boolean z, int i) throws RemoteException;

    void sync(SyncRequest syncRequest) throws RemoteException;

    void syncAsUser(SyncRequest syncRequest, int i) throws RemoteException;

    void unregisterContentObserver(IContentObserver iContentObserver) throws RemoteException;
}
