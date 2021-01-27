package ohos.hiaivision.image.docrefine;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ohos.ai.cv.common.ImageResult;
import ohos.ai.cv.common.VisionCallback;
import ohos.ai.cv.common.VisionConfiguration;
import ohos.ai.cv.common.VisionImage;
import ohos.ai.cv.docrefine.DocCoordinates;
import ohos.ai.cv.docrefine.DocRefineConfiguration;
import ohos.ai.cv.docrefine.IDocRefine;
import ohos.ai.engine.pluginservice.ILoadPluginCallback;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;
import ohos.hiaivision.common.IHiAIVisionCallback;
import ohos.hiaivision.common.VisionBase;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.utils.adapter.PacMapUtils;
import ohos.utils.fastjson.JSON;

public class DocRefine extends VisionBase implements IDocRefine {
    private static final int DETECT_MAX_LENGTH = 10000;
    private static final int DETECT_MIN_LENGTH = 100;
    private static final float SCALE_TOLERANCE = 1.0E-6f;
    private static final String TAG = DocRefine.class.getSimpleName();
    private VisionConfiguration visionConfiguration = new DocRefineConfiguration.Builder().build();

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public int getApiId() {
        return 659567;
    }

    public DocRefine(Context context) {
        super(context);
    }

    public int docRefine(VisionImage visionImage, DocCoordinates docCoordinates, ImageResult imageResult, VisionCallback<ImageResult> visionCallback) {
        HiAILog.debug(TAG, "refine doc in plugin");
        boolean z = visionCallback != null;
        if (isInputIllegal(visionImage, imageResult, visionCallback) || docCoordinates == null) {
            HiAILog.error(TAG, "input parameter is null when conducting docRefine");
            handleErrorCode(z, visionCallback, 201);
            return 201;
        }
        PixelMap pixelMap = visionImage.getPixelMap();
        if (!checkImage(pixelMap)) {
            handleErrorCode(z, visionCallback, 200);
            return 200;
        }
        int prepare = prepare();
        if (prepare != 0) {
            String str = TAG;
            HiAILog.error(str, "Failed to prepare docRefine, result: " + prepare);
            handleErrorCode(z, visionCallback, prepare);
            return prepare;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition newCondition = reentrantLock.newCondition();
        ImageResult imageResult2 = new ImageResult();
        int[] iArr = new int[1];
        docRefineImageProcess(pixelMap, getDocRefineCallback(imageResult2, visionCallback, reentrantLock, newCondition, iArr), docCoordinates);
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
    public int prepare() {
        return super.prepare();
    }

    public int docDetect(VisionImage visionImage, DocCoordinates docCoordinates, VisionCallback<DocCoordinates> visionCallback) {
        HiAILog.debug(TAG, "detect doc in plugin");
        if (isInputIllegal(visionImage, docCoordinates, visionCallback)) {
            HiAILog.error(TAG, "input parameter is null when conducting docDect");
            return 201;
        }
        PixelMap pixelMap = visionImage.getPixelMap();
        if (!checkImage(pixelMap)) {
            return 200;
        }
        int prepare = prepare();
        if (prepare != 0) {
            String str = TAG;
            HiAILog.error(str, "Can't start engine, try restart app, status " + prepare);
            return prepare;
        }
        boolean z = visionCallback != null;
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition newCondition = reentrantLock.newCondition();
        DocCoordinates docCoordinates2 = new DocCoordinates();
        int[] iArr = new int[1];
        docDetectImageProcess(pixelMap, getDocDetectCallback(docCoordinates2, visionCallback, reentrantLock, newCondition, iArr));
        int waitForResult = waitForResult(z, reentrantLock, newCondition);
        if (waitForResult == 0) {
            if (iArr[0] != 0) {
                return iArr[0];
            }
            docCoordinates.setDocCoordinates(resizeResult(docCoordinates2));
        }
        return waitForResult;
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int getAvailability() {
        return super.getAvailability();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public VisionConfiguration getConfiguration() {
        return this.visionConfiguration;
    }

    public void setVisionConfiguration(DocRefineConfiguration docRefineConfiguration) {
        this.visionConfiguration = docRefineConfiguration;
    }

    @Override // ohos.hiaivision.common.VisionBase
    public void loadPlugin(ILoadPluginCallback iLoadPluginCallback) {
        super.loadPlugin(iLoadPluginCallback);
    }

    @Override // ohos.hiaivision.common.VisionBase
    public int release() {
        return super.release();
    }

    private void docDetectImageProcess(PixelMap pixelMap, IHiAIVisionCallback iHiAIVisionCallback) {
        Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(this.visionConfiguration.getParam());
        convertIntoBundle.putInt("key_docrefine_func", 0);
        if (this.visionConfiguration.getProcessMode() == 1) {
            HiAILog.debug(TAG, "out mode doc detect");
            Bitmap orElse = getTargetBitmap(pixelMap).orElse(null);
            if (orElse == null) {
                HiAILog.error(TAG, "bitmap from input image is null in docDetect process");
                return;
            }
            convertIntoBundle.putParcelable("bitmap_input", orElse);
            String str = TAG;
            HiAILog.debug(str, "target bitmap in mode out detect process is " + orElse.getWidth() + " * " + orElse.getHeight());
            try {
                this.engine.run(convertIntoBundle, iHiAIVisionCallback);
            } catch (RemoteException e) {
                String str2 = TAG;
                HiAILog.error(str2, "out-built run error" + e.getMessage());
            }
        } else {
            HiAILog.debug(TAG, "in mode detect");
            Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
            if (createShadowBitmap == null) {
                HiAILog.error(TAG, "bitmap converted from image.getPixelMap is null");
                return;
            }
            convertIntoBundle.putParcelable("bitmap_input", createShadowBitmap);
            String str3 = TAG;
            HiAILog.debug(str3, "target bitmap in mode in detect process is " + createShadowBitmap.getWidth() + " * " + createShadowBitmap.getHeight());
            try {
                this.reflect.call("run", Bundle.class, Object.class).invoke(convertIntoBundle, iHiAIVisionCallback);
            } catch (ReflectiveOperationException e2) {
                String str4 = TAG;
                HiAILog.error(str4, "mix-built run error" + e2.getMessage());
            }
        }
    }

    private void docRefineImageProcess(PixelMap pixelMap, IHiAIVisionCallback iHiAIVisionCallback, DocCoordinates docCoordinates) {
        Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(this.visionConfiguration.getParam());
        convertIntoBundle.putInt("key_docrefine_func", 1);
        if (this.visionConfiguration.getProcessMode() == 1) {
            HiAILog.debug(TAG, "out mode doc refine");
            docCoordinates.scaleDocCoordinates(getScaleX(), getScaleY());
            convertIntoBundle.putString("docrefine_in_coord", JSON.toJSONString(docCoordinates));
            Bitmap orElse = getTargetBitmap(pixelMap).orElse(null);
            if (orElse == null) {
                HiAILog.error(TAG, "bitmap from input image is null in docRefine process");
                return;
            }
            convertIntoBundle.putParcelable("bitmap_input", orElse);
            String str = TAG;
            HiAILog.debug(str, "target bitmap in mode out refine process is " + orElse.getWidth() + " * " + orElse.getHeight());
            try {
                this.engine.run(convertIntoBundle, iHiAIVisionCallback);
            } catch (RemoteException e) {
                String str2 = TAG;
                HiAILog.error(str2, "out-built run error" + e.getMessage());
            }
        } else {
            HiAILog.debug(TAG, "in mode doc refine");
            convertIntoBundle.putString("docrefine_in_coord", JSON.toJSONString(docCoordinates));
            Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
            if (createShadowBitmap == null) {
                HiAILog.error(TAG, "bitmap from input image is null");
                return;
            }
            convertIntoBundle.putParcelable("bitmap_input", createShadowBitmap);
            String str3 = TAG;
            HiAILog.debug(str3, "target bitmap in mode in refine process is " + createShadowBitmap.getWidth() + " * " + createShadowBitmap.getHeight());
            try {
                this.reflect.call("run", Bundle.class, Object.class).invoke(convertIntoBundle, iHiAIVisionCallback);
            } catch (ReflectiveOperationException e2) {
                String str4 = TAG;
                HiAILog.error(str4, "mix-built run error" + e2.getMessage());
            }
        }
    }

    private DocCoordinates resizeResult(DocCoordinates docCoordinates) {
        float scaleX = getScaleX();
        float scaleY = getScaleY();
        if ((Math.abs(((double) scaleX) - 1.0d) > 9.999999974752427E-7d || Math.abs(((double) scaleY) - 1.0d) > 9.999999974752427E-7d) && docCoordinates != null && Math.abs(scaleX) > SCALE_TOLERANCE && Math.abs(scaleY) > SCALE_TOLERANCE) {
            docCoordinates.scaleDocCoordinates(1.0f / scaleX, 1.0f / scaleY);
        }
        return docCoordinates;
    }

    private boolean checkImage(PixelMap pixelMap) {
        Size size = pixelMap.getImageInfo().size;
        int i = size.height;
        int i2 = size.width;
        return i >= 100 && i <= DETECT_MAX_LENGTH && i2 >= 100 && i2 <= DETECT_MAX_LENGTH;
    }

    private IHiAIVisionCallback getDocDetectCallback(final DocCoordinates docCoordinates, final VisionCallback<DocCoordinates> visionCallback, final Lock lock, final Condition condition, final int[] iArr) {
        final boolean z = visionCallback != null;
        return new IHiAIVisionCallback.Stub() {
            /* class ohos.hiaivision.image.docrefine.DocRefine.AnonymousClass1 */

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onInfo(Bundle bundle) throws RemoteException {
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onResult(Bundle bundle) throws RemoteException {
                HiAILog.debug(DocRefine.TAG, "onResult");
                if (bundle != null) {
                    ArrayList<Integer> integerArrayList = bundle.getIntegerArrayList("docrefine_detect");
                    if (integerArrayList == null || integerArrayList.size() <= 0) {
                        HiAILog.error(DocRefine.TAG, "get IntegerArrayList from bundle failed!");
                        iArr[0] = 101;
                    } else {
                        docCoordinates.setDocCoordinates(DocCoordinates.toCoordinates(integerArrayList));
                    }
                } else {
                    HiAILog.error(DocRefine.TAG, "input bundle is null");
                    iArr[0] = 101;
                }
                if (z) {
                    visionCallback.onResult(docCoordinates);
                } else {
                    DocRefine.this.signalLockForSyncMode(lock, condition);
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onError(int i) throws RemoteException {
                HiAILog.debug(DocRefine.TAG, "onError");
                if (z) {
                    visionCallback.onError(i);
                    return;
                }
                iArr[0] = i;
                DocRefine.this.signalLockForSyncMode(lock, condition);
            }
        };
    }

    private IHiAIVisionCallback getDocRefineCallback(final ImageResult imageResult, final VisionCallback<ImageResult> visionCallback, final Lock lock, final Condition condition, final int[] iArr) {
        final boolean z = visionCallback != null;
        return new IHiAIVisionCallback.Stub() {
            /* class ohos.hiaivision.image.docrefine.DocRefine.AnonymousClass2 */

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onInfo(Bundle bundle) throws RemoteException {
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onResult(Bundle bundle) throws RemoteException {
                HiAILog.debug(DocRefine.TAG, "onResult");
                if (bundle != null) {
                    Parcelable parcelable = bundle.getParcelable("docrefine_refine");
                    if (parcelable instanceof Bitmap) {
                        imageResult.setPixelMap(ImageDoubleFwConverter.createShellPixelMap((Bitmap) parcelable));
                        iArr[0] = bundle.getInt("result_code");
                    } else {
                        HiAILog.error(DocRefine.TAG, "get bitMap from bundle failed!");
                        iArr[0] = 101;
                    }
                } else {
                    HiAILog.error(DocRefine.TAG, "bundle is null!");
                    iArr[0] = 101;
                }
                if (z) {
                    visionCallback.onResult(imageResult);
                } else {
                    DocRefine.this.signalLockForSyncMode(lock, condition);
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onError(int i) throws RemoteException {
                HiAILog.debug(DocRefine.TAG, "onError");
                if (z) {
                    visionCallback.onError(i);
                    return;
                }
                iArr[0] = i;
                DocRefine.this.signalLockForSyncMode(lock, condition);
            }
        };
    }
}
