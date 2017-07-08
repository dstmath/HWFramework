package com.android.internal.telephony.dataconnection;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.HwVSimDcSwitchAsyncChannel.RequestInfo;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class HwVSimDcSwitchStateMachine extends StateMachine {
    private static final int BASE = 274432;
    private static final int CMD_RETRY_ATTACH = 274434;
    private static final int EVENT_CONNECTED = 274432;
    private static final int EVENT_DATA_ALLOWED = 274433;
    private static final int EVENT_DATA_DISALLOWED = 274435;
    private static final String LOG_TAG = "VSimDcSwitchSM";
    private AsyncChannel mAc;
    private AttachedState mAttachedState;
    private AttachingState mAttachingState;
    private DefaultState mDefaultState;
    private DetachingState mDetachingState;
    private int mId;
    private IdleState mIdleState;
    private Phone mPhone;
    private HwVSimDctController mVSimDctController;

    private class AttachedState extends State {
        private AttachedState() {
        }

        public void enter() {
            HwVSimDcSwitchStateMachine.this.logd("AttachedState: enter");
            HwVSimDcSwitchStateMachine.this.mVSimDctController.executeAllRequests(HwVSimDcSwitchStateMachine.this.mId);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 278528:
                    RequestInfo apnRequest = msg.obj;
                    apnRequest.log("DcSwitchStateMachine.AttachedState: REQ_CONNECT");
                    HwVSimDcSwitchStateMachine.this.logd("AttachedState: REQ_CONNECT, apnRequest=" + apnRequest);
                    HwVSimDcSwitchStateMachine.this.mVSimDctController.executeRequest(apnRequest);
                    return true;
                case 278530:
                    HwVSimDcSwitchStateMachine.this.logd("AttachedState: REQ_DISCONNECT_ALL");
                    HwVSimDcSwitchStateMachine.this.mVSimDctController.releaseAllRequests(HwVSimDcSwitchStateMachine.this.mId);
                    HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mDetachingState);
                    return true;
                case 278536:
                    HwVSimDcSwitchStateMachine.this.logd("AttachedState: EVENT_DATA_DETACHED");
                    HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mAttachingState);
                    return true;
                default:
                    HwVSimDcSwitchStateMachine.this.logv("AttachedState: nothandled msg.what=0x" + Integer.toHexString(msg.what));
                    return false;
            }
        }
    }

    private class AttachingState extends State {
        private int mCurrentAllowedSequence;

        private AttachingState() {
            this.mCurrentAllowedSequence = 0;
        }

        public void enter() {
            HwVSimDcSwitchStateMachine.this.log("AttachingState: enter");
            doEnter();
        }

        private void doEnter() {
            CommandsInterface commandsInterface = HwVSimDcSwitchStateMachine.this.mPhone.mCi;
            HwVSimDcSwitchStateMachine hwVSimDcSwitchStateMachine = HwVSimDcSwitchStateMachine.this;
            int i = this.mCurrentAllowedSequence + 1;
            this.mCurrentAllowedSequence = i;
            commandsInterface.setDataAllowed(true, hwVSimDcSwitchStateMachine.obtainMessage(HwVSimDcSwitchStateMachine.EVENT_DATA_ALLOWED, i, 0));
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HwVSimDcSwitchStateMachine.EVENT_DATA_ALLOWED /*274433*/:
                    AsyncResult ar = msg.obj;
                    if (this.mCurrentAllowedSequence != msg.arg1) {
                        HwVSimDcSwitchStateMachine.this.loge("EVENT_DATA_ALLOWED ignored arg1=" + msg.arg1 + ", seq=" + this.mCurrentAllowedSequence);
                    } else if (ar.exception != null) {
                        if (ar.exception instanceof CommandException) {
                            return true;
                        }
                        HwVSimDcSwitchStateMachine.this.loge("EVENT_DATA_ALLOWED failed, " + ar.exception);
                        HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mIdleState);
                    }
                    HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mAttachedState);
                    return true;
                case 278528:
                    RequestInfo apnRequest = msg.obj;
                    apnRequest.log("DcSwitchStateMachine.AttachingState: REQ_CONNECT");
                    HwVSimDcSwitchStateMachine.this.logd("AttachingState: REQ_CONNECT, apnRequest=" + apnRequest);
                    return true;
                case 278529:
                    HwVSimDcSwitchStateMachine.this.logd("AttachingState going to retry");
                    doEnter();
                    return true;
                case 278530:
                    HwVSimDcSwitchStateMachine.this.logd("AttachingState: REQ_DISCONNECT_ALL");
                    HwVSimDcSwitchStateMachine.this.deferMessage(msg);
                    return true;
                case 278535:
                    HwVSimDcSwitchStateMachine.this.logd("AttachingState: EVENT_DATA_ATTACHED");
                    HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mAttachedState);
                    return true;
                default:
                    HwVSimDcSwitchStateMachine.this.logv("AttachingState: nothandled msg.what=0x" + Integer.toHexString(msg.what));
                    return false;
            }
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            int i = 0;
            boolean val;
            AsyncChannel -get0;
            switch (msg.what) {
                case 69633:
                    if (HwVSimDcSwitchStateMachine.this.mAc == null) {
                        HwVSimDcSwitchStateMachine.this.mAc = new AsyncChannel();
                        HwVSimDcSwitchStateMachine.this.mAc.connected(null, HwVSimDcSwitchStateMachine.this.getHandler(), msg.replyTo);
                        HwVSimDcSwitchStateMachine.this.logv("DcDefaultState: FULL_CONNECTION reply connected");
                        HwVSimDcSwitchStateMachine.this.mAc.replyToMessage(msg, 69634, 0, HwVSimDcSwitchStateMachine.this.mId, "hi");
                        break;
                    }
                    HwVSimDcSwitchStateMachine.this.logv("Disconnecting to previous connection mAc=" + HwVSimDcSwitchStateMachine.this.mAc);
                    HwVSimDcSwitchStateMachine.this.mAc.replyToMessage(msg, 69634, 3);
                    break;
                case 69635:
                    HwVSimDcSwitchStateMachine.this.logv("CMD_CHANNEL_DISCONNECT");
                    HwVSimDcSwitchStateMachine.this.mAc.disconnect();
                    break;
                case 69636:
                    HwVSimDcSwitchStateMachine.this.logv("CMD_CHANNEL_DISCONNECTED");
                    HwVSimDcSwitchStateMachine.this.mAc = null;
                    break;
                case 278531:
                    val = HwVSimDcSwitchStateMachine.this.getCurrentState() == HwVSimDcSwitchStateMachine.this.mIdleState;
                    HwVSimDcSwitchStateMachine.this.logv("REQ_IS_IDLE_STATE  isIdle=" + val);
                    -get0 = HwVSimDcSwitchStateMachine.this.mAc;
                    if (val) {
                        i = 1;
                    }
                    -get0.replyToMessage(msg, 278532, i);
                    break;
                case 278533:
                    val = HwVSimDcSwitchStateMachine.this.getCurrentState() != HwVSimDcSwitchStateMachine.this.mIdleState ? HwVSimDcSwitchStateMachine.this.getCurrentState() == HwVSimDcSwitchStateMachine.this.mDetachingState : true;
                    HwVSimDcSwitchStateMachine.this.logv("REQ_IS_IDLE_OR_DETACHING_STATE  isIdleDetaching=" + val);
                    -get0 = HwVSimDcSwitchStateMachine.this.mAc;
                    if (val) {
                        i = 1;
                    }
                    -get0.replyToMessage(msg, 278534, i);
                    break;
                default:
                    HwVSimDcSwitchStateMachine.this.logd("DefaultState: shouldn't happen but ignore msg.what=0x" + Integer.toHexString(msg.what));
                    break;
            }
            return true;
        }
    }

    private class DetachingState extends State {
        private int mCurrentDisallowedSequence;

        private DetachingState() {
            this.mCurrentDisallowedSequence = 0;
        }

        public void enter() {
            HwVSimDcSwitchStateMachine.this.logd("DetachingState: enter");
            CommandsInterface commandsInterface = HwVSimDcSwitchStateMachine.this.mPhone.mCi;
            HwVSimDcSwitchStateMachine hwVSimDcSwitchStateMachine = HwVSimDcSwitchStateMachine.this;
            int i = this.mCurrentDisallowedSequence + 1;
            this.mCurrentDisallowedSequence = i;
            commandsInterface.setDataAllowed(false, hwVSimDcSwitchStateMachine.obtainMessage(HwVSimDcSwitchStateMachine.EVENT_DATA_DISALLOWED, i, 0));
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HwVSimDcSwitchStateMachine.EVENT_DATA_DISALLOWED /*274435*/:
                    AsyncResult ar = msg.obj;
                    if (this.mCurrentDisallowedSequence != msg.arg1) {
                        HwVSimDcSwitchStateMachine.this.loge("EVENT_DATA_DISALLOWED ignored arg1=" + msg.arg1 + ", seq=" + this.mCurrentDisallowedSequence);
                    } else if (ar.exception != null) {
                        HwVSimDcSwitchStateMachine.this.loge("EVENT_DATA_DISALLOWED failed, " + ar.exception);
                        HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mAttachedState);
                    }
                    HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mIdleState);
                    return true;
                case 278528:
                    RequestInfo apnRequest = msg.obj;
                    apnRequest.log("DcSwitchStateMachine.DetachingState: REQ_CONNECT");
                    HwVSimDcSwitchStateMachine.this.logd("DetachingState: REQ_CONNECT, apnRequest=" + apnRequest);
                    HwVSimDcSwitchStateMachine.this.deferMessage(msg);
                    return true;
                case 278530:
                    HwVSimDcSwitchStateMachine.this.logd("DetachingState: REQ_DISCONNECT_ALL, already detaching");
                    return true;
                case 278536:
                    HwVSimDcSwitchStateMachine.this.logd("DetachingState: EVENT_DATA_DETACHED");
                    HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mIdleState);
                    return true;
                default:
                    HwVSimDcSwitchStateMachine.this.logv("DetachingState: nothandled msg.what=0x" + Integer.toHexString(msg.what));
                    return false;
            }
        }
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            HwVSimDcSwitchStateMachine.this.logd("IdleState: enter");
            try {
                HwVSimDcSwitchStateMachine.this.mVSimDctController.processRequests();
            } catch (RuntimeException e) {
                HwVSimDcSwitchStateMachine.this.loge("DctController is not ready");
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HwVSimDcSwitchStateMachine.EVENT_CONNECTED /*274432*/:
                    HwVSimDcSwitchStateMachine.this.logd("IdleState: Receive invalid event EVENT_CONNECTED!");
                    return true;
                case 278528:
                    RequestInfo apnRequest = msg.obj;
                    apnRequest.log("DcSwitchStateMachine.IdleState: REQ_CONNECT");
                    HwVSimDcSwitchStateMachine.this.logd("IdleState: REQ_CONNECT, apnRequest=" + apnRequest);
                    HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mAttachingState);
                    return true;
                case 278530:
                    HwVSimDcSwitchStateMachine.this.logd("AttachingState: REQ_DISCONNECT_ALL");
                    HwVSimDcSwitchStateMachine.this.mVSimDctController.releaseAllRequests(HwVSimDcSwitchStateMachine.this.mId);
                    return true;
                case 278535:
                    HwVSimDcSwitchStateMachine.this.logd("IdleState: EVENT_DATA_ATTACHED");
                    HwVSimDcSwitchStateMachine.this.transitionTo(HwVSimDcSwitchStateMachine.this.mAttachedState);
                    return true;
                default:
                    HwVSimDcSwitchStateMachine.this.logv("IdleState: nothandled msg.what=0x" + Integer.toHexString(msg.what));
                    return false;
            }
        }
    }

    protected HwVSimDcSwitchStateMachine(HwVSimDctController vimDctController, Phone phone, String name, int id) {
        super(name);
        this.mIdleState = new IdleState();
        this.mAttachingState = new AttachingState();
        this.mAttachedState = new AttachedState();
        this.mDetachingState = new DetachingState();
        this.mDefaultState = new DefaultState();
        logd("DcSwitchState constructor E");
        this.mPhone = phone;
        this.mId = id;
        this.mVSimDctController = vimDctController;
        addState(this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mAttachingState, this.mDefaultState);
        addState(this.mAttachedState, this.mDefaultState);
        addState(this.mDetachingState, this.mDefaultState);
        setInitialState(this.mIdleState);
        logd("DcSwitchState constructor X");
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void loge(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void logv(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }
}
