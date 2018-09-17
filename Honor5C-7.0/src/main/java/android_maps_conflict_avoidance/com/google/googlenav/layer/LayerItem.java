package android_maps_conflict_avoidance.com.google.googlenav.layer;

import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;

public class LayerItem {
    private final ProtoBuf activitySnippet;
    private final boolean isRoutable;
    private final String itemId;
    private final String layerId;
    private final String name;
    private final int ranking;
    private final ProtoBuf rating;
    private final String snippet;

    public LayerItem(ProtoBuf layerItem) {
        int i = 0;
        this.layerId = layerItem.getString(1);
        this.itemId = layerItem.getString(2);
        this.name = !layerItem.has(3) ? "" : layerItem.getString(3);
        this.snippet = !layerItem.has(4) ? "" : layerItem.getString(4);
        this.isRoutable = !layerItem.has(5) ? false : layerItem.getBool(5);
        this.rating = !layerItem.has(6) ? null : layerItem.getProtoBuf(6);
        if (layerItem.has(7)) {
            i = layerItem.getInt(7);
        }
        this.ranking = i;
        this.activitySnippet = !layerItem.has(9) ? null : layerItem.getProtoBuf(9);
    }

    public String getLayerId() {
        return this.layerId;
    }

    public String getItemId() {
        return this.itemId;
    }

    public String getName() {
        return this.name;
    }

    public String getSnippet() {
        return this.snippet;
    }

    public ProtoBuf getBuzzSnippet() {
        return this.activitySnippet;
    }

    public String toString() {
        return "layerId: " + this.layerId + ", itemId: " + this.itemId + ", name: " + this.name + ", snippet: " + this.snippet;
    }
}
