package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import android_maps_conflict_avoidance.com.google.googlenav.LayerPlacemark;
import android_maps_conflict_avoidance.com.google.googlenav.layer.ClickableArea;
import android_maps_conflict_avoidance.com.google.googlenav.layer.LayerItem;
import java.util.Hashtable;

public class LayerTile {
    private final Hashtable cache;
    private long dataTime = Long.MIN_VALUE;
    private GoogleImage image;
    private boolean isComplete;
    private final Tile location;

    public LayerTile(Tile location) {
        this.location = location;
        this.cache = new Hashtable();
        this.isComplete = false;
    }

    public long getDataTime() {
        return !isEmpty() ? this.dataTime : Config.getInstance().getClock().relativeTimeMillis();
    }

    public Tile getLocation() {
        return this.location;
    }

    public synchronized void compact() {
        this.cache.clear();
        this.image = null;
        this.isComplete = false;
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    public void setLayerTileData(ClickableArea[] areas) {
        this.cache.clear();
        if (areas != null) {
            updateLayerTileData(areas);
        } else {
            this.isComplete = false;
        }
    }

    public void updateLayerTileData(ClickableArea[] areas) {
        if (areas != null) {
            for (int i = areas.length - 1; i >= 0; i--) {
                ClickableArea area = areas[i];
                LayerItem[] items = area.getItems();
                for (int j = items.length - 1; j >= 0; j--) {
                    LayerItem item = items[j];
                    Hashtable layerCache = (Hashtable) this.cache.get(item.getLayerId());
                    LayerPlacemark layerPlacemark = null;
                    if (layerCache != null) {
                        layerPlacemark = (LayerPlacemark) layerCache.get(item.getItemId());
                    } else {
                        layerCache = new Hashtable();
                        this.cache.put(item.getLayerId(), layerCache);
                    }
                    if (layerPlacemark == null) {
                        layerCache.put(item.getItemId(), new LayerPlacemark(item, area));
                    }
                }
            }
            this.dataTime = Config.getInstance().getClock().relativeTimeMillis();
            this.isComplete = true;
        }
    }

    public boolean hasImage() {
        return this.image != null;
    }

    public synchronized GoogleImage getImage() {
        return this.image;
    }

    public synchronized void setImage(byte[] imageData) {
        if (imageData != null) {
            if (imageData.length != 0) {
                this.image = Config.getInstance().getImageFactory().createImage(imageData, 0, imageData.length);
            }
        }
        this.image = null;
    }
}
