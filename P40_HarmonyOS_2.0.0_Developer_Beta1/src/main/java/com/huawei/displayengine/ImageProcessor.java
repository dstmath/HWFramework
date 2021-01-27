package com.huawei.displayengine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Size;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/* access modifiers changed from: package-private */
public class ImageProcessor {
    private static final String CMD_CREATE_TILE_PROCESS_ENGINE = "createTileProcessEngine";
    private static final String CMD_DESTROY_TILE_PROCESS_ENGINE = "destroyTileProcessEngine";
    private static final String CMD_GET_SUPPORT_CMD = "getSupportCmd";
    private static final String CMD_GET_WIDE_COLOR_GAMUT_SUPPORTED = "getWideColorGamutSupported";
    private static final String CMD_PROCESS_THUMBNAIL = "processThumbnail";
    private static final String CMD_PROCESS_TILE = "processTile";
    private static final String CMD_TRANSFORM_COLORSPACE = "transformColorspace";
    private static final int COMMON_INFO_CACHE_MAX_SIZE = 14;
    private static final int DEFAULT_HARDWARE_SHARPNESS_LEVEL = 0;
    private static final Set<String> IMAGE_DESCRIPTION_BEAUTY = new HashSet<String>(12) {
        /* class com.huawei.displayengine.ImageProcessor.AnonymousClass1 */

        {
            add("fbt");
            add("fptbty");
            add("fairlight");
            add("fbtmdn");
            add("fbthdr");
            add("rbt");
            add("rpt");
            add("rbtozCO");
            add("btrozCO");
            add("rbtmdn");
            add("btrmdn");
            add("rbthdr");
        }
    };
    private static final int IMAGE_DESCRIPTION_BEAUTY_INDEX = 0;
    private static final Set<String> IMAGE_DESCRIPTION_VIVID = new HashSet<String>(5) {
        /* class com.huawei.displayengine.ImageProcessor.AnonymousClass2 */

        {
            add("vivid");
            add("HDRBFace");
            add("HDRB");
            add("BFace");
            add("Bright");
        }
    };
    private static final int IMAGE_DESCRIPTION_VIVID_INDEX = 1;
    private static final int IMAGE_ENGINE_NUM = 4;
    private static final String TAG = "DE J ImageProcessor";
    private static final int UN_INIT_HARDWARE_SHARPNESS_LEVEL = -1;
    private static ImageProcessorAlgoStrategy sAlgoStrategy;
    private final ImageProcessorAlgoImpl mAlgo;
    private Map<String, CommonInfo> mCommonInfoCache;
    private String mCurrentFilePath;
    private int mHardwareSharpnessLevel = -1;
    private final boolean mIsEnable;
    private final IDisplayEngineServiceEx mService;
    private Map<Long, ImageEngine> mTileProcessEngineCache;

    /* access modifiers changed from: package-private */
    public enum ImageType {
        NORMAL,
        WIDE_COLOR_SPACE,
        SKIN_BEAUTY,
        VIVID;
        
        static final int TYPE_NUM = 4;
    }

    /* access modifiers changed from: package-private */
    public enum ThumbnailType {
        DEFAULT,
        MICRO,
        FAST,
        ANIMATION,
        FULLSCREEN,
        HALFSCREEN,
        GIF;
        
        static final int TYPE_NUM = 7;
    }

    /* access modifiers changed from: package-private */
    public enum TileEngineType {
        SR,
        NON_SR;
        
        static final int TYPE_NUM = 2;
    }

    ImageProcessor(IDisplayEngineServiceEx service) {
        DeLog.i(TAG, "ImageProcessor enter");
        sAlgoStrategy = InstanceHolder.sInstance;
        this.mService = service;
        this.mAlgo = new ImageProcessorAlgoImpl(service);
        this.mIsEnable = sAlgoStrategy.isImageProcessorEnable() && this.mAlgo.isAlgoInitSuccess();
        this.mCommonInfoCache = Collections.synchronizedMap(new LinkedHashMap<String, CommonInfo>(16, 0.75f, true) {
            /* class com.huawei.displayengine.ImageProcessor.AnonymousClass3 */

            /* access modifiers changed from: protected */
            @Override // java.util.LinkedHashMap
            public boolean removeEldestEntry(Map.Entry<String, CommonInfo> entry) {
                if (size() > 14) {
                    return true;
                }
                return false;
            }
        });
        this.mTileProcessEngineCache = Collections.synchronizedMap(new HashMap(4));
        DeLog.i(TAG, "ImageProcessor exit");
    }

    static boolean isCommandOwner(String command) {
        if (command == null) {
            return false;
        }
        char c = 65535;
        switch (command.hashCode()) {
            case -1791436785:
                if (command.equals(CMD_TRANSFORM_COLORSPACE)) {
                    c = 6;
                    break;
                }
                break;
            case -911672607:
                if (command.equals(CMD_GET_SUPPORT_CMD)) {
                    c = 0;
                    break;
                }
                break;
            case 202511805:
                if (command.equals(CMD_PROCESS_TILE)) {
                    c = 4;
                    break;
                }
                break;
            case 802499670:
                if (command.equals(CMD_GET_WIDE_COLOR_GAMUT_SUPPORTED)) {
                    c = 1;
                    break;
                }
                break;
            case 1324162877:
                if (command.equals(CMD_PROCESS_THUMBNAIL)) {
                    c = 2;
                    break;
                }
                break;
            case 2079953895:
                if (command.equals(CMD_CREATE_TILE_PROCESS_ENGINE)) {
                    c = 3;
                    break;
                }
                break;
            case 2101719209:
                if (command.equals(CMD_DESTROY_TILE_PROCESS_ENGINE)) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    static boolean isSceneSensitive(int scene, int action) {
        if (scene != 3) {
            return false;
        }
        if (action == 9 || action == 10 || action == 13) {
            return true;
        }
        return false;
    }

    private static class InstanceHolder {
        static ImageProcessorAlgoStrategy sInstance = new ImageProcessorAlgoStrategy();

        private InstanceHolder() {
        }
    }

    /* access modifiers changed from: package-private */
    public Object imageProcess(String command, Map<String, Object> param) {
        if (command == null) {
            return null;
        }
        DeLog.d(TAG, "imageProcess() command=" + command);
        if (command.equals(CMD_GET_SUPPORT_CMD)) {
            return getSupportCmd();
        }
        if (!this.mIsEnable) {
            DeLog.e(TAG, "imageProcess() is disable, command=" + command);
            return null;
        }
        char c = 65535;
        switch (command.hashCode()) {
            case -1791436785:
                if (command.equals(CMD_TRANSFORM_COLORSPACE)) {
                    c = 5;
                    break;
                }
                break;
            case 202511805:
                if (command.equals(CMD_PROCESS_TILE)) {
                    c = 3;
                    break;
                }
                break;
            case 802499670:
                if (command.equals(CMD_GET_WIDE_COLOR_GAMUT_SUPPORTED)) {
                    c = 0;
                    break;
                }
                break;
            case 1324162877:
                if (command.equals(CMD_PROCESS_THUMBNAIL)) {
                    c = 1;
                    break;
                }
                break;
            case 2079953895:
                if (command.equals(CMD_CREATE_TILE_PROCESS_ENGINE)) {
                    c = 2;
                    break;
                }
                break;
            case 2101719209:
                if (command.equals(CMD_DESTROY_TILE_PROCESS_ENGINE)) {
                    c = 4;
                    break;
                }
                break;
        }
        if (c == 0) {
            return Boolean.valueOf(getWideColorGamutSupported());
        }
        if (c == 1) {
            processThumbnail(param);
        } else if (c == 2) {
            return Long.valueOf(createTileProcessEngine(param));
        } else {
            if (c == 3) {
                processTile(param);
            } else if (c == 4) {
                destroyTileProcessEngine(param);
            } else if (c != 5) {
                DeLog.e(TAG, "imageProcess() error! undefine command=" + command);
            } else {
                transformColorSpaceToSrgb(param);
            }
        }
        return null;
    }

    private List<String> getSupportCmd() {
        List<String> supportedCmd = new ArrayList<>();
        if (this.mIsEnable) {
            supportedCmd.add(CMD_GET_SUPPORT_CMD);
            supportedCmd.add(CMD_GET_WIDE_COLOR_GAMUT_SUPPORTED);
            supportedCmd.add(CMD_PROCESS_THUMBNAIL);
            supportedCmd.add(CMD_CREATE_TILE_PROCESS_ENGINE);
            supportedCmd.add(CMD_PROCESS_TILE);
            supportedCmd.add(CMD_DESTROY_TILE_PROCESS_ENGINE);
            if (sAlgoStrategy.needNormallizeColorSpace() && sAlgoStrategy.getNormallizeColorGamut() != ColorSpaceType.SRGB) {
                supportedCmd.add(CMD_TRANSFORM_COLORSPACE);
            }
        } else {
            supportedCmd.add(CMD_GET_SUPPORT_CMD);
        }
        return supportedCmd;
    }

    private boolean getWideColorGamutSupported() {
        return this.mIsEnable && (sAlgoStrategy.needNormallizeColorSpace() || sAlgoStrategy.isAlgoWideColorSpaceEnable());
    }

    /* access modifiers changed from: package-private */
    public enum ColorSpaceType {
        SRGB(ColorSpace.get(ColorSpace.Named.SRGB)),
        ADOBE_RGB(ColorSpace.get(ColorSpace.Named.ADOBE_RGB)),
        DISPLAY_P3(ColorSpace.get(ColorSpace.Named.DISPLAY_P3)),
        SUPER_GAMUT(null);
        
        private static final int TYPE_NUM = 4;
        private static final Map<Integer, ColorSpaceType> sColorSpaceIdToEnum = new HashMap(4);
        private ColorSpace mColorSpace;

        static {
            ColorSpaceType[] values = values();
            for (ColorSpaceType type : values) {
                ColorSpace colorSpace = type.mColorSpace;
                if (colorSpace != null) {
                    sColorSpaceIdToEnum.put(Integer.valueOf(colorSpace.getId()), type);
                }
            }
        }

        private ColorSpaceType(ColorSpace colorSpace) {
            this.mColorSpace = colorSpace;
        }

        static ColorSpaceType getEnum(ColorSpace colorSpace) {
            if (colorSpace == null) {
                return null;
            }
            return sColorSpaceIdToEnum.get(Integer.valueOf(colorSpace.getId()));
        }
    }

    /* access modifiers changed from: package-private */
    public enum AlgoType {
        ACE(1),
        SR(2),
        SHARPNESS(4),
        GMP(8),
        ACM(16),
        LUT3D(32);
        
        private int mId;

        private AlgoType(int id) {
            this.mId = id;
        }

        static int getType(Set<AlgoType> types) {
            int id = 0;
            for (AlgoType type : types) {
                id |= type.mId;
            }
            return id;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ColorSpaceParam {
        private static final String PARAM_IN_BITMAP = "inBitmap";
        private static final String PARAM_OUT_BITMAP = "outBitmap";
        Bitmap mInBitmap;
        ColorSpaceType mInColorSpace;
        Bitmap mOutBitmap;
        ColorSpaceType mOutColorSpace;

        ColorSpaceParam(Bitmap inBitmap, Bitmap outBitmap, ColorSpaceType inColorSpace, ColorSpaceType outColorSpace) {
            this.mInBitmap = inBitmap;
            this.mOutBitmap = outBitmap;
            this.mInColorSpace = inColorSpace;
            this.mOutColorSpace = outColorSpace;
        }

        ColorSpaceParam(Map<String, Object> param, ColorSpaceType inColorSpace, ColorSpaceType outColorSpace) {
            ColorSpaceType colorSpaceType;
            this.mInBitmap = (Bitmap) param.get(PARAM_IN_BITMAP);
            this.mOutBitmap = (Bitmap) param.get(PARAM_OUT_BITMAP);
            if (!isParamInvalid()) {
                this.mInColorSpace = inColorSpace != null ? inColorSpace : ColorSpaceType.getEnum(this.mInBitmap.getColorSpace());
                if (this.mInColorSpace == null) {
                    this.mInColorSpace = ColorSpaceType.SRGB;
                    DeLog.w(ImageProcessor.TAG, "ColorSpaceParam() error! unsupport inColorSpace = " + this.mInBitmap.getColorSpace() + ", treat as SRGB");
                }
                if (outColorSpace != null) {
                    colorSpaceType = outColorSpace;
                } else {
                    colorSpaceType = ImageProcessor.sAlgoStrategy.needNormallizeColorSpace() ? ImageProcessor.sAlgoStrategy.getNormallizeColorGamut() : this.mInColorSpace;
                }
                this.mOutColorSpace = colorSpaceType;
                return;
            }
            throw new IllegalArgumentException("ColorSpaceParam input param invalid");
        }

        ColorSpaceParam(Map<String, Object> param) {
            this(param, null, null);
        }

        private boolean isParamInvalid() {
            Bitmap bitmap = this.mInBitmap;
            if (bitmap == null) {
                DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mInBitmap is null");
                return true;
            }
            Bitmap.Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == Bitmap.Config.ARGB_8888 || bitmapConfig == Bitmap.Config.RGB_565) {
                Bitmap bitmap2 = this.mOutBitmap;
                if (bitmap2 == null) {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mOutBitmap is null");
                    return true;
                }
                Bitmap.Config bitmapConfig2 = bitmap2.getConfig();
                if (bitmapConfig2 == Bitmap.Config.ARGB_8888 || bitmapConfig2 == Bitmap.Config.RGB_565) {
                    return false;
                }
                DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! unsupported mOutBitmap format " + bitmapConfig2);
                return true;
            }
            DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! unsupported mInBitmap format " + bitmapConfig);
            return true;
        }
    }

    private void transformColorSpaceToSrgb(Map<String, Object> param) {
        DeLog.i(TAG, "transformColorSpaceToSrgb() begin");
        if (!sAlgoStrategy.isImageProcessorEnable()) {
            throw new UnsupportedOperationException("image process is disabled");
        } else if (sAlgoStrategy.needNormallizeColorSpace()) {
            ColorSpaceParam colorSpaceParam = new ColorSpaceParam(param, sAlgoStrategy.getNormallizeColorGamut(), ColorSpaceType.SRGB);
            if (isGmpInImageMode()) {
                Optional<BitmapConfigTransformer> transformer = BitmapConfigTransformer.create(colorSpaceParam);
                transformer.ifPresent($$Lambda$ImageProcessor$59YsElCxg154HU41yvTk9RTfNFE.INSTANCE);
                this.mAlgo.transformColorSpace(colorSpaceParam);
                transformer.ifPresent($$Lambda$ImageProcessor$nMk7Ggkw9f5A_8qTrVoIVwGQoOE.INSTANCE);
                DeLog.i(TAG, "transformColorSpaceToSrgb() trans end");
            } else if (colorSpaceParam.mInBitmap != colorSpaceParam.mOutBitmap) {
                copyPixels(colorSpaceParam.mInBitmap, colorSpaceParam.mOutBitmap);
                DeLog.i(TAG, "transformColorSpaceToSrgb() copy end");
            } else {
                DeLog.i(TAG, "transformColorSpaceToSrgb() bypass end");
            }
        } else {
            throw new UnsupportedOperationException("transform colorSpace is disabled");
        }
    }

    private boolean isGmpInImageMode() {
        IDisplayEngineServiceEx iDisplayEngineServiceEx = this.mService;
        if (iDisplayEngineServiceEx == null) {
            DeLog.e(TAG, "isGmpInImageMode() mService is null!");
            return false;
        }
        byte[] isImage = new byte[1];
        try {
            int ret = iDisplayEngineServiceEx.getEffect(3, 4, isImage, isImage.length);
            if (ret != 0) {
                DeLog.e(TAG, "isGmpInImageMode() getEffect failed, return " + ret);
                return false;
            } else if (isImage[0] != 0) {
                return true;
            } else {
                return false;
            }
        } catch (RemoteException e) {
            DeLog.e(TAG, "isGmpInImageMode() RemoteException " + e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class BitmapConfigTransformer {
        private final ColorSpaceParam mColorSpaceParam;
        private final Bitmap mInOriginalBitmap = this.mColorSpaceParam.mInBitmap;
        private Bitmap mInTransBitmap;
        private final Bitmap mOutOriginalBitmap = this.mColorSpaceParam.mOutBitmap;
        private Bitmap mOutTransBitmap;

        private BitmapConfigTransformer(ColorSpaceParam param) {
            this.mColorSpaceParam = param;
            if (this.mInOriginalBitmap == this.mOutOriginalBitmap) {
                DeLog.i(ImageProcessor.TAG, "BitmapConfigTransformer() in==out=" + this.mInOriginalBitmap.getConfig());
                return;
            }
            DeLog.i(ImageProcessor.TAG, "BitmapConfigTransformer() in=" + this.mInOriginalBitmap.getConfig() + ", out=" + this.mOutOriginalBitmap.getConfig());
        }

        static Optional<BitmapConfigTransformer> create(ColorSpaceParam param) {
            if (param.mInBitmap.getConfig() == Bitmap.Config.ARGB_8888 && param.mOutBitmap.getConfig() == Bitmap.Config.ARGB_8888) {
                return Optional.empty();
            }
            return Optional.of(new BitmapConfigTransformer(param));
        }

        /* access modifiers changed from: package-private */
        public void doPreTransform() {
            DeLog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform()");
            if (this.mInOriginalBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                this.mInTransBitmap = this.mInOriginalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                Bitmap bitmap = this.mInTransBitmap;
                if (bitmap != null) {
                    ColorSpaceParam colorSpaceParam = this.mColorSpaceParam;
                    colorSpaceParam.mInBitmap = bitmap;
                    if (this.mInOriginalBitmap == this.mOutOriginalBitmap) {
                        this.mOutTransBitmap = bitmap;
                        colorSpaceParam.mOutBitmap = this.mOutTransBitmap;
                        DeLog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform() done");
                        return;
                    }
                } else {
                    DeLog.e(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform() error! can't copy in bitmap");
                    throw new IllegalArgumentException("doPreTransform can't copy in bitmap");
                }
            }
            if (this.mOutOriginalBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                ColorSpace colorSpace = this.mOutOriginalBitmap.getColorSpace();
                this.mOutTransBitmap = Bitmap.createBitmap(this.mOutOriginalBitmap.getWidth(), this.mOutOriginalBitmap.getHeight(), Bitmap.Config.ARGB_8888, true, colorSpace != null ? colorSpace : ColorSpace.get(ColorSpace.Named.SRGB));
                Bitmap bitmap2 = this.mOutTransBitmap;
                if (bitmap2 != null) {
                    this.mColorSpaceParam.mOutBitmap = bitmap2;
                } else {
                    DeLog.e(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform() error! can't create out bitmap");
                    throw new IllegalArgumentException("doPreTransform can't create out bitmap");
                }
            }
            DeLog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform() done");
        }

        /* access modifiers changed from: package-private */
        public void doPostTransform() {
            if (this.mOutTransBitmap != null) {
                DeLog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPostTransform()");
                new Canvas(this.mOutOriginalBitmap).drawBitmap(this.mOutTransBitmap, 0.0f, 0.0f, (Paint) null);
                DeLog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPostTransform() done");
            }
        }
    }

    private void transformColorSpaceOnBitmap(ColorSpaceParam colorSpaceParam, Bitmap inBitmap, Bitmap outBitmap) {
        if (sAlgoStrategy.needNormallizeColorSpace() && colorSpaceParam.mInColorSpace != colorSpaceParam.mOutColorSpace) {
            this.mAlgo.transformColorSpace(new ColorSpaceParam(inBitmap, outBitmap, colorSpaceParam.mInColorSpace, colorSpaceParam.mOutColorSpace));
        }
    }

    /* access modifiers changed from: private */
    public static boolean isImageDescriptionBeauty(String imageDescription) {
        if (TextUtils.isEmpty(imageDescription)) {
            return false;
        }
        return IMAGE_DESCRIPTION_BEAUTY.contains(imageDescription.split("_")[0]);
    }

    /* access modifiers changed from: private */
    public static boolean isImageDescriptionVivid(String imageDescription) {
        if (TextUtils.isEmpty(imageDescription)) {
            return false;
        }
        String[] split = imageDescription.split("_");
        if (split.length < 2) {
            return false;
        }
        return IMAGE_DESCRIPTION_VIVID.contains(split[1]);
    }

    /* access modifiers changed from: package-private */
    public static class ThumbnailParam extends ColorSpaceParam {
        private static final String PARAM_FILE_PATH = "filePath";
        private static final String PARAM_IMAGE_DESCRIPTION = "imageDescription";
        private static final String PARAM_ISO = "iso";
        private static final String PARAM_SCALE_RATIO = "scaleRatio";
        private static final String PARAM_SKIN_BEAUTY = "skinBeauty";
        private static final String PARAM_THUMBNAIL_TYPE = "thumbnailType";
        private static final float SCALE_RATIO_MAX = 2.0f;
        Set<AlgoType> mAlgos;
        Set<AlgoType> mCommonAlgos;
        String mFilePath;
        String mImageDescription;
        ImageType mImageType;
        boolean mIsSkinBeauty;
        int mIso;
        float mScaleRatio = 1.0f;
        ThumbnailType mType = ThumbnailType.DEFAULT;

        ThumbnailParam(Map<String, Object> param) {
            super(param);
            this.mFilePath = (String) param.get(PARAM_FILE_PATH);
            if (param.containsKey(PARAM_THUMBNAIL_TYPE)) {
                this.mType = ThumbnailType.valueOf((String) param.get(PARAM_THUMBNAIL_TYPE));
            }
            if (param.containsKey(PARAM_SCALE_RATIO)) {
                this.mScaleRatio = ((Float) param.get(PARAM_SCALE_RATIO)).floatValue();
            }
            if (param.containsKey(PARAM_ISO)) {
                this.mIso = ((Integer) param.get(PARAM_ISO)).intValue();
            }
            if (param.containsKey(PARAM_SKIN_BEAUTY)) {
                this.mIsSkinBeauty = ((Boolean) param.get(PARAM_SKIN_BEAUTY)).booleanValue();
            }
            boolean isVivid = false;
            if (param.containsKey(PARAM_IMAGE_DESCRIPTION)) {
                this.mImageDescription = (String) param.get(PARAM_IMAGE_DESCRIPTION);
                this.mIsSkinBeauty = ImageProcessor.isImageDescriptionBeauty(this.mImageDescription);
                isVivid = ImageProcessor.isImageDescriptionVivid(this.mImageDescription);
            }
            if (this.mIsSkinBeauty && ImageProcessor.sAlgoStrategy.isAlgoSkinBeautyEnable()) {
                this.mImageType = ImageType.SKIN_BEAUTY;
            } else if (isVivid && ImageProcessor.sAlgoStrategy.isAlgoVividEnable()) {
                this.mImageType = ImageType.VIVID;
            } else if (this.mInColorSpace == ColorSpaceType.SRGB || !ImageProcessor.sAlgoStrategy.isAlgoWideColorSpaceEnable()) {
                this.mImageType = ImageType.NORMAL;
            } else {
                this.mImageType = ImageType.WIDE_COLOR_SPACE;
            }
            this.mAlgos = ImageProcessor.sAlgoStrategy.getThumbnailAlgos(this.mType, this.mImageType);
            this.mCommonAlgos = ImageProcessor.sAlgoStrategy.getCommonInfoAlgos(this.mType, this.mImageType);
            if (isParamInvalid()) {
                throw new IllegalArgumentException("processThumbnail input param invalid");
            }
        }

        private boolean isParamInvalid() {
            if (this.mFilePath.isEmpty()) {
                DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mFilePath is empty");
                return true;
            } else if (this.mType == null) {
                DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mType is empty");
                return true;
            } else {
                float f = this.mScaleRatio;
                if (f <= 0.0f || f > 2.0f) {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mScaleRatio=" + this.mScaleRatio + " out of range");
                    return true;
                } else if (this.mIso >= 0) {
                    return false;
                } else {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mIso=" + this.mIso + " out of range");
                    return true;
                }
            }
        }
    }

    private void processThumbnail(Map<String, Object> param) {
        DeLog.d(TAG, "processThumbnail()");
        if (sAlgoStrategy.isImageProcessorEnable()) {
            ThumbnailParam thumbnailParam = new ThumbnailParam(param);
            Optional<BitmapConfigTransformer> transformer = BitmapConfigTransformer.create(thumbnailParam);
            transformer.ifPresent($$Lambda$ImageProcessor$6HZAh5rBsUD6oJbozEMg9kdIL8.INSTANCE);
            processThumbnail(thumbnailParam);
            transformer.ifPresent($$Lambda$ImageProcessor$3vTyVjAiRD3BTJyMvWdvKWyEYU.INSTANCE);
            return;
        }
        throw new UnsupportedOperationException("image process is disabled");
    }

    private void processThumbnail(ThumbnailParam thumbnailParam) {
        boolean isProcessDone = false;
        CommonInfo commonInfo = null;
        ImageEngine imageEngine = null;
        try {
            if (sAlgoStrategy.needRunSoftwareAlgo(thumbnailParam.mType, thumbnailParam.mImageType)) {
                commonInfo = getCommonInfo(thumbnailParam.mFilePath);
                if (commonInfo == null) {
                    commonInfo = new CommonInfo(this.mAlgo.createCommonInfo(thumbnailParam), this.mAlgo);
                    if (sAlgoStrategy.needSaveCommonInfo(thumbnailParam.mType)) {
                        saveCommonInfo(thumbnailParam.mFilePath, commonInfo);
                    }
                }
                if (!thumbnailParam.mAlgos.isEmpty()) {
                    imageEngine = this.mAlgo.createImageEngine(thumbnailParam);
                    this.mAlgo.processThumbnail(imageEngine, commonInfo, thumbnailParam);
                    isProcessDone = true;
                }
            }
            if (!isProcessDone) {
                Bitmap bitmap = thumbnailParam.mInBitmap;
            }
        } finally {
            transformColorSpaceOnBitmap(thumbnailParam, isProcessDone ? thumbnailParam.mOutBitmap : thumbnailParam.mInBitmap, thumbnailParam.mOutBitmap);
            if (!isProcessDone) {
                copyPixelsToOutBitmapIfNeeded(thumbnailParam);
            }
            collectInfoForImageRecognization(thumbnailParam, commonInfo);
            if (imageEngine != null) {
                this.mAlgo.destroyImageEngine(imageEngine);
            }
        }
    }

    private static void copyPixels(Bitmap in, Bitmap out) {
        if (in != null && out != null && in != out) {
            ByteBuffer buffer = ByteBuffer.allocate(in.getByteCount());
            in.copyPixelsToBuffer(buffer);
            buffer.rewind();
            out.copyPixelsFromBuffer(buffer);
            DeLog.i(TAG, "copyPixels() done");
        }
    }

    private void copyPixelsToOutBitmapIfNeeded(ColorSpaceParam thumbnailParam) {
        if (thumbnailParam.mInBitmap != thumbnailParam.mOutBitmap) {
            if (!sAlgoStrategy.needNormallizeColorSpace() || thumbnailParam.mInColorSpace == thumbnailParam.mOutColorSpace) {
                copyPixels(thumbnailParam.mInBitmap, thumbnailParam.mOutBitmap);
            }
        }
    }

    private void collectInfoForImageRecognization(String filePath, String imageDescription, CommonInfo commonInfo) {
        int hardwareSharpnessLevel = 0;
        String str = this.mCurrentFilePath;
        if (str == null || !str.equals(filePath)) {
            this.mCurrentFilePath = filePath;
            this.mHardwareSharpnessLevel = -1;
            if (commonInfo != null) {
                hardwareSharpnessLevel = this.mAlgo.getHardwareSharpnessLevel(commonInfo);
                this.mHardwareSharpnessLevel = hardwareSharpnessLevel;
            }
            sendInfoToImageRecognization(this.mCurrentFilePath, imageDescription, hardwareSharpnessLevel);
        } else if (this.mHardwareSharpnessLevel == -1 && commonInfo != null) {
            int hardwareSharpnessLevel2 = this.mAlgo.getHardwareSharpnessLevel(commonInfo);
            this.mHardwareSharpnessLevel = hardwareSharpnessLevel2;
            if (hardwareSharpnessLevel2 != 0) {
                sendInfoToImageRecognization(this.mCurrentFilePath, imageDescription, hardwareSharpnessLevel2);
            }
        }
    }

    private void collectInfoForImageRecognization(ThumbnailParam thumbnailParam, CommonInfo commonInfo) {
        if (thumbnailParam.mType == ThumbnailType.FAST || thumbnailParam.mType == ThumbnailType.ANIMATION || thumbnailParam.mType == ThumbnailType.FULLSCREEN) {
            collectInfoForImageRecognization(thumbnailParam.mFilePath, thumbnailParam.mImageDescription, commonInfo);
        }
    }

    private void collectInfoForImageRecognization(TileParam tileParam, CommonInfo commonInfo) {
        collectInfoForImageRecognization(tileParam.mFilePath, tileParam.mImageDescription, commonInfo);
    }

    private void sendInfoToImageRecognization(String filePath, String imageDescription, int hardwareSharpnessLevel) {
        if (this.mService == null) {
            DeLog.e(TAG, "sendInfoToImageRecognization() mService is null!");
            return;
        }
        DeLog.i(TAG, "sendInfoToImageRecognization, imageDescription=" + imageDescription + ", hardwareSharpnessLevel=" + hardwareSharpnessLevel);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("filePath", filePath);
        if (imageDescription != null) {
            bundle.putString("imageDescription", imageDescription);
        }
        bundle.putInt("hardwareSharpnessLevel", hardwareSharpnessLevel);
        try {
            this.mService.setData(4, bundle);
        } catch (RemoteException e) {
            DeLog.e(TAG, "sendInfoToImageRecognization setData error! filePath=" + filePath + ", hardwareSharpnessLevel=" + hardwareSharpnessLevel + ", " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public class CommonInfo {
        private ImageProcessorAlgoImpl mAlgo;
        long mCommonHandle;

        CommonInfo(long handle, ImageProcessorAlgoImpl algo) {
            this.mCommonHandle = handle;
            this.mAlgo = algo;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            if (this.mCommonHandle != 0) {
                DeLog.i(ImageProcessor.TAG, "CommonInfo finalize() mCommonHandle=" + this.mCommonHandle);
                ImageProcessorAlgoImpl imageProcessorAlgoImpl = this.mAlgo;
                if (imageProcessorAlgoImpl != null) {
                    imageProcessorAlgoImpl.destroyCommonInfo(this);
                }
            }
        }
    }

    private void saveCommonInfo(String hashId, CommonInfo commonInfo) {
        DeLog.d(TAG, "CommonInfoCache save " + hashId + ", commonHandle=" + commonInfo.mCommonHandle);
        this.mCommonInfoCache.put(hashId, commonInfo);
    }

    private void clearCommonInfo() {
        if (!this.mCommonInfoCache.isEmpty()) {
            DeLog.i(TAG, "CommonInfoCache clear size=" + this.mCommonInfoCache.size());
            this.mCommonInfoCache.clear();
        }
    }

    private CommonInfo getCommonInfo(String hashId) {
        CommonInfo commonInfo = this.mCommonInfoCache.get(hashId);
        if (commonInfo == null || commonInfo.mCommonHandle != 0) {
            return commonInfo;
        }
        DeLog.e(TAG, "getCommonInfo() error! hashId " + hashId + " commonHandle is 0");
        throw new IllegalStateException("getCommonInfo() error! hashId " + hashId + " commonHandle is 0");
    }

    /* access modifiers changed from: package-private */
    public static class ImageEngine {
        long mAlgoHandle;

        ImageEngine(long handle) {
            this.mAlgoHandle = handle;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            if (this.mAlgoHandle != 0) {
                DeLog.e(ImageProcessor.TAG, "ImageEngine finalize() error! haven't destroyed yet, mAlgoHandle=" + this.mAlgoHandle);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class CreateTileProcessEngineParam {
        private static final String PARAM_ENGINE_TYPE = "engineType";
        private static final String PARAM_TILE_BORDER = "tileBorder";
        private static final String PARAM_TILE_SIZE = "tileSize";
        Set<AlgoType> mAlgos = ImageProcessor.sAlgoStrategy.getTileEngineAlgos(this.mEngineType);
        TileEngineType mEngineType;
        int mTileBorder;
        int mTileSize;

        CreateTileProcessEngineParam(Map<String, Object> param) {
            this.mEngineType = TileEngineType.valueOf((String) param.get(PARAM_ENGINE_TYPE));
            this.mTileSize = ((Integer) param.get(PARAM_TILE_SIZE)).intValue();
            this.mTileBorder = ((Integer) param.get(PARAM_TILE_BORDER)).intValue();
        }
    }

    private long createTileProcessEngine(Map<String, Object> param) {
        DeLog.i(TAG, "createTileProcessEngine()");
        if (sAlgoStrategy.isImageProcessorEnable()) {
            CreateTileProcessEngineParam createTileProcessEngineParam = new CreateTileProcessEngineParam(param);
            if (createTileProcessEngineParam.mAlgos.isEmpty()) {
                return 0;
            }
            ImageEngine imageEngine = this.mAlgo.createImageEngine(createTileProcessEngineParam);
            saveImageEngine(imageEngine);
            return imageEngine.mAlgoHandle;
        }
        throw new UnsupportedOperationException("image process is disabled");
    }

    /* access modifiers changed from: package-private */
    public static class TileParam extends ColorSpaceParam {
        private static final String PARAM_DECODED_SIZE = "decodedSize";
        private static final String PARAM_DECODED_START_POINT = "decodedStartPoint";
        private static final String PARAM_ENGINE = "engine";
        private static final String PARAM_ENGINE_TYPE = "engineType";
        private static final String PARAM_FILE_PATH = "filePath";
        private static final String PARAM_IMAGE_DESCRIPTION = "imageDescription";
        private static final String PARAM_IN_VISIBLE_RANGE = "inVisibleRange";
        private static final String PARAM_SCALED_START_POINT = "scaledStartPoint";
        private static final String PARAM_SCALE_RATIO = "scaleRatio";
        private static final String PARAM_SKIN_BEAUTY = "skinBeauty";
        private static final String PARAM_ZOOM_IN_RATIO = "zoomInRatio";
        private static final float SCALE_RATIO_MAX = 2.0f;
        long mAlgoHandle;
        Size mDecodedSize;
        Point mDecodedStartPoint;
        TileEngineType mEngineType;
        String mFilePath;
        String mImageDescription;
        ImageType mImageType;
        Rect mInVisibleRange;
        float mScaleRatio = 1.0f;
        PointF mScaledStartPoint;
        float mZoomInRatio = 1.0f;

        TileParam(Map<String, Object> param) {
            super(param);
            this.mEngineType = TileEngineType.valueOf((String) param.get(PARAM_ENGINE_TYPE));
            this.mAlgoHandle = ((Long) param.get(PARAM_ENGINE)).longValue();
            this.mFilePath = (String) param.get(PARAM_FILE_PATH);
            if (this.mEngineType == TileEngineType.SR) {
                this.mScaleRatio = ((Float) param.get(PARAM_SCALE_RATIO)).floatValue();
                this.mZoomInRatio = ((Float) param.get(PARAM_ZOOM_IN_RATIO)).floatValue();
                this.mScaledStartPoint = (PointF) param.get(PARAM_SCALED_START_POINT);
                if (param.containsKey(PARAM_IN_VISIBLE_RANGE)) {
                    this.mInVisibleRange = (Rect) param.get(PARAM_IN_VISIBLE_RANGE);
                } else {
                    this.mInVisibleRange = new Rect(0, 0, this.mInBitmap.getWidth() - 1, this.mInBitmap.getHeight() - 1);
                }
            } else {
                this.mDecodedSize = (Size) param.get(PARAM_DECODED_SIZE);
                this.mDecodedStartPoint = (Point) param.get(PARAM_DECODED_START_POINT);
            }
            boolean isSkinBeauty = param.containsKey(PARAM_SKIN_BEAUTY) ? ((Boolean) param.get(PARAM_SKIN_BEAUTY)).booleanValue() : false;
            boolean isVivid = false;
            if (param.containsKey(PARAM_IMAGE_DESCRIPTION)) {
                this.mImageDescription = (String) param.get(PARAM_IMAGE_DESCRIPTION);
                isSkinBeauty = ImageProcessor.isImageDescriptionBeauty(this.mImageDescription);
                isVivid = ImageProcessor.isImageDescriptionVivid(this.mImageDescription);
            }
            if (isSkinBeauty && ImageProcessor.sAlgoStrategy.isAlgoSkinBeautyEnable()) {
                this.mImageType = ImageType.SKIN_BEAUTY;
            } else if (isVivid && ImageProcessor.sAlgoStrategy.isAlgoVividEnable()) {
                this.mImageType = ImageType.VIVID;
            } else if (this.mInColorSpace == ColorSpaceType.SRGB || !ImageProcessor.sAlgoStrategy.isAlgoWideColorSpaceEnable()) {
                this.mImageType = ImageType.NORMAL;
            } else {
                this.mImageType = ImageType.WIDE_COLOR_SPACE;
            }
            if (isParamInvalid()) {
                throw new IllegalArgumentException("processTile input param invalid");
            }
        }

        private boolean isParamInvalid() {
            if (this.mEngineType == null) {
                DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mEngineType is null");
                return true;
            } else if (this.mFilePath.isEmpty()) {
                DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mFilePath is empty");
                return true;
            } else if (this.mEngineType == TileEngineType.SR) {
                float f = this.mScaleRatio;
                if (f <= 0.0f || f > 2.0f) {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mScaleRatio=" + this.mScaleRatio + " out of range");
                    return true;
                } else if (this.mZoomInRatio <= 0.0f) {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mZoomInRatio=" + this.mZoomInRatio + " out of range");
                    return true;
                } else if (this.mScaledStartPoint == null) {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mScaledStartPoint is null");
                    return true;
                } else if (isVisibleRangeInvalid(this.mInVisibleRange)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                Size size = this.mDecodedSize;
                if (size == null) {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mDecodedSize is null");
                    return true;
                } else if (size.getWidth() <= 0 || this.mDecodedSize.getHeight() <= 0) {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mDecodedSize=" + this.mDecodedSize);
                    return true;
                } else if (this.mDecodedStartPoint != null) {
                    return false;
                } else {
                    DeLog.e(ImageProcessor.TAG, "isParamInvalid() error! mDecodedStartPoint is null");
                    return true;
                }
            }
        }

        private boolean isVisibleRangeInvalid(Rect rect) {
            if (rect == null) {
                DeLog.e(ImageProcessor.TAG, "isVisibleRangeInvalid() error! rect is null");
                return true;
            } else if (rect.left >= 0 && rect.top >= 0 && rect.left < rect.right && rect.top < rect.bottom && rect.right < this.mInBitmap.getWidth() && rect.bottom < this.mInBitmap.getHeight()) {
                return false;
            } else {
                DeLog.e(ImageProcessor.TAG, "isVisibleRangeInvalid() error! rect=" + rect);
                return true;
            }
        }
    }

    private void processTile(Map<String, Object> param) {
        DeLog.d(TAG, "processTile()");
        if (sAlgoStrategy.isImageProcessorEnable()) {
            TileParam tileParam = new TileParam(param);
            Optional<BitmapConfigTransformer> transformer = BitmapConfigTransformer.create(tileParam);
            transformer.ifPresent($$Lambda$ImageProcessor$Ec2wZUIW1UsldcUgBX7W_zG_8eQ.INSTANCE);
            processTile(tileParam);
            transformer.ifPresent($$Lambda$ImageProcessor$BsdKRMQScfwPT82u5hWhNI_OgQ.INSTANCE);
            return;
        }
        throw new UnsupportedOperationException("image process is disabled");
    }

    private void processTile(TileParam tileParam) {
        TileEngineType tileEngineType;
        TileEngineType tileEngineType2;
        boolean isProcessDone = false;
        CommonInfo commonInfo = null;
        try {
            if (sAlgoStrategy.needRunSoftwareAlgo(tileParam.mEngineType, tileParam.mImageType)) {
                commonInfo = getCommonInfo(tileParam.mFilePath);
                if (commonInfo != null) {
                    ImageEngine imageEngine = getImageEngine(tileParam.mAlgoHandle);
                    if (imageEngine != null) {
                        this.mAlgo.processTileAlgo(imageEngine, commonInfo, tileParam);
                        isProcessDone = true;
                    } else {
                        DeLog.e(TAG, "processTile() error! can't find imageEngine for " + tileParam.mAlgoHandle + ", mTileProcessEngineCache size=" + this.mTileProcessEngineCache.size());
                        throw new IllegalStateException("processTile() can't find imageEngine, Cache size=" + this.mTileProcessEngineCache.size());
                    }
                } else {
                    DeLog.e(TAG, "processTile() error! can't find commonInfo for " + tileParam.mFilePath + ", mCommonInfoCache size=" + this.mCommonInfoCache.size());
                    throw new IllegalStateException("processTile() can't find commonInfo, Cache size=" + this.mCommonInfoCache.size());
                }
            } else if (tileParam.mEngineType == TileEngineType.SR) {
                DeLog.e(TAG, "processTile() error! engineType is SR but no algo run");
                throw new IllegalStateException("processTile() error! engineType is SR but no algo run");
            }
            if (tileEngineType == tileEngineType2) {
                if (!isProcessDone) {
                    Bitmap bitmap = tileParam.mInBitmap;
                }
            }
        } finally {
            if (tileParam.mEngineType == TileEngineType.NON_SR) {
                transformColorSpaceOnBitmap(tileParam, isProcessDone ? tileParam.mOutBitmap : tileParam.mInBitmap, tileParam.mOutBitmap);
                if (!isProcessDone) {
                    copyPixelsToOutBitmapIfNeeded(tileParam);
                }
            }
            collectInfoForImageRecognization(tileParam, commonInfo);
        }
    }

    /* access modifiers changed from: package-private */
    public static class DestroyTileProcessEngineParam {
        private static final String PARAM_ENGINE = "engine";
        long mAlgoHandle;

        DestroyTileProcessEngineParam(Map<String, Object> param) {
            this.mAlgoHandle = ((Long) param.get(PARAM_ENGINE)).longValue();
        }
    }

    private void destroyTileProcessEngine(Map<String, Object> param) {
        DeLog.d(TAG, "destroyTileProcessEngine()");
        if (sAlgoStrategy.isImageProcessorEnable()) {
            DestroyTileProcessEngineParam destroyTileProcessEngineParam = new DestroyTileProcessEngineParam(param);
            if (destroyTileProcessEngineParam.mAlgoHandle != 0) {
                removeImageEngine(destroyTileProcessEngineParam.mAlgoHandle);
                return;
            }
            return;
        }
        throw new UnsupportedOperationException("image process is disabled");
    }

    private void saveImageEngine(ImageEngine imageEngine) {
        DeLog.d(TAG, "TileProcessEngineCache save " + imageEngine.mAlgoHandle);
        this.mTileProcessEngineCache.put(Long.valueOf(imageEngine.mAlgoHandle), imageEngine);
    }

    private ImageEngine getImageEngine(long algoHandle) {
        return this.mTileProcessEngineCache.get(Long.valueOf(algoHandle));
    }

    private void removeImageEngine(long algoHandle) {
        ImageEngine imageEngine = this.mTileProcessEngineCache.get(Long.valueOf(algoHandle));
        if (imageEngine == null) {
            DeLog.e(TAG, "removeImageEngine() error! can't find " + algoHandle);
            return;
        }
        this.mAlgo.destroyImageEngine(imageEngine);
        this.mTileProcessEngineCache.remove(Long.valueOf(algoHandle));
    }

    /* access modifiers changed from: package-private */
    public void setScene(int scene, int action) {
        if (action == 9) {
            DeLog.i(TAG, "setScene THUMBNAIL");
            this.mCurrentFilePath = null;
            this.mHardwareSharpnessLevel = -1;
        } else if (action == 10) {
            DeLog.i(TAG, "setScene FULLSCREEN");
        } else if (action != 13) {
            DeLog.e(TAG, "setScene unknown action = " + action);
        } else {
            DeLog.i(TAG, "setScene EXIT");
            clearCommonInfo();
        }
    }
}
