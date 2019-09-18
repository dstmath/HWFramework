package com.android.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.ServiceManager;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.imsphone.ImsExternalCallTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.vsim.VSimUtilsInner;

public class TelephonyComponentFactory {
    private static TelephonyComponentFactory sInstance;

    public static TelephonyComponentFactory getInstance() {
        if (sInstance == null) {
            sInstance = new TelephonyComponentFactory();
        }
        return sInstance;
    }

    public GsmCdmaCallTracker makeGsmCdmaCallTracker(GsmCdmaPhone phone) {
        return new GsmCdmaCallTracker(phone);
    }

    public SmsStorageMonitor makeSmsStorageMonitor(Phone phone) {
        return new SmsStorageMonitor(phone);
    }

    public SmsUsageMonitor makeSmsUsageMonitor(Context context, Phone phone) {
        return HwTelephonyFactory.getHwInnerSmsManager().createHwSmsUsageMonitor(context, phone);
    }

    public ServiceStateTracker makeServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        if (phone == null || !VSimUtilsInner.isVSimSub(phone.getPhoneId())) {
            return new ServiceStateTracker(phone, ci);
        }
        return VSimUtilsInner.makeVSimServiceStateTracker(phone, ci);
    }

    public NitzStateMachine makeNitzStateMachine(GsmCdmaPhone phone) {
        return new NitzStateMachine(phone);
    }

    public SimActivationTracker makeSimActivationTracker(Phone phone) {
        return new SimActivationTracker(phone);
    }

    public DcTracker makeDcTracker(Phone phone) {
        return new DcTracker(phone, 1);
    }

    public CarrierSignalAgent makeCarrierSignalAgent(Phone phone) {
        return new CarrierSignalAgent(phone);
    }

    public CarrierActionAgent makeCarrierActionAgent(Phone phone) {
        return new CarrierActionAgent(phone);
    }

    public CarrierIdentifier makeCarrierIdentifier(Phone phone) {
        return new CarrierIdentifier(phone);
    }

    public IccPhoneBookInterfaceManager makeIccPhoneBookInterfaceManager(Phone phone) {
        return HwTelephonyFactory.getHwUiccManager().createHwIccPhoneBookInterfaceManager(phone);
    }

    public IccSmsInterfaceManager makeIccSmsInterfaceManager(Phone phone) {
        return HwTelephonyFactory.getHwInnerSmsManager().createHwIccSmsInterfaceManager(phone);
    }

    public UiccProfile makeUiccProfile(Context context, CommandsInterface ci, IccCardStatus ics, int phoneId, UiccCard uiccCard, Object lock) {
        UiccProfile uiccProfile = new UiccProfile(context, ci, ics, phoneId, uiccCard, lock);
        return uiccProfile;
    }

    public EriManager makeEriManager(Phone phone, Context context, int eriFileSource) {
        return new EriManager(phone, context, eriFileSource);
    }

    public WspTypeDecoder makeWspTypeDecoder(byte[] pdu) {
        return new WspTypeDecoder(pdu);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String displayAddr, String messageBody) {
        InboundSmsTracker inboundSmsTracker = new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, is3gpp2WapPdu, address, displayAddr, messageBody);
        return inboundSmsTracker;
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, String displayAddr, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody) {
        InboundSmsTracker inboundSmsTracker = new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, address, displayAddr, referenceNumber, sequenceNumber, messageCount, is3gpp2WapPdu, messageBody);
        return inboundSmsTracker;
    }

    public InboundSmsTracker makeInboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        return new InboundSmsTracker(cursor, isCurrentFormat3gpp2);
    }

    public ImsPhoneCallTracker makeImsPhoneCallTracker(ImsPhone imsPhone) {
        return new ImsPhoneCallTracker(imsPhone);
    }

    public ImsExternalCallTracker makeImsExternalCallTracker(ImsPhone imsPhone) {
        return new ImsExternalCallTracker(imsPhone);
    }

    public AppSmsManager makeAppSmsManager(Context context) {
        return new AppSmsManager(context);
    }

    public DeviceStateMonitor makeDeviceStateMonitor(Phone phone) {
        return new DeviceStateMonitor(phone);
    }

    public CdmaSubscriptionSourceManager getCdmaSubscriptionSourceManagerInstance(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        return CdmaSubscriptionSourceManager.getInstance(context, ci, h, what, obj);
    }

    public IDeviceIdleController getIDeviceIdleController() {
        return IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
    }

    public LocaleTracker makeLocaleTracker(Phone phone, Looper looper) {
        return new LocaleTracker(phone, looper);
    }
}
