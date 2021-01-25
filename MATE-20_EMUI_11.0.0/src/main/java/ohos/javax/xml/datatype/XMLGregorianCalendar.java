package ohos.javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import ohos.javax.xml.namespace.QName;

public abstract class XMLGregorianCalendar implements Cloneable {
    public abstract void add(Duration duration);

    public abstract void clear();

    @Override // java.lang.Object
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

    public void setTime(int i, int i2, int i3) {
        setTime(i, i2, i3, (BigDecimal) null);
    }

    public void setTime(int i, int i2, int i3, BigDecimal bigDecimal) {
        setHour(i);
        setMinute(i2);
        setSecond(i3);
        setFractionalSecond(bigDecimal);
    }

    public void setTime(int i, int i2, int i3, int i4) {
        setHour(i);
        setMinute(i2);
        setSecond(i3);
        setMillisecond(i4);
    }

    public int getMillisecond() {
        if (getFractionalSecond() == null) {
            return Integer.MIN_VALUE;
        }
        return getFractionalSecond().movePointRight(3).intValue();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof XMLGregorianCalendar) || compare((XMLGregorianCalendar) obj) != 0) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        int timezone = getTimezone();
        if (timezone == Integer.MIN_VALUE) {
            timezone = 0;
        }
        if (timezone != 0) {
            this = normalize();
        }
        return this.getYear() + this.getMonth() + this.getDay() + this.getHour() + this.getMinute() + this.getSecond();
    }

    @Override // java.lang.Object
    public String toString() {
        return toXMLFormat();
    }
}
