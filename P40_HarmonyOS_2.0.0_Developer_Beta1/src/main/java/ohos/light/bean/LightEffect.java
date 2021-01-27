package ohos.light.bean;

public class LightEffect {
    public static final String LIGHT_ID_BELT = "3";
    public static final String LIGHT_ID_BUTTONS = "2";
    public static final String LIGHT_ID_KEYBOARD = "1";
    public static final String LIGHT_ID_LED = "0";
    private LightBrightness lightBrightness;
    private int offDuration;
    private int onDuration;

    public LightEffect(LightBrightness lightBrightness2, int i, int i2) {
        this.lightBrightness = lightBrightness2;
        this.onDuration = i;
        this.offDuration = i2;
    }

    public LightBrightness getLightBrightness() {
        return this.lightBrightness;
    }

    public void setLightBrightness(LightBrightness lightBrightness2) {
        this.lightBrightness = lightBrightness2;
    }

    public int getOnDuration() {
        return this.onDuration;
    }

    public void setOnDuration(int i) {
        this.onDuration = i;
    }

    public int getOffDuration() {
        return this.offDuration;
    }

    public void setOffDuration(int i) {
        this.offDuration = i;
    }
}
