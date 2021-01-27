package ohos.aafwk.utils.dfx.hiview;

public class EventInfo {
    private String abilityName = "";
    private String bundleName = "";
    private int errorType = 0;
    private int eventId = 0;

    public int getEventId() {
        return this.eventId;
    }

    public void setEventId(int i) {
        this.eventId = i;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String str) {
        this.bundleName = str;
    }

    public String getAbilityName() {
        return this.abilityName;
    }

    public void setAbilityName(String str) {
        this.abilityName = str;
    }

    public int getErrorType() {
        return this.errorType;
    }

    public void setErrorType(int i) {
        this.errorType = i;
    }
}
