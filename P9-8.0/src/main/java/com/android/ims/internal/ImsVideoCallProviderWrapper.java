package com.android.ims.internal;

import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.telecom.Connection.VideoProvider;
import android.telecom.Log;
import android.telecom.VideoProfile;
import android.telecom.VideoProfile.CameraCapabilities;
import android.view.Surface;
import com.android.ims.internal.IImsVideoCallCallback.Stub;
import com.android.internal.os.SomeArgs;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImsVideoCallProviderWrapper extends VideoProvider {
    private static final int MSG_CHANGE_CALL_DATA_USAGE = 5;
    private static final int MSG_CHANGE_CAMERA_CAPABILITIES = 6;
    private static final int MSG_CHANGE_PEER_DIMENSIONS = 4;
    private static final int MSG_CHANGE_VIDEO_QUALITY = 7;
    private static final int MSG_HANDLE_CALL_SESSION_EVENT = 3;
    private static final int MSG_RECEIVE_SESSION_MODIFY_REQUEST = 1;
    private static final int MSG_RECEIVE_SESSION_MODIFY_RESPONSE = 2;
    private final ImsVideoCallCallback mBinder;
    private final Set<ImsVideoProviderWrapperCallback> mCallbacks;
    private RegistrantList mDataUsageUpdateRegistrants;
    private DeathRecipient mDeathRecipient;
    private final Handler mHandler;
    private boolean mUseVideoPauseWorkaround;
    private final IImsVideoCallProvider mVideoCallProvider;
    private VideoPauseTracker mVideoPauseTracker;

    private final class ImsVideoCallCallback extends Stub {
        /* synthetic */ ImsVideoCallCallback(ImsVideoCallProviderWrapper this$0, ImsVideoCallCallback -this1) {
            this();
        }

        private ImsVideoCallCallback() {
        }

        public void receiveSessionModifyRequest(VideoProfile VideoProfile) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(1, VideoProfile).sendToTarget();
        }

        public void receiveSessionModifyResponse(int status, VideoProfile requestProfile, VideoProfile responseProfile) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(status);
            args.arg2 = requestProfile;
            args.arg3 = responseProfile;
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(2, args).sendToTarget();
        }

        public void handleCallSessionEvent(int event) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(3, Integer.valueOf(event)).sendToTarget();
        }

        public void changePeerDimensions(int width, int height) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(width);
            args.arg2 = Integer.valueOf(height);
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        public void changeVideoQuality(int videoQuality) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(7, videoQuality, 0).sendToTarget();
        }

        public void changeCallDataUsage(long dataUsage) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(5, Long.valueOf(dataUsage)).sendToTarget();
        }

        public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) {
            ImsVideoCallProviderWrapper.this.mHandler.obtainMessage(6, cameraCapabilities).sendToTarget();
        }
    }

    public interface ImsVideoProviderWrapperCallback {
        void onReceiveSessionModifyResponse(int i, VideoProfile videoProfile, VideoProfile videoProfile2);
    }

    public void registerForDataUsageUpdate(Handler h, int what, Object obj) {
        this.mDataUsageUpdateRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForDataUsageUpdate(Handler h) {
        this.mDataUsageUpdateRegistrants.remove(h);
    }

    public void addImsVideoProviderCallback(ImsVideoProviderWrapperCallback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeImsVideoProviderCallback(ImsVideoProviderWrapperCallback callback) {
        this.mCallbacks.remove(callback);
    }

    public ImsVideoCallProviderWrapper(IImsVideoCallProvider videoProvider) throws RemoteException {
        this.mDataUsageUpdateRegistrants = new RegistrantList();
        this.mCallbacks = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        this.mVideoPauseTracker = new VideoPauseTracker();
        this.mUseVideoPauseWorkaround = false;
        this.mDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                ImsVideoCallProviderWrapper.this.mVideoCallProvider.asBinder().unlinkToDeath(this, 0);
            }
        };
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                SomeArgs args;
                switch (msg.what) {
                    case 1:
                        ImsVideoCallProviderWrapper.this.receiveSessionModifyRequest((VideoProfile) msg.obj);
                        return;
                    case 2:
                        args = msg.obj;
                        try {
                            int status = ((Integer) args.arg1).intValue();
                            VideoProfile requestProfile = args.arg2;
                            VideoProfile responseProfile = args.arg3;
                            ImsVideoCallProviderWrapper.this.receiveSessionModifyResponse(status, requestProfile, responseProfile);
                            for (ImsVideoProviderWrapperCallback callback : ImsVideoCallProviderWrapper.this.mCallbacks) {
                                if (callback != null) {
                                    callback.onReceiveSessionModifyResponse(status, requestProfile, responseProfile);
                                }
                            }
                            return;
                        } finally {
                            args.recycle();
                        }
                    case 3:
                        ImsVideoCallProviderWrapper.this.handleCallSessionEvent(((Integer) msg.obj).intValue());
                        return;
                    case 4:
                        args = (SomeArgs) msg.obj;
                        try {
                            ImsVideoCallProviderWrapper.this.changePeerDimensions(((Integer) args.arg1).intValue(), ((Integer) args.arg2).intValue());
                            return;
                        } finally {
                            args.recycle();
                        }
                    case 5:
                        ImsVideoCallProviderWrapper.this.setCallDataUsage(((Long) msg.obj).longValue());
                        ImsVideoCallProviderWrapper.this.mDataUsageUpdateRegistrants.notifyResult(msg.obj);
                        return;
                    case 6:
                        ImsVideoCallProviderWrapper.this.changeCameraCapabilities((CameraCapabilities) msg.obj);
                        return;
                    case 7:
                        ImsVideoCallProviderWrapper.this.changeVideoQuality(msg.arg1);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mVideoCallProvider = videoProvider;
        if (videoProvider != null) {
            this.mVideoCallProvider.asBinder().linkToDeath(this.mDeathRecipient, 0);
            this.mBinder = new ImsVideoCallCallback(this, null);
            this.mVideoCallProvider.setCallback(this.mBinder);
            return;
        }
        this.mBinder = null;
    }

    public ImsVideoCallProviderWrapper(IImsVideoCallProvider videoProvider, VideoPauseTracker videoPauseTracker) throws RemoteException {
        this(videoProvider);
        this.mVideoPauseTracker = videoPauseTracker;
    }

    public void onSetCamera(String cameraId) {
        try {
            this.mVideoCallProvider.setCamera(cameraId, Binder.getCallingUid());
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
        if (fromProfile == null || toProfile == null) {
            Log.w(this, "onSendSessionModifyRequest: null profile in request.", new Object[0]);
            return;
        }
        try {
            int fromVideoState = fromProfile.getVideoState();
            int toVideoState = toProfile.getVideoState();
            Log.i(this, "onSendSessionModifyRequest: fromVideoState=%s, toVideoState=%s; ", new Object[]{VideoProfile.videoStateToString(fromProfile.getVideoState()), VideoProfile.videoStateToString(toProfile.getVideoState())});
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

    public static boolean isPauseRequest(int from, int to) {
        return !VideoProfile.isPaused(from) ? VideoProfile.isPaused(to) : false;
    }

    public static boolean isResumeRequest(int from, int to) {
        return VideoProfile.isPaused(from) ? VideoProfile.isPaused(to) ^ 1 : false;
    }

    public static boolean isTurnOffCameraRequest(int from, int to) {
        if (VideoProfile.isTransmissionEnabled(from)) {
            return VideoProfile.isTransmissionEnabled(to) ^ 1;
        }
        return false;
    }

    public static boolean isTurnOnCameraRequest(int from, int to) {
        if (VideoProfile.isTransmissionEnabled(from)) {
            return false;
        }
        return VideoProfile.isTransmissionEnabled(to);
    }

    public VideoProfile maybeFilterPauseResume(VideoProfile fromProfile, VideoProfile toProfile, int source) {
        boolean isPauseSpecialCase;
        int fromVideoState = fromProfile.getVideoState();
        int toVideoState = toProfile.getVideoState();
        if (source == 1 && VideoProfile.isPaused(fromVideoState)) {
            isPauseSpecialCase = VideoProfile.isPaused(toVideoState);
        } else {
            isPauseSpecialCase = false;
        }
        boolean isPauseRequest = !isPauseRequest(fromVideoState, toVideoState) ? isPauseSpecialCase : true;
        boolean isResumeRequest = isResumeRequest(fromVideoState, toVideoState);
        if (isPauseRequest) {
            Log.i(this, "maybeFilterPauseResume: isPauseRequest (from=%s, to=%s)", new Object[]{VideoProfile.videoStateToString(fromVideoState), VideoProfile.videoStateToString(toVideoState)});
            if (!(this.mVideoPauseTracker.shouldPauseVideoFor(source) || (isPauseSpecialCase ^ 1) == 0)) {
                toProfile = new VideoProfile(toVideoState & -5, toProfile.getQuality());
            }
        } else if (isResumeRequest) {
            boolean isTurnOffCameraRequest = isTurnOffCameraRequest(fromVideoState, toVideoState);
            boolean isTurnOnCameraRequest = isTurnOnCameraRequest(fromVideoState, toVideoState);
            if (this.mUseVideoPauseWorkaround && (isTurnOffCameraRequest || isTurnOnCameraRequest)) {
                Log.i(this, "maybeFilterPauseResume: isResumeRequest, but camera turning on/off so skipping (from=%s, to=%s)", new Object[]{VideoProfile.videoStateToString(fromVideoState), VideoProfile.videoStateToString(toVideoState)});
                return toProfile;
            }
            Log.i(this, "maybeFilterPauseResume: isResumeRequest (from=%s, to=%s)", new Object[]{VideoProfile.videoStateToString(fromVideoState), VideoProfile.videoStateToString(toVideoState)});
            if (!this.mVideoPauseTracker.shouldResumeVideoFor(source)) {
                toProfile = new VideoProfile(toVideoState | 4, toProfile.getQuality());
            }
        }
        return toProfile;
    }

    public void pauseVideo(int fromVideoState, int source) {
        if (this.mVideoPauseTracker.shouldPauseVideoFor(source)) {
            try {
                Log.i(this, "pauseVideo: fromVideoState=%s, toVideoState=%s", new Object[]{VideoProfile.videoStateToString(new VideoProfile(fromVideoState).getVideoState()), VideoProfile.videoStateToString(new VideoProfile(fromVideoState | 4).getVideoState())});
                this.mVideoCallProvider.sendSessionModifyRequest(fromProfile, toProfile);
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        Log.i(this, "pauseVideo: video already paused", new Object[0]);
    }

    public void resumeVideo(int fromVideoState, int source) {
        if (this.mVideoPauseTracker.shouldResumeVideoFor(source)) {
            try {
                Log.i(this, "resumeVideo: fromVideoState=%s, toVideoState=%s", new Object[]{VideoProfile.videoStateToString(new VideoProfile(fromVideoState).getVideoState()), VideoProfile.videoStateToString(new VideoProfile(fromVideoState & -5).getVideoState())});
                this.mVideoCallProvider.sendSessionModifyRequest(fromProfile, toProfile);
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        Log.i(this, "resumeVideo: remaining paused (paused from other sources)", new Object[0]);
    }

    public boolean wasVideoPausedFromSource(int source) {
        return this.mVideoPauseTracker.wasVideoPausedFromSource(source);
    }

    public void setUseVideoPauseWorkaround(boolean useVideoPauseWorkaround) {
        this.mUseVideoPauseWorkaround = useVideoPauseWorkaround;
    }

    public void onVideoStateChanged(int newVideoState) {
        if (this.mVideoPauseTracker.isPaused() && (VideoProfile.isPaused(newVideoState) ^ 1) != 0) {
            Log.i(this, "onVideoStateChanged: newVideoState=%s, clearing pending pause requests.", new Object[]{VideoProfile.videoStateToString(newVideoState)});
            this.mVideoPauseTracker.clearPauseRequests();
        }
    }
}
