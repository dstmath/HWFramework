package android.media;

import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.security.keymaster.KeymasterDefs;
import android.text.TextUtils;
import android.util.Log;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class AudioAttributes implements Parcelable {
    private static final int ALL_PARCEL_FLAGS = 1;
    private static final int ATTR_PARCEL_IS_NULL_BUNDLE = -1977;
    private static final int ATTR_PARCEL_IS_VALID_BUNDLE = 1980;
    public static final int CONTENT_TYPE_MOVIE = 3;
    public static final int CONTENT_TYPE_MUSIC = 2;
    public static final int CONTENT_TYPE_SONIFICATION = 4;
    public static final int CONTENT_TYPE_SPEECH = 1;
    public static final int CONTENT_TYPE_UNKNOWN = 0;
    public static final Creator<AudioAttributes> CREATOR = null;
    private static final int FLAG_ALL = 511;
    private static final int FLAG_ALL_PUBLIC = 273;
    public static final int FLAG_AUDIBILITY_ENFORCED = 1;
    public static final int FLAG_BEACON = 8;
    public static final int FLAG_BYPASS_INTERRUPTION_POLICY = 64;
    public static final int FLAG_BYPASS_MUTE = 128;
    public static final int FLAG_HW_AV_SYNC = 16;
    public static final int FLAG_HW_HOTWORD = 32;
    public static final int FLAG_LOW_LATENCY = 256;
    public static final int FLAG_SCO = 4;
    public static final int FLAG_SECURE = 2;
    public static final int FLATTEN_TAGS = 1;
    private static final String TAG = "AudioAttributes";
    public static final int USAGE_ALARM = 4;
    public static final int USAGE_ASSISTANCE_ACCESSIBILITY = 11;
    public static final int USAGE_ASSISTANCE_NAVIGATION_GUIDANCE = 12;
    public static final int USAGE_ASSISTANCE_SONIFICATION = 13;
    public static final int USAGE_GAME = 14;
    public static final int USAGE_MEDIA = 1;
    public static final int USAGE_NOTIFICATION = 5;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_DELAYED = 9;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_INSTANT = 8;
    public static final int USAGE_NOTIFICATION_COMMUNICATION_REQUEST = 7;
    public static final int USAGE_NOTIFICATION_EVENT = 10;
    public static final int USAGE_NOTIFICATION_RINGTONE = 6;
    public static final int USAGE_UNKNOWN = 0;
    public static final int USAGE_VIRTUAL_SOURCE = 15;
    public static final int USAGE_VOICE_COMMUNICATION = 2;
    public static final int USAGE_VOICE_COMMUNICATION_SIGNALLING = 3;
    private Bundle mBundle;
    private int mContentType;
    private int mFlags;
    private String mFormattedTags;
    private int mSource;
    private HashSet<String> mTags;
    private int mUsage;

    public static class Builder {
        private Bundle mBundle;
        private int mContentType;
        private int mFlags;
        private int mSource;
        private HashSet<String> mTags;
        private int mUsage;

        public Builder() {
            this.mUsage = AudioAttributes.USAGE_UNKNOWN;
            this.mContentType = AudioAttributes.USAGE_UNKNOWN;
            this.mSource = -1;
            this.mFlags = AudioAttributes.USAGE_UNKNOWN;
            this.mTags = new HashSet();
        }

        public Builder(AudioAttributes aa) {
            this.mUsage = AudioAttributes.USAGE_UNKNOWN;
            this.mContentType = AudioAttributes.USAGE_UNKNOWN;
            this.mSource = -1;
            this.mFlags = AudioAttributes.USAGE_UNKNOWN;
            this.mTags = new HashSet();
            this.mUsage = aa.mUsage;
            this.mContentType = aa.mContentType;
            this.mFlags = aa.mFlags;
            this.mTags = (HashSet) aa.mTags.clone();
        }

        public AudioAttributes build() {
            AudioAttributes aa = new AudioAttributes();
            aa.mContentType = this.mContentType;
            aa.mUsage = this.mUsage;
            aa.mSource = this.mSource;
            aa.mFlags = this.mFlags;
            aa.mTags = (HashSet) this.mTags.clone();
            aa.mFormattedTags = TextUtils.join(";", this.mTags);
            if (this.mBundle != null) {
                aa.mBundle = new Bundle(this.mBundle);
            }
            return aa;
        }

        public Builder setUsage(int usage) {
            switch (usage) {
                case AudioAttributes.USAGE_UNKNOWN /*0*/:
                case AudioAttributes.USAGE_MEDIA /*1*/:
                case AudioAttributes.USAGE_VOICE_COMMUNICATION /*2*/:
                case AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING /*3*/:
                case AudioAttributes.USAGE_ALARM /*4*/:
                case AudioAttributes.USAGE_NOTIFICATION /*5*/:
                case AudioAttributes.USAGE_NOTIFICATION_RINGTONE /*6*/:
                case AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST /*7*/:
                case AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT /*8*/:
                case AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_DELAYED /*9*/:
                case AudioAttributes.USAGE_NOTIFICATION_EVENT /*10*/:
                case AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY /*11*/:
                case AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE /*12*/:
                case AudioAttributes.USAGE_ASSISTANCE_SONIFICATION /*13*/:
                case AudioAttributes.USAGE_GAME /*14*/:
                case AudioAttributes.USAGE_VIRTUAL_SOURCE /*15*/:
                    this.mUsage = usage;
                    break;
                default:
                    this.mUsage = AudioAttributes.USAGE_UNKNOWN;
                    break;
            }
            return this;
        }

        public Builder setContentType(int contentType) {
            switch (contentType) {
                case AudioAttributes.USAGE_UNKNOWN /*0*/:
                case AudioAttributes.USAGE_MEDIA /*1*/:
                case AudioAttributes.USAGE_VOICE_COMMUNICATION /*2*/:
                case AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING /*3*/:
                case AudioAttributes.USAGE_ALARM /*4*/:
                    this.mContentType = contentType;
                    break;
                default:
                    this.mUsage = AudioAttributes.USAGE_UNKNOWN;
                    break;
            }
            return this;
        }

        public Builder setFlags(int flags) {
            this.mFlags |= flags & AudioAttributes.FLAG_ALL;
            return this;
        }

        public Builder addBundle(Bundle bundle) {
            if (bundle == null) {
                throw new IllegalArgumentException("Illegal null bundle");
            }
            if (this.mBundle == null) {
                this.mBundle = new Bundle(bundle);
            } else {
                this.mBundle.putAll(bundle);
            }
            return this;
        }

        public Builder addTag(String tag) {
            this.mTags.add(tag);
            return this;
        }

        public Builder setLegacyStreamType(int streamType) {
            return setInternalLegacyStreamType(streamType);
        }

        public Builder setInternalLegacyStreamType(int streamType) {
            switch (streamType) {
                case AudioAttributes.USAGE_UNKNOWN /*0*/:
                    this.mContentType = AudioAttributes.USAGE_MEDIA;
                    break;
                case AudioAttributes.USAGE_MEDIA /*1*/:
                    break;
                case AudioAttributes.USAGE_VOICE_COMMUNICATION /*2*/:
                    this.mContentType = AudioAttributes.USAGE_ALARM;
                    break;
                case AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING /*3*/:
                    this.mContentType = AudioAttributes.USAGE_VOICE_COMMUNICATION;
                    break;
                case AudioAttributes.USAGE_ALARM /*4*/:
                    this.mContentType = AudioAttributes.USAGE_ALARM;
                    break;
                case AudioAttributes.USAGE_NOTIFICATION /*5*/:
                    this.mContentType = AudioAttributes.USAGE_ALARM;
                    break;
                case AudioAttributes.USAGE_NOTIFICATION_RINGTONE /*6*/:
                    this.mContentType = AudioAttributes.USAGE_MEDIA;
                    this.mFlags |= AudioAttributes.USAGE_ALARM;
                    break;
                case AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST /*7*/:
                    this.mFlags |= AudioAttributes.USAGE_MEDIA;
                    break;
                case AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT /*8*/:
                    this.mContentType = AudioAttributes.USAGE_ALARM;
                    break;
                case AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_DELAYED /*9*/:
                    this.mContentType = AudioAttributes.USAGE_MEDIA;
                    break;
                default:
                    Log.e(AudioAttributes.TAG, "Invalid stream type " + streamType + " for AudioAttributes");
                    break;
            }
            this.mContentType = AudioAttributes.USAGE_ALARM;
            this.mUsage = AudioAttributes.usageForLegacyStreamType(streamType);
            return this;
        }

        public Builder setCapturePreset(int preset) {
            switch (preset) {
                case AudioAttributes.USAGE_UNKNOWN /*0*/:
                case AudioAttributes.USAGE_MEDIA /*1*/:
                case AudioAttributes.USAGE_NOTIFICATION /*5*/:
                case AudioAttributes.USAGE_NOTIFICATION_RINGTONE /*6*/:
                case AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST /*7*/:
                case AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_DELAYED /*9*/:
                    this.mSource = preset;
                    break;
                default:
                    Log.e(AudioAttributes.TAG, "Invalid capture preset " + preset + " for AudioAttributes");
                    break;
            }
            return this;
        }

        public Builder setInternalCapturePreset(int preset) {
            if (preset == AudioSource.HOTWORD || preset == AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT || preset == AudioSource.RADIO_TUNER) {
                this.mSource = preset;
            } else {
                setCapturePreset(preset);
            }
            return this;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioAttributes.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.AudioAttributes.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioAttributes.<clinit>():void");
    }

    private AudioAttributes() {
        this.mUsage = USAGE_UNKNOWN;
        this.mContentType = USAGE_UNKNOWN;
        this.mSource = -1;
        this.mFlags = USAGE_UNKNOWN;
    }

    public int getContentType() {
        return this.mContentType;
    }

    public int getUsage() {
        return this.mUsage;
    }

    public int getCapturePreset() {
        return this.mSource;
    }

    public int getFlags() {
        return this.mFlags & FLAG_ALL_PUBLIC;
    }

    public int getAllFlags() {
        return this.mFlags & FLAG_ALL;
    }

    public Bundle getBundle() {
        if (this.mBundle == null) {
            return this.mBundle;
        }
        return new Bundle(this.mBundle);
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(this.mTags);
    }

    public int describeContents() {
        return USAGE_UNKNOWN;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUsage);
        dest.writeInt(this.mContentType);
        dest.writeInt(this.mSource);
        dest.writeInt(this.mFlags);
        dest.writeInt(flags & USAGE_MEDIA);
        if ((flags & USAGE_MEDIA) == 0) {
            String[] tagsArray = new String[this.mTags.size()];
            this.mTags.toArray(tagsArray);
            dest.writeStringArray(tagsArray);
        } else if ((flags & USAGE_MEDIA) == USAGE_MEDIA) {
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
        this.mUsage = USAGE_UNKNOWN;
        this.mContentType = USAGE_UNKNOWN;
        this.mSource = -1;
        this.mFlags = USAGE_UNKNOWN;
        this.mUsage = in.readInt();
        this.mContentType = in.readInt();
        this.mSource = in.readInt();
        this.mFlags = in.readInt();
        boolean hasFlattenedTags = (in.readInt() & USAGE_MEDIA) == USAGE_MEDIA;
        this.mTags = new HashSet();
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
        switch (in.readInt()) {
            case ATTR_PARCEL_IS_NULL_BUNDLE /*-1977*/:
                this.mBundle = null;
            case ATTR_PARCEL_IS_VALID_BUNDLE /*1980*/:
                this.mBundle = new Bundle(in.readBundle());
            default:
                Log.e(TAG, "Illegal value unmarshalling AudioAttributes, can't initialize bundle");
        }
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioAttributes that = (AudioAttributes) o;
        if (this.mContentType == that.mContentType && this.mFlags == that.mFlags && this.mSource == that.mSource && this.mUsage == that.mUsage) {
            z = this.mFormattedTags.equals(that.mFormattedTags);
        }
        return z;
    }

    public int hashCode() {
        Object[] objArr = new Object[USAGE_NOTIFICATION_RINGTONE];
        objArr[USAGE_UNKNOWN] = Integer.valueOf(this.mContentType);
        objArr[USAGE_MEDIA] = Integer.valueOf(this.mFlags);
        objArr[USAGE_VOICE_COMMUNICATION] = Integer.valueOf(this.mSource);
        objArr[USAGE_VOICE_COMMUNICATION_SIGNALLING] = Integer.valueOf(this.mUsage);
        objArr[USAGE_ALARM] = this.mFormattedTags;
        objArr[USAGE_NOTIFICATION] = this.mBundle;
        return Objects.hash(objArr);
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("AudioAttributes: usage=").append(this.mUsage).append(" content=").append(this.mContentType).append(" flags=0x").append(Integer.toHexString(this.mFlags).toUpperCase()).append(" tags=").append(this.mFormattedTags).append(" bundle=");
        if (this.mBundle == null) {
            str = "null";
        } else {
            str = this.mBundle.toString();
        }
        return new String(append.append(str).toString());
    }

    public String usageToString() {
        return usageToString(this.mUsage);
    }

    public static String usageToString(int usage) {
        switch (usage) {
            case USAGE_UNKNOWN /*0*/:
                return new String("USAGE_UNKNOWN");
            case USAGE_MEDIA /*1*/:
                return new String("USAGE_MEDIA");
            case USAGE_VOICE_COMMUNICATION /*2*/:
                return new String("USAGE_VOICE_COMMUNICATION");
            case USAGE_VOICE_COMMUNICATION_SIGNALLING /*3*/:
                return new String("USAGE_VOICE_COMMUNICATION_SIGNALLING");
            case USAGE_ALARM /*4*/:
                return new String("USAGE_ALARM");
            case USAGE_NOTIFICATION /*5*/:
                return new String("USAGE_NOTIFICATION");
            case USAGE_NOTIFICATION_RINGTONE /*6*/:
                return new String("USAGE_NOTIFICATION_RINGTONE");
            case USAGE_NOTIFICATION_COMMUNICATION_REQUEST /*7*/:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_REQUEST");
            case USAGE_NOTIFICATION_COMMUNICATION_INSTANT /*8*/:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_INSTANT");
            case USAGE_NOTIFICATION_COMMUNICATION_DELAYED /*9*/:
                return new String("USAGE_NOTIFICATION_COMMUNICATION_DELAYED");
            case USAGE_NOTIFICATION_EVENT /*10*/:
                return new String("USAGE_NOTIFICATION_EVENT");
            case USAGE_ASSISTANCE_ACCESSIBILITY /*11*/:
                return new String("USAGE_ASSISTANCE_ACCESSIBILITY");
            case USAGE_ASSISTANCE_NAVIGATION_GUIDANCE /*12*/:
                return new String("USAGE_ASSISTANCE_NAVIGATION_GUIDANCE");
            case USAGE_ASSISTANCE_SONIFICATION /*13*/:
                return new String("USAGE_ASSISTANCE_SONIFICATION");
            case USAGE_GAME /*14*/:
                return new String("USAGE_GAME");
            default:
                return new String("unknown usage " + usage);
        }
    }

    public static int usageForLegacyStreamType(int streamType) {
        switch (streamType) {
            case USAGE_UNKNOWN /*0*/:
                return USAGE_VOICE_COMMUNICATION;
            case USAGE_MEDIA /*1*/:
            case USAGE_NOTIFICATION_COMMUNICATION_REQUEST /*7*/:
                return USAGE_ASSISTANCE_SONIFICATION;
            case USAGE_VOICE_COMMUNICATION /*2*/:
                return USAGE_NOTIFICATION_RINGTONE;
            case USAGE_VOICE_COMMUNICATION_SIGNALLING /*3*/:
                return USAGE_MEDIA;
            case USAGE_ALARM /*4*/:
                return USAGE_ALARM;
            case USAGE_NOTIFICATION /*5*/:
                return USAGE_NOTIFICATION;
            case USAGE_NOTIFICATION_RINGTONE /*6*/:
                return USAGE_VOICE_COMMUNICATION;
            case USAGE_NOTIFICATION_COMMUNICATION_INSTANT /*8*/:
                return USAGE_VOICE_COMMUNICATION_SIGNALLING;
            case USAGE_NOTIFICATION_COMMUNICATION_DELAYED /*9*/:
                return USAGE_ASSISTANCE_ACCESSIBILITY;
            default:
                return USAGE_UNKNOWN;
        }
    }

    public static int getVolumeControlStream(AudioAttributes aa) {
        if (aa != null) {
            return toVolumeStreamType(true, aa);
        }
        throw new IllegalArgumentException("Invalid null audio attributes");
    }

    public static int toLegacyStreamType(AudioAttributes aa) {
        return toVolumeStreamType(false, aa);
    }

    private static int toVolumeStreamType(boolean fromGetVolumeControlStream, AudioAttributes aa) {
        int i = USAGE_MEDIA;
        int i2 = USAGE_UNKNOWN;
        if ((aa.getFlags() & USAGE_MEDIA) == USAGE_MEDIA) {
            if (!fromGetVolumeControlStream) {
                i = USAGE_NOTIFICATION_COMMUNICATION_REQUEST;
            }
            return i;
        } else if ((aa.getFlags() & USAGE_ALARM) == USAGE_ALARM) {
            if (fromGetVolumeControlStream) {
                i = USAGE_UNKNOWN;
            } else {
                i = USAGE_NOTIFICATION_RINGTONE;
            }
            return i;
        } else {
            switch (aa.getUsage()) {
                case USAGE_UNKNOWN /*0*/:
                    if (fromGetVolumeControlStream) {
                        i = KeymasterDefs.KM_BIGNUM;
                    } else {
                        i = USAGE_VOICE_COMMUNICATION_SIGNALLING;
                    }
                    return i;
                case USAGE_MEDIA /*1*/:
                case USAGE_ASSISTANCE_ACCESSIBILITY /*11*/:
                case USAGE_ASSISTANCE_NAVIGATION_GUIDANCE /*12*/:
                case USAGE_GAME /*14*/:
                    return USAGE_VOICE_COMMUNICATION_SIGNALLING;
                case USAGE_VOICE_COMMUNICATION /*2*/:
                    return USAGE_UNKNOWN;
                case USAGE_VOICE_COMMUNICATION_SIGNALLING /*3*/:
                    if (!fromGetVolumeControlStream) {
                        i2 = USAGE_NOTIFICATION_COMMUNICATION_INSTANT;
                    }
                    return i2;
                case USAGE_ALARM /*4*/:
                    return USAGE_ALARM;
                case USAGE_NOTIFICATION /*5*/:
                case USAGE_NOTIFICATION_COMMUNICATION_REQUEST /*7*/:
                case USAGE_NOTIFICATION_COMMUNICATION_INSTANT /*8*/:
                case USAGE_NOTIFICATION_COMMUNICATION_DELAYED /*9*/:
                case USAGE_NOTIFICATION_EVENT /*10*/:
                    return USAGE_NOTIFICATION;
                case USAGE_NOTIFICATION_RINGTONE /*6*/:
                    return USAGE_VOICE_COMMUNICATION;
                case USAGE_ASSISTANCE_SONIFICATION /*13*/:
                    return USAGE_MEDIA;
                default:
                    if (!fromGetVolumeControlStream) {
                        return USAGE_VOICE_COMMUNICATION_SIGNALLING;
                    }
                    throw new IllegalArgumentException("Unknown usage value " + aa.getUsage() + " in audio attributes");
            }
        }
    }
}
