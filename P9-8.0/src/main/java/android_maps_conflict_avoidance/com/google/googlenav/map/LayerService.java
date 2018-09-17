package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.OutOfMemoryHandler;
import android_maps_conflict_avoidance.com.google.common.StaticUtil;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufUtil;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.BaseDataRequest;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher;
import android_maps_conflict_avoidance.com.google.googlenav.layer.ClickableArea;
import android_maps_conflict_avoidance.com.google.googlenav.layer.LayerInfo;
import android_maps_conflict_avoidance.com.google.googlenav.proto.GmmMessageTypes;
import android_maps_conflict_avoidance.com.google.map.Zoom;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

public class LayerService implements OutOfMemoryHandler {
    private final Hashtable cache = new Hashtable();
    private long nextRefreshTime = Long.MIN_VALUE;
    private final Vector observers = new Vector();
    private long refreshMillis = -1;
    private volatile LayerTileRequest request;
    private final Hashtable requestedTiles = new Hashtable();

    private class LayerTileRequest extends BaseDataRequest {
        private boolean closed = false;
        private final Vector tiles = new Vector();

        public int getRequestType() {
            return 36;
        }

        public synchronized void addTile(LayerTile layerTile) {
            if (this.closed) {
                throw new RuntimeException("Adding tiles to closed request!");
            } else if (this.tiles.indexOf(layerTile) == -1) {
                this.tiles.addElement(layerTile);
            }
        }

        public void writeRequestData(DataOutput dos) throws IOException {
            int i;
            synchronized (this) {
                this.closed = true;
            }
            ProtoBuf request = new ProtoBuf(GmmMessageTypes.LAYER_TILE_REQUEST_PROTO);
            request.setInt(1, 256);
            int zoomLevel = 1;
            int n = this.tiles.size();
            for (i = 0; i < n; i++) {
                Tile tile = ((LayerTile) this.tiles.elementAt(i)).getLocation();
                zoomLevel = tile.getZoom().getZoomLevel();
                ProtoBuf tileRequest = new ProtoBuf(GmmMessageTypes.MAP_TILE_PROTO);
                tileRequest.setInt(1, 8);
                tileRequest.setInt(2, tile.getXIndex());
                tileRequest.setInt(3, tile.getYIndex());
                tileRequest.setInt(4, zoomLevel);
                request.addProtoBuf(3, tileRequest);
            }
            for (i = LayerService.this.observers.size() - 1; i >= 0; i--) {
                LayerInfo layerInfo = ((TileUpdateObserver) LayerService.this.observers.elementAt(i)).getLayerInfo();
                ProtoBuf layerRequest = new ProtoBuf(null);
                layerRequest.setString(21, layerInfo.getId());
                ProtoBuf[] parameters = layerInfo.getParameters();
                if (parameters != null) {
                    for (int j = parameters.length - 1; j >= 0; j--) {
                        layerRequest.addProtoBuf(22, parameters[j]);
                    }
                }
                if (layerInfo.isValidZoomLevel(zoomLevel)) {
                    request.addProtoBuf(2, layerRequest);
                }
            }
            request.outputWithSizeTo((OutputStream) dos);
        }

        public boolean readResponseData(DataInput dis) throws IOException {
            ProtoBuf response = ProtoBufUtil.readProtoBufResponse(GmmMessageTypes.LAYER_TILE_RESPONSE_PROTO, dis);
            processResponseHeader(response.getProtoBuf(1));
            int tilesNum = response.getCount(2);
            for (int i = 0; i < tilesNum; i++) {
                processLayerTile(response.getProtoBuf(2, i), true);
            }
            return true;
        }

        public void processResponseHeader(ProtoBuf responseHeader) {
            for (int i = responseHeader.getCount(1) - 1; i >= 0; i--) {
                ProtoBuf layerResponse = responseHeader.getProtoBuf(1, i);
                String layerId = layerResponse.getString(11);
                int layerParamsNum = layerResponse.getCount(13);
                ProtoBuf[] layerParams = new ProtoBuf[layerParamsNum];
                for (int j = layerParamsNum - 1; j >= 0; j--) {
                    layerParams[j] = layerResponse.getProtoBuf(13, j);
                }
                LayerService.this.notifyNewLayerInfo(layerId, layerParams);
            }
        }

        public void processLayerTile(ProtoBuf tileInfo, boolean replace) {
            ProtoBuf mapTile = tileInfo.getProtoBuf(1);
            Tile location = Tile.getTile((byte) mapTile.getInt(1), mapTile.getInt(2), mapTile.getInt(3), Zoom.getZoom(mapTile.getInt(4)));
            int areasNum = tileInfo.getCount(3);
            ClickableArea[] areas = new ClickableArea[areasNum];
            for (int j = areasNum - 1; j >= 0; j--) {
                areas[j] = new ClickableArea(tileInfo.getProtoBuf(3, j));
            }
            LayerTile layerTile = (LayerTile) LayerService.this.requestedTiles.get(location);
            if (layerTile == null) {
                layerTile = new LayerTile(location);
            } else if (layerTile.isComplete() && replace) {
                layerTile = new LayerTile(location);
            }
            if (layerTile.isComplete() && !replace) {
                layerTile.updateLayerTileData(areas);
            } else {
                layerTile.setLayerTileData(areas);
            }
            if (tileInfo.has(2)) {
                layerTile.setImage(tileInfo.getBytes(2));
            }
            synchronized (this) {
                LayerService.this.cache.put(location, layerTile);
                LayerService.this.requestedTiles.remove(location);
            }
            LayerService.this.notifyLayerTilesDirty();
        }
    }

    public interface TileUpdateObserver {
        LayerInfo getLayerInfo();

        void setLayerTilesDirty();

        void updateLayerInfo(String str, ProtoBuf[] protoBufArr);
    }

    public void clearTileCache() {
        this.cache.clear();
        this.requestedTiles.clear();
    }

    public boolean needFetchLayerTiles() {
        return this.observers.size() > 0;
    }

    public void notifyNewLayerInfo(String layerId, ProtoBuf[] params) {
        for (int i = this.observers.size() - 1; i >= 0; i--) {
            ((TileUpdateObserver) this.observers.elementAt(i)).updateLayerInfo(layerId, params);
        }
    }

    public void notifyLayerTilesDirty() {
        for (int i = this.observers.size() - 1; i >= 0; i--) {
            ((TileUpdateObserver) this.observers.elementAt(i)).setLayerTilesDirty();
        }
    }

    public void close() {
        StaticUtil.removeOutOfMemoryHandler(this);
    }

    /* JADX WARNING: Missing block: B:16:0x003e, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized LayerTile getTile(Tile tile, boolean fetch) {
        LayerTile layerTile = (LayerTile) this.cache.get(tile);
        LayerTile queuedTile = (LayerTile) this.requestedTiles.get(tile);
        if (layerTile == null) {
            if (queuedTile != null) {
                return queuedTile;
            }
            if (!fetch) {
                return null;
            }
            layerTile = new LayerTile(tile);
            requestTile(tile, layerTile);
            return layerTile;
        } else if (this.refreshMillis != -1) {
            long now = Config.getInstance().getClock().relativeTimeMillis();
            long tileDataTime = layerTile.getDataTime();
            long dataAge = now - tileDataTime;
            if (fetch && queuedTile == null && tileDataTime != Long.MIN_VALUE) {
                if ((dataAge <= this.refreshMillis ? 1 : null) == null) {
                    if (!((now > this.nextRefreshTime ? 1 : null) == null && this.request == null)) {
                        requestTile(tile, layerTile);
                    }
                }
            }
        }
    }

    private synchronized void requestTile(Tile tile, LayerTile layerTile) {
        if (isTileLocationValid(tile)) {
            if (this.request == null) {
                this.request = new LayerTileRequest();
            }
            this.request.addTile(layerTile);
            this.requestedTiles.put(tile, layerTile);
            return;
        }
        this.cache.put(tile, layerTile);
    }

    public synchronized void requestTiles() {
        if (this.request != null) {
            DataRequestDispatcher.getInstance().addDataRequest(this.request);
            this.request = null;
            this.nextRefreshTime = Config.getInstance().getClock().relativeTimeMillis() + this.refreshMillis;
        }
    }

    public synchronized void doCompact(Tile tile) {
        LayerTile layerTile = (LayerTile) this.cache.get(tile);
        if (layerTile != null) {
            if (layerTile.isComplete()) {
                layerTile.compact();
                this.cache.remove(tile);
            }
        }
    }

    public void handleOutOfMemory(boolean warning) {
        clearTileCache();
    }

    private boolean isTileLocationValid(Tile tile) {
        int zoomLevel = tile.getZoom().getZoomLevel();
        for (int i = this.observers.size() - 1; i >= 0; i--) {
            if (((TileUpdateObserver) this.observers.elementAt(i)).getLayerInfo().isValidZoomLevel(zoomLevel)) {
                return true;
            }
        }
        return false;
    }
}
