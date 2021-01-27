package com.huawei.hwaps;

public interface IHwapsFactory {
    IEventAnalyzed getEventAnalyzed();

    IFpsController getFpsController();

    IFpsRequest getFpsRequest();
}
