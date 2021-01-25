package ohos.data.distributed.device;

public class DeviceInfo {
    private String id;
    private String name;
    private String type;

    public DeviceInfo(String str, String str2, String str3) {
        this.id = str;
        this.name = str2;
        this.type = str3;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }
}
