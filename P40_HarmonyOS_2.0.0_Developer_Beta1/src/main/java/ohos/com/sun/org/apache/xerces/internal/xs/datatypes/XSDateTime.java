package ohos.com.sun.org.apache.xerces.internal.xs.datatypes;

import ohos.javax.xml.datatype.Duration;
import ohos.javax.xml.datatype.XMLGregorianCalendar;

public interface XSDateTime {
    int getDays();

    Duration getDuration();

    int getHours();

    String getLexicalValue();

    int getMinutes();

    int getMonths();

    double getSeconds();

    int getTimeZoneHours();

    int getTimeZoneMinutes();

    XMLGregorianCalendar getXMLGregorianCalendar();

    int getYears();

    boolean hasTimeZone();

    boolean isNormalized();

    XSDateTime normalize();
}
