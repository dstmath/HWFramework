package android.media;

import android.media.Image.Plane;
import android.net.wifi.AnqpInformationElement;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.rms.HwSysResource;
import android.speech.tts.TextToSpeech;
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
    private List<Image> mAcquiredImages;
    private final Object mCloseLock;
    private int mEstimatedNativeAllocBytes;
    private final int mFormat;
    private final int mHeight;
    private boolean mIsReaderValid;
    private OnImageAvailableListener mListener;
    private ListenerHandler mListenerHandler;
    private final Object mListenerLock;
    private final int mMaxImages;
    private long mNativeContext;
    private final int mNumPlanes;
    private final Surface mSurface;
    private final int mWidth;

    private final class ListenerHandler extends Handler {
        public ListenerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            synchronized (ImageReader.this.mListenerLock) {
                OnImageAvailableListener listener = ImageReader.this.mListener;
            }
            synchronized (ImageReader.this.mCloseLock) {
                boolean isReaderValid = ImageReader.this.mIsReaderValid;
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
        private int mFormat;
        private AtomicBoolean mIsDetached;
        private long mNativeBuffer;
        private SurfacePlane[] mPlanes;
        private long mTimestamp;

        private class SurfacePlane extends Plane {
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

            private void clearBuffer() {
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

        private native synchronized int nativeGetHeight();

        private native synchronized int nativeGetWidth();

        public SurfaceImage(int format) {
            this.mFormat = 0;
            this.mIsDetached = new AtomicBoolean(false);
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
            if (readerFormat != 34) {
                readerFormat = nativeGetFormat(readerFormat);
            }
            this.mFormat = readerFormat;
            return this.mFormat;
        }

        public int getWidth() {
            throwISEIfImageIsInvalid();
            switch (getFormat()) {
                case HwSysResource.ACTIVITY /*36*/:
                case TriangleMeshBuilder.TEXTURE_0 /*256*/:
                case AnqpInformationElement.ANQP_CAPABILITY_LIST /*257*/:
                    return ImageReader.this.getWidth();
                default:
                    return nativeGetWidth();
            }
        }

        public int getHeight() {
            throwISEIfImageIsInvalid();
            switch (getFormat()) {
                case HwSysResource.ACTIVITY /*36*/:
                case TriangleMeshBuilder.TEXTURE_0 /*256*/:
                case AnqpInformationElement.ANQP_CAPABILITY_LIST /*257*/:
                    return ImageReader.this.getHeight();
                default:
                    return nativeGetHeight();
            }
        }

        public long getTimestamp() {
            throwISEIfImageIsInvalid();
            return this.mTimestamp;
        }

        public void setTimestamp(long timestampNs) {
            throwISEIfImageIsInvalid();
            this.mTimestamp = timestampNs;
        }

        public Plane[] getPlanes() {
            throwISEIfImageIsInvalid();
            if (this.mPlanes == null) {
                this.mPlanes = nativeCreatePlanes(ImageReader.this.mNumPlanes, ImageReader.this.mFormat);
            }
            return (Plane[]) this.mPlanes.clone();
        }

        protected final void finalize() throws Throwable {
            try {
                close();
            } finally {
                super.finalize();
            }
        }

        boolean isAttachable() {
            throwISEIfImageIsInvalid();
            return this.mIsDetached.get();
        }

        ImageReader getOwner() {
            throwISEIfImageIsInvalid();
            return ImageReader.this;
        }

        long getNativeContext() {
            throwISEIfImageIsInvalid();
            return this.mNativeBuffer;
        }

        private void setDetached(boolean detached) {
            throwISEIfImageIsInvalid();
            this.mIsDetached.getAndSet(detached);
        }

        private void clearSurfacePlanes() {
            if (this.mIsImageValid && this.mPlanes != null) {
                for (int i = 0; i < this.mPlanes.length; i += ImageReader.ACQUIRE_NO_BUFS) {
                    if (this.mPlanes[i] != null) {
                        this.mPlanes[i].clearBuffer();
                        this.mPlanes[i] = null;
                    }
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.ImageReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.ImageReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.ImageReader.<clinit>():void");
    }

    private static native void nativeClassInit();

    private native synchronized void nativeClose();

    private native synchronized int nativeDetachImage(Image image);

    private native synchronized Surface nativeGetSurface();

    private native synchronized int nativeImageSetup(Image image);

    private native synchronized void nativeInit(Object obj, int i, int i2, int i3, int i4);

    private native synchronized void nativeReleaseImage(Image image);

    public static ImageReader newInstance(int width, int height, int format, int maxImages) {
        return new ImageReader(width, height, format, maxImages);
    }

    protected ImageReader(int width, int height, int format, int maxImages) {
        this.mListenerLock = new Object();
        this.mCloseLock = new Object();
        this.mIsReaderValid = false;
        this.mAcquiredImages = new CopyOnWriteArrayList();
        this.mWidth = width;
        this.mHeight = height;
        this.mFormat = format;
        this.mMaxImages = maxImages;
        if (width < ACQUIRE_NO_BUFS || height < ACQUIRE_NO_BUFS) {
            throw new IllegalArgumentException("The image dimensions must be positive");
        } else if (this.mMaxImages < ACQUIRE_NO_BUFS) {
            throw new IllegalArgumentException("Maximum outstanding image count must be at least 1");
        } else if (format == 17) {
            throw new IllegalArgumentException("NV21 format is not supported");
        } else {
            this.mNumPlanes = ImageUtils.getNumPlanesForFormat(this.mFormat);
            nativeInit(new WeakReference(this), width, height, format, maxImages);
            this.mSurface = nativeGetSurface();
            this.mIsReaderValid = true;
            this.mEstimatedNativeAllocBytes = ImageUtils.getEstimatedNativeAllocBytes(width, height, format, ACQUIRE_NO_BUFS);
            VMRuntime.getRuntime().registerNativeAllocation(this.mEstimatedNativeAllocBytes);
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
            Image next = acquireNextImageNoThrowISE();
            if (next == null) {
                return image;
            }
            try {
                image.close();
                image = next;
            } catch (Throwable th) {
                if (image != null) {
                    image.close();
                }
            }
        }
    }

    public Image acquireNextImageNoThrowISE() {
        SurfaceImage si = new SurfaceImage(this.mFormat);
        return acquireNextSurfaceImage(si) == 0 ? si : null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int acquireNextSurfaceImage(SurfaceImage si) {
        int status;
        synchronized (this.mCloseLock) {
            status = ACQUIRE_NO_BUFS;
            if (this.mIsReaderValid) {
                status = nativeImageSetup(si);
            }
            switch (status) {
                case TextToSpeech.SUCCESS /*0*/:
                    si.mIsImageValid = true;
                    break;
                case ACQUIRE_NO_BUFS /*1*/:
                case ACQUIRE_MAX_IMAGES /*2*/:
                    break;
                default:
                    throw new AssertionError("Unknown nativeImageSetup return code " + status);
            }
        }
        return status;
    }

    public Image acquireNextImage() {
        SurfaceImage si = new SurfaceImage(this.mFormat);
        int status = acquireNextSurfaceImage(si);
        switch (status) {
            case TextToSpeech.SUCCESS /*0*/:
                return si;
            case ACQUIRE_NO_BUFS /*1*/:
                return null;
            case ACQUIRE_MAX_IMAGES /*2*/:
                Object[] objArr = new Object[ACQUIRE_NO_BUFS];
                objArr[0] = Integer.valueOf(this.mMaxImages);
                throw new IllegalStateException(String.format("maxImages (%d) has already been acquired, call #close before acquiring more.", objArr));
            default:
                throw new AssertionError("Unknown nativeImageSetup return code " + status);
        }
    }

    private void releaseImage(Image i) {
        if (i instanceof SurfaceImage) {
            SurfaceImage si = (SurfaceImage) i;
            if (!si.mIsImageValid) {
                return;
            }
            if (si.getReader() == this && this.mAcquiredImages.contains(i)) {
                si.clearSurfacePlanes();
                nativeReleaseImage(i);
                si.mIsImageValid = false;
                this.mAcquiredImages.remove(i);
                return;
            }
            throw new IllegalArgumentException("This image was not produced by this ImageReader");
        }
        throw new IllegalArgumentException("This image was not produced by an ImageReader");
    }

    public void setOnImageAvailableListener(OnImageAvailableListener listener, Handler handler) {
        synchronized (this.mListenerLock) {
            if (listener != null) {
                Looper looper = handler != null ? handler.getLooper() : Looper.myLooper();
                if (looper == null) {
                    throw new IllegalArgumentException("handler is null but the current thread is not a looper");
                }
                if (this.mListenerHandler == null || this.mListenerHandler.getLooper() != looper) {
                    this.mListenerHandler = new ListenerHandler(looper);
                }
                this.mListener = listener;
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
        }
        if (this.mEstimatedNativeAllocBytes > 0) {
            VMRuntime.getRuntime().registerNativeFree(this.mEstimatedNativeAllocBytes);
            this.mEstimatedNativeAllocBytes = 0;
        }
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    void detachImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("input image must not be null");
        } else if (isImageOwnedbyMe(image)) {
            SurfaceImage si = (SurfaceImage) image;
            si.throwISEIfImageIsInvalid();
            if (si.isAttachable()) {
                throw new IllegalStateException("Image was already detached from this ImageReader");
            }
            nativeDetachImage(image);
            si.setDetached(true);
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

    private static void postEventFromNative(Object selfRef) {
        ImageReader ir = (ImageReader) ((WeakReference) selfRef).get();
        if (ir != null) {
            Handler handler;
            synchronized (ir.mListenerLock) {
                handler = ir.mListenerHandler;
            }
            if (handler != null) {
                handler.sendEmptyMessage(0);
            }
        }
    }
}
