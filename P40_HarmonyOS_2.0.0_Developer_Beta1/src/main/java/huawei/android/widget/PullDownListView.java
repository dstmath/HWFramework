package huawei.android.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;
import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class PullDownListView extends ListView implements AbsListView.OnScrollListener {
    private static final long ANIMATION_DURATION_MILLIS = 250;
    private static final int DONE = 3;
    private static final int FORMATTER_FLAGS = 68117;
    private static final long FRESH_TOLERANCE_MILLIS = 15000;
    private static final int INVALID_VALUE = -1;
    private static final int MSG_SELF_REFRESHCOMPLETE = 1;
    private static final int PADDING_TOP_FACTOR = -1;
    private static final int PULL_TO_REFRESH = 1;
    private static final int REFRESHING = 2;
    private static final int RELEASE_TO_REFRESH = 0;
    private static final float REVERSE_ANIMATION_FROM_DEGREES = -180.0f;
    private static final float REVERSE_ANIMATION_PIVOT_X_VALUE = 0.5f;
    private static final float REVERSE_ANIMATION_PIVOT_Y_VALUE = 0.5f;
    private static final float REVERSE_ANIMATION_TO_DEGREES = 0.0f;
    private static final float ROTATE_ANIMATION_FROM_DEGREES = 0.0f;
    private static final float ROTATE_ANIMATION_PIVOT_X_VALUE = 0.5f;
    private static final float ROTATE_ANIMATION_PIVOT_Y_VALUE = 0.5f;
    private static final float ROTATE_ANIMATION_TO_DEGREES = -180.0f;
    private static final int STRING_BUILDER_CAPACITY = 50;
    private static final String TAG = "PullDownListView";
    private static final float TOUCH_SCROLL_SCALE = 0.55f;
    private ImageView mArrowImageView;
    private Context mContext;
    private int mFirstItemIndex;
    private SelfRefreshCompleteHandler mHandler;
    private int mHeadContentHeight;
    private RelativeLayout mHeadView;
    private boolean mIsFlagIsBack;
    private boolean mIsFlagIsRecored;
    private boolean mIsSelectDownEnable;
    private long mLastUpdateTime;
    private TextView mLastUpdatedTextView;
    private String mPreferenceName;
    private SharedPreferences mPreferences;
    private ProgressBar mProgressBar;
    public OnRefreshListener mRefreshListener;
    private RotateAnimation mReverseAnimation;
    private RotateAnimation mRotateAnimation;
    private int mStartY;
    private int mState;
    private TextView mTipsTextview;
    private String mUpdateAtValue;

    public interface OnRefreshListener {
        void onRefresh();
    }

    public PullDownListView(Context context) {
        this(context, null);
    }

    public PullDownListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public PullDownListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PullDownListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mState = 3;
        this.mIsSelectDownEnable = false;
        this.mPreferenceName = null;
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        View view = ResLoaderUtil.getLayout(this.mContext, "pulldown_listview_head", null, false);
        if (view instanceof RelativeLayout) {
            this.mHeadView = (RelativeLayout) view;
            this.mArrowImageView = (ImageView) this.mHeadView.findViewById(ResLoaderUtil.getViewId(this.mContext, "head_arrowImageView"));
            this.mProgressBar = (ProgressBar) this.mHeadView.findViewById(ResLoaderUtil.getViewId(this.mContext, "head_progressBar"));
            this.mTipsTextview = (TextView) this.mHeadView.findViewById(ResLoaderUtil.getViewId(this.mContext, "description"));
            this.mLastUpdatedTextView = (TextView) this.mHeadView.findViewById(ResLoaderUtil.getViewId(this.mContext, "head_lastUpdatedTextView"));
            measureView(this.mHeadView);
            this.mHeadContentHeight = this.mHeadView.getMeasuredHeight();
            setPadding(0, -1, 0, 0);
            this.mHeadView.setPadding(0, this.mHeadContentHeight * -1, 0, 0);
            addHeaderView(this.mHeadView);
            setOnScrollListener(this);
            this.mRotateAnimation = new RotateAnimation(0.0f, -180.0f, 1, 0.5f, 1, 0.5f);
            this.mRotateAnimation.setInterpolator(new LinearInterpolator());
            this.mRotateAnimation.setDuration(ANIMATION_DURATION_MILLIS);
            this.mRotateAnimation.setFillAfter(true);
            this.mReverseAnimation = new RotateAnimation(-180.0f, 0.0f, 1, 0.5f, 1, 0.5f);
            this.mReverseAnimation.setInterpolator(new LinearInterpolator());
            this.mReverseAnimation.setDuration(ANIMATION_DURATION_MILLIS);
            this.mReverseAnimation.setFillAfter(true);
            this.mPreferences = context.getSharedPreferences("pullDownListView_pref", 0);
            refreshUpdatedAtValue();
            this.mHandler = new SelfRefreshCompleteHandler(this);
        }
    }

    public void setUpdateTimePreferenceName(String preferenceName) {
        this.mPreferenceName = preferenceName;
        refreshUpdatedAtValue();
    }

    private void refreshUpdatedAtValue() {
        this.mLastUpdateTime = this.mPreferences.getLong(this.mPreferenceName, -1);
        long j = this.mLastUpdateTime;
        if (j == -1) {
            this.mUpdateAtValue = ResLoaderUtil.getString(this.mContext, "updated_time_not_found");
        } else {
            this.mUpdateAtValue = formatChinaDateTime(this.mContext, null, j, FORMATTER_FLAGS, null);
        }
        this.mLastUpdatedTextView.setText(String.format(ResLoaderUtil.getString(this.mContext, "lastUpdate_tip"), this.mUpdateAtValue));
    }

    private String formatChinaDateTime(Context context, Formatter formatter, long startMillis, int flags, String timeZone) {
        Formatter formatterTemp = formatter;
        if (formatter == null) {
            formatterTemp = new Formatter(new StringBuilder((int) STRING_BUILDER_CAPACITY), Locale.getDefault());
        }
        String date = DateUtils.formatDateRange(context, formatterTemp, startMillis, startMillis, flags, timeZone).toString();
        if (DateFormat.is24HourFormat(context)) {
            return date;
        }
        return formatChinaDateTime(context, date);
    }

    private String formatChinaDateTime(Context context, String normalTime) {
        Locale defaultLocale = Locale.getDefault();
        String[] normalTwelveTime = ResLoaderUtil.getStringArray(context, "normal_12_time");
        String[] chinaTime = ResLoaderUtil.getStringArray(context, "china_time");
        if (!(Locale.SIMPLIFIED_CHINESE.equals(defaultLocale) || Locale.forLanguageTag("zh-Hans-CN").equals(defaultLocale))) {
            return normalTime;
        }
        for (int i = 0; i < normalTwelveTime.length; i++) {
            if (normalTime.contains(normalTwelveTime[i])) {
                return normalTime.replace(normalTwelveTime[i], chinaTime[i]);
            }
        }
        return normalTime;
    }

    @Override // android.widget.AbsListView.OnScrollListener
    public void onScroll(AbsListView view, int firstVisiableItem, int visibleItemCount, int totalItemCount) {
        this.mFirstItemIndex = firstVisiableItem;
    }

    @Override // android.widget.AbsListView.OnScrollListener
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001a, code lost:
        if (r1 != 3) goto L_0x005a;
     */
    @Override // android.widget.AbsListView, android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        if (!this.mIsSelectDownEnable || this.mRefreshListener == null) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    int tempY = (int) event.getY();
                    if (!this.mIsFlagIsRecored && this.mFirstItemIndex == 0) {
                        this.mIsFlagIsRecored = true;
                        this.mStartY = tempY;
                    }
                    if (this.mIsFlagIsRecored) {
                        int i = this.mState;
                        if (i == 0) {
                            stateReleaseToRefresh(tempY);
                        } else if (i == 1) {
                            statePullToRefresh(tempY);
                        } else if (i == 3 && tempY - this.mStartY > 0) {
                            this.mState = 1;
                            changeHeaderViewByState();
                        }
                    }
                }
            }
            onTouchUp();
            this.mIsFlagIsRecored = false;
            this.mIsFlagIsBack = false;
        } else {
            actionDown(event);
        }
        return super.onTouchEvent(event);
    }

    private void actionDown(MotionEvent event) {
        if (this.mFirstItemIndex == 0 && !this.mIsFlagIsRecored) {
            this.mStartY = (int) event.getY();
            this.mIsFlagIsRecored = true;
        }
    }

    private void statePullToRefresh(int tempY) {
        this.mHeadView.setPadding(0, ((int) (((float) (tempY - this.mStartY)) * TOUCH_SCROLL_SCALE)) - this.mHeadContentHeight, 0, 0);
        if (this.mHeadView.getHeight() + this.mHeadView.getTop() >= this.mHeadContentHeight) {
            this.mState = 0;
            this.mIsFlagIsBack = true;
            changeHeaderViewByState();
        } else if (tempY - this.mStartY <= 0) {
            this.mState = 3;
            changeHeaderViewByState();
        }
    }

    private void stateReleaseToRefresh(int tempY) {
        this.mHeadView.setPadding(0, ((int) (((float) (tempY - this.mStartY)) * TOUCH_SCROLL_SCALE)) - this.mHeadContentHeight, 0, 0);
        if (this.mHeadView.getHeight() + this.mHeadView.getTop() < this.mHeadContentHeight && tempY - this.mStartY > 0) {
            this.mState = 1;
            changeHeaderViewByState();
        } else if (tempY - this.mStartY <= 0) {
            this.mState = 3;
            changeHeaderViewByState();
        }
    }

    private void changeHeaderViewByState() {
        int i = this.mState;
        if (i == 0) {
            this.mArrowImageView.setVisibility(0);
            this.mProgressBar.setVisibility(8);
            this.mTipsTextview.setVisibility(0);
            this.mLastUpdatedTextView.setVisibility(0);
            this.mArrowImageView.clearAnimation();
            this.mArrowImageView.startAnimation(this.mRotateAnimation);
            this.mTipsTextview.setText(ResLoaderUtil.getString(this.mContext, "app_list_header_release_to_refresh"));
        } else if (i == 1) {
            this.mProgressBar.setVisibility(8);
            this.mTipsTextview.setVisibility(0);
            refreshUpdatedAtValue();
            this.mLastUpdatedTextView.setVisibility(0);
            this.mArrowImageView.clearAnimation();
            this.mArrowImageView.setVisibility(0);
            if (this.mIsFlagIsBack) {
                this.mIsFlagIsBack = false;
                this.mArrowImageView.startAnimation(this.mReverseAnimation);
                this.mTipsTextview.setText(ResLoaderUtil.getString(this.mContext, "pull_to_refresh"));
                return;
            }
            this.mTipsTextview.setText(ResLoaderUtil.getString(this.mContext, "pull_to_refresh"));
        } else if (i == 2) {
            this.mHeadView.setPadding(0, 0, 0, 0);
            this.mProgressBar.setVisibility(0);
            this.mArrowImageView.clearAnimation();
            this.mArrowImageView.setVisibility(8);
            this.mTipsTextview.setText(ResLoaderUtil.getString(this.mContext, "refreshing"));
            this.mLastUpdatedTextView.setVisibility(0);
        } else if (i == 3) {
            this.mHeadView.setPadding(0, this.mHeadContentHeight * -1, 0, 0);
            this.mProgressBar.setVisibility(8);
            this.mArrowImageView.clearAnimation();
            this.mTipsTextview.setText(ResLoaderUtil.getString(this.mContext, "pull_to_refresh"));
            this.mLastUpdatedTextView.setVisibility(0);
        }
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
    }

    public void onRefreshComplete() {
        this.mState = 3;
        changeHeaderViewByState();
        this.mHandler.removeMessages(1);
    }

    private void onRefresh() {
        this.mRefreshListener.onRefresh();
        this.mHandler.sendEmptyMessageDelayed(1, FRESH_TOLERANCE_MILLIS);
    }

    private void onTouchUp() {
        int i = this.mState;
        if (i == 1) {
            this.mState = 3;
            changeHeaderViewByState();
        } else if (i == 0) {
            this.mState = 2;
            changeHeaderViewByState();
            onRefresh();
        }
    }

    /* access modifiers changed from: private */
    public static class SelfRefreshCompleteHandler extends Handler {
        private WeakReference<PullDownListView> mPullWeakReference;

        SelfRefreshCompleteHandler(PullDownListView pullDownListView) {
            this.mPullWeakReference = new WeakReference<>(pullDownListView);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            PullDownListView pullDownListView = null;
            WeakReference<PullDownListView> weakReference = this.mPullWeakReference;
            if (weakReference != null) {
                pullDownListView = weakReference.get();
            }
            if (pullDownListView != null && msg.what == 1) {
                pullDownListView.onRefreshComplete();
            }
        }
    }

    private void measureView(View child) {
        int childHeightSpec;
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(-1, -2);
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

    public void setSelectDownEnable(boolean isSelectDownEnable) {
        this.mIsSelectDownEnable = isSelectDownEnable;
    }

    public boolean isSelectDownEnable() {
        return this.mIsSelectDownEnable;
    }
}
