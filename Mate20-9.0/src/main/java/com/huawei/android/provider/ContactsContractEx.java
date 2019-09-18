package com.huawei.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

public final class ContactsContractEx {
    public static final Uri CORP_CONTENT_URI = ContactsContract.Contacts.CORP_CONTENT_URI;
    public static final String HIDDEN_COLUMN_PREFIX = "x_";

    public static final class Authorization {
        public static final String AUTHORIZATION_METHOD = "authorize";
        public static final String KEY_AUTHORIZED_URI = "authorized_uri";
        public static final String KEY_URI_TO_AUTHORIZE = "uri_to_authorize";
    }

    protected interface ContactOptionsColumns {
        public static final String LAST_TIME_CONTACTED = "last_time_contacted";
        public static final String LR_LAST_TIME_CONTACTED = "last_time_contacted";
        public static final String LR_TIMES_CONTACTED = "times_contacted";
        public static final String RAW_LAST_TIME_CONTACTED = "x_last_time_contacted";
        public static final String RAW_TIMES_CONTACTED = "x_times_contacted";
        public static final String TIMES_CONTACTED = "times_contacted";
    }

    public static class Contacts implements BaseColumns, ContactOptionsColumns {
        public static final Uri CORP_CONTENT_URI = ContactsContract.Contacts.CORP_CONTENT_URI;
        public static long ENTERPRISE_CONTACT_ID_BASE = ContactsContract.Contacts.ENTERPRISE_CONTACT_ID_BASE;
        public static String ENTERPRISE_CONTACT_LOOKUP_PREFIX = ContactsContract.Contacts.ENTERPRISE_CONTACT_LOOKUP_PREFIX;

        public static final class AggregationSuggestions implements BaseColumns {
            public static final String PARAMETER_MATCH_NAME = "name";

            private AggregationSuggestions() {
            }
        }

        private Contacts() {
        }
    }

    public static final class Data implements BaseColumns, ContactOptionsColumns, DataUsageStatColumns, DataColumns {
        private Data() {
        }
    }

    protected interface DataColumns {
        public static final String HASH_ID = "hash_id";
    }

    protected interface DataUsageStatColumns {
        public static final String LAST_TIME_USED = "last_time_used";
        public static final String LR_LAST_TIME_USED = "last_time_used";
        public static final String LR_TIMES_USED = "times_used";
        public static final String RAW_LAST_TIME_USED = "x_last_time_used";
        public static final String RAW_TIMES_USED = "x_times_used";
        public static final String TIMES_USED = "times_used";
    }

    public static final class Directory implements BaseColumns {
        public static final long ENTERPRISE_DIRECTORY_ID_BASE = 1000000000;
        public static final Uri ENTERPRISE_FILE_URI = ContactsContract.Directory.ENTERPRISE_FILE_URI;

        private Directory() {
        }
    }

    public static final class Groups implements BaseColumns, GroupsColumns {
        private Groups() {
        }
    }

    public interface GroupsColumns {
        public static final String ACCOUNT_TYPE_AND_DATA_SET = "account_type_and_data_set";
        public static final String SUMMARY_GROUP_COUNT_PER_ACCOUNT = "group_count_per_account";
    }

    public static final class Intents {
        public static final String ACTION_PROFILE_CHANGED = "android.provider.Contacts.PROFILE_CHANGED";
    }

    public static final class MetadataSync implements BaseColumns, MetadataSyncColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_metadata";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact_metadata";
        public static final Uri CONTENT_URI = ContactsContract.MetadataSync.CONTENT_URI;
        public static final String METADATA_AUTHORITY = "com.android.contacts.metadata";
        public static final Uri METADATA_AUTHORITY_URI = ContactsContract.MetadataSync.METADATA_AUTHORITY_URI;

        private MetadataSync() {
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

    public static final class MetadataSyncState implements BaseColumns, MetadataSyncStateColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_metadata_sync_state";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact_metadata_sync_state";
        public static final Uri CONTENT_URI = ContactsContract.MetadataSyncState.CONTENT_URI;

        private MetadataSyncState() {
        }
    }

    protected interface MetadataSyncStateColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String DATA_SET = "data_set";
        public static final String STATE = "state";
    }

    public static final class PhoneLookup implements BaseColumns, ContactOptionsColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/phone_lookup";

        private PhoneLookup() {
        }
    }

    public static final class PhotoFiles implements BaseColumns, PhotoFilesColumns {
        private PhotoFiles() {
        }
    }

    protected interface PhotoFilesColumns {
        public static final String FILESIZE = "filesize";
        public static final String HEIGHT = "height";
        public static final String WIDTH = "width";
    }

    public static final class PinnedPositions {
        public static final String UNDEMOTE_METHOD = "undemote";
    }

    public static final class RawContacts implements BaseColumns, ContactOptionsColumns {
        private RawContacts() {
        }
    }

    public static final class RawContactsEntity {
        public static final String FOR_EXPORT_ONLY = "for_export_only";
    }

    public static class SearchSnippets {
        public static final String SNIPPET_ARGS_PARAM_KEY = "snippet_args";
    }

    public static final class StreamItemPhotos implements BaseColumns, StreamItemPhotosColumns {
        public static final String PHOTO = "photo";

        private StreamItemPhotos() {
        }
    }

    protected interface StreamItemPhotosColumns {
        public static final String PHOTO_FILE_ID = "photo_file_id";
        public static final String PHOTO_URI = "photo_uri";
        public static final String SORT_INDEX = "sort_index";
        public static final String STREAM_ITEM_ID = "stream_item_id";
        public static final String SYNC1 = "stream_item_photo_sync1";
        public static final String SYNC2 = "stream_item_photo_sync2";
        public static final String SYNC3 = "stream_item_photo_sync3";
        public static final String SYNC4 = "stream_item_photo_sync4";
    }

    public static final class StreamItems implements BaseColumns, StreamItemsColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/stream_item";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/stream_item";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "stream_items");
        public static final String MAX_ITEMS = "max_items";

        public static final class StreamItemPhotos implements BaseColumns, StreamItemPhotosColumns {
            public static final String CONTENT_DIRECTORY = "photo";
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/stream_item_photo";
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/stream_item_photo";

            private StreamItemPhotos() {
            }
        }

        private StreamItems() {
        }
    }

    protected interface StreamItemsColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String COMMENTS = "comments";
        public static final String CONTACT_ID = "contact_id";
        public static final String CONTACT_LOOKUP_KEY = "contact_lookup";
        public static final String DATA_SET = "data_set";
        public static final String RAW_CONTACT_ID = "raw_contact_id";
        public static final String RAW_CONTACT_SOURCE_ID = "raw_contact_source_id";
        public static final String RES_ICON = "icon";
        public static final String RES_LABEL = "label";
        public static final String RES_PACKAGE = "res_package";
        public static final String SYNC1 = "stream_item_sync1";
        public static final String SYNC2 = "stream_item_sync2";
        public static final String SYNC3 = "stream_item_sync3";
        public static final String SYNC4 = "stream_item_sync4";
        public static final String TEXT = "text";
        public static final String TIMESTAMP = "timestamp";
    }
}
