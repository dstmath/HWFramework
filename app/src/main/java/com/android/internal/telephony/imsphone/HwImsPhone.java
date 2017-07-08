package com.android.internal.telephony.imsphone;

import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.ims.ImsException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPersister;

public class HwImsPhone extends Handler {
    private static final int EVENT_GET_CLIR_DONE = 102;
    private static final int EVENT_HW_IMS = 100;
    private static final int EVENT_SET_CALL_FORWARD_TIMER_DONE = 103;
    private static final int EVENT_SET_CLIR_DONE = 101;
    private static final String LOG_TAG = "HwImsPhone";
    private boolean isBusy;
    boolean isImsEvent;
    ImsPhoneCallTracker mCT;

    private static class Cf {
        final boolean mIsCfu;
        final Message mOnComplete;
        final String mSetCfNumber;

        Cf(String cfNumber, boolean isCfu, Message onComplete) {
            this.mSetCfNumber = cfNumber;
            this.mIsCfu = isCfu;
            this.mOnComplete = onComplete;
        }

        String getCfNumber() {
            return this.mSetCfNumber;
        }

        boolean getIsCfu() {
            return this.mIsCfu;
        }
    }

    public HwImsPhone(ImsPhoneCallTracker ct) {
        this.isBusy = false;
        this.mCT = ct;
    }

    public void handleMessage(Message msg) {
        AsyncResult ar = msg.obj;
        this.isImsEvent = true;
        switch (msg.what) {
            case EVENT_SET_CLIR_DONE /*101*/:
                sendResponse((Message) ar.userObj, null, ar.exception);
                break;
            case EVENT_GET_CLIR_DONE /*102*/:
                if (!(ar.result instanceof Bundle)) {
                    sendResponse((Message) ar.userObj, ar.result, ar.exception);
                    break;
                }
                Object clirInfo = null;
                Bundle ssInfo = ar.result;
                if (ssInfo != null) {
                    clirInfo = ssInfo.getIntArray(ImsPhoneMmiCode.UT_BUNDLE_KEY_CLIR);
                }
                sendResponse((Message) ar.userObj, clirInfo, ar.exception);
                break;
            case EVENT_SET_CALL_FORWARD_TIMER_DONE /*103*/:
                sendResponse(ar.userObj.mOnComplete, null, ar.exception);
                break;
            default:
                this.isImsEvent = false;
                break;
        }
        Rlog.d(LOG_TAG, "handleMessage what=" + msg.what + ", isImsEvent = " + this.isImsEvent);
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        Rlog.d(LOG_TAG, "getCLIR");
        try {
            this.mCT.getUtInterface().queryCLIR(obtainMessage(EVENT_GET_CLIR_DONE, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setOutgoingCallerIdDisplay(int clirMode, Message onComplete) {
        Rlog.d(LOG_TAG, "setCLIR action= " + clirMode);
        try {
            this.mCT.getUtInterface().updateCLIR(clirMode, obtainMessage(EVENT_SET_CLIR_DONE, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public boolean isSupportCFT() {
        Rlog.d(LOG_TAG, "isSupportCFT");
        boolean isSupportCFT = false;
        try {
            isSupportCFT = this.mCT.getUtInterface().isSupportCFT();
        } catch (ImsException e) {
            Rlog.e(LOG_TAG, "e=" + e);
        }
        return isSupportCFT;
    }

    public boolean isUtEnable() {
        try {
            return !this.mCT.getUtInterface().isUtEnable() ? this.mCT.isUtEnabledForQcom() : true;
        } catch (ImsException e) {
            Rlog.e(LOG_TAG, "ImsException e=" + e);
            return false;
        }
    }

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        Rlog.d(LOG_TAG, "setCallForwardingUncondTimerOption action=" + commandInterfaceCFAction + ", reason=" + commandInterfaceCFReason + ", startHour=" + startHour + ", startMinute=" + startMinute + ", endHour=" + endHour + ", endMinute=" + endMinute);
        if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            try {
                this.mCT.getUtInterface().updateCallForwardUncondTimer(startHour, startMinute, endHour, endMinute, getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber, obtainMessage(EVENT_SET_CALL_FORWARD_TIMER_DONE, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cf(dialingNumber, commandInterfaceCFReason == 0, onComplete)));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public Message popUtMessage(int id) {
        try {
            return this.mCT.getUtInterface().popUtMessage(id);
        } catch (ImsException e) {
            Rlog.e(LOG_TAG, "ImsException e=" + e);
            return null;
        }
    }

    void sendErrorResponse(Message onComplete, Throwable e) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, getCommandException(e));
            onComplete.sendToTarget();
        }
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
            case CharacterSets.ISO_8859_1 /*4*/:
            case CharacterSets.ISO_8859_2 /*5*/:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
            case CharacterSets.ISO_8859_1 /*4*/:
                return true;
            default:
                return false;
        }
    }

    private void sendResponse(Message onComplete, Object result, Throwable e) {
        if (onComplete != null) {
            if (e != null) {
                AsyncResult.forMessage(onComplete, result, getCommandException(e));
            } else {
                AsyncResult.forMessage(onComplete, result, null);
            }
            onComplete.sendToTarget();
        }
    }

    void sendErrorResponse(Message onComplete) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, new CommandException(Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    CommandException getCommandException(int code) {
        Rlog.d(LOG_TAG, "getCommandException code=" + code);
        Error error = Error.GENERIC_FAILURE;
        switch (code) {
            case 801:
                error = Error.REQUEST_NOT_SUPPORTED;
                break;
            case 821:
                error = Error.PASSWORD_INCORRECT;
                break;
            case 831:
                error = Error.UT_NO_CONNECTION;
                break;
        }
        return new CommandException(error);
    }

    CommandException getCommandException(Throwable e) {
        if (e instanceof ImsException) {
            return getCommandException(((ImsException) e).getCode());
        }
        if (e instanceof CommandException) {
            Rlog.d(LOG_TAG, "e instanceof CommandException  : " + e);
            return new CommandException(((CommandException) e).getCommandError());
        }
        Rlog.d(LOG_TAG, "getCommandException generic failure");
        return new CommandException(Error.GENERIC_FAILURE);
    }

    public boolean beforeHandleMessage(Message msg) {
        handleMessage(msg);
        return this.isImsEvent;
    }

    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    private int getActionFromCFAction(int action) {
        switch (action) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                return 0;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return 1;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return 3;
            case CharacterSets.ISO_8859_1 /*4*/:
                return 4;
            default:
                return -1;
        }
    }

    private int getConditionFromCFReason(int reason) {
        switch (reason) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                return 0;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return 1;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return 2;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return 3;
            case CharacterSets.ISO_8859_1 /*4*/:
                return 4;
            case CharacterSets.ISO_8859_2 /*5*/:
                return 5;
            default:
                return -1;
        }
    }

    public void setIsBusy(boolean isImsPhoneBusy) {
        this.isBusy = isImsPhoneBusy;
    }

    public boolean isBusy() {
        return this.isBusy;
    }

    public void disableUTForQcom() {
        this.mCT.disableUTForQcom();
    }

    public void enableUTForQcom() {
        this.mCT.enableUTForQcom();
    }
}
