package com.huawei.servicehost;

import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import com.huawei.servicehost.IIPListener;
import com.huawei.servicehost.d3d.IIPEvent4D3DKeyFrame;
import com.huawei.servicehost.d3d.IIPEvent4D3DStatus;
import com.huawei.servicehost.d3d.IIPRequest4CreatePipeline;
import com.huawei.servicehost.d3d.IIPRequest4SetSex;
import com.huawei.servicehost.normal.IIPEvent4Metadata;
import com.huawei.servicehost.normal.IIPRequest4CreatePipeline;
import com.huawei.servicehost.normal.IIPRequest4Metadata;
import com.huawei.servicehost.normal.IIPRequest4Surfaceless;
import com.huawei.servicehost.pp.IIPEvent4PPStatus;
import com.huawei.servicehost.pp.IIPEvent4Thumbnail;
import com.huawei.servicehost.pp.IIPRequest4CreatePipeline;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ServiceHostSession {
    private static final int AND_CONSTANT = 255;
    private static final int HEIGHT_INDEX_12 = 12;
    private static final int HEIGHT_INDEX_13 = 13;
    private static final int LEFT_SHIT_8 = 8;
    private static final int QUICK_THUMB_HEAD_SIZE = 16;
    private static final int QUICK_THUMB_MAX_SIZE = 2097152;
    private static final int QUICK_THUMB_PIXEL_SIZE = 4;
    private static final String REQUEST_TYPE_CREATE_PIPELINE = "createpipeline";
    private static final String REQUEST_TYPE_D3D_CANCEL_CAPTURE = "cancelcapture";
    private static final String REQUEST_TYPE_D3D_SET_SEX = "setsex";
    private static final String REQUEST_TYPE_D3D_START_CAPTURE = "startcapture";
    private static final String REQUEST_TYPE_D3D_START_FACE3D = "startface3d";
    private static final String REQUEST_TYPE_DESTROY_PIPELINE = "destorypipeline";
    private static final String REQUEST_TYPE_SEND_METADATA = "metadata";
    private static final String RESOLUTION_CONNECTOR = "x";
    private static final int RET_ERROR = -1;
    private static final int RET_OK = 0;
    public static final String SESSION_D3D = "d3d";
    private static final String SESSION_EVENT_D3DKEYFRAME = "keyframe";
    private static final String SESSION_EVENT_METADATA = "metadata";
    private static final String SESSION_EVENT_RESULT = "result";
    private static final String SESSION_EVENT_STATUS = "status";
    private static final String SESSION_EVENT_THUMBNAIL = "thumbnail";
    public static final String SESSION_NORMAL = "normal";
    private static final String SESSION_NULL = "session is null!";
    public static final String SESSION_PP = "pp";
    private static final boolean SHOW_LOG_V = false;
    private static final int SLAVE_CAMERA_ID = 2;
    private static final String TAG = "ServiceHostSession";
    private static final long TIME_OUT = 1000;
    private static final int WIDTH_INDEX_8 = 8;
    private static final int WIDTH_INDEX_9 = 9;
    private CaptureStatusListener captureStatusListener = null;
    private IIPEvent4D3DKeyFrame d3dKeyFrame = null;
    private IIPRequest4CreatePipeline ipD3dRequest4CreatePipeline = null;
    private IIPListener ipListener = new IIPListener.Stub() {
        /* class com.huawei.servicehost.ServiceHostSession.AnonymousClass1 */

        @Override // com.huawei.servicehost.IIPListener
        public void onIPCompleted(IIPRequest iipRequest) throws RemoteException {
            Log.v(ServiceHostSession.TAG, "onIPCompleted");
        }

        @Override // com.huawei.servicehost.IIPListener
        public void onIPEvent(IIPEvent iipEvent) throws RemoteException {
            if (iipEvent != null) {
                IBinder obj = iipEvent.getObject();
                String type = iipEvent.getType();
                char c = 65535;
                switch (type.hashCode()) {
                    case -934426595:
                        if (type.equals(ServiceHostSession.SESSION_EVENT_RESULT)) {
                            c = 3;
                            break;
                        }
                        break;
                    case -892481550:
                        if (type.equals(ServiceHostSession.SESSION_EVENT_STATUS)) {
                            c = 1;
                            break;
                        }
                        break;
                    case -450004177:
                        if (type.equals("metadata")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 507522670:
                        if (type.equals(ServiceHostSession.SESSION_EVENT_D3DKEYFRAME)) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1330532588:
                        if (type.equals(ServiceHostSession.SESSION_EVENT_THUMBNAIL)) {
                            c = 2;
                            break;
                        }
                        break;
                }
                if (c != 0) {
                    if (c == 1) {
                        ServiceHostSession.this.onIpStatusEvent(obj, iipEvent);
                    } else if (c == 2) {
                        ServiceHostSession.this.onThumbnailEvent(obj);
                    } else if (c == 3) {
                    } else {
                        if (c != 4) {
                            Log.d(ServiceHostSession.TAG, "IP event type: " + type);
                            return;
                        }
                        ServiceHostSession.this.onD3dKeyFrameEvent(obj);
                    }
                } else if (ServiceHostSession.this.metadataListener != null) {
                    ServiceHostSession.this.metadataListener.onMetadataArrived(ServiceHostSession.this.serviceHostUtil.getTotalCaptureResult(IIPEvent4Metadata.Stub.asInterface(obj)));
                }
            }
        }
    };
    private com.huawei.servicehost.normal.IIPRequest4CreatePipeline ipPreviewRequest4CreatePipeline = null;
    private com.huawei.servicehost.normal.IIPRequest4CreatePipeline ipVideoRequest4CreatePipeline = null;
    private boolean isVideo = SHOW_LOG_V;
    private KeyFrameListener keyFrameListener = null;
    private MetadataListener metadataListener = null;
    private com.huawei.servicehost.pp.IIPRequest4CreatePipeline ppRequest4CreatePipeline = null;
    private ServiceHostUtil serviceHostUtil = new ServiceHostUtil();
    private IImageProcessSession session = null;
    private Object sessionLock = new Object();
    private String sessionType = SESSION_NORMAL;
    private StatusListener statusListener = null;
    private ThumbnailListener thumbnailListener = null;

    public interface CaptureStatusListener {
        void onCaptureStatusArrived(int i);
    }

    public interface KeyFrameListener {
        void onKeyFrameArrived(ArrayList<ImageWrap> arrayList);
    }

    public interface MetadataListener {
        void onMetadataArrived(TotalCaptureResult totalCaptureResult);
    }

    public interface StatusListener {
        void onStatusArrived(int i);
    }

    public interface ThumbnailListener {
        void onThumbnailArrived(byte[] bArr, String str);
    }

    public ServiceHostSession(String type) {
        Log.i(TAG, "create session for: " + type);
        this.session = ImageProcessManager.get().createIPSession(type);
        if (this.session == null) {
            Log.e(TAG, "create session failed!");
        }
        setIpListener(this.ipListener);
        this.sessionType = type;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onIpStatusEvent(IBinder obj, IIPEvent iipEvent) throws RemoteException {
        String name = iipEvent.getName();
        if (SESSION_PP.equals(name)) {
            if (this.captureStatusListener != null) {
                IIPEvent4PPStatus iipEvent4PpStatus = IIPEvent4PPStatus.Stub.asInterface(obj);
                Log.i(TAG, "status name: " + name + ", status: " + iipEvent4PpStatus.getStatus());
                this.captureStatusListener.onCaptureStatusArrived(iipEvent4PpStatus.getStatus());
            } else {
                return;
            }
        }
        if (SESSION_D3D.equals(name) && this.statusListener != null) {
            this.statusListener.onStatusArrived(IIPEvent4D3DStatus.Stub.asInterface(obj).getStatus());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onThumbnailEvent(IBinder obj) throws RemoteException {
        if (this.thumbnailListener != null) {
            IIPEvent4Thumbnail iipEvent4Thumbnail = IIPEvent4Thumbnail.Stub.asInterface(obj);
            String path = iipEvent4Thumbnail.getFilePath();
            Log.d(TAG, "thumbnail arrived, file path: " + path);
            byte[] data = extractQuickThumbnail(iipEvent4Thumbnail);
            if (data.length == 0) {
                Log.e(TAG, "quick thumbnail is null!");
                return;
            }
            setIpListener(null);
            this.thumbnailListener.onThumbnailArrived(data, path);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onD3dKeyFrameEvent(IBinder obj) throws RemoteException {
        if (this.keyFrameListener != null) {
            this.d3dKeyFrame = IIPEvent4D3DKeyFrame.Stub.asInterface(obj);
            IIPEvent4D3DKeyFrame iIPEvent4D3DKeyFrame = this.d3dKeyFrame;
            if (iIPEvent4D3DKeyFrame == null) {
                Log.i(TAG, "key frame is invalid.");
                return;
            }
            int count = iIPEvent4D3DKeyFrame.getKeyFrameCount();
            if (count <= 0) {
                Log.i(TAG, "invalid frame count: " + count);
                return;
            }
            try {
                Log.i(TAG, "key frame count: " + count);
                ArrayList<ImageWrap> keyFrameList = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    ImageWrap keyFrameBuffer = this.d3dKeyFrame.getKeyFrame(i);
                    if (keyFrameBuffer == null) {
                        Log.i(TAG, "key frame buffer is invalid!");
                    } else {
                        keyFrameList.add(keyFrameBuffer);
                    }
                }
                this.keyFrameListener.onKeyFrameArrived(keyFrameList);
            } catch (RemoteException e) {
                Log.e(TAG, "receive key frame failed." + e.getMessage());
            }
        }
    }

    public void destroy() {
        Log.i(TAG, "destroy session for " + this.sessionType);
        setIpListener(null);
        synchronized (this.sessionLock) {
            if (this.session == null) {
                Log.i(TAG, "session for " + this.sessionType + " has destroyed before!");
                return;
            }
            try {
                this.session.process(this.session.createIPRequest(REQUEST_TYPE_DESTROY_PIPELINE), true);
                this.session = null;
                Log.i(TAG, "destroy successfully for " + this.sessionType);
            } catch (RemoteException e) {
                Log.e(TAG, "destroy session for " + this.sessionType + " failed: " + e.getMessage());
            }
        }
    }

    public List<SHSurface> exchangeSurface(List<SHSurface> shSurfaces, String json, List<Size> captureSizes) {
        if (!isSurfaceValid(shSurfaces)) {
            return null;
        }
        if (SESSION_D3D.equals(this.sessionType)) {
            return exchangeSurface4D3d(shSurfaces, json);
        }
        if (!isCaptureSizeValid(captureSizes)) {
            return null;
        }
        Log.i(TAG, "exchange surfaces, size: " + shSurfaces.size());
        SHSurface previewSurfaceForBackCamera = null;
        SHSurface previewSurfaceForOtherCamera = null;
        SHSurface videoSurface = null;
        for (SHSurface shSurface : shSurfaces) {
            if (!(shSurface == null || shSurface.size == null || shSurface.type == null)) {
                Log.i(TAG, "input surface: " + shSurface.surface + ", type: " + shSurface.type + ", size: " + shSurface.size + ", camera id: " + shSurface.cameraId);
                int i = AnonymousClass2.$SwitchMap$com$huawei$servicehost$SurfaceType[shSurface.type.ordinal()];
                if (i != 1) {
                    if (i != 2) {
                        Log.e(TAG, "invalid surface type!");
                    } else {
                        videoSurface = shSurface;
                    }
                } else if (shSurface.cameraId == 0) {
                    previewSurfaceForBackCamera = shSurface;
                } else {
                    previewSurfaceForOtherCamera = shSurface;
                }
            }
        }
        if (previewSurfaceForBackCamera == null) {
            Log.e(TAG, "invalid preview surface!");
            return null;
        }
        if (videoSurface != null) {
            processIpSessionVideoRequest(previewSurfaceForBackCamera, videoSurface, captureSizes.get(0), json);
        } else {
            List<SHSurface> shPreviewSurfaces = new ArrayList<>();
            shPreviewSurfaces.add(previewSurfaceForBackCamera);
            shPreviewSurfaces.add(previewSurfaceForOtherCamera);
            processIpSessionPreviewRequest(shPreviewSurfaces, captureSizes, json);
        }
        return getSurfaceList(previewSurfaceForBackCamera, previewSurfaceForOtherCamera, videoSurface);
    }

    /* renamed from: com.huawei.servicehost.ServiceHostSession$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$servicehost$SurfaceType = new int[SurfaceType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$servicehost$SurfaceType[SurfaceType.SURFACE_FOR_PREVIEW.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$servicehost$SurfaceType[SurfaceType.SURFACE_FOR_VIDEO.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private List<SHSurface> getSurfaceList(SHSurface previewSurfaceForBackCamera, SHSurface previewSurfaceForOtherCamera, SHSurface videoSurface) {
        List<SHSurface> surfaces = new ArrayList<>();
        previewSurfaceForBackCamera.surface = getBackCameraPreviewSurface();
        surfaces.add(previewSurfaceForBackCamera);
        if (videoSurface != null) {
            videoSurface.surface = getVideoSurface();
            if (videoSurface.surface != null) {
                surfaces.add(videoSurface);
            }
        }
        SHSurface shMetadataSurface = new SHSurface();
        shMetadataSurface.surface = getMetadataSurface();
        if (shMetadataSurface.surface != null) {
            shMetadataSurface.type = SurfaceType.SURFACE_FOR_METADATA;
            shMetadataSurface.cameraId = previewSurfaceForBackCamera.cameraId;
            surfaces.add(shMetadataSurface);
        }
        SHSurface shCapture1Surface = new SHSurface();
        shCapture1Surface.surface = getFirstCaptureSurface();
        if (shCapture1Surface.surface != null) {
            shCapture1Surface.type = SurfaceType.SURFACE_FOR_CAPTURE;
            shCapture1Surface.cameraId = previewSurfaceForBackCamera.cameraId;
            surfaces.add(shCapture1Surface);
        }
        if (previewSurfaceForOtherCamera != null) {
            previewSurfaceForOtherCamera.surface = getOtherCameraPreviewSurface();
            if (previewSurfaceForOtherCamera.surface != null) {
                surfaces.add(previewSurfaceForOtherCamera);
            }
            SHSurface shCapture2Surface = new SHSurface();
            shCapture2Surface.surface = getSecondCaptureSurface();
            if (shCapture2Surface.surface != null) {
                shCapture2Surface.type = SurfaceType.SURFACE_FOR_CAPTURE;
                shCapture2Surface.cameraId = previewSurfaceForOtherCamera.cameraId;
                surfaces.add(shCapture2Surface);
            }
        }
        Log.i(TAG, "exchange surface successfully, surfaces size: " + surfaces.size());
        return surfaces;
    }

    private boolean isSurfaceValid(List<SHSurface> shSurfaces) {
        if (shSurfaces == null) {
            Log.e(TAG, "invalid input surface list.");
            return SHOW_LOG_V;
        } else if (shSurfaces.size() != 0) {
            return true;
        } else {
            Log.e(TAG, "invalid input surface.");
            return SHOW_LOG_V;
        }
    }

    private boolean isCaptureSizeValid(List<Size> captureSizes) {
        if (captureSizes == null) {
            Log.e(TAG, "invalid input capture list.");
            return SHOW_LOG_V;
        } else if (captureSizes.size() != 0) {
            return true;
        } else {
            Log.e(TAG, "invalid input capture size.");
            return SHOW_LOG_V;
        }
    }

    public int capture(ServiceHostSession previewSession, String filePath, String json) {
        return processPpSessionCaptureRequest(previewSession, filePath, json, SHOW_LOG_V);
    }

    public int capture(ServiceHostSession previewSession, String filePath, String json, boolean isClickDown) {
        return processPpSessionCaptureRequest(previewSession, filePath, json, isClickDown);
    }

    public void setMetadataListener(MetadataListener listener) {
        Log.i(TAG, "set metadata listener: " + listener);
        this.metadataListener = listener;
    }

    public void setThumbnailListener(ThumbnailListener listener) {
        Log.i(TAG, "set thumbnail listener: " + listener);
        this.thumbnailListener = listener;
    }

    public void setStatusListener(StatusListener listener) {
        Log.i(TAG, "set status listener: " + listener);
        this.statusListener = listener;
    }

    public void setCaptureStatusListener(CaptureStatusListener listener) {
        Log.i(TAG, "set capture status listener: " + listener);
        this.captureStatusListener = listener;
    }

    public void setKeyFrameListener(KeyFrameListener listener) {
        Log.i(TAG, "set key frame listener: " + listener);
        this.keyFrameListener = listener;
    }

    public void sendRequest(List<ServiceHostMetadata> requests) {
        logDebug("send request");
        if (!SESSION_D3D.equals(this.sessionType)) {
            if (requests.size() <= 0) {
                Log.e(TAG, "invalid requests to servicehost.");
                return;
            }
            IIPRequest iipRequest4Metadata = createIpRequest("metadata");
            if (iipRequest4Metadata == null) {
                Log.e(TAG, "create IPRequest for metadata failed!");
                return;
            }
            try {
                IIPRequest4Metadata.Stub.asInterface(getBinderFromRequest(iipRequest4Metadata)).setMetadata(requests.get(0).getNativeMetadata());
                logDebug("process metadata request.");
                process(iipRequest4Metadata, true);
            } catch (RemoteException e) {
                Log.e(TAG, "set metadata failed: " + e.getMessage());
            }
        }
    }

    public void setParameter(int key, Bundle param) {
    }

    public void getParameter(int key, Bundle param) {
    }

    private void processIpSessionPreviewRequest(List<SHSurface> shPreviewSurfaces, List<Size> captureSizes, String json) {
        Log.i(TAG, "process IP session request.");
        IIPRequest ipRequest = createIpRequest(REQUEST_TYPE_CREATE_PIPELINE);
        IBinder binder = getBinderFromRequest(ipRequest);
        if (binder == null) {
            Log.e(TAG, "IP request get binder failed!");
            return;
        }
        this.isVideo = SHOW_LOG_V;
        SHSurface previewSurfaceForOtherCamera = null;
        if (shPreviewSurfaces.size() > 1) {
            previewSurfaceForOtherCamera = shPreviewSurfaces.get(1);
        }
        Size secondCaptureSize = null;
        if (captureSizes.size() > 1) {
            secondCaptureSize = captureSizes.get(1);
        }
        this.ipPreviewRequest4CreatePipeline = IIPRequest4CreatePipeline.Stub.asInterface(binder);
        try {
            int usage = BufferShareManager.get().getDefaultUsage();
            Log.i(TAG, "get default usage: " + Integer.toHexString(usage));
            SHSurface previewSurfaceForBackCamera = shPreviewSurfaces.get(0);
            prepareForBackCameraPreview(usage, previewSurfaceForBackCamera);
            if (previewSurfaceForBackCamera.surface != null) {
                this.ipPreviewRequest4CreatePipeline.setPreview1Surface(previewSurfaceForBackCamera.surface);
            } else {
                Log.e(TAG, "setPreview1Surface not call due to null surface");
            }
            prepareForFirstCapture(usage, captureSizes.get(0));
            if (previewSurfaceForOtherCamera != null) {
                prepareForOtherCameraPreview(usage, previewSurfaceForOtherCamera);
            }
            if (secondCaptureSize != null) {
                prepareForSecondCapture(usage, secondCaptureSize);
            }
            this.ipPreviewRequest4CreatePipeline.setLayout(json);
            process(ipRequest, true);
        } catch (RemoteException e) {
            Log.e(TAG, "process IP session request failed: " + e.getMessage());
        }
    }

    private void prepareForFirstCapture(int usage, Size capture1Size) throws RemoteException {
        ImageDescriptor capture1ImageDescriptor = new ImageDescriptor();
        capture1ImageDescriptor.setUsage(usage);
        capture1ImageDescriptor.setFormat(35);
        if (capture1Size != null) {
            capture1ImageDescriptor.setWidth(capture1Size.getWidth());
            capture1ImageDescriptor.setHeight(capture1Size.getHeight());
            Log.d(TAG, "set capture1 size: " + capture1Size.getWidth() + RESOLUTION_CONNECTOR + capture1Size.getHeight());
        }
        this.ipPreviewRequest4CreatePipeline.setCaptureFormat(capture1ImageDescriptor);
    }

    private void prepareForSecondCapture(int usage, Size capture1Size) throws RemoteException {
        ImageDescriptor capture2ImageDescriptor = new ImageDescriptor();
        capture2ImageDescriptor.setUsage(usage);
        capture2ImageDescriptor.setFormat(35);
        if (capture1Size != null) {
            capture2ImageDescriptor.setWidth(capture1Size.getWidth());
            capture2ImageDescriptor.setHeight(capture1Size.getHeight());
            Log.d(TAG, "set capture2 size: " + capture1Size.getWidth() + RESOLUTION_CONNECTOR + capture1Size.getHeight());
        }
        this.ipPreviewRequest4CreatePipeline.setCaptureFormatLine2(capture2ImageDescriptor);
    }

    private void prepareForBackCameraPreview(int usage, SHSurface shPreview1Surface) throws RemoteException {
        ImageDescriptor backPreviewImageDescriptor = new ImageDescriptor();
        backPreviewImageDescriptor.setUsage(usage | 2048);
        if (shPreview1Surface.size != null) {
            backPreviewImageDescriptor.setWidth(shPreview1Surface.size.getWidth());
            backPreviewImageDescriptor.setHeight(shPreview1Surface.size.getHeight());
            Log.d(TAG, "set preview1 size: " + shPreview1Surface.size.getWidth() + RESOLUTION_CONNECTOR + shPreview1Surface.size.getHeight());
        }
        this.ipPreviewRequest4CreatePipeline.setPreviewFormat(backPreviewImageDescriptor);
    }

    private void prepareForOtherCameraPreview(int usage, SHSurface shPreview2Surface) throws RemoteException {
        ImageDescriptor otherPreviewImageDescriptor = new ImageDescriptor();
        otherPreviewImageDescriptor.setUsage(usage | 2048);
        if (shPreview2Surface.size != null) {
            otherPreviewImageDescriptor.setWidth(shPreview2Surface.size.getWidth());
            otherPreviewImageDescriptor.setHeight(shPreview2Surface.size.getHeight());
            Log.d(TAG, "set preview2 size: " + shPreview2Surface.size.getWidth() + RESOLUTION_CONNECTOR + shPreview2Surface.size.getHeight());
        }
        this.ipPreviewRequest4CreatePipeline.setPreviewFormatLine2(otherPreviewImageDescriptor);
        if (shPreview2Surface.surface != null) {
            this.ipPreviewRequest4CreatePipeline.setPreview1Surface(shPreview2Surface.surface);
        }
    }

    private int processPpSessionCaptureRequest(ServiceHostSession previewSession, String filePath, String json, boolean isClickDown) {
        Log.i(TAG, "process PP session request");
        if (previewSession == null) {
            Log.e(TAG, "Invalid preview session!");
            return -1;
        }
        IIPRequest ipRequest = createIpRequest(REQUEST_TYPE_CREATE_PIPELINE);
        IBinder binder = getBinderFromRequest(ipRequest);
        if (binder == null) {
            Log.e(TAG, "PP request get binder failed!");
            return -1;
        }
        if (this.ppRequest4CreatePipeline == null) {
            this.ppRequest4CreatePipeline = IIPRequest4CreatePipeline.Stub.asInterface(binder);
        }
        try {
            this.ppRequest4CreatePipeline.setLayout(json);
            this.ppRequest4CreatePipeline.setFilePath(filePath);
            this.ppRequest4CreatePipeline.SetClickDown(isClickDown);
            this.ppRequest4CreatePipeline.setForegroundSession(previewSession.asBinder());
            return process(ipRequest, true);
        } catch (RemoteException e) {
            Log.e(TAG, "process PP session request failed: " + e.getMessage());
            return -1;
        }
    }

    private void processIpSessionVideoRequest(SHSurface shPreviewSurface, SHSurface shVideoSurface, Size captureSize, String json) {
        Log.i(TAG, "process IP video request()");
        IIPRequest ipRequest = createIpRequest(REQUEST_TYPE_CREATE_PIPELINE);
        IBinder binder = getBinderFromRequest(ipRequest);
        if (binder == null) {
            Log.e(TAG, "IP request get binder failed!");
            return;
        }
        this.isVideo = true;
        if (this.ipVideoRequest4CreatePipeline == null) {
            this.ipVideoRequest4CreatePipeline = IIPRequest4CreatePipeline.Stub.asInterface(binder);
        }
        try {
            int usage = BufferShareManager.get().getDefaultUsage();
            Log.i(TAG, "get default usage: " + Integer.toHexString(usage));
            prepareForPreviewConfiguration(shPreviewSurface);
            ImageDescriptor captureImageDescriptor = new ImageDescriptor();
            if (captureSize != null) {
                captureImageDescriptor.setWidth(captureSize.getWidth());
                captureImageDescriptor.setHeight(captureSize.getHeight());
            }
            captureImageDescriptor.setUsage(usage);
            captureImageDescriptor.setFormat(35);
            this.ipVideoRequest4CreatePipeline.setCaptureFormat(captureImageDescriptor);
            if (captureSize != null) {
                Log.d(TAG, "set video capture size: " + captureSize.getWidth() + RESOLUTION_CONNECTOR + captureSize.getHeight());
            }
            ImageDescriptor videoImageDescriptor = new ImageDescriptor();
            if (shVideoSurface.size != null) {
                videoImageDescriptor.setWidth(shVideoSurface.size.getWidth());
                videoImageDescriptor.setHeight(shVideoSurface.size.getHeight());
                Log.d(TAG, "set video size: " + shVideoSurface.size.getWidth() + RESOLUTION_CONNECTOR + shVideoSurface.size.getHeight());
            }
            videoImageDescriptor.setUsage(73728);
            this.ipVideoRequest4CreatePipeline.setVideoFormat(videoImageDescriptor);
            this.ipVideoRequest4CreatePipeline.setLayout(json);
            this.ipVideoRequest4CreatePipeline.setPreview1Surface(shPreviewSurface.surface);
            this.ipVideoRequest4CreatePipeline.setCamera1VideoSurface(shVideoSurface.surface);
            process(ipRequest, true);
        } catch (RemoteException e) {
            Log.e(TAG, "process ip video request failed:" + e.getMessage());
        }
    }

    private void prepareForPreviewConfiguration(SHSurface shPreviewSurface) throws RemoteException {
        ImageDescriptor previewImageDescriptor = new ImageDescriptor();
        if (shPreviewSurface.size != null) {
            previewImageDescriptor.setWidth(shPreviewSurface.size.getWidth());
            previewImageDescriptor.setHeight(shPreviewSurface.size.getHeight());
            Log.d(TAG, "set video preview size: " + shPreviewSurface.size.getWidth() + RESOLUTION_CONNECTOR + shPreviewSurface.size.getHeight());
        }
        previewImageDescriptor.setUsage(2304);
        this.ipVideoRequest4CreatePipeline.setPreviewFormat(previewImageDescriptor);
    }

    private IIPRequest createIpRequest(String type) {
        IIPRequest request;
        long startTime = System.currentTimeMillis();
        synchronized (this.sessionLock) {
            if (this.session == null) {
                Log.e(TAG, SESSION_NULL);
                return null;
            }
            try {
                request = this.session.createIPRequest(type);
            } catch (RemoteException e) {
                Log.e(TAG, "create " + type + " IP request failed: " + e.getMessage());
                return null;
            }
        }
        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > TIME_OUT) {
            Log.w(TAG, "createIpRequest cost " + costTime);
        }
        return request;
    }

    private IBinder getBinderFromRequest(IIPRequest request) {
        logDebug("get binder from request.");
        if (request == null) {
            Log.e(TAG, "request is null!");
            return null;
        }
        try {
            return request.getObject();
        } catch (RemoteException e) {
            Log.e(TAG, "Get IBinder from IP Request fail.");
            return null;
        }
    }

    private int process(IIPRequest request, boolean isSync) {
        long startTime = System.currentTimeMillis();
        int ret = -1;
        synchronized (this.sessionLock) {
            if (this.session == null) {
                Log.e(TAG, SESSION_NULL);
                return -1;
            }
            try {
                ret = this.session.process(request, isSync);
            } catch (RemoteException e) {
                Log.e(TAG, "process request fail: " + e.getMessage());
            }
        }
        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > TIME_OUT) {
            Log.w(TAG, "process cost " + costTime);
        }
        return ret;
    }

    private IBinder asBinder() {
        synchronized (this.sessionLock) {
            if (this.session == null) {
                Log.e(TAG, SESSION_NULL);
                return null;
            }
            return this.session.asBinder();
        }
    }

    private Surface getBackCameraPreviewSurface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.ipPreviewRequest4CreatePipeline;
        if (this.isVideo) {
            pipeline = this.ipVideoRequest4CreatePipeline;
        }
        try {
            surface = pipeline.getCamera1Surface();
        } catch (RemoteException e) {
            Log.e(TAG, "get preview1 surface fail: " + e.getMessage());
        }
        if (!checkSurfaceValid(surface)) {
            surface = null;
        }
        Log.i(TAG, "get preview1 surface: " + surface);
        return surface;
    }

    private Surface getOtherCameraPreviewSurface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.ipPreviewRequest4CreatePipeline;
        if (this.isVideo) {
            pipeline = this.ipVideoRequest4CreatePipeline;
        }
        try {
            surface = pipeline.getCamera2Surface();
        } catch (RemoteException e) {
            Log.e(TAG, "get preview2 surface fail: " + e.getMessage());
        }
        if (!checkSurfaceValid(surface)) {
            surface = null;
        }
        Log.i(TAG, "get preview2 surface: " + surface);
        return surface;
    }

    private Surface getMetadataSurface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.ipPreviewRequest4CreatePipeline;
        if (this.isVideo) {
            pipeline = this.ipVideoRequest4CreatePipeline;
        }
        try {
            surface = pipeline.getMetadataSurface();
        } catch (RemoteException e) {
            Log.e(TAG, "get metadata surface fail: " + e.getMessage());
        }
        if (!checkSurfaceValid(surface)) {
            surface = null;
        }
        Log.i(TAG, "get metadata surface: " + surface);
        return surface;
    }

    private Surface getFirstCaptureSurface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.ipPreviewRequest4CreatePipeline;
        if (this.isVideo) {
            pipeline = this.ipVideoRequest4CreatePipeline;
        }
        try {
            surface = pipeline.getCamera1CapSurface();
        } catch (RemoteException e) {
            Log.e(TAG, "get capture1 surface fail: " + e.getMessage());
        }
        if (!checkSurfaceValid(surface)) {
            surface = null;
        }
        Log.i(TAG, "get capture1 surface: " + surface);
        return surface;
    }

    private Surface getSecondCaptureSurface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.ipPreviewRequest4CreatePipeline;
        if (this.isVideo) {
            pipeline = this.ipVideoRequest4CreatePipeline;
        }
        try {
            surface = pipeline.getCamera2CapSurface();
        } catch (RemoteException e) {
            Log.e(TAG, "get capture2 surface fail: " + e.getMessage());
        }
        if (!checkSurfaceValid(surface)) {
            surface = null;
        }
        Log.i(TAG, "get capture2 surface: " + surface);
        return surface;
    }

    private Surface getVideoSurface() {
        Surface surface = null;
        try {
            surface = this.ipVideoRequest4CreatePipeline.getCamera1VideoSurface();
        } catch (RemoteException e) {
            Log.e(TAG, "get video1 surface fail: " + e.getMessage());
        }
        if (!checkSurfaceValid(surface)) {
            surface = null;
        }
        Log.i(TAG, "get video1 surface: " + surface);
        return surface;
    }

    private void setIpListener(IIPListener listener) {
        Log.i(TAG, "set IP listener to servicehost: " + listener);
        synchronized (this.sessionLock) {
            if (this.session == null) {
                Log.e(TAG, SESSION_NULL);
                return;
            }
            try {
                this.session.setIPListener(listener);
            } catch (RemoteException e) {
                Log.e(TAG, "create IP listner failed: " + e.getMessage());
            }
        }
    }

    private byte[] extractQuickThumbnail(IIPEvent4Thumbnail iipEvent4Thumbnail) {
        Log.i(TAG, "extract quick thumbnail.");
        try {
            ImageWrap imageWrap = iipEvent4Thumbnail.getImage();
            if (imageWrap == null) {
                return new byte[0];
            }
            ByteBuffer dataBuffer = imageWrap.getData();
            if (dataBuffer == null) {
                return new byte[0];
            }
            int width = (dataBuffer.get(8) & 255) + ((dataBuffer.get(WIDTH_INDEX_9) & 255) << 8);
            int height = (dataBuffer.get(12) & 255) + ((dataBuffer.get(13) & 255) << 8);
            Log.i(TAG, "width: " + width + ", height: " + height);
            int quickThumbSize = (width * height * 4) + 16;
            if (quickThumbSize <= 0 || quickThumbSize > QUICK_THUMB_MAX_SIZE) {
                quickThumbSize = QUICK_THUMB_MAX_SIZE;
            }
            byte[] data = new byte[quickThumbSize];
            dataBuffer.rewind();
            dataBuffer.get(data);
            Log.i(TAG, "extract quick thumbnail, size: " + quickThumbSize);
            return data;
        } catch (RemoteException e) {
            Log.e(TAG, "parse event fail:" + e.getMessage());
            return new byte[0];
        }
    }

    private boolean checkSurfaceValid(Surface surface) {
        if (surface == null) {
            return SHOW_LOG_V;
        }
        if (surface.isValid()) {
            return true;
        }
        Log.e(TAG, "get invalid surface!");
        return SHOW_LOG_V;
    }

    private List<SHSurface> exchangeSurface4D3d(List<SHSurface> shSurfaces, String json) {
        Log.i(TAG, "exchange surface for d3d.");
        SHSurface shPreviewSurface = shSurfaces.get(0);
        if (shPreviewSurface == null) {
            Log.e(TAG, "invalid preview surface!");
            return null;
        }
        int slaveCameraId = 2;
        if (shSurfaces.size() > 1) {
            slaveCameraId = shSurfaces.get(1).cameraId;
        }
        processIpSessionD3dRequest(shPreviewSurface, json);
        List<SHSurface> surfaces = new ArrayList<>();
        try {
            SHSurface backCameraPreviewSurface = new SHSurface();
            backCameraPreviewSurface.surface = this.ipD3dRequest4CreatePipeline.getCamera1Surface();
            backCameraPreviewSurface.type = SurfaceType.SURFACE_FOR_D3DPREVIEW;
            backCameraPreviewSurface.cameraId = shPreviewSurface.cameraId;
            surfaces.add(backCameraPreviewSurface);
            SHSurface otherCameraPreviewSurface = new SHSurface();
            otherCameraPreviewSurface.surface = this.ipD3dRequest4CreatePipeline.getCamera2Surface();
            otherCameraPreviewSurface.type = SurfaceType.SURFACE_FOR_D3DPREVIEW;
            otherCameraPreviewSurface.cameraId = slaveCameraId;
            surfaces.add(otherCameraPreviewSurface);
            SHSurface shMetadataSurface = new SHSurface();
            shMetadataSurface.surface = this.ipD3dRequest4CreatePipeline.getMetadataSurface();
            shMetadataSurface.type = SurfaceType.SURFACE_FOR_METADATA;
            shMetadataSurface.cameraId = shPreviewSurface.cameraId;
            surfaces.add(shMetadataSurface);
            SHSurface depthMapSurface = new SHSurface();
            depthMapSurface.surface = this.ipD3dRequest4CreatePipeline.getDmapSurface();
            depthMapSurface.type = SurfaceType.SURFACE_FOR_DMAP;
            depthMapSurface.cameraId = shPreviewSurface.cameraId;
            surfaces.add(depthMapSurface);
        } catch (RemoteException e) {
            Log.e(TAG, "process set sex session request failed: " + e.getMessage());
        }
        Log.i(TAG, "d3d exchange surface successfully, surfaces size: " + surfaces.size());
        return surfaces;
    }

    private void processIpSessionD3dRequest(SHSurface shPreviewSurface, String json) {
        Log.i(TAG, "process IP session request for d3d.");
        IIPRequest ipRequest = createIpRequest(REQUEST_TYPE_CREATE_PIPELINE);
        IBinder binder = getBinderFromRequest(ipRequest);
        if (binder == null) {
            Log.e(TAG, "IP request for d3d get binder failed!");
            return;
        }
        this.ipD3dRequest4CreatePipeline = IIPRequest4CreatePipeline.Stub.asInterface(binder);
        try {
            this.ipD3dRequest4CreatePipeline.setPreview1Surface(shPreviewSurface.surface);
            this.ipD3dRequest4CreatePipeline.setLayout(json);
            process(ipRequest, true);
        } catch (RemoteException e) {
            Log.e(TAG, "process d3d IP session request failed: " + e.getMessage());
        }
    }

    public void startPhotoing() {
        Log.i(TAG, "start capture for d3d.");
        process(createIpRequest(REQUEST_TYPE_D3D_START_CAPTURE), true);
    }

    public void canclePhotoing() {
        Log.i(TAG, "cancel capture for d3d.");
        process(createIpRequest(REQUEST_TYPE_D3D_CANCEL_CAPTURE), true);
    }

    public void startFace3D() {
        Log.i(TAG, "start face 3d for d3d.");
        process(createIpRequest(REQUEST_TYPE_D3D_START_FACE3D), true);
    }

    public void setSex(int var, String filePath, int fileSource) {
        Log.i(TAG, "set sex, var: " + var + ", file source: " + fileSource);
        IIPRequest request = createIpRequest(REQUEST_TYPE_D3D_SET_SEX);
        IBinder binder = getBinderFromRequest(request);
        if (binder == null) {
            Log.e(TAG, "IP request for d3d get binder failed!");
            return;
        }
        IIPRequest4SetSex request4StartCapture = IIPRequest4SetSex.Stub.asInterface(binder);
        try {
            request4StartCapture.setSex(var);
            request4StartCapture.setFileSource(fileSource);
            request4StartCapture.setFilePath(filePath);
        } catch (RemoteException e) {
            Log.e(TAG, "process set sex session request failed: " + e.getMessage());
        }
        process(request, true);
    }

    public void releaseBuffer() {
        Log.i(TAG, "release key frame.");
        IIPEvent4D3DKeyFrame iIPEvent4D3DKeyFrame = this.d3dKeyFrame;
        if (iIPEvent4D3DKeyFrame != null) {
            try {
                iIPEvent4D3DKeyFrame.release();
                Log.d(TAG, "release key frame success.");
            } catch (RemoteException e) {
                Log.e(TAG, "release keyframe failed! " + e.getMessage());
            }
        }
    }

    public void setPreview1SurfaceForSurfaceLess(SHSurface shSurface) {
        Log.i(TAG, "setPreview1SurfaceForSurfaceLess.");
        if (shSurface == null) {
            Log.e(TAG, "invalid input surface list.");
            return;
        }
        try {
            Log.i(TAG, "prcess surfaceless request begin.");
            IIPRequest request = createIpRequest("surfaceless");
            IBinder binder = getBinderFromRequest(request);
            if (binder == null) {
                Log.e(TAG, "getObject failed.");
                return;
            }
            IIPRequest4Surfaceless request4Algo = IIPRequest4Surfaceless.Stub.asInterface(binder);
            if (request4Algo == null) {
                Log.e(TAG, "IIPRequest4Surfaceless failed.");
                return;
            }
            request4Algo.setPreview1Surface(shSurface.surface);
            process(request, true);
        } catch (RemoteException e) {
            Log.e(TAG, "prcess surfaceless request failed.");
        }
    }

    public int processCommand(String commandType, String commandValue) {
        if (commandType == null || commandValue == null) {
            Log.e(TAG, "Invalid paramaters!");
            return -1;
        }
        Log.i(TAG, "process command: the type is " + commandType + ", the value is " + commandValue);
        return processCommandWithType(commandType, commandValue);
    }

    private int processCommandWithType(String commandType, String commandValue) {
        com.huawei.servicehost.pp.IIPRequest4CreatePipeline iIPRequest4CreatePipeline = this.ppRequest4CreatePipeline;
        if (iIPRequest4CreatePipeline == null) {
            Log.e(TAG, "The pp pipeline is null!");
            return -1;
        }
        try {
            iIPRequest4CreatePipeline.SetCommand(commandType, commandValue);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "process PP session request failed: " + e.getMessage());
            return -1;
        }
    }

    private void logDebug(String message) {
    }
}
