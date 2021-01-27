package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.R;

public class SlidingTab extends ViewGroup {
    private static final int ANIM_DURATION = 250;
    private static final int ANIM_TARGET_TIME = 500;
    private static final boolean DBG = false;
    private static final int HORIZONTAL = 0;
    private static final String LOG_TAG = "SlidingTab";
    private static final float THRESHOLD = 0.6666667f;
    private static final int TRACKING_MARGIN = 50;
    private static final int VERTICAL = 1;
    private static final long VIBRATE_LONG = 40;
    private static final long VIBRATE_SHORT = 30;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private boolean mAnimating;
    @UnsupportedAppUsage
    private final Animation.AnimationListener mAnimationDoneListener;
    private Slider mCurrentSlider;
    private final float mDensity;
    private int mGrabbedState;
    private boolean mHoldLeftOnTransition;
    private boolean mHoldRightOnTransition;
    @UnsupportedAppUsage
    private final Slider mLeftSlider;
    private OnTriggerListener mOnTriggerListener;
    private final int mOrientation;
    private Slider mOtherSlider;
    @UnsupportedAppUsage
    private final Slider mRightSlider;
    private float mThreshold;
    private final Rect mTmpRect;
    private boolean mTracking;
    private boolean mTriggered;
    private Vibrator mVibrator;

    public interface OnTriggerListener {
        public static final int LEFT_HANDLE = 1;
        public static final int NO_HANDLE = 0;
        public static final int RIGHT_HANDLE = 2;

        void onGrabbedStateChange(View view, int i);

        void onTrigger(View view, int i);
    }

    /* access modifiers changed from: private */
    public static class Slider {
        public static final int ALIGN_BOTTOM = 3;
        public static final int ALIGN_LEFT = 0;
        public static final int ALIGN_RIGHT = 1;
        public static final int ALIGN_TOP = 2;
        public static final int ALIGN_UNKNOWN = 4;
        private static final int STATE_ACTIVE = 2;
        private static final int STATE_NORMAL = 0;
        private static final int STATE_PRESSED = 1;
        private int alignment = 4;
        private int alignment_value;
        private int currentState = 0;
        @UnsupportedAppUsage
        private final ImageView tab;
        private final ImageView target;
        @UnsupportedAppUsage
        private final TextView text;

        Slider(ViewGroup parent, int tabId, int barId, int targetId) {
            this.tab = new ImageView(parent.getContext());
            this.tab.setBackgroundResource(tabId);
            this.tab.setScaleType(ImageView.ScaleType.CENTER);
            this.tab.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            this.text = new TextView(parent.getContext());
            this.text.setLayoutParams(new ViewGroup.LayoutParams(-2, -1));
            this.text.setBackgroundResource(barId);
            this.text.setTextAppearance(parent.getContext(), R.style.TextAppearance_SlidingTabNormal);
            this.target = new ImageView(parent.getContext());
            this.target.setImageResource(targetId);
            this.target.setScaleType(ImageView.ScaleType.CENTER);
            this.target.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            this.target.setVisibility(4);
            parent.addView(this.target);
            parent.addView(this.tab);
            parent.addView(this.text);
        }

        /* access modifiers changed from: package-private */
        public void setIcon(int iconId) {
            this.tab.setImageResource(iconId);
        }

        /* access modifiers changed from: package-private */
        public void setTabBackgroundResource(int tabId) {
            this.tab.setBackgroundResource(tabId);
        }

        /* access modifiers changed from: package-private */
        public void setBarBackgroundResource(int barId) {
            this.text.setBackgroundResource(barId);
        }

        /* access modifiers changed from: package-private */
        public void setHintText(int resId) {
            this.text.setText(resId);
        }

        /* access modifiers changed from: package-private */
        public void hide() {
            int dx;
            int i = this.alignment;
            int dy = 0;
            boolean horiz = i == 0 || i == 1;
            if (horiz) {
                dx = this.alignment == 0 ? this.alignment_value - this.tab.getRight() : this.alignment_value - this.tab.getLeft();
            } else {
                dx = 0;
            }
            if (!horiz) {
                if (this.alignment == 2) {
                    dy = this.alignment_value - this.tab.getBottom();
                } else {
                    dy = this.alignment_value - this.tab.getTop();
                }
            }
            Animation trans = new TranslateAnimation(0.0f, (float) dx, 0.0f, (float) dy);
            trans.setDuration(250);
            trans.setFillAfter(true);
            this.tab.startAnimation(trans);
            this.text.startAnimation(trans);
            this.target.setVisibility(4);
        }

        /* access modifiers changed from: package-private */
        public void show(boolean animate) {
            int dy = 0;
            this.text.setVisibility(0);
            this.tab.setVisibility(0);
            if (animate) {
                int i = this.alignment;
                boolean horiz = true;
                if (!(i == 0 || i == 1)) {
                    horiz = false;
                }
                int dx = horiz ? this.alignment == 0 ? this.tab.getWidth() : -this.tab.getWidth() : 0;
                if (!horiz) {
                    dy = this.alignment == 2 ? this.tab.getHeight() : -this.tab.getHeight();
                }
                Animation trans = new TranslateAnimation((float) (-dx), 0.0f, (float) (-dy), 0.0f);
                trans.setDuration(250);
                this.tab.startAnimation(trans);
                this.text.startAnimation(trans);
            }
        }

        /* access modifiers changed from: package-private */
        public void setState(int state) {
            this.text.setPressed(state == 1);
            this.tab.setPressed(state == 1);
            if (state == 2) {
                int[] activeState = {16842914};
                if (this.text.getBackground().isStateful()) {
                    this.text.getBackground().setState(activeState);
                }
                if (this.tab.getBackground().isStateful()) {
                    this.tab.getBackground().setState(activeState);
                }
                TextView textView = this.text;
                textView.setTextAppearance(textView.getContext(), R.style.TextAppearance_SlidingTabActive);
            } else {
                TextView textView2 = this.text;
                textView2.setTextAppearance(textView2.getContext(), R.style.TextAppearance_SlidingTabNormal);
            }
            this.currentState = state;
        }

        /* access modifiers changed from: package-private */
        public void showTarget() {
            AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
            alphaAnim.setDuration(500);
            this.target.startAnimation(alphaAnim);
            this.target.setVisibility(0);
        }

        /* access modifiers changed from: package-private */
        public void reset(boolean animate) {
            int dx;
            setState(0);
            this.text.setVisibility(0);
            TextView textView = this.text;
            textView.setTextAppearance(textView.getContext(), R.style.TextAppearance_SlidingTabNormal);
            this.tab.setVisibility(0);
            this.target.setVisibility(4);
            int i = this.alignment;
            boolean horiz = true;
            if (!(i == 0 || i == 1)) {
                horiz = false;
            }
            if (horiz) {
                dx = this.alignment == 0 ? this.alignment_value - this.tab.getLeft() : this.alignment_value - this.tab.getRight();
            } else {
                dx = 0;
            }
            int dy = horiz ? 0 : this.alignment == 2 ? this.alignment_value - this.tab.getTop() : this.alignment_value - this.tab.getBottom();
            if (animate) {
                TranslateAnimation trans = new TranslateAnimation(0.0f, (float) dx, 0.0f, (float) dy);
                trans.setDuration(250);
                trans.setFillAfter(false);
                this.text.startAnimation(trans);
                this.tab.startAnimation(trans);
                return;
            }
            if (horiz) {
                this.text.offsetLeftAndRight(dx);
                this.tab.offsetLeftAndRight(dx);
            } else {
                this.text.offsetTopAndBottom(dy);
                this.tab.offsetTopAndBottom(dy);
            }
            this.text.clearAnimation();
            this.tab.clearAnimation();
            this.target.clearAnimation();
        }

        /* access modifiers changed from: package-private */
        public void setTarget(int targetId) {
            this.target.setImageResource(targetId);
        }

        /* JADX INFO: Multiple debug info for r9v2 int: [D('targetDrawable' android.graphics.drawable.Drawable), D('left' int)] */
        /* JADX INFO: Multiple debug info for r15v8 int: [D('rightTarget' int), D('targetRight' int)] */
        /* access modifiers changed from: package-private */
        public void layout(int l, int t, int r, int b, int alignment2) {
            int handleWidth;
            int rightTarget;
            int parentWidth;
            int leftTarget;
            int targetWidth;
            this.alignment = alignment2;
            Drawable tabBackground = this.tab.getBackground();
            int handleWidth2 = tabBackground.getIntrinsicWidth();
            int handleHeight = tabBackground.getIntrinsicHeight();
            Drawable targetDrawable = this.target.getDrawable();
            int targetWidth2 = targetDrawable.getIntrinsicWidth();
            int targetHeight = targetDrawable.getIntrinsicHeight();
            int parentWidth2 = r - l;
            int parentHeight = b - t;
            int leftTarget2 = (((int) (((float) parentWidth2) * SlidingTab.THRESHOLD)) - targetWidth2) + (handleWidth2 / 2);
            int rightTarget2 = ((int) (((float) parentWidth2) * 0.3333333f)) - (handleWidth2 / 2);
            int left = (parentWidth2 - handleWidth2) / 2;
            int right = left + handleWidth2;
            if (alignment2 == 0) {
                handleWidth = handleWidth2;
                targetWidth = targetWidth2;
                parentWidth = parentWidth2;
                leftTarget = leftTarget2;
                rightTarget = rightTarget2;
            } else if (alignment2 == 1) {
                handleWidth = handleWidth2;
                targetWidth = targetWidth2;
                parentWidth = parentWidth2;
                leftTarget = leftTarget2;
                rightTarget = rightTarget2;
            } else {
                int targetLeft = (parentWidth2 - targetWidth2) / 2;
                int targetRight = (parentWidth2 + targetWidth2) / 2;
                int top = (((int) (((float) parentHeight) * SlidingTab.THRESHOLD)) + (handleHeight / 2)) - targetHeight;
                int bottom = ((int) (((float) parentHeight) * 0.3333333f)) - (handleHeight / 2);
                if (alignment2 == 2) {
                    this.tab.layout(left, 0, right, handleHeight);
                    this.text.layout(left, 0 - parentHeight, right, 0);
                    this.target.layout(targetLeft, top, targetRight, top + targetHeight);
                    this.alignment_value = t;
                    return;
                }
                this.tab.layout(left, parentHeight - handleHeight, right, parentHeight);
                this.text.layout(left, parentHeight, right, parentHeight + parentHeight);
                this.target.layout(targetLeft, bottom, targetRight, bottom + targetHeight);
                this.alignment_value = b;
                return;
            }
            int targetTop = (parentHeight - targetHeight) / 2;
            int targetBottom = targetTop + targetHeight;
            int top2 = (parentHeight - handleHeight) / 2;
            int bottom2 = (parentHeight + handleHeight) / 2;
            if (alignment2 == 0) {
                this.tab.layout(0, top2, handleWidth, bottom2);
                this.text.layout(0 - parentWidth, top2, 0, bottom2);
                this.text.setGravity(5);
                this.target.layout(leftTarget, targetTop, leftTarget + targetWidth, targetBottom);
                this.alignment_value = l;
                return;
            }
            this.tab.layout(parentWidth - handleWidth, top2, parentWidth, bottom2);
            this.text.layout(parentWidth, top2, parentWidth + parentWidth, bottom2);
            this.target.layout(rightTarget, targetTop, rightTarget + targetWidth, targetBottom);
            this.text.setGravity(48);
            this.alignment_value = r;
        }

        public void updateDrawableStates() {
            setState(this.currentState);
        }

        public void measure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            int height = View.MeasureSpec.getSize(heightMeasureSpec);
            this.tab.measure(View.MeasureSpec.makeSafeMeasureSpec(width, 0), View.MeasureSpec.makeSafeMeasureSpec(height, 0));
            this.text.measure(View.MeasureSpec.makeSafeMeasureSpec(width, 0), View.MeasureSpec.makeSafeMeasureSpec(height, 0));
        }

        public int getTabWidth() {
            return this.tab.getMeasuredWidth();
        }

        public int getTabHeight() {
            return this.tab.getMeasuredHeight();
        }

        public void startAnimation(Animation anim1, Animation anim2) {
            this.tab.startAnimation(anim1);
            this.text.startAnimation(anim2);
        }

        public void hideTarget() {
            this.target.clearAnimation();
            this.target.setVisibility(4);
        }
    }

    public SlidingTab(Context context) {
        this(context, null);
    }

    public SlidingTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHoldLeftOnTransition = true;
        this.mHoldRightOnTransition = true;
        this.mGrabbedState = 0;
        this.mTriggered = false;
        this.mAnimationDoneListener = new Animation.AnimationListener() {
            /* class com.android.internal.widget.SlidingTab.AnonymousClass1 */

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                SlidingTab.this.onAnimationDone();
            }
        };
        this.mTmpRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTab);
        this.mOrientation = a.getInt(0, 0);
        a.recycle();
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mLeftSlider = new Slider(this, R.drawable.jog_tab_left_generic, R.drawable.jog_tab_bar_left_generic, R.drawable.jog_tab_target_gray);
        this.mRightSlider = new Slider(this, R.drawable.jog_tab_right_generic, R.drawable.jog_tab_bar_right_generic, R.drawable.jog_tab_target_gray);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height;
        int width;
        View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
        View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
        this.mLeftSlider.measure(widthMeasureSpec, heightMeasureSpec);
        this.mRightSlider.measure(widthMeasureSpec, heightMeasureSpec);
        int leftTabWidth = this.mLeftSlider.getTabWidth();
        int rightTabWidth = this.mRightSlider.getTabWidth();
        int leftTabHeight = this.mLeftSlider.getTabHeight();
        int rightTabHeight = this.mRightSlider.getTabHeight();
        if (isHorizontal()) {
            width = Math.max(widthSpecSize, leftTabWidth + rightTabWidth);
            height = Math.max(leftTabHeight, rightTabHeight);
        } else {
            width = Math.max(leftTabWidth, rightTabHeight);
            height = Math.max(heightSpecSize, leftTabHeight + rightTabHeight);
        }
        setMeasuredDimension(width, height);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        if (this.mAnimating) {
            return false;
        }
        this.mLeftSlider.tab.getHitRect(this.mTmpRect);
        boolean leftHit = this.mTmpRect.contains((int) x, (int) y);
        this.mRightSlider.tab.getHitRect(this.mTmpRect);
        boolean rightHit = this.mTmpRect.contains((int) x, (int) y);
        if (!this.mTracking && !leftHit && !rightHit) {
            return false;
        }
        if (action == 0) {
            this.mTracking = true;
            this.mTriggered = false;
            vibrate(VIBRATE_SHORT);
            float f = 0.3333333f;
            if (leftHit) {
                this.mCurrentSlider = this.mLeftSlider;
                this.mOtherSlider = this.mRightSlider;
                if (isHorizontal()) {
                    f = 0.6666667f;
                }
                this.mThreshold = f;
                setGrabbedState(1);
            } else {
                this.mCurrentSlider = this.mRightSlider;
                this.mOtherSlider = this.mLeftSlider;
                if (!isHorizontal()) {
                    f = 0.6666667f;
                }
                this.mThreshold = f;
                setGrabbedState(2);
            }
            this.mCurrentSlider.setState(1);
            this.mCurrentSlider.showTarget();
            this.mOtherSlider.hide();
        }
        return true;
    }

    public void reset(boolean animate) {
        this.mLeftSlider.reset(animate);
        this.mRightSlider.reset(animate);
        if (!animate) {
            this.mAnimating = false;
        }
    }

    @Override // android.view.View
    public void setVisibility(int visibility) {
        if (visibility != getVisibility() && visibility == 4) {
            reset(false);
        }
        super.setVisibility(visibility);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0018, code lost:
        if (r0 != 3) goto L_0x009d;
     */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        boolean thresholdReached;
        if (this.mTracking) {
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            if (action != 1) {
                int i = 2;
                if (action == 2) {
                    if (withinView(x, y, this)) {
                        moveHandle(x, y);
                        float position = isHorizontal() ? x : y;
                        float target = this.mThreshold * ((float) (isHorizontal() ? getWidth() : getHeight()));
                        if (isHorizontal()) {
                            thresholdReached = this.mCurrentSlider == this.mLeftSlider ? false : false;
                            thresholdReached = true;
                        } else {
                            thresholdReached = this.mCurrentSlider == this.mLeftSlider ? false : false;
                            thresholdReached = true;
                        }
                        if (!this.mTriggered && thresholdReached) {
                            this.mTriggered = true;
                            this.mTracking = false;
                            this.mCurrentSlider.setState(2);
                            boolean isLeft = this.mCurrentSlider == this.mLeftSlider;
                            if (isLeft) {
                                i = 1;
                            }
                            dispatchTriggerEvent(i);
                            startAnimating(isLeft ? this.mHoldLeftOnTransition : this.mHoldRightOnTransition);
                            setGrabbedState(0);
                        }
                    }
                }
            }
            cancelGrab();
        }
        return this.mTracking || super.onTouchEvent(event);
    }

    private void cancelGrab() {
        this.mTracking = false;
        this.mTriggered = false;
        this.mOtherSlider.show(true);
        this.mCurrentSlider.reset(false);
        this.mCurrentSlider.hideTarget();
        this.mCurrentSlider = null;
        this.mOtherSlider = null;
        setGrabbedState(0);
    }

    /* access modifiers changed from: package-private */
    public void startAnimating(final boolean holdAfter) {
        final int dx;
        final int right;
        int i;
        this.mAnimating = true;
        Slider slider = this.mCurrentSlider;
        Slider slider2 = this.mOtherSlider;
        int holdOffset = 0;
        if (isHorizontal()) {
            int right2 = slider.tab.getRight();
            int width = slider.tab.getWidth();
            int left = slider.tab.getLeft();
            int viewWidth = getWidth();
            if (!holdAfter) {
                holdOffset = width;
            }
            if (slider == this.mRightSlider) {
                dx = -((right2 + viewWidth) - holdOffset);
            } else {
                dx = ((viewWidth - left) + viewWidth) - holdOffset;
            }
            right = 0;
        } else {
            int top = slider.tab.getTop();
            int bottom = slider.tab.getBottom();
            int height = slider.tab.getHeight();
            int viewHeight = getHeight();
            if (!holdAfter) {
                holdOffset = height;
            }
            dx = 0;
            if (slider == this.mRightSlider) {
                i = (top + viewHeight) - holdOffset;
            } else {
                i = -(((viewHeight - bottom) + viewHeight) - holdOffset);
            }
            right = i;
        }
        Animation trans1 = new TranslateAnimation(0.0f, (float) dx, 0.0f, (float) right);
        trans1.setDuration(250);
        trans1.setInterpolator(new LinearInterpolator());
        trans1.setFillAfter(true);
        Animation trans2 = new TranslateAnimation(0.0f, (float) dx, 0.0f, (float) right);
        trans2.setDuration(250);
        trans2.setInterpolator(new LinearInterpolator());
        trans2.setFillAfter(true);
        trans1.setAnimationListener(new Animation.AnimationListener() {
            /* class com.android.internal.widget.SlidingTab.AnonymousClass2 */

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                Animation anim;
                if (holdAfter) {
                    int i = dx;
                    int i2 = right;
                    anim = new TranslateAnimation((float) i, (float) i, (float) i2, (float) i2);
                    anim.setDuration(1000);
                    SlidingTab.this.mAnimating = false;
                } else {
                    anim = new AlphaAnimation(0.5f, 1.0f);
                    anim.setDuration(250);
                    SlidingTab.this.resetView();
                }
                anim.setAnimationListener(SlidingTab.this.mAnimationDoneListener);
                SlidingTab.this.mLeftSlider.startAnimation(anim, anim);
                SlidingTab.this.mRightSlider.startAnimation(anim, anim);
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }
        });
        slider.hideTarget();
        slider.startAnimation(trans1, trans2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void onAnimationDone() {
        resetView();
        this.mAnimating = false;
    }

    private boolean withinView(float x, float y, View view) {
        return (isHorizontal() && y > -50.0f && y < ((float) (view.getHeight() + 50))) || (!isHorizontal() && x > -50.0f && x < ((float) (view.getWidth() + 50)));
    }

    private boolean isHorizontal() {
        return this.mOrientation == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void resetView() {
        this.mLeftSlider.reset(false);
        this.mRightSlider.reset(false);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            this.mLeftSlider.layout(l, t, r, b, isHorizontal() ? 0 : 3);
            this.mRightSlider.layout(l, t, r, b, isHorizontal() ? 1 : 2);
        }
    }

    private void moveHandle(float x, float y) {
        View handle = this.mCurrentSlider.tab;
        View content = this.mCurrentSlider.text;
        if (isHorizontal()) {
            int deltaX = (((int) x) - handle.getLeft()) - (handle.getWidth() / 2);
            handle.offsetLeftAndRight(deltaX);
            content.offsetLeftAndRight(deltaX);
        } else {
            int deltaY = (((int) y) - handle.getTop()) - (handle.getHeight() / 2);
            handle.offsetTopAndBottom(deltaY);
            content.offsetTopAndBottom(deltaY);
        }
        invalidate();
    }

    @UnsupportedAppUsage
    public void setLeftTabResources(int iconId, int targetId, int barId, int tabId) {
        this.mLeftSlider.setIcon(iconId);
        this.mLeftSlider.setTarget(targetId);
        this.mLeftSlider.setBarBackgroundResource(barId);
        this.mLeftSlider.setTabBackgroundResource(tabId);
        this.mLeftSlider.updateDrawableStates();
    }

    @UnsupportedAppUsage
    public void setLeftHintText(int resId) {
        if (isHorizontal()) {
            this.mLeftSlider.setHintText(resId);
        }
    }

    @UnsupportedAppUsage
    public void setRightTabResources(int iconId, int targetId, int barId, int tabId) {
        this.mRightSlider.setIcon(iconId);
        this.mRightSlider.setTarget(targetId);
        this.mRightSlider.setBarBackgroundResource(barId);
        this.mRightSlider.setTabBackgroundResource(tabId);
        this.mRightSlider.updateDrawableStates();
    }

    @UnsupportedAppUsage
    public void setRightHintText(int resId) {
        if (isHorizontal()) {
            this.mRightSlider.setHintText(resId);
        }
    }

    @UnsupportedAppUsage
    public void setHoldAfterTrigger(boolean holdLeft, boolean holdRight) {
        this.mHoldLeftOnTransition = holdLeft;
        this.mHoldRightOnTransition = holdRight;
    }

    private synchronized void vibrate(long duration) {
        boolean hapticEnabled = true;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1, -2) == 0) {
            hapticEnabled = false;
        }
        if (hapticEnabled) {
            if (this.mVibrator == null) {
                this.mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            }
            this.mVibrator.vibrate(duration, VIBRATION_ATTRIBUTES);
        }
    }

    @UnsupportedAppUsage
    public void setOnTriggerListener(OnTriggerListener listener) {
        this.mOnTriggerListener = listener;
    }

    private void dispatchTriggerEvent(int whichHandle) {
        vibrate(VIBRATE_LONG);
        OnTriggerListener onTriggerListener = this.mOnTriggerListener;
        if (onTriggerListener != null) {
            onTriggerListener.onTrigger(this, whichHandle);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && visibility != 0 && this.mGrabbedState != 0) {
            cancelGrab();
        }
    }

    private void setGrabbedState(int newState) {
        if (newState != this.mGrabbedState) {
            this.mGrabbedState = newState;
            OnTriggerListener onTriggerListener = this.mOnTriggerListener;
            if (onTriggerListener != null) {
                onTriggerListener.onGrabbedStateChange(this, this.mGrabbedState);
            }
        }
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
