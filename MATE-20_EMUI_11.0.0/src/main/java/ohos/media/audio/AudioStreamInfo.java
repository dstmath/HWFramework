package ohos.media.audio;

import ohos.media.codec.ProfileLevel;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class AudioStreamInfo {
    private static final int ALL_VALID_OUT_CHANNELS = ((((((((ChannelMask.CHANNEL_OUT_FRONT_LEFT.getValue() | ChannelMask.CHANNEL_OUT_FRONT_RIGHT.getValue()) | ChannelMask.CHANNEL_OUT_FRONT_CENTER.getValue()) | ChannelMask.CHANNEL_OUT_LOW_FREQUENCY.getValue()) | ChannelMask.CHANNEL_OUT_BACK_LEFT.getValue()) | ChannelMask.CHANNEL_OUT_BACK_RIGHT.getValue()) | ChannelMask.CHANNEL_OUT_BACK_CENTER.getValue()) | ChannelMask.CHANNEL_OUT_SIDE_LEFT.getValue()) | ChannelMask.CHANNEL_OUT_SIDE_RIGHT.getValue());
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioStreamInfo.class);
    private static final int MAX_CHANNEL_COUNT = 8;
    public static final int SAMPLE_RATE_FOR_DIRECT_HZ_MAX = 384000;
    public static final int SAMPLE_RATE_HZ_MAX = 192000;
    public static final int SAMPLE_RATE_HZ_MIN = 4000;
    public static final int SAMPLE_RATE_UNSPECIFIED = 4000;
    private final ChannelMask channelMask;
    private final ContentType contentType;
    private final EncodingFormat encodingFormat;
    private final int sampleRate;
    private final AudioStreamFlag streamFlag;
    private final StreamUsage streamUsage;

    /* synthetic */ AudioStreamInfo(EncodingFormat encodingFormat2, int i, ChannelMask channelMask2, StreamUsage streamUsage2, ContentType contentType2, AudioStreamFlag audioStreamFlag, AnonymousClass1 r7) {
        this(encodingFormat2, i, channelMask2, streamUsage2, contentType2, audioStreamFlag);
    }

    private AudioStreamInfo(EncodingFormat encodingFormat2, int i, ChannelMask channelMask2, StreamUsage streamUsage2, ContentType contentType2, AudioStreamFlag audioStreamFlag) {
        this.encodingFormat = encodingFormat2;
        this.sampleRate = i;
        this.channelMask = channelMask2;
        this.streamUsage = streamUsage2;
        this.contentType = contentType2;
        this.streamFlag = audioStreamFlag;
    }

    public EncodingFormat getEncodingFormat() {
        return this.encodingFormat;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public ChannelMask getChannelMask() {
        return this.channelMask;
    }

    public StreamUsage getUsage() {
        return this.streamUsage;
    }

    public StreamType getStreamType() {
        switch (this.streamUsage) {
            case STREAM_USAGE_MEDIA:
            case STREAM_USAGE_GAME:
            case STREAM_USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
            case STREAM_USAGE_ASSISTANT:
                return StreamType.STREAM_TYPE_MUSIC;
            case STREAM_USAGE_ASSISTANCE_SONIFICATION:
                return StreamType.STREAM_TYPE_SYSTEM;
            case STREAM_USAGE_VOICE_COMMUNICATION:
            case STREAM_USAGE_VOICE_COMMUNICATION_SIGNALLING:
                return StreamType.STREAM_TYPE_VOICE_CALL;
            case STREAM_USAGE_ALARM:
                return StreamType.STREAM_TYPE_ALARM;
            case STREAM_USAGE_NOTIFICATION_RINGTONE:
                return StreamType.STREAM_TYPE_RING;
            case STREAM_USAGE_NOTIFICATION:
            case STREAM_USAGE_NOTIFICATION_COMMUNICATION_REQUEST:
            case STREAM_USAGE_NOTIFICATION_COMMUNICATION_INSTANT:
            case STREAM_USAGE_NOTIFICATION_COMMUNICATION_DELAYED:
            case STREAM_USAGE_NOTIFICATION_EVENT:
                return StreamType.STREAM_TYPE_NOTIFICATION;
            case STREAM_USAGE_ASSISTANCE_ACCESSIBILITY:
                return StreamType.STREAM_TYPE_ACCESSIBILITY;
            default:
                return StreamType.STREAM_TYPE_MUSIC;
        }
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public AudioStreamFlag getAudioStreamFlag() {
        return this.streamFlag;
    }

    public String toString() {
        return "AudioStreamInfo: encodingFormat = " + this.encodingFormat + ", sampleRate = " + this.sampleRate + ", channelMask = " + this.channelMask + ", streamUsage = " + this.streamUsage + ", contentType = " + this.contentType + ", streamFlag = " + this.streamFlag;
    }

    public static class Builder {
        private ChannelMask channelMask = ChannelMask.CHANNEL_INVALID;
        private ContentType contentType = ContentType.CONTENT_TYPE_UNKNOWN;
        private EncodingFormat encodingFormat = EncodingFormat.ENCODING_DEFAULT;
        private int sampleRate = 4000;
        private AudioStreamFlag streamFlag = AudioStreamFlag.AUDIO_STREAM_FLAG_NONE;
        private StreamUsage streamUsage = StreamUsage.STREAM_USAGE_UNKNOWN;

        public Builder encodingFormat(EncodingFormat encodingFormat2) {
            if (encodingFormat2 == EncodingFormat.ENCODING_DEFAULT) {
                this.encodingFormat = EncodingFormat.ENCODING_PCM_16BIT;
            } else {
                this.encodingFormat = encodingFormat2;
            }
            return this;
        }

        public Builder sampleRate(int i) {
            this.sampleRate = i;
            return this;
        }

        public Builder channelMask(ChannelMask channelMask2) {
            if (channelMask2 == ChannelMask.CHANNEL_OUT_DEFAULT) {
                this.channelMask = ChannelMask.CHANNEL_OUT_MONO;
            } else {
                this.channelMask = channelMask2;
            }
            return this;
        }

        public Builder streamUsage(StreamUsage streamUsage2) {
            this.streamUsage = streamUsage2;
            setContentTypeFromStreamUsage(streamUsage2);
            return this;
        }

        private void setContentTypeFromStreamUsage(StreamUsage streamUsage2) {
            int i = AnonymousClass1.$SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[streamUsage2.ordinal()];
            if (i != 1) {
                if (!(i == 15 || i == 17)) {
                    switch (i) {
                        case 5:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                            this.contentType = ContentType.CONTENT_TYPE_SONIFICATION;
                            return;
                        case 6:
                            break;
                        default:
                            this.contentType = ContentType.CONTENT_TYPE_UNKNOWN;
                            return;
                    }
                }
                this.contentType = ContentType.CONTENT_TYPE_SPEECH;
                return;
            }
            this.contentType = ContentType.CONTENT_TYPE_MUSIC;
        }

        public Builder audioStreamFlag(AudioStreamFlag audioStreamFlag) {
            this.streamFlag = audioStreamFlag;
            return this;
        }

        public AudioStreamInfo build() {
            return new AudioStreamInfo(this.encodingFormat, this.sampleRate, this.channelMask, this.streamUsage, this.contentType, this.streamFlag, null);
        }
    }

    public enum EncodingFormat {
        ENCODING_INVALID(0),
        ENCODING_DEFAULT(1),
        ENCODING_PCM_16BIT(1),
        ENCODING_PCM_8BIT(2),
        ENCODING_PCM_FLOAT(5),
        ENCODING_MP3(ProfileLevel.HEVC_MAIN_TIER_LEVEL_6_2);
        
        private final int encodingFormatValue;

        private EncodingFormat(int i) {
            this.encodingFormatValue = i;
        }

        public int getValue() {
            return this.encodingFormatValue;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.media.audio.AudioStreamInfo$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat = new int[EncodingFormat.values().length];

        static {
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[EncodingFormat.ENCODING_PCM_8BIT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[EncodingFormat.ENCODING_DEFAULT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[EncodingFormat.ENCODING_PCM_16BIT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[EncodingFormat.ENCODING_PCM_FLOAT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[EncodingFormat.ENCODING_MP3.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage = new int[StreamUsage.values().length];
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_MEDIA.ordinal()] = 1;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_GAME.ordinal()] = 2;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_ASSISTANCE_NAVIGATION_GUIDANCE.ordinal()] = 3;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_ASSISTANT.ordinal()] = 4;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_ASSISTANCE_SONIFICATION.ordinal()] = 5;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_VOICE_COMMUNICATION.ordinal()] = 6;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_VOICE_COMMUNICATION_SIGNALLING.ordinal()] = 7;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_ALARM.ordinal()] = 8;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_NOTIFICATION_RINGTONE.ordinal()] = 9;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_NOTIFICATION.ordinal()] = 10;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_NOTIFICATION_COMMUNICATION_REQUEST.ordinal()] = 11;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_NOTIFICATION_COMMUNICATION_INSTANT.ordinal()] = 12;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_NOTIFICATION_COMMUNICATION_DELAYED.ordinal()] = 13;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_NOTIFICATION_EVENT.ordinal()] = 14;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_ASSISTANCE_ACCESSIBILITY.ordinal()] = 15;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_UNKNOWN.ordinal()] = 16;
            } catch (NoSuchFieldError unused21) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[StreamUsage.STREAM_USAGE_TTS.ordinal()] = 17;
            } catch (NoSuchFieldError unused22) {
            }
        }
    }

    static int getBytesCountForFormat(EncodingFormat encodingFormat2) {
        int i = AnonymousClass1.$SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[encodingFormat2.ordinal()];
        if (i == 1) {
            return 1;
        }
        int i2 = 2;
        if (!(i == 2 || i == 3)) {
            i2 = 4;
            if (i != 4) {
                LOGGER.error("getBytesCountForFormat error, not pcm format", new Object[0]);
                return 1;
            }
        }
        return i2;
    }

    public static boolean isValidEncodingFormat(EncodingFormat encodingFormat2) {
        int i = AnonymousClass1.$SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[encodingFormat2.ordinal()];
        return i == 1 || i == 2 || i == 3 || i == 4 || i == 5;
    }

    public enum ChannelMask {
        CHANNEL_INVALID(0),
        CHANNEL_OUT_DEFAULT(1),
        CHANNEL_OUT_FRONT_LEFT(1),
        CHANNEL_OUT_FRONT_RIGHT(2),
        CHANNEL_OUT_FRONT_CENTER(4),
        CHANNEL_OUT_LOW_FREQUENCY(8),
        CHANNEL_OUT_BACK_LEFT(16),
        CHANNEL_OUT_BACK_RIGHT(32),
        CHANNEL_OUT_FRONT_LEFT_OF_CENTER(64),
        CHANNEL_OUT_FRONT_RIGHT_OF_CENTER(128),
        CHANNEL_OUT_BACK_CENTER(256),
        CHANNEL_OUT_SIDE_LEFT(512),
        CHANNEL_OUT_SIDE_RIGHT(1024),
        CHANNEL_OUT_TOP_CENTER(2048),
        CHANNEL_OUT_TOP_FRONT_LEFT(4096),
        CHANNEL_OUT_TOP_FRONT_CENTER(8192),
        CHANNEL_OUT_TOP_FRONT_RIGHT(16384),
        CHANNEL_OUT_TOP_BACK_LEFT(32768),
        CHANNEL_OUT_TOP_BACK_CENTER(65536),
        CHANNEL_OUT_TOP_BACK_RIGHT(131072),
        CHANNEL_OUT_MONO(1),
        CHANNEL_OUT_STEREO(3),
        CHANNEL_OUT_QUAD(51),
        CHANNEL_OUT_QUAD_SIDE(1539),
        CHANNEL_OUT_SURROUND(263),
        CHANNEL_OUT_5POINT1(63),
        CHANNEL_OUT_5POINT1_SIDE(1551),
        CHANNEL_OUT_7POINT1(1599),
        CHANNEL_IN_MONO(16),
        CHANNEL_IN_STEREO(12);
        
        private final int channelMask;

        private ChannelMask(int i) {
            this.channelMask = i;
        }

        public int getValue() {
            return this.channelMask;
        }
    }

    public static boolean isValidOutChannelMask(ChannelMask channelMask2) {
        int value = channelMask2.getValue();
        if ((ALL_VALID_OUT_CHANNELS & value) != value) {
            LOGGER.error("isValidOutChannelMask error, mask has unsupported channels, mask = %{public}x", Integer.valueOf(value));
            return false;
        }
        if (!(channelMask2 == ChannelMask.CHANNEL_OUT_MONO || channelMask2 == ChannelMask.CHANNEL_OUT_STEREO || channelMask2 == ChannelMask.CHANNEL_OUT_FRONT_LEFT || channelMask2 == ChannelMask.CHANNEL_OUT_DEFAULT)) {
            if (getChannelCount(channelMask2) > 8) {
                LOGGER.error("isValidOutChannelMask error, mask has too many channels", new Object[0]);
                return false;
            }
            int value2 = ChannelMask.CHANNEL_OUT_FRONT_LEFT.getValue() | ChannelMask.CHANNEL_OUT_FRONT_RIGHT.getValue();
            if ((value & value2) != value2) {
                LOGGER.error("isValidOutChannelMask error, front left and front right must be present", new Object[0]);
                return false;
            }
            int value3 = ChannelMask.CHANNEL_OUT_BACK_LEFT.getValue() | ChannelMask.CHANNEL_OUT_BACK_RIGHT.getValue();
            int i = value3 & value;
            if (i == 0 || i == value3) {
                int value4 = ChannelMask.CHANNEL_OUT_SIDE_LEFT.getValue() | ChannelMask.CHANNEL_OUT_SIDE_RIGHT.getValue();
                int i2 = value & value4;
                if (!(i2 == 0 || i2 == value4)) {
                    LOGGER.error("isValidOutChannelMask error, side channels can't be used independently", new Object[0]);
                    return false;
                }
            } else {
                LOGGER.error("isValidOutChannelMask error, back channels can't be used independently", new Object[0]);
                return false;
            }
        }
        return true;
    }

    public static int getChannelCount(ChannelMask channelMask2) {
        return Integer.bitCount(channelMask2.getValue());
    }

    public enum StreamUsage {
        STREAM_USAGE_UNKNOWN(0),
        STREAM_USAGE_MEDIA(1),
        STREAM_USAGE_VOICE_COMMUNICATION(2),
        STREAM_USAGE_VOICE_COMMUNICATION_SIGNALLING(3),
        STREAM_USAGE_ALARM(4),
        STREAM_USAGE_NOTIFICATION(5),
        STREAM_USAGE_NOTIFICATION_RINGTONE(6),
        STREAM_USAGE_NOTIFICATION_COMMUNICATION_REQUEST(7),
        STREAM_USAGE_NOTIFICATION_COMMUNICATION_INSTANT(8),
        STREAM_USAGE_NOTIFICATION_COMMUNICATION_DELAYED(9),
        STREAM_USAGE_NOTIFICATION_EVENT(10),
        STREAM_USAGE_ASSISTANCE_ACCESSIBILITY(11),
        STREAM_USAGE_ASSISTANCE_NAVIGATION_GUIDANCE(12),
        STREAM_USAGE_ASSISTANCE_SONIFICATION(13),
        STREAM_USAGE_GAME(14),
        STREAM_USAGE_VIRTUAL_SOURCE(15),
        STREAM_USAGE_ASSISTANT(16),
        STREAM_USAGE_TTS(17);
        
        private final int streamUsageValue;

        private StreamUsage(int i) {
            this.streamUsageValue = i;
        }

        public int getValue() {
            return this.streamUsageValue;
        }

        public static StreamUsage getEnum(int i) {
            StreamUsage[] values = values();
            for (StreamUsage streamUsage : values) {
                if (streamUsage.getValue() == i) {
                    return streamUsage;
                }
            }
            return STREAM_USAGE_UNKNOWN;
        }
    }

    public enum ContentType {
        CONTENT_TYPE_UNKNOWN(0),
        CONTENT_TYPE_SPEECH(1),
        CONTENT_TYPE_MUSIC(2),
        CONTENT_TYPE_MOVIE(3),
        CONTENT_TYPE_SONIFICATION(4);
        
        private final int contentTypeValue;

        private ContentType(int i) {
            this.contentTypeValue = i;
        }

        public int getValue() {
            return this.contentTypeValue;
        }

        public static ContentType getEnum(int i) {
            ContentType[] values = values();
            for (ContentType contentType : values) {
                if (contentType.getValue() == i) {
                    return contentType;
                }
            }
            return CONTENT_TYPE_UNKNOWN;
        }
    }

    public enum AudioStreamFlag {
        AUDIO_STREAM_FLAG_NONE(0),
        AUDIO_STREAM_FLAG_AUDIBILITY_ENFORCED(1),
        AUDIO_STREAM_FLAG_SECURE(2),
        AUDIO_STREAM_FLAG_SCO(4),
        AUDIO_STREAM_FLAG_BEACON(8),
        AUDIO_STREAM_FLAG_HW_AV_SYNC(16),
        AUDIO_STREAM_FLAG_HW_HOTWORD(32),
        AUDIO_STREAM_FLAG_BYPASS_INTERRUPTION_POLICY(64),
        AUDIO_STREAM_FLAG_BYPASS_MUTE(128),
        AUDIO_STREAM_FLAG_LOW_LATENCY(256),
        AUDIO_STREAM_FLAG_DEEP_BUFFER(512),
        AUDIO_STREAM_FLAG_NO_MEDIA_PROJECTION(1024),
        AUDIO_STREAM_FLAG_MUTE_HAPTIC(2048),
        AUDIO_STREAM_FLAG_NO_SYSTEM_CAPTURE(4096),
        AUDIO_STREAM_FLAG_DIRECT_OUTPUT(1048576),
        AUDIO_STREAM_FLAG_MAY_DUCK(2097152),
        AUDIO_STREAM_FLAG_MAY_RESUME(4194304);
        
        private final int streamFlag;

        private AudioStreamFlag(int i) {
            this.streamFlag = i;
        }

        public int getValue() {
            return this.streamFlag;
        }
    }

    public enum StreamType {
        STREAM_TYPE_VOICE_CALL(0),
        STREAM_TYPE_SYSTEM(1),
        STREAM_TYPE_RING(2),
        STREAM_TYPE_MUSIC(3),
        STREAM_TYPE_ALARM(4),
        STREAM_TYPE_NOTIFICATION(5),
        STREAM_TYPE_DTMF(6),
        STREAM_TYPE_ACCESSIBILITY(7);
        
        private final int streamType;

        private StreamType(int i) {
            this.streamType = i;
        }

        public int getValue() {
            return this.streamType;
        }
    }
}
