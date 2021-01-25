package ohos.ivicommon.drivingsafety.model;

public class Position {
    private int height;
    private int width;
    private int xPos;
    private int yPos;

    public Position() {
        this(-1, -1, -1, -1);
    }

    public Position(int i, int i2, int i3, int i4) {
        this.xPos = i;
        this.yPos = i2;
        this.height = i3;
        this.width = i4;
    }

    public void setX(int i) {
        this.xPos = i;
    }

    public void setY(int i) {
        this.yPos = i;
    }

    public void setHeight(int i) {
        this.height = i;
    }

    public void setWidth(int i) {
        this.width = i;
    }

    public int getX() {
        return this.xPos;
    }

    public int getY() {
        return this.yPos;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }
}
