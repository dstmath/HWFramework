package com.huawei.wallet.sdk.business.clearssd.util;

import com.huawei.wallet.sdk.common.event.EventBase;

public class EventSSD extends EventBase {
    public static final int EVENT_ID_RANDOM_FAIL = 2;
    public static final int EVENT_ID_RANDOM_SUCCESS = 1;
    public static final int EVENT_ID_RESET_FAIL = 1;
    public static final int EVENT_ID_RESET_SUCCESS = 0;

    public EventSSD(int eventID, String[] result) {
        super(eventID, result);
    }
}
