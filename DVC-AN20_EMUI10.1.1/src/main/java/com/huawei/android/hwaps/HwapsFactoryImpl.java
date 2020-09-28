package com.huawei.android.hwaps;

public class HwapsFactoryImpl implements IHwapsFactory {
    private static volatile IEventAnalyzed mEventAnalyzed = null;
    private static volatile IFpsController mFpsController = null;
    private static volatile IFpsRequest mFpsRequest = null;

    public IFpsRequest getFpsRequest() {
        if (mFpsRequest == null) {
            synchronized (HwapsFactoryImpl.class) {
                if (mFpsRequest == null) {
                    mFpsRequest = new FpsRequest();
                }
            }
        }
        return mFpsRequest;
    }

    public IFpsController getFpsController() {
        if (mFpsController == null) {
            synchronized (HwapsFactoryImpl.class) {
                if (mFpsController == null) {
                    mFpsController = new FpsController();
                }
            }
        }
        return mFpsController;
    }

    public IEventAnalyzed getEventAnalyzed() {
        if (mEventAnalyzed == null) {
            synchronized (HwapsFactoryImpl.class) {
                if (mEventAnalyzed == null) {
                    mEventAnalyzed = new EventAnalyzed();
                }
            }
        }
        return mEventAnalyzed;
    }
}
