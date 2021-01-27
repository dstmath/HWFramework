package com.huawei.displayengine;

import android.os.SystemProperties;
import android.os.Trace;
import com.huawei.displayengine.ImageProcessor;
import com.huawei.sidetouch.TpCommandConstant;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public class ImageProcessorAlgoStrategy {
    private static final String TAG = "DE J ImageProcessorAlgoStrategy";
    private static final String XML_PATH = "display/effect/algorithm/imageprocessor/ImageProcessStrategy.xml";
    private static ImageProcessor.ThumbnailType sCurrentThumbnailType;
    private static ImageProcessor.TileEngineType sCurrentTileType;
    private static boolean sIsAlgoSkinBeautyEnable;
    private static boolean sIsAlgoVividEnable;
    private static boolean sIsAlgoWideColorSpaceEnable;
    private static boolean sIsEnable;
    private static boolean sIsNeedNormallizeColorSpace;
    private static ImageProcessor.ColorSpaceType sNormallizeColorGamut = ImageProcessor.ColorSpaceType.SRGB;
    private static Map<ImageProcessor.ThumbnailType, ThumbnailStrategy> sThumbnailStrategy = new HashMap(7);
    private static Map<ImageProcessor.TileEngineType, TileStrategy> sTileStrategy = new HashMap(2);

    ImageProcessorAlgoStrategy() {
        DeLog.i(TAG, "ImageProcessorAlgoStrategy enter");
        if (!isFactoryMode()) {
            loadXml();
            initCommonInfoAlgos();
        }
        printStrategy();
        DeLog.i(TAG, "ImageProcessorAlgoStrategy exit");
    }

    private void printStrategy() {
        DeLog.i(TAG, "printStrategy Enable=" + sIsEnable + ", AlgoWideColorSpaceEnable=" + sIsAlgoWideColorSpaceEnable + ", AlgoSkinBeautyEnable=" + sIsAlgoSkinBeautyEnable + ", AlgoVividEnable=" + sIsAlgoVividEnable + ", NeedNormallizeColorSpace=" + sIsNeedNormallizeColorSpace + ", NormallizeColorGamut=" + sNormallizeColorGamut);
        for (Map.Entry<ImageProcessor.ThumbnailType, ThumbnailStrategy> entry : sThumbnailStrategy.entrySet()) {
            ImageProcessor.ThumbnailType thumbnailType = entry.getKey();
            ThumbnailStrategy thumbnailStrategy = entry.getValue();
            if (thumbnailStrategy.mIsSaveCommonInfo) {
                DeLog.i(TAG, "printStrategy Thumbnail: " + thumbnailType + ", Algo:" + thumbnailStrategy.mAlgos + ", CommonInfoAlgo:" + thumbnailStrategy.mCommonInfoAlgos);
            } else {
                DeLog.i(TAG, "printStrategy Thumbnail: " + thumbnailType + ", Algo:" + thumbnailStrategy.mAlgos);
            }
        }
        for (Map.Entry<ImageProcessor.TileEngineType, TileStrategy> entry2 : sTileStrategy.entrySet()) {
            DeLog.i(TAG, "printStrategy Tile: " + entry2.getKey() + ", Algo:" + entry2.getValue().mAlgos);
        }
    }

    private void initCommonInfoAlgos() {
        DeLog.d(TAG, "initCommonInfoAlgos");
        ImageProcessor.ImageType[] values = ImageProcessor.ImageType.values();
        for (ImageProcessor.ImageType imageType : values) {
            if (isImageTypeEnable(imageType)) {
                Set<ImageProcessor.AlgoType> algos = EnumSet.noneOf(ImageProcessor.AlgoType.class);
                for (ImageProcessor.TileEngineType tileEngineType : sTileStrategy.keySet()) {
                    algos.addAll(getTileAlgos(tileEngineType, imageType));
                }
                if (!algos.isEmpty()) {
                    for (ThumbnailStrategy thumbnailStrategy : sThumbnailStrategy.values()) {
                        if (thumbnailStrategy.mIsSaveCommonInfo) {
                            if (thumbnailStrategy.mCommonInfoAlgos == null) {
                                thumbnailStrategy.mCommonInfoAlgos = new HashMap(4);
                            }
                            Set<ImageProcessor.AlgoType> set = thumbnailStrategy.mCommonInfoAlgos.get(imageType);
                            if (set == null) {
                                thumbnailStrategy.mCommonInfoAlgos.put(imageType, algos);
                            } else {
                                set.addAll(algos);
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
            return sIsAlgoWideColorSpaceEnable;
        }
        if (i == 3) {
            return sIsAlgoSkinBeautyEnable;
        }
        if (i != 4) {
            return false;
        }
        return sIsAlgoVividEnable;
    }

    /* access modifiers changed from: package-private */
    public boolean isImageProcessorEnable() {
        return sIsEnable;
    }

    /* access modifiers changed from: package-private */
    public boolean isAlgoWideColorSpaceEnable() {
        return sIsAlgoWideColorSpaceEnable;
    }

    /* access modifiers changed from: package-private */
    public boolean isAlgoSkinBeautyEnable() {
        return sIsAlgoSkinBeautyEnable;
    }

    /* access modifiers changed from: package-private */
    public boolean isAlgoVividEnable() {
        return sIsAlgoVividEnable;
    }

    /* access modifiers changed from: package-private */
    public boolean needNormallizeColorSpace() {
        return sIsNeedNormallizeColorSpace;
    }

    /* access modifiers changed from: package-private */
    public ImageProcessor.ColorSpaceType getNormallizeColorGamut() {
        return sNormallizeColorGamut;
    }

    /* access modifiers changed from: private */
    public static class ThumbnailStrategy {
        Map<ImageProcessor.ImageType, Set<ImageProcessor.AlgoType>> mAlgos;
        Map<ImageProcessor.ImageType, Set<ImageProcessor.AlgoType>> mCommonInfoAlgos;
        boolean mIsSaveCommonInfo;

        ThumbnailStrategy(ImageProcessor.ImageType imageType, Set<ImageProcessor.AlgoType> algos) {
            this(imageType, algos, false);
        }

        ThumbnailStrategy(ImageProcessor.ImageType imageType, Set<ImageProcessor.AlgoType> algos, boolean isSaveCommonInfo) {
            this.mIsSaveCommonInfo = isSaveCommonInfo;
            if (imageType != null && algos != null) {
                this.mAlgos = new HashMap(4);
                this.mAlgos.put(imageType, algos);
                if (isSaveCommonInfo) {
                    this.mCommonInfoAlgos = new HashMap(4);
                    this.mCommonInfoAlgos.put(imageType, EnumSet.copyOf(algos));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean needRunSoftwareAlgo(ImageProcessor.ThumbnailType thumbnailType, ImageProcessor.ImageType imageType) {
        return !getThumbnailAlgos(thumbnailType, imageType).isEmpty() || !getCommonInfoAlgos(thumbnailType, imageType).isEmpty();
    }

    /* access modifiers changed from: package-private */
    public boolean needSaveCommonInfo(ImageProcessor.ThumbnailType thumbnailType) {
        ThumbnailStrategy thumbnailStrategy = sThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null) {
            return false;
        }
        return thumbnailStrategy.mIsSaveCommonInfo;
    }

    /* access modifiers changed from: package-private */
    public Set<ImageProcessor.AlgoType> getThumbnailAlgos(ImageProcessor.ThumbnailType thumbnailType, ImageProcessor.ImageType imageType) {
        ThumbnailStrategy thumbnailStrategy = sThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null || thumbnailStrategy.mAlgos == null) {
            return Collections.emptySet();
        }
        Set<ImageProcessor.AlgoType> set = thumbnailStrategy.mAlgos.get(imageType);
        if (set != null && !set.isEmpty()) {
            return set;
        }
        if (imageType != ImageProcessor.ImageType.NORMAL) {
            set = thumbnailStrategy.mAlgos.get(ImageProcessor.ImageType.NORMAL);
        }
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }

    /* access modifiers changed from: package-private */
    public Set<ImageProcessor.AlgoType> getCommonInfoAlgos(ImageProcessor.ThumbnailType thumbnailType, ImageProcessor.ImageType imageType) {
        ThumbnailStrategy thumbnailStrategy = sThumbnailStrategy.get(thumbnailType);
        if (thumbnailStrategy == null) {
            return Collections.emptySet();
        }
        if (thumbnailStrategy.mCommonInfoAlgos == null) {
            return getThumbnailAlgos(thumbnailType, imageType);
        }
        Set<ImageProcessor.AlgoType> set = thumbnailStrategy.mCommonInfoAlgos.get(imageType);
        if (set != null && !set.isEmpty()) {
            return set;
        }
        if (imageType != ImageProcessor.ImageType.NORMAL) {
            set = thumbnailStrategy.mCommonInfoAlgos.get(ImageProcessor.ImageType.NORMAL);
        }
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }

    /* access modifiers changed from: package-private */
    public static class TileStrategy {
        Map<ImageProcessor.ImageType, Set<ImageProcessor.AlgoType>> mAlgos;

        TileStrategy(ImageProcessor.ImageType imageType, Set<ImageProcessor.AlgoType> algos) {
            if (imageType != null && algos != null) {
                this.mAlgos = new HashMap(4);
                this.mAlgos.put(imageType, algos);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean needRunSoftwareAlgo(ImageProcessor.TileEngineType tileEngineType, ImageProcessor.ImageType imageType) {
        return !getTileAlgos(tileEngineType, imageType).isEmpty();
    }

    /* access modifiers changed from: package-private */
    public Set<ImageProcessor.AlgoType> getTileEngineAlgos(ImageProcessor.TileEngineType tileEngineType) {
        TileStrategy tileStrategy = sTileStrategy.get(tileEngineType);
        if (tileStrategy == null || tileStrategy.mAlgos == null) {
            return Collections.emptySet();
        }
        Set<ImageProcessor.AlgoType> set = EnumSet.noneOf(ImageProcessor.AlgoType.class);
        for (Set<ImageProcessor.AlgoType> value : tileStrategy.mAlgos.values()) {
            set.addAll(value);
        }
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }

    /* access modifiers changed from: package-private */
    public Set<ImageProcessor.AlgoType> getTileAlgos(ImageProcessor.TileEngineType tileEngineType, ImageProcessor.ImageType imageType) {
        TileStrategy tileStrategy = sTileStrategy.get(tileEngineType);
        if (tileStrategy == null || tileStrategy.mAlgos == null) {
            return Collections.emptySet();
        }
        Set<ImageProcessor.AlgoType> set = tileStrategy.mAlgos.get(imageType);
        if (set != null && !set.isEmpty()) {
            return set;
        }
        if (imageType != ImageProcessor.ImageType.NORMAL) {
            set = tileStrategy.mAlgos.get(ImageProcessor.ImageType.NORMAL);
        }
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }

    private boolean isFactoryMode() {
        return "factory".equals(SystemProperties.get("ro.runmode", "factory"));
    }

    private void loadXml() {
        DeLog.d(TAG, "loadXml()");
        Trace.traceBegin(8, "loadXml");
        try {
            if (!parseXml(getXmlPath())) {
                loadDefaultConfig();
            }
        } catch (RuntimeException e) {
            DeLog.e(TAG, "loadXml RuntimeException " + e);
            loadDefaultConfig();
        }
        Trace.traceEnd(8);
    }

    private Optional<String> getXmlPath() {
        File xmlFile = HwCfgFilePolicy.getCfgFile(XML_PATH, 0);
        if (xmlFile == null) {
            DeLog.w(TAG, "getXmlPath() error! can't find xml file.display/effect/algorithm/imageprocessor/ImageProcessStrategy.xml");
            return Optional.empty();
        }
        String xmlPath = null;
        try {
            xmlPath = xmlFile.getCanonicalPath();
        } catch (IOException e) {
            DeLog.e(TAG, "getXmlPath() IOException " + e);
        }
        return Optional.ofNullable(xmlPath);
    }

    private boolean parseXml(Optional<String> xmlPath) {
        if (!xmlPath.isPresent()) {
            DeLog.w(TAG, "parseXml() error! xmlPath is null");
            return false;
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlPath.get());
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
        sIsEnable = true;
        sIsAlgoWideColorSpaceEnable = false;
        sIsAlgoSkinBeautyEnable = false;
        sIsAlgoVividEnable = false;
        sIsNeedNormallizeColorSpace = false;
        sNormallizeColorGamut = ImageProcessor.ColorSpaceType.SRGB;
        sThumbnailStrategy.clear();
        sThumbnailStrategy.put(ImageProcessor.ThumbnailType.ANIMATION, new ThumbnailStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE)));
        sThumbnailStrategy.put(ImageProcessor.ThumbnailType.FULLSCREEN, new ThumbnailStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE, ImageProcessor.AlgoType.SR, ImageProcessor.AlgoType.SHARPNESS), true));
        sThumbnailStrategy.put(ImageProcessor.ThumbnailType.HALFSCREEN, new ThumbnailStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE), true));
        sThumbnailStrategy.put(ImageProcessor.ThumbnailType.DEFAULT, new ThumbnailStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE)));
        sTileStrategy.clear();
        sTileStrategy.put(ImageProcessor.TileEngineType.NON_SR, new TileStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.ACE)));
        sTileStrategy.put(ImageProcessor.TileEngineType.SR, new TileStrategy(ImageProcessor.ImageType.NORMAL, EnumSet.of(ImageProcessor.AlgoType.SR, ImageProcessor.AlgoType.SHARPNESS)));
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement rootElement = parser.registerRootElement(new ElementConfig(null));
        rootElement.registerChildElement(new ElementEnable(null));
        rootElement.registerChildElement(new ElementCustom(null)).registerChildElement(new ElementCustomGroup(null));
        HwXmlElement thumbnail = rootElement.registerChildElement(new ElementThumbnail(null));
        thumbnail.registerChildElement(new ElementThumbnailAlgo(null));
        thumbnail.registerChildElement(new ElementThumbnailSaveCommonInfo(null));
        rootElement.registerChildElement(new ElementTile(null)).registerChildElement(new ElementTileAlgo(null));
    }

    /* access modifiers changed from: private */
    public static class ElementConfig extends HwXmlElement {
        private ElementConfig() {
        }

        /* synthetic */ ElementConfig(AnonymousClass1 x0) {
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
    public static class ElementEnable extends HwXmlElement {
        private ElementEnable() {
        }

        /* synthetic */ ElementEnable(AnonymousClass1 x0) {
            this();
        }

        @Override // com.huawei.displayengine.HwXmlElement
        public String getName() {
            return "Enable";
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            boolean unused = ImageProcessorAlgoStrategy.sIsEnable = string2Boolean(parser.nextText());
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementCustom extends HwXmlElement {
        private ElementCustom() {
        }

        /* synthetic */ ElementCustom(AnonymousClass1 x0) {
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
    public static class ElementCustomGroup extends HwXmlElement {
        private ElementCustomGroup() {
        }

        /* synthetic */ ElementCustomGroup(AnonymousClass1 x0) {
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

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            String valueName = parser.getName();
            if (valueName == null) {
                DeLog.e(this.TAG, "get valueName is null");
                return false;
            }
            char c = 65535;
            switch (valueName.hashCode()) {
                case -2091365941:
                    if (valueName.equals("AlgoVivid")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1723209940:
                    if (valueName.equals("AlgoSkinBeauty")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1052144663:
                    if (valueName.equals("AlgoWideColorSpace")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1255667550:
                    if (valueName.equals("NormallizeColorGamut")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                boolean unused = ImageProcessorAlgoStrategy.sIsAlgoWideColorSpaceEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                boolean unused2 = ImageProcessorAlgoStrategy.sIsAlgoSkinBeautyEnable = string2Boolean(parser.nextText());
            } else if (c == 2) {
                boolean unused3 = ImageProcessorAlgoStrategy.sIsAlgoVividEnable = string2Boolean(parser.nextText());
            } else if (c != 3) {
                DeLog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                String gamutName = parser.nextText();
                boolean unused4 = ImageProcessorAlgoStrategy.sIsNeedNormallizeColorSpace = true;
                DeLog.i(this.TAG, "GamutName = " + gamutName);
                if ("Super Gamut".equals(gamutName)) {
                    ImageProcessor.ColorSpaceType unused5 = ImageProcessorAlgoStrategy.sNormallizeColorGamut = ImageProcessor.ColorSpaceType.SUPER_GAMUT;
                } else if ("SRGB".equals(gamutName)) {
                    ImageProcessor.ColorSpaceType unused6 = ImageProcessorAlgoStrategy.sNormallizeColorGamut = ImageProcessor.ColorSpaceType.SRGB;
                } else if ("P3".equals(gamutName)) {
                    ImageProcessor.ColorSpaceType unused7 = ImageProcessorAlgoStrategy.sNormallizeColorGamut = ImageProcessor.ColorSpaceType.DISPLAY_P3;
                } else {
                    DeLog.e(this.TAG, "Unsupported gamut. GamutName = " + gamutName);
                    boolean unused8 = ImageProcessorAlgoStrategy.sIsNeedNormallizeColorSpace = false;
                }
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.HwXmlElement
        public boolean checkValue() {
            return ImageProcessorAlgoStrategy.sNormallizeColorGamut != null;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementThumbnail extends HwXmlElement {
        private ElementThumbnail() {
        }

        /* synthetic */ ElementThumbnail(AnonymousClass1 x0) {
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
            ImageProcessor.ThumbnailType unused = ImageProcessorAlgoStrategy.sCurrentThumbnailType = ImageProcessor.ThumbnailType.valueOf(parser.getAttributeValue(null, TpCommandConstant.TOUCH_REGION_TYPE));
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementThumbnailAlgo extends HwXmlElement {
        private ElementThumbnailAlgo() {
        }

        /* synthetic */ ElementThumbnailAlgo(AnonymousClass1 x0) {
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
            ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) ImageProcessorAlgoStrategy.sThumbnailStrategy.get(ImageProcessorAlgoStrategy.sCurrentThumbnailType);
            if (thumbnailStrategy == null) {
                ImageProcessorAlgoStrategy.sThumbnailStrategy.put(ImageProcessorAlgoStrategy.sCurrentThumbnailType, new ThumbnailStrategy(imageType, EnumSet.of(algoType)));
            } else if (thumbnailStrategy.mAlgos == null) {
                thumbnailStrategy.mAlgos = new HashMap(4);
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
    public static class ElementThumbnailSaveCommonInfo extends HwXmlElement {
        private ElementThumbnailSaveCommonInfo() {
        }

        /* synthetic */ ElementThumbnailSaveCommonInfo(AnonymousClass1 x0) {
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
            ThumbnailStrategy thumbnailStrategy = (ThumbnailStrategy) ImageProcessorAlgoStrategy.sThumbnailStrategy.get(ImageProcessorAlgoStrategy.sCurrentThumbnailType);
            if (thumbnailStrategy == null) {
                ImageProcessorAlgoStrategy.sThumbnailStrategy.put(ImageProcessorAlgoStrategy.sCurrentThumbnailType, new ThumbnailStrategy(null, null, isSaveCommonInfo));
            } else {
                if (thumbnailStrategy.mAlgos != null) {
                    thumbnailStrategy.mCommonInfoAlgos = new HashMap(4);
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
    public static class ElementTile extends HwXmlElement {
        private ElementTile() {
        }

        /* synthetic */ ElementTile(AnonymousClass1 x0) {
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
            ImageProcessor.TileEngineType unused = ImageProcessorAlgoStrategy.sCurrentTileType = ImageProcessor.TileEngineType.valueOf(parser.getAttributeValue(null, TpCommandConstant.TOUCH_REGION_TYPE));
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementTileAlgo extends HwXmlElement {
        private ElementTileAlgo() {
        }

        /* synthetic */ ElementTileAlgo(AnonymousClass1 x0) {
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
            TileStrategy tileStrategy = (TileStrategy) ImageProcessorAlgoStrategy.sTileStrategy.get(ImageProcessorAlgoStrategy.sCurrentTileType);
            if (tileStrategy == null) {
                ImageProcessorAlgoStrategy.sTileStrategy.put(ImageProcessorAlgoStrategy.sCurrentTileType, new TileStrategy(imageType, EnumSet.of(algoType)));
            } else if (tileStrategy.mAlgos == null) {
                tileStrategy.mAlgos = new HashMap(4);
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
