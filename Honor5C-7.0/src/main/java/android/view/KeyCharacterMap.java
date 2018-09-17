package android.view;

import android.hardware.input.InputManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AndroidRuntimeException;
import android.util.SparseIntArray;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.plug.PGSdk;
import com.nxp.nfc.gsma.internal.NxpNfcController;
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
    public static final Creator<KeyCharacterMap> CREATOR = null;
    public static final int FULL = 4;
    public static final char HEX_INPUT = '\uef00';
    public static final int MODIFIER_BEHAVIOR_CHORDED = 0;
    public static final int MODIFIER_BEHAVIOR_CHORDED_OR_TOGGLED = 1;
    public static final int NUMERIC = 1;
    public static final char PICKER_DIALOG_INPUT = '\uef01';
    public static final int PREDICTIVE = 2;
    public static final int SPECIAL_FUNCTION = 5;
    public static final int VIRTUAL_KEYBOARD = -1;
    private static final SparseIntArray sAccentToCombining = null;
    private static final SparseIntArray sCombiningToAccent = null;
    private static final StringBuilder sDeadKeyBuilder = null;
    private static final SparseIntArray sDeadKeyCache = null;
    private long mPtr;

    public static final class FallbackAction {
        private static final int MAX_RECYCLED = 10;
        private static FallbackAction sRecycleBin;
        private static final Object sRecycleLock = null;
        private static int sRecycledCount;
        public int keyCode;
        public int metaState;
        private FallbackAction next;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.KeyCharacterMap.FallbackAction.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.KeyCharacterMap.FallbackAction.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.KeyCharacterMap.FallbackAction.<clinit>():void");
        }

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
                    sRecycledCount += KeyCharacterMap.VIRTUAL_KEYBOARD;
                    target.next = null;
                }
            }
            return target;
        }

        public void recycle() {
            synchronized (sRecycleLock) {
                if (sRecycledCount < MAX_RECYCLED) {
                    this.next = sRecycleBin;
                    sRecycleBin = this;
                    sRecycledCount += KeyCharacterMap.NUMERIC;
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
        public char[] meta;
        public char number;

        public KeyData() {
            this.meta = new char[META_LENGTH];
        }
    }

    public static class UnavailableException extends AndroidRuntimeException {
        public UnavailableException(String msg) {
            super(msg);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.KeyCharacterMap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.KeyCharacterMap.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.KeyCharacterMap.<clinit>():void");
    }

    /* synthetic */ KeyCharacterMap(Parcel in, KeyCharacterMap keyCharacterMap) {
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
            inputDevice = im.getInputDevice(VIRTUAL_KEYBOARD);
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
            return COMBINING_ACCENT | map;
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
        return getMatch(keyCode, chars, MODIFIER_BEHAVIOR_CHORDED);
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
        if (c == accent || CHAR_SPACE == c) {
            return accent;
        }
        int combining = sAccentToCombining.get(accent);
        if (combining == 0) {
            return MODIFIER_BEHAVIOR_CHORDED;
        }
        int combined;
        int combination = (combining << 16) | c;
        synchronized (sDeadKeyCache) {
            combined = sDeadKeyCache.get(combination, VIRTUAL_KEYBOARD);
            if (combined == VIRTUAL_KEYBOARD) {
                sDeadKeyBuilder.setLength(MODIFIER_BEHAVIOR_CHORDED);
                sDeadKeyBuilder.append((char) c);
                sDeadKeyBuilder.append((char) combining);
                String result = Normalizer.normalize(sDeadKeyBuilder, Form.NFC);
                combined = result.codePointCount(MODIFIER_BEHAVIOR_CHORDED, result.length()) == NUMERIC ? result.codePointAt(MODIFIER_BEHAVIOR_CHORDED) : MODIFIER_BEHAVIOR_CHORDED;
                sDeadKeyCache.put(combination, combined);
            }
        }
        return combined;
    }

    @Deprecated
    public boolean getKeyData(int keyCode, KeyData results) {
        if (results.meta.length < FULL) {
            throw new IndexOutOfBoundsException("results.meta.length must be >= 4");
        }
        char displayLabel = nativeGetDisplayLabel(this.mPtr, keyCode);
        if (displayLabel == '\u0000') {
            return false;
        }
        results.displayLabel = displayLabel;
        results.number = nativeGetNumber(this.mPtr, keyCode);
        results.meta[MODIFIER_BEHAVIOR_CHORDED] = nativeGetCharacter(this.mPtr, keyCode, MODIFIER_BEHAVIOR_CHORDED);
        results.meta[NUMERIC] = nativeGetCharacter(this.mPtr, keyCode, NUMERIC);
        results.meta[PREDICTIVE] = nativeGetCharacter(this.mPtr, keyCode, PREDICTIVE);
        results.meta[ALPHA] = nativeGetCharacter(this.mPtr, keyCode, ALPHA);
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
            case PGSdk.TYPE_MUSIC /*12*/:
            case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
            case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
            case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
            case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
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
            case FULL /*4*/:
            case SPECIAL_FUNCTION /*5*/:
                return MODIFIER_BEHAVIOR_CHORDED;
            default:
                return NUMERIC;
        }
    }

    public static boolean deviceHasKey(int keyCode) {
        InputManager instance = InputManager.getInstance();
        int[] iArr = new int[NUMERIC];
        iArr[MODIFIER_BEHAVIOR_CHORDED] = keyCode;
        return instance.deviceHasKeys(iArr)[MODIFIER_BEHAVIOR_CHORDED];
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
        return MODIFIER_BEHAVIOR_CHORDED;
    }
}
