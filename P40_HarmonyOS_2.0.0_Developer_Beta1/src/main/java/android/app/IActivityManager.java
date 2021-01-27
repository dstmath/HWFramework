package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.IAssistDataReceiver;
import android.app.IInstrumentationWatcher;
import android.app.IProcessObserver;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.ITaskStackListener;
import android.app.IUiAutomationConnection;
import android.app.IUidObserver;
import android.app.IUserSwitchObserver;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IProgressListener;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.WorkSource;
import android.text.TextUtils;
import android.view.IRecentsAnimationRunner;
import com.android.internal.os.IResultReceiver;
import java.util.List;

public interface IActivityManager extends IInterface {
    void addInstrumentationResults(IApplicationThread iApplicationThread, Bundle bundle) throws RemoteException;

    void addPackageDependency(String str) throws RemoteException;

    void appNotRespondingViaProvider(IBinder iBinder) throws RemoteException;

    void attachApplication(IApplicationThread iApplicationThread, long j) throws RemoteException;

    void backgroundWhitelistUid(int i) throws RemoteException;

    void backupAgentCreated(String str, IBinder iBinder, int i) throws RemoteException;

    boolean bindBackupAgent(String str, int i, int i2) throws RemoteException;

    int bindIsolatedService(IApplicationThread iApplicationThread, IBinder iBinder, Intent intent, String str, IServiceConnection iServiceConnection, int i, String str2, String str3, int i2) throws RemoteException;

    @UnsupportedAppUsage
    int bindService(IApplicationThread iApplicationThread, IBinder iBinder, Intent intent, String str, IServiceConnection iServiceConnection, int i, String str2, int i2) throws RemoteException;

    void bootAnimationComplete() throws RemoteException;

    @UnsupportedAppUsage
    int broadcastIntent(IApplicationThread iApplicationThread, Intent intent, String str, IIntentReceiver iIntentReceiver, int i, String str2, Bundle bundle, String[] strArr, int i2, Bundle bundle2, boolean z, boolean z2, int i3) throws RemoteException;

    void cancelIntentSender(IIntentSender iIntentSender) throws RemoteException;

    @UnsupportedAppUsage
    void cancelRecentsAnimation(boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void cancelTaskWindowTransition(int i) throws RemoteException;

    @UnsupportedAppUsage
    int checkPermission(String str, int i, int i2) throws RemoteException;

    int checkPermissionWithToken(String str, int i, int i2, IBinder iBinder) throws RemoteException;

    int checkUriPermission(Uri uri, int i, int i2, int i3, int i4, IBinder iBinder) throws RemoteException;

    boolean clearApplicationUserData(String str, boolean z, IPackageDataObserver iPackageDataObserver, int i) throws RemoteException;

    @UnsupportedAppUsage
    void closeSystemDialogs(String str) throws RemoteException;

    void crashApplication(int i, int i2, String str, int i3, String str2, boolean z) throws RemoteException;

    boolean dumpHeap(String str, int i, boolean z, boolean z2, boolean z3, String str2, ParcelFileDescriptor parcelFileDescriptor, RemoteCallback remoteCallback) throws RemoteException;

    void dumpHeapFinished(String str) throws RemoteException;

    void enterSafeMode() throws RemoteException;

    @UnsupportedAppUsage
    boolean finishActivity(IBinder iBinder, int i, Intent intent, int i2) throws RemoteException;

    void finishBindApplication(IApplicationThread iApplicationThread) throws RemoteException;

    @UnsupportedAppUsage
    void finishHeavyWeightApp() throws RemoteException;

    void finishInstrumentation(IApplicationThread iApplicationThread, int i, Bundle bundle) throws RemoteException;

    void finishReceiver(IBinder iBinder, int i, String str, Bundle bundle, boolean z, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void forceStopPackage(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    List<ActivityManager.StackInfo> getAllStackInfos() throws RemoteException;

    @UnsupportedAppUsage
    Configuration getConfiguration() throws RemoteException;

    ContentProviderHolder getContentProvider(IApplicationThread iApplicationThread, String str, String str2, int i, boolean z) throws RemoteException;

    ContentProviderHolder getContentProviderExternal(String str, int i, IBinder iBinder, String str2) throws RemoteException;

    @UnsupportedAppUsage
    UserInfo getCurrentUser() throws RemoteException;

    @UnsupportedAppUsage
    List<ActivityManager.RunningTaskInfo> getFilteredTasks(int i, int i2, int i3) throws RemoteException;

    ActivityManager.StackInfo getFocusedStackInfo() throws RemoteException;

    int getForegroundServiceType(ComponentName componentName, IBinder iBinder) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    @UnsupportedAppUsage
    Intent getIntentForIntentSender(IIntentSender iIntentSender) throws RemoteException;

    @UnsupportedAppUsage
    IIntentSender getIntentSender(int i, String str, IBinder iBinder, String str2, int i2, Intent[] intentArr, String[] strArr, int i3, Bundle bundle, int i4) throws RemoteException;

    @UnsupportedAppUsage
    String getLaunchedFromPackage(IBinder iBinder) throws RemoteException;

    @UnsupportedAppUsage
    int getLaunchedFromUid(IBinder iBinder) throws RemoteException;

    ParcelFileDescriptor getLifeMonitor() throws RemoteException;

    @UnsupportedAppUsage
    int getLockTaskModeState() throws RemoteException;

    @UnsupportedAppUsage
    void getMemoryInfo(ActivityManager.MemoryInfo memoryInfo) throws RemoteException;

    int getMemoryTrimLevel() throws RemoteException;

    void getMyMemoryState(ActivityManager.RunningAppProcessInfo runningAppProcessInfo) throws RemoteException;

    String getPackageForIntentSender(IIntentSender iIntentSender) throws RemoteException;

    @UnsupportedAppUsage
    int getPackageProcessState(String str, String str2) throws RemoteException;

    @UnsupportedAppUsage
    int getProcessLimit() throws RemoteException;

    @UnsupportedAppUsage
    Debug.MemoryInfo[] getProcessMemoryInfo(int[] iArr) throws RemoteException;

    @UnsupportedAppUsage
    long[] getProcessPss(int[] iArr) throws RemoteException;

    List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException;

    @UnsupportedAppUsage
    String getProviderMimeType(Uri uri, int i) throws RemoteException;

    @UnsupportedAppUsage
    ParceledListSlice getRecentTasks(int i, int i2, int i3) throws RemoteException;

    @UnsupportedAppUsage
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException;

    List<ApplicationInfo> getRunningExternalApplications() throws RemoteException;

    PendingIntent getRunningServiceControlPanel(ComponentName componentName) throws RemoteException;

    int[] getRunningUserIds() throws RemoteException;

    @UnsupportedAppUsage
    List<ActivityManager.RunningServiceInfo> getServices(int i, int i2) throws RemoteException;

    String getTagForIntentSender(IIntentSender iIntentSender, String str) throws RemoteException;

    @UnsupportedAppUsage
    Rect getTaskBounds(int i) throws RemoteException;

    @UnsupportedAppUsage
    int getTaskForActivity(IBinder iBinder, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    ActivityManager.TaskSnapshot getTaskSnapshot(int i, boolean z) throws RemoteException;

    List<ActivityManager.RunningTaskInfo> getTasks(int i) throws RemoteException;

    int getUidForIntentSender(IIntentSender iIntentSender) throws RemoteException;

    int getUidProcessState(int i, String str) throws RemoteException;

    void grantUriPermission(IApplicationThread iApplicationThread, String str, Uri uri, int i, int i2) throws RemoteException;

    void handleApplicationCrash(IBinder iBinder, ApplicationErrorReport.ParcelableCrashInfo parcelableCrashInfo) throws RemoteException;

    @UnsupportedAppUsage
    void handleApplicationStrictModeViolation(IBinder iBinder, int i, StrictMode.ViolationInfo violationInfo) throws RemoteException;

    boolean handleApplicationWtf(IBinder iBinder, String str, boolean z, ApplicationErrorReport.ParcelableCrashInfo parcelableCrashInfo) throws RemoteException;

    int handleIncomingUser(int i, int i2, int i3, boolean z, boolean z2, String str, String str2) throws RemoteException;

    @UnsupportedAppUsage
    void hang(IBinder iBinder, boolean z) throws RemoteException;

    boolean isAppStartModeDisabled(int i, String str) throws RemoteException;

    boolean isBackgroundRestricted(String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean isInLockTaskMode() throws RemoteException;

    boolean isIntentSenderABroadcast(IIntentSender iIntentSender) throws RemoteException;

    boolean isIntentSenderAForegroundService(IIntentSender iIntentSender) throws RemoteException;

    @UnsupportedAppUsage
    boolean isIntentSenderAnActivity(IIntentSender iIntentSender) throws RemoteException;

    boolean isIntentSenderTargetedToPackage(IIntentSender iIntentSender) throws RemoteException;

    boolean isTopActivityImmersive() throws RemoteException;

    @UnsupportedAppUsage
    boolean isTopOfTask(IBinder iBinder) throws RemoteException;

    boolean isUidActive(int i, String str) throws RemoteException;

    boolean isUserAMonkey() throws RemoteException;

    @UnsupportedAppUsage
    boolean isUserRunning(int i, int i2) throws RemoteException;

    boolean isVrModePackageEnabled(ComponentName componentName) throws RemoteException;

    @UnsupportedAppUsage
    void killAllBackgroundProcesses() throws RemoteException;

    void killApplication(String str, int i, int i2, String str2) throws RemoteException;

    void killApplicationProcess(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    void killBackgroundProcesses(String str, int i) throws RemoteException;

    void killPackageDependents(String str, int i) throws RemoteException;

    boolean killPids(int[] iArr, String str, boolean z) throws RemoteException;

    boolean killProcessesBelowForeground(String str) throws RemoteException;

    void killUid(int i, int i2, String str) throws RemoteException;

    void makePackageIdle(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean moveActivityTaskToBack(IBinder iBinder, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void moveTaskToFront(IApplicationThread iApplicationThread, String str, int i, int i2, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    void moveTaskToStack(int i, int i2, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    boolean moveTopActivityToPinnedStack(int i, Rect rect) throws RemoteException;

    void noteAlarmFinish(IIntentSender iIntentSender, WorkSource workSource, int i, String str) throws RemoteException;

    void noteAlarmStart(IIntentSender iIntentSender, WorkSource workSource, int i, String str) throws RemoteException;

    void noteWakeupAlarm(IIntentSender iIntentSender, WorkSource workSource, int i, String str, String str2) throws RemoteException;

    void notifyCleartextNetwork(int i, byte[] bArr) throws RemoteException;

    void notifyLockedProfile(int i) throws RemoteException;

    ParcelFileDescriptor openContentUri(String str) throws RemoteException;

    IBinder peekService(Intent intent, String str, String str2) throws RemoteException;

    void performIdleMaintenance() throws RemoteException;

    @UnsupportedAppUsage
    void positionTaskInStack(int i, int i2, int i3) throws RemoteException;

    @UnsupportedAppUsage
    boolean profileControl(String str, int i, boolean z, ProfilerInfo profilerInfo, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void publishContentProviders(IApplicationThread iApplicationThread, List<ContentProviderHolder> list) throws RemoteException;

    void publishService(IBinder iBinder, Intent intent, IBinder iBinder2) throws RemoteException;

    boolean refContentProvider(IBinder iBinder, int i, int i2) throws RemoteException;

    void registerIntentSenderCancelListener(IIntentSender iIntentSender, IResultReceiver iResultReceiver) throws RemoteException;

    @UnsupportedAppUsage
    void registerProcessObserver(IProcessObserver iProcessObserver) throws RemoteException;

    @UnsupportedAppUsage
    Intent registerReceiver(IApplicationThread iApplicationThread, String str, IIntentReceiver iIntentReceiver, IntentFilter intentFilter, String str2, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void registerTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void registerUidObserver(IUidObserver iUidObserver, int i, int i2, String str) throws RemoteException;

    @UnsupportedAppUsage
    void registerUserSwitchObserver(IUserSwitchObserver iUserSwitchObserver, String str) throws RemoteException;

    void removeContentProvider(IBinder iBinder, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void removeContentProviderExternal(String str, IBinder iBinder) throws RemoteException;

    void removeContentProviderExternalAsUser(String str, IBinder iBinder, int i) throws RemoteException;

    @UnsupportedAppUsage
    void removeStack(int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean removeTask(int i) throws RemoteException;

    @UnsupportedAppUsage
    void requestBugReport(int i) throws RemoteException;

    void requestSystemServerHeapDump() throws RemoteException;

    void requestTelephonyBugReport(String str, String str2) throws RemoteException;

    void requestWifiBugReport(String str, String str2) throws RemoteException;

    @UnsupportedAppUsage
    void resizeDockedStack(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5) throws RemoteException;

    @UnsupportedAppUsage
    void resizeStack(int i, Rect rect, boolean z, boolean z2, boolean z3, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void resizeTask(int i, Rect rect, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void restart() throws RemoteException;

    int restartUserInBackground(int i) throws RemoteException;

    @UnsupportedAppUsage
    void resumeAppSwitches() throws RemoteException;

    void revokeUriPermission(IApplicationThread iApplicationThread, String str, Uri uri, int i, int i2) throws RemoteException;

    void scheduleApplicationInfoChanged(List<String> list, int i) throws RemoteException;

    @UnsupportedAppUsage
    void sendIdleJobTrigger() throws RemoteException;

    int sendIntentSender(IIntentSender iIntentSender, IBinder iBinder, int i, Intent intent, String str, IIntentReceiver iIntentReceiver, String str2, Bundle bundle) throws RemoteException;

    void serviceDoneExecuting(IBinder iBinder, int i, int i2, int i3) throws RemoteException;

    @UnsupportedAppUsage
    void setActivityController(IActivityController iActivityController, boolean z) throws RemoteException;

    void setAgentApp(String str, String str2) throws RemoteException;

    @UnsupportedAppUsage
    void setAlwaysFinish(boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void setDebugApp(String str, boolean z, boolean z2) throws RemoteException;

    @UnsupportedAppUsage
    void setDumpHeapDebugLimit(String str, int i, long j, String str2) throws RemoteException;

    void setFocusedStack(int i) throws RemoteException;

    void setHasTopUi(boolean z) throws RemoteException;

    void setHmThreadToRtg(String str) throws RemoteException;

    @UnsupportedAppUsage
    void setPackageScreenCompatMode(String str, int i) throws RemoteException;

    void setPersistentVrThread(int i) throws RemoteException;

    @UnsupportedAppUsage
    void setProcessImportant(IBinder iBinder, int i, boolean z, String str) throws RemoteException;

    @UnsupportedAppUsage
    void setProcessLimit(int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean setProcessMemoryTrimLevel(String str, int i, int i2) throws RemoteException;

    void setRenderThread(int i) throws RemoteException;

    @UnsupportedAppUsage
    void setRequestedOrientation(IBinder iBinder, int i) throws RemoteException;

    void setServiceForeground(ComponentName componentName, IBinder iBinder, int i, Notification notification, int i2, int i3) throws RemoteException;

    @UnsupportedAppUsage
    void setTaskResizeable(int i, int i2) throws RemoteException;

    void setUserIsMonkey(boolean z) throws RemoteException;

    void showBootMessage(CharSequence charSequence, boolean z) throws RemoteException;

    void showWaitingForDebugger(IApplicationThread iApplicationThread, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    boolean shutdown(int i) throws RemoteException;

    void signalPersistentProcesses(int i) throws RemoteException;

    @UnsupportedAppUsage
    int startActivity(IApplicationThread iApplicationThread, String str, Intent intent, String str2, IBinder iBinder, String str3, int i, int i2, ProfilerInfo profilerInfo, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    int startActivityAsUser(IApplicationThread iApplicationThread, String str, Intent intent, String str2, IBinder iBinder, String str3, int i, int i2, ProfilerInfo profilerInfo, Bundle bundle, int i3) throws RemoteException;

    @UnsupportedAppUsage
    int startActivityFromRecents(int i, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    boolean startBinderTracking() throws RemoteException;

    void startConfirmDeviceCredentialIntent(Intent intent, Bundle bundle) throws RemoteException;

    void startDelegateShellPermissionIdentity(int i, String[] strArr) throws RemoteException;

    @UnsupportedAppUsage
    boolean startInstrumentation(ComponentName componentName, String str, int i, Bundle bundle, IInstrumentationWatcher iInstrumentationWatcher, IUiAutomationConnection iUiAutomationConnection, int i2, String str2) throws RemoteException;

    @UnsupportedAppUsage
    void startRecentsActivity(Intent intent, IAssistDataReceiver iAssistDataReceiver, IRecentsAnimationRunner iRecentsAnimationRunner) throws RemoteException;

    ComponentName startService(IApplicationThread iApplicationThread, Intent intent, String str, boolean z, String str2, int i) throws RemoteException;

    @UnsupportedAppUsage
    void startSystemLockTaskMode(int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean startUserInBackground(int i) throws RemoteException;

    boolean startUserInBackgroundWithListener(int i, IProgressListener iProgressListener) throws RemoteException;

    boolean startUserInForegroundWithListener(int i, IProgressListener iProgressListener) throws RemoteException;

    @UnsupportedAppUsage
    void stopAppSwitches() throws RemoteException;

    @UnsupportedAppUsage
    boolean stopBinderTrackingAndDump(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void stopDelegateShellPermissionIdentity() throws RemoteException;

    @UnsupportedAppUsage
    int stopService(IApplicationThread iApplicationThread, Intent intent, String str, int i) throws RemoteException;

    boolean stopServiceToken(ComponentName componentName, IBinder iBinder, int i) throws RemoteException;

    @UnsupportedAppUsage
    int stopUser(int i, boolean z, IStopUserCallback iStopUserCallback) throws RemoteException;

    @UnsupportedAppUsage
    void suppressResizeConfigChanges(boolean z) throws RemoteException;

    @UnsupportedAppUsage
    boolean switchUser(int i) throws RemoteException;

    void unbindBackupAgent(ApplicationInfo applicationInfo) throws RemoteException;

    void unbindFinished(IBinder iBinder, Intent intent, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    boolean unbindService(IServiceConnection iServiceConnection) throws RemoteException;

    void unbroadcastIntent(IApplicationThread iApplicationThread, Intent intent, int i) throws RemoteException;

    @UnsupportedAppUsage
    void unhandledBack() throws RemoteException;

    @UnsupportedAppUsage
    boolean unlockUser(int i, byte[] bArr, byte[] bArr2, IProgressListener iProgressListener) throws RemoteException;

    void unregisterIntentSenderCancelListener(IIntentSender iIntentSender, IResultReceiver iResultReceiver) throws RemoteException;

    @UnsupportedAppUsage
    void unregisterProcessObserver(IProcessObserver iProcessObserver) throws RemoteException;

    @UnsupportedAppUsage
    void unregisterReceiver(IIntentReceiver iIntentReceiver) throws RemoteException;

    void unregisterTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void unregisterUidObserver(IUidObserver iUidObserver) throws RemoteException;

    void unregisterUserSwitchObserver(IUserSwitchObserver iUserSwitchObserver) throws RemoteException;

    @UnsupportedAppUsage
    void unstableProviderDied(IBinder iBinder) throws RemoteException;

    @UnsupportedAppUsage
    boolean updateConfiguration(Configuration configuration) throws RemoteException;

    void updateDeviceOwner(String str) throws RemoteException;

    void updateLockTaskPackages(int i, String[] strArr) throws RemoteException;

    @UnsupportedAppUsage
    void updatePersistentConfiguration(Configuration configuration) throws RemoteException;

    void updateServiceGroup(IServiceConnection iServiceConnection, int i, int i2) throws RemoteException;

    void waitForNetworkStateUpdate(long j) throws RemoteException;

    public static class Default implements IActivityManager {
        @Override // android.app.IActivityManager
        public ParcelFileDescriptor openContentUri(String uriString) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void registerUidObserver(IUidObserver observer, int which, int cutpoint, String callingPackage) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void unregisterUidObserver(IUidObserver observer) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean isUidActive(int uid, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public int getUidProcessState(int uid, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void handleApplicationCrash(IBinder app, ApplicationErrorReport.ParcelableCrashInfo crashInfo) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void unhandledBack() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean finishActivity(IBinder token, int code, Intent data, int finishTask) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public Intent registerReceiver(IApplicationThread caller, String callerPackage, IIntentReceiver receiver, IntentFilter filter, String requiredPermission, int userId, int flags) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void unregisterReceiver(IIntentReceiver receiver) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int broadcastIntent(IApplicationThread caller, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle map, String[] requiredPermissions, int appOp, Bundle options, boolean serialized, boolean sticky, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void finishReceiver(IBinder who, int resultCode, String resultData, Bundle map, boolean abortBroadcast, int flags) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void attachApplication(IApplicationThread app, long startSeq) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public List<ActivityManager.RunningTaskInfo> getFilteredTasks(int maxNum, int ignoreActivityType, int ignoreWindowingMode) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void moveTaskToFront(IApplicationThread caller, String callingPackage, int task, int flags, Bundle options) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int getTaskForActivity(IBinder token, boolean onlyRoot) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public ContentProviderHolder getContentProvider(IApplicationThread caller, String callingPackage, String name, int userId, boolean stable) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void publishContentProviders(IApplicationThread caller, List<ContentProviderHolder> list) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean refContentProvider(IBinder connection, int stableDelta, int unstableDelta) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public PendingIntent getRunningServiceControlPanel(ComponentName service) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, boolean requireForeground, String callingPackage, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public int bindIsolatedService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String instanceName, String callingPackage, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void updateServiceGroup(IServiceConnection connection, int group, int importance) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean unbindService(IServiceConnection connection) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setDebugApp(String packageName, boolean waitForDebugger, boolean persistent) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setAgentApp(String packageName, String agent) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setAlwaysFinish(boolean enabled) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean startInstrumentation(ComponentName className, String profileFile, int flags, Bundle arguments, IInstrumentationWatcher watcher, IUiAutomationConnection connection, int userId, String abiOverride) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void addInstrumentationResults(IApplicationThread target, Bundle results) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void finishInstrumentation(IApplicationThread target, int resultCode, Bundle results) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public Configuration getConfiguration() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public boolean updateConfiguration(Configuration values) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void setProcessLimit(int max) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int getProcessLimit() throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public int checkPermission(String permission, int pid, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public int checkUriPermission(Uri uri, int pid, int uid, int mode, int userId, IBinder callerToken) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void revokeUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setActivityController(IActivityController watcher, boolean imAMonkey) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void showWaitingForDebugger(IApplicationThread who, boolean waiting) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void signalPersistentProcesses(int signal) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public ParceledListSlice getRecentTasks(int maxNum, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void cancelIntentSender(IIntentSender sender) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public String getPackageForIntentSender(IIntentSender sender) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void registerIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void unregisterIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void enterSafeMode() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void noteWakeupAlarm(IIntentSender sender, WorkSource workSource, int sourceUid, String sourcePkg, String tag) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void removeContentProvider(IBinder connection, boolean stable) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setRequestedOrientation(IBinder token, int requestedOrientation) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void unbindFinished(IBinder token, Intent service, boolean doRebind) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setProcessImportant(IBinder token, int pid, boolean isForeground, String reason) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, int flags, int foregroundServiceType) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int getForegroundServiceType(ComponentName className, IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public boolean clearApplicationUserData(String packageName, boolean keepState, IPackageDataObserver observer, int userId) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void forceStopPackage(String packageName, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean killPids(int[] pids, String reason, boolean secure) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public List<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public IBinder peekService(Intent service, String resolvedType, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public boolean profileControl(String process, int userId, boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean shutdown(int timeout) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void stopAppSwitches() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void resumeAppSwitches() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean bindBackupAgent(String packageName, int backupRestoreMode, int targetUserId) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void backupAgentCreated(String packageName, IBinder agent, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void unbindBackupAgent(ApplicationInfo appInfo) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int getUidForIntentSender(IIntentSender sender) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void addPackageDependency(String packageName) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void killApplication(String pkg, int appId, int userId, String reason) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void closeSystemDialogs(String reason) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void killApplicationProcess(String processName, int uid) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean handleApplicationWtf(IBinder app, String tag, boolean system, ApplicationErrorReport.ParcelableCrashInfo crashInfo) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void killBackgroundProcesses(String packageName, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean isUserAMonkey() throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public List<ApplicationInfo> getRunningExternalApplications() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void finishHeavyWeightApp() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void handleApplicationStrictModeViolation(IBinder app, int penaltyMask, StrictMode.ViolationInfo crashInfo) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean isTopActivityImmersive() throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void crashApplication(int uid, int initialPid, String packageName, int userId, String message, boolean force) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public String getProviderMimeType(Uri uri, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public boolean dumpHeap(String process, int userId, boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd, RemoteCallback finishCallback) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean isUserRunning(int userid, int flags) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void setPackageScreenCompatMode(String packageName, int mode) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean switchUser(int userid) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean removeTask(int taskId) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void registerProcessObserver(IProcessObserver observer) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void unregisterProcessObserver(IProcessObserver observer) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean isIntentSenderTargetedToPackage(IIntentSender sender) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void updatePersistentConfiguration(Configuration values) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public long[] getProcessPss(int[] pids) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void showBootMessage(CharSequence msg, boolean always) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void killAllBackgroundProcesses() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token, String tag) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void removeContentProviderExternal(String name, IBinder token) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void removeContentProviderExternalAsUser(String name, IBinder token, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void getMyMemoryState(ActivityManager.RunningAppProcessInfo outInfo) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean killProcessesBelowForeground(String reason) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public UserInfo getCurrentUser() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public int getLaunchedFromUid(IBinder activityToken) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void unstableProviderDied(IBinder connection) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean isIntentSenderAnActivity(IIntentSender sender) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean isIntentSenderAForegroundService(IIntentSender sender) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean isIntentSenderABroadcast(IIntentSender sender) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public int stopUser(int userid, boolean force, IStopUserCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void registerUserSwitchObserver(IUserSwitchObserver observer, String name) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void unregisterUserSwitchObserver(IUserSwitchObserver observer) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int[] getRunningUserIds() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void requestSystemServerHeapDump() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void requestBugReport(int bugreportType) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void requestTelephonyBugReport(String shareTitle, String shareDescription) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void requestWifiBugReport(String shareTitle, String shareDescription) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public Intent getIntentForIntentSender(IIntentSender sender) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public String getLaunchedFromPackage(IBinder activityToken) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void killUid(int appId, int userId, String reason) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setUserIsMonkey(boolean monkey) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void hang(IBinder who, boolean allowRestart) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public List<ActivityManager.StackInfo> getAllStackInfos() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void moveTaskToStack(int taskId, int stackId, boolean toTop) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void resizeStack(int stackId, Rect bounds, boolean allowResizeInDockedMode, boolean preserveWindows, boolean animate, int animationDuration) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setFocusedStack(int stackId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public ActivityManager.StackInfo getFocusedStackInfo() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void restart() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void performIdleMaintenance() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void appNotRespondingViaProvider(IBinder connection) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public Rect getTaskBounds(int taskId) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public boolean setProcessMemoryTrimLevel(String process, int uid, int level) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public String getTagForIntentSender(IIntentSender sender, String prefix) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public boolean startUserInBackground(int userid) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean isInLockTaskMode() throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void startRecentsActivity(Intent intent, IAssistDataReceiver assistDataReceiver, IRecentsAnimationRunner recentsAnimationRunner) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void cancelRecentsAnimation(boolean restoreHomeStackPosition) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int startActivityFromRecents(int taskId, Bundle options) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void startSystemLockTaskMode(int taskId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean isTopOfTask(IBinder token) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void bootAnimationComplete() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int checkPermissionWithToken(String permission, int pid, int uid, IBinder callerToken) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void registerTaskStackListener(ITaskStackListener listener) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void unregisterTaskStackListener(ITaskStackListener listener) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void notifyCleartextNetwork(int uid, byte[] firstPacket) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setTaskResizeable(int taskId, int resizeableMode) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void resizeTask(int taskId, Rect bounds, int resizeMode) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int getLockTaskModeState() throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void setDumpHeapDebugLimit(String processName, int uid, long maxMemSize, String reportPackage) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void dumpHeapFinished(String path) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void updateLockTaskPackages(int userId, String[] packages) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void noteAlarmStart(IIntentSender sender, WorkSource workSource, int sourceUid, String tag) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void noteAlarmFinish(IIntentSender sender, WorkSource workSource, int sourceUid, String tag) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int getPackageProcessState(String packageName, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void updateDeviceOwner(String packageName) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean startBinderTracking() throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean stopBinderTrackingAndDump(ParcelFileDescriptor fd) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void positionTaskInStack(int taskId, int stackId, int position) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void suppressResizeConfigChanges(boolean suppress) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean moveTopActivityToPinnedStack(int stackId, Rect bounds) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean isAppStartModeDisabled(int uid, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public boolean unlockUser(int userid, byte[] token, byte[] secret, IProgressListener listener) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void killPackageDependents(String packageName, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void removeStack(int stackId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void makePackageIdle(String packageName, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int getMemoryTrimLevel() throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public boolean isVrModePackageEnabled(ComponentName packageName) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void notifyLockedProfile(int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void startConfirmDeviceCredentialIntent(Intent intent, Bundle options) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void sendIdleJobTrigger() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int sendIntentSender(IIntentSender target, IBinder whitelistToken, int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public boolean isBackgroundRestricted(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void setRenderThread(int tid) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setHasTopUi(boolean hasTopUi) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public int restartUserInBackground(int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityManager
        public void cancelTaskWindowTransition(int taskId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void scheduleApplicationInfoChanged(List<String> list, int userId) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setPersistentVrThread(int tid) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void waitForNetworkStateUpdate(long procStateSeq) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void backgroundWhitelistUid(int uid) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public boolean startUserInBackgroundWithListener(int userid, IProgressListener unlockProgressListener) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public void startDelegateShellPermissionIdentity(int uid, String[] permissions) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void stopDelegateShellPermissionIdentity() throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public ParcelFileDescriptor getLifeMonitor() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public boolean startUserInForegroundWithListener(int userid, IProgressListener unlockProgressListener) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityManager
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.app.IActivityManager
        public void finishBindApplication(IApplicationThread app) throws RemoteException {
        }

        @Override // android.app.IActivityManager
        public void setHmThreadToRtg(String param) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IActivityManager {
        private static final String DESCRIPTOR = "android.app.IActivityManager";
        static final int TRANSACTION_addInstrumentationResults = 35;
        static final int TRANSACTION_addPackageDependency = 82;
        static final int TRANSACTION_appNotRespondingViaProvider = 140;
        static final int TRANSACTION_attachApplication = 15;
        static final int TRANSACTION_backgroundWhitelistUid = 192;
        static final int TRANSACTION_backupAgentCreated = 78;
        static final int TRANSACTION_bindBackupAgent = 77;
        static final int TRANSACTION_bindIsolatedService = 27;
        static final int TRANSACTION_bindService = 26;
        static final int TRANSACTION_bootAnimationComplete = 151;
        static final int TRANSACTION_broadcastIntent = 12;
        static final int TRANSACTION_cancelIntentSender = 52;
        static final int TRANSACTION_cancelRecentsAnimation = 147;
        static final int TRANSACTION_cancelTaskWindowTransition = 187;
        static final int TRANSACTION_checkPermission = 42;
        static final int TRANSACTION_checkPermissionWithToken = 152;
        static final int TRANSACTION_checkUriPermission = 43;
        static final int TRANSACTION_clearApplicationUserData = 67;
        static final int TRANSACTION_closeSystemDialogs = 84;
        static final int TRANSACTION_crashApplication = 94;
        static final int TRANSACTION_dumpHeap = 96;
        static final int TRANSACTION_dumpHeapFinished = 160;
        static final int TRANSACTION_enterSafeMode = 56;
        static final int TRANSACTION_finishActivity = 9;
        static final int TRANSACTION_finishBindApplication = 199;
        static final int TRANSACTION_finishHeavyWeightApp = 91;
        static final int TRANSACTION_finishInstrumentation = 36;
        static final int TRANSACTION_finishReceiver = 14;
        static final int TRANSACTION_forceStopPackage = 68;
        static final int TRANSACTION_getAllStackInfos = 133;
        static final int TRANSACTION_getConfiguration = 37;
        static final int TRANSACTION_getContentProvider = 20;
        static final int TRANSACTION_getContentProviderExternal = 108;
        static final int TRANSACTION_getCurrentUser = 113;
        static final int TRANSACTION_getFilteredTasks = 17;
        static final int TRANSACTION_getFocusedStackInfo = 137;
        static final int TRANSACTION_getForegroundServiceType = 63;
        static final int TRANSACTION_getHwInnerService = 198;
        static final int TRANSACTION_getIntentForIntentSender = 128;
        static final int TRANSACTION_getIntentSender = 51;
        static final int TRANSACTION_getLaunchedFromPackage = 129;
        static final int TRANSACTION_getLaunchedFromUid = 114;
        static final int TRANSACTION_getLifeMonitor = 196;
        static final int TRANSACTION_getLockTaskModeState = 158;
        static final int TRANSACTION_getMemoryInfo = 65;
        static final int TRANSACTION_getMemoryTrimLevel = 177;
        static final int TRANSACTION_getMyMemoryState = 111;
        static final int TRANSACTION_getPackageForIntentSender = 53;
        static final int TRANSACTION_getPackageProcessState = 164;
        static final int TRANSACTION_getProcessLimit = 41;
        static final int TRANSACTION_getProcessMemoryInfo = 85;
        static final int TRANSACTION_getProcessPss = 105;
        static final int TRANSACTION_getProcessesInErrorState = 66;
        static final int TRANSACTION_getProviderMimeType = 95;
        static final int TRANSACTION_getRecentTasks = 49;
        static final int TRANSACTION_getRunningAppProcesses = 71;
        static final int TRANSACTION_getRunningExternalApplications = 90;
        static final int TRANSACTION_getRunningServiceControlPanel = 23;
        static final int TRANSACTION_getRunningUserIds = 123;
        static final int TRANSACTION_getServices = 70;
        static final int TRANSACTION_getTagForIntentSender = 143;
        static final int TRANSACTION_getTaskBounds = 141;
        static final int TRANSACTION_getTaskForActivity = 19;
        static final int TRANSACTION_getTaskSnapshot = 188;
        static final int TRANSACTION_getTasks = 16;
        static final int TRANSACTION_getUidForIntentSender = 80;
        static final int TRANSACTION_getUidProcessState = 5;
        static final int TRANSACTION_grantUriPermission = 44;
        static final int TRANSACTION_handleApplicationCrash = 6;
        static final int TRANSACTION_handleApplicationStrictModeViolation = 92;
        static final int TRANSACTION_handleApplicationWtf = 87;
        static final int TRANSACTION_handleIncomingUser = 81;
        static final int TRANSACTION_hang = 132;
        static final int TRANSACTION_isAppStartModeDisabled = 171;
        static final int TRANSACTION_isBackgroundRestricted = 183;
        static final int TRANSACTION_isInLockTaskMode = 145;
        static final int TRANSACTION_isIntentSenderABroadcast = 118;
        static final int TRANSACTION_isIntentSenderAForegroundService = 117;
        static final int TRANSACTION_isIntentSenderAnActivity = 116;
        static final int TRANSACTION_isIntentSenderTargetedToPackage = 103;
        static final int TRANSACTION_isTopActivityImmersive = 93;
        static final int TRANSACTION_isTopOfTask = 150;
        static final int TRANSACTION_isUidActive = 4;
        static final int TRANSACTION_isUserAMonkey = 89;
        static final int TRANSACTION_isUserRunning = 97;
        static final int TRANSACTION_isVrModePackageEnabled = 178;
        static final int TRANSACTION_killAllBackgroundProcesses = 107;
        static final int TRANSACTION_killApplication = 83;
        static final int TRANSACTION_killApplicationProcess = 86;
        static final int TRANSACTION_killBackgroundProcesses = 88;
        static final int TRANSACTION_killPackageDependents = 173;
        static final int TRANSACTION_killPids = 69;
        static final int TRANSACTION_killProcessesBelowForeground = 112;
        static final int TRANSACTION_killUid = 130;
        static final int TRANSACTION_makePackageIdle = 176;
        static final int TRANSACTION_moveActivityTaskToBack = 64;
        static final int TRANSACTION_moveTaskToFront = 18;
        static final int TRANSACTION_moveTaskToStack = 134;
        static final int TRANSACTION_moveTopActivityToPinnedStack = 170;
        static final int TRANSACTION_noteAlarmFinish = 163;
        static final int TRANSACTION_noteAlarmStart = 162;
        static final int TRANSACTION_noteWakeupAlarm = 57;
        static final int TRANSACTION_notifyCleartextNetwork = 155;
        static final int TRANSACTION_notifyLockedProfile = 179;
        static final int TRANSACTION_openContentUri = 1;
        static final int TRANSACTION_peekService = 72;
        static final int TRANSACTION_performIdleMaintenance = 139;
        static final int TRANSACTION_positionTaskInStack = 168;
        static final int TRANSACTION_profileControl = 73;
        static final int TRANSACTION_publishContentProviders = 21;
        static final int TRANSACTION_publishService = 30;
        static final int TRANSACTION_refContentProvider = 22;
        static final int TRANSACTION_registerIntentSenderCancelListener = 54;
        static final int TRANSACTION_registerProcessObserver = 101;
        static final int TRANSACTION_registerReceiver = 10;
        static final int TRANSACTION_registerTaskStackListener = 153;
        static final int TRANSACTION_registerUidObserver = 2;
        static final int TRANSACTION_registerUserSwitchObserver = 121;
        static final int TRANSACTION_removeContentProvider = 58;
        static final int TRANSACTION_removeContentProviderExternal = 109;
        static final int TRANSACTION_removeContentProviderExternalAsUser = 110;
        static final int TRANSACTION_removeStack = 175;
        static final int TRANSACTION_removeTask = 100;
        static final int TRANSACTION_requestBugReport = 125;
        static final int TRANSACTION_requestSystemServerHeapDump = 124;
        static final int TRANSACTION_requestTelephonyBugReport = 126;
        static final int TRANSACTION_requestWifiBugReport = 127;
        static final int TRANSACTION_resizeDockedStack = 174;
        static final int TRANSACTION_resizeStack = 135;
        static final int TRANSACTION_resizeTask = 157;
        static final int TRANSACTION_restart = 138;
        static final int TRANSACTION_restartUserInBackground = 186;
        static final int TRANSACTION_resumeAppSwitches = 76;
        static final int TRANSACTION_revokeUriPermission = 45;
        static final int TRANSACTION_scheduleApplicationInfoChanged = 189;
        static final int TRANSACTION_sendIdleJobTrigger = 181;
        static final int TRANSACTION_sendIntentSender = 182;
        static final int TRANSACTION_serviceDoneExecuting = 50;
        static final int TRANSACTION_setActivityController = 46;
        static final int TRANSACTION_setAgentApp = 32;
        static final int TRANSACTION_setAlwaysFinish = 33;
        static final int TRANSACTION_setDebugApp = 31;
        static final int TRANSACTION_setDumpHeapDebugLimit = 159;
        static final int TRANSACTION_setFocusedStack = 136;
        static final int TRANSACTION_setHasTopUi = 185;
        static final int TRANSACTION_setHmThreadToRtg = 200;
        static final int TRANSACTION_setPackageScreenCompatMode = 98;
        static final int TRANSACTION_setPersistentVrThread = 190;
        static final int TRANSACTION_setProcessImportant = 61;
        static final int TRANSACTION_setProcessLimit = 40;
        static final int TRANSACTION_setProcessMemoryTrimLevel = 142;
        static final int TRANSACTION_setRenderThread = 184;
        static final int TRANSACTION_setRequestedOrientation = 59;
        static final int TRANSACTION_setServiceForeground = 62;
        static final int TRANSACTION_setTaskResizeable = 156;
        static final int TRANSACTION_setUserIsMonkey = 131;
        static final int TRANSACTION_showBootMessage = 106;
        static final int TRANSACTION_showWaitingForDebugger = 47;
        static final int TRANSACTION_shutdown = 74;
        static final int TRANSACTION_signalPersistentProcesses = 48;
        static final int TRANSACTION_startActivity = 7;
        static final int TRANSACTION_startActivityAsUser = 119;
        static final int TRANSACTION_startActivityFromRecents = 148;
        static final int TRANSACTION_startBinderTracking = 166;
        static final int TRANSACTION_startConfirmDeviceCredentialIntent = 180;
        static final int TRANSACTION_startDelegateShellPermissionIdentity = 194;
        static final int TRANSACTION_startInstrumentation = 34;
        static final int TRANSACTION_startRecentsActivity = 146;
        static final int TRANSACTION_startService = 24;
        static final int TRANSACTION_startSystemLockTaskMode = 149;
        static final int TRANSACTION_startUserInBackground = 144;
        static final int TRANSACTION_startUserInBackgroundWithListener = 193;
        static final int TRANSACTION_startUserInForegroundWithListener = 197;
        static final int TRANSACTION_stopAppSwitches = 75;
        static final int TRANSACTION_stopBinderTrackingAndDump = 167;
        static final int TRANSACTION_stopDelegateShellPermissionIdentity = 195;
        static final int TRANSACTION_stopService = 25;
        static final int TRANSACTION_stopServiceToken = 39;
        static final int TRANSACTION_stopUser = 120;
        static final int TRANSACTION_suppressResizeConfigChanges = 169;
        static final int TRANSACTION_switchUser = 99;
        static final int TRANSACTION_unbindBackupAgent = 79;
        static final int TRANSACTION_unbindFinished = 60;
        static final int TRANSACTION_unbindService = 29;
        static final int TRANSACTION_unbroadcastIntent = 13;
        static final int TRANSACTION_unhandledBack = 8;
        static final int TRANSACTION_unlockUser = 172;
        static final int TRANSACTION_unregisterIntentSenderCancelListener = 55;
        static final int TRANSACTION_unregisterProcessObserver = 102;
        static final int TRANSACTION_unregisterReceiver = 11;
        static final int TRANSACTION_unregisterTaskStackListener = 154;
        static final int TRANSACTION_unregisterUidObserver = 3;
        static final int TRANSACTION_unregisterUserSwitchObserver = 122;
        static final int TRANSACTION_unstableProviderDied = 115;
        static final int TRANSACTION_updateConfiguration = 38;
        static final int TRANSACTION_updateDeviceOwner = 165;
        static final int TRANSACTION_updateLockTaskPackages = 161;
        static final int TRANSACTION_updatePersistentConfiguration = 104;
        static final int TRANSACTION_updateServiceGroup = 28;
        static final int TRANSACTION_waitForNetworkStateUpdate = 191;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IActivityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IActivityManager)) {
                return new Proxy(obj);
            }
            return (IActivityManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "openContentUri";
                case 2:
                    return "registerUidObserver";
                case 3:
                    return "unregisterUidObserver";
                case 4:
                    return "isUidActive";
                case 5:
                    return "getUidProcessState";
                case 6:
                    return "handleApplicationCrash";
                case 7:
                    return "startActivity";
                case 8:
                    return "unhandledBack";
                case 9:
                    return "finishActivity";
                case 10:
                    return "registerReceiver";
                case 11:
                    return "unregisterReceiver";
                case 12:
                    return "broadcastIntent";
                case 13:
                    return "unbroadcastIntent";
                case 14:
                    return "finishReceiver";
                case 15:
                    return "attachApplication";
                case 16:
                    return "getTasks";
                case 17:
                    return "getFilteredTasks";
                case 18:
                    return "moveTaskToFront";
                case 19:
                    return "getTaskForActivity";
                case 20:
                    return "getContentProvider";
                case 21:
                    return "publishContentProviders";
                case 22:
                    return "refContentProvider";
                case 23:
                    return "getRunningServiceControlPanel";
                case 24:
                    return "startService";
                case 25:
                    return "stopService";
                case 26:
                    return "bindService";
                case 27:
                    return "bindIsolatedService";
                case 28:
                    return "updateServiceGroup";
                case 29:
                    return "unbindService";
                case 30:
                    return "publishService";
                case 31:
                    return "setDebugApp";
                case 32:
                    return "setAgentApp";
                case 33:
                    return "setAlwaysFinish";
                case 34:
                    return "startInstrumentation";
                case 35:
                    return "addInstrumentationResults";
                case 36:
                    return "finishInstrumentation";
                case 37:
                    return "getConfiguration";
                case 38:
                    return "updateConfiguration";
                case 39:
                    return "stopServiceToken";
                case 40:
                    return "setProcessLimit";
                case 41:
                    return "getProcessLimit";
                case 42:
                    return "checkPermission";
                case 43:
                    return "checkUriPermission";
                case 44:
                    return "grantUriPermission";
                case 45:
                    return "revokeUriPermission";
                case 46:
                    return "setActivityController";
                case 47:
                    return "showWaitingForDebugger";
                case 48:
                    return "signalPersistentProcesses";
                case 49:
                    return "getRecentTasks";
                case 50:
                    return "serviceDoneExecuting";
                case 51:
                    return "getIntentSender";
                case 52:
                    return "cancelIntentSender";
                case 53:
                    return "getPackageForIntentSender";
                case 54:
                    return "registerIntentSenderCancelListener";
                case 55:
                    return "unregisterIntentSenderCancelListener";
                case 56:
                    return "enterSafeMode";
                case 57:
                    return "noteWakeupAlarm";
                case 58:
                    return "removeContentProvider";
                case 59:
                    return "setRequestedOrientation";
                case 60:
                    return "unbindFinished";
                case 61:
                    return "setProcessImportant";
                case 62:
                    return "setServiceForeground";
                case 63:
                    return "getForegroundServiceType";
                case 64:
                    return "moveActivityTaskToBack";
                case 65:
                    return "getMemoryInfo";
                case 66:
                    return "getProcessesInErrorState";
                case 67:
                    return "clearApplicationUserData";
                case 68:
                    return "forceStopPackage";
                case 69:
                    return "killPids";
                case 70:
                    return "getServices";
                case 71:
                    return "getRunningAppProcesses";
                case 72:
                    return "peekService";
                case 73:
                    return "profileControl";
                case 74:
                    return "shutdown";
                case 75:
                    return "stopAppSwitches";
                case 76:
                    return "resumeAppSwitches";
                case 77:
                    return "bindBackupAgent";
                case 78:
                    return "backupAgentCreated";
                case 79:
                    return "unbindBackupAgent";
                case 80:
                    return "getUidForIntentSender";
                case 81:
                    return "handleIncomingUser";
                case 82:
                    return "addPackageDependency";
                case 83:
                    return "killApplication";
                case 84:
                    return "closeSystemDialogs";
                case 85:
                    return "getProcessMemoryInfo";
                case 86:
                    return "killApplicationProcess";
                case 87:
                    return "handleApplicationWtf";
                case 88:
                    return "killBackgroundProcesses";
                case 89:
                    return "isUserAMonkey";
                case 90:
                    return "getRunningExternalApplications";
                case 91:
                    return "finishHeavyWeightApp";
                case 92:
                    return "handleApplicationStrictModeViolation";
                case 93:
                    return "isTopActivityImmersive";
                case 94:
                    return "crashApplication";
                case 95:
                    return "getProviderMimeType";
                case 96:
                    return "dumpHeap";
                case 97:
                    return "isUserRunning";
                case 98:
                    return "setPackageScreenCompatMode";
                case 99:
                    return "switchUser";
                case 100:
                    return "removeTask";
                case 101:
                    return "registerProcessObserver";
                case 102:
                    return "unregisterProcessObserver";
                case 103:
                    return "isIntentSenderTargetedToPackage";
                case 104:
                    return "updatePersistentConfiguration";
                case 105:
                    return "getProcessPss";
                case 106:
                    return "showBootMessage";
                case 107:
                    return "killAllBackgroundProcesses";
                case 108:
                    return "getContentProviderExternal";
                case 109:
                    return "removeContentProviderExternal";
                case 110:
                    return "removeContentProviderExternalAsUser";
                case 111:
                    return "getMyMemoryState";
                case 112:
                    return "killProcessesBelowForeground";
                case 113:
                    return "getCurrentUser";
                case 114:
                    return "getLaunchedFromUid";
                case 115:
                    return "unstableProviderDied";
                case 116:
                    return "isIntentSenderAnActivity";
                case 117:
                    return "isIntentSenderAForegroundService";
                case 118:
                    return "isIntentSenderABroadcast";
                case 119:
                    return "startActivityAsUser";
                case 120:
                    return "stopUser";
                case 121:
                    return "registerUserSwitchObserver";
                case 122:
                    return "unregisterUserSwitchObserver";
                case 123:
                    return "getRunningUserIds";
                case 124:
                    return "requestSystemServerHeapDump";
                case 125:
                    return "requestBugReport";
                case 126:
                    return "requestTelephonyBugReport";
                case 127:
                    return "requestWifiBugReport";
                case 128:
                    return "getIntentForIntentSender";
                case 129:
                    return "getLaunchedFromPackage";
                case 130:
                    return "killUid";
                case 131:
                    return "setUserIsMonkey";
                case 132:
                    return "hang";
                case 133:
                    return "getAllStackInfos";
                case 134:
                    return "moveTaskToStack";
                case 135:
                    return "resizeStack";
                case 136:
                    return "setFocusedStack";
                case 137:
                    return "getFocusedStackInfo";
                case 138:
                    return "restart";
                case 139:
                    return "performIdleMaintenance";
                case 140:
                    return "appNotRespondingViaProvider";
                case 141:
                    return "getTaskBounds";
                case 142:
                    return "setProcessMemoryTrimLevel";
                case 143:
                    return "getTagForIntentSender";
                case 144:
                    return "startUserInBackground";
                case 145:
                    return "isInLockTaskMode";
                case 146:
                    return "startRecentsActivity";
                case 147:
                    return "cancelRecentsAnimation";
                case 148:
                    return "startActivityFromRecents";
                case 149:
                    return "startSystemLockTaskMode";
                case 150:
                    return "isTopOfTask";
                case 151:
                    return "bootAnimationComplete";
                case 152:
                    return "checkPermissionWithToken";
                case 153:
                    return "registerTaskStackListener";
                case 154:
                    return "unregisterTaskStackListener";
                case 155:
                    return "notifyCleartextNetwork";
                case 156:
                    return "setTaskResizeable";
                case 157:
                    return "resizeTask";
                case 158:
                    return "getLockTaskModeState";
                case 159:
                    return "setDumpHeapDebugLimit";
                case 160:
                    return "dumpHeapFinished";
                case 161:
                    return "updateLockTaskPackages";
                case 162:
                    return "noteAlarmStart";
                case 163:
                    return "noteAlarmFinish";
                case 164:
                    return "getPackageProcessState";
                case 165:
                    return "updateDeviceOwner";
                case 166:
                    return "startBinderTracking";
                case 167:
                    return "stopBinderTrackingAndDump";
                case 168:
                    return "positionTaskInStack";
                case 169:
                    return "suppressResizeConfigChanges";
                case 170:
                    return "moveTopActivityToPinnedStack";
                case 171:
                    return "isAppStartModeDisabled";
                case 172:
                    return "unlockUser";
                case 173:
                    return "killPackageDependents";
                case 174:
                    return "resizeDockedStack";
                case 175:
                    return "removeStack";
                case 176:
                    return "makePackageIdle";
                case 177:
                    return "getMemoryTrimLevel";
                case 178:
                    return "isVrModePackageEnabled";
                case 179:
                    return "notifyLockedProfile";
                case 180:
                    return "startConfirmDeviceCredentialIntent";
                case 181:
                    return "sendIdleJobTrigger";
                case 182:
                    return "sendIntentSender";
                case 183:
                    return "isBackgroundRestricted";
                case 184:
                    return "setRenderThread";
                case 185:
                    return "setHasTopUi";
                case 186:
                    return "restartUserInBackground";
                case 187:
                    return "cancelTaskWindowTransition";
                case 188:
                    return "getTaskSnapshot";
                case 189:
                    return "scheduleApplicationInfoChanged";
                case 190:
                    return "setPersistentVrThread";
                case 191:
                    return "waitForNetworkStateUpdate";
                case 192:
                    return "backgroundWhitelistUid";
                case 193:
                    return "startUserInBackgroundWithListener";
                case 194:
                    return "startDelegateShellPermissionIdentity";
                case 195:
                    return "stopDelegateShellPermissionIdentity";
                case 196:
                    return "getLifeMonitor";
                case 197:
                    return "startUserInForegroundWithListener";
                case 198:
                    return "getHwInnerService";
                case 199:
                    return "finishBindApplication";
                case 200:
                    return "setHmThreadToRtg";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ApplicationErrorReport.ParcelableCrashInfo _arg1;
            Intent _arg2;
            ProfilerInfo _arg8;
            Bundle _arg9;
            Intent _arg22;
            IntentFilter _arg3;
            Intent _arg12;
            Bundle _arg6;
            Bundle _arg92;
            Intent _arg13;
            Bundle _arg32;
            Bundle _arg4;
            ComponentName _arg0;
            Intent _arg14;
            Intent _arg15;
            Intent _arg23;
            Intent _arg24;
            Intent _arg16;
            ComponentName _arg02;
            Bundle _arg33;
            Bundle _arg17;
            Bundle _arg25;
            Configuration _arg03;
            ComponentName _arg04;
            Uri _arg05;
            Uri _arg26;
            Uri _arg27;
            Bundle _arg82;
            WorkSource _arg18;
            Intent _arg19;
            ComponentName _arg06;
            Notification _arg34;
            ComponentName _arg07;
            Intent _arg08;
            ProfilerInfo _arg35;
            ApplicationInfo _arg09;
            ApplicationErrorReport.ParcelableCrashInfo _arg36;
            StrictMode.ViolationInfo _arg28;
            Uri _arg010;
            ParcelFileDescriptor _arg62;
            RemoteCallback _arg7;
            Configuration _arg011;
            CharSequence _arg012;
            Intent _arg29;
            ProfilerInfo _arg83;
            Bundle _arg93;
            Rect _arg110;
            Intent _arg013;
            Bundle _arg111;
            Rect _arg112;
            WorkSource _arg113;
            WorkSource _arg114;
            ParcelFileDescriptor _arg014;
            Rect _arg115;
            Rect _arg015;
            Rect _arg116;
            Rect _arg210;
            Rect _arg37;
            Rect _arg42;
            ComponentName _arg016;
            Intent _arg017;
            Bundle _arg117;
            Intent _arg38;
            Bundle _arg72;
            if (code != 1598968902) {
                boolean _arg018 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result = openContentUri(data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                            return true;
                        }
                        reply.writeInt(0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        registerUidObserver(IUidObserver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterUidObserver(IUidObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUidActive = isUidActive(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isUidActive ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getUidProcessState(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg019 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = ApplicationErrorReport.ParcelableCrashInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        handleApplicationCrash(_arg019, _arg1);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg020 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg118 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        String _arg39 = data.readString();
                        IBinder _arg43 = data.readStrongBinder();
                        String _arg5 = data.readString();
                        int _arg63 = data.readInt();
                        int _arg73 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg8 = ProfilerInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg8 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg9 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg9 = null;
                        }
                        int _result3 = startActivity(_arg020, _arg118, _arg2, _arg39, _arg43, _arg5, _arg63, _arg73, _arg8, _arg9);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        unhandledBack();
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg021 = data.readStrongBinder();
                        int _arg119 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        boolean finishActivity = finishActivity(_arg021, _arg119, _arg22, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(finishActivity ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg022 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg120 = data.readString();
                        IIntentReceiver _arg211 = IIntentReceiver.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg3 = IntentFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        Intent _result4 = registerReceiver(_arg022, _arg120, _arg211, _arg3, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                            return true;
                        }
                        reply.writeInt(0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterReceiver(IIntentReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg023 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        String _arg212 = data.readString();
                        IIntentReceiver _arg310 = IIntentReceiver.Stub.asInterface(data.readStrongBinder());
                        int _arg44 = data.readInt();
                        String _arg52 = data.readString();
                        if (data.readInt() != 0) {
                            _arg6 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        String[] _arg74 = data.createStringArray();
                        int _arg84 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg92 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg92 = null;
                        }
                        boolean _arg10 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        int _result5 = broadcastIntent(_arg023, _arg12, _arg212, _arg310, _arg44, _arg52, _arg6, _arg74, _arg84, _arg92, _arg10, _arg018, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg024 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        unbroadcastIntent(_arg024, _arg13, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg025 = data.readStrongBinder();
                        int _arg121 = data.readInt();
                        String _arg213 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        finishReceiver(_arg025, _arg121, _arg213, _arg32, data.readInt() != 0, data.readInt());
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        attachApplication(IApplicationThread.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningTaskInfo> _result6 = getTasks(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result6);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningTaskInfo> _result7 = getFilteredTasks(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result7);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg026 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg122 = data.readString();
                        int _arg214 = data.readInt();
                        int _arg311 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg4 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        moveTaskToFront(_arg026, _arg122, _arg214, _arg311, _arg4);
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg027 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        int _result8 = getTaskForActivity(_arg027, _arg018);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        ContentProviderHolder _result9 = getContentProvider(IApplicationThread.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        publishContentProviders(IApplicationThread.Stub.asInterface(data.readStrongBinder()), data.createTypedArrayList(ContentProviderHolder.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean refContentProvider = refContentProvider(data.readStrongBinder(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(refContentProvider ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        PendingIntent _result10 = getRunningServiceControlPanel(_arg0);
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg028 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        ComponentName _result11 = startService(_arg028, _arg14, data.readString(), data.readInt() != 0, data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg029 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        int _result12 = stopService(_arg029, _arg15, data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg030 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        IBinder _arg123 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _result13 = bindService(_arg030, _arg123, _arg23, data.readString(), IServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg031 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        IBinder _arg124 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg24 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        int _result14 = bindIsolatedService(_arg031, _arg124, _arg24, data.readString(), IServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        updateServiceGroup(IServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unbindService = unbindService(IServiceConnection.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unbindService ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg032 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg16 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        publishService(_arg032, _arg16, data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg033 = data.readString();
                        boolean _arg125 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        setDebugApp(_arg033, _arg125, _arg018);
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        setAgentApp(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        setAlwaysFinish(_arg018);
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        String _arg126 = data.readString();
                        int _arg215 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg33 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        boolean startInstrumentation = startInstrumentation(_arg02, _arg126, _arg215, _arg33, IInstrumentationWatcher.Stub.asInterface(data.readStrongBinder()), IUiAutomationConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(startInstrumentation ? 1 : 0);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg034 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg17 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        addInstrumentationResults(_arg034, _arg17);
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg035 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        int _arg127 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg25 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        finishInstrumentation(_arg035, _arg127, _arg25);
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        Configuration _result15 = getConfiguration();
                        reply.writeNoException();
                        if (_result15 != null) {
                            reply.writeInt(1);
                            _result15.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Configuration.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean updateConfiguration = updateConfiguration(_arg03);
                        reply.writeNoException();
                        reply.writeInt(updateConfiguration ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean stopServiceToken = stopServiceToken(_arg04, data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(stopServiceToken ? 1 : 0);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        setProcessLimit(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getProcessLimit();
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = checkPermission(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        int _result18 = checkUriPermission(_arg05, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg036 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg128 = data.readString();
                        if (data.readInt() != 0) {
                            _arg26 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        grantUriPermission(_arg036, _arg128, _arg26, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg037 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg129 = data.readString();
                        if (data.readInt() != 0) {
                            _arg27 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg27 = null;
                        }
                        revokeUriPermission(_arg037, _arg129, _arg27, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        IActivityController _arg038 = IActivityController.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        setActivityController(_arg038, _arg018);
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg039 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        showWaitingForDebugger(_arg039, _arg018);
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        signalPersistentProcesses(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result19 = getRecentTasks(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result19 != null) {
                            reply.writeInt(1);
                            _result19.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        serviceDoneExecuting(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg040 = data.readInt();
                        String _arg130 = data.readString();
                        IBinder _arg216 = data.readStrongBinder();
                        String _arg312 = data.readString();
                        int _arg45 = data.readInt();
                        Intent[] _arg53 = (Intent[]) data.createTypedArray(Intent.CREATOR);
                        String[] _arg64 = data.createStringArray();
                        int _arg75 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg82 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg82 = null;
                        }
                        IIntentSender _result20 = getIntentSender(_arg040, _arg130, _arg216, _arg312, _arg45, _arg53, _arg64, _arg75, _arg82, data.readInt());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result20 != null ? _result20.asBinder() : null);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        cancelIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getPackageForIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        registerIntentSenderCancelListener(IIntentSender.Stub.asInterface(data.readStrongBinder()), IResultReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterIntentSenderCancelListener(IIntentSender.Stub.asInterface(data.readStrongBinder()), IResultReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        enterSafeMode();
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        IIntentSender _arg041 = IIntentSender.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg18 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        noteWakeupAlarm(_arg041, _arg18, data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg042 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        removeContentProvider(_arg042, _arg018);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        setRequestedOrientation(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg043 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg19 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg19 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        unbindFinished(_arg043, _arg19, _arg018);
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg044 = data.readStrongBinder();
                        int _arg131 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        setProcessImportant(_arg044, _arg131, _arg018, data.readString());
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        IBinder _arg132 = data.readStrongBinder();
                        int _arg217 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg34 = Notification.CREATOR.createFromParcel(data);
                        } else {
                            _arg34 = null;
                        }
                        setServiceForeground(_arg06, _arg132, _arg217, _arg34, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        int _result22 = getForegroundServiceType(_arg07, data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg045 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        boolean moveActivityTaskToBack = moveActivityTaskToBack(_arg045, _arg018);
                        reply.writeNoException();
                        reply.writeInt(moveActivityTaskToBack ? 1 : 0);
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityManager.MemoryInfo _arg046 = new ActivityManager.MemoryInfo();
                        getMemoryInfo(_arg046);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg046.writeToParcel(reply, 1);
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.ProcessErrorStateInfo> _result23 = getProcessesInErrorState();
                        reply.writeNoException();
                        reply.writeTypedList(_result23);
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg047 = data.readString();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        boolean clearApplicationUserData = clearApplicationUserData(_arg047, _arg018, IPackageDataObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(clearApplicationUserData ? 1 : 0);
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        forceStopPackage(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _arg048 = data.createIntArray();
                        String _arg133 = data.readString();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        boolean killPids = killPids(_arg048, _arg133, _arg018);
                        reply.writeNoException();
                        reply.writeInt(killPids ? 1 : 0);
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningServiceInfo> _result24 = getServices(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result24);
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningAppProcessInfo> _result25 = getRunningAppProcesses();
                        reply.writeNoException();
                        reply.writeTypedList(_result25);
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        IBinder _result26 = peekService(_arg08, data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result26);
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg049 = data.readString();
                        int _arg134 = data.readInt();
                        boolean _arg218 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg35 = ProfilerInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg35 = null;
                        }
                        boolean profileControl = profileControl(_arg049, _arg134, _arg218, _arg35, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(profileControl ? 1 : 0);
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shutdown = shutdown(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(shutdown ? 1 : 0);
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        stopAppSwitches();
                        reply.writeNoException();
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        resumeAppSwitches();
                        reply.writeNoException();
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        boolean bindBackupAgent = bindBackupAgent(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(bindBackupAgent ? 1 : 0);
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        backupAgentCreated(data.readString(), data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = ApplicationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        unbindBackupAgent(_arg09);
                        reply.writeNoException();
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = getUidForIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = handleIncomingUser(data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        addPackageDependency(data.readString());
                        reply.writeNoException();
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        killApplication(data.readString(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        closeSystemDialogs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        Debug.MemoryInfo[] _result29 = getProcessMemoryInfo(data.createIntArray());
                        reply.writeNoException();
                        reply.writeTypedArray(_result29, 1);
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        killApplicationProcess(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg050 = data.readStrongBinder();
                        String _arg135 = data.readString();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg36 = ApplicationErrorReport.ParcelableCrashInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg36 = null;
                        }
                        boolean handleApplicationWtf = handleApplicationWtf(_arg050, _arg135, _arg018, _arg36);
                        reply.writeNoException();
                        reply.writeInt(handleApplicationWtf ? 1 : 0);
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        killBackgroundProcesses(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUserAMonkey = isUserAMonkey();
                        reply.writeNoException();
                        reply.writeInt(isUserAMonkey ? 1 : 0);
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        List<ApplicationInfo> _result30 = getRunningExternalApplications();
                        reply.writeNoException();
                        reply.writeTypedList(_result30);
                        return true;
                    case 91:
                        data.enforceInterface(DESCRIPTOR);
                        finishHeavyWeightApp();
                        reply.writeNoException();
                        return true;
                    case 92:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg051 = data.readStrongBinder();
                        int _arg136 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg28 = StrictMode.ViolationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg28 = null;
                        }
                        handleApplicationStrictModeViolation(_arg051, _arg136, _arg28);
                        reply.writeNoException();
                        return true;
                    case 93:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTopActivityImmersive = isTopActivityImmersive();
                        reply.writeNoException();
                        reply.writeInt(isTopActivityImmersive ? 1 : 0);
                        return true;
                    case 94:
                        data.enforceInterface(DESCRIPTOR);
                        crashApplication(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 95:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        String _result31 = getProviderMimeType(_arg010, data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result31);
                        return true;
                    case 96:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg052 = data.readString();
                        int _arg137 = data.readInt();
                        boolean _arg219 = data.readInt() != 0;
                        boolean _arg313 = data.readInt() != 0;
                        boolean _arg46 = data.readInt() != 0;
                        String _arg54 = data.readString();
                        if (data.readInt() != 0) {
                            _arg62 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg62 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg7 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg7 = null;
                        }
                        boolean dumpHeap = dumpHeap(_arg052, _arg137, _arg219, _arg313, _arg46, _arg54, _arg62, _arg7);
                        reply.writeNoException();
                        reply.writeInt(dumpHeap ? 1 : 0);
                        return true;
                    case 97:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUserRunning = isUserRunning(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUserRunning ? 1 : 0);
                        return true;
                    case 98:
                        data.enforceInterface(DESCRIPTOR);
                        setPackageScreenCompatMode(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 99:
                        data.enforceInterface(DESCRIPTOR);
                        boolean switchUser = switchUser(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(switchUser ? 1 : 0);
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeTask = removeTask(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeTask ? 1 : 0);
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        registerProcessObserver(IProcessObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterProcessObserver(IProcessObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIntentSenderTargetedToPackage = isIntentSenderTargetedToPackage(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(isIntentSenderTargetedToPackage ? 1 : 0);
                        return true;
                    case 104:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = Configuration.CREATOR.createFromParcel(data);
                        } else {
                            _arg011 = null;
                        }
                        updatePersistentConfiguration(_arg011);
                        reply.writeNoException();
                        return true;
                    case 105:
                        data.enforceInterface(DESCRIPTOR);
                        long[] _result32 = getProcessPss(data.createIntArray());
                        reply.writeNoException();
                        reply.writeLongArray(_result32);
                        return true;
                    case 106:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg012 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        showBootMessage(_arg012, _arg018);
                        reply.writeNoException();
                        return true;
                    case 107:
                        data.enforceInterface(DESCRIPTOR);
                        killAllBackgroundProcesses();
                        reply.writeNoException();
                        return true;
                    case 108:
                        data.enforceInterface(DESCRIPTOR);
                        ContentProviderHolder _result33 = getContentProviderExternal(data.readString(), data.readInt(), data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        if (_result33 != null) {
                            reply.writeInt(1);
                            _result33.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 109:
                        data.enforceInterface(DESCRIPTOR);
                        removeContentProviderExternal(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 110:
                        data.enforceInterface(DESCRIPTOR);
                        removeContentProviderExternalAsUser(data.readString(), data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 111:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityManager.RunningAppProcessInfo _arg053 = new ActivityManager.RunningAppProcessInfo();
                        getMyMemoryState(_arg053);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg053.writeToParcel(reply, 1);
                        return true;
                    case 112:
                        data.enforceInterface(DESCRIPTOR);
                        boolean killProcessesBelowForeground = killProcessesBelowForeground(data.readString());
                        reply.writeNoException();
                        reply.writeInt(killProcessesBelowForeground ? 1 : 0);
                        return true;
                    case 113:
                        data.enforceInterface(DESCRIPTOR);
                        UserInfo _result34 = getCurrentUser();
                        reply.writeNoException();
                        if (_result34 != null) {
                            reply.writeInt(1);
                            _result34.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 114:
                        data.enforceInterface(DESCRIPTOR);
                        int _result35 = getLaunchedFromUid(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case 115:
                        data.enforceInterface(DESCRIPTOR);
                        unstableProviderDied(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 116:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIntentSenderAnActivity = isIntentSenderAnActivity(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(isIntentSenderAnActivity ? 1 : 0);
                        return true;
                    case 117:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIntentSenderAForegroundService = isIntentSenderAForegroundService(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(isIntentSenderAForegroundService ? 1 : 0);
                        return true;
                    case 118:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIntentSenderABroadcast = isIntentSenderABroadcast(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(isIntentSenderABroadcast ? 1 : 0);
                        return true;
                    case 119:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg054 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg138 = data.readString();
                        if (data.readInt() != 0) {
                            _arg29 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg29 = null;
                        }
                        String _arg314 = data.readString();
                        IBinder _arg47 = data.readStrongBinder();
                        String _arg55 = data.readString();
                        int _arg65 = data.readInt();
                        int _arg76 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg83 = ProfilerInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg83 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg93 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg93 = null;
                        }
                        int _result36 = startActivityAsUser(_arg054, _arg138, _arg29, _arg314, _arg47, _arg55, _arg65, _arg76, _arg83, _arg93, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result36);
                        return true;
                    case 120:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg055 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        int _result37 = stopUser(_arg055, _arg018, IStopUserCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result37);
                        return true;
                    case 121:
                        data.enforceInterface(DESCRIPTOR);
                        registerUserSwitchObserver(IUserSwitchObserver.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 122:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterUserSwitchObserver(IUserSwitchObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 123:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result38 = getRunningUserIds();
                        reply.writeNoException();
                        reply.writeIntArray(_result38);
                        return true;
                    case 124:
                        data.enforceInterface(DESCRIPTOR);
                        requestSystemServerHeapDump();
                        reply.writeNoException();
                        return true;
                    case 125:
                        data.enforceInterface(DESCRIPTOR);
                        requestBugReport(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 126:
                        data.enforceInterface(DESCRIPTOR);
                        requestTelephonyBugReport(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 127:
                        data.enforceInterface(DESCRIPTOR);
                        requestWifiBugReport(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 128:
                        data.enforceInterface(DESCRIPTOR);
                        Intent _result39 = getIntentForIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result39 != null) {
                            reply.writeInt(1);
                            _result39.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 129:
                        data.enforceInterface(DESCRIPTOR);
                        String _result40 = getLaunchedFromPackage(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeString(_result40);
                        return true;
                    case 130:
                        data.enforceInterface(DESCRIPTOR);
                        killUid(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 131:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        setUserIsMonkey(_arg018);
                        reply.writeNoException();
                        return true;
                    case 132:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg056 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        hang(_arg056, _arg018);
                        reply.writeNoException();
                        return true;
                    case 133:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.StackInfo> _result41 = getAllStackInfos();
                        reply.writeNoException();
                        reply.writeTypedList(_result41);
                        return true;
                    case 134:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg057 = data.readInt();
                        int _arg139 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        moveTaskToStack(_arg057, _arg139, _arg018);
                        reply.writeNoException();
                        return true;
                    case 135:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg058 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg110 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg110 = null;
                        }
                        resizeStack(_arg058, _arg110, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 136:
                        data.enforceInterface(DESCRIPTOR);
                        setFocusedStack(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 137:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityManager.StackInfo _result42 = getFocusedStackInfo();
                        reply.writeNoException();
                        if (_result42 != null) {
                            reply.writeInt(1);
                            _result42.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 138:
                        data.enforceInterface(DESCRIPTOR);
                        restart();
                        reply.writeNoException();
                        return true;
                    case 139:
                        data.enforceInterface(DESCRIPTOR);
                        performIdleMaintenance();
                        reply.writeNoException();
                        return true;
                    case 140:
                        data.enforceInterface(DESCRIPTOR);
                        appNotRespondingViaProvider(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 141:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result43 = getTaskBounds(data.readInt());
                        reply.writeNoException();
                        if (_result43 != null) {
                            reply.writeInt(1);
                            _result43.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 142:
                        data.enforceInterface(DESCRIPTOR);
                        boolean processMemoryTrimLevel = setProcessMemoryTrimLevel(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(processMemoryTrimLevel ? 1 : 0);
                        return true;
                    case 143:
                        data.enforceInterface(DESCRIPTOR);
                        String _result44 = getTagForIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result44);
                        return true;
                    case 144:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startUserInBackground = startUserInBackground(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(startUserInBackground ? 1 : 0);
                        return true;
                    case 145:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInLockTaskMode = isInLockTaskMode();
                        reply.writeNoException();
                        reply.writeInt(isInLockTaskMode ? 1 : 0);
                        return true;
                    case 146:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg013 = null;
                        }
                        startRecentsActivity(_arg013, IAssistDataReceiver.Stub.asInterface(data.readStrongBinder()), IRecentsAnimationRunner.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 147:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        cancelRecentsAnimation(_arg018);
                        reply.writeNoException();
                        return true;
                    case 148:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg059 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg111 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg111 = null;
                        }
                        int _result45 = startActivityFromRecents(_arg059, _arg111);
                        reply.writeNoException();
                        reply.writeInt(_result45);
                        return true;
                    case 149:
                        data.enforceInterface(DESCRIPTOR);
                        startSystemLockTaskMode(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 150:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTopOfTask = isTopOfTask(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isTopOfTask ? 1 : 0);
                        return true;
                    case 151:
                        data.enforceInterface(DESCRIPTOR);
                        bootAnimationComplete();
                        reply.writeNoException();
                        return true;
                    case 152:
                        data.enforceInterface(DESCRIPTOR);
                        int _result46 = checkPermissionWithToken(data.readString(), data.readInt(), data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result46);
                        return true;
                    case 153:
                        data.enforceInterface(DESCRIPTOR);
                        registerTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 154:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 155:
                        data.enforceInterface(DESCRIPTOR);
                        notifyCleartextNetwork(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 156:
                        data.enforceInterface(DESCRIPTOR);
                        setTaskResizeable(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 157:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg060 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg112 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg112 = null;
                        }
                        resizeTask(_arg060, _arg112, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 158:
                        data.enforceInterface(DESCRIPTOR);
                        int _result47 = getLockTaskModeState();
                        reply.writeNoException();
                        reply.writeInt(_result47);
                        return true;
                    case 159:
                        data.enforceInterface(DESCRIPTOR);
                        setDumpHeapDebugLimit(data.readString(), data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 160:
                        data.enforceInterface(DESCRIPTOR);
                        dumpHeapFinished(data.readString());
                        reply.writeNoException();
                        return true;
                    case 161:
                        data.enforceInterface(DESCRIPTOR);
                        updateLockTaskPackages(data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 162:
                        data.enforceInterface(DESCRIPTOR);
                        IIntentSender _arg061 = IIntentSender.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg113 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg113 = null;
                        }
                        noteAlarmStart(_arg061, _arg113, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 163:
                        data.enforceInterface(DESCRIPTOR);
                        IIntentSender _arg062 = IIntentSender.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg114 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg114 = null;
                        }
                        noteAlarmFinish(_arg062, _arg114, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 164:
                        data.enforceInterface(DESCRIPTOR);
                        int _result48 = getPackageProcessState(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result48);
                        return true;
                    case 165:
                        data.enforceInterface(DESCRIPTOR);
                        updateDeviceOwner(data.readString());
                        reply.writeNoException();
                        return true;
                    case 166:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startBinderTracking = startBinderTracking();
                        reply.writeNoException();
                        reply.writeInt(startBinderTracking ? 1 : 0);
                        return true;
                    case 167:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg014 = null;
                        }
                        boolean stopBinderTrackingAndDump = stopBinderTrackingAndDump(_arg014);
                        reply.writeNoException();
                        reply.writeInt(stopBinderTrackingAndDump ? 1 : 0);
                        return true;
                    case 168:
                        data.enforceInterface(DESCRIPTOR);
                        positionTaskInStack(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 169:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        suppressResizeConfigChanges(_arg018);
                        reply.writeNoException();
                        return true;
                    case 170:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg063 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg115 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg115 = null;
                        }
                        boolean moveTopActivityToPinnedStack = moveTopActivityToPinnedStack(_arg063, _arg115);
                        reply.writeNoException();
                        reply.writeInt(moveTopActivityToPinnedStack ? 1 : 0);
                        return true;
                    case 171:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAppStartModeDisabled = isAppStartModeDisabled(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isAppStartModeDisabled ? 1 : 0);
                        return true;
                    case 172:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unlockUser = unlockUser(data.readInt(), data.createByteArray(), data.createByteArray(), IProgressListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unlockUser ? 1 : 0);
                        return true;
                    case 173:
                        data.enforceInterface(DESCRIPTOR);
                        killPackageDependents(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 174:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg015 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg116 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg116 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg210 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg210 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg37 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg37 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg42 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        resizeDockedStack(_arg015, _arg116, _arg210, _arg37, _arg42);
                        reply.writeNoException();
                        return true;
                    case 175:
                        data.enforceInterface(DESCRIPTOR);
                        removeStack(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 176:
                        data.enforceInterface(DESCRIPTOR);
                        makePackageIdle(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 177:
                        data.enforceInterface(DESCRIPTOR);
                        int _result49 = getMemoryTrimLevel();
                        reply.writeNoException();
                        reply.writeInt(_result49);
                        return true;
                    case 178:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg016 = null;
                        }
                        boolean isVrModePackageEnabled = isVrModePackageEnabled(_arg016);
                        reply.writeNoException();
                        reply.writeInt(isVrModePackageEnabled ? 1 : 0);
                        return true;
                    case 179:
                        data.enforceInterface(DESCRIPTOR);
                        notifyLockedProfile(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 180:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg017 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg117 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg117 = null;
                        }
                        startConfirmDeviceCredentialIntent(_arg017, _arg117);
                        reply.writeNoException();
                        return true;
                    case 181:
                        data.enforceInterface(DESCRIPTOR);
                        sendIdleJobTrigger();
                        reply.writeNoException();
                        return true;
                    case 182:
                        data.enforceInterface(DESCRIPTOR);
                        IIntentSender _arg064 = IIntentSender.Stub.asInterface(data.readStrongBinder());
                        IBinder _arg140 = data.readStrongBinder();
                        int _arg220 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg38 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg38 = null;
                        }
                        String _arg48 = data.readString();
                        IIntentReceiver _arg56 = IIntentReceiver.Stub.asInterface(data.readStrongBinder());
                        String _arg66 = data.readString();
                        if (data.readInt() != 0) {
                            _arg72 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg72 = null;
                        }
                        int _result50 = sendIntentSender(_arg064, _arg140, _arg220, _arg38, _arg48, _arg56, _arg66, _arg72);
                        reply.writeNoException();
                        reply.writeInt(_result50);
                        return true;
                    case 183:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBackgroundRestricted = isBackgroundRestricted(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isBackgroundRestricted ? 1 : 0);
                        return true;
                    case 184:
                        data.enforceInterface(DESCRIPTOR);
                        setRenderThread(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 185:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        setHasTopUi(_arg018);
                        reply.writeNoException();
                        return true;
                    case 186:
                        data.enforceInterface(DESCRIPTOR);
                        int _result51 = restartUserInBackground(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result51);
                        return true;
                    case 187:
                        data.enforceInterface(DESCRIPTOR);
                        cancelTaskWindowTransition(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 188:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityManager.TaskSnapshot _result52 = getTaskSnapshot(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result52 != null) {
                            reply.writeInt(1);
                            _result52.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 189:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleApplicationInfoChanged(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 190:
                        data.enforceInterface(DESCRIPTOR);
                        setPersistentVrThread(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 191:
                        data.enforceInterface(DESCRIPTOR);
                        waitForNetworkStateUpdate(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 192:
                        data.enforceInterface(DESCRIPTOR);
                        backgroundWhitelistUid(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 193:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startUserInBackgroundWithListener = startUserInBackgroundWithListener(data.readInt(), IProgressListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(startUserInBackgroundWithListener ? 1 : 0);
                        return true;
                    case 194:
                        data.enforceInterface(DESCRIPTOR);
                        startDelegateShellPermissionIdentity(data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 195:
                        data.enforceInterface(DESCRIPTOR);
                        stopDelegateShellPermissionIdentity();
                        reply.writeNoException();
                        return true;
                    case 196:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result53 = getLifeMonitor();
                        reply.writeNoException();
                        if (_result53 != null) {
                            reply.writeInt(1);
                            _result53.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 197:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startUserInForegroundWithListener = startUserInForegroundWithListener(data.readInt(), IProgressListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(startUserInForegroundWithListener ? 1 : 0);
                        return true;
                    case 198:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result54 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result54);
                        return true;
                    case 199:
                        data.enforceInterface(DESCRIPTOR);
                        finishBindApplication(IApplicationThread.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 200:
                        data.enforceInterface(DESCRIPTOR);
                        setHmThreadToRtg(data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IActivityManager {
            public static IActivityManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.app.IActivityManager
            public ParcelFileDescriptor openContentUri(String uriString) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uriString);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openContentUri(uriString);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void registerUidObserver(IUidObserver observer, int which, int cutpoint, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(which);
                    _data.writeInt(cutpoint);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerUidObserver(observer, which, cutpoint, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void unregisterUidObserver(IUidObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterUidObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isUidActive(int uid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUidActive(uid, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int getUidProcessState(int uid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUidProcessState(uid, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void handleApplicationCrash(IBinder app, ApplicationErrorReport.ParcelableCrashInfo crashInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app);
                    if (crashInfo != null) {
                        _data.writeInt(1);
                        crashInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().handleApplicationCrash(app, crashInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
                Parcel _reply;
                Throwable th;
                IBinder iBinder;
                Parcel _data = Parcel.obtain();
                Parcel _reply2 = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (caller != null) {
                        try {
                            iBinder = caller.asBinder();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply = _reply2;
                        }
                    } else {
                        iBinder = null;
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(callingPackage);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeStrongBinder(resultTo);
                    _data.writeString(resultWho);
                    _data.writeInt(requestCode);
                    _data.writeInt(flags);
                    if (profilerInfo != null) {
                        _data.writeInt(1);
                        profilerInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, _reply2, 0) || Stub.getDefaultImpl() == null) {
                        _reply2.readException();
                        int _result = _reply2.readInt();
                        _reply2.recycle();
                        _data.recycle();
                        return _result;
                    }
                    _reply = _reply2;
                    try {
                        int startActivity = Stub.getDefaultImpl().startActivity(caller, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, flags, profilerInfo, options);
                        _reply.recycle();
                        _data.recycle();
                        return startActivity;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply = _reply2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void unhandledBack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unhandledBack();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean finishActivity(IBinder token, int code, Intent data, int finishTask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(code);
                    boolean _result = true;
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(finishTask);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().finishActivity(token, code, data, finishTask);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public Intent registerReceiver(IApplicationThread caller, String callerPackage, IIntentReceiver receiver, IntentFilter filter, String requiredPermission, int userId, int flags) throws RemoteException {
                Throwable th;
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeString(callerPackage);
                        if (receiver != null) {
                            iBinder = receiver.asBinder();
                        }
                        _data.writeStrongBinder(iBinder);
                        if (filter != null) {
                            _data.writeInt(1);
                            filter.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(requiredPermission);
                        try {
                            _data.writeInt(userId);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(flags);
                            if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = Intent.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            Intent registerReceiver = Stub.getDefaultImpl().registerReceiver(caller, callerPackage, receiver, filter, requiredPermission, userId, flags);
                            _reply.recycle();
                            _data.recycle();
                            return registerReceiver;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void unregisterReceiver(IIntentReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterReceiver(receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int broadcastIntent(IApplicationThread caller, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle map, String[] requiredPermissions, int appOp, Bundle options, boolean serialized, boolean sticky, int userId) throws RemoteException {
                Parcel _data;
                Parcel _reply;
                Throwable th;
                IBinder iBinder;
                Parcel _data2 = Parcel.obtain();
                Parcel _reply2 = Parcel.obtain();
                try {
                    _data2.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder2 = null;
                    if (caller != null) {
                        try {
                            iBinder = caller.asBinder();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply = _reply2;
                            _data = _data2;
                        }
                    } else {
                        iBinder = null;
                    }
                    _data2.writeStrongBinder(iBinder);
                    int i = 1;
                    if (intent != null) {
                        _data2.writeInt(1);
                        intent.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeString(resolvedType);
                    if (resultTo != null) {
                        iBinder2 = resultTo.asBinder();
                    }
                    _data2.writeStrongBinder(iBinder2);
                    _data2.writeInt(resultCode);
                    _data2.writeString(resultData);
                    if (map != null) {
                        _data2.writeInt(1);
                        map.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeStringArray(requiredPermissions);
                    _data2.writeInt(appOp);
                    if (options != null) {
                        _data2.writeInt(1);
                        options.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeInt(serialized ? 1 : 0);
                    if (!sticky) {
                        i = 0;
                    }
                    _data2.writeInt(i);
                    _data2.writeInt(userId);
                    if (this.mRemote.transact(12, _data2, _reply2, 0) || Stub.getDefaultImpl() == null) {
                        _reply2.readException();
                        int _result = _reply2.readInt();
                        _reply2.recycle();
                        _data2.recycle();
                        return _result;
                    }
                    _reply = _reply2;
                    _data = _data2;
                    try {
                        int broadcastIntent = Stub.getDefaultImpl().broadcastIntent(caller, intent, resolvedType, resultTo, resultCode, resultData, map, requiredPermissions, appOp, options, serialized, sticky, userId);
                        _reply.recycle();
                        _data.recycle();
                        return broadcastIntent;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply = _reply2;
                    _data = _data2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unbroadcastIntent(caller, intent, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void finishReceiver(IBinder who, int resultCode, String resultData, Bundle map, boolean abortBroadcast, int flags) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(who);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(resultCode);
                        try {
                            _data.writeString(resultData);
                            int i = 0;
                            if (map != null) {
                                _data.writeInt(1);
                                map.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (abortBroadcast) {
                                i = 1;
                            }
                            _data.writeInt(i);
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flags);
                        try {
                            if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().finishReceiver(who, resultCode, resultData, map, abortBroadcast, flags);
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void attachApplication(IApplicationThread app, long startSeq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app != null ? app.asBinder() : null);
                    _data.writeLong(startSeq);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().attachApplication(app, startSeq);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxNum);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTasks(maxNum);
                    }
                    _reply.readException();
                    List<ActivityManager.RunningTaskInfo> _result = _reply.createTypedArrayList(ActivityManager.RunningTaskInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public List<ActivityManager.RunningTaskInfo> getFilteredTasks(int maxNum, int ignoreActivityType, int ignoreWindowingMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxNum);
                    _data.writeInt(ignoreActivityType);
                    _data.writeInt(ignoreWindowingMode);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFilteredTasks(maxNum, ignoreActivityType, ignoreWindowingMode);
                    }
                    _reply.readException();
                    List<ActivityManager.RunningTaskInfo> _result = _reply.createTypedArrayList(ActivityManager.RunningTaskInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void moveTaskToFront(IApplicationThread caller, String callingPackage, int task, int flags, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    _data.writeInt(task);
                    _data.writeInt(flags);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveTaskToFront(caller, callingPackage, task, flags, options);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int getTaskForActivity(IBinder token, boolean onlyRoot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(onlyRoot ? 1 : 0);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskForActivity(token, onlyRoot);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public ContentProviderHolder getContentProvider(IApplicationThread caller, String callingPackage, String name, int userId, boolean stable) throws RemoteException {
                ContentProviderHolder _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    _data.writeString(name);
                    _data.writeInt(userId);
                    _data.writeInt(stable ? 1 : 0);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getContentProvider(caller, callingPackage, name, userId, stable);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ContentProviderHolder.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void publishContentProviders(IApplicationThread caller, List<ContentProviderHolder> providers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeTypedList(providers);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().publishContentProviders(caller, providers);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean refContentProvider(IBinder connection, int stableDelta, int unstableDelta) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection);
                    _data.writeInt(stableDelta);
                    _data.writeInt(unstableDelta);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().refContentProvider(connection, stableDelta, unstableDelta);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public PendingIntent getRunningServiceControlPanel(ComponentName service) throws RemoteException {
                PendingIntent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRunningServiceControlPanel(service);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PendingIntent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, boolean requireForeground, String callingPackage, int userId) throws RemoteException {
                Throwable th;
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    int i = 1;
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeString(resolvedType);
                        if (!requireForeground) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        try {
                            _data.writeString(callingPackage);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(userId);
                            try {
                                if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    if (_reply.readInt() != 0) {
                                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                                    } else {
                                        _result = null;
                                    }
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                }
                                ComponentName startService = Stub.getDefaultImpl().startService(caller, service, resolvedType, requireForeground, callingPackage, userId);
                                _reply.recycle();
                                _data.recycle();
                                return startService;
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopService(caller, service, resolvedType, userId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeStrongBinder(token);
                        if (service != null) {
                            _data.writeInt(1);
                            service.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(resolvedType);
                        if (connection != null) {
                            iBinder = connection.asBinder();
                        }
                        _data.writeStrongBinder(iBinder);
                        try {
                            _data.writeInt(flags);
                            _data.writeString(callingPackage);
                            _data.writeInt(userId);
                            if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int bindService = Stub.getDefaultImpl().bindService(caller, token, service, resolvedType, connection, flags, callingPackage, userId);
                            _reply.recycle();
                            _data.recycle();
                            return bindService;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public int bindIsolatedService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String instanceName, String callingPackage, int userId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeStrongBinder(token);
                        if (service != null) {
                            _data.writeInt(1);
                            service.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(resolvedType);
                        if (connection != null) {
                            iBinder = connection.asBinder();
                        }
                        _data.writeStrongBinder(iBinder);
                        _data.writeInt(flags);
                        _data.writeString(instanceName);
                        _data.writeString(callingPackage);
                        _data.writeInt(userId);
                        if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int bindIsolatedService = Stub.getDefaultImpl().bindIsolatedService(caller, token, service, resolvedType, connection, flags, instanceName, callingPackage, userId);
                        _reply.recycle();
                        _data.recycle();
                        return bindIsolatedService;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void updateServiceGroup(IServiceConnection connection, int group, int importance) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection != null ? connection.asBinder() : null);
                    _data.writeInt(group);
                    _data.writeInt(importance);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateServiceGroup(connection, group, importance);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean unbindService(IServiceConnection connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection != null ? connection.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unbindService(connection);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(service);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().publishService(token, intent, service);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setDebugApp(String packageName, boolean waitForDebugger, boolean persistent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    int i = 1;
                    _data.writeInt(waitForDebugger ? 1 : 0);
                    if (!persistent) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDebugApp(packageName, waitForDebugger, persistent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setAgentApp(String packageName, String agent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(agent);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAgentApp(packageName, agent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setAlwaysFinish(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAlwaysFinish(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean startInstrumentation(ComponentName className, String profileFile, int flags, Bundle arguments, IInstrumentationWatcher watcher, IUiAutomationConnection connection, int userId, String abiOverride) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeString(profileFile);
                        try {
                            _data.writeInt(flags);
                            if (arguments != null) {
                                _data.writeInt(1);
                                arguments.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            IBinder iBinder = null;
                            _data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
                            if (connection != null) {
                                iBinder = connection.asBinder();
                            }
                            _data.writeStrongBinder(iBinder);
                            _data.writeInt(userId);
                            _data.writeString(abiOverride);
                            if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean startInstrumentation = Stub.getDefaultImpl().startInstrumentation(className, profileFile, flags, arguments, watcher, connection, userId, abiOverride);
                            _reply.recycle();
                            _data.recycle();
                            return startInstrumentation;
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void addInstrumentationResults(IApplicationThread target, Bundle results) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(target != null ? target.asBinder() : null);
                    if (results != null) {
                        _data.writeInt(1);
                        results.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addInstrumentationResults(target, results);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void finishInstrumentation(IApplicationThread target, int resultCode, Bundle results) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(target != null ? target.asBinder() : null);
                    _data.writeInt(resultCode);
                    if (results != null) {
                        _data.writeInt(1);
                        results.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finishInstrumentation(target, resultCode, results);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public Configuration getConfiguration() throws RemoteException {
                Configuration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConfiguration();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Configuration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean updateConfiguration(Configuration values) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateConfiguration(values);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(token);
                    _data.writeInt(startId);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopServiceToken(className, token, startId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setProcessLimit(int max) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(max);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProcessLimit(max);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int getProcessLimit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProcessLimit();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int checkPermission(String permission, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPermission(permission, pid, uid);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int checkUriPermission(Uri uri, int pid, int uid, int mode, int userId, IBinder callerToken) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(mode);
                        try {
                            _data.writeInt(userId);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeStrongBinder(callerToken);
                            if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int checkUriPermission = Stub.getDefaultImpl().checkUriPermission(uri, pid, uid, mode, userId, callerToken);
                            _reply.recycle();
                            _data.recycle();
                            return checkUriPermission;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(targetPkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().grantUriPermission(caller, targetPkg, uri, mode, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void revokeUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(targetPkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().revokeUriPermission(caller, targetPkg, uri, mode, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setActivityController(IActivityController watcher, boolean imAMonkey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
                    _data.writeInt(imAMonkey ? 1 : 0);
                    if (this.mRemote.transact(46, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setActivityController(watcher, imAMonkey);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void showWaitingForDebugger(IApplicationThread who, boolean waiting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(who != null ? who.asBinder() : null);
                    _data.writeInt(waiting ? 1 : 0);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showWaitingForDebugger(who, waiting);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void signalPersistentProcesses(int signal) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(signal);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().signalPersistentProcesses(signal);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public ParceledListSlice getRecentTasks(int maxNum, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxNum);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRecentTasks(maxNum, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(type);
                    _data.writeInt(startId);
                    _data.writeInt(res);
                    if (this.mRemote.transact(50, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().serviceDoneExecuting(token, type, startId, res);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(type);
                        _data.writeString(packageName);
                        _data.writeStrongBinder(token);
                        _data.writeString(resultWho);
                        _data.writeInt(requestCode);
                        _data.writeTypedArray(intents, 0);
                        _data.writeStringArray(resolvedTypes);
                        _data.writeInt(flags);
                        if (options != null) {
                            _data.writeInt(1);
                            options.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeInt(userId);
                        if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            IIntentSender _result = IIntentSender.Stub.asInterface(_reply.readStrongBinder());
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        IIntentSender intentSender = Stub.getDefaultImpl().getIntentSender(type, packageName, token, resultWho, requestCode, intents, resolvedTypes, flags, options, userId);
                        _reply.recycle();
                        _data.recycle();
                        return intentSender;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void cancelIntentSender(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelIntentSender(sender);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public String getPackageForIntentSender(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageForIntentSender(sender);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void registerIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerIntentSenderCancelListener(sender, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void unregisterIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterIntentSenderCancelListener(sender, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void enterSafeMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enterSafeMode();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void noteWakeupAlarm(IIntentSender sender, WorkSource workSource, int sourceUid, String sourcePkg, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceUid);
                    _data.writeString(sourcePkg);
                    _data.writeString(tag);
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().noteWakeupAlarm(sender, workSource, sourceUid, sourcePkg, tag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void removeContentProvider(IBinder connection, boolean stable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection);
                    _data.writeInt(stable ? 1 : 0);
                    if (this.mRemote.transact(58, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeContentProvider(connection, stable);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setRequestedOrientation(IBinder token, int requestedOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(requestedOrientation);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRequestedOrientation(token, requestedOrientation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void unbindFinished(IBinder token, Intent service, boolean doRebind) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    int i = 1;
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!doRebind) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(60, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unbindFinished(token, service, doRebind);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setProcessImportant(IBinder token, int pid, boolean isForeground, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(pid);
                    _data.writeInt(isForeground ? 1 : 0);
                    _data.writeString(reason);
                    if (this.mRemote.transact(61, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProcessImportant(token, pid, isForeground, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, int flags, int foregroundServiceType) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeStrongBinder(token);
                        try {
                            _data.writeInt(id);
                            if (notification != null) {
                                _data.writeInt(1);
                                notification.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeInt(flags);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(foregroundServiceType);
                            if (this.mRemote.transact(62, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().setServiceForeground(className, token, id, notification, flags, foregroundServiceType);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public int getForegroundServiceType(ComponentName className, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(63, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getForegroundServiceType(className, token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = true;
                    _data.writeInt(nonRoot ? 1 : 0);
                    if (!this.mRemote.transact(64, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().moveActivityTaskToBack(token, nonRoot);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            outInfo.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getMemoryInfo(outInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(66, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProcessesInErrorState();
                    }
                    _reply.readException();
                    List<ActivityManager.ProcessErrorStateInfo> _result = _reply.createTypedArrayList(ActivityManager.ProcessErrorStateInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean clearApplicationUserData(String packageName, boolean keepState, IPackageDataObserver observer, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(keepState ? 1 : 0);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(67, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearApplicationUserData(packageName, keepState, observer, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void forceStopPackage(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(68, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forceStopPackage(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean killPids(int[] pids, String reason, boolean secure) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(pids);
                    _data.writeString(reason);
                    boolean _result = true;
                    _data.writeInt(secure ? 1 : 0);
                    if (!this.mRemote.transact(69, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().killPids(pids, reason, secure);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public List<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxNum);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(70, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServices(maxNum, flags);
                    }
                    _reply.readException();
                    List<ActivityManager.RunningServiceInfo> _result = _reply.createTypedArrayList(ActivityManager.RunningServiceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(71, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRunningAppProcesses();
                    }
                    _reply.readException();
                    List<ActivityManager.RunningAppProcessInfo> _result = _reply.createTypedArrayList(ActivityManager.RunningAppProcessInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public IBinder peekService(Intent service, String resolvedType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(72, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().peekService(service, resolvedType, callingPackage);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean profileControl(String process, int userId, boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
                Throwable th;
                boolean _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(process);
                        try {
                            _data.writeInt(userId);
                            _result = true;
                            _data.writeInt(start ? 1 : 0);
                            if (profilerInfo != null) {
                                _data.writeInt(1);
                                profilerInfo.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(profileType);
                        try {
                            if (this.mRemote.transact(73, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean profileControl = Stub.getDefaultImpl().profileControl(process, userId, start, profilerInfo, profileType);
                            _reply.recycle();
                            _data.recycle();
                            return profileControl;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public boolean shutdown(int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeout);
                    boolean _result = false;
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shutdown(timeout);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void stopAppSwitches() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(75, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopAppSwitches();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void resumeAppSwitches() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(76, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resumeAppSwitches();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean bindBackupAgent(String packageName, int backupRestoreMode, int targetUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(backupRestoreMode);
                    _data.writeInt(targetUserId);
                    boolean _result = false;
                    if (!this.mRemote.transact(77, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().bindBackupAgent(packageName, backupRestoreMode, targetUserId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void backupAgentCreated(String packageName, IBinder agent, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(agent);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().backupAgentCreated(packageName, agent, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void unbindBackupAgent(ApplicationInfo appInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (appInfo != null) {
                        _data.writeInt(1);
                        appInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(79, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unbindBackupAgent(appInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int getUidForIntentSender(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (!this.mRemote.transact(80, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUidForIntentSender(sender);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(callingPid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(callingUid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        int i = 1;
                        _data.writeInt(allowAll ? 1 : 0);
                        if (!requireFull) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        try {
                            _data.writeString(name);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(callerPackage);
                            if (this.mRemote.transact(81, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int handleIncomingUser = Stub.getDefaultImpl().handleIncomingUser(callingPid, callingUid, userId, allowAll, requireFull, name, callerPackage);
                            _reply.recycle();
                            _data.recycle();
                            return handleIncomingUser;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void addPackageDependency(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(82, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addPackageDependency(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void killApplication(String pkg, int appId, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    if (this.mRemote.transact(83, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killApplication(pkg, appId, userId, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    if (this.mRemote.transact(84, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeSystemDialogs(reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(pids);
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProcessMemoryInfo(pids);
                    }
                    _reply.readException();
                    Debug.MemoryInfo[] _result = (Debug.MemoryInfo[]) _reply.createTypedArray(Debug.MemoryInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void killApplicationProcess(String processName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killApplicationProcess(processName, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean handleApplicationWtf(IBinder app, String tag, boolean system, ApplicationErrorReport.ParcelableCrashInfo crashInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app);
                    _data.writeString(tag);
                    boolean _result = true;
                    _data.writeInt(system ? 1 : 0);
                    if (crashInfo != null) {
                        _data.writeInt(1);
                        crashInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(87, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleApplicationWtf(app, tag, system, crashInfo);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void killBackgroundProcesses(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(88, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killBackgroundProcesses(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isUserAMonkey() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(89, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUserAMonkey();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public List<ApplicationInfo> getRunningExternalApplications() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(90, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRunningExternalApplications();
                    }
                    _reply.readException();
                    List<ApplicationInfo> _result = _reply.createTypedArrayList(ApplicationInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void finishHeavyWeightApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(91, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finishHeavyWeightApp();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void handleApplicationStrictModeViolation(IBinder app, int penaltyMask, StrictMode.ViolationInfo crashInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app);
                    _data.writeInt(penaltyMask);
                    if (crashInfo != null) {
                        _data.writeInt(1);
                        crashInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(92, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().handleApplicationStrictModeViolation(app, penaltyMask, crashInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isTopActivityImmersive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(93, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTopActivityImmersive();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void crashApplication(int uid, int initialPid, String packageName, int userId, String message, boolean force) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(uid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(initialPid);
                        try {
                            _data.writeString(packageName);
                            try {
                                _data.writeInt(userId);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(message);
                        _data.writeInt(force ? 1 : 0);
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(94, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().crashApplication(uid, initialPid, packageName, userId, message, force);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public String getProviderMimeType(Uri uri, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(95, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProviderMimeType(uri, userId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean dumpHeap(String process, int userId, boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd, RemoteCallback finishCallback) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(process);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        boolean _result = true;
                        _data.writeInt(managed ? 1 : 0);
                        _data.writeInt(mallocInfo ? 1 : 0);
                        _data.writeInt(runGc ? 1 : 0);
                        _data.writeString(path);
                        if (fd != null) {
                            _data.writeInt(1);
                            fd.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (finishCallback != null) {
                            _data.writeInt(1);
                            finishCallback.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (this.mRemote.transact(96, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() == 0) {
                                _result = false;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        boolean dumpHeap = Stub.getDefaultImpl().dumpHeap(process, userId, managed, mallocInfo, runGc, path, fd, finishCallback);
                        _reply.recycle();
                        _data.recycle();
                        return dumpHeap;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public boolean isUserRunning(int userid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeInt(flags);
                    boolean _result = false;
                    if (!this.mRemote.transact(97, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUserRunning(userid, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setPackageScreenCompatMode(String packageName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(98, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPackageScreenCompatMode(packageName, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean switchUser(int userid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    boolean _result = false;
                    if (!this.mRemote.transact(99, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().switchUser(userid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean removeTask(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    boolean _result = false;
                    if (!this.mRemote.transact(100, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeTask(taskId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void registerProcessObserver(IProcessObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(101, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerProcessObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void unregisterProcessObserver(IProcessObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(102, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterProcessObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isIntentSenderTargetedToPackage(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(103, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isIntentSenderTargetedToPackage(sender);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void updatePersistentConfiguration(Configuration values) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(104, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updatePersistentConfiguration(values);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public long[] getProcessPss(int[] pids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(pids);
                    if (!this.mRemote.transact(105, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProcessPss(pids);
                    }
                    _reply.readException();
                    long[] _result = _reply.createLongArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void showBootMessage(CharSequence msg, boolean always) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (msg != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(msg, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!always) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(106, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showBootMessage(msg, always);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void killAllBackgroundProcesses() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(107, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killAllBackgroundProcesses();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token, String tag) throws RemoteException {
                ContentProviderHolder _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(token);
                    _data.writeString(tag);
                    if (!this.mRemote.transact(108, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getContentProviderExternal(name, userId, token, tag);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ContentProviderHolder.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void removeContentProviderExternal(String name, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(109, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeContentProviderExternal(name, token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void removeContentProviderExternalAsUser(String name, IBinder token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeStrongBinder(token);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(110, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeContentProviderExternalAsUser(name, token, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void getMyMemoryState(ActivityManager.RunningAppProcessInfo outInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(111, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            outInfo.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getMyMemoryState(outInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean killProcessesBelowForeground(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    boolean _result = false;
                    if (!this.mRemote.transact(112, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().killProcessesBelowForeground(reason);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public UserInfo getCurrentUser() throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(113, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentUser();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int getLaunchedFromUid(IBinder activityToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    if (!this.mRemote.transact(114, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLaunchedFromUid(activityToken);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void unstableProviderDied(IBinder connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection);
                    if (this.mRemote.transact(115, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unstableProviderDied(connection);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isIntentSenderAnActivity(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(116, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isIntentSenderAnActivity(sender);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isIntentSenderAForegroundService(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(117, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isIntentSenderAForegroundService(sender);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isIntentSenderABroadcast(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(118, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isIntentSenderABroadcast(sender);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
                Parcel _data;
                Parcel _reply;
                Throwable th;
                IBinder iBinder;
                Parcel _data2 = Parcel.obtain();
                Parcel _reply2 = Parcel.obtain();
                try {
                    _data2.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (caller != null) {
                        try {
                            iBinder = caller.asBinder();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply = _reply2;
                            _data = _data2;
                        }
                    } else {
                        iBinder = null;
                    }
                    _data2.writeStrongBinder(iBinder);
                    _data2.writeString(callingPackage);
                    if (intent != null) {
                        _data2.writeInt(1);
                        intent.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeString(resolvedType);
                    _data2.writeStrongBinder(resultTo);
                    _data2.writeString(resultWho);
                    _data2.writeInt(requestCode);
                    _data2.writeInt(flags);
                    if (profilerInfo != null) {
                        _data2.writeInt(1);
                        profilerInfo.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (options != null) {
                        _data2.writeInt(1);
                        options.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeInt(userId);
                    if (this.mRemote.transact(119, _data2, _reply2, 0) || Stub.getDefaultImpl() == null) {
                        _reply2.readException();
                        int _result = _reply2.readInt();
                        _reply2.recycle();
                        _data2.recycle();
                        return _result;
                    }
                    _reply = _reply2;
                    _data = _data2;
                    try {
                        int startActivityAsUser = Stub.getDefaultImpl().startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, flags, profilerInfo, options, userId);
                        _reply.recycle();
                        _data.recycle();
                        return startActivityAsUser;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply = _reply2;
                    _data = _data2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public int stopUser(int userid, boolean force, IStopUserCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeInt(force ? 1 : 0);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(120, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopUser(userid, force, callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void registerUserSwitchObserver(IUserSwitchObserver observer, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeString(name);
                    if (this.mRemote.transact(121, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerUserSwitchObserver(observer, name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void unregisterUserSwitchObserver(IUserSwitchObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(122, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterUserSwitchObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int[] getRunningUserIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(123, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRunningUserIds();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void requestSystemServerHeapDump() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(124, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestSystemServerHeapDump();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void requestBugReport(int bugreportType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bugreportType);
                    if (this.mRemote.transact(125, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestBugReport(bugreportType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void requestTelephonyBugReport(String shareTitle, String shareDescription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(shareTitle);
                    _data.writeString(shareDescription);
                    if (this.mRemote.transact(126, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestTelephonyBugReport(shareTitle, shareDescription);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void requestWifiBugReport(String shareTitle, String shareDescription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(shareTitle);
                    _data.writeString(shareDescription);
                    if (this.mRemote.transact(127, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestWifiBugReport(shareTitle, shareDescription);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public Intent getIntentForIntentSender(IIntentSender sender) throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (!this.mRemote.transact(128, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIntentForIntentSender(sender);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public String getLaunchedFromPackage(IBinder activityToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    if (!this.mRemote.transact(129, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLaunchedFromPackage(activityToken);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void killUid(int appId, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    if (this.mRemote.transact(130, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killUid(appId, userId, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setUserIsMonkey(boolean monkey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monkey ? 1 : 0);
                    if (this.mRemote.transact(131, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserIsMonkey(monkey);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void hang(IBinder who, boolean allowRestart) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(who);
                    _data.writeInt(allowRestart ? 1 : 0);
                    if (this.mRemote.transact(132, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hang(who, allowRestart);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public List<ActivityManager.StackInfo> getAllStackInfos() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(133, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllStackInfos();
                    }
                    _reply.readException();
                    List<ActivityManager.StackInfo> _result = _reply.createTypedArrayList(ActivityManager.StackInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void moveTaskToStack(int taskId, int stackId, boolean toTop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    _data.writeInt(toTop ? 1 : 0);
                    if (this.mRemote.transact(134, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveTaskToStack(taskId, stackId, toTop);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void resizeStack(int stackId, Rect bounds, boolean allowResizeInDockedMode, boolean preserveWindows, boolean animate, int animationDuration) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(stackId);
                        int i = 1;
                        if (bounds != null) {
                            _data.writeInt(1);
                            bounds.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeInt(allowResizeInDockedMode ? 1 : 0);
                        _data.writeInt(preserveWindows ? 1 : 0);
                        if (!animate) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        try {
                            _data.writeInt(animationDuration);
                            try {
                                if (this.mRemote.transact(135, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    _reply.recycle();
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().resizeStack(stackId, bounds, allowResizeInDockedMode, preserveWindows, animate, animationDuration);
                                _reply.recycle();
                                _data.recycle();
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public void setFocusedStack(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    if (this.mRemote.transact(136, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFocusedStack(stackId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public ActivityManager.StackInfo getFocusedStackInfo() throws RemoteException {
                ActivityManager.StackInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(137, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFocusedStackInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.StackInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void restart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(138, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restart();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void performIdleMaintenance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(139, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().performIdleMaintenance();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void appNotRespondingViaProvider(IBinder connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection);
                    if (this.mRemote.transact(140, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().appNotRespondingViaProvider(connection);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public Rect getTaskBounds(int taskId) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (!this.mRemote.transact(141, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskBounds(taskId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean setProcessMemoryTrimLevel(String process, int uid, int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(process);
                    _data.writeInt(uid);
                    _data.writeInt(level);
                    boolean _result = false;
                    if (!this.mRemote.transact(142, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setProcessMemoryTrimLevel(process, uid, level);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public String getTagForIntentSender(IIntentSender sender, String prefix) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    _data.writeString(prefix);
                    if (!this.mRemote.transact(143, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTagForIntentSender(sender, prefix);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean startUserInBackground(int userid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    boolean _result = false;
                    if (!this.mRemote.transact(144, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startUserInBackground(userid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isInLockTaskMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(145, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInLockTaskMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void startRecentsActivity(Intent intent, IAssistDataReceiver assistDataReceiver, IRecentsAnimationRunner recentsAnimationRunner) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    IBinder iBinder = null;
                    _data.writeStrongBinder(assistDataReceiver != null ? assistDataReceiver.asBinder() : null);
                    if (recentsAnimationRunner != null) {
                        iBinder = recentsAnimationRunner.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (this.mRemote.transact(146, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startRecentsActivity(intent, assistDataReceiver, recentsAnimationRunner);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void cancelRecentsAnimation(boolean restoreHomeStackPosition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(restoreHomeStackPosition ? 1 : 0);
                    if (this.mRemote.transact(147, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelRecentsAnimation(restoreHomeStackPosition);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int startActivityFromRecents(int taskId, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(148, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startActivityFromRecents(taskId, options);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void startSystemLockTaskMode(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(149, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startSystemLockTaskMode(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isTopOfTask(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(150, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTopOfTask(token);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void bootAnimationComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(151, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bootAnimationComplete();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int checkPermissionWithToken(String permission, int pid, int uid, IBinder callerToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(callerToken);
                    if (!this.mRemote.transact(152, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPermissionWithToken(permission, pid, uid, callerToken);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void registerTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(153, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerTaskStackListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void unregisterTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(154, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterTaskStackListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void notifyCleartextNetwork(int uid, byte[] firstPacket) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeByteArray(firstPacket);
                    if (this.mRemote.transact(155, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyCleartextNetwork(uid, firstPacket);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setTaskResizeable(int taskId, int resizeableMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(resizeableMode);
                    if (this.mRemote.transact(156, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTaskResizeable(taskId, resizeableMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void resizeTask(int taskId, Rect bounds, int resizeMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resizeMode);
                    if (this.mRemote.transact(157, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resizeTask(taskId, bounds, resizeMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int getLockTaskModeState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(158, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLockTaskModeState();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setDumpHeapDebugLimit(String processName, int uid, long maxMemSize, String reportPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    _data.writeLong(maxMemSize);
                    _data.writeString(reportPackage);
                    if (this.mRemote.transact(159, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDumpHeapDebugLimit(processName, uid, maxMemSize, reportPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void dumpHeapFinished(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    if (this.mRemote.transact(160, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dumpHeapFinished(path);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void updateLockTaskPackages(int userId, String[] packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeStringArray(packages);
                    if (this.mRemote.transact(161, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateLockTaskPackages(userId, packages);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void noteAlarmStart(IIntentSender sender, WorkSource workSource, int sourceUid, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceUid);
                    _data.writeString(tag);
                    if (this.mRemote.transact(162, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().noteAlarmStart(sender, workSource, sourceUid, tag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void noteAlarmFinish(IIntentSender sender, WorkSource workSource, int sourceUid, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceUid);
                    _data.writeString(tag);
                    if (this.mRemote.transact(163, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().noteAlarmFinish(sender, workSource, sourceUid, tag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int getPackageProcessState(String packageName, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(164, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageProcessState(packageName, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void updateDeviceOwner(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(165, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateDeviceOwner(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean startBinderTracking() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(166, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startBinderTracking();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean stopBinderTrackingAndDump(ParcelFileDescriptor fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(167, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopBinderTrackingAndDump(fd);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void positionTaskInStack(int taskId, int stackId, int position) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    _data.writeInt(position);
                    if (this.mRemote.transact(168, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().positionTaskInStack(taskId, stackId, position);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void suppressResizeConfigChanges(boolean suppress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(suppress ? 1 : 0);
                    if (this.mRemote.transact(169, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().suppressResizeConfigChanges(suppress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean moveTopActivityToPinnedStack(int stackId, Rect bounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    boolean _result = true;
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(170, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().moveTopActivityToPinnedStack(stackId, bounds);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isAppStartModeDisabled(int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(171, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAppStartModeDisabled(uid, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean unlockUser(int userid, byte[] token, byte[] secret, IProgressListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(172, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unlockUser(userid, token, secret, listener);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void killPackageDependents(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(173, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killPackageDependents(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dockedBounds != null) {
                        _data.writeInt(1);
                        dockedBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempDockedTaskBounds != null) {
                        _data.writeInt(1);
                        tempDockedTaskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempDockedTaskInsetBounds != null) {
                        _data.writeInt(1);
                        tempDockedTaskInsetBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempOtherTaskBounds != null) {
                        _data.writeInt(1);
                        tempOtherTaskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempOtherTaskInsetBounds != null) {
                        _data.writeInt(1);
                        tempOtherTaskInsetBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(174, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resizeDockedStack(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds, tempOtherTaskBounds, tempOtherTaskInsetBounds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void removeStack(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    if (this.mRemote.transact(175, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeStack(stackId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void makePackageIdle(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(176, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().makePackageIdle(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int getMemoryTrimLevel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(177, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMemoryTrimLevel();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean isVrModePackageEnabled(ComponentName packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (packageName != null) {
                        _data.writeInt(1);
                        packageName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(178, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVrModePackageEnabled(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void notifyLockedProfile(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(179, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyLockedProfile(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void startConfirmDeviceCredentialIntent(Intent intent, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(180, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startConfirmDeviceCredentialIntent(intent, options);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void sendIdleJobTrigger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(181, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendIdleJobTrigger();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int sendIntentSender(IIntentSender target, IBinder whitelistToken, int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(target != null ? target.asBinder() : null);
                    try {
                        _data.writeStrongBinder(whitelistToken);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(code);
                        if (intent != null) {
                            _data.writeInt(1);
                            intent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeString(resolvedType);
                        if (finishedReceiver != null) {
                            iBinder = finishedReceiver.asBinder();
                        }
                        _data.writeStrongBinder(iBinder);
                        _data.writeString(requiredPermission);
                        if (options != null) {
                            _data.writeInt(1);
                            options.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (this.mRemote.transact(182, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int sendIntentSender = Stub.getDefaultImpl().sendIntentSender(target, whitelistToken, code, intent, resolvedType, finishedReceiver, requiredPermission, options);
                        _reply.recycle();
                        _data.recycle();
                        return sendIntentSender;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityManager
            public boolean isBackgroundRestricted(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(183, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isBackgroundRestricted(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setRenderThread(int tid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tid);
                    if (this.mRemote.transact(184, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRenderThread(tid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setHasTopUi(boolean hasTopUi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hasTopUi ? 1 : 0);
                    if (this.mRemote.transact(185, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHasTopUi(hasTopUi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public int restartUserInBackground(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(186, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().restartUserInBackground(userId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void cancelTaskWindowTransition(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(187, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelTaskWindowTransition(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) throws RemoteException {
                ActivityManager.TaskSnapshot _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(reducedResolution ? 1 : 0);
                    if (!this.mRemote.transact(188, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskSnapshot(taskId, reducedResolution);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.TaskSnapshot.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void scheduleApplicationInfoChanged(List<String> packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(189, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().scheduleApplicationInfoChanged(packageNames, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setPersistentVrThread(int tid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tid);
                    if (this.mRemote.transact(190, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPersistentVrThread(tid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void waitForNetworkStateUpdate(long procStateSeq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(procStateSeq);
                    if (this.mRemote.transact(191, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().waitForNetworkStateUpdate(procStateSeq);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void backgroundWhitelistUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(192, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().backgroundWhitelistUid(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean startUserInBackgroundWithListener(int userid, IProgressListener unlockProgressListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeStrongBinder(unlockProgressListener != null ? unlockProgressListener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(193, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startUserInBackgroundWithListener(userid, unlockProgressListener);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void startDelegateShellPermissionIdentity(int uid, String[] permissions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeStringArray(permissions);
                    if (this.mRemote.transact(194, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startDelegateShellPermissionIdentity(uid, permissions);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void stopDelegateShellPermissionIdentity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(195, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopDelegateShellPermissionIdentity();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public ParcelFileDescriptor getLifeMonitor() throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(196, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLifeMonitor();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public boolean startUserInForegroundWithListener(int userid, IProgressListener unlockProgressListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeStrongBinder(unlockProgressListener != null ? unlockProgressListener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(197, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startUserInForegroundWithListener(userid, unlockProgressListener);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(198, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void finishBindApplication(IApplicationThread app) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app != null ? app.asBinder() : null);
                    if (this.mRemote.transact(199, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finishBindApplication(app);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityManager
            public void setHmThreadToRtg(String param) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(param);
                    if (this.mRemote.transact(200, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHmThreadToRtg(param);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IActivityManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IActivityManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
