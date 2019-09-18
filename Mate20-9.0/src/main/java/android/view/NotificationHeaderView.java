package android.view;

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
import com.android.internal.widget.CachingIconView;
import java.util.ArrayList;

@RemoteViews.RemoteView
public class NotificationHeaderView extends ViewGroup {
    public static final int NO_COLOR = 1;
    /* access modifiers changed from: private */
    public boolean mAcceptAllTouches;
    private View mAppName;
    /* access modifiers changed from: private */
    public View mAppOps;
    private View.OnClickListener mAppOpsListener;
    /* access modifiers changed from: private */
    public Drawable mBackground;
    private View mCameraIcon;
    private final int mChildMinWidth;
    private final int mContentEndMargin;
    private boolean mEntireHeaderClickable;
    /* access modifiers changed from: private */
    public ImageView mExpandButton;
    private View.OnClickListener mExpandClickListener;
    /* access modifiers changed from: private */
    public boolean mExpandOnlyOnButton;
    private boolean mExpanded;
    private final int mGravity;
    private View mHeaderText;
    /* access modifiers changed from: private */
    public CachingIconView mIcon;
    private int mIconColor;
    private View mMicIcon;
    private int mOriginalNotificationColor;
    private View mOverlayIcon;
    private View mProfileBadge;
    private int mProgressBarNotificationColor;
    ViewOutlineProvider mProvider;
    private View mSecondaryHeaderText;
    private boolean mShowExpandButtonAtEnd;
    private boolean mShowWorkBadgeAtEnd;
    private int mTotalWidth;
    private HeaderTouchListener mTouchListener;

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
            addRectAroundView(NotificationHeaderView.this.mIcon);
            this.mExpandButtonRect = addRectAroundView(NotificationHeaderView.this.mExpandButton);
            this.mAppOpsRect = addRectAroundView(NotificationHeaderView.this.mAppOps);
            addWidthRect();
            this.mTouchSlop = ViewConfiguration.get(NotificationHeaderView.this.getContext()).getScaledTouchSlop();
        }

        private void addWidthRect() {
            Rect r = new Rect();
            r.top = 0;
            r.bottom = (int) (32.0f * NotificationHeaderView.this.getResources().getDisplayMetrics().density);
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
            float size = 48.0f * NotificationHeaderView.this.getResources().getDisplayMetrics().density;
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

        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getActionMasked() & 255) {
                case 0:
                    this.mTrackGesture = false;
                    if (isInside(x, y)) {
                        this.mDownX = x;
                        this.mDownY = y;
                        this.mTrackGesture = true;
                        return true;
                    }
                    break;
                case 1:
                    if (this.mTrackGesture) {
                        if (!NotificationHeaderView.this.mAppOps.isVisibleToUser() || (!this.mAppOpsRect.contains((int) x, (int) y) && !this.mAppOpsRect.contains((int) this.mDownX, (int) this.mDownY))) {
                            NotificationHeaderView.this.mExpandButton.performClick();
                            break;
                        } else {
                            NotificationHeaderView.this.mAppOps.performClick();
                            return true;
                        }
                    }
                    break;
                case 2:
                    if (this.mTrackGesture && (Math.abs(this.mDownX - x) > ((float) this.mTouchSlop) || Math.abs(this.mDownY - y) > ((float) this.mTouchSlop))) {
                        this.mTrackGesture = false;
                        break;
                    }
            }
            return this.mTrackGesture;
        }

        /* access modifiers changed from: private */
        public boolean isInside(float x, float y) {
            if (NotificationHeaderView.this.mAcceptAllTouches) {
                return true;
            }
            if (NotificationHeaderView.this.mExpandOnlyOnButton) {
                return this.mExpandButtonRect.contains((int) x, (int) y);
            }
            for (int i = 0; i < this.mTouchRects.size(); i++) {
                if (this.mTouchRects.get(i).contains((int) x, (int) y)) {
                    return true;
                }
            }
            return false;
        }
    }

    public NotificationHeaderView(Context context) {
        this(context, null);
    }

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
            public void getOutline(View view, Outline outline) {
                if (NotificationHeaderView.this.mBackground != null) {
                    outline.setRect(0, 0, NotificationHeaderView.this.getWidth(), NotificationHeaderView.this.getHeight());
                    outline.setAlpha(1.0f);
                }
            }
        };
        Resources res = getResources();
        this.mChildMinWidth = res.getDimensionPixelSize(17105228);
        this.mContentEndMargin = res.getDimensionPixelSize(17105206);
        this.mEntireHeaderClickable = res.getBoolean(17956998);
        TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{16842927}, defStyleAttr, defStyleRes);
        this.mGravity = ta.getInt(0, 0);
        ta.recycle();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mAppName = findViewById(16908739);
        this.mHeaderText = findViewById(16908958);
        this.mSecondaryHeaderText = findViewById(16908960);
        this.mExpandButton = (ImageView) findViewById(16908881);
        this.mIcon = findViewById(16908294);
        this.mProfileBadge = findViewById(16909225);
        this.mCameraIcon = findViewById(16908797);
        this.mMicIcon = findViewById(16909088);
        this.mOverlayIcon = findViewById(16909176);
        this.mAppOps = findViewById(16908740);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        int wrapContentWidthSpec = View.MeasureSpec.makeMeasureSpec(givenWidth, Integer.MIN_VALUE);
        int wrapContentHeightSpec = View.MeasureSpec.makeMeasureSpec(givenHeight, Integer.MIN_VALUE);
        int totalWidth = getPaddingStart() + getPaddingEnd();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                child.measure(getChildMeasureSpec(wrapContentWidthSpec, lp.leftMargin + lp.rightMargin, lp.width), getChildMeasureSpec(wrapContentHeightSpec, lp.topMargin + lp.bottomMargin, lp.height));
                totalWidth += lp.leftMargin + lp.rightMargin + child.getMeasuredWidth();
            }
        }
        if (totalWidth > givenWidth) {
            int overFlow = shrinkViewForOverflow(wrapContentHeightSpec, shrinkViewForOverflow(wrapContentHeightSpec, totalWidth - givenWidth, this.mAppName, this.mChildMinWidth), this.mHeaderText, 0);
            if (this.mSecondaryHeaderText != null) {
                shrinkViewForOverflow(wrapContentHeightSpec, overFlow, this.mSecondaryHeaderText, 0);
            }
        }
        this.mTotalWidth = Math.min(totalWidth, givenWidth);
        setMeasuredDimension(givenWidth, givenHeight);
    }

    private int shrinkViewForOverflow(int heightSpec, int overFlow, View targetView, int minimumWidth) {
        int oldWidth = targetView.getMeasuredWidth();
        if (overFlow <= 0 || targetView.getVisibility() == 8 || oldWidth <= minimumWidth) {
            return overFlow;
        }
        int newSize = Math.max(minimumWidth, oldWidth - overFlow);
        targetView.measure(View.MeasureSpec.makeMeasureSpec(newSize, Integer.MIN_VALUE), heightSpec);
        return overFlow - (oldWidth - newSize);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
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
                int left2 = left + params.getMarginStart();
                int right = child.getMeasuredWidth() + left2;
                int top = (int) (((float) getPaddingTop()) + (((float) (ownHeight - childHeight)) / 2.0f));
                int bottom = top + childHeight;
                int layoutLeft = left2;
                int layoutRight = right;
                if (child == this.mExpandButton && this.mShowExpandButtonAtEnd) {
                    layoutRight = end - this.mContentEndMargin;
                    int measuredWidth = layoutRight - child.getMeasuredWidth();
                    layoutLeft = measuredWidth;
                    end = measuredWidth;
                }
                if (child == this.mProfileBadge) {
                    int paddingEnd = getPaddingEnd();
                    int i2 = left2;
                    if (this.mShowWorkBadgeAtEnd != 0) {
                        paddingEnd = this.mContentEndMargin;
                    }
                    layoutRight = end - paddingEnd;
                    int measuredWidth2 = layoutRight - child.getMeasuredWidth();
                    layoutLeft = measuredWidth2;
                    end = measuredWidth2;
                }
                if (child == this.mAppOps) {
                    layoutRight = end - this.mContentEndMargin;
                    int measuredWidth3 = layoutRight - child.getMeasuredWidth();
                    layoutLeft = measuredWidth3;
                    end = measuredWidth3;
                }
                if (getLayoutDirection() == 1) {
                    int ltrLeft = layoutLeft;
                    layoutLeft = getWidth() - layoutRight;
                    layoutRight = getWidth() - ltrLeft;
                }
                child.layout(layoutLeft, top, layoutRight, bottom);
                left = right + params.getMarginEnd();
            }
        }
        updateTouchListener();
    }

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
    public void onDraw(Canvas canvas) {
        if (this.mBackground != null) {
            this.mBackground.setBounds(0, 0, getWidth(), getHeight());
            this.mBackground.draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mBackground;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        if (this.mBackground != null && this.mBackground.isStateful()) {
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
        if (this.mOverlayIcon != null && this.mCameraIcon != null && this.mMicIcon != null && appOps != null) {
            int i = 8;
            this.mOverlayIcon.setVisibility(appOps.contains(24) ? 0 : 8);
            this.mCameraIcon.setVisibility(appOps.contains(26) ? 0 : 8);
            View view = this.mMicIcon;
            if (appOps.contains(27)) {
                i = 0;
            }
            view.setVisibility(i);
        }
    }

    private void updateExpandButton() {
        int contentDescriptionId;
        int drawableId;
        if (this.mExpanded) {
            drawableId = 17302324;
            contentDescriptionId = 17040007;
        } else {
            drawableId = 17302381;
            contentDescriptionId = 17040006;
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

    /* access modifiers changed from: private */
    public View getFirstChildNotGone() {
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
