package ohos.hiaivision.image.sr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import ohos.ai.cv.common.ImageResult;
import ohos.ai.cv.common.VisionCallback;
import ohos.ai.cv.common.VisionConfiguration;
import ohos.ai.cv.common.VisionImage;
import ohos.ai.cv.sr.IImageSuperResolution;
import ohos.ai.cv.sr.SisrConfiguration;
import ohos.ai.engine.pluginservice.ILoadPluginCallback;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;
import ohos.hiaivision.common.IHiAIVisionCallback;
import ohos.hiaivision.common.VisionBase;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.utils.adapter.PacMapUtils;

public class ImageSuperResolution extends VisionBase implements IImageSuperResolution {
    private static final int ENGINE_VER_10_1 = 2;
    private static final int MAX_LONG_EDGE_DEFAULT = 800;
    private static final int MAX_SHORT_EDGE_DEFAULT = 600;
    private static final float SCALE_1X = 1.0f;
    private static final float SCALE_3X = 3.0f;
    private static final float SCALE_TOLERANCE = 1.0E-6f;
    private static final int SISR_MAX_DETECT_TIME = 10000;
    private static final String TAG = "ImageSuperResolution";
    private int maxLongEdgeSize1X = 800;
    private int maxLongEdgeSize3X = 800;
    private int maxShortEdgeSize1X = 600;
    private int maxShortEdgeSize3X = 600;
    private SisrConfiguration visionConfiguration = new SisrConfiguration.Builder().build();

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public int getApiId() {
        return 660481;
    }

    public ImageSuperResolution(Context context) {
        super(context);
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int prepare() {
        int i;
        int prepare = super.prepare();
        if (prepare == 0 && this.ability != null) {
            if (!this.ability.containsKey("engine_version")) {
                HiAILog.warn(TAG, "No ENGINE_VERSION in input bundle, default size 800x600 will be used");
                return prepare;
            }
            if (this.ability.getInt("engine_version") >= 2) {
                this.maxLongEdgeSize1X = this.ability.getInt("max_long_edge_1x");
                this.maxShortEdgeSize1X = this.ability.getInt("max_short_edge_1x");
                this.maxLongEdgeSize3X = this.ability.getInt("max_long_edge_3x");
                this.maxShortEdgeSize3X = this.ability.getInt("max_short_edge_3x");
                if (this.maxLongEdgeSize1X <= 0 || this.maxShortEdgeSize1X <= 0 || this.maxLongEdgeSize3X <= 0 || this.maxShortEdgeSize3X <= 0) {
                    HiAILog.error(TAG, "Incomplete SISR ability for new plugin.");
                    return -1;
                }
            } else {
                this.maxLongEdgeSize1X = this.ability.getInt("max_long_edge");
                this.maxShortEdgeSize1X = this.ability.getInt("max_short_edge");
                int i2 = this.maxLongEdgeSize1X;
                if (i2 <= 0 || (i = this.maxShortEdgeSize1X) <= 0) {
                    HiAILog.error(TAG, "Incomplete SISR ability for old plugin.");
                    return -1;
                }
                this.maxLongEdgeSize3X = i2;
                this.maxShortEdgeSize3X = i;
            }
            HiAILog.info(TAG, "Got SISR ability, maxLongEdgeSize1x: " + this.maxLongEdgeSize1X + ", maxShortEdgeSize1x: " + this.maxShortEdgeSize1X + ", maxLongEdgeSize3x: " + this.maxLongEdgeSize3X + ", maxShortEdgeSize3x: " + this.maxShortEdgeSize3X);
        }
        return prepare;
    }

    public void setVisionConfiguration(SisrConfiguration sisrConfiguration) {
        if (sisrConfiguration != null) {
            this.visionConfiguration = sisrConfiguration;
            return;
        }
        HiAILog.warn(TAG, "Got null for SR configuration, default configuration will be used instead.");
        this.visionConfiguration = new SisrConfiguration.Builder().build();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public VisionConfiguration getConfiguration() {
        return this.visionConfiguration;
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int getAvailability() {
        return super.getAvailability();
    }

    @Override // ohos.hiaivision.common.VisionBase
    public void loadPlugin(ILoadPluginCallback iLoadPluginCallback) {
        super.loadPlugin(iLoadPluginCallback);
    }

    public int doSuperResolution(VisionImage visionImage, ImageResult imageResult, VisionCallback<ImageResult> visionCallback) {
        boolean z = visionCallback != null;
        if (isInputIllegal(visionImage, imageResult, visionCallback)) {
            handleErrorCode(z, visionCallback, 201);
            return 201;
        }
        int prepare = prepare();
        if (prepare != 0) {
            HiAILog.error(TAG, "Failed to prepare sisr, result: " + prepare);
            handleErrorCode(z, visionCallback, prepare);
            return prepare;
        }
        Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(visionImage.getPixelMap());
        int checkBitmap = checkBitmap(createShadowBitmap, this.visionConfiguration.getScale());
        if (checkBitmap != 210) {
            HiAILog.error(TAG, "Invalid input bitmap.");
            handleErrorCode(z, visionCallback, checkBitmap);
            return checkBitmap;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition newCondition = reentrantLock.newCondition();
        ImageResult imageResult2 = new ImageResult();
        int[] iArr = new int[1];
        IHiAIVisionCallback createVisionCallback = createVisionCallback(imageResult2, visionCallback, reentrantLock, newCondition, iArr);
        Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(this.visionConfiguration.getParam());
        convertIntoBundle.putParcelable("bitmap_input", createShadowBitmap);
        if (this.visionConfiguration.getProcessMode() == 1) {
            try {
                this.engine.run(convertIntoBundle, createVisionCallback);
            } catch (RemoteException e) {
                HiAILog.error(TAG, "out-built run error" + e.getMessage());
            }
        } else {
            try {
                this.reflect.call("run", Bundle.class, Object.class).invoke(convertIntoBundle, createVisionCallback);
            } catch (ReflectiveOperationException e2) {
                HiAILog.error(TAG, "mix-built run error" + e2.getMessage());
            }
        }
        int waitForResult = waitForResult(z, reentrantLock, newCondition);
        if (waitForResult == 0) {
            if (iArr[0] != 0) {
                return iArr[0];
            }
            imageResult.setPixelMap(imageResult2.getPixelMap());
        }
        return waitForResult;
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int release() {
        return super.release();
    }

    private int checkBitmap(Bitmap bitmap, float f) {
        if (bitmap == null) {
            HiAILog.error(TAG, "Input bitmap is null");
            return 201;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixelLimit = getPixelLimit();
        if (height * width > pixelLimit) {
            HiAILog.error(TAG, "Image is too large than " + pixelLimit);
            return 200;
        }
        if (width >= height) {
            width = height;
            height = width;
        }
        if (Math.abs(f - 1.0f) < SCALE_TOLERANCE) {
            if (height <= this.maxLongEdgeSize1X && width <= this.maxShortEdgeSize1X) {
                return 210;
            }
            HiAILog.error(TAG, "Too big image for 1x SISR, the longer edge must not be longer than " + this.maxLongEdgeSize1X + " px, and the shorter edge must not be longer than " + this.maxShortEdgeSize1X + " px.");
            return 200;
        } else if (Math.abs(f - SCALE_3X) >= SCALE_TOLERANCE) {
            HiAILog.error(TAG, "Invalid scale: " + f);
            return 200;
        } else if (height <= this.maxLongEdgeSize3X && width <= this.maxShortEdgeSize3X) {
            return 210;
        } else {
            HiAILog.error(TAG, "Too big image for 3x SISR, the longer edge must not be longer than " + this.maxLongEdgeSize3X + " px, and the shorter edge must not be longer than " + this.maxShortEdgeSize3X + " px.");
            return 200;
        }
    }
}
