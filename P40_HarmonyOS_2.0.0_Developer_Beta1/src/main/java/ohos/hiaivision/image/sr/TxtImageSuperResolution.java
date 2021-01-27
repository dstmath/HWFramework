package ohos.hiaivision.image.sr;

import android.os.Bundle;
import android.os.RemoteException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import ohos.ai.cv.common.ImageResult;
import ohos.ai.cv.common.VisionCallback;
import ohos.ai.cv.common.VisionConfiguration;
import ohos.ai.cv.common.VisionImage;
import ohos.ai.cv.sr.ITxtImageSuperResolution;
import ohos.ai.cv.sr.TxtImageSuperResolutionConfiguration;
import ohos.ai.engine.pluginservice.ILoadPluginCallback;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;
import ohos.hiaivision.common.IHiAIVisionCallback;
import ohos.hiaivision.common.VisionBase;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.miscservices.download.DownloadSession;
import ohos.utils.adapter.PacMapUtils;

public class TxtImageSuperResolution extends VisionBase implements ITxtImageSuperResolution {
    private static final int MAX_SIZE = 1340000;
    private static final int MIN_HEIGHT = 506;
    private static final int MIN_WIDTH = 506;
    private static final String TAG = "TxtImageSuperResolution";
    private TxtImageSuperResolutionConfiguration visionConfiguration = new TxtImageSuperResolutionConfiguration.Builder().build();

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public int getApiId() {
        return 660491;
    }

    public TxtImageSuperResolution(Context context) {
        super(context);
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int prepare() {
        return super.prepare();
    }

    public int doSuperResolution(VisionImage visionImage, ImageResult imageResult, VisionCallback<ImageResult> visionCallback) {
        HiAILog.info(TAG, "text doSuperResolution using plugin interface");
        boolean z = visionCallback != null;
        if (isInputIllegal(visionImage, imageResult, visionCallback)) {
            handleErrorCode(z, visionCallback, 201);
            return 201;
        }
        int prepare = prepare();
        if (prepare != 0) {
            handleErrorCode(z, visionCallback, prepare);
            return prepare;
        }
        PixelMap pixelMap = visionImage.getPixelMap();
        if (checkImage(pixelMap) == 210) {
            return doSuperResolutionNewService(pixelMap, imageResult, visionCallback);
        }
        handleErrorCode(z, visionCallback, 200);
        return 200;
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int getAvailability() {
        return super.getAvailability();
    }

    public void setVisionConfiguration(TxtImageSuperResolutionConfiguration txtImageSuperResolutionConfiguration) {
        this.visionConfiguration = txtImageSuperResolutionConfiguration;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public VisionConfiguration getConfiguration() {
        return this.visionConfiguration;
    }

    @Override // ohos.hiaivision.common.VisionBase
    public void loadPlugin(ILoadPluginCallback iLoadPluginCallback) {
        super.loadPlugin(iLoadPluginCallback);
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int release() {
        return super.release();
    }

    private int checkImage(PixelMap pixelMap) {
        int i;
        int i2;
        Bundle bundle = this.ability;
        int i3 = DownloadSession.ERROR_CANNOT_RESUME;
        if (bundle != null) {
            i3 = this.ability.getInt("min_width");
            i2 = this.ability.getInt("min_height");
            i = this.ability.getInt("max_pixel_size");
        } else {
            i = MAX_SIZE;
            i2 = 506;
        }
        Size size = pixelMap.getImageInfo().size;
        int i4 = size.height;
        int i5 = size.width;
        if (i4 * i5 > i) {
            HiAILog.error(TAG, "Image is larger than " + i);
            return 200;
        } else if (i4 >= i2 && i5 >= i3) {
            return 210;
        } else {
            HiAILog.error(TAG, "Image is smaller than (width * height):(" + i3 + " * " + i2 + ")");
            return 200;
        }
    }

    private int doSuperResolutionNewService(PixelMap pixelMap, ImageResult imageResult, VisionCallback<ImageResult> visionCallback) {
        boolean z = visionCallback != null;
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition newCondition = reentrantLock.newCondition();
        ImageResult imageResult2 = new ImageResult();
        int[] iArr = new int[1];
        IHiAIVisionCallback createVisionCallback = createVisionCallback(imageResult2, visionCallback, reentrantLock, newCondition, iArr);
        Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(this.visionConfiguration.getParam());
        if (this.visionConfiguration.getProcessMode() == 1) {
            HiAILog.debug(TAG, "out mode super-resolution");
            convertIntoBundle.putParcelable("bitmap_input", ImageDoubleFwConverter.createShadowBitmap(pixelMap));
            try {
                this.engine.run(convertIntoBundle, createVisionCallback);
            } catch (RemoteException e) {
                HiAILog.error(TAG, "out-built run error" + e.getMessage());
            }
        } else {
            HiAILog.debug(TAG, "in mode super-resolution");
            convertIntoBundle.putParcelable("bitmap_input", ImageDoubleFwConverter.createShadowBitmap(pixelMap));
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
}
