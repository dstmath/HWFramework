package ohos.softnet.connect;

public class ConnectOption {
    public static final int STATE_CONNECTION_COMPLETED = 0;
    public static final int STATE_DISCONNECT = -1;
    private String mExtInfo;
    private byte[] mOption;
    private String mServiceId;
    private Strategy mStrategy;

    public String getServiceId() {
        return this.mServiceId;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public byte[] getOption() {
        return this.mOption;
    }

    public Strategy getStrategy() {
        return this.mStrategy;
    }

    public static class Builder {
        ConnectOption option = new ConnectOption();

        public Builder serviceId(String str) {
            this.option.mServiceId = str;
            return this;
        }

        public Builder extInfo(String str) {
            this.option.mExtInfo = str;
            return this;
        }

        public Builder strategy(Strategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder opt(byte[] bArr) {
            this.option.mOption = bArr;
            return this;
        }

        public ConnectOption build() {
            return this.option;
        }
    }
}
