package ohos.softnet.connect;

public class ServiceDesc {
    private byte[] mServiceData;
    private String mServiceId;
    private String mServiceName;

    private ServiceDesc() {
    }

    public String getServiceId() {
        return this.mServiceId;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public byte[] getServiceData() {
        return this.mServiceData;
    }

    public static class Builder {
        ServiceDesc info = new ServiceDesc();

        public Builder serviceId(String str) {
            this.info.mServiceId = str;
            return this;
        }

        public Builder serviceName(String str) {
            this.info.mServiceName = str;
            return this;
        }

        public Builder serviceData(byte[] bArr) {
            this.info.mServiceData = bArr;
            return this;
        }

        public ServiceDesc build() {
            return this.info;
        }
    }
}
