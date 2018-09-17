package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class ListPopupWindow extends HwListPopupWindow {
    public ListPopupWindow(Context context) {
        this(context, null, 16843519, 0);
    }

    public ListPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 16843519, 0);
    }

    public ListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
