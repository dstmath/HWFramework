package com.android.server.rms.iaware.memory.data.handle;

import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.policy.DMEServer;

public abstract class AbsDataHandle {
    DMEServer mDMEServer;

    public abstract int reportData(long j, int i, AttrSegments attrSegments);

    protected AbsDataHandle() {
        this.mDMEServer = null;
        this.mDMEServer = DMEServer.getInstance();
    }
}
