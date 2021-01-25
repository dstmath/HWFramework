package ohos.media.camera.mode.utils;

import java.util.HashMap;
import java.util.Map;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class ModeNameUtil {
    private static final int DEFAULT_MAP_SIZE = 5;
    private static final Map<Integer, String> MODE_ID_TO_NAME_MAP = new HashMap(5);

    static {
        MODE_ID_TO_NAME_MAP.put(1, ConstantValue.MODE_NAME_NORMAL_PHOTO);
        MODE_ID_TO_NAME_MAP.put(5, ConstantValue.MODE_NAME_NORMAL_VIDEO);
    }

    private ModeNameUtil() {
    }

    public static String getModeNameById(int i) {
        return MODE_ID_TO_NAME_MAP.getOrDefault(Integer.valueOf(i), "");
    }
}
