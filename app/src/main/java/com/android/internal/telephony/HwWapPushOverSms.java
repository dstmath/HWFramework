package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.Rlog;
import huawei.android.telephony.wrapper.OptWrapperFactory;
import huawei.android.telephony.wrapper.WrapperFactory;

public class HwWapPushOverSms extends WapPushOverSms {
    private static final String LOG_TAG = "HW WAP PUSH";

    public HwWapPushOverSms(Phone phone, SMSDispatcher smsDispatcher) {
        super(phone.getContext());
    }

    public HwWapPushOverSms(Context context) {
        super(context);
    }

    public int dispatchWapPdu(byte[] pdu, String oriaddress) {
        this.mOriginalAddr = oriaddress;
        return dispatchWapPdu(pdu, null, null);
    }

    public int dispatchWapPdu(byte[] pdu, String oriaddress, BroadcastReceiver receiver, InboundSmsHandler handler, boolean is3Gpp2) {
        if (!is3Gpp2) {
            this.mOriginalAddr = oriaddress;
        }
        return dispatchWapPdu(pdu, receiver, handler);
    }

    private void dispatchWapPdu_ConnectWbxml(byte[] pdu, int transactionId, int pduType, int dataIndex, int secType, byte[] macData, InboundSmsHandler handler, BroadcastReceiver receiver) {
        byte[] data = new byte[(pdu.length - dataIndex)];
        System.arraycopy(pdu, dataIndex, data, 0, data.length);
        Intent intent = new Intent("android.provider.Telephony.WAP_PUSH_RECEIVED");
        intent.setType(HwWspTypeDecoder.CONTENT_TYPE_B_CONNECT_WBXML);
        intent.putExtra("transactionId", transactionId);
        intent.putExtra("pduType", pduType);
        intent.putExtra("data", data);
        intent.putExtra("sec", secType);
        intent.putExtra("mac", macData);
        intent.putExtra("sender", this.mOriginalAddr);
        putSubIdAndNetworkTypeToIntentForMultiSim(intent, handler);
        handler.dispatchIntent(intent, null, -1, null, receiver, UserHandle.OWNER);
        this.mOriginalAddr = null;
    }

    protected void putSubIdAndNetworkTypeToIntentForMultiSim(Intent intent, InboundSmsHandler handler) {
        Rlog.d("XXXXXX", "putSubIdAndNetworkTypeToIntentForMultiSim begin");
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            intent.putExtra("sub_id", OptWrapperFactory.getPhoneWrapper().getSubscription(handler.mPhone));
            intent.putExtra("network_type", WrapperFactory.getMSimTelephonyManagerWrapper().getNetworkType(OptWrapperFactory.getPhoneWrapper().getSubscription(handler.mPhone)));
        }
    }

    protected boolean dispatchWapPduForWbxml(byte[] pdu, WspTypeDecoder pduDecoder, int transactionId, int pduType, int headerStartIndex, int headerLength, InboundSmsHandler handler, BroadcastReceiver receiver) {
        if (SystemProperties.get("ro.config.hw_omacp", "0").equals("1")) {
            byte[] bArr = pdu;
            int i = transactionId;
            int i2 = pduType;
            dispatchWapPdu_ConnectWbxml(bArr, i, i2, headerStartIndex + headerLength, pduDecoder.getSec(), pduDecoder.getMacByte(), handler, receiver);
            return true;
        }
        Rlog.d(LOG_TAG, "OMACP is not supported and the message will be discarded.");
        return false;
    }

    protected void saveSmsTracker(InboundSmsTracker tracker) {
        this.mSmsTracker = tracker;
    }
}
