package com.huawei.displayengine;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.RemoteException;
import android.os.Trace;
import com.huawei.displayengine.ImageProcessor.AlgoType;
import com.huawei.displayengine.ImageProcessor.ColorspaceParam;
import com.huawei.displayengine.ImageProcessor.ColorspaceType;
import com.huawei.displayengine.ImageProcessor.CommonInfo;
import com.huawei.displayengine.ImageProcessor.CreateTileProcessEngineParam;
import com.huawei.displayengine.ImageProcessor.ImageEngine;
import com.huawei.displayengine.ImageProcessor.ThumbnailParam;
import com.huawei.displayengine.ImageProcessor.TileEngineType;
import com.huawei.displayengine.ImageProcessor.TileParam;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;

class ImageProcessorAlgoImpl {
    private static final String TAG = "DE J ImageProcessorAlgoImpl";
    private static String mAlgoXmlPath = "";
    private int mHandle;
    private boolean mInited;
    private final IDisplayEngineServiceEx mService;

    private static class CreateCommonAlgoParam {
        private final int mAlgoType;
        private long mCommonHandle;
        private final int mISO;
        private final Bitmap mInBitmap;
        private final boolean mIsAlgoSkinBeauty;
        private final boolean mIsWideColorSpace;
        private final String mXmlPath = ImageProcessorAlgoImpl.mAlgoXmlPath;

        public CreateCommonAlgoParam(ThumbnailParam thumbnailParam) {
            boolean z;
            boolean z2 = false;
            this.mAlgoType = AlgoType.getType(thumbnailParam.mCommonAlgos);
            this.mInBitmap = thumbnailParam.mInBitmap;
            this.mISO = thumbnailParam.mISO;
            if (!thumbnailParam.mAlgoWideColorSpaceEnable || thumbnailParam.mInColorspace == ColorspaceType.SRGB) {
                z = false;
            } else {
                z = true;
            }
            this.mIsWideColorSpace = z;
            if (thumbnailParam.mAlgoSkinBeautyEnable) {
                z2 = thumbnailParam.mSkinBeauty;
            }
            this.mIsAlgoSkinBeauty = z2;
            DElog.i(ImageProcessorAlgoImpl.TAG, "CreateCommonAlgoParam() thumbnail CommonAlgos=" + thumbnailParam.mCommonAlgos);
        }

        public long getCommonHandle() {
            return this.mCommonHandle;
        }
    }

    private static class CreateImageEngineAlgoParam {
        private long mAlgoHandle;
        private final int mAlgoType;
        private final int mBorder;
        private final int mImageHeight;
        private final int mImageWidth;
        private final float mSrMaxScale;
        private final String mXmlPath = ImageProcessorAlgoImpl.mAlgoXmlPath;

        public CreateImageEngineAlgoParam(ThumbnailParam thumbnailParam) {
            float f = 2.0f;
            this.mAlgoType = AlgoType.getType(thumbnailParam.mAlgos);
            if (thumbnailParam.mScaleRatio != 1.0f) {
                if (thumbnailParam.mScaleRatio < 2.0f) {
                    f = thumbnailParam.mScaleRatio;
                }
                this.mSrMaxScale = f;
            } else {
                this.mSrMaxScale = 1.0f;
            }
            this.mImageWidth = thumbnailParam.mInBitmap.getWidth();
            this.mImageHeight = thumbnailParam.mInBitmap.getHeight();
            this.mBorder = 0;
            DElog.i(ImageProcessorAlgoImpl.TAG, "CreateImageEngineAlgoParam() thumbnail type=" + thumbnailParam.mType + ", algos=" + thumbnailParam.mAlgos);
        }

        public CreateImageEngineAlgoParam(CreateTileProcessEngineParam createTileProcessEngineParam) {
            this.mAlgoType = AlgoType.getType(createTileProcessEngineParam.mAlgos);
            if (createTileProcessEngineParam.mEngineType == TileEngineType.SR) {
                this.mSrMaxScale = 2.0f;
            } else {
                this.mSrMaxScale = 1.0f;
            }
            this.mImageWidth = createTileProcessEngineParam.mTileSize;
            this.mImageHeight = createTileProcessEngineParam.mTileSize;
            this.mBorder = createTileProcessEngineParam.mTileBorder;
            DElog.i(ImageProcessorAlgoImpl.TAG, "CreateImageEngineAlgoParam() tile type=" + createTileProcessEngineParam.mEngineType + ", algos=" + createTileProcessEngineParam.mAlgos);
        }

        public long getAlgoHandle() {
            return this.mAlgoHandle;
        }
    }

    private static class GetInfoFromCommonParam {
        private final long mCommonHandle;
        private int mHardwareSharpnessLevel;

        public GetInfoFromCommonParam(CommonInfo commonInfo) {
            this.mCommonHandle = commonInfo.mCommonHandle;
        }

        public int getHardwareSharpnessLevel() {
            return this.mHardwareSharpnessLevel;
        }
    }

    private static class ProcessImageEngineAlgoParam {
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
        private final float mScaleRatio;
        private final float mZoomInRatio;

        public ProcessImageEngineAlgoParam(ImageEngine imageEngine, CommonInfo commonInfo, ThumbnailParam thumbnailParam) {
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
            this.mZoomInRatio = 1.0f;
        }

        public ProcessImageEngineAlgoParam(ImageEngine imageEngine, CommonInfo commonInfo, TileParam tileParam) {
            this.mInBitmap = tileParam.mInBitmap;
            this.mOutBitmap = tileParam.mOutBitmap;
            this.mAlgoHandle = imageEngine.mAlgoHandle;
            this.mCommonHandle = commonInfo.mCommonHandle;
            this.mIsThumbnail = false;
            if (tileParam.mEngineType == TileEngineType.SR) {
                this.mNonSrWidth = 0;
                this.mNonSrHeight = 0;
                this.mNonSrStartX = 0;
                this.mNonSrStartY = 0;
                this.mScaleRatio = tileParam.mScaleRatio;
                this.mSRStartX = tileParam.mScaledStartPoint.x;
                this.mSRStartY = tileParam.mScaledStartPoint.y;
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
            this.mZoomInRatio = 1.0f;
        }
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

    private static class TransformColorspaceAlgoParam {
        private static final /* synthetic */ int[] -com-huawei-displayengine-ImageProcessor$ColorspaceTypeSwitchesValues = null;
        private final Bitmap mInBitmap;
        private final int mInColorspace;
        private final Bitmap mOutBitmap;
        private final int mOutColorspace;

        private static /* synthetic */ int[] -getcom-huawei-displayengine-ImageProcessor$ColorspaceTypeSwitchesValues() {
            if (-com-huawei-displayengine-ImageProcessor$ColorspaceTypeSwitchesValues != null) {
                return -com-huawei-displayengine-ImageProcessor$ColorspaceTypeSwitchesValues;
            }
            int[] iArr = new int[ColorspaceType.values().length];
            try {
                iArr[ColorspaceType.ADOBE_RGB.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ColorspaceType.DISPLAY_P3.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ColorspaceType.SRGB.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ColorspaceType.SUPER_GAMUT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            -com-huawei-displayengine-ImageProcessor$ColorspaceTypeSwitchesValues = iArr;
            return iArr;
        }

        public TransformColorspaceAlgoParam(ColorspaceParam colorspaceParam) {
            this.mInBitmap = colorspaceParam.mInBitmap;
            this.mOutBitmap = colorspaceParam.mOutBitmap;
            this.mInColorspace = getAlgoColorspaceId(colorspaceParam.mInColorspace);
            this.mOutColorspace = getAlgoColorspaceId(colorspaceParam.mOutColorspace);
        }

        private int getAlgoColorspaceId(ColorspaceType colorspaceType) {
            if (colorspaceType == null) {
                return 0;
            }
            switch (-getcom-huawei-displayengine-ImageProcessor$ColorspaceTypeSwitchesValues()[colorspaceType.ordinal()]) {
                case 1:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 0;
                case 4:
                    return 3;
                default:
                    return 0;
            }
        }
    }

    public ImageProcessorAlgoImpl(IDisplayEngineServiceEx service) {
        DElog.i(TAG, "ImageProcessorAlgoImpl enter");
        this.mService = service;
        initAlgo();
        initAlgoXmlPath();
    }

    public boolean isAlgoInitSuccess() {
        return this.mInited;
    }

    protected void finalize() throws Throwable {
        DElog.i(TAG, "finalize");
        try {
            if (this.mInited) {
                DElog.i(TAG, "deinitAlgo enter");
                Trace.traceBegin(8, "deinitAlgo");
                DisplayEngineLibraries.nativeDeinitAlgorithm(0, this.mHandle);
                Trace.traceEnd(8);
                DElog.i(TAG, "deinitAlgo exit");
                this.mInited = false;
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private void initAlgo() {
        if (!this.mInited) {
            DElog.i(TAG, "initAlgo enter");
            Trace.traceBegin(8, "initAlgo");
            int ret = DisplayEngineLibraries.nativeInitAlgorithm(0);
            Trace.traceEnd(8);
            if (ret >= 0) {
                DElog.i(TAG, "initAlgo success");
                this.mInited = true;
                this.mHandle = ret;
            } else {
                DElog.e(TAG, "initAlgo failed! ret = " + ret);
            }
            DElog.i(TAG, "initAlgo exit");
        }
    }

    private String getLcdModelName() {
        if (this.mService == null) {
            DElog.e(TAG, "getLcdModelName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = this.mService.getEffect(14, 0, name, name.length);
            if (ret != 0) {
                DElog.e(TAG, "getLcdModelName() getEffect failed! ret=" + ret);
                return null;
            }
            String lcdModelName = new String(name).trim().replaceAll("[^A-Za-z0-9_.-]", "_");
            DElog.i(TAG, "getLcdModelName() lcdModelName=" + lcdModelName);
            return lcdModelName;
        } catch (RemoteException e) {
            DElog.e(TAG, "getLcdModelName() RemoteException " + e);
            return null;
        }
    }

    private String getProductName() {
        String product = Build.MODEL;
        if (product == null) {
            DElog.e(TAG, "getProductName() get android.os.Build.MODEL failed!");
            return null;
        }
        String[] productSplit = product.split("-");
        if (productSplit.length != 2) {
            DElog.e(TAG, "getProductName() product=" + product + " format error!");
            return null;
        }
        String productName = productSplit[0];
        DElog.i(TAG, "getProductName() productName=" + productName);
        return productName;
    }

    private void initAlgoXmlPath() {
        String lcdModelName = getLcdModelName();
        if (lcdModelName == null) {
            DElog.w(TAG, "initAlgoXmlPath() getLcdModelName error!");
            return;
        }
        String xmlName = lcdModelName + ".xml";
        File xmlFile = HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/" + xmlName, 0);
        if (xmlFile == null) {
            String productName = getProductName();
            if (productName != null) {
                String xmlNameWithProduct = productName + "-" + lcdModelName + ".xml";
                xmlFile = HwCfgFilePolicy.getCfgFile("gallery/display_engine/" + xmlNameWithProduct, 0);
                if (xmlFile == null) {
                    DElog.w(TAG, "initAlgoXmlPath() error! can't find:" + xmlName + " or " + xmlNameWithProduct);
                    return;
                }
            }
            DElog.w(TAG, "initAlgoXmlPath() error! can't find:" + xmlName);
            return;
        }
        try {
            mAlgoXmlPath = xmlFile.getCanonicalPath();
        } catch (IOException e) {
            DElog.e(TAG, "initAlgoXmlPath() IOException " + e);
        }
        DElog.i(TAG, "initAlgoXmlPath() mAlgoXmlPath=" + mAlgoXmlPath);
    }

    public void transformColorspace(ColorspaceParam colorspaceParam) {
        if (this.mInited) {
            DElog.d(TAG, "transformColorspace() " + colorspaceParam.mInColorspace + " -> " + colorspaceParam.mOutColorspace);
            TransformColorspaceAlgoParam param = new TransformColorspaceAlgoParam(colorspaceParam);
            Trace.traceBegin(8, "transformColorspace");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 7, param, null);
            Trace.traceEnd(8);
            if (ret != 0) {
                DElog.e(TAG, "transformColorspace() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("transformColorspace failed ret=" + ret);
            }
            return;
        }
        DElog.e(TAG, "transformColorspace() algo init failed");
        throw new IllegalStateException("transformColorspace algo init failed");
    }

    public long createCommonInfo(ThumbnailParam thumbnailParam) {
        if (this.mInited) {
            DElog.d(TAG, "createCommonInfo()");
            CreateCommonAlgoParam param = new CreateCommonAlgoParam(thumbnailParam);
            Trace.traceBegin(8, "createCommonInfo");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 1, param, null);
            Trace.traceEnd(8);
            long handle = param.getCommonHandle();
            if (ret != 0 || handle == 0) {
                DElog.e(TAG, "createCommonInfo() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("createCommonInfo failed ret=" + ret);
            }
            DElog.i(TAG, "createCommonInfo() commonHandle=" + handle);
            return handle;
        }
        DElog.e(TAG, "createCommonInfo() algo init failed");
        throw new IllegalStateException("createCommonInfo algo init failed");
    }

    public void destroyCommonInfo(CommonInfo commonInfo) {
        if (commonInfo != null && commonInfo.mCommonHandle != 0) {
            if (this.mInited) {
                DElog.i(TAG, "destroyCommonInfo(), commonHandle=" + commonInfo.mCommonHandle);
                Trace.traceBegin(8, "destroyCommonInfo");
                int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 2, commonInfo, null);
                Trace.traceEnd(8);
                if (ret == 0) {
                    commonInfo.mCommonHandle = 0;
                    return;
                } else {
                    DElog.e(TAG, "destroyCommonInfo() native_processAlgorithm error, ret = " + ret);
                    throw new ArithmeticException("destroyCommonInfo failed ret=" + ret);
                }
            }
            DElog.e(TAG, "destroyCommonInfo() algo init failed");
            throw new IllegalStateException("destroyCommonInfo algo init failed");
        }
    }

    public int getHardwareSharpnessLevel(CommonInfo commonInfo) {
        DElog.d(TAG, "getHardwareSharpnessLevel(), handle=" + commonInfo.mCommonHandle);
        return getInfoFromCommon(commonInfo).getHardwareSharpnessLevel();
    }

    private GetInfoFromCommonParam getInfoFromCommon(CommonInfo commonInfo) {
        if (this.mInited) {
            GetInfoFromCommonParam getInfoFromCommonParam = new GetInfoFromCommonParam(commonInfo);
            Trace.traceBegin(8, "getInfoFromCommon");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 3, getInfoFromCommonParam, null);
            Trace.traceEnd(8);
            if (ret == 0) {
                return getInfoFromCommonParam;
            }
            DElog.e(TAG, "getInfoFromCommon() native_processAlgorithm error, ret = " + ret);
            throw new ArithmeticException("getInfoFromCommon failed ret=" + ret);
        }
        DElog.e(TAG, "getInfoFromCommon() algo init failed");
        throw new IllegalStateException("getInfoFromCommon algo init failed");
    }

    public ImageEngine createImageEngine(ThumbnailParam thumbnailParam) {
        return createImageEngine(new CreateImageEngineAlgoParam(thumbnailParam));
    }

    public ImageEngine createImageEngine(CreateTileProcessEngineParam createTileProcessEngineParam) {
        return createImageEngine(new CreateImageEngineAlgoParam(createTileProcessEngineParam));
    }

    private ImageEngine createImageEngine(CreateImageEngineAlgoParam param) {
        if (this.mInited) {
            DElog.d(TAG, "createImageEngineAlgo()");
            Trace.traceBegin(8, "createImageEngine");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 4, param, null);
            Trace.traceEnd(8);
            if (ret != 0 || param.getAlgoHandle() == 0) {
                DElog.e(TAG, "createImageEngineAlgo() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("createImageEngineAlgo failed ret=" + ret);
            }
            DElog.i(TAG, "createImageEngineAlgo() handle=" + param.getAlgoHandle());
            return new ImageEngine(param.getAlgoHandle());
        }
        DElog.e(TAG, "createImageEngine() algo init failed");
        throw new IllegalStateException("createImageEngine algo init failed");
    }

    private void processImageEngine(ProcessImageEngineAlgoParam param) {
        if (this.mInited) {
            DElog.d(TAG, "processImageEngine()");
            Trace.traceBegin(8, "processImageEngine");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 5, param, null);
            Trace.traceEnd(8);
            if (ret != 0) {
                DElog.e(TAG, "processImageEngine() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("processImageEngine failed ret=" + ret);
            }
            return;
        }
        DElog.e(TAG, "processImageEngine() algo init failed");
        throw new IllegalStateException("processImageEngine algo init failed");
    }

    public void processThumbnail(ImageEngine imageEngine, CommonInfo commonInfo, ThumbnailParam thumbnailParam) {
        DElog.i(TAG, "processThumbnail()");
        processImageEngine(new ProcessImageEngineAlgoParam(imageEngine, commonInfo, thumbnailParam));
    }

    public void processTileAlgo(ImageEngine imageEngine, CommonInfo commonInfo, TileParam tileParam) {
        DElog.d(TAG, "processTileAlgo()");
        processImageEngine(new ProcessImageEngineAlgoParam(imageEngine, commonInfo, tileParam));
    }

    public void destroyImageEngine(ImageEngine imageEngine) {
        if (imageEngine.mAlgoHandle != 0) {
            if (this.mInited) {
                DElog.i(TAG, "destroyImageEngine(), handle=" + imageEngine.mAlgoHandle);
                Trace.traceBegin(8, "destroyImageEngine");
                int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 6, imageEngine, null);
                Trace.traceEnd(8);
                if (ret == 0) {
                    imageEngine.mAlgoHandle = 0;
                    return;
                } else {
                    DElog.e(TAG, "destroyImageEngine() native_processAlgorithm error, ret = " + ret);
                    throw new ArithmeticException("destroyImageEngine failed ret=" + ret);
                }
            }
            DElog.e(TAG, "destroyImageEngine() algo init failed");
            throw new IllegalStateException("destroyImageEngine algo init failed");
        }
    }
}
