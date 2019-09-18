package com.android.internal.telephony;

import android.net.Uri;
import android.provider.BaseColumns;

public class HbpcdLookup {
    public static final String AUTHORITY = "hbpcd_lookup";
    public static final Uri CONTENT_URI = Uri.parse("content://hbpcd_lookup");
    public static final String ID = "_id";
    public static final int IDINDEX = 0;
    public static final String PATH_ARBITRARY_MCC_SID_MATCH = "arbitrary";
    public static final String PATH_MCC_IDD = "idd";
    public static final String PATH_MCC_LOOKUP_TABLE = "lookup";
    public static final String PATH_MCC_SID_CONFLICT = "conflict";
    public static final String PATH_MCC_SID_RANGE = "range";
    public static final String PATH_NANP_AREA_CODE = "nanp";
    public static final String PATH_USERADD_COUNTRY = "useradd";

    public static class ArbitraryMccSidMatch implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://hbpcd_lookup/arbitrary");
        public static final String DEFAULT_SORT_ORDER = "MCC ASC";
        public static final String MCC = "MCC";
        public static final String SID = "SID";
    }

    public static class MccIdd implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://hbpcd_lookup/idd");
        public static final String DEFAULT_SORT_ORDER = "MCC ASC";
        public static final String IDD = "IDD";
        public static final String MCC = "MCC";
    }

    public static class MccLookup implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://hbpcd_lookup/lookup");
        public static final String COUNTRY_CODE = "Country_Code";
        public static final String COUNTRY_NAME = "Country_Name";
        public static final String DEFAULT_SORT_ORDER = "MCC ASC";
        public static final String GMT_DST_HIGH = "GMT_DST_High";
        public static final String GMT_DST_LOW = "GMT_DST_Low";
        public static final String GMT_OFFSET_HIGH = "GMT_Offset_High";
        public static final String GMT_OFFSET_LOW = "GMT_Offset_Low";
        public static final String MCC = "MCC";
        public static final String NANPS = "NANPS";
        public static final String NDD = "NDD";
    }

    public static class MccSidConflicts implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://hbpcd_lookup/conflict");
        public static final String DEFAULT_SORT_ORDER = "MCC ASC";
        public static final String MCC = "MCC";
        public static final String SID_CONFLICT = "SID_Conflict";
    }

    public static class MccSidRange implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://hbpcd_lookup/range");
        public static final String DEFAULT_SORT_ORDER = "MCC ASC";
        public static final String MCC = "MCC";
        public static final String RANGE_HIGH = "SID_Range_High";
        public static final String RANGE_LOW = "SID_Range_Low";
    }

    public static class NanpAreaCode implements BaseColumns {
        public static final String AREA_CODE = "Area_Code";
        public static final Uri CONTENT_URI = Uri.parse("content://hbpcd_lookup/nanp");
        public static final String DEFAULT_SORT_ORDER = "Area_Code ASC";
    }
}
