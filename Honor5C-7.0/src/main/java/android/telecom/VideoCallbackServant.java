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
    private final Handler mHandler;
    private final IVideoCallback mStub;

    public VideoCallbackServant(IVideoCallback delegate) {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    internalHandleMessage(msg);
                } catch (RemoteException e) {
                }
            }

            private void internalHandleMessage(Message msg) throws RemoteException {
                SomeArgs args;
                switch (msg.what) {
                    case VideoCallbackServant.MSG_RECEIVE_SESSION_MODIFY_REQUEST /*0*/:
                        VideoCallbackServant.this.mDelegate.receiveSessionModifyRequest((VideoProfile) msg.obj);
                    case VideoCallbackServant.MSG_RECEIVE_SESSION_MODIFY_RESPONSE /*1*/:
                        args = msg.obj;
                        try {
                            VideoCallbackServant.this.mDelegate.receiveSessionModifyResponse(args.argi1, (VideoProfile) args.arg1, (VideoProfile) args.arg2);
                        } finally {
                            args.recycle();
                        }
                    case VideoCallbackServant.MSG_HANDLE_CALL_SESSION_EVENT /*2*/:
                        args = (SomeArgs) msg.obj;
                        try {
                            VideoCallbackServant.this.mDelegate.handleCallSessionEvent(args.argi1);
                        } finally {
                            args.recycle();
                        }
                    case VideoCallbackServant.MSG_CHANGE_PEER_DIMENSIONS /*3*/:
                        args = (SomeArgs) msg.obj;
                        try {
                            VideoCallbackServant.this.mDelegate.changePeerDimensions(args.argi1, args.argi2);
                        } finally {
                            args.recycle();
                        }
                    case VideoCallbackServant.MSG_CHANGE_CALL_DATA_USAGE /*4*/:
                        args = (SomeArgs) msg.obj;
                        try {
                            VideoCallbackServant.this.mDelegate.changeCallDataUsage(((Long) args.arg1).longValue());
                        } finally {
                            args.recycle();
                        }
                    case VideoCallbackServant.MSG_CHANGE_CAMERA_CAPABILITIES /*5*/:
                        VideoCallbackServant.this.mDelegate.changeCameraCapabilities((CameraCapabilities) msg.obj);
                    case VideoCallbackServant.MSG_CHANGE_VIDEO_QUALITY /*6*/:
                        VideoCallbackServant.this.mDelegate.changeVideoQuality(msg.arg1);
                    default:
                }
            }
        };
        this.mStub = new Stub() {
            public void receiveSessionModifyRequest(VideoProfile videoProfile) throws RemoteException {
                VideoCallbackServant.this.mHandler.obtainMessage(VideoCallbackServant.MSG_RECEIVE_SESSION_MODIFY_REQUEST, videoProfile).sendToTarget();
            }

            public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) throws RemoteException {
                SomeArgs args = SomeArgs.obtain();
                args.argi1 = status;
                args.arg1 = requestedProfile;
                args.arg2 = responseProfile;
                VideoCallbackServant.this.mHandler.obtainMessage(VideoCallbackServant.MSG_RECEIVE_SESSION_MODIFY_RESPONSE, args).sendToTarget();
            }

            public void handleCallSessionEvent(int event) throws RemoteException {
                SomeArgs args = SomeArgs.obtain();
                args.argi1 = event;
                VideoCallbackServant.this.mHandler.obtainMessage(VideoCallbackServant.MSG_HANDLE_CALL_SESSION_EVENT, args).sendToTarget();
            }

            public void changePeerDimensions(int width, int height) throws RemoteException {
                SomeArgs args = SomeArgs.obtain();
                args.argi1 = width;
                args.argi2 = height;
                VideoCallbackServant.this.mHandler.obtainMessage(VideoCallbackServant.MSG_CHANGE_PEER_DIMENSIONS, args).sendToTarget();
            }

            public void changeCallDataUsage(long dataUsage) throws RemoteException {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = Long.valueOf(dataUsage);
                VideoCallbackServant.this.mHandler.obtainMessage(VideoCallbackServant.MSG_CHANGE_CALL_DATA_USAGE, args).sendToTarget();
            }

            public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) throws RemoteException {
                VideoCallbackServant.this.mHandler.obtainMessage(VideoCallbackServant.MSG_CHANGE_CAMERA_CAPABILITIES, cameraCapabilities).sendToTarget();
            }

            public void changeVideoQuality(int videoQuality) throws RemoteException {
                VideoCallbackServant.this.mHandler.obtainMessage(VideoCallbackServant.MSG_CHANGE_VIDEO_QUALITY, videoQuality, VideoCallbackServant.MSG_RECEIVE_SESSION_MODIFY_REQUEST).sendToTarget();
            }
        };
        this.mDelegate = delegate;
    }

    public IVideoCallback getStub() {
        return this.mStub;
    }
}
