package com.android.server.audio;

import android.content.ContentResolver;
import android.media.AudioSystem;
import android.media.IAudioService.Stub;
import android.media.SoundPool;
import android.os.Message;
import android.util.Log;

public abstract class AbsAudioService extends Stub {
    private static final String AUDIO_CAPABILITY_LVM = null;
    protected static final boolean DTS_SOUND_EFFECTS_SUPPORT = false;
    protected static final boolean DUAL_SMARTPA_SUPPORT = false;
    protected static final boolean HPX_EFFECTS_SUPPORT = false;
    protected static final String HW_AUDIO_EXCEPTION_CALLER = "HwSystemExceptionCaller";
    protected static final int HW_DOBBLY_SOUND_EFFECT_BIT = 4;
    protected static final int HW_HPX_SOUND_EFFECT_BIT = 8;
    protected static final int HW_KARAOKE_EFFECT_BIT = 2;
    protected static final boolean HW_KARAOKE_EFFECT_ENABLED = false;
    protected static final int HW_SOUND_TRIGGER_BIT = 1;
    protected static final boolean HW_SOUND_TRIGGER_SUPPORT = false;
    protected static final int HW_TBD_BIT_4 = 16;
    protected static final int HW_TBD_BIT_5 = 32;
    protected static final int HW_TBD_BIT_6 = 64;
    protected static final int HW_TBD_BIT_7 = 128;
    protected static final boolean LOUD_VOICE_MODE_SUPPORT = false;
    protected static final int MSG_CHECK_LVM_CHANGED = 10001;
    protected static final int MSG_SOUND_EFFECTS_OPEN_CLOSE = 10002;
    protected static final boolean SOUND_EFFECTS_SUPPORT = false;
    protected static final boolean SPK_RCV_STEREO_SUPPORT = false;
    protected static final int STATE_LVM_CANCEL = 0;
    protected static final int STATE_LVM_CHANGE = 1;
    protected static final boolean SWS_SOUND_EFFECTS_SUPPORT = false;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.AbsAudioService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.audio.AbsAudioService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AbsAudioService.<clinit>():void");
    }

    protected void sendDeviceConnectionIntentForImcs(int device, int state, String name) {
    }

    private static boolean isSupportWakeUpV2() {
        String wakeupV2 = AudioSystem.getParameters("audio_capability=soundtrigger_version");
        Log.i(TAG, "wakeupV2" + wakeupV2);
        if ("".equals(wakeupV2)) {
            return SWS_SOUND_EFFECTS_SUPPORT;
        }
        return true;
    }

    public void initHwThemeHandler() {
    }

    public int getSampleId(SoundPool soundpool, int effect, String defFilePath, int index) {
        return STATE_LVM_CANCEL;
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
        return SWS_SOUND_EFFECTS_SUPPORT;
    }

    protected int getHwSafeMediaVolumeIndex() {
        return STATE_LVM_CANCEL;
    }

    protected boolean isHwSafeMediaVolumeEnabled() {
        return SWS_SOUND_EFFECTS_SUPPORT;
    }

    protected boolean checkMMIRunning() {
        return SWS_SOUND_EFFECTS_SUPPORT;
    }

    protected void handleLVMModeChangeProcess(int state, Object object) {
        Log.w(TAG, "dummy: handleLVMModeChangeProcess");
    }

    protected int getOldInCallDevice(int mode) {
        Log.w(TAG, "dummy: getOldInCallDevice");
        return STATE_LVM_CANCEL;
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
        return STATE_LVM_CANCEL;
    }
}
