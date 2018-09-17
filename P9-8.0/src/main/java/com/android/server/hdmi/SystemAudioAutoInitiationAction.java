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
        this.mState = 1;
        addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        sendGiveSystemAudioModeStatus();
        return true;
    }

    private void sendGiveSystemAudioModeStatus() {
        sendCommand(HdmiCecMessageBuilder.buildGiveSystemAudioModeStatus(getSourceAddress(), this.mAvrAddress), new SendMessageCallback() {
            public void onSendCompleted(int error) {
                if (error != 0) {
                    SystemAudioAutoInitiationAction.this.tv().setSystemAudioMode(false);
                    SystemAudioAutoInitiationAction.this.finish();
                }
            }
        });
    }

    /* JADX WARNING: Missing block: B:4:0x000e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || this.mAvrAddress != cmd.getSource() || cmd.getOpcode() != 126) {
            return false;
        }
        handleSystemAudioModeStatusMessage(HdmiUtils.parseCommandParamSystemAudioStatus(cmd));
        return true;
    }

    private void handleSystemAudioModeStatusMessage(boolean currentSystemAudioMode) {
        if (canChangeSystemAudio()) {
            boolean targetSystemAudioMode = tv().isSystemAudioControlFeatureEnabled();
            if (currentSystemAudioMode != targetSystemAudioMode) {
                addAndStartAction(new SystemAudioActionFromTv(tv(), this.mAvrAddress, targetSystemAudioMode, null));
            } else {
                tv().setSystemAudioMode(targetSystemAudioMode);
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
                case 1:
                    handleSystemAudioModeStatusTimeout();
                    break;
            }
        }
    }

    private void handleSystemAudioModeStatusTimeout() {
        if (canChangeSystemAudio()) {
            addAndStartAction(new SystemAudioActionFromTv(tv(), this.mAvrAddress, tv().isSystemAudioControlFeatureEnabled(), null));
            finish();
            return;
        }
        HdmiLogger.debug("Cannot change system audio mode in auto initiation action.", new Object[0]);
        finish();
    }

    private boolean canChangeSystemAudio() {
        int i;
        if (tv().hasAction(SystemAudioActionFromTv.class)) {
            i = 1;
        } else {
            i = tv().hasAction(SystemAudioActionFromAvr.class);
        }
        return i ^ 1;
    }
}
