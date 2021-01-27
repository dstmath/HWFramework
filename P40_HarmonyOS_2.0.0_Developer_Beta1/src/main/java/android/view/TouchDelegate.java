package android.view;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.ArrayMap;
import android.view.accessibility.AccessibilityNodeInfo;

public class TouchDelegate {
    public static final int ABOVE = 1;
    public static final int BELOW = 2;
    public static final int TO_LEFT = 4;
    public static final int TO_RIGHT = 8;
    private Rect mBounds;
    @UnsupportedAppUsage
    private boolean mDelegateTargeted;
    private View mDelegateView;
    private int mSlop;
    private Rect mSlopBounds;
    private AccessibilityNodeInfo.TouchDelegateInfo mTouchDelegateInfo;

    public TouchDelegate(Rect bounds, View delegateView) {
        this.mBounds = bounds;
        this.mSlop = ViewConfiguration.get(delegateView.getContext()).getScaledTouchSlop();
        this.mSlopBounds = new Rect(bounds);
        Rect rect = this.mSlopBounds;
        int i = this.mSlop;
        rect.inset(-i, -i);
        this.mDelegateView = delegateView;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
        if (r5 != 6) goto L_0x0042;
     */
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean sendToDelegate = false;
        boolean hit = true;
        int actionMasked = event.getActionMasked();
        if (actionMasked != 0) {
            if (!(actionMasked == 1 || actionMasked == 2)) {
                if (actionMasked == 3) {
                    sendToDelegate = this.mDelegateTargeted;
                    this.mDelegateTargeted = false;
                } else if (actionMasked != 5) {
                }
            }
            sendToDelegate = this.mDelegateTargeted;
            if (sendToDelegate && !this.mSlopBounds.contains(x, y)) {
                hit = false;
            }
        } else {
            this.mDelegateTargeted = this.mBounds.contains(x, y);
            sendToDelegate = this.mDelegateTargeted;
        }
        if (!sendToDelegate) {
            return false;
        }
        if (hit) {
            event.setLocation((float) (this.mDelegateView.getWidth() / 2), (float) (this.mDelegateView.getHeight() / 2));
        } else {
            int slop = this.mSlop;
            event.setLocation((float) (-(slop * 2)), (float) (-(slop * 2)));
        }
        return this.mDelegateView.dispatchTouchEvent(event);
    }

    public boolean onTouchExplorationHoverEvent(MotionEvent event) {
        if (this.mBounds == null) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean hit = true;
        boolean isInbound = this.mBounds.contains(x, y);
        int actionMasked = event.getActionMasked();
        if (actionMasked != 7) {
            if (actionMasked == 9) {
                this.mDelegateTargeted = isInbound;
            } else if (actionMasked == 10) {
                this.mDelegateTargeted = true;
            }
        } else if (isInbound) {
            this.mDelegateTargeted = true;
        } else if (this.mDelegateTargeted && !this.mSlopBounds.contains(x, y)) {
            hit = false;
        }
        if (!this.mDelegateTargeted) {
            return false;
        }
        if (hit) {
            event.setLocation((float) (this.mDelegateView.getWidth() / 2), (float) (this.mDelegateView.getHeight() / 2));
        } else {
            this.mDelegateTargeted = false;
        }
        return this.mDelegateView.dispatchHoverEvent(event);
    }

    public AccessibilityNodeInfo.TouchDelegateInfo getTouchDelegateInfo() {
        if (this.mTouchDelegateInfo == null) {
            ArrayMap<Region, View> targetMap = new ArrayMap<>(1);
            Rect bounds = this.mBounds;
            if (bounds == null) {
                bounds = new Rect();
            }
            targetMap.put(new Region(bounds), this.mDelegateView);
            this.mTouchDelegateInfo = new AccessibilityNodeInfo.TouchDelegateInfo(targetMap);
        }
        return this.mTouchDelegateInfo;
    }
}
