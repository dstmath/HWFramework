package ohos.sysappcomponents.contact.collection;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.ContactAttributes;
import ohos.sysappcomponents.contact.ContactsCollection;
import ohos.sysappcomponents.contact.creator.ContactCreator;
import ohos.sysappcomponents.contact.entity.Contact;

public class PhoneLookupCollection extends ContactsCollection {
    private static final String TAG = PhoneLookupCollection.class.getSimpleName();

    public PhoneLookupCollection(ResultSet resultSet, ContactAttributes contactAttributes, Context context) {
        this.mResultSet = resultSet;
        this.mAttributes = contactAttributes;
        this.mContext = context;
    }

    @Override // ohos.sysappcomponents.contact.ContactsCollection
    public Contact next() {
        if (this.mResultSet != null && this.mResultSet.goToNextRow()) {
            return ContactCreator.createContactFromPhoneLookup(this.mContext, this.mResultSet, this.mAttributes);
        }
        return null;
    }
}
