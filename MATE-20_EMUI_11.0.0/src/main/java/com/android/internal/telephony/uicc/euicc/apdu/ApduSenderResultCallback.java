package com.android.internal.telephony.uicc.euicc.apdu;

import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback;

public abstract class ApduSenderResultCallback extends AsyncResultCallback<byte[]> {
    public abstract boolean shouldContinueOnIntermediateResult(IccIoResult iccIoResult);
}
