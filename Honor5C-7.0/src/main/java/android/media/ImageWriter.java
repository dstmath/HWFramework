package android.media;

import android.graphics.Rect;
import android.hardware.camera2.utils.SurfaceUtils;
import android.media.Image.Plane;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Size;
import android.view.Surface;
import dalvik.system.VMRuntime;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.NioUtils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ImageWriter implements AutoCloseable {
    private List<Image> mDequeuedImages;
    private int mEstimatedNativeAllocBytes;
    private OnImageReleasedListener mListener;
    private ListenerHandler mListenerHandler;
    private final Object mListenerLock;
    private final int mMaxImages;
    private long mNativeContext;
    private int mWriterFormat;

    private final class ListenerHandler extends Handler {
        public ListenerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            synchronized (ImageWriter.this.mListenerLock) {
                OnImageReleasedListener listener = ImageWriter.this.mListener;
            }
            if (listener != null) {
                listener.onImageReleased(ImageWriter.this);
            }
        }
    }

    public interface OnImageReleasedListener {
        void onImageReleased(ImageWriter imageWriter);
    }

    private static class WriterSurfaceImage extends Image {
        private final long DEFAULT_TIMESTAMP;
        private int mFormat;
        private int mHeight;
        private long mNativeBuffer;
        private int mNativeFenceFd;
        private ImageWriter mOwner;
        private SurfacePlane[] mPlanes;
        private long mTimestamp;
        private int mWidth;

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

            public int getRowStride() {
                WriterSurfaceImage.this.throwISEIfImageIsInvalid();
                return this.mRowStride;
            }

            public int getPixelStride() {
                WriterSurfaceImage.this.throwISEIfImageIsInvalid();
                return this.mPixelStride;
            }

            public ByteBuffer getBuffer() {
                WriterSurfaceImage.this.throwISEIfImageIsInvalid();
                return this.mBuffer;
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

        private native synchronized int nativeGetFormat();

        private native synchronized int nativeGetHeight();

        private native synchronized int nativeGetWidth();

        public WriterSurfaceImage(ImageWriter writer) {
            this.mNativeFenceFd = -1;
            this.mHeight = -1;
            this.mWidth = -1;
            this.mFormat = -1;
            this.DEFAULT_TIMESTAMP = Long.MIN_VALUE;
            this.mTimestamp = Long.MIN_VALUE;
            this.mOwner = writer;
        }

        public int getFormat() {
            throwISEIfImageIsInvalid();
            if (this.mFormat == -1) {
                this.mFormat = nativeGetFormat();
            }
            return this.mFormat;
        }

        public int getWidth() {
            throwISEIfImageIsInvalid();
            if (this.mWidth == -1) {
                this.mWidth = nativeGetWidth();
            }
            return this.mWidth;
        }

        public int getHeight() {
            throwISEIfImageIsInvalid();
            if (this.mHeight == -1) {
                this.mHeight = nativeGetHeight();
            }
            return this.mHeight;
        }

        public long getTimestamp() {
            throwISEIfImageIsInvalid();
            return this.mTimestamp;
        }

        public void setTimestamp(long timestamp) {
            throwISEIfImageIsInvalid();
            this.mTimestamp = timestamp;
        }

        public Plane[] getPlanes() {
            throwISEIfImageIsInvalid();
            if (this.mPlanes == null) {
                this.mPlanes = nativeCreatePlanes(ImageUtils.getNumPlanesForFormat(getFormat()), getOwner().getFormat());
            }
            return (Plane[]) this.mPlanes.clone();
        }

        boolean isAttachable() {
            throwISEIfImageIsInvalid();
            return false;
        }

        ImageWriter getOwner() {
            throwISEIfImageIsInvalid();
            return this.mOwner;
        }

        long getNativeContext() {
            throwISEIfImageIsInvalid();
            return this.mNativeBuffer;
        }

        public void close() {
            if (this.mIsImageValid) {
                getOwner().abortImage(this);
            }
        }

        protected final void finalize() throws Throwable {
            try {
                close();
            } finally {
                super.finalize();
            }
        }

        private void clearSurfacePlanes() {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.ImageWriter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.ImageWriter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.ImageWriter.<clinit>():void");
    }

    private native synchronized void cancelImage(long j, Image image);

    private native synchronized int nativeAttachAndQueueImage(long j, long j2, int i, long j3, int i2, int i3, int i4, int i5);

    private static native void nativeClassInit();

    private native synchronized void nativeClose(long j);

    private native synchronized void nativeDequeueInputImage(long j, Image image);

    private native synchronized long nativeInit(Object obj, Surface surface, int i);

    private native synchronized void nativeQueueInputImage(long j, Image image, long j2, int i, int i2, int i3, int i4);

    public static ImageWriter newInstance(Surface surface, int maxImages) {
        return new ImageWriter(surface, maxImages);
    }

    protected ImageWriter(Surface surface, int maxImages) {
        this.mListenerLock = new Object();
        this.mDequeuedImages = new CopyOnWriteArrayList();
        if (surface == null || maxImages < 1) {
            throw new IllegalArgumentException("Illegal input argument: surface " + surface + ", maxImages: " + maxImages);
        }
        this.mMaxImages = maxImages;
        this.mNativeContext = nativeInit(new WeakReference(this), surface, maxImages);
        Size surfSize = SurfaceUtils.getSurfaceSize(surface);
        this.mEstimatedNativeAllocBytes = ImageUtils.getEstimatedNativeAllocBytes(surfSize.getWidth(), surfSize.getHeight(), SurfaceUtils.getSurfaceFormat(surface), 1);
        VMRuntime.getRuntime().registerNativeAllocation(this.mEstimatedNativeAllocBytes);
    }

    public int getMaxImages() {
        return this.mMaxImages;
    }

    public Image dequeueInputImage() {
        if (this.mWriterFormat == 34) {
            throw new IllegalStateException("PRIVATE format ImageWriter doesn't support this operation since the images are inaccessible to the application!");
        } else if (this.mDequeuedImages.size() >= this.mMaxImages) {
            throw new IllegalStateException("Already dequeued max number of Images " + this.mMaxImages);
        } else {
            WriterSurfaceImage newImage = new WriterSurfaceImage(this);
            nativeDequeueInputImage(this.mNativeContext, newImage);
            this.mDequeuedImages.add(newImage);
            newImage.mIsImageValid = true;
            return newImage;
        }
    }

    public void queueInputImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("image shouldn't be null");
        }
        boolean ownedByMe = isImageOwnedByMe(image);
        if (!ownedByMe || ((WriterSurfaceImage) image).mIsImageValid) {
            if (!ownedByMe) {
                if (image.getOwner() instanceof ImageReader) {
                    ImageReader prevOwner = (ImageReader) image.getOwner();
                    if (image.getFormat() == 34) {
                        prevOwner.detachImage(image);
                        attachAndQueueInputImage(image);
                        image.close();
                        return;
                    }
                    Image inputImage = dequeueInputImage();
                    inputImage.setTimestamp(image.getTimestamp());
                    inputImage.setCropRect(image.getCropRect());
                    ImageUtils.imageCopy(image, inputImage);
                    image.close();
                    image = inputImage;
                    ownedByMe = true;
                } else {
                    throw new IllegalArgumentException("Only images from ImageReader can be queued to ImageWriter, other image source is not supported yet!");
                }
            }
            Rect crop = image.getCropRect();
            nativeQueueInputImage(this.mNativeContext, image, image.getTimestamp(), crop.left, crop.top, crop.right, crop.bottom);
            if (ownedByMe) {
                this.mDequeuedImages.remove(image);
                WriterSurfaceImage wi = (WriterSurfaceImage) image;
                wi.clearSurfacePlanes();
                wi.mIsImageValid = false;
            }
            return;
        }
        throw new IllegalStateException("Image from ImageWriter is invalid");
    }

    public int getFormat() {
        return this.mWriterFormat;
    }

    public void setOnImageReleasedListener(OnImageReleasedListener listener, Handler handler) {
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
        setOnImageReleasedListener(null, null);
        for (Image image : this.mDequeuedImages) {
            image.close();
        }
        this.mDequeuedImages.clear();
        nativeClose(this.mNativeContext);
        this.mNativeContext = 0;
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

    private void attachAndQueueInputImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("image shouldn't be null");
        } else if (isImageOwnedByMe(image)) {
            throw new IllegalArgumentException("Can not attach an image that is owned ImageWriter already");
        } else if (image.isAttachable()) {
            Rect crop = image.getCropRect();
            nativeAttachAndQueueImage(this.mNativeContext, image.getNativeContext(), image.getFormat(), image.getTimestamp(), crop.left, crop.top, crop.right, crop.bottom);
        } else {
            throw new IllegalStateException("Image was not detached from last owner, or image  is not detachable");
        }
    }

    private static void postEventFromNative(Object selfRef) {
        ImageWriter iw = (ImageWriter) ((WeakReference) selfRef).get();
        if (iw != null) {
            Handler handler;
            synchronized (iw.mListenerLock) {
                handler = iw.mListenerHandler;
            }
            if (handler != null) {
                handler.sendEmptyMessage(0);
            }
        }
    }

    private void abortImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("image shouldn't be null");
        } else if (this.mDequeuedImages.contains(image)) {
            WriterSurfaceImage wi = (WriterSurfaceImage) image;
            if (wi.mIsImageValid) {
                cancelImage(this.mNativeContext, image);
                this.mDequeuedImages.remove(image);
                wi.clearSurfacePlanes();
                wi.mIsImageValid = false;
            }
        } else {
            throw new IllegalStateException("It is illegal to abort some image that is not dequeued yet");
        }
    }

    private boolean isImageOwnedByMe(Image image) {
        if ((image instanceof WriterSurfaceImage) && ((WriterSurfaceImage) image).getOwner() == this) {
            return true;
        }
        return false;
    }
}
