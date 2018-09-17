package android.renderscript;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.usb.UsbConstants;
import android.os.Trace;
import android.renderscript.Element.DataKind;
import android.renderscript.Element.DataType;
import android.renderscript.Type.Builder;
import android.renderscript.Type.CubemapFace;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;
import android.view.Surface;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class Allocation extends BaseObj {
    private static final /* synthetic */ int[] -android-graphics-Bitmap$ConfigSwitchesValues = null;
    private static final int MAX_NUMBER_IO_INPUT_ALLOC = 16;
    public static final int USAGE_GRAPHICS_CONSTANTS = 8;
    public static final int USAGE_GRAPHICS_RENDER_TARGET = 16;
    public static final int USAGE_GRAPHICS_TEXTURE = 2;
    public static final int USAGE_GRAPHICS_VERTEX = 4;
    public static final int USAGE_IO_INPUT = 32;
    public static final int USAGE_IO_OUTPUT = 64;
    public static final int USAGE_SCRIPT = 1;
    public static final int USAGE_SHARED = 128;
    static HashMap<Long, Allocation> mAllocationMap;
    static Options mBitmapOptions;
    Allocation mAdaptedAllocation;
    boolean mAutoPadding;
    Bitmap mBitmap;
    OnBufferAvailableListener mBufferNotifier;
    private ByteBuffer mByteBuffer;
    private long mByteBufferStride;
    int mCurrentCount;
    int mCurrentDimX;
    int mCurrentDimY;
    int mCurrentDimZ;
    private Surface mGetSurfaceSurface;
    MipmapControl mMipmapControl;
    boolean mOwningType;
    boolean mReadAllowed;
    int[] mSelectedArray;
    CubemapFace mSelectedFace;
    int mSelectedLOD;
    int mSelectedX;
    int mSelectedY;
    int mSelectedZ;
    int mSize;
    long mTimeStamp;
    Type mType;
    int mUsage;
    boolean mWriteAllowed;

    public enum MipmapControl {
        ;
        
        int mID;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.Allocation.MipmapControl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.Allocation.MipmapControl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.Allocation.MipmapControl.<clinit>():void");
        }

        private MipmapControl(int id) {
            this.mID = id;
        }
    }

    public interface OnBufferAvailableListener {
        void onBufferAvailable(Allocation allocation);
    }

    private static /* synthetic */ int[] -getandroid-graphics-Bitmap$ConfigSwitchesValues() {
        if (-android-graphics-Bitmap$ConfigSwitchesValues != null) {
            return -android-graphics-Bitmap$ConfigSwitchesValues;
        }
        int[] iArr = new int[Config.values().length];
        try {
            iArr[Config.ALPHA_8.ordinal()] = USAGE_SCRIPT;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Config.ARGB_4444.ordinal()] = USAGE_GRAPHICS_TEXTURE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Config.ARGB_8888.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Config.RGB_565.ordinal()] = USAGE_GRAPHICS_VERTEX;
        } catch (NoSuchFieldError e4) {
        }
        -android-graphics-Bitmap$ConfigSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.Allocation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.Allocation.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.Allocation.<clinit>():void");
    }

    private DataType validateObjectIsPrimitiveArray(Object d, boolean checkType) {
        Class c = d.getClass();
        if (c.isArray()) {
            Class cmp = c.getComponentType();
            if (!cmp.isPrimitive()) {
                throw new RSIllegalArgumentException("Object passed is not an Array of primitives.");
            } else if (cmp == Long.TYPE) {
                if (!checkType) {
                    return DataType.SIGNED_64;
                }
                validateIsInt64();
                return this.mType.mElement.mType;
            } else if (cmp == Integer.TYPE) {
                if (!checkType) {
                    return DataType.SIGNED_32;
                }
                validateIsInt32();
                return this.mType.mElement.mType;
            } else if (cmp == Short.TYPE) {
                if (!checkType) {
                    return DataType.SIGNED_16;
                }
                validateIsInt16OrFloat16();
                return this.mType.mElement.mType;
            } else if (cmp == Byte.TYPE) {
                if (!checkType) {
                    return DataType.SIGNED_8;
                }
                validateIsInt8();
                return this.mType.mElement.mType;
            } else if (cmp == Float.TYPE) {
                if (checkType) {
                    validateIsFloat32();
                }
                return DataType.FLOAT_32;
            } else if (cmp == Double.TYPE) {
                if (checkType) {
                    validateIsFloat64();
                }
                return DataType.FLOAT_64;
            } else {
                throw new RSIllegalArgumentException("Parameter of type " + cmp.getSimpleName() + "[] is not compatible with data type " + this.mType.mElement.mType.name() + " of allocation");
            }
        }
        throw new RSIllegalArgumentException("Object passed is not an array of primitives.");
    }

    private long getIDSafe() {
        if (this.mAdaptedAllocation != null) {
            return this.mAdaptedAllocation.getID(this.mRS);
        }
        return getID(this.mRS);
    }

    public Element getElement() {
        return this.mType.getElement();
    }

    public int getUsage() {
        return this.mUsage;
    }

    public MipmapControl getMipmap() {
        return this.mMipmapControl;
    }

    public void setAutoPadding(boolean useAutoPadding) {
        this.mAutoPadding = useAutoPadding;
    }

    public int getBytesSize() {
        if (this.mType.mDimYuv != 0) {
            return (int) Math.ceil(((double) (this.mType.getCount() * this.mType.getElement().getBytesSize())) * 1.5d);
        }
        return this.mType.getCount() * this.mType.getElement().getBytesSize();
    }

    private void updateCacheInfo(Type t) {
        this.mCurrentDimX = t.getX();
        this.mCurrentDimY = t.getY();
        this.mCurrentDimZ = t.getZ();
        this.mCurrentCount = this.mCurrentDimX;
        if (this.mCurrentDimY > USAGE_SCRIPT) {
            this.mCurrentCount *= this.mCurrentDimY;
        }
        if (this.mCurrentDimZ > USAGE_SCRIPT) {
            this.mCurrentCount *= this.mCurrentDimZ;
        }
    }

    private void setBitmap(Bitmap b) {
        this.mBitmap = b;
    }

    Allocation(long id, RenderScript rs, Type t, int usage) {
        super(id, rs);
        this.mOwningType = false;
        this.mTimeStamp = -1;
        this.mReadAllowed = true;
        this.mWriteAllowed = true;
        this.mAutoPadding = false;
        this.mSelectedFace = CubemapFace.POSITIVE_X;
        this.mGetSurfaceSurface = null;
        this.mByteBuffer = null;
        this.mByteBufferStride = -1;
        if ((usage & Color.YELLOW) != 0) {
            throw new RSIllegalArgumentException("Unknown usage specified.");
        }
        if ((usage & USAGE_IO_INPUT) != 0) {
            this.mWriteAllowed = false;
            if ((usage & -36) != 0) {
                throw new RSIllegalArgumentException("Invalid usage combination.");
            }
        }
        this.mType = t;
        this.mUsage = usage;
        if (t != null) {
            this.mSize = this.mType.getCount() * this.mType.getElement().getBytesSize();
            updateCacheInfo(t);
        }
        try {
            Method method = RenderScript.registerNativeAllocation;
            Object obj = RenderScript.sRuntime;
            Object[] objArr = new Object[USAGE_SCRIPT];
            objArr[0] = Integer.valueOf(this.mSize);
            method.invoke(obj, objArr);
            this.guard.open("destroy");
        } catch (Exception e) {
            Log.e("RenderScript_jni", "Couldn't invoke registerNativeAllocation:" + e);
            throw new RSRuntimeException("Couldn't invoke registerNativeAllocation:" + e);
        }
    }

    Allocation(long id, RenderScript rs, Type t, boolean owningType, int usage, MipmapControl mips) {
        this(id, rs, t, usage);
        this.mOwningType = owningType;
        this.mMipmapControl = mips;
    }

    protected void finalize() throws Throwable {
        Method method = RenderScript.registerNativeFree;
        Object obj = RenderScript.sRuntime;
        Object[] objArr = new Object[USAGE_SCRIPT];
        objArr[0] = Integer.valueOf(this.mSize);
        method.invoke(obj, objArr);
        super.finalize();
    }

    private void validateIsInt64() {
        if (this.mType.mElement.mType != DataType.SIGNED_64 && this.mType.mElement.mType != DataType.UNSIGNED_64) {
            throw new RSIllegalArgumentException("64 bit integer source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsInt32() {
        if (this.mType.mElement.mType != DataType.SIGNED_32 && this.mType.mElement.mType != DataType.UNSIGNED_32) {
            throw new RSIllegalArgumentException("32 bit integer source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsInt16OrFloat16() {
        if (this.mType.mElement.mType != DataType.SIGNED_16 && this.mType.mElement.mType != DataType.UNSIGNED_16 && this.mType.mElement.mType != DataType.FLOAT_16) {
            throw new RSIllegalArgumentException("16 bit integer source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsInt8() {
        if (this.mType.mElement.mType != DataType.SIGNED_8 && this.mType.mElement.mType != DataType.UNSIGNED_8) {
            throw new RSIllegalArgumentException("8 bit integer source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsFloat32() {
        if (this.mType.mElement.mType != DataType.FLOAT_32) {
            throw new RSIllegalArgumentException("32 bit float source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsFloat64() {
        if (this.mType.mElement.mType != DataType.FLOAT_64) {
            throw new RSIllegalArgumentException("64 bit float source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsObject() {
        if (this.mType.mElement.mType != DataType.RS_ELEMENT && this.mType.mElement.mType != DataType.RS_TYPE && this.mType.mElement.mType != DataType.RS_ALLOCATION && this.mType.mElement.mType != DataType.RS_SAMPLER && this.mType.mElement.mType != DataType.RS_SCRIPT && this.mType.mElement.mType != DataType.RS_MESH && this.mType.mElement.mType != DataType.RS_PROGRAM_FRAGMENT && this.mType.mElement.mType != DataType.RS_PROGRAM_VERTEX && this.mType.mElement.mType != DataType.RS_PROGRAM_RASTER && this.mType.mElement.mType != DataType.RS_PROGRAM_STORE) {
            throw new RSIllegalArgumentException("Object source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    void updateFromNative() {
        super.updateFromNative();
        long typeID = this.mRS.nAllocationGetType(getID(this.mRS));
        if (typeID != 0) {
            this.mType = new Type(typeID, this.mRS);
            this.mType.updateFromNative();
            updateCacheInfo(this.mType);
        }
    }

    public Type getType() {
        return this.mType;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void syncAll(int srcLocation) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "syncAll");
            switch (srcLocation) {
                case USAGE_SCRIPT /*1*/:
                case USAGE_GRAPHICS_TEXTURE /*2*/:
                    if ((this.mUsage & USAGE_SHARED) != 0) {
                        copyFrom(this.mBitmap);
                        break;
                    }
                    break;
                case USAGE_GRAPHICS_VERTEX /*4*/:
                case USAGE_GRAPHICS_CONSTANTS /*8*/:
                    break;
                case USAGE_SHARED /*128*/:
                    if ((this.mUsage & USAGE_SHARED) != 0) {
                        copyTo(this.mBitmap);
                        break;
                    }
                    break;
                default:
                    throw new RSIllegalArgumentException("Source must be exactly one usage type.");
            }
            this.mRS.validate();
            this.mRS.nAllocationSyncAll(getIDSafe(), srcLocation);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void ioSend() {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "ioSend");
            if ((this.mUsage & USAGE_IO_OUTPUT) == 0) {
                throw new RSIllegalArgumentException("Can only send buffer if IO_OUTPUT usage specified.");
            }
            this.mRS.validate();
            this.mRS.nAllocationIoSend(getID(this.mRS));
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void ioReceive() {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "ioReceive");
            if ((this.mUsage & USAGE_IO_INPUT) == 0) {
                throw new RSIllegalArgumentException("Can only receive if IO_INPUT usage specified.");
            }
            this.mRS.validate();
            this.mTimeStamp = this.mRS.nAllocationIoReceive(getID(this.mRS));
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copyFrom(BaseObj[] d) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copyFrom");
            this.mRS.validate();
            validateIsObject();
            if (d.length != this.mCurrentCount) {
                throw new RSIllegalArgumentException("Array size mismatch, allocation sizeX = " + this.mCurrentCount + ", array length = " + d.length);
            }
            int ct;
            if (RenderScript.sPointerSize == USAGE_GRAPHICS_CONSTANTS) {
                Object i = new long[(d.length * USAGE_GRAPHICS_VERTEX)];
                for (ct = 0; ct < d.length; ct += USAGE_SCRIPT) {
                    i[ct * USAGE_GRAPHICS_VERTEX] = d[ct].getID(this.mRS);
                }
                copy1DRangeFromUnchecked(0, this.mCurrentCount, i);
            } else {
                int[] i2 = new int[d.length];
                for (ct = 0; ct < d.length; ct += USAGE_SCRIPT) {
                    i2[ct] = (int) d[ct].getID(this.mRS);
                }
                copy1DRangeFromUnchecked(0, this.mCurrentCount, i2);
            }
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        } catch (Throwable th) {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    private void validateBitmapFormat(Bitmap b) {
        Config bc = b.getConfig();
        if (bc == null) {
            throw new RSIllegalArgumentException("Bitmap has an unsupported format for this operation");
        }
        switch (-getandroid-graphics-Bitmap$ConfigSwitchesValues()[bc.ordinal()]) {
            case USAGE_SCRIPT /*1*/:
                if (this.mType.getElement().mKind != DataKind.PIXEL_A) {
                    throw new RSIllegalArgumentException("Allocation kind is " + this.mType.getElement().mKind + ", type " + this.mType.getElement().mType + " of " + this.mType.getElement().getBytesSize() + " bytes, passed bitmap was " + bc);
                }
            case USAGE_GRAPHICS_TEXTURE /*2*/:
                if (this.mType.getElement().mKind != DataKind.PIXEL_RGBA || this.mType.getElement().getBytesSize() != USAGE_GRAPHICS_TEXTURE) {
                    throw new RSIllegalArgumentException("Allocation kind is " + this.mType.getElement().mKind + ", type " + this.mType.getElement().mType + " of " + this.mType.getElement().getBytesSize() + " bytes, passed bitmap was " + bc);
                }
            case Engine.DEFAULT_STREAM /*3*/:
                if (this.mType.getElement().mKind != DataKind.PIXEL_RGBA || this.mType.getElement().getBytesSize() != USAGE_GRAPHICS_VERTEX) {
                    throw new RSIllegalArgumentException("Allocation kind is " + this.mType.getElement().mKind + ", type " + this.mType.getElement().mType + " of " + this.mType.getElement().getBytesSize() + " bytes, passed bitmap was " + bc);
                }
            case USAGE_GRAPHICS_VERTEX /*4*/:
                if (this.mType.getElement().mKind != DataKind.PIXEL_RGB || this.mType.getElement().getBytesSize() != USAGE_GRAPHICS_TEXTURE) {
                    throw new RSIllegalArgumentException("Allocation kind is " + this.mType.getElement().mKind + ", type " + this.mType.getElement().mType + " of " + this.mType.getElement().getBytesSize() + " bytes, passed bitmap was " + bc);
                }
            default:
        }
    }

    private void validateBitmapSize(Bitmap b) {
        if (this.mCurrentDimX != b.getWidth() || this.mCurrentDimY != b.getHeight()) {
            throw new RSIllegalArgumentException("Cannot update allocation from bitmap, sizes mismatch");
        }
    }

    private void copyFromUnchecked(Object array, DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copyFromUnchecked");
            this.mRS.validate();
            if (this.mCurrentDimZ > 0) {
                copy3DRangeFromUnchecked(0, 0, 0, this.mCurrentDimX, this.mCurrentDimY, this.mCurrentDimZ, array, dt, arrayLen);
            } else if (this.mCurrentDimY > 0) {
                copy2DRangeFromUnchecked(0, 0, this.mCurrentDimX, this.mCurrentDimY, array, dt, arrayLen);
            } else {
                copy1DRangeFromUnchecked(0, this.mCurrentCount, array, dt, arrayLen);
            }
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        } catch (Throwable th) {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copyFromUnchecked(Object array) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copyFromUnchecked");
            copyFromUnchecked(array, validateObjectIsPrimitiveArray(array, false), Array.getLength(array));
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copyFromUnchecked(int[] d) {
        copyFromUnchecked(d, DataType.SIGNED_32, d.length);
    }

    public void copyFromUnchecked(short[] d) {
        copyFromUnchecked(d, DataType.SIGNED_16, d.length);
    }

    public void copyFromUnchecked(byte[] d) {
        copyFromUnchecked(d, DataType.SIGNED_8, d.length);
    }

    public void copyFromUnchecked(float[] d) {
        copyFromUnchecked(d, DataType.FLOAT_32, d.length);
    }

    public void copyFrom(Object array) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copyFrom");
            copyFromUnchecked(array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copyFrom(int[] d) {
        validateIsInt32();
        copyFromUnchecked(d, DataType.SIGNED_32, d.length);
    }

    public void copyFrom(short[] d) {
        validateIsInt16OrFloat16();
        copyFromUnchecked(d, DataType.SIGNED_16, d.length);
    }

    public void copyFrom(byte[] d) {
        validateIsInt8();
        copyFromUnchecked(d, DataType.SIGNED_8, d.length);
    }

    public void copyFrom(float[] d) {
        validateIsFloat32();
        copyFromUnchecked(d, DataType.FLOAT_32, d.length);
    }

    public void copyFrom(Bitmap b) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copyFrom");
            this.mRS.validate();
            if (b.getConfig() == null) {
                Bitmap newBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Config.ARGB_8888);
                new Canvas(newBitmap).drawBitmap(b, 0.0f, 0.0f, null);
                copyFrom(newBitmap);
                return;
            }
            validateBitmapSize(b);
            validateBitmapFormat(b);
            this.mRS.nAllocationCopyFromBitmap(getID(this.mRS), b);
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copyFrom(Allocation a) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copyFrom");
            this.mRS.validate();
            if (this.mType.equals(a.getType())) {
                copy2DRangeFrom(0, 0, this.mCurrentDimX, this.mCurrentDimY, a, 0, 0);
                return;
            }
            throw new RSIllegalArgumentException("Types of allocations must match.");
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void setFromFieldPacker(int xoff, FieldPacker fp) {
        this.mRS.validate();
        int eSize = this.mType.mElement.getBytesSize();
        byte[] data = fp.getData();
        int data_length = fp.getPos();
        int count = data_length / eSize;
        if (eSize * count != data_length) {
            throw new RSIllegalArgumentException("Field packer length " + data_length + " not divisible by element size " + eSize + ".");
        }
        copy1DRangeFromUnchecked(xoff, count, data);
    }

    public void setFromFieldPacker(int xoff, int component_number, FieldPacker fp) {
        setFromFieldPacker(xoff, 0, 0, component_number, fp);
    }

    public void setFromFieldPacker(int xoff, int yoff, int zoff, int component_number, FieldPacker fp) {
        this.mRS.validate();
        if (component_number >= this.mType.mElement.mElements.length) {
            throw new RSIllegalArgumentException("Component_number " + component_number + " out of range.");
        } else if (xoff < 0) {
            throw new RSIllegalArgumentException("Offset x must be >= 0.");
        } else if (yoff < 0) {
            throw new RSIllegalArgumentException("Offset y must be >= 0.");
        } else if (zoff < 0) {
            throw new RSIllegalArgumentException("Offset z must be >= 0.");
        } else {
            byte[] data = fp.getData();
            int data_length = fp.getPos();
            int eSize = this.mType.mElement.mElements[component_number].getBytesSize() * this.mType.mElement.mArraySizes[component_number];
            if (data_length != eSize) {
                throw new RSIllegalArgumentException("Field packer sizelength " + data_length + " does not match component size " + eSize + ".");
            }
            this.mRS.nAllocationElementData(getIDSafe(), xoff, yoff, zoff, this.mSelectedLOD, component_number, data, data_length);
        }
    }

    private void data1DChecks(int off, int count, int len, int dataSize, boolean usePadding) {
        this.mRS.validate();
        if (off < 0) {
            throw new RSIllegalArgumentException("Offset must be >= 0.");
        } else if (count < USAGE_SCRIPT) {
            throw new RSIllegalArgumentException("Count must be >= 1.");
        } else if (off + count > this.mCurrentCount) {
            throw new RSIllegalArgumentException("Overflow, Available count " + this.mCurrentCount + ", got " + count + " at offset " + off + ".");
        } else if (usePadding) {
            if (len < (dataSize / USAGE_GRAPHICS_VERTEX) * 3) {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
        } else if (len < dataSize) {
            throw new RSIllegalArgumentException("Array too small for allocation type.");
        }
    }

    public void generateMipmaps() {
        this.mRS.nAllocationGenerateMipmaps(getID(this.mRS));
    }

    private void copy1DRangeFromUnchecked(int off, int count, Object array, DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy1DRangeFromUnchecked");
            int dataSize = this.mType.mElement.getBytesSize() * count;
            boolean usePadding = false;
            if (this.mAutoPadding && this.mType.getElement().getVectorSize() == 3) {
                usePadding = true;
            }
            data1DChecks(off, count, arrayLen * dt.mSize, dataSize, usePadding);
            int i = off;
            int i2 = count;
            Object obj = array;
            int i3 = dataSize;
            DataType dataType = dt;
            this.mRS.nAllocationData1D(getIDSafe(), i, this.mSelectedLOD, i2, obj, i3, dataType, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy1DRangeFromUnchecked(int off, int count, Object array) {
        copy1DRangeFromUnchecked(off, count, array, validateObjectIsPrimitiveArray(array, false), Array.getLength(array));
    }

    public void copy1DRangeFromUnchecked(int off, int count, int[] d) {
        copy1DRangeFromUnchecked(off, count, d, DataType.SIGNED_32, d.length);
    }

    public void copy1DRangeFromUnchecked(int off, int count, short[] d) {
        copy1DRangeFromUnchecked(off, count, d, DataType.SIGNED_16, d.length);
    }

    public void copy1DRangeFromUnchecked(int off, int count, byte[] d) {
        copy1DRangeFromUnchecked(off, count, d, DataType.SIGNED_8, d.length);
    }

    public void copy1DRangeFromUnchecked(int off, int count, float[] d) {
        copy1DRangeFromUnchecked(off, count, d, DataType.FLOAT_32, d.length);
    }

    public void copy1DRangeFrom(int off, int count, Object array) {
        copy1DRangeFromUnchecked(off, count, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public void copy1DRangeFrom(int off, int count, int[] d) {
        validateIsInt32();
        copy1DRangeFromUnchecked(off, count, d, DataType.SIGNED_32, d.length);
    }

    public void copy1DRangeFrom(int off, int count, short[] d) {
        validateIsInt16OrFloat16();
        copy1DRangeFromUnchecked(off, count, d, DataType.SIGNED_16, d.length);
    }

    public void copy1DRangeFrom(int off, int count, byte[] d) {
        validateIsInt8();
        copy1DRangeFromUnchecked(off, count, d, DataType.SIGNED_8, d.length);
    }

    public void copy1DRangeFrom(int off, int count, float[] d) {
        validateIsFloat32();
        copy1DRangeFromUnchecked(off, count, d, DataType.FLOAT_32, d.length);
    }

    public void copy1DRangeFrom(int off, int count, Allocation data, int dataOff) {
        Trace.traceBegin(Trace.TRACE_TAG_RS, "copy1DRangeFrom");
        RenderScript renderScript = this.mRS;
        long iDSafe = getIDSafe();
        int i = this.mSelectedLOD;
        int i2 = this.mSelectedFace.mID;
        RenderScript renderScript2 = this.mRS;
        int i3 = off;
        int i4 = count;
        int i5 = dataOff;
        renderScript.nAllocationData2D(iDSafe, i3, 0, i, i2, i4, (int) USAGE_SCRIPT, data.getID(renderScript2), i5, 0, data.mSelectedLOD, data.mSelectedFace.mID);
        Trace.traceEnd(Trace.TRACE_TAG_RS);
    }

    private void validate2DRange(int xoff, int yoff, int w, int h) {
        if (this.mAdaptedAllocation == null) {
            if (xoff < 0 || yoff < 0) {
                throw new RSIllegalArgumentException("Offset cannot be negative.");
            } else if (h < 0 || w < 0) {
                throw new RSIllegalArgumentException("Height or width cannot be negative.");
            } else if (xoff + w > this.mCurrentDimX || yoff + h > this.mCurrentDimY) {
                throw new RSIllegalArgumentException("Updated region larger than allocation.");
            }
        }
    }

    void copy2DRangeFromUnchecked(int xoff, int yoff, int w, int h, Object array, DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy2DRangeFromUnchecked");
            this.mRS.validate();
            validate2DRange(xoff, yoff, w, h);
            int dataSize = (this.mType.mElement.getBytesSize() * w) * h;
            boolean usePadding = false;
            int sizeBytes = arrayLen * dt.mSize;
            if (this.mAutoPadding && this.mType.getElement().getVectorSize() == 3) {
                if ((dataSize / USAGE_GRAPHICS_VERTEX) * 3 > sizeBytes) {
                    throw new RSIllegalArgumentException("Array too small for allocation type.");
                }
                usePadding = true;
                sizeBytes = dataSize;
            } else if (dataSize > sizeBytes) {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
            this.mRS.nAllocationData2D(getIDSafe(), xoff, yoff, this.mSelectedLOD, this.mSelectedFace.mID, w, h, array, sizeBytes, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, Object array) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy2DRangeFrom");
            copy2DRangeFromUnchecked(xoff, yoff, w, h, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, byte[] data) {
        validateIsInt8();
        copy2DRangeFromUnchecked(xoff, yoff, w, h, data, DataType.SIGNED_8, data.length);
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, short[] data) {
        validateIsInt16OrFloat16();
        copy2DRangeFromUnchecked(xoff, yoff, w, h, data, DataType.SIGNED_16, data.length);
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, int[] data) {
        validateIsInt32();
        copy2DRangeFromUnchecked(xoff, yoff, w, h, data, DataType.SIGNED_32, data.length);
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, float[] data) {
        validateIsFloat32();
        copy2DRangeFromUnchecked(xoff, yoff, w, h, data, DataType.FLOAT_32, data.length);
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, Allocation data, int dataXoff, int dataYoff) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy2DRangeFrom");
            this.mRS.validate();
            validate2DRange(xoff, yoff, w, h);
            RenderScript renderScript = this.mRS;
            long iDSafe = getIDSafe();
            int i = this.mSelectedLOD;
            int i2 = this.mSelectedFace.mID;
            RenderScript renderScript2 = this.mRS;
            int i3 = xoff;
            int i4 = yoff;
            int i5 = w;
            int i6 = h;
            int i7 = dataXoff;
            int i8 = dataYoff;
            renderScript.nAllocationData2D(iDSafe, i3, i4, i, i2, i5, i6, data.getID(renderScript2), i7, i8, data.mSelectedLOD, data.mSelectedFace.mID);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy2DRangeFrom(int xoff, int yoff, Bitmap data) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy2DRangeFrom");
            this.mRS.validate();
            if (data.getConfig() == null) {
                Bitmap newBitmap = Bitmap.createBitmap(data.getWidth(), data.getHeight(), Config.ARGB_8888);
                new Canvas(newBitmap).drawBitmap(data, 0.0f, 0.0f, null);
                copy2DRangeFrom(xoff, yoff, newBitmap);
                return;
            }
            validateBitmapFormat(data);
            validate2DRange(xoff, yoff, data.getWidth(), data.getHeight());
            this.mRS.nAllocationData2D(getIDSafe(), xoff, yoff, this.mSelectedLOD, this.mSelectedFace.mID, data);
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    private void validate3DRange(int xoff, int yoff, int zoff, int w, int h, int d) {
        if (this.mAdaptedAllocation == null) {
            if (xoff < 0 || yoff < 0 || zoff < 0) {
                throw new RSIllegalArgumentException("Offset cannot be negative.");
            } else if (h < 0 || w < 0 || d < 0) {
                throw new RSIllegalArgumentException("Height or width cannot be negative.");
            } else {
                if (xoff + w <= this.mCurrentDimX && yoff + h <= this.mCurrentDimY) {
                    if (zoff + d <= this.mCurrentDimZ) {
                        return;
                    }
                }
                throw new RSIllegalArgumentException("Updated region larger than allocation.");
            }
        }
    }

    private void copy3DRangeFromUnchecked(int xoff, int yoff, int zoff, int w, int h, int d, Object array, DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy3DRangeFromUnchecked");
            this.mRS.validate();
            validate3DRange(xoff, yoff, zoff, w, h, d);
            int dataSize = ((this.mType.mElement.getBytesSize() * w) * h) * d;
            boolean usePadding = false;
            int sizeBytes = arrayLen * dt.mSize;
            if (this.mAutoPadding && this.mType.getElement().getVectorSize() == 3) {
                if ((dataSize / USAGE_GRAPHICS_VERTEX) * 3 > sizeBytes) {
                    throw new RSIllegalArgumentException("Array too small for allocation type.");
                }
                usePadding = true;
                sizeBytes = dataSize;
            } else if (dataSize > sizeBytes) {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
            int i = xoff;
            int i2 = yoff;
            int i3 = zoff;
            int i4 = w;
            int i5 = h;
            int i6 = d;
            Object obj = array;
            DataType dataType = dt;
            this.mRS.nAllocationData3D(getIDSafe(), i, i2, i3, this.mSelectedLOD, i4, i5, i6, obj, sizeBytes, dataType, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy3DRangeFrom(int xoff, int yoff, int zoff, int w, int h, int d, Object array) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy3DRangeFrom");
            copy3DRangeFromUnchecked(xoff, yoff, zoff, w, h, d, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy3DRangeFrom(int xoff, int yoff, int zoff, int w, int h, int d, Allocation data, int dataXoff, int dataYoff, int dataZoff) {
        this.mRS.validate();
        validate3DRange(xoff, yoff, zoff, w, h, d);
        RenderScript renderScript = this.mRS;
        long iDSafe = getIDSafe();
        int i = this.mSelectedLOD;
        RenderScript renderScript2 = this.mRS;
        int i2 = xoff;
        int i3 = yoff;
        int i4 = zoff;
        int i5 = w;
        int i6 = h;
        int i7 = d;
        int i8 = dataXoff;
        int i9 = dataYoff;
        int i10 = dataZoff;
        renderScript.nAllocationData3D(iDSafe, i2, i3, i4, i, i5, i6, i7, data.getID(renderScript2), i8, i9, i10, data.mSelectedLOD);
    }

    public void copyTo(Bitmap b) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copyTo");
            this.mRS.validate();
            validateBitmapFormat(b);
            validateBitmapSize(b);
            this.mRS.nAllocationCopyToBitmap(getID(this.mRS), b);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    private void copyTo(Object array, DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copyTo");
            this.mRS.validate();
            boolean usePadding = false;
            if (this.mAutoPadding && this.mType.getElement().getVectorSize() == 3) {
                usePadding = true;
            }
            if (usePadding) {
                if (dt.mSize * arrayLen < (this.mSize / USAGE_GRAPHICS_VERTEX) * 3) {
                    throw new RSIllegalArgumentException("Size of output array cannot be smaller than size of allocation.");
                }
            } else if (dt.mSize * arrayLen < this.mSize) {
                throw new RSIllegalArgumentException("Size of output array cannot be smaller than size of allocation.");
            }
            this.mRS.nAllocationRead(getID(this.mRS), array, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copyTo(Object array) {
        copyTo(array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public void copyTo(byte[] d) {
        validateIsInt8();
        copyTo(d, DataType.SIGNED_8, d.length);
    }

    public void copyTo(short[] d) {
        validateIsInt16OrFloat16();
        copyTo(d, DataType.SIGNED_16, d.length);
    }

    public void copyTo(int[] d) {
        validateIsInt32();
        copyTo(d, DataType.SIGNED_32, d.length);
    }

    public void copyTo(float[] d) {
        validateIsFloat32();
        copyTo(d, DataType.FLOAT_32, d.length);
    }

    public void copyToFieldPacker(int xoff, int yoff, int zoff, int component_number, FieldPacker fp) {
        this.mRS.validate();
        if (component_number >= this.mType.mElement.mElements.length) {
            throw new RSIllegalArgumentException("Component_number " + component_number + " out of range.");
        } else if (xoff < 0) {
            throw new RSIllegalArgumentException("Offset x must be >= 0.");
        } else if (yoff < 0) {
            throw new RSIllegalArgumentException("Offset y must be >= 0.");
        } else if (zoff < 0) {
            throw new RSIllegalArgumentException("Offset z must be >= 0.");
        } else {
            byte[] data = fp.getData();
            int data_length = data.length;
            int eSize = this.mType.mElement.mElements[component_number].getBytesSize() * this.mType.mElement.mArraySizes[component_number];
            if (data_length != eSize) {
                throw new RSIllegalArgumentException("Field packer sizelength " + data_length + " does not match component size " + eSize + ".");
            }
            this.mRS.nAllocationElementRead(getIDSafe(), xoff, yoff, zoff, this.mSelectedLOD, component_number, data, data_length);
        }
    }

    public synchronized void resize(int dimX) {
        if (this.mRS.getApplicationContext().getApplicationInfo().targetSdkVersion >= 21) {
            throw new RSRuntimeException("Resize is not allowed in API 21+.");
        } else if (this.mType.getY() > 0 || this.mType.getZ() > 0 || this.mType.hasFaces() || this.mType.hasMipmaps()) {
            throw new RSInvalidStateException("Resize only support for 1D allocations at this time.");
        } else {
            this.mRS.nAllocationResize1D(getID(this.mRS), dimX);
            this.mRS.finish();
            long typeID = this.mRS.nAllocationGetType(getID(this.mRS));
            this.mType.setID(0);
            this.mType = new Type(typeID, this.mRS);
            this.mType.updateFromNative();
            updateCacheInfo(this.mType);
        }
    }

    private void copy1DRangeToUnchecked(int off, int count, Object array, DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy1DRangeToUnchecked");
            int dataSize = this.mType.mElement.getBytesSize() * count;
            boolean usePadding = false;
            if (this.mAutoPadding && this.mType.getElement().getVectorSize() == 3) {
                usePadding = true;
            }
            data1DChecks(off, count, arrayLen * dt.mSize, dataSize, usePadding);
            int i = off;
            int i2 = count;
            Object obj = array;
            int i3 = dataSize;
            DataType dataType = dt;
            this.mRS.nAllocationRead1D(getIDSafe(), i, this.mSelectedLOD, i2, obj, i3, dataType, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy1DRangeToUnchecked(int off, int count, Object array) {
        copy1DRangeToUnchecked(off, count, array, validateObjectIsPrimitiveArray(array, false), Array.getLength(array));
    }

    public void copy1DRangeToUnchecked(int off, int count, int[] d) {
        copy1DRangeToUnchecked(off, count, d, DataType.SIGNED_32, d.length);
    }

    public void copy1DRangeToUnchecked(int off, int count, short[] d) {
        copy1DRangeToUnchecked(off, count, d, DataType.SIGNED_16, d.length);
    }

    public void copy1DRangeToUnchecked(int off, int count, byte[] d) {
        copy1DRangeToUnchecked(off, count, d, DataType.SIGNED_8, d.length);
    }

    public void copy1DRangeToUnchecked(int off, int count, float[] d) {
        copy1DRangeToUnchecked(off, count, d, DataType.FLOAT_32, d.length);
    }

    public void copy1DRangeTo(int off, int count, Object array) {
        copy1DRangeToUnchecked(off, count, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public void copy1DRangeTo(int off, int count, int[] d) {
        validateIsInt32();
        copy1DRangeToUnchecked(off, count, d, DataType.SIGNED_32, d.length);
    }

    public void copy1DRangeTo(int off, int count, short[] d) {
        validateIsInt16OrFloat16();
        copy1DRangeToUnchecked(off, count, d, DataType.SIGNED_16, d.length);
    }

    public void copy1DRangeTo(int off, int count, byte[] d) {
        validateIsInt8();
        copy1DRangeToUnchecked(off, count, d, DataType.SIGNED_8, d.length);
    }

    public void copy1DRangeTo(int off, int count, float[] d) {
        validateIsFloat32();
        copy1DRangeToUnchecked(off, count, d, DataType.FLOAT_32, d.length);
    }

    void copy2DRangeToUnchecked(int xoff, int yoff, int w, int h, Object array, DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy2DRangeToUnchecked");
            this.mRS.validate();
            validate2DRange(xoff, yoff, w, h);
            int dataSize = (this.mType.mElement.getBytesSize() * w) * h;
            boolean usePadding = false;
            int sizeBytes = arrayLen * dt.mSize;
            if (this.mAutoPadding && this.mType.getElement().getVectorSize() == 3) {
                if ((dataSize / USAGE_GRAPHICS_VERTEX) * 3 > sizeBytes) {
                    throw new RSIllegalArgumentException("Array too small for allocation type.");
                }
                usePadding = true;
                sizeBytes = dataSize;
            } else if (dataSize > sizeBytes) {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
            this.mRS.nAllocationRead2D(getIDSafe(), xoff, yoff, this.mSelectedLOD, this.mSelectedFace.mID, w, h, array, sizeBytes, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, Object array) {
        copy2DRangeToUnchecked(xoff, yoff, w, h, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, byte[] data) {
        validateIsInt8();
        copy2DRangeToUnchecked(xoff, yoff, w, h, data, DataType.SIGNED_8, data.length);
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, short[] data) {
        validateIsInt16OrFloat16();
        copy2DRangeToUnchecked(xoff, yoff, w, h, data, DataType.SIGNED_16, data.length);
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, int[] data) {
        validateIsInt32();
        copy2DRangeToUnchecked(xoff, yoff, w, h, data, DataType.SIGNED_32, data.length);
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, float[] data) {
        validateIsFloat32();
        copy2DRangeToUnchecked(xoff, yoff, w, h, data, DataType.FLOAT_32, data.length);
    }

    private void copy3DRangeToUnchecked(int xoff, int yoff, int zoff, int w, int h, int d, Object array, DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "copy3DRangeToUnchecked");
            this.mRS.validate();
            validate3DRange(xoff, yoff, zoff, w, h, d);
            int dataSize = ((this.mType.mElement.getBytesSize() * w) * h) * d;
            boolean usePadding = false;
            int sizeBytes = arrayLen * dt.mSize;
            if (this.mAutoPadding && this.mType.getElement().getVectorSize() == 3) {
                if ((dataSize / USAGE_GRAPHICS_VERTEX) * 3 > sizeBytes) {
                    throw new RSIllegalArgumentException("Array too small for allocation type.");
                }
                usePadding = true;
                sizeBytes = dataSize;
            } else if (dataSize > sizeBytes) {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
            int i = xoff;
            int i2 = yoff;
            int i3 = zoff;
            int i4 = w;
            int i5 = h;
            int i6 = d;
            Object obj = array;
            DataType dataType = dt;
            this.mRS.nAllocationRead3D(getIDSafe(), i, i2, i3, this.mSelectedLOD, i4, i5, i6, obj, sizeBytes, dataType, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public void copy3DRangeTo(int xoff, int yoff, int zoff, int w, int h, int d, Object array) {
        copy3DRangeToUnchecked(xoff, yoff, zoff, w, h, d, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public static Allocation createTyped(RenderScript rs, Type type, MipmapControl mips, int usage) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "createTyped");
            rs.validate();
            if (type.getID(rs) == 0) {
                throw new RSInvalidStateException("Bad Type");
            }
            long id = rs.nAllocationCreateTyped(type.getID(rs), mips.mID, usage, 0);
            if (id == 0) {
                throw new RSRuntimeException("Allocation creation failed.");
            }
            Allocation allocation = new Allocation(id, rs, type, false, usage, mips);
            return allocation;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public static Allocation createTyped(RenderScript rs, Type type, int usage) {
        return createTyped(rs, type, MipmapControl.MIPMAP_NONE, usage);
    }

    public static Allocation createTyped(RenderScript rs, Type type) {
        return createTyped(rs, type, MipmapControl.MIPMAP_NONE, USAGE_SCRIPT);
    }

    public static Allocation createSized(RenderScript rs, Element e, int count, int usage) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "createSized");
            rs.validate();
            Builder b = new Builder(rs, e);
            b.setX(count);
            Type t = b.create();
            long id = rs.nAllocationCreateTyped(t.getID(rs), MipmapControl.MIPMAP_NONE.mID, usage, 0);
            if (id == 0) {
                throw new RSRuntimeException("Allocation creation failed.");
            }
            Allocation allocation = new Allocation(id, rs, t, true, usage, MipmapControl.MIPMAP_NONE);
            return allocation;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public static Allocation createSized(RenderScript rs, Element e, int count) {
        return createSized(rs, e, count, USAGE_SCRIPT);
    }

    static Element elementFromBitmap(RenderScript rs, Bitmap b) {
        Config bc = b.getConfig();
        if (bc == Config.ALPHA_8) {
            return Element.A_8(rs);
        }
        if (bc == Config.ARGB_4444) {
            return Element.RGBA_4444(rs);
        }
        if (bc == Config.ARGB_8888) {
            return Element.RGBA_8888(rs);
        }
        if (bc == Config.RGB_565) {
            return Element.RGB_565(rs);
        }
        throw new RSInvalidStateException("Bad bitmap type: " + bc);
    }

    static Type typeFromBitmap(RenderScript rs, Bitmap b, MipmapControl mip) {
        Builder tb = new Builder(rs, elementFromBitmap(rs, b));
        tb.setX(b.getWidth());
        tb.setY(b.getHeight());
        tb.setMipmaps(mip == MipmapControl.MIPMAP_FULL);
        return tb.create();
    }

    public static Allocation createFromBitmap(RenderScript rs, Bitmap b, MipmapControl mips, int usage) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "createFromBitmap");
            rs.validate();
            if (b.getConfig() != null) {
                Type t = typeFromBitmap(rs, b, mips);
                long id;
                if (mips == MipmapControl.MIPMAP_NONE && t.getElement().isCompatible(Element.RGBA_8888(rs)) && usage == 131) {
                    id = rs.nAllocationCreateBitmapBackedAllocation(t.getID(rs), mips.mID, b, usage);
                    if (id == 0) {
                        throw new RSRuntimeException("Load failed.");
                    }
                    Allocation alloc = new Allocation(id, rs, t, true, usage, mips);
                    alloc.setBitmap(b);
                    Trace.traceEnd(Trace.TRACE_TAG_RS);
                    return alloc;
                }
                id = rs.nAllocationCreateFromBitmap(t.getID(rs), mips.mID, b, usage);
                if (id == 0) {
                    throw new RSRuntimeException("Load failed.");
                }
                Allocation allocation = new Allocation(id, rs, t, true, usage, mips);
                Trace.traceEnd(Trace.TRACE_TAG_RS);
                return allocation;
            } else if ((usage & USAGE_SHARED) != 0) {
                throw new RSIllegalArgumentException("USAGE_SHARED cannot be used with a Bitmap that has a null config.");
            } else {
                Bitmap newBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Config.ARGB_8888);
                new Canvas(newBitmap).drawBitmap(b, 0.0f, 0.0f, null);
                Allocation createFromBitmap = createFromBitmap(rs, newBitmap, mips, usage);
                return createFromBitmap;
            }
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    public ByteBuffer getByteBuffer() {
        if (this.mType.hasFaces()) {
            throw new RSInvalidStateException("Cubemap is not supported for getByteBuffer().");
        } else if (this.mType.getYuv() == 17 || this.mType.getYuv() == ImageFormat.YV12 || this.mType.getYuv() == 35) {
            throw new RSInvalidStateException("YUV format is not supported for getByteBuffer().");
        } else {
            if (this.mByteBuffer == null || (this.mUsage & USAGE_IO_INPUT) != 0) {
                long[] stride = new long[USAGE_SCRIPT];
                this.mByteBuffer = this.mRS.nAllocationGetByteBuffer(getID(this.mRS), stride, this.mType.getX() * this.mType.getElement().getBytesSize(), this.mType.getY(), this.mType.getZ());
                this.mByteBufferStride = stride[0];
            }
            if ((this.mUsage & USAGE_IO_INPUT) != 0) {
                return this.mByteBuffer.asReadOnlyBuffer();
            }
            return this.mByteBuffer;
        }
    }

    public static Allocation[] createAllocations(RenderScript rs, Type t, int usage, int numAlloc) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "createAllocations");
            rs.validate();
            if (t.getID(rs) == 0) {
                throw new RSInvalidStateException("Bad Type");
            }
            Allocation[] mAllocationArray = new Allocation[numAlloc];
            mAllocationArray[0] = createTyped(rs, t, usage);
            if ((usage & USAGE_IO_INPUT) != 0) {
                if (numAlloc > USAGE_GRAPHICS_RENDER_TARGET) {
                    throw new RSIllegalArgumentException("Exceeds the max number of Allocations allowed: 16");
                }
                mAllocationArray[0].setupBufferQueue(numAlloc);
            }
            for (int i = USAGE_SCRIPT; i < numAlloc; i += USAGE_SCRIPT) {
                mAllocationArray[i] = createFromAllocation(rs, mAllocationArray[0]);
            }
            return mAllocationArray;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    static Allocation createFromAllocation(RenderScript rs, Allocation alloc) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RS, "createFromAllcation");
            rs.validate();
            if (alloc.getID(rs) == 0) {
                throw new RSInvalidStateException("Bad input Allocation");
            }
            Type type = alloc.getType();
            int usage = alloc.getUsage();
            MipmapControl mips = alloc.getMipmap();
            long id = rs.nAllocationCreateTyped(type.getID(rs), mips.mID, usage, 0);
            if (id == 0) {
                throw new RSRuntimeException("Allocation creation failed.");
            }
            Allocation outAlloc = new Allocation(id, rs, type, false, usage, mips);
            if ((usage & USAGE_IO_INPUT) != 0) {
                outAlloc.shareBufferQueue(alloc);
            }
            Trace.traceEnd(Trace.TRACE_TAG_RS);
            return outAlloc;
        } catch (Throwable th) {
            Trace.traceEnd(Trace.TRACE_TAG_RS);
        }
    }

    void setupBufferQueue(int numAlloc) {
        this.mRS.validate();
        if ((this.mUsage & USAGE_IO_INPUT) == 0) {
            throw new RSInvalidStateException("Allocation is not USAGE_IO_INPUT.");
        }
        this.mRS.nAllocationSetupBufferQueue(getID(this.mRS), numAlloc);
    }

    void shareBufferQueue(Allocation alloc) {
        this.mRS.validate();
        if ((this.mUsage & USAGE_IO_INPUT) == 0) {
            throw new RSInvalidStateException("Allocation is not USAGE_IO_INPUT.");
        }
        this.mGetSurfaceSurface = alloc.getSurface();
        this.mRS.nAllocationShareBufferQueue(getID(this.mRS), alloc.getID(this.mRS));
    }

    public long getStride() {
        if (this.mByteBufferStride == -1) {
            getByteBuffer();
        }
        return this.mByteBufferStride;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public Surface getSurface() {
        if ((this.mUsage & USAGE_IO_INPUT) == 0) {
            throw new RSInvalidStateException("Allocation is not a surface texture.");
        }
        if (this.mGetSurfaceSurface == null) {
            this.mGetSurfaceSurface = this.mRS.nAllocationGetSurface(getID(this.mRS));
        }
        return this.mGetSurfaceSurface;
    }

    public void setSurface(Surface sur) {
        this.mRS.validate();
        if ((this.mUsage & USAGE_IO_OUTPUT) == 0) {
            throw new RSInvalidStateException("Allocation is not USAGE_IO_OUTPUT.");
        }
        this.mRS.nAllocationSetSurface(getID(this.mRS), sur);
    }

    public static Allocation createFromBitmap(RenderScript rs, Bitmap b) {
        if (rs.getApplicationContext().getApplicationInfo().targetSdkVersion >= 18) {
            return createFromBitmap(rs, b, MipmapControl.MIPMAP_NONE, ScriptIntrinsicBLAS.NON_UNIT);
        }
        return createFromBitmap(rs, b, MipmapControl.MIPMAP_NONE, USAGE_GRAPHICS_TEXTURE);
    }

    public static Allocation createCubemapFromBitmap(RenderScript rs, Bitmap b, MipmapControl mips, int usage) {
        rs.validate();
        int height = b.getHeight();
        int width = b.getWidth();
        if (width % 6 != 0) {
            throw new RSIllegalArgumentException("Cubemap height must be multiple of 6");
        } else if (width / 6 != height) {
            throw new RSIllegalArgumentException("Only square cube map faces supported");
        } else {
            if (((height + -1) & height) == 0) {
                Element e = elementFromBitmap(rs, b);
                Builder tb = new Builder(rs, e);
                tb.setX(height);
                tb.setY(height);
                tb.setFaces(true);
                tb.setMipmaps(mips == MipmapControl.MIPMAP_FULL);
                Type t = tb.create();
                long id = rs.nAllocationCubeCreateFromBitmap(t.getID(rs), mips.mID, b, usage);
                if (id != 0) {
                    return new Allocation(id, rs, t, true, usage, mips);
                }
                throw new RSRuntimeException("Load failed for bitmap " + b + " element " + e);
            }
            throw new RSIllegalArgumentException("Only power of 2 cube faces supported");
        }
    }

    public static Allocation createCubemapFromBitmap(RenderScript rs, Bitmap b) {
        return createCubemapFromBitmap(rs, b, MipmapControl.MIPMAP_NONE, USAGE_GRAPHICS_TEXTURE);
    }

    public static Allocation createCubemapFromCubeFaces(RenderScript rs, Bitmap xpos, Bitmap xneg, Bitmap ypos, Bitmap yneg, Bitmap zpos, Bitmap zneg, MipmapControl mips, int usage) {
        int height = xpos.getHeight();
        if (xpos.getWidth() == height && xneg.getWidth() == height && xneg.getHeight() == height && ypos.getWidth() == height && ypos.getHeight() == height && yneg.getWidth() == height && yneg.getHeight() == height && zpos.getWidth() == height && zpos.getHeight() == height && zneg.getWidth() == height && zneg.getHeight() == height) {
            if (((height + -1) & height) == 0) {
                Builder tb = new Builder(rs, elementFromBitmap(rs, xpos));
                tb.setX(height);
                tb.setY(height);
                tb.setFaces(true);
                tb.setMipmaps(mips == MipmapControl.MIPMAP_FULL);
                Allocation cubemap = createTyped(rs, tb.create(), mips, usage);
                AllocationAdapter adapter = AllocationAdapter.create2D(rs, cubemap);
                adapter.setFace(CubemapFace.POSITIVE_X);
                adapter.copyFrom(xpos);
                adapter.setFace(CubemapFace.NEGATIVE_X);
                adapter.copyFrom(xneg);
                adapter.setFace(CubemapFace.POSITIVE_Y);
                adapter.copyFrom(ypos);
                adapter.setFace(CubemapFace.NEGATIVE_Y);
                adapter.copyFrom(yneg);
                adapter.setFace(CubemapFace.POSITIVE_Z);
                adapter.copyFrom(zpos);
                adapter.setFace(CubemapFace.NEGATIVE_Z);
                adapter.copyFrom(zneg);
                return cubemap;
            }
            throw new RSIllegalArgumentException("Only power of 2 cube faces supported");
        }
        throw new RSIllegalArgumentException("Only square cube map faces supported");
    }

    public static Allocation createCubemapFromCubeFaces(RenderScript rs, Bitmap xpos, Bitmap xneg, Bitmap ypos, Bitmap yneg, Bitmap zpos, Bitmap zneg) {
        return createCubemapFromCubeFaces(rs, xpos, xneg, ypos, yneg, zpos, zneg, MipmapControl.MIPMAP_NONE, USAGE_GRAPHICS_TEXTURE);
    }

    public static Allocation createFromBitmapResource(RenderScript rs, Resources res, int id, MipmapControl mips, int usage) {
        rs.validate();
        if ((usage & UsbConstants.USB_CLASS_WIRELESS_CONTROLLER) != 0) {
            throw new RSIllegalArgumentException("Unsupported usage specified.");
        }
        Bitmap b = BitmapFactory.decodeResource(res, id);
        Allocation alloc = createFromBitmap(rs, b, mips, usage);
        b.recycle();
        return alloc;
    }

    public static Allocation createFromBitmapResource(RenderScript rs, Resources res, int id) {
        if (rs.getApplicationContext().getApplicationInfo().targetSdkVersion >= 18) {
            return createFromBitmapResource(rs, res, id, MipmapControl.MIPMAP_NONE, 3);
        }
        return createFromBitmapResource(rs, res, id, MipmapControl.MIPMAP_NONE, USAGE_GRAPHICS_TEXTURE);
    }

    public static Allocation createFromString(RenderScript rs, String str, int usage) {
        rs.validate();
        try {
            byte[] allocArray = str.getBytes("UTF-8");
            Allocation alloc = createSized(rs, Element.U8(rs), allocArray.length, usage);
            alloc.copyFrom(allocArray);
            return alloc;
        } catch (Exception e) {
            throw new RSRuntimeException("Could not convert string to utf-8.");
        }
    }

    public void setOnBufferAvailableListener(OnBufferAvailableListener callback) {
        synchronized (mAllocationMap) {
            mAllocationMap.put(new Long(getID(this.mRS)), this);
            this.mBufferNotifier = callback;
        }
    }

    static void sendBufferNotification(long id) {
        synchronized (mAllocationMap) {
            Allocation a = (Allocation) mAllocationMap.get(new Long(id));
            if (!(a == null || a.mBufferNotifier == null)) {
                a.mBufferNotifier.onBufferAvailable(a);
            }
        }
    }

    public void destroy() {
        if ((this.mUsage & USAGE_IO_OUTPUT) != 0) {
            setSurface(null);
        }
        if (this.mType != null && this.mOwningType) {
            this.mType.destroy();
        }
        super.destroy();
    }
}
