package com.android.internal.util;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Predicate;

public class StateMachine {
    public static final boolean HANDLED = true;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    public static final boolean NOT_HANDLED = false;
    private static final int SM_INIT_CMD = -2;
    private static final int SM_QUIT_CMD = -1;
    private static final String TAG = "StateMachine";
    private String mName;
    private SmHandler mSmHandler;
    private HandlerThread mSmThread;

    public static class LogRec {
        private IState mDstState;
        private String mInfo;
        private IState mOrgState;
        private StateMachine mSm;
        private IState mState;
        private long mTime;
        private int mWhat;

        LogRec(StateMachine sm, Message msg, String info, IState state, IState orgState, IState transToState) {
            update(sm, msg, info, state, orgState, transToState);
        }

        public void update(StateMachine sm, Message msg, String info, IState state, IState orgState, IState dstState) {
            this.mSm = sm;
            this.mTime = System.currentTimeMillis();
            this.mWhat = msg != null ? msg.what : 0;
            this.mInfo = info;
            this.mState = state;
            this.mOrgState = orgState;
            this.mDstState = dstState;
        }

        public long getTime() {
            return this.mTime;
        }

        public long getWhat() {
            return (long) this.mWhat;
        }

        public String getInfo() {
            return this.mInfo;
        }

        public IState getState() {
            return this.mState;
        }

        public IState getDestState() {
            return this.mDstState;
        }

        public IState getOriginalState() {
            return this.mOrgState;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("time=");
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(this.mTime);
            sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", c, c, c, c, c, c));
            sb.append(" processed=");
            IState iState = this.mState;
            String str = "<null>";
            sb.append(iState == null ? str : iState.getName());
            sb.append(" org=");
            IState iState2 = this.mOrgState;
            sb.append(iState2 == null ? str : iState2.getName());
            sb.append(" dest=");
            IState iState3 = this.mDstState;
            if (iState3 != null) {
                str = iState3.getName();
            }
            sb.append(str);
            sb.append(" what=");
            StateMachine stateMachine = this.mSm;
            String what = stateMachine != null ? stateMachine.getWhatToString(this.mWhat) : "";
            if (TextUtils.isEmpty(what)) {
                sb.append(this.mWhat);
                sb.append("(0x");
                sb.append(Integer.toHexString(this.mWhat));
                sb.append(")");
            } else {
                sb.append(what);
            }
            if (!TextUtils.isEmpty(this.mInfo)) {
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                sb.append(this.mInfo);
            }
            return sb.toString();
        }
    }

    /* access modifiers changed from: private */
    public static class LogRecords {
        private static final int DEFAULT_SIZE = 20;
        private int mCount;
        private boolean mLogOnlyTransitions;
        private Vector<LogRec> mLogRecVector;
        private int mMaxSize;
        private int mOldestIndex;

        private LogRecords() {
            this.mLogRecVector = new Vector<>();
            this.mMaxSize = 20;
            this.mOldestIndex = 0;
            this.mCount = 0;
            this.mLogOnlyTransitions = false;
        }

        /* access modifiers changed from: package-private */
        public synchronized void setSize(int maxSize) {
            this.mMaxSize = maxSize;
            this.mOldestIndex = 0;
            this.mCount = 0;
            this.mLogRecVector.clear();
        }

        /* access modifiers changed from: package-private */
        public synchronized void setLogOnlyTransitions(boolean enable) {
            this.mLogOnlyTransitions = enable;
        }

        /* access modifiers changed from: package-private */
        public synchronized boolean logOnlyTransitions() {
            return this.mLogOnlyTransitions;
        }

        /* access modifiers changed from: package-private */
        public synchronized int size() {
            return this.mLogRecVector.size();
        }

        /* access modifiers changed from: package-private */
        public synchronized int count() {
            return this.mCount;
        }

        /* access modifiers changed from: package-private */
        public synchronized void cleanup() {
            this.mLogRecVector.clear();
        }

        /* access modifiers changed from: package-private */
        public synchronized LogRec get(int index) {
            int nextIndex = this.mOldestIndex + index;
            if (nextIndex >= this.mMaxSize) {
                nextIndex -= this.mMaxSize;
            }
            if (nextIndex >= size()) {
                return null;
            }
            return this.mLogRecVector.get(nextIndex);
        }

        /* access modifiers changed from: package-private */
        public synchronized void add(StateMachine sm, Message msg, String messageInfo, IState state, IState orgState, IState transToState) {
            this.mCount++;
            if (this.mLogRecVector.size() < this.mMaxSize) {
                this.mLogRecVector.add(new LogRec(sm, msg, messageInfo, state, orgState, transToState));
            } else {
                LogRec pmi = this.mLogRecVector.get(this.mOldestIndex);
                this.mOldestIndex++;
                if (this.mOldestIndex >= this.mMaxSize) {
                    this.mOldestIndex = 0;
                }
                pmi.update(sm, msg, messageInfo, state, orgState, transToState);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SmHandler extends Handler {
        private static final Object mSmHandlerObj = new Object();
        private boolean mDbg;
        private ArrayList<Message> mDeferredMessages;
        private State mDestState;
        private HaltingState mHaltingState;
        private boolean mHasQuit;
        private State mInitialState;
        private boolean mIsConstructionCompleted;
        private LogRecords mLogRecords;
        private Message mMsg;
        private QuittingState mQuittingState;
        private StateMachine mSm;
        private HashMap<State, StateInfo> mStateInfo;
        private StateInfo[] mStateStack;
        private int mStateStackTopIndex;
        private StateInfo[] mTempStateStack;
        private int mTempStateStackCount;
        private boolean mTransitionInProgress;

        /* access modifiers changed from: private */
        public class StateInfo {
            boolean active;
            StateInfo parentStateInfo;
            State state;

            private StateInfo() {
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("state=");
                sb.append(this.state.getName());
                sb.append(",active=");
                sb.append(this.active);
                sb.append(",parent=");
                StateInfo stateInfo = this.parentStateInfo;
                sb.append(stateInfo == null ? "null" : stateInfo.state.getName());
                return sb.toString();
            }
        }

        /* access modifiers changed from: private */
        public class HaltingState extends State {
            private HaltingState() {
            }

            @Override // com.android.internal.util.State, com.android.internal.util.IState
            public boolean processMessage(Message msg) {
                SmHandler.this.mSm.haltedProcessMessage(msg);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public class QuittingState extends State {
            private QuittingState() {
            }

            @Override // com.android.internal.util.State, com.android.internal.util.IState
            public boolean processMessage(Message msg) {
                return false;
            }
        }

        @Override // android.os.Handler
        public final void handleMessage(Message msg) {
            StateMachine stateMachine;
            if (!this.mHasQuit) {
                if (!(this.mSm == null || msg.what == -2 || msg.what == -1)) {
                    this.mSm.onPreHandleMessage(msg);
                }
                if (this.mDbg) {
                    StateMachine stateMachine2 = this.mSm;
                    stateMachine2.log("handleMessage: E msg.what=" + msg.what);
                }
                this.mMsg = msg;
                State msgProcessedState = null;
                if (this.mIsConstructionCompleted || this.mMsg.what == -1) {
                    msgProcessedState = processMsg(msg);
                } else if (!this.mIsConstructionCompleted && this.mMsg.what == -2 && this.mMsg.obj == mSmHandlerObj) {
                    this.mIsConstructionCompleted = true;
                    invokeEnterMethods(0);
                } else {
                    throw new RuntimeException("StateMachine.handleMessage: The start method not called, received msg: " + msg);
                }
                performTransitions(msgProcessedState, msg);
                if (this.mDbg && (stateMachine = this.mSm) != null) {
                    stateMachine.log("handleMessage: X");
                }
                if (this.mSm != null && msg.what != -2 && msg.what != -1) {
                    this.mSm.onPostHandleMessage(msg);
                }
            }
        }

        private void performTransitions(State msgProcessedState, Message msg) {
            State orgState = this.mStateStack[this.mStateStackTopIndex].state;
            boolean recordLogMsg = this.mSm.recordLogRec(this.mMsg) && msg.obj != mSmHandlerObj;
            if (this.mLogRecords.logOnlyTransitions()) {
                if (this.mDestState != null) {
                    LogRecords logRecords = this.mLogRecords;
                    StateMachine stateMachine = this.mSm;
                    Message message = this.mMsg;
                    logRecords.add(stateMachine, message, stateMachine.getLogRecString(message), msgProcessedState, orgState, this.mDestState);
                }
            } else if (recordLogMsg) {
                LogRecords logRecords2 = this.mLogRecords;
                StateMachine stateMachine2 = this.mSm;
                Message message2 = this.mMsg;
                logRecords2.add(stateMachine2, message2, stateMachine2.getLogRecString(message2), msgProcessedState, orgState, this.mDestState);
            }
            State destState = this.mDestState;
            if (destState != null) {
                while (true) {
                    if (this.mDbg) {
                        this.mSm.log("handleMessage: new destination call exit/enter");
                    }
                    StateInfo commonStateInfo = setupTempStateStackWithStatesToEnter(destState);
                    this.mTransitionInProgress = true;
                    invokeExitMethods(commonStateInfo);
                    invokeEnterMethods(moveTempStateStackToStateStack());
                    moveDeferredMessageAtFrontOfQueue();
                    if (destState == this.mDestState) {
                        break;
                    }
                    destState = this.mDestState;
                }
                this.mDestState = null;
            }
            if (destState == null) {
                return;
            }
            if (destState == this.mQuittingState) {
                this.mSm.onQuitting();
                cleanupAfterQuitting();
            } else if (destState == this.mHaltingState) {
                this.mSm.onHalting();
            }
        }

        private final void cleanupAfterQuitting() {
            if (this.mSm.mSmThread != null) {
                getLooper().quit();
                this.mSm.mSmThread = null;
            }
            this.mSm.mSmHandler = null;
            this.mSm = null;
            this.mMsg = null;
            this.mLogRecords.cleanup();
            this.mStateStack = null;
            this.mTempStateStack = null;
            this.mStateInfo.clear();
            this.mInitialState = null;
            this.mDestState = null;
            this.mDeferredMessages.clear();
            this.mHasQuit = true;
            this.mStateStackTopIndex = -1;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final void completeConstruction() {
            if (this.mDbg) {
                this.mSm.log("completeConstruction: E");
            }
            int maxDepth = 0;
            for (StateInfo i : this.mStateInfo.values()) {
                int depth = 0;
                while (i != null) {
                    i = i.parentStateInfo;
                    depth++;
                }
                if (maxDepth < depth) {
                    maxDepth = depth;
                }
            }
            if (this.mDbg) {
                StateMachine stateMachine = this.mSm;
                stateMachine.log("completeConstruction: maxDepth=" + maxDepth);
            }
            this.mStateStack = new StateInfo[maxDepth];
            this.mTempStateStack = new StateInfo[maxDepth];
            setupInitialStateStack();
            sendMessageAtFrontOfQueue(obtainMessage(-2, mSmHandlerObj));
            if (this.mDbg) {
                this.mSm.log("completeConstruction: X");
            }
        }

        private final State processMsg(Message msg) {
            StateInfo curStateInfo = this.mStateStack[this.mStateStackTopIndex];
            if (this.mDbg) {
                StateMachine stateMachine = this.mSm;
                stateMachine.log("processMsg: " + curStateInfo.state.getName());
            }
            if (isQuit(msg)) {
                transitionTo(this.mQuittingState);
            } else {
                while (true) {
                    if (curStateInfo.state.processMessage(msg)) {
                        break;
                    }
                    curStateInfo = curStateInfo.parentStateInfo;
                    if (curStateInfo == null) {
                        this.mSm.unhandledMessage(msg);
                        break;
                    } else if (this.mDbg) {
                        StateMachine stateMachine2 = this.mSm;
                        stateMachine2.log("processMsg: " + curStateInfo.state.getName());
                    }
                }
            }
            if (curStateInfo != null) {
                return curStateInfo.state;
            }
            return null;
        }

        private final void invokeExitMethods(StateInfo commonStateInfo) {
            while (true) {
                int i = this.mStateStackTopIndex;
                if (i >= 0) {
                    StateInfo[] stateInfoArr = this.mStateStack;
                    if (stateInfoArr[i] != commonStateInfo) {
                        State curState = stateInfoArr[i].state;
                        if (this.mDbg) {
                            StateMachine stateMachine = this.mSm;
                            stateMachine.log("invokeExitMethods: " + curState.getName());
                        }
                        curState.exit();
                        StateInfo[] stateInfoArr2 = this.mStateStack;
                        int i2 = this.mStateStackTopIndex;
                        stateInfoArr2[i2].active = false;
                        this.mStateStackTopIndex = i2 - 1;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        private final void invokeEnterMethods(int stateStackEnteringIndex) {
            int i = stateStackEnteringIndex;
            while (true) {
                int i2 = this.mStateStackTopIndex;
                if (i <= i2) {
                    if (stateStackEnteringIndex == i2) {
                        this.mTransitionInProgress = false;
                    }
                    if (this.mDbg) {
                        StateMachine stateMachine = this.mSm;
                        stateMachine.log("invokeEnterMethods: " + this.mStateStack[i].state.getName());
                    }
                    this.mStateStack[i].state.enter();
                    this.mStateStack[i].active = true;
                    i++;
                } else {
                    this.mTransitionInProgress = false;
                    return;
                }
            }
        }

        private final void moveDeferredMessageAtFrontOfQueue() {
            for (int i = this.mDeferredMessages.size() - 1; i >= 0; i--) {
                Message curMsg = this.mDeferredMessages.get(i);
                if (this.mDbg) {
                    this.mSm.log("moveDeferredMessageAtFrontOfQueue; what=" + curMsg.what);
                }
                sendMessageAtFrontOfQueue(curMsg);
            }
            this.mDeferredMessages.clear();
        }

        private final int moveTempStateStackToStateStack() {
            int startingIndex = this.mStateStackTopIndex + 1;
            int j = startingIndex;
            for (int i = this.mTempStateStackCount - 1; i >= 0; i--) {
                if (this.mDbg) {
                    this.mSm.log("moveTempStackToStateStack: i=" + i + ",j=" + j);
                }
                this.mStateStack[j] = this.mTempStateStack[i];
                j++;
            }
            this.mStateStackTopIndex = j - 1;
            if (this.mDbg) {
                this.mSm.log("moveTempStackToStateStack: X mStateStackTop=" + this.mStateStackTopIndex + ",startingIndex=" + startingIndex + ",Top=" + this.mStateStack[this.mStateStackTopIndex].state.getName());
            }
            return startingIndex;
        }

        private final StateInfo setupTempStateStackWithStatesToEnter(State destState) {
            this.mTempStateStackCount = 0;
            StateInfo curStateInfo = this.mStateInfo.get(destState);
            do {
                StateInfo[] stateInfoArr = this.mTempStateStack;
                int i = this.mTempStateStackCount;
                this.mTempStateStackCount = i + 1;
                stateInfoArr[i] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
                if (curStateInfo == null) {
                    break;
                }
            } while (!curStateInfo.active);
            if (this.mDbg) {
                StateMachine stateMachine = this.mSm;
                stateMachine.log("setupTempStateStackWithStatesToEnter: X mTempStateStackCount=" + this.mTempStateStackCount + ",curStateInfo: " + curStateInfo);
            }
            return curStateInfo;
        }

        private final void setupInitialStateStack() {
            if (this.mDbg) {
                StateMachine stateMachine = this.mSm;
                stateMachine.log("setupInitialStateStack: E mInitialState=" + this.mInitialState.getName());
            }
            StateInfo curStateInfo = this.mStateInfo.get(this.mInitialState);
            int i = 0;
            while (true) {
                this.mTempStateStackCount = i;
                if (curStateInfo != null) {
                    this.mTempStateStack[this.mTempStateStackCount] = curStateInfo;
                    curStateInfo = curStateInfo.parentStateInfo;
                    i = this.mTempStateStackCount + 1;
                } else {
                    this.mStateStackTopIndex = -1;
                    moveTempStateStackToStateStack();
                    return;
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final Message getCurrentMessage() {
            return this.mMsg;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final IState getCurrentState() {
            int i;
            StateInfo[] stateInfoArr = this.mStateStack;
            if (stateInfoArr != null && (i = this.mStateStackTopIndex) >= 0) {
                return stateInfoArr[i].state;
            }
            if (!StateMachine.HWFLOW) {
                return null;
            }
            this.mSm.log("getCurrentState return null");
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final StateInfo addState(State state, State parent) {
            if (this.mDbg) {
                StateMachine stateMachine = this.mSm;
                StringBuilder sb = new StringBuilder();
                sb.append("addStateInternal: E state=");
                sb.append(state.getName());
                sb.append(",parent=");
                sb.append(parent == null ? "" : parent.getName());
                stateMachine.log(sb.toString());
            }
            StateInfo parentStateInfo = null;
            if (parent != null && (parentStateInfo = this.mStateInfo.get(parent)) == null) {
                parentStateInfo = addState(parent, null);
            }
            StateInfo stateInfo = this.mStateInfo.get(state);
            if (stateInfo == null) {
                stateInfo = new StateInfo();
                this.mStateInfo.put(state, stateInfo);
            }
            if (stateInfo.parentStateInfo == null || stateInfo.parentStateInfo == parentStateInfo) {
                stateInfo.state = state;
                stateInfo.parentStateInfo = parentStateInfo;
                stateInfo.active = false;
                if (this.mDbg) {
                    StateMachine stateMachine2 = this.mSm;
                    stateMachine2.log("addStateInternal: X stateInfo: " + stateInfo);
                }
                return stateInfo;
            }
            throw new RuntimeException("state already added");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeState(State state) {
            StateInfo stateInfo = this.mStateInfo.get(state);
            if (stateInfo != null && !stateInfo.active && !this.mStateInfo.values().stream().filter(new Predicate() {
                /* class com.android.internal.util.$$Lambda$StateMachine$SmHandler$KkPO7NIVuI9r_FPEGrY6ux6a5Ks */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return StateMachine.SmHandler.lambda$removeState$0(StateMachine.SmHandler.StateInfo.this, (StateMachine.SmHandler.StateInfo) obj);
                }
            }).findAny().isPresent()) {
                this.mStateInfo.remove(state);
            }
        }

        static /* synthetic */ boolean lambda$removeState$0(StateInfo stateInfo, StateInfo si) {
            return si.parentStateInfo == stateInfo;
        }

        private SmHandler(Looper looper, StateMachine sm) {
            super(looper);
            this.mHasQuit = false;
            this.mDbg = false;
            this.mLogRecords = new LogRecords();
            this.mStateStackTopIndex = -1;
            this.mHaltingState = new HaltingState();
            this.mQuittingState = new QuittingState();
            this.mStateInfo = new HashMap<>();
            this.mTransitionInProgress = false;
            this.mDeferredMessages = new ArrayList<>();
            this.mSm = sm;
            addState(this.mHaltingState, null);
            addState(this.mQuittingState, null);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final void setInitialState(State initialState) {
            if (this.mDbg) {
                StateMachine stateMachine = this.mSm;
                stateMachine.log("setInitialState: initialState=" + initialState.getName());
            }
            this.mInitialState = initialState;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final void transitionTo(IState destState) {
            if (this.mTransitionInProgress) {
                String str = this.mSm.mName;
                Log.wtf(str, "transitionTo called while transition already in progress to " + this.mDestState + ", new target state=" + destState);
            }
            this.mDestState = (State) destState;
            if (this.mDbg) {
                StateMachine stateMachine = this.mSm;
                stateMachine.log("transitionTo: destState=" + this.mDestState.getName());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final void deferMessage(Message msg) {
            if (this.mDbg) {
                StateMachine stateMachine = this.mSm;
                stateMachine.log("deferMessage: msg=" + msg.what);
            }
            Message newMsg = obtainMessage();
            newMsg.copyFrom(msg);
            this.mDeferredMessages.add(newMsg);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final void quit() {
            if (this.mDbg) {
                this.mSm.log("quit:");
            }
            sendMessage(obtainMessage(-1, mSmHandlerObj));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final void quitNow() {
            if (this.mDbg) {
                this.mSm.log("quitNow:");
            }
            sendMessageAtFrontOfQueue(obtainMessage(-1, mSmHandlerObj));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final boolean isQuit(Message msg) {
            return msg.what == -1 && msg.obj == mSmHandlerObj;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final boolean isDbg() {
            return this.mDbg;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private final void setDbg(boolean dbg) {
            this.mDbg = dbg;
        }
    }

    private void initStateMachine(String name, Looper looper) {
        this.mName = name;
        this.mSmHandler = new SmHandler(looper, this);
    }

    @UnsupportedAppUsage
    protected StateMachine(String name) {
        this.mSmThread = new HandlerThread(name);
        this.mSmThread.start();
        initStateMachine(name, this.mSmThread.getLooper());
    }

    @UnsupportedAppUsage
    protected StateMachine(String name, Looper looper) {
        initStateMachine(name, looper);
    }

    @UnsupportedAppUsage
    protected StateMachine(String name, Handler handler) {
        initStateMachine(name, handler.getLooper());
    }

    /* access modifiers changed from: protected */
    public void onPreHandleMessage(Message msg) {
    }

    /* access modifiers changed from: protected */
    public void onPostHandleMessage(Message msg) {
    }

    public final void addState(State state, State parent) {
        this.mSmHandler.addState(state, parent);
    }

    @UnsupportedAppUsage
    public final void addState(State state) {
        this.mSmHandler.addState(state, null);
    }

    public final void removeState(State state) {
        this.mSmHandler.removeState(state);
    }

    @UnsupportedAppUsage
    public final void setInitialState(State initialState) {
        this.mSmHandler.setInitialState(initialState);
    }

    public final Message getCurrentMessage() {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return null;
        }
        return smh.getCurrentMessage();
    }

    public final IState getCurrentState() {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return null;
        }
        return smh.getCurrentState();
    }

    @UnsupportedAppUsage
    public final void transitionTo(IState destState) {
        this.mSmHandler.transitionTo(destState);
    }

    public final void transitionToHaltingState() {
        SmHandler smHandler = this.mSmHandler;
        smHandler.transitionTo(smHandler.mHaltingState);
    }

    public final void deferMessage(Message msg) {
        this.mSmHandler.deferMessage(msg);
    }

    /* access modifiers changed from: protected */
    public void unhandledMessage(Message msg) {
        if (this.mSmHandler.mDbg) {
            loge(" - unhandledMessage: msg.what=" + msg.what);
        }
    }

    /* access modifiers changed from: protected */
    public void haltedProcessMessage(Message msg) {
    }

    /* access modifiers changed from: protected */
    public void onHalting() {
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
    }

    public final String getName() {
        return this.mName;
    }

    public final void setLogRecSize(int maxSize) {
        this.mSmHandler.mLogRecords.setSize(maxSize);
    }

    public final void setLogOnlyTransitions(boolean enable) {
        this.mSmHandler.mLogRecords.setLogOnlyTransitions(enable);
    }

    public final int getLogRecSize() {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return 0;
        }
        return smh.mLogRecords.size();
    }

    @VisibleForTesting
    public final int getLogRecMaxSize() {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return 0;
        }
        return smh.mLogRecords.mMaxSize;
    }

    public final int getLogRecCount() {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return 0;
        }
        return smh.mLogRecords.count();
    }

    public final LogRec getLogRec(int index) {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return null;
        }
        return smh.mLogRecords.get(index);
    }

    public final Collection<LogRec> copyLogRecs() {
        Vector<LogRec> vlr = new Vector<>();
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            Iterator it = smh.mLogRecords.mLogRecVector.iterator();
            while (it.hasNext()) {
                vlr.add((LogRec) it.next());
            }
        }
        return vlr;
    }

    public void addLogRec(String string) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.mLogRecords.add(this, smh.getCurrentMessage(), string, smh.getCurrentState(), smh.mStateStack[smh.mStateStackTopIndex].state, smh.mDestState);
        }
    }

    /* access modifiers changed from: protected */
    public boolean recordLogRec(Message msg) {
        return true;
    }

    /* access modifiers changed from: protected */
    public String getLogRecString(Message msg) {
        return "";
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        return null;
    }

    public final Handler getHandler() {
        return this.mSmHandler;
    }

    public final Message obtainMessage() {
        return Message.obtain(this.mSmHandler);
    }

    public final Message obtainMessage(int what) {
        return Message.obtain(this.mSmHandler, what);
    }

    public final Message obtainMessage(int what, Object obj) {
        return Message.obtain(this.mSmHandler, what, obj);
    }

    public final Message obtainMessage(int what, int arg1) {
        return Message.obtain(this.mSmHandler, what, arg1, 0);
    }

    @UnsupportedAppUsage
    public final Message obtainMessage(int what, int arg1, int arg2) {
        return Message.obtain(this.mSmHandler, what, arg1, arg2);
    }

    @UnsupportedAppUsage
    public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
        return Message.obtain(this.mSmHandler, what, arg1, arg2, obj);
    }

    @UnsupportedAppUsage
    public void sendMessage(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(obtainMessage(what));
        }
    }

    @UnsupportedAppUsage
    public void sendMessage(int what, Object obj) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(obtainMessage(what, obj));
        }
    }

    @UnsupportedAppUsage
    public void sendMessage(int what, int arg1) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(obtainMessage(what, arg1));
        }
    }

    public void sendMessage(int what, int arg1, int arg2) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(obtainMessage(what, arg1, arg2));
        }
    }

    @UnsupportedAppUsage
    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(obtainMessage(what, arg1, arg2, obj));
        }
    }

    @UnsupportedAppUsage
    public void sendMessage(Message msg) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(msg);
        }
    }

    public void sendMessageDelayed(int what, long delayMillis) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageDelayed(obtainMessage(what), delayMillis);
        }
    }

    public void sendMessageDelayed(int what, Object obj, long delayMillis) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageDelayed(obtainMessage(what, obj), delayMillis);
        }
    }

    public void sendMessageDelayed(int what, int arg1, long delayMillis) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageDelayed(obtainMessage(what, arg1), delayMillis);
        }
    }

    public void sendMessageDelayed(int what, int arg1, int arg2, long delayMillis) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageDelayed(obtainMessage(what, arg1, arg2), delayMillis);
        }
    }

    public void sendMessageDelayed(int what, int arg1, int arg2, Object obj, long delayMillis) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageDelayed(obtainMessage(what, arg1, arg2, obj), delayMillis);
        }
    }

    public void sendMessageDelayed(Message msg, long delayMillis) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageDelayed(msg, delayMillis);
        }
    }

    /* access modifiers changed from: protected */
    public final void sendMessageAtFrontOfQueue(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what));
        }
    }

    /* access modifiers changed from: protected */
    public final void sendMessageAtFrontOfQueue(int what, Object obj) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what, obj));
        }
    }

    /* access modifiers changed from: protected */
    public final void sendMessageAtFrontOfQueue(int what, int arg1) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what, arg1));
        }
    }

    /* access modifiers changed from: protected */
    public final void sendMessageAtFrontOfQueue(int what, int arg1, int arg2) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what, arg1, arg2));
        }
    }

    /* access modifiers changed from: protected */
    public final void sendMessageAtFrontOfQueue(int what, int arg1, int arg2, Object obj) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what, arg1, arg2, obj));
        }
    }

    /* access modifiers changed from: protected */
    public final void sendMessageAtFrontOfQueue(Message msg) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(msg);
        }
    }

    /* access modifiers changed from: protected */
    public final void removeMessages(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.removeMessages(what);
        }
    }

    /* access modifiers changed from: protected */
    public final void removeDeferredMessages(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            Iterator<Message> iterator = smh.mDeferredMessages.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().what == what) {
                    iterator.remove();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public final boolean hasDeferredMessages(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return false;
        }
        Iterator<Message> iterator = smh.mDeferredMessages.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().what == what) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public final boolean hasDeferredMessagesForArg1(int what, int arg1) {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return false;
        }
        Iterator<Message> iterator = smh.mDeferredMessages.iterator();
        while (iterator.hasNext()) {
            Message msg = iterator.next();
            if (msg.what == what && msg.arg1 == arg1) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public final boolean hasMessages(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return false;
        }
        return smh.hasMessages(what);
    }

    /* access modifiers changed from: protected */
    public final boolean isQuit(Message msg) {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return msg.what == -1;
        }
        return smh.isQuit(msg);
    }

    public final void quit() {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.quit();
        }
    }

    public final void quitNow() {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.quitNow();
        }
    }

    public boolean isDbg() {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return false;
        }
        return smh.isDbg();
    }

    public void setDbg(boolean dbg) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.setDbg(dbg);
        }
    }

    @UnsupportedAppUsage
    public void start() {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.completeConstruction();
        }
    }

    @UnsupportedAppUsage
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(getName() + SettingsStringUtil.DELIMITER);
        pw.println(" total records=" + getLogRecCount());
        for (int i = 0; i < getLogRecSize(); i++) {
            pw.println(" rec[" + i + "]: " + getLogRec(i).toString());
            pw.flush();
        }
        if (getCurrentState() != null) {
            pw.println("curState=" + getCurrentState().getName());
        }
    }

    public String toString() {
        String name = "(null)";
        String state = "(null)";
        try {
            name = this.mName.toString();
            state = this.mSmHandler.getCurrentState().getName().toString();
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
        }
        return "name=" + name + " state=" + state;
    }

    /* access modifiers changed from: protected */
    public void logAndAddLogRec(String s) {
        addLogRec(s);
        log(s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Log.d(this.mName, s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Log.d(this.mName, s);
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        Log.v(this.mName, s);
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        Log.i(this.mName, s);
    }

    /* access modifiers changed from: protected */
    public void logw(String s) {
        Log.w(this.mName, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Log.e(this.mName, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        Log.e(this.mName, s, e);
    }
}
