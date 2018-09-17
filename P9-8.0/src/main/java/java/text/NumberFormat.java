package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

public abstract class NumberFormat extends Format {
    private static final int CURRENCYSTYLE = 1;
    public static final int FRACTION_FIELD = 1;
    private static final int INTEGERSTYLE = 3;
    public static final int INTEGER_FIELD = 0;
    private static final int NUMBERSTYLE = 0;
    private static final int PERCENTSTYLE = 2;
    private static final Hashtable cachedLocaleData = new Hashtable(3);
    static final int currentSerialVersion = 1;
    static final long serialVersionUID = -2308460125733713944L;
    private boolean groupingUsed = true;
    private byte maxFractionDigits = (byte) 3;
    private byte maxIntegerDigits = (byte) 40;
    private int maximumFractionDigits = 3;
    private int maximumIntegerDigits = 40;
    private byte minFractionDigits = (byte) 0;
    private byte minIntegerDigits = (byte) 1;
    private int minimumFractionDigits = 0;
    private int minimumIntegerDigits = 1;
    private boolean parseIntegerOnly = false;
    private int serialVersionOnStream = 1;

    public static class Field extends java.text.Format.Field {
        public static final Field CURRENCY = new Field("currency");
        public static final Field DECIMAL_SEPARATOR = new Field("decimal separator");
        public static final Field EXPONENT = new Field("exponent");
        public static final Field EXPONENT_SIGN = new Field("exponent sign");
        public static final Field EXPONENT_SYMBOL = new Field("exponent symbol");
        public static final Field FRACTION = new Field("fraction");
        public static final Field GROUPING_SEPARATOR = new Field("grouping separator");
        public static final Field INTEGER = new Field("integer");
        public static final Field PERCENT = new Field("percent");
        public static final Field PERMILLE = new Field("per mille");
        public static final Field SIGN = new Field("sign");
        private static final Map<String, Field> instanceMap = new HashMap(11);
        private static final long serialVersionUID = 7494728892700160890L;

        protected Field(String name) {
            super(name);
            if (getClass() == Field.class) {
                instanceMap.put(name, this);
            }
        }

        protected Object readResolve() throws InvalidObjectException {
            if (getClass() != Field.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            Object instance = instanceMap.get(getName());
            if (instance != null) {
                return instance;
            }
            throw new InvalidObjectException("unknown attribute name");
        }
    }

    public abstract StringBuffer format(double d, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(long j, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract Number parse(String str, ParsePosition parsePosition);

    protected NumberFormat() {
    }

    public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if ((number instanceof Long) || (number instanceof Integer) || (number instanceof Short) || (number instanceof Byte) || (number instanceof AtomicInteger) || (number instanceof AtomicLong) || ((number instanceof BigInteger) && ((BigInteger) number).bitLength() < 64)) {
            return format(((Number) number).longValue(), toAppendTo, pos);
        }
        if (number instanceof Number) {
            return format(((Number) number).doubleValue(), toAppendTo, pos);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Number");
    }

    public final Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    public final String format(double number) {
        return format(number, new StringBuffer(), DontCareFieldPosition.INSTANCE).toString();
    }

    public final String format(long number) {
        return format(number, new StringBuffer(), DontCareFieldPosition.INSTANCE).toString();
    }

    public Number parse(String source) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Number result = parse(source, parsePosition);
        if (parsePosition.index != 0) {
            return result;
        }
        throw new ParseException("Unparseable number: \"" + source + "\"", parsePosition.errorIndex);
    }

    public boolean isParseIntegerOnly() {
        return this.parseIntegerOnly;
    }

    public void setParseIntegerOnly(boolean value) {
        this.parseIntegerOnly = value;
    }

    public static final NumberFormat getInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), 0);
    }

    public static NumberFormat getInstance(Locale inLocale) {
        return getInstance(inLocale, 0);
    }

    public static final NumberFormat getNumberInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), 0);
    }

    public static NumberFormat getNumberInstance(Locale inLocale) {
        return getInstance(inLocale, 0);
    }

    public static final NumberFormat getIntegerInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), 3);
    }

    public static NumberFormat getIntegerInstance(Locale inLocale) {
        return getInstance(inLocale, 3);
    }

    public static final NumberFormat getCurrencyInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), 1);
    }

    public static NumberFormat getCurrencyInstance(Locale inLocale) {
        return getInstance(inLocale, 1);
    }

    public static final NumberFormat getPercentInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), 2);
    }

    public static NumberFormat getPercentInstance(Locale inLocale) {
        return getInstance(inLocale, 2);
    }

    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableLocales();
    }

    public int hashCode() {
        return (this.maximumIntegerDigits * 37) + this.maxFractionDigits;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NumberFormat other = (NumberFormat) obj;
        if (this.maximumIntegerDigits != other.maximumIntegerDigits || this.minimumIntegerDigits != other.minimumIntegerDigits || this.maximumFractionDigits != other.maximumFractionDigits || this.minimumFractionDigits != other.minimumFractionDigits || this.groupingUsed != other.groupingUsed) {
            z = false;
        } else if (this.parseIntegerOnly != other.parseIntegerOnly) {
            z = false;
        }
        return z;
    }

    public Object clone() {
        return (NumberFormat) super.clone();
    }

    public boolean isGroupingUsed() {
        return this.groupingUsed;
    }

    public void setGroupingUsed(boolean newValue) {
        this.groupingUsed = newValue;
    }

    public int getMaximumIntegerDigits() {
        return this.maximumIntegerDigits;
    }

    public void setMaximumIntegerDigits(int newValue) {
        this.maximumIntegerDigits = Math.max(0, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.minimumIntegerDigits = this.maximumIntegerDigits;
        }
    }

    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    public void setMinimumIntegerDigits(int newValue) {
        this.minimumIntegerDigits = Math.max(0, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.maximumIntegerDigits = this.minimumIntegerDigits;
        }
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public void setMaximumFractionDigits(int newValue) {
        this.maximumFractionDigits = Math.max(0, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.minimumFractionDigits = this.maximumFractionDigits;
        }
    }

    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    public void setMinimumFractionDigits(int newValue) {
        this.minimumFractionDigits = Math.max(0, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.maximumFractionDigits = this.minimumFractionDigits;
        }
    }

    public Currency getCurrency() {
        throw new UnsupportedOperationException();
    }

    public void setCurrency(Currency currency) {
        throw new UnsupportedOperationException();
    }

    public RoundingMode getRoundingMode() {
        throw new UnsupportedOperationException();
    }

    public void setRoundingMode(RoundingMode roundingMode) {
        throw new UnsupportedOperationException();
    }

    private static NumberFormat getInstance(Locale desiredLocale, int choice) {
        String[] numberPatterns = (String[]) cachedLocaleData.get(desiredLocale);
        if (numberPatterns == null) {
            LocaleData data = LocaleData.get(desiredLocale);
            numberPatterns = new String[]{data.numberPattern, data.currencyPattern, data.percentPattern, data.integerPattern};
            cachedLocaleData.put(desiredLocale, numberPatterns);
        }
        DecimalFormat format = new DecimalFormat(numberPatterns[choice == 3 ? 0 : choice], DecimalFormatSymbols.getInstance(desiredLocale));
        if (choice == 3) {
            format.setMaximumFractionDigits(0);
            format.setDecimalSeparatorAlwaysShown(false);
            format.setParseIntegerOnly(true);
        } else if (choice == 1) {
            format.adjustForCurrencyDefaultFractionDigits();
        }
        return format;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            this.maximumIntegerDigits = this.maxIntegerDigits;
            this.minimumIntegerDigits = this.minIntegerDigits;
            this.maximumFractionDigits = this.maxFractionDigits;
            this.minimumFractionDigits = this.minFractionDigits;
        }
        if (this.minimumIntegerDigits > this.maximumIntegerDigits || this.minimumFractionDigits > this.maximumFractionDigits || this.minimumIntegerDigits < 0 || this.minimumFractionDigits < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        this.serialVersionOnStream = 1;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        byte b = Byte.MAX_VALUE;
        this.maxIntegerDigits = this.maximumIntegerDigits > 127 ? Byte.MAX_VALUE : (byte) this.maximumIntegerDigits;
        this.minIntegerDigits = this.minimumIntegerDigits > 127 ? Byte.MAX_VALUE : (byte) this.minimumIntegerDigits;
        this.maxFractionDigits = this.maximumFractionDigits > 127 ? Byte.MAX_VALUE : (byte) this.maximumFractionDigits;
        if (this.minimumFractionDigits <= 127) {
            b = (byte) this.minimumFractionDigits;
        }
        this.minFractionDigits = b;
        stream.defaultWriteObject();
    }
}
