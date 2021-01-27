package com.android.server.wm;

import android.content.Context;
import java.util.ArrayList;

public interface IHwActivityTaskManagerInner {
    ActivityTaskManagerService getATMS();

    HwAtmDAMonitorProxy getAtmDAMonitor();

    ActivityRecord getLastResumedActivityRecord();

    WindowProcessController getProcessControllerForHwAtmsEx(String str, int i);

    ArrayList<TaskRecord> getRecentRawTasks();

    RootActivityContainer getRootActivityContainer();

    ActivityStackSupervisor getStackSupervisor();

    boolean getSystemReady();

    Context getUiContext();
}
