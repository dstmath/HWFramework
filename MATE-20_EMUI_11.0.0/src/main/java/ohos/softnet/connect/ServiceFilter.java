package ohos.softnet.connect;

public class ServiceFilter {
    private byte[] mFilterData;
    private byte[] mFilterMask;
    private String mServiceId;

    private ServiceFilter() {
    }

    public byte[] getFilterData() {
        return this.mFilterData;
    }

    public String getServiceId() {
        return this.mServiceId;
    }

    public byte[] getFilterMask() {
        return this.mFilterMask;
    }

    public static class Builder {
        ServiceFilter filter = new ServiceFilter();

        public Builder serviceId(String str) {
            this.filter.mServiceId = str;
            return this;
        }

        public Builder filterData(byte[] bArr) {
            this.filter.mFilterData = bArr;
            return this;
        }

        public Builder filterMask(byte[] bArr) {
            this.filter.mFilterMask = bArr;
            return this;
        }

        public ServiceFilter build() {
            return this.filter;
        }
    }
}
