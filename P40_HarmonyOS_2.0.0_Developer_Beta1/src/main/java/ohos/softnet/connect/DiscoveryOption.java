package ohos.softnet.connect;

import java.util.List;

public class DiscoveryOption {
    public static final int DEVICE_FILTER_BY_NONE = 0;
    public static final int DEVICE_FILTER_BY_SAME_HWACCOUNT_ID = 1;
    public static final int DISCOVERY_MODE_DISCOVERY = 1;
    public static final int DISCOVERY_MODE_SUBSCRIBE = 2;
    private int mCount;
    private int mDiscoveryMode;
    private String mExtInfo;
    private List<ServiceDesc> mInfos;
    private PowerPolicy mPowerPolicy;
    private List<ServiceFilter> mServiceFilters;
    private Strategy mStrategy;
    private int mTimeout;

    private DiscoveryOption() {
    }

    public List<ServiceDesc> getInfos() {
        return this.mInfos;
    }

    public List<ServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
    }

    public int getCount() {
        return this.mCount;
    }

    public Strategy getStrategy() {
        return this.mStrategy;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public PowerPolicy getPowerPolicy() {
        return this.mPowerPolicy;
    }

    public int getDiscoveryMode() {
        return this.mDiscoveryMode;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public static class Builder {
        private DiscoveryOption option = new DiscoveryOption();

        public Builder strategy(Strategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder infos(List<ServiceDesc> list) {
            this.option.mInfos = list;
            return this;
        }

        public Builder serviceFilters(List<ServiceFilter> list) {
            this.option.mServiceFilters = list;
            return this;
        }

        public Builder timeout(int i) {
            this.option.mTimeout = i;
            return this;
        }

        public Builder count(int i) {
            this.option.mCount = i;
            return this;
        }

        public Builder powerPolicy(PowerPolicy powerPolicy) {
            this.option.mPowerPolicy = powerPolicy;
            return this;
        }

        public Builder discoveryMode(int i) {
            this.option.mDiscoveryMode = i;
            return this;
        }

        public Builder extInfo(String str) {
            this.option.mExtInfo = str;
            return this;
        }

        public DiscoveryOption build() {
            return this.option;
        }
    }
}
