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
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.ArrayList;
import java.util.List;

public class HwRefreshLayout extends ViewGroup {
    private static final int AUTO_REFRESH_DURATION = 400;
    private static final int COMPLETE_STICK_DURATION = 2000;
    private static final float DRAG_RATE = 0.5f;
    private static final int INITIAL_CAPACITY_SIZE = 10;
    private static final float RATIO_OF_HEADER_HEIGHT_TO_REACH = 1.6f;
    private static final float RATIO_OF_HEADER_HEIGHT_TO_REFRESH = 1.0f;
    public static final int STATUS_COMPLETE = 5;
    public static final int STATUS_DRAGGING = 1;
    public static final int STATUS_FAIL = 6;
    public static final int STATUS_INIT = 0;
    public static final int STATUS_REFRESHING = 3;
    public static final int STATUS_RELEASE_CANCEL = 4;
    public static final int STATUS_RELEASE_PREPARE = 2;
    private static final int TO_RETAIN_DURATION = 200;
    private static final int TO_START_DURATION = 200;
    private int mActivePointerId;
    private int mContentCurrentTop;
    private View mContentView;
    private DefaultHeaderView mDefaultHeaderView;
    private String mFailureMsg;
    private int mHeaderCurrentTop;
    private int mHeaderLayoutIndex;
    private int mHeaderOrginTop;
    private View mHeaderView;
    private float mInitDownY;
    private float mInitMotionY;
    private boolean mIsBeingDragged;
    private boolean mIsInitMesure;
    private boolean mIsRefreshing;
    private boolean mIsScollUpAction;
    private boolean mIsSelfLayout;
    private int mMaxDragDistance;
    private OnClickMessageListener mOnClickMessageListener;
    private OnLastUpdateTimeShowListener mOnLastUpdateTimeShowListener;
    private List<OnRefreshListener> mOnRefreshListeners;
    private float mPosY;
    private Progress mProgress;
    private int mRefreshDistance;
    private String mRefreshingMsg;
    private int mStatus;
    private String mSuccessMsg;
    private float mTouchSlop;
    private UpdateHandler mUpdateHandler;

    public interface OnClickMessageListener {
        void onClick(View view, int i);
    }

    public interface OnLastUpdateTimeShowListener {
        String getDescription(long j);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    /* access modifiers changed from: package-private */
    public interface UpdateHandler {
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
        this.mHeaderLayoutIndex = -1;
        this.mIsInitMesure = true;
        this.mIsSelfLayout = false;
        this.mIsScollUpAction = false;
        this.mActivePointerId = -1;
        this.mTouchSlop = (float) ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mProgress = new Progress();
        this.mRefreshingMsg = ResLoaderUtil.getString(getContext(), "hwpt_refresh_frefreshing_msg");
        setDefaultHeaderView();
        setChildrenDrawingOrderEnabled(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Interpolator getLinearOutSlowInInterpolator() {
        return AnimationUtils.loadInterpolator(getContext(), 17563662);
    }

    private void setDefaultHeaderView() {
        this.mDefaultHeaderView = new DefaultHeaderView(this, getContext());
        setHeaderView(this.mDefaultHeaderView);
        this.mDefaultHeaderView.setOnClickListener(new View.OnClickListener() {
            /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass1 */

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
    /* access modifiers changed from: public */
    private void ensureContent() {
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
    public int getChildDrawingOrder(int childCount, int index) {
        int i = this.mHeaderLayoutIndex;
        if (i < 0) {
            return index;
        }
        if (index == childCount - 1) {
            return i;
        }
        if (index >= i) {
            return index + 1;
        }
        return index;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reInitHeadMesure() {
        this.mIsInitMesure = true;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureContent();
        if (this.mContentView != null) {
            this.mContentView.measure(View.MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), 1073741824), View.MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824));
        }
        View view = this.mHeaderView;
        if (view != null) {
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            if (this.mIsInitMesure) {
                int measuredHeight = this.mHeaderView.getMeasuredHeight();
                this.mHeaderCurrentTop = -measuredHeight;
                this.mHeaderOrginTop = -measuredHeight;
                setMaxDragDistance((int) (((float) measuredHeight) * RATIO_OF_HEADER_HEIGHT_TO_REACH));
                setRefreshDistance((int) (((float) measuredHeight) * 1.0f));
                this.mIsInitMesure = false;
            }
        }
        this.mHeaderLayoutIndex = -1;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (this.mHeaderView == getChildAt(i)) {
                this.mHeaderLayoutIndex = i;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mContentView == null) {
            ensureContent();
        }
        int paddingLeft = getPaddingLeft();
        getPaddingTop();
        View view = this.mHeaderView;
        if (view != null) {
            view.layout(paddingLeft, this.mHeaderCurrentTop, view.getMeasuredWidth() + paddingLeft, this.mHeaderCurrentTop + this.mHeaderView.getMeasuredHeight());
        }
        View view2 = this.mContentView;
        if (view2 != null) {
            int contentHeight = view2.getMeasuredHeight();
            int contentWidth = this.mContentView.getMeasuredWidth();
            int top = this.mHeaderView.getBottom();
            if (this.mIsSelfLayout) {
                top = this.mContentCurrentTop;
            }
            this.mContentView.layout(paddingLeft, top, paddingLeft + contentWidth, top + contentHeight);
        }
    }

    private void dealWithRefreshingScrollUpAction() {
        this.mIsSelfLayout = true;
        animContentOffsetToRetainPos();
        this.mUpdateHandler.dealScrollUpAction();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000d, code lost:
        if (r0 != 3) goto L_0x0049;
     */
    private boolean dealWithRefreshingScrollUp(MotionEvent event) {
        int action = event.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    float offset = this.mPosY - event.getY();
                    float f = this.mTouchSlop;
                    if (offset >= f) {
                        this.mIsScollUpAction = false;
                        dealWithRefreshingScrollUpAction();
                        animHeaderToTop();
                    } else if ((-offset) >= f) {
                        float offsetY = (-offset) * 0.5f;
                        if (offsetY > 0.0f) {
                            actionMoving(offsetY - ((float) this.mHeaderOrginTop));
                        }
                    }
                }
            }
            animHeaderToTop();
        } else {
            this.mPosY = event.getY();
        }
        return true;
    }

    private void animHeaderToTop() {
        int from = this.mHeaderView.getTop();
        this.mHeaderCurrentTop = from;
        moveAnimation(from, getPaddingTop(), 200, null);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        ensureContent();
        if (this.mIsRefreshing && this.mIsScollUpAction) {
            return dealWithRefreshingScrollUp(event);
        }
        int action = event.getActionMasked();
        if (!isEnabled() || canChildScrollUp() || this.mIsRefreshing || this.mIsSelfLayout) {
            return false;
        }
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    int pointerIndex = event.findPointerIndex(this.mActivePointerId);
                    if (pointerIndex < 0) {
                        return false;
                    }
                    checkDragging(event.getY(pointerIndex));
                } else if (action != 3) {
                    if (action == 6) {
                        checkOtherPointerUp(event);
                    }
                }
            }
            this.mIsBeingDragged = false;
            this.mActivePointerId = -1;
        } else {
            this.mIsBeingDragged = false;
            notifyStatus(0);
            this.mActivePointerId = event.getPointerId(0);
            int pointerIndex2 = event.findPointerIndex(this.mActivePointerId);
            if (pointerIndex2 < 0) {
                return false;
            }
            this.mInitDownY = event.getY(pointerIndex2);
        }
        return this.mIsBeingDragged;
    }

    private void checkDragging(float eventY) {
        float f = this.mInitDownY;
        float distanceY = eventY - f;
        float f2 = this.mTouchSlop;
        this.mInitMotionY = f + f2;
        if (distanceY > f2 && !this.mIsBeingDragged) {
            this.mIsBeingDragged = true;
        }
    }

    private void checkOtherPointerUp(MotionEvent event) {
        int pointIndex = event.getActionIndex();
        if (event.getPointerId(pointIndex) == this.mActivePointerId) {
            this.mActivePointerId = event.getPointerId(pointIndex == 0 ? 1 : 0);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mIsRefreshing && this.mIsScollUpAction) {
            return dealWithRefreshingScrollUp(event);
        }
        if (!isEnabled() || canChildScrollUp() || this.mIsRefreshing || this.mIsSelfLayout) {
            return false;
        }
        int action = event.getActionMasked();
        if (action == 0) {
            this.mIsBeingDragged = false;
            notifyStatus(0);
            this.mActivePointerId = event.getPointerId(0);
            int pointerIndex = event.findPointerIndex(this.mActivePointerId);
            if (pointerIndex < 0) {
                return false;
            }
            this.mInitDownY = event.getY(pointerIndex);
        } else if (action != 1) {
            if (action != 2) {
                if (action == 3) {
                    return false;
                }
                if (action == 5) {
                    int pointerIndex2 = event.getActionIndex();
                    if (pointerIndex2 < 0) {
                        return false;
                    }
                    this.mActivePointerId = event.getPointerId(pointerIndex2);
                } else if (action == 6) {
                    checkOtherPointerUp(event);
                }
            } else if (onTouchMove(event)) {
                return false;
            }
        } else if (onTouchUp(event)) {
            return false;
        }
        return true;
    }

    private boolean onTouchMove(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(this.mActivePointerId);
        if (pointerIndex < 0) {
            return true;
        }
        float eventY = event.getY(pointerIndex);
        checkDragging(eventY);
        if (!this.mIsBeingDragged) {
            return false;
        }
        float distanceY = (eventY - this.mInitMotionY) * 0.5f;
        if (distanceY > 0.0f) {
            actionMoving(distanceY);
        }
        notifyStatus(1);
        return false;
    }

    private boolean onTouchUp(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(this.mActivePointerId);
        if (pointerIndex < 0) {
            return true;
        }
        float upY = event.getY(pointerIndex);
        if (this.mIsBeingDragged) {
            actionUp((upY - this.mInitMotionY) * 0.5f);
            this.mIsBeingDragged = false;
        }
        this.mActivePointerId = -1;
        return false;
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
        ValueAnimator valueAnimator = ValueAnimator.ofInt(star, end);
        valueAnimator.setInterpolator(getLinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass2 */

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
    /* access modifiers changed from: public */
    private void animOffsetToRetainPos() {
        int from = this.mHeaderView.getTop();
        this.mHeaderCurrentTop = from;
        moveAnimation(from, getPaddingTop(), 200, new AnimatorListenerAdapter() {
            /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass3 */

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                HwRefreshLayout.this.notifyRefreshListeners();
                HwRefreshLayout.this.notifyStatus(3);
                HwRefreshLayout.this.mIsScollUpAction = true;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animOffsetToStartPos() {
        int from = this.mHeaderView.getTop();
        this.mHeaderCurrentTop = from;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, this.mHeaderOrginTop);
        valueAnimator.setInterpolator(getLinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass4 */

            public void onAnimationUpdate(ValueAnimator animation) {
                HwRefreshLayout.this.mHeaderView.offsetTopAndBottom(((Integer) animation.getAnimatedValue()).intValue() - HwRefreshLayout.this.mHeaderCurrentTop);
                HwRefreshLayout hwRefreshLayout = HwRefreshLayout.this;
                hwRefreshLayout.mHeaderCurrentTop = hwRefreshLayout.mHeaderView.getTop();
                HwRefreshLayout.this.mProgress.setCurrentY(HwRefreshLayout.this.mHeaderCurrentTop - HwRefreshLayout.this.mHeaderOrginTop);
                HwRefreshLayout.this.notifyProgress();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass5 */

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                HwRefreshLayout.this.mIsRefreshing = false;
                HwRefreshLayout.this.mIsSelfLayout = false;
                HwRefreshLayout.this.notifyStatus(0);
            }
        });
        valueAnimator.setDuration(200L);
        valueAnimator.start();
    }

    private void animContentOffsetToRetainPos() {
        int from = this.mContentView.getTop();
        this.mContentCurrentTop = from;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, getPaddingTop());
        valueAnimator.setInterpolator(getLinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass6 */

            public void onAnimationUpdate(ValueAnimator animation) {
                HwRefreshLayout.this.mContentView.offsetTopAndBottom(((Integer) animation.getAnimatedValue()).intValue() - HwRefreshLayout.this.mContentCurrentTop);
                HwRefreshLayout hwRefreshLayout = HwRefreshLayout.this;
                hwRefreshLayout.mContentCurrentTop = hwRefreshLayout.mContentView.getTop();
            }
        });
        valueAnimator.setDuration(200L);
        valueAnimator.start();
    }

    private void animHeaderOffsetToRetainPos() {
        int from = this.mHeaderView.getTop();
        this.mHeaderCurrentTop = from;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, getPaddingTop());
        valueAnimator.setInterpolator(getLinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass7 */

            public void onAnimationUpdate(ValueAnimator animation) {
                HwRefreshLayout.this.mHeaderView.offsetTopAndBottom(((Integer) animation.getAnimatedValue()).intValue() - HwRefreshLayout.this.mHeaderCurrentTop);
                HwRefreshLayout hwRefreshLayout = HwRefreshLayout.this;
                hwRefreshLayout.mHeaderCurrentTop = hwRefreshLayout.mHeaderView.getTop();
            }
        });
        valueAnimator.setDuration(200L);
        valueAnimator.start();
    }

    private void animOffsetAutoRefresh() {
        this.mHeaderCurrentTop = this.mHeaderView.getTop();
        moveAnimation(this.mHeaderCurrentTop, this.mRefreshDistance + this.mHeaderOrginTop, AUTO_REFRESH_DURATION, new AnimatorListenerAdapter() {
            /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass8 */

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

    private void actionMoving(float offsetY) {
        float f = (float) this.mHeaderOrginTop;
        int i = this.mMaxDragDistance;
        moveTo((int) (f + (((float) i) < offsetY ? (float) i : offsetY)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyProgress() {
        UpdateHandler updateHandler = this.mUpdateHandler;
        if (updateHandler != null) {
            updateHandler.onProgressUpdate(this, this.mProgress, this.mStatus);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyStatus(int status) {
        this.mStatus = status;
        notifyProgress();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyRefreshListeners() {
        List<OnRefreshListener> list = this.mOnRefreshListeners;
        if (!(list == null || list.isEmpty())) {
            for (OnRefreshListener onRefreshListener : this.mOnRefreshListeners) {
                onRefreshListener.onRefresh();
            }
        }
    }

    private boolean canChildScrollUp() {
        if (Build.VERSION.SDK_INT >= 14) {
            return this.mContentView.canScrollVertically(-1);
        }
        View view = this.mContentView;
        if (!(view instanceof AbsListView)) {
            return view.canScrollVertically(-1) || this.mContentView.getScrollY() > 0;
        }
        AbsListView absListView = (AbsListView) view;
        return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void moveTo(int y) {
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
            this.mIsScollUpAction = false;
            this.mIsSelfLayout = true;
            postDelayed(new Runnable() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass9 */

                public void run() {
                    HwRefreshLayout.this.animOffsetToStartPos();
                }
            }, 2000);
            animContentOffsetToRetainPos();
            animHeaderOffsetToRetainPos();
        }
    }

    public void refreshFailure() {
        if (this.mIsRefreshing) {
            notifyStatus(6);
            this.mIsScollUpAction = false;
            this.mIsSelfLayout = true;
            postDelayed(new Runnable() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.AnonymousClass10 */

                public void run() {
                    HwRefreshLayout.this.animOffsetToStartPos();
                }
            }, 2000);
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

    public void setSuccessMsg(String successMsg) {
        this.mSuccessMsg = successMsg;
    }

    public void setFailureMsg(String failureMsg) {
        this.mFailureMsg = failureMsg;
    }

    public void setRefreshingMsg(String refreshingMsg) {
        this.mRefreshingMsg = refreshingMsg;
    }

    public void setOnClickMessageListener(OnClickMessageListener onClickMessageListener) {
        this.mOnClickMessageListener = onClickMessageListener;
    }

    public void addOnRefreshListener(OnRefreshListener onRefreshListener) {
        if (this.mOnRefreshListeners == null) {
            this.mOnRefreshListeners = new ArrayList((int) INITIAL_CAPACITY_SIZE);
        }
        this.mOnRefreshListeners.add(onRefreshListener);
    }

    public void setOnLastUpdateTimeShowListern(OnLastUpdateTimeShowListener onLastUpdateTimeShowListener) {
        this.mOnLastUpdateTimeShowListener = onLastUpdateTimeShowListener;
    }

    public void setLastUpdateTimeStamp(long timeStamp) {
        DefaultHeaderView defaultHeaderView = this.mDefaultHeaderView;
        if (defaultHeaderView != null) {
            defaultHeaderView.setLastUpdateTimeStamp(timeStamp);
        }
    }

    /* access modifiers changed from: package-private */
    public class DefaultHeaderView extends RelativeLayout implements UpdateHandler {
        private static final int DICHOTOMY_DURATION = 2;
        private static final int LOADING_TO_MSG_ANIM_DURATION = 300;
        private static final float MAX_SCALE_FACTOR = 1.1f;
        private static final float VIEW_ALPHA = 0.95f;
        private final float mInfoTextSize;
        private boolean mIsRunPassAnimEnable;
        private final int mMaxWidth;
        private final int mMinWidth;
        private int mOrgBgColor;
        private final int mPaddingL;
        private final int mPaddingM;
        private ProgressBar mProgressbar;
        private ValueAnimator mRootAnim;
        private View mRootView;
        private int mTextColor;
        private TextView mTextViewInfo;
        private TextView mTextViewMsg;
        private long mTimeStamp;

        DefaultHeaderView(HwRefreshLayout this$02, Context context) {
            this(this$02, context, null);
        }

        DefaultHeaderView(HwRefreshLayout this$02, Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        DefaultHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            this.mTimeStamp = 0;
            this.mIsRunPassAnimEnable = true;
            this.mMaxWidth = ResLoaderUtil.getDimensionPixelSize(getContext(), "hwpt_refresh_circle_width");
            this.mMinWidth = ResLoaderUtil.getDimensionPixelSize(getContext(), "hwpt_refresh_small_circle_width");
            this.mPaddingM = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_m");
            this.mPaddingL = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_l");
            this.mInfoTextSize = getResources().getDimension(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.DIMEN, "hwpt_refresh_default_info_text_size"));
            this.mRootView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_refresh_layout"), (ViewGroup) this, true);
            this.mProgressbar = (ProgressBar) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_progress_bar"));
            this.mTextViewMsg = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_tv_msg"));
            this.mTextViewInfo = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_tv_info"));
            TypedArray typedArray = getContext().obtainStyledAttributes(new int[]{16842801, 16842808});
            this.mOrgBgColor = typedArray.getColor(0, 0);
            this.mTextColor = typedArray.getColor(1, 0);
            typedArray.recycle();
            initView();
        }

        public void setLastUpdateTimeStamp(long timeStamp) {
            this.mTimeStamp = timeStamp;
        }

        @Override // huawei.android.widget.pattern.HwRefreshLayout.UpdateHandler
        public void onProgressUpdate(HwRefreshLayout layout, Progress progress, int status) {
            if (status == 0) {
                initView();
                if (HwRefreshLayout.this.mOnLastUpdateTimeShowListener != null && this.mTimeStamp != 0) {
                    HwRefreshLayout.this.reInitHeadMesure();
                }
            } else if (status == 1) {
                changeProgressbarSize(progress);
            } else if (status == 2) {
                changeProgressbarSize(progress);
            } else if (status == 3) {
                changeProgressbarSize(progress);
                if (isLayoutRtl()) {
                    this.mTextViewMsg.setPadding(0, 0, this.mMinWidth + this.mPaddingM, 0);
                } else {
                    this.mTextViewMsg.setPadding(this.mMinWidth + this.mPaddingM, 0, 0, 0);
                }
                this.mTextViewMsg.setText(HwRefreshLayout.this.mRefreshingMsg);
                this.mProgressbar.setVisibility(0);
            } else if (status == 5) {
                handleRefreshComplete();
            } else if (status == 6) {
                handleRefreshFail();
            }
        }

        private void handleRefreshComplete() {
            if (this.mIsRunPassAnimEnable) {
                this.mIsRunPassAnimEnable = false;
                this.mTextViewMsg.setTextColor(ResLoaderUtil.getColor(getContext(), "emui_white"));
                this.mTextViewMsg.setPadding(0, 0, 0, 0);
                this.mTextViewMsg.setText(HwRefreshLayout.this.mSuccessMsg);
                this.mTimeStamp = System.currentTimeMillis();
                this.mProgressbar.setVisibility(8);
                this.mTextViewInfo.setVisibility(8);
                this.mRootView.setAlpha(0.0f);
                this.mRootView.setBackgroundColor(ResLoaderUtil.getColor(getContext(), "hwpt_refresh_success"));
                this.mTextViewMsg.post(new Runnable() {
                    /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass1 */

                    public void run() {
                        DefaultHeaderView.this.runPassAnim();
                    }
                });
            }
        }

        private void handleRefreshFail() {
            if (this.mIsRunPassAnimEnable) {
                this.mIsRunPassAnimEnable = false;
                this.mTextViewMsg.setTextColor(ResLoaderUtil.getColor(getContext(), "emui_white"));
                this.mTextViewMsg.setPadding(0, 0, 0, 0);
                this.mTextViewMsg.setText(HwRefreshLayout.this.mFailureMsg);
                this.mProgressbar.setVisibility(8);
                this.mTextViewInfo.setVisibility(8);
                this.mRootView.setAlpha(0.0f);
                this.mRootView.setBackgroundColor(ResLoaderUtil.getColor(getContext(), "hwpt_refresh_fail"));
                this.mTextViewMsg.post(new Runnable() {
                    /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass2 */

                    public void run() {
                        DefaultHeaderView.this.runPassAnim();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void runPassAnim() {
            View view = this.mRootView;
            int i = this.mPaddingM;
            view.setPadding(i, 0, i, 0);
            int time = LOADING_TO_MSG_ANIM_DURATION;
            if (LOADING_TO_MSG_ANIM_DURATION > HwRefreshLayout.COMPLETE_STICK_DURATION) {
                time = 1000;
            }
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass3 */

                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    DefaultHeaderView.this.mTextViewMsg.setAlpha(value);
                    DefaultHeaderView.this.setAlpha(1.0f - value);
                    DefaultHeaderView.this.mRootView.setAlpha(DefaultHeaderView.VIEW_ALPHA);
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass4 */

                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    DefaultHeaderView.this.mProgressbar.setVisibility(8);
                    DefaultHeaderView.this.mTextViewInfo.setVisibility(8);
                }
            });
            anim.setDuration((long) time);
            anim.start();
            handleRootAnimation(time);
        }

        private void handleRootAnimation(int time) {
            ValueAnimator valueAnimator = this.mRootAnim;
            if (valueAnimator != null) {
                valueAnimator.end();
                this.mRootAnim = null;
            }
            int height = this.mTextViewMsg.getHeight();
            int i = this.mPaddingM;
            this.mRootAnim = ValueAnimator.ofInt(this.mRootView.getHeight(), height + i + i);
            this.mRootAnim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            this.mRootAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass5 */

                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = ((Integer) animation.getAnimatedValue()).intValue();
                    ViewGroup.LayoutParams layoutParams = DefaultHeaderView.this.mRootView.getLayoutParams();
                    layoutParams.height = value;
                    DefaultHeaderView.this.mRootView.setLayoutParams(layoutParams);
                }
            });
            this.mRootAnim.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass6 */

                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ViewGroup.LayoutParams layoutParams = DefaultHeaderView.this.mRootView.getLayoutParams();
                    layoutParams.height = -2;
                    DefaultHeaderView.this.mRootView.setLayoutParams(layoutParams);
                    DefaultHeaderView.this.mRootView.setPadding(DefaultHeaderView.this.mPaddingM, DefaultHeaderView.this.mPaddingM, DefaultHeaderView.this.mPaddingM, DefaultHeaderView.this.mPaddingM);
                }
            });
            this.mRootAnim.setDuration((long) time);
            this.mRootAnim.start();
        }

        @Override // huawei.android.widget.pattern.HwRefreshLayout.UpdateHandler
        public void dealScrollUpAction() {
            progressAnimAction();
            infoAnimAction();
            rootAnimAction();
        }

        private void rootAnimAction() {
            int to;
            View view = this.mRootView;
            int i = this.mPaddingM;
            view.setPadding(i, i, i, i);
            int from = this.mRootView.getHeight();
            int height = this.mTextViewMsg.getHeight();
            int i2 = this.mMinWidth;
            if (height > i2) {
                int height2 = this.mTextViewMsg.getHeight();
                int i3 = this.mPaddingM;
                to = height2 + i3 + i3;
            } else {
                int to2 = this.mPaddingM;
                to = to2 + i2 + to2;
            }
            this.mRootAnim = ValueAnimator.ofInt(from, to);
            this.mRootAnim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            this.mRootAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass7 */

                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = ((Integer) animation.getAnimatedValue()).intValue();
                    ViewGroup.LayoutParams layoutParams = DefaultHeaderView.this.mRootView.getLayoutParams();
                    layoutParams.height = value;
                    DefaultHeaderView.this.mRootView.setLayoutParams(layoutParams);
                }
            });
            this.mRootAnim.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass8 */

                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ViewGroup.LayoutParams layoutParams = DefaultHeaderView.this.mRootView.getLayoutParams();
                    layoutParams.height = -2;
                    DefaultHeaderView.this.mRootView.setLayoutParams(layoutParams);
                }
            });
            this.mRootAnim.setDuration(300L);
            this.mRootAnim.start();
        }

        private void progressAnimAction() {
            int to;
            int from;
            if (isLayoutRtl()) {
                from = this.mProgressbar.getRight();
                to = this.mTextViewMsg.getRight();
            } else {
                from = this.mProgressbar.getLeft();
                to = this.mTextViewMsg.getLeft();
            }
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, (float) (to - from));
            anim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass9 */

                public void onAnimationUpdate(ValueAnimator animation) {
                    DefaultHeaderView.this.mProgressbar.setTranslationX(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            anim.setDuration(300L);
            anim.start();
            ValueAnimator anim2 = ValueAnimator.ofInt(this.mProgressbar.getWidth(), this.mMinWidth);
            anim2.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass10 */

                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = ((Integer) animation.getAnimatedValue()).intValue();
                    ViewGroup.LayoutParams layoutParams = DefaultHeaderView.this.mProgressbar.getLayoutParams();
                    layoutParams.height = value;
                    layoutParams.width = value;
                    DefaultHeaderView.this.mProgressbar.setLayoutParams(layoutParams);
                }
            });
            anim2.setDuration(300L);
            anim2.start();
        }

        private void infoAnimAction() {
            ValueAnimator infoAnim = ValueAnimator.ofFloat(1.0f, 0.0f);
            infoAnim.setInterpolator(HwRefreshLayout.this.getLinearOutSlowInInterpolator());
            infoAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass11 */

                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    DefaultHeaderView.this.mTextViewInfo.setTextSize(0, DefaultHeaderView.this.mInfoTextSize * value);
                    DefaultHeaderView.this.mTextViewInfo.setAlpha(value);
                }
            });
            infoAnim.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass12 */

                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    DefaultHeaderView.this.mTextViewInfo.setVisibility(8);
                    DefaultHeaderView.this.msgAnimAction();
                }
            });
            infoAnim.setDuration(150L);
            infoAnim.start();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void msgAnimAction() {
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.pattern.HwRefreshLayout.DefaultHeaderView.AnonymousClass13 */

                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    DefaultHeaderView.this.mTextViewMsg.setAlpha(value);
                    DefaultHeaderView.this.mTextViewMsg.setScaleY(value);
                    DefaultHeaderView.this.mTextViewMsg.setScaleX(value);
                }
            });
            anim.setDuration(150L);
            anim.start();
        }

        private void changeProgressbarSize(Progress progress) {
            float scale = (((float) progress.getCurrentY()) * 1.0f) / ((float) progress.getRefreshY());
            if (scale > MAX_SCALE_FACTOR) {
                scale = MAX_SCALE_FACTOR;
            }
            float alpha = scale;
            if (alpha > 1.0f) {
                alpha = 1.0f;
            }
            this.mTextViewInfo.setAlpha(alpha);
            this.mProgressbar.setAlpha(alpha);
            ViewGroup.LayoutParams lp = this.mProgressbar.getLayoutParams();
            int width = (int) (((float) this.mMaxWidth) * scale);
            lp.height = width;
            lp.width = width;
            this.mProgressbar.setLayoutParams(lp);
        }

        private void initView() {
            this.mIsRunPassAnimEnable = true;
            if (HwRefreshLayout.this.mOnLastUpdateTimeShowListener == null || this.mTimeStamp == 0) {
                this.mTextViewInfo.setVisibility(8);
            } else {
                this.mTextViewInfo.setVisibility(0);
                this.mTextViewInfo.setText(HwRefreshLayout.this.mOnLastUpdateTimeShowListener.getDescription(this.mTimeStamp));
                this.mTextViewInfo.setTextSize(0, this.mInfoTextSize);
                this.mTextViewInfo.setAlpha(1.0f);
            }
            this.mTextViewMsg.setVisibility(0);
            this.mTextViewMsg.setText(BuildConfig.FLAVOR);
            this.mTextViewMsg.setAlpha(0.0f);
            this.mTextViewMsg.setScaleY(1.0f);
            this.mTextViewMsg.setScaleX(1.0f);
            this.mTextViewMsg.setTextColor(this.mTextColor);
            this.mProgressbar.setVisibility(0);
            this.mProgressbar.setTranslationX(0.0f);
            ViewGroup.LayoutParams layoutParams = this.mProgressbar.getLayoutParams();
            int i = this.mMaxWidth;
            layoutParams.height = i;
            layoutParams.width = i;
            this.mProgressbar.setLayoutParams(layoutParams);
            ViewGroup.LayoutParams layoutParams2 = this.mRootView.getLayoutParams();
            if (layoutParams2 == null) {
                layoutParams2 = new ViewGroup.LayoutParams(-1, -2);
            }
            layoutParams2.height = -2;
            View view = this.mRootView;
            int i2 = this.mPaddingM;
            int i3 = this.mPaddingL;
            view.setPadding(i2, i3, i2, i3);
            this.mRootView.setLayoutParams(layoutParams2);
            this.mRootView.setBackgroundColor(getResources().getColor(17170445));
            setBackgroundColor(this.mOrgBgColor);
            setAlpha(1.0f);
        }
    }

    /* access modifiers changed from: package-private */
    public class Progress {
        private int mCurrentY;
        private int mRefreshY;
        private int mTotalY;

        Progress() {
        }

        /* access modifiers changed from: package-private */
        public int getRefreshY() {
            return this.mRefreshY;
        }

        /* access modifiers changed from: package-private */
        public void setRefreshY(int refreshY) {
            this.mRefreshY = refreshY;
        }

        /* access modifiers changed from: package-private */
        public int getTotalY() {
            return this.mTotalY;
        }

        /* access modifiers changed from: package-private */
        public void setTotalY(int totalY) {
            this.mTotalY = totalY;
        }

        /* access modifiers changed from: package-private */
        public int getCurrentY() {
            return this.mCurrentY;
        }

        /* access modifiers changed from: package-private */
        public void setCurrentY(int currentY) {
            this.mCurrentY = currentY;
        }
    }
}
