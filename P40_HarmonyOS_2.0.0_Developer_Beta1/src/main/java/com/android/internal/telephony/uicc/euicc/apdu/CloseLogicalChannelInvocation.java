package com.android.internal.telephony.uicc.euicc.apdu;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.euicc.async.AsyncMessageInvocation;

class CloseLogicalChannelInvocation extends AsyncMessageInvocation<Integer, Boolean> {
    private static final String LOG_TAG = "CloseChan";
    private final CommandsInterface mCi;

    CloseLogicalChannelInvocation(CommandsInterface ci) {
        this.mCi = ci;
    }

    /* access modifiers changed from: protected */
    public void sendRequestMessage(Integer channel, Message msg) {
        Rlog.v(LOG_TAG, "Channel: " + channel);
        this.mCi.iccCloseLogicalChannel(channel.intValue(), msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncMessageInvocation
    public Boolean parseResult(AsyncResult ar) {
        if (ar.exception == null) {
            return true;
        }
        if (ar.exception instanceof CommandException) {
            Rlog.e(LOG_TAG, "CommandException", ar.exception);
        } else {
            Rlog.e(LOG_TAG, "Unknown exception", ar.exception);
        }
        return false;
    }
}
