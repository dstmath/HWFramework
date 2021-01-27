package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class DocumentItem extends Schema<DocumentItem> {
    public static final String AUTHOR = "author";
    public static final String CONTENT = "content";
    public static final String DOCUMENT_CATEGORY = "documentCategory";
    public static final String PATH = "path";
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

    public static List<IndexForm> getDocumentSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.DocumentItem.AnonymousClass1 */

            {
                add(new IndexForm(DocumentItem.AUTHOR, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(DocumentItem.DOCUMENT_CATEGORY, IndexType.SORTED, false, true, true));
                add(new IndexForm("size", IndexType.NO, false, true, false));
                add(new IndexForm("content", IndexType.ANALYZED, false, false, true));
                add(new IndexForm(DocumentItem.PATH, IndexType.NO, false, true, false));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public DocumentItem() {
        super.set(this);
    }

    public DocumentItem setAuthor(String str) {
        super.put(AUTHOR, str);
        return this;
    }

    public DocumentItem setDocumentCategory(String str) {
        super.put(DOCUMENT_CATEGORY, str);
        return this;
    }

    public DocumentItem setSize(Integer num) {
        super.put("size", num);
        return this;
    }

    public DocumentItem setContent(String str) {
        super.put("content", str);
        return this;
    }

    public DocumentItem setPath(String str) {
        super.put(PATH, str);
        return this;
    }

    public String getAuthor() {
        return super.getAsString(AUTHOR);
    }

    public String getDocumentCategory() {
        return super.getAsString(DOCUMENT_CATEGORY);
    }

    public Integer getSize() {
        return super.getAsInteger("size");
    }

    public String getContent() {
        return super.getAsString("content");
    }

    public String getPath() {
        return super.getAsString(PATH);
    }
}
