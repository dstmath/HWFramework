package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public interface HwInnerVSimManager {
    void createHwVSimService(Context context);

    ArrayList<ApnSetting> createVSimApnList();

    void dumpVSimPhoneFactory(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    IccRecords fetchVSimIccRecords(int i);

    String getPendingDeviceInfoFromSP(String str);

    int getTopPrioritySubscriptionId();

    Phone getVSimPhone();

    UiccCardApplication getVSimUiccCardApplication(int i);

    boolean isVSimInStatus(int i, int i2);

    boolean isVSimPhone(Phone phone);

    void makeVSimPhoneFactory(Context context, PhoneNotifier phoneNotifier, Phone[] phoneArr, CommandsInterface[] commandsInterfaceArr);

    ServiceStateTracker makeVSimServiceStateTracker(Phone phone, CommandsInterface commandsInterface);

    void putVSimExtraForIccStateChanged(Intent intent, int i, String str);

    void registerForIccChanged(Handler handler, int i, Object obj);

    void setMarkForCardReload(int i, boolean z);

    void unregisterForIccChanged(Handler handler);
}
