package android.media.hwmnote;

public class HwMnoteInterfaceUtils {
    public static final int HW_MNOTE_IFD_0 = 0;
    static final int HW_MNOTE_IFD_COUNT = 3;
    public static final int HW_MNOTE_IFD_FACE = 2;
    public static final int HW_MNOTE_IFD_SCENE = 1;
    public static final int HW_MNOTE_TAG_BURST_NUMBER = defineTag(0, 513);
    public static final int HW_MNOTE_TAG_CAPTURE_MODE = defineTag(0, 512);
    public static final int HW_MNOTE_TAG_FACE_CONF = defineTag(2, 259);
    public static final int HW_MNOTE_TAG_FACE_COUNT = defineTag(2, 258);
    public static final int HW_MNOTE_TAG_FACE_IFD = defineTag(0, 256);
    public static final int HW_MNOTE_TAG_FACE_LEYE_CENTER = defineTag(2, 262);
    public static final int HW_MNOTE_TAG_FACE_MOUTH_CENTER = defineTag(2, 264);
    public static final int HW_MNOTE_TAG_FACE_RECT = defineTag(2, 261);
    public static final int HW_MNOTE_TAG_FACE_REYE_CENTER = defineTag(2, 263);
    public static final int HW_MNOTE_TAG_FACE_SMILE_SCORE = defineTag(2, 260);
    public static final int HW_MNOTE_TAG_FACE_VERSION = defineTag(2, 257);
    public static final int HW_MNOTE_TAG_FRONT_CAMERA = defineTag(0, 514);
    public static final int HW_MNOTE_TAG_SCENE_BEACH_CONF = defineTag(1, 6);
    public static final int HW_MNOTE_TAG_SCENE_BLUESKY_CONF = defineTag(1, 4);
    public static final int HW_MNOTE_TAG_SCENE_FLOWERS_CONF = defineTag(1, 9);
    public static final int HW_MNOTE_TAG_SCENE_FOOD_CONF = defineTag(1, 2);
    public static final int HW_MNOTE_TAG_SCENE_GREENPLANT_CONF = defineTag(1, 5);
    public static final int HW_MNOTE_TAG_SCENE_IFD = defineTag(0, 0);
    public static final int HW_MNOTE_TAG_SCENE_NIGHT_CONF = defineTag(1, 10);
    public static final int HW_MNOTE_TAG_SCENE_SNOW_CONF = defineTag(1, 7);
    public static final int HW_MNOTE_TAG_SCENE_STAGE_CONF = defineTag(1, 3);
    public static final int HW_MNOTE_TAG_SCENE_SUNSET_CONF = defineTag(1, 8);
    public static final int HW_MNOTE_TAG_SCENE_TEXT_CONF = defineTag(1, 11);
    public static final int HW_MNOTE_TAG_SCENE_VERSION = defineTag(1, 1);
    private static final String TAG = "HwMnoteInterfaceUtils";

    public static int defineTag(int ifdId, short tagId) {
        return (65535 & tagId) | (ifdId << 16);
    }
}
