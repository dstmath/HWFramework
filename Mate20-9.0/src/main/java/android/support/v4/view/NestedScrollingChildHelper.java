package android.support.v4.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewParent;

public class NestedScrollingChildHelper {
    private boolean mIsNestedScrollingEnabled;
    private ViewParent mNestedScrollingParentNonTouch;
    private ViewParent mNestedScrollingParentTouch;
    private int[] mTempNestedScrollConsumed;
    private final View mView;

    public NestedScrollingChildHelper(@NonNull View view) {
        this.mView = view;
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        if (this.mIsNestedScrollingEnabled) {
            ViewCompat.stopNestedScroll(this.mView);
        }
        this.mIsNestedScrollingEnabled = enabled;
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
        if (isNestedScrollingEnabled()) {
            View child = this.mView;
            for (ViewParent p = this.mView.getParent(); p != null; p = p.getParent()) {
                if (ViewParentCompat.onStartNestedScroll(p, child, this.mView, axes, type)) {
                    setNestedScrollingParentForType(type, p);
                    ViewParentCompat.onNestedScrollAccepted(p, child, this.mView, axes, type);
                    return true;
                }
                if (p instanceof View) {
                    child = (View) p;
                }
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

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, 0);
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        int[] iArr = offsetInWindow;
        if (isNestedScrollingEnabled()) {
            int i = type;
            ViewParent parent = getNestedScrollingParentForType(i);
            if (parent == null) {
                return false;
            }
            if (dxConsumed != 0 || dyConsumed != 0 || dxUnconsumed != 0 || dyUnconsumed != 0) {
                int startX = 0;
                int startY = 0;
                if (iArr != null) {
                    this.mView.getLocationInWindow(iArr);
                    startX = iArr[0];
                    startY = iArr[1];
                }
                int startX2 = startX;
                int startY2 = startY;
                ViewParentCompat.onNestedScroll(parent, this.mView, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, i);
                if (iArr != null) {
                    this.mView.getLocationInWindow(iArr);
                    iArr[0] = iArr[0] - startX2;
                    iArr[1] = iArr[1] - startY2;
                }
                return true;
            } else if (iArr != null) {
                iArr[0] = 0;
                iArr[1] = 0;
            }
        } else {
            int i2 = type;
        }
        return false;
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, 0);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        int[] consumed2;
        int[] iArr = offsetInWindow;
        if (isNestedScrollingEnabled()) {
            int i = type;
            ViewParent parent = getNestedScrollingParentForType(i);
            if (parent == null) {
                return false;
            }
            boolean z = true;
            if (dx != 0 || dy != 0) {
                int startX = 0;
                int startY = 0;
                if (iArr != null) {
                    this.mView.getLocationInWindow(iArr);
                    startX = iArr[0];
                    startY = iArr[1];
                }
                int startX2 = startX;
                int startY2 = startY;
                if (consumed == null) {
                    if (this.mTempNestedScrollConsumed == null) {
                        this.mTempNestedScrollConsumed = new int[2];
                    }
                    consumed2 = this.mTempNestedScrollConsumed;
                } else {
                    consumed2 = consumed;
                }
                consumed2[0] = 0;
                consumed2[1] = 0;
                ViewParentCompat.onNestedPreScroll(parent, this.mView, dx, dy, consumed2, i);
                if (iArr != null) {
                    this.mView.getLocationInWindow(iArr);
                    iArr[0] = iArr[0] - startX2;
                    iArr[1] = iArr[1] - startY2;
                }
                if (consumed2[0] == 0 && consumed2[1] == 0) {
                    z = false;
                }
                return z;
            } else if (iArr != null) {
                iArr[0] = 0;
                iArr[1] = 0;
            }
        } else {
            int i2 = type;
        }
        return false;
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        if (isNestedScrollingEnabled()) {
            ViewParent parent = getNestedScrollingParentForType(0);
            if (parent != null) {
                return ViewParentCompat.onNestedFling(parent, this.mView, velocityX, velocityY, consumed);
            }
        }
        return false;
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        if (isNestedScrollingEnabled()) {
            ViewParent parent = getNestedScrollingParentForType(0);
            if (parent != null) {
                return ViewParentCompat.onNestedPreFling(parent, this.mView, velocityX, velocityY);
            }
        }
        return false;
    }

    public void onDetachedFromWindow() {
        ViewCompat.stopNestedScroll(this.mView);
    }

    public void onStopNestedScroll(@NonNull View child) {
        ViewCompat.stopNestedScroll(this.mView);
    }

    private ViewParent getNestedScrollingParentForType(int type) {
        switch (type) {
            case 0:
                return this.mNestedScrollingParentTouch;
            case 1:
                return this.mNestedScrollingParentNonTouch;
            default:
                return null;
        }
    }

    private void setNestedScrollingParentForType(int type, ViewParent p) {
        switch (type) {
            case 0:
                this.mNestedScrollingParentTouch = p;
                return;
            case 1:
                this.mNestedScrollingParentNonTouch = p;
                return;
            default:
                return;
        }
    }
}
