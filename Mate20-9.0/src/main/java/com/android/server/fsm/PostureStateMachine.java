package com.android.server.fsm;

import android.hardware.display.DisplayManagerInternal;
import android.os.Binder;
import android.os.Message;
import android.util.Slog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.LocalServices;
import java.util.HashMap;
import java.util.Map;

public class PostureStateMachine extends StateMachine {
    private static final int SM_CHANGE_POSTURE_CMD = 0;
    private static final String TAG = "Fsm_PostureStateMachine";
    private static PostureStateMachine mInstance = null;
    private boolean mAppLocked = false;
    /* access modifiers changed from: private */
    public int mDisplayMode = 0;
    /* access modifiers changed from: private */
    public int mDisplayRectForDoubleClick = 0;
    private final DisplayManagerInternal mDm = ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class));
    private State mFoldedState = new FoldedState();
    private State mFullState = new FullState();
    private State mHalfFoldedState = new HalfFoldedState();
    private State mHandheldFoldedMainState = new HandheldFoldedMainState();
    private State mHandheldFoldedSubState = new HandheldFoldedSubState();
    private State mLayFlatMainUpState = new LayFlatMainUpState();
    private State mLayFlatSubUpState = new LayFlatSubUpState();
    /* access modifiers changed from: private */
    public int mPosture = 100;
    private final Map<Integer, State> mPostureMap = new HashMap();
    /* access modifiers changed from: private */
    public HwFoldScreenManagerService mService;

    private class FoldedState extends PostureState {
        private FoldedState() {
            super();
        }

        public void enter() {
            if (super.isNeedEnter() && (PostureStateMachine.this.mPosture == 109 || PostureStateMachine.this.mPosture == 100)) {
                PostureStateMachine.this.setDisplayModeInner(2, false);
                int unused = PostureStateMachine.this.mDisplayMode = 2;
            }
            int unused2 = PostureStateMachine.this.mPosture = 103;
        }

        public int getPosture() {
            return 103;
        }

        public int getFoldableState() {
            return 2;
        }
    }

    private class FullState extends PostureState {
        private FullState() {
            super();
        }

        public void enter() {
            Slog.d("Fsm_PostureStateMachine", "FullState enter. mDisplayRectForDoubleClick = " + PostureStateMachine.this.mDisplayRectForDoubleClick + "DisplayMode: " + PostureStateMachine.this.mDisplayMode);
            if (PostureStateMachine.this.mDisplayMode == 4) {
                PostureStateMachine.this.mService.exitCoordinationDisplayMode();
            }
            PostureStateMachine.this.setDisplayModeInner(1, false);
            int unused = PostureStateMachine.this.mDisplayMode = 1;
            int unused2 = PostureStateMachine.this.mDisplayRectForDoubleClick = 0;
            int unused3 = PostureStateMachine.this.mPosture = 109;
        }

        public int getPosture() {
            return 109;
        }

        public int getFoldableState() {
            return 1;
        }

        public int setDisplayMode(int mode) {
            Slog.d("Fsm_PostureStateMachine", "FullState: setDisplayMode mode = " + mode + " old DisplayMode = " + PostureStateMachine.this.mDisplayMode);
            if (mode != 1) {
                return PostureStateMachine.this.mDisplayMode;
            }
            if (PostureStateMachine.this.mDisplayMode != mode) {
                PostureStateMachine.this.setDisplayModeInner(mode, true);
                int unused = PostureStateMachine.this.mDisplayMode = mode;
            }
            return PostureStateMachine.this.mDisplayMode;
        }
    }

    private class HalfFoldedState extends PostureState {
        private HalfFoldedState() {
            super();
        }

        public void enter() {
            if (super.isNeedEnter() && (PostureStateMachine.this.mPosture == 109 || PostureStateMachine.this.mPosture == 100)) {
                PostureStateMachine.this.setDisplayModeInner(2, false);
                int unused = PostureStateMachine.this.mDisplayMode = 2;
            }
            int unused2 = PostureStateMachine.this.mPosture = 106;
        }

        public int getPosture() {
            return 106;
        }

        public int getFoldableState() {
            return 3;
        }
    }

    private class HandheldFoldedMainState extends PostureState {
        private HandheldFoldedMainState() {
            super();
        }

        public void enter() {
            if (super.isNeedEnter()) {
                PostureStateMachine.this.setDisplayModeInner(3, false);
                int unused = PostureStateMachine.this.mDisplayMode = 3;
            }
            int unused2 = PostureStateMachine.this.mPosture = 104;
        }

        public int getPosture() {
            return 104;
        }

        public int getFoldableState() {
            return 2;
        }
    }

    private class HandheldFoldedSubState extends PostureState {
        private HandheldFoldedSubState() {
            super();
        }

        public void enter() {
            if (super.isNeedEnter()) {
                PostureStateMachine.this.setDisplayModeInner(2, false);
                int unused = PostureStateMachine.this.mDisplayMode = 2;
            }
            int unused2 = PostureStateMachine.this.mPosture = 105;
        }

        public int getPosture() {
            return 105;
        }

        public int getFoldableState() {
            return 2;
        }
    }

    private class LayFlatMainUpState extends PostureState {
        private LayFlatMainUpState() {
            super();
        }

        public void enter() {
            if (super.isNeedEnter()) {
                PostureStateMachine.this.setDisplayModeInner(2, false);
                int unused = PostureStateMachine.this.mDisplayMode = 2;
            }
            int unused2 = PostureStateMachine.this.mPosture = 101;
        }

        public int getPosture() {
            return 101;
        }

        public int getFoldableState() {
            return 2;
        }
    }

    private class LayFlatSubUpState extends PostureState {
        private LayFlatSubUpState() {
            super();
        }

        public void enter() {
            if (super.isNeedEnter()) {
                PostureStateMachine.this.setDisplayModeInner(3, false);
                int unused = PostureStateMachine.this.mDisplayMode = 3;
            }
            int unused2 = PostureStateMachine.this.mPosture = 102;
        }

        public int getPosture() {
            return 102;
        }

        public int getFoldableState() {
            return 2;
        }
    }

    private abstract class PostureState extends State {
        /* access modifiers changed from: protected */
        public abstract int getFoldableState();

        /* access modifiers changed from: protected */
        public abstract int getPosture();

        private PostureState() {
        }

        /* access modifiers changed from: protected */
        public boolean isNeedEnter() {
            Slog.d("Fsm_PostureStateMachine", getName() + " enter. mDisplayRectForDoubleClick = " + PostureStateMachine.this.mDisplayRectForDoubleClick + "DisplayMode: " + PostureStateMachine.this.mDisplayMode);
            if (PostureStateMachine.this.mDisplayMode == 4) {
                return false;
            }
            if (PostureStateMachine.this.mDisplayRectForDoubleClick == 2) {
                int unused = PostureStateMachine.this.mDisplayRectForDoubleClick = 0;
                return false;
            } else if (PostureStateMachine.this.mDisplayRectForDoubleClick == 4) {
                PostureStateMachine.this.setDisplayModeInner(3, false);
                int unused2 = PostureStateMachine.this.mDisplayMode = 3;
                int unused3 = PostureStateMachine.this.mDisplayRectForDoubleClick = 0;
                return false;
            } else if (PostureStateMachine.this.mDisplayRectForDoubleClick != 1) {
                return true;
            } else {
                PostureStateMachine.this.setDisplayModeInner(2, false);
                int unused4 = PostureStateMachine.this.mDisplayMode = 2;
                int unused5 = PostureStateMachine.this.mDisplayRectForDoubleClick = 0;
                return false;
            }
        }

        /* access modifiers changed from: protected */
        public int setDisplayMode(int mode) {
            Slog.d("Fsm_PostureStateMachine", getName() + ": setDisplayMode mode = " + mode + " old DisplayMode = " + PostureStateMachine.this.mDisplayMode);
            if (1 == mode) {
                return PostureStateMachine.this.mDisplayMode;
            }
            if (PostureStateMachine.this.mDisplayMode != mode) {
                if (PostureStateMachine.this.mDisplayMode == 4) {
                    PostureStateMachine.this.mService.exitCoordinationDisplayMode();
                }
                PostureStateMachine.this.setDisplayModeInner(mode, true);
                int unused = PostureStateMachine.this.mDisplayMode = mode;
            }
            return PostureStateMachine.this.mDisplayMode;
        }
    }

    private PostureStateMachine(String name) {
        super(name);
        initStateMap();
    }

    private void initStateMap() {
        if (this.mPostureMap != null) {
            this.mPostureMap.put(101, this.mLayFlatMainUpState);
            this.mPostureMap.put(102, this.mLayFlatSubUpState);
            this.mPostureMap.put(103, this.mFoldedState);
            this.mPostureMap.put(104, this.mHandheldFoldedMainState);
            this.mPostureMap.put(105, this.mHandheldFoldedSubState);
            this.mPostureMap.put(106, this.mHalfFoldedState);
            this.mPostureMap.put(109, this.mFullState);
        }
    }

    /* access modifiers changed from: package-private */
    public void init(HwFoldScreenManagerService service) {
        this.mService = service;
        addStates();
        setInitialState(this.mFullState);
    }

    private void addStates() {
        for (int i = 101; i <= 109; i++) {
            State state = this.mPostureMap.get(Integer.valueOf(i));
            if (state != null) {
                addState(state);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDisplayModeInner(int displayMode, boolean needClear) {
        if (needClear) {
            long origId = Binder.clearCallingIdentity();
            try {
                this.mDm.setDisplayMode(displayMode);
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            this.mDm.setDisplayMode(displayMode);
        }
        this.mService.notifyDisplayModeChange(displayMode);
    }

    /* access modifiers changed from: protected */
    public void onPostHandleMessage(Message msg) {
        if (msg.what == 0) {
            this.mService.notifyPostureChange(getPosture());
            this.mService.notifyFoldStateChange(getFoldableState());
        }
    }

    public static synchronized PostureStateMachine getInstance() {
        PostureStateMachine postureStateMachine;
        synchronized (PostureStateMachine.class) {
            if (mInstance == null) {
                mInstance = new PostureStateMachine("Fsm_PostureStateMachine");
            }
            postureStateMachine = mInstance;
        }
        return postureStateMachine;
    }

    /* access modifiers changed from: package-private */
    public void setPosture(int posture) {
        this.mService.removeForceWakeUp();
        transitionTo(this.mPostureMap.get(Integer.valueOf(posture)));
        sendMessage(0);
    }

    public int getPosture() {
        State posture = getCurrentState();
        if (posture == null || !(posture instanceof PostureState)) {
            return 100;
        }
        return ((PostureState) posture).getPosture();
    }

    public int getFoldableState() {
        State posture = getCurrentState();
        if (posture == null || !(posture instanceof PostureState)) {
            return 0;
        }
        return ((PostureState) posture).getFoldableState();
    }

    public int setDisplayMode(int mode) {
        State posture = getCurrentState();
        Slog.d("Fsm_PostureStateMachine", "setDisplayMode mode=" + mode + " CurrentPosture=" + posture);
        if (posture == null || !(posture instanceof PostureState)) {
            return 0;
        }
        return ((PostureState) posture).setDisplayMode(mode);
    }

    public int getDisplayMode() {
        return this.mDisplayMode;
    }

    /* access modifiers changed from: protected */
    public void setDisplayRectForDoubleClick(int displayRect) {
        this.mDisplayRectForDoubleClick = displayRect;
    }

    /* access modifiers changed from: protected */
    public int doubleClickToSetDisplayMode(int displayRect) {
        int displayMode;
        int posture = getPosture();
        Slog.i("Fsm_PostureStateMachine", "doubleClickToSetDisplayMode displayRect:" + displayRect + ", posture:" + posture + "DisplayMode: " + this.mDisplayMode);
        if (this.mDisplayMode == 4 || displayRect == 2 || displayRect == 0) {
            return this.mDisplayMode;
        }
        if (posture == 109) {
            return this.mDisplayMode;
        }
        if (displayRect == 1) {
            displayMode = 2;
        } else {
            displayMode = 3;
        }
        return setDisplayMode(displayMode);
    }

    /* access modifiers changed from: protected */
    public void handleFlipPosture() {
        int posture = getPosture();
        Slog.i("Fsm_PostureStateMachine", "handleFlipPosture posture:" + posture + ", mAppLocked:" + this.mAppLocked + "DisplayMode: " + this.mDisplayMode);
        if (!this.mAppLocked && this.mDisplayMode != 4 && posture != 109 && posture != 106) {
            int displayMode = getDisplayMode();
            if (displayMode == 2) {
                setDisplayMode(3);
            } else if (displayMode == 3) {
                setDisplayMode(2);
            }
            setPosture(103);
        }
    }

    /* access modifiers changed from: protected */
    public void notifySleep() {
        Slog.i("Fsm_PostureStateMachine", "notifySleep");
        this.mPosture = 100;
        this.mDisplayMode = 0;
    }
}
