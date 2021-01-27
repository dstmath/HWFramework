package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class NoticeItem extends Schema<NoticeItem> {
    public static final String CONTENT = "content";
    public static final String IMPORTANCE = "importance";
    public static final String NOTICE_CATEGORY = "noticeCategory";
    public static final String NOTICE_DATE = "noticeDate";
    public static final String REPEAT_STATUS = "repeatStatus";

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

    public static List<IndexForm> getNoticeSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.NoticeItem.AnonymousClass1 */

            {
                add(new IndexForm(NoticeItem.NOTICE_CATEGORY, IndexType.SORTED, false, true, true));
                add(new IndexForm("content", IndexType.ANALYZED, false, false, true));
                add(new IndexForm(NoticeItem.NOTICE_DATE, IndexType.LONG, false, true, false));
                add(new IndexForm(NoticeItem.REPEAT_STATUS, IndexType.SORTED, false, true, true));
                add(new IndexForm(NoticeItem.IMPORTANCE, IndexType.SORTED, false, true, true));
                add(new IndexForm(PlaceItem.ROAD, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(PlaceItem.LATITUDE, IndexType.DOUBLE, false, true, false));
                add(new IndexForm(PlaceItem.LONGITUDE, IndexType.DOUBLE, false, true, false));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public NoticeItem() {
        super.set(this);
    }

    public NoticeItem setNoticeCategory(String str) {
        super.put(NOTICE_CATEGORY, str);
        return this;
    }

    public NoticeItem setContent(String str) {
        super.put("content", str);
        return this;
    }

    public NoticeItem setNoticeDate(Long l) {
        super.put(NOTICE_DATE, l);
        return this;
    }

    public NoticeItem setRepeatStatus(String str) {
        super.put(REPEAT_STATUS, str);
        return this;
    }

    public NoticeItem setImportance(String str) {
        super.put(IMPORTANCE, str);
        return this;
    }

    public NoticeItem setLocation(String str) {
        super.put(PlaceItem.ROAD, str);
        return this;
    }

    public NoticeItem setLongitude(Double d) {
        super.put(PlaceItem.LONGITUDE, d);
        return this;
    }

    public NoticeItem setLatitude(Double d) {
        super.put(PlaceItem.LATITUDE, d);
        return this;
    }

    public String getNoticeCategory() {
        return super.getAsString(NOTICE_CATEGORY);
    }

    public String getContent() {
        return super.getAsString("content");
    }

    public Long getNoticeDate() {
        return super.getAsLong(NOTICE_DATE);
    }

    public String getRepeatStatus() {
        return super.getAsString(REPEAT_STATUS);
    }

    public String getImportance() {
        return super.getAsString(IMPORTANCE);
    }

    public String getLocation() {
        return super.getAsString(PlaceItem.ROAD);
    }

    public Double getLatitude() {
        return super.getAsDouble(PlaceItem.LATITUDE);
    }

    public Double getLongitude() {
        return super.getAsDouble(PlaceItem.LONGITUDE);
    }
}
