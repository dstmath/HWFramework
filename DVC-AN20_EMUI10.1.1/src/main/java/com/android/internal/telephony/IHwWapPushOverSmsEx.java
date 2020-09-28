package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import com.huawei.internal.telephony.InboundSmsHandlerEx;
import com.huawei.internal.telephony.WspTypeDecoderEx;

public interface IHwWapPushOverSmsEx {
    default boolean dispatchWapPduForWbxml(byte[] pdu, WspTypeDecoderEx pduDecoder, int transactionId, int pduType, int headerStartIndex, int headerLength, InboundSmsHandlerEx handler, BroadcastReceiver receiver) {
        return true;
    }
}
