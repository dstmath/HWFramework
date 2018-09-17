package com.android.server.hdmi;

import android.net.util.NetworkConstants;
import android.util.SparseArray;

public final class HdmiCecStandbyModeHandler {
    private final CecMessageHandler mAborterIncorrectMode = new Aborter(1);
    private final CecMessageHandler mAborterRefused = new Aborter(4);
    private final CecMessageHandler mAutoOnHandler = new AutoOnHandler(this, null);
    private final CecMessageHandler mBypasser = new Bypasser();
    private final CecMessageHandler mBystander = new Bystander();
    private final SparseArray<CecMessageHandler> mCecMessageHandlers = new SparseArray();
    private final CecMessageHandler mDefaultHandler = new Aborter(0);
    private final HdmiControlService mService;
    private final HdmiCecLocalDeviceTv mTv;
    private final UserControlProcessedHandler mUserControlProcessedHandler = new UserControlProcessedHandler(this, null);

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
        /* synthetic */ AutoOnHandler(HdmiCecStandbyModeHandler this$0, AutoOnHandler -this1) {
            this();
        }

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
        /* synthetic */ Bypasser(Bypasser -this0) {
            this();
        }

        private Bypasser() {
        }

        public boolean handle(HdmiCecMessage message) {
            return false;
        }
    }

    private static final class Bystander implements CecMessageHandler {
        /* synthetic */ Bystander(Bystander -this0) {
            this();
        }

        private Bystander() {
        }

        public boolean handle(HdmiCecMessage message) {
            return true;
        }
    }

    private final class UserControlProcessedHandler implements CecMessageHandler {
        /* synthetic */ UserControlProcessedHandler(HdmiCecStandbyModeHandler this$0, UserControlProcessedHandler -this1) {
            this();
        }

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
        this.mService = service;
        this.mTv = tv;
        addHandler(4, this.mAutoOnHandler);
        addHandler(13, this.mAutoOnHandler);
        addHandler(130, this.mBystander);
        addHandler(NetworkConstants.ICMPV6_ROUTER_SOLICITATION, this.mBystander);
        addHandler(128, this.mBystander);
        addHandler(129, this.mBystander);
        addHandler(NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT, this.mBystander);
        addHandler(54, this.mBystander);
        addHandler(50, this.mBystander);
        addHandler(NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION, this.mBystander);
        addHandler(69, this.mBystander);
        addHandler(144, this.mBystander);
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
        addHandler(255, this.mBypasser);
        addHandler(159, this.mBypasser);
        addHandler(160, this.mAborterIncorrectMode);
        addHandler(114, this.mAborterIncorrectMode);
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
