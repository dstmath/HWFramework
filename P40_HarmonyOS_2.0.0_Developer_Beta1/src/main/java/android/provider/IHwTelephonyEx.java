package android.provider;

import android.net.Uri;

public interface IHwTelephonyEx {

    public static final class GlobalMatchs implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://telephony/globalMatchs");
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final String ECC_FAKE = "ecc_fake";
        public static final String ECC_NOCARD = "ecc_nocard";
        public static final String ECC_WITHCARD = "ecc_withcard";
        public static final String MCC = "mcc";
        public static final String MNC = "mnc";
        public static final String NAME = "name";
        public static final String NUMERIC = "numeric";
    }
}
