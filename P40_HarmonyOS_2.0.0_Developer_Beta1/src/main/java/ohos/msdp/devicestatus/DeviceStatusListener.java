package ohos.msdp.devicestatus;

import java.util.List;

public interface DeviceStatusListener {
    void onDeviceStatusChanged(List<DeviceStatusEvent> list);
}
