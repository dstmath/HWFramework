package ohos.accessibility.adapter;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import ohos.accessibility.utils.LogUtil;

public class AdapterTouchDelegate extends TouchDelegate {
    private static final String TAG = "AdapterTouchDelegate";
    private BarrierFreeDelegateHelper mAccessibilityDelegate;
    private View mView;

    public AdapterTouchDelegate(Rect rect, View view, BarrierFreeDelegateHelper barrierFreeDelegateHelper) {
        super(rect, view);
        this.mView = view;
        this.mAccessibilityDelegate = barrierFreeDelegateHelper;
    }

    @Override // android.view.TouchDelegate
    public boolean onTouchEvent(MotionEvent motionEvent) {
        LogUtil.info(TAG, "get in onTouchEvent");
        return false;
    }

    @Override // android.view.TouchDelegate
    public boolean onTouchExplorationHoverEvent(MotionEvent motionEvent) {
        LogUtil.info(TAG, "onTouchExplorationHoverEvent start,");
        if (!(this.mView == null || this.mAccessibilityDelegate == null)) {
            Rect rect = new Rect();
            this.mView.getBoundsOnScreen(rect);
            if (rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                LogUtil.info(TAG, "onTouchExplorationHoverEvent in.");
                return this.mAccessibilityDelegate.dispatchHoverEvent(motionEvent);
            }
        }
        LogUtil.info(TAG, "onTouchExplorationHoverEvent end.");
        return false;
    }
}
