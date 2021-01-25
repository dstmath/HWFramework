package ohos.agp.components;

import ohos.agp.utils.Point;

public class ScaleInfo {
    public double horizontalScale;
    public double rotation;
    public double scale;
    public Point startPoint;
    public Point updatePoint;
    public double verticalScale;
    public double xVelocity;
    public double yVelocity;

    public ScaleInfo(Point point, Point point2, double d, double d2, double d3, double d4, double d5, double d6) {
        this.startPoint = point;
        this.updatePoint = point2;
        this.scale = d;
        this.horizontalScale = d2;
        this.verticalScale = d3;
        this.rotation = d4;
        this.xVelocity = d5;
        this.yVelocity = d6;
    }
}
