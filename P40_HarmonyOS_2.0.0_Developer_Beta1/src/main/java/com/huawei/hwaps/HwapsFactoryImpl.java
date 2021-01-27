package com.huawei.hwaps;

public class HwapsFactoryImpl implements IHwapsFactory {
    private static volatile IEventAnalyzed mEventAnalyzed = null;
    private static volatile IFpsController mFpsController = null;
    private static volatile IFpsRequest mFpsRequest = null;

    public IFpsRequest getFpsRequest() {
        if (mFpsRequest == null) {
            mFpsRequest = new FpsRequest();
        }
        return mFpsRequest;
    }

    public IFpsController getFpsController() {
        if (mFpsController == null) {
            mFpsController = new FpsController();
        }
        return mFpsController;
    }

    public IEventAnalyzed getEventAnalyzed() {
        if (mEventAnalyzed == null) {
            mEventAnalyzed = new EventAnalyzed();
        }
        return mEventAnalyzed;
    }
}
