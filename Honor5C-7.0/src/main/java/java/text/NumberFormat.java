package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.spi.NumberFormatProvider;
import java.util.Currency;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import libcore.icu.LocaleData;
import sun.misc.FloatConsts;
import sun.util.LocaleServiceProviderPool;
import sun.util.LocaleServiceProviderPool.LocalizedObjectGetter;

public abstract class NumberFormat extends Format {
    private static final int CURRENCYSTYLE = 1;
    public static final int FRACTION_FIELD = 1;
    private static final int INTEGERSTYLE = 3;
    public static final int INTEGER_FIELD = 0;
    private static final int NUMBERSTYLE = 0;
    private static final int PERCENTSTYLE = 2;
    private static final Hashtable cachedLocaleData = null;
    static final int currentSerialVersion = 1;
    static final long serialVersionUID = -2308460125733713944L;
    private boolean groupingUsed;
    private byte maxFractionDigits;
    private byte maxIntegerDigits;
    private int maximumFractionDigits;
    private int maximumIntegerDigits;
    private byte minFractionDigits;
    private byte minIntegerDigits;
    private int minimumFractionDigits;
    private int minimumIntegerDigits;
    private boolean parseIntegerOnly;
    private int serialVersionOnStream;

    public static class Field extends java.text.Format.Field {
        public static final Field CURRENCY = null;
        public static final Field DECIMAL_SEPARATOR = null;
        public static final Field EXPONENT = null;
        public static final Field EXPONENT_SIGN = null;
        public static final Field EXPONENT_SYMBOL = null;
        public static final Field FRACTION = null;
        public static final Field GROUPING_SEPARATOR = null;
        public static final Field INTEGER = null;
        public static final Field PERCENT = null;
        public static final Field PERMILLE = null;
        public static final Field SIGN = null;
        private static final Map instanceMap = null;
        private static final long serialVersionUID = 7494728892700160890L;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.NumberFormat.Field.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.NumberFormat.Field.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.text.NumberFormat.Field.<clinit>():void");
        }

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

    private static class NumberFormatGetter implements LocalizedObjectGetter<NumberFormatProvider, NumberFormat> {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final NumberFormatGetter INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.NumberFormat.NumberFormatGetter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.NumberFormat.NumberFormatGetter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.text.NumberFormat.NumberFormatGetter.<clinit>():void");
        }

        private NumberFormatGetter() {
        }

        public /* bridge */ /* synthetic */ Object getObject(Object numberFormatProvider, Locale locale, String key, Object[] params) {
            return getObject((NumberFormatProvider) numberFormatProvider, locale, key, params);
        }

        public NumberFormat getObject(NumberFormatProvider numberFormatProvider, Locale locale, String key, Object... params) {
            int i = NumberFormat.currentSerialVersion;
            if (!-assertionsDisabled) {
                if (params.length != NumberFormat.currentSerialVersion) {
                    i = NumberFormat.NUMBERSTYLE;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            int choice = ((Integer) params[NumberFormat.NUMBERSTYLE]).intValue();
            switch (choice) {
                case NumberFormat.NUMBERSTYLE /*0*/:
                    return numberFormatProvider.getNumberInstance(locale);
                case NumberFormat.currentSerialVersion /*1*/:
                    return numberFormatProvider.getCurrencyInstance(locale);
                case NumberFormat.PERCENTSTYLE /*2*/:
                    return numberFormatProvider.getPercentInstance(locale);
                case NumberFormat.INTEGERSTYLE /*3*/:
                    return numberFormatProvider.getIntegerInstance(locale);
                default:
                    if (-assertionsDisabled) {
                        return null;
                    }
                    throw new AssertionError(Integer.valueOf(choice));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.NumberFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.NumberFormat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.text.NumberFormat.<clinit>():void");
    }

    public abstract StringBuffer format(double d, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(long j, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract Number parse(String str, ParsePosition parsePosition);

    protected NumberFormat() {
        this.groupingUsed = true;
        this.maxIntegerDigits = (byte) 40;
        this.minIntegerDigits = (byte) 1;
        this.maxFractionDigits = (byte) 3;
        this.minFractionDigits = (byte) 0;
        this.parseIntegerOnly = false;
        this.maximumIntegerDigits = 40;
        this.minimumIntegerDigits = currentSerialVersion;
        this.maximumFractionDigits = INTEGERSTYLE;
        this.minimumFractionDigits = NUMBERSTYLE;
        this.serialVersionOnStream = currentSerialVersion;
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
        ParsePosition parsePosition = new ParsePosition(NUMBERSTYLE);
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
        return getInstance(Locale.getDefault(Category.FORMAT), NUMBERSTYLE);
    }

    public static NumberFormat getInstance(Locale inLocale) {
        return getInstance(inLocale, NUMBERSTYLE);
    }

    public static final NumberFormat getNumberInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), NUMBERSTYLE);
    }

    public static NumberFormat getNumberInstance(Locale inLocale) {
        return getInstance(inLocale, NUMBERSTYLE);
    }

    public static final NumberFormat getIntegerInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), INTEGERSTYLE);
    }

    public static NumberFormat getIntegerInstance(Locale inLocale) {
        return getInstance(inLocale, INTEGERSTYLE);
    }

    public static final NumberFormat getCurrencyInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), currentSerialVersion);
    }

    public static NumberFormat getCurrencyInstance(Locale inLocale) {
        return getInstance(inLocale, currentSerialVersion);
    }

    public static final NumberFormat getPercentInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT), PERCENTSTYLE);
    }

    public static NumberFormat getPercentInstance(Locale inLocale) {
        return getInstance(inLocale, PERCENTSTYLE);
    }

    public static Locale[] getAvailableLocales() {
        return LocaleServiceProviderPool.getPool(NumberFormatProvider.class).getAvailableLocales();
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
        this.maximumIntegerDigits = Math.max((int) NUMBERSTYLE, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.minimumIntegerDigits = this.maximumIntegerDigits;
        }
    }

    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    public void setMinimumIntegerDigits(int newValue) {
        this.minimumIntegerDigits = Math.max((int) NUMBERSTYLE, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.maximumIntegerDigits = this.minimumIntegerDigits;
        }
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public void setMaximumFractionDigits(int newValue) {
        this.maximumFractionDigits = Math.max((int) NUMBERSTYLE, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.minimumFractionDigits = this.maximumFractionDigits;
        }
    }

    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    public void setMinimumFractionDigits(int newValue) {
        this.minimumFractionDigits = Math.max((int) NUMBERSTYLE, newValue);
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
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(NumberFormatProvider.class);
        if (pool.hasProviders()) {
            LocalizedObjectGetter -get0 = NumberFormatGetter.INSTANCE;
            Object[] objArr = new Object[currentSerialVersion];
            objArr[NUMBERSTYLE] = Integer.valueOf(choice);
            NumberFormat providersInstance = (NumberFormat) pool.getLocalizedObject(-get0, desiredLocale, objArr);
            if (providersInstance != null) {
                return providersInstance;
            }
        }
        String[] numberPatterns = (String[]) cachedLocaleData.get(desiredLocale);
        if (numberPatterns == null) {
            LocaleData data = LocaleData.get(desiredLocale);
            numberPatterns = new String[]{data.numberPattern, data.currencyPattern, data.percentPattern, data.integerPattern};
            cachedLocaleData.put(desiredLocale, numberPatterns);
        }
        DecimalFormat format = new DecimalFormat(numberPatterns[choice == INTEGERSTYLE ? NUMBERSTYLE : choice], DecimalFormatSymbols.getInstance(desiredLocale));
        if (choice == INTEGERSTYLE) {
            format.setMaximumFractionDigits(NUMBERSTYLE);
            format.setDecimalSeparatorAlwaysShown(false);
            format.setParseIntegerOnly(true);
        } else if (choice == currentSerialVersion) {
            format.adjustForCurrencyDefaultFractionDigits();
        }
        return format;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < currentSerialVersion) {
            this.maximumIntegerDigits = this.maxIntegerDigits;
            this.minimumIntegerDigits = this.minIntegerDigits;
            this.maximumFractionDigits = this.maxFractionDigits;
            this.minimumFractionDigits = this.minFractionDigits;
        }
        if (this.minimumIntegerDigits > this.maximumIntegerDigits || this.minimumFractionDigits > this.maximumFractionDigits || this.minimumIntegerDigits < 0 || this.minimumFractionDigits < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        this.serialVersionOnStream = currentSerialVersion;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        byte b;
        byte b2 = Byte.MAX_VALUE;
        if (this.maximumIntegerDigits > FloatConsts.MAX_EXPONENT) {
            b = Byte.MAX_VALUE;
        } else {
            b = (byte) this.maximumIntegerDigits;
        }
        this.maxIntegerDigits = b;
        if (this.minimumIntegerDigits > FloatConsts.MAX_EXPONENT) {
            b = Byte.MAX_VALUE;
        } else {
            b = (byte) this.minimumIntegerDigits;
        }
        this.minIntegerDigits = b;
        if (this.maximumFractionDigits > FloatConsts.MAX_EXPONENT) {
            b = Byte.MAX_VALUE;
        } else {
            b = (byte) this.maximumFractionDigits;
        }
        this.maxFractionDigits = b;
        if (this.minimumFractionDigits <= FloatConsts.MAX_EXPONENT) {
            b2 = (byte) this.minimumFractionDigits;
        }
        this.minFractionDigits = b2;
        stream.defaultWriteObject();
    }
}
