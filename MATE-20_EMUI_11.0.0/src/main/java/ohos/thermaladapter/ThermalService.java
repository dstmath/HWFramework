package ohos.thermaladapter;

import ohos.thermallistener.ThermalConnection;

public class ThermalService {
    private static ThermalService sThermalService;

    public static ThermalService getInstance() {
        ThermalService thermalService;
        synchronized (ThermalService.class) {
            if (sThermalService == null) {
                sThermalService = new ThermalService();
            }
            thermalService = sThermalService;
        }
        return thermalService;
    }

    public boolean connectRemoteThermalService(ThermalConnection thermalConnection, String str) {
        ThermalConnectionAdapter thermalConnectionAdapter = new ThermalConnectionAdapter(thermalConnection);
        if (!thermalConnectionAdapter.linkToSdkService()) {
            return false;
        }
        ThermalConnectionAdapter.registerCallback(thermalConnectionAdapter, str);
        return true;
    }
}
