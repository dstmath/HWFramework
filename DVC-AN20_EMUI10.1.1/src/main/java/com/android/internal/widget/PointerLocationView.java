package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.ISystemGestureExclusionListener;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowInsets;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicyConstants;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.widget.PointerLocationView;
import java.util.ArrayList;

public class PointerLocationView extends View implements InputManager.InputDeviceListener, WindowManagerPolicyConstants.PointerEventListener {
    private static final String ALT_STRATEGY_PROPERY_KEY = "debug.velocitytracker.alt";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "Pointer";
    private int mActivePointerId;
    private final VelocityTracker mAltVelocity;
    @UnsupportedAppUsage
    private boolean mCurDown;
    @UnsupportedAppUsage
    private int mCurNumPointers;
    private final Paint mCurrentPointPaint;
    private int mHeaderBottom;
    private int mHeaderPaddingTop = 0;
    private final InputManager mIm;
    @UnsupportedAppUsage
    private int mMaxNumPointers;
    private final Paint mPaint;
    private final Paint mPathPaint;
    @UnsupportedAppUsage
    private final ArrayList<PointerState> mPointers = new ArrayList<>();
    @UnsupportedAppUsage
    private boolean mPrintCoords = true;
    private RectF mReusableOvalRect = new RectF();
    private final Region mSystemGestureExclusion = new Region();
    private ISystemGestureExclusionListener mSystemGestureExclusionListener = new ISystemGestureExclusionListener.Stub() {
        /* class com.android.internal.widget.PointerLocationView.AnonymousClass1 */

        @Override // android.view.ISystemGestureExclusionListener
        public void onSystemGestureExclusionChanged(int displayId, Region systemGestureExclusion) {
            Region exclusion = Region.obtain(systemGestureExclusion);
            Handler handler = PointerLocationView.this.getHandler();
            if (handler != null) {
                handler.post(new Runnable(exclusion) {
                    /* class com.android.internal.widget.$$Lambda$PointerLocationView$1$utsjc18145VWAe5S9LSLblHeqxc */
                    private final /* synthetic */ Region f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        PointerLocationView.AnonymousClass1.this.lambda$onSystemGestureExclusionChanged$0$PointerLocationView$1(this.f$1);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onSystemGestureExclusionChanged$0$PointerLocationView$1(Region exclusion) {
            PointerLocationView.this.mSystemGestureExclusion.set(exclusion);
            exclusion.recycle();
            PointerLocationView.this.invalidate();
        }
    };
    private final Paint mSystemGestureExclusionPaint;
    private final Path mSystemGestureExclusionPath = new Path();
    private final Paint mTargetPaint;
    private final MotionEvent.PointerCoords mTempCoords = new MotionEvent.PointerCoords();
    private final FasterStringBuilder mText = new FasterStringBuilder();
    private final Paint mTextBackgroundPaint;
    private final Paint mTextLevelPaint;
    private final Paint.FontMetricsInt mTextMetrics = new Paint.FontMetricsInt();
    private final Paint mTextPaint;
    private final ViewConfiguration mVC;
    private final VelocityTracker mVelocity;

    public static class PointerState {
        private VelocityTracker.Estimator mAltEstimator = new VelocityTracker.Estimator();
        private float mAltXVelocity;
        private float mAltYVelocity;
        private float mBoundingBottom;
        private float mBoundingLeft;
        private float mBoundingRight;
        private float mBoundingTop;
        private MotionEvent.PointerCoords mCoords = new MotionEvent.PointerCoords();
        @UnsupportedAppUsage
        private boolean mCurDown;
        private VelocityTracker.Estimator mEstimator = new VelocityTracker.Estimator();
        private boolean mHasBoundingBox;
        private int mToolType;
        private int mTraceCount;
        private boolean[] mTraceCurrent = new boolean[32];
        private float[] mTraceX = new float[32];
        private float[] mTraceY = new float[32];
        private float mXVelocity;
        private float mYVelocity;

        public void clearTrace() {
            this.mTraceCount = 0;
        }

        public void addTrace(float x, float y, boolean current) {
            float[] fArr = this.mTraceX;
            int traceCapacity = fArr.length;
            int i = this.mTraceCount;
            if (i == traceCapacity) {
                int traceCapacity2 = traceCapacity * 2;
                float[] newTraceX = new float[traceCapacity2];
                System.arraycopy(fArr, 0, newTraceX, 0, i);
                this.mTraceX = newTraceX;
                float[] newTraceY = new float[traceCapacity2];
                System.arraycopy(this.mTraceY, 0, newTraceY, 0, this.mTraceCount);
                this.mTraceY = newTraceY;
                boolean[] newTraceCurrent = new boolean[traceCapacity2];
                System.arraycopy(this.mTraceCurrent, 0, newTraceCurrent, 0, this.mTraceCount);
                this.mTraceCurrent = newTraceCurrent;
            }
            float[] newTraceY2 = this.mTraceX;
            int i2 = this.mTraceCount;
            newTraceY2[i2] = x;
            this.mTraceY[i2] = y;
            this.mTraceCurrent[i2] = current;
            this.mTraceCount = i2 + 1;
        }
    }

    public PointerLocationView(Context c) {
        super(c);
        setFocusableInTouchMode(true);
        this.mIm = (InputManager) c.getSystemService(InputManager.class);
        this.mVC = ViewConfiguration.get(c);
        this.mTextPaint = new Paint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize(getResources().getDisplayMetrics().density * 10.0f);
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
        this.mSystemGestureExclusionPaint = new Paint();
        this.mSystemGestureExclusionPaint.setARGB(25, 255, 0, 0);
        this.mSystemGestureExclusionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
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

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (insets.getDisplayCutout() != null) {
            this.mHeaderPaddingTop = insets.getDisplayCutout().getSafeInsetTop();
        } else {
            this.mHeaderPaddingTop = 0;
        }
        return super.onApplyWindowInsets(insets);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mTextPaint.getFontMetricsInt(this.mTextMetrics);
        this.mHeaderBottom = (this.mHeaderPaddingTop - this.mTextMetrics.ascent) + this.mTextMetrics.descent + 2;
    }

    private void drawOval(Canvas canvas, float x, float y, float major, float minor, float angle, Paint paint) {
        canvas.save(1);
        canvas.rotate((float) (((double) (180.0f * angle)) / 3.141592653589793d), x, y);
        RectF rectF = this.mReusableOvalRect;
        rectF.left = x - (minor / 2.0f);
        rectF.right = (minor / 2.0f) + x;
        rectF.top = y - (major / 2.0f);
        rectF.bottom = (major / 2.0f) + y;
        canvas.drawOval(rectF, paint);
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0557  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0575  */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        float lastX;
        float arrowSize;
        float orientationVectorY;
        float orientationVectorX;
        int itemW;
        int i;
        int itemW2;
        float f;
        int w = getWidth();
        int itemW3 = w / 7;
        int base = (this.mHeaderPaddingTop - this.mTextMetrics.ascent) + 1;
        int bottom = this.mHeaderBottom;
        int NP = this.mPointers.size();
        if (!this.mSystemGestureExclusion.isEmpty()) {
            this.mSystemGestureExclusionPath.reset();
            this.mSystemGestureExclusion.getBoundaryPath(this.mSystemGestureExclusionPath);
            canvas.drawPath(this.mSystemGestureExclusionPath, this.mSystemGestureExclusionPaint);
        }
        int i2 = this.mActivePointerId;
        if (i2 >= 0) {
            if (i2 < NP) {
                PointerState ps = this.mPointers.get(i2);
                canvas.drawRect(0.0f, (float) this.mHeaderPaddingTop, (float) (itemW3 - 1), (float) bottom, this.mTextBackgroundPaint);
                canvas.drawText(this.mText.clear().append("P: ").append(this.mCurNumPointers).append(" / ").append(this.mMaxNumPointers).toString(), 1.0f, (float) base, this.mTextPaint);
                int N = ps.mTraceCount;
                if ((!this.mCurDown || !ps.mCurDown) && N != 0) {
                    f = 1.0f;
                    float dx = ps.mTraceX[N - 1] - ps.mTraceX[0];
                    float dy = ps.mTraceY[N - 1] - ps.mTraceY[0];
                    canvas.drawRect((float) itemW3, (float) this.mHeaderPaddingTop, (float) ((itemW3 * 2) - 1), (float) bottom, Math.abs(dx) < ((float) this.mVC.getScaledTouchSlop()) ? this.mTextBackgroundPaint : this.mTextLevelPaint);
                    canvas.drawText(this.mText.clear().append("dX: ").append(dx, 1).toString(), (float) (itemW3 + 1), (float) base, this.mTextPaint);
                    canvas.drawRect((float) (itemW3 * 2), (float) this.mHeaderPaddingTop, (float) ((itemW3 * 3) - 1), (float) bottom, Math.abs(dy) < ((float) this.mVC.getScaledTouchSlop()) ? this.mTextBackgroundPaint : this.mTextLevelPaint);
                    canvas.drawText(this.mText.clear().append("dY: ").append(dy, 1).toString(), (float) ((itemW3 * 2) + 1), (float) base, this.mTextPaint);
                } else {
                    f = 1.0f;
                    canvas.drawRect((float) itemW3, (float) this.mHeaderPaddingTop, (float) ((itemW3 * 2) - 1), (float) bottom, this.mTextBackgroundPaint);
                    canvas.drawText(this.mText.clear().append("X: ").append(ps.mCoords.x, 1).toString(), (float) (itemW3 + 1), (float) base, this.mTextPaint);
                    canvas.drawRect((float) (itemW3 * 2), (float) this.mHeaderPaddingTop, (float) ((itemW3 * 3) - 1), (float) bottom, this.mTextBackgroundPaint);
                    canvas.drawText(this.mText.clear().append("Y: ").append(ps.mCoords.y, 1).toString(), (float) ((itemW3 * 2) + 1), (float) base, this.mTextPaint);
                }
                canvas.drawRect((float) (itemW3 * 3), (float) this.mHeaderPaddingTop, (float) ((itemW3 * 4) - 1), (float) bottom, this.mTextBackgroundPaint);
                canvas.drawText(this.mText.clear().append("Xv: ").append(ps.mXVelocity, 3).toString(), (float) ((itemW3 * 3) + 1), (float) base, this.mTextPaint);
                canvas.drawRect((float) (itemW3 * 4), (float) this.mHeaderPaddingTop, (float) ((itemW3 * 5) - 1), (float) bottom, this.mTextBackgroundPaint);
                canvas.drawText(this.mText.clear().append("Yv: ").append(ps.mYVelocity, 3).toString(), (float) ((itemW3 * 4) + 1), (float) base, this.mTextPaint);
                canvas.drawRect((float) (itemW3 * 5), (float) this.mHeaderPaddingTop, (float) ((itemW3 * 6) - 1), (float) bottom, this.mTextBackgroundPaint);
                canvas.drawRect((float) (itemW3 * 5), (float) this.mHeaderPaddingTop, (((float) (itemW3 * 5)) + (ps.mCoords.pressure * ((float) itemW3))) - f, (float) bottom, this.mTextLevelPaint);
                canvas.drawText(this.mText.clear().append("Prs: ").append(ps.mCoords.pressure, 2).toString(), (float) ((itemW3 * 5) + 1), (float) base, this.mTextPaint);
                canvas.drawRect((float) (itemW3 * 6), (float) this.mHeaderPaddingTop, (float) w, (float) bottom, this.mTextBackgroundPaint);
                canvas.drawRect((float) (itemW3 * 6), (float) this.mHeaderPaddingTop, (((float) (itemW3 * 6)) + (ps.mCoords.size * ((float) itemW3))) - f, (float) bottom, this.mTextLevelPaint);
                canvas.drawText(this.mText.clear().append("Size: ").append(ps.mCoords.size, 2).toString(), (float) ((itemW3 * 6) + 1), (float) base, this.mTextPaint);
            } else if (HWFLOW) {
                Log.i(TAG, "exception occur: mActivePointerId >= NP");
                return;
            } else {
                return;
            }
        }
        int p = 0;
        while (p < NP) {
            PointerState ps2 = this.mPointers.get(p);
            int N2 = ps2.mTraceCount;
            int i3 = 128;
            this.mPaint.setARGB(255, 128, 255, 255);
            int i4 = 0;
            boolean haveLast = false;
            boolean drawn = false;
            float lastX2 = 0.0f;
            float lastY = 0.0f;
            while (i4 < N2) {
                float x = ps2.mTraceX[i4];
                float y = ps2.mTraceY[i4];
                if (Float.isNaN(x)) {
                    haveLast = false;
                    i = i4;
                    itemW = itemW3;
                    itemW2 = i3;
                } else {
                    if (haveLast) {
                        i = i4;
                        itemW = itemW3;
                        itemW2 = i3;
                        canvas.drawLine(lastX2, lastY, x, y, this.mPathPaint);
                        canvas.drawPoint(lastX2, lastY, ps2.mTraceCurrent[i] ? this.mCurrentPointPaint : this.mPaint);
                        drawn = true;
                    } else {
                        i = i4;
                        itemW = itemW3;
                        itemW2 = i3;
                    }
                    lastX2 = x;
                    lastY = y;
                    haveLast = true;
                }
                i4 = i + 1;
                i3 = itemW2;
                itemW3 = itemW;
            }
            if (drawn) {
                this.mPaint.setARGB(255, 255, 64, i3);
                canvas.drawLine(lastX2, lastY, lastX2 + (ps2.mXVelocity * 16.0f), lastY + (ps2.mYVelocity * 16.0f), this.mPaint);
                if (this.mAltVelocity != null) {
                    this.mPaint.setARGB(255, 64, 255, i3);
                    canvas.drawLine(lastX2, lastY, lastX2 + (ps2.mAltXVelocity * 16.0f), lastY + (16.0f * ps2.mAltYVelocity), this.mPaint);
                }
            }
            if (!this.mCurDown || !ps2.mCurDown) {
                lastX = 2.8E-45f;
            } else {
                canvas.drawLine(0.0f, ps2.mCoords.y, (float) getWidth(), ps2.mCoords.y, this.mTargetPaint);
                canvas.drawLine(ps2.mCoords.x, 0.0f, ps2.mCoords.x, (float) getHeight(), this.mTargetPaint);
                int pressureLevel = (int) (ps2.mCoords.pressure * 255.0f);
                this.mPaint.setARGB(255, pressureLevel, 255, 255 - pressureLevel);
                canvas.drawPoint(ps2.mCoords.x, ps2.mCoords.y, this.mPaint);
                this.mPaint.setARGB(255, pressureLevel, 255 - pressureLevel, i3);
                lastX = 2.8E-45f;
                drawOval(canvas, ps2.mCoords.x, ps2.mCoords.y, ps2.mCoords.touchMajor, ps2.mCoords.touchMinor, ps2.mCoords.orientation, this.mPaint);
                this.mPaint.setARGB(255, pressureLevel, 128, 255 - pressureLevel);
                drawOval(canvas, ps2.mCoords.x, ps2.mCoords.y, ps2.mCoords.toolMajor, ps2.mCoords.toolMinor, ps2.mCoords.orientation, this.mPaint);
                float arrowSize2 = ps2.mCoords.toolMajor * 0.7f;
                if (arrowSize2 < 20.0f) {
                    arrowSize = 20.0f;
                } else {
                    arrowSize = arrowSize2;
                }
                this.mPaint.setARGB(255, pressureLevel, 255, 0);
                float orientationVectorX2 = (float) (Math.sin((double) ps2.mCoords.orientation) * ((double) arrowSize));
                float orientationVectorY2 = (float) ((-Math.cos((double) ps2.mCoords.orientation)) * ((double) arrowSize));
                if (ps2.mToolType == 2) {
                    orientationVectorY = orientationVectorY2;
                    orientationVectorX = orientationVectorX2;
                } else if (ps2.mToolType == 4) {
                    orientationVectorY = orientationVectorY2;
                    orientationVectorX = orientationVectorX2;
                } else {
                    orientationVectorY = orientationVectorY2;
                    orientationVectorX = orientationVectorX2;
                    canvas.drawLine(ps2.mCoords.x - orientationVectorX2, ps2.mCoords.y - orientationVectorY2, ps2.mCoords.x + orientationVectorX2, ps2.mCoords.y + orientationVectorY2, this.mPaint);
                    float tiltScale = (float) Math.sin((double) ps2.mCoords.getAxisValue(25));
                    canvas.drawCircle(ps2.mCoords.x + (orientationVectorX * tiltScale), ps2.mCoords.y + (orientationVectorY * tiltScale), 3.0f, this.mPaint);
                    if (!ps2.mHasBoundingBox) {
                        canvas.drawRect(ps2.mBoundingLeft, ps2.mBoundingTop, ps2.mBoundingRight, ps2.mBoundingBottom, this.mPaint);
                    }
                }
                canvas.drawLine(ps2.mCoords.x, ps2.mCoords.y, ps2.mCoords.x + orientationVectorX, ps2.mCoords.y + orientationVectorY, this.mPaint);
                float tiltScale2 = (float) Math.sin((double) ps2.mCoords.getAxisValue(25));
                canvas.drawCircle(ps2.mCoords.x + (orientationVectorX * tiltScale2), ps2.mCoords.y + (orientationVectorY * tiltScale2), 3.0f, this.mPaint);
                if (!ps2.mHasBoundingBox) {
                }
            }
            p++;
            w = w;
            itemW3 = itemW3;
        }
    }

    private void logMotionEvent(String type, MotionEvent event) {
        int action = event.getAction();
        int N = event.getHistorySize();
        int NI = event.getPointerCount();
        for (int historyPos = 0; historyPos < N; historyPos++) {
            for (int i = 0; i < NI; i++) {
                int id = event.getPointerId(i);
                event.getHistoricalPointerCoords(i, historyPos, this.mTempCoords);
                logCoords(type, action, i, this.mTempCoords, id, event);
            }
        }
        for (int i2 = 0; i2 < NI; i2++) {
            int id2 = event.getPointerId(i2);
            event.getPointerCoords(i2, this.mTempCoords);
            logCoords(type, action, i2, this.mTempCoords, id2, event);
        }
    }

    private void logCoords(String type, int action, int index, MotionEvent.PointerCoords coords, int id, MotionEvent event) {
        String prefix;
        int toolType = event.getToolType(index);
        int buttonState = event.getButtonState();
        switch (action & 255) {
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
                if (index != ((action & 65280) >> 8)) {
                    prefix = "MOVE";
                    break;
                } else {
                    prefix = "DOWN";
                    break;
                }
            case 6:
                if (index != ((action & 65280) >> 8)) {
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
        Log.i(TAG, this.mText.clear().append(type).append(" id ").append(id + 1).append(": ").append(prefix).append(" (").append(coords.x, 3).append(", ").append(coords.y, 3).append(") Pressure=").append(coords.pressure, 3).append(" Size=").append(coords.size, 3).append(" TouchMajor=").append(coords.touchMajor, 3).append(" TouchMinor=").append(coords.touchMinor, 3).append(" ToolMajor=").append(coords.toolMajor, 3).append(" ToolMinor=").append(coords.toolMinor, 3).append(" Orientation=").append((float) (((double) (coords.orientation * 180.0f)) / 3.141592653589793d), 1).append("deg").append(" Tilt=").append((float) (((double) (coords.getAxisValue(25) * 180.0f)) / 3.141592653589793d), 1).append("deg").append(" Distance=").append(coords.getAxisValue(24), 1).append(" VScroll=").append(coords.getAxisValue(9), 1).append(" HScroll=").append(coords.getAxisValue(10), 1).append(" BoundingBox=[(").append(event.getAxisValue(32), 3).append(", ").append(event.getAxisValue(33), 3).append(")").append(", (").append(event.getAxisValue(34), 3).append(", ").append(event.getAxisValue(35), 3).append(")]").append(" ToolType=").append(MotionEvent.toolTypeToString(toolType)).append(" ButtonState=").append(MotionEvent.buttonStateToString(buttonState)).toString());
    }

    /* JADX WARN: Type inference failed for: r4v1, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r4v2 */
    /* JADX WARN: Type inference failed for: r4v4 */
    @Override // android.view.WindowManagerPolicyConstants.PointerEventListener
    public void onPointerEvent(MotionEvent event) {
        ?? r4;
        PointerState ps;
        MotionEvent.PointerCoords coords;
        PointerState ps2;
        int id;
        char c;
        PointerState ps3;
        int N;
        int historyPos;
        int i;
        MotionEvent.PointerCoords coords2;
        PointerState ps4;
        int action = event.getAction();
        int NP = this.mPointers.size();
        int i2 = 1;
        if (action == 0 || (action & 255) == 5) {
            int index = (action & 65280) >> 8;
            if (action == 0) {
                for (int p = 0; p < NP; p++) {
                    PointerState ps5 = this.mPointers.get(p);
                    ps5.clearTrace();
                    ps5.mCurDown = false;
                }
                this.mCurDown = true;
                this.mCurNumPointers = 0;
                this.mMaxNumPointers = 0;
                this.mVelocity.clear();
                VelocityTracker velocityTracker = this.mAltVelocity;
                if (velocityTracker != null) {
                    velocityTracker.clear();
                }
            }
            this.mCurNumPointers++;
            int i3 = this.mMaxNumPointers;
            int i4 = this.mCurNumPointers;
            if (i3 < i4) {
                this.mMaxNumPointers = i4;
            }
            int id2 = event.getPointerId(index);
            while (NP <= id2) {
                this.mPointers.add(new PointerState());
                NP++;
            }
            int size = this.mPointers.size();
            int i5 = this.mActivePointerId;
            if (i5 < size && (i5 < 0 || !this.mPointers.get(i5).mCurDown)) {
                this.mActivePointerId = id2;
            }
            PointerState ps6 = this.mPointers.get(id2);
            ps6.mCurDown = true;
            InputDevice device = InputDevice.getDevice(event.getDeviceId());
            ps6.mHasBoundingBox = (device == null || device.getMotionRange(32) == null) ? false : true;
        }
        int NI = event.getPointerCount();
        this.mVelocity.addMovement(event);
        this.mVelocity.computeCurrentVelocity(1);
        VelocityTracker velocityTracker2 = this.mAltVelocity;
        if (velocityTracker2 != null) {
            velocityTracker2.addMovement(event);
            this.mAltVelocity.computeCurrentVelocity(1);
        }
        int N2 = event.getHistorySize();
        int historyPos2 = 0;
        while (historyPos2 < N2) {
            int i6 = 0;
            while (i6 < NI) {
                int id3 = event.getPointerId(i6);
                if (id3 < this.mPointers.size()) {
                    ps3 = this.mCurDown ? this.mPointers.get(id3) : null;
                } else {
                    ps3 = null;
                }
                MotionEvent.PointerCoords coords3 = ps3 != null ? ps3.mCoords : this.mTempCoords;
                event.getHistoricalPointerCoords(i6, historyPos2, coords3);
                if (this.mPrintCoords) {
                    coords2 = coords3;
                    ps4 = ps3;
                    i = i6;
                    historyPos = historyPos2;
                    N = N2;
                    logCoords(TAG, action, i6, coords2, id3, event);
                } else {
                    coords2 = coords3;
                    ps4 = ps3;
                    i = i6;
                    historyPos = historyPos2;
                    N = N2;
                }
                if (ps4 != null) {
                    ps4.addTrace(coords2.x, coords2.y, false);
                }
                i6 = i + 1;
                historyPos2 = historyPos;
                N2 = N;
            }
            historyPos2++;
        }
        for (int i7 = 0; i7 < NI; i7++) {
            int id4 = event.getPointerId(i7);
            if (id4 < this.mPointers.size()) {
                ps = this.mCurDown ? this.mPointers.get(id4) : null;
            } else {
                ps = null;
            }
            MotionEvent.PointerCoords coords4 = ps != null ? ps.mCoords : this.mTempCoords;
            event.getPointerCoords(i7, coords4);
            if (this.mPrintCoords) {
                coords = coords4;
                ps2 = ps;
                id = id4;
                logCoords(TAG, action, i7, coords4, id4, event);
            } else {
                coords = coords4;
                ps2 = ps;
                id = id4;
            }
            if (ps2 != null) {
                ps2.addTrace(coords.x, coords.y, true);
                ps2.mXVelocity = this.mVelocity.getXVelocity(id);
                ps2.mYVelocity = this.mVelocity.getYVelocity(id);
                this.mVelocity.getEstimator(id, ps2.mEstimator);
                VelocityTracker velocityTracker3 = this.mAltVelocity;
                if (velocityTracker3 != null) {
                    ps2.mAltXVelocity = velocityTracker3.getXVelocity(id);
                    ps2.mAltYVelocity = this.mAltVelocity.getYVelocity(id);
                    this.mAltVelocity.getEstimator(id, ps2.mAltEstimator);
                }
                ps2.mToolType = event.getToolType(i7);
                if (ps2.mHasBoundingBox) {
                    c = ' ';
                    ps2.mBoundingLeft = event.getAxisValue(32, i7);
                    ps2.mBoundingTop = event.getAxisValue(33, i7);
                    ps2.mBoundingRight = event.getAxisValue(34, i7);
                    ps2.mBoundingBottom = event.getAxisValue(35, i7);
                } else {
                    c = ' ';
                }
            } else {
                c = ' ';
            }
        }
        if (action == 1 || action == 3 || (action & 255) == 6) {
            int index2 = (65280 & action) >> 8;
            int id5 = event.getPointerId(index2);
            if (id5 < this.mPointers.size()) {
                PointerState ps7 = this.mPointers.get(id5);
                ps7.mCurDown = false;
                if (action == 1) {
                    r4 = 0;
                } else if (action == 3) {
                    r4 = 0;
                } else {
                    this.mCurNumPointers--;
                    if (this.mActivePointerId == id5) {
                        if (index2 != 0) {
                            i2 = 0;
                        }
                        this.mActivePointerId = event.getPointerId(i2);
                    }
                    ps7.addTrace(Float.NaN, Float.NaN, false);
                }
                this.mCurDown = r4;
                this.mCurNumPointers = r4;
            } else {
                return;
            }
        }
        invalidate();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        onPointerEvent(event);
        if (event.getAction() != 0 || isFocused()) {
            return true;
        }
        requestFocus();
        return true;
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent event) {
        int source = event.getSource();
        if ((source & 2) != 0) {
            onPointerEvent(event);
            return true;
        } else if ((source & 16) != 0) {
            logMotionEvent("Joystick", event);
            return true;
        } else if ((source & 8) != 0) {
            logMotionEvent("Position", event);
            return true;
        } else {
            logMotionEvent("Generic", event);
            return true;
        }
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!shouldLogKey(keyCode)) {
            return super.onKeyDown(keyCode, event);
        }
        int repeatCount = event.getRepeatCount();
        if (repeatCount == 0) {
            Log.i(TAG, "Key Down: " + event);
            return true;
        }
        Log.i(TAG, "Key Repeat #" + repeatCount + ": " + event);
        return true;
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!shouldLogKey(keyCode)) {
            return super.onKeyUp(keyCode, event);
        }
        Log.i(TAG, "Key Up: " + event);
        return true;
    }

    private static boolean shouldLogKey(int keyCode) {
        switch (keyCode) {
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
                return true;
            default:
                if (KeyEvent.isGamepadButton(keyCode) || KeyEvent.isModifierKey(keyCode)) {
                    return true;
                }
                return false;
        }
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent event) {
        logMotionEvent("Trackball", event);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIm.registerInputDeviceListener(this, getHandler());
        if (shouldShowSystemGestureExclusion()) {
            try {
                WindowManagerGlobal.getWindowManagerService().registerSystemGestureExclusionListener(this.mSystemGestureExclusionListener, this.mContext.getDisplayId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            this.mSystemGestureExclusion.setEmpty();
        }
        logInputDevices();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIm.unregisterInputDeviceListener(this);
        try {
            WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureExclusionListener(this.mSystemGestureExclusionListener, this.mContext.getDisplayId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceAdded(int deviceId) {
        logInputDeviceState(deviceId, "Device Added");
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceChanged(int deviceId) {
        logInputDeviceState(deviceId, "Device Changed");
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceRemoved(int deviceId) {
        logInputDeviceState(deviceId, "Device Removed");
    }

    private void logInputDevices() {
        int[] deviceIds;
        for (int i : InputDevice.getDeviceIds()) {
            logInputDeviceState(i, "Device Enumerated");
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

    private static boolean shouldShowSystemGestureExclusion() {
        return SystemProperties.getBoolean("debug.pointerlocation.showexclusion", false);
    }

    /* access modifiers changed from: private */
    public static final class FasterStringBuilder {
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
            if (!negative || (value = -value) >= 0) {
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
                int numberWidth = 10;
                while (value < divisor) {
                    divisor /= 10;
                    numberWidth--;
                    if (numberWidth < zeroPadWidth) {
                        chars[index] = '0';
                        index++;
                    }
                }
                while (true) {
                    int digit = value / divisor;
                    value -= digit * divisor;
                    divisor /= 10;
                    int index3 = index + 1;
                    chars[index] = (char) (digit + 48);
                    if (divisor == 0) {
                        this.mLength = index3;
                        return this;
                    }
                    index = index3;
                }
            } else {
                append("-2147483648");
                return this;
            }
        }

        public FasterStringBuilder append(float value, int precision) {
            int scale = 1;
            for (int i = 0; i < precision; i++) {
                scale *= 10;
            }
            float value2 = (float) (Math.rint((double) (((float) scale) * value)) / ((double) scale));
            if (((int) value2) == 0 && value2 < 0.0f) {
                append(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            }
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
}
