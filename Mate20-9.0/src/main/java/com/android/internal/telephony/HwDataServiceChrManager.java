package com.android.internal.telephony;

import android.content.Context;
import android.net.LinkProperties;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSetting;
import java.util.concurrent.ConcurrentHashMap;

public interface HwDataServiceChrManager {
    void SendIntentDNSfailure(String[] strArr);

    String getAnyDataEnabledFalseReason();

    boolean getBringUp();

    String getDataNotAllowedReason();

    void getModemParamsWhenCdmaPdpActFail(Phone phone, int i);

    String getPdpActiveIpType();

    boolean getReceivedSimloadedMsg();

    boolean getRecordsLoadedRegistered();

    void init(Context context);

    void removeMonitorWifiSwitchToMobileMessage();

    void sendIntentApnContextDisabledWhenWifiDisconnected(Phone phone, boolean z, boolean z2, ApnContext apnContext);

    void sendIntentApnListEmpty(int i);

    void sendIntentDSUseStatistics(Phone phone, int i);

    void sendIntentDataConnectionSetupResult(int i, String str, String str2, String str3, String str4, LinkProperties linkProperties);

    void sendIntentWhenDataConnected(Phone phone, ApnSetting apnSetting, LinkProperties linkProperties);

    void sendIntentWhenDorecovery(Phone phone, int i);

    void sendIntentWhenSetDataSubFail(int i);

    void sendMonitorWifiSwitchToMobileMessage(int i);

    void setAnyDataEnabledFalseReason(boolean z, boolean z2, boolean z3, boolean z4);

    void setAnyDataEnabledFalseReasonToNull();

    void setBringUp(boolean z);

    void setCheckApnContextState(boolean z);

    void setDataNotAllowedReason(Phone phone, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6);

    void setDataNotAllowedReasonToNull();

    void setPdpActiveIpType(String str, int i);

    void setReceivedSimloadedMsg(Phone phone, boolean z, ConcurrentHashMap<String, ApnContext> concurrentHashMap, boolean z2);

    void setRecordsLoadedRegistered(boolean z, int i);
}
