package com.huawei.internal.telephony;

import android.os.Bundle;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.huawei.android.util.NoExtAPIException;

public class CallEx {
    public static final void hangupAllCalls(Call obj) throws CallStateException {
        throw new NoExtAPIException("method not supported.");
    }

    public static final String[] getConfUriList(Call obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final boolean updateRcsPreCallInfo(android.telecom.Call call, Bundle extras) {
        return call.updateRcsPreCallInfo(extras);
    }

    public static final boolean isActiveSub(android.telecom.Call call) {
        return call.mIsActiveSub;
    }
}
