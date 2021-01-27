package ohos.sysappcomponents.contact;

import java.util.List;
import ohos.app.Context;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Group;
import ohos.sysappcomponents.contact.entity.Holder;

public class ContactsHelper {
    private ContactDataHelper mContactDataHelper;
    private Context mContext;

    public ContactsHelper(Context context) {
        this.mContext = context;
        this.mContactDataHelper = new ContactDataHelper(context);
    }

    public ContactsCollection queryContactsByPhoneNumber(String str, Holder holder, ContactAttributes contactAttributes) {
        return this.mContactDataHelper.queryContactsByPhoneNumber(str, holder, contactAttributes);
    }

    public ContactsCollection queryContactsByEmail(String str, Holder holder, ContactAttributes contactAttributes) {
        return this.mContactDataHelper.queryContactsByEmail(str, holder, contactAttributes);
    }

    public HoldersCollection queryHolders() {
        return this.mContactDataHelper.queryHolders();
    }

    public ContactsCollection queryContacts(Holder holder, ContactAttributes contactAttributes) {
        return this.mContactDataHelper.queryContacts(holder, contactAttributes);
    }

    public Contact queryMyCard(ContactAttributes contactAttributes) {
        return this.mContactDataHelper.queryMyCard(contactAttributes);
    }

    public Contact queryContact(String str, Holder holder, ContactAttributes contactAttributes) {
        return this.mContactDataHelper.queryContact(str, holder, contactAttributes);
    }

    public long addContact(Contact contact) {
        return this.mContactDataHelper.addContact(contact);
    }

    public boolean deleteContact(String str) {
        return this.mContactDataHelper.deleteContact(str);
    }

    public boolean updateContact(Contact contact, ContactAttributes contactAttributes) {
        return this.mContactDataHelper.updateContact(contact, contactAttributes);
    }

    public boolean isMyCard(long j) {
        return this.mContactDataHelper.isMyCard(j);
    }

    public List<Group> queryGroups(Holder holder) {
        return this.mContactDataHelper.queryGroups(holder);
    }

    public String queryKey(long j, Holder holder) {
        return this.mContactDataHelper.queryKey(j, holder);
    }

    public boolean isLocalContact(long j) {
        return this.mContactDataHelper.isLocalContact(j);
    }
}
