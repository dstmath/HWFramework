package com.android.server.hdmi;

import com.android.server.power.IHwShutdownThread;

final class SystemAudioAutoInitiationAction extends HdmiCecFeatureAction {
    private static final int STATE_WAITING_FOR_SYSTEM_AUDIO_MODE_STATUS = 1;
    private final int mAvrAddress;

    SystemAudioAutoInitiationAction(HdmiCecLocalDevice source, int avrAddress) {
        super(source);
        this.mAvrAddress = avrAddress;
    }

    boolean start() {
        this.mState = STATE_WAITING_FOR_SYSTEM_AUDIO_MODE_STATUS;
        addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        sendGiveSystemAudioModeStatus();
        return true;
    }

    private void sendGiveSystemAudioModeStatus() {
        sendCommand(HdmiCecMessageBuilder.buildGiveSystemAudioModeStatus(getSourceAddress(), this.mAvrAddress), new SendMessageCallback() {
            public void onSendCompleted(int error) {
                if (error != 0) {
                    SystemAudioAutoInitiationAction.this.tv().setSystemAudioMode(false, true);
                    SystemAudioAutoInitiationAction.this.finish();
                }
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != STATE_WAITING_FOR_SYSTEM_AUDIO_MODE_STATUS || this.mAvrAddress != cmd.getSource() || cmd.getOpcode() != 126) {
            return false;
        }
        handleSystemAudioModeStatusMessage(HdmiUtils.parseCommandParamSystemAudioStatus(cmd));
        return true;
    }

    private void handleSystemAudioModeStatusMessage(boolean isSystemAudioModeOn) {
        if (canChangeSystemAudio()) {
            boolean systemAudioModeSetting = tv().getSystemAudioModeSetting();
            if (!systemAudioModeSetting || isSystemAudioModeOn) {
                tv().setSystemAudioMode(isSystemAudioModeOn, true);
            } else {
                addAndStartAction(new SystemAudioActionFromTv(tv(), this.mAvrAddress, systemAudioModeSetting, null));
            }
            finish();
            return;
        }
        HdmiLogger.debug("Cannot change system audio mode in auto initiation action.", new Object[0]);
        finish();
    }

    void handleTimerEvent(int state) {
        if (this.mState == state) {
            switch (this.mState) {
                case STATE_WAITING_FOR_SYSTEM_AUDIO_MODE_STATUS /*1*/:
                    handleSystemAudioModeStatusTimeout();
                    break;
            }
        }
    }

    private void handleSystemAudioModeStatusTimeout() {
        if (!tv().getSystemAudioModeSetting()) {
            tv().setSystemAudioMode(false, true);
        } else if (canChangeSystemAudio()) {
            addAndStartAction(new SystemAudioActionFromTv(tv(), this.mAvrAddress, true, null));
        }
        finish();
    }

    private boolean canChangeSystemAudio() {
        return (tv().hasAction(SystemAudioActionFromTv.class) || tv().hasAction(SystemAudioActionFromAvr.class)) ? false : true;
    }
}
