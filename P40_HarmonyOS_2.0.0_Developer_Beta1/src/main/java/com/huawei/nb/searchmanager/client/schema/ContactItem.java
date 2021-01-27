package com.huawei.nb.searchmanager.client.schema;

import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.IndexType;
import java.util.ArrayList;
import java.util.List;

public final class ContactItem extends Schema<ContactItem> {
    public static final String ADDRESS = "address";
    public static final String BIRTH_DATE = "birthDate";
    public static final String EMAIL = "email";
    public static final String GENDER = "gender";
    public static final String PHONE = "phone";
    public static final String WORK_LOCATION = "workLocation";

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

    public static List<IndexForm> getContactSchema() {
        AnonymousClass1 r0 = new ArrayList<IndexForm>() {
            /* class com.huawei.nb.searchmanager.client.schema.ContactItem.AnonymousClass1 */

            {
                add(new IndexForm(ContactItem.PHONE, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(ContactItem.EMAIL, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(ContactItem.BIRTH_DATE, IndexType.LONG, false, true, false));
                add(new IndexForm(ContactItem.GENDER, IndexType.SORTED_NO_ANALYZED, false, true, false));
                add(new IndexForm(ContactItem.ADDRESS, IndexType.ANALYZED, false, true, true));
                add(new IndexForm(ContactItem.WORK_LOCATION, IndexType.ANALYZED, false, true, true));
            }
        };
        r0.addAll(CommonItem.getCommonSchema());
        return r0;
    }

    public ContactItem() {
        super.set(this);
    }

    public ContactItem setPhone(String str) {
        super.put(PHONE, str);
        return this;
    }

    public ContactItem setEmail(String str) {
        super.put(EMAIL, str);
        return this;
    }

    public ContactItem setBirthDate(Long l) {
        super.put(BIRTH_DATE, l);
        return this;
    }

    public ContactItem setGender(String str) {
        super.put(GENDER, str);
        return this;
    }

    public ContactItem setAddress(String str) {
        super.put(ADDRESS, str);
        return this;
    }

    public ContactItem setWorkLocation(String str) {
        super.put(WORK_LOCATION, str);
        return this;
    }

    public String getPhone() {
        return super.getAsString(PHONE);
    }

    public String getEmail() {
        return super.getAsString(EMAIL);
    }

    public Long getBirthDate() {
        return super.getAsLong(BIRTH_DATE);
    }

    public String getGender() {
        return super.getAsString(GENDER);
    }

    public String getAddress() {
        return super.getAsString(ADDRESS);
    }

    public String getWorkLocation() {
        return super.getAsString(WORK_LOCATION);
    }
}
