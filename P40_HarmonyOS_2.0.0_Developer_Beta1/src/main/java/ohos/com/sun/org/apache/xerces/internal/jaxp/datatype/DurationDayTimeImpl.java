package ohos.com.sun.org.apache.xerces.internal.jaxp.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;

class DurationDayTimeImpl extends DurationImpl {
    public DurationDayTimeImpl(boolean z, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigDecimal bigDecimal) {
        super(z, (BigInteger) null, (BigInteger) null, bigInteger, bigInteger2, bigInteger3, bigDecimal);
        convertToCanonicalDayTime();
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public DurationDayTimeImpl(boolean z, int i, int i2, int i3, int i4) {
        this(z, wrap(i), wrap(i2), wrap(i3), i4 != Integer.MIN_VALUE ? new BigDecimal(String.valueOf(i4)) : null);
    }

    protected DurationDayTimeImpl(String str) {
        super(str);
        if (getYears() > 0 || getMonths() > 0) {
            throw new IllegalArgumentException("Trying to create an xdt:dayTimeDuration with an invalid lexical representation of \"" + str + "\", data model requires a format PnDTnHnMnS.");
        }
        convertToCanonicalDayTime();
    }

    protected DurationDayTimeImpl(long j) {
        super(j);
        convertToCanonicalDayTime();
        this.years = null;
        this.months = null;
    }

    public float getValue() {
        return ((float) (((((getDays() * 24) + getHours()) * 60) + getMinutes()) * 60)) + (this.seconds == null ? 0.0f : this.seconds.floatValue());
    }

    private void convertToCanonicalDayTime() {
        while (getSeconds() >= 60) {
            this.seconds = this.seconds.subtract(BigDecimal.valueOf(60L));
            this.minutes = BigInteger.valueOf((long) getMinutes()).add(BigInteger.ONE);
        }
        while (getMinutes() >= 60) {
            this.minutes = this.minutes.subtract(BigInteger.valueOf(60));
            this.hours = BigInteger.valueOf((long) getHours()).add(BigInteger.ONE);
        }
        while (getHours() >= 24) {
            this.hours = this.hours.subtract(BigInteger.valueOf(24));
            this.days = BigInteger.valueOf((long) getDays()).add(BigInteger.ONE);
        }
    }
}
