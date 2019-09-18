package com.android.internal.telephony.imsphone;

import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.ims.HwImsUtManager;
import com.android.ims.ImsException;
import com.android.ims.ImsUt;
import com.android.internal.telephony.CommandException;

public class HwImsPhoneEx extends Handler implements IHwImsPhoneEx {
    private static final int EVENT_GET_CLIR_DONE = 102;
    private static final int EVENT_HW_IMS = 100;
    private static final int EVENT_SET_CALL_FORWARD_TIMER_DONE = 103;
    private static final int EVENT_SET_CLIR_DONE = 101;
    private static final String LOG_TAG = "HwImsPhoneEx";
    private boolean isBusy = false;
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

        /* access modifiers changed from: package-private */
        public String getCfNumber() {
            return this.mSetCfNumber;
        }

        /* access modifiers changed from: package-private */
        public boolean getIsCfu() {
            return this.mIsCfu;
        }
    }

    public HwImsPhoneEx(ImsPhoneCallTracker ct) {
        this.mCT = ct;
    }

    public void handleMessage(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        this.isImsEvent = true;
        switch (msg.what) {
            case EVENT_SET_CLIR_DONE /*101*/:
                sendResponse((Message) ar.userObj, null, ar.exception);
                break;
            case EVENT_GET_CLIR_DONE /*102*/:
                if (!(ar.result instanceof Bundle)) {
                    sendResponse((Message) ar.userObj, ar.result, ar.exception);
                    break;
                } else {
                    int[] clirInfo = null;
                    Bundle ssInfo = (Bundle) ar.result;
                    if (ssInfo != null) {
                        clirInfo = ssInfo.getIntArray("queryClir");
                    }
                    sendResponse((Message) ar.userObj, clirInfo, ar.exception);
                    break;
                }
            case EVENT_SET_CALL_FORWARD_TIMER_DONE /*103*/:
                Cf cft = (Cf) ar.userObj;
                if (cft != null) {
                    sendResponse(cft.mOnComplete, null, ar.exception);
                    break;
                }
                break;
            default:
                this.isImsEvent = false;
                break;
        }
        Rlog.d(LOG_TAG, "handleMessage what=" + msg.what + ", isImsEvent = " + this.isImsEvent);
    }

    public boolean isSupportCFT() {
        Rlog.d(LOG_TAG, "isSupportCFT");
        if (this.mCT == null || this.mCT.mPhone == null) {
            return false;
        }
        boolean isSupportCFT = HwImsUtManager.isSupportCFT(this.mCT.mPhone.getPhoneId());
        Rlog.d(LOG_TAG, "isSupportCFT is " + isSupportCFT);
        return isSupportCFT;
    }

    public boolean isUtEnable() {
        boolean z = false;
        if (this.mCT == null || this.mCT.mPhone == null) {
            return false;
        }
        if (this.mCT.getPhoneType() == 6 && !this.mCT.isVolteEnabledByPlatform()) {
            return false;
        }
        if (HwImsUtManager.isUtEnable(this.mCT.mPhone.getPhoneId()) || this.mCT.isUtEnabledForQcom()) {
            z = true;
        }
        return z;
    }

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        int i = commandInterfaceCFAction;
        int i2 = commandInterfaceCFReason;
        Message message = onComplete;
        StringBuilder sb = new StringBuilder();
        sb.append("setCallForwardingUncondTimerOption action=");
        sb.append(i);
        sb.append(", reason=");
        sb.append(i2);
        sb.append(", startHour=");
        int i3 = startHour;
        sb.append(i3);
        sb.append(", startMinute=");
        int i4 = startMinute;
        sb.append(i4);
        sb.append(", endHour=");
        int i5 = endHour;
        sb.append(i5);
        sb.append(", endMinute=");
        int i6 = endMinute;
        sb.append(i6);
        Rlog.d(LOG_TAG, sb.toString());
        if (!isValidCommandInterfaceCFAction(i) || !isValidCommandInterfaceCFReason(i2) || this.mCT == null || this.mCT.mPhone == null) {
            sendErrorResponse(message);
            return;
        }
        Message resp = obtainMessage(EVENT_SET_CALL_FORWARD_TIMER_DONE, isCfEnable(i) ? 1 : 0, 0, new Cf(dialingNumber, i2 == 0, message));
        try {
            ImsUt imsUt = this.mCT.getUtInterface();
            if (imsUt instanceof ImsUt) {
                HwImsUtManager.updateCallForwardUncondTimer(i3, i4, i5, i6, getActionFromCFAction(i), getConditionFromCFReason(i2), dialingNumber, resp, this.mCT.mPhone.getPhoneId(), imsUt);
            }
        } catch (ImsException e) {
            sendErrorResponse(message, e);
        }
    }

    public Message popUtMessage(int id) {
        Message msg;
        try {
            if (this.mCT == null) {
                return null;
            }
            ImsUt utInterface = this.mCT.getUtInterface();
            if (utInterface instanceof ImsUt) {
                ImsUt imsUt = utInterface;
                Integer key = Integer.valueOf(id);
                synchronized (imsUt.mLockObj) {
                    msg = (Message) imsUt.mPendingCmds.get(key);
                    imsUt.mPendingCmds.remove(key);
                }
                return msg;
            }
            return null;
        } catch (ImsException e) {
            Rlog.e(LOG_TAG, "ImsException e=" + e);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendErrorResponse(Message onComplete, Throwable e) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, getCommandException(e));
            onComplete.sendToTarget();
        }
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
            case 0:
            case 1:
            case 3:
            case 4:
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

    /* access modifiers changed from: package-private */
    public void sendErrorResponse(Message onComplete) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    /* access modifiers changed from: package-private */
    public CommandException getCommandException(int code) {
        Rlog.d(LOG_TAG, "getCommandException code=" + code);
        CommandException.Error error = CommandException.Error.GENERIC_FAILURE;
        if (code == 801) {
            error = CommandException.Error.REQUEST_NOT_SUPPORTED;
        } else if (code == 821) {
            error = CommandException.Error.PASSWORD_INCORRECT;
        } else if (code == 831) {
            error = CommandException.Error.UT_NO_CONNECTION;
        }
        return new CommandException(error);
    }

    /* access modifiers changed from: package-private */
    public CommandException getCommandException(Throwable e) {
        if (e instanceof ImsException) {
            return getCommandException(((ImsException) e).getCode());
        }
        if (e instanceof CommandException) {
            Rlog.d(LOG_TAG, "e instanceof CommandException  : " + e);
            return new CommandException(((CommandException) e).getCommandError());
        }
        Rlog.d(LOG_TAG, "getCommandException generic failure");
        return new CommandException(CommandException.Error.GENERIC_FAILURE);
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
            case 0:
                return 0;
            case 1:
                return 1;
            case 3:
                return 3;
            case 4:
                return 4;
            default:
                return -1;
        }
    }

    private int getConditionFromCFReason(int reason) {
        switch (reason) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
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
}
