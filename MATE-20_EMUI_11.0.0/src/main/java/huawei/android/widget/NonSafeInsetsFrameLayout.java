package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class NonSafeInsetsFrameLayout extends FrameLayout implements NonSafeInsetsAvailable {
    public NonSafeInsetsFrameLayout(Context context) {
        super(context);
    }

    public NonSafeInsetsFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonSafeInsetsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NonSafeInsetsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
