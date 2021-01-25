package ohos.multimodalinput.eventimpl;

import android.view.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.adapter.InnerMetadata;
import ohos.miscservices.inputmethod.InputAttribute;
import ohos.multimodalinput.event.KeyBoardEvent;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

/* access modifiers changed from: package-private */
public class KeyBoardEventImpl extends KeyBoardEvent {
    private static final Map<Integer, Integer> A2Z_KEYMAP = new HashMap();
    static final String EVENT_TYPE_KEYBOARD = "KeyBoardEvent";
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218114065, "KeyBoardEventImpl");
    static Sequenceable.Producer<KeyBoardEvent> PRODUCER = new Sequenceable.Producer<KeyBoardEvent>() {
        /* class ohos.multimodalinput.eventimpl.KeyBoardEventImpl.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public KeyBoardEvent createFromParcel(Parcel parcel) {
            KeyBoardEventImpl keyBoardEventImpl = new KeyBoardEventImpl();
            keyBoardEventImpl.unmarshalling(parcel);
            return keyBoardEventImpl;
        }
    };
    private static final Map<Integer, Integer> Z2A_KEYMAP = new HashMap();
    private KeyEvent androidKeyEvent;
    private ohos.multimodalinput.event.KeyEvent keyEventImpl;

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getSourceDevice() {
        return 1;
    }

    static {
        A2Z_KEYMAP.put(7, 2000);
        A2Z_KEYMAP.put(8, 2001);
        A2Z_KEYMAP.put(9, 2002);
        A2Z_KEYMAP.put(10, 2003);
        A2Z_KEYMAP.put(11, 2004);
        A2Z_KEYMAP.put(12, 2005);
        A2Z_KEYMAP.put(13, 2006);
        A2Z_KEYMAP.put(14, 2007);
        A2Z_KEYMAP.put(15, 2008);
        A2Z_KEYMAP.put(16, 2009);
        A2Z_KEYMAP.put(17, 2010);
        A2Z_KEYMAP.put(18, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_POUND));
        A2Z_KEYMAP.put(19, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_DPAD_UP));
        A2Z_KEYMAP.put(20, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_DPAD_DOWN));
        A2Z_KEYMAP.put(21, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_DPAD_LEFT));
        A2Z_KEYMAP.put(22, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_DPAD_RIGHT));
        A2Z_KEYMAP.put(23, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_DPAD_CENTER));
        A2Z_KEYMAP.put(29, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_A));
        A2Z_KEYMAP.put(30, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_B));
        A2Z_KEYMAP.put(31, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_C));
        A2Z_KEYMAP.put(32, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_D));
        A2Z_KEYMAP.put(33, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_E));
        A2Z_KEYMAP.put(34, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F));
        A2Z_KEYMAP.put(35, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_G));
        A2Z_KEYMAP.put(36, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_H));
        A2Z_KEYMAP.put(37, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_I));
        A2Z_KEYMAP.put(38, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_J));
        A2Z_KEYMAP.put(39, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_K));
        A2Z_KEYMAP.put(40, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_L));
        A2Z_KEYMAP.put(41, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_M));
        A2Z_KEYMAP.put(42, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_N));
        A2Z_KEYMAP.put(43, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_O));
        A2Z_KEYMAP.put(44, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_P));
        A2Z_KEYMAP.put(45, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_Q));
        A2Z_KEYMAP.put(46, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_R));
        A2Z_KEYMAP.put(47, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_S));
        A2Z_KEYMAP.put(48, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_T));
        A2Z_KEYMAP.put(49, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_U));
        A2Z_KEYMAP.put(50, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_V));
        A2Z_KEYMAP.put(51, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_W));
        A2Z_KEYMAP.put(52, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_X));
        A2Z_KEYMAP.put(53, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_Y));
        A2Z_KEYMAP.put(54, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_Z));
        A2Z_KEYMAP.put(55, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_COMMA));
        A2Z_KEYMAP.put(56, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_PERIOD));
        A2Z_KEYMAP.put(57, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_ALT_LEFT));
        A2Z_KEYMAP.put(58, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_ALT_RIGHT));
        A2Z_KEYMAP.put(59, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_SHIFT_LEFT));
        A2Z_KEYMAP.put(60, 2048);
        A2Z_KEYMAP.put(61, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_TAB));
        A2Z_KEYMAP.put(62, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_SPACE));
        A2Z_KEYMAP.put(63, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_SYM));
        A2Z_KEYMAP.put(64, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_EXPLORER));
        A2Z_KEYMAP.put(65, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_ENVELOPE));
        A2Z_KEYMAP.put(66, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_ENTER));
        A2Z_KEYMAP.put(67, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_DEL));
        A2Z_KEYMAP.put(68, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_GRAVE));
        A2Z_KEYMAP.put(69, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MINUS));
        A2Z_KEYMAP.put(70, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_EQUALS));
        A2Z_KEYMAP.put(71, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_LEFT_BRACKET));
        A2Z_KEYMAP.put(72, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_RIGHT_BRACKET));
        A2Z_KEYMAP.put(73, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_BACKSLASH));
        A2Z_KEYMAP.put(74, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_SEMICOLON));
        A2Z_KEYMAP.put(75, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_APOSTROPHE));
        A2Z_KEYMAP.put(76, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_SLASH));
        A2Z_KEYMAP.put(77, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_AT));
        A2Z_KEYMAP.put(81, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_PLUS));
        A2Z_KEYMAP.put(82, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MENU));
        A2Z_KEYMAP.put(92, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_PAGE_UP));
        A2Z_KEYMAP.put(93, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_PAGE_DOWN));
        A2Z_KEYMAP.put(111, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_ESCAPE));
        A2Z_KEYMAP.put(112, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_FORWARD_DEL));
        A2Z_KEYMAP.put(113, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_CTRL_LEFT));
        A2Z_KEYMAP.put(114, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_CTRL_RIGHT));
        A2Z_KEYMAP.put(Integer.valueOf((int) Metadata.SceneDetectionType.SMART_SUGGEST_MODE_MOON), Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_CAPS_LOCK));
        A2Z_KEYMAP.put(116, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_SCROLL_LOCK));
        A2Z_KEYMAP.put(Integer.valueOf((int) InnerMetadata.SceneDetectionType.SMART_SUGGEST_MODE_BEAUTY), Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_META_LEFT));
        A2Z_KEYMAP.put(118, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_META_RIGHT));
        A2Z_KEYMAP.put(119, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_FUNCTION));
        A2Z_KEYMAP.put(120, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_SYSRQ));
        A2Z_KEYMAP.put(121, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_BREAK));
        A2Z_KEYMAP.put(122, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MOVE_HOME));
        A2Z_KEYMAP.put(123, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MOVE_END));
        A2Z_KEYMAP.put(124, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_INSERT));
        A2Z_KEYMAP.put(125, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_FORWARD));
        A2Z_KEYMAP.put(126, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MEDIA_PLAY));
        A2Z_KEYMAP.put(127, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MEDIA_PAUSE));
        A2Z_KEYMAP.put(128, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MEDIA_CLOSE));
        A2Z_KEYMAP.put(129, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MEDIA_EJECT));
        A2Z_KEYMAP.put(130, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_MEDIA_RECORD));
        A2Z_KEYMAP.put(131, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F1));
        A2Z_KEYMAP.put(132, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F2));
        A2Z_KEYMAP.put(133, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F3));
        A2Z_KEYMAP.put(134, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F4));
        A2Z_KEYMAP.put(135, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F5));
        A2Z_KEYMAP.put(136, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F6));
        A2Z_KEYMAP.put(137, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F7));
        A2Z_KEYMAP.put(138, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F8));
        A2Z_KEYMAP.put(139, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F9));
        A2Z_KEYMAP.put(140, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F10));
        A2Z_KEYMAP.put(141, 2100);
        A2Z_KEYMAP.put(142, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_F12));
        A2Z_KEYMAP.put(143, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUM_LOCK));
        A2Z_KEYMAP.put(Integer.valueOf((int) InputAttribute.PATTERN_TEXT_VARIATION_VISIBLE_PASSWORD), Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_0));
        A2Z_KEYMAP.put(145, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_1));
        A2Z_KEYMAP.put(146, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_2));
        A2Z_KEYMAP.put(147, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_3));
        A2Z_KEYMAP.put(148, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_4));
        A2Z_KEYMAP.put(149, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_5));
        A2Z_KEYMAP.put(150, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_6));
        A2Z_KEYMAP.put(151, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_7));
        A2Z_KEYMAP.put(152, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_8));
        A2Z_KEYMAP.put(153, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_9));
        A2Z_KEYMAP.put(154, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_DIVIDE));
        A2Z_KEYMAP.put(155, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_MULTIPLY));
        A2Z_KEYMAP.put(156, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_SUBTRACT));
        A2Z_KEYMAP.put(157, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_ADD));
        A2Z_KEYMAP.put(158, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_DOT));
        A2Z_KEYMAP.put(159, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_COMMA));
        A2Z_KEYMAP.put(160, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_ENTER));
        A2Z_KEYMAP.put(161, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_EQUALS));
        A2Z_KEYMAP.put(162, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_LEFT_PAREN));
        A2Z_KEYMAP.put(163, Integer.valueOf((int) ohos.multimodalinput.event.KeyEvent.KEY_NUMPAD_RIGHT_PAREN));
        for (Map.Entry<Integer, Integer> entry : A2Z_KEYMAP.entrySet()) {
            Z2A_KEYMAP.put(entry.getValue(), entry.getKey());
        }
    }

    static boolean isKeyBoardKeyCode(int i) {
        return A2Z_KEYMAP.containsKey(Integer.valueOf(i));
    }

    KeyBoardEventImpl() {
    }

    KeyBoardEventImpl(KeyEvent keyEvent) {
        this.androidKeyEvent = keyEvent;
        this.keyEventImpl = new KeyEventImpl(keyEvent);
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
        return -1;
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

    @Override // ohos.multimodalinput.event.KeyBoardEvent
    public boolean isNoncharacterKeyPressed(int i) {
        if (i == 2051) {
            return this.androidKeyEvent.isSymPressed();
        }
        if (i == 2102) {
            return this.androidKeyEvent.isNumLockOn();
        }
        switch (i) {
            case ohos.multimodalinput.event.KeyEvent.KEY_ALT_LEFT /* 2045 */:
            case ohos.multimodalinput.event.KeyEvent.KEY_ALT_RIGHT /* 2046 */:
                return this.androidKeyEvent.isAltPressed();
            case ohos.multimodalinput.event.KeyEvent.KEY_SHIFT_LEFT /* 2047 */:
            case 2048:
                return this.androidKeyEvent.isShiftPressed();
            default:
                switch (i) {
                    case ohos.multimodalinput.event.KeyEvent.KEY_CTRL_LEFT /* 2072 */:
                    case ohos.multimodalinput.event.KeyEvent.KEY_CTRL_RIGHT /* 2073 */:
                        return this.androidKeyEvent.isCtrlPressed();
                    case ohos.multimodalinput.event.KeyEvent.KEY_CAPS_LOCK /* 2074 */:
                        return this.androidKeyEvent.isCapsLockOn();
                    case ohos.multimodalinput.event.KeyEvent.KEY_SCROLL_LOCK /* 2075 */:
                        return this.androidKeyEvent.isScrollLockOn();
                    case ohos.multimodalinput.event.KeyEvent.KEY_META_LEFT /* 2076 */:
                    case ohos.multimodalinput.event.KeyEvent.KEY_META_RIGHT /* 2077 */:
                        return this.androidKeyEvent.isMetaPressed();
                    case ohos.multimodalinput.event.KeyEvent.KEY_FUNCTION /* 2078 */:
                        return this.androidKeyEvent.isFunctionPressed();
                    default:
                        HiLog.error(LOG_LABEL, "not a noncharacter key: %{public}d", Integer.valueOf(i));
                        return false;
                }
        }
    }

    @Override // ohos.multimodalinput.event.KeyBoardEvent
    public boolean isNoncharacterKeyPressed(int i, int i2) {
        return isNoncharacterKeyPressed(i) && isNoncharacterKeyPressed(i2);
    }

    @Override // ohos.multimodalinput.event.KeyBoardEvent
    public boolean isNoncharacterKeyPressed(int i, int i2, int i3) {
        return isNoncharacterKeyPressed(i) && isNoncharacterKeyPressed(i2) && isNoncharacterKeyPressed(i3);
    }

    @Override // ohos.multimodalinput.event.KeyBoardEvent
    public int getUnicode() {
        return this.androidKeyEvent.getUnicodeChar();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        android.os.Parcel obtain = android.os.Parcel.obtain();
        boolean z = false;
        this.androidKeyEvent.writeToParcel(obtain, 0);
        obtain.setDataPosition(0);
        if (parcel.writeString(EVENT_TYPE_KEYBOARD) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeLong(obtain.readLong()) && parcel.writeLong(obtain.readLong()) && parcel.writeString(obtain.readString())) {
            z = true;
        }
        obtain.recycle();
        return z;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        android.os.Parcel obtain = android.os.Parcel.obtain();
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeLong(parcel.readLong());
        obtain.writeLong(parcel.readLong());
        obtain.writeString(parcel.readString());
        obtain.setDataPosition(0);
        this.androidKeyEvent = (KeyEvent) KeyEvent.CREATOR.createFromParcel(obtain);
        this.keyEventImpl = new KeyEventImpl(this.androidKeyEvent);
        obtain.recycle();
        return true;
    }

    /* access modifiers changed from: package-private */
    public KeyEvent getHostKeyEvent() {
        return this.androidKeyEvent;
    }
}
