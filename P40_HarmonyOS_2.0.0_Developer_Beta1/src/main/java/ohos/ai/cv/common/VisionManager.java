package ohos.ai.cv.common;

import ohos.ai.cv.docrefine.IDocRefine;
import ohos.ai.cv.qrcode.IBarcodeDetector;
import ohos.ai.cv.sr.IImageSuperResolution;
import ohos.ai.cv.sr.ITxtImageSuperResolution;
import ohos.ai.cv.text.ITextDetector;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;
import ohos.hiaivision.AiRuntimeException;
import ohos.hiaivision.common.VisionBase;
import ohos.hiaivision.image.docrefine.DocRefine;
import ohos.hiaivision.image.sr.ImageSuperResolution;
import ohos.hiaivision.image.sr.TxtImageSuperResolution;
import ohos.hiaivision.qrcode.BarcodeDetector;
import ohos.hiaivision.text.TextDetector;

public class VisionManager {
    private static final String TAG = "VisionManager";

    private VisionManager() {
    }

    public static int init(Context context, ConnectionCallback connectionCallback) {
        try {
            return VisionBase.init(context, connectionCallback);
        } catch (AiRuntimeException e) {
            HiAILog.error(TAG, "init failed: " + e.getMessage());
            return -1;
        }
    }

    public static void destroy() {
        VisionBase.destroy();
    }

    public static IImageSuperResolution getSisr(Context context) {
        return new ImageSuperResolution(context);
    }

    public static ITxtImageSuperResolution getTisr(Context context) {
        return new TxtImageSuperResolution(context);
    }

    public static IDocRefine getDocRefine(Context context) {
        return new DocRefine(context);
    }

    public static ITextDetector getTextDetector(Context context) {
        return new TextDetector(context);
    }

    public static IBarcodeDetector getBarcodeDetector(Context context) {
        return new BarcodeDetector(context);
    }
}
