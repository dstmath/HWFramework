package huawei.com.android.server.policy.stylus.glow;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.huawei.bd.Reporter;
import huawei.com.android.server.policy.stylus.StylusGestureListener;

public class HwPointPositionView extends View {
    private static final float DOUBLE = 2.0f;
    private static final int MAX_DIVERGENCE_NUM = 10;
    private static final int MAX_UPDATE_TIME_MILLIS = 500;
    private static final float PAINT_PATH_RADIUS = 10.0f;
    private static final float PAINT_STROKE_WIDTH = 10.0f;
    private static final int POINT_SYSTEM_SIZE = 2000;
    private static final int POINT_TOTAL_SIZE = 5;
    private static final int STYLUS_DEFAULT_PIXIE_COLOR = Color.parseColor("#b2ebf2");
    private static final int STYLUS_DEFAULT_STROKE_COLOR = Color.parseColor("#00bcd4");
    private static final String STYLUS_PIXIE_COLOR = "stylus_pixie_color";
    private static final String STYLUS_STROKE_COLOR = "stylus_stroke_color";
    private static final String TAG = "HwPointPositionView";
    private static final int TYPE_STYLUS_DRAW_LINE = 102;
    private HwGlowTraceSystem mGlowTraceSystem;
    private boolean mIsDrawing = false;
    private long mLastUpdateTimeMillis;
    private int mPointCounter = 0;
    private Path mPointerPath = new Path();
    private Paint mPointerPathPaint;
    private StylusGestureListener mStylusGestureListener;
    private PointF[] mTraceControlPoints = new PointF[5];

    public HwPointPositionView(Context context, StylusGestureListener stylusGestureListener) {
        super(context);
        initPointerPathPaint();
        this.mStylusGestureListener = stylusGestureListener;
        this.mGlowTraceSystem = new HwGlowTraceSystem(2000, BitmapFactory.decodeResource(getResources(), 33751446), 10);
        updateTrackColors();
        int controlPointLength = this.mTraceControlPoints.length;
        for (int i = 0; i < controlPointLength; i++) {
            this.mTraceControlPoints[i] = new PointF();
        }
    }

    private void initPointerPathPaint() {
        this.mPointerPathPaint = new Paint();
        this.mPointerPathPaint.setStyle(Paint.Style.STROKE);
        this.mPointerPathPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPointerPathPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPointerPathPaint.setStrokeWidth(10.0f);
        this.mPointerPathPaint.setAntiAlias(true);
        this.mPointerPathPaint.setPathEffect(new CornerPathEffect(10.0f));
    }

    private void updateTrackColors() {
        this.mPointerPathPaint.setColor(Settings.System.getInt(getContext().getContentResolver(), STYLUS_STROKE_COLOR, STYLUS_DEFAULT_STROKE_COLOR));
        this.mGlowTraceSystem.setTrackColor(Settings.System.getInt(getContext().getContentResolver(), STYLUS_PIXIE_COLOR, STYLUS_DEFAULT_PIXIE_COLOR));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
        if (r0 != 3) goto L_0x0031;
     */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        if (event.getPointerCount() > 1) {
            this.mPointerPath.reset();
            this.mGlowTraceSystem.clear();
            return false;
        }
        int action = event.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    processMoveEvent(event);
                }
            }
            processUpAndCancelEvent(event);
        } else {
            processDownEvent(event);
        }
        postInvalidate();
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        StylusGestureListener stylusGestureListener;
        super.onDraw(canvas);
        if (canvas != null) {
            canvas.drawPath(this.mPointerPath, this.mPointerPathPaint);
            this.mGlowTraceSystem.update();
            if (!this.mPointerPath.isEmpty()) {
                if (System.currentTimeMillis() - this.mLastUpdateTimeMillis > 500 && (stylusGestureListener = this.mStylusGestureListener) != null) {
                    stylusGestureListener.cancelStylusGesture();
                }
                this.mGlowTraceSystem.draw(canvas);
                if (!this.mIsDrawing) {
                    Log.d(TAG, "stylus drawing line");
                    Reporter.c(getContext(), 102);
                    this.mIsDrawing = true;
                }
                invalidate();
            }
        }
    }

    private void processUpAndCancelEvent(MotionEvent event) {
        if (this.mPointerPath.isEmpty()) {
            this.mGlowTraceSystem.resetDivergentPoints((double) event.getX(), (double) event.getY());
        }
        this.mPointerPath.reset();
        this.mGlowTraceSystem.clear();
        this.mIsDrawing = false;
    }

    private void processDownEvent(MotionEvent event) {
        updateTrackColors();
        this.mLastUpdateTimeMillis = System.currentTimeMillis();
        this.mPointCounter = 0;
        this.mTraceControlPoints[this.mPointCounter].set(event.getX(), event.getY());
        this.mGlowTraceSystem.resetDivergentPoints((double) event.getX(), (double) event.getY());
        this.mIsDrawing = false;
    }

    private void processMoveEvent(MotionEvent event) {
        this.mPointCounter++;
        int i = this.mPointCounter;
        if (i >= 5) {
            Log.w(TAG, "mTraceControlPoints index out of size");
            return;
        }
        this.mTraceControlPoints[i].set(event.getX(), event.getY());
        if (this.mPointCounter == 4) {
            PointF[] pointFArr = this.mTraceControlPoints;
            pointFArr[3].set((pointFArr[2].x + this.mTraceControlPoints[4].x) / 2.0f, (this.mTraceControlPoints[2].y + this.mTraceControlPoints[4].y) / 2.0f);
            this.mPointerPath.moveTo(this.mTraceControlPoints[0].x, this.mTraceControlPoints[0].y);
            this.mPointerPath.cubicTo(this.mTraceControlPoints[1].x, this.mTraceControlPoints[1].y, this.mTraceControlPoints[2].x, this.mTraceControlPoints[2].y, this.mTraceControlPoints[3].x, this.mTraceControlPoints[3].y);
            PointF[] pointFArr2 = this.mTraceControlPoints;
            pointFArr2[0].set(pointFArr2[3]);
            PointF[] pointFArr3 = this.mTraceControlPoints;
            pointFArr3[1].set(pointFArr3[4]);
            this.mPointCounter = 1;
            long now = System.currentTimeMillis();
            this.mGlowTraceSystem.addGlowPoints(this.mPointerPath, now - this.mLastUpdateTimeMillis);
            this.mLastUpdateTimeMillis = now;
        }
        this.mGlowTraceSystem.resetDivergentPoints((double) event.getX(), (double) event.getY());
    }
}
