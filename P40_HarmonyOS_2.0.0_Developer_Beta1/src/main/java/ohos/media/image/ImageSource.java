package ohos.media.image;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.Callable;
import ohos.global.resource.RawFileDescriptor;
import ohos.media.image.common.AllocatorType;
import ohos.media.image.common.ColorSpace;
import ohos.media.image.common.DecodeEvent;
import ohos.media.image.common.ImageInfo;
import ohos.media.image.common.MemoryUsagePreference;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;
import ohos.media.image.exifadapter.ExifAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ImageSource {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(ImageSource.class);
    private static final int SUCCESS = 0;
    private static long fileSize;
    private static SourceOptions inputOpts;
    private DecodeEventListener decodeEventListener;
    private volatile ExifAdapter exifAdapter;
    private final Object exifLock = new Object();
    private Object imgDataSource;
    private long nativeImageSource;

    public interface DecodeEventListener {
        void onDecodeEvent(ImageSource imageSource, DecodeEvent decodeEvent);
    }

    public static class DecodingOptions {
        public static final int DEFAULT_SAMPLE_SIZE = 1;
        public AllocatorType allocator = AllocatorType.DEFAULT;
        public boolean allowPartialImage = true;
        public ColorSpace desiredColorSpace = ColorSpace.SRGB;
        public PixelFormat desiredPixelFormat = PixelFormat.UNKNOWN;
        public Rect desiredRegion;
        public Size desiredSize;
        public boolean editable = false;
        public int fitDensity = 0;
        public float rotateDegrees = 0.0f;
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
        public int baseDensity = 0;
        public String formatHint;
    }

    private static native ImageSource nativeCreateImageSource(InputStream inputStream, SourceOptions sourceOptions);

    private static native ImageSource nativeCreateImageSource(String str, SourceOptions sourceOptions);

    private static native ImageSource nativeCreateImageSource(byte[] bArr, int i, int i2, SourceOptions sourceOptions);

    private static native ImageSource nativeCreateIncrementalImageSource(IncrementalSourceOptions incrementalSourceOptions);

    private native PixelMap nativeCreatePixelmap(ImageSource imageSource, long j, int i, DecodingOptions decodingOptions);

    private native ImageInfo nativeGetImageInfo(long j, int i);

    private native int nativeGetImagePropertyInt(long j, int i, String str, int i2);

    private native SourceInfo nativeGetSourceInfo(long j);

    private static native HashSet<String> nativeGetSupportedFormats();

    private static native void nativeInit();

    private native void nativeRelease(long j);

    private native void nativeSetMemoryUsagePreference(long j, MemoryUsagePreference memoryUsagePreference);

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
    public static class FilePathDataSource {
        private final String filePath;

        FilePathDataSource(String str) {
            this.filePath = str;
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

    /* access modifiers changed from: private */
    public static class InputStreamDataSource {
        private final InputStream is;

        InputStreamDataSource(InputStream inputStream) {
            this.is = inputStream;
        }
    }

    /* access modifiers changed from: private */
    public static class FileDataSource {
        private final File file;

        FileDataSource(File file2) {
            this.file = file2;
        }
    }

    /* access modifiers changed from: private */
    public static class FileDescriptorDataSource {
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

    public void setDecodeEventListener(DecodeEventListener decodeEventListener2) {
        this.decodeEventListener = decodeEventListener2;
    }

    private void onDecodeEventFromNative(ImageSource imageSource, int i) {
        if (this.decodeEventListener == null) {
            LOGGER.debug("decodeEventListener is null.", new Object[0]);
            return;
        }
        DecodeEvent decodeEvent = DecodeEvent.EVENT_COMPLETE_DECODE;
        if (i == 1) {
            decodeEvent = DecodeEvent.EVENT_PARTIAL_DECODE;
        }
        this.decodeEventListener.onDecodeEvent(imageSource, decodeEvent);
    }

    public void setMemoryUsagePreference(MemoryUsagePreference memoryUsagePreference) {
        nativeSetMemoryUsagePreference(this.nativeImageSource, memoryUsagePreference);
    }

    public static ImageSource create(InputStream inputStream, SourceOptions sourceOptions) {
        if (inputStream != null) {
            ImageSource nativeCreateImageSource = nativeCreateImageSource(inputStream, sourceOptions);
            inputOpts = sourceOptions;
            if (nativeCreateImageSource == null) {
                LOGGER.error("create ImageSource from input stream fail.", new Object[0]);
                return null;
            }
            nativeCreateImageSource.imgDataSource = new InputStreamDataSource(inputStream);
            try {
                fileSize = (long) inputStream.available();
            } catch (IOException unused) {
                LOGGER.error("get file size failed of InputStream ImageSource", new Object[0]);
            }
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

    public static ImageSource create(ByteBuffer byteBuffer, SourceOptions sourceOptions) {
        if (byteBuffer != null) {
            byteBuffer.flip();
            byte[] bArr = new byte[(byteBuffer.limit() - byteBuffer.position())];
            if (!byteBuffer.isReadOnly()) {
                byteBuffer.get(bArr);
                return create(bArr, 0, bArr.length, sourceOptions);
            }
            throw new IllegalArgumentException("data is read only");
        }
        throw new IllegalArgumentException("data is null");
    }

    public static ImageSource create(Callable<RawFileDescriptor> callable, SourceOptions sourceOptions) {
        if (callable != null) {
            try {
                RawFileDescriptor call = callable.call();
                if (call != null) {
                    FileDescriptor fileDescriptor = call.getFileDescriptor();
                    long startPosition = call.getStartPosition();
                    FileInputStream fileInputStream = new FileInputStream(fileDescriptor);
                    try {
                        fileInputStream.skip(startPosition);
                        return create(fileInputStream, sourceOptions);
                    } catch (IOException unused) {
                        LOGGER.error("inputStream skip fail, reason : IOException", new Object[0]);
                        return null;
                    }
                } else {
                    throw new IllegalArgumentException("rawFileDes is null");
                }
            } catch (Exception e) {
                if (e instanceof IOException) {
                    LOGGER.error("create ImageSource from raw file descriptor fail, reason : IOException.", new Object[0]);
                    return null;
                }
                LOGGER.error("create ImageSource from raw file descriptor fail, reason : Exception.", new Object[0]);
                return null;
            }
        } else {
            throw new IllegalArgumentException("RawFileDescriptor is null");
        }
    }

    public static ImageSource create(byte[] bArr, int i, int i2, SourceOptions sourceOptions) {
        if (bArr == null) {
            throw new IllegalArgumentException("data is null");
        } else if (i < 0 || i2 < 0 || i >= bArr.length || i + i2 > bArr.length) {
            throw new IndexOutOfBoundsException("offset or length is invalid");
        } else {
            ImageSource nativeCreateImageSource = nativeCreateImageSource(bArr, i, i2, sourceOptions);
            inputOpts = sourceOptions;
            if (nativeCreateImageSource == null) {
                LOGGER.error("create ImageSource from data array fail. offset : %{public}d, length :  %{public}d.", Integer.valueOf(i), Integer.valueOf(i2));
                return null;
            }
            nativeCreateImageSource.imgDataSource = new ByteArrayDataSource(bArr, i, i2);
            fileSize = (long) i2;
            return nativeCreateImageSource;
        }
    }

    public static ImageSource create(String str, SourceOptions sourceOptions) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("pathName is invalid");
        } else if (Files.isReadable(Paths.get(str, new String[0]))) {
            ImageSource nativeCreateImageSource = nativeCreateImageSource(str, sourceOptions);
            inputOpts = sourceOptions;
            if (nativeCreateImageSource == null) {
                LOGGER.error("create ImageSource from file path fail", new Object[0]);
                return null;
            }
            nativeCreateImageSource.imgDataSource = new FilePathDataSource(str);
            fileSize = new File(str).length();
            return nativeCreateImageSource;
        } else {
            throw new DataSourceUnavailableException("pathName can not read");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0031, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0032, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0035, code lost:
        throw r5;
     */
    public static ImageSource create(File file, SourceOptions sourceOptions) {
        if (file != null) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ImageSource nativeCreateImageSource = nativeCreateImageSource(fileInputStream, sourceOptions);
                inputOpts = sourceOptions;
                if (nativeCreateImageSource == null) {
                    LOGGER.error("create ImageSource from file fail", new Object[0]);
                    $closeResource(null, fileInputStream);
                    return null;
                }
                nativeCreateImageSource.imgDataSource = new FileDataSource(file);
                $closeResource(null, fileInputStream);
                fileSize = file.length();
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0038, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003c, code lost:
        throw r5;
     */
    public static ImageSource create(FileDescriptor fileDescriptor, SourceOptions sourceOptions) {
        if (fileDescriptor == null || !fileDescriptor.valid()) {
            throw new IllegalArgumentException("fd is invalid");
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(fileDescriptor);
            ImageSource nativeCreateImageSource = nativeCreateImageSource(fileInputStream, sourceOptions);
            inputOpts = sourceOptions;
            if (nativeCreateImageSource == null) {
                LOGGER.error("createImageSource from fd fail", new Object[0]);
                $closeResource(null, fileInputStream);
                return null;
            }
            nativeCreateImageSource.imgDataSource = new FileDescriptorDataSource(fileDescriptor);
            fileSize = (long) fileInputStream.available();
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
        inputOpts = sourceOptions;
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
            return nativeCreatePixelmap(this, this.nativeImageSource, i, decodingOptions);
        }
        throw new IllegalArgumentException("index must not be negative");
    }

    public PixelMap createThumbnailPixelmap(DecodingOptions decodingOptions, boolean z) {
        LOGGER.error("enter createThumbnailPixelmap", new Object[0]);
        checkExifSource();
        LOGGER.error("createThumbnailPixelmap has thumbnail", new Object[0]);
        byte[] thumbnailBytes = this.exifAdapter.getThumbnailBytes();
        if (thumbnailBytes != null) {
            ImageSource create = create(thumbnailBytes, (SourceOptions) null);
            if (create == null) {
                LOGGER.error("create thumbnail image source failed", new Object[0]);
                return null;
            }
            LOGGER.error("createThumbnailPixelmap from thumbnail bytes", new Object[0]);
            return create.createPixelmap(decodingOptions);
        } else if (z) {
            LOGGER.info("create thumbnail pixel map from original image", new Object[0]);
            return createPixelmap(decodingOptions);
        } else {
            LOGGER.info("get thumbnail data null", new Object[0]);
            return null;
        }
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

    public final String getImagePropertyString(String str) {
        if (str != null) {
            checkExifSource();
            return this.exifAdapter.getImagePropertyString(str);
        }
        throw new IllegalArgumentException("key is null");
    }

    public final int getImagePropertyInt(String str, int i) {
        return getImagePropertyInt(0, str, i);
    }

    public final int getImagePropertyInt(int i, String str, int i2) {
        if (str != null) {
            checkExifSource();
            if (str.startsWith("GIF")) {
                return nativeGetImagePropertyInt(this.nativeImageSource, i, str, i2);
            }
            return this.exifAdapter.getImagePropertyInt(str, i2);
        }
        throw new IllegalArgumentException("key is null");
    }

    public ImageInfo getThumbnailInfo() {
        checkExifSource();
        byte[] thumbnailBytes = this.exifAdapter.getThumbnailBytes();
        if (thumbnailBytes != null) {
            ImageSource create = create(thumbnailBytes, (SourceOptions) null);
            if (create != null) {
                return create.getImageInfo();
            }
            LOGGER.error("getThumbnailInfo: create thumbnail image source failed", new Object[0]);
            return null;
        }
        throw new UnsupportedOperationException("image does not contain thumbnail");
    }

    public byte[] getImageThumbnailBytes() {
        checkExifSource();
        if (this.exifAdapter.getThumbnailBytes() != null) {
            return this.exifAdapter.getThumbnailBytes();
        }
        throw new UnsupportedOperationException("image does not contain thumbnail");
    }

    public int getThumbnailFormat() {
        checkExifSource();
        if (this.exifAdapter.getThumbnailBytes() != null) {
            return this.exifAdapter.getThumbnail() != null ? 3 : 0;
        }
        throw new UnsupportedOperationException("image does not contain thumbnail");
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
            this.exifAdapter = null;
        }
        this.imgDataSource = null;
    }

    public boolean isReleased() {
        return this.nativeImageSource == 0;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    private void checkExifSource() {
        checkReleased();
        if (this.exifAdapter == null) {
            synchronized (this.exifLock) {
                if (this.exifAdapter == null) {
                    try {
                        if (this.imgDataSource instanceof FilePathDataSource) {
                            this.exifAdapter = new ExifAdapter(((FilePathDataSource) this.imgDataSource).filePath);
                        } else if (this.imgDataSource instanceof ByteArrayDataSource) {
                            ByteArrayDataSource byteArrayDataSource = (ByteArrayDataSource) this.imgDataSource;
                            this.exifAdapter = new ExifAdapter(byteArrayDataSource.data, byteArrayDataSource.offset, byteArrayDataSource.length);
                        } else if (this.imgDataSource instanceof InputStreamDataSource) {
                            InputStreamDataSource inputStreamDataSource = (InputStreamDataSource) this.imgDataSource;
                            inputStreamDataSource.is.mark(0);
                            inputStreamDataSource.is.reset();
                            this.exifAdapter = new ExifAdapter(inputStreamDataSource.is);
                        } else if (this.imgDataSource instanceof FileDataSource) {
                            this.exifAdapter = new ExifAdapter(((FileDataSource) this.imgDataSource).file);
                        } else if (this.imgDataSource instanceof FileDescriptorDataSource) {
                            this.exifAdapter = new ExifAdapter(((FileDescriptorDataSource) this.imgDataSource).fd);
                        } else {
                            throw new IllegalStateException("image data source invalid");
                        }
                    } catch (IOException unused) {
                        LOGGER.error("create exif adapter from data source failed", new Object[0]);
                        throw new IllegalStateException("image data source invalid");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final double getImagePropertyDouble(String str, double d) {
        if (str != null) {
            checkExifSource();
            return this.exifAdapter.getImagePropertyDouble(str, d);
        }
        throw new IllegalArgumentException("key is null");
    }

    /* access modifiers changed from: package-private */
    public ExifAdapter getExifAdapterInstance() {
        checkExifSource();
        return this.exifAdapter;
    }

    /* access modifiers changed from: package-private */
    public ImageSource updateImageSource() {
        try {
            if (this.imgDataSource instanceof FilePathDataSource) {
                return create(((FilePathDataSource) this.imgDataSource).filePath, inputOpts);
            }
            if (this.imgDataSource instanceof ByteArrayDataSource) {
                ByteArrayDataSource byteArrayDataSource = (ByteArrayDataSource) this.imgDataSource;
                return create(byteArrayDataSource.data, byteArrayDataSource.offset, byteArrayDataSource.length, inputOpts);
            } else if (this.imgDataSource instanceof InputStreamDataSource) {
                InputStreamDataSource inputStreamDataSource = (InputStreamDataSource) this.imgDataSource;
                inputStreamDataSource.is.mark(0);
                inputStreamDataSource.is.reset();
                return create(inputStreamDataSource.is, inputOpts);
            } else if (this.imgDataSource instanceof FileDataSource) {
                return create(((FileDataSource) this.imgDataSource).file, inputOpts);
            } else {
                if (this.imgDataSource instanceof FileDescriptorDataSource) {
                    return create(((FileDescriptorDataSource) this.imgDataSource).fd, inputOpts);
                }
                LOGGER.error("update ImageSource failed.", new Object[0]);
                return null;
            }
        } catch (IOException unused) {
            LOGGER.error("update ImageSource failed of IOException", new Object[0]);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public long getFileSize() {
        long j = fileSize;
        if (j > 0) {
            return j;
        }
        return -1;
    }
}
