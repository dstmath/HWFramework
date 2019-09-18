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
    private static final int MSG_SELF_REFRESHCOMPLETE = 1;
    private static final int PULL_To_REFRESH = 1;
    private static final int REFRESHING = 2;
    private static final long REFRESHING_ANIMATION_DURATION = 1500;
    private static final float REFRESHING_ANIMATION_FROM_DEGREES = 0.0f;
    private static final float REFRESHING_ANIMATION_PIVOT_X_VALUE = 0.5f;
    private static final float REFRESHING_ANIMATION_PIVOT_Y_VALUE = 0.5f;
    private static final float REFRESHING_ANIMATION_TO_DEGREES = 360.0f;
    private static final long RELEASED_ANIMATION_DURATION = 350;
    private static final int RELEASED_TO_FALLBACK = 0;
    private static final String TAG = "PullUpListView";
    private static final float TOUCH_SCROLL_SCALE = 0.55f;
    private ImageView iv_pullup_listview_refreshing_image;
    private boolean mActionDownHold = false;
    private boolean mBottomDownRecorded = false;
    private Context mContext;
    private int mCurrentPadding = 0;
    private TimeInterpolator mFallbackSinInterpolator;
    /* access modifiers changed from: private */
    public int mFootHeight;
    /* access modifiers changed from: private */
    public RelativeLayout mFooterView;
    private PullupListviewHandler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsAnimatingFallBack = false;
    private boolean mIsLastPosition = false;
    /* access modifiers changed from: private */
    public boolean mIsRefreshing = false;
    private long mLoadingTimeout = 15000;
    private boolean mPullUpEnabled;
    public OnRefreshListener mRefreshListener;
    private RotateAnimation mRefreshingAnimation;
    /* access modifiers changed from: private */
    public LinearLayout mRefreshingBar;
    /* access modifiers changed from: private */
    public int mRefreshingHeight;
    private float mStartY;
    private int mState = 1;
    private TextView tv_pullup_refreshing_title;

    public interface OnRefreshListener {
        void onRefresh();
    }

    private static class PullupFallbackSinInterpolator implements TimeInterpolator {
        private PullupFallbackSinInterpolator() {
        }

        public float getInterpolation(float input) {
            return (float) Math.sin(((double) (input / 2.0f)) * 3.141592653589793d);
        }
    }

    private static class PullupListviewHandler extends Handler {
        private WeakReference<PullUpListView> pullWeakReference;

        public PullupListviewHandler(PullUpListView pullupListView) {
            this.pullWeakReference = new WeakReference<>(pullupListView);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: huawei.android.widget.PullUpListView} */
        /* JADX WARNING: Multi-variable type inference failed */
        public void handleMessage(Message msg) {
            PullUpListView pullUpListView = null;
            if (this.pullWeakReference != null) {
                pullUpListView = this.pullWeakReference.get();
            }
            if (pullUpListView != null && msg.what == 1) {
                pullUpListView.onRefreshComplete();
            }
        }
    }

    public PullUpListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public PullUpListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullUpListView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        this.mContext = context;
        this.mFooterView = (RelativeLayout) ResLoaderUtil.getLayout(this.mContext, "pullup_listview_foot", null, false);
        this.mHandler = new PullupListviewHandler(this);
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
        RotateAnimation rotateAnimation = new RotateAnimation(REFRESHING_ANIMATION_FROM_DEGREES, REFRESHING_ANIMATION_TO_DEGREES, 1, 0.5f, 1, 0.5f);
        this.mRefreshingAnimation = rotateAnimation;
        this.mRefreshingAnimation.setFillAfter(true);
        this.mRefreshingAnimation.setRepeatCount(-1);
        this.mRefreshingAnimation.setDuration(REFRESHING_ANIMATION_DURATION);
        this.mRefreshingAnimation.setInterpolator(new LinearInterpolator());
        this.iv_pullup_listview_refreshing_image = (ImageView) this.mFooterView.findViewById(ResLoaderUtil.getViewId(this.mContext, "iv_pullup_listview_refreshing_image"));
        this.tv_pullup_refreshing_title = (TextView) this.mFooterView.findViewById(ResLoaderUtil.getViewId(this.mContext, "tv_pullup_refreshing_title"));
        this.mFallbackSinInterpolator = new PullupFallbackSinInterpolator();
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean z = true;
        if (getLastVisiblePosition() != getCount() - 1) {
            z = false;
        }
        boolean currentIsLastPostion = z;
        if (this.mIsLastPosition != currentIsLastPostion) {
            this.mIsLastPosition = currentIsLastPostion;
            if (this.iv_pullup_listview_refreshing_image != null && currentIsLastPostion && this.iv_pullup_listview_refreshing_image.getVisibility() == 0 && this.mIsRefreshing && this.mRefreshingAnimation != null) {
                this.iv_pullup_listview_refreshing_image.startAnimation(this.mRefreshingAnimation);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!this.mPullUpEnabled || this.mRefreshListener == null) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case 0:
                if (getLastVisiblePosition() == getCount() - 1 && !this.mBottomDownRecorded) {
                    this.mStartY = ev.getY();
                    this.mFooterView.setVisibility(0);
                    this.mRefreshingBar.setVisibility(0);
                    this.mBottomDownRecorded = true;
                    this.mActionDownHold = true;
                    this.mCurrentPadding = this.mFooterView.getPaddingBottom();
                    break;
                }
            case 1:
                this.mActionDownHold = false;
                if (this.mState == 1) {
                    this.mState = 3;
                    changeHeaderViewByState();
                } else if (this.mState == 2) {
                    this.mState = 0;
                    changeHeaderViewByState();
                }
                this.mBottomDownRecorded = false;
                break;
            case 2:
                int tempY = (int) ev.getY();
                if (!this.mBottomDownRecorded && getLastVisiblePosition() == getCount() - 1 && !this.mIsRefreshing && !this.mActionDownHold) {
                    this.mBottomDownRecorded = true;
                    this.mStartY = (float) tempY;
                    this.mFooterView.setVisibility(0);
                    this.mRefreshingBar.setVisibility(0);
                    this.mCurrentPadding = this.mFooterView.getPaddingBottom();
                }
                int distance = (int) ((this.mStartY - ev.getY()) * TOUCH_SCROLL_SCALE);
                if (this.mBottomDownRecorded) {
                    if (!this.mIsRefreshing || this.mFooterView.getPaddingBottom() < 0) {
                        if (!this.mIsRefreshing || this.mCurrentPadding + distance + this.mFootHeight >= this.mRefreshingHeight || distance >= 0) {
                            this.mFooterView.setPadding(0, 0, 0, this.mCurrentPadding + distance);
                            if (this.mIsRefreshing) {
                                this.mRefreshingBar.setPadding(0, ((this.mFooterView.getPaddingBottom() + this.mFootHeight) - this.mRefreshingHeight) / 2, 0, 0);
                            }
                            if (1 != this.mState) {
                                if (this.mState == 3 && ((float) tempY) - this.mStartY > REFRESHING_ANIMATION_FROM_DEGREES) {
                                    this.mState = 1;
                                    changeHeaderViewByState();
                                    break;
                                }
                            } else if (this.mFooterView.getPaddingBottom() + this.mFootHeight <= this.mRefreshingHeight) {
                                if (this.mFooterView.getPaddingBottom() + this.mFootHeight < this.mRefreshingHeight && this.mFooterView.getPaddingBottom() + this.mFootHeight > 0) {
                                    this.mRefreshingBar.setPadding(0, 0, 0, 0);
                                    break;
                                }
                            } else {
                                this.mState = 2;
                                changeHeaderViewByState();
                                this.mRefreshingBar.setPadding(0, ((this.mFooterView.getPaddingBottom() + this.mFootHeight) - this.mRefreshingHeight) / 2, 0, 0);
                                break;
                            }
                        }
                    } else {
                        this.mFooterView.setPadding(0, 0, 0, this.mCurrentPadding + distance);
                        this.mState = 0;
                        changeHeaderViewByState();
                        break;
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
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
        switch (this.mState) {
            case 0:
                this.mState = 2;
                if (!this.mIsAnimatingFallBack && this.mFooterView.getPaddingBottom() + this.mFootHeight > this.mRefreshingHeight) {
                    this.mBottomDownRecorded = false;
                    ValueAnimator animation = ValueAnimator.ofInt(new int[]{this.mFooterView.getPaddingBottom(), -1 * (this.mFootHeight - this.mRefreshingHeight)});
                    animation.setDuration(RELEASED_ANIMATION_DURATION);
                    animation.setInterpolator(this.mFallbackSinInterpolator);
                    animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int paddingdistance = Integer.parseInt(valueAnimator.getAnimatedValue().toString());
                            if (PullUpListView.this.mIsRefreshing) {
                                PullUpListView.this.mFooterView.setPadding(0, 0, 0, paddingdistance);
                                PullUpListView.this.mRefreshingBar.setPadding(0, ((PullUpListView.this.mFootHeight + PullUpListView.this.mFooterView.getPaddingBottom()) - PullUpListView.this.mRefreshingHeight) / 2, 0, 0);
                                if (paddingdistance == PullUpListView.this.mRefreshingHeight - PullUpListView.this.mFootHeight && PullUpListView.this.mIsRefreshing) {
                                    boolean unused = PullUpListView.this.mIsAnimatingFallBack = false;
                                }
                            }
                        }
                    });
                    this.mIsAnimatingFallBack = true;
                    animation.start();
                    break;
                }
            case 2:
                if (!this.mIsRefreshing) {
                    this.mRefreshingBar.setVisibility(0);
                    this.iv_pullup_listview_refreshing_image.setImageResource(ResLoaderUtil.getDrawableId(this.mContext, "pullup_refreshing_icon"));
                    this.tv_pullup_refreshing_title.setText(ResLoaderUtil.getString(this.mContext, "pullup_refreshing_text"));
                    this.iv_pullup_listview_refreshing_image.startAnimation(this.mRefreshingAnimation);
                    onRefresh();
                    this.mIsRefreshing = true;
                    break;
                } else {
                    return;
                }
            case 3:
                this.mBottomDownRecorded = false;
                if (this.mIsRefreshing && this.mActionDownHold) {
                    this.mActionDownHold = false;
                }
                this.mIsRefreshing = false;
                this.mIsAnimatingFallBack = false;
                this.iv_pullup_listview_refreshing_image.clearAnimation();
                this.mRefreshingBar.setPadding(0, this.mRefreshingHeight * -1, 0, 0);
                this.mFooterView.setPadding(0, 0, 0, -1 * this.mFootHeight);
                this.iv_pullup_listview_refreshing_image.setImageResource(ResLoaderUtil.getDrawableId(this.mContext, "pullup_pulltorefresh_icon"));
                this.tv_pullup_refreshing_title.setText(ResLoaderUtil.getString(this.mContext, "pullup_to_refresh_text"));
                this.mRefreshingBar.setVisibility(8);
                this.mFooterView.setVisibility(8);
                this.mState = 1;
                break;
        }
    }

    /* access modifiers changed from: protected */
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

    public void setPullUpEnable(boolean pullUpEnabled) {
        this.mPullUpEnabled = pullUpEnabled;
    }

    public boolean isSelectDownEnable() {
        return this.mPullUpEnabled;
    }

    private void measureView(View child) {
        int childHeightSpec;
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(-1, -1);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }
}
