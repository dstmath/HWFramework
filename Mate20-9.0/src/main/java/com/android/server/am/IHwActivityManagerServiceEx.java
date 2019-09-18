package com.android.server.am;

import android.app.ActivityOptions;
import android.app.IHwActivityNotifier;
import android.app.usage.UsageStatsManagerInternal;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.util.TimingsTraceLog;
import android.view.WindowManagerPolicyConstants;
import com.android.server.mtm.taskstatus.ProcessInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IHwActivityManagerServiceEx {
    void call(Bundle bundle);

    int canAppBoost(ActivityInfo activityInfo, boolean z);

    boolean canCleanTaskRecord(String str);

    boolean canPickColor(String str);

    boolean canSleepForPCMode();

    boolean canUpdateSleepForPCMode();

    int changeGidIfRepairMode(int i, String str);

    int[] changeGidsIfNeeded(ProcessRecord processRecord, int[] iArr);

    Intent changeStartActivityIfNeed(Intent intent);

    boolean checkActivityStartForPCMode(ActivityStarter activityStarter, ActivityOptions activityOptions, ActivityRecord activityRecord, ActivityStack activityStack);

    void checkAndPrintTestModeLog(List list, String str, String str2, String str3);

    void cleanAppForHiddenSpace();

    boolean cleanPackageRes(List<String> list, Map<String, List<String>> map, int i, boolean z, boolean z2, boolean z3);

    boolean cleanProcessResourceFast(String str, int i, IBinder iBinder, boolean z, boolean z2);

    boolean customActivityResuming(String str);

    void dismissSplitScreenModeWithFinish(ActivityRecord activityRecord);

    void dismissSplitScreenToFocusedStack();

    void dispatchActivityLifeState(ActivityRecord activityRecord, String str);

    boolean enterCoordinationMode(Intent intent);

    boolean exitCoordinationModeInner(boolean z, boolean z2);

    void forceGCAfterRebooting();

    int getActivityWindowMode(IBinder iBinder);

    int getCaptionState(IBinder iBinder);

    int getCloneAppUserId(String str, int i);

    int getContentProviderUserId(String str, int i);

    int getEffectiveUid(int i, int i2);

    TaskChangeNotificationController getHwTaskChangeController();

    Rect getPCTopTaskBounds(int i);

    List<String> getPidWithUiFromUid(int i);

    HashMap<String, Integer> getPkgDisplayMaps();

    WindowManagerPolicyConstants.PointerEventListener getPointerEventListener();

    boolean getProcessRecordFromMTM(ProcessInfo processInfo);

    String getTargetFromIntentForClone(Intent intent);

    int getTopTaskIdInDisplay(int i, String str, boolean z);

    int getUidByUriAuthority(Uri uri, int i);

    boolean handleANRFilterFIFO(int i, int i2);

    boolean isAllowToStartActivity(Context context, String str, ActivityInfo activityInfo, boolean z, ActivityInfo activityInfo2);

    boolean isApplyPersistAppPatch(String str, int i, int i2, boolean z, boolean z2, String str2, String str3);

    boolean isExemptedAuthority(Uri uri);

    boolean isFreeFormVisible();

    boolean isHiddenSpaceSwitch(UserInfo userInfo, UserInfo userInfo2);

    boolean isInMultiWindowMode();

    boolean isLimitedPackageBroadcast(Intent intent);

    boolean isProcessExistPidsSelfLocked(String str, int i);

    boolean isSpecialVideoForPCMode(ActivityRecord activityRecord);

    boolean isTaskSupportResize(int i, boolean z, boolean z2);

    boolean isTaskVisible(int i);

    void killApplication(String str, int i, int i2, String str2);

    boolean killNativeProcessRecordFast(String str, int i, int i2, boolean z, boolean z2, String str2);

    boolean killProcessRecordFast(String str, int i, int i2, boolean z, boolean z2, String str2, boolean z3);

    boolean killProcessRecordFromIAwareInternal(ProcessInfo processInfo, boolean z, boolean z2, String str, boolean z3, boolean z4);

    boolean killProcessRecordFromMTM(ProcessInfo processInfo, boolean z, String str);

    boolean needCheckProcDied(ProcessRecord processRecord);

    void noteActivityStart(String str, String str2, String str3, int i, int i2, boolean z);

    void notifyActivityState(ActivityRecord activityRecord, String str);

    void notifyAppSwitch(ActivityRecord activityRecord, ActivityRecord activityRecord2);

    void onAppGroupChanged(int i, int i2, String str, int i3, int i4);

    void onMultiWindowModeChanged(boolean z);

    int preloadApplication(String str, int i);

    void registerBroadcastReceiver();

    void registerHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier, String str);

    boolean registerThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback);

    void removePackageAlarm(String str, List<String> list, int i);

    void removePackageStopFlag(String str, int i, String str2, int i2, String str3, Bundle bundle, int i3);

    void reportAppDiedMsg(int i, String str, int i2, String str2);

    void reportAssocDisable();

    void reportHomeProcess(ProcessRecord processRecord);

    void reportPreviousInfo(int i, ProcessRecord processRecord);

    void reportProcessDied(int i);

    void reportServiceRelationIAware(int i, ContentProviderRecord contentProviderRecord, ProcessRecord processRecord);

    void reportServiceRelationIAware(int i, ServiceRecord serviceRecord, ProcessRecord processRecord);

    void resumeCoordinationPrimaryStack(ActivityRecord activityRecord);

    void setAndRestoreMaxAdjIfNeed(List<String> list);

    void setHbsMiniAppUid(ApplicationInfo applicationInfo, Intent intent);

    void setThreadSchedPolicy(int i, ProcessRecord processRecord);

    boolean shouldPreventSendBroadcast(Intent intent, String str, int i, int i2, String str2, int i3);

    boolean shouldPreventStartActivity(ActivityInfo activityInfo, int i, int i2, String str, int i3);

    boolean shouldPreventStartProcess(String str, int i);

    boolean shouldPreventStartProvider(ProviderInfo providerInfo, int i, int i2, String str, int i3);

    boolean shouldPreventStartService(ServiceInfo serviceInfo, int i, int i2, String str, int i3);

    boolean shouldResumeCoordinationPrimaryStack();

    boolean shouldSkipSendIntentSender(IIntentSender iIntentSender, Bundle bundle);

    void showUninstallLauncherDialog(String str);

    void startPushService();

    Boolean switchUser(int i);

    void systemReady(Runnable runnable, TimingsTraceLog timingsTraceLog);

    void unregisterHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier);

    boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback);

    String[] updateEntryPointArgsForPCMode(ProcessRecord processRecord, String[] strArr);

    void updateFreeFormOutLine(int i);

    void updateProcessRecordCurAdj(int i, ProcessRecord processRecord);

    void updateProcessRecordInfoBefStart(ProcessRecord processRecord);

    void updateProcessRecordMaxAdj(ProcessRecord processRecord);

    void updateUsageStatsForPCMode(ActivityRecord activityRecord, boolean z, UsageStatsManagerInternal usageStatsManagerInternal);

    boolean zrHungSendEvent(String str, int i, int i2, String str2, String str3, String str4);
}
