package com.huawei.server.fingerprint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.Surface;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class FpLayer {
    private static final int DEFAULT_SIZE = 900;
    private static final int HALF_PICTURE_SIZE = 2;
    private static final int MASK_LAYER = 2147483645;
    private static final int ROUNDING = 3;
    private static final String TAG = FpLayer.class.getSimpleName();
    private List<String> mAnimationRes;
    private Method mApplyMethod;
    private Point mBottomRight;
    private int mCenterX;
    private int mCenterY;
    private Context mContext;
    private Method mHideMethod;
    private List<Bitmap> mOverlayList;
    private float mScale;
    private Method mShowMethod;
    private Surface mSurface;
    private Object mSurfaceControl;
    private Class<?> mSurfaceControlClazz;
    private Class<?> mSurfaceControlTransactionClazz;
    private Point mTopLeft;
    private Object mTransaction;

    FpLayer(Context context, int centerX, int centerY, float scale, List<String> animationRes) {
        this.mContext = context;
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mScale = scale;
        this.mAnimationRes = animationRes;
        String str = TAG;
        Log.i(str, "mCenterX = " + this.mCenterX + ",mCenterY=" + this.mCenterY + ",mScale=" + this.mScale);
        List<String> list = this.mAnimationRes;
        if (list == null || list.isEmpty()) {
            this.mBottomRight = new Point(DEFAULT_SIZE, DEFAULT_SIZE);
        } else {
            String str2 = TAG;
            Log.i(str2, "mAnimationRes create resource = " + this.mAnimationRes.size());
            this.mOverlayList = new ArrayList(this.mAnimationRes.size());
            for (String animationName : this.mAnimationRes) {
                this.mOverlayList.add(BitmapFactory.decodeFile(animationName));
            }
            if (!this.mOverlayList.isEmpty()) {
                this.mBottomRight = new Point(this.mOverlayList.get(0).getWidth(), this.mOverlayList.get(0).getHeight());
            } else {
                this.mBottomRight = new Point(DEFAULT_SIZE, DEFAULT_SIZE);
            }
        }
        this.mTopLeft = new Point(0, 0);
        try {
            this.mSurfaceControlClazz = Class.forName("android.view.SurfaceControl");
            this.mSurfaceControlTransactionClazz = Class.forName("android.view.SurfaceControl$Transaction");
            this.mTransaction = this.mSurfaceControlTransactionClazz.newInstance();
            this.mShowMethod = this.mSurfaceControlTransactionClazz.getMethod("show", this.mSurfaceControlClazz);
            this.mHideMethod = this.mSurfaceControlTransactionClazz.getMethod("hide", this.mSurfaceControlClazz);
            this.mApplyMethod = this.mSurfaceControlTransactionClazz.getMethod("apply", new Class[0]);
            prepareSurfaceControl(this.mTopLeft, this.mBottomRight);
            prepareSurface();
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "Get SurfaceControl failed");
        }
    }

    private void prepareSurface() throws ReflectiveOperationException {
        Class<?> surfaceClazz = Class.forName("android.view.Surface");
        Object objectSurface = surfaceClazz.newInstance();
        if (objectSurface instanceof Surface) {
            this.mSurface = (Surface) objectSurface;
            surfaceClazz.getMethod("copyFrom", this.mSurfaceControl.getClass()).invoke(this.mSurface, this.mSurfaceControl);
            Log.i(TAG, "Create Surface success");
        }
    }

    private void prepareSurfaceControl(Point left, Point right) throws ReflectiveOperationException {
        Class<?> sessionClazz = Class.forName("android.view.SurfaceSession");
        Class<?> surfaceBuilderClazz = Class.forName("android.view.SurfaceControl$Builder");
        Object builder = surfaceBuilderClazz.getConstructor(sessionClazz).newInstance(sessionClazz.newInstance());
        surfaceBuilderClazz.getMethod("setName", String.class).invoke(builder, "fingerprint_animation_layer");
        surfaceBuilderClazz.getMethod("setBufferSize", Integer.TYPE, Integer.TYPE).invoke(builder, Integer.valueOf(right.x), Integer.valueOf(right.y));
        surfaceBuilderClazz.getMethod("setFlags", Integer.TYPE).invoke(builder, this.mSurfaceControlClazz.getField("HIDDEN").get(null));
        surfaceBuilderClazz.getMethod("setFormat", Integer.TYPE).invoke(builder, -3);
        this.mSurfaceControl = surfaceBuilderClazz.getMethod("build", new Class[0]).invoke(builder, new Object[0]);
        BigDecimal rightBigx = new BigDecimal(right.x);
        BigDecimal rightBigy = new BigDecimal(right.y);
        BigDecimal pictureHalf = new BigDecimal(2);
        this.mTransaction.getClass().getMethod("setPosition", this.mSurfaceControlClazz, Float.TYPE, Float.TYPE).invoke(this.mTransaction, this.mSurfaceControl, Float.valueOf((((float) this.mCenterX) * this.mScale) - rightBigx.divide(pictureHalf, 3, RoundingMode.HALF_UP).floatValue()), Float.valueOf((((float) this.mCenterY) * this.mScale) - rightBigy.divide(pictureHalf, 3, RoundingMode.HALF_UP).floatValue()));
        this.mTransaction.getClass().getMethod("setLayer", this.mSurfaceControlClazz, Integer.TYPE).invoke(this.mTransaction, this.mSurfaceControl, Integer.valueOf((int) MASK_LAYER));
        this.mApplyMethod.invoke(this.mTransaction, new Object[0]);
        String str = TAG;
        Log.i(str, "Create SurfaceControl success from right.x=" + right.x + " right.y= " + right.y + " mCenterX=" + this.mCenterX + " mCenterY=" + this.mCenterY + " left.x=" + left.x + " left.y=" + left.y);
    }

    public void draw(int index) {
        List<Bitmap> list;
        if (this.mSurface == null || (list = this.mOverlayList) == null || list.isEmpty()) {
            Log.w(TAG, "Surface not created");
        } else if (this.mOverlayList.size() <= index) {
            Log.w(TAG, "index over");
        } else {
            Canvas canvas = null;
            try {
                canvas = this.mSurface.lockCanvas(null);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException cannot get Canvas");
            }
            if (canvas == null) {
                Log.w(TAG, "canvas null");
                return;
            }
            Paint paint = new Paint();
            paint.setColor(-1);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            Path path = new Path();
            path.addRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), Path.Direction.CW);
            canvas.drawPath(path, paint);
            canvas.drawBitmap(this.mOverlayList.get(index), 0.0f, 0.0f, (Paint) null);
            this.mSurface.unlockCanvasAndPost(canvas);
            Log.i(TAG, "Draw fingerprint animation layer success");
        }
    }

    public void show() {
        if (this.mSurfaceControl == null || this.mShowMethod == null || this.mTransaction == null || this.mApplyMethod == null) {
            Log.e(TAG, "Show fingerprint animation layer failed");
            return;
        }
        Log.i(TAG, "Show fingerprint animation layer");
        try {
            this.mShowMethod.invoke(this.mTransaction, this.mSurfaceControl);
            this.mApplyMethod.invoke(this.mTransaction, new Object[0]);
        } catch (IllegalArgumentException | ReflectiveOperationException e) {
            Log.w(TAG, "Show fingerprint animation layer failed");
        }
    }

    public void setVisible(boolean isVisible) {
        draw(isVisible ? -16777216 : 0);
    }

    public void hide() {
        if (this.mSurfaceControl == null || this.mHideMethod == null || this.mTransaction == null || this.mApplyMethod == null) {
            Log.w(TAG, "Hide fingerprint animation layer failed");
            return;
        }
        Log.i(TAG, "Hide fingerprint animation layer");
        try {
            this.mHideMethod.invoke(this.mTransaction, this.mSurfaceControl);
            this.mApplyMethod.invoke(this.mTransaction, new Object[0]);
        } catch (IllegalArgumentException | ReflectiveOperationException e) {
            Log.w(TAG, "Hide fingerprint animation layer failed");
        }
    }

    public void destroy() {
        try {
            if (this.mSurfaceControl != null) {
                Log.i(TAG, "Destroy SurfaceControl");
                this.mSurfaceControl.getClass().getMethod("remove", new Class[0]).invoke(this.mSurfaceControl, new Object[0]);
                if (this.mTransaction != null) {
                    this.mApplyMethod.invoke(this.mTransaction, new Object[0]);
                }
            }
            if (this.mTransaction != null) {
                this.mTransaction.getClass().getMethod("close", new Class[0]).invoke(this.mTransaction, new Object[0]);
            }
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "Destroy surface failed");
        }
    }
}
