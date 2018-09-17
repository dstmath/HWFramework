package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;

@RemoteView
public class MediaNotificationView extends FrameLayout {
    private View mActions;
    private View mHeader;
    private View mMainColumn;
    private final int mNotificationContentImageMarginEnd;
    private final int mNotificationContentMarginEnd;
    private ImageView mRightIcon;
    private final int mSmallImageSize;

    public MediaNotificationView(Context context) {
        this(context, null);
    }

    public MediaNotificationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaNotificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean hasIcon = this.mRightIcon.getVisibility() != 8;
        if (!hasIcon) {
            resetHeaderIndention();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        boolean reMeasure = false;
        if (hasIcon && mode != 0) {
            int size = MeasureSpec.getSize(widthMeasureSpec) - this.mActions.getMeasuredWidth();
            MarginLayoutParams layoutParams = (MarginLayoutParams) this.mRightIcon.getLayoutParams();
            int imageEndMargin = layoutParams.getMarginEnd();
            size -= imageEndMargin;
            int fullHeight = getMeasuredHeight();
            if (size < fullHeight) {
                size = this.mSmallImageSize;
            } else {
                size = fullHeight;
            }
            if (!(layoutParams.width == size && layoutParams.height == size)) {
                layoutParams.width = size;
                layoutParams.height = size;
                this.mRightIcon.-wrap18(layoutParams);
                reMeasure = true;
            }
            MarginLayoutParams params = (MarginLayoutParams) this.mMainColumn.getLayoutParams();
            int marginEnd = (size + imageEndMargin) + this.mNotificationContentMarginEnd;
            if (marginEnd != params.getMarginEnd()) {
                params.setMarginEnd(marginEnd);
                this.mMainColumn.-wrap18(params);
                reMeasure = true;
            }
            int headerMarginEnd = size + imageEndMargin;
            params = (MarginLayoutParams) this.mHeader.getLayoutParams();
            if (params.getMarginEnd() != headerMarginEnd) {
                params.setMarginEnd(headerMarginEnd);
                this.mHeader.-wrap18(params);
                reMeasure = true;
            }
            if (this.mHeader.getPaddingEnd() != this.mNotificationContentImageMarginEnd) {
                this.mHeader.setPaddingRelative(this.mHeader.getPaddingStart(), this.mHeader.getPaddingTop(), this.mNotificationContentImageMarginEnd, this.mHeader.getPaddingBottom());
                reMeasure = true;
            }
        }
        if (reMeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void resetHeaderIndention() {
        if (this.mHeader.getPaddingEnd() != this.mNotificationContentMarginEnd) {
            this.mHeader.setPaddingRelative(this.mHeader.getPaddingStart(), this.mHeader.getPaddingTop(), this.mNotificationContentMarginEnd, this.mHeader.getPaddingBottom());
        }
        MarginLayoutParams headerParams = (MarginLayoutParams) this.mHeader.getLayoutParams();
        headerParams.setMarginEnd(0);
        if (headerParams.getMarginEnd() != 0) {
            headerParams.setMarginEnd(0);
            this.mHeader.-wrap18(headerParams);
        }
    }

    public MediaNotificationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSmallImageSize = context.getResources().getDimensionPixelSize(R.dimen.media_notification_expanded_image_small_size);
        this.mNotificationContentMarginEnd = context.getResources().getDimensionPixelSize(R.dimen.notification_content_margin_end);
        this.mNotificationContentImageMarginEnd = context.getResources().getDimensionPixelSize(R.dimen.notification_content_image_margin_end);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mRightIcon = (ImageView) findViewById(R.id.right_icon);
        this.mActions = findViewById(R.id.media_actions);
        this.mHeader = findViewById(R.id.notification_header);
        this.mMainColumn = findViewById(R.id.notification_main_column);
    }
}
