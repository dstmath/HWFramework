package ohos.media.common.adapter;

import android.app.ActivityThread;
import android.media.AudioAttributes;
import android.media.AudioPlaybackConfiguration;
import android.media.IAudioService;
import android.media.IPlayer;
import android.media.PlayerBase;
import android.media.VolumeShaper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class PlayerCommonAdapter {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(PlayerCommonAdapter.class);
    public static final int TYPE_AUDIO_PLAYER = 1;
    public static final int TYPE_MEDIA_PLAYER = 2;
    private static IAudioService audioService;
    private int playerIId = -1;
    private final Object playerLock = new Object();
    private int playerState = -1;
    private int playerType = -1;

    private void updateState(int i) {
        int i2;
        synchronized (this.playerLock) {
            this.playerState = i;
            i2 = this.playerIId;
        }
        try {
            getService().playerEvent(i2, i);
        } catch (RemoteException e) {
            LOGGER.error("Error talking to audio service %{public}s for piid = %{public}d, %{public}s", AudioPlaybackConfiguration.toLogFriendlyPlayerState(i), Integer.valueOf(i2), e);
        }
    }

    private static IAudioService getService() {
        IAudioService iAudioService = audioService;
        if (iAudioService != null) {
            return iAudioService;
        }
        audioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return audioService;
    }

    public PlayerCommonAdapter(int i) {
        this.playerType = i;
    }

    public void registerPlayer() {
        try {
            Parcel obtain = Parcel.obtain();
            obtain.writeInt(this.playerType);
            obtain.writeString(ActivityThread.currentPackageName());
            new AudioAttributes.Builder().build().writeToParcel(obtain, 0);
            obtain.writeStrongBinder(new IPlayerCommonWrapper().asBinder());
            obtain.recycle();
            this.playerIId = getService().trackPlayer((PlayerBase.PlayerIdCard) PlayerBase.PlayerIdCard.CREATOR.createFromParcel(obtain));
        } catch (RemoteException e) {
            LOGGER.error("Error talking to audio service, player will not be tracked for %{public}s", e, new Object[0]);
        }
    }

    public void commonStart() {
        updateState(2);
    }

    public void commonStop() {
        updateState(4);
    }

    public void commonPause() {
        updateState(3);
    }

    public void commonRelease() {
        boolean z;
        synchronized (this.playerLock) {
            if (this.playerState != 0) {
                z = true;
                this.playerState = 0;
            } else {
                z = false;
            }
        }
        if (z) {
            try {
                getService().releasePlayer(this.playerIId);
            } catch (RemoteException e) {
                LOGGER.error("Error talking to audio service, the player will still be tracked for %{public}s", e, new Object[0]);
            }
        }
    }

    private static class IPlayerCommonWrapper extends IPlayer.Stub {
        public void applyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation) {
        }

        public int getAudioSessionId() {
            return -1;
        }

        public void pause() {
        }

        public void setPan(float f) {
        }

        public void setStartDelayMs(int i) {
        }

        public void setVolume(float f) {
        }

        public void start() {
        }

        public void stop() {
        }

        private IPlayerCommonWrapper() {
        }
    }
}
