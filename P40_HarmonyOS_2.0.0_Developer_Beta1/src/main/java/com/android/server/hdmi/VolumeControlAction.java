package com.android.server.hdmi;

import android.media.AudioManager;
import com.android.server.display.color.DisplayTransformManager;

/* access modifiers changed from: package-private */
public final class VolumeControlAction extends HdmiCecFeatureAction {
    private static final int MAX_VOLUME = 100;
    private static final int STATE_WAIT_FOR_NEXT_VOLUME_PRESS = 1;
    private static final String TAG = "VolumeControlAction";
    private static final int UNKNOWN_AVR_VOLUME = -1;
    private final int mAvrAddress;
    private boolean mIsVolumeUp;
    private boolean mLastAvrMute = false;
    private int mLastAvrVolume = -1;
    private long mLastKeyUpdateTime;
    private boolean mSentKeyPressed = false;

    public static int scaleToCecVolume(int volume, int scale) {
        return (volume * 100) / scale;
    }

    public static int scaleToCustomVolume(int cecVolume, int scale) {
        return (cecVolume * scale) / 100;
    }

    VolumeControlAction(HdmiCecLocalDevice source, int avrAddress, boolean isVolumeUp) {
        super(source);
        this.mAvrAddress = avrAddress;
        this.mIsVolumeUp = isVolumeUp;
        updateLastKeyUpdateTime();
    }

    private void updateLastKeyUpdateTime() {
        this.mLastKeyUpdateTime = System.currentTimeMillis();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        this.mState = 1;
        sendVolumeKeyPressed();
        resetTimer();
        return true;
    }

    private void sendVolumeKeyPressed() {
        int i;
        int sourceAddress = getSourceAddress();
        int i2 = this.mAvrAddress;
        if (this.mIsVolumeUp) {
            i = 65;
        } else {
            i = 66;
        }
        sendCommand(HdmiCecMessageBuilder.buildUserControlPressed(sourceAddress, i2, i));
        this.mSentKeyPressed = true;
    }

    private void resetTimer() {
        this.mActionTimer.clearTimerMessage();
        addTimer(1, DisplayTransformManager.LEVEL_COLOR_MATRIX_INVERT_COLOR);
    }

    /* access modifiers changed from: package-private */
    public void handleVolumeChange(boolean isVolumeUp) {
        boolean z = this.mIsVolumeUp;
        if (z != isVolumeUp) {
            HdmiLogger.debug("Volume Key Status Changed[old:%b new:%b]", Boolean.valueOf(z), Boolean.valueOf(isVolumeUp));
            sendVolumeKeyReleased();
            this.mIsVolumeUp = isVolumeUp;
            sendVolumeKeyPressed();
            resetTimer();
        }
        updateLastKeyUpdateTime();
    }

    private void sendVolumeKeyReleased() {
        sendCommand(HdmiCecMessageBuilder.buildUserControlReleased(getSourceAddress(), this.mAvrAddress));
        this.mSentKeyPressed = false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || cmd.getSource() != this.mAvrAddress) {
            return false;
        }
        int opcode = cmd.getOpcode();
        if (opcode == 0) {
            return handleFeatureAbort(cmd);
        }
        if (opcode != 122) {
            return false;
        }
        return handleReportAudioStatus(cmd);
    }

    private boolean handleReportAudioStatus(HdmiCecMessage cmd) {
        cmd.getParams();
        boolean mute = HdmiUtils.isAudioStatusMute(cmd);
        int volume = HdmiUtils.getAudioStatusVolume(cmd);
        this.mLastAvrVolume = volume;
        this.mLastAvrMute = mute;
        if (shouldUpdateAudioVolume(mute)) {
            HdmiLogger.debug("Force volume change[mute:%b, volume=%d]", Boolean.valueOf(mute), Integer.valueOf(volume));
            tv().setAudioStatus(mute, volume);
            this.mLastAvrVolume = -1;
            this.mLastAvrMute = false;
        }
        return true;
    }

    private boolean shouldUpdateAudioVolume(boolean mute) {
        if (mute) {
            return true;
        }
        AudioManager audioManager = tv().getService().getAudioManager();
        int currentVolume = audioManager.getStreamVolume(3);
        if (this.mIsVolumeUp) {
            if (currentVolume == audioManager.getStreamMaxVolume(3)) {
                return true;
            }
            return false;
        } else if (currentVolume == 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean handleFeatureAbort(HdmiCecMessage cmd) {
        if ((cmd.getParams()[0] & 255) != 68) {
            return false;
        }
        finish();
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void clear() {
        super.clear();
        if (this.mSentKeyPressed) {
            sendVolumeKeyReleased();
        }
        if (this.mLastAvrVolume != -1) {
            tv().setAudioStatus(this.mLastAvrMute, this.mLastAvrVolume);
            this.mLastAvrVolume = -1;
            this.mLastAvrMute = false;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        if (state == 1) {
            if (System.currentTimeMillis() - this.mLastKeyUpdateTime >= 300) {
                finish();
                return;
            }
            sendVolumeKeyPressed();
            resetTimer();
        }
    }
}
