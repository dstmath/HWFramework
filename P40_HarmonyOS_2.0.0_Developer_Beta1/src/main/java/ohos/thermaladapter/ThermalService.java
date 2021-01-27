package ohos.thermaladapter;

import ohos.thermallistener.ThermalConnection;

public class ThermalService {
    private static volatile ThermalService thermalService;

    private ThermalService() {
    }

    public static ThermalService getInstance() {
        if (thermalService == null) {
            synchronized (ThermalService.class) {
                if (thermalService == null) {
                    thermalService = new ThermalService();
                }
            }
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
