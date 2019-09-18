package android.view;

import android.graphics.Matrix;
import android.graphics.Point;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public final class MotionEvent extends InputEvent implements Parcelable {
    public static final int ACTION_BUTTON_PRESS = 11;
    public static final int ACTION_BUTTON_RELEASE = 12;
    public static final int ACTION_CANCEL = 3;
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_HOVER_ENTER = 9;
    public static final int ACTION_HOVER_EXIT = 10;
    public static final int ACTION_HOVER_MOVE = 7;
    public static final int ACTION_MASK = 255;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_OUTSIDE = 4;
    @Deprecated
    public static final int ACTION_POINTER_1_DOWN = 5;
    @Deprecated
    public static final int ACTION_POINTER_1_UP = 6;
    @Deprecated
    public static final int ACTION_POINTER_2_DOWN = 261;
    @Deprecated
    public static final int ACTION_POINTER_2_UP = 262;
    @Deprecated
    public static final int ACTION_POINTER_3_DOWN = 517;
    @Deprecated
    public static final int ACTION_POINTER_3_UP = 518;
    public static final int ACTION_POINTER_DOWN = 5;
    @Deprecated
    public static final int ACTION_POINTER_ID_MASK = 65280;
    @Deprecated
    public static final int ACTION_POINTER_ID_SHIFT = 8;
    public static final int ACTION_POINTER_INDEX_MASK = 65280;
    public static final int ACTION_POINTER_INDEX_SHIFT = 8;
    public static final int ACTION_POINTER_UP = 6;
    public static final int ACTION_SCROLL = 8;
    public static final int ACTION_UP = 1;
    public static final int AXIS_BRAKE = 23;
    public static final int AXIS_DISTANCE = 24;
    public static final int AXIS_GAS = 22;
    public static final int AXIS_GENERIC_1 = 32;
    public static final int AXIS_GENERIC_10 = 41;
    public static final int AXIS_GENERIC_11 = 42;
    public static final int AXIS_GENERIC_12 = 43;
    public static final int AXIS_GENERIC_13 = 44;
    public static final int AXIS_GENERIC_14 = 45;
    public static final int AXIS_GENERIC_15 = 46;
    public static final int AXIS_GENERIC_16 = 47;
    public static final int AXIS_GENERIC_2 = 33;
    public static final int AXIS_GENERIC_3 = 34;
    public static final int AXIS_GENERIC_4 = 35;
    public static final int AXIS_GENERIC_5 = 36;
    public static final int AXIS_GENERIC_6 = 37;
    public static final int AXIS_GENERIC_7 = 38;
    public static final int AXIS_GENERIC_8 = 39;
    public static final int AXIS_GENERIC_9 = 40;
    public static final int AXIS_HAT_X = 15;
    public static final int AXIS_HAT_Y = 16;
    public static final int AXIS_HSCROLL = 10;
    public static final int AXIS_LTRIGGER = 17;
    public static final int AXIS_ORIENTATION = 8;
    public static final int AXIS_PRESSURE = 2;
    public static final int AXIS_RELATIVE_X = 27;
    public static final int AXIS_RELATIVE_Y = 28;
    public static final int AXIS_RTRIGGER = 18;
    public static final int AXIS_RUDDER = 20;
    public static final int AXIS_RX = 12;
    public static final int AXIS_RY = 13;
    public static final int AXIS_RZ = 14;
    public static final int AXIS_SCROLL = 26;
    public static final int AXIS_SIZE = 3;
    private static final SparseArray<String> AXIS_SYMBOLIC_NAMES = new SparseArray<>();
    public static final int AXIS_THROTTLE = 19;
    public static final int AXIS_TILT = 25;
    public static final int AXIS_TOOL_MAJOR = 6;
    public static final int AXIS_TOOL_MINOR = 7;
    public static final int AXIS_TOUCH_MAJOR = 4;
    public static final int AXIS_TOUCH_MINOR = 5;
    public static final int AXIS_VSCROLL = 9;
    public static final int AXIS_WHEEL = 21;
    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 11;
    public static final int BUTTON_BACK = 8;
    public static final int BUTTON_FORWARD = 16;
    public static final int BUTTON_PRIMARY = 1;
    public static final int BUTTON_SECONDARY = 2;
    public static final int BUTTON_STYLUS_PRIMARY = 32;
    public static final int BUTTON_STYLUS_SECONDARY = 64;
    private static final String[] BUTTON_SYMBOLIC_NAMES = {"BUTTON_PRIMARY", "BUTTON_SECONDARY", "BUTTON_TERTIARY", "BUTTON_BACK", "BUTTON_FORWARD", "BUTTON_STYLUS_PRIMARY", "BUTTON_STYLUS_SECONDARY", "0x00000080", "0x00000100", "0x00000200", "0x00000400", "0x00000800", "0x00001000", "0x00002000", "0x00004000", "0x00008000", "0x00010000", "0x00020000", "0x00040000", "0x00080000", "0x00100000", "0x00200000", "0x00400000", "0x00800000", "0x01000000", "0x02000000", "0x04000000", "0x08000000", "0x10000000", "0x20000000", "0x40000000", "0x80000000"};
    public static final int BUTTON_TERTIARY = 4;
    public static final Parcelable.Creator<MotionEvent> CREATOR = new Parcelable.Creator<MotionEvent>() {
        public MotionEvent createFromParcel(Parcel in) {
            in.readInt();
            return MotionEvent.createFromParcelBody(in);
        }

        public MotionEvent[] newArray(int size) {
            return new MotionEvent[size];
        }
    };
    private static final boolean DEBUG_CONCISE_TOSTRING = false;
    public static final int EDGE_BOTTOM = 2;
    public static final int EDGE_LEFT = 4;
    public static final int EDGE_RIGHT = 8;
    public static final int EDGE_TOP = 1;
    public static final int FLAG_HOVER_EXIT_PENDING = 4;
    public static final int FLAG_IS_GENERATED_GESTURE = 8;
    public static final int FLAG_TAINTED = Integer.MIN_VALUE;
    public static final int FLAG_TARGET_ACCESSIBILITY_FOCUS = 1073741824;
    public static final int FLAG_WINDOW_IS_EXCLUDE_TRANSFER_EVENT = 65536;
    public static final int FLAG_WINDOW_IS_OBSCURED = 1;
    public static final int FLAG_WINDOW_IS_PARTIALLY_OBSCURED = 2;
    private static final int HISTORY_CURRENT = Integer.MIN_VALUE;
    public static final int INVALID_POINTER_ID = -1;
    private static final String LABEL_PREFIX = "AXIS_";
    private static final int MAX_RECYCLED = 10;
    private static final long NS_PER_MS = 1000000;
    public static final int TOOL_TYPE_ERASER = 4;
    public static final int TOOL_TYPE_FINGER = 1;
    public static final int TOOL_TYPE_MOUSE = 3;
    public static final int TOOL_TYPE_STYLUS = 2;
    private static final SparseArray<String> TOOL_TYPE_SYMBOLIC_NAMES = new SparseArray<>();
    public static final int TOOL_TYPE_UNKNOWN = 0;
    private static final Object gRecyclerLock = new Object();
    private static MotionEvent gRecyclerTop;
    private static int gRecyclerUsed;
    private static final Object gSharedTempLock = new Object();
    private static PointerCoords[] gSharedTempPointerCoords;
    private static int[] gSharedTempPointerIndexMap;
    private static PointerProperties[] gSharedTempPointerProperties;
    private long mNativePtr;
    private MotionEvent mNext;
    private Point mOffset;

    public static final class PointerCoords {
        private static final int INITIAL_PACKED_AXIS_VALUES = 8;
        private long mPackedAxisBits;
        private float[] mPackedAxisValues;
        public float orientation;
        public float pressure;
        public float size;
        public float toolMajor;
        public float toolMinor;
        public float touchMajor;
        public float touchMinor;
        public float x;
        public float y;

        public PointerCoords() {
        }

        public PointerCoords(PointerCoords other) {
            copyFrom(other);
        }

        public static PointerCoords[] createArray(int size2) {
            PointerCoords[] array = new PointerCoords[size2];
            for (int i = 0; i < size2; i++) {
                array[i] = new PointerCoords();
            }
            return array;
        }

        public void clear() {
            this.mPackedAxisBits = 0;
            this.x = 0.0f;
            this.y = 0.0f;
            this.pressure = 0.0f;
            this.size = 0.0f;
            this.touchMajor = 0.0f;
            this.touchMinor = 0.0f;
            this.toolMajor = 0.0f;
            this.toolMinor = 0.0f;
            this.orientation = 0.0f;
        }

        public void copyFrom(PointerCoords other) {
            long bits = other.mPackedAxisBits;
            this.mPackedAxisBits = bits;
            if (bits != 0) {
                float[] otherValues = other.mPackedAxisValues;
                int count = Long.bitCount(bits);
                float[] values = this.mPackedAxisValues;
                if (values == null || count > values.length) {
                    values = new float[otherValues.length];
                    this.mPackedAxisValues = values;
                }
                System.arraycopy(otherValues, 0, values, 0, count);
            }
            this.x = other.x;
            this.y = other.y;
            this.pressure = other.pressure;
            this.size = other.size;
            this.touchMajor = other.touchMajor;
            this.touchMinor = other.touchMinor;
            this.toolMajor = other.toolMajor;
            this.toolMinor = other.toolMinor;
            this.orientation = other.orientation;
        }

        public float getAxisValue(int axis) {
            switch (axis) {
                case 0:
                    return this.x;
                case 1:
                    return this.y;
                case 2:
                    return this.pressure;
                case 3:
                    return this.size;
                case 4:
                    return this.touchMajor;
                case 5:
                    return this.touchMinor;
                case 6:
                    return this.toolMajor;
                case 7:
                    return this.toolMinor;
                case 8:
                    return this.orientation;
                default:
                    if (axis < 0 || axis > 63) {
                        throw new IllegalArgumentException("Axis out of range.");
                    }
                    long bits = this.mPackedAxisBits;
                    if ((bits & (-9223372036854775808 >>> axis)) == 0) {
                        return 0.0f;
                    }
                    return this.mPackedAxisValues[Long.bitCount((~(-1 >>> axis)) & bits)];
            }
        }

        public void setAxisValue(int axis, float value) {
            switch (axis) {
                case 0:
                    this.x = value;
                    return;
                case 1:
                    this.y = value;
                    return;
                case 2:
                    this.pressure = value;
                    return;
                case 3:
                    this.size = value;
                    return;
                case 4:
                    this.touchMajor = value;
                    return;
                case 5:
                    this.touchMinor = value;
                    return;
                case 6:
                    this.toolMajor = value;
                    return;
                case 7:
                    this.toolMinor = value;
                    return;
                case 8:
                    this.orientation = value;
                    return;
                default:
                    if (axis < 0 || axis > 63) {
                        throw new IllegalArgumentException("Axis out of range.");
                    }
                    long bits = this.mPackedAxisBits;
                    long axisBit = -9223372036854775808 >>> axis;
                    int index = Long.bitCount((~(-1 >>> axis)) & bits);
                    float[] values = this.mPackedAxisValues;
                    if ((bits & axisBit) == 0) {
                        if (values == null) {
                            values = new float[8];
                            this.mPackedAxisValues = values;
                        } else {
                            int count = Long.bitCount(bits);
                            if (count >= values.length) {
                                float[] newValues = new float[(count * 2)];
                                System.arraycopy(values, 0, newValues, 0, index);
                                System.arraycopy(values, index, newValues, index + 1, count - index);
                                values = newValues;
                                this.mPackedAxisValues = values;
                            } else if (index != count) {
                                System.arraycopy(values, index, values, index + 1, count - index);
                            }
                        }
                        this.mPackedAxisBits = bits | axisBit;
                    }
                    values[index] = value;
                    return;
            }
        }
    }

    public static final class PointerProperties {
        public int id;
        public int toolType;

        public PointerProperties() {
            clear();
        }

        public PointerProperties(PointerProperties other) {
            copyFrom(other);
        }

        public static PointerProperties[] createArray(int size) {
            PointerProperties[] array = new PointerProperties[size];
            for (int i = 0; i < size; i++) {
                array[i] = new PointerProperties();
            }
            return array;
        }

        public void clear() {
            this.id = -1;
            this.toolType = 0;
        }

        public void copyFrom(PointerProperties other) {
            this.id = other.id;
            this.toolType = other.toolType;
        }

        public boolean equals(Object other) {
            if (other instanceof PointerProperties) {
                return equals((PointerProperties) other);
            }
            return false;
        }

        /* access modifiers changed from: private */
        public boolean equals(PointerProperties other) {
            return other != null && this.id == other.id && this.toolType == other.toolType;
        }

        public int hashCode() {
            return this.id | (this.toolType << 8);
        }
    }

    private static native void nativeAddBatch(long j, long j2, PointerCoords[] pointerCoordsArr, int i);

    private static native int nativeAxisFromString(String str);

    private static native String nativeAxisToString(int i);

    private static native long nativeCopy(long j, long j2, boolean z);

    private static native void nativeDispose(long j);

    private static native int nativeFindPointerIndex(long j, int i);

    private static native int nativeGetAction(long j);

    private static native int nativeGetActionButton(long j);

    private static native float nativeGetAxisValue(long j, int i, int i2, int i3);

    private static native int nativeGetButtonState(long j);

    private static native int nativeGetDeviceId(long j);

    private static native long nativeGetDownTimeNanos(long j);

    private static native int nativeGetEdgeFlags(long j);

    private static native long nativeGetEventTimeNanos(long j, int i);

    private static native int nativeGetFlags(long j);

    private static native int nativeGetHistorySize(long j);

    private static native int nativeGetMetaState(long j);

    private static native void nativeGetPointerCoords(long j, int i, int i2, PointerCoords pointerCoords);

    private static native int nativeGetPointerCount(long j);

    private static native int nativeGetPointerId(long j, int i);

    private static native void nativeGetPointerProperties(long j, int i, PointerProperties pointerProperties);

    private static native float nativeGetRawAxisValue(long j, int i, int i2, int i3);

    private static native int nativeGetSource(long j);

    private static native int nativeGetToolType(long j, int i);

    private static native float nativeGetXOffset(long j);

    private static native float nativeGetXPrecision(long j);

    private static native float nativeGetYOffset(long j);

    private static native float nativeGetYPrecision(long j);

    private static native long nativeInitialize(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, float f, float f2, float f3, float f4, long j2, long j3, int i8, PointerProperties[] pointerPropertiesArr, PointerCoords[] pointerCoordsArr);

    private static native boolean nativeIsTouchEvent(long j);

    private static native void nativeOffsetLocation(long j, float f, float f2);

    private static native long nativeReadFromParcel(long j, Parcel parcel);

    private static native void nativeScale(long j, float f);

    private static native void nativeSetAction(long j, int i);

    private static native void nativeSetActionButton(long j, int i);

    private static native void nativeSetButtonState(long j, int i);

    private static native void nativeSetDownTimeNanos(long j, long j2);

    private static native void nativeSetEdgeFlags(long j, int i);

    private static native void nativeSetFlags(long j, int i);

    private static native int nativeSetSource(long j, int i);

    private static native void nativeTransform(long j, long j2);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    static {
        SparseArray<String> names = AXIS_SYMBOLIC_NAMES;
        names.append(0, "AXIS_X");
        names.append(1, "AXIS_Y");
        names.append(2, "AXIS_PRESSURE");
        names.append(3, "AXIS_SIZE");
        names.append(4, "AXIS_TOUCH_MAJOR");
        names.append(5, "AXIS_TOUCH_MINOR");
        names.append(6, "AXIS_TOOL_MAJOR");
        names.append(7, "AXIS_TOOL_MINOR");
        names.append(8, "AXIS_ORIENTATION");
        names.append(9, "AXIS_VSCROLL");
        names.append(10, "AXIS_HSCROLL");
        names.append(11, "AXIS_Z");
        names.append(12, "AXIS_RX");
        names.append(13, "AXIS_RY");
        names.append(14, "AXIS_RZ");
        names.append(15, "AXIS_HAT_X");
        names.append(16, "AXIS_HAT_Y");
        names.append(17, "AXIS_LTRIGGER");
        names.append(18, "AXIS_RTRIGGER");
        names.append(19, "AXIS_THROTTLE");
        names.append(20, "AXIS_RUDDER");
        names.append(21, "AXIS_WHEEL");
        names.append(22, "AXIS_GAS");
        names.append(23, "AXIS_BRAKE");
        names.append(24, "AXIS_DISTANCE");
        names.append(25, "AXIS_TILT");
        names.append(26, "AXIS_SCROLL");
        names.append(27, "AXIS_REALTIVE_X");
        names.append(28, "AXIS_REALTIVE_Y");
        names.append(32, "AXIS_GENERIC_1");
        names.append(33, "AXIS_GENERIC_2");
        names.append(34, "AXIS_GENERIC_3");
        names.append(35, "AXIS_GENERIC_4");
        names.append(36, "AXIS_GENERIC_5");
        names.append(37, "AXIS_GENERIC_6");
        names.append(38, "AXIS_GENERIC_7");
        names.append(39, "AXIS_GENERIC_8");
        names.append(40, "AXIS_GENERIC_9");
        names.append(41, "AXIS_GENERIC_10");
        names.append(42, "AXIS_GENERIC_11");
        names.append(43, "AXIS_GENERIC_12");
        names.append(44, "AXIS_GENERIC_13");
        names.append(45, "AXIS_GENERIC_14");
        names.append(46, "AXIS_GENERIC_15");
        names.append(47, "AXIS_GENERIC_16");
        SparseArray<String> names2 = TOOL_TYPE_SYMBOLIC_NAMES;
        names2.append(0, "TOOL_TYPE_UNKNOWN");
        names2.append(1, "TOOL_TYPE_FINGER");
        names2.append(2, "TOOL_TYPE_STYLUS");
        names2.append(3, "TOOL_TYPE_MOUSE");
        names2.append(4, "TOOL_TYPE_ERASER");
    }

    private static final void ensureSharedTempPointerCapacity(int desiredCapacity) {
        if (gSharedTempPointerCoords == null || gSharedTempPointerCoords.length < desiredCapacity) {
            int capacity = gSharedTempPointerCoords != null ? gSharedTempPointerCoords.length : 8;
            while (capacity < desiredCapacity) {
                capacity *= 2;
            }
            gSharedTempPointerCoords = PointerCoords.createArray(capacity);
            gSharedTempPointerProperties = PointerProperties.createArray(capacity);
            gSharedTempPointerIndexMap = new int[capacity];
        }
    }

    private MotionEvent() {
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mNativePtr != 0) {
                nativeDispose(this.mNativePtr);
                this.mNativePtr = 0;
            }
        } finally {
            super.finalize();
        }
    }

    private static MotionEvent obtain() {
        synchronized (gRecyclerLock) {
            MotionEvent ev = gRecyclerTop;
            if (ev == null) {
                MotionEvent motionEvent = new MotionEvent();
                return motionEvent;
            }
            gRecyclerTop = ev.mNext;
            gRecyclerUsed--;
            ev.mNext = null;
            ev.prepareForReuse();
            return ev;
        }
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action, int pointerCount, PointerProperties[] pointerProperties, PointerCoords[] pointerCoords, int metaState, int buttonState, float xPrecision, float yPrecision, int deviceId, int edgeFlags, int source, int flags) {
        MotionEvent ev = obtain();
        ev.mNativePtr = nativeInitialize(ev.mNativePtr, deviceId, source, action, flags, edgeFlags, metaState, buttonState, 0.0f, 0.0f, xPrecision, yPrecision, downTime * 1000000, eventTime * 1000000, pointerCount, pointerProperties, pointerCoords);
        return ev;
    }

    @Deprecated
    public static MotionEvent obtain(long downTime, long eventTime, int action, int pointerCount, int[] pointerIds, PointerCoords[] pointerCoords, int metaState, float xPrecision, float yPrecision, int deviceId, int edgeFlags, int source, int flags) {
        MotionEvent obtain;
        synchronized (gSharedTempLock) {
            ensureSharedTempPointerCapacity(pointerCount);
            PointerProperties[] pp = gSharedTempPointerProperties;
            int i = 0;
            while (true) {
                int i2 = pointerCount;
                if (i < i2) {
                    pp[i].clear();
                    pp[i].id = pointerIds[i];
                    i++;
                } else {
                    obtain = obtain(downTime, eventTime, action, i2, pp, pointerCoords, metaState, 0, xPrecision, yPrecision, deviceId, edgeFlags, source, flags);
                }
            }
        }
        return obtain;
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, float pressure, float size, int metaState, float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        MotionEvent ev = obtain();
        synchronized (gSharedTempLock) {
            ensureSharedTempPointerCapacity(1);
            PointerProperties[] pp = gSharedTempPointerProperties;
            pp[0].clear();
            pp[0].id = 0;
            PointerCoords[] pc = gSharedTempPointerCoords;
            pc[0].clear();
            pc[0].x = x;
            pc[0].y = y;
            pc[0].pressure = pressure;
            pc[0].size = size;
            ev.mNativePtr = nativeInitialize(ev.mNativePtr, deviceId, 0, action, 0, edgeFlags, metaState, 0, 0.0f, 0.0f, xPrecision, yPrecision, downTime * 1000000, eventTime * 1000000, 1, pp, pc);
        }
        return ev;
    }

    @Deprecated
    public static MotionEvent obtain(long downTime, long eventTime, int action, int pointerCount, float x, float y, float pressure, float size, int metaState, float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        return obtain(downTime, eventTime, action, x, y, pressure, size, metaState, xPrecision, yPrecision, deviceId, edgeFlags);
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        return obtain(downTime, eventTime, action, x, y, 1.0f, 1.0f, metaState, 1.0f, 1.0f, 0, 0);
    }

    public static MotionEvent obtain(MotionEvent other) {
        if (other != null) {
            MotionEvent ev = obtain();
            ev.mNativePtr = nativeCopy(ev.mNativePtr, other.mNativePtr, true);
            return ev;
        }
        throw new IllegalArgumentException("other motion event must not be null");
    }

    public static MotionEvent obtainNoHistory(MotionEvent other) {
        if (other != null) {
            MotionEvent ev = obtain();
            ev.mNativePtr = nativeCopy(ev.mNativePtr, other.mNativePtr, false);
            return ev;
        }
        throw new IllegalArgumentException("other motion event must not be null");
    }

    public MotionEvent copy() {
        return obtain(this);
    }

    public final void recycle() {
        super.recycle();
        synchronized (gRecyclerLock) {
            if (gRecyclerUsed < 10) {
                gRecyclerUsed++;
                this.mNext = gRecyclerTop;
                gRecyclerTop = this;
            }
        }
    }

    public final void scale(float scale) {
        if (scale != 1.0f) {
            nativeScale(this.mNativePtr, scale);
        }
    }

    public final int getDeviceId() {
        return nativeGetDeviceId(this.mNativePtr);
    }

    public final int getSource() {
        return nativeGetSource(this.mNativePtr);
    }

    public final void setSource(int source) {
        nativeSetSource(this.mNativePtr, source);
    }

    public final int getAction() {
        return nativeGetAction(this.mNativePtr);
    }

    public final int getActionMasked() {
        return nativeGetAction(this.mNativePtr) & 255;
    }

    public final int getActionIndex() {
        return (nativeGetAction(this.mNativePtr) & 65280) >> 8;
    }

    public final boolean isTouchEvent() {
        return nativeIsTouchEvent(this.mNativePtr);
    }

    public final int getFlags() {
        return nativeGetFlags(this.mNativePtr);
    }

    public final boolean isTainted() {
        return (Integer.MIN_VALUE & getFlags()) != 0;
    }

    public final void setTainted(boolean tainted) {
        int flags = getFlags();
        nativeSetFlags(this.mNativePtr, tainted ? Integer.MIN_VALUE | flags : Integer.MAX_VALUE & flags);
    }

    public final boolean isTargetAccessibilityFocus() {
        return (1073741824 & getFlags()) != 0;
    }

    public final void setTargetAccessibilityFocus(boolean targetsFocus) {
        int i;
        int flags = getFlags();
        long j = this.mNativePtr;
        if (targetsFocus) {
            i = 1073741824 | flags;
        } else {
            i = -1073741825 & flags;
        }
        nativeSetFlags(j, i);
    }

    public final boolean isHoverExitPending() {
        return (getFlags() & 4) != 0;
    }

    public void setHoverExitPending(boolean hoverExitPending) {
        int i;
        int flags = getFlags();
        long j = this.mNativePtr;
        if (hoverExitPending) {
            i = flags | 4;
        } else {
            i = flags & -5;
        }
        nativeSetFlags(j, i);
    }

    public final long getDownTime() {
        return nativeGetDownTimeNanos(this.mNativePtr) / 1000000;
    }

    public final void setDownTime(long downTime) {
        nativeSetDownTimeNanos(this.mNativePtr, 1000000 * downTime);
    }

    public final long getEventTime() {
        return nativeGetEventTimeNanos(this.mNativePtr, Integer.MIN_VALUE) / 1000000;
    }

    public final long getEventTimeNano() {
        return nativeGetEventTimeNanos(this.mNativePtr, Integer.MIN_VALUE);
    }

    public final float getX() {
        return nativeGetAxisValue(this.mNativePtr, 0, 0, Integer.MIN_VALUE);
    }

    public final float getY() {
        return nativeGetAxisValue(this.mNativePtr, 1, 0, Integer.MIN_VALUE);
    }

    public final float getPressure() {
        return nativeGetAxisValue(this.mNativePtr, 2, 0, Integer.MIN_VALUE);
    }

    public final float getSize() {
        return nativeGetAxisValue(this.mNativePtr, 3, 0, Integer.MIN_VALUE);
    }

    public final float getTouchMajor() {
        return nativeGetAxisValue(this.mNativePtr, 4, 0, Integer.MIN_VALUE);
    }

    public final float getTouchMinor() {
        return nativeGetAxisValue(this.mNativePtr, 5, 0, Integer.MIN_VALUE);
    }

    public final float getToolMajor() {
        return nativeGetAxisValue(this.mNativePtr, 6, 0, Integer.MIN_VALUE);
    }

    public final float getToolMinor() {
        return nativeGetAxisValue(this.mNativePtr, 7, 0, Integer.MIN_VALUE);
    }

    public final float getOrientation() {
        return nativeGetAxisValue(this.mNativePtr, 8, 0, Integer.MIN_VALUE);
    }

    public final float getAxisValue(int axis) {
        return nativeGetAxisValue(this.mNativePtr, axis, 0, Integer.MIN_VALUE);
    }

    public final int getPointerCount() {
        return nativeGetPointerCount(this.mNativePtr);
    }

    public final int getPointerId(int pointerIndex) {
        return nativeGetPointerId(this.mNativePtr, pointerIndex);
    }

    public final int getToolType(int pointerIndex) {
        return nativeGetToolType(this.mNativePtr, pointerIndex);
    }

    public final int findPointerIndex(int pointerId) {
        return nativeFindPointerIndex(this.mNativePtr, pointerId);
    }

    public final float getX(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 0, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getY(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 1, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getPressure(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 2, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getSize(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 3, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getTouchMajor(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 4, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getTouchMinor(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 5, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getToolMajor(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 6, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getToolMinor(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 7, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getOrientation(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, 8, pointerIndex, Integer.MIN_VALUE);
    }

    public final float getAxisValue(int axis, int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, axis, pointerIndex, Integer.MIN_VALUE);
    }

    public final void getPointerCoords(int pointerIndex, PointerCoords outPointerCoords) {
        nativeGetPointerCoords(this.mNativePtr, pointerIndex, Integer.MIN_VALUE, outPointerCoords);
    }

    public final void getPointerProperties(int pointerIndex, PointerProperties outPointerProperties) {
        nativeGetPointerProperties(this.mNativePtr, pointerIndex, outPointerProperties);
    }

    public final int getMetaState() {
        return nativeGetMetaState(this.mNativePtr);
    }

    public final int getButtonState() {
        return nativeGetButtonState(this.mNativePtr);
    }

    public final void setButtonState(int buttonState) {
        nativeSetButtonState(this.mNativePtr, buttonState);
    }

    public final int getActionButton() {
        return nativeGetActionButton(this.mNativePtr);
    }

    public final void setActionButton(int button) {
        nativeSetActionButton(this.mNativePtr, button);
    }

    public final float getRawX() {
        float rawX = nativeGetRawAxisValue(this.mNativePtr, 0, 0, Integer.MIN_VALUE);
        if (this.mOffset != null) {
            return rawX - ((float) this.mOffset.x);
        }
        return rawX;
    }

    public final float getRawY() {
        float rawY = nativeGetRawAxisValue(this.mNativePtr, 1, 0, Integer.MIN_VALUE);
        if (this.mOffset != null) {
            return rawY - ((float) this.mOffset.y);
        }
        return rawY;
    }

    public final float getXPrecision() {
        return nativeGetXPrecision(this.mNativePtr);
    }

    public final float getYPrecision() {
        return nativeGetYPrecision(this.mNativePtr);
    }

    public final int getHistorySize() {
        return nativeGetHistorySize(this.mNativePtr);
    }

    public final long getHistoricalEventTime(int pos) {
        return nativeGetEventTimeNanos(this.mNativePtr, pos) / 1000000;
    }

    public final long getHistoricalEventTimeNano(int pos) {
        return nativeGetEventTimeNanos(this.mNativePtr, pos);
    }

    public final float getHistoricalX(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 0, 0, pos);
    }

    public final float getHistoricalY(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 1, 0, pos);
    }

    public final float getHistoricalPressure(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 2, 0, pos);
    }

    public final float getHistoricalSize(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 3, 0, pos);
    }

    public final float getHistoricalTouchMajor(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 4, 0, pos);
    }

    public final float getHistoricalTouchMinor(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 5, 0, pos);
    }

    public final float getHistoricalToolMajor(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 6, 0, pos);
    }

    public final float getHistoricalToolMinor(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 7, 0, pos);
    }

    public final float getHistoricalOrientation(int pos) {
        return nativeGetAxisValue(this.mNativePtr, 8, 0, pos);
    }

    public final float getHistoricalAxisValue(int axis, int pos) {
        return nativeGetAxisValue(this.mNativePtr, axis, 0, pos);
    }

    public final float getHistoricalX(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 0, pointerIndex, pos);
    }

    public final float getHistoricalY(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 1, pointerIndex, pos);
    }

    public final float getHistoricalPressure(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 2, pointerIndex, pos);
    }

    public final float getHistoricalSize(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 3, pointerIndex, pos);
    }

    public final float getHistoricalTouchMajor(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 4, pointerIndex, pos);
    }

    public final float getHistoricalTouchMinor(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 5, pointerIndex, pos);
    }

    public final float getHistoricalToolMajor(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 6, pointerIndex, pos);
    }

    public final float getHistoricalToolMinor(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 7, pointerIndex, pos);
    }

    public final float getHistoricalOrientation(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, 8, pointerIndex, pos);
    }

    public final float getHistoricalAxisValue(int axis, int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, axis, pointerIndex, pos);
    }

    public final void getHistoricalPointerCoords(int pointerIndex, int pos, PointerCoords outPointerCoords) {
        nativeGetPointerCoords(this.mNativePtr, pointerIndex, pos, outPointerCoords);
    }

    public final int getEdgeFlags() {
        return nativeGetEdgeFlags(this.mNativePtr);
    }

    public final void setEdgeFlags(int flags) {
        nativeSetEdgeFlags(this.mNativePtr, flags);
    }

    public final void setAction(int action) {
        nativeSetAction(this.mNativePtr, action);
    }

    public final void offsetLocation(float deltaX, float deltaY) {
        if (deltaX != 0.0f || deltaY != 0.0f) {
            nativeOffsetLocation(this.mNativePtr, deltaX, deltaY);
        }
    }

    public final void setLocation(float x, float y) {
        offsetLocation(x - getX(), y - getY());
    }

    public final void transform(Matrix matrix) {
        if (matrix != null) {
            nativeTransform(this.mNativePtr, matrix.native_instance);
            return;
        }
        throw new IllegalArgumentException("matrix must not be null");
    }

    public final void addBatch(long eventTime, float x, float y, float pressure, float size, int metaState) {
        synchronized (gSharedTempLock) {
            ensureSharedTempPointerCapacity(1);
            PointerCoords[] pc = gSharedTempPointerCoords;
            pc[0].clear();
            pc[0].x = x;
            pc[0].y = y;
            pc[0].pressure = pressure;
            pc[0].size = size;
            nativeAddBatch(this.mNativePtr, 1000000 * eventTime, pc, metaState);
        }
    }

    public final void addBatch(long eventTime, PointerCoords[] pointerCoords, int metaState) {
        nativeAddBatch(this.mNativePtr, 1000000 * eventTime, pointerCoords, metaState);
    }

    public final boolean addBatch(MotionEvent event) {
        MotionEvent motionEvent = event;
        int action = nativeGetAction(this.mNativePtr);
        int i = 0;
        if ((action != 2 && action != 7) || action != nativeGetAction(motionEvent.mNativePtr)) {
            return false;
        }
        if (nativeGetDeviceId(this.mNativePtr) != nativeGetDeviceId(motionEvent.mNativePtr) || nativeGetSource(this.mNativePtr) != nativeGetSource(motionEvent.mNativePtr) || nativeGetFlags(this.mNativePtr) != nativeGetFlags(motionEvent.mNativePtr)) {
            return false;
        }
        int pointerCount = nativeGetPointerCount(this.mNativePtr);
        if (pointerCount != nativeGetPointerCount(motionEvent.mNativePtr)) {
            return false;
        }
        synchronized (gSharedTempLock) {
            ensureSharedTempPointerCapacity(Math.max(pointerCount, 2));
            PointerProperties[] pp = gSharedTempPointerProperties;
            PointerCoords[] pc = gSharedTempPointerCoords;
            for (int i2 = 0; i2 < pointerCount; i2++) {
                nativeGetPointerProperties(this.mNativePtr, i2, pp[0]);
                nativeGetPointerProperties(motionEvent.mNativePtr, i2, pp[1]);
                if (!pp[0].equals(pp[1])) {
                    return false;
                }
            }
            int metaState = nativeGetMetaState(motionEvent.mNativePtr);
            int historySize = nativeGetHistorySize(motionEvent.mNativePtr);
            int h = 0;
            while (true) {
                int h2 = h;
                if (h2 > historySize) {
                    return true;
                }
                int historyPos = h2 == historySize ? Integer.MIN_VALUE : h2;
                for (int i3 = i; i3 < pointerCount; i3++) {
                    nativeGetPointerCoords(motionEvent.mNativePtr, i3, historyPos, pc[i3]);
                }
                int i4 = historyPos;
                nativeAddBatch(this.mNativePtr, nativeGetEventTimeNanos(motionEvent.mNativePtr, historyPos), pc, metaState);
                h = h2 + 1;
                historySize = historySize;
                i = 0;
            }
        }
    }

    public final boolean isWithinBoundsNoHistory(float left, float top, float right, float bottom) {
        int pointerCount = nativeGetPointerCount(this.mNativePtr);
        for (int i = 0; i < pointerCount; i++) {
            float x = nativeGetAxisValue(this.mNativePtr, 0, i, Integer.MIN_VALUE);
            float y = nativeGetAxisValue(this.mNativePtr, 1, i, Integer.MIN_VALUE);
            if (x < left || x > right || y < top || y > bottom) {
                return false;
            }
        }
        return true;
    }

    private static final float clamp(float value, float low, float high) {
        if (value < low) {
            return low;
        }
        if (value > high) {
            return high;
        }
        return value;
    }

    public final MotionEvent clampNoHistory(float left, float top, float right, float bottom) {
        MotionEvent ev = obtain();
        synchronized (gSharedTempLock) {
            try {
                int pointerCount = nativeGetPointerCount(this.mNativePtr);
                ensureSharedTempPointerCapacity(pointerCount);
                PointerProperties[] pp = gSharedTempPointerProperties;
                PointerCoords[] pc = gSharedTempPointerCoords;
                int i = 0;
                while (i < pointerCount) {
                    try {
                        nativeGetPointerProperties(this.mNativePtr, i, pp[i]);
                        nativeGetPointerCoords(this.mNativePtr, i, Integer.MIN_VALUE, pc[i]);
                        pc[i].x = clamp(pc[i].x, left, right);
                        pc[i].y = clamp(pc[i].y, top, bottom);
                        i++;
                    } catch (Throwable th) {
                        th = th;
                        float f = left;
                        float f2 = top;
                        float f3 = right;
                        float f4 = bottom;
                        throw th;
                    }
                }
                float f5 = left;
                float f6 = top;
                float f7 = right;
                float f8 = bottom;
                ev.mNativePtr = nativeInitialize(ev.mNativePtr, nativeGetDeviceId(this.mNativePtr), nativeGetSource(this.mNativePtr), nativeGetAction(this.mNativePtr), nativeGetFlags(this.mNativePtr), nativeGetEdgeFlags(this.mNativePtr), nativeGetMetaState(this.mNativePtr), nativeGetButtonState(this.mNativePtr), nativeGetXOffset(this.mNativePtr), nativeGetYOffset(this.mNativePtr), nativeGetXPrecision(this.mNativePtr), nativeGetYPrecision(this.mNativePtr), nativeGetDownTimeNanos(this.mNativePtr), nativeGetEventTimeNanos(this.mNativePtr, Integer.MIN_VALUE), pointerCount, pp, pc);
                return ev;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public final int getPointerIdBits() {
        int idBits = 0;
        int pointerCount = nativeGetPointerCount(this.mNativePtr);
        for (int i = 0; i < pointerCount; i++) {
            idBits |= 1 << nativeGetPointerId(this.mNativePtr, i);
        }
        return idBits;
    }

    public final MotionEvent split(int idBits) {
        Object obj;
        int newPointerCount;
        int newAction;
        int oldAction;
        int oldActionMasked;
        int oldActionPointerIndex;
        int newPointerCount2;
        MotionEvent ev;
        int historySize;
        int h;
        int newAction2;
        MotionEvent ev2 = obtain();
        Object obj2 = gSharedTempLock;
        synchronized (obj2) {
            try {
                int oldPointerCount = nativeGetPointerCount(this.mNativePtr);
                ensureSharedTempPointerCapacity(oldPointerCount);
                PointerProperties[] pp = gSharedTempPointerProperties;
                PointerCoords[] pc = gSharedTempPointerCoords;
                int[] map = gSharedTempPointerIndexMap;
                int oldAction2 = nativeGetAction(this.mNativePtr);
                int oldActionMasked2 = oldAction2 & 255;
                int oldActionPointerIndex2 = (65280 & oldAction2) >> 8;
                int newActionPointerIndex = -1;
                int newPointerCount3 = 0;
                int newIdBits = 0;
                int i = 0;
                while (true) {
                    newPointerCount = 1;
                    if (i >= oldPointerCount) {
                        break;
                    }
                    try {
                        nativeGetPointerProperties(this.mNativePtr, i, pp[newPointerCount3]);
                        int idBit = 1 << pp[newPointerCount3].id;
                        if ((idBit & idBits) != 0) {
                            if (i == oldActionPointerIndex2) {
                                newActionPointerIndex = newPointerCount3;
                            }
                            map[newPointerCount3] = i;
                            newPointerCount3++;
                            newIdBits |= idBit;
                        }
                        i++;
                    } catch (Throwable th) {
                        th = th;
                        MotionEvent motionEvent = ev2;
                        obj = obj2;
                        throw th;
                    }
                }
                if (newPointerCount3 != 0) {
                    if (oldActionMasked2 == 5 || oldActionMasked2 == 6) {
                        if (newActionPointerIndex < 0) {
                            newAction2 = 2;
                        } else if (newPointerCount3 == 1) {
                            if (oldActionMasked2 == 5) {
                                newPointerCount = 0;
                            }
                            newAction2 = newPointerCount;
                        } else {
                            newAction2 = (newActionPointerIndex << 8) | oldActionMasked2;
                        }
                        newAction = newAction2;
                    } else {
                        newAction = oldAction2;
                    }
                    int historySize2 = nativeGetHistorySize(this.mNativePtr);
                    int h2 = 0;
                    while (true) {
                        int h3 = h2;
                        if (h3 <= historySize2) {
                            int historyPos = h3 == historySize2 ? Integer.MIN_VALUE : h3;
                            int i2 = 0;
                            while (i2 < newPointerCount3) {
                                int oldPointerCount2 = oldPointerCount;
                                int historySize3 = historySize2;
                                nativeGetPointerCoords(this.mNativePtr, map[i2], historyPos, pc[i2]);
                                i2++;
                                oldPointerCount = oldPointerCount2;
                                historySize2 = historySize3;
                            }
                            int oldPointerCount3 = oldPointerCount;
                            int historySize4 = historySize2;
                            long eventTimeNanos = nativeGetEventTimeNanos(this.mNativePtr, historyPos);
                            if (h3 == 0) {
                                long j = ev2.mNativePtr;
                                MotionEvent ev3 = ev2;
                                obj = obj2;
                                try {
                                    int i3 = historyPos;
                                    h = h3;
                                    historySize = historySize4;
                                    newPointerCount2 = newPointerCount3;
                                    oldActionPointerIndex = oldActionPointerIndex2;
                                    oldActionMasked = oldActionMasked2;
                                    oldAction = oldAction2;
                                    ev = ev3;
                                    ev.mNativePtr = nativeInitialize(j, nativeGetDeviceId(this.mNativePtr), nativeGetSource(this.mNativePtr), newAction, nativeGetFlags(this.mNativePtr), nativeGetEdgeFlags(this.mNativePtr), nativeGetMetaState(this.mNativePtr), nativeGetButtonState(this.mNativePtr), nativeGetXOffset(this.mNativePtr), nativeGetYOffset(this.mNativePtr), nativeGetXPrecision(this.mNativePtr), nativeGetYPrecision(this.mNativePtr), nativeGetDownTimeNanos(this.mNativePtr), eventTimeNanos, newPointerCount2, pp, pc);
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } else {
                                ev = ev2;
                                obj = obj2;
                                int i4 = historyPos;
                                h = h3;
                                oldActionPointerIndex = oldActionPointerIndex2;
                                oldActionMasked = oldActionMasked2;
                                oldAction = oldAction2;
                                historySize = historySize4;
                                newPointerCount2 = newPointerCount3;
                                nativeAddBatch(ev.mNativePtr, eventTimeNanos, pc, 0);
                            }
                            h2 = h + 1;
                            historySize2 = historySize;
                            ev2 = ev;
                            oldPointerCount = oldPointerCount3;
                            newPointerCount3 = newPointerCount2;
                            oldActionPointerIndex2 = oldActionPointerIndex;
                            oldActionMasked2 = oldActionMasked;
                            oldAction2 = oldAction;
                            obj2 = obj;
                        } else {
                            MotionEvent ev4 = ev2;
                            Object obj3 = obj2;
                            int i5 = historySize2;
                            int i6 = newPointerCount3;
                            int i7 = oldActionPointerIndex2;
                            int i8 = oldActionMasked2;
                            int i9 = oldAction2;
                            return ev4;
                        }
                    }
                } else {
                    MotionEvent motionEvent2 = ev2;
                    Object obj4 = obj2;
                    int i10 = newPointerCount3;
                    int i11 = oldActionPointerIndex2;
                    int i12 = oldActionMasked2;
                    int i13 = oldAction2;
                    throw new IllegalArgumentException("idBits did not match any ids in the event");
                }
            } catch (Throwable th3) {
                th = th3;
                MotionEvent motionEvent3 = ev2;
                obj = obj2;
                throw th;
            }
        }
    }

    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("MotionEvent { action=");
        msg.append(actionToString(getAction()));
        appendUnless(WifiEnterpriseConfig.ENGINE_DISABLE, msg, ", actionButton=", buttonStateToString(getActionButton()));
        int pointerCount = getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            appendUnless(Integer.valueOf(i), msg, ", id[" + i + "]=", Integer.valueOf(getPointerId(i)));
            float x = getX(i);
            float y = getY(i);
            msg.append(", x[");
            msg.append(i);
            msg.append("]=");
            msg.append(x);
            msg.append(", y[");
            msg.append(i);
            msg.append("]=");
            msg.append(y);
            appendUnless(TOOL_TYPE_SYMBOLIC_NAMES.get(1), msg, ", toolType[" + i + "]=", toolTypeToString(getToolType(i)));
        }
        appendUnless(WifiEnterpriseConfig.ENGINE_DISABLE, msg, ", buttonState=", buttonStateToString(getButtonState()));
        appendUnless(WifiEnterpriseConfig.ENGINE_DISABLE, msg, ", metaState=", KeyEvent.metaStateToString(getMetaState()));
        appendUnless(WifiEnterpriseConfig.ENGINE_DISABLE, msg, ", flags=0x", Integer.toHexString(getFlags()));
        appendUnless(WifiEnterpriseConfig.ENGINE_DISABLE, msg, ", edgeFlags=0x", Integer.toHexString(getEdgeFlags()));
        appendUnless(1, msg, ", pointerCount=", Integer.valueOf(pointerCount));
        appendUnless(0, msg, ", historySize=", Integer.valueOf(getHistorySize()));
        msg.append(", eventTime=");
        msg.append(getEventTime());
        msg.append(", downTime=");
        msg.append(getDownTime());
        msg.append(", deviceId=");
        msg.append(getDeviceId());
        msg.append(", source=0x");
        msg.append(Integer.toHexString(getSource()));
        msg.append(" }");
        return msg.toString();
    }

    private static <T> void appendUnless(T t, StringBuilder sb, String key, T value) {
        sb.append(key);
        sb.append(value);
    }

    public static String actionToString(int action) {
        switch (action) {
            case 0:
                return "ACTION_DOWN";
            case 1:
                return "ACTION_UP";
            case 2:
                return "ACTION_MOVE";
            case 3:
                return "ACTION_CANCEL";
            case 4:
                return "ACTION_OUTSIDE";
            case 7:
                return "ACTION_HOVER_MOVE";
            case 8:
                return "ACTION_SCROLL";
            case 9:
                return "ACTION_HOVER_ENTER";
            case 10:
                return "ACTION_HOVER_EXIT";
            case 11:
                return "ACTION_BUTTON_PRESS";
            case 12:
                return "ACTION_BUTTON_RELEASE";
            default:
                int index = (65280 & action) >> 8;
                switch (action & 255) {
                    case 5:
                        return "ACTION_POINTER_DOWN(" + index + ")";
                    case 6:
                        return "ACTION_POINTER_UP(" + index + ")";
                    default:
                        return Integer.toString(action);
                }
        }
    }

    public static String axisToString(int axis) {
        String symbolicName = nativeAxisToString(axis);
        if (symbolicName == null) {
            return Integer.toString(axis);
        }
        return LABEL_PREFIX + symbolicName;
    }

    public static int axisFromString(String symbolicName) {
        if (symbolicName.startsWith(LABEL_PREFIX)) {
            symbolicName = symbolicName.substring(LABEL_PREFIX.length());
            int axis = nativeAxisFromString(symbolicName);
            if (axis >= 0) {
                return axis;
            }
        }
        try {
            return Integer.parseInt(symbolicName, 10);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String buttonStateToString(int buttonState) {
        if (buttonState == 0) {
            return WifiEnterpriseConfig.ENGINE_DISABLE;
        }
        StringBuilder result = null;
        int buttonState2 = buttonState;
        int i = 0;
        while (buttonState2 != 0) {
            boolean isSet = (buttonState2 & 1) != 0;
            buttonState2 >>>= 1;
            if (isSet) {
                String name = BUTTON_SYMBOLIC_NAMES[i];
                if (result != null) {
                    result.append('|');
                    result.append(name);
                } else if (buttonState2 == 0) {
                    return name;
                } else {
                    result = new StringBuilder(name);
                }
            }
            i++;
        }
        return result.toString();
    }

    public static String toolTypeToString(int toolType) {
        String symbolicName = TOOL_TYPE_SYMBOLIC_NAMES.get(toolType);
        return symbolicName != null ? symbolicName : Integer.toString(toolType);
    }

    public final boolean isButtonPressed(int button) {
        boolean z = false;
        if (button == 0) {
            return false;
        }
        if ((getButtonState() & button) == button) {
            z = true;
        }
        return z;
    }

    public static MotionEvent createFromParcelBody(Parcel in) {
        MotionEvent ev = obtain();
        ev.mNativePtr = nativeReadFromParcel(ev.mNativePtr, in);
        return ev;
    }

    public final void cancel() {
        setAction(3);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(1);
        nativeWriteToParcel(this.mNativePtr, out);
    }

    public final void setOffset(Point offset) {
        this.mOffset = offset;
    }
}
