package huawei.com.android.server.policy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.provider.Settings.Global;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public class BlurUtils {
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final float SCALE = 0.75f;
    private static final int STATE_LEFT = 1;
    private static final int STATE_MIDDLE = 0;
    private static final int STATE_RIGHT = 2;
    private static final String TAG = "BlurUtils";

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
        Allocation tmpIn = Allocation.createFromBitmap(mRs, input, MipmapControl.MIPMAP_NONE, STATE_LEFT);
        Allocation tmpOut = Allocation.createTyped(mRs, tmpIn.getType());
        if (output.getConfig() == Config.ARGB_8888) {
            Element elementIn = tmpIn.getType().getElement();
            Element elementOut = tmpOut.getType().getElement();
            if (!(elementIn.isCompatible(Element.RGBA_8888(mRs)) && elementOut.isCompatible(Element.RGBA_8888(mRs)))) {
                Log.w(TAG, "Temp input Allocation kind is " + elementIn.getDataKind() + ", type " + elementIn.getDataType() + " of " + elementIn.getBytesSize() + " bytes." + "And Temp output Allocation kind is " + elementOut.getDataKind() + ", type " + elementOut.getDataType() + " of " + elementOut.getBytesSize() + " bytes." + " output bitmap was ARGB_8888.");
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

    public static Bitmap covertToARGB888(Bitmap img) {
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Config.ARGB_8888);
        new Canvas(result).drawBitmap(img, 0.0f, 0.0f, new Paint());
        return result;
    }

    public static Bitmap addBlackBoard(Bitmap bmp, int color) {
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Bitmap newBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);
        canvas.setBitmap(newBitmap);
        canvas.drawBitmap(bmp, 0.0f, 0.0f, paint);
        canvas.drawColor(color);
        return newBitmap;
    }

    public static Bitmap screenShotBitmap(Context ctx, int minLayer, int maxLayer, float scale, Rect rect) {
        Bitmap bitmap;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) ctx.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        int[] dims = new int[STATE_RIGHT];
        dims[STATE_MIDDLE] = (((int) (((float) displayMetrics.widthPixels) * scale)) / STATE_RIGHT) * STATE_RIGHT;
        dims[STATE_LEFT] = (((int) (((float) displayMetrics.heightPixels) * scale)) / STATE_RIGHT) * STATE_RIGHT;
        Rect sourceCrop = getScreenshotRect(ctx);
        if (isLazyMode(ctx)) {
            bitmap = SurfaceControl.screenshot(sourceCrop, dims[STATE_MIDDLE], dims[STATE_LEFT], STATE_MIDDLE, -1, false, STATE_MIDDLE);
        } else {
            bitmap = SurfaceControl.screenshot(dims[STATE_MIDDLE], dims[STATE_LEFT]);
        }
        if (bitmap == null) {
            Log.e(TAG, "screenShotBitmap error bitmap is null");
            return null;
        }
        bitmap.prepareToDraw();
        return bitmap;
    }

    private static int getLazyState(Context context) {
        String str = Global.getString(context.getContentResolver(), "single_hand_mode");
        if (str == null || AppHibernateCst.INVALID_PKG.equals(str)) {
            return STATE_MIDDLE;
        }
        if (str.contains(LEFT)) {
            return STATE_LEFT;
        }
        if (str.contains(RIGHT)) {
            return STATE_RIGHT;
        }
        return STATE_MIDDLE;
    }

    private static boolean isLazyMode(Context context) {
        return getLazyState(context) != 0;
    }

    private static Rect getScreenshotRect(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int state = getLazyState(context);
        if (STATE_LEFT == state) {
            return new Rect(STATE_MIDDLE, (int) (((float) displayMetrics.heightPixels) * 0.25f), (int) (((float) displayMetrics.widthPixels) * SCALE), displayMetrics.heightPixels);
        }
        if (STATE_RIGHT == state) {
            return new Rect((int) (((float) displayMetrics.widthPixels) * 0.25f), (int) (((float) displayMetrics.heightPixels) * 0.25f), displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return null;
    }
}
