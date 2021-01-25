package com.android.server.pm;

import android.content.pm.PackageInfo;
import android.content.pm.ShortcutInfo;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.pm.ShortcutService;
import com.android.server.pm.ShortcutUser;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public class ShortcutLauncher extends ShortcutPackageItem {
    private static final String ATTR_LAUNCHER_USER_ID = "launcher-user";
    private static final String ATTR_PACKAGE_NAME = "package-name";
    private static final String ATTR_PACKAGE_USER_ID = "package-user";
    private static final String ATTR_VALUE = "value";
    private static final String TAG = "ShortcutService";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PIN = "pin";
    static final String TAG_ROOT = "launcher-pins";
    private final int mOwnerUserId;
    private final ArrayMap<ShortcutUser.PackageWithUser, ArraySet<String>> mPinnedShortcuts;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    private ShortcutLauncher(ShortcutUser shortcutUser, int ownerUserId, String packageName, int launcherUserId, ShortcutPackageInfo spi) {
        super(shortcutUser, launcherUserId, packageName, spi != null ? spi : ShortcutPackageInfo.newEmpty());
        this.mPinnedShortcuts = new ArrayMap<>();
        this.mOwnerUserId = ownerUserId;
    }

    public ShortcutLauncher(ShortcutUser shortcutUser, int ownerUserId, String packageName, int launcherUserId) {
        this(shortcutUser, ownerUserId, packageName, launcherUserId, null);
    }

    @Override // com.android.server.pm.ShortcutPackageItem
    public int getOwnerUserId() {
        return this.mOwnerUserId;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.ShortcutPackageItem
    public boolean canRestoreAnyVersion() {
        return true;
    }

    private void onRestoreBlocked() {
        ArrayList<ShortcutUser.PackageWithUser> pinnedPackages = new ArrayList<>(this.mPinnedShortcuts.keySet());
        this.mPinnedShortcuts.clear();
        for (int i = pinnedPackages.size() - 1; i >= 0; i--) {
            ShortcutPackage p = this.mShortcutUser.getPackageShortcutsIfExists(pinnedPackages.get(i).packageName);
            if (p != null) {
                p.refreshPinnedFlags();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.ShortcutPackageItem
    public void onRestored(int restoreBlockReason) {
        if (restoreBlockReason != 0) {
            onRestoreBlocked();
        }
    }

    public void pinShortcuts(int packageUserId, String packageName, List<String> ids, boolean forPinRequest) {
        ShortcutPackage packageShortcuts = this.mShortcutUser.getPackageShortcutsIfExists(packageName);
        if (packageShortcuts != null) {
            ShortcutUser.PackageWithUser pu = ShortcutUser.PackageWithUser.of(packageUserId, packageName);
            int idSize = ids.size();
            if (idSize == 0) {
                this.mPinnedShortcuts.remove(pu);
            } else {
                ArraySet<String> prevSet = this.mPinnedShortcuts.get(pu);
                ArraySet<String> newSet = new ArraySet<>();
                for (int i = 0; i < idSize; i++) {
                    String id = ids.get(i);
                    ShortcutInfo si = packageShortcuts.findShortcutById(id);
                    if (si != null && (si.isDynamic() || si.isManifestShortcut() || ((prevSet != null && prevSet.contains(id)) || forPinRequest))) {
                        newSet.add(id);
                    }
                }
                this.mPinnedShortcuts.put(pu, newSet);
            }
            packageShortcuts.refreshPinnedFlags();
        }
    }

    public ArraySet<String> getPinnedShortcutIds(String packageName, int packageUserId) {
        return this.mPinnedShortcuts.get(ShortcutUser.PackageWithUser.of(packageUserId, packageName));
    }

    public boolean hasPinned(ShortcutInfo shortcut) {
        ArraySet<String> pinned = getPinnedShortcutIds(shortcut.getPackage(), shortcut.getUserId());
        return pinned != null && pinned.contains(shortcut.getId());
    }

    public void addPinnedShortcut(String packageName, int packageUserId, String id, boolean forPinRequest) {
        ArrayList<String> pinnedList;
        ArraySet<String> pinnedSet = getPinnedShortcutIds(packageName, packageUserId);
        if (pinnedSet != null) {
            pinnedList = new ArrayList<>(pinnedSet.size() + 1);
            pinnedList.addAll(pinnedSet);
        } else {
            pinnedList = new ArrayList<>(1);
        }
        pinnedList.add(id);
        pinShortcuts(packageUserId, packageName, pinnedList, forPinRequest);
    }

    /* access modifiers changed from: package-private */
    public boolean cleanUpPackage(String packageName, int packageUserId) {
        return this.mPinnedShortcuts.remove(ShortcutUser.PackageWithUser.of(packageUserId, packageName)) != null;
    }

    public void ensurePackageInfo() {
        PackageInfo pi = this.mShortcutUser.mService.getPackageInfoWithSignatures(getPackageName(), getPackageUserId());
        if (pi == null) {
            Slog.w(TAG, "Package not found: " + getPackageName());
            return;
        }
        getPackageInfo().updateFromPackageInfo(pi);
    }

    @Override // com.android.server.pm.ShortcutPackageItem
    public void saveToXml(XmlSerializer out, boolean forBackup) throws IOException {
        int size;
        if ((!forBackup || getPackageInfo().isBackupAllowed()) && (size = this.mPinnedShortcuts.size()) != 0) {
            out.startTag(null, TAG_ROOT);
            ShortcutService.writeAttr(out, ATTR_PACKAGE_NAME, getPackageName());
            ShortcutService.writeAttr(out, ATTR_LAUNCHER_USER_ID, (long) getPackageUserId());
            getPackageInfo().saveToXml(this.mShortcutUser.mService, out, forBackup);
            for (int i = 0; i < size; i++) {
                ShortcutUser.PackageWithUser pu = this.mPinnedShortcuts.keyAt(i);
                if (!forBackup || pu.userId == getOwnerUserId()) {
                    out.startTag(null, "package");
                    ShortcutService.writeAttr(out, ATTR_PACKAGE_NAME, pu.packageName);
                    ShortcutService.writeAttr(out, ATTR_PACKAGE_USER_ID, (long) pu.userId);
                    ArraySet<String> ids = this.mPinnedShortcuts.valueAt(i);
                    int idSize = ids.size();
                    for (int j = 0; j < idSize; j++) {
                        ShortcutService.writeTagValue(out, TAG_PIN, ids.valueAt(j));
                    }
                    out.endTag(null, "package");
                }
            }
            out.endTag(null, TAG_ROOT);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0097  */
    public static ShortcutLauncher loadFromXml(XmlPullParser parser, ShortcutUser shortcutUser, int ownerUserId, boolean fromBackup) throws IOException, XmlPullParserException {
        int launcherUserId;
        boolean z;
        int packageUserId;
        int i = ownerUserId;
        String launcherPackageName = ShortcutService.parseStringAttribute(parser, ATTR_PACKAGE_NAME);
        if (fromBackup) {
            launcherUserId = i;
        } else {
            launcherUserId = ShortcutService.parseIntAttribute(parser, ATTR_LAUNCHER_USER_ID, i);
        }
        ShortcutLauncher ret = new ShortcutLauncher(shortcutUser, i, launcherPackageName, launcherUserId);
        ArraySet<String> ids = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                break;
            }
            if (type == 2) {
                int depth = parser.getDepth();
                String tag = parser.getName();
                char c = 65535;
                if (depth == outerDepth + 1) {
                    int hashCode = tag.hashCode();
                    if (hashCode != -1923478059) {
                        if (hashCode == -807062458 && tag.equals("package")) {
                            z = true;
                            if (!z) {
                                ret.getPackageInfo().loadFromXml(parser, fromBackup);
                            } else if (z) {
                                String packageName = ShortcutService.parseStringAttribute(parser, ATTR_PACKAGE_NAME);
                                if (fromBackup) {
                                    packageUserId = i;
                                } else {
                                    packageUserId = ShortcutService.parseIntAttribute(parser, ATTR_PACKAGE_USER_ID, i);
                                }
                                ids = new ArraySet<>();
                                ret.mPinnedShortcuts.put(ShortcutUser.PackageWithUser.of(packageUserId, packageName), ids);
                                i = ownerUserId;
                            }
                        }
                    } else if (tag.equals("package-info")) {
                        z = false;
                        if (!z) {
                        }
                    }
                    z = true;
                    if (!z) {
                    }
                }
                if (depth == outerDepth + 2) {
                    if (tag.hashCode() == 110997 && tag.equals(TAG_PIN)) {
                        c = 0;
                    }
                    if (c == 0) {
                        if (ids == null) {
                            Slog.w(TAG, "pin in invalid place");
                        } else {
                            ids.add(ShortcutService.parseStringAttribute(parser, ATTR_VALUE));
                        }
                    }
                }
                ShortcutService.warnForInvalidTag(depth, tag);
            }
            i = ownerUserId;
        }
        return ret;
    }

    public void dump(PrintWriter pw, String prefix, ShortcutService.DumpFilter filter) {
        pw.println();
        pw.print(prefix);
        pw.print("Launcher: ");
        pw.print(getPackageName());
        pw.print("  Package user: ");
        pw.print(getPackageUserId());
        pw.print("  Owner user: ");
        pw.print(getOwnerUserId());
        pw.println();
        ShortcutPackageInfo packageInfo = getPackageInfo();
        packageInfo.dump(pw, prefix + "  ");
        pw.println();
        int size = this.mPinnedShortcuts.size();
        for (int i = 0; i < size; i++) {
            pw.println();
            ShortcutUser.PackageWithUser pu = this.mPinnedShortcuts.keyAt(i);
            pw.print(prefix);
            pw.print("  ");
            pw.print("Package: ");
            pw.print(pu.packageName);
            pw.print("  User: ");
            pw.println(pu.userId);
            ArraySet<String> ids = this.mPinnedShortcuts.valueAt(i);
            int idSize = ids.size();
            for (int j = 0; j < idSize; j++) {
                pw.print(prefix);
                pw.print("    Pinned: ");
                pw.print(ids.valueAt(j));
                pw.println();
            }
        }
    }

    @Override // com.android.server.pm.ShortcutPackageItem
    public JSONObject dumpCheckin(boolean clear) throws JSONException {
        return super.dumpCheckin(clear);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ArraySet<String> getAllPinnedShortcutsForTest(String packageName, int packageUserId) {
        return new ArraySet<>(this.mPinnedShortcuts.get(ShortcutUser.PackageWithUser.of(packageUserId, packageName)));
    }
}
