package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.uicc.IccRecords;

public abstract class CsgSearch extends Handler {
    protected static final int EVENT_CSG_MANUAL_SCAN_DONE = 2;
    protected static final int EVENT_CSG_MANUAL_SELECT_DONE = 3;
    protected static final int EVENT_CSG_OCSGL_LOADED = 7;
    protected static final int EVENT_CSG_PERIODIC_SCAN_DONE = 5;
    protected static final int EVENT_CSG_PERIODIC_SEARCH_TIMEOUT = 4;
    protected static final int EVENT_CSG_PERIODIC_SELECT_DONE = 6;
    protected static final int EVENT_GET_AVAILABLE_CSG_NETWORK_DONE = 0;
    protected static final int EVENT_SELECT_CSG_NETWORK_DONE = 1;
    private static final String LOG_TAG = "CsgSearch";
    protected static final String OPERATOR_NAME_ATT_MICROCELL = "AT&T MicroCell";
    protected static final int TIMER_CSG_PERIODIC_SEARCH = 7200000;
    public static final boolean isVZW;
    protected static boolean mIsSupportCsgSearch = SystemProperties.getBoolean("ro.config.att.csg", false);
    protected GsmCdmaPhone mPhone;

    /* access modifiers changed from: package-private */
    public abstract void getAvailableCSGNetworks(Message message);

    /* access modifiers changed from: package-private */
    public abstract void handleCsgNetworkQueryResult(AsyncResult asyncResult);

    /* access modifiers changed from: package-private */
    public abstract void selectCSGNetwork(Message message);

    static {
        boolean z = false;
        if ("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb"))) {
            z = true;
        }
        isVZW = z;
    }

    public static boolean isSupportCsgSearch() {
        return mIsSupportCsgSearch;
    }

    public CsgSearch(GsmCdmaPhone phone) {
        this.mPhone = phone;
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 0) {
            Rlog.d(LOG_TAG, "=csg= Receved EVENT_GET_AVAILABLE_CSG_NETWORK_DONE.");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null) {
                Rlog.e(LOG_TAG, "=csg=  ar is null, the code should never come here!!");
            } else {
                handleCsgNetworkQueryResult(ar);
            }
        } else if (i == 2) {
            Rlog.d(LOG_TAG, "=csg= Receved EVENT_CSG_MANUAL_SCAN_DONE.");
            AsyncResult ar2 = (AsyncResult) msg.obj;
            if (ar2 == null) {
                Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
            } else if (ar2.exception != null) {
                Rlog.e(LOG_TAG, "=csg= Manual Search: get avaiable CSG list failed! -> response " + ar2.exception);
                AsyncResult.forMessage((Message) ar2.userObj, (Object) null, ar2.exception);
                ((Message) ar2.userObj).sendToTarget();
            } else {
                Rlog.i(LOG_TAG, "=csg= Manual Search: get avaiable CSG list success -> select Csg! ");
                if (isVZW) {
                    AsyncResult.forMessage((Message) ar2.userObj, ar2.result, (Throwable) null);
                    ((Message) ar2.userObj).sendToTarget();
                    return;
                }
                selectCSGNetwork(obtainMessage(EVENT_CSG_MANUAL_SELECT_DONE, ar2));
            }
        } else if (i == EVENT_CSG_MANUAL_SELECT_DONE) {
            Rlog.d(LOG_TAG, "=csg= EVENT_CSG_MANUAL_SELECT_DONE!");
            AsyncResult ar3 = (AsyncResult) msg.obj;
            if (ar3 == null) {
                Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                return;
            }
            if (ar3.exception != null) {
                Rlog.i(LOG_TAG, "=csg= Manual Search: CSG-ID selection is failed! " + ar3.exception);
            } else {
                Rlog.i(LOG_TAG, "=csg= Manual Search: CSG-ID selection is success! ");
            }
            AsyncResult arUsrObj = (AsyncResult) ar3.userObj;
            if (arUsrObj == null) {
                Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                return;
            }
            AsyncResult.forMessage((Message) arUsrObj.userObj, (Object) null, ar3.exception);
            ((Message) arUsrObj.userObj).sendToTarget();
        } else if (i == 4) {
            Rlog.d(LOG_TAG, "=csg= EVENT_CSG_PERIODIC_SEARCH_TIMEOUT!");
            trigerPeriodicCsgSearch();
            Rlog.d(LOG_TAG, "=csg=  launch next Csg Periodic search timer!");
            judgeToLaunchCsgPeriodicSearchTimer();
        } else if (i == EVENT_CSG_PERIODIC_SELECT_DONE) {
            Rlog.d(LOG_TAG, "=csg= EVENT_CSG_PERIODIC_SELECT_DONE!");
            AsyncResult ar4 = (AsyncResult) msg.obj;
            if (ar4 == null) {
                Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
            } else if (ar4.exception != null) {
                Rlog.e(LOG_TAG, "=csg= Periodic Search: CSG-ID selection is failed! " + ar4.exception);
            } else {
                Rlog.e(LOG_TAG, "=csg= Periodic Search: CSG-ID selection is success! ");
            }
        } else if (i != EVENT_CSG_OCSGL_LOADED) {
            Rlog.e(LOG_TAG, "unexpected event not handled: " + msg.what);
        } else {
            Rlog.d(LOG_TAG, "=csg= EVENT_CSG_OCSGL_LOADED!");
            judgeToLaunchCsgPeriodicSearchTimer();
        }
    }

    public void registerForCsgRecordsLoadedEvent() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r != null) {
            r.registerForCsgRecordsLoaded(this, (int) EVENT_CSG_OCSGL_LOADED, (Object) null);
        }
    }

    public void unregisterForCsgRecordsLoadedEvent() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r != null) {
            r.unregisterForCsgRecordsLoaded(this);
        }
    }

    public boolean isCsgAwareUicc() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r == null || r.getOcsgl().length > 0 || r.getCsglexist()) {
            return true;
        }
        Rlog.d(LOG_TAG, "=csg=  EF-Operator not present =>CSG not Aware UICC");
        return false;
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        if (!CsgSearchFactory.isHisiChipset()) {
            boolean isLaunchTimer = false;
            IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
            ServiceState ss = this.mPhone.getServiceState();
            String operatorAlpha = SystemProperties.get("gsm.operator.alpha", "");
            if (ss != null && ((ss.getVoiceRegState() == 0 || ss.getDataRegState() == 0) && !ss.getRoaming() && operatorAlpha != null && !OPERATOR_NAME_ATT_MICROCELL.equals(operatorAlpha))) {
                isLaunchTimer = true;
            }
            if (isLaunchTimer && r != null) {
                byte[] csgLists = r.getOcsgl();
                if (true == r.getCsglexist() && csgLists.length == 0) {
                    Rlog.d(LOG_TAG, "=csg= EFOCSGL is empty, not trigger periodic search!");
                    isLaunchTimer = false;
                }
            }
            if (isLaunchTimer) {
                launchCsgPeriodicSearchTimer();
            } else {
                cancelCsgPeriodicSearchTimer();
            }
        }
    }

    public void launchCsgPeriodicSearchTimer() {
        if (!hasMessages(4)) {
            Rlog.d(LOG_TAG, "=csg= lauch periodic search timer!");
            sendEmptyMessageDelayed(4, 7200000);
        }
    }

    public void cancelCsgPeriodicSearchTimer() {
        if (hasMessages(4)) {
            Rlog.d(LOG_TAG, "=csg= cancel periodic search timer!");
            removeMessages(4);
        }
    }

    private void trigerPeriodicCsgSearch() {
        getAvailableCSGNetworks(obtainMessage(5));
    }

    public void selectCsgNetworkManually(Message response) {
        Rlog.i(LOG_TAG, "start manual select CSG network...");
        getAvailableCSGNetworks(obtainMessage(2, response));
    }

    public void selectExtendersCSGNetwork(HwHisiCsgNetworkInfo csgNetworkInfo, Message response) {
        Rlog.i(LOG_TAG, "selectExtendersCSGNetwork...");
    }
}
