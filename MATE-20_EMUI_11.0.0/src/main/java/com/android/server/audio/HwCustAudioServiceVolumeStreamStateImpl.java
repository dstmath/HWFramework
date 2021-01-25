package com.android.server.audio;

import android.content.Context;
import android.media.AudioSystem;
import android.os.SystemProperties;
import android.provider.Settings;
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
            for (int i = STATE_TURN_ON_ALL_SOUND; i < this.mNotPersistedIndex.size(); i += STATE_TURNING_OFF_ALL_SOUND) {
                int device = this.mNotPersistedIndex.keyAt(i);
                if (this.mNotPersistedIndex.indexOfKey(device) == -1 && device != 1073741824) {
                    if (!isTurnOffAllSound() && !isMuted) {
                        this.mNotPersistedIndex.get(device);
                    }
                }
            }
        }
    }

    public boolean isTurnOffAllSound() {
        if (!this.mIsFuncTurnOffAllSoundValidate || this.mContext == null) {
            return false;
        }
        int state = Settings.Global.getInt(this.mContext.getContentResolver(), SYSTEM_TURNOFF_ALL_SOUND, STATE_TURN_ON_ALL_SOUND);
        if (state == STATE_TURNING_OFF_ALL_SOUND || state == STATE_TURN_OFF_ALL_SOUND) {
            return true;
        }
        return false;
    }

    private boolean isDeviceRelatedStreamByUser(int streamType, int device) {
        return streamType == 8 && device == STATE_TURNING_OFF_ALL_SOUND;
    }

    private int getIndex(int device, SparseIntArray indexMap) {
        int index = indexMap.get(device, -1);
        if (index == -1) {
            return indexMap.get(1073741824);
        }
        return index;
    }
}
