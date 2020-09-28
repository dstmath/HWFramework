package android.view;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class DefaultHwViewRootImpl implements IHwViewRootImpl {
    private static DefaultHwViewRootImpl mInstance;

    public static synchronized IHwViewRootImpl getDefault() {
        DefaultHwViewRootImpl defaultHwViewRootImpl;
        synchronized (DefaultHwViewRootImpl.class) {
            if (mInstance == null) {
                mInstance = new DefaultHwViewRootImpl();
            }
            defaultHwViewRootImpl = mInstance;
        }
        return defaultHwViewRootImpl;
    }

    @Override // android.view.IHwViewRootImpl
    public void setRealSize(Point point) {
    }

    @Override // android.view.IHwViewRootImpl
    public void clearDisplayPoint() {
    }

    @Override // android.view.IHwViewRootImpl
    public boolean filterDecorPointerEvent(Context context, MotionEvent event, int action, WindowManager.LayoutParams windowattr, Display disp) {
        return false;
    }

    @Override // android.view.IHwViewRootImpl
    public MotionEvent getRedispatchEvent() {
        return null;
    }

    @Override // android.view.IHwViewRootImpl
    public boolean shouldQueueInputEvent(InputEvent event, Context context, View view, WindowManager.LayoutParams attr) {
        return false;
    }

    @Override // android.view.IHwViewRootImpl
    public boolean interceptMotionEvent(View view, MotionEvent event) {
        return false;
    }

    @Override // android.view.IHwViewRootImpl
    public void setIsFirstFrame(boolean isFirstFrame) {
    }

    @Override // android.view.IHwViewRootImpl
    public void setIsNeedDraw(boolean isNeedDraw) {
    }

    @Override // android.view.IHwViewRootImpl
    public void processJank(boolean scroll, long[] jankdrawdata, String windowtitle, int windowtype) {
    }

    @Override // android.view.IHwViewRootImpl
    public void onBatchedInputConsumed(long starttime) {
    }

    @Override // android.view.IHwViewRootImpl
    public void onChgCallBackCountsChanged(int changes) {
    }

    @Override // android.view.IHwViewRootImpl
    public void updateDoframeStatus(boolean indoframe) {
    }

    @Override // android.view.IHwViewRootImpl
    public void setLastFrameDoneTime(long time) {
    }

    @Override // android.view.IHwViewRootImpl
    public void setRealFrameTime(long time) {
    }

    @Override // android.view.IHwViewRootImpl
    public void setFrameDelayTime(long time) {
    }

    @Override // android.view.IHwViewRootImpl
    public void updateLastTraversal(boolean status) {
    }

    @Override // android.view.IHwViewRootImpl
    public void updateOldestInputTime(long time) {
    }

    @Override // android.view.IHwViewRootImpl
    public void checkOldestInputTime() {
    }

    @Override // android.view.IHwViewRootImpl
    public void traceInputEventInfo(InputEvent event) {
    }
}
