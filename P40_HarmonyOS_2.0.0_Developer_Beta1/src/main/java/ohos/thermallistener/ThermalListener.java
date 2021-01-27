package ohos.thermallistener;

import ohos.thermaladapter.ThermalService;

public class ThermalListener {
    private ThermalService thermalService = ThermalService.getInstance();

    public void connectThermalService(ThermalConnection thermalConnection, String str) {
        this.thermalService.connectRemoteThermalService(thermalConnection, str);
    }
}
