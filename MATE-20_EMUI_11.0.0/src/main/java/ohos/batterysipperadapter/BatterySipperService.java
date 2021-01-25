package ohos.batterysipperadapter;

import java.util.List;
import ohos.app.Context;
import ohos.batterysipper.DetailBatteryStats;

public class BatterySipperService {
    private static BatterySipperAdapter sBatterySipperAdapter;

    public BatterySipperService() {
        sipperAdatperInit();
    }

    private void sipperAdatperInit() {
        if (sBatterySipperAdapter == null) {
            sBatterySipperAdapter = new BatterySipperAdapter();
        }
    }

    public List<DetailBatteryStats> getBatteryStats(Context context, List<Integer> list) {
        return sBatterySipperAdapter.getBatteryStats(context, list);
    }

    public List<DetailBatteryStats> getBatteryStats(List<Integer> list) {
        return sBatterySipperAdapter.getBatteryStats(list);
    }
}
