package javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.namespace.QName;

public abstract class XMLGregorianCalendar implements Cloneable {
    public abstract void add(Duration duration);

    public abstract void clear();

    public abstract Object clone();

    public abstract int compare(XMLGregorianCalendar xMLGregorianCalendar);

    public abstract int getDay();

    public abstract BigInteger getEon();

    public abstract BigInteger getEonAndYear();

    public abstract BigDecimal getFractionalSecond();

    public abstract int getHour();

    public abstract int getMinute();

    public abstract int getMonth();

    public abstract int getSecond();

    public abstract TimeZone getTimeZone(int i);

    public abstract int getTimezone();

    public abstract QName getXMLSchemaType();

    public abstract int getYear();

    public abstract boolean isValid();

    public abstract XMLGregorianCalendar normalize();

    public abstract void reset();

    public abstract void setDay(int i);

    public abstract void setFractionalSecond(BigDecimal bigDecimal);

    public abstract void setHour(int i);

    public abstract void setMillisecond(int i);

    public abstract void setMinute(int i);

    public abstract void setMonth(int i);

    public abstract void setSecond(int i);

    public abstract void setTimezone(int i);

    public abstract void setYear(int i);

    public abstract void setYear(BigInteger bigInteger);

    public abstract GregorianCalendar toGregorianCalendar();

    public abstract GregorianCalendar toGregorianCalendar(TimeZone timeZone, Locale locale, XMLGregorianCalendar xMLGregorianCalendar);

    public abstract String toXMLFormat();

    public void setTime(int hour, int minute, int second) {
        setTime(hour, minute, second, null);
    }

    public void setTime(int hour, int minute, int second, BigDecimal fractional) {
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setFractionalSecond(fractional);
    }

    public void setTime(int hour, int minute, int second, int millisecond) {
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setMillisecond(millisecond);
    }

    public int getMillisecond() {
        if (getFractionalSecond() == null) {
            return Integer.MIN_VALUE;
        }
        return getFractionalSecond().movePointRight(3).intValue();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XMLGregorianCalendar)) {
            return false;
        }
        if (compare((XMLGregorianCalendar) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int timezone = getTimezone();
        if (timezone == Integer.MIN_VALUE) {
            timezone = 0;
        }
        XMLGregorianCalendar gc = this;
        if (timezone != 0) {
            gc = normalize();
        }
        return ((((gc.getYear() + gc.getMonth()) + gc.getDay()) + gc.getHour()) + gc.getMinute()) + gc.getSecond();
    }

    public String toString() {
        return toXMLFormat();
    }
}
