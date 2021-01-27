package com.android.internal.telephony.vsim.process;

import android.os.Message;
import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;

public abstract class HwVSimProcessor {
    protected static final boolean HANDLED = true;
    protected static final boolean NOT_HANDLED = false;
    private static final String TAG = "HwVSimProcessor";
    protected HwVSimModemAdapter mModemAdapter;
    protected HwVSimRequest mRequest;

    public abstract void doProcessException(AsyncResultEx asyncResultEx, HwVSimRequest hwVSimRequest);

    public abstract boolean isCrossProcess();

    public abstract boolean isDirectProcess();

    public abstract boolean isDisableProcess();

    public abstract boolean isEnableProcess();

    public abstract boolean isReadyProcess();

    public abstract boolean isSwapProcess();

    public abstract boolean isSwitchModeProcess();

    public abstract boolean isWorkProcess();

    public abstract Message obtainMessage(int i, Object obj);

    public abstract void onEnter();

    public abstract void onExit();

    public abstract boolean processMessage(Message message);

    public abstract void setProcessType(HwVSimConstants.ProcessType processType);

    public abstract void transitionToState(int i);

    public HwVSimProcessor(HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        this.mModemAdapter = modemAdapter;
        this.mRequest = request;
    }

    /* access modifiers changed from: protected */
    public void logd(String content) {
        HwVSimLog.debug(TAG, content);
    }

    /* access modifiers changed from: protected */
    public void loge(String content) {
        HwVSimLog.error(TAG, content);
    }

    public void notifyResult(HwVSimRequest request, Object result) {
        if (request != null) {
            request.setResult(result);
            request.doNotify();
        }
    }

    public void doEnableProcessException(AsyncResultEx ar, HwVSimRequest request, Object cause) {
        loge("doEnableProcessException : " + cause + ", request: " + request);
        if (!(ar == null || ar.getException() == null)) {
            loge("error, exception " + ar.getException());
        }
        this.mModemAdapter.closeChipSession(2);
        notifyResult(request, cause);
        transitionToState(0);
    }

    public void doDisableProcessException(AsyncResultEx ar, HwVSimRequest request) {
        if (!(ar == null || ar.getException() == null)) {
            loge("error, exception " + ar.getException());
        }
        notifyResult(request, false);
        transitionToState(0);
    }

    public void doReconnectProcessException(AsyncResultEx ar, HwVSimRequest request) {
        transitionToState(0);
    }

    public void doSwitchModeProcessException(AsyncResultEx ar, HwVSimRequest request) {
        if (!(ar == null || ar.getException() == null)) {
            loge("error, exception " + ar.getException());
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
            doProcessException(ar, null);
            return false;
        } else if (ar.getException() == null) {
            return HANDLED;
        } else {
            doProcessException(ar, request);
            return false;
        }
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
            loge("error, exception " + ar.getException());
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
        loge("error, exception " + ar.getException());
        return HANDLED;
    }

    public void unhandledMessage(Message msg) {
        logd(" - unhandledMessage: msg.what=" + msg.what);
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

    /* access modifiers changed from: protected */
    public int calcNetworkModeByAcqorder(String acqorder) {
        if (HwTelephonyManager.getDefault().isNrSupported() && acqorder != null && acqorder.startsWith("08")) {
            return 65;
        }
        if ("0201".equals(acqorder)) {
            return 3;
        }
        if ("01".equals(acqorder)) {
            return 1;
        }
        loge("calcNetworkModeByAcqorder, default is 9");
        return 9;
    }
}
