package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.WindowManager.LayoutParams;

public class VideoProfile implements Parcelable {
    public static final Creator<VideoProfile> CREATOR = null;
    public static final int QUALITY_DEFAULT = 4;
    public static final int QUALITY_HIGH = 1;
    public static final int QUALITY_LOW = 3;
    public static final int QUALITY_MEDIUM = 2;
    public static final int QUALITY_UNKNOWN = 0;
    public static final int STATE_AUDIO_ONLY = 0;
    public static final int STATE_BIDIRECTIONAL = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_RX_ENABLED = 2;
    public static final int STATE_TX_ENABLED = 1;
    private final int mQuality;
    private final int mVideoState;

    public static final class CameraCapabilities implements Parcelable {
        public static final Creator<CameraCapabilities> CREATOR = null;
        private final int mHeight;
        private final float mMaxZoom;
        private final int mWidth;
        private final boolean mZoomSupported;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.VideoProfile.CameraCapabilities.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.VideoProfile.CameraCapabilities.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.VideoProfile.CameraCapabilities.<clinit>():void");
        }

        public CameraCapabilities(int width, int height) {
            this(width, height, false, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        }

        public CameraCapabilities(int width, int height, boolean zoomSupported, float maxZoom) {
            this.mWidth = width;
            this.mHeight = height;
            this.mZoomSupported = zoomSupported;
            this.mMaxZoom = maxZoom;
        }

        public int describeContents() {
            return VideoProfile.STATE_AUDIO_ONLY;
        }

        public static boolean isVideo(int videoState) {
            if (VideoProfile.hasState(videoState, VideoProfile.STATE_TX_ENABLED) || VideoProfile.hasState(videoState, VideoProfile.STATE_RX_ENABLED)) {
                return true;
            }
            return VideoProfile.hasState(videoState, VideoProfile.STATE_BIDIRECTIONAL);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(getWidth());
            dest.writeInt(getHeight());
            dest.writeByte((byte) (isZoomSupported() ? VideoProfile.STATE_TX_ENABLED : VideoProfile.STATE_AUDIO_ONLY));
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

    public static class VideoState {
        public static final int AUDIO_ONLY = 0;
        public static final int BIDIRECTIONAL = 3;
        public static final int PAUSED = 4;
        public static final int RX_ENABLED = 2;
        public static final int TX_ENABLED = 1;

        public VideoState() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.VideoProfile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.VideoProfile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.VideoProfile.<clinit>():void");
    }

    public VideoProfile(int videoState) {
        this(videoState, STATE_PAUSED);
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
        return STATE_AUDIO_ONLY;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mVideoState);
        dest.writeInt(this.mQuality);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[VideoProfile videoState = ");
        sb.append(videoStateToString(this.mVideoState));
        sb.append(" videoQuality = ");
        sb.append(this.mQuality);
        sb.append("]");
        return sb.toString();
    }

    public static String videoStateToString(int videoState) {
        StringBuilder sb = new StringBuilder();
        sb.append("Audio");
        if (isAudioOnly(videoState)) {
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
        return (hasState(videoState, STATE_TX_ENABLED) || hasState(videoState, STATE_RX_ENABLED)) ? false : true;
    }

    public static boolean isVideo(int videoState) {
        if (hasState(videoState, STATE_TX_ENABLED) || hasState(videoState, STATE_RX_ENABLED)) {
            return true;
        }
        return hasState(videoState, STATE_BIDIRECTIONAL);
    }

    public static boolean isTransmissionEnabled(int videoState) {
        return hasState(videoState, STATE_TX_ENABLED);
    }

    public static boolean isReceptionEnabled(int videoState) {
        return hasState(videoState, STATE_RX_ENABLED);
    }

    public static boolean isBidirectional(int videoState) {
        return hasState(videoState, STATE_BIDIRECTIONAL);
    }

    public static boolean isPaused(int videoState) {
        return hasState(videoState, STATE_PAUSED);
    }

    private static boolean hasState(int videoState, int state) {
        return (videoState & state) == state;
    }
}
