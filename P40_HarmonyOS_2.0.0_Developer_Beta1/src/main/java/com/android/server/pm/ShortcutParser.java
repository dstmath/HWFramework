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
import com.android.server.pm.ShareTargetInfo;
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
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_DATA = "data";
    private static final String TAG_INTENT = "intent";
    private static final String TAG_SHARE_TARGET = "share-target";
    private static final String TAG_SHORTCUT = "shortcut";
    private static final String TAG_SHORTCUTS = "shortcuts";

    public static List<ShortcutInfo> parseShortcuts(ShortcutService service, String packageName, int userId, List<ShareTargetInfo> outShareTargets) throws IOException, XmlPullParserException {
        RuntimeException e;
        List<ResolveInfo> activities = service.injectGetMainActivities(packageName, userId);
        if (activities != null) {
            if (activities.size() != 0) {
                outShareTargets.clear();
                try {
                    int size = activities.size();
                    List<ShortcutInfo> result = null;
                    for (int i = 0; i < size; i++) {
                        try {
                            ActivityInfo activityInfoNoMetadata = activities.get(i).activityInfo;
                            if (activityInfoNoMetadata != null) {
                                try {
                                    ActivityInfo activityInfoWithMetadata = service.getActivityInfoWithMetadata(activityInfoNoMetadata.getComponentName(), userId);
                                    if (activityInfoWithMetadata != null) {
                                        result = parseShortcutsOneFile(service, activityInfoWithMetadata, packageName, userId, result, outShareTargets);
                                    }
                                } catch (RuntimeException e2) {
                                    e = e2;
                                    service.wtf("Exception caught while parsing shortcut XML for package=" + packageName, e);
                                    return null;
                                }
                            }
                        } catch (RuntimeException e3) {
                            e = e3;
                            service.wtf("Exception caught while parsing shortcut XML for package=" + packageName, e);
                            return null;
                        }
                    }
                    return result;
                } catch (RuntimeException e4) {
                    e = e4;
                    service.wtf("Exception caught while parsing shortcut XML for package=" + packageName, e);
                    return null;
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:195:0x0437  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01f8 A[Catch:{ all -> 0x01e7, all -> 0x0214 }] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0200 A[Catch:{ all -> 0x01e7, all -> 0x0214 }] */
    private static List<ShortcutInfo> parseShortcutsOneFile(ShortcutService service, ActivityInfo activityInfo, String packageName, int userId, List<ShortcutInfo> result, List<ShareTargetInfo> outShareTargets) throws IOException, XmlPullParserException {
        XmlResourceParser parser;
        Throwable th;
        ComponentName activity;
        String tag;
        int depth;
        Set<String> categories;
        int maxShortcuts;
        int numShortcuts;
        ShareTargetInfo currentShareTarget;
        ComponentName activity2;
        ShortcutService shortcutService;
        int maxShortcuts2;
        int numShortcuts2;
        ShareTargetInfo currentShareTarget2;
        ComponentName activity3;
        ShortcutService shortcutService2 = service;
        try {
            parser = shortcutService2.injectXmlMetaData(activityInfo, METADATA_KEY);
            if (parser == null) {
                if (parser != null) {
                    parser.close();
                }
                return result;
            }
            try {
                ComponentName activity4 = new ComponentName(packageName, activityInfo.name);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                int maxShortcuts3 = service.getMaxActivityShortcuts();
                ShareTargetInfo currentShareTarget3 = null;
                Set<String> categories2 = null;
                ArrayList<Intent> intents = new ArrayList<>();
                ArrayList<ShareTargetInfo.TargetData> dataList = new ArrayList<>();
                List<ShortcutInfo> result2 = result;
                int rank = 0;
                int numShortcuts3 = 0;
                ShortcutInfo currentShortcut = null;
                while (true) {
                    try {
                        int type = parser.next();
                        if (type == 1) {
                            break;
                        }
                        if (type == 3) {
                            if (parser.getDepth() <= 0) {
                                break;
                            }
                        }
                        int depth2 = parser.getDepth();
                        String tag2 = parser.getName();
                        if (type == 3) {
                            depth = depth2;
                            if (depth == 2) {
                                tag = tag2;
                                try {
                                    if (!TAG_SHORTCUT.equals(tag)) {
                                        activity = activity4;
                                    } else if (currentShortcut == null) {
                                        result = result2;
                                        activity = activity4;
                                        shortcutService2 = service;
                                        result2 = result;
                                        activity4 = activity;
                                    } else {
                                        if (!currentShortcut.isEnabled()) {
                                            activity3 = activity4;
                                            intents.clear();
                                            intents.add(new Intent("android.intent.action.VIEW"));
                                        } else if (intents.size() == 0) {
                                            StringBuilder sb = new StringBuilder();
                                            activity3 = activity4;
                                            sb.append("Shortcut ");
                                            sb.append(currentShortcut.getId());
                                            sb.append(" has no intent. Skipping it.");
                                            Log.e(TAG, sb.toString());
                                            shortcutService2 = service;
                                            currentShortcut = null;
                                            activity4 = activity3;
                                        } else {
                                            activity3 = activity4;
                                        }
                                        if (numShortcuts3 >= maxShortcuts3) {
                                            Log.e(TAG, "More than " + maxShortcuts3 + " shortcuts found for " + activityInfo.getComponentName() + ". Skipping the rest.");
                                            parser.close();
                                            return result2;
                                        }
                                        intents.get(0).addFlags(268484608);
                                        try {
                                            currentShortcut.setIntents((Intent[]) intents.toArray(new Intent[intents.size()]));
                                            intents.clear();
                                            if (categories2 != null) {
                                                currentShortcut.setCategories(categories2);
                                                categories2 = null;
                                            }
                                            if (result2 == null) {
                                                result2 = new ArrayList<>();
                                            }
                                            result2.add(currentShortcut);
                                            numShortcuts3++;
                                            rank++;
                                            shortcutService2 = service;
                                            currentShortcut = null;
                                            activity4 = activity3;
                                        } catch (RuntimeException e) {
                                            Log.e(TAG, "Shortcut's extras contain un-persistable values. Skipping it.");
                                        }
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (parser != null) {
                                    }
                                    throw th;
                                }
                            } else {
                                tag = tag2;
                                activity = activity4;
                            }
                        } else {
                            depth = depth2;
                            tag = tag2;
                            activity = activity4;
                        }
                        result = result2;
                        if (type == 3 && depth == 2) {
                            try {
                                if (TAG_SHARE_TARGET.equals(tag)) {
                                    if (currentShareTarget3 != null) {
                                        if (categories2 == null || categories2.isEmpty()) {
                                            currentShareTarget2 = null;
                                            numShortcuts2 = numShortcuts3;
                                            maxShortcuts2 = maxShortcuts3;
                                        } else if (dataList.isEmpty()) {
                                            currentShareTarget2 = null;
                                            numShortcuts2 = numShortcuts3;
                                            maxShortcuts2 = maxShortcuts3;
                                        } else {
                                            currentShareTarget = null;
                                            numShortcuts = numShortcuts3;
                                            maxShortcuts = maxShortcuts3;
                                            outShareTargets.add(new ShareTargetInfo((ShareTargetInfo.TargetData[]) dataList.toArray(new ShareTargetInfo.TargetData[dataList.size()]), currentShareTarget3.mTargetClass, (String[]) categories2.toArray(new String[categories2.size()])));
                                            dataList.clear();
                                            categories = null;
                                            if (type == 2) {
                                                shortcutService = service;
                                                result2 = result;
                                                activity2 = activity;
                                            } else if (depth != 1 || !TAG_SHORTCUTS.equals(tag)) {
                                                if (depth == 2) {
                                                    try {
                                                        if (TAG_SHORTCUT.equals(tag)) {
                                                            try {
                                                                ShortcutInfo si = parseShortcutAttributes(service, attrs, packageName, activity, userId, rank);
                                                                if (si == null) {
                                                                    shortcutService = service;
                                                                    result2 = result;
                                                                    activity2 = activity;
                                                                } else {
                                                                    if (result != null) {
                                                                        for (int i = result.size() - 1; i >= 0; i--) {
                                                                            if (si.getId().equals(result.get(i).getId())) {
                                                                                Log.e(TAG, "Duplicate shortcut ID detected. Skipping it.");
                                                                                shortcutService = service;
                                                                                result2 = result;
                                                                                activity2 = activity;
                                                                            }
                                                                        }
                                                                    }
                                                                    currentShortcut = si;
                                                                    categories2 = null;
                                                                    shortcutService2 = service;
                                                                    result2 = result;
                                                                    activity4 = activity;
                                                                    currentShareTarget3 = currentShareTarget;
                                                                    numShortcuts3 = numShortcuts;
                                                                    maxShortcuts3 = maxShortcuts;
                                                                }
                                                            } catch (Throwable th3) {
                                                                th = th3;
                                                                if (parser != null) {
                                                                }
                                                                throw th;
                                                            }
                                                        }
                                                    } catch (Throwable th4) {
                                                        th = th4;
                                                        if (parser != null) {
                                                        }
                                                        throw th;
                                                    }
                                                }
                                                result2 = result;
                                                if (depth == 2) {
                                                    try {
                                                        if (TAG_SHARE_TARGET.equals(tag)) {
                                                            shortcutService = service;
                                                            ShareTargetInfo sti = parseShareTargetAttributes(shortcutService, attrs);
                                                            if (sti == null) {
                                                                activity2 = activity;
                                                            } else {
                                                                currentShareTarget3 = sti;
                                                                categories2 = null;
                                                                dataList.clear();
                                                                shortcutService2 = shortcutService;
                                                                activity4 = activity;
                                                                numShortcuts3 = numShortcuts;
                                                                maxShortcuts3 = maxShortcuts;
                                                            }
                                                        }
                                                    } catch (Throwable th5) {
                                                        th = th5;
                                                        if (parser != null) {
                                                        }
                                                        throw th;
                                                    }
                                                }
                                                shortcutService = service;
                                                if (depth != 3 || !"intent".equals(tag)) {
                                                    activity2 = activity;
                                                    if (depth != 3 || !TAG_CATEGORIES.equals(tag)) {
                                                        if (depth != 3 || !TAG_CATEGORY.equals(tag)) {
                                                            if (depth != 3 || !"data".equals(tag)) {
                                                                Log.w(TAG, String.format("Invalid tag '%s' found at depth %d", tag, Integer.valueOf(depth)));
                                                            } else if (currentShareTarget != null) {
                                                                ShareTargetInfo.TargetData data = parseShareTargetData(shortcutService, attrs);
                                                                if (data == null) {
                                                                    Log.e(TAG, "Invalid data tag found. activity=" + activity2);
                                                                } else {
                                                                    dataList.add(data);
                                                                }
                                                            }
                                                        } else if (currentShareTarget != null) {
                                                            String name = parseCategory(shortcutService, attrs);
                                                            if (TextUtils.isEmpty(name)) {
                                                                Log.e(TAG, "Empty category found. activity=" + activity2);
                                                            } else {
                                                                if (categories == null) {
                                                                    categories2 = new ArraySet<>();
                                                                } else {
                                                                    categories2 = categories;
                                                                }
                                                                categories2.add(name);
                                                                shortcutService2 = shortcutService;
                                                                activity4 = activity2;
                                                                currentShareTarget3 = currentShareTarget;
                                                                numShortcuts3 = numShortcuts;
                                                                maxShortcuts3 = maxShortcuts;
                                                            }
                                                        }
                                                    } else if (currentShortcut != null && currentShortcut.getCategories() == null) {
                                                        String name2 = parseCategories(shortcutService, attrs);
                                                        if (TextUtils.isEmpty(name2)) {
                                                            Log.e(TAG, "Empty category found. activity=" + activity2);
                                                        } else {
                                                            if (categories == null) {
                                                                categories2 = new ArraySet<>();
                                                            } else {
                                                                categories2 = categories;
                                                            }
                                                            categories2.add(name2);
                                                            shortcutService2 = shortcutService;
                                                            activity4 = activity2;
                                                            currentShareTarget3 = currentShareTarget;
                                                            numShortcuts3 = numShortcuts;
                                                            maxShortcuts3 = maxShortcuts;
                                                        }
                                                    }
                                                } else {
                                                    if (currentShortcut == null) {
                                                        activity2 = activity;
                                                    } else if (!currentShortcut.isEnabled()) {
                                                        activity2 = activity;
                                                    } else {
                                                        Intent intent = Intent.parseIntent(shortcutService.mContext.getResources(), parser, attrs);
                                                        if (TextUtils.isEmpty(intent.getAction())) {
                                                            Log.e(TAG, "Shortcut intent action must be provided. activity=" + activity);
                                                            currentShortcut = null;
                                                            shortcutService2 = shortcutService;
                                                            activity4 = activity;
                                                            currentShareTarget3 = currentShareTarget;
                                                            numShortcuts3 = numShortcuts;
                                                            maxShortcuts3 = maxShortcuts;
                                                            categories2 = categories;
                                                        } else {
                                                            activity2 = activity;
                                                            intents.add(intent);
                                                        }
                                                    }
                                                    Log.e(TAG, "Ignoring excessive intent tag.");
                                                }
                                            } else {
                                                shortcutService = service;
                                                result2 = result;
                                                activity2 = activity;
                                            }
                                            shortcutService2 = shortcutService;
                                            activity4 = activity2;
                                            currentShareTarget3 = currentShareTarget;
                                            numShortcuts3 = numShortcuts;
                                            maxShortcuts3 = maxShortcuts;
                                            categories2 = categories;
                                        }
                                        shortcutService2 = service;
                                        result2 = result;
                                        activity4 = activity;
                                        currentShareTarget3 = currentShareTarget2;
                                        numShortcuts3 = numShortcuts2;
                                        maxShortcuts3 = maxShortcuts2;
                                    }
                                    shortcutService2 = service;
                                    result2 = result;
                                    activity4 = activity;
                                } else {
                                    numShortcuts = numShortcuts3;
                                    maxShortcuts = maxShortcuts3;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                if (parser != null) {
                                }
                                throw th;
                            }
                        } else {
                            numShortcuts = numShortcuts3;
                            maxShortcuts = maxShortcuts3;
                        }
                        currentShareTarget = currentShareTarget3;
                        categories = categories2;
                        if (type == 2) {
                        }
                        shortcutService2 = shortcutService;
                        activity4 = activity2;
                        currentShareTarget3 = currentShareTarget;
                        numShortcuts3 = numShortcuts;
                        maxShortcuts3 = maxShortcuts;
                        categories2 = categories;
                    } catch (Throwable th7) {
                        th = th7;
                        if (parser != null) {
                            parser.close();
                        }
                        throw th;
                    }
                }
                parser.close();
                return result2;
            } catch (Throwable th8) {
                th = th8;
                if (parser != null) {
                }
                throw th;
            }
        } catch (Throwable th9) {
            th = th9;
            parser = null;
            if (parser != null) {
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
        int disabledReason;
        int flags = (enabled ? 32 : 64) | 256 | (iconResId != 0 ? 4 : 0);
        if (enabled) {
            disabledReason = 0;
        } else {
            disabledReason = 1;
        }
        return new ShortcutInfo(userId, id, packageName, activityComponent, null, null, titleResId, null, null, textResId, null, null, disabledMessageResId, null, null, null, rank, null, service.injectCurrentTimeMillis(), flags, iconResId, null, null, disabledReason, null, null);
    }

    private static String parseCategory(ShortcutService service, AttributeSet attrs) {
        TypedArray sa = service.mContext.getResources().obtainAttributes(attrs, R.styleable.IntentCategory);
        try {
            if (sa.getType(0) != 3) {
                Log.w(TAG, "android:name must be string literal.");
                return null;
            }
            String string = sa.getString(0);
            sa.recycle();
            return string;
        } finally {
            sa.recycle();
        }
    }

    private static ShareTargetInfo parseShareTargetAttributes(ShortcutService service, AttributeSet attrs) {
        TypedArray sa = service.mContext.getResources().obtainAttributes(attrs, R.styleable.Intent);
        try {
            String targetClass = sa.getString(4);
            if (TextUtils.isEmpty(targetClass)) {
                Log.w(TAG, "android:targetClass must be provided.");
                return null;
            }
            ShareTargetInfo shareTargetInfo = new ShareTargetInfo(null, targetClass, null);
            sa.recycle();
            return shareTargetInfo;
        } finally {
            sa.recycle();
        }
    }

    private static ShareTargetInfo.TargetData parseShareTargetData(ShortcutService service, AttributeSet attrs) {
        TypedArray sa = service.mContext.getResources().obtainAttributes(attrs, R.styleable.AndroidManifestData);
        try {
            if (sa.getType(0) != 3) {
                Log.w(TAG, "android:mimeType must be string literal.");
                return null;
            }
            ShareTargetInfo.TargetData targetData = new ShareTargetInfo.TargetData(sa.getString(1), sa.getString(2), sa.getString(3), sa.getString(4), sa.getString(6), sa.getString(5), sa.getString(0));
            sa.recycle();
            return targetData;
        } finally {
            sa.recycle();
        }
    }
}
