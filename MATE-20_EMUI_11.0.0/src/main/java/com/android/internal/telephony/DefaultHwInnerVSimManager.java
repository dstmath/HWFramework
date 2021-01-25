package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.data.ApnSetting;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneNotifierEx;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DefaultHwInnerVSimManager implements HwInnerVSimManager {
    private static HwInnerVSimManager sInstance;

    public static HwInnerVSimManager getDefault() {
        if (sInstance == null) {
            sInstance = new DefaultHwInnerVSimManager();
        }
        return sInstance;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public void createHwVSimService(Context context) {
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public void makeVSimPhoneFactory(Context context, PhoneNotifierEx notifier, PhoneExt[] pps, CommandsInterfaceEx[] cis) {
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public void dumpVSimPhoneFactory(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public ServiceStateTrackerEx makeVSimServiceStateTracker(PhoneExt phone, CommandsInterfaceEx ci) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public PhoneExt getVSimPhone() {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public boolean isVSimPhone(PhoneExt phone) {
        return false;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public boolean isVSimInStatus(int type, int subId) {
        return type == 7;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public void registerForIccChanged(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public void unregisterForIccChanged(Handler h) {
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public IccRecordsEx fetchVSimIccRecords(int family) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public UiccCardApplicationEx getVSimUiccCardApplication(int appFamily) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public void setMarkForCardReload(int subId, boolean value) {
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public String getPendingDeviceInfoFromSP(String prefKey) {
        return null;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public int getTopPrioritySubscriptionId() {
        return 0;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public ArrayList<ApnSetting> createVSimApnList() {
        return new ArrayList<>();
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public void putVSimExtraForIccStateChanged(Intent intent, int subId, String value) {
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public void disposeSSTForVSim() {
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public String changeSpnForVSim(String spn) {
        return spn;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public int changeRuleForVSim(int rule) {
        return rule;
    }

    @Override // com.android.internal.telephony.HwInnerVSimManager
    public IccCardStatusExt.CardStateEx getVSimCardState() {
        return IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;
    }
}
