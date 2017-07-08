package android_maps_conflict_avoidance.com.google.googlenav.map;

import java.io.IOException;

public interface MapTileStorage {
    void close(boolean z);

    MapTile getMapTile(Tile tile);

    void mapChanged();

    boolean setTileEditionAndTextSize(int i, int i2);

    boolean writeCache() throws IOException;
}
