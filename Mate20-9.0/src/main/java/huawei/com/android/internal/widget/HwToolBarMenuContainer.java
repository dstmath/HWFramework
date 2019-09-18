package huawei.com.android.internal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;

public class HwToolBarMenuContainer extends HwActionBarContainer {
    public HwToolBarMenuContainer(Context context) {
        this(context, null);
    }

    public HwToolBarMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setSplitViewLocation(int start, int end) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
