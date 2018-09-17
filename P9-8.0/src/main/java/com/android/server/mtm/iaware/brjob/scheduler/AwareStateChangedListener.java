package com.android.server.mtm.iaware.brjob.scheduler;

import java.util.List;

public interface AwareStateChangedListener {
    void onControllerStateChanged(List<AwareJobStatus> list);

    void onRemoveJobNow(AwareJobStatus awareJobStatus);

    void onRunJobNow(AwareJobStatus awareJobStatus);
}
