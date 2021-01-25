package com.android.internal.telephony.uicc.euicc.apdu;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.euicc.async.AsyncMessageInvocation;

/* access modifiers changed from: package-private */
public class OpenLogicalChannelInvocation extends AsyncMessageInvocation<String, IccOpenLogicalChannelResponse> {
    private static final String LOG_TAG = "OpenChan";
    private final CommandsInterface mCi;

    OpenLogicalChannelInvocation(CommandsInterface ci) {
        this.mCi = ci;
    }

    /* access modifiers changed from: protected */
    public void sendRequestMessage(String aid, Message msg) {
        this.mCi.iccOpenLogicalChannel(aid, 0, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncMessageInvocation
    public IccOpenLogicalChannelResponse parseResult(AsyncResult ar) {
        IccOpenLogicalChannelResponse openChannelResp;
        if (ar.exception != null || ar.result == null) {
            if (ar.result == null) {
                Rlog.e(LOG_TAG, "Empty response");
            }
            if (ar.exception != null) {
                Rlog.e(LOG_TAG, "Exception", ar.exception);
            }
            int errorCode = 4;
            if (ar.exception instanceof CommandException) {
                CommandException.Error error = ((CommandException) ar.exception).getCommandError();
                if (error == CommandException.Error.MISSING_RESOURCE) {
                    errorCode = 2;
                } else if (error == CommandException.Error.NO_SUCH_ELEMENT) {
                    errorCode = 3;
                }
            }
            openChannelResp = new IccOpenLogicalChannelResponse(-1, errorCode, null);
        } else {
            int[] result = (int[]) ar.result;
            int channel = result[0];
            byte[] selectResponse = null;
            if (result.length > 1) {
                selectResponse = new byte[(result.length - 1)];
                for (int i = 1; i < result.length; i++) {
                    selectResponse[i - 1] = (byte) result[i];
                }
            }
            openChannelResp = new IccOpenLogicalChannelResponse(channel, 1, selectResponse);
        }
        Rlog.v(LOG_TAG, "Response: " + openChannelResp);
        return openChannelResp;
    }
}
