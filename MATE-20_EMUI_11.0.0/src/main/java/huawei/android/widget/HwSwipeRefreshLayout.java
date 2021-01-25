package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.huawei.android.os.VibratorEx;
import com.huawei.android.view.ViewEx;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwSwipeRefreshLayout extends FrameLayout {
    private static final String CAN_REFRESH_TEXT = "up to refresh";
    private static final float CHILD_VIEW_MOVE_FACTOR = 125.0f;
    private static final int DEFAULT_SCROLL_DIRECTION = -1;
    private static final float DIRECTION_RATIO = 1.5f;
    private static final float DRAW_DIV = 2.0f;
    private static final String HW_VIBRATOR_TYPE_COMMON_THRESHOLD = "haptic.common.threshold";
    private static final float LOADING_START_DISTANCE_FACTOR = 0.2f;
    private static final float MAX_PULL_DISTANCE_FACTOR = 0.25f;
    private static final int MIN_HEGHT_THRESHOLD = 16;
    private static final float OFF_DIV = 2.0f;
    private static final float PROGRESS_BAR_APPEAR_DISTANCE_FACTOR = 0.15f;
    private static final float PROGRESS_BAR_APPEAR_SCALE = 0.5f;
    private static final int PROGRESS_BAR_LOADING_POSITION_DP = 20;
    private static final float PROGRESS_BAR_MAX_EXTRA_SCALE = 0.1f;
    private static final int PROGRESS_BAR_MAX_SCALE_POSITION_DP = 40;
    private static final float PROGRESS_BAR_NORMAL_SCALE = 1.0f;
    private static final int PROGRESS_BAR_ROTATION = 15;
    private static final int PROGRESS_BAR_SIZE_DP = 40;
    private static final String PULL_DOWN_TEXT = "pull down";
    private static final String TAG = "HwSwipeRefreshLayout";
    private static final int TEXT_ALPHA_VAL = 255;
    private static final int VIBRATOR_THRESHOLD_INDEX = 4;
    private static final int WIDTH_DICHOTOMY = 2;
    private Callback mCallback;
    private String mCanRefreshText;
    private View mChildView;
    private float mCurPosX;
    private float mCurPosY;
    private RefreshHeaderView mHeaderView;
    private boolean mIsRefreshing;
    private boolean mIsStartBackAnimating;
    private boolean mIsSupportVibrator;
    private boolean mIsVibrate;
    private int mLoadingStartDistance;
    private int mMaxPullDistance;
    private float mOffsetLoadingStartDistance;
    private float mOffsetMaxPullDistance;
    private float mOffsetProgressBarAppearDistance;
    private int mProgressBarAppearDistance;
    private int mProgressBarSize;
    private int mProgressBarY;
    private String mPullDownText;
    protected ProgressBarColor mRefreshingBar;
    private ResLoader mResLoader;
    private int mScaledProgressBarSize;
    private View mScrollChildView;
    private float mStartX;
    private float mStartY;
    private int mTouchSlop;
    private VibratorEx mVibratorEx;

    public interface Callback {
        boolean canSwipeToRefresh();

        void closeSwipeLayout();

        void onRefresh();

        boolean supportSwipeToRefresh();
    }

    public HwSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public HwSwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mVibratorEx = new VibratorEx();
        this.mIsStartBackAnimating = false;
        this.mIsSupportVibrator = false;
        this.mIsVibrate = true;
        init();
        this.mIsSupportVibrator = this.mVibratorEx.isSupportHwVibrator(HW_VIBRATOR_TYPE_COMMON_THRESHOLD);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mProgressBarY = getPaddingTop();
    }

    public void startFinishRefreshingAnim() {
        if (!this.mIsRefreshing || this.mIsStartBackAnimating) {
            Log.d(TAG, "startFinishRefreshingAnim, but not refreshing or mIsStartBackAnimating return");
            return;
        }
        RefreshHeaderView refreshHeaderView = this.mHeaderView;
        if (refreshHeaderView != null && this.mRefreshingBar != null && this.mChildView != null) {
            refreshHeaderView.setPullState(RefreshHeaderView.REFRESH_FINISH);
            this.mIsStartBackAnimating = true;
            this.mHeaderView.startBackAnim(this.mRefreshingBar, this.mChildView);
        }
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setPullDownText(String pullDownText) {
        this.mPullDownText = pullDownText;
    }

    public void setCanRefreshText(String canRefreshText) {
        this.mCanRefreshText = canRefreshText;
    }

    public void setHeaderShadowColor(int color) {
        RefreshHeaderView refreshHeaderView = this.mHeaderView;
        if (refreshHeaderView != null) {
            refreshHeaderView.mTopShadowColors[0] = color;
            this.mHeaderView.invalidate();
        }
    }

    public void setScrollView(View scrollView) {
        this.mScrollChildView = scrollView;
    }

    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureChildView();
        View view = this.mChildView;
        if (view == null || this.mHeaderView == null) {
            Log.w(TAG, "onMeasure view is null mChildView = " + this.mChildView + ", mHeaderView = " + this.mHeaderView);
            return;
        }
        view.measure(View.MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), 1073741824), View.MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824));
        this.mHeaderView.measure(View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMaxPullDistance, 1073741824));
    }

    private void initMaxPullDistance() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Point size = new Point();
        Display display = ((WindowManager) getContext().getApplicationContext().getSystemService("window")).getDefaultDisplay();
        display.getSize(size);
        display.getMetrics(displayMetrics);
        int screenHeight = size.y;
        if (screenHeight < 16) {
            Log.e(TAG, "initMaxPullDistance, the screen height is illegal and the value = " + screenHeight);
        }
        this.mMaxPullDistance = (int) (((float) screenHeight) * MAX_PULL_DISTANCE_FACTOR);
        this.mProgressBarAppearDistance = (int) (((float) screenHeight) * PROGRESS_BAR_APPEAR_DISTANCE_FACTOR);
        this.mLoadingStartDistance = (int) (((float) screenHeight) * 0.2f);
        this.mOffsetMaxPullDistance = getMoveY((float) this.mMaxPullDistance);
        this.mOffsetLoadingStartDistance = getMoveY((float) this.mLoadingStartDistance);
        this.mOffsetProgressBarAppearDistance = getMoveY((float) this.mProgressBarAppearDistance);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        ensureChildView();
        if (this.mChildView == null || this.mHeaderView == null || this.mRefreshingBar == null) {
            Log.w(TAG, "onLayout view is null mChildView = " + this.mChildView + ", mHeaderView = " + this.mHeaderView);
            return;
        }
        View child = this.mChildView;
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        child.layout(childLeft, childTop, childLeft + ((width - getPaddingLeft()) - getPaddingRight()), childTop + ((height - getPaddingTop()) - getPaddingBottom()));
        this.mHeaderView.layout(childLeft, childTop, childLeft + width, this.mMaxPullDistance + childTop);
        ProgressBarColor progressBarColor = this.mRefreshingBar;
        int i = this.mScaledProgressBarSize;
        int i2 = this.mProgressBarY;
        progressBarColor.layout((width / 2) - (i / 2), i2, (width / 2) + (i / 2), i + i2);
    }

    private boolean canChildScrollVertically() {
        if (this.mChildView == null && this.mScrollChildView == null) {
            return false;
        }
        View view = this.mScrollChildView;
        if (view == null) {
            return this.mChildView.canScrollVertically(-1);
        }
        if (view instanceof AbsListView) {
            try {
                Method method = Class.forName("android.widget.AbsListView").getDeclaredMethod("canScrollUp", new Class[0]);
                method.setAccessible(true);
                Object object = method.invoke(this.mScrollChildView, new Object[0]);
                if (object instanceof Boolean) {
                    return ((Boolean) object).booleanValue();
                }
                return false;
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "canChildScrollVertically() ClassNotFoundException");
            } catch (NoSuchMethodException e2) {
                Log.e(TAG, "canChildScrollVertically() NoSuchMethodException");
            } catch (SecurityException e3) {
                Log.e(TAG, "canChildScrollVertically() SecurityException");
            } catch (IllegalAccessException e4) {
                Log.e(TAG, "canChildScrollVertically() IllegalAccessException");
            } catch (IllegalArgumentException e5) {
                Log.e(TAG, "canChildScrollVertically() IllegalArgumentException");
            } catch (InvocationTargetException e6) {
                Log.e(TAG, "canChildScrollVertically() InvocationTargetException");
            }
        }
        return this.mScrollChildView.canScrollVertically(-1);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean z = false;
        if (event == null) {
            return false;
        }
        if (this.mIsStartBackAnimating) {
            return true;
        }
        if (event.getAction() == 0) {
            this.mStartX = event.getX();
            this.mCurPosX = this.mStartX;
            this.mStartY = event.getY();
            this.mCurPosY = this.mStartY;
        }
        if (this.mIsRefreshing || !this.mCallback.supportSwipeToRefresh()) {
            return super.onInterceptTouchEvent(event);
        }
        if (event.getAction() == 2) {
            this.mCurPosX = event.getX();
            this.mCurPosY = event.getY();
            float dx = this.mCurPosX - this.mStartX;
            float dy = this.mCurPosY - this.mStartY;
            boolean isSwipeVertical = false;
            if (Float.compare(dx, 0.0f) != 0) {
                if (Math.abs(dy) / Math.abs(dx) > DIRECTION_RATIO) {
                    z = true;
                }
                isSwipeVertical = z;
            }
            if (isSwipeVertical && dy > ((float) this.mTouchSlop) && !canChildScrollVertically() && !this.mIsRefreshing) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        if (this.mHeaderView == null || this.mChildView == null || this.mRefreshingBar == null) {
            Log.w(TAG, "onTouchEvent view is null mChildView = " + this.mChildView + ", mHeaderView = " + this.mHeaderView);
            return super.onTouchEvent(event);
        } else if (this.mIsStartBackAnimating) {
            return true;
        } else {
            if (this.mIsRefreshing || !this.mCallback.supportSwipeToRefresh()) {
                return super.onTouchEvent(event);
            }
            int action = event.getAction();
            if (action != 0) {
                if (action != 1) {
                    if (action == 2) {
                        moveTouch(event);
                        return true;
                    } else if (action != 3) {
                        return super.onTouchEvent(event);
                    }
                }
                return upOrCancelTouch();
            }
            this.mHeaderView.setPullState(RefreshHeaderView.PULL_DOWN);
            return true;
        }
    }

    private boolean upOrCancelTouch() {
        if (this.mHeaderView.getPullState() != 6666 || !this.mCallback.canSwipeToRefresh()) {
            this.mHeaderView.startNoRefreshBackAnim(this.mChildView);
        } else {
            this.mHeaderView.setPullState(RefreshHeaderView.UP_REFRESH);
            this.mHeaderView.startUpAnim(this.mChildView);
            int pullTextId = this.mResLoader.getIdentifier(this.mContext, ResLoaderUtil.STRING, "release_to_refresh");
            if (pullTextId == 0) {
                Log.w(TAG, "pullTextId not found");
                return true;
            }
            String pullText = getResources().getString(pullTextId);
            if (!TextUtils.isEmpty(pullText) && pullText.equalsIgnoreCase(this.mCanRefreshText)) {
                int pullTextId2 = this.mResLoader.getIdentifier(this.mContext, ResLoaderUtil.STRING, "refreshing");
                if (pullTextId2 == 0) {
                    Log.w(TAG, "pullTextId not found");
                    return true;
                }
                this.mHeaderView.setPullText(getResources().getString(pullTextId2));
            }
            this.mIsRefreshing = true;
        }
        this.mCallback.closeSwipeLayout();
        return true;
    }

    private void moveTouch(MotionEvent event) {
        float offsetY;
        this.mCurPosY = event.getY();
        float dy = this.mCurPosY - this.mStartY;
        float dy2 = 0.0f;
        if (dy > 0.0f) {
            dy2 = dy;
        }
        float offsetY2 = getMoveY(dy2);
        int i = this.mTouchSlop;
        if (((float) i) < offsetY2) {
            offsetY = offsetY2 - ((float) i);
        } else {
            offsetY = 0.0f;
        }
        float offsetY3 = this.mOffsetMaxPullDistance;
        if (offsetY3 > offsetY) {
            offsetY3 = offsetY;
        }
        this.mChildView.setTranslationY(offsetY3);
        this.mHeaderView.setDragheight(offsetY3);
        this.mHeaderView.invalidate();
        if (this.mOffsetLoadingStartDistance > offsetY3) {
            this.mIsVibrate = true;
        }
        if (this.mOffsetLoadingStartDistance <= offsetY3) {
            if (this.mIsSupportVibrator && isHapticFeedbackEnabled() && this.mIsVibrate) {
                ViewEx.performHwHapticFeedback(this, 4, 0);
                this.mIsVibrate = false;
            }
            this.mRefreshingBar.setVisibility(0);
            setRefreshingBarAnimLoadingLine(offsetY3);
            this.mHeaderView.setPullState(RefreshHeaderView.CAN_UP_TO_REFRESH);
            this.mHeaderView.setPullText(this.mCanRefreshText);
        } else if (this.mOffsetProgressBarAppearDistance < offsetY3) {
            this.mRefreshingBar.setVisibility(0);
            setRefreshingBarAppearLine(offsetY3);
            this.mHeaderView.setPullState(RefreshHeaderView.PULL_DOWN);
            this.mHeaderView.setPullText(this.mPullDownText);
        } else {
            this.mRefreshingBar.setVisibility(4);
            this.mHeaderView.setPullState(RefreshHeaderView.PULL_DOWN);
            this.mHeaderView.setPullText(this.mPullDownText);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRefreshingBarAnimLoadingLine(float offsetY) {
        float f = this.mOffsetMaxPullDistance;
        float f2 = this.mOffsetLoadingStartDistance;
        int diff = (int) (f - f2);
        if (diff == 0) {
            Log.e(TAG, "setRefreshingBarAnimLoadingLine, the difference between two distance is illegal!");
            return;
        }
        float progressBarScaleRatio = (0.1f * ((offsetY - f2) / ((float) diff))) + 1.0f;
        this.mRefreshingBar.setScaleX(progressBarScaleRatio);
        this.mRefreshingBar.setScaleY(progressBarScaleRatio);
        float f3 = this.mOffsetLoadingStartDistance;
        float f4 = this.mOffsetProgressBarAppearDistance;
        int appearDistanceDiff = (int) (f3 - f4);
        if (appearDistanceDiff != 0) {
            this.mRefreshingBar.setAlpha((offsetY - f4) / ((float) appearDistanceDiff));
        }
        this.mScaledProgressBarSize = (int) (((float) this.mProgressBarSize) * progressBarScaleRatio);
        this.mProgressBarY = (int) ((offsetY / 2.0f) - (((float) this.mScaledProgressBarSize) / 2.0f));
        this.mRefreshingBar.requestLayout();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRefreshingBarAppearLine(float offsetY) {
        float f = this.mOffsetLoadingStartDistance;
        float f2 = this.mOffsetProgressBarAppearDistance;
        int diff = (int) (f - f2);
        if (diff == 0) {
            Log.e(TAG, "setRefreshingBarAppearLine, the difference between he two distance is illegal!");
            return;
        }
        float distanceRatio = (offsetY - f2) / ((float) diff);
        this.mRefreshingBar.setAlpha(distanceRatio);
        float progressBarScaleRatio = (distanceRatio * 0.5f) + 0.5f;
        this.mRefreshingBar.setScaleX(progressBarScaleRatio);
        this.mRefreshingBar.setScaleY(progressBarScaleRatio);
        this.mScaledProgressBarSize = (int) (((float) this.mProgressBarSize) * progressBarScaleRatio);
        this.mProgressBarY = (int) ((offsetY / 2.0f) - (((float) this.mScaledProgressBarSize) / 2.0f));
        this.mRefreshingBar.requestLayout();
    }

    private void ensureChildView() {
        if (this.mChildView == null) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (!child.equals(this.mHeaderView) && !child.equals(this.mRefreshingBar)) {
                    this.mChildView = child;
                    return;
                }
            }
        }
    }

    private void init() {
        this.mResLoader = ResLoader.getInstance();
        initMaxPullDistance();
        addHeader();
        addProgressBar();
    }

    private void addHeader() {
        setPullDownText(PULL_DOWN_TEXT);
        setCanRefreshText(CAN_REFRESH_TEXT);
        this.mHeaderView = new RefreshHeaderView(getContext());
        addView(this.mHeaderView);
    }

    private float getMoveY(float distance) {
        if (distance > CHILD_VIEW_MOVE_FACTOR) {
            return (float) Math.sqrt((double) (CHILD_VIEW_MOVE_FACTOR * distance));
        }
        return distance;
    }

    private void addProgressBar() {
        this.mProgressBarSize = (int) (getResources().getDisplayMetrics().scaledDensity * 40.0f);
        this.mScaledProgressBarSize = this.mProgressBarSize;
        this.mRefreshingBar = new ProgressBarColor(getContext());
        int progressBarId = this.mResLoader.getIdentifier(this.mContext, ResLoaderUtil.ID, "hw_refresh_progressbar");
        if (progressBarId == 0) {
            Log.w(TAG, "progressBarId not found");
            return;
        }
        this.mRefreshingBar.setId(progressBarId);
        addView(this.mRefreshingBar);
        this.mRefreshingBar.setVisibility(4);
    }

    public static class ProgressBarColor extends ProgressBar {
        public ProgressBarColor(Context context) {
            super(context);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.ProgressBar, android.view.View
        public synchronized void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
    }

    /* access modifiers changed from: private */
    public class RefreshHeaderView extends View {
        private static final int BACK_TO_LOADING_DURATION = 100;
        private static final int BACK_TO_TOP_DURATION = 350;
        private static final int BALL_COLOR = -16744193;
        private static final float BALL_PADDING_FACTOR = 0.04f;
        private static final int BALL_R_DP = 4;
        public static final int CAN_UP_TO_REFRESH = 6666;
        private static final float CHILD_VIEW_REFRESH_UP_DISTANCE_FACTOR = 0.3f;
        private static final int DEFAULT_SHADOW_START_COLOR = -1;
        private static final float MAX_BALL_PULL_DISTANCE_FACTOR = 0.3f;
        private static final float MAX_TEXT_ALPHA = 0.5f;
        private static final float MAX_TEXT_PULL_DISTANCE_FACTOR = 0.27f;
        private static final float MAX_TEXT_SIZE_SCALE = 1.1f;
        public static final int NO_REFRESH_BACK = 2222;
        private static final float PROGRESS_BAR_TOP_PADDING_FACTOR = 0.1f;
        public static final int PULL_DOWN = 1111;
        private static final float PULL_END_CIRCLE_SCALE = 1.0f;
        private static final float PULL_MAX_CIRCLE_SCALE = 1.0f;
        private static final float PULL_TAIL_HEIGHT = 25.0f;
        public static final int REFRESHING = 4444;
        private static final float REFRESH_BALL_DISTANCE_FACTOR = 0.03f;
        public static final int REFRESH_FINISH = 5555;
        private static final float REFRESH_TEXT_DISTANCE_FACTOR = 0.15f;
        private static final int SHADOW_END_COLOR = 16777215;
        private static final float TAIL_CTRL_POINT_MAX_DEGREE_FACTOR = 0.75f;
        private static final float TAIL_START_POINT_FACTOR = 1.2f;
        private static final float TEXT_BOTTOM_PADDING_FACTOR = 0.04f;
        private static final int TEXT_SIZE_DP = 13;
        private static final float TOP_SHADOW_HEIGHT_FACTOR = 0.0066f;
        public static final int UP_REFRESH = 3333;
        private float mBallScale;
        private float mBallY;
        private TimeInterpolator mDecelerationInterpolater;
        private float mDragHeight;
        private float mMaxTextPullDistance;
        private Paint mPaint;
        private int mPullState;
        private String mPullText;
        private int mTextAlpha;
        private Paint mTextPaint;
        private float mTextScale;
        private float mTextSize;
        private float mTextY;
        private GradientDrawable mTopDrawable;
        private int[] mTopShadowColors;
        private ValueAnimator.AnimatorUpdateListener mUpdateListener;

        private RefreshHeaderView(HwSwipeRefreshLayout hwSwipeRefreshLayout, Context context) {
            this(hwSwipeRefreshLayout, context, (AttributeSet) null);
        }

        private RefreshHeaderView(HwSwipeRefreshLayout hwSwipeRefreshLayout, Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        private RefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            this.mPaint = new Paint();
            this.mTextPaint = new Paint();
            this.mPullText = HwSwipeRefreshLayout.PULL_DOWN_TEXT;
            this.mPullState = PULL_DOWN;
            this.mTopShadowColors = new int[]{-1, SHADOW_END_COLOR};
            this.mTopDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, this.mTopShadowColors);
            this.mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.HwSwipeRefreshLayout.RefreshHeaderView.AnonymousClass1 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    RefreshHeaderView.this.invalidate();
                }
            };
            init(context);
        }

        private void init(Context context) {
            this.mTextSize = getResources().getDisplayMetrics().scaledDensity * 13.0f;
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStyle(Paint.Style.FILL);
            this.mPaint.setColor(-1);
            this.mTextPaint.setColor(context.getColor(HwSwipeRefreshLayout.this.mResLoader.getIdentifier(this.mContext, ResLoaderUtil.COLOR, "swipe_refresh_text_color")));
            this.mTextPaint.setTextAlign(Paint.Align.CENTER);
            this.mTextPaint.setTextSize(this.mTextSize);
            this.mTextPaint.setAntiAlias(true);
            this.mTopDrawable.setGradientType(0);
            this.mDecelerationInterpolater = AnimationUtils.loadInterpolator(context, 34078893);
            this.mMaxTextPullDistance = HwSwipeRefreshLayout.this.mOffsetMaxPullDistance;
        }

        public int getTextAlpha() {
            return this.mTextAlpha;
        }

        public void setTextAlpha(int textAlpha) {
            this.mTextAlpha = textAlpha;
        }

        public float getTextY() {
            return this.mTextY;
        }

        public void setTextY(float textY) {
            this.mTextY = textY;
        }

        public float getBallY() {
            return this.mBallY;
        }

        public void setBallY(float ballY) {
            this.mBallY = ballY;
        }

        public float getTextScale() {
            return this.mTextScale;
        }

        public void setTextScale(float textScale) {
            this.mTextScale = textScale;
        }

        public float getBallScale() {
            return this.mBallScale;
        }

        public void setBallScale(float ballScale) {
            this.mBallScale = ballScale;
        }

        public void setPullText(String text) {
            this.mPullText = text;
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            drawText(canvas);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setDragheight(float dragHeight) {
            this.mDragHeight = dragHeight;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setPullState(int pullState) {
            this.mPullState = pullState;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getPullState() {
            return this.mPullState;
        }

        private void drawText(Canvas canvas) {
            int i = this.mPullState;
            if (i == 1111 || i == 6666 || i == 2222) {
                if (this.mPullState != 2222) {
                    this.mTextY = this.mDragHeight;
                    float f = this.mMaxTextPullDistance;
                    if (f < this.mTextY) {
                        this.mTextY = f;
                    }
                }
                float f2 = this.mTextY;
                float f3 = this.mMaxTextPullDistance;
                this.mTextScale = ((f2 / f3) * 0.100000024f) + 1.0f;
                this.mTextAlpha = (int) ((f2 / f3) * 127.5f);
            }
            float textSize = this.mTextSize * this.mTextScale;
            this.mTextPaint.setAlpha(this.mTextAlpha);
            this.mTextPaint.setTextSize(textSize);
            String str = this.mPullText;
            canvas.drawText(str, 0, str.length(), ((float) getWidth()) / 2.0f, this.mTextY, this.mTextPaint);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startUpAnim(View childView) {
            HwSwipeRefreshLayout.this.mRefreshingBar.setAlpha(1.0f);
            ObjectAnimator textAnim = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("TextY", HwSwipeRefreshLayout.this.mOffsetLoadingStartDistance), PropertyValuesHolder.ofFloat("TextScale", 1.0f));
            textAnim.setInterpolator(this.mDecelerationInterpolater);
            textAnim.setDuration(100L);
            textAnim.addUpdateListener(this.mUpdateListener);
            textAnim.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.widget.HwSwipeRefreshLayout.RefreshHeaderView.AnonymousClass2 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (HwSwipeRefreshLayout.this.mCallback != null) {
                        HwSwipeRefreshLayout.this.mCallback.onRefresh();
                    }
                    HwSwipeRefreshLayout.this.mHeaderView.setPullState(RefreshHeaderView.REFRESHING);
                    HwSwipeRefreshLayout.this.mIsVibrate = true;
                }
            });
            ObjectAnimator childViewAnim = ObjectAnimator.ofFloat(childView, "translationY", HwSwipeRefreshLayout.this.mOffsetLoadingStartDistance);
            childViewAnim.setInterpolator(this.mDecelerationInterpolater);
            childViewAnim.setDuration(100L);
            childViewAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.HwSwipeRefreshLayout.RefreshHeaderView.AnonymousClass3 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    HwSwipeRefreshLayout.this.setRefreshingBarAnimLoadingLine(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            AnimatorSet refreshAnim = new AnimatorSet();
            refreshAnim.play(textAnim).with(childViewAnim);
            refreshAnim.start();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startBackAnim(View loadingView, View childView) {
            ObjectAnimator textAnim = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofInt("TextAlpha", 0), PropertyValuesHolder.ofFloat("TextY", 0.0f));
            textAnim.setInterpolator(this.mDecelerationInterpolater);
            textAnim.setDuration(350L);
            textAnim.addUpdateListener(this.mUpdateListener);
            ObjectAnimator childViewAnim = ObjectAnimator.ofFloat(childView, "translationY", 0.0f);
            childViewAnim.setInterpolator(this.mDecelerationInterpolater);
            childViewAnim.setDuration(350L);
            childViewAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.HwSwipeRefreshLayout.RefreshHeaderView.AnonymousClass4 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation != null) {
                        float offsetY = 0.0f;
                        Object obj = animation.getAnimatedValue();
                        if (obj instanceof Float) {
                            offsetY = ((Float) obj).floatValue();
                        }
                        HwSwipeRefreshLayout.this.setRefreshingBarAppearLine(offsetY);
                    }
                }
            });
            AnimatorSet backAnim = new AnimatorSet();
            backAnim.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.widget.HwSwipeRefreshLayout.RefreshHeaderView.AnonymousClass5 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (HwSwipeRefreshLayout.this.mRefreshingBar != null) {
                        HwSwipeRefreshLayout.this.mRefreshingBar.setVisibility(4);
                        HwSwipeRefreshLayout.this.mIsRefreshing = false;
                        HwSwipeRefreshLayout.this.mIsStartBackAnimating = false;
                        HwSwipeRefreshLayout.this.mHeaderView.setBallY(0.0f);
                    }
                }
            });
            backAnim.play(textAnim).with(childViewAnim);
            backAnim.start();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startNoRefreshBackAnim(View childView) {
            setPullState(NO_REFRESH_BACK);
            ObjectAnimator textBackAnim = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("TextY", 0.0f));
            textBackAnim.setInterpolator(this.mDecelerationInterpolater);
            textBackAnim.setDuration(350L);
            textBackAnim.addUpdateListener(this.mUpdateListener);
            ObjectAnimator childViewAnim = ObjectAnimator.ofFloat(childView, "translationY", 0.0f);
            childViewAnim.setInterpolator(this.mDecelerationInterpolater);
            childViewAnim.setDuration(350L);
            childViewAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.HwSwipeRefreshLayout.RefreshHeaderView.AnonymousClass6 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation != null) {
                        float offsetY = 0.0f;
                        Object obj = animation.getAnimatedValue();
                        if (obj instanceof Float) {
                            offsetY = ((Float) obj).floatValue();
                        }
                        HwSwipeRefreshLayout.this.setRefreshingBarAppearLine(offsetY);
                    }
                }
            });
            AnimatorSet noRefreshBackAnim = new AnimatorSet();
            noRefreshBackAnim.play(childViewAnim).with(textBackAnim);
            noRefreshBackAnim.start();
        }
    }
}
