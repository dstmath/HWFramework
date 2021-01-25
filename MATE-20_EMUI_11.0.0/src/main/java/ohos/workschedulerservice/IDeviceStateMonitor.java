package ohos.workschedulerservice;

import java.util.ArrayList;
import ohos.aafwk.content.Intent;
import ohos.workschedulerservice.controller.CommonEventStatus;
import ohos.workschedulerservice.controller.WorkStatus;

public interface IDeviceStateMonitor {
    void onAppStateChanged(int i, String str);

    void onCommonEventChanged(Intent intent, ArrayList<CommonEventStatus> arrayList);

    void onDeviceStateChanged(WorkStatus workStatus, long j);

    void onHapStateChanged(int i, String str);

    void onRunWorkNow(WorkStatus workStatus);

    void onUserStateChanged(int i);
}
