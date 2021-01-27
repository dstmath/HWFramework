package com.android.server.pm;

import android.content.ComponentName;
import android.metrics.LogMaker;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import com.android.server.pm.ShortcutService;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public class ShortcutUser {
    private static final String ATTR_KNOWN_LOCALES = "locales";
    private static final String ATTR_LAST_APP_SCAN_OS_FINGERPRINT = "last-app-scan-fp";
    private static final String ATTR_LAST_APP_SCAN_TIME = "last-app-scan-time2";
    private static final String ATTR_RESTORE_SOURCE_FINGERPRINT = "restore-from-fp";
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
    private final ArrayMap<PackageWithUser, ShortcutLauncher> mLaunchers = new ArrayMap<>();
    private final ArrayMap<String, ShortcutPackage> mPackages = new ArrayMap<>();
    private String mRestoreFromOsFingerprint;
    final ShortcutService mService;
    private final int mUserId;

    /* access modifiers changed from: package-private */
    public static final class PackageWithUser {
        final String packageName;
        final int userId;

        private PackageWithUser(int userId2, String packageName2) {
            this.userId = userId2;
            this.packageName = (String) Preconditions.checkNotNull(packageName2);
        }

        public static PackageWithUser of(int userId2, String packageName2) {
            return new PackageWithUser(userId2, packageName2);
        }

        public static PackageWithUser of(ShortcutPackageItem spi) {
            return new PackageWithUser(spi.getPackageUserId(), spi.getPackageName());
        }

        public int hashCode() {
            return this.packageName.hashCode() ^ this.userId;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof PackageWithUser)) {
                return false;
            }
            PackageWithUser that = (PackageWithUser) obj;
            if (this.userId != that.userId || !this.packageName.equals(that.packageName)) {
                return false;
            }
            return true;
        }

        public String toString() {
            return String.format("[Package: %d, %s]", Integer.valueOf(this.userId), this.packageName);
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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ArrayMap<String, ShortcutPackage> getAllPackagesForTest() {
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
        ShortcutPackage removed = this.mPackages.remove(packageName);
        this.mService.cleanupBitmapsForPackage(this.mUserId, packageName);
        return removed;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ArrayMap<PackageWithUser, ShortcutLauncher> getAllLaunchersForTest() {
        return this.mLaunchers;
    }

    private void addLauncher(ShortcutLauncher launcher) {
        launcher.replaceUser(this);
        this.mLaunchers.put(PackageWithUser.of(launcher.getPackageUserId(), launcher.getPackageName()), launcher);
    }

    public ShortcutLauncher removeLauncher(int packageUserId, String packageName) {
        return this.mLaunchers.remove(PackageWithUser.of(packageUserId, packageName));
    }

    public ShortcutPackage getPackageShortcutsIfExists(String packageName) {
        ShortcutPackage ret = this.mPackages.get(packageName);
        if (ret != null) {
            ret.attemptToRestoreIfNeededAndSave();
        }
        return ret;
    }

    public ShortcutPackage getPackageShortcuts(String packageName) {
        ShortcutPackage ret = getPackageShortcutsIfExists(packageName);
        if (ret != null) {
            return ret;
        }
        ShortcutPackage ret2 = new ShortcutPackage(this, this.mUserId, packageName);
        this.mPackages.put(packageName, ret2);
        return ret2;
    }

    public ShortcutLauncher getLauncherShortcuts(String packageName, int launcherUserId) {
        PackageWithUser key = PackageWithUser.of(launcherUserId, packageName);
        ShortcutLauncher ret = this.mLaunchers.get(key);
        if (ret == null) {
            ShortcutLauncher ret2 = new ShortcutLauncher(this, this.mUserId, packageName, launcherUserId);
            this.mLaunchers.put(key, ret2);
            return ret2;
        }
        ret.attemptToRestoreIfNeededAndSave();
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
        forAllPackageItems(new Consumer(packageUserId, packageName, callback) {
            /* class com.android.server.pm.$$Lambda$ShortcutUser$XHWlvjfCvG1SoVwGHi3envhmtfM */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ Consumer f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ShortcutUser.lambda$forPackageItem$0(this.f$0, this.f$1, this.f$2, (ShortcutPackageItem) obj);
            }
        });
    }

    static /* synthetic */ void lambda$forPackageItem$0(int packageUserId, String packageName, Consumer callback, ShortcutPackageItem spi) {
        if (spi.getPackageUserId() == packageUserId && spi.getPackageName().equals(packageName)) {
            callback.accept(spi);
        }
    }

    public void onCalledByPublisher(String packageName) {
        detectLocaleChange();
        rescanPackageIfNeeded(packageName, false);
    }

    private String getKnownLocales() {
        if (TextUtils.isEmpty(this.mKnownLocales)) {
            this.mKnownLocales = this.mService.injectGetLocaleTagsForUser(this.mUserId);
            this.mService.scheduleSaveUser(this.mUserId);
        }
        return this.mKnownLocales;
    }

    public void detectLocaleChange() {
        String currentLocales = this.mService.injectGetLocaleTagsForUser(this.mUserId);
        if (TextUtils.isEmpty(this.mKnownLocales) || !this.mKnownLocales.equals(currentLocales)) {
            this.mKnownLocales = currentLocales;
            forAllPackages($$Lambda$ShortcutUser$6rBk7xJFaM9dXyyKHFsDCus0iM.INSTANCE);
            this.mService.scheduleSaveUser(this.mUserId);
        }
    }

    static /* synthetic */ void lambda$detectLocaleChange$1(ShortcutPackage pkg) {
        pkg.resetRateLimiting();
        pkg.resolveResourceStrings();
    }

    public void rescanPackageIfNeeded(String packageName, boolean forceRescan) {
        boolean isNewApp = !this.mPackages.containsKey(packageName);
        if (!getPackageShortcuts(packageName).rescanPackageIfNeeded(isNewApp, forceRescan) && isNewApp) {
            this.mPackages.remove(packageName);
        }
    }

    public void attemptToRestoreIfNeededAndSave(ShortcutService s, String packageName, int packageUserId) {
        forPackageItem(packageName, packageUserId, $$Lambda$ShortcutUser$bsc89E_40a5X2amehalpqawQ5hY.INSTANCE);
    }

    public void saveToXml(XmlSerializer out, boolean forBackup) throws IOException, XmlPullParserException {
        out.startTag(null, TAG_ROOT);
        if (!forBackup) {
            ShortcutService.writeAttr(out, ATTR_KNOWN_LOCALES, this.mKnownLocales);
            ShortcutService.writeAttr(out, ATTR_LAST_APP_SCAN_TIME, this.mLastAppScanTime);
            ShortcutService.writeAttr(out, ATTR_LAST_APP_SCAN_OS_FINGERPRINT, this.mLastAppScanOsFingerprint);
            ShortcutService.writeAttr(out, ATTR_RESTORE_SOURCE_FINGERPRINT, this.mRestoreFromOsFingerprint);
            ShortcutService.writeTagValue(out, TAG_LAUNCHER, this.mLastKnownLauncher);
        } else {
            ShortcutService.writeAttr(out, ATTR_RESTORE_SOURCE_FINGERPRINT, this.mService.injectBuildFingerprint());
        }
        int size = this.mLaunchers.size();
        for (int i = 0; i < size; i++) {
            saveShortcutPackageItem(out, this.mLaunchers.valueAt(i), forBackup);
        }
        int size2 = this.mPackages.size();
        for (int i2 = 0; i2 < size2; i2++) {
            saveShortcutPackageItem(out, this.mPackages.valueAt(i2), forBackup);
        }
        out.endTag(null, TAG_ROOT);
    }

    private void saveShortcutPackageItem(XmlSerializer out, ShortcutPackageItem spi, boolean forBackup) throws IOException, XmlPullParserException {
        if (!forBackup || spi.getPackageUserId() == spi.getOwnerUserId()) {
            spi.saveToXml(out, forBackup);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x009b A[Catch:{ RuntimeException -> 0x00c9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00b8 A[Catch:{ RuntimeException -> 0x00c9 }] */
    public static ShortcutUser loadFromXml(ShortcutService s, XmlPullParser parser, int userId, boolean fromBackup) throws IOException, XmlPullParserException, ShortcutService.InvalidFileFormatException {
        int outerDepth;
        char c;
        ShortcutUser ret = new ShortcutUser(s, userId);
        try {
            ret.mKnownLocales = ShortcutService.parseStringAttribute(parser, ATTR_KNOWN_LOCALES);
            long lastAppScanTime = ShortcutService.parseLongAttribute(parser, ATTR_LAST_APP_SCAN_TIME);
            ret.mLastAppScanTime = lastAppScanTime < s.injectCurrentTimeMillis() ? lastAppScanTime : 0;
            ret.mLastAppScanOsFingerprint = ShortcutService.parseStringAttribute(parser, ATTR_LAST_APP_SCAN_OS_FINGERPRINT);
            ret.mRestoreFromOsFingerprint = ShortcutService.parseStringAttribute(parser, ATTR_RESTORE_SOURCE_FINGERPRINT);
            outerDepth = parser.getDepth();
            return ret;
        } catch (RuntimeException e) {
            throw new ShortcutService.InvalidFileFormatException("Unable to parse file", e);
        }
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (type == 2) {
                int depth = parser.getDepth();
                String tag = parser.getName();
                if (depth == outerDepth + 1) {
                    int hashCode = tag.hashCode();
                    if (hashCode != -1407250528) {
                        if (hashCode != -1146595445) {
                            if (hashCode == -807062458 && tag.equals("package")) {
                                c = 1;
                                if (c != 0) {
                                    ret.mLastKnownLauncher = ShortcutService.parseComponentNameAttribute(parser, ATTR_VALUE);
                                } else if (c == 1) {
                                    ShortcutPackage shortcuts = ShortcutPackage.loadFromXml(s, ret, parser, fromBackup);
                                    ret.mPackages.put(shortcuts.getPackageName(), shortcuts);
                                } else if (c == 2) {
                                    ret.addLauncher(ShortcutLauncher.loadFromXml(parser, ret, userId, fromBackup));
                                }
                            }
                        } else if (tag.equals("launcher-pins")) {
                            c = 2;
                            if (c != 0) {
                            }
                        }
                    } else if (tag.equals(TAG_LAUNCHER)) {
                        c = 0;
                        if (c != 0) {
                        }
                    }
                    c = 65535;
                    if (c != 0) {
                    }
                }
                ShortcutService.warnForInvalidTag(depth, tag);
            }
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
        if (!Objects.equals(this.mLastKnownLauncher, launcherComponent)) {
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
            this.mPackages.valueAt(i).resetThrottling();
        }
    }

    public void mergeRestoredFile(ShortcutUser restored) {
        ShortcutService s = this.mService;
        int[] restoredLaunchers = new int[1];
        int[] restoredPackages = new int[1];
        int[] restoredShortcuts = new int[1];
        this.mLaunchers.clear();
        restored.forAllLaunchers(new Consumer(s, restoredLaunchers) {
            /* class com.android.server.pm.$$Lambda$ShortcutUser$zwhAnw7NjAOfNphKSeWurjAD6OM */
            private final /* synthetic */ ShortcutService f$1;
            private final /* synthetic */ int[] f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ShortcutUser.this.lambda$mergeRestoredFile$3$ShortcutUser(this.f$1, this.f$2, (ShortcutLauncher) obj);
            }
        });
        restored.forAllPackages(new Consumer(s, restoredPackages, restoredShortcuts) {
            /* class com.android.server.pm.$$Lambda$ShortcutUser$078_3k15h1rTyJTkYAHYqf5ltYg */
            private final /* synthetic */ ShortcutService f$1;
            private final /* synthetic */ int[] f$2;
            private final /* synthetic */ int[] f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ShortcutUser.this.lambda$mergeRestoredFile$4$ShortcutUser(this.f$1, this.f$2, this.f$3, (ShortcutPackage) obj);
            }
        });
        restored.mLaunchers.clear();
        restored.mPackages.clear();
        this.mRestoreFromOsFingerprint = restored.mRestoreFromOsFingerprint;
        Slog.i(TAG, "Restored: L=" + restoredLaunchers[0] + " P=" + restoredPackages[0] + " S=" + restoredShortcuts[0]);
    }

    public /* synthetic */ void lambda$mergeRestoredFile$3$ShortcutUser(ShortcutService s, int[] restoredLaunchers, ShortcutLauncher sl) {
        if (!s.isPackageInstalled(sl.getPackageName(), getUserId()) || s.shouldBackupApp(sl.getPackageName(), getUserId())) {
            addLauncher(sl);
            restoredLaunchers[0] = restoredLaunchers[0] + 1;
        }
    }

    public /* synthetic */ void lambda$mergeRestoredFile$4$ShortcutUser(ShortcutService s, int[] restoredPackages, int[] restoredShortcuts, ShortcutPackage sp) {
        if (!s.isPackageInstalled(sp.getPackageName(), getUserId()) || s.shouldBackupApp(sp.getPackageName(), getUserId())) {
            ShortcutPackage previous = getPackageShortcutsIfExists(sp.getPackageName());
            if (previous != null && previous.hasNonManifestShortcuts()) {
                Log.w(TAG, "Shortcuts for package " + sp.getPackageName() + " are being restored. Existing non-manifeset shortcuts will be overwritten.");
            }
            addPackage(sp);
            restoredPackages[0] = restoredPackages[0] + 1;
            restoredShortcuts[0] = restoredShortcuts[0] + sp.getShortcutCount();
        }
    }

    public void dump(PrintWriter pw, String prefix, ShortcutService.DumpFilter filter) {
        if (filter.shouldDumpDetails()) {
            pw.print(prefix);
            pw.print("User: ");
            pw.print(this.mUserId);
            pw.print("  Known locales: ");
            pw.print(this.mKnownLocales);
            pw.print("  Last app scan: [");
            pw.print(this.mLastAppScanTime);
            pw.print("] ");
            pw.println(ShortcutService.formatTime(this.mLastAppScanTime));
            prefix = prefix + prefix + "  ";
            pw.print(prefix);
            pw.print("Last app scan FP: ");
            pw.println(this.mLastAppScanOsFingerprint);
            pw.print(prefix);
            pw.print("Restore from FP: ");
            pw.print(this.mRestoreFromOsFingerprint);
            pw.println();
            pw.print(prefix);
            pw.print("Cached launcher: ");
            pw.print(this.mCachedLauncher);
            pw.println();
            pw.print(prefix);
            pw.print("Last known launcher: ");
            pw.print(this.mLastKnownLauncher);
            pw.println();
        }
        for (int i = 0; i < this.mLaunchers.size(); i++) {
            ShortcutLauncher launcher = this.mLaunchers.valueAt(i);
            if (filter.isPackageMatch(launcher.getPackageName())) {
                launcher.dump(pw, prefix, filter);
            }
        }
        for (int i2 = 0; i2 < this.mPackages.size(); i2++) {
            ShortcutPackage pkg = this.mPackages.valueAt(i2);
            if (filter.isPackageMatch(pkg.getPackageName())) {
                pkg.dump(pw, prefix, filter);
            }
        }
        if (filter.shouldDumpDetails()) {
            pw.println();
            pw.print(prefix);
            pw.println("Bitmap directories: ");
            dumpDirectorySize(pw, prefix + "  ", this.mService.getUserBitmapFilePath(this.mUserId));
        }
    }

    private void dumpDirectorySize(PrintWriter pw, String prefix, File path) {
        int numFiles = 0;
        long size = 0;
        if (path.listFiles() != null) {
            File[] listFiles = path.listFiles();
            for (File child : listFiles) {
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
        JSONObject result = new JSONObject();
        result.put(KEY_USER_ID, this.mUserId);
        JSONArray launchers = new JSONArray();
        for (int i = 0; i < this.mLaunchers.size(); i++) {
            launchers.put(this.mLaunchers.valueAt(i).dumpCheckin(clear));
        }
        result.put(KEY_LAUNCHERS, launchers);
        JSONArray packages = new JSONArray();
        for (int i2 = 0; i2 < this.mPackages.size(); i2++) {
            packages.put(this.mPackages.valueAt(i2).dumpCheckin(clear));
        }
        result.put(KEY_PACKAGES, packages);
        return result;
    }

    /* access modifiers changed from: package-private */
    public void logSharingShortcutStats(MetricsLogger logger) {
        int packageWithShareTargetsCount = 0;
        int totalSharingShortcutCount = 0;
        for (int i = 0; i < this.mPackages.size(); i++) {
            if (this.mPackages.valueAt(i).hasShareTargets()) {
                packageWithShareTargetsCount++;
                totalSharingShortcutCount += this.mPackages.valueAt(i).getSharingShortcutCount();
            }
        }
        LogMaker logMaker = new LogMaker(1717);
        logger.write(logMaker.setType(1).setSubtype(this.mUserId));
        logger.write(logMaker.setType(2).setSubtype(packageWithShareTargetsCount));
        logger.write(logMaker.setType(3).setSubtype(totalSharingShortcutCount));
    }
}
