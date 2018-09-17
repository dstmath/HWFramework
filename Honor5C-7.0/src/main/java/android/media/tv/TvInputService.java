package android.media.tv;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAssignedNumbers;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.PlaybackParams;
import android.media.ToneGenerator;
import android.media.tv.ITvInputService.Stub;
import android.media.tv.TvInputManager.SessionCallback;
import android.net.Uri;
import android.net.wifi.ScanResult.InformationElement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.renderscript.ScriptIntrinsicBLAS;
import android.rms.AppAssociate;
import android.rms.HwSysResource;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyEvent;
import android.view.KeyEvent.Callback;
import android.view.KeyEvent.DispatcherState;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
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
    private final RemoteCallbackList<ITvInputServiceCallback> mCallbacks;
    private final Handler mServiceHandler;
    private TvInputManager mTvInputManager;

    public static abstract class Session implements Callback {
        private static final int POSITION_UPDATE_INTERVAL_MS = 1000;
        private final Context mContext;
        private long mCurrentPositionMs;
        private final DispatcherState mDispatcherState;
        final Handler mHandler;
        private final Object mLock;
        private Rect mOverlayFrame;
        private View mOverlayView;
        private OverlayViewCleanUpTask mOverlayViewCleanUpTask;
        private FrameLayout mOverlayViewContainer;
        private boolean mOverlayViewEnabled;
        private final List<Runnable> mPendingActions;
        private ITvInputSessionCallback mSessionCallback;
        private long mStartPositionMs;
        private Surface mSurface;
        private final TimeShiftPositionTrackingRunnable mTimeShiftPositionTrackingRunnable;
        private final WindowManager mWindowManager;
        private LayoutParams mWindowParams;
        private IBinder mWindowToken;

        /* renamed from: android.media.tv.TvInputService.Session.10 */
        class AnonymousClass10 implements Runnable {
            final /* synthetic */ int val$status;

            AnonymousClass10(int val$status) {
                this.val$status = val$status;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onTimeShiftStatusChanged(this.val$status);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyTimeShiftStatusChanged", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.11 */
        class AnonymousClass11 implements Runnable {
            final /* synthetic */ long val$timeMs;

            AnonymousClass11(long val$timeMs) {
                this.val$timeMs = val$timeMs;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onTimeShiftStartPositionChanged(this.val$timeMs);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyTimeShiftStartPositionChanged", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.12 */
        class AnonymousClass12 implements Runnable {
            final /* synthetic */ long val$timeMs;

            AnonymousClass12(long val$timeMs) {
                this.val$timeMs = val$timeMs;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onTimeShiftCurrentPositionChanged(this.val$timeMs);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyTimeShiftCurrentPositionChanged", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.13 */
        class AnonymousClass13 implements Runnable {
            final /* synthetic */ int val$bottom;
            final /* synthetic */ int val$left;
            final /* synthetic */ int val$right;
            final /* synthetic */ int val$top;

            AnonymousClass13(int val$left, int val$top, int val$right, int val$bottom) {
                this.val$left = val$left;
                this.val$top = val$top;
                this.val$right = val$right;
                this.val$bottom = val$bottom;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onLayoutSurface(this.val$left, this.val$top, this.val$right, this.val$bottom);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in layoutSurface", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ boolean val$enable;

            AnonymousClass1(boolean val$enable) {
                this.val$enable = val$enable;
            }

            public void run() {
                if (this.val$enable != Session.this.mOverlayViewEnabled) {
                    Session.this.mOverlayViewEnabled = this.val$enable;
                    if (!this.val$enable) {
                        Session.this.removeOverlayView(TvInputService.DEBUG);
                    } else if (Session.this.mWindowToken != null) {
                        Session.this.createOverlayView(Session.this.mWindowToken, Session.this.mOverlayFrame);
                    }
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ Bundle val$eventArgs;
            final /* synthetic */ String val$eventType;

            AnonymousClass2(String val$eventType, Bundle val$eventArgs) {
                this.val$eventType = val$eventType;
                this.val$eventArgs = val$eventArgs;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onSessionEvent(this.val$eventType, this.val$eventArgs);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in sending event (event=" + this.val$eventType + ")", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ Uri val$channelUri;

            AnonymousClass3(Uri val$channelUri) {
                this.val$channelUri = val$channelUri;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onChannelRetuned(this.val$channelUri);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyChannelRetuned", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ List val$tracksCopy;

            AnonymousClass4(List val$tracksCopy) {
                this.val$tracksCopy = val$tracksCopy;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onTracksChanged(this.val$tracksCopy);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyTracksChanged", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ String val$trackId;
            final /* synthetic */ int val$type;

            AnonymousClass5(int val$type, String val$trackId) {
                this.val$type = val$type;
                this.val$trackId = val$trackId;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onTrackSelected(this.val$type, this.val$trackId);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyTrackSelected", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.7 */
        class AnonymousClass7 implements Runnable {
            final /* synthetic */ int val$reason;

            AnonymousClass7(int val$reason) {
                this.val$reason = val$reason;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onVideoUnavailable(this.val$reason);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyVideoUnavailable", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.Session.9 */
        class AnonymousClass9 implements Runnable {
            final /* synthetic */ TvContentRating val$rating;

            AnonymousClass9(TvContentRating val$rating) {
                this.val$rating = val$rating;
            }

            public void run() {
                try {
                    if (Session.this.mSessionCallback != null) {
                        Session.this.mSessionCallback.onContentBlocked(this.val$rating.flattenToString());
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyContentBlocked", e);
                }
            }
        }

        private final class TimeShiftPositionTrackingRunnable implements Runnable {
            private TimeShiftPositionTrackingRunnable() {
            }

            public void run() {
                long startPositionMs = Session.this.onTimeShiftGetStartPosition();
                if (Session.this.mStartPositionMs != startPositionMs) {
                    Session.this.mStartPositionMs = startPositionMs;
                    Session.this.notifyTimeShiftStartPositionChanged(startPositionMs);
                }
                long currentPositionMs = Session.this.onTimeShiftGetCurrentPosition();
                if (currentPositionMs < Session.this.mStartPositionMs) {
                    Log.w(TvInputService.TAG, "Current position (" + currentPositionMs + ") cannot be earlier than" + " start position (" + Session.this.mStartPositionMs + "). Reset to the start " + "position.");
                    currentPositionMs = Session.this.mStartPositionMs;
                }
                if (Session.this.mCurrentPositionMs != currentPositionMs) {
                    Session.this.mCurrentPositionMs = currentPositionMs;
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
            this.mDispatcherState = new DispatcherState();
            this.mTimeShiftPositionTrackingRunnable = new TimeShiftPositionTrackingRunnable();
            this.mLock = new Object();
            this.mPendingActions = new ArrayList();
            this.mContext = context;
            this.mWindowManager = (WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW);
            this.mHandler = new Handler(context.getMainLooper());
            this.mCurrentPositionMs = Long.MIN_VALUE;
        }

        public void setOverlayViewEnabled(boolean enable) {
            this.mHandler.post(new AnonymousClass1(enable));
        }

        public void notifySessionEvent(String eventType, Bundle eventArgs) {
            Preconditions.checkNotNull(eventType);
            executeOrPostRunnableOnMainThread(new AnonymousClass2(eventType, eventArgs));
        }

        public void notifyChannelRetuned(Uri channelUri) {
            executeOrPostRunnableOnMainThread(new AnonymousClass3(channelUri));
        }

        public void notifyTracksChanged(List<TvTrackInfo> tracks) {
            executeOrPostRunnableOnMainThread(new AnonymousClass4(new ArrayList(tracks)));
        }

        public void notifyTrackSelected(int type, String trackId) {
            executeOrPostRunnableOnMainThread(new AnonymousClass5(type, trackId));
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

        public void notifyVideoUnavailable(int reason) {
            if (reason < 0 || reason > 4) {
                Log.e(TvInputService.TAG, "notifyVideoUnavailable - unknown reason: " + reason);
            }
            executeOrPostRunnableOnMainThread(new AnonymousClass7(reason));
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

        public void notifyContentBlocked(TvContentRating rating) {
            Preconditions.checkNotNull(rating);
            executeOrPostRunnableOnMainThread(new AnonymousClass9(rating));
        }

        public void notifyTimeShiftStatusChanged(int status) {
            executeOrPostRunnableOnMainThread(new AnonymousClass10(status));
        }

        private void notifyTimeShiftStartPositionChanged(long timeMs) {
            executeOrPostRunnableOnMainThread(new AnonymousClass11(timeMs));
        }

        private void notifyTimeShiftCurrentPositionChanged(long timeMs) {
            executeOrPostRunnableOnMainThread(new AnonymousClass12(timeMs));
        }

        public void layoutSurface(int left, int top, int right, int bottom) {
            if (left > right || top > bottom) {
                throw new IllegalArgumentException("Invalid parameter");
            }
            executeOrPostRunnableOnMainThread(new AnonymousClass13(left, top, right, bottom));
        }

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
            return TvInputService.DEBUG;
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
            return TvInputService.DEBUG;
        }

        public boolean onKeyLongPress(int keyCode, KeyEvent event) {
            return TvInputService.DEBUG;
        }

        public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
            return TvInputService.DEBUG;
        }

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            return TvInputService.DEBUG;
        }

        public boolean onTouchEvent(MotionEvent event) {
            return TvInputService.DEBUG;
        }

        public boolean onTrackballEvent(MotionEvent event) {
            return TvInputService.DEBUG;
        }

        public boolean onGenericMotionEvent(MotionEvent event) {
            return TvInputService.DEBUG;
        }

        void release() {
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

        void setMain(boolean isMain) {
            onSetMain(isMain);
        }

        void setSurface(Surface surface) {
            onSetSurface(surface);
            if (this.mSurface != null) {
                this.mSurface.release();
            }
            this.mSurface = surface;
        }

        void dispatchSurfaceChanged(int format, int width, int height) {
            onSurfaceChanged(format, width, height);
        }

        void setStreamVolume(float volume) {
            onSetStreamVolume(volume);
        }

        void tune(Uri channelUri, Bundle params) {
            this.mCurrentPositionMs = Long.MIN_VALUE;
            onTune(channelUri, params);
        }

        void setCaptionEnabled(boolean enabled) {
            onSetCaptionEnabled(enabled);
        }

        void selectTrack(int type, String trackId) {
            onSelectTrack(type, trackId);
        }

        void unblockContent(String unblockedRating) {
            onUnblockContent(TvContentRating.unflattenFromString(unblockedRating));
        }

        void appPrivateCommand(String action, Bundle data) {
            onAppPrivateCommand(action, data);
        }

        void createOverlayView(IBinder windowToken, Rect frame) {
            if (this.mOverlayViewContainer != null) {
                removeOverlayView(TvInputService.DEBUG);
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
                        flags = 16777752;
                    }
                    this.mWindowParams = new LayoutParams(frame.right - frame.left, frame.bottom - frame.top, frame.left, frame.top, TvInputInfo.TYPE_COMPONENT, flags, -2);
                    LayoutParams layoutParams = this.mWindowParams;
                    layoutParams.privateFlags |= 64;
                    this.mWindowParams.gravity = 8388659;
                    this.mWindowParams.token = windowToken;
                    this.mWindowManager.addView(this.mOverlayViewContainer, this.mWindowParams);
                }
            }
        }

        void relayoutOverlayView(Rect frame) {
            if (this.mOverlayFrame != null && this.mOverlayFrame.width() == frame.width()) {
                if (this.mOverlayFrame.height() != frame.height()) {
                }
                this.mOverlayFrame = frame;
                if (!this.mOverlayViewEnabled && this.mOverlayViewContainer != null) {
                    this.mWindowParams.x = frame.left;
                    this.mWindowParams.y = frame.top;
                    this.mWindowParams.width = frame.right - frame.left;
                    this.mWindowParams.height = frame.bottom - frame.top;
                    this.mWindowManager.updateViewLayout(this.mOverlayViewContainer, this.mWindowParams);
                    return;
                }
            }
            onOverlayViewSizeChanged(frame.right - frame.left, frame.bottom - frame.top);
            this.mOverlayFrame = frame;
            if (!this.mOverlayViewEnabled) {
            }
        }

        void removeOverlayView(boolean clearWindowToken) {
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

        void timeShiftPlay(Uri recordedProgramUri) {
            this.mCurrentPositionMs = 0;
            onTimeShiftPlay(recordedProgramUri);
        }

        void timeShiftPause() {
            onTimeShiftPause();
        }

        void timeShiftResume() {
            onTimeShiftResume();
        }

        void timeShiftSeekTo(long timeMs) {
            onTimeShiftSeekTo(timeMs);
        }

        void timeShiftSetPlaybackParams(PlaybackParams params) {
            onTimeShiftSetPlaybackParams(params);
        }

        void timeShiftEnablePositionTracking(boolean enable) {
            if (enable) {
                this.mHandler.post(this.mTimeShiftPositionTrackingRunnable);
                return;
            }
            this.mHandler.removeCallbacks(this.mTimeShiftPositionTrackingRunnable);
            this.mStartPositionMs = Long.MIN_VALUE;
            this.mCurrentPositionMs = Long.MIN_VALUE;
        }

        void scheduleOverlayViewCleanup() {
            if (this.mOverlayViewContainer != null) {
                this.mOverlayViewCleanUpTask = new OverlayViewCleanUpTask();
                this.mOverlayViewCleanUpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, overlayViewParent);
            }
        }

        int dispatchInputEvent(InputEvent event, InputEventReceiver receiver) {
            boolean z = TvInputService.DEBUG;
            boolean skipDispatchToOverlayView = TvInputService.DEBUG;
            if (event instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent) event;
                if (keyEvent.dispatch(this, this.mDispatcherState, this)) {
                    return 1;
                }
                z = TvInputService.isNavigationKey(keyEvent.getKeyCode());
                skipDispatchToOverlayView = !KeyEvent.isMediaKey(keyEvent.getKeyCode()) ? keyEvent.getKeyCode() == BluetoothAssignedNumbers.MUZIK ? true : TvInputService.DEBUG : true;
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
            if (z && this.mOverlayViewContainer.hasFocusable()) {
                this.mOverlayViewContainer.getViewRootImpl().dispatchInputEvent(event);
                return 1;
            }
            this.mOverlayViewContainer.getViewRootImpl().dispatchInputEvent(event, receiver);
            return -1;
        }

        private void initialize(ITvInputSessionCallback callback) {
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

    public static abstract class HardwareSession extends Session {
        private android.media.tv.TvInputManager.Session mHardwareSession;
        private final SessionCallback mHardwareSessionCallback;
        private ITvInputSession mProxySession;
        private ITvInputSessionCallback mProxySessionCallback;
        private Handler mServiceHandler;

        public abstract String getHardwareInputId();

        public HardwareSession(Context context) {
            super(context);
            this.mHardwareSessionCallback = new SessionCallback() {
                public void onSessionCreated(android.media.tv.TvInputManager.Session session) {
                    HardwareSession.this.mHardwareSession = session;
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

                public void onVideoAvailable(android.media.tv.TvInputManager.Session session) {
                    if (HardwareSession.this.mHardwareSession == session) {
                        HardwareSession.this.onHardwareVideoAvailable();
                    }
                }

                public void onVideoUnavailable(android.media.tv.TvInputManager.Session session, int reason) {
                    if (HardwareSession.this.mHardwareSession == session) {
                        HardwareSession.this.onHardwareVideoUnavailable(reason);
                    }
                }
            };
        }

        public final boolean onSetSurface(Surface surface) {
            Log.e(TvInputService.TAG, "onSetSurface() should not be called in HardwareProxySession.");
            return TvInputService.DEBUG;
        }

        public void onHardwareVideoAvailable() {
        }

        public void onHardwareVideoUnavailable(int reason) {
        }

        void release() {
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

        protected Void doInBackground(View... views) {
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
        private final Object mLock;
        private final List<Runnable> mPendingActions;
        private ITvInputSessionCallback mSessionCallback;

        /* renamed from: android.media.tv.TvInputService.RecordingSession.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ Uri val$channelUri;

            AnonymousClass1(Uri val$channelUri) {
                this.val$channelUri = val$channelUri;
            }

            public void run() {
                try {
                    if (RecordingSession.this.mSessionCallback != null) {
                        RecordingSession.this.mSessionCallback.onTuned(this.val$channelUri);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyTuned", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.RecordingSession.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ Uri val$recordedProgramUri;

            AnonymousClass2(Uri val$recordedProgramUri) {
                this.val$recordedProgramUri = val$recordedProgramUri;
            }

            public void run() {
                try {
                    if (RecordingSession.this.mSessionCallback != null) {
                        RecordingSession.this.mSessionCallback.onRecordingStopped(this.val$recordedProgramUri);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyRecordingStopped", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.RecordingSession.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ int val$validError;

            AnonymousClass3(int val$validError) {
                this.val$validError = val$validError;
            }

            public void run() {
                try {
                    if (RecordingSession.this.mSessionCallback != null) {
                        RecordingSession.this.mSessionCallback.onError(this.val$validError);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in notifyError", e);
                }
            }
        }

        /* renamed from: android.media.tv.TvInputService.RecordingSession.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ Bundle val$eventArgs;
            final /* synthetic */ String val$eventType;

            AnonymousClass4(String val$eventType, Bundle val$eventArgs) {
                this.val$eventType = val$eventType;
                this.val$eventArgs = val$eventArgs;
            }

            public void run() {
                try {
                    if (RecordingSession.this.mSessionCallback != null) {
                        RecordingSession.this.mSessionCallback.onSessionEvent(this.val$eventType, this.val$eventArgs);
                    }
                } catch (RemoteException e) {
                    Log.w(TvInputService.TAG, "error in sending event (event=" + this.val$eventType + ")", e);
                }
            }
        }

        public abstract void onRelease();

        public abstract void onStartRecording(Uri uri);

        public abstract void onStopRecording();

        public abstract void onTune(Uri uri);

        public RecordingSession(Context context) {
            this.mLock = new Object();
            this.mPendingActions = new ArrayList();
            this.mHandler = new Handler(context.getMainLooper());
        }

        public void notifyTuned(Uri channelUri) {
            executeOrPostRunnableOnMainThread(new AnonymousClass1(channelUri));
        }

        public void notifyRecordingStopped(Uri recordedProgramUri) {
            executeOrPostRunnableOnMainThread(new AnonymousClass2(recordedProgramUri));
        }

        public void notifyError(int error) {
            if (error < 0 || error > 2) {
                Log.w(TvInputService.TAG, "notifyError - invalid error code (" + error + ") is changed to RECORDING_ERROR_UNKNOWN.");
                error = 0;
            }
            executeOrPostRunnableOnMainThread(new AnonymousClass3(error));
        }

        public void notifySessionEvent(String eventType, Bundle eventArgs) {
            Preconditions.checkNotNull(eventType);
            executeOrPostRunnableOnMainThread(new AnonymousClass4(eventType, eventArgs));
        }

        public void onTune(Uri channelUri, Bundle params) {
            onTune(channelUri);
        }

        public void onAppPrivateCommand(String action, Bundle data) {
        }

        void tune(Uri channelUri, Bundle params) {
            onTune(channelUri, params);
        }

        void release() {
            onRelease();
        }

        void startRecording(Uri programUri) {
            onStartRecording(programUri);
        }

        void stopRecording() {
            onStopRecording();
        }

        void appPrivateCommand(String action, Bundle data) {
            onAppPrivateCommand(action, data);
        }

        private void initialize(ITvInputSessionCallback callback) {
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
            for (int i = 0; i < n; i += DO_CREATE_SESSION) {
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
            for (int i = 0; i < n; i += DO_CREATE_SESSION) {
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
            for (int i = 0; i < n; i += DO_CREATE_SESSION) {
                try {
                    ((ITvInputServiceCallback) TvInputService.this.mCallbacks.getBroadcastItem(i)).removeHardwareInput(inputId);
                } catch (RemoteException e) {
                    Log.e(TvInputService.TAG, "error in broadcastRemoveHardwareInput", e);
                }
            }
            TvInputService.this.mCallbacks.finishBroadcast();
        }

        public final void handleMessage(Message msg) {
            SomeArgs args;
            ITvInputSessionCallback cb;
            String inputId;
            Session sessionImpl;
            TvInputHardwareInfo hardwareInfo;
            TvInputInfo inputInfo;
            HdmiDeviceInfo deviceInfo;
            switch (msg.what) {
                case DO_CREATE_SESSION /*1*/:
                    args = msg.obj;
                    InputChannel channel = args.arg1;
                    cb = args.arg2;
                    inputId = args.arg3;
                    args.recycle();
                    sessionImpl = TvInputService.this.onCreateSession(inputId);
                    if (sessionImpl == null) {
                        try {
                            cb.onSessionCreated(null, null);
                        } catch (RemoteException e) {
                            Log.e(TvInputService.TAG, "error in onSessionCreated", e);
                        }
                        return;
                    }
                    ITvInputSession iTvInputSessionWrapper = new ITvInputSessionWrapper(TvInputService.this, sessionImpl, channel);
                    if (sessionImpl instanceof HardwareSession) {
                        HardwareSession proxySession = (HardwareSession) sessionImpl;
                        String hardwareInputId = proxySession.getHardwareInputId();
                        if (!TextUtils.isEmpty(hardwareInputId)) {
                            if (TvInputService.this.isPassthroughInput(hardwareInputId)) {
                                proxySession.mProxySession = iTvInputSessionWrapper;
                                proxySession.mProxySessionCallback = cb;
                                proxySession.mServiceHandler = TvInputService.this.mServiceHandler;
                                ((TvInputManager) TvInputService.this.getSystemService(Context.TV_INPUT_SERVICE)).createSession(hardwareInputId, proxySession.mHardwareSessionCallback, TvInputService.this.mServiceHandler);
                            }
                        }
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
                    SomeArgs someArgs = SomeArgs.obtain();
                    someArgs.arg1 = sessionImpl;
                    someArgs.arg2 = iTvInputSessionWrapper;
                    someArgs.arg3 = cb;
                    someArgs.arg4 = null;
                    TvInputService.this.mServiceHandler.obtainMessage(DO_NOTIFY_SESSION_CREATED, someArgs).sendToTarget();
                case DO_NOTIFY_SESSION_CREATED /*2*/:
                    args = (SomeArgs) msg.obj;
                    sessionImpl = (Session) args.arg1;
                    cb = (ITvInputSessionCallback) args.arg3;
                    try {
                        cb.onSessionCreated((ITvInputSession) args.arg2, args.arg4);
                    } catch (RemoteException e22) {
                        Log.e(TvInputService.TAG, "error in onSessionCreated", e22);
                    }
                    if (sessionImpl != null) {
                        sessionImpl.initialize(cb);
                    }
                    args.recycle();
                case DO_CREATE_RECORDING_SESSION /*3*/:
                    args = (SomeArgs) msg.obj;
                    cb = (ITvInputSessionCallback) args.arg1;
                    inputId = (String) args.arg2;
                    args.recycle();
                    RecordingSession recordingSessionImpl = TvInputService.this.onCreateRecordingSession(inputId);
                    if (recordingSessionImpl == null) {
                        try {
                            cb.onSessionCreated(null, null);
                        } catch (RemoteException e222) {
                            Log.e(TvInputService.TAG, "error in onSessionCreated", e222);
                        }
                        return;
                    }
                    try {
                        cb.onSessionCreated(new ITvInputSessionWrapper(TvInputService.this, recordingSessionImpl), null);
                    } catch (RemoteException e2222) {
                        Log.e(TvInputService.TAG, "error in onSessionCreated", e2222);
                    }
                    recordingSessionImpl.initialize(cb);
                case DO_ADD_HARDWARE_INPUT /*4*/:
                    hardwareInfo = msg.obj;
                    inputInfo = TvInputService.this.onHardwareAdded(hardwareInfo);
                    if (inputInfo != null) {
                        broadcastAddHardwareInput(hardwareInfo.getDeviceId(), inputInfo);
                    }
                case DO_REMOVE_HARDWARE_INPUT /*5*/:
                    hardwareInfo = (TvInputHardwareInfo) msg.obj;
                    inputId = TvInputService.this.onHardwareRemoved(hardwareInfo);
                    if (inputId != null) {
                        broadcastRemoveHardwareInput(inputId);
                    }
                case DO_ADD_HDMI_INPUT /*6*/:
                    deviceInfo = msg.obj;
                    inputInfo = TvInputService.this.onHdmiDeviceAdded(deviceInfo);
                    if (inputInfo != null) {
                        broadcastAddHdmiInput(deviceInfo.getId(), inputInfo);
                    }
                case DO_REMOVE_HDMI_INPUT /*7*/:
                    deviceInfo = (HdmiDeviceInfo) msg.obj;
                    inputId = TvInputService.this.onHdmiDeviceRemoved(deviceInfo);
                    if (inputId != null) {
                        broadcastRemoveHardwareInput(inputId);
                    }
                default:
                    Log.w(TvInputService.TAG, "Unhandled message code: " + msg.what);
            }
        }
    }

    public abstract Session onCreateSession(String str);

    public TvInputService() {
        this.mServiceHandler = new ServiceHandler();
        this.mCallbacks = new RemoteCallbackList();
    }

    public final IBinder onBind(Intent intent) {
        return new Stub() {
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

    public TvInputInfo onHardwareAdded(TvInputHardwareInfo hardwareInfo) {
        return null;
    }

    public String onHardwareRemoved(TvInputHardwareInfo hardwareInfo) {
        return null;
    }

    public TvInputInfo onHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) {
        return null;
    }

    public String onHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) {
        return null;
    }

    private boolean isPassthroughInput(String inputId) {
        if (this.mTvInputManager == null) {
            this.mTvInputManager = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
        }
        TvInputInfo info = this.mTvInputManager.getTvInputInfo(inputId);
        return info != null ? info.isPassthroughInput() : DEBUG;
    }

    public static boolean isNavigationKey(int keyCode) {
        switch (keyCode) {
            case HwSysResource.APP /*19*/:
            case HwSysResource.MEMORY /*20*/:
            case HwSysResource.CPU /*21*/:
            case HwSysResource.IO /*22*/:
            case HwSysResource.SCHEDGROUP /*23*/:
            case InformationElement.EID_HT_OPERATION /*61*/:
            case ToneGenerator.TONE_CDMA_HIGH_SS_2 /*62*/:
            case ToneGenerator.TONE_CDMA_MED_SLS /*66*/:
            case ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK /*92*/:
            case ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD /*93*/:
            case ScriptIntrinsicBLAS.LOWER /*122*/:
            case BluetoothAssignedNumbers.HANLYNN_TECHNOLOGIES /*123*/:
                return true;
            default:
                return DEBUG;
        }
    }
}
