package android.telecom;

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
    /* access modifiers changed from: private */
    public InCallService.VideoCall.Callback mCallback;
    private final String mCallingPackageName;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        public void binderDied() {
            VideoCallImpl.this.mVideoProvider.asBinder().unlinkToDeath(this, 0);
        }
    };
    /* access modifiers changed from: private */
    public Handler mHandler;
    private int mTargetSdkVersion;
    /* access modifiers changed from: private */
    public final IVideoProvider mVideoProvider;
    /* access modifiers changed from: private */
    public int mVideoQuality = 0;
    private int mVideoState = 0;

    private final class MessageHandler extends Handler {
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

        public void handleMessage(Message msg) {
            if (VideoCallImpl.this.mCallback != null) {
                switch (msg.what) {
                    case 1:
                        VideoCallImpl.this.mCallback.onSessionModifyRequestReceived((VideoProfile) msg.obj);
                        break;
                    case 2:
                        SomeArgs args = (SomeArgs) msg.obj;
                        try {
                            VideoCallImpl.this.mCallback.onSessionModifyResponseReceived(((Integer) args.arg1).intValue(), (VideoProfile) args.arg2, (VideoProfile) args.arg3);
                            break;
                        } finally {
                            args.recycle();
                        }
                    case 3:
                        VideoCallImpl.this.mCallback.onCallSessionEvent(((Integer) msg.obj).intValue());
                        break;
                    case 4:
                        SomeArgs args2 = (SomeArgs) msg.obj;
                        try {
                            VideoCallImpl.this.mCallback.onPeerDimensionsChanged(((Integer) args2.arg1).intValue(), ((Integer) args2.arg2).intValue());
                            break;
                        } finally {
                            args2.recycle();
                        }
                    case 5:
                        VideoCallImpl.this.mCallback.onCallDataUsageChanged(((Long) msg.obj).longValue());
                        break;
                    case 6:
                        VideoCallImpl.this.mCallback.onCameraCapabilitiesChanged((VideoProfile.CameraCapabilities) msg.obj);
                        break;
                    case 7:
                        int unused = VideoCallImpl.this.mVideoQuality = msg.arg1;
                        VideoCallImpl.this.mCallback.onVideoQualityChanged(msg.arg1);
                        break;
                }
            }
        }
    }

    private final class VideoCallListenerBinder extends IVideoCallback.Stub {
        private VideoCallListenerBinder() {
        }

        public void receiveSessionModifyRequest(VideoProfile videoProfile) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(1, videoProfile).sendToTarget();
            }
        }

        public void receiveSessionModifyResponse(int status, VideoProfile requestProfile, VideoProfile responseProfile) {
            if (VideoCallImpl.this.mHandler != null) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = Integer.valueOf(status);
                args.arg2 = requestProfile;
                args.arg3 = responseProfile;
                VideoCallImpl.this.mHandler.obtainMessage(2, args).sendToTarget();
            }
        }

        public void handleCallSessionEvent(int event) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(3, Integer.valueOf(event)).sendToTarget();
            }
        }

        public void changePeerDimensions(int width, int height) {
            if (VideoCallImpl.this.mHandler != null) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = Integer.valueOf(width);
                args.arg2 = Integer.valueOf(height);
                VideoCallImpl.this.mHandler.obtainMessage(4, args).sendToTarget();
            }
        }

        public void changeVideoQuality(int videoQuality) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(7, videoQuality, 0).sendToTarget();
            }
        }

        public void changeCallDataUsage(long dataUsage) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(5, Long.valueOf(dataUsage)).sendToTarget();
            }
        }

        public void changeCameraCapabilities(VideoProfile.CameraCapabilities cameraCapabilities) {
            if (VideoCallImpl.this.mHandler != null) {
                VideoCallImpl.this.mHandler.obtainMessage(6, cameraCapabilities).sendToTarget();
            }
        }
    }

    /* JADX WARNING: type inference failed for: r1v4, types: [android.telecom.VideoCallImpl$VideoCallListenerBinder, android.os.IBinder] */
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

    public void destroy() {
        unregisterCallback(this.mCallback);
    }

    public void registerCallback(InCallService.VideoCall.Callback callback) {
        registerCallback(callback, null);
    }

    public void registerCallback(InCallService.VideoCall.Callback callback, Handler handler) {
        this.mCallback = callback;
        if (handler == null) {
            this.mHandler = new MessageHandler(Looper.getMainLooper());
        } else {
            this.mHandler = new MessageHandler(handler.getLooper());
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.telecom.VideoCallImpl$VideoCallListenerBinder, android.os.IBinder] */
    public void unregisterCallback(InCallService.VideoCall.Callback callback) {
        if (callback == this.mCallback) {
            this.mCallback = null;
            try {
                this.mVideoProvider.removeVideoCallback(this.mBinder);
            } catch (RemoteException e) {
            }
        }
    }

    public void setCamera(String cameraId) {
        try {
            Log.w((Object) this, "setCamera: cameraId=%s, calling=%s", cameraId, this.mCallingPackageName);
            this.mVideoProvider.setCamera(cameraId, this.mCallingPackageName, this.mTargetSdkVersion);
        } catch (RemoteException e) {
        }
    }

    public void setPreviewSurface(Surface surface) {
        try {
            this.mVideoProvider.setPreviewSurface(surface);
        } catch (RemoteException e) {
        }
    }

    public void setDisplaySurface(Surface surface) {
        try {
            this.mVideoProvider.setDisplaySurface(surface);
        } catch (RemoteException e) {
        }
    }

    public void setDeviceOrientation(int rotation) {
        try {
            this.mVideoProvider.setDeviceOrientation(rotation);
        } catch (RemoteException e) {
        }
    }

    public void setZoom(float value) {
        try {
            this.mVideoProvider.setZoom(value);
        } catch (RemoteException e) {
        }
    }

    public void sendSessionModifyRequest(VideoProfile requestProfile) {
        try {
            this.mVideoProvider.sendSessionModifyRequest(new VideoProfile(this.mVideoState, this.mVideoQuality), requestProfile);
        } catch (RemoteException e) {
        }
    }

    public void sendSessionModifyResponse(VideoProfile responseProfile) {
        try {
            this.mVideoProvider.sendSessionModifyResponse(responseProfile);
        } catch (RemoteException e) {
        }
    }

    public void requestCameraCapabilities() {
        try {
            this.mVideoProvider.requestCameraCapabilities();
        } catch (RemoteException e) {
        }
    }

    public void requestCallDataUsage() {
        try {
            this.mVideoProvider.requestCallDataUsage();
        } catch (RemoteException e) {
        }
    }

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
