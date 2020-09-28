package com.huawei.displayengine;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.RemoteException;
import android.os.Trace;
import com.huawei.displayengine.ImageProcessor;
import com.huawei.uikit.effect.BuildConfig;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/* access modifiers changed from: package-private */
public class ImageProcessorAlgoImpl {
    private static final String TAG = "DE J ImageProcessorAlgoImpl";
    private static String mAlgoXmlPath = BuildConfig.FLAVOR;
    private int mHandle;
    private boolean mInited;
    private final IDisplayEngineServiceEx mService;

    public ImageProcessorAlgoImpl(IDisplayEngineServiceEx service) {
        DeLog.i(TAG, "ImageProcessorAlgoImpl enter");
        this.mService = service;
        initAlgo();
        initAlgoXmlPath();
    }

    public boolean isAlgoInitSuccess() {
        return this.mInited;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        DeLog.i(TAG, "finalize");
        if (this.mInited) {
            DeLog.i(TAG, "deinitAlgo enter");
            Trace.traceBegin(8, "deinitAlgo");
            DisplayEngineLibraries.nativeDeinitAlgorithm(0, this.mHandle);
            Trace.traceEnd(8);
            DeLog.i(TAG, "deinitAlgo exit");
            this.mInited = false;
        }
    }

    private void initAlgo() {
        if (!this.mInited) {
            DeLog.i(TAG, "initAlgo enter");
            Trace.traceBegin(8, "initAlgo");
            int ret = DisplayEngineLibraries.nativeInitAlgorithm(0);
            Trace.traceEnd(8);
            if (ret >= 0) {
                DeLog.d(TAG, "initAlgo success");
                this.mInited = true;
                this.mHandle = ret;
            } else {
                DeLog.e(TAG, "initAlgo failed! ret = " + ret);
            }
            DeLog.i(TAG, "initAlgo exit");
        }
    }

    private String getLcdModelName() {
        IDisplayEngineServiceEx iDisplayEngineServiceEx = this.mService;
        if (iDisplayEngineServiceEx == null) {
            DeLog.e(TAG, "getLcdModelName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = iDisplayEngineServiceEx.getEffect(14, 0, name, name.length);
            if (ret == 0) {
                return new String(name, StandardCharsets.UTF_8).trim().replaceAll("[^A-Za-z0-9_.-]", "_");
            }
            DeLog.w(TAG, "getLcdModelName() getEffect failed! ret=" + ret);
            return null;
        } catch (RemoteException e) {
            DeLog.e(TAG, "getLcdModelName() RemoteException " + e);
            return null;
        }
    }

    private String getProductName() {
        String product = Build.MODEL;
        if (product == null) {
            DeLog.e(TAG, "getProductName() get android.os.Build.MODEL failed!");
            return null;
        }
        String[] productSplit = product.split("-");
        if (productSplit.length != 2) {
            DeLog.e(TAG, "getProductName() product=" + product + " format error!");
            return null;
        }
        String productName = productSplit[0];
        DeLog.i(TAG, "getProductName() productName=" + productName);
        return productName;
    }

    private void initAlgoXmlPath() {
        File xmlFile = getAlgoXmlFile();
        if (xmlFile == null) {
            DeLog.w(TAG, "initAlgoXmlPath() error! can't find xml");
            return;
        }
        try {
            mAlgoXmlPath = xmlFile.getCanonicalPath();
        } catch (IOException e) {
            DeLog.e(TAG, "initAlgoXmlPath() IOException " + e);
        }
        DeLog.i(TAG, "initAlgoXmlPath() success");
    }

    private File getAlgoXmlFile() {
        String lcdModelName = getLcdModelName();
        if (lcdModelName == null) {
            DeLog.w(TAG, "initAlgoXmlPath() getLcdModelName fail!");
            return HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/ImageProcessAlgoParam.xml", 0);
        }
        File xmlFile = HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/" + (lcdModelName + ".xml"), 0);
        if (xmlFile != null) {
            return xmlFile;
        }
        File xmlFile2 = HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/ImageProcessAlgoParam.xml", 0);
        if (xmlFile2 != null) {
            return xmlFile2;
        }
        String productName = getProductName();
        if (productName == null) {
            return null;
        }
        return HwCfgFilePolicy.getCfgFile("gallery/display_engine/" + (productName + "-" + lcdModelName + ".xml"), 0);
    }

    private static class ProcessType {
        public static final int CREATE_COMMON = 1;
        public static final int CREATE_IMAGE_ENGINE = 4;
        public static final int DESTROY_COMMON = 2;
        public static final int DESTROY_IMAGE_ENGINE = 6;
        public static final int GET_INFO_FROM_COMMON = 3;
        public static final int PROCESS_IMAGE_ENGINE = 5;
        public static final int TRANSFORM_COLORSPACE = 7;

        private ProcessType() {
        }
    }

    /* access modifiers changed from: private */
    public static class TransformColorspaceAlgoParam {
        private final Bitmap mInBitmap;
        private final int mInColorspace;
        private final Bitmap mOutBitmap;
        private final int mOutColorspace;

        public TransformColorspaceAlgoParam(ImageProcessor.ColorspaceParam colorspaceParam) {
            this.mInBitmap = colorspaceParam.mInBitmap;
            this.mOutBitmap = colorspaceParam.mOutBitmap;
            this.mInColorspace = getAlgoColorspaceId(colorspaceParam.mInColorspace);
            this.mOutColorspace = getAlgoColorspaceId(colorspaceParam.mOutColorspace);
        }

        private int getAlgoColorspaceId(ImageProcessor.ColorspaceType colorspaceType) {
            int i;
            if (colorspaceType == null || (i = AnonymousClass1.$SwitchMap$com$huawei$displayengine$ImageProcessor$ColorspaceType[colorspaceType.ordinal()]) == 1) {
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
        static final /* synthetic */ int[] $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorspaceType = new int[ImageProcessor.ColorspaceType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorspaceType[ImageProcessor.ColorspaceType.SRGB.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorspaceType[ImageProcessor.ColorspaceType.ADOBE_RGB.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorspaceType[ImageProcessor.ColorspaceType.DISPLAY_P3.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ColorspaceType[ImageProcessor.ColorspaceType.SUPER_GAMUT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public void transformColorspace(ImageProcessor.ColorspaceParam colorspaceParam) {
        if (this.mInited) {
            DeLog.d(TAG, "transformColorspace() " + colorspaceParam.mInColorspace + " -> " + colorspaceParam.mOutColorspace);
            TransformColorspaceAlgoParam param = new TransformColorspaceAlgoParam(colorspaceParam);
            Trace.traceBegin(8, "transformColorspace");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 7, param, null);
            Trace.traceEnd(8);
            if (ret != 0) {
                DeLog.e(TAG, "transformColorspace() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("transformColorspace failed ret=" + ret);
            }
            return;
        }
        DeLog.e(TAG, "transformColorspace() algo init failed");
        throw new IllegalStateException("transformColorspace algo init failed");
    }

    /* access modifiers changed from: private */
    public static class CreateCommonAlgoParam {
        private final int mAlgoType;
        private long mCommonHandle;
        private final int mISO;
        private final Bitmap mInBitmap;
        private final boolean mIsAlgoSkinBeauty;
        private final boolean mIsAlgoVivid;
        private final boolean mIsWideColorSpace;
        private final String mXmlPath = ImageProcessorAlgoImpl.mAlgoXmlPath;

        public CreateCommonAlgoParam(ImageProcessor.ThumbnailParam thumbnailParam) {
            this.mAlgoType = ImageProcessor.AlgoType.getType(thumbnailParam.mCommonAlgos);
            this.mInBitmap = thumbnailParam.mInBitmap;
            this.mISO = thumbnailParam.mISO;
            boolean z = true;
            this.mIsWideColorSpace = thumbnailParam.mImageType == ImageProcessor.ImageType.WIDE_COLOR_SPACE;
            this.mIsAlgoSkinBeauty = thumbnailParam.mImageType == ImageProcessor.ImageType.SKIN_BEAUTY;
            this.mIsAlgoVivid = thumbnailParam.mImageType != ImageProcessor.ImageType.VIVID ? false : z;
            DeLog.i(ImageProcessorAlgoImpl.TAG, "CreateCommonAlgoParam() thumbnail imageType=" + thumbnailParam.mImageType + ", commonAlgos=" + thumbnailParam.mCommonAlgos);
        }

        public long getCommonHandle() {
            return this.mCommonHandle;
        }
    }

    public long createCommonInfo(ImageProcessor.ThumbnailParam thumbnailParam) {
        if (this.mInited) {
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

    public void destroyCommonInfo(ImageProcessor.CommonInfo commonInfo) {
        if (commonInfo != null && commonInfo.mCommonHandle != 0) {
            if (this.mInited) {
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

        public GetInfoFromCommonParam(ImageProcessor.CommonInfo commonInfo) {
            this.mCommonHandle = commonInfo.mCommonHandle;
        }

        public int getHardwareSharpnessLevel() {
            return this.mHardwareSharpnessLevel;
        }
    }

    public int getHardwareSharpnessLevel(ImageProcessor.CommonInfo commonInfo) {
        DeLog.d(TAG, "getHardwareSharpnessLevel(), handle=" + commonInfo.mCommonHandle);
        return getInfoFromCommon(commonInfo).getHardwareSharpnessLevel();
    }

    private GetInfoFromCommonParam getInfoFromCommon(ImageProcessor.CommonInfo commonInfo) {
        if (this.mInited) {
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
        private long mAlgoHandle;
        private final int mAlgoType;
        private final int mBorder;
        private final int mImageHeight;
        private final int mImageWidth;
        private final float mSrMaxScale;
        private final String mXmlPath = ImageProcessorAlgoImpl.mAlgoXmlPath;

        public CreateImageEngineAlgoParam(ImageProcessor.ThumbnailParam thumbnailParam) {
            this.mAlgoType = ImageProcessor.AlgoType.getType(thumbnailParam.mAlgos);
            if (thumbnailParam.mScaleRatio != 1.0f) {
                this.mSrMaxScale = thumbnailParam.mScaleRatio < 2.0f ? thumbnailParam.mScaleRatio : 2.0f;
            } else {
                this.mSrMaxScale = 1.0f;
            }
            this.mImageWidth = thumbnailParam.mInBitmap.getWidth();
            this.mImageHeight = thumbnailParam.mInBitmap.getHeight();
            this.mBorder = 0;
            DeLog.i(ImageProcessorAlgoImpl.TAG, "CreateImageEngineAlgoParam() thumbnail type=" + thumbnailParam.mType + ", imageType=" + thumbnailParam.mImageType + ", algos=" + thumbnailParam.mAlgos);
        }

        public CreateImageEngineAlgoParam(ImageProcessor.CreateTileProcessEngineParam createTileProcessEngineParam) {
            this.mAlgoType = ImageProcessor.AlgoType.getType(createTileProcessEngineParam.mAlgos);
            if (createTileProcessEngineParam.mEngineType == ImageProcessor.TileEngineType.SR) {
                this.mSrMaxScale = 2.0f;
            } else {
                this.mSrMaxScale = 1.0f;
            }
            this.mImageWidth = createTileProcessEngineParam.mTileSize;
            this.mImageHeight = createTileProcessEngineParam.mTileSize;
            this.mBorder = createTileProcessEngineParam.mTileBorder;
            DeLog.i(ImageProcessorAlgoImpl.TAG, "CreateImageEngineAlgoParam() tile type=" + createTileProcessEngineParam.mEngineType + ", algos=" + createTileProcessEngineParam.mAlgos);
        }

        public long getAlgoHandle() {
            return this.mAlgoHandle;
        }
    }

    public ImageProcessor.ImageEngine createImageEngine(ImageProcessor.ThumbnailParam thumbnailParam) {
        return createImageEngine(new CreateImageEngineAlgoParam(thumbnailParam));
    }

    public ImageProcessor.ImageEngine createImageEngine(ImageProcessor.CreateTileProcessEngineParam createTileProcessEngineParam) {
        return createImageEngine(new CreateImageEngineAlgoParam(createTileProcessEngineParam));
    }

    private ImageProcessor.ImageEngine createImageEngine(CreateImageEngineAlgoParam param) {
        if (this.mInited) {
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
        private final float mSRStartX;
        private final float mSRStartY;
        private final int mSRVisibleEndX;
        private final int mSRVisibleEndY;
        private final int mSRVisibleStartX;
        private final int mSRVisibleStartY;
        private final float mScaleRatio;
        private final float mZoomInRatio;

        public ProcessImageEngineAlgoParam(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.ThumbnailParam thumbnailParam) {
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
            this.mSRStartX = 0.0f;
            this.mSRStartY = 0.0f;
            this.mSRVisibleStartX = 0;
            this.mSRVisibleStartY = 0;
            this.mSRVisibleEndX = this.mInBitmap.getWidth() - 1;
            this.mSRVisibleEndY = this.mInBitmap.getHeight() - 1;
            this.mZoomInRatio = 1.0f;
        }

        public ProcessImageEngineAlgoParam(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.TileParam tileParam) {
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
                this.mSRStartX = tileParam.mScaledStartPoint.x;
                this.mSRStartY = tileParam.mScaledStartPoint.y;
                this.mSRVisibleStartX = tileParam.mInVisibleRange.left;
                this.mSRVisibleStartY = tileParam.mInVisibleRange.top;
                this.mSRVisibleEndX = tileParam.mInVisibleRange.right;
                this.mSRVisibleEndY = tileParam.mInVisibleRange.bottom;
                this.mZoomInRatio = tileParam.mZoomInRatio;
                return;
            }
            this.mNonSrWidth = tileParam.mDecodedSize.getWidth();
            this.mNonSrHeight = tileParam.mDecodedSize.getHeight();
            this.mNonSrStartX = tileParam.mDecodedStartPoint.x;
            this.mNonSrStartY = tileParam.mDecodedStartPoint.y;
            this.mScaleRatio = 1.0f;
            this.mSRStartX = 0.0f;
            this.mSRStartY = 0.0f;
            this.mSRVisibleStartX = 0;
            this.mSRVisibleStartY = 0;
            this.mSRVisibleEndX = this.mInBitmap.getWidth() - 1;
            this.mSRVisibleEndY = this.mInBitmap.getHeight() - 1;
            this.mZoomInRatio = 1.0f;
        }
    }

    private void processImageEngine(ProcessImageEngineAlgoParam param) {
        if (this.mInited) {
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

    public void processThumbnail(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.ThumbnailParam thumbnailParam) {
        DeLog.i(TAG, "processThumbnail()");
        processImageEngine(new ProcessImageEngineAlgoParam(imageEngine, commonInfo, thumbnailParam));
    }

    public void processTileAlgo(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.TileParam tileParam) {
        DeLog.d(TAG, "processTileAlgo()");
        processImageEngine(new ProcessImageEngineAlgoParam(imageEngine, commonInfo, tileParam));
    }

    public void destroyImageEngine(ImageProcessor.ImageEngine imageEngine) {
        if (imageEngine.mAlgoHandle != 0) {
            if (this.mInited) {
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
