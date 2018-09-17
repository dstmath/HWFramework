package android_maps_conflict_avoidance.com.google.googlenav.ui;

import android_maps_conflict_avoidance.com.google.map.MapPoint;

public interface RenderableEllipse extends RenderableShape {
    MapPoint getCenter();

    int getEllipseHeight();

    int getEllipseWidth();
}
