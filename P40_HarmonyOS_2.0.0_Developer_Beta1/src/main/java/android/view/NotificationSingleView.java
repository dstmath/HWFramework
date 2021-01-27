package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.FloatConsts;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.widget.CachingIconView;
import com.android.internal.widget.ImageFloatingTextView;
import com.android.internal.widget.NotificationExpandButton;
import com.huawei.android.os.ProcessExt;

@RemoteViews.RemoteView
public class NotificationSingleView extends NotificationHeaderView {
    private View mAppName;
    private final int mChildMinWidth;
    private TextView mEmptyView;
    private NotificationExpandButton mExpandButton;
    private View.OnClickListener mExpandClickListener;
    private CachingIconView mIcon;
    private ImageFloatingTextView mText;
    private TextView mTitle;

    public NotificationSingleView(Context context) {
        this(context, null);
    }

    public NotificationSingleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationSingleView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationSingleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mChildMinWidth = getResources().getDimensionPixelSize(17105350);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        NotificationSingleView.super.onFinishInflate();
        this.mAppName = findViewById(16908770);
        this.mIcon = findViewById(16908294);
        this.mText = findViewById(16909474);
        this.mExpandButton = findViewById(16908935);
        this.mTitle = (TextView) findViewById(16908310);
        this.mEmptyView = (TextView) findViewById(34603503);
    }

    private int getTotalWidth(int wrapContentWidthSpec, int wrapContentHeightSpec) {
        int totalWidth = getPaddingStart() + getPaddingEnd();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !(child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams))) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                child.measure(getChildMeasureSpec(wrapContentWidthSpec, params.leftMargin + params.rightMargin, params.width), getChildMeasureSpec(wrapContentHeightSpec, params.topMargin + params.bottomMargin, params.height));
                totalWidth += params.leftMargin + params.rightMargin + child.getMeasuredWidth();
            }
        }
        return totalWidth;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int givenWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        int wrapContentWidthSpec = View.MeasureSpec.makeMeasureSpec(givenWidth, FloatConsts.SIGN_BIT_MASK);
        int wrapContentHeightSpec = View.MeasureSpec.makeMeasureSpec(givenHeight, FloatConsts.SIGN_BIT_MASK);
        int totalWidth = getTotalWidth(wrapContentWidthSpec, wrapContentHeightSpec);
        if (totalWidth < givenWidth) {
            int overFlow = givenWidth - totalWidth;
            TextView textView = this.mEmptyView;
            if (textView != null) {
                int emptyWidth = textView.getMeasuredWidth();
                if (overFlow > 0 && this.mEmptyView.getVisibility() != 8) {
                    this.mEmptyView.measure(View.MeasureSpec.makeMeasureSpec(overFlow + emptyWidth, ProcessExt.SCHED_RESET_ON_FORK), wrapContentHeightSpec);
                }
            }
        } else {
            int overFlow2 = totalWidth - givenWidth;
            int appWidth = this.mAppName.getMeasuredWidth();
            if (overFlow2 > 0 && this.mAppName.getVisibility() != 8 && appWidth > (i = this.mChildMinWidth)) {
                int newSize = appWidth - (appWidth - i > overFlow2 ? overFlow2 : appWidth - i);
                this.mAppName.measure(View.MeasureSpec.makeMeasureSpec(newSize, FloatConsts.SIGN_BIT_MASK), wrapContentHeightSpec);
                overFlow2 -= appWidth - newSize;
            }
            if (overFlow2 > 0 && this.mText.getVisibility() != 8) {
                int textWidth = this.mText.getMeasuredWidth();
                int i2 = this.mChildMinWidth;
                int newSize2 = textWidth - (textWidth - i2 > overFlow2 ? overFlow2 : textWidth - i2);
                this.mText.measure(View.MeasureSpec.makeMeasureSpec(newSize2, FloatConsts.SIGN_BIT_MASK), wrapContentHeightSpec);
                overFlow2 -= textWidth - newSize2;
            }
            if (overFlow2 > 0 && this.mTitle.getVisibility() != 8) {
                int titleWidth = this.mTitle.getMeasuredWidth();
                int i3 = this.mChildMinWidth;
                if (titleWidth - i3 <= overFlow2) {
                    overFlow2 = titleWidth - i3;
                }
                this.mTitle.measure(View.MeasureSpec.makeMeasureSpec(titleWidth - overFlow2, FloatConsts.SIGN_BIT_MASK), wrapContentHeightSpec);
            }
        }
        setMeasuredDimension(givenWidth, givenHeight);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingStart();
        int childCount = getChildCount();
        int ownHeight = (getHeight() - getPaddingTop()) - getPaddingBottom();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !(child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams))) {
                int childHeight = child.getMeasuredHeight();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                int left2 = left + params.getMarginStart();
                int right = child.getMeasuredWidth() + left2;
                int top = (int) (((float) getPaddingTop()) + (((float) (ownHeight - childHeight)) / 2.0f));
                int bottom = top + childHeight;
                int layoutLeft = left2;
                int layoutRight = right;
                if (getLayoutDirection() == 1) {
                    layoutLeft = getWidth() - layoutRight;
                    layoutRight = getWidth() - layoutLeft;
                }
                child.layout(layoutLeft, top, layoutRight, bottom);
                left = params.getMarginEnd() + right;
            }
        }
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new RelativeLayout.LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        NotificationSingleView.super.onDraw(canvas);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        NotificationSingleView.super.setOnClickListener(listener);
        this.mExpandClickListener = listener;
        this.mExpandButton.setOnClickListener(this.mExpandClickListener);
    }

    public CachingIconView getIcon() {
        return this.mIcon;
    }
}
