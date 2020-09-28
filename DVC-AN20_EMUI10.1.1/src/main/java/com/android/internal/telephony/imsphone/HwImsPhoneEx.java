package com.android.internal.telephony.imsphone;

import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.ims.HwImsUtManager;
import com.android.ims.ImsException;
import com.android.ims.ImsUt;
import com.android.ims.ImsUtInterface;
import com.android.internal.telephony.CommandException;
import com.huawei.android.telephony.RlogEx;

public class HwImsPhoneEx extends Handler implements IHwImsPhoneEx {
    private static final int EVENT_GET_CLIR_DONE = 102;
    private static final int EVENT_HW_IMS = 100;
    private static final int EVENT_SET_CALL_FORWARD_TIMER_DONE = 103;
    private static final int EVENT_SET_CLIR_DONE = 101;
    private static final String LOG_TAG = "HwImsPhoneEx";
    private boolean isBusy = false;
    boolean isImsEvent;
    ImsPhoneCallTracker mCT;

    public HwImsPhoneEx(ImsPhoneCallTracker ct) {
        this.mCT = ct;
    }

    public void handleMessage(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        this.isImsEvent = true;
        switch (msg.what) {
            case EVENT_SET_CLIR_DONE /*{ENCODED_INT: 101}*/:
                sendResponse((Message) ar.userObj, null, ar.exception);
                break;
            case EVENT_GET_CLIR_DONE /*{ENCODED_INT: 102}*/:
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
            case EVENT_SET_CALL_FORWARD_TIMER_DONE /*{ENCODED_INT: 103}*/:
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
        RlogEx.i(LOG_TAG, "handleMessage what=" + msg.what + ", isImsEvent = " + this.isImsEvent);
    }

    public boolean isSupportCFT() {
        RlogEx.i(LOG_TAG, "isSupportCFT");
        ImsPhoneCallTracker imsPhoneCallTracker = this.mCT;
        if (imsPhoneCallTracker == null || imsPhoneCallTracker.mPhone == null) {
            return false;
        }
        boolean isSupportCFT = HwImsUtManager.isSupportCFT(this.mCT.mPhone.getPhoneId());
        RlogEx.i(LOG_TAG, "isSupportCFT is " + isSupportCFT);
        return isSupportCFT;
    }

    public boolean isUtEnable() {
        ImsPhoneCallTracker imsPhoneCallTracker = this.mCT;
        if (imsPhoneCallTracker == null || imsPhoneCallTracker.mPhone == null) {
            return false;
        }
        if (this.mCT.getPhoneType() == 6 && !this.mCT.isVolteEnabledByPlatform()) {
            return false;
        }
        if (HwImsUtManager.isUtEnable(this.mCT.mPhone.getPhoneId()) || this.mCT.isUtEnabledForQcom()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static class Cf {
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

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        ImsPhoneCallTracker imsPhoneCallTracker;
        RlogEx.i(LOG_TAG, "setCallForwardingUncondTimerOption action=" + commandInterfaceCFAction + ", reason=" + commandInterfaceCFReason + ", startHour=" + startHour + ", startMinute=" + startMinute + ", endHour=" + endHour + ", endMinute=" + endMinute);
        if (!isValidCommandInterfaceCFAction(commandInterfaceCFAction) || !isValidCommandInterfaceCFReason(commandInterfaceCFReason) || (imsPhoneCallTracker = this.mCT) == null || imsPhoneCallTracker.mPhone == null) {
            sendErrorResponse(onComplete);
            return;
        }
        Message resp = obtainMessage(EVENT_SET_CALL_FORWARD_TIMER_DONE, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cf(dialingNumber, commandInterfaceCFReason == 0, onComplete));
        try {
            ImsUtInterface ut = this.mCT.getUtInterface();
            if (ut instanceof ImsUt) {
                try {
                    HwImsUtManager.updateCallForwardUncondTimer(startHour, startMinute, endHour, endMinute, getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber, resp, this.mCT.mPhone.getPhoneId(), (ImsUt) ut);
                } catch (ImsException e) {
                    ut = e;
                }
            }
        } catch (ImsException e2) {
            ut = e2;
            sendErrorResponse(onComplete, ut);
        }
    }

    public Message popUtMessage(int id) {
        Message msg;
        try {
            if (this.mCT == null) {
                return null;
            }
            ImsUtInterface ut = this.mCT.getUtInterface();
            if (ut instanceof ImsUt) {
                ImsUt imsUt = (ImsUt) ut;
                Integer key = Integer.valueOf(id);
                synchronized (imsUt.mLockObj) {
                    msg = (Message) imsUt.mPendingCmds.get(key);
                    imsUt.mPendingCmds.remove(key);
                }
                return msg;
            }
            return null;
        } catch (ImsException e) {
            RlogEx.e(LOG_TAG, "ImsException : popUtMessage");
        }
    }

    /* access modifiers changed from: package-private */
    public void sendErrorResponse(Message onComplete, Throwable e) {
        RlogEx.i(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, getCommandException(e));
            onComplete.sendToTarget();
        }
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        if (commandInterfaceCFReason == 0 || commandInterfaceCFReason == 1 || commandInterfaceCFReason == 2 || commandInterfaceCFReason == 3 || commandInterfaceCFReason == 4 || commandInterfaceCFReason == 5) {
            return true;
        }
        return false;
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        if (commandInterfaceCFAction == 0 || commandInterfaceCFAction == 1 || commandInterfaceCFAction == 3 || commandInterfaceCFAction == 4) {
            return true;
        }
        return false;
    }

    private void sendResponse(Message onComplete, Object result, Throwable e) {
        if (onComplete != null) {
            if (e != null) {
                AsyncResult.forMessage(onComplete, result, getCommandException(e));
            } else {
                AsyncResult.forMessage(onComplete, result, (Throwable) null);
            }
            onComplete.sendToTarget();
        }
    }

    /* access modifiers changed from: package-private */
    public void sendErrorResponse(Message onComplete) {
        RlogEx.i(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    /* access modifiers changed from: package-private */
    public CommandException getCommandException(int code) {
        RlogEx.i(LOG_TAG, "getCommandException code=" + code);
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
            RlogEx.i(LOG_TAG, "e instanceof CommandException ");
            return new CommandException(((CommandException) e).getCommandError());
        }
        RlogEx.i(LOG_TAG, "getCommandException generic failure");
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
        if (action == 0) {
            return 0;
        }
        if (action == 1) {
            return 1;
        }
        if (action == 3) {
            return 3;
        }
        if (action != 4) {
            return -1;
        }
        return 4;
    }

    private int getConditionFromCFReason(int reason) {
        if (reason == 0) {
            return 0;
        }
        if (reason == 1) {
            return 1;
        }
        if (reason == 2) {
            return 2;
        }
        if (reason == 3) {
            return 3;
        }
        if (reason != 4) {
            return reason != 5 ? -1 : 5;
        }
        return 4;
    }

    public void setIsBusy(boolean isImsPhoneBusy) {
        this.isBusy = isImsPhoneBusy;
    }

    public boolean isBusy() {
        return this.isBusy;
    }
}
