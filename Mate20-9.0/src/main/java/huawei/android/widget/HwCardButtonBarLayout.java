package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HwCardButtonBarLayout extends LinearLayout {
    private Comparator<Pair> mComparator;
    private boolean needSelfLayout;

    private class Pair {
        int index;
        int width;

        Pair(int a, int b) {
            this.index = a;
            this.width = b;
        }
    }

    public HwCardButtonBarLayout(Context context) {
        this(context, null);
    }

    public HwCardButtonBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mComparator = new Comparator<Pair>() {
            public int compare(Pair o1, Pair o2) {
                return o1.width - o2.width;
            }
        };
        setOrientation(0);
    }

    /* access modifiers changed from: protected */
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
        int averageWidth = ((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) / visibleCount;
        List<Pair> indexWidthList = new ArrayList<>(childCount);
        int initialWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(widthSize, Integer.MIN_VALUE);
        int overflowCount = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            View view = getChildAt(i2);
            if (view.getVisibility() == 8) {
                int i3 = heightMeasureSpec;
            } else {
                view.measure(initialWidthMeasureSpec, heightMeasureSpec);
                int viewWidth = view.getMeasuredWidth();
                indexWidthList.add(new Pair(i2, viewWidth));
                if (viewWidth > averageWidth) {
                    overflowCount++;
                }
            }
        }
        int i4 = heightMeasureSpec;
        if (overflowCount >= visibleCount) {
            adjustChildParams(childCount, true);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (overflowCount == 0) {
            adjustChildParams(childCount, false);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            this.needSelfLayout = true;
            adjustChildWidth(indexWidthList, visibleCount, averageWidth);
        }
    }

    private void adjustChildParams(int childCount, boolean overflow) {
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != 8) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                if (params == null) {
                    params = new LinearLayout.LayoutParams(-2, -1);
                }
                if (overflow) {
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
            int width = pair.width;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getChildAt(pair.index).getLayoutParams();
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
                pair.width = tempAverageWidth;
            }
        }
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824);
        for (Pair pair2 : indexWidthList) {
            getChildAt(pair2.index).measure(View.MeasureSpec.makeMeasureSpec(pair2.width, Integer.MIN_VALUE), heightMeasureSpec);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!this.needSelfLayout || !isLayoutRtl()) {
            super.onLayout(changed, l, t, r, b);
        } else {
            layoutHorizontal(l);
        }
    }

    private void layoutHorizontal(int left) {
        int childCount = getChildCount();
        int childLeft = getPaddingEnd() + left;
        int childTop = getPaddingTop();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                int childLeft2 = childLeft + params.getMarginEnd();
                int childWidth = child.getMeasuredWidth();
                child.layout(childLeft2, childTop, childLeft2 + childWidth, childTop + child.getMeasuredHeight());
                childLeft = childLeft2 + params.getMarginStart() + childWidth;
            }
        }
    }
}
