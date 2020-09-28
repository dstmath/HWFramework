package com.huawei.displayengine;

import android.os.SystemProperties;
import android.os.Trace;
import com.huawei.displayengine.ImageProcessor;
import com.huawei.sidetouch.TpCommandConstant;
import com.huawei.uikit.effect.BuildConfig;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public class ImageProcessorAlgoStrategy {
    private static final String TAG = "DE J ImageProcessorAlgoStrategy";
    private static final String XML_PATH = "display/effect/algorithm/imageprocessor/ImageProcessStrategy.xml";
    private static boolean mAlgoSkinBeautyEnable;
    private static boolean mAlgoVividEnable;
    private static boolean mAlgoWideColorSpaceEnable;
    private static ImageProcessor.ThumbnailType mCurrentThumbnailType;
    private static ImageProcessor.TileEngineType mCurrentTileType;
    private static boolean mEnable;
    private static boolean mNeedNormallizeColorSpace;
    private static ImageProcessor.ColorspaceType mNormallizeColorGamut = ImageProcessor.ColorspaceType.SRGB;
    private static Map<ImageProcessor.ThumbnailType, ThumbnailStrategy> mThumbnailStrategy = new HashMap();
    private static Map<ImageProcessor.TileEngineType, TileStrategy> mTileStrategy = new HashMap();

    public ImageProcessorAlgoStrategy() {
        DeLog.i(TAG, "ImageProcessorAlgoStrategy enter");
        if (!isFactoryMode()) {
            loadXML();
            initCommonInfoAlgos();
        }
        printStrategy();
        DeLog.i(TAG, "ImageProcessorAlgoStrategy exit");
    }

    private void printStrategy() {
        DeLog.i(TAG, "printStrategy mEnable=" + mEnable + ", mAlgoWideColorSpaceEnable=" + mAlgoWideColorSpaceEnable + ", mAlgoSkinBeautyEnable=" + mAlgoSkinBeautyEnable + ", mAlgoVividEnable=" + mAlgoVividEnable + ", mNeedNormallizeColorSpace=" + mNeedNormallizeColorSpace + ", mNormallizeColorGamut=" + mNormallizeColorGamut);
        for (Map.Entry<ImageProcessor.ThumbnailType, ThumbnailStrategy> entry : mThumbnailStrategy.entrySet()) {
            ThumbnailStrategy thumbnailStrategy = entry.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append("printStrategy Thumbnail: ");
            sb.append(entry.getKey());
            sb.append(", Algo:");
            sb.append(thumbnailStrategy.mAlgos);
            sb.append(thumbnailStrategy.mIsSaveCommonInfo ? ", CommonInfoAlgo:" + thumbnailStrategy.mCommonInfoAlgos : BuildConfig.FLAVOR);
            DeLog.i(TAG, sb.toString());
        }
        for (Map.Entry<ImageProcessor.TileEngineType, TileStrategy> entry2 : mTileStrategy.entrySet()) {
            DeLog.i(TAG, "printStrategy Tile: " + entry2.getKey() + ", Algo:" + entry2.getValue().mAlgos);
        }
    }

    private void initCommonInfoAlgos() {
        DeLog.d(TAG, "initCommonInfoAlgos");
        ImageProcessor.ImageType[] values = ImageProcessor.ImageType.values();
        for (ImageProcessor.ImageType imageType : values) {
            if (isImageTypeEnable(imageType)) {
                Set<ImageProcessor.AlgoType> algos = EnumSet.noneOf(ImageProcessor.AlgoType.class);
                for (ImageProcessor.TileEngineType tileEngineType : mTileStrategy.keySet()) {
                    Set<ImageProcessor.AlgoType> set = getTileAlgos(tileEngineType, imageType);
                    if (set != null) {
                        algos.addAll(set);
                    }
                }
                if (!algos.isEmpty()) {
                    for (ThumbnailStrategy thumbnailStrategy : mThumbnailStrategy.values()) {
                        if (thumbnailStrategy.mIsSaveCommonInfo) {
                            if (thumbnailStrategy.mCommonInfoAlgos == null) {
                                thumbnailStrategy.mCommonInfoAlgos = new HashMap();
                                thumbnailStrategy.mCommonInfoAlgos.put(imageType, algos);
                            } else {
                                Set<ImageProcessor.AlgoType> set2 = thumbnailStrategy.mCommonInfoAlgos.get(imageType);
                                if (set2 == null) {
                                    thumbnailStrategy.mCommonInfoAlgos.put(imageType, algos);
                                } else {
                                    set2.addAll(algos);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.displayengine.ImageProcessorAlgoStrategy$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$displayengine$ImageProcessor$ImageType = new int[ImageProcessor.ImageType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ImageType[ImageProcessor.ImageType.NORMAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ImageType[ImageProcessor.ImageType.WIDE_COLOR_SPACE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ImageType[ImageProcessor.ImageType.SKIN_BEAUTY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$displayengine$ImageProcessor$ImageType[ImageProcessor.ImageType.VIVID.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean isImageTypeEnable(ImageProcessor.ImageType imageType) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$displayengine$ImageProcessor$ImageType[imageType.ordinal()];
        if (i == 1) {
            return true;
        }
        if (i == 2) {
            return mAlgoWideColorSpaceEnable;
        }
        if (i == 3) {
            return mAlgoSkinBeautyEnable;
        }
        if (i != 4) {
            return false;
        }
        return mAlgoVividEnable;
    }

    public boolean isImageProcessorEnable() {
        return mEnable;
    }

    public boolean isAlgoWideColorSpaceEnable() {
        return mAlgoWideColorSpaceEnable;
    }

    public boolean isAlgoSkinBeautyEnable() {
        return mAlgoSkinBeautyEnable;
    }

    public boolean isAlgoVividEnable() {
        return mAlgoVividEnable;
    }

    public boolean needNormallizeColorSpace() {
        return mNeedNormallizeColorSpace;
    }

    public ImageProcessor.ColorspaceType getNormallizeColorGamut() {
        return mNormallizeColorGamut;
    }

    /* access modifiers changed from: private */
    public static class ThumbnailStrategy {
        public Map<ImageProcessor.ImageType, Set<ImageProcessor.AlgoType>> mAlgos;
        public Map<ImageProcessor.ImageType, Set<ImageProcessor.AlgoType>> mCommonInfoAlgos;
        public boolean mIsSaveCommonInfo;

        public ThumbnailStrategy(ImageProcessor.ImageType imageType, Set<ImageProcessor.AlgoType> algos) {
            this(imageType, algos, false);
        }

        public ThumbnailStrategy(ImageProcessor.ImageType imageType, Set<ImageProcessor.AlgoType> algos, boolean isSaveCommonInfo) {
            this.mIsSaveCommonInfo = isSaveCommonInfo;
            if (imageType != null && algos != null) {
                this.mAlgos = new HashMap();
                this.mAlgos.put(imageType, algos);
                if (isSaveCommonInfo) {
                    this.mCommonInfoAlgos = new HashMap();
                    this.mCommonInfoAlgos.put(imageType, EnumSet.copyOf(algos));
                }
            }
        }
    }

    public boolean needRunSoftwareAlgo(ImageProcessor.ThumbnailType thumbnailType, ImageProcessor.ImageType imageType) {
        if (getThumbnailAlgos(thumbnailType, imageType) == null && getCommonInfoAlgos(thumbnailType, imageType) == null) {
            return false;
        }
        return true;
    }

    public boolean needSaveCommonInfo(ImageProcessor.ThumbnailType thumbnailType) {
        ThumbnailStrategy thumbnailStrategy = mThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null) {
            return false;
        }
        return thumbnailStrategy.mIsSaveCommonInfo;
    }

    public Set<ImageProcessor.AlgoType> getThumbnailAlgos(ImageProcessor.ThumbnailType thumbnailType, ImageProcessor.ImageType imageType) {
        ThumbnailStrategy thumbnailStrategy = mThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null || thumbnailStrategy.mAlgos == null) {
            return null;
        }
        Set<ImageProcessor.AlgoType> set = thumbnailStrategy.mAlgos.get(imageType);
        if ((set == null || set.isEmpty()) && imageType != ImageProcessor.ImageType.NORMAL) {
            return thumbnailStrategy.mAlgos.get(ImageProcessor.ImageType.NORMAL);
        }
        return set;
    }

    public Set<ImageProcessor.AlgoType> getCommonInfoAlgos(ImageProcessor.ThumbnailType thumbnailType, ImageProcessor.ImageType imageType) {
        ThumbnailStrategy thumbnailStrategy = mThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null) {
            return null;
        }
        if (thumbnailStrategy.mCommonInfoAlgos == null) {
            return getThumbnailAlgos(thumbnailType, imageType);
        }
        Set<ImageProcessor.AlgoType> set = thumbnailStrategy.mCommonInfoAlgos.get(imageType);
        if ((set == null || set.isEmpty()) && imageType != ImageProcessor.ImageType.NORMAL) {
            return thumbnailStrategy.mCommonInfoAlgos.get(ImageProcessor.ImageType.NORMAL);
        }
        return set;
    }

    public static class TileStrategy {
        public Map<ImageProcessor.ImageType, Set<ImageProcessor.AlgoType>> mAlgos;

        public TileStrategy(ImageProcessor.ImageType imageType, Set<ImageProcessor.AlgoType> algos) {
            if (imageType != null && algos != null) {
                this.mAlgos = new HashMap();
                this.mAlgos.put(imageType, algos);
            }
        }
    }

    public boolean needRunSoftwareAlgo(ImageProcessor.TileEngineType tileEngineType, ImageProcessor.ImageType imageType) {
        return getTileAlgos(tileEngineType, imageType) != null;
    }

    public Set<ImageProcessor.AlgoType> getTileEngineAlgos(ImageProcessor.TileEngineType tileEngineType) {
        TileStrategy tileStrategy = mTileStrategy.get(tileEngineType);
        if (tileStrategy == null || tileStrategy.mAlgos == null) {
            return null;
        }
        Set<ImageProcessor.AlgoType> set = null;
        for (Set<ImageProcessor.AlgoType> value : tileStrategy.mAlgos.values()) {
            if (set == null) {
                set = EnumSet.copyOf(value);
            } else {
                set.addAll(value);
            }
        }
        return set;
    }

    public Set<ImageProcessor.AlgoType> getTileAlgos(ImageProcessor.TileEngineType tileEngineType, ImageProcessor.ImageType imageType) {
        TileStrategy tileStrategy = mTileStrategy.get(tileEngineType);
        if (tileStrategy == null || tileStrategy.mAlgos == null) {
            return null;
        }
        Set<ImageProcessor.AlgoType> set = tileStrategy.mAlgos.get(imageType);
        if ((set == null || set.isEmpty()) && imageType != ImageProcessor.ImageType.NORMAL) {
            return tileStrategy.mAlgos.get(ImageProcessor.ImageType.NORMAL);
        }
        return set;
    }

    private boolean isFactoryMode() {
        return "factory".equals(SystemProperties.get("ro.runmode", "factory"));
    }

    private void loadXML() {
        DeLog.d(TAG, "loadXML()");
        Trace.traceBegin(8, "loadXML");
        try {
            if (!parseXml(getXmlPath())) {
                loadDefaultConfig();
            }
        } catch (RuntimeException e) {
            DeLog.e(TAG, "loadXML RuntimeException " + e);
            loadDefaultConfig();
        }
        Trace.traceEnd(8);
    }

    private String getXmlPath() {
        File xmlFile = HwCfgFilePolicy.getCfgFile(XML_PATH, 0);
        if (xmlFile == null) {
            DeLog.w(TAG, "getXmlPath() error! can't find xml file.display/effect/algorithm/imageprocessor/ImageProcessStrategy.xml");
            return null;
        }
        try {
            return xmlFile.getCanonicalPath();
        } catch (IOException e) {
            DeLog.e(TAG, "getXmlPath() IOException " + e);
            return null;
        }
    }

    private boolean parseXml(String xmlPath) {
        if (xmlPath == null) {
            DeLog.w(TAG, "parseXml() error! xmlPath is null");
            return false;
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlPath);
        registerElement(xmlParser);
        if (!xmlParser.parse()) {
            DeLog.e(TAG, "parseXml() error! xmlParser.parse() failed!");
            return false;
        } else if (!xmlParser.check()) {
            DeLog.e(TAG, "parseXml() error! xmlParser.check() failed!");
            return false;
        } else {
            DeLog.i(TAG, "parseXml() load success!");
            return true;
        }
    }

    private void loadDefaultConfig() {
        DeLog.i(TAG, "loadDefaultConfig()");
        mEnable = true;
        mAlgoWideColorSpaceEnable = false;
        mAlgoSkinBeautyEnable = false;
        mAlgoVividEnable = false;
        mNeedNormallizeColorSpace = false;
        mNormallizeColorGamut = ImageProcessor.ColorspaceType.SRGB;
        mThumbnailStrategy.clear();
        mThumbnailStrategy.put(ImageProcessor.ThumbnailType.ANIMATION, new ThumbnailStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE)));
        mThumbnailStrategy.put(ImageProcessor.ThumbnailType.FULLSCREEN, new ThumbnailStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE, ImageProcessor.AlgoType.SR, ImageProcessor.AlgoType.SHARPNESS), true));
        mThumbnailStrategy.put(ImageProcessor.ThumbnailType.HALFSCREEN, new ThumbnailStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE), true));
        mThumbnailStrategy.put(ImageProcessor.ThumbnailType.DEFAULT, new ThumbnailStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE)));
        mTileStrategy.clear();
        mTileStrategy.put(ImageProcessor.TileEngineType.NON_SR, new TileStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE)));
        mTileStrategy.put(ImageProcessor.TileEngineType.SR, new TileStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.SR, ImageProcessor.AlgoType.SHARPNESS)));
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement rootElement = parser.registerRootElement(new Element_Config(null));
        rootElement.registerChildElement(new Element_Enable(null));
        rootElement.registerChildElement(new Element_Custom(null)).registerChildElement(new Element_CustomGroup(null));
        HwXmlElement thumbnail = rootElement.registerChildElement(new Element_Thumbnail(null));
        thumbnail.registerChildElement(new Element_Thumbnail_Algo(null));
        thumbnail.registerChildElement(new Element_Thumbnail_SaveCommonInfo(null));
        rootElement.registerChildElement(new Element_Tile(null)).registerChildElement(new Element_Tile_Algo(null));
    }

    /* access modifiers changed from: private */
    public static class Element_Config extends HwXmlElement {
        private Element_Config() {
        }

        /* synthetic */ Element_Config(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "Config";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_Enable extends HwXmlElement {
        private Element_Enable() {
        }

        /* synthetic */ Element_Enable(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "Enable";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            boolean unused = ImageProcessorAlgoStrategy.mEnable = string2Boolean(parser.nextText());
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_Custom extends HwXmlElement {
        private Element_Custom() {
        }

        /* synthetic */ Element_Custom(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "Custom";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_CustomGroup extends HwXmlElement {
        private Element_CustomGroup() {
        }

        /* synthetic */ Element_CustomGroup(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "CustomGroup";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public List<String> getNameList() {
            return Arrays.asList("AlgoWideColorSpace", "AlgoSkinBeauty", "AlgoVivid", "NormallizeColorGamut");
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2091365941:
                    if (valueName.equals("AlgoVivid")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1723209940:
                    if (valueName.equals("AlgoSkinBeauty")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1052144663:
                    if (valueName.equals("AlgoWideColorSpace")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1255667550:
                    if (valueName.equals("NormallizeColorGamut")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                boolean unused = ImageProcessorAlgoStrategy.mAlgoWideColorSpaceEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                boolean unused2 = ImageProcessorAlgoStrategy.mAlgoSkinBeautyEnable = string2Boolean(parser.nextText());
            } else if (c == 2) {
                boolean unused3 = ImageProcessorAlgoStrategy.mAlgoVividEnable = string2Boolean(parser.nextText());
            } else if (c != 3) {
                DeLog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                String gamutName = parser.nextText();
                if (gamutName != null && gamutName.equals("Super Gamut")) {
                    ImageProcessor.ColorspaceType unused4 = ImageProcessorAlgoStrategy.mNormallizeColorGamut = ImageProcessor.ColorspaceType.SUPER_GAMUT;
                }
                boolean unused5 = ImageProcessorAlgoStrategy.mNeedNormallizeColorSpace = true;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean checkValue() {
            return ImageProcessorAlgoStrategy.mNormallizeColorGamut != null;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_Thumbnail extends HwXmlElement {
        private Element_Thumbnail() {
        }

        /* synthetic */ Element_Thumbnail(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "Thumbnail";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            ImageProcessor.ThumbnailType unused = ImageProcessorAlgoStrategy.mCurrentThumbnailType = ImageProcessor.ThumbnailType.valueOf(parser.getAttributeValue(null, TpCommandConstant.TOUCH_REGION_TYPE));
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_Thumbnail_Algo extends HwXmlElement {
        private Element_Thumbnail_Algo() {
        }

        /* synthetic */ Element_Thumbnail_Algo(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "Algo";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            String imageName = parser.getAttributeValue(null, "image");
            ImageProcessor.ImageType imageType = ImageProcessor.ImageType.NORMAL;
            if (imageName != null) {
                imageType = ImageProcessor.ImageType.valueOf(imageName);
            }
            if (!ImageProcessorAlgoStrategy.isImageTypeEnable(imageType)) {
                return true;
            }
            ImageProcessor.AlgoType algoType = ImageProcessor.AlgoType.valueOf(parser.nextText());
            ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) ImageProcessorAlgoStrategy.mThumbnailStrategy.get(ImageProcessorAlgoStrategy.mCurrentThumbnailType);
            if (thumbnailStrategy == null) {
                ImageProcessorAlgoStrategy.mThumbnailStrategy.put(ImageProcessorAlgoStrategy.mCurrentThumbnailType, new ThumbnailStrategy(imageType, EnumSet.of(algoType)));
            } else if (thumbnailStrategy.mAlgos == null) {
                thumbnailStrategy.mAlgos = new HashMap();
                thumbnailStrategy.mAlgos.put(imageType, EnumSet.of(algoType));
            } else {
                Set<ImageProcessor.AlgoType> set = thumbnailStrategy.mAlgos.get(imageType);
                if (set == null) {
                    thumbnailStrategy.mAlgos.put(imageType, EnumSet.of(algoType));
                } else {
                    set.add(algoType);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_Thumbnail_SaveCommonInfo extends HwXmlElement {
        private Element_Thumbnail_SaveCommonInfo() {
        }

        /* synthetic */ Element_Thumbnail_SaveCommonInfo(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "SaveCommonInfo";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            boolean isSaveCommonInfo = string2Boolean(parser.nextText());
            if (!isSaveCommonInfo) {
                return true;
            }
            ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) ImageProcessorAlgoStrategy.mThumbnailStrategy.get(ImageProcessorAlgoStrategy.mCurrentThumbnailType);
            if (thumbnailStrategy == null) {
                ImageProcessorAlgoStrategy.mThumbnailStrategy.put(ImageProcessorAlgoStrategy.mCurrentThumbnailType, new ThumbnailStrategy(null, null, isSaveCommonInfo));
            } else {
                if (thumbnailStrategy.mAlgos != null) {
                    thumbnailStrategy.mCommonInfoAlgos = new HashMap();
                    for (Map.Entry<ImageProcessor.ImageType, Set<ImageProcessor.AlgoType>> entry : thumbnailStrategy.mAlgos.entrySet()) {
                        thumbnailStrategy.mCommonInfoAlgos.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
                    }
                }
                thumbnailStrategy.mIsSaveCommonInfo = isSaveCommonInfo;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_Tile extends HwXmlElement {
        private Element_Tile() {
        }

        /* synthetic */ Element_Tile(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "Tile";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            ImageProcessor.TileEngineType unused = ImageProcessorAlgoStrategy.mCurrentTileType = ImageProcessor.TileEngineType.valueOf(parser.getAttributeValue(null, TpCommandConstant.TOUCH_REGION_TYPE));
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_Tile_Algo extends HwXmlElement {
        private Element_Tile_Algo() {
        }

        /* synthetic */ Element_Tile_Algo(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "Algo";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            String imageName = parser.getAttributeValue(null, "image");
            ImageProcessor.ImageType imageType = ImageProcessor.ImageType.NORMAL;
            if (imageName != null) {
                imageType = ImageProcessor.ImageType.valueOf(imageName);
            }
            if (!ImageProcessorAlgoStrategy.isImageTypeEnable(imageType)) {
                return true;
            }
            ImageProcessor.AlgoType algoType = ImageProcessor.AlgoType.valueOf(parser.nextText());
            TileStrategy tileStrategy = (TileStrategy) ImageProcessorAlgoStrategy.mTileStrategy.get(ImageProcessorAlgoStrategy.mCurrentTileType);
            if (tileStrategy == null) {
                ImageProcessorAlgoStrategy.mTileStrategy.put(ImageProcessorAlgoStrategy.mCurrentTileType, new TileStrategy(imageType, EnumSet.of(algoType)));
            } else if (tileStrategy.mAlgos == null) {
                tileStrategy.mAlgos = new HashMap();
                tileStrategy.mAlgos.put(imageType, EnumSet.of(algoType));
            } else {
                Set<ImageProcessor.AlgoType> set = tileStrategy.mAlgos.get(imageType);
                if (set == null) {
                    tileStrategy.mAlgos.put(imageType, EnumSet.of(algoType));
                } else {
                    set.add(algoType);
                }
            }
            return true;
        }
    }
}
