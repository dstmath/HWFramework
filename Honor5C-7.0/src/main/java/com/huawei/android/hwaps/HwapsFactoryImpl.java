package com.huawei.android.hwaps;

public class HwapsFactoryImpl implements IHwapsFactory {
    public IFpsRequest getFpsRequest() {
        return new FpsRequest();
    }

    public IFpsController getFpsController() {
        return new FpsController();
    }

    public IEventAnalyzed getEventAnalyzed() {
        return new EventAnalyzed();
    }
}
