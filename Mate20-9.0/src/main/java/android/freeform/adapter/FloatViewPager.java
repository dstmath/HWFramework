package android.freeform.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.widget.ViewPager;

public class FloatViewPager extends ViewPager {
    public FloatViewPager(Context context) {
        super(context);
    }

    public FloatViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int height = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, 0));
            int h = child.getMeasuredHeight();
            if (h > height) {
                height = h;
            }
        }
        FloatViewPager.super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(3 * height, 1073741824));
    }
}
