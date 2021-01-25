package huawei.android.widget.pattern;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class HwBannerTextStrcLayout extends RelativeLayout {
    private static final double ELE_TEXT_ABOVE_RATIO = 2.33d;
    private static final double ELE_TEXT_TWO_TYPE = 2.0d;
    private static final int INITIAL_CAPACITY_SIZE = 10;
    private static final int SPECIFIED_HEIGHT = 48;

    public HwBannerTextStrcLayout(Context context) {
        this(context, null);
    }

    public HwBannerTextStrcLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwBannerTextStrcLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwBannerTextStrcLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.RelativeLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        initLayout();
    }

    private void initLayout() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class huawei.android.widget.pattern.HwBannerTextStrcLayout.AnonymousClass1 */

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                int width = HwBannerTextStrcLayout.this.getWidth();
                if (width > 0) {
                    double allViewsHeight = HwBannerTextStrcLayout.this.getAllViewsHeight();
                    HwBannerTextStrcLayout hwBannerTextStrcLayout = HwBannerTextStrcLayout.this;
                    double totalContentHeight = allViewsHeight + ((double) hwBannerTextStrcLayout.dp2px(hwBannerTextStrcLayout.getContext(), 48.0f));
                    int relativeHeight = (int) (((double) width) / HwBannerTextStrcLayout.ELE_TEXT_ABOVE_RATIO);
                    int height = HwBannerTextStrcLayout.this.getLayoutParams().height;
                    if (totalContentHeight < ((double) relativeHeight)) {
                        HwBannerTextStrcLayout.this.getLayoutParams().height = relativeHeight;
                    } else {
                        HwBannerTextStrcLayout.this.getLayoutParams().height = (int) totalContentHeight;
                    }
                    if (height != HwBannerTextStrcLayout.this.getLayoutParams().height) {
                        HwBannerTextStrcLayout.this.invalidate();
                        HwBannerTextStrcLayout.this.requestLayout();
                    }
                    HwBannerTextStrcLayout.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private double getAllViewsHeight() {
        List<View> viewList = getAllChildViews(this);
        double height = 0.0d;
        if (viewList.isEmpty()) {
            return 0.0d;
        }
        int viewListSize = viewList.size();
        if (((double) viewListSize) > ELE_TEXT_TWO_TYPE) {
            for (int i = 0; i < viewListSize; i++) {
                if (((double) i) != ELE_TEXT_TWO_TYPE) {
                    height += getViewHeight(viewList.get(i));
                }
            }
        } else {
            for (int i2 = 0; i2 < viewListSize; i2++) {
                height += getViewHeight(viewList.get(i2));
            }
        }
        return height;
    }

    private double getViewHeight(View view) {
        if (view instanceof TextView) {
            return (double) getTextViewHeight((TextView) view);
        }
        return 0.0d;
    }

    private List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<>((int) INITIAL_CAPACITY_SIZE);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View viewchild = viewGroup.getChildAt(i);
                if (viewchild instanceof TextView) {
                    allchildren.add(viewchild);
                }
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int dp2px(Context context, float dpValue) {
        return Math.round(dpValue * context.getResources().getDisplayMetrics().density);
    }

    private int getTextViewHeight(TextView textView) {
        Layout layout;
        if (textView == null || (layout = textView.getLayout()) == null) {
            return 0;
        }
        return layout.getLineTop(textView.getLineCount()) + textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
    }
}
