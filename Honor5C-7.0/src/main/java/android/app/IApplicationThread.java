package android.app;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.content.ReferrerIntent;
import java.io.FileDescriptor;
import java.util.List;
import java.util.Map;

public interface IApplicationThread extends IInterface {
    public static final int BACKGROUND_VISIBLE_BEHIND_CHANGED_TRANSACTION = 54;
    public static final int BACKUP_MODE_FULL = 1;
    public static final int BACKUP_MODE_INCREMENTAL = 0;
    public static final int BACKUP_MODE_RESTORE = 2;
    public static final int BACKUP_MODE_RESTORE_FULL = 3;
    public static final int BIND_APPLICATION_TRANSACTION = 13;
    public static final int CANCEL_VISIBLE_BEHIND_TRANSACTION = 53;
    public static final int CLEAR_DNS_CACHE_TRANSACTION = 38;
    public static final int DEBUG_OFF = 0;
    public static final int DEBUG_ON = 1;
    public static final int DEBUG_WAIT = 2;
    public static final int DISPATCH_PACKAGE_BROADCAST_TRANSACTION = 34;
    public static final int DUMP_ACTIVITY_TRANSACTION = 37;
    public static final int DUMP_DB_INFO_TRANSACTION = 46;
    public static final int DUMP_GFX_INFO_TRANSACTION = 44;
    public static final int DUMP_HEAP_TRANSACTION = 36;
    public static final int DUMP_MEM_INFO_TRANSACTION = 43;
    public static final int DUMP_PROVIDER_TRANSACTION = 45;
    public static final int DUMP_SERVICE_TRANSACTION = 22;
    public static final int ENTER_ANIMATION_COMPLETE_TRANSACTION = 55;
    public static final int EXTERNAL_STORAGE_UNAVAILABLE = 1;
    public static final int NOTIFY_CLEARTEXT_NETWORK_TRANSACTION = 56;
    public static final int PACKAGE_REMOVED = 0;
    public static final int PACKAGE_REMOVED_DONT_KILL = 2;
    public static final int PACKAGE_REPLACED = 3;
    public static final int PROCESS_IN_BACKGROUND_TRANSACTION = 19;
    public static final int PROFILER_CONTROL_TRANSACTION = 28;
    public static final int REQUEST_ASSIST_CONTEXT_EXTRAS_TRANSACTION = 48;
    public static final int SCHEDULE_ACTIVITY_CONFIGURATION_CHANGED_TRANSACTION = 25;
    public static final int SCHEDULE_BIND_SERVICE_TRANSACTION = 20;
    public static final int SCHEDULE_CONFIGURATION_CHANGED_TRANSACTION = 16;
    public static final int SCHEDULE_CRASH_TRANSACTION = 35;
    public static final int SCHEDULE_CREATE_BACKUP_AGENT_TRANSACTION = 30;
    public static final int SCHEDULE_CREATE_SERVICE_TRANSACTION = 11;
    public static final int SCHEDULE_DESTROY_BACKUP_AGENT_TRANSACTION = 31;
    public static final int SCHEDULE_EXIT_TRANSACTION = 14;
    public static final int SCHEDULE_FINISH_ACTIVITY_TRANSACTION = 9;
    public static final int SCHEDULE_INSTALL_PROVIDER_TRANSACTION = 51;
    public static final int SCHEDULE_LAUNCH_ACTIVITY_TRANSACTION = 7;
    public static final int SCHEDULE_LOCAL_VOICE_INTERACTION_STARTED_TRANSACTION = 61;
    public static final int SCHEDULE_LOW_MEMORY_TRANSACTION = 24;
    public static final int SCHEDULE_MULTI_WINDOW_CHANGED_TRANSACTION = 59;
    public static final int SCHEDULE_NEW_INTENT_TRANSACTION = 8;
    public static final int SCHEDULE_ON_NEW_ACTIVITY_OPTIONS_TRANSACTION = 32;
    public static final int SCHEDULE_PAUSE_ACTIVITY_TRANSACTION = 1;
    public static final int SCHEDULE_PICTURE_IN_PICTURE_CHANGED_TRANSACTION = 60;
    public static final int SCHEDULE_RECEIVER_TRANSACTION = 10;
    public static final int SCHEDULE_REGISTERED_RECEIVER_TRANSACTION = 23;
    public static final int SCHEDULE_RELAUNCH_ACTIVITY_TRANSACTION = 26;
    public static final int SCHEDULE_RESUME_ACTIVITY_TRANSACTION = 5;
    public static final int SCHEDULE_SEND_RESULT_TRANSACTION = 6;
    public static final int SCHEDULE_SERVICE_ARGS_TRANSACTION = 17;
    public static final int SCHEDULE_SLEEPING_TRANSACTION = 27;
    public static final int SCHEDULE_STOP_ACTIVITY_TRANSACTION = 3;
    public static final int SCHEDULE_STOP_SERVICE_TRANSACTION = 12;
    public static final int SCHEDULE_SUICIDE_TRANSACTION = 33;
    public static final int SCHEDULE_TRANSLUCENT_CONVERSION_COMPLETE_TRANSACTION = 49;
    public static final int SCHEDULE_TRIM_MEMORY_TRANSACTION = 42;
    public static final int SCHEDULE_UNBIND_SERVICE_TRANSACTION = 21;
    public static final int SCHEDULE_WINDOW_VISIBILITY_TRANSACTION = 4;
    public static final int SET_CORE_SETTINGS_TRANSACTION = 40;
    public static final int SET_HTTP_PROXY_TRANSACTION = 39;
    public static final int SET_PROCESS_STATE_TRANSACTION = 50;
    public static final int SET_SCHEDULING_GROUP_TRANSACTION = 29;
    public static final int START_BINDER_TRACKING_TRANSACTION = 57;
    public static final int STOP_BINDER_TRACKING_AND_DUMP_TRANSACTION = 58;
    public static final int UNSTABLE_PROVIDER_DIED_TRANSACTION = 47;
    public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO_TRANSACTION = 41;
    public static final int UPDATE_TIME_PREFS_TRANSACTION = 52;
    public static final int UPDATE_TIME_ZONE_TRANSACTION = 18;
    public static final String descriptor = "android.app.IApplicationThread";

    void bindApplication(String str, ApplicationInfo applicationInfo, List<ProviderInfo> list, ComponentName componentName, ProfilerInfo profilerInfo, Bundle bundle, IInstrumentationWatcher iInstrumentationWatcher, IUiAutomationConnection iUiAutomationConnection, int i, boolean z, boolean z2, boolean z3, boolean z4, Configuration configuration, CompatibilityInfo compatibilityInfo, Map<String, IBinder> map, Bundle bundle2) throws RemoteException;

    void clearDnsCache() throws RemoteException;

    void dispatchPackageBroadcast(int i, String[] strArr) throws RemoteException;

    void dumpActivity(FileDescriptor fileDescriptor, IBinder iBinder, String str, String[] strArr) throws RemoteException;

    void dumpDbInfo(FileDescriptor fileDescriptor, String[] strArr) throws RemoteException;

    void dumpGfxInfo(FileDescriptor fileDescriptor, String[] strArr) throws RemoteException;

    void dumpHeap(boolean z, String str, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void dumpMemInfo(FileDescriptor fileDescriptor, MemoryInfo memoryInfo, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, String[] strArr) throws RemoteException;

    void dumpProvider(FileDescriptor fileDescriptor, IBinder iBinder, String[] strArr) throws RemoteException;

    void dumpService(FileDescriptor fileDescriptor, IBinder iBinder, String[] strArr) throws RemoteException;

    void notifyCleartextNetwork(byte[] bArr) throws RemoteException;

    void processInBackground() throws RemoteException;

    void profilerControl(boolean z, ProfilerInfo profilerInfo, int i) throws RemoteException;

    void requestAssistContextExtras(IBinder iBinder, IBinder iBinder2, int i, int i2) throws RemoteException;

    void scheduleActivityConfigurationChanged(IBinder iBinder, Configuration configuration, boolean z) throws RemoteException;

    void scheduleBackgroundVisibleBehindChanged(IBinder iBinder, boolean z) throws RemoteException;

    void scheduleBindService(IBinder iBinder, Intent intent, boolean z, int i) throws RemoteException;

    void scheduleCancelVisibleBehind(IBinder iBinder) throws RemoteException;

    void scheduleConfigurationChanged(Configuration configuration) throws RemoteException;

    void scheduleCrash(String str) throws RemoteException;

    void scheduleCreateBackupAgent(ApplicationInfo applicationInfo, CompatibilityInfo compatibilityInfo, int i) throws RemoteException;

    void scheduleCreateService(IBinder iBinder, ServiceInfo serviceInfo, CompatibilityInfo compatibilityInfo, int i) throws RemoteException;

    void scheduleDestroyActivity(IBinder iBinder, boolean z, int i) throws RemoteException;

    void scheduleDestroyBackupAgent(ApplicationInfo applicationInfo, CompatibilityInfo compatibilityInfo) throws RemoteException;

    void scheduleEnterAnimationComplete(IBinder iBinder) throws RemoteException;

    void scheduleExit() throws RemoteException;

    void scheduleInstallProvider(ProviderInfo providerInfo) throws RemoteException;

    void scheduleLaunchActivity(Intent intent, IBinder iBinder, int i, ActivityInfo activityInfo, Configuration configuration, Configuration configuration2, CompatibilityInfo compatibilityInfo, String str, IVoiceInteractor iVoiceInteractor, int i2, Bundle bundle, PersistableBundle persistableBundle, List<ResultInfo> list, List<ReferrerIntent> list2, boolean z, boolean z2, ProfilerInfo profilerInfo) throws RemoteException;

    void scheduleLocalVoiceInteractionStarted(IBinder iBinder, IVoiceInteractor iVoiceInteractor) throws RemoteException;

    void scheduleLowMemory() throws RemoteException;

    void scheduleMultiWindowModeChanged(IBinder iBinder, boolean z) throws RemoteException;

    void scheduleNewIntent(List<ReferrerIntent> list, IBinder iBinder) throws RemoteException;

    void scheduleOnNewActivityOptions(IBinder iBinder, ActivityOptions activityOptions) throws RemoteException;

    void schedulePauseActivity(IBinder iBinder, boolean z, boolean z2, int i, boolean z3) throws RemoteException;

    void schedulePictureInPictureModeChanged(IBinder iBinder, boolean z) throws RemoteException;

    void scheduleReceiver(Intent intent, ActivityInfo activityInfo, CompatibilityInfo compatibilityInfo, int i, String str, Bundle bundle, boolean z, int i2, int i3) throws RemoteException;

    void scheduleRegisteredReceiver(IIntentReceiver iIntentReceiver, Intent intent, int i, String str, Bundle bundle, boolean z, boolean z2, int i2, int i3) throws RemoteException;

    void scheduleRelaunchActivity(IBinder iBinder, List<ResultInfo> list, List<ReferrerIntent> list2, int i, boolean z, Configuration configuration, Configuration configuration2, boolean z2) throws RemoteException;

    void scheduleResumeActivity(IBinder iBinder, int i, boolean z, Bundle bundle) throws RemoteException;

    void scheduleSendResult(IBinder iBinder, List<ResultInfo> list) throws RemoteException;

    void scheduleServiceArgs(IBinder iBinder, boolean z, int i, int i2, Intent intent) throws RemoteException;

    void scheduleSleeping(IBinder iBinder, boolean z) throws RemoteException;

    void scheduleStopActivity(IBinder iBinder, boolean z, int i) throws RemoteException;

    void scheduleStopService(IBinder iBinder) throws RemoteException;

    void scheduleSuicide() throws RemoteException;

    void scheduleTranslucentConversionComplete(IBinder iBinder, boolean z) throws RemoteException;

    void scheduleTrimMemory(int i) throws RemoteException;

    void scheduleUnbindService(IBinder iBinder, Intent intent) throws RemoteException;

    void scheduleWindowVisibility(IBinder iBinder, boolean z) throws RemoteException;

    void setCoreSettings(Bundle bundle) throws RemoteException;

    void setHttpProxy(String str, String str2, String str3, Uri uri) throws RemoteException;

    void setProcessState(int i) throws RemoteException;

    void setSchedulingGroup(int i) throws RemoteException;

    void startBinderTracking() throws RemoteException;

    void stopBinderTrackingAndDump(FileDescriptor fileDescriptor) throws RemoteException;

    void unstableProviderDied(IBinder iBinder) throws RemoteException;

    void updatePackageCompatibilityInfo(String str, CompatibilityInfo compatibilityInfo) throws RemoteException;

    void updateTimePrefs(boolean z) throws RemoteException;

    void updateTimeZone() throws RemoteException;
}
