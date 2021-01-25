package ohos.ace.featureabilityplugin.requestparse;

import java.util.List;

public class ParsedJsRequest {
    private String abilityName;
    private int abilityType;
    private String action;
    private String bundleName;
    private String deviceId;
    private List<String> entities;
    private int finishAbilityResultCode;
    private String finishAbilityResultData;
    private int flag;
    private boolean intentType = true;
    private int messageCode;
    private String parseErrorMessage;
    private String requestData;
    private int startAbilityDeviceType;
    private boolean syncOption;

    public String getBundleName() {
        return this.bundleName;
    }

    public String getAbilityName() {
        return this.abilityName;
    }

    public int getMessageCode() {
        return this.messageCode;
    }

    public int getAbilityType() {
        return this.abilityType;
    }

    public boolean getSyncOption() {
        return this.syncOption;
    }

    public String getRequestData() {
        return this.requestData;
    }

    public String getBundleAndAbilityName() {
        return this.bundleName + "." + this.abilityName;
    }

    public String getParseErrorMessage() {
        return this.parseErrorMessage;
    }

    public boolean getIntentType() {
        return this.intentType;
    }

    public String getAction() {
        return this.action;
    }

    public List<String> getEntities() {
        return this.entities;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public int getStartAbilityDeviceType() {
        return this.startAbilityDeviceType;
    }

    public int getFinishAbilityResultCode() {
        return this.finishAbilityResultCode;
    }

    public String getFinishAbilityResultData() {
        return this.finishAbilityResultData;
    }

    public void setBundleName(String str) {
        this.bundleName = str;
    }

    public void setAbilityName(String str) {
        this.abilityName = str;
    }

    public void setMessageCode(int i) {
        this.messageCode = i;
    }

    public void setAbilityType(int i) {
        this.abilityType = i;
    }

    public void setSyncOption(int i) {
        if (i == 0) {
            this.syncOption = true;
        } else {
            this.syncOption = false;
        }
    }

    public void setRequestData(String str) {
        this.requestData = str;
    }

    public void setParseErrorMessage(String str) {
        this.parseErrorMessage = str;
    }

    public void setIntentType(boolean z) {
        this.intentType = z;
    }

    public void setAction(String str) {
        this.action = str;
    }

    public void setEntities(List<String> list) {
        this.entities = list;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public void setStartAbilityDeviceType(int i) {
        this.startAbilityDeviceType = i;
    }

    public void setFinishAbilityResultCode(int i) {
        this.finishAbilityResultCode = i;
    }

    public void setFinishAbilityResultData(String str) {
        this.finishAbilityResultData = str;
    }

    public void setFlag(int i) {
        this.flag = i;
    }

    public int getFlag() {
        return this.flag;
    }
}
