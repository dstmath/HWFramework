package com.android.server.rms.iaware.memory.data.handle;

import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.policy.DmeServer;

public abstract class AbsDataHandle {
    DmeServer mDmeServer;

    public abstract int reportData(long j, int i, AttrSegments attrSegments);

    protected AbsDataHandle() {
        this.mDmeServer = null;
        this.mDmeServer = DmeServer.getInstance();
    }
}
