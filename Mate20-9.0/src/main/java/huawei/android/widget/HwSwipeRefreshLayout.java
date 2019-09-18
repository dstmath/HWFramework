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
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwSwipeRefreshLayout extends FrameLayout {
    private static final String CAN_REFRESH_TEXT = "up to refresh";
    private static final float CHILD_VIEW_MOVE_FACTOR = 125.0f;
    private static final float DIRECTION_RATIO = 1.5f;
    private static final float LOADING_START_DISTANCE_FACTOR = 0.2f;
    private static final float MAX_PULL_DISTANCE_FACTOR = 0.25f;
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
    private float curPosX;
    private float curPosY;
    /* access modifiers changed from: private */
    public Callback mCallback;
    private String mCanRefreshText;
    private View mChildView;
    /* access modifiers changed from: private */
    public RefreshHeaderView mHeaderView;
    /* access modifiers changed from: private */
    public boolean mIsRefreshing;
    /* access modifiers changed from: private */
    public boolean mIsStartBackAnimating;
    private int mLoadingStartDistance;
    private int mMaxPullDistance;
    /* access modifiers changed from: private */
    public float mOffsetLoadingStartDistance;
    /* access modifiers changed from: private */
    public float mOffsetMaxPullDistance;
    private float mOffsetProgressBarAppearDistance;
    private int mProgressBarAppearDistance;
    private int mProgressBarSize;
    private int mProgressBarY;
    private String mPullDownText;
    protected ProgressBarColor mRefreshingBar;
    /* access modifiers changed from: private */
    public ResLoader mResLoader;
    private int mScaledProgressBarSize;
    private View mScrollChildView;
    private int mTouchSlop;
    private float startX;
    private float startY;

    public interface Callback {
        boolean canSwipeToRefresh();

        void closeSwipeLayout();

        void onRefresh();

        boolean supportSwipeToRefresh();
    }

    public static class ProgressBarColor extends ProgressBar {
        public ProgressBarColor(Context context) {
            super(context);
        }

        /* access modifiers changed from: protected */
        public synchronized void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
    }

    private class RefreshHeaderView extends View {
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
        private ValueAnimator.AnimatorUpdateListener mUpdateListener;
        private GradientDrawable topDrawable;
        /* access modifiers changed from: private */
        public int[] topShadowColors;

        public RefreshHeaderView(HwSwipeRefreshLayout hwSwipeRefreshLayout, Context context) {
            this(hwSwipeRefreshLayout, context, null);
        }

        public RefreshHeaderView(HwSwipeRefreshLayout hwSwipeRefreshLayout, Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public RefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            this.mPaint = new Paint();
            this.mTextPaint = new Paint();
            this.mPullText = HwSwipeRefreshLayout.PULL_DOWN_TEXT;
            this.mPullState = PULL_DOWN;
            this.topShadowColors = new int[]{-1, SHADOW_END_COLOR};
            this.topDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, this.topShadowColors);
            this.mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    RefreshHeaderView.this.invalidate();
                }
            };
            init(context);
        }

        private void init(Context context) {
            this.mTextSize = 13.0f * getResources().getDisplayMetrics().scaledDensity;
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStyle(Paint.Style.FILL);
            this.mPaint.setColor(-1);
            this.mTextPaint.setColor(context.getColor(HwSwipeRefreshLayout.this.mResLoader.getIdentifier(this.mContext, ResLoaderUtil.COLOR, "swipe_refresh_text_color")));
            this.mTextPaint.setTextAlign(Paint.Align.CENTER);
            this.mTextPaint.setTextSize(this.mTextSize);
            this.mTextPaint.setAntiAlias(true);
            this.topDrawable.setGradientType(0);
            this.mDecelerationInterpolater = AnimationUtils.loadInterpolator(context, 34078893);
            this.mMaxTextPullDistance = HwSwipeRefreshLayout.this.mOffsetMaxPullDistance;
        }

        public int getTextAlpha() {
            return this.mTextAlpha;
        }

        public void setTextAlpha(int mTextAlpha2) {
            this.mTextAlpha = mTextAlpha2;
        }

        public float getTextY() {
            return this.mTextY;
        }

        public void setTextY(float mTextY2) {
            this.mTextY = mTextY2;
        }

        public float getBallY() {
            return this.mBallY;
        }

        public void setBallY(float mBallY2) {
            this.mBallY = mBallY2;
        }

        public float getTextScale() {
            return this.mTextScale;
        }

        public void setTextScale(float mTextScale2) {
            this.mTextScale = mTextScale2;
        }

        public float getBallScale() {
            return this.mBallScale;
        }

        public void setBallScale(float mBallScale2) {
            this.mBallScale = mBallScale2;
        }

        public void setPullText(String text) {
            this.mPullText = text;
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            drawText(canvas);
        }

        /* access modifiers changed from: private */
        public void setDragheight(float dragHeight) {
            this.mDragHeight = dragHeight;
        }

        /* access modifiers changed from: private */
        public void setPullState(int pullState) {
            this.mPullState = pullState;
        }

        /* access modifiers changed from: private */
        public int getPullState() {
            return this.mPullState;
        }

        private void drawText(Canvas canvas) {
            if (this.mPullState == 1111 || this.mPullState == 6666 || this.mPullState == 2222) {
                if (this.mPullState != 2222) {
                    this.mTextY = this.mDragHeight;
                    if (this.mTextY > this.mMaxTextPullDistance) {
                        this.mTextY = this.mMaxTextPullDistance;
                    }
                }
                this.mTextScale = HwSwipeRefreshLayout.PROGRESS_BAR_NORMAL_SCALE + ((this.mTextY / this.mMaxTextPullDistance) * 0.100000024f);
                this.mTextAlpha = (int) (127.5f * (this.mTextY / this.mMaxTextPullDistance));
            }
            float textSize = this.mTextSize * this.mTextScale;
            this.mTextPaint.setAlpha(this.mTextAlpha);
            this.mTextPaint.setTextSize(textSize);
            canvas.drawText(this.mPullText, 0, this.mPullText.length(), ((float) getWidth()) / 2.0f, this.mTextY, this.mTextPaint);
        }

        /* access modifiers changed from: private */
        public void startUpAnim(View childView) {
            HwSwipeRefreshLayout.this.mRefreshingBar.setAlpha(HwSwipeRefreshLayout.PROGRESS_BAR_NORMAL_SCALE);
            ObjectAnimator textAnim = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("TextY", new float[]{HwSwipeRefreshLayout.this.mOffsetLoadingStartDistance}), PropertyValuesHolder.ofFloat("TextScale", new float[]{1.0f})});
            textAnim.setInterpolator(this.mDecelerationInterpolater);
            textAnim.setDuration(100);
            textAnim.addUpdateListener(this.mUpdateListener);
            textAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (HwSwipeRefreshLayout.this.mCallback != null) {
                        HwSwipeRefreshLayout.this.mCallback.onRefresh();
                    }
                    HwSwipeRefreshLayout.this.mHeaderView.setPullState(RefreshHeaderView.REFRESHING);
                }
            });
            ObjectAnimator childViewYAnim = ObjectAnimator.ofFloat(childView, "translationY", new float[]{HwSwipeRefreshLayout.this.mOffsetLoadingStartDistance});
            childViewYAnim.setInterpolator(this.mDecelerationInterpolater);
            childViewYAnim.setDuration(100);
            childViewYAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    HwSwipeRefreshLayout.this.setRefreshingBarAnimLoadingLine(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            AnimatorSet refreshAnim = new AnimatorSet();
            refreshAnim.play(textAnim).with(childViewYAnim);
            refreshAnim.start();
        }

        /* access modifiers changed from: private */
        public void startBackAnim(View loadingView, View childView) {
            PropertyValuesHolder textYPvh = PropertyValuesHolder.ofFloat("TextY", new float[]{0.0f});
            ObjectAnimator textAnim = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofInt("TextAlpha", new int[]{0}), textYPvh});
            textAnim.setInterpolator(this.mDecelerationInterpolater);
            textAnim.setDuration(350);
            textAnim.addUpdateListener(this.mUpdateListener);
            ObjectAnimator childViewAnim = ObjectAnimator.ofFloat(childView, "translationY", new float[]{0.0f});
            childViewAnim.setInterpolator(this.mDecelerationInterpolater);
            childViewAnim.setDuration(350);
            childViewAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    HwSwipeRefreshLayout.this.setRefreshingBarAppearLine(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            AnimatorSet backAnim = new AnimatorSet();
            backAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (HwSwipeRefreshLayout.this.mRefreshingBar != null) {
                        HwSwipeRefreshLayout.this.mRefreshingBar.setVisibility(4);
                        boolean unused = HwSwipeRefreshLayout.this.mIsRefreshing = false;
                        boolean unused2 = HwSwipeRefreshLayout.this.mIsStartBackAnimating = false;
                        HwSwipeRefreshLayout.this.mHeaderView.setBallY(0.0f);
                    }
                }
            });
            backAnim.play(textAnim).with(childViewAnim);
            backAnim.start();
        }

        /* access modifiers changed from: private */
        public void startNoRefreshBackAnim(View childView) {
            setPullState(NO_REFRESH_BACK);
            ObjectAnimator textBackAnim = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("TextY", new float[]{0.0f})});
            textBackAnim.setInterpolator(this.mDecelerationInterpolater);
            textBackAnim.setDuration(350);
            textBackAnim.addUpdateListener(this.mUpdateListener);
            ObjectAnimator childViewYAnim = ObjectAnimator.ofFloat(childView, "translationY", new float[]{0.0f});
            childViewYAnim.setInterpolator(this.mDecelerationInterpolater);
            childViewYAnim.setDuration(350);
            childViewYAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    HwSwipeRefreshLayout.this.setRefreshingBarAppearLine(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            AnimatorSet noRefreshBackAnim = new AnimatorSet();
            noRefreshBackAnim.play(childViewYAnim).with(textBackAnim);
            noRefreshBackAnim.start();
        }
    }

    public HwSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public HwSwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mIsStartBackAnimating = false;
        init();
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mProgressBarY = getPaddingTop();
    }

    public void startFinishRefreshingAnim() {
        if (!this.mIsRefreshing || this.mIsStartBackAnimating) {
            Log.d(TAG, "startFinishRefreshingAnim, but not refreshing or mIsStartBackAnimating return");
            return;
        }
        if (!(this.mHeaderView == null || this.mRefreshingBar == null || this.mChildView == null)) {
            this.mHeaderView.setPullState(RefreshHeaderView.REFRESH_FINISH);
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
        if (this.mHeaderView != null) {
            this.mHeaderView.topShadowColors[0] = color;
            this.mHeaderView.invalidate();
        }
    }

    public void setScrollView(View scrollView) {
        this.mScrollChildView = scrollView;
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureChildView();
        if (this.mChildView == null || this.mHeaderView == null) {
            Log.w(TAG, "onMeasure view is null mChildView = " + this.mChildView + ", mHeaderView = " + this.mHeaderView);
            return;
        }
        this.mChildView.measure(View.MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), 1073741824), View.MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824));
        this.mHeaderView.measure(View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMaxPullDistance, 1073741824));
    }

    private void initMaxPullDistance() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Point size = new Point();
        Display display = ((WindowManager) getContext().getApplicationContext().getSystemService("window")).getDefaultDisplay();
        display.getSize(size);
        display.getMetrics(displayMetrics);
        int screenHeight = size.y;
        this.mMaxPullDistance = (int) (((float) screenHeight) * MAX_PULL_DISTANCE_FACTOR);
        this.mProgressBarAppearDistance = (int) (((float) screenHeight) * PROGRESS_BAR_APPEAR_DISTANCE_FACTOR);
        this.mLoadingStartDistance = (int) (((float) screenHeight) * LOADING_START_DISTANCE_FACTOR);
        this.mOffsetMaxPullDistance = getMoveY((float) this.mMaxPullDistance);
        this.mOffsetLoadingStartDistance = getMoveY((float) this.mLoadingStartDistance);
        this.mOffsetProgressBarAppearDistance = getMoveY((float) this.mProgressBarAppearDistance);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
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
        this.mRefreshingBar.layout((width / 2) - (this.mScaledProgressBarSize / 2), this.mProgressBarY, (width / 2) + (this.mScaledProgressBarSize / 2), this.mProgressBarY + this.mScaledProgressBarSize);
    }

    private boolean canChildScrollVertically() {
        if (this.mChildView == null && this.mScrollChildView == null) {
            return false;
        }
        if (this.mScrollChildView == null) {
            return this.mChildView.canScrollVertically(-1);
        }
        if (this.mScrollChildView instanceof AbsListView) {
            try {
                Method method = Class.forName("android.widget.AbsListView").getDeclaredMethod("canScrollUp", new Class[0]);
                method.setAccessible(true);
                return ((Boolean) method.invoke(this.mScrollChildView, new Object[0])).booleanValue();
            } catch (ClassNotFoundException e) {
                Log.d(TAG, "canChildScrollVertically() ClassNotFoundException");
            } catch (NoSuchMethodException e2) {
                Log.d(TAG, "canChildScrollVertically() NoSuchMethodException");
            } catch (SecurityException e3) {
                Log.d(TAG, "canChildScrollVertically() SecurityException");
            } catch (IllegalAccessException e4) {
                Log.d(TAG, "canChildScrollVertically() IllegalAccessException");
            } catch (IllegalArgumentException e5) {
                Log.d(TAG, "canChildScrollVertically() IllegalArgumentException");
            } catch (InvocationTargetException e6) {
                Log.d(TAG, "canChildScrollVertically() InvocationTargetException");
            }
        }
        return this.mScrollChildView.canScrollVertically(-1);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            this.startX = ev.getX();
            this.curPosX = this.startX;
            this.startY = ev.getY();
            this.curPosY = this.startY;
        }
        if (this.mIsRefreshing || !this.mCallback.supportSwipeToRefresh()) {
            return super.onInterceptTouchEvent(ev);
        }
        if (ev.getAction() == 2) {
            this.curPosX = ev.getX();
            this.curPosY = ev.getY();
            float dx = this.curPosX - this.startX;
            float dy = this.curPosY - this.startY;
            if ((Math.abs(dy) / Math.abs(dx) > DIRECTION_RATIO) && dy > ((float) this.mTouchSlop) && !canChildScrollVertically() && !this.mIsRefreshing) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float offsetY;
        if (this.mHeaderView == null || this.mChildView == null || this.mRefreshingBar == null) {
            Log.w(TAG, "onTouchEvent view is null mChildView = " + this.mChildView + ", mHeaderView = " + this.mHeaderView);
            return super.onTouchEvent(event);
        } else if (this.mIsRefreshing || !this.mCallback.supportSwipeToRefresh()) {
            return super.onTouchEvent(event);
        } else {
            switch (event.getAction()) {
                case 0:
                    this.mHeaderView.setPullState(RefreshHeaderView.PULL_DOWN);
                    return true;
                case 1:
                case 3:
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
                case 2:
                    this.curPosY = event.getY();
                    float dy = this.curPosY - this.startY;
                    float dy2 = 0.0f;
                    if (dy > 0.0f) {
                        dy2 = dy;
                    }
                    float offsetY2 = getMoveY(dy2);
                    if (offsetY2 > ((float) this.mTouchSlop)) {
                        offsetY = offsetY2 - ((float) this.mTouchSlop);
                    } else {
                        offsetY = 0.0f;
                    }
                    float offsetY3 = offsetY < this.mOffsetMaxPullDistance ? offsetY : this.mOffsetMaxPullDistance;
                    this.mChildView.setTranslationY(offsetY3);
                    this.mHeaderView.setDragheight(offsetY3);
                    this.mHeaderView.invalidate();
                    if (offsetY3 > this.mOffsetLoadingStartDistance) {
                        this.mRefreshingBar.setVisibility(0);
                        setRefreshingBarAnimLoadingLine(offsetY3);
                        this.mHeaderView.setPullState(RefreshHeaderView.CAN_UP_TO_REFRESH);
                        this.mHeaderView.setPullText(this.mCanRefreshText);
                    } else if (offsetY3 > this.mOffsetProgressBarAppearDistance) {
                        this.mRefreshingBar.setVisibility(0);
                        setRefreshingBarAppearLine(offsetY3);
                        this.mHeaderView.setPullState(RefreshHeaderView.PULL_DOWN);
                        this.mHeaderView.setPullText(this.mPullDownText);
                    } else {
                        this.mRefreshingBar.setVisibility(4);
                        this.mHeaderView.setPullState(RefreshHeaderView.PULL_DOWN);
                        this.mHeaderView.setPullText(this.mPullDownText);
                    }
                    return true;
                default:
                    return super.onTouchEvent(event);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setRefreshingBarAnimLoadingLine(float offsetY) {
        float progressBarScaleRatio = PROGRESS_BAR_NORMAL_SCALE + (PROGRESS_BAR_MAX_EXTRA_SCALE * ((offsetY - this.mOffsetLoadingStartDistance) / (this.mOffsetMaxPullDistance - this.mOffsetLoadingStartDistance)));
        this.mRefreshingBar.setScaleX(progressBarScaleRatio);
        this.mRefreshingBar.setScaleY(progressBarScaleRatio);
        this.mScaledProgressBarSize = (int) (((float) this.mProgressBarSize) * progressBarScaleRatio);
        this.mProgressBarY = (int) ((offsetY / 2.0f) - (((float) this.mScaledProgressBarSize) / 2.0f));
        this.mRefreshingBar.requestLayout();
    }

    /* access modifiers changed from: private */
    public void setRefreshingBarAppearLine(float offsetY) {
        float distanceRatio = (offsetY - this.mOffsetProgressBarAppearDistance) / (this.mOffsetLoadingStartDistance - this.mOffsetProgressBarAppearDistance);
        this.mRefreshingBar.setAlpha(distanceRatio);
        float progressBarScaleRatio = PROGRESS_BAR_APPEAR_SCALE + (PROGRESS_BAR_APPEAR_SCALE * distanceRatio);
        this.mRefreshingBar.setScaleX(progressBarScaleRatio);
        this.mRefreshingBar.setScaleY(progressBarScaleRatio);
        this.mScaledProgressBarSize = (int) (((float) this.mProgressBarSize) * progressBarScaleRatio);
        this.mProgressBarY = (int) ((offsetY / 2.0f) - (((float) this.mScaledProgressBarSize) / 2.0f));
        this.mRefreshingBar.requestLayout();
    }

    private void ensureChildView() {
        if (this.mChildView == null) {
            int sz = getChildCount();
            int i = 0;
            while (i < sz) {
                View child = getChildAt(i);
                if (child.equals(this.mHeaderView) || child.equals(this.mRefreshingBar)) {
                    i++;
                } else {
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
        this.mHeaderView = new RefreshHeaderView(this, getContext());
        addView(this.mHeaderView);
    }

    private float getMoveY(float s) {
        if (s > CHILD_VIEW_MOVE_FACTOR) {
            return (float) Math.sqrt((double) (CHILD_VIEW_MOVE_FACTOR * s));
        }
        return s;
    }

    private void addProgressBar() {
        this.mProgressBarSize = (int) (40.0f * getResources().getDisplayMetrics().scaledDensity);
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
}
