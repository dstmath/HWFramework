package android.icu.text;

import android.icu.impl.Grego;
import android.icu.util.Currency.CurrencyUsage;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CurrencyMetaInfo {
    @Deprecated
    protected static final CurrencyDigits defaultDigits = null;
    private static final boolean hasData = false;
    private static final CurrencyMetaInfo impl = null;

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
        private static final CurrencyFilter ALL = null;
        public final String currency;
        public final long from;
        public final String region;
        @Deprecated
        public final boolean tenderOnly;
        public final long to;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CurrencyMetaInfo.CurrencyFilter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CurrencyMetaInfo.CurrencyFilter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CurrencyMetaInfo.CurrencyFilter.<clinit>():void");
        }

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
            if (this == rhs) {
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
            if (lhs != rhs) {
                return lhs != null ? lhs.equals(rhs) : false;
            } else {
                return true;
            }
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CurrencyMetaInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CurrencyMetaInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CurrencyMetaInfo.<clinit>():void");
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
