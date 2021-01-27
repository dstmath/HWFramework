package com.android.internal.telephony.gsm;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.ServiceState;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.android.internal.telephony.OnsDisplayParams;
import com.android.internal.telephony.ServiceStateTracker;
import com.huawei.internal.telephony.PhoneExt;

public class HwCustGsmServiceStateManager {
    protected Context mContext;
    protected ContentResolver mCr;
    protected GsmCdmaPhone mGsmPhone;
    protected ServiceStateTracker mGsst;

    public HwCustGsmServiceStateManager(IServiceStateTrackerInner sst, PhoneExt phoneExt) {
        if (sst != null && (sst instanceof ServiceStateTracker)) {
            this.mGsst = (ServiceStateTracker) sst;
        }
        this.mGsmPhone = (GsmCdmaPhone) phoneExt.getPhone();
        this.mContext = this.mGsmPhone.getContext();
        this.mCr = this.mContext.getContentResolver();
    }

    public boolean setRoamingStateForOperatorCustomization(ServiceState currentState, boolean isRoamingState) {
        return isRoamingState;
    }

    public OnsDisplayParams getGsmOnsDisplayParamsForGlobalOperator(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        return null;
    }

    public OnsDisplayParams getVirtualNetOnsDisplayParams() {
        return null;
    }

    public boolean skipPlmnUpdateFromCust() {
        return false;
    }

    public OnsDisplayParams setOnsDisplayCustomization(OnsDisplayParams odp, ServiceState currentState) {
        return odp;
    }

    public boolean notUseVirtualName(String imsi) {
        return false;
    }

    public void storeModemRoamingStatus(boolean isRoaming) {
    }

    public OnsDisplayParams getGsmOnsDisplayParamsForVideotron(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        return null;
    }

    public boolean checkIsInternationalRoaming(boolean isRoaming, ServiceState currentState) {
        return isRoaming;
    }

    public IntentFilter getCustIntentFilter(IntentFilter filter) {
        return filter;
    }

    public int handleBroadcastReceived(Context context, Intent intent, int rac) {
        return rac;
    }

    public boolean iscustRoamingRuleAffect(boolean isRoaming) {
        return false;
    }
}
