package android.telephony;

import android.content.Context;
import android.os.Looper;
import com.android.internal.telephony.HwCallManagerEx;
import com.android.internal.telephony.HwChrServiceManager;
import com.android.internal.telephony.HwChrServiceManagerImpl;
import com.android.internal.telephony.HwDataConnectionManager;
import com.android.internal.telephony.HwDataConnectionManagerImpl;
import com.android.internal.telephony.HwGsmCdmaCallTrackerEx;
import com.android.internal.telephony.HwGsmCdmaConnectionEx;
import com.android.internal.telephony.HwGsmCdmaPhoneEx;
import com.android.internal.telephony.HwIccPhoneBookInterfaceManager;
import com.android.internal.telephony.HwIccSmsInterfaceManagerEx;
import com.android.internal.telephony.HwInnerSmsManager;
import com.android.internal.telephony.HwInnerSmsManagerImpl;
import com.android.internal.telephony.HwNetworkManager;
import com.android.internal.telephony.HwNetworkManagerImpl;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.HwPhoneManagerImpl;
import com.android.internal.telephony.HwPhoneSwitcherEx;
import com.android.internal.telephony.HwReportManager;
import com.android.internal.telephony.HwReportManagerImpl;
import com.android.internal.telephony.HwServiceStateTrackerEx;
import com.android.internal.telephony.HwSmsUsageMonitorEx;
import com.android.internal.telephony.HwSubscriptionControllerReference;
import com.android.internal.telephony.HwSubscriptionInfoUpdaterReference;
import com.android.internal.telephony.HwTelephonyChrManager;
import com.android.internal.telephony.HwTelephonyChrManagerImpl;
import com.android.internal.telephony.HwUiccManager;
import com.android.internal.telephony.HwUiccManagerImpl;
import com.android.internal.telephony.HwUiccPhoneBookController;
import com.android.internal.telephony.HwUiccSmsControllerEx;
import com.android.internal.telephony.HwWapPushOverSmsEx;
import com.android.internal.telephony.HwWspTypeDecoderEx;
import com.android.internal.telephony.ICallManagerInner;
import com.android.internal.telephony.IGsmCdmaCallTrackerInner;
import com.android.internal.telephony.IGsmCdmaConnectionInner;
import com.android.internal.telephony.IHwCallManagerEx;
import com.android.internal.telephony.IHwCdmaSMSDispatcherEx;
import com.android.internal.telephony.IHwGsmCdmaCallTrackerEx;
import com.android.internal.telephony.IHwGsmCdmaConnectionEx;
import com.android.internal.telephony.IHwGsmCdmaPhoneEx;
import com.android.internal.telephony.IHwGsmCdmaPhoneInner;
import com.android.internal.telephony.IHwGsmSMSDispatcherEx;
import com.android.internal.telephony.IHwIccSmsInterfaceManagerEx;
import com.android.internal.telephony.IHwIccSmsInterfaceManagerInner;
import com.android.internal.telephony.IHwPhoneBookInterfaceManagerEx;
import com.android.internal.telephony.IHwPhoneSwitcherEx;
import com.android.internal.telephony.IHwPhoneSwitcherInner;
import com.android.internal.telephony.IHwServiceStateTrackerEx;
import com.android.internal.telephony.IHwSmsUsageMonitorEx;
import com.android.internal.telephony.IHwSubscriptionControllerEx;
import com.android.internal.telephony.IHwSubscriptionInfoUpdaterEx;
import com.android.internal.telephony.IHwUiccPhoneBookControllerEx;
import com.android.internal.telephony.IHwUiccSmsControllerEx;
import com.android.internal.telephony.IHwUiccSmsControllerInner;
import com.android.internal.telephony.IHwWapPushOverSmsEx;
import com.android.internal.telephony.IHwWspTypeDecoderEx;
import com.android.internal.telephony.IIccPhoneBookInterfaceManagerInner;
import com.android.internal.telephony.ISMSDispatcherInner;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.android.internal.telephony.ISmsUsageMonitorInner;
import com.android.internal.telephony.ISubscriptionControllerInner;
import com.android.internal.telephony.ISubscriptionInfoUpdaterInner;
import com.android.internal.telephony.IUiccPhoneBookControllerInner;
import com.android.internal.telephony.IWapPushOverSmsInner;
import com.android.internal.telephony.IWspTypeDecoderInner;
import com.android.internal.telephony.cat.HwCatServiceReference;
import com.android.internal.telephony.cat.ICatServiceInner;
import com.android.internal.telephony.cat.IHwCatServiceEx;
import com.android.internal.telephony.cdma.HwCdmaSMSDispatcherEx;
import com.android.internal.telephony.dataconnection.HwDcTrackerEx;
import com.android.internal.telephony.dataconnection.HwSlicesNetworkFactory;
import com.android.internal.telephony.dataconnection.IHwDcTrackerEx;
import com.android.internal.telephony.emergency.HwEmergencyNumberTrackerMgr;
import com.android.internal.telephony.emergency.HwEmergencyNumberTrackerMgrImpl;
import com.android.internal.telephony.gsm.HwGsmSMSDispatcherEx;
import com.android.internal.telephony.gsm.HwUsimPhoneBookManager;
import com.android.internal.telephony.gsm.HwUsimPhoneBookManagerEmailAnr;
import com.android.internal.telephony.gsm.IHwUsimPhoneBookManagerEx;
import com.android.internal.telephony.gsm.IUsimPhoneBookManagerInner;
import com.android.internal.telephony.uicc.HwAdnRecordCacheEx;
import com.android.internal.telephony.uicc.HwAdnRecordLoaderEx;
import com.android.internal.telephony.uicc.HwIccCardProxyReference;
import com.android.internal.telephony.uicc.HwIccFileHandlerReference;
import com.android.internal.telephony.uicc.HwIccRecordsEx;
import com.android.internal.telephony.uicc.HwRuimRecords;
import com.android.internal.telephony.uicc.HwSIMRecords;
import com.android.internal.telephony.uicc.HwUiccCardReference;
import com.android.internal.telephony.uicc.HwUiccControllerReference;
import com.android.internal.telephony.uicc.IAdnRecordCacheInner;
import com.android.internal.telephony.uicc.IAdnRecordLoaderInner;
import com.android.internal.telephony.uicc.IHwAdnRecordCacheEx;
import com.android.internal.telephony.uicc.IHwAdnRecordLoaderEx;
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
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;

public class HwPartTelephonyFactoryImpl extends HwPartTelephonyFactory {
    public HwInnerSmsManager createHwInnerSmsManager() {
        return HwInnerSmsManagerImpl.getDefault();
    }

    public HwUiccManager createHwUiccManager() {
        return HwUiccManagerImpl.getDefault();
    }

    public IHwCatServiceEx createHwCatServiceEx(ICatServiceInner catServiceInner) {
        return new HwCatServiceReference(catServiceInner);
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

    public IHwGsmCdmaPhoneEx createHwGsmCdmaPhoneEx(IHwGsmCdmaPhoneInner hwGsmCdmaPhoneInner, PhoneExt phoneExt) {
        return new HwGsmCdmaPhoneEx(hwGsmCdmaPhoneInner, phoneExt);
    }

    public HwPhoneManager createHwPhoneManagerImpl() {
        return HwPhoneManagerImpl.getDefault();
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

    public IHwCdmaSMSDispatcherEx createHwCdmaSMSDispatcherEx(Context context, ISMSDispatcherInner smsDispatcher, PhoneExt phoneExt) {
        return new HwCdmaSMSDispatcherEx(context, smsDispatcher, phoneExt);
    }

    public IHwGsmSMSDispatcherEx createHwGsmSMSDispatcherEx(Context context, ISMSDispatcherInner smsDispatcher, PhoneExt phoneExt) {
        return new HwGsmSMSDispatcherEx(context, smsDispatcher, phoneExt);
    }

    public IHwSmsUsageMonitorEx createHwSmsUsageMonitorEx(ISmsUsageMonitorInner smsUsageMonitor, PhoneExt phoneExt) {
        return new HwSmsUsageMonitorEx(smsUsageMonitor, phoneExt);
    }

    public IHwAdnRecordCacheEx createHwAdnRecordCacheEx(IAdnRecordCacheInner adnRecordCacheInner, IIccFileHandlerInner fileHandlerInner) {
        return new HwAdnRecordCacheEx(adnRecordCacheInner, fileHandlerInner);
    }

    public IHwAdnRecordLoaderEx createHwAdnRecordLoaderEx(IAdnRecordLoaderInner adnRecordLoaderInner, IIccFileHandlerInner fileHandlerInner) {
        return new HwAdnRecordLoaderEx(adnRecordLoaderInner, fileHandlerInner);
    }

    public IHwUsimPhoneBookManagerEx createHwUsimPhoneBookManagerEx(IUsimPhoneBookManagerInner iUsimPhoneBookManagerInner, IIccFileHandlerInner iIccFileHandlerInner, IAdnRecordCacheInner iAdnRecordCacheInner, Object lock) {
        if (!IccRecordsEx.getEmailAnrSupport()) {
            return new HwUsimPhoneBookManager(iUsimPhoneBookManagerInner, lock);
        }
        return new HwUsimPhoneBookManagerEmailAnr(iUsimPhoneBookManagerInner, iIccFileHandlerInner, iAdnRecordCacheInner);
    }

    public IHwPhoneBookInterfaceManagerEx createHwIccPhoneBookInterfaceManager(IIccPhoneBookInterfaceManagerInner iIccPhoneBookInterfaceManagerInner, PhoneExt phone, Object lock) {
        return new HwIccPhoneBookInterfaceManager(iIccPhoneBookInterfaceManagerInner, phone, lock);
    }

    public IHwUiccPhoneBookControllerEx createHwUiccPhoneBookController(IUiccPhoneBookControllerInner iUiccPhoneBookControllerInner) {
        return new HwUiccPhoneBookController(iUiccPhoneBookControllerInner);
    }

    public IHwGsmCdmaConnectionEx createHwGsmCdmaConnectionEx(IGsmCdmaConnectionInner gsmCdmaConnectionInner) {
        return new HwGsmCdmaConnectionEx(gsmCdmaConnectionInner);
    }

    public IHwGsmCdmaCallTrackerEx createHwGsmCdmaCallTrackerEx(IGsmCdmaCallTrackerInner gsmCdmaCallTrackerInner) {
        return new HwGsmCdmaCallTrackerEx(gsmCdmaCallTrackerInner);
    }

    public IHwCallManagerEx createHwCallManagerEx(ICallManagerInner callManagerInner) {
        return new HwCallManagerEx(callManagerInner);
    }

    public IHwPhoneSwitcherEx createHwPhoneSwitcherEx(IHwPhoneSwitcherInner phoneSwitcher, int numPhones) {
        return new HwPhoneSwitcherEx(phoneSwitcher, numPhones);
    }
}
