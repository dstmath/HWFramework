package com.huawei.android.hwaps;

public class HwapsFactoryImpl implements IHwapsFactory {
    public static ISmartLowpowerBrowser iSmartLowpowerBrowser = null;

    public IFpsRequest getFpsRequest() {
        return new FpsRequest();
    }

    public IFpsController getFpsController() {
        return new FpsController();
    }

    public IEventAnalyzed getEventAnalyzed() {
        return new EventAnalyzed();
    }

    public ISmartLowpowerBrowser getSmartLowpowerBrowser() {
        if (iSmartLowpowerBrowser == null) {
            iSmartLowpowerBrowser = new SmartLowpowerBrowser();
        }
        return iSmartLowpowerBrowser;
    }
}
