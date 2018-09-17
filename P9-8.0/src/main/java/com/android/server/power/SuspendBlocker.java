package com.android.server.power;

import android.util.proto.ProtoOutputStream;

interface SuspendBlocker {
    void acquire();

    void release();

    void writeToProto(ProtoOutputStream protoOutputStream, long j);
}
