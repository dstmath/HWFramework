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

/* access modifiers changed from: package-private */
public class ShortcutManager {
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

    public ShortcutManager(Context context) {
        this.mContext = context;
        loadShortcuts();
    }

    public Intent getIntent(KeyCharacterMap kcm, int keyCode, int metaState) {
        int shortcutChar;
        ShortcutInfo shortcut = null;
        boolean isShiftOn = true;
        if ((metaState & 1) != 1) {
            isShiftOn = false;
        }
        SparseArray<ShortcutInfo> shortcutMap = isShiftOn ? this.mShiftShortcuts : this.mShortcuts;
        int shortcutChar2 = kcm.get(keyCode, metaState);
        if (shortcutChar2 != 0) {
            shortcut = shortcutMap.get(shortcutChar2);
        }
        if (shortcut == null && (shortcutChar = Character.toLowerCase(kcm.getDisplayLabel(keyCode))) != 0) {
            shortcut = shortcutMap.get(shortcutChar);
        }
        if (shortcut != null) {
            return shortcut.intent;
        }
        return null;
    }

    /* JADX INFO: Multiple debug info for r5v9 android.content.ComponentName: [D('parser' android.content.res.XmlResourceParser), D('componentName' android.content.ComponentName)] */
    private void loadShortcuts() {
        XmlResourceParser parser;
        Intent intent;
        String title;
        ActivityInfo info;
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            XmlResourceParser parser2 = this.mContext.getResources().getXml(18284548);
            XmlUtils.beginDocument(parser2, TAG_BOOKMARKS);
            while (true) {
                XmlUtils.nextElement(parser2);
                if (parser2.getEventType() != 1) {
                    if (TAG_BOOKMARK.equals(parser2.getName())) {
                        String packageName = parser2.getAttributeValue(null, "package");
                        String className = parser2.getAttributeValue(null, ATTRIBUTE_CLASS);
                        String shortcutName = parser2.getAttributeValue(null, ATTRIBUTE_SHORTCUT);
                        String categoryName = parser2.getAttributeValue(null, ATTRIBUTE_CATEGORY);
                        String shiftName = parser2.getAttributeValue(null, ATTRIBUTE_SHIFT);
                        if (TextUtils.isEmpty(shortcutName)) {
                            Log.w(TAG, "Unable to get shortcut for: " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + className);
                        } else {
                            int shortcutChar = shortcutName.charAt(0);
                            boolean isShiftShortcut = shiftName != null && shiftName.equals("true");
                            if (packageName == null || className == null) {
                                parser = parser2;
                                if (categoryName != null) {
                                    intent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", categoryName);
                                    title = "";
                                } else {
                                    Log.w(TAG, "Unable to add bookmark for shortcut " + shortcutName + ": missing package/class or category attributes");
                                    parser2 = parser;
                                }
                            } else {
                                parser = parser2;
                                ComponentName componentName = new ComponentName(packageName, className);
                                try {
                                    info = packageManager.getActivityInfo(componentName, 794624);
                                } catch (PackageManager.NameNotFoundException e) {
                                    componentName = new ComponentName(packageManager.canonicalToCurrentPackageNames(new String[]{packageName})[0], className);
                                    try {
                                        info = packageManager.getActivityInfo(componentName, 794624);
                                    } catch (PackageManager.NameNotFoundException e2) {
                                        Log.w(TAG, "Unable to add bookmark: " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + className, e);
                                        parser2 = parser;
                                    }
                                }
                                intent = new Intent("android.intent.action.MAIN");
                                intent.addCategory("android.intent.category.LAUNCHER");
                                intent.setComponent(componentName);
                                title = info.loadLabel(packageManager).toString();
                            }
                            ShortcutInfo shortcut = new ShortcutInfo(title, intent);
                            if (isShiftShortcut) {
                                this.mShiftShortcuts.put(shortcutChar, shortcut);
                            } else {
                                this.mShortcuts.put(shortcutChar, shortcut);
                            }
                            parser2 = parser;
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

    /* access modifiers changed from: private */
    public static final class ShortcutInfo {
        public final Intent intent;
        public final String title;

        public ShortcutInfo(String title2, Intent intent2) {
            this.title = title2;
            this.intent = intent2;
        }
    }
}
