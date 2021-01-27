package ohos.accessibility.adapter;

public class AccessibilityNativeTools {
    public static native AccessibilityViewInfo getAccessibilityNodeInfoById(int i);

    public static native boolean onAccessibilityEvent(int i, int i2);

    static {
        System.loadLibrary("barrierfree_native.z");
    }

    private AccessibilityNativeTools() {
    }
}
