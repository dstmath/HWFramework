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

class HwBannerTextStrcLayout extends RelativeLayout {
    final double ELE_TEXT_ABOVE_RATIO;
    final double ELE_TEXT_TWO_TYPE;

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
        this.ELE_TEXT_ABOVE_RATIO = 2.33d;
        this.ELE_TEXT_TWO_TYPE = 2.0d;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        initLayout();
    }

    private void initLayout() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int width = HwBannerTextStrcLayout.this.getWidth();
                if (width > 0) {
                    double hContentHeight = HwBannerTextStrcLayout.this.getAllViewsHeight() + ((double) HwBannerTextStrcLayout.this.dp2px(HwBannerTextStrcLayout.this.getContext(), 48.0f));
                    int heightP = (int) (((double) width) / 2.33d);
                    int h = HwBannerTextStrcLayout.this.getLayoutParams().height;
                    if (hContentHeight < ((double) heightP)) {
                        HwBannerTextStrcLayout.this.getLayoutParams().height = heightP;
                    } else {
                        HwBannerTextStrcLayout.this.getLayoutParams().height = (int) hContentHeight;
                    }
                    if (h != HwBannerTextStrcLayout.this.getLayoutParams().height) {
                        HwBannerTextStrcLayout.this.invalidate();
                        HwBannerTextStrcLayout.this.requestLayout();
                    }
                    HwBannerTextStrcLayout.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public double getAllViewsHeight() {
        List<View> viewList = getAllChildViews(this);
        double height = 0.0d;
        if (viewList.isEmpty()) {
            return 0.0d;
        }
        int viewListSize = viewList.size();
        int i = 0;
        if (((double) viewListSize) <= 2.0d) {
            while (true) {
                int i2 = i;
                if (i2 >= viewListSize) {
                    break;
                }
                height += (double) getTextViewHeight((TextView) viewList.get(i2));
                i = i2 + 1;
            }
        } else {
            while (true) {
                int i3 = i;
                if (i3 >= viewListSize) {
                    break;
                }
                if (((double) i3) != 2.0d) {
                    height += (double) getTextViewHeight((TextView) viewList.get(i3));
                }
                i = i3 + 1;
            }
        }
        return height;
    }

    private List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<>();
        if (view != null && (view instanceof ViewGroup)) {
            ViewGroup vp = (ViewGroup) view;
            int childCount = vp.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View viewchild = vp.getChildAt(i);
                if (viewchild instanceof TextView) {
                    allchildren.add(viewchild);
                }
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }

    /* access modifiers changed from: private */
    public int dp2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int getTextViewHeight(TextView pTextView) {
        if (pTextView == null) {
            return 0;
        }
        Layout layout = pTextView.getLayout();
        if (layout != null) {
            return layout.getLineTop(pTextView.getLineCount()) + pTextView.getCompoundPaddingTop() + pTextView.getCompoundPaddingBottom();
        }
        return 0;
    }
}
