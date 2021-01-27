package com.huawei.internal.widget;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.HardwareBuffer;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.ImageView;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.internal.widget.HwCaptionViewUtils;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class HwCaptionViewUtils {
    private static final String BLUR_THREAD_NAME = "HwCaptionViewUtils - BlurThread";
    private static final int CORE_THREADS_NUM = 1;
    private static final float HALF_ADJUST = 0.5f;
    private static final long KEEP_ALIVE_DURATION = 60;
    private static final int MAX_THREADS_NUM = 5;
    private static final int SAMPLE_SIZE = 12;
    private static final String TAG = "HwCaptionViewUtils";
    private static ThreadPoolExecutor sPoolExecutor;

    public interface BlurListener {
        void onBlurDone();
    }

    private HwCaptionViewUtils() {
    }

    public static boolean isInSubFoldDisplayMode(Context context) {
        if (context == null) {
            Log.w(TAG, "isInSubFoldDisplayMode check failed!");
            return false;
        } else if (Settings.Global.getInt(context.getContentResolver(), ConstantValues.HW_FOLD_DISPLAY_MODE_STR_PREPARE, 0) == 3) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInLazyMode(Context context) {
        if (context == null) {
            Log.w(TAG, "isInLazyMode check failed!");
            return false;
        }
        String lazyModeStr = Settings.Global.getString(context.getContentResolver(), "single_hand_mode");
        if (ConstantValues.LEFT_HAND_LAZY_MODE_STR.equals(lazyModeStr) || ConstantValues.RIGHT_HAND_LAZY_MODE_STR.equals(lazyModeStr)) {
            return true;
        }
        return false;
    }

    public static int dipToPx(Context context, float dpValue) {
        if (context != null) {
            return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
        }
        Log.e(TAG, "dipToPx context is null.");
        return (int) dpValue;
    }

    public static Bitmap getTaskSnapshot(Activity activity) {
        ActivityManager.TaskSnapshot snapshot = HwActivityTaskManager.getActivityTaskSnapshot(activity.getActivityToken(), true);
        if (snapshot != null) {
            return Bitmap.wrapHardwareBuffer(HardwareBuffer.createFromGraphicBuffer(snapshot.getSnapshot()), snapshot.getColorSpace());
        }
        return null;
    }

    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
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

    public static void startToBlur(Bitmap inBitmap, ImageView dstImageView, BlurListener listener, int radius) {
        if (inBitmap == null || dstImageView == null) {
            Log.w(TAG, "startToBlur failed, cause inputBitmap or dstImageView is null");
            return;
        }
        if (sPoolExecutor == null) {
            sPoolExecutor = new ThreadPoolExecutor(1, 5, KEEP_ALIVE_DURATION, TimeUnit.SECONDS, new LinkedBlockingQueue(), $$Lambda$HwCaptionViewUtils$DiV_wcIMUjdiEA07bC5MdTuBpo.INSTANCE, new ThreadPoolExecutor.DiscardOldestPolicy());
        }
        sPoolExecutor.execute(new Runnable(dstImageView, inBitmap.copy(Bitmap.Config.ARGB_8888, true), radius, dstImageView.getScaleType(), listener) {
            /* class com.huawei.internal.widget.$$Lambda$HwCaptionViewUtils$HQvUn3kjbBB32ax63DDAmZVVEE */
            private final /* synthetic */ ImageView f$0;
            private final /* synthetic */ Bitmap f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ ImageView.ScaleType f$3;
            private final /* synthetic */ HwCaptionViewUtils.BlurListener f$4;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwCaptionViewUtils.lambda$startToBlur$2(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    static /* synthetic */ Thread lambda$startToBlur$0(Runnable runnable) {
        return new Thread(runnable, BLUR_THREAD_NAME);
    }

    static /* synthetic */ void lambda$startToBlur$2(ImageView dstImageView, Bitmap softBitmap, int radius, ImageView.ScaleType scaleType, BlurListener listener) {
        Bitmap outBitmap = rsBlur(dstImageView.getContext(), softBitmap, radius);
        if (scaleType != ImageView.ScaleType.FIT_XY) {
            outBitmap = Bitmap.createScaledBitmap(outBitmap, softBitmap.getWidth(), softBitmap.getHeight(), true);
        }
        dstImageView.post(new Runnable(dstImageView, scaleType, outBitmap, listener) {
            /* class com.huawei.internal.widget.$$Lambda$HwCaptionViewUtils$qfeAnxBC5h2oS65bCCw5NG4h3ho */
            private final /* synthetic */ ImageView f$0;
            private final /* synthetic */ ImageView.ScaleType f$1;
            private final /* synthetic */ Bitmap f$2;
            private final /* synthetic */ HwCaptionViewUtils.BlurListener f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwCaptionViewUtils.lambda$startToBlur$1(this.f$0, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    static /* synthetic */ void lambda$startToBlur$1(ImageView dstImageView, ImageView.ScaleType scaleType, Bitmap dstBitmap, BlurListener listener) {
        dstImageView.setScaleType(scaleType);
        dstImageView.setImageDrawable(bitmap2Drawable(dstBitmap));
        if (listener != null) {
            listener.onBlurDone();
        }
    }
}
