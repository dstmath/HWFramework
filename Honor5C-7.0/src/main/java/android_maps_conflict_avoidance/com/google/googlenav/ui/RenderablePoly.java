package android_maps_conflict_avoidance.com.google.googlenav.ui;

import android_maps_conflict_avoidance.com.google.map.MapPoint;

public interface RenderablePoly extends RenderableShape {
    MapPoint[][] getInnerBoundaries();

    MapPoint[] getLine();

    int getLineStyle();
}
