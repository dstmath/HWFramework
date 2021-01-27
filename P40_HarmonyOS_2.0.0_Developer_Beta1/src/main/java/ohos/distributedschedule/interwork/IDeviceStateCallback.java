package ohos.distributedschedule.interwork;

public interface IDeviceStateCallback {
    void onDeviceOffline(String str, int i);

    void onDeviceOnline(String str, int i);
}
