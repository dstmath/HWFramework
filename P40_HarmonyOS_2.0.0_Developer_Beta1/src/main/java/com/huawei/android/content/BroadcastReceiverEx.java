package com.huawei.android.content;

import android.content.BroadcastReceiver;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BroadcastReceiverEx {
    public static int getSendingUserId(BroadcastReceiver broadcastReceiver) {
        return broadcastReceiver.getSendingUserId();
    }
}
