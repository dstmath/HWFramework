package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HwCardButtonBarLayout extends LinearLayout {
    private static final int AVERAGE_DIVIDE = 2;
    private Comparator<Pair> mComparator;
    private boolean mIsNeedSelfLayout;

    public HwCardButtonBarLayout(Context context) {
        this(context, null);
    }

    public HwCardButtonBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mComparator = new Comparator<Pair>() {
            /* class huawei.android.widget.HwCardButtonBarLayout.AnonymousClass1 */

            public int compare(Pair pair1, Pair pair2) {
                return pair1.mWidth - pair2.mWidth;
            }
        };
        setOrientation(0);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(widthSize, View.MeasureSpec.getSize(heightMeasureSpec));
        int childCount = getChildCount();
        int visibleCount = 0;
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).getVisibility() != 8) {
                visibleCount++;
            }
        }
        int averageWidth = 0;
        if (visibleCount != 0) {
            averageWidth = ((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) / visibleCount;
        }
        int overflowCount = 0;
        List<Pair> indexWidthList = new ArrayList<>(childCount);
        int initialWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(widthSize, Integer.MIN_VALUE);
        for (int i2 = 0; i2 < childCount; i2++) {
            View view = getChildAt(i2);
            if (view.getVisibility() != 8) {
                view.measure(initialWidthMeasureSpec, heightMeasureSpec);
                int viewWidth = view.getMeasuredWidth();
                indexWidthList.add(new Pair(i2, viewWidth));
                if (viewWidth > averageWidth) {
                    overflowCount++;
                }
            }
        }
        if (overflowCount >= visibleCount) {
            adjustChildParams(childCount, true);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (overflowCount == 0) {
            adjustChildParams(childCount, false);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            this.mIsNeedSelfLayout = true;
            adjustChildWidth(indexWidthList, visibleCount, averageWidth);
        }
    }

    private void adjustChildParams(int childCount, boolean isOverflow) {
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != 8) {
                LinearLayout.LayoutParams params = null;
                if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    params = (LinearLayout.LayoutParams) view.getLayoutParams();
                }
                if (params == null) {
                    params = new LinearLayout.LayoutParams(-2, -1);
                }
                if (isOverflow) {
                    params.width = 0;
                    params.weight = 1.0f;
                } else {
                    params.width = -2;
                    params.weight = 0.0f;
                }
            }
        }
    }

    private void adjustChildWidth(List<Pair> indexWidthList, int visibleCount, int averageWidth) {
        int tempAverageWidth;
        Collections.sort(indexWidthList, this.mComparator);
        int tempRemainingWidth = (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight();
        int count = 0;
        for (Pair pair : indexWidthList) {
            int width = pair.mWidth;
            int index = pair.mIndex;
            LinearLayout.LayoutParams params = null;
            if (getChildAt(index).getLayoutParams() instanceof LinearLayout.LayoutParams) {
                params = (LinearLayout.LayoutParams) getChildAt(index).getLayoutParams();
            }
            int margin = 0;
            if (params != null) {
                margin = params.leftMargin + params.rightMargin;
            }
            if (width <= averageWidth) {
                count++;
                tempRemainingWidth -= width + margin;
            } else {
                int remain = visibleCount - count;
                if (remain == 0) {
                    tempAverageWidth = tempRemainingWidth - margin;
                } else {
                    tempAverageWidth = (tempRemainingWidth / remain) - margin;
                }
                pair.mWidth = tempAverageWidth;
            }
        }
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), Integer.MIN_VALUE);
        for (Pair pair2 : indexWidthList) {
            getChildAt(pair2.mIndex).measure(View.MeasureSpec.makeMeasureSpec(pair2.mWidth, Integer.MIN_VALUE), heightMeasureSpec);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int l, int t, int r, int b) {
        if (!this.mIsNeedSelfLayout || !isLayoutRtl()) {
            super.onLayout(isChanged, l, t, r, b);
        } else {
            layoutHorizontal(l, t, r, b);
        }
    }

    private void layoutHorizontal(int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        int childRight = right - getPaddingStart();
        int childTop = getPaddingTop();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                ViewGroup.LayoutParams viewGroupLayoutParams = child.getLayoutParams();
                if (viewGroupLayoutParams instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewGroupLayoutParams;
                    int childRight2 = childRight - layoutParams.getMarginStart();
                    int childWidth = child.getMeasuredWidth();
                    int childHeight = child.getMeasuredHeight();
                    if (layoutParams.gravity == 16) {
                        childTop = getPaddingTop() + (((((bottom - top) - childHeight) - getPaddingTop()) - getPaddingBottom()) / 2);
                    }
                    child.layout(childRight2 - childWidth, childTop, childRight2, childTop + childHeight);
                    childRight = childRight2 - (layoutParams.getMarginEnd() + childWidth);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class Pair {
        int mIndex;
        int mWidth;

        Pair(int index, int width) {
            this.mIndex = index;
            this.mWidth = width;
        }
    }
}
