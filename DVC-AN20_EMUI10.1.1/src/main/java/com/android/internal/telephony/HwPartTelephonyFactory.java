package com.android.internal.telephony;

import android.common.FactoryLoader;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.android.internal.telephony.dataconnection.DefaultHwDcTrackerEx;
import com.android.internal.telephony.dataconnection.IHwDcTrackerEx;
import com.android.internal.telephony.emergency.HwEmergencyNumberTrackerMgr;
import com.android.internal.telephony.uicc.DefaultHwIccFileHandlerEx;
import com.android.internal.telephony.uicc.DefaultHwIccRecordsEx;
import com.android.internal.telephony.uicc.DefaultHwUiccCardEx;
import com.android.internal.telephony.uicc.DefaultHwUiccControllerEx;
import com.android.internal.telephony.uicc.DefaultHwUiccProfileEx;
import com.android.internal.telephony.uicc.DefaultHwVoiceMailConstantsEx;
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
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import java.util.HashMap;

public class HwPartTelephonyFactory {
    private static final int HASH_MAP_INIT_SIZE = 2;
    private static final String TAG = "HwPartTelephonyFactory";
    public static final String TELEPHONY_FACTORY_IMPL_NAME = "android.telephony.HwPartTelephonyFactoryImpl";
    public static final String TELEPHONY_VSIM_FACTORY_IMPL_NAME = "android.telephony.HwPartTelephonyVSimFactoryImpl";
    private static HashMap<String, HwPartTelephonyFactory> mFactory = new HashMap<>(2);

    public static HwPartTelephonyFactory loadFactory(String factoryName) {
        HwPartTelephonyFactory factory;
        if (mFactory.containsKey(factoryName)) {
            return mFactory.get(factoryName);
        }
        Object object = FactoryLoader.loadFactory(factoryName);
        if (object == null || !(object instanceof HwPartTelephonyFactory)) {
            factory = new HwPartTelephonyFactory();
        } else {
            factory = (HwPartTelephonyFactory) object;
        }
        mFactory.put(factoryName, factory);
        Log.i(TAG, "add " + factoryName + " to memory.");
        return factory;
    }

    public HwInnerVSimManager createHwInnerVSimManager() {
        return DefaultHwInnerVSimManager.getDefault();
    }

    public void createHwSlicesNetworkFactory(DcTrackerEx dcTracker, Looper looper, Context context, int phoneId) {
    }

    public HwInnerSmsManager createHwInnerSmsManager() {
        return DefaultHwInnerSmsManager.getDefault();
    }

    public IHwDcTrackerEx createHwDcTrackerEx(PhoneExt phoneExt, DcTrackerEx dcTrackerEx) {
        return new DefaultHwDcTrackerEx();
    }

    public HwDataConnectionManager getHwDataConnectionManager() {
        return DefaultDataConnectionManager.getDefault();
    }

    public HwUiccManager createHwUiccManager() {
        return DefaultHwUiccManager.getDefault();
    }

    public HwReportManager getHwReportManager() {
        return DefaultHwReportManager.getDefault();
    }

    public IHwServiceStateTrackerEx createHwServiceStateTrackerEx(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt) {
        return new DefaultHwServiceStateTrackerEx(serviceStateTracker, phoneExt);
    }

    public HwNetworkManager getHwNetworkManager() {
        return DefaultHwNetworkManager.getDefault();
    }

    public IHwUiccControllerEx createHwUiccControllerEx(IUiccControllerInner uiccController) {
        return new DefaultHwUiccControllerEx(uiccController);
    }

    public IHwUiccCardEx createHwUiccCardEx(IUiccCardInner uiccCardInner) {
        return new DefaultHwUiccCardEx(uiccCardInner);
    }

    public IHwUiccProfileEx createHwUiccProfileEx(IUiccProfileInner uiccProfileInner) {
        return new DefaultHwUiccProfileEx(uiccProfileInner);
    }

    public HwChrServiceManager getHwChrServiceManager() {
        return DefaultHwChrServiceManager.getDefault();
    }

    public HwTelephonyChrManager getHwTelephonyChrManager() {
        return DefaultHwTelephonyChrManager.getDefault();
    }

    public HwEmergencyNumberTrackerMgr getHwEmergencyNumberTrackerMgr() {
        return null;
    }

    public IHwIccRecordsEx createHwSIMRecordsEx(IIccRecordsInner iccRecordsInner, UiccCardApplicationEx app, Context c, CommandsInterfaceEx ci) {
        return new DefaultHwIccRecordsEx(iccRecordsInner);
    }

    public IHwIccRecordsEx createHwRUIMRecordsEx(IIccRecordsInner iccRecordsInner, UiccCardApplicationEx app, Context c, CommandsInterfaceEx ci) {
        return new DefaultHwIccRecordsEx(iccRecordsInner);
    }

    public IHwIccRecordsEx createHwIccRecordsEx(IIccRecordsInner iccRecordsInner, UiccCardApplicationEx app, Context context, CommandsInterfaceEx ci) {
        return new DefaultHwIccRecordsEx(iccRecordsInner);
    }

    public IHwSubscriptionControllerEx createHwSubscriptionControllerEx(Context context, ISubscriptionControllerInner subscriptionControllerInner) {
        return new DefaultHwSubscriptionControllerEx(context, subscriptionControllerInner);
    }

    public IHwSubscriptionInfoUpdaterEx createHwSubscriptionInfoUpdaterEx(ISubscriptionInfoUpdaterInner subscriptionInfoUpdaterInner, Context context, CommandsInterfaceEx[] ci) {
        return new DefaultHwSubscriptionInfoUpdaterEx(subscriptionInfoUpdaterInner, context, ci);
    }

    public IHwIccFileHandlerEx createHwIccFileHandlerReference(IIccFileHandlerInner fileHandler, String aid, CommandsInterfaceEx ci) {
        return new DefaultHwIccFileHandlerEx(fileHandler, aid, ci);
    }

    public IHwVoiceMailConstantsEx creatHwVoiceMailConstantsEx(IVoiceMailConstantsInner voiceMailConstantsInner, Context context, int slotId) {
        return new DefaultHwVoiceMailConstantsEx(voiceMailConstantsInner, context, slotId);
    }

    public IHwUiccSmsControllerEx createHwUiccSmsControllerEx(IHwUiccSmsControllerInner uiccSmsController) {
        return DefaultHwUiccSmsControllerEx.getDefault();
    }

    public IHwIccSmsInterfaceManagerEx createIHwIccSmsInterfaceManagerEx(IHwIccSmsInterfaceManagerInner iccSmsInterfaceManager, PhoneExt phoneExt) {
        return new DefaultHwIccSmsInterfaceManagerEx(iccSmsInterfaceManager, phoneExt);
    }

    public IHwWspTypeDecoderEx createHwWspTypeDecoderEx(IWspTypeDecoderInner wspTypeDecoderInner) {
        return new DefaultHwWspTypeDecoderEx(wspTypeDecoderInner);
    }

    public IHwWapPushOverSmsEx createHwWapPushOverSmsEx(IWapPushOverSmsInner wapPushOverSms) {
        return new DefaultHwWapPushOverSmsEx(wapPushOverSms);
    }
}
