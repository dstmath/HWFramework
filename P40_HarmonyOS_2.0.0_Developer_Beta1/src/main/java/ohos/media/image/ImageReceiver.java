package ohos.media.image;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ImageReceiver {
    private static final String HANDLER_NAME = "nativeEventHandler";
    private static final Logger LOGGER = LoggerFactory.getImageLogger(ImageReceiver.class);
    private static final int MIN_POSITIVE = 1;
    private static final long USAGE_CPU_READ_OFTEN = 3;
    private volatile IImageArrivalListener arrivalListener;
    private final int capacity;
    private final int format;
    private volatile EventHandler handler;
    private final int height;
    private final Set<Image> images = new HashSet();
    private volatile boolean isReceiverValid = false;
    private long nativeContext;
    private final Object receiverLock = new Object();
    private final Surface surface;
    private final int width;

    public interface IImageArrivalListener {
        void onImageArrival(ImageReceiver imageReceiver);
    }

    private static native void nativeCacheClass();

    private native synchronized Surface nativeGetReceiverSurface();

    private native synchronized void nativeInit(Object obj, int i, int i2, int i3, int i4, long j);

    private native synchronized boolean nativeReadNext(Image image);

    private native synchronized void nativeRelease();

    private native synchronized void nativeReleaseFreeBuffers();

    static {
        LOGGER.debug("Begin loading imagereceiver_jni library.", new Object[0]);
        System.loadLibrary("imagereceiver_jni.z");
        nativeCacheClass();
    }

    public static ImageReceiver create(int i, int i2, int i3, int i4) {
        return new ImageReceiver(i, i2, i3, i4, 3);
    }

    private ImageReceiver(int i, int i2, int i3, int i4, long j) {
        if (i < 1 || i2 < 1) {
            LOGGER.error("The image size:[%{public}d, %{public}d] must be positive.", Integer.valueOf(i), Integer.valueOf(i2));
            throw new IllegalArgumentException("The image size must be positive.");
        } else if (i4 < 1) {
            LOGGER.error("The image capacity %{public}d must be larger than zero.", Integer.valueOf(i4));
            throw new IllegalArgumentException("The image capacity must be larger than zero.");
        } else if (i3 != 1) {
            this.width = i;
            this.height = i2;
            this.format = i3;
            this.capacity = i4;
            nativeInit(new WeakReference(this), i, i2, i3, i4, j);
            this.surface = nativeGetReceiverSurface();
            this.isReceiverValid = true;
        } else {
            LOGGER.error("NV21 format is not supported.", new Object[0]);
            throw new IllegalArgumentException("NV21 format is not supported.");
        }
    }

    public Surface getRecevingSurface() {
        return this.surface;
    }

    public Size getImageSize() {
        return new Size(this.width, this.height);
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getImageFormat() {
        return this.format;
    }

    public Image readLatestImage() {
        Image readNextImage = readNextImage();
        if (readNextImage == null) {
            return null;
        }
        while (true) {
            Image readNextImage2 = readNextImage();
            if (readNextImage2 == null) {
                return readNextImage;
            }
            readNextImage.release();
            synchronized (this.receiverLock) {
                this.images.remove(readNextImage);
            }
            readNextImage = readNextImage2;
        }
    }

    public Image readNextImage() {
        Image image;
        synchronized (this.receiverLock) {
            if (this.isReceiverValid) {
                image = new Image(this);
                if (!nativeReadNext(image)) {
                    LOGGER.error("get image from native response empty.", new Object[0]);
                    return null;
                }
                image.setImageStatus(true);
                this.images.add(image);
            } else {
                image = null;
            }
            return image;
        }
    }

    public void release() {
        setImageArrivalListener(null);
        synchronized (this.receiverLock) {
            this.isReceiverValid = false;
            for (Image image : this.images) {
                if (image.getImageStatus()) {
                    image.release();
                }
            }
            this.images.clear();
            nativeRelease();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }

    private static void onEventFromNative(Object obj) {
        if (obj instanceof WeakReference) {
            final ImageReceiver imageReceiver = (ImageReceiver) ((WeakReference) obj).get();
            if (imageReceiver == null) {
                LOGGER.error("native transmit image receiver is null.", new Object[0]);
                return;
            }
            EventHandler eventHandler = imageReceiver.handler;
            if (eventHandler == null) {
                LOGGER.error("event handler is null, set image arrival listener firstly.", new Object[0]);
                return;
            }
            final IImageArrivalListener iImageArrivalListener = imageReceiver.arrivalListener;
            final boolean z = imageReceiver.isReceiverValid;
            eventHandler.postTask(new Runnable() {
                /* class ohos.media.image.ImageReceiver.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    IImageArrivalListener iImageArrivalListener = IImageArrivalListener.this;
                    if (iImageArrivalListener != null && z) {
                        iImageArrivalListener.onImageArrival(imageReceiver);
                    }
                }
            });
            return;
        }
        LOGGER.error("can't cast object to weak reference.", new Object[0]);
    }

    public void setImageArrivalListener(IImageArrivalListener iImageArrivalListener) {
        if (iImageArrivalListener == null) {
            this.arrivalListener = null;
            if (this.handler != null) {
                this.handler.removeAllEvent();
                this.handler = null;
                return;
            }
            return;
        }
        this.arrivalListener = iImageArrivalListener;
        if (this.handler == null) {
            this.handler = new EventHandler(EventRunner.create(HANDLER_NAME));
        }
    }
}
