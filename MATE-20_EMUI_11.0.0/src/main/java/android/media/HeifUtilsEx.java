package android.media;

import java.io.IOException;

public class HeifUtilsEx {
    public static final int ERROR_DECODE = -8;
    public static final int ERROR_EXCEED_JPG_MAX_SIZE = -7;
    public static final int ERROR_HEIF_FILE_NOT_FOUND = -5;
    public static final int ERROR_JPG_FILE_EXISTED = -6;
    public static final int ERROR_OK = 0;
    public static final int ERROR_PARAM_HEIF_FORMAT_WRONG = -3;
    public static final int ERROR_PARAM_HEIF_PATH_NULL = -1;
    public static final int ERROR_PARAM_JPG_FORMAT_WORNG = -4;
    public static final int ERROR_PARAM_JPG_PATH_NULL = -2;

    public static int convertHeifToJpg(String heifPath, String jpgPath) throws IOException {
        return HeifUtils.convertHeifToJpg(heifPath, jpgPath);
    }
}
