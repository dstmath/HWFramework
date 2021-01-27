package ohos.batterysipperadapter;

import java.util.List;
import ohos.app.Context;
import ohos.batterysipper.DetailBatteryStats;

public class BatterySipperService {
    private static BatterySipperAdapter batterySipperAdapter;

    public BatterySipperService() {
        batterySipperAdapter = BatterySipperAdapter.getInstance();
    }

    public List<DetailBatteryStats> getBatteryStats(Context context, List<Integer> list) {
        return batterySipperAdapter.getBatteryStats(context, list);
    }

    public List<DetailBatteryStats> getBatteryStats(List<Integer> list) {
        return batterySipperAdapter.getBatteryStats(list);
    }
}
