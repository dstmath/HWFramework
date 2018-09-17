package com.android.server.pm;

import android.content.Intent;
import android.content.pm.InstantAppInfo;
import android.content.pm.PackageParser.Package;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
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
import android.provider.Settings.Global;
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
import com.android.server.pm.-$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw.AnonymousClass1;
import com.android.server.pm.-$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw.AnonymousClass2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
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
        private final SparseArray<ArrayMap<Package, SomeArgs>> mPendingPersistCookies = new SparseArray();

        public CookiePersistence(Looper looper) {
            super(looper);
        }

        public void schedulePersistLPw(int userId, Package pkg, byte[] cookie) {
            File cookieFile = InstantAppRegistry.computeInstantCookieFile(pkg, userId);
            cancelPendingPersistLPw(pkg, userId);
            addPendingPersistCookieLPw(userId, pkg, cookie, cookieFile);
            sendMessageDelayed(obtainMessage(userId, pkg), 1000);
        }

        public byte[] getPendingPersistCookieLPr(Package pkg, int userId) {
            ArrayMap<Package, SomeArgs> pendingWorkForUser = (ArrayMap) this.mPendingPersistCookies.get(userId);
            if (pendingWorkForUser != null) {
                SomeArgs state = (SomeArgs) pendingWorkForUser.get(pkg);
                if (state != null) {
                    return (byte[]) state.arg1;
                }
            }
            return null;
        }

        public void cancelPendingPersistLPw(Package pkg, int userId) {
            removeMessages(userId, pkg);
            SomeArgs state = removePendingPersistCookieLPr(pkg, userId);
            if (state != null) {
                state.recycle();
            }
        }

        private void addPendingPersistCookieLPw(int userId, Package pkg, byte[] cookie, File cookieFile) {
            ArrayMap<Package, SomeArgs> pendingWorkForUser = (ArrayMap) this.mPendingPersistCookies.get(userId);
            if (pendingWorkForUser == null) {
                pendingWorkForUser = new ArrayMap();
                this.mPendingPersistCookies.put(userId, pendingWorkForUser);
            }
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = cookie;
            args.arg2 = cookieFile;
            pendingWorkForUser.put(pkg, args);
        }

        private SomeArgs removePendingPersistCookieLPr(Package pkg, int userId) {
            ArrayMap<Package, SomeArgs> pendingWorkForUser = (ArrayMap) this.mPendingPersistCookies.get(userId);
            SomeArgs state = null;
            if (pendingWorkForUser != null) {
                state = (SomeArgs) pendingWorkForUser.remove(pkg);
                if (pendingWorkForUser.isEmpty()) {
                    this.mPendingPersistCookies.remove(userId);
                }
            }
            return state;
        }

        public void handleMessage(Message message) {
            int userId = message.what;
            Package pkg = message.obj;
            SomeArgs state = removePendingPersistCookieLPr(pkg, userId);
            if (state != null) {
                byte[] cookie = state.arg1;
                File cookieFile = state.arg2;
                state.recycle();
                InstantAppRegistry.this.persistInstantApplicationCookie(cookie, pkg.packageName, cookieFile, userId);
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
        Package pkg = (Package) this.mService.mPackages.get(packageName);
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
        Package pkg = (Package) this.mService.mPackages.get(packageName);
        if (pkg == null) {
            return false;
        }
        this.mCookiePersistence.schedulePersistLPw(userId, pkg, cookie);
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0081 A:{SYNTHETIC, Splitter: B:48:0x0081} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0094 A:{Catch:{ IOException -> 0x0087 }} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0086 A:{SYNTHETIC, Splitter: B:51:0x0086} */
    /* JADX WARNING: Missing block: B:20:0x0040, code:
            return;
     */
    /* JADX WARNING: Missing block: B:22:0x0042, code:
            r2 = null;
     */
    /* JADX WARNING: Missing block: B:24:?, code:
            r3 = new java.io.FileOutputStream(r12);
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            r3.write(r10, 0, r10.length);
     */
    /* JADX WARNING: Missing block: B:27:0x004d, code:
            if (r3 == null) goto L_0x0052;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:35:0x0057, code:
            android.util.Slog.e(LOG_TAG, "Error writing instant app cookie file: " + r12, r1);
     */
    /* JADX WARNING: Missing block: B:40:0x0075, code:
            r5 = th;
     */
    /* JADX WARNING: Missing block: B:42:0x0079, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            throw r4;
     */
    /* JADX WARNING: Missing block: B:45:0x007b, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:46:0x007c, code:
            r8 = r5;
            r5 = r4;
            r4 = r8;
     */
    /* JADX WARNING: Missing block: B:47:0x007f, code:
            if (r2 != null) goto L_0x0081;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r2.close();
     */
    /* JADX WARNING: Missing block: B:50:0x0084, code:
            if (r5 == null) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            throw r5;
     */
    /* JADX WARNING: Missing block: B:53:0x0087, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:54:0x0089, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:55:0x008a, code:
            if (r5 == null) goto L_0x008c;
     */
    /* JADX WARNING: Missing block: B:56:0x008c, code:
            r5 = r6;
     */
    /* JADX WARNING: Missing block: B:57:0x008e, code:
            if (r5 != r6) goto L_0x0090;
     */
    /* JADX WARNING: Missing block: B:58:0x0090, code:
            r5.addSuppressed(r6);
     */
    /* JADX WARNING: Missing block: B:59:0x0094, code:
            throw r4;
     */
    /* JADX WARNING: Missing block: B:60:0x0095, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:61:0x0097, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:62:0x0098, code:
            r2 = r3;
     */
    /* JADX WARNING: Missing block: B:63:0x009a, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:64:0x009b, code:
            r2 = r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void persistInstantApplicationCookie(byte[] cookie, String packageName, File cookieFile, int userId) {
        Throwable th = null;
        synchronized (this.mService.mPackages) {
            File appDir = getInstantApplicationDir(packageName, userId);
            if (appDir.exists() || (appDir.mkdirs() ^ 1) == 0) {
                if (cookieFile.exists() && (cookieFile.delete() ^ 1) != 0) {
                    Slog.e(LOG_TAG, "Cannot delete instant app cookie file");
                }
                if (cookie == null || cookie.length <= 0) {
                }
            } else {
                Slog.e(LOG_TAG, "Cannot create instant app cookie directory");
                return;
            }
        }
        if (th != null) {
            try {
                throw th;
            } catch (IOException e) {
                IOException e2 = e;
                FileOutputStream fileOutputStream = fos;
            }
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

    /* JADX WARNING: Removed duplicated region for block: B:29:0x007d A:{SYNTHETIC, Splitter: B:29:0x007d} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0090 A:{Catch:{ IOException -> 0x0083 }} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0082 A:{SYNTHETIC, Splitter: B:32:0x0082} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String generateInstantAppAndroidIdLPw(String packageName, int userId) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        byte[] randomBytes = new byte[8];
        new SecureRandom().nextBytes(randomBytes);
        String id = ByteStringUtils.toHexString(randomBytes).toLowerCase(Locale.US);
        File appDir = getInstantApplicationDir(packageName, userId);
        if (appDir.exists() || (appDir.mkdirs() ^ 1) == 0) {
            File idFile = new File(getInstantApplicationDir(packageName, userId), INSTANT_APP_ANDROID_ID_FILE);
            FileOutputStream fos = null;
            try {
                FileOutputStream fos2 = new FileOutputStream(idFile);
                try {
                    fos2.write(id.getBytes());
                    if (fos2 != null) {
                        try {
                            fos2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (IOException e2) {
                            e = e2;
                            fos = fos2;
                        }
                    } else {
                        return id;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fos = fos2;
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (IOException e3) {
                            e = e3;
                            Slog.e(LOG_TAG, "Error writing instant app android id file: " + idFile, e);
                            return id;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (fos != null) {
                }
                if (th2 == null) {
                }
            }
        } else {
            Slog.e(LOG_TAG, "Cannot create instant app cookie directory");
            return id;
        }
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

    public void onPackageInstalledLPw(Package pkg, int[] userIds) {
        PackageSetting ps = pkg.mExtras;
        if (ps != null) {
            for (int userId : userIds) {
                if (this.mService.mPackages.get(pkg.packageName) != null && (ps.getInstalled(userId) ^ 1) == 0) {
                    propagateInstantAppPermissionsIfNeeded(pkg.packageName, userId);
                    if (ps.getInstantApp(userId)) {
                        addInstantAppLPw(userId, ps.appId);
                    }
                    removeUninstalledInstantAppStateLPw(new AnonymousClass2(pkg), userId);
                    File instantAppDir = getInstantApplicationDir(pkg.packageName, userId);
                    new File(instantAppDir, INSTANT_APP_METADATA_FILE).delete();
                    new File(instantAppDir, INSTANT_APP_ICON_FILE).delete();
                    File currentCookieFile = peekInstantCookieFile(pkg.packageName, userId);
                    if (!(currentCookieFile == null || currentCookieFile.equals(computeInstantCookieFile(pkg, userId)))) {
                        Slog.i(LOG_TAG, "Signature for package " + pkg.packageName + " changed - dropping cookie");
                        this.mCookiePersistence.cancelPendingPersistLPw(pkg, userId);
                        currentCookieFile.delete();
                    }
                }
            }
        }
    }

    public void onPackageUninstalledLPw(Package pkg, int[] userIds) {
        PackageSetting ps = pkg.mExtras;
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
        SparseArray<SparseBooleanArray> targetAppList = (SparseArray) this.mInstantGrants.get(userId);
        if (targetAppList == null) {
            return false;
        }
        SparseBooleanArray instantGrantList = (SparseBooleanArray) targetAppList.get(targetAppId);
        if (instantGrantList == null) {
            return false;
        }
        return instantGrantList.get(instantAppId);
    }

    /* JADX WARNING: Missing block: B:7:0x0017, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void grantInstantAccessLPw(int userId, Intent intent, int targetAppId, int instantAppId) {
        if (this.mInstalledInstantAppUids != null) {
            SparseBooleanArray instantAppList = (SparseBooleanArray) this.mInstalledInstantAppUids.get(userId);
            if (instantAppList != null && (instantAppList.get(instantAppId) ^ 1) == 0 && !instantAppList.get(targetAppId)) {
                if (intent != null && "android.intent.action.VIEW".equals(intent.getAction())) {
                    Set<String> categories = intent.getCategories();
                    if (categories != null && categories.contains("android.intent.category.BROWSABLE")) {
                        return;
                    }
                }
                if (this.mInstantGrants == null) {
                    this.mInstantGrants = new SparseArray();
                }
                SparseArray<SparseBooleanArray> targetAppList = (SparseArray) this.mInstantGrants.get(userId);
                if (targetAppList == null) {
                    targetAppList = new SparseArray();
                    this.mInstantGrants.put(userId, targetAppList);
                }
                SparseBooleanArray instantGrantList = (SparseBooleanArray) targetAppList.get(targetAppId);
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
            this.mInstalledInstantAppUids = new SparseArray();
        }
        SparseBooleanArray instantAppList = (SparseBooleanArray) this.mInstalledInstantAppUids.get(userId);
        if (instantAppList == null) {
            instantAppList = new SparseBooleanArray();
            this.mInstalledInstantAppUids.put(userId, instantAppList);
        }
        instantAppList.put(instantAppId, true);
    }

    private void removeInstantAppLPw(int userId, int instantAppId) {
        if (this.mInstalledInstantAppUids != null) {
            SparseBooleanArray instantAppList = (SparseBooleanArray) this.mInstalledInstantAppUids.get(userId);
            if (instantAppList != null) {
                instantAppList.delete(instantAppId);
                if (this.mInstantGrants != null) {
                    SparseArray<SparseBooleanArray> targetAppList = (SparseArray) this.mInstantGrants.get(userId);
                    if (targetAppList != null) {
                        for (int i = targetAppList.size() - 1; i >= 0; i--) {
                            ((SparseBooleanArray) targetAppList.valueAt(i)).delete(instantAppId);
                        }
                    }
                }
            }
        }
    }

    private void removeAppLPw(int userId, int targetAppId) {
        if (this.mInstantGrants != null) {
            SparseArray<SparseBooleanArray> targetAppList = (SparseArray) this.mInstantGrants.get(userId);
            if (targetAppList != null) {
                targetAppList.delete(targetAppId);
            }
        }
    }

    private void addUninstalledInstantAppLPw(Package pkg, int userId) {
        InstantAppInfo uninstalledApp = createInstantAppInfoForPackage(pkg, userId, false);
        if (uninstalledApp != null) {
            if (this.mUninstalledInstantApps == null) {
                this.mUninstalledInstantApps = new SparseArray();
            }
            List<UninstalledInstantAppState> uninstalledAppStates = (List) this.mUninstalledInstantApps.get(userId);
            if (uninstalledAppStates == null) {
                uninstalledAppStates = new ArrayList();
                this.mUninstalledInstantApps.put(userId, uninstalledAppStates);
            }
            uninstalledAppStates.add(new UninstalledInstantAppState(uninstalledApp, System.currentTimeMillis()));
            writeUninstalledInstantAppMetadata(uninstalledApp, userId);
            writeInstantApplicationIconLPw(pkg, userId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0085 A:{SYNTHETIC, Splitter: B:30:0x0085} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0098 A:{Catch:{ Exception -> 0x008b }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008a A:{SYNTHETIC, Splitter: B:33:0x008a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeInstantApplicationIconLPw(Package pkg, int userId) {
        Exception e;
        Throwable th;
        if (getInstantApplicationDir(pkg.packageName, userId).exists()) {
            Bitmap bitmap;
            Drawable icon = pkg.applicationInfo.loadIcon(this.mService.mContext.getPackageManager());
            if (icon instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) icon).getBitmap();
            } else {
                bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                icon.draw(canvas);
            }
            Throwable th2 = null;
            FileOutputStream out = null;
            try {
                FileOutputStream out2 = new FileOutputStream(new File(getInstantApplicationDir(pkg.packageName, userId), INSTANT_APP_ICON_FILE));
                try {
                    bitmap.compress(CompressFormat.PNG, 100, out2);
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (Exception e2) {
                            e = e2;
                            out = out2;
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (Exception e3) {
                            e = e3;
                            Slog.e(LOG_TAG, "Error writing instant app icon", e);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (out != null) {
                }
                if (th2 == null) {
                }
            }
        }
    }

    public void deleteInstantApplicationMetadataLPw(String packageName, int userId) {
        if (packageName != null) {
            removeUninstalledInstantAppStateLPw(new AnonymousClass1(packageName), userId);
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
            List<UninstalledInstantAppState> uninstalledAppStates = (List) this.mUninstalledInstantApps.get(userId);
            if (uninstalledAppStates != null) {
                for (int i = uninstalledAppStates.size() - 1; i >= 0; i--) {
                    if (criteria.test((UninstalledInstantAppState) uninstalledAppStates.get(i))) {
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

    void pruneInstantApps() {
        try {
            pruneInstantApps(JobStatus.NO_LATEST_RUNTIME, Global.getLong(this.mService.mContext.getContentResolver(), "installed_instant_app_max_cache_period", 15552000000L), Global.getLong(this.mService.mContext.getContentResolver(), "uninstalled_instant_app_max_cache_period", 15552000000L));
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning installed and uninstalled instant apps", e);
        }
    }

    boolean pruneInstalledInstantApps(long neededSpace, long maxInstalledCacheDuration) {
        try {
            return pruneInstantApps(neededSpace, maxInstalledCacheDuration, JobStatus.NO_LATEST_RUNTIME);
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning installed instant apps", e);
            return false;
        }
    }

    boolean pruneUninstalledInstantApps(long neededSpace, long maxUninstalledCacheDuration) {
        try {
            return pruneInstantApps(neededSpace, JobStatus.NO_LATEST_RUNTIME, maxUninstalledCacheDuration);
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error pruning uninstalled instant apps", e);
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:37:0x00d8, code:
            if (r21 == null) goto L_0x00ea;
     */
    /* JADX WARNING: Missing block: B:39:?, code:
            r21.sort(new com.android.server.pm.-$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw(r37));
     */
    /* JADX WARNING: Missing block: B:41:0x00eb, code:
            if (r21 == null) goto L_0x012e;
     */
    /* JADX WARNING: Missing block: B:42:0x00ed, code:
            r18 = r21.size();
            r11 = 0;
     */
    /* JADX WARNING: Missing block: B:44:0x00f4, code:
            if (r11 >= r18) goto L_0x012e;
     */
    /* JADX WARNING: Missing block: B:46:0x011e, code:
            if (r37.mService.deletePackageX((java.lang.String) r21.get(r11), -1, 0, 2) != 1) goto L_0x012b;
     */
    /* JADX WARNING: Missing block: B:48:0x0126, code:
            if (r7.getUsableSpace() < r38) goto L_0x012b;
     */
    /* JADX WARNING: Missing block: B:50:0x012a, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:51:0x012b, code:
            r11 = r11 + 1;
     */
    /* JADX WARNING: Missing block: B:52:0x012e, code:
            r28 = r37.mService.mPackages;
     */
    /* JADX WARNING: Missing block: B:53:0x013a, code:
            monitor-enter(r28);
     */
    /* JADX WARNING: Missing block: B:55:?, code:
            r29 = com.android.server.pm.UserManagerService.getInstance().getUserIds();
            r26 = 0;
            r30 = r29.length;
     */
    /* JADX WARNING: Missing block: B:56:0x014c, code:
            r27 = r26;
     */
    /* JADX WARNING: Missing block: B:57:0x0150, code:
            if (r27 >= r30) goto L_0x01be;
     */
    /* JADX WARNING: Missing block: B:58:0x0152, code:
            r25 = r29[r27];
            removeUninstalledInstantAppStateLPw(new com.android.server.pm.-$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw.AnonymousClass3(r42), r25);
            r13 = getInstantApplicationsDir(r25);
     */
    /* JADX WARNING: Missing block: B:59:0x016e, code:
            if (r13.exists() != false) goto L_0x0175;
     */
    /* JADX WARNING: Missing block: B:60:0x0170, code:
            r26 = r27 + 1;
     */
    /* JADX WARNING: Missing block: B:61:0x0175, code:
            r10 = r13.listFiles();
     */
    /* JADX WARNING: Missing block: B:62:0x0179, code:
            if (r10 == null) goto L_0x0170;
     */
    /* JADX WARNING: Missing block: B:63:0x017b, code:
            r26 = 0;
            r31 = r10.length;
     */
    /* JADX WARNING: Missing block: B:65:0x0184, code:
            if (r26 >= r31) goto L_0x0170;
     */
    /* JADX WARNING: Missing block: B:66:0x0186, code:
            r14 = r10[r26];
     */
    /* JADX WARNING: Missing block: B:67:0x018c, code:
            if (r14.isDirectory() != false) goto L_0x0191;
     */
    /* JADX WARNING: Missing block: B:68:0x018e, code:
            r26 = r26 + 1;
     */
    /* JADX WARNING: Missing block: B:69:0x0191, code:
            r15 = new java.io.File(r14, INSTANT_APP_METADATA_FILE);
     */
    /* JADX WARNING: Missing block: B:70:0x019f, code:
            if (r15.exists() == false) goto L_0x018e;
     */
    /* JADX WARNING: Missing block: B:72:0x01ad, code:
            if ((java.lang.System.currentTimeMillis() - r15.lastModified()) <= r42) goto L_0x018e;
     */
    /* JADX WARNING: Missing block: B:73:0x01af, code:
            deleteDir(r14);
     */
    /* JADX WARNING: Missing block: B:75:0x01b8, code:
            if (r7.getUsableSpace() < r38) goto L_0x018e;
     */
    /* JADX WARNING: Missing block: B:77:0x01bc, code:
            monitor-exit(r28);
     */
    /* JADX WARNING: Missing block: B:78:0x01bd, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:79:0x01be, code:
            monitor-exit(r28);
     */
    /* JADX WARNING: Missing block: B:81:0x01c1, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean pruneInstantApps(long neededSpace, long maxInstalledCacheDuration, long maxUninstalledCacheDuration) throws IOException {
        Throwable th;
        File file = ((StorageManager) this.mService.mContext.getSystemService(StorageManager.class)).findPathForUuid(StorageManager.UUID_PRIVATE_INTERNAL);
        if (file.getUsableSpace() >= neededSpace) {
            return true;
        }
        List<String> packagesToDelete = null;
        long now = System.currentTimeMillis();
        synchronized (this.mService.mPackages) {
            try {
                int[] allUsers = PackageManagerService.sUserManager.getUserIds();
                int packageCount = this.mService.mPackages.size();
                int i = 0;
                while (true) {
                    List<String> packagesToDelete2 = packagesToDelete;
                    if (i >= packageCount) {
                        break;
                    }
                    try {
                        Package pkg = (Package) this.mService.mPackages.valueAt(i);
                        if (now - pkg.getLatestPackageUseTimeInMills() < maxInstalledCacheDuration) {
                            packagesToDelete = packagesToDelete2;
                        } else if (pkg.mExtras instanceof PackageSetting) {
                            PackageSetting ps = pkg.mExtras;
                            boolean installedOnlyAsInstantApp = false;
                            for (int userId : allUsers) {
                                if (ps.getInstalled(userId)) {
                                    if (!ps.getInstantApp(userId)) {
                                        installedOnlyAsInstantApp = false;
                                        break;
                                    }
                                    installedOnlyAsInstantApp = true;
                                }
                            }
                            if (installedOnlyAsInstantApp) {
                                if (packagesToDelete2 == null) {
                                    packagesToDelete = new ArrayList();
                                } else {
                                    packagesToDelete = packagesToDelete2;
                                }
                                packagesToDelete.add(pkg.packageName);
                            } else {
                                packagesToDelete = packagesToDelete2;
                            }
                        } else {
                            packagesToDelete = packagesToDelete2;
                        }
                        i++;
                    } catch (Throwable th2) {
                        th = th2;
                        packagesToDelete = packagesToDelete2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    /* synthetic */ int lambda$-com_android_server_pm_InstantAppRegistry_26684(String lhs, String rhs) {
        Package lhsPkg = (Package) this.mService.mPackages.get(lhs);
        Package rhsPkg = (Package) this.mService.mPackages.get(rhs);
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
        return ((PackageSetting) lhsPkg.mExtras).firstInstallTime > ((PackageSetting) rhsPkg.mExtras).firstInstallTime ? 1 : -1;
    }

    static /* synthetic */ boolean lambda$-com_android_server_pm_InstantAppRegistry_29374(long maxUninstalledCacheDuration, UninstalledInstantAppState state) {
        return System.currentTimeMillis() - state.mTimestamp > maxUninstalledCacheDuration;
    }

    private List<InstantAppInfo> getInstalledInstantApplicationsLPr(int userId) {
        List<InstantAppInfo> result = null;
        int packageCount = this.mService.mPackages.size();
        for (int i = 0; i < packageCount; i++) {
            Package pkg = (Package) this.mService.mPackages.valueAt(i);
            PackageSetting ps = pkg.mExtras;
            if (ps != null && (ps.getInstantApp(userId) ^ 1) == 0) {
                InstantAppInfo info = createInstantAppInfoForPackage(pkg, userId, true);
                if (info != null) {
                    if (result == null) {
                        result = new ArrayList();
                    }
                    result.add(info);
                }
            }
        }
        return result;
    }

    private InstantAppInfo createInstantAppInfoForPackage(Package pkg, int userId, boolean addApplicationInfo) {
        PackageSetting ps = pkg.mExtras;
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
            UninstalledInstantAppState uninstalledAppState = (UninstalledInstantAppState) uninstalledAppStates.get(i);
            if (uninstalledApps == null) {
                uninstalledApps = new ArrayList();
            }
            uninstalledApps.add(uninstalledAppState.mInstantAppInfo);
        }
        return uninstalledApps;
    }

    private void propagateInstantAppPermissionsIfNeeded(String packageName, int userId) {
        InstantAppInfo appInfo = peekOrParseUninstalledInstantAppInfo(packageName, userId);
        if (appInfo != null && !ArrayUtils.isEmpty(appInfo.getGrantedPermissions())) {
            long identity = Binder.clearCallingIdentity();
            try {
                for (String grantedPermission : appInfo.getGrantedPermissions()) {
                    BasePermission bp = (BasePermission) this.mService.mSettings.mPermissions.get(grantedPermission);
                    if (bp != null && ((bp.isRuntime() || bp.isDevelopment()) && bp.isInstant())) {
                        this.mService.grantRuntimePermission(packageName, grantedPermission, userId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private InstantAppInfo peekOrParseUninstalledInstantAppInfo(String packageName, int userId) {
        UninstalledInstantAppState uninstalledAppState;
        if (this.mUninstalledInstantApps != null) {
            List<UninstalledInstantAppState> uninstalledAppStates = (List) this.mUninstalledInstantApps.get(userId);
            if (uninstalledAppStates != null) {
                int appCount = uninstalledAppStates.size();
                for (int i = 0; i < appCount; i++) {
                    uninstalledAppState = (UninstalledInstantAppState) uninstalledAppStates.get(i);
                    if (uninstalledAppState.mInstantAppInfo.getPackageName().equals(packageName)) {
                        return uninstalledAppState.mInstantAppInfo;
                    }
                }
            }
        }
        uninstalledAppState = parseMetadataFile(new File(getInstantApplicationDir(packageName, userId), INSTANT_APP_METADATA_FILE));
        if (uninstalledAppState == null) {
            return null;
        }
        return uninstalledAppState.mInstantAppInfo;
    }

    private List<UninstalledInstantAppState> getUninstalledInstantAppStatesLPr(int userId) {
        List<UninstalledInstantAppState> uninstalledAppStates = null;
        if (this.mUninstalledInstantApps != null) {
            uninstalledAppStates = (List) this.mUninstalledInstantApps.get(userId);
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
                                uninstalledAppStates = new ArrayList();
                            }
                            uninstalledAppStates.add(uninstalledAppState);
                        }
                    }
                }
            }
        }
        if (uninstalledAppStates != null) {
            if (this.mUninstalledInstantApps == null) {
                this.mUninstalledInstantApps = new SparseArray();
            }
            this.mUninstalledInstantApps.put(userId, uninstalledAppStates);
        }
        return uninstalledAppStates;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0042 A:{Splitter: B:6:0x001d, ExcHandler: org.xmlpull.v1.XmlPullParserException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:13:0x0042, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:0x005c, code:
            throw new java.lang.IllegalStateException("Failed parsing instant metadata file: " + r11, r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            } catch (Exception e) {
            } catch (Throwable th) {
                IoUtils.closeQuietly(in);
            }
        } catch (FileNotFoundException e2) {
            Slog.i(LOG_TAG, "No instant metadata file");
            return null;
        }
    }

    private static File computeInstantCookieFile(Package pkg, int userId) {
        return new File(getInstantApplicationDir(pkg.packageName, userId), INSTANT_APP_COOKIE_FILE_PREFIX + PackageUtils.computeSha256Digest(pkg.mSignatures[0].toByteArray()) + INSTANT_APP_COOKIE_FILE_SIFFIX);
    }

    private static File peekInstantCookieFile(String packageName, int userId) {
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
        List<String> outRequestedPermissions = new ArrayList();
        List<String> outGrantedPermissions = new ArrayList();
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
                String permission = XmlUtils.readStringAttribute(parser, ATTR_NAME);
                outRequestedPermissions.add(permission);
                if (XmlUtils.readBooleanAttribute(parser, ATTR_GRANTED)) {
                    outGrantedPermissions.add(permission);
                }
            }
        }
    }

    private void writeUninstalledInstantAppMetadata(InstantAppInfo instantApp, int userId) {
        File appDir = getInstantApplicationDir(instantApp.getPackageName(), userId);
        if (appDir.exists() || (appDir.mkdirs() ^ 1) == 0) {
            AtomicFile destination = new AtomicFile(new File(appDir, INSTANT_APP_METADATA_FILE));
            AutoCloseable autoCloseable = null;
            try {
                autoCloseable = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(autoCloseable, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, Boolean.valueOf(true));
                serializer.startTag(null, "package");
                serializer.attribute(null, ATTR_LABEL, instantApp.loadLabel(this.mService.mContext.getPackageManager()).toString());
                serializer.startTag(null, TAG_PERMISSIONS);
                for (String permission : instantApp.getRequestedPermissions()) {
                    serializer.startTag(null, TAG_PERMISSION);
                    serializer.attribute(null, ATTR_NAME, permission);
                    if (ArrayUtils.contains(instantApp.getGrantedPermissions(), permission)) {
                        serializer.attribute(null, ATTR_GRANTED, String.valueOf(true));
                    }
                    serializer.endTag(null, TAG_PERMISSION);
                }
                serializer.endTag(null, TAG_PERMISSIONS);
                serializer.endTag(null, "package");
                serializer.endDocument();
                destination.finishWrite(autoCloseable);
            } catch (Throwable t) {
                Slog.wtf(LOG_TAG, "Failed to write instant state, restoring backup", t);
                destination.failWrite(null);
            } finally {
                IoUtils.closeQuietly(autoCloseable);
            }
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
