package com.android.internal.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogException;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class StateMachine {
    public static final boolean HANDLED = true;
    private static final boolean HWFLOW;
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
            Calendar.getInstance().setTimeInMillis(this.mTime);
            sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c}));
            sb.append(" processed=");
            sb.append(this.mState == null ? "<null>" : this.mState.getName());
            sb.append(" org=");
            sb.append(this.mOrgState == null ? "<null>" : this.mOrgState.getName());
            sb.append(" dest=");
            sb.append(this.mDstState == null ? "<null>" : this.mDstState.getName());
            sb.append(" what=");
            String what = this.mSm != null ? this.mSm.getWhatToString(this.mWhat) : LogException.NO_VALUE;
            if (TextUtils.isEmpty(what)) {
                sb.append(this.mWhat);
                sb.append("(0x");
                sb.append(Integer.toHexString(this.mWhat));
                sb.append(")");
            } else {
                sb.append(what);
            }
            if (!TextUtils.isEmpty(this.mInfo)) {
                sb.append(" ");
                sb.append(this.mInfo);
            }
            return sb.toString();
        }
    }

    private static class LogRecords {
        private static final int DEFAULT_SIZE = 20;
        private int mCount;
        private boolean mLogOnlyTransitions;
        private Vector<LogRec> mLogRecVector;
        private int mMaxSize;
        private int mOldestIndex;

        /* synthetic */ LogRecords(LogRecords -this0) {
            this();
        }

        private LogRecords() {
            this.mLogRecVector = new Vector();
            this.mMaxSize = 20;
            this.mOldestIndex = 0;
            this.mCount = 0;
            this.mLogOnlyTransitions = false;
        }

        synchronized void setSize(int maxSize) {
            this.mMaxSize = maxSize;
            this.mOldestIndex = 0;
            this.mCount = 0;
            this.mLogRecVector.clear();
        }

        synchronized void setLogOnlyTransitions(boolean enable) {
            this.mLogOnlyTransitions = enable;
        }

        synchronized boolean logOnlyTransitions() {
            return this.mLogOnlyTransitions;
        }

        synchronized int size() {
            return this.mLogRecVector.size();
        }

        synchronized int count() {
            return this.mCount;
        }

        synchronized void cleanup() {
            this.mLogRecVector.clear();
        }

        synchronized LogRec get(int index) {
            int nextIndex = this.mOldestIndex + index;
            if (nextIndex >= this.mMaxSize) {
                nextIndex -= this.mMaxSize;
            }
            if (nextIndex >= size()) {
                return null;
            }
            return (LogRec) this.mLogRecVector.get(nextIndex);
        }

        synchronized void add(StateMachine sm, Message msg, String messageInfo, IState state, IState orgState, IState transToState) {
            this.mCount++;
            if (this.mLogRecVector.size() < this.mMaxSize) {
                this.mLogRecVector.add(new LogRec(sm, msg, messageInfo, state, orgState, transToState));
            } else {
                LogRec pmi = (LogRec) this.mLogRecVector.get(this.mOldestIndex);
                this.mOldestIndex++;
                if (this.mOldestIndex >= this.mMaxSize) {
                    this.mOldestIndex = 0;
                }
                pmi.update(sm, msg, messageInfo, state, orgState, transToState);
            }
        }
    }

    private static class SmHandler extends Handler {
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

        private class HaltingState extends State {
            /* synthetic */ HaltingState(SmHandler this$1, HaltingState -this1) {
                this();
            }

            private HaltingState() {
            }

            public boolean processMessage(Message msg) {
                SmHandler.this.mSm.haltedProcessMessage(msg);
                return true;
            }
        }

        private class QuittingState extends State {
            /* synthetic */ QuittingState(SmHandler this$1, QuittingState -this1) {
                this();
            }

            private QuittingState() {
            }

            public boolean processMessage(Message msg) {
                return false;
            }
        }

        private class StateInfo {
            boolean active;
            StateInfo parentStateInfo;
            State state;

            /* synthetic */ StateInfo(SmHandler this$1, StateInfo -this1) {
                this();
            }

            private StateInfo() {
            }

            public String toString() {
                return "state=" + this.state.getName() + ",active=" + this.active + ",parent=" + (this.parentStateInfo == null ? "null" : this.parentStateInfo.state.getName());
            }
        }

        /* synthetic */ SmHandler(Looper looper, StateMachine sm, SmHandler -this2) {
            this(looper, sm);
        }

        public final void handleMessage(Message msg) {
            if (!this.mHasQuit) {
                if (!(this.mSm == null || msg.what == -2 || msg.what == -1)) {
                    this.mSm.onPreHandleMessage(msg);
                }
                if (this.mDbg) {
                    this.mSm.log("handleMessage: E msg.what=" + msg.what);
                }
                this.mMsg = msg;
                State msgProcessedState = null;
                if (this.mIsConstructionCompleted) {
                    msgProcessedState = processMsg(msg);
                } else if (!this.mIsConstructionCompleted && this.mMsg.what == -2 && this.mMsg.obj == mSmHandlerObj) {
                    this.mIsConstructionCompleted = true;
                    invokeEnterMethods(0);
                } else {
                    throw new RuntimeException("StateMachine.handleMessage: The start method not called, received msg: " + msg);
                }
                performTransitions(msgProcessedState, msg);
                if (this.mDbg && this.mSm != null) {
                    this.mSm.log("handleMessage: X");
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
                    this.mLogRecords.add(this.mSm, this.mMsg, this.mSm.getLogRecString(this.mMsg), msgProcessedState, orgState, this.mDestState);
                }
            } else if (recordLogMsg) {
                this.mLogRecords.add(this.mSm, this.mMsg, this.mSm.getLogRecString(this.mMsg), msgProcessedState, orgState, this.mDestState);
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

        private final void completeConstruction() {
            if (this.mDbg) {
                this.mSm.log("completeConstruction: E");
            }
            int maxDepth = 0;
            for (StateInfo i : this.mStateInfo.values()) {
                int depth = 0;
                StateInfo i2;
                while (i2 != null) {
                    i2 = i2.parentStateInfo;
                    depth++;
                }
                if (maxDepth < depth) {
                    maxDepth = depth;
                }
            }
            if (this.mDbg) {
                this.mSm.log("completeConstruction: maxDepth=" + maxDepth);
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
                this.mSm.log("processMsg: " + curStateInfo.state.getName());
            }
            if (isQuit(msg)) {
                transitionTo(this.mQuittingState);
            } else {
                while (!curStateInfo.state.processMessage(msg)) {
                    curStateInfo = curStateInfo.parentStateInfo;
                    if (curStateInfo == null) {
                        this.mSm.unhandledMessage(msg);
                        break;
                    } else if (this.mDbg) {
                        this.mSm.log("processMsg: " + curStateInfo.state.getName());
                    }
                }
            }
            if (curStateInfo != null) {
                return curStateInfo.state;
            }
            return null;
        }

        private final void invokeExitMethods(StateInfo commonStateInfo) {
            while (this.mStateStackTopIndex >= 0 && this.mStateStack[this.mStateStackTopIndex] != commonStateInfo) {
                State curState = this.mStateStack[this.mStateStackTopIndex].state;
                if (this.mDbg) {
                    this.mSm.log("invokeExitMethods: " + curState.getName());
                }
                curState.exit();
                this.mStateStack[this.mStateStackTopIndex].active = false;
                this.mStateStackTopIndex--;
            }
        }

        private final void invokeEnterMethods(int stateStackEnteringIndex) {
            for (int i = stateStackEnteringIndex; i <= this.mStateStackTopIndex; i++) {
                if (stateStackEnteringIndex == this.mStateStackTopIndex) {
                    this.mTransitionInProgress = false;
                }
                if (this.mDbg) {
                    this.mSm.log("invokeEnterMethods: " + this.mStateStack[i].state.getName());
                }
                this.mStateStack[i].state.enter();
                this.mStateStack[i].active = true;
            }
            this.mTransitionInProgress = false;
        }

        private final void moveDeferredMessageAtFrontOfQueue() {
            for (int i = this.mDeferredMessages.size() - 1; i >= 0; i--) {
                Message curMsg = (Message) this.mDeferredMessages.get(i);
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
            StateInfo curStateInfo = (StateInfo) this.mStateInfo.get(destState);
            do {
                StateInfo[] stateInfoArr = this.mTempStateStack;
                int i = this.mTempStateStackCount;
                this.mTempStateStackCount = i + 1;
                stateInfoArr[i] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
                if (curStateInfo == null) {
                    break;
                }
            } while ((curStateInfo.active ^ 1) != 0);
            if (this.mDbg) {
                this.mSm.log("setupTempStateStackWithStatesToEnter: X mTempStateStackCount=" + this.mTempStateStackCount + ",curStateInfo: " + curStateInfo);
            }
            return curStateInfo;
        }

        private final void setupInitialStateStack() {
            if (this.mDbg) {
                this.mSm.log("setupInitialStateStack: E mInitialState=" + this.mInitialState.getName());
            }
            StateInfo curStateInfo = (StateInfo) this.mStateInfo.get(this.mInitialState);
            this.mTempStateStackCount = 0;
            while (curStateInfo != null) {
                this.mTempStateStack[this.mTempStateStackCount] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
                this.mTempStateStackCount++;
            }
            this.mStateStackTopIndex = -1;
            moveTempStateStackToStateStack();
        }

        private final Message getCurrentMessage() {
            return this.mMsg;
        }

        private final IState getCurrentState() {
            if (this.mStateStack != null && this.mStateStackTopIndex >= 0) {
                return this.mStateStack[this.mStateStackTopIndex].state;
            }
            if (StateMachine.HWFLOW) {
                this.mSm.log("getCurrentState return null");
            }
            return null;
        }

        private final StateInfo addState(State state, State parent) {
            if (this.mDbg) {
                this.mSm.log("addStateInternal: E state=" + state.getName() + ",parent=" + (parent == null ? LogException.NO_VALUE : parent.getName()));
            }
            StateInfo parentStateInfo = null;
            if (parent != null) {
                parentStateInfo = (StateInfo) this.mStateInfo.get(parent);
                if (parentStateInfo == null) {
                    parentStateInfo = addState(parent, null);
                }
            }
            StateInfo stateInfo = (StateInfo) this.mStateInfo.get(state);
            if (stateInfo == null) {
                stateInfo = new StateInfo(this, null);
                this.mStateInfo.put(state, stateInfo);
            }
            if (stateInfo.parentStateInfo == null || stateInfo.parentStateInfo == parentStateInfo) {
                stateInfo.state = state;
                stateInfo.parentStateInfo = parentStateInfo;
                stateInfo.active = false;
                if (this.mDbg) {
                    this.mSm.log("addStateInternal: X stateInfo: " + stateInfo);
                }
                return stateInfo;
            }
            throw new RuntimeException("state already added");
        }

        /* JADX WARNING: Missing block: B:4:0x000e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void removeState(State state) {
            StateInfo stateInfo = (StateInfo) this.mStateInfo.get(state);
            if (stateInfo != null && !stateInfo.active && !this.mStateInfo.values().stream().filter(new -$Lambda$yemvYsjJALMa5EcxPVEO8dzsQUY(stateInfo)).findAny().isPresent()) {
                this.mStateInfo.remove(state);
            }
        }

        static /* synthetic */ boolean lambda$-com_android_internal_util_StateMachine$SmHandler_44565(StateInfo stateInfo, StateInfo si) {
            return si.parentStateInfo == stateInfo;
        }

        private SmHandler(Looper looper, StateMachine sm) {
            super(looper);
            this.mHasQuit = false;
            this.mDbg = false;
            this.mLogRecords = new LogRecords();
            this.mStateStackTopIndex = -1;
            this.mHaltingState = new HaltingState(this, null);
            this.mQuittingState = new QuittingState(this, null);
            this.mStateInfo = new HashMap();
            this.mTransitionInProgress = false;
            this.mDeferredMessages = new ArrayList();
            this.mSm = sm;
            addState(this.mHaltingState, null);
            addState(this.mQuittingState, null);
        }

        private final void setInitialState(State initialState) {
            if (this.mDbg) {
                this.mSm.log("setInitialState: initialState=" + initialState.getName());
            }
            this.mInitialState = initialState;
        }

        private final void transitionTo(IState destState) {
            if (this.mTransitionInProgress) {
                Log.wtf(this.mSm.mName, "transitionTo called while transition already in progress to " + this.mDestState + ", new target state=" + destState);
            }
            this.mDestState = (State) destState;
            if (this.mDbg) {
                this.mSm.log("transitionTo: destState=" + this.mDestState.getName());
            }
        }

        private final void deferMessage(Message msg) {
            if (this.mDbg) {
                this.mSm.log("deferMessage: msg=" + msg.what);
            }
            Message newMsg = obtainMessage();
            newMsg.copyFrom(msg);
            this.mDeferredMessages.add(newMsg);
        }

        private final void quit() {
            if (this.mDbg) {
                this.mSm.log("quit:");
            }
            sendMessage(obtainMessage(-1, mSmHandlerObj));
        }

        private final void quitNow() {
            if (this.mDbg) {
                this.mSm.log("quitNow:");
            }
            sendMessageAtFrontOfQueue(obtainMessage(-1, mSmHandlerObj));
        }

        private final boolean isQuit(Message msg) {
            return msg.what == -1 && msg.obj == mSmHandlerObj;
        }

        private final boolean isDbg() {
            return this.mDbg;
        }

        private final void setDbg(boolean dbg) {
            this.mDbg = dbg;
        }

        public String toString() {
            StateMachine sm = this.mSm;
            if (sm != null) {
                return super.toString() + "#" + sm.getName();
            }
            return super.toString();
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    private void initStateMachine(String name, Looper looper) {
        this.mName = name;
        this.mSmHandler = new SmHandler(looper, this, null);
    }

    protected StateMachine(String name) {
        this.mSmThread = new HandlerThread(name);
        this.mSmThread.start();
        initStateMachine(name, this.mSmThread.getLooper());
    }

    protected StateMachine(String name, Looper looper) {
        initStateMachine(name, looper);
    }

    protected StateMachine(String name, Handler handler) {
        initStateMachine(name, handler.getLooper());
    }

    protected void onPreHandleMessage(Message msg) {
    }

    protected void onPostHandleMessage(Message msg) {
    }

    public final void addState(State state, State parent) {
        this.mSmHandler.addState(state, parent);
    }

    public final void addState(State state) {
        this.mSmHandler.addState(state, null);
    }

    public final void removeState(State state) {
        this.mSmHandler.removeState(state);
    }

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

    public final void transitionTo(IState destState) {
        this.mSmHandler.transitionTo(destState);
        transitionToCallback(destState);
    }

    public final void transitionToHaltingState() {
        this.mSmHandler.transitionTo(this.mSmHandler.mHaltingState);
    }

    public final void deferMessage(Message msg) {
        this.mSmHandler.deferMessage(msg);
    }

    protected void unhandledMessage(Message msg) {
        if (this.mSmHandler.mDbg) {
            loge(" - unhandledMessage: msg.what=" + msg.what);
        }
    }

    protected void haltedProcessMessage(Message msg) {
    }

    protected void onHalting() {
    }

    protected void onQuitting() {
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
        Vector<LogRec> vlr = new Vector();
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            for (LogRec lr : smh.mLogRecords.mLogRecVector) {
                vlr.add(lr);
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

    protected boolean recordLogRec(Message msg) {
        return true;
    }

    protected String getLogRecString(Message msg) {
        return LogException.NO_VALUE;
    }

    protected String getWhatToString(int what) {
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

    public final Message obtainMessage(int what, int arg1, int arg2) {
        return Message.obtain(this.mSmHandler, what, arg1, arg2);
    }

    public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
        return Message.obtain(this.mSmHandler, what, arg1, arg2, obj);
    }

    public void sendMessage(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(obtainMessage(what));
        }
    }

    public void sendMessage(int what, Object obj) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(obtainMessage(what, obj));
        }
    }

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

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessage(obtainMessage(what, arg1, arg2, obj));
        }
    }

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

    protected final void sendMessageAtFrontOfQueue(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what));
        }
    }

    protected final void sendMessageAtFrontOfQueue(int what, Object obj) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what, obj));
        }
    }

    protected final void sendMessageAtFrontOfQueue(int what, int arg1) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what, arg1));
        }
    }

    protected final void sendMessageAtFrontOfQueue(int what, int arg1, int arg2) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what, arg1, arg2));
        }
    }

    protected final void sendMessageAtFrontOfQueue(int what, int arg1, int arg2, Object obj) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(obtainMessage(what, arg1, arg2, obj));
        }
    }

    protected final void sendMessageAtFrontOfQueue(Message msg) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.sendMessageAtFrontOfQueue(msg);
        }
    }

    protected final void removeMessages(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.removeMessages(what);
        }
    }

    protected final void removeDeferredMessages(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            Iterator<Message> iterator = smh.mDeferredMessages.iterator();
            while (iterator.hasNext()) {
                if (((Message) iterator.next()).what == what) {
                    iterator.remove();
                }
            }
        }
    }

    protected final boolean hasDeferredMessages(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return false;
        }
        Iterator<Message> iterator = smh.mDeferredMessages.iterator();
        while (iterator.hasNext()) {
            if (((Message) iterator.next()).what == what) {
                return true;
            }
        }
        return false;
    }

    protected final boolean hasMessages(int what) {
        SmHandler smh = this.mSmHandler;
        if (smh == null) {
            return false;
        }
        return smh.hasMessages(what);
    }

    protected final boolean isQuit(Message msg) {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            return smh.isQuit(msg);
        }
        return msg.what == -1;
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

    public void start() {
        SmHandler smh = this.mSmHandler;
        if (smh != null) {
            smh.completeConstruction();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(getName() + SettingsStringUtil.DELIMITER);
        pw.println(" total records=" + getLogRecCount());
        for (int i = 0; i < getLogRecSize(); i++) {
            pw.println(" rec[" + i + "]: " + getLogRec(i).toString());
            pw.flush();
        }
        if (getCurrentState() == null) {
            pw.println("curState missed.");
        } else {
            pw.println("curState=" + getCurrentState().getName());
        }
    }

    public String toString() {
        StringWriter sr = new StringWriter();
        PrintWriter pr = new PrintWriter(sr);
        dump(null, pr, new String[]{" "});
        pr.flush();
        pr.close();
        return sr.toString();
    }

    protected void logAndAddLogRec(String s) {
        addLogRec(s);
        log(s);
    }

    protected void log(String s) {
        Log.d(this.mName, s);
    }

    protected void logd(String s) {
        Log.d(this.mName, s);
    }

    protected void logv(String s) {
        Log.v(this.mName, s);
    }

    protected void logi(String s) {
        Log.i(this.mName, s);
    }

    protected void logw(String s) {
        Log.w(this.mName, s);
    }

    protected void loge(String s) {
        Log.e(this.mName, s);
    }

    protected void loge(String s, Throwable e) {
        Log.e(this.mName, s, e);
    }

    public void transitionToCallback(IState destState) {
    }
}
