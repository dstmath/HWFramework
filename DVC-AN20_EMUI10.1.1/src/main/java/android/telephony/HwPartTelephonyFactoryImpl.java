package android.telephony;

import android.content.Context;
import android.os.Looper;
import com.android.internal.telephony.HwChrServiceManager;
import com.android.internal.telephony.HwChrServiceManagerImpl;
import com.android.internal.telephony.HwDataConnectionManager;
import com.android.internal.telephony.HwDataConnectionManagerImpl;
import com.android.internal.telephony.HwIccSmsInterfaceManagerEx;
import com.android.internal.telephony.HwInnerSmsManager;
import com.android.internal.telephony.HwInnerSmsManagerImpl;
import com.android.internal.telephony.HwNetworkManager;
import com.android.internal.telephony.HwNetworkManagerImpl;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwReportManager;
import com.android.internal.telephony.HwReportManagerImpl;
import com.android.internal.telephony.HwServiceStateTrackerEx;
import com.android.internal.telephony.HwSubscriptionControllerReference;
import com.android.internal.telephony.HwSubscriptionInfoUpdaterReference;
import com.android.internal.telephony.HwTelephonyChrManager;
import com.android.internal.telephony.HwTelephonyChrManagerImpl;
import com.android.internal.telephony.HwUiccManager;
import com.android.internal.telephony.HwUiccManagerImpl;
import com.android.internal.telephony.HwUiccSmsControllerEx;
import com.android.internal.telephony.HwWapPushOverSmsEx;
import com.android.internal.telephony.HwWspTypeDecoderEx;
import com.android.internal.telephony.IHwIccSmsInterfaceManagerEx;
import com.android.internal.telephony.IHwIccSmsInterfaceManagerInner;
import com.android.internal.telephony.IHwServiceStateTrackerEx;
import com.android.internal.telephony.IHwSubscriptionControllerEx;
import com.android.internal.telephony.IHwSubscriptionInfoUpdaterEx;
import com.android.internal.telephony.IHwUiccSmsControllerEx;
import com.android.internal.telephony.IHwUiccSmsControllerInner;
import com.android.internal.telephony.IHwWapPushOverSmsEx;
import com.android.internal.telephony.IHwWspTypeDecoderEx;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.android.internal.telephony.ISubscriptionControllerInner;
import com.android.internal.telephony.ISubscriptionInfoUpdaterInner;
import com.android.internal.telephony.IWapPushOverSmsInner;
import com.android.internal.telephony.IWspTypeDecoderInner;
import com.android.internal.telephony.dataconnection.HwDcTrackerEx;
import com.android.internal.telephony.dataconnection.HwSlicesNetworkFactory;
import com.android.internal.telephony.dataconnection.IHwDcTrackerEx;
import com.android.internal.telephony.emergency.HwEmergencyNumberTrackerMgr;
import com.android.internal.telephony.emergency.HwEmergencyNumberTrackerMgrImpl;
import com.android.internal.telephony.uicc.HwIccCardProxyReference;
import com.android.internal.telephony.uicc.HwIccFileHandlerReference;
import com.android.internal.telephony.uicc.HwIccRecordsEx;
import com.android.internal.telephony.uicc.HwRuimRecords;
import com.android.internal.telephony.uicc.HwSIMRecords;
import com.android.internal.telephony.uicc.HwUiccCardReference;
import com.android.internal.telephony.uicc.HwUiccControllerReference;
import com.android.internal.telephony.uicc.IHwIccFileHandlerEx;
import com.android.internal.telephony.uicc.IHwIccRecordsEx;
import com.android.internal.telephony.uicc.IHwUiccCardEx;
import com.android.internal.telephony.uicc.IHwUiccControllerEx;
import com.android.internal.telephony.uicc.IHwUiccProfileEx;
import com.android.internal.telephony.uicc.IHwVoiceMailConstantsEx;
import com.android.internal.telephony.uicc.IIccFileHandlerInner;
import com.android.internal.telephony.uicc.IIccRecordsInner;
import com.android.internal.telephony.uicc.IUiccCardInner;
import com.android.internal.telephony.uicc.IUiccControllerInner;
import com.android.internal.telephony.uicc.IUiccProfileInner;
import com.android.internal.telephony.uicc.IVoiceMailConstantsInner;
import com.android.internal.telephony.uicc.VoiceMailConstantsEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;

public class HwPartTelephonyFactoryImpl extends HwPartTelephonyFactory {
    public HwInnerSmsManager createHwInnerSmsManager() {
        return HwInnerSmsManagerImpl.getDefault();
    }

    public HwUiccManager createHwUiccManager() {
        return HwUiccManagerImpl.getDefault();
    }

    public HwReportManager getHwReportManager() {
        return HwReportManagerImpl.getDefault();
    }

    public IHwServiceStateTrackerEx createHwServiceStateTrackerEx(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt) {
        return new HwServiceStateTrackerEx(serviceStateTracker, phoneExt);
    }

    public HwNetworkManager getHwNetworkManager() {
        return HwNetworkManagerImpl.getDefault();
    }

    public IHwUiccControllerEx createHwUiccControllerEx(IUiccControllerInner uiccController) {
        return new HwUiccControllerReference(uiccController);
    }

    public IHwUiccCardEx createHwUiccCardEx(IUiccCardInner uiccCardInner) {
        return new HwUiccCardReference(uiccCardInner);
    }

    public IHwUiccProfileEx createHwUiccProfileEx(IUiccProfileInner uiccProfileInner) {
        return new HwIccCardProxyReference(uiccProfileInner);
    }

    public IHwDcTrackerEx createHwDcTrackerEx(PhoneExt phoneExt, DcTrackerEx dcTrackerEx) {
        return new HwDcTrackerEx(phoneExt, dcTrackerEx);
    }

    public HwChrServiceManager getHwChrServiceManager() {
        return HwChrServiceManagerImpl.getDefault();
    }

    public HwTelephonyChrManager getHwTelephonyChrManager() {
        return HwTelephonyChrManagerImpl.getDefault();
    }

    public HwEmergencyNumberTrackerMgr getHwEmergencyNumberTrackerMgr() {
        return HwEmergencyNumberTrackerMgrImpl.getDefault();
    }

    public IHwIccRecordsEx createHwSIMRecordsEx(IIccRecordsInner iccRecordsInner, UiccCardApplicationEx app, Context context, CommandsInterfaceEx ci) {
        return new HwSIMRecords(iccRecordsInner, app, context, ci);
    }

    public IHwIccRecordsEx createHwRUIMRecordsEx(IIccRecordsInner iccRecordsInner, UiccCardApplicationEx app, Context context, CommandsInterfaceEx ci) {
        return new HwRuimRecords(iccRecordsInner, app, context, ci);
    }

    public IHwIccRecordsEx createHwIccRecordsEx(IIccRecordsInner iccRecordsInner, UiccCardApplicationEx app, Context context, CommandsInterfaceEx ci) {
        return new HwIccRecordsEx(iccRecordsInner, app, context, ci);
    }

    public IHwSubscriptionControllerEx createHwSubscriptionControllerEx(Context context, ISubscriptionControllerInner subscriptionControllerInner) {
        return new HwSubscriptionControllerReference(context, subscriptionControllerInner);
    }

    public IHwSubscriptionInfoUpdaterEx createHwSubscriptionInfoUpdaterEx(ISubscriptionInfoUpdaterInner subscriptionInfoUpdaterInner, Context context, CommandsInterfaceEx[] ci) {
        return new HwSubscriptionInfoUpdaterReference(subscriptionInfoUpdaterInner, context, ci);
    }

    public IHwIccFileHandlerEx createHwIccFileHandlerReference(IIccFileHandlerInner fileHandler, String aid, CommandsInterfaceEx ci) {
        return new HwIccFileHandlerReference(fileHandler, aid, ci);
    }

    public IHwVoiceMailConstantsEx creatHwVoiceMailConstantsEx(IVoiceMailConstantsInner voiceMailConstantsInner, Context context, int slotId) {
        return new VoiceMailConstantsEx(voiceMailConstantsInner, context, slotId);
    }

    public void createHwSlicesNetworkFactory(DcTrackerEx dcTracker, Looper looper, Context context, int phoneId) {
        new HwSlicesNetworkFactory(dcTracker, looper, context, phoneId);
    }

    public HwDataConnectionManager getHwDataConnectionManager() {
        return HwDataConnectionManagerImpl.getDefault();
    }

    public IHwUiccSmsControllerEx createHwUiccSmsControllerEx(IHwUiccSmsControllerInner uiccSmsController) {
        return new HwUiccSmsControllerEx(uiccSmsController);
    }

    public IHwIccSmsInterfaceManagerEx createIHwIccSmsInterfaceManagerEx(IHwIccSmsInterfaceManagerInner iccSmsInterfaceManager, PhoneExt phoneExt) {
        return new HwIccSmsInterfaceManagerEx(iccSmsInterfaceManager, phoneExt);
    }

    public IHwWspTypeDecoderEx createHwWspTypeDecoderEx(IWspTypeDecoderInner wspTypeDecoderInner) {
        return new HwWspTypeDecoderEx(wspTypeDecoderInner);
    }

    public IHwWapPushOverSmsEx createHwWapPushOverSmsEx(IWapPushOverSmsInner wapPushOverSmsInner) {
        return new HwWapPushOverSmsEx(wapPushOverSmsInner);
    }
}
