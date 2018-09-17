package huawei.android.app;

import android.os.RemoteException;
import android.os.ServiceManager;
import huawei.android.app.IHwAlarmManagerEx.Stub;
import huawei.android.content.HwContextEx;
import java.util.List;

public class HwAlarmManagerEx {
    private static final String TAG = "HwAlarmManagerEx";
    private static volatile HwAlarmManagerEx mInstance = null;
    IHwAlarmManagerEx mService;

    public static synchronized HwAlarmManagerEx getInstance() {
        HwAlarmManagerEx hwAlarmManagerEx;
        synchronized (HwAlarmManagerEx.class) {
            if (mInstance == null) {
                mInstance = new HwAlarmManagerEx();
            }
            hwAlarmManagerEx = mInstance;
        }
        return hwAlarmManagerEx;
    }

    private HwAlarmManagerEx() {
        this.mService = null;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_ALARM_SERVICE));
    }

    public void setAlarmsPending(List<String> pkgList, List<String> actionList, boolean pending, int type) {
        try {
            this.mService.setAlarmsPending(pkgList, actionList, pending, type);
        } catch (RemoteException e) {
        }
    }

    public void removeAllPendingAlarms() {
        try {
            this.mService.removeAllPendingAlarms();
        } catch (RemoteException e) {
        }
    }

    public void setAlarmsAdjust(List<String> pkgList, List<String> actionList, boolean adjust, int type, long interval, int mode) {
        try {
            this.mService.setAlarmsAdjust(pkgList, actionList, adjust, type, interval, mode);
        } catch (RemoteException e) {
        }
    }

    public void removeAllAdjustAlarms() {
        try {
            this.mService.removeAllAdjustAlarms();
        } catch (RemoteException e) {
        }
    }
}
