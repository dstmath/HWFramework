package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class EventItem extends Schema<EventItem> {
    public static final String END_DATE = "endDate";
    public static final String START_DATE = "startDate";

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

    public static List<IndexForm> getEventSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.EventItem.AnonymousClass1 */

            {
                add(new IndexForm(EventItem.START_DATE, IndexType.LONG, false, true, false));
                add(new IndexForm(EventItem.END_DATE, IndexType.LONG, false, true, false));
                add(new IndexForm(PlaceItem.ROAD, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(PlaceItem.LATITUDE, IndexType.DOUBLE, false, true, false));
                add(new IndexForm(PlaceItem.LONGITUDE, IndexType.DOUBLE, false, true, false));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public EventItem() {
        super.set(this);
    }

    public EventItem setStartDate(Long l) {
        super.put(START_DATE, l);
        return this;
    }

    public EventItem setEndDate(Long l) {
        super.put(END_DATE, l);
        return this;
    }

    public EventItem setLocation(String str) {
        super.put(PlaceItem.ROAD, str);
        return this;
    }

    public EventItem setLongitude(Double d) {
        super.put(PlaceItem.LONGITUDE, d);
        return this;
    }

    public EventItem setLatitude(Double d) {
        super.put(PlaceItem.LATITUDE, d);
        return this;
    }

    public Long getStartDate() {
        return super.getAsLong(START_DATE);
    }

    public Long getEndDate() {
        return super.getAsLong(END_DATE);
    }

    public String getLocation() {
        return super.getAsString(PlaceItem.ROAD);
    }

    public Double getLongitude() {
        return super.getAsDouble(PlaceItem.LONGITUDE);
    }

    public Double getLatitude() {
        return super.getAsDouble(PlaceItem.LATITUDE);
    }
}
