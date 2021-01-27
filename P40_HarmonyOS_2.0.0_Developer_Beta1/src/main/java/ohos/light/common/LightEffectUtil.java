package ohos.light.common;

public class LightEffectUtil {
    private LightColor color;
    private int offMs;
    private int onMs;

    public LightEffectUtil(LightColor lightColor, int i, int i2) {
        this.color = lightColor;
        this.onMs = i;
        this.offMs = i2;
    }

    public LightColor getColor() {
        return this.color;
    }

    public void setColor(LightColor lightColor) {
        this.color = lightColor;
    }

    public int getOnMs() {
        return this.onMs;
    }

    public void setOnMs(int i) {
        this.onMs = i;
    }

    public int getOffMs() {
        return this.offMs;
    }

    public void setOffMs(int i) {
        this.offMs = i;
    }
}
