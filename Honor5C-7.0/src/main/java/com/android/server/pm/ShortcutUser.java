package com.android.server.pm;

import android.content.ComponentName;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.internal.util.Preconditions;
import com.android.server.am.HwBroadcastRadarUtil;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;
import libcore.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class ShortcutUser {
    private static final String ATTR_KNOWN_LOCALE_CHANGE_SEQUENCE_NUMBER = "locale-seq-no";
    private static final String ATTR_VALUE = "value";
    private static final String TAG = "ShortcutService";
    private static final String TAG_LAUNCHER = "launcher";
    static final String TAG_ROOT = "user";
    private long mKnownLocaleChangeSequenceNumber;
    private ComponentName mLauncherComponent;
    private final ArrayMap<PackageWithUser, ShortcutLauncher> mLaunchers;
    private final ArrayMap<String, ShortcutPackage> mPackages;
    private final SparseArray<ShortcutPackage> mPackagesFromUid;
    private final int mUserId;

    final /* synthetic */ class -void_attemptToRestoreIfNeededAndSave_com_android_server_pm_ShortcutService_s_java_lang_String_packageName_int_packageUserId_LambdaImpl0 implements Consumer {
        private /* synthetic */ ShortcutService val$s;

        public /* synthetic */ -void_attemptToRestoreIfNeededAndSave_com_android_server_pm_ShortcutService_s_java_lang_String_packageName_int_packageUserId_LambdaImpl0(ShortcutService shortcutService) {
            this.val$s = shortcutService;
        }

        public void accept(Object arg0) {
            ((ShortcutPackageItem) arg0).attemptToRestoreIfNeededAndSave(this.val$s);
        }
    }

    final /* synthetic */ class -void_forPackageItem_java_lang_String_packageName_int_packageUserId_java_util_function_Consumer_callback_LambdaImpl0 implements Consumer {
        private /* synthetic */ Consumer val$callback;
        private /* synthetic */ String val$packageName;
        private /* synthetic */ int val$packageUserId;

        public /* synthetic */ -void_forPackageItem_java_lang_String_packageName_int_packageUserId_java_util_function_Consumer_callback_LambdaImpl0(int i, String str, Consumer consumer) {
            this.val$packageUserId = i;
            this.val$packageName = str;
            this.val$callback = consumer;
        }

        public void accept(Object arg0) {
            ShortcutUser.-com_android_server_pm_ShortcutUser_lambda$1(this.val$packageUserId, this.val$packageName, this.val$callback, (ShortcutPackageItem) arg0);
        }
    }

    final /* synthetic */ class -void_resetThrottlingIfNeeded_com_android_server_pm_ShortcutService_s_LambdaImpl0 implements Consumer {
        private /* synthetic */ ShortcutService val$s;

        public /* synthetic */ -void_resetThrottlingIfNeeded_com_android_server_pm_ShortcutService_s_LambdaImpl0(ShortcutService shortcutService) {
            this.val$s = shortcutService;
        }

        public void accept(Object arg0) {
            ((ShortcutPackage) arg0).resetRateLimiting(this.val$s);
        }
    }

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
            return String.format("{Package: %d, %s}", new Object[]{Integer.valueOf(this.userId), this.packageName});
        }
    }

    public ShortcutUser(int userId) {
        this.mPackages = new ArrayMap();
        this.mPackagesFromUid = new SparseArray();
        this.mLaunchers = new ArrayMap();
        this.mUserId = userId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    ArrayMap<String, ShortcutPackage> getAllPackagesForTest() {
        return this.mPackages;
    }

    public ShortcutPackage removePackage(ShortcutService s, String packageName) {
        ShortcutPackage removed = (ShortcutPackage) this.mPackages.remove(packageName);
        s.cleanupBitmapsForPackage(this.mUserId, packageName);
        return removed;
    }

    ArrayMap<PackageWithUser, ShortcutLauncher> getAllLaunchersForTest() {
        return this.mLaunchers;
    }

    public void addLauncher(ShortcutLauncher launcher) {
        this.mLaunchers.put(PackageWithUser.of(launcher.getPackageUserId(), launcher.getPackageName()), launcher);
    }

    public ShortcutLauncher removeLauncher(int packageUserId, String packageName) {
        return (ShortcutLauncher) this.mLaunchers.remove(PackageWithUser.of(packageUserId, packageName));
    }

    public ShortcutPackage getPackageShortcuts(ShortcutService s, String packageName) {
        ShortcutPackage ret = (ShortcutPackage) this.mPackages.get(packageName);
        if (ret == null) {
            ret = new ShortcutPackage(s, this, this.mUserId, packageName);
            this.mPackages.put(packageName, ret);
            return ret;
        }
        ret.attemptToRestoreIfNeededAndSave(s);
        return ret;
    }

    public ShortcutLauncher getLauncherShortcuts(ShortcutService s, String packageName, int launcherUserId) {
        PackageWithUser key = PackageWithUser.of(launcherUserId, packageName);
        ShortcutLauncher ret = (ShortcutLauncher) this.mLaunchers.get(key);
        if (ret == null) {
            ret = new ShortcutLauncher(this, this.mUserId, packageName, launcherUserId);
            this.mLaunchers.put(key, ret);
            return ret;
        }
        ret.attemptToRestoreIfNeededAndSave(s);
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
        forAllPackageItems(new -void_forPackageItem_java_lang_String_packageName_int_packageUserId_java_util_function_Consumer_callback_LambdaImpl0(packageUserId, packageName, callback));
    }

    static /* synthetic */ void -com_android_server_pm_ShortcutUser_lambda$1(int packageUserId, String packageName, Consumer callback, ShortcutPackageItem spi) {
        if (spi.getPackageUserId() == packageUserId && spi.getPackageName().equals(packageName)) {
            callback.accept(spi);
        }
    }

    public void resetThrottlingIfNeeded(ShortcutService s) {
        long currentNo = s.getLocaleChangeSequenceNumber();
        if (this.mKnownLocaleChangeSequenceNumber < currentNo) {
            this.mKnownLocaleChangeSequenceNumber = currentNo;
            forAllPackages(new -void_resetThrottlingIfNeeded_com_android_server_pm_ShortcutService_s_LambdaImpl0(s));
            s.scheduleSaveUser(this.mUserId);
        }
    }

    public void handlePackageUpdated(ShortcutService s, String packageName, int newVersionCode) {
        if (this.mPackages.containsKey(packageName)) {
            getPackageShortcuts(s, packageName).handlePackageUpdated(s, newVersionCode);
        }
    }

    public void attemptToRestoreIfNeededAndSave(ShortcutService s, String packageName, int packageUserId) {
        forPackageItem(packageName, packageUserId, new -void_attemptToRestoreIfNeededAndSave_com_android_server_pm_ShortcutService_s_java_lang_String_packageName_int_packageUserId_LambdaImpl0(s));
    }

    public void saveToXml(ShortcutService s, XmlSerializer out, boolean forBackup) throws IOException, XmlPullParserException {
        int i;
        out.startTag(null, TAG_ROOT);
        ShortcutService.writeAttr(out, ATTR_KNOWN_LOCALE_CHANGE_SEQUENCE_NUMBER, this.mKnownLocaleChangeSequenceNumber);
        ShortcutService.writeTagValue(out, TAG_LAUNCHER, this.mLauncherComponent);
        int size = this.mLaunchers.size();
        for (i = 0; i < size; i++) {
            saveShortcutPackageItem(s, out, (ShortcutPackageItem) this.mLaunchers.valueAt(i), forBackup);
        }
        size = this.mPackages.size();
        for (i = 0; i < size; i++) {
            saveShortcutPackageItem(s, out, (ShortcutPackageItem) this.mPackages.valueAt(i), forBackup);
        }
        out.endTag(null, TAG_ROOT);
    }

    private void saveShortcutPackageItem(ShortcutService s, XmlSerializer out, ShortcutPackageItem spi, boolean forBackup) throws IOException, XmlPullParserException {
        if (!forBackup || (s.shouldBackupApp(spi.getPackageName(), spi.getPackageUserId()) && spi.getPackageUserId() == spi.getOwnerUserId())) {
            spi.saveToXml(out, forBackup);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ShortcutUser loadFromXml(ShortcutService s, XmlPullParser parser, int userId, boolean fromBackup) throws IOException, XmlPullParserException {
        ShortcutUser ret = new ShortcutUser(userId);
        ret.mKnownLocaleChangeSequenceNumber = ShortcutService.parseLongAttribute(parser, ATTR_KNOWN_LOCALE_CHANGE_SEQUENCE_NUMBER);
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
                        ret.mLauncherComponent = ShortcutService.parseComponentNameAttribute(parser, ATTR_VALUE);
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
    }

    public ComponentName getLauncherComponent() {
        return this.mLauncherComponent;
    }

    public void setLauncherComponent(ShortcutService s, ComponentName launcherComponent) {
        if (!Objects.equal(this.mLauncherComponent, launcherComponent)) {
            this.mLauncherComponent = launcherComponent;
            s.scheduleSaveUser(this.mUserId);
        }
    }

    public void resetThrottling() {
        for (int i = this.mPackages.size() - 1; i >= 0; i--) {
            ((ShortcutPackage) this.mPackages.valueAt(i)).resetThrottling();
        }
    }

    public void dump(ShortcutService s, PrintWriter pw, String prefix) {
        int i;
        pw.print(prefix);
        pw.print("User: ");
        pw.print(this.mUserId);
        pw.print("  Known locale seq#: ");
        pw.print(this.mKnownLocaleChangeSequenceNumber);
        pw.println();
        prefix = prefix + prefix + "  ";
        pw.print(prefix);
        pw.print("Default launcher: ");
        pw.print(this.mLauncherComponent);
        pw.println();
        for (i = 0; i < this.mLaunchers.size(); i++) {
            ((ShortcutLauncher) this.mLaunchers.valueAt(i)).dump(s, pw, prefix);
        }
        for (i = 0; i < this.mPackages.size(); i++) {
            ((ShortcutPackage) this.mPackages.valueAt(i)).dump(s, pw, prefix);
        }
        pw.println();
        pw.print(prefix);
        pw.println("Bitmap directories: ");
        dumpDirectorySize(s, pw, prefix + "  ", s.getUserBitmapFilePath(this.mUserId));
    }

    private void dumpDirectorySize(ShortcutService s, PrintWriter pw, String prefix, File path) {
        int numFiles = 0;
        long size = 0;
        if (path.listFiles() != null) {
            for (File child : path.listFiles()) {
                if (child.isFile()) {
                    numFiles++;
                    size += child.length();
                } else if (child.isDirectory()) {
                    dumpDirectorySize(s, pw, prefix + "  ", child);
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
        pw.print(Formatter.formatFileSize(s.mContext, size));
        pw.println(")");
    }
}
