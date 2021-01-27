package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class MessageItem extends Schema<MessageItem> {
    public static final String ATTACHMENT = "attachment";
    public static final String CONTENT = "content";
    public static final String RECIPIENT_NAME = "recipientName";
    public static final String RECIPIENT_PHONE = "recipientPhone";
    public static final String SENDER_NAME = "senderName";
    public static final String SENDER_PHONE = "senderPhone";

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

    public static List<IndexForm> getMessageSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.MessageItem.AnonymousClass1 */

            {
                add(new IndexForm("senderName", IndexType.ANALYZED, false, true, true));
                add(new IndexForm(MessageItem.SENDER_PHONE, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(MessageItem.RECIPIENT_NAME, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(MessageItem.RECIPIENT_PHONE, IndexType.ANALYZED, false, true, true));
                add(new IndexForm("content", IndexType.ANALYZED, false, true, true));
                add(new IndexForm("attachment", IndexType.ANALYZED, false, false, true));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public MessageItem() {
        super.set(this);
    }

    public MessageItem setSenderName(String str) {
        super.put("senderName", str);
        return this;
    }

    public MessageItem setSenderPhone(String str) {
        super.put(SENDER_PHONE, str);
        return this;
    }

    public MessageItem setRecipientName(String str) {
        super.put(RECIPIENT_NAME, str);
        return this;
    }

    public MessageItem setRecipientPhone(String str) {
        super.put(RECIPIENT_PHONE, str);
        return this;
    }

    public MessageItem setContent(String str) {
        super.put("content", str);
        return this;
    }

    public MessageItem setAttachment(String str) {
        super.put("attachment", str);
        return this;
    }

    public String getSenderName() {
        return super.getAsString("senderName");
    }

    public String getSenderPhone() {
        return super.getAsString(SENDER_PHONE);
    }

    public String getRecipientName() {
        return super.getAsString(RECIPIENT_NAME);
    }

    public String getRecipientPhone() {
        return super.getAsString(RECIPIENT_PHONE);
    }

    public String getContent() {
        return super.getAsString("content");
    }

    public String getAttachment() {
        return super.getAsString("attachment");
    }
}
