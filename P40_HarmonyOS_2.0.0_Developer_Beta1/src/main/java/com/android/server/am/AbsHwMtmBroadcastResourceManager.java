package com.android.server.am;

import android.app.BroadcastOptionsEx;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.huawei.android.content.IIntentReceiverEx;
import com.huawei.annotation.HwSystemApi;
import java.util.List;

@HwSystemApi
public interface AbsHwMtmBroadcastResourceManager {
    void iawareCheckCombinedConditon(IntentFilter intentFilter);

    void iawareCountDuplicatedReceiver(boolean z, ReceiverListEx receiverListEx, IntentFilter intentFilter);

    void iawareEndCountBroadcastSpeed(BroadcastRecordEx broadcastRecordEx);

    void iawareFilterBroadcast(Intent intent, ProcessRecordEx processRecordEx, String str, int i, int i2, boolean z, String str2, String[] strArr, int i3, BroadcastOptionsEx broadcastOptionsEx, List list, List<BroadcastFilterEx> list2, IIntentReceiverEx iIntentReceiverEx, int i4, String str3, Bundle bundle, boolean z2, boolean z3, boolean z4, int i5, boolean z5, boolean z6);

    boolean iawareNeedSkipBroadcastSend(String str, Object[] objArr);

    boolean iawareProcessBroadcast(int i, boolean z, BroadcastRecordEx broadcastRecordEx, Object obj);

    void iawareStartCountBroadcastSpeed(boolean z, BroadcastRecordEx broadcastRecordEx);
}
