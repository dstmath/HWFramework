package com.android.ims.internal;

import android.net.Uri;
import android.os.Handler;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.Connection.VideoProvider;
import android.telecom.VideoProfile;
import android.telecom.VideoProfile.CameraCapabilities;
import android.view.Surface;
import com.android.ims.internal.IImsVideoCallCallback.Stub;
import com.android.internal.os.SomeArgs;

public class ImsVideoCallProviderWrapper extends VideoProvider {
    private static final int MSG_CHANGE_CALL_DATA_USAGE = 5;
    private static final int MSG_CHANGE_CAMERA_CAPABILITIES = 6;
    private static final int MSG_CHANGE_PEER_DIMENSIONS = 4;
    private static final int MSG_CHANGE_VIDEO_QUALITY = 7;
    private static final int MSG_HANDLE_CALL_SESSION_EVENT = 3;
    private static final int MSG_RECEIVE_SESSION_MODIFY_REQUEST = 1;
    private static final int MSG_RECEIVE_SESSION_MODIFY_RESPONSE = 2;
    private final ImsVideoCallCallback mBinder;
    private DeathRecipient mDeathRecipient;
    private final Handler mHandler;
    private final IImsVideoCallProvider mVideoCallProvider;

    /* renamed from: com.android.ims.internal.ImsVideoCallProviderWrapper.2 */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            SomeArgs args;
            switch (msg.what) {
                case ImsVideoCallProviderWrapper.MSG_RECEIVE_SESSION_MODIFY_REQUEST /*1*/:
                    ImsVideoCallProviderWrapper.this.receiveSessionModifyRequest((VideoProfile) msg.obj);
                case ImsVideoCallProviderWrapper.MSG_RECEIVE_SESSION_MODIFY_RESPONSE /*2*/:
                    args = msg.obj;
                    try {
                        ImsVideoCallProviderWrapper.this.receiveSessionModifyResponse(((Integer) args.arg1).intValue(), args.arg2, args.arg3);
                    } finally {
                        args.recycle();
                    }
                case ImsVideoCallProviderWrapper.MSG_HANDLE_CALL_SESSION_EVENT /*3*/:
                    ImsVideoCallProviderWrapper.this.handleCallSessionEvent(((Integer) msg.obj).intValue());
                case ImsVideoCallProviderWrapper.MSG_CHANGE_PEER_DIMENSIONS /*4*/:
                    args = (SomeArgs) msg.obj;
                    try {
                        ImsVideoCallProviderWrapper.this.changePeerDimensions(((Integer) args.arg1).intValue(), ((Integer) args.arg2).intValue());
                    } finally {
                        args.recycle();
                    }
                case ImsVideoCallProviderWrapper.MSG_CHANGE_CALL_DATA_USAGE /*5*/:
                    ImsVideoCallProviderWrapper.this.changeCallDataUsage(((Long) msg.obj).longValue());
                case ImsVideoCallProviderWrapper.MSG_CHANGE_CAMERA_CAPABILITIES /*6*/:
                    ImsVideoCallProviderWrapper.this.changeCameraCapabilities((CameraCapabilities) msg.obj);
                case ImsVideoCallProviderWrapper.MSG_CHANGE_VIDEO_QUALITY /*7*/:
                    ImsVideoCallProviderWrapper.this.changeVideoQuality(msg.arg1);
                default:
            }
        }
    }

    private final class ImsVideoCallCallback extends Stub {
        private ImsVideoCallCallback() {
        }

        public void receiveSessionModifyRequest(VideoProfile VideoProfile) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(ImsVideoCallProviderWrapper.MSG_RECEIVE_SESSION_MODIFY_REQUEST, VideoProfile).sendToTarget();
        }

        public void receiveSessionModifyResponse(int status, VideoProfile requestProfile, VideoProfile responseProfile) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(status);
            args.arg2 = requestProfile;
            args.arg3 = responseProfile;
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(ImsVideoCallProviderWrapper.MSG_RECEIVE_SESSION_MODIFY_RESPONSE, args).sendToTarget();
        }

        public void handleCallSessionEvent(int event) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(ImsVideoCallProviderWrapper.MSG_HANDLE_CALL_SESSION_EVENT, Integer.valueOf(event)).sendToTarget();
        }

        public void changePeerDimensions(int width, int height) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(width);
            args.arg2 = Integer.valueOf(height);
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(ImsVideoCallProviderWrapper.MSG_CHANGE_PEER_DIMENSIONS, args).sendToTarget();
        }

        public void changeVideoQuality(int videoQuality) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(ImsVideoCallProviderWrapper.MSG_CHANGE_VIDEO_QUALITY, videoQuality, 0).sendToTarget();
        }

        public void changeCallDataUsage(long dataUsage) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(ImsVideoCallProviderWrapper.MSG_CHANGE_CALL_DATA_USAGE, Long.valueOf(dataUsage)).sendToTarget();
        }

        public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(ImsVideoCallProviderWrapper.MSG_CHANGE_CAMERA_CAPABILITIES, cameraCapabilities).sendToTarget();
        }
    }

    public ImsVideoCallProviderWrapper(IImsVideoCallProvider VideoProvider) throws RemoteException {
        this.mDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                ImsVideoCallProviderWrapper.this.mVideoCallProvider.asBinder().unlinkToDeath(this, 0);
            }
        };
        this.mHandler = new AnonymousClass2(Looper.getMainLooper());
        this.mVideoCallProvider = VideoProvider;
        this.mVideoCallProvider.asBinder().linkToDeath(this.mDeathRecipient, 0);
        this.mBinder = new ImsVideoCallCallback();
        this.mVideoCallProvider.setCallback(this.mBinder);
    }

    public void onSetCamera(String cameraId) {
        try {
            this.mVideoCallProvider.setCamera(cameraId);
        } catch (RemoteException e) {
        }
    }

    public void onSetPreviewSurface(Surface surface) {
        try {
            this.mVideoCallProvider.setPreviewSurface(surface);
        } catch (RemoteException e) {
        }
    }

    public void onSetDisplaySurface(Surface surface) {
        try {
            this.mVideoCallProvider.setDisplaySurface(surface);
        } catch (RemoteException e) {
        }
    }

    public void onSetDeviceOrientation(int rotation) {
        try {
            this.mVideoCallProvider.setDeviceOrientation(rotation);
        } catch (RemoteException e) {
        }
    }

    public void onSetZoom(float value) {
        try {
            this.mVideoCallProvider.setZoom(value);
        } catch (RemoteException e) {
        }
    }

    public void onSendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) {
        try {
            this.mVideoCallProvider.sendSessionModifyRequest(fromProfile, toProfile);
        } catch (RemoteException e) {
        }
    }

    public void onSendSessionModifyResponse(VideoProfile responseProfile) {
        try {
            this.mVideoCallProvider.sendSessionModifyResponse(responseProfile);
        } catch (RemoteException e) {
        }
    }

    public void onRequestCameraCapabilities() {
        try {
            this.mVideoCallProvider.requestCameraCapabilities();
        } catch (RemoteException e) {
        }
    }

    public void onRequestConnectionDataUsage() {
        try {
            this.mVideoCallProvider.requestCallDataUsage();
        } catch (RemoteException e) {
        }
    }

    public void onSetPauseImage(Uri uri) {
        try {
            this.mVideoCallProvider.setPauseImage(uri);
        } catch (RemoteException e) {
        }
    }
}
