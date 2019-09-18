package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;

public interface HwTelephonyChrManager {

    public interface FailCause {
        public static final int FAIL_CAUSE_INCALLDATA = 1001;
        public static final int FAIL_CAUSE_PARSEPDU = 2001;
        public static final int FAIL_CAUSE_RESUMELINKFAULT = 1002;
        public static final int FAIL_CAUSE_UT = 1;
    }

    public interface Scenario {
        public static final String INCALL_DATA = "INCALLDATA";
        public static final String RESUME_LINK_FAULT = "RESUMELINKFAULT";
        public static final String SMS = "TELEPHONYSMSEVENT";
        public static final String UT = "UT";
    }

    void init(Context context);

    void sendTelephonyChrBroadcast(Bundle bundle);

    void sendTelephonyChrBroadcast(Bundle bundle, int i);
}
