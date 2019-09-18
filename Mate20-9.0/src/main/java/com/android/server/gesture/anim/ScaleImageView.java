package com.android.server.gesture.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import com.android.server.gesture.GestureNavConst;
import com.android.server.wm.PhaseInterpolator;
import java.util.Arrays;
import java.util.List;

public class ScaleImageView extends ImageView {
    public static final int ANIM_TYPE_RECOVER = 1;
    public static final int ANIM_TYPE_TRANSLATE = 0;
    private static final int ERROR_ID = -1;
    private static final float FOLLOW_TRANSLATE_RATIO = 0.1f;
    private static final int LANDSCAPE_FINAL_WIDTH = 316;
    private static final String LEFT = "left";
    private static final float MAX_SCALE_DISTANCE = 500.0f;
    private static final float MAX_SCALE_RATIO = 0.7f;
    private static final int NAVIGATIONBAR_LAYER = 231000;
    private static final int PICTURE_ROUND = 2;
    private static final int PORTRAIT_FINAL_WIDTH = 328;
    private static final long RECOVER_ANIMATION_DURATION = 100;
    private static final String RIGHT = "right";
    private static final float SCALE = 0.75f;
    private static final float SCALE_X_PIVOT = 0.5f;
    private static final float SCALE_Y_PIVOT = 0.625f;
    private static final int STATE_LEFT = 1;
    private static final int STATE_MIDDLE = 0;
    private static final int STATE_RIGHT = 2;
    private static final String TAG = "GestureScale";
    private static final long TRANSLATE_ANIMATION_DURATION = 300;
    private static final int TYPE_LANDSCAPE = 1;
    private static final int TYPE_PORTRAIT = 0;
    private TimeInterpolator[] mAlphaInterpolators = {this.mConstantInterpolator, new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.6f, 1.0f), this.mConstantInterpolator};
    private ActivityManager mAm;
    /* access modifiers changed from: private */
    public TranslateAnimationListener mAnimationListener;
    private TimeInterpolator mConstantInterpolator = new TimeInterpolator() {
        public float getInterpolation(float input) {
            return 1.0f;
        }
    };
    private Context mContext;
    private boolean mFollowStart;
    private float mFollowStartX;
    private float mFollowStartY;
    private float mHeight;
    private int mScreenOrientation;
    private TimeInterpolator[] mSizeBigInterpolators = {new PathInterpolator(0.44f, 0.43f, 0.7f, 0.75f), new PathInterpolator(0.13f, 0.79f, 0.3f, 1.0f)};
    private TimeInterpolator[] mSizeSmallInterpolators = {new PathInterpolator(0.41f, 0.38f, 0.7f, 0.71f), new PathInterpolator(0.16f, 0.64f, 0.33f, 1.0f)};
    private float mWidth;

    public interface TranslateAnimationListener {
        void onAnimationEnd(int i);
    }

    public ScaleImageView(Context context) {
        super(context);
        init(context);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "init");
        this.mFollowStart = false;
        this.mContext = context;
        this.mAm = (ActivityManager) context.getSystemService(ActivityManager.class);
        initWms();
    }

    private void initWms() {
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        this.mWidth = (float) w;
        this.mHeight = (float) h;
        Log.d(TAG, "view width is " + this.mWidth + ", height is " + this.mHeight);
        float pivotX = this.mWidth * 0.5f;
        float pivotY = this.mHeight * SCALE_Y_PIVOT;
        setPivotX(pivotX);
        setPivotY(pivotY);
        Log.d(TAG, "view pivotX is " + pivotX + ", pivotY is " + pivotY);
    }

    private static int dp2px(Context context, int dp) {
        if (context != null && dp > 0) {
            return (int) TypedValue.applyDimension(1, (float) dp, context.getResources().getDisplayMetrics());
        }
        Log.e(TAG, "dp2px parameters error.");
        return 0;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == 1) {
            this.mScreenOrientation = 0;
        } else {
            this.mScreenOrientation = 1;
        }
        Log.d(TAG, "mScreenOrientation " + this.mScreenOrientation);
    }

    public void setFollowPosition(float x, float y) {
        if (!this.mFollowStart) {
            this.mFollowStartX = x;
            this.mFollowStartY = y;
            this.mFollowStart = true;
            return;
        }
        doScale(this.mFollowStartY < y ? GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO : this.mFollowStartY - y);
        doTranslate(x - this.mFollowStartX, this.mFollowStartY - y, 0.1f);
    }

    private void doScale(float y) {
        float scale;
        if (y >= MAX_SCALE_DISTANCE) {
            scale = 0.7f;
        } else {
            scale = 1.0f - (0.3f * (y / MAX_SCALE_DISTANCE));
        }
        setScaleX(scale);
        setScaleY(scale);
    }

    private void doTranslate(float x, float y, float transRatio) {
        float moveX = x * transRatio;
        float moveY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (y > MAX_SCALE_DISTANCE) {
            moveY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO - ((y - MAX_SCALE_DISTANCE) * transRatio);
        } else if (y < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            moveY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO + ((-y) * transRatio);
        }
        setX(moveX);
        setY(moveY);
    }

    public void refreshContent() {
        Log.d(TAG, "refreshContent");
        long oldT = System.currentTimeMillis();
        setBackgroundColor(-7829368);
        refreshBySurfaceControl();
        setVisibility(0);
        long newT = System.currentTimeMillis();
        Log.d(TAG, "refresh content cost time " + (newT - oldT));
    }

    private static Bitmap addRoundOnBitmap(Bitmap source, float round) {
        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
        RectF rectf = new RectF(rect);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectf, round, round, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, rect, rect, paint);
        return bitmap;
    }

    private void refreshContentByActivityManager() {
        Log.d(TAG, "refreshContentByActivityManager");
        int topTaskId = getTopTaskId();
        if (topTaskId == -1) {
            Log.e(TAG, "error top task id");
            return;
        }
        Bitmap snapshot = getTaskSnapshot(topTaskId);
        if (snapshot == null) {
            Log.w(TAG, "task snapshot is null, try to get task thumbnail");
            snapshot = getTaskThumbnail(topTaskId);
        }
        if (snapshot != null) {
            Log.d(TAG, "set snapshot bitmap into ImageView");
            setScaleType(ImageView.ScaleType.FIT_CENTER);
            setImageBitmap(addRoundOnBitmap(snapshot, (float) dp2px(this.mContext, 2)));
        } else {
            Log.d(TAG, "no snapshot, use color.");
            setBackgroundColor(-7829368);
        }
    }

    private int getTopTaskId() {
        List<ActivityManager.RunningTaskInfo> tasks = this.mAm.getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) {
            Log.d(TAG, "tasks count is 0");
            return -1;
        }
        Log.d(TAG, "tasks count is " + tasks.size());
        return tasks.get(0).id;
    }

    private Bitmap getTaskSnapshot(int topTaskId) {
        Log.d(TAG, "getTaskSnapshot with top task id " + topTaskId);
        try {
            ActivityManager.TaskSnapshot taskSnapshot = ActivityManager.getService().getTaskSnapshot(topTaskId, false);
            if (taskSnapshot == null) {
                Log.e(TAG, "error, taskSnapshot is null");
                return null;
            }
            Bitmap snapshot = Bitmap.createHardwareBitmap(taskSnapshot.getSnapshot());
            if (snapshot == null) {
                Log.e(TAG, "error, snapshot is null");
            }
            return snapshot;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException");
            return null;
        }
    }

    private Bitmap getTaskThumbnail(int topTaskId) {
        Log.d(TAG, "getTaskThumbnail with top task id " + topTaskId);
        return null;
    }

    private void refreshBySurfaceControl() {
        Log.d(TAG, "refreshBySurfaceControl");
        setScaleType(ImageView.ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        matrix.setTranslate(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, (float) getStatusBarHeight(this.mContext));
        setImageMatrix(matrix);
        Bitmap bitmap = screenShotBitmap(this.mContext, 0, Integer.MAX_VALUE);
        if (bitmap == null) {
            setBackgroundColor(-7829368);
            return;
        }
        setScaleType(ImageView.ScaleType.FIT_CENTER);
        setImageBitmap(addRoundOnBitmap(bitmap, (float) dp2px(this.mContext, 2)));
    }

    public static Bitmap screenShotBitmap(Context ctx, int minLayer, int maxLayer) {
        return screenShotBitmap(ctx, 1.0f);
    }

    public static Bitmap screenShotBitmap(Context ctx, float scale) {
        Bitmap bitmap;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        DisplayMetrics displayMetricsBody = new DisplayMetrics();
        Display display = ((WindowManager) ctx.getSystemService("window")).getDefaultDisplay();
        display.getMetrics(displayMetricsBody);
        display.getRealMetrics(displayMetrics);
        Log.d(TAG, "Display getMetrics w:" + displayMetricsBody.widthPixels + ", h:" + displayMetricsBody.heightPixels);
        Log.d(TAG, "Display getRealMetrics w:" + displayMetrics.widthPixels + ", h:" + displayMetrics.heightPixels);
        int[] dims = {(((int) (((float) displayMetrics.widthPixels) * scale)) / 2) * 2, (((int) (((float) displayMetrics.heightPixels) * scale)) / 2) * 2};
        int[] dimsBody = {(((int) (((float) displayMetricsBody.widthPixels) * scale)) / 2) * 2, (((int) (((float) displayMetricsBody.heightPixels) * scale)) / 2) * 2};
        Log.d(TAG, "mFingerViewParams, dims[0] =" + dims[0] + ", dims[1] =" + dims[1]);
        Log.d(TAG, "mFingerViewParams, dimsBody[0] =" + dimsBody[0] + ", dimsBody[1] =" + dimsBody[1]);
        if (isLazyMode(ctx)) {
            Rect screenshotRect = getScreenshotRect(ctx);
        } else {
            new Rect();
        }
        Rect screenRect = new Rect(0, getStatusBarHeight(ctx), dimsBody[0], dimsBody[1]);
        int rotation = display.getRotation();
        if (rotation == 0 || 2 == rotation) {
            Log.d(TAG, "SurfaceControl.screenshot_ext_hw with rotation " + rotation);
            bitmap = SurfaceControl.screenshot_ext_hw(screenRect, dimsBody[0], dimsBody[1], 0, NAVIGATIONBAR_LAYER, false, converseRotation(rotation));
        } else {
            bitmap = rotationScreenBitmap(screenRect, rotation, dimsBody, 0, NAVIGATIONBAR_LAYER);
        }
        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, dimsBody[0], dimsBody[1]);
        if (bitmap2 == null) {
            Log.e(TAG, "screenShotBitmap error bitmap is null");
            return null;
        }
        bitmap2.prepareToDraw();
        return bitmap2;
    }

    private static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private static int converseRotation(int rotation) {
        switch (rotation) {
            case 1:
                return 3;
            case 2:
                return 2;
            case 3:
                return 1;
            default:
                return 0;
        }
    }

    private static float convertRotationToDegrees(int rotation) {
        switch (rotation) {
            case 1:
                return 270.0f;
            case 2:
                return 180.0f;
            case 3:
                return 90.0f;
            default:
                return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static Bitmap rotationScreenBitmap(Rect rect, int rotation, int[] srcDims, int minLayer, int maxLayer) {
        StringBuilder sb = new StringBuilder();
        sb.append("rotationScreenBitmap with rotation ");
        sb.append(rotation);
        sb.append(", srcDims ");
        sb.append(Arrays.toString(srcDims));
        sb.append(", layer ");
        int i = minLayer;
        sb.append(i);
        sb.append(",");
        int i2 = maxLayer;
        sb.append(i2);
        Log.d(TAG, sb.toString());
        float degrees = convertRotationToDegrees(rotation);
        float[] dims = {(float) srcDims[0], (float) srcDims[1]};
        Matrix metrics = new Matrix();
        metrics.reset();
        metrics.preRotate(-degrees);
        metrics.mapPoints(dims);
        dims[0] = Math.abs(dims[0]);
        dims[1] = Math.abs(dims[1]);
        Bitmap bitmap = SurfaceControl.screenshot(rect, (int) dims[0], (int) dims[1], i, i2, false, 0);
        Bitmap ss = Bitmap.createBitmap(srcDims[0], srcDims[1], Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(ss);
        c.translate(((float) srcDims[0]) * 0.5f, ((float) srcDims[1]) * 0.5f);
        c.rotate(degrees);
        c.translate((-dims[0]) * 0.5f, (-dims[1]) * 0.5f);
        c.drawBitmap(bitmap, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, null);
        bitmap.recycle();
        return ss;
    }

    private static int getLazyState(Context context) {
        String str = Settings.Global.getString(context.getContentResolver(), "single_hand_mode");
        if (str == null || "".equals(str)) {
            return 0;
        }
        if (str.contains(LEFT)) {
            return 1;
        }
        if (str.contains(RIGHT)) {
            return 2;
        }
        return 0;
    }

    private static boolean isLazyMode(Context context) {
        return getLazyState(context) != 0;
    }

    private static Rect getScreenshotRect(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        if (windowManager == null) {
            return new Rect();
        }
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        Rect sourceCrop = null;
        int state = getLazyState(context);
        if (1 == state) {
            sourceCrop = new Rect(0, (int) (((float) displayMetrics.heightPixels) * 0.25f), (int) (((float) displayMetrics.widthPixels) * 0.75f), displayMetrics.heightPixels);
        } else if (2 == state) {
            sourceCrop = new Rect((int) (((float) displayMetrics.widthPixels) * 0.25f), (int) (((float) displayMetrics.heightPixels) * 0.25f), displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return sourceCrop;
    }

    public void playTranslateAnimation(int topDistance) {
        int finalWidth;
        this.mFollowStart = false;
        float oldX = getX();
        float oldY = getY();
        float oldScaleX = getScaleX();
        float oldScaleY = getScaleY();
        if (this.mScreenOrientation == 0) {
            finalWidth = dp2px(this.mContext, PORTRAIT_FINAL_WIDTH);
        } else {
            finalWidth = dp2px(this.mContext, LANDSCAPE_FINAL_WIDTH);
        }
        float newScale = ((float) finalWidth) / this.mWidth;
        float newY = ((float) topDistance) - ((this.mHeight - (this.mHeight * newScale)) / 2.0f);
        PropertyValuesHolder xProperty = PropertyValuesHolder.ofFloat("x", new float[]{oldX, 0.0f});
        PropertyValuesHolder yProperty = PropertyValuesHolder.ofFloat("y", new float[]{oldY, newY});
        PropertyValuesHolder xScaleProperty = PropertyValuesHolder.ofFloat("scaleX", new float[]{oldScaleX, newScale});
        float f = oldX;
        PropertyValuesHolder yScaleProperty = PropertyValuesHolder.ofFloat("scaleY", new float[]{oldScaleY, newScale});
        ObjectAnimator transAnimator = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{xProperty, yProperty, xScaleProperty, yScaleProperty, PropertyValuesHolder.ofFloat("alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO})});
        transAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563661));
        PropertyValuesHolder propertyValuesHolder = yScaleProperty;
        float f2 = oldY;
        transAnimator.setDuration(TRANSLATE_ANIMATION_DURATION);
        transAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                Log.d(ScaleImageView.TAG, "onAnimationEnd translate");
                ScaleImageView.this.setVisibility(8);
                ScaleImageView.this.setScaleX(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
                ScaleImageView.this.setScaleY(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
                ScaleImageView.this.setX(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
                ScaleImageView.this.setY(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
                ScaleImageView.this.setAlpha(1.0f);
                if (ScaleImageView.this.mAnimationListener != null) {
                    ScaleImageView.this.mAnimationListener.onAnimationEnd(0);
                }
            }
        });
        transAnimator.start();
    }

    public void playRecoverAnimation() {
        this.mFollowStart = false;
        float oldX = getX();
        float oldY = getY();
        float oldScaleX = getScaleX();
        float oldScaleY = getScaleY();
        ObjectAnimator recover = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("x", new float[]{oldX, 0.0f}), PropertyValuesHolder.ofFloat("y", new float[]{oldY, 0.0f}), PropertyValuesHolder.ofFloat("scaleX", new float[]{oldScaleX, 1.0f}), PropertyValuesHolder.ofFloat("scaleY", new float[]{oldScaleY, 1.0f})});
        recover.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563661));
        recover.setDuration(RECOVER_ANIMATION_DURATION);
        recover.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                Log.d(ScaleImageView.TAG, "onAnimationEnd recover");
                ScaleImageView.this.setVisibility(8);
                if (ScaleImageView.this.mAnimationListener != null) {
                    ScaleImageView.this.mAnimationListener.onAnimationEnd(1);
                }
            }
        });
        recover.start();
    }

    public void playGestureToLauncherIconAnimation(boolean isIconView) {
    }

    private float[] calculateScalePivot(float px, float py, float fromWidth, float fromHeight, float toWidth, float toHeight, int[] fromLocInWindow) {
        float scaleX = toWidth / fromWidth;
        float scaleY = toHeight / fromHeight;
        return new float[]{((px - (toWidth / 2.0f)) - (((float) fromLocInWindow[0]) * scaleX)) / (1.0f - scaleX), ((py - (toHeight / 2.0f)) - (((float) fromLocInWindow[1]) * scaleY)) / (1.0f - scaleY)};
    }

    private Animation createGestureToLauncherIconAnimation(float targetPx, float targetPy, int iconWidth, int iconHeight, float fromAlpha, float toAlpha) {
        TimeInterpolator[] sizeXInterpolators;
        TimeInterpolator[] sizeYInterpolators;
        PhaseInterpolator contentAnimSet = new AnimationSet(false);
        long duration = SystemProperties.getLong("to_launcher_dur", 350);
        AlphaAnimation contentAlphaAnim = new AlphaAnimation(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f);
        contentAlphaAnim.setDuration(duration);
        float[] alphaInValues = {GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.16f, 0.32f, 1.0f};
        float[] contentAlphaOutValues = {fromAlpha, fromAlpha, toAlpha, toAlpha};
        PhaseInterpolator contentAlphaInterpolator = new PhaseInterpolator(alphaInValues, contentAlphaOutValues, this.mAlphaInterpolators);
        contentAlphaAnim.setInterpolator(contentAlphaInterpolator);
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        int windowWidth = wm.getDefaultDisplay().getWidth();
        int windowHeight = wm.getDefaultDisplay().getHeight();
        int viewHeight = (int) (((float) getMeasuredHeight()) * getScaleY());
        int viewWidth = (int) (((float) getMeasuredWidth()) * getScaleX());
        int[] viewLocInWindow = new int[2];
        getLocationInWindow(viewLocInWindow);
        int windowWidth2 = windowWidth;
        StringBuilder sb = new StringBuilder();
        WindowManager windowManager = wm;
        sb.append("[viewWidth, viewHeight, viewLocX, viewLocY] = [");
        sb.append(viewWidth);
        sb.append(", ");
        sb.append(viewHeight);
        sb.append(", ");
        PhaseInterpolator phaseInterpolator = contentAlphaInterpolator;
        sb.append(viewLocInWindow[0]);
        sb.append(", ");
        sb.append(viewLocInWindow[1]);
        sb.append("]");
        Log.d(TAG, sb.toString());
        float fromWidth = (float) viewWidth;
        float fromHeight = (float) viewHeight;
        float toWidth = (float) iconWidth;
        PhaseInterpolator contentAnimSet2 = contentAnimSet;
        float toHeight = (float) iconHeight;
        float scaleToX = toWidth / fromWidth;
        float scaleToY = toHeight / fromHeight;
        boolean isHorizontal = viewWidth > viewHeight;
        float middleYRatio = 0.44f;
        float middleXRatio = isHorizontal ? 0.54f : 0.44f;
        if (!isHorizontal) {
            middleYRatio = 0.54f;
        }
        float middleY = 1.0f - (((fromHeight - toHeight) * middleYRatio) / fromHeight);
        int windowWidth3 = windowWidth2;
        float[] fArr = contentAlphaOutValues;
        int windowHeight2 = windowHeight;
        int viewHeight2 = viewHeight;
        float f = fromWidth;
        float[] fArr2 = alphaInValues;
        float[] pivot = calculateScalePivot(targetPx, targetPy, fromWidth, fromHeight, toWidth, toHeight, viewLocInWindow);
        float pivotX = pivot[0];
        float pivotY = pivot[1];
        float[] scaleInValues = {GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.16f, 1.0f};
        float[] scaleOutValuesX = {1.0f, 1.0f - (((fromWidth - toWidth) * middleXRatio) / fromWidth), scaleToX};
        float[] scaleOutValuesY = {1.0f, middleY, scaleToY};
        if (isHorizontal) {
            sizeXInterpolators = this.mSizeBigInterpolators;
        } else {
            sizeXInterpolators = this.mSizeSmallInterpolators;
        }
        if (isHorizontal) {
            sizeYInterpolators = this.mSizeSmallInterpolators;
        } else {
            sizeYInterpolators = this.mSizeBigInterpolators;
        }
        PhaseInterpolator interpolatorX = new PhaseInterpolator(scaleInValues, scaleOutValuesX, sizeXInterpolators);
        float[] fArr3 = pivot;
        PhaseInterpolator interpolatorY = new PhaseInterpolator(scaleInValues, scaleOutValuesY, sizeYInterpolators);
        float f2 = pivotX;
        float f3 = pivotY;
        ScaleAnimation contentScaleAnimX = new ScaleAnimation(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f, 1.0f, 1.0f, f2, f3);
        float[] fArr4 = scaleOutValuesY;
        ScaleAnimation contentScaleAnimX2 = contentScaleAnimX;
        contentScaleAnimX2.setDuration(duration);
        contentScaleAnimX2.setInterpolator(interpolatorX);
        ScaleAnimation contentScaleAnimY = new ScaleAnimation(1.0f, 1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f, f2, f3);
        float f4 = pivotX;
        ScaleAnimation contentScaleAnimY2 = contentScaleAnimY;
        contentScaleAnimY2.setDuration(duration);
        contentScaleAnimY2.setInterpolator(interpolatorY);
        PhaseInterpolator phaseInterpolator2 = interpolatorY;
        PhaseInterpolator interpolatorY2 = contentAnimSet2;
        interpolatorY2.addAnimation(contentAlphaAnim);
        interpolatorY2.addAnimation(contentScaleAnimX2);
        interpolatorY2.addAnimation(contentScaleAnimY2);
        ScaleAnimation scaleAnimation = contentScaleAnimX2;
        ScaleAnimation scaleAnimation2 = contentScaleAnimY2;
        float f5 = pivotY;
        int windowWidth4 = windowWidth3;
        interpolatorY2.initialize(viewWidth, viewHeight2, windowWidth4, windowHeight2);
        int i = windowWidth4;
        interpolatorY2.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                ScaleImageView.this.setVisibility(8);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        return interpolatorY2;
    }

    public void setAnimationListener(TranslateAnimationListener listener) {
        this.mAnimationListener = listener;
    }
}
