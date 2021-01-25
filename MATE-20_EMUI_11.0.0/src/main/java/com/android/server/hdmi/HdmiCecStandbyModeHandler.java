package com.android.server.hdmi;

import android.util.SparseArray;
import com.android.server.display.FoldPolicy;

public final class HdmiCecStandbyModeHandler {
    private final CecMessageHandler mAborterIncorrectMode = new Aborter(1);
    private final CecMessageHandler mAborterRefused = new Aborter(4);
    private final CecMessageHandler mAutoOnHandler = new AutoOnHandler();
    private final CecMessageHandler mBypasser = new Bypasser();
    private final CecMessageHandler mBystander = new Bystander();
    private final SparseArray<CecMessageHandler> mCecMessageHandlers = new SparseArray<>();
    private final CecMessageHandler mDefaultHandler = new Aborter(0);
    private final HdmiControlService mService;
    private final HdmiCecLocalDeviceTv mTv;
    private final UserControlProcessedHandler mUserControlProcessedHandler = new UserControlProcessedHandler();

    /* access modifiers changed from: private */
    public interface CecMessageHandler {
        boolean handle(HdmiCecMessage hdmiCecMessage);
    }

    private static final class Bystander implements CecMessageHandler {
        private Bystander() {
        }

        @Override // com.android.server.hdmi.HdmiCecStandbyModeHandler.CecMessageHandler
        public boolean handle(HdmiCecMessage message) {
            return true;
        }
    }

    private static final class Bypasser implements CecMessageHandler {
        private Bypasser() {
        }

        @Override // com.android.server.hdmi.HdmiCecStandbyModeHandler.CecMessageHandler
        public boolean handle(HdmiCecMessage message) {
            return false;
        }
    }

    private final class Aborter implements CecMessageHandler {
        private final int mReason;

        public Aborter(int reason) {
            this.mReason = reason;
        }

        @Override // com.android.server.hdmi.HdmiCecStandbyModeHandler.CecMessageHandler
        public boolean handle(HdmiCecMessage message) {
            HdmiCecStandbyModeHandler.this.mService.maySendFeatureAbortCommand(message, this.mReason);
            return true;
        }
    }

    private final class AutoOnHandler implements CecMessageHandler {
        private AutoOnHandler() {
        }

        @Override // com.android.server.hdmi.HdmiCecStandbyModeHandler.CecMessageHandler
        public boolean handle(HdmiCecMessage message) {
            if (HdmiCecStandbyModeHandler.this.mTv.getAutoWakeup()) {
                return false;
            }
            HdmiCecStandbyModeHandler.this.mAborterRefused.handle(message);
            return true;
        }
    }

    private final class UserControlProcessedHandler implements CecMessageHandler {
        private UserControlProcessedHandler() {
        }

        @Override // com.android.server.hdmi.HdmiCecStandbyModeHandler.CecMessageHandler
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
        this.mService = service;
        this.mTv = tv;
        addHandler(4, this.mAutoOnHandler);
        addHandler(13, this.mAutoOnHandler);
        addHandler(130, this.mBystander);
        addHandler(133, this.mBystander);
        addHandler(128, this.mBystander);
        addHandler(129, this.mBystander);
        addHandler(134, this.mBystander);
        addHandler(54, this.mBystander);
        addHandler(50, this.mBystander);
        addHandler(135, this.mBystander);
        addHandler(69, this.mBystander);
        addHandler(144, this.mBystander);
        addHandler(0, this.mBystander);
        addHandler(157, this.mBystander);
        addHandler(126, this.mBystander);
        addHandler(FoldPolicy.NAV_BAR_HEIGHT, this.mBystander);
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
        addHandler(255, this.mBypasser);
        addHandler(159, this.mBypasser);
        addHandler(160, this.mAborterIncorrectMode);
        addHandler(HdmiCecKeycode.CEC_KEYCODE_F2_RED, this.mAborterIncorrectMode);
    }

    private void addHandler(int opcode, CecMessageHandler handler) {
        this.mCecMessageHandlers.put(opcode, handler);
    }

    /* access modifiers changed from: package-private */
    public boolean handleCommand(HdmiCecMessage message) {
        CecMessageHandler handler = this.mCecMessageHandlers.get(message.getOpcode());
        if (handler != null) {
            return handler.handle(message);
        }
        return this.mDefaultHandler.handle(message);
    }
}
