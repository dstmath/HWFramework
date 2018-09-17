package android_maps_conflict_avoidance.com.google.googlenav.ui;

import android_maps_conflict_avoidance.com.google.map.Zoom;

public interface RenderableShape {
    int getFillColor();

    int getId();

    int getLineColor();

    int getLineWidthForZoom(Zoom zoom);

    boolean isAvailable();

    boolean isFilled();
}
