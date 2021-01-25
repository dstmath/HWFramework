package ohos.ai.cv.sr;

import java.util.LinkedHashMap;
import ohos.ai.cv.common.VisionConfiguration;
import ohos.utils.PacMap;
import ohos.utils.fastjson.JSON;

public class SisrConfiguration extends VisionConfiguration {
    public static final int SISR_QUALITY_HIGH = 30;
    public static final int SISR_QUALITY_LOW = 10;
    public static final int SISR_QUALITY_MEDIUM = 20;
    public static final float SISR_SCALE_1X = 1.0f;
    public static final float SISR_SCALE_3X = 3.0f;
    protected int quality;
    protected float scale;

    private SisrConfiguration(Builder builder) {
        super(builder);
        this.scale = 1.0f;
        this.quality = 30;
    }

    public static class Builder extends VisionConfiguration.Builder<Builder> {
        /* access modifiers changed from: protected */
        @Override // ohos.ai.cv.common.VisionConfiguration.Builder
        public Builder self() {
            return this;
        }

        public SisrConfiguration build() {
            return new SisrConfiguration(this);
        }
    }

    @Override // ohos.ai.cv.common.VisionConfiguration
    public PacMap getParam() {
        PacMap param = super.getParam();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("quality", Integer.valueOf(this.quality));
        linkedHashMap.put("scale", Float.valueOf(this.scale));
        param.putString(SrParamKey.VISION_SISR_CONFIG, JSON.toJSONString(linkedHashMap));
        return param;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float f) {
        this.scale = f;
    }

    public void setScale() {
        this.scale = 1.0f;
    }

    public int getQuality() {
        return this.quality;
    }

    public void setQuality(int i) {
        this.quality = i;
    }

    public void setQuality() {
        this.quality = 30;
    }
}
