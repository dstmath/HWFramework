package ohos.media.player;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import ohos.aafwk.content.IntentFilter;
import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.app.Context;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.global.resource.BaseFileDescriptor;
import ohos.ivicommon.drivingsafety.DrivingSafetyClient;
import ohos.ivicommon.drivingsafety.model.ControlItemEnum;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.common.BufferInfo;
import ohos.media.common.Format;
import ohos.media.common.Source;
import ohos.media.common.adapter.PlayerCommonAdapter;
import ohos.media.extractor.Extractor;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.rpc.RemoteException;
import ohos.utils.Parcel;
import ohos.utils.ParcelException;

public class Player {
    private static final String COMMON_EVENT_DRIVE_MODE_ACTION_KEY = "com.action.ivi.drvmod_change";
    private static final int DEFAULT_INDEX = 0;
    private static final int KEY_PARAMETER_AUDIO_STREAM_PROPERTY = 1400;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(Player.class);
    private static final int MAX_RETRY_TIMES = 100;
    private static final int MEDIA_PLAYER_PAUSED = 32;
    private static final int MEDIA_PLAYER_STARTED = 16;
    private static final int MSG_WRITE_BUFFER = 0;
    private static final int PLAYER_BUFFERING_UPDATE = 3;
    private static final int PLAYER_ERROR = 100;
    public static final int PLAYER_ERROR_IO = -1004;
    public static final int PLAYER_ERROR_MALFORMED = -1007;
    public static final int PLAYER_ERROR_SERVER_DIED = 100;
    public static final int PLAYER_ERROR_SYSTEM = Integer.MIN_VALUE;
    public static final int PLAYER_ERROR_TIMED_OUT = -110;
    public static final int PLAYER_ERROR_UNKNOWN = 1;
    public static final int PLAYER_ERROR_UNSUPPORTED = -1010;
    private static final int PLAYER_INFO = 200;
    public static final int PLAYER_INFO_AUDIO_NOT_PLAYING = 804;
    public static final int PLAYER_INFO_BAD_INTERLEAVING = 800;
    public static final int PLAYER_INFO_BUFFERING_END = 702;
    public static final int PLAYER_INFO_BUFFERING_START = 701;
    public static final int PLAYER_INFO_EXTERNAL_METADATA_UPDATE = 803;
    public static final int PLAYER_INFO_METADATA_UPDATE = 802;
    public static final int PLAYER_INFO_NETWORK_BANDWIDTH = 703;
    public static final int PLAYER_INFO_NOT_SEEKABLE = 801;
    public static final int PLAYER_INFO_STARTED_AS_NEXT = 2;
    public static final int PLAYER_INFO_SUBTITLE_TIMED_OUT = 902;
    public static final int PLAYER_INFO_TIMED_TEXT_ERROR = 900;
    public static final int PLAYER_INFO_UNKNOWN = 1;
    public static final int PLAYER_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int PLAYER_INFO_VIDEO_NOT_PLAYING = 805;
    public static final int PLAYER_INFO_VIDEO_RENDERING_START = 3;
    public static final int PLAYER_INFO_VIDEO_TRACK_LAGGING = 700;
    private static final int PLAYER_INVALID_DURATION = -1;
    private static final int PLAYER_META_DATA_AVAILABLE = 202;
    private static final int PLAYER_PLAYBACK_COMPLETE = 2;
    private static final int PLAYER_PREPARED = 1;
    private static final int PLAYER_SEEK_COMPLETE = 4;
    private static final int PLAYER_SUBTITLE_DATA = 201;
    private static final int PLAYER_TIME_DISCONTINUITY = 211;
    private static final int PLAYER_VIDEO_SIZE_CHANGED = 5;
    private static final int RETRY_INTERVAL_MS = 10;
    private static final int REWIND_CLOSEST = 3;
    private static final int REWIND_INVALID = -1;
    private static final int REWIND_PREVIOUS_SYNC = 0;
    private static final int SOURCE_TYPE_URI = 1;
    private static final int SUCCESS = 0;
    private static final long THOUSAND_NUMBER = 1000;
    public static final int VIDEO_SCALE_TYPE_CROP = 2;
    public static final int VIDEO_SCALE_TYPE_FIT = 1;
    private Context appContext = null;
    private boolean awake = false;
    private volatile PlayerCallbackEventHandler callbackEventHandler;
    private final Object callbackLock = new Object();
    private final PlayerCommonAdapter commonPlayer;
    private volatile int duration = -1;
    private List<PlayerEventHandler> eventHandlerList = null;
    private volatile Extractor extractor = null;
    private Map<Integer, Format> formatMap = null;
    private volatile boolean isDriverMode = false;
    private boolean looping = false;
    private long nativeZPlayer = 0;
    private volatile IPlayerCallback playerCallback;
    private IPlayerListener playerInternalCallback = null;
    private volatile boolean sawEos = true;
    private boolean screenOn = false;
    private Source source = null;
    private PlaySourceType sourceType = PlaySourceType.ONLY_AUDIO;
    private SurfaceOps surfaceHolder = null;
    private boolean useDistributed = false;
    private AtomicInteger writeThreadCount = new AtomicInteger(0);

    public interface IPlayerCallback {
        void onBufferingChange(int i);

        void onError(int i, int i2);

        void onMediaTimeIncontinuity(MediaTimeInfo mediaTimeInfo);

        void onMessage(int i, int i2);

        void onNewTimedMetaData(MediaTimedMetaData mediaTimedMetaData);

        void onPlayBackComplete();

        void onPrepared();

        void onResolutionChanged(int i, int i2);

        void onRewindToComplete();
    }

    /* access modifiers changed from: private */
    public interface IPlayerListener {
        void onError(int i, int i2);

        void onPlayBackComplete();
    }

    /* access modifiers changed from: private */
    public enum PlaySourceType {
        ONLY_AUDIO,
        HAVE_VIDEO
    }

    private native int nativeEnableLooping(boolean z);

    private native int nativeGetAudioStreamSessionId();

    private native int nativeGetAudioStreamType();

    private native int nativeGetCurrentPosition();

    private native int nativeGetDuration(long j);

    private native float nativeGetPlaybackSpeed();

    private native int nativeGetPlayerState();

    private native int nativeGetVideoHeight(long j);

    private native int nativeGetVideoWidth(long j);

    private static native void nativeInit();

    private native boolean nativeIsLooping(long j);

    private native boolean nativeIsNowPlaying(long j);

    private native int nativePause();

    private native int nativePlay();

    private native int nativePrepare();

    private native int nativePrepareTrack(int i, String[] strArr, Object[] objArr);

    private native int nativeRegisterInternalCallback(IPlayerListener iPlayerListener);

    private native int nativeRelease();

    private native int nativeReset();

    private native int nativeRewindTo(long j, int i);

    private native int nativeRewindToDistributed(boolean z);

    private native int nativeSetAudioStreamSessionId(int i);

    private native int nativeSetAudioStreamType(int i);

    private native int nativeSetNextPlayer(long j, long j2);

    private native int nativeSetParameter(int i, Parcel parcel);

    private native int nativeSetPlaybackSpeed(float f);

    private native int nativeSetSource(Source source2);

    private native int nativeSetVideoScaleType(int i);

    private native int nativeSetVideoSize(int i, int i2);

    private native int nativeSetVideoSurface(Surface surface);

    private native int nativeSetVolume(float f, float f2);

    private native void nativeSetup();

    private native void nativeSetupDistributed(String str);

    private native int nativeStartTrackRenderer();

    private native int nativeStop();

    private native int nativeWriteBuffer(int i, byte[] bArr, BufferInfo bufferInfo);

    static {
        System.loadLibrary("zplayer_jni.z");
        nativeInit();
    }

    private class RunTask implements Runnable {
        private PlayerCommonAdapter player;
        private Thread thread;
        private String threadName;

        public RunTask(String str, PlayerCommonAdapter playerCommonAdapter) {
            this.threadName = str;
            this.player = playerCommonAdapter;
        }

        @Override // java.lang.Runnable
        public void run() {
            PlayerCommonAdapter playerCommonAdapter = this.player;
            if (playerCommonAdapter != null) {
                playerCommonAdapter.registerPlayer();
            } else {
                Player.LOGGER.warn("PlayerCommonAdapter is null", new Object[0]);
            }
            Player.this.registerDriverModeEvent();
        }

        public void start() {
            if (this.thread == null) {
                this.thread = new Thread(this, this.threadName);
                this.thread.start();
            }
        }
    }

    public Player(Context context) {
        this.appContext = context;
        nativeSetup();
        this.useDistributed = false;
        this.commonPlayer = new PlayerCommonAdapter(2);
        new RunTask(toString(), this.commonPlayer).start();
    }

    public Player(List<DeviceInfo> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("deviceInfo can't be null");
        }
        nativeSetupDistributed(list.get(0).getDeviceId());
        this.duration = -1;
        this.useDistributed = true;
        this.commonPlayer = new PlayerCommonAdapter(2);
        this.commonPlayer.registerPlayer();
    }

    public boolean setSource(Source source2) {
        if (source2 == null) {
            LOGGER.error("source must not be null", new Object[0]);
            return false;
        }
        this.source = source2;
        if (!this.useDistributed) {
            int nativeSetSource = nativeSetSource(source2);
            if (nativeSetSource == 0) {
                return true;
            }
            LOGGER.error("setSource for media player failed, error code is %{public}d", Integer.valueOf(nativeSetSource));
            return false;
        } else if (source2.getFileType() != 1) {
            LOGGER.error("distributed player only support uri source", new Object[0]);
            return false;
        } else {
            this.extractor = new Extractor();
            this.duration = -1;
            return this.extractor.setSource(source2);
        }
    }

    public boolean setSource(BaseFileDescriptor baseFileDescriptor) {
        if (baseFileDescriptor == null) {
            LOGGER.error("setSource with asset fd failed, assetFD is null", new Object[0]);
            return false;
        }
        FileDescriptor fileDescriptor = baseFileDescriptor.getFileDescriptor();
        if (fileDescriptor == null) {
            LOGGER.error("setSource with asset fd failed, fd is null", new Object[0]);
            return false;
        }
        long startPosition = baseFileDescriptor.getStartPosition();
        if (startPosition < 0) {
            LOGGER.error("setSource with asset fd failed, offset invalid", new Object[0]);
            return false;
        }
        long fileSize = baseFileDescriptor.getFileSize();
        if (fileSize > 0) {
            return setSource(new Source(fileDescriptor, startPosition, fileSize));
        }
        LOGGER.error("setSource with asset fd failed, length invalid", new Object[0]);
        return false;
    }

    public boolean prepare() {
        if (!this.useDistributed) {
            int nativePrepare = nativePrepare();
            if (nativePrepare == 0) {
                return true;
            }
            LOGGER.error("prepare media player failed, error code is %{public}d", Integer.valueOf(nativePrepare));
            return false;
        } else if (this.source != null && this.extractor != null) {
            return doExtractor();
        } else {
            LOGGER.error("prepare failed, source must be set first", new Object[0]);
            return false;
        }
    }

    public boolean play() {
        int i;
        if (!DrivingSafetyClient.isDrivingSafety(this.appContext, ControlItemEnum.VIDEO)) {
            LOGGER.warn("Currently in driving mode, Disable video playback", new Object[0]);
            return false;
        }
        if (this.useDistributed) {
            i = nativeStartTrackRenderer();
        } else {
            i = nativePlay();
        }
        if (i != 0) {
            LOGGER.error("start media player failed, error code is %{public}d", Integer.valueOf(i));
            return false;
        }
        if (this.playerInternalCallback == null) {
            this.playerInternalCallback = new PlayerInternalCallback();
            if (!registerInternalCallback(this.playerInternalCallback)) {
                LOGGER.warn("play registerInternalCallback failed", new Object[0]);
            }
        }
        keepAwake(true);
        this.commonPlayer.commonStart();
        return true;
    }

    public boolean isNowPlaying() {
        return nativeIsNowPlaying(this.nativeZPlayer);
    }

    public boolean pause() {
        int nativePause = nativePause();
        if (nativePause != 0) {
            LOGGER.error("pause media player failed, error code is %{public}d", Integer.valueOf(nativePause));
            return false;
        }
        keepAwake(false);
        this.commonPlayer.commonPause();
        return true;
    }

    public boolean stop() {
        if (this.useDistributed) {
            LOGGER.debug("Distributed stop media player begin", new Object[0]);
            this.sawEos = true;
        }
        int nativeStop = nativeStop();
        if (nativeStop != 0) {
            LOGGER.error("stop media player failed, error code is %{public}d", Integer.valueOf(nativeStop));
            return false;
        }
        keepAwake(false);
        this.commonPlayer.commonStop();
        return true;
    }

    public boolean rewindTo(long j) {
        if (!this.useDistributed) {
            int nativeRewindTo = nativeRewindTo(j, 3);
            if (nativeRewindTo == 0) {
                return true;
            }
            LOGGER.error("rewind media player failed, error code is %{public}d", Integer.valueOf(nativeRewindTo));
            return false;
        } else if (this.source != null && this.extractor != null) {
            return rewindToInDistributed(j, 3);
        } else {
            LOGGER.error("rewindTo failed, source must be set first", new Object[0]);
            return false;
        }
    }

    public boolean setVolume(float f) {
        if (f > 1.0f || f < ConstantValue.MIN_ZOOM_VALUE) {
            LOGGER.error("volume %{public}f is invalid", Float.valueOf(f));
            return false;
        }
        float f2 = f * 1.0f * 1.0f;
        int nativeSetVolume = nativeSetVolume(f2, f2);
        if (nativeSetVolume == 0) {
            return true;
        }
        LOGGER.error("setVolume media player failed, error code is %{public}d", Integer.valueOf(nativeSetVolume));
        return false;
    }

    public boolean setVideoSurface(Surface surface) {
        if (surface == null) {
            LOGGER.error("setVideoSurface media player failed, parameter is null", new Object[0]);
            return false;
        }
        int nativeSetVideoSurface = nativeSetVideoSurface(surface);
        if (nativeSetVideoSurface != 0) {
            LOGGER.error("setVideoSurface media player failed, error code is %{public}d", Integer.valueOf(nativeSetVideoSurface));
            return false;
        }
        this.surfaceHolder = null;
        return true;
    }

    public boolean setSurfaceOps(SurfaceOps surfaceOps) {
        if (surfaceOps == null) {
            LOGGER.error("setSurfaceOps failed, surfaceHolder is null", new Object[0]);
            return false;
        }
        Surface surface = surfaceOps.getSurface();
        if (surface == null) {
            LOGGER.error("setSurfaceOps failed, surface is null", new Object[0]);
            return false;
        }
        int nativeSetVideoSurface = nativeSetVideoSurface(surface);
        if (nativeSetVideoSurface != 0) {
            LOGGER.error("setSurfaceOps failed, error code is %{public}d", Integer.valueOf(nativeSetVideoSurface));
            return false;
        }
        this.surfaceHolder = surfaceOps;
        updateScreenOn();
        return true;
    }

    private void updateScreenOn() {
        SurfaceOps surfaceOps = this.surfaceHolder;
        if (surfaceOps != null) {
            surfaceOps.setKeepScreenOn(this.screenOn && this.awake);
        }
    }

    public boolean enableScreenOn(boolean z) {
        if (this.surfaceHolder == null) {
            LOGGER.error("enableScreenOn failed, surfaceHolder is null", new Object[0]);
            return false;
        } else if (this.screenOn == z) {
            return true;
        } else {
            this.screenOn = z;
            updateScreenOn();
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void keepAwake(boolean z) {
        this.awake = z;
        updateScreenOn();
    }

    public boolean setVideoScaleType(int i) {
        if (i == 1 || i == 2) {
            int nativeSetVideoScaleType = nativeSetVideoScaleType(i);
            if (nativeSetVideoScaleType == 0) {
                return true;
            }
            LOGGER.error("setVideoScaleType failed, error code is %{public}d", Integer.valueOf(nativeSetVideoScaleType));
            return false;
        }
        LOGGER.error("setVideoScaleType failed, mode:%{public}d is invalid", Integer.valueOf(i));
        return false;
    }

    public boolean enableSingleLooping(boolean z) {
        if (this.useDistributed) {
            if (z && this.playerInternalCallback == null) {
                this.playerInternalCallback = new PlayerInternalCallback();
                if (!registerInternalCallback(this.playerInternalCallback)) {
                    LOGGER.error("enableSingleLooping registerInternalCallback failed", new Object[0]);
                    return false;
                }
            }
            this.looping = z;
            return true;
        }
        int nativeEnableLooping = nativeEnableLooping(z);
        if (nativeEnableLooping == 0) {
            return true;
        }
        LOGGER.error("enableSingleLooping media player failed, error code is %{public}d", Integer.valueOf(nativeEnableLooping));
        return false;
    }

    public boolean isSingleLooping() {
        if (this.useDistributed) {
            return this.looping;
        }
        return nativeIsLooping(this.nativeZPlayer);
    }

    public int getPlayerState() {
        return nativeGetPlayerState();
    }

    public int getCurrentTime() {
        return nativeGetCurrentPosition();
    }

    public int getDuration() {
        if (this.useDistributed) {
            return this.duration != -1 ? this.duration : getDurationInDistributed();
        }
        return nativeGetDuration(this.nativeZPlayer);
    }

    private int getDurationInDistributed() {
        if (this.source == null || this.extractor == null) {
            LOGGER.error("getDurationInDistributed extractor is null", new Object[0]);
            return -1;
        }
        int totalStreams = this.extractor.getTotalStreams();
        if (totalStreams <= 0) {
            LOGGER.error("getTotalStreams failed, totalTrackNums is %{public}d", Integer.valueOf(totalStreams));
            return -1;
        }
        this.extractor.specifyStream(0);
        Format streamFormat = this.extractor.getStreamFormat(0);
        if (streamFormat.hasKey(Format.DURATION)) {
            this.duration = new Long(streamFormat.getLongValue(Format.DURATION) / THOUSAND_NUMBER).intValue();
        }
        return this.duration;
    }

    public boolean setVideoSize(int i, int i2) {
        int nativeSetVideoSize = nativeSetVideoSize(i, i2);
        if (nativeSetVideoSize == 0) {
            return true;
        }
        LOGGER.error("setVideoSize failed, error code:%{public}d, width:%{public}d, height:%{public}d", Integer.valueOf(nativeSetVideoSize), Integer.valueOf(i), Integer.valueOf(i2));
        return false;
    }

    public int getVideoWidth() {
        return nativeGetVideoWidth(this.nativeZPlayer);
    }

    public int getVideoHeight() {
        return nativeGetVideoHeight(this.nativeZPlayer);
    }

    public boolean setPlaybackSpeed(float f) {
        int nativeSetPlaybackSpeed = nativeSetPlaybackSpeed(f);
        if (nativeSetPlaybackSpeed == 0) {
            return true;
        }
        LOGGER.error("setPlaybackSpeed failed, error code is %{public}d", Integer.valueOf(nativeSetPlaybackSpeed));
        return false;
    }

    public float getPlaybackSpeed() {
        return nativeGetPlaybackSpeed();
    }

    public boolean setAudioStreamType(int i) {
        int nativeSetAudioStreamType = nativeSetAudioStreamType(i);
        if (nativeSetAudioStreamType == 0) {
            return true;
        }
        LOGGER.error("setAudioStreamType failed, error code is %{public}d", Integer.valueOf(nativeSetAudioStreamType));
        return false;
    }

    public int getAudioStreamType() {
        return nativeGetAudioStreamType();
    }

    public boolean setAudioStreamSessionId(int i) {
        if (i < 0) {
            LOGGER.error("setAudioStreamSessionId failed, sessionId: %{public}d is invalid", Integer.valueOf(i));
            return false;
        }
        int nativeSetAudioStreamSessionId = nativeSetAudioStreamSessionId(i);
        if (nativeSetAudioStreamSessionId == 0) {
            return true;
        }
        LOGGER.error("setAudioStreamSessionId failed, error code is %{public}d", Integer.valueOf(nativeSetAudioStreamSessionId));
        return false;
    }

    public int getAudioStreamSessionId() {
        return nativeGetAudioStreamSessionId();
    }

    public boolean setNextPlayer(Player player) {
        if (player == null) {
            LOGGER.error("setNextPlayer failed, input player is null", new Object[0]);
            return false;
        }
        int nativeSetNextPlayer = nativeSetNextPlayer(this.nativeZPlayer, player.nativeZPlayer);
        if (nativeSetNextPlayer == 0) {
            return true;
        }
        LOGGER.error("setNextPlayer failed, error code is %{public}d", Integer.valueOf(nativeSetNextPlayer));
        return false;
    }

    public boolean reset() {
        Map<Integer, Format> map = this.formatMap;
        if (map != null) {
            map.clear();
        }
        int nativeReset = nativeReset();
        if (nativeReset != 0) {
            LOGGER.error("nativeReset media player failed, error code is %{public}d", Integer.valueOf(nativeReset));
            return false;
        }
        keepAwake(false);
        return true;
    }

    public boolean release() {
        if (this.useDistributed) {
            LOGGER.debug("Distributed release media player begin", new Object[0]);
            this.sawEos = true;
        }
        int nativeRelease = nativeRelease();
        if (nativeRelease != 0) {
            LOGGER.error("release media player failed, error code is %{public}d", Integer.valueOf(nativeRelease));
            return false;
        }
        keepAwake(false);
        this.commonPlayer.commonRelease();
        return true;
    }

    public void setPlayerCallback(IPlayerCallback iPlayerCallback) {
        synchronized (this.callbackLock) {
            this.playerCallback = iPlayerCallback;
        }
    }

    /* access modifiers changed from: private */
    public static class PlayerCallbackData {
        public int arg1;
        public int arg2;
        public Parcel parcel;

        public PlayerCallbackData(int i, int i2, Parcel parcel2) {
            this.arg1 = i;
            this.arg2 = i2;
            this.parcel = parcel2;
        }
    }

    private class PlayerCallbackEventHandler extends EventHandler {
        private PlayerCallbackEventHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            Player.super.processEvent(innerEvent);
            if (innerEvent == null || innerEvent.object == null || !(innerEvent.object instanceof PlayerCallbackData)) {
                Player.LOGGER.error("processEvent failed, param error.", new Object[0]);
                return;
            }
            synchronized (Player.this.callbackLock) {
                if (Player.this.playerCallback == null) {
                    Player.LOGGER.error("processEvent failed, playerCallback is null.", new Object[0]);
                    return;
                }
                PlayerCallbackData playerCallbackData = (PlayerCallbackData) innerEvent.object;
                int i = innerEvent.eventId;
                if (i == 1) {
                    Player.this.playerCallback.onPrepared();
                } else if (i == 2) {
                    Player.this.playerCallback.onPlayBackComplete();
                } else if (i == 3) {
                    Player.this.playerCallback.onBufferingChange(playerCallbackData.arg1);
                } else if (i == 4) {
                    Player.this.playerCallback.onRewindToComplete();
                } else if (i == 5) {
                    Player.this.playerCallback.onResolutionChanged(playerCallbackData.arg1, playerCallbackData.arg2);
                } else if (i == 100) {
                    Player.this.playerCallback.onError(playerCallbackData.arg1, playerCallbackData.arg2);
                } else if (i != 200) {
                    processObjectEvent(i, playerCallbackData);
                } else {
                    Player.this.playerCallback.onMessage(playerCallbackData.arg1, playerCallbackData.arg2);
                }
            }
        }

        private void processObjectEvent(int i, PlayerCallbackData playerCallbackData) {
            MediaTimeInfo createFromParcel;
            if (i == Player.PLAYER_META_DATA_AVAILABLE) {
                MediaTimedMetaData createFromParcel2 = MediaTimedMetaData.createFromParcel(playerCallbackData.parcel);
                if (createFromParcel2 != null) {
                    Player.this.playerCallback.onNewTimedMetaData(createFromParcel2);
                }
            } else if (i == Player.PLAYER_TIME_DISCONTINUITY && (createFromParcel = MediaTimeInfo.createFromParcel(playerCallbackData.parcel)) != null) {
                Player.this.playerCallback.onMediaTimeIncontinuity(createFromParcel);
            }
        }
    }

    private void onNativeEventReceived(int i, int i2, int i3, Parcel parcel) {
        synchronized (this.callbackLock) {
            if (this.playerCallback != null) {
                if (this.callbackEventHandler == null) {
                    EventRunner create = EventRunner.create("PlayerCallbackEvent");
                    if (create == null) {
                        LOGGER.error("onNativeEventReceived failed, can't create EventRunner.", new Object[0]);
                        return;
                    }
                    this.callbackEventHandler = new PlayerCallbackEventHandler(create);
                }
                InnerEvent innerEvent = InnerEvent.get(i, new PlayerCallbackData(i2, i3, parcel));
                if (innerEvent == null) {
                    LOGGER.error("onNativeEventReceived failed, can't create inner event.", new Object[0]);
                } else {
                    this.callbackEventHandler.sendEvent(innerEvent);
                }
            }
        }
    }

    public static final class MediaTimedMetaData {
        public byte[] metaData;
        public long timestampUs;

        public static MediaTimedMetaData createFromParcel(Parcel parcel) {
            MediaTimedMetaData mediaTimedMetaData = new MediaTimedMetaData();
            if (parcel == null || parcel.getReadableBytes() == 0) {
                Player.LOGGER.error("MediaTimedMetaData.createFromParcel failed, parcel data invalid.", new Object[0]);
                return null;
            }
            mediaTimedMetaData.timestampUs = parcel.readLong();
            if (parcel.getReadableBytes() == 0) {
                Player.LOGGER.error("MediaTimedMetaData.createFromParcel failed, parcel data error.", new Object[0]);
                return null;
            }
            try {
                mediaTimedMetaData.metaData = parcel.readByteArray();
                byte[] bArr = mediaTimedMetaData.metaData;
                if (bArr != null && bArr.length != 0) {
                    return mediaTimedMetaData;
                }
                Player.LOGGER.error("MediaTimedMetaData.createFromParcel failed, parcel data error.", new Object[0]);
                return null;
            } catch (ParcelException unused) {
                Player.LOGGER.error("MediaTimedMetaData.createFromParcel failed, parcel data length error.", new Object[0]);
                return null;
            }
        }
    }

    public static class MediaTimeInfo {
        public float clockRate;
        public long mediaTimeUs;
        public long nanoTime;

        public static MediaTimeInfo createFromParcel(Parcel parcel) {
            MediaTimeInfo mediaTimeInfo = new MediaTimeInfo();
            if (parcel == null || parcel.getReadableBytes() == 0) {
                Player.LOGGER.error("MediaTimeInfo.createFromParcel failed, parcel data invalid.", new Object[0]);
                return null;
            }
            mediaTimeInfo.mediaTimeUs = parcel.readLong();
            if (parcel.getReadableBytes() == 0) {
                Player.LOGGER.error("MediaTimeInfo.createFromParcel failed, parcel data error.", new Object[0]);
                return null;
            }
            mediaTimeInfo.nanoTime = parcel.readLong() * Player.THOUSAND_NUMBER;
            if (parcel.getReadableBytes() == 0) {
                Player.LOGGER.error("MediaTimeInfo.createFromParcel failed, parcel data error.", new Object[0]);
                return null;
            }
            mediaTimeInfo.clockRate = parcel.readFloat();
            return mediaTimeInfo;
        }
    }

    public static class DeviceInfo {
        private String deviceId;
        private int trackId;

        public DeviceInfo(String str) {
            this.deviceId = str;
        }

        public DeviceInfo(String str, int i) {
            this.deviceId = str;
            this.trackId = i;
        }

        public String getDeviceId() {
            return this.deviceId;
        }
    }

    /* access modifiers changed from: package-private */
    public class PlayerEventHandler extends EventHandler {
        private long microseconds;
        private int mode;
        private int trackId;

        private PlayerEventHandler(EventRunner eventRunner, int i) {
            super(eventRunner);
            this.trackId = i;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void rewindTo(long j, int i) {
            this.microseconds = j;
            this.mode = i;
        }

        public void processEvent(InnerEvent innerEvent) {
            Player.super.processEvent(innerEvent);
            if (innerEvent != null) {
                int i = innerEvent.eventId;
                Player.LOGGER.info("processEvent, eventid=%{public}d, trackId=%{public}d, microseconds=%{public}d, mode=%{public}d", Integer.valueOf(innerEvent.eventId), Integer.valueOf(this.trackId), Long.valueOf(this.microseconds), Integer.valueOf(this.mode));
                if (i != 0) {
                    Player.LOGGER.error("Unexpected message %{public}d", Integer.valueOf(i));
                    return;
                }
                Player.this.writeThreadCount.incrementAndGet();
                Player.this.extractAndWriteBuffer(this.trackId, this.microseconds, this.mode);
                Player.this.writeThreadCount.decrementAndGet();
            }
        }
    }

    /* access modifiers changed from: private */
    public class PlayerInternalCallback implements IPlayerListener {
        private PlayerInternalCallback() {
        }

        @Override // ohos.media.player.Player.IPlayerListener
        public void onPlayBackComplete() {
            Player.LOGGER.info("onPlayBackComplete looping is %{public}b", Boolean.valueOf(Player.this.looping));
            if (!Player.this.useDistributed || !Player.this.looping) {
                Player.this.keepAwake(false);
            } else {
                Player.this.rewindToInDistributed(0, -1);
            }
        }

        @Override // ohos.media.player.Player.IPlayerListener
        public void onError(int i, int i2) {
            Player.LOGGER.error("onError errorType is %{public}d, errorCode is %{public}d", Integer.valueOf(i), Integer.valueOf(i2));
            Player.this.keepAwake(false);
        }
    }

    private boolean registerInternalCallback(IPlayerListener iPlayerListener) {
        LOGGER.debug("registerInternalCallback begin", new Object[0]);
        int nativeRegisterInternalCallback = nativeRegisterInternalCallback(iPlayerListener);
        if (nativeRegisterInternalCallback == 0) {
            return true;
        }
        LOGGER.error("register internal callback failed, error code is %{public}d", Integer.valueOf(nativeRegisterInternalCallback));
        return false;
    }

    private boolean writeBuffer(int i, byte[] bArr, BufferInfo bufferInfo) {
        int nativeWriteBuffer = nativeWriteBuffer(i, bArr, bufferInfo);
        if (nativeWriteBuffer == 0) {
            return true;
        }
        LOGGER.error("writeBuffer failed, error code is %{public}d", Integer.valueOf(nativeWriteBuffer));
        return false;
    }

    private boolean prepareTrack(int i, Format format) {
        if (format == null) {
            LOGGER.error("prepareTrack failed, format is null.", new Object[0]);
            return false;
        }
        Format.FormatArrays formatArrays = format.getFormatArrays();
        int nativePrepareTrack = nativePrepareTrack(i, formatArrays.keys, formatArrays.values);
        if (nativePrepareTrack == 0) {
            return true;
        }
        LOGGER.error("prepareTrack failed, error code is %{public}d", Integer.valueOf(nativePrepareTrack));
        return false;
    }

    private boolean doExtractor() {
        int totalStreams = this.extractor.getTotalStreams();
        if (totalStreams <= 0) {
            LOGGER.error("doExtractor failed, total track num is %{public}d", Integer.valueOf(totalStreams));
            return false;
        }
        this.sawEos = false;
        this.writeThreadCount.set(0);
        for (int i = 0; i < totalStreams; i++) {
            if (!prepareTrack(i, this.extractor.getStreamFormat(i))) {
                LOGGER.error("prepareTrack failed, trackid is %{public}d", Integer.valueOf(i));
                this.sawEos = true;
                return false;
            }
            if (this.eventHandlerList == null) {
                this.eventHandlerList = new ArrayList();
            }
            EventRunner create = EventRunner.create("WriteBufferTrack" + i);
            if (create != null) {
                PlayerEventHandler playerEventHandler = new PlayerEventHandler(create, i);
                playerEventHandler.rewindTo(0, -1);
                playerEventHandler.sendEvent(InnerEvent.get(0));
                this.eventHandlerList.add(playerEventHandler);
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean rewindToInDistributed(long j, int i) {
        LOGGER.info("rewindToInDistributed begin, microseconds is %{public}d, mode is %{public}d", Long.valueOf(j), Integer.valueOf(i));
        if (this.eventHandlerList == null) {
            LOGGER.error("rewindToInDistributed failed, eventHandlerList is null", new Object[0]);
            return false;
        }
        this.sawEos = true;
        int nativeRewindToDistributed = nativeRewindToDistributed(true);
        if (nativeRewindToDistributed != 0) {
            LOGGER.error("rewindToInDistributed native rewind(true) failed, error code is %{public}d", Integer.valueOf(nativeRewindToDistributed));
            return false;
        }
        int i2 = 0;
        while (this.writeThreadCount.get() > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException unused) {
                LOGGER.error("rewindToInDistributed Interrupted in thread sleep", new Object[0]);
            }
            int i3 = i2 + 1;
            if (i2 >= 100) {
                LOGGER.error("rewindToInDistributed failed, last write thread still exist", new Object[0]);
                return false;
            }
            i2 = i3;
        }
        int nativeRewindToDistributed2 = nativeRewindToDistributed(false);
        if (nativeRewindToDistributed2 != 0) {
            LOGGER.error("rewindToInDistributed native rewind(false) failed, error code is %{public}d", Integer.valueOf(nativeRewindToDistributed2));
            return false;
        }
        this.writeThreadCount.set(0);
        this.sawEos = false;
        for (PlayerEventHandler playerEventHandler : this.eventHandlerList) {
            playerEventHandler.removeAllEvent();
            InnerEvent innerEvent = InnerEvent.get(0);
            playerEventHandler.rewindTo(j, i);
            playerEventHandler.sendEvent(innerEvent);
        }
        LOGGER.info("rewindToInDistributed end", new Object[0]);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void extractAndWriteBuffer(int i, long j, int i2) {
        Extractor extractor2 = new Extractor();
        extractor2.setSource(this.source);
        extractor2.specifyStream(i);
        if (i2 < 0 || i2 > 3 || extractor2.rewindTo(j, i2)) {
            boolean z = false;
            do {
                ByteBuffer allocate = ByteBuffer.allocate((int) extractor2.getFrameSize());
                int readBuffer = extractor2.readBuffer(allocate, 0);
                if (readBuffer < 0) {
                    LOGGER.error("Read stream failed, byteRead is %{public}d", Integer.valueOf(readBuffer));
                    z = true;
                } else {
                    byte[] bArr = new byte[readBuffer];
                    allocate.get(bArr, 0, readBuffer);
                    BufferInfo bufferInfo = new BufferInfo();
                    bufferInfo.offset = 0;
                    bufferInfo.size = readBuffer;
                    bufferInfo.timeStamp = extractor2.getFrameTimestamp();
                    bufferInfo.bufferType = extractor2.getFrameType();
                    if (!extractor2.next()) {
                        bufferInfo.bufferType = 4;
                        z = true;
                    }
                    writeBuffer(i, bArr, bufferInfo);
                }
                if (z) {
                    return;
                }
            } while (!this.sawEos);
            return;
        }
        LOGGER.error("extractAndWriteBuffer rewindTo failed", new Object[0]);
        this.sawEos = true;
    }

    /* access modifiers changed from: package-private */
    public class DriverModeEventSubscriber extends CommonEventSubscriber {
        DriverModeEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            if (commonEventData == null || commonEventData.getIntent() == null) {
                Player.LOGGER.error("onReceiveEvent commonEventData is null", new Object[0]);
                return;
            }
            if (Player.this.formatMap == null || Player.this.formatMap.isEmpty()) {
                Player player = Player.this;
                player.createTrackFormatMap(player.source);
                Player player2 = Player.this;
                player2.findVideoType(player2.formatMap);
            }
            String action = commonEventData.getIntent().getAction();
            char c = 65535;
            if (action.hashCode() == -1519062833 && action.equals("common.event.DRIVE_MODE")) {
                c = 0;
            }
            if (c != 0) {
                Player.LOGGER.error("Unrecognized message type %{public}s", commonEventData.getIntent().getAction());
            } else if (commonEventData.getIntent().getBooleanParam(Player.COMMON_EVENT_DRIVE_MODE_ACTION_KEY, false)) {
                if (!DrivingSafetyClient.isDrivingSafety(Player.this.appContext, ControlItemEnum.VIDEO) && Player.this.sourceType == PlaySourceType.HAVE_VIDEO && Player.this.getPlayerState() == 16) {
                    Player.this.isDriverMode = true;
                    Player.this.pause();
                }
            } else if (Player.this.sourceType == PlaySourceType.HAVE_VIDEO && Player.this.getPlayerState() == 32 && Player.this.isDriverMode) {
                Player.this.isDriverMode = false;
                Player.this.play();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerDriverModeEvent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("common.event.DRIVE_MODE");
        try {
            CommonEventManager.subscribeCommonEvent(new DriverModeEventSubscriber(new CommonEventSubscribeInfo(intentFilter)));
        } catch (RemoteException e) {
            LOGGER.debug("subscribeCommonEvent occur exception %{public}s", e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createTrackFormatMap(Source source2) {
        if (source2 == null) {
            LOGGER.error("source must not be null", new Object[0]);
            return;
        }
        this.formatMap = new ConcurrentHashMap();
        this.extractor = new Extractor();
        this.extractor.setSource(source2);
        int totalStreams = this.extractor.getTotalStreams();
        for (int i = 0; i < totalStreams; i++) {
            this.extractor.specifyStream(i);
            this.formatMap.put(Integer.valueOf(i), this.extractor.getStreamFormat(i));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void findVideoType(Map<Integer, Format> map) {
        if (map != null) {
            for (Map.Entry<Integer, Format> entry : map.entrySet()) {
                if (entry.getValue().getStringValue(Format.MIME).contains("video")) {
                    this.sourceType = PlaySourceType.HAVE_VIDEO;
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
