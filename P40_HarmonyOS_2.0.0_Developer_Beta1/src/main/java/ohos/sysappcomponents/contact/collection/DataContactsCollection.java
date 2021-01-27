package ohos.sysappcomponents.contact.collection;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.ContactAttributes;
import ohos.sysappcomponents.contact.ContactsCollection;
import ohos.sysappcomponents.contact.creator.DataContactCreator;
import ohos.sysappcomponents.contact.entity.Contact;

public class DataContactsCollection extends ContactsCollection {
    public DataContactsCollection(ResultSet resultSet, ContactAttributes contactAttributes, Context context) {
        this.mResultSet = resultSet;
        this.mAttributes = contactAttributes;
        this.mContext = context;
    }

    @Override // ohos.sysappcomponents.contact.ContactsCollection
    public Contact next() {
        if (this.mResultSet == null || this.mResultSet.isEnded()) {
            return null;
        }
        return DataContactCreator.createContact(this.mAttributes, this.mResultSet, this.mContext);
    }
}
