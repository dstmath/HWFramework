package ohos.sysappcomponents.contact;

import android.content.Context;
import android.provider.ContactsContract;
import java.util.HashMap;
import java.util.Map;
import ohos.abilityshell.utils.AbilityContextUtils;
import ohos.net.UriConverter;
import ohos.sysappcomponents.contact.ContactAttributes;
import ohos.utils.net.Uri;

public class Attribute {
    public static final int INVALID_INDEX = -1;
    public static final String MIMETYPE_ID = "mimetype_id";
    private static Map<String, ContactAttributes.Attribute> mMimeTypeAttribute = new HashMap();

    public static class Data {
        public static final String CONTACT_ID = "contact_id";
        public static final Uri CONTENT_URI = UriConverter.convertToZidaneContentUri(ContactsContract.Data.CONTENT_URI, "");
        public static final String DATA1 = "data1";
        public static final String DATA10 = "data10";
        public static final String DATA11 = "data11";
        public static final String DATA12 = "data12";
        public static final String DATA13 = "data13";
        public static final String DATA14 = "data14";
        public static final String DATA15 = "data15";
        public static final String DATA2 = "data2";
        public static final String DATA3 = "data3";
        public static final String DATA4 = "data4";
        public static final String DATA5 = "data5";
        public static final String DATA6 = "data6";
        public static final String DATA7 = "data7";
        public static final String DATA8 = "data8";
        public static final String DATA9 = "data9";
        public static final String ID = "_id";
        public static final String MIMETYPE = "mimetype";
        public static final String NAME = "data";
        public static final String RAW_CONTACT_ID = "raw_contact_id";
    }

    public static final class Groups {
        public static final Uri CONTENT_URI = UriConverter.convertToZidaneContentUri(ContactsContract.Groups.CONTENT_URI, "");
        public static final String GROUP_ID = "_id";
        public static final String TITLE = "title";
    }

    public static final class HiCallDevice {
        public static final String CONTENT_ITEM_TYPE = "vnd.huawei.cursor.item/hicall_device";
        public static final String DEVICE_COM_ID = "data4";
        public static final String DEVICE_ID = "data13";
        public static final String DEVICE_MODEL = "data10";
        public static final String DEVICE_NICK_NAME = "data11";
        public static final String DEVICE_ORDINAL = "data9";
        public static final String DEVICE_PHONE_NUMBER = "data1";
        public static final String DEVICE_PROFILE = "data7";
        public static final String DEVICE_TYPE = "data5";
        public static final String IS_PRIVATE = "data6";
        public static final String IS_SAME_VIBRATION = "data8";
        public static final String USER_NAME = "data12";
    }

    protected interface LabelColumns {
        public static final String DATA = "data1";
        public static final String LABEL = "data3";
        public static final String TYPE = "data2";
    }

    public static class PhoneFinder {
        public static final String COMPANY = "company";
        public static final String CONTACT_ID = "contact_id";
        public static final Uri CONTENT_FILTER_URI = UriConverter.convertToZidaneContentUri(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, "");
        public static final String DISPLAY_NAME = "display_name";
        public static final String FAVORITE_STATE = "starred";
        public static final String LABEL = "label";
        public static final String LOOKUP_KEY = "lookup";
        public static final String NUMBER = "number";
        public static final String PHOTO_FILE_ID = "photo_file_id";
        public static final String PHOTO_URI = "photo_uri";
        public static final String TITLE = "title";
        public static final String TYPE = "type";
    }

    public static class Profile {
        public static final Uri CONTENT_URI = UriConverter.convertToZidaneContentUri(ContactsContract.Profile.CONTENT_URI, "");
        public static final long MIN_ID = 9223372034707292160L;
    }

    static {
        mMimeTypeAttribute.put(CommonDataKinds.Email.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_EMAIL);
        mMimeTypeAttribute.put(CommonDataKinds.ImAddress.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_IM);
        mMimeTypeAttribute.put(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_NICKNAME);
        mMimeTypeAttribute.put(CommonDataKinds.Organization.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_ORGANIZATION);
        mMimeTypeAttribute.put(CommonDataKinds.PhoneNumber.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_PHONE);
        mMimeTypeAttribute.put(CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_SIP_ADDRESS);
        mMimeTypeAttribute.put(CommonDataKinds.Name.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_NAME);
        mMimeTypeAttribute.put(CommonDataKinds.PostalAddress.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_POSTAL_ADDRESS);
        mMimeTypeAttribute.put(CommonDataKinds.Identity.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_IDENTITY);
        mMimeTypeAttribute.put(CommonDataKinds.Portrait.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_PORTRAIT);
        mMimeTypeAttribute.put(CommonDataKinds.Group.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_GROUP_MEMBERSHIP);
        mMimeTypeAttribute.put(CommonDataKinds.Note.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_NOTE);
        mMimeTypeAttribute.put(CommonDataKinds.Event.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_CONTACT_EVENT);
        mMimeTypeAttribute.put(CommonDataKinds.Website.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_WEBSITE);
        mMimeTypeAttribute.put(CommonDataKinds.Relation.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_RELATION);
        mMimeTypeAttribute.put("vnd.com.google.cursor.item/contact_misc", ContactAttributes.Attribute.ATTR_CONTACT_MISC);
        mMimeTypeAttribute.put("vnd.android.cursor.item/vnd.com.huawei.camcard.photo", ContactAttributes.Attribute.ATTR_CAMCARD_PHOTO);
        mMimeTypeAttribute.put(HiCallDevice.CONTENT_ITEM_TYPE, ContactAttributes.Attribute.ATTR_HICALL_DEVICE);
    }

    private Attribute() {
    }

    public static class Holder {
        public static final String ACCOUNT_NAME = "accountName";
        public static final String ACCOUNT_TYPE = "accountType";
        public static final Uri CONTENT_URI = UriConverter.convertToZidaneContentUri(ContactsContract.Directory.CONTENT_URI, "");
        public static final long DEFAULT = 0;
        public static final String DISPLAY_NAME = "displayName";
        public static final long ENTERPRISE_DEFAULT = 1000000000;
        public static final long ENTERPRISE_LOCAL_INVISIBLE = 1000000001;
        public static final String EXPORT_SUPPORT = "exportSupport";
        public static final String HOLDER_AUTHORITY = "authority";
        public static final String ID = "_id";
        public static final long LOCAL_INVISIBLE = 1;
        public static final String PACKAGE_NAME = "packageName";
        public static final String PHOTO_SUPPORT = "photoSupport";
        public static final String SHORTCUT_SUPPORT = "shortcutSupport";
        public static final String TYPE_RESOURCE_ID = "typeResourceId";

        public static boolean isRemoteHolderId(long j) {
            return ContactsContract.Directory.isRemoteDirectoryId(j);
        }

        public static boolean isEnterpriseHolderId(long j) {
            return ContactsContract.Directory.isEnterpriseDirectoryId(j);
        }
    }

    public static class Contacts {
        public static final String CONTACT_ID = "name_raw_contact_id";
        public static final Uri CONTENT_LOOKUP_URI = UriConverter.convertToZidaneContentUri(ContactsContract.Contacts.CONTENT_LOOKUP_URI, "");
        public static final Uri CONTENT_URI = UriConverter.convertToZidaneContentUri(ContactsContract.Contacts.CONTENT_URI, "");
        public static final String LOOKUP_KEY = "lookup";

        public static boolean isEnterpriseContactId(long j) {
            return ContactsContract.Contacts.isEnterpriseContactId(j);
        }
    }

    public static Context getAplatFromContext(ohos.app.Context context) {
        if (context == null) {
            return null;
        }
        Object androidContext = AbilityContextUtils.getAndroidContext(context);
        if (androidContext instanceof Context) {
            return (Context) androidContext;
        }
        return null;
    }

    public static Map<String, ContactAttributes.Attribute> getMimeTypeMap() {
        return mMimeTypeAttribute;
    }

    public static final class CommonDataKinds {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final Uri AUTHORITY = UriConverter.convertToZidaneContentUri(ContactsContract.AUTHORITY_URI, "");
        public static final Uri CONTENT_URI = UriConverter.convertToZidaneContentUri(ContactsContract.RawContacts.CONTENT_URI, "");
        public static final String MIMETYPE = "mimetype";

        public static final class Group {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/group_membership";
            public static final String GROUP_ID = "data1";
        }

        public static final class Identity {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/identity";
            public static final String IDENTITY = "data1";
            public static final String NAMESPACE = "data2";
        }

        public static final class Name {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/name";
            public static final String DISPLAY_NAME = "data1";
            public static final String FAMILY_NAME = "data3";
            public static final String GIVEN_NAME = "data2";
            public static final String MIDDLE_NAME = "data5";
            public static final String PHONETIC_FAMILY_NAME = "data9";
            public static final String PHONETIC_GIVEN_NAME = "data7";
            public static final String PHONETIC_MIDDLE_NAME = "data8";
            public static final String PREFIX = "data4";
            public static final String SUFFIX = "data6";
        }

        public static final class Nickname {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/nickname";
            public static final String NICKNAME = "data1";
        }

        public static final class Note {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/note";
            public static final String NOTE = "data1";
        }

        public static final class Organization {
            public static final String COMPANY = "data1";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/organization";
            public static final String TITLE = "data4";
        }

        public static final class Portrait {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/photo";
            public static final String PHOTO = "data15";
            public static final String PHOTO_FILE_ID = "data14";
        }

        public static final class Website {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/website";
            public static final String WEBSITE_ADDRESS = "data1";
        }

        public static final class PhoneNumber implements LabelColumns {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone_v2";
            public static final String NUMBER = "data1";
            public static final int NUM_ASSISTANT = 19;
            public static final int NUM_CALLBACK = 8;
            public static final int NUM_CAR = 9;
            public static final int NUM_COMPANY_MAIN = 10;
            public static final int NUM_FAX_HOME = 5;
            public static final int NUM_FAX_WORK = 4;
            public static final int NUM_HOME = 1;
            public static final int NUM_ISDN = 11;
            public static final int NUM_MAIN = 12;
            public static final int NUM_MMS = 20;
            public static final int NUM_MOBILE = 2;
            public static final int NUM_OTHER = 7;
            public static final int NUM_OTHER_FAX = 13;
            public static final int NUM_PAGER = 6;
            public static final int NUM_RADIO = 14;
            public static final int NUM_TELEX = 15;
            public static final int NUM_TTY_TDD = 16;
            public static final int NUM_WORK = 3;
            public static final int NUM_WORK_MOBILE = 17;
            public static final int NUM_WORK_PAGER = 18;

            public static String getLabelNameResId(ohos.app.Context context, int i) {
                Context aplatFromContext;
                if (context == null || (aplatFromContext = Attribute.getAplatFromContext(context)) == null) {
                    return "";
                }
                return aplatFromContext.getString(ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(i));
            }
        }

        public static final class SipAddress implements LabelColumns {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/sip_address";
            public static final String SIP_ADDRESS = "data1";
            public static final int SIP_HOME = 1;
            public static final int SIP_OTHER = 3;
            public static final int SIP_WORK = 2;

            public static String getLabelNameResId(ohos.app.Context context, int i) {
                Context aplatFromContext;
                if (context == null || (aplatFromContext = Attribute.getAplatFromContext(context)) == null) {
                    return "";
                }
                return aplatFromContext.getString(ContactsContract.CommonDataKinds.SipAddress.getTypeLabelResource(i));
            }
        }

        public static final class Email implements LabelColumns {
            public static final String ADDRESS = "data1";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/email_v2";
            public static final String DISPLAY_NAME = "data4";
            public static final int EMAIL_HOME = 1;
            public static final int EMAIL_MOBILE = 4;
            public static final int EMAIL_OTHER = 3;
            public static final int EMAIL_WORK = 2;

            public static String getLabelNameResId(ohos.app.Context context, int i) {
                Context aplatFromContext;
                if (context == null || (aplatFromContext = Attribute.getAplatFromContext(context)) == null) {
                    return "";
                }
                return aplatFromContext.getString(ContactsContract.CommonDataKinds.Email.getTypeLabelResource(i));
            }
        }

        public static final class ImAddress implements LabelColumns {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/im";
            public static final String CUSTOM_PROTOCOL = "data6";
            public static final String IM_ADDRESS = "data1";
            public static final int IM_AIM = 0;
            public static final int IM_CUSTOM = -1;
            public static final int IM_ICQ = 6;
            public static final int IM_JABBER = 7;
            public static final int IM_MSN = 1;
            public static final int IM_QQ = 4;
            public static final int IM_SKYPE = 3;
            public static final int IM_YAHOO = 2;
            public static final String PROTOCOL = "data5";

            public static String getLabelNameResId(ohos.app.Context context, int i) {
                Context aplatFromContext;
                if (context == null || (aplatFromContext = Attribute.getAplatFromContext(context)) == null) {
                    return "";
                }
                return aplatFromContext.getString(ContactsContract.CommonDataKinds.Im.getTypeLabelResource(i));
            }
        }

        public static final class Relation implements LabelColumns {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/relation";
            public static final String NAME = "data1";
            public static final int RELATION_ASSISTANT = 1;
            public static final int RELATION_BROTHER = 2;
            public static final int RELATION_CHILD = 3;
            public static final int RELATION_DOMESTIC_PARTNER = 4;
            public static final int RELATION_FATHER = 5;
            public static final int RELATION_FRIEND = 6;
            public static final int RELATION_MANAGER = 7;
            public static final int RELATION_MOTHER = 8;
            public static final int RELATION_PARENT = 9;
            public static final int RELATION_PARTNER = 10;
            public static final int RELATION_REFERRED_BY = 11;
            public static final int RELATION_RELATIVE = 12;
            public static final int RELATION_SISTER = 13;
            public static final int RELATION_SPOUSE = 14;

            public static String getLabelNameResId(ohos.app.Context context, int i) {
                Context aplatFromContext;
                if (context == null || (aplatFromContext = Attribute.getAplatFromContext(context)) == null) {
                    return "";
                }
                return aplatFromContext.getString(ContactsContract.CommonDataKinds.Relation.getTypeLabelResource(i));
            }
        }

        public static final class Event implements LabelColumns {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
            public static final int EVENT_ANNIVERSARY = 1;
            public static final int EVENT_BIRTHDAY = 3;
            public static final int EVENT_OTHER = 2;
            public static final String SPECIAL_DATE = "data1";

            public static String getLabelNameResId(ohos.app.Context context, int i) {
                Context aplatFromContext;
                if (context == null || (aplatFromContext = Attribute.getAplatFromContext(context)) == null) {
                    return "";
                }
                return aplatFromContext.getString(ContactsContract.CommonDataKinds.Event.getTypeResource(Integer.valueOf(i)));
            }
        }

        public static final class PostalAddress implements LabelColumns {
            public static final int ADDR_HOME = 1;
            public static final int ADDR_OTHER = 3;
            public static final int ADDR_WORK = 2;
            public static final String CITY = "data7";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postal-address_v2";
            public static final String COUNTRY = "data10";
            public static final String NEIGHBORHOOD = "data6";
            public static final String POBOX = "data5";
            public static final String POSTAL_ADDRESS = "data1";
            public static final String POSTCODE = "data9";
            public static final String REGION = "data8";
            public static final String STREET = "data4";

            public static String getLabelNameResId(ohos.app.Context context, int i) {
                Context aplatFromContext;
                if (context == null || (aplatFromContext = Attribute.getAplatFromContext(context)) == null) {
                    return "";
                }
                return aplatFromContext.getString(ContactsContract.CommonDataKinds.StructuredPostal.getTypeLabelResource(i));
            }
        }
    }
}
