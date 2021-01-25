package ohos.media.camera.mode.utils;

import java.lang.Thread;
import java.util.concurrent.CopyOnWriteArrayList;
import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.mode.utils.ImageReaderProxy;
import ohos.media.image.Image;
import ohos.media.image.ImageReceiver;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.ConditionHelper;

public class ImageReaderProxy {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ImageReaderProxy.class);
    private final CopyOnWriteArrayList<Image> activeImageList = new CopyOnWriteArrayList<>();
    private final ConditionHelper closeLock = new ConditionHelper();
    private final Thread deferredCloseThread = new DeferredCloseThread(new Runnable() {
        /* class ohos.media.camera.mode.utils.ImageReaderProxy.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (ImageReaderProxy.this.lock) {
                if (ImageReaderProxy.this.activeImageList.isEmpty()) {
                    ImageReaderProxy.this.printLog("in thread close");
                    ImageReaderProxy.this.imageReader.release();
                    ImageReaderProxy.this.imageReader = null;
                    return;
                }
                ImageReaderProxy.this.closeLock.resetCondition();
                ImageReaderProxy.this.closeLock.await();
                synchronized (ImageReaderProxy.this.lock) {
                    ImageReaderProxy.this.printLog("unlock and close");
                    ImageReaderProxy.this.imageReader.release();
                    ImageReaderProxy.this.imageReader = null;
                }
            }
        }
    }, "deferredClose");
    private volatile ImageReceiver imageReader;
    private volatile boolean isValid = true;
    private final Object lock = new Object();

    public interface OnImageAvailableListener {
        void onImageAvailable(ImageReaderProxy imageReaderProxy);
    }

    public ImageReaderProxy(int i, int i2, int i3, int i4) {
        this.imageReader = ImageReceiver.create(i, i2, i3, i4);
    }

    public synchronized int getHeight() {
        if (!isAvailable()) {
            return 0;
        }
        return this.imageReader.getImageSize().height;
    }

    public synchronized int getWidth() {
        if (!isAvailable()) {
            return 0;
        }
        return this.imageReader.getImageSize().width;
    }

    public synchronized int getImageFormat() {
        if (!isAvailable()) {
            return 0;
        }
        return this.imageReader.getImageFormat();
    }

    public synchronized int getMaxImages() {
        if (!isAvailable()) {
            return 0;
        }
        return this.imageReader.getCapacity();
    }

    public synchronized Surface getSurface() {
        if (!isAvailable()) {
            return null;
        }
        return this.imageReader.getRecevingSurface();
    }

    public synchronized Image readLatestImage() {
        printLog("acquireLatestImage");
        if (!isAvailable()) {
            clearImageReceiver();
            return null;
        }
        Image readLatestImage = this.imageReader.readLatestImage();
        if (readLatestImage != null) {
            onImageActive(readLatestImage);
        }
        return readLatestImage;
    }

    public synchronized Image readNextImage() {
        printLog("acquireNextImage");
        if (!isAvailable()) {
            clearImageReceiver();
            return null;
        }
        Image readNextImage = this.imageReader.readNextImage();
        if (readNextImage != null) {
            onImageActive(readNextImage);
        }
        return readNextImage;
    }

    public synchronized void setOnImageAvailableListener(OnImageAvailableListener onImageAvailableListener, EventHandler eventHandler) {
        if (!isAvailable()) {
            LOGGER.warn("setOnImageAvailableListener is not available", new Object[0]);
            return;
        }
        this.imageReader.setImageArrivalListener(new ImageReceiver.IImageArrivalListener(onImageAvailableListener) {
            /* class ohos.media.camera.mode.utils.$$Lambda$ImageReaderProxy$yAFRDZv2xfYVRMl8U94ZrbWttY */
            private final /* synthetic */ ImageReaderProxy.OnImageAvailableListener f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.media.image.ImageReceiver.IImageArrivalListener
            public final void onImageArrival(ImageReceiver imageReceiver) {
                ImageReaderProxy.this.lambda$setOnImageAvailableListener$0$ImageReaderProxy(this.f$1, imageReceiver);
            }
        });
    }

    public /* synthetic */ void lambda$setOnImageAvailableListener$0$ImageReaderProxy(OnImageAvailableListener onImageAvailableListener, ImageReceiver imageReceiver) {
        printLog("onImageAvailable");
        onImageAvailableListener.onImageAvailable(this);
    }

    public synchronized void close() {
        printLog("close");
        if (this.isValid) {
            this.isValid = false;
            if (this.activeImageList.isEmpty()) {
                printLog("immediately close");
                this.imageReader.release();
                this.imageReader = null;
            } else {
                this.deferredCloseThread.setUncaughtExceptionHandler(new UncaughtCloseExceptionHandler());
                this.deferredCloseThread.start();
            }
        }
    }

    public synchronized void onImageClosed(Image image) {
        if (image == null) {
            printLog("onImageClosed: image is null.");
            return;
        }
        printLog("onImageClosed:" + image);
        if (this.activeImageList.contains(image)) {
            this.activeImageList.remove(image);
            if (this.activeImageList.isEmpty()) {
                this.closeLock.wakeup();
            }
        }
    }

    private synchronized boolean isAvailable() {
        return this.isValid && this.imageReader != null;
    }

    private synchronized void onImageActive(Image image) {
        if (image != null) {
            if (!this.activeImageList.contains(image)) {
                printLog("onImageActive:" + image);
                this.activeImageList.add(image);
            }
        }
    }

    private synchronized void clearImageReceiver() {
        if (this.imageReader != null) {
            try {
                Image readLatestImage = this.imageReader.readLatestImage();
                if (readLatestImage != null) {
                    printLog("clearImageReceiver:" + readLatestImage);
                    readLatestImage.release();
                }
            } catch (AssertionError | IllegalStateException unused) {
                LOGGER.warn("ImageReceiver is already null", new Object[0]);
            }
        }
        return;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void printLog(String str) {
        LOGGER.debug(":%{public}s isValid: %{public}b AIL: %{public}d | %{public}s", this, Boolean.valueOf(this.isValid), Integer.valueOf(this.activeImageList.size()), str);
    }

    private static class DeferredCloseThread extends Thread {
        DeferredCloseThread(Runnable runnable, String str) {
            super(runnable, str);
        }
    }

    private static class UncaughtCloseExceptionHandler implements Thread.UncaughtExceptionHandler {
        private UncaughtCloseExceptionHandler() {
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable th) {
            ImageReaderProxy.LOGGER.error("Unknown exception when close image reader.", new Object[0]);
        }
    }
}
