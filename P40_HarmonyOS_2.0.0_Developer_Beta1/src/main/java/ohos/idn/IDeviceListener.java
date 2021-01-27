package ohos.idn;

import ohos.annotation.SystemApi;

@SystemApi
public interface IDeviceListener {
    void onDeviceInfoChanged(String str, DeviceInfoType deviceInfoType);

    void onDeviceOffline(BasicInfo basicInfo);

    void onDeviceOnline(BasicInfo basicInfo);
}
