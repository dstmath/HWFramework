package ohos.media.sessioncore;

import java.util.List;
import ohos.app.Context;
import ohos.event.intentagent.IntentAgent;
import ohos.media.common.AVMetadata;
import ohos.media.common.sessioncore.AVCallerUserInfo;
import ohos.media.common.sessioncore.AVPlaybackState;
import ohos.media.common.sessioncore.AVQueueElement;
import ohos.media.common.sessioncore.AVSessionCallback;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.sessioncore.adapter.AVSessionAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;

public final class AVSession {
    public static final String ACTION_MEDIA_BUTTON = "harmony.intent.action.MEDIA_BUTTON";
    public static final int FLAG_EXCLUSIVE_GLOBAL_PRIORITY = 65536;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVSession.class);
    public static final String PARAM_KEY_EVENT = "harmony.intent.param.KEY_EVENT";
    private final AVController cachedController;
    private final AVToken cachedToken;
    private final AVSessionAdapter sessionAdapter;

    public AVSession(Context context, String str) {
        this(context, str, null);
    }

    public AVSession(Context context, String str, PacMap pacMap) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        } else if (str != null) {
            this.sessionAdapter = new AVSessionAdapter(context, str, pacMap);
            this.cachedToken = this.sessionAdapter.getAVToken();
            this.cachedController = new AVController(this.sessionAdapter.getAVController());
        } else {
            throw new IllegalArgumentException("tag is null");
        }
    }

    public void setAVSessionCallback(AVSessionCallback aVSessionCallback) {
        this.sessionAdapter.setAVSessionCallback(aVSessionCallback);
    }

    public boolean setAVSessionAbility(IntentAgent intentAgent) {
        return this.sessionAdapter.setAVSessionAbility(intentAgent);
    }

    public boolean setAVButtonReceiver(IntentAgent intentAgent) {
        return this.sessionAdapter.setAVButtonReceiver(intentAgent);
    }

    public void setFlags(int i) {
        this.sessionAdapter.setFlags(i);
    }

    public void enableAVSessionActive(boolean z) {
        this.sessionAdapter.enableAVSessionActive(z);
    }

    public boolean isAVSessionActive() {
        return this.sessionAdapter.isAVSessionActive();
    }

    public void sendAVSessionEvent(String str, PacMap pacMap) {
        this.sessionAdapter.sendAVSessionEvent(str, pacMap);
    }

    public void release() {
        this.sessionAdapter.release();
    }

    public AVToken getAVToken() {
        return this.cachedToken;
    }

    public AVController getAVController() {
        return this.cachedController;
    }

    public void setAVPlaybackState(AVPlaybackState aVPlaybackState) {
        this.sessionAdapter.setAVPlaybackState(aVPlaybackState);
    }

    public void setAVMetadata(AVMetadata aVMetadata) {
        if (aVMetadata == null) {
            LOGGER.error("avMetadata is null", new Object[0]);
        } else {
            this.sessionAdapter.setAVMetadata(aVMetadata);
        }
    }

    public void setAVQueue(List<AVQueueElement> list) {
        if (list == null) {
            LOGGER.error("AVQueueElement list is null", new Object[0]);
        } else {
            this.sessionAdapter.setAVQueue(list);
        }
    }

    public void setAVQueueTitle(CharSequence charSequence) {
        this.sessionAdapter.setAVQueueTitle(charSequence);
    }

    public void setAVRatingStyle(int i) {
        this.sessionAdapter.setAVRatingStyle(i);
    }

    public void setOptions(PacMap pacMap) {
        this.sessionAdapter.setOptions(pacMap);
    }

    public AVCallerUserInfo getCurrentControllerInfo() {
        try {
            return this.sessionAdapter.getCurrentControllerInfo();
        } catch (IllegalStateException unused) {
            LOGGER.error("This should be called inside of AVSessionCallback methods", new Object[0]);
            return null;
        }
    }

    public void notifyVolumeControlChanged(AVVolumeControl aVVolumeControl) {
        if (aVVolumeControl == null) {
            LOGGER.error("AVVolumeControl is null", new Object[0]);
        } else {
            this.sessionAdapter.notifyVolumeControlChanged(aVVolumeControl.getHostVolumeControl());
        }
    }

    public String getCallerPackageName() {
        return this.sessionAdapter.getCallerPackageName();
    }

    public static boolean isAVSessionActive(int i) {
        return AVSessionAdapter.isAVSessionActive(i);
    }
}
