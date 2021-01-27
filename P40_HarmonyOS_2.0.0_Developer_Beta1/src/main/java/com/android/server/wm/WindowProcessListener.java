package com.android.server.wm;

import android.util.proto.ProtoOutputStream;

public interface WindowProcessListener {
    void appDied();

    void clearProfilerIfNeeded();

    long getCpuTime();

    boolean isRemoved();

    void onStartActivity(int i, boolean z, String str, long j);

    void setPendingUiClean(boolean z);

    void setPendingUiCleanAndForceProcessStateUpTo(int i);

    void updateProcessInfo(boolean z, boolean z2, boolean z3);

    void updateServiceConnectionActivities();

    void writeToProto(ProtoOutputStream protoOutputStream, long j);
}
