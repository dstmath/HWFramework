package com.android.server.hdmi;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.hdmi.HdmiCecLocalDeviceAudioSystem;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.power.IHwShutdownThread;

public class SystemAudioInitiationActionFromAvr extends HdmiCecFeatureAction {
    @VisibleForTesting
    static final int MAX_RETRY_COUNT = 5;
    private static final int STATE_WAITING_FOR_ACTIVE_SOURCE = 1;
    private static final int STATE_WAITING_FOR_TV_SUPPORT = 2;
    private int mSendRequestActiveSourceRetryCount = 0;
    private int mSendSetSystemAudioModeRetryCount = 0;

    SystemAudioInitiationActionFromAvr(HdmiCecLocalDevice source) {
        super(source);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        if (audioSystem().getActiveSource().physicalAddress == 65535) {
            this.mState = 1;
            addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
            sendRequestActiveSource();
        } else {
            queryTvSystemAudioModeSupport();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (cmd.getOpcode() != 130 || this.mState != 1) {
            return false;
        }
        this.mActionTimer.clearTimerMessage();
        audioSystem().handleActiveSource(cmd);
        this.mState = 2;
        queryTvSystemAudioModeSupport();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        if (this.mState == state && this.mState == 1) {
            handleActiveSourceTimeout();
        }
    }

    /* access modifiers changed from: protected */
    public void sendRequestActiveSource() {
        sendCommand(HdmiCecMessageBuilder.buildRequestActiveSource(getSourceAddress()), new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.$$Lambda$SystemAudioInitiationActionFromAvr$f4MXnpVbndKtwah7RVztUtj3RoU */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
            public final void onSendCompleted(int i) {
                SystemAudioInitiationActionFromAvr.this.lambda$sendRequestActiveSource$0$SystemAudioInitiationActionFromAvr(i);
            }
        });
    }

    public /* synthetic */ void lambda$sendRequestActiveSource$0$SystemAudioInitiationActionFromAvr(int result) {
        if (result != 0) {
            int i = this.mSendRequestActiveSourceRetryCount;
            if (i < 5) {
                this.mSendRequestActiveSourceRetryCount = i + 1;
                sendRequestActiveSource();
                return;
            }
            audioSystem().checkSupportAndSetSystemAudioMode(false);
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void sendSetSystemAudioMode(boolean on, int dest) {
        sendCommand(HdmiCecMessageBuilder.buildSetSystemAudioMode(getSourceAddress(), dest, on), new HdmiControlService.SendMessageCallback(on, dest) {
            /* class com.android.server.hdmi.$$Lambda$SystemAudioInitiationActionFromAvr$aPH0zHEfcwbPVrfqva9MSL3cLbI */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
            public final void onSendCompleted(int i) {
                SystemAudioInitiationActionFromAvr.this.lambda$sendSetSystemAudioMode$1$SystemAudioInitiationActionFromAvr(this.f$1, this.f$2, i);
            }
        });
    }

    public /* synthetic */ void lambda$sendSetSystemAudioMode$1$SystemAudioInitiationActionFromAvr(boolean on, int dest, int result) {
        if (result != 0) {
            int i = this.mSendSetSystemAudioModeRetryCount;
            if (i < 5) {
                this.mSendSetSystemAudioModeRetryCount = i + 1;
                sendSetSystemAudioMode(on, dest);
                return;
            }
            audioSystem().checkSupportAndSetSystemAudioMode(false);
            finish();
        }
    }

    private void handleActiveSourceTimeout() {
        HdmiLogger.debug("Cannot get active source.", new Object[0]);
        if (audioSystem().mService.isPlaybackDevice()) {
            audioSystem().mService.setAndBroadcastActiveSourceFromOneDeviceType(15, getSourcePath());
            this.mState = 2;
            queryTvSystemAudioModeSupport();
        } else {
            audioSystem().checkSupportAndSetSystemAudioMode(false);
        }
        finish();
    }

    private void queryTvSystemAudioModeSupport() {
        audioSystem().queryTvSystemAudioModeSupport(new HdmiCecLocalDeviceAudioSystem.TvSystemAudioModeSupportedCallback() {
            /* class com.android.server.hdmi.$$Lambda$SystemAudioInitiationActionFromAvr$Kp6VigLqlvVoDJpkhSkGpu8E8NQ */

            @Override // com.android.server.hdmi.HdmiCecLocalDeviceAudioSystem.TvSystemAudioModeSupportedCallback
            public final void onResult(boolean z) {
                SystemAudioInitiationActionFromAvr.this.lambda$queryTvSystemAudioModeSupport$2$SystemAudioInitiationActionFromAvr(z);
            }
        });
    }

    public /* synthetic */ void lambda$queryTvSystemAudioModeSupport$2$SystemAudioInitiationActionFromAvr(boolean supported) {
        if (supported) {
            if (audioSystem().checkSupportAndSetSystemAudioMode(true)) {
                sendSetSystemAudioMode(true, 15);
            }
            finish();
            return;
        }
        audioSystem().checkSupportAndSetSystemAudioMode(false);
        finish();
    }

    private void switchToRelevantInputForDeviceAt(int physicalAddress) {
    }
}
