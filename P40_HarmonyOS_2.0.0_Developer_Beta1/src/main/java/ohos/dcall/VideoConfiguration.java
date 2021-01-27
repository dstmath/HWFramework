package ohos.dcall;

import ohos.annotation.SystemApi;

@SystemApi
public class VideoConfiguration {
    public static final int VIDEOCALL_STATUS_AUDIO_ONLY = 0;
    public static final int VIDEOCALL_STATUS_BEAUTYFACE_ENABLED = 32;
    public static final int VIDEOCALL_STATUS_CAMERAFRONT_ENABLED = 16;
    public static final int VIDEOCALL_STATUS_CANCELED = 8;
    public static final int VIDEOCALL_STATUS_PAUSED = 4;
    public static final int VIDEOCALL_STATUS_RX_VIDEOSTREAM_ENABLED = 2;
    public static final int VIDEOCALL_STATUS_TWOWAY_ENABLED = 3;
    public static final int VIDEOCALL_STATUS_TX_VIDEOSTREAM_ENABLED = 1;
    private int mVideoStatus;

    private static boolean hasVideoCallStatus(int i, int i2) {
        return (i & i2) == i2;
    }

    public VideoConfiguration(int i) {
        this.mVideoStatus = i;
    }

    public void setVideoStatus(int i) {
        this.mVideoStatus = i;
    }

    public int getVideoStatus() {
        return this.mVideoStatus;
    }

    public String toString() {
        return "[VideoConfiguration videoStatus = " + videoStatusToString(this.mVideoStatus) + "]";
    }

    public static String videoStatusToString(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("Audio");
        if (i == 0) {
            sb.append(" Only");
        } else {
            if (isTxVideoStreamEnabled(i)) {
                sb.append(" Tx");
            }
            if (isRxVideoStreamEnabled(i)) {
                sb.append(" Rx");
            }
            if (isPaused(i)) {
                sb.append(" Pause");
            }
            if (isCanceled(i)) {
                sb.append(" Cancel");
            }
            if (isCameraFront(i)) {
                sb.append(" CameraFront");
            }
            if (isBeautyFaceEnabled(i)) {
                sb.append(" BeautyFace");
            }
        }
        return sb.toString();
    }

    public static boolean isAudioOnly(int i) {
        if (hasVideoCallStatus(i, 1) || hasVideoCallStatus(i, 2)) {
            return false;
        }
        return true;
    }

    public boolean isVideoCall() {
        if (hasVideoCallStatus(this.mVideoStatus, 1) || hasVideoCallStatus(this.mVideoStatus, 2) || hasVideoCallStatus(this.mVideoStatus, 3)) {
            return true;
        }
        return false;
    }

    public static boolean isVideoCall(int i) {
        if (hasVideoCallStatus(i, 1) || hasVideoCallStatus(i, 2) || hasVideoCallStatus(i, 3)) {
            return true;
        }
        return false;
    }

    public static boolean isTxVideoStreamEnabled(int i) {
        return hasVideoCallStatus(i, 1);
    }

    public static boolean isRxVideoStreamEnabled(int i) {
        return hasVideoCallStatus(i, 2);
    }

    public static boolean isTwoWay(int i) {
        return hasVideoCallStatus(i, 3);
    }

    public static boolean isCameraFront(int i) {
        return hasVideoCallStatus(i, 16);
    }

    public static boolean isBeautyFaceEnabled(int i) {
        return hasVideoCallStatus(i, 32);
    }

    public static boolean isPaused(int i) {
        return hasVideoCallStatus(i, 4);
    }

    public static boolean isCanceled(int i) {
        return hasVideoCallStatus(i, 8);
    }
}
