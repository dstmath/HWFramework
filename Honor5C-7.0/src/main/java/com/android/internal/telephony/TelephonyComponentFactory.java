package com.android.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.IDeviceIdleController.Stub;
import android.os.ServiceManager;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.imsphone.ImsExternalCallTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.ImsPullCall;
import com.android.internal.telephony.uicc.IccCardProxy;
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

    public SmsUsageMonitor makeSmsUsageMonitor(Context context) {
        return new SmsUsageMonitor(context);
    }

    public ServiceStateTracker makeServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        if (phone == null || !VSimUtilsInner.isVSimSub(phone.getPhoneId())) {
            return new ServiceStateTracker(phone, ci);
        }
        return VSimUtilsInner.makeVSimServiceStateTracker(phone, ci);
    }

    public DcTracker makeDcTracker(Phone phone) {
        return new DcTracker(phone);
    }

    public IccPhoneBookInterfaceManager makeIccPhoneBookInterfaceManager(Phone phone) {
        return HwTelephonyFactory.getHwUiccManager().createHwIccPhoneBookInterfaceManager(phone);
    }

    public IccSmsInterfaceManager makeIccSmsInterfaceManager(Phone phone) {
        return HwTelephonyFactory.getHwInnerSmsManager().createHwIccSmsInterfaceManager(phone);
    }

    public IccCardProxy makeIccCardProxy(Context context, CommandsInterface ci, int phoneId) {
        return new IccCardProxy(context, ci, phoneId);
    }

    public EriManager makeEriManager(Phone phone, Context context, int eriFileSource) {
        return new EriManager(phone, context, eriFileSource);
    }

    public WspTypeDecoder makeWspTypeDecoder(byte[] pdu) {
        return new WspTypeDecoder(pdu);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String messageBody) {
        return new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, is3gpp2WapPdu, address, messageBody);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody) {
        return new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, address, referenceNumber, sequenceNumber, messageCount, is3gpp2WapPdu, messageBody);
    }

    public InboundSmsTracker makeInboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        return new InboundSmsTracker(cursor, isCurrentFormat3gpp2);
    }

    public ImsPhoneCallTracker makeImsPhoneCallTracker(ImsPhone imsPhone) {
        return new ImsPhoneCallTracker(imsPhone);
    }

    public ImsExternalCallTracker makeImsExternalCallTracker(ImsPhone imsPhone, ImsPullCall callPuller) {
        return new ImsExternalCallTracker(imsPhone, callPuller);
    }

    public CdmaSubscriptionSourceManager getCdmaSubscriptionSourceManagerInstance(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        return CdmaSubscriptionSourceManager.getInstance(context, ci, h, what, obj);
    }

    public IDeviceIdleController getIDeviceIdleController() {
        return Stub.asInterface(ServiceManager.getService("deviceidle"));
    }
}
