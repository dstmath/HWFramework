package android.view;

import android.annotation.UnsupportedAppUsage;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.NullVibrator;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.List;

public final class InputDevice implements Parcelable {
    public static final Parcelable.Creator<InputDevice> CREATOR = new Parcelable.Creator<InputDevice>() {
        /* class android.view.InputDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InputDevice createFromParcel(Parcel in) {
            return new InputDevice(in);
        }

        @Override // android.os.Parcelable.Creator
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
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private final boolean mIsExternal;
    private final KeyCharacterMap mKeyCharacterMap;
    private final int mKeyboardType;
    private final ArrayList<MotionRange> mMotionRanges;
    private final String mName;
    private final int mProductId;
    private final int mSources;
    private final int mVendorId;
    private Vibrator mVibrator;

    @UnsupportedAppUsage
    private InputDevice(int id, int generation, int controllerNumber, String name, int vendorId, int productId, String descriptor, boolean isExternal, int sources, int keyboardType, KeyCharacterMap keyCharacterMap, boolean hasVibrator, boolean hasMicrophone, boolean hasButtonUnderPad) {
        this.mMotionRanges = new ArrayList<>();
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
        this.mMotionRanges = new ArrayList<>();
        this.mId = in.readInt();
        this.mGeneration = in.readInt();
        this.mControllerNumber = in.readInt();
        this.mName = in.readString();
        this.mVendorId = in.readInt();
        this.mProductId = in.readInt();
        this.mDescriptor = in.readString();
        boolean z = false;
        this.mIsExternal = in.readInt() != 0;
        this.mSources = in.readInt();
        this.mKeyboardType = in.readInt();
        this.mKeyCharacterMap = KeyCharacterMap.CREATOR.createFromParcel(in);
        this.mHasVibrator = in.readInt() != 0;
        this.mHasMicrophone = in.readInt() != 0;
        this.mHasButtonUnderPad = in.readInt() != 0 ? true : z;
        this.mIdentifier = new InputDeviceIdentifier(this.mDescriptor, this.mVendorId, this.mProductId);
        int numRanges = in.readInt();
        numRanges = numRanges > 1000 ? 1000 : numRanges;
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
        return (this.mSources & 257) == 257 && this.mKeyboardType == 2;
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
            MotionRange range = this.mMotionRanges.get(i);
            if (range.mAxis == axis) {
                return range;
            }
        }
        return null;
    }

    public MotionRange getMotionRange(int axis, int source) {
        int numRanges = this.mMotionRanges.size();
        for (int i = 0; i < numRanges; i++) {
            MotionRange range = this.mMotionRanges.get(i);
            if (range.mAxis == axis && range.mSource == source) {
                return range;
            }
        }
        return null;
    }

    public List<MotionRange> getMotionRanges() {
        return this.mMotionRanges;
    }

    @UnsupportedAppUsage
    private void addMotionRange(int axis, int source, float min, float max, float flat, float fuzz, float resolution) {
        this.mMotionRanges.add(new MotionRange(axis, source, min, max, flat, fuzz, resolution));
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

    public boolean isEnabled() {
        return InputManager.getInstance().isInputDeviceEnabled(this.mId);
    }

    public void enable() {
        InputManager.getInstance().enableInputDevice(this.mId);
    }

    public void disable() {
        InputManager.getInstance().disableInputDevice(this.mId);
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

    public static final class MotionRange {
        private int mAxis;
        private float mFlat;
        private float mFuzz;
        private float mMax;
        private float mMin;
        private float mResolution;
        private int mSource;

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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mId);
        out.writeInt(this.mGeneration);
        out.writeInt(this.mControllerNumber);
        out.writeString(this.mName);
        out.writeInt(this.mVendorId);
        out.writeInt(this.mProductId);
        out.writeString(this.mDescriptor);
        out.writeInt(this.mIsExternal ? 1 : 0);
        out.writeInt(this.mSources);
        out.writeInt(this.mKeyboardType);
        this.mKeyCharacterMap.writeToParcel(out, flags);
        out.writeInt(this.mHasVibrator ? 1 : 0);
        out.writeInt(this.mHasMicrophone ? 1 : 0);
        out.writeInt(this.mHasButtonUnderPad ? 1 : 0);
        int numRanges = this.mMotionRanges.size();
        out.writeInt(numRanges);
        for (int i = 0; i < numRanges; i++) {
            MotionRange range = this.mMotionRanges.get(i);
            out.writeInt(range.mAxis);
            out.writeInt(range.mSource);
            out.writeFloat(range.mMin);
            out.writeFloat(range.mMax);
            out.writeFloat(range.mFlat);
            out.writeFloat(range.mFuzz);
            out.writeFloat(range.mResolution);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("Input Device ");
        description.append(this.mId);
        description.append(": ");
        description.append(this.mName);
        description.append("\n");
        description.append("  Descriptor: ");
        description.append(this.mDescriptor);
        description.append("\n");
        description.append("  Generation: ");
        description.append(this.mGeneration);
        description.append("\n");
        description.append("  Location: ");
        description.append(this.mIsExternal ? MediaStore.VOLUME_EXTERNAL : "built-in");
        description.append("\n");
        description.append("  Keyboard Type: ");
        int i = this.mKeyboardType;
        if (i == 0) {
            description.append("none");
        } else if (i == 1) {
            description.append("non-alphabetic");
        } else if (i == 2) {
            description.append("alphabetic");
        }
        description.append("\n");
        description.append("  Has Vibrator: ");
        description.append(this.mHasVibrator);
        description.append("\n");
        description.append("  Has mic: ");
        description.append(this.mHasMicrophone);
        description.append("\n");
        description.append("  Sources: 0x");
        description.append(Integer.toHexString(this.mSources));
        description.append(" (");
        appendSourceDescriptionIfApplicable(description, 257, "keyboard");
        appendSourceDescriptionIfApplicable(description, 513, "dpad");
        appendSourceDescriptionIfApplicable(description, 4098, "touchscreen");
        appendSourceDescriptionIfApplicable(description, 8194, "mouse");
        appendSourceDescriptionIfApplicable(description, 16386, "stylus");
        appendSourceDescriptionIfApplicable(description, 65540, "trackball");
        appendSourceDescriptionIfApplicable(description, SOURCE_MOUSE_RELATIVE, "mouse_relative");
        appendSourceDescriptionIfApplicable(description, SOURCE_TOUCHPAD, "touchpad");
        appendSourceDescriptionIfApplicable(description, SOURCE_JOYSTICK, "joystick");
        appendSourceDescriptionIfApplicable(description, 1025, "gamepad");
        description.append(" )\n");
        int numAxes = this.mMotionRanges.size();
        for (int i2 = 0; i2 < numAxes; i2++) {
            MotionRange range = this.mMotionRanges.get(i2);
            description.append("    ");
            description.append(MotionEvent.axisToString(range.mAxis));
            description.append(": source=0x");
            description.append(Integer.toHexString(range.mSource));
            description.append(" min=");
            description.append(range.mMin);
            description.append(" max=");
            description.append(range.mMax);
            description.append(" flat=");
            description.append(range.mFlat);
            description.append(" fuzz=");
            description.append(range.mFuzz);
            description.append(" resolution=");
            description.append(range.mResolution);
            description.append("\n");
        }
        return description.toString();
    }

    private void appendSourceDescriptionIfApplicable(StringBuilder description, int source, String sourceName) {
        if ((this.mSources & source) == source) {
            description.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            description.append(sourceName);
        }
    }
}
