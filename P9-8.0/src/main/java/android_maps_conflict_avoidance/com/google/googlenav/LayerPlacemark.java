package android_maps_conflict_avoidance.com.google.googlenav;

import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufUtil;
import android_maps_conflict_avoidance.com.google.googlenav.layer.ClickableArea;
import android_maps_conflict_avoidance.com.google.googlenav.layer.LayerItem;
import java.util.Vector;

public class LayerPlacemark extends Placemark {
    private int activeIconIndex;
    private long authorGaiaId;
    private String authorName;
    private String clusterDocId;
    private String content;
    private long distanceSquaredToTarget;
    private int height;
    private final String itemId;
    private final String layerId;
    private String locationName;
    private Vector mediaUrls;
    private boolean needToFetchDetails = false;
    private long nextRefreshTime;
    private int normalIconIndex;
    private int numberComments;
    private int shadowIconIndex;
    private String snippet;
    private long timestamp;
    private int width;

    public LayerPlacemark(LayerItem item, ClickableArea area) {
        super(area.getCenterPoint(), item.getName());
        this.layerId = item.getLayerId();
        this.itemId = item.getItemId();
        this.snippet = item.getSnippet();
        this.activeIconIndex = area.getIconIndex();
        this.normalIconIndex = area.getIconInactiveIndex();
        this.shadowIconIndex = area.getIconShadowIndex();
        this.width = area.getWidth();
        this.height = area.getHeight();
        this.needToFetchDetails = true;
        this.nextRefreshTime = -1;
        this.distanceSquaredToTarget = Long.MAX_VALUE;
        ProtoBuf activitySnippet = item.getBuzzSnippet();
        if (activitySnippet != null) {
            ProtoBuf author = ProtoBufUtil.getSubProtoOrNull(activitySnippet, 2);
            if (author != null) {
                this.authorName = ProtoBufUtil.getProtoValueOrEmpty(author, 4);
                this.authorGaiaId = ProtoBufUtil.getProtoLongValueOrZero(author, 3);
            }
            this.timestamp = ProtoBufUtil.getProtoLongValueOrZero(activitySnippet, 5);
            this.numberComments = ProtoBufUtil.getProtoValueOrZero(activitySnippet, 6);
            this.clusterDocId = ProtoBufUtil.getProtoValueOrEmpty(activitySnippet, 7);
            this.content = ProtoBufUtil.getProtoValueOrEmpty(activitySnippet, 1);
            this.locationName = ProtoBufUtil.getProtoValueOrEmpty(activitySnippet, 8);
            if (ProtoBufUtil.getProtoValueOrFalse(activitySnippet, 9)) {
                int size = activitySnippet.getCount(10);
                this.mediaUrls = new Vector(size);
                for (int i = 0; i < size; i++) {
                    this.mediaUrls.addElement(activitySnippet.getString(10, i));
                }
            }
        }
    }
}
