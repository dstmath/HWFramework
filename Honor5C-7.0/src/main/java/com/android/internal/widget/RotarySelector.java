package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import huawei.cust.HwCfgFilePolicy;

public class RotarySelector extends View {
    private static final int ARROW_SCRUNCH_DIP = 6;
    private static final boolean DBG = false;
    private static final int EDGE_PADDING_DIP = 9;
    private static final int EDGE_TRIGGER_DIP = 100;
    public static final int HORIZONTAL = 0;
    public static final int LEFT_HANDLE_GRABBED = 1;
    private static final String LOG_TAG = "RotarySelector";
    public static final int NOTHING_GRABBED = 0;
    static final int OUTER_ROTARY_RADIUS_DIP = 390;
    public static final int RIGHT_HANDLE_GRABBED = 2;
    static final int ROTARY_STROKE_WIDTH_DIP = 83;
    static final int SNAP_BACK_ANIMATION_DURATION_MILLIS = 300;
    static final int SPIN_ANIMATION_DURATION_MILLIS = 800;
    public static final int VERTICAL = 1;
    private static final long VIBRATE_LONG = 20;
    private static final long VIBRATE_SHORT = 20;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = null;
    private static final boolean VISUAL_DEBUG = false;
    private boolean mAnimating;
    private int mAnimatingDeltaXEnd;
    private int mAnimatingDeltaXStart;
    private long mAnimationDuration;
    private long mAnimationStartTime;
    private Bitmap mArrowLongLeft;
    private Bitmap mArrowLongRight;
    final Matrix mArrowMatrix;
    private Bitmap mArrowShortLeftAndRight;
    private Bitmap mBackground;
    private int mBackgroundHeight;
    private int mBackgroundWidth;
    final Matrix mBgMatrix;
    private float mDensity;
    private Bitmap mDimple;
    private Bitmap mDimpleDim;
    private int mDimpleSpacing;
    private int mDimpleWidth;
    private int mDimplesOfFling;
    private int mEdgeTriggerThresh;
    private int mGrabbedState;
    private final int mInnerRadius;
    private DecelerateInterpolator mInterpolator;
    private Bitmap mLeftHandleIcon;
    private int mLeftHandleX;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private OnDialTriggerListener mOnDialTriggerListener;
    private int mOrientation;
    private final int mOuterRadius;
    private Paint mPaint;
    private Bitmap mRightHandleIcon;
    private int mRightHandleX;
    private int mRotaryOffsetX;
    private boolean mTriggered;
    private VelocityTracker mVelocityTracker;
    private Vibrator mVibrator;

    public interface OnDialTriggerListener {
        public static final int LEFT_HANDLE = 1;
        public static final int RIGHT_HANDLE = 2;

        void onDialTrigger(View view, int i);

        void onGrabbedStateChange(View view, int i);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.RotarySelector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.RotarySelector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.RotarySelector.<clinit>():void");
    }

    public RotarySelector(Context context) {
        this(context, null);
    }

    public RotarySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRotaryOffsetX = NOTHING_GRABBED;
        this.mAnimating = DBG;
        this.mPaint = new Paint();
        this.mBgMatrix = new Matrix();
        this.mArrowMatrix = new Matrix();
        this.mGrabbedState = NOTHING_GRABBED;
        this.mTriggered = DBG;
        this.mDimplesOfFling = NOTHING_GRABBED;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotarySelector);
        this.mOrientation = a.getInt(NOTHING_GRABBED, NOTHING_GRABBED);
        a.recycle();
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mBackground = getBitmapFor(R.drawable.jog_dial_bg);
        this.mDimple = getBitmapFor(R.drawable.jog_dial_dimple);
        this.mDimpleDim = getBitmapFor(R.drawable.jog_dial_dimple_dim);
        this.mArrowLongLeft = getBitmapFor(R.drawable.jog_dial_arrow_long_left_green);
        this.mArrowLongRight = getBitmapFor(R.drawable.jog_dial_arrow_long_right_red);
        this.mArrowShortLeftAndRight = getBitmapFor(R.drawable.jog_dial_arrow_short_left_and_right);
        this.mInterpolator = new DecelerateInterpolator(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        this.mEdgeTriggerThresh = (int) (this.mDensity * 100.0f);
        this.mDimpleWidth = this.mDimple.getWidth();
        this.mBackgroundWidth = this.mBackground.getWidth();
        this.mBackgroundHeight = this.mBackground.getHeight();
        this.mOuterRadius = (int) (this.mDensity * 390.0f);
        this.mInnerRadius = (int) (this.mDensity * 307.0f);
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity() * RIGHT_HANDLE_GRABBED;
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int length;
        super.onSizeChanged(w, h, oldw, oldh);
        int edgePadding = (int) (this.mDensity * 9.0f);
        this.mLeftHandleX = (this.mDimpleWidth / RIGHT_HANDLE_GRABBED) + edgePadding;
        if (isHoriz()) {
            length = w;
        } else {
            length = h;
        }
        this.mRightHandleX = (length - edgePadding) - (this.mDimpleWidth / RIGHT_HANDLE_GRABBED);
        this.mDimpleSpacing = (length / RIGHT_HANDLE_GRABBED) - this.mLeftHandleX;
        this.mBgMatrix.setTranslate(0.0f, 0.0f);
        if (isHoriz()) {
            this.mBgMatrix.postTranslate(0.0f, (float) (h - this.mBackgroundHeight));
            return;
        }
        int left = w - this.mBackgroundHeight;
        this.mBgMatrix.preRotate(-90.0f, 0.0f, 0.0f);
        this.mBgMatrix.postTranslate((float) left, (float) h);
    }

    private boolean isHoriz() {
        return this.mOrientation == 0 ? true : DBG;
    }

    public void setLeftHandleResource(int resId) {
        if (resId != 0) {
            this.mLeftHandleIcon = getBitmapFor(resId);
        }
        invalidate();
    }

    public void setRightHandleResource(int resId) {
        if (resId != 0) {
            this.mRightHandleIcon = getBitmapFor(resId);
        }
        invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int length;
        if (isHoriz()) {
            length = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            length = MeasureSpec.getSize(heightMeasureSpec);
        }
        int height = (this.mBackgroundHeight + this.mArrowShortLeftAndRight.getHeight()) - ((int) (this.mDensity * 6.0f));
        if (isHoriz()) {
            setMeasuredDimension(length, height);
        } else {
            setMeasuredDimension(height, length);
        }
    }

    protected void onDraw(Canvas canvas) {
        int bgTop;
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (this.mAnimating) {
            updateAnimation();
        }
        canvas.drawBitmap(this.mBackground, this.mBgMatrix, this.mPaint);
        this.mArrowMatrix.reset();
        switch (this.mGrabbedState) {
            case NOTHING_GRABBED /*0*/:
                break;
            case VERTICAL /*1*/:
                this.mArrowMatrix.setTranslate(0.0f, 0.0f);
                if (!isHoriz()) {
                    this.mArrowMatrix.preRotate(-90.0f, 0.0f, 0.0f);
                    this.mArrowMatrix.postTranslate(0.0f, (float) height);
                }
                canvas.drawBitmap(this.mArrowLongLeft, this.mArrowMatrix, this.mPaint);
                break;
            case RIGHT_HANDLE_GRABBED /*2*/:
                this.mArrowMatrix.setTranslate(0.0f, 0.0f);
                if (!isHoriz()) {
                    this.mArrowMatrix.preRotate(-90.0f, 0.0f, 0.0f);
                    this.mArrowMatrix.postTranslate(0.0f, (float) ((this.mBackgroundWidth - height) + height));
                }
                canvas.drawBitmap(this.mArrowLongRight, this.mArrowMatrix, this.mPaint);
                break;
            default:
                throw new IllegalStateException("invalid mGrabbedState: " + this.mGrabbedState);
        }
        int bgHeight = this.mBackgroundHeight;
        if (isHoriz()) {
            bgTop = height - bgHeight;
        } else {
            bgTop = width - bgHeight;
        }
        int xOffset = this.mLeftHandleX + this.mRotaryOffsetX;
        int drawableY = getYOnArc(this.mBackgroundWidth, this.mInnerRadius, this.mOuterRadius, xOffset);
        int x = isHoriz() ? xOffset : drawableY + bgTop;
        int y = isHoriz() ? drawableY + bgTop : height - xOffset;
        int i = this.mGrabbedState;
        if (r0 != RIGHT_HANDLE_GRABBED) {
            drawCentered(this.mDimple, canvas, x, y);
            drawCentered(this.mLeftHandleIcon, canvas, x, y);
        } else {
            drawCentered(this.mDimpleDim, canvas, x, y);
        }
        if (isHoriz()) {
            xOffset = (width / RIGHT_HANDLE_GRABBED) + this.mRotaryOffsetX;
        } else {
            xOffset = (height / RIGHT_HANDLE_GRABBED) + this.mRotaryOffsetX;
        }
        drawableY = getYOnArc(this.mBackgroundWidth, this.mInnerRadius, this.mOuterRadius, xOffset);
        if (isHoriz()) {
            drawCentered(this.mDimpleDim, canvas, xOffset, drawableY + bgTop);
        } else {
            drawCentered(this.mDimpleDim, canvas, drawableY + bgTop, height - xOffset);
        }
        xOffset = this.mRightHandleX + this.mRotaryOffsetX;
        drawableY = getYOnArc(this.mBackgroundWidth, this.mInnerRadius, this.mOuterRadius, xOffset);
        x = isHoriz() ? xOffset : drawableY + bgTop;
        y = isHoriz() ? drawableY + bgTop : height - xOffset;
        i = this.mGrabbedState;
        if (r0 != VERTICAL) {
            drawCentered(this.mDimple, canvas, x, y);
            drawCentered(this.mRightHandleIcon, canvas, x, y);
        } else {
            drawCentered(this.mDimpleDim, canvas, x, y);
        }
        int dimpleLeft = (this.mRotaryOffsetX + this.mLeftHandleX) - this.mDimpleSpacing;
        int halfdimple = this.mDimpleWidth / RIGHT_HANDLE_GRABBED;
        while (true) {
            i = -halfdimple;
            if (dimpleLeft > r0) {
                drawableY = getYOnArc(this.mBackgroundWidth, this.mInnerRadius, this.mOuterRadius, dimpleLeft);
                if (isHoriz()) {
                    drawCentered(this.mDimpleDim, canvas, dimpleLeft, drawableY + bgTop);
                } else {
                    drawCentered(this.mDimpleDim, canvas, drawableY + bgTop, height - dimpleLeft);
                }
                dimpleLeft -= this.mDimpleSpacing;
            } else {
                int dimpleRight = (this.mRotaryOffsetX + this.mRightHandleX) + this.mDimpleSpacing;
                int rightThresh = this.mRight + halfdimple;
                while (dimpleRight < rightThresh) {
                    drawableY = getYOnArc(this.mBackgroundWidth, this.mInnerRadius, this.mOuterRadius, dimpleRight);
                    if (isHoriz()) {
                        drawCentered(this.mDimpleDim, canvas, dimpleRight, drawableY + bgTop);
                    } else {
                        drawCentered(this.mDimpleDim, canvas, drawableY + bgTop, height - dimpleRight);
                    }
                    dimpleRight += this.mDimpleSpacing;
                }
                return;
            }
        }
    }

    private int getYOnArc(int backgroundWidth, int innerRadius, int outerRadius, int x) {
        int halfWidth = (outerRadius - innerRadius) / RIGHT_HANDLE_GRABBED;
        int middleRadius = innerRadius + halfWidth;
        int triangleBottom = (backgroundWidth / RIGHT_HANDLE_GRABBED) - x;
        return (middleRadius - ((int) Math.sqrt((double) ((middleRadius * middleRadius) - (triangleBottom * triangleBottom))))) + halfWidth;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mAnimating) {
            return true;
        }
        int eventX;
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        int height = getHeight();
        if (isHoriz()) {
            eventX = (int) event.getX();
        } else {
            eventX = height - ((int) event.getY());
        }
        int hitWindow = this.mDimpleWidth;
        switch (event.getAction()) {
            case NOTHING_GRABBED /*0*/:
                this.mTriggered = DBG;
                if (this.mGrabbedState != 0) {
                    reset();
                    invalidate();
                }
                if (eventX >= this.mLeftHandleX + hitWindow) {
                    if (eventX > this.mRightHandleX - hitWindow) {
                        this.mRotaryOffsetX = eventX - this.mRightHandleX;
                        setGrabbedState(RIGHT_HANDLE_GRABBED);
                        invalidate();
                        vibrate(VIBRATE_SHORT);
                        break;
                    }
                }
                this.mRotaryOffsetX = eventX - this.mLeftHandleX;
                setGrabbedState(VERTICAL);
                invalidate();
                vibrate(VIBRATE_SHORT);
                break;
                break;
            case VERTICAL /*1*/:
                if (this.mGrabbedState == VERTICAL && Math.abs(eventX - this.mLeftHandleX) > 5) {
                    startAnimation(eventX - this.mLeftHandleX, NOTHING_GRABBED, SNAP_BACK_ANIMATION_DURATION_MILLIS);
                } else if (this.mGrabbedState == RIGHT_HANDLE_GRABBED && Math.abs(eventX - this.mRightHandleX) > 5) {
                    startAnimation(eventX - this.mRightHandleX, NOTHING_GRABBED, SNAP_BACK_ANIMATION_DURATION_MILLIS);
                }
                this.mRotaryOffsetX = NOTHING_GRABBED;
                setGrabbedState(NOTHING_GRABBED);
                invalidate();
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    break;
                }
                break;
            case RIGHT_HANDLE_GRABBED /*2*/:
                VelocityTracker velocityTracker;
                int rawVelocity;
                int velocity;
                if (this.mGrabbedState != VERTICAL) {
                    if (this.mGrabbedState == RIGHT_HANDLE_GRABBED) {
                        this.mRotaryOffsetX = eventX - this.mRightHandleX;
                        invalidate();
                        if (eventX <= this.mEdgeTriggerThresh && !this.mTriggered) {
                            this.mTriggered = true;
                            dispatchTriggerEvent(RIGHT_HANDLE_GRABBED);
                            velocityTracker = this.mVelocityTracker;
                            velocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) this.mMaximumVelocity);
                            if (isHoriz()) {
                                rawVelocity = (int) velocityTracker.getXVelocity();
                            } else {
                                rawVelocity = -((int) velocityTracker.getYVelocity());
                            }
                            velocity = Math.min(-this.mMinimumVelocity, rawVelocity);
                            this.mDimplesOfFling = Math.max(8, Math.abs(velocity / this.mDimpleSpacing));
                            startAnimationWithVelocity(eventX - this.mRightHandleX, -(this.mDimplesOfFling * this.mDimpleSpacing), velocity);
                            break;
                        }
                    }
                }
                this.mRotaryOffsetX = eventX - this.mLeftHandleX;
                invalidate();
                if (eventX >= (isHoriz() ? getRight() : height) - this.mEdgeTriggerThresh && !this.mTriggered) {
                    this.mTriggered = true;
                    dispatchTriggerEvent(VERTICAL);
                    velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) this.mMaximumVelocity);
                    if (isHoriz()) {
                        rawVelocity = (int) velocityTracker.getXVelocity();
                    } else {
                        rawVelocity = -((int) velocityTracker.getYVelocity());
                    }
                    velocity = Math.max(this.mMinimumVelocity, rawVelocity);
                    this.mDimplesOfFling = Math.max(8, Math.abs(velocity / this.mDimpleSpacing));
                    startAnimationWithVelocity(eventX - this.mLeftHandleX, this.mDimplesOfFling * this.mDimpleSpacing, velocity);
                    break;
                }
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                reset();
                invalidate();
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    break;
                }
                break;
        }
        return true;
    }

    private void startAnimation(int startX, int endX, int duration) {
        this.mAnimating = true;
        this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mAnimationDuration = (long) duration;
        this.mAnimatingDeltaXStart = startX;
        this.mAnimatingDeltaXEnd = endX;
        setGrabbedState(NOTHING_GRABBED);
        this.mDimplesOfFling = NOTHING_GRABBED;
        invalidate();
    }

    private void startAnimationWithVelocity(int startX, int endX, int pixelsPerSecond) {
        this.mAnimating = true;
        this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mAnimationDuration = (long) (((endX - startX) * RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) / pixelsPerSecond);
        this.mAnimatingDeltaXStart = startX;
        this.mAnimatingDeltaXEnd = endX;
        setGrabbedState(NOTHING_GRABBED);
        invalidate();
    }

    private void updateAnimation() {
        long millisSoFar = AnimationUtils.currentAnimationTimeMillis() - this.mAnimationStartTime;
        long millisLeft = this.mAnimationDuration - millisSoFar;
        int totalDeltaX = this.mAnimatingDeltaXStart - this.mAnimatingDeltaXEnd;
        boolean goingRight = totalDeltaX < 0 ? true : DBG;
        if (millisLeft <= 0) {
            reset();
            return;
        }
        this.mRotaryOffsetX = this.mAnimatingDeltaXEnd + ((int) (((float) totalDeltaX) * (LayoutParams.BRIGHTNESS_OVERRIDE_FULL - this.mInterpolator.getInterpolation(((float) millisSoFar) / ((float) this.mAnimationDuration)))));
        if (this.mDimplesOfFling > 0) {
            if (!goingRight && this.mRotaryOffsetX < this.mDimpleSpacing * -3) {
                this.mRotaryOffsetX += this.mDimplesOfFling * this.mDimpleSpacing;
            } else if (goingRight && this.mRotaryOffsetX > this.mDimpleSpacing * 3) {
                this.mRotaryOffsetX -= this.mDimplesOfFling * this.mDimpleSpacing;
            }
        }
        invalidate();
    }

    private void reset() {
        this.mAnimating = DBG;
        this.mRotaryOffsetX = NOTHING_GRABBED;
        this.mDimplesOfFling = NOTHING_GRABBED;
        setGrabbedState(NOTHING_GRABBED);
        this.mTriggered = DBG;
    }

    private synchronized void vibrate(long duration) {
        if (System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", VERTICAL, -2) != 0 ? true : DBG) {
            if (this.mVibrator == null) {
                this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
            }
            this.mVibrator.vibrate(duration, VIBRATION_ATTRIBUTES);
        }
    }

    private void drawCentered(Bitmap d, Canvas c, int x, int y) {
        c.drawBitmap(d, (float) (x - (d.getWidth() / RIGHT_HANDLE_GRABBED)), (float) (y - (d.getHeight() / RIGHT_HANDLE_GRABBED)), this.mPaint);
    }

    public void setOnDialTriggerListener(OnDialTriggerListener l) {
        this.mOnDialTriggerListener = l;
    }

    private void dispatchTriggerEvent(int whichHandle) {
        vibrate(VIBRATE_SHORT);
        if (this.mOnDialTriggerListener != null) {
            this.mOnDialTriggerListener.onDialTrigger(this, whichHandle);
        }
    }

    private void setGrabbedState(int newState) {
        if (newState != this.mGrabbedState) {
            this.mGrabbedState = newState;
            if (this.mOnDialTriggerListener != null) {
                this.mOnDialTriggerListener.onGrabbedStateChange(this, this.mGrabbedState);
            }
        }
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
