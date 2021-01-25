package ohos.appexecfwk.utils;

public class ExceptionInfo {
    private String abilityName;
    private String callbackName;
    private String className;
    private int errorType;
    private int eventId;
    private int exceptionId;
    private String interfaceName;
    private String packageName;
    private int serviceId;
    private String serviceName;

    public ExceptionInfo() {
    }

    public ExceptionInfo(int i) {
        this.eventId = i;
    }

    public ExceptionInfo(int i, int i2) {
        this(i);
        this.serviceId = i2;
    }

    public ExceptionInfo(int i, String str) {
        this(i);
        this.serviceName = str;
    }

    public void setEventId(int i) {
        this.eventId = i;
    }

    public void setServiceId(int i) {
        this.serviceId = i;
    }

    public void setServiceName(String str) {
        this.serviceName = str;
    }

    public void setInterfaceName(String str) {
        this.interfaceName = str;
    }

    public void setCallbackName(String str) {
        this.callbackName = str;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public void setAbilityName(String str) {
        this.abilityName = str;
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public void setErrorType(int i) {
        this.errorType = i;
    }

    public void setExceptionId(int i) {
        this.exceptionId = i;
    }

    public int getEventId() {
        return this.eventId;
    }

    public int getServiceId() {
        return this.serviceId;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getInterfaceName() {
        return this.interfaceName;
    }

    public String getCallbackName() {
        return this.callbackName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getAbilityName() {
        return this.abilityName;
    }

    public String getClassName() {
        return this.className;
    }

    public int getErrorType() {
        return this.errorType;
    }

    public int getExceptionId() {
        return this.exceptionId;
    }
}
