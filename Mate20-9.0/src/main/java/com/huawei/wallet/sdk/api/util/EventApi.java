package com.huawei.wallet.sdk.api.util;

import com.huawei.wallet.sdk.common.event.EventBase;

public class EventApi extends EventBase {
    public static final int EVENT_ID_DEL_SSD_FAIL = 2;
    public static final int EVENT_ID_DEL_SSD_SUCCESS = 1;

    public EventApi(int eventID, String[] result) {
        super(eventID, result);
    }
}
