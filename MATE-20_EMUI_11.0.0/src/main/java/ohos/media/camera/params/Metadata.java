package ohos.media.camera.params;

public final class Metadata {

    public @interface AeMode {
        public static final int AE_MODE_OFF = 0;
        public static final int AE_MODE_ON = 1;
    }

    public @interface AeTrigger {
        public static final int AE_TRIGGER_CANCEL = 2;
        public static final int AE_TRIGGER_NONE = 0;
        public static final int AE_TRIGGER_START = 1;
    }

    public @interface AfMode {
        public static final int AF_MODE_CONTINUOUS = 1;
        public static final int AF_MODE_OFF = 0;
        public static final int AF_MODE_TOUCH_LOCK = 2;
    }

    public @interface AfTrigger {
        public static final int AF_TRIGGER_CANCEL = 2;
        public static final int AF_TRIGGER_NONE = 0;
        public static final int AF_TRIGGER_START = 1;
    }

    public @interface AiMovieEffectType {
        public static final byte AI_MOVIE_AICOLOR_EFFECT = 2;
        public static final byte AI_MOVIE_FRESH_EFFECT = 4;
        public static final byte AI_MOVIE_HITCHCOCK_EFFECT = 5;
        public static final byte AI_MOVIE_NOSTALGIA_EFFECT = 3;
        public static final byte AI_MOVIE_NO_EFFECT = 0;
        public static final byte AI_MOVIE_PORTRAIT_FICTITIOUS_EFFECT = 1;
    }

    public @interface AwbType {
        public static final int AWB_AUTO = 1;
        public static final int AWB_CLOUDY = 6;
        public static final int AWB_FLOU_LAMP = 3;
        public static final int AWB_INC_LAMP = 2;
        public static final int AWB_OFF = 0;
        public static final int AWB_SUNNY = 5;
    }

    public @interface BeautySkinToneType {
        public static final byte BEAUTY_COLORS_NONE = -1;
        public static final byte BEAUTY_COLORS_RGB_BF986C = 0;
        public static final byte BEAUTY_COLORS_RGB_E9BB97 = 1;
        public static final byte BEAUTY_COLORS_RGB_EDCDA3 = 2;
        public static final byte BEAUTY_COLORS_RGB_F6CBCF = 7;
        public static final byte BEAUTY_COLORS_RGB_F7D7B3 = 3;
        public static final byte BEAUTY_COLORS_RGB_F7E2D1 = 5;
        public static final byte BEAUTY_COLORS_RGB_FCDEDD = 6;
        public static final byte BEAUTY_COLORS_RGB_FEE6CF = 4;
    }

    public @interface BeautyType {
        public static final byte BEAUTY_BODY_SHAPING = 4;
        public static final byte BEAUTY_FACE_SLENDER = 2;
        public static final byte BEAUTY_SKIN_SMOOTH = 1;
        public static final byte BEAUTY_SKIN_TONE = 3;
    }

    public @interface BokehSpotType {
        public static final byte BOKEHSPOT_CIRCLES = 2;
        public static final byte BOKEHSPOT_DISCS = 15;
        public static final byte BOKEHSPOT_HEARTS = 5;
        public static final byte BOKEHSPOT_OFF = 0;
        public static final byte BOKEHSPOT_SUPER = 17;
        public static final byte BOKEHSPOT_SWIRL = 7;
    }

    public @interface ColorType {
        public static final byte COLOR_BRIGHT = 1;
        public static final byte COLOR_GENERAL = 0;
        public static final byte COLOR_SOFT = 2;
    }

    public @interface FaceDetectionType {
        public static final int FACE_DETECTION = 1;
        public static final int FACE_DETECTION_OFF = 0;
        public static final int FACE_SMILE_DETECTION = 2;
    }

    public @interface FairLightType {
        public static final byte FAIRLIGHT_BUTTERFLY = 2;
        public static final byte FAIRLIGHT_CLASSIC = 5;
        public static final byte FAIRLIGHT_FAIR_POP = 9;
        public static final byte FAIRLIGHT_FOLDING_BLINDS = 10;
        public static final byte FAIRLIGHT_OFF = 0;
        public static final byte FAIRLIGHT_PHOTO_BOOTH = 15;
        public static final byte FAIRLIGHT_SOFT = 1;
        public static final byte FAIRLIGHT_SPLIT = 3;
        public static final byte FAIRLIGHT_STAINED_GLASS = 8;
        public static final byte FAIRLIGHT_THEATRE = 4;
        public static final byte FAIRLIGHT_THEATRE_LIGHT = 16;
    }

    public @interface FilterEffectType {
        public static final byte FILTER_EFFECT_BLUE = 13;
        public static final byte FILTER_EFFECT_CHILDHOOD = 7;
        public static final byte FILTER_EFFECT_DAWN = 6;
        public static final byte FILTER_EFFECT_DUSK = 9;
        public static final byte FILTER_EFFECT_HALO = 8;
        public static final byte FILTER_EFFECT_HANDSOME = 14;
        public static final byte FILTER_EFFECT_ILLUSION = 11;
        public static final byte FILTER_EFFECT_IMPACT = 19;
        public static final byte FILTER_EFFECT_INDIVIDUALITY = 16;
        public static final byte FILTER_EFFECT_MONO = 12;
        public static final byte FILTER_EFFECT_ND = 20;
        public static final byte FILTER_EFFECT_NONE = 0;
        public static final byte FILTER_EFFECT_NOSTALGIA = 3;
        public static final byte FILTER_EFFECT_PURE = 2;
        public static final byte FILTER_EFFECT_SENTIMENTAL = 15;
        public static final byte FILTER_EFFECT_SWEET = 10;
        public static final byte FILTER_EFFECT_VALENCIA = 4;
        public static final byte FILTER_EFFECT_VINTAGE = 5;
    }

    public @interface FlashMode {
        public static final int FLASH_ALWAYS_OPEN = 3;
        public static final int FLASH_AUTO = 0;
        public static final int FLASH_CLOSE = 1;
        public static final int FLASH_OPEN = 2;
    }

    public @interface FocusMode {
        public static final byte AF_CONTINUOUS = 1;
        public static final byte AF_OFF = 0;
        public static final byte AF_TOUCH_AUTO = 2;
        public static final byte AF_TOUCH_LOCK = 3;
    }

    public @interface FpsRange {
        public static final int FPS_120 = 120;
        public static final int FPS_1920 = 1920;
        public static final int FPS_240 = 240;
        public static final int FPS_30 = 30;
        public static final int FPS_480 = 480;
        public static final int FPS_60 = 60;
        public static final int FPS_7680 = 7680;
        public static final int FPS_960 = 960;
    }

    public @interface MeteringType {
        public static final byte METERING_CENTER_WEIGHTED = 2;
        public static final byte METERING_MATRIX = 0;
        public static final byte METERING_SPOT = 3;
    }

    public @interface SceneDetectionType {
        public static final int COMPOSITION_MODE_WIDE_ANGLE_AUTO_SWITCH = 262144;
        public static final int COMPOSITION_MODE_WIDE_ANGLE_SUGGEST = 196608;
        public static final int SMART_SUGGEST_DISABLED = 0;
        public static final int SMART_SUGGEST_MODE_BICYCLE = 29;
        public static final int SMART_SUGGEST_MODE_CAR = 28;
        public static final int SMART_SUGGEST_MODE_CAT = 19;
        public static final int SMART_SUGGEST_MODE_CLASSIC_BUILDING = 26;
        public static final int SMART_SUGGEST_MODE_CLOUDS = 25;
        public static final int SMART_SUGGEST_MODE_DOG = 20;
        public static final int SMART_SUGGEST_MODE_FIREWORKS = 9;
        public static final int SMART_SUGGEST_MODE_FLOWER = 14;
        public static final int SMART_SUGGEST_MODE_FOOD = 7;
        public static final int SMART_SUGGEST_MODE_GROUPPHOTO = 10;
        public static final int SMART_SUGGEST_MODE_LEAVES = 22;
        public static final int SMART_SUGGEST_MODE_MACRO = 3;
        public static final int SMART_SUGGEST_MODE_MOON = 115;
        public static final int SMART_SUGGEST_MODE_MOUNTAIN = 32;
        public static final int SMART_SUGGEST_MODE_NIGHT = 2;
        public static final int SMART_SUGGEST_MODE_ORNAMENTAL_FISH = 33;
        public static final int SMART_SUGGEST_MODE_PANDA = 27;
        public static final int SMART_SUGGEST_MODE_PLANT = 15;
        public static final int SMART_SUGGEST_MODE_PORTRAIT = 1;
        public static final int SMART_SUGGEST_MODE_SAND = 12;
        public static final int SMART_SUGGEST_MODE_SILKYWATER = 11;
        public static final int SMART_SUGGEST_MODE_SKY = 13;
        public static final int SMART_SUGGEST_MODE_SNOW = 16;
        public static final int SMART_SUGGEST_MODE_STAGE = 18;
        public static final int SMART_SUGGEST_MODE_SUNSET = 8;
        public static final int SMART_SUGGEST_MODE_TELE_MACRO = 116;
        public static final int SMART_SUGGEST_MODE_TEXT = 23;
    }

    private Metadata() {
    }
}
