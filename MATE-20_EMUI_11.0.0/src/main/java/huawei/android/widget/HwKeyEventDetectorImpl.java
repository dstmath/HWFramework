package huawei.android.widget;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.HwKeyEventDetector;

public class HwKeyEventDetectorImpl implements HwKeyEventDetector {
    private static final String TAG = "HwKeyEventDetectorImpl";
    private HwOnEditEventListener mOnEditListener = null;
    private HwOnGlobalNextTabEventListener mOnGlobalNextTabListener = null;
    private HwOnNextTabEventListener mOnNextTabListener = null;
    private HwOnSearchEventListener mOnSearchListener = null;
    private View.OnUnhandledKeyEventListener mOnUnhandledKeyEventListener = null;
    private View mTargetView = null;

    public HwKeyEventDetectorImpl(Context context) {
    }

    public void setOnEditEventListener(HwOnEditEventListener listener) {
        this.mOnEditListener = listener;
    }

    public HwOnEditEventListener getOnEditEventListener() {
        return this.mOnEditListener;
    }

    public void setOnSearchEventListener(HwOnSearchEventListener listener) {
        this.mOnSearchListener = listener;
    }

    public HwOnSearchEventListener getOnSearchEventListener() {
        return this.mOnSearchListener;
    }

    public void setOnNextTabListener(HwOnNextTabEventListener listener) {
        this.mOnNextTabListener = listener;
    }

    public HwOnNextTabEventListener getOnNextTabListener() {
        return this.mOnNextTabListener;
    }

    public void setOnGlobalNextTabListener(View view, HwOnGlobalNextTabEventListener listener) {
        this.mTargetView = view;
        this.mOnGlobalNextTabListener = listener;
        if (listener != null) {
            unhandledKeyEventListenerProc(true);
        }
    }

    public HwOnGlobalNextTabEventListener getOnGlobalNextTabListener() {
        return this.mOnGlobalNextTabListener;
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
                    /* class huawei.android.widget.HwKeyEventDetectorImpl.AnonymousClass1 */

                    @Override // android.view.View.OnUnhandledKeyEventListener
                    public boolean onUnhandledKeyEvent(View view, KeyEvent event) {
                        return HwKeyEventDetectorImpl.this.onUnhandledKeyEvent(view, event);
                    }
                };
                this.mTargetView.addOnUnhandledKeyEventListener(this.mOnUnhandledKeyEventListener);
            }
        }
    }

    public void onDetachedFromWindow() {
        unhandledKeyEventListenerProc(false);
    }

    public boolean onKeyEvent(int keyCode, KeyEvent event) {
        int action = event.getAction();
        if (!event.isCtrlPressed()) {
            return onSingleKeyEvent(keyCode, action, event);
        }
        if (onEditKeyEventWithCtrl(keyCode, action, event)) {
            return true;
        }
        HwOnNextTabEventListener hwOnNextTabEventListener = this.mOnNextTabListener;
        if (hwOnNextTabEventListener != null && keyCode == 61 && hwOnNextTabEventListener.onNextTab(action, event)) {
            return true;
        }
        HwOnSearchEventListener hwOnSearchEventListener = this.mOnSearchListener;
        if (hwOnSearchEventListener == null || keyCode != 34 || !hwOnSearchEventListener.onSearch(action, event)) {
            return false;
        }
        return true;
    }

    private boolean onEditKeyEventWithCtrl(int keyCode, int action, KeyEvent event) {
        HwOnEditEventListener hwOnEditEventListener = this.mOnEditListener;
        if (hwOnEditEventListener == null) {
            return false;
        }
        if (keyCode != 29) {
            if (keyCode != 31) {
                if (keyCode != 50) {
                    if (keyCode != 52) {
                        if (keyCode == 54 && hwOnEditEventListener.onUndo(action, event)) {
                            return true;
                        }
                    } else if (hwOnEditEventListener.onCut(action, event)) {
                        return true;
                    }
                } else if (hwOnEditEventListener.onPaste(action, event)) {
                    return true;
                }
            } else if (hwOnEditEventListener.onCopy(action, event)) {
                return true;
            }
        } else if (hwOnEditEventListener.onSelectAll(action, event)) {
            return true;
        }
        return false;
    }

    private boolean onSingleKeyEvent(int keyCode, int action, KeyEvent event) {
        HwOnEditEventListener hwOnEditEventListener;
        if (keyCode != 112 || (hwOnEditEventListener = this.mOnEditListener) == null) {
            return false;
        }
        return hwOnEditEventListener.onDelete(action, event);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean onUnhandledKeyEvent(View view, KeyEvent event) {
        if (this.mOnGlobalNextTabListener != null && event.getKeyCode() == 61 && event.isCtrlPressed() && this.mOnGlobalNextTabListener.onGlobalNextTab(event.getAction(), event)) {
            return true;
        }
        return false;
    }
}
