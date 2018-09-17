package android.view;

import android.hardware.input.InputManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AndroidRuntimeException;
import android.util.SparseIntArray;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.text.Normalizer;
import java.text.Normalizer.Form;

public class KeyCharacterMap implements Parcelable {
    private static final int ACCENT_ACUTE = 180;
    private static final int ACCENT_BREVE = 728;
    private static final int ACCENT_CARON = 711;
    private static final int ACCENT_CEDILLA = 184;
    private static final int ACCENT_CIRCUMFLEX = 710;
    private static final int ACCENT_CIRCUMFLEX_LEGACY = 94;
    private static final int ACCENT_COMMA_ABOVE = 8125;
    private static final int ACCENT_COMMA_ABOVE_RIGHT = 700;
    private static final int ACCENT_DOT_ABOVE = 729;
    private static final int ACCENT_DOT_BELOW = 46;
    private static final int ACCENT_DOUBLE_ACUTE = 733;
    private static final int ACCENT_GRAVE = 715;
    private static final int ACCENT_GRAVE_LEGACY = 96;
    private static final int ACCENT_HOOK_ABOVE = 704;
    private static final int ACCENT_HORN = 39;
    private static final int ACCENT_MACRON = 175;
    private static final int ACCENT_MACRON_BELOW = 717;
    private static final int ACCENT_OGONEK = 731;
    private static final int ACCENT_REVERSED_COMMA_ABOVE = 701;
    private static final int ACCENT_RING_ABOVE = 730;
    private static final int ACCENT_STROKE = 45;
    private static final int ACCENT_TILDE = 732;
    private static final int ACCENT_TILDE_LEGACY = 126;
    private static final int ACCENT_TURNED_COMMA_ABOVE = 699;
    private static final int ACCENT_UMLAUT = 168;
    private static final int ACCENT_VERTICAL_LINE_ABOVE = 712;
    private static final int ACCENT_VERTICAL_LINE_BELOW = 716;
    public static final int ALPHA = 3;
    @Deprecated
    public static final int BUILT_IN_KEYBOARD = 0;
    private static final int CHAR_SPACE = 32;
    public static final int COMBINING_ACCENT = Integer.MIN_VALUE;
    public static final int COMBINING_ACCENT_MASK = Integer.MAX_VALUE;
    public static final Creator<KeyCharacterMap> CREATOR = new Creator<KeyCharacterMap>() {
        public KeyCharacterMap createFromParcel(Parcel in) {
            return new KeyCharacterMap(in, null);
        }

        public KeyCharacterMap[] newArray(int size) {
            return new KeyCharacterMap[size];
        }
    };
    public static final int FULL = 4;
    public static final char HEX_INPUT = '';
    public static final int MODIFIER_BEHAVIOR_CHORDED = 0;
    public static final int MODIFIER_BEHAVIOR_CHORDED_OR_TOGGLED = 1;
    public static final int NUMERIC = 1;
    public static final char PICKER_DIALOG_INPUT = '';
    public static final int PREDICTIVE = 2;
    public static final int SPECIAL_FUNCTION = 5;
    public static final int VIRTUAL_KEYBOARD = -1;
    private static final SparseIntArray sAccentToCombining = new SparseIntArray();
    private static final SparseIntArray sCombiningToAccent = new SparseIntArray();
    private static final StringBuilder sDeadKeyBuilder = new StringBuilder();
    private static final SparseIntArray sDeadKeyCache = new SparseIntArray();
    private long mPtr;

    public static final class FallbackAction {
        private static final int MAX_RECYCLED = 10;
        private static FallbackAction sRecycleBin;
        private static final Object sRecycleLock = new Object();
        private static int sRecycledCount;
        public int keyCode;
        public int metaState;
        private FallbackAction next;

        private FallbackAction() {
        }

        public static FallbackAction obtain() {
            FallbackAction target;
            synchronized (sRecycleLock) {
                if (sRecycleBin == null) {
                    target = new FallbackAction();
                } else {
                    target = sRecycleBin;
                    sRecycleBin = target.next;
                    sRecycledCount--;
                    target.next = null;
                }
            }
            return target;
        }

        public void recycle() {
            synchronized (sRecycleLock) {
                if (sRecycledCount < 10) {
                    this.next = sRecycleBin;
                    sRecycleBin = this;
                    sRecycledCount++;
                } else {
                    this.next = null;
                }
            }
        }
    }

    @Deprecated
    public static class KeyData {
        public static final int META_LENGTH = 4;
        public char displayLabel;
        public char[] meta = new char[4];
        public char number;
    }

    public static class UnavailableException extends AndroidRuntimeException {
        public UnavailableException(String msg) {
            super(msg);
        }
    }

    /* synthetic */ KeyCharacterMap(Parcel in, KeyCharacterMap -this1) {
        this(in);
    }

    private static native void nativeDispose(long j);

    private static native char nativeGetCharacter(long j, int i, int i2);

    private static native char nativeGetDisplayLabel(long j, int i);

    private static native KeyEvent[] nativeGetEvents(long j, char[] cArr);

    private static native boolean nativeGetFallbackAction(long j, int i, int i2, FallbackAction fallbackAction);

    private static native int nativeGetKeyboardType(long j);

    private static native char nativeGetMatch(long j, int i, char[] cArr, int i2);

    private static native char nativeGetNumber(long j, int i);

    private static native long nativeReadFromParcel(Parcel parcel);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    static {
        addCombining(768, 715);
        addCombining(769, 180);
        addCombining(770, 710);
        addCombining(771, 732);
        addCombining(772, 175);
        addCombining(774, 728);
        addCombining(775, 729);
        addCombining(776, 168);
        addCombining(MetricsEvent.APP_SPECIAL_PERMISSION_NOTIVIEW_DENY, 704);
        addCombining(MetricsEvent.APP_SPECIAL_PERMISSION_PREMIUM_SMS_ASK, 730);
        addCombining(MetricsEvent.APP_SPECIAL_PERMISSION_PREMIUM_SMS_DENY, 733);
        addCombining(MetricsEvent.APP_SPECIAL_PERMISSION_PREMIUM_SMS_ALWAYS_ALLOW, 711);
        addCombining(MetricsEvent.APP_SPECIAL_PERMISSION_UNL_DATA_ALLOW, 712);
        addCombining(MetricsEvent.DEFAULT_EMERGENCY_APP_PICKER, 699);
        addCombining(MetricsEvent.DEFAULT_HOME_PICKER, ACCENT_COMMA_ABOVE);
        addCombining(MetricsEvent.DEFAULT_PHONE_PICKER, 701);
        addCombining(MetricsEvent.DEFAULT_SMS_PICKER, 700);
        addCombining(MetricsEvent.NOTIFICATION_SINCE_UPDATE_MILLIS, 39);
        addCombining(803, 46);
        addCombining(MetricsEvent.ACTION_APP_FORCE_STOP, 184);
        addCombining(MetricsEvent.MANAGE_EXTERNAL_SOURCES, 731);
        addCombining(MetricsEvent.PROVISIONING_TERMS_ACTIVITY_TIME_MS, 716);
        addCombining(MetricsEvent.SUW_ACCESSIBILITY_TOGGLE_SELECT_TO_SPEAK, 717);
        addCombining(821, 45);
        sCombiningToAccent.append(MetricsEvent.NOTIFICATION_SNOOZED_CRITERIA, 715);
        sCombiningToAccent.append(MetricsEvent.FIELD_CONTEXT, 180);
        sCombiningToAccent.append(MetricsEvent.WIFI_NETWORK_RECOMMENDATION_SAVED_NETWORK_EVALUATOR, ACCENT_COMMA_ABOVE);
        sAccentToCombining.append(96, 768);
        sAccentToCombining.append(94, 770);
        sAccentToCombining.append(126, 771);
        addDeadKey(45, 68, 272);
        addDeadKey(45, 71, MetricsEvent.ACTION_SUPPORT_DISCLAIMER_OK);
        addDeadKey(45, 72, 294);
        addDeadKey(45, 73, MetricsEvent.ACTION_NOTIFICATION_EXPANDER);
        addDeadKey(45, 76, 321);
        addDeadKey(45, 79, 216);
        addDeadKey(45, 84, 358);
        addDeadKey(45, 100, 273);
        addDeadKey(45, 103, MetricsEvent.ACTION_SUPPORT_DAIL_TOLLFREE);
        addDeadKey(45, 104, 295);
        addDeadKey(45, 105, MetricsEvent.PROVISIONING_ENTRY_POINT_QR_CODE);
        addDeadKey(45, 108, 322);
        addDeadKey(45, 111, 248);
        addDeadKey(45, 116, 359);
    }

    private static void addCombining(int combining, int accent) {
        sCombiningToAccent.append(combining, accent);
        sAccentToCombining.append(accent, combining);
    }

    private static void addDeadKey(int accent, int c, int result) {
        int combining = sAccentToCombining.get(accent);
        if (combining == 0) {
            throw new IllegalStateException("Invalid dead key declaration.");
        }
        sDeadKeyCache.put((combining << 16) | c, result);
    }

    private KeyCharacterMap(Parcel in) {
        if (in == null) {
            throw new IllegalArgumentException("parcel must not be null");
        }
        this.mPtr = nativeReadFromParcel(in);
        if (this.mPtr == 0) {
            throw new RuntimeException("Could not read KeyCharacterMap from parcel.");
        }
    }

    private KeyCharacterMap(long ptr) {
        this.mPtr = ptr;
    }

    protected void finalize() throws Throwable {
        if (this.mPtr != 0) {
            nativeDispose(this.mPtr);
            this.mPtr = 0;
        }
    }

    public static KeyCharacterMap load(int deviceId) {
        InputManager im = InputManager.getInstance();
        InputDevice inputDevice = im.getInputDevice(deviceId);
        if (inputDevice == null) {
            inputDevice = im.getInputDevice(-1);
            if (inputDevice == null) {
                throw new UnavailableException("Could not load key character map for device " + deviceId);
            }
        }
        return inputDevice.getKeyCharacterMap();
    }

    public int get(int keyCode, int metaState) {
        char ch = nativeGetCharacter(this.mPtr, keyCode, KeyEvent.normalizeMetaState(metaState));
        int map = sCombiningToAccent.get(ch);
        if (map != 0) {
            return Integer.MIN_VALUE | map;
        }
        return ch;
    }

    public FallbackAction getFallbackAction(int keyCode, int metaState) {
        FallbackAction action = FallbackAction.obtain();
        if (nativeGetFallbackAction(this.mPtr, keyCode, KeyEvent.normalizeMetaState(metaState), action)) {
            action.metaState = KeyEvent.normalizeMetaState(action.metaState);
            return action;
        }
        action.recycle();
        return null;
    }

    public char getNumber(int keyCode) {
        return nativeGetNumber(this.mPtr, keyCode);
    }

    public char getMatch(int keyCode, char[] chars) {
        return getMatch(keyCode, chars, 0);
    }

    public char getMatch(int keyCode, char[] chars, int metaState) {
        if (chars == null) {
            throw new IllegalArgumentException("chars must not be null.");
        }
        return nativeGetMatch(this.mPtr, keyCode, chars, KeyEvent.normalizeMetaState(metaState));
    }

    public char getDisplayLabel(int keyCode) {
        return nativeGetDisplayLabel(this.mPtr, keyCode);
    }

    public static int getDeadChar(int accent, int c) {
        if (c == accent || 32 == c) {
            return accent;
        }
        int combining = sAccentToCombining.get(accent);
        if (combining == 0) {
            return 0;
        }
        int combined;
        int combination = (combining << 16) | c;
        synchronized (sDeadKeyCache) {
            combined = sDeadKeyCache.get(combination, -1);
            if (combined == -1) {
                sDeadKeyBuilder.setLength(0);
                sDeadKeyBuilder.append((char) c);
                sDeadKeyBuilder.append((char) combining);
                String result = Normalizer.normalize(sDeadKeyBuilder, Form.NFC);
                combined = result.codePointCount(0, result.length()) == 1 ? result.codePointAt(0) : 0;
                sDeadKeyCache.put(combination, combined);
            }
        }
        return combined;
    }

    @Deprecated
    public boolean getKeyData(int keyCode, KeyData results) {
        if (results.meta.length < 4) {
            throw new IndexOutOfBoundsException("results.meta.length must be >= 4");
        }
        char displayLabel = nativeGetDisplayLabel(this.mPtr, keyCode);
        if (displayLabel == 0) {
            return false;
        }
        results.displayLabel = displayLabel;
        results.number = nativeGetNumber(this.mPtr, keyCode);
        results.meta[0] = nativeGetCharacter(this.mPtr, keyCode, 0);
        results.meta[1] = nativeGetCharacter(this.mPtr, keyCode, 1);
        results.meta[2] = nativeGetCharacter(this.mPtr, keyCode, 2);
        results.meta[3] = nativeGetCharacter(this.mPtr, keyCode, 3);
        return true;
    }

    public KeyEvent[] getEvents(char[] chars) {
        if (chars != null) {
            return nativeGetEvents(this.mPtr, chars);
        }
        throw new IllegalArgumentException("chars must not be null.");
    }

    public boolean isPrintingKey(int keyCode) {
        switch (Character.getType(nativeGetDisplayLabel(this.mPtr, keyCode))) {
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                return false;
            default:
                return true;
        }
    }

    public int getKeyboardType() {
        return nativeGetKeyboardType(this.mPtr);
    }

    public int getModifierBehavior() {
        switch (getKeyboardType()) {
            case 4:
            case 5:
                return 0;
            default:
                return 1;
        }
    }

    public static boolean deviceHasKey(int keyCode) {
        return InputManager.getInstance().deviceHasKeys(new int[]{keyCode})[0];
    }

    public static boolean[] deviceHasKeys(int[] keyCodes) {
        return InputManager.getInstance().deviceHasKeys(keyCodes);
    }

    public void writeToParcel(Parcel out, int flags) {
        if (out == null) {
            throw new IllegalArgumentException("parcel must not be null");
        }
        nativeWriteToParcel(this.mPtr, out);
    }

    public int describeContents() {
        return 0;
    }
}
