package ohos.ace.ability;

import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceApplicationInfo;
import com.huawei.ace.runtime.EventInfo;
import com.huawei.ace.runtime.IEventReport;
import huawei.hiview.HiEvent;
import huawei.hiview.HiView;

public class EventReport implements IEventReport {
    private static final String EVENT_KEY_ERROR_TYPE = "ERROR_TYPE";
    private static final String EVENT_KEY_PACKAGE_NAME = "PACKAGE_NAME";
    private static final String EVENT_KEY_PID = "PID";
    public static final int EXCEPTION_COMPONENT = 951004002;
    public static final int EXCEPTION_FRAMEWORK_APP_START = 951004000;
    public static final int EXCEPTION_RENDER = 951004004;
    private static final String LOG_TAG = "EventReport";
    private static final int MAX_PACKAGE_NAME_LENGTH = 128;

    private void sendEvent(EventInfo eventInfo) {
        if (eventInfo == null) {
            ALog.e(LOG_TAG, "EventReport::sendEvent failed, EventInfo is null");
            return;
        }
        int eventType = eventInfo.getEventType();
        HiEvent hiEvent = new HiEvent(eventType);
        hiEvent.putInt(EVENT_KEY_PID, AceApplicationInfo.getInstance().getPid());
        String packageName = AceApplicationInfo.getInstance().getPackageName();
        if (packageName.length() > 128) {
            packageName = packageName.substring(packageName.length() - 128);
        }
        hiEvent.putString(EVENT_KEY_PACKAGE_NAME, packageName);
        if (eventType == 951004000) {
            setFrameworkAppStartEvent(hiEvent, eventInfo);
        } else if (eventType == 951004002) {
            setComponentEvent(hiEvent, eventInfo);
        } else if (eventType == 951004004) {
            setRenderEvent(hiEvent, eventInfo);
        }
        HiView.report(hiEvent);
    }

    @Override // com.huawei.ace.runtime.IEventReport
    public void sendFrameworkAppStartEvent(int i) {
        EventInfo eventInfo = new EventInfo(EXCEPTION_FRAMEWORK_APP_START);
        eventInfo.setErrType(i);
        sendEvent(eventInfo);
    }

    @Override // com.huawei.ace.runtime.IEventReport
    public void sendComponentEvent(int i) {
        EventInfo eventInfo = new EventInfo(EXCEPTION_COMPONENT);
        eventInfo.setErrType(i);
        sendEvent(eventInfo);
    }

    @Override // com.huawei.ace.runtime.IEventReport
    public void sendRenderEvent(int i) {
        EventInfo eventInfo = new EventInfo(EXCEPTION_RENDER);
        eventInfo.setErrType(i);
        sendEvent(eventInfo);
    }

    private void setFrameworkAppStartEvent(HiEvent hiEvent, EventInfo eventInfo) {
        hiEvent.putInt(EVENT_KEY_ERROR_TYPE, eventInfo.getErrType());
    }

    private void setComponentEvent(HiEvent hiEvent, EventInfo eventInfo) {
        hiEvent.putInt(EVENT_KEY_ERROR_TYPE, eventInfo.getErrType());
    }

    private void setRenderEvent(HiEvent hiEvent, EventInfo eventInfo) {
        hiEvent.putInt(EVENT_KEY_ERROR_TYPE, eventInfo.getErrType());
    }
}
