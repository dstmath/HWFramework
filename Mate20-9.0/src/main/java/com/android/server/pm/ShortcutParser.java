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
import com.android.internal.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public class ShortcutParser {
    private static final boolean DEBUG = false;
    @VisibleForTesting
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
                ActivityInfo activityInfoNoMetadata = activities.get(i).activityInfo;
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

    /* JADX WARNING: Removed duplicated region for block: B:136:0x02b7  */
    private static List<ShortcutInfo> parseShortcutsOneFile(ShortcutService service, ActivityInfo activityInfo, String packageName, int userId, List<ShortcutInfo> result) throws IOException, XmlPullParserException {
        XmlResourceParser parser;
        List<ShortcutInfo> result2;
        int numShortcuts;
        List<ShortcutInfo> result3;
        ArrayList<Intent> intents;
        ComponentName activity;
        AttributeSet attrs;
        AttributeSet attrs2;
        ComponentName activity2;
        int depth;
        String tag;
        AttributeSet attrs3;
        ShortcutService shortcutService = service;
        ActivityInfo activityInfo2 = activityInfo;
        try {
            parser = shortcutService.injectXmlMetaData(activityInfo2, METADATA_KEY);
            if (parser == null) {
                if (parser != null) {
                    parser.close();
                }
                return result;
            }
            try {
                ComponentName activity3 = new ComponentName(packageName, activityInfo2.name);
                AttributeSet attrs4 = Xml.asAttributeSet(parser);
                int maxShortcuts = service.getMaxActivityShortcuts();
                ArrayList<Intent> intents2 = new ArrayList<>();
                int rank = 0;
                int numShortcuts2 = 0;
                ShortcutInfo currentShortcut = null;
                Set<String> categories = null;
                List<ShortcutInfo> result4 = result;
                while (true) {
                    int numShortcuts3 = parser.next();
                    int type = numShortcuts3;
                    if (numShortcuts3 == 1) {
                        result2 = result4;
                        break;
                    }
                    if (type == 3) {
                        try {
                            if (parser.getDepth() <= 0) {
                                result2 = result4;
                                break;
                            }
                        } catch (RuntimeException e) {
                            Log.e(TAG, "Shortcut's extras contain un-persistable values. Skipping it.");
                        } catch (Throwable th) {
                            th = th;
                            List<ShortcutInfo> list = result4;
                            if (parser != null) {
                            }
                            throw th;
                        }
                    }
                    try {
                        int depth2 = parser.getDepth();
                        String tag2 = parser.getName();
                        if (type == 3) {
                            activity2 = activity3;
                            depth = depth2;
                            if (depth == 2) {
                                tag = tag2;
                                if (!TAG_SHORTCUT.equals(tag)) {
                                    attrs2 = attrs4;
                                } else if (currentShortcut == null) {
                                    intents = intents2;
                                    numShortcuts = numShortcuts2;
                                    attrs = attrs4;
                                    activity = activity2;
                                    result3 = result4;
                                    String str = packageName;
                                    activity3 = activity;
                                    intents2 = intents;
                                    result4 = result3;
                                    numShortcuts2 = numShortcuts;
                                    ActivityInfo activityInfo3 = activityInfo;
                                    attrs4 = attrs;
                                } else {
                                    ShortcutInfo si = currentShortcut;
                                    if (!si.isEnabled()) {
                                        attrs3 = attrs4;
                                        intents2.clear();
                                        intents2.add(new Intent("android.intent.action.VIEW"));
                                    } else if (intents2.size() == 0) {
                                        StringBuilder sb = new StringBuilder();
                                        attrs3 = attrs4;
                                        sb.append("Shortcut ");
                                        sb.append(si.getId());
                                        sb.append(" has no intent. Skipping it.");
                                        Log.e(TAG, sb.toString());
                                        currentShortcut = null;
                                        activity3 = activity2;
                                        attrs4 = attrs3;
                                        String str2 = packageName;
                                    } else {
                                        attrs3 = attrs4;
                                    }
                                    if (numShortcuts2 >= maxShortcuts) {
                                        Log.e(TAG, "More than " + maxShortcuts + " shortcuts found for " + activityInfo.getComponentName() + ". Skipping the rest.");
                                        if (parser != null) {
                                            parser.close();
                                        }
                                        return result4;
                                    }
                                    intents2.get(0).addFlags(268484608);
                                    si.setIntents((Intent[]) intents2.toArray(new Intent[intents2.size()]));
                                    intents2.clear();
                                    if (categories != null) {
                                        si.setCategories(categories);
                                        categories = null;
                                    }
                                    if (result4 == null) {
                                        result4 = new ArrayList<>();
                                    }
                                    result4.add(si);
                                    numShortcuts2++;
                                    rank++;
                                    currentShortcut = null;
                                    activity3 = activity2;
                                    attrs4 = attrs3;
                                    String str22 = packageName;
                                }
                            } else {
                                attrs2 = attrs4;
                                tag = tag2;
                            }
                        } else {
                            activity2 = activity3;
                            attrs2 = attrs4;
                            depth = depth2;
                            tag = tag2;
                        }
                        if (type == 2 && (depth != 1 || !TAG_SHORTCUTS.equals(tag))) {
                            if (depth == 2) {
                                if (TAG_SHORTCUT.equals(tag)) {
                                    String str3 = tag;
                                    int i = type;
                                    result3 = result4;
                                    intents = intents2;
                                    numShortcuts = numShortcuts2;
                                    try {
                                        ShortcutInfo si2 = parseShortcutAttributes(shortcutService, attrs2, packageName, activity2, userId, rank);
                                        if (si2 != null) {
                                            if (result3 != null) {
                                                int i2 = result3.size() - 1;
                                                while (i2 >= 0) {
                                                    if (si2.getId().equals(result3.get(i2).getId())) {
                                                        Log.e(TAG, "Duplicate shortcut ID detected. Skipping it.");
                                                    } else {
                                                        i2--;
                                                    }
                                                }
                                            }
                                            currentShortcut = si2;
                                            categories = null;
                                            String tag3 = packageName;
                                            intents2 = intents;
                                            result4 = result3;
                                            numShortcuts2 = numShortcuts;
                                            activity3 = activity2;
                                            attrs4 = attrs2;
                                            ActivityInfo activityInfo4 = activityInfo;
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (parser != null) {
                                        }
                                        throw th;
                                    }
                                }
                            }
                            String tag4 = tag;
                            int i3 = type;
                            result3 = result4;
                            intents = intents2;
                            numShortcuts = numShortcuts2;
                            if (depth != 3 || !"intent".equals(tag4)) {
                                activity = activity2;
                                attrs = attrs2;
                                if (depth != 3 || !TAG_CATEGORIES.equals(tag4)) {
                                    Log.w(TAG, String.format("Invalid tag '%s' found at depth %d", new Object[]{tag4, Integer.valueOf(depth)}));
                                    String str4 = packageName;
                                    activity3 = activity;
                                    intents2 = intents;
                                    result4 = result3;
                                    numShortcuts2 = numShortcuts;
                                    ActivityInfo activityInfo32 = activityInfo;
                                    attrs4 = attrs;
                                } else {
                                    if (currentShortcut != null) {
                                        if (currentShortcut.getCategories() == null) {
                                            String name = parseCategories(shortcutService, attrs);
                                            if (TextUtils.isEmpty(name)) {
                                                Log.e(TAG, "Empty category found. activity=" + activity);
                                            } else {
                                                if (categories == null) {
                                                    categories = new ArraySet<>();
                                                }
                                                categories.add(name);
                                            }
                                        }
                                    }
                                    String str42 = packageName;
                                    activity3 = activity;
                                    intents2 = intents;
                                    result4 = result3;
                                    numShortcuts2 = numShortcuts;
                                    ActivityInfo activityInfo322 = activityInfo;
                                    attrs4 = attrs;
                                }
                            } else {
                                if (currentShortcut == null) {
                                    activity = activity2;
                                    attrs = attrs2;
                                } else if (!currentShortcut.isEnabled()) {
                                    activity = activity2;
                                    attrs = attrs2;
                                } else {
                                    attrs = attrs2;
                                    Intent intent = Intent.parseIntent(shortcutService.mContext.getResources(), parser, attrs);
                                    if (TextUtils.isEmpty(intent.getAction())) {
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append("Shortcut intent action must be provided. activity=");
                                        activity = activity2;
                                        sb2.append(activity);
                                        Log.e(TAG, sb2.toString());
                                        currentShortcut = null;
                                    } else {
                                        activity = activity2;
                                        intents.add(intent);
                                        String str422 = packageName;
                                        activity3 = activity;
                                        intents2 = intents;
                                        result4 = result3;
                                        numShortcuts2 = numShortcuts;
                                        ActivityInfo activityInfo3222 = activityInfo;
                                        attrs4 = attrs;
                                    }
                                }
                                Log.e(TAG, "Ignoring excessive intent tag.");
                                String str4222 = packageName;
                                activity3 = activity;
                                intents2 = intents;
                                result4 = result3;
                                numShortcuts2 = numShortcuts;
                                ActivityInfo activityInfo32222 = activityInfo;
                                attrs4 = attrs;
                            }
                            String tag5 = packageName;
                            activity3 = activity;
                            intents2 = intents;
                            result4 = result3;
                            numShortcuts2 = numShortcuts;
                            ActivityInfo activityInfo5 = activityInfo;
                            attrs4 = attrs;
                        } else {
                            result3 = result4;
                            intents = intents2;
                            numShortcuts = numShortcuts2;
                        }
                        activity = activity2;
                        attrs = attrs2;
                        String str42222 = packageName;
                        activity3 = activity;
                        intents2 = intents;
                        result4 = result3;
                        numShortcuts2 = numShortcuts;
                        ActivityInfo activityInfo322222 = activityInfo;
                        attrs4 = attrs;
                    } catch (Throwable th3) {
                        th = th3;
                        List<ShortcutInfo> list2 = result4;
                        if (parser != null) {
                        }
                        throw th;
                    }
                }
                if (parser != null) {
                    parser.close();
                }
                return result2;
            } catch (Throwable th4) {
                th = th4;
                List<ShortcutInfo> list3 = result;
                if (parser != null) {
                }
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            List<ShortcutInfo> list4 = result;
            parser = null;
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    private static String parseCategories(ShortcutService service, AttributeSet attrs) {
        TypedArray sa = service.mContext.getResources().obtainAttributes(attrs, R.styleable.ShortcutCategories);
        try {
            if (sa.getType(0) == 3) {
                return sa.getNonResourceString(0);
            }
            Log.w(TAG, "android:name for shortcut category must be string literal.");
            sa.recycle();
            return null;
        } finally {
            sa.recycle();
        }
    }

    private static ShortcutInfo parseShortcutAttributes(ShortcutService service, AttributeSet attrs, String packageName, ComponentName activity, int userId, int rank) {
        ComponentName componentName = activity;
        ShortcutService shortcutService = service;
        TypedArray sa = shortcutService.mContext.getResources().obtainAttributes(attrs, R.styleable.Shortcut);
        try {
            if (sa.getType(2) != 3) {
                Log.w(TAG, "android:shortcutId must be string literal. activity=" + componentName);
                return null;
            }
            String id = sa.getNonResourceString(2);
            boolean enabled = sa.getBoolean(1, true);
            int iconResId = sa.getResourceId(0, 0);
            int titleResId = sa.getResourceId(3, 0);
            int textResId = sa.getResourceId(4, 0);
            int disabledMessageResId = sa.getResourceId(5, 0);
            if (TextUtils.isEmpty(id)) {
                Log.w(TAG, "android:shortcutId must be provided. activity=" + componentName);
                sa.recycle();
                return null;
            } else if (titleResId == 0) {
                Log.w(TAG, "android:shortcutShortLabel must be provided. activity=" + componentName);
                sa.recycle();
                return null;
            } else {
                ShortcutInfo createShortcutFromManifest = createShortcutFromManifest(shortcutService, userId, id, packageName, componentName, titleResId, textResId, disabledMessageResId, rank, iconResId, enabled);
                sa.recycle();
                return createShortcutFromManifest;
            }
        } finally {
            sa.recycle();
        }
    }

    private static ShortcutInfo createShortcutFromManifest(ShortcutService service, int userId, String id, String packageName, ComponentName activityComponent, int titleResId, int textResId, int disabledMessageResId, int rank, int iconResId, boolean enabled) {
        int disabledReason = 0;
        int flags = (enabled ? 32 : 64) | 256 | (iconResId != 0 ? 4 : 0);
        if (!enabled) {
            disabledReason = 1;
        }
        ShortcutInfo shortcutInfo = new ShortcutInfo(userId, id, packageName, activityComponent, null, null, titleResId, null, null, textResId, null, null, disabledMessageResId, null, null, null, rank, null, service.injectCurrentTimeMillis(), flags, iconResId, null, null, disabledReason);
        return shortcutInfo;
    }
}
