package com.android.server.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyCharacterMap;
import com.android.internal.util.XmlUtils;
import com.android.server.slice.SliceClientPermissions;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

class ShortcutManager {
    private static final String ATTRIBUTE_CATEGORY = "category";
    private static final String ATTRIBUTE_CLASS = "class";
    private static final String ATTRIBUTE_PACKAGE = "package";
    private static final String ATTRIBUTE_SHIFT = "shift";
    private static final String ATTRIBUTE_SHORTCUT = "shortcut";
    private static final String TAG = "ShortcutManager";
    private static final String TAG_BOOKMARK = "bookmark";
    private static final String TAG_BOOKMARKS = "bookmarks";
    private final Context mContext;
    private final SparseArray<ShortcutInfo> mShiftShortcuts = new SparseArray<>();
    private final SparseArray<ShortcutInfo> mShortcuts = new SparseArray<>();

    private static final class ShortcutInfo {
        public final Intent intent;
        public final String title;

        public ShortcutInfo(String title2, Intent intent2) {
            this.title = title2;
            this.intent = intent2;
        }
    }

    public ShortcutManager(Context context) {
        this.mContext = context;
        loadShortcuts();
    }

    public Intent getIntent(KeyCharacterMap kcm, int keyCode, int metaState) {
        ShortcutInfo shortcut = null;
        boolean isShiftOn = true;
        if ((metaState & 1) != 1) {
            isShiftOn = false;
        }
        SparseArray<ShortcutInfo> shortcutMap = isShiftOn ? this.mShiftShortcuts : this.mShortcuts;
        int shortcutChar = kcm.get(keyCode, metaState);
        if (shortcutChar != 0) {
            shortcut = shortcutMap.get(shortcutChar);
        }
        if (shortcut == null) {
            int shortcutChar2 = Character.toLowerCase(kcm.getDisplayLabel(keyCode));
            if (shortcutChar2 != 0) {
                shortcut = shortcutMap.get(shortcutChar2);
            }
        }
        if (shortcut != null) {
            return shortcut.intent;
        }
        return null;
    }

    private void loadShortcuts() {
        Intent intent;
        String title;
        ActivityInfo info;
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            XmlResourceParser parser = this.mContext.getResources().getXml(18284548);
            XmlUtils.beginDocument(parser, TAG_BOOKMARKS);
            while (true) {
                XmlUtils.nextElement(parser);
                if (parser.getEventType() != 1) {
                    if (TAG_BOOKMARK.equals(parser.getName())) {
                        String packageName = parser.getAttributeValue(null, "package");
                        String className = parser.getAttributeValue(null, "class");
                        String shortcutName = parser.getAttributeValue(null, ATTRIBUTE_SHORTCUT);
                        String categoryName = parser.getAttributeValue(null, ATTRIBUTE_CATEGORY);
                        String shiftName = parser.getAttributeValue(null, ATTRIBUTE_SHIFT);
                        if (TextUtils.isEmpty(shortcutName)) {
                            Log.w(TAG, "Unable to get shortcut for: " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + className);
                        } else {
                            int shortcutChar = shortcutName.charAt(0);
                            boolean isShiftShortcut = shiftName != null && shiftName.equals("true");
                            if (packageName != null && className != null) {
                                ComponentName componentName = new ComponentName(packageName, className);
                                try {
                                    info = packageManager.getActivityInfo(componentName, 794624);
                                } catch (PackageManager.NameNotFoundException e) {
                                    PackageManager.NameNotFoundException e2 = e;
                                    componentName = new ComponentName(packageManager.canonicalToCurrentPackageNames(new String[]{packageName})[0], className);
                                    try {
                                        info = packageManager.getActivityInfo(componentName, 794624);
                                    } catch (PackageManager.NameNotFoundException e1) {
                                        PackageManager.NameNotFoundException nameNotFoundException = e1;
                                        StringBuilder sb = new StringBuilder();
                                        PackageManager.NameNotFoundException nameNotFoundException2 = e1;
                                        sb.append("Unable to add bookmark: ");
                                        sb.append(packageName);
                                        sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
                                        sb.append(className);
                                        Log.w(TAG, sb.toString(), e2);
                                    }
                                }
                                intent = new Intent("android.intent.action.MAIN");
                                intent.addCategory("android.intent.category.LAUNCHER");
                                intent.setComponent(componentName);
                                title = info.loadLabel(packageManager).toString();
                            } else if (categoryName != null) {
                                intent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", categoryName);
                                title = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                            } else {
                                Log.w(TAG, "Unable to add bookmark for shortcut " + shortcutName + ": missing package/class or category attributes");
                            }
                            ShortcutInfo shortcut = new ShortcutInfo(title, intent);
                            if (isShiftShortcut) {
                                this.mShiftShortcuts.put(shortcutChar, shortcut);
                            } else {
                                this.mShortcuts.put(shortcutChar, shortcut);
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        } catch (XmlPullParserException e3) {
            Log.w(TAG, "Got exception parsing bookmarks.", e3);
        } catch (IOException e4) {
            Log.w(TAG, "Got exception parsing bookmarks.", e4);
        }
    }
}
