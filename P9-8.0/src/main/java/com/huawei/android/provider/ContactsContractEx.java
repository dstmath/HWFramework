package com.huawei.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

public final class ContactsContractEx {
    public static final Uri CORP_CONTENT_URI = Contacts.CORP_CONTENT_URI;

    public static final class Authorization {
        public static final String AUTHORIZATION_METHOD = "authorize";
        public static final String KEY_AUTHORIZED_URI = "authorized_uri";
        public static final String KEY_URI_TO_AUTHORIZE = "uri_to_authorize";
    }

    public interface GroupsColumns {
        public static final String ACCOUNT_TYPE_AND_DATA_SET = "account_type_and_data_set";
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

    public static final class RawContactsEntity {
        public static final String FOR_EXPORT_ONLY = "for_export_only";
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

    public static final class StreamItems implements BaseColumns, StreamItemsColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "stream_items");

        public static final class StreamItemPhotos implements BaseColumns, StreamItemPhotosColumns {
            public static final String CONTENT_DIRECTORY = "photo";

            private StreamItemPhotos() {
            }
        }

        private StreamItems() {
        }
    }
}
