package ohos.ivicommon.drivingsafety.model;

public class Position implements Comparable<Position> {
    private static final String REG = "^(\\+)?\\d+(\\.\\d+)?+(,(\\+)?\\d+(\\.\\d+)?)*$";
    private int height;
    private int width;
    private int xPos;
    private int yPos;

    public Position() {
        this(-1, -1, -1, -1);
    }

    public Position(StringBuffer stringBuffer) {
        this(-1, -1, -1, -1);
        if (stringBuffer != null) {
            parsePosition(stringBuffer);
        }
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

    private void parsePosition(StringBuffer stringBuffer) {
        if (!stringBuffer.toString().matches(REG)) {
            setX(-1);
            setY(-1);
            setWidth(-1);
            setHeight(-1);
            return;
        }
        String[] split = stringBuffer.toString().split(",");
        if (split.length != 4) {
            setX(-1);
            setY(-1);
            setWidth(-1);
            setHeight(-1);
            return;
        }
        setX(Integer.parseInt(split[0]));
        setY(Integer.parseInt(split[1]));
        setWidth(Integer.parseInt(split[2]));
        setHeight(Integer.parseInt(split[3]));
    }

    public int compareTo(Position position) {
        if (this == position) {
            return 0;
        }
        if (position == null) {
            return -1;
        }
        if ((position.getX() > 0 || position.getY() > 0 || position.getHeight() > 0 || position.getWidth() > 0) && getX() + position.getX() + position.getWidth() <= getWidth() && getY() + position.getY() + position.getHeight() <= getHeight()) {
            return 1;
        }
        return -1;
    }
}
