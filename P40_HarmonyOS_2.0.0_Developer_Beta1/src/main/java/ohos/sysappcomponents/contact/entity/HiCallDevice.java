package ohos.sysappcomponents.contact.entity;

public class HiCallDevice {
    private String bindPhoneNumber;
    private String deviceCommuncationId;
    private String deviceInfo;
    private String deviceModel;
    private String deviceOrdinal;
    private String deviceProfile;
    private String deviceType;
    private int id;
    private String isPrivate;
    private String isSameVibration;
    private String remarkName;
    private String userName;

    public HiCallDevice(String str) {
        this.bindPhoneNumber = str;
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getBindPhoneNumber() {
        return this.bindPhoneNumber;
    }

    public void setDeviceCommuncationId(String str) {
        this.deviceCommuncationId = str;
    }

    public String getDeviceCommuncationId() {
        return this.deviceCommuncationId;
    }

    public void setDeviceType(String str) {
        this.deviceType = str;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public void setPrivateState(String str) {
        this.isPrivate = str;
    }

    public String getPrivate() {
        return this.isPrivate;
    }

    public void setDeviceProfile(String str) {
        this.deviceProfile = str;
    }

    public String getDeviceProfile() {
        return this.deviceProfile;
    }

    public void setSameVibrationState(String str) {
        this.isSameVibration = str;
    }

    public String getSameVibration() {
        return this.isSameVibration;
    }

    public void setDeviceOrdinal(String str) {
        this.deviceOrdinal = str;
    }

    public String getDeviceOrdinal() {
        return this.deviceOrdinal;
    }

    public void setDeviceModel(String str) {
        this.deviceModel = str;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public void setRemarkName(String str) {
        this.remarkName = str;
    }

    public String getRemarkName() {
        return this.remarkName;
    }

    public void setUserName(String str) {
        this.userName = str;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setDeviceInfo(String str) {
        this.deviceInfo = str;
    }

    public String getDeviceInfo() {
        return this.deviceInfo;
    }
}
