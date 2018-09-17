package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class TextSwitcher extends ViewSwitcher {
    public TextSwitcher(Context context) {
        super(context);
    }

    public TextSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addView(View child, int index, LayoutParams params) {
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

    public CharSequence getAccessibilityClassName() {
        return TextSwitcher.class.getName();
    }
}
