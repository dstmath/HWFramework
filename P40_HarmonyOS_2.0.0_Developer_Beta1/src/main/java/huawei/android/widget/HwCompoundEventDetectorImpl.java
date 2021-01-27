package huawei.android.widget;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HwCompoundEventDetector;

public class HwCompoundEventDetectorImpl implements HwCompoundEventDetector {
    private static final String TAG = "HwCompoundEventDetectorImpl";
    private HwOnMultiSelectListener mOnMultiSelectListener = null;
    private View.OnUnhandledKeyEventListener mOnUnhandledKeyEventListener = null;
    private HwOnZoomEventListener mOnZoomEventListener = null;
    private View mTargetView = null;

    public HwCompoundEventDetectorImpl(Context context) {
    }

    public void setOnZoomEventListener(View view, HwOnZoomEventListener listener) {
        this.mTargetView = view;
        this.mOnZoomEventListener = listener;
        if (listener != null) {
            unhandledKeyEventListenerProc(true);
        }
    }

    public HwOnZoomEventListener getOnZoomEventListener() {
        return this.mOnZoomEventListener;
    }

    public void setOnMultiSelectEventListener(View view, HwOnMultiSelectListener listener) {
        this.mTargetView = view;
        this.mOnMultiSelectListener = listener;
    }

    public HwOnMultiSelectListener getOnMultiSelectEventListener() {
        return this.mOnMultiSelectListener;
    }

    public boolean onKeyEvent(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!event.isFromSource(2)) {
            return false;
        }
        int action = event.getAction();
        if (!((event.getMetaState() & 4096) == 0 || this.mOnZoomEventListener == null || action != 8)) {
            float scroll = event.getAxisValue(10);
            if (Float.compare(scroll, 0.0f) == 0) {
                scroll = event.getAxisValue(9);
            }
            if (Float.compare(scroll, 0.0f) != 0 && this.mOnZoomEventListener.onZoom(scroll, event)) {
                return true;
            }
        }
        if (action == 11 && event.getActionButton() == 1 && this.mOnMultiSelectListener != null) {
            if ((event.getMetaState() & 4096) != 0 && this.mOnMultiSelectListener.onSelectDiscrete(event)) {
                return true;
            }
            if ((event.getMetaState() & 1) != 0 && this.mOnMultiSelectListener.onSelectContinuous(false, event)) {
                return true;
            }
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    private void unhandledKeyEventListenerProc(boolean isRegister) {
        if (Build.VERSION.SDK_INT < 28) {
            Log.w(TAG, "unhandledKeyEventListenerProc: need minimum sdk version 28.");
            return;
        }
        View view = this.mTargetView;
        if (view != null) {
            if (!isRegister) {
                View.OnUnhandledKeyEventListener onUnhandledKeyEventListener = this.mOnUnhandledKeyEventListener;
                if (onUnhandledKeyEventListener != null) {
                    view.removeOnUnhandledKeyEventListener(onUnhandledKeyEventListener);
                    this.mOnUnhandledKeyEventListener = null;
                }
            } else if (this.mOnUnhandledKeyEventListener == null) {
                this.mOnUnhandledKeyEventListener = new View.OnUnhandledKeyEventListener() {
                    /* class huawei.android.widget.HwCompoundEventDetectorImpl.AnonymousClass1 */

                    @Override // android.view.View.OnUnhandledKeyEventListener
                    public boolean onUnhandledKeyEvent(View view, KeyEvent event) {
                        return HwCompoundEventDetectorImpl.this.onUnhandledKeyEvent(view, event);
                    }
                };
                this.mTargetView.addOnUnhandledKeyEventListener(this.mOnUnhandledKeyEventListener);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean onUnhandledKeyEvent(View view, KeyEvent event) {
        return onKeyEvent(event.getKeyCode(), event);
    }

    public void onDetachedFromWindow() {
        unhandledKeyEventListenerProc(false);
    }
}
