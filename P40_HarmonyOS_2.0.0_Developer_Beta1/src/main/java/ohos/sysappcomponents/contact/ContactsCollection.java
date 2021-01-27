package ohos.sysappcomponents.contact;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.collection.Collection;
import ohos.sysappcomponents.contact.collection.DataContactsCollection;
import ohos.sysappcomponents.contact.collection.PhoneLookupCollection;
import ohos.sysappcomponents.contact.entity.Contact;

public abstract class ContactsCollection extends Collection {
    protected ContactAttributes mAttributes;
    protected Context mContext;

    public enum Type {
        PHONE_LOOKUP,
        DATA_CONTACT
    }

    public abstract Contact next();

    /* renamed from: ohos.sysappcomponents.contact.ContactsCollection$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$sysappcomponents$contact$ContactsCollection$Type = new int[Type.values().length];

        static {
            try {
                $SwitchMap$ohos$sysappcomponents$contact$ContactsCollection$Type[Type.PHONE_LOOKUP.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$sysappcomponents$contact$ContactsCollection$Type[Type.DATA_CONTACT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public static ContactsCollection createContactsCollection(ResultSet resultSet, ContactAttributes contactAttributes, Context context, Type type) {
        int i = AnonymousClass1.$SwitchMap$ohos$sysappcomponents$contact$ContactsCollection$Type[type.ordinal()];
        if (i == 1) {
            return new PhoneLookupCollection(resultSet, contactAttributes, context);
        }
        if (i == 2) {
            return new DataContactsCollection(resultSet, contactAttributes, context);
        }
        LogUtil.error(ContactsCollection.class.getSimpleName(), "Wrong type for construct ContactsCollection.");
        return null;
    }
}
