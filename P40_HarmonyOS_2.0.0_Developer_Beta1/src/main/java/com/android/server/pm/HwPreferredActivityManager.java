package com.android.server.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.LogPrinter;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwPreferredActivityManager {
    private static final int DOC_TYPE_EXCEL = 8;
    private static final int DOC_TYPE_PPT = 4;
    private static final int DOC_TYPE_WORD = 2;
    private static final int FILE_TYPE_UNKOWN = 1;
    private static final boolean IS_HW_PREFER_APP_POLICY = SystemProperties.getBoolean("hw_mc.pms.prefer_app_policy", true);
    private static final String TAG = "HwPreferredActivityManager";
    private static HwPreferredActivityManager sInstance;
    private final Context mContext;
    private final String[] mDocExcelMimes = this.mContext.getResources().getStringArray(33816591);
    private final String[] mDocPptMimes = this.mContext.getResources().getStringArray(33816592);
    private final String[] mDocWordMimes = this.mContext.getResources().getStringArray(33816594);
    private final PackageManagerService mPms;
    private final Settings mSettings;

    private HwPreferredActivityManager(Context context, Settings settings, PackageManagerService pms) {
        this.mSettings = settings;
        this.mContext = context;
        this.mPms = pms;
    }

    public static synchronized HwPreferredActivityManager getInstance(Context context, Settings settings, PackageManagerService pms) {
        HwPreferredActivityManager hwPreferredActivityManager;
        synchronized (HwPreferredActivityManager.class) {
            if (sInstance == null) {
                sInstance = new HwPreferredActivityManager(context, settings, pms);
            }
            hwPreferredActivityManager = sInstance;
        }
        return hwPreferredActivityManager;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00bd, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c0, code lost:
        android.util.Slog.e(com.android.server.pm.HwPreferredActivityManager.TAG, "resolve preferred activity error, occured exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c8, code lost:
        return false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00bf A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:15:0x003b] */
    public boolean resolvePreferredActivity(IntentFilter filter, int match, ComponentName[] sets, ComponentName activity, int userId) {
        boolean z;
        boolean z2;
        boolean z3;
        if (!IS_HW_PREFER_APP_POLICY) {
            Slog.i(TAG, "do nothing, prefer app policy is not hw policy");
            return false;
        }
        if (filter == null) {
            z = false;
        } else if (activity == null) {
            z = false;
        } else {
            if (!filter.hasAction("android.intent.action.VIEW") || !filter.hasCategory("android.intent.category.DEFAULT")) {
                z2 = false;
            } else if (filter.countDataTypes() == 0) {
                z2 = false;
            } else {
                Slog.i(TAG, "start resolve preferred activity");
                try {
                    LogPrinter logPrinter = new LogPrinter(4, TAG);
                    filter.dump(logPrinter, "before derive: ");
                    Map<Integer, IntentFilter> derivedFilters = deriveIntentFilters(filter);
                    filter.dump(logPrinter, "after derive: ");
                    synchronized (this.mPms.getPackagesLock()) {
                        PreferredIntentResolver preferredIntentResolver = this.mSettings.editPreferredActivitiesLPw(userId);
                        for (Integer fileType : derivedFilters.keySet()) {
                            if (fileType.intValue() == 1) {
                                Slog.i(TAG, "fileType is unkown");
                                preferredIntentResolver.addFilter(new PreferredActivity(filter, match, sets, activity, true));
                            } else {
                                adjustPreferredActivityLocked(preferredIntentResolver, fileType.intValue(), derivedFilters.get(fileType), activity, userId);
                            }
                        }
                        this.mPms.scheduleWritePackageRestrictionsLocked(userId);
                    }
                    return true;
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    z3 = false;
                } catch (Exception e2) {
                }
            }
            Slog.i(TAG, "the filter does not meet the requirement");
            return z2;
        }
        Slog.i(TAG, "the filter or activity is null");
        return z;
        Slog.e(TAG, "resolve preferred activity error, occured MalformedMimeTypeException");
        return z3;
    }

    private Map<Integer, IntentFilter> deriveIntentFilters(IntentFilter filter) {
        Map<Integer, IntentFilter> derivedIntentFilters = new HashMap<>();
        int mixedFileType = determineFileTypes(filter);
        addDeriveIntentFilters(filter, derivedIntentFilters, mixedFileType, 2);
        addDeriveIntentFilters(filter, derivedIntentFilters, mixedFileType, 4);
        addDeriveIntentFilters(filter, derivedIntentFilters, mixedFileType, 8);
        if (filter.countDataTypes() != 0) {
            derivedIntentFilters.put(1, filter);
        }
        return derivedIntentFilters;
    }

    private int determineFileTypes(IntentFilter filter) {
        Iterator<String> iter = filter.typesIterator();
        int typeFlags = 0;
        while (iter.hasNext()) {
            String type = iter.next();
            if (ArrayUtils.contains(this.mDocWordMimes, type)) {
                typeFlags |= 2;
            } else if (ArrayUtils.contains(this.mDocPptMimes, type)) {
                typeFlags |= 4;
            } else if (ArrayUtils.contains(this.mDocExcelMimes, type)) {
                typeFlags |= 8;
            } else {
                typeFlags |= 1;
            }
        }
        return typeFlags;
    }

    private void addDeriveIntentFilters(IntentFilter filter, Map derivedIntentFilters, int mixedFileType, int fileType) {
        if ((mixedFileType & fileType) != 0) {
            derivedIntentFilters.put(Integer.valueOf(fileType), deriveIntentFilter(fileType, filter));
        }
    }

    private IntentFilter deriveIntentFilter(int fileType, IntentFilter filter) {
        String[] derivedDocTypes = getDataTypes(fileType);
        IntentFilter derivedFilter = new IntentFilter(filter);
        Iterator<String> iter = derivedFilter.typesIterator();
        while (iter.hasNext()) {
            String type = iter.next();
            if (!ArrayUtils.contains(derivedDocTypes, type)) {
                Slog.i(TAG, "remove non-doc type: " + type + " in derived filter");
                iter.remove();
            }
        }
        Iterator<String> iter2 = filter.typesIterator();
        while (iter2.hasNext()) {
            String type2 = iter2.next();
            if (ArrayUtils.contains(derivedDocTypes, type2)) {
                Slog.i(TAG, "remove doc type: " + type2 + " in original filter");
                iter2.remove();
            }
        }
        return derivedFilter;
    }

    private String[] getDataTypes(int fileType) {
        if (fileType == 2) {
            return this.mDocWordMimes;
        }
        if (fileType == 4) {
            return this.mDocPptMimes;
        }
        if (fileType != 8) {
            return new String[0];
        }
        return this.mDocExcelMimes;
    }

    private void adjustPreferredActivityLocked(PreferredIntentResolver preferredIntentResolver, int fileType, IntentFilter filter, ComponentName activity, int userId) throws IntentFilter.MalformedMimeTypeException {
        Slog.i(TAG, "adjustPreferredActivityLocked: " + fileType);
        String[] dataTypes = getDataTypes(fileType);
        if (dataTypes.length == 0) {
            Slog.i(TAG, "dataTypes is empty for fileType: " + fileType);
        } else if (this.mPms.isUpgrade() || !isAlreadyPreferredActivity(preferredIntentResolver, filter, activity, userId)) {
            clearPreferActivity(fileType, preferredIntentResolver);
            addPreferActivity(activity, userId, preferredIntentResolver, dataTypes, "content");
            addPreferActivity(activity, userId, preferredIntentResolver, dataTypes, "file");
        }
    }

    private boolean isAlreadyPreferredActivity(PreferredIntentResolver preferredIntentResolver, IntentFilter filter, ComponentName activity, int userId) {
        ArrayList<PreferredActivity> existings = preferredIntentResolver.findFilters(filter);
        if (existings == null) {
            Slog.i(TAG, "not find preferred activity with " + activity);
            return false;
        }
        Iterator<PreferredActivity> it = existings.iterator();
        while (it.hasNext()) {
            PreferredActivity cur = it.next();
            if (cur != null && cur.mPref != null && cur.mPref.mAlways && activity.equals(cur.mPref.mComponent)) {
                Slog.i(TAG, activity + " is already preferred activity.");
                return true;
            }
        }
        return false;
    }

    private void clearPreferActivity(int fileType, PreferredIntentResolver preferredIntentResolver) {
        Iterator<PreferredActivity> iter = preferredIntentResolver.filterIterator();
        while (iter.hasNext()) {
            PreferredActivity preferActivity = iter.next();
            if (preferActivity == null || preferActivity.mPref == null) {
                Slog.i(TAG, "preferActivity is null");
            } else if (isContainDocType(preferActivity, fileType)) {
                iter.remove();
            }
        }
    }

    private void addPreferActivity(ComponentName activity, int userId, PreferredIntentResolver preferredIntentResolver, String[] dataTypes, String scheme) throws IntentFilter.MalformedMimeTypeException {
        String[] strArr = dataTypes;
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        int length = strArr.length;
        int i = 0;
        while (i < length) {
            String type = strArr[i];
            intent.setDataAndType(Uri.parse(scheme + "://"), type);
            Slog.i(TAG, "addPreferActivity: start--> query intent activities for " + scheme + AwarenessInnerConstants.COLON_KEY + type);
            List<ResolveInfo> resolveInfos = this.mPms.queryIntentActivitiesInternal(intent, type, 851968, userId);
            boolean isContainsActivity = false;
            int len = resolveInfos.size();
            ComponentName[] sets = new ComponentName[len];
            int bestMatch = 0;
            int i2 = 0;
            while (i2 < len) {
                ResolveInfo resolveInfo = resolveInfos.get(i2);
                sets[i2] = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                if (resolveInfo.match > bestMatch) {
                    bestMatch = resolveInfo.match;
                }
                Slog.i(TAG, "queried result[" + i2 + "]=" + sets[i2]);
                if (activity.equals(sets[i2])) {
                    isContainsActivity = true;
                }
                i2++;
                length = length;
                resolveInfos = resolveInfos;
            }
            if (!isContainsActivity) {
                Slog.i(TAG, "user set preferred activity not support:" + scheme + AwarenessInnerConstants.COLON_KEY + type);
            } else {
                IntentFilter newFilter = new IntentFilter();
                newFilter.addAction("android.intent.action.VIEW");
                newFilter.addCategory("android.intent.category.DEFAULT");
                newFilter.addDataScheme(scheme);
                newFilter.addDataType(type);
                preferredIntentResolver.addFilter(new PreferredActivity(newFilter, bestMatch, sets, activity, true));
            }
            i++;
            strArr = dataTypes;
            intent = intent;
            length = length;
        }
    }

    public void rebuildPreferredActivity(int userId) {
        if (!IS_HW_PREFER_APP_POLICY) {
            Slog.i(TAG, "do nothing, prefer app policy is not hw policy");
            return;
        }
        synchronized (this.mPms.getPackagesLock()) {
            buildDocPreference(2, userId);
            buildDocPreference(8, userId);
            buildDocPreference(4, userId);
        }
    }

    private void buildDocPreference(int fileType, int userId) {
        try {
            for (PreferredActivity preferActivity : this.mSettings.editPreferredActivitiesLPw(userId).filterSet()) {
                if (preferActivity.hasAction("android.intent.action.VIEW") && preferActivity.hasCategory("android.intent.category.DEFAULT") && preferActivity.countDataTypes() != 0) {
                    if (preferActivity.mPref.mAlways) {
                        if (isContainDocType(preferActivity, fileType)) {
                            Slog.i(TAG, "build doc preference with preferred activity " + preferActivity.mPref.mShortComponent + " for user " + userId + ", and type " + fileType);
                            int len = preferActivity.mPref.mSetPackages.length;
                            ComponentName[] sets = new ComponentName[len];
                            for (int i = 0; i < len; i++) {
                                sets[i] = new ComponentName(preferActivity.mPref.mSetPackages[i], preferActivity.mPref.mSetClasses[i]);
                                Slog.i(TAG, "build doc preference sets[" + i + "]=" + sets[i]);
                            }
                            IntentFilter filter = new IntentFilter();
                            filter.addAction("android.intent.action.VIEW");
                            filter.addCategory("android.intent.category.DEFAULT");
                            Iterator<String> iter = preferActivity.typesIterator();
                            while (iter.hasNext()) {
                                filter.addDataType(iter.next());
                            }
                            resolvePreferredActivity(filter, preferActivity.mPref.mMatch, sets, preferActivity.mPref.mComponent, userId);
                            return;
                        }
                    }
                }
            }
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Slog.e(TAG, "failed to build doc preference, occured MalformedMimeTypeException");
        } catch (Exception e2) {
            Slog.e(TAG, "failed to build doc preference, occured Exception");
        }
    }

    private boolean isContainDocType(PreferredActivity preferActivity, int fileType) {
        if (preferActivity.countDataTypes() == 0) {
            return false;
        }
        String[] allTypes = getDataTypes(fileType);
        if (allTypes.length == 0) {
            return false;
        }
        Iterator<String> iter = preferActivity.typesIterator();
        while (iter.hasNext()) {
            if (ArrayUtils.contains(allTypes, iter.next())) {
                return true;
            }
        }
        return false;
    }

    public boolean removeMatchedPreferredActivity(Intent intent, PreferredIntentResolver preferredIntentResolver, PreferredActivity preferredActivity) {
        if (intent == null || preferredIntentResolver == null || preferredActivity == null || preferredActivity.mPref == null) {
            Slog.i(TAG, "param is invalid");
            return false;
        } else if (!IS_HW_PREFER_APP_POLICY || !isDocType(intent)) {
            preferredIntentResolver.removeFilter(preferredActivity);
            return true;
        } else if (preferredActivity.mPref.mAlways) {
            return false;
        } else {
            preferredIntentResolver.removeFilter(preferredActivity);
            return true;
        }
    }

    private boolean isDocType(Intent intent) {
        if (!"android.intent.action.VIEW".equals(intent.getAction())) {
            Slog.i(TAG, "the action is not view");
            return false;
        }
        String type = intent.getType();
        if (TextUtils.isEmpty(type)) {
            Slog.i(TAG, "the type is empty");
            return false;
        } else if (ArrayUtils.contains(this.mDocWordMimes, type) || ArrayUtils.contains(this.mDocPptMimes, type) || ArrayUtils.contains(this.mDocExcelMimes, type)) {
            return true;
        } else {
            return false;
        }
    }
}
