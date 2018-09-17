package com.android.server.audio;

import android.content.Context;
import android.util.Log;

public class HwCustAudioService {
    private static final boolean DEBUG = true;
    private static final String TAG = "HwCustAudioService";
    protected Context mContext;

    public HwCustAudioService(Context context) {
        this.mContext = context;
    }

    public boolean isTurningAllSound() {
        Log.v(TAG, "isTurningAllSound dummy interface");
        return false;
    }
}
