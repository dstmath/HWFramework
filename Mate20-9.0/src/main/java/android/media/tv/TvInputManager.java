package android.media.tv;

import android.annotation.SystemApi;
import android.content.Intent;
import android.graphics.Rect;
import android.media.PlaybackParams;
import android.media.tv.ITvInputClient;
import android.media.tv.ITvInputHardwareCallback;
import android.media.tv.ITvInputManagerCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pools;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventSender;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class TvInputManager {
    public static final String ACTION_BLOCKED_RATINGS_CHANGED = "android.media.tv.action.BLOCKED_RATINGS_CHANGED";
    public static final String ACTION_PARENTAL_CONTROLS_ENABLED_CHANGED = "android.media.tv.action.PARENTAL_CONTROLS_ENABLED_CHANGED";
    public static final String ACTION_QUERY_CONTENT_RATING_SYSTEMS = "android.media.tv.action.QUERY_CONTENT_RATING_SYSTEMS";
    public static final String ACTION_SETUP_INPUTS = "android.media.tv.action.SETUP_INPUTS";
    public static final String ACTION_VIEW_RECORDING_SCHEDULES = "android.media.tv.action.VIEW_RECORDING_SCHEDULES";
    public static final int DVB_DEVICE_DEMUX = 0;
    public static final int DVB_DEVICE_DVR = 1;
    static final int DVB_DEVICE_END = 2;
    public static final int DVB_DEVICE_FRONTEND = 2;
    static final int DVB_DEVICE_START = 0;
    public static final int INPUT_STATE_CONNECTED = 0;
    public static final int INPUT_STATE_CONNECTED_STANDBY = 1;
    public static final int INPUT_STATE_DISCONNECTED = 2;
    public static final String META_DATA_CONTENT_RATING_SYSTEMS = "android.media.tv.metadata.CONTENT_RATING_SYSTEMS";
    static final int RECORDING_ERROR_END = 2;
    public static final int RECORDING_ERROR_INSUFFICIENT_SPACE = 1;
    public static final int RECORDING_ERROR_RESOURCE_BUSY = 2;
    static final int RECORDING_ERROR_START = 0;
    public static final int RECORDING_ERROR_UNKNOWN = 0;
    private static final String TAG = "TvInputManager";
    public static final long TIME_SHIFT_INVALID_TIME = Long.MIN_VALUE;
    public static final int TIME_SHIFT_STATUS_AVAILABLE = 3;
    public static final int TIME_SHIFT_STATUS_UNAVAILABLE = 2;
    public static final int TIME_SHIFT_STATUS_UNKNOWN = 0;
    public static final int TIME_SHIFT_STATUS_UNSUPPORTED = 1;
    public static final int VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY = 4;
    public static final int VIDEO_UNAVAILABLE_REASON_BUFFERING = 3;
    static final int VIDEO_UNAVAILABLE_REASON_END = 4;
    static final int VIDEO_UNAVAILABLE_REASON_START = 0;
    public static final int VIDEO_UNAVAILABLE_REASON_TUNING = 1;
    public static final int VIDEO_UNAVAILABLE_REASON_UNKNOWN = 0;
    public static final int VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL = 2;
    /* access modifiers changed from: private */
    public final List<TvInputCallbackRecord> mCallbackRecords = new LinkedList();
    private final ITvInputClient mClient;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private int mNextSeq;
    /* access modifiers changed from: private */
    public final ITvInputManager mService;
    /* access modifiers changed from: private */
    public final SparseArray<SessionCallbackRecord> mSessionCallbackRecordMap = new SparseArray<>();
    /* access modifiers changed from: private */
    public final Map<String, Integer> mStateMap = new ArrayMap();
    /* access modifiers changed from: private */
    public final int mUserId;

    @SystemApi
    public static final class Hardware {
        private final ITvInputHardware mInterface;

        private Hardware(ITvInputHardware hardwareInterface) {
            this.mInterface = hardwareInterface;
        }

        /* access modifiers changed from: private */
        public ITvInputHardware getInterface() {
            return this.mInterface;
        }

        public boolean setSurface(Surface surface, TvStreamConfig config) {
            try {
                return this.mInterface.setSurface(surface, config);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        public void setStreamVolume(float volume) {
            try {
                this.mInterface.setStreamVolume(volume);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        @SystemApi
        public boolean dispatchKeyEventToHdmi(KeyEvent event) {
            return false;
        }

        public void overrideAudioSink(int audioType, String audioAddress, int samplingRate, int channelMask, int format) {
            try {
                this.mInterface.overrideAudioSink(audioType, audioAddress, samplingRate, channelMask, format);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SystemApi
    public static abstract class HardwareCallback {
        public abstract void onReleased();

        public abstract void onStreamConfigChanged(TvStreamConfig[] tvStreamConfigArr);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InputState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RecordingError {
    }

    public static final class Session {
        static final int DISPATCH_HANDLED = 1;
        static final int DISPATCH_IN_PROGRESS = -1;
        static final int DISPATCH_NOT_HANDLED = 0;
        private static final long INPUT_SESSION_NOT_RESPONDING_TIMEOUT = 2500;
        private final List<TvTrackInfo> mAudioTracks;
        private InputChannel mChannel;
        private final InputEventHandler mHandler;
        private final Object mMetadataLock;
        private final Pools.Pool<PendingEvent> mPendingEventPool;
        private final SparseArray<PendingEvent> mPendingEvents;
        private String mSelectedAudioTrackId;
        private String mSelectedSubtitleTrackId;
        private String mSelectedVideoTrackId;
        private TvInputEventSender mSender;
        private final int mSeq;
        private final ITvInputManager mService;
        private final SparseArray<SessionCallbackRecord> mSessionCallbackRecordMap;
        private final List<TvTrackInfo> mSubtitleTracks;
        private IBinder mToken;
        private final int mUserId;
        private int mVideoHeight;
        private final List<TvTrackInfo> mVideoTracks;
        private int mVideoWidth;

        public interface FinishedInputEventCallback {
            void onFinishedInputEvent(Object obj, boolean z);
        }

        private final class InputEventHandler extends Handler {
            public static final int MSG_FLUSH_INPUT_EVENT = 3;
            public static final int MSG_SEND_INPUT_EVENT = 1;
            public static final int MSG_TIMEOUT_INPUT_EVENT = 2;

            InputEventHandler(Looper looper) {
                super(looper, null, true);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Session.this.sendInputEventAndReportResultOnMainLooper((PendingEvent) msg.obj);
                        return;
                    case 2:
                        int seq = msg.arg1;
                        if (msg.obj instanceof PendingEvent) {
                            PendingEvent p = (PendingEvent) msg.obj;
                            seq = p.mEvent == null ? msg.arg1 : p.mEvent.getSequenceNumber();
                        }
                        Session.this.finishedInputEvent(seq, false, true);
                        return;
                    case 3:
                        Session.this.finishedInputEvent(msg.arg1, false, false);
                        return;
                    default:
                        return;
                }
            }
        }

        private final class PendingEvent implements Runnable {
            public FinishedInputEventCallback mCallback;
            public InputEvent mEvent;
            public Handler mEventHandler;
            public Object mEventToken;
            public boolean mHandled;

            private PendingEvent() {
            }

            public void recycle() {
                this.mEvent = null;
                this.mEventToken = null;
                this.mCallback = null;
                this.mEventHandler = null;
                this.mHandled = false;
            }

            public void run() {
                this.mCallback.onFinishedInputEvent(this.mEventToken, this.mHandled);
                synchronized (this.mEventHandler) {
                    Session.this.recyclePendingEventLocked(this);
                }
            }
        }

        private final class TvInputEventSender extends InputEventSender {
            public TvInputEventSender(InputChannel inputChannel, Looper looper) {
                super(inputChannel, looper);
            }

            public void onInputEventFinished(int seq, boolean handled) {
                Session.this.finishedInputEvent(seq, handled, false);
            }
        }

        private Session(IBinder token, InputChannel channel, ITvInputManager service, int userId, int seq, SparseArray<SessionCallbackRecord> sessionCallbackRecordMap) {
            this.mHandler = new InputEventHandler(Looper.getMainLooper());
            this.mPendingEventPool = new Pools.SimplePool(20);
            this.mPendingEvents = new SparseArray<>(20);
            this.mMetadataLock = new Object();
            this.mAudioTracks = new ArrayList();
            this.mVideoTracks = new ArrayList();
            this.mSubtitleTracks = new ArrayList();
            this.mToken = token;
            this.mChannel = channel;
            this.mService = service;
            this.mUserId = userId;
            this.mSeq = seq;
            this.mSessionCallbackRecordMap = sessionCallbackRecordMap;
        }

        public void release() {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.releaseSession(this.mToken, this.mUserId);
                releaseInternal();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void setMain() {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.setMainSession(this.mToken, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void setSurface(Surface surface) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.setSurface(this.mToken, surface, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void dispatchSurfaceChanged(int format, int width, int height) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.dispatchSurfaceChanged(this.mToken, format, width, height, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void setStreamVolume(float volume) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
            } else if (volume < 0.0f || volume > 1.0f) {
                throw new IllegalArgumentException("volume should be between 0.0f and 1.0f");
            } else {
                try {
                    this.mService.setVolume(this.mToken, volume, this.mUserId);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        public void tune(Uri channelUri) {
            tune(channelUri, null);
        }

        public void tune(Uri channelUri, Bundle params) {
            Preconditions.checkNotNull(channelUri);
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            synchronized (this.mMetadataLock) {
                this.mAudioTracks.clear();
                this.mVideoTracks.clear();
                this.mSubtitleTracks.clear();
                this.mSelectedAudioTrackId = null;
                this.mSelectedVideoTrackId = null;
                this.mSelectedSubtitleTrackId = null;
                this.mVideoWidth = 0;
                this.mVideoHeight = 0;
            }
            try {
                this.mService.tune(this.mToken, channelUri, params, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void setCaptionEnabled(boolean enabled) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.setCaptionEnabled(this.mToken, enabled, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0077, code lost:
            if (r4.mToken != null) goto L_0x0081;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0079, code lost:
            android.util.Log.w(android.media.tv.TvInputManager.TAG, "The session has been already released");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0080, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
            r4.mService.selectTrack(r4.mToken, r5, r6, r4.mUserId);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x008b, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x008c, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0091, code lost:
            throw r0.rethrowFromSystemServer();
         */
        public void selectTrack(int type, String trackId) {
            synchronized (this.mMetadataLock) {
                if (type == 0) {
                    if (trackId != null) {
                        try {
                            if (!containsTrack(this.mAudioTracks, trackId)) {
                                Log.w(TvInputManager.TAG, "Invalid audio trackId: " + trackId);
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                } else if (type == 1) {
                    if (trackId != null && !containsTrack(this.mVideoTracks, trackId)) {
                        Log.w(TvInputManager.TAG, "Invalid video trackId: " + trackId);
                    }
                } else if (type != 2) {
                    throw new IllegalArgumentException("invalid type: " + type);
                } else if (trackId != null && !containsTrack(this.mSubtitleTracks, trackId)) {
                    Log.w(TvInputManager.TAG, "Invalid subtitle trackId: " + trackId);
                }
            }
        }

        private boolean containsTrack(List<TvTrackInfo> tracks, String trackId) {
            for (TvTrackInfo track : tracks) {
                if (track.getId().equals(trackId)) {
                    return true;
                }
            }
            return false;
        }

        public List<TvTrackInfo> getTracks(int type) {
            synchronized (this.mMetadataLock) {
                if (type == 0) {
                    try {
                        if (this.mAudioTracks == null) {
                            return null;
                        }
                        ArrayList arrayList = new ArrayList(this.mAudioTracks);
                        return arrayList;
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
                } else if (type == 1) {
                    if (this.mVideoTracks == null) {
                        return null;
                    }
                    ArrayList arrayList2 = new ArrayList(this.mVideoTracks);
                    return arrayList2;
                } else if (type != 2) {
                    throw new IllegalArgumentException("invalid type: " + type);
                } else if (this.mSubtitleTracks == null) {
                    return null;
                } else {
                    ArrayList arrayList3 = new ArrayList(this.mSubtitleTracks);
                    return arrayList3;
                }
            }
        }

        public String getSelectedTrack(int type) {
            synchronized (this.mMetadataLock) {
                if (type == 0) {
                    try {
                        String str = this.mSelectedAudioTrackId;
                        return str;
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
                } else if (type == 1) {
                    String str2 = this.mSelectedVideoTrackId;
                    return str2;
                } else if (type == 2) {
                    String str3 = this.mSelectedSubtitleTrackId;
                    return str3;
                } else {
                    throw new IllegalArgumentException("invalid type: " + type);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean updateTracks(List<TvTrackInfo> tracks) {
            boolean z;
            synchronized (this.mMetadataLock) {
                this.mAudioTracks.clear();
                this.mVideoTracks.clear();
                this.mSubtitleTracks.clear();
                Iterator<TvTrackInfo> it = tracks.iterator();
                while (true) {
                    z = true;
                    if (!it.hasNext()) {
                        break;
                    }
                    TvTrackInfo track = it.next();
                    if (track.getType() == 0) {
                        this.mAudioTracks.add(track);
                    } else if (track.getType() == 1) {
                        this.mVideoTracks.add(track);
                    } else if (track.getType() == 2) {
                        this.mSubtitleTracks.add(track);
                    }
                }
                if (this.mAudioTracks.isEmpty() && this.mVideoTracks.isEmpty()) {
                    if (this.mSubtitleTracks.isEmpty()) {
                        z = false;
                    }
                }
            }
            return z;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0033, code lost:
            return false;
         */
        public boolean updateTrackSelection(int type, String trackId) {
            synchronized (this.mMetadataLock) {
                if (type == 0) {
                    try {
                        if (!TextUtils.equals(trackId, this.mSelectedAudioTrackId)) {
                            this.mSelectedAudioTrackId = trackId;
                            return true;
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (type == 1 && !TextUtils.equals(trackId, this.mSelectedVideoTrackId)) {
                    this.mSelectedVideoTrackId = trackId;
                    return true;
                } else if (type == 2 && !TextUtils.equals(trackId, this.mSelectedSubtitleTrackId)) {
                    this.mSelectedSubtitleTrackId = trackId;
                    return true;
                }
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
            return null;
         */
        public TvTrackInfo getVideoTrackToNotify() {
            synchronized (this.mMetadataLock) {
                if (!this.mVideoTracks.isEmpty() && this.mSelectedVideoTrackId != null) {
                    for (TvTrackInfo track : this.mVideoTracks) {
                        if (track.getId().equals(this.mSelectedVideoTrackId)) {
                            int videoWidth = track.getVideoWidth();
                            int videoHeight = track.getVideoHeight();
                            if (this.mVideoWidth != videoWidth || this.mVideoHeight != videoHeight) {
                                this.mVideoWidth = videoWidth;
                                this.mVideoHeight = videoHeight;
                                return track;
                            }
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void timeShiftPlay(Uri recordedProgramUri) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.timeShiftPlay(this.mToken, recordedProgramUri, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void timeShiftPause() {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.timeShiftPause(this.mToken, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void timeShiftResume() {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.timeShiftResume(this.mToken, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void timeShiftSeekTo(long timeMs) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.timeShiftSeekTo(this.mToken, timeMs, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void timeShiftSetPlaybackParams(PlaybackParams params) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.timeShiftSetPlaybackParams(this.mToken, params, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void timeShiftEnablePositionTracking(boolean enable) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.timeShiftEnablePositionTracking(this.mToken, enable, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void startRecording(Uri programUri) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.startRecording(this.mToken, programUri, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void stopRecording() {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.stopRecording(this.mToken, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void sendAppPrivateCommand(String action, Bundle data) {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.sendAppPrivateCommand(this.mToken, action, data, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void createOverlayView(View view, Rect frame) {
            Preconditions.checkNotNull(view);
            Preconditions.checkNotNull(frame);
            if (view.getWindowToken() == null) {
                throw new IllegalStateException("view must be attached to a window");
            } else if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
            } else {
                try {
                    this.mService.createOverlayView(this.mToken, view.getWindowToken(), frame, this.mUserId);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void relayoutOverlayView(Rect frame) {
            Preconditions.checkNotNull(frame);
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.relayoutOverlayView(this.mToken, frame, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void removeOverlayView() {
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.removeOverlayView(this.mToken, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void unblockContent(TvContentRating unblockedRating) {
            Preconditions.checkNotNull(unblockedRating);
            if (this.mToken == null) {
                Log.w(TvInputManager.TAG, "The session has been already released");
                return;
            }
            try {
                this.mService.unblockContent(this.mToken, unblockedRating.flattenToString(), this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public int dispatchInputEvent(InputEvent event, Object token, FinishedInputEventCallback callback, Handler handler) {
            Preconditions.checkNotNull(event);
            Preconditions.checkNotNull(callback);
            Preconditions.checkNotNull(handler);
            synchronized (this.mHandler) {
                if (this.mChannel == null) {
                    return 0;
                }
                PendingEvent p = obtainPendingEventLocked(event, token, callback, handler);
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    int sendInputEventOnMainLooperLocked = sendInputEventOnMainLooperLocked(p);
                    return sendInputEventOnMainLooperLocked;
                }
                Message msg = this.mHandler.obtainMessage(1, p);
                msg.setAsynchronous(true);
                this.mHandler.sendMessage(msg);
                return -1;
            }
        }

        /* access modifiers changed from: private */
        public void sendInputEventAndReportResultOnMainLooper(PendingEvent p) {
            synchronized (this.mHandler) {
                if (sendInputEventOnMainLooperLocked(p) != -1) {
                    invokeFinishedInputEventCallback(p, false);
                }
            }
        }

        private int sendInputEventOnMainLooperLocked(PendingEvent p) {
            if (this.mChannel != null) {
                if (this.mSender == null) {
                    this.mSender = new TvInputEventSender(this.mChannel, this.mHandler.getLooper());
                }
                InputEvent event = p.mEvent;
                int seq = event.getSequenceNumber();
                if (this.mSender.sendInputEvent(seq, event)) {
                    this.mPendingEvents.put(seq, p);
                    Message msg = this.mHandler.obtainMessage(2, p);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageDelayed(msg, INPUT_SESSION_NOT_RESPONDING_TIMEOUT);
                    return -1;
                }
                Log.w(TvInputManager.TAG, "Unable to send input event to session: " + this.mToken + " dropping:" + event);
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x003c, code lost:
            invokeFinishedInputEventCallback(r2, r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0040, code lost:
            return;
         */
        public void finishedInputEvent(int seq, boolean handled, boolean timeout) {
            synchronized (this.mHandler) {
                int index = this.mPendingEvents.indexOfKey(seq);
                if (index >= 0) {
                    PendingEvent p = this.mPendingEvents.valueAt(index);
                    this.mPendingEvents.removeAt(index);
                    if (timeout) {
                        Log.w(TvInputManager.TAG, "Timeout waiting for session to handle input event after 2500 ms: " + this.mToken);
                    } else {
                        this.mHandler.removeMessages(2, p);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void invokeFinishedInputEventCallback(PendingEvent p, boolean handled) {
            p.mHandled = handled;
            if (p.mEventHandler.getLooper().isCurrentThread()) {
                p.run();
                return;
            }
            Message msg = Message.obtain(p.mEventHandler, p);
            msg.setAsynchronous(true);
            msg.sendToTarget();
        }

        private void flushPendingEventsLocked() {
            this.mHandler.removeMessages(3);
            int count = this.mPendingEvents.size();
            for (int i = 0; i < count; i++) {
                Message msg = this.mHandler.obtainMessage(3, this.mPendingEvents.keyAt(i), 0);
                msg.setAsynchronous(true);
                msg.sendToTarget();
            }
        }

        private PendingEvent obtainPendingEventLocked(InputEvent event, Object token, FinishedInputEventCallback callback, Handler handler) {
            PendingEvent p = (PendingEvent) this.mPendingEventPool.acquire();
            if (p == null) {
                p = new PendingEvent();
            }
            p.mEvent = event;
            p.mEventToken = token;
            p.mCallback = callback;
            p.mEventHandler = handler;
            return p;
        }

        /* access modifiers changed from: private */
        public void recyclePendingEventLocked(PendingEvent p) {
            p.recycle();
            this.mPendingEventPool.release(p);
        }

        /* access modifiers changed from: package-private */
        public IBinder getToken() {
            return this.mToken;
        }

        /* access modifiers changed from: private */
        public void releaseInternal() {
            this.mToken = null;
            synchronized (this.mHandler) {
                if (this.mChannel != null) {
                    if (this.mSender != null) {
                        flushPendingEventsLocked();
                        this.mSender.dispose();
                        this.mSender = null;
                    }
                    this.mChannel.dispose();
                    this.mChannel = null;
                }
            }
            synchronized (this.mSessionCallbackRecordMap) {
                this.mSessionCallbackRecordMap.remove(this.mSeq);
            }
        }
    }

    public static abstract class SessionCallback {
        public void onSessionCreated(Session session) {
        }

        public void onSessionReleased(Session session) {
        }

        public void onChannelRetuned(Session session, Uri channelUri) {
        }

        public void onTracksChanged(Session session, List<TvTrackInfo> list) {
        }

        public void onTrackSelected(Session session, int type, String trackId) {
        }

        public void onVideoSizeChanged(Session session, int width, int height) {
        }

        public void onVideoAvailable(Session session) {
        }

        public void onVideoUnavailable(Session session, int reason) {
        }

        public void onContentAllowed(Session session) {
        }

        public void onContentBlocked(Session session, TvContentRating rating) {
        }

        public void onLayoutSurface(Session session, int left, int top, int right, int bottom) {
        }

        public void onSessionEvent(Session session, String eventType, Bundle eventArgs) {
        }

        public void onTimeShiftStatusChanged(Session session, int status) {
        }

        public void onTimeShiftStartPositionChanged(Session session, long timeMs) {
        }

        public void onTimeShiftCurrentPositionChanged(Session session, long timeMs) {
        }

        /* access modifiers changed from: package-private */
        public void onTuned(Session session, Uri channelUri) {
        }

        /* access modifiers changed from: package-private */
        public void onRecordingStopped(Session session, Uri recordedProgramUri) {
        }

        /* access modifiers changed from: package-private */
        public void onError(Session session, int error) {
        }
    }

    private static final class SessionCallbackRecord {
        private final Handler mHandler;
        /* access modifiers changed from: private */
        public Session mSession;
        /* access modifiers changed from: private */
        public final SessionCallback mSessionCallback;

        SessionCallbackRecord(SessionCallback sessionCallback, Handler handler) {
            this.mSessionCallback = sessionCallback;
            this.mHandler = handler;
        }

        /* access modifiers changed from: package-private */
        public void postSessionCreated(final Session session) {
            this.mSession = session;
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onSessionCreated(session);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postSessionReleased() {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onSessionReleased(SessionCallbackRecord.this.mSession);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postChannelRetuned(final Uri channelUri) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onChannelRetuned(SessionCallbackRecord.this.mSession, channelUri);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postTracksChanged(final List<TvTrackInfo> tracks) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onTracksChanged(SessionCallbackRecord.this.mSession, tracks);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postTrackSelected(final int type, final String trackId) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onTrackSelected(SessionCallbackRecord.this.mSession, type, trackId);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postVideoSizeChanged(final int width, final int height) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onVideoSizeChanged(SessionCallbackRecord.this.mSession, width, height);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postVideoAvailable() {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onVideoAvailable(SessionCallbackRecord.this.mSession);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postVideoUnavailable(final int reason) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onVideoUnavailable(SessionCallbackRecord.this.mSession, reason);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postContentAllowed() {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onContentAllowed(SessionCallbackRecord.this.mSession);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postContentBlocked(final TvContentRating rating) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onContentBlocked(SessionCallbackRecord.this.mSession, rating);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postLayoutSurface(int left, int top, int right, int bottom) {
            Handler handler = this.mHandler;
            final int i = left;
            final int i2 = top;
            final int i3 = right;
            final int i4 = bottom;
            AnonymousClass11 r1 = new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onLayoutSurface(SessionCallbackRecord.this.mSession, i, i2, i3, i4);
                }
            };
            handler.post(r1);
        }

        /* access modifiers changed from: package-private */
        public void postSessionEvent(final String eventType, final Bundle eventArgs) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onSessionEvent(SessionCallbackRecord.this.mSession, eventType, eventArgs);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postTimeShiftStatusChanged(final int status) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onTimeShiftStatusChanged(SessionCallbackRecord.this.mSession, status);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postTimeShiftStartPositionChanged(final long timeMs) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onTimeShiftStartPositionChanged(SessionCallbackRecord.this.mSession, timeMs);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postTimeShiftCurrentPositionChanged(final long timeMs) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onTimeShiftCurrentPositionChanged(SessionCallbackRecord.this.mSession, timeMs);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postTuned(final Uri channelUri) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onTuned(SessionCallbackRecord.this.mSession, channelUri);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postRecordingStopped(final Uri recordedProgramUri) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onRecordingStopped(SessionCallbackRecord.this.mSession, recordedProgramUri);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void postError(final int error) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onError(SessionCallbackRecord.this.mSession, error);
                }
            });
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TimeShiftStatus {
    }

    public static abstract class TvInputCallback {
        public void onInputStateChanged(String inputId, int state) {
        }

        public void onInputAdded(String inputId) {
        }

        public void onInputRemoved(String inputId) {
        }

        public void onInputUpdated(String inputId) {
        }

        public void onTvInputInfoUpdated(TvInputInfo inputInfo) {
        }
    }

    private static final class TvInputCallbackRecord {
        /* access modifiers changed from: private */
        public final TvInputCallback mCallback;
        private final Handler mHandler;

        public TvInputCallbackRecord(TvInputCallback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        public TvInputCallback getCallback() {
            return this.mCallback;
        }

        public void postInputAdded(final String inputId) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    TvInputCallbackRecord.this.mCallback.onInputAdded(inputId);
                }
            });
        }

        public void postInputRemoved(final String inputId) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    TvInputCallbackRecord.this.mCallback.onInputRemoved(inputId);
                }
            });
        }

        public void postInputUpdated(final String inputId) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    TvInputCallbackRecord.this.mCallback.onInputUpdated(inputId);
                }
            });
        }

        public void postInputStateChanged(final String inputId, final int state) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    TvInputCallbackRecord.this.mCallback.onInputStateChanged(inputId, state);
                }
            });
        }

        public void postTvInputInfoUpdated(final TvInputInfo inputInfo) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    TvInputCallbackRecord.this.mCallback.onTvInputInfoUpdated(inputInfo);
                }
            });
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface VideoUnavailableReason {
    }

    public TvInputManager(ITvInputManager service, int userId) {
        this.mService = service;
        this.mUserId = userId;
        this.mClient = new ITvInputClient.Stub() {
            public void onSessionCreated(String inputId, IBinder token, InputChannel channel, int seq) {
                IBinder iBinder = token;
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    try {
                        int i = seq;
                        SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(i);
                        if (record == null) {
                            Log.e(TvInputManager.TAG, "Callback not found for " + iBinder);
                            return;
                        }
                        Session session = null;
                        if (iBinder != null) {
                            Session session2 = new Session(iBinder, channel, TvInputManager.this.mService, TvInputManager.this.mUserId, i, TvInputManager.this.mSessionCallbackRecordMap);
                            session = session2;
                        }
                        record.postSessionCreated(session);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            }

            public void onSessionReleased(int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    TvInputManager.this.mSessionCallbackRecordMap.delete(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq:" + seq);
                        return;
                    }
                    record.mSession.releaseInternal();
                    record.postSessionReleased();
                }
            }

            public void onChannelRetuned(Uri channelUri, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postChannelRetuned(channelUri);
                }
            }

            /* JADX WARNING: Code restructure failed: missing block: B:12:0x003e, code lost:
                return;
             */
            public void onTracksChanged(List<TvTrackInfo> tracks, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                    } else if (record.mSession.updateTracks(tracks)) {
                        record.postTracksChanged(tracks);
                        postVideoSizeChangedIfNeededLocked(record);
                    }
                }
            }

            /* JADX WARNING: Code restructure failed: missing block: B:12:0x003e, code lost:
                return;
             */
            public void onTrackSelected(int type, String trackId, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                    } else if (record.mSession.updateTrackSelection(type, trackId)) {
                        record.postTrackSelected(type, trackId);
                        postVideoSizeChangedIfNeededLocked(record);
                    }
                }
            }

            private void postVideoSizeChangedIfNeededLocked(SessionCallbackRecord record) {
                TvTrackInfo track = record.mSession.getVideoTrackToNotify();
                if (track != null) {
                    record.postVideoSizeChanged(track.getVideoWidth(), track.getVideoHeight());
                }
            }

            public void onVideoAvailable(int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postVideoAvailable();
                }
            }

            public void onVideoUnavailable(int reason, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postVideoUnavailable(reason);
                }
            }

            public void onContentAllowed(int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postContentAllowed();
                }
            }

            public void onContentBlocked(String rating, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postContentBlocked(TvContentRating.unflattenFromString(rating));
                }
            }

            public void onLayoutSurface(int left, int top, int right, int bottom, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postLayoutSurface(left, top, right, bottom);
                }
            }

            public void onSessionEvent(String eventType, Bundle eventArgs, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postSessionEvent(eventType, eventArgs);
                }
            }

            public void onTimeShiftStatusChanged(int status, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postTimeShiftStatusChanged(status);
                }
            }

            public void onTimeShiftStartPositionChanged(long timeMs, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postTimeShiftStartPositionChanged(timeMs);
                }
            }

            public void onTimeShiftCurrentPositionChanged(long timeMs, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postTimeShiftCurrentPositionChanged(timeMs);
                }
            }

            public void onTuned(int seq, Uri channelUri) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postTuned(channelUri);
                }
            }

            public void onRecordingStopped(Uri recordedProgramUri, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postRecordingStopped(recordedProgramUri);
                }
            }

            public void onError(int error, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    record.postError(error);
                }
            }
        };
        ITvInputManagerCallback managerCallback = new ITvInputManagerCallback.Stub() {
            public void onInputAdded(String inputId) {
                synchronized (TvInputManager.this.mLock) {
                    TvInputManager.this.mStateMap.put(inputId, 0);
                    for (TvInputCallbackRecord record : TvInputManager.this.mCallbackRecords) {
                        record.postInputAdded(inputId);
                    }
                }
            }

            public void onInputRemoved(String inputId) {
                synchronized (TvInputManager.this.mLock) {
                    TvInputManager.this.mStateMap.remove(inputId);
                    for (TvInputCallbackRecord record : TvInputManager.this.mCallbackRecords) {
                        record.postInputRemoved(inputId);
                    }
                }
            }

            public void onInputUpdated(String inputId) {
                synchronized (TvInputManager.this.mLock) {
                    for (TvInputCallbackRecord record : TvInputManager.this.mCallbackRecords) {
                        record.postInputUpdated(inputId);
                    }
                }
            }

            public void onInputStateChanged(String inputId, int state) {
                synchronized (TvInputManager.this.mLock) {
                    TvInputManager.this.mStateMap.put(inputId, Integer.valueOf(state));
                    for (TvInputCallbackRecord record : TvInputManager.this.mCallbackRecords) {
                        record.postInputStateChanged(inputId, state);
                    }
                }
            }

            public void onTvInputInfoUpdated(TvInputInfo inputInfo) {
                synchronized (TvInputManager.this.mLock) {
                    for (TvInputCallbackRecord record : TvInputManager.this.mCallbackRecords) {
                        record.postTvInputInfoUpdated(inputInfo);
                    }
                }
            }
        };
        try {
            if (this.mService != null) {
                this.mService.registerCallback(managerCallback, this.mUserId);
                List<TvInputInfo> infos = this.mService.getTvInputList(this.mUserId);
                synchronized (this.mLock) {
                    for (TvInputInfo info : infos) {
                        String inputId = info.getId();
                        this.mStateMap.put(inputId, Integer.valueOf(this.mService.getTvInputState(inputId, this.mUserId)));
                    }
                }
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<TvInputInfo> getTvInputList() {
        try {
            return this.mService.getTvInputList(this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public TvInputInfo getTvInputInfo(String inputId) {
        Preconditions.checkNotNull(inputId);
        try {
            return this.mService.getTvInputInfo(inputId, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updateTvInputInfo(TvInputInfo inputInfo) {
        Preconditions.checkNotNull(inputInfo);
        try {
            this.mService.updateTvInputInfo(inputInfo, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getInputState(String inputId) {
        Preconditions.checkNotNull(inputId);
        synchronized (this.mLock) {
            Integer state = this.mStateMap.get(inputId);
            if (state == null) {
                Log.w(TAG, "Unrecognized input ID: " + inputId);
                return 2;
            }
            int intValue = state.intValue();
            return intValue;
        }
    }

    public void registerCallback(TvInputCallback callback, Handler handler) {
        Preconditions.checkNotNull(callback);
        Preconditions.checkNotNull(handler);
        synchronized (this.mLock) {
            this.mCallbackRecords.add(new TvInputCallbackRecord(callback, handler));
        }
    }

    public void unregisterCallback(TvInputCallback callback) {
        Preconditions.checkNotNull(callback);
        synchronized (this.mLock) {
            Iterator<TvInputCallbackRecord> it = this.mCallbackRecords.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (it.next().getCallback() == callback) {
                    it.remove();
                    break;
                }
            }
        }
    }

    public boolean isParentalControlsEnabled() {
        try {
            return this.mService.isParentalControlsEnabled(this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setParentalControlsEnabled(boolean enabled) {
        try {
            this.mService.setParentalControlsEnabled(enabled, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isRatingBlocked(TvContentRating rating) {
        Preconditions.checkNotNull(rating);
        try {
            return this.mService.isRatingBlocked(rating.flattenToString(), this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<TvContentRating> getBlockedRatings() {
        try {
            List<TvContentRating> ratings = new ArrayList<>();
            for (String rating : this.mService.getBlockedRatings(this.mUserId)) {
                ratings.add(TvContentRating.unflattenFromString(rating));
            }
            return ratings;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void addBlockedRating(TvContentRating rating) {
        Preconditions.checkNotNull(rating);
        try {
            this.mService.addBlockedRating(rating.flattenToString(), this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void removeBlockedRating(TvContentRating rating) {
        Preconditions.checkNotNull(rating);
        try {
            this.mService.removeBlockedRating(rating.flattenToString(), this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<TvContentRatingSystemInfo> getTvContentRatingSystemList() {
        try {
            return this.mService.getTvContentRatingSystemList(this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void notifyPreviewProgramBrowsableDisabled(String packageName, long programId) {
        Intent intent = new Intent();
        intent.setAction(TvContract.ACTION_PREVIEW_PROGRAM_BROWSABLE_DISABLED);
        intent.putExtra(TvContract.EXTRA_PREVIEW_PROGRAM_ID, programId);
        intent.setPackage(packageName);
        try {
            this.mService.sendTvInputNotifyIntent(intent, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void notifyWatchNextProgramBrowsableDisabled(String packageName, long programId) {
        Intent intent = new Intent();
        intent.setAction(TvContract.ACTION_WATCH_NEXT_PROGRAM_BROWSABLE_DISABLED);
        intent.putExtra(TvContract.EXTRA_WATCH_NEXT_PROGRAM_ID, programId);
        intent.setPackage(packageName);
        try {
            this.mService.sendTvInputNotifyIntent(intent, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void notifyPreviewProgramAddedToWatchNext(String packageName, long previewProgramId, long watchNextProgramId) {
        Intent intent = new Intent();
        intent.setAction(TvContract.ACTION_PREVIEW_PROGRAM_ADDED_TO_WATCH_NEXT);
        intent.putExtra(TvContract.EXTRA_PREVIEW_PROGRAM_ID, previewProgramId);
        intent.putExtra(TvContract.EXTRA_WATCH_NEXT_PROGRAM_ID, watchNextProgramId);
        intent.setPackage(packageName);
        try {
            this.mService.sendTvInputNotifyIntent(intent, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void createSession(String inputId, SessionCallback callback, Handler handler) {
        createSessionInternal(inputId, false, callback, handler);
    }

    public void createRecordingSession(String inputId, SessionCallback callback, Handler handler) {
        createSessionInternal(inputId, true, callback, handler);
    }

    private void createSessionInternal(String inputId, boolean isRecordingSession, SessionCallback callback, Handler handler) {
        Preconditions.checkNotNull(inputId);
        Preconditions.checkNotNull(callback);
        Preconditions.checkNotNull(handler);
        SessionCallbackRecord record = new SessionCallbackRecord(callback, handler);
        synchronized (this.mSessionCallbackRecordMap) {
            int seq = this.mNextSeq;
            this.mNextSeq = seq + 1;
            this.mSessionCallbackRecordMap.put(seq, record);
            try {
                this.mService.createSession(this.mClient, inputId, isRecordingSession, seq, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @SystemApi
    public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId) {
        try {
            return this.mService.getAvailableTvStreamConfigList(inputId, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean captureFrame(String inputId, Surface surface, TvStreamConfig config) {
        try {
            return this.mService.captureFrame(inputId, surface, config, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isSingleSessionActive() {
        try {
            return this.mService.isSingleSessionActive(this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public List<TvInputHardwareInfo> getHardwareList() {
        try {
            return this.mService.getHardwareList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public Hardware acquireTvInputHardware(int deviceId, HardwareCallback callback, TvInputInfo info) {
        return acquireTvInputHardware(deviceId, info, callback);
    }

    @SystemApi
    public Hardware acquireTvInputHardware(int deviceId, TvInputInfo info, final HardwareCallback callback) {
        try {
            return new Hardware(this.mService.acquireTvInputHardware(deviceId, new ITvInputHardwareCallback.Stub() {
                public void onReleased() {
                    callback.onReleased();
                }

                public void onStreamConfigChanged(TvStreamConfig[] configs) {
                    callback.onStreamConfigChanged(configs);
                }
            }, info, this.mUserId));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void releaseTvInputHardware(int deviceId, Hardware hardware) {
        try {
            this.mService.releaseTvInputHardware(deviceId, hardware.getInterface(), this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<DvbDeviceInfo> getDvbDeviceList() {
        try {
            return this.mService.getDvbDeviceList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ParcelFileDescriptor openDvbDevice(DvbDeviceInfo info, int device) {
        if (device < 0 || 2 < device) {
            throw new IllegalArgumentException("Invalid DVB device: " + device);
        }
        try {
            return this.mService.openDvbDevice(info, device);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestChannelBrowsable(Uri channelUri) {
        try {
            this.mService.requestChannelBrowsable(channelUri, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
