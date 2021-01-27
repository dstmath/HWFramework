package ohos.agp.components;

import ohos.agp.utils.Point;

public class ScaleInfo {
    public final double horizontalScale;
    public final double rotation;
    public final double scale;
    public final Point startPoint;
    public final Point updatePoint;
    public final double verticalScale;
    public final double xVelocity;
    public final double yVelocity;

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
