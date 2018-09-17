package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException.Error;
import java.util.ArrayList;

public class HwHisiCsgSearch extends CsgSearch {
    private static final String LOG_TAG = "HwHisiCsgSearch";

    private class CSGNetworkList {
        public ArrayList<HwHisiCsgNetworkInfo> mCSGNetworks = new ArrayList();
        private HwHisiCsgNetworkInfo mCurSelectingCsgNetwork = null;

        public CSGNetworkList(ArrayList<HwHisiCsgNetworkInfo> csgNetworkInfos) {
            copyFrom(csgNetworkInfos);
        }

        private void copyFrom(ArrayList<HwHisiCsgNetworkInfo> csgNetworkInfos) {
            int csgNetworkInfosSize = csgNetworkInfos.size();
            for (int i = 0; i < csgNetworkInfosSize; i++) {
                this.mCSGNetworks.add((HwHisiCsgNetworkInfo) csgNetworkInfos.get(i));
            }
        }

        public HwHisiCsgNetworkInfo getCurrentSelectingCsgNetwork() {
            return this.mCurSelectingCsgNetwork;
        }

        public boolean isToBeSearchedCsgListsEmpty() {
            return this.mCSGNetworks.isEmpty();
        }

        public HwHisiCsgNetworkInfo getToBeRegsiteredCSGNetwork() {
            this.mCurSelectingCsgNetwork = null;
            if (this.mCSGNetworks == null) {
                Rlog.e(HwHisiCsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return this.mCurSelectingCsgNetwork;
            }
            int list_size = this.mCSGNetworks.size();
            for (int i = 0; i < list_size; i++) {
                HwHisiCsgNetworkInfo csgInfo = (HwHisiCsgNetworkInfo) this.mCSGNetworks.get(i);
                if (!csgInfo.isSelectedFail) {
                    this.mCurSelectingCsgNetwork = csgInfo;
                    break;
                }
                Rlog.d(HwHisiCsgSearch.LOG_TAG, "=csg=  had selected and failed, so not reselect again!");
            }
            return this.mCurSelectingCsgNetwork;
        }
    }

    public HwHisiCsgSearch(GsmCdmaPhone phone) {
        super(phone);
    }

    public void handleMessage(Message msg) {
        Rlog.d(LOG_TAG, "msg id is " + msg.what);
        AsyncResult ar;
        CSGNetworkList csgNetworklist;
        switch (msg.what) {
            case 1:
                Rlog.d(LOG_TAG, "=csg=  Receved EVENT_SELECT_CSG_NETWORK_DONE.");
                ar = msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg=  ar is null, the code should never come here!!");
                    return;
                }
                Message onComplete = ar.userObj;
                if (onComplete != null) {
                    csgNetworklist = onComplete.obj.result;
                    if (ar.exception != null) {
                        Rlog.e(LOG_TAG, "=csg= select CSG failed! " + ar.exception);
                        HwHisiCsgNetworkInfo curSelectingCsgNetwork = csgNetworklist.getCurrentSelectingCsgNetwork();
                        if (curSelectingCsgNetwork == null) {
                            Rlog.i(LOG_TAG, "=csg= current select CSG is null->maybe loop end. response result.");
                            AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                            onComplete.sendToTarget();
                            return;
                        }
                        curSelectingCsgNetwork.isSelectedFail = true;
                        Rlog.e(LOG_TAG, "=csg= mark  current CSG-ID item Failed! " + csgNetworklist.mCurSelectingCsgNetwork);
                        Rlog.i(LOG_TAG, "=csg= select next strongest CSG-ID->start select");
                        selectCSGNetwork(onComplete);
                        return;
                    }
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                Rlog.e(LOG_TAG, "=csg=  ar.userObj is null, the code should never come here!!");
                return;
            case CSGNetworkList.RADIO_IF_UMTS /*5*/:
                Rlog.i(LOG_TAG, "=csg= Receved EVENT_CSG_PERIODIC_SCAN_DONE.");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                } else if (ar.exception != null || ar.result == null) {
                    Rlog.e(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list failed! " + ar.exception);
                    return;
                } else {
                    csgNetworklist = (CSGNetworkList) ar.result;
                    Rlog.d(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list success -> select Csg! ");
                    if (csgNetworklist.isToBeSearchedCsgListsEmpty()) {
                        Rlog.i(LOG_TAG, "=csg= Periodic Search: no avaiable CSG-ID -> cancel periodic search! ");
                        cancelCsgPeriodicSearchTimer();
                        return;
                    }
                    selectCSGNetwork(obtainMessage(6, ar));
                    return;
                }
            default:
                super.handleMessage(msg);
                return;
        }
    }

    void getAvailableCSGNetworks(Message response) {
        Rlog.d(LOG_TAG, "=csg=  getAvailableCSGNetworks...");
        this.mPhone.mCi.getAvailableCSGNetworks(obtainMessage(0, response));
    }

    void handleCsgNetworkQueryResult(AsyncResult ar) {
        Rlog.d(LOG_TAG, "=csg=  handleCsgNetworkQueryResult...");
        if (ar == null || ar.userObj == null) {
            Rlog.e(LOG_TAG, "=csg=  ar or userObj is null, the code should never come here!!");
        } else if (ar.exception != null) {
            Rlog.e(LOG_TAG, "=csg=  exception happen: " + ar.exception);
            AsyncResult.forMessage((Message) ar.userObj, null, ar.exception);
            ((Message) ar.userObj).sendToTarget();
        } else {
            if (ar.result == null) {
                Rlog.e(LOG_TAG, "=csg=  result is null: ");
                AsyncResult.forMessage((Message) ar.userObj, null, new CommandException(Error.GENERIC_FAILURE));
                ((Message) ar.userObj).sendToTarget();
            } else {
                AsyncResult.forMessage((Message) ar.userObj, new CSGNetworkList((ArrayList) ar.result), null);
                ((Message) ar.userObj).sendToTarget();
            }
        }
    }

    void selectCSGNetwork(Message response) {
        AsyncResult ar = response.obj;
        if (ar == null || ar.result == null) {
            Rlog.e(LOG_TAG, "=csg= parsed CSG list is null, return exception");
            AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
            response.sendToTarget();
            return;
        }
        CSGNetworkList csgNetworklist = ar.result;
        if (csgNetworklist.mCSGNetworks.size() > 0) {
            HwHisiCsgNetworkInfo curSelCsgNetwork = csgNetworklist.getToBeRegsiteredCSGNetwork();
            Rlog.d(LOG_TAG, "to be registered CSG info is " + curSelCsgNetwork);
            if (curSelCsgNetwork != null && (curSelCsgNetwork.isEmpty() ^ 1) != 0) {
                this.mPhone.mCi.setCSGNetworkSelectionModeManual(curSelCsgNetwork, obtainMessage(1, response));
                return;
            } else if (curSelCsgNetwork == null || !curSelCsgNetwork.isEmpty()) {
                Rlog.e(LOG_TAG, "=csg= not find suitable CSG-ID, Select CSG fail!");
                AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
                response.sendToTarget();
                return;
            } else {
                Rlog.e(LOG_TAG, "=csg= not find suitable CSG-ID, so finish Select! ");
                AsyncResult.forMessage(response, null, null);
                response.sendToTarget();
                return;
            }
        }
        Rlog.e(LOG_TAG, "=csg= mCSGNetworks is not initailized, return with exception");
        AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
        response.sendToTarget();
    }
}
