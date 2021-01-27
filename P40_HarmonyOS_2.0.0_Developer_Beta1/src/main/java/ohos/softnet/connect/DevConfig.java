package ohos.softnet.connect;

public class DevConfig {
    private NetRole mNetRole;

    private DevConfig() {
    }

    public NetRole getNetRole() {
        return this.mNetRole;
    }

    public static class Builder {
        DevConfig config = new DevConfig();

        public Builder netRole(NetRole netRole) {
            this.config.mNetRole = netRole;
            return this;
        }

        public DevConfig build() {
            return this.config;
        }
    }
}
