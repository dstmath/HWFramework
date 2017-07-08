package com.android.server.hdmi;

import android.util.SparseArray;
import com.android.server.display.RampAnimator;

public final class HdmiCecStandbyModeHandler {
    private final CecMessageHandler mAborterIncorrectMode;
    private final CecMessageHandler mAborterRefused;
    private final CecMessageHandler mAutoOnHandler;
    private final CecMessageHandler mBypasser;
    private final CecMessageHandler mBystander;
    private final SparseArray<CecMessageHandler> mCecMessageHandlers;
    private final CecMessageHandler mDefaultHandler;
    private final HdmiControlService mService;
    private final HdmiCecLocalDeviceTv mTv;
    private final UserControlProcessedHandler mUserControlProcessedHandler;

    private interface CecMessageHandler {
        boolean handle(HdmiCecMessage hdmiCecMessage);
    }

    private final class Aborter implements CecMessageHandler {
        private final int mReason;

        public Aborter(int reason) {
            this.mReason = reason;
        }

        public boolean handle(HdmiCecMessage message) {
            HdmiCecStandbyModeHandler.this.mService.maySendFeatureAbortCommand(message, this.mReason);
            return true;
        }
    }

    private final class AutoOnHandler implements CecMessageHandler {
        private AutoOnHandler() {
        }

        public boolean handle(HdmiCecMessage message) {
            if (HdmiCecStandbyModeHandler.this.mTv.getAutoWakeup()) {
                return false;
            }
            HdmiCecStandbyModeHandler.this.mAborterRefused.handle(message);
            return true;
        }
    }

    private static final class Bypasser implements CecMessageHandler {
        private Bypasser() {
        }

        public boolean handle(HdmiCecMessage message) {
            return false;
        }
    }

    private static final class Bystander implements CecMessageHandler {
        private Bystander() {
        }

        public boolean handle(HdmiCecMessage message) {
            return true;
        }
    }

    private final class UserControlProcessedHandler implements CecMessageHandler {
        private UserControlProcessedHandler() {
        }

        public boolean handle(HdmiCecMessage message) {
            if (HdmiCecLocalDevice.isPowerOnOrToggleCommand(message)) {
                return false;
            }
            if (HdmiCecLocalDevice.isPowerOffOrToggleCommand(message)) {
                return true;
            }
            return HdmiCecStandbyModeHandler.this.mAborterIncorrectMode.handle(message);
        }
    }

    public HdmiCecStandbyModeHandler(HdmiControlService service, HdmiCecLocalDeviceTv tv) {
        this.mCecMessageHandlers = new SparseArray();
        this.mDefaultHandler = new Aborter(0);
        this.mAborterIncorrectMode = new Aborter(1);
        this.mAborterRefused = new Aborter(4);
        this.mAutoOnHandler = new AutoOnHandler();
        this.mBypasser = new Bypasser();
        this.mBystander = new Bystander();
        this.mUserControlProcessedHandler = new UserControlProcessedHandler();
        this.mService = service;
        this.mTv = tv;
        addHandler(4, this.mAutoOnHandler);
        addHandler(13, this.mAutoOnHandler);
        addHandler(130, this.mBystander);
        addHandler(133, this.mBystander);
        addHandler(DumpState.DUMP_PACKAGES, this.mBystander);
        addHandler(129, this.mBystander);
        addHandler(134, this.mBystander);
        addHandler(54, this.mBystander);
        addHandler(50, this.mBystander);
        addHandler(135, this.mBystander);
        addHandler(69, this.mBystander);
        addHandler(HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_REVERBERATION, this.mBystander);
        addHandler(0, this.mBystander);
        addHandler(157, this.mBystander);
        addHandler(126, this.mBystander);
        addHandler(122, this.mBystander);
        addHandler(10, this.mBystander);
        addHandler(15, this.mAborterIncorrectMode);
        addHandler(192, this.mAborterIncorrectMode);
        addHandler(197, this.mAborterIncorrectMode);
        addHandler(131, this.mBypasser);
        addHandler(HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2, this.mBypasser);
        addHandler(132, this.mBypasser);
        addHandler(140, this.mBypasser);
        addHandler(70, this.mBypasser);
        addHandler(71, this.mBypasser);
        addHandler(68, this.mUserControlProcessedHandler);
        addHandler(143, this.mBypasser);
        addHandler(RampAnimator.DEFAULT_MAX_BRIGHTNESS, this.mBypasser);
        addHandler(159, this.mBypasser);
        addHandler(HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER, this.mAborterIncorrectMode);
        addHandler(HdmiCecKeycode.CEC_KEYCODE_F2_RED, this.mAborterIncorrectMode);
    }

    private void addHandler(int opcode, CecMessageHandler handler) {
        this.mCecMessageHandlers.put(opcode, handler);
    }

    boolean handleCommand(HdmiCecMessage message) {
        CecMessageHandler handler = (CecMessageHandler) this.mCecMessageHandlers.get(message.getOpcode());
        if (handler != null) {
            return handler.handle(message);
        }
        return this.mDefaultHandler.handle(message);
    }
}
