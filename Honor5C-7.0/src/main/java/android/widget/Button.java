package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;

@RemoteView
public class Button extends TextView {
    public Button(Context context) {
        this(context, null);
    }

    public Button(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.buttonStyle);
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CharSequence getAccessibilityClassName() {
        return Button.class.getName();
    }
}
