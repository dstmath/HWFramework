package ohos.accessibility.remoteadapter;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.graphics.Region;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import ohos.rpc.IRemoteBroker;

public interface IBarrierfreeManager extends IRemoteBroker {
    void clearAccessibilityCache();

    void onAccessibilityButtonAvailabilityChanged(boolean z);

    void onAccessibilityButtonClicked();

    void onAccessibilityEvent(AccessibilityEvent accessibilityEvent, boolean z);

    void onFingerprintCapturingGesturesChanged(boolean z);

    void onFingerprintGesture(int i);

    void onGesture(int i);

    void onInterrupt();

    void onMagnificationChanged(int i, Region region, float f, float f2, float f3);

    void onPerformGestureResult(int i, boolean z);

    void onSoftKeyboardShowModeChanged(int i);

    void startInit(IAccessibilityServiceConnection iAccessibilityServiceConnection, int i);

    void startOnKeyEvent(KeyEvent keyEvent, int i);
}
