package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View.AccessibilityDelegate;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.Protocol;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;

@RemoteView
public class NotificationHeaderView extends ViewGroup {
    public static final int NO_COLOR = -1;
    private View mAppName;
    private Drawable mBackground;
    private final int mChildMinWidth;
    private final int mContentEndMargin;
    private ImageView mExpandButton;
    private OnClickListener mExpandClickListener;
    final AccessibilityDelegate mExpandDelegate;
    private boolean mExpanded;
    private int mHeaderBackgroundHeight;
    private View mHeaderText;
    private View mIcon;
    private int mIconColor;
    private View mInfo;
    private int mOriginalNotificationColor;
    private View mProfileBadge;
    ViewOutlineProvider mProvider;
    private boolean mShowWorkBadgeAtEnd;
    private HeaderTouchListener mTouchListener;

    public class HeaderTouchListener implements OnTouchListener {
        private float mDownX;
        private float mDownY;
        private final ArrayList<Rect> mTouchRects;
        private int mTouchSlop;
        private boolean mTrackGesture;

        public HeaderTouchListener() {
            this.mTouchRects = new ArrayList();
        }

        public void bindTouchRects() {
            this.mTouchRects.clear();
            addRectAroundViewView(NotificationHeaderView.this.mIcon);
            addRectAroundViewView(NotificationHeaderView.this.mExpandButton);
            addWidthRect();
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

        private void addRectAroundViewView(View view) {
            this.mTouchRects.add(getRectAroundView(view));
        }

        private Rect getRectAroundView(View view) {
            float size = 48.0f * NotificationHeaderView.this.getResources().getDisplayMetrics().density;
            Rect r = new Rect();
            if (view.getVisibility() == 8) {
                view = NotificationHeaderView.this.getFirstChildNotGone();
                r.left = (int) (((float) view.getLeft()) - (size / 2.0f));
            } else {
                r.left = (int) ((((float) (view.getLeft() + view.getRight())) / 2.0f) - (size / 2.0f));
            }
            r.top = (int) ((((float) (view.getTop() + view.getBottom())) / 2.0f) - (size / 2.0f));
            r.bottom = (int) (((float) r.top) + size);
            r.right = (int) (((float) r.left) + size);
            return r;
        }

        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getActionMasked() & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    this.mTrackGesture = false;
                    if (isInside(x, y)) {
                        this.mTrackGesture = true;
                        return true;
                    }
                    break;
                case HwCfgFilePolicy.EMUI /*1*/:
                    if (this.mTrackGesture) {
                        NotificationHeaderView.this.mExpandClickListener.onClick(NotificationHeaderView.this);
                        break;
                    }
                    break;
                case HwCfgFilePolicy.PC /*2*/:
                    if (this.mTrackGesture && (Math.abs(this.mDownX - x) > ((float) this.mTouchSlop) || Math.abs(this.mDownY - y) > ((float) this.mTouchSlop))) {
                        this.mTrackGesture = false;
                        break;
                    }
            }
            return this.mTrackGesture;
        }

        private boolean isInside(float x, float y) {
            for (int i = 0; i < this.mTouchRects.size(); i++) {
                if (((Rect) this.mTouchRects.get(i)).contains((int) x, (int) y)) {
                    this.mDownX = x;
                    this.mDownY = y;
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
                    outline.setRect(0, 0, NotificationHeaderView.this.getWidth(), NotificationHeaderView.this.mHeaderBackgroundHeight);
                    outline.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                }
            }
        };
        this.mExpandDelegate = new AccessibilityDelegate() {
            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                if (super.performAccessibilityAction(host, action, args)) {
                    return true;
                }
                if (action != Protocol.BASE_CONNECTIVITY_MANAGER && action != Protocol.BASE_DATA_CONNECTION) {
                    return false;
                }
                NotificationHeaderView.this.mExpandClickListener.onClick(NotificationHeaderView.this.mExpandButton);
                return true;
            }

            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName(getClass().getName());
                if (NotificationHeaderView.this.mExpanded) {
                    info.addAction(AccessibilityAction.ACTION_COLLAPSE);
                } else {
                    info.addAction(AccessibilityAction.ACTION_EXPAND);
                }
            }
        };
        this.mChildMinWidth = getResources().getDimensionPixelSize(R.dimen.notification_header_shrink_min_width);
        this.mContentEndMargin = getResources().getDimensionPixelSize(R.dimen.notification_content_margin_end);
        this.mHeaderBackgroundHeight = getResources().getDimensionPixelSize(R.dimen.notification_header_background_height);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAppName = findViewById(R.id.app_name_text);
        this.mHeaderText = findViewById(R.id.header_text);
        this.mExpandButton = (ImageView) findViewById(R.id.expand_button);
        if (this.mExpandButton != null) {
            this.mExpandButton.setAccessibilityDelegate(this.mExpandDelegate);
        }
        this.mIcon = findViewById(R.id.icon);
        this.mProfileBadge = findViewById(R.id.profile_badge);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = MeasureSpec.getSize(heightMeasureSpec);
        int wrapContentWidthSpec = MeasureSpec.makeMeasureSpec(givenWidth, RtlSpacingHelper.UNDEFINED);
        int wrapContentHeightSpec = MeasureSpec.makeMeasureSpec(givenHeight, RtlSpacingHelper.UNDEFINED);
        int totalWidth = getPaddingStart() + getPaddingEnd();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int i2 = lp.rightMargin;
                i2 = lp.width;
                i2 = lp.bottomMargin;
                child.measure(ViewGroup.getChildMeasureSpec(wrapContentWidthSpec, lp.leftMargin + r0, r0), ViewGroup.getChildMeasureSpec(wrapContentHeightSpec, lp.topMargin + r0, lp.height));
                totalWidth += (lp.leftMargin + lp.rightMargin) + child.getMeasuredWidth();
            }
        }
        if (totalWidth > givenWidth) {
            int overFlow = totalWidth - givenWidth;
            int appWidth = this.mAppName.getMeasuredWidth();
            if (overFlow > 0 && this.mAppName.getVisibility() != 8 && appWidth > this.mChildMinWidth) {
                int newSize = appWidth - Math.min(appWidth - this.mChildMinWidth, overFlow);
                this.mAppName.measure(MeasureSpec.makeMeasureSpec(newSize, RtlSpacingHelper.UNDEFINED), wrapContentHeightSpec);
                overFlow -= appWidth - newSize;
            }
            if (overFlow > 0 && this.mHeaderText.getVisibility() != 8) {
                this.mHeaderText.measure(MeasureSpec.makeMeasureSpec(Math.max(0, this.mHeaderText.getMeasuredWidth() - overFlow), RtlSpacingHelper.UNDEFINED), wrapContentHeightSpec);
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
                if (child == this.mProfileBadge) {
                    int paddingEnd = getPaddingEnd();
                    if (this.mShowWorkBadgeAtEnd) {
                        paddingEnd = this.mContentEndMargin;
                    }
                    layoutRight = getWidth() - paddingEnd;
                    layoutLeft = layoutRight - child.getMeasuredWidth();
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
        return new MarginLayoutParams(getContext(), attrs);
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

    protected void onDraw(Canvas canvas) {
        if (this.mBackground != null) {
            this.mBackground.setBounds(0, 0, getWidth(), this.mHeaderBackgroundHeight);
            this.mBackground.draw(canvas);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mBackground;
    }

    protected void drawableStateChanged() {
        if (this.mBackground != null && this.mBackground.isStateful()) {
            this.mBackground.setState(getDrawableState());
        }
    }

    private void updateTouchListener() {
        if (this.mExpandClickListener != null) {
            this.mTouchListener.bindTouchRects();
        }
    }

    public void setOnClickListener(OnClickListener l) {
        OnTouchListener onTouchListener = null;
        this.mExpandClickListener = l;
        if (this.mExpandClickListener != null) {
            onTouchListener = this.mTouchListener;
        }
        setOnTouchListener(onTouchListener);
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

    private void updateExpandButton() {
        int drawableId;
        if (this.mExpanded) {
            drawableId = R.drawable.ic_collapse_notification;
        } else {
            drawableId = R.drawable.ic_expand_notification;
        }
        this.mExpandButton.setImageDrawable(getContext().getDrawable(drawableId));
        this.mExpandButton.setColorFilter(this.mOriginalNotificationColor);
    }

    public void setShowWorkBadgeAtEnd(boolean showWorkBadgeAtEnd) {
        if (showWorkBadgeAtEnd != this.mShowWorkBadgeAtEnd) {
            setClipToPadding(!showWorkBadgeAtEnd);
            this.mShowWorkBadgeAtEnd = showWorkBadgeAtEnd;
        }
    }

    public View getWorkProfileIcon() {
        return this.mProfileBadge;
    }

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

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isInTouchRect(float x, float y) {
        if (this.mExpandClickListener == null) {
            return false;
        }
        return this.mTouchListener.isInside(x, y);
    }
}
