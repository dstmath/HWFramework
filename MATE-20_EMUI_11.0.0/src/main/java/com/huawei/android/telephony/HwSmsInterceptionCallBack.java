package com.huawei.android.telephony;

import android.os.Bundle;

public interface HwSmsInterceptionCallBack {
    int handleSmsDeliverAction(Bundle bundle);

    int handleWapPushDeliverAction(Bundle bundle);

    boolean sendNumberBlockedRecord(Bundle bundle);
}
