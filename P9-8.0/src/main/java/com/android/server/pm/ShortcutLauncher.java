package com.android.server.pm;

import android.content.pm.PackageInfo;
import android.content.pm.ShortcutInfo;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class ShortcutLauncher extends ShortcutPackageItem {
    private static final String ATTR_LAUNCHER_USER_ID = "launcher-user";
    private static final String ATTR_PACKAGE_NAME = "package-name";
    private static final String ATTR_PACKAGE_USER_ID = "package-user";
    private static final String ATTR_VALUE = "value";
    private static final String TAG = "ShortcutService";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PIN = "pin";
    static final String TAG_ROOT = "launcher-pins";
    private final int mOwnerUserId;
    private final ArrayMap<PackageWithUser, ArraySet<String>> mPinnedShortcuts;

    private ShortcutLauncher(ShortcutUser shortcutUser, int ownerUserId, String packageName, int launcherUserId, ShortcutPackageInfo spi) {
        if (spi == null) {
            spi = ShortcutPackageInfo.newEmpty();
        }
        super(shortcutUser, launcherUserId, packageName, spi);
        this.mPinnedShortcuts = new ArrayMap();
        this.mOwnerUserId = ownerUserId;
    }

    public ShortcutLauncher(ShortcutUser shortcutUser, int ownerUserId, String packageName, int launcherUserId) {
        this(shortcutUser, ownerUserId, packageName, launcherUserId, null);
    }

    public int getOwnerUserId() {
        return this.mOwnerUserId;
    }

    protected void onRestoreBlocked() {
        ArrayList<PackageWithUser> pinnedPackages = new ArrayList(this.mPinnedShortcuts.keySet());
        this.mPinnedShortcuts.clear();
        for (int i = pinnedPackages.size() - 1; i >= 0; i--) {
            ShortcutPackage p = this.mShortcutUser.getPackageShortcutsIfExists(((PackageWithUser) pinnedPackages.get(i)).packageName);
            if (p != null) {
                p.lambda$-com_android_server_pm_ShortcutService_84442();
            }
        }
    }

    protected void onRestored() {
    }

    public void pinShortcuts(int packageUserId, String packageName, List<String> ids) {
        ShortcutPackage packageShortcuts = this.mShortcutUser.getPackageShortcutsIfExists(packageName);
        if (packageShortcuts != null) {
            PackageWithUser pu = PackageWithUser.of(packageUserId, packageName);
            int idSize = ids.size();
            if (idSize == 0) {
                this.mPinnedShortcuts.remove(pu);
            } else {
                ArraySet<String> prevSet = (ArraySet) this.mPinnedShortcuts.get(pu);
                ArraySet<String> newSet = new ArraySet();
                for (int i = 0; i < idSize; i++) {
                    String id = (String) ids.get(i);
                    ShortcutInfo si = packageShortcuts.findShortcutById(id);
                    if (si != null && (si.isDynamic() || si.isManifestShortcut() || (prevSet != null && prevSet.contains(id)))) {
                        newSet.add(id);
                    }
                }
                this.mPinnedShortcuts.put(pu, newSet);
            }
            packageShortcuts.lambda$-com_android_server_pm_ShortcutService_84442();
        }
    }

    public ArraySet<String> getPinnedShortcutIds(String packageName, int packageUserId) {
        return (ArraySet) this.mPinnedShortcuts.get(PackageWithUser.of(packageUserId, packageName));
    }

    public boolean hasPinned(ShortcutInfo shortcut) {
        ArraySet<String> pinned = getPinnedShortcutIds(shortcut.getPackage(), shortcut.getUserId());
        return pinned != null ? pinned.contains(shortcut.getId()) : false;
    }

    public void addPinnedShortcut(String packageName, int packageUserId, String id) {
        ArrayList<String> pinnedList;
        ArraySet<String> pinnedSet = getPinnedShortcutIds(packageName, packageUserId);
        if (pinnedSet != null) {
            pinnedList = new ArrayList(pinnedSet.size() + 1);
            pinnedList.addAll(pinnedSet);
        } else {
            pinnedList = new ArrayList(1);
        }
        pinnedList.add(id);
        pinShortcuts(packageUserId, packageName, pinnedList);
    }

    /* renamed from: cleanUpPackage */
    boolean lambda$-com_android_server_pm_ShortcutService_84229(String packageName, int packageUserId) {
        return this.mPinnedShortcuts.remove(PackageWithUser.of(packageUserId, packageName)) != null;
    }

    /* renamed from: ensureVersionInfo */
    public void lambda$-com_android_server_pm_ShortcutService_124812() {
        PackageInfo pi = this.mShortcutUser.mService.getPackageInfoWithSignatures(getPackageName(), getPackageUserId());
        if (pi == null) {
            Slog.w(TAG, "Package not found: " + getPackageName());
        } else {
            getPackageInfo().updateVersionInfo(pi);
        }
    }

    public void saveToXml(XmlSerializer out, boolean forBackup) throws IOException {
        int size = this.mPinnedShortcuts.size();
        if (size != 0) {
            out.startTag(null, TAG_ROOT);
            ShortcutService.writeAttr(out, ATTR_PACKAGE_NAME, getPackageName());
            ShortcutService.writeAttr(out, ATTR_LAUNCHER_USER_ID, (long) getPackageUserId());
            getPackageInfo().saveToXml(out);
            for (int i = 0; i < size; i++) {
                PackageWithUser pu = (PackageWithUser) this.mPinnedShortcuts.keyAt(i);
                if (!forBackup || pu.userId == getOwnerUserId()) {
                    out.startTag(null, "package");
                    ShortcutService.writeAttr(out, ATTR_PACKAGE_NAME, pu.packageName);
                    ShortcutService.writeAttr(out, ATTR_PACKAGE_USER_ID, (long) pu.userId);
                    ArraySet<String> ids = (ArraySet) this.mPinnedShortcuts.valueAt(i);
                    int idSize = ids.size();
                    for (int j = 0; j < idSize; j++) {
                        ShortcutService.writeTagValue(out, TAG_PIN, (String) ids.valueAt(j));
                    }
                    out.endTag(null, "package");
                }
            }
            out.endTag(null, TAG_ROOT);
        }
    }

    public static ShortcutLauncher loadFromXml(XmlPullParser parser, ShortcutUser shortcutUser, int ownerUserId, boolean fromBackup) throws IOException, XmlPullParserException {
        int launcherUserId;
        String launcherPackageName = ShortcutService.parseStringAttribute(parser, ATTR_PACKAGE_NAME);
        if (fromBackup) {
            launcherUserId = ownerUserId;
        } else {
            launcherUserId = ShortcutService.parseIntAttribute(parser, ATTR_LAUNCHER_USER_ID, ownerUserId);
        }
        ShortcutLauncher ret = new ShortcutLauncher(shortcutUser, ownerUserId, launcherPackageName, launcherUserId);
        ArraySet ids = null;
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
                    if (tag.equals("package-info")) {
                        ret.getPackageInfo().loadFromXml(parser, fromBackup);
                    } else if (tag.equals("package")) {
                        int packageUserId;
                        String packageName = ShortcutService.parseStringAttribute(parser, ATTR_PACKAGE_NAME);
                        if (fromBackup) {
                            packageUserId = ownerUserId;
                        } else {
                            packageUserId = ShortcutService.parseIntAttribute(parser, ATTR_PACKAGE_USER_ID, ownerUserId);
                        }
                        ids = new ArraySet();
                        ret.mPinnedShortcuts.put(PackageWithUser.of(packageUserId, packageName), ids);
                    }
                }
                if (depth != outerDepth + 2 || !tag.equals(TAG_PIN)) {
                    ShortcutService.warnForInvalidTag(depth, tag);
                } else if (ids == null) {
                    Slog.w(TAG, "pin in invalid place");
                } else {
                    ids.add(ShortcutService.parseStringAttribute(parser, ATTR_VALUE));
                }
            }
        }
        return ret;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println();
        pw.print(prefix);
        pw.print("Launcher: ");
        pw.print(getPackageName());
        pw.print("  Package user: ");
        pw.print(getPackageUserId());
        pw.print("  Owner user: ");
        pw.print(getOwnerUserId());
        pw.println();
        getPackageInfo().dump(pw, prefix + "  ");
        pw.println();
        int size = this.mPinnedShortcuts.size();
        for (int i = 0; i < size; i++) {
            pw.println();
            PackageWithUser pu = (PackageWithUser) this.mPinnedShortcuts.keyAt(i);
            pw.print(prefix);
            pw.print("  ");
            pw.print("Package: ");
            pw.print(pu.packageName);
            pw.print("  User: ");
            pw.println(pu.userId);
            ArraySet<String> ids = (ArraySet) this.mPinnedShortcuts.valueAt(i);
            int idSize = ids.size();
            for (int j = 0; j < idSize; j++) {
                pw.print(prefix);
                pw.print("    Pinned: ");
                pw.print((String) ids.valueAt(j));
                pw.println();
            }
        }
    }

    public JSONObject dumpCheckin(boolean clear) throws JSONException {
        return super.dumpCheckin(clear);
    }

    ArraySet<String> getAllPinnedShortcutsForTest(String packageName, int packageUserId) {
        return new ArraySet((ArraySet) this.mPinnedShortcuts.get(PackageWithUser.of(packageUserId, packageName)));
    }
}
