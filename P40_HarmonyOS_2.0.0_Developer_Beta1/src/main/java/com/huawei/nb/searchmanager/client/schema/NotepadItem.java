package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class NotepadItem extends Schema<NotepadItem> {
    public static final String ATTACHMENT = "attachment";
    public static final String CONTENT = "content";
    public static final String NOTEPAD_CATEGORY = "notepadCategory";

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

    public static List<IndexForm> getNotepadSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.NotepadItem.AnonymousClass1 */

            {
                add(new IndexForm(NotepadItem.NOTEPAD_CATEGORY, IndexType.SORTED, false, true, true));
                add(new IndexForm("attachment", IndexType.ANALYZED, false, false, true));
                add(new IndexForm("content", IndexType.ANALYZED, false, false, true));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public NotepadItem() {
        super.set(this);
    }

    public NotepadItem setNotepadCategory(String str) {
        super.put(NOTEPAD_CATEGORY, str);
        return this;
    }

    public NotepadItem setAttachment(String str) {
        super.put("attachment", str);
        return this;
    }

    public NotepadItem setContent(String str) {
        super.put("content", str);
        return this;
    }

    public String getNotepadCategory() {
        return super.getAsString(NOTEPAD_CATEGORY);
    }

    public String getAttachment() {
        return super.getAsString("attachment");
    }

    public String getContent() {
        return super.getAsString("content");
    }
}
