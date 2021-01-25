package com.android.server.pm;

import android.app.ActivityManager;
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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.pm.ComponentResolver;
import com.android.server.pm.InstantAppResolverConnection;
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
            sanitizedUri = Uri.fromParts(origIntent.getScheme(), "", "");
        }
        sanitizedIntent.setDataAndType(sanitizedUri, origIntent.getType());
        sanitizedIntent.addFlags(origIntent.getFlags());
        sanitizedIntent.setPackage(origIntent.getPackage());
        return sanitizedIntent;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00b7  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0105  */
    public static AuxiliaryResolveInfo doInstantAppResolutionPhaseOne(InstantAppResolverConnection connection, InstantAppRequest requestObj) {
        int i;
        int resolutionStatus;
        InstantAppResolverConnection.ConnectionException e;
        long startTime = System.currentTimeMillis();
        String token = UUID.randomUUID().toString();
        if (DEBUG_INSTANT) {
            Log.d(TAG, "[" + token + "] Phase1; resolving");
        }
        Intent origIntent = requestObj.origIntent;
        AuxiliaryResolveInfo resolveInfo = null;
        try {
            List<InstantAppResolveInfo> instantAppResolveInfoList = connection.getInstantAppResolveInfoList(sanitizeIntent(origIntent), requestObj.digest.getDigestPrefixSecure(), requestObj.userId, token);
            if (instantAppResolveInfoList == null || instantAppResolveInfoList.size() <= 0) {
                i = 2;
            } else {
                i = 2;
                try {
                    resolveInfo = filterInstantAppIntent(instantAppResolveInfoList, origIntent, requestObj.resolvedType, requestObj.userId, origIntent.getPackage(), requestObj.digest, token);
                } catch (InstantAppResolverConnection.ConnectionException e2) {
                    e = e2;
                    if (e.failure != 1) {
                        resolutionStatus = 2;
                    } else if (e.failure == i) {
                        resolutionStatus = 3;
                    } else {
                        resolutionStatus = 1;
                    }
                    logMetrics(899, startTime, token, resolutionStatus);
                    if (resolutionStatus == i) {
                    }
                    if (resolveInfo == null) {
                    }
                    return resolveInfo;
                }
            }
            resolutionStatus = 0;
        } catch (InstantAppResolverConnection.ConnectionException e3) {
            e = e3;
            i = 2;
            if (e.failure != 1) {
            }
            logMetrics(899, startTime, token, resolutionStatus);
            if (resolutionStatus == i) {
            }
            if (resolveInfo == null) {
            }
            return resolveInfo;
        }
        if (requestObj.resolveForStart && resolutionStatus == 0) {
            logMetrics(899, startTime, token, resolutionStatus);
        }
        if (DEBUG_INSTANT && resolveInfo == null) {
            if (resolutionStatus == i) {
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
        return new AuxiliaryResolveInfo(token, false, createFailureIntent(origIntent, token), (List) null);
    }

    public static void doInstantAppResolutionPhaseTwo(final Context context, InstantAppResolverConnection connection, final InstantAppRequest requestObj, final ActivityInfo instantAppInstaller, Handler callbackHandler) {
        long startTime;
        String str;
        String str2;
        String token;
        InstantAppResolverConnection.ConnectionException e;
        long startTime2 = System.currentTimeMillis();
        final String token2 = requestObj.responseObj.token;
        if (DEBUG_INSTANT) {
            Log.d(TAG, "[" + token2 + "] Phase2; resolving");
        }
        final Intent origIntent = requestObj.origIntent;
        final Intent sanitizedIntent = sanitizeIntent(origIntent);
        InstantAppResolverConnection.PhaseTwoCallback callback = new InstantAppResolverConnection.PhaseTwoCallback() {
            /* class com.android.server.pm.InstantAppResolver.AnonymousClass1 */

            /* access modifiers changed from: package-private */
            @Override // com.android.server.pm.InstantAppResolverConnection.PhaseTwoCallback
            public void onPhaseTwoResolved(List<InstantAppResolveInfo> instantAppResolveInfoList, long startTime) {
                Intent failureIntent;
                if (instantAppResolveInfoList == null || instantAppResolveInfoList.size() <= 0) {
                    failureIntent = null;
                } else {
                    Intent intent = origIntent;
                    AuxiliaryResolveInfo instantAppIntentInfo = InstantAppResolver.filterInstantAppIntent(instantAppResolveInfoList, intent, null, 0, intent.getPackage(), requestObj.digest, token2);
                    if (instantAppIntentInfo != null) {
                        failureIntent = instantAppIntentInfo.failureIntent;
                    } else {
                        failureIntent = null;
                    }
                }
                Intent installerIntent = InstantAppResolver.buildEphemeralInstallerIntent(requestObj.origIntent, sanitizedIntent, failureIntent, requestObj.callingPackage, requestObj.verificationBundle, requestObj.resolvedType, requestObj.userId, requestObj.responseObj.installFailureActivity, token2, false, requestObj.responseObj.filters);
                installerIntent.setComponent(new ComponentName(instantAppInstaller.packageName, instantAppInstaller.name));
                InstantAppResolver.logMetrics(900, startTime, token2, requestObj.responseObj.filters != null ? 0 : 1);
                context.startActivity(installerIntent);
            }
        };
        try {
            int[] digestPrefixSecure = requestObj.digest.getDigestPrefixSecure();
            int i = requestObj.userId;
            str2 = TAG;
            str = "[";
            startTime = startTime2;
            token = token2;
            try {
                connection.getInstantAppIntentFilterList(sanitizedIntent, digestPrefixSecure, i, token2, callback, callbackHandler, startTime);
            } catch (InstantAppResolverConnection.ConnectionException e2) {
                e = e2;
            }
        } catch (InstantAppResolverConnection.ConnectionException e3) {
            e = e3;
            str2 = TAG;
            str = "[";
            startTime = startTime2;
            token = token2;
            int resolutionStatus = 1;
            if (e.failure == 1) {
                resolutionStatus = 2;
            }
            logMetrics(900, startTime, token, resolutionStatus);
            if (!DEBUG_INSTANT) {
                return;
            }
            if (resolutionStatus == 2) {
                Log.d(str2, str + token + "] Phase2; bind timed out");
                return;
            }
            Log.d(str2, str + token + "] Phase2; service connection error");
        }
    }

    public static Intent buildEphemeralInstallerIntent(Intent origIntent, Intent sanitizedIntent, Intent failureIntent, String callingPackage, Bundle verificationBundle, String resolvedType, int userId, ComponentName installFailureActivity, String token, boolean needsPhaseTwo, List<AuxiliaryResolveInfo.AuxiliaryFilter> filters) {
        Intent onFailureIntent;
        int flags = origIntent.getFlags();
        Intent intent = new Intent();
        intent.setFlags(1073741824 | flags | DumpState.DUMP_VOLUMES);
        if (token != null) {
            intent.putExtra("android.intent.extra.INSTANT_APP_TOKEN", token);
        }
        if (origIntent.getData() != null) {
            intent.putExtra("android.intent.extra.INSTANT_APP_HOSTNAME", origIntent.getData().getHost());
        }
        intent.putExtra("android.intent.extra.INSTANT_APP_ACTION", origIntent.getAction());
        intent.putExtra("android.intent.extra.INTENT", sanitizedIntent);
        if (needsPhaseTwo) {
            intent.setAction("android.intent.action.RESOLVE_INSTANT_APP_PACKAGE");
        } else {
            if (!(failureIntent == null && installFailureActivity == null)) {
                if (installFailureActivity != null) {
                    try {
                        onFailureIntent = new Intent();
                        onFailureIntent.setComponent(installFailureActivity);
                        if (filters != null && filters.size() == 1) {
                            onFailureIntent.putExtra("android.intent.extra.SPLIT_NAME", filters.get(0).splitName);
                        }
                        onFailureIntent.putExtra("android.intent.extra.INTENT", origIntent);
                    } catch (RemoteException e) {
                    }
                } else {
                    onFailureIntent = failureIntent;
                }
                intent.putExtra("android.intent.extra.INSTANT_APP_FAILURE", new IntentSender(ActivityManager.getService().getIntentSender(2, callingPackage, (IBinder) null, (String) null, 1, new Intent[]{onFailureIntent}, new String[]{resolvedType}, 1409286144, (Bundle) null, userId)));
            }
            Intent successIntent = new Intent(origIntent);
            successIntent.setLaunchToken(token);
            try {
                intent.putExtra("android.intent.extra.INSTANT_APP_SUCCESS", new IntentSender(ActivityManager.getService().getIntentSender(2, callingPackage, (IBinder) null, (String) null, 0, new Intent[]{successIntent}, new String[]{resolvedType}, 1409286144, (Bundle) null, userId)));
            } catch (RemoteException e2) {
            }
            if (verificationBundle != null) {
                intent.putExtra("android.intent.extra.VERIFICATION_BUNDLE", verificationBundle);
            }
            intent.putExtra("android.intent.extra.CALLING_PACKAGE", callingPackage);
            if (filters != null) {
                Bundle[] resolvableFilters = new Bundle[filters.size()];
                int max = filters.size();
                for (int i = 0; i < max; i++) {
                    Bundle resolvableFilter = new Bundle();
                    AuxiliaryResolveInfo.AuxiliaryFilter filter = filters.get(i);
                    resolvableFilter.putBoolean("android.intent.extra.UNKNOWN_INSTANT_APP", filter.resolveInfo != null && filter.resolveInfo.shouldLetInstallerDecide());
                    resolvableFilter.putString("android.intent.extra.PACKAGE_NAME", filter.packageName);
                    resolvableFilter.putString("android.intent.extra.SPLIT_NAME", filter.splitName);
                    resolvableFilter.putLong("android.intent.extra.LONG_VERSION_CODE", filter.versionCode);
                    resolvableFilter.putBundle("android.intent.extra.INSTANT_APP_EXTRAS", filter.extras);
                    resolvableFilters[i] = resolvableFilter;
                    if (i == 0) {
                        intent.putExtras(resolvableFilter);
                        intent.putExtra("android.intent.extra.VERSION_CODE", (int) filter.versionCode);
                    }
                }
                intent.putExtra("android.intent.extra.INSTANT_APP_BUNDLES", resolvableFilters);
            }
            intent.setAction("android.intent.action.INSTALL_INSTANT_APP_PACKAGE");
        }
        return intent;
    }

    /* access modifiers changed from: private */
    public static AuxiliaryResolveInfo filterInstantAppIntent(List<InstantAppResolveInfo> instantAppResolveInfoList, Intent origIntent, String resolvedType, int userId, String packageName, InstantAppResolveInfo.InstantAppDigest digest, String token) {
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
                List<AuxiliaryResolveInfo.AuxiliaryFilter> matchFilters = computeResolveFilters(origIntent, resolvedType, userId, packageName, token, instantAppResolveInfo);
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
        if (filters == null || filters.isEmpty()) {
            return null;
        }
        return new AuxiliaryResolveInfo(token, requiresSecondPhase, createFailureIntent(origIntent, token), filters);
    }

    private static Intent createFailureIntent(Intent origIntent, String token) {
        Intent failureIntent = new Intent(origIntent);
        failureIntent.setFlags(failureIntent.getFlags() | 512);
        failureIntent.setFlags(failureIntent.getFlags() & -2049);
        failureIntent.setLaunchToken(token);
        return failureIntent;
    }

    private static List<AuxiliaryResolveInfo.AuxiliaryFilter> computeResolveFilters(Intent origIntent, String resolvedType, int userId, String packageName, String token, InstantAppResolveInfo instantAppInfo) {
        if (instantAppInfo.shouldLetInstallerDecide()) {
            return Collections.singletonList(new AuxiliaryResolveInfo.AuxiliaryFilter(instantAppInfo, (String) null, instantAppInfo.getExtras()));
        }
        if (!(packageName == null || packageName.equals(instantAppInfo.getPackageName()))) {
            return null;
        }
        List<InstantAppIntentFilter> instantAppFilters = instantAppInfo.getIntentFilters();
        if (instantAppFilters != null) {
            if (!instantAppFilters.isEmpty()) {
                ComponentResolver.InstantAppIntentResolver instantAppResolver = new ComponentResolver.InstantAppIntentResolver();
                for (int j = instantAppFilters.size() - 1; j >= 0; j--) {
                    InstantAppIntentFilter instantAppFilter = instantAppFilters.get(j);
                    List<IntentFilter> splitFilters = instantAppFilter.getFilters();
                    if (splitFilters != null && !splitFilters.isEmpty()) {
                        for (int k = splitFilters.size() - 1; k >= 0; k--) {
                            IntentFilter filter = splitFilters.get(k);
                            Iterator<IntentFilter.AuthorityEntry> authorities = filter.authoritiesIterator();
                            if ((authorities != null && authorities.hasNext()) || !((filter.hasDataScheme("http") || filter.hasDataScheme("https")) && filter.hasAction("android.intent.action.VIEW") && filter.hasCategory("android.intent.category.BROWSABLE"))) {
                                instantAppResolver.addFilter(new AuxiliaryResolveInfo.AuxiliaryFilter(filter, instantAppInfo, instantAppFilter.getSplitName(), instantAppInfo.getExtras()));
                            }
                        }
                    }
                }
                List<AuxiliaryResolveInfo.AuxiliaryFilter> matchedResolveInfoList = instantAppResolver.queryIntent(origIntent, resolvedType, false, userId);
                if (!matchedResolveInfoList.isEmpty()) {
                    if (DEBUG_INSTANT) {
                        Log.d(TAG, "[" + token + "] Found match(es); " + matchedResolveInfoList);
                    }
                    return matchedResolveInfoList;
                }
                if (DEBUG_INSTANT) {
                    Log.d(TAG, "[" + token + "] No matches found package: " + instantAppInfo.getPackageName() + ", versionCode: " + instantAppInfo.getVersionCode());
                }
                return null;
            }
        }
        if (origIntent.isWebIntent()) {
            return null;
        }
        if (DEBUG_INSTANT) {
            Log.d(TAG, "No app filters; go to phase 2");
        }
        return Collections.emptyList();
    }

    /* access modifiers changed from: private */
    public static void logMetrics(int action, long startTime, String token, int status) {
        getLogger().write(new LogMaker(action).setType(4).addTaggedData(901, new Long(System.currentTimeMillis() - startTime)).addTaggedData(903, token).addTaggedData(902, new Integer(status)));
    }
}
