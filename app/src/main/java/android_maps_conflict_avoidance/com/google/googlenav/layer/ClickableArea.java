package android_maps_conflict_avoidance.com.google.googlenav.layer;

import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufUtil;
import android_maps_conflict_avoidance.com.google.map.MapPoint;

public class ClickableArea {
    private final MapPoint centerPoint;
    private final int height;
    private final int iconInactiveIndex;
    private final int iconIndex;
    private final int iconShadowIndex;
    private LayerItem[] items;
    private final int type;
    private final int width;

    public ClickableArea(ProtoBuf clickableArea) {
        this.type = clickableArea.getInt(1);
        ProtoBuf icon = clickableArea.getProtoBuf(3);
        ProtoBuf pixelPoint = icon.getProtoBuf(31);
        this.centerPoint = MapPoint.getMapPointFromXY(pixelPoint.getInt(1), pixelPoint.getInt(2), pixelPoint.getInt(3));
        this.width = ProtoBufUtil.getProtoValueOrZero(icon, 32);
        this.height = ProtoBufUtil.getProtoValueOrZero(icon, 33);
        this.iconIndex = (int) ProtoBufUtil.getProtoValueOrNegativeOne(icon, 34);
        this.iconInactiveIndex = ProtoBufUtil.getProtoValueOrDefault(icon, 35, this.iconIndex);
        this.iconShadowIndex = (int) ProtoBufUtil.getProtoValueOrNegativeOne(icon, 36);
        int size = clickableArea.getCount(2);
        this.items = new LayerItem[size];
        for (int i = 0; i < size; i++) {
            this.items[i] = new LayerItem(clickableArea.getProtoBuf(2, i));
        }
    }

    public MapPoint getCenterPoint() {
        return this.centerPoint;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getIconIndex() {
        return this.iconIndex;
    }

    public int getIconInactiveIndex() {
        return this.iconInactiveIndex;
    }

    public int getIconShadowIndex() {
        return this.iconShadowIndex;
    }

    public LayerItem[] getItems() {
        return this.items;
    }

    public String toString() {
        return "CenterPoint: " + this.centerPoint + ", " + this.items.length + " items";
    }
}
