package javax.xml.datatype;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

public final class DatatypeConstants {
    public static final int APRIL = 4;
    public static final int AUGUST = 8;
    public static final QName DATE = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "date");
    public static final QName DATETIME = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "dateTime");
    public static final Field DAYS = new Field("DAYS", 2, null);
    public static final int DECEMBER = 12;
    public static final QName DURATION = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "duration");
    public static final QName DURATION_DAYTIME = new QName(XMLConstants.W3C_XPATH_DATATYPE_NS_URI, "dayTimeDuration");
    public static final QName DURATION_YEARMONTH = new QName(XMLConstants.W3C_XPATH_DATATYPE_NS_URI, "yearMonthDuration");
    public static final int EQUAL = 0;
    public static final int FEBRUARY = 2;
    public static final int FIELD_UNDEFINED = Integer.MIN_VALUE;
    public static final QName GDAY = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gDay");
    public static final QName GMONTH = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gMonth");
    public static final QName GMONTHDAY = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gMonthDay");
    public static final int GREATER = 1;
    public static final QName GYEAR = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gYear");
    public static final QName GYEARMONTH = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gYearMonth");
    public static final Field HOURS = new Field("HOURS", 3, null);
    public static final int INDETERMINATE = 2;
    public static final int JANUARY = 1;
    public static final int JULY = 7;
    public static final int JUNE = 6;
    public static final int LESSER = -1;
    public static final int MARCH = 3;
    public static final int MAX_TIMEZONE_OFFSET = -840;
    public static final int MAY = 5;
    public static final Field MINUTES = new Field("MINUTES", 4, null);
    public static final int MIN_TIMEZONE_OFFSET = 840;
    public static final Field MONTHS = new Field("MONTHS", 1, null);
    public static final int NOVEMBER = 11;
    public static final int OCTOBER = 10;
    public static final Field SECONDS = new Field("SECONDS", 5, null);
    public static final int SEPTEMBER = 9;
    public static final QName TIME = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "time");
    public static final Field YEARS = new Field("YEARS", 0, null);

    public static final class Field {
        private final int id;
        private final String str;

        /* synthetic */ Field(String str, int id, Field -this2) {
            this(str, id);
        }

        private Field(String str, int id) {
            this.str = str;
            this.id = id;
        }

        public String toString() {
            return this.str;
        }

        public int getId() {
            return this.id;
        }
    }

    private DatatypeConstants() {
    }
}
