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

    /* access modifiers changed from: protected */
    public void putSubIdAndNetworkTypeToIntentForMultiSim(Intent intent, InboundSmsHandler handler) {
    }

    /* access modifiers changed from: protected */
    public boolean dispatchWapPduForWbxml(byte[] pdu, WspTypeDecoder pduDecoder, int transactionId, int pduType, int headerStartIndex, int headerLength, InboundSmsHandler handler, BroadcastReceiver receiver) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void saveSmsTracker(InboundSmsTracker tracker) {
    }
}
