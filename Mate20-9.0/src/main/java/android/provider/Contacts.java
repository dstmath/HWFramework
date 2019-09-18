package android.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Deprecated
public class Contacts {
    @Deprecated
    public static final String AUTHORITY = "contacts";
    @Deprecated
    public static final Uri CONTENT_URI = Uri.parse("content://contacts");
    @Deprecated
    public static final int KIND_EMAIL = 1;
    @Deprecated
    public static final int KIND_IM = 3;
    @Deprecated
    public static final int KIND_ORGANIZATION = 4;
    @Deprecated
    public static final int KIND_PHONE = 5;
    @Deprecated
    public static final int KIND_POSTAL = 2;
    private static final String TAG = "Contacts";

    @Deprecated
    public static final class ContactMethods implements BaseColumns, ContactMethodsColumns, PeopleColumns {
        @Deprecated
        public static final String CONTENT_EMAIL_ITEM_TYPE = "vnd.android.cursor.item/email";
        @Deprecated
        public static final String CONTENT_EMAIL_TYPE = "vnd.android.cursor.dir/email";
        @Deprecated
        public static final Uri CONTENT_EMAIL_URI = Uri.parse("content://contacts/contact_methods/email");
        @Deprecated
        public static final String CONTENT_IM_ITEM_TYPE = "vnd.android.cursor.item/jabber-im";
        @Deprecated
        public static final String CONTENT_POSTAL_ITEM_TYPE = "vnd.android.cursor.item/postal-address";
        @Deprecated
        public static final String CONTENT_POSTAL_TYPE = "vnd.android.cursor.dir/postal-address";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact-methods";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/contact_methods");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final String POSTAL_LOCATION_LATITUDE = "data";
        @Deprecated
        public static final String POSTAL_LOCATION_LONGITUDE = "aux_data";
        @Deprecated
        public static final int PROTOCOL_AIM = 0;
        @Deprecated
        public static final int PROTOCOL_GOOGLE_TALK = 5;
        @Deprecated
        public static final int PROTOCOL_ICQ = 6;
        @Deprecated
        public static final int PROTOCOL_JABBER = 7;
        @Deprecated
        public static final int PROTOCOL_MSN = 1;
        @Deprecated
        public static final int PROTOCOL_QQ = 4;
        @Deprecated
        public static final int PROTOCOL_SKYPE = 3;
        @Deprecated
        public static final int PROTOCOL_YAHOO = 2;

        interface ProviderNames {
            public static final String AIM = "AIM";
            public static final String GTALK = "GTalk";
            public static final String ICQ = "ICQ";
            public static final String JABBER = "JABBER";
            public static final String MSN = "MSN";
            public static final String QQ = "QQ";
            public static final String SKYPE = "SKYPE";
            public static final String XMPP = "XMPP";
            public static final String YAHOO = "Yahoo";
        }

        @Deprecated
        public static String encodePredefinedImProtocol(int protocol) {
            return "pre:" + protocol;
        }

        @Deprecated
        public static String encodeCustomImProtocol(String protocolString) {
            return "custom:" + protocolString;
        }

        @Deprecated
        public static Object decodeImProtocol(String encodedString) {
            if (encodedString == null) {
                return null;
            }
            if (encodedString.startsWith("pre:")) {
                return Integer.valueOf(Integer.parseInt(encodedString.substring(4)));
            }
            if (encodedString.startsWith("custom:")) {
                return encodedString.substring(7);
            }
            throw new IllegalArgumentException("the value is not a valid encoded protocol, " + encodedString);
        }

        @Deprecated
        public static String lookupProviderNameFromId(int protocol) {
            switch (protocol) {
                case 0:
                    return ProviderNames.AIM;
                case 1:
                    return ProviderNames.MSN;
                case 2:
                    return ProviderNames.YAHOO;
                case 3:
                    return ProviderNames.SKYPE;
                case 4:
                    return ProviderNames.QQ;
                case 5:
                    return ProviderNames.GTALK;
                case 6:
                    return ProviderNames.ICQ;
                case 7:
                    return ProviderNames.JABBER;
                default:
                    return null;
            }
        }

        private ContactMethods() {
        }

        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int kind, int type, CharSequence label) {
            switch (kind) {
                case 1:
                    if (type != 0) {
                        CharSequence[] labels = context.getResources().getTextArray(17235968);
                        try {
                            return labels[type - 1];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            return labels[0];
                        }
                    } else if (!TextUtils.isEmpty(label)) {
                        return label;
                    } else {
                        return "";
                    }
                case 2:
                    if (type != 0) {
                        CharSequence[] labels2 = context.getResources().getTextArray(17235972);
                        try {
                            return labels2[type - 1];
                        } catch (ArrayIndexOutOfBoundsException e2) {
                            return labels2[0];
                        }
                    } else if (!TextUtils.isEmpty(label)) {
                        return label;
                    } else {
                        return "";
                    }
                default:
                    return context.getString(17039375);
            }
        }

        @Deprecated
        public void addPostalLocation(Context context, long postalId, double latitude, double longitude) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues(2);
            values.put("data", Double.valueOf(latitude));
            values.put("aux_data", Double.valueOf(longitude));
            long locId = ContentUris.parseId(resolver.insert(CONTENT_URI, values));
            values.clear();
            values.put("aux_data", Long.valueOf(locId));
            resolver.update(ContentUris.withAppendedId(CONTENT_URI, postalId), values, null, null);
        }
    }

    @Deprecated
    public interface ContactMethodsColumns {
        @Deprecated
        public static final String AUX_DATA = "aux_data";
        @Deprecated
        public static final String DATA = "data";
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
        @Deprecated
        public static final String KIND = "kind";
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final int MOBILE_EMAIL_TYPE_INDEX = 2;
        @Deprecated
        public static final String MOBILE_EMAIL_TYPE_NAME = "_AUTO_CELL";
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_HOME = 1;
        @Deprecated
        public static final int TYPE_OTHER = 3;
        @Deprecated
        public static final int TYPE_WORK = 2;
    }

    @Deprecated
    public static final class Extensions implements BaseColumns, ExtensionsColumns {
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_extensions";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact_extensions";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/extensions");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "person, name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";

        private Extensions() {
        }
    }

    @Deprecated
    public interface ExtensionsColumns {
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String VALUE = "value";
    }

    @Deprecated
    public static final class GroupMembership implements BaseColumns, GroupsColumns {
        @Deprecated
        public static final String CONTENT_DIRECTORY = "groupmembership";
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contactsgroupmembership";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contactsgroupmembership";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/groupmembership");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "group_id ASC";
        @Deprecated
        public static final String GROUP_ID = "group_id";
        @Deprecated
        public static final String GROUP_SYNC_ACCOUNT = "group_sync_account";
        @Deprecated
        public static final String GROUP_SYNC_ACCOUNT_TYPE = "group_sync_account_type";
        @Deprecated
        public static final String GROUP_SYNC_ID = "group_sync_id";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final Uri RAW_CONTENT_URI = Uri.parse("content://contacts/groupmembershipraw");

        private GroupMembership() {
        }
    }

    @Deprecated
    public static final class Groups implements BaseColumns, GroupsColumns {
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contactsgroup";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contactsgroup";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/groups");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final Uri DELETED_CONTENT_URI = Uri.parse("content://contacts/deleted_groups");
        @Deprecated
        public static final String GROUP_ANDROID_STARRED = "Starred in Android";
        @Deprecated
        public static final String GROUP_MY_CONTACTS = "Contacts";

        private Groups() {
        }
    }

    @Deprecated
    public interface GroupsColumns {
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String NOTES = "notes";
        @Deprecated
        public static final String SHOULD_SYNC = "should_sync";
        @Deprecated
        public static final String SYSTEM_ID = "system_id";
    }

    @Deprecated
    public static final class Intents {
        @Deprecated
        public static final String ATTACH_IMAGE = "com.android.contacts.action.ATTACH_IMAGE";
        @Deprecated
        public static final String EXTRA_CREATE_DESCRIPTION = "com.android.contacts.action.CREATE_DESCRIPTION";
        @Deprecated
        public static final String EXTRA_FORCE_CREATE = "com.android.contacts.action.FORCE_CREATE";
        @Deprecated
        public static final String EXTRA_TARGET_RECT = "target_rect";
        @Deprecated
        public static final String SEARCH_SUGGESTION_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_CLICKED";
        @Deprecated
        public static final String SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED";
        @Deprecated
        public static final String SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED";
        @Deprecated
        public static final String SHOW_OR_CREATE_CONTACT = "com.android.contacts.action.SHOW_OR_CREATE_CONTACT";

        @Deprecated
        public static final class Insert {
            @Deprecated
            public static final String ACTION = "android.intent.action.INSERT";
            @Deprecated
            public static final String COMPANY = "company";
            @Deprecated
            public static final String EMAIL = "email";
            @Deprecated
            public static final String EMAIL_ISPRIMARY = "email_isprimary";
            @Deprecated
            public static final String EMAIL_TYPE = "email_type";
            @Deprecated
            public static final String FULL_MODE = "full_mode";
            @Deprecated
            public static final String IM_HANDLE = "im_handle";
            @Deprecated
            public static final String IM_ISPRIMARY = "im_isprimary";
            @Deprecated
            public static final String IM_PROTOCOL = "im_protocol";
            @Deprecated
            public static final String JOB_TITLE = "job_title";
            @Deprecated
            public static final String NAME = "name";
            @Deprecated
            public static final String NOTES = "notes";
            @Deprecated
            public static final String PHONE = "phone";
            @Deprecated
            public static final String PHONETIC_NAME = "phonetic_name";
            @Deprecated
            public static final String PHONE_ISPRIMARY = "phone_isprimary";
            @Deprecated
            public static final String PHONE_TYPE = "phone_type";
            @Deprecated
            public static final String POSTAL = "postal";
            @Deprecated
            public static final String POSTAL_ISPRIMARY = "postal_isprimary";
            @Deprecated
            public static final String POSTAL_TYPE = "postal_type";
            @Deprecated
            public static final String SECONDARY_EMAIL = "secondary_email";
            @Deprecated
            public static final String SECONDARY_EMAIL_TYPE = "secondary_email_type";
            @Deprecated
            public static final String SECONDARY_PHONE = "secondary_phone";
            @Deprecated
            public static final String SECONDARY_PHONE_TYPE = "secondary_phone_type";
            @Deprecated
            public static final String TERTIARY_EMAIL = "tertiary_email";
            @Deprecated
            public static final String TERTIARY_EMAIL_TYPE = "tertiary_email_type";
            @Deprecated
            public static final String TERTIARY_PHONE = "tertiary_phone";
            @Deprecated
            public static final String TERTIARY_PHONE_TYPE = "tertiary_phone_type";
        }

        @Deprecated
        public static final class UI {
            @Deprecated
            public static final String FILTER_CONTACTS_ACTION = "com.android.contacts.action.FILTER_CONTACTS";
            @Deprecated
            public static final String FILTER_TEXT_EXTRA_KEY = "com.android.contacts.extra.FILTER_TEXT";
            @Deprecated
            public static final String GROUP_NAME_EXTRA_KEY = "com.android.contacts.extra.GROUP";
            @Deprecated
            public static final String LIST_ALL_CONTACTS_ACTION = "com.android.contacts.action.LIST_ALL_CONTACTS";
            @Deprecated
            public static final String LIST_CONTACTS_WITH_PHONES_ACTION = "com.android.contacts.action.LIST_CONTACTS_WITH_PHONES";
            @Deprecated
            public static final String LIST_DEFAULT = "com.android.contacts.action.LIST_DEFAULT";
            @Deprecated
            public static final String LIST_FREQUENT_ACTION = "com.android.contacts.action.LIST_FREQUENT";
            @Deprecated
            public static final String LIST_GROUP_ACTION = "com.android.contacts.action.LIST_GROUP";
            @Deprecated
            public static final String LIST_STARRED_ACTION = "com.android.contacts.action.LIST_STARRED";
            @Deprecated
            public static final String LIST_STREQUENT_ACTION = "com.android.contacts.action.LIST_STREQUENT";
            @Deprecated
            public static final String TITLE_EXTRA_KEY = "com.android.contacts.extra.TITLE_EXTRA";
        }
    }

    @Deprecated
    public interface OrganizationColumns {
        @Deprecated
        public static final String COMPANY = "company";
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final String TITLE = "title";
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_OTHER = 2;
        @Deprecated
        public static final int TYPE_WORK = 1;
    }

    @Deprecated
    public static final class Organizations implements BaseColumns, OrganizationColumns {
        @Deprecated
        public static final String CONTENT_DIRECTORY = "organizations";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/organizations");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "company, title, isprimary ASC";

        private Organizations() {
        }

        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int type, CharSequence label) {
            if (type != 0) {
                CharSequence[] labels = context.getResources().getTextArray(17235970);
                try {
                    return labels[type - 1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    return labels[0];
                }
            } else if (!TextUtils.isEmpty(label)) {
                return label;
            } else {
                return "";
            }
        }
    }

    @Deprecated
    public static final class People implements BaseColumns, PeopleColumns, PhonesColumns, PresenceColumns {
        @Deprecated
        public static final Uri CONTENT_FILTER_URI = Uri.parse("content://contacts/people/filter");
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/person";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/person";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/people");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final Uri DELETED_CONTENT_URI = Uri.parse("content://contacts/deleted_people");
        private static final String[] GROUPS_PROJECTION = {"_id"};
        @Deprecated
        public static final String PRIMARY_EMAIL_ID = "primary_email";
        @Deprecated
        public static final String PRIMARY_ORGANIZATION_ID = "primary_organization";
        @Deprecated
        public static final String PRIMARY_PHONE_ID = "primary_phone";
        @Deprecated
        public static final Uri WITH_EMAIL_OR_IM_FILTER_URI = Uri.parse("content://contacts/people/with_email_or_im_filter");

        @Deprecated
        public static final class ContactMethods implements BaseColumns, ContactMethodsColumns, PeopleColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "contact_methods";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "data ASC";

            private ContactMethods() {
            }
        }

        @Deprecated
        public static class Extensions implements BaseColumns, ExtensionsColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "extensions";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "name ASC";
            @Deprecated
            public static final String PERSON_ID = "person";

            @Deprecated
            private Extensions() {
            }
        }

        @Deprecated
        public static final class Phones implements BaseColumns, PhonesColumns, PeopleColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "phones";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "number ASC";

            private Phones() {
            }
        }

        @Deprecated
        private People() {
        }

        @Deprecated
        public static void markAsContacted(ContentResolver resolver, long personId) {
        }

        @Deprecated
        public static long tryGetMyContactsGroupId(ContentResolver resolver) {
            Cursor groupsCursor = resolver.query(Groups.CONTENT_URI, GROUPS_PROJECTION, "system_id='Contacts'", null, null);
            if (groupsCursor != null) {
                try {
                    if (groupsCursor.moveToFirst()) {
                        return groupsCursor.getLong(0);
                    }
                    groupsCursor.close();
                } finally {
                    groupsCursor.close();
                }
            }
            return 0;
        }

        @Deprecated
        public static Uri addToMyContactsGroup(ContentResolver resolver, long personId) {
            long groupId = tryGetMyContactsGroupId(resolver);
            if (groupId != 0) {
                return addToGroup(resolver, personId, groupId);
            }
            throw new IllegalStateException("Failed to find the My Contacts group");
        }

        @Deprecated
        public static Uri addToGroup(ContentResolver resolver, long personId, String groupName) {
            long groupId = 0;
            Cursor groupsCursor = resolver.query(Groups.CONTENT_URI, GROUPS_PROJECTION, "name=?", new String[]{groupName}, null);
            if (groupsCursor != null) {
                try {
                    if (groupsCursor.moveToFirst()) {
                        groupId = groupsCursor.getLong(0);
                    }
                } finally {
                    groupsCursor.close();
                }
            }
            if (groupId != 0) {
                return addToGroup(resolver, personId, groupId);
            }
            throw new IllegalStateException("Failed to find the My Contacts group");
        }

        @Deprecated
        public static Uri addToGroup(ContentResolver resolver, long personId, long groupId) {
            ContentValues values = new ContentValues();
            values.put("person", Long.valueOf(personId));
            values.put(GroupMembership.GROUP_ID, Long.valueOf(groupId));
            return resolver.insert(GroupMembership.CONTENT_URI, values);
        }

        @Deprecated
        public static Uri createPersonInMyContactsGroup(ContentResolver resolver, ContentValues values) {
            Uri contactUri = resolver.insert(CONTENT_URI, values);
            if (contactUri == null) {
                Log.e("Contacts", "Failed to create the contact");
                return null;
            } else if (addToMyContactsGroup(resolver, ContentUris.parseId(contactUri)) != null) {
                return contactUri;
            } else {
                resolver.delete(contactUri, null, null);
                return null;
            }
        }

        @Deprecated
        public static Cursor queryGroups(ContentResolver resolver, long person) {
            return resolver.query(GroupMembership.CONTENT_URI, null, "person=?", new String[]{String.valueOf(person)}, "name ASC");
        }

        @Deprecated
        public static void setPhotoData(ContentResolver cr, Uri person, byte[] data) {
            Uri photoUri = Uri.withAppendedPath(person, "photo");
            ContentValues values = new ContentValues();
            values.put("data", data);
            cr.update(photoUri, values, null, null);
        }

        @Deprecated
        public static InputStream openContactPhotoInputStream(ContentResolver cr, Uri person) {
            Cursor cursor = cr.query(Uri.withAppendedPath(person, "photo"), new String[]{"data"}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        byte[] data = cursor.getBlob(0);
                        if (data != null) {
                            return new ByteArrayInputStream(data);
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        return null;
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

        @Deprecated
        public static Bitmap loadContactPhoto(Context context, Uri person, int placeholderImageResource, BitmapFactory.Options options) {
            if (person == null) {
                return loadPlaceholderPhoto(placeholderImageResource, context, options);
            }
            InputStream stream = openContactPhotoInputStream(context.getContentResolver(), person);
            Bitmap bm = null;
            if (stream != null) {
                bm = BitmapFactory.decodeStream(stream, null, options);
            }
            if (bm == null) {
                bm = loadPlaceholderPhoto(placeholderImageResource, context, options);
            }
            return bm;
        }

        private static Bitmap loadPlaceholderPhoto(int placeholderImageResource, Context context, BitmapFactory.Options options) {
            if (placeholderImageResource == 0) {
                return null;
            }
            return BitmapFactory.decodeResource(context.getResources(), placeholderImageResource, options);
        }
    }

    @Deprecated
    public interface PeopleColumns {
        @Deprecated
        public static final String CUSTOM_RINGTONE = "custom_ringtone";
        @Deprecated
        public static final String DISPLAY_NAME = "display_name";
        @Deprecated
        public static final String LAST_TIME_CONTACTED = "last_time_contacted";
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String NOTES = "notes";
        @Deprecated
        public static final String PHONETIC_NAME = "phonetic_name";
        @Deprecated
        public static final String PHOTO_VERSION = "photo_version";
        @Deprecated
        public static final String SEND_TO_VOICEMAIL = "send_to_voicemail";
        @Deprecated
        public static final String SORT_STRING = "sort_string";
        @Deprecated
        public static final String STARRED = "starred";
        @Deprecated
        public static final String TIMES_CONTACTED = "times_contacted";
    }

    @Deprecated
    public static final class Phones implements BaseColumns, PhonesColumns, PeopleColumns {
        @Deprecated
        public static final Uri CONTENT_FILTER_URL = Uri.parse("content://contacts/phones/filter");
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/phone";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/phones");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";

        private Phones() {
        }

        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int type, CharSequence label, CharSequence[] labelArray) {
            if (type != 0) {
                CharSequence[] labels = labelArray != null ? labelArray : context.getResources().getTextArray(17235971);
                try {
                    return labels[type - 1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    return labels[0];
                }
            } else if (!TextUtils.isEmpty(label)) {
                return label;
            } else {
                return "";
            }
        }

        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int type, CharSequence label) {
            return getDisplayLabel(context, type, label, null);
        }
    }

    @Deprecated
    public interface PhonesColumns {
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final String NUMBER = "number";
        @Deprecated
        public static final String NUMBER_KEY = "number_key";
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_FAX_HOME = 5;
        @Deprecated
        public static final int TYPE_FAX_WORK = 4;
        @Deprecated
        public static final int TYPE_HOME = 1;
        @Deprecated
        public static final int TYPE_MOBILE = 2;
        @Deprecated
        public static final int TYPE_OTHER = 7;
        @Deprecated
        public static final int TYPE_PAGER = 6;
        @Deprecated
        public static final int TYPE_WORK = 3;
    }

    @Deprecated
    public static final class Photos implements BaseColumns, PhotosColumns {
        @Deprecated
        public static final String CONTENT_DIRECTORY = "photo";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/photos");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "person ASC";

        private Photos() {
        }
    }

    @Deprecated
    public interface PhotosColumns {
        @Deprecated
        public static final String DATA = "data";
        @Deprecated
        public static final String DOWNLOAD_REQUIRED = "download_required";
        @Deprecated
        public static final String EXISTS_ON_SERVER = "exists_on_server";
        @Deprecated
        public static final String LOCAL_VERSION = "local_version";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final String SYNC_ERROR = "sync_error";
    }

    @Deprecated
    public static final class Presence implements BaseColumns, PresenceColumns, PeopleColumns {
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/presence");
        @Deprecated
        public static final String PERSON_ID = "person";

        @Deprecated
        public static final int getPresenceIconResourceId(int status) {
            switch (status) {
                case 1:
                    return 17301609;
                case 2:
                case 3:
                    return 17301607;
                case 4:
                    return 17301608;
                case 5:
                    return 17301611;
                default:
                    return 17301610;
            }
        }

        @Deprecated
        public static final void setPresenceIcon(ImageView icon, int serverStatus) {
            icon.setImageResource(getPresenceIconResourceId(serverStatus));
        }
    }

    @Deprecated
    public interface PresenceColumns {
        public static final int AVAILABLE = 5;
        public static final int AWAY = 2;
        public static final int DO_NOT_DISTURB = 4;
        public static final int IDLE = 3;
        @Deprecated
        public static final String IM_ACCOUNT = "im_account";
        @Deprecated
        public static final String IM_HANDLE = "im_handle";
        @Deprecated
        public static final String IM_PROTOCOL = "im_protocol";
        public static final int INVISIBLE = 1;
        public static final int OFFLINE = 0;
        public static final String PRESENCE_CUSTOM_STATUS = "status";
        public static final String PRESENCE_STATUS = "mode";
        public static final String PRIORITY = "priority";
    }

    @Deprecated
    public static final class Settings implements BaseColumns, SettingsColumns {
        @Deprecated
        public static final String CONTENT_DIRECTORY = "settings";
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content://contacts/settings");
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "key ASC";
        @Deprecated
        public static final String SYNC_EVERYTHING = "syncEverything";

        private Settings() {
        }

        @Deprecated
        public static String getSetting(ContentResolver cr, String account, String key) {
            ContentResolver contentResolver = cr;
            Cursor cursor = contentResolver.query(CONTENT_URI, new String[]{"value"}, "key=?", new String[]{key}, null);
            try {
                if (!cursor.moveToNext()) {
                    return null;
                }
                String string = cursor.getString(0);
                cursor.close();
                return string;
            } finally {
                cursor.close();
            }
        }

        @Deprecated
        public static void setSetting(ContentResolver cr, String account, String key, String value) {
            ContentValues values = new ContentValues();
            values.put("key", key);
            values.put("value", value);
            cr.update(CONTENT_URI, values, null, null);
        }
    }

    @Deprecated
    public interface SettingsColumns {
        @Deprecated
        public static final String KEY = "key";
        @Deprecated
        public static final String VALUE = "value";
        @Deprecated
        public static final String _SYNC_ACCOUNT = "_sync_account";
        @Deprecated
        public static final String _SYNC_ACCOUNT_TYPE = "_sync_account_type";
    }

    private Contacts() {
    }
}
