package com.android.server.location;

public interface SdmCallback {
    void onAckSdmDataResponse(long j, int i);

    void onSdmDataRequest(long j, int i);
}
