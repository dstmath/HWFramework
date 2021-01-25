package ohos.thermallistener;

import ohos.hiviewdfx.HiLogLabel;
import ohos.thermaladapter.ThermalService;

public class ThermalListener {
    private static final int LOG_DOMAIN = 218114308;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, LOG_DOMAIN, TAG);
    private static final String TAG = "ThermalListener";
    private ThermalService mThermalService = ThermalService.getInstance();

    public void connectThermalService(ThermalConnection thermalConnection, String str) {
        this.mThermalService.connectRemoteThermalService(thermalConnection, str);
    }
}
