package huawei.android.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;

public class PullUpListView extends ListView implements OnScrollListener {
    private static final int DONE = 3;
    private static final int MSG_SELF_REFRESHCOMPLETE = 1;
    private static final int PULL_To_REFRESH = 1;
    private static final int REFRESHING = 2;
    private static final int RELEASED_TO_FALLBACK = 0;
    private static final String TAG = "PullUpListView";
    private static final float TOUCH_SCROLL_SCALE = 0.55f;
    private ImageView iv_pullup_listview_refreshing_image;
    private boolean mActionDownHold = false;
    private boolean mBottomDownRecorded = false;
    private Context mContext;
    private int mCurrentPadding = 0;
    private TimeInterpolator mFallbackSinInterpolator;
    private int mFootHeight;
    private RelativeLayout mFooterView;
    private PullupListviewHandler mHandler;
    private boolean mIsAnimatingFallBack = false;
    private boolean mIsLastPosition = false;
    private boolean mIsRefreshing = false;
    private long mLoadingTimeout = 15000;
    private boolean mPullUpEnabled;
    public OnRefreshListener mRefreshListener;
    private RotateAnimation mRefreshingAnimation;
    private LinearLayout mRefreshingBar;
    private int mRefreshingHeight;
    private float mStartY;
    private int mState = 1;
    private TextView tv_pullup_refreshing_title;

    public interface OnRefreshListener {
        void onRefresh();
    }

    private static class PullupFallbackSinInterpolator implements TimeInterpolator {
        /* synthetic */ PullupFallbackSinInterpolator(PullupFallbackSinInterpolator -this0) {
            this();
        }

        private PullupFallbackSinInterpolator() {
        }

        public float getInterpolation(float input) {
            return (float) Math.sin(((double) (input / 2.0f)) * 3.141592653589793d);
        }
    }

    private static class PullupListviewHandler extends Handler {
        private WeakReference<PullUpListView> pullWeakReference;

        public PullupListviewHandler(PullUpListView pullupListView) {
            this.pullWeakReference = new WeakReference(pullupListView);
        }

        public void handleMessage(Message msg) {
            PullUpListView pullUpListView = null;
            if (this.pullWeakReference != null) {
                pullUpListView = (PullUpListView) this.pullWeakReference.get();
            }
            if (pullUpListView != null) {
                switch (msg.what) {
                    case 1:
                        pullUpListView.onRefreshComplete();
                        break;
                }
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
        this.mFooterView = (RelativeLayout) View.inflate(this.mContext, 34013251, null);
        this.mHandler = new PullupListviewHandler(this);
        this.mRefreshingBar = (LinearLayout) this.mFooterView.findViewById(34603124);
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
        this.mRefreshingAnimation = new RotateAnimation(0.0f, 360.0f, 1, 0.5f, 1, 0.5f);
        this.mRefreshingAnimation.setFillAfter(true);
        this.mRefreshingAnimation.setRepeatCount(-1);
        this.mRefreshingAnimation.setDuration(1500);
        this.mRefreshingAnimation.setInterpolator(new LinearInterpolator());
        this.iv_pullup_listview_refreshing_image = (ImageView) this.mFooterView.findViewById(34603125);
        this.tv_pullup_refreshing_title = (TextView) this.mFooterView.findViewById(34603126);
        this.mFallbackSinInterpolator = new PullupFallbackSinInterpolator();
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean currentIsLastPostion = getLastVisiblePosition() == getCount() + -1;
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
                if (getLastVisiblePosition() == getCount() - 1 && (this.mBottomDownRecorded ^ 1) != 0) {
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
                if (!(this.mBottomDownRecorded || getLastVisiblePosition() != getCount() - 1 || (this.mIsRefreshing ^ 1) == 0 || (this.mActionDownHold ^ 1) == 0)) {
                    this.mBottomDownRecorded = true;
                    this.mStartY = (float) tempY;
                    this.mFooterView.setVisibility(0);
                    this.mRefreshingBar.setVisibility(0);
                    this.mCurrentPadding = this.mFooterView.getPaddingBottom();
                }
                int distance = (int) ((this.mStartY - ev.getY()) * TOUCH_SCROLL_SCALE);
                if (this.mBottomDownRecorded) {
                    if (!this.mIsRefreshing || this.mFooterView.getPaddingBottom() < 0) {
                        if (!this.mIsRefreshing || (this.mCurrentPadding + distance) + this.mFootHeight >= this.mRefreshingHeight || distance >= 0) {
                            this.mFooterView.setPadding(0, 0, 0, this.mCurrentPadding + distance);
                            if (this.mIsRefreshing) {
                                this.mRefreshingBar.setPadding(0, ((this.mFooterView.getPaddingBottom() + this.mFootHeight) - this.mRefreshingHeight) / 2, 0, 0);
                            }
                            if (1 != this.mState) {
                                if (this.mState == 3 && ((float) tempY) - this.mStartY > 0.0f) {
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
                    }
                    this.mFooterView.setPadding(0, 0, 0, this.mCurrentPadding + distance);
                    this.mState = 0;
                    changeHeaderViewByState();
                    break;
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
                    ValueAnimator animation = ValueAnimator.ofInt(new int[]{this.mFooterView.getPaddingBottom(), (this.mFootHeight - this.mRefreshingHeight) * -1});
                    animation.setDuration(350);
                    animation.setInterpolator(this.mFallbackSinInterpolator);
                    animation.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int paddingdistance = Integer.parseInt(valueAnimator.getAnimatedValue().toString());
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
                    break;
                }
            case 2:
                if (!this.mIsRefreshing) {
                    this.mRefreshingBar.setVisibility(0);
                    this.iv_pullup_listview_refreshing_image.setImageResource(33751295);
                    this.tv_pullup_refreshing_title.setText(33685884);
                    this.iv_pullup_listview_refreshing_image.startAnimation(this.mRefreshingAnimation);
                    onRefresh();
                    this.mIsRefreshing = true;
                    break;
                }
                return;
            case 3:
                this.mBottomDownRecorded = false;
                if (this.mIsRefreshing && this.mActionDownHold) {
                    this.mActionDownHold = false;
                }
                this.mIsRefreshing = false;
                this.mIsAnimatingFallBack = false;
                this.iv_pullup_listview_refreshing_image.clearAnimation();
                this.mRefreshingBar.setPadding(0, this.mRefreshingHeight * -1, 0, 0);
                this.mFooterView.setPadding(0, 0, 0, this.mFootHeight * -1);
                this.iv_pullup_listview_refreshing_image.setImageResource(33751294);
                this.tv_pullup_refreshing_title.setText(33685885);
                this.mRefreshingBar.setVisibility(8);
                this.mFooterView.setVisibility(8);
                this.mState = 1;
                break;
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (this.mFooterView == null) {
            Log.w(TAG, "mHeadView == null");
        }
        if (this.mHandler == null) {
            Log.w(TAG, "mHandler == null");
        } else {
            Log.w(TAG, "mHandler =" + this.mHandler);
        }
    }

    public void setPullUpEnable(boolean pullUpEnabled) {
        this.mPullUpEnabled = pullUpEnabled;
    }

    public boolean isSelectDownEnable() {
        return this.mPullUpEnabled;
    }

    private void measureView(View child) {
        int childHeightSpec;
        LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new LayoutParams(-1, -1);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }
}
