package android.media;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import dalvik.system.VMRuntime;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.NioUtils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageReader implements AutoCloseable {
    private static final int ACQUIRE_MAX_IMAGES = 2;
    private static final int ACQUIRE_NO_BUFS = 1;
    private static final int ACQUIRE_SUCCESS = 0;
    private static final long BUFFER_USAGE_UNKNOWN = 0;
    private List<Image> mAcquiredImages = new CopyOnWriteArrayList();
    /* access modifiers changed from: private */
    public final Object mCloseLock = new Object();
    private int mEstimatedNativeAllocBytes;
    /* access modifiers changed from: private */
    public final int mFormat;
    private final int mHeight;
    /* access modifiers changed from: private */
    public boolean mIsReaderValid = false;
    /* access modifiers changed from: private */
    public OnImageAvailableListener mListener;
    private ListenerHandler mListenerHandler;
    /* access modifiers changed from: private */
    public final Object mListenerLock = new Object();
    private final int mMaxImages;
    private long mNativeContext;
    /* access modifiers changed from: private */
    public final int mNumPlanes;
    private final Surface mSurface;
    private final int mWidth;

    private final class ListenerHandler extends Handler {
        public ListenerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            OnImageAvailableListener listener;
            boolean isReaderValid;
            synchronized (ImageReader.this.mListenerLock) {
                listener = ImageReader.this.mListener;
            }
            synchronized (ImageReader.this.mCloseLock) {
                isReaderValid = ImageReader.this.mIsReaderValid;
            }
            if (listener != null && isReaderValid) {
                listener.onImageAvailable(ImageReader.this);
            }
        }
    }

    public interface OnImageAvailableListener {
        void onImageAvailable(ImageReader imageReader);
    }

    private class SurfaceImage extends Image {
        private int mFormat = 0;
        private AtomicBoolean mIsDetached = new AtomicBoolean(false);
        private long mNativeBuffer;
        /* access modifiers changed from: private */
        public SurfacePlane[] mPlanes;
        private int mScalingMode;
        private long mTimestamp;
        private int mTransform;

        private class SurfacePlane extends Image.Plane {
            private ByteBuffer mBuffer;
            private final int mPixelStride;
            private final int mRowStride;

            private SurfacePlane(int rowStride, int pixelStride, ByteBuffer buffer) {
                this.mRowStride = rowStride;
                this.mPixelStride = pixelStride;
                this.mBuffer = buffer;
                this.mBuffer.order(ByteOrder.nativeOrder());
            }

            public ByteBuffer getBuffer() {
                SurfaceImage.this.throwISEIfImageIsInvalid();
                return this.mBuffer;
            }

            public int getPixelStride() {
                SurfaceImage.this.throwISEIfImageIsInvalid();
                if (ImageReader.this.mFormat != 36) {
                    return this.mPixelStride;
                }
                throw new UnsupportedOperationException("getPixelStride is not supported for RAW_PRIVATE plane");
            }

            public int getRowStride() {
                SurfaceImage.this.throwISEIfImageIsInvalid();
                if (ImageReader.this.mFormat != 36) {
                    return this.mRowStride;
                }
                throw new UnsupportedOperationException("getRowStride is not supported for RAW_PRIVATE plane");
            }

            /* access modifiers changed from: private */
            public void clearBuffer() {
                if (this.mBuffer != null) {
                    if (this.mBuffer.isDirect()) {
                        NioUtils.freeDirectBuffer(this.mBuffer);
                    }
                    this.mBuffer = null;
                }
            }
        }

        private native synchronized SurfacePlane[] nativeCreatePlanes(int i, int i2);

        private native synchronized int nativeGetFormat(int i);

        private native synchronized HardwareBuffer nativeGetHardwareBuffer();

        private native synchronized int nativeGetHeight();

        private native synchronized int nativeGetWidth();

        public SurfaceImage(int format) {
            this.mFormat = format;
        }

        public void close() {
            ImageReader.this.releaseImage(this);
        }

        public ImageReader getReader() {
            return ImageReader.this;
        }

        public int getFormat() {
            throwISEIfImageIsInvalid();
            int readerFormat = ImageReader.this.getImageFormat();
            this.mFormat = readerFormat == 34 ? readerFormat : nativeGetFormat(readerFormat);
            return this.mFormat;
        }

        public int getWidth() {
            throwISEIfImageIsInvalid();
            int format = getFormat();
            if (format != 36) {
                switch (format) {
                    case 256:
                    case 257:
                        break;
                    default:
                        return nativeGetWidth();
                }
            }
            return ImageReader.this.getWidth();
        }

        public int getHeight() {
            throwISEIfImageIsInvalid();
            int format = getFormat();
            if (format != 36) {
                switch (format) {
                    case 256:
                    case 257:
                        break;
                    default:
                        return nativeGetHeight();
                }
            }
            return ImageReader.this.getHeight();
        }

        public long getTimestamp() {
            throwISEIfImageIsInvalid();
            return this.mTimestamp;
        }

        public int getTransform() {
            throwISEIfImageIsInvalid();
            return this.mTransform;
        }

        public int getScalingMode() {
            throwISEIfImageIsInvalid();
            return this.mScalingMode;
        }

        public HardwareBuffer getHardwareBuffer() {
            throwISEIfImageIsInvalid();
            return nativeGetHardwareBuffer();
        }

        public void setTimestamp(long timestampNs) {
            throwISEIfImageIsInvalid();
            this.mTimestamp = timestampNs;
        }

        public Image.Plane[] getPlanes() {
            throwISEIfImageIsInvalid();
            if (this.mPlanes == null) {
                this.mPlanes = nativeCreatePlanes(ImageReader.this.mNumPlanes, ImageReader.this.mFormat);
            }
            return (Image.Plane[]) this.mPlanes.clone();
        }

        /* access modifiers changed from: protected */
        public final void finalize() throws Throwable {
            try {
                close();
            } finally {
                super.finalize();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isAttachable() {
            throwISEIfImageIsInvalid();
            return this.mIsDetached.get();
        }

        /* access modifiers changed from: package-private */
        public ImageReader getOwner() {
            throwISEIfImageIsInvalid();
            return ImageReader.this;
        }

        /* access modifiers changed from: package-private */
        public long getNativeContext() {
            throwISEIfImageIsInvalid();
            return this.mNativeBuffer;
        }

        /* access modifiers changed from: private */
        public void setDetached(boolean detached) {
            throwISEIfImageIsInvalid();
            this.mIsDetached.getAndSet(detached);
        }

        /* access modifiers changed from: private */
        public void clearSurfacePlanes() {
            if (this.mIsImageValid && this.mPlanes != null) {
                for (int i = 0; i < this.mPlanes.length; i++) {
                    if (this.mPlanes[i] != null) {
                        this.mPlanes[i].clearBuffer();
                        this.mPlanes[i] = null;
                    }
                }
            }
        }
    }

    private static native void nativeClassInit();

    private native synchronized void nativeClose();

    private native synchronized int nativeDetachImage(Image image);

    private native synchronized void nativeDiscardFreeBuffers();

    private native synchronized Surface nativeGetSurface();

    private native synchronized int nativeImageSetup(Image image);

    private native synchronized void nativeInit(Object obj, int i, int i2, int i3, int i4, long j);

    private native synchronized void nativeReleaseImage(Image image);

    public static ImageReader newInstance(int width, int height, int format, int maxImages) {
        ImageReader imageReader = new ImageReader(width, height, format, maxImages, 0);
        return imageReader;
    }

    public static ImageReader newInstance(int width, int height, int format, int maxImages, long usage) {
        if (isFormatUsageCombinationAllowed(format, usage)) {
            ImageReader imageReader = new ImageReader(width, height, format, maxImages, usage);
            return imageReader;
        }
        throw new IllegalArgumentException("Format usage combination is not supported: format = " + format + ", usage = " + usage);
    }

    protected ImageReader(int width, int height, int format, int maxImages, long usage) {
        int i = width;
        int i2 = height;
        int i3 = format;
        this.mWidth = i;
        this.mHeight = i2;
        this.mFormat = i3;
        int i4 = maxImages;
        this.mMaxImages = i4;
        if (i < 1 || i2 < 1) {
            throw new IllegalArgumentException("The image dimensions must be positive");
        } else if (this.mMaxImages < 1) {
            throw new IllegalArgumentException("Maximum outstanding image count must be at least 1");
        } else if (i3 != 17) {
            this.mNumPlanes = ImageUtils.getNumPlanesForFormat(this.mFormat);
            nativeInit(new WeakReference(this), i, i2, i3, i4, usage);
            this.mSurface = nativeGetSurface();
            this.mIsReaderValid = true;
            this.mEstimatedNativeAllocBytes = ImageUtils.getEstimatedNativeAllocBytes(i, i2, i3, 1);
            VMRuntime.getRuntime().registerNativeAllocation(this.mEstimatedNativeAllocBytes);
        } else {
            throw new IllegalArgumentException("NV21 format is not supported");
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getImageFormat() {
        return this.mFormat;
    }

    public int getMaxImages() {
        return this.mMaxImages;
    }

    public Surface getSurface() {
        return this.mSurface;
    }

    public Image acquireLatestImage() {
        Image image = acquireNextImage();
        if (image == null) {
            return null;
        }
        while (true) {
            try {
                Image next = acquireNextImageNoThrowISE();
                if (next == null) {
                    break;
                }
                image.close();
                image = next;
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }
        Image result = image;
        image = null;
        return result;
    }

    public Image acquireNextImageNoThrowISE() {
        SurfaceImage si = new SurfaceImage(this.mFormat);
        if (acquireNextSurfaceImage(si) == 0) {
            return si;
        }
        return null;
    }

    private int acquireNextSurfaceImage(SurfaceImage si) {
        int status;
        synchronized (this.mCloseLock) {
            status = 1;
            if (this.mIsReaderValid) {
                status = nativeImageSetup(si);
            }
            switch (status) {
                case 0:
                    si.mIsImageValid = true;
                    break;
                case 1:
                case 2:
                    break;
                default:
                    throw new AssertionError("Unknown nativeImageSetup return code " + status);
            }
            if (status == 0) {
                this.mAcquiredImages.add(si);
            }
        }
        return status;
    }

    public Image acquireNextImage() {
        SurfaceImage si = new SurfaceImage(this.mFormat);
        int status = acquireNextSurfaceImage(si);
        switch (status) {
            case 0:
                return si;
            case 1:
                return null;
            case 2:
                throw new IllegalStateException(String.format("maxImages (%d) has already been acquired, call #close before acquiring more.", new Object[]{Integer.valueOf(this.mMaxImages)}));
            default:
                throw new AssertionError("Unknown nativeImageSetup return code " + status);
        }
    }

    /* access modifiers changed from: private */
    public void releaseImage(Image i) {
        if (i instanceof SurfaceImage) {
            SurfaceImage si = (SurfaceImage) i;
            if (si.mIsImageValid) {
                if (si.getReader() != this || !this.mAcquiredImages.contains(i)) {
                    throw new IllegalArgumentException("This image was not produced by this ImageReader");
                }
                si.clearSurfacePlanes();
                nativeReleaseImage(i);
                si.mIsImageValid = false;
                this.mAcquiredImages.remove(i);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("This image was not produced by an ImageReader");
    }

    public void setOnImageAvailableListener(OnImageAvailableListener listener, Handler handler) {
        synchronized (this.mListenerLock) {
            if (listener != null) {
                Looper looper = handler != null ? handler.getLooper() : Looper.myLooper();
                if (looper != null) {
                    if (this.mListenerHandler == null || this.mListenerHandler.getLooper() != looper) {
                        this.mListenerHandler = new ListenerHandler(looper);
                    }
                    this.mListener = listener;
                } else {
                    throw new IllegalArgumentException("handler is null but the current thread is not a looper");
                }
            } else {
                this.mListener = null;
                this.mListenerHandler = null;
            }
        }
    }

    public void close() {
        setOnImageAvailableListener(null, null);
        if (this.mSurface != null) {
            this.mSurface.release();
        }
        synchronized (this.mCloseLock) {
            this.mIsReaderValid = false;
            for (Image image : this.mAcquiredImages) {
                image.close();
            }
            this.mAcquiredImages.clear();
            nativeClose();
            if (this.mEstimatedNativeAllocBytes > 0) {
                VMRuntime.getRuntime().registerNativeFree(this.mEstimatedNativeAllocBytes);
                this.mEstimatedNativeAllocBytes = 0;
            }
        }
    }

    public void discardFreeBuffers() {
        synchronized (this.mCloseLock) {
            nativeDiscardFreeBuffers();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /* access modifiers changed from: package-private */
    public void detachImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("input image must not be null");
        } else if (isImageOwnedbyMe(image)) {
            SurfaceImage si = (SurfaceImage) image;
            si.throwISEIfImageIsInvalid();
            if (!si.isAttachable()) {
                nativeDetachImage(image);
                si.clearSurfacePlanes();
                SurfaceImage.SurfacePlane[] unused = si.mPlanes = null;
                si.setDetached(true);
                return;
            }
            throw new IllegalStateException("Image was already detached from this ImageReader");
        } else {
            throw new IllegalArgumentException("Trying to detach an image that is not owned by this ImageReader");
        }
    }

    private boolean isImageOwnedbyMe(Image image) {
        boolean z = false;
        if (!(image instanceof SurfaceImage)) {
            return false;
        }
        if (((SurfaceImage) image).getReader() == this) {
            z = true;
        }
        return z;
    }

    private static boolean isFormatUsageCombinationAllowed(int format, long usage) {
        if ((ImageFormat.isPublicFormat(format) || PixelFormat.isPublicFormat(format)) && usage != 0) {
            return true;
        }
        return false;
    }

    private static void postEventFromNative(Object selfRef) {
        Handler handler;
        ImageReader ir = (ImageReader) ((WeakReference) selfRef).get();
        if (ir != null) {
            synchronized (ir.mListenerLock) {
                handler = ir.mListenerHandler;
            }
            if (handler != null) {
                handler.sendEmptyMessage(0);
            }
        }
    }

    static {
        System.loadLibrary("media_jni");
        nativeClassInit();
    }
}
