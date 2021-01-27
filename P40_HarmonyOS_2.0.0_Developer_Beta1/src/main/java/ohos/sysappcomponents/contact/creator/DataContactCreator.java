package ohos.sysappcomponents.contact.creator;

import java.util.Map;
import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.ContactAttributes;
import ohos.sysappcomponents.contact.entity.Contact;

public class DataContactCreator {
    private static final int INVALID_COLUMN = -1;
    private static final long INVALID_CONTACT_ID = -1;
    private static String TAG = "HolderContactCreator";

    public static Contact createContact(ContactAttributes contactAttributes, ResultSet resultSet, Context context) {
        Contact contact = new Contact();
        contact.setContactAttributes(contactAttributes);
        contact.setId(-1);
        setDataContactInfo(contactAttributes, resultSet, contact, context);
        return contact;
    }

    private static void setDataContactInfo(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        while (resultSet.goToNextRow()) {
            int columnIndexForName = resultSet.getColumnIndexForName("contact_id");
            if (columnIndexForName != -1) {
                long j = resultSet.getLong(columnIndexForName);
                if (contact.getId() == -1) {
                    contact.setId(j);
                    contact.setKey(getKey(resultSet));
                }
                if (j == contact.getId()) {
                    int columnIndexForName2 = resultSet.getColumnIndexForName("mimetype");
                    if (columnIndexForName2 != -1) {
                        String string = resultSet.getString(columnIndexForName2);
                        Map<String, ContactAttributes.Attribute> mimeTypeMap = Attribute.getMimeTypeMap();
                        if (mimeTypeMap != null) {
                            setContactValue(mimeTypeMap.get(string), contactAttributes, resultSet, contact, context);
                        }
                    }
                } else {
                    resultSet.goToPreviousRow();
                    return;
                }
            }
        }
    }

    private static void setContactValue(ContactAttributes.Attribute attribute, ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (attribute != null) {
            switch (attribute) {
                case ATTR_EMAIL:
                    addEmailAttr(contactAttributes, resultSet, contact, context);
                    return;
                case ATTR_IM:
                    addImAttr(contactAttributes, resultSet, contact, context);
                    return;
                case ATTR_NICKNAME:
                    addNickNameAttr(contactAttributes, resultSet, contact, context);
                    return;
                case ATTR_ORGANIZATION:
                    addOrganizationAttr(contactAttributes, resultSet, contact);
                    return;
                case ATTR_PHONE:
                    addPhoneAttr(contactAttributes, resultSet, contact, context);
                    return;
                case ATTR_SIP_ADDRESS:
                    addSipAddressAttr(contactAttributes, resultSet, contact, context);
                    return;
                case ATTR_NAME:
                    addNameAttr(contactAttributes, resultSet, contact);
                    return;
                case ATTR_POSTAL_ADDRESS:
                    addPostalAddressAttr(contactAttributes, resultSet, contact, context);
                    return;
                case ATTR_IDENTITY:
                    addIdentityAttr(contactAttributes, resultSet, contact);
                    return;
                case ATTR_PORTRAIT:
                    addDataPhotoAttr(contactAttributes, resultSet, contact);
                    return;
                case ATTR_GROUP_MEMBERSHIP:
                    addGroupMembershipAttr(contactAttributes, resultSet, contact);
                    return;
                case ATTR_NOTE:
                    addNoteAttr(contactAttributes, resultSet, contact);
                    return;
                case ATTR_CONTACT_EVENT:
                    addEventAttr(contactAttributes, resultSet, contact, context);
                    return;
                case ATTR_WEBSITE:
                    addWebsiteAttr(contactAttributes, resultSet, contact);
                    return;
                case ATTR_RELATION:
                    addRelationAttr(contactAttributes, resultSet, contact, context);
                    return;
                case ATTR_CONTACT_MISC:
                    return;
                case ATTR_CAMCARD_PHOTO:
                    addCamCardAttr(contactAttributes, resultSet, contact);
                    return;
                case ATTR_HICALL_DEVICE:
                    addHiCallDeviceAttr(contactAttributes, resultSet, contact);
                    return;
                default:
                    return;
            }
        }
    }

    private static void addEmailAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_EMAIL)) {
            contact.addEmail(EmailCreator.createEmailFromDataContact(context, resultSet));
        }
    }

    private static void addImAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_IM)) {
            contact.addImAddress(ImCreator.createImFromDataContact(context, resultSet));
        }
    }

    private static void addNickNameAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_NICKNAME)) {
            contact.setNickName(NickNameCreator.createImFromDataContact(resultSet));
        }
    }

    private static void addOrganizationAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_ORGANIZATION)) {
            contact.setOrganization(OrganizationCreator.createOrganizationFromDataContact(resultSet));
        }
    }

    private static void addPhoneAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_PHONE)) {
            contact.addPhoneNumber(PhoneNumberCreator.createPhoneNumberFromDataContact(context, resultSet));
        }
    }

    private static void addSipAddressAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_SIP_ADDRESS)) {
            contact.addSipAddress(SipAddressCreator.createSipAddressFromDataContact(context, resultSet));
        }
    }

    private static void addNameAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_NAME)) {
            contact.setName(NameCreator.createNameFromDataContact(resultSet));
        }
    }

    private static void addPostalAddressAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_POSTAL_ADDRESS)) {
            contact.addPostalAddress(PostalAddressCreator.createPostalAddressFromDataContact(context, resultSet));
        }
    }

    private static void addIdentityAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_IDENTITY)) {
            contact.setIdentity(IdentityCreator.createIdentityFromDataContact(resultSet));
        }
    }

    private static void addDataPhotoAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_PORTRAIT)) {
            contact.setPortrait(PhotoCreator.createPortraitFromDataContact(resultSet));
        }
    }

    private static void addGroupMembershipAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_GROUP_MEMBERSHIP)) {
            contact.addGroup(GroupCreator.createGroupFromDataContact(resultSet));
        }
    }

    private static void addNoteAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_NOTE)) {
            contact.setNote(NoteCreator.createNoteFromDataContact(resultSet));
        }
    }

    private static void addEventAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_CONTACT_EVENT)) {
            contact.addEvent(EventCreator.createEventFromDataContact(context, resultSet));
        }
    }

    private static void addWebsiteAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_WEBSITE)) {
            contact.addWebsite(WebsiteCreator.createWebsiteFromDataContact(resultSet));
        }
    }

    private static void addRelationAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact, Context context) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_RELATION)) {
            contact.addRelation(RelationCreator.createRelationFromDataContact(context, resultSet));
        }
    }

    private static void addCamCardAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_CAMCARD_PHOTO)) {
            contact.setCamCard(PhotoCreator.createPortraitFromDataContact(resultSet));
        }
    }

    private static void addHiCallDeviceAttr(ContactAttributes contactAttributes, ResultSet resultSet, Contact contact) {
        if (isNeedAddAttr(contactAttributes, ContactAttributes.Attribute.ATTR_HICALL_DEVICE)) {
            contact.addHiCallDevice(HiCallDeviceCreator.createHiCallDeviceFromDataContact(resultSet));
        }
    }

    private static boolean isNeedAddAttr(ContactAttributes contactAttributes, ContactAttributes.Attribute attribute) {
        if (contactAttributes == null) {
            return true;
        }
        return contactAttributes.isValid(attribute);
    }

    private static String getKey(ResultSet resultSet) {
        int columnIndexForName = resultSet.getColumnIndexForName("lookup");
        if (columnIndexForName == -1) {
            return null;
        }
        return resultSet.getString(columnIndexForName);
    }
}
