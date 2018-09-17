package android.support.v4.view;

import android.graphics.Rect;
import android.os.Build.VERSION;
import android.view.WindowInsets;

public class WindowInsetsCompat {
    private final Object mInsets;

    private WindowInsetsCompat(Object insets) {
        this.mInsets = insets;
    }

    public WindowInsetsCompat(WindowInsetsCompat src) {
        Object obj = null;
        if (VERSION.SDK_INT >= 20) {
            if (src != null) {
                WindowInsets obj2 = new WindowInsets((WindowInsets) src.mInsets);
            }
            this.mInsets = obj2;
            return;
        }
        this.mInsets = null;
    }

    public int getSystemWindowInsetLeft() {
        if (VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).getSystemWindowInsetLeft();
        }
        return 0;
    }

    public int getSystemWindowInsetTop() {
        if (VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).getSystemWindowInsetTop();
        }
        return 0;
    }

    public int getSystemWindowInsetRight() {
        if (VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).getSystemWindowInsetRight();
        }
        return 0;
    }

    public int getSystemWindowInsetBottom() {
        if (VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).getSystemWindowInsetBottom();
        }
        return 0;
    }

    public boolean hasSystemWindowInsets() {
        if (VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).hasSystemWindowInsets();
        }
        return false;
    }

    public boolean hasInsets() {
        if (VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).hasInsets();
        }
        return false;
    }

    public boolean isConsumed() {
        if (VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).isConsumed();
        }
        return false;
    }

    public boolean isRound() {
        if (VERSION.SDK_INT >= 20) {
            return ((WindowInsets) this.mInsets).isRound();
        }
        return false;
    }

    public WindowInsetsCompat consumeSystemWindowInsets() {
        if (VERSION.SDK_INT >= 20) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).consumeSystemWindowInsets());
        }
        return null;
    }

    public WindowInsetsCompat replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        if (VERSION.SDK_INT >= 20) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).replaceSystemWindowInsets(left, top, right, bottom));
        }
        return null;
    }

    public WindowInsetsCompat replaceSystemWindowInsets(Rect systemWindowInsets) {
        if (VERSION.SDK_INT >= 21) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).replaceSystemWindowInsets(systemWindowInsets));
        }
        return null;
    }

    public int getStableInsetTop() {
        if (VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).getStableInsetTop();
        }
        return 0;
    }

    public int getStableInsetLeft() {
        if (VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).getStableInsetLeft();
        }
        return 0;
    }

    public int getStableInsetRight() {
        if (VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).getStableInsetRight();
        }
        return 0;
    }

    public int getStableInsetBottom() {
        if (VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).getStableInsetBottom();
        }
        return 0;
    }

    public boolean hasStableInsets() {
        if (VERSION.SDK_INT >= 21) {
            return ((WindowInsets) this.mInsets).hasStableInsets();
        }
        return false;
    }

    public WindowInsetsCompat consumeStableInsets() {
        if (VERSION.SDK_INT >= 21) {
            return new WindowInsetsCompat(((WindowInsets) this.mInsets).consumeStableInsets());
        }
        return null;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WindowInsetsCompat other = (WindowInsetsCompat) o;
        if (this.mInsets != null) {
            z = this.mInsets.equals(other.mInsets);
        } else if (other.mInsets != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.mInsets == null ? 0 : this.mInsets.hashCode();
    }

    static WindowInsetsCompat wrap(Object insets) {
        return insets == null ? null : new WindowInsetsCompat(insets);
    }

    static Object unwrap(WindowInsetsCompat insets) {
        return insets == null ? null : insets.mInsets;
    }
}
