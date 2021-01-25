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
    ImsPhoneCallTracker mCallTracker;
    private boolean mIsBusy = false;
    boolean mIsImsEvent;

    public HwImsPhoneEx(ImsPhoneCallTracker ct) {
        this.mCallTracker = ct;
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        this.mIsImsEvent = true;
        switch (msg.what) {
            case EVENT_SET_CLIR_DONE /* 101 */:
                sendResponse((Message) ar.userObj, null, ar.exception);
                break;
            case EVENT_GET_CLIR_DONE /* 102 */:
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
            case EVENT_SET_CALL_FORWARD_TIMER_DONE /* 103 */:
                Cf cft = (Cf) ar.userObj;
                if (cft != null) {
                    sendResponse(cft.mOnComplete, null, ar.exception);
                    break;
                }
                break;
            default:
                this.mIsImsEvent = false;
                break;
        }
        RlogEx.i(LOG_TAG, "handleMessage what=" + msg.what + ", mIsImsEvent = " + this.mIsImsEvent);
    }

    public boolean isSupportCFT() {
        RlogEx.i(LOG_TAG, "isSupportCFT");
        ImsPhoneCallTracker imsPhoneCallTracker = this.mCallTracker;
        if (imsPhoneCallTracker == null || imsPhoneCallTracker.mPhone == null) {
            return false;
        }
        boolean isSupportCft = HwImsUtManager.isSupportCFT(this.mCallTracker.mPhone.getPhoneId());
        RlogEx.i(LOG_TAG, "isSupportCft is " + isSupportCft);
        return isSupportCft;
    }

    public boolean isUtEnable() {
        ImsPhoneCallTracker imsPhoneCallTracker = this.mCallTracker;
        if (imsPhoneCallTracker == null || imsPhoneCallTracker.mPhone == null) {
            return false;
        }
        if (this.mCallTracker.getPhoneType() == 6 && !this.mCallTracker.isVolteEnabledByPlatform()) {
            return false;
        }
        if (HwImsUtManager.isUtEnable(this.mCallTracker.mPhone.getPhoneId()) || this.mCallTracker.isUtEnabledForQcom()) {
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

    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCfAction, int commandInterfaceCfReason, String dialingNumber, Message onComplete) {
        ImsPhoneCallTracker imsPhoneCallTracker;
        ImsUtInterface ut;
        RlogEx.i(LOG_TAG, "setCallForwardingUncondTimerOption action=" + commandInterfaceCfAction + ", reason=" + commandInterfaceCfReason + ", startHour=" + startHour + ", startMinute=" + startMinute + ", endHour=" + endHour + ", endMinute=" + endMinute);
        if (!isValidCommandInterfaceCfAction(commandInterfaceCfAction) || !isValidCommandInterfaceCfReason(commandInterfaceCfReason) || (imsPhoneCallTracker = this.mCallTracker) == null || imsPhoneCallTracker.mPhone == null) {
            sendErrorResponse(onComplete);
            return;
        }
        Message resp = obtainMessage(EVENT_SET_CALL_FORWARD_TIMER_DONE, isCfEnable(commandInterfaceCfAction) ? 1 : 0, 0, new Cf(dialingNumber, commandInterfaceCfReason == 0, onComplete));
        try {
            ImsUtInterface ut2 = this.mCallTracker.getUtInterface();
            if (ut2 instanceof ImsUt) {
                try {
                    HwImsUtManager.updateCallForwardUncondTimer(startHour, startMinute, endHour, endMinute, getActionFromCfAction(commandInterfaceCfAction), getConditionFromCfReason(commandInterfaceCfReason), dialingNumber, resp, this.mCallTracker.mPhone.getPhoneId(), (ImsUt) ut2);
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
        ImsPhoneCallTracker imsPhoneCallTracker = this.mCallTracker;
        if (imsPhoneCallTracker == null) {
            return null;
        }
        try {
            ImsUtInterface ut = imsPhoneCallTracker.getUtInterface();
            if (ut instanceof ImsUt) {
                ImsUt imsUt = (ImsUt) ut;
                Integer key = Integer.valueOf(id);
                synchronized (imsUt.mLockObj) {
                    msg = (Message) imsUt.mPendingCmds.get(key);
                    imsUt.mPendingCmds.remove(key);
                }
                return msg;
            }
        } catch (ImsException e) {
            RlogEx.e(LOG_TAG, "ImsException : popUtMessage");
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void sendErrorResponse(Message onComplete, Throwable e) {
        RlogEx.i(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, getCommandException(e));
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

    private boolean isValidCommandInterfaceCfReason(int commandInterfaceCfReason) {
        if (commandInterfaceCfReason == 0 || commandInterfaceCfReason == 1 || commandInterfaceCfReason == 2 || commandInterfaceCfReason == 3 || commandInterfaceCfReason == 4 || commandInterfaceCfReason == 5) {
            return true;
        }
        return false;
    }

    private boolean isValidCommandInterfaceCfAction(int commandInterfaceCfAction) {
        if (commandInterfaceCfAction == 0 || commandInterfaceCfAction == 1 || commandInterfaceCfAction == 3 || commandInterfaceCfAction == 4) {
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
        return this.mIsImsEvent;
    }

    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    private int getActionFromCfAction(int action) {
        if (action == 0) {
            return 0;
        }
        if (action == 1) {
            return 1;
        }
        if (action != 3) {
            return action != 4 ? -1 : 4;
        }
        return 3;
    }

    private int getConditionFromCfReason(int reason) {
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
        this.mIsBusy = isImsPhoneBusy;
    }

    public boolean isBusy() {
        return this.mIsBusy;
    }
}
