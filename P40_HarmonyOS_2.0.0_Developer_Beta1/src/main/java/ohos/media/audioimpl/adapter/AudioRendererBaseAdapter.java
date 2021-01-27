package ohos.media.audioimpl.adapter;

import android.app.ActivityThread;
import android.media.AudioAttributes;
import android.media.IAudioService;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import ohos.media.audio.AudioCapturerInfo;
import ohos.media.audio.AudioRendererInfo;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AudioRendererBaseAdapter {
    private static final String AUDIO_SERVICE = "audio";
    private static final String DESCRIPTOR = "android.media.IAudioService";
    private static final String GET_AUDIO_SERVICE_FAILED = "Get IAudioService failed.";
    private static final String INVALID_PARAMS = "Invalid Parameters.";
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioRendererBaseAdapter.class);
    private static final int NULL_BUNDLE = -1977;
    private static final int RENDERER_PIID_INVALID = -1;
    public static final int RENDERER_STATE_INITIALIZED = 1;
    public static final int RENDERER_STATE_PAUSED = 3;
    public static final int RENDERER_STATE_PLAYING = 2;
    public static final int RENDERER_STATE_RELEASED = 0;
    public static final int RENDERER_STATE_STOPPED = 4;
    public static final int RENDERER_STATE_UNINITIALIZED = -1;
    public static final int RENDERER_TYPE_AUDIORENDERER = 1;
    public static final int RENDERER_TYPE_MEDIAPLAYER = 2;
    public static final int RENDERER_TYPE_SOUNDPLAYER = 3;
    public static final int RENDERER_TYPE_UNKNOWN = -1;
    private static final int TRANSACTION_TRACK_PLAYER = 1;
    private static volatile IAudioService audioService;
    private final IBinder binder = ServiceManager.getService(AUDIO_SERVICE);
    private int rendererId = -1;
    private AudioRendererInfo rendererInfo;
    private int rendererType;

    public AudioRendererBaseAdapter(AudioRendererInfo audioRendererInfo, int i) {
        this.rendererInfo = audioRendererInfo;
        this.rendererType = i;
    }

    public void registerRendererBase() {
        LOGGER.debug("[registerRendererBase] begin", new Object[0]);
        AudioAttributes audioAttributes = getAudioAttributes(this.rendererInfo);
        if (audioAttributes != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(DESCRIPTOR);
                obtain.writeInt(1);
                obtain.writeInt(this.rendererType);
                obtain.writeString(ActivityThread.currentPackageName());
                obtain.writeInt(audioAttributes.getUsage());
                obtain.writeInt(audioAttributes.getContentType());
                obtain.writeInt(AudioCapturerInfo.AudioInputSource.AUDIO_INPUT_SOURCE_INVALID.getValue());
                obtain.writeInt(audioAttributes.getFlags());
                obtain.writeInt(0);
                obtain.writeStringArray(new String[0]);
                obtain.writeInt(NULL_BUNDLE);
                obtain.writeStrongBinder(null);
                LOGGER.debug("[registerRendererBase] status = %{public}s", Boolean.valueOf(this.binder.transact(1, obtain, obtain2, 0)));
                obtain2.readException();
                this.rendererId = obtain2.readInt();
                LOGGER.debug("[registerRendererBase] rendererIId = %{public}s", Integer.valueOf(this.rendererId));
            } catch (RemoteException e) {
                LOGGER.error("[registerRendererBase] occur exception = %{public}s", e.getMessage());
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                LOGGER.debug("[registerRendererBase] end", new Object[0]);
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            LOGGER.debug("[registerRendererBase] end", new Object[0]);
            return;
        }
        LOGGER.error("[registerRendererBase] rendererInfo is null.", new Object[0]);
        throw new IllegalArgumentException(INVALID_PARAMS);
    }

    public void unregisterRendererBase() {
        LOGGER.debug("[unregisterRendererBase] begin", new Object[0]);
        try {
            getService().releasePlayer(this.rendererId);
        } catch (RemoteException e) {
            LOGGER.error("[unregisterRendererBase] occur exception = %{public}s", e.getMessage());
        }
        LOGGER.debug("[unregisterRendererBase] end", new Object[0]);
    }

    public void updateState(int i) {
        LOGGER.debug("[updateState] begin state = %{public}d", Integer.valueOf(i));
        try {
            getService().playerEvent(this.rendererId, i);
        } catch (RemoteException e) {
            LOGGER.error("[updateState] occur exception = %{public}s", e.getMessage());
        }
        LOGGER.debug("[updateState] end", new Object[0]);
    }

    public void updateRendererInfo(AudioRendererInfo audioRendererInfo) {
        LOGGER.debug("[updateRendererInfo] begin", new Object[0]);
        AudioAttributes audioAttributes = getAudioAttributes(audioRendererInfo);
        if (audioAttributes != null) {
            try {
                getService().playerAttributes(this.rendererId, audioAttributes);
            } catch (RemoteException e) {
                LOGGER.error("[updateRendererInfo] occur exception = %{public}s", e.getMessage());
            }
            this.rendererInfo = audioRendererInfo;
            LOGGER.debug("[updateRendererInfo] end", new Object[0]);
            return;
        }
        throw new IllegalArgumentException(INVALID_PARAMS);
    }

    private static IAudioService getService() throws AudioRemoteAdapterException {
        if (audioService == null) {
            synchronized (IAudioService.class) {
                if (audioService == null) {
                    audioService = IAudioService.Stub.asInterface(ServiceManager.getService(AUDIO_SERVICE));
                }
            }
        }
        if (audioService != null) {
            return audioService;
        }
        LOGGER.error(GET_AUDIO_SERVICE_FAILED, new Object[0]);
        throw new AudioRemoteAdapterException(GET_AUDIO_SERVICE_FAILED);
    }

    private AudioAttributes getAudioAttributes(AudioRendererInfo audioRendererInfo) {
        if (audioRendererInfo == null || audioRendererInfo.getAudioStreamInfo() == null) {
            LOGGER.error("[getAudioAttributes] rendererInfo is null.", new Object[0]);
            return null;
        }
        AudioStreamInfo audioStreamInfo = audioRendererInfo.getAudioStreamInfo();
        return new AudioAttributes.Builder().setUsage(audioStreamInfo.getUsage().getValue()).setContentType(audioStreamInfo.getContentType().getValue()).setFlags(audioStreamInfo.getAudioStreamFlag().getValue()).build();
    }
}
