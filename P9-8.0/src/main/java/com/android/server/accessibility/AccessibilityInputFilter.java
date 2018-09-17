package com.android.server.accessibility;

import android.content.Context;
import android.os.PowerManager;
import android.util.Pools.SimplePool;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.view.Choreographer;
import android.view.InputEvent;
import android.view.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import com.android.server.accessibility.HwMagnificationFactory.IMagnificationGestureHandler;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.LightsManager;

class AccessibilityInputFilter extends InputFilter implements EventStreamTransformation {
    private static final boolean DEBUG = false;
    static final int FEATURES_AFFECTING_MOTION_EVENTS = 91;
    static final int FLAG_FEATURE_AUTOCLICK = 8;
    static final int FLAG_FEATURE_CONTROL_SCREEN_MAGNIFIER = 32;
    static final int FLAG_FEATURE_FILTER_KEY_EVENTS = 4;
    static final int FLAG_FEATURE_INJECT_MOTION_EVENTS = 16;
    static final int FLAG_FEATURE_SCREEN_MAGNIFIER = 1;
    static final int FLAG_FEATURE_TOUCH_EXPLORATION = 2;
    static final int FLAG_FEATURE_TRIGGERED_SCREEN_MAGNIFIER = 64;
    private static final String TAG = AccessibilityInputFilter.class.getSimpleName();
    private final AccessibilityManagerService mAms;
    private AutoclickController mAutoclickController;
    private final Choreographer mChoreographer;
    private final Context mContext;
    private int mEnabledFeatures;
    private EventStreamTransformation mEventHandler;
    private MotionEventHolder mEventQueue;
    private boolean mInstalled;
    private KeyboardInterceptor mKeyboardInterceptor;
    private EventStreamState mKeyboardStreamState;
    private MagnificationGestureHandler mMagnificationGestureHandler;
    private MotionEventInjector mMotionEventInjector;
    private EventStreamState mMouseStreamState;
    private final PowerManager mPm;
    private final Runnable mProcessBatchedEventsRunnable = new Runnable() {
        public void run() {
            try {
                AccessibilityInputFilter.this.processBatchedEvents(AccessibilityInputFilter.this.mChoreographer.getFrameTimeNanos());
            } catch (IllegalArgumentException e) {
                Slog.e(AccessibilityInputFilter.TAG, "AccessibilityInputFilter error: idBits did not match any ids in the event.");
            } catch (Exception e2) {
                Slog.e(AccessibilityInputFilter.TAG, "AccessibilityInputFilter other error: " + e2);
            }
            if (AccessibilityInputFilter.this.mEventQueue != null) {
                AccessibilityInputFilter.this.scheduleProcessBatchedEvents();
            }
        }
    };
    private TouchExplorer mTouchExplorer;
    private EventStreamState mTouchScreenStreamState;
    private int mUserId;

    private static class EventStreamState {
        private int mDeviceId = -1;

        EventStreamState() {
        }

        public boolean updateDeviceId(int deviceId) {
            if (this.mDeviceId == deviceId) {
                return false;
            }
            reset();
            this.mDeviceId = deviceId;
            return true;
        }

        public boolean deviceIdValid() {
            return this.mDeviceId >= 0;
        }

        public void reset() {
            this.mDeviceId = -1;
        }

        public boolean shouldProcessScroll() {
            return false;
        }

        public boolean shouldProcessMotionEvent(MotionEvent event) {
            return false;
        }

        public boolean shouldProcessKeyEvent(KeyEvent event) {
            return false;
        }
    }

    private static class KeyboardEventStreamState extends EventStreamState {
        private SparseBooleanArray mEventSequenceStartedMap = new SparseBooleanArray();

        public KeyboardEventStreamState() {
            reset();
        }

        public final void reset() {
            super.reset();
            this.mEventSequenceStartedMap.clear();
        }

        public boolean updateDeviceId(int deviceId) {
            return false;
        }

        public boolean deviceIdValid() {
            return true;
        }

        public final boolean shouldProcessKeyEvent(KeyEvent event) {
            int deviceId = event.getDeviceId();
            if (this.mEventSequenceStartedMap.get(deviceId, false)) {
                return true;
            }
            boolean shouldProcess = event.getAction() == 0;
            this.mEventSequenceStartedMap.put(deviceId, shouldProcess);
            return shouldProcess;
        }
    }

    private static class MotionEventHolder {
        private static final int MAX_POOL_SIZE = 32;
        private static final SimplePool<MotionEventHolder> sPool = new SimplePool(32);
        public MotionEvent event;
        public MotionEventHolder next;
        public int policyFlags;
        public MotionEventHolder previous;

        private MotionEventHolder() {
        }

        public static MotionEventHolder obtain(MotionEvent event, int policyFlags) {
            MotionEventHolder holder = (MotionEventHolder) sPool.acquire();
            if (holder == null) {
                holder = new MotionEventHolder();
            }
            holder.event = MotionEvent.obtain(event);
            holder.policyFlags = policyFlags;
            return holder;
        }

        public void recycle() {
            this.event.recycle();
            this.event = null;
            this.policyFlags = 0;
            this.next = null;
            this.previous = null;
            sPool.release(this);
        }
    }

    private static class MouseEventStreamState extends EventStreamState {
        private boolean mMotionSequenceStarted;

        public MouseEventStreamState() {
            reset();
        }

        public final void reset() {
            super.reset();
            this.mMotionSequenceStarted = false;
        }

        public final boolean shouldProcessScroll() {
            return true;
        }

        public final boolean shouldProcessMotionEvent(MotionEvent event) {
            boolean z = true;
            if (this.mMotionSequenceStarted) {
                return true;
            }
            int action = event.getActionMasked();
            if (!(action == 0 || action == 7)) {
                z = false;
            }
            this.mMotionSequenceStarted = z;
            return this.mMotionSequenceStarted;
        }
    }

    private static class TouchScreenEventStreamState extends EventStreamState {
        private boolean mHoverSequenceStarted;
        private boolean mTouchSequenceStarted;

        public TouchScreenEventStreamState() {
            reset();
        }

        public final void reset() {
            super.reset();
            this.mTouchSequenceStarted = false;
            this.mHoverSequenceStarted = false;
        }

        public final boolean shouldProcessMotionEvent(MotionEvent event) {
            boolean z = true;
            if (event.isTouchEvent()) {
                if (this.mTouchSequenceStarted) {
                    return true;
                }
                if (event.getActionMasked() != 0) {
                    z = false;
                }
                this.mTouchSequenceStarted = z;
                return this.mTouchSequenceStarted;
            } else if (this.mHoverSequenceStarted) {
                return true;
            } else {
                if (event.getActionMasked() != 9) {
                    z = false;
                }
                this.mHoverSequenceStarted = z;
                return this.mHoverSequenceStarted;
            }
        }
    }

    AccessibilityInputFilter(Context context, AccessibilityManagerService service) {
        super(context.getMainLooper());
        this.mContext = context;
        this.mAms = service;
        this.mPm = (PowerManager) context.getSystemService("power");
        this.mChoreographer = Choreographer.getInstance();
    }

    public void onInstalled() {
        this.mInstalled = true;
        disableFeatures();
        enableFeatures();
        super.onInstalled();
    }

    public void onUninstalled() {
        this.mInstalled = false;
        disableFeatures();
        super.onUninstalled();
    }

    public void onInputEvent(InputEvent event, int policyFlags) {
        if (this.mEventHandler == null) {
            super.onInputEvent(event, policyFlags);
            return;
        }
        EventStreamState state = getEventStreamState(event);
        if (state == null) {
            super.onInputEvent(event, policyFlags);
            return;
        }
        int eventSource = event.getSource();
        if ((1073741824 & policyFlags) == 0) {
            state.reset();
            this.mEventHandler.clearEvents(eventSource);
            super.onInputEvent(event, policyFlags);
            return;
        }
        if (state.updateDeviceId(event.getDeviceId())) {
            this.mEventHandler.clearEvents(eventSource);
        }
        if (state.deviceIdValid()) {
            if (event instanceof MotionEvent) {
                if ((this.mEnabledFeatures & FEATURES_AFFECTING_MOTION_EVENTS) != 0) {
                    processMotionEvent(state, (MotionEvent) event, policyFlags);
                    return;
                }
                super.onInputEvent(event, policyFlags);
            } else if (event instanceof KeyEvent) {
                processKeyEvent(state, (KeyEvent) event, policyFlags);
            }
            return;
        }
        super.onInputEvent(event, policyFlags);
    }

    private EventStreamState getEventStreamState(InputEvent event) {
        if (event instanceof MotionEvent) {
            if (event.isFromSource(4098)) {
                if (this.mTouchScreenStreamState == null) {
                    this.mTouchScreenStreamState = new TouchScreenEventStreamState();
                }
                return this.mTouchScreenStreamState;
            } else if (event.isFromSource(8194)) {
                if (this.mMouseStreamState == null) {
                    this.mMouseStreamState = new MouseEventStreamState();
                }
                return this.mMouseStreamState;
            }
        } else if ((event instanceof KeyEvent) && event.isFromSource(LightsManager.LIGHT_ID_SMARTBACKLIGHT)) {
            if (this.mKeyboardStreamState == null) {
                this.mKeyboardStreamState = new KeyboardEventStreamState();
            }
            return this.mKeyboardStreamState;
        }
        return null;
    }

    private void processMotionEvent(EventStreamState state, MotionEvent event, int policyFlags) {
        if (!state.shouldProcessScroll() && event.getActionMasked() == 8) {
            super.onInputEvent(event, policyFlags);
        } else if (state.shouldProcessMotionEvent(event)) {
            batchMotionEvent(event, policyFlags);
        }
    }

    private void processKeyEvent(EventStreamState state, KeyEvent event, int policyFlags) {
        if (state.shouldProcessKeyEvent(event)) {
            this.mEventHandler.onKeyEvent(event, policyFlags);
        } else {
            super.onInputEvent(event, policyFlags);
        }
    }

    private void scheduleProcessBatchedEvents() {
        this.mChoreographer.postCallback(0, this.mProcessBatchedEventsRunnable, null);
    }

    private void batchMotionEvent(MotionEvent event, int policyFlags) {
        if (this.mEventQueue == null) {
            this.mEventQueue = MotionEventHolder.obtain(event, policyFlags);
            scheduleProcessBatchedEvents();
        } else if (!this.mEventQueue.event.addBatch(event)) {
            MotionEventHolder holder = MotionEventHolder.obtain(event, policyFlags);
            holder.next = this.mEventQueue;
            this.mEventQueue.previous = holder;
            this.mEventQueue = holder;
        }
    }

    private void processBatchedEvents(long frameNanos) {
        MotionEventHolder current = this.mEventQueue;
        if (current != null) {
            while (current.next != null) {
                current = current.next;
            }
            while (current != null) {
                if (current.event.getEventTimeNano() >= frameNanos) {
                    current.next = null;
                    break;
                }
                handleMotionEvent(current.event, current.policyFlags);
                MotionEventHolder prior = current;
                current = current.previous;
                prior.recycle();
            }
            this.mEventQueue = null;
        }
    }

    private void handleMotionEvent(MotionEvent event, int policyFlags) {
        if (this.mEventHandler != null) {
            this.mPm.userActivity(event.getEventTime(), false);
            MotionEvent transformedEvent = MotionEvent.obtain(event);
            this.mEventHandler.onMotionEvent(transformedEvent, event, policyFlags);
            transformedEvent.recycle();
        }
    }

    public void onMotionEvent(MotionEvent transformedEvent, MotionEvent rawEvent, int policyFlags) {
        sendInputEvent(transformedEvent, policyFlags);
    }

    public void onKeyEvent(KeyEvent event, int policyFlags) {
        sendInputEvent(event, policyFlags);
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    public void setNext(EventStreamTransformation sink) {
    }

    public void clearEvents(int inputSource) {
    }

    void setUserAndEnabledFeatures(int userId, int enabledFeatures) {
        if (this.mEnabledFeatures != enabledFeatures || this.mUserId != userId) {
            if (this.mInstalled) {
                disableFeatures();
            }
            this.mUserId = userId;
            this.mEnabledFeatures = enabledFeatures;
            if (this.mInstalled) {
                enableFeatures();
            }
        }
    }

    void notifyAccessibilityEvent(AccessibilityEvent event) {
        if (this.mEventHandler != null) {
            this.mEventHandler.onAccessibilityEvent(event);
        }
    }

    void notifyAccessibilityButtonClicked() {
        if (this.mMagnificationGestureHandler != null) {
            this.mMagnificationGestureHandler.notifyShortcutTriggered();
        }
    }

    private void enableFeatures() {
        resetStreamState();
        if ((this.mEnabledFeatures & 8) != 0) {
            this.mAutoclickController = new AutoclickController(this.mContext, this.mUserId);
            addFirstEventHandler(this.mAutoclickController);
        }
        if ((this.mEnabledFeatures & 2) != 0) {
            this.mTouchExplorer = new TouchExplorer(this.mContext, this.mAms);
            addFirstEventHandler(this.mTouchExplorer);
        }
        if (!((this.mEnabledFeatures & 32) == 0 && (this.mEnabledFeatures & 1) == 0 && (this.mEnabledFeatures & 64) == 0)) {
            boolean detectControlGestures = (this.mEnabledFeatures & 1) != 0;
            boolean triggerable = (this.mEnabledFeatures & 64) != 0;
            IMagnificationGestureHandler ism = HwMagnificationFactory.getHwMagnificationGestureHandler();
            if (ism != null) {
                this.mMagnificationGestureHandler = ism.getInstance(this.mContext, this.mAms, detectControlGestures, triggerable);
            } else {
                this.mMagnificationGestureHandler = new MagnificationGestureHandler(this.mContext, this.mAms, detectControlGestures, triggerable);
            }
            addFirstEventHandler(this.mMagnificationGestureHandler);
        }
        if ((this.mEnabledFeatures & 16) != 0) {
            this.mMotionEventInjector = new MotionEventInjector(this.mContext.getMainLooper());
            addFirstEventHandler(this.mMotionEventInjector);
            this.mAms.setMotionEventInjector(this.mMotionEventInjector);
        }
        if ((this.mEnabledFeatures & 4) != 0) {
            this.mKeyboardInterceptor = new KeyboardInterceptor(this.mAms);
            addFirstEventHandler(this.mKeyboardInterceptor);
        }
    }

    private void addFirstEventHandler(EventStreamTransformation handler) {
        if (this.mEventHandler != null) {
            handler.setNext(this.mEventHandler);
        } else {
            handler.setNext(this);
        }
        this.mEventHandler = handler;
    }

    private void disableFeatures() {
        processBatchedEvents(JobStatus.NO_LATEST_RUNTIME);
        if (this.mMotionEventInjector != null) {
            this.mAms.setMotionEventInjector(null);
            this.mMotionEventInjector.onDestroy();
            this.mMotionEventInjector = null;
        }
        if (this.mAutoclickController != null) {
            this.mAutoclickController.onDestroy();
            this.mAutoclickController = null;
        }
        if (this.mTouchExplorer != null) {
            this.mTouchExplorer.onDestroy();
            this.mTouchExplorer = null;
        }
        if (this.mMagnificationGestureHandler != null) {
            this.mMagnificationGestureHandler.onDestroy();
            this.mMagnificationGestureHandler = null;
        }
        if (this.mKeyboardInterceptor != null) {
            this.mKeyboardInterceptor.onDestroy();
            this.mKeyboardInterceptor = null;
        }
        this.mEventHandler = null;
        resetStreamState();
    }

    void resetStreamState() {
        if (this.mTouchScreenStreamState != null) {
            this.mTouchScreenStreamState.reset();
        }
        if (this.mMouseStreamState != null) {
            this.mMouseStreamState.reset();
        }
        if (this.mKeyboardStreamState != null) {
            this.mKeyboardStreamState.reset();
        }
    }

    public void onDestroy() {
    }
}
