package com.android.server.audio;

import android.content.ContentResolver;
import android.media.AudioSystem;
import android.media.IAudioService.Stub;
import android.media.SoundPool;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

public abstract class AbsAudioService extends Stub {
    private static final String AUDIO_CAPABILITY_LVM = AudioSystem.getParameters("audio_capability#lvm_support");
    protected static final boolean AUDIO_CHANNEL_SWITCH = "true".equals(AudioSystem.getParameters("audio_channel_switch"));
    protected static final boolean DTS_SOUND_EFFECTS_SUPPORT = SystemProperties.getBoolean("ro.config.hw_dts", false);
    protected static final boolean DUAL_SMARTPA_SUPPORT = "true".equals(AudioSystem.getParameters("audio_capability#dual_smartpa_support"));
    protected static final String HW_AUDIO_EXCEPTION_CALLER = "HwSystemExceptionCaller";
    protected static final int HW_DOBBLY_SOUND_EFFECT_BIT = 4;
    protected static final int HW_HPX_SOUND_EFFECT_BIT = 8;
    protected static final int HW_KARAOKE_EFFECT_BIT = 2;
    protected static final boolean HW_KARAOKE_EFFECT_ENABLED;
    protected static final int HW_SOUND_TRIGGER_BIT = 1;
    protected static final boolean HW_SOUND_TRIGGER_SUPPORT;
    protected static final int HW_TBD_BIT_4 = 16;
    protected static final int HW_TBD_BIT_5 = 32;
    protected static final int HW_TBD_BIT_6 = 64;
    protected static final int HW_TBD_BIT_7 = 128;
    protected static final boolean LOUD_VOICE_MODE_SUPPORT = ("".equals(AUDIO_CAPABILITY_LVM) ? SystemProperties.getBoolean("ro.config.lvm_mode", false) : "true".equals(AUDIO_CAPABILITY_LVM));
    protected static final int MSG_CHECK_LVM_CHANGED = 10001;
    protected static final int MSG_SOUND_EFFECTS_OPEN_CLOSE = 10002;
    protected static final boolean SOUND_EFFECTS_SUPPORT;
    protected static final boolean SPK_RCV_STEREO_SUPPORT;
    protected static final int STATE_LVM_CANCEL = 0;
    protected static final int STATE_LVM_CHANGE = 1;
    protected static final boolean SWS_SOUND_EFFECTS_SUPPORT = SystemProperties.getBoolean("ro.config.hw_sws", false);
    private static final String TAG = "AbsAudioService";

    protected class DeviceVolumeState {
        public final int mDevice;
        public final int mDirection;
        public final int mOldIndex;
        public final int mstreamType;

        public DeviceVolumeState(int direction, int device, int oldIndex, int type) {
            this.mDirection = direction;
            this.mDevice = device;
            this.mOldIndex = oldIndex;
            this.mstreamType = type;
        }
    }

    static {
        boolean z = true;
        boolean z2 = false;
        boolean z3 = (isSupportWakeUpV2() || (SystemProperties.getInt("ro.config.hw_media_flags", 0) & 1) == 0) ? false : true;
        HW_SOUND_TRIGGER_SUPPORT = z3;
        if ((SystemProperties.getInt("ro.config.hw_media_flags", 0) & 2) != 0) {
            z3 = true;
        } else {
            z3 = false;
        }
        HW_KARAOKE_EFFECT_ENABLED = z3;
        if (!DTS_SOUND_EFFECTS_SUPPORT) {
            z = SWS_SOUND_EFFECTS_SUPPORT;
        }
        SOUND_EFFECTS_SUPPORT = z;
        if (DUAL_SMARTPA_SUPPORT) {
            z2 = "true".equals(AudioSystem.getParameters("audio_capability#spk_rcv_stereo_support"));
        }
        SPK_RCV_STEREO_SUPPORT = z2;
    }

    protected void sendDeviceConnectionIntentForImcs(int device, int state, String name) {
    }

    private static boolean isSupportWakeUpV2() {
        String wakeupV2 = AudioSystem.getParameters("audio_capability#soundtrigger_version");
        Log.i(TAG, "wakeupV2" + wakeupV2);
        if ("".equals(wakeupV2)) {
            return false;
        }
        return true;
    }

    public void initHwThemeHandler() {
    }

    public int getSampleId(SoundPool soundpool, int effect, String defFilePath, int index) {
        return 0;
    }

    public void unloadHwThemeSoundEffects() {
    }

    protected void readPersistedSettingsEx(ContentResolver cr) {
        Log.w(TAG, "dummy: handleMessageEx");
    }

    protected void handleMessageEx(Message msg) {
        Log.w(TAG, "dummy: handleMessageEx");
    }

    protected void onErrorCallBackEx(int error) {
        Log.w(TAG, "dummy: onErrorCallBackEx");
    }

    protected void onScoExceptionOccur(int clientPid) {
        Log.w(TAG, "dummy: onScoExceptionOccur");
    }

    protected boolean usingHwSafeMediaConfig() {
        return false;
    }

    protected int getHwSafeMediaVolumeIndex() {
        return 0;
    }

    protected boolean isHwSafeMediaVolumeEnabled() {
        return false;
    }

    protected boolean checkMMIRunning() {
        return false;
    }

    protected void handleLVMModeChangeProcess(int state, Object object) {
        Log.w(TAG, "dummy: handleLVMModeChangeProcess");
    }

    protected int getOldInCallDevice(int mode) {
        Log.w(TAG, "dummy: getOldInCallDevice");
        return 0;
    }

    protected void setLVMMode(int direction, int device, int oldIndex, int streamType) {
        Log.w(TAG, "dummy: setLVMMode");
    }

    protected void getSoundTriggerSettings(ContentResolver cr) {
        Log.w(TAG, "dummy: getSoundTriggerSettings");
    }

    protected boolean checkAudioSettingAllowed(String msg) {
        Log.w(TAG, "dummy: checkAudioSettingAllowed");
        return true;
    }

    public void sendAudioRecordStateChangedIntent(String sender, int state, int pid, String packageName) {
        Log.w(TAG, "dummy: sendAudioRecordStateChangedIntent");
    }

    protected void getEffectsState(ContentResolver contentResolver) {
        Log.w(TAG, "dummy: getEffectsState");
    }

    protected void onSetSoundEffectState(int device, int state) {
        Log.w(TAG, "dummy: onSetSoundEffectState");
    }

    public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        Log.w(TAG, "dummy: setSoundEffectState");
        return 0;
    }

    protected void updateAftPolicy() {
        Log.w(TAG, "dummy: updateAftPolicy");
    }
}
