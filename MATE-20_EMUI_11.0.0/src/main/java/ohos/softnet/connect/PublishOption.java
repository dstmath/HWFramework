package ohos.softnet.connect;

import java.util.List;

public class PublishOption {
    public static final int PUBLISH_MODE_PUBLISH_OFFLINE = 3;
    public static final int PUBLISH_MODE_PUBLISH_ONLINE = 2;
    public static final int PUBLISH_MODE_REGISTER_SERVICE = 1;
    private int mCount;
    private String mExtInfo;
    private List<ServiceDesc> mInfos;
    private PowerPolicy mPowerPolicy;
    private int mPublishMode;
    private List<ServiceFilter> mServiceFilters;
    private Strategy mStrategy;
    private int mTimeout;

    private PublishOption() {
    }

    public List<ServiceDesc> getInfos() {
        return this.mInfos;
    }

    public Strategy getStrategy() {
        return this.mStrategy;
    }

    public int getCount() {
        return this.mCount;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public PowerPolicy getPowerPolicy() {
        return this.mPowerPolicy;
    }

    public List<ServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
    }

    public int getPublishMode() {
        return this.mPublishMode;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public static class Builder {
        private PublishOption option = new PublishOption();

        public Builder infos(List<ServiceDesc> list) {
            this.option.mInfos = list;
            return this;
        }

        public Builder serviceFilters(List<ServiceFilter> list) {
            this.option.mServiceFilters = list;
            return this;
        }

        public Builder strategy(Strategy strategy) {
            this.option.mStrategy = strategy;
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

        public Builder publishMode(int i) {
            this.option.mPublishMode = i;
            return this;
        }

        public Builder extInfo(String str) {
            this.option.mExtInfo = str;
            return this;
        }

        public PublishOption build() {
            return this.option;
        }
    }
}
