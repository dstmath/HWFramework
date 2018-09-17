package android_maps_conflict_avoidance.com.google.map;

public class MapState {
    private final boolean bicyclingLayerEnabled;
    private final MapPoint centerPoint;
    private final int mapMode;
    private final Zoom zoom;

    public MapState(MapPoint centerPoint, Zoom zoom, int mapMode, boolean bicyclingLayerEnabled) {
        this.centerPoint = centerPoint;
        this.zoom = zoom;
        this.mapMode = mapMode;
        this.bicyclingLayerEnabled = bicyclingLayerEnabled;
    }

    public MapState(MapPoint centerPoint, Zoom zoom, int mapMode) {
        this(centerPoint, zoom, mapMode, false);
    }

    public MapPoint getCenterPoint() {
        return this.centerPoint;
    }

    public Zoom getZoom() {
        return this.zoom;
    }

    public int getMapMode() {
        return this.mapMode;
    }

    public boolean isSatellite() {
        return this.mapMode == 1;
    }

    public boolean isTerrain() {
        return this.mapMode == 2;
    }

    public boolean isBicyclingLayerEnabled() {
        return this.bicyclingLayerEnabled;
    }

    public MapState newMapState(MapPoint centerPoint) {
        return new MapState(centerPoint, this.zoom, this.mapMode, this.bicyclingLayerEnabled);
    }

    public MapState newMapState(Zoom zoom) {
        return new MapState(this.centerPoint, zoom, this.mapMode, this.bicyclingLayerEnabled);
    }

    public MapState newMapState(int mapMode) {
        return new MapState(this.centerPoint, this.zoom, mapMode, this.bicyclingLayerEnabled);
    }
}
