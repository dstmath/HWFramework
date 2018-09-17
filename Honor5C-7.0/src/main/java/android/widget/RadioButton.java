package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.android.internal.R;

public class RadioButton extends CompoundButton {
    public RadioButton(Context context) {
        this(context, null);
    }

    public RadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.radioButtonStyle);
    }

    public RadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void toggle() {
        if (!isChecked()) {
            super.toggle();
        }
    }

    public CharSequence getAccessibilityClassName() {
        return RadioButton.class.getName();
    }
}
