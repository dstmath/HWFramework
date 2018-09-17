package com.android.server.pm;

import android.annotation.IntDef;
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
import android.content.pm.InstantAppResolveInfo.InstantAppDigest;
import android.metrics.LogMaker;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.server.pm.EphemeralResolverConnection.ConnectionException;
import com.android.server.pm.EphemeralResolverConnection.PhaseTwoCallback;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class InstantAppResolver {
    private static final boolean DEBUG_EPHEMERAL = Build.IS_DEBUGGABLE;
    private static final int RESOLUTION_BIND_TIMEOUT = 2;
    private static final int RESOLUTION_CALL_TIMEOUT = 3;
    private static final int RESOLUTION_FAILURE = 1;
    private static final int RESOLUTION_SUCCESS = 0;
    private static final String TAG = "PackageManager";
    private static MetricsLogger sMetricsLogger;

    @IntDef(flag = true, prefix = {"RESOLUTION_"}, value = {0, 1, 2, 3})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ResolutionStatus {
    }

    private static MetricsLogger getLogger() {
        if (sMetricsLogger == null) {
            sMetricsLogger = new MetricsLogger();
        }
        return sMetricsLogger;
    }

    public static AuxiliaryResolveInfo doInstantAppResolutionPhaseOne(Context context, EphemeralResolverConnection connection, InstantAppRequest requestObj) {
        long startTime = System.currentTimeMillis();
        String token = UUID.randomUUID().toString();
        if (DEBUG_EPHEMERAL) {
            Log.d(TAG, "[" + token + "] Phase1; resolving");
        }
        Intent intent = requestObj.origIntent;
        InstantAppDigest digest = new InstantAppDigest(intent.getData().getHost(), 5);
        AuxiliaryResolveInfo resolveInfo = null;
        int resolutionStatus = 0;
        try {
            List<InstantAppResolveInfo> instantAppResolveInfoList = connection.getInstantAppResolveInfoList(digest.getDigestPrefix(), token);
            if (instantAppResolveInfoList != null && instantAppResolveInfoList.size() > 0) {
                resolveInfo = filterInstantAppIntent(instantAppResolveInfoList, intent, requestObj.resolvedType, requestObj.userId, intent.getPackage(), digest, token);
            }
        } catch (ConnectionException e) {
            resolutionStatus = e.failure == 1 ? 2 : e.failure == 2 ? 3 : 1;
        }
        if (resolutionStatus == 0) {
            logMetrics(899, startTime, token, resolutionStatus);
        }
        if (DEBUG_EPHEMERAL && resolveInfo == null) {
            if (resolutionStatus == 2) {
                Log.d(TAG, "[" + token + "] Phase1; bind timed out");
            } else if (resolutionStatus == 3) {
                Log.d(TAG, "[" + token + "] Phase1; call timed out");
            } else if (resolutionStatus != 0) {
                Log.d(TAG, "[" + token + "] Phase1; service connection error");
            } else {
                Log.d(TAG, "[" + token + "] Phase1; No results matched");
            }
        }
        return resolveInfo;
    }

    public static void doInstantAppResolutionPhaseTwo(Context context, EphemeralResolverConnection connection, InstantAppRequest requestObj, ActivityInfo instantAppInstaller, Handler callbackHandler) {
        long startTime = System.currentTimeMillis();
        final String token = requestObj.responseObj.token;
        if (DEBUG_EPHEMERAL) {
            Log.d(TAG, "[" + token + "] Phase2; resolving");
        }
        final Intent intent = requestObj.origIntent;
        String hostName = intent.getData().getHost();
        final InstantAppDigest digest = new InstantAppDigest(hostName, 5);
        final InstantAppRequest instantAppRequest = requestObj;
        final ActivityInfo activityInfo = instantAppInstaller;
        final Context context2 = context;
        try {
            connection.getInstantAppIntentFilterList(digest.getDigestPrefix(), token, hostName, new PhaseTwoCallback() {
                void onPhaseTwoResolved(List<InstantAppResolveInfo> instantAppResolveInfoList, long startTime) {
                    String packageName;
                    String splitName;
                    int versionCode;
                    Intent failureIntent;
                    if (instantAppResolveInfoList == null || instantAppResolveInfoList.size() <= 0) {
                        packageName = null;
                        splitName = null;
                        versionCode = -1;
                        failureIntent = null;
                    } else {
                        AuxiliaryResolveInfo instantAppIntentInfo = InstantAppResolver.filterInstantAppIntent(instantAppResolveInfoList, intent, null, 0, intent.getPackage(), digest, token);
                        if (instantAppIntentInfo == null || instantAppIntentInfo.resolveInfo == null) {
                            packageName = null;
                            splitName = null;
                            versionCode = -1;
                            failureIntent = null;
                        } else {
                            packageName = instantAppIntentInfo.resolveInfo.getPackageName();
                            splitName = instantAppIntentInfo.splitName;
                            versionCode = instantAppIntentInfo.resolveInfo.getVersionCode();
                            failureIntent = instantAppIntentInfo.failureIntent;
                        }
                    }
                    Intent installerIntent = InstantAppResolver.buildEphemeralInstallerIntent("android.intent.action.RESOLVE_INSTANT_APP_PACKAGE", instantAppRequest.origIntent, failureIntent, instantAppRequest.callingPackage, instantAppRequest.verificationBundle, instantAppRequest.resolvedType, instantAppRequest.userId, packageName, splitName, versionCode, token, false);
                    installerIntent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
                    InstantAppResolver.logMetrics(900, startTime, token, packageName != null ? 0 : 1);
                    context2.startActivity(installerIntent);
                }
            }, callbackHandler, startTime);
        } catch (ConnectionException e) {
            int resolutionStatus = 1;
            if (e.failure == 1) {
                resolutionStatus = 2;
            }
            logMetrics(900, startTime, token, resolutionStatus);
            if (!DEBUG_EPHEMERAL) {
                return;
            }
            if (resolutionStatus == 2) {
                Log.d(TAG, "[" + token + "] Phase2; bind timed out");
            } else {
                Log.d(TAG, "[" + token + "] Phase2; service connection error");
            }
        }
    }

    public static Intent buildEphemeralInstallerIntent(String action, Intent origIntent, Intent failureIntent, String callingPackage, Bundle verificationBundle, String resolvedType, int userId, String instantAppPackageName, String instantAppSplitName, int versionCode, String token, boolean needsPhaseTwo) {
        int flags = origIntent.getFlags();
        Intent intent = new Intent(action);
        intent.setFlags((((268435456 | flags) | 32768) | 1073741824) | 8388608);
        if (token != null) {
            intent.putExtra("android.intent.extra.EPHEMERAL_TOKEN", token);
        }
        if (origIntent.getData() != null) {
            intent.putExtra("android.intent.extra.EPHEMERAL_HOSTNAME", origIntent.getData().getHost());
        }
        if (!needsPhaseTwo) {
            if (failureIntent != null) {
                try {
                    intent = intent;
                    intent.putExtra("android.intent.extra.EPHEMERAL_FAILURE", new IntentSender(ActivityManager.getService().getIntentSender(2, callingPackage, null, null, 1, new Intent[]{failureIntent}, new String[]{resolvedType}, 1409286144, null, userId)));
                } catch (RemoteException e) {
                }
            }
            new Intent(origIntent).setLaunchToken(token);
            try {
                intent = intent;
                intent.putExtra("android.intent.extra.EPHEMERAL_SUCCESS", new IntentSender(ActivityManager.getService().getIntentSender(2, callingPackage, null, null, 0, new Intent[]{intent}, new String[]{resolvedType}, 1409286144, null, userId)));
            } catch (RemoteException e2) {
            }
            intent.putExtra("android.intent.extra.PACKAGE_NAME", instantAppPackageName);
            intent.putExtra("android.intent.extra.SPLIT_NAME", instantAppSplitName);
            intent.putExtra("android.intent.extra.VERSION_CODE", versionCode);
            intent.putExtra("android.intent.extra.CALLING_PACKAGE", callingPackage);
            if (verificationBundle != null) {
                intent.putExtra("android.intent.extra.VERIFICATION_BUNDLE", verificationBundle);
            }
        }
        return intent;
    }

    private static AuxiliaryResolveInfo filterInstantAppIntent(List<InstantAppResolveInfo> instantAppResolveInfoList, Intent origIntent, String resolvedType, int userId, String packageName, InstantAppDigest digest, String token) {
        int[] shaPrefix = digest.getDigestPrefix();
        byte[][] digestBytes = digest.getDigestBytes();
        Intent failureIntent = new Intent(origIntent);
        failureIntent.setFlags(failureIntent.getFlags() | 512);
        failureIntent.setLaunchToken(token);
        for (int i = shaPrefix.length - 1; i >= 0; i--) {
            for (InstantAppResolveInfo instantAppInfo : instantAppResolveInfoList) {
                if (Arrays.equals(digestBytes[i], instantAppInfo.getDigestBytes())) {
                    if (packageName != null) {
                        if ((packageName.equals(instantAppInfo.getPackageName()) ^ 1) != 0) {
                            continue;
                        }
                    }
                    List<InstantAppIntentFilter> instantAppFilters = instantAppInfo.getIntentFilters();
                    if (instantAppFilters == null || instantAppFilters.isEmpty()) {
                        if (DEBUG_EPHEMERAL) {
                            Log.d(TAG, "No app filters; go to phase 2");
                        }
                        return new AuxiliaryResolveInfo(instantAppInfo, new IntentFilter("android.intent.action.VIEW"), null, token, true, null);
                    }
                    EphemeralIntentResolver instantAppResolver = new EphemeralIntentResolver();
                    for (int j = instantAppFilters.size() - 1; j >= 0; j--) {
                        InstantAppIntentFilter instantAppFilter = (InstantAppIntentFilter) instantAppFilters.get(j);
                        List<IntentFilter> splitFilters = instantAppFilter.getFilters();
                        if (!(splitFilters == null || splitFilters.isEmpty())) {
                            for (int k = splitFilters.size() - 1; k >= 0; k--) {
                                instantAppResolver.addFilter(new AuxiliaryResolveInfo(instantAppInfo, (IntentFilter) splitFilters.get(k), instantAppFilter.getSplitName(), token, false, failureIntent));
                            }
                        }
                    }
                    List<AuxiliaryResolveInfo> matchedResolveInfoList = instantAppResolver.queryIntent(origIntent, resolvedType, false, userId);
                    if (!matchedResolveInfoList.isEmpty()) {
                        if (DEBUG_EPHEMERAL) {
                            AuxiliaryResolveInfo info = (AuxiliaryResolveInfo) matchedResolveInfoList.get(0);
                            Log.d(TAG, "[" + token + "] Found match;" + " package: " + info.packageName + ", split: " + info.splitName + ", versionCode: " + info.versionCode);
                        }
                        return (AuxiliaryResolveInfo) matchedResolveInfoList.get(0);
                    } else if (DEBUG_EPHEMERAL) {
                        Log.d(TAG, "[" + token + "] No matches found" + " package: " + instantAppInfo.getPackageName() + ", versionCode: " + instantAppInfo.getVersionCode());
                    }
                }
            }
        }
        return null;
    }

    private static void logMetrics(int action, long startTime, String token, int status) {
        getLogger().write(new LogMaker(action).setType(4).addTaggedData(NetdResponseCode.ApLinkedStaListChangeQCOM, new Long(System.currentTimeMillis() - startTime)).addTaggedData(903, token).addTaggedData(902, new Integer(status)));
    }
}
