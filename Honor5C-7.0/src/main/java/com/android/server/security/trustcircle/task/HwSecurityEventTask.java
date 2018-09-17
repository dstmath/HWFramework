package com.android.server.security.trustcircle.task;

public class HwSecurityEventTask extends HwSecurityTaskBase {
    private HwSecurityEvent mEvent;

    public HwSecurityEventTask(HwSecurityEvent ev) {
        super(null, null);
        this.mEvent = ev;
    }

    public int doAction() {
        HwSecurityMsgCenter msgCenter = HwSecurityMsgCenter.getInstance();
        if (msgCenter != null) {
            msgCenter.processEvent(this.mEvent);
        }
        return 0;
    }
}
