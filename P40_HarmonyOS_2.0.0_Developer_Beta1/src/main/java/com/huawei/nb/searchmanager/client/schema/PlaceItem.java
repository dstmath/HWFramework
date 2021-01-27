package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class PlaceItem extends Schema<PlaceItem> {
    public static final String CITY = "city";
    public static final String COUNTRY = "country";
    public static final String DISTRICT = "district";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String POSTAL_CODE = "postalCode";
    public static final String PROVINCE = "province";
    public static final String ROAD = "road";

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

    public static List<IndexForm> getPlaceSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.PlaceItem.AnonymousClass1 */

            {
                add(new IndexForm(PlaceItem.POSTAL_CODE, IndexType.SORTED, false, true, true));
                add(new IndexForm(PlaceItem.COUNTRY, IndexType.SORTED, false, true, true));
                add(new IndexForm(PlaceItem.PROVINCE, IndexType.SORTED, false, true, true));
                add(new IndexForm(PlaceItem.CITY, IndexType.SORTED, false, true, true));
                add(new IndexForm(PlaceItem.DISTRICT, IndexType.SORTED, false, true, true));
                add(new IndexForm(PlaceItem.ROAD, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(PlaceItem.LATITUDE, IndexType.DOUBLE, false, true, false));
                add(new IndexForm(PlaceItem.LONGITUDE, IndexType.DOUBLE, false, true, false));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public PlaceItem() {
        super.set(this);
    }

    public PlaceItem setLatitude(Double d) {
        super.put(LATITUDE, d);
        return this;
    }

    public PlaceItem setLongitude(Double d) {
        super.put(LONGITUDE, d);
        return this;
    }

    public PlaceItem setCountry(String str) {
        super.put(COUNTRY, str);
        return this;
    }

    public PlaceItem setProvince(String str) {
        super.put(PROVINCE, str);
        return this;
    }

    public PlaceItem setCity(String str) {
        super.put(CITY, str);
        return this;
    }

    public PlaceItem setDistrict(String str) {
        super.put(DISTRICT, str);
        return this;
    }

    public PlaceItem setRoad(String str) {
        super.put(ROAD, str);
        return this;
    }

    public PlaceItem setPostalCode(Long l) {
        super.put(POSTAL_CODE, l);
        return this;
    }

    public Double getLatitude() {
        return super.getAsDouble(LATITUDE);
    }

    public Double getLongitude() {
        return super.getAsDouble(LONGITUDE);
    }

    public Long getPostalCode() {
        return super.getAsLong(POSTAL_CODE);
    }

    public String getCountry() {
        return super.getAsString(COUNTRY);
    }

    public String getProvince() {
        return super.getAsString(PROVINCE);
    }

    public String getCity() {
        return super.getAsString(CITY);
    }

    public String getDistrict() {
        return super.getAsString(DISTRICT);
    }

    public String getRoad() {
        return super.getAsString(ROAD);
    }
}
