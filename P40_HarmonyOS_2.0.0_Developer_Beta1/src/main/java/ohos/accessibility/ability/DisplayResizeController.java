package ohos.accessibility.ability;

import java.util.ArrayList;
import java.util.List;
import ohos.accessibility.adapter.ability.AccessibleControlAdapter;
import ohos.accessibility.utils.LogUtil;
import ohos.agp.utils.Rect;

public class DisplayResizeController {
    private static final String TAG = "DisplayResizeController";
    private final int mConnectionId;
    private final int mDisplayId;
    private List<DisplayResizeListener> mListeners = new ArrayList();
    private Object mLock;

    public DisplayResizeController(int i, int i2, Object obj) {
        this.mConnectionId = i;
        this.mDisplayId = i2;
        this.mLock = obj;
    }

    public void addListener(DisplayResizeListener displayResizeListener) {
        synchronized (this.mLock) {
            if (this.mListeners.isEmpty()) {
                setDisplayResizeCallbackEnabled(true);
            }
            this.mListeners.add(displayResizeListener);
        }
    }

    public boolean deleteListener(DisplayResizeListener displayResizeListener) {
        boolean z = false;
        if (this.mListeners.isEmpty()) {
            return false;
        }
        synchronized (this.mLock) {
            int indexOf = this.mListeners.indexOf(displayResizeListener);
            if (indexOf >= 0) {
                this.mListeners.remove(indexOf);
            }
            if (indexOf >= 0 && this.mListeners.isEmpty()) {
                setDisplayResizeCallbackEnabled(false);
            }
            if (indexOf >= 0) {
                z = true;
            }
        }
        return z;
    }

    public float getScale() {
        return AccessibleControlAdapter.getDisplayResizeScale(this.mConnectionId, this.mDisplayId);
    }

    public float getCenterX() {
        int i = this.mConnectionId;
        return AccessibleControlAdapter.getDisplayResizeCenterX(i, i);
    }

    public float getCenterY() {
        return AccessibleControlAdapter.getDisplayResizeCenterY(this.mConnectionId, this.mDisplayId);
    }

    public boolean setScale(float f, boolean z) {
        return AccessibleControlAdapter.setScale(this.mConnectionId, this.mDisplayId, f, z);
    }

    public boolean setCenter(float f, float f2, boolean z) {
        return AccessibleControlAdapter.setCenter(this.mConnectionId, this.mDisplayId, f, f2, z);
    }

    public Rect getDisplayResizeRect() {
        return AccessibleControlAdapter.getDisplayResizeRect(this.mConnectionId, this.mDisplayId);
    }

    public boolean reset(boolean z) {
        return AccessibleControlAdapter.resetDisplayResizeToDefault(this.mConnectionId, this.mDisplayId, z);
    }

    /* access modifiers changed from: package-private */
    public void onServiceConnectedLocked() {
        if (!this.mListeners.isEmpty()) {
            setDisplayResizeCallbackEnabled(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchMagnificationChanged(Rect rect, float f, float f2, float f3) {
        if (this.mListeners.isEmpty()) {
            LogUtil.info(TAG, "Received magnification changed callback with no listeners registered!");
            setDisplayResizeCallbackEnabled(false);
            return;
        }
        for (DisplayResizeListener displayResizeListener : this.mListeners) {
            displayResizeListener.onDisplayResizeChanged(this, rect, f, f2, f3);
        }
    }

    private void setDisplayResizeCallbackEnabled(boolean z) {
        AccessibleControlAdapter.setDisplayResizeCallbackEnabled(this.mConnectionId, this.mDisplayId, z);
    }
}
