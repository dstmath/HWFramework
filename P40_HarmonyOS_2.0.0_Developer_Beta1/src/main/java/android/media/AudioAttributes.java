package android.media;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.media.audiopolicy.AudioProductStrategy;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class AudioAttributes implements Parcelable {
    public static final int ALLOW_CAPTURE_BY_ALL = 1;
    public static final int ALLOW_CAPTURE_BY_NONE = 3;
    public static final int ALLOW_CAPTURE_BY_SYSTEM = 2;
    private static final int ALL_PARCEL_FLAGS = 1;
    private static final int ATTR_PARCEL_IS_NULL_BUNDLE = -1977;
    private static final int ATTR_PARCEL_IS_VALID_BUNDLE = 1980;
    public static final int CONTENT_TYPE_MOVIE = 3;
    public static final int CONTENT_TYPE_MUSIC = 2;
    public static final int CONTENT_TYPE_SONIFICATION = 4;
    public static final int CONTENT_TYPE_SPEECH = 1;
    public static final int CONTENT_TYPE_UNKNOWN = 0;
    public static final Parcelable.Creator<AudioAttributes> CREATOR = new Parcelable.Creator<AudioAttributes>() {
        /* class android.media.AudioAttributes.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioAttributes createFromParcel(Parcel p) {
            return new AudioAttributes(p);
        }

        @Override // android.os.Parcelable.Creator
        public AudioAttributes[] newArray(int size) {
            return new AudioAttributes[size];
        }
    };
    private static final int FLAG_ALL = 3151871;
    private static final int FLAG_ALL_PUBLIC = 273;
    public static final int FLAG_AUDIBILITY_ENFORCED = 1;
    @SystemApi
    public static final int FLAG_BEACON = 8;
    @SystemApi
    public static final int FLAG_BYPASS_INTERRUPTION_POLICY = 64;
    @SystemApi
    public static final int FLAG_BYPASS_MUTE = 128;
    public static final int FLAG_DEEP_BUFFER = 512;
    public static final int FLAG_DIRECT_OUTPUT = 1048576;
    public static final int FLAG_HW_AV_SYNC = 16;
    @SystemApi
    public static final int FLAG_HW_HOTWORD = 32;
    public static final int FLAG_LOW_LATENCY = 256;
    public static final int FLAG_MUTE_HAPTIC = 2048;
    public static final int FLAG_NO_MEDIA_PROJECTION = 1024;
    public static final int FLAG_NO_OFFLOAD_OUTPUT = 2097152;
    public static final int FLAG_NO_SYSTEM_CAPTURE = 4096;
    public static final int FLAG_SCO = 4;
    public static final int FLAG_SECURE = 2;
    public static final int FLATTEN_TAGS = 1;
    public static final int[] SDK_USAGES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 17};
    public static final int SUPPRESSIBLE_ALARM = 4;
    public static final int SUPPRESSIBLE_CALL = 2;
    public static final int SUPPRESSIBLE_MEDIA = 5;
    public static final int SUPPRESSIBLE_NEVER = 3;
    public static final int SUPPRESSIBLE_NOTIFICATION = 1;
    public static final int SUPPRESSIBLE_SYSTEM = 6;
    public static final SparseIntArray SUPPRESSIBLE_USAGES = new SparseIntArray();
    private static final String TAG = "AudioAttributes";
    public static final int USAGE_ALARM = 4;
    public static final int USAGE_ASSISTANCE_ACCESSIBILITY = 11;
    public static final int USAGE_ASSISTANCE_NAVIGATION_GUIDANCE = 12;
    public static final int USAGE_ASSISTANCE_SONIFICATION = 13;
    public static final int USAGE_ASSISTANT = 16;
    public static final int USAGE_GAME = 14;
    public static final int USAGE_MEDIA = 1;
    public static final int USAGE_NOTIFICATION = 5;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_DELAYED = 9;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_INSTANT = 8;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_REQUEST = 7;
    public static final int USAGE_NOTIFICATION_EVENT = 10;
    public static final int USAGE_NOTIFICATION_RINGTONE = 6;
    public static final int USAGE_TTS = 17;
    public static final int USAGE_UNKNOWN = 0;
    public static final int USAGE_VIRTUAL_SOURCE = 15;
    public static final int USAGE_VOICE_COMMUNICATION = 2;
    public static final int USAGE_VOICE_COMMUNICATION_SIGNALLING = 3;
    private Bundle mBundle;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mContentType;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mFlags;
    @UnsupportedAppUsage
    private String mFormattedTags;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mSource;
    private HashSet<String> mTags;
    @UnsupportedAppUsage
    private int mUsage;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AttributeContentType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AttributeUsage {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CapturePolicy {
    }

    static /* synthetic */ int access$576(AudioAttributes x0, int x1) {
        int i = x0.mFlags | x1;
        x0.mFlags = i;
        return i;
    }

    static {
        SUPPRESSIBLE_USAGES.put(5, 1);
        SUPPRESSIBLE_USAGES.put(6, 2);
        SUPPRESSIBLE_USAGES.put(7, 2);
        SUPPRESSIBLE_USAGES.put(8, 1);
        SUPPRESSIBLE_USAGES.put(9, 1);
        SUPPRESSIBLE_USAGES.put(10, 1);
        SUPPRESSIBLE_USAGES.put(11, 3);
        SUPPRESSIBLE_USAGES.put(2, 3);
        SUPPRESSIBLE_USAGES.put(3, 3);
        SUPPRESSIBLE_USAGES.put(4, 4);
        SUPPRESSIBLE_USAGES.put(1, 5);
        SUPPRESSIBLE_USAGES.put(12, 5);
        SUPPRESSIBLE_USAGES.put(14, 5);
        SUPPRESSIBLE_USAGES.put(16, 5);
        SUPPRESSIBLE_USAGES.put(0, 5);
        SUPPRESSIBLE_USAGES.put(13, 6);
    }

    private AudioAttributes() {
        this.mUsage = 0;
        this.mContentType = 0;
        this.mSource = -1;
        this.mFlags = 0;
    }

    public int getContentType() {
        return this.mContentType;
    }

    public int getUsage() {
        return this.mUsage;
    }

    @SystemApi
    public int getCapturePreset() {
        return this.mSource;
    }

    public int getFlags() {
        return this.mFlags & 273;
    }

    @SystemApi
    public int getAllFlags() {
        return this.mFlags & FLAG_ALL;
    }

    @SystemApi
    public Bundle getBundle() {
        Bundle bundle = this.mBundle;
        if (bundle == null) {
            return bundle;
        }
        return new Bundle(bundle);
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(this.mTags);
    }

    public boolean areHapticChannelsMuted() {
        return (this.mFlags & 2048) != 0;
    }

    public int getAllowedCapturePolicy() {
        int i = this.mFlags;
        if ((i & 4096) == 4096) {
            return 3;
        }
        if ((i & 1024) == 1024) {
            return 2;
        }
        return 1;
    }

    public void appendFlags(int flags) {
        this.mFlags |= flags;
    }

    public static class Builder {
        private Bundle mBundle;
        private int mContentType = 0;
        private int mFlags = 0;
        private boolean mMuteHapticChannels = true;
        private int mSource = -1;
        private HashSet<String> mTags = new HashSet<>();
        private int mUsage = 0;

        public Builder() {
        }

        public Builder(AudioAttributes aa) {
            this.mUsage = aa.mUsage;
            this.mContentType = aa.mContentType;
            this.mFlags = aa.getAllFlags();
            this.mTags = (HashSet) aa.mTags.clone();
            this.mMuteHapticChannels = aa.areHapticChannelsMuted();
        }

        public AudioAttributes build() {
            AudioAttributes aa = new AudioAttributes();
            aa.mContentType = this.mContentType;
            aa.mUsage = this.mUsage;
            aa.mSource = this.mSource;
            aa.mFlags = this.mFlags;
            if (this.mMuteHapticChannels) {
                AudioAttributes.access$576(aa, 2048);
            }
            aa.mTags = (HashSet) this.mTags.clone();
            aa.mFormattedTags = TextUtils.join(";", this.mTags);
            Bundle bundle = this.mBundle;
            if (bundle != null) {
                aa.mBundle = new Bundle(bundle);
            }
            return aa;
        }

        public Builder setUsage(int usage) {
            switch (usage) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                    this.mUsage = usage;
                    break;
                default:
                    this.mUsage = 0;
                    break;
            }
            return this;
        }

        public Builder setContentType(int contentType) {
            if (contentType == 0 || contentType == 1 || contentType == 2 || contentType == 3 || contentType == 4) {
                this.mContentType = contentType;
            } else {
                this.mContentType = 0;
            }
            return this;
        }

        public Builder setFlags(int flags) {
            this.mFlags |= flags & AudioAttributes.FLAG_ALL;
            return this;
        }

        public Builder setAllowedCapturePolicy(int capturePolicy) {
            this.mFlags = AudioAttributes.capturePolicyToFlags(capturePolicy, this.mFlags);
            return this;
        }

        public Builder replaceFlags(int flags) {
            this.mFlags = AudioAttributes.FLAG_ALL & flags;
            return this;
        }

        @SystemApi
        public Builder addBundle(Bundle bundle) {
            if (bundle != null) {
                Bundle bundle2 = this.mBundle;
                if (bundle2 == null) {
                    this.mBundle = new Bundle(bundle);
                } else {
                    bundle2.putAll(bundle);
                }
                return this;
            }
            throw new IllegalArgumentException("Illegal null bundle");
        }

        @UnsupportedAppUsage
        public Builder addTag(String tag) {
            this.mTags.add(tag);
            return this;
        }

        public Builder setLegacyStreamType(int streamType) {
            if (streamType != 10) {
                setInternalLegacyStreamType(streamType);
                return this;
            }
            throw new IllegalArgumentException("STREAM_ACCESSIBILITY is not a legacy stream type that was used for audio playback");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @UnsupportedAppUsage
        public Builder setInternalLegacyStreamType(int streamType) {
            AudioAttributes attributes;
            this.mContentType = 0;
            this.mUsage = 0;
            if (AudioProductStrategy.getAudioProductStrategies().size() > 0 && (attributes = AudioProductStrategy.getAudioAttributesForStrategyWithLegacyStreamType(streamType)) != null) {
                this.mUsage = attributes.mUsage;
                this.mContentType = attributes.mContentType;
                this.mFlags = attributes.mFlags;
                this.mMuteHapticChannels = attributes.areHapticChannelsMuted();
                this.mTags = attributes.mTags;
                this.mBundle = attributes.mBundle;
                this.mSource = attributes.mSource;
            }
            if (this.mContentType == 0) {
                switch (streamType) {
                    case 0:
                        this.mContentType = 1;
                        break;
                    case 1:
                        this.mContentType = 4;
                        break;
                    case 2:
                        this.mContentType = 4;
                        break;
                    case 3:
                        this.mContentType = 2;
                        break;
                    case 4:
                        this.mContentType = 4;
                        break;
                    case 5:
                        this.mContentType = 4;
                        break;
                    case 6:
                        this.mContentType = 1;
                        this.mFlags |= 4;
                        break;
                    case 7:
                        this.mFlags = 1 | this.mFlags;
                        this.mContentType = 4;
                        break;
                    case 8:
                        this.mContentType = 4;
                        break;
                    case 9:
                        this.mContentType = 1;
                        this.mFlags |= 8;
                        break;
                    case 10:
                        this.mContentType = 1;
                        break;
                    default:
                        Log.e(AudioAttributes.TAG, "Invalid stream type " + streamType + " for AudioAttributes");
                        break;
                }
            }
            if (this.mUsage == 0) {
                this.mUsage = AudioAttributes.usageForStreamType(streamType);
            }
            return this;
        }

        @SystemApi
        public Builder setCapturePreset(int preset) {
            if (preset == 0 || preset == 1 || preset == 5 || preset == 6 || preset == 7 || preset == 9 || preset == 10 || preset == 5001 || preset == 5002 || preset == 10007) {
                this.mSource = preset;
            } else {
                Log.e(AudioAttributes.TAG, "Invalid capture preset " + preset + " for AudioAttributes");
            }
            return this;
        }

        @SystemApi
        public Builder setInternalCapturePreset(int preset) {
            if (preset == 1999 || preset == 8 || preset == 1998 || preset == 3 || preset == 2 || preset == 4 || preset == 1997) {
                this.mSource = preset;
            } else {
                setCapturePreset(preset);
            }
            return this;
        }

        public Builder setHapticChannelsMuted(boolean muted) {
            this.mMuteHapticChannels = muted;
            return this;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUsage);
        dest.writeInt(this.mContentType);
        dest.writeInt(this.mSource);
        dest.writeInt(this.mFlags);
        dest.writeInt(flags & 1);
        if ((flags & 1) == 0) {
            String[] tagsArray = new String[this.mTags.size()];
            this.mTags.toArray(tagsArray);
            dest.writeStringArray(tagsArray);
        } else if ((flags & 1) == 1) {
            dest.writeString(this.mFormattedTags);
        }
        if (this.mBundle == null) {
            dest.writeInt(ATTR_PARCEL_IS_NULL_BUNDLE);
            return;
        }
        dest.writeInt(ATTR_PARCEL_IS_VALID_BUNDLE);
        dest.writeBundle(this.mBundle);
    }

    private AudioAttributes(Parcel in) {
        boolean hasFlattenedTags = false;
        this.mUsage = 0;
        this.mContentType = 0;
        this.mSource = -1;
        this.mFlags = 0;
        this.mUsage = in.readInt();
        this.mContentType = in.readInt();
        this.mSource = in.readInt();
        this.mFlags = in.readInt();
        hasFlattenedTags = (in.readInt() & 1) == 1 ? true : hasFlattenedTags;
        this.mTags = new HashSet<>();
        if (hasFlattenedTags) {
            this.mFormattedTags = new String(in.readString());
            this.mTags.add(this.mFormattedTags);
        } else {
            String[] tagsArray = in.readStringArray();
            for (int i = tagsArray.length - 1; i >= 0; i--) {
                this.mTags.add(tagsArray[i]);
            }
            this.mFormattedTags = TextUtils.join(";", this.mTags);
        }
        int readInt = in.readInt();
        if (readInt == ATTR_PARCEL_IS_NULL_BUNDLE) {
            this.mBundle = null;
        } else if (readInt != ATTR_PARCEL_IS_VALID_BUNDLE) {
            Log.e(TAG, "Illegal value unmarshalling AudioAttributes, can't initialize bundle");
        } else {
            this.mBundle = new Bundle(in.readBundle());
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioAttributes that = (AudioAttributes) o;
        if (this.mContentType == that.mContentType && this.mFlags == that.mFlags && this.mSource == that.mSource && this.mUsage == that.mUsage && this.mFormattedTags.equals(that.mFormattedTags)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mContentType), Integer.valueOf(this.mFlags), Integer.valueOf(this.mSource), Integer.valueOf(this.mUsage), this.mFormattedTags, this.mBundle);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AudioAttributes: usage=");
        sb.append(usageToString());
        sb.append(" content=");
        sb.append(contentTypeToString());
        sb.append(" flags=0x");
        sb.append(Integer.toHexString(this.mFlags).toUpperCase());
        sb.append(" tags=");
        sb.append(this.mFormattedTags);
        sb.append(" bundle=");
        Bundle bundle = this.mBundle;
        sb.append(bundle == null ? "null" : bundle.toString());
        return new String(sb.toString());
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1159641169921L, this.mUsage);
        proto.write(1159641169922L, this.mContentType);
        proto.write(1120986464259L, this.mFlags);
        for (String t : this.mFormattedTags.split(";")) {
            String t2 = t.trim();
            if (t2 != "") {
                proto.write(2237677961220L, t2);
            }
        }
        proto.end(token);
    }

    public String usageToString() {
        return usageToString(this.mUsage);
    }

    public static String usageToString(int usage) {
        switch (usage) {
            case 0:
                return new String("USAGE_UNKNOWN");
            case 1:
                return new String("USAGE_MEDIA");
            case 2:
                return new String("USAGE_VOICE_COMMUNICATION");
            case 3:
                return new String("USAGE_VOICE_COMMUNICATION_SIGNALLING");
            case 4:
                return new String("USAGE_ALARM");
            case 5:
                return new String("USAGE_NOTIFICATION");
            case 6:
                return new String("USAGE_NOTIFICATION_RINGTONE");
            case 7:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_REQUEST");
            case 8:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_INSTANT");
            case 9:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_DELAYED");
            case 10:
                return new String("USAGE_NOTIFICATION_EVENT");
            case 11:
                return new String("USAGE_ASSISTANCE_ACCESSIBILITY");
            case 12:
                return new String("USAGE_ASSISTANCE_NAVIGATION_GUIDANCE");
            case 13:
                return new String("USAGE_ASSISTANCE_SONIFICATION");
            case 14:
                return new String("USAGE_GAME");
            case 15:
            default:
                return new String("unknown usage " + usage);
            case 16:
                return new String("USAGE_ASSISTANT");
            case 17:
                return new String("USAGE_TTS");
        }
    }

    public String contentTypeToString() {
        int i = this.mContentType;
        if (i == 0) {
            return new String("CONTENT_TYPE_UNKNOWN");
        }
        if (i == 1) {
            return new String("CONTENT_TYPE_SPEECH");
        }
        if (i == 2) {
            return new String("CONTENT_TYPE_MUSIC");
        }
        if (i == 3) {
            return new String("CONTENT_TYPE_MOVIE");
        }
        if (i == 4) {
            return new String("CONTENT_TYPE_SONIFICATION");
        }
        return new String("unknown content type " + this.mContentType);
    }

    /* access modifiers changed from: private */
    public static int usageForStreamType(int streamType) {
        switch (streamType) {
            case 0:
                return 2;
            case 1:
            case 7:
                return 13;
            case 2:
                return 6;
            case 3:
                return 1;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 2;
            case 8:
                return 3;
            case 9:
                return 17;
            case 10:
                return 11;
            default:
                return 0;
        }
    }

    public int getVolumeControlStream() {
        return toVolumeStreamType(true, this);
    }

    @UnsupportedAppUsage
    public static int toLegacyStreamType(AudioAttributes aa) {
        return toVolumeStreamType(false, aa);
    }

    private static int toVolumeStreamType(boolean fromGetVolumeControlStream, AudioAttributes aa) {
        if ((aa.getFlags() & 1) == 1) {
            if (fromGetVolumeControlStream) {
                return 1;
            }
            return 7;
        } else if ((aa.getAllFlags() & 4) == 4) {
            if (fromGetVolumeControlStream) {
                return 0;
            }
            return 6;
        } else if ((aa.getAllFlags() & 8) == 8) {
            return fromGetVolumeControlStream ? 3 : 9;
        } else {
            if (AudioProductStrategy.getAudioProductStrategies().size() > 0) {
                return AudioProductStrategy.getLegacyStreamTypeForStrategyWithAudioAttributes(aa);
            }
            switch (aa.getUsage()) {
                case 0:
                    return 3;
                case 1:
                case 12:
                case 14:
                case 16:
                    return 3;
                case 2:
                    return 0;
                case 3:
                    if (fromGetVolumeControlStream) {
                        return 0;
                    }
                    return 8;
                case 4:
                    return 4;
                case 5:
                case 7:
                case 8:
                case 9:
                case 10:
                    return 5;
                case 6:
                    return 2;
                case 11:
                    return 10;
                case 13:
                    return 1;
                case 15:
                default:
                    if (!fromGetVolumeControlStream) {
                        return 3;
                    }
                    throw new IllegalArgumentException("Unknown usage value " + aa.getUsage() + " in audio attributes");
                case 17:
                    return 9;
            }
        }
    }

    static int capturePolicyToFlags(int capturePolicy, int flags) {
        if (capturePolicy == 1) {
            return flags & -5121;
        }
        if (capturePolicy == 2) {
            return (flags | 1024) & -4097;
        }
        if (capturePolicy == 3) {
            return flags | 5120;
        }
        throw new IllegalArgumentException("Unknown allow playback capture policy");
    }
}
