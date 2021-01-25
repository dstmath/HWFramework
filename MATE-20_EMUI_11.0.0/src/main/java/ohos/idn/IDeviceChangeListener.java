package ohos.idn;

import ohos.annotation.SystemApi;

@SystemApi
public interface IDeviceChangeListener {
    void onDeviceOffline(BasicInfo basicInfo);

    void onDeviceOnline(BasicInfo basicInfo);
}
