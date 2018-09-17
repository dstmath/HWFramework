package android.provider;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorEntityIterator;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Rect;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Contacts.People.Phones;
import android.provider.SyncStateContract.Columns;
import android.provider.SyncStateContract.Helpers;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import com.android.internal.R;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public final class ContactsContract {
    public static final String AUTHORITY = "com.android.contacts";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.contacts");
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";
    public static final String DEFERRED_SNIPPETING = "deferred_snippeting";
    public static final String DEFERRED_SNIPPETING_QUERY = "deferred_snippeting_query";
    public static final String DIRECTORY_PARAM_KEY = "directory";
    public static final String HIDDEN_COLUMN_PREFIX = "x_";
    public static final String LIMIT_PARAM_KEY = "limit";
    public static final String PRIMARY_ACCOUNT_NAME = "name_for_primary_account";
    public static final String PRIMARY_ACCOUNT_TYPE = "type_for_primary_account";
    public static final String REMOVE_DUPLICATE_ENTRIES = "remove_duplicate_entries";
    public static final String STREQUENT_PHONE_ONLY = "strequent_phone_only";

    public static final class AggregationExceptions implements BaseColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/aggregation_exception";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/aggregation_exception";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "aggregation_exceptions");
        public static final String RAW_CONTACT_ID1 = "raw_contact_id1";
        public static final String RAW_CONTACT_ID2 = "raw_contact_id2";
        public static final String TYPE = "type";
        public static final int TYPE_AUTOMATIC = 0;
        public static final int TYPE_KEEP_SEPARATE = 2;
        public static final int TYPE_KEEP_TOGETHER = 1;

        private AggregationExceptions() {
        }
    }

    public static final class Authorization {
        public static final String AUTHORIZATION_METHOD = "authorize";
        public static final String KEY_AUTHORIZED_URI = "authorized_uri";
        public static final String KEY_URI_TO_AUTHORIZE = "uri_to_authorize";
    }

    protected interface BaseSyncColumns {
        public static final String SYNC1 = "sync1";
        public static final String SYNC2 = "sync2";
        public static final String SYNC3 = "sync3";
        public static final String SYNC4 = "sync4";
    }

    protected interface DataColumns {
        public static final String CARRIER_PRESENCE = "carrier_presence";
        public static final int CARRIER_PRESENCE_VT_CAPABLE = 1;
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
        public static final String DATA_VERSION = "data_version";
        public static final String HASH_ID = "hash_id";
        public static final String IS_PRIMARY = "is_primary";
        public static final String IS_READ_ONLY = "is_read_only";
        public static final String IS_SUPER_PRIMARY = "is_super_primary";
        public static final String MIMETYPE = "mimetype";
        public static final String RAW_CONTACT_ID = "raw_contact_id";
        public static final String RES_PACKAGE = "res_package";
        public static final String SYNC1 = "data_sync1";
        public static final String SYNC2 = "data_sync2";
        public static final String SYNC3 = "data_sync3";
        public static final String SYNC4 = "data_sync4";
    }

    protected interface StatusColumns {
        public static final int AVAILABLE = 5;
        public static final int AWAY = 2;
        public static final int CAPABILITY_HAS_CAMERA = 4;
        public static final int CAPABILITY_HAS_VIDEO = 2;
        public static final int CAPABILITY_HAS_VOICE = 1;
        public static final String CHAT_CAPABILITY = "chat_capability";
        public static final int DO_NOT_DISTURB = 4;
        public static final int IDLE = 3;
        public static final int INVISIBLE = 1;
        public static final int OFFLINE = 0;
        public static final String PRESENCE = "mode";
        @Deprecated
        public static final String PRESENCE_CUSTOM_STATUS = "status";
        @Deprecated
        public static final String PRESENCE_STATUS = "mode";
        public static final String STATUS = "status";
        public static final String STATUS_ICON = "status_icon";
        public static final String STATUS_LABEL = "status_label";
        public static final String STATUS_RES_PACKAGE = "status_res_package";
        public static final String STATUS_TIMESTAMP = "status_ts";
    }

    protected interface RawContactsColumns {
        public static final String ACCOUNT_TYPE_AND_DATA_SET = "account_type_and_data_set";
        public static final String AGGREGATION_MODE = "aggregation_mode";
        public static final String BACKUP_ID = "backup_id";
        public static final String CONTACT_ID = "contact_id";
        public static final String DATA_SET = "data_set";
        public static final String DELETED = "deleted";
        public static final String METADATA_DIRTY = "metadata_dirty";
        public static final String RAW_CONTACT_IS_READ_ONLY = "raw_contact_is_read_only";
        public static final String RAW_CONTACT_IS_USER_PROFILE = "raw_contact_is_user_profile";
    }

    protected interface ContactsColumns {
        public static final String CONTACT_LAST_UPDATED_TIMESTAMP = "contact_last_updated_timestamp";
        public static final String DISPLAY_NAME = "display_name";
        public static final String HAS_PHONE_NUMBER = "has_phone_number";
        public static final String IN_DEFAULT_DIRECTORY = "in_default_directory";
        public static final String IN_VISIBLE_GROUP = "in_visible_group";
        public static final String IS_USER_PROFILE = "is_user_profile";
        public static final String LOOKUP_KEY = "lookup";
        public static final String NAME_RAW_CONTACT_ID = "name_raw_contact_id";
        public static final String PHOTO_FILE_ID = "photo_file_id";
        public static final String PHOTO_ID = "photo_id";
        public static final String PHOTO_THUMBNAIL_URI = "photo_thumb_uri";
        public static final String PHOTO_URI = "photo_uri";
    }

    protected interface ContactNameColumns {
        public static final String DISPLAY_NAME_ALTERNATIVE = "display_name_alt";
        public static final String DISPLAY_NAME_PRIMARY = "display_name";
        public static final String DISPLAY_NAME_SOURCE = "display_name_source";
        public static final String PHONETIC_NAME = "phonetic_name";
        public static final String PHONETIC_NAME_STYLE = "phonetic_name_style";
        public static final String SORT_KEY_ALTERNATIVE = "sort_key_alt";
        public static final String SORT_KEY_PRIMARY = "sort_key";
    }

    protected interface ContactOptionsColumns {
        public static final String CUSTOM_RINGTONE = "custom_ringtone";
        public static final String LAST_TIME_CONTACTED = "last_time_contacted";
        public static final String LR_LAST_TIME_CONTACTED = "last_time_contacted";
        public static final String LR_TIMES_CONTACTED = "times_contacted";
        public static final String PINNED = "pinned";
        public static final String RAW_LAST_TIME_CONTACTED = "x_last_time_contacted";
        public static final String RAW_TIMES_CONTACTED = "x_times_contacted";
        public static final String SEND_TO_VOICEMAIL = "send_to_voicemail";
        public static final String STARRED = "starred";
        public static final String TIMES_CONTACTED = "times_contacted";
    }

    protected interface ContactStatusColumns {
        public static final String CONTACT_CHAT_CAPABILITY = "contact_chat_capability";
        public static final String CONTACT_PRESENCE = "contact_presence";
        public static final String CONTACT_STATUS = "contact_status";
        public static final String CONTACT_STATUS_ICON = "contact_status_icon";
        public static final String CONTACT_STATUS_LABEL = "contact_status_label";
        public static final String CONTACT_STATUS_RES_PACKAGE = "contact_status_res_package";
        public static final String CONTACT_STATUS_TIMESTAMP = "contact_status_ts";
    }

    protected interface DataUsageStatColumns {
        public static final String LAST_TIME_USED = "last_time_used";
        public static final String LR_LAST_TIME_USED = "last_time_used";
        public static final String LR_TIMES_USED = "times_used";
        public static final String RAW_LAST_TIME_USED = "x_last_time_used";
        public static final String RAW_TIMES_USED = "x_times_used";
        public static final String TIMES_USED = "times_used";
    }

    protected interface DataColumnsWithJoins extends BaseColumns, DataColumns, StatusColumns, RawContactsColumns, ContactsColumns, ContactNameColumns, ContactOptionsColumns, ContactStatusColumns, DataUsageStatColumns {
    }

    interface ContactCounts {
        public static final String EXTRA_ADDRESS_BOOK_INDEX = "android.provider.extra.ADDRESS_BOOK_INDEX";
        public static final String EXTRA_ADDRESS_BOOK_INDEX_COUNTS = "android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS";
        public static final String EXTRA_ADDRESS_BOOK_INDEX_TITLES = "android.provider.extra.ADDRESS_BOOK_INDEX_TITLES";
    }

    public static final class CommonDataKinds {
        public static final String PACKAGE_COMMON = "common";

        public interface BaseTypes {
            public static final int TYPE_CUSTOM = 0;
        }

        protected interface CommonColumns extends BaseTypes {
            public static final String DATA = "data1";
            public static final String LABEL = "data3";
            public static final String TYPE = "data2";
        }

        public static final class Callable implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter");
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Data.CONTENT_URI, "callables");
            public static final Uri ENTERPRISE_CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter_enterprise");
        }

        public static final class Contactables implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter");
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Data.CONTENT_URI, "contactables");
            public static final String VISIBLE_CONTACTS_ONLY = "visible_contacts_only";
        }

        public static final class Email implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String ADDRESS = "data1";
            public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter");
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/email_v2";
            public static final Uri CONTENT_LOOKUP_URI = Uri.withAppendedPath(CONTENT_URI, ContactsColumns.LOOKUP_KEY);
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/email_v2";
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Data.CONTENT_URI, "emails");
            public static final String DISPLAY_NAME = "data4";
            public static final Uri ENTERPRISE_CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter_enterprise");
            public static final Uri ENTERPRISE_CONTENT_LOOKUP_URI = Uri.withAppendedPath(CONTENT_URI, "lookup_enterprise");
            public static final int TYPE_HOME = 1;
            public static final int TYPE_MOBILE = 4;
            public static final int TYPE_OTHER = 3;
            public static final int TYPE_WORK = 2;

            private Email() {
            }

            public static final int getTypeLabelResource(int type) {
                switch (type) {
                    case 1:
                        return R.string.emailTypeHome;
                    case 2:
                        return R.string.emailTypeWork;
                    case 3:
                        return R.string.emailTypeOther;
                    case 4:
                        return R.string.emailTypeMobile;
                    default:
                        return R.string.emailTypeCustom;
                }
            }

            public static final CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
                if (type != 0 || (TextUtils.isEmpty(label) ^ 1) == 0) {
                    return res.getText(getTypeLabelResource(type));
                }
                return label;
            }
        }

        public static final class Event implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
            public static final String START_DATE = "data1";
            public static final int TYPE_ANNIVERSARY = 1;
            public static final int TYPE_BIRTHDAY = 3;
            public static final int TYPE_OTHER = 2;

            private Event() {
            }

            public static int getTypeResource(Integer type) {
                if (type == null) {
                    return R.string.eventTypeOther;
                }
                switch (type.intValue()) {
                    case 1:
                        return R.string.eventTypeAnniversary;
                    case 2:
                        return R.string.eventTypeOther;
                    case 3:
                        return R.string.eventTypeBirthday;
                    default:
                        return R.string.eventTypeCustom;
                }
            }

            public static final CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
                if (type != 0 || (TextUtils.isEmpty(label) ^ 1) == 0) {
                    return res.getText(getTypeResource(Integer.valueOf(type)));
                }
                return label;
            }
        }

        public static final class GroupMembership implements DataColumnsWithJoins, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/group_membership";
            public static final String GROUP_ROW_ID = "data1";
            public static final String GROUP_SOURCE_ID = "group_sourceid";

            private GroupMembership() {
            }
        }

        public static final class Identity implements DataColumnsWithJoins, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/identity";
            public static final String IDENTITY = "data1";
            public static final String NAMESPACE = "data2";

            private Identity() {
            }
        }

        public static final class Im implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/im";
            public static final String CUSTOM_PROTOCOL = "data6";
            public static final String PROTOCOL = "data5";
            public static final int PROTOCOL_AIM = 0;
            public static final int PROTOCOL_CUSTOM = -1;
            public static final int PROTOCOL_GOOGLE_TALK = 5;
            public static final int PROTOCOL_ICQ = 6;
            public static final int PROTOCOL_JABBER = 7;
            public static final int PROTOCOL_MSN = 1;
            public static final int PROTOCOL_NETMEETING = 8;
            public static final int PROTOCOL_QQ = 4;
            public static final int PROTOCOL_SKYPE = 3;
            public static final int PROTOCOL_YAHOO = 2;
            public static final int TYPE_HOME = 1;
            public static final int TYPE_OTHER = 3;
            public static final int TYPE_WORK = 2;

            private Im() {
            }

            public static final int getTypeLabelResource(int type) {
                switch (type) {
                    case 1:
                        return R.string.imTypeHome;
                    case 2:
                        return R.string.imTypeWork;
                    case 3:
                        return R.string.imTypeOther;
                    default:
                        return R.string.imTypeCustom;
                }
            }

            public static final CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
                if (type != 0 || (TextUtils.isEmpty(label) ^ 1) == 0) {
                    return res.getText(getTypeLabelResource(type));
                }
                return label;
            }

            public static final int getProtocolLabelResource(int type) {
                switch (type) {
                    case 0:
                        return R.string.imProtocolAim;
                    case 1:
                        return R.string.imProtocolMsn;
                    case 2:
                        return R.string.imProtocolYahoo;
                    case 3:
                        return R.string.imProtocolSkype;
                    case 4:
                        return R.string.imProtocolQq;
                    case 5:
                        return R.string.imProtocolGoogleTalk;
                    case 6:
                        return R.string.imProtocolIcq;
                    case 7:
                        return R.string.imProtocolJabber;
                    case 8:
                        return R.string.imProtocolNetMeeting;
                    default:
                        return R.string.imProtocolCustom;
                }
            }

            public static final CharSequence getProtocolLabel(Resources res, int type, CharSequence label) {
                if (type != -1 || (TextUtils.isEmpty(label) ^ 1) == 0) {
                    return res.getText(getProtocolLabelResource(type));
                }
                return label;
            }
        }

        public static final class Nickname implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/nickname";
            public static final String NAME = "data1";
            public static final int TYPE_DEFAULT = 1;
            public static final int TYPE_INITIALS = 5;
            public static final int TYPE_MAIDEN_NAME = 3;
            @Deprecated
            public static final int TYPE_MAINDEN_NAME = 3;
            public static final int TYPE_OTHER_NAME = 2;
            public static final int TYPE_SHORT_NAME = 4;

            private Nickname() {
            }
        }

        public static final class Note implements DataColumnsWithJoins, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/note";
            public static final String NOTE = "data1";

            private Note() {
            }
        }

        public static final class Organization implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String COMPANY = "data1";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/organization";
            public static final String DEPARTMENT = "data5";
            public static final String JOB_DESCRIPTION = "data6";
            public static final String OFFICE_LOCATION = "data9";
            public static final String PHONETIC_NAME = "data8";
            public static final String PHONETIC_NAME_STYLE = "data10";
            public static final String SYMBOL = "data7";
            public static final String TITLE = "data4";
            public static final int TYPE_OTHER = 2;
            public static final int TYPE_WORK = 1;

            private Organization() {
            }

            public static final int getTypeLabelResource(int type) {
                switch (type) {
                    case 1:
                        return R.string.orgTypeWork;
                    case 2:
                        return R.string.orgTypeOther;
                    default:
                        return R.string.orgTypeCustom;
                }
            }

            public static final CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
                if (type != 0 || (TextUtils.isEmpty(label) ^ 1) == 0) {
                    return res.getText(getTypeLabelResource(type));
                }
                return label;
            }
        }

        public static final class Phone implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter");
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone_v2";
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/phone_v2";
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Data.CONTENT_URI, Phones.CONTENT_DIRECTORY);
            public static final Uri ENTERPRISE_CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter_enterprise");
            public static final Uri ENTERPRISE_CONTENT_URI = Uri.withAppendedPath(Data.ENTERPRISE_CONTENT_URI, Phones.CONTENT_DIRECTORY);
            public static final String NORMALIZED_NUMBER = "data4";
            public static final String NUMBER = "data1";
            public static final String SEARCH_DISPLAY_NAME_KEY = "search_display_name";
            public static final String SEARCH_PHONE_NUMBER_KEY = "search_phone_number";
            public static final int TYPE_ASSISTANT = 19;
            public static final int TYPE_CALLBACK = 8;
            public static final int TYPE_CAR = 9;
            public static final int TYPE_COMPANY_MAIN = 10;
            public static final int TYPE_FAX_HOME = 5;
            public static final int TYPE_FAX_WORK = 4;
            public static final int TYPE_HOME = 1;
            public static final int TYPE_ISDN = 11;
            public static final int TYPE_MAIN = 12;
            public static final int TYPE_MMS = 20;
            public static final int TYPE_MOBILE = 2;
            public static final int TYPE_OTHER = 7;
            public static final int TYPE_OTHER_FAX = 13;
            public static final int TYPE_PAGER = 6;
            public static final int TYPE_RADIO = 14;
            public static final int TYPE_TELEX = 15;
            public static final int TYPE_TTY_TDD = 16;
            public static final int TYPE_WORK = 3;
            public static final int TYPE_WORK_MOBILE = 17;
            public static final int TYPE_WORK_PAGER = 18;

            private Phone() {
            }

            @Deprecated
            public static final CharSequence getDisplayLabel(Context context, int type, CharSequence label, CharSequence[] labelArray) {
                return getTypeLabel(context.getResources(), type, label);
            }

            @Deprecated
            public static final CharSequence getDisplayLabel(Context context, int type, CharSequence label) {
                return getTypeLabel(context.getResources(), type, label);
            }

            public static final int getTypeLabelResource(int type) {
                switch (type) {
                    case 1:
                        return R.string.phoneTypeHome;
                    case 2:
                        return R.string.phoneTypeMobile;
                    case 3:
                        return R.string.phoneTypeWork;
                    case 4:
                        return R.string.phoneTypeFaxWork;
                    case 5:
                        return R.string.phoneTypeFaxHome;
                    case 6:
                        return R.string.phoneTypePager;
                    case 7:
                        return R.string.phoneTypeOther;
                    case 8:
                        return R.string.phoneTypeCallback;
                    case 9:
                        return R.string.phoneTypeCar;
                    case 10:
                        return R.string.phoneTypeCompanyMain;
                    case 11:
                        return R.string.phoneTypeIsdn;
                    case 12:
                        return R.string.phoneTypeMain;
                    case 13:
                        return R.string.phoneTypeOtherFax;
                    case 14:
                        return R.string.phoneTypeRadio;
                    case 15:
                        return R.string.phoneTypeTelex;
                    case 16:
                        return R.string.phoneTypeTtyTdd;
                    case 17:
                        return R.string.phoneTypeWorkMobile;
                    case 18:
                        return R.string.phoneTypeWorkPager;
                    case 19:
                        return R.string.phoneTypeAssistant;
                    case 20:
                        return R.string.phoneTypeMms;
                    default:
                        return R.string.phoneTypeCustom;
                }
            }

            public static final CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
                if ((type == 0 || type == 19) && (TextUtils.isEmpty(label) ^ 1) != 0) {
                    return label;
                }
                return res.getText(getTypeLabelResource(type));
            }
        }

        public static final class Photo implements DataColumnsWithJoins, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/photo";
            public static final String PHOTO = "data15";
            public static final String PHOTO_FILE_ID = "data14";

            private Photo() {
            }
        }

        public static final class Relation implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/relation";
            public static final String NAME = "data1";
            public static final int TYPE_ASSISTANT = 1;
            public static final int TYPE_BROTHER = 2;
            public static final int TYPE_CHILD = 3;
            public static final int TYPE_DOMESTIC_PARTNER = 4;
            public static final int TYPE_FATHER = 5;
            public static final int TYPE_FRIEND = 6;
            public static final int TYPE_MANAGER = 7;
            public static final int TYPE_MOTHER = 8;
            public static final int TYPE_PARENT = 9;
            public static final int TYPE_PARTNER = 10;
            public static final int TYPE_REFERRED_BY = 11;
            public static final int TYPE_RELATIVE = 12;
            public static final int TYPE_SISTER = 13;
            public static final int TYPE_SPOUSE = 14;

            private Relation() {
            }

            public static final int getTypeLabelResource(int type) {
                switch (type) {
                    case 1:
                        return R.string.relationTypeAssistant;
                    case 2:
                        return R.string.relationTypeBrother;
                    case 3:
                        return R.string.relationTypeChild;
                    case 4:
                        return R.string.relationTypeDomesticPartner;
                    case 5:
                        return R.string.relationTypeFather;
                    case 6:
                        return R.string.relationTypeFriend;
                    case 7:
                        return R.string.relationTypeManager;
                    case 8:
                        return R.string.relationTypeMother;
                    case 9:
                        return R.string.relationTypeParent;
                    case 10:
                        return R.string.relationTypePartner;
                    case 11:
                        return R.string.relationTypeReferredBy;
                    case 12:
                        return R.string.relationTypeRelative;
                    case 13:
                        return R.string.relationTypeSister;
                    case 14:
                        return R.string.relationTypeSpouse;
                    default:
                        return R.string.orgTypeCustom;
                }
            }

            public static final CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
                if (type != 0 || (TextUtils.isEmpty(label) ^ 1) == 0) {
                    return res.getText(getTypeLabelResource(type));
                }
                return label;
            }
        }

        public static final class SipAddress implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/sip_address";
            public static final String SIP_ADDRESS = "data1";
            public static final int TYPE_HOME = 1;
            public static final int TYPE_OTHER = 3;
            public static final int TYPE_WORK = 2;

            private SipAddress() {
            }

            public static final int getTypeLabelResource(int type) {
                switch (type) {
                    case 1:
                        return R.string.sipAddressTypeHome;
                    case 2:
                        return R.string.sipAddressTypeWork;
                    case 3:
                        return R.string.sipAddressTypeOther;
                    default:
                        return R.string.sipAddressTypeCustom;
                }
            }

            public static final CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
                if (type != 0 || (TextUtils.isEmpty(label) ^ 1) == 0) {
                    return res.getText(getTypeLabelResource(type));
                }
                return label;
            }
        }

        public static final class StructuredName implements DataColumnsWithJoins, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/name";
            public static final String DISPLAY_NAME = "data1";
            public static final String FAMILY_NAME = "data3";
            public static final String FULL_NAME_STYLE = "data10";
            public static final String GIVEN_NAME = "data2";
            public static final String MIDDLE_NAME = "data5";
            public static final String PHONETIC_FAMILY_NAME = "data9";
            public static final String PHONETIC_GIVEN_NAME = "data7";
            public static final String PHONETIC_MIDDLE_NAME = "data8";
            public static final String PHONETIC_NAME_STYLE = "data11";
            public static final String PREFIX = "data4";
            public static final String SUFFIX = "data6";

            private StructuredName() {
            }
        }

        public static final class StructuredPostal implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String CITY = "data7";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postal-address_v2";
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/postal-address_v2";
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Data.CONTENT_URI, "postals");
            public static final String COUNTRY = "data10";
            public static final String FORMATTED_ADDRESS = "data1";
            public static final String NEIGHBORHOOD = "data6";
            public static final String POBOX = "data5";
            public static final String POSTCODE = "data9";
            public static final String REGION = "data8";
            public static final String STREET = "data4";
            public static final int TYPE_HOME = 1;
            public static final int TYPE_OTHER = 3;
            public static final int TYPE_WORK = 2;

            private StructuredPostal() {
            }

            public static final int getTypeLabelResource(int type) {
                switch (type) {
                    case 1:
                        return R.string.postalTypeHome;
                    case 2:
                        return R.string.postalTypeWork;
                    case 3:
                        return R.string.postalTypeOther;
                    default:
                        return R.string.postalTypeCustom;
                }
            }

            public static final CharSequence getTypeLabel(Resources res, int type, CharSequence label) {
                if (type != 0 || (TextUtils.isEmpty(label) ^ 1) == 0) {
                    return res.getText(getTypeLabelResource(type));
                }
                return label;
            }
        }

        public static final class Website implements DataColumnsWithJoins, CommonColumns, ContactCounts {
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/website";
            public static final int TYPE_BLOG = 2;
            public static final int TYPE_FTP = 6;
            public static final int TYPE_HOME = 4;
            public static final int TYPE_HOMEPAGE = 1;
            public static final int TYPE_OTHER = 7;
            public static final int TYPE_PROFILE = 3;
            public static final int TYPE_WORK = 5;
            public static final String URL = "data1";

            private Website() {
            }
        }

        private CommonDataKinds() {
        }
    }

    protected interface SyncColumns extends BaseSyncColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String DIRTY = "dirty";
        public static final String SOURCE_ID = "sourceid";
        public static final String VERSION = "version";
    }

    @Deprecated
    protected interface StreamItemsColumns {
        @Deprecated
        public static final String ACCOUNT_NAME = "account_name";
        @Deprecated
        public static final String ACCOUNT_TYPE = "account_type";
        @Deprecated
        public static final String COMMENTS = "comments";
        @Deprecated
        public static final String CONTACT_ID = "contact_id";
        @Deprecated
        public static final String CONTACT_LOOKUP_KEY = "contact_lookup";
        @Deprecated
        public static final String DATA_SET = "data_set";
        @Deprecated
        public static final String RAW_CONTACT_ID = "raw_contact_id";
        @Deprecated
        public static final String RAW_CONTACT_SOURCE_ID = "raw_contact_source_id";
        @Deprecated
        public static final String RES_ICON = "icon";
        @Deprecated
        public static final String RES_LABEL = "label";
        @Deprecated
        public static final String RES_PACKAGE = "res_package";
        @Deprecated
        public static final String SYNC1 = "stream_item_sync1";
        @Deprecated
        public static final String SYNC2 = "stream_item_sync2";
        @Deprecated
        public static final String SYNC3 = "stream_item_sync3";
        @Deprecated
        public static final String SYNC4 = "stream_item_sync4";
        @Deprecated
        public static final String TEXT = "text";
        @Deprecated
        public static final String TIMESTAMP = "timestamp";
    }

    public static class Contacts implements BaseColumns, ContactsColumns, ContactOptionsColumns, ContactNameColumns, ContactStatusColumns, ContactCounts {
        public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter");
        public static final Uri CONTENT_FREQUENT_URI = Uri.withAppendedPath(CONTENT_URI, "frequent");
        public static final Uri CONTENT_GROUP_URI = Uri.withAppendedPath(CONTENT_URI, "group");
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact";
        public static final Uri CONTENT_LOOKUP_URI = Uri.withAppendedPath(CONTENT_URI, ContactsColumns.LOOKUP_KEY);
        public static final Uri CONTENT_MULTI_VCARD_URI = Uri.withAppendedPath(CONTENT_URI, "as_multi_vcard");
        public static final Uri CONTENT_STREQUENT_FILTER_URI = Uri.withAppendedPath(CONTENT_STREQUENT_URI, "filter");
        public static final Uri CONTENT_STREQUENT_URI = Uri.withAppendedPath(CONTENT_URI, "strequent");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, Contacts.AUTHORITY);
        public static final String CONTENT_VCARD_TYPE = "text/x-vcard";
        public static final Uri CONTENT_VCARD_URI = Uri.withAppendedPath(CONTENT_URI, "as_vcard");
        public static final Uri CORP_CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts_corp");
        public static long ENTERPRISE_CONTACT_ID_BASE = 1000000000;
        public static String ENTERPRISE_CONTACT_LOOKUP_PREFIX = "c-";
        public static final Uri ENTERPRISE_CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter_enterprise");
        public static final String QUERY_PARAMETER_VCARD_NO_PHOTO = "no_photo";

        public static final class AggregationSuggestions implements BaseColumns, ContactsColumns, ContactOptionsColumns, ContactStatusColumns {
            public static final String CONTENT_DIRECTORY = "suggestions";
            public static final String PARAMETER_MATCH_NAME = "name";

            public static final class Builder {
                private long mContactId;
                private int mLimit;
                private final ArrayList<String> mValues = new ArrayList();

                public Builder setContactId(long contactId) {
                    this.mContactId = contactId;
                    return this;
                }

                public Builder addNameParameter(String name) {
                    this.mValues.add(name);
                    return this;
                }

                public Builder setLimit(int limit) {
                    this.mLimit = limit;
                    return this;
                }

                public Uri build() {
                    android.net.Uri.Builder builder = Contacts.CONTENT_URI.buildUpon();
                    builder.appendEncodedPath(String.valueOf(this.mContactId));
                    builder.appendPath(AggregationSuggestions.CONTENT_DIRECTORY);
                    if (this.mLimit != 0) {
                        builder.appendQueryParameter("limit", String.valueOf(this.mLimit));
                    }
                    int count = this.mValues.size();
                    for (int i = 0; i < count; i++) {
                        builder.appendQueryParameter(SuggestionColumns.QUERY, "name:" + ((String) this.mValues.get(i)));
                    }
                    return builder.build();
                }
            }

            private AggregationSuggestions() {
            }

            public static final Builder builder() {
                return new Builder();
            }
        }

        public static final class Data implements BaseColumns, DataColumns {
            public static final String CONTENT_DIRECTORY = "data";

            private Data() {
            }
        }

        public static final class Entity implements BaseColumns, ContactsColumns, ContactNameColumns, RawContactsColumns, BaseSyncColumns, SyncColumns, DataColumns, StatusColumns, ContactOptionsColumns, ContactStatusColumns, DataUsageStatColumns {
            public static final String CONTENT_DIRECTORY = "entities";
            public static final String DATA_ID = "data_id";
            public static final String RAW_CONTACT_ID = "raw_contact_id";

            private Entity() {
            }
        }

        public static final class Photo implements BaseColumns, DataColumnsWithJoins {
            public static final String CONTENT_DIRECTORY = "photo";
            public static final String DISPLAY_PHOTO = "display_photo";
            public static final String PHOTO = "data15";
            public static final String PHOTO_FILE_ID = "data14";

            private Photo() {
            }
        }

        @Deprecated
        public static final class StreamItems implements StreamItemsColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "stream_items";

            @Deprecated
            private StreamItems() {
            }
        }

        private Contacts() {
        }

        public static Uri getLookupUri(ContentResolver resolver, Uri contactUri) {
            Cursor c = resolver.query(contactUri, new String[]{ContactsColumns.LOOKUP_KEY, "_id"}, null, null, null);
            if (c == null) {
                return null;
            }
            try {
                if (c.moveToFirst()) {
                    Uri lookupUri = getLookupUri(c.getLong(1), c.getString(0));
                    return lookupUri;
                }
                c.close();
                return null;
            } finally {
                c.close();
            }
        }

        public static Uri getLookupUri(long contactId, String lookupKey) {
            if (TextUtils.isEmpty(lookupKey)) {
                return null;
            }
            return ContentUris.withAppendedId(Uri.withAppendedPath(CONTENT_LOOKUP_URI, lookupKey), contactId);
        }

        public static Uri lookupContact(ContentResolver resolver, Uri lookupUri) {
            if (lookupUri == null) {
                return null;
            }
            Cursor c = resolver.query(lookupUri, new String[]{"_id"}, null, null, null);
            if (c == null) {
                return null;
            }
            try {
                if (c.moveToFirst()) {
                    Uri withAppendedId = ContentUris.withAppendedId(CONTENT_URI, c.getLong(0));
                    return withAppendedId;
                }
                c.close();
                return null;
            } finally {
                c.close();
            }
        }

        @Deprecated
        public static void markAsContacted(ContentResolver resolver, long contactId) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, contactId);
            ContentValues values = new ContentValues();
            values.put("last_time_contacted", Long.valueOf(System.currentTimeMillis()));
            resolver.update(uri, values, null, null);
        }

        public static boolean isEnterpriseContactId(long contactId) {
            return contactId >= ENTERPRISE_CONTACT_ID_BASE && contactId < Profile.MIN_ID;
        }

        public static InputStream openContactPhotoInputStream(ContentResolver cr, Uri contactUri, boolean preferHighres) {
            if (preferHighres) {
                try {
                    AssetFileDescriptor fd = cr.openAssetFileDescriptor(Uri.withAppendedPath(contactUri, "display_photo"), "r");
                    if (fd != null) {
                        return fd.createInputStream();
                    }
                } catch (IOException e) {
                }
            }
            Uri photoUri = Uri.withAppendedPath(contactUri, "photo");
            if (photoUri == null) {
                return null;
            }
            Cursor cursor = cr.query(photoUri, new String[]{"data15"}, null, null, null);
            if (cursor != null) {
                try {
                    if ((cursor.moveToNext() ^ 1) == 0) {
                        byte[] data = cursor.getBlob(0);
                        if (data == null) {
                            return null;
                        }
                        InputStream byteArrayInputStream = new ByteArrayInputStream(data);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return byteArrayInputStream;
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }

        public static InputStream openContactPhotoInputStream(ContentResolver cr, Uri contactUri) {
            return openContactPhotoInputStream(cr, contactUri, false);
        }
    }

    public static final class Data implements DataColumnsWithJoins, ContactCounts {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/data";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "data");
        static final Uri ENTERPRISE_CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "data_enterprise");
        public static final String VISIBLE_CONTACTS_ONLY = "visible_contacts_only";

        private Data() {
        }

        public static Uri getContactLookupUri(ContentResolver resolver, Uri dataUri) {
            Cursor cursor = resolver.query(dataUri, new String[]{"contact_id", ContactsColumns.LOOKUP_KEY}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        Uri lookupUri = Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                        return lookupUri;
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
    }

    public static final class DataUsageFeedback {
        public static final Uri DELETE_USAGE_URI = Uri.withAppendedPath(Contacts.CONTENT_URI, "delete_usage");
        public static final Uri FEEDBACK_URI = Uri.withAppendedPath(Data.CONTENT_URI, "usagefeedback");
        public static final String USAGE_TYPE = "type";
        public static final String USAGE_TYPE_CALL = "call";
        public static final String USAGE_TYPE_LONG_TEXT = "long_text";
        public static final String USAGE_TYPE_SHORT_TEXT = "short_text";
    }

    protected interface DeletedContactsColumns {
        public static final String CONTACT_DELETED_TIMESTAMP = "contact_deleted_timestamp";
        public static final String CONTACT_ID = "contact_id";
    }

    public static final class DeletedContacts implements DeletedContactsColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "deleted_contacts");
        private static final int DAYS_KEPT = 30;
        public static final long DAYS_KEPT_MILLISECONDS = 2592000000L;

        private DeletedContacts() {
        }
    }

    public static final class Directory implements BaseColumns {
        public static final String ACCOUNT_NAME = "accountName";
        public static final String ACCOUNT_TYPE = "accountType";
        public static final String CALLER_PACKAGE_PARAM_KEY = "callerPackage";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_directory";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact_directories";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "directories");
        public static final long DEFAULT = 0;
        public static final String DIRECTORY_AUTHORITY = "authority";
        public static final String DISPLAY_NAME = "displayName";
        public static final Uri ENTERPRISE_CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "directories_enterprise");
        public static final long ENTERPRISE_DEFAULT = 1000000000;
        public static final long ENTERPRISE_DIRECTORY_ID_BASE = 1000000000;
        public static final Uri ENTERPRISE_FILE_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "directory_file_enterprise");
        public static final long ENTERPRISE_LOCAL_INVISIBLE = 1000000001;
        public static final String EXPORT_SUPPORT = "exportSupport";
        public static final int EXPORT_SUPPORT_ANY_ACCOUNT = 2;
        public static final int EXPORT_SUPPORT_NONE = 0;
        public static final int EXPORT_SUPPORT_SAME_ACCOUNT_ONLY = 1;
        public static final long LOCAL_INVISIBLE = 1;
        public static final String PACKAGE_NAME = "packageName";
        public static final String PHOTO_SUPPORT = "photoSupport";
        public static final int PHOTO_SUPPORT_FULL = 3;
        public static final int PHOTO_SUPPORT_FULL_SIZE_ONLY = 2;
        public static final int PHOTO_SUPPORT_NONE = 0;
        public static final int PHOTO_SUPPORT_THUMBNAIL_ONLY = 1;
        public static final String SHORTCUT_SUPPORT = "shortcutSupport";
        public static final int SHORTCUT_SUPPORT_DATA_ITEMS_ONLY = 1;
        public static final int SHORTCUT_SUPPORT_FULL = 2;
        public static final int SHORTCUT_SUPPORT_NONE = 0;
        public static final String TYPE_RESOURCE_ID = "typeResourceId";

        private Directory() {
        }

        public static boolean isRemoteDirectoryId(long directoryId) {
            if (directoryId == 0 || directoryId == 1 || directoryId == 1000000000 || directoryId == ENTERPRISE_LOCAL_INVISIBLE) {
                return false;
            }
            return true;
        }

        public static boolean isRemoteDirectory(long directoryId) {
            return isRemoteDirectoryId(directoryId);
        }

        public static boolean isEnterpriseDirectoryId(long directoryId) {
            return directoryId >= 1000000000;
        }

        public static void notifyDirectoryChange(ContentResolver resolver) {
            resolver.update(CONTENT_URI, new ContentValues(), null, null);
        }
    }

    public interface DisplayNameSources {
        public static final int EMAIL = 10;
        public static final int NICKNAME = 35;
        public static final int ORGANIZATION = 30;
        public static final int PHONE = 20;
        public static final int STRUCTURED_NAME = 40;
        public static final int STRUCTURED_PHONETIC_NAME = 37;
        public static final int UNDEFINED = 0;
    }

    public static final class DisplayPhoto {
        public static final Uri CONTENT_MAX_DIMENSIONS_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "photo_dimensions");
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "display_photo");
        public static final String DISPLAY_MAX_DIM = "display_max_dim";
        public static final String THUMBNAIL_MAX_DIM = "thumbnail_max_dim";

        private DisplayPhoto() {
        }
    }

    public interface FullNameStyle {
        public static final int CHINESE = 3;
        public static final int CJK = 2;
        public static final int JAPANESE = 4;
        public static final int KOREAN = 5;
        public static final int UNDEFINED = 0;
        public static final int WESTERN = 1;
    }

    protected interface GroupsColumns {
        public static final String ACCOUNT_TYPE_AND_DATA_SET = "account_type_and_data_set";
        public static final String AUTO_ADD = "auto_add";
        public static final String DATA_SET = "data_set";
        public static final String DELETED = "deleted";
        public static final String FAVORITES = "favorites";
        public static final String GROUP_IS_READ_ONLY = "group_is_read_only";
        public static final String GROUP_VISIBLE = "group_visible";
        public static final String NOTES = "notes";
        public static final String PARAM_RETURN_GROUP_COUNT_PER_ACCOUNT = "return_group_count_per_account";
        public static final String RES_PACKAGE = "res_package";
        public static final String SHOULD_SYNC = "should_sync";
        public static final String SUMMARY_COUNT = "summ_count";
        public static final String SUMMARY_GROUP_COUNT_PER_ACCOUNT = "group_count_per_account";
        public static final String SUMMARY_WITH_PHONES = "summ_phones";
        public static final String SYSTEM_ID = "system_id";
        public static final String TITLE = "title";
        public static final String TITLE_RES = "title_res";
    }

    public static final class Groups implements BaseColumns, GroupsColumns, SyncColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/group";
        public static final Uri CONTENT_SUMMARY_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "groups_summary");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/group";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "groups");

        private static class EntityIteratorImpl extends CursorEntityIterator {
            public EntityIteratorImpl(Cursor cursor) {
                super(cursor);
            }

            public Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException {
                ContentValues values = new ContentValues();
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, "_id");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "account_name");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "account_type");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, "dirty");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, "version");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "sourceid");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "res_package");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "title");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, GroupsColumns.TITLE_RES);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, GroupsColumns.GROUP_VISIBLE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "sync1");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "sync2");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "sync3");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "sync4");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "system_id");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, "deleted");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "notes");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, "should_sync");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, GroupsColumns.FAVORITES);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, GroupsColumns.AUTO_ADD);
                cursor.moveToNext();
                return new Entity(values);
            }
        }

        private Groups() {
        }

        public static EntityIterator newEntityIterator(Cursor cursor) {
            return new EntityIteratorImpl(cursor);
        }
    }

    public static final class Intents {
        public static final String ACTION_GET_MULTIPLE_PHONES = "com.android.contacts.action.GET_MULTIPLE_PHONES";
        public static final String ACTION_PROFILE_CHANGED = "android.provider.Contacts.PROFILE_CHANGED";
        public static final String ACTION_VOICE_SEND_MESSAGE_TO_CONTACTS = "android.provider.action.VOICE_SEND_MESSAGE_TO_CONTACTS";
        public static final String ATTACH_IMAGE = "com.android.contacts.action.ATTACH_IMAGE";
        public static final String CONTACTS_DATABASE_CREATED = "android.provider.Contacts.DATABASE_CREATED";
        public static final String EXTRA_CREATE_DESCRIPTION = "com.android.contacts.action.CREATE_DESCRIPTION";
        @Deprecated
        public static final String EXTRA_EXCLUDE_MIMES = "exclude_mimes";
        public static final String EXTRA_FORCE_CREATE = "com.android.contacts.action.FORCE_CREATE";
        @Deprecated
        public static final String EXTRA_MODE = "mode";
        public static final String EXTRA_PHONE_URIS = "com.android.contacts.extra.PHONE_URIS";
        public static final String EXTRA_RECIPIENT_CONTACT_CHAT_ID = "android.provider.extra.RECIPIENT_CONTACT_CHAT_ID";
        public static final String EXTRA_RECIPIENT_CONTACT_NAME = "android.provider.extra.RECIPIENT_CONTACT_NAME";
        public static final String EXTRA_RECIPIENT_CONTACT_URI = "android.provider.extra.RECIPIENT_CONTACT_URI";
        @Deprecated
        public static final String EXTRA_TARGET_RECT = "target_rect";
        public static final String INVITE_CONTACT = "com.android.contacts.action.INVITE_CONTACT";
        public static final String METADATA_ACCOUNT_TYPE = "android.provider.account_type";
        public static final String METADATA_MIMETYPE = "android.provider.mimetype";
        @Deprecated
        public static final int MODE_LARGE = 3;
        @Deprecated
        public static final int MODE_MEDIUM = 2;
        @Deprecated
        public static final int MODE_SMALL = 1;
        public static final String SEARCH_SUGGESTION_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_CLICKED";
        public static final String SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED";
        public static final String SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED";
        public static final String SHOW_OR_CREATE_CONTACT = "com.android.contacts.action.SHOW_OR_CREATE_CONTACT";

        public static final class Insert {
            public static final String ACTION = "android.intent.action.INSERT";
            public static final String COMPANY = "company";
            public static final String DATA = "data";
            public static final String EMAIL = "email";
            public static final String EMAIL_ISPRIMARY = "email_isprimary";
            public static final String EMAIL_TYPE = "email_type";
            public static final String EXTRA_ACCOUNT = "android.provider.extra.ACCOUNT";
            public static final String EXTRA_DATA_SET = "android.provider.extra.DATA_SET";
            public static final String FULL_MODE = "full_mode";
            public static final String IM_HANDLE = "im_handle";
            public static final String IM_ISPRIMARY = "im_isprimary";
            public static final String IM_PROTOCOL = "im_protocol";
            public static final String JOB_TITLE = "job_title";
            public static final String NAME = "name";
            public static final String NOTES = "notes";
            public static final String PHONE = "phone";
            public static final String PHONETIC_NAME = "phonetic_name";
            public static final String PHONE_ISPRIMARY = "phone_isprimary";
            public static final String PHONE_TYPE = "phone_type";
            public static final String POSTAL = "postal";
            public static final String POSTAL_ISPRIMARY = "postal_isprimary";
            public static final String POSTAL_TYPE = "postal_type";
            public static final String SECONDARY_EMAIL = "secondary_email";
            public static final String SECONDARY_EMAIL_TYPE = "secondary_email_type";
            public static final String SECONDARY_PHONE = "secondary_phone";
            public static final String SECONDARY_PHONE_TYPE = "secondary_phone_type";
            public static final String TERTIARY_EMAIL = "tertiary_email";
            public static final String TERTIARY_EMAIL_TYPE = "tertiary_email_type";
            public static final String TERTIARY_PHONE = "tertiary_phone";
            public static final String TERTIARY_PHONE_TYPE = "tertiary_phone_type";
        }
    }

    protected interface MetadataSyncColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String DATA = "data";
        public static final String DATA_SET = "data_set";
        public static final String DELETED = "deleted";
        public static final String RAW_CONTACT_BACKUP_ID = "raw_contact_backup_id";
    }

    public static final class MetadataSync implements BaseColumns, MetadataSyncColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_metadata";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact_metadata";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(METADATA_AUTHORITY_URI, "metadata_sync");
        public static final String METADATA_AUTHORITY = "com.android.contacts.metadata";
        public static final Uri METADATA_AUTHORITY_URI = Uri.parse("content://com.android.contacts.metadata");

        private MetadataSync() {
        }
    }

    protected interface MetadataSyncStateColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String DATA_SET = "data_set";
        public static final String STATE = "state";
    }

    public static final class MetadataSyncState implements BaseColumns, MetadataSyncStateColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_metadata_sync_state";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact_metadata_sync_state";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MetadataSync.METADATA_AUTHORITY_URI, "metadata_sync_state");

        private MetadataSyncState() {
        }
    }

    protected interface PhoneLookupColumns {
        public static final String CONTACT_ID = "contact_id";
        public static final String DATA_ID = "data_id";
        public static final String LABEL = "label";
        public static final String NORMALIZED_NUMBER = "normalized_number";
        public static final String NUMBER = "number";
        public static final String TYPE = "type";
    }

    public static final class PhoneLookup implements BaseColumns, PhoneLookupColumns, ContactsColumns, ContactOptionsColumns, ContactNameColumns {
        public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "phone_lookup");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/phone_lookup";
        public static final Uri ENTERPRISE_CONTENT_FILTER_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "phone_lookup_enterprise");
        public static final String QUERY_PARAMETER_SIP_ADDRESS = "sip";

        private PhoneLookup() {
        }
    }

    public interface PhoneticNameStyle {
        public static final int JAPANESE = 4;
        public static final int KOREAN = 5;
        public static final int PINYIN = 3;
        public static final int UNDEFINED = 0;
    }

    protected interface PhotoFilesColumns {
        public static final String FILESIZE = "filesize";
        public static final String HEIGHT = "height";
        public static final String WIDTH = "width";
    }

    public static final class PhotoFiles implements BaseColumns, PhotoFilesColumns {
        private PhotoFiles() {
        }
    }

    public static final class PinnedPositions {
        public static final int DEMOTED = -1;
        public static final String UNDEMOTE_METHOD = "undemote";
        public static final int UNPINNED = 0;

        public static void undemote(ContentResolver contentResolver, long contactId) {
            contentResolver.call(ContactsContract.AUTHORITY_URI, UNDEMOTE_METHOD, String.valueOf(contactId), null);
        }

        public static void pin(ContentResolver contentResolver, long contactId, int pinnedPosition) {
            Uri uri = Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(contactId));
            ContentValues values = new ContentValues();
            values.put(ContactOptionsColumns.PINNED, Integer.valueOf(pinnedPosition));
            contentResolver.update(uri, values, null, null);
        }
    }

    protected interface PresenceColumns {
        public static final String CUSTOM_PROTOCOL = "custom_protocol";
        public static final String DATA_ID = "presence_data_id";
        public static final String IM_ACCOUNT = "im_account";
        public static final String IM_HANDLE = "im_handle";
        public static final String PROTOCOL = "protocol";
    }

    public static class StatusUpdates implements StatusColumns, PresenceColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/status-update";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/status-update";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "status_updates");
        public static final Uri PROFILE_CONTENT_URI = Uri.withAppendedPath(Profile.CONTENT_URI, "status_updates");

        /* synthetic */ StatusUpdates(StatusUpdates -this0) {
            this();
        }

        private StatusUpdates() {
        }

        public static final int getPresenceIconResourceId(int status) {
            switch (status) {
                case 1:
                    return R.drawable.presence_invisible;
                case 2:
                case 3:
                    return R.drawable.presence_away;
                case 4:
                    return R.drawable.presence_busy;
                case 5:
                    return R.drawable.presence_online;
                default:
                    return R.drawable.presence_offline;
            }
        }

        public static final int getPresencePrecedence(int status) {
            return status;
        }
    }

    @Deprecated
    public static final class Presence extends StatusUpdates {
        public Presence() {
            super();
        }
    }

    public static final class Profile implements BaseColumns, ContactsColumns, ContactOptionsColumns, ContactNameColumns, ContactStatusColumns {
        public static final Uri CONTENT_RAW_CONTACTS_URI = Uri.withAppendedPath(CONTENT_URI, "raw_contacts");
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "profile");
        public static final Uri CONTENT_VCARD_URI = Uri.withAppendedPath(CONTENT_URI, "as_vcard");
        public static final long MIN_ID = 9223372034707292160L;

        private Profile() {
        }
    }

    public static final class ProfileSyncState implements Columns {
        public static final String CONTENT_DIRECTORY = "syncstate";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Profile.CONTENT_URI, "syncstate");

        private ProfileSyncState() {
        }

        public static byte[] get(ContentProviderClient provider, Account account) throws RemoteException {
            return Helpers.get(provider, CONTENT_URI, account);
        }

        public static Pair<Uri, byte[]> getWithUri(ContentProviderClient provider, Account account) throws RemoteException {
            return Helpers.getWithUri(provider, CONTENT_URI, account);
        }

        public static void set(ContentProviderClient provider, Account account, byte[] data) throws RemoteException {
            Helpers.set(provider, CONTENT_URI, account, data);
        }

        public static ContentProviderOperation newSetOperation(Account account, byte[] data) {
            return Helpers.newSetOperation(CONTENT_URI, account, data);
        }
    }

    public static final class ProviderStatus {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/provider_status";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "provider_status");
        public static final String DATABASE_CREATION_TIMESTAMP = "database_creation_timestamp";
        public static final String STATUS = "status";
        public static final int STATUS_BUSY = 1;
        public static final int STATUS_EMPTY = 2;
        public static final int STATUS_NORMAL = 0;

        private ProviderStatus() {
        }
    }

    public static final class QuickContact {
        public static final String ACTION_QUICK_CONTACT = "android.provider.action.QUICK_CONTACT";
        public static final String EXTRA_EXCLUDE_MIMES = "android.provider.extra.EXCLUDE_MIMES";
        public static final String EXTRA_MODE = "android.provider.extra.MODE";
        public static final String EXTRA_PRIORITIZED_MIMETYPE = "android.provider.extra.PRIORITIZED_MIMETYPE";
        @Deprecated
        public static final String EXTRA_TARGET_RECT = "android.provider.extra.TARGET_RECT";
        public static final int MODE_DEFAULT = 3;
        public static final int MODE_LARGE = 3;
        public static final int MODE_MEDIUM = 2;
        public static final int MODE_SMALL = 1;

        public static Intent composeQuickContactsIntent(Context context, View target, Uri lookupUri, int mode, String[] excludeMimes) {
            float appScale = context.getResources().getCompatibilityInfo().applicationScale;
            int[] pos = new int[2];
            target.getLocationOnScreen(pos);
            Rect rect = new Rect();
            rect.left = (int) ((((float) pos[0]) * appScale) + 0.5f);
            rect.top = (int) ((((float) pos[1]) * appScale) + 0.5f);
            rect.right = (int) ((((float) (pos[0] + target.getWidth())) * appScale) + 0.5f);
            rect.bottom = (int) ((((float) (pos[1] + target.getHeight())) * appScale) + 0.5f);
            return composeQuickContactsIntent(context, rect, lookupUri, mode, excludeMimes);
        }

        public static Intent composeQuickContactsIntent(Context context, Rect target, Uri lookupUri, int mode, String[] excludeMimes) {
            Context actualContext = context;
            while ((actualContext instanceof ContextWrapper) && ((actualContext instanceof Activity) ^ 1) != 0) {
                actualContext = ((ContextWrapper) actualContext).getBaseContext();
            }
            Intent intent = new Intent(ACTION_QUICK_CONTACT).addFlags((actualContext instanceof Activity ? 0 : 268468224) | 536870912);
            intent.setData(lookupUri);
            intent.setSourceBounds(target);
            intent.putExtra(EXTRA_MODE, mode);
            intent.putExtra(EXTRA_EXCLUDE_MIMES, excludeMimes);
            return intent;
        }

        public static Intent rebuildManagedQuickContactsIntent(String lookupKey, long contactId, boolean isContactIdIgnored, long directoryId, Intent originalIntent) {
            Intent intent = new Intent(ACTION_QUICK_CONTACT);
            Uri uri = null;
            if (!TextUtils.isEmpty(lookupKey)) {
                if (isContactIdIgnored) {
                    uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
                } else {
                    uri = Contacts.getLookupUri(contactId, lookupKey);
                }
            }
            if (!(uri == null || directoryId == 0)) {
                uri = uri.buildUpon().appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();
            }
            intent.setData(uri);
            intent.setFlags(originalIntent.getFlags() | 268435456);
            intent.setSourceBounds(originalIntent.getSourceBounds());
            intent.putExtra(EXTRA_MODE, originalIntent.getIntExtra(EXTRA_MODE, 3));
            intent.putExtra(EXTRA_EXCLUDE_MIMES, originalIntent.getStringArrayExtra(EXTRA_EXCLUDE_MIMES));
            return intent;
        }

        public static void showQuickContact(Context context, View target, Uri lookupUri, int mode, String[] excludeMimes) {
            ContactsInternal.startQuickContactWithErrorToast(context, composeQuickContactsIntent(context, target, lookupUri, mode, excludeMimes));
        }

        public static void showQuickContact(Context context, Rect target, Uri lookupUri, int mode, String[] excludeMimes) {
            ContactsInternal.startQuickContactWithErrorToast(context, composeQuickContactsIntent(context, target, lookupUri, mode, excludeMimes));
        }

        public static void showQuickContact(Context context, View target, Uri lookupUri, String[] excludeMimes, String prioritizedMimeType) {
            Intent intent = composeQuickContactsIntent(context, target, lookupUri, 3, excludeMimes);
            intent.putExtra(EXTRA_PRIORITIZED_MIMETYPE, prioritizedMimeType);
            ContactsInternal.startQuickContactWithErrorToast(context, intent);
        }

        public static void showQuickContact(Context context, Rect target, Uri lookupUri, String[] excludeMimes, String prioritizedMimeType) {
            Intent intent = composeQuickContactsIntent(context, target, lookupUri, 3, excludeMimes);
            intent.putExtra(EXTRA_PRIORITIZED_MIMETYPE, prioritizedMimeType);
            ContactsInternal.startQuickContactWithErrorToast(context, intent);
        }
    }

    public static final class RawContacts implements BaseColumns, RawContactsColumns, ContactOptionsColumns, ContactNameColumns, SyncColumns {
        public static final int AGGREGATION_MODE_DEFAULT = 0;
        public static final int AGGREGATION_MODE_DISABLED = 3;
        @Deprecated
        public static final int AGGREGATION_MODE_IMMEDIATE = 1;
        public static final int AGGREGATION_MODE_SUSPENDED = 2;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/raw_contact";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/raw_contact";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "raw_contacts");

        public static final class Data implements BaseColumns, DataColumns {
            public static final String CONTENT_DIRECTORY = "data";

            private Data() {
            }
        }

        public static final class DisplayPhoto {
            public static final String CONTENT_DIRECTORY = "display_photo";

            private DisplayPhoto() {
            }
        }

        public static final class Entity implements BaseColumns, DataColumns {
            public static final String CONTENT_DIRECTORY = "entity";
            public static final String DATA_ID = "data_id";

            private Entity() {
            }
        }

        private static class EntityIteratorImpl extends CursorEntityIterator {
            private static final String[] DATA_KEYS = new String[]{"data1", "data2", "data3", "data4", "data5", "data6", "data7", "data8", "data9", "data10", "data11", DataColumns.DATA12, DataColumns.DATA13, "data14", "data15", DataColumns.SYNC1, DataColumns.SYNC2, DataColumns.SYNC3, DataColumns.SYNC4};

            public EntityIteratorImpl(Cursor cursor) {
                super(cursor);
            }

            public android.content.Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException {
                int columnRawContactId = cursor.getColumnIndexOrThrow("_id");
                long rawContactId = cursor.getLong(columnRawContactId);
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "account_name");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "account_type");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "data_set");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "_id");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "dirty");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "version");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "sourceid");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "sync1");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "sync2");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "sync3");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "sync4");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "deleted");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "contact_id");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "starred");
                android.content.Entity contact = new android.content.Entity(cv);
                while (rawContactId == cursor.getLong(columnRawContactId)) {
                    cv = new ContentValues();
                    cv.put("_id", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("data_id"))));
                    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "res_package");
                    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "mimetype");
                    DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, DataColumns.IS_PRIMARY);
                    DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, DataColumns.IS_SUPER_PRIMARY);
                    DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, DataColumns.DATA_VERSION);
                    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, GroupMembership.GROUP_SOURCE_ID);
                    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, DataColumns.DATA_VERSION);
                    for (String key : DATA_KEYS) {
                        int columnIndex = cursor.getColumnIndexOrThrow(key);
                        switch (cursor.getType(columnIndex)) {
                            case 0:
                                break;
                            case 1:
                            case 2:
                            case 3:
                                cv.put(key, cursor.getString(columnIndex));
                                break;
                            case 4:
                                cv.put(key, cursor.getBlob(columnIndex));
                                break;
                            default:
                                throw new IllegalStateException("Invalid or unhandled data type");
                        }
                    }
                    contact.addSubValue(Data.CONTENT_URI, cv);
                    if (!cursor.moveToNext()) {
                        return contact;
                    }
                }
                return contact;
            }
        }

        @Deprecated
        public static final class StreamItems implements BaseColumns, StreamItemsColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "stream_items";

            @Deprecated
            private StreamItems() {
            }
        }

        private RawContacts() {
        }

        public static Uri getContactLookupUri(ContentResolver resolver, Uri rawContactUri) {
            Cursor cursor = resolver.query(Uri.withAppendedPath(rawContactUri, "data"), new String[]{"contact_id", ContactsColumns.LOOKUP_KEY}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        Uri lookupUri = Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                        return lookupUri;
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }

        public static EntityIterator newEntityIterator(Cursor cursor) {
            return new EntityIteratorImpl(cursor);
        }
    }

    public static final class RawContactsEntity implements BaseColumns, DataColumns, RawContactsColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/raw_contact_entity";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "raw_contact_entities");
        public static final Uri CORP_CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "raw_contact_entities_corp");
        public static final String DATA_ID = "data_id";
        public static final String FOR_EXPORT_ONLY = "for_export_only";
        public static final Uri PROFILE_CONTENT_URI = Uri.withAppendedPath(Profile.CONTENT_URI, "raw_contact_entities");

        private RawContactsEntity() {
        }
    }

    public static class SearchSnippets {
        public static final String DEFERRED_SNIPPETING_KEY = "deferred_snippeting";
        public static final String SNIPPET = "snippet";
        public static final String SNIPPET_ARGS_PARAM_KEY = "snippet_args";
    }

    protected interface SettingsColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String ANY_UNSYNCED = "any_unsynced";
        public static final String DATA_SET = "data_set";
        public static final String SHOULD_SYNC = "should_sync";
        public static final String UNGROUPED_COUNT = "summ_count";
        public static final String UNGROUPED_VISIBLE = "ungrouped_visible";
        public static final String UNGROUPED_WITH_PHONES = "summ_phones";
    }

    public static final class Settings implements SettingsColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/setting";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/setting";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "settings");

        private Settings() {
        }
    }

    @Deprecated
    protected interface StreamItemPhotosColumns {
        @Deprecated
        public static final String PHOTO_FILE_ID = "photo_file_id";
        @Deprecated
        public static final String PHOTO_URI = "photo_uri";
        @Deprecated
        public static final String SORT_INDEX = "sort_index";
        @Deprecated
        public static final String STREAM_ITEM_ID = "stream_item_id";
        @Deprecated
        public static final String SYNC1 = "stream_item_photo_sync1";
        @Deprecated
        public static final String SYNC2 = "stream_item_photo_sync2";
        @Deprecated
        public static final String SYNC3 = "stream_item_photo_sync3";
        @Deprecated
        public static final String SYNC4 = "stream_item_photo_sync4";
    }

    @Deprecated
    public static final class StreamItemPhotos implements BaseColumns, StreamItemPhotosColumns {
        @Deprecated
        public static final String PHOTO = "photo";

        @Deprecated
        private StreamItemPhotos() {
        }
    }

    @Deprecated
    public static final class StreamItems implements BaseColumns, StreamItemsColumns {
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/stream_item";
        @Deprecated
        public static final Uri CONTENT_LIMIT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "stream_items_limit");
        @Deprecated
        public static final Uri CONTENT_PHOTO_URI = Uri.withAppendedPath(CONTENT_URI, "photo");
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/stream_item";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "stream_items");
        @Deprecated
        public static final String MAX_ITEMS = "max_items";

        @Deprecated
        public static final class StreamItemPhotos implements BaseColumns, StreamItemPhotosColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "photo";
            @Deprecated
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/stream_item_photo";
            @Deprecated
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/stream_item_photo";

            @Deprecated
            private StreamItemPhotos() {
            }
        }

        @Deprecated
        private StreamItems() {
        }
    }

    public static final class SyncState implements Columns {
        public static final String CONTENT_DIRECTORY = "syncstate";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "syncstate");

        private SyncState() {
        }

        public static byte[] get(ContentProviderClient provider, Account account) throws RemoteException {
            return Helpers.get(provider, CONTENT_URI, account);
        }

        public static Pair<Uri, byte[]> getWithUri(ContentProviderClient provider, Account account) throws RemoteException {
            return Helpers.getWithUri(provider, CONTENT_URI, account);
        }

        public static void set(ContentProviderClient provider, Account account, byte[] data) throws RemoteException {
            Helpers.set(provider, CONTENT_URI, account, data);
        }

        public static ContentProviderOperation newSetOperation(Account account, byte[] data) {
            return Helpers.newSetOperation(CONTENT_URI, account, data);
        }
    }

    @Deprecated
    public interface SyncStateColumns extends Columns {
    }

    public static boolean isProfileId(long id) {
        return id >= Profile.MIN_ID;
    }
}
