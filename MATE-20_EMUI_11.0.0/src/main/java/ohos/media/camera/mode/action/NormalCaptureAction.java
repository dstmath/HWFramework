package ohos.media.camera.mode.action;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.ActionDataCallback;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.function.PreCapture;
import ohos.media.camera.mode.function.Promise;
import ohos.media.camera.mode.impl.ActionDataCallbackImpl;
import ohos.media.camera.mode.impl.ActionStateCallbackImpl;
import ohos.media.camera.mode.impl.ModeImpl;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.camera.mode.tags.CaptureParameters;
import ohos.media.camera.mode.utils.CollectionUtil;
import ohos.media.camera.mode.utils.ImageReaderProxy;
import ohos.media.camera.mode.utils.ImageSaver;
import ohos.media.camera.mode.utils.JpegFileNameUtil;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.image.Image;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class NormalCaptureAction implements CaptureAction {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(NormalCaptureAction.class);
    private static final int MAX_BURST_COUNT = 100;
    private static final int MAX_SURFACE_NUMBER = 2;
    private CameraAbilityImpl cameraAbility;
    private final CaptureAccomplishChecker captureAccomplishChecker;
    private EventHandler captureCallBackHandler;
    private volatile Surface captureSurface;
    private int captureTemplateType = 2;
    private final CameraController controller;
    private final EventHandler dataCallbackHandler;
    private FrameConfig.Builder frameConfigBuilder;
    private ImageReaderProxy imageReader;
    private boolean isBursting;
    private volatile boolean isReadyToCapture;
    private final Object lock = new Object();
    private final BaseModeTags modeTags;
    private final PreCapture preCapture;
    private final List<Surface> surfaces = new ArrayList(2);

    @Retention(RetentionPolicy.SOURCE)
    public @interface HandlerIndex {
        public static final int CAPTURE_CALLBACK = 0;
        public static final int DATA_CALLBACK = 1;
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public Surface getRawSurface() {
        return null;
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void releaseResources() {
    }

    public NormalCaptureAction(CameraController cameraController, BaseModeTags baseModeTags, CameraAbilityImpl cameraAbilityImpl, EventHandler[] eventHandlerArr, PreCapture preCapture2) {
        LOGGER.debug("NormalCaptureAction: ", new Object[0]);
        if (eventHandlerArr.length > 1) {
            this.controller = cameraController;
            this.captureCallBackHandler = eventHandlerArr[0];
            this.dataCallbackHandler = eventHandlerArr[1];
            this.modeTags = baseModeTags;
            this.preCapture = preCapture2;
            this.cameraAbility = cameraAbilityImpl;
            this.isReadyToCapture = true;
            this.captureAccomplishChecker = new CaptureAccomplishChecker();
            return;
        }
        throw new IllegalArgumentException("Handlers are not enough!");
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void capture(final File file, final ActionDataCallbackImpl actionDataCallbackImpl, final ActionStateCallbackImpl actionStateCallbackImpl, final int i, final List<Surface> list) {
        if (actionDataCallbackImpl == null || actionStateCallbackImpl == null) {
            LOGGER.warn("capture: null actionDataCallback or null actionStateCallback!", new Object[0]);
            return;
        }
        LOGGER.begin("do capture file");
        if (!this.isReadyToCapture) {
            actionStateCallbackImpl.onTakePicture(-3, null);
            return;
        }
        this.isReadyToCapture = false;
        synchronized (this.lock) {
            if (this.imageReader == null || file == null) {
                LOGGER.warn("capture: null imageReader or null file!", new Object[0]);
                throw new NullPointerException("file and imageReader should not be null!");
            }
        }
        PreCapture preCapture2 = this.preCapture;
        if (preCapture2 == null) {
            LOGGER.warn("preCapture is null", new Object[0]);
            this.isReadyToCapture = true;
            return;
        }
        preCapture2.capture(new Promise() {
            /* class ohos.media.camera.mode.action.NormalCaptureAction.AnonymousClass1 */

            @Override // ohos.media.camera.mode.function.Promise
            public void done() {
                NormalCaptureAction.LOGGER.debug("promise done", new Object[0]);
                if (NormalCaptureAction.this.buildCapture(i, list)) {
                    NormalCaptureAction.this.setOutputImageReceiver(file, actionDataCallbackImpl, actionStateCallbackImpl);
                    if (NormalCaptureAction.this.controller.capture(NormalCaptureAction.this.frameConfigBuilder.build(), NormalCaptureAction.this.getCaptureCallback(actionStateCallbackImpl), null) == -1) {
                        actionStateCallbackImpl.onTakePicture(-3, null);
                        return;
                    }
                    return;
                }
                NormalCaptureAction.this.isReadyToCapture = true;
            }

            @Override // ohos.media.camera.mode.function.Promise
            public void cancel() {
                NormalCaptureAction.this.isReadyToCapture = true;
            }
        });
        LOGGER.end("do capture file");
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void capture(final ActionDataCallbackImpl actionDataCallbackImpl, final ActionStateCallbackImpl actionStateCallbackImpl, final int i, final List<Surface> list) {
        LOGGER.begin("do capture image");
        if (actionStateCallbackImpl == null) {
            LOGGER.warn("the input value is null", new Object[0]);
        } else if (!this.isReadyToCapture) {
            actionStateCallbackImpl.onTakePicture(-3, null);
        } else {
            this.isReadyToCapture = false;
            synchronized (this.lock) {
                if (this.imageReader == null || actionDataCallbackImpl == null) {
                    LOGGER.warn("capture: null imageReader or null imageCallback!", new Object[0]);
                    throw new NullPointerException("actionStateCallback and imageReader should not be null!");
                }
            }
            PreCapture preCapture2 = this.preCapture;
            if (preCapture2 == null) {
                LOGGER.warn("preCapture is null", new Object[0]);
                this.isReadyToCapture = true;
                return;
            }
            preCapture2.capture(new Promise() {
                /* class ohos.media.camera.mode.action.NormalCaptureAction.AnonymousClass2 */

                @Override // ohos.media.camera.mode.function.Promise
                public void done() {
                    NormalCaptureAction.LOGGER.debug("promise done", new Object[0]);
                    if (NormalCaptureAction.this.buildCapture(i, list)) {
                        NormalCaptureAction.this.setOutputImageReceiver(actionDataCallbackImpl, actionStateCallbackImpl);
                        if (NormalCaptureAction.this.controller.capture(NormalCaptureAction.this.frameConfigBuilder.build(), NormalCaptureAction.this.getCaptureCallback(actionStateCallbackImpl), null) == -1) {
                            actionStateCallbackImpl.onTakePicture(-3, null);
                            return;
                        }
                        return;
                    }
                    NormalCaptureAction.this.isReadyToCapture = true;
                }

                @Override // ohos.media.camera.mode.function.Promise
                public void cancel() {
                    NormalCaptureAction.this.isReadyToCapture = true;
                }
            });
            LOGGER.end("do capture image");
        }
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void captureBurst(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, int i, List<Surface> list) {
        LOGGER.begin("do capture Burst image");
        if (!this.isReadyToCapture) {
            actionStateCallbackImpl.onBurst(-3, null);
            return;
        }
        synchronized (this.lock) {
            if (this.imageReader == null || actionStateCallbackImpl == null) {
                LOGGER.warn("captureBurst: null imageReader or null imageCallback!", new Object[0]);
                throw new NullPointerException("actionStateCallback and imageReader should not be null!");
            }
        }
        if (buildCapture(i, list)) {
            this.isReadyToCapture = false;
            if (!this.surfaces.isEmpty()) {
                for (Surface surface : this.surfaces) {
                    this.frameConfigBuilder.addSurface(surface);
                }
            }
            setOutputImageReceiverBurst(actionDataCallbackImpl, actionStateCallbackImpl);
            this.controller.setRepeatingRequest(this.frameConfigBuilder, getBurstCaptureCallback(actionStateCallbackImpl), null);
            this.isBursting = true;
        }
        LOGGER.end("do captureBurst image");
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void captureBurst(File file, ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, int i, List<Surface> list) {
        if (actionDataCallbackImpl == null) {
            LOGGER.warn("captureBurst:actionDataCallback is null", new Object[0]);
        } else if (actionStateCallbackImpl == null) {
            LOGGER.warn("captureBurst:actionStateCallback is null", new Object[0]);
        } else {
            LOGGER.begin("do capture Burst file");
            if (!this.isReadyToCapture) {
                actionStateCallbackImpl.onBurst(-3, null);
                return;
            }
            synchronized (this.lock) {
                if (this.imageReader == null || file == null) {
                    LOGGER.warn("captureBurst: null imageReader or null file!", new Object[0]);
                    throw new NullPointerException("file and imageReader should not be null!");
                }
            }
            if (buildCapture(i, list)) {
                this.isReadyToCapture = false;
                if (!this.surfaces.isEmpty()) {
                    for (Surface surface : this.surfaces) {
                        this.frameConfigBuilder.addSurface(surface);
                    }
                }
                setOutputImageReceiverBurst(file, actionDataCallbackImpl, actionStateCallbackImpl);
                this.controller.setRepeatingRequest(this.frameConfigBuilder, getBurstCaptureCallback(actionStateCallbackImpl), null);
                this.isBursting = true;
            }
            LOGGER.end("do captureBurst file");
        }
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void setCaptureTemplateType(int i) {
        this.captureTemplateType = i;
    }

    /* access modifiers changed from: protected */
    public boolean buildCapture(int i, List<Surface> list) {
        this.frameConfigBuilder = this.controller.createCaptureRequest(this.captureTemplateType);
        FrameConfig.Builder builder = this.frameConfigBuilder;
        if (builder == null) {
            LOGGER.warn("createCaptureRequest failed", new Object[0]);
            return false;
        }
        builder.addSurface(this.captureSurface);
        if (!CollectionUtil.isEmptyCollection(list)) {
            for (Surface surface : list) {
                this.frameConfigBuilder.addSurface(surface);
            }
        }
        this.frameConfigBuilder.setImageRotation(i);
        for (CaptureParameters captureParameters : this.modeTags.enableCapture()) {
            captureParameters.applyToBuilder(this.frameConfigBuilder);
        }
        return true;
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void stop() {
        CameraController cameraController = this.controller;
        if (cameraController != null && this.isBursting) {
            cameraController.stopRepeating();
        }
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void createSurface(Size size, int i, int i2) {
        synchronized (this.lock) {
            if (this.imageReader != null) {
                this.imageReader.close();
            }
            if (size == null) {
                this.imageReader = null;
                this.captureSurface = null;
            } else {
                this.imageReader = new ImageReaderProxy(size.width, size.height, i, i2);
                this.captureSurface = this.imageReader.getSurface();
            }
        }
    }

    @Override // ohos.media.camera.mode.action.CaptureAction
    public void destroySurface() {
        synchronized (this.lock) {
            if (this.captureSurface != null) {
                this.captureSurface.release();
            }
            if (this.imageReader != null) {
                this.imageReader.close();
            }
            this.captureSurface = null;
            this.imageReader = null;
        }
    }

    @Override // ohos.media.camera.mode.action.Action
    public List<Surface> getSurfaces() {
        return this.surfaces;
    }

    @Override // ohos.media.camera.mode.action.Action
    public Surface getSurface() {
        return this.captureSurface;
    }

    @Override // ohos.media.camera.mode.action.Action
    public void setSurface(Surface surface) {
        this.captureSurface = surface;
    }

    private ImageReaderProxy.OnImageAvailableListener getImageAvailableListener(File file, ActionStateCallbackImpl actionStateCallbackImpl) {
        return new ImageReaderProxy.OnImageAvailableListener(file, actionStateCallbackImpl) {
            /* class ohos.media.camera.mode.action.$$Lambda$NormalCaptureAction$xxTohQfEZqfgOIrOcJH8z46pdNA */
            private final /* synthetic */ File f$1;
            private final /* synthetic */ ActionStateCallbackImpl f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // ohos.media.camera.mode.utils.ImageReaderProxy.OnImageAvailableListener
            public final void onImageAvailable(ImageReaderProxy imageReaderProxy) {
                NormalCaptureAction.this.lambda$getImageAvailableListener$0$NormalCaptureAction(this.f$1, this.f$2, imageReaderProxy);
            }
        };
    }

    public /* synthetic */ void lambda$getImageAvailableListener$0$NormalCaptureAction(File file, ActionStateCallbackImpl actionStateCallbackImpl, ImageReaderProxy imageReaderProxy) {
        if (imageReaderProxy == null) {
            LOGGER.warn("onImageAvailable: ImageReaderProxy is null", new Object[0]);
            return;
        }
        LOGGER.debug("file onImageAvailable!", new Object[0]);
        LOGGER.debug("onImageAvailable: PreProcess save img. %{public}s", file.toString());
        Image image = null;
        synchronized (this.lock) {
            try {
                image = imageReaderProxy.readNextImage();
            } catch (IllegalStateException unused) {
                LOGGER.error("imageReader IllegalStateException", new Object[0]);
            }
        }
        if (image != null) {
            ImageSaver.ImageSaverImage imageSaverImage = new ImageSaver.ImageSaverImage(image, file, actionStateCallbackImpl);
            imageSaverImage.setActionType(2);
            imageSaverImage.run();
            image.release();
            imageReaderProxy.onImageClosed(image);
        }
        this.captureAccomplishChecker.onImageAvailableCalled(actionStateCallbackImpl);
    }

    /* access modifiers changed from: protected */
    public ImageReaderProxy.OnImageAvailableListener getImageAvailableListener(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl) {
        if (actionDataCallbackImpl instanceof ModeImpl.InnerActionDataCallbackImpl) {
            return new ImageReaderProxy.OnImageAvailableListener(actionDataCallbackImpl, actionStateCallbackImpl) {
                /* class ohos.media.camera.mode.action.$$Lambda$NormalCaptureAction$wLRZo9Ul_MNJ0mAucKwNjCN7EKU */
                private final /* synthetic */ ActionDataCallbackImpl f$1;
                private final /* synthetic */ ActionStateCallbackImpl f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // ohos.media.camera.mode.utils.ImageReaderProxy.OnImageAvailableListener
                public final void onImageAvailable(ImageReaderProxy imageReaderProxy) {
                    NormalCaptureAction.this.lambda$getImageAvailableListener$1$NormalCaptureAction(this.f$1, this.f$2, imageReaderProxy);
                }
            };
        }
        LOGGER.warn("imageCallback is not instance of ModeImpl.InnerActionDataCallbackImpl", new Object[0]);
        throw new IllegalArgumentException("imageCallback instance error");
    }

    public /* synthetic */ void lambda$getImageAvailableListener$1$NormalCaptureAction(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, ImageReaderProxy imageReaderProxy) {
        if (imageReaderProxy == null) {
            LOGGER.warn("getImageAvailableListener: null reader!", new Object[0]);
            return;
        }
        LOGGER.debug("image callback onImageAvailable!", new Object[0]);
        ((ModeImpl.InnerActionDataCallbackImpl) actionDataCallbackImpl).setImageReaderProxy(imageReaderProxy);
        actionDataCallbackImpl.onImageAvailable(1, imageReaderProxy.readNextImage());
        this.captureAccomplishChecker.onImageAvailableCalled(actionStateCallbackImpl);
    }

    private ImageReaderProxy.OnImageAvailableListener getImageAvailableListenerBurst(final ActionDataCallbackImpl actionDataCallbackImpl, final ActionStateCallbackImpl actionStateCallbackImpl) {
        if (actionDataCallbackImpl instanceof ModeImpl.InnerActionDataCallbackImpl) {
            return new ImageReaderProxy.OnImageAvailableListener() {
                /* class ohos.media.camera.mode.action.NormalCaptureAction.AnonymousClass3 */
                private int burstImageSavedCount = 1;

                @Override // ohos.media.camera.mode.utils.ImageReaderProxy.OnImageAvailableListener
                public void onImageAvailable(ImageReaderProxy imageReaderProxy) {
                    if (imageReaderProxy == null) {
                        NormalCaptureAction.LOGGER.warn("getImageAvailableListenerBurst: null reader!", new Object[0]);
                        return;
                    }
                    NormalCaptureAction.LOGGER.debug(" onImageAvailable! burstCount: %{public}d", Integer.valueOf(this.burstImageSavedCount));
                    Image image = null;
                    try {
                        image = imageReaderProxy.readNextImage();
                        if (this.burstImageSavedCount <= 100) {
                            ((ModeImpl.InnerActionDataCallbackImpl) actionDataCallbackImpl).setImageReaderProxy(imageReaderProxy);
                            actionDataCallbackImpl.onImageAvailable(2, image);
                        }
                        this.burstImageSavedCount++;
                        NormalCaptureAction.this.captureAccomplishChecker.onImageAvailableCalled(actionStateCallbackImpl);
                        if (this.burstImageSavedCount <= 101) {
                            return;
                        }
                    } catch (IllegalStateException e) {
                        NormalCaptureAction.LOGGER.debug("onImageAvailable %{public}s", e.getMessage());
                        NormalCaptureAction.this.captureAccomplishChecker.onImageUnAvailableCalled(actionStateCallbackImpl);
                        if (this.burstImageSavedCount <= 101) {
                            return;
                        }
                    } catch (Throwable th) {
                        if (this.burstImageSavedCount > 101) {
                            NormalCaptureAction.this.imageClose(null);
                            imageReaderProxy.onImageClosed(null);
                        }
                        throw th;
                    }
                    NormalCaptureAction.this.imageClose(image);
                    imageReaderProxy.onImageClosed(image);
                }
            };
        }
        LOGGER.warn("imageCallback is not instance of ModeImpl.InnerActionDataCallbackImpl", new Object[0]);
        throw new IllegalArgumentException("imageCallback instance error");
    }

    private ImageReaderProxy.OnImageAvailableListener getImageAvailableListenerBurst(File file, ActionStateCallbackImpl actionStateCallbackImpl) {
        return new BrustImageAvailableListener(file, actionStateCallbackImpl);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void imageClose(Image image) {
        if (image != null) {
            try {
                image.release();
            } catch (IllegalStateException e) {
                LOGGER.error("image close error %{public}s", e.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public FrameStateCallback getCaptureCallback(ActionStateCallbackImpl actionStateCallbackImpl) {
        return new CaptureFrameStateCallback(actionStateCallbackImpl);
    }

    private FrameStateCallback getBurstCaptureCallback(ActionStateCallbackImpl actionStateCallbackImpl) {
        return new BurstFrameStateCallback(actionStateCallbackImpl);
    }

    /* access modifiers changed from: protected */
    public void setOutputImageReceiver(File file, ActionDataCallback actionDataCallback, ActionStateCallbackImpl actionStateCallbackImpl) {
        if (this.modeTags.isGetImageFromPostProc()) {
            setPostProcReceiver(file);
            return;
        }
        ImageReaderProxy.OnImageAvailableListener imageAvailableListener = getImageAvailableListener(file, actionStateCallbackImpl);
        synchronized (this.lock) {
            if (this.imageReader != null) {
                this.imageReader.setOnImageAvailableListener(imageAvailableListener, this.dataCallbackHandler);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setOutputImageReceiver(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl) {
        ImageReaderProxy.OnImageAvailableListener imageAvailableListener = getImageAvailableListener(actionDataCallbackImpl, actionStateCallbackImpl);
        synchronized (this.lock) {
            if (this.imageReader != null) {
                this.imageReader.setOnImageAvailableListener(imageAvailableListener, this.dataCallbackHandler);
            }
        }
    }

    private void setOutputImageReceiverBurst(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl) {
        ImageReaderProxy.OnImageAvailableListener imageAvailableListenerBurst = getImageAvailableListenerBurst(actionDataCallbackImpl, actionStateCallbackImpl);
        synchronized (this.lock) {
            if (this.imageReader != null) {
                this.imageReader.setOnImageAvailableListener(imageAvailableListenerBurst, this.dataCallbackHandler);
            }
        }
    }

    private void setOutputImageReceiverBurst(File file, ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl) {
        if (this.modeTags.isGetImageFromPostProc()) {
            setPostProcReceiver(file);
            return;
        }
        ImageReaderProxy.OnImageAvailableListener imageAvailableListenerBurst = getImageAvailableListenerBurst(file, actionStateCallbackImpl);
        synchronized (this.lock) {
            if (this.imageReader != null) {
                this.imageReader.setOnImageAvailableListener(imageAvailableListenerBurst, this.dataCallbackHandler);
            }
        }
    }

    private void setPostProcReceiver(File file) {
        String name = file.getName();
        JpegFileNameUtil.addJpegFileName(name);
        this.frameConfigBuilder.setParameter(InnerParameterKey.JPEG_FILE_NAME, name.getBytes(Charset.forName(ConstantValue.JPEG_FILE_NAME_ENCODE_CHARSET)));
    }

    /* access modifiers changed from: protected */
    public void processComplete(ActionStateCallbackImpl actionStateCallbackImpl, int i, int i2) {
        if (actionStateCallbackImpl == null) {
            LOGGER.warn("processComplete:callback is null!", new Object[0]);
            return;
        }
        actionStateCallbackImpl.onTakePicture(5, null);
        LOGGER.debug("Just do callback in NormalCaptureAction.", new Object[0]);
    }

    /* access modifiers changed from: private */
    public class BrustImageAvailableListener implements ImageReaderProxy.OnImageAvailableListener {
        private ActionStateCallbackImpl actionStateCallback;
        private int burstImageSavedCount = 1;
        private File file;

        BrustImageAvailableListener(File file2, ActionStateCallbackImpl actionStateCallbackImpl) {
            this.file = file2;
            this.actionStateCallback = actionStateCallbackImpl;
        }

        @Override // ohos.media.camera.mode.utils.ImageReaderProxy.OnImageAvailableListener
        public void onImageAvailable(ImageReaderProxy imageReaderProxy) {
            if (imageReaderProxy != null) {
                NormalCaptureAction.LOGGER.debug("burst onImageAvailable! isReadyToCapture: %{public}b", Boolean.valueOf(NormalCaptureAction.this.isReadyToCapture));
                if (!NormalCaptureAction.this.isReadyToCapture) {
                    Image image = null;
                    try {
                        image = imageReaderProxy.readNextImage();
                        if (image != null && this.burstImageSavedCount <= 100) {
                            saveBurstImage(image, getBurstNewFile());
                        }
                        this.burstImageSavedCount++;
                        NormalCaptureAction.this.captureAccomplishChecker.onImageAvailableCalled(this.actionStateCallback);
                    } catch (IllegalStateException e) {
                        NormalCaptureAction.LOGGER.debug("onImageAvailable %{public}s", e.getMessage());
                        NormalCaptureAction.this.captureAccomplishChecker.onImageUnAvailableCalled(this.actionStateCallback);
                    } catch (Throwable th) {
                        NormalCaptureAction.this.imageClose(null);
                        imageReaderProxy.onImageClosed(null);
                        throw th;
                    }
                    NormalCaptureAction.this.imageClose(image);
                    imageReaderProxy.onImageClosed(image);
                }
            }
        }

        private File getBurstNewFile() {
            String str;
            String name = this.file.getName();
            int lastIndexOf = name.lastIndexOf(".");
            if (lastIndexOf >= name.length() || lastIndexOf < 0) {
                str = String.format(Locale.ENGLISH, name + "_BURST%03d.jpg", Integer.valueOf(this.burstImageSavedCount));
            } else {
                str = name.substring(0, lastIndexOf) + String.format(Locale.ENGLISH, "_BURST%03d", Integer.valueOf(this.burstImageSavedCount)) + name.substring(lastIndexOf);
            }
            File file2 = new File(this.file.getParent(), str);
            NormalCaptureAction.LOGGER.debug("burst file renamed to %{public}s", file2.getName());
            return file2;
        }

        private void saveBurstImage(Image image, File file2) {
            ImageSaver.ImageSaverImage imageSaverImage = new ImageSaver.ImageSaverImage(image, file2, this.actionStateCallback);
            imageSaverImage.setActionType(3);
            imageSaverImage.run();
        }
    }

    /* access modifiers changed from: protected */
    public class CaptureFrameStateCallback extends FrameStateCallback {
        private final ActionStateCallbackImpl actionStateCallback;

        CaptureFrameStateCallback(ActionStateCallbackImpl actionStateCallbackImpl) {
            this.actionStateCallback = actionStateCallbackImpl;
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onCaptureTriggerStarted(Camera camera, int i, long j) {
            super.onCaptureTriggerStarted(camera, i, j);
            this.actionStateCallback.onTakePicture(1, null);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onCaptureTriggerFinished(Camera camera, int i, long j) {
            super.onCaptureTriggerFinished(camera, i, j);
            NormalCaptureAction.LOGGER.debug("onCaptureCompleted called", new Object[0]);
            this.actionStateCallback.onTakePicture(5, null);
            NormalCaptureAction.this.isReadyToCapture = true;
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onCaptureTriggerInterrupted(Camera camera, int i) {
            super.onCaptureTriggerInterrupted(camera, i);
            this.actionStateCallback.onTakePicture(-1, null);
            NormalCaptureAction.this.isReadyToCapture = true;
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameStarted(Camera camera, FrameConfig frameConfig, long j, long j2) {
            super.onFrameStarted(camera, frameConfig, j, j2);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameProgressed(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameProgressed(camera, frameConfig, frameResult);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameFinished(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameFinished(camera, frameConfig, frameResult);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameError(Camera camera, FrameConfig frameConfig, int i, FrameResult frameResult) {
            super.onFrameError(camera, frameConfig, i, frameResult);
        }
    }

    /* access modifiers changed from: protected */
    public class BurstFrameStateCallback extends FrameStateCallback {
        private ActionStateCallbackImpl actionStateCallback;
        private int burstCount = 1;

        BurstFrameStateCallback(ActionStateCallbackImpl actionStateCallbackImpl) {
            this.actionStateCallback = actionStateCallbackImpl;
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameStarted(Camera camera, FrameConfig frameConfig, long j, long j2) {
            super.onFrameStarted(camera, frameConfig, j, j2);
            if (this.burstCount == 1) {
                this.actionStateCallback.onBurst(1, null);
            }
            this.burstCount++;
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameProgressed(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameProgressed(camera, frameConfig, frameResult);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameFinished(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameFinished(camera, frameConfig, frameResult);
            NormalCaptureAction.LOGGER.debug("burst onCaptureCompleted called", new Object[0]);
            if (NormalCaptureAction.this.modeTags.isGetImageFromPostProc()) {
                this.actionStateCallback.onBurst(2, null);
                NormalCaptureAction.this.isReadyToCapture = true;
                return;
            }
            NormalCaptureAction.this.captureAccomplishChecker.onCaptureCompletedCalled(this.actionStateCallback);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameError(Camera camera, FrameConfig frameConfig, int i, FrameResult frameResult) {
            super.onFrameError(camera, frameConfig, i, frameResult);
            this.actionStateCallback.onBurst(-1, null);
            NormalCaptureAction.this.isReadyToCapture = true;
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onCaptureTriggerFinished(Camera camera, int i, long j) {
            super.onCaptureTriggerFinished(camera, i, j);
            if (NormalCaptureAction.this.modeTags.isGetImageFromPostProc()) {
                this.actionStateCallback.onBurst(3, null);
                return;
            }
            if (this.burstCount == 1) {
                NormalCaptureAction.this.captureAccomplishChecker.setReadyToCloseBurst();
            }
            NormalCaptureAction.this.captureAccomplishChecker.onBurstSequenceCompleted(this.actionStateCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public class CaptureAccomplishChecker {
        private boolean isBurstCompletedCalled = false;
        private boolean isReadyToCloseBurst = false;
        private final Byte[] locks = new Byte[0];
        private int numBurstOnceCalled = 0;
        private int numCaptureCompletedCalled = 0;
        private int numImageAvailableCalled = 0;
        private int numImageUnAvailableCalled = 0;

        CaptureAccomplishChecker() {
        }

        /* access modifiers changed from: package-private */
        public void setReadyToCloseBurst() {
            this.isReadyToCloseBurst = true;
        }

        /* access modifiers changed from: package-private */
        public void onCaptureCompletedCalled(ActionStateCallbackImpl actionStateCallbackImpl) {
            synchronized (this.locks) {
                if (!NormalCaptureAction.this.isReadyToCapture) {
                    this.numCaptureCompletedCalled++;
                    checkCaptureAccomplishState(actionStateCallbackImpl);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onImageAvailableCalled(ActionStateCallbackImpl actionStateCallbackImpl) {
            synchronized (this.locks) {
                if (!NormalCaptureAction.this.isReadyToCapture) {
                    this.numImageAvailableCalled++;
                    checkCaptureAccomplishState(actionStateCallbackImpl);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onImageUnAvailableCalled(ActionStateCallbackImpl actionStateCallbackImpl) {
            synchronized (this.locks) {
                if (!NormalCaptureAction.this.isReadyToCapture) {
                    this.numImageUnAvailableCalled++;
                    checkCaptureAccomplishState(actionStateCallbackImpl);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onBurstSequenceCompleted(ActionStateCallbackImpl actionStateCallbackImpl) {
            synchronized (this.locks) {
                this.isBurstCompletedCalled = true;
                if (this.isReadyToCloseBurst) {
                    doBurstCallback(actionStateCallbackImpl);
                }
            }
        }

        private void checkCaptureAccomplishState(ActionStateCallbackImpl actionStateCallbackImpl) {
            NormalCaptureAction.LOGGER.debug("numImageAvailableCalled = %{public}d, numCaptureCompletedCalled = %{public}d, numImageUnAvailableCalled = %{public}d", Integer.valueOf(this.numImageAvailableCalled), Integer.valueOf(this.numCaptureCompletedCalled), Integer.valueOf(this.numImageUnAvailableCalled));
            int i = this.numCaptureCompletedCalled - this.numImageUnAvailableCalled;
            if (NormalCaptureAction.this.isBursting) {
                int i2 = this.numBurstOnceCalled;
                if (i > i2 && this.numImageAvailableCalled > i2 && i2 < 100) {
                    actionStateCallbackImpl.onBurst(2, null);
                    NormalCaptureAction.LOGGER.debug("BurstOnce: %{public}d", Integer.valueOf(this.numBurstOnceCalled));
                    this.numBurstOnceCalled++;
                }
                if (i == this.numImageAvailableCalled) {
                    NormalCaptureAction.LOGGER.debug("ready to capture isBurstComplete: %{public}b", Boolean.valueOf(this.isBurstCompletedCalled));
                    if (this.isBurstCompletedCalled) {
                        doBurstCallback(actionStateCallbackImpl);
                    } else {
                        this.isReadyToCloseBurst = true;
                    }
                } else {
                    this.isReadyToCloseBurst = false;
                }
            } else if (i == this.numImageAvailableCalled) {
                NormalCaptureAction.this.isReadyToCapture = true;
                NormalCaptureAction.this.processComplete(actionStateCallbackImpl, i, this.numImageAvailableCalled);
            }
        }

        private void doBurstCallback(ActionStateCallbackImpl actionStateCallbackImpl) {
            this.isBurstCompletedCalled = false;
            NormalCaptureAction.this.isReadyToCapture = true;
            this.numCaptureCompletedCalled = 0;
            this.numImageAvailableCalled = 0;
            this.numImageUnAvailableCalled = 0;
            NormalCaptureAction.this.isBursting = false;
            this.isReadyToCloseBurst = false;
            actionStateCallbackImpl.onBurst(3, null);
            NormalCaptureAction.LOGGER.debug("doBurstCallback!", new Object[0]);
        }
    }
}
