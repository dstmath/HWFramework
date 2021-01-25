package ohos.workschedulerservice.controller;

import android.os.SystemClock;
import com.huawei.ohos.workscheduleradapter.WorkSchedulerCommon;
import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.bundle.CommonEventInfo;
import ohos.bundle.ElementName;

public final class CommonEventStatus {
    private static final int DEFAULT_TRIGGER_TIME = -1;
    private final String abilityName;
    private final String bundleName;
    private final List<String> data;
    private final ElementName elementName;
    private final String event;
    private Intent intent;
    private long lastTriggerTime;
    private final String permissions;
    private final List<String> type;
    private final int uid;
    private final int userID = WorkSchedulerCommon.getUserIdFromUid(this.uid);

    public CommonEventStatus(CommonEventInfo commonEventInfo, String str) {
        this.uid = commonEventInfo.getUid();
        this.bundleName = commonEventInfo.getBundleName();
        this.abilityName = commonEventInfo.getClassName();
        this.permissions = commonEventInfo.getPermission();
        this.event = str;
        this.data = commonEventInfo.getData();
        this.type = commonEventInfo.getType();
        this.elementName = new ElementName("", this.bundleName, this.abilityName);
        this.lastTriggerTime = -1;
    }

    public void updateTriggerTime() {
        this.lastTriggerTime = SystemClock.elapsedRealtime();
    }

    public long getTriggerTime() {
        return this.lastTriggerTime;
    }

    public void setIntent(Intent intent2) {
        this.intent = intent2;
    }

    public Intent getIntent() {
        return this.intent;
    }

    public int getRegistUid() {
        return this.uid;
    }

    public int getRegistUserId() {
        return this.userID;
    }

    public List<String> getEventData() {
        return this.data;
    }

    public List<String> getEventType() {
        return this.type;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getAbilityName() {
        return this.abilityName;
    }

    public ElementName getElementName() {
        return this.elementName;
    }

    public String getEventName() {
        return this.event;
    }

    public String getEventPermission() {
        return this.permissions;
    }

    public boolean isSameStatus(CommonEventStatus commonEventStatus) {
        return commonEventStatus.getRegistUid() == this.uid && this.abilityName.equals(commonEventStatus.getAbilityName()) && this.event.equals(commonEventStatus.getEventName()) && this.permissions.equals(commonEventStatus.getEventPermission());
    }
}
