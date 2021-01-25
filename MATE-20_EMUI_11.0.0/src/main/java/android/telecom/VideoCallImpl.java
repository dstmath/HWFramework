package android.telecom;

import android.annotation.UnsupportedAppUsage;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.InCallService;
import android.telecom.VideoProfile;
import android.view.Surface;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IVideoCallback;
import com.android.internal.telecom.IVideoProvider;

public class VideoCallImpl extends InCallService.VideoCall {
    private final VideoCallListenerBinder mBinder;
    private InCallService.VideoCall.Callback mCallback;
    private final String mCallingPackageName;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class android.telecom.VideoCallImpl.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            VideoCallImpl.this.mVideoProvider.asBinder().unlinkToDeath(this, 0);
        }
    };
    private Handler mHandler;
    private int mTargetSdkVersion;
    private final IVideoProvider mVideoProvider;
    private int mVideoQuality = 0;
    private int mVideoState = 0;

    /* access modifiers changed from: private */
    public final class VideoCallListenerBinder extends IVideoCallback.Stub {
        private VideoCallListenerBinder() {
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void receiveSessionModifyRequest(VideoProfile videoProfile) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(1, videoProfile).sendToTarget();
            }
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void receiveSessionModifyResponse(int status, VideoProfile requestProfile, VideoProfile responseProfile) {
            if (VideoCallImpl.this.mHandler != null) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = Integer.valueOf(status);
                args.arg2 = requestProfile;
                args.arg3 = responseProfile;
                VideoCallImpl.this.mHandler.obtainMessage(2, args).sendToTarget();
            }
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void handleCallSessionEvent(int event) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(3, Integer.valueOf(event)).sendToTarget();
            }
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void changePeerDimensions(int width, int height) {
            if (VideoCallImpl.this.mHandler != null) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = Integer.valueOf(width);
                args.arg2 = Integer.valueOf(height);
                VideoCallImpl.this.mHandler.obtainMessage(4, args).sendToTarget();
            }
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void changeVideoQuality(int videoQuality) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(7, videoQuality, 0).sendToTarget();
            }
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void changeCallDataUsage(long dataUsage) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(5, Long.valueOf(dataUsage)).sendToTarget();
            }
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void changeCameraCapabilities(VideoProfile.CameraCapabilities cameraCapabilities) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(6, cameraCapabilities).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class MessageHandler extends Handler {
        private static final int MSG_CHANGE_CALL_DATA_USAGE = 5;
        private static final int MSG_CHANGE_CAMERA_CAPABILITIES = 6;
        private static final int MSG_CHANGE_PEER_DIMENSIONS = 4;
        private static final int MSG_CHANGE_VIDEO_QUALITY = 7;
        private static final int MSG_HANDLE_CALL_SESSION_EVENT = 3;
        private static final int MSG_RECEIVE_SESSION_MODIFY_REQUEST = 1;
        private static final int MSG_RECEIVE_SESSION_MODIFY_RESPONSE = 2;

        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (VideoCallImpl.this.mCallback != null) {
                switch (msg.what) {
                    case 1:
                        VideoCallImpl.this.mCallback.onSessionModifyRequestReceived((VideoProfile) msg.obj);
                        return;
                    case 2:
                        SomeArgs args = (SomeArgs) msg.obj;
                        try {
                            VideoCallImpl.this.mCallback.onSessionModifyResponseReceived(((Integer) args.arg1).intValue(), (VideoProfile) args.arg2, (VideoProfile) args.arg3);
                            return;
                        } finally {
                            args.recycle();
                        }
                    case 3:
                        VideoCallImpl.this.mCallback.onCallSessionEvent(((Integer) msg.obj).intValue());
                        return;
                    case 4:
                        SomeArgs args2 = (SomeArgs) msg.obj;
                        try {
                            VideoCallImpl.this.mCallback.onPeerDimensionsChanged(((Integer) args2.arg1).intValue(), ((Integer) args2.arg2).intValue());
                            return;
                        } finally {
                            args2.recycle();
                        }
                    case 5:
                        VideoCallImpl.this.mCallback.onCallDataUsageChanged(((Long) msg.obj).longValue());
                        return;
                    case 6:
                        VideoCallImpl.this.mCallback.onCameraCapabilitiesChanged((VideoProfile.CameraCapabilities) msg.obj);
                        return;
                    case 7:
                        VideoCallImpl.this.mVideoQuality = msg.arg1;
                        VideoCallImpl.this.mCallback.onVideoQualityChanged(msg.arg1);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    VideoCallImpl(IVideoProvider videoProvider, String callingPackageName, int targetSdkVersion) throws RemoteException {
        this.mVideoProvider = videoProvider;
        this.mVideoProvider.asBinder().linkToDeath(this.mDeathRecipient, 0);
        this.mBinder = new VideoCallListenerBinder();
        this.mVideoProvider.addVideoCallback(this.mBinder);
        this.mCallingPackageName = callingPackageName;
        setTargetSdkVersion(targetSdkVersion);
    }

    @VisibleForTesting
    public void setTargetSdkVersion(int sdkVersion) {
        this.mTargetSdkVersion = sdkVersion;
    }

    @Override // android.telecom.InCallService.VideoCall
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 127403196)
    public void destroy() {
        unregisterCallback(this.mCallback);
    }

    @Override // android.telecom.InCallService.VideoCall
    public void registerCallback(InCallService.VideoCall.Callback callback) {
        registerCallback(callback, null);
    }

    @Override // android.telecom.InCallService.VideoCall
    public void registerCallback(InCallService.VideoCall.Callback callback, Handler handler) {
        this.mCallback = callback;
        if (handler == null) {
            this.mHandler = new MessageHandler(Looper.getMainLooper());
        } else {
            this.mHandler = new MessageHandler(handler.getLooper());
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void unregisterCallback(InCallService.VideoCall.Callback callback) {
        if (callback == this.mCallback) {
            this.mCallback = null;
            try {
                this.mVideoProvider.removeVideoCallback(this.mBinder);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setCamera(String cameraId) {
        try {
            Log.w(this, "setCamera: cameraId=%s, calling=%s", cameraId, this.mCallingPackageName);
            this.mVideoProvider.setCamera(cameraId, this.mCallingPackageName, this.mTargetSdkVersion);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setPreviewSurface(Surface surface) {
        try {
            this.mVideoProvider.setPreviewSurface(surface);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setDisplaySurface(Surface surface) {
        try {
            this.mVideoProvider.setDisplaySurface(surface);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setDeviceOrientation(int rotation) {
        try {
            this.mVideoProvider.setDeviceOrientation(rotation);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setZoom(float value) {
        try {
            this.mVideoProvider.setZoom(value);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void sendSessionModifyRequest(VideoProfile requestProfile) {
        try {
            this.mVideoProvider.sendSessionModifyRequest(new VideoProfile(this.mVideoState, this.mVideoQuality), requestProfile);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void sendSessionModifyResponse(VideoProfile responseProfile) {
        try {
            this.mVideoProvider.sendSessionModifyResponse(responseProfile);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void requestCameraCapabilities() {
        try {
            this.mVideoProvider.requestCameraCapabilities();
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void requestCallDataUsage() {
        try {
            this.mVideoProvider.requestCallDataUsage();
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setPauseImage(Uri uri) {
        try {
            this.mVideoProvider.setPauseImage(uri);
        } catch (RemoteException e) {
        }
    }

    public void setVideoState(int videoState) {
        this.mVideoState = videoState;
    }
}
