package com.android.internal.telephony.dataconnection;

import android.net.LinkAddress;
import android.net.LinkProperties.CompareResult;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.DctConstants.Activity;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.dataconnection.DataConnection.UpdateLinkPropertyResult;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class DcController extends StateMachine {
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_DORMANT = 1;
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_INACTIVE = 0;
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_UP = 2;
    static final int DATA_CONNECTION_ACTIVE_UNKNOWN = Integer.MAX_VALUE;
    private static final boolean DBG = true;
    private static final boolean VDBG = false;
    private HashMap<Integer, DataConnection> mDcListActiveByCid = new HashMap();
    ArrayList<DataConnection> mDcListAll = new ArrayList();
    private DcTesterDeactivateAll mDcTesterDeactivateAll;
    private DccDefaultState mDccDefaultState = new DccDefaultState(this, null);
    private DcTracker mDct;
    private volatile boolean mExecutingCarrierChange;
    private Phone mPhone;
    private PhoneStateListener mPhoneStateListener;
    TelephonyManager mTelephonyManager;

    private class DccDefaultState extends State {
        /* synthetic */ DccDefaultState(DcController this$0, DccDefaultState -this1) {
            this();
        }

        private DccDefaultState() {
        }

        public void enter() {
            DcController.this.mPhone.mCi.registerForRilConnected(DcController.this.getHandler(), 262149, null);
            DcController.this.mPhone.mCi.registerForDataCallListChanged(DcController.this.getHandler(), 262151, null);
            if (Build.IS_DEBUGGABLE) {
                DcController.this.mDcTesterDeactivateAll = new DcTesterDeactivateAll(DcController.this.mPhone, DcController.this, DcController.this.getHandler());
            }
        }

        public void exit() {
            if (DcController.this.mPhone != null) {
                DcController.this.mPhone.mCi.unregisterForRilConnected(DcController.this.getHandler());
                DcController.this.mPhone.mCi.unregisterForDataCallListChanged(DcController.this.getHandler());
            }
            if (DcController.this.mDcTesterDeactivateAll != null) {
                DcController.this.mDcTesterDeactivateAll.dispose();
            }
        }

        public boolean processMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case 262149:
                    ar = msg.obj;
                    if (ar.exception != null) {
                        DcController.this.log("DccDefaultState: Unexpected exception on EVENT_RIL_CONNECTED");
                        break;
                    }
                    DcController.this.log("DccDefaultState: msg.what=EVENT_RIL_CONNECTED mRilVersion=" + ar.result);
                    break;
                case 262151:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        DcController.this.log("DccDefaultState: EVENT_DATA_STATE_CHANGED: exception; likely radio not available, ignore");
                        break;
                    }
                    onDataStateChanged((ArrayList) ar.result);
                    break;
            }
            return true;
        }

        private void onDataStateChanged(ArrayList<DataCallResponse> dcsList) {
            DataConnection dc;
            DcController.this.lr("onDataStateChanged: dcsList=" + dcsList + " mDcListActiveByCid=" + DcController.this.mDcListActiveByCid);
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid = new HashMap();
            for (DataCallResponse dcs : dcsList) {
                dataCallResponseListByCid.put(Integer.valueOf(dcs.cid), dcs);
            }
            ArrayList<DataConnection> dcsToRetry = new ArrayList();
            for (DataConnection dc2 : DcController.this.mDcListActiveByCid.values()) {
                if (dataCallResponseListByCid.get(Integer.valueOf(dc2.mCid)) == null) {
                    DcController.this.log("onDataStateChanged: add to retry dc=" + dc2);
                    dcsToRetry.add(dc2);
                }
            }
            DcController.this.log("onDataStateChanged: dcsToRetry=" + dcsToRetry);
            ArrayList<ApnContext> apnsToCleanup = new ArrayList();
            boolean isAnyDataCallDormant = false;
            boolean isAnyDataCallActive = false;
            boolean isSupportRcnInd = HwModemCapability.isCapabilitySupport(14);
            for (DataCallResponse newState : dcsList) {
                dc2 = (DataConnection) DcController.this.mDcListActiveByCid.get(Integer.valueOf(newState.cid));
                if (dc2 != null) {
                    if (dc2.mApnContexts.size() == 0) {
                        DcController.this.loge("onDataStateChanged: no connected apns, ignore");
                    } else {
                        DcController.this.log("onDataStateChanged: Found ConnId=" + newState.cid + " newState=" + newState.toString());
                        if (newState.active == 0) {
                            for (ApnContext apnContext : dc2.mApnContexts.keySet()) {
                                apnContext.setReason(PhoneInternalInterface.REASON_LOST_DATA_CONNECTION);
                            }
                            if (DcController.this.mDct.isCleanupRequired.get()) {
                                apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                DcController.this.mDct.isCleanupRequired.set(false);
                            } else {
                                DcFailCause failCause = DcFailCause.fromInt(newState.status);
                                if (failCause.isRestartRadioFail(DcController.this.mPhone.getContext(), DcController.this.mPhone.getSubId())) {
                                    DcController.this.log("onDataStateChanged: X restart radio, failCause=" + failCause);
                                    DcController.this.mDct.sendRestartRadio();
                                } else if (DcController.this.mDct.isPermanentFailure(failCause)) {
                                    DcController.this.log("onDataStateChanged: inactive, add to cleanup list. failCause=" + failCause);
                                    apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                } else {
                                    DcController.this.log("onDataStateChanged: inactive, add to retry list. failCause=" + failCause);
                                    if (DcController.this.mDct.needRetryAfterDisconnected(failCause)) {
                                        dcsToRetry.add(dc2);
                                    } else {
                                        DcController.this.log("onDataStateChanged: not needRetryAfterDisconnected !");
                                        DcController.this.mDct.setRetryAfterDisconnectedReason(dc2, apnsToCleanup);
                                    }
                                }
                            }
                        } else {
                            UpdateLinkPropertyResult result = dc2.updateLinkProperty(newState);
                            if (result.oldLp.equals(result.newLp)) {
                                DcController.this.log("onDataStateChanged: no change");
                            } else if (!result.oldLp.isIdenticalInterfaceName(result.newLp)) {
                                apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                DcController.this.log("onDataStateChanged: interface change, cleanup apns=" + dc2.mApnContexts);
                            } else if ((result.oldLp.isIdenticalDnses(result.newLp) && (result.oldLp.isIdenticalRoutes(result.newLp) ^ 1) == 0 && (result.oldLp.isIdenticalHttpProxy(result.newLp) ^ 1) == 0 && (result.oldLp.isIdenticalAddresses(result.newLp) ^ 1) == 0) || (DcController.this.mPhone.getPhoneType() == 2 && (isSupportRcnInd ^ 1) == 0)) {
                                DcController.this.log("onDataStateChanged: no changes, is CDMA Phone:" + (DcController.this.mPhone.getPhoneType() == 2) + " is Supprot Reconnect ind:" + isSupportRcnInd);
                            } else {
                                CompareResult<LinkAddress> car = result.oldLp.compareAddresses(result.newLp);
                                DcController.this.log("onDataStateChanged: oldLp=" + result.oldLp + " newLp=" + result.newLp + " car=" + car);
                                boolean needToClean = false;
                                for (LinkAddress added : car.added) {
                                    for (LinkAddress removed : car.removed) {
                                        if (NetworkUtils.addressTypeMatches(removed.getAddress(), added.getAddress())) {
                                            needToClean = true;
                                            break;
                                        }
                                    }
                                }
                                if (needToClean) {
                                    DcController.this.log("onDataStateChanged: addr change, cleanup apns=" + dc2.mApnContexts + " oldLp=" + result.oldLp + " newLp=" + result.newLp);
                                    apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                } else {
                                    DcController.this.log("onDataStateChanged: simple change");
                                    for (ApnContext apnContext2 : dc2.mApnContexts.keySet()) {
                                        DcController.this.mPhone.notifyDataConnection("linkPropertiesChanged", apnContext2.getApnType());
                                    }
                                }
                            }
                        }
                    }
                    if (newState.active == 2) {
                        isAnyDataCallActive = true;
                    }
                    if (newState.active == 1) {
                        isAnyDataCallDormant = true;
                    }
                }
            }
            if (!isAnyDataCallDormant || (isAnyDataCallActive ^ 1) == 0) {
                DcController.this.log("onDataStateChanged: Data Activity updated to NONE. isAnyDataCallActive = " + isAnyDataCallActive + " isAnyDataCallDormant = " + isAnyDataCallDormant);
                if (isAnyDataCallActive) {
                    DcController.this.mDct.sendStartNetStatPoll(Activity.NONE);
                }
            } else {
                DcController.this.log("onDataStateChanged: Data Activity updated to DORMANT. stopNetStatePoll");
                DcController.this.mDct.sendStopNetStatPoll(Activity.DORMANT);
            }
            DcController.this.lr("onDataStateChanged: dcsToRetry=" + dcsToRetry + " apnsToCleanup=" + apnsToCleanup);
            for (ApnContext apnContext22 : apnsToCleanup) {
                DcController.this.mDct.sendCleanUpConnection(true, apnContext22);
            }
            for (DataConnection dc22 : dcsToRetry) {
                DcController.this.log("onDataStateChanged: send EVENT_LOST_CONNECTION dc.mTag=" + dc22.mTag);
                dc22.sendMessage(262153, dc22.mTag);
            }
        }
    }

    private DcController(String name, Phone phone, DcTracker dct, Handler handler) {
        super(name, handler);
        setLogRecSize(300);
        log("E ctor");
        this.mPhone = phone;
        this.mDct = dct;
        addState(this.mDccDefaultState);
        setInitialState(this.mDccDefaultState);
        log("X ctor");
        this.mPhoneStateListener = new PhoneStateListener(handler.getLooper()) {
            public void onCarrierNetworkChange(boolean active) {
                DcController.this.mExecutingCarrierChange = active;
            }
        };
        this.mTelephonyManager = (TelephonyManager) phone.getContext().getSystemService("phone");
        if (this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 65536);
        }
    }

    public static DcController makeDcc(Phone phone, DcTracker dct, Handler handler) {
        DcController dcc = new DcController("Dcc", phone, dct, handler);
        dcc.start();
        return dcc;
    }

    void dispose() {
        log("dispose: call quiteNow()");
        if (this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        }
        quitNow();
    }

    void addDc(DataConnection dc) {
        this.mDcListAll.add(dc);
    }

    void removeDc(DataConnection dc) {
        this.mDcListActiveByCid.remove(Integer.valueOf(dc.mCid));
        this.mDcListAll.remove(dc);
    }

    public void addActiveDcByCid(DataConnection dc) {
        if (dc.mCid < 0) {
            log("addActiveDcByCid dc.mCid < 0 dc=" + dc);
        }
        this.mDcListActiveByCid.put(Integer.valueOf(dc.mCid), dc);
    }

    public DataConnection getActiveDcByCid(int cid) {
        return (DataConnection) this.mDcListActiveByCid.get(Integer.valueOf(cid));
    }

    void removeActiveDcByCid(DataConnection dc) {
        if (((DataConnection) this.mDcListActiveByCid.remove(Integer.valueOf(dc.mCid))) == null) {
            log("removeActiveDcByCid removedDc=null dc=" + dc);
        }
    }

    boolean isExecutingCarrierChange() {
        return this.mExecutingCarrierChange;
    }

    void getDataCallList() {
        log("DcController:getDataCallList");
        this.mPhone.mCi.getDataCallList(obtainMessage(262151));
    }

    private void lr(String s) {
        logAndAddLogRec(s);
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected String getWhatToString(int what) {
        String info = DataConnection.cmdToString(what);
        if (info == null) {
            return DcAsyncChannel.cmdToString(what);
        }
        return info;
    }

    public String toString() {
        return "mDcListAll=" + this.mDcListAll + " mDcListActiveByCid=" + this.mDcListActiveByCid;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDcListAll=" + this.mDcListAll);
        pw.println(" mDcListActiveByCid=" + this.mDcListActiveByCid);
    }
}
