package com.android.server.accessibility;

import android.content.Context;
import android.os.PowerManager;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.InputEvent;
import android.view.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import com.android.server.LocalServices;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.usb.descriptors.UsbACInterface;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class AccessibilityInputFilter extends InputFilter implements EventStreamTransformation {
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
    private final Context mContext;
    private int mEnabledFeatures;
    private final SparseArray<EventStreamTransformation> mEventHandler;
    private boolean mInstalled;
    private KeyboardInterceptor mKeyboardInterceptor;
    private EventStreamState mKeyboardStreamState;
    private final SparseArray<MagnificationGestureHandler> mMagnificationGestureHandler;
    private final SparseArray<MotionEventInjector> mMotionEventInjector;
    private EventStreamState mMouseStreamState;
    private final PowerManager mPm;
    private final SparseArray<TouchExplorer> mTouchExplorer;
    private EventStreamState mTouchScreenStreamState;
    private int mUserId;

    AccessibilityInputFilter(Context context, AccessibilityManagerService service) {
        this(context, service, new SparseArray(0));
    }

    AccessibilityInputFilter(Context context, AccessibilityManagerService service, SparseArray<EventStreamTransformation> eventHandler) {
        super(context.getMainLooper());
        this.mTouchExplorer = new SparseArray<>(0);
        this.mMagnificationGestureHandler = new SparseArray<>(0);
        this.mMotionEventInjector = new SparseArray<>(0);
        this.mContext = context;
        this.mAms = service;
        this.mPm = (PowerManager) context.getSystemService("power");
        this.mEventHandler = eventHandler;
    }

    public void onInstalled() {
        this.mInstalled = true;
        disableFeatures();
        enableFeatures();
        AccessibilityInputFilter.super.onInstalled();
    }

    public void onUninstalled() {
        this.mInstalled = false;
        disableFeatures();
        AccessibilityInputFilter.super.onUninstalled();
    }

    public void noFilterEvent(InputEvent event, int policyFlags) {
        if (this.mInstalled) {
            AccessibilityInputFilter.super.onInputEvent(event, policyFlags);
        }
    }

    public void sendInputEvent(InputEvent event, int policyFlags) {
        if (this.mInstalled) {
            AccessibilityInputFilter.super.sendInputEvent(event, policyFlags);
        } else {
            Slog.e(TAG, "Input filter not installed.");
        }
    }

    /* access modifiers changed from: package-private */
    public void onDisplayChanged() {
        if (this.mInstalled) {
            disableFeatures();
            enableFeatures();
        }
    }

    public void onInputEvent(InputEvent event, int policyFlags) {
        if (this.mEventHandler.size() == 0) {
            AccessibilityInputFilter.super.onInputEvent(event, policyFlags);
            return;
        }
        EventStreamState state = getEventStreamState(event);
        if (state == null) {
            AccessibilityInputFilter.super.onInputEvent(event, policyFlags);
            return;
        }
        int eventSource = event.getSource();
        if ((1073741824 & policyFlags) == 0) {
            state.reset();
            clearEventsForAllEventHandlers(eventSource);
            AccessibilityInputFilter.super.onInputEvent(event, policyFlags);
            return;
        }
        if (state.updateInputSource(event.getSource())) {
            clearEventsForAllEventHandlers(eventSource);
        }
        if (!state.inputSourceValid()) {
            AccessibilityInputFilter.super.onInputEvent(event, policyFlags);
        } else if (event instanceof MotionEvent) {
            if ((this.mEnabledFeatures & FEATURES_AFFECTING_MOTION_EVENTS) != 0) {
                processMotionEvent(state, (MotionEvent) event, policyFlags);
            } else {
                AccessibilityInputFilter.super.onInputEvent(event, policyFlags);
            }
        } else if (event instanceof KeyEvent) {
            processKeyEvent(state, (KeyEvent) event, policyFlags);
        }
    }

    private EventStreamState getEventStreamState(InputEvent event) {
        if (event instanceof MotionEvent) {
            if (event.isFromSource(UsbACInterface.FORMAT_II_AC3)) {
                if (this.mTouchScreenStreamState == null) {
                    this.mTouchScreenStreamState = new TouchScreenEventStreamState();
                }
                return this.mTouchScreenStreamState;
            } else if (!event.isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
                return null;
            } else {
                if (this.mMouseStreamState == null) {
                    this.mMouseStreamState = new MouseEventStreamState();
                }
                return this.mMouseStreamState;
            }
        } else if (!(event instanceof KeyEvent) || !event.isFromSource(257)) {
            return null;
        } else {
            if (this.mKeyboardStreamState == null) {
                this.mKeyboardStreamState = new KeyboardEventStreamState();
            }
            return this.mKeyboardStreamState;
        }
    }

    private void clearEventsForAllEventHandlers(int eventSource) {
        for (int i = 0; i < this.mEventHandler.size(); i++) {
            EventStreamTransformation eventHandler = this.mEventHandler.valueAt(i);
            if (eventHandler != null) {
                eventHandler.clearEvents(eventSource);
            }
        }
    }

    private void processMotionEvent(EventStreamState state, MotionEvent event, int policyFlags) {
        if (!state.shouldProcessScroll() && event.getActionMasked() == 8) {
            AccessibilityInputFilter.super.onInputEvent(event, policyFlags);
        } else if (state.shouldProcessMotionEvent(event)) {
            handleMotionEvent(event, policyFlags);
        }
    }

    private void processKeyEvent(EventStreamState state, KeyEvent event, int policyFlags) {
        if (!state.shouldProcessKeyEvent(event)) {
            AccessibilityInputFilter.super.onInputEvent(event, policyFlags);
        } else {
            this.mEventHandler.get(0).onKeyEvent(event, policyFlags);
        }
    }

    private void handleMotionEvent(MotionEvent event, int policyFlags) {
        int i = 0;
        this.mPm.userActivity(event.getEventTime(), false);
        MotionEvent transformedEvent = MotionEvent.obtain(event);
        int displayId = event.getDisplayId();
        SparseArray<EventStreamTransformation> sparseArray = this.mEventHandler;
        if (isDisplayIdValid(displayId)) {
            i = displayId;
        }
        sparseArray.get(i).onMotionEvent(transformedEvent, event, policyFlags);
        transformedEvent.recycle();
    }

    private boolean isDisplayIdValid(int displayId) {
        return this.mEventHandler.get(displayId) != null;
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onMotionEvent(MotionEvent transformedEvent, MotionEvent rawEvent, int policyFlags) {
        sendInputEvent(transformedEvent, policyFlags);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onKeyEvent(KeyEvent event, int policyFlags) {
        sendInputEvent(event, policyFlags);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void setNext(EventStreamTransformation sink) {
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public EventStreamTransformation getNext() {
        return null;
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void clearEvents(int inputSource) {
    }

    /* access modifiers changed from: package-private */
    public void setUserAndEnabledFeatures(int userId, int enabledFeatures) {
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

    /* access modifiers changed from: package-private */
    public void notifyAccessibilityEvent(AccessibilityEvent event) {
        for (int i = 0; i < this.mEventHandler.size(); i++) {
            EventStreamTransformation eventHandler = this.mEventHandler.valueAt(i);
            if (eventHandler != null) {
                eventHandler.onAccessibilityEvent(event);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAccessibilityButtonClicked(int displayId) {
        MagnificationGestureHandler handler;
        if (this.mMagnificationGestureHandler.size() != 0 && (handler = this.mMagnificationGestureHandler.get(displayId)) != null) {
            handler.notifyShortcutTriggered();
        }
    }

    private void enableFeatures() {
        resetStreamState();
        ArrayList<Display> displaysList = this.mAms.getValidDisplayList();
        if ((this.mEnabledFeatures & 8) != 0) {
            this.mAutoclickController = new AutoclickController(this.mContext, this.mUserId);
            addFirstEventHandlerForAllDisplays(displaysList, this.mAutoclickController);
        }
        for (int i = displaysList.size() - 1; i >= 0; i--) {
            int displayId = displaysList.get(i).getDisplayId();
            if ((this.mEnabledFeatures & 2) != 0) {
                TouchExplorer explorer = new TouchExplorer(this.mContext, this.mAms);
                addFirstEventHandler(displayId, explorer);
                this.mTouchExplorer.put(displayId, explorer);
            }
            int i2 = this.mEnabledFeatures;
            if (!((i2 & 32) == 0 && (i2 & 1) == 0 && (i2 & 64) == 0)) {
                MagnificationGestureHandler magnificationGestureHandler = new MagnificationGestureHandler(this.mContext, this.mAms.getMagnificationController(), (this.mEnabledFeatures & 1) != 0, (this.mEnabledFeatures & 64) != 0, displayId);
                addFirstEventHandler(displayId, magnificationGestureHandler);
                this.mMagnificationGestureHandler.put(displayId, magnificationGestureHandler);
            }
            if ((this.mEnabledFeatures & 16) != 0) {
                MotionEventInjector injector = new MotionEventInjector(this.mContext.getMainLooper());
                addFirstEventHandler(displayId, injector);
                this.mAms.setMotionEventInjector(injector);
                this.mMotionEventInjector.put(displayId, injector);
            }
        }
        if ((this.mEnabledFeatures & 4) != 0) {
            this.mKeyboardInterceptor = new KeyboardInterceptor(this.mAms, (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class));
            addFirstEventHandler(0, this.mKeyboardInterceptor);
        }
    }

    private void addFirstEventHandler(int displayId, EventStreamTransformation handler) {
        EventStreamTransformation eventHandler = this.mEventHandler.get(displayId);
        if (eventHandler != null) {
            handler.setNext(eventHandler);
        } else {
            handler.setNext(this);
        }
        this.mEventHandler.put(displayId, handler);
    }

    private void addFirstEventHandlerForAllDisplays(ArrayList<Display> displayList, EventStreamTransformation handler) {
        for (int i = 0; i < displayList.size(); i++) {
            addFirstEventHandler(displayList.get(i).getDisplayId(), handler);
        }
    }

    private void disableFeatures() {
        int i = this.mMotionEventInjector.size();
        while (true) {
            i--;
            if (i < 0) {
                break;
            }
            MotionEventInjector injector = this.mMotionEventInjector.valueAt(i);
            this.mAms.setMotionEventInjector(null);
            if (injector != null) {
                injector.onDestroy();
            }
        }
        this.mMotionEventInjector.clear();
        AutoclickController autoclickController = this.mAutoclickController;
        if (autoclickController != null) {
            autoclickController.onDestroy();
            this.mAutoclickController = null;
        }
        for (int i2 = this.mTouchExplorer.size() - 1; i2 >= 0; i2--) {
            TouchExplorer explorer = this.mTouchExplorer.valueAt(i2);
            if (explorer != null) {
                explorer.onDestroy();
            }
        }
        this.mTouchExplorer.clear();
        for (int i3 = this.mMagnificationGestureHandler.size() - 1; i3 >= 0; i3--) {
            MagnificationGestureHandler handler = this.mMagnificationGestureHandler.valueAt(i3);
            if (handler != null) {
                handler.onDestroy();
            }
        }
        this.mMagnificationGestureHandler.clear();
        KeyboardInterceptor keyboardInterceptor = this.mKeyboardInterceptor;
        if (keyboardInterceptor != null) {
            keyboardInterceptor.onDestroy();
            this.mKeyboardInterceptor = null;
        }
        this.mEventHandler.clear();
        resetStreamState();
    }

    /* access modifiers changed from: package-private */
    public void resetStreamState() {
        EventStreamState eventStreamState = this.mTouchScreenStreamState;
        if (eventStreamState != null) {
            eventStreamState.reset();
        }
        EventStreamState eventStreamState2 = this.mMouseStreamState;
        if (eventStreamState2 != null) {
            eventStreamState2.reset();
        }
        EventStreamState eventStreamState3 = this.mKeyboardStreamState;
        if (eventStreamState3 != null) {
            eventStreamState3.reset();
        }
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onDestroy() {
    }

    /* access modifiers changed from: private */
    public static class EventStreamState {
        private int mSource = -1;

        EventStreamState() {
        }

        public boolean updateInputSource(int source) {
            if (this.mSource == source) {
                return false;
            }
            reset();
            this.mSource = source;
            return true;
        }

        public boolean inputSourceValid() {
            return this.mSource >= 0;
        }

        public void reset() {
            this.mSource = -1;
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

    /* access modifiers changed from: private */
    public static class MouseEventStreamState extends EventStreamState {
        private boolean mMotionSequenceStarted;

        public MouseEventStreamState() {
            reset();
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
        public final void reset() {
            super.reset();
            this.mMotionSequenceStarted = false;
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
        public final boolean shouldProcessScroll() {
            return true;
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
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

    /* access modifiers changed from: private */
    public static class TouchScreenEventStreamState extends EventStreamState {
        private boolean mHoverSequenceStarted;
        private boolean mTouchSequenceStarted;

        public TouchScreenEventStreamState() {
            reset();
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
        public final void reset() {
            super.reset();
            this.mTouchSequenceStarted = false;
            this.mHoverSequenceStarted = false;
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
        public final boolean shouldProcessMotionEvent(MotionEvent event) {
            boolean z = false;
            if (event.isTouchEvent()) {
                if (this.mTouchSequenceStarted) {
                    return true;
                }
                if (event.getActionMasked() == 0) {
                    z = true;
                }
                this.mTouchSequenceStarted = z;
                return this.mTouchSequenceStarted;
            } else if (this.mHoverSequenceStarted) {
                return true;
            } else {
                if (event.getActionMasked() == 9) {
                    z = true;
                }
                this.mHoverSequenceStarted = z;
                return this.mHoverSequenceStarted;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class KeyboardEventStreamState extends EventStreamState {
        private SparseBooleanArray mEventSequenceStartedMap = new SparseBooleanArray();

        public KeyboardEventStreamState() {
            reset();
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
        public final void reset() {
            super.reset();
            this.mEventSequenceStartedMap.clear();
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
        public boolean updateInputSource(int deviceId) {
            return false;
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
        public boolean inputSourceValid() {
            return true;
        }

        @Override // com.android.server.accessibility.AccessibilityInputFilter.EventStreamState
        public final boolean shouldProcessKeyEvent(KeyEvent event) {
            int deviceId = event.getDeviceId();
            boolean shouldProcess = false;
            if (this.mEventSequenceStartedMap.get(deviceId, false)) {
                return true;
            }
            if (event.getAction() == 0) {
                shouldProcess = true;
            }
            this.mEventSequenceStartedMap.put(deviceId, shouldProcess);
            return shouldProcess;
        }
    }
}
