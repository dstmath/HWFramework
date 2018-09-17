package com.android.server.pm;

import android.content.ComponentName;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.pm.-$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ.AnonymousClass2;
import com.android.server.pm.-$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ.AnonymousClass3;
import com.android.server.pm.-$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ.AnonymousClass4;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;
import libcore.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class ShortcutUser {
    private static final String ATTR_KNOWN_LOCALES = "locales";
    private static final String ATTR_LAST_APP_SCAN_OS_FINGERPRINT = "last-app-scan-fp";
    private static final String ATTR_LAST_APP_SCAN_TIME = "last-app-scan-time2";
    private static final String ATTR_VALUE = "value";
    private static final String KEY_LAUNCHERS = "launchers";
    private static final String KEY_PACKAGES = "packages";
    private static final String KEY_USER_ID = "userId";
    private static final String TAG = "ShortcutService";
    private static final String TAG_LAUNCHER = "launcher";
    static final String TAG_ROOT = "user";
    private ComponentName mCachedLauncher;
    private String mKnownLocales;
    private String mLastAppScanOsFingerprint;
    private long mLastAppScanTime;
    private ComponentName mLastKnownLauncher;
    private final ArrayMap<PackageWithUser, ShortcutLauncher> mLaunchers = new ArrayMap();
    private final ArrayMap<String, ShortcutPackage> mPackages = new ArrayMap();
    final ShortcutService mService;
    private final int mUserId;

    static final class PackageWithUser {
        final String packageName;
        final int userId;

        private PackageWithUser(int userId, String packageName) {
            this.userId = userId;
            this.packageName = (String) Preconditions.checkNotNull(packageName);
        }

        public static PackageWithUser of(int userId, String packageName) {
            return new PackageWithUser(userId, packageName);
        }

        public static PackageWithUser of(ShortcutPackageItem spi) {
            return new PackageWithUser(spi.getPackageUserId(), spi.getPackageName());
        }

        public int hashCode() {
            return this.packageName.hashCode() ^ this.userId;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof PackageWithUser)) {
                return false;
            }
            PackageWithUser that = (PackageWithUser) obj;
            if (this.userId == that.userId) {
                z = this.packageName.equals(that.packageName);
            }
            return z;
        }

        public String toString() {
            return String.format("[Package: %d, %s]", new Object[]{Integer.valueOf(this.userId), this.packageName});
        }
    }

    public ShortcutUser(ShortcutService service, int userId) {
        this.mService = service;
        this.mUserId = userId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public long getLastAppScanTime() {
        return this.mLastAppScanTime;
    }

    public void setLastAppScanTime(long lastAppScanTime) {
        this.mLastAppScanTime = lastAppScanTime;
    }

    public String getLastAppScanOsFingerprint() {
        return this.mLastAppScanOsFingerprint;
    }

    public void setLastAppScanOsFingerprint(String lastAppScanOsFingerprint) {
        this.mLastAppScanOsFingerprint = lastAppScanOsFingerprint;
    }

    ArrayMap<String, ShortcutPackage> getAllPackagesForTest() {
        return this.mPackages;
    }

    public boolean hasPackage(String packageName) {
        return this.mPackages.containsKey(packageName);
    }

    private void addPackage(ShortcutPackage p) {
        p.replaceUser(this);
        this.mPackages.put(p.getPackageName(), p);
    }

    public ShortcutPackage removePackage(String packageName) {
        ShortcutPackage removed = (ShortcutPackage) this.mPackages.remove(packageName);
        this.mService.cleanupBitmapsForPackage(this.mUserId, packageName);
        return removed;
    }

    ArrayMap<PackageWithUser, ShortcutLauncher> getAllLaunchersForTest() {
        return this.mLaunchers;
    }

    private void addLauncher(ShortcutLauncher launcher) {
        launcher.replaceUser(this);
        this.mLaunchers.put(PackageWithUser.of(launcher.getPackageUserId(), launcher.getPackageName()), launcher);
    }

    public ShortcutLauncher removeLauncher(int packageUserId, String packageName) {
        return (ShortcutLauncher) this.mLaunchers.remove(PackageWithUser.of(packageUserId, packageName));
    }

    public ShortcutPackage getPackageShortcutsIfExists(String packageName) {
        ShortcutPackage ret = (ShortcutPackage) this.mPackages.get(packageName);
        if (ret != null) {
            ret.lambda$-com_android_server_pm_ShortcutUser_11189();
        }
        return ret;
    }

    public ShortcutPackage getPackageShortcuts(String packageName) {
        ShortcutPackage ret = getPackageShortcutsIfExists(packageName);
        if (ret != null) {
            return ret;
        }
        ret = new ShortcutPackage(this, this.mUserId, packageName);
        this.mPackages.put(packageName, ret);
        return ret;
    }

    public ShortcutLauncher getLauncherShortcuts(String packageName, int launcherUserId) {
        PackageWithUser key = PackageWithUser.of(launcherUserId, packageName);
        ShortcutLauncher ret = (ShortcutLauncher) this.mLaunchers.get(key);
        if (ret == null) {
            ret = new ShortcutLauncher(this, this.mUserId, packageName, launcherUserId);
            this.mLaunchers.put(key, ret);
            return ret;
        }
        ret.lambda$-com_android_server_pm_ShortcutUser_11189();
        return ret;
    }

    public void forAllPackages(Consumer<? super ShortcutPackage> callback) {
        int size = this.mPackages.size();
        for (int i = 0; i < size; i++) {
            callback.accept(this.mPackages.valueAt(i));
        }
    }

    public void forAllLaunchers(Consumer<? super ShortcutLauncher> callback) {
        int size = this.mLaunchers.size();
        for (int i = 0; i < size; i++) {
            callback.accept(this.mLaunchers.valueAt(i));
        }
    }

    public void forAllPackageItems(Consumer<? super ShortcutPackageItem> callback) {
        forAllLaunchers(callback);
        forAllPackages(callback);
    }

    public void forPackageItem(String packageName, int packageUserId, Consumer<ShortcutPackageItem> callback) {
        forAllPackageItems(new AnonymousClass4(packageUserId, packageName, callback));
    }

    static /* synthetic */ void lambda$-com_android_server_pm_ShortcutUser_8403(int packageUserId, String packageName, Consumer callback, ShortcutPackageItem spi) {
        if (spi.getPackageUserId() == packageUserId && spi.getPackageName().equals(packageName)) {
            callback.accept(spi);
        }
    }

    public void onCalledByPublisher(String packageName) {
        lambda$-com_android_server_pm_ShortcutService_98230();
        rescanPackageIfNeeded(packageName, false);
    }

    private String getKnownLocales() {
        if (TextUtils.isEmpty(this.mKnownLocales)) {
            this.mKnownLocales = this.mService.injectGetLocaleTagsForUser(this.mUserId);
            this.mService.scheduleSaveUser(this.mUserId);
        }
        return this.mKnownLocales;
    }

    /* renamed from: detectLocaleChange */
    public void lambda$-com_android_server_pm_ShortcutService_98230() {
        String currentLocales = this.mService.injectGetLocaleTagsForUser(this.mUserId);
        if (!getKnownLocales().equals(currentLocales)) {
            this.mKnownLocales = currentLocales;
            forAllPackages(new Consumer() {
                public final void accept(Object obj) {
                    $m$0(obj);
                }
            });
            this.mService.scheduleSaveUser(this.mUserId);
        }
    }

    static /* synthetic */ void lambda$-com_android_server_pm_ShortcutUser_10419(ShortcutPackage pkg) {
        pkg.resetRateLimiting();
        pkg.resolveResourceStrings();
    }

    public void rescanPackageIfNeeded(String packageName, boolean forceRescan) {
        boolean isNewApp = this.mPackages.containsKey(packageName) ^ 1;
        if (!getPackageShortcuts(packageName).rescanPackageIfNeeded(isNewApp, forceRescan) && isNewApp) {
            this.mPackages.remove(packageName);
        }
    }

    public void attemptToRestoreIfNeededAndSave(ShortcutService s, String packageName, int packageUserId) {
        forPackageItem(packageName, packageUserId, new -$Lambda$QyqU-JNCRoGQda4e7us8wqygKfQ());
    }

    public void saveToXml(XmlSerializer out, boolean forBackup) throws IOException, XmlPullParserException {
        int i;
        out.startTag(null, TAG_ROOT);
        if (!forBackup) {
            ShortcutService.writeAttr(out, ATTR_KNOWN_LOCALES, this.mKnownLocales);
            ShortcutService.writeAttr(out, ATTR_LAST_APP_SCAN_TIME, this.mLastAppScanTime);
            ShortcutService.writeAttr(out, ATTR_LAST_APP_SCAN_OS_FINGERPRINT, this.mLastAppScanOsFingerprint);
            ShortcutService.writeTagValue(out, TAG_LAUNCHER, this.mLastKnownLauncher);
        }
        int size = this.mLaunchers.size();
        for (i = 0; i < size; i++) {
            saveShortcutPackageItem(out, (ShortcutPackageItem) this.mLaunchers.valueAt(i), forBackup);
        }
        size = this.mPackages.size();
        for (i = 0; i < size; i++) {
            saveShortcutPackageItem(out, (ShortcutPackageItem) this.mPackages.valueAt(i), forBackup);
        }
        out.endTag(null, TAG_ROOT);
    }

    private void saveShortcutPackageItem(XmlSerializer out, ShortcutPackageItem spi, boolean forBackup) throws IOException, XmlPullParserException {
        if (!forBackup || (this.mService.shouldBackupApp(spi.getPackageName(), spi.getPackageUserId()) && spi.getPackageUserId() == spi.getOwnerUserId())) {
            spi.saveToXml(out, forBackup);
        }
    }

    public static ShortcutUser loadFromXml(ShortcutService s, XmlPullParser parser, int userId, boolean fromBackup) throws IOException, XmlPullParserException, InvalidFileFormatException {
        ShortcutUser ret = new ShortcutUser(s, userId);
        try {
            ret.mKnownLocales = ShortcutService.parseStringAttribute(parser, ATTR_KNOWN_LOCALES);
            long lastAppScanTime = ShortcutService.parseLongAttribute(parser, ATTR_LAST_APP_SCAN_TIME);
            if (lastAppScanTime >= s.injectCurrentTimeMillis()) {
                lastAppScanTime = 0;
            }
            ret.mLastAppScanTime = lastAppScanTime;
            ret.mLastAppScanOsFingerprint = ShortcutService.parseStringAttribute(parser, ATTR_LAST_APP_SCAN_OS_FINGERPRINT);
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    return ret;
                }
                if (type == 2) {
                    int depth = parser.getDepth();
                    String tag = parser.getName();
                    if (depth == outerDepth + 1) {
                        if (tag.equals(TAG_LAUNCHER)) {
                            ret.mLastKnownLauncher = ShortcutService.parseComponentNameAttribute(parser, ATTR_VALUE);
                        } else if (tag.equals(HwBroadcastRadarUtil.KEY_PACKAGE)) {
                            ShortcutPackage shortcuts = ShortcutPackage.loadFromXml(s, ret, parser, fromBackup);
                            ret.mPackages.put(shortcuts.getPackageName(), shortcuts);
                        } else if (tag.equals("launcher-pins")) {
                            ret.addLauncher(ShortcutLauncher.loadFromXml(parser, ret, userId, fromBackup));
                        }
                    }
                    ShortcutService.warnForInvalidTag(depth, tag);
                }
            }
            return ret;
        } catch (RuntimeException e) {
            throw new InvalidFileFormatException("Unable to parse file", e);
        }
    }

    public ComponentName getLastKnownLauncher() {
        return this.mLastKnownLauncher;
    }

    public void setLauncher(ComponentName launcherComponent) {
        setLauncher(launcherComponent, false);
    }

    public void clearLauncher() {
        setLauncher(null);
    }

    public void forceClearLauncher() {
        setLauncher(null, true);
    }

    private void setLauncher(ComponentName launcherComponent, boolean allowPurgeLastKnown) {
        this.mCachedLauncher = launcherComponent;
        if (!Objects.equal(this.mLastKnownLauncher, launcherComponent)) {
            if (allowPurgeLastKnown || launcherComponent != null) {
                this.mLastKnownLauncher = launcherComponent;
                this.mService.scheduleSaveUser(this.mUserId);
            }
        }
    }

    public ComponentName getCachedLauncher() {
        return this.mCachedLauncher;
    }

    public void resetThrottling() {
        for (int i = this.mPackages.size() - 1; i >= 0; i--) {
            ((ShortcutPackage) this.mPackages.valueAt(i)).resetThrottling();
        }
    }

    public void mergeRestoredFile(ShortcutUser restored) {
        ShortcutService s = this.mService;
        this.mLaunchers.clear();
        restored.forAllLaunchers(new AnonymousClass2(this, s));
        restored.forAllPackages(new AnonymousClass3(this, s));
        restored.mLaunchers.clear();
        restored.mPackages.clear();
    }

    /* synthetic */ void lambda$-com_android_server_pm_ShortcutUser_18230(ShortcutService s, ShortcutLauncher sl) {
        if (!s.isPackageInstalled(sl.getPackageName(), getUserId()) || (s.shouldBackupApp(sl.getPackageName(), getUserId()) ^ 1) == 0) {
            addLauncher(sl);
        }
    }

    /* synthetic */ void lambda$-com_android_server_pm_ShortcutUser_18617(ShortcutService s, ShortcutPackage sp) {
        if (!s.isPackageInstalled(sp.getPackageName(), getUserId()) || (s.shouldBackupApp(sp.getPackageName(), getUserId()) ^ 1) == 0) {
            ShortcutPackage previous = getPackageShortcutsIfExists(sp.getPackageName());
            if (previous != null && previous.hasNonManifestShortcuts()) {
                Log.w(TAG, "Shortcuts for package " + sp.getPackageName() + " are being restored." + " Existing non-manifeset shortcuts will be overwritten.");
            }
            addPackage(sp);
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        int i;
        pw.print(prefix);
        pw.print("User: ");
        pw.print(this.mUserId);
        pw.print("  Known locales: ");
        pw.print(this.mKnownLocales);
        pw.print("  Last app scan: [");
        pw.print(this.mLastAppScanTime);
        pw.print("] ");
        pw.print(ShortcutService.formatTime(this.mLastAppScanTime));
        pw.print("  Last app scan FP: ");
        pw.print(this.mLastAppScanOsFingerprint);
        pw.println();
        prefix = prefix + prefix + "  ";
        pw.print(prefix);
        pw.print("Cached launcher: ");
        pw.print(this.mCachedLauncher);
        pw.println();
        pw.print(prefix);
        pw.print("Last known launcher: ");
        pw.print(this.mLastKnownLauncher);
        pw.println();
        for (i = 0; i < this.mLaunchers.size(); i++) {
            ((ShortcutLauncher) this.mLaunchers.valueAt(i)).dump(pw, prefix);
        }
        for (i = 0; i < this.mPackages.size(); i++) {
            ((ShortcutPackage) this.mPackages.valueAt(i)).dump(pw, prefix);
        }
        pw.println();
        pw.print(prefix);
        pw.println("Bitmap directories: ");
        dumpDirectorySize(pw, prefix + "  ", this.mService.getUserBitmapFilePath(this.mUserId));
    }

    private void dumpDirectorySize(PrintWriter pw, String prefix, File path) {
        int numFiles = 0;
        long size = 0;
        if (path.listFiles() != null) {
            for (File child : path.listFiles()) {
                if (child.isFile()) {
                    numFiles++;
                    size += child.length();
                } else if (child.isDirectory()) {
                    dumpDirectorySize(pw, prefix + "  ", child);
                }
            }
        }
        pw.print(prefix);
        pw.print("Path: ");
        pw.print(path.getName());
        pw.print("/ has ");
        pw.print(numFiles);
        pw.print(" files, size=");
        pw.print(size);
        pw.print(" (");
        pw.print(Formatter.formatFileSize(this.mService.mContext, size));
        pw.println(")");
    }

    public JSONObject dumpCheckin(boolean clear) throws JSONException {
        int i;
        JSONObject result = new JSONObject();
        result.put(KEY_USER_ID, this.mUserId);
        JSONArray launchers = new JSONArray();
        for (i = 0; i < this.mLaunchers.size(); i++) {
            launchers.put(((ShortcutLauncher) this.mLaunchers.valueAt(i)).dumpCheckin(clear));
        }
        result.put(KEY_LAUNCHERS, launchers);
        JSONArray packages = new JSONArray();
        for (i = 0; i < this.mPackages.size(); i++) {
            packages.put(((ShortcutPackage) this.mPackages.valueAt(i)).dumpCheckin(clear));
        }
        result.put(KEY_PACKAGES, packages);
        return result;
    }
}
