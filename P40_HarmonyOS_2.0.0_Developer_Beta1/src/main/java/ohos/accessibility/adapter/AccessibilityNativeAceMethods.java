package ohos.accessibility.adapter;

public class AccessibilityNativeAceMethods {
    public static native AccessibilityViewInfo getAccessibilityViewInfoById(int i);

    public static native boolean performAction(int i, int i2);

    static {
        System.loadLibrary("barrierfree_native_ace.z");
    }

    private AccessibilityNativeAceMethods() {
    }
}
