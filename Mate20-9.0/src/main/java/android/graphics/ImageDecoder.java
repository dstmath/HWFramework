package android.graphics;

import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.ColorSpace;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Size;
import android.util.TypedValue;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoUtils;

public final class ImageDecoder implements AutoCloseable {
    public static final int ALLOCATOR_DEFAULT = 0;
    public static final int ALLOCATOR_HARDWARE = 3;
    public static final int ALLOCATOR_SHARED_MEMORY = 2;
    public static final int ALLOCATOR_SOFTWARE = 1;
    @Deprecated
    public static final int ERROR_SOURCE_ERROR = 3;
    @Deprecated
    public static final int ERROR_SOURCE_EXCEPTION = 1;
    @Deprecated
    public static final int ERROR_SOURCE_INCOMPLETE = 2;
    public static final int MEMORY_POLICY_DEFAULT = 1;
    public static final int MEMORY_POLICY_LOW_RAM = 0;
    public static int sApiLevel;
    private int mAllocator = 0;
    /* access modifiers changed from: private */
    public final boolean mAnimated;
    /* access modifiers changed from: private */
    public AssetFileDescriptor mAssetFd;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private boolean mConserveMemory = false;
    private Rect mCropRect;
    private boolean mDecodeAsAlphaMask = false;
    private ColorSpace mDesiredColorSpace = null;
    private int mDesiredHeight;
    private int mDesiredWidth;
    /* access modifiers changed from: private */
    public final int mHeight;
    private InputStream mInputStream;
    private final boolean mIsNinePatch;
    private boolean mMutable = false;
    private long mNativePtr;
    private OnPartialImageListener mOnPartialImageListener;
    private Rect mOutPaddingRect;
    private boolean mOwnsInputStream;
    private PostProcessor mPostProcessor;
    private Source mSource;
    private byte[] mTempStorage;
    private boolean mUnpremultipliedRequired = false;
    /* access modifiers changed from: private */
    public final int mWidth;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Allocator {
    }

    public static class AssetInputStreamSource extends Source {
        private AssetManager.AssetInputStream mAssetInputStream;
        private final int mDensity;
        private final Resources mResources;

        public AssetInputStreamSource(AssetManager.AssetInputStream ais, Resources res, TypedValue value) {
            super();
            this.mAssetInputStream = ais;
            this.mResources = res;
            if (value.density == 0) {
                this.mDensity = 160;
            } else if (value.density != 65535) {
                this.mDensity = value.density;
            } else {
                this.mDensity = 0;
            }
        }

        public Resources getResources() {
            return this.mResources;
        }

        public int getDensity() {
            return this.mDensity;
        }

        public ImageDecoder createImageDecoder() throws IOException {
            ImageDecoder access$600;
            synchronized (this) {
                if (this.mAssetInputStream != null) {
                    AssetManager.AssetInputStream ais = this.mAssetInputStream;
                    this.mAssetInputStream = null;
                    access$600 = ImageDecoder.createFromAsset(ais, this);
                } else {
                    throw new IOException("Cannot reuse AssetInputStreamSource");
                }
            }
            return access$600;
        }
    }

    private static class AssetSource extends Source {
        private final AssetManager mAssets;
        private final String mFileName;

        AssetSource(AssetManager assets, String fileName) {
            super();
            this.mAssets = assets;
            this.mFileName = fileName;
        }

        public ImageDecoder createImageDecoder() throws IOException {
            return ImageDecoder.createFromAsset((AssetManager.AssetInputStream) this.mAssets.open(this.mFileName), this);
        }
    }

    private static class ByteArraySource extends Source {
        private final byte[] mData;
        private final int mLength;
        private final int mOffset;

        ByteArraySource(byte[] data, int offset, int length) {
            super();
            this.mData = data;
            this.mOffset = offset;
            this.mLength = length;
        }

        public ImageDecoder createImageDecoder() throws IOException {
            return ImageDecoder.nCreate(this.mData, this.mOffset, this.mLength, (Source) this);
        }
    }

    private static class ByteBufferSource extends Source {
        private final ByteBuffer mBuffer;

        ByteBufferSource(ByteBuffer buffer) {
            super();
            this.mBuffer = buffer;
        }

        public ImageDecoder createImageDecoder() throws IOException {
            if (this.mBuffer.isDirect() || !this.mBuffer.hasArray()) {
                ByteBuffer buffer = this.mBuffer.slice();
                return ImageDecoder.nCreate(buffer, buffer.position(), buffer.limit(), (Source) this);
            }
            return ImageDecoder.nCreate(this.mBuffer.array(), this.mBuffer.arrayOffset() + this.mBuffer.position(), this.mBuffer.limit() - this.mBuffer.position(), (Source) this);
        }
    }

    private static class ContentResolverSource extends Source {
        private final ContentResolver mResolver;
        private final Resources mResources;
        private final Uri mUri;

        ContentResolverSource(ContentResolver resolver, Uri uri, Resources res) {
            super();
            this.mResolver = resolver;
            this.mUri = uri;
            this.mResources = res;
        }

        /* access modifiers changed from: package-private */
        public Resources getResources() {
            return this.mResources;
        }

        public ImageDecoder createImageDecoder() throws IOException {
            AssetFileDescriptor assetFd;
            ImageDecoder decoder;
            try {
                if (this.mUri.getScheme() == "content") {
                    assetFd = this.mResolver.openTypedAssetFileDescriptor(this.mUri, "image/*", null);
                } else {
                    assetFd = this.mResolver.openAssetFileDescriptor(this.mUri, FullBackup.ROOT_TREE_TOKEN);
                }
                FileDescriptor fd = assetFd.getFileDescriptor();
                try {
                    Os.lseek(fd, assetFd.getStartOffset(), OsConstants.SEEK_SET);
                    decoder = ImageDecoder.nCreate(fd, (Source) this);
                } catch (ErrnoException e) {
                    decoder = ImageDecoder.createFromStream(new FileInputStream(fd), true, this);
                } catch (Throwable th) {
                    if (0 == 0) {
                        IoUtils.closeQuietly(assetFd);
                    } else {
                        AssetFileDescriptor unused = null.mAssetFd = assetFd;
                    }
                    throw th;
                }
                if (decoder == null) {
                    IoUtils.closeQuietly(assetFd);
                } else {
                    AssetFileDescriptor unused2 = decoder.mAssetFd = assetFd;
                }
                return decoder;
            } catch (FileNotFoundException e2) {
                InputStream is = this.mResolver.openInputStream(this.mUri);
                if (is != null) {
                    return ImageDecoder.createFromStream(is, true, this);
                }
                throw new FileNotFoundException(this.mUri.toString());
            }
        }
    }

    public static final class DecodeException extends IOException {
        public static final int SOURCE_EXCEPTION = 1;
        public static final int SOURCE_INCOMPLETE = 2;
        public static final int SOURCE_MALFORMED_DATA = 3;
        final int mError;
        final Source mSource;

        @Retention(RetentionPolicy.SOURCE)
        public @interface Error {
        }

        DecodeException(int error, Throwable cause, Source source) {
            super(errorMessage(error, cause), cause);
            this.mError = error;
            this.mSource = source;
        }

        DecodeException(int error, String msg, Throwable cause, Source source) {
            super(msg + errorMessage(error, cause), cause);
            this.mError = error;
            this.mSource = source;
        }

        public int getError() {
            return this.mError;
        }

        public Source getSource() {
            return this.mSource;
        }

        private static String errorMessage(int error, Throwable cause) {
            switch (error) {
                case 1:
                    return "Exception in input: " + cause;
                case 2:
                    return "Input was incomplete.";
                case 3:
                    return "Input contained an error.";
                default:
                    return "";
            }
        }
    }

    private static class FileSource extends Source {
        private final File mFile;

        FileSource(File file) {
            super();
            this.mFile = file;
        }

        public ImageDecoder createImageDecoder() throws IOException {
            return ImageDecoder.createFromFile(this.mFile, this);
        }
    }

    public static class ImageInfo {
        /* access modifiers changed from: private */
        public ImageDecoder mDecoder;
        private final Size mSize;

        private ImageInfo(ImageDecoder decoder) {
            this.mSize = new Size(decoder.mWidth, decoder.mHeight);
            this.mDecoder = decoder;
        }

        public Size getSize() {
            return this.mSize;
        }

        public String getMimeType() {
            return this.mDecoder.getMimeType();
        }

        public boolean isAnimated() {
            return this.mDecoder.mAnimated;
        }

        public ColorSpace getColorSpace() {
            return this.mDecoder.getColorSpace();
        }
    }

    @Deprecated
    public static class IncompleteException extends IOException {
    }

    private static class InputStreamSource extends Source {
        final int mInputDensity;
        InputStream mInputStream;
        final Resources mResources;

        InputStreamSource(Resources res, InputStream is, int inputDensity) {
            super();
            if (is != null) {
                this.mResources = res;
                this.mInputStream = is;
                this.mInputDensity = res != null ? inputDensity : 0;
                return;
            }
            throw new IllegalArgumentException("The InputStream cannot be null");
        }

        public Resources getResources() {
            return this.mResources;
        }

        public int getDensity() {
            return this.mInputDensity;
        }

        public ImageDecoder createImageDecoder() throws IOException {
            ImageDecoder access$300;
            synchronized (this) {
                if (this.mInputStream != null) {
                    InputStream is = this.mInputStream;
                    this.mInputStream = null;
                    access$300 = ImageDecoder.createFromStream(is, false, this);
                } else {
                    throw new IOException("Cannot reuse InputStreamSource");
                }
            }
            return access$300;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MemoryPolicy {
    }

    public interface OnHeaderDecodedListener {
        void onHeaderDecoded(ImageDecoder imageDecoder, ImageInfo imageInfo, Source source);
    }

    public interface OnPartialImageListener {
        boolean onPartialImage(DecodeException decodeException);
    }

    private static class ResourceSource extends Source {
        private Object mLock = new Object();
        int mResDensity;
        final int mResId;
        final Resources mResources;

        ResourceSource(Resources res, int resId) {
            super();
            this.mResources = res;
            this.mResId = resId;
            this.mResDensity = 0;
        }

        public Resources getResources() {
            return this.mResources;
        }

        public int getDensity() {
            int i;
            synchronized (this.mLock) {
                i = this.mResDensity;
            }
            return i;
        }

        public ImageDecoder createImageDecoder() throws IOException {
            TypedValue value = new TypedValue();
            InputStream is = this.mResources.openRawResource(this.mResId, value);
            synchronized (this.mLock) {
                if (value.density == 0) {
                    this.mResDensity = 160;
                } else if (value.density != 65535) {
                    this.mResDensity = value.density;
                }
            }
            return ImageDecoder.createFromAsset((AssetManager.AssetInputStream) is, this);
        }
    }

    public static abstract class Source {
        /* access modifiers changed from: package-private */
        public abstract ImageDecoder createImageDecoder() throws IOException;

        private Source() {
        }

        /* access modifiers changed from: package-private */
        public Resources getResources() {
            return null;
        }

        /* access modifiers changed from: package-private */
        public int getDensity() {
            return 0;
        }

        /* access modifiers changed from: package-private */
        public final int computeDstDensity() {
            Resources res = getResources();
            if (res == null) {
                return Bitmap.getDefaultDensity();
            }
            return res.getDisplayMetrics().densityDpi;
        }
    }

    private static native void nClose(long j);

    private static native ImageDecoder nCreate(long j, Source source) throws IOException;

    /* access modifiers changed from: private */
    public static native ImageDecoder nCreate(FileDescriptor fileDescriptor, Source source) throws IOException;

    private static native ImageDecoder nCreate(InputStream inputStream, byte[] bArr, Source source) throws IOException;

    /* access modifiers changed from: private */
    public static native ImageDecoder nCreate(ByteBuffer byteBuffer, int i, int i2, Source source) throws IOException;

    /* access modifiers changed from: private */
    public static native ImageDecoder nCreate(byte[] bArr, int i, int i2, Source source) throws IOException;

    private static native Bitmap nDecodeBitmap(long j, ImageDecoder imageDecoder, boolean z, int i, int i2, Rect rect, boolean z2, int i3, boolean z3, boolean z4, boolean z5, ColorSpace colorSpace) throws IOException;

    private static native ColorSpace nGetColorSpace(long j);

    private static native String nGetMimeType(long j);

    private static native void nGetPadding(long j, Rect rect);

    private static native Size nGetSampledSize(long j, int i);

    /* access modifiers changed from: private */
    public static ImageDecoder createFromFile(File file, Source source) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        FileDescriptor fd = stream.getFD();
        try {
            Os.lseek(fd, 0, OsConstants.SEEK_CUR);
            ImageDecoder decoder = null;
            try {
                decoder = nCreate(fd, source);
                if (decoder != null) {
                    decoder.mInputStream = stream;
                    decoder.mOwnsInputStream = true;
                }
                return decoder;
            } finally {
                if (decoder == null) {
                    IoUtils.closeQuietly(stream);
                } else {
                    decoder.mInputStream = stream;
                    decoder.mOwnsInputStream = true;
                }
            }
        } catch (ErrnoException e) {
            return createFromStream(stream, true, source);
        }
    }

    /* access modifiers changed from: private */
    public static ImageDecoder createFromStream(InputStream is, boolean closeInputStream, Source source) throws IOException {
        byte[] storage = new byte[16384];
        ImageDecoder decoder = null;
        try {
            decoder = nCreate(is, storage, source);
            if (decoder != null) {
                decoder.mInputStream = is;
                decoder.mOwnsInputStream = closeInputStream;
                decoder.mTempStorage = storage;
            }
            return decoder;
        } finally {
            if (decoder != null) {
                decoder.mInputStream = is;
                decoder.mOwnsInputStream = closeInputStream;
                decoder.mTempStorage = storage;
            } else if (closeInputStream) {
                IoUtils.closeQuietly(is);
            }
        }
    }

    /* access modifiers changed from: private */
    public static ImageDecoder createFromAsset(AssetManager.AssetInputStream ais, Source source) throws IOException {
        ImageDecoder decoder = null;
        try {
            decoder = nCreate(ais.getNativeAsset(), source);
            if (decoder != null) {
                decoder.mInputStream = ais;
                decoder.mOwnsInputStream = true;
            }
            return decoder;
        } finally {
            if (decoder == null) {
                IoUtils.closeQuietly(ais);
            } else {
                decoder.mInputStream = ais;
                decoder.mOwnsInputStream = true;
            }
        }
    }

    private ImageDecoder(long nativePtr, int width, int height, boolean animated, boolean isNinePatch) {
        this.mNativePtr = nativePtr;
        this.mWidth = width;
        this.mHeight = height;
        this.mDesiredWidth = width;
        this.mDesiredHeight = height;
        this.mAnimated = animated;
        this.mIsNinePatch = isNinePatch;
        this.mCloseGuard.open("close");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            this.mInputStream = null;
            this.mAssetFd = null;
            close();
        } finally {
            super.finalize();
        }
    }

    public static Source createSource(Resources res, int resId) {
        return new ResourceSource(res, resId);
    }

    public static Source createSource(ContentResolver cr, Uri uri) {
        return new ContentResolverSource(cr, uri, null);
    }

    public static Source createSource(ContentResolver cr, Uri uri, Resources res) {
        return new ContentResolverSource(cr, uri, res);
    }

    public static Source createSource(AssetManager assets, String fileName) {
        return new AssetSource(assets, fileName);
    }

    public static Source createSource(byte[] data, int offset, int length) throws ArrayIndexOutOfBoundsException {
        if (data == null) {
            throw new NullPointerException("null byte[] in createSource!");
        } else if (offset >= 0 && length >= 0 && offset < data.length && offset + length <= data.length) {
            return new ByteArraySource(data, offset, length);
        } else {
            throw new ArrayIndexOutOfBoundsException("invalid offset/length!");
        }
    }

    public static Source createSource(byte[] data) {
        return createSource(data, 0, data.length);
    }

    public static Source createSource(ByteBuffer buffer) {
        return new ByteBufferSource(buffer);
    }

    public static Source createSource(Resources res, InputStream is) {
        return new InputStreamSource(res, is, Bitmap.getDefaultDensity());
    }

    public static Source createSource(Resources res, InputStream is, int density) {
        return new InputStreamSource(res, is, density);
    }

    public static Source createSource(File file) {
        return new FileSource(file);
    }

    public Size getSampledSize(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("sampleSize must be positive! provided " + sampleSize);
        } else if (this.mNativePtr != 0) {
            return nGetSampledSize(this.mNativePtr, sampleSize);
        } else {
            throw new IllegalStateException("ImageDecoder is closed!");
        }
    }

    @Deprecated
    public ImageDecoder setResize(int width, int height) {
        setTargetSize(width, height);
        return this;
    }

    public void setTargetSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive! provided (" + width + ", " + height + ")");
        }
        this.mDesiredWidth = width;
        this.mDesiredHeight = height;
    }

    @Deprecated
    public ImageDecoder setResize(int sampleSize) {
        setTargetSampleSize(sampleSize);
        return this;
    }

    private int getTargetDimension(int original, int sampleSize, int computed) {
        if (sampleSize >= original) {
            return 1;
        }
        int target = original / sampleSize;
        if (computed != target && Math.abs((computed * sampleSize) - original) >= sampleSize) {
            return target;
        }
        return computed;
    }

    public void setTargetSampleSize(int sampleSize) {
        Size size = getSampledSize(sampleSize);
        setTargetSize(getTargetDimension(this.mWidth, sampleSize, size.getWidth()), getTargetDimension(this.mHeight, sampleSize, size.getHeight()));
    }

    private boolean requestedResize() {
        return (this.mWidth == this.mDesiredWidth && this.mHeight == this.mDesiredHeight) ? false : true;
    }

    public void setAllocator(int allocator) {
        if (allocator < 0 || allocator > 3) {
            throw new IllegalArgumentException("invalid allocator " + allocator);
        }
        this.mAllocator = allocator;
    }

    public int getAllocator() {
        return this.mAllocator;
    }

    public void setUnpremultipliedRequired(boolean unpremultipliedRequired) {
        this.mUnpremultipliedRequired = unpremultipliedRequired;
    }

    @Deprecated
    public ImageDecoder setRequireUnpremultiplied(boolean unpremultipliedRequired) {
        setUnpremultipliedRequired(unpremultipliedRequired);
        return this;
    }

    public boolean isUnpremultipliedRequired() {
        return this.mUnpremultipliedRequired;
    }

    @Deprecated
    public boolean getRequireUnpremultiplied() {
        return isUnpremultipliedRequired();
    }

    public void setPostProcessor(PostProcessor postProcessor) {
        this.mPostProcessor = postProcessor;
    }

    public PostProcessor getPostProcessor() {
        return this.mPostProcessor;
    }

    public void setOnPartialImageListener(OnPartialImageListener listener) {
        this.mOnPartialImageListener = listener;
    }

    public OnPartialImageListener getOnPartialImageListener() {
        return this.mOnPartialImageListener;
    }

    public void setCrop(Rect subset) {
        this.mCropRect = subset;
    }

    public Rect getCrop() {
        return this.mCropRect;
    }

    public void setOutPaddingRect(Rect outPadding) {
        this.mOutPaddingRect = outPadding;
    }

    public void setMutableRequired(boolean mutable) {
        this.mMutable = mutable;
    }

    @Deprecated
    public ImageDecoder setMutable(boolean mutable) {
        setMutableRequired(mutable);
        return this;
    }

    public boolean isMutableRequired() {
        return this.mMutable;
    }

    @Deprecated
    public boolean getMutable() {
        return isMutableRequired();
    }

    public void setMemorySizePolicy(int policy) {
        this.mConserveMemory = policy == 0;
    }

    public int getMemorySizePolicy() {
        return this.mConserveMemory ^ true ? 1 : 0;
    }

    @Deprecated
    public void setConserveMemory(boolean conserveMemory) {
        this.mConserveMemory = conserveMemory;
    }

    @Deprecated
    public boolean getConserveMemory() {
        return this.mConserveMemory;
    }

    public void setDecodeAsAlphaMaskEnabled(boolean enabled) {
        this.mDecodeAsAlphaMask = enabled;
    }

    @Deprecated
    public ImageDecoder setDecodeAsAlphaMask(boolean enabled) {
        setDecodeAsAlphaMaskEnabled(enabled);
        return this;
    }

    @Deprecated
    public ImageDecoder setAsAlphaMask(boolean asAlphaMask) {
        setDecodeAsAlphaMask(asAlphaMask);
        return this;
    }

    public boolean isDecodeAsAlphaMaskEnabled() {
        return this.mDecodeAsAlphaMask;
    }

    @Deprecated
    public boolean getDecodeAsAlphaMask() {
        return this.mDecodeAsAlphaMask;
    }

    @Deprecated
    public boolean getAsAlphaMask() {
        return getDecodeAsAlphaMask();
    }

    public void setTargetColorSpace(ColorSpace colorSpace) {
        this.mDesiredColorSpace = colorSpace;
    }

    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            nClose(this.mNativePtr);
            this.mNativePtr = 0;
            if (this.mOwnsInputStream) {
                IoUtils.closeQuietly(this.mInputStream);
            }
            IoUtils.closeQuietly(this.mAssetFd);
            this.mInputStream = null;
            this.mAssetFd = null;
            this.mTempStorage = null;
        }
    }

    private void checkState() {
        if (this.mNativePtr != 0) {
            checkSubset(this.mDesiredWidth, this.mDesiredHeight, this.mCropRect);
            if (this.mAllocator == 3) {
                if (this.mMutable) {
                    throw new IllegalStateException("Cannot make mutable HARDWARE Bitmap!");
                } else if (this.mDecodeAsAlphaMask) {
                    throw new IllegalStateException("Cannot make HARDWARE Alpha mask Bitmap!");
                }
            }
            if (this.mPostProcessor != null && this.mUnpremultipliedRequired) {
                throw new IllegalStateException("Cannot draw to unpremultiplied pixels!");
            } else if (this.mDesiredColorSpace == null) {
            } else {
                if (!(this.mDesiredColorSpace instanceof ColorSpace.Rgb)) {
                    throw new IllegalArgumentException("The target color space must use the RGB color model - provided: " + this.mDesiredColorSpace);
                } else if (((ColorSpace.Rgb) this.mDesiredColorSpace).getTransferParameters() == null) {
                    throw new IllegalArgumentException("The target color space must use an ICC parametric transfer function - provided: " + this.mDesiredColorSpace);
                }
            }
        } else {
            throw new IllegalStateException("Cannot use closed ImageDecoder!");
        }
    }

    private static void checkSubset(int width, int height, Rect r) {
        if (r != null) {
            if (r.left < 0 || r.top < 0 || r.right > width || r.bottom > height) {
                throw new IllegalStateException("Subset " + r + " not contained by scaled image bounds: (" + width + " x " + height + ")");
            }
        }
    }

    private Bitmap decodeBitmapInternal() throws IOException {
        checkState();
        return nDecodeBitmap(this.mNativePtr, this, this.mPostProcessor != null, this.mDesiredWidth, this.mDesiredHeight, this.mCropRect, this.mMutable, this.mAllocator, this.mUnpremultipliedRequired, this.mConserveMemory, this.mDecodeAsAlphaMask, this.mDesiredColorSpace);
    }

    private void callHeaderDecoded(OnHeaderDecodedListener listener, Source src) {
        if (listener != null) {
            ImageInfo info = new ImageInfo();
            try {
                listener.onHeaderDecoded(this, info, src);
            } finally {
                ImageDecoder unused = info.mDecoder = null;
            }
        }
    }

    public static Drawable decodeDrawable(Source src, OnHeaderDecodedListener listener) throws IOException {
        if (listener != null) {
            return decodeDrawableImpl(src, listener);
        }
        throw new IllegalArgumentException("listener cannot be null! Use decodeDrawable(Source) to not have a listener");
    }

    private static Drawable decodeDrawableImpl(Source src, OnHeaderDecodedListener listener) throws IOException {
        Source source = src;
        ImageDecoder decoder = src.createImageDecoder();
        Throwable th = null;
        try {
            decoder.mSource = source;
            try {
                decoder.callHeaderDecoded(listener, source);
                if (decoder.mUnpremultipliedRequired) {
                    throw new IllegalStateException("Cannot decode a Drawable with unpremultiplied pixels!");
                } else if (!decoder.mMutable) {
                    int srcDensity = decoder.computeDensity(source);
                    if (decoder.mAnimated) {
                        AnimatedImageDrawable animatedImageDrawable = new AnimatedImageDrawable(decoder.mNativePtr, decoder.mPostProcessor == null ? null : decoder, decoder.mDesiredWidth, decoder.mDesiredHeight, srcDensity, src.computeDstDensity(), decoder.mCropRect, decoder.mInputStream, decoder.mAssetFd);
                        Drawable d = animatedImageDrawable;
                        decoder.mInputStream = null;
                        decoder.mAssetFd = null;
                        if (decoder != null) {
                            $closeResource(null, decoder);
                        }
                        return d;
                    }
                    Bitmap bm = decoder.decodeBitmapInternal();
                    bm.setDensity(srcDensity);
                    Resources res = src.getResources();
                    byte[] np = bm.getNinePatchChunk();
                    if (np == null || !NinePatch.isNinePatchChunk(np)) {
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(res, bm);
                        if (decoder != null) {
                            $closeResource(null, decoder);
                        }
                        return bitmapDrawable;
                    }
                    Rect opticalInsets = new Rect();
                    bm.getOpticalInsets(opticalInsets);
                    Rect padding = decoder.mOutPaddingRect;
                    if (padding == null) {
                        padding = new Rect();
                    }
                    Rect padding2 = padding;
                    nGetPadding(decoder.mNativePtr, padding2);
                    Rect rect = padding2;
                    NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(res, bm, np, padding2, opticalInsets, null);
                    if (decoder != null) {
                        $closeResource(null, decoder);
                    }
                    return ninePatchDrawable;
                } else {
                    throw new IllegalStateException("Cannot decode a mutable Drawable!");
                }
            } catch (Throwable th2) {
                th = th2;
                th = th;
                try {
                    throw th;
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        } catch (Throwable th4) {
            th = th4;
            OnHeaderDecodedListener onHeaderDecodedListener = listener;
            if (decoder != null) {
                $closeResource(th, decoder);
            }
            throw th;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public static Drawable decodeDrawable(Source src) throws IOException {
        return decodeDrawableImpl(src, null);
    }

    public static Bitmap decodeBitmap(Source src, OnHeaderDecodedListener listener) throws IOException {
        if (listener != null) {
            return decodeBitmapImpl(src, listener);
        }
        throw new IllegalArgumentException("listener cannot be null! Use decodeBitmap(Source) to not have a listener");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0031, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        if (r0 != null) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0037, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003a, code lost:
        throw r2;
     */
    private static Bitmap decodeBitmapImpl(Source src, OnHeaderDecodedListener listener) throws IOException {
        ImageDecoder decoder = src.createImageDecoder();
        decoder.mSource = src;
        decoder.callHeaderDecoded(listener, src);
        int srcDensity = decoder.computeDensity(src);
        Bitmap bm = decoder.decodeBitmapInternal();
        bm.setDensity(srcDensity);
        Rect padding = decoder.mOutPaddingRect;
        if (padding != null) {
            byte[] np = bm.getNinePatchChunk();
            if (np != null && NinePatch.isNinePatchChunk(np)) {
                nGetPadding(decoder.mNativePtr, padding);
            }
        }
        if (decoder != null) {
            $closeResource(null, decoder);
        }
        return bm;
    }

    private int computeDensity(Source src) {
        if (requestedResize()) {
            return 0;
        }
        int srcDensity = src.getDensity();
        if (srcDensity == 0) {
            return srcDensity;
        }
        if (this.mIsNinePatch && this.mPostProcessor == null) {
            return srcDensity;
        }
        Resources res = src.getResources();
        if (res != null && res.getDisplayMetrics().noncompatDensityDpi == srcDensity) {
            return srcDensity;
        }
        int dstDensity = src.computeDstDensity();
        if (srcDensity == dstDensity) {
            return srcDensity;
        }
        if (srcDensity < dstDensity && sApiLevel >= 28) {
            return srcDensity;
        }
        float scale = ((float) dstDensity) / ((float) srcDensity);
        setTargetSize((int) ((((float) this.mWidth) * scale) + 0.5f), (int) ((((float) this.mHeight) * scale) + 0.5f));
        return dstDensity;
    }

    /* access modifiers changed from: private */
    public String getMimeType() {
        return nGetMimeType(this.mNativePtr);
    }

    /* access modifiers changed from: private */
    public ColorSpace getColorSpace() {
        return nGetColorSpace(this.mNativePtr);
    }

    public static Bitmap decodeBitmap(Source src) throws IOException {
        return decodeBitmapImpl(src, null);
    }

    private int postProcessAndRelease(Canvas canvas) {
        try {
            return this.mPostProcessor.onPostProcess(canvas);
        } finally {
            canvas.release();
        }
    }

    private void onPartialImage(int error, Throwable cause) throws DecodeException {
        DecodeException exception = new DecodeException(error, cause, this.mSource);
        if (this.mOnPartialImageListener == null || !this.mOnPartialImageListener.onPartialImage(exception)) {
            throw exception;
        }
    }
}
