package com.android.server.uri;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.GrantedUriPermission;
import android.app.IUriGrantsManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.IoThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.SystemServiceManager;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.uri.UriPermission;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class UriGrantsManagerService extends IUriGrantsManager.Stub {
    private static final String ATTR_CREATED_TIME = "createdTime";
    private static final String ATTR_MODE_FLAGS = "modeFlags";
    private static final String ATTR_PREFIX = "prefix";
    private static final String ATTR_SOURCE_PKG = "sourcePkg";
    private static final String ATTR_SOURCE_USER_ID = "sourceUserId";
    private static final String ATTR_TARGET_PKG = "targetPkg";
    private static final String ATTR_TARGET_USER_ID = "targetUserId";
    private static final String ATTR_URI = "uri";
    private static final String ATTR_USER_HANDLE = "userHandle";
    private static final boolean DEBUG = false;
    private static final Set<String> EXEMPTED_AUTHORITIES = new HashSet<String>() {
        /* class com.android.server.uri.UriGrantsManagerService.AnonymousClass1 */

        {
            add("com.huawei.systemmanager.fileProvider");
            add("com.huawei.pcassistant.provider");
            add("com.huawei.android.instantshare.onestep.provider");
            add("com.huawei.nb.service.fileprovider");
            add("com.huawei.searchservice.fileprovider");
            add("com.huawei.betaclub.provider");
            add("com.huawei.mediacenter.fileprovider");
            add("com.huawei.homevision.settings.fileProvider");
            add("com.huawei.aod.fileprovider");
        }
    };
    private static final int MAX_PERSISTED_URI_GRANTS = 128;
    private static final String TAG = "UriGrantsManagerService";
    private static final String TAG_URI_GRANT = "uri-grant";
    private static final String TAG_URI_GRANTS = "uri-grants";
    ActivityManagerInternal mAmInternal;
    private final Context mContext;
    private final AtomicFile mGrantFile;
    private final SparseArray<ArrayMap<GrantUri, UriPermission>> mGrantedUriPermissions;
    private final H mH;
    private final Object mLock;
    PackageManagerInternal mPmInternal;

    private UriGrantsManagerService(Context context) {
        this.mLock = new Object();
        this.mGrantedUriPermissions = new SparseArray<>();
        this.mContext = context;
        this.mH = new H(IoThread.get().getLooper());
        this.mGrantFile = new AtomicFile(new File(SystemServiceManager.ensureSystemDir(), "urigrants.xml"), TAG_URI_GRANTS);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void start() {
        LocalServices.addService(UriGrantsManagerInternal.class, new LocalService());
    }

    /* access modifiers changed from: package-private */
    public void onActivityManagerInternalAdded() {
        this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
    }

    public static final class Lifecycle extends SystemService {
        private final UriGrantsManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new UriGrantsManagerService(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.uri.UriGrantsManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.uri.UriGrantsManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            publishBinderService("uri_grants", this.mService);
            this.mService.start();
        }

        public UriGrantsManagerService getService() {
            return this.mService;
        }
    }

    public void grantUriPermissionFromOwner(IBinder token, int fromUid, String targetPkg, Uri uri, int modeFlags, int sourceUserId, int targetUserId) {
        int targetUserId2 = this.mAmInternal.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), targetUserId, false, 2, "grantUriPermissionFromOwner", (String) null);
        synchronized (this.mLock) {
            try {
                UriPermissionOwner owner = UriPermissionOwner.fromExternalToken(token);
                if (owner != null) {
                    try {
                        if (fromUid != Binder.getCallingUid()) {
                            try {
                                if (Binder.getCallingUid() != Process.myUid()) {
                                    throw new SecurityException("nice try");
                                }
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                        if (targetPkg == null) {
                            throw new IllegalArgumentException("null target");
                        } else if (uri != null) {
                            try {
                                grantUriPermission(fromUid, targetPkg, new GrantUri(sourceUserId, uri, false), modeFlags, owner, targetUserId2);
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        } else {
                            throw new IllegalArgumentException("null uri");
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unknown owner: ");
                    sb.append(token);
                    throw new IllegalArgumentException(sb.toString());
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    public ParceledListSlice<UriPermission> getUriPermissions(String packageName, boolean incoming, boolean persistedOnly) {
        enforceNotIsolatedCaller("getUriPermissions");
        Preconditions.checkNotNull(packageName, "packageName");
        int callingUid = Binder.getCallingUid();
        try {
            if (AppGlobals.getPackageManager().getPackageUid(packageName, 786432, UserHandle.getUserId(callingUid)) == callingUid) {
                ArrayList<UriPermission> result = Lists.newArrayList();
                synchronized (this.mLock) {
                    if (incoming) {
                        ArrayMap<GrantUri, UriPermission> perms = this.mGrantedUriPermissions.get(callingUid);
                        if (perms == null) {
                            Slog.w(TAG, "No permission grants found for " + packageName);
                        } else {
                            for (int j = 0; j < perms.size(); j++) {
                                UriPermission perm = perms.valueAt(j);
                                if (packageName.equals(perm.targetPkg) && (!persistedOnly || perm.persistedModeFlags != 0)) {
                                    result.add(perm.buildPersistedPublicApiObject());
                                }
                            }
                        }
                    } else {
                        int size = this.mGrantedUriPermissions.size();
                        for (int i = 0; i < size; i++) {
                            ArrayMap<GrantUri, UriPermission> perms2 = this.mGrantedUriPermissions.valueAt(i);
                            for (int j2 = 0; j2 < perms2.size(); j2++) {
                                UriPermission perm2 = perms2.valueAt(j2);
                                if (packageName.equals(perm2.sourcePkg) && (!persistedOnly || perm2.persistedModeFlags != 0)) {
                                    result.add(perm2.buildPersistedPublicApiObject());
                                }
                            }
                        }
                    }
                }
                return new ParceledListSlice<>(result);
            }
            throw new SecurityException("Package " + packageName + " does not belong to calling UID " + callingUid);
        } catch (RemoteException e) {
            throw new SecurityException("Failed to verify package name ownership");
        }
    }

    public ParceledListSlice<GrantedUriPermission> getGrantedUriPermissions(String packageName, int userId) {
        this.mAmInternal.enforceCallingPermission("android.permission.GET_APP_GRANTED_URI_PERMISSIONS", "getGrantedUriPermissions");
        List<GrantedUriPermission> result = new ArrayList<>();
        synchronized (this.mLock) {
            int size = this.mGrantedUriPermissions.size();
            for (int i = 0; i < size; i++) {
                ArrayMap<GrantUri, UriPermission> perms = this.mGrantedUriPermissions.valueAt(i);
                for (int j = 0; j < perms.size(); j++) {
                    UriPermission perm = perms.valueAt(j);
                    if ((packageName == null || packageName.equals(perm.targetPkg)) && perm.targetUserId == userId && perm.persistedModeFlags != 0) {
                        result.add(perm.buildGrantedUriPermission());
                    }
                }
            }
        }
        return new ParceledListSlice<>(result);
    }

    public void takePersistableUriPermission(Uri uri, int modeFlags, String toPackage, int userId) {
        int uid;
        boolean prefixValid = false;
        if (toPackage != null) {
            this.mAmInternal.enforceCallingPermission("android.permission.FORCE_PERSISTABLE_URI_PERMISSIONS", "takePersistableUriPermission");
            uid = getPmInternal().getPackageUid(toPackage, 0, userId);
        } else {
            enforceNotIsolatedCaller("takePersistableUriPermission");
            uid = Binder.getCallingUid();
        }
        Preconditions.checkFlagsArgument(modeFlags, 3);
        synchronized (this.mLock) {
            boolean persistChanged = false;
            GrantUri grantUri = new GrantUri(userId, uri, false);
            UriPermission exactPerm = findUriPermissionLocked(uid, grantUri);
            UriPermission prefixPerm = findUriPermissionLocked(uid, new GrantUri(userId, uri, true));
            boolean exactValid = exactPerm != null && (exactPerm.persistableModeFlags & modeFlags) == modeFlags;
            if (prefixPerm != null && (prefixPerm.persistableModeFlags & modeFlags) == modeFlags) {
                prefixValid = true;
            }
            if (!exactValid) {
                if (!prefixValid) {
                    throw new SecurityException("No persistable permission grants found for UID " + uid + " and Uri " + grantUri.toSafeString());
                }
            }
            if (exactValid) {
                persistChanged = false | exactPerm.takePersistableModes(modeFlags);
            }
            if (prefixValid) {
                persistChanged |= prefixPerm.takePersistableModes(modeFlags);
            }
            if (persistChanged || maybePrunePersistedUriGrants(uid)) {
                schedulePersistUriGrants();
            }
        }
    }

    public void clearGrantedUriPermissions(String packageName, int userId) {
        this.mAmInternal.enforceCallingPermission("android.permission.CLEAR_APP_GRANTED_URI_PERMISSIONS", "clearGrantedUriPermissions");
        synchronized (this.mLock) {
            removeUriPermissionsForPackage(packageName, userId, true, true);
        }
    }

    public void releasePersistableUriPermission(Uri uri, int modeFlags, String toPackage, int userId) {
        int uid;
        if (toPackage != null) {
            this.mAmInternal.enforceCallingPermission("android.permission.FORCE_PERSISTABLE_URI_PERMISSIONS", "releasePersistableUriPermission");
            uid = getPmInternal().getPackageUid(toPackage, 0, userId);
        } else {
            enforceNotIsolatedCaller("releasePersistableUriPermission");
            uid = Binder.getCallingUid();
        }
        Preconditions.checkFlagsArgument(modeFlags, 3);
        synchronized (this.mLock) {
            boolean persistChanged = false;
            UriPermission exactPerm = findUriPermissionLocked(uid, new GrantUri(userId, uri, false));
            UriPermission prefixPerm = findUriPermissionLocked(uid, new GrantUri(userId, uri, true));
            if (exactPerm == null && prefixPerm == null) {
                if (toPackage == null) {
                    throw new SecurityException("No permission grants found for UID " + uid + " and Uri " + uri.toSafeString());
                }
            }
            if (exactPerm != null) {
                persistChanged = false | exactPerm.releasePersistableModes(modeFlags);
                removeUriPermissionIfNeeded(exactPerm);
            }
            if (prefixPerm != null) {
                persistChanged |= prefixPerm.releasePersistableModes(modeFlags);
                removeUriPermissionIfNeeded(prefixPerm);
            }
            if (persistChanged) {
                schedulePersistUriGrants();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeUriPermissionsForPackage(String packageName, int userHandle, boolean persistable, boolean targetOnly) {
        if (userHandle == -1 && packageName == null) {
            throw new IllegalArgumentException("Must narrow by either package or user");
        }
        boolean persistChanged = false;
        int N = this.mGrantedUriPermissions.size();
        int i = 0;
        while (i < N) {
            int targetUid = this.mGrantedUriPermissions.keyAt(i);
            ArrayMap<GrantUri, UriPermission> perms = this.mGrantedUriPermissions.valueAt(i);
            if (userHandle == -1 || userHandle == UserHandle.getUserId(targetUid)) {
                Iterator<UriPermission> it = perms.values().iterator();
                while (it.hasNext()) {
                    UriPermission perm = it.next();
                    if ((packageName == null || ((!targetOnly && perm.sourcePkg.equals(packageName)) || perm.targetPkg.equals(packageName))) && (!"downloads".equals(perm.uri.uri.getAuthority()) || persistable)) {
                        persistChanged |= perm.revokeModes(persistable ? -1 : -65, true);
                        if (perm.modeFlags == 0) {
                            it.remove();
                        }
                    }
                }
                if (perms.isEmpty()) {
                    this.mGrantedUriPermissions.remove(targetUid);
                    N--;
                    i--;
                }
            }
            i++;
        }
        if (persistChanged) {
            schedulePersistUriGrants();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkAuthorityGrants(int callingUid, ProviderInfo cpi, int userId, boolean checkUser) {
        ArrayMap<GrantUri, UriPermission> perms = this.mGrantedUriPermissions.get(callingUid);
        if (perms == null) {
            return false;
        }
        for (int i = perms.size() - 1; i >= 0; i--) {
            GrantUri grantUri = perms.keyAt(i);
            if ((grantUri.sourceUserId == userId || !checkUser) && matchesProvider(grantUri.uri, cpi)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesProvider(Uri uri, ProviderInfo cpi) {
        String uriAuth = uri.getAuthority();
        String cpiAuth = cpi.authority;
        if (cpiAuth.indexOf(59) == -1) {
            return cpiAuth.equals(uriAuth);
        }
        for (String str : cpiAuth.split(";")) {
            if (str.equals(uriAuth)) {
                return true;
            }
        }
        return false;
    }

    private boolean maybePrunePersistedUriGrants(int uid) {
        ArrayMap<GrantUri, UriPermission> perms = this.mGrantedUriPermissions.get(uid);
        if (perms == null || perms.size() < 128) {
            return false;
        }
        ArrayList<UriPermission> persisted = Lists.newArrayList();
        for (UriPermission perm : perms.values()) {
            if (perm.persistedModeFlags != 0) {
                persisted.add(perm);
            }
        }
        int trimCount = persisted.size() - 128;
        if (trimCount <= 0) {
            return false;
        }
        Collections.sort(persisted, new UriPermission.PersistedTimeComparator());
        for (int i = 0; i < trimCount; i++) {
            UriPermission perm2 = persisted.get(i);
            perm2.releasePersistableModes(-1);
            removeUriPermissionIfNeeded(perm2);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public NeededUriGrants checkGrantUriPermissionFromIntent(int callingUid, String targetPkg, Intent intent, int mode, NeededUriGrants needed, int targetUserId) {
        int contentUserHint;
        int targetUid;
        NeededUriGrants needed2;
        NeededUriGrants needed3 = needed;
        if (targetPkg == null) {
            throw new NullPointerException(ATTR_TARGET_PKG);
        } else if (intent == null) {
            return null;
        } else {
            Uri data = intent.getData();
            ClipData clip = intent.getClipData();
            if (data == null && clip == null) {
                return null;
            }
            int contentUserHint2 = intent.getContentUserHint();
            if (contentUserHint2 == -2) {
                contentUserHint = UserHandle.getUserId(callingUid);
            } else {
                contentUserHint = contentUserHint2;
            }
            IPackageManager pm = AppGlobals.getPackageManager();
            if (needed3 != null) {
                targetUid = needed3.targetUid;
            } else {
                try {
                    targetUid = pm.getPackageUid(targetPkg, 268435456, targetUserId);
                    if (targetUid < 0) {
                        return null;
                    }
                } catch (RemoteException e) {
                    return null;
                }
            }
            if (data != null) {
                GrantUri grantUri = GrantUri.resolve(contentUserHint, data);
                targetUid = checkGrantUriPermission(callingUid, targetPkg, grantUri, mode, targetUid);
                if (targetUid > 0) {
                    if (needed3 == null) {
                        needed2 = new NeededUriGrants(targetPkg, targetUid, mode);
                    } else {
                        needed2 = needed3;
                    }
                    needed2.add(grantUri);
                    needed3 = needed2;
                }
            }
            if (clip == null) {
                return needed3;
            }
            int targetUid2 = targetUid;
            NeededUriGrants needed4 = needed3;
            for (int i = 0; i < clip.getItemCount(); i++) {
                Uri uri = clip.getItemAt(i).getUri();
                if (uri != null) {
                    GrantUri grantUri2 = GrantUri.resolve(contentUserHint, uri);
                    int targetUid3 = checkGrantUriPermission(callingUid, targetPkg, grantUri2, mode, targetUid2);
                    if (targetUid3 > 0) {
                        if (needed4 == null) {
                            needed4 = new NeededUriGrants(targetPkg, targetUid3, mode);
                        }
                        needed4.add(grantUri2);
                    }
                    targetUid2 = targetUid3;
                } else {
                    Intent clipIntent = clip.getItemAt(i).getIntent();
                    if (clipIntent != null) {
                        NeededUriGrants newNeeded = checkGrantUriPermissionFromIntent(callingUid, targetPkg, clipIntent, mode, needed4, targetUserId);
                        if (newNeeded != null) {
                            needed4 = newNeeded;
                        }
                    }
                }
            }
            return needed4;
        }
    }

    /* access modifiers changed from: package-private */
    public void grantUriPermissionFromIntent(int callingUid, String targetPkg, Intent intent, UriPermissionOwner owner, int targetUserId) {
        NeededUriGrants needed = checkGrantUriPermissionFromIntent(callingUid, targetPkg, intent, intent != null ? intent.getFlags() : 0, null, targetUserId);
        if (needed != null) {
            grantUriPermissionUncheckedFromIntent(needed, owner);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00da  */
    public void readGrantedUriPermissions() {
        Throwable th;
        IOException e;
        XmlPullParserException e2;
        XmlPullParser in;
        long now;
        int targetUserId;
        int sourceUserId;
        int targetUid;
        int targetUid2;
        UriGrantsManagerService uriGrantsManagerService = this;
        long now2 = System.currentTimeMillis();
        FileInputStream fis = null;
        try {
            fis = uriGrantsManagerService.mGrantFile.openRead();
            XmlPullParser in2 = Xml.newPullParser();
            in2.setInput(fis, StandardCharsets.UTF_8.name());
            while (true) {
                int type = in2.next();
                if (type == 1) {
                    break;
                }
                String tag = in2.getName();
                if (type != 2) {
                    now = now2;
                    in = in2;
                } else if (TAG_URI_GRANT.equals(tag)) {
                    int userHandle = XmlUtils.readIntAttribute(in2, ATTR_USER_HANDLE, -10000);
                    if (userHandle != -10000) {
                        targetUserId = userHandle;
                        sourceUserId = userHandle;
                    } else {
                        int sourceUserId2 = XmlUtils.readIntAttribute(in2, ATTR_SOURCE_USER_ID);
                        targetUserId = XmlUtils.readIntAttribute(in2, ATTR_TARGET_USER_ID);
                        sourceUserId = sourceUserId2;
                    }
                    String sourcePkg = in2.getAttributeValue(null, ATTR_SOURCE_PKG);
                    String targetPkg = in2.getAttributeValue(null, ATTR_TARGET_PKG);
                    Uri uri = Uri.parse(in2.getAttributeValue(null, ATTR_URI));
                    boolean prefix = XmlUtils.readBooleanAttribute(in2, ATTR_PREFIX);
                    int modeFlags = XmlUtils.readIntAttribute(in2, ATTR_MODE_FLAGS);
                    long createdTime = XmlUtils.readLongAttribute(in2, ATTR_CREATED_TIME, now2);
                    now = now2;
                    try {
                        ProviderInfo pi = uriGrantsManagerService.getProviderInfo(uri.getAuthority(), sourceUserId, 786432);
                        if (pi == null || !sourcePkg.equals(pi.packageName)) {
                            in = in2;
                            Slog.w(TAG, "Persisted grant for " + uri + " had source " + sourcePkg + " but instead found " + pi);
                        } else {
                            try {
                                targetUid2 = -1;
                                try {
                                    targetUid = AppGlobals.getPackageManager().getPackageUid(targetPkg, 8192, targetUserId);
                                } catch (RemoteException e3) {
                                    targetUid = targetUid2;
                                    if (targetUid != -1) {
                                    }
                                    uriGrantsManagerService = this;
                                    now2 = now;
                                    in2 = in;
                                }
                            } catch (RemoteException e4) {
                                targetUid2 = -1;
                                targetUid = targetUid2;
                                if (targetUid != -1) {
                                }
                                uriGrantsManagerService = this;
                                now2 = now;
                                in2 = in;
                            }
                            if (targetUid != -1) {
                                in = in2;
                                uriGrantsManagerService.findOrCreateUriPermission(sourcePkg, targetPkg, targetUid, new GrantUri(sourceUserId, uri, prefix)).initPersistedModes(modeFlags, createdTime);
                            } else {
                                in = in2;
                            }
                        }
                    } catch (FileNotFoundException e5) {
                    } catch (IOException e6) {
                        e = e6;
                        Slog.wtf(TAG, "Failed reading Uri grants", e);
                        IoUtils.closeQuietly(fis);
                    } catch (XmlPullParserException e7) {
                        e2 = e7;
                        Slog.wtf(TAG, "Failed reading Uri grants", e2);
                        IoUtils.closeQuietly(fis);
                    }
                } else {
                    now = now2;
                    in = in2;
                }
                uriGrantsManagerService = this;
                now2 = now;
                in2 = in;
            }
        } catch (FileNotFoundException e8) {
        } catch (IOException e9) {
            e = e9;
            Slog.wtf(TAG, "Failed reading Uri grants", e);
        } catch (XmlPullParserException e10) {
            e2 = e10;
            Slog.wtf(TAG, "Failed reading Uri grants", e2);
        } catch (Throwable th2) {
            th = th2;
            IoUtils.closeQuietly(fis);
            throw th;
        }
        IoUtils.closeQuietly(fis);
    }

    private UriPermission findOrCreateUriPermission(String sourcePkg, String targetPkg, int targetUid, GrantUri grantUri) {
        ArrayMap<GrantUri, UriPermission> targetUris = this.mGrantedUriPermissions.get(targetUid);
        if (targetUris == null) {
            targetUris = Maps.newArrayMap();
            this.mGrantedUriPermissions.put(targetUid, targetUris);
        }
        UriPermission perm = targetUris.get(grantUri);
        if (perm != null) {
            return perm;
        }
        UriPermission perm2 = new UriPermission(sourcePkg, targetPkg, targetUid, grantUri);
        targetUris.put(grantUri, perm2);
        return perm2;
    }

    private void grantUriPermissionUnchecked(int targetUid, String targetPkg, GrantUri grantUri, int modeFlags, UriPermissionOwner owner) {
        if (Intent.isAccessUriMode(modeFlags)) {
            ProviderInfo pi = getProviderInfo(grantUri.uri.getAuthority(), grantUri.sourceUserId, 268435456);
            if (pi == null) {
                Slog.w(TAG, "No content provider found for grant: " + grantUri.toSafeString());
                return;
            }
            if ((modeFlags & 128) != 0) {
                grantUri.prefix = true;
            }
            findOrCreateUriPermission(pi.packageName, targetPkg, targetUid, grantUri).grantModes(modeFlags, owner);
        }
    }

    /* access modifiers changed from: package-private */
    public void grantUriPermissionUncheckedFromIntent(NeededUriGrants needed, UriPermissionOwner owner) {
        if (needed != null) {
            for (int i = 0; i < needed.size(); i++) {
                grantUriPermissionUnchecked(needed.targetUid, needed.targetPkg, (GrantUri) needed.get(i), needed.flags, owner);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void grantUriPermission(int callingUid, String targetPkg, GrantUri grantUri, int modeFlags, UriPermissionOwner owner, int targetUserId) {
        if (targetPkg != null) {
            try {
                int targetUid = checkGrantUriPermission(callingUid, targetPkg, grantUri, modeFlags, AppGlobals.getPackageManager().getPackageUid(targetPkg, 268435456, targetUserId));
                if (targetUid >= 0) {
                    grantUriPermissionUnchecked(targetUid, targetPkg, grantUri, modeFlags, owner);
                }
            } catch (RemoteException e) {
            }
        } else {
            throw new NullPointerException(ATTR_TARGET_PKG);
        }
    }

    /* access modifiers changed from: package-private */
    public void revokeUriPermission(String targetPackage, int callingUid, GrantUri grantUri, int modeFlags) {
        IPackageManager pm = AppGlobals.getPackageManager();
        ProviderInfo pi = getProviderInfo(grantUri.uri.getAuthority(), grantUri.sourceUserId, 786432);
        if (pi == null) {
            Slog.w(TAG, "No content provider found for permission revoke: " + grantUri.toSafeString());
        } else if (!checkHoldingPermissions(pm, pi, grantUri, callingUid, modeFlags)) {
            ArrayMap<GrantUri, UriPermission> perms = this.mGrantedUriPermissions.get(callingUid);
            if (perms != null) {
                boolean persistChanged = false;
                for (int i = perms.size() - 1; i >= 0; i--) {
                    UriPermission perm = perms.valueAt(i);
                    if ((targetPackage == null || targetPackage.equals(perm.targetPkg)) && perm.uri.sourceUserId == grantUri.sourceUserId && perm.uri.uri.isPathPrefixMatch(grantUri.uri)) {
                        persistChanged |= perm.revokeModes(modeFlags | 64, false);
                        if (perm.modeFlags == 0) {
                            perms.removeAt(i);
                        }
                    }
                }
                if (perms.isEmpty()) {
                    this.mGrantedUriPermissions.remove(callingUid);
                }
                if (persistChanged) {
                    schedulePersistUriGrants();
                }
            }
        } else {
            boolean persistChanged2 = false;
            for (int i2 = this.mGrantedUriPermissions.size() - 1; i2 >= 0; i2--) {
                this.mGrantedUriPermissions.keyAt(i2);
                ArrayMap<GrantUri, UriPermission> perms2 = this.mGrantedUriPermissions.valueAt(i2);
                for (int j = perms2.size() - 1; j >= 0; j--) {
                    UriPermission perm2 = perms2.valueAt(j);
                    if ((targetPackage == null || targetPackage.equals(perm2.targetPkg)) && perm2.uri.sourceUserId == grantUri.sourceUserId && perm2.uri.uri.isPathPrefixMatch(grantUri.uri)) {
                        persistChanged2 |= perm2.revokeModes(modeFlags | 64, targetPackage == null);
                        if (perm2.modeFlags == 0) {
                            perms2.removeAt(j);
                        }
                    }
                }
                if (perms2.isEmpty()) {
                    this.mGrantedUriPermissions.removeAt(i2);
                }
            }
            if (persistChanged2) {
                schedulePersistUriGrants();
            }
        }
    }

    private boolean checkHoldingPermissions(IPackageManager pm, ProviderInfo pi, GrantUri grantUri, int uid, int modeFlags) {
        if (UserHandle.getUserId(uid) == grantUri.sourceUserId || ActivityManager.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", uid, -1, true) == 0) {
            return checkHoldingPermissionsInternal(pm, pi, grantUri, uid, modeFlags, true);
        }
        return false;
    }

    private boolean checkHoldingPermissionsInternal(IPackageManager pm, ProviderInfo pi, GrantUri grantUri, int uid, int modeFlags, boolean considerUidPermissions) {
        String ppwperm;
        String pprperm;
        if (pi.applicationInfo.uid == uid) {
            return true;
        }
        if (!pi.exported) {
            return false;
        }
        boolean readMet = (modeFlags & 1) == 0;
        boolean writeMet = (modeFlags & 2) == 0;
        if (!readMet) {
            try {
                if (pi.readPermission != null && considerUidPermissions && pm.checkUidPermission(pi.readPermission, uid) == 0) {
                    readMet = true;
                }
            } catch (RemoteException e) {
                return false;
            }
        }
        if (!writeMet && pi.writePermission != null && considerUidPermissions && pm.checkUidPermission(pi.writePermission, uid) == 0) {
            writeMet = true;
        }
        boolean allowDefaultRead = pi.readPermission == null;
        boolean allowDefaultWrite = pi.writePermission == null;
        PathPermission[] pps = pi.pathPermissions;
        if (pps != null) {
            try {
                String path = grantUri.uri.getPath();
                int i = pps.length;
                while (i > 0 && (!readMet || !writeMet)) {
                    i--;
                    PathPermission pp = pps[i];
                    if (pp.match(path)) {
                        if (!readMet && (pprperm = pp.getReadPermission()) != null) {
                            if (!considerUidPermissions || pm.checkUidPermission(pprperm, uid) != 0) {
                                allowDefaultRead = false;
                            } else {
                                readMet = true;
                            }
                        }
                        if (!writeMet && (ppwperm = pp.getWritePermission()) != null) {
                            if (!considerUidPermissions || pm.checkUidPermission(ppwperm, uid) != 0) {
                                allowDefaultWrite = false;
                            } else {
                                writeMet = true;
                            }
                        }
                    }
                }
            } catch (RemoteException e2) {
                return false;
            }
        }
        if (allowDefaultRead) {
            readMet = true;
        }
        if (allowDefaultWrite) {
            writeMet = true;
        }
        if (!readMet || !writeMet) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeUriPermissionIfNeeded(UriPermission perm) {
        ArrayMap<GrantUri, UriPermission> perms;
        if (perm.modeFlags == 0 && (perms = this.mGrantedUriPermissions.get(perm.targetUid)) != null) {
            perms.remove(perm.uri);
            if (perms.isEmpty()) {
                this.mGrantedUriPermissions.remove(perm.targetUid);
            }
        }
    }

    private UriPermission findUriPermissionLocked(int targetUid, GrantUri grantUri) {
        ArrayMap<GrantUri, UriPermission> targetUris = this.mGrantedUriPermissions.get(targetUid);
        if (targetUris != null) {
            return targetUris.get(grantUri);
        }
        return null;
    }

    private void schedulePersistUriGrants() {
        if (!this.mH.hasMessages(1)) {
            H h = this.mH;
            h.sendMessageDelayed(h.obtainMessage(1), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceNotIsolatedCaller(String caller) {
        if (UserHandle.isIsolated(Binder.getCallingUid())) {
            throw new SecurityException("Isolated process not allowed to call " + caller);
        }
    }

    private ProviderInfo getProviderInfo(String authority, int userHandle, int pmFlags) {
        try {
            return AppGlobals.getPackageManager().resolveContentProvider(authority, pmFlags | 2048, userHandle);
        } catch (RemoteException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x015b  */
    public int checkGrantUriPermission(int callingUid, String targetPkg, GrantUri grantUri, int modeFlags, int lastTargetUid) {
        int targetUid;
        int allowedResult;
        ProviderInfo pi;
        boolean specialCrossUserGrant;
        int N;
        if (!Intent.isAccessUriMode(modeFlags)) {
            return -1;
        }
        IPackageManager pm = AppGlobals.getPackageManager();
        if (!"content".equals(grantUri.uri.getScheme())) {
            return -1;
        }
        int callingAppId = UserHandle.getAppId(callingUid);
        if ((callingAppId == 1000 || callingAppId == 0) && !"com.android.settings.files".equals(grantUri.uri.getAuthority()) && !"com.android.settings.module_licenses".equals(grantUri.uri.getAuthority()) && !EXEMPTED_AUTHORITIES.contains(grantUri.uri.getAuthority())) {
            Slog.w(TAG, "For security reasons, the system cannot issue a Uri permission grant to " + grantUri + "; use startActivityAsCaller() instead");
            return -1;
        }
        ProviderInfo pi2 = getProviderInfo(grantUri.uri.getAuthority(), grantUri.sourceUserId, 268435456);
        if (pi2 == null) {
            Slog.w(TAG, "No content provider found for permission check: " + grantUri.uri.toSafeString());
            return -1;
        }
        if (lastTargetUid >= 0 || targetPkg == null) {
            targetUid = lastTargetUid;
        } else {
            try {
                targetUid = pm.getPackageUid(targetPkg, 268435456, UserHandle.getUserId(callingUid));
                if (targetUid < 0) {
                    return -1;
                }
            } catch (RemoteException e) {
                return -1;
            }
        }
        if ((modeFlags & 64) != 0 || pi2.forceUriPermissions) {
            allowedResult = targetUid;
        } else {
            allowedResult = -1;
        }
        if (targetUid < 0) {
            boolean allowed = pi2.exported;
            if (!((modeFlags & 1) == 0 || pi2.readPermission == null)) {
                allowed = false;
            }
            if (!((modeFlags & 2) == 0 || pi2.writePermission == null)) {
                allowed = false;
            }
            if (pi2.pathPermissions != null) {
                int N2 = pi2.pathPermissions.length;
                int i = 0;
                while (true) {
                    if (i >= N2) {
                        break;
                    } else if (pi2.pathPermissions[i] == null || !pi2.pathPermissions[i].match(grantUri.uri.getPath())) {
                        i++;
                    } else {
                        if (!((modeFlags & 1) == 0 || pi2.pathPermissions[i].getReadPermission() == null)) {
                            allowed = false;
                        }
                        if ((modeFlags & 2) != 0 && pi2.pathPermissions[i].getWritePermission() != null) {
                            allowed = false;
                        }
                    }
                }
            }
            if (allowed) {
                return allowedResult;
            }
        } else if (checkHoldingPermissions(pm, pi2, grantUri, targetUid, modeFlags)) {
            return allowedResult;
        }
        if (targetUid < 0) {
            pi = pi2;
        } else if (UserHandle.getUserId(targetUid) != grantUri.sourceUserId) {
            pi = pi2;
            if (checkHoldingPermissionsInternal(pm, pi2, grantUri, callingUid, modeFlags, false)) {
                specialCrossUserGrant = true;
                if (!specialCrossUserGrant) {
                    if (!pi.grantUriPermissions) {
                        throw new SecurityException("Provider " + pi.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + pi.name + " does not allow granting of Uri permissions (uri " + grantUri + ")");
                    } else if (pi.uriPermissionPatterns != null) {
                        int N3 = pi.uriPermissionPatterns.length;
                        boolean allowed2 = false;
                        int i2 = 0;
                        while (true) {
                            if (i2 >= N3) {
                                break;
                            }
                            if (pi.uriPermissionPatterns[i2] != null) {
                                N = N3;
                                if (pi.uriPermissionPatterns[i2].match(grantUri.uri.getPath())) {
                                    allowed2 = true;
                                    break;
                                }
                            } else {
                                N = N3;
                            }
                            i2++;
                            N3 = N;
                        }
                        if (!allowed2) {
                            throw new SecurityException("Provider " + pi.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + pi.name + " does not allow granting of permission to path of Uri " + grantUri);
                        }
                    }
                }
                if (!checkHoldingPermissions(pm, pi, grantUri, callingUid, modeFlags) || checkUriPermission(grantUri, callingUid, modeFlags)) {
                    return targetUid;
                }
                if ("android.permission.MANAGE_DOCUMENTS".equals(pi.readPermission)) {
                    throw new SecurityException("UID " + callingUid + " does not have permission to " + grantUri + "; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs");
                }
                throw new SecurityException("UID " + callingUid + " does not have permission to " + grantUri);
            }
        } else {
            pi = pi2;
        }
        specialCrossUserGrant = false;
        if (!specialCrossUserGrant) {
        }
        if (!checkHoldingPermissions(pm, pi, grantUri, callingUid, modeFlags)) {
        }
        return targetUid;
    }

    /* access modifiers changed from: package-private */
    public int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri, int modeFlags, int userId) {
        return checkGrantUriPermission(callingUid, targetPkg, new GrantUri(userId, uri, false), modeFlags, -1);
    }

    /* access modifiers changed from: package-private */
    public boolean checkUriPermission(GrantUri grantUri, int uid, int modeFlags) {
        int userId;
        int formatUid;
        int minStrength = (modeFlags & 64) != 0 ? 3 : 1;
        if (uid == 0) {
            return true;
        }
        ArrayMap<GrantUri, UriPermission> perms = this.mGrantedUriPermissions.get(uid);
        if (!(perms != null || this.mAmInternal == null || grantUri == null || grantUri.uri == null || (userId = UserHandle.getUserId(uid)) == 0 || (formatUid = UserHandle.getUid(this.mAmInternal.handleUserForClone(grantUri.uri.getAuthority(), userId), uid)) == uid)) {
            perms = this.mGrantedUriPermissions.get(formatUid);
        }
        if (perms == null) {
            return false;
        }
        UriPermission exactPerm = perms.get(grantUri);
        if (exactPerm != null && exactPerm.getStrength(modeFlags) >= minStrength) {
            return true;
        }
        int N = perms.size();
        for (int i = 0; i < N; i++) {
            UriPermission perm = perms.valueAt(i);
            if (perm.uri.prefix && grantUri.uri.isPathPrefixMatch(perm.uri.uri) && perm.getStrength(modeFlags) >= minStrength) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeGrantedUriPermissions() {
        long startTime = SystemClock.uptimeMillis();
        ArrayList<UriPermission.Snapshot> persist = Lists.newArrayList();
        synchronized (this) {
            int size = this.mGrantedUriPermissions.size();
            for (int i = 0; i < size; i++) {
                for (UriPermission perm : this.mGrantedUriPermissions.valueAt(i).values()) {
                    if (perm.persistedModeFlags != 0) {
                        persist.add(perm.snapshot());
                    }
                }
            }
        }
        try {
            FileOutputStream fos = this.mGrantFile.startWrite(startTime);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, TAG_URI_GRANTS);
            Iterator<UriPermission.Snapshot> it = persist.iterator();
            while (it.hasNext()) {
                UriPermission.Snapshot perm2 = it.next();
                out.startTag(null, TAG_URI_GRANT);
                XmlUtils.writeIntAttribute(out, ATTR_SOURCE_USER_ID, perm2.uri.sourceUserId);
                XmlUtils.writeIntAttribute(out, ATTR_TARGET_USER_ID, perm2.targetUserId);
                out.attribute(null, ATTR_SOURCE_PKG, perm2.sourcePkg);
                out.attribute(null, ATTR_TARGET_PKG, perm2.targetPkg);
                out.attribute(null, ATTR_URI, String.valueOf(perm2.uri.uri));
                XmlUtils.writeBooleanAttribute(out, ATTR_PREFIX, perm2.uri.prefix);
                XmlUtils.writeIntAttribute(out, ATTR_MODE_FLAGS, perm2.persistedModeFlags);
                XmlUtils.writeLongAttribute(out, ATTR_CREATED_TIME, perm2.persistedCreateTime);
                out.endTag(null, TAG_URI_GRANT);
            }
            out.endTag(null, TAG_URI_GRANTS);
            out.endDocument();
            this.mGrantFile.finishWrite(fos);
        } catch (IOException e) {
            if (0 != 0) {
                this.mGrantFile.failWrite(null);
            }
        }
    }

    private PackageManagerInternal getPmInternal() {
        if (this.mPmInternal == null) {
            this.mPmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        return this.mPmInternal;
    }

    /* access modifiers changed from: package-private */
    public final class H extends Handler {
        static final int PERSIST_URI_GRANTS_MSG = 1;

        public H(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                UriGrantsManagerService.this.writeGrantedUriPermissions();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class LocalService implements UriGrantsManagerInternal {
        LocalService() {
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void removeUriPermissionIfNeeded(UriPermission perm) {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.removeUriPermissionIfNeeded(perm);
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void grantUriPermission(int callingUid, String targetPkg, GrantUri grantUri, int modeFlags, UriPermissionOwner owner, int targetUserId) {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.grantUriPermission(callingUid, targetPkg, grantUri, modeFlags, owner, targetUserId);
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void revokeUriPermission(String targetPackage, int callingUid, GrantUri grantUri, int modeFlags) {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.revokeUriPermission(targetPackage, callingUid, grantUri, modeFlags);
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public boolean checkUriPermission(GrantUri grantUri, int uid, int modeFlags) {
            boolean checkUriPermission;
            synchronized (UriGrantsManagerService.this.mLock) {
                checkUriPermission = UriGrantsManagerService.this.checkUriPermission(grantUri, uid, modeFlags);
            }
            return checkUriPermission;
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public int checkGrantUriPermission(int callingUid, String targetPkg, GrantUri uri, int modeFlags, int userId) {
            int checkGrantUriPermission;
            synchronized (UriGrantsManagerService.this.mLock) {
                checkGrantUriPermission = UriGrantsManagerService.this.checkGrantUriPermission(callingUid, targetPkg, uri, modeFlags, userId);
            }
            return checkGrantUriPermission;
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri, int modeFlags, int userId) {
            int checkGrantUriPermission;
            UriGrantsManagerService.this.enforceNotIsolatedCaller("checkGrantUriPermission");
            synchronized (UriGrantsManagerService.this.mLock) {
                checkGrantUriPermission = UriGrantsManagerService.this.checkGrantUriPermission(callingUid, targetPkg, uri, modeFlags, userId);
            }
            return checkGrantUriPermission;
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public NeededUriGrants checkGrantUriPermissionFromIntent(int callingUid, String targetPkg, Intent intent, int mode, NeededUriGrants needed, int targetUserId) {
            NeededUriGrants checkGrantUriPermissionFromIntent;
            synchronized (UriGrantsManagerService.this.mLock) {
                checkGrantUriPermissionFromIntent = UriGrantsManagerService.this.checkGrantUriPermissionFromIntent(callingUid, targetPkg, intent, mode, needed, targetUserId);
            }
            return checkGrantUriPermissionFromIntent;
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void grantUriPermissionFromIntent(int callingUid, String targetPkg, Intent intent, int targetUserId) {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.grantUriPermissionFromIntent(callingUid, targetPkg, intent, null, targetUserId);
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void grantUriPermissionFromIntent(int callingUid, String targetPkg, Intent intent, UriPermissionOwner owner, int targetUserId) {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.grantUriPermissionFromIntent(callingUid, targetPkg, intent, owner, targetUserId);
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void grantUriPermissionUncheckedFromIntent(NeededUriGrants needed, UriPermissionOwner owner) {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.grantUriPermissionUncheckedFromIntent(needed, owner);
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void onSystemReady() {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.readGrantedUriPermissions();
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void onActivityManagerInternalAdded() {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.onActivityManagerInternalAdded();
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public IBinder newUriPermissionOwner(String name) {
            Binder externalToken;
            UriGrantsManagerService.this.enforceNotIsolatedCaller("newUriPermissionOwner");
            synchronized (UriGrantsManagerService.this.mLock) {
                externalToken = new UriPermissionOwner(this, name).getExternalToken();
            }
            return externalToken;
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void removeUriPermissionsForPackage(String packageName, int userHandle, boolean persistable, boolean targetOnly) {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriGrantsManagerService.this.removeUriPermissionsForPackage(packageName, userHandle, persistable, targetOnly);
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void revokeUriPermissionFromOwner(IBinder token, Uri uri, int mode, int userId) {
            synchronized (UriGrantsManagerService.this.mLock) {
                UriPermissionOwner owner = UriPermissionOwner.fromExternalToken(token);
                if (owner == null) {
                    throw new IllegalArgumentException("Unknown owner: " + token);
                } else if (uri == null) {
                    owner.removeUriPermissions(mode);
                } else {
                    owner.removeUriPermission(new GrantUri(userId, uri, (mode & 128) != 0), mode);
                }
            }
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public boolean checkAuthorityGrants(int callingUid, ProviderInfo cpi, int userId, boolean checkUser) {
            boolean checkAuthorityGrants;
            synchronized (UriGrantsManagerService.this.mLock) {
                checkAuthorityGrants = UriGrantsManagerService.this.checkAuthorityGrants(callingUid, cpi, userId, checkUser);
            }
            return checkAuthorityGrants;
        }

        @Override // com.android.server.uri.UriGrantsManagerInternal
        public void dump(PrintWriter pw, boolean dumpAll, String dumpPackage) {
            synchronized (UriGrantsManagerService.this.mLock) {
                boolean needSep = false;
                boolean printedAnything = false;
                if (UriGrantsManagerService.this.mGrantedUriPermissions.size() > 0) {
                    boolean printed = false;
                    int dumpUid = -2;
                    if (dumpPackage != null) {
                        try {
                            dumpUid = UriGrantsManagerService.this.mContext.getPackageManager().getPackageUidAsUser(dumpPackage, DumpState.DUMP_CHANGES, 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            dumpUid = -1;
                        }
                    }
                    for (int i = 0; i < UriGrantsManagerService.this.mGrantedUriPermissions.size(); i++) {
                        int uid = UriGrantsManagerService.this.mGrantedUriPermissions.keyAt(i);
                        if (dumpUid < -1 || UserHandle.getAppId(uid) == dumpUid) {
                            ArrayMap<GrantUri, UriPermission> perms = (ArrayMap) UriGrantsManagerService.this.mGrantedUriPermissions.valueAt(i);
                            if (!printed) {
                                if (needSep) {
                                    pw.println();
                                }
                                needSep = true;
                                pw.println("  Granted Uri Permissions:");
                                printed = true;
                                printedAnything = true;
                            }
                            pw.print("  * UID ");
                            pw.print(uid);
                            pw.println(" holds:");
                            for (UriPermission perm : perms.values()) {
                                pw.print("    ");
                                pw.println(perm);
                                if (dumpAll) {
                                    perm.dump(pw, "      ");
                                }
                            }
                        }
                    }
                }
                if (!printedAnything) {
                    pw.println("  (nothing)");
                }
            }
        }
    }
}
