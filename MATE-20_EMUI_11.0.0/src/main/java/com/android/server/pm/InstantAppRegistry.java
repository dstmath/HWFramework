package com.android.server.pm;

import android.content.Intent;
import android.content.pm.InstantAppInfo;
import android.content.pm.PackageParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.ByteStringUtils;
import android.util.PackageUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.Xml;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.InstantAppRegistry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class InstantAppRegistry {
    private static final String ATTR_GRANTED = "granted";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_NAME = "name";
    private static final boolean DEBUG = false;
    private static final long DEFAULT_INSTALLED_INSTANT_APP_MAX_CACHE_PERIOD = 15552000000L;
    static final long DEFAULT_INSTALLED_INSTANT_APP_MIN_CACHE_PERIOD = 604800000;
    private static final long DEFAULT_UNINSTALLED_INSTANT_APP_MAX_CACHE_PERIOD = 15552000000L;
    static final long DEFAULT_UNINSTALLED_INSTANT_APP_MIN_CACHE_PERIOD = 604800000;
    private static final String INSTANT_APPS_FOLDER = "instant";
    private static final String INSTANT_APP_ANDROID_ID_FILE = "android_id";
    private static final String INSTANT_APP_COOKIE_FILE_PREFIX = "cookie_";
    private static final String INSTANT_APP_COOKIE_FILE_SIFFIX = ".dat";
    private static final String INSTANT_APP_ICON_FILE = "icon.png";
    private static final String INSTANT_APP_METADATA_FILE = "metadata.xml";
    private static final String LOG_TAG = "InstantAppRegistry";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PERMISSION = "permission";
    private static final String TAG_PERMISSIONS = "permissions";
    private final CookiePersistence mCookiePersistence = new CookiePersistence(BackgroundThread.getHandler().getLooper());
    private SparseArray<SparseBooleanArray> mInstalledInstantAppUids;
    private SparseArray<SparseArray<SparseBooleanArray>> mInstantGrants;
    private final PackageManagerService mService;
    private SparseArray<List<UninstalledInstantAppState>> mUninstalledInstantApps;

    public InstantAppRegistry(PackageManagerService service) {
        this.mService = service;
    }

    public byte[] getInstantAppCookieLPw(String packageName, int userId) {
        PackageParser.Package pkg = this.mService.mPackages.get(packageName);
        if (pkg == null) {
            return null;
        }
        byte[] pendingCookie = this.mCookiePersistence.getPendingPersistCookieLPr(pkg, userId);
        if (pendingCookie != null) {
            return pendingCookie;
        }
        File cookieFile = peekInstantCookieFile(packageName, userId);
        if (cookieFile != null && cookieFile.exists()) {
            try {
                return IoUtils.readFileAsByteArray(cookieFile.toString());
            } catch (IOException e) {
                Slog.w(LOG_TAG, "Error reading cookie file: " + cookieFile);
            }
        }
        return null;
    }

    public boolean setInstantAppCookieLPw(String packageName, byte[] cookie, int userId) {
        int maxCookieSize;
        if (cookie == null || cookie.length <= 0 || cookie.length <= (maxCookieSize = this.mService.mContext.getPackageManager().getInstantAppCookieMaxBytes())) {
            PackageParser.Package pkg = this.mService.mPackages.get(packageName);
            if (pkg == null) {
                return false;
            }
            this.mCookiePersistence.schedulePersistLPw(userId, pkg, cookie);
            return true;
        }
        Slog.e(LOG_TAG, "Instant app cookie for package " + packageName + " size " + cookie.length + " bytes while max size is " + maxCookieSize);
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0049, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x004a, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004d, code lost:
        throw r2;
     */
    private void persistInstantApplicationCookie(byte[] cookie, String packageName, File cookieFile, int userId) {
        synchronized (this.mService.mPackages) {
            File appDir = getInstantApplicationDir(packageName, userId);
            if (appDir.exists() || appDir.mkdirs()) {
                if (cookieFile.exists() && !cookieFile.delete()) {
                    Slog.e(LOG_TAG, "Cannot delete instant app cookie file");
                }
                if (cookie != null) {
                    if (cookie.length > 0) {
                        try {
                            FileOutputStream fos = new FileOutputStream(cookieFile);
                            fos.write(cookie, 0, cookie.length);
                            $closeResource(null, fos);
                            return;
                        } catch (IOException e) {
                            Slog.e(LOG_TAG, "Error writing instant app cookie file: " + cookieFile, e);
                            return;
                        }
                    }
                }
                return;
            }
            Slog.e(LOG_TAG, "Cannot create instant app cookie directory");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public Bitmap getInstantAppIconLPw(String packageName, int userId) {
        File iconFile = new File(getInstantApplicationDir(packageName, userId), INSTANT_APP_ICON_FILE);
        if (iconFile.exists()) {
            return BitmapFactory.decodeFile(iconFile.toString());
        }
        return null;
    }

    public String getInstantAppAndroidIdLPw(String packageName, int userId) {
        File idFile = new File(getInstantApplicationDir(packageName, userId), INSTANT_APP_ANDROID_ID_FILE);
        if (idFile.exists()) {
            try {
                return IoUtils.readFileAsString(idFile.getAbsolutePath());
            } catch (IOException e) {
                Slog.e(LOG_TAG, "Failed to read instant app android id file: " + idFile, e);
            }
        }
        return generateInstantAppAndroidIdLPw(packageName, userId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004c, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004d, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0050, code lost:
        throw r7;
     */
    private String generateInstantAppAndroidIdLPw(String packageName, int userId) {
        byte[] randomBytes = new byte[8];
        new SecureRandom().nextBytes(randomBytes);
        String id = ByteStringUtils.toHexString(randomBytes).toLowerCase(Locale.US);
        File appDir = getInstantApplicationDir(packageName, userId);
        if (appDir.exists() || appDir.mkdirs()) {
            File idFile = new File(getInstantApplicationDir(packageName, userId), INSTANT_APP_ANDROID_ID_FILE);
            try {
                FileOutputStream fos = new FileOutputStream(idFile);
                fos.write(id.getBytes());
                $closeResource(null, fos);
            } catch (IOException e) {
                Slog.e(LOG_TAG, "Error writing instant app android id file: " + idFile, e);
            }
            return id;
        }
        Slog.e(LOG_TAG, "Cannot create instant app cookie directory");
        return id;
    }

    public List<InstantAppInfo> getInstantAppsLPr(int userId) {
        List<InstantAppInfo> installedApps = getInstalledInstantApplicationsLPr(userId);
        List<InstantAppInfo> uninstalledApps = getUninstalledInstantApplicationsLPr(userId);
        if (installedApps == null) {
            return uninstalledApps;
        }
        if (uninstalledApps != null) {
            installedApps.addAll(uninstalledApps);
        }
        return installedApps;
    }

    public void onPackageInstalledLPw(PackageParser.Package pkg, int[] userIds) {
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (ps != null) {
            for (int userId : userIds) {
                if (this.mService.mPackages.get(pkg.packageName) != null && ps.getInstalled(userId)) {
                    propagateInstantAppPermissionsIfNeeded(pkg, userId);
                    if (ps.getInstantApp(userId)) {
                        addInstantAppLPw(userId, ps.appId);
                    }
                    removeUninstalledInstantAppStateLPw(new Predicate(pkg) {
                        /* class com.android.server.pm.$$Lambda$InstantAppRegistry$oQxi7GaamyhhMKIMWv499oME */
                        private final /* synthetic */ PackageParser.Package f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Predicate
                        public final boolean test(Object obj) {
                            return InstantAppRegistry.lambda$onPackageInstalledLPw$0(this.f$0, (InstantAppRegistry.UninstalledInstantAppState) obj);
                        }
                    }, userId);
                    File instantAppDir = getInstantApplicationDir(pkg.packageName, userId);
                    new File(instantAppDir, INSTANT_APP_METADATA_FILE).delete();
                    new File(instantAppDir, INSTANT_APP_ICON_FILE).delete();
                    File currentCookieFile = peekInstantCookieFile(pkg.packageName, userId);
                    if (currentCookieFile == null) {
                        continue;
                    } else {
                        String cookieName = currentCookieFile.getName();
                        String currentCookieSha256 = cookieName.substring(INSTANT_APP_COOKIE_FILE_PREFIX.length(), cookieName.length() - INSTANT_APP_COOKIE_FILE_SIFFIX.length());
                        if (!pkg.mSigningDetails.checkCapability(currentCookieSha256, 1)) {
                            for (String s : PackageUtils.computeSignaturesSha256Digests(pkg.mSigningDetails.signatures)) {
                                if (s.equals(currentCookieSha256)) {
                                    return;
                                }
                            }
                            Slog.i(LOG_TAG, "Signature for package " + pkg.packageName + " changed - dropping cookie");
                            this.mCookiePersistence.cancelPendingPersistLPw(pkg, userId);
                            currentCookieFile.delete();
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    public void onPackageUninstalledLPw(PackageParser.Package pkg, int[] userIds) {
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (!(userIds == null || ps == null)) {
            for (int userId : userIds) {
                if (this.mService.mPackages.get(pkg.packageName) == null || !ps.getInstalled(userId)) {
                    if (ps.getInstantApp(userId)) {
                        addUninstalledInstantAppLPw(pkg, userId);
                        removeInstantAppLPw(userId, ps.appId);
                    } else {
                        deleteDir(getInstantApplicationDir(pkg.packageName, userId));
                        this.mCookiePersistence.cancelPendingPersistLPw(pkg, userId);
                        removeAppLPw(userId, ps.appId);
                    }
                }
            }
        }
    }

    public void onUserRemovedLPw(int userId) {
        SparseArray<List<UninstalledInstantAppState>> sparseArray = this.mUninstalledInstantApps;
        if (sparseArray != null) {
            sparseArray.remove(userId);
            if (this.mUninstalledInstantApps.size() <= 0) {
                this.mUninstalledInstantApps = null;
            }
        }
        SparseArray<SparseBooleanArray> sparseArray2 = this.mInstalledInstantAppUids;
        if (sparseArray2 != null) {
            sparseArray2.remove(userId);
            if (this.mInstalledInstantAppUids.size() <= 0) {
                this.mInstalledInstantAppUids = null;
            }
        }
        SparseArray<SparseArray<SparseBooleanArray>> sparseArray3 = this.mInstantGrants;
        if (sparseArray3 != null) {
            sparseArray3.remove(userId);
            if (this.mInstantGrants.size() <= 0) {
                this.mInstantGrants = null;
            }
        }
        deleteDir(getInstantApplicationsDir(userId));
    }

    public boolean isInstantAccessGranted(int userId, int targetAppId, int instantAppId) {
        SparseArray<SparseBooleanArray> targetAppList;
        SparseBooleanArray instantGrantList;
        SparseArray<SparseArray<SparseBooleanArray>> sparseArray = this.mInstantGrants;
        if (sparseArray == null || (targetAppList = sparseArray.get(userId)) == null || (instantGrantList = targetAppList.get(targetAppId)) == null) {
            return false;
        }
        return instantGrantList.get(instantAppId);
    }

    public void grantInstantAccessLPw(int userId, Intent intent, int targetAppId, int instantAppId) {
        SparseBooleanArray instantAppList;
        Set<String> categories;
        SparseArray<SparseBooleanArray> sparseArray = this.mInstalledInstantAppUids;
        if (sparseArray == null || (instantAppList = sparseArray.get(userId)) == null || !instantAppList.get(instantAppId) || instantAppList.get(targetAppId)) {
            return;
        }
        if (intent == null || !"android.intent.action.VIEW".equals(intent.getAction()) || (categories = intent.getCategories()) == null || !categories.contains("android.intent.category.BROWSABLE")) {
            if (this.mInstantGrants == null) {
                this.mInstantGrants = new SparseArray<>();
            }
            SparseArray<SparseBooleanArray> targetAppList = this.mInstantGrants.get(userId);
            if (targetAppList == null) {
                targetAppList = new SparseArray<>();
                this.mInstantGrants.put(userId, targetAppList);
            }
            SparseBooleanArray instantGrantList = targetAppList.get(targetAppId);
            if (instantGrantList == null) {
                instantGrantList = new SparseBooleanArray();
                targetAppList.put(targetAppId, instantGrantList);
            }
            instantGrantList.put(instantAppId, true);
        }
    }

    public void addInstantAppLPw(int userId, int instantAppId) {
        if (this.mInstalledInstantAppUids == null) {
            this.mInstalledInstantAppUids = new SparseArray<>();
        }
        SparseBooleanArray instantAppList = this.mInstalledInstantAppUids.get(userId);
        if (instantAppList == null) {
            instantAppList = new SparseBooleanArray();
            this.mInstalledInstantAppUids.put(userId, instantAppList);
        }
        instantAppList.put(instantAppId, true);
    }

    private void removeInstantAppLPw(int userId, int instantAppId) {
        SparseBooleanArray instantAppList;
        SparseArray<SparseBooleanArray> targetAppList;
        SparseArray<SparseBooleanArray> sparseArray = this.mInstalledInstantAppUids;
        if (!(sparseArray == null || (instantAppList = sparseArray.get(userId)) == null)) {
            instantAppList.delete(instantAppId);
            SparseArray<SparseArray<SparseBooleanArray>> sparseArray2 = this.mInstantGrants;
            if (!(sparseArray2 == null || (targetAppList = sparseArray2.get(userId)) == null)) {
                for (int i = targetAppList.size() - 1; i >= 0; i--) {
                    targetAppList.valueAt(i).delete(instantAppId);
                }
            }
        }
    }

    private void removeAppLPw(int userId, int targetAppId) {
        SparseArray<SparseBooleanArray> targetAppList;
        SparseArray<SparseArray<SparseBooleanArray>> sparseArray = this.mInstantGrants;
        if (sparseArray != null && (targetAppList = sparseArray.get(userId)) != null) {
            targetAppList.delete(targetAppId);
        }
    }

    private void addUninstalledInstantAppLPw(PackageParser.Package pkg, int userId) {
        InstantAppInfo uninstalledApp = createInstantAppInfoForPackage(pkg, userId, false);
        if (uninstalledApp != null) {
            if (this.mUninstalledInstantApps == null) {
                this.mUninstalledInstantApps = new SparseArray<>();
            }
            List<UninstalledInstantAppState> uninstalledAppStates = this.mUninstalledInstantApps.get(userId);
            if (uninstalledAppStates == null) {
                uninstalledAppStates = new ArrayList();
                this.mUninstalledInstantApps.put(userId, uninstalledAppStates);
            }
            uninstalledAppStates.add(new UninstalledInstantAppState(uninstalledApp, System.currentTimeMillis()));
            writeUninstalledInstantAppMetadata(uninstalledApp, userId);
            writeInstantApplicationIconLPw(pkg, userId);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x006a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x006b, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006e, code lost:
        throw r6;
     */
    private void writeInstantApplicationIconLPw(PackageParser.Package pkg, int userId) {
        Bitmap bitmap;
        if (getInstantApplicationDir(pkg.packageName, userId).exists()) {
            Drawable icon = pkg.applicationInfo.loadIcon(this.mService.mContext.getPackageManager());
            if (icon instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) icon).getBitmap();
            } else {
                bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                icon.draw(canvas);
            }
            try {
                FileOutputStream out = new FileOutputStream(new File(getInstantApplicationDir(pkg.packageName, userId), INSTANT_APP_ICON_FILE));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                $closeResource(null, out);
            } catch (Exception e) {
                Slog.e(LOG_TAG, "Error writing instant app icon", e);
            }
        }
    }

    public boolean hasInstantApplicationMetadataLPr(String packageName, int userId) {
        if (packageName == null) {
            return false;
        }
        if (hasUninstalledInstantAppStateLPr(packageName, userId) || hasInstantAppMetadataLPr(packageName, userId)) {
            return true;
        }
        return false;
    }

    public void deleteInstantApplicationMetadataLPw(String packageName, int userId) {
        if (packageName != null) {
            removeUninstalledInstantAppStateLPw(new Predicate(packageName) {
                /* class com.android.server.pm.$$Lambda$InstantAppRegistry$eaYsiecM_Rq6dliDvliwVtj695o */
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return InstantAppRegistry.lambda$deleteInstantApplicationMetadataLPw$1(this.f$0, (InstantAppRegistry.UninstalledInstantAppState) obj);
                }
            }, userId);
            File instantAppDir = getInstantApplicationDir(packageName, userId);
            new File(instantAppDir, INSTANT_APP_METADATA_FILE).delete();
            new File(instantAppDir, INSTANT_APP_ICON_FILE).delete();
            new File(instantAppDir, INSTANT_APP_ANDROID_ID_FILE).delete();
            File cookie = peekInstantCookieFile(packageName, userId);
            if (cookie != null) {
                cookie.delete();
            }
        }
    }

    private void removeUninstalledInstantAppStateLPw(Predicate<UninstalledInstantAppState> criteria, int userId) {
        List<UninstalledInstantAppState> uninstalledAppStates;
        SparseArray<List<UninstalledInstantAppState>> sparseArray = this.mUninstalledInstantApps;
        if (!(sparseArray == null || (uninstalledAppStates = sparseArray.get(userId)) == null)) {
            for (int i = uninstalledAppStates.size() - 1; i >= 0; i--) {
                if (criteria.test(uninstalledAppStates.get(i))) {
                    uninstalledAppStates.remove(i);
                    if (uninstalledAppStates.isEmpty()) {
                        this.mUninstalledInstantApps.remove(userId);
                        if (this.mUninstalledInstantApps.size() <= 0) {
                            this.mUninstalledInstantApps = null;
                            return;
                        }
                        return;
                    }
                }
            }
        }
    }

    private boolean hasUninstalledInstantAppStateLPr(String packageName, int userId) {
        List<UninstalledInstantAppState> uninstalledAppStates;
        SparseArray<List<UninstalledInstantAppState>> sparseArray = this.mUninstalledInstantApps;
        if (sparseArray == null || (uninstalledAppStates = sparseArray.get(userId)) == null) {
            return false;
        }
        int appCount = uninstalledAppStates.size();
        for (int i = 0; i < appCount; i++) {
            if (packageName.equals(uninstalledAppStates.get(i).mInstantAppInfo.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInstantAppMetadataLPr(String packageName, int userId) {
        File instantAppDir = getInstantApplicationDir(packageName, userId);
        return new File(instantAppDir, INSTANT_APP_METADATA_FILE).exists() || new File(instantAppDir, INSTANT_APP_ICON_FILE).exists() || new File(instantAppDir, INSTANT_APP_ANDROID_ID_FILE).exists() || peekInstantCookieFile(packageName, userId) != null;
    }

    public void pruneInstantApps() {
        try {
            pruneInstantApps(JobStatus.NO_LATEST_RUNTIME, Settings.Global.getLong(this.mService.mContext.getContentResolver(), "installed_instant_app_max_cache_period", 15552000000L), Settings.Global.getLong(this.mService.mContext.getContentResolver(), "uninstalled_instant_app_max_cache_period", 15552000000L));
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning installed and uninstalled instant apps", e);
        }
    }

    public boolean pruneInstalledInstantApps(long neededSpace, long maxInstalledCacheDuration) {
        try {
            return pruneInstantApps(neededSpace, maxInstalledCacheDuration, JobStatus.NO_LATEST_RUNTIME);
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning installed instant apps", e);
            return false;
        }
    }

    public boolean pruneUninstalledInstantApps(long neededSpace, long maxUninstalledCacheDuration) {
        try {
            return pruneInstantApps(neededSpace, JobStatus.NO_LATEST_RUNTIME, maxUninstalledCacheDuration);
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning uninstalled instant apps", e);
            return false;
        }
    }

    private boolean pruneInstantApps(long neededSpace, long maxInstalledCacheDuration, long maxUninstalledCacheDuration) {
        Throwable th;
        List<String> packagesToDelete;
        StorageManager storage;
        int[] iArr;
        List<String> packagesToDelete2;
        StorageManager storage2;
        int i;
        int packageCount;
        InstantAppRegistry instantAppRegistry = this;
        StorageManager storage3 = (StorageManager) instantAppRegistry.mService.mContext.getSystemService(StorageManager.class);
        File file = storage3.findPathForUuid(StorageManager.UUID_PRIVATE_INTERNAL);
        if (file.getUsableSpace() >= neededSpace) {
            return true;
        }
        List<String> packagesToDelete3 = null;
        long now = System.currentTimeMillis();
        synchronized (instantAppRegistry.mService.mPackages) {
            try {
                int[] allUsers = PackageManagerService.sUserManager.getUserIds();
                int packageCount2 = instantAppRegistry.mService.mPackages.size();
                int i2 = 0;
                while (i2 < packageCount2) {
                    try {
                        PackageParser.Package pkg = instantAppRegistry.mService.mPackages.valueAt(i2);
                        if (now - pkg.getLatestPackageUseTimeInMills() < maxInstalledCacheDuration) {
                            packageCount = packageCount2;
                        } else if (!(pkg.mExtras instanceof PackageSetting)) {
                            packageCount = packageCount2;
                        } else {
                            PackageSetting ps = (PackageSetting) pkg.mExtras;
                            boolean installedOnlyAsInstantApp = false;
                            int length = allUsers.length;
                            int i3 = 0;
                            while (true) {
                                if (i3 >= length) {
                                    packageCount = packageCount2;
                                    break;
                                }
                                int userId = allUsers[i3];
                                packageCount = packageCount2;
                                if (ps.getInstalled(userId)) {
                                    if (!ps.getInstantApp(userId)) {
                                        installedOnlyAsInstantApp = false;
                                        break;
                                    }
                                    installedOnlyAsInstantApp = true;
                                }
                                i3++;
                                packageCount2 = packageCount;
                            }
                            if (installedOnlyAsInstantApp) {
                                if (packagesToDelete3 == null) {
                                    packagesToDelete3 = new ArrayList<>();
                                }
                                packagesToDelete3.add(pkg.packageName);
                            }
                        }
                        i2++;
                        packageCount2 = packageCount;
                    } catch (Throwable th2) {
                        th = th2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        throw th;
                    }
                }
                if (packagesToDelete3 != null) {
                    packagesToDelete3.sort(new Comparator() {
                        /* class com.android.server.pm.$$Lambda$InstantAppRegistry$UOn4sUy4zBQuofxUbY8RBYhkNSE */

                        @Override // java.util.Comparator
                        public final int compare(Object obj, Object obj2) {
                            return InstantAppRegistry.this.lambda$pruneInstantApps$2$InstantAppRegistry((String) obj, (String) obj2);
                        }
                    });
                }
                try {
                } catch (Throwable th4) {
                    th = th4;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                while (true) {
                    break;
                }
                throw th;
            }
        }
        if (packagesToDelete3 != null) {
            int packageCount3 = packagesToDelete3.size();
            for (int i4 = 0; i4 < packageCount3; i4++) {
                if (instantAppRegistry.mService.deletePackageX(packagesToDelete3.get(i4), -1, 0, 2) == 1 && file.getUsableSpace() >= neededSpace) {
                    return true;
                }
            }
        }
        synchronized (instantAppRegistry.mService.mPackages) {
            try {
                int[] userIds = UserManagerService.getInstance().getUserIds();
                int length2 = userIds.length;
                int i5 = 0;
                while (i5 < length2) {
                    int userId2 = userIds[i5];
                    instantAppRegistry.removeUninstalledInstantAppStateLPw(new Predicate(maxUninstalledCacheDuration) {
                        /* class com.android.server.pm.$$Lambda$InstantAppRegistry$BuKCbLr_MGBazMPl54pWTuGHYY */
                        private final /* synthetic */ long f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Predicate
                        public final boolean test(Object obj) {
                            return InstantAppRegistry.lambda$pruneInstantApps$3(this.f$0, (InstantAppRegistry.UninstalledInstantAppState) obj);
                        }
                    }, userId2);
                    File instantAppsDir = getInstantApplicationsDir(userId2);
                    if (!instantAppsDir.exists()) {
                        iArr = userIds;
                        storage = storage3;
                        packagesToDelete = packagesToDelete3;
                    } else {
                        File[] files = instantAppsDir.listFiles();
                        if (files == null) {
                            iArr = userIds;
                            storage = storage3;
                            packagesToDelete = packagesToDelete3;
                        } else {
                            iArr = userIds;
                            int length3 = files.length;
                            int i6 = 0;
                            while (i6 < length3) {
                                File instantDir = files[i6];
                                if (!instantDir.isDirectory()) {
                                    i = length3;
                                    storage2 = storage3;
                                    packagesToDelete2 = packagesToDelete3;
                                } else {
                                    i = length3;
                                    storage2 = storage3;
                                    packagesToDelete2 = packagesToDelete3;
                                    File metadataFile = new File(instantDir, INSTANT_APP_METADATA_FILE);
                                    if (metadataFile.exists()) {
                                        if (System.currentTimeMillis() - metadataFile.lastModified() > maxUninstalledCacheDuration) {
                                            deleteDir(instantDir);
                                            if (file.getUsableSpace() >= neededSpace) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                                i6++;
                                length3 = i;
                                storage3 = storage2;
                                packagesToDelete3 = packagesToDelete2;
                            }
                            storage = storage3;
                            packagesToDelete = packagesToDelete3;
                        }
                    }
                    i5++;
                    instantAppRegistry = this;
                    userIds = iArr;
                    storage3 = storage;
                    packagesToDelete3 = packagesToDelete;
                }
                return false;
            } catch (Throwable th6) {
                th = th6;
                throw th;
            }
        }
    }

    public /* synthetic */ int lambda$pruneInstantApps$2$InstantAppRegistry(String lhs, String rhs) {
        PackageParser.Package lhsPkg = this.mService.mPackages.get(lhs);
        PackageParser.Package rhsPkg = this.mService.mPackages.get(rhs);
        if (lhsPkg == null && rhsPkg == null) {
            return 0;
        }
        if (lhsPkg == null) {
            return -1;
        }
        if (rhsPkg == null || lhsPkg.getLatestPackageUseTimeInMills() > rhsPkg.getLatestPackageUseTimeInMills()) {
            return 1;
        }
        if (lhsPkg.getLatestPackageUseTimeInMills() < rhsPkg.getLatestPackageUseTimeInMills()) {
            return -1;
        }
        if (!(lhsPkg.mExtras instanceof PackageSetting) || !(rhsPkg.mExtras instanceof PackageSetting)) {
            return 0;
        }
        if (((PackageSetting) lhsPkg.mExtras).firstInstallTime > ((PackageSetting) rhsPkg.mExtras).firstInstallTime) {
            return 1;
        }
        return -1;
    }

    static /* synthetic */ boolean lambda$pruneInstantApps$3(long maxUninstalledCacheDuration, UninstalledInstantAppState state) {
        return System.currentTimeMillis() - state.mTimestamp > maxUninstalledCacheDuration;
    }

    private List<InstantAppInfo> getInstalledInstantApplicationsLPr(int userId) {
        InstantAppInfo info;
        List<InstantAppInfo> result = null;
        int packageCount = this.mService.mPackages.size();
        for (int i = 0; i < packageCount; i++) {
            PackageParser.Package pkg = this.mService.mPackages.valueAt(i);
            PackageSetting ps = (PackageSetting) pkg.mExtras;
            if (!(ps == null || !ps.getInstantApp(userId) || (info = createInstantAppInfoForPackage(pkg, userId, true)) == null)) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(info);
            }
        }
        return result;
    }

    private InstantAppInfo createInstantAppInfoForPackage(PackageParser.Package pkg, int userId, boolean addApplicationInfo) {
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (ps == null || !ps.getInstalled(userId)) {
            return null;
        }
        String[] requestedPermissions = new String[pkg.requestedPermissions.size()];
        pkg.requestedPermissions.toArray(requestedPermissions);
        Set<String> permissions = ps.getPermissionsState().getPermissions(userId);
        String[] grantedPermissions = new String[permissions.size()];
        permissions.toArray(grantedPermissions);
        if (addApplicationInfo) {
            return new InstantAppInfo(pkg.applicationInfo, requestedPermissions, grantedPermissions);
        }
        return new InstantAppInfo(pkg.applicationInfo.packageName, pkg.applicationInfo.loadLabel(this.mService.mContext.getPackageManager()), requestedPermissions, grantedPermissions);
    }

    private List<InstantAppInfo> getUninstalledInstantApplicationsLPr(int userId) {
        List<UninstalledInstantAppState> uninstalledAppStates = getUninstalledInstantAppStatesLPr(userId);
        if (uninstalledAppStates == null || uninstalledAppStates.isEmpty()) {
            return null;
        }
        List<InstantAppInfo> uninstalledApps = null;
        int stateCount = uninstalledAppStates.size();
        for (int i = 0; i < stateCount; i++) {
            UninstalledInstantAppState uninstalledAppState = uninstalledAppStates.get(i);
            if (uninstalledApps == null) {
                uninstalledApps = new ArrayList<>();
            }
            uninstalledApps.add(uninstalledAppState.mInstantAppInfo);
        }
        return uninstalledApps;
    }

    private void propagateInstantAppPermissionsIfNeeded(PackageParser.Package pkg, int userId) {
        InstantAppInfo appInfo = peekOrParseUninstalledInstantAppInfo(pkg.packageName, userId);
        if (appInfo != null && !ArrayUtils.isEmpty(appInfo.getGrantedPermissions())) {
            long identity = Binder.clearCallingIdentity();
            try {
                String[] grantedPermissions = appInfo.getGrantedPermissions();
                for (String grantedPermission : grantedPermissions) {
                    if (this.mService.mSettings.canPropagatePermissionToInstantApp(grantedPermission) && pkg.requestedPermissions.contains(grantedPermission)) {
                        this.mService.grantRuntimePermission(pkg.packageName, grantedPermission, userId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private InstantAppInfo peekOrParseUninstalledInstantAppInfo(String packageName, int userId) {
        List<UninstalledInstantAppState> uninstalledAppStates;
        SparseArray<List<UninstalledInstantAppState>> sparseArray = this.mUninstalledInstantApps;
        if (!(sparseArray == null || (uninstalledAppStates = sparseArray.get(userId)) == null)) {
            int appCount = uninstalledAppStates.size();
            for (int i = 0; i < appCount; i++) {
                UninstalledInstantAppState uninstalledAppState = uninstalledAppStates.get(i);
                if (uninstalledAppState.mInstantAppInfo.getPackageName().equals(packageName)) {
                    return uninstalledAppState.mInstantAppInfo;
                }
            }
        }
        UninstalledInstantAppState uninstalledAppState2 = parseMetadataFile(new File(getInstantApplicationDir(packageName, userId), INSTANT_APP_METADATA_FILE));
        if (uninstalledAppState2 == null) {
            return null;
        }
        return uninstalledAppState2.mInstantAppInfo;
    }

    private List<UninstalledInstantAppState> getUninstalledInstantAppStatesLPr(int userId) {
        File[] files;
        UninstalledInstantAppState uninstalledAppState;
        List<UninstalledInstantAppState> uninstalledAppStates = null;
        SparseArray<List<UninstalledInstantAppState>> sparseArray = this.mUninstalledInstantApps;
        if (!(sparseArray == null || (uninstalledAppStates = sparseArray.get(userId)) == null)) {
            return uninstalledAppStates;
        }
        File instantAppsDir = getInstantApplicationsDir(userId);
        if (instantAppsDir.exists() && (files = instantAppsDir.listFiles()) != null) {
            for (File instantDir : files) {
                if (instantDir.isDirectory() && (uninstalledAppState = parseMetadataFile(new File(instantDir, INSTANT_APP_METADATA_FILE))) != null) {
                    if (uninstalledAppStates == null) {
                        uninstalledAppStates = new ArrayList<>();
                    }
                    uninstalledAppStates.add(uninstalledAppState);
                }
            }
        }
        if (uninstalledAppStates != null) {
            if (this.mUninstalledInstantApps == null) {
                this.mUninstalledInstantApps = new SparseArray<>();
            }
            this.mUninstalledInstantApps.put(userId, uninstalledAppStates);
        }
        return uninstalledAppStates;
    }

    private static UninstalledInstantAppState parseMetadataFile(File metadataFile) {
        if (!metadataFile.exists()) {
            return null;
        }
        try {
            FileInputStream in = new AtomicFile(metadataFile).openRead();
            File instantDir = metadataFile.getParentFile();
            long timestamp = metadataFile.lastModified();
            String packageName = instantDir.getName();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, StandardCharsets.UTF_8.name());
                UninstalledInstantAppState uninstalledInstantAppState = new UninstalledInstantAppState(parseMetadata(parser, packageName), timestamp);
                IoUtils.closeQuietly(in);
                return uninstalledInstantAppState;
            } catch (IOException | XmlPullParserException e) {
                throw new IllegalStateException("Failed parsing instant metadata file: " + metadataFile, e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(in);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            Slog.i(LOG_TAG, "No instant metadata file");
            return null;
        }
    }

    public static File computeInstantCookieFile(String packageName, String sha256Digest, int userId) {
        File appDir = getInstantApplicationDir(packageName, userId);
        return new File(appDir, INSTANT_APP_COOKIE_FILE_PREFIX + sha256Digest + INSTANT_APP_COOKIE_FILE_SIFFIX);
    }

    public static File peekInstantCookieFile(String packageName, int userId) {
        File[] files;
        File appDir = getInstantApplicationDir(packageName, userId);
        if (!appDir.exists() || (files = appDir.listFiles()) == null) {
            return null;
        }
        for (File file : files) {
            if (!file.isDirectory() && file.getName().startsWith(INSTANT_APP_COOKIE_FILE_PREFIX) && file.getName().endsWith(INSTANT_APP_COOKIE_FILE_SIFFIX)) {
                return file;
            }
        }
        return null;
    }

    private static InstantAppInfo parseMetadata(XmlPullParser parser, String packageName) {
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if ("package".equals(parser.getName())) {
                return parsePackage(parser, packageName);
            }
        }
        return null;
    }

    private static InstantAppInfo parsePackage(XmlPullParser parser, String packageName) {
        String label = parser.getAttributeValue(null, ATTR_LABEL);
        List<String> outRequestedPermissions = new ArrayList<>();
        List<String> outGrantedPermissions = new ArrayList<>();
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (TAG_PERMISSIONS.equals(parser.getName())) {
                parsePermissions(parser, outRequestedPermissions, outGrantedPermissions);
            }
        }
        String[] requestedPermissions = new String[outRequestedPermissions.size()];
        outRequestedPermissions.toArray(requestedPermissions);
        String[] grantedPermissions = new String[outGrantedPermissions.size()];
        outGrantedPermissions.toArray(grantedPermissions);
        return new InstantAppInfo(packageName, label, requestedPermissions, grantedPermissions);
    }

    private static void parsePermissions(XmlPullParser parser, List<String> outRequestedPermissions, List<String> outGrantedPermissions) {
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (TAG_PERMISSION.equals(parser.getName())) {
                String permission = XmlUtils.readStringAttribute(parser, "name");
                outRequestedPermissions.add(permission);
                if (XmlUtils.readBooleanAttribute(parser, ATTR_GRANTED)) {
                    outGrantedPermissions.add(permission);
                }
            }
        }
    }

    private void writeUninstalledInstantAppMetadata(InstantAppInfo instantApp, int userId) {
        Throwable t;
        boolean z;
        File appDir = getInstantApplicationDir(instantApp.getPackageName(), userId);
        if (appDir.exists() || appDir.mkdirs()) {
            AtomicFile destination = new AtomicFile(new File(appDir, INSTANT_APP_METADATA_FILE));
            FileOutputStream out = null;
            try {
                out = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                String str = null;
                serializer.startDocument(null, true);
                serializer.startTag(null, "package");
                try {
                } catch (Throwable th) {
                    t = th;
                    try {
                        Slog.wtf(LOG_TAG, "Failed to write instant state, restoring backup", t);
                        destination.failWrite(out);
                        IoUtils.closeQuietly(out);
                    } catch (Throwable th2) {
                        IoUtils.closeQuietly(out);
                        throw th2;
                    }
                }
                try {
                    serializer.attribute(null, ATTR_LABEL, instantApp.loadLabel(this.mService.mContext.getPackageManager()).toString());
                    serializer.startTag(null, TAG_PERMISSIONS);
                    String[] requestedPermissions = instantApp.getRequestedPermissions();
                    int length = requestedPermissions.length;
                    int i = 0;
                    while (i < length) {
                        String permission = requestedPermissions[i];
                        serializer.startTag(str, TAG_PERMISSION);
                        try {
                            serializer.attribute(str, "name", permission);
                            if (ArrayUtils.contains(instantApp.getGrantedPermissions(), permission)) {
                                z = true;
                                serializer.attribute(null, ATTR_GRANTED, String.valueOf(true));
                            } else {
                                z = true;
                            }
                            serializer.endTag(null, TAG_PERMISSION);
                            i++;
                            appDir = appDir;
                            str = null;
                        } catch (Throwable th3) {
                            t = th3;
                            Slog.wtf(LOG_TAG, "Failed to write instant state, restoring backup", t);
                            destination.failWrite(out);
                            IoUtils.closeQuietly(out);
                        }
                    }
                    serializer.endTag(null, TAG_PERMISSIONS);
                    serializer.endTag(null, "package");
                    serializer.endDocument();
                    destination.finishWrite(out);
                } catch (Throwable th4) {
                    t = th4;
                    Slog.wtf(LOG_TAG, "Failed to write instant state, restoring backup", t);
                    destination.failWrite(out);
                    IoUtils.closeQuietly(out);
                }
            } catch (Throwable th5) {
                t = th5;
                Slog.wtf(LOG_TAG, "Failed to write instant state, restoring backup", t);
                destination.failWrite(out);
                IoUtils.closeQuietly(out);
            }
            IoUtils.closeQuietly(out);
        }
    }

    private static File getInstantApplicationsDir(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), INSTANT_APPS_FOLDER);
    }

    private static File getInstantApplicationDir(String packageName, int userId) {
        return new File(getInstantApplicationsDir(userId), packageName);
    }

    private static void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteDir(file);
            }
        }
        dir.delete();
    }

    public static final class UninstalledInstantAppState {
        final InstantAppInfo mInstantAppInfo;
        final long mTimestamp;

        public UninstalledInstantAppState(InstantAppInfo instantApp, long timestamp) {
            this.mInstantAppInfo = instantApp;
            this.mTimestamp = timestamp;
        }
    }

    public final class CookiePersistence extends Handler {
        private static final long PERSIST_COOKIE_DELAY_MILLIS = 1000;
        private final SparseArray<ArrayMap<String, SomeArgs>> mPendingPersistCookies = new SparseArray<>();

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public CookiePersistence(Looper looper) {
            super(looper);
            InstantAppRegistry.this = r1;
        }

        public void schedulePersistLPw(int userId, PackageParser.Package pkg, byte[] cookie) {
            File newCookieFile = InstantAppRegistry.computeInstantCookieFile(pkg.packageName, PackageUtils.computeSignaturesSha256Digest(pkg.mSigningDetails.signatures), userId);
            if (!pkg.mSigningDetails.hasSignatures()) {
                Slog.wtf(InstantAppRegistry.LOG_TAG, "Parsed Instant App contains no valid signatures!");
            }
            File oldCookieFile = InstantAppRegistry.peekInstantCookieFile(pkg.packageName, userId);
            if (oldCookieFile != null && !newCookieFile.equals(oldCookieFile)) {
                oldCookieFile.delete();
            }
            cancelPendingPersistLPw(pkg, userId);
            addPendingPersistCookieLPw(userId, pkg, cookie, newCookieFile);
            sendMessageDelayed(obtainMessage(userId, pkg), 1000);
        }

        public byte[] getPendingPersistCookieLPr(PackageParser.Package pkg, int userId) {
            SomeArgs state;
            ArrayMap<String, SomeArgs> pendingWorkForUser = this.mPendingPersistCookies.get(userId);
            if (pendingWorkForUser == null || (state = pendingWorkForUser.get(pkg.packageName)) == null) {
                return null;
            }
            return (byte[]) state.arg1;
        }

        public void cancelPendingPersistLPw(PackageParser.Package pkg, int userId) {
            removeMessages(userId, pkg);
            SomeArgs state = removePendingPersistCookieLPr(pkg, userId);
            if (state != null) {
                state.recycle();
            }
        }

        private void addPendingPersistCookieLPw(int userId, PackageParser.Package pkg, byte[] cookie, File cookieFile) {
            ArrayMap<String, SomeArgs> pendingWorkForUser = this.mPendingPersistCookies.get(userId);
            if (pendingWorkForUser == null) {
                pendingWorkForUser = new ArrayMap<>();
                this.mPendingPersistCookies.put(userId, pendingWorkForUser);
            }
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = cookie;
            args.arg2 = cookieFile;
            pendingWorkForUser.put(pkg.packageName, args);
        }

        private SomeArgs removePendingPersistCookieLPr(PackageParser.Package pkg, int userId) {
            ArrayMap<String, SomeArgs> pendingWorkForUser = this.mPendingPersistCookies.get(userId);
            SomeArgs state = null;
            if (pendingWorkForUser != null) {
                state = pendingWorkForUser.remove(pkg.packageName);
                if (pendingWorkForUser.isEmpty()) {
                    this.mPendingPersistCookies.remove(userId);
                }
            }
            return state;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int userId = message.what;
            PackageParser.Package pkg = (PackageParser.Package) message.obj;
            SomeArgs state = removePendingPersistCookieLPr(pkg, userId);
            if (state != null) {
                state.recycle();
                InstantAppRegistry.this.persistInstantApplicationCookie((byte[]) state.arg1, pkg.packageName, (File) state.arg2, userId);
            }
        }
    }
}
