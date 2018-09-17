package android.view;

import android.view.Window.Callback;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import java.util.List;

public class WindowCallbackWrapper implements Callback {
    private Callback mWrapped;

    public WindowCallbackWrapper(Callback wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Window callback may not be null");
        }
        this.mWrapped = wrapped;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return this.mWrapped.dispatchKeyEvent(event);
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return this.mWrapped.dispatchKeyShortcutEvent(event);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        return this.mWrapped.dispatchTouchEvent(event);
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        return this.mWrapped.dispatchTrackballEvent(event);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return this.mWrapped.dispatchGenericMotionEvent(event);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return this.mWrapped.dispatchPopulateAccessibilityEvent(event);
    }

    public View onCreatePanelView(int featureId) {
        return this.mWrapped.onCreatePanelView(featureId);
    }

    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return this.mWrapped.onCreatePanelMenu(featureId, menu);
    }

    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return this.mWrapped.onPreparePanel(featureId, view, menu);
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        return this.mWrapped.onMenuOpened(featureId, menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return this.mWrapped.onMenuItemSelected(featureId, item);
    }

    public void onWindowAttributesChanged(LayoutParams attrs) {
        this.mWrapped.onWindowAttributesChanged(attrs);
    }

    public void onContentChanged() {
        this.mWrapped.onContentChanged();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        this.mWrapped.onWindowFocusChanged(hasFocus);
    }

    public void onAttachedToWindow() {
        this.mWrapped.onAttachedToWindow();
    }

    public void onDetachedFromWindow() {
        this.mWrapped.onDetachedFromWindow();
    }

    public void onPanelClosed(int featureId, Menu menu) {
        this.mWrapped.onPanelClosed(featureId, menu);
    }

    public boolean onSearchRequested(SearchEvent searchEvent) {
        return this.mWrapped.onSearchRequested(searchEvent);
    }

    public boolean onSearchRequested() {
        return this.mWrapped.onSearchRequested();
    }

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return this.mWrapped.onWindowStartingActionMode(callback);
    }

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return this.mWrapped.onWindowStartingActionMode(callback, type);
    }

    public void onActionModeStarted(ActionMode mode) {
        this.mWrapped.onActionModeStarted(mode);
    }

    public void onActionModeFinished(ActionMode mode) {
        this.mWrapped.onActionModeFinished(mode);
    }

    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        this.mWrapped.onProvideKeyboardShortcuts(data, menu, deviceId);
    }

    public void onPointerCaptureChanged(boolean hasCapture) {
        this.mWrapped.onPointerCaptureChanged(hasCapture);
    }
}
