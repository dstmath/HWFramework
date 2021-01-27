package com.android.server.am;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ReceiverListEx {
    private ReceiverList mReceiverList;

    public ReceiverListEx() {
    }

    public ReceiverListEx(ReceiverList receiverList) {
        this.mReceiverList = receiverList;
    }

    public int getPid() {
        return this.mReceiverList.pid;
    }

    public int getUid() {
        return this.mReceiverList.uid;
    }

    public ProcessRecordEx getApp() {
        return new ProcessRecordEx(this.mReceiverList.app);
    }

    public boolean isReceiverListNull() {
        return this.mReceiverList == null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ReceiverListEx)) {
            return false;
        }
        ReceiverList receiverList = ((ReceiverListEx) obj).mReceiverList;
        if (receiverList != null) {
            return receiverList.equals(this.mReceiverList);
        }
        if (this.mReceiverList == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        ReceiverList receiverList = this.mReceiverList;
        if (receiverList == null) {
            return 0;
        }
        return receiverList.hashCode();
    }
}
