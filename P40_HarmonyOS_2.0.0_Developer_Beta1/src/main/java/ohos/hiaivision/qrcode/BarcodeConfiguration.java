package ohos.hiaivision.qrcode;

import ohos.ai.cv.common.VisionConfiguration;
import ohos.utils.PacMap;

public class BarcodeConfiguration extends VisionConfiguration {
    public BarcodeConfiguration(Builder builder) {
        super(builder);
    }

    public static class Builder extends VisionConfiguration.Builder<Builder> {
        /* access modifiers changed from: protected */
        public Builder self() {
            return this;
        }

        public BarcodeConfiguration build() {
            return new BarcodeConfiguration(this);
        }
    }

    public PacMap getParam() {
        return BarcodeConfiguration.super.getParam();
    }
}
