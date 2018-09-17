package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import com.huawei.android.view.HwWindowManager;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Action {
    protected Context mContext;
    protected AtomicBoolean mInterrupt = new AtomicBoolean(false);

    public abstract int execute(Bundle bundle);

    public abstract void reset();

    public Action(Context context) {
        this.mContext = context;
    }

    public void interrupt(boolean interrupted) {
        this.mInterrupt.set(interrupted);
    }

    public boolean reqInterrupt(Bundle extras) {
        return false;
    }

    public boolean canBeExecuted() {
        return true;
    }

    public int getLastExecFailCount() {
        return 0;
    }

    protected void releaseSnapshots(int memLevel) {
        AwareLog.i("Action", "wms released " + HwWindowManager.releaseSnapshots(memLevel) + " snapshots");
    }
}
