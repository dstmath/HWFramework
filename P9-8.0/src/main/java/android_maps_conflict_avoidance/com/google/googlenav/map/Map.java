package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Clock;
import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.geom.Point;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleGraphics;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import android_maps_conflict_avoidance.com.google.common.io.IoUtil;
import android_maps_conflict_avoidance.com.google.common.util.MathUtil;
import android_maps_conflict_avoidance.com.google.map.MapPoint;
import android_maps_conflict_avoidance.com.google.map.MapState;
import android_maps_conflict_avoidance.com.google.map.Zoom;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

public class Map {
    private final MapBiller biller = new MapBiller();
    private int centerPixelX;
    private int centerPixelY;
    private int completeTilesInViewport = 0;
    private int cornerToCenterDist = 0;
    private int displayHeight = 0;
    private int displayWidth = 0;
    private long earliestTileNeededTime;
    private int estimatedCountOfRenderedImagesInMapCache = 0;
    private int halfDisplayHeight = 0;
    private int halfDisplayWidth = 0;
    private int halfHeight = 0;
    private int halfWidth = 0;
    private boolean hardwareAcceleration = false;
    private int height = 0;
    private boolean isViewportAllNew;
    private long lastPaintStartTime;
    private final MapService mapService;
    private MapState mapState;
    private PixelMapper pixelMapper = null;
    private boolean running;
    private TileOverlayRenderer tileOverlayRenderer;
    private Tile[] tiles;
    private Tile topLeftDisplayTile;
    private Tile topLeftTile;
    private int width = 0;
    private int xDisplayTiles = 0;
    private int xTiles = 0;
    private int yDisplayTiles = 0;
    private int yTiles = 0;

    public interface BillingPointListener {
        void billingPointSent(MapState mapState);
    }

    public interface PixelMapper {
        void transformPoint(Point point);
    }

    public Map(int maxCacheDataSize, int targetCacheDataSize, int maxFlashSize, MapPoint defaultCenter, Zoom defaultZoom, int maxRecordStores) {
        this.mapService = new MapService(maxCacheDataSize, targetCacheDataSize, maxFlashSize, maxRecordStores, "Tiles");
        this.mapState = new MapState(defaultCenter, Zoom.getZoom(3), 0);
        load(defaultCenter, defaultZoom);
        this.running = true;
    }

    public synchronized void setCenterPoint(MapPoint centerPoint) {
        if (centerPoint != null) {
            setMapState(this.mapState.newMapState(centerPoint));
        }
    }

    public synchronized void setZoom(Zoom zoom) {
        if (zoom != null) {
            setMapState(this.mapState.newMapState(zoom));
        }
    }

    public synchronized void setMapState(MapState mapState) {
        if (mapState != null) {
            this.mapState = mapState;
            int maxMapZoom = getMaxMapZoomForPoint(getCenterPoint());
            if (mapState.getZoom().getZoomLevel() > maxMapZoom) {
                this.mapState = mapState.newMapState(Zoom.getZoom(maxMapZoom));
            }
            calculateCenterPixel();
            updateTopLeftTile(false, false);
            this.mapService.mapChanged();
            this.completeTilesInViewport = 0;
            this.isViewportAllNew = false;
        }
    }

    public MapState getMapState() {
        return this.mapState;
    }

    public Zoom getZoom() {
        return this.mapState.getZoom();
    }

    public MapPoint getCenterPoint() {
        return this.mapState.getCenterPoint();
    }

    Tile getTopLeftTile(int numXTiles, int numYTiles) {
        MapPoint centerPoint = this.mapState.getCenterPoint();
        Zoom zoom = this.mapState.getZoom();
        int xIndex = Tile.getXTileIndex(centerPoint, zoom) - (numXTiles / 2);
        int yIndex = Tile.getYTileIndex(centerPoint, zoom) - (numYTiles / 2);
        Tile centerTile = Tile.getTile(getTileFlags(), centerPoint, zoom);
        int centerXOffsetFromCenterTile = this.centerPixelX - centerTile.getXPixelTopLeft();
        if ((numXTiles % 2 == 0) && centerXOffsetFromCenterTile > 128) {
            xIndex++;
        }
        int centerYOffsetFromCenterTile = this.centerPixelY - centerTile.getYPixelTopLeft();
        if ((numYTiles % 2 == 0) && centerYOffsetFromCenterTile > 128) {
            yIndex++;
        }
        return Tile.getTile(getTileFlags(), xIndex, yIndex, zoom);
    }

    private void updateTopLeftTile(boolean sizeChanged, boolean displaySizeChanged) {
        Tile oldTopLeftTile = this.topLeftTile;
        Tile oldTopLeftDisplayTile = this.topLeftDisplayTile;
        this.topLeftTile = getTopLeftTile(this.xTiles, this.yTiles);
        this.topLeftDisplayTile = getTopLeftTile(this.xDisplayTiles, this.yDisplayTiles);
        if (sizeChanged || oldTopLeftTile == null || !oldTopLeftTile.equals(this.topLeftTile)) {
            precalculateTiles();
        }
        if (displaySizeChanged || oldTopLeftDisplayTile == null || !oldTopLeftDisplayTile.equals(this.topLeftDisplayTile)) {
            this.mapService.notifyLayerTilesDirty();
        }
    }

    private void precalculateTiles() {
        if (this.tiles != null) {
            int index = 0;
            byte tileFlags = getTileFlags();
            for (int i = 0; i < this.xTiles; i++) {
                int j = 0;
                while (j < this.yTiles) {
                    int index2 = index + 1;
                    this.tiles[index] = Tile.getTile(tileFlags, this.topLeftTile.getXIndex() + i, this.topLeftTile.getYIndex() + j, this.mapState.getZoom());
                    j++;
                    index = index2;
                }
            }
        }
    }

    public synchronized void zoomToSpan(int latSpanE6, int lonSpanE6) {
        setZoom(findZoom(latSpanE6, lonSpanE6, this.mapState));
    }

    private synchronized Zoom findZoom(int latSpanE6, int lonSpanE6, MapState newMapState) {
        Zoom newZoom;
        newZoom = Zoom.getZoom(getMaxMapZoomForPoint(newMapState.getCenterPoint()));
        while (newZoom.getNextLowerZoom() != null) {
            if (getLatitudeSpan(newMapState.newMapState(newZoom)) >= latSpanE6 && getLongitudeSpan(newMapState.newMapState(newZoom)) >= lonSpanE6) {
                break;
            }
            newZoom = newZoom.getNextLowerZoom();
        }
        return newZoom;
    }

    private void calculateCenterPixel() {
        this.centerPixelX = this.mapState.getCenterPoint().getXPixel(this.mapState.getZoom());
        this.centerPixelY = this.mapState.getCenterPoint().getYPixel(this.mapState.getZoom());
    }

    public Point getPointXY(MapPoint point) {
        Point pixelPoint = new Point();
        getPointXY(point, pixelPoint);
        return pixelPoint;
    }

    public void getPointXY(MapPoint point, Point pixelPoint) {
        getPixelOffsetFromCenter(point, pixelPoint);
        pixelPoint.x += this.halfWidth;
        pixelPoint.y += this.halfHeight;
    }

    public Point getPixelOffsetFromCenter(MapPoint point) {
        Point resultPoint = new Point();
        getPixelOffsetFromCenter(point, resultPoint);
        return resultPoint;
    }

    public void getPixelOffsetFromCenter(MapPoint point, Point pixelResult) {
        pixelResult.x = point.getXPixel(this.mapState.getZoom()) - this.centerPixelX;
        int equatorPixels = this.mapState.getZoom().getEquatorPixels();
        if (pixelResult.x < (-equatorPixels) / 2) {
            pixelResult.x += equatorPixels;
        } else if (pixelResult.x > equatorPixels / 2) {
            pixelResult.x -= equatorPixels;
        }
        pixelResult.y = point.getYPixel(this.mapState.getZoom()) - this.centerPixelY;
        if (this.pixelMapper != null) {
            pixelResult.x += this.halfDisplayWidth;
            pixelResult.y += this.halfDisplayHeight;
            this.pixelMapper.transformPoint(pixelResult);
            pixelResult.x -= this.halfDisplayWidth;
            pixelResult.y -= this.halfDisplayHeight;
        }
    }

    public synchronized void saveState() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(14);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(2);
            MapPoint.writePoint(this.mapState.getCenterPoint(), dos);
            dos.writeInt(this.mapState.getZoom().getZoomLevel());
            int flags = 0;
            if (this.mapState.isSatellite()) {
                flags = 1;
            } else if (this.mapState.isTerrain()) {
                flags = 2;
            }
            if (this.mapState.isBicyclingLayerEnabled()) {
                flags |= 4;
            }
            dos.writeByte(flags);
            Config.getInstance().getPersistentStore().setPreference("Map info", baos.toByteArray());
        } catch (IOException e) {
            Log.logThrowable("MAP", e);
        }
        return;
    }

    private void load(MapPoint defaultCenter, Zoom defaultZoom) {
        boolean bicyclingLayerEnabled = false;
        byte[] data = Config.getInstance().getPersistentStore().readPreference("Map info");
        MapPoint startPoint = null;
        int zoomLevel = 0;
        boolean loadSuccess = false;
        int flags = 0;
        if (data != null) {
            try {
                DataInput dis = IoUtil.createDataInputFromBytes(data);
                if (dis.readUnsignedShort() == 2) {
                    startPoint = MapPoint.readPoint(dis);
                    zoomLevel = dis.readInt();
                    flags = dis.readUnsignedByte();
                    loadSuccess = true;
                }
            } catch (IOException e) {
                Log.logThrowable("MAP", e);
                Config.getInstance().getPersistentStore().deleteBlock("Map info");
            }
        }
        if (loadSuccess) {
            int mapMode;
            if ((flags & 1) != 0) {
                mapMode = 1;
            } else if ((flags & 2) == 0) {
                mapMode = 0;
            } else {
                mapMode = 2;
            }
            if ((flags & 4) != 0) {
                bicyclingLayerEnabled = true;
            }
            setMapState(new MapState(startPoint, Zoom.getZoom(Math.min(zoomLevel, getMaxMapInitialZoomForPoint(startPoint))), mapMode, bicyclingLayerEnabled));
            return;
        }
        setMapState(new MapState(defaultCenter, defaultZoom, 0));
    }

    public synchronized void preLoad(MapPoint newCenter) {
        MapPoint oldCenter = this.mapState.getCenterPoint();
        try {
            setCenterPoint(newCenter);
            drawMapBackground(null, true, false);
            setCenterPoint(oldCenter);
        } catch (Throwable th) {
            setCenterPoint(oldCenter);
        }
    }

    public boolean drawMap(GoogleGraphics g, boolean fetch, boolean locationDisplayed, boolean routeDisplayed, boolean drawIncompleteTiles, boolean drawFast) {
        if (this.height == 0 || this.width == 0) {
            throw new IllegalStateException("Map has zero size");
        }
        if (g != null) {
            this.biller.doBilling(locationDisplayed, routeDisplayed, this);
        }
        return drawMapBackground(g, fetch, drawIncompleteTiles);
    }

    private boolean drawMapBackground(GoogleGraphics g, boolean fetch, boolean drawIncompleteTiles) {
        int xOffset = (this.halfWidth + this.topLeftTile.getXPixelTopLeft()) - this.mapState.getCenterPoint().getXPixel(this.mapState.getZoom());
        int yOffset = (this.halfHeight + this.topLeftTile.getYPixelTopLeft()) - this.mapState.getCenterPoint().getYPixel(this.mapState.getZoom());
        while (xOffset > 0) {
            xOffset -= this.mapState.getZoom().getEquatorPixels();
        }
        if (this.tileOverlayRenderer != null) {
            this.tileOverlayRenderer.begin();
        }
        int tileIndex = 0;
        int tilesDrawn = 0;
        Clock clock = Config.getInstance().getClock();
        long paintStartTimeRelative = clock.relativeTimeMillis();
        long paintStartTimeCurrent = clock.currentTimeMillis();
        boolean drawOptionalFeatures = true;
        if (g == null) {
            drawOptionalFeatures = false;
        }
        this.earliestTileNeededTime = Long.MAX_VALUE;
        for (int i = 0; i < this.xTiles; i++) {
            int j = 0;
            while (j < this.yTiles) {
                int i2;
                long tileAccessTime = paintStartTimeCurrent + ((long) tileIndex);
                int tileIndex2 = tileIndex + 1;
                if (drawTile(this.tiles[tileIndex], i, j, g, xOffset, yOffset, fetch, drawIncompleteTiles, drawOptionalFeatures, paintStartTimeRelative, tileAccessTime)) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                tilesDrawn += i2;
                if (drawOptionalFeatures && checkPaintTimeExceeded(paintStartTimeRelative)) {
                    drawOptionalFeatures = false;
                }
                j++;
                tileIndex = tileIndex2;
            }
        }
        if (this.estimatedCountOfRenderedImagesInMapCache > 48) {
            this.estimatedCountOfRenderedImagesInMapCache = this.mapService.restoreBaseImagesIfNeeded();
        }
        if (g != null) {
            logPerceivedTileLatency(tilesDrawn);
        }
        if (fetch) {
            this.mapService.requestTiles();
        }
        this.mapService.requestLayerTiles();
        if (this.tileOverlayRenderer != null) {
            this.tileOverlayRenderer.end();
        }
        FlashRecord.clearDataCache();
        this.lastPaintStartTime = paintStartTimeRelative;
        if (checkPaintTimeExceeded(paintStartTimeRelative)) {
            return false;
        }
        return true;
    }

    private void logPerceivedTileLatency(int tilesDrawn) {
        if (this.earliestTileNeededTime == Long.MAX_VALUE) {
            this.completeTilesInViewport = tilesDrawn;
            return;
        }
        String tileType;
        byte tileFlags = getTileFlags();
        switch (tileFlags) {
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                tileType = "m";
                break;
            case LayoutParams.LEFT /*3*/:
                tileType = "s";
                break;
            case (byte) 6:
                tileType = "h";
                break;
            case (byte) 7:
                tileType = "n";
                break;
            default:
                if ((tileFlags & -128) != 0) {
                    return;
                }
                return;
        }
        long latency = Config.getInstance().getClock().relativeTimeMillis() - this.earliestTileNeededTime;
        if (this.completeTilesInViewport == 0) {
            Log.addEvent((short) 22, "tf" + tileType, "" + latency);
            this.isViewportAllNew = true;
        }
        if (this.completeTilesInViewport < tilesDrawn && tilesDrawn == this.xTiles * this.yTiles) {
            Log.addEvent((short) 22, (!this.isViewportAllNew ? "tp" : "tc") + tileType, "" + latency);
        }
        this.completeTilesInViewport = tilesDrawn;
    }

    private void updatePerceivedTileLatency(MapTile mapTile) {
        if (mapTile.getCompletePaintCount() == 1) {
            this.earliestTileNeededTime = Math.min(mapTile.getFirstPaintTime(), this.earliestTileNeededTime);
        }
    }

    private void logIfPreCached(MapTile mapTile) {
        if (mapTile.getIsPreCached() && mapTile.getCompletePaintCount() == 1) {
            String str = "pc";
            Log.addEvent((short) 22, str, "" + (Config.getInstance().getClock().relativeTimeMillis() - mapTile.getFirstPaintTime()));
        }
    }

    private boolean drawTile(Tile tile, int i, int j, GoogleGraphics g, int xOffset, int yOffset, boolean fetch, boolean drawIncompleteTiles, boolean drawOptionalFeatures, long paintStartTime, long accessTime) {
        int x = xOffset + (i * 256);
        int y = yOffset + (j * 256);
        if (!tile.notValid()) {
            int xDist = this.halfWidth - (x + 128);
            int yDist = this.halfHeight - (y + 128);
            int priority = (xDist * xDist) + (yDist * yDist);
            boolean tileOnScreen = isTileOnScreen(tile);
            if (fetch && !tileOnScreen) {
                fetch = false;
            }
            MapTile mapTile = this.mapService.getTile(tile, priority, fetch, !drawOptionalFeatures ? 1 : 2, accessTime);
            if (tileOnScreen && this.tileOverlayRenderer != null && drawOptionalFeatures && this.tileOverlayRenderer.renderTile(mapTile, fetch) && mapTile.hasRenderedImage()) {
                this.estimatedCountOfRenderedImagesInMapCache++;
            }
            boolean completeTileDrawn = false;
            if (g != null && (mapTile.hasImage() || drawIncompleteTiles)) {
                g.drawImage(mapTile.getImage(accessTime), x, y);
                mapTile.setPaint(paintStartTime, this.lastPaintStartTime);
                if (mapTile.getCompletePaintCount() > 0) {
                    updatePerceivedTileLatency(mapTile);
                    logIfPreCached(mapTile);
                    completeTileDrawn = true;
                }
            }
            Vector layerTileImages = this.mapService.getLayerTiles(tile, fetch);
            if (g != null) {
                for (int n = layerTileImages.size() - 1; n >= 0; n--) {
                    g.drawImage((GoogleImage) layerTileImages.elementAt(n), x, y);
                }
            }
            return completeTileDrawn;
        } else if (g == null) {
            return false;
        } else {
            g.setColor(16777215);
            g.fillRect(x, y, 256, 256);
            return true;
        }
    }

    public boolean isTileOnScreen(Tile tile) {
        return isTileOnScreenY(tile) && isTileOnScreenX(tile);
    }

    private boolean isTileOnScreenY(Tile tile) {
        return !tile.notValid() && tile.getYIndex() >= this.topLeftDisplayTile.getYIndex() && tile.getYIndex() < this.topLeftDisplayTile.getYIndex() + this.yDisplayTiles;
    }

    private boolean isTileOnScreenX(Tile tile) {
        boolean allXTilesOnScreen;
        boolean z = true;
        boolean z2 = false;
        int equatorTiles = tile.getZoom().getEquatorPixels() / 256;
        if (this.xDisplayTiles < equatorTiles) {
            allXTilesOnScreen = false;
        } else {
            allXTilesOnScreen = true;
        }
        if (allXTilesOnScreen) {
            return true;
        }
        int rightDisplayTileXIndex = ((this.topLeftDisplayTile.getXIndex() + this.xDisplayTiles) - 1) % equatorTiles;
        if (this.topLeftDisplayTile.getXIndex() >= rightDisplayTileXIndex) {
            if (tile.getXIndex() >= this.topLeftDisplayTile.getXIndex() || tile.getXIndex() <= rightDisplayTileXIndex) {
                z2 = true;
            }
            return z2;
        }
        if (tile.getXIndex() < this.topLeftDisplayTile.getXIndex() || tile.getXIndex() > rightDisplayTileXIndex) {
            z = false;
        }
        return z;
    }

    private static boolean checkPaintTimeExceeded(long paintStartTime) {
        return !(((Config.getInstance().getClock().relativeTimeMillis() - paintStartTime) > 200 ? 1 : ((Config.getInstance().getClock().relativeTimeMillis() - paintStartTime) == 200 ? 0 : -1)) <= 0);
    }

    public void close(boolean saveState) {
        if (saveState) {
            saveState();
        }
        this.mapService.close(saveState);
    }

    public void pause() {
        if (this.running) {
            this.running = false;
            this.mapService.pause();
        }
    }

    public void resume() {
        if (!this.running) {
            this.running = true;
            this.mapService.resume();
        }
    }

    public void resize(int newWidth, int newHeight) {
        resize(newWidth, newHeight, newWidth, newHeight);
    }

    public void resize(int newWidth, int newHeight, int newDisplayWidth, int newDisplayHeight) {
        boolean z = true;
        if (newWidth != this.width || newHeight != this.height || newDisplayWidth != this.displayWidth || newDisplayHeight != this.displayHeight) {
            this.height = newHeight;
            this.width = newWidth;
            this.cornerToCenterDist = (int) Math.sqrt((double) (((this.width * this.width) / 4) + ((this.height * this.height) / 4)));
            this.halfWidth = this.width / 2;
            this.halfHeight = this.height / 2;
            this.displayHeight = newDisplayHeight;
            this.displayWidth = newDisplayWidth;
            this.halfDisplayWidth = newDisplayWidth / 2;
            this.halfDisplayHeight = newDisplayHeight / 2;
            int oldXTiles = this.xTiles;
            int oldYTiles = this.yTiles;
            int oldXDisplayTiles = this.xDisplayTiles;
            int oldYDisplayTiles = this.yDisplayTiles;
            this.xTiles = getMaxTiles(this.width);
            this.yTiles = getMaxTiles(this.height);
            this.xDisplayTiles = getMaxTiles(newDisplayWidth);
            this.yDisplayTiles = getMaxTiles(newDisplayHeight);
            if (this.tiles == null || oldXTiles * oldYTiles != this.xTiles * this.yTiles) {
                this.tiles = new Tile[(this.xTiles * this.yTiles)];
            }
            boolean z2 = (oldXTiles == this.xTiles && oldYTiles == this.yTiles) ? false : true;
            if (oldXDisplayTiles == this.xDisplayTiles && oldYDisplayTiles == this.yDisplayTiles) {
                z = false;
            }
            updateTopLeftTile(z2, z);
            this.mapService.mapChanged();
            this.completeTilesInViewport = 0;
            this.isViewportAllNew = false;
        }
    }

    public static int getMaxTiles(int size) {
        return MathUtil.ceiledDivision(size, 256) + 1;
    }

    public byte getTileFlags() {
        byte tileFlags = getTileFlagsForMapMode();
        if (this.mapState.isBicyclingLayerEnabled()) {
            return (byte) (tileFlags | -128);
        }
        return tileFlags;
    }

    private byte getTileFlagsForMapMode() {
        switch (this.mapState.getMapMode()) {
            case 1:
                return Tile.getSatType();
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                return (byte) 7;
            default:
                return (byte) 2;
        }
    }

    public void setMapMode(int mapMode) {
        setMapState(this.mapState.newMapState(mapMode));
    }

    public int getLatitudeSpan(MapState mapState) {
        MapPoint point = mapState.getCenterPoint();
        Zoom zoom = mapState.getZoom();
        return Math.abs(point.pixelOffset(-this.halfDisplayWidth, -this.halfDisplayHeight, zoom).getLatitude() - point.pixelOffset(this.halfDisplayWidth, this.halfDisplayHeight, zoom).getLatitude());
    }

    public int getLongitudeSpan(MapState mapState) {
        MapPoint point = mapState.getCenterPoint();
        Zoom zoom = mapState.getZoom();
        int span = point.pixelOffset(this.halfDisplayWidth, this.halfDisplayHeight, zoom).getLongitude() - point.pixelOffset(-this.halfDisplayWidth, -this.halfDisplayHeight, zoom).getLongitude();
        if (span >= 0) {
            return span;
        }
        return span + 360000000;
    }

    public synchronized int getLatitudeSpan() {
        return getLatitudeSpan(this.mapState);
    }

    public boolean canCover(MapPoint point, boolean scaleImageOk) {
        return canCover(point, scaleImageOk, this.mapState.getZoom());
    }

    public boolean canCover(MapPoint point, boolean scaleImageOk, Zoom zoomLevel) {
        MapTile mapTile = this.mapService.getTile(Tile.getTile(getTileFlags(), point, zoomLevel), 0, false, scaleImageOk);
        if ((scaleImageOk && mapTile.hasScaledImage()) || mapTile.isComplete()) {
            return true;
        }
        return false;
    }

    public boolean isSatellite() {
        return this.mapState.isSatellite();
    }

    public void setTileOverlayRenderer(TileOverlayRenderer tileOverlayRenderer) {
        this.tileOverlayRenderer = tileOverlayRenderer;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getDisplayHeight() {
        return this.displayHeight;
    }

    public int getDisplayWidth() {
        return this.displayWidth;
    }

    public int getMaxMapZoomForPoint(MapPoint point) {
        int extra = 0;
        if (isSatellite()) {
            return 22;
        }
        if (this.mapState.getMapMode() == 2) {
            return 16;
        }
        if (MapTile.getTextSize() == 3) {
            extra = 1;
        }
        if (Config.isChinaVersion()) {
            return extra + 18;
        }
        return extra + 20;
    }

    public static int getMaxMapInitialZoomForPoint(MapPoint point) {
        if (isMapPointInKoreaBoundingBox(point) || !isMapPointInJapanBoundingBox(point)) {
            return 15;
        }
        return 16;
    }

    public static boolean isMapPointInJapanBoundingBox(MapPoint point) {
        if (point != null) {
            int lat = point.getLatitude();
            int lng = point.getLongitude();
            if (lat > 23883332 && lat < 46072278 && lng > 123748627 && lng < 143789063) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMapPointInKoreaBoundingBox(MapPoint point) {
        if (point != null) {
            int lat = point.getLatitude();
            int lng = point.getLongitude();
            if ((lat > 32989084 && lat < 38693013 && lng > 124605560 && lng < 128496094) || ((lat > 34464674 && lat < 38693013 && lng > 128496094 && lng < 128847656) || ((lat > 35027747 && lat < 38693013 && lng > 128847656 && lng < 131053162) || (lat > 37027773 && lat < 38693013 && lng > 131053162 && lng < 132003479)))) {
                return true;
            }
        }
        return false;
    }
}
