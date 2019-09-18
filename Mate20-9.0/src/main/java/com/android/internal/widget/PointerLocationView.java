package com.android.internal.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManagerPolicyConstants;
import java.util.ArrayList;

public class PointerLocationView extends View implements InputManager.InputDeviceListener, WindowManagerPolicyConstants.PointerEventListener {
    private static final String ALT_STRATEGY_PROPERY_KEY = "debug.velocitytracker.alt";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "Pointer";
    private final int ESTIMATE_FUTURE_POINTS = 2;
    private final float ESTIMATE_INTERVAL = 0.02f;
    private final int ESTIMATE_PAST_POINTS = 4;
    private int mActivePointerId;
    private final VelocityTracker mAltVelocity;
    private boolean mCurDown;
    private int mCurNumPointers;
    private final Paint mCurrentPointPaint;
    private int mHeaderBottom;
    private final InputManager mIm;
    private int mMaxNumPointers;
    private final Paint mPaint;
    private final Paint mPathPaint;
    private final ArrayList<PointerState> mPointers = new ArrayList<>();
    private boolean mPrintCoords = true;
    private RectF mReusableOvalRect = new RectF();
    private final Paint mTargetPaint;
    private final MotionEvent.PointerCoords mTempCoords = new MotionEvent.PointerCoords();
    private final FasterStringBuilder mText = new FasterStringBuilder();
    private final Paint mTextBackgroundPaint;
    private final Paint mTextLevelPaint;
    private final Paint.FontMetricsInt mTextMetrics = new Paint.FontMetricsInt();
    private final Paint mTextPaint;
    private final ViewConfiguration mVC;
    private final VelocityTracker mVelocity;

    private static final class FasterStringBuilder {
        private char[] mChars = new char[64];
        private int mLength;

        public FasterStringBuilder clear() {
            this.mLength = 0;
            return this;
        }

        public FasterStringBuilder append(String value) {
            int valueLength = value.length();
            value.getChars(0, valueLength, this.mChars, reserve(valueLength));
            this.mLength += valueLength;
            return this;
        }

        public FasterStringBuilder append(int value) {
            return append(value, 0);
        }

        public FasterStringBuilder append(int value, int zeroPadWidth) {
            int index;
            boolean negative = value < 0;
            if (negative) {
                value = -value;
                if (value < 0) {
                    append("-2147483648");
                    return this;
                }
            }
            int index2 = reserve(11);
            char[] chars = this.mChars;
            if (value == 0) {
                int i = index2 + 1;
                chars[index2] = '0';
                this.mLength++;
                return this;
            }
            if (negative) {
                index = index2 + 1;
                chars[index2] = '-';
            } else {
                index = index2;
            }
            int divisor = 1000000000;
            int index3 = index;
            int numberWidth = 10;
            while (value < divisor) {
                divisor /= 10;
                numberWidth--;
                if (numberWidth < zeroPadWidth) {
                    chars[index3] = '0';
                    index3++;
                }
            }
            while (true) {
                int digit = value / divisor;
                value -= digit * divisor;
                divisor /= 10;
                int index4 = index3 + 1;
                chars[index3] = (char) (digit + 48);
                if (divisor == 0) {
                    this.mLength = index4;
                    return this;
                }
                index3 = index4;
            }
        }

        public FasterStringBuilder append(float value, int precision) {
            int scale = 1;
            for (int i = 0; i < precision; i++) {
                scale *= 10;
            }
            float value2 = (float) (Math.rint((double) (((float) scale) * value)) / ((double) scale));
            append((int) value2);
            if (precision != 0) {
                append(".");
                float value3 = Math.abs(value2);
                append((int) (((float) scale) * ((float) (((double) value3) - Math.floor((double) value3)))), precision);
            }
            return this;
        }

        public String toString() {
            return new String(this.mChars, 0, this.mLength);
        }

        private int reserve(int length) {
            int oldLength = this.mLength;
            int newLength = this.mLength + length;
            char[] oldChars = this.mChars;
            int oldCapacity = oldChars.length;
            if (newLength > oldCapacity) {
                char[] newChars = new char[(oldCapacity * 2)];
                System.arraycopy(oldChars, 0, newChars, 0, oldLength);
                this.mChars = newChars;
            }
            return oldLength;
        }
    }

    public static class PointerState {
        /* access modifiers changed from: private */
        public VelocityTracker.Estimator mAltEstimator = new VelocityTracker.Estimator();
        /* access modifiers changed from: private */
        public float mAltXVelocity;
        /* access modifiers changed from: private */
        public float mAltYVelocity;
        /* access modifiers changed from: private */
        public float mBoundingBottom;
        /* access modifiers changed from: private */
        public float mBoundingLeft;
        /* access modifiers changed from: private */
        public float mBoundingRight;
        /* access modifiers changed from: private */
        public float mBoundingTop;
        /* access modifiers changed from: private */
        public MotionEvent.PointerCoords mCoords = new MotionEvent.PointerCoords();
        /* access modifiers changed from: private */
        public boolean mCurDown;
        /* access modifiers changed from: private */
        public VelocityTracker.Estimator mEstimator = new VelocityTracker.Estimator();
        /* access modifiers changed from: private */
        public boolean mHasBoundingBox;
        /* access modifiers changed from: private */
        public int mToolType;
        /* access modifiers changed from: private */
        public int mTraceCount;
        /* access modifiers changed from: private */
        public boolean[] mTraceCurrent = new boolean[32];
        /* access modifiers changed from: private */
        public float[] mTraceX = new float[32];
        /* access modifiers changed from: private */
        public float[] mTraceY = new float[32];
        /* access modifiers changed from: private */
        public float mXVelocity;
        /* access modifiers changed from: private */
        public float mYVelocity;

        public void clearTrace() {
            this.mTraceCount = 0;
        }

        public void addTrace(float x, float y, boolean current) {
            int traceCapacity = this.mTraceX.length;
            if (this.mTraceCount == traceCapacity) {
                int traceCapacity2 = traceCapacity * 2;
                float[] newTraceX = new float[traceCapacity2];
                System.arraycopy(this.mTraceX, 0, newTraceX, 0, this.mTraceCount);
                this.mTraceX = newTraceX;
                float[] newTraceY = new float[traceCapacity2];
                System.arraycopy(this.mTraceY, 0, newTraceY, 0, this.mTraceCount);
                this.mTraceY = newTraceY;
                boolean[] newTraceCurrent = new boolean[traceCapacity2];
                System.arraycopy(this.mTraceCurrent, 0, newTraceCurrent, 0, this.mTraceCount);
                this.mTraceCurrent = newTraceCurrent;
            }
            this.mTraceX[this.mTraceCount] = x;
            this.mTraceY[this.mTraceCount] = y;
            this.mTraceCurrent[this.mTraceCount] = current;
            this.mTraceCount++;
        }
    }

    public PointerLocationView(Context c) {
        super(c);
        setFocusableInTouchMode(true);
        this.mIm = (InputManager) c.getSystemService(InputManager.class);
        this.mVC = ViewConfiguration.get(c);
        this.mTextPaint = new Paint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize(10.0f * getResources().getDisplayMetrics().density);
        this.mTextPaint.setARGB(255, 0, 0, 0);
        this.mTextBackgroundPaint = new Paint();
        this.mTextBackgroundPaint.setAntiAlias(false);
        this.mTextBackgroundPaint.setARGB(128, 255, 255, 255);
        this.mTextLevelPaint = new Paint();
        this.mTextLevelPaint.setAntiAlias(false);
        this.mTextLevelPaint.setARGB(192, 255, 0, 0);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setARGB(255, 255, 255, 255);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeWidth(2.0f);
        this.mCurrentPointPaint = new Paint();
        this.mCurrentPointPaint.setAntiAlias(true);
        this.mCurrentPointPaint.setARGB(255, 255, 0, 0);
        this.mCurrentPointPaint.setStyle(Paint.Style.STROKE);
        this.mCurrentPointPaint.setStrokeWidth(2.0f);
        this.mTargetPaint = new Paint();
        this.mTargetPaint.setAntiAlias(false);
        this.mTargetPaint.setARGB(255, 0, 0, 192);
        this.mPathPaint = new Paint();
        this.mPathPaint.setAntiAlias(false);
        this.mPathPaint.setARGB(255, 0, 96, 255);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeWidth(1.0f);
        this.mPointers.add(new PointerState());
        this.mActivePointerId = 0;
        this.mVelocity = VelocityTracker.obtain();
        String altStrategy = SystemProperties.get(ALT_STRATEGY_PROPERY_KEY);
        if (altStrategy.length() != 0) {
            Log.d(TAG, "Comparing default velocity tracker strategy with " + altStrategy);
            this.mAltVelocity = VelocityTracker.obtain(altStrategy);
            return;
        }
        this.mAltVelocity = null;
    }

    public void setPrintCoords(boolean state) {
        this.mPrintCoords = state;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mTextPaint.getFontMetricsInt(this.mTextMetrics);
        this.mHeaderBottom = (-this.mTextMetrics.ascent) + this.mTextMetrics.descent + 2;
    }

    private void drawOval(Canvas canvas, float x, float y, float major, float minor, float angle, Paint paint) {
        canvas.save(1);
        canvas.rotate((float) (((double) (180.0f * angle)) / 3.141592653589793d), x, y);
        this.mReusableOvalRect.left = x - (minor / 2.0f);
        this.mReusableOvalRect.right = (minor / 2.0f) + x;
        this.mReusableOvalRect.top = y - (major / 2.0f);
        this.mReusableOvalRect.bottom = (major / 2.0f) + y;
        canvas.drawOval(this.mReusableOvalRect, paint);
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0621  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0643 A[SYNTHETIC] */
    public void onDraw(Canvas canvas) {
        int i;
        int bottom;
        float orientationVectorY;
        int base;
        int itemW;
        int i2;
        int base2;
        float x;
        float f;
        Canvas canvas2 = canvas;
        int w = getWidth();
        int itemW2 = w / 8;
        int base3 = (-this.mTextMetrics.ascent) + 1;
        int bottom2 = this.mHeaderBottom;
        int NP = this.mPointers.size();
        if (this.mActivePointerId >= 0) {
            if (this.mActivePointerId >= NP) {
                if (HWFLOW) {
                    Log.i(TAG, "exception occur: mActivePointerId >= NP");
                }
                return;
            }
            Paint paint = this.mTextBackgroundPaint;
            PointerState ps = this.mPointers.get(this.mActivePointerId);
            canvas2.drawRect(0.0f, 0.0f, (float) (itemW2 - 1), (float) bottom2, paint);
            canvas2.drawText(this.mText.clear().append("P: ").append(this.mCurNumPointers).append(" / ").append(this.mMaxNumPointers).toString(), 1.0f, (float) base3, this.mTextPaint);
            int N = ps.mTraceCount;
            if ((!this.mCurDown || !ps.mCurDown) && N != 0) {
                f = 1.0f;
                float dx = ps.mTraceX[N - 1] - ps.mTraceX[0];
                float dy = (float) bottom2;
                float dy2 = ps.mTraceY[N - 1] - ps.mTraceY[0];
                canvas2.drawRect((float) itemW2, 0.0f, (float) ((itemW2 * 2) - 1), dy, Math.abs(dx) < ((float) this.mVC.getScaledTouchSlop()) ? this.mTextBackgroundPaint : this.mTextLevelPaint);
                canvas2.drawText(this.mText.clear().append("dX: ").append(dx, 1).toString(), (float) (1 + itemW2), (float) base3, this.mTextPaint);
                float dy3 = dy2;
                canvas2.drawRect((float) (itemW2 * 2), 0.0f, (float) ((itemW2 * 3) - 1), (float) bottom2, Math.abs(dy3) < ((float) this.mVC.getScaledTouchSlop()) ? this.mTextBackgroundPaint : this.mTextLevelPaint);
                canvas2.drawText(this.mText.clear().append("dY: ").append(dy3, 1).toString(), (float) ((itemW2 * 2) + 1), (float) base3, this.mTextPaint);
            } else {
                f = 1.0f;
                canvas2.drawRect((float) itemW2, 0.0f, (float) ((itemW2 * 2) - 1), (float) bottom2, this.mTextBackgroundPaint);
                canvas2.drawText(this.mText.clear().append("X: ").append(ps.mCoords.x, 1).toString(), (float) (1 + itemW2), (float) base3, this.mTextPaint);
                canvas2.drawRect((float) (itemW2 * 2), 0.0f, (float) ((itemW2 * 3) - 1), (float) bottom2, this.mTextBackgroundPaint);
                canvas2.drawText(this.mText.clear().append("Y: ").append(ps.mCoords.y, 1).toString(), (float) ((itemW2 * 2) + 1), (float) base3, this.mTextPaint);
            }
            canvas2.drawRect((float) (itemW2 * 3), 0.0f, (float) ((itemW2 * 4) - 1), (float) bottom2, this.mTextBackgroundPaint);
            canvas2.drawText(this.mText.clear().append("Xv: ").append(ps.mXVelocity, 3).toString(), (float) ((itemW2 * 3) + 1), (float) base3, this.mTextPaint);
            canvas2.drawRect((float) (itemW2 * 4), 0.0f, (float) ((itemW2 * 5) - 1), (float) bottom2, this.mTextBackgroundPaint);
            canvas2.drawText(this.mText.clear().append("Yv: ").append(ps.mYVelocity, 3).toString(), (float) (1 + (itemW2 * 4)), (float) base3, this.mTextPaint);
            canvas2.drawRect((float) (itemW2 * 5), 0.0f, (float) ((itemW2 * 6) - 1), (float) bottom2, this.mTextBackgroundPaint);
            canvas2.drawRect((float) (itemW2 * 5), 0.0f, (((float) (itemW2 * 5)) + (ps.mCoords.pressure * ((float) itemW2))) - f, (float) bottom2, this.mTextLevelPaint);
            canvas2.drawText(this.mText.clear().append("Prs: ").append(ps.mCoords.pressure, 2).toString(), (float) (1 + (itemW2 * 5)), (float) base3, this.mTextPaint);
            canvas2.drawRect((float) (itemW2 * 6), 0.0f, (float) ((itemW2 * 7) - 1), (float) bottom2, this.mTextBackgroundPaint);
            canvas2.drawRect((float) (itemW2 * 6), 0.0f, (((float) (itemW2 * 6)) + (ps.mCoords.size * ((float) itemW2))) - f, (float) bottom2, this.mTextLevelPaint);
            canvas2.drawText(this.mText.clear().append("Size: ").append(ps.mCoords.size, 2).toString(), (float) (1 + (itemW2 * 6)), (float) base3, this.mTextPaint);
            canvas2.drawRect((float) (itemW2 * 7), 0.0f, (float) w, (float) bottom2, this.mTextBackgroundPaint);
            canvas2.drawRect((float) (itemW2 * 7), 0.0f, (((float) (itemW2 * 7)) + (ps.mCoords.size * ((float) itemW2))) - f, (float) bottom2, this.mTextLevelPaint);
            canvas2.drawText(this.mText.clear().append("T: ").append((float) SystemClock.uptimeMillis(), 6).toString(), (float) (1 + (itemW2 * 7)), (float) base3, this.mTextPaint);
        }
        int p = 0;
        while (true) {
            int p2 = p;
            if (p2 < NP) {
                PointerState ps2 = this.mPointers.get(p2);
                int N2 = ps2.mTraceCount;
                int i3 = 128;
                int w2 = w;
                this.mPaint.setARGB(255, 128, 255, 255);
                float lastX = 0.0f;
                boolean haveLast = false;
                boolean drawn = false;
                int i4 = 0;
                float lastY = 0.0f;
                while (true) {
                    int i5 = i4;
                    if (i5 >= N2) {
                        break;
                    }
                    float x2 = ps2.mTraceX[i5];
                    float y = ps2.mTraceY[i5];
                    if (Float.isNaN(x2)) {
                        haveLast = false;
                        i2 = i5;
                        itemW = itemW2;
                        base = base3;
                        base2 = i3;
                    } else {
                        if (haveLast) {
                            x = x2;
                            i2 = i5;
                            float lastY2 = lastY;
                            itemW = itemW2;
                            float lastX2 = lastX;
                            base = base3;
                            base2 = i3;
                            canvas2.drawLine(lastX, lastY, x, y, this.mPathPaint);
                            canvas2.drawPoint(lastX2, lastY2, ps2.mTraceCurrent[i2] ? this.mCurrentPointPaint : this.mPaint);
                            drawn = true;
                        } else {
                            x = x2;
                            i2 = i5;
                            float f2 = lastY;
                            itemW = itemW2;
                            base = base3;
                            float f3 = lastX;
                            base2 = i3;
                        }
                        lastX = x;
                        lastY = y;
                        haveLast = true;
                    }
                    i4 = i2 + 1;
                    i3 = base2;
                    itemW2 = itemW;
                    base3 = base;
                }
                float lastY3 = lastY;
                int itemW3 = itemW2;
                int base4 = base3;
                float lastX3 = lastX;
                int base5 = i3;
                if (drawn) {
                    this.mPaint.setARGB(base5, base5, 0, base5);
                    int i6 = -3;
                    float lx = ps2.mEstimator.estimateX(-0.08f);
                    float ly = ps2.mEstimator.estimateY(-0.08f);
                    int i7 = -3;
                    while (true) {
                        int i8 = i7;
                        if (i8 > 2) {
                            break;
                        }
                        float x3 = ps2.mEstimator.estimateX(((float) i8) * 0.02f);
                        float y2 = ps2.mEstimator.estimateY(((float) i8) * 0.02f);
                        canvas2.drawLine(lx, ly, x3, y2, this.mPaint);
                        lx = x3;
                        ly = y2;
                        i7 = i8 + 1;
                    }
                    this.mPaint.setARGB(255, 255, 64, base5);
                    canvas2.drawLine(lastX3, lastY3, lastX3 + (ps2.mXVelocity * 16.0f), lastY3 + (ps2.mYVelocity * 16.0f), this.mPaint);
                    if (this.mAltVelocity != null) {
                        this.mPaint.setARGB(base5, 0, base5, base5);
                        float lx2 = ps2.mAltEstimator.estimateX(-0.08f);
                        float ly2 = ps2.mAltEstimator.estimateY(-0.08f);
                        while (true) {
                            int i9 = i6;
                            if (i9 > 2) {
                                break;
                            }
                            float x4 = ps2.mAltEstimator.estimateX(((float) i9) * 0.02f);
                            float y3 = ps2.mAltEstimator.estimateY(((float) i9) * 0.02f);
                            canvas2.drawLine(lx2, ly2, x4, y3, this.mPaint);
                            lx2 = x4;
                            ly2 = y3;
                            i6 = i9 + 1;
                        }
                        i = 2;
                        this.mPaint.setARGB(255, 64, 255, base5);
                        canvas2.drawLine(lastX3, lastY3, lastX3 + (ps2.mAltXVelocity * 16.0f), lastY3 + (ps2.mAltYVelocity * 16.0f), this.mPaint);
                        if (this.mCurDown || !ps2.mCurDown) {
                            bottom = bottom2;
                            int bottom3 = i;
                        } else {
                            canvas2.drawLine(0.0f, ps2.mCoords.y, (float) getWidth(), ps2.mCoords.y, this.mTargetPaint);
                            canvas2.drawLine(ps2.mCoords.x, 0.0f, ps2.mCoords.x, (float) getHeight(), this.mTargetPaint);
                            int pressureLevel = (int) (ps2.mCoords.pressure * 255.0f);
                            this.mPaint.setARGB(255, pressureLevel, 255, 255 - pressureLevel);
                            canvas2.drawPoint(ps2.mCoords.x, ps2.mCoords.y, this.mPaint);
                            this.mPaint.setARGB(255, pressureLevel, 255 - pressureLevel, base5);
                            float f4 = lastY3;
                            int pressureLevel2 = pressureLevel;
                            int i10 = N2;
                            float f5 = lastX3;
                            bottom = bottom2;
                            int bottom4 = i;
                            PointerState ps3 = ps2;
                            drawOval(canvas2, ps2.mCoords.x, ps2.mCoords.y, ps2.mCoords.touchMajor, ps2.mCoords.touchMinor, ps2.mCoords.orientation, this.mPaint);
                            this.mPaint.setARGB(255, pressureLevel2, 128, 255 - pressureLevel2);
                            drawOval(canvas2, ps3.mCoords.x, ps3.mCoords.y, ps3.mCoords.toolMajor, ps3.mCoords.toolMinor, ps3.mCoords.orientation, this.mPaint);
                            float arrowSize = ps3.mCoords.toolMajor * 0.7f;
                            if (arrowSize < 20.0f) {
                                arrowSize = 20.0f;
                            }
                            float arrowSize2 = arrowSize;
                            this.mPaint.setARGB(255, pressureLevel2, 255, 0);
                            float orientationVectorX = (float) (Math.sin((double) ps3.mCoords.orientation) * ((double) arrowSize2));
                            float orientationVectorY2 = (float) ((-Math.cos((double) ps3.mCoords.orientation)) * ((double) arrowSize2));
                            if (ps3.mToolType == bottom4) {
                                orientationVectorY = orientationVectorY2;
                            } else if (ps3.mToolType == 4) {
                                orientationVectorY = orientationVectorY2;
                            } else {
                                orientationVectorY = orientationVectorY2;
                                canvas2.drawLine(ps3.mCoords.x - orientationVectorX, ps3.mCoords.y - orientationVectorY2, ps3.mCoords.x + orientationVectorX, ps3.mCoords.y + orientationVectorY2, this.mPaint);
                                float tiltScale = (float) Math.sin((double) ps3.mCoords.getAxisValue(25));
                                canvas2.drawCircle(ps3.mCoords.x + (orientationVectorX * tiltScale), ps3.mCoords.y + (orientationVectorY * tiltScale), 3.0f, this.mPaint);
                                if (!ps3.mHasBoundingBox) {
                                    float f6 = tiltScale;
                                    canvas2.drawRect(ps3.mBoundingLeft, ps3.mBoundingTop, ps3.mBoundingRight, ps3.mBoundingBottom, this.mPaint);
                                }
                            }
                            canvas2.drawLine(ps3.mCoords.x, ps3.mCoords.y, ps3.mCoords.x + orientationVectorX, ps3.mCoords.y + orientationVectorY, this.mPaint);
                            float tiltScale2 = (float) Math.sin((double) ps3.mCoords.getAxisValue(25));
                            canvas2.drawCircle(ps3.mCoords.x + (orientationVectorX * tiltScale2), ps3.mCoords.y + (orientationVectorY * tiltScale2), 3.0f, this.mPaint);
                            if (!ps3.mHasBoundingBox) {
                            }
                        }
                        p = p2 + 1;
                        w = w2;
                        itemW2 = itemW3;
                        base3 = base4;
                        bottom2 = bottom;
                    }
                }
                i = 2;
                if (this.mCurDown) {
                }
                bottom = bottom2;
                int bottom32 = i;
                p = p2 + 1;
                w = w2;
                itemW2 = itemW3;
                base3 = base4;
                bottom2 = bottom;
            } else {
                int i11 = itemW2;
                int i12 = base3;
                int i13 = bottom2;
                return;
            }
        }
    }

    private void logMotionEvent(String type, MotionEvent event) {
        MotionEvent motionEvent = event;
        int action = event.getAction();
        int N = event.getHistorySize();
        int NI = event.getPointerCount();
        int i = 0;
        while (true) {
            int historyPos = i;
            if (historyPos >= N) {
                break;
            }
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 >= NI) {
                    break;
                }
                int id = motionEvent.getPointerId(i3);
                motionEvent.getHistoricalPointerCoords(i3, historyPos, this.mTempCoords);
                logCoords(type, action, i3, this.mTempCoords, id, motionEvent);
                i2 = i3 + 1;
            }
            i = historyPos + 1;
        }
        for (int i4 = 0; i4 < NI; i4++) {
            int id2 = motionEvent.getPointerId(i4);
            motionEvent.getPointerCoords(i4, this.mTempCoords);
            logCoords(type, action, i4, this.mTempCoords, id2, motionEvent);
        }
    }

    private void logCoords(String type, int action, int index, MotionEvent.PointerCoords coords, int id, MotionEvent event) {
        String prefix;
        int i = action;
        int i2 = index;
        MotionEvent.PointerCoords pointerCoords = coords;
        MotionEvent motionEvent = event;
        int toolType = motionEvent.getToolType(i2);
        int buttonState = event.getButtonState();
        switch (i & 255) {
            case 0:
                prefix = "DOWN";
                break;
            case 1:
                prefix = "UP";
                break;
            case 2:
                prefix = "MOVE";
                break;
            case 3:
                prefix = "CANCEL";
                break;
            case 4:
                prefix = "OUTSIDE";
                break;
            case 5:
                if (i2 != ((i & 65280) >> 8)) {
                    prefix = "MOVE";
                    break;
                } else {
                    prefix = "DOWN";
                    break;
                }
            case 6:
                if (i2 != ((i & 65280) >> 8)) {
                    prefix = "MOVE";
                    break;
                } else {
                    prefix = "UP";
                    break;
                }
            case 7:
                prefix = "HOVER MOVE";
                break;
            case 8:
                prefix = "SCROLL";
                break;
            case 9:
                prefix = "HOVER ENTER";
                break;
            case 10:
                prefix = "HOVER EXIT";
                break;
            default:
                prefix = Integer.toString(action);
                break;
        }
        Log.i(TAG, this.mText.clear().append(type).append(" id ").append(id + 1).append(": ").append(prefix).append(" (").append(pointerCoords.x, 3).append(", ").append(pointerCoords.y, 3).append(") Pressure=").append(pointerCoords.pressure, 3).append(" Size=").append(pointerCoords.size, 3).append(" TouchMajor=").append(pointerCoords.touchMajor, 3).append(" TouchMinor=").append(pointerCoords.touchMinor, 3).append(" ToolMajor=").append(pointerCoords.toolMajor, 3).append(" ToolMinor=").append(pointerCoords.toolMinor, 3).append(" Orientation=").append((float) (((double) (pointerCoords.orientation * 180.0f)) / 3.141592653589793d), 1).append("deg").append(" Tilt=").append((float) (((double) (pointerCoords.getAxisValue(25) * 180.0f)) / 3.141592653589793d), 1).append("deg").append(" Distance=").append(pointerCoords.getAxisValue(24), 1).append(" VScroll=").append(pointerCoords.getAxisValue(9), 1).append(" HScroll=").append(pointerCoords.getAxisValue(10), 1).append(" BoundingBox=[(").append(motionEvent.getAxisValue(32), 3).append(", ").append(motionEvent.getAxisValue(33), 3).append(")").append(", (").append(motionEvent.getAxisValue(34), 3).append(", ").append(motionEvent.getAxisValue(35), 3).append(")]").append(" ToolType=").append(MotionEvent.toolTypeToString(toolType)).append(" ButtonState=").append(MotionEvent.buttonStateToString(buttonState)).toString());
    }

    public void onPointerEvent(MotionEvent event) {
        MotionEvent.PointerCoords coords;
        PointerState ps;
        int id;
        char c;
        int N;
        int historyPos;
        int i;
        MotionEvent.PointerCoords coords2;
        PointerState ps2;
        MotionEvent motionEvent = event;
        int action = event.getAction();
        int NP = this.mPointers.size();
        int i2 = 1;
        if (action == 0 || (action & 255) == 5) {
            int index = (action & 65280) >> 8;
            if (action == 0) {
                for (int p = 0; p < NP; p++) {
                    PointerState ps3 = this.mPointers.get(p);
                    ps3.clearTrace();
                    boolean unused = ps3.mCurDown = false;
                }
                this.mCurDown = true;
                this.mCurNumPointers = 0;
                this.mMaxNumPointers = 0;
                this.mVelocity.clear();
                if (this.mAltVelocity != null) {
                    this.mAltVelocity.clear();
                }
            }
            this.mCurNumPointers++;
            if (this.mMaxNumPointers < this.mCurNumPointers) {
                this.mMaxNumPointers = this.mCurNumPointers;
            }
            int id2 = motionEvent.getPointerId(index);
            while (NP <= id2) {
                this.mPointers.add(new PointerState());
                NP++;
            }
            if (this.mActivePointerId < this.mPointers.size() && (this.mActivePointerId < 0 || !this.mPointers.get(this.mActivePointerId).mCurDown)) {
                this.mActivePointerId = id2;
            }
            PointerState ps4 = this.mPointers.get(id2);
            boolean unused2 = ps4.mCurDown = true;
            InputDevice device = InputDevice.getDevice(event.getDeviceId());
            boolean unused3 = ps4.mHasBoundingBox = (device == null || device.getMotionRange(32) == null) ? false : true;
        }
        int NP2 = NP;
        int NI = event.getPointerCount();
        this.mVelocity.addMovement(motionEvent);
        this.mVelocity.computeCurrentVelocity(1);
        if (this.mAltVelocity != null) {
            this.mAltVelocity.addMovement(motionEvent);
            this.mAltVelocity.computeCurrentVelocity(1);
        }
        int N2 = event.getHistorySize();
        int historyPos2 = 0;
        while (true) {
            int historyPos3 = historyPos2;
            if (historyPos3 >= N2) {
                break;
            }
            int i3 = 0;
            while (true) {
                int i4 = i3;
                if (i4 >= NI) {
                    break;
                }
                int id3 = motionEvent.getPointerId(i4);
                PointerState ps5 = null;
                if (id3 < this.mPointers.size()) {
                    ps5 = this.mCurDown ? this.mPointers.get(id3) : null;
                }
                PointerState ps6 = ps5;
                MotionEvent.PointerCoords coords3 = ps6 != null ? ps6.mCoords : this.mTempCoords;
                motionEvent.getHistoricalPointerCoords(i4, historyPos3, coords3);
                if (this.mPrintCoords) {
                    coords2 = coords3;
                    ps2 = ps6;
                    i = i4;
                    historyPos = historyPos3;
                    N = N2;
                    logCoords(TAG, action, i4, coords2, id3, motionEvent);
                } else {
                    coords2 = coords3;
                    ps2 = ps6;
                    int i5 = id3;
                    i = i4;
                    historyPos = historyPos3;
                    N = N2;
                }
                if (ps2 != null) {
                    MotionEvent.PointerCoords coords4 = coords2;
                    ps2.addTrace(coords4.x, coords4.y, false);
                }
                i3 = i + 1;
                historyPos3 = historyPos;
                N2 = N;
            }
            int i6 = N2;
            historyPos2 = historyPos3 + 1;
        }
        int i7 = 0;
        while (true) {
            int i8 = i7;
            if (i8 >= NI) {
                break;
            }
            int id4 = motionEvent.getPointerId(i8);
            PointerState ps7 = null;
            if (id4 < this.mPointers.size()) {
                ps7 = this.mCurDown ? this.mPointers.get(id4) : null;
            }
            PointerState ps8 = ps7;
            MotionEvent.PointerCoords coords5 = ps8 != null ? ps8.mCoords : this.mTempCoords;
            motionEvent.getPointerCoords(i8, coords5);
            if (this.mPrintCoords) {
                coords = coords5;
                ps = ps8;
                id = id4;
                logCoords(TAG, action, i8, coords5, id4, motionEvent);
            } else {
                coords = coords5;
                ps = ps8;
                id = id4;
            }
            if (ps != null) {
                MotionEvent.PointerCoords coords6 = coords;
                ps.addTrace(coords6.x, coords6.y, true);
                float unused4 = ps.mXVelocity = this.mVelocity.getXVelocity(id);
                float unused5 = ps.mYVelocity = this.mVelocity.getYVelocity(id);
                this.mVelocity.getEstimator(id, ps.mEstimator);
                if (this.mAltVelocity != null) {
                    float unused6 = ps.mAltXVelocity = this.mAltVelocity.getXVelocity(id);
                    float unused7 = ps.mAltYVelocity = this.mAltVelocity.getYVelocity(id);
                    this.mAltVelocity.getEstimator(id, ps.mAltEstimator);
                }
                int unused8 = ps.mToolType = motionEvent.getToolType(i8);
                if (ps.mHasBoundingBox) {
                    c = ' ';
                    float unused9 = ps.mBoundingLeft = motionEvent.getAxisValue(32, i8);
                    float unused10 = ps.mBoundingTop = motionEvent.getAxisValue(33, i8);
                    float unused11 = ps.mBoundingRight = motionEvent.getAxisValue(34, i8);
                    float unused12 = ps.mBoundingBottom = motionEvent.getAxisValue(35, i8);
                    i7 = i8 + 1;
                    char c2 = c;
                }
            }
            c = ' ';
            i7 = i8 + 1;
            char c22 = c;
        }
        if (action == 1 || action == 3 || (action & 255) == 6) {
            int index2 = (65280 & action) >> 8;
            int id5 = motionEvent.getPointerId(index2);
            if (id5 >= NP2) {
                Slog.wtf(TAG, "Got pointer ID out of bounds: id=" + id5 + " arraysize=" + NP2 + " pointerindex=" + index2 + " action=0x" + Integer.toHexString(action));
                return;
            } else if (id5 < this.mPointers.size()) {
                PointerState ps9 = this.mPointers.get(id5);
                boolean unused13 = ps9.mCurDown = false;
                if (action == 1 || action == 3) {
                    this.mCurDown = false;
                    this.mCurNumPointers = 0;
                } else {
                    this.mCurNumPointers--;
                    if (this.mActivePointerId == id5) {
                        if (index2 != 0) {
                            i2 = 0;
                        }
                        this.mActivePointerId = motionEvent.getPointerId(i2);
                    }
                    ps9.addTrace(Float.NaN, Float.NaN, false);
                }
            } else {
                return;
            }
        }
        invalidate();
    }

    public boolean onTouchEvent(MotionEvent event) {
        onPointerEvent(event);
        if (event.getAction() == 0 && !isFocused()) {
            requestFocus();
        }
        return true;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        int source = event.getSource();
        if ((source & 2) != 0) {
            onPointerEvent(event);
        } else if ((source & 16) != 0) {
            logMotionEvent("Joystick", event);
        } else if ((source & 8) != 0) {
            logMotionEvent("Position", event);
        } else {
            logMotionEvent("Generic", event);
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!shouldLogKey(keyCode)) {
            return super.onKeyDown(keyCode, event);
        }
        int repeatCount = event.getRepeatCount();
        if (repeatCount == 0) {
            Log.i(TAG, "Key Down: " + event);
        } else {
            Log.i(TAG, "Key Repeat #" + repeatCount + ": " + event);
        }
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!shouldLogKey(keyCode)) {
            return super.onKeyUp(keyCode, event);
        }
        Log.i(TAG, "Key Up: " + event);
        return true;
    }

    private static boolean shouldLogKey(int keyCode) {
        boolean z = true;
        switch (keyCode) {
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
                return true;
            default:
                if (!KeyEvent.isGamepadButton(keyCode) && !KeyEvent.isModifierKey(keyCode)) {
                    z = false;
                }
                return z;
        }
    }

    public boolean onTrackballEvent(MotionEvent event) {
        logMotionEvent("Trackball", event);
        return true;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIm.registerInputDeviceListener(this, getHandler());
        logInputDevices();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIm.unregisterInputDeviceListener(this);
    }

    public void onInputDeviceAdded(int deviceId) {
        logInputDeviceState(deviceId, "Device Added");
    }

    public void onInputDeviceChanged(int deviceId) {
        logInputDeviceState(deviceId, "Device Changed");
    }

    public void onInputDeviceRemoved(int deviceId) {
        logInputDeviceState(deviceId, "Device Removed");
    }

    private void logInputDevices() {
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int logInputDeviceState : deviceIds) {
            logInputDeviceState(logInputDeviceState, "Device Enumerated");
        }
    }

    private void logInputDeviceState(int deviceId, String state) {
        InputDevice device = this.mIm.getInputDevice(deviceId);
        if (device != null) {
            Log.i(TAG, state + ": " + device);
            return;
        }
        Log.i(TAG, state + ": " + deviceId);
    }
}
