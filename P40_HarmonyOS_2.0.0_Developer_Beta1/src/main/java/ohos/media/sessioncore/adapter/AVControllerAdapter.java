package ohos.media.sessioncore.adapter;

import android.app.PendingIntent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ohos.app.Context;
import ohos.app.GeneralReceiver;
import ohos.event.intentagent.IntentAgent;
import ohos.global.icu.text.Bidi;
import ohos.media.common.AVMetadata;
import ohos.media.common.adapter.AVMetadataAdapter;
import ohos.media.common.adapter.AVPlaybackInfoAdapter;
import ohos.media.common.adapter.AVPlaybackStateAdapter;
import ohos.media.common.adapter.AVRatingAdapter;
import ohos.media.common.sessioncore.AVControllerCallback;
import ohos.media.common.sessioncore.AVPlaybackInfo;
import ohos.media.common.sessioncore.AVPlaybackState;
import ohos.media.common.sessioncore.AVQueueElement;
import ohos.media.common.sessioncore.AVRating;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.common.utils.AVUtils;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;
import ohos.net.UriConverter;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;
import ohos.utils.net.Uri;

public class AVControllerAdapter {
    private static final SparseIntArray HOST_KEY_CODE_MAPPING = new SparseIntArray();
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVControllerAdapter.class);
    private static final Map<Integer, Integer> RATING_TYPE_MAP = new HashMap();
    private final Object avLock = new Object();
    private final ArrayList<CallbackMapData> callbackList = new ArrayList<>();
    private final MediaController mediaController;
    private final MediaSession.Token token;
    private final MediaController.TransportControls transportControls;

    static {
        RATING_TYPE_MAP.put(0, 0);
        RATING_TYPE_MAP.put(1, 1);
        RATING_TYPE_MAP.put(2, 2);
        RATING_TYPE_MAP.put(3, 3);
        RATING_TYPE_MAP.put(4, 4);
        RATING_TYPE_MAP.put(5, 5);
        RATING_TYPE_MAP.put(6, 6);
        HOST_KEY_CODE_MAPPING.put(10, 85);
        HOST_KEY_CODE_MAPPING.put(11, 86);
        HOST_KEY_CODE_MAPPING.put(12, 87);
        HOST_KEY_CODE_MAPPING.put(13, 88);
        HOST_KEY_CODE_MAPPING.put(14, 89);
        HOST_KEY_CODE_MAPPING.put(15, 90);
        HOST_KEY_CODE_MAPPING.put(KeyEvent.KEY_MEDIA_PLAY, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        HOST_KEY_CODE_MAPPING.put(KeyEvent.KEY_MEDIA_PAUSE, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
        HOST_KEY_CODE_MAPPING.put(KeyEvent.KEY_MEDIA_RECORD, 130);
    }

    /* access modifiers changed from: private */
    public final class CallbackMapData {
        private AVControllerCallback avControllerCallback;
        private MediaController.Callback mediaControllerCallback;

        public CallbackMapData(AVControllerCallback aVControllerCallback, MediaController.Callback callback) {
            this.avControllerCallback = aVControllerCallback;
            this.mediaControllerCallback = callback;
        }

        public AVControllerCallback getAVControllerCallback() {
            return this.avControllerCallback;
        }

        public MediaController.Callback getMediaControllerCallback() {
            return this.mediaControllerCallback;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                AVControllerAdapter.LOGGER.debug("compare with different object", new Object[0]);
                return false;
            } else if (!(obj instanceof CallbackMapData)) {
                AVControllerAdapter.LOGGER.debug("that object is not instance of CallbackMapData", new Object[0]);
                return false;
            } else {
                CallbackMapData callbackMapData = (CallbackMapData) obj;
                return this.avControllerCallback == callbackMapData.avControllerCallback && this.mediaControllerCallback == callbackMapData.mediaControllerCallback;
            }
        }

        public int hashCode() {
            return Objects.hash(this.avControllerCallback, this.mediaControllerCallback);
        }
    }

    private static int getHostKeyEvent(int i) {
        return HOST_KEY_CODE_MAPPING.get(i, 0);
    }

    public AVControllerAdapter(Context context, AVToken aVToken) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        } else if (aVToken == null) {
            throw new IllegalArgumentException("avToken is null");
        } else if (!(context.getHostContext() instanceof android.content.Context)) {
            throw new IllegalArgumentException("Invalid context");
        } else if (aVToken.getHostAVToken() instanceof MediaSession.Token) {
            this.token = (MediaSession.Token) aVToken.getHostAVToken();
            this.mediaController = new MediaController((android.content.Context) context.getHostContext(), this.token);
            this.transportControls = this.mediaController.getTransportControls();
        } else {
            throw new IllegalArgumentException("Invalid avToken");
        }
    }

    public AVControllerAdapter(MediaController mediaController2) {
        if (mediaController2 != null) {
            this.mediaController = mediaController2;
            this.token = this.mediaController.getSessionToken();
            this.transportControls = this.mediaController.getTransportControls();
            return;
        }
        throw new IllegalArgumentException("controller is null");
    }

    public Object getHostController() {
        return this.mediaController;
    }

    private boolean isHaveRegisterCallback(AVControllerCallback aVControllerCallback) {
        for (int size = this.callbackList.size() - 1; size >= 0; size--) {
            if (this.callbackList.get(size).getAVControllerCallback() == aVControllerCallback) {
                return true;
            }
        }
        return false;
    }

    public boolean setAVControllerCallback(final AVControllerCallback aVControllerCallback) {
        AnonymousClass1 r0 = new MediaController.Callback() {
            /* class ohos.media.sessioncore.adapter.AVControllerAdapter.AnonymousClass1 */

            @Override // android.media.session.MediaController.Callback
            public void onSessionDestroyed() {
                aVControllerCallback.onAVSessionDestroyed();
            }

            @Override // android.media.session.MediaController.Callback
            public void onSessionEvent(String str, Bundle bundle) {
                aVControllerCallback.onAVSessionEvent(str, AVUtils.convert2PacMap(bundle));
            }

            @Override // android.media.session.MediaController.Callback
            public void onPlaybackStateChanged(PlaybackState playbackState) {
                aVControllerCallback.onAVPlaybackStateChanged(AVPlaybackStateAdapter.getAVPlaybackState(playbackState));
            }

            @Override // android.media.session.MediaController.Callback
            public void onMetadataChanged(MediaMetadata mediaMetadata) {
                aVControllerCallback.onAVMetadataChanged(AVMetadataAdapter.getAVMetadata(mediaMetadata));
            }

            @Override // android.media.session.MediaController.Callback
            public void onQueueChanged(List<MediaSession.QueueItem> list) {
                ArrayList arrayList = new ArrayList();
                if (list == null) {
                    AVControllerAdapter.LOGGER.error("Failed to getQueue", new Object[0]);
                    return;
                }
                for (MediaSession.QueueItem queueItem : list) {
                    AVQueueElement aVQueueElement = AVQueueElementAdapter.getAVQueueElement(queueItem);
                    if (aVQueueElement != null) {
                        arrayList.add(aVQueueElement);
                    }
                }
                aVControllerCallback.onAVQueueChanged(arrayList);
            }

            @Override // android.media.session.MediaController.Callback
            public void onQueueTitleChanged(CharSequence charSequence) {
                aVControllerCallback.onAVQueueTitleChanged(charSequence);
            }

            @Override // android.media.session.MediaController.Callback
            public void onExtrasChanged(Bundle bundle) {
                aVControllerCallback.onOptionsChanged(AVUtils.convert2PacMap(bundle));
            }

            @Override // android.media.session.MediaController.Callback
            public void onAudioInfoChanged(MediaController.PlaybackInfo playbackInfo) {
                Optional<AVPlaybackInfo> aVPlaybackInfo = AVPlaybackInfoAdapter.getAVPlaybackInfo(playbackInfo);
                if (aVPlaybackInfo.isPresent()) {
                    aVControllerCallback.onAVPlaybackInfoChanged(aVPlaybackInfo.get());
                }
            }
        };
        synchronized (this.avLock) {
            if (isHaveRegisterCallback(aVControllerCallback)) {
                LOGGER.warn("AVControllerCallback is already set", new Object[0]);
                return true;
            }
            this.callbackList.add(new CallbackMapData(aVControllerCallback, r0));
            this.mediaController.registerCallback(r0);
            return true;
        }
    }

    public boolean releaseAVControllerCallback(AVControllerCallback aVControllerCallback) {
        boolean z;
        synchronized (this.avLock) {
            z = false;
            for (int size = this.callbackList.size() - 1; size >= 0; size--) {
                CallbackMapData callbackMapData = this.callbackList.get(size);
                if (callbackMapData.getAVControllerCallback() == aVControllerCallback) {
                    this.callbackList.remove(callbackMapData);
                    this.mediaController.unregisterCallback(callbackMapData.getMediaControllerCallback());
                    z = true;
                }
            }
        }
        return z;
    }

    public List<AVQueueElement> getAVQueueElement() {
        List<MediaSession.QueueItem> queue = this.mediaController.getQueue();
        ArrayList arrayList = new ArrayList();
        if (queue == null) {
            LOGGER.error("Failed to getQueue", new Object[0]);
            return arrayList;
        }
        for (MediaSession.QueueItem queueItem : queue) {
            AVQueueElement aVQueueElement = AVQueueElementAdapter.getAVQueueElement(queueItem);
            if (aVQueueElement != null) {
                arrayList.add(aVQueueElement);
            }
        }
        return arrayList;
    }

    public CharSequence getAVQueueTitle() {
        return this.mediaController.getQueueTitle();
    }

    public int getAVRatingStyle() {
        return RATING_TYPE_MAP.getOrDefault(Integer.valueOf(this.mediaController.getRatingType()), 0).intValue();
    }

    public Optional<AVPlaybackState> getAVPlaybackState() {
        PlaybackState playbackState = this.mediaController.getPlaybackState();
        if (playbackState != null) {
            return Optional.of(AVPlaybackStateAdapter.getAVPlaybackState(playbackState));
        }
        LOGGER.error("Failed to getPlaybackState", new Object[0]);
        return Optional.empty();
    }

    public boolean dispatchAVKeyEvent(KeyEvent keyEvent) {
        Optional<android.view.KeyEvent> hostKeyEvent = MultimodalEventFactory.getHostKeyEvent(keyEvent);
        if (hostKeyEvent.isPresent()) {
            return this.mediaController.dispatchMediaButtonEvent(hostKeyEvent.get());
        }
        LOGGER.error("Failed to get host KeyEvent", new Object[0]);
        return false;
    }

    public void sendCustomCommand(String str, PacMap pacMap, final GeneralReceiver generalReceiver) {
        Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(pacMap);
        AnonymousClass2 r0 = null;
        if (generalReceiver != null) {
            r0 = new ResultReceiver(null) {
                /* class ohos.media.sessioncore.adapter.AVControllerAdapter.AnonymousClass2 */

                /* access modifiers changed from: protected */
                @Override // android.os.ResultReceiver
                public void onReceiveResult(int i, Bundle bundle) {
                    generalReceiver.sendResult(i, PacMapUtils.convertFromBundle(bundle));
                }
            };
        }
        this.mediaController.sendCommand(str, convertIntoBundle, r0);
    }

    public Optional<IntentAgent> getAVSessionAbility() {
        PendingIntent sessionActivity = this.mediaController.getSessionActivity();
        if (sessionActivity != null) {
            return Optional.of(new IntentAgent(sessionActivity));
        }
        LOGGER.error("Failed to getSessionIntentAgent", new Object[0]);
        return Optional.empty();
    }

    public AVToken getAVToken() {
        return new AVToken(this.token);
    }

    public void adjustAVPlaybackVolume(int i, int i2) {
        this.mediaController.adjustVolume(i, i2);
    }

    public void setAVPlaybackVolume(int i, int i2) {
        this.mediaController.setVolumeTo(i, i2);
    }

    public PacMap getOptions() {
        return PacMapUtils.convertFromBundle(this.mediaController.getExtras());
    }

    public long getFlags() {
        return this.mediaController.getFlags();
    }

    public Optional<AVMetadata> getAVMetadata() {
        MediaMetadata metadata = this.mediaController.getMetadata();
        if (metadata == null) {
            LOGGER.error("Failed to getMetadata!", new Object[0]);
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(AVMetadataAdapter.getAVMetadata(metadata));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to getAVMetadata:%{public}s", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<AVPlaybackInfo> getAVPlaybackInfo() {
        MediaController.PlaybackInfo playbackInfo = this.mediaController.getPlaybackInfo();
        if (playbackInfo != null) {
            return AVPlaybackInfoAdapter.getAVPlaybackInfo(playbackInfo);
        }
        LOGGER.error("Failed to getPlaybackInfo", new Object[0]);
        return Optional.empty();
    }

    public String getSessionOwnerPackageName() {
        return this.mediaController.getPackageName();
    }

    public PacMap getAVSessionInfo() {
        return PacMapUtils.convertFromBundle(this.mediaController.getSessionInfo());
    }

    public void prepareToPlay() {
        this.transportControls.prepare();
    }

    public void prepareToPlayByMediaId(String str, PacMap pacMap) {
        this.transportControls.prepareFromMediaId(str, PacMapUtils.convertIntoBundle(pacMap));
    }

    public void prepareToPlayBySearch(String str, PacMap pacMap) {
        this.transportControls.prepareFromSearch(str, PacMapUtils.convertIntoBundle(pacMap));
    }

    public void prepareToPlayByUri(Uri uri, PacMap pacMap) {
        this.transportControls.prepareFromUri(UriConverter.convertToAndroidUri(uri), PacMapUtils.convertIntoBundle(pacMap));
    }

    public void play() {
        this.transportControls.play();
    }

    public void playByMediaId(String str, PacMap pacMap) {
        this.transportControls.playFromMediaId(str, PacMapUtils.convertIntoBundle(pacMap));
    }

    public void playBySearch(String str, PacMap pacMap) {
        this.transportControls.playFromSearch(str, PacMapUtils.convertIntoBundle(pacMap));
    }

    public void playByUri(Uri uri, PacMap pacMap) {
        this.transportControls.playFromUri(UriConverter.convertToAndroidUri(uri), PacMapUtils.convertIntoBundle(pacMap));
    }

    public void skipToAVQueueItem(long j) {
        this.transportControls.skipToQueueItem(j);
    }

    public void pause() {
        this.transportControls.pause();
    }

    public void stop() {
        this.transportControls.stop();
    }

    public void seekTo(long j) {
        this.transportControls.seekTo(j);
    }

    public void playFastForward() {
        this.transportControls.fastForward();
    }

    public void playNext() {
        this.transportControls.skipToNext();
    }

    public void rewind() {
        this.transportControls.rewind();
    }

    public void playPrevious() {
        this.transportControls.skipToPrevious();
    }

    public void setAVRatingStyle(AVRating aVRating) {
        this.transportControls.setRating(AVRatingAdapter.getRating(aVRating));
    }

    public void setAVPlaybackSpeed(float f) {
        this.transportControls.setPlaybackSpeed(f);
    }

    public void sendAVPlaybackCustomAction(AVPlaybackState.AVPlaybackCustomAction aVPlaybackCustomAction, PacMap pacMap) {
        this.transportControls.sendCustomAction(AVPlaybackStateAdapter.getCustomAction(aVPlaybackCustomAction), PacMapUtils.convertIntoBundle(pacMap));
    }

    public void sendAVPlaybackCustomAction(String str, PacMap pacMap) {
        this.transportControls.sendCustomAction(str, PacMapUtils.convertIntoBundle(pacMap));
    }
}
