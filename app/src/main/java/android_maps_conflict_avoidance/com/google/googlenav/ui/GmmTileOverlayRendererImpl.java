package android_maps_conflict_avoidance.com.google.googlenav.ui;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;
import android_maps_conflict_avoidance.com.google.googlenav.map.Tile;
import android_maps_conflict_avoidance.com.google.googlenav.map.TileOverlayRenderer;
import android_maps_conflict_avoidance.com.google.googlenav.map.TrafficRenderer;
import android_maps_conflict_avoidance.com.google.googlenav.map.TrafficService;
import android_maps_conflict_avoidance.com.google.googlenav.map.TrafficTile;
import java.util.Vector;

public abstract class GmmTileOverlayRendererImpl implements TileOverlayRenderer {
    private final Vector shapeProviders;
    protected ShapeRenderer shapeRenderer;
    private int shapeRendererVersion;
    private boolean showTraffic;
    protected final TrafficRenderer trafficRenderer;
    protected TrafficService trafficService;

    protected abstract GoogleImage generateNewTileImage(MapTile mapTile, TrafficTile trafficTile);

    protected abstract boolean isFast();

    public GmmTileOverlayRendererImpl() {
        this.showTraffic = false;
        this.shapeProviders = new Vector();
        this.trafficRenderer = new TrafficRenderer();
    }

    public void setTrafficService(TrafficService trafficService) {
        this.trafficService = trafficService;
    }

    public void begin() {
        this.shapeRendererVersion = this.shapeRenderer == null ? 0 : this.shapeRenderer.getImageVersion();
    }

    public boolean isShowTraffic() {
        return this.showTraffic;
    }

    public void setShowTraffic(boolean showTraffic) {
        this.showTraffic = showTraffic;
    }

    public boolean renderTile(MapTile tile, boolean fetch) {
        return renderTileImage(tile, fetch);
    }

    public void end() {
        this.trafficService.requestTiles();
    }

    private boolean renderTileImage(MapTile mapTile, boolean fetch) {
        TrafficTile tt = null;
        Tile tile = mapTile.getLocation();
        if (this.showTraffic && tile.getZoom().getZoomLevel() >= 9 && tile.getZoom().getZoomLevel() <= 20) {
            tt = this.trafficService.getTile(mapTile.getLocation().toTraffic(), fetch);
        }
        int imageVersion = getImageVersion(mapTile);
        if (isFast() || mapTile.hasImage()) {
            if (!(mapTile.hasScaledImage() || mapTile.getImageVersion() == imageVersion)) {
                updateTileImage(mapTile, tt, imageVersion);
                return true;
            }
        }
        return false;
    }

    private void updateTileImage(MapTile mapTile, TrafficTile tt, int imageVersion) {
        if (tt != null || this.shapeRenderer != null) {
            if (mapTile.getImageVersion() == 0) {
                if (!(tt == null || tt.isComplete())) {
                    tt = null;
                }
                GoogleImage newImage = generateNewTileImage(mapTile, tt);
                if (newImage != null) {
                    mapTile.setImage(newImage, imageVersion, true);
                    return;
                } else {
                    mapTile.setImageVersion(imageVersion);
                    return;
                }
            }
        }
        if (mapTile.getImageVersion() != 0 && mapTile.getImageVersion() != imageVersion) {
            mapTile.restoreBaseImage();
            mapTile.getImage();
        }
    }

    private int getImageVersion(MapTile mapTile) {
        int version = 0;
        if (this.showTraffic) {
            TrafficTile tt = this.trafficService.getTile(mapTile.getLocation().toTraffic(), false);
            if (tt != null && tt.isComplete()) {
                if (!tt.isEmpty()) {
                    version = (int) tt.getDataTime();
                }
                tt.setLastAccess(Config.getInstance().getClock().relativeTimeMillis());
            }
        }
        return (version * 29) + this.shapeRendererVersion;
    }
}
