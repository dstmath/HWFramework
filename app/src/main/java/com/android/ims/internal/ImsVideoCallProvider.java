package com.android.ims.internal;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.VideoProfile;
import android.telecom.VideoProfile.CameraCapabilities;
import android.view.Surface;
import com.android.ims.internal.IImsVideoCallProvider.Stub;
import com.android.internal.os.SomeArgs;

public abstract class ImsVideoCallProvider {
    private static final int MSG_REQUEST_CALL_DATA_USAGE = 10;
    private static final int MSG_REQUEST_CAMERA_CAPABILITIES = 9;
    private static final int MSG_SEND_SESSION_MODIFY_REQUEST = 7;
    private static final int MSG_SEND_SESSION_MODIFY_RESPONSE = 8;
    private static final int MSG_SET_CALLBACK = 1;
    private static final int MSG_SET_CAMERA = 2;
    private static final int MSG_SET_DEVICE_ORIENTATION = 5;
    private static final int MSG_SET_DISPLAY_SURFACE = 4;
    private static final int MSG_SET_PAUSE_IMAGE = 11;
    private static final int MSG_SET_PREVIEW_SURFACE = 3;
    private static final int MSG_SET_ZOOM = 6;
    private final ImsVideoCallProviderBinder mBinder;
    private IImsVideoCallCallback mCallback;
    private final Handler mProviderHandler;

    /* renamed from: com.android.ims.internal.ImsVideoCallProvider.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ImsVideoCallProvider.MSG_SET_CALLBACK /*1*/:
                    ImsVideoCallProvider.this.mCallback = (IImsVideoCallCallback) msg.obj;
                case ImsVideoCallProvider.MSG_SET_CAMERA /*2*/:
                    ImsVideoCallProvider.this.onSetCamera((String) msg.obj);
                case ImsVideoCallProvider.MSG_SET_PREVIEW_SURFACE /*3*/:
                    ImsVideoCallProvider.this.onSetPreviewSurface((Surface) msg.obj);
                case ImsVideoCallProvider.MSG_SET_DISPLAY_SURFACE /*4*/:
                    ImsVideoCallProvider.this.onSetDisplaySurface((Surface) msg.obj);
                case ImsVideoCallProvider.MSG_SET_DEVICE_ORIENTATION /*5*/:
                    ImsVideoCallProvider.this.onSetDeviceOrientation(msg.arg1);
                case ImsVideoCallProvider.MSG_SET_ZOOM /*6*/:
                    ImsVideoCallProvider.this.onSetZoom(((Float) msg.obj).floatValue());
                case ImsVideoCallProvider.MSG_SEND_SESSION_MODIFY_REQUEST /*7*/:
                    SomeArgs args = msg.obj;
                    try {
                        ImsVideoCallProvider.this.onSendSessionModifyRequest(args.arg1, args.arg2);
                    } finally {
                        args.recycle();
                    }
                case ImsVideoCallProvider.MSG_SEND_SESSION_MODIFY_RESPONSE /*8*/:
                    ImsVideoCallProvider.this.onSendSessionModifyResponse((VideoProfile) msg.obj);
                case ImsVideoCallProvider.MSG_REQUEST_CAMERA_CAPABILITIES /*9*/:
                    ImsVideoCallProvider.this.onRequestCameraCapabilities();
                case ImsVideoCallProvider.MSG_REQUEST_CALL_DATA_USAGE /*10*/:
                    ImsVideoCallProvider.this.onRequestCallDataUsage();
                case ImsVideoCallProvider.MSG_SET_PAUSE_IMAGE /*11*/:
                    ImsVideoCallProvider.this.onSetPauseImage((Uri) msg.obj);
                default:
            }
        }
    }

    private final class ImsVideoCallProviderBinder extends Stub {
        private ImsVideoCallProviderBinder() {
        }

        public void setCallback(IImsVideoCallCallback callback) {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SET_CALLBACK, callback).sendToTarget();
        }

        public void setCamera(String cameraId) {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SET_CAMERA, cameraId).sendToTarget();
        }

        public void setPreviewSurface(Surface surface) {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SET_PREVIEW_SURFACE, surface).sendToTarget();
        }

        public void setDisplaySurface(Surface surface) {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SET_DISPLAY_SURFACE, surface).sendToTarget();
        }

        public void setDeviceOrientation(int rotation) {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SET_DEVICE_ORIENTATION, rotation, 0).sendToTarget();
        }

        public void setZoom(float value) {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SET_ZOOM, Float.valueOf(value)).sendToTarget();
        }

        public void sendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fromProfile;
            args.arg2 = toProfile;
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SEND_SESSION_MODIFY_REQUEST, args).sendToTarget();
        }

        public void sendSessionModifyResponse(VideoProfile responseProfile) {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SEND_SESSION_MODIFY_RESPONSE, responseProfile).sendToTarget();
        }

        public void requestCameraCapabilities() {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_REQUEST_CAMERA_CAPABILITIES).sendToTarget();
        }

        public void requestCallDataUsage() {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_REQUEST_CALL_DATA_USAGE).sendToTarget();
        }

        public void setPauseImage(Uri uri) {
            ImsVideoCallProvider.this.mProviderHandler.obtainMessage(ImsVideoCallProvider.MSG_SET_PAUSE_IMAGE, uri).sendToTarget();
        }
    }

    public abstract void onRequestCallDataUsage();

    public abstract void onRequestCameraCapabilities();

    public abstract void onSendSessionModifyRequest(VideoProfile videoProfile, VideoProfile videoProfile2);

    public abstract void onSendSessionModifyResponse(VideoProfile videoProfile);

    public abstract void onSetCamera(String str);

    public abstract void onSetDeviceOrientation(int i);

    public abstract void onSetDisplaySurface(Surface surface);

    public abstract void onSetPauseImage(Uri uri);

    public abstract void onSetPreviewSurface(Surface surface);

    public abstract void onSetZoom(float f);

    public ImsVideoCallProvider() {
        this.mProviderHandler = new AnonymousClass1(Looper.getMainLooper());
        this.mBinder = new ImsVideoCallProviderBinder();
    }

    public final IImsVideoCallProvider getInterface() {
        return this.mBinder;
    }

    public void receiveSessionModifyRequest(VideoProfile VideoProfile) {
        if (this.mCallback != null) {
            try {
                this.mCallback.receiveSessionModifyRequest(VideoProfile);
            } catch (RemoteException e) {
            }
        }
    }

    public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
        if (this.mCallback != null) {
            try {
                this.mCallback.receiveSessionModifyResponse(status, requestedProfile, responseProfile);
            } catch (RemoteException e) {
            }
        }
    }

    public void handleCallSessionEvent(int event) {
        if (this.mCallback != null) {
            try {
                this.mCallback.handleCallSessionEvent(event);
            } catch (RemoteException e) {
            }
        }
    }

    public void changePeerDimensions(int width, int height) {
        if (this.mCallback != null) {
            try {
                this.mCallback.changePeerDimensions(width, height);
            } catch (RemoteException e) {
            }
        }
    }

    public void changeCallDataUsage(long dataUsage) {
        if (this.mCallback != null) {
            try {
                this.mCallback.changeCallDataUsage(dataUsage);
            } catch (RemoteException e) {
            }
        }
    }

    public void changeCameraCapabilities(CameraCapabilities CameraCapabilities) {
        if (this.mCallback != null) {
            try {
                this.mCallback.changeCameraCapabilities(CameraCapabilities);
            } catch (RemoteException e) {
            }
        }
    }

    public void changeVideoQuality(int videoQuality) {
        if (this.mCallback != null) {
            try {
                this.mCallback.changeVideoQuality(videoQuality);
            } catch (RemoteException e) {
            }
        }
    }
}
