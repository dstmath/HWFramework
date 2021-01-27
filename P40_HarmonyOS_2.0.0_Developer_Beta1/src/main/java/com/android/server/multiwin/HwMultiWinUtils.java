package com.android.server.multiwin;

import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerGlobal;
import android.os.UserHandle;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.SurfaceControl;
import android.widget.ImageView;
import com.android.server.multiwin.listener.BlurListener;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.HwMultiWindowManager;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.UserHandleEx;
import com.huawei.server.magicwin.DefaultHwMagicWindowManagerService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class HwMultiWinUtils {
    private static final String BLUR_THREAD_NAME = "HwMultiWindow - BlurScreenShotThread";
    private static final String COLOR_STR_PREFIX = "#";
    private static final int CORE_THREADS_NUM = 1;
    private static final int DEFAULT = -1;
    private static final int DEFAULT_BLUR_RADIUS = 15;
    private static final double EQ_DELTA_TH = 1.0E-6d;
    private static final String FLOATING_BALL_BG_COLOR = "#FFFFFFFF";
    private static final String FLOATING_BALL_BG_COLOR_NIGHT = "#FF262626";
    private static final float FLOATING_BALL_RADIUS_RATIO = 0.84f;
    private static final String FLOAT_TASK_STATE_KEY = "float_task_state";
    private static final int GESTURE_NAVIGATION_CLOSE = 0;
    private static final int GESTURE_NAVIGATION_OPEN = 1;
    private static final long KEEP_ALIVE_DURATION = 60;
    private static final String KEY_SECURE_GESTURE_NAVIGATION = "secure_gesture_navigation";
    private static final int MAX_THREADS_NUM = 5;
    private static final String NAV_BAR_COLOR = "#FFFCFCFC";
    private static final String NAV_BAR_COLOR_NIGHT = "#FF000000";
    private static final String NOTEPAD_OLD_PACKAGE_NAME = "com.example.android.notepad";
    private static final String NOTEPAD_PACKAGE_NAME = "com.huawei.notepad";
    private static final String QUICK_NOTE_CLASS_NAME = "com.example.android.notepad.QuickNoteActivity";
    private static final String QUICK_NOTE_ICON_RES = "ic_quick_note_dock";
    private static final int SAMPLE_SIZE = 12;
    private static final String TAG = "HwMultiWinUtils";
    private static final String ZERO_STR = "0";
    private static ThreadPoolExecutor sPoolExecutor;

    private HwMultiWinUtils() {
    }

    public static boolean floatEquals(float f1, float f2) {
        return ((double) ((f1 > f2 ? 1 : (f1 == f2 ? 0 : -1)) > 0 ? f1 - f2 : f2 - f1)) <= EQ_DELTA_TH;
    }

    public static void blurForScreenShot(Bitmap inputBitmap, ImageView dstImageView, BlurListener blurListener, ImageView.ScaleType scaleType, int blurRadius) {
        if (inputBitmap == null || dstImageView == null) {
            Log.w(TAG, "blurForScreenShot failed, cause inputBitmap or dstImageView is null");
            return;
        }
        if (sPoolExecutor == null) {
            sPoolExecutor = new ThreadPoolExecutor(1, 5, KEEP_ALIVE_DURATION, TimeUnit.SECONDS, new LinkedBlockingQueue(), $$Lambda$HwMultiWinUtils$RY3fFIqVfo42JFoSnwPH0DNILAw.INSTANCE, new ThreadPoolExecutor.DiscardOldestPolicy());
        }
        sPoolExecutor.execute(new Runnable(dstImageView, inputBitmap, blurRadius, scaleType, blurListener) {
            /* class com.android.server.multiwin.$$Lambda$HwMultiWinUtils$rtOUP69xPNKsUdySRZpkRh7WRag */
            private final /* synthetic */ ImageView f$0;
            private final /* synthetic */ Bitmap f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ ImageView.ScaleType f$3;
            private final /* synthetic */ BlurListener f$4;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwMultiWinUtils.lambda$blurForScreenShot$2(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    static /* synthetic */ Thread lambda$blurForScreenShot$0(Runnable runnable) {
        return new Thread(runnable, BLUR_THREAD_NAME);
    }

    static /* synthetic */ void lambda$blurForScreenShot$2(ImageView dstImageView, Bitmap inputBitmap, int blurRadius, ImageView.ScaleType scaleType, BlurListener blurListener) {
        Bitmap outputBitmap = rsBlur(dstImageView.getContext(), inputBitmap, blurRadius);
        if (scaleType != ImageView.ScaleType.FIT_XY) {
            outputBitmap = Bitmap.createScaledBitmap(outputBitmap, inputBitmap.getWidth(), inputBitmap.getHeight(), true);
        }
        dstImageView.post(new Runnable(dstImageView, scaleType, outputBitmap, blurListener) {
            /* class com.android.server.multiwin.$$Lambda$HwMultiWinUtils$9rjgYv7IvYLT4z2G4GghCEXi2BU */
            private final /* synthetic */ ImageView f$0;
            private final /* synthetic */ ImageView.ScaleType f$1;
            private final /* synthetic */ Bitmap f$2;
            private final /* synthetic */ BlurListener f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwMultiWinUtils.lambda$blurForScreenShot$1(this.f$0, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    static /* synthetic */ void lambda$blurForScreenShot$1(ImageView dstImageView, ImageView.ScaleType scaleType, Bitmap dstBitmap, BlurListener blurListener) {
        dstImageView.setScaleType(scaleType);
        dstImageView.setImageDrawable(bitmap2Drawable(dstBitmap));
        if (blurListener != null) {
            blurListener.onBlurDone();
        }
    }

    public static void blurForScreenShot(ImageView sourceImageView, ImageView dstImageView, BlurListener blurListener, ImageView.ScaleType scaleType) {
        blurForScreenShot(drawable2Bitmap(sourceImageView.getDrawable()), dstImageView, blurListener, scaleType, 15);
    }

    public static void blurForScreenShot(Bitmap inputBitmap, ImageView dstImageView, BlurListener blurListener, ImageView.ScaleType scaleType) {
        blurForScreenShot(inputBitmap, dstImageView, blurListener, scaleType, 15);
    }

    public static int dip2px(Context context, float dipValue) {
        return (int) ((dipValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Log.w(TAG, "drawable2Bitmap: drawable = " + drawable + " is not instance of BitmapDrawable");
        return null;
    }

    public static Bitmap rsBlur(Context context, Bitmap bmp, int radius) {
        if (context == null) {
            Log.w(TAG, "context is null, rsBlur failed!");
            return bmp;
        } else if (bmp == null) {
            Log.w(TAG, "bmp is null, ruBlur failed!");
            return bmp;
        } else {
            Bitmap blurBmp = Bitmap.createScaledBitmap(bmp, Math.round(((float) bmp.getWidth()) / 12.0f), Math.round(((float) bmp.getHeight()) / 12.0f), false);
            if (blurBmp == null) {
                Log.w(TAG, "rsBlur failed, cause blurBmp is null!");
                return bmp;
            }
            RenderScript renderScript = RenderScript.create(context);
            if (renderScript == null) {
                Log.w(TAG, "rsBlur failed, cause renderScript is null!");
                return bmp;
            }
            Allocation input = Allocation.createFromBitmap(renderScript, blurBmp);
            if (input == null) {
                Log.w(TAG, "rsBlur failed, cause input is null!");
                return bmp;
            }
            Allocation output = Allocation.createTyped(renderScript, input.getType());
            if (output == null) {
                Log.w(TAG, "rsBlur failed, cause output is null!");
                return bmp;
            }
            ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            if (scriptIntrinsicBlur == null) {
                Log.w(TAG, "rsBlur failed, cause scriptIntrinsicBlur is null!");
                return bmp;
            }
            scriptIntrinsicBlur.setInput(input);
            scriptIntrinsicBlur.setRadius((float) radius);
            scriptIntrinsicBlur.forEach(output);
            output.copyTo(blurBmp);
            renderScript.destroy();
            return blurBmp;
        }
    }

    public static Point getDisplaySize() {
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        if (display == null) {
            Log.w(TAG, "getDisplaySize failed, cause display is null!");
            return null;
        }
        Point displaySize = new Point();
        display.getRealSize(displaySize);
        return displaySize;
    }

    public static Bitmap takeScreenshot(int topHeightSubtraction) {
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        if (display == null) {
            Log.w(TAG, "takeScreenshot failed, cause display is null!");
            return null;
        }
        Point displaySize = getDisplaySize();
        if (displaySize == null) {
            return null;
        }
        int screenShotWidth = displaySize.x;
        int screenShotHeight = displaySize.y - topHeightSubtraction;
        int rotation = display.getRotation();
        Rect crop = new Rect(0, topHeightSubtraction, displaySize.x, displaySize.y);
        Log.i(TAG, "Taking screenshot of dimensions " + screenShotWidth + " x " + screenShotHeight + ", crop = " + crop);
        Bitmap screenShot = SurfaceControl.screenshot_ext_hw(crop, screenShotWidth, screenShotHeight, rotation);
        if (screenShot == null) {
            Log.w(TAG, "Failed to take screenshot of dimensions " + screenShotWidth + " x " + screenShotHeight);
            return null;
        }
        Bitmap softBmp = screenShot.copy(Bitmap.Config.ARGB_8888, true);
        if (softBmp == null) {
            Log.w(TAG, "Failed to copy soft bitmap!");
            return null;
        }
        softBmp.setHasAlpha(false);
        return softBmp;
    }

    public static Bitmap getScreenShotBmpWithoutNavBar(Bitmap src, int navBarPos, Rect navBarBound, float srcScale) {
        if (src == null) {
            Slog.w(TAG, "getScreenShotBmpWithoutNavBar failed, cause src is null!");
            return src;
        } else if (navBarPos == -1) {
            return src;
        } else {
            Bitmap temp = src;
            int navBarWidth = (int) (((float) navBarBound.width()) * srcScale);
            int navBarHeight = (int) (((float) navBarBound.height()) * srcScale);
            int sourceHeight = src.getHeight();
            int sourceWidth = src.getWidth();
            if (navBarPos == 1) {
                temp = Bitmap.createBitmap(src, navBarWidth, 0, sourceWidth - navBarWidth, sourceHeight);
            }
            if (navBarPos == 2) {
                temp = Bitmap.createBitmap(src, 0, 0, sourceWidth - navBarWidth, sourceHeight);
            }
            if (navBarPos == 4) {
                return Bitmap.createBitmap(src, 0, 0, sourceWidth, sourceHeight - navBarHeight);
            }
            return temp;
        }
    }

    public static int convertWindowMode2SplitMode(int windowMode, boolean isLandScape) {
        if (WindowConfiguration.isHwSplitScreenPrimaryWindowingMode(windowMode)) {
            return isLandScape ? 1 : 3;
        }
        if (WindowConfiguration.isHwSplitScreenSecondaryWindowingMode(windowMode)) {
            return isLandScape ? 2 : 4;
        }
        if (WindowConfiguration.isHwFreeFormWindowingMode(windowMode)) {
            return 5;
        }
        return 0;
    }

    public static boolean isNeedToResizeWithoutNavBar(int splitMode, int navBarPos) {
        if (splitMode == 1 && (navBarPos == 1 || navBarPos == 4)) {
            return true;
        }
        if (splitMode == 2 && (navBarPos == 2 || navBarPos == 4)) {
            return true;
        }
        if (splitMode == 3 && (navBarPos == 1 || navBarPos == 2)) {
            return true;
        }
        if (splitMode != 4) {
            return false;
        }
        if (navBarPos == 1 || navBarPos == 2 || navBarPos == 4) {
            return true;
        }
        return false;
    }

    public static boolean isNeedToFillNavBarRegion(int splitMode, int navBarPos) {
        if (splitMode == 1 && navBarPos == 1) {
            return true;
        }
        if (splitMode == 2 && navBarPos == 2) {
            return true;
        }
        if (splitMode == 4 && navBarPos == 4) {
            return true;
        }
        return false;
    }

    public static void fillNavBarRegionWithColor(Bitmap src, int navBarPos, Rect navBarBound, float srcScale, boolean isFocused, Context context, int safeSideWidth) {
        int i;
        float clipLength;
        Paint borderPaint;
        Canvas canvas;
        if (src == null) {
            Slog.w(TAG, "getScreenShotBmpWithoutNavBar failed, cause src is null!");
        } else if (navBarPos != -1) {
            int scaledNavBarWidth = (int) (((float) navBarBound.width()) * srcScale);
            int scaledNavBarHeight = (int) (((float) navBarBound.height()) * srcScale);
            float borderWidth = context.getResources().getDimension(34472616) * srcScale;
            float f = 0.0f;
            RectF boundRect = new RectF(0.0f, 0.0f, (float) src.getWidth(), (float) src.getHeight());
            Paint paint = new Paint();
            if (!isInNightMode(context)) {
                i = Color.parseColor(NAV_BAR_COLOR);
            } else {
                i = Color.parseColor(NAV_BAR_COLOR_NIGHT);
            }
            paint.setColor(i);
            paint.setStyle(Paint.Style.FILL);
            calculateBoundRect(navBarPos, boundRect, scaledNavBarWidth, scaledNavBarHeight, borderWidth, safeSideWidth);
            Canvas canvas2 = new Canvas(src);
            canvas2.save();
            canvas2.drawRect(boundRect, paint);
            canvas2.restore();
            if (!HwActivityManager.IS_PHONE) {
                f = context.getResources().getDimension(34472617) * srcScale;
            }
            int roundCornerRadius = (int) f;
            canvas2.save();
            RectF clipRect = new RectF(boundRect);
            float clipLength2 = ((float) roundCornerRadius) > borderWidth ? (float) roundCornerRadius : borderWidth;
            calculateClipRect(navBarPos, clipRect, clipLength2);
            canvas2.clipRect(clipRect);
            Paint borderPaint2 = prepareBorderPaint(context, isFocused, borderWidth * 2.0f);
            canvas2.drawRoundRect(boundRect, (float) roundCornerRadius, (float) roundCornerRadius, borderPaint2);
            canvas2.restore();
            if (roundCornerRadius > 0) {
                RectF r1 = new RectF();
                RectF r2 = new RectF();
                borderPaint = borderPaint2;
                clipLength = clipLength2;
                calculateCoverRectRegion(r1, r2, navBarPos, borderWidth, boundRect, (float) roundCornerRadius);
                Paint cornerCoverPaint = new Paint();
                cornerCoverPaint.setStyle(Paint.Style.FILL);
                canvas2.save();
                cornerCoverPaint.setColor(src.getPixel((int) ((r1.left + r1.right) / 2.0f), (int) ((r1.top + r1.bottom) / 2.0f)));
                canvas = canvas2;
                canvas.drawRect(r1, cornerCoverPaint);
                cornerCoverPaint.setColor(src.getPixel((int) ((r2.left + r2.right) / 2.0f), (int) ((r2.top + r2.bottom) / 2.0f)));
                canvas.drawRect(r2, cornerCoverPaint);
                canvas.restore();
            } else {
                canvas = canvas2;
                borderPaint = borderPaint2;
                clipLength = clipLength2;
            }
            drawMissedLines(canvas, navBarPos, boundRect, clipLength, borderPaint);
        }
    }

    private static Paint prepareBorderPaint(Context context, boolean isFocused, float borderWidth) {
        int borderColor;
        String str;
        String str2;
        String str3;
        Paint borderPaint = new Paint();
        if (isFocused && !isGestureNavigation(context.getContentResolver())) {
            borderColor = context.getColor(33883094);
        } else if (isInNightMode(context)) {
            borderColor = context.getColor(33883091);
        } else {
            borderColor = Color.parseColor(NAV_BAR_COLOR);
        }
        float alphaRatio = ((float) Color.alpha(borderColor)) / 255.0f;
        String red = Integer.toHexString((int) (((float) Color.red(borderColor)) * alphaRatio));
        String green = Integer.toHexString((int) (((float) Color.green(borderColor)) * alphaRatio));
        String blue = Integer.toHexString((int) (((float) Color.blue(borderColor)) * alphaRatio));
        StringBuilder sb = new StringBuilder();
        sb.append(COLOR_STR_PREFIX);
        if (red.length() > 1) {
            str = red;
        } else {
            str = "0" + red;
        }
        sb.append(str);
        if (green.length() > 1) {
            str2 = green;
        } else {
            str2 = "0" + green;
        }
        sb.append(str2);
        if (blue.length() > 1) {
            str3 = blue;
        } else {
            str3 = "0" + blue;
        }
        sb.append(str3);
        borderPaint.setColor(Color.parseColor(sb.toString()));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(borderWidth);
        return borderPaint;
    }

    private static void calculateBoundRect(int navBarPos, RectF boundRect, int scaledNavBarWidth, int scaledNavBarHeight, float borderWidth, int safeSideWidth) {
        if (navBarPos == 1) {
            boundRect.right = ((float) scaledNavBarWidth) + borderWidth;
            boundRect.top += (float) safeSideWidth;
            boundRect.bottom -= (float) safeSideWidth;
        }
        if (navBarPos == 2) {
            boundRect.left = (boundRect.right - ((float) scaledNavBarWidth)) - borderWidth;
            boundRect.top += (float) safeSideWidth;
            boundRect.bottom -= (float) safeSideWidth;
        }
        if (navBarPos == 4) {
            boundRect.top = (boundRect.bottom - ((float) scaledNavBarHeight)) - borderWidth;
            boundRect.left += (float) safeSideWidth;
            boundRect.right -= (float) safeSideWidth;
        }
    }

    private static void calculateClipRect(int navBarPos, RectF clipRect, float clipLength) {
        if (navBarPos == 1) {
            clipRect.right -= clipLength;
        }
        if (navBarPos == 2) {
            clipRect.left += clipLength;
        }
        if (navBarPos == 4) {
            clipRect.top += clipLength;
        }
    }

    private static void calculateCoverRectRegion(RectF r1, RectF r2, int navBarPos, float borderWidth, RectF boundRect, float roundCornerRadius) {
        if (navBarPos == 1) {
            r1.left = boundRect.right;
            r1.top = borderWidth;
            r1.right = r1.left + roundCornerRadius;
            r1.bottom = r1.top + roundCornerRadius;
            r2.left = r1.left;
            r2.right = r1.right;
            r2.bottom = boundRect.height() - borderWidth;
            r2.top = r2.bottom - roundCornerRadius;
        }
        if (navBarPos == 2) {
            r1.right = boundRect.left;
            r1.top = borderWidth;
            r1.left = r1.right - roundCornerRadius;
            r1.bottom = r1.top + roundCornerRadius;
            r2.right = r1.right;
            r2.left = r1.left;
            r2.bottom = boundRect.height() - borderWidth;
            r2.top = r2.bottom - roundCornerRadius;
        }
        if (navBarPos == 4) {
            r1.left = borderWidth;
            r1.right = r1.left + roundCornerRadius;
            r1.bottom = boundRect.top;
            r1.top = r1.bottom - roundCornerRadius;
            r2.top = r1.top;
            r2.bottom = r1.bottom;
            r2.right = boundRect.width() - borderWidth;
            r2.left = r2.right - roundCornerRadius;
        }
    }

    private static void drawMissedLines(Canvas canvas, int navBarPos, RectF boundRect, float clipLength, Paint paintStroke) {
        canvas.save();
        canvas.clipRect(boundRect);
        if (navBarPos == 1) {
            canvas.drawLine(boundRect.right - clipLength, boundRect.top, boundRect.right, boundRect.top, paintStroke);
            canvas.drawLine(boundRect.right - clipLength, boundRect.bottom, boundRect.right, boundRect.bottom, paintStroke);
        }
        if (navBarPos == 2) {
            canvas.drawLine(boundRect.left + clipLength, boundRect.top, boundRect.left - clipLength, boundRect.top, paintStroke);
            canvas.drawLine(boundRect.left + clipLength, boundRect.bottom, boundRect.left - clipLength, boundRect.bottom, paintStroke);
        }
        if (navBarPos == 4) {
            canvas.drawLine(boundRect.left, boundRect.top - clipLength, boundRect.left, boundRect.top + clipLength, paintStroke);
            canvas.drawLine(boundRect.right, boundRect.top - clipLength, boundRect.right, boundRect.top + clipLength, paintStroke);
        }
        canvas.restore();
    }

    public static Rect getBoundWithoutNavBar(int navBarPos, Rect navBarBound, Rect bound) {
        Rect outBound = new Rect(bound);
        if (navBarPos == 1) {
            outBound.left += navBarBound.width();
        }
        if (navBarPos == 2) {
            outBound.right -= navBarBound.width();
        }
        if (navBarPos == 4) {
            outBound.bottom -= navBarBound.height();
        }
        return outBound;
    }

    public static boolean isInNightMode(Context context) {
        if (context == null) {
            Log.w(TAG, "check if isInNightMode failed, cause context is null");
            return false;
        } else if ((context.getResources().getConfiguration().uiMode & 48) == 32) {
            return true;
        } else {
            return false;
        }
    }

    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    public static Drawable getAppIcon(Context context, String pkgName, int userId) {
        PackageManager pm;
        Slog.d(TAG, "get app icon: pkgName = " + pkgName);
        Drawable userBadgedIconDrawable = null;
        if (pkgName == null || (pm = context.getPackageManager()) == null) {
            return null;
        }
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfoAsUser(pkgName, 0, userId);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "icon not load!");
        }
        Drawable iconDrawable = info == null ? null : info.loadIcon(pm);
        if (iconDrawable == null) {
            Log.w(TAG, "getAppIcon failed, cause iconDrawable is null!");
            return iconDrawable;
        }
        UserHandle userHandle = UserHandleEx.getUserHandle(userId);
        if (userHandle != null) {
            userBadgedIconDrawable = pm.getUserBadgedIcon(iconDrawable, userHandle);
        }
        Drawable showedIconDrawable = userBadgedIconDrawable == null ? iconDrawable : userBadgedIconDrawable;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm == null) {
            Log.w(TAG, "getAppIcon failed, cause DisplayMetrics is null!");
            return showedIconDrawable;
        }
        float density = dm.density;
        Drawable icon = new BitmapDrawable(drawable2Bitmap(showedIconDrawable));
        icon.setBounds(0, 0, (int) (density * 68.0f), (int) (68.0f * density));
        return icon;
    }

    public static boolean isQuickNote(ComponentName component) {
        return component != null && (NOTEPAD_PACKAGE_NAME.equals(component.getPackageName()) || NOTEPAD_OLD_PACKAGE_NAME.equals(component.getPackageName())) && QUICK_NOTE_CLASS_NAME.equals(component.getClassName());
    }

    public static Drawable getQuickNoteIcon(Context context, int userId) {
        Drawable quickNoteIcon;
        if (context == null || (quickNoteIcon = getRawQuickNoteIcon(context, userId)) == null) {
            return null;
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm == null) {
            Log.w(TAG, "getQuickNoteIcon failed, DisplayMetrics is null!");
            return quickNoteIcon;
        }
        float density = dm.density;
        Drawable icon = new BitmapDrawable(drawable2Bitmap(quickNoteIcon));
        icon.setBounds(0, 0, (int) (density * 68.0f), (int) (68.0f * density));
        return icon;
    }

    private static Drawable getRawQuickNoteIcon(Context context, int userId) {
        Drawable quickNoteIcon = getAppDrawableResources(context, userId, NOTEPAD_PACKAGE_NAME, QUICK_NOTE_ICON_RES);
        if (quickNoteIcon == null) {
            return getAppDrawableResources(context, userId, NOTEPAD_OLD_PACKAGE_NAME, QUICK_NOTE_ICON_RES);
        }
        return quickNoteIcon;
    }

    private static Drawable getAppDrawableResources(Context context, int userId, String pkgName, String resName) {
        try {
            Context targetContext = context.createPackageContextAsUser(pkgName, 0, UserHandle.of(userId));
            int resId = targetContext.getResources().getIdentifier(resName, "drawable", pkgName);
            return resId > 0 ? targetContext.getDrawable(resId) : null;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "create target context failed");
            return null;
        }
    }

    public static Drawable getAppIconForFloatingBall(Context context, String pkgName, int userId, boolean isQuickNote) {
        Drawable srcDrawable;
        int bgColor;
        if (context == null) {
            return null;
        }
        if (isQuickNote) {
            srcDrawable = getRawQuickNoteIcon(context, userId);
        } else {
            srcDrawable = getRawAppIcon(context, pkgName, userId, false);
        }
        Bitmap srcBitmap = drawable2Bitmap(srcDrawable);
        if (srcBitmap == null) {
            Log.e(TAG, "get round app icon error!");
            return null;
        }
        Bitmap target = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(srcBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setShader(shader);
        float radius = ((float) Math.min(target.getWidth(), target.getHeight())) / 2.0f;
        Canvas canvas = new Canvas(target);
        Paint bgPaint = new Paint();
        if (isInNightMode(context)) {
            bgColor = Color.parseColor(FLOATING_BALL_BG_COLOR_NIGHT);
        } else {
            bgColor = Color.parseColor(FLOATING_BALL_BG_COLOR);
        }
        bgPaint.setColor(bgColor);
        canvas.drawCircle(radius, radius, radius * FLOATING_BALL_RADIUS_RATIO, bgPaint);
        canvas.drawCircle(radius, radius, FLOATING_BALL_RADIUS_RATIO * radius, paint);
        return bitmap2Drawable(target);
    }

    private static Drawable getRawAppIcon(Context context, String pkgName, int userId, boolean isUseBadgedIcon) {
        PackageManager pm;
        Drawable userBadgedIconDrawable = null;
        if (pkgName == null || pkgName.isEmpty() || (pm = context.getPackageManager()) == null) {
            return null;
        }
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfoAsUser(pkgName, 0, userId);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "icon not load!");
        }
        Drawable iconDrawable = info == null ? null : info.loadIcon(pm);
        if (iconDrawable == null) {
            Log.w(TAG, "getAppIcon failed, cause iconDrawable is null!");
            return iconDrawable;
        }
        UserHandle userHandle = UserHandleEx.getUserHandle(userId);
        if (!isUseBadgedIcon) {
            userBadgedIconDrawable = null;
        } else if (userHandle != null) {
            userBadgedIconDrawable = pm.getUserBadgedIcon(iconDrawable, userHandle);
        }
        return userBadgedIconDrawable == null ? iconDrawable : userBadgedIconDrawable;
    }

    public static int getFloatTaskState(Context context) {
        if (context == null) {
            return 0;
        }
        int state = Settings.Secure.getIntForUser(context.getContentResolver(), FLOAT_TASK_STATE_KEY, -1, -2);
        Log.i(TAG, "getSettingsSecureIntForUser: key=float_task_state, state=" + state + ", default=-1");
        return state;
    }

    public static void putFloatTaskStateToSettings(boolean isEnable, Context context) {
        if (context != null) {
            Settings.Secure.putIntForUser(context.getContentResolver(), FLOAT_TASK_STATE_KEY, isEnable ? 1 : 0, -2);
        }
    }

    public static Bitmap getWallpaperScreenShot(ActivityTaskManagerService atms) {
        if (atms == null) {
            Slog.w(TAG, "getWallpaperScreenShot failed, cause atms is null!");
            return null;
        }
        HwMultiWindowManager manager = HwMultiWindowManager.getInstance(atms);
        if (manager == null) {
            Slog.w(TAG, "getWallpaperScreenShot failed, cause manager is null!");
            return null;
        }
        DefaultHwMagicWindowManagerService hwMagicWindowService = manager.getHwMagicWindowService();
        if (hwMagicWindowService != null) {
            return hwMagicWindowService.getWallpaperScreenShot();
        }
        Slog.w(TAG, "getWallpaperScreenShot failed, cause hwMagicWS is null!");
        return null;
    }

    public static Bitmap getStatusBarScreenShot(int statusBarWidth, int statusBarHeight) {
        Rect sourceCrop = new Rect(0, 0, statusBarWidth, statusBarHeight);
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        if (display != null) {
            return SurfaceControl.screenshot_ext_hw(sourceCrop, statusBarWidth, statusBarHeight, display.getRotation());
        }
        Log.w(TAG, "getStatusBarScreenShot failed, cause display is null!");
        return null;
    }

    public static boolean isGestureNavigation(ContentResolver cr) {
        if (cr != null && Settings.Secure.getInt(cr, "secure_gesture_navigation", 0) == 1) {
            return true;
        }
        return false;
    }
}
