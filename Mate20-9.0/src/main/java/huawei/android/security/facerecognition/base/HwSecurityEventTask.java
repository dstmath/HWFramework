package huawei.android.security.facerecognition.base;

import huawei.android.security.facerecognition.FaceRecognizeEvent;

public class HwSecurityEventTask extends HwSecurityTaskBase {
    private FaceRecognizeEvent mEvent;

    public HwSecurityEventTask(FaceRecognizeEvent ev) {
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
