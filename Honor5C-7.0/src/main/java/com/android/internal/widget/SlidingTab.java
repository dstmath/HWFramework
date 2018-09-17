package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.android.internal.R;
import huawei.cust.HwCfgFilePolicy;

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
    private static final AudioAttributes VIBRATION_ATTRIBUTES = null;
    private boolean mAnimating;
    private final AnimationListener mAnimationDoneListener;
    private Slider mCurrentSlider;
    private final float mDensity;
    private int mGrabbedState;
    private boolean mHoldLeftOnTransition;
    private boolean mHoldRightOnTransition;
    private final Slider mLeftSlider;
    private OnTriggerListener mOnTriggerListener;
    private final int mOrientation;
    private Slider mOtherSlider;
    private final Slider mRightSlider;
    private float mThreshold;
    private final Rect mTmpRect;
    private boolean mTracking;
    private boolean mTriggered;
    private Vibrator mVibrator;

    /* renamed from: com.android.internal.widget.SlidingTab.2 */
    class AnonymousClass2 implements AnimationListener {
        final /* synthetic */ int val$dx;
        final /* synthetic */ int val$dy;
        final /* synthetic */ boolean val$holdAfter;

        AnonymousClass2(boolean val$holdAfter, int val$dx, int val$dy) {
            this.val$holdAfter = val$holdAfter;
            this.val$dx = val$dx;
            this.val$dy = val$dy;
        }

        public void onAnimationEnd(Animation animation) {
            Animation anim;
            if (this.val$holdAfter) {
                anim = new TranslateAnimation((float) this.val$dx, (float) this.val$dx, (float) this.val$dy, (float) this.val$dy);
                anim.setDuration(1000);
                SlidingTab.this.mAnimating = SlidingTab.DBG;
            } else {
                anim = new AlphaAnimation(0.5f, (float) LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                anim.setDuration(250);
                SlidingTab.this.resetView();
            }
            anim.setAnimationListener(SlidingTab.this.mAnimationDoneListener);
            SlidingTab.this.mLeftSlider.startAnimation(anim, anim);
            SlidingTab.this.mRightSlider.startAnimation(anim, anim);
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    public interface OnTriggerListener {
        public static final int LEFT_HANDLE = 1;
        public static final int NO_HANDLE = 0;
        public static final int RIGHT_HANDLE = 2;

        void onGrabbedStateChange(View view, int i);

        void onTrigger(View view, int i);
    }

    private static class Slider {
        public static final int ALIGN_BOTTOM = 3;
        public static final int ALIGN_LEFT = 0;
        public static final int ALIGN_RIGHT = 1;
        public static final int ALIGN_TOP = 2;
        public static final int ALIGN_UNKNOWN = 4;
        private static final int STATE_ACTIVE = 2;
        private static final int STATE_NORMAL = 0;
        private static final int STATE_PRESSED = 1;
        private int alignment;
        private int alignment_value;
        private int currentState;
        private final ImageView tab;
        private final ImageView target;
        private final TextView text;

        Slider(ViewGroup parent, int tabId, int barId, int targetId) {
            this.currentState = STATE_NORMAL;
            this.alignment = ALIGN_UNKNOWN;
            this.tab = new ImageView(parent.getContext());
            this.tab.setBackgroundResource(tabId);
            this.tab.setScaleType(ScaleType.CENTER);
            this.tab.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            this.text = new TextView(parent.getContext());
            this.text.setLayoutParams(new ViewGroup.LayoutParams(-2, -1));
            this.text.setBackgroundResource(barId);
            this.text.setTextAppearance(parent.getContext(), R.style.TextAppearance_SlidingTabNormal);
            this.target = new ImageView(parent.getContext());
            this.target.setImageResource(targetId);
            this.target.setScaleType(ScaleType.CENTER);
            this.target.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            this.target.setVisibility(ALIGN_UNKNOWN);
            parent.addView(this.target);
            parent.addView(this.tab);
            parent.addView(this.text);
        }

        void setIcon(int iconId) {
            this.tab.setImageResource(iconId);
        }

        void setTabBackgroundResource(int tabId) {
            this.tab.setBackgroundResource(tabId);
        }

        void setBarBackgroundResource(int barId) {
            this.text.setBackgroundResource(barId);
        }

        void setHintText(int resId) {
            this.text.setText(resId);
        }

        void hide() {
            boolean horiz = SlidingTab.DBG;
            if (this.alignment == 0 || this.alignment == STATE_PRESSED) {
                horiz = true;
            }
            int dx = horiz ? this.alignment == 0 ? this.alignment_value - this.tab.getRight() : this.alignment_value - this.tab.getLeft() : STATE_NORMAL;
            int dy = horiz ? STATE_NORMAL : this.alignment == STATE_ACTIVE ? this.alignment_value - this.tab.getBottom() : this.alignment_value - this.tab.getTop();
            Animation trans = new TranslateAnimation(0.0f, (float) dx, 0.0f, (float) dy);
            trans.setDuration(250);
            trans.setFillAfter(true);
            this.tab.startAnimation(trans);
            this.text.startAnimation(trans);
            this.target.setVisibility(ALIGN_UNKNOWN);
        }

        void show(boolean animate) {
            boolean horiz = true;
            this.text.setVisibility(STATE_NORMAL);
            this.tab.setVisibility(STATE_NORMAL);
            if (animate) {
                if (!(this.alignment == 0 || this.alignment == STATE_PRESSED)) {
                    horiz = SlidingTab.DBG;
                }
                int dx = horiz ? this.alignment == 0 ? this.tab.getWidth() : -this.tab.getWidth() : STATE_NORMAL;
                int dy = horiz ? STATE_NORMAL : this.alignment == STATE_ACTIVE ? this.tab.getHeight() : -this.tab.getHeight();
                Animation trans = new TranslateAnimation((float) (-dx), 0.0f, (float) (-dy), 0.0f);
                trans.setDuration(250);
                this.tab.startAnimation(trans);
                this.text.startAnimation(trans);
            }
        }

        void setState(int state) {
            boolean z;
            TextView textView = this.text;
            if (state == STATE_PRESSED) {
                z = true;
            } else {
                z = SlidingTab.DBG;
            }
            textView.setPressed(z);
            ImageView imageView = this.tab;
            if (state == STATE_PRESSED) {
                z = true;
            } else {
                z = SlidingTab.DBG;
            }
            imageView.setPressed(z);
            if (state == STATE_ACTIVE) {
                int[] activeState = new int[STATE_PRESSED];
                activeState[STATE_NORMAL] = R.attr.state_active;
                if (this.text.getBackground().isStateful()) {
                    this.text.getBackground().setState(activeState);
                }
                if (this.tab.getBackground().isStateful()) {
                    this.tab.getBackground().setState(activeState);
                }
                this.text.setTextAppearance(this.text.getContext(), R.style.TextAppearance_SlidingTabActive);
            } else {
                this.text.setTextAppearance(this.text.getContext(), R.style.TextAppearance_SlidingTabNormal);
            }
            this.currentState = state;
        }

        void showTarget() {
            AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, (float) LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            alphaAnim.setDuration(500);
            this.target.startAnimation(alphaAnim);
            this.target.setVisibility(STATE_NORMAL);
        }

        void reset(boolean animate) {
            boolean horiz = true;
            setState(STATE_NORMAL);
            this.text.setVisibility(STATE_NORMAL);
            this.text.setTextAppearance(this.text.getContext(), R.style.TextAppearance_SlidingTabNormal);
            this.tab.setVisibility(STATE_NORMAL);
            this.target.setVisibility(ALIGN_UNKNOWN);
            if (!(this.alignment == 0 || this.alignment == STATE_PRESSED)) {
                horiz = SlidingTab.DBG;
            }
            int dx = horiz ? this.alignment == 0 ? this.alignment_value - this.tab.getLeft() : this.alignment_value - this.tab.getRight() : STATE_NORMAL;
            int dy = horiz ? STATE_NORMAL : this.alignment == STATE_ACTIVE ? this.alignment_value - this.tab.getTop() : this.alignment_value - this.tab.getBottom();
            if (animate) {
                TranslateAnimation trans = new TranslateAnimation(0.0f, (float) dx, 0.0f, (float) dy);
                trans.setDuration(250);
                trans.setFillAfter(SlidingTab.DBG);
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

        void setTarget(int targetId) {
            this.target.setImageResource(targetId);
        }

        void layout(int l, int t, int r, int b, int alignment) {
            this.alignment = alignment;
            Drawable tabBackground = this.tab.getBackground();
            int handleWidth = tabBackground.getIntrinsicWidth();
            int handleHeight = tabBackground.getIntrinsicHeight();
            Drawable targetDrawable = this.target.getDrawable();
            int targetWidth = targetDrawable.getIntrinsicWidth();
            int targetHeight = targetDrawable.getIntrinsicHeight();
            int parentWidth = r - l;
            int parentHeight = b - t;
            int leftTarget = (((int) (((float) parentWidth) * SlidingTab.THRESHOLD)) - targetWidth) + (handleWidth / STATE_ACTIVE);
            int rightTarget = ((int) (((float) parentWidth) * 0.3333333f)) - (handleWidth / STATE_ACTIVE);
            int left = (parentWidth - handleWidth) / STATE_ACTIVE;
            int right = left + handleWidth;
            int top;
            int bottom;
            if (alignment == 0 || alignment == STATE_PRESSED) {
                int targetTop = (parentHeight - targetHeight) / STATE_ACTIVE;
                int targetBottom = targetTop + targetHeight;
                top = (parentHeight - handleHeight) / STATE_ACTIVE;
                bottom = (parentHeight + handleHeight) / STATE_ACTIVE;
                if (alignment == 0) {
                    this.tab.layout(STATE_NORMAL, top, handleWidth, bottom);
                    this.text.layout(0 - parentWidth, top, STATE_NORMAL, bottom);
                    this.text.setGravity(5);
                    this.target.layout(leftTarget, targetTop, leftTarget + targetWidth, targetBottom);
                    this.alignment_value = l;
                    return;
                }
                this.tab.layout(parentWidth - handleWidth, top, parentWidth, bottom);
                this.text.layout(parentWidth, top, parentWidth + parentWidth, bottom);
                this.target.layout(rightTarget, targetTop, rightTarget + targetWidth, targetBottom);
                this.text.setGravity(48);
                this.alignment_value = r;
                return;
            }
            int targetLeft = (parentWidth - targetWidth) / STATE_ACTIVE;
            int targetRight = (parentWidth + targetWidth) / STATE_ACTIVE;
            top = (((int) (((float) parentHeight) * SlidingTab.THRESHOLD)) + (handleHeight / STATE_ACTIVE)) - targetHeight;
            bottom = ((int) (((float) parentHeight) * 0.3333333f)) - (handleHeight / STATE_ACTIVE);
            if (alignment == STATE_ACTIVE) {
                this.tab.layout(left, STATE_NORMAL, right, handleHeight);
                this.text.layout(left, 0 - parentHeight, right, STATE_NORMAL);
                this.target.layout(targetLeft, top, targetRight, top + targetHeight);
                this.alignment_value = t;
                return;
            }
            this.tab.layout(left, parentHeight - handleHeight, right, parentHeight);
            this.text.layout(left, parentHeight, right, parentHeight + parentHeight);
            this.target.layout(targetLeft, bottom, targetRight, bottom + targetHeight);
            this.alignment_value = b;
        }

        public void updateDrawableStates() {
            setState(this.currentState);
        }

        public void measure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            this.tab.measure(MeasureSpec.makeSafeMeasureSpec(width, STATE_NORMAL), MeasureSpec.makeSafeMeasureSpec(height, STATE_NORMAL));
            this.text.measure(MeasureSpec.makeSafeMeasureSpec(width, STATE_NORMAL), MeasureSpec.makeSafeMeasureSpec(height, STATE_NORMAL));
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
            this.target.setVisibility(ALIGN_UNKNOWN);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.SlidingTab.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.SlidingTab.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.SlidingTab.<clinit>():void");
    }

    public SlidingTab(Context context) {
        this(context, null);
    }

    public SlidingTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHoldLeftOnTransition = true;
        this.mHoldRightOnTransition = true;
        this.mGrabbedState = HORIZONTAL;
        this.mTriggered = DBG;
        this.mAnimationDoneListener = new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                SlidingTab.this.onAnimationDone();
            }
        };
        this.mTmpRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTab);
        this.mOrientation = a.getInt(HORIZONTAL, HORIZONTAL);
        a.recycle();
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mLeftSlider = new Slider(this, R.drawable.jog_tab_left_generic, R.drawable.jog_tab_bar_left_generic, R.drawable.jog_tab_target_gray);
        this.mRightSlider = new Slider(this, R.drawable.jog_tab_right_generic, R.drawable.jog_tab_bar_right_generic, R.drawable.jog_tab_target_gray);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
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

    public boolean onInterceptTouchEvent(MotionEvent event) {
        float f = THRESHOLD;
        float f2 = 0.3333333f;
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        if (this.mAnimating) {
            return DBG;
        }
        this.mLeftSlider.tab.getHitRect(this.mTmpRect);
        boolean leftHit = this.mTmpRect.contains((int) x, (int) y);
        this.mRightSlider.tab.getHitRect(this.mTmpRect);
        boolean rightHit = this.mTmpRect.contains((int) x, (int) y);
        if (!this.mTracking && !leftHit && !rightHit) {
            return DBG;
        }
        switch (action) {
            case HORIZONTAL /*0*/:
                this.mTracking = true;
                this.mTriggered = DBG;
                vibrate(VIBRATE_SHORT);
                if (leftHit) {
                    this.mCurrentSlider = this.mLeftSlider;
                    this.mOtherSlider = this.mRightSlider;
                    if (!isHorizontal()) {
                        f = 0.3333333f;
                    }
                    this.mThreshold = f;
                    setGrabbedState(VERTICAL);
                } else {
                    this.mCurrentSlider = this.mRightSlider;
                    this.mOtherSlider = this.mLeftSlider;
                    if (!isHorizontal()) {
                        f2 = THRESHOLD;
                    }
                    this.mThreshold = f2;
                    setGrabbedState(2);
                }
                this.mCurrentSlider.setState(VERTICAL);
                this.mCurrentSlider.showTarget();
                this.mOtherSlider.hide();
                break;
        }
        return true;
    }

    public void reset(boolean animate) {
        this.mLeftSlider.reset(animate);
        this.mRightSlider.reset(animate);
        if (!animate) {
            this.mAnimating = DBG;
        }
    }

    public void setVisibility(int visibility) {
        if (visibility != getVisibility() && visibility == 4) {
            reset(DBG);
        }
        super.setVisibility(visibility);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mTracking) {
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            switch (action) {
                case HwCfgFilePolicy.PC /*2*/:
                    if (withinView(x, y, this)) {
                        moveHandle(x, y);
                        float position = isHorizontal() ? x : y;
                        float target = this.mThreshold * ((float) (isHorizontal() ? getWidth() : getHeight()));
                        boolean thresholdReached;
                        if (isHorizontal()) {
                            if (this.mCurrentSlider == this.mLeftSlider) {
                                break;
                            }
                            if (position < target) {
                            }
                            thresholdReached = DBG;
                            thresholdReached = true;
                        } else {
                            if (this.mCurrentSlider == this.mLeftSlider) {
                                break;
                            }
                            if (position > target) {
                            }
                            thresholdReached = DBG;
                            thresholdReached = true;
                        }
                        if (!this.mTriggered && thresholdReached) {
                            boolean isLeft;
                            int i;
                            this.mTriggered = true;
                            this.mTracking = DBG;
                            this.mCurrentSlider.setState(2);
                            if (this.mCurrentSlider == this.mLeftSlider) {
                                isLeft = true;
                            } else {
                                isLeft = DBG;
                            }
                            if (isLeft) {
                                i = VERTICAL;
                            } else {
                                i = 2;
                            }
                            dispatchTriggerEvent(i);
                            startAnimating(isLeft ? this.mHoldLeftOnTransition : this.mHoldRightOnTransition);
                            setGrabbedState(HORIZONTAL);
                            break;
                        }
                    }
                case VERTICAL /*1*/:
                case HwCfgFilePolicy.BASE /*3*/:
                    cancelGrab();
                    break;
            }
        }
        if (this.mTracking) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void cancelGrab() {
        this.mTracking = DBG;
        this.mTriggered = DBG;
        this.mOtherSlider.show(true);
        this.mCurrentSlider.reset(DBG);
        this.mCurrentSlider.hideTarget();
        this.mCurrentSlider = null;
        this.mOtherSlider = null;
        setGrabbedState(HORIZONTAL);
    }

    void startAnimating(boolean holdAfter) {
        int dx;
        int dy;
        this.mAnimating = true;
        Slider slider = this.mCurrentSlider;
        Slider other = this.mOtherSlider;
        int holdOffset;
        Slider slider2;
        if (isHorizontal()) {
            int right = slider.tab.getRight();
            int width = slider.tab.getWidth();
            int left = slider.tab.getLeft();
            int viewWidth = getWidth();
            holdOffset = holdAfter ? HORIZONTAL : width;
            slider2 = this.mRightSlider;
            if (slider == r0) {
                dx = -((right + viewWidth) - holdOffset);
            } else {
                dx = ((viewWidth - left) + viewWidth) - holdOffset;
            }
            dy = HORIZONTAL;
        } else {
            int top = slider.tab.getTop();
            int bottom = slider.tab.getBottom();
            int height = slider.tab.getHeight();
            int viewHeight = getHeight();
            holdOffset = holdAfter ? HORIZONTAL : height;
            dx = HORIZONTAL;
            slider2 = this.mRightSlider;
            if (slider == r0) {
                dy = (top + viewHeight) - holdOffset;
            } else {
                dy = -(((viewHeight - bottom) + viewHeight) - holdOffset);
            }
        }
        Animation trans1 = new TranslateAnimation(0.0f, (float) dx, 0.0f, (float) dy);
        trans1.setDuration(250);
        trans1.setInterpolator(new LinearInterpolator());
        trans1.setFillAfter(true);
        Animation trans2 = new TranslateAnimation(0.0f, (float) dx, 0.0f, (float) dy);
        trans2.setDuration(250);
        trans2.setInterpolator(new LinearInterpolator());
        trans2.setFillAfter(true);
        trans1.setAnimationListener(new AnonymousClass2(holdAfter, dx, dy));
        slider.hideTarget();
        slider.startAnimation(trans1, trans2);
    }

    private void onAnimationDone() {
        resetView();
        this.mAnimating = DBG;
    }

    private boolean withinView(float x, float y, View view) {
        if (isHorizontal() && y > -50.0f && y < ((float) (view.getHeight() + TRACKING_MARGIN))) {
            return true;
        }
        if (isHorizontal() || x <= -50.0f || x >= ((float) (view.getWidth() + TRACKING_MARGIN))) {
            return DBG;
        }
        return true;
    }

    private boolean isHorizontal() {
        return this.mOrientation == 0 ? true : DBG;
    }

    private void resetView() {
        this.mLeftSlider.reset(DBG);
        this.mRightSlider.reset(DBG);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int i;
            this.mLeftSlider.layout(l, t, r, b, isHorizontal() ? HORIZONTAL : 3);
            Slider slider = this.mRightSlider;
            if (isHorizontal()) {
                i = VERTICAL;
            } else {
                i = 2;
            }
            slider.layout(l, t, r, b, i);
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

    public void setLeftTabResources(int iconId, int targetId, int barId, int tabId) {
        this.mLeftSlider.setIcon(iconId);
        this.mLeftSlider.setTarget(targetId);
        this.mLeftSlider.setBarBackgroundResource(barId);
        this.mLeftSlider.setTabBackgroundResource(tabId);
        this.mLeftSlider.updateDrawableStates();
    }

    public void setLeftHintText(int resId) {
        if (isHorizontal()) {
            this.mLeftSlider.setHintText(resId);
        }
    }

    public void setRightTabResources(int iconId, int targetId, int barId, int tabId) {
        this.mRightSlider.setIcon(iconId);
        this.mRightSlider.setTarget(targetId);
        this.mRightSlider.setBarBackgroundResource(barId);
        this.mRightSlider.setTabBackgroundResource(tabId);
        this.mRightSlider.updateDrawableStates();
    }

    public void setRightHintText(int resId) {
        if (isHorizontal()) {
            this.mRightSlider.setHintText(resId);
        }
    }

    public void setHoldAfterTrigger(boolean holdLeft, boolean holdRight) {
        this.mHoldLeftOnTransition = holdLeft;
        this.mHoldRightOnTransition = holdRight;
    }

    private synchronized void vibrate(long duration) {
        if (System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", VERTICAL, -2) != 0 ? true : DBG) {
            if (this.mVibrator == null) {
                this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
            }
            this.mVibrator.vibrate(duration, VIBRATION_ATTRIBUTES);
        }
    }

    public void setOnTriggerListener(OnTriggerListener listener) {
        this.mOnTriggerListener = listener;
    }

    private void dispatchTriggerEvent(int whichHandle) {
        vibrate(VIBRATE_LONG);
        if (this.mOnTriggerListener != null) {
            this.mOnTriggerListener.onTrigger(this, whichHandle);
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && visibility != 0 && this.mGrabbedState != 0) {
            cancelGrab();
        }
    }

    private void setGrabbedState(int newState) {
        if (newState != this.mGrabbedState) {
            this.mGrabbedState = newState;
            if (this.mOnTriggerListener != null) {
                this.mOnTriggerListener.onGrabbedStateChange(this, this.mGrabbedState);
            }
        }
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
