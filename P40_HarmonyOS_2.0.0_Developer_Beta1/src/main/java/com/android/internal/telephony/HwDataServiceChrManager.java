package com.android.internal.telephony;

import android.content.Context;
import android.net.LinkProperties;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.dataconnection.ApnContext;
import java.util.concurrent.ConcurrentHashMap;

public interface HwDataServiceChrManager {
    String getAnyDataEnabledFalseReason();

    boolean getBringUp();

    String getDataNotAllowedReason();

    void getModemParamsWhenCdmaPdpActFail(Phone phone, int i);

    boolean getReceivedSimloadedMsg();

    boolean getRecordsLoadedRegistered();

    void init(Context context);

    void removeMonitorWifiSwitchToMobileMessage();

    void sendIntentApnContextDisabledWhenWifiDisconnected(Phone phone, boolean z, boolean z2, ApnContext apnContext);

    void sendIntentApnListEmpty(int i);

    void sendIntentDataSelfCure(int i, int i2);

    void sendIntentDnsFailure(String[] strArr);

    void sendIntentDsUseStatistics(Phone phone, int i);

    void sendIntentHasNoDefaultApn(int i);

    void sendIntentWhenApnNeedReport(Phone phone, ApnSetting apnSetting, int i, LinkProperties linkProperties);

    void sendIntentWhenDataConnected(Phone phone, ApnSetting apnSetting, LinkProperties linkProperties);

    void sendIntentWhenDisableNr(Phone phone, int i, long j);

    void sendIntentWhenDorecovery(Phone phone, int i, String str);

    void sendIntentWhenReenableNr(Phone phone);

    void sendIntentWhenSetDataSubFail(int i);

    void sendMonitorWifiSwitchToMobileMessage(int i);

    void setAnyDataEnabledFalseReason(boolean z, boolean z2, boolean z3, boolean z4);

    void setAnyDataEnabledFalseReasonToNull();

    void setBringUp(boolean z);

    void setCheckApnContextState(boolean z);

    void setDataNotAllowedReasonToNull();

    void setReceivedSimloadedMsg(Phone phone, boolean z, ConcurrentHashMap<String, ApnContext> concurrentHashMap, boolean z2);

    void setRecordsLoadedRegistered(boolean z, int i);
}
