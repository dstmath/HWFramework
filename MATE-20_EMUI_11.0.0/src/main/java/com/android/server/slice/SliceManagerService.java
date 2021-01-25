package com.android.server.slice;

import android.app.AppOpsManager;
import android.app.slice.ISliceManager;
import android.app.slice.SliceSpec;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.AssistUtils;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class SliceManagerService extends ISliceManager.Stub {
    private static final String TAG = "SliceManagerService";
    private final AppOpsManager mAppOps;
    private final UsageStatsManagerInternal mAppUsageStats;
    private final AssistUtils mAssistUtils;
    @GuardedBy({"mLock"})
    private final SparseArray<PackageMatchingCache> mAssistantLookup;
    private final Context mContext;
    private final Handler mHandler;
    @GuardedBy({"mLock"})
    private final SparseArray<PackageMatchingCache> mHomeLookup;
    private final Object mLock;
    private final PackageManagerInternal mPackageManagerInternal;
    private final SlicePermissionManager mPermissions;
    @GuardedBy({"mLock"})
    private final ArrayMap<Uri, PinnedSliceState> mPinnedSlicesByUri;
    private final BroadcastReceiver mReceiver;

    public SliceManagerService(Context context) {
        this(context, createHandler().getLooper());
    }

    @VisibleForTesting
    SliceManagerService(Context context, Looper looper) {
        this.mLock = new Object();
        this.mPinnedSlicesByUri = new ArrayMap<>();
        this.mAssistantLookup = new SparseArray<>();
        this.mHomeLookup = new SparseArray<>();
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.slice.SliceManagerService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (userId == -10000) {
                    Slog.w(SliceManagerService.TAG, "Intent broadcast does not contain user handle: " + intent);
                    return;
                }
                Uri data = intent.getData();
                String pkg = data != null ? data.getSchemeSpecificPart() : null;
                if (pkg == null) {
                    Slog.w(SliceManagerService.TAG, "Intent broadcast does not contain package name: " + intent);
                    return;
                }
                String action = intent.getAction();
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != 267468725) {
                    if (hashCode == 525384130 && action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        c = 0;
                    }
                } else if (action.equals("android.intent.action.PACKAGE_DATA_CLEARED")) {
                    c = 1;
                }
                if (c != 0) {
                    if (c == 1) {
                        SliceManagerService.this.mPermissions.removePkg(pkg, userId);
                    }
                } else if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    SliceManagerService.this.mPermissions.removePkg(pkg, userId);
                }
            }
        };
        this.mContext = context;
        this.mPackageManagerInternal = (PackageManagerInternal) Preconditions.checkNotNull((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mAssistUtils = new AssistUtils(context);
        this.mHandler = new Handler(looper);
        this.mAppUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mPermissions = new SlicePermissionManager(this.mContext, looper);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void systemReady() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUnlockUser(int userId) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onStopUser(int userId) {
        synchronized (this.mLock) {
            this.mPinnedSlicesByUri.values().removeIf(new Predicate(userId) {
                /* class com.android.server.slice.$$Lambda$SliceManagerService$EsoJb3dNe0G_qzoQixj72OS5gnw */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return SliceManagerService.lambda$onStopUser$0(this.f$0, (PinnedSliceState) obj);
                }
            });
        }
    }

    static /* synthetic */ boolean lambda$onStopUser$0(int userId, PinnedSliceState s) {
        return ContentProvider.getUserIdFromUri(s.getUri()) == userId;
    }

    public Uri[] getPinnedSlices(String pkg) {
        verifyCaller(pkg);
        int callingUser = Binder.getCallingUserHandle().getIdentifier();
        ArrayList<Uri> ret = new ArrayList<>();
        synchronized (this.mLock) {
            for (PinnedSliceState state : this.mPinnedSlicesByUri.values()) {
                if (Objects.equals(pkg, state.getPkg())) {
                    Uri uri = state.getUri();
                    if (ContentProvider.getUserIdFromUri(uri, callingUser) == callingUser) {
                        ret.add(ContentProvider.getUriWithoutUserId(uri));
                    }
                }
            }
        }
        return (Uri[]) ret.toArray(new Uri[ret.size()]);
    }

    public void pinSlice(String pkg, Uri uri, SliceSpec[] specs, IBinder token) throws RemoteException {
        verifyCaller(pkg);
        enforceAccess(pkg, uri);
        int user = Binder.getCallingUserHandle().getIdentifier();
        Uri uri2 = ContentProvider.maybeAddUserId(uri, user);
        String slicePkg = getProviderPkg(uri2, user);
        getOrCreatePinnedSlice(uri2, slicePkg).pin(pkg, specs, token);
        this.mHandler.post(new Runnable(slicePkg, pkg, user) {
            /* class com.android.server.slice.$$Lambda$SliceManagerService$pJ39TkC3AEVezLFEPuJgSQSTDJM */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                SliceManagerService.this.lambda$pinSlice$1$SliceManagerService(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$pinSlice$1$SliceManagerService(String slicePkg, String pkg, int user) {
        if (slicePkg != null && !Objects.equals(pkg, slicePkg)) {
            this.mAppUsageStats.reportEvent(slicePkg, user, (isAssistant(pkg, user) || isDefaultHomeApp(pkg, user)) ? 13 : 14);
        }
    }

    public void unpinSlice(String pkg, Uri uri, IBinder token) throws RemoteException {
        verifyCaller(pkg);
        enforceAccess(pkg, uri);
        Uri uri2 = ContentProvider.maybeAddUserId(uri, Binder.getCallingUserHandle().getIdentifier());
        if (getPinnedSlice(uri2).unpin(pkg, token)) {
            removePinnedSlice(uri2);
        }
    }

    public boolean hasSliceAccess(String pkg) throws RemoteException {
        verifyCaller(pkg);
        return hasFullSliceAccess(pkg, Binder.getCallingUserHandle().getIdentifier());
    }

    public SliceSpec[] getPinnedSpecs(Uri uri, String pkg) throws RemoteException {
        verifyCaller(pkg);
        enforceAccess(pkg, uri);
        return getPinnedSlice(ContentProvider.maybeAddUserId(uri, Binder.getCallingUserHandle().getIdentifier())).getSpecs();
    }

    public void grantSlicePermission(String pkg, String toPkg, Uri uri) throws RemoteException {
        verifyCaller(pkg);
        int user = Binder.getCallingUserHandle().getIdentifier();
        enforceOwner(pkg, uri, user);
        this.mPermissions.grantSliceAccess(toPkg, user, pkg, user, uri);
    }

    public void revokeSlicePermission(String pkg, String toPkg, Uri uri) throws RemoteException {
        verifyCaller(pkg);
        int user = Binder.getCallingUserHandle().getIdentifier();
        enforceOwner(pkg, uri, user);
        this.mPermissions.revokeSliceAccess(toPkg, user, pkg, user, uri);
    }

    public int checkSlicePermission(Uri uri, String callingPkg, String pkg, int pid, int uid, String[] autoGrantPermissions) {
        int userId = UserHandle.getUserId(uid);
        if (pkg == null) {
            String[] packagesForUid = this.mContext.getPackageManager().getPackagesForUid(uid);
            int length = packagesForUid.length;
            int i = 0;
            while (i < length) {
                if (checkSlicePermission(uri, callingPkg, packagesForUid[i], pid, uid, autoGrantPermissions) == 0) {
                    return 0;
                }
                i++;
                length = length;
                packagesForUid = packagesForUid;
            }
            return -1;
        } else if (hasFullSliceAccess(pkg, userId) || this.mPermissions.hasPermission(pkg, userId, uri)) {
            return 0;
        } else {
            if (!(autoGrantPermissions == null || callingPkg == null)) {
                enforceOwner(callingPkg, uri, userId);
                for (String perm : autoGrantPermissions) {
                    if (this.mContext.checkPermission(perm, pid, uid) == 0) {
                        int providerUser = ContentProvider.getUserIdFromUri(uri, userId);
                        this.mPermissions.grantSliceAccess(pkg, userId, getProviderPkg(uri, providerUser), providerUser, uri);
                        return 0;
                    }
                }
            }
            return this.mContext.checkUriPermission(uri, pid, uid, 2) == 0 ? 0 : -1;
        }
    }

    public void grantPermissionFromUser(Uri uri, String pkg, String callingPkg, boolean allSlices) {
        verifyCaller(callingPkg);
        getContext().enforceCallingOrSelfPermission("android.permission.MANAGE_SLICE_PERMISSIONS", "Slice granting requires MANAGE_SLICE_PERMISSIONS");
        int userId = Binder.getCallingUserHandle().getIdentifier();
        if (allSlices) {
            this.mPermissions.grantFullAccess(pkg, userId);
        } else {
            Uri grantUri = uri.buildUpon().path("").build();
            int providerUser = ContentProvider.getUserIdFromUri(grantUri, userId);
            this.mPermissions.grantSliceAccess(pkg, userId, getProviderPkg(grantUri, providerUser), providerUser, grantUri);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.getContentResolver().notifyChange(uri, null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public byte[] getBackupPayload(int user) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Caller must be system");
        } else if (user != 0) {
            Slog.w(TAG, "getBackupPayload: cannot backup policy for user " + user);
            return null;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                XmlSerializer out = XmlPullParserFactory.newInstance().newSerializer();
                out.setOutput(baos, Xml.Encoding.UTF_8.name());
                this.mPermissions.writeBackup(out);
                out.flush();
                return baos.toByteArray();
            } catch (IOException | XmlPullParserException e) {
                Slog.w(TAG, "getBackupPayload: error writing payload for user " + user, e);
                return null;
            }
        }
    }

    public void applyRestore(byte[] payload, int user) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Caller must be system");
        } else if (payload == null) {
            Slog.w(TAG, "applyRestore: no payload to restore for user " + user);
        } else if (user != 0) {
            Slog.w(TAG, "applyRestore: cannot restore policy for user " + user);
        } else {
            ByteArrayInputStream bais = new ByteArrayInputStream(payload);
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(bais, Xml.Encoding.UTF_8.name());
                this.mPermissions.readRestore(parser);
            } catch (IOException | NumberFormatException | XmlPullParserException e) {
                Slog.w(TAG, "applyRestore: error reading payload", e);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.slice.SliceManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new SliceShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    private void enforceOwner(String pkg, Uri uri, int user) {
        if (!Objects.equals(getProviderPkg(uri, user), pkg) || pkg == null) {
            throw new SecurityException("Caller must own " + uri);
        }
    }

    /* access modifiers changed from: protected */
    public void removePinnedSlice(Uri uri) {
        synchronized (this.mLock) {
            this.mPinnedSlicesByUri.remove(uri).destroy();
        }
    }

    private PinnedSliceState getPinnedSlice(Uri uri) {
        PinnedSliceState manager;
        synchronized (this.mLock) {
            manager = this.mPinnedSlicesByUri.get(uri);
            if (manager == null) {
                throw new IllegalStateException(String.format("Slice %s not pinned", uri.toString()));
            }
        }
        return manager;
    }

    private PinnedSliceState getOrCreatePinnedSlice(Uri uri, String pkg) {
        PinnedSliceState manager;
        synchronized (this.mLock) {
            manager = this.mPinnedSlicesByUri.get(uri);
            if (manager == null) {
                manager = createPinnedSlice(uri, pkg);
                this.mPinnedSlicesByUri.put(uri, manager);
            }
        }
        return manager;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public PinnedSliceState createPinnedSlice(Uri uri, String pkg) {
        return new PinnedSliceState(this, uri, pkg);
    }

    public Object getLock() {
        return this.mLock;
    }

    public Context getContext() {
        return this.mContext;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    /* access modifiers changed from: protected */
    public int checkAccess(String pkg, Uri uri, int uid, int pid) {
        return checkSlicePermission(uri, null, pkg, pid, uid, null);
    }

    private String getProviderPkg(Uri uri, int user) {
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mContext.getPackageManager().resolveContentProviderAsUser(ContentProvider.getUriWithoutUserId(uri).getAuthority(), 0, ContentProvider.getUserIdFromUri(uri, user)).packageName;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void enforceCrossUser(String pkg, Uri uri) {
        int user = Binder.getCallingUserHandle().getIdentifier();
        if (ContentProvider.getUserIdFromUri(uri, user) != user) {
            getContext().enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "Slice interaction across users requires INTERACT_ACROSS_USERS_FULL");
        }
    }

    private void enforceAccess(String pkg, Uri uri) throws RemoteException {
        if (checkAccess(pkg, uri, Binder.getCallingUid(), Binder.getCallingPid()) == 0 || Objects.equals(pkg, getProviderPkg(uri, ContentProvider.getUserIdFromUri(uri, Binder.getCallingUserHandle().getIdentifier())))) {
            enforceCrossUser(pkg, uri);
            return;
        }
        throw new SecurityException("Access to slice " + uri + " is required");
    }

    private void verifyCaller(String pkg) {
        this.mAppOps.checkPackage(Binder.getCallingUid(), pkg);
    }

    private boolean hasFullSliceAccess(String pkg, int userId) {
        long ident = Binder.clearCallingIdentity();
        try {
            return isDefaultHomeApp(pkg, userId) || isAssistant(pkg, userId) || isGrantedFullAccess(pkg, userId);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean isAssistant(String pkg, int userId) {
        return getAssistantMatcher(userId).matches(pkg);
    }

    private boolean isDefaultHomeApp(String pkg, int userId) {
        return getHomeMatcher(userId).matches(pkg);
    }

    private PackageMatchingCache getAssistantMatcher(int userId) {
        PackageMatchingCache matcher = this.mAssistantLookup.get(userId);
        if (matcher != null) {
            return matcher;
        }
        PackageMatchingCache matcher2 = new PackageMatchingCache(new Supplier(userId) {
            /* class com.android.server.slice.$$Lambda$SliceManagerService$ic_PW16x_KcViNszMwHhErqI0s */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return SliceManagerService.this.lambda$getAssistantMatcher$2$SliceManagerService(this.f$1);
            }
        });
        this.mAssistantLookup.put(userId, matcher2);
        return matcher2;
    }

    private PackageMatchingCache getHomeMatcher(int userId) {
        PackageMatchingCache matcher = this.mHomeLookup.get(userId);
        if (matcher != null) {
            return matcher;
        }
        PackageMatchingCache matcher2 = new PackageMatchingCache(new Supplier(userId) {
            /* class com.android.server.slice.$$Lambda$SliceManagerService$LkusK1jmu9JKJTiMRWqWxNGEGbY */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return SliceManagerService.this.lambda$getHomeMatcher$3$SliceManagerService(this.f$1);
            }
        });
        this.mHomeLookup.put(userId, matcher2);
        return matcher2;
    }

    /* access modifiers changed from: private */
    /* renamed from: getAssistant */
    public String lambda$getAssistantMatcher$2$SliceManagerService(int userId) {
        ComponentName cn = this.mAssistUtils.getAssistComponentForUser(userId);
        if (cn == null) {
            return null;
        }
        return cn.getPackageName();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    /* renamed from: getDefaultHome */
    public String lambda$getHomeMatcher$3$SliceManagerService(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            List<ResolveInfo> allHomeCandidates = new ArrayList<>();
            ComponentName defaultLauncher = this.mPackageManagerInternal.getHomeActivitiesAsUser(allHomeCandidates, userId);
            ComponentName detected = null;
            if (defaultLauncher != null) {
                detected = defaultLauncher;
            }
            if (detected == null) {
                int size = allHomeCandidates.size();
                int lastPriority = Integer.MIN_VALUE;
                for (int i = 0; i < size; i++) {
                    ResolveInfo ri = allHomeCandidates.get(i);
                    if (ri.activityInfo.applicationInfo.isSystemApp()) {
                        if (ri.priority >= lastPriority) {
                            detected = ri.activityInfo.getComponentName();
                            lastPriority = ri.priority;
                        }
                    }
                }
            }
            return detected != null ? detected.getPackageName() : null;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isGrantedFullAccess(String pkg, int userId) {
        return this.mPermissions.hasFullAccess(pkg, userId);
    }

    private static ServiceThread createHandler() {
        ServiceThread handlerThread = new ServiceThread(TAG, 10, true);
        handlerThread.start();
        return handlerThread;
    }

    public String[] getAllPackagesGranted(String authority) {
        return this.mPermissions.getAllPackagesGranted(getProviderPkg(new Uri.Builder().scheme("content").authority(authority).build(), 0));
    }

    /* access modifiers changed from: package-private */
    public static class PackageMatchingCache {
        private String mCurrentPkg;
        private final Supplier<String> mPkgSource;

        public PackageMatchingCache(Supplier<String> pkgSource) {
            this.mPkgSource = pkgSource;
        }

        public boolean matches(String pkgCandidate) {
            if (pkgCandidate == null) {
                return false;
            }
            if (Objects.equals(pkgCandidate, this.mCurrentPkg)) {
                return true;
            }
            this.mCurrentPkg = this.mPkgSource.get();
            return Objects.equals(pkgCandidate, this.mCurrentPkg);
        }
    }

    public static class Lifecycle extends SystemService {
        private SliceManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.slice.SliceManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.slice.SliceManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            this.mService = new SliceManagerService(getContext());
            publishBinderService("slice", this.mService);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemReady();
            }
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }

        @Override // com.android.server.SystemService
        public void onStopUser(int userHandle) {
            this.mService.onStopUser(userHandle);
        }
    }

    private class SliceGrant {
        private final String mPkg;
        private final Uri mUri;
        private final int mUserId;

        public SliceGrant(Uri uri, String pkg, int userId) {
            this.mUri = uri;
            this.mPkg = pkg;
            this.mUserId = userId;
        }

        public int hashCode() {
            return this.mUri.hashCode() + this.mPkg.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof SliceGrant)) {
                return false;
            }
            SliceGrant other = (SliceGrant) obj;
            if (!Objects.equals(other.mUri, this.mUri) || !Objects.equals(other.mPkg, this.mPkg) || other.mUserId != this.mUserId) {
                return false;
            }
            return true;
        }
    }
}
