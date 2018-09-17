package android.provider;

public class SearchIndexablesContract {
    public static final int COLUMN_INDEX_NON_INDEXABLE_KEYS_KEY_VALUE = 0;
    public static final int COLUMN_INDEX_RAW_CLASS_NAME = 7;
    public static final int COLUMN_INDEX_RAW_ENTRIES = 4;
    public static final int COLUMN_INDEX_RAW_ICON_RESID = 8;
    public static final int COLUMN_INDEX_RAW_INTENT_ACTION = 9;
    public static final int COLUMN_INDEX_RAW_INTENT_TARGET_CLASS = 11;
    public static final int COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE = 10;
    public static final int COLUMN_INDEX_RAW_KEY = 12;
    public static final int COLUMN_INDEX_RAW_KEYWORDS = 5;
    public static final int COLUMN_INDEX_RAW_PAYLOAD = 15;
    public static final int COLUMN_INDEX_RAW_PAYLOAD_TYPE = 14;
    public static final int COLUMN_INDEX_RAW_RANK = 0;
    public static final int COLUMN_INDEX_RAW_SCREEN_TITLE = 6;
    public static final int COLUMN_INDEX_RAW_SUMMARY_OFF = 3;
    public static final int COLUMN_INDEX_RAW_SUMMARY_ON = 2;
    public static final int COLUMN_INDEX_RAW_TITLE = 1;
    public static final int COLUMN_INDEX_RAW_USER_ID = 13;
    public static final int COLUMN_INDEX_XML_RES_CLASS_NAME = 2;
    public static final int COLUMN_INDEX_XML_RES_ICON_RESID = 3;
    public static final int COLUMN_INDEX_XML_RES_INTENT_ACTION = 4;
    public static final int COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS = 6;
    public static final int COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE = 5;
    public static final int COLUMN_INDEX_XML_RES_RANK = 0;
    public static final int COLUMN_INDEX_XML_RES_RESID = 1;
    public static final String INDEXABLES_RAW = "indexables_raw";
    public static final String[] INDEXABLES_RAW_COLUMNS = new String[]{BaseColumns.COLUMN_RANK, "title", RawData.COLUMN_SUMMARY_ON, RawData.COLUMN_SUMMARY_OFF, RawData.COLUMN_ENTRIES, RawData.COLUMN_KEYWORDS, RawData.COLUMN_SCREEN_TITLE, BaseColumns.COLUMN_CLASS_NAME, BaseColumns.COLUMN_ICON_RESID, BaseColumns.COLUMN_INTENT_ACTION, BaseColumns.COLUMN_INTENT_TARGET_PACKAGE, BaseColumns.COLUMN_INTENT_TARGET_CLASS, "key", "user_id", RawData.PAYLOAD_TYPE, RawData.PAYLOAD};
    public static final String INDEXABLES_RAW_PATH = "settings/indexables_raw";
    public static final String INDEXABLES_XML_RES = "indexables_xml_res";
    public static final String[] INDEXABLES_XML_RES_COLUMNS = new String[]{BaseColumns.COLUMN_RANK, XmlResource.COLUMN_XML_RESID, BaseColumns.COLUMN_CLASS_NAME, BaseColumns.COLUMN_ICON_RESID, BaseColumns.COLUMN_INTENT_ACTION, BaseColumns.COLUMN_INTENT_TARGET_PACKAGE, BaseColumns.COLUMN_INTENT_TARGET_CLASS};
    public static final String INDEXABLES_XML_RES_PATH = "settings/indexables_xml_res";
    public static final String NON_INDEXABLES_KEYS = "non_indexables_key";
    public static final String[] NON_INDEXABLES_KEYS_COLUMNS = new String[]{"key"};
    public static final String NON_INDEXABLES_KEYS_PATH = "settings/non_indexables_key";
    public static final String PROVIDER_INTERFACE = "android.content.action.SEARCH_INDEXABLES_PROVIDER";
    private static final String SETTINGS = "settings";

    public static class BaseColumns {
        public static final String COLUMN_CLASS_NAME = "className";
        public static final String COLUMN_ICON_RESID = "iconResId";
        public static final String COLUMN_INTENT_ACTION = "intentAction";
        public static final String COLUMN_INTENT_TARGET_CLASS = "intentTargetClass";
        public static final String COLUMN_INTENT_TARGET_PACKAGE = "intentTargetPackage";
        public static final String COLUMN_RANK = "rank";

        /* synthetic */ BaseColumns(BaseColumns -this0) {
            this();
        }

        private BaseColumns() {
        }
    }

    public static final class NonIndexableKey extends BaseColumns {
        public static final String COLUMN_KEY_VALUE = "key";
        public static final String MIME_TYPE = "vnd.android.cursor.dir/non_indexables_key";

        private NonIndexableKey() {
            super();
        }
    }

    public static final class RawData extends BaseColumns {
        public static final String COLUMN_ENTRIES = "entries";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_KEYWORDS = "keywords";
        public static final String COLUMN_SCREEN_TITLE = "screenTitle";
        public static final String COLUMN_SUMMARY_OFF = "summaryOff";
        public static final String COLUMN_SUMMARY_ON = "summaryOn";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String MIME_TYPE = "vnd.android.cursor.dir/indexables_raw";
        public static final String PAYLOAD = "payload";
        public static final String PAYLOAD_TYPE = "payload_type";

        private RawData() {
            super();
        }
    }

    public static final class XmlResource extends BaseColumns {
        public static final String COLUMN_XML_RESID = "xmlResId";
        public static final String MIME_TYPE = "vnd.android.cursor.dir/indexables_xml_res";

        private XmlResource() {
            super();
        }
    }
}
