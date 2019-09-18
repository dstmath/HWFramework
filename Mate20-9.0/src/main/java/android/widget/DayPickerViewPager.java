package android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.widget.ViewPager;
import java.util.ArrayList;
import java.util.function.Predicate;

class DayPickerViewPager extends ViewPager {
    private final ArrayList<View> mMatchParentChildren;

    public DayPickerViewPager(Context context) {
        this(context, null);
    }

    public DayPickerViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DayPickerViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DayPickerViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMatchParentChildren = new ArrayList<>(1);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        int i = widthMeasureSpec;
        int i2 = heightMeasureSpec;
        populate();
        int count = getChildCount();
        int i3 = 0;
        int i4 = 1073741824;
        boolean measureMatchParentChildren = (View.MeasureSpec.getMode(widthMeasureSpec) == 1073741824 && View.MeasureSpec.getMode(heightMeasureSpec) == 1073741824) ? false : true;
        int maxWidth = 0;
        int childState = 0;
        int maxHeight = 0;
        for (int i5 = 0; i5 < count; i5++) {
            View child = getChildAt(i5);
            if (child.getVisibility() != 8) {
                measureChild(child, i, i2);
                ViewPager.LayoutParams lp = child.getLayoutParams();
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                    this.mMatchParentChildren.add(child);
                }
            }
        }
        int maxWidth2 = maxWidth + getPaddingLeft() + getPaddingRight();
        int maxHeight2 = Math.max(maxHeight + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight());
        int maxWidth3 = Math.max(maxWidth2, getSuggestedMinimumWidth());
        Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight2 = Math.max(maxHeight2, drawable.getMinimumHeight());
            maxWidth3 = Math.max(maxWidth3, drawable.getMinimumWidth());
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth3, i, childState), resolveSizeAndState(maxHeight2, i2, childState << 16));
        int count2 = this.mMatchParentChildren.size();
        if (count2 > 1) {
            while (i3 < count2) {
                View child2 = this.mMatchParentChildren.get(i3);
                ViewPager.LayoutParams lp2 = child2.getLayoutParams();
                if (lp2.width == -1) {
                    childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), i4);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(i, getPaddingLeft() + getPaddingRight(), lp2.width);
                }
                if (lp2.height == -1) {
                    childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), i4);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(i2, getPaddingTop() + getPaddingBottom(), lp2.height);
                }
                child2.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                i3++;
                i4 = 1073741824;
            }
        }
        this.mMatchParentChildren.clear();
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        if (predicate.test(this)) {
            return this;
        }
        SimpleMonthView current = getAdapter().getView(getCurrent());
        if (!(current == childToSkip || current == null)) {
            View v = current.findViewByPredicate(predicate);
            if (v != null) {
                return v;
            }
        }
        int len = getChildCount();
        for (int i = 0; i < len; i++) {
            View child = getChildAt(i);
            if (!(child == childToSkip || child == current)) {
                View v2 = child.findViewByPredicate(predicate);
                if (v2 != null) {
                    return v2;
                }
            }
        }
        return null;
    }
}
