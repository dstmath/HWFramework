package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.rms.iaware.AppTypeInfo;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.RelativeLayout;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import com.android.internal.widget.CachingIconView;
import com.android.internal.widget.ImageFloatingTextView;
import com.android.internal.widget.NotificationExpandButton;
import com.huawei.hsm.permission.StubController;

@RemoteView
public class NotificationSingleView extends NotificationHeaderView {
    private View mAppName;
    private final int mChildMinWidth;
    private NotificationExpandButton mExpandButton;
    private OnClickListener mExpandClickListener;
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
        this.mChildMinWidth = getResources().getDimensionPixelSize(17105165);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAppName = findViewById(16908726);
        this.mIcon = (CachingIconView) findViewById(16908294);
        this.mText = (ImageFloatingTextView) findViewById(16909348);
        this.mExpandButton = (NotificationExpandButton) findViewById(16908860);
        this.mTitle = (TextView) findViewById(16908310);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = MeasureSpec.getSize(heightMeasureSpec);
        int wrapContentWidthSpec = MeasureSpec.makeMeasureSpec(givenWidth, AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
        int wrapContentHeightSpec = MeasureSpec.makeMeasureSpec(givenHeight, AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
        int totalWidth = getPaddingStart() + getPaddingEnd();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                child.measure(getChildMeasureSpec(wrapContentWidthSpec, lp.leftMargin + lp.rightMargin, lp.width), getChildMeasureSpec(wrapContentHeightSpec, lp.topMargin + lp.bottomMargin, lp.height));
                totalWidth += (lp.leftMargin + lp.rightMargin) + child.getMeasuredWidth();
            }
        }
        int overFlow;
        int textWidth;
        if (totalWidth < givenWidth) {
            overFlow = givenWidth - totalWidth;
            textWidth = this.mText.getMeasuredWidth();
            if (overFlow > 0 && this.mText.getVisibility() != 8) {
                this.mText.measure(MeasureSpec.makeMeasureSpec(overFlow + textWidth, StubController.PERMISSION_ACCESS_BROWSER_RECORDS), wrapContentHeightSpec);
            }
        } else {
            int newSize;
            overFlow = totalWidth - givenWidth;
            int appWidth = this.mAppName.getMeasuredWidth();
            if (overFlow > 0 && this.mAppName.getVisibility() != 8 && appWidth > this.mChildMinWidth) {
                newSize = appWidth - Math.min(appWidth - this.mChildMinWidth, overFlow);
                this.mAppName.measure(MeasureSpec.makeMeasureSpec(newSize, AppTypeInfo.APP_ATTRIBUTE_OVERSEA), wrapContentHeightSpec);
                overFlow -= appWidth - newSize;
            }
            if (overFlow > 0 && this.mText.getVisibility() != 8) {
                textWidth = this.mText.getMeasuredWidth();
                newSize = textWidth - Math.min(textWidth - this.mChildMinWidth, overFlow);
                this.mText.measure(MeasureSpec.makeMeasureSpec(newSize, AppTypeInfo.APP_ATTRIBUTE_OVERSEA), wrapContentHeightSpec);
                overFlow -= textWidth - newSize;
            }
            if (overFlow > 0 && this.mTitle.getVisibility() != 8) {
                int titleWidth = this.mTitle.getMeasuredWidth();
                newSize = titleWidth - Math.min(titleWidth - this.mChildMinWidth, overFlow);
                this.mTitle.measure(MeasureSpec.makeMeasureSpec(newSize, AppTypeInfo.APP_ATTRIBUTE_OVERSEA), wrapContentHeightSpec);
                overFlow -= titleWidth - newSize;
            }
        }
        setMeasuredDimension(givenWidth, givenHeight);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingStart();
        int childCount = getChildCount();
        int ownHeight = (getHeight() - getPaddingTop()) - getPaddingBottom();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int childHeight = child.getMeasuredHeight();
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                left += params.getMarginStart();
                int right = left + child.getMeasuredWidth();
                int top = (int) (((float) getPaddingTop()) + (((float) (ownHeight - childHeight)) / 2.0f));
                int bottom = top + childHeight;
                int layoutLeft = left;
                int layoutRight = right;
                if (getLayoutDirection() == 1) {
                    int ltrLeft = left;
                    layoutLeft = getWidth() - right;
                    layoutRight = getWidth() - left;
                }
                child.layout(layoutLeft, top, layoutRight, bottom);
                left = right + params.getMarginEnd();
            }
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new RelativeLayout.LayoutParams(getContext(), attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        this.mExpandClickListener = l;
        this.mExpandButton.setOnClickListener(this.mExpandClickListener);
    }

    public CachingIconView getIcon() {
        return this.mIcon;
    }
}
