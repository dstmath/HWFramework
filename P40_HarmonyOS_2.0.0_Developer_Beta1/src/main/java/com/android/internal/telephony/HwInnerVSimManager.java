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

public interface HwInnerVSimManager {
    int changeRuleForVSim(int i);

    String changeSpnForVSim(String str);

    void createHwVSimService(Context context);

    ArrayList<ApnSetting> createVSimApnList();

    void disposeSSTForVSim();

    void dumpVSimPhoneFactory(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    IccRecordsEx fetchVSimIccRecords(int i);

    String getPendingDeviceInfoFromSP(String str);

    int getTopPrioritySubscriptionId();

    IccCardStatusExt.CardStateEx getVSimCardState();

    PhoneExt getVSimPhone();

    int getVSimSlot();

    UiccCardApplicationEx getVSimUiccCardApplication(int i);

    boolean isVSimInStatus(int i, int i2);

    boolean isVSimPhone(PhoneExt phoneExt);

    void makeVSimPhoneFactory(Context context, PhoneNotifierEx phoneNotifierEx, PhoneExt[] phoneExtArr, CommandsInterfaceEx[] commandsInterfaceExArr);

    ServiceStateTrackerEx makeVSimServiceStateTracker(PhoneExt phoneExt, CommandsInterfaceEx commandsInterfaceEx);

    void putVSimExtraForIccStateChanged(Intent intent, int i, String str);

    void registerForIccChanged(Handler handler, int i, Object obj);

    void setMarkForCardReload(int i, boolean z);

    void unregisterForIccChanged(Handler handler);
}
