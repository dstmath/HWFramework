package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

/* access modifiers changed from: package-private */
public class RilMessageDecoder extends StateMachine {
    private static final int CMD_PARAMS_READY = 2;
    private static final int CMD_START = 1;
    @UnsupportedAppUsage
    private static RilMessageDecoder[] mInstance = null;
    private static int mSimCount = 0;
    private Handler mCaller = null;
    @UnsupportedAppUsage
    private CommandParamsFactory mCmdParamsFactory = null;
    private RilMessage mCurrentRilMessage = null;
    private StateCmdParamsReady mStateCmdParamsReady = new StateCmdParamsReady();
    @UnsupportedAppUsage
    private StateStart mStateStart = new StateStart();

    @UnsupportedAppUsage
    public static synchronized RilMessageDecoder getInstance(Handler caller, IccFileHandler fh, int slotId) {
        synchronized (RilMessageDecoder.class) {
            if (mInstance == null) {
                mSimCount = TelephonyManager.getDefault().getSimCount();
                mInstance = new RilMessageDecoder[mSimCount];
                for (int i = 0; i < mSimCount; i++) {
                    mInstance[i] = null;
                }
            }
            if (slotId == -1 || slotId >= mSimCount) {
                CatLog.d("RilMessageDecoder", "invaild slot id: " + slotId);
                return null;
            }
            if (mInstance[slotId] == null) {
                mInstance[slotId] = new RilMessageDecoder(caller, fh);
            }
            return mInstance[slotId];
        }
    }

    public void sendStartDecodingMessageParams(RilMessage rilMsg) {
        Message msg = obtainMessage(1);
        msg.obj = rilMsg;
        sendMessage(msg);
    }

    public void sendMsgParamsDecoded(ResultCode resCode, CommandParams cmdParams) {
        Message msg = obtainMessage(2);
        msg.arg1 = resCode.value();
        msg.obj = cmdParams;
        sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCmdForExecution(RilMessage rilMsg) {
        this.mCaller.obtainMessage(10, new RilMessage(rilMsg)).sendToTarget();
    }

    private RilMessageDecoder(Handler caller, IccFileHandler fh) {
        super("RilMessageDecoder");
        addState(this.mStateStart);
        addState(this.mStateCmdParamsReady);
        setInitialState(this.mStateStart);
        this.mCaller = caller;
        this.mCmdParamsFactory = CommandParamsFactory.getInstance(this, fh);
    }

    private RilMessageDecoder() {
        super("RilMessageDecoder");
    }

    /* access modifiers changed from: private */
    public class StateStart extends State {
        private StateStart() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 1) {
                CatLog.d(this, "StateStart unexpected expecting START=1 got " + msg.what);
            } else if (RilMessageDecoder.this.decodeMessageParams((RilMessage) msg.obj)) {
                RilMessageDecoder rilMessageDecoder = RilMessageDecoder.this;
                rilMessageDecoder.transitionTo(rilMessageDecoder.mStateCmdParamsReady);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class StateCmdParamsReady extends State {
        private StateCmdParamsReady() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what == 2) {
                RilMessageDecoder.this.mCurrentRilMessage.mResCode = ResultCode.fromInt(msg.arg1);
                RilMessageDecoder.this.mCurrentRilMessage.mData = msg.obj;
                RilMessageDecoder rilMessageDecoder = RilMessageDecoder.this;
                rilMessageDecoder.sendCmdForExecution(rilMessageDecoder.mCurrentRilMessage);
                RilMessageDecoder rilMessageDecoder2 = RilMessageDecoder.this;
                rilMessageDecoder2.transitionTo(rilMessageDecoder2.mStateStart);
                return true;
            }
            CatLog.d(this, "StateCmdParamsReady expecting CMD_PARAMS_READY=2 got " + msg.what);
            RilMessageDecoder.this.deferMessage(msg);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean decodeMessageParams(RilMessage rilMsg) {
        this.mCurrentRilMessage = rilMsg;
        int i = rilMsg.mId;
        if (i != 1) {
            if (!(i == 2 || i == 3)) {
                if (i != 4) {
                    if (i != 5) {
                        return false;
                    }
                }
            }
            try {
                try {
                    this.mCmdParamsFactory.make(BerTlv.decode(IccUtils.hexStringToBytes((String) rilMsg.mData)));
                    return true;
                } catch (ResultException e) {
                    CatLog.d(this, "decodeMessageParams: caught ResultException e=" + e);
                    this.mCurrentRilMessage.mResCode = e.result();
                    RilMessage rilMessage = this.mCurrentRilMessage;
                    rilMessage.mData = null;
                    sendCmdForExecution(rilMessage);
                    return false;
                }
            } catch (Exception e2) {
                CatLog.d(this, "decodeMessageParams dropping zombie messages");
                return false;
            }
        }
        this.mCurrentRilMessage.mResCode = ResultCode.OK;
        sendCmdForExecution(this.mCurrentRilMessage);
        return false;
    }

    public void dispose() {
        quitNow();
        this.mStateStart = null;
        this.mStateCmdParamsReady = null;
        CommandParamsFactory commandParamsFactory = this.mCmdParamsFactory;
        if (commandParamsFactory != null) {
            commandParamsFactory.dispose();
        }
        this.mCmdParamsFactory = null;
        this.mCurrentRilMessage = null;
        this.mCaller = null;
        mInstance = null;
    }
}
