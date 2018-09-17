package com.android.internal.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.VelocityTracker;
import android.view.VelocityTracker.Estimator;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.PointerEventListener;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.hisi.perfhub.PerfHub;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;

public class PointerLocationView extends View implements InputDeviceListener, PointerEventListener {
    private static final String ALT_STRATEGY_PROPERY_KEY = "debug.velocitytracker.alt";
    protected static final boolean HWFLOW = false;
    private static final String TAG = "Pointer";
    private final int ESTIMATE_FUTURE_POINTS;
    private final float ESTIMATE_INTERVAL;
    private final int ESTIMATE_PAST_POINTS;
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
    private final ArrayList<PointerState> mPointers;
    private boolean mPrintCoords;
    private RectF mReusableOvalRect;
    private final Paint mTargetPaint;
    private final PointerCoords mTempCoords;
    private final FasterStringBuilder mText;
    private final Paint mTextBackgroundPaint;
    private final Paint mTextLevelPaint;
    private final FontMetricsInt mTextMetrics;
    private final Paint mTextPaint;
    private final ViewConfiguration mVC;
    private final VelocityTracker mVelocity;

    private static final class FasterStringBuilder {
        private char[] mChars;
        private int mLength;

        public FasterStringBuilder() {
            this.mChars = new char[64];
        }

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
            boolean negative = PointerLocationView.HWFLOW;
            if (value < 0) {
                negative = true;
            }
            if (negative) {
                value = -value;
                if (value < 0) {
                    append("-2147483648");
                    return this;
                }
            }
            int index = reserve(11);
            char[] chars = this.mChars;
            if (value == 0) {
                int index2 = index + 1;
                chars[index] = '0';
                this.mLength++;
                return this;
            }
            if (negative) {
                index2 = index + 1;
                chars[index] = '-';
                index = index2;
            }
            int divisor = 1000000000;
            int numberWidth = 10;
            index2 = index;
            while (value < divisor) {
                divisor /= 10;
                numberWidth--;
                if (numberWidth < zeroPadWidth) {
                    index = index2 + 1;
                    chars[index2] = '0';
                } else {
                    index = index2;
                }
                index2 = index;
            }
            do {
                index = index2;
                int digit = value / divisor;
                value -= digit * divisor;
                divisor /= 10;
                index2 = index + 1;
                chars[index] = (char) (digit + 48);
            } while (divisor != 0);
            this.mLength = index2;
            return this;
        }

        public FasterStringBuilder append(float value, int precision) {
            int scale = 1;
            for (int i = 0; i < precision; i++) {
                scale *= 10;
            }
            value = (float) (Math.rint((double) (((float) scale) * value)) / ((double) scale));
            append((int) value);
            if (precision != 0) {
                append(".");
                value = Math.abs(value);
                append((int) (((float) scale) * ((float) (((double) value) - Math.floor((double) value)))), precision);
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
        private Estimator mAltEstimator;
        private float mAltXVelocity;
        private float mAltYVelocity;
        private float mBoundingBottom;
        private float mBoundingLeft;
        private float mBoundingRight;
        private float mBoundingTop;
        private PointerCoords mCoords;
        private boolean mCurDown;
        private Estimator mEstimator;
        private boolean mHasBoundingBox;
        private int mToolType;
        private int mTraceCount;
        private boolean[] mTraceCurrent;
        private float[] mTraceX;
        private float[] mTraceY;
        private float mXVelocity;
        private float mYVelocity;

        public PointerState() {
            this.mTraceX = new float[32];
            this.mTraceY = new float[32];
            this.mTraceCurrent = new boolean[32];
            this.mCoords = new PointerCoords();
            this.mEstimator = new Estimator();
            this.mAltEstimator = new Estimator();
        }

        public void clearTrace() {
            this.mTraceCount = 0;
        }

        public void addTrace(float x, float y, boolean current) {
            int traceCapacity = this.mTraceX.length;
            if (this.mTraceCount == traceCapacity) {
                traceCapacity *= 2;
                float[] newTraceX = new float[traceCapacity];
                System.arraycopy(this.mTraceX, 0, newTraceX, 0, this.mTraceCount);
                this.mTraceX = newTraceX;
                float[] newTraceY = new float[traceCapacity];
                System.arraycopy(this.mTraceY, 0, newTraceY, 0, this.mTraceCount);
                this.mTraceY = newTraceY;
                boolean[] newTraceCurrent = new boolean[traceCapacity];
                System.arraycopy(this.mTraceCurrent, 0, newTraceCurrent, 0, this.mTraceCount);
                this.mTraceCurrent = newTraceCurrent;
            }
            this.mTraceX[this.mTraceCount] = x;
            this.mTraceY[this.mTraceCount] = y;
            this.mTraceCurrent[this.mTraceCount] = current;
            this.mTraceCount++;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.PointerLocationView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.PointerLocationView.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.PointerLocationView.<clinit>():void");
    }

    public PointerLocationView(Context c) {
        super(c);
        this.ESTIMATE_PAST_POINTS = 4;
        this.ESTIMATE_FUTURE_POINTS = 2;
        this.ESTIMATE_INTERVAL = 0.02f;
        this.mTextMetrics = new FontMetricsInt();
        this.mPointers = new ArrayList();
        this.mTempCoords = new PointerCoords();
        this.mText = new FasterStringBuilder();
        this.mPrintCoords = true;
        this.mReusableOvalRect = new RectF();
        setFocusableInTouchMode(true);
        this.mIm = (InputManager) c.getSystemService(InputManager.class);
        this.mVC = ViewConfiguration.get(c);
        this.mTextPaint = new Paint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize(getResources().getDisplayMetrics().density * 10.0f);
        this.mTextPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 0, 0, 0);
        this.mTextBackgroundPaint = new Paint();
        this.mTextBackgroundPaint.setAntiAlias(HWFLOW);
        this.mTextBackgroundPaint.setARGB(LogPower.START_CHG_ROTATION, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
        this.mTextLevelPaint = new Paint();
        this.mTextLevelPaint.setAntiAlias(HWFLOW);
        this.mTextLevelPaint.setARGB(MetricsEvent.ACTION_LS_NOTE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 0, 0);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth(2.0f);
        this.mCurrentPointPaint = new Paint();
        this.mCurrentPointPaint.setAntiAlias(true);
        this.mCurrentPointPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 0, 0);
        this.mCurrentPointPaint.setStyle(Style.STROKE);
        this.mCurrentPointPaint.setStrokeWidth(2.0f);
        this.mTargetPaint = new Paint();
        this.mTargetPaint.setAntiAlias(HWFLOW);
        this.mTargetPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 0, 0, MetricsEvent.ACTION_LS_NOTE);
        this.mPathPaint = new Paint();
        this.mPathPaint.setAntiAlias(HWFLOW);
        this.mPathPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 0, 96, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mTextPaint.getFontMetricsInt(this.mTextMetrics);
        this.mHeaderBottom = ((-this.mTextMetrics.ascent) + this.mTextMetrics.descent) + 2;
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

    protected void onDraw(Canvas canvas) {
        PointerState ps;
        int N;
        int w = getWidth();
        int itemW = w / 8;
        int base = (-this.mTextMetrics.ascent) + 1;
        int bottom = this.mHeaderBottom;
        int NP = this.mPointers.size();
        if (this.mActivePointerId >= 0) {
            if (this.mActivePointerId >= NP) {
                if (HWFLOW) {
                    Log.i(TAG, "exception occur: mActivePointerId >= NP");
                }
                return;
            }
            ps = (PointerState) this.mPointers.get(this.mActivePointerId);
            canvas.drawRect(0.0f, 0.0f, (float) (itemW - 1), (float) bottom, this.mTextBackgroundPaint);
            canvas.drawText(this.mText.clear().append("P: ").append(this.mCurNumPointers).append(" / ").append(this.mMaxNumPointers).toString(), LayoutParams.BRIGHTNESS_OVERRIDE_FULL, (float) base, this.mTextPaint);
            N = ps.mTraceCount;
            if ((this.mCurDown && ps.mCurDown) || N == 0) {
                canvas.drawRect((float) itemW, 0.0f, (float) ((itemW * 2) - 1), (float) bottom, this.mTextBackgroundPaint);
                canvas.drawText(this.mText.clear().append("X: ").append(ps.mCoords.x, 1).toString(), (float) (itemW + 1), (float) base, this.mTextPaint);
                canvas.drawRect((float) (itemW * 2), 0.0f, (float) ((itemW * 3) - 1), (float) bottom, this.mTextBackgroundPaint);
                canvas.drawText(this.mText.clear().append("Y: ").append(ps.mCoords.y, 1).toString(), (float) ((itemW * 2) + 1), (float) base, this.mTextPaint);
            } else {
                Paint paint;
                float dx = ps.mTraceX[N - 1] - ps.mTraceX[0];
                float dy = ps.mTraceY[N - 1] - ps.mTraceY[0];
                canvas.drawRect((float) itemW, 0.0f, (float) ((itemW * 2) - 1), (float) bottom, Math.abs(dx) < ((float) this.mVC.getScaledTouchSlop()) ? this.mTextBackgroundPaint : this.mTextLevelPaint);
                canvas.drawText(this.mText.clear().append("dX: ").append(dx, 1).toString(), (float) (itemW + 1), (float) base, this.mTextPaint);
                float f = (float) (itemW * 2);
                float f2 = (float) ((itemW * 3) - 1);
                float f3 = (float) bottom;
                if (Math.abs(dy) < ((float) this.mVC.getScaledTouchSlop())) {
                    paint = this.mTextBackgroundPaint;
                } else {
                    paint = this.mTextLevelPaint;
                }
                canvas.drawRect(f, 0.0f, f2, f3, paint);
                canvas.drawText(this.mText.clear().append("dY: ").append(dy, 1).toString(), (float) ((itemW * 2) + 1), (float) base, this.mTextPaint);
            }
            canvas.drawRect((float) (itemW * 3), 0.0f, (float) ((itemW * 4) - 1), (float) bottom, this.mTextBackgroundPaint);
            canvas.drawText(this.mText.clear().append("Xv: ").append(ps.mXVelocity, 3).toString(), (float) ((itemW * 3) + 1), (float) base, this.mTextPaint);
            canvas.drawRect((float) (itemW * 4), 0.0f, (float) ((itemW * 5) - 1), (float) bottom, this.mTextBackgroundPaint);
            canvas.drawText(this.mText.clear().append("Yv: ").append(ps.mYVelocity, 3).toString(), (float) ((itemW * 4) + 1), (float) base, this.mTextPaint);
            canvas.drawRect((float) (itemW * 5), 0.0f, (float) ((itemW * 6) - 1), (float) bottom, this.mTextBackgroundPaint);
            canvas.drawRect((float) (itemW * 5), 0.0f, (((float) (itemW * 5)) + (ps.mCoords.pressure * ((float) itemW))) - LayoutParams.BRIGHTNESS_OVERRIDE_FULL, (float) bottom, this.mTextLevelPaint);
            canvas.drawText(this.mText.clear().append("Prs: ").append(ps.mCoords.pressure, 2).toString(), (float) ((itemW * 5) + 1), (float) base, this.mTextPaint);
            canvas.drawRect((float) (itemW * 6), 0.0f, (float) ((itemW * 7) - 1), (float) bottom, this.mTextBackgroundPaint);
            canvas.drawRect((float) (itemW * 6), 0.0f, (((float) (itemW * 6)) + (ps.mCoords.size * ((float) itemW))) - LayoutParams.BRIGHTNESS_OVERRIDE_FULL, (float) bottom, this.mTextLevelPaint);
            canvas.drawText(this.mText.clear().append("Size: ").append(ps.mCoords.size, 2).toString(), (float) ((itemW * 6) + 1), (float) base, this.mTextPaint);
            canvas.drawRect((float) (itemW * 7), 0.0f, (float) w, (float) bottom, this.mTextBackgroundPaint);
            canvas.drawRect((float) (itemW * 7), 0.0f, (((float) (itemW * 7)) + (ps.mCoords.size * ((float) itemW))) - LayoutParams.BRIGHTNESS_OVERRIDE_FULL, (float) bottom, this.mTextLevelPaint);
            canvas.drawText(this.mText.clear().append("T: ").append((float) SystemClock.uptimeMillis(), 6).toString(), (float) ((itemW * 7) + 1), (float) base, this.mTextPaint);
        }
        for (int p = 0; p < NP; p++) {
            int i;
            ps = (PointerState) this.mPointers.get(p);
            N = ps.mTraceCount;
            f = 0.0f;
            float lastY = 0.0f;
            boolean haveLast = HWFLOW;
            boolean drawn = HWFLOW;
            this.mPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, LogPower.START_CHG_ROTATION, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
            for (i = 0; i < N; i++) {
                f2 = ps.mTraceX[i];
                f3 = ps.mTraceY[i];
                if (Float.isNaN(f2)) {
                    haveLast = HWFLOW;
                } else {
                    if (haveLast) {
                        canvas.drawLine(f, lastY, f2, f3, this.mPathPaint);
                        canvas.drawPoint(f, lastY, ps.mTraceCurrent[i] ? this.mCurrentPointPaint : this.mPaint);
                        drawn = true;
                    }
                    f = f2;
                    lastY = f3;
                    haveLast = true;
                }
            }
            if (drawn) {
                this.mPaint.setARGB(LogPower.START_CHG_ROTATION, LogPower.START_CHG_ROTATION, 0, LogPower.START_CHG_ROTATION);
                float lx = ps.mEstimator.estimateX(-0.08f);
                float ly = ps.mEstimator.estimateY(-0.08f);
                for (i = -3; i <= 2; i++) {
                    f2 = ps.mEstimator.estimateX(((float) i) * 0.02f);
                    f3 = ps.mEstimator.estimateY(((float) i) * 0.02f);
                    canvas.drawLine(lx, ly, f2, f3, this.mPaint);
                    lx = f2;
                    ly = f3;
                }
                this.mPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 64, LogPower.START_CHG_ROTATION);
                canvas.drawLine(f, lastY, f + (ps.mXVelocity * 16.0f), lastY + (ps.mYVelocity * 16.0f), this.mPaint);
                if (this.mAltVelocity != null) {
                    this.mPaint.setARGB(LogPower.START_CHG_ROTATION, 0, LogPower.START_CHG_ROTATION, LogPower.START_CHG_ROTATION);
                    lx = ps.mAltEstimator.estimateX(-0.08f);
                    ly = ps.mAltEstimator.estimateY(-0.08f);
                    for (i = -3; i <= 2; i++) {
                        f2 = ps.mAltEstimator.estimateX(((float) i) * 0.02f);
                        f3 = ps.mAltEstimator.estimateY(((float) i) * 0.02f);
                        canvas.drawLine(lx, ly, f2, f3, this.mPaint);
                        lx = f2;
                        ly = f3;
                    }
                    this.mPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 64, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, LogPower.START_CHG_ROTATION);
                    canvas.drawLine(f, lastY, f + (ps.mAltXVelocity * 16.0f), lastY + (ps.mAltYVelocity * 16.0f), this.mPaint);
                }
            }
            if (this.mCurDown && ps.mCurDown) {
                canvas.drawLine(0.0f, ps.mCoords.y, (float) getWidth(), ps.mCoords.y, this.mTargetPaint);
                canvas.drawLine(ps.mCoords.x, 0.0f, ps.mCoords.x, (float) getHeight(), this.mTargetPaint);
                int pressureLevel = (int) (ps.mCoords.pressure * 255.0f);
                this.mPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, pressureLevel, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 255 - pressureLevel);
                canvas.drawPoint(ps.mCoords.x, ps.mCoords.y, this.mPaint);
                this.mPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, pressureLevel, 255 - pressureLevel, LogPower.START_CHG_ROTATION);
                drawOval(canvas, ps.mCoords.x, ps.mCoords.y, ps.mCoords.touchMajor, ps.mCoords.touchMinor, ps.mCoords.orientation, this.mPaint);
                this.mPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, pressureLevel, LogPower.START_CHG_ROTATION, 255 - pressureLevel);
                drawOval(canvas, ps.mCoords.x, ps.mCoords.y, ps.mCoords.toolMajor, ps.mCoords.toolMinor, ps.mCoords.orientation, this.mPaint);
                float arrowSize = ps.mCoords.toolMajor * 0.7f;
                if (arrowSize < 20.0f) {
                    arrowSize = 20.0f;
                }
                this.mPaint.setARGB(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, pressureLevel, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, 0);
                float orientationVectorX = (float) (Math.sin((double) ps.mCoords.orientation) * ((double) arrowSize));
                float orientationVectorY = (float) ((-Math.cos((double) ps.mCoords.orientation)) * ((double) arrowSize));
                if (ps.mToolType == 2 || ps.mToolType == 4) {
                    canvas.drawLine(ps.mCoords.x, ps.mCoords.y, ps.mCoords.x + orientationVectorX, ps.mCoords.y + orientationVectorY, this.mPaint);
                } else {
                    canvas.drawLine(ps.mCoords.x - orientationVectorX, ps.mCoords.y - orientationVectorY, ps.mCoords.x + orientationVectorX, ps.mCoords.y + orientationVectorY, this.mPaint);
                }
                float tiltScale = (float) Math.sin((double) ps.mCoords.getAxisValue(25));
                canvas.drawCircle(ps.mCoords.x + (orientationVectorX * tiltScale), ps.mCoords.y + (orientationVectorY * tiltScale), 3.0f, this.mPaint);
                if (ps.mHasBoundingBox) {
                    canvas.drawRect(ps.mBoundingLeft, ps.mBoundingTop, ps.mBoundingRight, ps.mBoundingBottom, this.mPaint);
                }
            }
        }
    }

    private void logMotionEvent(String type, MotionEvent event) {
        int i;
        int action = event.getAction();
        int N = event.getHistorySize();
        int NI = event.getPointerCount();
        for (int historyPos = 0; historyPos < N; historyPos++) {
            for (i = 0; i < NI; i++) {
                int id = event.getPointerId(i);
                event.getHistoricalPointerCoords(i, historyPos, this.mTempCoords);
                logCoords(type, action, i, this.mTempCoords, id, event);
            }
        }
        for (i = 0; i < NI; i++) {
            id = event.getPointerId(i);
            event.getPointerCoords(i, this.mTempCoords);
            logCoords(type, action, i, this.mTempCoords, id, event);
        }
    }

    private void logCoords(String type, int action, int index, PointerCoords coords, int id, MotionEvent event) {
        String prefix;
        int toolType = event.getToolType(index);
        int buttonState = event.getButtonState();
        switch (action & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                prefix = "DOWN";
                break;
            case HwCfgFilePolicy.EMUI /*1*/:
                prefix = "UP";
                break;
            case HwCfgFilePolicy.PC /*2*/:
                prefix = "MOVE";
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                prefix = "CANCEL";
                break;
            case HwCfgFilePolicy.CUST /*4*/:
                prefix = "OUTSIDE";
                break;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                if (index != ((MotionEvent.ACTION_POINTER_INDEX_MASK & action) >> 8)) {
                    prefix = "MOVE";
                    break;
                } else {
                    prefix = "DOWN";
                    break;
                }
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                if (index != ((MotionEvent.ACTION_POINTER_INDEX_MASK & action) >> 8)) {
                    prefix = "MOVE";
                    break;
                } else {
                    prefix = "UP";
                    break;
                }
            case HwCfgFilePolicy.CLOUD_APN /*7*/:
                prefix = "HOVER MOVE";
                break;
            case PGSdk.TYPE_VIDEO /*8*/:
                prefix = "SCROLL";
                break;
            case PGSdk.TYPE_SCRLOCK /*9*/:
                prefix = "HOVER ENTER";
                break;
            case PGSdk.TYPE_CLOCK /*10*/:
                prefix = "HOVER EXIT";
                break;
            default:
                prefix = Integer.toString(action);
                break;
        }
        Log.i(TAG, this.mText.clear().append(type).append(" id ").append(id + 1).append(": ").append(prefix).append(" (").append(coords.x, 3).append(", ").append(coords.y, 3).append(") Pressure=").append(coords.pressure, 3).append(" Size=").append(coords.size, 3).append(" TouchMajor=").append(coords.touchMajor, 3).append(" TouchMinor=").append(coords.touchMinor, 3).append(" ToolMajor=").append(coords.toolMajor, 3).append(" ToolMinor=").append(coords.toolMinor, 3).append(" Orientation=").append((float) (((double) (coords.orientation * 180.0f)) / 3.141592653589793d), 1).append("deg").append(" Tilt=").append((float) (((double) (coords.getAxisValue(25) * 180.0f)) / 3.141592653589793d), 1).append("deg").append(" Distance=").append(coords.getAxisValue(24), 1).append(" VScroll=").append(coords.getAxisValue(9), 1).append(" HScroll=").append(coords.getAxisValue(10), 1).append(" BoundingBox=[(").append(event.getAxisValue(32), 3).append(", ").append(event.getAxisValue(33), 3).append(")").append(", (").append(event.getAxisValue(34), 3).append(", ").append(event.getAxisValue(35), 3).append(")]").append(" ToolType=").append(MotionEvent.toolTypeToString(toolType)).append(" ButtonState=").append(MotionEvent.buttonStateToString(buttonState)).toString());
    }

    public void onPointerEvent(MotionEvent event) {
        int index;
        PointerState ps;
        int id;
        int i;
        int action = event.getAction();
        int NP = this.mPointers.size();
        if (action == 0 || (action & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) == 5) {
            index = (MotionEvent.ACTION_POINTER_INDEX_MASK & action) >> 8;
            if (action == 0) {
                for (int p = 0; p < NP; p++) {
                    ps = (PointerState) this.mPointers.get(p);
                    ps.clearTrace();
                    ps.mCurDown = HWFLOW;
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
            id = event.getPointerId(index);
            while (NP <= id) {
                this.mPointers.add(new PointerState());
                NP++;
            }
            if (this.mActivePointerId < this.mPointers.size() && (this.mActivePointerId < 0 || !((PointerState) this.mPointers.get(this.mActivePointerId)).mCurDown)) {
                this.mActivePointerId = id;
            }
            ps = (PointerState) this.mPointers.get(id);
            ps.mCurDown = true;
            InputDevice device = InputDevice.getDevice(event.getDeviceId());
            boolean z = device != null ? device.getMotionRange(32) != null ? true : HWFLOW : HWFLOW;
            ps.mHasBoundingBox = z;
        }
        int NI = event.getPointerCount();
        this.mVelocity.addMovement(event);
        this.mVelocity.computeCurrentVelocity(1);
        if (this.mAltVelocity != null) {
            this.mAltVelocity.addMovement(event);
            this.mAltVelocity.computeCurrentVelocity(1);
        }
        int N = event.getHistorySize();
        for (int historyPos = 0; historyPos < N; historyPos++) {
            for (i = 0; i < NI; i++) {
                PointerCoords coords;
                id = event.getPointerId(i);
                ps = null;
                if (id < this.mPointers.size()) {
                    ps = this.mCurDown ? (PointerState) this.mPointers.get(id) : null;
                }
                if (ps != null) {
                    coords = ps.mCoords;
                } else {
                    coords = this.mTempCoords;
                }
                event.getHistoricalPointerCoords(i, historyPos, coords);
                if (this.mPrintCoords) {
                    logCoords(TAG, action, i, coords, id, event);
                }
                if (ps != null) {
                    ps.addTrace(coords.x, coords.y, HWFLOW);
                }
            }
        }
        for (i = 0; i < NI; i++) {
            id = event.getPointerId(i);
            ps = null;
            if (id < this.mPointers.size()) {
                ps = this.mCurDown ? (PointerState) this.mPointers.get(id) : null;
            }
            if (ps != null) {
                coords = ps.mCoords;
            } else {
                coords = this.mTempCoords;
            }
            event.getPointerCoords(i, coords);
            if (this.mPrintCoords) {
                logCoords(TAG, action, i, coords, id, event);
            }
            if (ps != null) {
                ps.addTrace(coords.x, coords.y, true);
                ps.mXVelocity = this.mVelocity.getXVelocity(id);
                ps.mYVelocity = this.mVelocity.getYVelocity(id);
                this.mVelocity.getEstimator(id, ps.mEstimator);
                if (this.mAltVelocity != null) {
                    ps.mAltXVelocity = this.mAltVelocity.getXVelocity(id);
                    ps.mAltYVelocity = this.mAltVelocity.getYVelocity(id);
                    this.mAltVelocity.getEstimator(id, ps.mAltEstimator);
                }
                ps.mToolType = event.getToolType(i);
                if (ps.mHasBoundingBox) {
                    ps.mBoundingLeft = event.getAxisValue(32, i);
                    ps.mBoundingTop = event.getAxisValue(33, i);
                    ps.mBoundingRight = event.getAxisValue(34, i);
                    ps.mBoundingBottom = event.getAxisValue(35, i);
                }
            }
        }
        if (!(action == 1 || action == 3)) {
            if ((action & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) == 6) {
            }
            invalidate();
        }
        index = (MotionEvent.ACTION_POINTER_INDEX_MASK & action) >> 8;
        id = event.getPointerId(index);
        if (id < this.mPointers.size()) {
            ps = (PointerState) this.mPointers.get(id);
            ps.mCurDown = HWFLOW;
            if (action == 1 || action == 3) {
                this.mCurDown = HWFLOW;
                this.mCurNumPointers = 0;
                invalidate();
            }
            this.mCurNumPointers--;
            if (this.mActivePointerId == id) {
                this.mActivePointerId = event.getPointerId(index == 0 ? 1 : 0);
            }
            ps.addTrace(Float.NaN, Float.NaN, HWFLOW);
            invalidate();
        }
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
            case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
            case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
            case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
                return true;
            default:
                if (!KeyEvent.isGamepadButton(keyCode)) {
                    z = KeyEvent.isModifierKey(keyCode);
                }
                return z;
        }
    }

    public boolean onTrackballEvent(MotionEvent event) {
        logMotionEvent("Trackball", event);
        return true;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIm.registerInputDeviceListener(this, getHandler());
        logInputDevices();
    }

    protected void onDetachedFromWindow() {
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
        } else {
            Log.i(TAG, state + ": " + deviceId);
        }
    }
}
