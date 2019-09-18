package com.android.mms.pdu;

import android.content.Context;
import com.google.android.mms.pdu.EncodedStringValue;

public class HwCustPduPersister {
    public boolean hasShortCode(EncodedStringValue[] toNumbers, EncodedStringValue[] ccNumbers) {
        return false;
    }

    public boolean isShortCodeFeatureEnabled() {
        return false;
    }

    public boolean hasShortCode(boolean isMMS, String[] list, Context context, String toastString) {
        return false;
    }
}
