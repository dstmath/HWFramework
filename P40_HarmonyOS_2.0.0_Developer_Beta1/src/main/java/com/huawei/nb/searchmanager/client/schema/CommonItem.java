package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class CommonItem extends Schema<CommonItem> {
    public static final String ALTERNATE_NAME = "alternateName";
    public static final String CATEGORY = "category";
    public static final String DATE_CREATE = "dateCreate";
    public static final String DESCRIPTION = "description";
    public static final String IDENTIFIER = "identifier";
    public static final String KEYWORDS = "keywords";
    public static final String NAME = "name";
    public static final String POTENTIAL_ACTION = "potentialAction";
    public static final String RESERVED1 = "reserved1";
    public static final String RESERVED2 = "reserved2";
    public static final String SUB_TITLE = "subTitle";
    public static final String THUMBNAIL_URL = "thumbnailUrl";
    public static final String TITLE = "title";
    public static final String URL = "url";

    public static List<IndexForm> getCommonSchema() {
        return new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.CommonItem.AnonymousClass1 */

            {
                add(new IndexForm(CommonItem.IDENTIFIER, IndexType.NO_ANALYZED, true, true, false));
                add(new IndexForm(CommonItem.TITLE, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(CommonItem.NAME, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(CommonItem.CATEGORY, IndexType.SORTED, false, true, true));
                add(new IndexForm(CommonItem.DATE_CREATE, IndexType.LONG, false, true, false));
                add(new IndexForm(CommonItem.SUB_TITLE, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(CommonItem.ALTERNATE_NAME, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(CommonItem.KEYWORDS, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(CommonItem.DESCRIPTION, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(CommonItem.THUMBNAIL_URL, IndexType.NO, false, true, false));
                add(new IndexForm(CommonItem.POTENTIAL_ACTION, IndexType.NO, false, true, false));
                add(new IndexForm(CommonItem.URL, IndexType.NO, false, true, false));
                add(new IndexForm(CommonItem.RESERVED1, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(CommonItem.RESERVED2, IndexType.NO_ANALYZED, false, true, false));
            }
        };
    }

    public CommonItem() {
        super.set(this);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setIdentifier(String str) {
        super.put(IDENTIFIER, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setName(String str) {
        super.put(NAME, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setAlternateName(String str) {
        super.put(ALTERNATE_NAME, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setTitle(String str) {
        super.put(TITLE, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setSubTitle(String str) {
        super.put(SUB_TITLE, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setCategory(String str) {
        super.put(CATEGORY, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setKeywords(String str) {
        super.put(KEYWORDS, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setDescription(String str) {
        super.put(DESCRIPTION, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setDateCreate(Long l) {
        super.put(DATE_CREATE, l);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setThumbnailUrl(String str) {
        super.put(THUMBNAIL_URL, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setUrl(String str) {
        super.put(URL, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setPotentialAction(String str) {
        super.put(POTENTIAL_ACTION, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setReserved1(String str) {
        super.put(RESERVED1, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public CommonItem setReserved2(String str) {
        super.put(RESERVED2, str);
        return this;
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getIdentifier() {
        return super.getAsString(IDENTIFIER);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getName() {
        return super.getAsString(NAME);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getAlternateName() {
        return super.getAsString(ALTERNATE_NAME);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getTitle() {
        return super.getAsString(TITLE);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getSubTitle() {
        return super.getAsString(SUB_TITLE);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getCategory() {
        return super.getAsString(CATEGORY);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getKeywords() {
        return super.getAsString(KEYWORDS);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getDescription() {
        return super.getAsString(DESCRIPTION);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public Long getDateCreate() {
        return super.getAsLong(DATE_CREATE);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getThumbnailUrl() {
        return super.getAsString(THUMBNAIL_URL);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getUrl() {
        return super.getAsString(URL);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getPotentialAction() {
        return super.getAsString(POTENTIAL_ACTION);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getReserved1() {
        return super.getAsString(RESERVED1);
    }

    @Override // com.huawei.nb.searchmanager.client.schema.Schema
    public String getReserved2() {
        return super.getAsString(RESERVED2);
    }
}
