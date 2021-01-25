package ohos.media.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import ohos.media.audio.AudioCapturerInfo;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class AudioStreamProperty implements Sequenceable {
    public static final int ALLOW_RECORD_BY_ALL = 1;
    public static final int ALLOW_RECORD_BY_NONE = 3;
    public static final int ALLOW_RECORD_BY_SYSTEM = 2;
    private static final int AUDIO_STREAM_PARCEL_NULL_PACMAP = -1977;
    private static final int AUDIO_STREAM_PARCEL_VALID_PACMAP = 1980;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_AUDIBILITY_ENFORCED = 1;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_BEACON = 8;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_BYPASS_INTERRUPTION_POLICY = 64;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_BYPASS_MUTE = 128;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_DEEP_BUFFER = 512;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_DIRECT_OUTPUT = 1048576;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_HW_AV_SYNC = 16;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_HW_HOTWORD = 32;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_LOW_LATENCY = 256;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_MUTE_HAPTIC = 2048;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_NO_MEDIA_PROJECTION = 1024;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_NO_SYSTEM_CAPTURE = 4096;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_SCO = 4;
    public static final int AUDIO_STREAM_PROPERTY_FLAG_SECURE = 2;
    public static final Sequenceable.Producer<AudioStreamProperty> CREATOR = new Sequenceable.Producer<AudioStreamProperty>() {
        /* class ohos.media.common.AudioStreamProperty.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public AudioStreamProperty createFromParcel(Parcel parcel) {
            AudioStreamProperty audioStreamProperty = new AudioStreamProperty();
            audioStreamProperty.unmarshalling(parcel);
            return audioStreamProperty;
        }
    };
    private static final int FLAG_ALL = 1054719;
    private static final int FLATTEN_TAGS_FLAG = 1;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AudioStreamProperty.class);
    private AudioStreamInfo.ContentType contentType;
    private String formattedTags;
    private PacMap pacMap;
    private AudioCapturerInfo.AudioInputSource source;
    private int streamFlag;
    private AudioStreamInfo.StreamUsage streamUsage;
    private HashSet<String> tags;

    static /* synthetic */ int access$376(AudioStreamProperty audioStreamProperty, int i) {
        int i2 = i | audioStreamProperty.streamFlag;
        audioStreamProperty.streamFlag = i2;
        return i2;
    }

    private AudioStreamProperty() {
        this.streamUsage = AudioStreamInfo.StreamUsage.STREAM_USAGE_UNKNOWN;
        this.contentType = AudioStreamInfo.ContentType.CONTENT_TYPE_UNKNOWN;
        this.streamFlag = 0;
        this.source = AudioCapturerInfo.AudioInputSource.AUDIO_INPUT_SOURCE_DEFAULT;
    }

    public AudioStreamInfo.ContentType getStreamContentType() {
        return this.contentType;
    }

    public AudioStreamInfo.StreamUsage getStreamUsage() {
        return this.streamUsage;
    }

    public AudioCapturerInfo.AudioInputSource getStreamSource() {
        return this.source;
    }

    public int getStreamFlags() {
        return this.streamFlag & FLAG_ALL;
    }

    public PacMap getPacMap() {
        PacMap pacMap2 = this.pacMap;
        if (pacMap2 == null) {
            return pacMap2;
        }
        PacMap pacMap3 = new PacMap();
        pacMap3.putAll(this.pacMap.getAll());
        return pacMap3;
    }

    public Set<String> getStreamTags() {
        return Collections.unmodifiableSet(this.tags);
    }

    public boolean isHapticChannelsMuted() {
        return (this.streamFlag & 2048) != 0;
    }

    public int getAllowedRecordPolicy() {
        int i = this.streamFlag;
        if ((i & 4096) == 4096) {
            return 3;
        }
        return (i & 1024) == 1024 ? 2 : 1;
    }

    public static class Builder {
        private AudioStreamInfo.ContentType contentType = AudioStreamInfo.ContentType.CONTENT_TYPE_UNKNOWN;
        private boolean muteHapticChannels = true;
        private PacMap pacMap;
        private AudioCapturerInfo.AudioInputSource source = AudioCapturerInfo.AudioInputSource.AUDIO_INPUT_SOURCE_INVALID;
        private int streamFlag = 0;
        private AudioStreamInfo.StreamUsage streamUsage = AudioStreamInfo.StreamUsage.STREAM_USAGE_UNKNOWN;
        private HashSet<String> tags = new HashSet<>();

        public Builder() {
        }

        public Builder(AudioStreamProperty audioStreamProperty) {
            this.streamUsage = audioStreamProperty.streamUsage;
            this.contentType = audioStreamProperty.contentType;
            this.source = audioStreamProperty.source;
            this.streamFlag = audioStreamProperty.streamFlag;
            Object clone = audioStreamProperty.tags.clone();
            if (clone instanceof HashSet) {
                this.tags = (HashSet) clone;
            }
            this.muteHapticChannels = audioStreamProperty.isHapticChannelsMuted();
        }

        public AudioStreamProperty build() {
            AudioStreamProperty audioStreamProperty = new AudioStreamProperty();
            audioStreamProperty.contentType = this.contentType;
            audioStreamProperty.streamUsage = this.streamUsage;
            audioStreamProperty.source = this.source;
            audioStreamProperty.streamFlag = this.streamFlag;
            if (this.muteHapticChannels) {
                AudioStreamProperty.access$376(audioStreamProperty, 2048);
            }
            Object clone = this.tags.clone();
            if (clone instanceof HashSet) {
                audioStreamProperty.tags = (HashSet) clone;
            }
            audioStreamProperty.formattedTags = AudioStreamProperty.formatTags(";", this.tags);
            PacMap pacMap2 = this.pacMap;
            if (pacMap2 != null) {
                try {
                    Object clone2 = pacMap2.clone();
                    if (clone2 instanceof PacMap) {
                        audioStreamProperty.pacMap = (PacMap) clone2;
                    }
                } catch (CloneNotSupportedException e) {
                    AudioStreamProperty.LOGGER.error("AudioStreamInfo pacmap clone failed, error message:%{public}s", e.getMessage());
                }
            }
            return audioStreamProperty;
        }

        public Builder setStreamUsage(AudioStreamInfo.StreamUsage streamUsage2) {
            this.streamUsage = streamUsage2;
            return this;
        }

        public Builder setStreamContentType(AudioStreamInfo.ContentType contentType2) {
            this.contentType = contentType2;
            return this;
        }

        public Builder setStreamFlags(int i) {
            this.streamFlag = (i & AudioStreamProperty.FLAG_ALL) | this.streamFlag;
            return this;
        }

        public Builder replaceStreamFlags(int i) {
            this.streamFlag = i & AudioStreamProperty.FLAG_ALL;
            return this;
        }

        public Builder addStreamTag(String str) {
            this.tags.add(str);
            return this;
        }

        public Builder addPacMap(PacMap pacMap2) {
            if (pacMap2 != null) {
                PacMap pacMap3 = this.pacMap;
                if (pacMap3 == null) {
                    try {
                        Object clone = pacMap2.clone();
                        if (clone instanceof PacMap) {
                            this.pacMap = (PacMap) clone;
                        }
                    } catch (CloneNotSupportedException e) {
                        AudioStreamProperty.LOGGER.error("addPacMap pacmap clone failed, error message:%{public}s", e.getMessage());
                        return this;
                    }
                } else {
                    pacMap3.putAll(pacMap2);
                }
                return this;
            }
            throw new IllegalArgumentException("Illegal null PacMap");
        }

        public Builder setAllowedRecordPolicy(int i) {
            this.streamFlag = AudioStreamProperty.recordPolicyToFlags(i, this.streamFlag);
            return this;
        }

        public Builder setStreamSource(AudioCapturerInfo.AudioInputSource audioInputSource) {
            this.source = audioInputSource;
            return this;
        }

        public Builder enableHapticChannelsMuted(boolean z) {
            this.muteHapticChannels = z;
            return this;
        }
    }

    public static int recordPolicyToFlags(int i, int i2) {
        if (i == 1) {
            return i2 & -5121;
        }
        if (i == 2) {
            return (i2 | 1024) & -4097;
        }
        if (i == 3) {
            return i2 | 5120;
        }
        throw new IllegalArgumentException("unknown allow playback record policy");
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.streamUsage.getValue());
        parcel.writeInt(this.contentType.getValue());
        parcel.writeInt(this.source.getValue());
        parcel.writeInt(this.streamFlag);
        parcel.writeInt(1);
        parcel.writeString(this.formattedTags);
        if (this.pacMap == null) {
            parcel.writeInt(AUDIO_STREAM_PARCEL_NULL_PACMAP);
        } else {
            parcel.writeInt(AUDIO_STREAM_PARCEL_VALID_PACMAP);
            this.pacMap.marshalling(parcel);
        }
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        String[] readStringArray;
        this.streamUsage = AudioStreamInfo.StreamUsage.getEnum(parcel.readInt());
        this.contentType = AudioStreamInfo.ContentType.getEnum(parcel.readInt());
        this.source = AudioCapturerInfo.AudioInputSource.getEnum(parcel.readInt());
        this.streamFlag = parcel.readInt();
        if (parcel.readInt() == 1) {
            this.formattedTags = new String(parcel.readString());
            this.tags.add(this.formattedTags);
        } else {
            for (String str : parcel.readStringArray()) {
                this.tags.add(str);
            }
            this.formattedTags = formatTags(";", this.tags);
        }
        int readInt = parcel.readInt();
        if (readInt == AUDIO_STREAM_PARCEL_NULL_PACMAP) {
            this.pacMap = null;
        } else if (readInt == AUDIO_STREAM_PARCEL_VALID_PACMAP) {
            this.pacMap = new PacMap();
            this.pacMap.unmarshalling(parcel);
        } else {
            LOGGER.error("Illegal value unmarshalling AudioStreamProperty, get PacMap flag=%{public}d", Integer.valueOf(readInt));
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static String formatTags(CharSequence charSequence, HashSet<String> hashSet) {
        Iterator<String> it = hashSet.iterator();
        if (!it.hasNext()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(charSequence);
            sb.append(it.next());
        }
        return sb.toString();
    }
}
