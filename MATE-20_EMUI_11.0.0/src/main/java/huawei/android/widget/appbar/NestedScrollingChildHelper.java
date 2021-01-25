package huawei.android.widget.appbar;

import android.view.View;
import android.view.ViewParent;

public class NestedScrollingChildHelper {
    private static final int CONSUMED_POINTS_LENGTH = 2;
    private boolean mIsNestedScrollingEnabled;
    private ViewParent mNestedScrollingParentNonTouch;
    private ViewParent mNestedScrollingParentTouch;
    private int[] mTempNestedScrollConsumedPoints;
    private final View mView;

    public NestedScrollingChildHelper(View view) {
        this.mView = view;
    }

    public void setNestedScrollingEnabled(boolean isEnabled) {
        if (this.mIsNestedScrollingEnabled) {
            this.mView.stopNestedScroll();
        }
        this.mIsNestedScrollingEnabled = isEnabled;
    }

    public boolean isNestedScrollingEnabled() {
        return this.mIsNestedScrollingEnabled;
    }

    public boolean hasNestedScrollingParent() {
        return hasNestedScrollingParent(0);
    }

    public boolean hasNestedScrollingParent(int type) {
        return getNestedScrollingParentForType(type) != null;
    }

    public boolean startNestedScroll(int axes) {
        return startNestedScroll(axes, 0);
    }

    public boolean startNestedScroll(int axes, int type) {
        if (hasNestedScrollingParent(type)) {
            return true;
        }
        if (!isNestedScrollingEnabled()) {
            return false;
        }
        View child = this.mView;
        for (ViewParent parent = this.mView.getParent(); parent != null; parent = parent.getParent()) {
            if (ViewParentCompat.onStartNestedScroll(parent, child, this.mView, axes, type)) {
                setNestedScrollingParentForType(type, parent);
                ViewParentCompat.onNestedScrollAccepted(parent, child, this.mView, axes, type);
                return true;
            }
            if (parent instanceof View) {
                child = (View) parent;
            }
        }
        return false;
    }

    public void stopNestedScroll() {
        stopNestedScroll(0);
    }

    public void stopNestedScroll(int type) {
        ViewParent parent = getNestedScrollingParentForType(type);
        if (parent != null) {
            ViewParentCompat.onStopNestedScroll(parent, this.mView, type);
            setNestedScrollingParentForType(type, null);
        }
    }

    public boolean dispatchNestedScroll(int consumedDeltaX, int consumedDeltaY, int unconsumedDeltaX, int unconsumedDeltaY, int[] offsetsInWindows) {
        return dispatchNestedScroll(consumedDeltaX, consumedDeltaY, unconsumedDeltaX, unconsumedDeltaY, offsetsInWindows, 0);
    }

    public boolean dispatchNestedScroll(int consumedDeltaX, int consumedDeltaY, int unconsumedDeltaX, int unconsumedDeltaY, int[] offsetsInWindows, int type) {
        ViewParent parent;
        int startY;
        int startX;
        if (!isNestedScrollingEnabled() || (parent = getNestedScrollingParentForType(type)) == null) {
            return false;
        }
        if (((consumedDeltaX == 0 && consumedDeltaY == 0 && unconsumedDeltaX == 0) ? false : true) || unconsumedDeltaY != 0) {
            if (offsetsInWindows != null) {
                this.mView.getLocationInWindow(offsetsInWindows);
                startX = offsetsInWindows[0];
                startY = offsetsInWindows[1];
            } else {
                startX = 0;
                startY = 0;
            }
            ViewParentCompat.onNestedScroll(parent, this.mView, consumedDeltaX, consumedDeltaY, unconsumedDeltaX, unconsumedDeltaY, type);
            if (offsetsInWindows != null) {
                this.mView.getLocationInWindow(offsetsInWindows);
                offsetsInWindows[0] = offsetsInWindows[0] - startX;
                offsetsInWindows[1] = offsetsInWindows[1] - startY;
            }
            return true;
        }
        if (offsetsInWindows != null) {
            offsetsInWindows[0] = 0;
            offsetsInWindows[1] = 0;
        }
        return false;
    }

    public boolean dispatchNestedPreScroll(int deltaX, int deltaY, int[] consumeArrays, int[] offsetsInWindows) {
        return dispatchNestedPreScroll(deltaX, deltaY, consumeArrays, offsetsInWindows, 0);
    }

    public boolean dispatchNestedPreScroll(int deltaX, int deltaY, int[] consumeArrays, int[] offsetsInWindows, int type) {
        ViewParent parent;
        int startY;
        int startX;
        int[] myConsumedPoints = consumeArrays;
        if (!isNestedScrollingEnabled() || (parent = getNestedScrollingParentForType(type)) == null) {
            return false;
        }
        if (deltaX == 0 && deltaY == 0) {
            if (offsetsInWindows != null) {
                offsetsInWindows[0] = 0;
                offsetsInWindows[1] = 0;
            }
            return false;
        }
        if (offsetsInWindows != null) {
            this.mView.getLocationInWindow(offsetsInWindows);
            startX = offsetsInWindows[0];
            startY = offsetsInWindows[1];
        } else {
            startX = 0;
            startY = 0;
        }
        if (myConsumedPoints == null) {
            int[] iArr = this.mTempNestedScrollConsumedPoints;
            if (iArr == null) {
                iArr = new int[2];
            }
            this.mTempNestedScrollConsumedPoints = iArr;
            myConsumedPoints = this.mTempNestedScrollConsumedPoints;
        }
        myConsumedPoints[0] = 0;
        myConsumedPoints[1] = 0;
        ViewParentCompat.onNestedPreScroll(parent, this.mView, deltaX, deltaY, myConsumedPoints, type);
        if (offsetsInWindows != null) {
            this.mView.getLocationInWindow(offsetsInWindows);
            offsetsInWindows[0] = offsetsInWindows[0] - startX;
            offsetsInWindows[1] = offsetsInWindows[1] - startY;
        }
        if (myConsumedPoints[0] == 0 && myConsumedPoints[1] == 0) {
            return false;
        }
        return true;
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean isConsumed) {
        ViewParent parent;
        if (!isNestedScrollingEnabled() || (parent = getNestedScrollingParentForType(0)) == null) {
            return false;
        }
        return ViewParentCompat.onNestedFling(parent, this.mView, velocityX, velocityY, isConsumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        ViewParent parent;
        if (!isNestedScrollingEnabled() || (parent = getNestedScrollingParentForType(0)) == null) {
            return false;
        }
        return ViewParentCompat.onNestedPreFling(parent, this.mView, velocityX, velocityY);
    }

    public void onDetachedFromWindow() {
        this.mView.stopNestedScroll();
    }

    public void onStopNestedScroll(View child) {
        this.mView.stopNestedScroll();
    }

    private ViewParent getNestedScrollingParentForType(int type) {
        if (type == 0) {
            return this.mNestedScrollingParentTouch;
        }
        if (type != 1) {
            return null;
        }
        return this.mNestedScrollingParentNonTouch;
    }

    private void setNestedScrollingParentForType(int type, ViewParent viewParent) {
        if (type == 0) {
            this.mNestedScrollingParentTouch = viewParent;
        } else if (type == 1) {
            this.mNestedScrollingParentNonTouch = viewParent;
        }
    }
}
