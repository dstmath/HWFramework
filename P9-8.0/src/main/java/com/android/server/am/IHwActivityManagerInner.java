package com.android.server.am;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManagerInternal;
import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.internal.app.ProcessMap;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.AlarmManagerService;
import com.android.server.SystemServiceManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public interface IHwActivityManagerInner {
    boolean bringDownDisabledPackageServicesLocked(String str, Set<String> set, int i, boolean z, boolean z2, boolean z3);

    File dumpStackTracesInner(boolean z, ArrayList<Integer> arrayList, ProcessCpuTracker processCpuTracker, SparseArray<Boolean> sparseArray, ArrayList<Integer> arrayList2);

    boolean finishDisabledPackageActivitiesLocked(String str, Set<String> set, boolean z, boolean z2, int i);

    void finishForceStopPackageLockedInner(String str, int i);

    void forceStopPackage(String str, int i);

    boolean forceStopPackageLockedInner(String str, int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i2, String str2);

    ActivityManagerService getAMSForLock();

    boolean getActivityIdle();

    AlarmManagerService getAlarmService();

    int getAmsPid();

    BroadcastQueue getBgBroadcastQueue();

    BroadcastQueue[] getBroadcastQueues();

    HwDAMonitorProxy getDAMonitor();

    TaskChangeNotificationController getHwTaskChangeController();

    ArrayList<ContentProviderRecord> getLaunchingProviders();

    PackageManagerInternal getPackageManagerInternal();

    ProcessMap<ProcessRecord> getProcessNames();

    ProcessRecord getProcessRecord(String str, int i, boolean z);

    ProviderMap getProviderMap();

    ArrayList getRecentTasks();

    SystemServiceManager getServiceManager();

    ActivityStackSupervisor getStackSupervisor();

    SparseArray<ArrayMap<String, ArrayList<Intent>>> getStickyBroadcasts();

    boolean getSystemReady();

    Context getUiContext();

    UserController getUserController();

    void showUninstallLauncherDialog(String str);

    void startPersistApp(ApplicationInfo applicationInfo, String str, boolean z, String str2);

    void stopAssociationLockedInner(int i, String str, int i2, ComponentName componentName);
}
