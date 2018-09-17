package android.media.tv;

import android.graphics.Rect;
import android.media.PlaybackParams;
import android.media.tv.ITvInputHardwareCallback.Stub;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech.Engine;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pools.Pool;
import android.util.Pools.SimplePool;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventSender;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import com.android.internal.util.Preconditions;
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
    private final List<TvInputCallbackRecord> mCallbackRecords;
    private final ITvInputClient mClient;
    private final Object mLock;
    private int mNextSeq;
    private final ITvInputManager mService;
    private final SparseArray<SessionCallbackRecord> mSessionCallbackRecordMap;
    private final Map<String, Integer> mStateMap;
    private final int mUserId;

    /* renamed from: android.media.tv.TvInputManager.3 */
    class AnonymousClass3 extends Stub {
        final /* synthetic */ HardwareCallback val$callback;

        AnonymousClass3(HardwareCallback val$callback) {
            this.val$callback = val$callback;
        }

        public void onReleased() {
            this.val$callback.onReleased();
        }

        public void onStreamConfigChanged(TvStreamConfig[] configs) {
            this.val$callback.onStreamConfigChanged(configs);
        }
    }

    public static final class Hardware {
        private final ITvInputHardware mInterface;

        private Hardware(ITvInputHardware hardwareInterface) {
            this.mInterface = hardwareInterface;
        }

        private ITvInputHardware getInterface() {
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

        public boolean dispatchKeyEventToHdmi(KeyEvent event) {
            try {
                return this.mInterface.dispatchKeyEventToHdmi(event);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        public void overrideAudioSink(int audioType, String audioAddress, int samplingRate, int channelMask, int format) {
            try {
                this.mInterface.overrideAudioSink(audioType, audioAddress, samplingRate, channelMask, format);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static abstract class HardwareCallback {
        public abstract void onReleased();

        public abstract void onStreamConfigChanged(TvStreamConfig[] tvStreamConfigArr);
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
        private final Pool<PendingEvent> mPendingEventPool;
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
                    case MSG_SEND_INPUT_EVENT /*1*/:
                        Session.this.sendInputEventAndReportResultOnMainLooper((PendingEvent) msg.obj);
                    case MSG_TIMEOUT_INPUT_EVENT /*2*/:
                        int seq = msg.arg1;
                        if (msg.obj instanceof PendingEvent) {
                            PendingEvent p = msg.obj;
                            seq = p.mEvent == null ? msg.arg1 : p.mEvent.getSequenceNumber();
                        }
                        Session.this.finishedInputEvent(seq, false, true);
                    case MSG_FLUSH_INPUT_EVENT /*3*/:
                        Session.this.finishedInputEvent(msg.arg1, false, false);
                    default:
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
            this.mPendingEventPool = new SimplePool(20);
            this.mPendingEvents = new SparseArray(20);
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

        void setMain() {
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
            } else if (volume < 0.0f || volume > Engine.DEFAULT_VOLUME) {
                try {
                    throw new IllegalArgumentException("volume should be between 0.0f and 1.0f");
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } else {
                this.mService.setVolume(this.mToken, volume, this.mUserId);
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
                this.mVideoWidth = DISPATCH_NOT_HANDLED;
                this.mVideoHeight = DISPATCH_NOT_HANDLED;
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

        public void selectTrack(int type, String trackId) {
            synchronized (this.mMetadataLock) {
                if (type == 0) {
                    if (trackId != null) {
                        if (!containsTrack(this.mAudioTracks, trackId)) {
                            Log.w(TvInputManager.TAG, "Invalid audio trackId: " + trackId);
                            return;
                        }
                    }
                } else if (type != DISPATCH_HANDLED) {
                    if (type != TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL) {
                        throw new IllegalArgumentException("invalid type: " + type);
                    } else if (trackId != null) {
                        if (!containsTrack(this.mSubtitleTracks, trackId)) {
                            Log.w(TvInputManager.TAG, "Invalid subtitle trackId: " + trackId);
                            return;
                        }
                    }
                } else if (trackId != null) {
                    if (!containsTrack(this.mVideoTracks, trackId)) {
                        Log.w(TvInputManager.TAG, "Invalid video trackId: " + trackId);
                        return;
                    }
                }
                if (this.mToken == null) {
                    Log.w(TvInputManager.TAG, "The session has been already released");
                    return;
                }
                try {
                    this.mService.selectTrack(this.mToken, type, trackId, this.mUserId);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
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
                List arrayList;
                if (type == 0) {
                    if (this.mAudioTracks == null) {
                        return null;
                    }
                    arrayList = new ArrayList(this.mAudioTracks);
                    return arrayList;
                } else if (type == DISPATCH_HANDLED) {
                    if (this.mVideoTracks == null) {
                        return null;
                    }
                    arrayList = new ArrayList(this.mVideoTracks);
                    return arrayList;
                } else if (type != TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL) {
                    throw new IllegalArgumentException("invalid type: " + type);
                } else if (this.mSubtitleTracks == null) {
                    return null;
                } else {
                    arrayList = new ArrayList(this.mSubtitleTracks);
                    return arrayList;
                }
            }
        }

        public String getSelectedTrack(int type) {
            synchronized (this.mMetadataLock) {
                String str;
                if (type == 0) {
                    str = this.mSelectedAudioTrackId;
                    return str;
                } else if (type == DISPATCH_HANDLED) {
                    str = this.mSelectedVideoTrackId;
                    return str;
                } else if (type == TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL) {
                    str = this.mSelectedSubtitleTrackId;
                    return str;
                } else {
                    throw new IllegalArgumentException("invalid type: " + type);
                }
            }
        }

        boolean updateTracks(List<TvTrackInfo> tracks) {
            boolean z = false;
            synchronized (this.mMetadataLock) {
                this.mAudioTracks.clear();
                this.mVideoTracks.clear();
                this.mSubtitleTracks.clear();
                for (TvTrackInfo track : tracks) {
                    if (track.getType() == 0) {
                        this.mAudioTracks.add(track);
                    } else if (track.getType() == DISPATCH_HANDLED) {
                        this.mVideoTracks.add(track);
                    } else if (track.getType() == TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL) {
                        this.mSubtitleTracks.add(track);
                    }
                }
                if (!this.mAudioTracks.isEmpty() || !this.mVideoTracks.isEmpty()) {
                    z = true;
                } else if (!this.mSubtitleTracks.isEmpty()) {
                    z = true;
                }
            }
            return z;
        }

        boolean updateTrackSelection(int type, String trackId) {
            synchronized (this.mMetadataLock) {
                if (type == 0) {
                    if (!TextUtils.equals(trackId, this.mSelectedAudioTrackId)) {
                        this.mSelectedAudioTrackId = trackId;
                        return true;
                    }
                }
                if (type == DISPATCH_HANDLED && !TextUtils.equals(trackId, this.mSelectedVideoTrackId)) {
                    this.mSelectedVideoTrackId = trackId;
                    return true;
                } else if (type != TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL || TextUtils.equals(trackId, this.mSelectedSubtitleTrackId)) {
                    return false;
                } else {
                    this.mSelectedSubtitleTrackId = trackId;
                    return true;
                }
            }
        }

        TvTrackInfo getVideoTrackToNotify() {
            synchronized (this.mMetadataLock) {
                if (!(this.mVideoTracks.isEmpty() || this.mSelectedVideoTrackId == null)) {
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
                return null;
            }
        }

        void timeShiftPlay(Uri recordedProgramUri) {
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

        void timeShiftPause() {
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

        void timeShiftResume() {
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

        void timeShiftSeekTo(long timeMs) {
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

        void timeShiftSetPlaybackParams(PlaybackParams params) {
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

        void timeShiftEnablePositionTracking(boolean enable) {
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

        void startRecording(Uri programUri) {
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

        void stopRecording() {
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

        void createOverlayView(View view, Rect frame) {
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

        void relayoutOverlayView(Rect frame) {
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

        void removeOverlayView() {
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

        void unblockContent(TvContentRating unblockedRating) {
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
                    return DISPATCH_NOT_HANDLED;
                }
                PendingEvent p = obtainPendingEventLocked(event, token, callback, handler);
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    int sendInputEventOnMainLooperLocked = sendInputEventOnMainLooperLocked(p);
                    return sendInputEventOnMainLooperLocked;
                }
                Message msg = this.mHandler.obtainMessage(DISPATCH_HANDLED, p);
                msg.setAsynchronous(true);
                this.mHandler.sendMessage(msg);
                return DISPATCH_IN_PROGRESS;
            }
        }

        private void sendInputEventAndReportResultOnMainLooper(PendingEvent p) {
            synchronized (this.mHandler) {
                if (sendInputEventOnMainLooperLocked(p) == DISPATCH_IN_PROGRESS) {
                    return;
                }
                invokeFinishedInputEventCallback(p, false);
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
                    Message msg = this.mHandler.obtainMessage(TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL, p);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageDelayed(msg, INPUT_SESSION_NOT_RESPONDING_TIMEOUT);
                    return DISPATCH_IN_PROGRESS;
                }
                Log.w(TvInputManager.TAG, "Unable to send input event to session: " + this.mToken + " dropping:" + event);
            }
            return DISPATCH_NOT_HANDLED;
        }

        void finishedInputEvent(int seq, boolean handled, boolean timeout) {
            synchronized (this.mHandler) {
                int index = this.mPendingEvents.indexOfKey(seq);
                if (index < 0) {
                    return;
                }
                PendingEvent p = (PendingEvent) this.mPendingEvents.valueAt(index);
                this.mPendingEvents.removeAt(index);
                if (timeout) {
                    Log.w(TvInputManager.TAG, "Timeout waiting for session to handle input event after 2500 ms: " + this.mToken);
                } else {
                    this.mHandler.removeMessages(TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL, p);
                }
                invokeFinishedInputEventCallback(p, handled);
            }
        }

        void invokeFinishedInputEventCallback(PendingEvent p, boolean handled) {
            p.mHandled = handled;
            if (p.mEventHandler.getLooper().isCurrentThread()) {
                p.run();
                return;
            }
            Message msg = Message.obtain(p.mEventHandler, (Runnable) p);
            msg.setAsynchronous(true);
            msg.sendToTarget();
        }

        private void flushPendingEventsLocked() {
            this.mHandler.removeMessages(TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING);
            int count = this.mPendingEvents.size();
            for (int i = DISPATCH_NOT_HANDLED; i < count; i += DISPATCH_HANDLED) {
                Message msg = this.mHandler.obtainMessage(TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING, this.mPendingEvents.keyAt(i), DISPATCH_NOT_HANDLED);
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

        private void recyclePendingEventLocked(PendingEvent p) {
            p.recycle();
            this.mPendingEventPool.release(p);
        }

        IBinder getToken() {
            return this.mToken;
        }

        private void releaseInternal() {
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

        void onTuned(Session session, Uri channelUri) {
        }

        void onRecordingStopped(Session session, Uri recordedProgramUri) {
        }

        void onError(Session session, int error) {
        }
    }

    private static final class SessionCallbackRecord {
        private final Handler mHandler;
        private Session mSession;
        private final SessionCallback mSessionCallback;

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.10 */
        class AnonymousClass10 implements Runnable {
            final /* synthetic */ TvContentRating val$rating;

            AnonymousClass10(TvContentRating val$rating) {
                this.val$rating = val$rating;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onContentBlocked(SessionCallbackRecord.this.mSession, this.val$rating);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.11 */
        class AnonymousClass11 implements Runnable {
            final /* synthetic */ int val$bottom;
            final /* synthetic */ int val$left;
            final /* synthetic */ int val$right;
            final /* synthetic */ int val$top;

            AnonymousClass11(int val$left, int val$top, int val$right, int val$bottom) {
                this.val$left = val$left;
                this.val$top = val$top;
                this.val$right = val$right;
                this.val$bottom = val$bottom;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onLayoutSurface(SessionCallbackRecord.this.mSession, this.val$left, this.val$top, this.val$right, this.val$bottom);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.12 */
        class AnonymousClass12 implements Runnable {
            final /* synthetic */ Bundle val$eventArgs;
            final /* synthetic */ String val$eventType;

            AnonymousClass12(String val$eventType, Bundle val$eventArgs) {
                this.val$eventType = val$eventType;
                this.val$eventArgs = val$eventArgs;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onSessionEvent(SessionCallbackRecord.this.mSession, this.val$eventType, this.val$eventArgs);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.13 */
        class AnonymousClass13 implements Runnable {
            final /* synthetic */ int val$status;

            AnonymousClass13(int val$status) {
                this.val$status = val$status;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onTimeShiftStatusChanged(SessionCallbackRecord.this.mSession, this.val$status);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.14 */
        class AnonymousClass14 implements Runnable {
            final /* synthetic */ long val$timeMs;

            AnonymousClass14(long val$timeMs) {
                this.val$timeMs = val$timeMs;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onTimeShiftStartPositionChanged(SessionCallbackRecord.this.mSession, this.val$timeMs);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.15 */
        class AnonymousClass15 implements Runnable {
            final /* synthetic */ long val$timeMs;

            AnonymousClass15(long val$timeMs) {
                this.val$timeMs = val$timeMs;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onTimeShiftCurrentPositionChanged(SessionCallbackRecord.this.mSession, this.val$timeMs);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.16 */
        class AnonymousClass16 implements Runnable {
            final /* synthetic */ Uri val$channelUri;

            AnonymousClass16(Uri val$channelUri) {
                this.val$channelUri = val$channelUri;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onTuned(SessionCallbackRecord.this.mSession, this.val$channelUri);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.17 */
        class AnonymousClass17 implements Runnable {
            final /* synthetic */ Uri val$recordedProgramUri;

            AnonymousClass17(Uri val$recordedProgramUri) {
                this.val$recordedProgramUri = val$recordedProgramUri;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onRecordingStopped(SessionCallbackRecord.this.mSession, this.val$recordedProgramUri);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.18 */
        class AnonymousClass18 implements Runnable {
            final /* synthetic */ int val$error;

            AnonymousClass18(int val$error) {
                this.val$error = val$error;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onError(SessionCallbackRecord.this.mSession, this.val$error);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ Session val$session;

            AnonymousClass1(Session val$session) {
                this.val$session = val$session;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onSessionCreated(this.val$session);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ Uri val$channelUri;

            AnonymousClass3(Uri val$channelUri) {
                this.val$channelUri = val$channelUri;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onChannelRetuned(SessionCallbackRecord.this.mSession, this.val$channelUri);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ List val$tracks;

            AnonymousClass4(List val$tracks) {
                this.val$tracks = val$tracks;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onTracksChanged(SessionCallbackRecord.this.mSession, this.val$tracks);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ String val$trackId;
            final /* synthetic */ int val$type;

            AnonymousClass5(int val$type, String val$trackId) {
                this.val$type = val$type;
                this.val$trackId = val$trackId;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onTrackSelected(SessionCallbackRecord.this.mSession, this.val$type, this.val$trackId);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.6 */
        class AnonymousClass6 implements Runnable {
            final /* synthetic */ int val$height;
            final /* synthetic */ int val$width;

            AnonymousClass6(int val$width, int val$height) {
                this.val$width = val$width;
                this.val$height = val$height;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onVideoSizeChanged(SessionCallbackRecord.this.mSession, this.val$width, this.val$height);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.SessionCallbackRecord.8 */
        class AnonymousClass8 implements Runnable {
            final /* synthetic */ int val$reason;

            AnonymousClass8(int val$reason) {
                this.val$reason = val$reason;
            }

            public void run() {
                SessionCallbackRecord.this.mSessionCallback.onVideoUnavailable(SessionCallbackRecord.this.mSession, this.val$reason);
            }
        }

        SessionCallbackRecord(SessionCallback sessionCallback, Handler handler) {
            this.mSessionCallback = sessionCallback;
            this.mHandler = handler;
        }

        void postSessionCreated(Session session) {
            this.mSession = session;
            this.mHandler.post(new AnonymousClass1(session));
        }

        void postSessionReleased() {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onSessionReleased(SessionCallbackRecord.this.mSession);
                }
            });
        }

        void postChannelRetuned(Uri channelUri) {
            this.mHandler.post(new AnonymousClass3(channelUri));
        }

        void postTracksChanged(List<TvTrackInfo> tracks) {
            this.mHandler.post(new AnonymousClass4(tracks));
        }

        void postTrackSelected(int type, String trackId) {
            this.mHandler.post(new AnonymousClass5(type, trackId));
        }

        void postVideoSizeChanged(int width, int height) {
            this.mHandler.post(new AnonymousClass6(width, height));
        }

        void postVideoAvailable() {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onVideoAvailable(SessionCallbackRecord.this.mSession);
                }
            });
        }

        void postVideoUnavailable(int reason) {
            this.mHandler.post(new AnonymousClass8(reason));
        }

        void postContentAllowed() {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SessionCallbackRecord.this.mSessionCallback.onContentAllowed(SessionCallbackRecord.this.mSession);
                }
            });
        }

        void postContentBlocked(TvContentRating rating) {
            this.mHandler.post(new AnonymousClass10(rating));
        }

        void postLayoutSurface(int left, int top, int right, int bottom) {
            this.mHandler.post(new AnonymousClass11(left, top, right, bottom));
        }

        void postSessionEvent(String eventType, Bundle eventArgs) {
            this.mHandler.post(new AnonymousClass12(eventType, eventArgs));
        }

        void postTimeShiftStatusChanged(int status) {
            this.mHandler.post(new AnonymousClass13(status));
        }

        void postTimeShiftStartPositionChanged(long timeMs) {
            this.mHandler.post(new AnonymousClass14(timeMs));
        }

        void postTimeShiftCurrentPositionChanged(long timeMs) {
            this.mHandler.post(new AnonymousClass15(timeMs));
        }

        void postTuned(Uri channelUri) {
            this.mHandler.post(new AnonymousClass16(channelUri));
        }

        void postRecordingStopped(Uri recordedProgramUri) {
            this.mHandler.post(new AnonymousClass17(recordedProgramUri));
        }

        void postError(int error) {
            this.mHandler.post(new AnonymousClass18(error));
        }
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
        private final TvInputCallback mCallback;
        private final Handler mHandler;

        /* renamed from: android.media.tv.TvInputManager.TvInputCallbackRecord.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ String val$inputId;

            AnonymousClass1(String val$inputId) {
                this.val$inputId = val$inputId;
            }

            public void run() {
                TvInputCallbackRecord.this.mCallback.onInputAdded(this.val$inputId);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.TvInputCallbackRecord.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ String val$inputId;

            AnonymousClass2(String val$inputId) {
                this.val$inputId = val$inputId;
            }

            public void run() {
                TvInputCallbackRecord.this.mCallback.onInputRemoved(this.val$inputId);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.TvInputCallbackRecord.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ String val$inputId;

            AnonymousClass3(String val$inputId) {
                this.val$inputId = val$inputId;
            }

            public void run() {
                TvInputCallbackRecord.this.mCallback.onInputUpdated(this.val$inputId);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.TvInputCallbackRecord.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ String val$inputId;
            final /* synthetic */ int val$state;

            AnonymousClass4(String val$inputId, int val$state) {
                this.val$inputId = val$inputId;
                this.val$state = val$state;
            }

            public void run() {
                TvInputCallbackRecord.this.mCallback.onInputStateChanged(this.val$inputId, this.val$state);
            }
        }

        /* renamed from: android.media.tv.TvInputManager.TvInputCallbackRecord.5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ TvInputInfo val$inputInfo;

            AnonymousClass5(TvInputInfo val$inputInfo) {
                this.val$inputInfo = val$inputInfo;
            }

            public void run() {
                TvInputCallbackRecord.this.mCallback.onTvInputInfoUpdated(this.val$inputInfo);
            }
        }

        public TvInputCallbackRecord(TvInputCallback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        public TvInputCallback getCallback() {
            return this.mCallback;
        }

        public void postInputAdded(String inputId) {
            this.mHandler.post(new AnonymousClass1(inputId));
        }

        public void postInputRemoved(String inputId) {
            this.mHandler.post(new AnonymousClass2(inputId));
        }

        public void postInputUpdated(String inputId) {
            this.mHandler.post(new AnonymousClass3(inputId));
        }

        public void postInputStateChanged(String inputId, int state) {
            this.mHandler.post(new AnonymousClass4(inputId, state));
        }

        public void postTvInputInfoUpdated(TvInputInfo inputInfo) {
            this.mHandler.post(new AnonymousClass5(inputInfo));
        }
    }

    public TvInputManager(ITvInputManager service, int userId) {
        this.mLock = new Object();
        this.mCallbackRecords = new LinkedList();
        this.mStateMap = new ArrayMap();
        this.mSessionCallbackRecordMap = new SparseArray();
        this.mService = service;
        this.mUserId = userId;
        this.mClient = new ITvInputClient.Stub() {
            public void onSessionCreated(String inputId, IBinder token, InputChannel channel, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for " + token);
                        return;
                    }
                    Session session = null;
                    if (token != null) {
                        session = new Session(channel, TvInputManager.this.mService, TvInputManager.this.mUserId, seq, TvInputManager.this.mSessionCallbackRecordMap, null);
                    }
                    record.postSessionCreated(session);
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

            public void onTracksChanged(List<TvTrackInfo> tracks, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    if (record.mSession.updateTracks(tracks)) {
                        record.postTracksChanged(tracks);
                        postVideoSizeChangedIfNeededLocked(record);
                    }
                }
            }

            public void onTrackSelected(int type, String trackId, int seq) {
                synchronized (TvInputManager.this.mSessionCallbackRecordMap) {
                    SessionCallbackRecord record = (SessionCallbackRecord) TvInputManager.this.mSessionCallbackRecordMap.get(seq);
                    if (record == null) {
                        Log.e(TvInputManager.TAG, "Callback not found for seq " + seq);
                        return;
                    }
                    if (record.mSession.updateTrackSelection(type, trackId)) {
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
                    TvInputManager.this.mStateMap.put(inputId, Integer.valueOf(TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN));
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
            Integer state = (Integer) this.mStateMap.get(inputId);
            if (state == null) {
                Log.w(TAG, "Unrecognized input ID: " + inputId);
                return VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL;
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
            while (it.hasNext()) {
                if (((TvInputCallbackRecord) it.next()).getCallback() == callback) {
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

    public List<TvContentRating> getBlockedRatings() {
        try {
            List<TvContentRating> ratings = new ArrayList();
            for (String rating : this.mService.getBlockedRatings(this.mUserId)) {
                ratings.add(TvContentRating.unflattenFromString(rating));
            }
            return ratings;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addBlockedRating(TvContentRating rating) {
        Preconditions.checkNotNull(rating);
        try {
            this.mService.addBlockedRating(rating.flattenToString(), this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeBlockedRating(TvContentRating rating) {
        Preconditions.checkNotNull(rating);
        try {
            this.mService.removeBlockedRating(rating.flattenToString(), this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<TvContentRatingSystemInfo> getTvContentRatingSystemList() {
        try {
            return this.mService.getTvContentRatingSystemList(this.mUserId);
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
            this.mNextSeq = seq + VIDEO_UNAVAILABLE_REASON_TUNING;
            this.mSessionCallbackRecordMap.put(seq, record);
            try {
                this.mService.createSession(this.mClient, inputId, isRecordingSession, seq, this.mUserId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId) {
        try {
            return this.mService.getAvailableTvStreamConfigList(inputId, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean captureFrame(String inputId, Surface surface, TvStreamConfig config) {
        try {
            return this.mService.captureFrame(inputId, surface, config, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSingleSessionActive() {
        try {
            return this.mService.isSingleSessionActive(this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<TvInputHardwareInfo> getHardwareList() {
        try {
            return this.mService.getHardwareList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Hardware acquireTvInputHardware(int deviceId, HardwareCallback callback, TvInputInfo info) {
        return acquireTvInputHardware(deviceId, info, callback);
    }

    public Hardware acquireTvInputHardware(int deviceId, TvInputInfo info, HardwareCallback callback) {
        try {
            return new Hardware(null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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
        if (device >= 0 && VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL >= device) {
            return this.mService.openDvbDevice(info, device);
        }
        try {
            throw new IllegalArgumentException("Invalid DVB device: " + device);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
