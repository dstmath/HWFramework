package ohos.media.sessioncore;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.ability.Ability;
import ohos.app.Context;
import ohos.app.GeneralReceiver;
import ohos.event.intentagent.IntentAgent;
import ohos.media.common.AVMetadata;
import ohos.media.common.sessioncore.AVControllerCallback;
import ohos.media.common.sessioncore.AVPlaybackState;
import ohos.media.common.sessioncore.AVQueueElement;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.sessioncore.adapter.AVControllerAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.multimodalinput.event.KeyEvent;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public final class AVController {
    private static final List<Integer> AV_KEY_LIST = new ArrayList();
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVController.class);
    private final AVControllerAdapter hostController;
    private final PlayControls playControls;

    static {
        AV_KEY_LIST.add(10);
        AV_KEY_LIST.add(11);
        AV_KEY_LIST.add(12);
        AV_KEY_LIST.add(13);
        AV_KEY_LIST.add(14);
        AV_KEY_LIST.add(15);
        AV_KEY_LIST.add(Integer.valueOf((int) KeyEvent.KEY_MEDIA_PLAY));
        AV_KEY_LIST.add(Integer.valueOf((int) KeyEvent.KEY_MEDIA_PAUSE));
        AV_KEY_LIST.add(Integer.valueOf((int) KeyEvent.KEY_MEDIA_CLOSE));
        AV_KEY_LIST.add(Integer.valueOf((int) KeyEvent.KEY_MEDIA_EJECT));
        AV_KEY_LIST.add(Integer.valueOf((int) KeyEvent.KEY_MEDIA_RECORD));
    }

    private static boolean isMediaKey(int i) {
        return AV_KEY_LIST.contains(Integer.valueOf(i));
    }

    public AVController(Context context, AVToken aVToken) {
        this.hostController = new AVControllerAdapter(context, aVToken);
        this.playControls = new PlayControls();
    }

    public AVController(AVControllerAdapter aVControllerAdapter) {
        if (aVControllerAdapter != null) {
            this.hostController = aVControllerAdapter;
            this.playControls = new PlayControls();
            return;
        }
        throw new IllegalArgumentException("avControllerAdapter is null");
    }

    public Object getHostController() {
        return this.hostController.getHostController();
    }

    public static boolean setControllerForAbility(Ability ability, AVController aVController) {
        if (ability == null) {
            LOGGER.error("ability is null", new Object[0]);
            return false;
        } else if (aVController == null) {
            LOGGER.error("controller is null", new Object[0]);
            return false;
        } else {
            ability.setAVController(aVController.getHostController());
            return true;
        }
    }

    public boolean setAVControllerCallback(AVControllerCallback aVControllerCallback) {
        if (aVControllerCallback != null) {
            return this.hostController.setAVControllerCallback(aVControllerCallback);
        }
        LOGGER.error("callback is null", new Object[0]);
        return false;
    }

    public boolean releaseAVControllerCallback(AVControllerCallback aVControllerCallback) {
        if (aVControllerCallback != null) {
            return this.hostController.releaseAVControllerCallback(aVControllerCallback);
        }
        LOGGER.error("callback is null", new Object[0]);
        return false;
    }

    public List<AVQueueElement> getAVQueueElement() {
        return this.hostController.getAVQueueElement();
    }

    public CharSequence getAVQueueTitle() {
        return this.hostController.getAVQueueTitle();
    }

    public AVPlaybackState getAVPlaybackState() {
        return this.hostController.getAVPlaybackState().orElse(null);
    }

    public boolean dispatchAVKeyEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            throw new IllegalArgumentException("KeyEvent is null");
        } else if (isMediaKey(keyEvent.getKeyCode())) {
            return this.hostController.dispatchAVKeyEvent(keyEvent);
        } else {
            return false;
        }
    }

    public void sendCustomCommand(String str, PacMap pacMap, GeneralReceiver generalReceiver) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("command is null or invalid!");
        }
        this.hostController.sendCustomCommand(str, pacMap, generalReceiver);
    }

    public IntentAgent getAVSessionAbility() {
        return this.hostController.getAVSessionAbility().orElse(null);
    }

    public AVToken getAVToken() {
        return this.hostController.getAVToken();
    }

    public void adjustAVPlaybackVolume(int i, int i2) {
        this.hostController.adjustAVPlaybackVolume(i, i2);
    }

    public void setAVPlaybackVolume(int i, int i2) {
        this.hostController.setAVPlaybackVolume(i, i2);
    }

    public PacMap getOptions() {
        return this.hostController.getOptions();
    }

    public long getFlags() {
        return this.hostController.getFlags();
    }

    public AVMetadata getAVMetadata() {
        return this.hostController.getAVMetadata().orElse(null);
    }

    public String getSessionOwnerPackageName() {
        return this.hostController.getSessionOwnerPackageName();
    }

    public PacMap getAVSessionInfo() {
        return this.hostController.getAVSessionInfo();
    }

    public PlayControls getPlayControls() {
        return this.playControls;
    }

    public final class PlayControls {
        private PlayControls() {
        }

        public void prepareToPlay() {
            AVController.this.hostController.prepareToPlay();
        }

        public void prepareToPlayByMediaId(String str, PacMap pacMap) {
            AVController.this.hostController.prepareToPlayByMediaId(str, pacMap);
        }

        public void prepareToPlayBySearch(String str, PacMap pacMap) {
            AVController.this.hostController.prepareToPlayBySearch(str, pacMap);
        }

        public void prepareToPlayByUri(Uri uri, PacMap pacMap) {
            AVController.this.hostController.prepareToPlayByUri(uri, pacMap);
        }

        public void play() {
            AVController.this.hostController.play();
        }

        public void playByMediaId(String str, PacMap pacMap) {
            AVController.this.hostController.playByMediaId(str, pacMap);
        }

        public void playBySearch(String str, PacMap pacMap) {
            AVController.this.hostController.playBySearch(str, pacMap);
        }

        public void playByUri(Uri uri, PacMap pacMap) {
            AVController.this.hostController.playByUri(uri, pacMap);
        }

        public void skipToAVQueueItem(long j) {
            AVController.this.hostController.skipToAVQueueItem(j);
        }

        public void pause() {
            AVController.this.hostController.pause();
        }

        public void stop() {
            AVController.this.hostController.stop();
        }

        public void seekTo(long j) {
            AVController.this.hostController.seekTo(j);
        }

        public void playFastForward() {
            AVController.this.hostController.playFastForward();
        }

        public void playNext() {
            AVController.this.hostController.playNext();
        }

        public void rewind() {
            AVController.this.hostController.rewind();
        }

        public void playPrevious() {
            AVController.this.hostController.playPrevious();
        }

        public void setAVPlaybackSpeed(float f) {
            AVController.this.hostController.setAVPlaybackSpeed(f);
        }

        public void sendAVPlaybackCustomAction(AVPlaybackState.AVPlaybackCustomAction aVPlaybackCustomAction, PacMap pacMap) {
            AVController.this.hostController.sendAVPlaybackCustomAction(aVPlaybackCustomAction, pacMap);
        }

        public void sendAVPlaybackCustomAction(String str, PacMap pacMap) {
            AVController.this.hostController.sendAVPlaybackCustomAction(str, pacMap);
        }
    }
}
