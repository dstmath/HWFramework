package android.media;

import android.hardware.Camera;

public class CamcorderProfile {
    public static final int QUALITY_1080P = 6;
    public static final int QUALITY_2160P = 8;
    public static final int QUALITY_480P = 4;
    public static final int QUALITY_720P = 5;
    public static final int QUALITY_CIF = 3;
    public static final int QUALITY_HIGH = 1;
    public static final int QUALITY_HIGH_SPEED_1080P = 2004;
    public static final int QUALITY_HIGH_SPEED_2160P = 2005;
    public static final int QUALITY_HIGH_SPEED_480P = 2002;
    public static final int QUALITY_HIGH_SPEED_720P = 2003;
    public static final int QUALITY_HIGH_SPEED_HIGH = 2001;
    private static final int QUALITY_HIGH_SPEED_LIST_END = 2005;
    private static final int QUALITY_HIGH_SPEED_LIST_START = 2000;
    public static final int QUALITY_HIGH_SPEED_LOW = 2000;
    private static final int QUALITY_LIST_END = 8;
    private static final int QUALITY_LIST_START = 0;
    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_QCIF = 2;
    public static final int QUALITY_QVGA = 7;
    public static final int QUALITY_TIME_LAPSE_1080P = 1006;
    public static final int QUALITY_TIME_LAPSE_2160P = 1008;
    public static final int QUALITY_TIME_LAPSE_480P = 1004;
    public static final int QUALITY_TIME_LAPSE_720P = 1005;
    public static final int QUALITY_TIME_LAPSE_CIF = 1003;
    public static final int QUALITY_TIME_LAPSE_HIGH = 1001;
    private static final int QUALITY_TIME_LAPSE_LIST_END = 1008;
    private static final int QUALITY_TIME_LAPSE_LIST_START = 1000;
    public static final int QUALITY_TIME_LAPSE_LOW = 1000;
    public static final int QUALITY_TIME_LAPSE_QCIF = 1002;
    public static final int QUALITY_TIME_LAPSE_QVGA = 1007;
    public int audioBitRate;
    public int audioChannels;
    public int audioCodec;
    public int audioSampleRate;
    public int duration;
    public int fileFormat;
    public int quality;
    public int videoBitRate;
    public int videoCodec;
    public int videoFrameHeight;
    public int videoFrameRate;
    public int videoFrameWidth;

    private static final native CamcorderProfile native_get_camcorder_profile(int i, int i2);

    private static final native boolean native_has_camcorder_profile(int i, int i2);

    private static final native void native_init();

    public static CamcorderProfile get(int quality2) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 0) {
                return get(i, quality2);
            }
        }
        return null;
    }

    public static CamcorderProfile get(int cameraId, int quality2) {
        if ((quality2 >= 0 && quality2 <= 8) || ((quality2 >= 1000 && quality2 <= 1008) || (quality2 >= 2000 && quality2 <= 2005))) {
            return native_get_camcorder_profile(cameraId, quality2);
        }
        throw new IllegalArgumentException("Unsupported quality level: " + quality2);
    }

    public static boolean hasProfile(int quality2) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 0) {
                return hasProfile(i, quality2);
            }
        }
        return false;
    }

    public static boolean hasProfile(int cameraId, int quality2) {
        return native_has_camcorder_profile(cameraId, quality2);
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    private CamcorderProfile(int duration2, int quality2, int fileFormat2, int videoCodec2, int videoBitRate2, int videoFrameRate2, int videoWidth, int videoHeight, int audioCodec2, int audioBitRate2, int audioSampleRate2, int audioChannels2) {
        this.duration = duration2;
        this.quality = quality2;
        this.fileFormat = fileFormat2;
        this.videoCodec = videoCodec2;
        this.videoBitRate = videoBitRate2;
        this.videoFrameRate = videoFrameRate2;
        this.videoFrameWidth = videoWidth;
        this.videoFrameHeight = videoHeight;
        this.audioCodec = audioCodec2;
        this.audioBitRate = audioBitRate2;
        this.audioSampleRate = audioSampleRate2;
        this.audioChannels = audioChannels2;
    }
}
