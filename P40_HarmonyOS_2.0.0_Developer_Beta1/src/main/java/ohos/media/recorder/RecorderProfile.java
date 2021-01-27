package ohos.media.recorder;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class RecorderProfile {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(RecorderProfile.class);
    public static final int QUALITY_LEVEL_1080P = 6;
    public static final int QUALITY_LEVEL_2160P = 8;
    public static final int QUALITY_LEVEL_2K = 12;
    public static final int QUALITY_LEVEL_480P = 4;
    public static final int QUALITY_LEVEL_4KDCI = 10;
    public static final int QUALITY_LEVEL_720P = 5;
    public static final int QUALITY_LEVEL_CIF = 3;
    public static final int QUALITY_LEVEL_HIGH = 1;
    public static final int QUALITY_LEVEL_HIGH_SPEED_1080P = 2004;
    public static final int QUALITY_LEVEL_HIGH_SPEED_2160P = 2005;
    public static final int QUALITY_LEVEL_HIGH_SPEED_480P = 2002;
    public static final int QUALITY_LEVEL_HIGH_SPEED_4KDCI = 2008;
    public static final int QUALITY_LEVEL_HIGH_SPEED_720P = 2003;
    public static final int QUALITY_LEVEL_HIGH_SPEED_CIF = 2006;
    public static final int QUALITY_LEVEL_HIGH_SPEED_HIGH = 2001;
    private static final int QUALITY_LEVEL_HIGH_SPEED_LIST_END = 2008;
    private static final int QUALITY_LEVEL_HIGH_SPEED_LIST_START = 2000;
    public static final int QUALITY_LEVEL_HIGH_SPEED_LOW = 2000;
    public static final int QUALITY_LEVEL_HIGH_SPEED_VGA = 2007;
    private static final int QUALITY_LEVEL_LIST_END = 12;
    private static final int QUALITY_LEVEL_LIST_START = 0;
    public static final int QUALITY_LEVEL_LOW = 0;
    public static final int QUALITY_LEVEL_QCIF = 2;
    public static final int QUALITY_LEVEL_QHD = 11;
    public static final int QUALITY_LEVEL_QVGA = 7;
    public static final int QUALITY_LEVEL_TIME_LAPSE_1080P = 1006;
    public static final int QUALITY_LEVEL_TIME_LAPSE_2160P = 1008;
    public static final int QUALITY_LEVEL_TIME_LAPSE_2K = 1012;
    public static final int QUALITY_LEVEL_TIME_LAPSE_480P = 1004;
    public static final int QUALITY_LEVEL_TIME_LAPSE_4KDCI = 1010;
    public static final int QUALITY_LEVEL_TIME_LAPSE_720P = 1005;
    public static final int QUALITY_LEVEL_TIME_LAPSE_CIF = 1003;
    public static final int QUALITY_LEVEL_TIME_LAPSE_HIGH = 1001;
    private static final int QUALITY_LEVEL_TIME_LAPSE_LIST_END = 1012;
    private static final int QUALITY_LEVEL_TIME_LAPSE_LIST_START = 1000;
    public static final int QUALITY_LEVEL_TIME_LAPSE_LOW = 1000;
    public static final int QUALITY_LEVEL_TIME_LAPSE_QCIF = 1002;
    public static final int QUALITY_LEVEL_TIME_LAPSE_QHD = 1011;
    public static final int QUALITY_LEVEL_TIME_LAPSE_QVGA = 1007;
    public static final int QUALITY_LEVEL_TIME_LAPSE_VGA = 1009;
    public static final int QUALITY_LEVEL_VGA = 9;
    private static final int SUCCESS = 0;
    public int aBitRate;
    public int aChannels;
    public int aCodec;
    public int aSampleRate;
    public int durationTime;
    public int fileFormat;
    public int qualityLevel;
    public int vBitRate;
    public int vCodec;
    public int vFrameHeight;
    public int vFrameRate;
    public int vFrameWidth;

    private static native RecorderProfile nativeGetProfile(int i, int i2);

    private static native void nativeInit();

    private static native boolean nativeIsProfile(int i, int i2);

    static {
        System.loadLibrary("zrecorder_profile_jni.z");
        nativeInit();
    }

    public RecorderProfile(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12) {
        this.durationTime = i2;
        this.qualityLevel = i;
        this.fileFormat = i3;
        this.vFrameRate = i9;
        this.vFrameWidth = i11;
        this.vCodec = i8;
        this.vBitRate = i12;
        this.vFrameHeight = i10;
        this.aSampleRate = i6;
        this.aChannels = i7;
        this.aCodec = i4;
        this.aBitRate = i5;
    }

    public static RecorderProfile getParameter(String str, int i) {
        if (str == null) {
            LOGGER.error("getParameter failed, cameraId is null", new Object[0]);
            return null;
        }
        try {
            int parseInt = Integer.parseInt(str);
            if ((i >= 0 && i <= 12) || ((i >= 1000 && i <= 1012) || (i >= 2000 && i <= 2008))) {
                return nativeGetProfile(parseInt, i);
            }
            LOGGER.error("Unsupported quality level: %{public}d", Integer.valueOf(i));
            return null;
        } catch (NumberFormatException unused) {
            LOGGER.error("NumberFormatException", new Object[0]);
            return null;
        }
    }

    public static boolean isProfile(int i, int i2) {
        if (nativeIsProfile(i, i2)) {
            return true;
        }
        LOGGER.error("isProfile failed, error code is false.", new Object[0]);
        return false;
    }
}
