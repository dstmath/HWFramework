package ohos.softnet.connect;

public interface DiscoveryCallback {
    void onDeviceFound(DeviceDesc deviceDesc);

    void onDeviceLost(DeviceDesc deviceDesc);
}
