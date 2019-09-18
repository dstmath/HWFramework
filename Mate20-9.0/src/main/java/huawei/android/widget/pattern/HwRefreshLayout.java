package huawei.android.widget.pattern;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.ArrayList;
import java.util.List;

public class HwRefreshLayout extends ViewGroup {
    public static final int STATUS_COMPLETE = 5;
    public static final int STATUS_DRAGGING = 1;
    public static final int STATUS_FAIL = 6;
    public static final int STATUS_INIT = 0;
    public static final int STATUS_REFRESHING = 3;
    public static final int STATUS_RELEASE_CANCEL = 4;
    public static final int STATUS_RELEASE_PREPARE = 2;
    private int mActivePointerId;
    private int mAutoRefreshDuration;
    /* access modifiers changed from: private */
    public int mCompleteStickDuration;
    /* access modifiers changed from: private */
    public int mContentCurrentTop;
    /* access modifiers changed from: private */
    public View mContentView;
    private DefaultHeaderView mDefaultHeaderView;
    private float mDragRate;
    /* access modifiers changed from: private */
    public String mFailureMsg;
    /* access modifiers changed from: private */
    public int mHeaderCurrentTop;
    private int mHeaderLayoutIndex;
    /* access modifiers changed from: private */
    public int mHeaderOrginTop;
    /* access modifiers changed from: private */
    public View mHeaderView;
    private float mInitDownY;
    private float mInitMotionY;
    private boolean mIsBeingDragged;
    private boolean mIsInitMesure;
    /* access modifiers changed from: private */
    public boolean mIsRefreshing;
    /* access modifiers changed from: private */
    public boolean mIsSelfLayout;
    private int mMaxDragDistance;
    /* access modifiers changed from: private */
    public OnClickMessageListener mOnClickMessageListener;
    /* access modifiers changed from: private */
    public OnLastUpdateTimeShowListener mOnLastUpdateTimeShowListener;
    private List<OnRefreshListener> mOnRefreshListeners;
    private float mPosY;
    /* access modifiers changed from: private */
    public Progress mProgress;
    private int mRefreshDistance;
    /* access modifiers changed from: private */
    public String mRefreshingMsg;
    /* access modifiers changed from: private */
    public boolean mScollUpAction;
    /* access modifiers changed from: private */
    public int mStatus;
    /* access modifiers changed from: private */
    public String mSuccessMsg;
    private int mToRetainDuration;
    private int mToStartDuration;
    private float mTouchSlop;
    private UpdateHandler mUpdateHandler;
    private float ratioOfHeaderHeightToReach;
    private float ratioOfHeaderHeightToRefresh;

    class DefaultHeaderView extends RelativeLayout implements UpdateHandler {
        private final int BIG_WIDTH;
        /* access modifiers changed from: private */
        public final float INIT_INFO_TEXT_SIZE;
        private final int LOADING_TO_MSG_ANIM_DURATION;
        private final int PADDING_L;
        /* access modifiers changed from: private */
        public final int PADDING_M;
        private final int SMALL_WIDTH;
        private final float VIEW_ALPHA;
        private int mOrgBgColor;
        /* access modifiers changed from: private */
        public ProgressBar mProgressbar;
        private ValueAnimator mRootAnim;
        /* access modifiers changed from: private */
        public View mRootView;
        private boolean mRunPassAnimEnable;
        private int mTextColor;
        private long mTimeStamp;
        /* access modifiers changed from: private */
        public TextView mTvInfo;
        /* access modifiers changed from: private */
        public TextView mTvMsg;

        public DefaultHeaderView(HwRefreshLayout this$02, Context context) {
            this(this$02, context, null);
        }

        public DefaultHeaderView(HwRefreshLayout this$02, Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public DefaultHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            this.BIG_WIDTH = ResLoaderUtil.getDimensionPixelSize(getContext(), "hwpt_refresh_circle_width");
            this.SMALL_WIDTH = ResLoaderUtil.getDimensionPixelSize(getContext(), "hwpt_refresh_small_circle_width");
            this.PADDING_M = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_m");
            this.PADDING_L = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_l");
            this.LOADING_TO_MSG_ANIM_DURATION = 300;
            this.VIEW_ALPHA = 0.95f;
            this.mTimeStamp = 0;
            this.mRunPassAnimEnable = true;
            this.INIT_INFO_TEXT_SIZE = getResources().getDimension(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.DIMEN, "hwpt_refresh_default_info_text_size"));
            this.mRootView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_refresh_layout"), this, true);
            this.mProgressbar = (ProgressBar) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_progress_bar"));
            this.mTvMsg = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_tv_msg"));
            this.mTvInfo = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_tv_info"));
            this.mOrgBgColor = getContext().obtainStyledAttributes(new int[]{16842801}).getColor(0, 0);
            TypedArray typedArray = getContext().obtainStyledAttributes(new int[]{16842808});
            this.mTextColor = typedArray.getColor(0, 0);
            typedArray.recycle();
            initView();
        }

        public void setLastUpdateTimeStamp(long timeStamp) {
            this.mTimeStamp = timeStamp;
        }

        public void onProgressUpdate(HwRefreshLayout layout, Progress progress, int status) {
            switch (status) {
                case 0:
                    initView();
                    if (!(HwRefreshLayout.this.mOnLastUpdateTimeShowListener == null || this.mTimeStamp == 0)) {
                        HwRefreshLayout.this.reInitHeadMesure();
                        break;
                    }
                case 1:
                    changeProgressbarSize(progress);
                    break;
                case 2:
                    changeProgressbarSize(progress);
                    break;
                case 3:
                    changeProgressbarSize(progress);
                    if (isLayoutRtl()) {
                        this.mTvMsg.setPadding(0, 0, this.SMALL_WIDTH + this.PADDING_M, 0);
                    } else {
                        this.mTvMsg.setPadding(this.SMALL_WIDTH + this.PADDING_M, 0, 0, 0);
                    }
                    this.mTvMsg.setText(HwRefreshLayout.this.mRefreshingMsg);
                    this.mProgressbar.setVisibility(0);
                    break;
                case 5:
                    if (this.mRunPassAnimEnable) {
                        this.mRunPassAnimEnable = false;
                        this.mTvMsg.setTextColor(ResLoaderUtil.getColor(getContext(), "emui_white"));
                        this.mTvMsg.setPadding(0, 0, 0, 0);
                        this.mTvMsg.setText(HwRefreshLayout.this.mSuccessMsg);
                        this.mTimeStamp = System.currentTimeMillis();
                        this.mProgressbar.setVisibility(8);
                        this.mTvInfo.setVisibility(8);
                        this.mRootView.setAlpha(0.0f);
                        this.mRootView.setBackgroundColor(ResLoaderUtil.getColor(getContext(), "hwpt_refresh_success"));
                        this.mTvMsg.post(new Runnable() {
                            public void run() {
                                DefaultHeaderView.this.runPassAnim();
                            }
                        });
                        break;
                    } else {
                        return;
                    }
                case 6:
                    if (this.mRunPassAnimEnable) {
                        this.mRunPassAnimEnable = false;
                        this.mTvMsg.setTextColor(ResLoaderUtil.getColor(getContext(), "emui_white"));
                        this.mTvMsg.setPadding(0, 0, 0, 0);
                        this.mTvMsg.setText(HwRefreshLayout.this.mFailureMsg);
                        this.mProgressbar.setVisibility(8);
                        this.mTvInfo.setVisibility(8);
                        this.mRootView.setAlpha(0.0f);
                        this.mRootView.setBackgroundColor(ResLoaderUtil.getColor(getContext(), "hwpt_refresh_fail"));
                        this.mTvMsg.post(new Runnable() {
                            public void run() {
                                DefaultHeaderView.this.runPassAnim();
                            }
                        });
                        break;
                    } else {
                        return;
                    }
            }
        }

        /* access modifiers changed from: private */
        public void runPassAnim() {
            this.mRootView.setPadding(this.PADDING_M, 0, this.PADDING_M, 0);
            int time = 300;
            if (HwRefreshLayout.this.mCompleteStickDuration < 300) {
                time = HwRefreshLayout.this.mCompleteStickDuration / 2;
            }
            ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            anim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    DefaultHeaderView.this.mTvMsg.setAlpha(value);
                    DefaultHeaderView.this.setAlpha(1.0f - value);
                    DefaultHeaderView.this.mRootView.setAlpha(0.95f);
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    DefaultHeaderView.this.mProgressbar.setVisibility(8);
                    DefaultHeaderView.this.mTvInfo.setVisibility(8);
                }
            });
            anim.setDuration((long) time);
            anim.start();
            if (this.mRootAnim != null) {
                this.mRootAnim.end();
                this.mRootAnim = null;
            }
            this.mRootAnim = ValueAnimator.ofInt(new int[]{this.mRootView.getHeight(), this.mTvMsg.getHeight() + this.PADDING_M + this.PADDING_M});
            this.mRootAnim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            this.mRootAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = ((Integer) animation.getAnimatedValue()).intValue();
                    ViewGroup.LayoutParams lp = DefaultHeaderView.this.mRootView.getLayoutParams();
                    lp.height = value;
                    DefaultHeaderView.this.mRootView.setLayoutParams(lp);
                }
            });
            this.mRootAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ViewGroup.LayoutParams lp = DefaultHeaderView.this.mRootView.getLayoutParams();
                    lp.height = -2;
                    DefaultHeaderView.this.mRootView.setLayoutParams(lp);
                    DefaultHeaderView.this.mRootView.setPadding(DefaultHeaderView.this.PADDING_M, DefaultHeaderView.this.PADDING_M, DefaultHeaderView.this.PADDING_M, DefaultHeaderView.this.PADDING_M);
                }
            });
            this.mRootAnim.setDuration((long) time);
            this.mRootAnim.start();
        }

        public void dealScrollUpAction() {
            progressAnimAction();
            infoAnimAction();
            rootAnimAction();
        }

        private void rootAnimAction() {
            int to;
            this.mRootView.setPadding(this.PADDING_M, this.PADDING_M, this.PADDING_M, this.PADDING_M);
            int from = this.mRootView.getHeight();
            if (this.mTvMsg.getHeight() > this.SMALL_WIDTH) {
                to = this.mTvMsg.getHeight() + this.PADDING_M + this.PADDING_M;
            } else {
                to = this.SMALL_WIDTH + this.PADDING_M + this.PADDING_M;
            }
            this.mRootAnim = ValueAnimator.ofInt(new int[]{from, to});
            this.mRootAnim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            this.mRootAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = ((Integer) animation.getAnimatedValue()).intValue();
                    ViewGroup.LayoutParams lp = DefaultHeaderView.this.mRootView.getLayoutParams();
                    lp.height = value;
                    DefaultHeaderView.this.mRootView.setLayoutParams(lp);
                }
            });
            this.mRootAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ViewGroup.LayoutParams lp = DefaultHeaderView.this.mRootView.getLayoutParams();
                    lp.height = -2;
                    DefaultHeaderView.this.mRootView.setLayoutParams(lp);
                }
            });
            this.mRootAnim.setDuration(300);
            this.mRootAnim.start();
        }

        private void progressAnimAction() {
            int to;
            int from;
            if (isLayoutRtl()) {
                from = this.mProgressbar.getRight();
                to = this.mTvMsg.getRight();
            } else {
                from = this.mProgressbar.getLeft();
                to = this.mTvMsg.getLeft();
            }
            ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, (float) (to - from)});
            anim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    DefaultHeaderView.this.mProgressbar.setTranslationX(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            anim.setDuration(300);
            anim.start();
            ValueAnimator anim2 = ValueAnimator.ofInt(new int[]{this.mProgressbar.getWidth(), this.SMALL_WIDTH});
            anim2.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = ((Integer) animation.getAnimatedValue()).intValue();
                    ViewGroup.LayoutParams lp = DefaultHeaderView.this.mProgressbar.getLayoutParams();
                    lp.height = value;
                    lp.width = value;
                    DefaultHeaderView.this.mProgressbar.setLayoutParams(lp);
                }
            });
            anim2.setDuration(300);
            anim2.start();
        }

        private void infoAnimAction() {
            ValueAnimator infoAnim = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
            infoAnim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            infoAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    DefaultHeaderView.this.mTvInfo.setTextSize(0, DefaultHeaderView.this.INIT_INFO_TEXT_SIZE * value);
                    DefaultHeaderView.this.mTvInfo.setAlpha(value);
                }
            });
            infoAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    DefaultHeaderView.this.mTvInfo.setVisibility(8);
                    DefaultHeaderView.this.msgAnimAction();
                }
            });
            infoAnim.setDuration(150);
            infoAnim.start();
        }

        /* access modifiers changed from: private */
        public void msgAnimAction() {
            ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    DefaultHeaderView.this.mTvMsg.setAlpha(value);
                    DefaultHeaderView.this.mTvMsg.setScaleY(value);
                    DefaultHeaderView.this.mTvMsg.setScaleX(value);
                }
            });
            anim.setDuration(150);
            anim.start();
        }

        private void changeProgressbarSize(Progress progress) {
            float scale = (((float) progress.getCurrentY()) * 1.0f) / ((float) progress.getRefreshY());
            if (scale > 1.1f) {
                scale = 1.1f;
            }
            int width = (int) (((float) this.BIG_WIDTH) * scale);
            float alpha = scale;
            if (alpha > 1.0f) {
                alpha = 1.0f;
            }
            this.mTvInfo.setAlpha(alpha);
            this.mProgressbar.setAlpha(alpha);
            ViewGroup.LayoutParams lp = this.mProgressbar.getLayoutParams();
            lp.height = width;
            lp.width = width;
            this.mProgressbar.setLayoutParams(lp);
        }

        private void initView() {
            this.mRunPassAnimEnable = true;
            if (HwRefreshLayout.this.mOnLastUpdateTimeShowListener == null || this.mTimeStamp == 0) {
                this.mTvInfo.setVisibility(8);
            } else {
                this.mTvInfo.setVisibility(0);
                this.mTvInfo.setText(HwRefreshLayout.this.mOnLastUpdateTimeShowListener.getDescription(this.mTimeStamp));
                this.mTvInfo.setTextSize(0, this.INIT_INFO_TEXT_SIZE);
                this.mTvInfo.setAlpha(1.0f);
            }
            this.mTvMsg.setVisibility(0);
            this.mTvMsg.setText("");
            this.mTvMsg.setAlpha(0.0f);
            this.mTvMsg.setScaleY(1.0f);
            this.mTvMsg.setScaleX(1.0f);
            this.mTvMsg.setTextColor(this.mTextColor);
            this.mProgressbar.setVisibility(0);
            this.mProgressbar.setTranslationX(0.0f);
            ViewGroup.LayoutParams lp = this.mProgressbar.getLayoutParams();
            lp.height = this.BIG_WIDTH;
            lp.width = this.BIG_WIDTH;
            this.mProgressbar.setLayoutParams(lp);
            ViewGroup.LayoutParams lp2 = this.mRootView.getLayoutParams();
            if (lp2 == null) {
                lp2 = new ViewGroup.LayoutParams(-1, -2);
            }
            lp2.height = -2;
            this.mRootView.setPadding(this.PADDING_M, this.PADDING_L, this.PADDING_M, this.PADDING_L);
            this.mRootView.setLayoutParams(lp2);
            this.mRootView.setBackgroundColor(getResources().getColor(17170445));
            setBackgroundColor(this.mOrgBgColor);
            setAlpha(1.0f);
        }
    }

    public interface OnClickMessageListener {
        void onClick(View view, int i);
    }

    public interface OnLastUpdateTimeShowListener {
        String getDescription(long j);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    class Progress {
        private int currentY;
        private int refreshY;
        private int totalY;

        Progress() {
        }

        public int getRefreshY() {
            return this.refreshY;
        }

        public int getTotalY() {
            return this.totalY;
        }

        public int getCurrentY() {
            return this.currentY;
        }

        public void setTotalY(int totalY2) {
            this.totalY = totalY2;
        }

        public void setCurrentY(int currentY2) {
            this.currentY = currentY2;
        }

        public void setRefreshY(int refreshY2) {
            this.refreshY = refreshY2;
        }
    }

    interface UpdateHandler {
        void dealScrollUpAction();

        void onProgressUpdate(HwRefreshLayout hwRefreshLayout, Progress progress, int i);
    }

    public HwRefreshLayout(Context context) {
        this(context, null);
    }

    public HwRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMaxDragDistance = -1;
        this.mDragRate = 0.5f;
        this.mHeaderLayoutIndex = -1;
        this.mIsInitMesure = true;
        this.mIsSelfLayout = false;
        this.mScollUpAction = false;
        this.mActivePointerId = -1;
        this.mTouchSlop = (float) ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mProgress = new Progress();
        this.mToStartDuration = 200;
        this.mToRetainDuration = 200;
        this.mAutoRefreshDuration = 400;
        this.mCompleteStickDuration = 2000;
        this.ratioOfHeaderHeightToRefresh = 1.0f;
        this.ratioOfHeaderHeightToReach = 1.6f;
        this.mRefreshingMsg = ResLoaderUtil.getString(getContext(), "hwpt_refresh_frefreshing_msg");
        setDefaultHeaderView();
        setChildrenDrawingOrderEnabled(true);
    }

    /* access modifiers changed from: private */
    public Interpolator getLinearOutSlowInInterpolator() {
        return AnimationUtils.loadInterpolator(getContext(), 17563662);
    }

    private void setDefaultHeaderView() {
        this.mDefaultHeaderView = new DefaultHeaderView(this, getContext());
        setHeaderView(this.mDefaultHeaderView);
        this.mDefaultHeaderView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HwRefreshLayout.this.ensureContent();
                if (HwRefreshLayout.this.mOnClickMessageListener != null) {
                    HwRefreshLayout.this.mOnClickMessageListener.onClick(HwRefreshLayout.this.mContentView, HwRefreshLayout.this.mStatus);
                }
            }
        });
    }

    private void setHeaderView(View view) {
        if (view != null && view != this.mHeaderView) {
            this.mHeaderView = view;
            this.mIsInitMesure = true;
            addView(this.mHeaderView);
            if (view instanceof UpdateHandler) {
                setUpdateHandler((UpdateHandler) view);
            }
        }
    }

    private void setUpdateHandler(UpdateHandler updateHandler) {
        this.mUpdateHandler = updateHandler;
    }

    /* access modifiers changed from: private */
    public void ensureContent() {
        if (this.mContentView == null) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = getChildAt(i);
                if (childAt != this.mHeaderView) {
                    this.mContentView = childAt;
                    return;
                }
            }
        }
    }

    private void setMaxDragDistance(int distance) {
        this.mMaxDragDistance = distance;
        this.mProgress.setTotalY(distance);
    }

    private void setRefreshDistance(int distance) {
        this.mRefreshDistance = distance;
        this.mProgress.setRefreshY(this.mRefreshDistance);
    }

    /* access modifiers changed from: protected */
    public int getChildDrawingOrder(int childCount, int i) {
        if (this.mHeaderLayoutIndex < 0) {
            return i;
        }
        if (i == childCount - 1) {
            return this.mHeaderLayoutIndex;
        }
        if (i >= this.mHeaderLayoutIndex) {
            return i + 1;
        }
        return i;
    }

    /* access modifiers changed from: private */
    public void reInitHeadMesure() {
        this.mIsInitMesure = true;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureContent();
        if (this.mContentView != null) {
            this.mContentView.measure(View.MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), 1073741824), View.MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824));
        }
        if (this.mHeaderView != null) {
            measureChild(this.mHeaderView, widthMeasureSpec, heightMeasureSpec);
            if (this.mIsInitMesure) {
                int measuredHeight = this.mHeaderView.getMeasuredHeight();
                int i = -measuredHeight;
                this.mHeaderCurrentTop = i;
                this.mHeaderOrginTop = i;
                setMaxDragDistance((int) (((float) measuredHeight) * this.ratioOfHeaderHeightToReach));
                setRefreshDistance((int) (((float) measuredHeight) * this.ratioOfHeaderHeightToRefresh));
                this.mIsInitMesure = false;
            }
        }
        this.mHeaderLayoutIndex = -1;
        int childCount = getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            if (this.mHeaderView == getChildAt(i2)) {
                this.mHeaderLayoutIndex = i2;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mContentView == null) {
            ensureContent();
        }
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (this.mHeaderView != null) {
            this.mHeaderView.layout(paddingLeft, this.mHeaderCurrentTop, this.mHeaderView.getMeasuredWidth() + paddingLeft, this.mHeaderCurrentTop + this.mHeaderView.getMeasuredHeight());
        }
        if (this.mContentView != null) {
            int contentHeight = this.mContentView.getMeasuredHeight();
            int contentWidth = this.mContentView.getMeasuredWidth();
            int left = paddingLeft;
            int top = this.mHeaderView.getBottom();
            if (this.mIsSelfLayout) {
                top = this.mContentCurrentTop;
            }
            this.mContentView.layout(left, top, left + contentWidth, top + contentHeight);
        }
    }

    private void dealWithRefreshingScrollUpAction() {
        this.mIsSelfLayout = true;
        animContentOffsetToRetainPos();
        this.mUpdateHandler.dealScrollUpAction();
    }

    private boolean dealWithRefreshingScrollUp(MotionEvent ev) {
        switch (ev.getAction()) {
            case 0:
                this.mPosY = ev.getY();
                break;
            case 1:
            case 3:
                animHeaderToTop();
                break;
            case 2:
                float offset = this.mPosY - ev.getY();
                if (offset < this.mTouchSlop) {
                    if ((-offset) >= this.mTouchSlop) {
                        float dy = (-offset) * this.mDragRate;
                        if (dy > 0.0f) {
                            actionMoving(dy - ((float) this.mHeaderOrginTop));
                            break;
                        }
                    }
                } else {
                    this.mScollUpAction = false;
                    dealWithRefreshingScrollUpAction();
                    animHeaderToTop();
                    break;
                }
                break;
        }
        return true;
    }

    private void animHeaderToTop() {
        int from = this.mHeaderView.getTop();
        this.mHeaderCurrentTop = from;
        moveAnimation(from, getPaddingTop(), this.mToRetainDuration, null);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureContent();
        if (this.mIsRefreshing && this.mScollUpAction) {
            return dealWithRefreshingScrollUp(ev);
        }
        int action = ev.getActionMasked();
        if (!isEnabled() || canChildScrollUp() || this.mIsRefreshing || this.mIsSelfLayout) {
            return false;
        }
        if (action != 6) {
            switch (action) {
                case 0:
                    this.mIsBeingDragged = false;
                    notifyStatus(0);
                    this.mActivePointerId = ev.getPointerId(0);
                    int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (pointerIndex >= 0) {
                        this.mInitDownY = ev.getY(pointerIndex);
                        break;
                    } else {
                        return false;
                    }
                case 1:
                case 3:
                    this.mIsBeingDragged = false;
                    this.mActivePointerId = -1;
                    break;
                case 2:
                    int pointerIndex2 = ev.findPointerIndex(this.mActivePointerId);
                    if (pointerIndex2 >= 0) {
                        checkDragging(ev.getY(pointerIndex2));
                        break;
                    } else {
                        return false;
                    }
            }
        } else {
            checkOtherPointerUp(ev);
        }
        return this.mIsBeingDragged;
    }

    private void checkDragging(float y) {
        this.mInitMotionY = this.mInitDownY + this.mTouchSlop;
        if (y - this.mInitDownY > this.mTouchSlop && !this.mIsBeingDragged) {
            this.mIsBeingDragged = true;
        }
    }

    private void checkOtherPointerUp(MotionEvent ev) {
        int pointIndex = ev.getActionIndex();
        if (ev.getPointerId(pointIndex) == this.mActivePointerId) {
            this.mActivePointerId = ev.getPointerId(pointIndex == 0 ? 1 : 0);
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mIsRefreshing && this.mScollUpAction) {
            return dealWithRefreshingScrollUp(ev);
        }
        if (!isEnabled() || canChildScrollUp() || this.mIsRefreshing || this.mIsSelfLayout) {
            return false;
        }
        switch (ev.getActionMasked()) {
            case 0:
                this.mIsBeingDragged = false;
                notifyStatus(0);
                this.mActivePointerId = ev.getPointerId(0);
                int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex >= 0) {
                    this.mInitDownY = ev.getY(pointerIndex);
                    break;
                } else {
                    return false;
                }
            case 1:
                int pointerIndex2 = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex2 >= 0) {
                    float upY = ev.getY(pointerIndex2);
                    if (this.mIsBeingDragged) {
                        actionUp((upY - this.mInitMotionY) * this.mDragRate);
                        this.mIsBeingDragged = false;
                    }
                    this.mActivePointerId = -1;
                    break;
                } else {
                    return false;
                }
            case 2:
                int pointerIndex3 = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex3 >= 0) {
                    float evY = ev.getY(pointerIndex3);
                    checkDragging(evY);
                    if (this.mIsBeingDragged) {
                        float dy = (evY - this.mInitMotionY) * this.mDragRate;
                        if (dy > 0.0f) {
                            actionMoving(dy);
                        }
                        notifyStatus(1);
                        break;
                    }
                } else {
                    return false;
                }
                break;
            case 3:
                return false;
            case 5:
                int pointerIndex4 = ev.getActionIndex();
                if (pointerIndex4 >= 0) {
                    this.mActivePointerId = ev.getPointerId(pointerIndex4);
                    break;
                } else {
                    return false;
                }
            case 6:
                checkOtherPointerUp(ev);
                break;
        }
        return true;
    }

    private void actionUp(float dy) {
        if (dy < ((float) this.mRefreshDistance)) {
            animOffsetToStartPos();
            this.mIsRefreshing = false;
            notifyStatus(4);
            return;
        }
        animOffsetToRetainPos();
        this.mIsRefreshing = true;
        notifyStatus(2);
    }

    private void moveAnimation(int star, int end, int duration, Animator.AnimatorListener animatorListener) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(new int[]{star, end});
        valueAnimator.setInterpolator(getLinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                HwRefreshLayout.this.moveTo(((Integer) animation.getAnimatedValue()).intValue());
            }
        });
        if (animatorListener != null) {
            valueAnimator.addListener(animatorListener);
        }
        valueAnimator.setDuration((long) duration);
        valueAnimator.start();
    }

    /* access modifiers changed from: private */
    public void animOffsetToRetainPos() {
        int from = this.mHeaderView.getTop();
        this.mHeaderCurrentTop = from;
        moveAnimation(from, getPaddingTop(), this.mToRetainDuration, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                HwRefreshLayout.this.notifyRefreshListeners();
                HwRefreshLayout.this.notifyStatus(3);
                boolean unused = HwRefreshLayout.this.mScollUpAction = true;
            }
        });
    }

    /* access modifiers changed from: private */
    public void animOffsetToStartPos() {
        int from = this.mHeaderView.getTop();
        this.mHeaderCurrentTop = from;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(new int[]{from, this.mHeaderOrginTop});
        valueAnimator.setInterpolator(getLinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                HwRefreshLayout.this.mHeaderView.offsetTopAndBottom(((Integer) animation.getAnimatedValue()).intValue() - HwRefreshLayout.this.mHeaderCurrentTop);
                int unused = HwRefreshLayout.this.mHeaderCurrentTop = HwRefreshLayout.this.mHeaderView.getTop();
                HwRefreshLayout.this.mProgress.setCurrentY(HwRefreshLayout.this.mHeaderCurrentTop - HwRefreshLayout.this.mHeaderOrginTop);
                HwRefreshLayout.this.notifyProgress();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                boolean unused = HwRefreshLayout.this.mIsRefreshing = false;
                boolean unused2 = HwRefreshLayout.this.mIsSelfLayout = false;
                HwRefreshLayout.this.notifyStatus(0);
            }
        });
        valueAnimator.setDuration((long) this.mToStartDuration);
        valueAnimator.start();
    }

    private void animContentOffsetToRetainPos() {
        int from = this.mContentView.getTop();
        this.mContentCurrentTop = from;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(new int[]{from, getPaddingTop()});
        valueAnimator.setInterpolator(getLinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                HwRefreshLayout.this.mContentView.offsetTopAndBottom(((Integer) animation.getAnimatedValue()).intValue() - HwRefreshLayout.this.mContentCurrentTop);
                int unused = HwRefreshLayout.this.mContentCurrentTop = HwRefreshLayout.this.mContentView.getTop();
            }
        });
        valueAnimator.setDuration((long) this.mToRetainDuration);
        valueAnimator.start();
    }

    private void animHeaderOffsetToRetainPos() {
        int from = this.mHeaderView.getTop();
        this.mHeaderCurrentTop = from;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(new int[]{from, getPaddingTop()});
        valueAnimator.setInterpolator(getLinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                HwRefreshLayout.this.mHeaderView.offsetTopAndBottom(((Integer) animation.getAnimatedValue()).intValue() - HwRefreshLayout.this.mHeaderCurrentTop);
                int unused = HwRefreshLayout.this.mHeaderCurrentTop = HwRefreshLayout.this.mHeaderView.getTop();
            }
        });
        valueAnimator.setDuration((long) this.mToRetainDuration);
        valueAnimator.start();
    }

    private void animOffsetAutoRefresh() {
        this.mHeaderCurrentTop = this.mHeaderView.getTop();
        moveAnimation(this.mHeaderCurrentTop, this.mRefreshDistance + this.mHeaderOrginTop, this.mAutoRefreshDuration, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                HwRefreshLayout.this.notifyStatus(2);
                HwRefreshLayout.this.animOffsetToRetainPos();
            }

            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                HwRefreshLayout.this.notifyStatus(1);
            }
        });
    }

    private void actionMoving(float y) {
        float y2 = y > ((float) this.mMaxDragDistance) ? (float) this.mMaxDragDistance : y;
        if (y2 <= ((float) this.mMaxDragDistance)) {
            moveTo((int) (((float) this.mHeaderOrginTop) + y2));
        }
    }

    /* access modifiers changed from: private */
    public void notifyProgress() {
        if (this.mUpdateHandler != null) {
            this.mUpdateHandler.onProgressUpdate(this, this.mProgress, this.mStatus);
        }
    }

    /* access modifiers changed from: private */
    public void notifyStatus(int status) {
        this.mStatus = status;
        notifyProgress();
    }

    /* access modifiers changed from: private */
    public void notifyRefreshListeners() {
        if (this.mOnRefreshListeners != null && !this.mOnRefreshListeners.isEmpty()) {
            for (OnRefreshListener onRefreshListener : this.mOnRefreshListeners) {
                onRefreshListener.onRefresh();
            }
        }
    }

    private boolean canChildScrollUp() {
        if (Build.VERSION.SDK_INT >= 14) {
            return this.mContentView.canScrollVertically(-1);
        }
        boolean z = true;
        if (this.mContentView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) this.mContentView;
            if (absListView.getChildCount() <= 0 || (absListView.getFirstVisiblePosition() <= 0 && absListView.getChildAt(0).getTop() >= absListView.getPaddingTop())) {
                z = false;
            }
            return z;
        }
        if (!this.mContentView.canScrollVertically(-1) && this.mContentView.getScrollY() <= 0) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void moveTo(int y) {
        int dy = y - this.mHeaderCurrentTop;
        this.mHeaderView.offsetTopAndBottom(dy);
        this.mContentView.offsetTopAndBottom(dy);
        this.mHeaderCurrentTop = this.mHeaderView.getTop();
        this.mProgress.setCurrentY(this.mHeaderCurrentTop - this.mHeaderOrginTop);
        notifyProgress();
    }

    public void refreshComplete() {
        if (this.mIsRefreshing) {
            notifyStatus(5);
            this.mScollUpAction = false;
            this.mIsSelfLayout = true;
            postDelayed(new Runnable() {
                public void run() {
                    HwRefreshLayout.this.animOffsetToStartPos();
                }
            }, (long) this.mCompleteStickDuration);
            animContentOffsetToRetainPos();
            animHeaderOffsetToRetainPos();
        }
    }

    public void refreshFailure() {
        if (this.mIsRefreshing) {
            notifyStatus(6);
            this.mScollUpAction = false;
            this.mIsSelfLayout = true;
            postDelayed(new Runnable() {
                public void run() {
                    HwRefreshLayout.this.animOffsetToStartPos();
                }
            }, (long) this.mCompleteStickDuration);
            animContentOffsetToRetainPos();
            animHeaderOffsetToRetainPos();
        }
    }

    public void autoRefresh() {
        if (!this.mIsRefreshing) {
            this.mIsRefreshing = true;
            animOffsetAutoRefresh();
        }
    }

    public void setSuccessMsg(String mSuccessMsg2) {
        this.mSuccessMsg = mSuccessMsg2;
    }

    public void setFailureMsg(String mFailureMsg2) {
        this.mFailureMsg = mFailureMsg2;
    }

    public void setRefreshingMsg(String mRefreshingMsg2) {
        this.mRefreshingMsg = mRefreshingMsg2;
    }

    public void setOnClickMessageListener(OnClickMessageListener mOnClickMessageListener2) {
        this.mOnClickMessageListener = mOnClickMessageListener2;
    }

    public void addOnRefreshListener(OnRefreshListener onRefreshListener) {
        if (this.mOnRefreshListeners == null) {
            this.mOnRefreshListeners = new ArrayList();
        }
        this.mOnRefreshListeners.add(onRefreshListener);
    }

    public void setOnLastUpdateTimeShowListener(OnLastUpdateTimeShowListener onLastUpdateTimeShowListener) {
        this.mOnLastUpdateTimeShowListener = onLastUpdateTimeShowListener;
    }

    public void setLastUpdateTimeStamp(long timeStamp) {
        if (this.mDefaultHeaderView != null) {
            this.mDefaultHeaderView.setLastUpdateTimeStamp(timeStamp);
        }
    }
}
