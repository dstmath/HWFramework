package android.zrhung.appeye;

import android.app.Activity;
import android.common.HwFrameworkFactory;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.rms.HwAppInnerBoostImpl;
import android.rms.IHwAppInnerBoost;
import android.util.Jlog;
import android.util.Log;
import android.view.FrameMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.OverScroller;
import android.widget.Scroller;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class AppEyeUiScroll {
    private static final int FRAME_RATE_MEASURE_CONST = 600;
    private static final int HALF_NANOSEC_PER_FRAME = 8333333;
    private static final int NANOSEC_PER_FRAME = 16666666;
    private static final int SCENE_ON_UISCROLL_FLAG = 2;
    private static final int SCENE_ON_UISCROLL_LOG_FLAG = 4;
    private static final int SCROLL_CHECK_STOP = 3;
    private static final int SCROLL_STATE_DRAGGING = 1;
    private static final int SCROLL_STATE_FLING = 2;
    private static final int SCROLL_STATE_IDLE = 0;
    private static final int SCROLL_STOP_INTERVAL = 700;
    private static final int SCROLL_VIEW_CHANGE = 1;
    private static final int SCROLL_VIEW_TOUCH = 2;
    private static final String TAG = "ZrHung.AppEyeUiScroll";
    private String mActivityName;
    private final FrameStatsCollector mCollector = new FrameStatsCollector();
    private WeakReference<Activity> mCurrActivity = null;
    private boolean mDebugLogOn = false;
    private volatile int mFrameCount = 0;
    private GestureDetector mGestureDetector = null;
    private Handler mHandler;
    private final WeakHashMap<Activity, Boolean> mHasCollectorMap = new WeakHashMap<>();
    private HwAppInnerBoostImpl mHwAppInnerBoostImpl = null;
    private AtomicBoolean mIsCollectorAdded = new AtomicBoolean(false);
    private AtomicBoolean mIsScrolling = new AtomicBoolean(false);
    private volatile int mMaxSkipFrames = 0;
    private final MotionEventListenerImpl mMotionEventListener = new MotionEventListenerImpl();
    private final OnScrollChangedListenerImpl mScrollChangedListener = new OnScrollChangedListenerImpl();
    private AtomicInteger mScrollState = new AtomicInteger(0);
    private WeakHashMap<Object, Integer> mScrollerMap = new WeakHashMap<>();
    private volatile int mSumSkipFrames = 0;

    static /* synthetic */ int access$1012(AppEyeUiScroll x0, int x1) {
        int i = x0.mSumSkipFrames + x1;
        x0.mSumSkipFrames = i;
        return i;
    }

    static /* synthetic */ int access$804(AppEyeUiScroll x0) {
        int i = x0.mFrameCount + 1;
        x0.mFrameCount = i;
        return i;
    }

    private AppEyeUiScroll(Handler handler, int sceneOn) {
        if ((sceneOn & 4) != 0) {
            this.mDebugLogOn = true;
        }
        if (handler != null) {
            this.mHandler = new ScrollHandler(handler);
        } else {
            this.mHandler = new ScrollHandler();
        }
    }

    public static Optional<AppEyeUiScroll> createInstance(Handler handler) {
        if (handler == null) {
            return Optional.empty();
        }
        int sceneOn = SystemProperties.getInt("persist.product.jankSceneOn", 0);
        if ((sceneOn & 2) != 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(new AppEyeUiScroll(handler, sceneOn));
    }

    public void setCurrActivity(Activity activity) {
        if (this.mDebugLogOn) {
            Log.d(TAG, "setCurrActivity: " + activity);
        }
        if (activity.getWindow() == null) {
            Log.i(TAG, "Activity:" + activity + " NOT visual, just igore");
            return;
        }
        this.mCurrActivity = new WeakReference<>(activity);
        this.mActivityName = activity.getComponentName().flattenToShortString();
        this.mScrollState.set(0);
        this.mIsScrolling.set(false);
        synchronized (this.mHasCollectorMap) {
            if (!this.mHasCollectorMap.containsKey(activity)) {
                this.mHasCollectorMap.put(activity, false);
                this.mIsCollectorAdded.set(false);
            }
        }
        this.mGestureDetector = new GestureDetector(activity, new ScrollGestureDetector());
        HwAppInnerBoostImpl hwAppInnerBoostImpl = this.mHwAppInnerBoostImpl;
        if (hwAppInnerBoostImpl != null) {
            hwAppInnerBoostImpl.setMotionEventListener(this.mMotionEventListener);
            return;
        }
        IHwAppInnerBoost appInnerBoost = HwFrameworkFactory.getHwAppInnerBoostImpl();
        if (appInnerBoost instanceof HwAppInnerBoostImpl) {
            this.mHwAppInnerBoostImpl = (HwAppInnerBoostImpl) appInnerBoost;
            this.mHwAppInnerBoostImpl.setMotionEventListener(this.mMotionEventListener);
        }
    }

    public void stop(Activity activity) {
        if (this.mDebugLogOn) {
            Log.d(TAG, "stop: " + activity);
        }
        HwAppInnerBoostImpl hwAppInnerBoostImpl = this.mHwAppInnerBoostImpl;
        if (hwAppInnerBoostImpl != null) {
            hwAppInnerBoostImpl.setMotionEventListener(null);
        }
        this.mHandler.removeCallbacksAndMessages(null);
        synchronized (this.mHasCollectorMap) {
            if (this.mHasCollectorMap.containsKey(activity)) {
                if (!(!this.mHasCollectorMap.get(activity).booleanValue() || activity.getWindow() == null || activity.getWindow().getDecorView() == null)) {
                    activity.getWindow().removeOnFrameMetricsAvailableListener(this.mCollector);
                    activity.getWindow().getDecorView().getViewTreeObserver().removeOnScrollChangedListener(this.mScrollChangedListener);
                }
                this.mHasCollectorMap.remove(activity);
            }
            this.mIsCollectorAdded.set(false);
        }
    }

    public void onFlingStart(final Object obj) {
        long uptime1 = 0;
        if (this.mDebugLogOn) {
            uptime1 = SystemClock.elapsedRealtimeNanos();
            Log.d(TAG, "onFlingStart obj=" + obj);
        }
        this.mHandler.post(new Runnable() {
            /* class android.zrhung.appeye.AppEyeUiScroll.AnonymousClass1 */

            public void run() {
                if (AppEyeUiScroll.this.mScrollState.get() == 2) {
                    AppEyeUiScroll.this.mScrollerMap.put(obj, 2);
                }
            }
        });
        if (this.mDebugLogOn) {
            Log.d(TAG, "FLING_START:" + (SystemClock.elapsedRealtimeNanos() - uptime1));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshScrollState() {
        if (this.mScrollState.get() != 1) {
            for (Map.Entry<Object, Integer> entry : this.mScrollerMap.entrySet()) {
                Object obj = entry.getKey();
                boolean isFinished = true;
                if (obj instanceof OverScroller) {
                    isFinished = ((OverScroller) obj).isFinished();
                    continue;
                } else if (obj instanceof Scroller) {
                    isFinished = ((Scroller) obj).isFinished();
                    continue;
                } else {
                    Log.e(TAG, "Neither Scroller or OverScroller");
                    continue;
                }
                if (!isFinished) {
                    return;
                }
            }
            if (this.mScrollerMap.size() != 0) {
                Handler handler = this.mHandler;
                handler.sendMessage(handler.obtainMessage(3));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean addFrameStatsCollector() {
        Trace.traceBegin(4, "UiScrollCollectorCreate");
        Activity tempActivity = this.mCurrActivity.get();
        if (tempActivity == null) {
            Trace.traceEnd(4);
            return false;
        } else if (tempActivity.getWindow() == null || tempActivity.getWindow().getDecorView() == null || tempActivity.getWindow().getDecorView().getThreadedRenderer() == null) {
            Trace.traceEnd(4);
            return false;
        } else {
            synchronized (this.mHasCollectorMap) {
                if (!this.mHasCollectorMap.containsKey(tempActivity) || this.mHasCollectorMap.get(tempActivity).booleanValue() || tempActivity.getWindow() == null || tempActivity.getWindow().getDecorView() == null) {
                    Trace.traceEnd(4);
                    return false;
                }
                tempActivity.getWindow().addOnFrameMetricsAvailableListener(this.mCollector, this.mHandler);
                tempActivity.getWindow().getDecorView().getViewTreeObserver().addOnScrollChangedListener(this.mScrollChangedListener);
                this.mHasCollectorMap.put(tempActivity, true);
                this.mIsCollectorAdded.set(true);
                Trace.traceEnd(4);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class FrameStatsCollector implements Window.OnFrameMetricsAvailableListener {
        private FrameStatsCollector() {
        }

        public void onFrameMetricsAvailable(Window win, FrameMetrics metrics, int dropout) {
            if (AppEyeUiScroll.this.mIsScrolling.get()) {
                long uptime1 = 0;
                if (AppEyeUiScroll.this.mDebugLogOn) {
                    uptime1 = SystemClock.elapsedRealtimeNanos();
                }
                FrameMetrics fmetrics = new FrameMetrics(metrics);
                AppEyeUiScroll.access$804(AppEyeUiScroll.this);
                if (AppEyeUiScroll.this.mFrameCount == 1) {
                    Jlog.d(398, "#P:" + AppEyeUiScroll.this.mActivityName);
                }
                int skipFrames = (int) (((fmetrics.getMetric(11) - fmetrics.getMetric(10)) + 8333333) / 16666666);
                if (AppEyeUiScroll.this.mDebugLogOn && skipFrames != 0) {
                    Log.d(AppEyeUiScroll.TAG, "Scroll SkipFrames=" + skipFrames);
                }
                AppEyeUiScroll.access$1012(AppEyeUiScroll.this, skipFrames);
                if (skipFrames > AppEyeUiScroll.this.mMaxSkipFrames) {
                    AppEyeUiScroll.this.mMaxSkipFrames = skipFrames;
                }
                AppEyeUiScroll.this.refreshScrollState();
                if (AppEyeUiScroll.this.mDebugLogOn) {
                    Log.d(AppEyeUiScroll.TAG, "FRAME:" + (SystemClock.elapsedRealtimeNanos() - uptime1));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class OnScrollChangedListenerImpl implements ViewTreeObserver.OnScrollChangedListener {
        private OnScrollChangedListenerImpl() {
        }

        public void onScrollChanged() {
            if (((Activity) AppEyeUiScroll.this.mCurrActivity.get()) != null) {
                AppEyeUiScroll.this.mHandler.sendMessage(AppEyeUiScroll.this.mHandler.obtainMessage(1));
            }
        }
    }

    /* access modifiers changed from: private */
    public class MotionEventListenerImpl implements HwAppInnerBoostImpl.MotionEventListener {
        private MotionEventListenerImpl() {
        }

        @Override // android.rms.HwAppInnerBoostImpl.MotionEventListener
        public void onMotionEvent(MotionEvent ev) {
            long uptime1 = 0;
            if (AppEyeUiScroll.this.mDebugLogOn) {
                uptime1 = SystemClock.elapsedRealtimeNanos();
            }
            AppEyeUiScroll.this.mHandler.sendMessage(AppEyeUiScroll.this.mHandler.obtainMessage(2, MotionEvent.obtain(ev)));
            if (AppEyeUiScroll.this.mDebugLogOn) {
                Log.d(AppEyeUiScroll.TAG, "MOTION:" + (SystemClock.elapsedRealtimeNanos() - uptime1));
            }
        }
    }

    /* access modifiers changed from: private */
    public class ScrollGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private ScrollGestureDetector() {
        }

        public boolean onDown(MotionEvent e) {
            if (AppEyeUiScroll.this.mScrollState.get() == 2) {
                AppEyeUiScroll.this.mScrollState.set(1);
                if (AppEyeUiScroll.this.mHandler.hasMessages(3)) {
                    AppEyeUiScroll.this.mHandler.removeMessages(3);
                }
            }
            if (!AppEyeUiScroll.this.mIsScrolling.get()) {
                AppEyeUiScroll.this.mScrollState.set(0);
                AppEyeUiScroll.this.mIsScrolling.set(false);
            }
            return super.onDown(e);
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (AppEyeUiScroll.this.mScrollState.get() == 0) {
                Jlog.d(397, "#P:" + AppEyeUiScroll.this.mActivityName);
                AppEyeUiScroll.this.mFrameCount = 0;
                AppEyeUiScroll.this.mMaxSkipFrames = 0;
                AppEyeUiScroll.this.mSumSkipFrames = 0;
                AppEyeUiScroll.this.mScrollerMap.clear();
                AppEyeUiScroll.this.mScrollState.set(1);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (AppEyeUiScroll.this.mScrollState.get() != 2) {
                AppEyeUiScroll.this.mScrollState.set(2);
                if (AppEyeUiScroll.this.mHandler.hasMessages(3)) {
                    AppEyeUiScroll.this.mHandler.removeMessages(3);
                }
                AppEyeUiScroll.this.mHandler.sendMessageDelayed(AppEyeUiScroll.this.mHandler.obtainMessage(3), 700);
            }
            return super.onFling(e1, e2, distanceX, distanceY);
        }
    }

    private class ScrollHandler extends Handler {
        ScrollHandler(Handler handler) {
            super(handler.getLooper());
        }

        ScrollHandler() {
        }

        public void handleMessage(Message msg) {
            boolean hadCheckStopMessage = AppEyeUiScroll.this.mHandler.hasMessages(3);
            int i = msg.what;
            if (i == 1) {
                if (!AppEyeUiScroll.this.mIsScrolling.get() && AppEyeUiScroll.this.mScrollState.get() != 0) {
                    AppEyeUiScroll.this.mIsScrolling.set(true);
                }
                if (AppEyeUiScroll.this.mScrollState.get() == 2) {
                    if (hadCheckStopMessage) {
                        AppEyeUiScroll.this.mHandler.removeMessages(3);
                    }
                    AppEyeUiScroll.this.mHandler.sendMessageDelayed(AppEyeUiScroll.this.mHandler.obtainMessage(3), 700);
                }
            } else if (i != 2) {
                if (i == 3) {
                    if (hadCheckStopMessage) {
                        AppEyeUiScroll.this.mHandler.removeMessages(3);
                    }
                    reportJankState();
                    AppEyeUiScroll.this.mScrollState.set(0);
                    AppEyeUiScroll.this.mIsScrolling.set(false);
                }
            } else if (checkMsgValid(msg)) {
                MotionEvent msgEvent = (MotionEvent) msg.obj;
                AppEyeUiScroll.this.mGestureDetector.onTouchEvent(msgEvent);
                int actionMasked = msgEvent.getActionMasked();
                if ((actionMasked == 1 || actionMasked == 3) && AppEyeUiScroll.this.mScrollState.get() == 1) {
                    AppEyeUiScroll.this.mScrollState.set(0);
                    AppEyeUiScroll.this.mIsScrolling.set(false);
                    if (hadCheckStopMessage) {
                        AppEyeUiScroll.this.mHandler.removeMessages(3);
                    }
                    AppEyeUiScroll.this.mHandler.sendMessage(AppEyeUiScroll.this.mHandler.obtainMessage(3));
                }
            }
        }

        private boolean checkMsgValid(Message msg) {
            if (!AppEyeUiScroll.this.mIsCollectorAdded.get() && !AppEyeUiScroll.this.addFrameStatsCollector()) {
                return false;
            }
            if (msg.obj instanceof MotionEvent) {
                return true;
            }
            Log.e(AppEyeUiScroll.TAG, "msg.obj cannot be converted to MotionEvent");
            return false;
        }

        private void reportJankState() {
            if (AppEyeUiScroll.this.mFrameCount <= 0 || AppEyeUiScroll.this.mSumSkipFrames < 0) {
                Log.e(AppEyeUiScroll.TAG, "FrameRate error: mFrameCount=" + AppEyeUiScroll.this.mFrameCount + " mSumSkipFrames=" + AppEyeUiScroll.this.mSumSkipFrames);
                return;
            }
            Jlog.d(399, "#P:" + AppEyeUiScroll.this.mActivityName + "#FrameRate:" + ((AppEyeUiScroll.this.mFrameCount * AppEyeUiScroll.FRAME_RATE_MEASURE_CONST) / (AppEyeUiScroll.this.mFrameCount + AppEyeUiScroll.this.mSumSkipFrames)) + "#MaxSkipFrames:" + AppEyeUiScroll.this.mMaxSkipFrames);
        }
    }
}
