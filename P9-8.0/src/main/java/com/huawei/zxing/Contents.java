package com.huawei.zxing;

public final class Contents {
    public static final String DATA = "ENCODE_DATA";
    public static final String[] EMAIL_KEYS = new String[]{"email", "secondary_email", "tertiary_email"};
    public static final String[] EMAIL_TYPE_KEYS = new String[]{"email_type", "secondary_email_type", "tertiary_email_type"};
    public static final String NOTE_KEY = "NOTE_KEY";
    public static final String[] PHONE_KEYS = new String[]{"phone", "secondary_phone", "tertiary_phone"};
    public static final String[] PHONE_TYPE_KEYS = new String[]{"phone_type", "secondary_phone_type", "tertiary_phone_type"};
    public static final String URL_KEY = "URL_KEY";

    public static final class Type {
        public static final String CONTACT = "CONTACT_TYPE";
        public static final String EMAIL = "EMAIL_TYPE";
        public static final String LOCATION = "LOCATION_TYPE";
        public static final String PHONE = "PHONE_TYPE";
        public static final String SMS = "SMS_TYPE";
        public static final String TEXT = "TEXT_TYPE";

        private Type() {
        }
    }

    private Contents() {
    }
}
