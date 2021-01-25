package com.android.internal.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.SystemProperties;
import com.android.internal.R;

public class UserIcons {
    private static final float COLOR_START_DEGREE = 0.0f;
    private static final float HALF_CIRCLE_DEGREE = 180.0f;
    private static final boolean IS_HONOR = HwThemeManager.HONOR_TAG.equalsIgnoreCase(SystemProperties.get("ro.product.brand", ""));
    private static final float RADIUS_PARAMETER = 2.0f;
    private static final float[] SWEEP_CICLE_POSITIONS = {0.0f, 0.5f, 0.8f, 1.0f};
    private static final int[] USER_ICON_COLORS = {R.color.user_icon_1, R.color.user_icon_2, R.color.user_icon_3, R.color.user_icon_4, R.color.user_icon_5, R.color.user_icon_6};

    public static Bitmap convertToBitmap(Drawable icon) {
        if (icon == null) {
            return null;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        return bitmap;
    }

    public static Drawable getDefaultUserIcon(Resources resources, int userId, boolean light) {
        if (IS_HONOR) {
            return createRoundPhotoForHonor(resources, userId);
        }
        return createRoundPhoto(resources, userId, light);
    }

    @SuppressLint({"AvoidMax/Min"})
    private static Drawable createRoundPhoto(Resources resources, int userId, boolean isLight) {
        int backgroundColorId = isLight ? R.color.user_icon_default_white : R.color.user_icon_default_gray;
        int headColorId = com.android.hwext.internal.R.color.user_icon_head_default;
        if (userId != -10000) {
            int[] iArr = USER_ICON_COLORS;
            backgroundColorId = iArr[userId % iArr.length];
            headColorId = com.android.hwext.internal.R.color.user_icon_head;
        }
        Drawable icon = resources.getDrawable(R.drawable.ic_account_circle, null).mutate();
        icon.setColorFilter(resources.getColor(headColorId, null), PorterDuff.Mode.SRC_IN);
        Bitmap bitmap = convertToBitmap(icon);
        if (bitmap == null) {
            return null;
        }
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(0, 0, size, size);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(resources.getColor(backgroundColorId, null));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(((float) size) / RADIUS_PARAMETER, ((float) size) / RADIUS_PARAMETER, ((float) size) / RADIUS_PARAMETER, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, (Paint) null);
        bitmap.recycle();
        return new BitmapDrawable(resources, output);
    }

    private static Drawable createRoundPhotoForHonor(Resources resources, int userId) {
        Bitmap srcBitmap = convertToBitmap(resources.getDrawable(33751223, null).mutate());
        if (srcBitmap == null) {
            return null;
        }
        TypedArray sweepCircleColors = resources.obtainTypedArray(com.android.hwext.internal.R.array.sweep_circle_colors);
        int defaultCircleColor = resources.getColor(com.android.hwext.internal.R.color.sweep_circle_default_color, null);
        int[] sweepCircleColorValues = new int[sweepCircleColors.length()];
        int size = sweepCircleColors.length();
        for (int i = 0; i < size; i++) {
            if (userId != -10000) {
                sweepCircleColorValues[i] = sweepCircleColors.getColor(i, defaultCircleColor);
            } else {
                sweepCircleColorValues[i] = defaultCircleColor;
            }
        }
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        float sweepStrokeWidth = (float) resources.getDimensionPixelSize(com.android.hwext.internal.R.dimen.sweep_stroke_width_for_list);
        paint.setStrokeWidth(sweepStrokeWidth);
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        SweepGradient sweepGradient = new SweepGradient(((float) width) / RADIUS_PARAMETER, ((float) height) / RADIUS_PARAMETER, sweepCircleColorValues, SWEEP_CICLE_POSITIONS);
        Matrix matrix = new Matrix();
        matrix.setRotate(0.0f, ((float) width) / RADIUS_PARAMETER, ((float) height) / RADIUS_PARAMETER);
        sweepGradient.setLocalMatrix(matrix);
        paint.setShader(sweepGradient);
        canvas.drawArc(new RectF(sweepStrokeWidth / RADIUS_PARAMETER, sweepStrokeWidth / RADIUS_PARAMETER, ((float) width) - (sweepStrokeWidth / RADIUS_PARAMETER), ((float) height) - (sweepStrokeWidth / RADIUS_PARAMETER)), 0.0f, 360.0f, false, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        Rect srcRect = new Rect();
        srcRect.set(0, 0, width, height);
        Rect destRect = new Rect();
        destRect.set((int) sweepStrokeWidth, (int) sweepStrokeWidth, (int) (((float) width) - sweepStrokeWidth), (int) (((float) height) - sweepStrokeWidth));
        canvas.drawBitmap(srcBitmap, srcRect, destRect, paint);
        srcBitmap.recycle();
        sweepCircleColors.recycle();
        return new BitmapDrawable(resources, bitmap);
    }
}
