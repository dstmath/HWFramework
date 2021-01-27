package ohos.media.common.adapter;

import android.media.session.MediaController;
import android.os.Parcel;
import java.util.Optional;
import ohos.media.common.sessioncore.AVPlaybackInfo;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVPlaybackInfoAdapter {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(AVPlaybackInfoAdapter.class);
    private static final int PARCELABLE_FLAGS = 1;

    public static Optional<AVPlaybackInfo> getAVPlaybackInfo(MediaController.PlaybackInfo playbackInfo) {
        if (playbackInfo == null) {
            LOGGER.error("info is null", new Object[0]);
            return Optional.empty();
        } else if (playbackInfo.getAudioAttributes() != null) {
            return Optional.ofNullable(new AVPlaybackInfo(playbackInfo.getPlaybackType(), playbackInfo.getVolumeControl(), playbackInfo.getMaxVolume(), playbackInfo.getCurrentVolume(), AVAudioStreamPropertyAdapter.getAudioStreamProperty(playbackInfo.getAudioAttributes())));
        } else {
            LOGGER.error("info audioAttributes is null", new Object[0]);
            return Optional.empty();
        }
    }

    public static Optional<MediaController.PlaybackInfo> getPlaybackInfo(AVPlaybackInfo aVPlaybackInfo) {
        if (aVPlaybackInfo == null) {
            LOGGER.error("convertIntoAVPlaybackInfo controlInfo is null", new Object[0]);
            return Optional.empty();
        }
        Parcel obtain = Parcel.obtain();
        obtain.writeInt(aVPlaybackInfo.getAVPlaybackVolumeHandle());
        obtain.writeInt(aVPlaybackInfo.getAVPlaybackVolumeControl());
        obtain.writeInt(aVPlaybackInfo.getAVPlaybackMaxVolume());
        obtain.writeInt(aVPlaybackInfo.getAVPlaybackCurrentVolume());
        obtain.writeParcelable(AVAudioStreamPropertyAdapter.getAudioAttributes(aVPlaybackInfo.getAudioStreamProperty()), 1);
        obtain.recycle();
        return Optional.ofNullable((MediaController.PlaybackInfo) MediaController.PlaybackInfo.CREATOR.createFromParcel(obtain));
    }
}
