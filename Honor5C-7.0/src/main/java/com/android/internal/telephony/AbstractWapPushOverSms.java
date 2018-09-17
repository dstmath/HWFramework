package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.ServiceConnection;

public abstract class AbstractWapPushOverSms implements ServiceConnection {
    public int dispatchWapPdu(byte[] pdu, String oriaddress, BroadcastReceiver receiver, InboundSmsHandler handler, boolean is3gpp2) {
        return 0;
    }

    public int dispatchWapPdu(byte[] pdu, String oriaddress, BroadcastReceiver receiver, InboundSmsHandler handler) {
        return 0;
    }

    protected void putSubIdAndNetworkTypeToIntentForMultiSim(Intent intent, InboundSmsHandler handler) {
    }

    protected boolean dispatchWapPduForWbxml(byte[] pdu, WspTypeDecoder pduDecoder, int transactionId, int pduType, int headerStartIndex, int headerLength, InboundSmsHandler handler, BroadcastReceiver receiver) {
        return false;
    }

    protected void saveSmsTracker(InboundSmsTracker tracker) {
    }
}
