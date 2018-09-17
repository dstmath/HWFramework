package com.android.server.audio;

import android.content.Context;
import android.media.AudioSystem;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.SparseIntArray;

public class HwCustAudioServiceVolumeStreamStateImpl extends HwCustAudioServiceVolumeStreamState {
    private static final int STATE_TURNING_OFF_ALL_SOUND = 1;
    private static final int STATE_TURNING_ON_ALL_SOUND = 2;
    private static final int STATE_TURN_OFF_ALL_SOUND = 3;
    private static final int STATE_TURN_ON_ALL_SOUND = 0;
    private static final String SYSTEM_TURNOFF_ALL_SOUND = "trun_off_all_sound";
    private static final String TAG = "HwCustAudioServiceVolumeStreamStateImpl";
    private boolean mIsFuncTurnOffAllSoundValidate = SystemProperties.getBoolean("ro.show_turn_off_all_sound", false);
    private final SparseIntArray mNotPersistedIndex = new SparseIntArray(8);

    public HwCustAudioServiceVolumeStreamStateImpl(Context context) {
        super(context);
    }

    public void readSettings(int streamType, int device) {
        if (this.mIsFuncTurnOffAllSoundValidate && !isDeviceRelatedStreamByUser(streamType, device)) {
            Log.w(TAG, "device:" + device + "|streamType:" + streamType + "|volume:" + AudioSystem.DEFAULT_STREAM_VOLUME[streamType]);
            this.mNotPersistedIndex.put(device, AudioSystem.DEFAULT_STREAM_VOLUME[streamType]);
        }
    }

    public void applyAllVolumes(boolean isMuted, int streamType) {
        if (this.mIsFuncTurnOffAllSoundValidate) {
            for (int i = 0; i < this.mNotPersistedIndex.size(); i++) {
                int device = this.mNotPersistedIndex.keyAt(i);
                if (this.mNotPersistedIndex.indexOfKey(device) == -1 && device != 1073741824) {
                    int index;
                    if (isTurnOffAllSound() || isMuted) {
                        index = 0;
                    } else {
                        index = this.mNotPersistedIndex.get(device);
                    }
                    AudioSystem.setStreamVolumeIndex(streamType, index, device);
                }
            }
        }
    }

    public boolean isTurnOffAllSound() {
        boolean z = true;
        if (!this.mIsFuncTurnOffAllSoundValidate) {
            return false;
        }
        int state = Global.getInt(this.mContext.getContentResolver(), SYSTEM_TURNOFF_ALL_SOUND, 0);
        if (!(state == 1 || state == STATE_TURN_OFF_ALL_SOUND)) {
            z = false;
        }
        return z;
    }

    private boolean isDeviceRelatedStreamByUser(int streamType, int device) {
        return streamType == 8 && device == 1;
    }

    private int getIndex(int device, SparseIntArray indexMap) {
        int index = indexMap.get(device, -1);
        if (index == -1) {
            return indexMap.get(1073741824);
        }
        return index;
    }
}
