package com.android.server.audio;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Global;

public class HwCustAudioServiceImpl extends HwCustAudioService {
    private static final int STATE_TURNING_OFF_ALL_SOUND = 1;
    private static final int STATE_TURNING_ON_ALL_SOUND = 2;
    private static final int STATE_TURN_OFF_ALL_SOUND = 3;
    private static final int STATE_TURN_ON_ALL_SOUND = 0;
    private static final String SYSTEM_TURNOFF_ALL_SOUND = "trun_off_all_sound";
    private static final String TAG = "HwCustAudioServiceImpl";
    private boolean mIsFuncTurnOffAllSoundValidate = SystemProperties.getBoolean("ro.show_turn_off_all_sound", false);

    public HwCustAudioServiceImpl(Context context) {
        super(context);
    }

    public boolean isTurningAllSound() {
        boolean z = true;
        if (!this.mIsFuncTurnOffAllSoundValidate) {
            return false;
        }
        int state = Global.getInt(this.mContext.getContentResolver(), SYSTEM_TURNOFF_ALL_SOUND, 0);
        if (!(state == 1 || state == STATE_TURNING_ON_ALL_SOUND)) {
            z = false;
        }
        return z;
    }
}
