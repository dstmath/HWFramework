package ohos.media.image;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.image.common.ColorSpace;
import ohos.media.image.common.ImageInfo;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.PropertyData;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ImageSource {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(ImageSource.class);
    private static final int SUCCESS = 0;
    private Object imgDataSource;
    private long nativeImageSource;

    public static class DecodingOptions {
        public static final int DEFAULT_SAMPLE_SIZE = 1;
        public boolean allowPartialImage = true;
        public ColorSpace desiredColorSpace = ColorSpace.SRGB;
        public PixelFormat desiredPixelFormat = PixelFormat.ARGB_8888;
        public Rect desiredRegion;
        public Size desiredSize;
        public float rotateDegrees = ConstantValue.MIN_ZOOM_VALUE;
        public int sampleSize = 1;
    }

    public static class IncrementalSourceOptions {
        public UpdateMode mode = UpdateMode.FULL_DATA;
        public SourceOptions opts;
    }

    public static class SourceInfo {
        public String encodedFormat;
        public int topLevelImageNum = 0;
    }

    public static class SourceOptions {
        public String formatHint;
    }

    private static native ImageSource nativeCreateImageSource(InputStream inputStream, SourceOptions sourceOptions);

    private static native ImageSource nativeCreateImageSource(String str, SourceOptions sourceOptions);

    private static native ImageSource nativeCreateImageSource(byte[] bArr, int i, int i2, SourceOptions sourceOptions);

    private static native ImageSource nativeCreateIncrementalImageSource(IncrementalSourceOptions incrementalSourceOptions);

    private native PixelMap nativeCreatePixelmap(long j, int i, DecodingOptions decodingOptions);

    private native PixelMap nativeCreateThumbnailPixelmap(long j, DecodingOptions decodingOptions, boolean z);

    private native ImageInfo nativeGetImageInfo(long j, int i);

    private native PropertyData nativeGetImageProperty(long j, String str);

    private native byte[] nativeGetImageThumbnailBytes(long j);

    private native SourceInfo nativeGetSourceInfo(long j);

    private static native HashSet<String> nativeGetSupportedFormats();

    private native ImageInfo nativeGetThumbnailInfo(long j);

    private static native void nativeInit();

    private native void nativeRelease(long j);

    private native int nativeUpdateData(long j, byte[] bArr, int i, int i2, boolean z);

    static {
        LOGGER.debug("Begin loading image_source_jni library", new Object[0]);
        System.loadLibrary("image_source_jni.z");
        nativeInit();
    }

    public enum UpdateMode {
        FULL_DATA(0),
        INCREMENTAL_DATA(1);
        
        private final int updateMode;

        private UpdateMode(int i) {
            this.updateMode = i;
        }

        public int getValue() {
            return this.updateMode;
        }
    }

    /* access modifiers changed from: private */
    public static class ByteArrayDataSource {
        private final byte[] data;
        private final int length;
        private final int offset;

        ByteArrayDataSource(byte[] bArr, int i, int i2) {
            this.data = bArr;
            this.offset = i;
            this.length = i2;
        }
    }

    private static class InputStreamDataSource {
        private final InputStream is;

        InputStreamDataSource(InputStream inputStream) {
            this.is = inputStream;
        }
    }

    private static class FileDataSource {
        private final File file;

        FileDataSource(File file2) {
            this.file = file2;
        }
    }

    private static class FileDescriptorDataSource {
        private final FileDescriptor fd;

        FileDescriptorDataSource(FileDescriptor fileDescriptor) {
            this.fd = fileDescriptor;
        }
    }

    private ImageSource(long j) {
        this.nativeImageSource = j;
    }

    public static HashSet<String> getSupportedFormats() {
        return nativeGetSupportedFormats();
    }

    public static ImageSource create(InputStream inputStream, SourceOptions sourceOptions) {
        if (inputStream != null) {
            ImageSource nativeCreateImageSource = nativeCreateImageSource(inputStream, sourceOptions);
            if (nativeCreateImageSource == null) {
                LOGGER.error("create ImageSource from input stream fail.", new Object[0]);
                return null;
            }
            nativeCreateImageSource.imgDataSource = new InputStreamDataSource(inputStream);
            return nativeCreateImageSource;
        }
        throw new IllegalArgumentException("is is null");
    }

    public static ImageSource create(byte[] bArr, SourceOptions sourceOptions) {
        if (bArr != null) {
            return create(bArr, 0, bArr.length, sourceOptions);
        }
        throw new IllegalArgumentException("data is null");
    }

    public static ImageSource create(byte[] bArr, int i, int i2, SourceOptions sourceOptions) {
        if (bArr == null) {
            throw new IllegalArgumentException("data is null");
        } else if (i < 0 || i2 < 0 || i >= bArr.length || i + i2 > bArr.length) {
            throw new IndexOutOfBoundsException("offset or length is invalid");
        } else {
            ImageSource nativeCreateImageSource = nativeCreateImageSource(bArr, i, i2, sourceOptions);
            if (nativeCreateImageSource == null) {
                LOGGER.error("create ImageSource from data array fail. offset : %{public}d, length :  %{public}d.", Integer.valueOf(i), Integer.valueOf(i2));
                return null;
            }
            nativeCreateImageSource.imgDataSource = new ByteArrayDataSource(bArr, i, i2);
            return nativeCreateImageSource;
        }
    }

    public static ImageSource create(String str, SourceOptions sourceOptions) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("pathName is invalid");
        } else if (Files.isReadable(Paths.get(str, new String[0]))) {
            return nativeCreateImageSource(str, sourceOptions);
        } else {
            throw new DataSourceUnavailableException("pathName can not read");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0029, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
        throw r5;
     */
    public static ImageSource create(File file, SourceOptions sourceOptions) {
        if (file != null) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ImageSource nativeCreateImageSource = nativeCreateImageSource(fileInputStream, sourceOptions);
                if (nativeCreateImageSource == null) {
                    LOGGER.error("create ImageSource from file fail", new Object[0]);
                    $closeResource(null, fileInputStream);
                    return null;
                }
                nativeCreateImageSource.imgDataSource = new FileDataSource(file);
                $closeResource(null, fileInputStream);
                return nativeCreateImageSource;
            } catch (FileNotFoundException unused) {
                LOGGER.error("create ImageSource from file fail, reason : file not found.", new Object[0]);
                return null;
            } catch (IOException unused2) {
                LOGGER.error("create ImageSource from file, IO Exception", new Object[0]);
                return null;
            }
        } else {
            throw new IllegalArgumentException("file is null");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0033, code lost:
        throw r5;
     */
    public static ImageSource create(FileDescriptor fileDescriptor, SourceOptions sourceOptions) {
        if (fileDescriptor == null || !fileDescriptor.valid()) {
            throw new IllegalArgumentException("fd is invalid");
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(fileDescriptor);
            ImageSource nativeCreateImageSource = nativeCreateImageSource(fileInputStream, sourceOptions);
            if (nativeCreateImageSource == null) {
                LOGGER.error("createImageSource from fd fail", new Object[0]);
                $closeResource(null, fileInputStream);
                return null;
            }
            nativeCreateImageSource.imgDataSource = new FileDescriptorDataSource(fileDescriptor);
            $closeResource(null, fileInputStream);
            return nativeCreateImageSource;
        } catch (SecurityException e) {
            LOGGER.error("createImageSource from file descriptor fail, SecurityException : %{public}s.", e.getMessage());
            return null;
        } catch (IOException unused) {
            LOGGER.error("createImageSource from file, IO Exception", new Object[0]);
            return null;
        }
    }

    public static ImageSource createIncrementalSource(SourceOptions sourceOptions) {
        IncrementalSourceOptions incrementalSourceOptions = new IncrementalSourceOptions();
        incrementalSourceOptions.opts = sourceOptions;
        return createIncrementalSource(incrementalSourceOptions);
    }

    public static ImageSource createIncrementalSource(IncrementalSourceOptions incrementalSourceOptions) {
        return nativeCreateIncrementalImageSource(incrementalSourceOptions);
    }

    public PixelMap createPixelmap(DecodingOptions decodingOptions) {
        return createPixelmap(0, decodingOptions);
    }

    public PixelMap createPixelmap(int i, DecodingOptions decodingOptions) {
        if (i >= 0) {
            checkReleased();
            return nativeCreatePixelmap(this.nativeImageSource, i, decodingOptions);
        }
        throw new IllegalArgumentException("index must not be negative");
    }

    public PixelMap createThumbnailPixelmap(DecodingOptions decodingOptions, boolean z) {
        checkReleased();
        return nativeCreateThumbnailPixelmap(this.nativeImageSource, decodingOptions, z);
    }

    public boolean updateData(byte[] bArr, boolean z) {
        return updateData(bArr, 0, bArr.length, z);
    }

    public boolean updateData(byte[] bArr, int i, int i2, boolean z) {
        if (bArr == null) {
            throw new IllegalArgumentException("data must not be null");
        } else if (i < 0 || i2 < 0 || i >= bArr.length || i + i2 > bArr.length) {
            throw new IndexOutOfBoundsException("offset or length is invalid");
        } else {
            checkReleased();
            int nativeUpdateData = nativeUpdateData(this.nativeImageSource, bArr, i, i2, z);
            if (nativeUpdateData == 0) {
                return true;
            }
            LOGGER.error("updateData failed from data array, error code is %{public}d", Integer.valueOf(nativeUpdateData));
            return false;
        }
    }

    public ImageInfo getImageInfo() {
        return getImageInfo(0);
    }

    public ImageInfo getImageInfo(int i) {
        if (i >= 0) {
            checkReleased();
            return nativeGetImageInfo(this.nativeImageSource, i);
        }
        throw new IllegalArgumentException("index must not be negative");
    }

    public final PropertyData getImageProperty(String str) {
        if (str == null) {
            throw new IllegalArgumentException("key is null");
        } else if (!str.isEmpty()) {
            checkReleased();
            return nativeGetImageProperty(this.nativeImageSource, str);
        } else {
            throw new IllegalArgumentException("key is empty");
        }
    }

    public ImageInfo getThumbnailInfo() {
        checkReleased();
        return nativeGetThumbnailInfo(this.nativeImageSource);
    }

    public byte[] getImageThumbnailBytes() {
        checkReleased();
        return nativeGetImageThumbnailBytes(this.nativeImageSource);
    }

    private void checkReleased() {
        if (isReleased()) {
            throw new IllegalStateException("native resources has been released");
        }
    }

    public SourceInfo getSourceInfo() {
        checkReleased();
        return nativeGetSourceInfo(this.nativeImageSource);
    }

    public void release() {
        if (!isReleased()) {
            nativeRelease(this.nativeImageSource);
            this.nativeImageSource = 0;
        }
        this.imgDataSource = null;
    }

    private boolean isReleased() {
        return this.nativeImageSource == 0;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
