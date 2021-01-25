package com.android.server.power;

import android.util.proto.ProtoOutputStream;

/* access modifiers changed from: package-private */
public interface SuspendBlocker {
    void acquire();

    void release();

    void writeToProto(ProtoOutputStream protoOutputStream, long j);
}
