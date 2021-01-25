package ohos.batterysipper;

import java.util.Iterator;
import java.util.List;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.batterysipperadapter.BatterySipperHelper;
import ohos.batterysipperadapter.BatterySipperService;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BatterySipper {
    private static final int LOG_DOMAIN = 218114307;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final String TAG = "BatterySipper";
    private BatterySipperService sipperService = BatterySipperHelper.getService();

    @SystemApi
    public List<DetailBatteryStats> getBatteryStats(Context context, List<Integer> list) {
        List<DetailBatteryStats> batteryStats = this.sipperService.getBatteryStats(context, list);
        Iterator<DetailBatteryStats> it = batteryStats.iterator();
        while (it.hasNext()) {
            HiLog.info(LOG_LABEL, "BatterySipper.getBatteryStats() %s ", new Object[]{it.next().toString()});
        }
        return batteryStats;
    }

    @SystemApi
    public List<DetailBatteryStats> getBatteryStats(List<Integer> list) {
        List<DetailBatteryStats> batteryStats = this.sipperService.getBatteryStats(list);
        Iterator<DetailBatteryStats> it = batteryStats.iterator();
        while (it.hasNext()) {
            HiLog.info(LOG_LABEL, "BatterySipper.getBatteryStats() %s ", new Object[]{it.next().toString()});
        }
        return batteryStats;
    }
}
