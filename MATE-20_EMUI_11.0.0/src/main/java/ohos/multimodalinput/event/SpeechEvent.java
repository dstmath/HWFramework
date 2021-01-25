package ohos.multimodalinput.event;

import java.util.Optional;

public class SpeechEvent extends MultimodalEvent {
    public static final int ACTION_HIT_HOTWORD = 3;
    public static final int ACTION_SWITCH_OFF = 2;
    public static final int ACTION_SWITCH_ON = 1;
    public static final int MATCH_MODE_EXACT = 1;
    public static final int MATCH_MODE_FUZZY = 2;
    public static final int SCENES_AUDIO = 2;
    public static final int SCENES_COMMON = 5;
    public static final int SCENES_PAGE = 3;
    public static final int SCENES_SWITCH = 4;
    public static final int SCENES_VIDEO = 1;
    private int action;
    private String actionProperty;
    private int deviceId;
    private int mode;
    private int scene;
    private long time;

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public String getDeviceId() {
        return "";
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getSourceDevice() {
        return 6;
    }

    public static Optional<SpeechEvent> createEvent(long j, int i, String str) {
        if (i > 3 || i < 1) {
            return Optional.empty();
        }
        return Optional.of(new SpeechEvent(j, i, str));
    }

    private SpeechEvent(long j, int i, String str) {
        this.time = j;
        this.action = i;
        this.actionProperty = str;
        this.scene = 3;
        this.mode = 1;
    }

    private SpeechEvent() {
    }

    public int getAction() {
        return this.action;
    }

    public int getScene() {
        return this.scene;
    }

    public String getActionProperty() {
        return this.actionProperty;
    }

    public int getMatchMode() {
        return this.mode;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getInputDeviceId() {
        return this.deviceId;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public long getOccurredTime() {
        return this.time;
    }
}
