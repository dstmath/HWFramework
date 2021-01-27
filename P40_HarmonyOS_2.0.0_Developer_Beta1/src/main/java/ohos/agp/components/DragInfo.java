package ohos.agp.components;

import ohos.agp.utils.Point;

public class DragInfo {
    public final Point downPoint;
    public final Point startPoint;
    public final Point updatePoint;
    public final double xOffset;
    public final double xVelocity;
    public final double yOffset;
    public final double yVelocity;

    public DragInfo(Point point, Point point2, Point point3, double d, double d2, double d3, double d4) {
        this.downPoint = point;
        this.startPoint = point2;
        this.updatePoint = point3;
        this.xOffset = d;
        this.yOffset = d2;
        this.xVelocity = d3;
        this.yVelocity = d4;
    }
}
