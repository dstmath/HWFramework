package com.st.android.nfc_extensions;

import android.util.Log;

public class StNonAidBasedServiceInfo {
    static final boolean DBG = true;
    static final String TAG = "APINfc_StNonAidBasedServiceInfo";
    int mSeId;

    public StNonAidBasedServiceInfo(int host_id) {
        Log.d(TAG, "Constructor - hostId: " + host_id);
        this.mSeId = host_id;
    }

    public int getSeId() {
        Log.d(TAG, "getHostId - seId: " + this.mSeId);
        return this.mSeId;
    }
}
