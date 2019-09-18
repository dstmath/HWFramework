package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import huawei.android.telephony.wrapper.OptWrapperFactory;
import huawei.android.telephony.wrapper.WrapperFactory;

public class HwWapPushOverSms extends WapPushOverSms {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HW WAP PUSH";
    private static final String OMACP_SUPPORT_CUST = "hw_cust_omacp_not_support";

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
        byte[] bArr = pdu;
        int i = dataIndex;
        byte[] data = new byte[(bArr.length - i)];
        System.arraycopy(bArr, i, data, 0, data.length);
        Intent intent = new Intent("android.provider.Telephony.WAP_PUSH_RECEIVED");
        intent.setType(HwWspTypeDecoder.CONTENT_TYPE_B_CONNECT_WBXML);
        intent.putExtra("transactionId", transactionId);
        intent.putExtra("pduType", pduType);
        intent.putExtra("data", data);
        intent.putExtra("sec", secType);
        intent.putExtra("mac", macData);
        intent.putExtra("sender", this.mOriginalAddr);
        InboundSmsHandler inboundSmsHandler = handler;
        putSubIdAndNetworkTypeToIntentForMultiSim(intent, inboundSmsHandler);
        inboundSmsHandler.dispatchIntent(intent, null, -1, null, receiver, UserHandle.OWNER);
        this.mOriginalAddr = null;
    }

    /* access modifiers changed from: protected */
    public void putSubIdAndNetworkTypeToIntentForMultiSim(Intent intent, InboundSmsHandler handler) {
        Rlog.d("XXXXXX", "putSubIdAndNetworkTypeToIntentForMultiSim begin");
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            intent.putExtra(HwTelephonyChrManagerImpl.SUB_ID, OptWrapperFactory.getPhoneWrapper().getSubscription(handler.mPhone));
            intent.putExtra("network_type", WrapperFactory.getMSimTelephonyManagerWrapper().getNetworkType(OptWrapperFactory.getPhoneWrapper().getSubscription(handler.mPhone)));
        }
    }

    /* access modifiers changed from: protected */
    public boolean dispatchWapPduForWbxml(byte[] pdu, WspTypeDecoder pduDecoder, int transactionId, int pduType, int headerStartIndex, int headerLength, InboundSmsHandler handler, BroadcastReceiver receiver) {
        if (SystemProperties.get("ro.config.hw_omacp", "0").equals("1")) {
            InboundSmsHandler inboundSmsHandler = handler;
            if (isCustMccMncOmacpEnabled(inboundSmsHandler)) {
                dispatchWapPdu_ConnectWbxml(pdu, transactionId, pduType, headerStartIndex + headerLength, pduDecoder.getSec(), pduDecoder.getMacByte(), inboundSmsHandler, receiver);
                return true;
            }
        } else {
            InboundSmsHandler inboundSmsHandler2 = handler;
        }
        Rlog.d(LOG_TAG, "OMACP is not supported and the message will be discarded.");
        return false;
    }

    /* access modifiers changed from: protected */
    public void saveSmsTracker(InboundSmsTracker tracker) {
        this.mSmsTracker = tracker;
    }

    private boolean isCustMccMncOmacpEnabled(InboundSmsHandler aHandler) {
        if (aHandler == null || aHandler.mPhone == null || aHandler.mPhone.getContext() == null || aHandler.mPhone.getContext().getContentResolver() == null) {
            return true;
        }
        String lCustMccMnc = Settings.System.getString(aHandler.mPhone.getContext().getContentResolver(), OMACP_SUPPORT_CUST);
        if (!TextUtils.isEmpty(lCustMccMnc)) {
            String lCurrentMccMnc = ((TelephonyManager) aHandler.mPhone.getContext().getSystemService("phone")).getSimOperator(aHandler.mPhone.getSubId());
            if (!TextUtils.isEmpty(lCurrentMccMnc)) {
                String[] custMccmncAfterSplit = lCustMccMnc.split(",");
                for (String equals : custMccmncAfterSplit) {
                    if (lCurrentMccMnc.equals(equals)) {
                        Rlog.d(LOG_TAG, "OMACP is not supported for current Mcc/Mnc and the message will be discarded.");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
