package android.content;

import android.accounts.Account;
import android.annotation.UnsupportedAppUsage;
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
    void addPeriodicSync(Account account, String str, Bundle bundle, long j) throws RemoteException;

    void addStatusChangeListener(int i, ISyncStatusObserver iSyncStatusObserver) throws RemoteException;

    void cancelRequest(SyncRequest syncRequest) throws RemoteException;

    @UnsupportedAppUsage
    void cancelSync(Account account, String str, ComponentName componentName) throws RemoteException;

    void cancelSyncAsUser(Account account, String str, ComponentName componentName, int i) throws RemoteException;

    Bundle getCache(String str, Uri uri, int i) throws RemoteException;

    List<SyncInfo> getCurrentSyncs() throws RemoteException;

    List<SyncInfo> getCurrentSyncsAsUser(int i) throws RemoteException;

    @UnsupportedAppUsage
    int getIsSyncable(Account account, String str) throws RemoteException;

    int getIsSyncableAsUser(Account account, String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean getMasterSyncAutomatically() throws RemoteException;

    boolean getMasterSyncAutomaticallyAsUser(int i) throws RemoteException;

    List<PeriodicSync> getPeriodicSyncs(Account account, String str, ComponentName componentName) throws RemoteException;

    String[] getSyncAdapterPackagesForAuthorityAsUser(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    SyncAdapterType[] getSyncAdapterTypes() throws RemoteException;

    SyncAdapterType[] getSyncAdapterTypesAsUser(int i) throws RemoteException;

    boolean getSyncAutomatically(Account account, String str) throws RemoteException;

    boolean getSyncAutomaticallyAsUser(Account account, String str, int i) throws RemoteException;

    SyncStatusInfo getSyncStatus(Account account, String str, ComponentName componentName) throws RemoteException;

    SyncStatusInfo getSyncStatusAsUser(Account account, String str, ComponentName componentName, int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean isSyncActive(Account account, String str, ComponentName componentName) throws RemoteException;

    boolean isSyncPending(Account account, String str, ComponentName componentName) throws RemoteException;

    boolean isSyncPendingAsUser(Account account, String str, ComponentName componentName, int i) throws RemoteException;

    void notifyChange(Uri uri, IContentObserver iContentObserver, boolean z, int i, int i2, int i3, String str) throws RemoteException;

    void onDbCorruption(String str, String str2, String str3) throws RemoteException;

    void putCache(String str, Uri uri, Bundle bundle, int i) throws RemoteException;

    void registerContentObserver(Uri uri, boolean z, IContentObserver iContentObserver, int i, int i2) throws RemoteException;

    void removePeriodicSync(Account account, String str, Bundle bundle) throws RemoteException;

    void removeStatusChangeListener(ISyncStatusObserver iSyncStatusObserver) throws RemoteException;

    void requestSync(Account account, String str, Bundle bundle, String str2) throws RemoteException;

    void resetTodayStats() throws RemoteException;

    void setIsSyncable(Account account, String str, int i) throws RemoteException;

    void setIsSyncableAsUser(Account account, String str, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void setMasterSyncAutomatically(boolean z) throws RemoteException;

    void setMasterSyncAutomaticallyAsUser(boolean z, int i) throws RemoteException;

    void setSyncAutomatically(Account account, String str, boolean z) throws RemoteException;

    void setSyncAutomaticallyAsUser(Account account, String str, boolean z, int i) throws RemoteException;

    void sync(SyncRequest syncRequest, String str) throws RemoteException;

    void syncAsUser(SyncRequest syncRequest, int i, String str) throws RemoteException;

    void unregisterContentObserver(IContentObserver iContentObserver) throws RemoteException;

    public static class Default implements IContentService {
        @Override // android.content.IContentService
        public void unregisterContentObserver(IContentObserver observer) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void registerContentObserver(Uri uri, boolean notifyForDescendants, IContentObserver observer, int userHandle, int targetSdkVersion) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void notifyChange(Uri uri, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int userHandle, int targetSdkVersion, String callingPackage) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void requestSync(Account account, String authority, Bundle extras, String callingPackage) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void sync(SyncRequest request, String callingPackage) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void syncAsUser(SyncRequest request, int userId, String callingPackage) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void cancelSync(Account account, String authority, ComponentName cname) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void cancelSyncAsUser(Account account, String authority, ComponentName cname, int userId) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void cancelRequest(SyncRequest request) throws RemoteException {
        }

        @Override // android.content.IContentService
        public boolean getSyncAutomatically(Account account, String providerName) throws RemoteException {
            return false;
        }

        @Override // android.content.IContentService
        public boolean getSyncAutomaticallyAsUser(Account account, String providerName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.IContentService
        public void setSyncAutomatically(Account account, String providerName, boolean sync) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void setSyncAutomaticallyAsUser(Account account, String providerName, boolean sync, int userId) throws RemoteException {
        }

        @Override // android.content.IContentService
        public List<PeriodicSync> getPeriodicSyncs(Account account, String providerName, ComponentName cname) throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public void addPeriodicSync(Account account, String providerName, Bundle extras, long pollFrequency) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void removePeriodicSync(Account account, String providerName, Bundle extras) throws RemoteException {
        }

        @Override // android.content.IContentService
        public int getIsSyncable(Account account, String providerName) throws RemoteException {
            return 0;
        }

        @Override // android.content.IContentService
        public int getIsSyncableAsUser(Account account, String providerName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.IContentService
        public void setIsSyncable(Account account, String providerName, int syncable) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void setIsSyncableAsUser(Account account, String providerName, int syncable, int userId) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void setMasterSyncAutomatically(boolean flag) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void setMasterSyncAutomaticallyAsUser(boolean flag, int userId) throws RemoteException {
        }

        @Override // android.content.IContentService
        public boolean getMasterSyncAutomatically() throws RemoteException {
            return false;
        }

        @Override // android.content.IContentService
        public boolean getMasterSyncAutomaticallyAsUser(int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.IContentService
        public List<SyncInfo> getCurrentSyncs() throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public List<SyncInfo> getCurrentSyncsAsUser(int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public SyncAdapterType[] getSyncAdapterTypes() throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public SyncAdapterType[] getSyncAdapterTypesAsUser(int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public boolean isSyncActive(Account account, String authority, ComponentName cname) throws RemoteException {
            return false;
        }

        @Override // android.content.IContentService
        public SyncStatusInfo getSyncStatus(Account account, String authority, ComponentName cname) throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public SyncStatusInfo getSyncStatusAsUser(Account account, String authority, ComponentName cname, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public boolean isSyncPending(Account account, String authority, ComponentName cname) throws RemoteException {
            return false;
        }

        @Override // android.content.IContentService
        public boolean isSyncPendingAsUser(Account account, String authority, ComponentName cname, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.IContentService
        public void addStatusChangeListener(int mask, ISyncStatusObserver callback) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void removeStatusChangeListener(ISyncStatusObserver callback) throws RemoteException {
        }

        @Override // android.content.IContentService
        public void putCache(String packageName, Uri key, Bundle value, int userId) throws RemoteException {
        }

        @Override // android.content.IContentService
        public Bundle getCache(String packageName, Uri key, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.IContentService
        public void resetTodayStats() throws RemoteException {
        }

        @Override // android.content.IContentService
        public void onDbCorruption(String tag, String message, String stacktrace) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IContentService {
        private static final String DESCRIPTOR = "android.content.IContentService";
        static final int TRANSACTION_addPeriodicSync = 15;
        static final int TRANSACTION_addStatusChangeListener = 35;
        static final int TRANSACTION_cancelRequest = 9;
        static final int TRANSACTION_cancelSync = 7;
        static final int TRANSACTION_cancelSyncAsUser = 8;
        static final int TRANSACTION_getCache = 38;
        static final int TRANSACTION_getCurrentSyncs = 25;
        static final int TRANSACTION_getCurrentSyncsAsUser = 26;
        static final int TRANSACTION_getIsSyncable = 17;
        static final int TRANSACTION_getIsSyncableAsUser = 18;
        static final int TRANSACTION_getMasterSyncAutomatically = 23;
        static final int TRANSACTION_getMasterSyncAutomaticallyAsUser = 24;
        static final int TRANSACTION_getPeriodicSyncs = 14;
        static final int TRANSACTION_getSyncAdapterPackagesForAuthorityAsUser = 29;
        static final int TRANSACTION_getSyncAdapterTypes = 27;
        static final int TRANSACTION_getSyncAdapterTypesAsUser = 28;
        static final int TRANSACTION_getSyncAutomatically = 10;
        static final int TRANSACTION_getSyncAutomaticallyAsUser = 11;
        static final int TRANSACTION_getSyncStatus = 31;
        static final int TRANSACTION_getSyncStatusAsUser = 32;
        static final int TRANSACTION_isSyncActive = 30;
        static final int TRANSACTION_isSyncPending = 33;
        static final int TRANSACTION_isSyncPendingAsUser = 34;
        static final int TRANSACTION_notifyChange = 3;
        static final int TRANSACTION_onDbCorruption = 40;
        static final int TRANSACTION_putCache = 37;
        static final int TRANSACTION_registerContentObserver = 2;
        static final int TRANSACTION_removePeriodicSync = 16;
        static final int TRANSACTION_removeStatusChangeListener = 36;
        static final int TRANSACTION_requestSync = 4;
        static final int TRANSACTION_resetTodayStats = 39;
        static final int TRANSACTION_setIsSyncable = 19;
        static final int TRANSACTION_setIsSyncableAsUser = 20;
        static final int TRANSACTION_setMasterSyncAutomatically = 21;
        static final int TRANSACTION_setMasterSyncAutomaticallyAsUser = 22;
        static final int TRANSACTION_setSyncAutomatically = 12;
        static final int TRANSACTION_setSyncAutomaticallyAsUser = 13;
        static final int TRANSACTION_sync = 5;
        static final int TRANSACTION_syncAsUser = 6;
        static final int TRANSACTION_unregisterContentObserver = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "unregisterContentObserver";
                case 2:
                    return "registerContentObserver";
                case 3:
                    return "notifyChange";
                case 4:
                    return "requestSync";
                case 5:
                    return "sync";
                case 6:
                    return "syncAsUser";
                case 7:
                    return "cancelSync";
                case 8:
                    return "cancelSyncAsUser";
                case 9:
                    return "cancelRequest";
                case 10:
                    return "getSyncAutomatically";
                case 11:
                    return "getSyncAutomaticallyAsUser";
                case 12:
                    return "setSyncAutomatically";
                case 13:
                    return "setSyncAutomaticallyAsUser";
                case 14:
                    return "getPeriodicSyncs";
                case 15:
                    return "addPeriodicSync";
                case 16:
                    return "removePeriodicSync";
                case 17:
                    return "getIsSyncable";
                case 18:
                    return "getIsSyncableAsUser";
                case 19:
                    return "setIsSyncable";
                case 20:
                    return "setIsSyncableAsUser";
                case 21:
                    return "setMasterSyncAutomatically";
                case 22:
                    return "setMasterSyncAutomaticallyAsUser";
                case 23:
                    return "getMasterSyncAutomatically";
                case 24:
                    return "getMasterSyncAutomaticallyAsUser";
                case 25:
                    return "getCurrentSyncs";
                case 26:
                    return "getCurrentSyncsAsUser";
                case 27:
                    return "getSyncAdapterTypes";
                case 28:
                    return "getSyncAdapterTypesAsUser";
                case 29:
                    return "getSyncAdapterPackagesForAuthorityAsUser";
                case 30:
                    return "isSyncActive";
                case 31:
                    return "getSyncStatus";
                case 32:
                    return "getSyncStatusAsUser";
                case 33:
                    return "isSyncPending";
                case 34:
                    return "isSyncPendingAsUser";
                case 35:
                    return "addStatusChangeListener";
                case 36:
                    return "removeStatusChangeListener";
                case 37:
                    return "putCache";
                case 38:
                    return "getCache";
                case 39:
                    return "resetTodayStats";
                case 40:
                    return "onDbCorruption";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri _arg0;
            Uri _arg02;
            Account _arg03;
            Bundle _arg2;
            SyncRequest _arg04;
            SyncRequest _arg05;
            Account _arg06;
            ComponentName _arg22;
            Account _arg07;
            ComponentName _arg23;
            SyncRequest _arg08;
            Account _arg09;
            Account _arg010;
            Account _arg011;
            Account _arg012;
            Account _arg013;
            ComponentName _arg24;
            Account _arg014;
            Bundle _arg25;
            Account _arg015;
            Bundle _arg26;
            Account _arg016;
            Account _arg017;
            Account _arg018;
            Account _arg019;
            Account _arg020;
            ComponentName _arg27;
            Account _arg021;
            ComponentName _arg28;
            Account _arg022;
            ComponentName _arg29;
            Account _arg023;
            ComponentName _arg210;
            Account _arg024;
            ComponentName _arg211;
            Uri _arg1;
            Bundle _arg212;
            Uri _arg12;
            if (code != 1598968902) {
                boolean _arg025 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterContentObserver(IContentObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        registerContentObserver(_arg0, data.readInt() != 0, IContentObserver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        notifyChange(_arg02, IContentObserver.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0, data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        requestSync(_arg03, _arg13, _arg2, data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = SyncRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        sync(_arg04, data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = SyncRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        syncAsUser(_arg05, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        cancelSync(_arg06, _arg14, _arg22);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        cancelSyncAsUser(_arg07, _arg15, _arg23, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = SyncRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        cancelRequest(_arg08);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        boolean syncAutomatically = getSyncAutomatically(_arg09, data.readString());
                        reply.writeNoException();
                        reply.writeInt(syncAutomatically ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        boolean syncAutomaticallyAsUser = getSyncAutomaticallyAsUser(_arg010, data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(syncAutomaticallyAsUser ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg011 = null;
                        }
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg025 = true;
                        }
                        setSyncAutomatically(_arg011, _arg16, _arg025);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg012 = null;
                        }
                        String _arg17 = data.readString();
                        if (data.readInt() != 0) {
                            _arg025 = true;
                        }
                        setSyncAutomaticallyAsUser(_arg012, _arg17, _arg025, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg013 = null;
                        }
                        String _arg18 = data.readString();
                        if (data.readInt() != 0) {
                            _arg24 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        List<PeriodicSync> _result = getPeriodicSyncs(_arg013, _arg18, _arg24);
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg014 = null;
                        }
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg25 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        addPeriodicSync(_arg014, _arg19, _arg25, data.readLong());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg015 = null;
                        }
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg26 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        removePeriodicSync(_arg015, _arg110, _arg26);
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg016 = null;
                        }
                        int _result2 = getIsSyncable(_arg016, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg017 = null;
                        }
                        int _result3 = getIsSyncableAsUser(_arg017, data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg018 = null;
                        }
                        setIsSyncable(_arg018, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg019 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg019 = null;
                        }
                        setIsSyncableAsUser(_arg019, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg025 = true;
                        }
                        setMasterSyncAutomatically(_arg025);
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg025 = true;
                        }
                        setMasterSyncAutomaticallyAsUser(_arg025, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean masterSyncAutomatically = getMasterSyncAutomatically();
                        reply.writeNoException();
                        reply.writeInt(masterSyncAutomatically ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean masterSyncAutomaticallyAsUser = getMasterSyncAutomaticallyAsUser(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(masterSyncAutomaticallyAsUser ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        List<SyncInfo> _result4 = getCurrentSyncs();
                        reply.writeNoException();
                        reply.writeTypedList(_result4);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        List<SyncInfo> _result5 = getCurrentSyncsAsUser(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result5);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        SyncAdapterType[] _result6 = getSyncAdapterTypes();
                        reply.writeNoException();
                        reply.writeTypedArray(_result6, 1);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        SyncAdapterType[] _result7 = getSyncAdapterTypesAsUser(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result7, 1);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result8 = getSyncAdapterPackagesForAuthorityAsUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result8);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg020 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg020 = null;
                        }
                        String _arg111 = data.readString();
                        if (data.readInt() != 0) {
                            _arg27 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg27 = null;
                        }
                        boolean isSyncActive = isSyncActive(_arg020, _arg111, _arg27);
                        reply.writeNoException();
                        reply.writeInt(isSyncActive ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg021 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg021 = null;
                        }
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg28 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg28 = null;
                        }
                        SyncStatusInfo _result9 = getSyncStatus(_arg021, _arg112, _arg28);
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg022 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg022 = null;
                        }
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg29 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg29 = null;
                        }
                        SyncStatusInfo _result10 = getSyncStatusAsUser(_arg022, _arg113, _arg29, data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg023 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg023 = null;
                        }
                        String _arg114 = data.readString();
                        if (data.readInt() != 0) {
                            _arg210 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg210 = null;
                        }
                        boolean isSyncPending = isSyncPending(_arg023, _arg114, _arg210);
                        reply.writeNoException();
                        reply.writeInt(isSyncPending ? 1 : 0);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg024 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg024 = null;
                        }
                        String _arg115 = data.readString();
                        if (data.readInt() != 0) {
                            _arg211 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg211 = null;
                        }
                        boolean isSyncPendingAsUser = isSyncPendingAsUser(_arg024, _arg115, _arg211, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSyncPendingAsUser ? 1 : 0);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        addStatusChangeListener(data.readInt(), ISyncStatusObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        removeStatusChangeListener(ISyncStatusObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg026 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg212 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg212 = null;
                        }
                        putCache(_arg026, _arg1, _arg212, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg027 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        Bundle _result11 = getCache(_arg027, _arg12, data.readInt());
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        resetTodayStats();
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        onDbCorruption(data.readString(), data.readString(), data.readString());
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

        /* access modifiers changed from: private */
        public static class Proxy implements IContentService {
            public static IContentService sDefaultImpl;
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

            @Override // android.content.IContentService
            public void unregisterContentObserver(IContentObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterContentObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void registerContentObserver(Uri uri, boolean notifyForDescendants, IContentObserver observer, int userHandle, int targetSdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!notifyForDescendants) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userHandle);
                    _data.writeInt(targetSdkVersion);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerContentObserver(uri, notifyForDescendants, observer, userHandle, targetSdkVersion);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void notifyChange(Uri uri, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int userHandle, int targetSdkVersion, String callingPackage) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (!observerWantsSelfNotifications) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    try {
                        _data.writeInt(flags);
                        try {
                            _data.writeInt(userHandle);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(targetSdkVersion);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPackage);
                        if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().notifyChange(uri, observer, observerWantsSelfNotifications, flags, userHandle, targetSdkVersion, callingPackage);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.IContentService
            public void requestSync(Account account, String authority, Bundle extras, String callingPackage) throws RemoteException {
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
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestSync(account, authority, extras, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void sync(SyncRequest request, String callingPackage) throws RemoteException {
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
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sync(request, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void syncAsUser(SyncRequest request, int userId, String callingPackage) throws RemoteException {
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
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().syncAsUser(request, userId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelSync(account, authority, cname);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelSyncAsUser(account, authority, cname, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelRequest(request);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSyncAutomatically(account, providerName);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSyncAutomaticallyAsUser(account, providerName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void setSyncAutomatically(Account account, String providerName, boolean sync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(providerName);
                    if (!sync) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSyncAutomatically(account, providerName, sync);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void setSyncAutomaticallyAsUser(Account account, String providerName, boolean sync, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (account != null) {
                        _data.writeInt(1);
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
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSyncAutomaticallyAsUser(account, providerName, sync, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPeriodicSyncs(account, providerName, cname);
                    }
                    _reply.readException();
                    List<PeriodicSync> _result = _reply.createTypedArrayList(PeriodicSync.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addPeriodicSync(account, providerName, extras, pollFrequency);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removePeriodicSync(account, providerName, extras);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsSyncable(account, providerName);
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

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsSyncableAsUser(account, providerName, userId);
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

            @Override // android.content.IContentService
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
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIsSyncable(account, providerName, syncable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void setIsSyncableAsUser(Account account, String providerName, int syncable, int userId) throws RemoteException {
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
                    _data.writeInt(userId);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIsSyncableAsUser(account, providerName, syncable, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void setMasterSyncAutomatically(boolean flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag ? 1 : 0);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMasterSyncAutomatically(flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void setMasterSyncAutomaticallyAsUser(boolean flag, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMasterSyncAutomaticallyAsUser(flag, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public boolean getMasterSyncAutomatically() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMasterSyncAutomatically();
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

            @Override // android.content.IContentService
            public boolean getMasterSyncAutomaticallyAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMasterSyncAutomaticallyAsUser(userId);
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

            @Override // android.content.IContentService
            public List<SyncInfo> getCurrentSyncs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentSyncs();
                    }
                    _reply.readException();
                    List<SyncInfo> _result = _reply.createTypedArrayList(SyncInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public List<SyncInfo> getCurrentSyncsAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentSyncsAsUser(userId);
                    }
                    _reply.readException();
                    List<SyncInfo> _result = _reply.createTypedArrayList(SyncInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public SyncAdapterType[] getSyncAdapterTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSyncAdapterTypes();
                    }
                    _reply.readException();
                    SyncAdapterType[] _result = (SyncAdapterType[]) _reply.createTypedArray(SyncAdapterType.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public SyncAdapterType[] getSyncAdapterTypesAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSyncAdapterTypesAsUser(userId);
                    }
                    _reply.readException();
                    SyncAdapterType[] _result = (SyncAdapterType[]) _reply.createTypedArray(SyncAdapterType.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(authority);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSyncActive(account, authority, cname);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSyncStatus(account, authority, cname);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SyncStatusInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSyncStatusAsUser(account, authority, cname, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SyncStatusInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSyncPending(account, authority, cname);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSyncPendingAsUser(account, authority, cname, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void addStatusChangeListener(int mask, ISyncStatusObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mask);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addStatusChangeListener(mask, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void removeStatusChangeListener(ISyncStatusObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeStatusChangeListener(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().putCache(packageName, key, value, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
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
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCache(packageName, key, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void resetTodayStats() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetTodayStats();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.IContentService
            public void onDbCorruption(String tag, String message, String stacktrace) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tag);
                    _data.writeString(message);
                    _data.writeString(stacktrace);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDbCorruption(tag, message, stacktrace);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IContentService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IContentService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
