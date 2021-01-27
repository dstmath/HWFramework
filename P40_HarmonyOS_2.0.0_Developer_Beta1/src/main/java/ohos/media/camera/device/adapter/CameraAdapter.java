package ohos.media.camera.device.adapter;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.impl.PhysicalCaptureResultInfo;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.utils.SubmitInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Size;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import ohos.media.camera.device.adapter.CameraAdapter;
import ohos.media.camera.device.adapter.utils.Converter;
import ohos.media.camera.exception.AccessException;
import ohos.media.camera.exception.ExceptionTransfer;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.ResultKey;
import ohos.media.camera.params.adapter.ParameterKeyMapper;
import ohos.media.camera.params.adapter.ResultKeyMapper;
import ohos.media.camera.params.adapter.StaticCameraCharacteristics;
import ohos.media.camera.zidl.CameraAbilityNative;
import ohos.media.camera.zidl.CaptureTriggerInfo;
import ohos.media.camera.zidl.FrameConfigNative;
import ohos.media.camera.zidl.FrameResultNative;
import ohos.media.camera.zidl.ICamera;
import ohos.media.camera.zidl.ICameraCallback;
import ohos.media.camera.zidl.StreamConfiguration;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CameraAdapter implements ICamera, IBinder.DeathRecipient {
    private static final int CAPTURE_TRIGGER_ERROR_ID = -1;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraAdapter.class);
    private final CameraAbilityNative cameraAbilityNative;
    private final ICameraCallback cameraCallback;
    private final ICameraDeviceCallbacks cameraDeviceCallbacks;
    private ICameraDeviceUser cameraDeviceUser;
    private final Map<Integer, List<FrameConfigNative>> captureTriggerId2FrameConfigsCache = new ConcurrentHashMap();
    private int currentLoopingCaptureTriggerId = -1;
    private final Executor executor;
    private final FrameConfigMapper frameConfigMapper;
    private final Set<Long> lastFrameNumberCache = ConcurrentHashMap.newKeySet();
    private final SparseArray<OutputConfiguration> streamId2OutputConfigMap = new SparseArray<>();

    CameraAdapter(String str, CameraAbilityNative cameraAbilityNative2, StaticCameraCharacteristics staticCameraCharacteristics, ICameraCallback iCameraCallback) {
        this.cameraAbilityNative = cameraAbilityNative2;
        this.cameraCallback = iCameraCallback;
        this.executor = Executors.newSingleThreadExecutor();
        this.frameConfigMapper = new FrameConfigMapper(str, staticCameraCharacteristics);
        this.cameraDeviceCallbacks = new CameraDeviceCallbacksAdapter(iCameraCallback, cameraAbilityNative2, staticCameraCharacteristics, this.captureTriggerId2FrameConfigsCache, this.lastFrameNumberCache, this.executor);
    }

    /* access modifiers changed from: package-private */
    public void setCameraDeviceUser(ICameraDeviceUser iCameraDeviceUser) {
        this.cameraDeviceUser = iCameraDeviceUser;
    }

    @Override // ohos.media.camera.zidl.ICamera
    public int createOutput(StreamConfiguration streamConfiguration) throws AccessException {
        OutputConfiguration outputConfiguration;
        try {
            if (!streamConfiguration.isDeferred()) {
                outputConfiguration = new OutputConfiguration(Converter.convert2ASurface(streamConfiguration.getSurface()));
            } else {
                Size size = new Size(streamConfiguration.getSurfaceSize().width, streamConfiguration.getSurfaceSize().height);
                if (streamConfiguration.getSurfaceType() == 0) {
                    outputConfiguration = new OutputConfiguration(size, SurfaceHolder.class);
                } else {
                    LOGGER.warn("Invalid surface type!", new Object[0]);
                    return -1;
                }
            }
            int createStream = this.cameraDeviceUser.createStream(outputConfiguration);
            this.streamId2OutputConfigMap.put(createStream, outputConfiguration);
            return createStream;
        } catch (RemoteException | ServiceSpecificException | IllegalArgumentException e) {
            ExceptionTransfer.trans2AccessException(e);
            return -1;
        }
    }

    @Override // ohos.media.camera.zidl.ICamera
    public void deleteOutput(int i) throws AccessException {
        try {
            this.cameraDeviceUser.deleteStream(i);
            this.streamId2OutputConfigMap.delete(i);
        } catch (RemoteException | ServiceSpecificException e) {
            ExceptionTransfer.trans2AccessException(e);
        }
    }

    @Override // ohos.media.camera.zidl.ICamera
    public void beginConfig() throws AccessException {
        try {
            this.cameraDeviceUser.beginConfigure();
        } catch (RemoteException | ServiceSpecificException e) {
            ExceptionTransfer.trans2AccessException(e);
        }
    }

    @Override // ohos.media.camera.zidl.ICamera
    public void endConfig(int i) throws AccessException {
        try {
            this.cameraDeviceUser.endConfigure(i, new CameraMetadataNative());
        } catch (RemoteException | ServiceSpecificException e) {
            ExceptionTransfer.trans2AccessException(e);
        }
    }

    @Override // ohos.media.camera.zidl.ICamera
    public void finalizeOutput(int i, StreamConfiguration streamConfiguration) throws AccessException {
        try {
            OutputConfiguration outputConfiguration = this.streamId2OutputConfigMap.get(i);
            if (outputConfiguration == null) {
                LOGGER.error("finalizeOutput failed! Cannot find config for stream %{public}d", Integer.valueOf(i));
                return;
            }
            outputConfiguration.addSurface(Converter.convert2ASurface(streamConfiguration.getSurface()));
            this.cameraDeviceUser.finalizeOutputConfigurations(i, outputConfiguration);
        } catch (RemoteException | ServiceSpecificException | IllegalArgumentException e) {
            ExceptionTransfer.trans2AccessException(e);
        }
    }

    @Override // ohos.media.camera.zidl.ICamera
    public Map<ParameterKey.Key<?>, Object> getDefaultFrameConfigParameters(int i) throws AccessException {
        int i2 = 1;
        LOGGER.debug("getDefaultFrameConfigParameters for %{public}d", Integer.valueOf(i));
        if (i != 1) {
            if (i == 2) {
                i2 = 2;
            } else if (i != 3) {
                LOGGER.error("Unsupported template type:%{public}d", Integer.valueOf(i));
                ExceptionTransfer.trans2AccessException(new IllegalArgumentException("Unsupported template type:" + i));
            } else {
                i2 = 3;
            }
        }
        try {
            CameraMetadataNative createDefaultRequest = this.cameraDeviceUser.createDefaultRequest(i2);
            if (createDefaultRequest == null) {
                return null;
            }
            this.frameConfigMapper.addDefaultMetadataSetting(i2, createDefaultRequest);
            List<ParameterKey.Key<?>> supportedParameters = this.cameraAbilityNative.getSupportedParameters();
            if (supportedParameters != null) {
                return ParameterKeyMapper.getParameterKeyValues(createDefaultRequest, supportedParameters);
            }
            return null;
        } catch (RemoteException | ServiceSpecificException e) {
            ExceptionTransfer.trans2AccessException(e);
            return null;
        }
    }

    @Override // ohos.media.camera.zidl.ICamera
    public void waitIdle() throws AccessException {
        try {
            this.cameraDeviceUser.waitUntilIdle();
        } catch (RemoteException | ServiceSpecificException e) {
            ExceptionTransfer.trans2AccessException(e);
        }
    }

    @Override // ohos.media.camera.zidl.ICamera
    public void release() throws AccessException {
        ICameraDeviceUser iCameraDeviceUser = this.cameraDeviceUser;
        if (iCameraDeviceUser == null) {
            LOGGER.warn("Camera device user is already released", new Object[0]);
            return;
        }
        try {
            iCameraDeviceUser.disconnect();
            this.cameraDeviceUser = null;
        } catch (RemoteException | ServiceSpecificException e) {
            ExceptionTransfer.trans2AccessException(e);
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0102, code lost:
        r12 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0104, code lost:
        r12 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0106, code lost:
        r12 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0107, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        ohos.media.camera.exception.ExceptionTransfer.trans2AccessException(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x010b, code lost:
        r12 = r0.length;
        r13 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x010d, code lost:
        if (r13 < r12) goto L_0x010f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x010f, code lost:
        r0[r13].recoverStreamIdToSurface();
        r13 = r13 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0121, code lost:
        r13 = r0.length;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0122, code lost:
        if (r4 < r13) goto L_0x0124;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0124, code lost:
        r0[r4].recoverStreamIdToSurface();
        r4 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x012c, code lost:
        throw r12;
     */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0102 A[ExcHandler: RemoteException | ServiceSpecificException | IllegalArgumentException (e java.lang.Throwable), Splitter:B:19:0x0096] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0106 A[ExcHandler: RemoteException | ServiceSpecificException | IllegalArgumentException (e java.lang.Throwable), Splitter:B:9:0x0043] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x010f A[LOOP:4: B:32:0x010d->B:33:0x010f, LOOP_END] */
    @Override // ohos.media.camera.zidl.ICamera
    public CaptureTriggerInfo captureFrames(List<FrameConfigNative> list, boolean z) throws AccessException {
        int i = 0;
        LOGGER.info("captureFrames start, isLooping: %{public}b", Boolean.valueOf(z));
        CaptureRequest[] convert2CaptureRequests = this.frameConfigMapper.convert2CaptureRequests(list);
        for (CaptureRequest captureRequest : convert2CaptureRequests) {
            captureRequest.convertSurfaceToStreamId(this.streamId2OutputConfigMap);
        }
        SubmitInfo submitRequestList = this.cameraDeviceUser.submitRequestList(convert2CaptureRequests, z);
        if (submitRequestList == null) {
            LOGGER.error("submitInfo is null, return CaptureTriggerInfo as null", new Object[0]);
            int length = convert2CaptureRequests.length;
            while (i < length) {
                convert2CaptureRequests[i].recoverStreamIdToSurface();
                i++;
            }
            return null;
        }
        try {
            int requestId = submitRequestList.getRequestId();
            long lastFrameNumber = submitRequestList.getLastFrameNumber();
            LOGGER.info("submitCaptureRequest success, captureTriggerId: %{public}d, lastFrameNumber: %{public}d, isLooping: %{public}b", Integer.valueOf(requestId), Long.valueOf(lastFrameNumber), Boolean.valueOf(z));
            CaptureTriggerInfo captureTriggerInfo = new CaptureTriggerInfo(requestId, lastFrameNumber);
            if (z || lastFrameNumber != -1) {
                if (z) {
                    try {
                        LOGGER.info("change currentLoopingCaptureTriggerId to %{public}d", Integer.valueOf(requestId));
                        this.currentLoopingCaptureTriggerId = requestId;
                    } catch (RemoteException | ServiceSpecificException | IllegalArgumentException e) {
                    }
                }
                this.captureTriggerId2FrameConfigsCache.put(Integer.valueOf(requestId), list);
                LOGGER.debug("captureTriggerId: %{public}d added to the captureTriggerId2FrameConfigsCache", Integer.valueOf(requestId));
                this.lastFrameNumberCache.add(Long.valueOf(lastFrameNumber));
                LOGGER.debug("lastFrameNumber: %{public}d added to the lastFrameNumberCache", Long.valueOf(lastFrameNumber));
                this.executor.execute(new Runnable(requestId, lastFrameNumber) {
                    /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$eV3fnlOJIS5h8P3EWQbdYrY4QFI */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ long f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraAdapter.this.lambda$captureFrames$1$CameraAdapter(this.f$1, this.f$2);
                    }
                });
                LOGGER.debug("captureFrames emit onCaptureTriggerStarted event, captureTriggerId: %{public}d, lastFrameNumber: %{public}d", Integer.valueOf(requestId), Long.valueOf(lastFrameNumber));
                for (CaptureRequest captureRequest2 : convert2CaptureRequests) {
                    captureRequest2.recoverStreamIdToSurface();
                }
                LOGGER.info("captureFrames end", new Object[0]);
                return captureTriggerInfo;
            }
            LOGGER.warn("lastFrameNumber is NO_FRAME_NUMBER, emit onCaptureTriggerInterrupted event", new Object[0]);
            this.executor.execute(new Runnable(requestId) {
                /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$dWDWDDR08QM4w3Ye7JqkWEfZQ8 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CameraAdapter.this.lambda$captureFrames$0$CameraAdapter(this.f$1);
                }
            });
            int length2 = convert2CaptureRequests.length;
            while (i < length2) {
                convert2CaptureRequests[i].recoverStreamIdToSurface();
                i++;
            }
            return captureTriggerInfo;
        } catch (RemoteException | ServiceSpecificException | IllegalArgumentException e2) {
        }
    }

    public /* synthetic */ void lambda$captureFrames$0$CameraAdapter(int i) {
        this.cameraCallback.onCaptureTriggerInterrupted(i);
    }

    public /* synthetic */ void lambda$captureFrames$1$CameraAdapter(int i, long j) {
        this.cameraCallback.onCaptureTriggerStarted(i, j);
    }

    @Override // ohos.media.camera.zidl.ICamera
    public long cancelCaptureFrames(int i) throws AccessException {
        long j;
        RemoteException e;
        try {
            j = this.cameraDeviceUser.cancelRequest(i);
            try {
                LOGGER.info("cancelCaptureFrames captureTriggerId: %{public}d, lastFrameNumber: %{public}d", Integer.valueOf(i), Long.valueOf(j));
                if (j == -1) {
                    LOGGER.warn("lastFrameNumber is NO_FRAME_NUMBER, emit onCaptureTriggerInterrupted event", new Object[0]);
                    this.executor.execute(new Runnable(i) {
                        /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$l8xSbZaYRLgxrIUIbNgoGoav5gg */
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraAdapter.this.lambda$cancelCaptureFrames$2$CameraAdapter(this.f$1);
                        }
                    });
                    LOGGER.debug("captureTriggerId: %{public}d removed from the captureTriggerId2FrameConfigsCache", Integer.valueOf(i));
                    return -1;
                }
                this.lastFrameNumberCache.add(Long.valueOf(j));
                LOGGER.debug("lastFrameNumber: %{public}d added to the lastFrameNumberCache", Long.valueOf(j));
                return j;
            } catch (RemoteException | ServiceSpecificException e2) {
                e = e2;
                LOGGER.error("cancelCaptureFrames failed for captureTriggerId %{public}d", Integer.valueOf(i));
                ExceptionTransfer.trans2AccessException(e);
                return j;
            }
        } catch (RemoteException | ServiceSpecificException e3) {
            e = e3;
            j = -1;
            LOGGER.error("cancelCaptureFrames failed for captureTriggerId %{public}d", Integer.valueOf(i));
            ExceptionTransfer.trans2AccessException(e);
            return j;
        }
    }

    public /* synthetic */ void lambda$cancelCaptureFrames$2$CameraAdapter(int i) {
        this.cameraCallback.onCaptureTriggerInterrupted(i);
    }

    @Override // ohos.media.camera.zidl.ICamera
    public long flush() throws AccessException {
        long j;
        RemoteException e;
        try {
            j = this.cameraDeviceUser.flush();
            try {
                LOGGER.info("flush returned lastFrameNumber: %{public}d", Long.valueOf(j));
                if (j == -1) {
                    LOGGER.warn("lastFrameNumber is NO_FRAME_NUMBER, emit onCaptureTriggerInterrupted event", new Object[0]);
                    this.executor.execute(new Runnable() {
                        /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$47Wcqy0vhCxmUF3qXsItwbXMk */

                        @Override // java.lang.Runnable
                        public final void run() {
                            CameraAdapter.this.lambda$flush$3$CameraAdapter();
                        }
                    });
                    LOGGER.debug("captureTriggerId: %{public}d removed from the captureTriggerId2FrameConfigsCache", Integer.valueOf(this.currentLoopingCaptureTriggerId));
                    return -1;
                }
                this.lastFrameNumberCache.add(Long.valueOf(j));
                LOGGER.debug("lastFrameNumber: %{public}d added to the lastFrameNumberCache", Long.valueOf(j));
                return j;
            } catch (RemoteException | ServiceSpecificException e2) {
                e = e2;
                LOGGER.error("flush failed", new Object[0]);
                ExceptionTransfer.trans2AccessException(e);
                return j;
            }
        } catch (RemoteException | ServiceSpecificException e3) {
            e = e3;
            j = -1;
            LOGGER.error("flush failed", new Object[0]);
            ExceptionTransfer.trans2AccessException(e);
            return j;
        }
    }

    public /* synthetic */ void lambda$flush$3$CameraAdapter() {
        this.cameraCallback.onCaptureTriggerInterrupted(this.currentLoopingCaptureTriggerId);
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        LOGGER.warn("Camera device user died", new Object[0]);
        if (this.cameraDeviceUser == null) {
            LOGGER.warn("Camera device user is already released", new Object[0]);
            return;
        }
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            this.executor.execute(new Runnable() {
                /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CyLnNyEVQelw00EWEH485iU_htk */

                @Override // java.lang.Runnable
                public final void run() {
                    CameraAdapter.this.lambda$binderDied$4$CameraAdapter();
                }
            });
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public /* synthetic */ void lambda$binderDied$4$CameraAdapter() {
        this.cameraCallback.onCameraError(-6);
    }

    /* access modifiers changed from: package-private */
    public ICameraDeviceCallbacks getCallbacks() {
        return this.cameraDeviceCallbacks;
    }

    public static class CameraDeviceCallbacksAdapter extends ICameraDeviceCallbacks.Stub {
        private static final int DEFAULT_PARTIAL_COUNT = 1;
        private static final long INVALID_FRAME_TIMESTAMP = -1;
        private final CameraAbilityNative cameraAbilityNative;
        private final ICameraCallback cameraCallback;
        private final Map<Integer, List<FrameConfigNative>> captureTriggerId2FrameConfigsCache;
        private final Executor executor;
        private final Map<Long, Long> frameNumber2TimestampMap = new ConcurrentHashMap();
        private final Set<Long> lastFrameNumberCache;
        private final StaticCameraCharacteristics staticCameraCharacteristics;
        private final int totalPartialCount;

        public CameraDeviceCallbacksAdapter(ICameraCallback iCameraCallback, CameraAbilityNative cameraAbilityNative2, StaticCameraCharacteristics staticCameraCharacteristics2, Map<Integer, List<FrameConfigNative>> map, Set<Long> set, Executor executor2) {
            this.cameraCallback = iCameraCallback;
            this.cameraAbilityNative = cameraAbilityNative2;
            this.staticCameraCharacteristics = staticCameraCharacteristics2;
            this.captureTriggerId2FrameConfigsCache = map;
            this.lastFrameNumberCache = set;
            Integer num = (Integer) cameraAbilityNative2.getPropertyValue(PropertyKey.PARTIAL_RESULT_COUNT);
            if (num == null) {
                this.totalPartialCount = 1;
            } else {
                this.totalPartialCount = num.intValue();
            }
            this.executor = executor2;
        }

        public /* synthetic */ void lambda$onDeviceError$0$CameraAdapter$CameraDeviceCallbacksAdapter() {
            this.cameraCallback.onCameraError(-5);
        }

        public /* synthetic */ void lambda$onDeviceError$1$CameraAdapter$CameraDeviceCallbacksAdapter() {
            this.cameraCallback.onCameraError(-6);
        }

        public /* synthetic */ void lambda$onDeviceError$2$CameraAdapter$CameraDeviceCallbacksAdapter() {
            this.cameraCallback.onCameraError(-4);
        }

        public void onDeviceError(int i, CaptureResultExtras captureResultExtras) throws RemoteException {
            if (i == 0) {
                this.executor.execute(new Runnable() {
                    /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$7twFb45WTWwPG9STlSjjQXrH8 */

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onDeviceError$1$CameraAdapter$CameraDeviceCallbacksAdapter();
                    }
                });
            } else if (i == 1) {
                this.executor.execute(new Runnable() {
                    /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$OBTbgWByF7DxVlOoOZygSOcbd5M */

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onDeviceError$0$CameraAdapter$CameraDeviceCallbacksAdapter();
                    }
                });
            } else if (i == 3 || i == 4 || i == 5) {
                onFrameError(i, captureResultExtras);
            } else if (i != 6) {
                CameraAdapter.LOGGER.error("Unknown error code: %{public}d", Integer.valueOf(i));
                this.executor.execute(new Runnable() {
                    /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$GeFIK_lodcroqHO7IeJ8fRSzEmQ */

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onDeviceError$3$CameraAdapter$CameraDeviceCallbacksAdapter();
                    }
                });
            } else {
                this.executor.execute(new Runnable() {
                    /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$3jhsbD137ntAYuivHNI0a5Upje4 */

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onDeviceError$2$CameraAdapter$CameraDeviceCallbacksAdapter();
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onDeviceError$3$CameraAdapter$CameraDeviceCallbacksAdapter() {
            this.cameraCallback.onCameraError(-1);
        }

        private void onFrameError(int i, CaptureResultExtras captureResultExtras) {
            long frameNumber = captureResultExtras.getFrameNumber();
            int requestId = captureResultExtras.getRequestId();
            int subsequenceId = captureResultExtras.getSubsequenceId();
            int errorStreamId = captureResultExtras.getErrorStreamId();
            String errorPhysicalCameraId = captureResultExtras.getErrorPhysicalCameraId();
            CameraAdapter.LOGGER.warn("onFrameError errorCode: %{public}d, frameNumber: %{public}d, captureTriggerId: %{public}d, sequenceId: %{public}d, errorStreamId: %{public}d, errorPhysicalCameraId: %{public}s", Integer.valueOf(i), Long.valueOf(frameNumber), Integer.valueOf(requestId), Integer.valueOf(subsequenceId), Integer.valueOf(errorStreamId), errorPhysicalCameraId);
            this.executor.execute(new Runnable(new FrameResultNative.Builder(frameNumber, requestId, subsequenceId, getFrameConfigCached(requestId, subsequenceId)).errorStreamId(errorStreamId).errorPhysicalCameraId(errorPhysicalCameraId).build(), i == 5 ? -7 : i == 3 ? -8 : -9) {
                /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$EiNk82iUxv7MLAkBNXuCAc5IWIY */
                private final /* synthetic */ FrameResultNative f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onFrameError$4$CameraAdapter$CameraDeviceCallbacksAdapter(this.f$1, this.f$2);
                }
            });
            handleCacheRemoval(requestId, frameNumber);
        }

        public /* synthetic */ void lambda$onFrameError$4$CameraAdapter$CameraDeviceCallbacksAdapter(FrameResultNative frameResultNative, int i) {
            this.cameraCallback.onFrameError(frameResultNative, i);
        }

        private void handleCacheRemoval(int i, long j) {
            if (this.lastFrameNumberCache.remove(Long.valueOf(j))) {
                CameraAdapter.LOGGER.debug("lastFrameNumber: %{public}d removed from the lastFrameNumberCache", Long.valueOf(j));
                if (this.captureTriggerId2FrameConfigsCache.remove(Integer.valueOf(i)) != null) {
                    CameraAdapter.LOGGER.debug("captureTriggerId: %{public}d removed from the captureTriggerId2FrameConfigsCache", Integer.valueOf(i));
                }
            }
        }

        private FrameConfigNative getFrameConfigCached(int i, int i2) {
            return this.captureTriggerId2FrameConfigsCache.get(Integer.valueOf(i)).get(i2);
        }

        public void onDeviceIdle() throws RemoteException {
            CameraAdapter.LOGGER.info("onDeviceIdle", new Object[0]);
        }

        public void onCaptureStarted(CaptureResultExtras captureResultExtras, long j) throws RemoteException {
            long frameNumber = captureResultExtras.getFrameNumber();
            int requestId = captureResultExtras.getRequestId();
            int subsequenceId = captureResultExtras.getSubsequenceId();
            CameraAdapter.LOGGER.debug("onCaptureStarted frameNumber: %{public}d, captureTriggerId: %{public}d, sequenceId: %{public}d, timestamp: %{public}d", Long.valueOf(frameNumber), Integer.valueOf(requestId), Integer.valueOf(subsequenceId), Long.valueOf(j));
            this.frameNumber2TimestampMap.put(Long.valueOf(frameNumber), Long.valueOf(j));
            this.executor.execute(new Runnable(new FrameResultNative.Builder(frameNumber, requestId, subsequenceId, getFrameConfigCached(requestId, subsequenceId)).timestamp(j).build()) {
                /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$oGA__z5CyLxjNrBIb6JDwiyI4M */
                private final /* synthetic */ FrameResultNative f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onCaptureStarted$5$CameraAdapter$CameraDeviceCallbacksAdapter(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onCaptureStarted$5$CameraAdapter$CameraDeviceCallbacksAdapter(FrameResultNative frameResultNative) {
            this.cameraCallback.onFrameStarted(frameResultNative);
        }

        public void onResultReceived(CameraMetadataNative cameraMetadataNative, CaptureResultExtras captureResultExtras, PhysicalCaptureResultInfo[] physicalCaptureResultInfoArr) throws RemoteException {
            CameraAdapter.LOGGER.debug("onResultReceived start", new Object[0]);
            FrameResultNative convert2FrameResultNative = convert2FrameResultNative(cameraMetadataNative, captureResultExtras, physicalCaptureResultInfoArr);
            if (convert2FrameResultNative.isPartial()) {
                this.executor.execute(new Runnable(convert2FrameResultNative) {
                    /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$XLezgb6DvYsm5JNaQ5dDhPzauI */
                    private final /* synthetic */ FrameResultNative f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onResultReceived$6$CameraAdapter$CameraDeviceCallbacksAdapter(this.f$1);
                    }
                });
            } else {
                this.executor.execute(new Runnable(convert2FrameResultNative) {
                    /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$dj1yKAyMKFOZ8chAdsaKIlb91U */
                    private final /* synthetic */ FrameResultNative f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onResultReceived$7$CameraAdapter$CameraDeviceCallbacksAdapter(this.f$1);
                    }
                });
            }
            handleCaptureTriggerCompletedEvent(convert2FrameResultNative);
            CameraAdapter.LOGGER.debug("onResultReceived end", new Object[0]);
        }

        public /* synthetic */ void lambda$onResultReceived$6$CameraAdapter$CameraDeviceCallbacksAdapter(FrameResultNative frameResultNative) {
            this.cameraCallback.onFrameProgressed(frameResultNative);
        }

        public /* synthetic */ void lambda$onResultReceived$7$CameraAdapter$CameraDeviceCallbacksAdapter(FrameResultNative frameResultNative) {
            this.cameraCallback.onFrameCompleted(frameResultNative);
        }

        private void handleCaptureTriggerCompletedEvent(FrameResultNative frameResultNative) {
            int captureTriggerId = frameResultNative.getCaptureTriggerId();
            long frameNumber = frameResultNative.getFrameNumber();
            if (this.lastFrameNumberCache.remove(Long.valueOf(frameNumber))) {
                CameraAdapter.LOGGER.debug("lastFrameNumber: %{public}d removed from the lastFrameNumberCache", Long.valueOf(frameNumber));
                this.executor.execute(new Runnable(captureTriggerId, frameNumber) {
                    /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$6GTGsl4_Uc3R4sJK71RdAYcPFHI */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ long f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$handleCaptureTriggerCompletedEvent$8$CameraAdapter$CameraDeviceCallbacksAdapter(this.f$1, this.f$2);
                    }
                });
                CameraAdapter.LOGGER.debug("emit onCaptureTriggerCompleted event, captureTriggerId: %{public}d, frameNumber: %{public}d", Integer.valueOf(captureTriggerId), Long.valueOf(frameNumber));
            }
        }

        public /* synthetic */ void lambda$handleCaptureTriggerCompletedEvent$8$CameraAdapter$CameraDeviceCallbacksAdapter(int i, long j) {
            this.cameraCallback.onCaptureTriggerCompleted(i, j);
        }

        private FrameResultNative convert2FrameResultNative(CameraMetadataNative cameraMetadataNative, CaptureResultExtras captureResultExtras, PhysicalCaptureResultInfo[] physicalCaptureResultInfoArr) {
            long j;
            PhysicalCaptureResultInfo[] physicalCaptureResultInfoArr2 = physicalCaptureResultInfoArr;
            long frameNumber = captureResultExtras.getFrameNumber();
            int requestId = captureResultExtras.getRequestId();
            int subsequenceId = captureResultExtras.getSubsequenceId();
            int afTriggerId = captureResultExtras.getAfTriggerId();
            int precaptureTriggerId = captureResultExtras.getPrecaptureTriggerId();
            int partialResultCount = captureResultExtras.getPartialResultCount();
            int errorStreamId = captureResultExtras.getErrorStreamId();
            String errorPhysicalCameraId = captureResultExtras.getErrorPhysicalCameraId();
            CameraAdapter.LOGGER.debug("onResultReceived frameNumber: %{public}d, captureTriggerId: %{public}d, sequenceId: %{public}d, afTriggerId: %{public}d, preCaptureTriggerId: %{public}d, partialResultCount: %{public}d, errorStreamId: %{public}d, errorPhysicalCameraId: %{public}s", Long.valueOf(frameNumber), Integer.valueOf(requestId), Integer.valueOf(subsequenceId), Integer.valueOf(afTriggerId), Integer.valueOf(precaptureTriggerId), Integer.valueOf(partialResultCount), Integer.valueOf(errorStreamId), errorPhysicalCameraId);
            List<ResultKey.Key<?>> supportedResults = this.cameraAbilityNative.getSupportedResults();
            Map<ResultKey.Key<?>, Object> resultKeyValues = ResultKeyMapper.getResultKeyValues(cameraMetadataNative, supportedResults, this.staticCameraCharacteristics);
            HashMap hashMap = new HashMap(physicalCaptureResultInfoArr2.length);
            int length = physicalCaptureResultInfoArr2.length;
            int i = 0;
            while (i < length) {
                PhysicalCaptureResultInfo physicalCaptureResultInfo = physicalCaptureResultInfoArr2[i];
                hashMap.put(physicalCaptureResultInfo.getCameraId(), ResultKeyMapper.getResultKeyValues(physicalCaptureResultInfo.getCameraMetadata(), supportedResults, this.staticCameraCharacteristics));
                i++;
                length = length;
                physicalCaptureResultInfoArr2 = physicalCaptureResultInfoArr;
                resultKeyValues = resultKeyValues;
            }
            boolean z = partialResultCount < this.totalPartialCount;
            Long remove = this.frameNumber2TimestampMap.remove(Long.valueOf(frameNumber));
            if (remove == null) {
                j = -1;
            } else {
                j = remove.longValue();
            }
            return new FrameResultNative.Builder(frameNumber, requestId, subsequenceId, getFrameConfigCached(requestId, subsequenceId)).timestamp(j).afTriggerId(afTriggerId).preCaptureTriggerId(precaptureTriggerId).isPartial(z).errorStreamId(errorStreamId).errorPhysicalCameraId(errorPhysicalCameraId).logicalCameraResult(resultKeyValues).physicalCaptureResults(hashMap).build();
        }

        public void onPrepared(int i) throws RemoteException {
            CameraAdapter.LOGGER.info("onPrepared streamId: %{public}d", Integer.valueOf(i));
        }

        public void onRepeatingRequestError(long j, int i) throws RemoteException {
            CameraAdapter.LOGGER.warn("onRepeatingRequestError lastFrameNumber: %{public}d, captureTriggerId: %{public}d", Long.valueOf(j), Integer.valueOf(i));
            handleCacheRemoval(i, j);
            this.executor.execute(new Runnable(i) {
                /* class ohos.media.camera.device.adapter.$$Lambda$CameraAdapter$CameraDeviceCallbacksAdapter$o1KENSEHeuGSI1rkWJUxqKSF0FI */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CameraAdapter.CameraDeviceCallbacksAdapter.this.lambda$onRepeatingRequestError$9$CameraAdapter$CameraDeviceCallbacksAdapter(this.f$1);
                }
            });
            CameraAdapter.LOGGER.warn("onRepeatingRequestError, emit onCaptureTriggerInterrupted event", new Object[0]);
        }

        public /* synthetic */ void lambda$onRepeatingRequestError$9$CameraAdapter$CameraDeviceCallbacksAdapter(int i) {
            this.cameraCallback.onCaptureTriggerInterrupted(i);
        }

        public void onRequestQueueEmpty() {
            CameraAdapter.LOGGER.info("onRequestQueueEmpty", new Object[0]);
        }
    }
}
