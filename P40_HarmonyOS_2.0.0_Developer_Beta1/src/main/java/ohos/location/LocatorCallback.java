package ohos.location;

public interface LocatorCallback {
    void onErrorReport(int i);

    void onLocationReport(Location location);

    void onStatusChanged(int i);
}
