package android.view;

import android.graphics.Matrix;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;
import android.view.WindowManager.LayoutParams;
import com.android.internal.os.HwBootFail;

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
    private static final SparseArray<String> AXIS_SYMBOLIC_NAMES = null;
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
    private static final String[] BUTTON_SYMBOLIC_NAMES = null;
    public static final int BUTTON_TERTIARY = 4;
    public static final Creator<MotionEvent> CREATOR = null;
    public static final int EDGE_BOTTOM = 2;
    public static final int EDGE_LEFT = 4;
    public static final int EDGE_RIGHT = 8;
    public static final int EDGE_TOP = 1;
    public static final int FLAG_TAINTED = Integer.MIN_VALUE;
    public static final int FLAG_TARGET_ACCESSIBILITY_FOCUS = 1073741824;
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
    private static final SparseArray<String> TOOL_TYPE_SYMBOLIC_NAMES = null;
    public static final int TOOL_TYPE_UNKNOWN = 0;
    private static final Object gRecyclerLock = null;
    private static MotionEvent gRecyclerTop;
    private static int gRecyclerUsed;
    private static final Object gSharedTempLock = null;
    private static PointerCoords[] gSharedTempPointerCoords;
    private static int[] gSharedTempPointerIndexMap;
    private static PointerProperties[] gSharedTempPointerProperties;
    private long mNativePtr;
    private MotionEvent mNext;

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

        public float getAxisValue(int r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.MotionEvent.PointerCoords.getAxisValue(int):float
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.MotionEvent.PointerCoords.getAxisValue(int):float");
        }

        public void setAxisValue(int r1, float r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.MotionEvent.PointerCoords.setAxisValue(int, float):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.MotionEvent.PointerCoords.setAxisValue(int, float):void");
        }

        public PointerCoords(PointerCoords other) {
            copyFrom(other);
        }

        public static PointerCoords[] createArray(int size) {
            PointerCoords[] array = new PointerCoords[size];
            for (int i = MotionEvent.AXIS_X; i < size; i += MotionEvent.TOOL_TYPE_FINGER) {
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
                System.arraycopy(otherValues, MotionEvent.AXIS_X, values, MotionEvent.AXIS_X, count);
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
            for (int i = MotionEvent.AXIS_X; i < size; i += MotionEvent.TOOL_TYPE_FINGER) {
                array[i] = new PointerProperties();
            }
            return array;
        }

        public void clear() {
            this.id = MotionEvent.INVALID_POINTER_ID;
            this.toolType = MotionEvent.AXIS_X;
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

        private boolean equals(PointerProperties other) {
            return other != null && this.id == other.id && this.toolType == other.toolType;
        }

        public int hashCode() {
            return this.id | (this.toolType << MotionEvent.EDGE_RIGHT);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.MotionEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.MotionEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.MotionEvent.<clinit>():void");
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

    private static native void nativeTransform(long j, Matrix matrix);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    private static final void ensureSharedTempPointerCapacity(int desiredCapacity) {
        if (gSharedTempPointerCoords == null || gSharedTempPointerCoords.length < desiredCapacity) {
            int capacity = gSharedTempPointerCoords != null ? gSharedTempPointerCoords.length : EDGE_RIGHT;
            while (capacity < desiredCapacity) {
                capacity *= TOOL_TYPE_STYLUS;
            }
            gSharedTempPointerCoords = PointerCoords.createArray(capacity);
            gSharedTempPointerProperties = PointerProperties.createArray(capacity);
            gSharedTempPointerIndexMap = new int[capacity];
        }
    }

    private MotionEvent() {
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mNativePtr != 0) {
                nativeDispose(this.mNativePtr);
                this.mNativePtr = 0;
            }
            super.finalize();
        } catch (Throwable th) {
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
            gRecyclerUsed += INVALID_POINTER_ID;
            ev.mNext = null;
            ev.prepareForReuse();
            return ev;
        }
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action, int pointerCount, PointerProperties[] pointerProperties, PointerCoords[] pointerCoords, int metaState, int buttonState, float xPrecision, float yPrecision, int deviceId, int edgeFlags, int source, int flags) {
        MotionEvent ev = obtain();
        ev.mNativePtr = nativeInitialize(ev.mNativePtr, deviceId, source, action, flags, edgeFlags, metaState, buttonState, 0.0f, 0.0f, xPrecision, yPrecision, downTime * NS_PER_MS, eventTime * NS_PER_MS, pointerCount, pointerProperties, pointerCoords);
        return ev;
    }

    @Deprecated
    public static MotionEvent obtain(long downTime, long eventTime, int action, int pointerCount, int[] pointerIds, PointerCoords[] pointerCoords, int metaState, float xPrecision, float yPrecision, int deviceId, int edgeFlags, int source, int flags) {
        MotionEvent obtain;
        synchronized (gSharedTempLock) {
            ensureSharedTempPointerCapacity(pointerCount);
            PointerProperties[] pp = gSharedTempPointerProperties;
            for (int i = AXIS_X; i < pointerCount; i += TOOL_TYPE_FINGER) {
                pp[i].clear();
                pp[i].id = pointerIds[i];
            }
            obtain = obtain(downTime, eventTime, action, pointerCount, pp, pointerCoords, metaState, AXIS_X, xPrecision, yPrecision, deviceId, edgeFlags, source, flags);
        }
        return obtain;
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, float pressure, float size, int metaState, float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        MotionEvent ev = obtain();
        synchronized (gSharedTempLock) {
            ensureSharedTempPointerCapacity(TOOL_TYPE_FINGER);
            PointerProperties[] pp = gSharedTempPointerProperties;
            pp[AXIS_X].clear();
            pp[AXIS_X].id = AXIS_X;
            PointerCoords[] pc = gSharedTempPointerCoords;
            pc[AXIS_X].clear();
            pc[AXIS_X].x = x;
            pc[AXIS_X].y = y;
            pc[AXIS_X].pressure = pressure;
            pc[AXIS_X].size = size;
            ev.mNativePtr = nativeInitialize(ev.mNativePtr, deviceId, AXIS_X, action, AXIS_X, edgeFlags, metaState, AXIS_X, 0.0f, 0.0f, xPrecision, yPrecision, downTime * NS_PER_MS, eventTime * NS_PER_MS, TOOL_TYPE_FINGER, pp, pc);
        }
        return ev;
    }

    @Deprecated
    public static MotionEvent obtain(long downTime, long eventTime, int action, int pointerCount, float x, float y, float pressure, float size, int metaState, float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        return obtain(downTime, eventTime, action, x, y, pressure, size, metaState, xPrecision, yPrecision, deviceId, edgeFlags);
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        return obtain(downTime, eventTime, action, x, y, LayoutParams.BRIGHTNESS_OVERRIDE_FULL, LayoutParams.BRIGHTNESS_OVERRIDE_FULL, metaState, LayoutParams.BRIGHTNESS_OVERRIDE_FULL, LayoutParams.BRIGHTNESS_OVERRIDE_FULL, AXIS_X, AXIS_X);
    }

    public static MotionEvent obtain(MotionEvent other) {
        if (other == null) {
            throw new IllegalArgumentException("other motion event must not be null");
        }
        MotionEvent ev = obtain();
        ev.mNativePtr = nativeCopy(ev.mNativePtr, other.mNativePtr, true);
        return ev;
    }

    public static MotionEvent obtainNoHistory(MotionEvent other) {
        if (other == null) {
            throw new IllegalArgumentException("other motion event must not be null");
        }
        MotionEvent ev = obtain();
        ev.mNativePtr = nativeCopy(ev.mNativePtr, other.mNativePtr, false);
        return ev;
    }

    public MotionEvent copy() {
        return obtain(this);
    }

    public final void recycle() {
        super.recycle();
        synchronized (gRecyclerLock) {
            if (gRecyclerUsed < MAX_RECYCLED) {
                gRecyclerUsed += TOOL_TYPE_FINGER;
                this.mNext = gRecyclerTop;
                gRecyclerTop = this;
            }
        }
    }

    public final void scale(float scale) {
        if (scale != LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
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
        return nativeGetAction(this.mNativePtr) & ACTION_MASK;
    }

    public final int getActionIndex() {
        return (nativeGetAction(this.mNativePtr) & ACTION_POINTER_INDEX_MASK) >> EDGE_RIGHT;
    }

    public final boolean isTouchEvent() {
        return nativeIsTouchEvent(this.mNativePtr);
    }

    public final int getFlags() {
        return nativeGetFlags(this.mNativePtr);
    }

    public final boolean isTainted() {
        if ((HISTORY_CURRENT & getFlags()) != 0) {
            return true;
        }
        return false;
    }

    public final void setTainted(boolean tainted) {
        int flags = getFlags();
        nativeSetFlags(this.mNativePtr, tainted ? HISTORY_CURRENT | flags : HwBootFail.STAGE_BOOT_SUCCESS & flags);
    }

    public final boolean isTargetAccessibilityFocus() {
        if ((FLAG_TARGET_ACCESSIBILITY_FOCUS & getFlags()) != 0) {
            return true;
        }
        return false;
    }

    public final void setTargetAccessibilityFocus(boolean targetsFocus) {
        int i;
        int flags = getFlags();
        long j = this.mNativePtr;
        if (targetsFocus) {
            i = FLAG_TARGET_ACCESSIBILITY_FOCUS | flags;
        } else {
            i = -1073741825 & flags;
        }
        nativeSetFlags(j, i);
    }

    public final long getDownTime() {
        return nativeGetDownTimeNanos(this.mNativePtr) / NS_PER_MS;
    }

    public final void setDownTime(long downTime) {
        nativeSetDownTimeNanos(this.mNativePtr, NS_PER_MS * downTime);
    }

    public final long getEventTime() {
        return nativeGetEventTimeNanos(this.mNativePtr, HISTORY_CURRENT) / NS_PER_MS;
    }

    public final long getEventTimeNano() {
        return nativeGetEventTimeNanos(this.mNativePtr, HISTORY_CURRENT);
    }

    public final float getX() {
        return nativeGetAxisValue(this.mNativePtr, AXIS_X, AXIS_X, HISTORY_CURRENT);
    }

    public final float getY() {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_FINGER, AXIS_X, HISTORY_CURRENT);
    }

    public final float getPressure() {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_STYLUS, AXIS_X, HISTORY_CURRENT);
    }

    public final float getSize() {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_MOUSE, AXIS_X, HISTORY_CURRENT);
    }

    public final float getTouchMajor() {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_ERASER, AXIS_X, HISTORY_CURRENT);
    }

    public final float getTouchMinor() {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOUCH_MINOR, AXIS_X, HISTORY_CURRENT);
    }

    public final float getToolMajor() {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOOL_MAJOR, AXIS_X, HISTORY_CURRENT);
    }

    public final float getToolMinor() {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOOL_MINOR, AXIS_X, HISTORY_CURRENT);
    }

    public final float getOrientation() {
        return nativeGetAxisValue(this.mNativePtr, EDGE_RIGHT, AXIS_X, HISTORY_CURRENT);
    }

    public final float getAxisValue(int axis) {
        return nativeGetAxisValue(this.mNativePtr, axis, AXIS_X, HISTORY_CURRENT);
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
        return nativeGetAxisValue(this.mNativePtr, AXIS_X, pointerIndex, HISTORY_CURRENT);
    }

    public final float getY(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_FINGER, pointerIndex, HISTORY_CURRENT);
    }

    public final float getPressure(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_STYLUS, pointerIndex, HISTORY_CURRENT);
    }

    public final float getSize(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_MOUSE, pointerIndex, HISTORY_CURRENT);
    }

    public final float getTouchMajor(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_ERASER, pointerIndex, HISTORY_CURRENT);
    }

    public final float getTouchMinor(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOUCH_MINOR, pointerIndex, HISTORY_CURRENT);
    }

    public final float getToolMajor(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOOL_MAJOR, pointerIndex, HISTORY_CURRENT);
    }

    public final float getToolMinor(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOOL_MINOR, pointerIndex, HISTORY_CURRENT);
    }

    public final float getOrientation(int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, EDGE_RIGHT, pointerIndex, HISTORY_CURRENT);
    }

    public final float getAxisValue(int axis, int pointerIndex) {
        return nativeGetAxisValue(this.mNativePtr, axis, pointerIndex, HISTORY_CURRENT);
    }

    public final void getPointerCoords(int pointerIndex, PointerCoords outPointerCoords) {
        nativeGetPointerCoords(this.mNativePtr, pointerIndex, HISTORY_CURRENT, outPointerCoords);
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
        return nativeGetRawAxisValue(this.mNativePtr, AXIS_X, AXIS_X, HISTORY_CURRENT);
    }

    public final float getRawY() {
        return nativeGetRawAxisValue(this.mNativePtr, TOOL_TYPE_FINGER, AXIS_X, HISTORY_CURRENT);
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
        return nativeGetEventTimeNanos(this.mNativePtr, pos) / NS_PER_MS;
    }

    public final long getHistoricalEventTimeNano(int pos) {
        return nativeGetEventTimeNanos(this.mNativePtr, pos);
    }

    public final float getHistoricalX(int pos) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_X, AXIS_X, pos);
    }

    public final float getHistoricalY(int pos) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_FINGER, AXIS_X, pos);
    }

    public final float getHistoricalPressure(int pos) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_STYLUS, AXIS_X, pos);
    }

    public final float getHistoricalSize(int pos) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_MOUSE, AXIS_X, pos);
    }

    public final float getHistoricalTouchMajor(int pos) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_ERASER, AXIS_X, pos);
    }

    public final float getHistoricalTouchMinor(int pos) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOUCH_MINOR, AXIS_X, pos);
    }

    public final float getHistoricalToolMajor(int pos) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOOL_MAJOR, AXIS_X, pos);
    }

    public final float getHistoricalToolMinor(int pos) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOOL_MINOR, AXIS_X, pos);
    }

    public final float getHistoricalOrientation(int pos) {
        return nativeGetAxisValue(this.mNativePtr, EDGE_RIGHT, AXIS_X, pos);
    }

    public final float getHistoricalAxisValue(int axis, int pos) {
        return nativeGetAxisValue(this.mNativePtr, axis, AXIS_X, pos);
    }

    public final float getHistoricalX(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_X, pointerIndex, pos);
    }

    public final float getHistoricalY(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_FINGER, pointerIndex, pos);
    }

    public final float getHistoricalPressure(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_STYLUS, pointerIndex, pos);
    }

    public final float getHistoricalSize(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_MOUSE, pointerIndex, pos);
    }

    public final float getHistoricalTouchMajor(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_ERASER, pointerIndex, pos);
    }

    public final float getHistoricalTouchMinor(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOUCH_MINOR, pointerIndex, pos);
    }

    public final float getHistoricalToolMajor(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOOL_MAJOR, pointerIndex, pos);
    }

    public final float getHistoricalToolMinor(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, AXIS_TOOL_MINOR, pointerIndex, pos);
    }

    public final float getHistoricalOrientation(int pointerIndex, int pos) {
        return nativeGetAxisValue(this.mNativePtr, EDGE_RIGHT, pointerIndex, pos);
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
        if (matrix == null) {
            throw new IllegalArgumentException("matrix must not be null");
        }
        nativeTransform(this.mNativePtr, matrix);
    }

    public final void addBatch(long eventTime, float x, float y, float pressure, float size, int metaState) {
        synchronized (gSharedTempLock) {
            ensureSharedTempPointerCapacity(TOOL_TYPE_FINGER);
            PointerCoords[] pc = gSharedTempPointerCoords;
            pc[AXIS_X].clear();
            pc[AXIS_X].x = x;
            pc[AXIS_X].y = y;
            pc[AXIS_X].pressure = pressure;
            pc[AXIS_X].size = size;
            nativeAddBatch(this.mNativePtr, NS_PER_MS * eventTime, pc, metaState);
        }
    }

    public final void addBatch(long eventTime, PointerCoords[] pointerCoords, int metaState) {
        nativeAddBatch(this.mNativePtr, NS_PER_MS * eventTime, pointerCoords, metaState);
    }

    public final boolean addBatch(MotionEvent event) {
        int action = nativeGetAction(this.mNativePtr);
        if (action != TOOL_TYPE_STYLUS && action != AXIS_TOOL_MINOR) {
            return false;
        }
        if (action != nativeGetAction(event.mNativePtr)) {
            return false;
        }
        if (nativeGetDeviceId(this.mNativePtr) == nativeGetDeviceId(event.mNativePtr)) {
            if (nativeGetSource(this.mNativePtr) == nativeGetSource(event.mNativePtr)) {
                if (nativeGetFlags(this.mNativePtr) == nativeGetFlags(event.mNativePtr)) {
                    int pointerCount = nativeGetPointerCount(this.mNativePtr);
                    if (pointerCount != nativeGetPointerCount(event.mNativePtr)) {
                        return false;
                    }
                    synchronized (gSharedTempLock) {
                        ensureSharedTempPointerCapacity(Math.max(pointerCount, TOOL_TYPE_STYLUS));
                        PointerProperties[] pp = gSharedTempPointerProperties;
                        PointerCoords[] pc = gSharedTempPointerCoords;
                        int i = AXIS_X;
                        while (i < pointerCount) {
                            nativeGetPointerProperties(this.mNativePtr, i, pp[AXIS_X]);
                            nativeGetPointerProperties(event.mNativePtr, i, pp[TOOL_TYPE_FINGER]);
                            if (pp[AXIS_X].equals(pp[TOOL_TYPE_FINGER])) {
                                i += TOOL_TYPE_FINGER;
                            } else {
                                return false;
                            }
                        }
                        int metaState = nativeGetMetaState(event.mNativePtr);
                        int historySize = nativeGetHistorySize(event.mNativePtr);
                        int h = AXIS_X;
                        while (h <= historySize) {
                            int historyPos = h == historySize ? HISTORY_CURRENT : h;
                            for (i = AXIS_X; i < pointerCount; i += TOOL_TYPE_FINGER) {
                                nativeGetPointerCoords(event.mNativePtr, i, historyPos, pc[i]);
                            }
                            nativeAddBatch(this.mNativePtr, nativeGetEventTimeNanos(event.mNativePtr, historyPos), pc, metaState);
                            h += TOOL_TYPE_FINGER;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public final boolean isWithinBoundsNoHistory(float left, float top, float right, float bottom) {
        int pointerCount = nativeGetPointerCount(this.mNativePtr);
        for (int i = AXIS_X; i < pointerCount; i += TOOL_TYPE_FINGER) {
            float x = nativeGetAxisValue(this.mNativePtr, AXIS_X, i, HISTORY_CURRENT);
            float y = nativeGetAxisValue(this.mNativePtr, TOOL_TYPE_FINGER, i, HISTORY_CURRENT);
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
            int pointerCount = nativeGetPointerCount(this.mNativePtr);
            ensureSharedTempPointerCapacity(pointerCount);
            PointerProperties[] pp = gSharedTempPointerProperties;
            PointerCoords[] pc = gSharedTempPointerCoords;
            for (int i = AXIS_X; i < pointerCount; i += TOOL_TYPE_FINGER) {
                nativeGetPointerProperties(this.mNativePtr, i, pp[i]);
                nativeGetPointerCoords(this.mNativePtr, i, HISTORY_CURRENT, pc[i]);
                pc[i].x = clamp(pc[i].x, left, right);
                pc[i].y = clamp(pc[i].y, top, bottom);
            }
            ev.mNativePtr = nativeInitialize(ev.mNativePtr, nativeGetDeviceId(this.mNativePtr), nativeGetSource(this.mNativePtr), nativeGetAction(this.mNativePtr), nativeGetFlags(this.mNativePtr), nativeGetEdgeFlags(this.mNativePtr), nativeGetMetaState(this.mNativePtr), nativeGetButtonState(this.mNativePtr), nativeGetXOffset(this.mNativePtr), nativeGetYOffset(this.mNativePtr), nativeGetXPrecision(this.mNativePtr), nativeGetYPrecision(this.mNativePtr), nativeGetDownTimeNanos(this.mNativePtr), nativeGetEventTimeNanos(this.mNativePtr, HISTORY_CURRENT), pointerCount, pp, pc);
        }
        return ev;
    }

    public final int getPointerIdBits() {
        int idBits = AXIS_X;
        int pointerCount = nativeGetPointerCount(this.mNativePtr);
        for (int i = AXIS_X; i < pointerCount; i += TOOL_TYPE_FINGER) {
            idBits |= TOOL_TYPE_FINGER << nativeGetPointerId(this.mNativePtr, i);
        }
        return idBits;
    }

    public final MotionEvent split(int idBits) {
        MotionEvent ev = obtain();
        synchronized (gSharedTempLock) {
            int i;
            int oldPointerCount = nativeGetPointerCount(this.mNativePtr);
            ensureSharedTempPointerCapacity(oldPointerCount);
            PointerProperties[] pp = gSharedTempPointerProperties;
            PointerCoords[] pc = gSharedTempPointerCoords;
            int[] map = gSharedTempPointerIndexMap;
            int oldAction = nativeGetAction(this.mNativePtr);
            int oldActionMasked = oldAction & ACTION_MASK;
            int oldActionPointerIndex = (ACTION_POINTER_INDEX_MASK & oldAction) >> EDGE_RIGHT;
            int newActionPointerIndex = INVALID_POINTER_ID;
            int newPointerCount = AXIS_X;
            int newIdBits = AXIS_X;
            for (i = AXIS_X; i < oldPointerCount; i += TOOL_TYPE_FINGER) {
                nativeGetPointerProperties(this.mNativePtr, i, pp[newPointerCount]);
                int idBit = TOOL_TYPE_FINGER << pp[newPointerCount].id;
                if ((idBit & idBits) != 0) {
                    if (i == oldActionPointerIndex) {
                        newActionPointerIndex = newPointerCount;
                    }
                    map[newPointerCount] = i;
                    newPointerCount += TOOL_TYPE_FINGER;
                    newIdBits |= idBit;
                }
            }
            if (newPointerCount == 0) {
                throw new IllegalArgumentException("idBits did not match any ids in the event");
            }
            int newAction = (oldActionMasked == AXIS_TOUCH_MINOR || oldActionMasked == AXIS_TOOL_MAJOR) ? newActionPointerIndex < 0 ? TOOL_TYPE_STYLUS : newPointerCount == TOOL_TYPE_FINGER ? oldActionMasked == AXIS_TOUCH_MINOR ? AXIS_X : TOOL_TYPE_FINGER : oldActionMasked | (newActionPointerIndex << EDGE_RIGHT) : oldAction;
            int historySize = nativeGetHistorySize(this.mNativePtr);
            int h = AXIS_X;
            while (h <= historySize) {
                int historyPos = h == historySize ? HISTORY_CURRENT : h;
                for (i = AXIS_X; i < newPointerCount; i += TOOL_TYPE_FINGER) {
                    nativeGetPointerCoords(this.mNativePtr, map[i], historyPos, pc[i]);
                }
                long eventTimeNanos = nativeGetEventTimeNanos(this.mNativePtr, historyPos);
                if (h == 0) {
                    ev.mNativePtr = nativeInitialize(ev.mNativePtr, nativeGetDeviceId(this.mNativePtr), nativeGetSource(this.mNativePtr), newAction, nativeGetFlags(this.mNativePtr), nativeGetEdgeFlags(this.mNativePtr), nativeGetMetaState(this.mNativePtr), nativeGetButtonState(this.mNativePtr), nativeGetXOffset(this.mNativePtr), nativeGetYOffset(this.mNativePtr), nativeGetXPrecision(this.mNativePtr), nativeGetYPrecision(this.mNativePtr), nativeGetDownTimeNanos(this.mNativePtr), eventTimeNanos, newPointerCount, pp, pc);
                } else {
                    nativeAddBatch(ev.mNativePtr, eventTimeNanos, pc, AXIS_X);
                }
                h += TOOL_TYPE_FINGER;
            }
        }
        return ev;
    }

    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("MotionEvent { action=").append(actionToString(getAction()));
        msg.append(", actionButton=").append(buttonStateToString(getActionButton()));
        int pointerCount = getPointerCount();
        for (int i = AXIS_X; i < pointerCount; i += TOOL_TYPE_FINGER) {
            msg.append(", id[").append(i).append("]=").append(getPointerId(i));
            msg.append(", x[").append(i).append("]=").append(getX(i));
            msg.append(", y[").append(i).append("]=").append(getY(i));
            msg.append(", toolType[").append(i).append("]=").append(toolTypeToString(getToolType(i)));
        }
        msg.append(", buttonState=").append(buttonStateToString(getButtonState()));
        msg.append(", metaState=").append(KeyEvent.metaStateToString(getMetaState()));
        msg.append(", flags=0x").append(Integer.toHexString(getFlags()));
        msg.append(", edgeFlags=0x").append(Integer.toHexString(getEdgeFlags()));
        msg.append(", pointerCount=").append(pointerCount);
        msg.append(", historySize=").append(getHistorySize());
        msg.append(", eventTime=").append(getEventTime());
        msg.append(", downTime=").append(getDownTime());
        msg.append(", deviceId=").append(getDeviceId());
        msg.append(", source=0x").append(Integer.toHexString(getSource()));
        msg.append(" }");
        return msg.toString();
    }

    public static String actionToString(int action) {
        switch (action) {
            case AXIS_X /*0*/:
                return "ACTION_DOWN";
            case TOOL_TYPE_FINGER /*1*/:
                return "ACTION_UP";
            case TOOL_TYPE_STYLUS /*2*/:
                return "ACTION_MOVE";
            case TOOL_TYPE_MOUSE /*3*/:
                return "ACTION_CANCEL";
            case TOOL_TYPE_ERASER /*4*/:
                return "ACTION_OUTSIDE";
            case AXIS_TOOL_MINOR /*7*/:
                return "ACTION_HOVER_MOVE";
            case EDGE_RIGHT /*8*/:
                return "ACTION_SCROLL";
            case AXIS_VSCROLL /*9*/:
                return "ACTION_HOVER_ENTER";
            case MAX_RECYCLED /*10*/:
                return "ACTION_HOVER_EXIT";
            case AXIS_Z /*11*/:
                return "ACTION_BUTTON_PRESS";
            case AXIS_RX /*12*/:
                return "ACTION_BUTTON_RELEASE";
            default:
                int index = (ACTION_POINTER_INDEX_MASK & action) >> EDGE_RIGHT;
                switch (action & ACTION_MASK) {
                    case AXIS_TOUCH_MINOR /*5*/:
                        return "ACTION_POINTER_DOWN(" + index + ")";
                    case AXIS_TOOL_MAJOR /*6*/:
                        return "ACTION_POINTER_UP(" + index + ")";
                    default:
                        return Integer.toString(action);
                }
        }
    }

    public static String axisToString(int axis) {
        String symbolicName = nativeAxisToString(axis);
        return symbolicName != null ? LABEL_PREFIX + symbolicName : Integer.toString(axis);
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
            return Integer.parseInt(symbolicName, MAX_RECYCLED);
        } catch (NumberFormatException e) {
            return INVALID_POINTER_ID;
        }
    }

    public static String buttonStateToString(int buttonState) {
        if (buttonState == 0) {
            return "0";
        }
        StringBuilder result = null;
        int i = AXIS_X;
        while (buttonState != 0) {
            boolean isSet;
            if ((buttonState & TOOL_TYPE_FINGER) != 0) {
                isSet = true;
            } else {
                isSet = false;
            }
            buttonState >>>= TOOL_TYPE_FINGER;
            if (isSet) {
                String name = BUTTON_SYMBOLIC_NAMES[i];
                if (result != null) {
                    result.append('|');
                    result.append(name);
                } else if (buttonState == 0) {
                    return name;
                } else {
                    result = new StringBuilder(name);
                }
            }
            i += TOOL_TYPE_FINGER;
        }
        return result.toString();
    }

    public static String toolTypeToString(int toolType) {
        String symbolicName = (String) TOOL_TYPE_SYMBOLIC_NAMES.get(toolType);
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
        setAction(TOOL_TYPE_MOUSE);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(TOOL_TYPE_FINGER);
        nativeWriteToParcel(this.mNativePtr, out);
    }
}
