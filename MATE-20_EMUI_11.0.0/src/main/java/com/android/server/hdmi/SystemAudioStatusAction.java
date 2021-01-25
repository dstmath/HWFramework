package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.power.IHwShutdownThread;

/* access modifiers changed from: package-private */
public final class SystemAudioStatusAction extends HdmiCecFeatureAction {
    private static final int STATE_WAIT_FOR_REPORT_AUDIO_STATUS = 1;
    private static final String TAG = "SystemAudioStatusAction";
    private final int mAvrAddress;
    private final IHdmiControlCallback mCallback;

    SystemAudioStatusAction(HdmiCecLocalDevice source, int avrAddress, IHdmiControlCallback callback) {
        super(source);
        this.mAvrAddress = avrAddress;
        this.mCallback = callback;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        this.mState = 1;
        addTimer(this.mState, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
        sendGiveAudioStatus();
        return true;
    }

    private void sendGiveAudioStatus() {
        sendCommand(HdmiCecMessageBuilder.buildGiveAudioStatus(getSourceAddress(), this.mAvrAddress), new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.SystemAudioStatusAction.AnonymousClass1 */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
            public void onSendCompleted(int error) {
                if (error != 0) {
                    SystemAudioStatusAction.this.handleSendGiveAudioStatusFailure();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendGiveAudioStatusFailure() {
        tv().setAudioStatus(false, -1);
        sendUserControlPressedAndReleased(this.mAvrAddress, HdmiCecKeycode.getMuteKey(!tv().isSystemAudioActivated()));
        finishWithCallback(0);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean processCommand(HdmiCecMessage cmd) {
        if (this.mState != 1 || this.mAvrAddress != cmd.getSource() || cmd.getOpcode() != 122) {
            return false;
        }
        handleReportAudioStatus(cmd);
        return true;
    }

    private void handleReportAudioStatus(HdmiCecMessage cmd) {
        cmd.getParams();
        boolean mute = HdmiUtils.isAudioStatusMute(cmd);
        tv().setAudioStatus(mute, HdmiUtils.getAudioStatusVolume(cmd));
        if (!(tv().isSystemAudioActivated() ^ mute)) {
            sendUserControlPressedAndReleased(this.mAvrAddress, 67);
        }
        finishWithCallback(0);
    }

    private void finishWithCallback(int returnCode) {
        IHdmiControlCallback iHdmiControlCallback = this.mCallback;
        if (iHdmiControlCallback != null) {
            try {
                iHdmiControlCallback.onComplete(returnCode);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to invoke callback.", e);
            }
        }
        finish();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public void handleTimerEvent(int state) {
        if (this.mState == state) {
            handleSendGiveAudioStatusFailure();
        }
    }
}
