package android.view;

import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.os.NullVibrator;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Vibrator;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.os.PowerProfile;
import java.util.ArrayList;
import java.util.List;

public final class InputDevice implements Parcelable {
    public static final Creator<InputDevice> CREATOR = new Creator<InputDevice>() {
        public InputDevice createFromParcel(Parcel in) {
            return new InputDevice(in, null);
        }

        public InputDevice[] newArray(int size) {
            return new InputDevice[size];
        }
    };
    public static final int KEYBOARD_TYPE_ALPHABETIC = 2;
    public static final int KEYBOARD_TYPE_NONE = 0;
    public static final int KEYBOARD_TYPE_NON_ALPHABETIC = 1;
    private static final int MAX_RANGES = 1000;
    @Deprecated
    public static final int MOTION_RANGE_ORIENTATION = 8;
    @Deprecated
    public static final int MOTION_RANGE_PRESSURE = 2;
    @Deprecated
    public static final int MOTION_RANGE_SIZE = 3;
    @Deprecated
    public static final int MOTION_RANGE_TOOL_MAJOR = 6;
    @Deprecated
    public static final int MOTION_RANGE_TOOL_MINOR = 7;
    @Deprecated
    public static final int MOTION_RANGE_TOUCH_MAJOR = 4;
    @Deprecated
    public static final int MOTION_RANGE_TOUCH_MINOR = 5;
    @Deprecated
    public static final int MOTION_RANGE_X = 0;
    @Deprecated
    public static final int MOTION_RANGE_Y = 1;
    public static final int SOURCE_ANY = -256;
    public static final int SOURCE_BLUETOOTH_STYLUS = 49154;
    public static final int SOURCE_CLASS_BUTTON = 1;
    public static final int SOURCE_CLASS_JOYSTICK = 16;
    public static final int SOURCE_CLASS_MASK = 255;
    public static final int SOURCE_CLASS_NONE = 0;
    public static final int SOURCE_CLASS_POINTER = 2;
    public static final int SOURCE_CLASS_POSITION = 8;
    public static final int SOURCE_CLASS_TRACKBALL = 4;
    public static final int SOURCE_DPAD = 513;
    public static final int SOURCE_GAMEPAD = 1025;
    public static final int SOURCE_HDMI = 33554433;
    public static final int SOURCE_JOYSTICK = 16777232;
    public static final int SOURCE_KEYBOARD = 257;
    public static final int SOURCE_MOUSE = 8194;
    public static final int SOURCE_MOUSE_RELATIVE = 131076;
    public static final int SOURCE_ROTARY_ENCODER = 4194304;
    public static final int SOURCE_STYLUS = 16386;
    public static final int SOURCE_TOUCHPAD = 1048584;
    public static final int SOURCE_TOUCHSCREEN = 4098;
    public static final int SOURCE_TOUCH_NAVIGATION = 2097152;
    public static final int SOURCE_TRACKBALL = 65540;
    public static final int SOURCE_UNKNOWN = 0;
    private final int mControllerNumber;
    private final String mDescriptor;
    private final int mGeneration;
    private final boolean mHasButtonUnderPad;
    private final boolean mHasMicrophone;
    private final boolean mHasVibrator;
    private final int mId;
    private final InputDeviceIdentifier mIdentifier;
    private final boolean mIsExternal;
    private final KeyCharacterMap mKeyCharacterMap;
    private final int mKeyboardType;
    private final ArrayList<MotionRange> mMotionRanges;
    private final String mName;
    private final int mProductId;
    private final int mSources;
    private final int mVendorId;
    private Vibrator mVibrator;

    public static final class MotionRange {
        private int mAxis;
        private float mFlat;
        private float mFuzz;
        private float mMax;
        private float mMin;
        private float mResolution;
        private int mSource;

        /* synthetic */ MotionRange(int axis, int source, float min, float max, float flat, float fuzz, float resolution, MotionRange -this7) {
            this(axis, source, min, max, flat, fuzz, resolution);
        }

        private MotionRange(int axis, int source, float min, float max, float flat, float fuzz, float resolution) {
            this.mAxis = axis;
            this.mSource = source;
            this.mMin = min;
            this.mMax = max;
            this.mFlat = flat;
            this.mFuzz = fuzz;
            this.mResolution = resolution;
        }

        public int getAxis() {
            return this.mAxis;
        }

        public int getSource() {
            return this.mSource;
        }

        public boolean isFromSource(int source) {
            return (getSource() & source) == source;
        }

        public float getMin() {
            return this.mMin;
        }

        public float getMax() {
            return this.mMax;
        }

        public float getRange() {
            return this.mMax - this.mMin;
        }

        public float getFlat() {
            return this.mFlat;
        }

        public float getFuzz() {
            return this.mFuzz;
        }

        public float getResolution() {
            return this.mResolution;
        }
    }

    /* synthetic */ InputDevice(Parcel in, InputDevice -this1) {
        this(in);
    }

    private InputDevice(int id, int generation, int controllerNumber, String name, int vendorId, int productId, String descriptor, boolean isExternal, int sources, int keyboardType, KeyCharacterMap keyCharacterMap, boolean hasVibrator, boolean hasMicrophone, boolean hasButtonUnderPad) {
        this.mMotionRanges = new ArrayList();
        this.mId = id;
        this.mGeneration = generation;
        this.mControllerNumber = controllerNumber;
        this.mName = name;
        this.mVendorId = vendorId;
        this.mProductId = productId;
        this.mDescriptor = descriptor;
        this.mIsExternal = isExternal;
        this.mSources = sources;
        this.mKeyboardType = keyboardType;
        this.mKeyCharacterMap = keyCharacterMap;
        this.mHasVibrator = hasVibrator;
        this.mHasMicrophone = hasMicrophone;
        this.mHasButtonUnderPad = hasButtonUnderPad;
        this.mIdentifier = new InputDeviceIdentifier(descriptor, vendorId, productId);
    }

    private InputDevice(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mMotionRanges = new ArrayList();
        this.mId = in.readInt();
        this.mGeneration = in.readInt();
        this.mControllerNumber = in.readInt();
        this.mName = in.readString();
        this.mVendorId = in.readInt();
        this.mProductId = in.readInt();
        this.mDescriptor = in.readString();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsExternal = z;
        this.mSources = in.readInt();
        this.mKeyboardType = in.readInt();
        this.mKeyCharacterMap = (KeyCharacterMap) KeyCharacterMap.CREATOR.createFromParcel(in);
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasVibrator = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasMicrophone = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.mHasButtonUnderPad = z2;
        this.mIdentifier = new InputDeviceIdentifier(this.mDescriptor, this.mVendorId, this.mProductId);
        int numRanges = in.readInt();
        if (numRanges > 1000) {
            numRanges = 1000;
        }
        for (int i = 0; i < numRanges; i++) {
            addMotionRange(in.readInt(), in.readInt(), in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
        }
    }

    public static InputDevice getDevice(int id) {
        return InputManager.getInstance().getInputDevice(id);
    }

    public static int[] getDeviceIds() {
        return InputManager.getInstance().getInputDeviceIds();
    }

    public int getId() {
        return this.mId;
    }

    public int getControllerNumber() {
        return this.mControllerNumber;
    }

    public InputDeviceIdentifier getIdentifier() {
        return this.mIdentifier;
    }

    public int getGeneration() {
        return this.mGeneration;
    }

    public int getVendorId() {
        return this.mVendorId;
    }

    public int getProductId() {
        return this.mProductId;
    }

    public String getDescriptor() {
        return this.mDescriptor;
    }

    public boolean isVirtual() {
        return this.mId < 0;
    }

    public boolean isExternal() {
        return this.mIsExternal;
    }

    public boolean isFullKeyboard() {
        if ((this.mSources & 257) == 257 && this.mKeyboardType == 2) {
            return true;
        }
        return false;
    }

    public String getName() {
        return this.mName;
    }

    public int getSources() {
        return this.mSources;
    }

    public boolean supportsSource(int source) {
        return (this.mSources & source) == source;
    }

    public int getKeyboardType() {
        return this.mKeyboardType;
    }

    public KeyCharacterMap getKeyCharacterMap() {
        return this.mKeyCharacterMap;
    }

    public boolean[] hasKeys(int... keys) {
        return InputManager.getInstance().deviceHasKeys(this.mId, keys);
    }

    public MotionRange getMotionRange(int axis) {
        int numRanges = this.mMotionRanges.size();
        for (int i = 0; i < numRanges; i++) {
            MotionRange range = (MotionRange) this.mMotionRanges.get(i);
            if (range.mAxis == axis) {
                return range;
            }
        }
        return null;
    }

    public MotionRange getMotionRange(int axis, int source) {
        int numRanges = this.mMotionRanges.size();
        for (int i = 0; i < numRanges; i++) {
            MotionRange range = (MotionRange) this.mMotionRanges.get(i);
            if (range.mAxis == axis && range.mSource == source) {
                return range;
            }
        }
        return null;
    }

    public List<MotionRange> getMotionRanges() {
        return this.mMotionRanges;
    }

    private void addMotionRange(int axis, int source, float min, float max, float flat, float fuzz, float resolution) {
        this.mMotionRanges.add(new MotionRange(axis, source, min, max, flat, fuzz, resolution, null));
    }

    public Vibrator getVibrator() {
        Vibrator vibrator;
        synchronized (this.mMotionRanges) {
            if (this.mVibrator == null) {
                if (this.mHasVibrator) {
                    this.mVibrator = InputManager.getInstance().getInputDeviceVibrator(this.mId);
                } else {
                    this.mVibrator = NullVibrator.getInstance();
                }
            }
            vibrator = this.mVibrator;
        }
        return vibrator;
    }

    public boolean hasMicrophone() {
        return this.mHasMicrophone;
    }

    public boolean hasButtonUnderPad() {
        return this.mHasButtonUnderPad;
    }

    public void setPointerType(int pointerType) {
        InputManager.getInstance().setPointerIconType(pointerType);
    }

    public void setCustomPointerIcon(PointerIcon icon) {
        InputManager.getInstance().setCustomPointerIcon(icon);
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeInt(this.mId);
        out.writeInt(this.mGeneration);
        out.writeInt(this.mControllerNumber);
        out.writeString(this.mName);
        out.writeInt(this.mVendorId);
        out.writeInt(this.mProductId);
        out.writeString(this.mDescriptor);
        if (this.mIsExternal) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeInt(this.mSources);
        out.writeInt(this.mKeyboardType);
        this.mKeyCharacterMap.writeToParcel(out, flags);
        if (this.mHasVibrator) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (this.mHasMicrophone) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.mHasButtonUnderPad) {
            i2 = 0;
        }
        out.writeInt(i2);
        int numRanges = this.mMotionRanges.size();
        out.writeInt(numRanges);
        for (int i3 = 0; i3 < numRanges; i3++) {
            MotionRange range = (MotionRange) this.mMotionRanges.get(i3);
            out.writeInt(range.mAxis);
            out.writeInt(range.mSource);
            out.writeFloat(range.mMin);
            out.writeFloat(range.mMax);
            out.writeFloat(range.mFlat);
            out.writeFloat(range.mFuzz);
            out.writeFloat(range.mResolution);
        }
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("Input Device ").append(this.mId).append(": ").append(this.mName).append("\n");
        description.append("  Descriptor: ").append(this.mDescriptor).append("\n");
        description.append("  Generation: ").append(this.mGeneration).append("\n");
        description.append("  Location: ").append(this.mIsExternal ? "external" : "built-in").append("\n");
        description.append("  Keyboard Type: ");
        switch (this.mKeyboardType) {
            case 0:
                description.append(PowerProfile.POWER_NONE);
                break;
            case 1:
                description.append("non-alphabetic");
                break;
            case 2:
                description.append("alphabetic");
                break;
        }
        description.append("\n");
        description.append("  Has Vibrator: ").append(this.mHasVibrator).append("\n");
        description.append("  Has mic: ").append(this.mHasMicrophone).append("\n");
        description.append("  Sources: 0x").append(Integer.toHexString(this.mSources)).append(" (");
        appendSourceDescriptionIfApplicable(description, 257, InputMethodUtils.SUBTYPE_MODE_KEYBOARD);
        appendSourceDescriptionIfApplicable(description, 513, "dpad");
        appendSourceDescriptionIfApplicable(description, 4098, "touchscreen");
        appendSourceDescriptionIfApplicable(description, SOURCE_MOUSE, "mouse");
        appendSourceDescriptionIfApplicable(description, 16386, "stylus");
        appendSourceDescriptionIfApplicable(description, SOURCE_TRACKBALL, "trackball");
        appendSourceDescriptionIfApplicable(description, SOURCE_MOUSE_RELATIVE, "mouse_relative");
        appendSourceDescriptionIfApplicable(description, SOURCE_TOUCHPAD, "touchpad");
        appendSourceDescriptionIfApplicable(description, SOURCE_JOYSTICK, "joystick");
        appendSourceDescriptionIfApplicable(description, 1025, "gamepad");
        description.append(" )\n");
        int numAxes = this.mMotionRanges.size();
        for (int i = 0; i < numAxes; i++) {
            MotionRange range = (MotionRange) this.mMotionRanges.get(i);
            description.append("    ").append(MotionEvent.axisToString(range.mAxis));
            description.append(": source=0x").append(Integer.toHexString(range.mSource));
            description.append(" min=").append(range.mMin);
            description.append(" max=").append(range.mMax);
            description.append(" flat=").append(range.mFlat);
            description.append(" fuzz=").append(range.mFuzz);
            description.append(" resolution=").append(range.mResolution);
            description.append("\n");
        }
        return description.toString();
    }

    private void appendSourceDescriptionIfApplicable(StringBuilder description, int source, String sourceName) {
        if ((this.mSources & source) == source) {
            description.append(" ");
            description.append(sourceName);
        }
    }
}
