package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class VideoProfile implements Parcelable {
    public static final Parcelable.Creator<VideoProfile> CREATOR = new Parcelable.Creator<VideoProfile>() {
        public VideoProfile createFromParcel(Parcel source) {
            int state = source.readInt();
            int quality = source.readInt();
            ClassLoader classLoader = VideoProfile.class.getClassLoader();
            return new VideoProfile(state, quality);
        }

        public VideoProfile[] newArray(int size) {
            return new VideoProfile[size];
        }
    };
    public static final int QUALITY_DEFAULT = 4;
    public static final int QUALITY_HIGH = 1;
    public static final int QUALITY_LOW = 3;
    public static final int QUALITY_MEDIUM = 2;
    public static final int QUALITY_UNKNOWN = 0;
    public static final int STATE_AUDIO_ONLY = 0;
    public static final int STATE_BIDIRECTIONAL = 3;
    public static final int STATE_CANCELED = 8;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_RX_ENABLED = 2;
    public static final int STATE_TX_ENABLED = 1;
    private final int mQuality;
    private final int mVideoState;

    public static final class CameraCapabilities implements Parcelable {
        public static final Parcelable.Creator<CameraCapabilities> CREATOR = new Parcelable.Creator<CameraCapabilities>() {
            public CameraCapabilities createFromParcel(Parcel source) {
                return new CameraCapabilities(source.readInt(), source.readInt(), source.readByte() != 0, source.readFloat());
            }

            public CameraCapabilities[] newArray(int size) {
                return new CameraCapabilities[size];
            }
        };
        private final int mHeight;
        private final float mMaxZoom;
        private final int mWidth;
        private final boolean mZoomSupported;

        public CameraCapabilities(int width, int height) {
            this(width, height, false, 1.0f);
        }

        public CameraCapabilities(int width, int height, boolean zoomSupported, float maxZoom) {
            this.mWidth = width;
            this.mHeight = height;
            this.mZoomSupported = zoomSupported;
            this.mMaxZoom = maxZoom;
        }

        public int describeContents() {
            return 0;
        }

        public static boolean isVideo(int videoState) {
            if (VideoProfile.hasState(videoState, 1) || VideoProfile.hasState(videoState, 2) || VideoProfile.hasState(videoState, 3)) {
                return true;
            }
            return false;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(getWidth());
            dest.writeInt(getHeight());
            dest.writeByte(isZoomSupported() ? (byte) 1 : 0);
            dest.writeFloat(getMaxZoom());
        }

        public int getWidth() {
            return this.mWidth;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public boolean isZoomSupported() {
            return this.mZoomSupported;
        }

        public float getMaxZoom() {
            return this.mMaxZoom;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface VideoQuality {
    }

    public static class VideoState {
        public static final int AUDIO_ONLY = 0;
        public static final int BIDIRECTIONAL = 3;
        public static final int PAUSED = 4;
        public static final int RX_ENABLED = 2;
        public static final int TX_ENABLED = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface VideoState_VS {
    }

    public VideoProfile(int videoState) {
        this(videoState, 4);
    }

    public VideoProfile(int videoState, int quality) {
        this.mVideoState = videoState;
        this.mQuality = quality;
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public int getQuality() {
        return this.mQuality;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mVideoState);
        dest.writeInt(this.mQuality);
    }

    public String toString() {
        return "[VideoProfile videoState = " + videoStateToString(this.mVideoState) + " videoQuality = " + this.mQuality + "]";
    }

    public static String videoStateToString(int videoState) {
        StringBuilder sb = new StringBuilder();
        sb.append("Audio");
        if (videoState == 0) {
            sb.append(" Only");
        } else {
            if (isTransmissionEnabled(videoState)) {
                sb.append(" Tx");
            }
            if (isReceptionEnabled(videoState)) {
                sb.append(" Rx");
            }
            if (isPaused(videoState)) {
                sb.append(" Pause");
            }
        }
        return sb.toString();
    }

    public static boolean isAudioOnly(int videoState) {
        if (hasState(videoState, 1) || hasState(videoState, 2)) {
            return false;
        }
        return true;
    }

    public static boolean isVideo(int videoState) {
        if (hasState(videoState, 1) || hasState(videoState, 2) || hasState(videoState, 3)) {
            return true;
        }
        return false;
    }

    public static boolean isTransmissionEnabled(int videoState) {
        return hasState(videoState, 1);
    }

    public static boolean isReceptionEnabled(int videoState) {
        return hasState(videoState, 2);
    }

    public static boolean isBidirectional(int videoState) {
        return hasState(videoState, 3);
    }

    public static boolean isPaused(int videoState) {
        return hasState(videoState, 4);
    }

    /* access modifiers changed from: private */
    public static boolean hasState(int videoState, int state) {
        return (videoState & state) == state;
    }
}
