package ohos.accessibility.ability;

public class GestureResultListenerInfo {
    private GestureResultListener mGestureResultListener;

    public GestureResultListenerInfo(GestureResultListener gestureResultListener) {
        this.mGestureResultListener = gestureResultListener;
    }

    public GestureResultListener getGestureResultListener() {
        return this.mGestureResultListener;
    }
}
