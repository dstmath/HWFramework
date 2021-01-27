package ohos.ai.cv.sr;

import ohos.ai.cv.common.VisionConfiguration;

public class TxtImageSuperResolutionConfiguration extends VisionConfiguration {
    private TxtImageSuperResolutionConfiguration(Builder builder) {
        super(builder);
    }

    public static class Builder extends VisionConfiguration.Builder<Builder> {
        /* access modifiers changed from: protected */
        @Override // ohos.ai.cv.common.VisionConfiguration.Builder
        public Builder self() {
            return this;
        }

        public TxtImageSuperResolutionConfiguration build() {
            return new TxtImageSuperResolutionConfiguration(this);
        }
    }
}
