package ohos.media.audioimpl.adapter;

import ohos.media.audio.AudioCapturerInfo;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AudioStreamInfoAdapter {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioStreamInfoAdapter.class);

    public static AudioStreamInfo.EncodingFormat convertEncodingFormat(int i) {
        LOGGER.debug("Converting encoding format = %{public}d", Integer.valueOf(i));
        if (i == 1) {
            return AudioStreamInfo.EncodingFormat.ENCODING_DEFAULT;
        }
        if (i == 2) {
            return AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT;
        }
        if (i == 3) {
            return AudioStreamInfo.EncodingFormat.ENCODING_PCM_8BIT;
        }
        if (i == 4) {
            return AudioStreamInfo.EncodingFormat.ENCODING_PCM_FLOAT;
        }
        if (i == 9) {
            return AudioStreamInfo.EncodingFormat.ENCODING_MP3;
        }
        LOGGER.warn("Unknown encoding format = %{public}d", Integer.valueOf(i));
        return AudioStreamInfo.EncodingFormat.ENCODING_INVALID;
    }

    public static AudioStreamInfo.ChannelMask convertChannelMaskIn(int i) {
        LOGGER.debug("Converting channel mask = %{public}x", Integer.valueOf(i));
        if (i == 1 || i == 12) {
            return AudioStreamInfo.ChannelMask.CHANNEL_IN_STEREO;
        }
        if (i == 16) {
            return AudioStreamInfo.ChannelMask.CHANNEL_IN_MONO;
        }
        LOGGER.warn("Unknown channel mask = %{public}x", Integer.valueOf(i));
        return AudioStreamInfo.ChannelMask.CHANNEL_INVALID;
    }

    public static AudioCapturerInfo.AudioInputSource convertInputSource(int i) {
        LOGGER.debug("Converting input source = %{public}d", Integer.valueOf(i));
        AudioCapturerInfo.AudioInputSource[] values = AudioCapturerInfo.AudioInputSource.values();
        for (AudioCapturerInfo.AudioInputSource audioInputSource : values) {
            if (audioInputSource.getValue() == i) {
                return audioInputSource;
            }
        }
        LOGGER.warn("Unknown input source = %{public}d", Integer.valueOf(i));
        return AudioCapturerInfo.AudioInputSource.AUDIO_INPUT_SOURCE_INVALID;
    }

    public static AudioStreamInfo.StreamUsage convertUsage(int i) {
        LOGGER.debug("Converting usage = %{public}d", Integer.valueOf(i));
        AudioStreamInfo.StreamUsage[] values = AudioStreamInfo.StreamUsage.values();
        for (AudioStreamInfo.StreamUsage streamUsage : values) {
            if (streamUsage.getValue() == i) {
                return streamUsage;
            }
        }
        LOGGER.warn("Unknown usage = %{public}d", Integer.valueOf(i));
        return AudioStreamInfo.StreamUsage.STREAM_USAGE_UNKNOWN;
    }

    public static AudioStreamInfo.ContentType convertContentType(int i) {
        LOGGER.debug("Converting contentType = %{public}d", Integer.valueOf(i));
        AudioStreamInfo.ContentType[] values = AudioStreamInfo.ContentType.values();
        for (AudioStreamInfo.ContentType contentType : values) {
            if (contentType.getValue() == i) {
                return contentType;
            }
        }
        LOGGER.warn("Unknown contentType = %{public}d", Integer.valueOf(i));
        return AudioStreamInfo.ContentType.CONTENT_TYPE_UNKNOWN;
    }

    public static AudioStreamInfo.AudioStreamFlag convertFlag(int i) {
        LOGGER.debug("Converting flag = %{public}d", Integer.valueOf(i));
        AudioStreamInfo.AudioStreamFlag[] values = AudioStreamInfo.AudioStreamFlag.values();
        for (AudioStreamInfo.AudioStreamFlag audioStreamFlag : values) {
            if (audioStreamFlag.getValue() == i) {
                return audioStreamFlag;
            }
        }
        LOGGER.warn("Unknown flag = %{public}d", Integer.valueOf(i));
        return AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_NONE;
    }
}
