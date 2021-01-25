package ohos.light.bean;

public class LightBrightness {
    private int blueBrightness;
    private int greenBrightness;
    private int redBrightness;

    public LightBrightness(int i, int i2, int i3) {
        this.redBrightness = i;
        this.greenBrightness = i2;
        this.blueBrightness = i3;
    }

    public int getRedBrightness() {
        return this.redBrightness;
    }

    public void setRedBrightness(int i) {
        this.redBrightness = i;
    }

    public int getGreenBrightness() {
        return this.greenBrightness;
    }

    public void setGreenBrightness(int i) {
        this.greenBrightness = i;
    }

    public int getBlueBrightness() {
        return this.blueBrightness;
    }

    public void setBlueBrightness(int i) {
        this.blueBrightness = i;
    }
}
