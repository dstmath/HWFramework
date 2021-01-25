package com.android.server.am;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManagerInternal;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.internal.app.ProcessMap;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.AlarmManagerService;
import com.android.server.SystemServiceManager;
import com.android.server.am.ActivityManagerService;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public interface IHwActivityManagerInner {
    boolean bringDownDisabledPackageServicesLocked(String str, Set<String> set, int i, boolean z, boolean z2);

    boolean cleanupAppInLaunchingProvidersLockedEx(ProcessRecord processRecord, boolean z);

    File dumpStackTracesInner(boolean z, ArrayList<Integer> arrayList, ProcessCpuTracker processCpuTracker, SparseArray<Boolean> sparseArray, ArrayList<Integer> arrayList2);

    boolean finishDisabledPackageActivitiesLocked(String str, boolean z, boolean z2, int i);

    void finishForceStopPackageLockedInner(String str, int i);

    void forceStopPackage(String str, int i);

    boolean forceStopPackageLockedInner(String str, int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i2, String str2);

    ActivityManagerService getAMSForLock();

    boolean getActivityIdle();

    AlarmManagerService getAlarmService();

    int getAmsPid();

    AppErrors getAppErrors();

    BroadcastQueue getBgBroadcastQueue();

    BroadcastQueue[] getBroadcastQueues();

    ActivityManagerConstants getConstants();

    HwDAMonitorProxy getDAMonitor();

    ArrayList<ContentProviderRecord> getLaunchingProviders();

    ArrayList<ProcessRecord> getLruProcesses();

    int getMyPid();

    PackageManagerInternal getPackageManagerInternal();

    ActivityManagerService.PidMap getPidsSelfLocked();

    ProcessMap<ProcessRecord> getProcessNames();

    ProcessRecord getProcessRecord(String str, int i, boolean z);

    ProcessRecord getProcessRecordLockedEx(String str, int i, boolean z);

    ProviderMap getProviderMap();

    SystemServiceManager getServiceManager();

    SparseArray<ArrayMap<String, ArrayList<Intent>>> getStickyBroadcasts();

    boolean getSystemReady();

    Context getUiContext();

    Handler getUiHandler();

    UserController getUserController();

    boolean isKeyguardLockedEx();

    ProcessRecord newProcessRecordLockedEx(ApplicationInfo applicationInfo, String str, boolean z, int i, HostingRecord hostingRecord);

    boolean removeProcessLockedEx(ProcessRecord processRecord, boolean z, boolean z2, String str);

    boolean removeProcessLockedInner(ProcessRecord processRecord, int i, IBinder iBinder, boolean z, boolean z2, String str);

    void startPersistApp(ApplicationInfo applicationInfo, String str, boolean z, String str2);

    void startProcessLockedEx(ProcessRecord processRecord, HostingRecord hostingRecord, String str);

    void stopAssociationLockedInner(int i, String str, int i2, ComponentName componentName);

    void updateLruProcessLockedEx(ProcessRecord processRecord, boolean z, ProcessRecord processRecord2);

    void updateOomAdjLockedEx(String str);
}
