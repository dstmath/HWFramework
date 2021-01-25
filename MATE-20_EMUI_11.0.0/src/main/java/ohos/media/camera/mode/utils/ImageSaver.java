package ohos.media.camera.mode.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import ohos.media.camera.mode.impl.ActionStateCallbackImpl;
import ohos.media.image.Image;
import ohos.media.image.common.ImageFormat;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ImageSaver {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ImageSaver.class);

    public static class ImageSaverImage implements Runnable {
        private final ActionStateCallbackImpl actionStateCallback;
        private int actionType;
        private final File file;
        private final Image image;

        public ImageSaverImage(Image image2, File file2, ActionStateCallbackImpl actionStateCallbackImpl) {
            this.image = image2;
            this.file = file2;
            this.actionStateCallback = actionStateCallbackImpl;
        }

        public void setActionType(int i) {
            this.actionType = i;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x005a, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x005f, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0060, code lost:
            r4.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0063, code lost:
            throw r5;
         */
        @Override // java.lang.Runnable
        public void run() {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(this.file);
                Image.Component component = this.image.getComponent(ImageFormat.ComponentType.JPEG);
                if (component == null) {
                    ImageSaver.LOGGER.error("Image getComponent returns null", new Object[0]);
                } else {
                    byte[] bArr = new byte[component.remaining()];
                    int read = component.read(bArr);
                    if (read != 0) {
                        ImageSaver.LOGGER.error("Image read error, code: %{public}d", Integer.valueOf(read));
                    } else {
                        fileOutputStream.write(bArr);
                        if (this.actionType == 2) {
                            this.actionStateCallback.onTakePicture(6, null);
                        }
                        fileOutputStream.close();
                        this.image.release();
                        return;
                    }
                }
                fileOutputStream.close();
                this.image.release();
            } catch (IOException unused) {
                ImageSaver.LOGGER.error("IOException when write in run", new Object[0]);
                if (this.actionType == 2) {
                    this.actionStateCallback.onTakePicture(-2, null);
                }
                if (this.actionType == 3) {
                    this.actionStateCallback.onBurst(-2, null);
                }
            } catch (Throwable th) {
                this.image.release();
                throw th;
            }
        }
    }
}
