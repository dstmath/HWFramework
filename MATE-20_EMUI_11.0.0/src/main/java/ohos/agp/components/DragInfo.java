package ohos.agp.components;

import ohos.agp.utils.Point;

public class DragInfo {
    public Point downPoint;
    public Point startPoint;
    public Point updatePoint;
    public double xOffset;
    public double xVelocity;
    public double yOffset;
    public double yVelocity;

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
