package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.rms.iaware.AppTypeInfo;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.widget.CachingIconView;
import com.android.internal.widget.ImageFloatingTextView;
import com.android.internal.widget.NotificationExpandButton;
import com.huawei.hsm.permission.StubController;

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
        this.mChildMinWidth = getResources().getDimensionPixelSize(17105228);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        NotificationSingleView.super.onFinishInflate();
        this.mAppName = findViewById(16908739);
        this.mIcon = findViewById(16908294);
        this.mText = findViewById(16909402);
        this.mExpandButton = findViewById(16908881);
        this.mTitle = (TextView) findViewById(16908310);
        this.mEmptyView = (TextView) findViewById(34603396);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        int wrapContentWidthSpec = View.MeasureSpec.makeMeasureSpec(givenWidth, AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
        int wrapContentHeightSpec = View.MeasureSpec.makeMeasureSpec(givenHeight, AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
        int totalWidth = getPaddingStart() + getPaddingEnd();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                child.measure(getChildMeasureSpec(wrapContentWidthSpec, lp.leftMargin + lp.rightMargin, lp.width), getChildMeasureSpec(wrapContentHeightSpec, lp.topMargin + lp.bottomMargin, lp.height));
                totalWidth += lp.leftMargin + lp.rightMargin + child.getMeasuredWidth();
            }
        }
        if (totalWidth < givenWidth) {
            int overFlow = givenWidth - totalWidth;
            if (this.mEmptyView != null) {
                int emptyWidth = this.mEmptyView.getMeasuredWidth();
                if (overFlow > 0 && this.mEmptyView.getVisibility() != 8) {
                    this.mEmptyView.measure(View.MeasureSpec.makeMeasureSpec(overFlow + emptyWidth, StubController.PERMISSION_ACCESS_BROWSER_RECORDS), wrapContentHeightSpec);
                }
            }
        } else {
            int overFlow2 = totalWidth - givenWidth;
            int appWidth = this.mAppName.getMeasuredWidth();
            if (overFlow2 > 0 && this.mAppName.getVisibility() != 8 && appWidth > this.mChildMinWidth) {
                int newSize = appWidth - (appWidth - this.mChildMinWidth > overFlow2 ? overFlow2 : appWidth - this.mChildMinWidth);
                this.mAppName.measure(View.MeasureSpec.makeMeasureSpec(newSize, AppTypeInfo.APP_ATTRIBUTE_OVERSEA), wrapContentHeightSpec);
                overFlow2 -= appWidth - newSize;
            }
            if (overFlow2 > 0 && this.mText.getVisibility() != 8) {
                int textWidth = this.mText.getMeasuredWidth();
                int newSize2 = textWidth - (textWidth - this.mChildMinWidth > overFlow2 ? overFlow2 : textWidth - this.mChildMinWidth);
                this.mText.measure(View.MeasureSpec.makeMeasureSpec(newSize2, AppTypeInfo.APP_ATTRIBUTE_OVERSEA), wrapContentHeightSpec);
                overFlow2 -= textWidth - newSize2;
            }
            if (overFlow2 > 0 && this.mTitle.getVisibility() != 8) {
                int titleWidth = this.mTitle.getMeasuredWidth();
                this.mTitle.measure(View.MeasureSpec.makeMeasureSpec(titleWidth - (titleWidth - this.mChildMinWidth > overFlow2 ? overFlow2 : titleWidth - this.mChildMinWidth), AppTypeInfo.APP_ATTRIBUTE_OVERSEA), wrapContentHeightSpec);
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
            if (child.getVisibility() != 8) {
                int childHeight = child.getMeasuredHeight();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                int left2 = left + params.getMarginStart();
                int right = child.getMeasuredWidth() + left2;
                int top = (int) (((float) getPaddingTop()) + (((float) (ownHeight - childHeight)) / 2.0f));
                int bottom = top + childHeight;
                int layoutLeft = left2;
                int layoutRight = right;
                if (getLayoutDirection() == 1) {
                    int ltrLeft = layoutLeft;
                    layoutLeft = getWidth() - layoutRight;
                    layoutRight = getWidth() - ltrLeft;
                }
                child.layout(layoutLeft, top, layoutRight, bottom);
                left = right + params.getMarginEnd();
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

    public void setOnClickListener(View.OnClickListener l) {
        NotificationSingleView.super.setOnClickListener(l);
        this.mExpandClickListener = l;
        this.mExpandButton.setOnClickListener(this.mExpandClickListener);
    }

    public CachingIconView getIcon() {
        return this.mIcon;
    }
}
