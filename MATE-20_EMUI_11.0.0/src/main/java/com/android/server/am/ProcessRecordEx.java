package com.android.server.am;

import android.content.pm.ApplicationInfo;
import com.huawei.android.app.IApplicationThreadEx;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ProcessRecordEx {
    private ProcessRecord mProcessRecord;

    public ProcessRecordEx() {
    }

    public ProcessRecordEx(ProcessRecord record) {
        this.mProcessRecord = record;
    }

    public ProcessRecord getProcessRecord() {
        return this.mProcessRecord;
    }

    public boolean isInstrNull() {
        return this.mProcessRecord.mInstr == null;
    }

    public int getPid() {
        return this.mProcessRecord.pid;
    }

    public int getCurAdj() {
        return this.mProcessRecord.curAdj;
    }

    public String getProcessName() {
        return this.mProcessRecord.processName;
    }

    public int getUid() {
        return this.mProcessRecord.uid;
    }

    public ApplicationInfo getInfo() {
        return this.mProcessRecord.info;
    }

    public int getCurProcState() {
        return this.mProcessRecord.getCurProcState();
    }

    public IApplicationThreadEx getThread() {
        return new IApplicationThreadEx(this.mProcessRecord.thread);
    }

    public boolean isProcessRecordNull() {
        return this.mProcessRecord == null;
    }
}
