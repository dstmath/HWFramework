package com.huawei.displayengine;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.RemoteException;
import android.os.Trace;
import com.huawei.displayengine.ImageProcessor;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;

class ImageProcessorAlgoImpl {
    private static final String TAG = "DE J ImageProcessorAlgoImpl";
    /* access modifiers changed from: private */
    public static String mAlgoXmlPath = "";
    private int mHandle;
    private boolean mInited;
    private final IDisplayEngineServiceEx mService;

    private static class CreateCommonAlgoParam {
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
            boolean z = false;
            this.mIsWideColorSpace = thumbnailParam.mImageType == ImageProcessor.ImageType.WIDE_COLOR_SPACE;
            this.mIsAlgoSkinBeauty = thumbnailParam.mImageType == ImageProcessor.ImageType.SKIN_BEAUTY;
            this.mIsAlgoVivid = thumbnailParam.mImageType == ImageProcessor.ImageType.VIVID ? true : z;
            DElog.i(ImageProcessorAlgoImpl.TAG, "CreateCommonAlgoParam() thumbnail imageType=" + thumbnailParam.mImageType + ", commonAlgos=" + thumbnailParam.mCommonAlgos);
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
            DElog.i(ImageProcessorAlgoImpl.TAG, "CreateImageEngineAlgoParam() thumbnail type=" + thumbnailParam.mType + ", imageType=" + thumbnailParam.mImageType + ", algos=" + thumbnailParam.mAlgos);
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
            DElog.i(ImageProcessorAlgoImpl.TAG, "CreateImageEngineAlgoParam() tile type=" + createTileProcessEngineParam.mEngineType + ", algos=" + createTileProcessEngineParam.mAlgos);
        }

        public long getAlgoHandle() {
            return this.mAlgoHandle;
        }
    }

    private static class GetInfoFromCommonParam {
        private final long mCommonHandle;
        private int mHardwareSharpnessLevel;

        public GetInfoFromCommonParam(ImageProcessor.CommonInfo commonInfo) {
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
            if (colorspaceType == null) {
                return 0;
            }
            switch (colorspaceType) {
                case SRGB:
                    return 0;
                case ADOBE_RGB:
                    return 1;
                case DISPLAY_P3:
                    return 2;
                case SUPER_GAMUT:
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

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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
        } finally {
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
                DElog.d(TAG, "initAlgo success");
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
            if (ret == 0) {
                return new String(name).trim().replaceAll("[^A-Za-z0-9_.-]", "_");
            }
            DElog.w(TAG, "getLcdModelName() getEffect failed! ret=" + ret);
            return null;
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
        File xmlFile = getAlgoXmlFile();
        if (xmlFile == null) {
            DElog.w(TAG, "initAlgoXmlPath() error! can't find xml");
            return;
        }
        try {
            mAlgoXmlPath = xmlFile.getCanonicalPath();
        } catch (IOException e) {
            DElog.e(TAG, "initAlgoXmlPath() IOException " + e);
        }
        DElog.i(TAG, "initAlgoXmlPath() success");
    }

    private File getAlgoXmlFile() {
        if (getLcdModelName() == null) {
            DElog.w(TAG, "initAlgoXmlPath() getLcdModelName fail!");
            return HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/ImageProcessAlgoParam.xml", 0);
        }
        String xmlName = lcdModelName + ".xml";
        File xmlFile = HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/" + xmlName, 0);
        if (xmlFile != null) {
            return xmlFile;
        }
        File xmlFile2 = HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/ImageProcessAlgoParam.xml", 0);
        if (xmlFile2 != null) {
            return xmlFile2;
        }
        if (getProductName() == null) {
            return null;
        }
        String xmlNameWithProduct = productName + "-" + lcdModelName + ".xml";
        return HwCfgFilePolicy.getCfgFile("gallery/display_engine/" + xmlNameWithProduct, 0);
    }

    public void transformColorspace(ImageProcessor.ColorspaceParam colorspaceParam) {
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

    public long createCommonInfo(ImageProcessor.ThumbnailParam thumbnailParam) {
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
            DElog.d(TAG, "createCommonInfo() commonHandle=" + handle);
            return handle;
        }
        DElog.e(TAG, "createCommonInfo() algo init failed");
        throw new IllegalStateException("createCommonInfo algo init failed");
    }

    public void destroyCommonInfo(ImageProcessor.CommonInfo commonInfo) {
        if (commonInfo != null && commonInfo.mCommonHandle != 0) {
            if (this.mInited) {
                DElog.d(TAG, "destroyCommonInfo(), commonHandle=" + commonInfo.mCommonHandle);
                Trace.traceBegin(8, "destroyCommonInfo");
                int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 2, commonInfo, null);
                Trace.traceEnd(8);
                if (ret == 0) {
                    commonInfo.mCommonHandle = 0;
                    return;
                }
                DElog.e(TAG, "destroyCommonInfo() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("destroyCommonInfo failed ret=" + ret);
            }
            DElog.e(TAG, "destroyCommonInfo() algo init failed");
            throw new IllegalStateException("destroyCommonInfo algo init failed");
        }
    }

    public int getHardwareSharpnessLevel(ImageProcessor.CommonInfo commonInfo) {
        DElog.d(TAG, "getHardwareSharpnessLevel(), handle=" + commonInfo.mCommonHandle);
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
            DElog.e(TAG, "getInfoFromCommon() native_processAlgorithm error, ret = " + ret);
            throw new ArithmeticException("getInfoFromCommon failed ret=" + ret);
        }
        DElog.e(TAG, "getInfoFromCommon() algo init failed");
        throw new IllegalStateException("getInfoFromCommon algo init failed");
    }

    public ImageProcessor.ImageEngine createImageEngine(ImageProcessor.ThumbnailParam thumbnailParam) {
        return createImageEngine(new CreateImageEngineAlgoParam(thumbnailParam));
    }

    public ImageProcessor.ImageEngine createImageEngine(ImageProcessor.CreateTileProcessEngineParam createTileProcessEngineParam) {
        return createImageEngine(new CreateImageEngineAlgoParam(createTileProcessEngineParam));
    }

    private ImageProcessor.ImageEngine createImageEngine(CreateImageEngineAlgoParam param) {
        if (this.mInited) {
            DElog.d(TAG, "createImageEngineAlgo()");
            Trace.traceBegin(8, "createImageEngine");
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 4, param, null);
            Trace.traceEnd(8);
            if (ret != 0 || param.getAlgoHandle() == 0) {
                DElog.e(TAG, "createImageEngineAlgo() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("createImageEngineAlgo failed ret=" + ret);
            }
            DElog.d(TAG, "createImageEngineAlgo() handle=" + param.getAlgoHandle());
            return new ImageProcessor.ImageEngine(param.getAlgoHandle());
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

    public void processThumbnail(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.ThumbnailParam thumbnailParam) {
        DElog.i(TAG, "processThumbnail()");
        processImageEngine(new ProcessImageEngineAlgoParam(imageEngine, commonInfo, thumbnailParam));
    }

    public void processTileAlgo(ImageProcessor.ImageEngine imageEngine, ImageProcessor.CommonInfo commonInfo, ImageProcessor.TileParam tileParam) {
        DElog.d(TAG, "processTileAlgo()");
        processImageEngine(new ProcessImageEngineAlgoParam(imageEngine, commonInfo, tileParam));
    }

    public void destroyImageEngine(ImageProcessor.ImageEngine imageEngine) {
        if (imageEngine.mAlgoHandle != 0) {
            if (this.mInited) {
                DElog.d(TAG, "destroyImageEngine(), handle=" + imageEngine.mAlgoHandle);
                Trace.traceBegin(8, "destroyImageEngine");
                int ret = DisplayEngineLibraries.nativeProcessAlgorithm(0, this.mHandle, 6, imageEngine, null);
                Trace.traceEnd(8);
                if (ret == 0) {
                    imageEngine.mAlgoHandle = 0;
                    return;
                }
                DElog.e(TAG, "destroyImageEngine() native_processAlgorithm error, ret = " + ret);
                throw new ArithmeticException("destroyImageEngine failed ret=" + ret);
            }
            DElog.e(TAG, "destroyImageEngine() algo init failed");
            throw new IllegalStateException("destroyImageEngine algo init failed");
        }
    }
}
