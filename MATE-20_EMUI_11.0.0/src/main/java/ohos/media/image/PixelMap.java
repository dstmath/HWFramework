package ohos.media.image;

import ark.system.NativeAllocationNotifier;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;
import ohos.media.image.common.AlphaType;
import ohos.media.image.common.ImageInfo;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Position;
import ohos.media.image.common.Rect;
import ohos.media.image.common.ScaleMode;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.system.Parameters;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class PixelMap implements Sequenceable {
    private static final int DENSITY_DEFAULT = 160;
    private static final int DENSITY_NONE = 0;
    private static final Logger LOGGER = LoggerFactory.getImageLogger(PixelMap.class);
    private static final int MAX_DIMENSION = 536870911;
    public static final Sequenceable.Producer<PixelMap> PRODUCER = new Sequenceable.Producer<PixelMap>() {
        /* class ohos.media.image.PixelMap.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public PixelMap createFromParcel(Parcel parcel) {
            if (parcel.readInt() == 0) {
                return null;
            }
            PixelMap nativeCreateFromParcel = PixelMap.nativeCreateFromParcel(parcel);
            if (nativeCreateFromParcel == null) {
                PixelMap.LOGGER.error("create pixel map from parcel failed.", new Object[0]);
            }
            return nativeCreateFromParcel;
        }
    };
    private int baseDensity;
    private long nativeImagePixelMap;
    private long nativeImageRelatedRes;
    private byte[] nativeNinePatchChunk;
    private Size size;
    private boolean useMipMap;

    public static class InitializationOptions {
        public AlphaType alphaType = AlphaType.UNKNOWN;
        public boolean editable = false;
        public PixelFormat pixelFormat = PixelFormat.UNKNOWN;
        public boolean releaseSource = false;
        public ScaleMode scaleMode = ScaleMode.FIT_TARGET_SIZE;
        public Size size;
        public boolean useSourceIfMatch = false;
    }

    private static native PixelMap nativeCreate(int i, int i2, int i3, int i4, boolean z);

    private static native PixelMap nativeCreate(PixelMap pixelMap, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, boolean z, boolean z2);

    private static native PixelMap nativeCreate(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6, boolean z);

    private native PixelMap nativeCreateFromAlpha(long j);

    /* access modifiers changed from: private */
    public static native PixelMap nativeCreateFromParcel(Parcel parcel);

    private native long nativeGetBytesNumber(long j);

    private native int nativeGetBytesNumberPerRow(long j);

    private static native long nativeGetFreeFuntion();

    private native ImageInfo nativeGetImageInfo(long j);

    private native long nativeGetPixelBytesCapacity(long j);

    private native boolean nativeIsEditable(long j);

    private native boolean nativeIsSameImage(long j, long j2);

    private native int nativeReadPixel(long j, int i, int i2);

    private native boolean nativeReadPixels(long j, Buffer buffer, long j2);

    private native boolean nativeReadPixels(long j, int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    private native void nativeRelease(long j, long j2);

    private native boolean nativeResetConfig(long j, int i, int i2, int i3);

    private native boolean nativeSetAlphaType(long j, int i);

    private native boolean nativeWritePixel(long j, int i, int i2, int i3);

    private native boolean nativeWritePixels(long j, int i);

    private native boolean nativeWritePixels(long j, Buffer buffer, long j2);

    private native boolean nativeWritePixels(long j, int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    private native boolean nativeWriteToParcel(long j, Parcel parcel);

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }

    static {
        LOGGER.debug("Begin loading pixelmap_jni library", new Object[0]);
        System.loadLibrary("pixelmap_jni.z");
    }

    protected PixelMap(long j, long j2) {
        this.baseDensity = getdefaultBaseDensity();
        this.useMipMap = false;
        this.nativeImagePixelMap = j;
        NativeAllocationNotifier.notifyMallocAllocation(PixelMap.class.getClassLoader(), nativeGetFreeFuntion(), j2, this, j);
    }

    private PixelMap(long j, long j2, long j3) {
        this(j, j2);
        this.nativeImageRelatedRes = j3;
    }

    private PixelMap(long j, long j2, byte[] bArr) {
        this(j, j2);
        this.nativeNinePatchChunk = bArr;
    }

    public static PixelMap create(int[] iArr, InitializationOptions initializationOptions) {
        if (!Objects.isNull(initializationOptions) && !Objects.isNull(initializationOptions.size)) {
            return doCreate(iArr, 0, initializationOptions.size.width, initializationOptions);
        }
        throw new IllegalArgumentException("initial options or size is null");
    }

    public static PixelMap create(int[] iArr, int i, int i2, InitializationOptions initializationOptions) {
        if (!Objects.isNull(initializationOptions) && !Objects.isNull(initializationOptions.size)) {
            return doCreate(iArr, i, i2, initializationOptions);
        }
        throw new IllegalArgumentException("initial options or size is null");
    }

    private static PixelMap doCreate(int[] iArr, int i, int i2, InitializationOptions initializationOptions) {
        if (Objects.isNull(iArr) || iArr.length <= 0) {
            throw new IllegalArgumentException("init colors is invalid");
        }
        int i3 = initializationOptions.size.width;
        int i4 = initializationOptions.size.height;
        if (i3 <= 0 || i4 <= 0) {
            throw new IllegalArgumentException("init size is invalid");
        } else if (i2 >= i3) {
            long j = (((long) (i4 - 1)) * ((long) i2)) + ((long) i);
            if (i >= 0 && i + i3 <= iArr.length && j + ((long) i3) <= ((long) iArr.length)) {
                return nativeCreate(iArr, i, i2, initializationOptions.size.width, initializationOptions.size.height, initializationOptions.pixelFormat.getValue(), initializationOptions.alphaType.getValue(), initializationOptions.editable);
            }
            throw new IllegalArgumentException("colors length is less than target pixelMap");
        } else {
            throw new IllegalArgumentException("init stride must be >= width");
        }
    }

    public static PixelMap create(InitializationOptions initializationOptions) {
        if (Objects.isNull(initializationOptions) || Objects.isNull(initializationOptions.size)) {
            throw new IllegalArgumentException("initial options or size is null");
        } else if (initializationOptions.size.width > 0 && initializationOptions.size.height > 0) {
            return nativeCreate(initializationOptions.size.width, initializationOptions.size.height, initializationOptions.pixelFormat.getValue(), initializationOptions.alphaType.getValue(), initializationOptions.editable);
        } else {
            throw new IllegalArgumentException("init size is invalid");
        }
    }

    public static PixelMap create(PixelMap pixelMap, InitializationOptions initializationOptions) {
        return create(pixelMap, null, initializationOptions);
    }

    public static PixelMap create(PixelMap pixelMap, Rect rect, InitializationOptions initializationOptions) {
        if (Objects.isNull(pixelMap) || pixelMap.isReleased()) {
            throw new IllegalArgumentException("the source pixelMap is invalid, maybe has released");
        }
        if (rect == null) {
            rect = new Rect();
        }
        if (initializationOptions == null) {
            initializationOptions = new InitializationOptions();
        }
        if (initializationOptions.size == null) {
            initializationOptions.size = new Size();
        }
        if (rect.minX < 0 || rect.minY < 0 || rect.width < 0 || rect.height < 0 || initializationOptions.size.width < 0 || initializationOptions.size.height < 0) {
            throw new IllegalArgumentException("srcRegion or option size invalid");
        }
        PixelMap nativeCreate = nativeCreate(pixelMap, rect.minX, rect.minY, rect.width, rect.height, initializationOptions.size.width, initializationOptions.size.height, initializationOptions.pixelFormat.getValue(), initializationOptions.alphaType.getValue(), initializationOptions.scaleMode.getValue(), initializationOptions.useSourceIfMatch, initializationOptions.editable);
        if (nativeCreate != null && pixelMap.nativeImagePixelMap != nativeCreate.nativeImagePixelMap && initializationOptions.releaseSource && !initializationOptions.useSourceIfMatch) {
            LOGGER.debug("release source pixelMap", new Object[0]);
            pixelMap.release();
        }
        return nativeCreate;
    }

    public ImageInfo getImageInfo() {
        return nativeGetImageInfo(this.nativeImagePixelMap);
    }

    public void release() {
        if (!isReleased()) {
            nativeRelease(this.nativeImagePixelMap, this.nativeImageRelatedRes);
            this.nativeImagePixelMap = 0;
            this.nativeImageRelatedRes = 0;
            this.nativeNinePatchChunk = null;
            this.size = null;
        }
    }

    public byte[] getNinePatchChunk() {
        return this.nativeNinePatchChunk;
    }

    public boolean isReleased() {
        return this.nativeImagePixelMap == 0;
    }

    private void checkRelease(String str) {
        if (isReleased()) {
            throw new IllegalStateException(str);
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return nativeWriteToParcel(this.nativeImagePixelMap, parcel);
    }

    public int getBytesNumberPerRow() {
        checkRelease("getBytesNumberPerRow but current pixelmap had released.");
        return nativeGetBytesNumberPerRow(this.nativeImagePixelMap);
    }

    public long getPixelBytesNumber() {
        checkRelease("getBytesNumber but current pixelmap had released.");
        return nativeGetBytesNumber(this.nativeImagePixelMap);
    }

    public long getPixelBytesCapacity() {
        checkRelease("getPixelBytesCapacity but current pixelmap had released.");
        return nativeGetPixelBytesCapacity(this.nativeImagePixelMap);
    }

    public boolean isEditable() {
        checkRelease("isEditable but current pixelmap had released.");
        return nativeIsEditable(this.nativeImagePixelMap);
    }

    public boolean isSameImage(PixelMap pixelMap) {
        if (pixelMap != null) {
            checkRelease("isSameImage but current pixelmap had released.");
            return nativeIsSameImage(this.nativeImagePixelMap, pixelMap.nativeImagePixelMap);
        }
        LOGGER.error("isSameImage input other pixelmap is null.", new Object[0]);
        throw new IllegalStateException("other pixelmap object is null.");
    }

    public int readPixel(Position position) {
        if (position == null || position.posX > MAX_DIMENSION || position.posY > MAX_DIMENSION || position.posX < 0 || position.posY < 0) {
            LOGGER.error("readPixel pos object is invalid.", new Object[0]);
            throw new IllegalArgumentException("read Pixelpos object is invalid.");
        }
        checkRelease("readPixel but current pixelmap had released.");
        return nativeReadPixel(this.nativeImagePixelMap, position.posX, position.posY);
    }

    public void readPixels(int[] iArr, int i, int i2, Rect rect) {
        if (rect == null || rect.height < 0 || rect.height > MAX_DIMENSION || rect.width < 0 || rect.width > MAX_DIMENSION) {
            LOGGER.error("readPixels region object is invalid.", new Object[0]);
            throw new IllegalArgumentException("read Pixels region object is invalid.");
        } else if (iArr == null) {
            LOGGER.error("readPixels pixels array is null.", new Object[0]);
            throw new IllegalArgumentException("read Pixels array is null.");
        } else if (i < 0 || i2 < 0 || i > iArr.length) {
            LOGGER.error("readPixels offset or stride is invalid.", new Object[0]);
            throw new IllegalArgumentException("read Pixels offset or stride is invalid.");
        } else {
            checkRelease("readPixels called read Pixels to int[] but the pixelmap is release.");
            if (!nativeReadPixels(this.nativeImagePixelMap, iArr, i, i2, rect.minX, rect.minY, rect.width, rect.height)) {
                LOGGER.error("nativeReadPixels return fail.", new Object[0]);
                throw new IllegalStateException("native readPixels fail.");
            }
        }
    }

    public void readPixels(Buffer buffer) {
        char c;
        if (buffer != null) {
            checkRelease("called read Pixels to Buffer but the pixelmap is release.");
            int remaining = buffer.remaining();
            if (buffer instanceof ByteBuffer) {
                c = 0;
            } else if (buffer instanceof ShortBuffer) {
                c = 1;
            } else if (buffer instanceof IntBuffer) {
                c = 2;
            } else {
                LOGGER.error("readPixels dst is invalid.", new Object[0]);
                throw new IllegalArgumentException("unsupported Buffer subclass");
            }
            long j = ((long) remaining) << c;
            long pixelBytesNumber = getPixelBytesNumber();
            if (j < pixelBytesNumber) {
                LOGGER.error("readPixels dstSize:%{public}d < curPixelSize:%{public}d.", Long.valueOf(j), Long.valueOf(pixelBytesNumber));
                throw new ArrayIndexOutOfBoundsException("Buffer not large enough for pixels");
            } else if (nativeReadPixels(this.nativeImagePixelMap, buffer, j)) {
                buffer.position((int) (((long) buffer.position()) + (pixelBytesNumber >> c)));
            } else {
                LOGGER.error("nativeImagePixelMap return fail.", new Object[0]);
                throw new IllegalStateException("native readPixels fail.");
            }
        } else {
            LOGGER.error("readPixels Buffer is null.", new Object[0]);
            throw new IllegalArgumentException("read pixels but Buffer is null");
        }
    }

    public void resetConfig(Size size2, PixelFormat pixelFormat) {
        if (size2 == null || size2.height <= 0 || size2.width <= 0 || size2.height > MAX_DIMENSION || size2.width > MAX_DIMENSION) {
            LOGGER.error("resetConfig input parameter invalid.", new Object[0]);
            throw new IllegalArgumentException("reset config width and height must be > 0");
        } else if (pixelFormat == null || pixelFormat == PixelFormat.UNKNOWN) {
            LOGGER.error("resetConfig pixelFormat invalid.", new Object[0]);
            throw new IllegalArgumentException("reset config pixelFormat invalid");
        } else {
            checkRelease("reset pixelmap but it is release.");
            if (!nativeResetConfig(this.nativeImagePixelMap, size2.width, size2.height, pixelFormat.getValue())) {
                LOGGER.error("nativeReset return fail.", new Object[0]);
                throw new IllegalStateException("native reset config fail.");
            }
        }
    }

    public void setAlphaType(AlphaType alphaType) {
        if (alphaType == null || alphaType == AlphaType.UNKNOWN) {
            LOGGER.error("setAlphaType input alphaType is unknown.", new Object[0]);
            throw new IllegalArgumentException("set AlphaType is UNKNOWN.");
        }
        checkRelease("set current pixelmap Alpha but pixlmap is release.");
        if (!nativeSetAlphaType(this.nativeImagePixelMap, alphaType.getValue())) {
            LOGGER.error("nativeSetAlphaType return fail.", new Object[0]);
            throw new IllegalArgumentException("set Alpha Type fail.");
        }
    }

    public void writePixel(Position position, int i) {
        if (position == null || position.posX < 0 || position.posY < 0 || position.posX > MAX_DIMENSION || position.posY > MAX_DIMENSION) {
            LOGGER.error("writePixel input invalid.", new Object[0]);
            throw new IllegalArgumentException("write pixel pos object is invalid");
        }
        checkRelease("write current pixelmap by color but pixlmap is release.");
        if (!nativeWritePixel(this.nativeImagePixelMap, position.posX, position.posY, i)) {
            LOGGER.error("nativeWritePixel return fail.", new Object[0]);
            throw new IllegalStateException("native write pixel fail.");
        }
    }

    public void writePixels(int[] iArr, int i, int i2, Rect rect) {
        if (rect == null || rect.height < 0 || rect.height > MAX_DIMENSION || rect.width < 0 || rect.width > MAX_DIMENSION) {
            LOGGER.error("writePixels region object is invalid.", new Object[0]);
            throw new IllegalArgumentException("write pixels region object is invalid.");
        } else if (iArr == null) {
            LOGGER.error("writePixels pixels array is null.", new Object[0]);
            throw new IllegalArgumentException("write pixels array is null.");
        } else if (i < 0 || i2 < 0 || i > iArr.length) {
            LOGGER.error("writePixels offset or stride is invalid.", new Object[0]);
            throw new IllegalArgumentException("write pixels offset or stride is invalid.");
        } else {
            checkRelease("writePixels from intArray but the pixelmap is release.");
            if (!nativeWritePixels(this.nativeImagePixelMap, iArr, i, i2, rect.minX, rect.minY, rect.width, rect.height)) {
                LOGGER.error("nativeWritePixels return fail.", new Object[0]);
                throw new IllegalStateException("native WritePixels from intArray fail.");
            }
        }
    }

    public void writePixels(Buffer buffer) {
        char c;
        if (buffer != null) {
            checkRelease("writePixels from Buffer but the pixelmap is release.");
            int remaining = buffer.remaining();
            if (buffer instanceof ByteBuffer) {
                c = 0;
            } else if (buffer instanceof ShortBuffer) {
                c = 1;
            } else if (buffer instanceof IntBuffer) {
                c = 2;
            } else {
                LOGGER.error("writePixels src is invalid.", new Object[0]);
                throw new IllegalStateException("unsupported Buffer subclass");
            }
            long j = ((long) remaining) << c;
            long pixelBytesNumber = getPixelBytesNumber();
            if (j < pixelBytesNumber) {
                LOGGER.error("writePixels srcSize:%{public}d < curPixelSize:%{public}d.", Long.valueOf(j), Long.valueOf(pixelBytesNumber));
                throw new ArrayIndexOutOfBoundsException("write pixels Buffer not large enough for pixels");
            } else if (nativeWritePixels(this.nativeImagePixelMap, buffer, j)) {
                buffer.position((int) (((long) buffer.position()) + (pixelBytesNumber >> c)));
            } else {
                LOGGER.error("nativeWritePixels by Buffer return false.", new Object[0]);
                throw new IllegalStateException("native WritePixels from Buffer fail.");
            }
        } else {
            LOGGER.error("writePixels Buffer is null.", new Object[0]);
            throw new IllegalArgumentException("write pixels but Buffer is null");
        }
    }

    public void writePixels(int i) {
        checkRelease("writePixels from int color but the pixelmap is release.");
        if (!nativeWritePixels(this.nativeImagePixelMap, i)) {
            LOGGER.error("nativeWritePixels by int color return false.", new Object[0]);
            throw new IllegalStateException("nativeWritePixels from int color fail.");
        }
    }

    public void setBaseDensity(int i) {
        this.baseDensity = i;
    }

    public int getBaseDensity() {
        return this.baseDensity;
    }

    public final void setUseMipmap(boolean z) {
        this.useMipMap = z;
    }

    public final boolean useMipmap() {
        checkRelease("call useMipmap() but the pixelmap is release.");
        return this.useMipMap;
    }

    public Size getFitDensitySize(int i) {
        if (this.size == null) {
            checkRelease("PixelMap has been released");
            ImageInfo imageInfo = getImageInfo();
            if (imageInfo != null) {
                this.size = imageInfo.size;
            } else {
                LOGGER.error("getFitDensitySize getImageInfo failed.", new Object[0]);
                throw new IllegalStateException("PixelMap does not contain valid info.");
            }
        }
        int i2 = this.baseDensity;
        if (i2 == 0 || i == 0 || i2 == i) {
            return this.size;
        }
        int i3 = i2 >> 1;
        return new Size(((this.size.width * i) + i3) / i2, ((this.size.height * i) + i3) / i2);
    }

    public PixelMap createFromAlpha() {
        return nativeCreateFromAlpha(this.nativeImagePixelMap);
    }

    private int getdefaultBaseDensity() {
        int i = Parameters.getInt("ro.sf.real_lcd_density", Parameters.getInt("ro.sf.lcd_density", DENSITY_DEFAULT));
        if (Parameters.getInt("persist.sys.rog.width", 0) > 0) {
            return Parameters.getInt("persist.sys.realdpi", Parameters.getInt("persist.sys.dpi", i));
        }
        int i2 = Parameters.getInt("persist.sys.dpi", 0);
        if (i2 <= 0 || i2 == i) {
            return Parameters.getInt("qemu.sf.lcd_density", i);
        }
        return i2;
    }
}
