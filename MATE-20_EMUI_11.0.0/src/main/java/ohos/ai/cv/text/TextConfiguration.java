package ohos.ai.cv.text;

import ohos.ai.cv.common.CvRect;
import ohos.ai.cv.common.ParamKey;
import ohos.ai.cv.common.VisionConfiguration;
import ohos.ai.engine.utils.HiAILog;
import ohos.utils.PacMap;

public class TextConfiguration extends VisionConfiguration {
    public static final int AUTO = 0;
    public static final int CHINESE = 1;
    public static final int ENGLISH = 3;
    public static final int FRENCH = 7;
    public static final int GERMAN = 6;
    public static final int ITALIAN = 5;
    public static final int JAPANESE = 9;
    public static final int KOREAN = 10;
    public static final int PORTUGUESE = 4;
    public static final int RUSSIAN = 8;
    public static final int SPANISH = 2;
    private static final String TAG = TextConfiguration.class.getSimpleName();
    public static final int TEXT_DETECT = 1;
    public static final int TEXT_LEVEL_BLOCK = 0;
    public static final int TEXT_LEVEL_CHAR = 2;
    public static final int TEXT_LEVEL_LINE = 1;
    public static final int TEXT_RECOG = 0;
    private int detectMode = 0;
    private int detectType = TextDetectType.TYPE_TEXT_DETECT_SCREEN_SHOT;
    private int isTracking = 0;
    private int language = 0;
    private int level = 0;
    private CvRect roi = null;
    private TextShape textShape = TextShape.TEXT_SHAPE_STRAIGHT;
    private int trackingForceOcr = 0;
    private int trackingRelease = 0;

    public enum TextShape {
        TEXT_SHAPE_STRAIGHT(0),
        TEXT_SHAPE_CURVE(1);
        
        private final int shapeType;

        private TextShape(int i) {
            this.shapeType = i;
        }

        public int getShapeType() {
            return this.shapeType;
        }
    }

    public TextConfiguration(Builder builder) {
        super(builder);
        this.roi = builder.roi;
        this.level = builder.level;
        this.detectType = builder.detectType;
        this.language = builder.language;
        this.detectMode = builder.detectMode;
        this.textShape = builder.textShape;
        this.isTracking = builder.isTracking;
        this.trackingForceOcr = builder.trackingForceOcr;
        this.trackingRelease = builder.trackingRelease;
    }

    public CvRect getRoi() {
        return this.roi;
    }

    public int getLevel() {
        return this.level;
    }

    public int getDetectType() {
        return this.detectType;
    }

    public int getLanguage() {
        return this.language;
    }

    public int getDetectMode() {
        return this.detectMode;
    }

    public TextShape getTextShape() {
        return this.textShape;
    }

    public int getIsTracking() {
        return this.isTracking;
    }

    public int getTrackingForceOcrFlag() {
        return this.trackingForceOcr;
    }

    public int getTrackingRelease() {
        return this.trackingRelease;
    }

    public static class Builder extends VisionConfiguration.Builder<Builder> {
        private int detectMode = 0;
        private int detectType = TextDetectType.TYPE_TEXT_DETECT_SCREEN_SHOT;
        private int isTracking = 0;
        private int language = 0;
        private int level = 0;
        private CvRect roi = null;
        private TextShape textShape = TextShape.TEXT_SHAPE_STRAIGHT;
        private int trackingForceOcr = 0;
        private int trackingRelease = 0;

        /* access modifiers changed from: protected */
        @Override // ohos.ai.cv.common.VisionConfiguration.Builder
        public Builder self() {
            return this;
        }

        public TextConfiguration build() {
            return new TextConfiguration(this);
        }

        public Builder setDetectType(int i) {
            this.detectType = i;
            return self();
        }

        private boolean checkValidRoi(CvRect cvRect) {
            return cvRect != null && cvRect.left >= 0 && cvRect.right > cvRect.left && cvRect.top >= 0 && cvRect.bottom > cvRect.top;
        }

        public Builder setRoi(CvRect cvRect) {
            if (!checkValidRoi(cvRect)) {
                HiAILog.warn(TextConfiguration.TAG, "roi is invalid.");
                return self();
            }
            this.roi = cvRect;
            return self();
        }

        public Builder setLevel(int i) {
            this.level = i;
            return self();
        }

        public Builder setLanguage(int i) {
            this.language = i;
            return self();
        }

        public Builder setDetectMode(int i) {
            this.detectMode = i;
            return self();
        }

        public Builder setTextShape(TextShape textShape2) {
            if (textShape2 == null) {
                textShape2 = TextShape.TEXT_SHAPE_STRAIGHT;
            }
            this.textShape = textShape2;
            return self();
        }

        public Builder setIsTracking(int i) {
            this.isTracking = i;
            return self();
        }

        public Builder setTrackingRelease(int i) {
            this.trackingRelease = i;
            return self();
        }

        public Builder setTrackingForceOcr(int i) {
            this.trackingForceOcr = i;
            return self();
        }
    }

    @Override // ohos.ai.cv.common.VisionConfiguration
    public PacMap getParam() {
        PacMap param = super.getParam();
        param.putObjectValue(TextParamKey.ROI, this.roi);
        param.putIntValue("level", this.level);
        param.putIntValue(TextParamKey.DETECT_TYPE, this.detectType);
        param.putIntValue("language", this.language);
        param.putIntValue(TextParamKey.DETECT_MODE, this.detectMode);
        param.putIntValue(TextParamKey.CURVE_SUPPORT, this.textShape.getShapeType());
        param.putIntValue(TextParamKey.VISION_TRACKER_OCR_TRACKING_FLAG, this.isTracking);
        param.putIntValue(TextParamKey.VISION_TRACKER_OCR_FORCE_OCR_FLAG, this.trackingForceOcr);
        param.putIntValue(TextParamKey.VISION_TRACKER_OCR_RELEASE, this.trackingRelease);
        return param;
    }

    public static TextConfiguration fromPacMap(PacMap pacMap) {
        if (pacMap == null) {
            HiAILog.error(TAG, "Input PacMap is null!");
            return new Builder().build();
        }
        Object obj = pacMap.getAll().get(TextParamKey.ROI);
        if (!(obj instanceof CvRect)) {
            HiAILog.error(TAG, "Failed to get CvRect from input PacMap!");
            return new Builder().build();
        }
        int intValue = pacMap.getIntValue(TextParamKey.CURVE_SUPPORT);
        if (intValue >= 0 && intValue < TextShape.values().length) {
            return ((Builder) ((Builder) ((Builder) ((Builder) ((Builder) new Builder().setAppType(pacMap.getIntValue(ParamKey.APP_TYPE))).setProcessMode(pacMap.getIntValue(ParamKey.PROCESS_MODE))).setClientPkgName(pacMap.getString(ParamKey.CLIENT_PKG_NAME))).setClientState(pacMap.getIntValue(ParamKey.CLIENT_STATE))).setClientVersion(pacMap.getString(ParamKey.CLIENT_VERSION))).setRoi((CvRect) obj).setLevel(pacMap.getIntValue("level")).setDetectType(pacMap.getIntValue(TextParamKey.DETECT_TYPE)).setLanguage(pacMap.getIntValue("language")).setTextShape(TextShape.values()[intValue]).setDetectMode(pacMap.getIntValue(TextParamKey.DETECT_MODE)).setIsTracking(pacMap.getIntValue(TextParamKey.VISION_TRACKER_OCR_TRACKING_FLAG)).setTrackingForceOcr(pacMap.getIntValue(TextParamKey.VISION_TRACKER_OCR_FORCE_OCR_FLAG)).setTrackingRelease(pacMap.getIntValue(TextParamKey.VISION_TRACKER_OCR_RELEASE)).build();
        }
        HiAILog.error(TAG, "Failed to get TextShape from input PacMap!");
        return new Builder().build();
    }
}
