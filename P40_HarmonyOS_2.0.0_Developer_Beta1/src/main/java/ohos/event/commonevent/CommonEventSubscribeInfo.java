package ohos.event.commonevent;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class CommonEventSubscribeInfo implements Sequenceable {
    public static final Sequenceable.Producer<CommonEventSubscribeInfo> PRODUCER = $$Lambda$CommonEventSubscribeInfo$1Jfp7mF0Q9nY26CabrvJuAUyuY.INSTANCE;
    private static final int USER_CURRENT = -2;
    private MatchingSkills matchingSkills;
    private int priority;
    private String publisherDeviceId;
    private String publisherPermission;
    private ThreadMode threadMode = ThreadMode.HANDLER;
    private int userId = -2;

    public enum ThreadMode {
        HANDLER,
        POST,
        ASYNC,
        BACKGROUND
    }

    static /* synthetic */ CommonEventSubscribeInfo lambda$static$0(Parcel parcel) {
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo();
        commonEventSubscribeInfo.unmarshalling(parcel);
        return commonEventSubscribeInfo;
    }

    private CommonEventSubscribeInfo() {
    }

    public CommonEventSubscribeInfo(MatchingSkills matchingSkills2) {
        if (matchingSkills2 != null) {
            this.matchingSkills = new MatchingSkills(matchingSkills2);
        }
    }

    public CommonEventSubscribeInfo(CommonEventSubscribeInfo commonEventSubscribeInfo) {
        if (commonEventSubscribeInfo != null) {
            this.publisherPermission = commonEventSubscribeInfo.publisherPermission;
            this.publisherDeviceId = commonEventSubscribeInfo.publisherDeviceId;
            this.threadMode = commonEventSubscribeInfo.threadMode;
            this.userId = commonEventSubscribeInfo.userId;
            this.matchingSkills = commonEventSubscribeInfo.matchingSkills;
            this.priority = commonEventSubscribeInfo.priority;
        }
    }

    public MatchingSkills getMatchingSkills() {
        return this.matchingSkills;
    }

    public ThreadMode getThreadMode() {
        return this.threadMode;
    }

    public void setThreadMode(ThreadMode threadMode2) {
        if (threadMode2 != null) {
            this.threadMode = threadMode2;
        }
    }

    public String getPermission() {
        return this.publisherPermission;
    }

    public void setPermission(String str) {
        this.publisherPermission = str;
    }

    public void setDeviceId(String str) {
        this.publisherDeviceId = str;
    }

    public String getDeviceId() {
        return this.publisherDeviceId;
    }

    public void setUserId(int i) {
        this.userId = i;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setPriority(int i) {
        this.priority = i;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean marshalling(Parcel parcel) {
        return MatchingSkillsTransformation.writeToParcel(this, parcel);
    }

    public boolean unmarshalling(Parcel parcel) {
        CommonEventSubscribeInfo orElse = MatchingSkillsTransformation.readFromParcel(parcel).orElse(null);
        if (orElse == null) {
            return false;
        }
        MatchingSkills matchingSkills2 = orElse.matchingSkills;
        if (matchingSkills2 != null) {
            this.matchingSkills = matchingSkills2;
        }
        this.publisherDeviceId = orElse.publisherDeviceId;
        this.publisherPermission = orElse.publisherPermission;
        this.threadMode = orElse.threadMode;
        this.userId = orElse.userId;
        this.priority = orElse.priority;
        return true;
    }

    public String toString() {
        return "CommonEventSubscribeInfo[ publisherDeviceId = " + this.publisherDeviceId + " publisherPermission = " + this.publisherPermission + " threadMode = " + this.threadMode + " priority = " + this.priority + "]";
    }
}
