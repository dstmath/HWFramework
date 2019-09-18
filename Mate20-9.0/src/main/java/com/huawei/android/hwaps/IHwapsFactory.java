package com.huawei.android.hwaps;

public interface IHwapsFactory {
    IEventAnalyzed getEventAnalyzed();

    IFpsController getFpsController();

    IFpsRequest getFpsRequest();

    ISmartLowpowerBrowser getSmartLowpowerBrowser();
}
