package ohos.multimodalinput.eventimpl;

import android.view.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import ohos.multimodalinput.event.BuiltinKeyEvent;
import ohos.utils.Parcel;

/* access modifiers changed from: package-private */
public class BuiltinKeyEventImpl extends BuiltinKeyEvent {
    private static final Map<Integer, Integer> A2Z_KEYMAP = new HashMap();
    private static final int A_KEYCODE_BACK = 10013;
    private static final int A_KEYCODE_CUSTOM1 = 10001;
    private static final int A_KEYCODE_LEFT_KNOB = 10004;
    private static final int A_KEYCODE_LEFT_KNOB_ROLL_DOWN = 10003;
    private static final int A_KEYCODE_LEFT_KNOB_ROLL_UP = 10002;
    private static final int A_KEYCODE_MEDIA_FLICK_DOWN = 10015;
    private static final int A_KEYCODE_MEDIA_FLICK_LEFT = 10016;
    private static final int A_KEYCODE_MEDIA_FLICK_RIGHT = 10017;
    private static final int A_KEYCODE_MEDIA_FLICK_UP = 10014;
    private static final int A_KEYCODE_MEDIA_NEXT = 10011;
    private static final int A_KEYCODE_MEDIA_PLAY_PAUSE = 10020;
    private static final int A_KEYCODE_MEDIA_PREVIOUS = 10010;
    private static final int A_KEYCODE_MEDIA_ROLL_LEFT = 10018;
    private static final int A_KEYCODE_MEDIA_ROLL_RIGHT = 10019;
    private static final int A_KEYCODE_MENU = 10012;
    private static final int A_KEYCODE_RIGHT_KNOB = 10007;
    private static final int A_KEYCODE_RIGHT_KNOB_ROLL_DOWN = 10006;
    private static final int A_KEYCODE_RIGHT_KNOB_ROLL_UP = 10005;
    private static final int A_KEYCODE_VOICE_ASSISTANT = 10009;
    private static final int A_KEYCODE_VOICE_SOURCE_SWITCH = 10008;
    private KeyEvent androidKeyEvent;
    private ohos.multimodalinput.event.KeyEvent keyEventImpl;

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getSourceDevice() {
        return 4;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return false;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }

    static {
        A2Z_KEYMAP.put(10001, 21);
        A2Z_KEYMAP.put(10002, 10001);
        A2Z_KEYMAP.put(10003, 10002);
        A2Z_KEYMAP.put(10004, 10003);
        A2Z_KEYMAP.put(10005, 10004);
        A2Z_KEYMAP.put(10006, 10005);
        A2Z_KEYMAP.put(10007, 10006);
        A2Z_KEYMAP.put(10008, 10007);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_VOICE_ASSISTANT), 20);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_PREVIOUS), 13);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_NEXT), 12);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MENU), 10008);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_BACK), 2);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_FLICK_UP), 40);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_FLICK_DOWN), 41);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_FLICK_LEFT), 13);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_FLICK_RIGHT), 12);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_ROLL_LEFT), 17);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_ROLL_RIGHT), 16);
        A2Z_KEYMAP.put(Integer.valueOf((int) A_KEYCODE_MEDIA_PLAY_PAUSE), 10);
        A2Z_KEYMAP.put(265, 1001);
    }

    BuiltinKeyEventImpl(KeyEvent keyEvent) {
        this.androidKeyEvent = keyEvent;
        this.keyEventImpl = new KeyEventImpl(keyEvent);
    }

    static boolean isBuiltInKeyCode(int i) {
        return A2Z_KEYMAP.containsKey(Integer.valueOf(i));
    }

    @Override // ohos.multimodalinput.event.KeyEvent
    public boolean isKeyDown() {
        return this.keyEventImpl.isKeyDown();
    }

    @Override // ohos.multimodalinput.event.KeyEvent
    public int getKeyCode() {
        int keyCode = this.androidKeyEvent.getKeyCode();
        if (A2Z_KEYMAP.containsKey(Integer.valueOf(keyCode))) {
            return A2Z_KEYMAP.get(Integer.valueOf(keyCode)).intValue();
        }
        return this.keyEventImpl.getKeyCode();
    }

    @Override // ohos.multimodalinput.event.KeyEvent
    public long getKeyDownDuration() {
        return this.keyEventImpl.getKeyDownDuration();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public String getDeviceId() {
        return this.keyEventImpl.getDeviceId();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getInputDeviceId() {
        return this.keyEventImpl.getInputDeviceId();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public long getOccurredTime() {
        return this.keyEventImpl.getOccurredTime();
    }
}
