package ohos.aafwk.utils.dfx.hiview;

import huawei.hiview.HiEvent;
import huawei.hiview.HiView;

public class AbilityHiviewWrapper {
    public static final int ABILITY_DISPATCH_BACK_NOT_PAGE = 1;
    public static final int ABILITY_DISPATCH_BACK_TOPSLICE_NOT_INIT = 2;
    public static final int ABILITY_ERROR_TOP_SILICE_NOT_INIT = 1;
    public static final int ABILITY_WINDOW_CREATE_FAILED = 1;
    public static final int ABILITY_WINDOW_LOAD_FAILED = 2;
    public static final int ABILITY_WINDOW_SHOW_FAILED = 3;
    public static final int EVENT_ID_ABILITY_ERROR = 951000002;
    public static final int EVENT_ID_ZFRAMEWORK_DISPATCH_EVENT_FAILED = 951000012;
    public static final int EVENT_ID_ZFRAMEWORK_WINDOW_OP_FAILED = 951000011;
    private static final String HIEVENT_KEY_ABILITY_NAME = "ABILITY_NAME";
    private static final String HIEVENT_KEY_ERROR_TYPE = "ERROR_TYPE";
    private static final String HIEVENT_KEY_PACKAGE_NAME = "PACKAGE_NAME";

    public static void sendEvent(EventInfo eventInfo) {
        if (eventInfo != null) {
            HiEvent hiEvent = new HiEvent(eventInfo.getEventId());
            if (eventInfo.getBundleName() != null) {
                hiEvent.putString(HIEVENT_KEY_PACKAGE_NAME, eventInfo.getBundleName());
            }
            if (eventInfo.getAbilityName() != null) {
                hiEvent.putString(HIEVENT_KEY_ABILITY_NAME, eventInfo.getAbilityName());
            }
            hiEvent.putInt(HIEVENT_KEY_ERROR_TYPE, eventInfo.getErrorType());
            HiView.report(hiEvent);
        }
    }
}
