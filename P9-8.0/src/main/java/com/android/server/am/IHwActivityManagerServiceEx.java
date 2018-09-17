package com.android.server.am;

import java.util.List;
import java.util.Map;

public interface IHwActivityManagerServiceEx {
    boolean canCleanTaskRecord(String str);

    int changeGidIfRepairMode(int i, String str);

    boolean cleanPackageRes(List<String> list, Map<String, List<String>> map, int i, boolean z, boolean z2, boolean z3);

    TaskChangeNotificationController getHwTaskChangeController();

    String[] initPCEntryArgsForService(ServiceRecord serviceRecord, ActivityManagerService activityManagerService);

    boolean isApplyPersistAppPatch(String str, int i, int i2, boolean z, boolean z2, String str2, String str3);

    boolean isSpecialVideoForPCMode(ActivityRecord activityRecord);

    void killApplication(String str, int i, int i2, String str2);

    void notifyActivityState(ActivityRecord activityRecord, String str);

    void notifyActivityStateExt(ActivityRecord activityRecord, String str);

    void onAppGroupChanged(int i, int i2, String str, int i3, int i4);

    void showUninstallLauncherDialog(String str);

    void storeDisplayIdForPCMode(String str, int i, ActivityManagerService activityManagerService);

    Boolean switchUser(int i);

    void systemReady(Runnable runnable);
}
