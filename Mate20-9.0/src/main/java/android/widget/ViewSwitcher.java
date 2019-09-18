package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ViewSwitcher extends ViewAnimator {
    ViewFactory mFactory;

    public interface ViewFactory {
        View makeView();
    }

    public ViewSwitcher(Context context) {
        super(context);
    }

    public ViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() < 2) {
            super.addView(child, index, params);
            return;
        }
        throw new IllegalStateException("Can't add more than 2 views to a ViewSwitcher");
    }

    public CharSequence getAccessibilityClassName() {
        return ViewSwitcher.class.getName();
    }

    public View getNextView() {
        return getChildAt(this.mWhichChild == 0 ? 1 : 0);
    }

    private View obtainView() {
        View child = this.mFactory.makeView();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(-1, -2);
        }
        addView(child, (ViewGroup.LayoutParams) lp);
        return child;
    }

    public void setFactory(ViewFactory factory) {
        this.mFactory = factory;
        obtainView();
        obtainView();
    }

    public void reset() {
        this.mFirstTime = true;
        View v = getChildAt(0);
        if (v != null) {
            v.setVisibility(8);
        }
        View v2 = getChildAt(1);
        if (v2 != null) {
            v2.setVisibility(8);
        }
    }
}
