package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class VideoItem extends Schema<VideoItem> {
    public static final String ACTOR = "actor";
    public static final String DOWNLOAD_COUNT = "downloadCount";
    public static final String DURATION = "duration";
    public static final String RATING = "rating";
    public static final String SIZE = "size";
    public static final String WATCH_DURATION = "watchDuration";

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getAlternateName() {
        return super.getAlternateName();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getCategory() {
        return super.getCategory();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ Long getDateCreate() {
        return super.getDateCreate();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getDescription() {
        return super.getDescription();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getIdentifier() {
        return super.getIdentifier();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getKeywords() {
        return super.getKeywords();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getName() {
        return super.getName();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getPotentialAction() {
        return super.getPotentialAction();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getReserved1() {
        return super.getReserved1();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getReserved2() {
        return super.getReserved2();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getSubTitle() {
        return super.getSubTitle();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getThumbnailUrl() {
        return super.getThumbnailUrl();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getTitle() {
        return super.getTitle();
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public /* bridge */ /* synthetic */ String getUrl() {
        return super.getUrl();
    }

    public static List<IndexForm> getVideoSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.VideoItem.AnonymousClass1 */

            {
                add(new IndexForm(VideoItem.ACTOR, IndexType.SORTED, false, true, true));
                add(new IndexForm("duration", IndexType.NO, false, true, false));
                add(new IndexForm(VideoItem.WATCH_DURATION, IndexType.NO, false, true, false));
                add(new IndexForm("size", IndexType.NO, false, true, false));
                add(new IndexForm("downloadCount", IndexType.LONG, false, true, false));
                add(new IndexForm("rating", IndexType.DOUBLE, false, true, false));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public VideoItem() {
        super.set(this);
    }

    public VideoItem setActor(String str) {
        super.put(ACTOR, str);
        return this;
    }

    public VideoItem setDuration(Integer num) {
        super.put("duration", num);
        return this;
    }

    public VideoItem setWatchDuration(Integer num) {
        super.put(WATCH_DURATION, num);
        return this;
    }

    public VideoItem setRating(Double d) {
        super.put("rating", d);
        return this;
    }

    public VideoItem setDownloadCount(Long l) {
        super.put("downloadCount", l);
        return this;
    }

    public VideoItem setSize(Integer num) {
        super.put("size", num);
        return this;
    }

    public String getActor() {
        return super.getAsString(ACTOR);
    }

    public Integer getDuration() {
        return super.getAsInteger("duration");
    }

    public Integer getWatchDuration() {
        return super.getAsInteger(WATCH_DURATION);
    }

    public Double getRating() {
        return super.getAsDouble("rating");
    }

    public Long getDownloadCount() {
        return super.getAsLong("downloadCount");
    }

    public Integer getSize() {
        return super.getAsInteger("size");
    }
}
