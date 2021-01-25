package ohos.media.image;

import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ohos.media.camera.params.Metadata;
import ohos.media.image.common.ImageFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class Image {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(Image.class);
    private Rect clipRect;
    private Component[] components;
    private ImageReceiver imageReceiver;
    private volatile boolean isImageValid = false;
    private long nativeBuffer;
    private long timestamp = 0;

    private native synchronized Component[] nativeGetComponents(int i, int i2);

    private native synchronized int nativeGetFormat(int i);

    private native synchronized Size nativeGetSize();

    private native synchronized void nativeReleaseImage(ImageReceiver imageReceiver2);

    Image(ImageReceiver imageReceiver2) {
        if (imageReceiver2 != null) {
            this.imageReceiver = imageReceiver2;
        } else {
            LOGGER.error("imageReceiver is null.", new Object[0]);
            throw new IllegalArgumentException("imageReceiver is null.");
        }
    }

    /* access modifiers changed from: package-private */
    public void setImageStatus(boolean z) {
        this.isImageValid = z;
    }

    /* access modifiers changed from: package-private */
    public boolean getImageStatus() {
        return this.isImageValid;
    }

    public int getFormat() {
        checkImageIsValid();
        return nativeGetFormat(this.imageReceiver.getImageFormat());
    }

    public Size getImageSize() {
        checkImageIsValid();
        if (getFormat() == 3) {
            return this.imageReceiver.getImageSize();
        }
        return nativeGetSize();
    }

    public long getTimestamp() {
        checkImageIsValid();
        return this.timestamp;
    }

    public Rect getClipRect() {
        checkImageIsValid();
        Rect rect = this.clipRect;
        if (rect != null) {
            return new Rect(rect);
        }
        Size imageSize = getImageSize();
        return new Rect(0, 0, imageSize.width, imageSize.height);
    }

    public void setClipRect(Rect rect) {
        checkImageIsValid();
        if (rect != null) {
            Rect rect2 = new Rect(rect);
            Size imageSize = getImageSize();
            if (!rect2.cropRect(0, 0, imageSize.width, imageSize.height)) {
                rect2.setEmpty();
            }
            this.clipRect = rect2;
            return;
        }
        this.clipRect = rect;
    }

    public void release() {
        if (this.isImageValid) {
            nativeReleaseImage(this.imageReceiver);
            this.isImageValid = false;
            Component[] componentArr = this.components;
            if (componentArr != null) {
                for (Component component : componentArr) {
                    component.release();
                }
            }
            this.components = null;
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

    public Component getComponent(ImageFormat.ComponentType componentType) {
        checkImageIsValid();
        if (componentType == null) {
            LOGGER.error("component type is null.", new Object[0]);
            return null;
        }
        if (this.components == null) {
            initComponents();
        }
        Component[] componentArr = this.components;
        if (componentArr != null) {
            for (Component component : componentArr) {
                if (componentType == component.componentType) {
                    return component;
                }
            }
        }
        LOGGER.error("component type %{public}s mismatch native acquire.", componentType);
        return null;
    }

    private void initComponents() {
        checkImageIsValid();
        int imageFormat = this.imageReceiver.getImageFormat();
        this.components = nativeGetComponents(ImageFormat.getComponentNumber(imageFormat), imageFormat);
    }

    private void checkImageIsValid() {
        if (!this.isImageValid) {
            throw new IllegalStateException("image state is invalid");
        }
    }

    public static class Component {
        private static final int UNSIGNED_BYTE_MASK = 255;
        private ByteBuffer byteBuffer;
        public final ImageFormat.ComponentType componentType;
        public final int pixelStride;
        public final int rowStride;

        public static final class OperationResult {
            public static final int FAILURE = -1;
            public static final int RELEASED = -2;
            public static final int SUCCESS = 0;

            private OperationResult() {
            }
        }

        private Component(int i, int i2, int i3, ByteBuffer byteBuffer2) {
            this.componentType = ImageFormat.ComponentType.valueOf(i);
            this.rowStride = i2;
            this.pixelStride = i3;
            this.byteBuffer = byteBuffer2;
            this.byteBuffer.order(ByteOrder.nativeOrder());
        }

        public int read() {
            try {
                checkParameterIsValid();
                return this.byteBuffer.get() & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
            } catch (IllegalStateException unused) {
                Image.LOGGER.error("component has been released.", new Object[0]);
                return -2;
            } catch (BufferUnderflowException unused2) {
                Image.LOGGER.error("read byte occurs exception.", new Object[0]);
                return -1;
            }
        }

        public int read(byte[] bArr) {
            try {
                checkParameterIsValid();
                this.byteBuffer.get(bArr);
                return 0;
            } catch (IllegalStateException unused) {
                Image.LOGGER.error("component has been released.", new Object[0]);
                return -2;
            } catch (BufferUnderflowException unused2) {
                Image.LOGGER.error("read bulk byte occurs exception.", new Object[0]);
                return -1;
            }
        }

        public int read(byte[] bArr, int i, int i2) {
            try {
                checkParameterIsValid();
                this.byteBuffer.get(bArr, i, i2);
                return 0;
            } catch (IllegalStateException unused) {
                Image.LOGGER.error("component has been released.", new Object[0]);
                return -2;
            } catch (IndexOutOfBoundsException | BufferUnderflowException unused2) {
                Image.LOGGER.error("read bulk byte occurs exception.", new Object[0]);
                return -1;
            }
        }

        public int tell() {
            try {
                checkParameterIsValid();
                return this.byteBuffer.position();
            } catch (IllegalStateException unused) {
                Image.LOGGER.error("tell position occurs exception.", new Object[0]);
                return -2;
            }
        }

        public int seek(int i) {
            try {
                checkParameterIsValid();
                this.byteBuffer.position(i);
                return 0;
            } catch (IllegalStateException unused) {
                Image.LOGGER.error("component has been released.", new Object[0]);
                return -2;
            } catch (IllegalArgumentException unused2) {
                Image.LOGGER.error("seek position occurs exception.", new Object[0]);
                return -1;
            }
        }

        public int remaining() {
            try {
                checkParameterIsValid();
                return this.byteBuffer.remaining();
            } catch (IllegalStateException unused) {
                Image.LOGGER.error("component has been released.", new Object[0]);
                return -2;
            }
        }

        public ByteBuffer getBuffer() {
            checkParameterIsValid();
            return this.byteBuffer;
        }

        public void release() {
            ByteBuffer byteBuffer2 = this.byteBuffer;
            if (byteBuffer2 != null) {
                if (byteBuffer2.isDirect()) {
                    reflectFreeDirectBuffer(this.byteBuffer);
                }
                this.byteBuffer = null;
            }
        }

        private void checkParameterIsValid() {
            if (this.byteBuffer == null) {
                throw new IllegalStateException("image or buffer is invalid");
            }
        }

        private void reflectFreeDirectBuffer(ByteBuffer byteBuffer2) {
            try {
                Class.forName("java.nio.NioUtils").getMethod("freeDirectBuffer", ByteBuffer.class).invoke(null, byteBuffer2);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                Image.LOGGER.error("reflect free direct buffer error %{public}s", e.getMessage());
            }
        }
    }
}
