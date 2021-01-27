package ohos.media.image;

import java.io.OutputStream;
import java.util.HashSet;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ImagePacker {
    private static final int BUFFER_SIZE = 4096;
    private static final long FAILED = -1;
    private static final Logger LOGGER = LoggerFactory.getImageLogger(ImagePacker.class);
    private static final int SUCCESS = 0;
    private byte[] nativeBuffer = new byte[4096];
    private long nativeImagePacker;

    public static class PackingOptions {
        public String format = "image/jpeg";
        public int numberHint = 1;
        public int quality = 100;
    }

    private native int nativeAddImage(long j, ImageSource imageSource);

    private native int nativeAddImage(long j, ImageSource imageSource, int i);

    private native int nativeAddImage(long j, PixelMap pixelMap);

    private static native ImagePacker nativeCreateImagePacker();

    private native long nativeFinalizePacking(long j);

    private static native HashSet<String> nativeGetSupportedFormats();

    private native void nativeRelease(long j);

    private native int nativeStartPacking(long j, OutputStream outputStream, PackingOptions packingOptions, byte[] bArr);

    private native int nativeStartPacking(long j, byte[] bArr, int i, PackingOptions packingOptions);

    static {
        LOGGER.debug("Begin loading image_packer_jni library", new Object[0]);
        System.loadLibrary("image_packer_jni.z");
    }

    private ImagePacker(long j) {
        this.nativeImagePacker = j;
    }

    public static ImagePacker create() {
        return nativeCreateImagePacker();
    }

    public static HashSet<String> getSupportedFormats() {
        return nativeGetSupportedFormats();
    }

    public boolean initializePacking(byte[] bArr, PackingOptions packingOptions) {
        if (bArr != null) {
            if (packingOptions == null) {
                packingOptions = new PackingOptions();
            }
            if (!isPackerOptionValid(packingOptions.format, packingOptions.quality)) {
                return false;
            }
            return initializePacking(bArr, 0, packingOptions);
        }
        throw new IllegalArgumentException("data is null");
    }

    public boolean initializePacking(byte[] bArr, int i, PackingOptions packingOptions) {
        if (bArr == null) {
            throw new IllegalArgumentException("data is null");
        } else if (i < 0 || i >= bArr.length) {
            throw new IndexOutOfBoundsException("offset is invalid");
        } else {
            if (packingOptions == null) {
                packingOptions = new PackingOptions();
            }
            if (!isPackerOptionValid(packingOptions.format, packingOptions.quality)) {
                return false;
            }
            int nativeStartPacking = nativeStartPacking(this.nativeImagePacker, bArr, i, packingOptions);
            if (nativeStartPacking == 0) {
                return true;
            }
            LOGGER.error("startPacking failed to data array,error code is %{public}d", Integer.valueOf(nativeStartPacking));
            return false;
        }
    }

    public boolean initializePacking(OutputStream outputStream, PackingOptions packingOptions) {
        if (outputStream != null) {
            if (packingOptions == null) {
                packingOptions = new PackingOptions();
            }
            if (!isPackerOptionValid(packingOptions.format, packingOptions.quality)) {
                return false;
            }
            int nativeStartPacking = nativeStartPacking(this.nativeImagePacker, outputStream, packingOptions, this.nativeBuffer);
            if (nativeStartPacking == 0) {
                return true;
            }
            LOGGER.error("startPacking failed to outputStream, error code is %{public}d", Integer.valueOf(nativeStartPacking));
            return false;
        }
        throw new IllegalArgumentException("outputStream is null");
    }

    public boolean addImage(PixelMap pixelMap) {
        if (pixelMap != null) {
            int nativeAddImage = nativeAddImage(this.nativeImagePacker, pixelMap);
            if (nativeAddImage == 0) {
                return true;
            }
            LOGGER.error("addImage failed, error code is %{public}d", Integer.valueOf(nativeAddImage));
            return false;
        }
        throw new IllegalArgumentException("pixelmap is null");
    }

    public boolean addImage(ImageSource imageSource) {
        if (imageSource != null) {
            int nativeAddImage = nativeAddImage(this.nativeImagePacker, imageSource);
            if (nativeAddImage == 0) {
                return true;
            }
            LOGGER.error("addImage failed from image source, error code is %{public}d", Integer.valueOf(nativeAddImage));
            return false;
        }
        throw new IllegalArgumentException("source is null");
    }

    public boolean addImage(ImageSource imageSource, int i) {
        if (imageSource == null) {
            throw new IllegalArgumentException("source is null");
        } else if (i >= 0) {
            int nativeAddImage = nativeAddImage(this.nativeImagePacker, imageSource, i);
            if (nativeAddImage == 0) {
                return true;
            }
            LOGGER.error("addImage failed from index image source, error code is %{public}d, index is %{public}d", Integer.valueOf(nativeAddImage), Integer.valueOf(i));
            return false;
        } else {
            throw new IllegalArgumentException("index must not be negative");
        }
    }

    public long finalizePacking() {
        long nativeFinalizePacking = nativeFinalizePacking(this.nativeImagePacker);
        if (nativeFinalizePacking >= 0) {
            return nativeFinalizePacking;
        }
        LOGGER.error("finalizePacking failed, error code is %{public}d.", Long.valueOf(nativeFinalizePacking));
        return -1;
    }

    public void release() {
        if (!isReleased()) {
            nativeRelease(this.nativeImagePacker);
            this.nativeImagePacker = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    private boolean isReleased() {
        return this.nativeImagePacker == 0;
    }

    private boolean isPackerOptionValid(String str, int i) {
        if (str != null && !str.isEmpty() && i >= 0 && i <= 100) {
            return true;
        }
        LOGGER.error("PackingOptions invalid %{public}s, %{public}d", str, Integer.valueOf(i));
        return false;
    }
}
