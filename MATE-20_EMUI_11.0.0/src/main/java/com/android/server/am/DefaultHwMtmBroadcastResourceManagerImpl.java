package com.android.server.am;

import android.app.BroadcastOptionsEx;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.huawei.android.content.IIntentReceiverEx;
import com.huawei.annotation.HwSystemApi;
import java.util.List;

@HwSystemApi
public class DefaultHwMtmBroadcastResourceManagerImpl implements AbsHwMtmBroadcastResourceManager {
    public DefaultHwMtmBroadcastResourceManagerImpl() {
    }

    public DefaultHwMtmBroadcastResourceManagerImpl(BroadcastQueueEx queue) {
    }

    @Override // com.android.server.am.AbsHwMtmBroadcastResourceManager
    public boolean iawareProcessBroadcast(int type, boolean isParallel, BroadcastRecordEx broadcastRecord, Object target) {
        return false;
    }

    @Override // com.android.server.am.AbsHwMtmBroadcastResourceManager
    public void iawareStartCountBroadcastSpeed(boolean isParallel, BroadcastRecordEx broadcastRecord) {
    }

    @Override // com.android.server.am.AbsHwMtmBroadcastResourceManager
    public void iawareEndCountBroadcastSpeed(BroadcastRecordEx broadcastRecord) {
    }

    @Override // com.android.server.am.AbsHwMtmBroadcastResourceManager
    public void iawareFilterBroadcast(Intent intent, ProcessRecordEx callerApp, String callerPackage, int callingPid, int callingUid, boolean isCallerInstantApp, String resolvedType, String[] requiredPermissions, int appOp, BroadcastOptionsEx broadcastOptions, List receivers, List<BroadcastFilterEx> list, IIntentReceiverEx resultTo, int resultCode, String resultData, Bundle resultExtras, boolean isOrdered, boolean isSticky, boolean isInitialSticky, int userId, boolean isAllowBackgroundActivityStarts, boolean isTimeoutExempt) {
    }

    @Override // com.android.server.am.AbsHwMtmBroadcastResourceManager
    public void iawareCountDuplicatedReceiver(boolean isRegister, ReceiverListEx receiverList, IntentFilter filter) {
    }

    @Override // com.android.server.am.AbsHwMtmBroadcastResourceManager
    public void iawareCheckCombinedConditon(IntentFilter filter) {
    }

    @Override // com.android.server.am.AbsHwMtmBroadcastResourceManager
    public boolean iawareNeedSkipBroadcastSend(String action, Object[] data) {
        return false;
    }
}
