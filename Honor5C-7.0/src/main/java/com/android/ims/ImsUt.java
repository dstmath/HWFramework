package com.android.ims;

import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.IImsUtListener.Stub;
import java.util.HashMap;
import java.util.Map.Entry;

public class ImsUt extends AbstractImsUt implements ImsUtInterface {
    public static final String CATEGORY_CB = "CB";
    public static final String CATEGORY_CDIV = "CDIV";
    public static final String CATEGORY_CONF = "CONF";
    public static final String CATEGORY_CW = "CW";
    public static final String CATEGORY_OIP = "OIP";
    public static final String CATEGORY_OIR = "OIR";
    public static final String CATEGORY_TIP = "TIP";
    public static final String CATEGORY_TIR = "TIR";
    private static final boolean DBG = true;
    public static final String KEY_ACTION = "action";
    public static final String KEY_CATEGORY = "category";
    private static final String TAG = "ImsUt";
    public Object mLockObj;
    public HashMap<Integer, Message> mPendingCmds;
    private final IImsUt miUt;

    private class IImsUtListenerProxy extends Stub {
        private IImsUtListenerProxy() {
        }

        public void utConfigurationUpdated(IImsUt ut, int id) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport((Message) ImsUt.this.mPendingCmds.get(key));
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationUpdateFailed(IImsUt ut, int id, ImsReasonInfo error) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendFailureReport((Message) ImsUt.this.mPendingCmds.get(key), error);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationQueried(IImsUt ut, int id, Bundle ssInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport((Message) ImsUt.this.mPendingCmds.get(key), ssInfo);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationQueryFailed(IImsUt ut, int id, ImsReasonInfo error) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendFailureReport((Message) ImsUt.this.mPendingCmds.get(key), error);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationCallBarringQueried(IImsUt ut, int id, ImsSsInfo[] cbInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport((Message) ImsUt.this.mPendingCmds.get(key), cbInfo);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationCallForwardQueried(IImsUt ut, int id, ImsCallForwardInfo[] cfInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport((Message) ImsUt.this.mPendingCmds.get(key), cfInfo);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationCallWaitingQueried(IImsUt ut, int id, ImsSsInfo[] cwInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport((Message) ImsUt.this.mPendingCmds.get(key), cwInfo);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }
    }

    public ImsUt(IImsUt iUt) {
        this.mLockObj = new Object();
        this.mPendingCmds = new HashMap();
        this.miUt = iUt;
        if (this.miUt != null) {
            try {
                this.miUt.setListener(new IImsUtListenerProxy());
            } catch (RemoteException e) {
            }
        }
    }

    public void close() {
        synchronized (this.mLockObj) {
            if (this.miUt != null) {
                try {
                    this.miUt.close();
                } catch (RemoteException e) {
                }
            }
            if (!this.mPendingCmds.isEmpty()) {
                for (Entry<Integer, Message> entry : (Entry[]) this.mPendingCmds.entrySet().toArray(new Entry[this.mPendingCmds.size()])) {
                    sendFailureReport((Message) entry.getValue(), new ImsReasonInfo(802, 0));
                }
                this.mPendingCmds.clear();
            }
        }
    }

    public void queryCallBarring(int cbType, Message result) {
        log("queryCallBarring :: Ut=" + this.miUt + ", cbType=" + cbType);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCallBarring(cbType);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void queryCallForward(int condition, String number, Message result) {
        log("queryCallForward :: Ut=" + this.miUt + ", condition=" + condition);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCallForward(condition, number);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void queryCallWaiting(Message result) {
        log("queryCallWaiting :: Ut=" + this.miUt);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCallWaiting();
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void queryCLIR(Message result) {
        log("queryCLIR :: Ut=" + this.miUt);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCLIR();
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void queryCLIP(Message result) {
        log("queryCLIP :: Ut=" + this.miUt);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCLIP();
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void queryCOLR(Message result) {
        log("queryCOLR :: Ut=" + this.miUt);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCOLR();
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void queryCOLP(Message result) {
        log("queryCOLP :: Ut=" + this.miUt);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCOLP();
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCallBarring(int cbType, int action, Message result, String[] barrList) {
        if (barrList != null) {
            String bList = new String();
            for (String str : barrList) {
                bList.concat(str + " ");
            }
            log("updateCallBarring :: Ut=" + this.miUt + ", cbType=" + cbType + ", action=" + action + ", barrList=" + bList);
        } else {
            log("updateCallBarring :: Ut=" + this.miUt + ", cbType=" + cbType + ", action=" + action);
        }
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.updateCallBarring(cbType, action, barrList);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCallForward(int action, int condition, String number, int serviceClass, int timeSeconds, Message result) {
        log("updateCallForward :: Ut=" + this.miUt + ", action=" + action + ", condition=" + condition + ", serviceClass=" + serviceClass + ", timeSeconds=" + timeSeconds);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.updateCallForward(action, condition, number, serviceClass, timeSeconds);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCallWaiting(boolean enable, int serviceClass, Message result) {
        log("updateCallWaiting :: Ut=" + this.miUt + ", enable=" + enable + ",serviceClass=" + serviceClass);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.updateCallWaiting(enable, serviceClass);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCLIR(int clirMode, Message result) {
        log("updateCLIR :: Ut=" + this.miUt + ", clirMode=" + clirMode);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.updateCLIR(clirMode);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCLIP(boolean enable, Message result) {
        log("updateCLIP :: Ut=" + this.miUt + ", enable=" + enable);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.updateCLIP(enable);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCOLR(int presentation, Message result) {
        log("updateCOLR :: Ut=" + this.miUt + ", presentation=" + presentation);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.updateCOLR(presentation);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCOLP(boolean enable, Message result) {
        log("updateCallWaiting :: Ut=" + this.miUt + ", enable=" + enable);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.updateCOLP(enable);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void transact(Bundle ssInfo, Message result) {
        log("transact :: Ut=" + this.miUt + ", ssInfo=" + ssInfo);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.transact(ssInfo);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                    return;
                }
                this.mPendingCmds.put(Integer.valueOf(id), result);
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void sendFailureReport(Message result, ImsReasonInfo error) {
        if (result != null && error != null) {
            String errorString;
            if (error.mExtraMessage == null) {
                errorString = new String("IMS UT exception");
            } else {
                errorString = new String(error.mExtraMessage);
            }
            AsyncResult.forMessage(result, null, new ImsException(errorString, error.mCode));
            result.sendToTarget();
        }
    }

    private void sendSuccessReport(Message result) {
        if (result != null) {
            AsyncResult.forMessage(result, null, null);
            result.sendToTarget();
        }
    }

    private void sendSuccessReport(Message result, Object ssInfo) {
        if (result != null) {
            AsyncResult.forMessage(result, ssInfo, null);
            result.sendToTarget();
        }
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    private void loge(String s) {
        Rlog.e(TAG, s);
    }

    private void loge(String s, Throwable t) {
        Rlog.e(TAG, s, t);
    }
}
