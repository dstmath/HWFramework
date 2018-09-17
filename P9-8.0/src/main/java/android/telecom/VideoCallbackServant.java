package android.telecom;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.VideoProfile.CameraCapabilities;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IVideoCallback;
import com.android.internal.telecom.IVideoCallback.Stub;

final class VideoCallbackServant {
    private static final int MSG_CHANGE_CALL_DATA_USAGE = 4;
    private static final int MSG_CHANGE_CAMERA_CAPABILITIES = 5;
    private static final int MSG_CHANGE_PEER_DIMENSIONS = 3;
    private static final int MSG_CHANGE_VIDEO_QUALITY = 6;
    private static final int MSG_HANDLE_CALL_SESSION_EVENT = 2;
    private static final int MSG_RECEIVE_SESSION_MODIFY_REQUEST = 0;
    private static final int MSG_RECEIVE_SESSION_MODIFY_RESPONSE = 1;
    private final IVideoCallback mDelegate;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                internalHandleMessage(msg);
            } catch (RemoteException e) {
            }
        }

        private void internalHandleMessage(Message msg) throws RemoteException {
            SomeArgs args;
            switch (msg.what) {
                case 0:
                    VideoCallbackServant.this.mDelegate.receiveSessionModifyRequest((VideoProfile) msg.obj);
                    return;
                case 1:
                    args = msg.obj;
                    try {
                        VideoCallbackServant.this.mDelegate.receiveSessionModifyResponse(args.argi1, (VideoProfile) args.arg1, (VideoProfile) args.arg2);
                        return;
                    } finally {
                        args.recycle();
                    }
                case 2:
                    args = (SomeArgs) msg.obj;
                    try {
                        VideoCallbackServant.this.mDelegate.handleCallSessionEvent(args.argi1);
                        return;
                    } finally {
                        args.recycle();
                    }
                case 3:
                    args = (SomeArgs) msg.obj;
                    try {
                        VideoCallbackServant.this.mDelegate.changePeerDimensions(args.argi1, args.argi2);
                        return;
                    } finally {
                        args.recycle();
                    }
                case 4:
                    args = (SomeArgs) msg.obj;
                    try {
                        VideoCallbackServant.this.mDelegate.changeCallDataUsage(((Long) args.arg1).longValue());
                        return;
                    } finally {
                        args.recycle();
                    }
                case 5:
                    VideoCallbackServant.this.mDelegate.changeCameraCapabilities((CameraCapabilities) msg.obj);
                    return;
                case 6:
                    VideoCallbackServant.this.mDelegate.changeVideoQuality(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    };
    private final IVideoCallback mStub = new Stub() {
        public void receiveSessionModifyRequest(VideoProfile videoProfile) throws RemoteException {
            VideoCallbackServant.this.mHandler.obtainMessage(0, videoProfile).sendToTarget();
        }

        public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = status;
            args.arg1 = requestedProfile;
            args.arg2 = responseProfile;
            VideoCallbackServant.this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        public void handleCallSessionEvent(int event) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = event;
            VideoCallbackServant.this.mHandler.obtainMessage(2, args).sendToTarget();
        }

        public void changePeerDimensions(int width, int height) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = width;
            args.argi2 = height;
            VideoCallbackServant.this.mHandler.obtainMessage(3, args).sendToTarget();
        }

        public void changeCallDataUsage(long dataUsage) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Long.valueOf(dataUsage);
            VideoCallbackServant.this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) throws RemoteException {
            VideoCallbackServant.this.mHandler.obtainMessage(5, cameraCapabilities).sendToTarget();
        }

        public void changeVideoQuality(int videoQuality) throws RemoteException {
            VideoCallbackServant.this.mHandler.obtainMessage(6, videoQuality, 0).sendToTarget();
        }
    };

    public VideoCallbackServant(IVideoCallback delegate) {
        this.mDelegate = delegate;
    }

    public IVideoCallback getStub() {
        return this.mStub;
    }
}
