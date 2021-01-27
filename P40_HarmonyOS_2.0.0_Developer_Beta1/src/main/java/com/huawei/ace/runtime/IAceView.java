package com.huawei.ace.runtime;

public interface IAceView {
    public static final int SURFACE_STATE_CREATED = 1;
    public static final int SURFACE_STATE_HIDDEN = 3;
    public static final int SURFACE_STATE_SHOWING = 2;
    public static final int SURFACE_STATE_UNINITIALIZED = 0;

    public static class ViewportMetrics {
        public float devicePixelRatio = 1.0f;
        public int physicalHeight = 0;
        public int physicalPaddingBottom = 0;
        public int physicalPaddingLeft = 0;
        public int physicalPaddingRight = 0;
        public int physicalPaddingTop = 0;
        public int physicalViewInsetBottom = 0;
        public int physicalViewInsetLeft = 0;
        public int physicalViewInsetRight = 0;
        public int physicalViewInsetTop = 0;
        public int physicalWidth = 0;
        public int systemGestureInsetBottom = 0;
        public int systemGestureInsetLeft = 0;
        public int systemGestureInsetRight = 0;
        public int systemGestureInsetTop = 0;
    }

    void addResourcePlugin(AceResourcePlugin aceResourcePlugin);

    long getNativePtr();

    void initDeviceType();

    void onPause();

    void onResume();

    void releaseNativeView();

    void setWindowModal(int i);

    void viewCreated();
}
