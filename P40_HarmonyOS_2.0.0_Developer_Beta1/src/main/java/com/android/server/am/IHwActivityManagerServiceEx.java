package com.android.server.am;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.SparseArray;
import android.util.TimingsTraceLog;
import android.view.WindowManagerPolicyConstants;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.mtm.taskstatus.ProcessInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IHwActivityManagerServiceEx {
    int bindServiceEx(IApplicationThread iApplicationThread, IBinder iBinder, Intent intent, String str, IServiceConnection iServiceConnection, int i, String str2, String str3, int i2, int i3);

    boolean canCleanTaskRecord(String str);

    boolean canPickColor(String str);

    int changeGidIfRepairMode(int i, String str);

    int[] changeGidsIfNeeded(ProcessRecord processRecord, int[] iArr);

    void checkAndPrintTestModeLog(List list, String str, String str2, String str3);

    void cleanAppForHiddenSpace();

    boolean cleanPackageRes(List<String> list, Map<String, List<String>> map, int i, boolean z, boolean z2, boolean z3);

    boolean cleanProcessResourceFast(String str, int i, IBinder iBinder, boolean z, boolean z2);

    File dumpStackTraces(ProcessRecord processRecord, boolean z, ArrayList<Integer> arrayList, ProcessCpuTracker processCpuTracker, SparseArray<Boolean> sparseArray, ArrayList<Integer> arrayList2);

    void finishBooting();

    void forceGCAfterRebooting();

    void forceStopPackages(List<String> list, int i);

    int getCloneAppUserId(String str, int i);

    int getContentProviderUserId(String str, int i);

    List<String> getPidWithUiFromUid(int i);

    WindowManagerPolicyConstants.PointerEventListener getPointerEventListener();

    boolean getProcessRecordFromMTM(ProcessInfo processInfo);

    int getStableProviderProcStatus(int i);

    String getTargetFromIntentForClone(Intent intent);

    boolean handleANRFilterFIFO(int i, int i2);

    void handleAppDiedLocked(ProcessRecord processRecord, boolean z, boolean z2);

    void handleApplicationCrash(String str, String str2);

    boolean isApplyPersistAppPatch(String str, int i, int i2, boolean z, boolean z2, String str2, String str3);

    boolean isHiddenSpaceSwitch(UserInfo userInfo, UserInfo userInfo2);

    boolean isLimitedPackageBroadcast(Intent intent);

    boolean isNeedForbidShellFunc(String str);

    boolean isPreloadEnable();

    void killApplication(String str, int i, int i2, String str2);

    boolean killNativeProcessRecordFast(String str, int i, int i2, boolean z, boolean z2, String str2);

    boolean killProcessRecordFast(String str, int i, int i2, boolean z, boolean z2, String str2, boolean z3);

    boolean killProcessRecordFromIAwareInternal(ProcessInfo processInfo, boolean z, boolean z2, String str, boolean z3, boolean z4);

    boolean killProcessRecordFromMTM(ProcessInfo processInfo, boolean z, String str);

    boolean needCheckProcDied(ProcessRecord processRecord);

    void notifyProcessStatusChange(String str, int i, int i2);

    int preloadAppForLauncher(String str, int i, int i2);

    int preloadApplication(String str, int i);

    void registerServiceHooker(IBinder iBinder, Intent intent);

    void removePackageAlarm(String str, List<String> list, int i);

    void removePackageStopFlag(String str, int i, String str2, int i2, String str3, Bundle bundle, int i3);

    void reportAppDiedMsg(int i, String str, int i2, String str2);

    void reportAssocDisable();

    void reportProcessDied(int i);

    void reportServiceRelationIAware(int i, ContentProviderRecord contentProviderRecord, ProcessRecord processRecord, boolean z);

    void reportServiceRelationIAware(int i, ServiceRecord serviceRecord, ProcessRecord processRecord, Intent intent, AppBindRecord appBindRecord);

    void setAndRestoreMaxAdjIfNeed(List<String> list);

    void setHmThreadToRtg(int i, String str);

    void setProcessRecForPid(int i);

    void setRtgThreadToIAware(ProcessRecord processRecord, boolean z);

    void setServiceFlagLocked(int i);

    void setThreadSchedPolicy(int i, ProcessRecord processRecord);

    boolean shouldPreventSendBroadcast(BroadcastRecord broadcastRecord, ResolveInfo resolveInfo, ProcessRecord processRecord, String str, boolean z);

    boolean shouldPreventStartProcess(ApplicationInfo applicationInfo, String str, int i);

    boolean shouldPreventStartProvider(ProviderInfo providerInfo, int i, int i2, int i3, ProcessRecord processRecord, boolean z);

    boolean shouldPreventStartService(ServiceInfo serviceInfo, int i, int i2, int i3, ProcessRecord processRecord, boolean z, Intent intent);

    boolean shouldSkipSendIntentSender(IIntentSender iIntentSender, Bundle bundle);

    void startPersistentAppsDelay(int i);

    void startPushService();

    Boolean switchUser(int i);

    void systemReady(Runnable runnable, TimingsTraceLog timingsTraceLog);

    int unbindServiceEx(IServiceConnection iServiceConnection, int i);

    void unregisterServiceHooker(IBinder iBinder);

    String[] updateEntryPointArgsForPCMode(ProcessRecord processRecord, String[] strArr);

    void updateProcessEntryPointArgsInfo(ProcessRecord processRecord, String[] strArr);

    void updateProcessRecordCurAdj(int i, ProcessRecord processRecord);

    void updateProcessRecordInfoBefStart(ProcessRecord processRecord);

    void updateProcessRecordMaxAdj(ProcessRecord processRecord);

    boolean zrHungSendEvent(String str, int i, int i2, String str2, String str3, String str4);
}
