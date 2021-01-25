package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class TextSwitcher extends ViewSwitcher {
    public TextSwitcher(Context context) {
        super(context);
    }

    public TextSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // android.widget.ViewSwitcher, android.widget.ViewAnimator, android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof TextView) {
            super.addView(child, index, params);
            return;
        }
        throw new IllegalArgumentException("TextSwitcher children must be instances of TextView");
    }

    public void setText(CharSequence text) {
        ((TextView) getNextView()).setText(text);
        showNext();
    }

    public void setCurrentText(CharSequence text) {
        ((TextView) getCurrentView()).setText(text);
    }

    @Override // android.widget.ViewSwitcher, android.widget.ViewAnimator, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return TextSwitcher.class.getName();
    }
}
