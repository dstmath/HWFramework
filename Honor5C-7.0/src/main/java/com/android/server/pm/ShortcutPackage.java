package com.android.server.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.os.PersistableBundle;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class ShortcutPackage extends ShortcutPackageItem {
    private static final String ATTR_ACTIVITY = "activity";
    private static final String ATTR_BITMAP_PATH = "bitmap-path";
    private static final String ATTR_CALL_COUNT = "call-count";
    private static final String ATTR_DYNAMIC_COUNT = "dynamic-count";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_ICON_RES = "icon-res";
    private static final String ATTR_ID = "id";
    private static final String ATTR_INTENT = "intent";
    private static final String ATTR_LAST_RESET = "last-reset";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_NAME_XMLUTILS = "name";
    private static final String ATTR_TEXT = "text";
    private static final String ATTR_TIMESTAMP = "timestamp";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_WEIGHT = "weight";
    private static final String NAME_CATEGORIES = "categories";
    private static final String TAG = "ShortcutService";
    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_EXTRAS = "extras";
    private static final String TAG_INTENT_EXTRAS = "intent-extras";
    static final String TAG_ROOT = "package";
    private static final String TAG_SHORTCUT = "shortcut";
    private static final String TAG_STRING_ARRAY_XMLUTILS = "string-array";
    private int mApiCallCount;
    private int mDynamicShortcutCount;
    private long mLastKnownForegroundElapsedTime;
    private long mLastResetTime;
    private final int mPackageUid;
    private final ArrayMap<String, ShortcutInfo> mShortcuts;

    final /* synthetic */ class -void_refreshPinnedFlags_com_android_server_pm_ShortcutService_s_LambdaImpl0 implements Consumer {
        private /* synthetic */ ShortcutPackage val$this;

        public /* synthetic */ -void_refreshPinnedFlags_com_android_server_pm_ShortcutService_s_LambdaImpl0(ShortcutPackage shortcutPackage) {
            this.val$this = shortcutPackage;
        }

        public void accept(Object arg0) {
            this.val$this.-com_android_server_pm_ShortcutPackage_lambda$1((ShortcutLauncher) arg0);
        }
    }

    private ShortcutPackage(ShortcutService s, ShortcutUser shortcutUser, int packageUserId, String packageName, ShortcutPackageInfo spi) {
        if (spi == null) {
            spi = ShortcutPackageInfo.newEmpty();
        }
        super(shortcutUser, packageUserId, packageName, spi);
        this.mShortcuts = new ArrayMap();
        this.mDynamicShortcutCount = 0;
        this.mPackageUid = s.injectGetPackageUid(packageName, packageUserId);
    }

    public ShortcutPackage(ShortcutService s, ShortcutUser shortcutUser, int packageUserId, String packageName) {
        this(s, shortcutUser, packageUserId, packageName, null);
    }

    public int getOwnerUserId() {
        return getPackageUserId();
    }

    public int getPackageUid() {
        return this.mPackageUid;
    }

    private void onShortcutPublish(ShortcutService s) {
        if (getPackageInfo().getVersionCode() < 0) {
            int versionCode = s.getApplicationVersionCode(getPackageName(), getOwnerUserId());
            if (versionCode >= 0) {
                getPackageInfo().setVersionCode(versionCode);
                s.scheduleSaveUser(getOwnerUserId());
            }
        }
    }

    protected void onRestoreBlocked(ShortcutService s) {
        this.mShortcuts.clear();
    }

    protected void onRestored(ShortcutService s) {
        refreshPinnedFlags(s);
    }

    public ShortcutInfo findShortcutById(String id) {
        return (ShortcutInfo) this.mShortcuts.get(id);
    }

    private ShortcutInfo deleteShortcut(ShortcutService s, String id) {
        ShortcutInfo shortcut = (ShortcutInfo) this.mShortcuts.remove(id);
        if (shortcut != null) {
            s.removeIcon(getPackageUserId(), shortcut);
            shortcut.clearFlags(3);
        }
        return shortcut;
    }

    void addShortcut(ShortcutService s, ShortcutInfo newShortcut) {
        deleteShortcut(s, newShortcut.getId());
        s.saveIconAndFixUpShortcut(getPackageUserId(), newShortcut);
        this.mShortcuts.put(newShortcut.getId(), newShortcut);
    }

    public void addDynamicShortcut(ShortcutService s, ShortcutInfo newShortcut) {
        boolean wasPinned;
        int newDynamicCount;
        onShortcutPublish(s);
        newShortcut.addFlags(1);
        ShortcutInfo oldShortcut = (ShortcutInfo) this.mShortcuts.get(newShortcut.getId());
        if (oldShortcut == null) {
            wasPinned = false;
            newDynamicCount = this.mDynamicShortcutCount + 1;
        } else {
            wasPinned = oldShortcut.isPinned();
            if (oldShortcut.isDynamic()) {
                newDynamicCount = this.mDynamicShortcutCount;
            } else {
                newDynamicCount = this.mDynamicShortcutCount + 1;
            }
        }
        s.enforceMaxDynamicShortcuts(newDynamicCount);
        if (wasPinned) {
            newShortcut.addFlags(2);
        }
        addShortcut(s, newShortcut);
        this.mDynamicShortcutCount = newDynamicCount;
    }

    private void removeOrphans(ShortcutService s) {
        int i;
        ArrayList removeList = null;
        for (i = this.mShortcuts.size() - 1; i >= 0; i--) {
            ShortcutInfo si = (ShortcutInfo) this.mShortcuts.valueAt(i);
            if (!(si.isPinned() || si.isDynamic())) {
                if (removeList == null) {
                    removeList = new ArrayList();
                }
                removeList.add(si.getId());
            }
        }
        if (removeList != null) {
            for (i = removeList.size() - 1; i >= 0; i--) {
                deleteShortcut(s, (String) removeList.get(i));
            }
        }
    }

    public void deleteAllDynamicShortcuts(ShortcutService s) {
        for (int i = this.mShortcuts.size() - 1; i >= 0; i--) {
            ((ShortcutInfo) this.mShortcuts.valueAt(i)).clearFlags(1);
        }
        removeOrphans(s);
        this.mDynamicShortcutCount = 0;
    }

    public void deleteDynamicWithId(ShortcutService s, String shortcutId) {
        ShortcutInfo oldShortcut = (ShortcutInfo) this.mShortcuts.get(shortcutId);
        if (oldShortcut != null) {
            if (oldShortcut.isDynamic()) {
                this.mDynamicShortcutCount--;
            }
            if (oldShortcut.isPinned()) {
                oldShortcut.clearFlags(1);
            } else {
                deleteShortcut(s, shortcutId);
            }
        }
    }

    public void refreshPinnedFlags(ShortcutService s) {
        for (int i = this.mShortcuts.size() - 1; i >= 0; i--) {
            ((ShortcutInfo) this.mShortcuts.valueAt(i)).clearFlags(2);
        }
        s.getUserShortcutsLocked(getPackageUserId()).forAllLaunchers(new -void_refreshPinnedFlags_com_android_server_pm_ShortcutService_s_LambdaImpl0());
        removeOrphans(s);
    }

    /* synthetic */ void -com_android_server_pm_ShortcutPackage_lambda$1(ShortcutLauncher launcherShortcuts) {
        ArraySet<String> pinned = launcherShortcuts.getPinnedShortcutIds(getPackageName(), getPackageUserId());
        if (pinned != null && pinned.size() != 0) {
            for (int i = pinned.size() - 1; i >= 0; i--) {
                ShortcutInfo si = (ShortcutInfo) this.mShortcuts.get((String) pinned.valueAt(i));
                if (si != null) {
                    si.addFlags(2);
                }
            }
        }
    }

    public int getApiCallCount(ShortcutService s) {
        this.mShortcutUser.resetThrottlingIfNeeded(s);
        if (s.isUidForegroundLocked(this.mPackageUid) || this.mLastKnownForegroundElapsedTime < s.getUidLastForegroundElapsedTimeLocked(this.mPackageUid)) {
            this.mLastKnownForegroundElapsedTime = s.injectElapsedRealtime();
            resetRateLimiting(s);
        }
        long last = s.getLastResetTimeLocked();
        long now = s.injectCurrentTimeMillis();
        if (!ShortcutService.isClockValid(now) || this.mLastResetTime <= now) {
            if (this.mLastResetTime < last) {
                this.mApiCallCount = 0;
                this.mLastResetTime = last;
            }
            return this.mApiCallCount;
        }
        Slog.w(TAG, "Clock rewound");
        this.mLastResetTime = now;
        this.mApiCallCount = 0;
        return this.mApiCallCount;
    }

    public boolean tryApiCall(ShortcutService s) {
        if (getApiCallCount(s) >= s.mMaxUpdatesPerInterval) {
            return false;
        }
        this.mApiCallCount++;
        s.scheduleSaveUser(getOwnerUserId());
        return true;
    }

    public void resetRateLimiting(ShortcutService s) {
        if (this.mApiCallCount > 0) {
            this.mApiCallCount = 0;
            s.scheduleSaveUser(getOwnerUserId());
        }
    }

    public void resetRateLimitingForCommandLineNoSaving() {
        this.mApiCallCount = 0;
        this.mLastResetTime = 0;
    }

    public void findAll(ShortcutService s, List<ShortcutInfo> result, Predicate<ShortcutInfo> query, int cloneFlag) {
        findAll(s, result, query, cloneFlag, null, 0);
    }

    public void findAll(ShortcutService s, List<ShortcutInfo> result, Predicate<ShortcutInfo> query, int cloneFlag, String callingLauncher, int launcherUserId) {
        if (!getPackageInfo().isShadow()) {
            ArraySet arraySet;
            if (callingLauncher == null) {
                arraySet = null;
            } else {
                arraySet = s.getLauncherShortcutsLocked(callingLauncher, getPackageUserId(), launcherUserId).getPinnedShortcutIds(getPackageName(), getPackageUserId());
            }
            int i = 0;
            while (i < this.mShortcuts.size()) {
                ShortcutInfo si = (ShortcutInfo) this.mShortcuts.valueAt(i);
                boolean contains = callingLauncher != null ? arraySet != null ? arraySet.contains(si.getId()) : false : true;
                if (!si.isDynamic()) {
                    if (!si.isPinned()) {
                        s.wtf("Shortcut not pinned: package " + getPackageName() + ", user=" + getPackageUserId() + ", id=" + si.getId());
                    } else if (!contains) {
                    }
                    i++;
                }
                ShortcutInfo clone = si.clone(cloneFlag);
                if (!contains) {
                    clone.clearFlags(2);
                }
                if (query == null || query.test(clone)) {
                    result.add(clone);
                    i++;
                } else {
                    i++;
                }
            }
        }
    }

    public void resetThrottling() {
        this.mApiCallCount = 0;
    }

    public void handlePackageUpdated(ShortcutService s, int newVersionCode) {
        if (getPackageInfo().getVersionCode() < newVersionCode) {
            getPackageInfo().setVersionCode(newVersionCode);
            boolean changed = false;
            for (int i = this.mShortcuts.size() - 1; i >= 0; i--) {
                ShortcutInfo si = (ShortcutInfo) this.mShortcuts.valueAt(i);
                if (si.hasIconResource()) {
                    changed = true;
                    si.setTimestamp(s.injectCurrentTimeMillis());
                }
            }
            if (changed) {
                s.packageShortcutsChanged(getPackageName(), getPackageUserId());
            } else {
                s.scheduleSaveUser(getPackageUserId());
            }
        }
    }

    public void dump(ShortcutService s, PrintWriter pw, String prefix) {
        pw.println();
        pw.print(prefix);
        pw.print("Package: ");
        pw.print(getPackageName());
        pw.print("  UID: ");
        pw.print(this.mPackageUid);
        pw.println();
        pw.print(prefix);
        pw.print("  ");
        pw.print("Calls: ");
        pw.print(getApiCallCount(s));
        pw.println();
        pw.print(prefix);
        pw.print("  ");
        pw.print("Last known FG: ");
        pw.print(this.mLastKnownForegroundElapsedTime);
        pw.println();
        pw.print(prefix);
        pw.print("  ");
        pw.print("Last reset: [");
        pw.print(this.mLastResetTime);
        pw.print("] ");
        pw.print(ShortcutService.formatTime(this.mLastResetTime));
        pw.println();
        getPackageInfo().dump(s, pw, prefix + "  ");
        pw.println();
        pw.print(prefix);
        pw.println("  Shortcuts:");
        long totalBitmapSize = 0;
        ArrayMap<String, ShortcutInfo> shortcuts = this.mShortcuts;
        int size = shortcuts.size();
        for (int i = 0; i < size; i++) {
            ShortcutInfo si = (ShortcutInfo) shortcuts.valueAt(i);
            pw.print(prefix);
            pw.print("    ");
            pw.println(si.toInsecureString());
            if (si.getBitmapPath() != null) {
                long len = new File(si.getBitmapPath()).length();
                pw.print(prefix);
                pw.print("      ");
                pw.print("bitmap size=");
                pw.println(len);
                totalBitmapSize += len;
            }
        }
        pw.print(prefix);
        pw.print("  ");
        pw.print("Total bitmap size: ");
        pw.print(totalBitmapSize);
        pw.print(" (");
        pw.print(Formatter.formatFileSize(s.mContext, totalBitmapSize));
        pw.println(")");
    }

    public void saveToXml(XmlSerializer out, boolean forBackup) throws IOException, XmlPullParserException {
        int size = this.mShortcuts.size();
        if (size != 0 || this.mApiCallCount != 0) {
            out.startTag(null, TAG_ROOT);
            ShortcutService.writeAttr(out, ATTR_NAME_XMLUTILS, getPackageName());
            ShortcutService.writeAttr(out, ATTR_DYNAMIC_COUNT, (long) this.mDynamicShortcutCount);
            ShortcutService.writeAttr(out, ATTR_CALL_COUNT, (long) this.mApiCallCount);
            ShortcutService.writeAttr(out, ATTR_LAST_RESET, this.mLastResetTime);
            getPackageInfo().saveToXml(out);
            for (int j = 0; j < size; j++) {
                saveShortcut(out, (ShortcutInfo) this.mShortcuts.valueAt(j), forBackup);
            }
            out.endTag(null, TAG_ROOT);
        }
    }

    private static void saveShortcut(XmlSerializer out, ShortcutInfo si, boolean forBackup) throws IOException, XmlPullParserException {
        if (!forBackup || si.isPinned()) {
            out.startTag(null, TAG_SHORTCUT);
            ShortcutService.writeAttr(out, ATTR_ID, si.getId());
            ShortcutService.writeAttr(out, ATTR_ACTIVITY, si.getActivityComponent());
            ShortcutService.writeAttr(out, ATTR_TITLE, si.getTitle());
            ShortcutService.writeAttr(out, ATTR_TEXT, si.getText());
            ShortcutService.writeAttr(out, ATTR_INTENT, si.getIntentNoExtras());
            ShortcutService.writeAttr(out, ATTR_WEIGHT, (long) si.getWeight());
            ShortcutService.writeAttr(out, ATTR_TIMESTAMP, si.getLastChangedTimestamp());
            if (forBackup) {
                ShortcutService.writeAttr(out, ATTR_FLAGS, (long) (si.getFlags() & -14));
            } else {
                ShortcutService.writeAttr(out, ATTR_FLAGS, (long) si.getFlags());
                ShortcutService.writeAttr(out, ATTR_ICON_RES, (long) si.getIconResourceId());
                ShortcutService.writeAttr(out, ATTR_BITMAP_PATH, si.getBitmapPath());
            }
            Set<String> cat = si.getCategories();
            if (cat != null && cat.size() > 0) {
                out.startTag(null, TAG_CATEGORIES);
                XmlUtils.writeStringArrayXml((String[]) cat.toArray(new String[cat.size()]), TAG_CATEGORIES, out);
                out.endTag(null, TAG_CATEGORIES);
            }
            ShortcutService.writeTagExtra(out, TAG_INTENT_EXTRAS, si.getIntentPersistableExtras());
            ShortcutService.writeTagExtra(out, TAG_EXTRAS, si.getExtras());
            out.endTag(null, TAG_SHORTCUT);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ShortcutPackage loadFromXml(ShortcutService s, ShortcutUser shortcutUser, XmlPullParser parser, boolean fromBackup) throws IOException, XmlPullParserException {
        String packageName = ShortcutService.parseStringAttribute(parser, ATTR_NAME_XMLUTILS);
        ShortcutPackage ret = new ShortcutPackage(s, shortcutUser, shortcutUser.getUserId(), packageName);
        ret.mDynamicShortcutCount = ShortcutService.parseIntAttribute(parser, ATTR_DYNAMIC_COUNT);
        ret.mApiCallCount = ShortcutService.parseIntAttribute(parser, ATTR_CALL_COUNT);
        ret.mLastResetTime = ShortcutService.parseLongAttribute(parser, ATTR_LAST_RESET);
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
                    } else if (tag.equals(TAG_SHORTCUT)) {
                        ShortcutInfo si = parseShortcut(parser, packageName, shortcutUser.getUserId());
                        ret.mShortcuts.put(si.getId(), si);
                    }
                }
                ShortcutService.warnForInvalidTag(depth, tag);
            }
        }
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ShortcutInfo parseShortcut(XmlPullParser parser, String packageName, int userId) throws IOException, XmlPullParserException {
        PersistableBundle intentPersistableExtras = null;
        PersistableBundle extras = null;
        Set categories = null;
        String id = ShortcutService.parseStringAttribute(parser, ATTR_ID);
        ComponentName activityComponent = ShortcutService.parseComponentNameAttribute(parser, ATTR_ACTIVITY);
        String title = ShortcutService.parseStringAttribute(parser, ATTR_TITLE);
        String text = ShortcutService.parseStringAttribute(parser, ATTR_TEXT);
        Intent intent = ShortcutService.parseIntentAttribute(parser, ATTR_INTENT);
        int weight = (int) ShortcutService.parseLongAttribute(parser, ATTR_WEIGHT);
        long lastChangedTimestamp = ShortcutService.parseLongAttribute(parser, ATTR_TIMESTAMP);
        int flags = (int) ShortcutService.parseLongAttribute(parser, ATTR_FLAGS);
        int iconRes = (int) ShortcutService.parseLongAttribute(parser, ATTR_ICON_RES);
        String bitmapPath = ShortcutService.parseStringAttribute(parser, ATTR_BITMAP_PATH);
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (type == 2) {
                    int depth = parser.getDepth();
                    String tag = parser.getName();
                    if (tag.equals(TAG_INTENT_EXTRAS)) {
                        intentPersistableExtras = PersistableBundle.restoreFromXml(parser);
                    } else {
                        if (tag.equals(TAG_EXTRAS)) {
                            extras = PersistableBundle.restoreFromXml(parser);
                        } else {
                            if (!tag.equals(TAG_CATEGORIES)) {
                                if (!tag.equals(TAG_STRING_ARRAY_XMLUTILS)) {
                                    break;
                                }
                                if (TAG_CATEGORIES.equals(ShortcutService.parseStringAttribute(parser, ATTR_NAME_XMLUTILS))) {
                                    String[] ar = XmlUtils.readThisStringArrayXml(parser, TAG_STRING_ARRAY_XMLUTILS, null);
                                    categories = new ArraySet(ar.length);
                                    for (Object add : ar) {
                                        categories.add(add);
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
        }
        return new ShortcutInfo(userId, id, packageName, activityComponent, null, title, text, categories, intent, intentPersistableExtras, weight, extras, lastChangedTimestamp, flags, iconRes, bitmapPath);
    }

    List<ShortcutInfo> getAllShortcutsForTest() {
        return new ArrayList(this.mShortcuts.values());
    }
}
