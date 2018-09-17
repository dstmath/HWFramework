package android.icu.text;

import android.icu.impl.Grego;
import android.icu.impl.Utility;
import android.icu.util.Currency.CurrencyUsage;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CurrencyMetaInfo {
    @Deprecated
    protected static final CurrencyDigits defaultDigits = new CurrencyDigits(2, 0);
    private static final boolean hasData;
    private static final CurrencyMetaInfo impl;

    public static final class CurrencyDigits {
        public final int fractionDigits;
        public final int roundingIncrement;

        public CurrencyDigits(int fractionDigits, int roundingIncrement) {
            this.fractionDigits = fractionDigits;
            this.roundingIncrement = roundingIncrement;
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }
    }

    public static final class CurrencyFilter {
        private static final CurrencyFilter ALL = new CurrencyFilter(null, null, Long.MIN_VALUE, Long.MAX_VALUE, false);
        public final String currency;
        public final long from;
        public final String region;
        @Deprecated
        public final boolean tenderOnly;
        public final long to;

        private CurrencyFilter(String region, String currency, long from, long to, boolean tenderOnly) {
            this.region = region;
            this.currency = currency;
            this.from = from;
            this.to = to;
            this.tenderOnly = tenderOnly;
        }

        public static CurrencyFilter all() {
            return ALL;
        }

        public static CurrencyFilter now() {
            return ALL.withDate(new Date());
        }

        public static CurrencyFilter onRegion(String region) {
            return ALL.withRegion(region);
        }

        public static CurrencyFilter onCurrency(String currency) {
            return ALL.withCurrency(currency);
        }

        public static CurrencyFilter onDate(Date date) {
            return ALL.withDate(date);
        }

        public static CurrencyFilter onDateRange(Date from, Date to) {
            return ALL.withDateRange(from, to);
        }

        public static CurrencyFilter onDate(long date) {
            return ALL.withDate(date);
        }

        public static CurrencyFilter onDateRange(long from, long to) {
            return ALL.withDateRange(from, to);
        }

        public static CurrencyFilter onTender() {
            return ALL.withTender();
        }

        public CurrencyFilter withRegion(String region) {
            return new CurrencyFilter(region, this.currency, this.from, this.to, this.tenderOnly);
        }

        public CurrencyFilter withCurrency(String currency) {
            return new CurrencyFilter(this.region, currency, this.from, this.to, this.tenderOnly);
        }

        public CurrencyFilter withDate(Date date) {
            return new CurrencyFilter(this.region, this.currency, date.getTime(), date.getTime(), this.tenderOnly);
        }

        public CurrencyFilter withDateRange(Date from, Date to) {
            return new CurrencyFilter(this.region, this.currency, from == null ? Long.MIN_VALUE : from.getTime(), to == null ? Long.MAX_VALUE : to.getTime(), this.tenderOnly);
        }

        public CurrencyFilter withDate(long date) {
            return new CurrencyFilter(this.region, this.currency, date, date, this.tenderOnly);
        }

        public CurrencyFilter withDateRange(long from, long to) {
            return new CurrencyFilter(this.region, this.currency, from, to, this.tenderOnly);
        }

        public CurrencyFilter withTender() {
            return new CurrencyFilter(this.region, this.currency, this.from, this.to, true);
        }

        public boolean equals(Object rhs) {
            if (rhs instanceof CurrencyFilter) {
                return equals((CurrencyFilter) rhs);
            }
            return false;
        }

        public boolean equals(CurrencyFilter rhs) {
            if (Utility.sameObjects(this, rhs)) {
                return true;
            }
            if (rhs != null && equals(this.region, rhs.region) && equals(this.currency, rhs.currency) && this.from == rhs.from && this.to == rhs.to) {
                return this.tenderOnly == rhs.tenderOnly;
            } else {
                return false;
            }
        }

        public int hashCode() {
            int hc = 0;
            if (this.region != null) {
                hc = this.region.hashCode();
            }
            if (this.currency != null) {
                hc = (hc * 31) + this.currency.hashCode();
            }
            return (((((((((hc * 31) + ((int) this.from)) * 31) + ((int) (this.from >>> 32))) * 31) + ((int) this.to)) * 31) + ((int) (this.to >>> 32))) * 31) + (this.tenderOnly ? 1 : 0);
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }

        private static boolean equals(String lhs, String rhs) {
            if (Utility.sameObjects(lhs, rhs)) {
                return true;
            }
            return lhs != null ? lhs.equals(rhs) : false;
        }
    }

    public static final class CurrencyInfo {
        public final String code;
        public final long from;
        public final int priority;
        public final String region;
        private final boolean tender;
        public final long to;

        @Deprecated
        public CurrencyInfo(String region, String code, long from, long to, int priority) {
            this(region, code, from, to, priority, true);
        }

        @Deprecated
        public CurrencyInfo(String region, String code, long from, long to, int priority, boolean tender) {
            this.region = region;
            this.code = code;
            this.from = from;
            this.to = to;
            this.priority = priority;
            this.tender = tender;
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }

        public boolean isTender() {
            return this.tender;
        }
    }

    public static CurrencyMetaInfo getInstance() {
        return impl;
    }

    public static CurrencyMetaInfo getInstance(boolean noSubstitute) {
        return hasData ? impl : null;
    }

    @Deprecated
    public static boolean hasData() {
        return hasData;
    }

    @Deprecated
    protected CurrencyMetaInfo() {
    }

    public List<CurrencyInfo> currencyInfo(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public List<String> currencies(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public List<String> regions(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public CurrencyDigits currencyDigits(String isoCode) {
        return currencyDigits(isoCode, CurrencyUsage.STANDARD);
    }

    public CurrencyDigits currencyDigits(String isoCode, CurrencyUsage currencyUsage) {
        return defaultDigits;
    }

    static {
        CurrencyMetaInfo temp;
        boolean tempHasData = false;
        try {
            temp = (CurrencyMetaInfo) Class.forName("android.icu.impl.ICUCurrencyMetaInfo").newInstance();
            tempHasData = true;
        } catch (Throwable th) {
            temp = new CurrencyMetaInfo();
        }
        impl = temp;
        hasData = tempHasData;
    }

    private static String dateString(long date) {
        if (date == Long.MAX_VALUE || date == Long.MIN_VALUE) {
            return null;
        }
        return Grego.timeToString(date);
    }

    private static String debugString(Object o) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Field f : o.getClass().getFields()) {
                Object v = f.get(o);
                if (v != null) {
                    String s;
                    if (v instanceof Date) {
                        s = dateString(((Date) v).getTime());
                    } else if (v instanceof Long) {
                        s = dateString(((Long) v).longValue());
                    } else {
                        s = String.valueOf(v);
                    }
                    if (s != null) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(f.getName()).append("='").append(s).append("'");
                    }
                }
            }
        } catch (Throwable th) {
        }
        sb.insert(0, o.getClass().getSimpleName() + "(");
        sb.append(")");
        return sb.toString();
    }
}
