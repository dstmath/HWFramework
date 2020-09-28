package android.view;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.android.internal.widget.CachingIconView;
import java.util.ArrayList;

@RemoteViews.RemoteView
public class NotificationHeaderView extends ViewGroup {
    public static final int NO_COLOR = 1;
    private boolean mAcceptAllTouches;
    private View mAppName;
    private View mAppOps;
    private View.OnClickListener mAppOpsListener;
    private View mAudiblyAlertedIcon;
    private Drawable mBackground;
    private View mCameraIcon;
    private final int mChildMinWidth;
    private final int mContentEndMargin;
    private boolean mEntireHeaderClickable;
    private ImageView mExpandButton;
    private View.OnClickListener mExpandClickListener;
    private boolean mExpandOnlyOnButton;
    private boolean mExpanded;
    private final int mGravity;
    private View mHeaderText;
    private int mHeaderTextMarginEnd;
    private CachingIconView mIcon;
    private int mIconColor;
    private View mMicIcon;
    private int mOriginalNotificationColor;
    private View mOverlayIcon;
    private View mProfileBadge;
    ViewOutlineProvider mProvider;
    private View mSecondaryHeaderText;
    private boolean mShowExpandButtonAtEnd;
    private boolean mShowWorkBadgeAtEnd;
    private int mTotalWidth;
    private HeaderTouchListener mTouchListener;

    public NotificationHeaderView(Context context) {
        this(context, null);
    }

    @UnsupportedAppUsage
    public NotificationHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTouchListener = new HeaderTouchListener();
        this.mProvider = new ViewOutlineProvider() {
            /* class android.view.NotificationHeaderView.AnonymousClass1 */

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                if (NotificationHeaderView.this.mBackground != null) {
                    outline.setRect(0, 0, NotificationHeaderView.this.getWidth(), NotificationHeaderView.this.getHeight());
                    outline.setAlpha(1.0f);
                }
            }
        };
        Resources res = getResources();
        this.mChildMinWidth = res.getDimensionPixelSize(R.dimen.notification_header_shrink_min_width);
        this.mContentEndMargin = res.getDimensionPixelSize(R.dimen.notification_content_margin_end);
        this.mEntireHeaderClickable = res.getBoolean(R.bool.config_notificationHeaderClickableForExpand);
        TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{16842927}, defStyleAttr, defStyleRes);
        this.mGravity = ta.getInt(0, 0);
        ta.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mAppName = findViewById(R.id.app_name_text);
        this.mHeaderText = findViewById(R.id.header_text);
        this.mSecondaryHeaderText = findViewById(R.id.header_text_secondary);
        this.mExpandButton = (ImageView) findViewById(R.id.expand_button);
        this.mIcon = (CachingIconView) findViewById(16908294);
        this.mProfileBadge = findViewById(R.id.profile_badge);
        this.mCameraIcon = findViewById(R.id.camera);
        this.mMicIcon = findViewById(R.id.mic);
        this.mOverlayIcon = findViewById(R.id.overlay);
        this.mAppOps = findViewById(R.id.app_ops);
        this.mAudiblyAlertedIcon = findViewById(R.id.alerted_icon);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        int wrapContentWidthSpec = View.MeasureSpec.makeMeasureSpec(givenWidth, Integer.MIN_VALUE);
        int wrapContentHeightSpec = View.MeasureSpec.makeMeasureSpec(givenHeight, Integer.MIN_VALUE);
        int totalWidth = getPaddingStart();
        int iconWidth = getPaddingEnd();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                child.measure(getChildMeasureSpec(wrapContentWidthSpec, lp.leftMargin + lp.rightMargin, lp.width), getChildMeasureSpec(wrapContentHeightSpec, lp.topMargin + lp.bottomMargin, lp.height));
                if ((child == this.mExpandButton && this.mShowExpandButtonAtEnd) || child == this.mProfileBadge || child == this.mAppOps) {
                    iconWidth += lp.leftMargin + lp.rightMargin + child.getMeasuredWidth();
                } else {
                    totalWidth += lp.leftMargin + lp.rightMargin + child.getMeasuredWidth();
                }
            }
        }
        int endMargin = Math.max(this.mHeaderTextMarginEnd, iconWidth);
        if (totalWidth > givenWidth - endMargin) {
            shrinkViewForOverflow(wrapContentHeightSpec, shrinkViewForOverflow(wrapContentHeightSpec, shrinkViewForOverflow(wrapContentHeightSpec, (totalWidth - givenWidth) + endMargin, this.mAppName, this.mChildMinWidth), this.mHeaderText, 0), this.mSecondaryHeaderText, 0);
        }
        this.mTotalWidth = Math.min(totalWidth + getPaddingEnd(), givenWidth);
        setMeasuredDimension(givenWidth, givenHeight);
    }

    private int shrinkViewForOverflow(int heightSpec, int overFlow, View targetView, int minimumWidth) {
        if (targetView == null) {
            return 0;
        }
        int oldWidth = targetView.getMeasuredWidth();
        if (overFlow <= 0 || targetView.getVisibility() == 8 || oldWidth <= minimumWidth) {
            return overFlow;
        }
        int newSize = Math.max(minimumWidth, oldWidth - overFlow);
        targetView.measure(View.MeasureSpec.makeMeasureSpec(newSize, Integer.MIN_VALUE), heightSpec);
        return overFlow - (oldWidth - newSize);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutRight;
        int layoutLeft;
        int left = getPaddingStart();
        int end = getMeasuredWidth();
        if ((this.mGravity & 1) != 0) {
            left += (getMeasuredWidth() / 2) - (this.mTotalWidth / 2);
        }
        int childCount = getChildCount();
        int ownHeight = (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int childHeight = child.getMeasuredHeight();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                int top = (int) (((float) getPaddingTop()) + (((float) (ownHeight - childHeight)) / 2.0f));
                int bottom = top + childHeight;
                if ((child == this.mExpandButton && this.mShowExpandButtonAtEnd) || child == this.mProfileBadge || child == this.mAppOps) {
                    if (end == getMeasuredWidth()) {
                        layoutRight = end - this.mContentEndMargin;
                    } else {
                        layoutRight = end - params.getMarginEnd();
                    }
                    layoutLeft = layoutRight - child.getMeasuredWidth();
                    end = layoutLeft - params.getMarginStart();
                } else {
                    int left2 = left + params.getMarginStart();
                    int right = child.getMeasuredWidth() + left2;
                    layoutLeft = left2;
                    layoutRight = right;
                    left = right + params.getMarginEnd();
                }
                if (getLayoutDirection() == 1) {
                    layoutLeft = getWidth() - layoutRight;
                    layoutRight = getWidth() - layoutLeft;
                }
                child.layout(layoutLeft, top, layoutRight, bottom);
            }
        }
        updateTouchListener();
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.MarginLayoutParams(getContext(), attrs);
    }

    public void setHeaderBackgroundDrawable(Drawable drawable) {
        if (drawable != null) {
            setWillNotDraw(false);
            this.mBackground = drawable;
            this.mBackground.setCallback(this);
            setOutlineProvider(this.mProvider);
        } else {
            setWillNotDraw(true);
            this.mBackground = null;
            setOutlineProvider(null);
        }
        invalidate();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setBounds(0, 0, getWidth(), getHeight());
            this.mBackground.draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mBackground;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void drawableStateChanged() {
        Drawable drawable = this.mBackground;
        if (drawable != null && drawable.isStateful()) {
            this.mBackground.setState(getDrawableState());
        }
    }

    private void updateTouchListener() {
        if (this.mExpandClickListener == null && this.mAppOpsListener == null) {
            setOnTouchListener(null);
            return;
        }
        setOnTouchListener(this.mTouchListener);
        this.mTouchListener.bindTouchRects();
    }

    public void setAppOpsOnClickListener(View.OnClickListener l) {
        this.mAppOpsListener = l;
        this.mAppOps.setOnClickListener(this.mAppOpsListener);
        this.mCameraIcon.setOnClickListener(this.mAppOpsListener);
        this.mMicIcon.setOnClickListener(this.mAppOpsListener);
        this.mOverlayIcon.setOnClickListener(this.mAppOpsListener);
        updateTouchListener();
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener l) {
        this.mExpandClickListener = l;
        this.mExpandButton.setOnClickListener(this.mExpandClickListener);
        updateTouchListener();
    }

    @RemotableViewMethod
    public void setOriginalIconColor(int color) {
        this.mIconColor = color;
    }

    public int getOriginalIconColor() {
        return this.mIconColor;
    }

    @RemotableViewMethod
    public void setOriginalNotificationColor(int color) {
        this.mOriginalNotificationColor = color;
    }

    public int getOriginalNotificationColor() {
        return this.mOriginalNotificationColor;
    }

    @RemotableViewMethod
    public void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
        updateExpandButton();
    }

    public void showAppOpsIcons(ArraySet<Integer> appOps) {
        View view = this.mOverlayIcon;
        if (view != null && this.mCameraIcon != null && this.mMicIcon != null && appOps != null) {
            int i = 0;
            view.setVisibility(appOps.contains(24) ? 0 : 8);
            this.mCameraIcon.setVisibility(appOps.contains(26) ? 0 : 8);
            View view2 = this.mMicIcon;
            if (!appOps.contains(27)) {
                i = 8;
            }
            view2.setVisibility(i);
        }
    }

    public void setRecentlyAudiblyAlerted(boolean audiblyAlerted) {
        View view = this.mAudiblyAlertedIcon;
        if (view != null) {
            view.setVisibility(audiblyAlerted ? 0 : 8);
        }
    }

    private void updateExpandButton() {
        int contentDescriptionId;
        int drawableId;
        if (this.mExpanded) {
            drawableId = R.drawable.ic_collapse_notification;
            contentDescriptionId = R.string.expand_button_content_description_expanded;
        } else {
            drawableId = R.drawable.ic_expand_notification;
            contentDescriptionId = R.string.expand_button_content_description_collapsed;
        }
        this.mExpandButton.setImageDrawable(getContext().getDrawable(drawableId));
        this.mExpandButton.setColorFilter(this.mOriginalNotificationColor);
        this.mExpandButton.setContentDescription(this.mContext.getText(contentDescriptionId));
    }

    public void setShowWorkBadgeAtEnd(boolean showWorkBadgeAtEnd) {
        if (showWorkBadgeAtEnd != this.mShowWorkBadgeAtEnd) {
            setClipToPadding(!showWorkBadgeAtEnd);
            this.mShowWorkBadgeAtEnd = showWorkBadgeAtEnd;
        }
    }

    public void setShowExpandButtonAtEnd(boolean showExpandButtonAtEnd) {
        if (showExpandButtonAtEnd != this.mShowExpandButtonAtEnd) {
            setClipToPadding(!showExpandButtonAtEnd);
            this.mShowExpandButtonAtEnd = showExpandButtonAtEnd;
        }
    }

    public View getWorkProfileIcon() {
        return this.mProfileBadge;
    }

    public CachingIconView getIcon() {
        return this.mIcon;
    }

    @RemotableViewMethod
    public void setHeaderTextMarginEnd(int headerTextMarginEnd) {
        if (this.mHeaderTextMarginEnd != headerTextMarginEnd) {
            this.mHeaderTextMarginEnd = headerTextMarginEnd;
            requestLayout();
        }
    }

    public int getHeaderTextMarginEnd() {
        return this.mHeaderTextMarginEnd;
    }

    public class HeaderTouchListener implements View.OnTouchListener {
        private Rect mAppOpsRect;
        private float mDownX;
        private float mDownY;
        private Rect mExpandButtonRect;
        private final ArrayList<Rect> mTouchRects = new ArrayList<>();
        private int mTouchSlop;
        private boolean mTrackGesture;

        public HeaderTouchListener() {
        }

        public void bindTouchRects() {
            this.mTouchRects.clear();
            if (NotificationHeaderView.this.mExpandClickListener != null) {
                addRectAroundView(NotificationHeaderView.this.mIcon);
                this.mExpandButtonRect = addRectAroundView(NotificationHeaderView.this.mExpandButton);
            }
            this.mAppOpsRect = addRectAroundView(NotificationHeaderView.this.mAppOps);
            if (NotificationHeaderView.this.mExpandClickListener != null) {
                addWidthRect();
            }
            this.mTouchSlop = ViewConfiguration.get(NotificationHeaderView.this.getContext()).getScaledTouchSlop();
        }

        private void addWidthRect() {
            Rect r = new Rect();
            r.top = 0;
            r.bottom = (int) (NotificationHeaderView.this.getResources().getDisplayMetrics().density * 32.0f);
            r.left = 0;
            r.right = NotificationHeaderView.this.getWidth();
            this.mTouchRects.add(r);
        }

        private Rect addRectAroundView(View view) {
            Rect r = getRectAroundView(view);
            this.mTouchRects.add(r);
            return r;
        }

        private Rect getRectAroundView(View view) {
            float size = NotificationHeaderView.this.getResources().getDisplayMetrics().density * 48.0f;
            float width = Math.max(size, (float) view.getWidth());
            float height = Math.max(size, (float) view.getHeight());
            Rect r = new Rect();
            if (view.getVisibility() == 8) {
                view = NotificationHeaderView.this.getFirstChildNotGone();
                r.left = (int) (((float) view.getLeft()) - (width / 2.0f));
            } else {
                r.left = (int) ((((float) (view.getLeft() + view.getRight())) / 2.0f) - (width / 2.0f));
            }
            r.top = (int) ((((float) (view.getTop() + view.getBottom())) / 2.0f) - (height / 2.0f));
            r.bottom = (int) (((float) r.top) + height);
            r.right = (int) (((float) r.left) + width);
            return r;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            int actionMasked = event.getActionMasked() & 255;
            if (actionMasked == 0) {
                this.mTrackGesture = false;
                if (isInside(x, y)) {
                    this.mDownX = x;
                    this.mDownY = y;
                    this.mTrackGesture = true;
                    return true;
                }
            } else if (actionMasked != 1) {
                if (actionMasked == 2 && this.mTrackGesture && (Math.abs(this.mDownX - x) > ((float) this.mTouchSlop) || Math.abs(this.mDownY - y) > ((float) this.mTouchSlop))) {
                    this.mTrackGesture = false;
                }
            } else if (this.mTrackGesture) {
                if (!NotificationHeaderView.this.mAppOps.isVisibleToUser() || (!this.mAppOpsRect.contains((int) x, (int) y) && !this.mAppOpsRect.contains((int) this.mDownX, (int) this.mDownY))) {
                    NotificationHeaderView.this.mExpandButton.performClick();
                } else {
                    NotificationHeaderView.this.mAppOps.performClick();
                    return true;
                }
            }
            return this.mTrackGesture;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isInside(float x, float y) {
            Rect rect;
            if (NotificationHeaderView.this.mAcceptAllTouches) {
                return true;
            }
            if (NotificationHeaderView.this.mExpandOnlyOnButton && (rect = this.mExpandButtonRect) != null) {
                return rect.contains((int) x, (int) y);
            }
            for (int i = 0; i < this.mTouchRects.size(); i++) {
                if (this.mTouchRects.get(i).contains((int) x, (int) y)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private View getFirstChildNotGone() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                return child;
            }
        }
        return this;
    }

    public ImageView getExpandButton() {
        return this.mExpandButton;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isInTouchRect(float x, float y) {
        if (this.mExpandClickListener == null) {
            return false;
        }
        return this.mTouchListener.isInside(x, y);
    }

    @RemotableViewMethod
    public void setAcceptAllTouches(boolean acceptAllTouches) {
        this.mAcceptAllTouches = this.mEntireHeaderClickable || acceptAllTouches;
    }

    @RemotableViewMethod
    public void setExpandOnlyOnButton(boolean expandOnlyOnButton) {
        this.mExpandOnlyOnButton = expandOnlyOnButton;
    }
}
