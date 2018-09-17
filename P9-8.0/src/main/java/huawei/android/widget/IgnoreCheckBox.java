package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class IgnoreCheckBox extends CheckBox {
    public IgnoreCheckBox(Context context) {
        this(context, null);
    }

    public IgnoreCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 33620017);
    }

    public IgnoreCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IgnoreCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setButtonDrawableHeight(context.getResources().getDimensionPixelSize(34472081));
        setButtonDrawableWidth(context.getResources().getDimensionPixelSize(34472082));
    }
}
