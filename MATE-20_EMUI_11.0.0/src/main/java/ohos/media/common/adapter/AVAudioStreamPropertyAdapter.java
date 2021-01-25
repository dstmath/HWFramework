package ohos.media.common.adapter;

import android.media.AudioAttributes;
import android.os.Bundle;
import ohos.media.audio.AudioCapturerInfo;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.common.AudioStreamProperty;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.adapter.PacMapUtils;

public class AVAudioStreamPropertyAdapter {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(AVAudioStreamPropertyAdapter.class);

    public static AudioAttributes getAudioAttributes(AudioStreamProperty audioStreamProperty) {
        if (audioStreamProperty == null) {
            LOGGER.error("getAudioAttributes failed, parameter is invalid", new Object[0]);
            return null;
        }
        AudioAttributes.Builder hapticChannelsMuted = new AudioAttributes.Builder().setUsage(audioStreamProperty.getStreamUsage().getValue()).setContentType(audioStreamProperty.getStreamContentType().getValue()).setFlags(audioStreamProperty.getStreamFlags()).setCapturePreset(audioStreamProperty.getStreamSource().getValue()).setHapticChannelsMuted(audioStreamProperty.isHapticChannelsMuted());
        if (audioStreamProperty.getPacMap() != null) {
            hapticChannelsMuted.addBundle(PacMapUtils.convertIntoBundle(audioStreamProperty.getPacMap()));
        }
        for (String str : audioStreamProperty.getStreamTags()) {
            hapticChannelsMuted.addTag(str);
        }
        return hapticChannelsMuted.build();
    }

    public static AudioStreamProperty getAudioStreamProperty(AudioAttributes audioAttributes) {
        if (audioAttributes == null) {
            LOGGER.error("getAudioStreamProperty failed, parameter is invalid", new Object[0]);
            return null;
        }
        AudioStreamProperty.Builder enableHapticChannelsMuted = new AudioStreamProperty.Builder().setStreamUsage(AudioStreamInfo.StreamUsage.getEnum(audioAttributes.getUsage())).setStreamContentType(AudioStreamInfo.ContentType.getEnum(audioAttributes.getContentType())).setStreamFlags(audioAttributes.getFlags()).addPacMap(PacMapUtils.convertFromBundle(audioAttributes.getBundle() == null ? new Bundle() : audioAttributes.getBundle())).setStreamSource(AudioCapturerInfo.AudioInputSource.getEnum(audioAttributes.getCapturePreset())).enableHapticChannelsMuted(audioAttributes.areHapticChannelsMuted());
        for (String str : audioAttributes.getTags()) {
            enableHapticChannelsMuted.addStreamTag(str);
        }
        return enableHapticChannelsMuted.build();
    }
}
