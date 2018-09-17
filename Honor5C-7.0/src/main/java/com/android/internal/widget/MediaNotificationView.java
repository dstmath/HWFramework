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
    private final int mImageMinTopMargin;
    private View mMainColumn;
    private final int mMaxImageSize;
    private final int mNotificationContentImageMarginEnd;
    private final int mNotificationContentMarginEnd;
    private ImageView mRightIcon;

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
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        boolean hasIcon = this.mRightIcon.getVisibility() != 8;
        if (hasIcon && mode != 0) {
            measureChild(this.mActions, widthMeasureSpec, heightMeasureSpec);
            int size = MeasureSpec.getSize(widthMeasureSpec) - this.mActions.getMeasuredWidth();
            MarginLayoutParams layoutParams = (MarginLayoutParams) this.mRightIcon.getLayoutParams();
            int imageEndMargin = layoutParams.getMarginEnd();
            size = Math.max(Math.min(size - imageEndMargin, this.mMaxImageSize), this.mRightIcon.getMinimumWidth());
            layoutParams.width = size;
            layoutParams.height = size;
            this.mRightIcon.setLayoutParams(layoutParams);
            MarginLayoutParams mainParams = (MarginLayoutParams) this.mMainColumn.getLayoutParams();
            int marginEnd = (size + imageEndMargin) + this.mNotificationContentMarginEnd;
            if (marginEnd != mainParams.getMarginEnd()) {
                mainParams.setMarginEnd(marginEnd);
                this.mMainColumn.setLayoutParams(mainParams);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams iconParams = (MarginLayoutParams) this.mRightIcon.getLayoutParams();
        int topMargin = (getMeasuredHeight() - this.mRightIcon.getMeasuredHeight()) - iconParams.bottomMargin;
        boolean z = false;
        if (!hasIcon || topMargin >= this.mImageMinTopMargin) {
            z = resetHeaderIndention();
        } else {
            int paddingEnd = this.mNotificationContentImageMarginEnd;
            MarginLayoutParams headerParams = (MarginLayoutParams) this.mHeader.getLayoutParams();
            int newMarginEnd = this.mRightIcon.getMeasuredWidth() + iconParams.getMarginEnd();
            if (headerParams.getMarginEnd() != newMarginEnd) {
                headerParams.setMarginEnd(newMarginEnd);
                this.mHeader.setLayoutParams(headerParams);
                z = true;
            }
            if (this.mHeader.getPaddingEnd() != paddingEnd) {
                this.mHeader.setPaddingRelative(this.mHeader.getPaddingStart(), this.mHeader.getPaddingTop(), paddingEnd, this.mHeader.getPaddingBottom());
                z = true;
            }
        }
        if (z) {
            measureChildWithMargins(this.mHeader, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
    }

    private boolean resetHeaderIndention() {
        boolean remeasure = false;
        if (this.mHeader.getPaddingEnd() != this.mNotificationContentMarginEnd) {
            this.mHeader.setPaddingRelative(this.mHeader.getPaddingStart(), this.mHeader.getPaddingTop(), this.mNotificationContentMarginEnd, this.mHeader.getPaddingBottom());
            remeasure = true;
        }
        MarginLayoutParams headerParams = (MarginLayoutParams) this.mHeader.getLayoutParams();
        headerParams.setMarginEnd(0);
        if (headerParams.getMarginEnd() == 0) {
            return remeasure;
        }
        headerParams.setMarginEnd(0);
        this.mHeader.setLayoutParams(headerParams);
        return true;
    }

    public MediaNotificationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMaxImageSize = context.getResources().getDimensionPixelSize(R.dimen.media_notification_expanded_image_max_size);
        this.mImageMinTopMargin = (int) (((float) context.getResources().getDimensionPixelSize(R.dimen.notification_content_margin_top)) + (getResources().getDisplayMetrics().density * 2.0f));
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
