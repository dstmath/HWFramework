package android.view;

import android.graphics.Rect;
import huawei.cust.HwCfgFilePolicy;

public class TouchDelegate {
    public static final int ABOVE = 1;
    public static final int BELOW = 2;
    public static final int TO_LEFT = 4;
    public static final int TO_RIGHT = 8;
    private Rect mBounds;
    private boolean mDelegateTargeted;
    private View mDelegateView;
    private int mSlop;
    private Rect mSlopBounds;

    public TouchDelegate(Rect bounds, View delegateView) {
        this.mBounds = bounds;
        this.mSlop = ViewConfiguration.get(delegateView.getContext()).getScaledTouchSlop();
        this.mSlopBounds = new Rect(bounds);
        this.mSlopBounds.inset(-this.mSlop, -this.mSlop);
        this.mDelegateView = delegateView;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean sendToDelegate = false;
        boolean hit = true;
        switch (event.getAction()) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                if (this.mBounds.contains(x, y)) {
                    this.mDelegateTargeted = true;
                    sendToDelegate = true;
                    break;
                }
                break;
            case ABOVE /*1*/:
            case BELOW /*2*/:
                sendToDelegate = this.mDelegateTargeted;
                if (sendToDelegate && !this.mSlopBounds.contains(x, y)) {
                    hit = false;
                    break;
                }
            case HwCfgFilePolicy.BASE /*3*/:
                sendToDelegate = this.mDelegateTargeted;
                this.mDelegateTargeted = false;
                break;
        }
        if (!sendToDelegate) {
            return false;
        }
        View delegateView = this.mDelegateView;
        if (hit) {
            event.setLocation((float) (delegateView.getWidth() / BELOW), (float) (delegateView.getHeight() / BELOW));
        } else {
            int slop = this.mSlop;
            event.setLocation((float) (-(slop * BELOW)), (float) (-(slop * BELOW)));
        }
        return delegateView.dispatchTouchEvent(event);
    }
}
