package ohos.hiaivision.text;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ohos.ai.cv.common.VisionCallback;
import ohos.ai.cv.common.VisionConfiguration;
import ohos.ai.cv.common.VisionImage;
import ohos.ai.cv.text.ITextDetector;
import ohos.ai.cv.text.Text;
import ohos.ai.cv.text.TextConfiguration;
import ohos.ai.engine.pluginservice.ILoadPluginCallback;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;
import ohos.hiaivision.common.IHiAIVisionCallback;
import ohos.hiaivision.common.VisionBase;
import ohos.hiaivision.visionutil.text.TextUtility;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.utils.adapter.PacMapUtils;

public class TextDetector extends VisionBase implements ITextDetector {
    private static final int DETECT_SCREEN_SHOT_MAXH = 15210;
    private static final int DETECT_SCREEN_SHOT_MAXW = 2200;
    private static final int DETECT_SCREEN_SHOT_MAX_GENERAL_H = 2560;
    private static final int MAX_DETECT_TIME = 5000;
    private static final String TAG = "TextDetector";
    private TextConfiguration textConfiguration = new TextConfiguration.Builder().build();

    public TextDetector(Context context) {
        super(context);
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int prepare() {
        return super.prepare();
    }

    public int detect(VisionImage visionImage, Text text, VisionCallback<Text> visionCallback) {
        boolean z = visionCallback != null;
        int checkInput = checkInput(visionImage, text, visionCallback);
        if (checkInput != 0) {
            handleErrorCode(z, visionCallback, checkInput);
            return checkInput;
        }
        int prepare = prepare();
        if (prepare != 0) {
            HiAILog.error(TAG, "Can't start engine, try restart app, status " + prepare);
            handleErrorCode(z, visionCallback, prepare);
            return prepare;
        }
        Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(this.textConfiguration.getParam());
        PixelMap pixelMap = visionImage.getPixelMap();
        setWidthAndHeight(pixelMap, convertIntoBundle);
        Bitmap orElse = this.textConfiguration.getProcessMode() == 1 ? getTargetBitmap(pixelMap).orElse(null) : ImageDoubleFwConverter.createShadowBitmap(pixelMap);
        convertIntoBundle.putParcelable("bitmap_input", orElse);
        if (orElse == null) {
            HiAILog.error(TAG, "bitmap from input VisionImage is null");
            handleErrorCode(z, visionCallback, 200);
            return 200;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition newCondition = reentrantLock.newCondition();
        int[] iArr = new int[1];
        Text text2 = new Text();
        getAsyncResult(convertIntoBundle, this.textConfiguration.getProcessMode(), getVisionCallback(text2, visionCallback, reentrantLock, newCondition, iArr));
        int waitForResult = waitForResult(z, reentrantLock, newCondition);
        if (waitForResult == 0) {
            if (iArr[0] != 0) {
                return iArr[0];
            }
            text.setText(text2);
        }
        return waitForResult;
    }

    public void setVisionConfiguration(TextConfiguration textConfiguration2) {
        if (textConfiguration2 == null) {
            HiAILog.error(TAG, "setTextConfiguration textConfiguration == null");
        } else {
            this.textConfiguration = textConfiguration2;
        }
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int getAvailability() {
        return super.getAvailability();
    }

    @Override // ohos.hiaivision.common.VisionBase
    public void loadPlugin(ILoadPluginCallback iLoadPluginCallback) {
        super.loadPlugin(iLoadPluginCallback);
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int release() {
        return super.release();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public int getApiId() {
        switch (this.textConfiguration.getDetectType()) {
            case 196609:
                return 659457;
            case 196610:
            case 196611:
            case 196612:
            default:
                return 659468;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public VisionConfiguration getConfiguration() {
        return this.textConfiguration;
    }

    private int checkInput(VisionImage visionImage, Text text, VisionCallback<Text> visionCallback) {
        if (isInputIllegal(visionImage, text, visionCallback)) {
            return 201;
        }
        return !checkImage(visionImage) ? 200 : 0;
    }

    private boolean checkImage(VisionImage visionImage) {
        PixelMap pixelMap;
        if (visionImage == null || this.textConfiguration == null || (pixelMap = visionImage.getPixelMap()) == null) {
            return false;
        }
        if (this.textConfiguration.getDetectType() != 196609) {
            return true;
        }
        Size size = pixelMap.getImageInfo().size;
        int i = size.height;
        if (size.width > 2200 || i > DETECT_SCREEN_SHOT_MAXH) {
            return false;
        }
        return true;
    }

    private IHiAIVisionCallback getVisionCallback(final Text text, final VisionCallback<Text> visionCallback, final Lock lock, final Condition condition, final int[] iArr) {
        final boolean z = visionCallback != null;
        return new IHiAIVisionCallback.Stub() {
            /* class ohos.hiaivision.text.TextDetector.AnonymousClass1 */

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onInfo(Bundle bundle) throws RemoteException {
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onResult(Bundle bundle) throws RemoteException {
                HiAILog.debug(TextDetector.TAG, "onResult");
                if (bundle != null) {
                    text.setText(TextUtility.textFromBundle(bundle));
                    iArr[0] = 0;
                } else {
                    HiAILog.error(TextDetector.TAG, "bundle is null!");
                    iArr[0] = 101;
                }
                if (z) {
                    visionCallback.onResult(text);
                } else {
                    TextDetector.this.signalLockForSyncMode(lock, condition);
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onError(int i) throws RemoteException {
                HiAILog.debug(TextDetector.TAG, "onError");
                if (z) {
                    visionCallback.onError(i);
                    return;
                }
                iArr[0] = i;
                TextDetector.this.signalLockForSyncMode(lock, condition);
            }
        };
    }

    private void setWidthAndHeight(PixelMap pixelMap, Bundle bundle) {
        if (pixelMap != null) {
            Size size = pixelMap.getImageInfo().size;
            bundle.putInt("origin_width", size.width);
            bundle.putInt("origin_height", size.height);
            return;
        }
        bundle.putInt("origin_width", 0);
        bundle.putInt("origin_height", 0);
    }
}
