package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.schema.Schema;

class Schema<T extends Schema> extends IndexData {
    private T item;

    Schema() {
    }

    /* access modifiers changed from: package-private */
    public void set(T t) {
        this.item = t;
    }

    public Long getDateCreate() {
        return super.getAsLong(CommonItem.DATE_CREATE);
    }

    public String getAlternateName() {
        return super.getAsString(CommonItem.ALTERNATE_NAME);
    }

    public String getCategory() {
        return super.getAsString(CommonItem.CATEGORY);
    }

    public String getDescription() {
        return super.getAsString(CommonItem.DESCRIPTION);
    }

    public String getIdentifier() {
        return super.getAsString(CommonItem.IDENTIFIER);
    }

    public String getKeywords() {
        return super.getAsString(CommonItem.KEYWORDS);
    }

    public String getName() {
        return super.getAsString(CommonItem.NAME);
    }

    public String getPotentialAction() {
        return super.getAsString(CommonItem.POTENTIAL_ACTION);
    }

    public String getReserved1() {
        return super.getAsString(CommonItem.RESERVED1);
    }

    public String getReserved2() {
        return super.getAsString(CommonItem.RESERVED2);
    }

    public String getSubTitle() {
        return super.getAsString(CommonItem.SUB_TITLE);
    }

    public String getThumbnailUrl() {
        return super.getAsString(CommonItem.THUMBNAIL_URL);
    }

    public String getTitle() {
        return super.getAsString(CommonItem.TITLE);
    }

    public String getUrl() {
        return super.getAsString(CommonItem.URL);
    }

    public T setAlternateName(String str) {
        super.put(CommonItem.ALTERNATE_NAME, str);
        return this.item;
    }

    public T setCategory(String str) {
        super.put(CommonItem.CATEGORY, str);
        return this.item;
    }

    public T setDateCreate(Long l) {
        super.put(CommonItem.DATE_CREATE, l);
        return this.item;
    }

    public T setDescription(String str) {
        super.put(CommonItem.DESCRIPTION, str);
        return this.item;
    }

    public T setIdentifier(String str) {
        super.put(CommonItem.IDENTIFIER, str);
        return this.item;
    }

    public T setKeywords(String str) {
        super.put(CommonItem.KEYWORDS, str);
        return this.item;
    }

    public T setName(String str) {
        super.put(CommonItem.NAME, str);
        return this.item;
    }

    public T setPotentialAction(String str) {
        super.put(CommonItem.POTENTIAL_ACTION, str);
        return this.item;
    }

    public T setReserved1(String str) {
        super.put(CommonItem.RESERVED1, str);
        return this.item;
    }

    public T setReserved2(String str) {
        super.put(CommonItem.RESERVED2, str);
        return this.item;
    }

    public T setSubTitle(String str) {
        super.put(CommonItem.SUB_TITLE, str);
        return this.item;
    }

    public T setThumbnailUrl(String str) {
        super.put(CommonItem.THUMBNAIL_URL, str);
        return this.item;
    }

    public T setTitle(String str) {
        super.put(CommonItem.TITLE, str);
        return this.item;
    }

    public T setUrl(String str) {
        super.put(CommonItem.URL, str);
        return this.item;
    }
}
