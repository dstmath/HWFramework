package ohos.com.sun.org.apache.xerces.internal.jaxp.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;

class DurationYearMonthImpl extends DurationImpl {
    public DurationYearMonthImpl(boolean z, BigInteger bigInteger, BigInteger bigInteger2) {
        super(z, bigInteger, bigInteger2, (BigInteger) null, (BigInteger) null, (BigInteger) null, (BigDecimal) null);
        convertToCanonicalYearMonth();
    }

    protected DurationYearMonthImpl(boolean z, int i, int i2) {
        this(z, wrap(i), wrap(i2));
    }

    protected DurationYearMonthImpl(long j) {
        super(j);
        convertToCanonicalYearMonth();
        this.days = null;
        this.hours = null;
        this.minutes = null;
        this.seconds = null;
        this.signum = calcSignum(this.signum >= 0);
    }

    protected DurationYearMonthImpl(String str) {
        super(str);
        if (getDays() > 0 || getHours() > 0 || getMinutes() > 0 || getSeconds() > 0) {
            throw new IllegalArgumentException("Trying to create an xdt:yearMonthDuration with an invalid lexical representation of \"" + str + "\", data model requires PnYnM.");
        }
        convertToCanonicalYearMonth();
    }

    public int getValue() {
        return (getYears() * 12) + getMonths();
    }

    private void convertToCanonicalYearMonth() {
        while (getMonths() >= 12) {
            this.months = this.months.subtract(BigInteger.valueOf(12));
            this.years = BigInteger.valueOf((long) getYears()).add(BigInteger.ONE);
        }
    }
}
