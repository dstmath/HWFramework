package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class AppItem extends Schema<AppItem> {
    public static final String APP_CATEGORY = "appCategory";
    public static final String COMMENT = "comment";
    public static final String DOWNLOAD_COUNT = "downloadCount";
    public static final String RATING = "rating";
    public static final String SIZE = "size";

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

    public static List<IndexForm> getAppSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.AppItem.AnonymousClass1 */

            {
                add(new IndexForm(AppItem.COMMENT, IndexType.ANALYZED, false, false, true));
                add(new IndexForm(AppItem.APP_CATEGORY, IndexType.SORTED, false, true, true));
                add(new IndexForm("size", IndexType.NO, false, true, false));
                add(new IndexForm("downloadCount", IndexType.LONG, false, true, false));
                add(new IndexForm("rating", IndexType.DOUBLE, false, true, false));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public AppItem() {
        super.set(this);
    }

    public AppItem setAppCategory(String str) {
        super.put(APP_CATEGORY, str);
        return this;
    }

    public AppItem setComment(String str) {
        super.put(COMMENT, str);
        return this;
    }

    public AppItem setSize(Integer num) {
        super.put("size", num);
        return this;
    }

    public AppItem setRating(Double d) {
        super.put("rating", d);
        return this;
    }

    public AppItem setDownloadCount(Long l) {
        super.put("downloadCount", l);
        return this;
    }

    public String getAppCategory() {
        return super.getAsString(APP_CATEGORY);
    }

    public String getComment() {
        return super.getAsString(COMMENT);
    }

    public Integer getSize() {
        return super.getAsInteger("size");
    }

    public Double getRating() {
        return super.getAsDouble("rating");
    }

    public Long getDownloadCount() {
        return super.getAsLong("downloadCount");
    }
}
