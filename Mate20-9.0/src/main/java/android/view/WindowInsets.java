package android.view;

import android.graphics.Rect;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public final class WindowInsets {
    public static final WindowInsets CONSUMED;
    private static final Rect EMPTY_RECT = new Rect(0, 0, 0, 0);
    private boolean mAlwaysConsumeNavBar;
    private DisplayCutout mDisplayCutout;
    private boolean mDisplayCutoutConsumed;
    private boolean mIsRound;
    private Rect mStableInsets;
    private boolean mStableInsetsConsumed;
    private Rect mSystemWindowInsets;
    private boolean mSystemWindowInsetsConsumed;
    private Rect mTempRect;
    private Rect mWindowDecorInsets;
    private boolean mWindowDecorInsetsConsumed;

    static {
        WindowInsets windowInsets = new WindowInsets(null, null, null, false, false, null);
        CONSUMED = windowInsets;
    }

    public WindowInsets(Rect systemWindowInsets, Rect windowDecorInsets, Rect stableInsets, boolean isRound, boolean alwaysConsumeNavBar, DisplayCutout displayCutout) {
        boolean z = false;
        this.mSystemWindowInsetsConsumed = false;
        this.mWindowDecorInsetsConsumed = false;
        this.mStableInsetsConsumed = false;
        this.mDisplayCutoutConsumed = false;
        this.mSystemWindowInsetsConsumed = systemWindowInsets == null;
        this.mSystemWindowInsets = this.mSystemWindowInsetsConsumed ? EMPTY_RECT : new Rect(systemWindowInsets);
        this.mWindowDecorInsetsConsumed = windowDecorInsets == null;
        this.mWindowDecorInsets = this.mWindowDecorInsetsConsumed ? EMPTY_RECT : new Rect(windowDecorInsets);
        this.mStableInsetsConsumed = stableInsets == null;
        this.mStableInsets = this.mStableInsetsConsumed ? EMPTY_RECT : new Rect(stableInsets);
        this.mIsRound = isRound;
        this.mAlwaysConsumeNavBar = alwaysConsumeNavBar;
        this.mDisplayCutoutConsumed = displayCutout == null ? true : z;
        this.mDisplayCutout = (this.mDisplayCutoutConsumed || displayCutout.isEmpty()) ? null : displayCutout;
    }

    public WindowInsets(WindowInsets src) {
        this.mSystemWindowInsetsConsumed = false;
        this.mWindowDecorInsetsConsumed = false;
        this.mStableInsetsConsumed = false;
        this.mDisplayCutoutConsumed = false;
        this.mSystemWindowInsets = src.mSystemWindowInsets;
        this.mWindowDecorInsets = src.mWindowDecorInsets;
        this.mStableInsets = src.mStableInsets;
        this.mSystemWindowInsetsConsumed = src.mSystemWindowInsetsConsumed;
        this.mWindowDecorInsetsConsumed = src.mWindowDecorInsetsConsumed;
        this.mStableInsetsConsumed = src.mStableInsetsConsumed;
        this.mIsRound = src.mIsRound;
        this.mAlwaysConsumeNavBar = src.mAlwaysConsumeNavBar;
        this.mDisplayCutout = src.mDisplayCutout;
        this.mDisplayCutoutConsumed = src.mDisplayCutoutConsumed;
    }

    public WindowInsets(Rect systemWindowInsets) {
        this(systemWindowInsets, null, null, false, false, null);
    }

    public Rect getSystemWindowInsets() {
        if (this.mTempRect == null) {
            this.mTempRect = new Rect();
        }
        if (this.mSystemWindowInsets != null) {
            this.mTempRect.set(this.mSystemWindowInsets);
        } else {
            this.mTempRect.setEmpty();
        }
        return this.mTempRect;
    }

    public int getSystemWindowInsetLeft() {
        return this.mSystemWindowInsets.left;
    }

    public int getSystemWindowInsetTop() {
        return this.mSystemWindowInsets.top;
    }

    public int getSystemWindowInsetRight() {
        return this.mSystemWindowInsets.right;
    }

    public int getSystemWindowInsetBottom() {
        return this.mSystemWindowInsets.bottom;
    }

    public int getWindowDecorInsetLeft() {
        return this.mWindowDecorInsets.left;
    }

    public int getWindowDecorInsetTop() {
        return this.mWindowDecorInsets.top;
    }

    public int getWindowDecorInsetRight() {
        return this.mWindowDecorInsets.right;
    }

    public int getWindowDecorInsetBottom() {
        return this.mWindowDecorInsets.bottom;
    }

    public boolean hasSystemWindowInsets() {
        return (this.mSystemWindowInsets.left == 0 && this.mSystemWindowInsets.top == 0 && this.mSystemWindowInsets.right == 0 && this.mSystemWindowInsets.bottom == 0) ? false : true;
    }

    public boolean hasWindowDecorInsets() {
        return (this.mWindowDecorInsets.left == 0 && this.mWindowDecorInsets.top == 0 && this.mWindowDecorInsets.right == 0 && this.mWindowDecorInsets.bottom == 0) ? false : true;
    }

    public boolean hasInsets() {
        return hasSystemWindowInsets() || hasWindowDecorInsets() || hasStableInsets() || this.mDisplayCutout != null;
    }

    public DisplayCutout getDisplayCutout() {
        return this.mDisplayCutout;
    }

    public WindowInsets consumeDisplayCutout() {
        WindowInsets result = new WindowInsets(this);
        result.mDisplayCutout = null;
        result.mDisplayCutoutConsumed = true;
        return result;
    }

    public boolean isConsumed() {
        return this.mSystemWindowInsetsConsumed && this.mWindowDecorInsetsConsumed && this.mStableInsetsConsumed && this.mDisplayCutoutConsumed;
    }

    public boolean isRound() {
        return this.mIsRound;
    }

    public WindowInsets consumeSystemWindowInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = EMPTY_RECT;
        result.mSystemWindowInsetsConsumed = true;
        return result;
    }

    public WindowInsets consumeSystemWindowInsets(boolean left, boolean top, boolean right, boolean bottom) {
        int i;
        int i2;
        int i3;
        if (!left && !top && !right && !bottom) {
            return this;
        }
        WindowInsets result = new WindowInsets(this);
        int i4 = 0;
        if (left) {
            i = 0;
        } else {
            i = this.mSystemWindowInsets.left;
        }
        if (top) {
            i2 = 0;
        } else {
            i2 = this.mSystemWindowInsets.top;
        }
        if (right) {
            i3 = 0;
        } else {
            i3 = this.mSystemWindowInsets.right;
        }
        if (!bottom) {
            i4 = this.mSystemWindowInsets.bottom;
        }
        result.mSystemWindowInsets = new Rect(i, i2, i3, i4);
        return result;
    }

    public WindowInsets replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = new Rect(left, top, right, bottom);
        return result;
    }

    public WindowInsets replaceSystemWindowInsets(Rect systemWindowInsets) {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = new Rect(systemWindowInsets);
        return result;
    }

    public WindowInsets consumeWindowDecorInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mWindowDecorInsets.set(0, 0, 0, 0);
        result.mWindowDecorInsetsConsumed = true;
        return result;
    }

    public WindowInsets consumeWindowDecorInsets(boolean left, boolean top, boolean right, boolean bottom) {
        int i;
        int i2;
        int i3;
        if (!left && !top && !right && !bottom) {
            return this;
        }
        WindowInsets result = new WindowInsets(this);
        int i4 = 0;
        if (left) {
            i = 0;
        } else {
            i = this.mWindowDecorInsets.left;
        }
        if (top) {
            i2 = 0;
        } else {
            i2 = this.mWindowDecorInsets.top;
        }
        if (right) {
            i3 = 0;
        } else {
            i3 = this.mWindowDecorInsets.right;
        }
        if (!bottom) {
            i4 = this.mWindowDecorInsets.bottom;
        }
        result.mWindowDecorInsets = new Rect(i, i2, i3, i4);
        return result;
    }

    public WindowInsets replaceWindowDecorInsets(int left, int top, int right, int bottom) {
        WindowInsets result = new WindowInsets(this);
        result.mWindowDecorInsets = new Rect(left, top, right, bottom);
        return result;
    }

    public int getStableInsetTop() {
        return this.mStableInsets.top;
    }

    public int getStableInsetLeft() {
        return this.mStableInsets.left;
    }

    public int getStableInsetRight() {
        return this.mStableInsets.right;
    }

    public int getStableInsetBottom() {
        return this.mStableInsets.bottom;
    }

    public boolean hasStableInsets() {
        return (this.mStableInsets.top == 0 && this.mStableInsets.left == 0 && this.mStableInsets.right == 0 && this.mStableInsets.bottom == 0) ? false : true;
    }

    public WindowInsets consumeStableInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mStableInsets = EMPTY_RECT;
        result.mStableInsetsConsumed = true;
        return result;
    }

    public boolean shouldAlwaysConsumeNavBar() {
        return this.mAlwaysConsumeNavBar;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("WindowInsets{systemWindowInsets=");
        sb.append(this.mSystemWindowInsets);
        sb.append(" windowDecorInsets=");
        sb.append(this.mWindowDecorInsets);
        sb.append(" stableInsets=");
        sb.append(this.mStableInsets);
        if (this.mDisplayCutout != null) {
            str = " cutout=" + this.mDisplayCutout;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append(isRound() ? " round" : "");
        sb.append("}");
        return sb.toString();
    }

    public WindowInsets inset(Rect r) {
        return inset(r.left, r.top, r.right, r.bottom);
    }

    public WindowInsets inset(int left, int top, int right, int bottom) {
        Preconditions.checkArgumentNonnegative(left);
        Preconditions.checkArgumentNonnegative(top);
        Preconditions.checkArgumentNonnegative(right);
        Preconditions.checkArgumentNonnegative(bottom);
        WindowInsets result = new WindowInsets(this);
        if (!result.mSystemWindowInsetsConsumed) {
            result.mSystemWindowInsets = insetInsets(result.mSystemWindowInsets, left, top, right, bottom);
        }
        if (!result.mWindowDecorInsetsConsumed) {
            result.mWindowDecorInsets = insetInsets(result.mWindowDecorInsets, left, top, right, bottom);
        }
        if (!result.mStableInsetsConsumed) {
            result.mStableInsets = insetInsets(result.mStableInsets, left, top, right, bottom);
        }
        if (this.mDisplayCutout != null) {
            result.mDisplayCutout = result.mDisplayCutout.inset(left, top, right, bottom);
            if (result.mDisplayCutout.isEmpty()) {
                result.mDisplayCutout = null;
            }
        }
        return result;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof WindowInsets)) {
            return false;
        }
        WindowInsets that = (WindowInsets) o;
        if (!(this.mIsRound == that.mIsRound && this.mAlwaysConsumeNavBar == that.mAlwaysConsumeNavBar && this.mSystemWindowInsetsConsumed == that.mSystemWindowInsetsConsumed && this.mWindowDecorInsetsConsumed == that.mWindowDecorInsetsConsumed && this.mStableInsetsConsumed == that.mStableInsetsConsumed && this.mDisplayCutoutConsumed == that.mDisplayCutoutConsumed && Objects.equals(this.mSystemWindowInsets, that.mSystemWindowInsets) && Objects.equals(this.mWindowDecorInsets, that.mWindowDecorInsets) && Objects.equals(this.mStableInsets, that.mStableInsets) && Objects.equals(this.mDisplayCutout, that.mDisplayCutout))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mSystemWindowInsets, this.mWindowDecorInsets, this.mStableInsets, Boolean.valueOf(this.mIsRound), this.mDisplayCutout, Boolean.valueOf(this.mAlwaysConsumeNavBar), Boolean.valueOf(this.mSystemWindowInsetsConsumed), Boolean.valueOf(this.mWindowDecorInsetsConsumed), Boolean.valueOf(this.mStableInsetsConsumed), Boolean.valueOf(this.mDisplayCutoutConsumed)});
    }

    private static Rect insetInsets(Rect insets, int left, int top, int right, int bottom) {
        int newLeft = Math.max(0, insets.left - left);
        int newTop = Math.max(0, insets.top - top);
        int newRight = Math.max(0, insets.right - right);
        int newBottom = Math.max(0, insets.bottom - bottom);
        if (newLeft == left && newTop == top && newRight == right && newBottom == bottom) {
            return insets;
        }
        return new Rect(newLeft, newTop, newRight, newBottom);
    }

    /* access modifiers changed from: package-private */
    public boolean isSystemWindowInsetsConsumed() {
        return this.mSystemWindowInsetsConsumed;
    }
}
