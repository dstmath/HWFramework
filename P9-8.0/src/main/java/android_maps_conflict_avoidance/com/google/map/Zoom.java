package android_maps_conflict_avoidance.com.google.map;

public final class Zoom {
    private static int minZoomLevel = 1;
    private static final Zoom[] zoomArray = new Zoom[22];
    private final int equatorPixels;
    private final int zoomLevel;

    static {
        int equatorPixels = 256;
        for (int zoomLevel = 1; zoomLevel <= 22; zoomLevel++) {
            zoomArray[zoomLevel - 1] = new Zoom(zoomLevel, equatorPixels);
            equatorPixels *= 2;
        }
    }

    private Zoom(int zoomLevel, int equatorPixels) {
        this.zoomLevel = zoomLevel;
        this.equatorPixels = equatorPixels;
    }

    public int getPixelsForDistance(int meters) {
        return Math.max((int) ((((long) meters) * ((long) this.equatorPixels)) / 40076000), 1);
    }

    public static Zoom getZoom(int zoomLevel) {
        if (zoomLevel >= minZoomLevel && zoomLevel <= 22) {
            return zoomArray[zoomLevel - 1];
        }
        return null;
    }

    public int getZoomRatio(Zoom zoomIn) {
        return zoomIn.equatorPixels / this.equatorPixels;
    }

    public int getZoomLevel() {
        return this.zoomLevel;
    }

    public int getEquatorPixels() {
        return this.equatorPixels;
    }

    public boolean isMoreZoomedIn(Zoom zoom) {
        return this.zoomLevel > zoom.zoomLevel;
    }

    public Zoom getNextHigherZoom() {
        return getZoom(this.zoomLevel + 1);
    }

    public int changePixelsToTargetZoomlevel(int pixels, int zoomTargetLevel) {
        if (this.zoomLevel >= zoomTargetLevel) {
            return pixels >> (this.zoomLevel - zoomTargetLevel);
        }
        return pixels << (zoomTargetLevel - this.zoomLevel);
    }

    public Zoom getNextLowerZoom() {
        return getZoom(this.zoomLevel - 1);
    }

    public String toString() {
        return super.toString();
    }
}
