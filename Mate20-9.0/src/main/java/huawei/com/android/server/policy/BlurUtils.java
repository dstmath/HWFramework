package huawei.com.android.server.policy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.server.gesture.GestureNavConst;

public class BlurUtils {
    private static final String LEFT = "left";
    private static int NAVIGATIONBAR_LAYER = 231000;
    private static final String RIGHT = "right";
    private static final float SCALE = 0.75f;
    private static final int STATE_LEFT = 1;
    private static final int STATE_MIDDLE = 0;
    private static final int STATE_RIGHT = 2;
    private static final String TAG = "BlurUtils";
    private static RenderScript mRenderScript;
    private static ScriptIntrinsicBlur mScriptIntrinsicBlur;

    public static Bitmap blurImage(Context ctx, Bitmap input, Bitmap output, int radius) {
        if (ctx == null || input == null || output == null || radius <= 0 || radius > 25) {
            Log.w(TAG, "blurImage() parameter is incorrect:" + ctx + "," + input + "," + output + "," + radius);
            return null;
        }
        Context c = ctx.getApplicationContext();
        if (c != null) {
            ctx = c;
        }
        RenderScript mRs = RenderScript.create(ctx);
        if (mRs == null) {
            Log.w(TAG, "blurImage() mRs consturct error");
            return null;
        }
        Allocation tmpIn = Allocation.createFromBitmap(mRs, input, Allocation.MipmapControl.MIPMAP_NONE, 1);
        Allocation tmpOut = Allocation.createTyped(mRs, tmpIn.getType());
        if (output.getConfig() == Bitmap.Config.ARGB_8888) {
            Element elementIn = tmpIn.getType().getElement();
            Element elementOut = tmpOut.getType().getElement();
            if (!elementIn.isCompatible(Element.RGBA_8888(mRs)) || !elementOut.isCompatible(Element.RGBA_8888(mRs))) {
                Log.w(TAG, "Temp input Allocation kind is " + elementIn.getDataKind() + ", type " + elementIn.getDataType() + " of " + elementIn.getBytesSize() + " bytes.And Temp output Allocation kind is " + elementOut.getDataKind() + ", type " + elementOut.getDataType() + " of " + elementOut.getBytesSize() + " bytes. output bitmap was ARGB_8888.");
                return null;
            }
        }
        ScriptIntrinsicBlur mScriptIntrinsic = ScriptIntrinsicBlur.create(mRs, Element.U8_4(mRs));
        mScriptIntrinsic.setRadius((float) radius);
        mScriptIntrinsic.setInput(tmpIn);
        mScriptIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(output);
        try {
            tmpIn.destroy();
        } catch (RSInvalidStateException e) {
            e.printStackTrace();
        }
        try {
            tmpOut.destroy();
        } catch (RSInvalidStateException e2) {
            e2.printStackTrace();
        }
        mRs.destroy();
        return output;
    }

    public static Bitmap blurMaskImage(Context ctx, Bitmap input, Bitmap output, int radius) {
        if (ctx == null || input == null || output == null || radius <= 0 || radius > 25) {
            Log.w(TAG, "blurImage() parameter is incorrect:" + ctx + "," + input + "," + output + "," + radius);
            return null;
        }
        Context c = ctx.getApplicationContext();
        if (c != null) {
            ctx = c;
        }
        if (mRenderScript == null) {
            mRenderScript = RenderScript.create(ctx);
        }
        if (mRenderScript == null) {
            Log.w(TAG, "blurImage() mRs consturct error");
            return null;
        }
        Allocation tmpIn = Allocation.createFromBitmap(mRenderScript, input, Allocation.MipmapControl.MIPMAP_NONE, 1);
        Allocation tmpOut = Allocation.createTyped(mRenderScript, tmpIn.getType());
        if (output.getConfig() == Bitmap.Config.ARGB_8888) {
            Element elementIn = tmpIn.getType().getElement();
            Element elementOut = tmpOut.getType().getElement();
            if (!elementIn.isCompatible(Element.RGBA_8888(mRenderScript)) || !elementOut.isCompatible(Element.RGBA_8888(mRenderScript))) {
                Log.w(TAG, "Temp input Allocation kind is " + elementIn.getDataKind() + ", type " + elementIn.getDataType() + " of " + elementIn.getBytesSize() + " bytes.And Temp output Allocation kind is " + elementOut.getDataKind() + ", type " + elementOut.getDataType() + " of " + elementOut.getBytesSize() + " bytes. output bitmap was ARGB_8888.");
                return null;
            }
        }
        if (mScriptIntrinsicBlur == null) {
            mScriptIntrinsicBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        }
        mScriptIntrinsicBlur.setRadius((float) radius);
        mScriptIntrinsicBlur.setInput(tmpIn);
        mScriptIntrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(output);
        try {
            tmpIn.destroy();
        } catch (RSInvalidStateException e) {
            e.printStackTrace();
        }
        try {
            tmpOut.destroy();
        } catch (RSInvalidStateException e2) {
            e2.printStackTrace();
        }
        return output;
    }

    public static Bitmap covertToARGB888(Bitmap img) {
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(result).drawBitmap(img, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, new Paint());
        return result;
    }

    public static Bitmap addBlackBoard(Bitmap bmp, int color) {
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Bitmap newBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(newBitmap);
        canvas.drawBitmap(bmp, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, paint);
        canvas.drawColor(color);
        return newBitmap;
    }

    public static Bitmap screenShotBitmap(Context ctx, int minLayer, int maxLayer, float scale, Rect rect) {
        Bitmap bitmap;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Context context = ctx;
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        int[] dims = {(((int) (((float) displayMetrics.widthPixels) * scale)) / 2) * 2, (((int) (((float) displayMetrics.heightPixels) * scale)) / 2) * 2};
        Rect sourceCrop = getScreenshotRect(context);
        if (isLazyMode(context)) {
            bitmap = SurfaceControl.screenshot(sourceCrop, dims[0], dims[1], Integer.MIN_VALUE, Integer.MAX_VALUE, false, 0);
        } else {
            bitmap = SurfaceControl.screenshot(new Rect(), dims[0], dims[1], 0);
        }
        if (bitmap == null) {
            Log.e(TAG, "screenShotBitmap error bitmap is null");
            return null;
        }
        bitmap.prepareToDraw();
        return bitmap;
    }

    public static Bitmap screenShotBitmap(Context ctx, float scale) {
        Bitmap bitmap;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        DisplayMetrics displayMetricsBody = new DisplayMetrics();
        Display display = ((WindowManager) ctx.getSystemService("window")).getDefaultDisplay();
        display.getMetrics(displayMetricsBody);
        display.getRealMetrics(displayMetrics);
        int[] dims = {(((int) (((float) displayMetrics.widthPixels) * scale)) / 2) * 2, (((int) (((float) displayMetrics.heightPixels) * scale)) / 2) * 2};
        int[] dims2 = {(((int) (((float) displayMetricsBody.widthPixels) * scale)) / 2) * 2, (((int) (((float) displayMetricsBody.heightPixels) * scale)) / 2) * 2};
        Log.e(TAG, "mFingerViewParams,dims[0] =" + dims[0] + ",dims[1] =" + dims[1]);
        Log.e(TAG, "mFingerViewParams,dims2[0] =" + dims2[0] + ",dims2[1] =" + dims2[1]);
        int rotation = display.getRotation();
        if (rotation == 0 || 2 == rotation) {
            bitmap = SurfaceControl.screenshot_ext_hw(new Rect(), dims[0], dims[1], Integer.MIN_VALUE, Integer.MAX_VALUE, false, converseRotation(rotation));
        } else {
            bitmap = rotationScreenBitmap(rotation, dims, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, dims2[0], dims2[1]);
        if (bitmap2 == null) {
            Log.e(TAG, "screenShotBitmap error bitmap is null");
            return null;
        }
        bitmap2.prepareToDraw();
        return bitmap2;
    }

    private static Bitmap rotationScreenBitmap(int rotation, int[] srcDims, int minLayer, int maxLayer) {
        float degrees = convertRotationToDegrees(rotation);
        float[] dims = {(float) srcDims[0], (float) srcDims[1]};
        Matrix metrics = new Matrix();
        metrics.reset();
        metrics.preRotate(-degrees);
        metrics.mapPoints(dims);
        dims[0] = Math.abs(dims[0]);
        dims[1] = Math.abs(dims[1]);
        Bitmap bitmap = SurfaceControl.screenshot(new Rect(), (int) dims[0], (int) dims[1], minLayer, maxLayer, false, 0);
        Bitmap ss = Bitmap.createBitmap(srcDims[0], srcDims[1], Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(ss);
        c.translate(((float) srcDims[0]) * 0.5f, ((float) srcDims[1]) * 0.5f);
        c.rotate(degrees);
        c.translate((-dims[0]) * 0.5f, (-dims[1]) * 0.5f);
        c.drawBitmap(bitmap, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, null);
        bitmap.recycle();
        return ss;
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
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int state = getLazyState(context);
        if (1 == state) {
            return new Rect(0, (int) (((float) displayMetrics.heightPixels) * 0.25f), (int) (((float) displayMetrics.widthPixels) * 0.75f), displayMetrics.heightPixels);
        }
        if (2 == state) {
            return new Rect((int) (((float) displayMetrics.widthPixels) * 0.25f), (int) (((float) displayMetrics.heightPixels) * 0.25f), displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return null;
    }
}
