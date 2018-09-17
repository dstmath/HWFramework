package com.android.server.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public class ShortcutParser {
    private static final boolean DEBUG = false;
    static final String METADATA_KEY = "android.app.shortcuts";
    private static final String TAG = "ShortcutService";
    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_INTENT = "intent";
    private static final String TAG_SHORTCUT = "shortcut";
    private static final String TAG_SHORTCUTS = "shortcuts";

    public static List<ShortcutInfo> parseShortcuts(ShortcutService service, String packageName, int userId) throws IOException, XmlPullParserException {
        List<ResolveInfo> activities = service.injectGetMainActivities(packageName, userId);
        if (activities == null || activities.size() == 0) {
            return null;
        }
        List<ShortcutInfo> result = null;
        try {
            int size = activities.size();
            for (int i = 0; i < size; i++) {
                ActivityInfo activityInfoNoMetadata = ((ResolveInfo) activities.get(i)).activityInfo;
                if (activityInfoNoMetadata != null) {
                    ActivityInfo activityInfoWithMetadata = service.getActivityInfoWithMetadata(activityInfoNoMetadata.getComponentName(), userId);
                    if (activityInfoWithMetadata != null) {
                        result = parseShortcutsOneFile(service, activityInfoWithMetadata, packageName, userId, result);
                    }
                }
            }
            return result;
        } catch (RuntimeException e) {
            service.wtf("Exception caught while parsing shortcut XML for package=" + packageName, e);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:108:0x025f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static List<ShortcutInfo> parseShortcutsOneFile(ShortcutService service, ActivityInfo activityInfo, String packageName, int userId, List<ShortcutInfo> result) throws IOException, XmlPullParserException {
        Throwable th;
        XmlResourceParser parser = null;
        try {
            parser = service.injectXmlMetaData(activityInfo, METADATA_KEY);
            if (parser == null) {
                if (parser != null) {
                    parser.close();
                }
                return result;
            }
            ComponentName activity = new ComponentName(packageName, activityInfo.name);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            int rank = 0;
            int maxShortcuts = service.getMaxActivityShortcuts();
            int numShortcuts = 0;
            ShortcutInfo currentShortcut = null;
            Set categories = null;
            ArrayList<Intent> intents = new ArrayList();
            List<ShortcutInfo> result2 = result;
            while (true) {
                try {
                    int type = parser.next();
                    if (type != 1 && (type != 3 || parser.getDepth() > 0)) {
                        int depth = parser.getDepth();
                        String tag = parser.getName();
                        ShortcutInfo si;
                        if (type == 3 && depth == 2 && TAG_SHORTCUT.equals(tag)) {
                            if (currentShortcut != null) {
                                si = currentShortcut;
                                currentShortcut = null;
                                if (!si.isEnabled()) {
                                    intents.clear();
                                    intents.add(new Intent("android.intent.action.VIEW"));
                                } else if (intents.size() == 0) {
                                    Log.e(TAG, "Shortcut " + si.getId() + " has no intent. Skipping it.");
                                }
                                if (numShortcuts >= maxShortcuts) {
                                    Log.e(TAG, "More than " + maxShortcuts + " shortcuts found for " + activityInfo.getComponentName() + ". Skipping the rest.");
                                    if (parser != null) {
                                        parser.close();
                                    }
                                    return result2;
                                }
                                ((Intent) intents.get(0)).addFlags(268484608);
                                si.setIntents((Intent[]) intents.toArray(new Intent[intents.size()]));
                                intents.clear();
                                if (categories != null) {
                                    si.setCategories(categories);
                                    categories = null;
                                }
                                if (result2 == null) {
                                    result = new ArrayList();
                                } else {
                                    result = result2;
                                }
                                result.add(si);
                                numShortcuts++;
                                rank++;
                                result2 = result;
                            } else {
                                continue;
                            }
                        } else if (type == 2 && !(depth == 1 && TAG_SHORTCUTS.equals(tag))) {
                            if (depth == 2 && TAG_SHORTCUT.equals(tag)) {
                                si = parseShortcutAttributes(service, attrs, packageName, activity, userId, rank);
                                if (si != null) {
                                    if (result2 != null) {
                                        for (int i = result2.size() - 1; i >= 0; i--) {
                                            if (si.getId().equals(((ShortcutInfo) result2.get(i)).getId())) {
                                                Log.e(TAG, "Duplicate shortcut ID detected. Skipping it.");
                                                break;
                                            }
                                        }
                                    }
                                    currentShortcut = si;
                                    categories = null;
                                }
                            } else if (depth == 3 && "intent".equals(tag)) {
                                if (currentShortcut == null || (currentShortcut.isEnabled() ^ 1) != 0) {
                                    Log.e(TAG, "Ignoring excessive intent tag.");
                                } else {
                                    Intent intent = Intent.parseIntent(service.mContext.getResources(), parser, attrs);
                                    if (TextUtils.isEmpty(intent.getAction())) {
                                        Log.e(TAG, "Shortcut intent action must be provided. activity=" + activity);
                                        currentShortcut = null;
                                    } else {
                                        intents.add(intent);
                                    }
                                }
                            } else if (depth != 3 || !TAG_CATEGORIES.equals(tag)) {
                                Log.w(TAG, String.format("Invalid tag '%s' found at depth %d", new Object[]{tag, Integer.valueOf(depth)}));
                            } else if (currentShortcut != null && currentShortcut.getCategories() == null) {
                                String name = parseCategories(service, attrs);
                                if (TextUtils.isEmpty(name)) {
                                    Log.e(TAG, "Empty category found. activity=" + activity);
                                } else {
                                    if (categories == null) {
                                        categories = new ArraySet();
                                    }
                                    categories.add(name);
                                }
                            }
                        }
                    } else if (parser != null) {
                        parser.close();
                    }
                } catch (RuntimeException e) {
                    Log.e(TAG, "Shortcut's extras contain un-persistable values. Skipping it.");
                } catch (Throwable th2) {
                    th = th2;
                    result = result2;
                }
            }
            if (parser != null) {
            }
            return result2;
        } catch (Throwable th3) {
            th = th3;
        }
        if (parser != null) {
            parser.close();
        }
        throw th;
    }

    private static String parseCategories(ShortcutService service, AttributeSet attrs) {
        TypedArray sa = service.mContext.getResources().obtainAttributes(attrs, R.styleable.ShortcutCategories);
        try {
            if (sa.getType(0) == 3) {
                String nonResourceString = sa.getNonResourceString(0);
                return nonResourceString;
            }
            Log.w(TAG, "android:name for shortcut category must be string literal.");
            sa.recycle();
            return null;
        } finally {
            sa.recycle();
        }
    }

    private static ShortcutInfo parseShortcutAttributes(ShortcutService service, AttributeSet attrs, String packageName, ComponentName activity, int userId, int rank) {
        TypedArray sa = service.mContext.getResources().obtainAttributes(attrs, R.styleable.Shortcut);
        try {
            if (sa.getType(2) != 3) {
                Log.w(TAG, "android:shortcutId must be string literal. activity=" + activity);
                return null;
            }
            String id = sa.getNonResourceString(2);
            boolean enabled = sa.getBoolean(1, true);
            int iconResId = sa.getResourceId(0, 0);
            int titleResId = sa.getResourceId(3, 0);
            int textResId = sa.getResourceId(4, 0);
            int disabledMessageResId = sa.getResourceId(5, 0);
            if (TextUtils.isEmpty(id)) {
                Log.w(TAG, "android:shortcutId must be provided. activity=" + activity);
                sa.recycle();
                return null;
            } else if (titleResId == 0) {
                Log.w(TAG, "android:shortcutShortLabel must be provided. activity=" + activity);
                sa.recycle();
                return null;
            } else {
                ShortcutInfo createShortcutFromManifest = createShortcutFromManifest(service, userId, id, packageName, activity, titleResId, textResId, disabledMessageResId, rank, iconResId, enabled);
                sa.recycle();
                return createShortcutFromManifest;
            }
        } finally {
            sa.recycle();
        }
    }

    private static ShortcutInfo createShortcutFromManifest(ShortcutService service, int userId, String id, String packageName, ComponentName activityComponent, int titleResId, int textResId, int disabledMessageResId, int rank, int iconResId, boolean enabled) {
        return new ShortcutInfo(userId, id, packageName, activityComponent, null, null, titleResId, null, null, textResId, null, null, disabledMessageResId, null, null, null, rank, null, service.injectCurrentTimeMillis(), ((enabled ? 32 : 64) | 256) | (iconResId != 0 ? 4 : 0), iconResId, null, null);
    }
}
