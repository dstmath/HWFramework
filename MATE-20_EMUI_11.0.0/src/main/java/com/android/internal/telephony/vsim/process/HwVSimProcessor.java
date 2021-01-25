package com.android.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;

public abstract class HwVSimProcessor {
    static final boolean HANDLED = true;
    static final boolean NOT_HANDLED = false;
    protected HwVSimModemAdapter mModemAdapter;
    protected HwVSimRequest mRequest;

    public abstract void doProcessException(AsyncResultEx asyncResultEx, HwVSimRequest hwVSimRequest);

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    public abstract Message obtainMessage(int i, Object obj);

    public abstract void onEnter();

    public abstract void onExit();

    public abstract boolean processMessage(Message message);

    public abstract void transitionToState(int i);

    public HwVSimProcessor(HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        this.mModemAdapter = modemAdapter;
        this.mRequest = request;
    }

    public void notifyResult(HwVSimRequest request, Object result) {
        if (request != null) {
            request.setResult(result);
            request.doNotify();
        }
    }

    public void doEnableProcessException(AsyncResultEx ar, HwVSimRequest request, Object cause) {
        logd("doEnableProcessException : " + cause);
        if (!(ar == null || ar.getException() == null)) {
            logd("error, exception " + ar.getException());
        }
        this.mModemAdapter.closeChipSession(2);
        notifyResult(request, cause);
        transitionToState(0);
    }

    public void doDisableProcessException(AsyncResultEx ar, HwVSimRequest request) {
        if (!(ar == null || ar.getException() == null)) {
            logd("error, exception " + ar.getException());
        }
        notifyResult(request, false);
        transitionToState(0);
    }

    public void doReconnectProcessException(AsyncResultEx ar, HwVSimRequest request) {
        transitionToState(0);
    }

    public void doSwitchModeProcessException(AsyncResultEx ar, HwVSimRequest request) {
        if (!(ar == null || ar.getException() == null)) {
            logd("error, exception " + ar.getException());
        }
        notifyResult(request, false);
        transitionToState(0);
    }

    public boolean isAsyncResultValid(AsyncResultEx ar) {
        if (ar == null) {
            doProcessException(null, null);
            return false;
        }
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (request == null) {
            return false;
        }
        if (ar.getException() == null) {
            return HANDLED;
        }
        doProcessException(ar, request);
        return false;
    }

    public boolean isAsyncResultValid(AsyncResultEx ar, Object cause) {
        if (ar == null) {
            doEnableProcessException(null, null, cause);
            return false;
        }
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (request == null) {
            return false;
        }
        if (ar.getException() == null) {
            return HANDLED;
        }
        doEnableProcessException(ar, request, cause);
        return false;
    }

    public boolean isAsyncResultValidForRequestNotSupport(AsyncResultEx ar) {
        HwVSimRequest request;
        if (ar == null || (request = (HwVSimRequest) ar.getUserObj()) == null) {
            return false;
        }
        boolean noError = HANDLED;
        if (ar.getException() != null) {
            logd("error, exception " + ar.getException());
            if (!isRequestNotSupport(ar.getException())) {
                noError = false;
            }
        }
        if (noError) {
            return HANDLED;
        }
        doProcessException(ar, request);
        return false;
    }

    public boolean isAsyncResultValidNoProcessException(AsyncResultEx ar) {
        if (ar == null) {
            return false;
        }
        if (ar.getException() == null) {
            return HANDLED;
        }
        logd("error, exception " + ar.getException());
        return HANDLED;
    }

    public void unhandledMessage(Message msg) {
        logd(" - unhandledMessage: msg.what=" + msg.what);
    }

    public void setIsVSimOn(boolean isVSimOn) {
        HwVSimController.getInstance().setIsVSimOn(isVSimOn);
    }

    public void setProcessAction(HwVSimController.ProcessAction action) {
        HwVSimController.getInstance().setProcessAction(action);
    }

    public void setProcessType(HwVSimController.ProcessType type) {
        HwVSimController.getInstance().setProcessType(type);
    }

    public void setProcessState(HwVSimController.ProcessState state) {
        HwVSimController.getInstance().setProcessState(state);
    }

    public boolean isSwapProcess() {
        return HwVSimController.getInstance().isSwapProcess();
    }

    public boolean isCrossProcess() {
        return HwVSimController.getInstance().isCrossProcess();
    }

    public boolean isDirectProcess() {
        return HwVSimController.getInstance().isDirectProcess();
    }

    public boolean isEnableProcess() {
        return HwVSimController.getInstance().isEnableProcess();
    }

    public boolean isDisableProcess() {
        return HwVSimController.getInstance().isDisableProcess();
    }

    public boolean isReconnectProcess() {
        return HwVSimController.getInstance().isReconnectProcess();
    }

    public boolean isSwitchModeProcess() {
        return HwVSimController.getInstance().isSwitchModeProcess();
    }

    public boolean isWorkProcess() {
        return HwVSimController.getInstance().isWorkProcess();
    }

    public boolean isReadyProcess() {
        return HwVSimController.getInstance().isReadyProcess();
    }

    public boolean isRequestNotSupport(Throwable ex) {
        return HwVSimUtilsInner.isRequestNotSupport(ex);
    }

    public boolean isAllMarkClear(HwVSimRequest request) {
        if (request == null) {
            return false;
        }
        return request.isAllMarkClear();
    }

    /* access modifiers changed from: protected */
    public boolean isNeedWaitNvCfgMatchAndRestartRild() {
        if (!isCrossProcess() || !HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() || HwVSimController.getInstance().getInsertedCardCount() == 0) {
            return false;
        }
        return HANDLED;
    }

    /* access modifiers changed from: protected */
    public boolean isMessageShouldDeal(Message msg, int current) {
        if (msg != null) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null || ar.getUserObj() == null || !(ar.getUserObj() instanceof HwVSimRequest)) {
                return HANDLED;
            }
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            if (request.getSource() != 0 && current != request.getSource()) {
                logd("request source is " + request.getSource() + ", ignore it.");
                return false;
            } else if (request.getSource() == 0) {
                return HANDLED;
            } else {
                logd("request source is " + request.getSource() + ", pass.");
                return HANDLED;
            }
        } else {
            logd("msg is null, ignore this event.");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void closeChipSessionWhenOpenFailOrTimeout(AsyncResultEx ar) {
        logd("closeChipSessionWhenOpenFailOrTimeout, ar = " + ar);
        doEnableProcessException(ar, this.mRequest, 11);
    }
}
