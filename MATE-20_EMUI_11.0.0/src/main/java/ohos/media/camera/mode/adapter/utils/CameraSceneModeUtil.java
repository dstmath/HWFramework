package ohos.media.camera.mode.adapter.utils;

import android.util.ArrayMap;
import java.util.Map;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class CameraSceneModeUtil {
    private static final int ANIMOJI_GIF_MODE_TAG = 38;
    private static final int ANIMOJI_VIDEO_MODE_TAG = 38;
    private static final int APERTURE_WHITE_BLACK_TAG = 34;
    private static final int ARTIST_FILTER_TAG = 21;
    private static final int AR_3DANIMOJI_GIF_MODE_TAG = 39;
    private static final int AR_3DANIMOJI_VIDEO_MODE_TAG = 39;
    private static final int AR_3D_OBJECT_PHOTO_MODE_TAG = 25;
    private static final int AR_3D_OBJECT_VIDEO_MODE_TAG = 25;
    private static final int AR_CARTOON_PHOTO_MODE_TAG = 27;
    private static final int AR_CARTOON_VIDEO_MODE_TAG = 27;
    private static final int AR_GESTURE_PHOTO_MODE_TAG = 40;
    private static final int AR_GESTURE_VIDEO_MODE_TAG = 40;
    private static final int AR_STAR_PHOTO_MODE_TAG = 26;
    private static final int AR_STAR_VIDEO_MODE_TAG = 26;
    private static final int BACKGROUND_PHOTO_MODE_TAG = 41;
    private static final int BACKGROUND_VIDEO_MODE_TAG = 41;
    private static final int BACK_PANORAMA_TAG = 8;
    private static final int BEAUTY_TAG = 23;
    private static final int BEAUTY_VIDEO_TAG = 30;
    private static final int BEAUTY_WHITE_BLACK_TAG = 35;
    private static final int COSPLAY_GIF_MODE_TAG = 41;
    private static final int COSPLAY_PHOTO_MODE_TAG = 22;
    private static final int D3D_MODEL_TAG = 6;
    private static final int DOCUMENT_RECOGNITION_TAG = 15;
    private static final int DUAL_VIDEO_MODE_RANK = 50;
    private static final int FILTER_EFFECT_TAG = 17;
    private static final int FILTER_EFFECT_VIDEO_TAG = 37;
    private static final int FOOD_TAG = 18;
    private static final int FRONT_PANORAMA_TAG = 8;
    private static final int HDR_PHOTO_TAG = 5;
    private static final int LIGHT_PAINTING_CAR_TAG = 9;
    private static final int LIGHT_PAINTING_LIGHT_TAG = 12;
    private static final int LIGHT_PAINTING_STAR_TAG = 10;
    private static final int LIGHT_PAINTING_TAG = 9;
    private static final int LIGHT_PAINTING_WATER_TAG = 11;
    private static final int LIVE_PHOTO_TAG = 20;
    private static final int NAME_COSPLAY_PHOTO_MODE_TAG = 41;
    private static final int NAME_COSPLAY_VIDEO_MODE_TAG = 41;
    private static final int NORMAL_BURST_TAG = 1;
    private static final int NORMAL_PHOTO_TAG = 0;
    private static final int NORMAL_VIDEO_TAG = 28;
    private static final int OBJECT_RECOGNITION_TAG = 24;
    private static final int PANORAMA_3D_TAG = 3;
    private static final int PRO_PHOTO_MODE_TAG = 2;
    private static final int PRO_VIDEO_MODE_TAG = 31;
    private static final int PRO_WHITE_BLACK_PHOTO_MODE = 36;
    private static final int SLOW_MOTION_TAG = 33;
    private static final int SMART_BEAUTY_TAG = 23;
    private static final int SMART_SUPER_NIGHT_TAG = 7;
    private static final int SMART_WIDE_APERTURE_PHOTO_TAG = 19;
    private static final int SUPER_CAMERA_BACK_LIGHT_TAG = 44;
    private static final int SUPER_CAMERA_NIGHT_TAG = 43;
    private static final int SUPER_CAMERA_OTHERS_TAG = 42;
    private static final int SUPER_CAMERA_PORTRAIT_TAG = 46;
    private static final int SUPER_CAMERA_TAG = 42;
    private static final int SUPER_CAMERA_WATER_TAG = 45;
    private static final int SUPER_MACRO_TAG = 47;
    private static final int SUPER_NIGHT_TAG = 7;
    private static final int SUPER_SLOW_MOTION_TAG = 33;
    private static final int TIME_LAPSE_TAG = 32;
    private static final int VOICE_PHOTO_TAG = 14;
    private static final int WATER_MARK_TAG = 13;
    private static final int WHITE_BLACK_TAG = 4;
    private static final int WIDE_APERTURE_PHOTO_TAG = 19;
    private static final int WIDE_APERTURE_VIDEO_TAG = 29;
    private static Map<Integer, String> sceneModeIntegerStringMap = new ArrayMap();
    private static Map<String, Integer> sceneModeMap = new ArrayMap();

    static {
        sceneModeMap.put(ConstantValue.MODE_NAME_NORMAL_PHOTO, 0);
        sceneModeMap.put(ConstantValue.MODE_NAME_HDR_PHOTO, 5);
        sceneModeMap.put(ConstantValue.MODE_NAME_SUPERNIGHT, 7);
        sceneModeMap.put(ConstantValue.MODE_NAME_WIDE_APERTURE_PHOTO, 19);
        sceneModeMap.put(ConstantValue.MODE_NAME_BEAUTY, 23);
        sceneModeMap.put(ConstantValue.MODE_NAME_NORMAL_VIDEO, 28);
        sceneModeMap.put(ConstantValue.MODE_NAME_SUPER_SLOW_MOTION, 33);
        sceneModeMap.put(ConstantValue.MODE_NAME_SLOW_MOTION, 33);
        sceneModeMap.put(ConstantValue.MODE_NAME_PRO_VIDEO_MODE, 31);
        sceneModeMap.put(ConstantValue.MODE_NAME_PRO_PHOTO_MODE, 2);
        for (Map.Entry<String, Integer> entry : sceneModeMap.entrySet()) {
            sceneModeIntegerStringMap.put(entry.getValue(), entry.getKey());
        }
    }

    private CameraSceneModeUtil() {
    }

    public static Map<String, Integer> getSceneModeMap() {
        return sceneModeMap;
    }

    public static Map<Integer, String> getSceneModeIntegerStringMap() {
        return sceneModeIntegerStringMap;
    }
}
