package com.huawei.displayengine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.util.Size;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ImageProcessor {
    private static final String CMD_CREATE_TILE_PROCESS_ENGINE = "createTileProcessEngine";
    private static final String CMD_DESTROY_TILE_PROCESS_ENGINE = "destroyTileProcessEngine";
    private static final String CMD_GET_SUPPORT_CMD = "getSupportCmd";
    private static final String CMD_GET_WIDE_COLOR_GAMUT_SUPPORTED = "getWideColorGamutSupported";
    private static final String CMD_PROCESS_THUMBNAIL = "processThumbnail";
    private static final String CMD_PROCESS_TILE = "processTile";
    private static final String CMD_TRANSFORM_COLORSPACE = "transformColorspace";
    private static final int COMMON_INFO_CACHE_MAX_SIZE = 14;
    private static final int DEFAULT_HARDWARE_SHARPNESS_LEVEL = 0;
    private static final String TAG = "DE J ImageProcessor";
    private static final int UNINIT_HARDWARE_SHARPNESS_LEVEL = -1;
    /* access modifiers changed from: private */
    public static ImageProcessorAlgoStrategy mAlgoStrategy;
    private static final Set<String> mImageDescriptionBeauty = new HashSet<String>() {
        {
            add("fbt");
            add("fptbty");
            add("fairlight");
            add("btf");
            add("btfmdn");
            add("fbtmdn");
            add("btfhdr");
            add("fbthdr");
            add("rbt");
            add("rpt");
            add("ptr");
            add("btr");
            add("rbtozCO");
            add("btrozCO");
            add("rbtmdn");
            add("btrmdn");
            add("rbthdr");
            add("btrhdr");
        }
    };
    private static final Set<String> mImageDescriptionVivid = new HashSet<String>() {
        {
            add("vivid");
            add("HDRBFace");
            add("HDRB");
            add("BFace");
            add("Bright");
        }
    };
    private final ImageProcessorAlgoImpl mAlgo;
    private Map<String, CommonInfo> mCommonInfoCache;
    private String mCurrentFilePath;
    private final boolean mEnable;
    private int mHardwareSharpnessLevel = -1;
    private final IDisplayEngineServiceEx mService;
    private Map<Long, ImageEngine> mTileProcessEngineCache;

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

        public static int getType(Set<AlgoType> types) {
            if (types == null) {
                return 0;
            }
            int id = 0;
            for (AlgoType type : types) {
                id |= type.mId;
            }
            return id;
        }
    }

    private static class BitmapConfigTransformer {
        private final ColorspaceParam mColorspaceParam;
        private final Bitmap mInOriginalBitmap = this.mColorspaceParam.mInBitmap;
        private Bitmap mInTransBitmap;
        private final Bitmap mOutOriginalBitmap = this.mColorspaceParam.mOutBitmap;
        private Bitmap mOutTransBitmap;

        private BitmapConfigTransformer(ColorspaceParam param) {
            this.mColorspaceParam = param;
            if (this.mInOriginalBitmap == this.mOutOriginalBitmap) {
                DElog.i(ImageProcessor.TAG, "BitmapConfigTransformer() in==out=" + this.mInOriginalBitmap.getConfig());
                return;
            }
            DElog.i(ImageProcessor.TAG, "BitmapConfigTransformer() in=" + this.mInOriginalBitmap.getConfig() + ", out=" + this.mOutOriginalBitmap.getConfig());
        }

        public static BitmapConfigTransformer create(ColorspaceParam param) {
            if (param.mInBitmap.getConfig() == Bitmap.Config.ARGB_8888 && param.mOutBitmap.getConfig() == Bitmap.Config.ARGB_8888) {
                return null;
            }
            return new BitmapConfigTransformer(param);
        }

        public void doPreTransform() {
            ColorSpace colorSpace;
            DElog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform()");
            if (this.mInOriginalBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                this.mInTransBitmap = this.mInOriginalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                if (this.mInTransBitmap != null) {
                    this.mColorspaceParam.mInBitmap = this.mInTransBitmap;
                    if (this.mInOriginalBitmap == this.mOutOriginalBitmap) {
                        this.mOutTransBitmap = this.mInTransBitmap;
                        this.mColorspaceParam.mOutBitmap = this.mOutTransBitmap;
                        DElog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform() done");
                        return;
                    }
                } else {
                    DElog.e(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform() error! can't copy in bitmap");
                    throw new IllegalArgumentException("doPreTransform can't copy in bitmap");
                }
            }
            if (this.mOutOriginalBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                ColorSpace colorSpace2 = this.mOutOriginalBitmap.getColorSpace();
                int width = this.mOutOriginalBitmap.getWidth();
                int height = this.mOutOriginalBitmap.getHeight();
                Bitmap.Config config = Bitmap.Config.ARGB_8888;
                if (colorSpace2 != null) {
                    colorSpace = colorSpace2;
                } else {
                    colorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
                }
                this.mOutTransBitmap = Bitmap.createBitmap(width, height, config, true, colorSpace);
                if (this.mOutTransBitmap != null) {
                    this.mColorspaceParam.mOutBitmap = this.mOutTransBitmap;
                } else {
                    DElog.e(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform() error! can't create out bitmap");
                    throw new IllegalArgumentException("doPreTransform can't create out bitmap");
                }
            }
            DElog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPreTransform() done");
        }

        public void doPostTransform() {
            if (this.mOutTransBitmap != null) {
                DElog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPostTransform()");
                new Canvas(this.mOutOriginalBitmap).drawBitmap(this.mOutTransBitmap, 0.0f, 0.0f, null);
                DElog.i(ImageProcessor.TAG, "BitmapConfigTransformer doPostTransform() done");
            }
        }
    }

    public static class ColorspaceParam {
        protected static final String PARAM_IN_BITMAP = "inBitmap";
        protected static final String PARAM_OUT_BITMAP = "outBitmap";
        public Bitmap mInBitmap;
        public ColorspaceType mInColorspace;
        public Bitmap mOutBitmap;
        public ColorspaceType mOutColorspace;

        public ColorspaceParam(Bitmap inBitmap, Bitmap outBitmap, ColorspaceType inColorspace, ColorspaceType outColorspace) {
            this.mInBitmap = inBitmap;
            this.mOutBitmap = outBitmap;
            this.mInColorspace = inColorspace;
            this.mOutColorspace = outColorspace;
        }

        public ColorspaceParam(Map<String, Object> param) {
            this.mInBitmap = (Bitmap) param.get(PARAM_IN_BITMAP);
            this.mOutBitmap = (Bitmap) param.get(PARAM_OUT_BITMAP);
            if (!isParamInvalid()) {
                this.mInColorspace = ColorspaceType.getEnum(this.mInBitmap.getColorSpace());
                if (this.mInColorspace == null) {
                    this.mInColorspace = ColorspaceType.SRGB;
                    DElog.w(ImageProcessor.TAG, "ColorspaceParam() error! unsupport inColorspace = " + this.mInBitmap.getColorSpace() + ", treat as SRGB");
                }
                this.mOutColorspace = ImageProcessor.mAlgoStrategy.needNormallizeColorSpace() ? ImageProcessor.mAlgoStrategy.getNormallizeColorGamut() : this.mInColorspace;
                return;
            }
            throw new IllegalArgumentException("ColorspaceParam input param invalid");
        }

        public ColorspaceParam(Map<String, Object> param, ColorspaceType inColorspace, ColorspaceType outColorspace) {
            this.mInBitmap = (Bitmap) param.get(PARAM_IN_BITMAP);
            this.mOutBitmap = (Bitmap) param.get(PARAM_OUT_BITMAP);
            this.mInColorspace = inColorspace;
            this.mOutColorspace = outColorspace;
            if (isParamInvalid()) {
                throw new IllegalArgumentException("ColorspaceParam input param invalid");
            }
        }

        private boolean isParamInvalid() {
            if (this.mInBitmap == null) {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mInBitmap is null");
                return true;
            }
            Bitmap.Config bitmapConfig = this.mInBitmap.getConfig();
            if (bitmapConfig != Bitmap.Config.ARGB_8888 && bitmapConfig != Bitmap.Config.RGB_565) {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! unsupported mInBitmap format " + bitmapConfig);
                return true;
            } else if (this.mOutBitmap == null) {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mOutBitmap is null");
                return true;
            } else {
                Bitmap.Config bitmapConfig2 = this.mOutBitmap.getConfig();
                if (bitmapConfig2 == Bitmap.Config.ARGB_8888 || bitmapConfig2 == Bitmap.Config.RGB_565) {
                    return false;
                }
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! unsupported mOutBitmap format " + bitmapConfig2);
                return true;
            }
        }
    }

    public enum ColorspaceType {
        SRGB(ColorSpace.get(ColorSpace.Named.SRGB)),
        ADOBE_RGB(ColorSpace.get(ColorSpace.Named.ADOBE_RGB)),
        DISPLAY_P3(ColorSpace.get(ColorSpace.Named.DISPLAY_P3)),
        SUPER_GAMUT(null);
        
        private static final Map<Integer, ColorspaceType> mColorSpaceIdToEnum = null;
        private ColorSpace mColorSpace;

        static {
            int i;
            mColorSpaceIdToEnum = new HashMap();
            for (ColorspaceType type : values()) {
                if (type.mColorSpace != null) {
                    mColorSpaceIdToEnum.put(Integer.valueOf(type.mColorSpace.getId()), type);
                }
            }
        }

        private ColorspaceType(ColorSpace colorSpace) {
            this.mColorSpace = colorSpace;
        }

        public static ColorspaceType getEnum(ColorSpace colorSpace) {
            if (colorSpace == null) {
                return null;
            }
            return mColorSpaceIdToEnum.get(Integer.valueOf(colorSpace.getId()));
        }
    }

    public static class CommonInfo {
        private ImageProcessorAlgoImpl mAlgo;
        public long mCommonHandle;

        public CommonInfo(long handle, ImageProcessorAlgoImpl algo) {
            this.mCommonHandle = handle;
            this.mAlgo = algo;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                if (this.mCommonHandle != 0) {
                    DElog.i(ImageProcessor.TAG, "CommonInfo finalize() mCommonHandle=" + this.mCommonHandle);
                    if (this.mAlgo != null) {
                        this.mAlgo.destroyCommonInfo(this);
                    }
                }
            } finally {
                super.finalize();
            }
        }
    }

    public static class CreateTileProcessEngineParam {
        private static final String PARAM_ENGINE_TYPE = "engineType";
        private static final String PARAM_TILE_BORDER = "tileBorder";
        private static final String PARAM_TILE_SIZE = "tileSize";
        public Set<AlgoType> mAlgos = ImageProcessor.mAlgoStrategy.getTileEngineAlgos(this.mEngineType);
        public TileEngineType mEngineType;
        public int mTileBorder;
        public int mTileSize;

        public CreateTileProcessEngineParam(Map<String, Object> param) {
            this.mEngineType = TileEngineType.valueOf((String) param.get(PARAM_ENGINE_TYPE));
            this.mTileSize = ((Integer) param.get(PARAM_TILE_SIZE)).intValue();
            this.mTileBorder = ((Integer) param.get(PARAM_TILE_BORDER)).intValue();
        }
    }

    private static class DestroyTileProcessEngineParam {
        private static final String PARAM_ENGINE = "engine";
        public long mAlgoHandle;

        public DestroyTileProcessEngineParam(Map<String, Object> param) {
            this.mAlgoHandle = ((Long) param.get(PARAM_ENGINE)).longValue();
        }
    }

    public static class ImageEngine {
        public long mAlgoHandle;

        public ImageEngine(long handle) {
            this.mAlgoHandle = handle;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            if (this.mAlgoHandle != 0) {
                DElog.e(ImageProcessor.TAG, "ImageEngine finalize() error! haven't destroyed yet, mAlgoHandle=" + this.mAlgoHandle);
            }
            super.finalize();
        }
    }

    public enum ImageType {
        NORMAL,
        WIDE_COLOR_SPACE,
        SKIN_BEAUTY,
        VIVID
    }

    private static class InstanceHolder {
        public static ImageProcessorAlgoStrategy mInstance = new ImageProcessorAlgoStrategy();

        private InstanceHolder() {
        }
    }

    public static class ThumbnailParam extends ColorspaceParam {
        private static final String PARAM_FILE_PATH = "filePath";
        private static final String PARAM_IMAGE_DESCRIPTION = "imageDescription";
        private static final String PARAM_ISO = "iso";
        private static final String PARAM_SCALE_RATIO = "scaleRatio";
        private static final String PARAM_SKIN_BEAUTY = "skinBeauty";
        private static final String PARAM_THUMBNAIL_TYPE = "thumbnailType";
        public Set<AlgoType> mAlgos;
        public Set<AlgoType> mCommonAlgos;
        public String mFilePath;
        public int mISO;
        public String mImageDescription;
        public ImageType mImageType = ImageType.NORMAL;
        public float mScaleRatio = 1.0f;
        public boolean mSkinBeauty;
        public ThumbnailType mType = ThumbnailType.DEFAULT;

        public ThumbnailParam(Map<String, Object> param) {
            super(param);
            this.mFilePath = (String) param.get(PARAM_FILE_PATH);
            if (param.containsKey(PARAM_THUMBNAIL_TYPE)) {
                this.mType = ThumbnailType.valueOf((String) param.get(PARAM_THUMBNAIL_TYPE));
            }
            if (param.containsKey(PARAM_SCALE_RATIO)) {
                this.mScaleRatio = ((Float) param.get(PARAM_SCALE_RATIO)).floatValue();
            }
            if (param.containsKey(PARAM_ISO)) {
                this.mISO = ((Integer) param.get(PARAM_ISO)).intValue();
            }
            if (param.containsKey(PARAM_SKIN_BEAUTY)) {
                this.mSkinBeauty = ((Boolean) param.get(PARAM_SKIN_BEAUTY)).booleanValue();
            }
            boolean isVivid = false;
            if (param.containsKey(PARAM_IMAGE_DESCRIPTION)) {
                this.mImageDescription = (String) param.get(PARAM_IMAGE_DESCRIPTION);
                this.mSkinBeauty = ImageProcessor.isImageDescriptionBeauty(this.mImageDescription);
                isVivid = ImageProcessor.isImageDescriptionVivid(this.mImageDescription);
            }
            if (this.mSkinBeauty && ImageProcessor.mAlgoStrategy.isAlgoSkinBeautyEnable()) {
                this.mImageType = ImageType.SKIN_BEAUTY;
            } else if (isVivid && ImageProcessor.mAlgoStrategy.isAlgoVividEnable()) {
                this.mImageType = ImageType.VIVID;
            } else if (this.mInColorspace != ColorspaceType.SRGB && ImageProcessor.mAlgoStrategy.isAlgoWideColorSpaceEnable()) {
                this.mImageType = ImageType.WIDE_COLOR_SPACE;
            }
            this.mAlgos = ImageProcessor.mAlgoStrategy.getThumbnailAlgos(this.mType, this.mImageType);
            this.mCommonAlgos = ImageProcessor.mAlgoStrategy.getCommonInfoAlgos(this.mType, this.mImageType);
            if (isParamInvalid()) {
                throw new IllegalArgumentException("processThumbnail input param invalid");
            }
        }

        private boolean isParamInvalid() {
            if (this.mFilePath.isEmpty()) {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mFilePath is empty");
                return true;
            } else if (this.mType == null) {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mType is empty");
                return true;
            } else if (this.mScaleRatio <= 0.0f || this.mScaleRatio > 2.0f) {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mScaleRatio=" + this.mScaleRatio + " out of range");
                return true;
            } else if (this.mISO >= 0 && this.mISO <= 102400) {
                return false;
            } else {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mISO=" + this.mISO + " out of range");
                return true;
            }
        }
    }

    public enum ThumbnailType {
        DEFAULT,
        MICRO,
        FAST,
        ANIMATION,
        FULLSCREEN,
        HALFSCREEN,
        GIF
    }

    public enum TileEngineType {
        SR,
        NON_SR
    }

    public static class TileParam extends ColorspaceParam {
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
        public long mAlgoHandle;
        public Size mDecodedSize;
        public Point mDecodedStartPoint;
        public TileEngineType mEngineType;
        public String mFilePath;
        public String mImageDescription;
        public ImageType mImageType = ImageType.NORMAL;
        public Rect mInVisibleRange;
        public float mScaleRatio = 1.0f;
        public PointF mScaledStartPoint;
        public float mZoomInRatio = 1.0f;

        public TileParam(Map<String, Object> param) {
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
            if (isSkinBeauty && ImageProcessor.mAlgoStrategy.isAlgoSkinBeautyEnable()) {
                this.mImageType = ImageType.SKIN_BEAUTY;
            } else if (isVivid && ImageProcessor.mAlgoStrategy.isAlgoVividEnable()) {
                this.mImageType = ImageType.VIVID;
            } else if (this.mInColorspace != ColorspaceType.SRGB && ImageProcessor.mAlgoStrategy.isAlgoWideColorSpaceEnable()) {
                this.mImageType = ImageType.WIDE_COLOR_SPACE;
            }
            if (isParamInvalid()) {
                throw new IllegalArgumentException("processTile input param invalid");
            }
        }

        private boolean isParamInvalid() {
            if (this.mEngineType == null) {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mEngineType is null");
                return true;
            } else if (this.mFilePath.isEmpty()) {
                DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mFilePath is empty");
                return true;
            } else {
                if (this.mEngineType == TileEngineType.SR) {
                    if (this.mScaleRatio <= 0.0f || this.mScaleRatio > 2.0f) {
                        DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mScaleRatio=" + this.mScaleRatio + " out of range");
                        return true;
                    } else if (this.mZoomInRatio <= 0.0f) {
                        DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mZoomInRatio=" + this.mZoomInRatio + " out of range");
                        return true;
                    } else if (this.mScaledStartPoint == null) {
                        DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mScaledStartPoint is null");
                        return true;
                    } else if (isVisibleRangeInvalid(this.mInVisibleRange)) {
                        return true;
                    }
                } else if (this.mDecodedSize == null) {
                    DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mDecodedSize is null");
                    return true;
                } else if (this.mDecodedSize.getWidth() <= 0 || this.mDecodedSize.getHeight() <= 0) {
                    DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mDecodedSize=" + this.mDecodedSize);
                    return true;
                } else if (this.mDecodedStartPoint == null) {
                    DElog.e(ImageProcessor.TAG, "isParamInvalid() error! mDecodedStartPoint is null");
                    return true;
                }
                return false;
            }
        }

        private boolean isVisibleRangeInvalid(Rect rect) {
            if (rect == null) {
                DElog.e(ImageProcessor.TAG, "isVisibleRangeInvalid() error! rect is null");
                return true;
            } else if (rect.left >= 0 && rect.top >= 0 && rect.left < rect.right && rect.top < rect.bottom && rect.right < this.mInBitmap.getWidth() && rect.bottom < this.mInBitmap.getHeight()) {
                return false;
            } else {
                DElog.e(ImageProcessor.TAG, "isVisibleRangeInvalid() error! rect=" + rect);
                return true;
            }
        }
    }

    public static boolean isCommandOwner(String command) {
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

    public static boolean isSceneSensitive(int scene, int action) {
        if (scene == 3 && (action == 9 || action == 10 || action == 13)) {
            return true;
        }
        return false;
    }

    public ImageProcessor(IDisplayEngineServiceEx service) {
        DElog.i(TAG, "ImageProcessor enter");
        mAlgoStrategy = InstanceHolder.mInstance;
        this.mService = service;
        this.mAlgo = new ImageProcessorAlgoImpl(service);
        this.mEnable = mAlgoStrategy.isImageProcessorEnable() && this.mAlgo.isAlgoInitSuccess();
        this.mCommonInfoCache = Collections.synchronizedMap(new LinkedHashMap<String, CommonInfo>(16, 0.75f, true) {
            /* access modifiers changed from: protected */
            public boolean removeEldestEntry(Map.Entry<String, CommonInfo> entry) {
                if (size() > 14) {
                    return true;
                }
                return false;
            }
        });
        this.mTileProcessEngineCache = Collections.synchronizedMap(new HashMap());
        DElog.i(TAG, "ImageProcessor exit");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        DElog.i(TAG, "finalize");
        try {
            clearCommonInfo();
            clearImageEngine();
        } finally {
            super.finalize();
        }
    }

    public Object imageProcess(String command, Map<String, Object> param) {
        if (command == null) {
            return null;
        }
        DElog.d(TAG, "imageProcess() command=" + command);
        if (command.equals(CMD_GET_SUPPORT_CMD)) {
            return getSupportCmd();
        }
        if (!this.mEnable) {
            DElog.e(TAG, "imageProcess() is disable, command=" + command);
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
        switch (c) {
            case 0:
                return getWideColorGamutSupported();
            case 1:
                processThumbnail(param);
                break;
            case 2:
                return createTileProcessEngine(param);
            case 3:
                processTile(param);
                break;
            case 4:
                destroyTileProcessEngine(param);
                break;
            case 5:
                transformColorspaceToSRGB(param);
                break;
            default:
                DElog.e(TAG, "imageProcess() error! undefine command=" + command);
                break;
        }
        return null;
    }

    private List<String> getSupportCmd() {
        List<String> supportedCmd = new ArrayList<>();
        if (this.mEnable) {
            supportedCmd.add(CMD_GET_SUPPORT_CMD);
            supportedCmd.add(CMD_GET_WIDE_COLOR_GAMUT_SUPPORTED);
            supportedCmd.add(CMD_PROCESS_THUMBNAIL);
            supportedCmd.add(CMD_CREATE_TILE_PROCESS_ENGINE);
            supportedCmd.add(CMD_PROCESS_TILE);
            supportedCmd.add(CMD_DESTROY_TILE_PROCESS_ENGINE);
            if (mAlgoStrategy.needNormallizeColorSpace()) {
                supportedCmd.add(CMD_TRANSFORM_COLORSPACE);
            }
        } else {
            supportedCmd.add(CMD_GET_SUPPORT_CMD);
        }
        return supportedCmd;
    }

    private Boolean getWideColorGamutSupported() {
        return Boolean.valueOf(this.mEnable && (mAlgoStrategy.needNormallizeColorSpace() || mAlgoStrategy.isAlgoWideColorSpaceEnable()));
    }

    private void transformColorspaceToSRGB(Map<String, Object> param) {
        DElog.i(TAG, "transformColorspaceToSRGB() begin");
        if (!mAlgoStrategy.isImageProcessorEnable()) {
            throw new UnsupportedOperationException("image process is disabled");
        } else if (mAlgoStrategy.needNormallizeColorSpace()) {
            ColorspaceParam colorspaceParam = new ColorspaceParam(param, ColorspaceType.SUPER_GAMUT, ColorspaceType.SRGB);
            if (!isGMPInImageMode()) {
                if (colorspaceParam.mInBitmap != colorspaceParam.mOutBitmap) {
                    copyPixels(colorspaceParam.mInBitmap, colorspaceParam.mOutBitmap);
                    DElog.i(TAG, "transformColorspaceToSRGB() copy end");
                } else {
                    DElog.i(TAG, "transformColorspaceToSRGB() bypass end");
                }
                return;
            }
            BitmapConfigTransformer transformer = BitmapConfigTransformer.create(colorspaceParam);
            if (transformer != null) {
                transformer.doPreTransform();
            }
            this.mAlgo.transformColorspace(colorspaceParam);
            if (transformer != null) {
                transformer.doPostTransform();
            }
            DElog.i(TAG, "transformColorspaceToSRGB() trans end");
        } else {
            throw new UnsupportedOperationException("transform colorspace is disabled");
        }
    }

    private boolean isGMPInImageMode() {
        if (this.mService == null) {
            DElog.e(TAG, "isGMPInImageMode() mService is null!");
            return false;
        }
        boolean z = true;
        byte[] isImage = new byte[1];
        try {
            if (this.mService.getEffect(3, 4, isImage, isImage.length) != 0) {
                DElog.e(TAG, "isGMPInImageMode() getEffect failed, return " + ret);
                return false;
            }
            if (isImage[0] == 0) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            DElog.e(TAG, "isGMPInImageMode() RemoteException " + e);
            return false;
        }
    }

    private void transformColorspaceOnBitmap(ColorspaceParam colorspaceParam, Bitmap inBitmap, Bitmap outBitmap) {
        if (mAlgoStrategy.needNormallizeColorSpace() && colorspaceParam.mInColorspace != colorspaceParam.mOutColorspace) {
            this.mAlgo.transformColorspace(new ColorspaceParam(inBitmap, outBitmap, colorspaceParam.mInColorspace, colorspaceParam.mOutColorspace));
        }
    }

    /* access modifiers changed from: private */
    public static boolean isImageDescriptionBeauty(String imageDescription) {
        if (imageDescription == null || imageDescription.isEmpty()) {
            return false;
        }
        return mImageDescriptionBeauty.contains(imageDescription.split("_", 2)[0]);
    }

    /* access modifiers changed from: private */
    public static boolean isImageDescriptionVivid(String imageDescription) {
        if (imageDescription == null || imageDescription.isEmpty()) {
            return false;
        }
        String[] split = imageDescription.split("_", 3);
        if (split.length < 2) {
            return false;
        }
        return mImageDescriptionVivid.contains(split[1]);
    }

    private void processThumbnail(Map<String, Object> param) {
        DElog.d(TAG, "processThumbnail()");
        if (mAlgoStrategy.isImageProcessorEnable()) {
            ThumbnailParam thumbnailParam = new ThumbnailParam(param);
            BitmapConfigTransformer transformer = BitmapConfigTransformer.create(thumbnailParam);
            if (transformer != null) {
                transformer.doPreTransform();
            }
            processThumbnail(thumbnailParam);
            if (transformer != null) {
                transformer.doPostTransform();
                return;
            }
            return;
        }
        throw new UnsupportedOperationException("image process is disabled");
    }

    private static void copyPixels(Bitmap in, Bitmap out) {
        if (in != null && out != null && in != out) {
            ByteBuffer buffer = ByteBuffer.allocate(in.getByteCount());
            in.copyPixelsToBuffer(buffer);
            buffer.rewind();
            out.copyPixelsFromBuffer(buffer);
            DElog.i(TAG, "copyPixels() done");
        }
    }

    private void copyPixelsToOutBitmapIfNeeded(ColorspaceParam thumbnailParam) {
        if (thumbnailParam.mInBitmap != thumbnailParam.mOutBitmap) {
            if (!mAlgoStrategy.needNormallizeColorSpace() || thumbnailParam.mInColorspace == thumbnailParam.mOutColorspace) {
                copyPixels(thumbnailParam.mInBitmap, thumbnailParam.mOutBitmap);
            }
        }
    }

    private void processThumbnail(ThumbnailParam thumbnailParam) {
        boolean processDone = false;
        CommonInfo commonInfo = null;
        if (mAlgoStrategy.needRunSoftwareAlgo(thumbnailParam.mType, thumbnailParam.mImageType)) {
            commonInfo = getCommonInfo(thumbnailParam.mFilePath);
            if (commonInfo == null) {
                try {
                    commonInfo = new CommonInfo(this.mAlgo.createCommonInfo(thumbnailParam), this.mAlgo);
                    if (mAlgoStrategy.needSaveCommonInfo(thumbnailParam.mType)) {
                        saveCommonInfo(thumbnailParam.mFilePath, commonInfo);
                    }
                } catch (RuntimeException e) {
                    transformColorspaceOnBitmap(thumbnailParam, thumbnailParam.mInBitmap, thumbnailParam.mInBitmap);
                    collectInfoForImageRecognization(thumbnailParam, commonInfo);
                    throw e;
                }
            }
            if (thumbnailParam.mAlgos != null) {
                ImageEngine imageEngine = null;
                try {
                    imageEngine = this.mAlgo.createImageEngine(thumbnailParam);
                    this.mAlgo.processThumbnail(imageEngine, commonInfo, thumbnailParam);
                    processDone = true;
                    this.mAlgo.destroyImageEngine(imageEngine);
                    if (0 != 0) {
                        this.mAlgo.destroyImageEngine(null);
                    }
                } catch (RuntimeException e2) {
                    transformColorspaceOnBitmap(thumbnailParam, thumbnailParam.mInBitmap, thumbnailParam.mInBitmap);
                    collectInfoForImageRecognization(thumbnailParam, commonInfo);
                    throw e2;
                } catch (Throwable th) {
                    if (imageEngine != null) {
                        this.mAlgo.destroyImageEngine(imageEngine);
                    }
                    throw th;
                }
            }
        }
        transformColorspaceOnBitmap(thumbnailParam, processDone ? thumbnailParam.mOutBitmap : thumbnailParam.mInBitmap, thumbnailParam.mOutBitmap);
        if (!processDone) {
            copyPixelsToOutBitmapIfNeeded(thumbnailParam);
        }
        collectInfoForImageRecognization(thumbnailParam, commonInfo);
    }

    private void collectInfoForImageRecognization(String filePath, String imageDescription, CommonInfo commonInfo) {
        int hardwareSharpnessLevel = 0;
        if (this.mCurrentFilePath == null || !this.mCurrentFilePath.equals(filePath)) {
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
            DElog.e(TAG, "sendInfoToImageRecognization() mService is null!");
            return;
        }
        DElog.i(TAG, "sendInfoToImageRecognization filePath=" + filePath + ", imageDescription=" + imageDescription + ", hardwareSharpnessLevel=" + hardwareSharpnessLevel);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("filePath", filePath);
        if (imageDescription != null) {
            bundle.putString("imageDescription", imageDescription);
        }
        bundle.putInt("hardwareSharpnessLevel", hardwareSharpnessLevel);
        try {
            this.mService.setData(4, bundle);
        } catch (RemoteException e) {
            DElog.e(TAG, "sendInfoToImageRecognization setData error! filePath=" + filePath + ", hardwareSharpnessLevel=" + hardwareSharpnessLevel + ", " + e.getMessage());
        }
    }

    private void saveCommonInfo(String hashID, CommonInfo commonInfo) {
        DElog.d(TAG, "CommonInfoCache save " + hashID + ", commonHandle=" + commonInfo.mCommonHandle);
        this.mCommonInfoCache.put(hashID, commonInfo);
    }

    private void clearCommonInfo() {
        if (!this.mCommonInfoCache.isEmpty()) {
            DElog.i(TAG, "CommonInfoCache clear size=" + this.mCommonInfoCache.size());
            this.mCommonInfoCache.clear();
        }
    }

    private CommonInfo getCommonInfo(String hashID) {
        CommonInfo commonInfo = this.mCommonInfoCache.get(hashID);
        if (commonInfo == null || commonInfo.mCommonHandle != 0) {
            return commonInfo;
        }
        DElog.e(TAG, "getCommonInfo() error! hashID " + hashID + " commonHandle is 0");
        throw new IllegalStateException("getCommonInfo() error! hashID " + hashID + " commonHandle is 0");
    }

    private Long createTileProcessEngine(Map<String, Object> param) {
        DElog.i(TAG, "createTileProcessEngine()");
        if (mAlgoStrategy.isImageProcessorEnable()) {
            CreateTileProcessEngineParam createTileProcessEngineParam = new CreateTileProcessEngineParam(param);
            if (createTileProcessEngineParam.mAlgos == null) {
                return 0L;
            }
            ImageEngine imageEngine = this.mAlgo.createImageEngine(createTileProcessEngineParam);
            saveImageEngine(imageEngine);
            return Long.valueOf(imageEngine.mAlgoHandle);
        }
        throw new UnsupportedOperationException("image process is disabled");
    }

    private void processTile(Map<String, Object> param) {
        DElog.d(TAG, "processTile()");
        if (mAlgoStrategy.isImageProcessorEnable()) {
            TileParam tileParam = new TileParam(param);
            BitmapConfigTransformer transformer = BitmapConfigTransformer.create(tileParam);
            if (transformer != null) {
                transformer.doPreTransform();
            }
            processTile(tileParam);
            if (transformer != null) {
                transformer.doPostTransform();
                return;
            }
            return;
        }
        throw new UnsupportedOperationException("image process is disabled");
    }

    private void processTile(TileParam tileParam) {
        boolean processDone = false;
        CommonInfo commonInfo = null;
        if (mAlgoStrategy.needRunSoftwareAlgo(tileParam.mEngineType, tileParam.mImageType)) {
            commonInfo = getCommonInfo(tileParam.mFilePath);
            if (commonInfo == null) {
                DElog.e(TAG, "processTile() error! can't find commonInfo for " + tileParam.mFilePath + ", mCommonInfoCache size=" + this.mCommonInfoCache.size());
                if (tileParam.mEngineType == TileEngineType.NON_SR) {
                    transformColorspaceOnBitmap(tileParam, tileParam.mInBitmap, tileParam.mOutBitmap);
                }
                collectInfoForImageRecognization(tileParam, commonInfo);
                throw new IllegalStateException("processTile() can't find commonInfo, mCommonInfoCache size=" + this.mCommonInfoCache.size());
            }
            ImageEngine imageEngine = getImageEngine(tileParam.mAlgoHandle);
            if (imageEngine == null) {
                DElog.e(TAG, "processTile() error! can't find imageEngine for " + tileParam.mAlgoHandle + ", mTileProcessEngineCache size=" + this.mTileProcessEngineCache.size());
                if (tileParam.mEngineType == TileEngineType.NON_SR) {
                    transformColorspaceOnBitmap(tileParam, tileParam.mInBitmap, tileParam.mOutBitmap);
                }
                collectInfoForImageRecognization(tileParam, commonInfo);
                throw new IllegalStateException("processTile() can't find imageEngine, mTileProcessEngineCache size=" + this.mTileProcessEngineCache.size());
            }
            try {
                this.mAlgo.processTileAlgo(imageEngine, commonInfo, tileParam);
                processDone = true;
            } catch (RuntimeException e) {
                if (tileParam.mEngineType == TileEngineType.NON_SR) {
                    transformColorspaceOnBitmap(tileParam, tileParam.mInBitmap, tileParam.mOutBitmap);
                }
                collectInfoForImageRecognization(tileParam, commonInfo);
                throw e;
            }
        }
        if (tileParam.mEngineType == TileEngineType.NON_SR) {
            transformColorspaceOnBitmap(tileParam, processDone ? tileParam.mOutBitmap : tileParam.mInBitmap, tileParam.mOutBitmap);
            if (!processDone) {
                copyPixelsToOutBitmapIfNeeded(tileParam);
            }
        } else if (!processDone) {
            DElog.e(TAG, "processTile() error! engineType is SR but no algo run");
            throw new IllegalStateException("processTile() error! engineType is SR but no algo run");
        }
        collectInfoForImageRecognization(tileParam, commonInfo);
    }

    private void destroyTileProcessEngine(Map<String, Object> param) {
        DElog.d(TAG, "destroyTileProcessEngine()");
        if (mAlgoStrategy.isImageProcessorEnable()) {
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
        DElog.d(TAG, "TileProcessEngineCache save " + imageEngine.mAlgoHandle);
        this.mTileProcessEngineCache.put(Long.valueOf(imageEngine.mAlgoHandle), imageEngine);
    }

    private ImageEngine getImageEngine(long algoHandle) {
        return this.mTileProcessEngineCache.get(Long.valueOf(algoHandle));
    }

    private void removeImageEngine(long algoHandle) {
        ImageEngine imageEngine = this.mTileProcessEngineCache.get(Long.valueOf(algoHandle));
        if (imageEngine == null) {
            DElog.e(TAG, "removeImageEngine() error! can't find " + algoHandle);
            return;
        }
        this.mAlgo.destroyImageEngine(imageEngine);
        this.mTileProcessEngineCache.remove(Long.valueOf(algoHandle));
    }

    private void clearImageEngine() {
        if (!this.mTileProcessEngineCache.isEmpty()) {
            DElog.e(TAG, "TileProcessEngineCache clear size=" + this.mTileProcessEngineCache.size());
            synchronized (this.mTileProcessEngineCache) {
                for (ImageEngine imageEngine : this.mTileProcessEngineCache.values()) {
                    this.mAlgo.destroyImageEngine(imageEngine);
                }
            }
            this.mTileProcessEngineCache.clear();
        }
    }

    public void setScene(int scene, int action) {
        if (action != 13) {
            switch (action) {
                case 9:
                    DElog.i(TAG, "setScene THUMBNAIL");
                    this.mCurrentFilePath = null;
                    this.mHardwareSharpnessLevel = -1;
                    return;
                case 10:
                    DElog.i(TAG, "setScene FULLSCREEN");
                    return;
                default:
                    DElog.e(TAG, "setScene unknown action = " + action);
                    return;
            }
        } else {
            DElog.i(TAG, "setScene EXIT");
            clearCommonInfo();
        }
    }
}
