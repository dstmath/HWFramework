package com.android.server.content;

import android.accounts.Account;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.job.JobInfo;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentService.Stub;
import android.content.ISyncStatusObserver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.PeriodicSync;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncRequest;
import android.content.SyncStatusInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageManagerInternal.SyncAdapterPackagesProvider;
import android.content.pm.ProviderInfo;
import android.database.IContentObserver;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FactoryTest;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.HwSysResource;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.content.SyncStorageEngine.EndPoint;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ContentService extends Stub {
    static final boolean DEBUG = false;
    static final String TAG = "ContentService";
    private static HwSysResource mContentObserverResource = null;
    private static PackageManager mPackageManager = null;
    @GuardedBy("mCache")
    private final SparseArray<ArrayMap<String, ArrayMap<Pair<String, Uri>, Bundle>>> mCache = new SparseArray();
    private BroadcastReceiver mCacheReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            synchronized (ContentService.this.mCache) {
                if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                    ContentService.this.mCache.clear();
                } else {
                    Uri data = intent.getData();
                    if (data != null) {
                        int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                        String packageName = data.getSchemeSpecificPart();
                        ContentService.this.invalidateCacheLocked(userId, packageName, null);
                        if ("android.intent.action.PACKAGE_CHANGED".equals(intent.getAction()) || "android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                            int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                            if (!(ContentService.mContentObserverResource == null || uid == -1)) {
                                ContentService.mContentObserverResource.clear(uid, packageName, -1);
                            }
                        }
                    }
                }
            }
        }
    };
    private Context mContext;
    private boolean mFactoryTest;
    private final ObserverNode mRootNode = new ObserverNode("");
    private SyncManager mSyncManager = null;
    private final Object mSyncManagerLock = new Object();

    public static class Lifecycle extends SystemService {
        private ContentService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mService = new ContentService(getContext(), FactoryTest.getMode() == 1);
            publishBinderService("content", this.mService);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemReady();
            }
        }

        public void onCleanupUser(int userHandle) {
            synchronized (this.mService.mCache) {
                this.mService.mCache.remove(userHandle);
            }
        }
    }

    public static final class ObserverCall {
        final ObserverNode mNode;
        final IContentObserver mObserver;
        final int mObserverUserId;
        final boolean mSelfChange;

        ObserverCall(ObserverNode node, IContentObserver observer, boolean selfChange, int observerUserId) {
            this.mNode = node;
            this.mObserver = observer;
            this.mSelfChange = selfChange;
            this.mObserverUserId = observerUserId;
        }
    }

    public static final class ObserverNode {
        public static final int DELETE_TYPE = 2;
        public static final int INSERT_TYPE = 0;
        public static final int UPDATE_TYPE = 1;
        private ArrayList<ObserverNode> mChildren = new ArrayList();
        private String mName;
        private ArrayList<ObserverEntry> mObservers = new ArrayList();

        private class ObserverEntry implements DeathRecipient {
            public final boolean notifyForDescendants;
            public final IContentObserver observer;
            private final Object observersLock;
            public final int pid;
            public final int uid;
            private final int userHandle;

            public ObserverEntry(IContentObserver o, boolean n, Object observersLock, int _uid, int _pid, int _userHandle) {
                this.observersLock = observersLock;
                this.observer = o;
                this.uid = _uid;
                this.pid = _pid;
                this.userHandle = _userHandle;
                this.notifyForDescendants = n;
                try {
                    this.observer.asBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    binderDied();
                }
            }

            public void binderDied() {
                synchronized (this.observersLock) {
                    ObserverNode.this.removeObserverLocked(this.observer);
                }
            }

            public void dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, String name, String prefix, SparseIntArray pidCounts) {
                Object obj = null;
                pidCounts.put(this.pid, pidCounts.get(this.pid) + 1);
                pw.print(prefix);
                pw.print(name);
                pw.print(": pid=");
                pw.print(this.pid);
                pw.print(" uid=");
                pw.print(this.uid);
                pw.print(" user=");
                pw.print(this.userHandle);
                pw.print(" target=");
                if (this.observer != null) {
                    obj = this.observer.asBinder();
                }
                pw.println(Integer.toHexString(System.identityHashCode(obj)));
            }
        }

        public ObserverNode(String name) {
            this.mName = name;
        }

        public void dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, String name, String prefix, int[] counts, SparseIntArray pidCounts) {
            int i;
            String innerName = null;
            if (this.mObservers.size() > 0) {
                if ("".equals(name)) {
                    innerName = this.mName;
                } else {
                    innerName = name + "/" + this.mName;
                }
                for (i = 0; i < this.mObservers.size(); i++) {
                    counts[1] = counts[1] + 1;
                    ((ObserverEntry) this.mObservers.get(i)).dumpLocked(fd, pw, args, innerName, prefix, pidCounts);
                }
            }
            if (this.mChildren.size() > 0) {
                if (innerName == null) {
                    if ("".equals(name)) {
                        innerName = this.mName;
                    } else {
                        innerName = name + "/" + this.mName;
                    }
                }
                for (i = 0; i < this.mChildren.size(); i++) {
                    counts[0] = counts[0] + 1;
                    ((ObserverNode) this.mChildren.get(i)).dumpLocked(fd, pw, args, innerName, prefix, counts, pidCounts);
                }
            }
        }

        private String getUriSegment(Uri uri, int index) {
            if (uri == null) {
                return null;
            }
            if (index == 0) {
                return uri.getAuthority();
            }
            return (String) uri.getPathSegments().get(index - 1);
        }

        private int countUriSegments(Uri uri) {
            if (uri == null) {
                return 0;
            }
            return uri.getPathSegments().size() + 1;
        }

        public void addObserverLocked(Uri uri, IContentObserver observer, boolean notifyForDescendants, Object observersLock, int uid, int pid, int userHandle) {
            addObserverLocked(uri, 0, observer, notifyForDescendants, observersLock, uid, pid, userHandle);
        }

        private void addObserverLocked(Uri uri, int index, IContentObserver observer, boolean notifyForDescendants, Object observersLock, int uid, int pid, int userHandle) {
            if (index == countUriSegments(uri)) {
                this.mObservers.add(new ObserverEntry(observer, notifyForDescendants, observersLock, uid, pid, userHandle));
                if (ContentService.mContentObserverResource == null) {
                    ContentService.mContentObserverResource = HwFrameworkFactory.getHwResource(29);
                }
                if (!(ContentService.mContentObserverResource == null || ContentService.mPackageManager == null)) {
                    String packageName = ContentService.mPackageManager.getNameForUid(uid);
                    if (packageName != null) {
                        ContentService.mContentObserverResource.acquire(uid, packageName, -1);
                    }
                }
                return;
            }
            String segment = getUriSegment(uri, index);
            if (segment == null) {
                throw new IllegalArgumentException("Invalid Uri (" + uri + ") used for observer");
            }
            ObserverNode node;
            int N = this.mChildren.size();
            for (int i = 0; i < N; i++) {
                node = (ObserverNode) this.mChildren.get(i);
                if (node.mName.equals(segment)) {
                    node.addObserverLocked(uri, index + 1, observer, notifyForDescendants, observersLock, uid, pid, userHandle);
                    return;
                }
            }
            node = new ObserverNode(segment);
            this.mChildren.add(node);
            node.addObserverLocked(uri, index + 1, observer, notifyForDescendants, observersLock, uid, pid, userHandle);
        }

        public boolean removeObserverLocked(IContentObserver observer) {
            int size = this.mChildren.size();
            int i = 0;
            while (i < size) {
                if (((ObserverNode) this.mChildren.get(i)).removeObserverLocked(observer)) {
                    this.mChildren.remove(i);
                    i--;
                    size--;
                }
                i++;
            }
            IBinder observerBinder = observer.asBinder();
            size = this.mObservers.size();
            for (i = 0; i < size; i++) {
                ObserverEntry entry = (ObserverEntry) this.mObservers.get(i);
                if (entry.observer.asBinder() == observerBinder) {
                    this.mObservers.remove(i);
                    if (!(ContentService.mContentObserverResource == null || ContentService.mPackageManager == null)) {
                        int uid = entry.uid;
                        String packageName = ContentService.mPackageManager.getNameForUid(uid);
                        if (packageName != null) {
                            ContentService.mContentObserverResource.release(uid, packageName, -1);
                        }
                    }
                    observerBinder.unlinkToDeath(entry, 0);
                    if (this.mChildren.size() == 0 || this.mObservers.size() != 0) {
                        return false;
                    }
                    return true;
                }
            }
            if (this.mChildren.size() == 0) {
            }
            return false;
        }

        private void collectMyObserversLocked(boolean leaf, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int targetUserHandle, ArrayList<ObserverCall> calls) {
            int N = this.mObservers.size();
            IBinder observerBinder = observer == null ? null : observer.asBinder();
            for (int i = 0; i < N; i++) {
                ObserverEntry entry = (ObserverEntry) this.mObservers.get(i);
                boolean selfChange = entry.observer.asBinder() == observerBinder;
                if ((!selfChange || (observerWantsSelfNotifications ^ 1) == 0) && !HwServiceFactory.getHwNLPManager().shouldSkipGoogleNlp(entry.pid) && (targetUserHandle == -1 || entry.userHandle == -1 || targetUserHandle == entry.userHandle)) {
                    if (leaf) {
                        if ((flags & 2) != 0 && entry.notifyForDescendants) {
                        }
                    } else if (!entry.notifyForDescendants) {
                    }
                    calls.add(new ObserverCall(this, entry.observer, selfChange, UserHandle.getUserId(entry.uid)));
                }
            }
        }

        public void collectObserversLocked(Uri uri, int index, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int targetUserHandle, ArrayList<ObserverCall> calls) {
            Object segment = null;
            int segmentCount = countUriSegments(uri);
            if (index >= segmentCount) {
                collectMyObserversLocked(true, observer, observerWantsSelfNotifications, flags, targetUserHandle, calls);
            } else if (index < segmentCount) {
                segment = getUriSegment(uri, index);
                collectMyObserversLocked(false, observer, observerWantsSelfNotifications, flags, targetUserHandle, calls);
            }
            int N = this.mChildren.size();
            for (int i = 0; i < N; i++) {
                ObserverNode node = (ObserverNode) this.mChildren.get(i);
                if (segment == null || node.mName.equals(segment)) {
                    node.collectObserversLocked(uri, index + 1, observer, observerWantsSelfNotifications, flags, targetUserHandle, calls);
                    if (segment != null) {
                        return;
                    }
                }
            }
        }
    }

    private SyncManager getSyncManager() {
        if (SystemProperties.getBoolean("config.disable_network", false)) {
            return null;
        }
        SyncManager syncManager;
        synchronized (this.mSyncManagerLock) {
            try {
                if (this.mSyncManager == null) {
                    this.mSyncManager = HwServiceFactory.createHwSyncManager(this.mContext, this.mFactoryTest);
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Can't create SyncManager", e);
            }
            syncManager = this.mSyncManager;
        }
        return syncManager;
    }

    protected synchronized void dump(FileDescriptor fd, PrintWriter pw_, String[] args) {
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw_)) {
            IndentingPrintWriter pw = new IndentingPrintWriter(pw_, "  ");
            long identityToken = clearCallingIdentity();
            try {
                int i;
                if (this.mSyncManager == null) {
                    pw.println("No SyncManager created!  (Disk full?)");
                } else {
                    this.mSyncManager.dump(fd, pw);
                }
                pw.println();
                pw.println("Observer tree:");
                synchronized (this.mRootNode) {
                    int[] counts = new int[2];
                    final SparseIntArray pidCounts = new SparseIntArray();
                    this.mRootNode.dumpLocked(fd, pw, args, "", "  ", counts, pidCounts);
                    pw.println();
                    ArrayList<Integer> sorted = new ArrayList();
                    for (i = 0; i < pidCounts.size(); i++) {
                        sorted.add(Integer.valueOf(pidCounts.keyAt(i)));
                    }
                    Collections.sort(sorted, new Comparator<Integer>() {
                        public int compare(Integer lhs, Integer rhs) {
                            int lc = pidCounts.get(lhs.intValue());
                            int rc = pidCounts.get(rhs.intValue());
                            if (lc < rc) {
                                return 1;
                            }
                            if (lc > rc) {
                                return -1;
                            }
                            return 0;
                        }
                    });
                    for (i = 0; i < sorted.size(); i++) {
                        int pid = ((Integer) sorted.get(i)).intValue();
                        pw.print("  pid ");
                        pw.print(pid);
                        pw.print(": ");
                        pw.print(pidCounts.get(pid));
                        pw.println(" observers");
                    }
                    pw.println();
                    pw.print(" Total number of nodes: ");
                    pw.println(counts[0]);
                    pw.print(" Total number of observers: ");
                    pw.println(counts[1]);
                }
                synchronized (this.mCache) {
                    pw.println();
                    pw.println("Cached content:");
                    pw.increaseIndent();
                    for (i = 0; i < this.mCache.size(); i++) {
                        pw.println("User " + this.mCache.keyAt(i) + ":");
                        pw.increaseIndent();
                        pw.println(this.mCache.valueAt(i));
                        pw.decreaseIndent();
                    }
                    pw.decreaseIndent();
                }
                restoreCallingIdentity(identityToken);
            } catch (Throwable th) {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Content Service Crash", e);
            }
            throw e;
        }
    }

    ContentService(Context context, boolean factoryTest) {
        this.mContext = context;
        this.mFactoryTest = factoryTest;
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setSyncAdapterPackagesprovider(new SyncAdapterPackagesProvider() {
            public String[] getPackages(String authority, int userId) {
                return ContentService.this.getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
            }
        });
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        this.mContext.registerReceiverAsUser(this.mCacheReceiver, UserHandle.ALL, packageFilter, null, null);
        IntentFilter localeFilter = new IntentFilter();
        localeFilter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mCacheReceiver, UserHandle.ALL, localeFilter, null, null);
        setPackageManager(this.mContext.getPackageManager());
    }

    private static void setPackageManager(PackageManager pm) {
        if (mPackageManager == null && pm != null) {
            mPackageManager = pm;
        }
    }

    void systemReady() {
        getSyncManager();
    }

    public void registerContentObserver(Uri uri, boolean notifyForDescendants, IContentObserver observer, int userHandle, int targetSdkVersion) {
        if (observer == null || uri == null) {
            throw new IllegalArgumentException("You must pass a valid uri and observer");
        }
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        userHandle = handleIncomingUser(uri, pid, uid, 1, true, userHandle);
        String msg = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).checkContentProviderAccess(uri.getAuthority(), userHandle);
        if (msg != null) {
            if (targetSdkVersion >= 26) {
                throw new SecurityException(msg);
            } else if (!msg.startsWith("Failed to find provider")) {
                Log.w(TAG, "Ignoring content changes for " + uri + " from " + uid + ": " + msg);
                return;
            }
        }
        synchronized (this.mRootNode) {
            this.mRootNode.addObserverLocked(uri, observer, notifyForDescendants, this.mRootNode, uid, pid, ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).handleUserForClone(uri.getAuthority(), userHandle));
        }
    }

    public void registerContentObserver(Uri uri, boolean notifyForDescendants, IContentObserver observer) {
        registerContentObserver(uri, notifyForDescendants, observer, UserHandle.getCallingUserId(), 10000);
    }

    public void unregisterContentObserver(IContentObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("You must pass a valid observer");
        }
        synchronized (this.mRootNode) {
            this.mRootNode.removeObserverLocked(observer);
        }
    }

    public void notifyChange(Uri uri, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int userHandle, int targetSdkVersion) {
        if (uri == null) {
            throw new NullPointerException("Uri must not be null");
        }
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        int callingUserHandle = UserHandle.getCallingUserId();
        userHandle = handleIncomingUser(uri, pid, uid, 2, true, userHandle);
        String msg = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).checkContentProviderAccess(uri.getAuthority(), userHandle);
        if (msg != null) {
            if (targetSdkVersion >= 26) {
                throw new SecurityException(msg);
            }
            if (!msg.startsWith("Failed to find provider")) {
                Log.w(TAG, "Ignoring notify for " + uri + " from " + uid + ": " + msg);
                return;
            }
        }
        long identityToken = clearCallingIdentity();
        try {
            ArrayList<ObserverCall> calls = new ArrayList();
            synchronized (this.mRootNode) {
                this.mRootNode.collectObserversLocked(uri, 0, observer, observerWantsSelfNotifications, flags, userHandle, calls);
            }
            int numCalls = calls.size();
            for (int i = 0; i < numCalls; i++) {
                ObserverCall oc = (ObserverCall) calls.get(i);
                try {
                    oc.mObserver.onChange(oc.mSelfChange, uri, userHandle);
                } catch (RemoteException e) {
                    synchronized (this.mRootNode) {
                        Log.w(TAG, "Found dead observer, removing");
                        IBinder binder = oc.mObserver.asBinder();
                        ArrayList<ObserverEntry> list = oc.mNode.mObservers;
                        int numList = list.size();
                        int j = 0;
                        while (j < numList) {
                            if (((ObserverEntry) list.get(j)).observer.asBinder() == binder) {
                                list.remove(j);
                                j--;
                                numList--;
                            }
                            j++;
                        }
                    }
                }
            }
            if ((flags & 1) != 0) {
                SyncManager syncManager = getSyncManager();
                if (syncManager != null) {
                    syncManager.scheduleLocalSync(null, callingUserHandle, uid, uri.getAuthority());
                }
            }
            synchronized (this.mCache) {
                invalidateCacheLocked(userHandle, getProviderPackageName(uri), uri);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    private int checkUriPermission(Uri uri, int pid, int uid, int modeFlags, int userHandle) {
        try {
            return ActivityManager.getService().checkUriPermission(uri, pid, uid, modeFlags, userHandle, null);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public void notifyChange(Uri uri, IContentObserver observer, boolean observerWantsSelfNotifications, boolean syncToNetwork) {
        notifyChange(uri, observer, observerWantsSelfNotifications, syncToNetwork ? 1 : 0, UserHandle.getCallingUserId(), 10000);
    }

    public void requestSync(Account account, String authority, Bundle extras) {
        Bundle.setDefusable(extras, true);
        ContentResolver.validateSyncExtrasBundle(extras);
        int userId = UserHandle.getCallingUserId();
        int uId = Binder.getCallingUid();
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                syncManager.scheduleSync(account, userId, uId, authority, extras, -2);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    public void sync(SyncRequest request) {
        syncAsUser(request, UserHandle.getCallingUserId());
    }

    private long clampPeriod(long period) {
        long minPeriod = JobInfo.getMinPeriodMillis() / 1000;
        if (period >= minPeriod) {
            return period;
        }
        Slog.w(TAG, "Requested poll frequency of " + period + " seconds being rounded up to " + minPeriod + "s.");
        return minPeriod;
    }

    public void syncAsUser(SyncRequest request, int userId) {
        enforceCrossUserPermission(userId, "no permission to request sync as user: " + userId);
        int callerUid = Binder.getCallingUid();
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                Bundle extras = request.getBundle();
                long flextime = request.getSyncFlexTime();
                long runAtTime = request.getSyncRunTime();
                if (request.isPeriodic()) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
                    getSyncManager().updateOrAddPeriodicSync(new EndPoint(request.getAccount(), request.getProvider(), userId), clampPeriod(runAtTime), flextime, extras);
                } else {
                    syncManager.scheduleSync(request.getAccount(), userId, callerUid, request.getProvider(), extras, -2);
                }
                restoreCallingIdentity(identityToken);
            }
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void cancelSync(Account account, String authority, ComponentName cname) {
        cancelSyncAsUser(account, authority, cname, UserHandle.getCallingUserId());
    }

    public void cancelSyncAsUser(Account account, String authority, ComponentName cname, int userId) {
        if (authority == null || authority.length() != 0) {
            enforceCrossUserPermission(userId, "no permission to modify the sync settings for user " + userId);
            long identityToken = clearCallingIdentity();
            if (cname != null) {
                Slog.e(TAG, "cname not null.");
                return;
            }
            try {
                SyncManager syncManager = getSyncManager();
                if (syncManager != null) {
                    EndPoint info = new EndPoint(account, authority, userId);
                    syncManager.clearScheduledSyncOperations(info);
                    syncManager.cancelActiveSync(info, null);
                }
                restoreCallingIdentity(identityToken);
            } catch (Throwable th) {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new IllegalArgumentException("Authority must be non-empty");
        }
    }

    public void cancelRequest(SyncRequest request) {
        SyncManager syncManager = getSyncManager();
        if (syncManager != null) {
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                Bundle extras = new Bundle(request.getBundle());
                EndPoint info = new EndPoint(request.getAccount(), request.getProvider(), userId);
                if (request.isPeriodic()) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
                    getSyncManager().removePeriodicSync(info, extras);
                }
                syncManager.cancelScheduledSyncOperation(info, extras);
                syncManager.cancelActiveSync(info, extras);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public SyncAdapterType[] getSyncAdapterTypes() {
        return getSyncAdapterTypesAsUser(UserHandle.getCallingUserId());
    }

    public SyncAdapterType[] getSyncAdapterTypesAsUser(int userId) {
        enforceCrossUserPermission(userId, "no permission to read sync settings for user " + userId);
        long identityToken = clearCallingIdentity();
        try {
            SyncAdapterType[] syncAdapterTypes = getSyncManager().getSyncAdapterTypes(userId);
            return syncAdapterTypes;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) {
        enforceCrossUserPermission(userId, "no permission to read sync settings for user " + userId);
        long identityToken = clearCallingIdentity();
        try {
            String[] syncAdapterPackagesForAuthorityAsUser = getSyncManager().getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
            return syncAdapterPackagesForAuthorityAsUser;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public boolean getSyncAutomatically(Account account, String providerName) {
        return getSyncAutomaticallyAsUser(account, providerName, UserHandle.getCallingUserId());
    }

    public boolean getSyncAutomaticallyAsUser(Account account, String providerName, int userId) {
        enforceCrossUserPermission(userId, "no permission to read the sync settings for user " + userId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_SETTINGS", "no permission to read the sync settings");
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                boolean syncAutomatically = syncManager.getSyncStorageEngine().getSyncAutomatically(account, userId, providerName);
                return syncAutomatically;
            }
            restoreCallingIdentity(identityToken);
            return false;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void setSyncAutomatically(Account account, String providerName, boolean sync) {
        setSyncAutomaticallyAsUser(account, providerName, sync, UserHandle.getCallingUserId());
    }

    public void setSyncAutomaticallyAsUser(Account account, String providerName, boolean sync, int userId) {
        if (TextUtils.isEmpty(providerName)) {
            throw new IllegalArgumentException("Authority must be non-empty");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
        enforceCrossUserPermission(userId, "no permission to modify the sync settings for user " + userId);
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                syncManager.getSyncStorageEngine().setSyncAutomatically(account, userId, providerName, sync);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    public void addPeriodicSync(Account account, String authority, Bundle extras, long pollFrequency) {
        Bundle.setDefusable(extras, true);
        if (account == null) {
            throw new IllegalArgumentException("Account must not be null");
        } else if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("Authority must not be empty.");
        } else {
            this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
            int userId = UserHandle.getCallingUserId();
            pollFrequency = clampPeriod(pollFrequency);
            long defaultFlex = SyncStorageEngine.calculateDefaultFlexTime(pollFrequency);
            long identityToken = clearCallingIdentity();
            try {
                getSyncManager().updateOrAddPeriodicSync(new EndPoint(account, authority, userId), pollFrequency, defaultFlex, extras);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void removePeriodicSync(Account account, String authority, Bundle extras) {
        Bundle.setDefusable(extras, true);
        if (account == null) {
            throw new IllegalArgumentException("Account must not be null");
        } else if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("Authority must not be empty");
        } else {
            this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                getSyncManager().removePeriodicSync(new EndPoint(account, authority, userId), extras);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public List<PeriodicSync> getPeriodicSyncs(Account account, String providerName, ComponentName cname) {
        if (account == null) {
            throw new IllegalArgumentException("Account must not be null");
        } else if (TextUtils.isEmpty(providerName)) {
            throw new IllegalArgumentException("Authority must not be empty");
        } else {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_SETTINGS", "no permission to read the sync settings");
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                List<PeriodicSync> periodicSyncs = getSyncManager().getPeriodicSyncs(new EndPoint(account, providerName, userId));
                return periodicSyncs;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public int getIsSyncable(Account account, String providerName) {
        return getIsSyncableAsUser(account, providerName, UserHandle.getCallingUserId());
    }

    public int getIsSyncableAsUser(Account account, String providerName, int userId) {
        enforceCrossUserPermission(userId, "no permission to read the sync settings for user " + userId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_SETTINGS", "no permission to read the sync settings");
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                int computeSyncable = syncManager.computeSyncable(account, userId, providerName, false);
                return computeSyncable;
            }
            restoreCallingIdentity(identityToken);
            return -1;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void setIsSyncable(Account account, String providerName, int syncable) {
        if (TextUtils.isEmpty(providerName)) {
            throw new IllegalArgumentException("Authority must not be empty");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
        syncable = normalizeSyncable(syncable);
        int userId = UserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                syncManager.getSyncStorageEngine().setIsSyncable(account, userId, providerName, syncable);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    public boolean getMasterSyncAutomatically() {
        return getMasterSyncAutomaticallyAsUser(UserHandle.getCallingUserId());
    }

    public boolean getMasterSyncAutomaticallyAsUser(int userId) {
        enforceCrossUserPermission(userId, "no permission to read the sync settings for user " + userId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_SETTINGS", "no permission to read the sync settings");
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                boolean masterSyncAutomatically = syncManager.getSyncStorageEngine().getMasterSyncAutomatically(userId);
                return masterSyncAutomatically;
            }
            restoreCallingIdentity(identityToken);
            return false;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void setMasterSyncAutomatically(boolean flag) {
        setMasterSyncAutomaticallyAsUser(flag, UserHandle.getCallingUserId());
    }

    public void setMasterSyncAutomaticallyAsUser(boolean flag, int userId) {
        enforceCrossUserPermission(userId, "no permission to set the sync status for user " + userId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                syncManager.getSyncStorageEngine().setMasterSyncAutomatically(flag, userId);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    public boolean isSyncActive(Account account, String authority, ComponentName cname) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_STATS", "no permission to read the sync stats");
        int userId = UserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager == null) {
                return false;
            }
            boolean isSyncActive = syncManager.getSyncStorageEngine().isSyncActive(new EndPoint(account, authority, userId));
            restoreCallingIdentity(identityToken);
            return isSyncActive;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public List<SyncInfo> getCurrentSyncs() {
        return getCurrentSyncsAsUser(UserHandle.getCallingUserId());
    }

    public List<SyncInfo> getCurrentSyncsAsUser(int userId) {
        enforceCrossUserPermission(userId, "no permission to read the sync settings for user " + userId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_STATS", "no permission to read the sync stats");
        boolean canAccessAccounts = this.mContext.checkCallingOrSelfPermission("android.permission.GET_ACCOUNTS") == 0;
        long identityToken = clearCallingIdentity();
        try {
            List<SyncInfo> currentSyncsCopy = getSyncManager().getSyncStorageEngine().getCurrentSyncsCopy(userId, canAccessAccounts);
            return currentSyncsCopy;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public SyncStatusInfo getSyncStatus(Account account, String authority, ComponentName cname) {
        return getSyncStatusAsUser(account, authority, cname, UserHandle.getCallingUserId());
    }

    public SyncStatusInfo getSyncStatusAsUser(Account account, String authority, ComponentName cname, int userId) {
        if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("Authority must not be empty");
        }
        enforceCrossUserPermission(userId, "no permission to read the sync stats for user " + userId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_STATS", "no permission to read the sync stats");
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager == null) {
                return null;
            }
            if (account == null || authority == null) {
                throw new IllegalArgumentException("Must call sync status with valid authority");
            }
            SyncStatusInfo statusByAuthority = syncManager.getSyncStorageEngine().getStatusByAuthority(new EndPoint(account, authority, userId));
            restoreCallingIdentity(identityToken);
            return statusByAuthority;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public boolean isSyncPending(Account account, String authority, ComponentName cname) {
        return isSyncPendingAsUser(account, authority, cname, UserHandle.getCallingUserId());
    }

    public boolean isSyncPendingAsUser(Account account, String authority, ComponentName cname, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_STATS", "no permission to read the sync stats");
        enforceCrossUserPermission(userId, "no permission to retrieve the sync settings for user " + userId);
        long identityToken = clearCallingIdentity();
        SyncManager syncManager = getSyncManager();
        if (syncManager == null) {
            return false;
        }
        if (account == null || authority == null) {
            throw new IllegalArgumentException("Invalid authority specified");
        }
        try {
            boolean isSyncPending = syncManager.getSyncStorageEngine().isSyncPending(new EndPoint(account, authority, userId));
            return isSyncPending;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void addStatusChangeListener(int mask, ISyncStatusObserver callback) {
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (!(syncManager == null || callback == null)) {
                syncManager.getSyncStorageEngine().addStatusChangeListener(mask, callback);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    public void removeStatusChangeListener(ISyncStatusObserver callback) {
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (!(syncManager == null || callback == null)) {
                syncManager.getSyncStorageEngine().removeStatusChangeListener(callback);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    private String getProviderPackageName(Uri uri) {
        ProviderInfo pi = this.mContext.getPackageManager().resolveContentProvider(uri.getAuthority(), 0);
        if (pi != null) {
            return pi.packageName;
        }
        return null;
    }

    private ArrayMap<Pair<String, Uri>, Bundle> findOrCreateCacheLocked(int userId, String providerPackageName) {
        ArrayMap<String, ArrayMap<Pair<String, Uri>, Bundle>> userCache = (ArrayMap) this.mCache.get(userId);
        if (userCache == null) {
            userCache = new ArrayMap();
            this.mCache.put(userId, userCache);
        }
        ArrayMap<Pair<String, Uri>, Bundle> packageCache = (ArrayMap) userCache.get(providerPackageName);
        if (packageCache != null) {
            return packageCache;
        }
        packageCache = new ArrayMap();
        userCache.put(providerPackageName, packageCache);
        return packageCache;
    }

    private void invalidateCacheLocked(int userId, String providerPackageName, Uri uri) {
        ArrayMap<String, ArrayMap<Pair<String, Uri>, Bundle>> userCache = (ArrayMap) this.mCache.get(userId);
        if (userCache != null) {
            ArrayMap<Pair<String, Uri>, Bundle> packageCache = (ArrayMap) userCache.get(providerPackageName);
            if (packageCache != null) {
                if (uri != null) {
                    int i = 0;
                    while (i < packageCache.size()) {
                        Pair<String, Uri> key = (Pair) packageCache.keyAt(i);
                        if (key.second == null || !((Uri) key.second).toString().startsWith(uri.toString())) {
                            i++;
                        } else {
                            packageCache.removeAt(i);
                        }
                    }
                } else {
                    packageCache.clear();
                }
            }
        }
    }

    public void putCache(String packageName, Uri key, Bundle value, int userId) {
        Bundle.setDefusable(value, true);
        enforceCrossUserPermission(userId, TAG);
        this.mContext.enforceCallingOrSelfPermission("android.permission.CACHE_CONTENT", TAG);
        ((AppOpsManager) this.mContext.getSystemService(AppOpsManager.class)).checkPackage(Binder.getCallingUid(), packageName);
        String providerPackageName = getProviderPackageName(key);
        Pair<String, Uri> fullKey = Pair.create(packageName, key);
        synchronized (this.mCache) {
            ArrayMap<Pair<String, Uri>, Bundle> cache = findOrCreateCacheLocked(userId, providerPackageName);
            if (value != null) {
                cache.put(fullKey, value);
            } else {
                cache.remove(fullKey);
            }
        }
    }

    public Bundle getCache(String packageName, Uri key, int userId) {
        Bundle bundle;
        enforceCrossUserPermission(userId, TAG);
        this.mContext.enforceCallingOrSelfPermission("android.permission.CACHE_CONTENT", TAG);
        ((AppOpsManager) this.mContext.getSystemService(AppOpsManager.class)).checkPackage(Binder.getCallingUid(), packageName);
        String providerPackageName = getProviderPackageName(key);
        Pair<String, Uri> fullKey = Pair.create(packageName, key);
        synchronized (this.mCache) {
            bundle = (Bundle) findOrCreateCacheLocked(userId, providerPackageName).get(fullKey);
        }
        return bundle;
    }

    private int handleIncomingUser(Uri uri, int pid, int uid, int modeFlags, boolean allowNonFull, int userId) {
        if (userId == -2) {
            userId = ActivityManager.getCurrentUser();
        }
        if (userId == -1) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", TAG);
        } else if (userId < 0) {
            throw new IllegalArgumentException("Invalid user: " + userId);
        } else if (!(userId == UserHandle.getCallingUserId() || checkUriPermission(uri, pid, uid, modeFlags, userId) == 0)) {
            boolean allow = false;
            if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
                allow = true;
            } else if (allowNonFull && this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS") == 0) {
                allow = true;
            }
            if (!allow) {
                String permissions;
                if (allowNonFull) {
                    permissions = "android.permission.INTERACT_ACROSS_USERS_FULL or android.permission.INTERACT_ACROSS_USERS";
                } else {
                    permissions = "android.permission.INTERACT_ACROSS_USERS_FULL";
                }
                throw new SecurityException("ContentServiceNeither user " + uid + " nor current process has " + permissions);
            }
        }
        return userId;
    }

    private void enforceCrossUserPermission(int userHandle, String message) {
        if (UserHandle.getCallingUserId() != userHandle) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
        }
    }

    private static int normalizeSyncable(int syncable) {
        if (syncable > 0) {
            return 1;
        }
        if (syncable == 0) {
            return 0;
        }
        return -2;
    }
}
