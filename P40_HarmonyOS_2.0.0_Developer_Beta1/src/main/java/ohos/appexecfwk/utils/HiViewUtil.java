package ohos.appexecfwk.utils;

import huawei.hiview.HiEvent;
import huawei.hiview.HiView;

public class HiViewUtil {
    public static final int DEFAULT_ERROR_TYPE = 0;
    public static final int DEFAULT_EXCEPTION_ID = 0;
    public static final int EVENT_ID_BMS_INTERFACE_FAILED = 951000102;
    public static final int EVENT_ID_BMS_SERVICE_FAILED = 951000101;
    private static final int EVENT_ID_CONNECT_ABILITY_FAILED = 951000002;
    public static final int EVENT_ID_DISTRIBUTED_DATAMGR_INTERFACE_FAILED = 951000104;
    public static final int EVENT_ID_DMS_CALLBACK_FAILED = 950004203;
    public static final int EVENT_ID_DMS_INTERFACE_FAILED = 950004202;
    public static final int EVENT_ID_DMS_SERVICE_FAILED = 950004201;
    public static final int EVENT_ID_GLOBAL_INTERFACE_FAILED = 951000103;
    public static final int EVENT_ID_INIT_ENVIRONMENT_FAILED = 951000105;
    private static final int EVENT_ID_LOAD_ABILITY_FAILED = 951000001;
    private static final String HIEVENT_KEY_ABILITY_NAME = "ABILITY_NAME";
    private static final String HIEVENT_KEY_CALLBACK_NAME = "CALLBACK_NAME";
    private static final String HIEVENT_KEY_CLASS_NAME = "CLASS_NAME";
    private static final String HIEVENT_KEY_ERROR_TYPE = "ERROR_TYPE";
    private static final String HIEVENT_KEY_EXCEPTION_ID = "EXCEPTION_ID";
    private static final String HIEVENT_KEY_INTERFACE_NAME = "INTERFACE_NAME";
    private static final String HIEVENT_KEY_PACKAGE_NAME = "PACKAGE_NAME";
    private static final String HIEVENT_KEY_SERVICE_ID = "SERVICE_ID";
    private static final String HIEVENT_KEY_SERVICE_NAME = "SERVICE_NAME";

    public static void sendAppEnvironmentEvent() {
        ExceptionInfo exceptionInfo = new ExceptionInfo(EVENT_ID_INIT_ENVIRONMENT_FAILED);
        exceptionInfo.setExceptionId(0);
        sendEvent(exceptionInfo);
    }

    public static void sendBmsInterfaceEvent(String str, String str2, String str3, int i) {
        ExceptionInfo exceptionInfo = new ExceptionInfo((int) EVENT_ID_BMS_INTERFACE_FAILED, 401);
        exceptionInfo.setInterfaceName(str);
        exceptionInfo.setPackageName(str2);
        exceptionInfo.setClassName(str3);
        exceptionInfo.setErrorType(i);
        sendEvent(exceptionInfo);
    }

    public static void sendBmsEvent() {
        sendEvent(new ExceptionInfo((int) EVENT_ID_BMS_SERVICE_FAILED, 401));
    }

    public static void sendDmsEvent() {
        sendEvent(new ExceptionInfo((int) EVENT_ID_DMS_SERVICE_FAILED, (int) AppConstants.DISTRIBUTED_SERVICE_ID));
    }

    public static void sendDmsInterfaceEvent(String str, int i) {
        ExceptionInfo exceptionInfo = new ExceptionInfo((int) EVENT_ID_DMS_INTERFACE_FAILED, (int) AppConstants.DISTRIBUTED_SERVICE_ID);
        exceptionInfo.setInterfaceName(str);
        exceptionInfo.setErrorType(i);
        sendEvent(exceptionInfo);
    }

    public static void sendGlobalEvent(String str, String str2, int i) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(EVENT_ID_GLOBAL_INTERFACE_FAILED);
        exceptionInfo.setServiceName(AppConstants.GLOBAL_SERVICE_NAME);
        exceptionInfo.setInterfaceName(str);
        exceptionInfo.setAbilityName(str2);
        exceptionInfo.setErrorType(i);
        sendEvent(exceptionInfo);
    }

    public static void sendDistributedDataEvent(String str, int i) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(EVENT_ID_DISTRIBUTED_DATAMGR_INTERFACE_FAILED);
        exceptionInfo.setServiceName(AppConstants.DISTRIBUTE_DATA_SERVICE_NAME);
        exceptionInfo.setInterfaceName(str);
        exceptionInfo.setErrorType(i);
        sendEvent(exceptionInfo);
    }

    public static void sendAbilityEvent(String str, String str2, int i) {
        HiEvent hiEvent = new HiEvent((int) EVENT_ID_LOAD_ABILITY_FAILED);
        if (str != null) {
            hiEvent.putString(HIEVENT_KEY_PACKAGE_NAME, str);
        }
        if (str2 != null) {
            hiEvent.putString(HIEVENT_KEY_ABILITY_NAME, str2);
        }
        hiEvent.putInt(HIEVENT_KEY_ERROR_TYPE, i);
        HiView.report(hiEvent);
    }

    public static void sendConnectAbilityEvent(String str, String str2, int i) {
        HiEvent hiEvent = new HiEvent(951000002);
        if (str != null) {
            hiEvent.putString(HIEVENT_KEY_PACKAGE_NAME, str);
        }
        if (str2 != null) {
            hiEvent.putString(HIEVENT_KEY_ABILITY_NAME, str2);
        }
        hiEvent.putInt(HIEVENT_KEY_ERROR_TYPE, i);
        HiView.report(hiEvent);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0033  */
    private static void sendEvent(ExceptionInfo exceptionInfo) {
        if (exceptionInfo == null) {
            AppLog.e("HiViewUtil::sendEvent failed, exceptionInfo is null", new Object[0]);
            return;
        }
        int eventId = exceptionInfo.getEventId();
        HiEvent hiEvent = new HiEvent(eventId);
        switch (eventId) {
            case EVENT_ID_DMS_SERVICE_FAILED /* 950004201 */:
                setServiceEventInfo(hiEvent, exceptionInfo);
                break;
            case EVENT_ID_DMS_INTERFACE_FAILED /* 950004202 */:
                setDmsInterfaceEventInfo(hiEvent, exceptionInfo);
                break;
            case EVENT_ID_DMS_CALLBACK_FAILED /* 950004203 */:
                setDmsCallbackEventInfo(hiEvent, exceptionInfo);
                break;
            default:
                switch (eventId) {
                    case EVENT_ID_BMS_INTERFACE_FAILED /* 951000102 */:
                        setBmsInterfaceEventInfo(hiEvent, exceptionInfo);
                        break;
                    case EVENT_ID_GLOBAL_INTERFACE_FAILED /* 951000103 */:
                        setGlobalInterfaceEventInfo(hiEvent, exceptionInfo);
                        break;
                    case EVENT_ID_DISTRIBUTED_DATAMGR_INTERFACE_FAILED /* 951000104 */:
                        setDistribtePathEventInfo(hiEvent, exceptionInfo);
                        break;
                    case EVENT_ID_INIT_ENVIRONMENT_FAILED /* 951000105 */:
                        setEvenironmentEventInfo(hiEvent, exceptionInfo);
                        break;
                }
        }
        HiView.report(hiEvent);
    }

    private static void setServiceEventInfo(HiEvent hiEvent, ExceptionInfo exceptionInfo) {
        hiEvent.putInt(HIEVENT_KEY_SERVICE_ID, exceptionInfo.getServiceId());
    }

    private static void setDmsInterfaceEventInfo(HiEvent hiEvent, ExceptionInfo exceptionInfo) {
        hiEvent.putInt(HIEVENT_KEY_SERVICE_ID, exceptionInfo.getServiceId());
        hiEvent.putString(HIEVENT_KEY_INTERFACE_NAME, exceptionInfo.getInterfaceName());
        hiEvent.putInt(HIEVENT_KEY_ERROR_TYPE, exceptionInfo.getErrorType());
    }

    private static void setDmsCallbackEventInfo(HiEvent hiEvent, ExceptionInfo exceptionInfo) {
        hiEvent.putInt(HIEVENT_KEY_SERVICE_ID, exceptionInfo.getServiceId());
        hiEvent.putString(HIEVENT_KEY_INTERFACE_NAME, exceptionInfo.getInterfaceName());
        hiEvent.putString(HIEVENT_KEY_CALLBACK_NAME, exceptionInfo.getCallbackName());
        hiEvent.putInt(HIEVENT_KEY_ERROR_TYPE, exceptionInfo.getErrorType());
    }

    private static void setBmsInterfaceEventInfo(HiEvent hiEvent, ExceptionInfo exceptionInfo) {
        hiEvent.putInt(HIEVENT_KEY_SERVICE_ID, exceptionInfo.getServiceId());
        hiEvent.putString(HIEVENT_KEY_INTERFACE_NAME, exceptionInfo.getInterfaceName());
        hiEvent.putString(HIEVENT_KEY_PACKAGE_NAME, exceptionInfo.getPackageName());
        hiEvent.putString(HIEVENT_KEY_CLASS_NAME, exceptionInfo.getClassName());
        hiEvent.putInt(HIEVENT_KEY_ERROR_TYPE, exceptionInfo.getErrorType());
    }

    private static void setGlobalInterfaceEventInfo(HiEvent hiEvent, ExceptionInfo exceptionInfo) {
        hiEvent.putString(HIEVENT_KEY_SERVICE_NAME, exceptionInfo.getServiceName());
        hiEvent.putString(HIEVENT_KEY_INTERFACE_NAME, exceptionInfo.getInterfaceName());
        hiEvent.putString(HIEVENT_KEY_ABILITY_NAME, exceptionInfo.getAbilityName());
        hiEvent.putInt(HIEVENT_KEY_ERROR_TYPE, exceptionInfo.getErrorType());
    }

    private static void setDistribtePathEventInfo(HiEvent hiEvent, ExceptionInfo exceptionInfo) {
        hiEvent.putString(HIEVENT_KEY_SERVICE_NAME, exceptionInfo.getServiceName());
        hiEvent.putString(HIEVENT_KEY_INTERFACE_NAME, exceptionInfo.getInterfaceName());
        hiEvent.putInt(HIEVENT_KEY_ERROR_TYPE, exceptionInfo.getErrorType());
    }

    private static void setEvenironmentEventInfo(HiEvent hiEvent, ExceptionInfo exceptionInfo) {
        hiEvent.putInt(HIEVENT_KEY_EXCEPTION_ID, exceptionInfo.getExceptionId());
    }
}
