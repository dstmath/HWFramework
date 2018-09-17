package android.support.v4.view;

import android.os.Build.VERSION;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;

public final class ViewParentCompat {
    static final ViewParentCompatBaseImpl IMPL;
    private static final String TAG = "ViewParentCompat";

    static class ViewParentCompatBaseImpl {
        ViewParentCompatBaseImpl() {
        }

        public boolean onStartNestedScroll(ViewParent parent, View child, View target, int nestedScrollAxes) {
            if (parent instanceof NestedScrollingParent) {
                return ((NestedScrollingParent) parent).onStartNestedScroll(child, target, nestedScrollAxes);
            }
            return false;
        }

        public void onNestedScrollAccepted(ViewParent parent, View child, View target, int nestedScrollAxes) {
            if (parent instanceof NestedScrollingParent) {
                ((NestedScrollingParent) parent).onNestedScrollAccepted(child, target, nestedScrollAxes);
            }
        }

        public void onStopNestedScroll(ViewParent parent, View target) {
            if (parent instanceof NestedScrollingParent) {
                ((NestedScrollingParent) parent).onStopNestedScroll(target);
            }
        }

        public void onNestedScroll(ViewParent parent, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            if (parent instanceof NestedScrollingParent) {
                ((NestedScrollingParent) parent).onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
            }
        }

        public void onNestedPreScroll(ViewParent parent, View target, int dx, int dy, int[] consumed) {
            if (parent instanceof NestedScrollingParent) {
                ((NestedScrollingParent) parent).onNestedPreScroll(target, dx, dy, consumed);
            }
        }

        public boolean onNestedFling(ViewParent parent, View target, float velocityX, float velocityY, boolean consumed) {
            if (parent instanceof NestedScrollingParent) {
                return ((NestedScrollingParent) parent).onNestedFling(target, velocityX, velocityY, consumed);
            }
            return false;
        }

        public boolean onNestedPreFling(ViewParent parent, View target, float velocityX, float velocityY) {
            if (parent instanceof NestedScrollingParent) {
                return ((NestedScrollingParent) parent).onNestedPreFling(target, velocityX, velocityY);
            }
            return false;
        }

        public void notifySubtreeAccessibilityStateChanged(ViewParent parent, View child, View source, int changeType) {
        }
    }

    @RequiresApi(19)
    static class ViewParentCompatApi19Impl extends ViewParentCompatBaseImpl {
        ViewParentCompatApi19Impl() {
        }

        public void notifySubtreeAccessibilityStateChanged(ViewParent parent, View child, View source, int changeType) {
            parent.notifySubtreeAccessibilityStateChanged(child, source, changeType);
        }
    }

    @RequiresApi(21)
    static class ViewParentCompatApi21Impl extends ViewParentCompatApi19Impl {
        ViewParentCompatApi21Impl() {
        }

        public boolean onStartNestedScroll(ViewParent parent, View child, View target, int nestedScrollAxes) {
            try {
                return parent.onStartNestedScroll(child, target, nestedScrollAxes);
            } catch (AbstractMethodError e) {
                Log.e(ViewParentCompat.TAG, "ViewParent " + parent + " does not implement interface " + "method onStartNestedScroll", e);
                return false;
            }
        }

        public void onNestedScrollAccepted(ViewParent parent, View child, View target, int nestedScrollAxes) {
            try {
                parent.onNestedScrollAccepted(child, target, nestedScrollAxes);
            } catch (AbstractMethodError e) {
                Log.e(ViewParentCompat.TAG, "ViewParent " + parent + " does not implement interface " + "method onNestedScrollAccepted", e);
            }
        }

        public void onStopNestedScroll(ViewParent parent, View target) {
            try {
                parent.onStopNestedScroll(target);
            } catch (AbstractMethodError e) {
                Log.e(ViewParentCompat.TAG, "ViewParent " + parent + " does not implement interface " + "method onStopNestedScroll", e);
            }
        }

        public void onNestedScroll(ViewParent parent, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            try {
                parent.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
            } catch (AbstractMethodError e) {
                Log.e(ViewParentCompat.TAG, "ViewParent " + parent + " does not implement interface " + "method onNestedScroll", e);
            }
        }

        public void onNestedPreScroll(ViewParent parent, View target, int dx, int dy, int[] consumed) {
            try {
                parent.onNestedPreScroll(target, dx, dy, consumed);
            } catch (AbstractMethodError e) {
                Log.e(ViewParentCompat.TAG, "ViewParent " + parent + " does not implement interface " + "method onNestedPreScroll", e);
            }
        }

        public boolean onNestedFling(ViewParent parent, View target, float velocityX, float velocityY, boolean consumed) {
            try {
                return parent.onNestedFling(target, velocityX, velocityY, consumed);
            } catch (AbstractMethodError e) {
                Log.e(ViewParentCompat.TAG, "ViewParent " + parent + " does not implement interface " + "method onNestedFling", e);
                return false;
            }
        }

        public boolean onNestedPreFling(ViewParent parent, View target, float velocityX, float velocityY) {
            try {
                return parent.onNestedPreFling(target, velocityX, velocityY);
            } catch (AbstractMethodError e) {
                Log.e(ViewParentCompat.TAG, "ViewParent " + parent + " does not implement interface " + "method onNestedPreFling", e);
                return false;
            }
        }
    }

    static {
        if (VERSION.SDK_INT >= 21) {
            IMPL = new ViewParentCompatApi21Impl();
        } else if (VERSION.SDK_INT >= 19) {
            IMPL = new ViewParentCompatApi19Impl();
        } else {
            IMPL = new ViewParentCompatBaseImpl();
        }
    }

    private ViewParentCompat() {
    }

    @Deprecated
    public static boolean requestSendAccessibilityEvent(ViewParent parent, View child, AccessibilityEvent event) {
        return parent.requestSendAccessibilityEvent(child, event);
    }

    public static boolean onStartNestedScroll(ViewParent parent, View child, View target, int nestedScrollAxes) {
        return onStartNestedScroll(parent, child, target, nestedScrollAxes, 0);
    }

    public static void onNestedScrollAccepted(ViewParent parent, View child, View target, int nestedScrollAxes) {
        onNestedScrollAccepted(parent, child, target, nestedScrollAxes, 0);
    }

    public static void onStopNestedScroll(ViewParent parent, View target) {
        onStopNestedScroll(parent, target, 0);
    }

    public static void onNestedScroll(ViewParent parent, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(parent, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, 0);
    }

    public static void onNestedPreScroll(ViewParent parent, View target, int dx, int dy, int[] consumed) {
        onNestedPreScroll(parent, target, dx, dy, consumed, 0);
    }

    public static boolean onStartNestedScroll(ViewParent parent, View child, View target, int nestedScrollAxes, int type) {
        if (parent instanceof NestedScrollingParent2) {
            return ((NestedScrollingParent2) parent).onStartNestedScroll(child, target, nestedScrollAxes, type);
        }
        if (type == 0) {
            return IMPL.onStartNestedScroll(parent, child, target, nestedScrollAxes);
        }
        return false;
    }

    public static void onNestedScrollAccepted(ViewParent parent, View child, View target, int nestedScrollAxes, int type) {
        if (parent instanceof NestedScrollingParent2) {
            ((NestedScrollingParent2) parent).onNestedScrollAccepted(child, target, nestedScrollAxes, type);
        } else if (type == 0) {
            IMPL.onNestedScrollAccepted(parent, child, target, nestedScrollAxes);
        }
    }

    public static void onStopNestedScroll(ViewParent parent, View target, int type) {
        if (parent instanceof NestedScrollingParent2) {
            ((NestedScrollingParent2) parent).onStopNestedScroll(target, type);
        } else if (type == 0) {
            IMPL.onStopNestedScroll(parent, target);
        }
    }

    public static void onNestedScroll(ViewParent parent, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (parent instanceof NestedScrollingParent2) {
            ((NestedScrollingParent2) parent).onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        } else if (type == 0) {
            IMPL.onNestedScroll(parent, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        }
    }

    public static void onNestedPreScroll(ViewParent parent, View target, int dx, int dy, int[] consumed, int type) {
        if (parent instanceof NestedScrollingParent2) {
            ((NestedScrollingParent2) parent).onNestedPreScroll(target, dx, dy, consumed, type);
        } else if (type == 0) {
            IMPL.onNestedPreScroll(parent, target, dx, dy, consumed);
        }
    }

    public static boolean onNestedFling(ViewParent parent, View target, float velocityX, float velocityY, boolean consumed) {
        return IMPL.onNestedFling(parent, target, velocityX, velocityY, consumed);
    }

    public static boolean onNestedPreFling(ViewParent parent, View target, float velocityX, float velocityY) {
        return IMPL.onNestedPreFling(parent, target, velocityX, velocityY);
    }

    public static void notifySubtreeAccessibilityStateChanged(ViewParent parent, View child, View source, int changeType) {
        IMPL.notifySubtreeAccessibilityStateChanged(parent, child, source, changeType);
    }
}
