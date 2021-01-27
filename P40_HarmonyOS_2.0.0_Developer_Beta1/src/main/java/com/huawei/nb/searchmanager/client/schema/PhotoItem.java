package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class PhotoItem extends Schema<PhotoItem> {
    public static final String FEATURE = "feature";
    public static final String HEIGHT = "height";
    public static final String HOLIDAY = "holiday";
    public static final String OCR_TEXT = "ocrText";
    public static final String PHOTO_CATEGORY = "photoCategory";
    public static final String SHOOTING_DATE = "shootingDate";
    public static final String SHOOTING_MODE = "shootingMode";
    public static final String SIZE = "size";
    public static final String TAG = "tag";
    public static final String WIDTH = "width";

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

    public static List<IndexForm> getPhotoSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.PhotoItem.AnonymousClass1 */

            {
                add(new IndexForm(PhotoItem.PHOTO_CATEGORY, IndexType.SORTED, false, true, true));
                add(new IndexForm(PhotoItem.OCR_TEXT, IndexType.ANALYZED, false, false, true));
                add(new IndexForm(PhotoItem.HOLIDAY, IndexType.SORTED, false, true, true));
                add(new IndexForm(PhotoItem.TAG, IndexType.SORTED, false, true, true));
                add(new IndexForm(PhotoItem.FEATURE, IndexType.SORTED, false, true, true));
                add(new IndexForm(PhotoItem.SHOOTING_DATE, IndexType.SORTED, false, true, true));
                add(new IndexForm(PhotoItem.SHOOTING_MODE, IndexType.SORTED, false, true, true));
                add(new IndexForm("size", IndexType.NO, false, true, false));
                add(new IndexForm(PhotoItem.WIDTH, IndexType.NO, false, true, false));
                add(new IndexForm(PhotoItem.HEIGHT, IndexType.NO, false, true, false));
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

    public PhotoItem() {
        super.set(this);
    }

    public PhotoItem setPhotoCategory(String str) {
        super.put(PHOTO_CATEGORY, str);
        return this;
    }

    public PhotoItem setOcrText(String str) {
        super.put(OCR_TEXT, str);
        return this;
    }

    public PhotoItem setHoliday(String str) {
        super.put(HOLIDAY, str);
        return this;
    }

    public PhotoItem setTag(String str) {
        super.put(TAG, str);
        return this;
    }

    public PhotoItem setFeature(String str) {
        super.put(FEATURE, str);
        return this;
    }

    public PhotoItem setShootingDate(String str) {
        super.put(SHOOTING_DATE, str);
        return this;
    }

    public PhotoItem setShootingMode(String str) {
        super.put(SHOOTING_MODE, str);
        return this;
    }

    public PhotoItem setLongitude(Double d) {
        super.put(PlaceItem.LONGITUDE, d);
        return this;
    }

    public PhotoItem setLatitude(Double d) {
        super.put(PlaceItem.LATITUDE, d);
        return this;
    }

    public PhotoItem setCountry(String str) {
        super.put(PlaceItem.COUNTRY, str);
        return this;
    }

    public PhotoItem setProvince(String str) {
        super.put(PlaceItem.PROVINCE, str);
        return this;
    }

    public PhotoItem setCity(String str) {
        super.put(PlaceItem.CITY, str);
        return this;
    }

    public PhotoItem setDistrict(String str) {
        super.put(PlaceItem.DISTRICT, str);
        return this;
    }

    public PhotoItem setRoad(String str) {
        super.put(PlaceItem.ROAD, str);
        return this;
    }

    public PhotoItem setSize(Integer num) {
        super.put("size", num);
        return this;
    }

    public PhotoItem setHeight(Integer num) {
        super.put(HEIGHT, num);
        return this;
    }

    public PhotoItem setWidth(Integer num) {
        super.put(WIDTH, num);
        return this;
    }

    public String getPhotoCategory() {
        return super.getAsString(PHOTO_CATEGORY);
    }

    public String getOcrText() {
        return super.getAsString(OCR_TEXT);
    }

    public String getHoliday() {
        return super.getAsString(HOLIDAY);
    }

    public String getTag() {
        return super.getAsString(TAG);
    }

    public String getFeature() {
        return super.getAsString(FEATURE);
    }

    public String getShootingDate() {
        return super.getAsString(SHOOTING_DATE);
    }

    public String getShootingMode() {
        return super.getAsString(SHOOTING_MODE);
    }

    public Double getLatitude() {
        return super.getAsDouble(PlaceItem.LATITUDE);
    }

    public Double getLongitude() {
        return super.getAsDouble(PlaceItem.LONGITUDE);
    }

    public String getCountry() {
        return super.getAsString(PlaceItem.COUNTRY);
    }

    public String getProvince() {
        return super.getAsString(PlaceItem.PROVINCE);
    }

    public String getCity() {
        return super.getAsString(PlaceItem.CITY);
    }

    public String getDistrict() {
        return super.getAsString(PlaceItem.DISTRICT);
    }

    public String getRoad() {
        return super.getAsString(PlaceItem.ROAD);
    }

    public Integer getSize() {
        return super.getAsInteger("size");
    }

    public Integer getHeight() {
        return super.getAsInteger(HEIGHT);
    }

    public Integer getWidth() {
        return super.getAsInteger(WIDTH);
    }
}
