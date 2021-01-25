package android.support.v4.media;

import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.media.AudioAttributesCompatApi21;
import android.util.SparseIntArray;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public class AudioAttributesCompat {
    private static final String AUDIO_ATTRIBUTES_CONTENT_TYPE = "android.support.v4.media.audio_attrs.CONTENT_TYPE";
    private static final String AUDIO_ATTRIBUTES_FLAGS = "android.support.v4.media.audio_attrs.FLAGS";
    private static final String AUDIO_ATTRIBUTES_FRAMEWORKS = "android.support.v4.media.audio_attrs.FRAMEWORKS";
    private static final String AUDIO_ATTRIBUTES_LEGACY_STREAM_TYPE = "android.support.v4.media.audio_attrs.LEGACY_STREAM_TYPE";
    private static final String AUDIO_ATTRIBUTES_USAGE = "android.support.v4.media.audio_attrs.USAGE";
    public static final int CONTENT_TYPE_MOVIE = 3;
    public static final int CONTENT_TYPE_MUSIC = 2;
    public static final int CONTENT_TYPE_SONIFICATION = 4;
    public static final int CONTENT_TYPE_SPEECH = 1;
    public static final int CONTENT_TYPE_UNKNOWN = 0;
    private static final int FLAG_ALL = 1023;
    private static final int FLAG_ALL_PUBLIC = 273;
    public static final int FLAG_AUDIBILITY_ENFORCED = 1;
    private static final int FLAG_BEACON = 8;
    private static final int FLAG_BYPASS_INTERRUPTION_POLICY = 64;
    private static final int FLAG_BYPASS_MUTE = 128;
    private static final int FLAG_DEEP_BUFFER = 512;
    public static final int FLAG_HW_AV_SYNC = 16;
    private static final int FLAG_HW_HOTWORD = 32;
    private static final int FLAG_LOW_LATENCY = 256;
    private static final int FLAG_SCO = 4;
    private static final int FLAG_SECURE = 2;
    private static final int[] SDK_USAGES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16};
    private static final int SUPPRESSIBLE_CALL = 2;
    private static final int SUPPRESSIBLE_NOTIFICATION = 1;
    private static final SparseIntArray SUPPRESSIBLE_USAGES = new SparseIntArray();
    private static final String TAG = "AudioAttributesCompat";
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
    public static final int USAGE_UNKNOWN = 0;
    private static final int USAGE_VIRTUAL_SOURCE = 15;
    public static final int USAGE_VOICE_COMMUNICATION = 2;
    public static final int USAGE_VOICE_COMMUNICATION_SIGNALLING = 3;
    private static boolean sForceLegacyBehavior;
    private AudioAttributesCompatApi21.Wrapper mAudioAttributesWrapper;
    int mContentType;
    int mFlags;
    Integer mLegacyStream;
    int mUsage;

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface AttributeContentType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface AttributeUsage {
    }

    static {
        SUPPRESSIBLE_USAGES.put(5, 1);
        SUPPRESSIBLE_USAGES.put(6, 2);
        SUPPRESSIBLE_USAGES.put(7, 2);
        SUPPRESSIBLE_USAGES.put(8, 1);
        SUPPRESSIBLE_USAGES.put(9, 1);
        SUPPRESSIBLE_USAGES.put(10, 1);
    }

    private AudioAttributesCompat() {
        this.mUsage = 0;
        this.mContentType = 0;
        this.mFlags = 0;
    }

    public int getVolumeControlStream() {
        if (Build.VERSION.SDK_INT < 26 || sForceLegacyBehavior || unwrap() == null) {
            return toVolumeStreamType(true, this);
        }
        return ((AudioAttributes) unwrap()).getVolumeControlStream();
    }

    @Nullable
    public Object unwrap() {
        if (this.mAudioAttributesWrapper != null) {
            return this.mAudioAttributesWrapper.unwrap();
        }
        return null;
    }

    public int getLegacyStreamType() {
        if (this.mLegacyStream != null) {
            return this.mLegacyStream.intValue();
        }
        if (Build.VERSION.SDK_INT < 21 || sForceLegacyBehavior) {
            return toVolumeStreamType(false, this.mFlags, this.mUsage);
        }
        return AudioAttributesCompatApi21.toLegacyStreamType(this.mAudioAttributesWrapper);
    }

    @Nullable
    public static AudioAttributesCompat wrap(@NonNull Object aa) {
        if (Build.VERSION.SDK_INT < 21 || sForceLegacyBehavior) {
            return null;
        }
        AudioAttributesCompat aac = new AudioAttributesCompat();
        aac.mAudioAttributesWrapper = AudioAttributesCompatApi21.Wrapper.wrap((AudioAttributes) aa);
        return aac;
    }

    public int getContentType() {
        if (Build.VERSION.SDK_INT < 21 || sForceLegacyBehavior || this.mAudioAttributesWrapper == null) {
            return this.mContentType;
        }
        return this.mAudioAttributesWrapper.unwrap().getContentType();
    }

    public int getUsage() {
        if (Build.VERSION.SDK_INT < 21 || sForceLegacyBehavior || this.mAudioAttributesWrapper == null) {
            return this.mUsage;
        }
        return this.mAudioAttributesWrapper.unwrap().getUsage();
    }

    public int getFlags() {
        if (Build.VERSION.SDK_INT >= 21 && !sForceLegacyBehavior && this.mAudioAttributesWrapper != null) {
            return this.mAudioAttributesWrapper.unwrap().getFlags();
        }
        int flags = this.mFlags;
        int legacyStream = getLegacyStreamType();
        if (legacyStream == 6) {
            flags |= 4;
        } else if (legacyStream == 7) {
            flags |= 1;
        }
        return flags & FLAG_ALL_PUBLIC;
    }

    @NonNull
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        if (Build.VERSION.SDK_INT >= 21) {
            bundle.putParcelable(AUDIO_ATTRIBUTES_FRAMEWORKS, this.mAudioAttributesWrapper.unwrap());
        } else {
            bundle.putInt(AUDIO_ATTRIBUTES_USAGE, this.mUsage);
            bundle.putInt(AUDIO_ATTRIBUTES_CONTENT_TYPE, this.mContentType);
            bundle.putInt(AUDIO_ATTRIBUTES_FLAGS, this.mFlags);
            if (this.mLegacyStream != null) {
                bundle.putInt(AUDIO_ATTRIBUTES_LEGACY_STREAM_TYPE, this.mLegacyStream.intValue());
            }
        }
        return bundle;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static AudioAttributesCompat fromBundle(Bundle bundle) {
        Integer num = null;
        if (bundle == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes frameworkAttrs = (AudioAttributes) bundle.getParcelable(AUDIO_ATTRIBUTES_FRAMEWORKS);
            if (frameworkAttrs == null) {
                return null;
            }
            return wrap(frameworkAttrs);
        }
        int usage = bundle.getInt(AUDIO_ATTRIBUTES_USAGE, 0);
        int contentType = bundle.getInt(AUDIO_ATTRIBUTES_CONTENT_TYPE, 0);
        int flags = bundle.getInt(AUDIO_ATTRIBUTES_FLAGS, 0);
        AudioAttributesCompat attr = new AudioAttributesCompat();
        attr.mUsage = usage;
        attr.mContentType = contentType;
        attr.mFlags = flags;
        if (bundle.containsKey(AUDIO_ATTRIBUTES_LEGACY_STREAM_TYPE)) {
            num = Integer.valueOf(bundle.getInt(AUDIO_ATTRIBUTES_LEGACY_STREAM_TYPE));
        }
        attr.mLegacyStream = num;
        return attr;
    }

    public static class Builder {
        private Object mAAObject;
        private int mContentType = 0;
        private int mFlags = 0;
        private Integer mLegacyStream;
        private int mUsage = 0;

        public Builder() {
        }

        public Builder(AudioAttributesCompat aa) {
            this.mUsage = aa.mUsage;
            this.mContentType = aa.mContentType;
            this.mFlags = aa.mFlags;
            this.mLegacyStream = aa.mLegacyStream;
            this.mAAObject = aa.unwrap();
        }

        public AudioAttributesCompat build() {
            if (AudioAttributesCompat.sForceLegacyBehavior || Build.VERSION.SDK_INT < 21) {
                AudioAttributesCompat aac = new AudioAttributesCompat();
                aac.mContentType = this.mContentType;
                aac.mFlags = this.mFlags;
                aac.mUsage = this.mUsage;
                aac.mLegacyStream = this.mLegacyStream;
                aac.mAudioAttributesWrapper = null;
                return aac;
            } else if (this.mAAObject != null) {
                return AudioAttributesCompat.wrap(this.mAAObject);
            } else {
                AudioAttributes.Builder api21Builder = new AudioAttributes.Builder().setContentType(this.mContentType).setFlags(this.mFlags).setUsage(this.mUsage);
                if (this.mLegacyStream != null) {
                    api21Builder.setLegacyStreamType(this.mLegacyStream.intValue());
                }
                return AudioAttributesCompat.wrap(api21Builder.build());
            }
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
                    this.mUsage = usage;
                    break;
                case 16:
                    if (!AudioAttributesCompat.sForceLegacyBehavior && Build.VERSION.SDK_INT > 25) {
                        this.mUsage = usage;
                        break;
                    } else {
                        this.mUsage = 12;
                        break;
                    }
                default:
                    this.mUsage = 0;
                    break;
            }
            return this;
        }

        public Builder setContentType(int contentType) {
            switch (contentType) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    this.mContentType = contentType;
                    break;
                default:
                    this.mUsage = 0;
                    break;
            }
            return this;
        }

        public Builder setFlags(int flags) {
            this.mFlags |= flags & AudioAttributesCompat.FLAG_ALL;
            return this;
        }

        public Builder setLegacyStreamType(int streamType) {
            if (streamType != 10) {
                this.mLegacyStream = Integer.valueOf(streamType);
                this.mUsage = AudioAttributesCompat.usageForStreamType(streamType);
                return this;
            }
            throw new IllegalArgumentException("STREAM_ACCESSIBILITY is not a legacy stream type that was used for audio playback");
        }
    }

    public int hashCode() {
        if (Build.VERSION.SDK_INT < 21 || sForceLegacyBehavior || this.mAudioAttributesWrapper == null) {
            return Arrays.hashCode(new Object[]{Integer.valueOf(this.mContentType), Integer.valueOf(this.mFlags), Integer.valueOf(this.mUsage), this.mLegacyStream});
        }
        return this.mAudioAttributesWrapper.unwrap().hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AudioAttributesCompat:");
        if (unwrap() != null) {
            sb.append(" audioattributes=");
            sb.append(unwrap());
        } else {
            if (this.mLegacyStream != null) {
                sb.append(" stream=");
                sb.append(this.mLegacyStream);
                sb.append(" derived");
            }
            sb.append(" usage=");
            sb.append(usageToString());
            sb.append(" content=");
            sb.append(this.mContentType);
            sb.append(" flags=0x");
            sb.append(Integer.toHexString(this.mFlags).toUpperCase());
        }
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public String usageToString() {
        return usageToString(this.mUsage);
    }

    static String usageToString(int usage) {
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
        }
    }

    private static abstract class AudioManagerHidden {
        public static final int STREAM_ACCESSIBILITY = 10;
        public static final int STREAM_BLUETOOTH_SCO = 6;
        public static final int STREAM_SYSTEM_ENFORCED = 7;
        public static final int STREAM_TTS = 9;

        private AudioManagerHidden() {
        }
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
            default:
                return 0;
            case 10:
                return 11;
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static void setForceLegacyBehavior(boolean force) {
        sForceLegacyBehavior = force;
    }

    static int toVolumeStreamType(boolean fromGetVolumeControlStream, AudioAttributesCompat aa) {
        return toVolumeStreamType(fromGetVolumeControlStream, aa.getFlags(), aa.getUsage());
    }

    static int toVolumeStreamType(boolean fromGetVolumeControlStream, int flags, int usage) {
        if ((flags & 1) == 1) {
            if (fromGetVolumeControlStream) {
                return 1;
            }
            return 7;
        } else if ((flags & 4) != 4) {
            switch (usage) {
                case 0:
                    if (fromGetVolumeControlStream) {
                        return Integer.MIN_VALUE;
                    }
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
                    throw new IllegalArgumentException("Unknown usage value " + usage + " in audio attributes");
            }
        } else if (fromGetVolumeControlStream) {
            return 0;
        } else {
            return 6;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioAttributesCompat that = (AudioAttributesCompat) o;
        if (Build.VERSION.SDK_INT >= 21 && !sForceLegacyBehavior && this.mAudioAttributesWrapper != null) {
            return this.mAudioAttributesWrapper.unwrap().equals(that.unwrap());
        }
        if (this.mContentType == that.getContentType() && this.mFlags == that.getFlags() && this.mUsage == that.getUsage()) {
            if (this.mLegacyStream != null) {
                if (this.mLegacyStream.equals(that.mLegacyStream)) {
                    return true;
                }
            } else if (that.mLegacyStream == null) {
                return true;
            }
        }
        return false;
    }
}
