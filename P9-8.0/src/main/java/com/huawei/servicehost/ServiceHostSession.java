package com.huawei.servicehost;

import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import com.huawei.servicehost.IIPListener.Stub;
import com.huawei.servicehost.d3d.IIPEvent4D3DKeyFrame;
import com.huawei.servicehost.d3d.IIPEvent4D3DStatus;
import com.huawei.servicehost.d3d.IIPRequest4CreatePipeline;
import com.huawei.servicehost.d3d.IIPRequest4SetSex;
import com.huawei.servicehost.normal.IIPEvent4Metadata;
import com.huawei.servicehost.normal.IIPRequest4Metadata;
import com.huawei.servicehost.pp.IIPEvent4PPStatus;
import com.huawei.servicehost.pp.IIPEvent4Thumbnail;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ServiceHostSession {
    private static final /* synthetic */ int[] -com-huawei-servicehost-SurfaceTypeSwitchesValues = null;
    private static final boolean DEBUG = false;
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
    public static final String SESSION_D3D = "d3d";
    private static final String SESSION_EVENT_D3DKEYFRAME = "keyframe";
    private static final String SESSION_EVENT_METADATA = "metadata";
    private static final String SESSION_EVENT_RESULT = "result";
    private static final String SESSION_EVENT_STATUS = "status";
    private static final String SESSION_EVENT_THUMBNAIL = "thumbnail";
    public static final String SESSION_NORMAL = "normal";
    public static final String SESSION_PP = "pp";
    private static final String TAG = "ServiceHostSession";
    private CaptureStatusListener mCaptureStatusListener = null;
    private IIPEvent4D3DKeyFrame mD3dKeyFrame = null;
    private IIPRequest4CreatePipeline mIpD3dRequest4CreatePipeline = null;
    private IIPListener mIpListener = new Stub() {
        public void onIPCompleted(IIPRequest iipRequest) throws RemoteException {
            Log.v(ServiceHostSession.TAG, "onIPCompleted");
        }

        public void onIPEvent(IIPEvent iipEvent) throws RemoteException {
            if (iipEvent != null) {
                IBinder obj = iipEvent.getObject();
                String type = iipEvent.getType();
                if (type.equals("metadata")) {
                    if (ServiceHostSession.this.mMetadataListener != null) {
                        ServiceHostSession.this.mMetadataListener.onMetadataArrived(ServiceHostSession.this.serviceHostUtil.getTotalCaptureResult(IIPEvent4Metadata.Stub.asInterface(obj)));
                    }
                } else if (type.equals(ServiceHostSession.SESSION_EVENT_STATUS)) {
                    String name = iipEvent.getName();
                    if (ServiceHostSession.SESSION_PP.equals(name)) {
                        if (ServiceHostSession.this.mCaptureStatusListener != null) {
                            IIPEvent4PPStatus iipEvent4PPStatus = IIPEvent4PPStatus.Stub.asInterface(obj);
                            Log.i(ServiceHostSession.TAG, "status name: " + name + ", status: " + iipEvent4PPStatus.getStatus());
                            ServiceHostSession.this.mCaptureStatusListener.onCaptureStatusArrived(iipEvent4PPStatus.getStatus());
                        } else {
                            return;
                        }
                    }
                    if (ServiceHostSession.SESSION_D3D.equals(name) && ServiceHostSession.this.mStatusListener != null) {
                        ServiceHostSession.this.mStatusListener.onStatusArrived(IIPEvent4D3DStatus.Stub.asInterface(obj).getStatus());
                    }
                } else if (type.equals(ServiceHostSession.SESSION_EVENT_THUMBNAIL)) {
                    if (ServiceHostSession.this.mThumbnailListener != null) {
                        IIPEvent4Thumbnail iipEvent4Thumbnail = IIPEvent4Thumbnail.Stub.asInterface(obj);
                        String path = iipEvent4Thumbnail.getFilePath();
                        Log.d(ServiceHostSession.TAG, "thumbnail arrived, file path: " + path);
                        byte[] data = ServiceHostSession.this.extractQuickThumbnail(iipEvent4Thumbnail);
                        if (data.length == 0) {
                            Log.e(ServiceHostSession.TAG, "quick thumbnail is null!");
                        } else {
                            ServiceHostSession.this.setIpListener(null);
                            ServiceHostSession.this.mThumbnailListener.onThumbnailArrived(data, path);
                        }
                    }
                } else if (!type.equals(ServiceHostSession.SESSION_EVENT_RESULT)) {
                    if (!type.equals(ServiceHostSession.SESSION_EVENT_D3DKEYFRAME)) {
                        Log.d(ServiceHostSession.TAG, "IP event type: " + type);
                    } else if (ServiceHostSession.this.mKeyFrameListener != null) {
                        ServiceHostSession.this.mD3dKeyFrame = IIPEvent4D3DKeyFrame.Stub.asInterface(obj);
                        if (ServiceHostSession.this.mD3dKeyFrame == null) {
                            Log.i(ServiceHostSession.TAG, "key frame is invalid.");
                            return;
                        }
                        int count = ServiceHostSession.this.mD3dKeyFrame.getKeyFrameCount();
                        if (count <= 0) {
                            Log.i(ServiceHostSession.TAG, "invalid frame count: " + count);
                            return;
                        }
                        try {
                            Log.i(ServiceHostSession.TAG, "key frame count: " + count);
                            ArrayList<ImageWrap> keyFrameList = new ArrayList();
                            for (int i = 0; i < count; i++) {
                                ImageWrap keyFrameBuffer = ServiceHostSession.this.mD3dKeyFrame.getKeyFrame(i);
                                if (keyFrameBuffer == null) {
                                    Log.i(ServiceHostSession.TAG, "key frame buffer is invalid!");
                                } else {
                                    keyFrameList.add(keyFrameBuffer);
                                }
                            }
                            ServiceHostSession.this.mKeyFrameListener.onKeyFrameArrived(keyFrameList);
                        } catch (RemoteException e) {
                            Log.e(ServiceHostSession.TAG, "receive key frame failed." + e.getMessage());
                        }
                    }
                }
            }
        }
    };
    private com.huawei.servicehost.normal.IIPRequest4CreatePipeline mIpPreviewRequest4CreatePipeline = null;
    private com.huawei.servicehost.normal.IIPRequest4CreatePipeline mIpVideoRequest4CreatePipeline = null;
    private boolean mIsVideo = DEBUG;
    private KeyFrameListener mKeyFrameListener = null;
    private MetadataListener mMetadataListener = null;
    private com.huawei.servicehost.pp.IIPRequest4CreatePipeline mPpRequest4CreatePipeline = null;
    private IImageProcessSession mSession = null;
    private Object mSessionLock = new Object();
    private String mSessionType = SESSION_NORMAL;
    private StatusListener mStatusListener = null;
    private ThumbnailListener mThumbnailListener = null;
    private ServiceHostUtil serviceHostUtil = new ServiceHostUtil();

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

    private static /* synthetic */ int[] -getcom-huawei-servicehost-SurfaceTypeSwitchesValues() {
        if (-com-huawei-servicehost-SurfaceTypeSwitchesValues != null) {
            return -com-huawei-servicehost-SurfaceTypeSwitchesValues;
        }
        int[] iArr = new int[SurfaceType.values().length];
        try {
            iArr[SurfaceType.SURFACE_FOR_CAPTURE.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SurfaceType.SURFACE_FOR_D3DPREVIEW.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SurfaceType.SURFACE_FOR_DMAP.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SurfaceType.SURFACE_FOR_METADATA.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SurfaceType.SURFACE_FOR_PREVIEW.ordinal()] = 1;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SurfaceType.SURFACE_FOR_VIDEO.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        -com-huawei-servicehost-SurfaceTypeSwitchesValues = iArr;
        return iArr;
    }

    public ServiceHostSession(String type) {
        Log.i(TAG, "create session for: " + type);
        this.mSession = ImageProcessManager.get().createIPSession(type);
        if (this.mSession == null) {
            Log.e(TAG, "create session failed!");
        }
        setIpListener(this.mIpListener);
        this.mSessionType = type;
    }

    public void destroy() {
        Log.i(TAG, "destroy session for " + this.mSessionType);
        setIpListener(null);
        synchronized (this.mSessionLock) {
            if (this.mSession == null) {
                Log.i(TAG, "session for " + this.mSessionType + " has destroyed before!");
                return;
            }
            try {
                this.mSession.process(this.mSession.createIPRequest(REQUEST_TYPE_DESTROY_PIPELINE), true);
                this.mSession = null;
                Log.i(TAG, "destroy successfully for " + this.mSessionType);
            } catch (RemoteException e) {
                Log.e(TAG, "destroy session for " + this.mSessionType + " failed: " + e.getMessage());
            }
        }
    }

    public List<SHSurface> exchangeSurface(List<SHSurface> shSurfaces, String json, List<Size> captureSizes) {
        if (shSurfaces == null) {
            Log.e(TAG, "invalid input surface list.");
            return null;
        } else if (shSurfaces.size() == 0) {
            Log.e(TAG, "invalid input surface.");
            return null;
        } else if (SESSION_D3D.equals(this.mSessionType)) {
            return exchangeSurface4D3d(shSurfaces, json);
        } else {
            if (captureSizes == null) {
                Log.e(TAG, "invalid input capture list.");
                return null;
            } else if (captureSizes.size() == 0) {
                Log.e(TAG, "invalid input capture size.");
                return null;
            } else {
                Log.i(TAG, "exchange surfaces, size: " + shSurfaces.size());
                SHSurface shPreview1Surface = null;
                SHSurface shPreview2Surface = null;
                SHSurface shVideo1Surface = null;
                for (SHSurface shSurface : shSurfaces) {
                    if (!(shSurface == null || shSurface.size == null || shSurface.type == null)) {
                        Log.i(TAG, "input surface: " + shSurface.surface + ", type: " + shSurface.type + ", size: " + shSurface.size + ", camera id: " + shSurface.cameraId);
                        switch (-getcom-huawei-servicehost-SurfaceTypeSwitchesValues()[shSurface.type.ordinal()]) {
                            case 1:
                                if (shSurface.cameraId != 0) {
                                    shPreview2Surface = shSurface;
                                    break;
                                }
                                shPreview1Surface = shSurface;
                                break;
                            case 2:
                                shVideo1Surface = shSurface;
                                break;
                            default:
                                Log.e(TAG, "invalid surface type!");
                                break;
                        }
                    }
                }
                if (shPreview1Surface == null) {
                    Log.e(TAG, "invalid preview surface!");
                    return null;
                }
                if (shVideo1Surface != null) {
                    processIpSessionVideoRequest(shPreview1Surface, shVideo1Surface, (Size) captureSizes.get(0), json);
                } else {
                    List<SHSurface> shPreviewSurfaces = new ArrayList();
                    shPreviewSurfaces.add(shPreview1Surface);
                    shPreviewSurfaces.add(shPreview2Surface);
                    processIpSessionPreviewRequest(shPreviewSurfaces, captureSizes, json);
                }
                List<SHSurface> surfaces = new ArrayList();
                shPreview1Surface.surface = getPreview1Surface();
                surfaces.add(shPreview1Surface);
                if (shVideo1Surface != null) {
                    shVideo1Surface.surface = getVideo1Surface();
                    if (shVideo1Surface.surface != null) {
                        surfaces.add(shVideo1Surface);
                    }
                }
                SHSurface shMetadataSurface = new SHSurface();
                shMetadataSurface.surface = getMetadataSurface();
                if (shMetadataSurface.surface != null) {
                    shMetadataSurface.type = SurfaceType.SURFACE_FOR_METADATA;
                    shMetadataSurface.cameraId = shPreview1Surface.cameraId;
                    surfaces.add(shMetadataSurface);
                }
                SHSurface shCapture1Surface = new SHSurface();
                shCapture1Surface.surface = getCapture1Surface();
                if (shCapture1Surface.surface != null) {
                    shCapture1Surface.type = SurfaceType.SURFACE_FOR_CAPTURE;
                    shCapture1Surface.cameraId = shPreview1Surface.cameraId;
                    surfaces.add(shCapture1Surface);
                }
                if (shPreview2Surface != null) {
                    shPreview2Surface.surface = getPreview2Surface();
                    if (shPreview2Surface.surface != null) {
                        surfaces.add(shPreview2Surface);
                    }
                    SHSurface shCapture2Surface = new SHSurface();
                    shCapture2Surface.surface = getCapture2Surface();
                    if (shCapture2Surface.surface != null) {
                        shCapture2Surface.type = SurfaceType.SURFACE_FOR_CAPTURE;
                        shCapture2Surface.cameraId = shPreview2Surface.cameraId;
                        surfaces.add(shCapture2Surface);
                    }
                }
                Log.i(TAG, "exchange surface successfully, surfaces size: " + surfaces.size());
                return surfaces;
            }
        }
    }

    public void capture(ServiceHostSession previewSession, String filePath, String json) {
        Log.i(TAG, "capture with file path: " + filePath);
        processPpSessionCaptureRequest(previewSession, filePath, json);
    }

    public void setMetadataListener(MetadataListener listener) {
        Log.i(TAG, "set metadata listener: " + listener);
        this.mMetadataListener = listener;
    }

    public void setThumbnailListener(ThumbnailListener listener) {
        Log.i(TAG, "set thumbnail listener: " + listener);
        this.mThumbnailListener = listener;
    }

    public void setStatusListener(StatusListener listener) {
        Log.i(TAG, "set status listener: " + listener);
        this.mStatusListener = listener;
    }

    public void setCaptureStatusListener(CaptureStatusListener listener) {
        Log.i(TAG, "set capture status listener: " + listener);
        this.mCaptureStatusListener = listener;
    }

    public void setKeyFrameListener(KeyFrameListener listener) {
        Log.i(TAG, "set key frame listener: " + listener);
        this.mKeyFrameListener = listener;
    }

    public void sendRequest(List<ServiceHostMetadata> requests) {
        Log.i(TAG, "send request");
        if (!SESSION_D3D.equals(this.mSessionType)) {
            if (requests.size() <= 0) {
                Log.e(TAG, "invalid requests to servicehost.");
                return;
            }
            IIPRequest iipRequest4Metadata = createIPRequest("metadata");
            if (iipRequest4Metadata == null) {
                Log.e(TAG, "create IPRequest for metadata failed!");
                return;
            }
            try {
                IIPRequest4Metadata.Stub.asInterface(getIBinderFromRequest(iipRequest4Metadata)).setMetadata(((ServiceHostMetadata) requests.get(0)).getNativeMetadata());
                Log.d(TAG, "process metadata request.");
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
        IIPRequest ipRequest = createIPRequest(REQUEST_TYPE_CREATE_PIPELINE);
        IBinder binder = getIBinderFromRequest(ipRequest);
        if (binder == null) {
            Log.e(TAG, "IP request get binder failed!");
            return;
        }
        this.mIsVideo = DEBUG;
        SHSurface shPreview1Surface = (SHSurface) shPreviewSurfaces.get(0);
        Size capture1Size = (Size) captureSizes.get(0);
        SHSurface sHSurface = null;
        if (shPreviewSurfaces.size() > 1) {
            sHSurface = (SHSurface) shPreviewSurfaces.get(1);
        }
        Size size = null;
        if (captureSizes.size() > 1) {
            size = (Size) captureSizes.get(1);
        }
        this.mIpPreviewRequest4CreatePipeline = com.huawei.servicehost.normal.IIPRequest4CreatePipeline.Stub.asInterface(binder);
        try {
            int usage = BufferShareManager.get().getDefaultUsage();
            Log.i(TAG, "get default usage: " + Integer.toHexString(usage));
            ImageDescriptor preview1ImageDescriptor = new ImageDescriptor();
            preview1ImageDescriptor.setUsage(usage | ImageDescriptor.GRALLOC_USAGE_HW_COMPOSER);
            if (shPreview1Surface.size != null) {
                preview1ImageDescriptor.setWidth(shPreview1Surface.size.getWidth());
                preview1ImageDescriptor.setHeight(shPreview1Surface.size.getHeight());
                Log.d(TAG, "set preview1 size: " + shPreview1Surface.size.getWidth() + "x" + shPreview1Surface.size.getHeight());
            }
            this.mIpPreviewRequest4CreatePipeline.setPreviewFormat(preview1ImageDescriptor);
            this.mIpPreviewRequest4CreatePipeline.setPreview1Surface(shPreview1Surface.surface);
            ImageDescriptor capture1ImageDescriptor = new ImageDescriptor();
            capture1ImageDescriptor.setUsage(usage);
            capture1ImageDescriptor.setFormat(35);
            if (capture1Size != null) {
                capture1ImageDescriptor.setWidth(capture1Size.getWidth());
                capture1ImageDescriptor.setHeight(capture1Size.getHeight());
                Log.d(TAG, "set capture1 size: " + capture1Size.getWidth() + "x" + capture1Size.getHeight());
            }
            this.mIpPreviewRequest4CreatePipeline.setCaptureFormat(capture1ImageDescriptor);
            if (sHSurface != null) {
                ImageDescriptor preview2ImageDescriptor = new ImageDescriptor();
                preview2ImageDescriptor.setUsage(usage | ImageDescriptor.GRALLOC_USAGE_HW_COMPOSER);
                if (sHSurface.size != null) {
                    preview2ImageDescriptor.setWidth(sHSurface.size.getWidth());
                    preview2ImageDescriptor.setHeight(sHSurface.size.getHeight());
                    Log.d(TAG, "set preview2 size: " + sHSurface.size.getWidth() + "x" + sHSurface.size.getHeight());
                }
                this.mIpPreviewRequest4CreatePipeline.setPreviewFormatLine2(preview2ImageDescriptor);
                if (sHSurface.surface != null) {
                    this.mIpPreviewRequest4CreatePipeline.setPreview1Surface(sHSurface.surface);
                }
            }
            if (size != null) {
                ImageDescriptor capture2ImageDescriptor = new ImageDescriptor();
                capture2ImageDescriptor.setUsage(usage);
                capture2ImageDescriptor.setFormat(35);
                if (capture1Size != null) {
                    capture2ImageDescriptor.setWidth(capture1Size.getWidth());
                    capture2ImageDescriptor.setHeight(capture1Size.getHeight());
                    Log.d(TAG, "set capture2 size: " + capture1Size.getWidth() + "x" + capture1Size.getHeight());
                }
                this.mIpPreviewRequest4CreatePipeline.setCaptureFormatLine2(capture2ImageDescriptor);
            }
            this.mIpPreviewRequest4CreatePipeline.setLayout(json);
            process(ipRequest, true);
        } catch (RemoteException e) {
            Log.e(TAG, "process IP session resuest failed: " + e.getMessage());
        }
    }

    private void processPpSessionCaptureRequest(ServiceHostSession previewSession, String filePath, String json) {
        Log.i(TAG, "process PP session request");
        if (previewSession == null) {
            Log.e(TAG, "Invalid preview session!");
            return;
        }
        IIPRequest ipRequest = createIPRequest(REQUEST_TYPE_CREATE_PIPELINE);
        IBinder binder = getIBinderFromRequest(ipRequest);
        if (binder == null) {
            Log.e(TAG, "PP request get binder failed!");
            return;
        }
        if (this.mPpRequest4CreatePipeline == null) {
            this.mPpRequest4CreatePipeline = com.huawei.servicehost.pp.IIPRequest4CreatePipeline.Stub.asInterface(binder);
        }
        try {
            this.mPpRequest4CreatePipeline.setLayout(json);
            this.mPpRequest4CreatePipeline.setFilePath(filePath);
            this.mPpRequest4CreatePipeline.setForegroundSession(previewSession.asBinder());
            process(ipRequest, true);
        } catch (RemoteException e) {
            Log.e(TAG, "process PP session request failed: " + e.getMessage());
        }
    }

    private void processIpSessionVideoRequest(SHSurface shPreviewSurface, SHSurface shVideoSurface, Size captureSize, String json) {
        Log.i(TAG, "process IP video request()");
        IIPRequest ipRequest = createIPRequest(REQUEST_TYPE_CREATE_PIPELINE);
        IBinder binder = getIBinderFromRequest(ipRequest);
        if (binder == null) {
            Log.e(TAG, "IP request get binder failed!");
            return;
        }
        this.mIsVideo = true;
        if (this.mIpVideoRequest4CreatePipeline == null) {
            this.mIpVideoRequest4CreatePipeline = com.huawei.servicehost.normal.IIPRequest4CreatePipeline.Stub.asInterface(binder);
        }
        try {
            int usage = BufferShareManager.get().getDefaultUsage();
            Log.i(TAG, "get default usage: " + Integer.toHexString(usage));
            ImageDescriptor previewImageDescriptor = new ImageDescriptor();
            if (shPreviewSurface.size != null) {
                previewImageDescriptor.setWidth(shPreviewSurface.size.getWidth());
                previewImageDescriptor.setHeight(shPreviewSurface.size.getHeight());
                Log.d(TAG, "set video preview size: " + shPreviewSurface.size.getWidth() + "x" + shPreviewSurface.size.getHeight());
            }
            previewImageDescriptor.setUsage(usage);
            this.mIpVideoRequest4CreatePipeline.setPreviewFormat(previewImageDescriptor);
            ImageDescriptor captureImageDescriptor = new ImageDescriptor();
            if (captureSize != null) {
                captureImageDescriptor.setWidth(captureSize.getWidth());
                captureImageDescriptor.setHeight(captureSize.getHeight());
            }
            captureImageDescriptor.setUsage(usage);
            captureImageDescriptor.setFormat(35);
            this.mIpVideoRequest4CreatePipeline.setCaptureFormat(captureImageDescriptor);
            if (captureSize != null) {
                Log.d(TAG, "set video capture size: " + captureSize.getWidth() + "x" + captureSize.getHeight());
            }
            ImageDescriptor videoImageDescriptor = new ImageDescriptor();
            if (shVideoSurface.size != null) {
                videoImageDescriptor.setWidth(shVideoSurface.size.getWidth());
                videoImageDescriptor.setHeight(shVideoSurface.size.getHeight());
                Log.d(TAG, "set video size: " + shVideoSurface.size.getWidth() + "x" + shVideoSurface.size.getHeight());
            }
            videoImageDescriptor.setUsage(usage);
            this.mIpVideoRequest4CreatePipeline.setVideoFormat(videoImageDescriptor);
            this.mIpVideoRequest4CreatePipeline.setLayout(json);
            this.mIpVideoRequest4CreatePipeline.setPreview1Surface(shPreviewSurface.surface);
            this.mIpVideoRequest4CreatePipeline.setCamera1VideoSurface(shVideoSurface.surface);
            process(ipRequest, true);
        } catch (RemoteException e) {
            Log.e(TAG, "process ip video resuest failed:" + e.getMessage());
        }
    }

    private IIPRequest createIPRequest(String type) {
        Log.i(TAG, "create IP request, type: " + type);
        synchronized (this.mSessionLock) {
            if (this.mSession == null) {
                Log.e(TAG, "session is null!");
                return null;
            }
            try {
                IIPRequest request = this.mSession.createIPRequest(type);
                Log.i(TAG, "create IP request end.");
                return request;
            } catch (RemoteException e) {
                Log.e(TAG, "create " + type + " IP request failed: " + e.getMessage());
                return null;
            } catch (NullPointerException e2) {
                Log.e(TAG, "session is null !");
                return null;
            }
        }
    }

    private IBinder getIBinderFromRequest(IIPRequest request) {
        Log.i(TAG, "get binder from request.");
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

    private void process(IIPRequest request, boolean isSync) {
        Log.i(TAG, "process request, sync: " + isSync);
        synchronized (this.mSessionLock) {
            if (this.mSession == null) {
                Log.e(TAG, "session is null!");
                return;
            }
            try {
                this.mSession.process(request, isSync);
            } catch (RemoteException e) {
                Log.e(TAG, "process request fail: " + e.getMessage());
            } catch (NullPointerException e2) {
                Log.e(TAG, "session is null !");
            }
        }
        Log.i(TAG, "process request end.");
        return;
    }

    private IBinder asBinder() {
        synchronized (this.mSessionLock) {
            if (this.mSession == null) {
                Log.e(TAG, "session is null!");
                return null;
            }
            IBinder binder = this.mSession.asBinder();
            return binder;
        }
    }

    private Surface getPreview1Surface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.mIpPreviewRequest4CreatePipeline;
        if (this.mIsVideo) {
            pipeline = this.mIpVideoRequest4CreatePipeline;
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

    private Surface getPreview2Surface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.mIpPreviewRequest4CreatePipeline;
        if (this.mIsVideo) {
            pipeline = this.mIpVideoRequest4CreatePipeline;
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
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.mIpPreviewRequest4CreatePipeline;
        if (this.mIsVideo) {
            pipeline = this.mIpVideoRequest4CreatePipeline;
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

    private Surface getCapture1Surface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.mIpPreviewRequest4CreatePipeline;
        if (this.mIsVideo) {
            pipeline = this.mIpVideoRequest4CreatePipeline;
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

    private Surface getCapture2Surface() {
        Surface surface = null;
        com.huawei.servicehost.normal.IIPRequest4CreatePipeline pipeline = this.mIpPreviewRequest4CreatePipeline;
        if (this.mIsVideo) {
            pipeline = this.mIpVideoRequest4CreatePipeline;
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

    private Surface getVideo1Surface() {
        Surface surface = null;
        try {
            surface = this.mIpVideoRequest4CreatePipeline.getCamera1VideoSurface();
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
        synchronized (this.mSessionLock) {
            if (this.mSession == null) {
                Log.e(TAG, "session is null!");
                return;
            }
            try {
                this.mSession.setIPListener(listener);
            } catch (RemoteException e) {
                Log.e(TAG, "create IP listner failed: " + e.getMessage());
            }
        }
        return;
    }

    private byte[] extractQuickThumbnail(IIPEvent4Thumbnail iipEvent4Thumbnail) {
        Log.i(TAG, "extract quick thumbnail.");
        try {
            ByteBuffer dataBuffer = iipEvent4Thumbnail.getImage().getData();
            int width = (dataBuffer.get(8) & 255) + ((dataBuffer.get(9) & 255) << 8);
            int height = (dataBuffer.get(12) & 255) + ((dataBuffer.get(13) & 255) << 8);
            Log.i(TAG, "width: " + width + ", heigth: " + height);
            int quickThumbSize = ((width * height) * 4) + 16;
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
            return DEBUG;
        }
        if (surface.isValid()) {
            return true;
        }
        Log.e(TAG, "get invalid surface!");
        return DEBUG;
    }

    private List<SHSurface> exchangeSurface4D3d(List<SHSurface> shSurfaces, String json) {
        Log.i(TAG, "exchange surface for d3d.");
        SHSurface shPreviewSurface = (SHSurface) shSurfaces.get(0);
        if (shPreviewSurface == null) {
            Log.e(TAG, "invalid preview surface!");
            return null;
        }
        int slaveCameraId = 2;
        if (shSurfaces.size() > 1) {
            slaveCameraId = ((SHSurface) shSurfaces.get(1)).cameraId;
        }
        processIpSessionD3dRequest(shPreviewSurface, json);
        List<SHSurface> surfaces = new ArrayList();
        try {
            SHSurface shPreview1Surface = new SHSurface();
            shPreview1Surface.surface = this.mIpD3dRequest4CreatePipeline.getCamera1Surface();
            shPreview1Surface.type = SurfaceType.SURFACE_FOR_D3DPREVIEW;
            shPreview1Surface.cameraId = shPreviewSurface.cameraId;
            surfaces.add(shPreview1Surface);
            SHSurface shPreview2Surface = new SHSurface();
            shPreview2Surface.surface = this.mIpD3dRequest4CreatePipeline.getCamera2Surface();
            shPreview2Surface.type = SurfaceType.SURFACE_FOR_D3DPREVIEW;
            shPreview2Surface.cameraId = slaveCameraId;
            surfaces.add(shPreview2Surface);
            SHSurface shMetadataSurface = new SHSurface();
            shMetadataSurface.surface = this.mIpD3dRequest4CreatePipeline.getMetadataSurface();
            shMetadataSurface.type = SurfaceType.SURFACE_FOR_METADATA;
            shMetadataSurface.cameraId = shPreviewSurface.cameraId;
            surfaces.add(shMetadataSurface);
            SHSurface shDmapSurface = new SHSurface();
            shDmapSurface.surface = this.mIpD3dRequest4CreatePipeline.getDmapSurface();
            shDmapSurface.type = SurfaceType.SURFACE_FOR_DMAP;
            shDmapSurface.cameraId = shPreviewSurface.cameraId;
            surfaces.add(shDmapSurface);
        } catch (RemoteException e) {
            Log.e(TAG, "process set sex session resuest failed: " + e.getMessage());
        }
        Log.i(TAG, "d3d exchange surface successfully, surfaces size: " + surfaces.size());
        return surfaces;
    }

    private void processIpSessionD3dRequest(SHSurface shPreviewSurface, String json) {
        Log.i(TAG, "process IP session request for d3d.");
        IIPRequest ipRequest = createIPRequest(REQUEST_TYPE_CREATE_PIPELINE);
        IBinder binder = getIBinderFromRequest(ipRequest);
        if (binder == null) {
            Log.e(TAG, "IP request for d3d get binder failed!");
            return;
        }
        this.mIpD3dRequest4CreatePipeline = IIPRequest4CreatePipeline.Stub.asInterface(binder);
        try {
            this.mIpD3dRequest4CreatePipeline.setPreview1Surface(shPreviewSurface.surface);
            this.mIpD3dRequest4CreatePipeline.setLayout(json);
            process(ipRequest, true);
        } catch (RemoteException e) {
            Log.e(TAG, "process d3d IP session resuest failed: " + e.getMessage());
        }
    }

    public void startPhotoing() {
        Log.i(TAG, "start capture for d3d.");
        process(createIPRequest(REQUEST_TYPE_D3D_START_CAPTURE), true);
    }

    public void canclePhotoing() {
        Log.i(TAG, "cancel capture for d3d.");
        process(createIPRequest(REQUEST_TYPE_D3D_CANCEL_CAPTURE), true);
    }

    public void startFace3D() {
        Log.i(TAG, "start face 3d for d3d.");
        process(createIPRequest(REQUEST_TYPE_D3D_START_FACE3D), true);
    }

    public void setSex(int var, String filePath, int fileSource) {
        Log.i(TAG, "set sex, var: " + var + ", file path: " + filePath + ", file source: " + fileSource);
        IIPRequest request = createIPRequest(REQUEST_TYPE_D3D_SET_SEX);
        IBinder binder = getIBinderFromRequest(request);
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
            Log.e(TAG, "process set sex session resuest failed: " + e.getMessage());
        }
        process(request, true);
    }

    public void releaseBuffer() {
        Log.i(TAG, "release key frame.");
        if (this.mD3dKeyFrame != null) {
            try {
                this.mD3dKeyFrame.release();
                Log.d(TAG, "release key frame success.");
            } catch (RemoteException e) {
                Log.e(TAG, "release keyframe failed! " + e.getMessage());
            }
        }
    }
}
