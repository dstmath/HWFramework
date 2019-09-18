package android.inputmethodservice;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

class ExtractButton extends Button {
    public ExtractButton(Context context) {
        super(context, null);
    }

    public ExtractButton(Context context, AttributeSet attrs) {
        super(context, attrs, 16842824);
    }

    public ExtractButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExtractButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean hasWindowFocus() {
        return isEnabled() && getVisibility() == 0;
    }
}
