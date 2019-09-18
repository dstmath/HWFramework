package com.android.internal.telephony.dataconnection;

import android.hardware.radio.V1_2.ScanIntervalRange;
import android.net.INetworkPolicyListener;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkPolicyManager;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.data.DataCallResponse;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DcController extends StateMachine {
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_DORMANT = 1;
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_INACTIVE = 0;
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_UP = 2;
    static final int DATA_CONNECTION_ACTIVE_UNKNOWN = Integer.MAX_VALUE;
    private static final boolean DBG = true;
    private static final boolean VDBG = false;
    /* access modifiers changed from: private */
    public final DataServiceManager mDataServiceManager;
    /* access modifiers changed from: private */
    public final HashMap<Integer, DataConnection> mDcListActiveByCid = new HashMap<>();
    final ArrayList<DataConnection> mDcListAll = new ArrayList<>();
    /* access modifiers changed from: private */
    public final DcTesterDeactivateAll mDcTesterDeactivateAll;
    private DccDefaultState mDccDefaultState;
    /* access modifiers changed from: private */
    public final DcTracker mDct;
    /* access modifiers changed from: private */
    public volatile boolean mExecutingCarrierChange;
    /* access modifiers changed from: private */
    public final INetworkPolicyListener mListener;
    final NetworkPolicyManager mNetworkPolicyManager;
    /* access modifiers changed from: private */
    public final Phone mPhone;
    private PhoneStateListener mPhoneStateListener;
    final TelephonyManager mTelephonyManager;

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
                } else {
                    DcController.this.log("DccDefaultState: Unexpected exception on EVENT_RIL_CONNECTED");
                }
            } else if (i == 262151) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null) {
                    onDataStateChanged((ArrayList) ar2.result);
                } else {
                    DcController.this.log("DccDefaultState: EVENT_DATA_STATE_CHANGED: exception; likely radio not available, ignore");
                }
            }
            return true;
        }

        /* JADX INFO: finally extract failed */
        /* JADX WARNING: Code restructure failed: missing block: B:117:0x048c, code lost:
            r0 = th;
         */
        /* JADX WARNING: Removed duplicated region for block: B:91:0x03c1  */
        /* JADX WARNING: Removed duplicated region for block: B:94:0x03ca  */
        private void onDataStateChanged(ArrayList<DataCallResponse> dcsList) {
            ArrayList<DataConnection> dcListAll;
            HashMap<Integer, DataConnection> dcListActiveByCid;
            HashMap<Integer, DataConnection> dcListActiveByCid2;
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid;
            ArrayList<DataConnection> dcListAll2;
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid2;
            boolean z;
            LinkProperties.CompareResult<LinkAddress> car;
            synchronized (DcController.this.mDcListAll) {
                try {
                    dcListAll = new ArrayList<>(DcController.this.mDcListAll);
                    dcListActiveByCid = new HashMap<>(DcController.this.mDcListActiveByCid);
                } catch (Throwable th) {
                    th = th;
                    ArrayList<DataCallResponse> arrayList = dcsList;
                    while (true) {
                        throw th;
                    }
                }
            }
            DcController.this.lr("onDataStateChanged: dcsList=" + dcsList + " dcListActiveByCid=" + dcListActiveByCid);
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid3 = new HashMap<>();
            Iterator<DataCallResponse> it = dcsList.iterator();
            while (it.hasNext()) {
                DataCallResponse dcs = it.next();
                dataCallResponseListByCid3.put(Integer.valueOf(dcs.getCallId()), dcs);
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
            Iterator<DataCallResponse> it2 = dcsList.iterator();
            while (it2.hasNext()) {
                DataCallResponse newState = it2.next();
                DataConnection dc2 = dcListActiveByCid.get(Integer.valueOf(newState.getCallId()));
                if (dc2 != null) {
                    if (dc2.mApnContexts.size() == 0) {
                        DcController.this.loge("onDataStateChanged: no connected apns, ignore");
                    } else {
                        DcController.this.log("onDataStateChanged: Found ConnId=" + newState.getCallId() + " newState=" + newState.toString());
                        if (newState.getActive() == 0) {
                            for (ApnContext apnContext : dc2.mApnContexts.keySet()) {
                                apnContext.setReason(PhoneInternalInterface.REASON_LOST_DATA_CONNECTION);
                            }
                            if (DcController.this.mDct.isCleanupRequired.get()) {
                                apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                DcController.this.mDct.isCleanupRequired.set(false);
                            } else {
                                DcFailCause failCause = DcFailCause.fromInt(newState.getStatus());
                                if (failCause.isRestartRadioFail(DcController.this.mPhone.getContext(), DcController.this.mPhone.getSubId())) {
                                    DcController dcController = DcController.this;
                                    StringBuilder sb = new StringBuilder();
                                    dcListAll2 = dcListAll;
                                    sb.append("onDataStateChanged: X restart radio, failCause=");
                                    sb.append(failCause);
                                    dcController.log(sb.toString());
                                    DcController.this.mDct.sendRestartRadio();
                                } else {
                                    dcListAll2 = dcListAll;
                                    if (DcController.this.mDct.isPermanentFailure(failCause)) {
                                        DcController.this.log("onDataStateChanged: inactive, add to cleanup list. failCause=" + failCause);
                                        apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                    } else {
                                        DcController.this.log("onDataStateChanged: inactive, add to retry list. failCause=" + failCause);
                                        if (!DcController.this.mDct.needRetryAfterDisconnected(failCause)) {
                                            DcController.this.log("onDataStateChanged: not needRetryAfterDisconnected !");
                                            DcController.this.mDct.setRetryAfterDisconnectedReason(dc2, apnsToCleanup);
                                        } else {
                                            dcsToRetry.add(dc2);
                                        }
                                    }
                                }
                            }
                        } else {
                            dcListAll2 = dcListAll;
                            DataConnection.UpdateLinkPropertyResult result = dc2.updateLinkProperty(newState);
                            if (result.oldLp.equals(result.newLp)) {
                                DcController.this.log("onDataStateChanged: no change");
                            } else {
                                if (result.oldLp.isIdenticalInterfaceName(result.newLp)) {
                                    if (!result.oldLp.isIdenticalDnses(result.newLp) || !result.oldLp.isIdenticalRoutes(result.newLp) || !result.oldLp.isIdenticalHttpProxy(result.newLp) || !result.oldLp.isIdenticalAddresses(result.newLp)) {
                                        if (DcController.this.mPhone.getPhoneType() != 2) {
                                            dataCallResponseListByCid = dataCallResponseListByCid3;
                                        } else if (!isSupportRcnInd) {
                                            dataCallResponseListByCid = dataCallResponseListByCid3;
                                        }
                                        LinkProperties.CompareResult<LinkAddress> car2 = result.oldLp.compareAddresses(result.newLp);
                                        DcController.this.log("onDataStateChanged: oldLp=" + result.oldLp + " newLp=" + result.newLp + " car=" + car2);
                                        boolean needToClean = false;
                                        for (LinkAddress added : car2.added) {
                                            HashMap<Integer, DataConnection> dcListActiveByCid3 = dcListActiveByCid;
                                            Iterator it3 = car2.removed.iterator();
                                            while (true) {
                                                if (!it3.hasNext()) {
                                                    car = car2;
                                                    break;
                                                }
                                                car = car2;
                                                LinkAddress removed = (LinkAddress) it3.next();
                                                Iterator it4 = it3;
                                                LinkAddress linkAddress = removed;
                                                if (NetworkUtils.addressTypeMatches(removed.getAddress(), added.getAddress())) {
                                                    needToClean = true;
                                                    break;
                                                } else {
                                                    car2 = car;
                                                    it3 = it4;
                                                }
                                            }
                                            dcListActiveByCid = dcListActiveByCid3;
                                            car2 = car;
                                        }
                                        dcListActiveByCid2 = dcListActiveByCid;
                                        if (needToClean) {
                                            DcController.this.log("onDataStateChanged: addr change, cleanup apns=" + dc2.mApnContexts + " oldLp=" + result.oldLp + " newLp=" + result.newLp);
                                            apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                            DataConnection.UpdateLinkPropertyResult updateLinkPropertyResult = result;
                                        } else {
                                            DcController.this.log("onDataStateChanged: simple change");
                                            for (ApnContext apnContext2 : dc2.mApnContexts.keySet()) {
                                                DcController.this.mPhone.notifyDataConnection("linkPropertiesChanged", apnContext2.getApnType());
                                                result = result;
                                            }
                                        }
                                    }
                                    DcController dcController2 = DcController.this;
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append("onDataStateChanged: no changes, is CDMA Phone:");
                                    dataCallResponseListByCid2 = dataCallResponseListByCid3;
                                    if (DcController.this.mPhone.getPhoneType() == 2) {
                                        z = true;
                                    } else {
                                        z = false;
                                    }
                                    sb2.append(z);
                                    sb2.append(" is Supprot Reconnect ind:");
                                    sb2.append(isSupportRcnInd);
                                    dcController2.log(sb2.toString());
                                    dcListActiveByCid2 = dcListActiveByCid;
                                } else {
                                    dataCallResponseListByCid = dataCallResponseListByCid3;
                                    dcListActiveByCid2 = dcListActiveByCid;
                                    apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                    DcController.this.log("onDataStateChanged: interface change, cleanup apns=" + dc2.mApnContexts);
                                }
                                if (newState.getActive() == 2) {
                                    isAnyDataCallActive = true;
                                }
                                if (newState.getActive() == 1) {
                                    isAnyDataCallDormant = true;
                                }
                                dcListAll = dcListAll2;
                                dataCallResponseListByCid3 = dataCallResponseListByCid;
                                dcListActiveByCid = dcListActiveByCid2;
                            }
                        }
                        dataCallResponseListByCid2 = dataCallResponseListByCid3;
                        dcListActiveByCid2 = dcListActiveByCid;
                        if (newState.getActive() == 2) {
                        }
                        if (newState.getActive() == 1) {
                        }
                        dcListAll = dcListAll2;
                        dataCallResponseListByCid3 = dataCallResponseListByCid;
                        dcListActiveByCid = dcListActiveByCid2;
                    }
                    dcListAll2 = dcListAll;
                    dataCallResponseListByCid2 = dataCallResponseListByCid3;
                    dcListActiveByCid2 = dcListActiveByCid;
                    if (newState.getActive() == 2) {
                    }
                    if (newState.getActive() == 1) {
                    }
                    dcListAll = dcListAll2;
                    dataCallResponseListByCid3 = dataCallResponseListByCid;
                    dcListActiveByCid = dcListActiveByCid2;
                }
            }
            HashMap<Integer, DataCallResponse> hashMap = dataCallResponseListByCid3;
            HashMap<Integer, DataConnection> hashMap2 = dcListActiveByCid;
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
            Iterator<ApnContext> it5 = apnsToCleanup.iterator();
            while (it5.hasNext()) {
                DcController.this.mDct.sendCleanUpConnection(true, it5.next());
            }
            Iterator<DataConnection> it6 = dcsToRetry.iterator();
            while (it6.hasNext()) {
                DataConnection dc3 = it6.next();
                DcController.this.log("onDataStateChanged: send EVENT_LOST_CONNECTION dc.mTag=" + dc3.mTag);
                dc3.sendMessage(262153, dc3.mTag);
            }
        }
    }

    private DcController(String name, Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler) {
        super(name, handler);
        DcTesterDeactivateAll dcTesterDeactivateAll = null;
        this.mDccDefaultState = new DccDefaultState();
        this.mListener = new NetworkPolicyManager.Listener() {
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
        setLogRecSize(ScanIntervalRange.MAX);
        log("E ctor");
        this.mPhone = phone;
        this.mDct = dct;
        this.mDataServiceManager = dataServiceManager;
        addState(this.mDccDefaultState);
        setInitialState(this.mDccDefaultState);
        log("X ctor");
        this.mPhoneStateListener = new PhoneStateListener(handler.getLooper()) {
            public void onCarrierNetworkChange(boolean active) {
                boolean unused = DcController.this.mExecutingCarrierChange = active;
            }
        };
        this.mTelephonyManager = (TelephonyManager) phone.getContext().getSystemService("phone");
        this.mNetworkPolicyManager = (NetworkPolicyManager) phone.getContext().getSystemService("netpolicy");
        this.mDcTesterDeactivateAll = Build.IS_DEBUGGABLE ? new DcTesterDeactivateAll(this.mPhone, this, getHandler()) : dcTesterDeactivateAll;
        if (this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 65536);
        }
    }

    public static DcController makeDcc(Phone phone, DcTracker dct, DataServiceManager dataServiceManager, Handler handler) {
        DcController dcc = new DcController("Dcc", phone, dct, dataServiceManager, handler);
        return dcc;
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        log("dispose: call quiteNow()");
        if (this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
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

    /* access modifiers changed from: private */
    public void lr(String s) {
        logAndAddLogRec(s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        String info = DataConnection.cmdToString(what);
        if (info == null) {
            return DcAsyncChannel.cmdToString(what);
        }
        return info;
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
