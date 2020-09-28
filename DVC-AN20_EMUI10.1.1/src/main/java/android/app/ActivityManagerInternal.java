package android.app;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityPresentationInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.TransactionTooLargeException;
import java.util.ArrayList;
import java.util.List;

public abstract class ActivityManagerInternal {
    public static final int ALLOW_FULL_ONLY = 2;
    public static final int ALLOW_NON_FULL = 0;
    public static final int ALLOW_NON_FULL_IN_PROFILE = 1;

    public abstract void broadcastCloseSystemDialogs(String str);

    public abstract void broadcastGlobalConfigurationChanged(int i, boolean z);

    public abstract int broadcastIntentInPackage(String str, int i, int i2, int i3, Intent intent, String str2, IIntentReceiver iIntentReceiver, int i4, String str3, Bundle bundle, String str4, Bundle bundle2, boolean z, boolean z2, int i5, boolean z3);

    public abstract boolean canStartMoreUsers();

    public abstract String checkContentProviderAccess(String str, int i);

    public abstract void cleanUpServices(int i, ComponentName componentName, Intent intent);

    public abstract void clearPendingBackup(int i);

    public abstract void clearPendingIntentAllowBgActivityStarts(IIntentSender iIntentSender, IBinder iBinder);

    public abstract void disconnectActivityFromServices(Object obj, Object obj2);

    public abstract void enforceCallingPermission(String str, String str2);

    public abstract void ensureBootCompleted();

    public abstract void ensureNotSpecialUser(int i);

    public abstract void finishBooting();

    public abstract void finishUserSwitch(Object obj);

    public abstract ActivityInfo getActivityInfoForUser(ActivityInfo activityInfo, int i);

    public abstract ActivityPresentationInfo getActivityPresentationInfo(IBinder iBinder);

    public abstract int[] getCurrentProfileIds();

    public abstract UserInfo getCurrentUser();

    public abstract int getCurrentUserId();

    public abstract boolean getHaveTryCloneProUserUnlock();

    public abstract int getMaxRunningUsers();

    public abstract List<ProcessMemoryState> getMemoryStateForProcesses();

    public abstract int getStorageMountMode(int i, int i2);

    public abstract int getTaskIdForActivity(IBinder iBinder, boolean z);

    public abstract int getUidProcessState(int i);

    public abstract int handleIncomingUser(int i, int i2, int i3, boolean z, int i4, String str, String str2);

    public abstract int handleUserForClone(String str, int i);

    public abstract boolean hasRunningActivity(int i, String str);

    public abstract boolean hasRunningForegroundService(int i, int i2);

    public abstract boolean hasStartedUserState(int i);

    public abstract long inputDispatchingTimedOut(int i, boolean z, String str);

    public abstract boolean inputDispatchingTimedOut(Object obj, String str, ApplicationInfo applicationInfo, String str2, Object obj2, boolean z, String str3);

    public abstract boolean isActivityStartsLoggingEnabled();

    public abstract boolean isAppBad(ApplicationInfo applicationInfo);

    public abstract boolean isAppForeground(int i);

    public abstract boolean isBackgroundActivityStartsEnabled();

    public abstract boolean isBooted();

    public abstract boolean isBooting();

    public abstract boolean isCurrentProfile(int i);

    public abstract boolean isRuntimeRestarted();

    public abstract boolean isSystemReady();

    public abstract boolean isUidActive(int i);

    public abstract boolean isUserRunning(int i, int i2);

    public abstract void killAllBackgroundProcessesExcept(int i, int i2);

    public abstract void killForegroundAppsForUser(int i);

    public abstract void killProcess(String str, int i, String str2);

    public abstract void killProcessesForRemovedTask(ArrayList<Object> arrayList);

    public abstract void notifyNetworkPolicyRulesUpdated(int i, long j);

    public abstract void onWakefulnessChanged(int i);

    public abstract void preloadApp(ApplicationInfo applicationInfo);

    public abstract void prepareForPossibleShutdown();

    public abstract void registerProcessObserver(IProcessObserver iProcessObserver);

    public abstract void reportCurKeyguardUsageEvent(boolean z);

    public abstract void scheduleAppGcs();

    public abstract void sendForegroundProfileChanged(int i);

    public abstract void setBooted(boolean z);

    public abstract void setBooting(boolean z);

    public abstract void setDebugFlagsForStartingActivity(ActivityInfo activityInfo, int i, ProfilerInfo profilerInfo, Object obj);

    public abstract void setDeviceIdleWhitelist(int[] iArr, int[] iArr2);

    public abstract void setHasOverlayUi(int i, boolean z);

    public abstract void setPendingIntentAllowBgActivityStarts(IIntentSender iIntentSender, IBinder iBinder, int i);

    public abstract void setPendingIntentWhitelistDuration(IIntentSender iIntentSender, IBinder iBinder, long j);

    public abstract void setRunningRemoteAnimation(int i, boolean z);

    public abstract void setSwitchingFromSystemUserMessage(String str);

    public abstract void setSwitchingToSystemUserMessage(String str);

    public abstract boolean shouldConfirmCredentials(int i);

    public abstract boolean startIsolatedProcess(String str, String[] strArr, String str2, String str3, int i, Runnable runnable);

    public abstract void startProcess(String str, ApplicationInfo applicationInfo, boolean z, String str2, ComponentName componentName);

    public abstract ComponentName startServiceInPackage(int i, Intent intent, String str, boolean z, String str2, int i2, boolean z2) throws TransactionTooLargeException;

    public abstract void tempWhitelistForPendingIntent(int i, int i2, int i3, long j, String str);

    public abstract void trimApplications();

    public abstract void unregisterProcessObserver(IProcessObserver iProcessObserver);

    public abstract void updateActivityUsageStats(ComponentName componentName, int i, int i2, IBinder iBinder, ComponentName componentName2);

    public abstract void updateBatteryStats(ComponentName componentName, int i, int i2, boolean z);

    public abstract void updateCpuStats();

    public abstract void updateDeviceIdleTempWhitelist(int[] iArr, int i, boolean z);

    public abstract void updateForegroundTimeIfOnBattery(String str, int i, long j);

    public abstract void updateOomAdj();

    public abstract void updateOomLevelsForDisplay(int i);
}
