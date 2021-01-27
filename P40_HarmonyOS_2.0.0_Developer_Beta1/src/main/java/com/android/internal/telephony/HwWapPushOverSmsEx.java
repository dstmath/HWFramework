package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.InboundSmsHandlerEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.WspTypeDecoderEx;
import huawei.android.telephony.wrapper.WrapperFactory;

public class HwWapPushOverSmsEx extends DefaultHwWapPushOverSmsEx {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HW WAP PUSH";
    private static final String OMACP_SUPPORT_CUST = "hw_cust_omacp_not_support";
    private IWapPushOverSmsInner mWapPushOverSms;

    public HwWapPushOverSmsEx(IWapPushOverSmsInner wapPushOverSms) {
        this.mWapPushOverSms = wapPushOverSms;
    }

    private void dispatchWapPduConnectWbXml(byte[] pdu, int transactionId, int pduType, int dataIndex, int secType, byte[] macData, InboundSmsHandlerEx handler, BroadcastReceiver receiver) {
        byte[] data = new byte[(pdu.length - dataIndex)];
        System.arraycopy(pdu, dataIndex, data, 0, data.length);
        Intent intent = new Intent("android.provider.Telephony.WAP_PUSH_RECEIVED");
        intent.setType(HwWspTypeDecoderEx.CONTENT_TYPE_B_CONNECT_WBXML);
        intent.putExtra("transactionId", transactionId);
        intent.putExtra("pduType", pduType);
        intent.putExtra("data", data);
        intent.putExtra("sec", secType);
        intent.putExtra("mac", macData);
        intent.putExtra("sender", this.mWapPushOverSms.getOriginalAddr());
        putSubIdAndNetworkTypeToIntentForMultiSim(intent, handler);
        handler.dispatchIntent(intent, (String) null, AppOpsManagerEx.getOp(-1), (Bundle) null, receiver, UserHandleEx.OWNER);
        this.mWapPushOverSms.setOriginalAddr((String) null);
    }

    private void putSubIdAndNetworkTypeToIntentForMultiSim(Intent intent, InboundSmsHandlerEx handler) {
        PhoneExt phone;
        RlogEx.i(LOG_TAG, "putSubIdAndNetworkTypeToIntentForMultiSim begin");
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() && (phone = handler.getPhoneExt()) != null) {
            int subId = phone.getSubId();
            intent.putExtra(HwTelephonyChrManagerImpl.SUB_ID, subId);
            intent.putExtra("network_type", WrapperFactory.getMSimTelephonyManagerWrapper().getNetworkType(subId));
        }
    }

    public boolean dispatchWapPduForWbxml(byte[] pdu, WspTypeDecoderEx pduDecoder, int transactionId, int pduType, int headerStartIndex, int headerLength, InboundSmsHandlerEx handler, BroadcastReceiver receiver) {
        if (pdu != null && pduDecoder != null) {
            if (handler != null) {
                if (SystemPropertiesEx.get("ro.config.hw_omacp", "0").equals("1")) {
                    if (isCustMccMncOmacpEnabled(handler)) {
                        dispatchWapPduConnectWbXml(pdu, transactionId, pduType, headerStartIndex + headerLength, pduDecoder.getSec(), pduDecoder.getMacByte(), handler, receiver);
                        return true;
                    }
                }
                RlogEx.e(LOG_TAG, "OMACP is not supported and the message will be discarded.");
                return false;
            }
        }
        RlogEx.e(LOG_TAG, "some input is null.");
        return false;
    }

    private boolean isCustMccMncOmacpEnabled(InboundSmsHandlerEx aHandler) {
        PhoneExt phone;
        String[] custMccmncAfterSplit;
        if (aHandler == null || (phone = aHandler.getPhoneExt()) == null || phone.getContext() == null || phone.getContext().getContentResolver() == null) {
            return true;
        }
        String lCustMccMnc = Settings.System.getString(phone.getContext().getContentResolver(), OMACP_SUPPORT_CUST);
        if (TextUtils.isEmpty(lCustMccMnc)) {
            return true;
        }
        String lCurrentMccMnc = TelephonyManagerEx.getSimOperatorNumericForPhone(phone.getPhoneId());
        if (!TextUtils.isEmpty(lCurrentMccMnc)) {
            for (String str : lCustMccMnc.split(",")) {
                if (lCurrentMccMnc.equals(str)) {
                    RlogEx.e(LOG_TAG, "OMACP is not supported for current Mcc/Mnc and the message will be discarded.");
                    return false;
                }
            }
        }
        return true;
    }
}
