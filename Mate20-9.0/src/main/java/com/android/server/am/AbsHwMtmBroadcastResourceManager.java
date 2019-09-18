package com.android.server.am;

import android.app.BroadcastOptions;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import java.util.List;

public interface AbsHwMtmBroadcastResourceManager {
    void iawareCheckCombinedConditon(IntentFilter intentFilter);

    void iawareCountDuplicatedReceiver(boolean z, ReceiverList receiverList, IntentFilter intentFilter);

    void iawareEndCountBroadcastSpeed(BroadcastRecord broadcastRecord);

    void iawareFilterBroadcast(Intent intent, ProcessRecord processRecord, String str, int i, int i2, boolean z, String str2, String[] strArr, int i3, BroadcastOptions broadcastOptions, List list, List<BroadcastFilter> list2, IIntentReceiver iIntentReceiver, int i4, String str3, Bundle bundle, boolean z2, boolean z3, boolean z4, int i5);

    boolean iawareNeedSkipBroadcastSend(String str, Object[] objArr);

    boolean iawareProcessBroadcast(int i, boolean z, BroadcastRecord broadcastRecord, Object obj);

    void iawareStartCountBroadcastSpeed(boolean z, BroadcastRecord broadcastRecord);
}
