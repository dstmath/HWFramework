package huawei.android.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;
import java.lang.ref.WeakReference;

public class PullUpListView extends ListView implements AbsListView.OnScrollListener {
    private static final int DONE = 3;
    private static final long LOADING_TIMEOUT_DEFAULT = 15000;
    private static final int MATH_DIV = 2;
    private static final int MSG_SELF_REFRESHCOMPLETE = 1;
    private static final int PULL_TO_REFRESH = 1;
    private static final int REFRESHING = 2;
    private static final long REFRESHING_ANIMATION_DURATION = 1500;
    private static final float REFRESHING_ANIMATION_FROM_DEGREES = 0.0f;
    private static final float REFRESHING_ANIMATION_PIVOT_X_VALUE = 0.5f;
    private static final float REFRESHING_ANIMATION_PIVOT_Y_VALUE = 0.5f;
    private static final float REFRESHING_ANIMATION_TO_DEGREES = 360.0f;
    private static final int REFRESH_HEIGHT = 2;
    private static final long RELEASED_ANIMATION_DURATION = 350;
    private static final int RELEASED_TO_FALLBACK = 0;
    private static final String TAG = "PullUpListView";
    private static final float TOUCH_SCROLL_SCALE = 0.55f;
    private Context mContext;
    private int mCurrentPadding = 0;
    private TimeInterpolator mFallbackSinInterpolator;
    private int mFootHeight;
    private RelativeLayout mFooterView;
    private PullupListviewHandler mHandler;
    private boolean mIsActionDownHold = false;
    private boolean mIsAnimatingFallBack = false;
    private boolean mIsBottomDownRecorded = false;
    private boolean mIsLastPosition = false;
    private boolean mIsPullUpEnabled;
    private boolean mIsRefreshing = false;
    private ImageView mIvPullupListviewRefreshingImage;
    private long mLoadingTimeout = LOADING_TIMEOUT_DEFAULT;
    private OnRefreshListener mRefreshListener;
    private RotateAnimation mRefreshingAnimation;
    private LinearLayout mRefreshingBar;
    private int mRefreshingHeight;
    private float mStartY;
    private int mState = 1;
    private TextView mTvPullupRefreshingTitle;

    public interface OnRefreshListener {
        void onRefresh();
    }

    public PullUpListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initInternal(context);
    }

    public PullUpListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initInternal(context);
    }

    public PullUpListView(Context context) {
        super(context);
        initInternal(context);
    }

    private void initInternal(Context context) {
        this.mContext = context;
        this.mFooterView = (RelativeLayout) ResLoaderUtil.getLayout(this.mContext, "pullup_listview_foot", null, false);
        this.mHandler = new PullupListviewHandler();
        this.mRefreshingBar = (LinearLayout) this.mFooterView.findViewById(ResLoaderUtil.getViewId(this.mContext, "pullup_listview_loadingbar"));
        addFooterView(this.mFooterView);
        measureView(this.mFooterView);
        measureView(this.mRefreshingBar);
        this.mFootHeight = this.mFooterView.getMeasuredHeight();
        this.mRefreshingHeight = this.mRefreshingBar.getMeasuredHeight();
        this.mFooterView.setPadding(0, 0, 0, this.mFootHeight * -1);
        this.mRefreshingBar.setPadding(0, this.mRefreshingHeight * -1, 0, 0);
        this.mFooterView.setVisibility(8);
        this.mRefreshingBar.setVisibility(8);
        setOnScrollListener(this);
        this.mRefreshingAnimation = new RotateAnimation(REFRESHING_ANIMATION_FROM_DEGREES, REFRESHING_ANIMATION_TO_DEGREES, 1, 0.5f, 1, 0.5f);
        this.mRefreshingAnimation.setFillAfter(true);
        this.mRefreshingAnimation.setRepeatCount(-1);
        this.mRefreshingAnimation.setDuration(REFRESHING_ANIMATION_DURATION);
        this.mRefreshingAnimation.setInterpolator(new LinearInterpolator());
        this.mIvPullupListviewRefreshingImage = (ImageView) this.mFooterView.findViewById(ResLoaderUtil.getViewId(this.mContext, "iv_pullup_listview_refreshing_image"));
        this.mTvPullupRefreshingTitle = (TextView) this.mFooterView.findViewById(ResLoaderUtil.getViewId(this.mContext, "tv_pullup_refreshing_title"));
        this.mFallbackSinInterpolator = new PullupFallbackSinInterpolator();
    }

    public void init(Context context) {
        initInternal(context);
    }

    /* access modifiers changed from: private */
    public static class PullupFallbackSinInterpolator implements TimeInterpolator {
        private PullupFallbackSinInterpolator() {
        }

        public float getInterpolation(float input) {
            return (float) Math.sin(((double) (input / 2.0f)) * 3.141592653589793d);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        RotateAnimation rotateAnimation;
        boolean currentIsLastPostion = true;
        if (getLastVisiblePosition() != getCount() - 1) {
            currentIsLastPostion = false;
        }
        if (this.mIsLastPosition != currentIsLastPostion) {
            this.mIsLastPosition = currentIsLastPostion;
            ImageView imageView = this.mIvPullupListviewRefreshingImage;
            if (imageView != null && currentIsLastPostion && imageView.getVisibility() == 0 && this.mIsRefreshing && (rotateAnimation = this.mRefreshingAnimation) != null) {
                this.mIvPullupListviewRefreshingImage.startAnimation(rotateAnimation);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mIsPullUpEnabled || this.mRefreshListener == null) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        if (action == 0) {
            downTouch(event);
        } else if (action == 1) {
            upTouch();
        } else if (action == 2) {
            int tempY = (int) event.getY();
            moveTouch(tempY);
            int distance = (int) ((this.mStartY - event.getY()) * TOUCH_SCROLL_SCALE);
            if (this.mIsBottomDownRecorded) {
                if (this.mIsRefreshing && this.mFooterView.getPaddingBottom() >= 0) {
                    refreshAndBootom(distance);
                } else if (!this.mIsRefreshing || this.mCurrentPadding + distance + this.mFootHeight >= this.mRefreshingHeight || distance >= 0) {
                    this.mFooterView.setPadding(0, 0, 0, this.mCurrentPadding + distance);
                    if (this.mIsRefreshing) {
                        this.mRefreshingBar.setPadding(0, ((this.mFooterView.getPaddingBottom() + this.mFootHeight) - this.mRefreshingHeight) / 2, 0, 0);
                    }
                    int i = this.mState;
                    if (i == 1) {
                        pullToRefresh();
                    } else if (i == 3 && ((float) tempY) - this.mStartY > REFRESHING_ANIMATION_FROM_DEGREES) {
                        this.mState = 1;
                        changeHeaderViewByState();
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void pullToRefresh() {
        if (this.mFooterView.getPaddingBottom() + this.mFootHeight > this.mRefreshingHeight) {
            this.mState = 2;
            changeHeaderViewByState();
            this.mRefreshingBar.setPadding(0, ((this.mFooterView.getPaddingBottom() + this.mFootHeight) - this.mRefreshingHeight) / 2, 0, 0);
        } else if (this.mFooterView.getPaddingBottom() + this.mFootHeight < this.mRefreshingHeight && this.mFooterView.getPaddingBottom() + this.mFootHeight > 0) {
            this.mRefreshingBar.setPadding(0, 0, 0, 0);
        }
    }

    private void refreshAndBootom(int distance) {
        this.mFooterView.setPadding(0, 0, 0, this.mCurrentPadding + distance);
        this.mState = 0;
        changeHeaderViewByState();
    }

    private void upTouch() {
        this.mIsActionDownHold = false;
        int i = this.mState;
        if (i == 1) {
            this.mState = 3;
            changeHeaderViewByState();
        } else if (i == 2) {
            this.mState = 0;
            changeHeaderViewByState();
        }
        this.mIsBottomDownRecorded = false;
    }

    private void moveTouch(int tempY) {
        if (!this.mIsBottomDownRecorded && getLastVisiblePosition() == getCount() - 1 && !this.mIsRefreshing && !this.mIsActionDownHold) {
            this.mIsBottomDownRecorded = true;
            this.mStartY = (float) tempY;
            this.mFooterView.setVisibility(0);
            this.mRefreshingBar.setVisibility(0);
            this.mCurrentPadding = this.mFooterView.getPaddingBottom();
        }
    }

    private void downTouch(MotionEvent event) {
        if (getLastVisiblePosition() == getCount() - 1 && !this.mIsBottomDownRecorded) {
            this.mStartY = event.getY();
            this.mFooterView.setVisibility(0);
            this.mRefreshingBar.setVisibility(0);
            this.mIsBottomDownRecorded = true;
            this.mIsActionDownHold = true;
            this.mCurrentPadding = this.mFooterView.getPaddingBottom();
        }
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
    }

    public void onRefreshComplete() {
        this.mHandler.removeMessages(1);
        this.mState = 3;
        changeHeaderViewByState();
    }

    public void setLoadingTimeOut(long timeout) {
        this.mLoadingTimeout = timeout;
    }

    private void onRefresh() {
        this.mRefreshListener.onRefresh();
        this.mHandler.sendEmptyMessageDelayed(1, this.mLoadingTimeout);
    }

    private void changeHeaderViewByState() {
        int i = this.mState;
        if (i == 0) {
            this.mState = 2;
            if (!this.mIsAnimatingFallBack && this.mFooterView.getPaddingBottom() + this.mFootHeight > this.mRefreshingHeight) {
                releasedToFallbackSwitch();
            }
        } else if (i == 1) {
        } else {
            if (i != 2) {
                if (i == 3) {
                    this.mIsBottomDownRecorded = false;
                    if (this.mIsRefreshing && this.mIsActionDownHold) {
                        this.mIsActionDownHold = false;
                    }
                    doneSwitch();
                }
            } else if (!this.mIsRefreshing) {
                refreshingSwitch();
            }
        }
    }

    private void doneSwitch() {
        this.mIsRefreshing = false;
        this.mIsAnimatingFallBack = false;
        this.mIvPullupListviewRefreshingImage.clearAnimation();
        this.mRefreshingBar.setPadding(0, this.mRefreshingHeight * -1, 0, 0);
        this.mFooterView.setPadding(0, 0, 0, this.mFootHeight * -1);
        this.mIvPullupListviewRefreshingImage.setImageResource(ResLoaderUtil.getDrawableId(this.mContext, "pullup_pulltorefresh_icon"));
        this.mTvPullupRefreshingTitle.setText(ResLoaderUtil.getString(this.mContext, "pullup_to_refresh_text"));
        this.mRefreshingBar.setVisibility(8);
        this.mFooterView.setVisibility(8);
        this.mState = 1;
    }

    private void releasedToFallbackSwitch() {
        this.mIsBottomDownRecorded = false;
        ValueAnimator animation = ValueAnimator.ofInt(this.mFooterView.getPaddingBottom(), (this.mFootHeight - this.mRefreshingHeight) * -1);
        animation.setDuration(RELEASED_ANIMATION_DURATION);
        animation.setInterpolator(this.mFallbackSinInterpolator);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.PullUpListView.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int paddingdistance = 0;
                if (valueAnimator.getAnimatedValue() != null) {
                    try {
                        paddingdistance = Integer.parseInt(valueAnimator.getAnimatedValue().toString());
                    } catch (NumberFormatException e) {
                        Log.e(PullUpListView.TAG, "Integer.parseInt failed, AnimatedValue: " + valueAnimator.getAnimatedValue().toString());
                    }
                }
                if (PullUpListView.this.mIsRefreshing) {
                    PullUpListView.this.mFooterView.setPadding(0, 0, 0, paddingdistance);
                    PullUpListView.this.mRefreshingBar.setPadding(0, ((PullUpListView.this.mFootHeight + PullUpListView.this.mFooterView.getPaddingBottom()) - PullUpListView.this.mRefreshingHeight) / 2, 0, 0);
                    if (paddingdistance == PullUpListView.this.mRefreshingHeight - PullUpListView.this.mFootHeight && PullUpListView.this.mIsRefreshing) {
                        PullUpListView.this.mIsAnimatingFallBack = false;
                    }
                }
            }
        });
        this.mIsAnimatingFallBack = true;
        animation.start();
    }

    private void refreshingSwitch() {
        this.mRefreshingBar.setVisibility(0);
        this.mIvPullupListviewRefreshingImage.setImageResource(ResLoaderUtil.getDrawableId(this.mContext, "pullup_refreshing_icon"));
        this.mTvPullupRefreshingTitle.setText(ResLoaderUtil.getString(this.mContext, "pullup_refreshing_text"));
        this.mIvPullupListviewRefreshingImage.startAnimation(this.mRefreshingAnimation);
        onRefresh();
        this.mIsRefreshing = true;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        super.finalize();
        if (this.mFooterView == null) {
            Log.w(TAG, "mHeadView == null");
        }
        if (this.mHandler == null) {
            Log.w(TAG, "mHandler == null");
            return;
        }
        Log.w(TAG, "mHandler =" + this.mHandler);
    }

    /* access modifiers changed from: private */
    public static class PullupListviewHandler extends Handler {
        private WeakReference<PullUpListView> mPullWeakReference;

        private PullupListviewHandler(PullUpListView pullupListView) {
            this.mPullWeakReference = new WeakReference<>(pullupListView);
        }

        public void handleMessage(Message msg) {
            PullUpListView pullUpListView = null;
            WeakReference<PullUpListView> weakReference = this.mPullWeakReference;
            if (weakReference != null) {
                pullUpListView = weakReference.get();
            }
            if (pullUpListView != null && msg.what == 1) {
                pullUpListView.onRefreshComplete();
            }
        }
    }

    public void setPullUpEnable(boolean isPullUpEnabled) {
        this.mIsPullUpEnabled = isPullUpEnabled;
    }

    public boolean isSelectDownEnable() {
        return this.mIsPullUpEnabled;
    }

    private void measureView(View child) {
        int childHeightSpec;
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(-1, -1);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
        int lpHeight = params.height;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }
}
