package ohos.sysappcomponents.contact.creator;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.app.Context;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.ContactAttributes;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Name;
import ohos.sysappcomponents.contact.entity.Organization;
import ohos.sysappcomponents.contact.entity.PhoneNumber;
import ohos.sysappcomponents.contact.entity.Portrait;

public class ContactCreator {
    private static final int FAVORITE_CODE = 1;

    private ContactCreator() {
    }

    public static Contact createContactFromPhoneLookup(Context context, ResultSet resultSet, ContactAttributes contactAttributes) {
        Contact contact = new Contact();
        contact.setContactAttributes(contactAttributes);
        if (isNeedtoCreate(contactAttributes, ContactAttributes.Attribute.ATTR_PHONE)) {
            fillPhoneNumberFromPhoneLookup(contact, context, resultSet);
        }
        if (isNeedtoCreate(contactAttributes, ContactAttributes.Attribute.ATTR_NAME)) {
            fillDisplayName(contact, resultSet);
        }
        if (isNeedtoCreate(contactAttributes, ContactAttributes.Attribute.ATTR_PORTRAIT)) {
            fillPhoto(contact, resultSet);
        }
        if (isNeedtoCreate(contactAttributes, ContactAttributes.Attribute.ATTR_ORGANIZATION)) {
            fillOrganization(contact, resultSet);
        }
        fillLookupIndex(contact, resultSet);
        fillContactIdFromPhoneLookup(contact, resultSet);
        fillFavoriteState(contact, resultSet);
        return contact;
    }

    public static ArrayList<DataAbilityOperation> createOperations(Contact contact, Insertor insertor) {
        ArrayList<DataAbilityOperation> arrayList = new ArrayList<>();
        ValuesBucket valuesBucket = new ValuesBucket();
        valuesBucket.putNull("account_type");
        ValuesBucket valuesBucket2 = new ValuesBucket();
        valuesBucket2.putNull("account_name");
        arrayList.add(DataAbilityOperation.newInsertBuilder(Attribute.CommonDataKinds.CONTENT_URI).withValuesBucket(valuesBucket).withValuesBucket(valuesBucket2).build());
        if (insertor != null) {
            insertor.fillOperation(contact, arrayList, Insertor.OperationType.INSERT);
        }
        return arrayList;
    }

    public static String getKey(ResultSet resultSet) {
        int columnIndexForName;
        if (resultSet == null || !resultSet.goToNextRow() || (columnIndexForName = resultSet.getColumnIndexForName("lookup")) == -1) {
            return null;
        }
        return resultSet.getString(columnIndexForName);
    }

    private static boolean isNeedtoCreate(ContactAttributes contactAttributes, ContactAttributes.Attribute attribute) {
        if (contactAttributes == null) {
            return true;
        }
        return contactAttributes.isValid(attribute);
    }

    private static void fillPhoneNumberFromPhoneLookup(Contact contact, Context context, ResultSet resultSet) {
        PhoneNumber createFromPhoneLookup;
        if (!Stream.of(contact, context, resultSet).anyMatch($$Lambda$ContactCreator$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE) && (createFromPhoneLookup = PhoneNumberCreator.createFromPhoneLookup(context, resultSet)) != null) {
            contact.addPhoneNumber(createFromPhoneLookup);
        }
    }

    private static void fillContactIdFromPhoneLookup(Contact contact, ResultSet resultSet) {
        int columnIndexForName;
        if (!Stream.of(contact, resultSet).anyMatch($$Lambda$ContactCreator$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE) && (columnIndexForName = resultSet.getColumnIndexForName("contact_id")) != -1) {
            contact.setId(resultSet.getLong(columnIndexForName));
        }
    }

    private static void fillDisplayName(Contact contact, ResultSet resultSet) {
        Name createFullNameFromPhoneLookUp;
        if (!Stream.of(contact, resultSet).anyMatch($$Lambda$ContactCreator$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE) && (createFullNameFromPhoneLookUp = NameCreator.createFullNameFromPhoneLookUp(resultSet)) != null) {
            contact.setName(createFullNameFromPhoneLookUp);
        }
    }

    private static void fillOrganization(Contact contact, ResultSet resultSet) {
        Organization createOrganizationFromPhoneLookup;
        if (!Stream.of(contact, resultSet).anyMatch($$Lambda$ContactCreator$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE) && (createOrganizationFromPhoneLookup = OrganizationCreator.createOrganizationFromPhoneLookup(resultSet)) != null) {
            contact.setOrganization(createOrganizationFromPhoneLookup);
        }
    }

    private static void fillLookupIndex(Contact contact, ResultSet resultSet) {
        int columnIndexForName;
        if (!Stream.of(contact, resultSet).anyMatch($$Lambda$ContactCreator$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE) && (columnIndexForName = resultSet.getColumnIndexForName("lookup")) != -1) {
            contact.setKey(resultSet.getString(columnIndexForName));
        }
    }

    private static void fillPhoto(Contact contact, ResultSet resultSet) {
        Portrait createFromPhoneLookup;
        if (!Stream.of(contact, resultSet).anyMatch($$Lambda$ContactCreator$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE) && (createFromPhoneLookup = PhotoCreator.createFromPhoneLookup(resultSet)) != null) {
            contact.setPortrait(createFromPhoneLookup);
        }
    }

    private static void fillFavoriteState(Contact contact, ResultSet resultSet) {
        int columnIndexForName;
        boolean z = false;
        if (!Stream.of(contact, resultSet).anyMatch($$Lambda$ContactCreator$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE) && (columnIndexForName = resultSet.getColumnIndexForName(Attribute.PhoneFinder.FAVORITE_STATE)) != -1) {
            if (resultSet.getInt(columnIndexForName) == 1) {
                z = true;
            }
            contact.setFavorite(z);
        }
    }
}
