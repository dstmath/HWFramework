package android_maps_conflict_avoidance.com.google.googlenav.map;

public interface TileOverlayRenderer {
    void begin();

    void end();

    boolean renderTile(MapTile mapTile, boolean z);
}
