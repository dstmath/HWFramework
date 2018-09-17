package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.map.MapPoint;
import android_maps_conflict_avoidance.com.google.map.Zoom;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Tile {
    private static final int[] CACHE_SIZES = new int[]{131, 257, 521, 1031, 2053, 4099, 8209, 16411};
    private static Tile[] tileObjectCache;
    private static int tileObjectCacheSize;
    private final byte flags;
    private final int hashCode;
    private final int xIndex;
    private final int yIndex;
    private final Zoom zoom;

    public static void initializeTileObjectCache(int workingSetSize) {
        tileObjectCacheSize = getCacheSizeFromMinCacheSize(workingSetSize * 6);
        tileObjectCache = new Tile[tileObjectCacheSize];
    }

    static {
        initializeTileObjectCache(1);
    }

    private static int getCacheSizeFromMinCacheSize(int minSize) {
        for (int prime : CACHE_SIZES) {
            if (prime >= minSize) {
                return prime;
            }
        }
        return CACHE_SIZES[CACHE_SIZES.length - 1];
    }

    public static Tile getTile(byte flags, MapPoint point, Zoom zoom) {
        return getTile(flags, getXTileIndex(point, zoom), getYTileIndex(point, zoom), zoom);
    }

    public static Tile getTile(byte flags, Tile oldTile) {
        return getTile(flags, oldTile.xIndex, oldTile.yIndex, oldTile.zoom);
    }

    public static Tile getTile(byte flags, int xIndex, int yIndex, Zoom zoom) {
        xIndex %= zoom.getEquatorPixels() / 256;
        if (xIndex < 0) {
            xIndex += zoom.getEquatorPixels() / 256;
        }
        int hashCode = calculateHashCode(xIndex, yIndex, zoom, flags);
        int objectCacheIndex = hashCode % tileObjectCacheSize;
        if (objectCacheIndex < 0) {
            objectCacheIndex += tileObjectCacheSize;
        }
        Tile tile = tileObjectCache[objectCacheIndex];
        if (tile != null && tile.flags == flags && tile.xIndex == xIndex && tile.yIndex == yIndex && tile.zoom == zoom) {
            return tile;
        }
        tile = new Tile(flags, xIndex, yIndex, zoom, hashCode);
        tileObjectCache[objectCacheIndex] = tile;
        return tile;
    }

    private Tile(byte flags, int xIndex, int yIndex, Zoom zoom, int hashCode) {
        if (zoom != null) {
            this.flags = (byte) flags;
            this.xIndex = xIndex;
            this.yIndex = yIndex;
            this.zoom = zoom;
            this.hashCode = hashCode;
            return;
        }
        throw new IllegalArgumentException("Zoom cannot be null");
    }

    public static byte getSatType() {
        return !Config.isChinaVersion() ? (byte) 6 : (byte) 3;
    }

    public byte getFlags() {
        return this.flags;
    }

    public int getXIndex() {
        return this.xIndex;
    }

    public int getYIndex() {
        return this.yIndex;
    }

    public Zoom getZoom() {
        return this.zoom;
    }

    public String toString() {
        return "(" + this.xIndex + ", " + this.yIndex + ", " + this.zoom + ")";
    }

    public int getXPixelTopLeft() {
        return this.xIndex * 256;
    }

    public int getYPixelTopLeft() {
        return this.yIndex * 256;
    }

    public Tile getZoomParent() {
        Zoom newZoom = Zoom.getZoom(this.zoom.getZoomLevel() - 1);
        if (newZoom == null) {
            return null;
        }
        int x = this.xIndex;
        int y = this.yIndex;
        if (x < 0) {
            x--;
        }
        if (y < 0) {
            y--;
        }
        return getTile(this.flags, x / 2, y / 2, newZoom);
    }

    public Tile toTraffic() {
        return getTile((byte) 4, this);
    }

    public static Tile read(DataInput is) throws IOException {
        try {
            return getTile(is.readByte(), is.readInt(), is.readInt(), Zoom.getZoom(is.readUnsignedByte()));
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void write(DataOutput os) throws IOException {
        os.writeByte(this.flags);
        os.writeInt(this.xIndex);
        os.writeInt(this.yIndex);
        os.writeByte(this.zoom.getZoomLevel());
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tile)) {
            return false;
        }
        Tile tile = (Tile) o;
        if (!(this.xIndex == tile.xIndex && this.yIndex == tile.yIndex && this.zoom == tile.zoom && this.flags == tile.flags)) {
            z = false;
        }
        return z;
    }

    public final int hashCode() {
        return this.hashCode;
    }

    private static int calculateHashCode(int xIndex, int yIndex, Zoom zoom, int flags) {
        return (((((xIndex * 29) ^ yIndex) * 29) + zoom.getZoomLevel()) << 8) + flags;
    }

    public boolean notValid() {
        return this.yIndex < 0 || this.yIndex >= this.zoom.getEquatorPixels() / 256;
    }

    public static int getXTileIndex(MapPoint point, Zoom zoom) {
        return point.getXPixel(zoom) / 256;
    }

    public static int getYTileIndex(MapPoint point, Zoom zoom) {
        return point.getYPixel(zoom) / 256;
    }
}
