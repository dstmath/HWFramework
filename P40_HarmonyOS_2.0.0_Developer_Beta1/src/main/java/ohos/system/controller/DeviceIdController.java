package ohos.system.controller;

import java.util.Optional;
import ohos.system.adapter.DeviceIdAdapter;

public class DeviceIdController {
    public static Optional<String> getUdid() {
        return DeviceIdAdapter.getUdid();
    }
}
