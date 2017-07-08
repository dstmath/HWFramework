package android.media.tv;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech.Engine;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public final class TvTrackInfo implements Parcelable {
    public static final Creator<TvTrackInfo> CREATOR = null;
    public static final int TYPE_AUDIO = 0;
    public static final int TYPE_SUBTITLE = 2;
    public static final int TYPE_VIDEO = 1;
    private final int mAudioChannelCount;
    private final int mAudioSampleRate;
    private final CharSequence mDescription;
    private final Bundle mExtra;
    private final String mId;
    private final String mLanguage;
    private final int mType;
    private final byte mVideoActiveFormatDescription;
    private final float mVideoFrameRate;
    private final int mVideoHeight;
    private final float mVideoPixelAspectRatio;
    private final int mVideoWidth;

    public static final class Builder {
        private int mAudioChannelCount;
        private int mAudioSampleRate;
        private CharSequence mDescription;
        private Bundle mExtra;
        private final String mId;
        private String mLanguage;
        private final int mType;
        private byte mVideoActiveFormatDescription;
        private float mVideoFrameRate;
        private int mVideoHeight;
        private float mVideoPixelAspectRatio;
        private int mVideoWidth;

        public Builder(int type, String id) {
            this.mVideoPixelAspectRatio = Engine.DEFAULT_VOLUME;
            if (type == 0 || type == TvTrackInfo.TYPE_VIDEO || type == TvTrackInfo.TYPE_SUBTITLE) {
                Preconditions.checkNotNull(id);
                this.mType = type;
                this.mId = id;
                return;
            }
            throw new IllegalArgumentException("Unknown type: " + type);
        }

        public final Builder setLanguage(String language) {
            this.mLanguage = language;
            return this;
        }

        public final Builder setDescription(CharSequence description) {
            this.mDescription = description;
            return this;
        }

        public final Builder setAudioChannelCount(int audioChannelCount) {
            if (this.mType != 0) {
                throw new IllegalStateException("Not an audio track");
            }
            this.mAudioChannelCount = audioChannelCount;
            return this;
        }

        public final Builder setAudioSampleRate(int audioSampleRate) {
            if (this.mType != 0) {
                throw new IllegalStateException("Not an audio track");
            }
            this.mAudioSampleRate = audioSampleRate;
            return this;
        }

        public final Builder setVideoWidth(int videoWidth) {
            if (this.mType != TvTrackInfo.TYPE_VIDEO) {
                throw new IllegalStateException("Not a video track");
            }
            this.mVideoWidth = videoWidth;
            return this;
        }

        public final Builder setVideoHeight(int videoHeight) {
            if (this.mType != TvTrackInfo.TYPE_VIDEO) {
                throw new IllegalStateException("Not a video track");
            }
            this.mVideoHeight = videoHeight;
            return this;
        }

        public final Builder setVideoFrameRate(float videoFrameRate) {
            if (this.mType != TvTrackInfo.TYPE_VIDEO) {
                throw new IllegalStateException("Not a video track");
            }
            this.mVideoFrameRate = videoFrameRate;
            return this;
        }

        public final Builder setVideoPixelAspectRatio(float videoPixelAspectRatio) {
            if (this.mType != TvTrackInfo.TYPE_VIDEO) {
                throw new IllegalStateException("Not a video track");
            }
            this.mVideoPixelAspectRatio = videoPixelAspectRatio;
            return this;
        }

        public final Builder setVideoActiveFormatDescription(byte videoActiveFormatDescription) {
            if (this.mType != TvTrackInfo.TYPE_VIDEO) {
                throw new IllegalStateException("Not a video track");
            }
            this.mVideoActiveFormatDescription = videoActiveFormatDescription;
            return this;
        }

        public final Builder setExtra(Bundle extra) {
            this.mExtra = new Bundle(extra);
            return this;
        }

        public TvTrackInfo build() {
            return new TvTrackInfo(this.mId, this.mLanguage, this.mDescription, this.mAudioChannelCount, this.mAudioSampleRate, this.mVideoWidth, this.mVideoHeight, this.mVideoFrameRate, this.mVideoPixelAspectRatio, this.mVideoActiveFormatDescription, this.mExtra, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.tv.TvTrackInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.tv.TvTrackInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.tv.TvTrackInfo.<clinit>():void");
    }

    private TvTrackInfo(int type, String id, String language, CharSequence description, int audioChannelCount, int audioSampleRate, int videoWidth, int videoHeight, float videoFrameRate, float videoPixelAspectRatio, byte videoActiveFormatDescription, Bundle extra) {
        this.mType = type;
        this.mId = id;
        this.mLanguage = language;
        this.mDescription = description;
        this.mAudioChannelCount = audioChannelCount;
        this.mAudioSampleRate = audioSampleRate;
        this.mVideoWidth = videoWidth;
        this.mVideoHeight = videoHeight;
        this.mVideoFrameRate = videoFrameRate;
        this.mVideoPixelAspectRatio = videoPixelAspectRatio;
        this.mVideoActiveFormatDescription = videoActiveFormatDescription;
        this.mExtra = extra;
    }

    private TvTrackInfo(Parcel in) {
        this.mType = in.readInt();
        this.mId = in.readString();
        this.mLanguage = in.readString();
        this.mDescription = in.readString();
        this.mAudioChannelCount = in.readInt();
        this.mAudioSampleRate = in.readInt();
        this.mVideoWidth = in.readInt();
        this.mVideoHeight = in.readInt();
        this.mVideoFrameRate = in.readFloat();
        this.mVideoPixelAspectRatio = in.readFloat();
        this.mVideoActiveFormatDescription = in.readByte();
        this.mExtra = in.readBundle();
    }

    public final int getType() {
        return this.mType;
    }

    public final String getId() {
        return this.mId;
    }

    public final String getLanguage() {
        return this.mLanguage;
    }

    public final CharSequence getDescription() {
        return this.mDescription;
    }

    public final int getAudioChannelCount() {
        if (this.mType == 0) {
            return this.mAudioChannelCount;
        }
        throw new IllegalStateException("Not an audio track");
    }

    public final int getAudioSampleRate() {
        if (this.mType == 0) {
            return this.mAudioSampleRate;
        }
        throw new IllegalStateException("Not an audio track");
    }

    public final int getVideoWidth() {
        if (this.mType == TYPE_VIDEO) {
            return this.mVideoWidth;
        }
        throw new IllegalStateException("Not a video track");
    }

    public final int getVideoHeight() {
        if (this.mType == TYPE_VIDEO) {
            return this.mVideoHeight;
        }
        throw new IllegalStateException("Not a video track");
    }

    public final float getVideoFrameRate() {
        if (this.mType == TYPE_VIDEO) {
            return this.mVideoFrameRate;
        }
        throw new IllegalStateException("Not a video track");
    }

    public final float getVideoPixelAspectRatio() {
        if (this.mType == TYPE_VIDEO) {
            return this.mVideoPixelAspectRatio;
        }
        throw new IllegalStateException("Not a video track");
    }

    public final byte getVideoActiveFormatDescription() {
        if (this.mType == TYPE_VIDEO) {
            return this.mVideoActiveFormatDescription;
        }
        throw new IllegalStateException("Not a video track");
    }

    public final Bundle getExtra() {
        return this.mExtra;
    }

    public int describeContents() {
        return TYPE_AUDIO;
    }

    public void writeToParcel(Parcel dest, int flags) {
        String str = null;
        dest.writeInt(this.mType);
        dest.writeString(this.mId);
        dest.writeString(this.mLanguage);
        if (this.mDescription != null) {
            str = this.mDescription.toString();
        }
        dest.writeString(str);
        dest.writeInt(this.mAudioChannelCount);
        dest.writeInt(this.mAudioSampleRate);
        dest.writeInt(this.mVideoWidth);
        dest.writeInt(this.mVideoHeight);
        dest.writeFloat(this.mVideoFrameRate);
        dest.writeFloat(this.mVideoPixelAspectRatio);
        dest.writeByte(this.mVideoActiveFormatDescription);
        dest.writeBundle(this.mExtra);
    }

    public boolean equals(Object o) {
        boolean z = true;
        boolean z2 = false;
        if (this == o) {
            return true;
        }
        if (!(o instanceof TvTrackInfo)) {
            return false;
        }
        TvTrackInfo obj = (TvTrackInfo) o;
        if (TextUtils.equals(this.mId, obj.mId) && this.mType == obj.mType && TextUtils.equals(this.mLanguage, obj.mLanguage) && TextUtils.equals(this.mDescription, obj.mDescription) && Objects.equals(this.mExtra, obj.mExtra)) {
            if (this.mType != 0) {
                if (this.mType == TYPE_VIDEO) {
                    if (this.mVideoWidth == obj.mVideoWidth && this.mVideoHeight == obj.mVideoHeight && this.mVideoFrameRate == obj.mVideoFrameRate) {
                        if (this.mVideoPixelAspectRatio != obj.mVideoPixelAspectRatio) {
                            z = false;
                        }
                    }
                }
                z2 = z;
            } else if (this.mAudioChannelCount == obj.mAudioChannelCount && this.mAudioSampleRate == obj.mAudioSampleRate) {
                z2 = true;
            }
        }
        return z2;
    }

    public int hashCode() {
        return Objects.hashCode(this.mId);
    }
}
