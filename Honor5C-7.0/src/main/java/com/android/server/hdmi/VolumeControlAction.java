package com.android.server.hdmi;

import android.media.AudioManager;
import com.android.server.display.RampAnimator;
import com.android.server.wm.WindowState;

final class VolumeControlAction extends HdmiCecFeatureAction {
    private static final int MAX_VOLUME = 100;
    private static final int STATE_WAIT_FOR_NEXT_VOLUME_PRESS = 1;
    private static final String TAG = "VolumeControlAction";
    private static final int UNKNOWN_AVR_VOLUME = -1;
    private final int mAvrAddress;
    private boolean mIsVolumeUp;
    private boolean mLastAvrMute;
    private int mLastAvrVolume;
    private long mLastKeyUpdateTime;
    private boolean mSentKeyPressed;

    public static int scaleToCecVolume(int volume, int scale) {
        return (volume * MAX_VOLUME) / scale;
    }

    public static int scaleToCustomVolume(int cecVolume, int scale) {
        return (cecVolume * scale) / MAX_VOLUME;
    }

    VolumeControlAction(HdmiCecLocalDevice source, int avrAddress, boolean isVolumeUp) {
        super(source);
        this.mAvrAddress = avrAddress;
        this.mIsVolumeUp = isVolumeUp;
        this.mLastAvrVolume = UNKNOWN_AVR_VOLUME;
        this.mLastAvrMute = false;
        this.mSentKeyPressed = false;
        updateLastKeyUpdateTime();
    }

    private void updateLastKeyUpdateTime() {
        this.mLastKeyUpdateTime = System.currentTimeMillis();
    }

    boolean start() {
        this.mState = STATE_WAIT_FOR_NEXT_VOLUME_PRESS;
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
        addTimer(STATE_WAIT_FOR_NEXT_VOLUME_PRESS, 300);
    }

    void handleVolumeChange(boolean isVolumeUp) {
        if (this.mIsVolumeUp != isVolumeUp) {
            HdmiLogger.debug("Volume Key Status Changed[old:%b new:%b]", Boolean.valueOf(this.mIsVolumeUp), Boolean.valueOf(isVolumeUp));
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

    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != STATE_WAIT_FOR_NEXT_VOLUME_PRESS || cmd.getSource() != this.mAvrAddress) {
            return false;
        }
        switch (cmd.getOpcode()) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return handleFeatureAbort(cmd);
            case 122:
                return handleReportAudioStatus(cmd);
            default:
                return false;
        }
    }

    private boolean handleReportAudioStatus(HdmiCecMessage cmd) {
        byte[] params = cmd.getParams();
        boolean mute = (params[0] & DumpState.DUMP_PACKAGES) == DumpState.DUMP_PACKAGES;
        int volume = params[0] & 127;
        this.mLastAvrVolume = volume;
        this.mLastAvrMute = mute;
        if (shouldUpdateAudioVolume(mute)) {
            HdmiLogger.debug("Force volume change[mute:%b, volume=%d]", Boolean.valueOf(mute), Integer.valueOf(volume));
            tv().setAudioStatus(mute, volume);
            this.mLastAvrVolume = UNKNOWN_AVR_VOLUME;
            this.mLastAvrMute = false;
        }
        return true;
    }

    private boolean shouldUpdateAudioVolume(boolean mute) {
        boolean z = true;
        if (mute) {
            return true;
        }
        AudioManager audioManager = tv().getService().getAudioManager();
        int currentVolume = audioManager.getStreamVolume(3);
        if (this.mIsVolumeUp) {
            if (currentVolume != audioManager.getStreamMaxVolume(3)) {
                z = false;
            }
            return z;
        }
        if (currentVolume != 0) {
            z = false;
        }
        return z;
    }

    private boolean handleFeatureAbort(HdmiCecMessage cmd) {
        if ((cmd.getParams()[0] & RampAnimator.DEFAULT_MAX_BRIGHTNESS) != 68) {
            return false;
        }
        finish();
        return true;
    }

    protected void clear() {
        super.clear();
        if (this.mSentKeyPressed) {
            sendVolumeKeyReleased();
        }
        if (this.mLastAvrVolume != UNKNOWN_AVR_VOLUME) {
            tv().setAudioStatus(this.mLastAvrMute, this.mLastAvrVolume);
            this.mLastAvrVolume = UNKNOWN_AVR_VOLUME;
            this.mLastAvrMute = false;
        }
    }

    void handleTimerEvent(int state) {
        if (state == STATE_WAIT_FOR_NEXT_VOLUME_PRESS) {
            if (System.currentTimeMillis() - this.mLastKeyUpdateTime >= 300) {
                finish();
            } else {
                sendVolumeKeyPressed();
                resetTimer();
            }
        }
    }
}
