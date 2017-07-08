package com.android.server.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyCharacterMap;
import com.android.internal.util.XmlUtils;
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
    private final SparseArray<ShortcutInfo> mShiftShortcuts;
    private final SparseArray<ShortcutInfo> mShortcuts;

    private static final class ShortcutInfo {
        public final Intent intent;
        public final String title;

        public ShortcutInfo(String title, Intent intent) {
            this.title = title;
            this.intent = intent;
        }
    }

    public ShortcutManager(Context context) {
        this.mShortcuts = new SparseArray();
        this.mShiftShortcuts = new SparseArray();
        this.mContext = context;
        loadShortcuts();
    }

    public Intent getIntent(KeyCharacterMap kcm, int keyCode, int metaState) {
        boolean isShiftOn = true;
        ShortcutInfo shortcutInfo = null;
        if ((metaState & 1) != 1) {
            isShiftOn = false;
        }
        SparseArray<ShortcutInfo> shortcutMap = isShiftOn ? this.mShiftShortcuts : this.mShortcuts;
        int shortcutChar = kcm.get(keyCode, metaState);
        if (shortcutChar != 0) {
            shortcutInfo = (ShortcutInfo) shortcutMap.get(shortcutChar);
        }
        if (shortcutInfo == null) {
            shortcutChar = Character.toLowerCase(kcm.getDisplayLabel(keyCode));
            if (shortcutChar != 0) {
                shortcutInfo = (ShortcutInfo) shortcutMap.get(shortcutChar);
            }
        }
        if (shortcutInfo != null) {
            return shortcutInfo.intent;
        }
        return null;
    }

    private void loadShortcuts() {
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            XmlResourceParser parser = this.mContext.getResources().getXml(17891331);
            XmlUtils.beginDocument(parser, TAG_BOOKMARKS);
            while (true) {
                XmlUtils.nextElement(parser);
                if (parser.getEventType() != 1 && TAG_BOOKMARK.equals(parser.getName())) {
                    String packageName = parser.getAttributeValue(null, ATTRIBUTE_PACKAGE);
                    String className = parser.getAttributeValue(null, ATTRIBUTE_CLASS);
                    String shortcutName = parser.getAttributeValue(null, ATTRIBUTE_SHORTCUT);
                    String categoryName = parser.getAttributeValue(null, ATTRIBUTE_CATEGORY);
                    String shiftName = parser.getAttributeValue(null, ATTRIBUTE_SHIFT);
                    if (TextUtils.isEmpty(shortcutName)) {
                        Log.w(TAG, "Unable to get shortcut for: " + packageName + "/" + className);
                    } else {
                        boolean equals;
                        Intent intent;
                        String title;
                        int shortcutChar = shortcutName.charAt(0);
                        if (shiftName != null) {
                            equals = shiftName.equals("true");
                        } else {
                            equals = false;
                        }
                        if (packageName != null && className != null) {
                            ActivityInfo info;
                            ComponentName componentName = new ComponentName(packageName, className);
                            try {
                                info = packageManager.getActivityInfo(componentName, 794624);
                            } catch (NameNotFoundException e) {
                                componentName = new ComponentName(packageManager.canonicalToCurrentPackageNames(new String[]{packageName})[0], className);
                                try {
                                    info = packageManager.getActivityInfo(componentName, 794624);
                                } catch (NameNotFoundException e2) {
                                    Log.w(TAG, "Unable to add bookmark: " + packageName + "/" + className, e);
                                }
                            }
                            intent = new Intent("android.intent.action.MAIN");
                            intent.addCategory("android.intent.category.LAUNCHER");
                            intent.setComponent(componentName);
                            title = info.loadLabel(packageManager).toString();
                        } else if (categoryName != null) {
                            intent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", categoryName);
                            title = "";
                        } else {
                            Log.w(TAG, "Unable to add bookmark for shortcut " + shortcutName + ": missing package/class or category attributes");
                        }
                        ShortcutInfo shortcutInfo = new ShortcutInfo(title, intent);
                        if (equals) {
                            this.mShiftShortcuts.put(shortcutChar, shortcutInfo);
                        } else {
                            this.mShortcuts.put(shortcutChar, shortcutInfo);
                        }
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
