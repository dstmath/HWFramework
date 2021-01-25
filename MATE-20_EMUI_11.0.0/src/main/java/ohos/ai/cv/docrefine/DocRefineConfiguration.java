package ohos.ai.cv.docrefine;

import ohos.ai.cv.common.VisionConfiguration;

public class DocRefineConfiguration extends VisionConfiguration {
    private DocRefineConfiguration(Builder builder) {
        super(builder);
    }

    public static class Builder extends VisionConfiguration.Builder<Builder> {
        /* access modifiers changed from: protected */
        @Override // ohos.ai.cv.common.VisionConfiguration.Builder
        public Builder self() {
            return this;
        }

        public DocRefineConfiguration build() {
            return new DocRefineConfiguration(this);
        }
    }
}
