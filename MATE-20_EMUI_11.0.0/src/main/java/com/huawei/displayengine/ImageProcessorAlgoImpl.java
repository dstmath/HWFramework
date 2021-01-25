package com.huawei.displayengine;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.os.Trace;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.displayengine.ImageProcessor;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/* access modifiers changed from: package-private */
public class ImageProcessorAlgoImpl {
    private static final int ALGO_SUCCESS = 0;
    private static final String DEFAULT_XML_DIR = "display/effect/algorithm/imageprocessor/";
    private static final String DEFAULT_XML_NAME = "ImageProcessAlgoParam.xml";
    private static final int PANEL_NAME_MAX_LENGTH = 128;
    private static final String TAG = "DE J ImageProcessorAlgoImpl";
    private static String sAlgoXmlPath;
    private int mHandle;
    private boolean mIsInited;
    private final IDisplayEngineServiceEx mService;

    ImageProcessorAlgoImpl(IDisplayEngineServiceEx service) {
        DeLog.i(TAG, "ImageProcessorAlgoImpl enter");
        this.mService = service;
        initAlgo();
        initAlgoXmlPath();
    }

    /* access modifiers changed from: package-private */
    public boolean isAlgoInitSuccess() {
        return this.mIsInited;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        DeLog.i(TAG, "finalize");
        if (this.mIsInited) {
            DeLog.i(TAG, "deinitAlgo enter");
            Trace.traceBegin(8, "deinitAlgo");
            DisplayEngineLibraries.nativeDeinitAlgorithm(0, this.mHandle);
            Trace.traceEnd(8);
            DeLog.i(TAG, "deinitAlgo exit");
            this.mIsInited = false;
        }
    }

    private void initAlgo() {
        if (!this.mIsInited) {
            DeLog.i(TAG, "initAlgo enter");
            Trace.traceBegin(8, "initAlgo");
            int ret = DisplayEngineLibraries.nativeInitAlgorithm(0);
            Trace.traceEnd(8);
            if (ret >= 0) {
                DeLog.d(TAG, "initAlgo success");
                this.mIsInited = true;
                this.mHandle = ret;
            } else {
                DeLog.e(TAG, "initAlgo failed! ret = " + ret);
            }
            DeLog.i(TAG, "initAlgo exit");
        }
    }

    private Optional<String> getLcdModelName() {
        IDisplayEngineServiceEx iDisplayEngineServiceEx = this.mService;
        if (iDisplayEngineServiceEx == null) {
            DeLog.e(TAG, "getLcdModelName() mService is null!");
            return Optional.empty();
        }
        byte[] name = new byte[128];
        try {
            int ret = iDisplayEngineServiceEx.getEffect(14, 0, name, name.length);
            if (ret == 0) {
                return Optional.ofNullable(new String(name, StandardCharsets.UTF_8).trim().replaceAll("[^A-Za-z0-9_.-]", "_"));
            }
            DeLog.w(TAG, "getLcdModelName() getEffect failed! ret=" + ret);
            return Optional.empty();
        } catch (RemoteException e) {
            DeLog.e(TAG, "getLcdModelName() RemoteException " + e);
            return Optional.empty();
        }
    }

    private void initAlgoXmlPath() {
        if (sAlgoXmlPath == null) {
            File xmlFile = getAlgoXmlFile();
            if (xmlFile == null) {
                DeLog.w(TAG, "initAlgoXmlPath() error! can't find xml");
                sAlgoXmlPath = StorageManagerExt.INVALID_KEY_DESC;
                return;
            }
            try {
                sAlgoXmlPath = xmlFile.getCanonicalPath();
            } catch (IOException e) {
                DeLog.e(TAG, "initAlgoXmlPath() IOException " + e);
                sAlgoXmlPath = StorageManagerExt.INVALID_KEY_DESC;
            }
            DeLog.i(TAG, "initAlgoXmlPath() success");
        }
    }

    private File getAlgoXmlFile() {
        Optional<String> lcdModelName = getLcdModelName();
        if (!lcdModelName.isPresent()) {
            DeLog.w(TAG, "initAlgoXmlPath() getLcdModelName fail!");
            return HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/ImageProcessAlgoParam.xml", 0);
        }
        return (File) Optional.ofNullable(HwCfgFilePolicy.getCfgFile(DEFAULT_XML_DIR + (lcdModelName.get() + ".xml"), 0)).orElseGet($$Lambda$ImageProcessorAlgoImpl$ojMOCJyTk7r1_npxvyMBpAP2GHk.INSTANCE);
    }

    private static class ProcessType {
        static final int CREATE_COMMON = 1;
        static final int CREATE_IMAGE_ENGINE = 4;
        static final int DESTROY_COMMON = 2;
        static final int DESTROY_IMAGE_ENGINE = 6;
        static final int GET_INFO_FROM_COMMON = 3;
        static final int PROCESS_IMAGE_ENGINE = 5;
        static final int TRANSFORM_COLOR_SPACE = 7;

        private ProcessType() {
        }
    }

    /* access modifiers changed from: private */
    public static class TransformColorSpaceAlgoParam {
        private static final int ADOBE_RGB_ID = 1;
        private static final int DISPLAY_P3_ID = 2;
        private static final int SRGB_ID = 0;
        private static final int SUPER_GAMUT_ID = 3;
        private final Bitmap mInBitmap;
        private final int mInColorSpace;
        private final Bitmap mOutBitmap;
        private final int mOutColorSpace;

        TransformColorSpaceAlgoParam(ImageProcessor.ColorSpaceParam colorSpaceParam) {
            this.mInBitmap = colorSpaceParam.mInBitmap;
            this.mOutBitmap = colorSpaceParam.mOutBitmap;
            this.mInColorSpace = getAlgoColorSpaceId(colorSpaceParam.mInColorSpace);
            this.mOutColorSpace = getAlgoColorSpaceId(colorSpaceParam.mOutColorSpace);
        }

        private int getAlgoColorSpaceId(ImageProcessor.ColorSpaceType colorSpaceType) {
            int i;
            if (colorSpaceType == null || (i = AnonymousClass1.$SwitchMap$com$huawei$displayengine$ImageProcessor$ColorSpaceType[colorSpaceType.ordinal()]) == 1) {
                return 0;
            }
            if (i == 2) {
                return 1;
            }
            if (i == 3) {
                return 2;
            }
            if (i != 4) {
                return 0;
            }
            return 3;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.displayengine.ImageProcessorAlgoImpl$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorSpaceType = new int[ImageProcessor.ColorSpaceType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorSpaceType[ImageProcessor.ColorSpaceType.SRGB.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorSpaceType[ImageProcessor.ColorSpaceType.ADOBE_RGB.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorSpaceType[ImageProcessor.ColorSpaceType.DISPLAY_P3.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorSpaceType[ImageProcessor.ColorSpaceType.SUPER_GAMUT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void transformColorSpace(ImageProcessor.ColorSpaceParam colorSpaceParam) {
        if (this.mIsInited) {
            DeLog.d(TAG, "transformColorSpace() " + colorSpaceParam.mInColorSpace + " -> " + colorSpaceParam.mOutColorSpace);
            TransformColorSpaceAlgoParam param = new TransformColorSpaceAlgoParam(colorSpaceParam);
            Trace.traceBegin(8, "transformColorSpace");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 7, param, null);
            Trace.traceEnd(8);
            if (ret != 0) {
                DeLog.e(TAG, "transformColorSpace() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("transformColorSpace failed ret=" + ret);
            }
            return;
        }
        DeLog.e(TAG, "transformColorSpace() algo init failed");
        throw new IllegalStateException("transformColorSpace algo init failed");
    }

    /* access modifiers changed from: private */
    public static class CreateCommonAlgoParam {
        private final int mAlgoType;
        private long mCommonHandle;
        private final Bitmap mInBitmap;
        private final boolean mIsAlgoSkinBeauty;
        private final boolean mIsAlgoVivid;
        private final boolean mIsWideColorSpace;
        private final int mIso;
        private final String mXmlPath = ImageProcessorAlgoImpl.sAlgoXmlPath;

        CreateCommonAlgoParam(ImageProcessor.ThumbnailParam thumbnailParam) {
            this.mAlgoType = ImageProcessor.AlgoType.getType(thumbnailParam.mCommonAlgos);
            this.mInBitmap = thumbnailParam.mInBitmap;
            this.mIso = thumbnailParam.mIso;
            boolean z = true;
            this.mIsWideColorSpace = thumbnailParam.mImageType == ImageProcessor.ImageType.WIDE_COLOR_SPACE;
            this.mIsAlgoSkinBeauty = thumbnailParam.mImageType == ImageProcessor.ImageType.SKIN_BEAUTY;
            this.mIsAlgoVivid = thumbnailParam.mImageType != ImageProcessor.ImageType.VIVID ? false : z;
            DeLog.i(ImageProcessorAlgoImpl.TAG, "CreateCommonAlgoParam() thumbnail imageType=" + thumbnailParam.mImageType + ", commonAlgos=" + thumbnailParam.mCommonAlgos);
        }

        /* access modifiers changed from: package-private */
        public long getCommonHandle() {
            return this.mCommonHandle;
        }
    }

    /* access modifiers changed from: package-private */
    public long createCommonInfo(ImageProcessor.ThumbnailParam thumbnailParam) {
        if (this.mIsInited) {
            DeLog.d(TAG, "createCommonInfo()");
            CreateCommonAlgoParam param = new CreateCommonAlgoParam(thumbnailParam);
            Trace.traceBegin(8, "createCommonInfo");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 1, param, null);
            Trace.traceEnd(8);
            long handle = param.getCommonHandle();
            if (ret != 0 || handle == 0) {
                DeLog.e(TAG, "createCommonInfo() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("createCommonInfo failed ret=" + ret);
            }
            DeLog.d(TAG, "createCommonInfo() commonHandle=" + handle);
            return handle;
        }
        DeLog.e(TAG, "createCommonInfo() algo init failed");
        throw new IllegalStateException("createCommonInfo algo init failed");
    }

    /* access modifiers changed from: package-private */
    public void destroyCommonInfo(ImageProcessor.CommonInfo commonInfo) {
        if (commonInfo != null && commonInfo.mCommonHandle != 0) {
            if (this.mIsInited) {
                DeLog.d(TAG, "destroyCommonInfo(), commonHandle=" + commonInfo.mCommonHandle);
                Trace.traceBegin(8, "destroyCommonInfo");
                int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 2, commonInfo, null);
                Trace.traceEnd(8);
                if (ret == 0) {
                    commonInfo.mCommonHandle = 0;
                    return;
                }
                DeLog.e(TAG, "destroyCommonInfo() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("destroyCommonInfo failed ret=" + ret);
            }
            DeLog.e(TAG, "destroyCommonInfo() algo init failed");
            throw new IllegalStateException("destroyCommonInfo algo init failed");
        }
    }

    /* access modifiers changed from: private */
    public static class GetInfoFromCommonParam {
        private final long mCommonHandle;
        private int mHardwareSharpnessLevel;

        GetInfoFromCommonParam(ImageProcessor.CommonInfo commonInfo) {
            this.mCommonHandle = commonInfo.mCommonHandle;
        }

        /* access modifiers changed from: package-private */
        public int getHardwareSharpnessLevel() {
            return this.mHardwareSharpnessLevel;
        }
    }

    /* access modifiers changed from: package-private */
    public int getHardwareSharpnessLevel(ImageProcessor.CommonInfo commonInfo) {
        DeLog.d(TAG, "getHardwareSharpnessLevel(), handle=" + commonInfo.mCommonHandle);
        return getInfoFromCommon(commonInfo).getHardwareSharpnessLevel();
    }

    private GetInfoFromCommonParam getInfoFromCommon(ImageProcessor.CommonInfo commonInfo) {
        if (this.mIsInited) {
            GetInfoFromCommonParam getInfoFromCommonParam = new GetInfoFromCommonParam(commonInfo);
            Trace.traceBegin(8, "getInfoFromCommon");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 3, getInfoFromCommonParam, null);
            Trace.traceEnd(8);
            if (ret == 0) {
                return getInfoFromCommonParam;
            }
            DeLog.e(TAG, "getInfoFromCommon() native_processAlgorithm error, ret = " + ret);
            throw new ArithmeticException("getInfoFromCommon failed ret=" + ret);
        }
        DeLog.e(TAG, "getInfoFromCommon() algo init failed");
        throw new IllegalStateException("getInfoFromCommon algo init failed");
    }

    /* access modifiers changed from: private */
    public static class CreateImageEngineAlgoParam {
        private static final float MAX_SCALE_RATIO = 2.0f;
        private static final float NO_SCALE_RATIO = 1.0f;
        private long mAlgoHandle;
        private final int mAlgoType;
        private final int mBorder;
        private final int mImageHeight;
        private final int mImageWidth;
        private final float mSrMaxScale;
        private final String mXmlPath = ImageProcessorAlgoImpl.sAlgoXmlPath;

        CreateImageEngineAlgoParam(ImageProcessor.ThumbnailParam thumbnailParam) {
            this.mAlgoType = ImageProcessor.AlgoType.getType(thumbnailParam.mAlgos);
            if (Float.compare(thumbnailParam.mScaleRatio, 1.0f) != 0) {
                this.mSrMaxScale = thumbnailParam.mScaleRatio < 2.0f ? thumbnailParam.mScaleRatio : 2.0f;
            } else {
                this.mSrMaxScale = 1.0f;
            }
            this.mImageWidth = thumbnailParam.mInBitmap.getWidth();
            this.mImageHeight = thumbnailParam.mInBitmap.getHeight();
            this.mBorder = 0;
            DeLog.i(ImageProcessorAlgoImpl.TAG, "CreateImageEngineAlgoParam() thumbnail type=" + thumbnailParam.mType + ", imageType=" + thumbnailParam.mImageType + ", algos=" + thumbnailParam.mAlgos);
        }

        CreateImageEngineAlgoParam(ImageProcessor.CreateTileProcessEngineParam createTileProcessEngineParam) {
            this.mAlgoType = ImageProcessor.AlgoType.getType(createTileProcessEngineParam.mAlgos);
            this.mSrMaxScale = createTileProcessEngineParam.mEngineType == ImageProcessor.TileEngineType.SR ? 2.0f : 1.0f;
            this.mImageWidth = createTileProcessEngineParam.mTileSize;
            this.mImageHeight = createTileProcessEngineParam.mTileSize;
            this.mBorder = createTileProcessEngineParam.mTileBorder;
            DeLog.i(ImageProcessorAlgoImpl.TAG, "CreateImageEngineAlgoParam() tile type=" + createTileProcessEngineParam.mEngineType + ", algos=" + createTileProcessEngineParam.mAlgos);
        }

        /* access modifiers changed from: package-private */
        public long getAlgoHandle() {
            return this.mAlgoHandle;
        }
    }

    /* access modifiers changed from: package-private */
    public ImageProcessor.ImageEngine createImageEngine(ImageProcessor.ThumbnailParam thumbnailParam) {
        return createImageEngine(new CreateImageEngineAlgoParam(thumbnailParam));
    }

    /* access modifiers changed from: package-private */
    public ImageProcessor.ImageEngine createImageEngine(ImageProcessor.CreateTileProcessEngineParam createTileProcessEngineParam) {
        return createImageEngine(new CreateImageEngineAlgoParam(createTileProcessEngineParam));
    }

    private ImageProcessor.ImageEngine createImageEngine(CreateImageEngineAlgoParam param) {
        if (this.mIsInited) {
            DeLog.d(TAG, "createImageEngineAlgo()");
            Trace.traceBegin(8, "createImageEngine");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 4, param, null);
            Trace.traceEnd(8);
            if (ret != 0 || param.getAlgoHandle() == 0) {
                DeLog.e(TAG, "createImageEngineAlgo() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("createImageEngineAlgo failed ret=" + ret);
            }
            DeLog.d(TAG, "createImageEngineAlgo() handle=" + param.getAlgoHandle());
            return new ImageProcessor.ImageEngine(param.getAlgoHandle());
        }
        DeLog.e(TAG, "createImageEngine() algo init failed");
        throw new IllegalStateException("createImageEngine algo init failed");
    }

    /* access modifiers changed from: private */
    public static class ProcessImageEngineAlgoParam {
        private final long mAlgoHandle;
        private final long mCommonHandle;
        private final Bitmap mInBitmap;
        private final boolean mIsThumbnail;
        private final int mNonSrHeight;
        private final int mNonSrStartX;
        private final int mNonSrStartY;
        private final int mNonSrWidth;
        private final Bitmap mOutBitmap;
        private final float mScaleRatio;
        private final float mSrStartX;
        private final float mSrStartY;
        private final int mSrVisibleEndX;
        private final int mSrVisibleEndY;
        private final int mSrVisibleStartX;
        private final int mSrVisibleStartY;
        private final float mZoomInRatio;

        ProcessImageEngineAlgoParam(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.ThumbnailParam thumbnailParam) {
            this.mInBitmap = thumbnailParam.mInBitmap;
            this.mOutBitmap = thumbnailParam.mOutBitmap;
            this.mAlgoHandle = imageEngine.mAlgoHandle;
            this.mCommonHandle = commonInfo.mCommonHandle;
            this.mIsThumbnail = true;
            this.mNonSrWidth = this.mInBitmap.getWidth();
            this.mNonSrHeight = this.mInBitmap.getHeight();
            this.mNonSrStartX = 0;
            this.mNonSrStartY = 0;
            this.mScaleRatio = thumbnailParam.mScaleRatio;
            this.mSrStartX = 0.0f;
            this.mSrStartY = 0.0f;
            this.mSrVisibleStartX = 0;
            this.mSrVisibleStartY = 0;
            this.mSrVisibleEndX = this.mInBitmap.getWidth() - 1;
            this.mSrVisibleEndY = this.mInBitmap.getHeight() - 1;
            this.mZoomInRatio = 1.0f;
        }

        ProcessImageEngineAlgoParam(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.TileParam tileParam) {
            this.mInBitmap = tileParam.mInBitmap;
            this.mOutBitmap = tileParam.mOutBitmap;
            this.mAlgoHandle = imageEngine.mAlgoHandle;
            this.mCommonHandle = commonInfo.mCommonHandle;
            this.mIsThumbnail = false;
            if (tileParam.mEngineType == ImageProcessor.TileEngineType.SR) {
                this.mNonSrWidth = 0;
                this.mNonSrHeight = 0;
                this.mNonSrStartX = 0;
                this.mNonSrStartY = 0;
                this.mScaleRatio = tileParam.mScaleRatio;
                this.mSrStartX = tileParam.mScaledStartPoint.x;
                this.mSrStartY = tileParam.mScaledStartPoint.y;
                this.mSrVisibleStartX = tileParam.mInVisibleRange.left;
                this.mSrVisibleStartY = tileParam.mInVisibleRange.top;
                this.mSrVisibleEndX = tileParam.mInVisibleRange.right;
                this.mSrVisibleEndY = tileParam.mInVisibleRange.bottom;
                this.mZoomInRatio = tileParam.mZoomInRatio;
                return;
            }
            this.mNonSrWidth = tileParam.mDecodedSize.getWidth();
            this.mNonSrHeight = tileParam.mDecodedSize.getHeight();
            this.mNonSrStartX = tileParam.mDecodedStartPoint.x;
            this.mNonSrStartY = tileParam.mDecodedStartPoint.y;
            this.mScaleRatio = 1.0f;
            this.mSrStartX = 0.0f;
            this.mSrStartY = 0.0f;
            this.mSrVisibleStartX = 0;
            this.mSrVisibleStartY = 0;
            this.mSrVisibleEndX = this.mInBitmap.getWidth() - 1;
            this.mSrVisibleEndY = this.mInBitmap.getHeight() - 1;
            this.mZoomInRatio = 1.0f;
        }
    }

    private void processImageEngine(ProcessImageEngineAlgoParam param) {
        if (this.mIsInited) {
            DeLog.d(TAG, "processImageEngine()");
            Trace.traceBegin(8, "processImageEngine");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 5, param, null);
            Trace.traceEnd(8);
            if (ret != 0) {
                DeLog.e(TAG, "processImageEngine() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("processImageEngine failed ret=" + ret);
            }
            return;
        }
        DeLog.e(TAG, "processImageEngine() algo init failed");
        throw new IllegalStateException("processImageEngine algo init failed");
    }

    /* access modifiers changed from: package-private */
    public void processThumbnail(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.ThumbnailParam thumbnailParam) {
        DeLog.i(TAG, "processThumbnail()");
        processImageEngine(new ProcessImageEngineAlgoParam(imageEngine, commonInfo, thumbnailParam));
    }

    /* access modifiers changed from: package-private */
    public void processTileAlgo(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.TileParam tileParam) {
        DeLog.d(TAG, "processTileAlgo()");
        processImageEngine(new ProcessImageEngineAlgoParam(imageEngine, commonInfo, tileParam));
    }

    /* access modifiers changed from: package-private */
    public void destroyImageEngine(ImageProcessor.ImageEngine imageEngine) {
        if (imageEngine.mAlgoHandle != 0) {
            if (this.mIsInited) {
                DeLog.d(TAG, "destroyImageEngine(), handle=" + imageEngine.mAlgoHandle);
                Trace.traceBegin(8, "destroyImageEngine");
                int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 6, imageEngine, null);
                Trace.traceEnd(8);
                if (ret == 0) {
                    imageEngine.mAlgoHandle = 0;
                    return;
                }
                DeLog.e(TAG, "destroyImageEngine() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("destroyImageEngine failed ret=" + ret);
            }
            DeLog.e(TAG, "destroyImageEngine() algo init failed");
            throw new IllegalStateException("destroyImageEngine algo init failed");
        }
    }
}
