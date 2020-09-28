package com.android.ims;

import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsData;
import android.telephony.ims.ImsSsInfo;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.IImsUtListener;
import java.util.HashMap;
import java.util.Map;

public class ImsUt implements ImsUtInterface {
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
    private static final int SERVICE_CLASS_NONE = 0;
    private static final int SERVICE_CLASS_VOICE = 1;
    private static final String TAG = "ImsUt";
    public Object mLockObj = new Object();
    public HashMap<Integer, Message> mPendingCmds = new HashMap<>();
    private int mPhoneId = 0;
    private Registrant mSsIndicationRegistrant;
    private final IImsUt miUt;

    public ImsUt(IImsUt iUt) {
        this.miUt = iUt;
        IImsUt iImsUt = this.miUt;
        if (iImsUt != null) {
            try {
                iImsUt.setListener(new IImsUtListenerProxy());
            } catch (RemoteException e) {
            }
        }
    }

    public ImsUt(IImsUt iUt, int phoneId) {
        log("ImsUt :: iUt =" + iUt + ", phoneId=" + phoneId);
        this.mPhoneId = phoneId;
        this.miUt = iUt;
        IImsUt iImsUt = this.miUt;
        if (iImsUt != null) {
            try {
                iImsUt.setListener(new IImsUtListenerProxy());
            } catch (RemoteException e) {
                loge("miUt setListener failed");
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
                for (Map.Entry<Integer, Message> entry : (Map.Entry[]) this.mPendingCmds.entrySet().toArray(new Map.Entry[this.mPendingCmds.size()])) {
                    sendFailureReport(entry.getValue(), new ImsReasonInfo(802, 0));
                }
                this.mPendingCmds.clear();
            }
        }
    }

    public void registerForSuppServiceIndication(Handler h, int what, Object obj) {
        this.mSsIndicationRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForSuppServiceIndication(Handler h) {
        this.mSsIndicationRegistrant.clear();
    }

    public void queryCallBarring(int cbType, Message result) {
        queryCallBarring(cbType, result, 0);
    }

    public void queryCallBarring(int cbType, Message result, int serviceClass) {
        log("queryCallBarring :: Ut=" + this.miUt + ", cbType=" + cbType + ", serviceClass=" + serviceClass);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCallBarringForServiceClass(cbType, serviceClass);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void queryCallForward(int condition, String number, Message result) {
        log("queryCallForward :: Ut=" + this.miUt + ", condition=" + condition + ", number=" + Rlog.pii(TAG, number));
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.queryCallForward(condition, number);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCallBarring(int cbType, int action, Message result, String[] barrList) {
        updateCallBarring(cbType, action, result, barrList, 0);
    }

    public void updateCallBarring(int cbType, int action, Message result, String[] barrList, int serviceClass) {
        if (barrList != null) {
            String bList = new String();
            for (int i = 0; i < barrList.length; i++) {
                bList.concat(barrList[i] + " ");
            }
            log("updateCallBarring :: Ut=" + this.miUt + ", cbType=" + cbType + ", action=" + action + ", serviceClass=" + serviceClass + ", barrList=" + bList);
        } else {
            log("updateCallBarring :: Ut=" + this.miUt + ", cbType=" + cbType + ", action=" + action + ", serviceClass=" + serviceClass);
        }
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.updateCallBarringForServiceClass(cbType, action, barrList, serviceClass);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
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
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public boolean isBinderAlive() {
        return this.miUt.asBinder().isBinderAlive();
    }

    public void transact(Bundle ssInfo, Message result) {
        log("transact :: Ut=" + this.miUt + ", ssInfo=" + ssInfo);
        synchronized (this.mLockObj) {
            try {
                int id = this.miUt.transact(ssInfo);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void sendFailureReport(Message result, ImsReasonInfo error) {
        String errorString;
        if (result != null && error != null) {
            if (error.mExtraMessage == null) {
                errorString = Resources.getSystem().getString(17040617);
            } else {
                errorString = new String(error.mExtraMessage);
            }
            AsyncResult.forMessage(result, (Object) null, new ImsException(errorString, error.mCode));
            result.sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSuccessReport(Message result) {
        if (result != null) {
            AsyncResult.forMessage(result, (Object) null, (Throwable) null);
            result.sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSuccessReport(Message result, Object ssInfo) {
        if (result != null) {
            AsyncResult.forMessage(result, ssInfo, (Throwable) null);
            result.sendToTarget();
        }
    }

    private void log(String s) {
        Rlog.d("ImsUt[" + this.mPhoneId + "]", s);
    }

    private void loge(String s) {
        Rlog.e("ImsUt[" + this.mPhoneId + "]", s);
    }

    private void loge(String s, Throwable t) {
        Rlog.e("ImsUt[" + this.mPhoneId + "]", s, t);
    }

    private class IImsUtListenerProxy extends IImsUtListener.Stub {
        private IImsUtListenerProxy() {
        }

        public void utConfigurationUpdated(IImsUt ut, int id) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport(ImsUt.this.mPendingCmds.get(key));
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationUpdateFailed(IImsUt ut, int id, ImsReasonInfo error) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendFailureReport(ImsUt.this.mPendingCmds.get(key), error);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationQueried(IImsUt ut, int id, Bundle ssInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport(ImsUt.this.mPendingCmds.get(key), ssInfo);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationQueryFailed(IImsUt ut, int id, ImsReasonInfo error) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendFailureReport(ImsUt.this.mPendingCmds.get(key), error);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationCallBarringQueried(IImsUt ut, int id, ImsSsInfo[] cbInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport(ImsUt.this.mPendingCmds.get(key), cbInfo);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationCallForwardQueried(IImsUt ut, int id, ImsCallForwardInfo[] cfInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport(ImsUt.this.mPendingCmds.get(key), cfInfo);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationCallWaitingQueried(IImsUt ut, int id, ImsSsInfo[] cwInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (ImsUt.this.mLockObj) {
                ImsUt.this.sendSuccessReport(ImsUt.this.mPendingCmds.get(key), cwInfo);
                ImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void onSupplementaryServiceIndication(ImsSsData ssData) {
            if (ImsUt.this.mSsIndicationRegistrant != null) {
                ImsUt.this.mSsIndicationRegistrant.notifyResult(ssData);
            }
        }
    }
}
