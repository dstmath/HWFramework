package android.media.tv;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.PlaybackParams;
import android.media.tv.ITvInputService;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;

public abstract class TvInputService extends Service {
    private static final boolean DEBUG = false;
    private static final int DETACH_OVERLAY_VIEW_TIMEOUT_MS = 5000;
    public static final String SERVICE_INTERFACE = "android.media.tv.TvInputService";
    public static final String SERVICE_META_DATA = "android.media.tv.input";
    private static final String TAG = "TvInputService";
    /* access modifiers changed from: private */
    public final RemoteCallbackList<ITvInputServiceCallback> mCallbacks = new RemoteCallbackList<>();
    /* access modifiers changed from: private */
    public final Handler mServiceHandler = new ServiceHandler();
    private TvInputManager mTvInputManager;

    public static abstract class HardwareSession extends Session {
        /* access modifiers changed from: private */
        public TvInputManager.Session mHardwareSession;
        /* access modifiers changed from: private */
        public final TvInputManager.SessionCallback mHardwareSessionCallback = new TvInputManager.SessionCallback() {
            public void onSessionCreated(TvInputManager.Session session) {
                TvInputManager.Session unused = HardwareSession.this.mHardwareSession = session;
                SomeArgs args = SomeArgs.obtain();
                if (session != null) {
                    args.arg1 = HardwareSession.this;
                    args.arg2 = HardwareSession.this.mProxySession;
                    args.arg3 = HardwareSession.this.mProxySessionCallback;
                    args.arg4 = session.getToken();
                    session.tune(TvContract.buildChannelUriForPassthroughInput(HardwareSession.this.getHardwareInputId()));
                } else {
                    args.arg1 = null;
                    args.arg2 = null;
                    args.arg3 = HardwareSession.this.mProxySessionCallback;
                    args.arg4 = null;
                    HardwareSession.this.onRelease();
                }
                HardwareSession.this.mServiceHandler.obtainMessage(2, args).sendToTarget();
            }

            public void onVideoAvailable(TvInputManager.Session session) {
                if (HardwareSession.this.mHardwareSession == session) {
                    HardwareSession.this.onHardwareVideoAvailable();
                }
            }

            public void onVideoUnavailable(TvInputManager.Session session, int reason) {
                if (HardwareSession.this.mHardwareSession == session) {
                    HardwareSession.this.onHardwareVideoUnavailable(reason);
                }
            }
        };
        /* access modifiers changed from: private */
        public ITvInputSession mProxySession;
        /* access modifiers changed from: private */
        public ITvInputSessionCallback mProxySessionCallback;
        /* access modifiers changed from: private */
        public Handler mServiceHandler;

        public abstract String getHardwareInputId();

        public HardwareSession(Context context) {
            super(context);
        }

        public final boolean onSetSurface(Surface surface) {
            Log.e(TvInputService.TAG, "onSetSurface() should not be called in HardwareProxySession.");
            return false;
        }

        public void onHardwareVideoAvailable() {
        }

        public void onHardwareVideoUnavailable(int reason) {
        }

        /* access modifiers changed from: package-private */
        public void release() {
            if (this.mHardwareSession != null) {
                this.mHardwareSession.release();
                this.mHardwareSession = null;
            }
            super.release();
        }
    }

    private static final class OverlayViewCleanUpTask extends AsyncTask<View, Void, Void> {
        private OverlayViewCleanUpTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(View... views) {
            View overlayViewParent = views[0];
            try {
                Thread.sleep(5000);
                if (!isCancelled() && overlayViewParent.isAttachedToWindow()) {
                    Log.e(TvInputService.TAG, "Time out on releasing overlay view. Killing " + overlayViewParent.getContext().getPackageName());
                    Process.killProcess(Process.myPid());
                }
                return null;
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

    public static abstract class RecordingSession {
        final Handler mHandler;
        private final Object mLock = new Object();
        private final List<Runnable> mPendingActions = new ArrayList();
        /* access modifiers changed from: private */
        public ITvInputSessionCallback mSessionCallback;

        public abstract void onRelease();

        public abstract void onStartRecording(Uri uri);

        public abstract void onStopRecording();

        public abstract void onTune(Uri uri);

        public RecordingSession(Context context) {
            this.mHandler = new Handler(context.getMainLooper());
        }

        public void notifyTuned(final Uri channelUri) {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (RecordingSession.this.mSessionCallback != null) {
                            RecordingSession.this.mSessionCallback.onTuned(channelUri);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyTuned", e);
                    }
                }
            });
        }

        public void notifyRecordingStopped(final Uri recordedProgramUri) {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (RecordingSession.this.mSessionCallback != null) {
                            RecordingSession.this.mSessionCallback.onRecordingStopped(recordedProgramUri);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyRecordingStopped", e);
                    }
                }
            });
        }

        public void notifyError(int error) {
            if (error < 0 || error > 2) {
                Log.w(TvInputService.TAG, "notifyError - invalid error code (" + error + ") is changed to RECORDING_ERROR_UNKNOWN.");
                error = 0;
            }
            final int validError = error;
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (RecordingSession.this.mSessionCallback != null) {
                            RecordingSession.this.mSessionCallback.onError(validError);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyError", e);
                    }
                }
            });
        }

        @SystemApi
        public void notifySessionEvent(final String eventType, final Bundle eventArgs) {
            Preconditions.checkNotNull(eventType);
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (RecordingSession.this.mSessionCallback != null) {
                            RecordingSession.this.mSessionCallback.onSessionEvent(eventType, eventArgs);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in sending event (event=" + eventType + ")", e);
                    }
                }
            });
        }

        public void onTune(Uri channelUri, Bundle params) {
            onTune(channelUri);
        }

        public void onAppPrivateCommand(String action, Bundle data) {
        }

        /* access modifiers changed from: package-private */
        public void tune(Uri channelUri, Bundle params) {
            onTune(channelUri, params);
        }

        /* access modifiers changed from: package-private */
        public void release() {
            onRelease();
        }

        /* access modifiers changed from: package-private */
        public void startRecording(Uri programUri) {
            onStartRecording(programUri);
        }

        /* access modifiers changed from: package-private */
        public void stopRecording() {
            onStopRecording();
        }

        /* access modifiers changed from: package-private */
        public void appPrivateCommand(String action, Bundle data) {
            onAppPrivateCommand(action, data);
        }

        /* access modifiers changed from: private */
        public void initialize(ITvInputSessionCallback callback) {
            synchronized (this.mLock) {
                this.mSessionCallback = callback;
                for (Runnable runnable : this.mPendingActions) {
                    runnable.run();
                }
                this.mPendingActions.clear();
            }
        }

        private void executeOrPostRunnableOnMainThread(Runnable action) {
            synchronized (this.mLock) {
                if (this.mSessionCallback == null) {
                    this.mPendingActions.add(action);
                } else if (this.mHandler.getLooper().isCurrentThread()) {
                    action.run();
                } else {
                    this.mHandler.post(action);
                }
            }
        }
    }

    @SuppressLint({"HandlerLeak"})
    private final class ServiceHandler extends Handler {
        private static final int DO_ADD_HARDWARE_INPUT = 4;
        private static final int DO_ADD_HDMI_INPUT = 6;
        private static final int DO_CREATE_RECORDING_SESSION = 3;
        private static final int DO_CREATE_SESSION = 1;
        private static final int DO_NOTIFY_SESSION_CREATED = 2;
        private static final int DO_REMOVE_HARDWARE_INPUT = 5;
        private static final int DO_REMOVE_HDMI_INPUT = 7;

        private ServiceHandler() {
        }

        private void broadcastAddHardwareInput(int deviceId, TvInputInfo inputInfo) {
            int n = TvInputService.this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((ITvInputServiceCallback) TvInputService.this.mCallbacks.getBroadcastItem(i)).addHardwareInput(deviceId, inputInfo);
                } catch (RemoteException e) {
                    Log.e(TvInputService.TAG, "error in broadcastAddHardwareInput", e);
                }
            }
            TvInputService.this.mCallbacks.finishBroadcast();
        }

        private void broadcastAddHdmiInput(int id, TvInputInfo inputInfo) {
            int n = TvInputService.this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((ITvInputServiceCallback) TvInputService.this.mCallbacks.getBroadcastItem(i)).addHdmiInput(id, inputInfo);
                } catch (RemoteException e) {
                    Log.e(TvInputService.TAG, "error in broadcastAddHdmiInput", e);
                }
            }
            TvInputService.this.mCallbacks.finishBroadcast();
        }

        private void broadcastRemoveHardwareInput(String inputId) {
            int n = TvInputService.this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((ITvInputServiceCallback) TvInputService.this.mCallbacks.getBroadcastItem(i)).removeHardwareInput(inputId);
                } catch (RemoteException e) {
                    Log.e(TvInputService.TAG, "error in broadcastRemoveHardwareInput", e);
                }
            }
            TvInputService.this.mCallbacks.finishBroadcast();
        }

        public final void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    InputChannel channel = (InputChannel) args.arg1;
                    ITvInputSessionCallback cb = (ITvInputSessionCallback) args.arg2;
                    args.recycle();
                    Session sessionImpl = TvInputService.this.onCreateSession((String) args.arg3);
                    if (sessionImpl == null) {
                        try {
                            cb.onSessionCreated(null, null);
                        } catch (RemoteException e) {
                            Log.e(TvInputService.TAG, "error in onSessionCreated", e);
                        }
                        return;
                    }
                    ITvInputSession stub = new ITvInputSessionWrapper(TvInputService.this, sessionImpl, channel);
                    if (sessionImpl instanceof HardwareSession) {
                        HardwareSession proxySession = (HardwareSession) sessionImpl;
                        String hardwareInputId = proxySession.getHardwareInputId();
                        if (TextUtils.isEmpty(hardwareInputId) || !TvInputService.this.isPassthroughInput(hardwareInputId)) {
                            if (TextUtils.isEmpty(hardwareInputId)) {
                                Log.w(TvInputService.TAG, "Hardware input id is not setup yet.");
                            } else {
                                Log.w(TvInputService.TAG, "Invalid hardware input id : " + hardwareInputId);
                            }
                            sessionImpl.onRelease();
                            try {
                                cb.onSessionCreated(null, null);
                            } catch (RemoteException e2) {
                                Log.e(TvInputService.TAG, "error in onSessionCreated", e2);
                            }
                            return;
                        }
                        ITvInputSession unused = proxySession.mProxySession = stub;
                        ITvInputSessionCallback unused2 = proxySession.mProxySessionCallback = cb;
                        Handler unused3 = proxySession.mServiceHandler = TvInputService.this.mServiceHandler;
                        ((TvInputManager) TvInputService.this.getSystemService(Context.TV_INPUT_SERVICE)).createSession(hardwareInputId, proxySession.mHardwareSessionCallback, TvInputService.this.mServiceHandler);
                    } else {
                        HardwareSession proxySession2 = SomeArgs.obtain();
                        proxySession2.arg1 = sessionImpl;
                        proxySession2.arg2 = stub;
                        proxySession2.arg3 = cb;
                        proxySession2.arg4 = null;
                        TvInputService.this.mServiceHandler.obtainMessage(2, proxySession2).sendToTarget();
                    }
                    return;
                case 2:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    Session sessionImpl2 = (Session) args2.arg1;
                    ITvInputSession stub2 = (ITvInputSession) args2.arg2;
                    ITvInputSessionCallback cb2 = (ITvInputSessionCallback) args2.arg3;
                    try {
                        cb2.onSessionCreated(stub2, (IBinder) args2.arg4);
                    } catch (RemoteException e3) {
                        Log.e(TvInputService.TAG, "error in onSessionCreated", e3);
                    }
                    if (sessionImpl2 != null) {
                        sessionImpl2.initialize(cb2);
                    }
                    args2.recycle();
                    return;
                case 3:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    ITvInputSessionCallback cb3 = (ITvInputSessionCallback) args3.arg1;
                    args3.recycle();
                    RecordingSession recordingSessionImpl = TvInputService.this.onCreateRecordingSession((String) args3.arg2);
                    if (recordingSessionImpl == null) {
                        try {
                            cb3.onSessionCreated(null, null);
                        } catch (RemoteException e4) {
                            Log.e(TvInputService.TAG, "error in onSessionCreated", e4);
                        }
                        return;
                    }
                    try {
                        cb3.onSessionCreated(new ITvInputSessionWrapper(TvInputService.this, recordingSessionImpl), null);
                    } catch (RemoteException e5) {
                        Log.e(TvInputService.TAG, "error in onSessionCreated", e5);
                    }
                    recordingSessionImpl.initialize(cb3);
                    return;
                case 4:
                    TvInputHardwareInfo hardwareInfo = (TvInputHardwareInfo) msg.obj;
                    TvInputInfo inputInfo = TvInputService.this.onHardwareAdded(hardwareInfo);
                    if (inputInfo != null) {
                        broadcastAddHardwareInput(hardwareInfo.getDeviceId(), inputInfo);
                    }
                    return;
                case 5:
                    String inputId = TvInputService.this.onHardwareRemoved((TvInputHardwareInfo) msg.obj);
                    if (inputId != null) {
                        broadcastRemoveHardwareInput(inputId);
                    }
                    return;
                case 6:
                    HdmiDeviceInfo deviceInfo = (HdmiDeviceInfo) msg.obj;
                    TvInputInfo inputInfo2 = TvInputService.this.onHdmiDeviceAdded(deviceInfo);
                    if (inputInfo2 != null) {
                        broadcastAddHdmiInput(deviceInfo.getId(), inputInfo2);
                    }
                    return;
                case 7:
                    String inputId2 = TvInputService.this.onHdmiDeviceRemoved((HdmiDeviceInfo) msg.obj);
                    if (inputId2 != null) {
                        broadcastRemoveHardwareInput(inputId2);
                    }
                    return;
                default:
                    Log.w(TvInputService.TAG, "Unhandled message code: " + msg.what);
                    return;
            }
        }
    }

    public static abstract class Session implements KeyEvent.Callback {
        private static final int POSITION_UPDATE_INTERVAL_MS = 1000;
        private final Context mContext;
        /* access modifiers changed from: private */
        public long mCurrentPositionMs = Long.MIN_VALUE;
        private final KeyEvent.DispatcherState mDispatcherState = new KeyEvent.DispatcherState();
        final Handler mHandler;
        private final Object mLock = new Object();
        /* access modifiers changed from: private */
        public Rect mOverlayFrame;
        private View mOverlayView;
        private OverlayViewCleanUpTask mOverlayViewCleanUpTask;
        private FrameLayout mOverlayViewContainer;
        /* access modifiers changed from: private */
        public boolean mOverlayViewEnabled;
        private final List<Runnable> mPendingActions = new ArrayList();
        /* access modifiers changed from: private */
        public ITvInputSessionCallback mSessionCallback;
        /* access modifiers changed from: private */
        public long mStartPositionMs = Long.MIN_VALUE;
        private Surface mSurface;
        /* access modifiers changed from: private */
        public final TimeShiftPositionTrackingRunnable mTimeShiftPositionTrackingRunnable = new TimeShiftPositionTrackingRunnable();
        private final WindowManager mWindowManager;
        private WindowManager.LayoutParams mWindowParams;
        /* access modifiers changed from: private */
        public IBinder mWindowToken;

        private final class TimeShiftPositionTrackingRunnable implements Runnable {
            private TimeShiftPositionTrackingRunnable() {
            }

            public void run() {
                long startPositionMs = Session.this.onTimeShiftGetStartPosition();
                if (Session.this.mStartPositionMs == Long.MIN_VALUE || Session.this.mStartPositionMs != startPositionMs) {
                    long unused = Session.this.mStartPositionMs = startPositionMs;
                    Session.this.notifyTimeShiftStartPositionChanged(startPositionMs);
                }
                long currentPositionMs = Session.this.onTimeShiftGetCurrentPosition();
                if (currentPositionMs < Session.this.mStartPositionMs) {
                    Log.w(TvInputService.TAG, "Current position (" + currentPositionMs + ") cannot be earlier than start position (" + Session.this.mStartPositionMs + "). Reset to the start position.");
                    currentPositionMs = Session.this.mStartPositionMs;
                }
                if (Session.this.mCurrentPositionMs == Long.MIN_VALUE || Session.this.mCurrentPositionMs != currentPositionMs) {
                    long unused2 = Session.this.mCurrentPositionMs = currentPositionMs;
                    Session.this.notifyTimeShiftCurrentPositionChanged(currentPositionMs);
                }
                Session.this.mHandler.removeCallbacks(Session.this.mTimeShiftPositionTrackingRunnable);
                Session.this.mHandler.postDelayed(Session.this.mTimeShiftPositionTrackingRunnable, 1000);
            }
        }

        public abstract void onRelease();

        public abstract void onSetCaptionEnabled(boolean z);

        public abstract void onSetStreamVolume(float f);

        public abstract boolean onSetSurface(Surface surface);

        public abstract boolean onTune(Uri uri);

        public Session(Context context) {
            this.mContext = context;
            this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            this.mHandler = new Handler(context.getMainLooper());
        }

        public void setOverlayViewEnabled(final boolean enable) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (enable != Session.this.mOverlayViewEnabled) {
                        boolean unused = Session.this.mOverlayViewEnabled = enable;
                        if (!enable) {
                            Session.this.removeOverlayView(false);
                        } else if (Session.this.mWindowToken != null) {
                            Session.this.createOverlayView(Session.this.mWindowToken, Session.this.mOverlayFrame);
                        }
                    }
                }
            });
        }

        @SystemApi
        public void notifySessionEvent(final String eventType, final Bundle eventArgs) {
            Preconditions.checkNotNull(eventType);
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onSessionEvent(eventType, eventArgs);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in sending event (event=" + eventType + ")", e);
                    }
                }
            });
        }

        public void notifyChannelRetuned(final Uri channelUri) {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onChannelRetuned(channelUri);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyChannelRetuned", e);
                    }
                }
            });
        }

        public void notifyTracksChanged(List<TvTrackInfo> tracks) {
            final List<TvTrackInfo> tracksCopy = new ArrayList<>(tracks);
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onTracksChanged(tracksCopy);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyTracksChanged", e);
                    }
                }
            });
        }

        public void notifyTrackSelected(final int type, final String trackId) {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onTrackSelected(type, trackId);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyTrackSelected", e);
                    }
                }
            });
        }

        public void notifyVideoAvailable() {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onVideoAvailable();
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyVideoAvailable", e);
                    }
                }
            });
        }

        public void notifyVideoUnavailable(final int reason) {
            if (reason < 0 || reason > 4) {
                Log.e(TvInputService.TAG, "notifyVideoUnavailable - unknown reason: " + reason);
            }
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onVideoUnavailable(reason);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyVideoUnavailable", e);
                    }
                }
            });
        }

        public void notifyContentAllowed() {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onContentAllowed();
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyContentAllowed", e);
                    }
                }
            });
        }

        public void notifyContentBlocked(final TvContentRating rating) {
            Preconditions.checkNotNull(rating);
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onContentBlocked(rating.flattenToString());
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyContentBlocked", e);
                    }
                }
            });
        }

        public void notifyTimeShiftStatusChanged(final int status) {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    Session.this.timeShiftEnablePositionTracking(status == 3);
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onTimeShiftStatusChanged(status);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyTimeShiftStatusChanged", e);
                    }
                }
            });
        }

        /* access modifiers changed from: private */
        public void notifyTimeShiftStartPositionChanged(final long timeMs) {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onTimeShiftStartPositionChanged(timeMs);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyTimeShiftStartPositionChanged", e);
                    }
                }
            });
        }

        /* access modifiers changed from: private */
        public void notifyTimeShiftCurrentPositionChanged(final long timeMs) {
            executeOrPostRunnableOnMainThread(new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onTimeShiftCurrentPositionChanged(timeMs);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyTimeShiftCurrentPositionChanged", e);
                    }
                }
            });
        }

        public void layoutSurface(int left, int top, int right, int bottom) {
            if (left > right || top > bottom) {
                throw new IllegalArgumentException("Invalid parameter");
            }
            final int i = left;
            final int i2 = top;
            final int i3 = right;
            final int i4 = bottom;
            AnonymousClass13 r0 = new Runnable() {
                public void run() {
                    try {
                        if (Session.this.mSessionCallback != null) {
                            Session.this.mSessionCallback.onLayoutSurface(i, i2, i3, i4);
                        }
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in layoutSurface", e);
                    }
                }
            };
            executeOrPostRunnableOnMainThread(r0);
        }

        @SystemApi
        public void onSetMain(boolean isMain) {
        }

        public void onSurfaceChanged(int format, int width, int height) {
        }

        public void onOverlayViewSizeChanged(int width, int height) {
        }

        public boolean onTune(Uri channelUri, Bundle params) {
            return onTune(channelUri);
        }

        public void onUnblockContent(TvContentRating unblockedRating) {
        }

        public boolean onSelectTrack(int type, String trackId) {
            return false;
        }

        public void onAppPrivateCommand(String action, Bundle data) {
        }

        public View onCreateOverlayView() {
            return null;
        }

        public void onTimeShiftPlay(Uri recordedProgramUri) {
        }

        public void onTimeShiftPause() {
        }

        public void onTimeShiftResume() {
        }

        public void onTimeShiftSeekTo(long timeMs) {
        }

        public void onTimeShiftSetPlaybackParams(PlaybackParams params) {
        }

        public long onTimeShiftGetStartPosition() {
            return Long.MIN_VALUE;
        }

        public long onTimeShiftGetCurrentPosition() {
            return Long.MIN_VALUE;
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return false;
        }

        public boolean onKeyLongPress(int keyCode, KeyEvent event) {
            return false;
        }

        public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
            return false;
        }

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            return false;
        }

        public boolean onTouchEvent(MotionEvent event) {
            return false;
        }

        public boolean onTrackballEvent(MotionEvent event) {
            return false;
        }

        public boolean onGenericMotionEvent(MotionEvent event) {
            return false;
        }

        /* access modifiers changed from: package-private */
        public void release() {
            onRelease();
            if (this.mSurface != null) {
                this.mSurface.release();
                this.mSurface = null;
            }
            synchronized (this.mLock) {
                this.mSessionCallback = null;
                this.mPendingActions.clear();
            }
            removeOverlayView(true);
            this.mHandler.removeCallbacks(this.mTimeShiftPositionTrackingRunnable);
        }

        /* access modifiers changed from: package-private */
        public void setMain(boolean isMain) {
            onSetMain(isMain);
        }

        /* access modifiers changed from: package-private */
        public void setSurface(Surface surface) {
            onSetSurface(surface);
            if (this.mSurface != null) {
                this.mSurface.release();
            }
            this.mSurface = surface;
        }

        /* access modifiers changed from: package-private */
        public void dispatchSurfaceChanged(int format, int width, int height) {
            onSurfaceChanged(format, width, height);
        }

        /* access modifiers changed from: package-private */
        public void setStreamVolume(float volume) {
            onSetStreamVolume(volume);
        }

        /* access modifiers changed from: package-private */
        public void tune(Uri channelUri, Bundle params) {
            this.mCurrentPositionMs = Long.MIN_VALUE;
            onTune(channelUri, params);
        }

        /* access modifiers changed from: package-private */
        public void setCaptionEnabled(boolean enabled) {
            onSetCaptionEnabled(enabled);
        }

        /* access modifiers changed from: package-private */
        public void selectTrack(int type, String trackId) {
            onSelectTrack(type, trackId);
        }

        /* access modifiers changed from: package-private */
        public void unblockContent(String unblockedRating) {
            onUnblockContent(TvContentRating.unflattenFromString(unblockedRating));
        }

        /* access modifiers changed from: package-private */
        public void appPrivateCommand(String action, Bundle data) {
            onAppPrivateCommand(action, data);
        }

        /* access modifiers changed from: package-private */
        public void createOverlayView(IBinder windowToken, Rect frame) {
            if (this.mOverlayViewContainer != null) {
                removeOverlayView(false);
            }
            this.mWindowToken = windowToken;
            this.mOverlayFrame = frame;
            onOverlayViewSizeChanged(frame.right - frame.left, frame.bottom - frame.top);
            if (this.mOverlayViewEnabled) {
                this.mOverlayView = onCreateOverlayView();
                if (this.mOverlayView != null) {
                    if (this.mOverlayViewCleanUpTask != null) {
                        this.mOverlayViewCleanUpTask.cancel(true);
                        this.mOverlayViewCleanUpTask = null;
                    }
                    this.mOverlayViewContainer = new FrameLayout(this.mContext.getApplicationContext());
                    this.mOverlayViewContainer.addView(this.mOverlayView);
                    int flags = 536;
                    if (ActivityManager.isHighEndGfx()) {
                        flags = 536 | 16777216;
                    }
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(frame.right - frame.left, frame.bottom - frame.top, frame.left, frame.top, 1004, flags, -2);
                    this.mWindowParams = layoutParams;
                    this.mWindowParams.privateFlags |= 64;
                    this.mWindowParams.gravity = 8388659;
                    this.mWindowParams.token = windowToken;
                    this.mWindowManager.addView(this.mOverlayViewContainer, this.mWindowParams);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void relayoutOverlayView(Rect frame) {
            if (!(this.mOverlayFrame != null && this.mOverlayFrame.width() == frame.width() && this.mOverlayFrame.height() == frame.height())) {
                onOverlayViewSizeChanged(frame.right - frame.left, frame.bottom - frame.top);
            }
            this.mOverlayFrame = frame;
            if (this.mOverlayViewEnabled && this.mOverlayViewContainer != null) {
                this.mWindowParams.x = frame.left;
                this.mWindowParams.y = frame.top;
                this.mWindowParams.width = frame.right - frame.left;
                this.mWindowParams.height = frame.bottom - frame.top;
                this.mWindowManager.updateViewLayout(this.mOverlayViewContainer, this.mWindowParams);
            }
        }

        /* access modifiers changed from: package-private */
        public void removeOverlayView(boolean clearWindowToken) {
            if (clearWindowToken) {
                this.mWindowToken = null;
                this.mOverlayFrame = null;
            }
            if (this.mOverlayViewContainer != null) {
                this.mOverlayViewContainer.removeView(this.mOverlayView);
                this.mOverlayView = null;
                this.mWindowManager.removeView(this.mOverlayViewContainer);
                this.mOverlayViewContainer = null;
                this.mWindowParams = null;
            }
        }

        /* access modifiers changed from: package-private */
        public void timeShiftPlay(Uri recordedProgramUri) {
            this.mCurrentPositionMs = 0;
            onTimeShiftPlay(recordedProgramUri);
        }

        /* access modifiers changed from: package-private */
        public void timeShiftPause() {
            onTimeShiftPause();
        }

        /* access modifiers changed from: package-private */
        public void timeShiftResume() {
            onTimeShiftResume();
        }

        /* access modifiers changed from: package-private */
        public void timeShiftSeekTo(long timeMs) {
            onTimeShiftSeekTo(timeMs);
        }

        /* access modifiers changed from: package-private */
        public void timeShiftSetPlaybackParams(PlaybackParams params) {
            onTimeShiftSetPlaybackParams(params);
        }

        /* access modifiers changed from: package-private */
        public void timeShiftEnablePositionTracking(boolean enable) {
            if (enable) {
                this.mHandler.post(this.mTimeShiftPositionTrackingRunnable);
                return;
            }
            this.mHandler.removeCallbacks(this.mTimeShiftPositionTrackingRunnable);
            this.mStartPositionMs = Long.MIN_VALUE;
            this.mCurrentPositionMs = Long.MIN_VALUE;
        }

        /* access modifiers changed from: package-private */
        public void scheduleOverlayViewCleanup() {
            View overlayViewParent = this.mOverlayViewContainer;
            if (overlayViewParent != null) {
                this.mOverlayViewCleanUpTask = new OverlayViewCleanUpTask();
                this.mOverlayViewCleanUpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new View[]{overlayViewParent});
            }
        }

        /* access modifiers changed from: package-private */
        public int dispatchInputEvent(InputEvent event, InputEventReceiver receiver) {
            boolean isNavigationKey = false;
            boolean skipDispatchToOverlayView = false;
            if (event instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent) event;
                if (keyEvent.dispatch(this, this.mDispatcherState, this)) {
                    return 1;
                }
                isNavigationKey = TvInputService.isNavigationKey(keyEvent.getKeyCode());
                skipDispatchToOverlayView = KeyEvent.isMediaKey(keyEvent.getKeyCode()) || keyEvent.getKeyCode() == 222;
            } else if (event instanceof MotionEvent) {
                MotionEvent motionEvent = (MotionEvent) event;
                int source = motionEvent.getSource();
                if (motionEvent.isTouchEvent()) {
                    if (onTouchEvent(motionEvent)) {
                        return 1;
                    }
                } else if ((source & 4) != 0) {
                    if (onTrackballEvent(motionEvent)) {
                        return 1;
                    }
                } else if (onGenericMotionEvent(motionEvent)) {
                    return 1;
                }
            }
            if (this.mOverlayViewContainer == null || !this.mOverlayViewContainer.isAttachedToWindow() || skipDispatchToOverlayView) {
                return 0;
            }
            if (!this.mOverlayViewContainer.hasWindowFocus()) {
                this.mOverlayViewContainer.getViewRootImpl().windowFocusChanged(true, true);
            }
            if (!isNavigationKey || !this.mOverlayViewContainer.hasFocusable()) {
                this.mOverlayViewContainer.getViewRootImpl().dispatchInputEvent(event, receiver);
                return -1;
            }
            this.mOverlayViewContainer.getViewRootImpl().dispatchInputEvent(event);
            return 1;
        }

        /* access modifiers changed from: private */
        public void initialize(ITvInputSessionCallback callback) {
            synchronized (this.mLock) {
                this.mSessionCallback = callback;
                for (Runnable runnable : this.mPendingActions) {
                    runnable.run();
                }
                this.mPendingActions.clear();
            }
        }

        private void executeOrPostRunnableOnMainThread(Runnable action) {
            synchronized (this.mLock) {
                if (this.mSessionCallback == null) {
                    this.mPendingActions.add(action);
                } else if (this.mHandler.getLooper().isCurrentThread()) {
                    action.run();
                } else {
                    this.mHandler.post(action);
                }
            }
        }
    }

    public abstract Session onCreateSession(String str);

    public final IBinder onBind(Intent intent) {
        return new ITvInputService.Stub() {
            public void registerCallback(ITvInputServiceCallback cb) {
                if (cb != null) {
                    TvInputService.this.mCallbacks.register(cb);
                }
            }

            public void unregisterCallback(ITvInputServiceCallback cb) {
                if (cb != null) {
                    TvInputService.this.mCallbacks.unregister(cb);
                }
            }

            public void createSession(InputChannel channel, ITvInputSessionCallback cb, String inputId) {
                if (channel == null) {
                    Log.w(TvInputService.TAG, "Creating session without input channel");
                }
                if (cb != null) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = channel;
                    args.arg2 = cb;
                    args.arg3 = inputId;
                    TvInputService.this.mServiceHandler.obtainMessage(1, args).sendToTarget();
                }
            }

            public void createRecordingSession(ITvInputSessionCallback cb, String inputId) {
                if (cb != null) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = cb;
                    args.arg2 = inputId;
                    TvInputService.this.mServiceHandler.obtainMessage(3, args).sendToTarget();
                }
            }

            public void notifyHardwareAdded(TvInputHardwareInfo hardwareInfo) {
                TvInputService.this.mServiceHandler.obtainMessage(4, hardwareInfo).sendToTarget();
            }

            public void notifyHardwareRemoved(TvInputHardwareInfo hardwareInfo) {
                TvInputService.this.mServiceHandler.obtainMessage(5, hardwareInfo).sendToTarget();
            }

            public void notifyHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) {
                TvInputService.this.mServiceHandler.obtainMessage(6, deviceInfo).sendToTarget();
            }

            public void notifyHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) {
                TvInputService.this.mServiceHandler.obtainMessage(7, deviceInfo).sendToTarget();
            }
        };
    }

    public RecordingSession onCreateRecordingSession(String inputId) {
        return null;
    }

    @SystemApi
    public TvInputInfo onHardwareAdded(TvInputHardwareInfo hardwareInfo) {
        return null;
    }

    @SystemApi
    public String onHardwareRemoved(TvInputHardwareInfo hardwareInfo) {
        return null;
    }

    @SystemApi
    public TvInputInfo onHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) {
        return null;
    }

    @SystemApi
    public String onHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) {
        return null;
    }

    /* access modifiers changed from: private */
    public boolean isPassthroughInput(String inputId) {
        if (this.mTvInputManager == null) {
            this.mTvInputManager = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
        }
        TvInputInfo info = this.mTvInputManager.getTvInputInfo(inputId);
        return info != null && info.isPassthroughInput();
    }

    public static boolean isNavigationKey(int keyCode) {
        switch (keyCode) {
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 61:
            case 62:
            case 66:
            case 92:
            case 93:
            case 122:
            case 123:
                return true;
            default:
                return false;
        }
    }
}
