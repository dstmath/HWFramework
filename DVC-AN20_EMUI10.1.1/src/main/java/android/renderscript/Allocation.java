package android.renderscript;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Trace;
import android.renderscript.Element;
import android.renderscript.Type;
import android.util.Log;
import android.view.Surface;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class Allocation extends BaseObj {
    private static final int MAX_NUMBER_IO_INPUT_ALLOC = 16;
    public static final int USAGE_GRAPHICS_CONSTANTS = 8;
    public static final int USAGE_GRAPHICS_RENDER_TARGET = 16;
    public static final int USAGE_GRAPHICS_TEXTURE = 2;
    public static final int USAGE_GRAPHICS_VERTEX = 4;
    public static final int USAGE_IO_INPUT = 32;
    public static final int USAGE_IO_OUTPUT = 64;
    public static final int USAGE_SCRIPT = 1;
    public static final int USAGE_SHARED = 128;
    static HashMap<Long, Allocation> mAllocationMap = new HashMap<>();
    static BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
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
    Type.CubemapFace mSelectedFace;
    int mSelectedLOD;
    int mSelectedX;
    int mSelectedY;
    int mSelectedZ;
    int mSize;
    long mTimeStamp;
    Type mType;
    int mUsage;
    boolean mWriteAllowed;

    public interface OnBufferAvailableListener {
        void onBufferAvailable(Allocation allocation);
    }

    static {
        mBitmapOptions.inScaled = false;
    }

    private Element.DataType validateObjectIsPrimitiveArray(Object d, boolean checkType) {
        Class c = d.getClass();
        if (c.isArray()) {
            Class cmp = c.getComponentType();
            if (!cmp.isPrimitive()) {
                throw new RSIllegalArgumentException("Object passed is not an Array of primitives.");
            } else if (cmp == Long.TYPE) {
                if (!checkType) {
                    return Element.DataType.SIGNED_64;
                }
                validateIsInt64();
                return this.mType.mElement.mType;
            } else if (cmp == Integer.TYPE) {
                if (!checkType) {
                    return Element.DataType.SIGNED_32;
                }
                validateIsInt32();
                return this.mType.mElement.mType;
            } else if (cmp == Short.TYPE) {
                if (!checkType) {
                    return Element.DataType.SIGNED_16;
                }
                validateIsInt16OrFloat16();
                return this.mType.mElement.mType;
            } else if (cmp == Byte.TYPE) {
                if (!checkType) {
                    return Element.DataType.SIGNED_8;
                }
                validateIsInt8();
                return this.mType.mElement.mType;
            } else if (cmp == Float.TYPE) {
                if (checkType) {
                    validateIsFloat32();
                }
                return Element.DataType.FLOAT_32;
            } else if (cmp == Double.TYPE) {
                if (checkType) {
                    validateIsFloat64();
                }
                return Element.DataType.FLOAT_64;
            } else {
                throw new RSIllegalArgumentException("Parameter of type " + cmp.getSimpleName() + "[] is not compatible with data type " + this.mType.mElement.mType.name() + " of allocation");
            }
        } else {
            throw new RSIllegalArgumentException("Object passed is not an array of primitives.");
        }
    }

    public enum MipmapControl {
        MIPMAP_NONE(0),
        MIPMAP_FULL(1),
        MIPMAP_ON_SYNC_TO_TEXTURE(2);
        
        int mID;

        private MipmapControl(int id) {
            this.mID = id;
        }
    }

    private long getIDSafe() {
        Allocation allocation = this.mAdaptedAllocation;
        if (allocation != null) {
            return allocation.getID(this.mRS);
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
        int i = this.mCurrentDimY;
        if (i > 1) {
            this.mCurrentCount *= i;
        }
        int i2 = this.mCurrentDimZ;
        if (i2 > 1) {
            this.mCurrentCount *= i2;
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
        this.mSelectedFace = Type.CubemapFace.POSITIVE_X;
        this.mGetSurfaceSurface = null;
        this.mByteBuffer = null;
        this.mByteBufferStride = -1;
        if ((usage & -256) == 0) {
            if ((usage & 32) != 0) {
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
                RenderScript.registerNativeAllocation.invoke(RenderScript.sRuntime, Integer.valueOf(this.mSize));
                this.guard.open("destroy");
            } catch (Exception e) {
                Log.e("RenderScript_jni", "Couldn't invoke registerNativeAllocation:" + e);
                throw new RSRuntimeException("Couldn't invoke registerNativeAllocation:" + e);
            }
        } else {
            throw new RSIllegalArgumentException("Unknown usage specified.");
        }
    }

    Allocation(long id, RenderScript rs, Type t, boolean owningType, int usage, MipmapControl mips) {
        this(id, rs, t, usage);
        this.mOwningType = owningType;
        this.mMipmapControl = mips;
    }

    /* access modifiers changed from: protected */
    @Override // android.renderscript.BaseObj
    public void finalize() throws Throwable {
        RenderScript.registerNativeFree.invoke(RenderScript.sRuntime, Integer.valueOf(this.mSize));
        super.finalize();
    }

    private void validateIsInt64() {
        if (this.mType.mElement.mType != Element.DataType.SIGNED_64 && this.mType.mElement.mType != Element.DataType.UNSIGNED_64) {
            throw new RSIllegalArgumentException("64 bit integer source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsInt32() {
        if (this.mType.mElement.mType != Element.DataType.SIGNED_32 && this.mType.mElement.mType != Element.DataType.UNSIGNED_32) {
            throw new RSIllegalArgumentException("32 bit integer source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsInt16OrFloat16() {
        if (this.mType.mElement.mType != Element.DataType.SIGNED_16 && this.mType.mElement.mType != Element.DataType.UNSIGNED_16 && this.mType.mElement.mType != Element.DataType.FLOAT_16) {
            throw new RSIllegalArgumentException("16 bit integer source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsInt8() {
        if (this.mType.mElement.mType != Element.DataType.SIGNED_8 && this.mType.mElement.mType != Element.DataType.UNSIGNED_8) {
            throw new RSIllegalArgumentException("8 bit integer source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsFloat32() {
        if (this.mType.mElement.mType != Element.DataType.FLOAT_32) {
            throw new RSIllegalArgumentException("32 bit float source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsFloat64() {
        if (this.mType.mElement.mType != Element.DataType.FLOAT_64) {
            throw new RSIllegalArgumentException("64 bit float source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    private void validateIsObject() {
        if (this.mType.mElement.mType != Element.DataType.RS_ELEMENT && this.mType.mElement.mType != Element.DataType.RS_TYPE && this.mType.mElement.mType != Element.DataType.RS_ALLOCATION && this.mType.mElement.mType != Element.DataType.RS_SAMPLER && this.mType.mElement.mType != Element.DataType.RS_SCRIPT && this.mType.mElement.mType != Element.DataType.RS_MESH && this.mType.mElement.mType != Element.DataType.RS_PROGRAM_FRAGMENT && this.mType.mElement.mType != Element.DataType.RS_PROGRAM_VERTEX && this.mType.mElement.mType != Element.DataType.RS_PROGRAM_RASTER && this.mType.mElement.mType != Element.DataType.RS_PROGRAM_STORE) {
            throw new RSIllegalArgumentException("Object source does not match allocation type " + this.mType.mElement.mType);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.renderscript.BaseObj
    public void updateFromNative() {
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

    public void syncAll(int srcLocation) {
        try {
            Trace.traceBegin(32768, "syncAll");
            if (srcLocation == 1 || srcLocation == 2) {
                if ((this.mUsage & 128) != 0) {
                    copyFrom(this.mBitmap);
                }
            } else if (srcLocation != 4 && srcLocation != 8) {
                if (srcLocation != 128) {
                    throw new RSIllegalArgumentException("Source must be exactly one usage type.");
                } else if ((this.mUsage & 128) != 0) {
                    copyTo(this.mBitmap);
                }
            }
            this.mRS.validate();
            this.mRS.nAllocationSyncAll(getIDSafe(), srcLocation);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void ioSend() {
        try {
            Trace.traceBegin(32768, "ioSend");
            if ((this.mUsage & 64) != 0) {
                this.mRS.validate();
                this.mRS.nAllocationIoSend(getID(this.mRS));
                return;
            }
            throw new RSIllegalArgumentException("Can only send buffer if IO_OUTPUT usage specified.");
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void ioReceive() {
        try {
            Trace.traceBegin(32768, "ioReceive");
            if ((this.mUsage & 32) != 0) {
                this.mRS.validate();
                this.mTimeStamp = this.mRS.nAllocationIoReceive(getID(this.mRS));
                return;
            }
            throw new RSIllegalArgumentException("Can only receive if IO_INPUT usage specified.");
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copyFrom(BaseObj[] d) {
        try {
            Trace.traceBegin(32768, "copyFrom");
            this.mRS.validate();
            validateIsObject();
            if (d.length == this.mCurrentCount) {
                if (RenderScript.sPointerSize == 8) {
                    long[] i = new long[(d.length * 4)];
                    for (int ct = 0; ct < d.length; ct++) {
                        i[ct * 4] = d[ct].getID(this.mRS);
                    }
                    copy1DRangeFromUnchecked(0, this.mCurrentCount, i);
                } else {
                    int[] i2 = new int[d.length];
                    for (int ct2 = 0; ct2 < d.length; ct2++) {
                        i2[ct2] = (int) d[ct2].getID(this.mRS);
                    }
                    copy1DRangeFromUnchecked(0, this.mCurrentCount, i2);
                }
                return;
            }
            throw new RSIllegalArgumentException("Array size mismatch, allocation sizeX = " + this.mCurrentCount + ", array length = " + d.length);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    private void validateBitmapFormat(Bitmap b) {
        Bitmap.Config bc = b.getConfig();
        if (bc != null) {
            int i = AnonymousClass1.$SwitchMap$android$graphics$Bitmap$Config[bc.ordinal()];
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i == 4) {
                            if (this.mType.getElement().mKind != Element.DataKind.PIXEL_RGBA || this.mType.getElement().getBytesSize() != 2) {
                                throw new RSIllegalArgumentException("Allocation kind is " + this.mType.getElement().mKind + ", type " + this.mType.getElement().mType + " of " + this.mType.getElement().getBytesSize() + " bytes, passed bitmap was " + bc);
                            }
                        }
                    } else if (this.mType.getElement().mKind != Element.DataKind.PIXEL_RGB || this.mType.getElement().getBytesSize() != 2) {
                        throw new RSIllegalArgumentException("Allocation kind is " + this.mType.getElement().mKind + ", type " + this.mType.getElement().mType + " of " + this.mType.getElement().getBytesSize() + " bytes, passed bitmap was " + bc);
                    }
                } else if (this.mType.getElement().mKind != Element.DataKind.PIXEL_RGBA || this.mType.getElement().getBytesSize() != 4) {
                    throw new RSIllegalArgumentException("Allocation kind is " + this.mType.getElement().mKind + ", type " + this.mType.getElement().mType + " of " + this.mType.getElement().getBytesSize() + " bytes, passed bitmap was " + bc);
                }
            } else if (this.mType.getElement().mKind != Element.DataKind.PIXEL_A) {
                throw new RSIllegalArgumentException("Allocation kind is " + this.mType.getElement().mKind + ", type " + this.mType.getElement().mType + " of " + this.mType.getElement().getBytesSize() + " bytes, passed bitmap was " + bc);
            }
        } else {
            throw new RSIllegalArgumentException("Bitmap has an unsupported format for this operation");
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.renderscript.Allocation$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$graphics$Bitmap$Config = new int[Bitmap.Config.values().length];

        static {
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Bitmap.Config.ALPHA_8.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Bitmap.Config.ARGB_8888.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Bitmap.Config.RGB_565.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Bitmap.Config.ARGB_4444.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private void validateBitmapSize(Bitmap b) {
        if (this.mCurrentDimX != b.getWidth() || this.mCurrentDimY != b.getHeight()) {
            throw new RSIllegalArgumentException("Cannot update allocation from bitmap, sizes mismatch");
        }
    }

    private void copyFromUnchecked(Object array, Element.DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(32768, "copyFromUnchecked");
            this.mRS.validate();
            if (this.mCurrentDimZ > 0) {
                copy3DRangeFromUnchecked(0, 0, 0, this.mCurrentDimX, this.mCurrentDimY, this.mCurrentDimZ, array, dt, arrayLen);
            } else if (this.mCurrentDimY > 0) {
                copy2DRangeFromUnchecked(0, 0, this.mCurrentDimX, this.mCurrentDimY, array, dt, arrayLen);
            } else {
                copy1DRangeFromUnchecked(0, this.mCurrentCount, array, dt, arrayLen);
            }
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copyFromUnchecked(Object array) {
        try {
            Trace.traceBegin(32768, "copyFromUnchecked");
            copyFromUnchecked(array, validateObjectIsPrimitiveArray(array, false), Array.getLength(array));
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copyFromUnchecked(int[] d) {
        copyFromUnchecked(d, Element.DataType.SIGNED_32, d.length);
    }

    public void copyFromUnchecked(short[] d) {
        copyFromUnchecked(d, Element.DataType.SIGNED_16, d.length);
    }

    public void copyFromUnchecked(byte[] d) {
        copyFromUnchecked(d, Element.DataType.SIGNED_8, d.length);
    }

    public void copyFromUnchecked(float[] d) {
        copyFromUnchecked(d, Element.DataType.FLOAT_32, d.length);
    }

    public void copyFrom(Object array) {
        try {
            Trace.traceBegin(32768, "copyFrom");
            copyFromUnchecked(array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copyFrom(int[] d) {
        validateIsInt32();
        copyFromUnchecked(d, Element.DataType.SIGNED_32, d.length);
    }

    public void copyFrom(short[] d) {
        validateIsInt16OrFloat16();
        copyFromUnchecked(d, Element.DataType.SIGNED_16, d.length);
    }

    public void copyFrom(byte[] d) {
        validateIsInt8();
        copyFromUnchecked(d, Element.DataType.SIGNED_8, d.length);
    }

    public void copyFrom(float[] d) {
        validateIsFloat32();
        copyFromUnchecked(d, Element.DataType.FLOAT_32, d.length);
    }

    public void copyFrom(Bitmap b) {
        try {
            Trace.traceBegin(32768, "copyFrom");
            this.mRS.validate();
            if (b.getConfig() == null) {
                Bitmap newBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
                new Canvas(newBitmap).drawBitmap(b, 0.0f, 0.0f, (Paint) null);
                copyFrom(newBitmap);
                return;
            }
            validateBitmapSize(b);
            validateBitmapFormat(b);
            this.mRS.nAllocationCopyFromBitmap(getID(this.mRS), b);
            Trace.traceEnd(32768);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copyFrom(Allocation a) {
        try {
            Trace.traceBegin(32768, "copyFrom");
            this.mRS.validate();
            if (this.mType.equals(a.getType())) {
                copy2DRangeFrom(0, 0, this.mCurrentDimX, this.mCurrentDimY, a, 0, 0);
                return;
            }
            throw new RSIllegalArgumentException("Types of allocations must match.");
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void setFromFieldPacker(int xoff, FieldPacker fp) {
        this.mRS.validate();
        int eSize = this.mType.mElement.getBytesSize();
        byte[] data = fp.getData();
        int data_length = fp.getPos();
        int count = data_length / eSize;
        if (eSize * count == data_length) {
            copy1DRangeFromUnchecked(xoff, count, data);
            return;
        }
        throw new RSIllegalArgumentException("Field packer length " + data_length + " not divisible by element size " + eSize + ".");
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
        } else if (zoff >= 0) {
            byte[] data = fp.getData();
            int data_length = fp.getPos();
            int eSize = this.mType.mElement.mElements[component_number].getBytesSize() * this.mType.mElement.mArraySizes[component_number];
            if (data_length == eSize) {
                this.mRS.nAllocationElementData(getIDSafe(), xoff, yoff, zoff, this.mSelectedLOD, component_number, data, data_length);
                return;
            }
            throw new RSIllegalArgumentException("Field packer sizelength " + data_length + " does not match component size " + eSize + ".");
        } else {
            throw new RSIllegalArgumentException("Offset z must be >= 0.");
        }
    }

    private void data1DChecks(int off, int count, int len, int dataSize, boolean usePadding) {
        this.mRS.validate();
        if (off < 0) {
            throw new RSIllegalArgumentException("Offset must be >= 0.");
        } else if (count < 1) {
            throw new RSIllegalArgumentException("Count must be >= 1.");
        } else if (off + count > this.mCurrentCount) {
            throw new RSIllegalArgumentException("Overflow, Available count " + this.mCurrentCount + ", got " + count + " at offset " + off + ".");
        } else if (usePadding) {
            if (len < (dataSize / 4) * 3) {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
        } else if (len < dataSize) {
            throw new RSIllegalArgumentException("Array too small for allocation type.");
        }
    }

    public void generateMipmaps() {
        this.mRS.nAllocationGenerateMipmaps(getID(this.mRS));
    }

    private void copy1DRangeFromUnchecked(int off, int count, Object array, Element.DataType dt, int arrayLen) {
        boolean usePadding;
        try {
            Trace.traceBegin(32768, "copy1DRangeFromUnchecked");
            int dataSize = this.mType.mElement.getBytesSize() * count;
            if (!this.mAutoPadding || this.mType.getElement().getVectorSize() != 3) {
                usePadding = false;
            } else {
                usePadding = true;
            }
            data1DChecks(off, count, arrayLen * dt.mSize, dataSize, usePadding);
            this.mRS.nAllocationData1D(getIDSafe(), off, this.mSelectedLOD, count, array, dataSize, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copy1DRangeFromUnchecked(int off, int count, Object array) {
        copy1DRangeFromUnchecked(off, count, array, validateObjectIsPrimitiveArray(array, false), Array.getLength(array));
    }

    public void copy1DRangeFromUnchecked(int off, int count, int[] d) {
        copy1DRangeFromUnchecked(off, count, d, Element.DataType.SIGNED_32, d.length);
    }

    public void copy1DRangeFromUnchecked(int off, int count, short[] d) {
        copy1DRangeFromUnchecked(off, count, d, Element.DataType.SIGNED_16, d.length);
    }

    public void copy1DRangeFromUnchecked(int off, int count, byte[] d) {
        copy1DRangeFromUnchecked(off, count, d, Element.DataType.SIGNED_8, d.length);
    }

    public void copy1DRangeFromUnchecked(int off, int count, float[] d) {
        copy1DRangeFromUnchecked(off, count, d, Element.DataType.FLOAT_32, d.length);
    }

    public void copy1DRangeFrom(int off, int count, Object array) {
        copy1DRangeFromUnchecked(off, count, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public void copy1DRangeFrom(int off, int count, int[] d) {
        validateIsInt32();
        copy1DRangeFromUnchecked(off, count, d, Element.DataType.SIGNED_32, d.length);
    }

    public void copy1DRangeFrom(int off, int count, short[] d) {
        validateIsInt16OrFloat16();
        copy1DRangeFromUnchecked(off, count, d, Element.DataType.SIGNED_16, d.length);
    }

    public void copy1DRangeFrom(int off, int count, byte[] d) {
        validateIsInt8();
        copy1DRangeFromUnchecked(off, count, d, Element.DataType.SIGNED_8, d.length);
    }

    public void copy1DRangeFrom(int off, int count, float[] d) {
        validateIsFloat32();
        copy1DRangeFromUnchecked(off, count, d, Element.DataType.FLOAT_32, d.length);
    }

    public void copy1DRangeFrom(int off, int count, Allocation data, int dataOff) {
        Trace.traceBegin(32768, "copy1DRangeFrom");
        this.mRS.nAllocationData2D(getIDSafe(), off, 0, this.mSelectedLOD, this.mSelectedFace.mID, count, 1, data.getID(this.mRS), dataOff, 0, data.mSelectedLOD, data.mSelectedFace.mID);
        Trace.traceEnd(32768);
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

    /* access modifiers changed from: package-private */
    public void copy2DRangeFromUnchecked(int xoff, int yoff, int w, int h, Object array, Element.DataType dt, int arrayLen) {
        boolean usePadding;
        int sizeBytes;
        try {
            Trace.traceBegin(32768, "copy2DRangeFromUnchecked");
            this.mRS.validate();
            validate2DRange(xoff, yoff, w, h);
            int dataSize = this.mType.mElement.getBytesSize() * w * h;
            int sizeBytes2 = dt.mSize * arrayLen;
            if (!this.mAutoPadding || this.mType.getElement().getVectorSize() != 3) {
                if (dataSize <= sizeBytes2) {
                    usePadding = false;
                    sizeBytes = sizeBytes2;
                } else {
                    throw new RSIllegalArgumentException("Array too small for allocation type.");
                }
            } else if ((dataSize / 4) * 3 <= sizeBytes2) {
                usePadding = true;
                sizeBytes = dataSize;
            } else {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
            this.mRS.nAllocationData2D(getIDSafe(), xoff, yoff, this.mSelectedLOD, this.mSelectedFace.mID, w, h, array, sizeBytes, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, Object array) {
        try {
            Trace.traceBegin(32768, "copy2DRangeFrom");
            try {
                copy2DRangeFromUnchecked(xoff, yoff, w, h, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
                Trace.traceEnd(32768);
            } catch (Throwable th) {
                th = th;
                Trace.traceEnd(32768);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            Trace.traceEnd(32768);
            throw th;
        }
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, byte[] data) {
        validateIsInt8();
        copy2DRangeFromUnchecked(xoff, yoff, w, h, data, Element.DataType.SIGNED_8, data.length);
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, short[] data) {
        validateIsInt16OrFloat16();
        copy2DRangeFromUnchecked(xoff, yoff, w, h, data, Element.DataType.SIGNED_16, data.length);
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, int[] data) {
        validateIsInt32();
        copy2DRangeFromUnchecked(xoff, yoff, w, h, data, Element.DataType.SIGNED_32, data.length);
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, float[] data) {
        validateIsFloat32();
        copy2DRangeFromUnchecked(xoff, yoff, w, h, data, Element.DataType.FLOAT_32, data.length);
    }

    public void copy2DRangeFrom(int xoff, int yoff, int w, int h, Allocation data, int dataXoff, int dataYoff) {
        try {
            Trace.traceBegin(32768, "copy2DRangeFrom");
            this.mRS.validate();
            validate2DRange(xoff, yoff, w, h);
            this.mRS.nAllocationData2D(getIDSafe(), xoff, yoff, this.mSelectedLOD, this.mSelectedFace.mID, w, h, data.getID(this.mRS), dataXoff, dataYoff, data.mSelectedLOD, data.mSelectedFace.mID);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copy2DRangeFrom(int xoff, int yoff, Bitmap data) {
        try {
            Trace.traceBegin(32768, "copy2DRangeFrom");
            this.mRS.validate();
            if (data.getConfig() == null) {
                Bitmap newBitmap = Bitmap.createBitmap(data.getWidth(), data.getHeight(), Bitmap.Config.ARGB_8888);
                new Canvas(newBitmap).drawBitmap(data, 0.0f, 0.0f, (Paint) null);
                copy2DRangeFrom(xoff, yoff, newBitmap);
                return;
            }
            validateBitmapFormat(data);
            validate2DRange(xoff, yoff, data.getWidth(), data.getHeight());
            this.mRS.nAllocationData2D(getIDSafe(), xoff, yoff, this.mSelectedLOD, this.mSelectedFace.mID, data);
            Trace.traceEnd(32768);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    private void validate3DRange(int xoff, int yoff, int zoff, int w, int h, int d) {
        if (this.mAdaptedAllocation == null) {
            if (xoff < 0 || yoff < 0 || zoff < 0) {
                throw new RSIllegalArgumentException("Offset cannot be negative.");
            } else if (h < 0 || w < 0 || d < 0) {
                throw new RSIllegalArgumentException("Height or width cannot be negative.");
            } else if (xoff + w > this.mCurrentDimX || yoff + h > this.mCurrentDimY || zoff + d > this.mCurrentDimZ) {
                throw new RSIllegalArgumentException("Updated region larger than allocation.");
            }
        }
    }

    private void copy3DRangeFromUnchecked(int xoff, int yoff, int zoff, int w, int h, int d, Object array, Element.DataType dt, int arrayLen) {
        boolean usePadding;
        int sizeBytes;
        try {
            Trace.traceBegin(32768, "copy3DRangeFromUnchecked");
            this.mRS.validate();
            validate3DRange(xoff, yoff, zoff, w, h, d);
            int dataSize = this.mType.mElement.getBytesSize() * w * h * d;
            int sizeBytes2 = dt.mSize * arrayLen;
            if (!this.mAutoPadding || this.mType.getElement().getVectorSize() != 3) {
                if (dataSize <= sizeBytes2) {
                    usePadding = false;
                    sizeBytes = sizeBytes2;
                } else {
                    throw new RSIllegalArgumentException("Array too small for allocation type.");
                }
            } else if ((dataSize / 4) * 3 <= sizeBytes2) {
                usePadding = true;
                sizeBytes = dataSize;
            } else {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
            this.mRS.nAllocationData3D(getIDSafe(), xoff, yoff, zoff, this.mSelectedLOD, w, h, d, array, sizeBytes, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copy3DRangeFrom(int xoff, int yoff, int zoff, int w, int h, int d, Object array) {
        try {
            Trace.traceBegin(32768, "copy3DRangeFrom");
            try {
                copy3DRangeFromUnchecked(xoff, yoff, zoff, w, h, d, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
                Trace.traceEnd(32768);
            } catch (Throwable th) {
                th = th;
                Trace.traceEnd(32768);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            Trace.traceEnd(32768);
            throw th;
        }
    }

    public void copy3DRangeFrom(int xoff, int yoff, int zoff, int w, int h, int d, Allocation data, int dataXoff, int dataYoff, int dataZoff) {
        this.mRS.validate();
        validate3DRange(xoff, yoff, zoff, w, h, d);
        this.mRS.nAllocationData3D(getIDSafe(), xoff, yoff, zoff, this.mSelectedLOD, w, h, d, data.getID(this.mRS), dataXoff, dataYoff, dataZoff, data.mSelectedLOD);
    }

    public void copyTo(Bitmap b) {
        try {
            Trace.traceBegin(32768, "copyTo");
            this.mRS.validate();
            validateBitmapFormat(b);
            validateBitmapSize(b);
            this.mRS.nAllocationCopyToBitmap(getID(this.mRS), b);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    private void copyTo(Object array, Element.DataType dt, int arrayLen) {
        try {
            Trace.traceBegin(32768, "copyTo");
            this.mRS.validate();
            boolean usePadding = false;
            if (this.mAutoPadding && this.mType.getElement().getVectorSize() == 3) {
                usePadding = true;
            }
            if (usePadding) {
                if (dt.mSize * arrayLen < (this.mSize / 4) * 3) {
                    throw new RSIllegalArgumentException("Size of output array cannot be smaller than size of allocation.");
                }
            } else if (dt.mSize * arrayLen < this.mSize) {
                throw new RSIllegalArgumentException("Size of output array cannot be smaller than size of allocation.");
            }
            this.mRS.nAllocationRead(getID(this.mRS), array, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copyTo(Object array) {
        copyTo(array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public void copyTo(byte[] d) {
        validateIsInt8();
        copyTo(d, Element.DataType.SIGNED_8, d.length);
    }

    public void copyTo(short[] d) {
        validateIsInt16OrFloat16();
        copyTo(d, Element.DataType.SIGNED_16, d.length);
    }

    public void copyTo(int[] d) {
        validateIsInt32();
        copyTo(d, Element.DataType.SIGNED_32, d.length);
    }

    public void copyTo(float[] d) {
        validateIsFloat32();
        copyTo(d, Element.DataType.FLOAT_32, d.length);
    }

    public void copyToFieldPacker(int xoff, int yoff, int zoff, int component_number, FieldPacker fp) {
        this.mRS.validate();
        if (component_number >= this.mType.mElement.mElements.length) {
            throw new RSIllegalArgumentException("Component_number " + component_number + " out of range.");
        } else if (xoff < 0) {
            throw new RSIllegalArgumentException("Offset x must be >= 0.");
        } else if (yoff < 0) {
            throw new RSIllegalArgumentException("Offset y must be >= 0.");
        } else if (zoff >= 0) {
            byte[] data = fp.getData();
            int data_length = data.length;
            int eSize = this.mType.mElement.mElements[component_number].getBytesSize() * this.mType.mElement.mArraySizes[component_number];
            if (data_length == eSize) {
                this.mRS.nAllocationElementRead(getIDSafe(), xoff, yoff, zoff, this.mSelectedLOD, component_number, data, data_length);
                return;
            }
            throw new RSIllegalArgumentException("Field packer sizelength " + data_length + " does not match component size " + eSize + ".");
        } else {
            throw new RSIllegalArgumentException("Offset z must be >= 0.");
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

    private void copy1DRangeToUnchecked(int off, int count, Object array, Element.DataType dt, int arrayLen) {
        boolean usePadding;
        try {
            Trace.traceBegin(32768, "copy1DRangeToUnchecked");
            int dataSize = this.mType.mElement.getBytesSize() * count;
            if (!this.mAutoPadding || this.mType.getElement().getVectorSize() != 3) {
                usePadding = false;
            } else {
                usePadding = true;
            }
            data1DChecks(off, count, arrayLen * dt.mSize, dataSize, usePadding);
            this.mRS.nAllocationRead1D(getIDSafe(), off, this.mSelectedLOD, count, array, dataSize, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copy1DRangeToUnchecked(int off, int count, Object array) {
        copy1DRangeToUnchecked(off, count, array, validateObjectIsPrimitiveArray(array, false), Array.getLength(array));
    }

    public void copy1DRangeToUnchecked(int off, int count, int[] d) {
        copy1DRangeToUnchecked(off, count, d, Element.DataType.SIGNED_32, d.length);
    }

    public void copy1DRangeToUnchecked(int off, int count, short[] d) {
        copy1DRangeToUnchecked(off, count, d, Element.DataType.SIGNED_16, d.length);
    }

    public void copy1DRangeToUnchecked(int off, int count, byte[] d) {
        copy1DRangeToUnchecked(off, count, d, Element.DataType.SIGNED_8, d.length);
    }

    public void copy1DRangeToUnchecked(int off, int count, float[] d) {
        copy1DRangeToUnchecked(off, count, d, Element.DataType.FLOAT_32, d.length);
    }

    public void copy1DRangeTo(int off, int count, Object array) {
        copy1DRangeToUnchecked(off, count, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public void copy1DRangeTo(int off, int count, int[] d) {
        validateIsInt32();
        copy1DRangeToUnchecked(off, count, d, Element.DataType.SIGNED_32, d.length);
    }

    public void copy1DRangeTo(int off, int count, short[] d) {
        validateIsInt16OrFloat16();
        copy1DRangeToUnchecked(off, count, d, Element.DataType.SIGNED_16, d.length);
    }

    public void copy1DRangeTo(int off, int count, byte[] d) {
        validateIsInt8();
        copy1DRangeToUnchecked(off, count, d, Element.DataType.SIGNED_8, d.length);
    }

    public void copy1DRangeTo(int off, int count, float[] d) {
        validateIsFloat32();
        copy1DRangeToUnchecked(off, count, d, Element.DataType.FLOAT_32, d.length);
    }

    /* access modifiers changed from: package-private */
    public void copy2DRangeToUnchecked(int xoff, int yoff, int w, int h, Object array, Element.DataType dt, int arrayLen) {
        boolean usePadding;
        int sizeBytes;
        try {
            Trace.traceBegin(32768, "copy2DRangeToUnchecked");
            this.mRS.validate();
            validate2DRange(xoff, yoff, w, h);
            int dataSize = this.mType.mElement.getBytesSize() * w * h;
            int sizeBytes2 = dt.mSize * arrayLen;
            if (!this.mAutoPadding || this.mType.getElement().getVectorSize() != 3) {
                if (dataSize <= sizeBytes2) {
                    usePadding = false;
                    sizeBytes = sizeBytes2;
                } else {
                    throw new RSIllegalArgumentException("Array too small for allocation type.");
                }
            } else if ((dataSize / 4) * 3 <= sizeBytes2) {
                usePadding = true;
                sizeBytes = dataSize;
            } else {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
            this.mRS.nAllocationRead2D(getIDSafe(), xoff, yoff, this.mSelectedLOD, this.mSelectedFace.mID, w, h, array, sizeBytes, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, Object array) {
        copy2DRangeToUnchecked(xoff, yoff, w, h, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, byte[] data) {
        validateIsInt8();
        copy2DRangeToUnchecked(xoff, yoff, w, h, data, Element.DataType.SIGNED_8, data.length);
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, short[] data) {
        validateIsInt16OrFloat16();
        copy2DRangeToUnchecked(xoff, yoff, w, h, data, Element.DataType.SIGNED_16, data.length);
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, int[] data) {
        validateIsInt32();
        copy2DRangeToUnchecked(xoff, yoff, w, h, data, Element.DataType.SIGNED_32, data.length);
    }

    public void copy2DRangeTo(int xoff, int yoff, int w, int h, float[] data) {
        validateIsFloat32();
        copy2DRangeToUnchecked(xoff, yoff, w, h, data, Element.DataType.FLOAT_32, data.length);
    }

    private void copy3DRangeToUnchecked(int xoff, int yoff, int zoff, int w, int h, int d, Object array, Element.DataType dt, int arrayLen) {
        boolean usePadding;
        int sizeBytes;
        try {
            Trace.traceBegin(32768, "copy3DRangeToUnchecked");
            this.mRS.validate();
            validate3DRange(xoff, yoff, zoff, w, h, d);
            int dataSize = this.mType.mElement.getBytesSize() * w * h * d;
            int sizeBytes2 = dt.mSize * arrayLen;
            if (!this.mAutoPadding || this.mType.getElement().getVectorSize() != 3) {
                if (dataSize <= sizeBytes2) {
                    usePadding = false;
                    sizeBytes = sizeBytes2;
                } else {
                    throw new RSIllegalArgumentException("Array too small for allocation type.");
                }
            } else if ((dataSize / 4) * 3 <= sizeBytes2) {
                usePadding = true;
                sizeBytes = dataSize;
            } else {
                throw new RSIllegalArgumentException("Array too small for allocation type.");
            }
            this.mRS.nAllocationRead3D(getIDSafe(), xoff, yoff, zoff, this.mSelectedLOD, w, h, d, array, sizeBytes, dt, this.mType.mElement.mType.mSize, usePadding);
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public void copy3DRangeTo(int xoff, int yoff, int zoff, int w, int h, int d, Object array) {
        copy3DRangeToUnchecked(xoff, yoff, zoff, w, h, d, array, validateObjectIsPrimitiveArray(array, true), Array.getLength(array));
    }

    public static Allocation createTyped(RenderScript rs, Type type, MipmapControl mips, int usage) {
        try {
            Trace.traceBegin(32768, "createTyped");
            rs.validate();
            if (type.getID(rs) != 0) {
                try {
                    long id = rs.nAllocationCreateTyped(type.getID(rs), mips.mID, usage, 0);
                    if (id != 0) {
                        Allocation allocation = new Allocation(id, rs, type, false, usage, mips);
                        Trace.traceEnd(32768);
                        return allocation;
                    }
                    throw new RSRuntimeException("Allocation creation failed.");
                } catch (Throwable th) {
                    th = th;
                    Trace.traceEnd(32768);
                    throw th;
                }
            } else {
                throw new RSInvalidStateException("Bad Type");
            }
        } catch (Throwable th2) {
            th = th2;
            Trace.traceEnd(32768);
            throw th;
        }
    }

    public static Allocation createTyped(RenderScript rs, Type type, int usage) {
        return createTyped(rs, type, MipmapControl.MIPMAP_NONE, usage);
    }

    public static Allocation createTyped(RenderScript rs, Type type) {
        return createTyped(rs, type, MipmapControl.MIPMAP_NONE, 1);
    }

    public static Allocation createSized(RenderScript rs, Element e, int count, int usage) {
        try {
            Trace.traceBegin(32768, "createSized");
            rs.validate();
            try {
                Type.Builder b = new Type.Builder(rs, e);
                try {
                    b.setX(count);
                    Type t = b.create();
                    long id = rs.nAllocationCreateTyped(t.getID(rs), MipmapControl.MIPMAP_NONE.mID, usage, 0);
                    if (id != 0) {
                        Allocation allocation = new Allocation(id, rs, t, true, usage, MipmapControl.MIPMAP_NONE);
                        Trace.traceEnd(32768);
                        return allocation;
                    }
                    throw new RSRuntimeException("Allocation creation failed.");
                } catch (Throwable th) {
                    th = th;
                    Trace.traceEnd(32768);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                Trace.traceEnd(32768);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            Trace.traceEnd(32768);
            throw th;
        }
    }

    public static Allocation createSized(RenderScript rs, Element e, int count) {
        return createSized(rs, e, count, 1);
    }

    static Element elementFromBitmap(RenderScript rs, Bitmap b) {
        Bitmap.Config bc = b.getConfig();
        if (bc == Bitmap.Config.ALPHA_8) {
            return Element.A_8(rs);
        }
        if (bc == Bitmap.Config.ARGB_4444) {
            return Element.RGBA_4444(rs);
        }
        if (bc == Bitmap.Config.ARGB_8888) {
            return Element.RGBA_8888(rs);
        }
        if (bc == Bitmap.Config.RGB_565) {
            return Element.RGB_565(rs);
        }
        throw new RSInvalidStateException("Bad bitmap type: " + bc);
    }

    static Type typeFromBitmap(RenderScript rs, Bitmap b, MipmapControl mip) {
        Type.Builder tb = new Type.Builder(rs, elementFromBitmap(rs, b));
        tb.setX(b.getWidth());
        tb.setY(b.getHeight());
        tb.setMipmaps(mip == MipmapControl.MIPMAP_FULL);
        return tb.create();
    }

    public static Allocation createFromBitmap(RenderScript rs, Bitmap b, MipmapControl mips, int usage) {
        try {
            Trace.traceBegin(32768, "createFromBitmap");
            rs.validate();
            if (b.getConfig() != null) {
                Type t = typeFromBitmap(rs, b, mips);
                if (mips == MipmapControl.MIPMAP_NONE && t.getElement().isCompatible(Element.RGBA_8888(rs)) && usage == 131) {
                    long id = rs.nAllocationCreateBitmapBackedAllocation(t.getID(rs), mips.mID, b, usage);
                    if (id != 0) {
                        Allocation alloc = new Allocation(id, rs, t, true, usage, mips);
                        alloc.setBitmap(b);
                        Trace.traceEnd(32768);
                        return alloc;
                    }
                    throw new RSRuntimeException("Load failed.");
                }
                long id2 = rs.nAllocationCreateFromBitmap(t.getID(rs), mips.mID, b, usage);
                if (id2 != 0) {
                    Allocation allocation = new Allocation(id2, rs, t, true, usage, mips);
                    Trace.traceEnd(32768);
                    return allocation;
                }
                throw new RSRuntimeException("Load failed.");
            } else if ((usage & 128) == 0) {
                Bitmap newBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
                new Canvas(newBitmap).drawBitmap(b, 0.0f, 0.0f, (Paint) null);
                return createFromBitmap(rs, newBitmap, mips, usage);
            } else {
                throw new RSIllegalArgumentException("USAGE_SHARED cannot be used with a Bitmap that has a null config.");
            }
        } finally {
            Trace.traceEnd(32768);
        }
    }

    public ByteBuffer getByteBuffer() {
        if (this.mType.hasFaces()) {
            throw new RSInvalidStateException("Cubemap is not supported for getByteBuffer().");
        } else if (this.mType.getYuv() == 17 || this.mType.getYuv() == 842094169 || this.mType.getYuv() == 35) {
            throw new RSInvalidStateException("YUV format is not supported for getByteBuffer().");
        } else {
            if (this.mByteBuffer == null || (this.mUsage & 32) != 0) {
                long[] stride = new long[1];
                this.mByteBuffer = this.mRS.nAllocationGetByteBuffer(getID(this.mRS), stride, this.mType.getX() * this.mType.getElement().getBytesSize(), this.mType.getY(), this.mType.getZ());
                this.mByteBufferStride = stride[0];
            }
            if ((this.mUsage & 32) != 0) {
                return this.mByteBuffer.asReadOnlyBuffer();
            }
            return this.mByteBuffer;
        }
    }

    public static Allocation[] createAllocations(RenderScript rs, Type t, int usage, int numAlloc) {
        try {
            Trace.traceBegin(32768, "createAllocations");
            rs.validate();
            if (t.getID(rs) != 0) {
                Allocation[] mAllocationArray = new Allocation[numAlloc];
                mAllocationArray[0] = createTyped(rs, t, usage);
                if ((usage & 32) != 0) {
                    if (numAlloc <= 16) {
                        mAllocationArray[0].setupBufferQueue(numAlloc);
                    } else {
                        mAllocationArray[0].destroy();
                        throw new RSIllegalArgumentException("Exceeds the max number of Allocations allowed: 16");
                    }
                }
                for (int i = 1; i < numAlloc; i++) {
                    mAllocationArray[i] = createFromAllocation(rs, mAllocationArray[0]);
                }
                return mAllocationArray;
            }
            throw new RSInvalidStateException("Bad Type");
        } finally {
            Trace.traceEnd(32768);
        }
    }

    static Allocation createFromAllocation(RenderScript rs, Allocation alloc) {
        try {
            Trace.traceBegin(32768, "createFromAllcation");
            rs.validate();
            if (alloc.getID(rs) != 0) {
                Type type = alloc.getType();
                int usage = alloc.getUsage();
                MipmapControl mips = alloc.getMipmap();
                long id = rs.nAllocationCreateTyped(type.getID(rs), mips.mID, usage, 0);
                if (id != 0) {
                    Allocation outAlloc = new Allocation(id, rs, type, false, usage, mips);
                    if ((usage & 32) != 0) {
                        outAlloc.shareBufferQueue(alloc);
                    }
                    return outAlloc;
                }
                throw new RSRuntimeException("Allocation creation failed.");
            }
            throw new RSInvalidStateException("Bad input Allocation");
        } finally {
            Trace.traceEnd(32768);
        }
    }

    /* access modifiers changed from: package-private */
    public void setupBufferQueue(int numAlloc) {
        this.mRS.validate();
        if ((this.mUsage & 32) != 0) {
            this.mRS.nAllocationSetupBufferQueue(getID(this.mRS), numAlloc);
            return;
        }
        throw new RSInvalidStateException("Allocation is not USAGE_IO_INPUT.");
    }

    /* access modifiers changed from: package-private */
    public void shareBufferQueue(Allocation alloc) {
        this.mRS.validate();
        if ((this.mUsage & 32) != 0) {
            this.mGetSurfaceSurface = alloc.getSurface();
            this.mRS.nAllocationShareBufferQueue(getID(this.mRS), alloc.getID(this.mRS));
            return;
        }
        throw new RSInvalidStateException("Allocation is not USAGE_IO_INPUT.");
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
        if ((this.mUsage & 32) != 0) {
            if (this.mGetSurfaceSurface == null) {
                this.mGetSurfaceSurface = this.mRS.nAllocationGetSurface(getID(this.mRS));
            }
            return this.mGetSurfaceSurface;
        }
        throw new RSInvalidStateException("Allocation is not a surface texture.");
    }

    public void setSurface(Surface sur) {
        this.mRS.validate();
        if ((this.mUsage & 64) != 0) {
            this.mRS.nAllocationSetSurface(getID(this.mRS), sur);
            return;
        }
        throw new RSInvalidStateException("Allocation is not USAGE_IO_OUTPUT.");
    }

    public static Allocation createFromBitmap(RenderScript rs, Bitmap b) {
        if (rs.getApplicationContext().getApplicationInfo().targetSdkVersion >= 18) {
            return createFromBitmap(rs, b, MipmapControl.MIPMAP_NONE, 131);
        }
        return createFromBitmap(rs, b, MipmapControl.MIPMAP_NONE, 2);
    }

    public static Allocation createCubemapFromBitmap(RenderScript rs, Bitmap b, MipmapControl mips, int usage) {
        rs.validate();
        int height = b.getHeight();
        int width = b.getWidth();
        if (width % 6 != 0) {
            throw new RSIllegalArgumentException("Cubemap height must be multiple of 6");
        } else if (width / 6 == height) {
            boolean z = false;
            if (((height + -1) & height) == 0) {
                Element e = elementFromBitmap(rs, b);
                Type.Builder tb = new Type.Builder(rs, e);
                tb.setX(height);
                tb.setY(height);
                tb.setFaces(true);
                if (mips == MipmapControl.MIPMAP_FULL) {
                    z = true;
                }
                tb.setMipmaps(z);
                Type t = tb.create();
                long id = rs.nAllocationCubeCreateFromBitmap(t.getID(rs), mips.mID, b, usage);
                if (id != 0) {
                    return new Allocation(id, rs, t, true, usage, mips);
                }
                throw new RSRuntimeException("Load failed for bitmap " + b + " element " + e);
            }
            throw new RSIllegalArgumentException("Only power of 2 cube faces supported");
        } else {
            throw new RSIllegalArgumentException("Only square cube map faces supported");
        }
    }

    public static Allocation createCubemapFromBitmap(RenderScript rs, Bitmap b) {
        return createCubemapFromBitmap(rs, b, MipmapControl.MIPMAP_NONE, 2);
    }

    public static Allocation createCubemapFromCubeFaces(RenderScript rs, Bitmap xpos, Bitmap xneg, Bitmap ypos, Bitmap yneg, Bitmap zpos, Bitmap zneg, MipmapControl mips, int usage) {
        int height = xpos.getHeight();
        if (xpos.getWidth() == height) {
            if (xneg.getWidth() == height && xneg.getHeight() == height) {
                if (ypos.getWidth() == height && ypos.getHeight() == height) {
                    if (yneg.getWidth() == height && yneg.getHeight() == height) {
                        if (zpos.getWidth() == height && zpos.getHeight() == height) {
                            if (zneg.getWidth() == height && zneg.getHeight() == height) {
                                boolean z = false;
                                if (((height + -1) & height) == 0) {
                                    Type.Builder tb = new Type.Builder(rs, elementFromBitmap(rs, xpos));
                                    tb.setX(height);
                                    tb.setY(height);
                                    tb.setFaces(true);
                                    if (mips == MipmapControl.MIPMAP_FULL) {
                                        z = true;
                                    }
                                    tb.setMipmaps(z);
                                    Allocation cubemap = createTyped(rs, tb.create(), mips, usage);
                                    AllocationAdapter adapter = AllocationAdapter.create2D(rs, cubemap);
                                    adapter.setFace(Type.CubemapFace.POSITIVE_X);
                                    adapter.copyFrom(xpos);
                                    adapter.setFace(Type.CubemapFace.NEGATIVE_X);
                                    adapter.copyFrom(xneg);
                                    adapter.setFace(Type.CubemapFace.POSITIVE_Y);
                                    adapter.copyFrom(ypos);
                                    adapter.setFace(Type.CubemapFace.NEGATIVE_Y);
                                    adapter.copyFrom(yneg);
                                    adapter.setFace(Type.CubemapFace.POSITIVE_Z);
                                    adapter.copyFrom(zpos);
                                    adapter.setFace(Type.CubemapFace.NEGATIVE_Z);
                                    adapter.copyFrom(zneg);
                                    return cubemap;
                                }
                                throw new RSIllegalArgumentException("Only power of 2 cube faces supported");
                            }
                        }
                    }
                }
            }
        }
        throw new RSIllegalArgumentException("Only square cube map faces supported");
    }

    public static Allocation createCubemapFromCubeFaces(RenderScript rs, Bitmap xpos, Bitmap xneg, Bitmap ypos, Bitmap yneg, Bitmap zpos, Bitmap zneg) {
        return createCubemapFromCubeFaces(rs, xpos, xneg, ypos, yneg, zpos, zneg, MipmapControl.MIPMAP_NONE, 2);
    }

    public static Allocation createFromBitmapResource(RenderScript rs, Resources res, int id, MipmapControl mips, int usage) {
        rs.validate();
        if ((usage & 224) == 0) {
            Bitmap b = BitmapFactory.decodeResource(res, id);
            Allocation alloc = createFromBitmap(rs, b, mips, usage);
            b.recycle();
            return alloc;
        }
        throw new RSIllegalArgumentException("Unsupported usage specified.");
    }

    public static Allocation createFromBitmapResource(RenderScript rs, Resources res, int id) {
        if (rs.getApplicationContext().getApplicationInfo().targetSdkVersion >= 18) {
            return createFromBitmapResource(rs, res, id, MipmapControl.MIPMAP_NONE, 3);
        }
        return createFromBitmapResource(rs, res, id, MipmapControl.MIPMAP_NONE, 2);
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
        OnBufferAvailableListener onBufferAvailableListener;
        synchronized (mAllocationMap) {
            Allocation a = mAllocationMap.get(new Long(id));
            if (!(a == null || (onBufferAvailableListener = a.mBufferNotifier) == null)) {
                onBufferAvailableListener.onBufferAvailable(a);
            }
        }
    }

    @Override // android.renderscript.BaseObj
    public void destroy() {
        if ((this.mUsage & 64) != 0) {
            setSurface(null);
        }
        Type type = this.mType;
        if (type != null && this.mOwningType) {
            type.destroy();
        }
        super.destroy();
    }
}
