package android.view;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.IBinder;
import android.view.animation.Animation;
import java.util.List;

public abstract class WindowManagerInternal {

    public static abstract class AppTransitionListener {
        public void onAppTransitionPendingLocked() {
        }

        public void onAppTransitionCancelledLocked() {
        }

        public void onAppTransitionStartingLocked(IBinder openToken, IBinder closeToken, Animation openAnimation, Animation closeAnimation) {
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
        }
    }

    public interface MagnificationCallbacks {
        void onMagnificationRegionChanged(Region region);

        void onRectangleOnScreenRequested(int i, int i2, int i3, int i4);

        void onRotationChanged(int i);

        void onUserContextChanged();
    }

    public interface OnHardKeyboardStatusChangeListener {
        void onHardKeyboardStatusChange(boolean z);
    }

    public interface WindowsForAccessibilityCallback {
        void onWindowsForAccessibilityChanged(List<WindowInfo> list);
    }

    public abstract void addWindowToken(IBinder iBinder, int i);

    public abstract void clearLastInputMethodWindowForTransition();

    public abstract MagnificationSpec getCompatibleMagnificationSpecForWindow(IBinder iBinder);

    public abstract IBinder getFocusedWindowToken();

    public abstract int getInputMethodWindowVisibleHeight();

    public abstract void getMagnificationRegion(Region region);

    public abstract void getWindowFrame(IBinder iBinder, Rect rect);

    public abstract boolean isDockedDividerResizing();

    public abstract boolean isHardKeyboardAvailable();

    public abstract boolean isKeyguardLocked();

    public abstract boolean isStackVisible(int i);

    public abstract void registerAppTransitionListener(AppTransitionListener appTransitionListener);

    public abstract void removeWindowToken(IBinder iBinder, boolean z);

    public abstract void requestTraversalFromDisplayManager();

    public abstract void saveLastInputMethodWindowForTransition();

    public abstract void setDockedStackDividerRotation(int i);

    public abstract void setInputFilter(IInputFilter iInputFilter);

    public abstract void setMagnificationCallbacks(MagnificationCallbacks magnificationCallbacks);

    public abstract void setMagnificationSpec(MagnificationSpec magnificationSpec);

    public abstract void setOnHardKeyboardStatusChangeListener(OnHardKeyboardStatusChangeListener onHardKeyboardStatusChangeListener);

    public abstract void setWindowsForAccessibilityCallback(WindowsForAccessibilityCallback windowsForAccessibilityCallback);

    public abstract void showGlobalActions();

    public abstract void waitForAllWindowsDrawn(Runnable runnable, long j);

    public abstract void waitForKeyguardDismissDone(Runnable runnable, long j);
}
