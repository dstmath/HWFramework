package ohos.light.common;

public class LightColor {
    private int blue;
    private int green;
    private int red;

    public LightColor(int i, int i2, int i3) {
        this.red = i;
        this.green = i2;
        this.blue = i3;
    }

    public int getRed() {
        return this.red;
    }

    public void setRed(int i) {
        this.red = i;
    }

    public int getGreen() {
        return this.green;
    }

    public void setGreen(int i) {
        this.green = i;
    }

    public int getBlue() {
        return this.blue;
    }

    public void setBlue(int i) {
        this.blue = i;
    }
}
