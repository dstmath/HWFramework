package ohos.media.sessioncore.adapter;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.Rating;
import android.media.VolumeProvider;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.KeyEvent;
import java.util.List;
import java.util.Optional;
import ohos.app.GeneralReceiver;
import ohos.event.intentagent.IntentAgent;
import ohos.media.common.AVMetadata;
import ohos.media.common.AudioStreamProperty;
import ohos.media.common.adapter.AVAudioStreamPropertyAdapter;
import ohos.media.common.adapter.AVCallerUserInfoAdapter;
import ohos.media.common.adapter.AVMetadataAdapter;
import ohos.media.common.adapter.AVPlaybackStateAdapter;
import ohos.media.common.adapter.AVRatingAdapter;
import ohos.media.common.sessioncore.AVCallerUserInfo;
import ohos.media.common.sessioncore.AVPlaybackState;
import ohos.media.common.sessioncore.AVQueueElement;
import ohos.media.common.sessioncore.AVSessionCallback;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.common.utils.AVUtils;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;
import ohos.net.UriConverter;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;

public class AVSessionAdapter {
    public static final String ACTION_MEDIA_BUTTON = "harmony.intent.action.MEDIA_BUTTON";
    private static final int DEFAULT_FLAGS = 0;
    private static final int DEFAULT_REQUEST_CODE = 0;
    private static final int DEFAULT_RESOLVE_INFO_FLAGS = 0;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVSessionAdapter.class);
    public static final String PARAM_KEY_EVENT = "harmony.intent.param.KEY_EVENT";
    private final Object cbLock;
    private final MediaSession session;
    private final MediaSessionCallback sessionCallback;

    /* access modifiers changed from: private */
    public static GeneralReceiver convert2GeneralReceiver(final ResultReceiver resultReceiver) {
        if (resultReceiver != null) {
            return new GeneralReceiver() {
                /* class ohos.media.sessioncore.adapter.AVSessionAdapter.AnonymousClass1 */

                /* access modifiers changed from: protected */
                public void onReceive(int i, PacMap pacMap) {
                    if (pacMap == null) {
                        AVSessionAdapter.LOGGER.warn("convert2GeneralReceiver onReceive invalid data", new Object[0]);
                    } else {
                        resultReceiver.send(i, AVUtils.convert2Bundle(pacMap));
                    }
                }
            };
        }
        LOGGER.error("convert2GeneralReceiver failed, receiver is null.", new Object[0]);
        return null;
    }

    private boolean setDefaultMediaButtonReceiver(Context context) {
        if (context == null) {
            LOGGER.error("setDefaultMediaButtonReceiver failed, context is null.", new Object[0]);
            return false;
        }
        Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
        intent.setPackage(context.getPackageName());
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            LOGGER.error("get package manager failed", new Object[0]);
            return false;
        }
        List<ResolveInfo> queryBroadcastReceivers = packageManager.queryBroadcastReceivers(intent, 0);
        if (queryBroadcastReceivers == null) {
            LOGGER.error("package manager query broadcast receivers failed", new Object[0]);
            return false;
        } else if (queryBroadcastReceivers.size() > 1) {
            LOGGER.error("more than one BroadcastReceiver that handles ACTION_MEDIA_BUTTON intent", new Object[0]);
            return false;
        } else {
            ComponentName componentName = null;
            if (queryBroadcastReceivers.size() == 1) {
                ResolveInfo resolveInfo = queryBroadcastReceivers.get(0);
                componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            }
            if (componentName == null) {
                componentName = new ComponentName(context.getPackageName(), "ohos.media.sessioncore.adapter.AVMediaButtonReceiverAdapter");
            }
            Intent intent2 = new Intent("android.intent.action.MEDIA_BUTTON");
            intent2.setComponent(componentName);
            this.session.setMediaButtonReceiver(PendingIntent.getBroadcast(context, 0, intent2, 0));
            return true;
        }
    }

    public AVSessionAdapter(ohos.app.Context context, String str) {
        this(context, str, null);
    }

    public AVSessionAdapter(ohos.app.Context context, String str, PacMap pacMap) {
        this.cbLock = new Object();
        if (context == null || !(context.getHostContext() instanceof Context)) {
            throw new IllegalArgumentException("Invalid context");
        }
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        Context context2 = (Context) context.getHostContext();
        this.session = new MediaSession(context2, str, PacMapUtils.convertIntoBundle(pacMap));
        this.sessionCallback = new MediaSessionCallback();
        this.session.setCallback(this.sessionCallback);
        if (!setDefaultMediaButtonReceiver(context2)) {
            LOGGER.warn("setDefaultMediaButtonReceiver failed", new Object[0]);
        }
    }

    public void setAVSessionCallback(AVSessionCallback aVSessionCallback) {
        synchronized (this.cbLock) {
            this.sessionCallback.setHostCallback(aVSessionCallback);
        }
    }

    private class MediaSessionCallback extends MediaSession.Callback {
        private AVSessionCallback avCallback;

        private MediaSessionCallback() {
        }

        public void setHostCallback(AVSessionCallback aVSessionCallback) {
            this.avCallback = aVSessionCallback;
        }

        @Override // android.media.session.MediaSession.Callback
        public void onCommand(String str, Bundle bundle, ResultReceiver resultReceiver) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onCommand(str, AVUtils.convert2PacMap(bundle), AVSessionAdapter.convert2GeneralReceiver(resultReceiver));
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public boolean onMediaButtonEvent(Intent intent) {
            if (this.avCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
                return false;
            } else if (intent == null || intent.getAction() == null) {
                AVSessionAdapter.LOGGER.error("invalid mediaButtonIntent", new Object[0]);
                return false;
            } else if (!"android.intent.action.MEDIA_BUTTON".equals(intent.getAction())) {
                AVSessionAdapter.LOGGER.error("unsupported action %{public}s", intent.getAction());
                return false;
            } else {
                ohos.aafwk.content.Intent intent2 = new ohos.aafwk.content.Intent();
                intent2.setAction("harmony.intent.action.MEDIA_BUTTON");
                KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
                if (keyEvent == null) {
                    AVSessionAdapter.LOGGER.error("getParcelableExtra KeyEvent failed", new Object[0]);
                    return false;
                }
                Optional<MultimodalEvent> createEvent = MultimodalEventFactory.createEvent(keyEvent);
                if (!createEvent.isPresent()) {
                    AVSessionAdapter.LOGGER.error("MultimodalEventFactory.createEvent failed", new Object[0]);
                    return false;
                }
                MultimodalEvent multimodalEvent = createEvent.get();
                if (!(multimodalEvent instanceof ohos.multimodalinput.event.KeyEvent)) {
                    AVSessionAdapter.LOGGER.error("multimodalEvent is not instanceof KeyEvent", new Object[0]);
                    return false;
                }
                ohos.multimodalinput.event.KeyEvent keyEvent2 = (ohos.multimodalinput.event.KeyEvent) multimodalEvent;
                AVSessionAdapter.LOGGER.debug("keyEvent code = %{public}d, isKeyDown = %{public}s", Integer.valueOf(keyEvent2.getKeyCode()), Boolean.valueOf(keyEvent2.isKeyDown()));
                intent2.setParam("harmony.intent.param.KEY_EVENT", keyEvent2);
                boolean onMediaButtonEvent = this.avCallback.onMediaButtonEvent(intent2);
                if (!this.avCallback.isOnMediaButtonEventOverridden()) {
                    return super.onMediaButtonEvent(intent);
                }
                return onMediaButtonEvent;
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPlay() {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPlay();
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPause() {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPause();
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onStop() {
            AVSessionAdapter.LOGGER.debug("called onStop", new Object[0]);
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onStop();
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPrepare() {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPrepareToPlay();
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSeekTo(long j) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onSeekTo(j);
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPrepareFromMediaId(String str, Bundle bundle) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPrepareToPlayByMediaId(str, AVUtils.convert2PacMap(bundle));
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPrepareFromSearch(String str, Bundle bundle) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPrepareToPlayBySearch(str, AVUtils.convert2PacMap(bundle));
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPrepareFromUri(Uri uri, Bundle bundle) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPrepareToPlayByUri(UriConverter.convertToZidaneUri(uri), AVUtils.convert2PacMap(bundle));
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onFastForward() {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPlayFastForward();
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onRewind() {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onRewind();
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSkipToNext() {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPlayNext();
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSkipToPrevious() {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPlayPrevious();
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSkipToQueueItem(long j) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onSkipToAVQueueElement(j);
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPlayFromSearch(String str, Bundle bundle) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPlayBySearch(str, AVUtils.convert2PacMap(bundle));
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPlayFromMediaId(String str, Bundle bundle) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPlayByMediaId(str, AVUtils.convert2PacMap(bundle));
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPlayFromUri(Uri uri, Bundle bundle) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onPlayByUri(UriConverter.convertToZidaneUri(uri), AVUtils.convert2PacMap(bundle));
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSetRating(Rating rating) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onSetAVRatingStyle(AVRatingAdapter.getAVRating(rating));
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSetPlaybackSpeed(float f) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onSetAVPlaybackSpeed(f);
            }
        }

        @Override // android.media.session.MediaSession.Callback
        public void onCustomAction(String str, Bundle bundle) {
            AVSessionCallback aVSessionCallback = this.avCallback;
            if (aVSessionCallback == null) {
                AVSessionAdapter.LOGGER.error("AVSessionCallback is null", new Object[0]);
            } else {
                aVSessionCallback.onSetAVPlaybackCustomAction(str, AVUtils.convert2PacMap(bundle));
            }
        }
    }

    public boolean setAVSessionAbility(IntentAgent intentAgent) {
        if (intentAgent == null) {
            LOGGER.error("Invalid IntentAgent", new Object[0]);
            return false;
        } else if (!(intentAgent.getObject() instanceof PendingIntent)) {
            LOGGER.error("Invalid PendingIntent", new Object[0]);
            return false;
        } else {
            this.session.setSessionActivity((PendingIntent) intentAgent.getObject());
            return true;
        }
    }

    public boolean setAVButtonReceiver(IntentAgent intentAgent) {
        if (intentAgent == null) {
            LOGGER.error("Invalid IntentAgent", new Object[0]);
            return false;
        } else if (!(intentAgent.getObject() instanceof PendingIntent)) {
            LOGGER.error("Invalid PendingIntent", new Object[0]);
            return false;
        } else {
            this.session.setMediaButtonReceiver((PendingIntent) intentAgent.getObject());
            return true;
        }
    }

    public void setFlags(int i) {
        this.session.setFlags(i);
    }

    public void setAVPlaybackVolumeToSystem(AudioStreamProperty audioStreamProperty) {
        if (audioStreamProperty == null) {
            LOGGER.error("properties is null", new Object[0]);
            return;
        }
        this.session.setPlaybackToLocal(AVAudioStreamPropertyAdapter.getAudioAttributes(audioStreamProperty));
    }

    public void setCustomAVPlaybackVolume(Object obj) {
        if (obj instanceof VolumeProvider) {
            this.session.setPlaybackToRemote((VolumeProvider) obj);
            return;
        }
        throw new IllegalArgumentException("Invalid VolControl");
    }

    public void enableAVSessionActive(boolean z) {
        this.session.setActive(z);
    }

    public boolean isAVSessionActive() {
        return this.session.isActive();
    }

    public void sendAVSessionEvent(String str, PacMap pacMap) {
        if (str == null) {
            LOGGER.error("event is null", new Object[0]);
        } else {
            this.session.sendSessionEvent(str, PacMapUtils.convertIntoBundle(pacMap));
        }
    }

    public void release() {
        this.session.release();
    }

    public AVToken getAVToken() {
        return new AVToken(this.session.getSessionToken());
    }

    public AVControllerAdapter getAVController() {
        return new AVControllerAdapter(this.session.getController());
    }

    public void setAVPlaybackState(AVPlaybackState aVPlaybackState) {
        if (aVPlaybackState == null) {
            LOGGER.error("AVPlaybackState is null", new Object[0]);
        } else {
            this.session.setPlaybackState(AVPlaybackStateAdapter.getPlaybackState(aVPlaybackState));
        }
    }

    public void setAVMetadata(AVMetadata aVMetadata) {
        this.session.setMetadata(AVMetadataAdapter.getMediaMetadata(aVMetadata));
    }

    public void setAVQueue(List<AVQueueElement> list) {
        this.session.setQueue(AVQueueElementAdapter.getListQueueItem(list));
    }

    public void setAVQueueTitle(CharSequence charSequence) {
        this.session.setQueueTitle(charSequence);
    }

    public void setAVRatingStyle(int i) {
        this.session.setRatingType(i);
    }

    public void setOptions(PacMap pacMap) {
        this.session.setExtras(PacMapUtils.convertIntoBundle(pacMap));
    }

    public AVCallerUserInfo getCurrentControllerInfo() {
        return AVCallerUserInfoAdapter.getAVCallerUserInfo(this.session.getCurrentControllerInfo());
    }

    public void notifyVolumeControlChanged(Object obj) {
        if (obj instanceof VolumeProvider) {
            this.session.notifyRemoteVolumeChanged((VolumeProvider) obj);
            return;
        }
        throw new IllegalArgumentException("Invalid volControl");
    }

    public String getCallerPackageName() {
        return this.session.getCallingPackage();
    }

    public static boolean isAVSessionActive(int i) {
        return MediaSession.isActiveState(i);
    }
}
