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
import android.content.IContentService;
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
import android.content.pm.ProviderInfo;
import android.database.IContentObserver;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FactoryTest;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
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
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.content.SyncStorageEngine;
import com.android.server.slice.SliceClientPermissions;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ContentService extends IContentService.Stub {
    private static final long CONTENT_OBSERVER_THRESHOLD = 20000;
    static final boolean DEBUG = false;
    static final String TAG = "ContentService";
    /* access modifiers changed from: private */
    public static HwSysResource mContentObserverResource = null;
    /* access modifiers changed from: private */
    public static int mObserverNum;
    /* access modifiers changed from: private */
    public static PackageManager mPackageManager = null;
    /* access modifiers changed from: private */
    @GuardedBy("mCache")
    public final SparseArray<ArrayMap<String, ArrayMap<Pair<String, Uri>, Bundle>>> mCache = new SparseArray<>();
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
    private final ObserverNode mRootNode = new ObserverNode(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    private SyncManager mSyncManager = null;
    private final Object mSyncManagerLock = new Object();

    public static class Lifecycle extends SystemService {
        private ContentService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX WARNING: type inference failed for: r2v1, types: [com.android.server.content.ContentService, android.os.IBinder] */
        public void onStart() {
            boolean factoryTest = true;
            if (FactoryTest.getMode() != 1) {
                factoryTest = false;
            }
            this.mService = new ContentService(getContext(), factoryTest);
            publishBinderService("content", this.mService);
        }

        public void onBootPhase(int phase) {
            this.mService.onBootPhase(phase);
        }

        public void onStartUser(int userHandle) {
            this.mService.onStartUser(userHandle);
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }

        public void onStopUser(int userHandle) {
            this.mService.onStopUser(userHandle);
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
        private ArrayList<ObserverNode> mChildren = new ArrayList<>();
        private String mName;
        /* access modifiers changed from: private */
        public ArrayList<ObserverEntry> mObservers = new ArrayList<>();

        private class ObserverEntry implements IBinder.DeathRecipient {
            public final boolean notifyForDescendants;
            public final IContentObserver observer;
            private final Object observersLock;
            public final String packageName;
            public final int pid;
            public final int uid;
            /* access modifiers changed from: private */
            public final int userHandle;

            public ObserverEntry(IContentObserver o, boolean n, Object observersLock2, int _uid, int _pid, String _packageName, int _userHandle) {
                this.observersLock = observersLock2;
                this.observer = o;
                this.uid = _uid;
                this.pid = _pid;
                this.packageName = _packageName;
                this.userHandle = _userHandle;
                this.notifyForDescendants = n;
                try {
                    this.observer.asBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    binderDied();
                }
            }

            public ObserverEntry(IContentObserver o, boolean n, Object observersLock2, int _uid, int _pid, int _userHandle) {
                this.observersLock = observersLock2;
                this.observer = o;
                this.uid = _uid;
                this.pid = _pid;
                this.packageName = null;
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
                pw.println(Integer.toHexString(System.identityHashCode(this.observer != null ? this.observer.asBinder() : null)));
            }
        }

        public ObserverNode(String name) {
            this.mName = name;
        }

        public void dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, String name, String prefix, int[] counts, SparseIntArray pidCounts) {
            String str = name;
            String innerName = null;
            if (this.mObservers.size() > 0) {
                if (BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(str)) {
                    innerName = this.mName;
                } else {
                    innerName = str + SliceClientPermissions.SliceAuthority.DELIMITER + this.mName;
                }
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= this.mObservers.size()) {
                        break;
                    }
                    counts[1] = counts[1] + 1;
                    this.mObservers.get(i2).dumpLocked(fd, pw, args, innerName, prefix, pidCounts);
                    i = i2 + 1;
                }
            }
            if (this.mChildren.size() > 0) {
                if (innerName == null) {
                    if (BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(str)) {
                        innerName = this.mName;
                    } else {
                        innerName = str + SliceClientPermissions.SliceAuthority.DELIMITER + this.mName;
                    }
                }
                String innerName2 = innerName;
                int i3 = 0;
                while (true) {
                    int i4 = i3;
                    if (i4 < this.mChildren.size()) {
                        counts[0] = counts[0] + 1;
                        this.mChildren.get(i4).dumpLocked(fd, pw, args, innerName2, prefix, counts, pidCounts);
                        i3 = i4 + 1;
                    } else {
                        return;
                    }
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
            return uri.getPathSegments().get(index - 1);
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
            Uri uri2 = uri;
            int i = uid;
            int i2 = pid;
            if (index == countUriSegments(uri)) {
                String packageName = null;
                if (ContentService.mContentObserverResource == null) {
                    HwSysResource unused = ContentService.mContentObserverResource = HwFrameworkFactory.getHwResource(29);
                }
                if (!(ContentService.mContentObserverResource == null || ContentService.mPackageManager == null)) {
                    packageName = ContentService.mPackageManager.getNameForUid(i);
                    if (packageName != null) {
                        ContentService.mContentObserverResource.acquire(i, packageName, -1);
                    }
                }
                String packageName2 = packageName;
                ArrayList<ObserverEntry> arrayList = this.mObservers;
                ObserverEntry observerEntry = r0;
                ObserverEntry observerEntry2 = new ObserverEntry(observer, notifyForDescendants, observersLock, i, i2, packageName2, userHandle);
                arrayList.add(observerEntry);
                ContentService.access$508();
                if (((long) ContentService.mObserverNum) > ContentService.CONTENT_OBSERVER_THRESHOLD) {
                    if (i2 == Process.myPid()) {
                        RuntimeException e = new RuntimeException();
                        e.fillInStackTrace();
                        e.printStackTrace();
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("add observer mName:");
                    sb.append(this.mName);
                    sb.append(", pid:");
                    sb.append(i2);
                    sb.append(", uid:");
                    sb.append(i);
                    sb.append(", user:");
                    sb.append(userHandle);
                    sb.append(", packageName:");
                    sb.append(packageName2);
                    sb.append(", uri:");
                    sb.append(uri2);
                    sb.append(", target:");
                    sb.append(Integer.toHexString(System.identityHashCode(observer != null ? observer.asBinder() : null)));
                    sb.append(", mObserverNum: ");
                    sb.append(ContentService.mObserverNum);
                    Slog.d(ContentService.TAG, sb.toString());
                } else {
                    int i3 = userHandle;
                }
                return;
            }
            int i4 = userHandle;
            int i5 = index;
            String segment = getUriSegment(uri, index);
            if (segment != null) {
                int N = this.mChildren.size();
                int i6 = 0;
                while (true) {
                    int i7 = i6;
                    if (i7 < N) {
                        ObserverNode node = this.mChildren.get(i7);
                        if (node.mName.equals(segment)) {
                            ObserverNode observerNode = node;
                            int i8 = i7;
                            int i9 = N;
                            int N2 = i2;
                            String str = segment;
                            node.addObserverLocked(uri2, i5 + 1, observer, notifyForDescendants, observersLock, i, N2, i4);
                            return;
                        }
                        int i10 = N;
                        String str2 = segment;
                        i6 = i7 + 1;
                        i2 = pid;
                    } else {
                        ObserverNode node2 = new ObserverNode(segment);
                        this.mChildren.add(node2);
                        ObserverNode observerNode2 = node2;
                        node2.addObserverLocked(uri2, i5 + 1, observer, notifyForDescendants, observersLock, i, pid, i4);
                        return;
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid Uri (" + uri2 + ") used for observer");
            }
        }

        public boolean removeObserverLocked(IContentObserver observer) {
            int size = this.mChildren.size();
            int i = 0;
            while (i < size) {
                if (this.mChildren.get(i).removeObserverLocked(observer)) {
                    this.mChildren.remove(i);
                    i--;
                    size--;
                }
                i++;
            }
            IBinder observerBinder = observer.asBinder();
            int size2 = this.mObservers.size();
            int i2 = 0;
            while (true) {
                if (i2 >= size2) {
                    break;
                }
                ObserverEntry entry = this.mObservers.get(i2);
                if (entry.observer.asBinder() == observerBinder) {
                    this.mObservers.remove(i2);
                    ContentService.access$510();
                    if (((long) ContentService.mObserverNum) > ContentService.CONTENT_OBSERVER_THRESHOLD) {
                        Slog.d(ContentService.TAG, "remove observer mName:" + this.mName + ", pid:" + entry.pid + ", uid:" + entry.uid + ", user:" + entry.userHandle + ", packageName:" + entry.packageName + ", target:" + Integer.toHexString(System.identityHashCode(observerBinder)) + ", mObserverNum:" + ContentService.mObserverNum);
                    }
                    if (!(ContentService.mContentObserverResource == null || entry.packageName == null)) {
                        ContentService.mContentObserverResource.release(entry.uid, entry.packageName, -1);
                    }
                    observerBinder.unlinkToDeath(entry, 0);
                } else {
                    i2++;
                }
            }
            return this.mChildren.size() == 0 && this.mObservers.size() == 0;
        }

        private void collectMyObserversLocked(boolean leaf, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int targetUserHandle, ArrayList<ObserverCall> calls) {
            int N = this.mObservers.size();
            IBinder observerBinder = observer == null ? null : observer.asBinder();
            for (int i = 0; i < N; i++) {
                ObserverEntry entry = this.mObservers.get(i);
                boolean selfChange = entry.observer.asBinder() == observerBinder;
                if ((!selfChange || observerWantsSelfNotifications) && !HwServiceFactory.getHwNLPManager().shouldSkipGoogleNlp(entry.pid) && (targetUserHandle == -1 || entry.userHandle == -1 || targetUserHandle == entry.userHandle)) {
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
            int i = index;
            String segment = null;
            int segmentCount = countUriSegments(uri);
            if (i >= segmentCount) {
                collectMyObserversLocked(true, observer, observerWantsSelfNotifications, flags, targetUserHandle, calls);
            } else if (i < segmentCount) {
                segment = getUriSegment(uri, index);
                collectMyObserversLocked(false, observer, observerWantsSelfNotifications, flags, targetUserHandle, calls);
            }
            int N = this.mChildren.size();
            for (int i2 = 0; i2 < N; i2++) {
                ObserverNode node = this.mChildren.get(i2);
                if (segment == null || node.mName.equals(segment)) {
                    node.collectObserversLocked(uri, i + 1, observer, observerWantsSelfNotifications, flags, targetUserHandle, calls);
                    if (segment != null) {
                        return;
                    }
                }
            }
        }
    }

    static /* synthetic */ int access$508() {
        int i = mObserverNum;
        mObserverNum = i + 1;
        return i;
    }

    static /* synthetic */ int access$510() {
        int i = mObserverNum;
        mObserverNum = i - 1;
        return i;
    }

    private SyncManager getSyncManager() {
        SyncManager syncManager;
        synchronized (this.mSyncManagerLock) {
            try {
                if (this.mSyncManager == null) {
                    SyncManager createHwSyncManager = HwServiceFactory.createHwSyncManager(this.mContext, this.mFactoryTest);
                    this.mSyncManager = createHwSyncManager;
                    this.mSyncManager = createHwSyncManager;
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Can't create SyncManager", e);
            }
            syncManager = this.mSyncManager;
        }
        return syncManager;
    }

    /* access modifiers changed from: package-private */
    public void onStartUser(int userHandle) {
        if (this.mSyncManager != null) {
            this.mSyncManager.onStartUser(userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public void onUnlockUser(int userHandle) {
        if (this.mSyncManager != null) {
            this.mSyncManager.onUnlockUser(userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public void onStopUser(int userHandle) {
        if (this.mSyncManager != null) {
            this.mSyncManager.onStopUser(userHandle);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void dump(FileDescriptor fd, PrintWriter pw_, String[] args) {
        FileDescriptor fileDescriptor;
        ObserverNode observerNode;
        final SparseIntArray pidCounts;
        PrintWriter printWriter = pw_;
        synchronized (this) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, printWriter)) {
                IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
                String[] strArr = args;
                boolean dumpAll = ArrayUtils.contains(strArr, "-a");
                long identityToken = clearCallingIdentity();
                try {
                    if (this.mSyncManager == null) {
                        pw.println("SyncManager not available yet");
                        fileDescriptor = fd;
                    } else {
                        fileDescriptor = fd;
                        this.mSyncManager.dump(fileDescriptor, pw, dumpAll);
                    }
                    pw.println();
                    pw.println("Observer tree:");
                    ObserverNode observerNode2 = this.mRootNode;
                    synchronized (observerNode2) {
                        try {
                            int[] counts = new int[2];
                            SparseIntArray pidCounts2 = new SparseIntArray();
                            observerNode = observerNode2;
                            try {
                                this.mRootNode.dumpLocked(fileDescriptor, pw, strArr, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, "  ", counts, pidCounts2);
                                pw.println();
                                ArrayList<Integer> sorted = new ArrayList<>();
                                int i = 0;
                                int i2 = 0;
                                while (true) {
                                    pidCounts = pidCounts2;
                                    if (i2 >= pidCounts.size()) {
                                        break;
                                    }
                                    sorted.add(Integer.valueOf(pidCounts.keyAt(i2)));
                                    i2++;
                                    pidCounts2 = pidCounts;
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
                                for (int i3 = 0; i3 < sorted.size(); i3++) {
                                    int pid = sorted.get(i3).intValue();
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
                                synchronized (this.mCache) {
                                    pw.println();
                                    pw.println("Cached content:");
                                    pw.increaseIndent();
                                    while (true) {
                                        int i4 = i;
                                        if (i4 < this.mCache.size()) {
                                            pw.println("User " + this.mCache.keyAt(i4) + ":");
                                            pw.increaseIndent();
                                            pw.println(this.mCache.valueAt(i4));
                                            pw.decreaseIndent();
                                            i = i4 + 1;
                                        } else {
                                            pw.decreaseIndent();
                                        }
                                    }
                                }
                                restoreCallingIdentity(identityToken);
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            observerNode = observerNode2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    restoreCallingIdentity(identityToken);
                    throw th3;
                }
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return ContentService.super.onTransact(code, data, reply, flags);
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
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setSyncAdapterPackagesprovider(new PackageManagerInternal.SyncAdapterPackagesProvider() {
            public String[] getPackages(String authority, int userId) {
                return ContentService.this.getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
            }
        });
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        packageFilter.addDataScheme("package");
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

    /* access modifiers changed from: package-private */
    public void onBootPhase(int phase) {
        if (phase == 550) {
            getSyncManager();
        }
        if (this.mSyncManager != null) {
            this.mSyncManager.onBootPhase(phase);
        }
    }

    public void registerContentObserver(Uri uri, boolean notifyForDescendants, IContentObserver observer, int userHandle, int targetSdkVersion) {
        Uri uri2 = uri;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONTENT_REGISTERCONTENTOBSERVER, new Object[]{uri2});
        if (observer == null || uri2 == null) {
            int i = targetSdkVersion;
            throw new IllegalArgumentException("You must pass a valid uri and observer");
        }
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        int userHandle2 = handleIncomingUser(uri2, pid, uid, 1, true, userHandle);
        String msg = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).checkContentProviderAccess(uri.getAuthority(), userHandle2);
        if (msg == null) {
            int i2 = targetSdkVersion;
        } else if (targetSdkVersion >= 26) {
            throw new SecurityException(msg);
        } else if (!msg.startsWith("Failed to find provider")) {
            Log.w(TAG, "Ignoring content changes for " + uri2 + " from " + uid + ": " + msg);
            return;
        }
        synchronized (this.mRootNode) {
            try {
                int i3 = uid;
                this.mRootNode.addObserverLocked(uri2, observer, notifyForDescendants, this.mRootNode, uid, pid, ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).handleUserForClone(uri.getAuthority(), userHandle2));
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public void registerContentObserver(Uri uri, boolean notifyForDescendants, IContentObserver observer) {
        registerContentObserver(uri, notifyForDescendants, observer, UserHandle.getCallingUserId(), 10000);
    }

    public void unregisterContentObserver(IContentObserver observer) {
        if (observer != null) {
            synchronized (this.mRootNode) {
                this.mRootNode.removeObserverLocked(observer);
            }
            return;
        }
        throw new IllegalArgumentException("You must pass a valid observer");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0181, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0182, code lost:
        restoreCallingIdentity(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0186, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x018a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x018b, code lost:
        r9 = r3;
        r11 = r6;
        r12 = r19;
        r13 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x01a2, code lost:
        r9 = r9;
        r9 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x01a3, code lost:
        r0 = th;
        r9 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0094, code lost:
        r9 = r2.size();
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0096, code lost:
        r10 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0097, code lost:
        if (r10 >= r9) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009f, code lost:
        r11 = r2.get(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        r11.mObserver.onChange(r11.mSelfChange, r28, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a9, code lost:
        r21 = r2;
        r23 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00b0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00b4, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b5, code lost:
        r13 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b7, code lost:
        r9 = r3;
        r11 = r6;
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00bd, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00be, code lost:
        r13 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00c0, code lost:
        r12 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c3, code lost:
        monitor-enter(r8.mRootNode);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        android.util.Log.w(TAG, "Found dead observer, removing");
        r0 = r11.mObserver.asBinder();
        r15 = com.android.server.content.ContentService.ObserverNode.access$300(r11.mNode);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00db, code lost:
        r1 = r15.size();
        r16 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00df, code lost:
        r21 = r2;
        r2 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e5, code lost:
        if (r2 < r1) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ed, code lost:
        r23 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00f9, code lost:
        if (r15.get(r2).observer.asBinder() == r0) goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00fb, code lost:
        r15.remove(r2);
        r1 = r1 - 1;
        r2 = r2 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0104, code lost:
        r16 = r2 + 1;
        r2 = r21;
        r3 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x010b, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x010c, code lost:
        r23 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x010f, code lost:
        r23 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x011a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x011b, code lost:
        r21 = r2;
        r23 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0121, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0122, code lost:
        r11 = r6;
        r12 = r19;
        r9 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0129, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x012b, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x012d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x012e, code lost:
        r13 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0130, code lost:
        r9 = r3;
        r11 = r6;
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0136, code lost:
        r21 = r2;
        r23 = r3;
        r13 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x013e, code lost:
        if ((r31 & 1) == 0) goto L_0x016e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0140, code lost:
        r9 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:?, code lost:
        r0 = getSyncManager();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0144, code lost:
        if (r0 == null) goto L_0x016e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x014b, code lost:
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x014d, code lost:
        r9 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0151, code lost:
        r15 = r21;
        r25 = r9;
        r9 = r23;
        r11 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x015e, code lost:
        r9 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        r0.scheduleLocalSync(null, r18, r12, r28.getAuthority(), getSyncExemptionForCaller(r12));
        r9 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0162, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0163, code lost:
        r11 = r6;
        r9 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0167, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0168, code lost:
        r11 = r6;
        r12 = r19;
        r9 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x016e, code lost:
        r11 = r6;
        r25 = r9;
        r12 = r19;
        r15 = r21;
        r9 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0177, code lost:
        r1 = r8.mCache;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0179, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:?, code lost:
        invalidateCacheLocked(r7, getProviderPackageName(r28), r13);
     */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00c4  */
    public void notifyChange(Uri uri, IContentObserver observer, boolean observerWantsSelfNotifications, int flags, int userHandle, int targetSdkVersion) {
        long identityToken;
        ArrayList<ObserverCall> calls;
        long identityToken2;
        int i;
        long identityToken3;
        ArrayList<ObserverCall> calls2;
        Uri uri2 = uri;
        if (uri2 != null) {
            int uid = Binder.getCallingUid();
            int pid = Binder.getCallingPid();
            int callingUserHandle = UserHandle.getCallingUserId();
            int userHandle2 = handleIncomingUser(uri2, pid, uid, 2, true, userHandle);
            String msg = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).checkContentProviderAccess(uri.getAuthority(), userHandle2);
            if (msg == null) {
                int i2 = targetSdkVersion;
            } else if (targetSdkVersion >= 26) {
                throw new SecurityException(msg);
            } else if (!msg.startsWith("Failed to find provider")) {
                Log.w(TAG, "Ignoring notify for " + uri2 + " from " + uid + ": " + msg);
                return;
            }
            identityToken = clearCallingIdentity();
            try {
                calls = new ArrayList<>();
                synchronized (this.mRootNode) {
                    try {
                        int uid2 = uid;
                        try {
                            this.mRootNode.collectObserversLocked(uri2, 0, observer, observerWantsSelfNotifications, flags, userHandle2, calls);
                        } catch (Throwable th) {
                            th = th;
                            ArrayList<ObserverCall> arrayList = calls;
                            identityToken2 = identityToken;
                            String str = msg;
                            int i3 = uid2;
                            Uri uri3 = uri;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        identityToken2 = identityToken;
                        String str2 = msg;
                        int i4 = uid;
                        Uri uri4 = uri2;
                        ArrayList<ObserverCall> arrayList2 = calls;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                long identityToken4 = identityToken;
                String str3 = msg;
                int i5 = uid;
                Uri uri5 = uri2;
                restoreCallingIdentity(identityToken4);
                throw th;
            }
        } else {
            Uri uri6 = uri2;
            throw new NullPointerException("Uri must not be null");
        }
        int i6 = i + 1;
        calls = calls2;
        identityToken = identityToken3;
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
        Bundle bundle = extras;
        Bundle.setDefusable(bundle, true);
        ContentResolver.validateSyncExtrasBundle(extras);
        int userId = UserHandle.getCallingUserId();
        int uId = Binder.getCallingUid();
        validateExtras(uId, bundle);
        int syncExemption = getSyncExemptionAndCleanUpExtrasForCaller(uId, bundle);
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                syncManager.scheduleSync(account, userId, uId, authority, bundle, -2, syncExemption);
            }
        } finally {
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
        long identityToken;
        int i = userId;
        enforceCrossUserPermission(i, "no permission to request sync as user: " + i);
        int callerUid = Binder.getCallingUid();
        Bundle extras = request.getBundle();
        validateExtras(callerUid, extras);
        int syncExemption = getSyncExemptionAndCleanUpExtrasForCaller(callerUid, extras);
        long identityToken2 = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager == null) {
                restoreCallingIdentity(identityToken2);
                return;
            }
            long flextime = request.getSyncFlexTime();
            long runAtTime = request.getSyncRunTime();
            if (request.isPeriodic()) {
                try {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
                    getSyncManager().updateOrAddPeriodicSync(new SyncStorageEngine.EndPoint(request.getAccount(), request.getProvider(), i), clampPeriod(runAtTime), flextime, extras);
                    Bundle bundle = extras;
                    int i2 = callerUid;
                    identityToken = identityToken2;
                } catch (Throwable th) {
                    th = th;
                    Bundle bundle2 = extras;
                    int i3 = callerUid;
                    identityToken = identityToken2;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } else {
                int i4 = i;
                int i5 = callerUid;
                identityToken = identityToken2;
                Bundle bundle3 = extras;
                try {
                    syncManager.scheduleSync(request.getAccount(), i4, callerUid, request.getProvider(), extras, -2, syncExemption);
                } catch (Throwable th2) {
                    th = th2;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th3) {
            th = th3;
            Bundle bundle4 = extras;
            int i6 = callerUid;
            identityToken = identityToken2;
            restoreCallingIdentity(identityToken);
            throw th;
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
                    SyncStorageEngine.EndPoint info = new SyncStorageEngine.EndPoint(account, authority, userId);
                    syncManager.clearScheduledSyncOperations(info);
                    syncManager.cancelActiveSync(info, null, "API");
                }
            } finally {
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
            int callingUid = Binder.getCallingUid();
            if (request.isPeriodic()) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
            }
            Bundle extras = new Bundle(request.getBundle());
            validateExtras(callingUid, extras);
            long identityToken = clearCallingIdentity();
            try {
                SyncStorageEngine.EndPoint info = new SyncStorageEngine.EndPoint(request.getAccount(), request.getProvider(), userId);
                if (request.isPeriodic()) {
                    SyncManager syncManager2 = getSyncManager();
                    syncManager2.removePeriodicSync(info, extras, "cancelRequest() by uid=" + callingUid);
                }
                syncManager.cancelScheduledSyncOperation(info, extras);
                syncManager.cancelActiveSync(info, extras, "API");
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
            return getSyncManager().getSyncAdapterTypes(userId);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) {
        enforceCrossUserPermission(userId, "no permission to read sync settings for user " + userId);
        long identityToken = clearCallingIdentity();
        try {
            return getSyncManager().getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
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
                return syncManager.getSyncStorageEngine().getSyncAutomatically(account, userId, providerName);
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
        int i = userId;
        if (!TextUtils.isEmpty(providerName)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
            enforceCrossUserPermission(i, "no permission to modify the sync settings for user " + i);
            int callingUid = Binder.getCallingUid();
            int syncExemptionFlag = getSyncExemptionForCaller(callingUid);
            long identityToken = clearCallingIdentity();
            try {
                SyncManager syncManager = getSyncManager();
                if (syncManager != null) {
                    syncManager.getSyncStorageEngine().setSyncAutomatically(account, i, providerName, sync, syncExemptionFlag, callingUid);
                }
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new IllegalArgumentException("Authority must be non-empty");
        }
    }

    public void addPeriodicSync(Account account, String authority, Bundle extras, long pollFrequency) {
        long identityToken;
        Account account2 = account;
        Bundle bundle = extras;
        Bundle.setDefusable(bundle, true);
        if (account2 == null) {
            long j = pollFrequency;
            throw new IllegalArgumentException("Account must not be null");
        } else if (!TextUtils.isEmpty(authority)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
            validateExtras(Binder.getCallingUid(), bundle);
            int userId = UserHandle.getCallingUserId();
            long pollFrequency2 = clampPeriod(pollFrequency);
            long defaultFlex = SyncStorageEngine.calculateDefaultFlexTime(pollFrequency2);
            long identityToken2 = clearCallingIdentity();
            try {
                identityToken = identityToken2;
                try {
                    getSyncManager().updateOrAddPeriodicSync(new SyncStorageEngine.EndPoint(account2, authority, userId), pollFrequency2, defaultFlex, bundle);
                    restoreCallingIdentity(identityToken);
                } catch (Throwable th) {
                    th = th;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            long j2 = pollFrequency;
            throw new IllegalArgumentException("Authority must not be empty.");
        }
    }

    public void removePeriodicSync(Account account, String authority, Bundle extras) {
        Bundle.setDefusable(extras, true);
        if (account == null) {
            throw new IllegalArgumentException("Account must not be null");
        } else if (!TextUtils.isEmpty(authority)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
            validateExtras(Binder.getCallingUid(), extras);
            int callingUid = Binder.getCallingUid();
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                SyncManager syncManager = getSyncManager();
                SyncStorageEngine.EndPoint endPoint = new SyncStorageEngine.EndPoint(account, authority, userId);
                syncManager.removePeriodicSync(endPoint, extras, "removePeriodicSync() by uid=" + callingUid);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new IllegalArgumentException("Authority must not be empty");
        }
    }

    public List<PeriodicSync> getPeriodicSyncs(Account account, String providerName, ComponentName cname) {
        if (account == null) {
            throw new IllegalArgumentException("Account must not be null");
        } else if (!TextUtils.isEmpty(providerName)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_SETTINGS", "no permission to read the sync settings");
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                return getSyncManager().getPeriodicSyncs(new SyncStorageEngine.EndPoint(account, providerName, userId));
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new IllegalArgumentException("Authority must not be empty");
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
                return syncManager.computeSyncable(account, userId, providerName, false);
            }
            restoreCallingIdentity(identityToken);
            return -1;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void setIsSyncable(Account account, String providerName, int syncable) {
        if (!TextUtils.isEmpty(providerName)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SYNC_SETTINGS", "no permission to write the sync settings");
            int syncable2 = normalizeSyncable(syncable);
            int callingUid = Binder.getCallingUid();
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                SyncManager syncManager = getSyncManager();
                if (syncManager != null) {
                    syncManager.getSyncStorageEngine().setIsSyncable(account, userId, providerName, syncable2, callingUid);
                }
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new IllegalArgumentException("Authority must not be empty");
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
                return syncManager.getSyncStorageEngine().getMasterSyncAutomatically(userId);
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
        int callingUid = Binder.getCallingUid();
        long identityToken = clearCallingIdentity();
        try {
            SyncManager syncManager = getSyncManager();
            if (syncManager != null) {
                syncManager.getSyncStorageEngine().setMasterSyncAutomatically(flag, userId, getSyncExemptionForCaller(callingUid), callingUid);
            }
        } finally {
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
            boolean isSyncActive = syncManager.getSyncStorageEngine().isSyncActive(new SyncStorageEngine.EndPoint(account, authority, userId));
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
            return getSyncManager().getSyncStorageEngine().getCurrentSyncsCopy(userId, canAccessAccounts);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public SyncStatusInfo getSyncStatus(Account account, String authority, ComponentName cname) {
        return getSyncStatusAsUser(account, authority, cname, UserHandle.getCallingUserId());
    }

    public SyncStatusInfo getSyncStatusAsUser(Account account, String authority, ComponentName cname, int userId) {
        if (!TextUtils.isEmpty(authority)) {
            enforceCrossUserPermission(userId, "no permission to read the sync stats for user " + userId);
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SYNC_STATS", "no permission to read the sync stats");
            long identityToken = clearCallingIdentity();
            try {
                SyncManager syncManager = getSyncManager();
                if (syncManager == null) {
                    return null;
                } else if (account == null || authority == null) {
                    throw new IllegalArgumentException("Must call sync status with valid authority");
                } else {
                    SyncStatusInfo statusByAuthority = syncManager.getSyncStorageEngine().getStatusByAuthority(new SyncStorageEngine.EndPoint(account, authority, userId));
                    restoreCallingIdentity(identityToken);
                    return statusByAuthority;
                }
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new IllegalArgumentException("Authority must not be empty");
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
            return syncManager.getSyncStorageEngine().isSyncPending(new SyncStorageEngine.EndPoint(account, authority, userId));
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
        } finally {
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
        } finally {
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

    @GuardedBy("mCache")
    private ArrayMap<Pair<String, Uri>, Bundle> findOrCreateCacheLocked(int userId, String providerPackageName) {
        ArrayMap<String, ArrayMap<Pair<String, Uri>, Bundle>> userCache = this.mCache.get(userId);
        if (userCache == null) {
            userCache = new ArrayMap<>();
            this.mCache.put(userId, userCache);
        }
        ArrayMap<Pair<String, Uri>, Bundle> packageCache = userCache.get(providerPackageName);
        if (packageCache != null) {
            return packageCache;
        }
        ArrayMap<Pair<String, Uri>, Bundle> packageCache2 = new ArrayMap<>();
        userCache.put(providerPackageName, packageCache2);
        return packageCache2;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mCache")
    public void invalidateCacheLocked(int userId, String providerPackageName, Uri uri) {
        ArrayMap<String, ArrayMap<Pair<String, Uri>, Bundle>> userCache = this.mCache.get(userId);
        if (userCache != null) {
            ArrayMap<Pair<String, Uri>, Bundle> packageCache = userCache.get(providerPackageName);
            if (packageCache != null) {
                if (uri != null) {
                    int i = 0;
                    while (i < packageCache.size()) {
                        Pair<String, Uri> key = packageCache.keyAt(i);
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
            bundle = findOrCreateCacheLocked(userId, providerPackageName).get(fullKey);
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
                String permissions = allowNonFull ? "android.permission.INTERACT_ACROSS_USERS_FULL or android.permission.INTERACT_ACROSS_USERS" : "android.permission.INTERACT_ACROSS_USERS_FULL";
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

    private void validateExtras(int callingUid, Bundle extras) {
        if (extras.containsKey("v_exemption") && callingUid != 0 && callingUid != 1000 && callingUid != 2000) {
            Log.w(TAG, "Invalid extras specified. requestsync -f/-F needs to run on 'adb shell'");
            throw new SecurityException("Invalid extras specified.");
        }
    }

    private int getSyncExemptionForCaller(int callingUid) {
        return getSyncExemptionAndCleanUpExtrasForCaller(callingUid, null);
    }

    private int getSyncExemptionAndCleanUpExtrasForCaller(int callingUid, Bundle extras) {
        int procState;
        if (extras != null) {
            int exemption = extras.getInt("v_exemption", -1);
            extras.remove("v_exemption");
            if (exemption != -1) {
                return exemption;
            }
        }
        ActivityManagerInternal ami = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        if (ami != null) {
            procState = ami.getUidProcessState(callingUid);
        } else {
            procState = 19;
        }
        if (procState <= 2) {
            return 2;
        }
        if (procState <= 5) {
            return 1;
        }
        return 0;
    }

    private void enforceShell(String method) {
        int callingUid = Binder.getCallingUid();
        if (callingUid != 2000 && callingUid != 0) {
            throw new SecurityException("Non-shell user attempted to call " + method);
        }
    }

    public void resetTodayStats() {
        enforceShell("resetTodayStats");
        if (this.mSyncManager != null) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mSyncManager.resetTodayStats();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new ContentShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }
}
