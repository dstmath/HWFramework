package huawei.android.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.text.format.HwDateUtils;
import java.lang.ref.WeakReference;

public class PullDownListView extends ListView implements OnScrollListener {
    private static final long ANIMATION_DURATION_MILLIS = 250;
    private static final int DONE = 3;
    private static final int FORMATTER_FLAGS = 68117;
    private static final long FRESH_TOLERANCE_MILLIS = 15000;
    private static final int MSG_SELF_REFRESHCOMPLETE = 1;
    private static final int PULL_To_REFRESH = 1;
    private static final int REFRESHING = 2;
    private static final int RELEASE_To_REFRESH = 0;
    private static final String TAG = "PullDownListView";
    private static final float TOUCH_SCROLL_SCALE = 0.55f;
    private ImageView mArrowImageView;
    private int mFirstItemIndex;
    private boolean mFlagIsBack;
    private boolean mFlagIsRecored;
    private SelfRefreshCompleteHandler mHandler;
    private int mHeadContentHeight;
    private RelativeLayout mHeadView;
    private LayoutInflater mInflater;
    private long mLastUpdateTime;
    private TextView mLastUpdatedTextView;
    private String mPreferenceName;
    private SharedPreferences mPreferences;
    private ProgressBar mProgressBar;
    public OnRefreshListener mRefreshListener;
    private RotateAnimation mReverseAnimation;
    private RotateAnimation mRotateAnimation;
    private boolean mSelectDownEnable;
    private int mStartY;
    private int mState;
    private TextView mTipsTextview;
    private String mUpdateAtValue;

    public interface OnRefreshListener {
        void onRefresh();
    }

    private static class SelfRefreshCompleteHandler extends Handler {
        private WeakReference<PullDownListView> pullWeakReference;

        public SelfRefreshCompleteHandler(PullDownListView pullDownListView) {
            this.pullWeakReference = new WeakReference(pullDownListView);
        }

        public void handleMessage(Message msg) {
            PullDownListView pullDownListView = null;
            if (this.pullWeakReference != null) {
                pullDownListView = (PullDownListView) this.pullWeakReference.get();
            }
            if (pullDownListView != null) {
                switch (msg.what) {
                    case PullDownListView.PULL_To_REFRESH /*1*/:
                        pullDownListView.onRefreshComplete();
                        break;
                }
            }
        }
    }

    public PullDownListView(Context context) {
        this(context, null);
    }

    public PullDownListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public PullDownListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, RELEASE_To_REFRESH);
    }

    public PullDownListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mState = DONE;
        this.mSelectDownEnable = false;
        this.mPreferenceName = null;
        init(context);
    }

    private void init(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mHeadView = (RelativeLayout) this.mInflater.inflate(34013250, null);
        this.mArrowImageView = (ImageView) this.mHeadView.findViewById(34603169);
        this.mProgressBar = (ProgressBar) this.mHeadView.findViewById(34603168);
        this.mTipsTextview = (TextView) this.mHeadView.findViewById(34603170);
        this.mLastUpdatedTextView = (TextView) this.mHeadView.findViewById(34603171);
        measureView(this.mHeadView);
        this.mHeadContentHeight = this.mHeadView.getMeasuredHeight();
        setPadding(RELEASE_To_REFRESH, -1, RELEASE_To_REFRESH, RELEASE_To_REFRESH);
        this.mHeadView.setPadding(RELEASE_To_REFRESH, this.mHeadContentHeight * -1, RELEASE_To_REFRESH, RELEASE_To_REFRESH);
        addHeaderView(this.mHeadView);
        setOnScrollListener(this);
        this.mRotateAnimation = new RotateAnimation(0.0f, -180.0f, PULL_To_REFRESH, 0.5f, PULL_To_REFRESH, 0.5f);
        this.mRotateAnimation.setInterpolator(new LinearInterpolator());
        this.mRotateAnimation.setDuration(ANIMATION_DURATION_MILLIS);
        this.mRotateAnimation.setFillAfter(true);
        this.mReverseAnimation = new RotateAnimation(-180.0f, 0.0f, PULL_To_REFRESH, 0.5f, PULL_To_REFRESH, 0.5f);
        this.mReverseAnimation.setInterpolator(new LinearInterpolator());
        this.mReverseAnimation.setDuration(ANIMATION_DURATION_MILLIS);
        this.mReverseAnimation.setFillAfter(true);
        this.mPreferences = context.getSharedPreferences("pullDownListView_pref", RELEASE_To_REFRESH);
        refreshUpdatedAtValue();
        this.mHandler = new SelfRefreshCompleteHandler(this);
    }

    public void setUpdateTimePreferenceName(String preferenceName) {
        this.mPreferenceName = preferenceName;
        refreshUpdatedAtValue();
    }

    private void refreshUpdatedAtValue() {
        this.mLastUpdateTime = this.mPreferences.getLong(this.mPreferenceName, -1);
        if (this.mLastUpdateTime == -1) {
            this.mUpdateAtValue = getResources().getString(33685761);
        } else {
            this.mUpdateAtValue = HwDateUtils.formatChinaDateTime(getContext(), this.mLastUpdateTime, FORMATTER_FLAGS);
        }
        TextView textView = this.mLastUpdatedTextView;
        String string = getContext().getString(33685757);
        Object[] objArr = new Object[PULL_To_REFRESH];
        objArr[RELEASE_To_REFRESH] = this.mUpdateAtValue;
        textView.setText(String.format(string, objArr));
    }

    public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2, int arg3) {
        this.mFirstItemIndex = firstVisiableItem;
    }

    public void onScrollStateChanged(AbsListView arg0, int arg1) {
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mSelectDownEnable || this.mRefreshListener == null) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case RELEASE_To_REFRESH /*0*/:
                if (this.mFirstItemIndex == 0 && !this.mFlagIsRecored) {
                    this.mStartY = (int) event.getY();
                    this.mFlagIsRecored = true;
                    break;
                }
            case PULL_To_REFRESH /*1*/:
            case DONE /*3*/:
                if (this.mState == PULL_To_REFRESH) {
                    this.mState = DONE;
                    changeHeaderViewByState();
                } else if (this.mState == 0) {
                    this.mState = REFRESHING;
                    changeHeaderViewByState();
                    onRefresh();
                }
                this.mFlagIsRecored = false;
                this.mFlagIsBack = false;
                break;
            case REFRESHING /*2*/:
                int tempY = (int) event.getY();
                if (!this.mFlagIsRecored && this.mFirstItemIndex == 0) {
                    this.mFlagIsRecored = true;
                    this.mStartY = tempY;
                }
                if (this.mFlagIsRecored) {
                    if (this.mState != 0) {
                        if (this.mState != PULL_To_REFRESH) {
                            if (this.mState == DONE && tempY - this.mStartY > 0) {
                                this.mState = PULL_To_REFRESH;
                                changeHeaderViewByState();
                                break;
                            }
                        }
                        this.mHeadView.setPadding(RELEASE_To_REFRESH, ((int) (((float) (tempY - this.mStartY)) * TOUCH_SCROLL_SCALE)) - this.mHeadContentHeight, RELEASE_To_REFRESH, RELEASE_To_REFRESH);
                        if (this.mHeadView.getHeight() + this.mHeadView.getTop() < this.mHeadContentHeight) {
                            if (tempY - this.mStartY <= 0) {
                                this.mState = DONE;
                                changeHeaderViewByState();
                                break;
                            }
                        }
                        this.mState = RELEASE_To_REFRESH;
                        this.mFlagIsBack = true;
                        changeHeaderViewByState();
                        break;
                    }
                    this.mHeadView.setPadding(RELEASE_To_REFRESH, ((int) (((float) (tempY - this.mStartY)) * TOUCH_SCROLL_SCALE)) - this.mHeadContentHeight, RELEASE_To_REFRESH, RELEASE_To_REFRESH);
                    if (this.mHeadView.getHeight() + this.mHeadView.getTop() >= this.mHeadContentHeight || tempY - this.mStartY <= 0) {
                        if (tempY - this.mStartY <= 0) {
                            this.mState = DONE;
                            changeHeaderViewByState();
                            break;
                        }
                    }
                    this.mState = PULL_To_REFRESH;
                    changeHeaderViewByState();
                    break;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void changeHeaderViewByState() {
        switch (this.mState) {
            case RELEASE_To_REFRESH /*0*/:
                this.mArrowImageView.setVisibility(RELEASE_To_REFRESH);
                this.mProgressBar.setVisibility(8);
                this.mTipsTextview.setVisibility(RELEASE_To_REFRESH);
                this.mLastUpdatedTextView.setVisibility(RELEASE_To_REFRESH);
                this.mArrowImageView.clearAnimation();
                this.mArrowImageView.startAnimation(this.mRotateAnimation);
                this.mTipsTextview.setText(33685758);
            case PULL_To_REFRESH /*1*/:
                this.mProgressBar.setVisibility(8);
                this.mTipsTextview.setVisibility(RELEASE_To_REFRESH);
                refreshUpdatedAtValue();
                this.mLastUpdatedTextView.setVisibility(RELEASE_To_REFRESH);
                this.mArrowImageView.clearAnimation();
                this.mArrowImageView.setVisibility(RELEASE_To_REFRESH);
                if (this.mFlagIsBack) {
                    this.mFlagIsBack = false;
                    this.mArrowImageView.startAnimation(this.mReverseAnimation);
                    this.mTipsTextview.setText(33685759);
                    return;
                }
                this.mTipsTextview.setText(33685759);
            case REFRESHING /*2*/:
                this.mHeadView.setPadding(RELEASE_To_REFRESH, RELEASE_To_REFRESH, RELEASE_To_REFRESH, RELEASE_To_REFRESH);
                this.mProgressBar.setVisibility(RELEASE_To_REFRESH);
                this.mArrowImageView.clearAnimation();
                this.mArrowImageView.setVisibility(8);
                this.mTipsTextview.setText(33685760);
                this.mLastUpdatedTextView.setVisibility(RELEASE_To_REFRESH);
            case DONE /*3*/:
                this.mHeadView.setPadding(RELEASE_To_REFRESH, this.mHeadContentHeight * -1, RELEASE_To_REFRESH, RELEASE_To_REFRESH);
                this.mProgressBar.setVisibility(8);
                this.mArrowImageView.clearAnimation();
                this.mTipsTextview.setText(33685759);
                this.mLastUpdatedTextView.setVisibility(RELEASE_To_REFRESH);
            default:
        }
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
    }

    public void onRefreshComplete() {
        this.mState = DONE;
        changeHeaderViewByState();
        this.mHandler.removeMessages(PULL_To_REFRESH);
    }

    private void onRefresh() {
        this.mRefreshListener.onRefresh();
        this.mHandler.sendEmptyMessageDelayed(PULL_To_REFRESH, FRESH_TOLERANCE_MILLIS);
    }

    private void measureView(View child) {
        int childHeightSpec;
        LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new LayoutParams(-1, -2);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(RELEASE_To_REFRESH, RELEASE_To_REFRESH, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(RELEASE_To_REFRESH, RELEASE_To_REFRESH);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public void setSelectDownEnable(boolean SelectDownEnable) {
        this.mSelectDownEnable = SelectDownEnable;
    }

    public boolean isSelectDownEnable() {
        return this.mSelectDownEnable;
    }
}
