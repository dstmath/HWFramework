package com.android.internal.telephony.dataconnection;

import android.common.HwFrameworkFactory;
import android.net.INetworkPolicyListener;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkPolicyManager;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.DataFailCause;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.data.DataCallResponse;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DcController extends StateMachine {
    private static final boolean DBG = true;
    private static final boolean VDBG = false;
    private final DataServiceManager mDataServiceManager;
    private final HashMap<Integer, DataConnection> mDcListActiveByCid = new HashMap<>();
    final ArrayList<DataConnection> mDcListAll = new ArrayList<>();
    private final DcTesterDeactivateAll mDcTesterDeactivateAll;
    private DccDefaultState mDccDefaultState;
    private final DcTracker mDct;
    private volatile boolean mExecutingCarrierChange;
    private final INetworkPolicyListener mListener;
    final NetworkPolicyManager mNetworkPolicyManager;
    private final Phone mPhone;
    private PhoneStateListener mPhoneStateListener;
    final TelephonyManager mTelephonyManager;

    private DcController(String name, Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler) {
        super(name, handler);
        DcTesterDeactivateAll dcTesterDeactivateAll = null;
        this.mDccDefaultState = new DccDefaultState();
        this.mListener = new NetworkPolicyManager.Listener() {
            /* class com.android.internal.telephony.dataconnection.DcController.AnonymousClass2 */

            public void onSubscriptionOverride(int subId, int overrideMask, int overrideValue) {
                HashMap<Integer, DataConnection> dcListActiveByCid;
                if (DcController.this.mPhone != null && DcController.this.mPhone.getSubId() == subId) {
                    synchronized (DcController.this.mDcListAll) {
                        dcListActiveByCid = new HashMap<>(DcController.this.mDcListActiveByCid);
                    }
                    for (DataConnection dc : dcListActiveByCid.values()) {
                        dc.onSubscriptionOverride(overrideMask, overrideValue);
                    }
                }
            }
        };
        setLogRecSize(300);
        log("E ctor");
        this.mPhone = phone;
        this.mDct = dct;
        this.mDataServiceManager = dataServiceManager;
        addState(this.mDccDefaultState);
        setInitialState(this.mDccDefaultState);
        log("X ctor");
        this.mPhoneStateListener = new PhoneStateListener(handler.getLooper()) {
            /* class com.android.internal.telephony.dataconnection.DcController.AnonymousClass1 */

            public void onCarrierNetworkChange(boolean active) {
                DcController.this.mExecutingCarrierChange = active;
            }
        };
        this.mTelephonyManager = (TelephonyManager) phone.getContext().getSystemService("phone");
        this.mNetworkPolicyManager = (NetworkPolicyManager) phone.getContext().getSystemService("netpolicy");
        this.mDcTesterDeactivateAll = Build.IS_DEBUGGABLE ? new DcTesterDeactivateAll(this.mPhone, this, getHandler()) : dcTesterDeactivateAll;
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 65536);
        }
    }

    public static DcController makeDcc(Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler, String tagSuffix) {
        return new DcController("Dcc" + tagSuffix, phone, dct, dataServiceManager, handler);
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        log("dispose: call quiteNow()");
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 0);
        }
        quitNow();
    }

    /* access modifiers changed from: package-private */
    public void addDc(DataConnection dc) {
        synchronized (this.mDcListAll) {
            this.mDcListAll.add(dc);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeDc(DataConnection dc) {
        synchronized (this.mDcListAll) {
            this.mDcListActiveByCid.remove(Integer.valueOf(dc.mCid));
            this.mDcListAll.remove(dc);
        }
    }

    public void addActiveDcByCid(DataConnection dc) {
        if (dc.mCid < 0) {
            log("addActiveDcByCid dc.mCid < 0 dc=" + dc);
        }
        synchronized (this.mDcListAll) {
            this.mDcListActiveByCid.put(Integer.valueOf(dc.mCid), dc);
        }
    }

    public DataConnection getActiveDcByCid(int cid) {
        DataConnection dataConnection;
        synchronized (this.mDcListAll) {
            dataConnection = this.mDcListActiveByCid.get(Integer.valueOf(cid));
        }
        return dataConnection;
    }

    /* access modifiers changed from: package-private */
    public void removeActiveDcByCid(DataConnection dc) {
        synchronized (this.mDcListAll) {
            if (this.mDcListActiveByCid.remove(Integer.valueOf(dc.mCid)) == null) {
                log("removeActiveDcByCid removedDc=null dc=" + dc);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isExecutingCarrierChange() {
        return this.mExecutingCarrierChange;
    }

    /* access modifiers changed from: package-private */
    public void getDataCallList() {
        log("DcController:getDataCallList");
        this.mPhone.mCi.getDataCallList(obtainMessage(262151));
    }

    private class DccDefaultState extends State {
        private DccDefaultState() {
        }

        public void enter() {
            if (DcController.this.mPhone != null && DcController.this.mDataServiceManager.getTransportType() == 1) {
                DcController.this.mPhone.mCi.registerForRilConnected(DcController.this.getHandler(), 262149, null);
            }
            DcController.this.mDataServiceManager.registerForDataCallListChanged(DcController.this.getHandler(), 262151);
            if (DcController.this.mNetworkPolicyManager != null) {
                DcController.this.mNetworkPolicyManager.registerListener(DcController.this.mListener);
            }
        }

        public void exit() {
            boolean z = false;
            boolean z2 = DcController.this.mPhone != null;
            if (DcController.this.mDataServiceManager.getTransportType() == 1) {
                z = true;
            }
            if (z2 && z) {
                DcController.this.mPhone.mCi.unregisterForRilConnected(DcController.this.getHandler());
            }
            DcController.this.mDataServiceManager.unregisterForDataCallListChanged(DcController.this.getHandler());
            if (DcController.this.mDcTesterDeactivateAll != null) {
                DcController.this.mDcTesterDeactivateAll.dispose();
            }
            if (DcController.this.mNetworkPolicyManager != null) {
                DcController.this.mNetworkPolicyManager.unregisterListener(DcController.this.mListener);
            }
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 262149) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    DcController dcController = DcController.this;
                    dcController.log("DccDefaultState: msg.what=EVENT_RIL_CONNECTED mRilVersion=" + ar.result);
                    return true;
                }
                DcController.this.log("DccDefaultState: Unexpected exception on EVENT_RIL_CONNECTED");
                return true;
            } else if (i != 262151) {
                return true;
            } else {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null) {
                    onDataStateChanged((ArrayList) ar2.result);
                    return true;
                }
                DcController.this.log("DccDefaultState: EVENT_DATA_STATE_CHANGED: exception; likely radio not available, ignore");
                return true;
            }
        }

        private long getSuggestedRetryDelay(DataCallResponse response) {
            long modemSuggestedRetryTime = (long) response.getSuggestedRetryTime();
            if (modemSuggestedRetryTime < 0) {
                DcController.this.log("No suggested retry delay.");
                return -2;
            } else if (modemSuggestedRetryTime != 2147483647L) {
                return modemSuggestedRetryTime;
            } else {
                DcController.this.log("Modem suggested not retrying.");
                return -1;
            }
        }

        private void saveDcFailPara(DataConnection dc, DataCallResponse dataCallResp) {
            int failCause = DataFailCause.getFailCause(dataCallResp.getCause());
            long delayTime = getSuggestedRetryDelay(dataCallResp);
            for (ApnContext apnContext : dc.mApnContexts.keySet()) {
                apnContext.setFailCause(failCause);
                apnContext.setModemSuggestedDelay(delayTime);
            }
        }

        private void onDataStateChanged(ArrayList<DataCallResponse> dcsList) {
            ArrayList<DataConnection> dcListAll;
            HashMap<Integer, DataConnection> dcListActiveByCid;
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid;
            HashMap<Integer, DataConnection> dcListActiveByCid2;
            ArrayList<DataConnection> dcListAll2;
            LinkProperties.CompareResult<LinkAddress> car;
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid2;
            Iterator<ApnContext> it;
            synchronized (DcController.this.mDcListAll) {
                dcListAll = new ArrayList<>(DcController.this.mDcListAll);
                dcListActiveByCid = new HashMap<>(DcController.this.mDcListActiveByCid);
            }
            DcController.this.lr("onDataStateChanged: dcsList=" + dcsList + " dcListActiveByCid=" + dcListActiveByCid);
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid3 = new HashMap<>();
            Iterator<DataCallResponse> it2 = dcsList.iterator();
            while (it2.hasNext()) {
                DataCallResponse dcs = it2.next();
                dataCallResponseListByCid3.put(Integer.valueOf(dcs.getId()), dcs);
            }
            ArrayList<DataConnection> dcsToRetry = new ArrayList<>();
            for (DataConnection dc : dcListActiveByCid.values()) {
                if (dataCallResponseListByCid3.get(Integer.valueOf(dc.mCid)) == null) {
                    DcController.this.log("onDataStateChanged: add to retry dc=" + dc);
                    dcsToRetry.add(dc);
                }
            }
            DcController.this.log("onDataStateChanged: dcsToRetry=" + dcsToRetry);
            ArrayList<ApnContext> apnsToCleanup = new ArrayList<>();
            boolean isAnyDataCallDormant = false;
            boolean isAnyDataCallActive = false;
            boolean isSupportRcnInd = HwModemCapability.isCapabilitySupport(14);
            Iterator<DataCallResponse> it3 = dcsList.iterator();
            while (it3.hasNext()) {
                DataCallResponse newState = it3.next();
                DataConnection dc2 = dcListActiveByCid.get(Integer.valueOf(newState.getId()));
                if (dc2 != null) {
                    List<ApnContext> apnContexts = dc2.getApnContexts();
                    if (apnContexts.size() == 0) {
                        DcController.this.loge("onDataStateChanged: no connected apns, ignore");
                        dcListAll2 = dcListAll;
                        dataCallResponseListByCid = dataCallResponseListByCid3;
                        dcListActiveByCid2 = dcListActiveByCid;
                    } else {
                        DcController dcController = DcController.this;
                        StringBuilder sb = new StringBuilder();
                        dcListAll2 = dcListAll;
                        sb.append("onDataStateChanged: Found ConnId=");
                        sb.append(newState.getId());
                        sb.append(" newState=");
                        sb.append(newState.toString());
                        dcController.log(sb.toString());
                        if (newState.getLinkStatus() == 0) {
                            Iterator<ApnContext> it4 = dc2.mApnContexts.keySet().iterator();
                            while (it4.hasNext()) {
                                ApnContext apnContext = it4.next();
                                apnContext.setReason(PhoneInternalInterface.REASON_LOST_DATA_CONNECTION);
                                if (HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported()) {
                                    ApnContextEx apnContextEx = new ApnContextEx();
                                    apnContextEx.setApnContext(apnContext);
                                    it = it4;
                                    dataCallResponseListByCid2 = dataCallResponseListByCid3;
                                    HwTelephonyFactory.getHwDataConnectionManager().reportDataFailReason(newState.getCause(), apnContextEx);
                                } else {
                                    it = it4;
                                    dataCallResponseListByCid2 = dataCallResponseListByCid3;
                                }
                                it4 = it;
                                dataCallResponseListByCid3 = dataCallResponseListByCid2;
                            }
                            dataCallResponseListByCid = dataCallResponseListByCid3;
                            if (DcController.this.mDct.isCleanupRequired.get()) {
                                apnsToCleanup.addAll(apnContexts);
                                DcController.this.mDct.isCleanupRequired.set(false);
                                dcListActiveByCid2 = dcListActiveByCid;
                            } else {
                                int failCause = DataFailCause.getFailCause(newState.getCause());
                                if (DataFailCause.isRadioRestartFailure(DcController.this.mPhone.getContext(), failCause, DcController.this.mPhone.getSubId())) {
                                    DcController.this.log("onDataStateChanged: X restart radio, failCause=" + failCause);
                                    DcController.this.mDct.sendRestartRadio();
                                } else if (DcController.this.mDct.isPermanentFailure(failCause)) {
                                    DcController.this.log("onDataStateChanged: inactive, add to cleanup list. failCause=" + failCause);
                                    apnsToCleanup.addAll(apnContexts);
                                } else {
                                    DcController.this.log("onDataStateChanged: inactive, add to retry list. failCause=" + failCause);
                                    if (!DcController.this.mDct.getHwDcTrackerEx().needRetryAfterDisconnected(failCause)) {
                                        DcController.this.log("onDataStateChanged: not needRetryAfterDisconnected !");
                                        for (ApnContext apnContext2 : dc2.mApnContexts.keySet()) {
                                            apnContext2.setReason(AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT);
                                        }
                                        apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                    } else {
                                        if (newState.getSuggestedRetryTime() < 0) {
                                            DcController.this.mDct.getHwDcTrackerEx().checkDataSelfCureAfterDisconnect(newState.getCause());
                                        }
                                        dcsToRetry.add(dc2);
                                        saveDcFailPara(dc2, newState);
                                    }
                                }
                                dcListActiveByCid2 = dcListActiveByCid;
                            }
                        } else {
                            dataCallResponseListByCid = dataCallResponseListByCid3;
                            DataConnection.UpdateLinkPropertyResult result = dc2.updateLinkProperty(newState);
                            if (result.oldLp.equals(result.newLp)) {
                                DcController.this.log("onDataStateChanged: no change");
                                dcListActiveByCid2 = dcListActiveByCid;
                            } else if (result.oldLp.isIdenticalInterfaceName(result.newLp)) {
                                if (!result.oldLp.isIdenticalDnses(result.newLp) || !result.oldLp.isIdenticalRoutes(result.newLp) || !result.oldLp.isIdenticalHttpProxy(result.newLp) || !result.oldLp.isIdenticalAddresses(result.newLp)) {
                                    if (DcController.this.mPhone.getPhoneType() != 2) {
                                        dcListActiveByCid2 = dcListActiveByCid;
                                    } else if (!isSupportRcnInd) {
                                        dcListActiveByCid2 = dcListActiveByCid;
                                    }
                                    LinkProperties.CompareResult<LinkAddress> car2 = result.oldLp.compareAddresses(result.newLp);
                                    DcController.this.log("onDataStateChanged: oldLp=" + result.oldLp + " newLp=" + result.newLp);
                                    boolean needToClean = false;
                                    for (LinkAddress added : car2.added) {
                                        Iterator it5 = car2.removed.iterator();
                                        while (true) {
                                            if (!it5.hasNext()) {
                                                car = car2;
                                                break;
                                            }
                                            car = car2;
                                            if (NetworkUtils.addressTypeMatches(((LinkAddress) it5.next()).getAddress(), added.getAddress())) {
                                                needToClean = true;
                                                break;
                                            } else {
                                                car2 = car;
                                                it5 = it5;
                                            }
                                        }
                                        car2 = car;
                                    }
                                    if (needToClean) {
                                        DcController.this.log("onDataStateChanged: addr change, cleanup apns=" + apnContexts + " oldLp=" + result.oldLp + " newLp=" + result.newLp);
                                        apnsToCleanup.addAll(apnContexts);
                                    } else {
                                        DcController.this.log("onDataStateChanged: simple change");
                                        for (ApnContext apnContext3 : apnContexts) {
                                            DcController.this.mPhone.notifyDataConnection(apnContext3.getApnType());
                                        }
                                    }
                                }
                                DcController dcController2 = DcController.this;
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("onDataStateChanged: no changes, is CDMA Phone:");
                                dcListActiveByCid2 = dcListActiveByCid;
                                sb2.append(DcController.this.mPhone.getPhoneType() == 2);
                                sb2.append(" is Supprot Reconnect ind:");
                                sb2.append(isSupportRcnInd);
                                dcController2.log(sb2.toString());
                            } else {
                                dcListActiveByCid2 = dcListActiveByCid;
                                apnsToCleanup.addAll(apnContexts);
                                DcController.this.log("onDataStateChanged: interface change, cleanup apns=" + apnContexts);
                            }
                        }
                    }
                    if (newState.getLinkStatus() == 2) {
                        isAnyDataCallActive = true;
                    }
                    if (newState.getLinkStatus() == 1) {
                        isAnyDataCallDormant = true;
                    }
                    dcListAll = dcListAll2;
                    dcListActiveByCid = dcListActiveByCid2;
                    dataCallResponseListByCid3 = dataCallResponseListByCid;
                }
            }
            if (!isAnyDataCallDormant || isAnyDataCallActive) {
                DcController.this.log("onDataStateChanged: Data Activity updated to NONE. isAnyDataCallActive = " + isAnyDataCallActive + " isAnyDataCallDormant = " + isAnyDataCallDormant);
                if (isAnyDataCallActive) {
                    DcController.this.mDct.sendStartNetStatPoll(DctConstants.Activity.NONE);
                }
            } else {
                DcController.this.log("onDataStateChanged: Data Activity updated to DORMANT. stopNetStatePoll");
                DcController.this.mDct.sendStopNetStatPoll(DctConstants.Activity.DORMANT);
            }
            DcController.this.lr("onDataStateChanged: dcsToRetry=" + dcsToRetry + " apnsToCleanup=" + apnsToCleanup);
            Iterator<ApnContext> it6 = apnsToCleanup.iterator();
            while (it6.hasNext()) {
                DcController.this.mDct.cleanUpConnection(it6.next());
            }
            Iterator<DataConnection> it7 = dcsToRetry.iterator();
            while (it7.hasNext()) {
                DataConnection dc3 = it7.next();
                DcController.this.log("onDataStateChanged: send EVENT_LOST_CONNECTION dc.mTag=" + dc3.mTag);
                dc3.sendMessage(262153, dc3.mTag);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void lr(String s) {
        logAndAddLogRec(s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.i(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        return DataConnection.cmdToString(what);
    }

    public String toString() {
        String str;
        synchronized (this.mDcListAll) {
            str = "mDcListAll=" + this.mDcListAll + " mDcListActiveByCid=" + this.mDcListActiveByCid;
        }
        return str;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        DcController.super.dump(fd, pw, args);
        pw.println(" mPhone=" + this.mPhone);
        synchronized (this.mDcListAll) {
            pw.println(" mDcListAll=" + this.mDcListAll);
            pw.println(" mDcListActiveByCid=" + this.mDcListActiveByCid);
        }
    }
}
