package com.huawei.hwtransition.anim;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

public class HintErrorEffect {
    private static String OUTILNE_PATH_FIELD_NAME = "mPath";
    private static int OUTLINE_COLOR = -322791;
    private static String OUTLINE_RADIUS_FIELD_NAME = "mRadius";
    private static String OUTLINE_RECT_FIELD_NAME = "mRect";
    private static int OUTLINE_SHADOW_RADIUS = 16;
    private static int OUTLINE_SHADOW_STROKE_WIDTH = 1;
    private static int OUTLINE_START_ALPHA = 127;
    private static String TAG = "HintErrorEffect";
    private boolean isShowOutline;
    private ArrayList<Float> mAlphaListFactor;
    private Path mEffectPath;
    private RectF mEffectPathBounds;
    private Matrix mEffectPathMatrix;
    private Outline mOutline;
    private Paint mPaint;
    private Path mPath;
    private RectF mPathBounds;
    private Float mRadius;
    private Rect mRect;
    private View mView;
    private ViewOutlineProvider mVop;

    public HintErrorEffect(View view) {
        if (view == null) {
            Log.w(TAG, "TargetView is null");
        }
        this.mPathBounds = new RectF();
        this.mPaint = new Paint(1);
        this.mAlphaListFactor = new ArrayList();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth((float) OUTLINE_SHADOW_STROKE_WIDTH);
        this.mPaint.setColor(OUTLINE_COLOR);
        this.mPaint.setAlpha(OUTLINE_START_ALPHA);
        this.mPaint.setDither(true);
        this.mPaint.setStrokeJoin(Join.ROUND);
        this.mEffectPath = new Path();
        this.mEffectPathBounds = new RectF();
        this.mEffectPathMatrix = new Matrix();
        computeAlphaList();
        this.mView = view;
        this.mOutline = new Outline();
    }

    public void getOutlinePath(Outline outline) {
        try {
            final Field pathField = outline.getClass().getField(OUTILNE_PATH_FIELD_NAME);
            final Field rectField = outline.getClass().getField(OUTLINE_RECT_FIELD_NAME);
            final Field radiusField = outline.getClass().getField(OUTLINE_RADIUS_FIELD_NAME);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    pathField.setAccessible(true);
                    rectField.setAccessible(true);
                    radiusField.setAccessible(true);
                    return null;
                }
            });
            this.mPath = (Path) pathField.get(outline);
            this.mRect = (Rect) rectField.get(outline);
            this.mRadius = (Float) radiusField.get(outline);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.w(TAG, "Wrong reflection. no such filed in Outline class");
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            Log.w(TAG, "Wrong reflection. Illegal access the Outline class");
        }
    }

    public void drawErrorEffect(Canvas canvas) {
        if (!this.isShowOutline) {
            return;
        }
        if (getPath() == null && getRect() == null) {
            Log.w(TAG, "drawErrorEffect outline is null " + getPath());
            return;
        }
        int delta;
        int i;
        if (this.mRect != null && !this.mRect.isEmpty()) {
            delta = 0;
            for (i = 0; i < this.mAlphaListFactor.size(); i++) {
                this.mPaint.setAlpha((int) (((Float) this.mAlphaListFactor.get(i)).floatValue() * ((float) OUTLINE_START_ALPHA)));
                Canvas canvas2 = canvas;
                canvas2.drawRoundRect((float) (this.mRect.left + delta), (float) (this.mRect.top + delta), (float) (this.mRect.right - delta), (float) (this.mRect.bottom - delta), this.mRadius.floatValue(), this.mRadius.floatValue(), this.mPaint);
                delta += OUTLINE_SHADOW_STROKE_WIDTH;
            }
        } else if (this.mPath != null) {
            delta = 0;
            this.mPath.computeBounds(this.mPathBounds, true);
            float oldWidth = this.mPathBounds.width();
            float oldHeight = this.mPathBounds.height();
            for (i = 0; i < this.mAlphaListFactor.size(); i++) {
                this.mPaint.setAlpha((int) (((Float) this.mAlphaListFactor.get(i)).floatValue() * ((float) OUTLINE_START_ALPHA)));
                this.mEffectPathMatrix.setScale(1.0f - (((float) delta) / oldWidth), 1.0f - (((float) delta) / oldHeight));
                this.mEffectPath.set(this.mPath);
                this.mEffectPath.transform(this.mEffectPathMatrix);
                this.mEffectPath.computeBounds(this.mEffectPathBounds, true);
                float currentWidth = this.mEffectPathBounds.width();
                float currentDrawleft = (oldWidth / 2.0f) - (currentWidth / 2.0f);
                float currentDrawTop = (oldHeight / 2.0f) - (this.mEffectPathBounds.height() / 2.0f);
                canvas.save();
                canvas.translate(currentDrawleft, currentDrawTop);
                canvas.drawPath(this.mEffectPath, this.mPaint);
                canvas.restore();
                delta += OUTLINE_SHADOW_STROKE_WIDTH * 2;
            }
        }
    }

    private void computeAlphaList() {
        this.mAlphaListFactor.clear();
        int index = 0;
        int currentAlpha = OUTLINE_START_ALPHA;
        int range = OUTLINE_SHADOW_RADIUS / 2;
        while (currentAlpha > 0) {
            float currentAlphaFactor = 0.0f;
            for (int j = -range; j < range; j++) {
                float alpha;
                if (index + j < 0) {
                    alpha = 1.0f;
                } else if (index + j >= this.mAlphaListFactor.size()) {
                    alpha = 0.0f;
                } else {
                    alpha = ((Float) this.mAlphaListFactor.get(index + j)).floatValue();
                }
                currentAlphaFactor += alpha;
            }
            currentAlphaFactor /= (float) OUTLINE_SHADOW_RADIUS;
            this.mAlphaListFactor.add(Float.valueOf(currentAlphaFactor));
            currentAlpha = (int) (((float) OUTLINE_START_ALPHA) * currentAlphaFactor);
            index++;
        }
    }

    public Path getPath() {
        return this.mPath;
    }

    public Rect getRect() {
        return this.mRect;
    }

    public void showErrEffect(boolean show) {
        if (this.mView != null && show) {
            this.mVop = this.mView.getOutlineProvider();
            this.mVop.getOutline(this.mView, this.mOutline);
            getOutlinePath(this.mOutline);
        }
        this.isShowOutline = show;
    }
}
