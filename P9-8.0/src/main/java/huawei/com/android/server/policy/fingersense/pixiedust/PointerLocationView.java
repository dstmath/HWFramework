package huawei.com.android.server.policy.fingersense.pixiedust;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.provider.Settings.System;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.provider.HwSettings;
import huawei.com.android.server.policy.fingersense.SystemWideActionsListener;
import huawei.com.android.server.policy.stylus.StylusGestureListener;

public class PointerLocationView extends View {
    private static final String TAG = "PointerLocationView";
    int controlPointCounter;
    PointF[] controlPoints;
    GlowParticleSystem glowParticleSystem;
    long lastUpdateMs;
    private boolean mIsDrawing = false;
    StylusGestureListener mStylusGestureListener;
    SystemWideActionsListener mSysWideActionsListener;
    Paint pathPaint = new Paint();
    PixieDustParticleSystem pixieParticleSystem;
    Path pointerPath;
    Bitmap sprite;

    public PointerLocationView(Context context, SystemWideActionsListener sysWideActionsListener) {
        super(context);
        this.pathPaint.setStyle(Style.STROKE);
        this.pathPaint.setStrokeJoin(Join.ROUND);
        this.pathPaint.setStrokeCap(Cap.ROUND);
        this.pathPaint.setStrokeWidth(10.0f);
        this.pathPaint.setAntiAlias(true);
        this.pathPaint.setPathEffect(new CornerPathEffect(10.0f));
        this.pointerPath = new Path();
        this.controlPoints = new PointF[5];
        this.controlPointCounter = 0;
        this.mSysWideActionsListener = sysWideActionsListener;
        this.sprite = BitmapFactory.decodeResource(getResources(), 33751446);
        this.pixieParticleSystem = new PixieDustParticleSystem(2000, this.sprite, 10);
        this.glowParticleSystem = new GlowParticleSystem(2000, this.sprite, 100);
        updateColors();
        for (int i = 0; i < this.controlPoints.length; i++) {
            this.controlPoints[i] = new PointF();
        }
    }

    public PointerLocationView(Context context, StylusGestureListener stylusGestureListener) {
        super(context);
        this.pathPaint.setStyle(Style.STROKE);
        this.pathPaint.setStrokeJoin(Join.ROUND);
        this.pathPaint.setStrokeCap(Cap.ROUND);
        this.pathPaint.setStrokeWidth(10.0f);
        this.pathPaint.setAntiAlias(true);
        this.pathPaint.setPathEffect(new CornerPathEffect(10.0f));
        this.pointerPath = new Path();
        this.controlPoints = new PointF[5];
        this.controlPointCounter = 0;
        this.mStylusGestureListener = stylusGestureListener;
        this.sprite = BitmapFactory.decodeResource(getResources(), 33751446);
        this.pixieParticleSystem = new PixieDustParticleSystem(2000, this.sprite, 10);
        this.glowParticleSystem = new GlowParticleSystem(2000, this.sprite, 100);
        updateColors();
        for (int i = 0; i < this.controlPoints.length; i++) {
            this.controlPoints[i] = new PointF();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(this.pointerPath, this.pathPaint);
        this.pixieParticleSystem.update();
        this.glowParticleSystem.update();
        if (!this.pointerPath.isEmpty()) {
            if (System.currentTimeMillis() - this.lastUpdateMs > 500) {
                if (this.mSysWideActionsListener != null) {
                    this.mSysWideActionsListener.cancelSystemWideAction();
                }
                if (this.mStylusGestureListener != null) {
                    this.mStylusGestureListener.cancelStylusGesture();
                }
            }
            this.pixieParticleSystem.draw(canvas);
            this.glowParticleSystem.draw(canvas);
            if (!this.mIsDrawing) {
                Log.d(TAG, "Fingersense draw line");
                StatisticalUtils.reportc(getContext(), 102);
                this.mIsDrawing = true;
            }
            invalidate();
        }
    }

    private void updateColors() {
        this.pathPaint.setColor(System.getInt(getContext().getContentResolver(), "fingersense_stroke_color", HwSettings.System.FINGERSENSE_DEFAULT_STROKE_COLOR));
        int pixieColor = System.getInt(getContext().getContentResolver(), "fingersense_pixie_color", HwSettings.System.FINGERSENSE_DEFAULT_PIXIE_COLOR);
        this.pixieParticleSystem.setColor(pixieColor);
        this.glowParticleSystem.setColor(pixieColor);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            this.pointerPath.reset();
            this.pixieParticleSystem.kill();
            return false;
        }
        switch (event.getAction()) {
            case 0:
                updateColors();
                this.lastUpdateMs = System.currentTimeMillis();
                this.controlPointCounter = 0;
                this.controlPoints[this.controlPointCounter].set(event.getX(), event.getY());
                this.pixieParticleSystem.setEmitter((double) event.getX(), (double) event.getY());
                this.mIsDrawing = false;
                break;
            case 1:
            case 3:
                if (this.pointerPath.isEmpty()) {
                    this.pixieParticleSystem.setEmitter((double) event.getX(), (double) event.getY());
                }
                this.pointerPath.reset();
                this.pixieParticleSystem.kill();
                this.mIsDrawing = false;
                break;
            case 2:
                PointF[] pointFArr = this.controlPoints;
                int i = this.controlPointCounter + 1;
                this.controlPointCounter = i;
                pointFArr[i].set(event.getX(), event.getY());
                if (this.controlPointCounter == 4) {
                    this.controlPoints[3].set((this.controlPoints[2].x + this.controlPoints[4].x) / 2.0f, (this.controlPoints[2].y + this.controlPoints[4].y) / 2.0f);
                    this.pointerPath.moveTo(this.controlPoints[0].x, this.controlPoints[0].y);
                    this.pointerPath.cubicTo(this.controlPoints[1].x, this.controlPoints[1].y, this.controlPoints[2].x, this.controlPoints[2].y, this.controlPoints[3].x, this.controlPoints[3].y);
                    this.controlPoints[0].set(this.controlPoints[3]);
                    this.controlPoints[1].set(this.controlPoints[4]);
                    this.controlPointCounter = 1;
                    long now = System.currentTimeMillis();
                    this.glowParticleSystem.addParticles(this.pointerPath, now - this.lastUpdateMs);
                    this.lastUpdateMs = now;
                }
                this.pixieParticleSystem.setEmitter((double) event.getX(), (double) event.getY());
                break;
        }
        postInvalidate();
        return true;
    }
}
