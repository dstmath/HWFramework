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
import com.android.internal.annotations.GuardedBy;
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

class InstantAppRegistry {
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
    @GuardedBy("mService.mPackages")
    private SparseArray<SparseBooleanArray> mInstalledInstantAppUids;
    @GuardedBy("mService.mPackages")
    private SparseArray<SparseArray<SparseBooleanArray>> mInstantGrants;
    private final PackageManagerService mService;
    @GuardedBy("mService.mPackages")
    private SparseArray<List<UninstalledInstantAppState>> mUninstalledInstantApps;

    private final class CookiePersistence extends Handler {
        private static final long PERSIST_COOKIE_DELAY_MILLIS = 1000;
        private final SparseArray<ArrayMap<PackageParser.Package, SomeArgs>> mPendingPersistCookies = new SparseArray<>();

        public CookiePersistence(Looper looper) {
            super(looper);
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
            ArrayMap<PackageParser.Package, SomeArgs> pendingWorkForUser = this.mPendingPersistCookies.get(userId);
            if (pendingWorkForUser != null) {
                SomeArgs state = pendingWorkForUser.get(pkg);
                if (state != null) {
                    return (byte[]) state.arg1;
                }
            }
            return null;
        }

        public void cancelPendingPersistLPw(PackageParser.Package pkg, int userId) {
            removeMessages(userId, pkg);
            SomeArgs state = removePendingPersistCookieLPr(pkg, userId);
            if (state != null) {
                state.recycle();
            }
        }

        private void addPendingPersistCookieLPw(int userId, PackageParser.Package pkg, byte[] cookie, File cookieFile) {
            ArrayMap<PackageParser.Package, SomeArgs> pendingWorkForUser = this.mPendingPersistCookies.get(userId);
            if (pendingWorkForUser == null) {
                pendingWorkForUser = new ArrayMap<>();
                this.mPendingPersistCookies.put(userId, pendingWorkForUser);
            }
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = cookie;
            args.arg2 = cookieFile;
            pendingWorkForUser.put(pkg, args);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: com.android.internal.os.SomeArgs} */
        /* JADX WARNING: Multi-variable type inference failed */
        private SomeArgs removePendingPersistCookieLPr(PackageParser.Package pkg, int userId) {
            ArrayMap<PackageParser.Package, SomeArgs> pendingWorkForUser = this.mPendingPersistCookies.get(userId);
            SomeArgs state = null;
            if (pendingWorkForUser != null) {
                state = pendingWorkForUser.remove(pkg);
                if (pendingWorkForUser.isEmpty()) {
                    this.mPendingPersistCookies.remove(userId);
                }
            }
            return state;
        }

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

    private static final class UninstalledInstantAppState {
        final InstantAppInfo mInstantAppInfo;
        final long mTimestamp;

        public UninstalledInstantAppState(InstantAppInfo instantApp, long timestamp) {
            this.mInstantAppInfo = instantApp;
            this.mTimestamp = timestamp;
        }
    }

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
        if (cookie != null && cookie.length > 0) {
            int maxCookieSize = this.mService.mContext.getPackageManager().getInstantAppCookieMaxBytes();
            if (cookie.length > maxCookieSize) {
                Slog.e(LOG_TAG, "Instant app cookie for package " + packageName + " size " + cookie.length + " bytes while max size is " + maxCookieSize);
                return false;
            }
        }
        PackageParser.Package pkg = this.mService.mPackages.get(packageName);
        if (pkg == null) {
            return false;
        }
        this.mCookiePersistence.schedulePersistLPw(userId, pkg, cookie);
        return true;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r0 = new java.io.FileOutputStream(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r0.write(r5, 0, r5.length);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        $closeResource(null, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0047, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x004e, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x004f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0050, code lost:
        android.util.Slog.e(LOG_TAG, "Error writing instant app cookie file: " + r7, r0);
     */
    public void persistInstantApplicationCookie(byte[] cookie, String packageName, File cookieFile, int userId) {
        synchronized (this.mService.mPackages) {
            File appDir = getInstantApplicationDir(packageName, userId);
            if (appDir.exists() || appDir.mkdirs()) {
                if (cookieFile.exists() && !cookieFile.delete()) {
                    Slog.e(LOG_TAG, "Cannot delete instant app cookie file");
                }
                if (cookie != null) {
                    if (cookie.length <= 0) {
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

    private String generateInstantAppAndroidIdLPw(String packageName, int userId) {
        FileOutputStream fos;
        byte[] randomBytes = new byte[8];
        new SecureRandom().nextBytes(randomBytes);
        String id = ByteStringUtils.toHexString(randomBytes).toLowerCase(Locale.US);
        File appDir = getInstantApplicationDir(packageName, userId);
        if (appDir.exists() || appDir.mkdirs()) {
            File idFile = new File(getInstantApplicationDir(packageName, userId), INSTANT_APP_ANDROID_ID_FILE);
            try {
                fos = new FileOutputStream(idFile);
                fos.write(id.getBytes());
                $closeResource(null, fos);
            } catch (IOException e) {
                Slog.e(LOG_TAG, "Error writing instant app android id file: " + idFile, e);
            } catch (Throwable th) {
                $closeResource(r5, fos);
                throw th;
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
        PackageParser.Package packageR = pkg;
        int[] iArr = userIds;
        PackageSetting ps = (PackageSetting) packageR.mExtras;
        if (ps != null) {
            for (int userId : iArr) {
                if (this.mService.mPackages.get(packageR.packageName) != null && ps.getInstalled(userId)) {
                    propagateInstantAppPermissionsIfNeeded(packageR, userId);
                    if (ps.getInstantApp(userId)) {
                        addInstantAppLPw(userId, ps.appId);
                    }
                    removeUninstalledInstantAppStateLPw(new Predicate(packageR) {
                        private final /* synthetic */ PackageParser.Package f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final boolean test(Object obj) {
                            return ((InstantAppRegistry.UninstalledInstantAppState) obj).mInstantAppInfo.getPackageName().equals(this.f$0.packageName);
                        }
                    }, userId);
                    File instantAppDir = getInstantApplicationDir(packageR.packageName, userId);
                    new File(instantAppDir, INSTANT_APP_METADATA_FILE).delete();
                    new File(instantAppDir, INSTANT_APP_ICON_FILE).delete();
                    File currentCookieFile = peekInstantCookieFile(packageR.packageName, userId);
                    if (currentCookieFile == null) {
                        continue;
                    } else {
                        String cookieName = currentCookieFile.getName();
                        String currentCookieSha256 = cookieName.substring(INSTANT_APP_COOKIE_FILE_PREFIX.length(), cookieName.length() - INSTANT_APP_COOKIE_FILE_SIFFIX.length());
                        if (!packageR.mSigningDetails.checkCapability(currentCookieSha256, 1)) {
                            String[] signaturesSha256Digests = PackageUtils.computeSignaturesSha256Digests(packageR.mSigningDetails.signatures);
                            int length = signaturesSha256Digests.length;
                            int i = 0;
                            while (i < length) {
                                if (!signaturesSha256Digests[i].equals(currentCookieSha256)) {
                                    i++;
                                } else {
                                    return;
                                }
                            }
                            Slog.i(LOG_TAG, "Signature for package " + packageR.packageName + " changed - dropping cookie");
                            this.mCookiePersistence.cancelPendingPersistLPw(packageR, userId);
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
        if (ps != null) {
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
        if (this.mUninstalledInstantApps != null) {
            this.mUninstalledInstantApps.remove(userId);
            if (this.mUninstalledInstantApps.size() <= 0) {
                this.mUninstalledInstantApps = null;
            }
        }
        if (this.mInstalledInstantAppUids != null) {
            this.mInstalledInstantAppUids.remove(userId);
            if (this.mInstalledInstantAppUids.size() <= 0) {
                this.mInstalledInstantAppUids = null;
            }
        }
        if (this.mInstantGrants != null) {
            this.mInstantGrants.remove(userId);
            if (this.mInstantGrants.size() <= 0) {
                this.mInstantGrants = null;
            }
        }
        deleteDir(getInstantApplicationsDir(userId));
    }

    public boolean isInstantAccessGranted(int userId, int targetAppId, int instantAppId) {
        if (this.mInstantGrants == null) {
            return false;
        }
        SparseArray<SparseBooleanArray> targetAppList = this.mInstantGrants.get(userId);
        if (targetAppList == null) {
            return false;
        }
        SparseBooleanArray instantGrantList = targetAppList.get(targetAppId);
        if (instantGrantList == null) {
            return false;
        }
        return instantGrantList.get(instantAppId);
    }

    public void grantInstantAccessLPw(int userId, Intent intent, int targetAppId, int instantAppId) {
        if (this.mInstalledInstantAppUids != null) {
            SparseBooleanArray instantAppList = this.mInstalledInstantAppUids.get(userId);
            if (instantAppList != null && instantAppList.get(instantAppId) && !instantAppList.get(targetAppId)) {
                if (intent != null && "android.intent.action.VIEW".equals(intent.getAction())) {
                    Set<String> categories = intent.getCategories();
                    if (categories != null && categories.contains("android.intent.category.BROWSABLE")) {
                        return;
                    }
                }
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
        if (this.mInstalledInstantAppUids != null) {
            SparseBooleanArray instantAppList = this.mInstalledInstantAppUids.get(userId);
            if (instantAppList != null) {
                instantAppList.delete(instantAppId);
                if (this.mInstantGrants != null) {
                    SparseArray<SparseBooleanArray> targetAppList = this.mInstantGrants.get(userId);
                    if (targetAppList != null) {
                        for (int i = targetAppList.size() - 1; i >= 0; i--) {
                            targetAppList.valueAt(i).delete(instantAppId);
                        }
                    }
                }
            }
        }
    }

    private void removeAppLPw(int userId, int targetAppId) {
        if (this.mInstantGrants != null) {
            SparseArray<SparseBooleanArray> targetAppList = this.mInstantGrants.get(userId);
            if (targetAppList != null) {
                targetAppList.delete(targetAppId);
            }
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
                uninstalledAppStates = new ArrayList<>();
                this.mUninstalledInstantApps.put(userId, uninstalledAppStates);
            }
            uninstalledAppStates.add(new UninstalledInstantAppState(uninstalledApp, System.currentTimeMillis()));
            writeUninstalledInstantAppMetadata(uninstalledApp, userId);
            writeInstantApplicationIconLPw(pkg, userId);
        }
    }

    private void writeInstantApplicationIconLPw(PackageParser.Package pkg, int userId) {
        Bitmap bitmap;
        FileOutputStream out;
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
                out = new FileOutputStream(new File(getInstantApplicationDir(pkg.packageName, userId), INSTANT_APP_ICON_FILE));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                $closeResource(null, out);
            } catch (Exception e) {
                Slog.e(LOG_TAG, "Error writing instant app icon", e);
            } catch (Throwable th) {
                $closeResource(r5, out);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasInstantApplicationMetadataLPr(String packageName, int userId) {
        boolean z = false;
        if (packageName == null) {
            return false;
        }
        if (hasUninstalledInstantAppStateLPr(packageName, userId) || hasInstantAppMetadataLPr(packageName, userId)) {
            z = true;
        }
        return z;
    }

    public void deleteInstantApplicationMetadataLPw(String packageName, int userId) {
        if (packageName != null) {
            removeUninstalledInstantAppStateLPw(new Predicate(packageName) {
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return ((InstantAppRegistry.UninstalledInstantAppState) obj).mInstantAppInfo.getPackageName().equals(this.f$0);
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
        if (this.mUninstalledInstantApps != null) {
            List<UninstalledInstantAppState> uninstalledAppStates = this.mUninstalledInstantApps.get(userId);
            if (uninstalledAppStates != null) {
                for (int i = uninstalledAppStates.size() - 1; i >= 0; i--) {
                    if (criteria.test(uninstalledAppStates.get(i))) {
                        uninstalledAppStates.remove(i);
                        if (uninstalledAppStates.isEmpty()) {
                            this.mUninstalledInstantApps.remove(userId);
                            if (this.mUninstalledInstantApps.size() <= 0) {
                                this.mUninstalledInstantApps = null;
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean hasUninstalledInstantAppStateLPr(String packageName, int userId) {
        if (this.mUninstalledInstantApps == null) {
            return false;
        }
        List<UninstalledInstantAppState> uninstalledAppStates = this.mUninstalledInstantApps.get(userId);
        if (uninstalledAppStates == null) {
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

    /* access modifiers changed from: package-private */
    public void pruneInstantApps() {
        try {
            pruneInstantApps(JobStatus.NO_LATEST_RUNTIME, Settings.Global.getLong(this.mService.mContext.getContentResolver(), "installed_instant_app_max_cache_period", 15552000000L), Settings.Global.getLong(this.mService.mContext.getContentResolver(), "uninstalled_instant_app_max_cache_period", 15552000000L));
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning installed and uninstalled instant apps", e);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean pruneInstalledInstantApps(long neededSpace, long maxInstalledCacheDuration) {
        try {
            return pruneInstantApps(neededSpace, maxInstalledCacheDuration, JobStatus.NO_LATEST_RUNTIME);
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning installed instant apps", e);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean pruneUninstalledInstantApps(long neededSpace, long maxUninstalledCacheDuration) {
        try {
            return pruneInstantApps(neededSpace, JobStatus.NO_LATEST_RUNTIME, maxUninstalledCacheDuration);
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning uninstalled instant apps", e);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00b8, code lost:
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b9, code lost:
        if (r13 == null) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00bb, code lost:
        r0 = r13.size();
        r6 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c0, code lost:
        if (r6 >= r0) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d9, code lost:
        if (r1.mService.deletePackageX(r13.get(r6), -1, 0, 2) != 1) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00e1, code lost:
        if (r5.getUsableSpace() < r30) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00e3, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e4, code lost:
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00e7, code lost:
        r6 = r1.mService.mPackages;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00eb, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        r0 = com.android.server.pm.UserManagerService.getInstance().getUserIds();
        r7 = r0.length;
        r8 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00f6, code lost:
        if (r8 >= r7) goto L_0x017e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00f8, code lost:
        r9 = r0[r8];
        r1.removeUninstalledInstantAppStateLPw(new com.android.server.pm.$$Lambda$InstantAppRegistry$BuKCbLr_MGBazMPl54pWTuGHYY(r2), r9);
        r10 = getInstantApplicationsDir(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x010a, code lost:
        if (r10.exists() != false) goto L_0x0115;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x010d, code lost:
        r25 = r0;
        r27 = r4;
        r28 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0115, code lost:
        r11 = r10.listFiles();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0119, code lost:
        if (r11 != null) goto L_0x011c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x011c, code lost:
        r12 = r11.length;
        r25 = r0;
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0120, code lost:
        if (r0 >= r12) goto L_0x016d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0122, code lost:
        r1 = r11[r0];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x012c, code lost:
        if (r1.isDirectory() != false) goto L_0x0135;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x012e, code lost:
        r27 = r4;
        r28 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0135, code lost:
        r27 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:?, code lost:
        r28 = r7;
        r4 = new java.io.File(r1, INSTANT_APP_METADATA_FILE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0145, code lost:
        if (r4.exists() != false) goto L_0x0148;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0154, code lost:
        if ((java.lang.System.currentTimeMillis() - r4.lastModified()) <= r2) goto L_0x0133;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0156, code lost:
        deleteDir(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x015f, code lost:
        if (r5.getUsableSpace() < r30) goto L_0x0133;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0161, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0163, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0164, code lost:
        r0 = r0 + 1;
        r4 = r27;
        r7 = r28;
        r1 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x016d, code lost:
        r27 = r4;
        r28 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0172, code lost:
        r8 = r8 + 1;
        r0 = r25;
        r4 = r27;
        r7 = r28;
        r1 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x017e, code lost:
        r27 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0180, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0182, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0183, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0184, code lost:
        r27 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0186, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0187, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0188, code lost:
        r0 = th;
     */
    private boolean pruneInstantApps(long neededSpace, long maxInstalledCacheDuration, long maxUninstalledCacheDuration) throws IOException {
        long now;
        StorageManager storage;
        InstantAppRegistry instantAppRegistry = this;
        long j = maxUninstalledCacheDuration;
        StorageManager storage2 = (StorageManager) instantAppRegistry.mService.mContext.getSystemService(StorageManager.class);
        File file = storage2.findPathForUuid(StorageManager.UUID_PRIVATE_INTERNAL);
        if (file.getUsableSpace() >= neededSpace) {
            return true;
        }
        long now2 = System.currentTimeMillis();
        synchronized (instantAppRegistry.mService.mPackages) {
            try {
                int[] allUsers = PackageManagerService.sUserManager.getUserIds();
                int packageCount = instantAppRegistry.mService.mPackages.size();
                List<String> packagesToDelete = null;
                int i = 0;
                while (i < packageCount) {
                    try {
                        PackageParser.Package pkg = instantAppRegistry.mService.mPackages.valueAt(i);
                        if (now2 - pkg.getLatestPackageUseTimeInMills() >= maxInstalledCacheDuration) {
                            if (pkg.mExtras instanceof PackageSetting) {
                                PackageSetting ps = (PackageSetting) pkg.mExtras;
                                boolean installedOnlyAsInstantApp = false;
                                storage = storage2;
                                try {
                                    int length = allUsers.length;
                                    now = now2;
                                    int i2 = 0;
                                    while (true) {
                                        if (i2 >= length) {
                                            break;
                                        }
                                        int userId = allUsers[i2];
                                        if (ps.getInstalled(userId)) {
                                            if (!ps.getInstantApp(userId)) {
                                                installedOnlyAsInstantApp = false;
                                                break;
                                            }
                                            installedOnlyAsInstantApp = true;
                                        }
                                        i2++;
                                    }
                                    if (installedOnlyAsInstantApp) {
                                        if (packagesToDelete == null) {
                                            packagesToDelete = new ArrayList<>();
                                        }
                                        packagesToDelete.add(pkg.packageName);
                                    }
                                    i++;
                                    storage2 = storage;
                                    now2 = now;
                                } catch (Throwable th) {
                                    th = th;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                        }
                        storage = storage2;
                        now = now2;
                        i++;
                        storage2 = storage;
                        now2 = now;
                    } catch (Throwable th2) {
                        th = th2;
                        StorageManager storageManager = storage2;
                        long j2 = now2;
                        ArrayList arrayList = packagesToDelete;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                StorageManager storageManager2 = storage2;
                long j3 = now2;
                if (packagesToDelete != null) {
                    packagesToDelete.sort(new Comparator() {
                        public final int compare(Object obj, Object obj2) {
                            return InstantAppRegistry.lambda$pruneInstantApps$2(InstantAppRegistry.this, (String) obj, (String) obj2);
                        }
                    });
                }
            } catch (Throwable th3) {
                th = th3;
                StorageManager storageManager3 = storage2;
                long j4 = now2;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    public static /* synthetic */ int lambda$pruneInstantApps$2(InstantAppRegistry instantAppRegistry, String lhs, String rhs) {
        PackageParser.Package lhsPkg = instantAppRegistry.mService.mPackages.get(lhs);
        PackageParser.Package rhsPkg = instantAppRegistry.mService.mPackages.get(rhs);
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
        List<InstantAppInfo> result = null;
        int packageCount = this.mService.mPackages.size();
        for (int i = 0; i < packageCount; i++) {
            PackageParser.Package pkg = this.mService.mPackages.valueAt(i);
            PackageSetting ps = (PackageSetting) pkg.mExtras;
            if (ps != null && ps.getInstantApp(userId)) {
                InstantAppInfo info = createInstantAppInfoForPackage(pkg, userId, true);
                if (info != null) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(info);
                }
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
                for (String grantedPermission : appInfo.getGrantedPermissions()) {
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
        if (this.mUninstalledInstantApps != null) {
            List<UninstalledInstantAppState> uninstalledAppStates = this.mUninstalledInstantApps.get(userId);
            if (uninstalledAppStates != null) {
                int appCount = uninstalledAppStates.size();
                for (int i = 0; i < appCount; i++) {
                    UninstalledInstantAppState uninstalledAppState = uninstalledAppStates.get(i);
                    if (uninstalledAppState.mInstantAppInfo.getPackageName().equals(packageName)) {
                        return uninstalledAppState.mInstantAppInfo;
                    }
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
        List<UninstalledInstantAppState> uninstalledAppStates = null;
        if (this.mUninstalledInstantApps != null) {
            uninstalledAppStates = this.mUninstalledInstantApps.get(userId);
            if (uninstalledAppStates != null) {
                return uninstalledAppStates;
            }
        }
        File instantAppsDir = getInstantApplicationsDir(userId);
        if (instantAppsDir.exists()) {
            File[] files = instantAppsDir.listFiles();
            if (files != null) {
                for (File instantDir : files) {
                    if (instantDir.isDirectory()) {
                        UninstalledInstantAppState uninstalledAppState = parseMetadataFile(new File(instantDir, INSTANT_APP_METADATA_FILE));
                        if (uninstalledAppState != null) {
                            if (uninstalledAppStates == null) {
                                uninstalledAppStates = new ArrayList<>();
                            }
                            uninstalledAppStates.add(uninstalledAppState);
                        }
                    }
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

    /* access modifiers changed from: private */
    public static File computeInstantCookieFile(String packageName, String sha256Digest, int userId) {
        File appDir = getInstantApplicationDir(packageName, userId);
        return new File(appDir, INSTANT_APP_COOKIE_FILE_PREFIX + sha256Digest + INSTANT_APP_COOKIE_FILE_SIFFIX);
    }

    /* access modifiers changed from: private */
    public static File peekInstantCookieFile(String packageName, int userId) {
        File appDir = getInstantApplicationDir(packageName, userId);
        if (!appDir.exists()) {
            return null;
        }
        File[] files = appDir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (!file.isDirectory() && file.getName().startsWith(INSTANT_APP_COOKIE_FILE_PREFIX) && file.getName().endsWith(INSTANT_APP_COOKIE_FILE_SIFFIX)) {
                return file;
            }
        }
        return null;
    }

    private static InstantAppInfo parseMetadata(XmlPullParser parser, String packageName) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if ("package".equals(parser.getName())) {
                return parsePackage(parser, packageName);
            }
        }
        return null;
    }

    private static InstantAppInfo parsePackage(XmlPullParser parser, String packageName) throws IOException, XmlPullParserException {
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

    private static void parsePermissions(XmlPullParser parser, List<String> outRequestedPermissions, List<String> outGrantedPermissions) throws IOException, XmlPullParserException {
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
        File appDir = getInstantApplicationDir(instantApp.getPackageName(), userId);
        if (appDir.exists() || appDir.mkdirs()) {
            AtomicFile destination = new AtomicFile(new File(appDir, INSTANT_APP_METADATA_FILE));
            FileOutputStream out = null;
            try {
                out = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, true);
                serializer.startTag(null, "package");
                serializer.attribute(null, ATTR_LABEL, instantApp.loadLabel(this.mService.mContext.getPackageManager()).toString());
                serializer.startTag(null, TAG_PERMISSIONS);
                for (String permission : instantApp.getRequestedPermissions()) {
                    serializer.startTag(null, TAG_PERMISSION);
                    serializer.attribute(null, "name", permission);
                    if (ArrayUtils.contains(instantApp.getGrantedPermissions(), permission)) {
                        serializer.attribute(null, ATTR_GRANTED, String.valueOf(true));
                    }
                    serializer.endTag(null, TAG_PERMISSION);
                }
                serializer.endTag(null, TAG_PERMISSIONS);
                serializer.endTag(null, "package");
                serializer.endDocument();
                destination.finishWrite(out);
            } catch (Throwable th) {
                IoUtils.closeQuietly(null);
                throw th;
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
}
