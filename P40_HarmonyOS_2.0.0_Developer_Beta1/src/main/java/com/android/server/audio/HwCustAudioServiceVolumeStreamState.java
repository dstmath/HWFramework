package com.android.server.audio;

import android.content.Context;
import android.util.Log;

public class HwCustAudioServiceVolumeStreamState {
    private static final boolean DEBUG = true;
    private static final String TAG = "HwCustAudioServiceVolumeStreamState";
    protected Context mContext;

    public HwCustAudioServiceVolumeStreamState(Context context) {
        this.mContext = context;
    }

    public void readSettings(int streamType, int device) {
        Log.i(TAG, "readSettings dummy interface");
    }

    public void applyAllVolumes(boolean isMuted, int streamType) {
        Log.i(TAG, "applyAllVolumes dummy interface");
    }

    public boolean isTurnOffAllSound() {
        Log.i(TAG, "isTurnOffAllSound dummy interface");
        return false;
    }
}
