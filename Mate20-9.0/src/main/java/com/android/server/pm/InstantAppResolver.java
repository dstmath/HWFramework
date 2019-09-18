package com.android.server.pm;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.AuxiliaryResolveInfo;
import android.content.pm.InstantAppIntentFilter;
import android.content.pm.InstantAppRequest;
import android.content.pm.InstantAppResolveInfo;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.NetworkManagementService;
import com.android.server.pm.InstantAppResolverConnection;
import com.android.server.pm.PackageManagerService;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class InstantAppResolver {
    private static final boolean DEBUG_INSTANT = Build.IS_DEBUGGABLE;
    private static final int RESOLUTION_BIND_TIMEOUT = 2;
    private static final int RESOLUTION_CALL_TIMEOUT = 3;
    private static final int RESOLUTION_FAILURE = 1;
    private static final int RESOLUTION_SUCCESS = 0;
    private static final String TAG = "PackageManager";
    private static MetricsLogger sMetricsLogger;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResolutionStatus {
    }

    private static MetricsLogger getLogger() {
        if (sMetricsLogger == null) {
            sMetricsLogger = new MetricsLogger();
        }
        return sMetricsLogger;
    }

    public static Intent sanitizeIntent(Intent origIntent) {
        Uri sanitizedUri;
        Intent sanitizedIntent = new Intent(origIntent.getAction());
        Set<String> categories = origIntent.getCategories();
        if (categories != null) {
            for (String category : categories) {
                sanitizedIntent.addCategory(category);
            }
        }
        if (origIntent.getData() == null) {
            sanitizedUri = null;
        } else {
            sanitizedUri = Uri.fromParts(origIntent.getScheme(), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        }
        sanitizedIntent.setDataAndType(sanitizedUri, origIntent.getType());
        sanitizedIntent.addFlags(origIntent.getFlags());
        sanitizedIntent.setPackage(origIntent.getPackage());
        return sanitizedIntent;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0108  */
    public static AuxiliaryResolveInfo doInstantAppResolutionPhaseOne(InstantAppResolverConnection connection, InstantAppRequest requestObj) {
        int i;
        int resolutionStatus;
        InstantAppRequest instantAppRequest = requestObj;
        long startTime = System.currentTimeMillis();
        String token = UUID.randomUUID().toString();
        if (DEBUG_INSTANT) {
            Log.d(TAG, "[" + token + "] Phase1; resolving");
        }
        Intent origIntent = instantAppRequest.origIntent;
        AuxiliaryResolveInfo resolveInfo = null;
        int resolutionStatus2 = 0;
        try {
            List<InstantAppResolveInfo> instantAppResolveInfoList = connection.getInstantAppResolveInfoList(sanitizeIntent(origIntent), instantAppRequest.digest.getDigestPrefixSecure(), token);
            if (instantAppResolveInfoList == null || instantAppResolveInfoList.size() <= 0) {
                i = 2;
                resolutionStatus = resolutionStatus2;
                if (instantAppRequest.resolveForStart && resolutionStatus == 0) {
                    logMetrics(899, startTime, token, resolutionStatus);
                }
                if (DEBUG_INSTANT && resolveInfo == null) {
                    if (resolutionStatus != i) {
                        Log.d(TAG, "[" + token + "] Phase1; bind timed out");
                    } else if (resolutionStatus == 3) {
                        Log.d(TAG, "[" + token + "] Phase1; call timed out");
                    } else if (resolutionStatus != 0) {
                        Log.d(TAG, "[" + token + "] Phase1; service connection error");
                    } else {
                        Log.d(TAG, "[" + token + "] Phase1; No results matched");
                    }
                }
                if (resolveInfo == null || (origIntent.getFlags() & 2048) == 0) {
                    return resolveInfo;
                }
                return new AuxiliaryResolveInfo(token, false, createFailureIntent(origIntent, token), null);
            }
            i = 2;
            try {
                resolveInfo = filterInstantAppIntent(instantAppResolveInfoList, origIntent, instantAppRequest.resolvedType, instantAppRequest.userId, origIntent.getPackage(), instantAppRequest.digest, token);
            } catch (InstantAppResolverConnection.ConnectionException e) {
                resolveInfo = e;
                resolutionStatus2 = resolveInfo.failure != 1 ? 2 : resolveInfo.failure == i ? 3 : 1;
                resolutionStatus = resolutionStatus2;
                logMetrics(899, startTime, token, resolutionStatus);
                if (resolutionStatus != i) {
                }
                if (resolveInfo == null) {
                }
                return resolveInfo;
            }
            resolutionStatus = resolutionStatus2;
            logMetrics(899, startTime, token, resolutionStatus);
            if (resolutionStatus != i) {
            }
            if (resolveInfo == null) {
            }
            return resolveInfo;
        } catch (InstantAppResolverConnection.ConnectionException e2) {
            resolveInfo = e2;
            i = 2;
            if (resolveInfo.failure != 1) {
            }
            resolutionStatus = resolutionStatus2;
            logMetrics(899, startTime, token, resolutionStatus);
            if (resolutionStatus != i) {
            }
            if (resolveInfo == null) {
            }
            return resolveInfo;
        }
    }

    public static void doInstantAppResolutionPhaseTwo(Context context, InstantAppResolverConnection connection, InstantAppRequest requestObj, ActivityInfo instantAppInstaller, Handler callbackHandler) {
        long startTime;
        String token;
        InstantAppRequest instantAppRequest = requestObj;
        long startTime2 = System.currentTimeMillis();
        String token2 = instantAppRequest.responseObj.token;
        if (DEBUG_INSTANT) {
            Log.d(TAG, "[" + token2 + "] Phase2; resolving");
        }
        Intent origIntent = instantAppRequest.origIntent;
        Intent sanitizedIntent = sanitizeIntent(origIntent);
        final Intent intent = origIntent;
        final InstantAppRequest instantAppRequest2 = instantAppRequest;
        final String str = token2;
        final Intent intent2 = sanitizedIntent;
        final ActivityInfo activityInfo = instantAppInstaller;
        final Context context2 = context;
        AnonymousClass1 r1 = new InstantAppResolverConnection.PhaseTwoCallback() {
            /* access modifiers changed from: package-private */
            public void onPhaseTwoResolved(List<InstantAppResolveInfo> instantAppResolveInfoList, long startTime) {
                Intent failureIntent = null;
                if (instantAppResolveInfoList != null && instantAppResolveInfoList.size() > 0) {
                    AuxiliaryResolveInfo instantAppIntentInfo = InstantAppResolver.filterInstantAppIntent(instantAppResolveInfoList, intent, null, 0, intent.getPackage(), instantAppRequest2.digest, str);
                    if (instantAppIntentInfo != null) {
                        failureIntent = instantAppIntentInfo.failureIntent;
                    }
                }
                Intent installerIntent = InstantAppResolver.buildEphemeralInstallerIntent(instantAppRequest2.origIntent, intent2, failureIntent, instantAppRequest2.callingPackage, instantAppRequest2.verificationBundle, instantAppRequest2.resolvedType, instantAppRequest2.userId, instantAppRequest2.responseObj.installFailureActivity, str, false, instantAppRequest2.responseObj.filters);
                installerIntent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
                InstantAppResolver.logMetrics(900, startTime, str, instantAppRequest2.responseObj.filters != null ? 0 : 1);
                context2.startActivity(installerIntent);
            }
        };
        try {
            Intent intent3 = origIntent;
            token = token2;
            startTime = startTime2;
            try {
                connection.getInstantAppIntentFilterList(sanitizedIntent, instantAppRequest.digest.getDigestPrefixSecure(), token2, r1, callbackHandler, startTime);
            } catch (InstantAppResolverConnection.ConnectionException e) {
                e = e;
            }
        } catch (InstantAppResolverConnection.ConnectionException e2) {
            e = e2;
            Intent intent4 = origIntent;
            token = token2;
            startTime = startTime2;
            int resolutionStatus = 1;
            if (e.failure == 1) {
                resolutionStatus = 2;
            }
            logMetrics(900, startTime, token, resolutionStatus);
            if (!DEBUG_INSTANT) {
                return;
            }
            if (resolutionStatus == 2) {
                Log.d(TAG, "[" + token + "] Phase2; bind timed out");
                return;
            }
            Log.d(TAG, "[" + token + "] Phase2; service connection error");
        }
    }

    public static Intent buildEphemeralInstallerIntent(Intent origIntent, Intent sanitizedIntent, Intent failureIntent, String callingPackage, Bundle verificationBundle, String resolvedType, int userId, ComponentName installFailureActivity, String token, boolean needsPhaseTwo, List<AuxiliaryResolveInfo.AuxiliaryFilter> filters) {
        boolean z;
        Intent onFailureIntent;
        Intent intent = origIntent;
        Bundle bundle = verificationBundle;
        ComponentName componentName = installFailureActivity;
        String str = token;
        List<AuxiliaryResolveInfo.AuxiliaryFilter> list = filters;
        int flags = origIntent.getFlags();
        Intent intent2 = new Intent();
        intent2.setFlags(1073741824 | flags | DumpState.DUMP_VOLUMES);
        if (str != null) {
            intent2.putExtra("android.intent.extra.EPHEMERAL_TOKEN", str);
            intent2.putExtra("android.intent.extra.INSTANT_APP_TOKEN", str);
        }
        if (origIntent.getData() != null) {
            intent2.putExtra("android.intent.extra.EPHEMERAL_HOSTNAME", origIntent.getData().getHost());
            intent2.putExtra("android.intent.extra.INSTANT_APP_HOSTNAME", origIntent.getData().getHost());
        }
        intent2.putExtra("android.intent.extra.INSTANT_APP_ACTION", origIntent.getAction());
        intent2.putExtra("android.intent.extra.INTENT", sanitizedIntent);
        if (needsPhaseTwo) {
            intent2.setAction("android.intent.action.RESOLVE_INSTANT_APP_PACKAGE");
            String str2 = callingPackage;
        } else {
            if (!(failureIntent == null && componentName == null)) {
                if (componentName != null) {
                    try {
                        onFailureIntent = new Intent();
                        onFailureIntent.setComponent(componentName);
                        if (list != null && filters.size() == 1) {
                            onFailureIntent.putExtra("android.intent.extra.SPLIT_NAME", list.get(0).splitName);
                        }
                        onFailureIntent.putExtra("android.intent.extra.INTENT", intent);
                    } catch (RemoteException e) {
                    }
                } else {
                    onFailureIntent = failureIntent;
                }
                String str3 = callingPackage;
                IntentSender failureSender = new IntentSender(ActivityManager.getService().getIntentSender(2, str3, null, null, 1, new Intent[]{onFailureIntent}, new String[]{resolvedType}, 1409286144, null, userId));
                intent2.putExtra("android.intent.extra.EPHEMERAL_FAILURE", failureSender);
                intent2.putExtra("android.intent.extra.INSTANT_APP_FAILURE", failureSender);
            }
            Intent successIntent = new Intent(intent);
            successIntent.setLaunchToken(str);
            try {
                IActivityManager service = ActivityManager.getService();
                Intent[] intentArr = new Intent[1];
                z = false;
                try {
                    intentArr[0] = successIntent;
                    IntentSender successSender = new IntentSender(service.getIntentSender(2, callingPackage, null, null, 0, intentArr, new String[]{resolvedType}, 1409286144, null, userId));
                    intent2.putExtra("android.intent.extra.EPHEMERAL_SUCCESS", successSender);
                    intent2.putExtra("android.intent.extra.INSTANT_APP_SUCCESS", successSender);
                } catch (RemoteException e2) {
                }
            } catch (RemoteException e3) {
                z = false;
            }
            if (bundle != null) {
                intent2.putExtra("android.intent.extra.VERIFICATION_BUNDLE", bundle);
            }
            intent2.putExtra("android.intent.extra.CALLING_PACKAGE", callingPackage);
            if (list != null) {
                Bundle[] resolvableFilters = new Bundle[filters.size()];
                int i = 0;
                int max = filters.size();
                while (true) {
                    int max2 = max;
                    if (i >= max2) {
                        break;
                    }
                    Bundle resolvableFilter = new Bundle();
                    AuxiliaryResolveInfo.AuxiliaryFilter filter = list.get(i);
                    resolvableFilter.putBoolean("android.intent.extra.UNKNOWN_INSTANT_APP", (filter.resolveInfo == null || !filter.resolveInfo.shouldLetInstallerDecide()) ? z : true);
                    resolvableFilter.putString("android.intent.extra.PACKAGE_NAME", filter.packageName);
                    resolvableFilter.putString("android.intent.extra.SPLIT_NAME", filter.splitName);
                    resolvableFilter.putLong("android.intent.extra.LONG_VERSION_CODE", filter.versionCode);
                    resolvableFilter.putBundle("android.intent.extra.INSTANT_APP_EXTRAS", filter.extras);
                    resolvableFilters[i] = resolvableFilter;
                    if (i == 0) {
                        intent2.putExtras(resolvableFilter);
                        intent2.putExtra("android.intent.extra.VERSION_CODE", (int) filter.versionCode);
                    }
                    i++;
                    max = max2;
                    Intent intent3 = origIntent;
                    Bundle bundle2 = verificationBundle;
                    ComponentName componentName2 = installFailureActivity;
                    String str4 = token;
                }
                intent2.putExtra("android.intent.extra.INSTANT_APP_BUNDLES", resolvableFilters);
            }
            intent2.setAction("android.intent.action.INSTALL_INSTANT_APP_PACKAGE");
        }
        return intent2;
    }

    /* access modifiers changed from: private */
    public static AuxiliaryResolveInfo filterInstantAppIntent(List<InstantAppResolveInfo> instantAppResolveInfoList, Intent origIntent, String resolvedType, int userId, String packageName, InstantAppResolveInfo.InstantAppDigest digest, String token) {
        String str = token;
        int[] shaPrefix = digest.getDigestPrefix();
        byte[][] digestBytes = digest.getDigestBytes();
        boolean requiresPrefixMatch = origIntent.isWebIntent() || (shaPrefix.length > 0 && (origIntent.getFlags() & 2048) == 0);
        boolean requiresSecondPhase = false;
        ArrayList<AuxiliaryResolveInfo.AuxiliaryFilter> filters = null;
        for (InstantAppResolveInfo instantAppResolveInfo : instantAppResolveInfoList) {
            if (!requiresPrefixMatch || !instantAppResolveInfo.shouldLetInstallerDecide()) {
                byte[] filterDigestBytes = instantAppResolveInfo.getDigestBytes();
                if (shaPrefix.length > 0 && (requiresPrefixMatch || filterDigestBytes.length > 0)) {
                    boolean matchFound = false;
                    int i = shaPrefix.length - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        } else if (Arrays.equals(digestBytes[i], filterDigestBytes)) {
                            matchFound = true;
                            break;
                        } else {
                            i--;
                        }
                    }
                    if (!matchFound) {
                    }
                }
                List<AuxiliaryResolveInfo.AuxiliaryFilter> matchFilters = computeResolveFilters(origIntent, resolvedType, userId, packageName, str, instantAppResolveInfo);
                if (matchFilters != null) {
                    if (matchFilters.isEmpty()) {
                        requiresSecondPhase = true;
                    }
                    if (filters == null) {
                        filters = new ArrayList<>(matchFilters);
                    } else {
                        filters.addAll(matchFilters);
                    }
                }
            } else {
                Slog.d(TAG, "InstantAppResolveInfo with mShouldLetInstallerDecide=true when digest required; ignoring");
            }
        }
        if (filters != null && !filters.isEmpty()) {
            return new AuxiliaryResolveInfo(str, requiresSecondPhase, createFailureIntent(origIntent, str), filters);
        }
        Intent intent = origIntent;
        return null;
    }

    private static Intent createFailureIntent(Intent origIntent, String token) {
        Intent failureIntent = new Intent(origIntent);
        failureIntent.setFlags(failureIntent.getFlags() | 512);
        failureIntent.setFlags(failureIntent.getFlags() & -2049);
        failureIntent.setLaunchToken(token);
        return failureIntent;
    }

    private static List<AuxiliaryResolveInfo.AuxiliaryFilter> computeResolveFilters(Intent origIntent, String resolvedType, int userId, String packageName, String token, InstantAppResolveInfo instantAppInfo) {
        String str = packageName;
        String str2 = token;
        InstantAppResolveInfo instantAppResolveInfo = instantAppInfo;
        if (instantAppInfo.shouldLetInstallerDecide()) {
            return Collections.singletonList(new AuxiliaryResolveInfo.AuxiliaryFilter(instantAppResolveInfo, null, instantAppInfo.getExtras()));
        }
        if (str != null && !str.equals(instantAppInfo.getPackageName())) {
            return null;
        }
        List<InstantAppIntentFilter> instantAppFilters = instantAppInfo.getIntentFilters();
        if (instantAppFilters == null || instantAppFilters.isEmpty()) {
            String str3 = resolvedType;
            int i = userId;
            if (origIntent.isWebIntent()) {
                return null;
            }
            if (DEBUG_INSTANT) {
                Log.d(TAG, "No app filters; go to phase 2");
            }
            return Collections.emptyList();
        }
        PackageManagerService.InstantAppIntentResolver instantAppResolver = new PackageManagerService.InstantAppIntentResolver();
        for (int j = instantAppFilters.size() - 1; j >= 0; j--) {
            InstantAppIntentFilter instantAppFilter = instantAppFilters.get(j);
            List<IntentFilter> splitFilters = instantAppFilter.getFilters();
            if (splitFilters != null && !splitFilters.isEmpty()) {
                for (int k = splitFilters.size() - 1; k >= 0; k--) {
                    IntentFilter filter = splitFilters.get(k);
                    Iterator<IntentFilter.AuthorityEntry> authorities = filter.authoritiesIterator();
                    if ((authorities != null && authorities.hasNext()) || ((!filter.hasDataScheme("http") && !filter.hasDataScheme("https")) || !filter.hasAction("android.intent.action.VIEW") || !filter.hasCategory("android.intent.category.BROWSABLE"))) {
                        instantAppResolver.addFilter(new AuxiliaryResolveInfo.AuxiliaryFilter(filter, instantAppResolveInfo, instantAppFilter.getSplitName(), instantAppInfo.getExtras()));
                    }
                }
            }
        }
        List<AuxiliaryResolveInfo.AuxiliaryFilter> matchedResolveInfoList = instantAppResolver.queryIntent(origIntent, resolvedType, false, userId);
        if (!matchedResolveInfoList.isEmpty()) {
            if (DEBUG_INSTANT) {
                Log.d(TAG, "[" + str2 + "] Found match(es); " + matchedResolveInfoList);
            }
            return matchedResolveInfoList;
        }
        if (DEBUG_INSTANT) {
            Log.d(TAG, "[" + str2 + "] No matches found package: " + instantAppInfo.getPackageName() + ", versionCode: " + instantAppInfo.getVersionCode());
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static void logMetrics(int action, long startTime, String token, int status) {
        getLogger().write(new LogMaker(action).setType(4).addTaggedData(NetworkManagementService.NetdResponseCode.ApLinkedStaListChangeQCOM, new Long(System.currentTimeMillis() - startTime)).addTaggedData(903, token).addTaggedData(902, new Integer(status)));
    }
}
