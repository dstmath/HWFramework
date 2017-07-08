package com.android.server.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.server.display.Utils;
import com.android.server.input.HwCircleAnimation;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsView;
import java.util.ArrayList;

public class SearchPanelCircleView extends FrameLayout {
    public static final Interpolator ALPHA_OUT = null;
    private static final String TAG = "SearchPanelCircleView";
    private boolean mAnimatingOut;
    private final Interpolator mAppearInterpolator;
    private final Paint mBackgroundPaint;
    private final int mBaseMargin;
    private float mCircleAnimationEndValue;
    private ValueAnimator mCircleAnimator;
    private boolean mCircleHidden;
    private final int mCircleMinSize;
    private final Rect mCircleRect;
    private float mCircleSize;
    private AnimatorUpdateListener mCircleUpdateListener;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private boolean mClipToOutline;
    private final Interpolator mDisappearInterpolator;
    private boolean mDraggedFarEnough;
    private ValueAnimator mFadeOutAnimator;
    private final Interpolator mFastOutSlowInInterpolator;
    private ImageView mLogo;
    private final int mMaxElevation;
    private float mOffset;
    private boolean mOffsetAnimatingIn;
    private ValueAnimator mOffsetAnimator;
    private AnimatorUpdateListener mOffsetUpdateListener;
    private float mOutlineAlpha;
    private final Paint mRipplePaint;
    private ArrayList<Ripple> mRipples;
    private final int mStaticOffset;
    private final Rect mStaticRect;

    /* renamed from: com.android.server.policy.SearchPanelCircleView.5 */
    class AnonymousClass5 extends AnimatorListenerAdapter {
        final /* synthetic */ Runnable val$endRunnable;

        AnonymousClass5(Runnable val$endRunnable) {
            this.val$endRunnable = val$endRunnable;
        }

        public void onAnimationEnd(Animator animation) {
            SearchPanelCircleView.this.mOffsetAnimator = null;
            if (this.val$endRunnable != null) {
                this.val$endRunnable.run();
            }
        }
    }

    /* renamed from: com.android.server.policy.SearchPanelCircleView.7 */
    class AnonymousClass7 extends AnimatorListenerAdapter {
        final /* synthetic */ Runnable val$endRunnable;

        AnonymousClass7(Runnable val$endRunnable) {
            this.val$endRunnable = val$endRunnable;
        }

        public void onAnimationEnd(Animator animation) {
            if (this.val$endRunnable != null) {
                this.val$endRunnable.run();
            }
            SearchPanelCircleView.this.mLogo.setAlpha(HwCircleAnimation.SMALL_ALPHA);
            SearchPanelCircleView.this.mBackgroundPaint.setAlpha(Utils.MAXINUM_TEMPERATURE);
            SearchPanelCircleView.this.mOutlineAlpha = HwCircleAnimation.SMALL_ALPHA;
            SearchPanelCircleView.this.mFadeOutAnimator = null;
        }
    }

    private static class MyAnimatorListenerAdapter extends AnimatorListenerAdapter {
        Runnable mRunnable;

        public MyAnimatorListenerAdapter(Runnable runnable) {
            this.mRunnable = runnable;
        }

        public void onAnimationEnd(Animator animation) {
            if (this.mRunnable != null) {
                this.mRunnable.run();
            }
        }
    }

    private class Ripple {
        float alpha;
        float endRadius;
        float radius;
        float x;
        float y;

        Ripple(float x, float y, float endRadius) {
            this.x = x;
            this.y = y;
            this.endRadius = endRadius;
        }

        void start() {
            ValueAnimator animator = ValueAnimator.ofFloat(new float[]{0.0f, HwCircleAnimation.SMALL_ALPHA});
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Ripple.this.alpha = HwCircleAnimation.SMALL_ALPHA - animation.getAnimatedFraction();
                    Ripple.this.alpha = SearchPanelCircleView.this.mDisappearInterpolator.getInterpolation(Ripple.this.alpha);
                    Ripple.this.radius = SearchPanelCircleView.this.mAppearInterpolator.getInterpolation(animation.getAnimatedFraction());
                    Ripple ripple = Ripple.this;
                    ripple.radius *= Ripple.this.endRadius;
                    SearchPanelCircleView.this.invalidate();
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    SearchPanelCircleView.this.mRipples.remove(Ripple.this);
                    SearchPanelCircleView.this.updateClipping();
                }

                public void onAnimationStart(Animator animation) {
                    SearchPanelCircleView.this.mRipples.add(Ripple.this);
                    SearchPanelCircleView.this.updateClipping();
                }
            });
            animator.setDuration(400);
            animator.start();
        }

        public void draw(Canvas canvas) {
            SearchPanelCircleView.this.mRipplePaint.setAlpha((int) (this.alpha * 255.0f));
            canvas.drawCircle(this.x, this.y, this.radius, SearchPanelCircleView.this.mRipplePaint);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.SearchPanelCircleView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.SearchPanelCircleView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.SearchPanelCircleView.<clinit>():void");
    }

    public SearchPanelCircleView(Context context) {
        this(context, null);
    }

    public SearchPanelCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchPanelCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SearchPanelCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBackgroundPaint = new Paint();
        this.mRipplePaint = new Paint();
        this.mCircleRect = new Rect();
        this.mStaticRect = new Rect();
        this.mRipples = new ArrayList();
        this.mCircleUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                SearchPanelCircleView.this.applyCircleSize(((Float) animation.getAnimatedValue()).floatValue());
                SearchPanelCircleView.this.updateElevation();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                SearchPanelCircleView.this.mCircleAnimator = null;
            }
        };
        this.mOffsetUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                SearchPanelCircleView.this.setOffset(((Float) animation.getAnimatedValue()).floatValue());
            }
        };
        setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                if (SearchPanelCircleView.this.mCircleSize > 0.0f) {
                    outline.setOval(SearchPanelCircleView.this.mCircleRect);
                } else {
                    outline.setEmpty();
                }
                outline.setAlpha(SearchPanelCircleView.this.mOutlineAlpha);
            }
        });
        setWillNotDraw(false);
        this.mCircleMinSize = context.getResources().getDimensionPixelSize(34472146);
        this.mBaseMargin = context.getResources().getDimensionPixelSize(34472147);
        this.mStaticOffset = context.getResources().getDimensionPixelSize(34472148);
        this.mMaxElevation = context.getResources().getDimensionPixelSize(34472149);
        this.mAppearInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563662);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563661);
        this.mDisappearInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563663);
        this.mBackgroundPaint.setAntiAlias(true);
        this.mBackgroundPaint.setColor(getResources().getColor(33882300));
        this.mRipplePaint.setColor(getResources().getColor(33882301));
        this.mRipplePaint.setAntiAlias(true);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawRipples(canvas);
    }

    private void drawRipples(Canvas canvas) {
        for (int i = 0; i < this.mRipples.size(); i++) {
            ((Ripple) this.mRipples.get(i)).draw(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawCircle((float) this.mCircleRect.centerX(), (float) this.mCircleRect.centerY(), this.mCircleSize / 2.0f, this.mBackgroundPaint);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLogo = (ImageView) findViewById(34603183);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mLogo.layout(0, 0, this.mLogo.getMeasuredWidth(), this.mLogo.getMeasuredHeight());
        if (changed) {
            updateCircleRect(this.mStaticRect, (float) this.mStaticOffset, true);
        }
    }

    public void setCircleSize(float circleSize) {
        setCircleSize(circleSize, false, null, 0, null);
    }

    public void setCircleSize(float circleSize, boolean animated, Runnable endRunnable, int startDelay, Interpolator interpolator) {
        boolean isAnimating = this.mCircleAnimator != null;
        boolean animationPending = isAnimating && !this.mCircleAnimator.isRunning();
        boolean animatingOut = isAnimating && this.mCircleAnimationEndValue == 0.0f;
        if (animated || animationPending || animatingOut) {
            Interpolator desiredInterpolator;
            if (isAnimating) {
                this.mCircleAnimator.cancel();
            }
            this.mCircleAnimator = ValueAnimator.ofFloat(new float[]{this.mCircleSize, circleSize});
            this.mCircleAnimator.addUpdateListener(this.mCircleUpdateListener);
            this.mCircleAnimator.addListener(this.mClearAnimatorListener);
            this.mCircleAnimator.addListener(new MyAnimatorListenerAdapter(endRunnable));
            if (interpolator != null) {
                desiredInterpolator = interpolator;
            } else {
                desiredInterpolator = this.mDisappearInterpolator;
            }
            this.mCircleAnimator.setInterpolator(desiredInterpolator);
            this.mCircleAnimator.setDuration(300);
            this.mCircleAnimator.setStartDelay((long) startDelay);
            this.mCircleAnimator.start();
            this.mCircleAnimationEndValue = circleSize;
        } else if (isAnimating) {
            float diff = circleSize - this.mCircleAnimationEndValue;
            this.mCircleAnimator.getValues()[0].setFloatValues(new float[]{diff, circleSize});
            this.mCircleAnimator.setCurrentPlayTime(this.mCircleAnimator.getCurrentPlayTime());
            this.mCircleAnimationEndValue = circleSize;
        } else {
            applyCircleSize(circleSize);
            updateElevation();
        }
    }

    private void applyCircleSize(float circleSize) {
        this.mCircleSize = circleSize;
        updateLayout();
    }

    private void updateElevation() {
        setElevation((HwCircleAnimation.SMALL_ALPHA - Math.max((((float) this.mStaticOffset) - this.mOffset) / ((float) this.mStaticOffset), 0.0f)) * ((float) this.mMaxElevation));
    }

    public void setOffset(float offset) {
        setOffset(offset, false, 0, null, null);
    }

    private void setOffset(float offset, boolean animate, int startDelay, Interpolator interpolator, Runnable endRunnable) {
        boolean z = true;
        if (animate) {
            if (this.mOffsetAnimator != null) {
                this.mOffsetAnimator.removeAllListeners();
                this.mOffsetAnimator.cancel();
            }
            this.mOffsetAnimator = ValueAnimator.ofFloat(new float[]{this.mOffset, offset});
            this.mOffsetAnimator.addUpdateListener(this.mOffsetUpdateListener);
            this.mOffsetAnimator.addListener(new AnonymousClass5(endRunnable));
            this.mOffsetAnimator.setInterpolator(interpolator != null ? interpolator : this.mDisappearInterpolator);
            this.mOffsetAnimator.setStartDelay((long) startDelay);
            this.mOffsetAnimator.setDuration(300);
            this.mOffsetAnimator.start();
            if (offset == 0.0f) {
                z = false;
            }
            this.mOffsetAnimatingIn = z;
            return;
        }
        this.mOffset = offset;
        updateLayout();
        if (endRunnable != null) {
            endRunnable.run();
        }
    }

    private void updateLayout() {
        updateCircleRect();
        updateLogo();
        invalidateOutline();
        invalidate();
        updateClipping();
    }

    private void updateClipping() {
        boolean clip = this.mCircleSize < ((float) this.mCircleMinSize) || !this.mRipples.isEmpty();
        if (clip != this.mClipToOutline) {
            setClipToOutline(clip);
            this.mClipToOutline = clip;
        }
    }

    private void updateLogo() {
        boolean exitAnimationRunning = this.mFadeOutAnimator != null;
        Rect rect = exitAnimationRunning ? this.mCircleRect : this.mStaticRect;
        float translationX = (((float) (rect.left + rect.right)) / 2.0f) - (((float) this.mLogo.getWidth()) / 2.0f);
        float translationY = (((float) (rect.top + rect.bottom)) / 2.0f) - (((float) this.mLogo.getHeight()) / 2.0f);
        float t = (((float) this.mStaticOffset) - this.mOffset) / ((float) this.mStaticOffset);
        if (exitAnimationRunning) {
            translationY += (this.mOffset - ((float) this.mStaticOffset)) / 2.0f;
        } else {
            if (isLand()) {
                translationX += (((float) this.mStaticOffset) * t) * 0.3f;
            } else {
                translationY += (((float) this.mStaticOffset) * t) * 0.3f;
            }
            this.mLogo.setAlpha(Math.max(((HwCircleAnimation.SMALL_ALPHA - t) - WifiProCommonUtils.RECOVERY_PERCENTAGE) * 2.0f, 0.0f));
        }
        this.mLogo.setTranslationX(translationX);
        this.mLogo.setTranslationY(translationY);
    }

    private void updateCircleRect() {
        updateCircleRect(this.mCircleRect, this.mOffset, false);
    }

    private void updateCircleRect(Rect rect, float offset, boolean useStaticSize) {
        int left;
        int top;
        float circleSize = useStaticSize ? (float) this.mCircleMinSize : this.mCircleSize;
        if (isLand()) {
            left = (int) (((((float) getWidth()) - (circleSize / 2.0f)) - ((float) this.mBaseMargin)) - offset);
            top = (int) ((((float) getHeight()) - circleSize) / 2.0f);
        } else {
            left = ((int) (((float) getWidth()) - circleSize)) / 2;
            top = (int) (((((float) getHeight()) - (circleSize / 2.0f)) - ((float) this.mBaseMargin)) - offset);
        }
        rect.set(left, top, (int) (((float) left) + circleSize), (int) (((float) top) + circleSize));
    }

    public void setHorizontal(boolean horizontal) {
        updateCircleRect(this.mStaticRect, (float) this.mStaticOffset, true);
        updateLayout();
    }

    public void setDragDistance(float distance) {
        if (!this.mAnimatingOut) {
            if (!this.mCircleHidden || this.mDraggedFarEnough) {
                setCircleSize(((float) this.mCircleMinSize) + rubberband(distance));
            }
        }
    }

    private float rubberband(float diff) {
        return (float) Math.pow((double) Math.abs(diff), 0.6000000238418579d);
    }

    public void startAbortAnimation(Runnable endRunnable) {
        if (this.mAnimatingOut) {
            if (endRunnable != null) {
                endRunnable.run();
            }
            return;
        }
        setCircleSize(0.0f, true, null, 0, null);
        setOffset(0.0f, true, 0, null, endRunnable);
        animate().alpha(0.0f).setDuration(300).setStartDelay(0).setInterpolator(ALPHA_OUT).start();
        this.mCircleHidden = true;
    }

    public void startEnterAnimation() {
        if (!this.mAnimatingOut) {
            setAlpha(HwCircleAnimation.SMALL_ALPHA);
            applyCircleSize(0.0f);
            setOffset(0.0f);
            setCircleSize((float) this.mCircleMinSize, true, null, 50, null);
            setOffset((float) this.mStaticOffset, true, 50, null, null);
            this.mCircleHidden = false;
        }
    }

    public void startExitAnimation(Runnable endRunnable) {
        if (isLand()) {
            endRunnable.run();
            return;
        }
        setOffset((((float) getHeight()) / 2.0f) - ((float) this.mBaseMargin), true, 50, this.mFastOutSlowInInterpolator, null);
        setCircleSize((float) Math.ceil(Math.hypot((double) (((float) getWidth()) / 2.0f), (double) (((float) getHeight()) / 2.0f)) * 2.0d), true, null, 50, this.mFastOutSlowInInterpolator);
        performExitFadeOutAnimation(50, HwGlobalActionsView.VIBRATE_DELAY, endRunnable);
    }

    private void performExitFadeOutAnimation(int startDelay, int duration, Runnable endRunnable) {
        this.mFadeOutAnimator = ValueAnimator.ofFloat(new float[]{((float) this.mBackgroundPaint.getAlpha()) / 255.0f, 0.0f});
        this.mFadeOutAnimator.setInterpolator(new LinearInterpolator());
        this.mFadeOutAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float backgroundValue;
                float animatedFraction = animation.getAnimatedFraction();
                float logoValue = SearchPanelCircleView.ALPHA_OUT.getInterpolation(HwCircleAnimation.SMALL_ALPHA - (animatedFraction > WifiProCommonUtils.RECOVERY_PERCENTAGE ? HwCircleAnimation.SMALL_ALPHA : animatedFraction / WifiProCommonUtils.RECOVERY_PERCENTAGE));
                if (animatedFraction < HwCircleAnimation.BG_ALPHA_FILL) {
                    backgroundValue = 0.0f;
                } else {
                    backgroundValue = SearchPanelCircleView.ALPHA_OUT.getInterpolation((animatedFraction - HwCircleAnimation.BG_ALPHA_FILL) / 0.8f);
                }
                backgroundValue = HwCircleAnimation.SMALL_ALPHA - backgroundValue;
                SearchPanelCircleView.this.mBackgroundPaint.setAlpha((int) (255.0f * backgroundValue));
                SearchPanelCircleView.this.mOutlineAlpha = backgroundValue;
                SearchPanelCircleView.this.mLogo.setAlpha(logoValue);
                SearchPanelCircleView.this.invalidateOutline();
                SearchPanelCircleView.this.invalidate();
            }
        });
        this.mFadeOutAnimator.addListener(new AnonymousClass7(endRunnable));
        this.mFadeOutAnimator.setStartDelay((long) startDelay);
        this.mFadeOutAnimator.setDuration((long) duration);
        this.mFadeOutAnimator.start();
    }

    public void setDraggedFarEnough(boolean farEnough) {
        if (farEnough != this.mDraggedFarEnough) {
            if (farEnough) {
                if (this.mCircleHidden) {
                    startEnterAnimation();
                }
                if (this.mOffsetAnimator == null) {
                    addRipple();
                } else {
                    postDelayed(new Runnable() {
                        public void run() {
                            SearchPanelCircleView.this.addRipple();
                        }
                    }, 100);
                }
            } else {
                startAbortAnimation(null);
            }
            this.mDraggedFarEnough = farEnough;
        }
    }

    private void addRipple() {
        if (this.mRipples.size() <= 1) {
            float xInterpolation;
            float yInterpolation;
            if (isLand()) {
                xInterpolation = SlideTouchEvent.SCALE;
                yInterpolation = WifiProCommonUtils.RECOVERY_PERCENTAGE;
            } else {
                xInterpolation = WifiProCommonUtils.RECOVERY_PERCENTAGE;
                yInterpolation = SlideTouchEvent.SCALE;
            }
            new Ripple((((float) this.mStaticRect.left) * (HwCircleAnimation.SMALL_ALPHA - xInterpolation)) + (((float) this.mStaticRect.right) * xInterpolation), (((float) this.mStaticRect.top) * (HwCircleAnimation.SMALL_ALPHA - yInterpolation)) + (((float) this.mStaticRect.bottom) * yInterpolation), Math.max(this.mCircleSize, ((float) this.mCircleMinSize) * 1.25f) * SlideTouchEvent.SCALE).start();
        }
    }

    public void reset() {
        this.mDraggedFarEnough = false;
        this.mAnimatingOut = false;
        this.mCircleHidden = true;
        this.mClipToOutline = false;
        if (this.mFadeOutAnimator != null) {
            this.mFadeOutAnimator.cancel();
        }
        this.mBackgroundPaint.setAlpha(Utils.MAXINUM_TEMPERATURE);
        this.mOutlineAlpha = HwCircleAnimation.SMALL_ALPHA;
    }

    public boolean isAnimationRunning(boolean enterAnimation) {
        return this.mOffsetAnimator != null && enterAnimation == this.mOffsetAnimatingIn;
    }

    public void performOnAnimationFinished(Runnable runnable) {
        if (this.mOffsetAnimator != null) {
            this.mOffsetAnimator.addListener(new MyAnimatorListenerAdapter(runnable));
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void setAnimatingOut(boolean animatingOut) {
        this.mAnimatingOut = animatingOut;
    }

    public boolean isAnimatingOut() {
        return this.mAnimatingOut;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isLand() {
        return 2 == getResources().getConfiguration().orientation;
    }
}
