package android.text.style;

import android.os.Parcel;
import android.os.PersistableBundle;
import android.text.ParcelableSpan;
import java.text.NumberFormat;
import java.util.Locale;

public class TtsSpan implements ParcelableSpan {
    public static final String ANIMACY_ANIMATE = "android.animate";
    public static final String ANIMACY_INANIMATE = "android.inanimate";
    public static final String ARG_ANIMACY = "android.arg.animacy";
    public static final String ARG_CASE = "android.arg.case";
    public static final String ARG_COUNTRY_CODE = "android.arg.country_code";
    public static final String ARG_CURRENCY = "android.arg.money";
    public static final String ARG_DAY = "android.arg.day";
    public static final String ARG_DENOMINATOR = "android.arg.denominator";
    public static final String ARG_DIGITS = "android.arg.digits";
    public static final String ARG_DOMAIN = "android.arg.domain";
    public static final String ARG_EXTENSION = "android.arg.extension";
    public static final String ARG_FRACTIONAL_PART = "android.arg.fractional_part";
    public static final String ARG_FRAGMENT_ID = "android.arg.fragment_id";
    public static final String ARG_GENDER = "android.arg.gender";
    public static final String ARG_HOURS = "android.arg.hours";
    public static final String ARG_INTEGER_PART = "android.arg.integer_part";
    public static final String ARG_MINUTES = "android.arg.minutes";
    public static final String ARG_MONTH = "android.arg.month";
    public static final String ARG_MULTIPLICITY = "android.arg.multiplicity";
    public static final String ARG_NUMBER = "android.arg.number";
    public static final String ARG_NUMBER_PARTS = "android.arg.number_parts";
    public static final String ARG_NUMERATOR = "android.arg.numerator";
    public static final String ARG_PASSWORD = "android.arg.password";
    public static final String ARG_PATH = "android.arg.path";
    public static final String ARG_PORT = "android.arg.port";
    public static final String ARG_PROTOCOL = "android.arg.protocol";
    public static final String ARG_QUANTITY = "android.arg.quantity";
    public static final String ARG_QUERY_STRING = "android.arg.query_string";
    public static final String ARG_TEXT = "android.arg.text";
    public static final String ARG_UNIT = "android.arg.unit";
    public static final String ARG_USERNAME = "android.arg.username";
    public static final String ARG_VERBATIM = "android.arg.verbatim";
    public static final String ARG_WEEKDAY = "android.arg.weekday";
    public static final String ARG_YEAR = "android.arg.year";
    public static final String CASE_ABLATIVE = "android.ablative";
    public static final String CASE_ACCUSATIVE = "android.accusative";
    public static final String CASE_DATIVE = "android.dative";
    public static final String CASE_GENITIVE = "android.genitive";
    public static final String CASE_INSTRUMENTAL = "android.instrumental";
    public static final String CASE_LOCATIVE = "android.locative";
    public static final String CASE_NOMINATIVE = "android.nominative";
    public static final String CASE_VOCATIVE = "android.vocative";
    public static final String GENDER_FEMALE = "android.female";
    public static final String GENDER_MALE = "android.male";
    public static final String GENDER_NEUTRAL = "android.neutral";
    public static final int MONTH_APRIL = 3;
    public static final int MONTH_AUGUST = 7;
    public static final int MONTH_DECEMBER = 11;
    public static final int MONTH_FEBRUARY = 1;
    public static final int MONTH_JANUARY = 0;
    public static final int MONTH_JULY = 6;
    public static final int MONTH_JUNE = 5;
    public static final int MONTH_MARCH = 2;
    public static final int MONTH_MAY = 4;
    public static final int MONTH_NOVEMBER = 10;
    public static final int MONTH_OCTOBER = 9;
    public static final int MONTH_SEPTEMBER = 8;
    public static final String MULTIPLICITY_DUAL = "android.dual";
    public static final String MULTIPLICITY_PLURAL = "android.plural";
    public static final String MULTIPLICITY_SINGLE = "android.single";
    public static final String TYPE_CARDINAL = "android.type.cardinal";
    public static final String TYPE_DATE = "android.type.date";
    public static final String TYPE_DECIMAL = "android.type.decimal";
    public static final String TYPE_DIGITS = "android.type.digits";
    public static final String TYPE_ELECTRONIC = "android.type.electronic";
    public static final String TYPE_FRACTION = "android.type.fraction";
    public static final String TYPE_MEASURE = "android.type.measure";
    public static final String TYPE_MONEY = "android.type.money";
    public static final String TYPE_ORDINAL = "android.type.ordinal";
    public static final String TYPE_TELEPHONE = "android.type.telephone";
    public static final String TYPE_TEXT = "android.type.text";
    public static final String TYPE_TIME = "android.type.time";
    public static final String TYPE_VERBATIM = "android.type.verbatim";
    public static final int WEEKDAY_FRIDAY = 6;
    public static final int WEEKDAY_MONDAY = 2;
    public static final int WEEKDAY_SATURDAY = 7;
    public static final int WEEKDAY_SUNDAY = 1;
    public static final int WEEKDAY_THURSDAY = 5;
    public static final int WEEKDAY_TUESDAY = 3;
    public static final int WEEKDAY_WEDNESDAY = 4;
    private final PersistableBundle mArgs;
    private final String mType;

    public static class Builder<C extends Builder<?>> {
        private PersistableBundle mArgs = new PersistableBundle();
        private final String mType;

        public Builder(String type) {
            this.mType = type;
        }

        public TtsSpan build() {
            return new TtsSpan(this.mType, this.mArgs);
        }

        public C setStringArgument(String arg, String value) {
            this.mArgs.putString(arg, value);
            return this;
        }

        public C setIntArgument(String arg, int value) {
            this.mArgs.putInt(arg, value);
            return this;
        }

        public C setLongArgument(String arg, long value) {
            this.mArgs.putLong(arg, value);
            return this;
        }
    }

    public static class SemioticClassBuilder<C extends SemioticClassBuilder<?>> extends Builder<C> {
        public SemioticClassBuilder(String type) {
            super(type);
        }

        public C setGender(String gender) {
            return (SemioticClassBuilder) setStringArgument(TtsSpan.ARG_GENDER, gender);
        }

        public C setAnimacy(String animacy) {
            return (SemioticClassBuilder) setStringArgument(TtsSpan.ARG_ANIMACY, animacy);
        }

        public C setMultiplicity(String multiplicity) {
            return (SemioticClassBuilder) setStringArgument(TtsSpan.ARG_MULTIPLICITY, multiplicity);
        }

        public C setCase(String grammaticalCase) {
            return (SemioticClassBuilder) setStringArgument(TtsSpan.ARG_CASE, grammaticalCase);
        }
    }

    public static class CardinalBuilder extends SemioticClassBuilder<CardinalBuilder> {
        public CardinalBuilder() {
            super(TtsSpan.TYPE_CARDINAL);
        }

        public CardinalBuilder(long number) {
            this();
            setNumber(number);
        }

        public CardinalBuilder(String number) {
            this();
            setNumber(number);
        }

        public CardinalBuilder setNumber(long number) {
            return setNumber(String.valueOf(number));
        }

        public CardinalBuilder setNumber(String number) {
            return (CardinalBuilder) setStringArgument(TtsSpan.ARG_NUMBER, number);
        }
    }

    public static class DateBuilder extends SemioticClassBuilder<DateBuilder> {
        public DateBuilder() {
            super(TtsSpan.TYPE_DATE);
        }

        public DateBuilder(Integer weekday, Integer day, Integer month, Integer year) {
            this();
            if (weekday != null) {
                setWeekday(weekday.intValue());
            }
            if (day != null) {
                setDay(day.intValue());
            }
            if (month != null) {
                setMonth(month.intValue());
            }
            if (year != null) {
                setYear(year.intValue());
            }
        }

        public DateBuilder setWeekday(int weekday) {
            return (DateBuilder) setIntArgument(TtsSpan.ARG_WEEKDAY, weekday);
        }

        public DateBuilder setDay(int day) {
            return (DateBuilder) setIntArgument(TtsSpan.ARG_DAY, day);
        }

        public DateBuilder setMonth(int month) {
            return (DateBuilder) setIntArgument(TtsSpan.ARG_MONTH, month);
        }

        public DateBuilder setYear(int year) {
            return (DateBuilder) setIntArgument(TtsSpan.ARG_YEAR, year);
        }
    }

    public static class DecimalBuilder extends SemioticClassBuilder<DecimalBuilder> {
        public DecimalBuilder() {
            super(TtsSpan.TYPE_DECIMAL);
        }

        public DecimalBuilder(double number, int minimumFractionDigits, int maximumFractionDigits) {
            this();
            setArgumentsFromDouble(number, minimumFractionDigits, maximumFractionDigits);
        }

        public DecimalBuilder(String integerPart, String fractionalPart) {
            this();
            setIntegerPart(integerPart);
            setFractionalPart(fractionalPart);
        }

        public DecimalBuilder setArgumentsFromDouble(double number, int minimumFractionDigits, int maximumFractionDigits) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            formatter.setMinimumFractionDigits(maximumFractionDigits);
            formatter.setMaximumFractionDigits(maximumFractionDigits);
            formatter.setGroupingUsed(false);
            String str = formatter.format(number);
            int i = str.indexOf(46);
            if (i >= 0) {
                setIntegerPart(str.substring(0, i));
                setFractionalPart(str.substring(i + 1));
            } else {
                setIntegerPart(str);
            }
            return this;
        }

        public DecimalBuilder setIntegerPart(long integerPart) {
            return setIntegerPart(String.valueOf(integerPart));
        }

        public DecimalBuilder setIntegerPart(String integerPart) {
            return (DecimalBuilder) setStringArgument(TtsSpan.ARG_INTEGER_PART, integerPart);
        }

        public DecimalBuilder setFractionalPart(String fractionalPart) {
            return (DecimalBuilder) setStringArgument(TtsSpan.ARG_FRACTIONAL_PART, fractionalPart);
        }
    }

    public static class DigitsBuilder extends SemioticClassBuilder<DigitsBuilder> {
        public DigitsBuilder() {
            super(TtsSpan.TYPE_DIGITS);
        }

        public DigitsBuilder(String digits) {
            this();
            setDigits(digits);
        }

        public DigitsBuilder setDigits(String digits) {
            return (DigitsBuilder) setStringArgument(TtsSpan.ARG_DIGITS, digits);
        }
    }

    public static class ElectronicBuilder extends SemioticClassBuilder<ElectronicBuilder> {
        public ElectronicBuilder() {
            super(TtsSpan.TYPE_ELECTRONIC);
        }

        public ElectronicBuilder setEmailArguments(String username, String domain) {
            return setDomain(domain).setUsername(username);
        }

        public ElectronicBuilder setProtocol(String protocol) {
            return (ElectronicBuilder) setStringArgument(TtsSpan.ARG_PROTOCOL, protocol);
        }

        public ElectronicBuilder setUsername(String username) {
            return (ElectronicBuilder) setStringArgument(TtsSpan.ARG_USERNAME, username);
        }

        public ElectronicBuilder setPassword(String password) {
            return (ElectronicBuilder) setStringArgument(TtsSpan.ARG_PASSWORD, password);
        }

        public ElectronicBuilder setDomain(String domain) {
            return (ElectronicBuilder) setStringArgument(TtsSpan.ARG_DOMAIN, domain);
        }

        public ElectronicBuilder setPort(int port) {
            return (ElectronicBuilder) setIntArgument(TtsSpan.ARG_PORT, port);
        }

        public ElectronicBuilder setPath(String path) {
            return (ElectronicBuilder) setStringArgument(TtsSpan.ARG_PATH, path);
        }

        public ElectronicBuilder setQueryString(String queryString) {
            return (ElectronicBuilder) setStringArgument(TtsSpan.ARG_QUERY_STRING, queryString);
        }

        public ElectronicBuilder setFragmentId(String fragmentId) {
            return (ElectronicBuilder) setStringArgument(TtsSpan.ARG_FRAGMENT_ID, fragmentId);
        }
    }

    public static class FractionBuilder extends SemioticClassBuilder<FractionBuilder> {
        public FractionBuilder() {
            super(TtsSpan.TYPE_FRACTION);
        }

        public FractionBuilder(long integerPart, long numerator, long denominator) {
            this();
            setIntegerPart(integerPart);
            setNumerator(numerator);
            setDenominator(denominator);
        }

        public FractionBuilder setIntegerPart(long integerPart) {
            return setIntegerPart(String.valueOf(integerPart));
        }

        public FractionBuilder setIntegerPart(String integerPart) {
            return (FractionBuilder) setStringArgument(TtsSpan.ARG_INTEGER_PART, integerPart);
        }

        public FractionBuilder setNumerator(long numerator) {
            return setNumerator(String.valueOf(numerator));
        }

        public FractionBuilder setNumerator(String numerator) {
            return (FractionBuilder) setStringArgument(TtsSpan.ARG_NUMERATOR, numerator);
        }

        public FractionBuilder setDenominator(long denominator) {
            return setDenominator(String.valueOf(denominator));
        }

        public FractionBuilder setDenominator(String denominator) {
            return (FractionBuilder) setStringArgument(TtsSpan.ARG_DENOMINATOR, denominator);
        }
    }

    public static class MeasureBuilder extends SemioticClassBuilder<MeasureBuilder> {
        public MeasureBuilder() {
            super(TtsSpan.TYPE_MEASURE);
        }

        public MeasureBuilder setNumber(long number) {
            return setNumber(String.valueOf(number));
        }

        public MeasureBuilder setNumber(String number) {
            return (MeasureBuilder) setStringArgument(TtsSpan.ARG_NUMBER, number);
        }

        public MeasureBuilder setIntegerPart(long integerPart) {
            return setIntegerPart(String.valueOf(integerPart));
        }

        public MeasureBuilder setIntegerPart(String integerPart) {
            return (MeasureBuilder) setStringArgument(TtsSpan.ARG_INTEGER_PART, integerPart);
        }

        public MeasureBuilder setFractionalPart(String fractionalPart) {
            return (MeasureBuilder) setStringArgument(TtsSpan.ARG_FRACTIONAL_PART, fractionalPart);
        }

        public MeasureBuilder setNumerator(long numerator) {
            return setNumerator(String.valueOf(numerator));
        }

        public MeasureBuilder setNumerator(String numerator) {
            return (MeasureBuilder) setStringArgument(TtsSpan.ARG_NUMERATOR, numerator);
        }

        public MeasureBuilder setDenominator(long denominator) {
            return setDenominator(String.valueOf(denominator));
        }

        public MeasureBuilder setDenominator(String denominator) {
            return (MeasureBuilder) setStringArgument(TtsSpan.ARG_DENOMINATOR, denominator);
        }

        public MeasureBuilder setUnit(String unit) {
            return (MeasureBuilder) setStringArgument(TtsSpan.ARG_UNIT, unit);
        }
    }

    public static class MoneyBuilder extends SemioticClassBuilder<MoneyBuilder> {
        public MoneyBuilder() {
            super(TtsSpan.TYPE_MONEY);
        }

        public MoneyBuilder setIntegerPart(long integerPart) {
            return setIntegerPart(String.valueOf(integerPart));
        }

        public MoneyBuilder setIntegerPart(String integerPart) {
            return (MoneyBuilder) setStringArgument(TtsSpan.ARG_INTEGER_PART, integerPart);
        }

        public MoneyBuilder setFractionalPart(String fractionalPart) {
            return (MoneyBuilder) setStringArgument(TtsSpan.ARG_FRACTIONAL_PART, fractionalPart);
        }

        public MoneyBuilder setCurrency(String currency) {
            return (MoneyBuilder) setStringArgument(TtsSpan.ARG_CURRENCY, currency);
        }

        public MoneyBuilder setQuantity(String quantity) {
            return (MoneyBuilder) setStringArgument(TtsSpan.ARG_QUANTITY, quantity);
        }
    }

    public static class OrdinalBuilder extends SemioticClassBuilder<OrdinalBuilder> {
        public OrdinalBuilder() {
            super(TtsSpan.TYPE_ORDINAL);
        }

        public OrdinalBuilder(long number) {
            this();
            setNumber(number);
        }

        public OrdinalBuilder(String number) {
            this();
            setNumber(number);
        }

        public OrdinalBuilder setNumber(long number) {
            return setNumber(String.valueOf(number));
        }

        public OrdinalBuilder setNumber(String number) {
            return (OrdinalBuilder) setStringArgument(TtsSpan.ARG_NUMBER, number);
        }
    }

    public static class TelephoneBuilder extends SemioticClassBuilder<TelephoneBuilder> {
        public TelephoneBuilder() {
            super(TtsSpan.TYPE_TELEPHONE);
        }

        public TelephoneBuilder(String numberParts) {
            this();
            setNumberParts(numberParts);
        }

        public TelephoneBuilder setCountryCode(String countryCode) {
            return (TelephoneBuilder) setStringArgument(TtsSpan.ARG_COUNTRY_CODE, countryCode);
        }

        public TelephoneBuilder setNumberParts(String numberParts) {
            return (TelephoneBuilder) setStringArgument(TtsSpan.ARG_NUMBER_PARTS, numberParts);
        }

        public TelephoneBuilder setExtension(String extension) {
            return (TelephoneBuilder) setStringArgument(TtsSpan.ARG_EXTENSION, extension);
        }
    }

    public static class TextBuilder extends SemioticClassBuilder<TextBuilder> {
        public TextBuilder() {
            super(TtsSpan.TYPE_TEXT);
        }

        public TextBuilder(String text) {
            this();
            setText(text);
        }

        public TextBuilder setText(String text) {
            return (TextBuilder) setStringArgument(TtsSpan.ARG_TEXT, text);
        }
    }

    public static class TimeBuilder extends SemioticClassBuilder<TimeBuilder> {
        public TimeBuilder() {
            super(TtsSpan.TYPE_TIME);
        }

        public TimeBuilder(int hours, int minutes) {
            this();
            setHours(hours);
            setMinutes(minutes);
        }

        public TimeBuilder setHours(int hours) {
            return (TimeBuilder) setIntArgument(TtsSpan.ARG_HOURS, hours);
        }

        public TimeBuilder setMinutes(int minutes) {
            return (TimeBuilder) setIntArgument(TtsSpan.ARG_MINUTES, minutes);
        }
    }

    public static class VerbatimBuilder extends SemioticClassBuilder<VerbatimBuilder> {
        public VerbatimBuilder() {
            super(TtsSpan.TYPE_VERBATIM);
        }

        public VerbatimBuilder(String verbatim) {
            this();
            setVerbatim(verbatim);
        }

        public VerbatimBuilder setVerbatim(String verbatim) {
            return (VerbatimBuilder) setStringArgument(TtsSpan.ARG_VERBATIM, verbatim);
        }
    }

    public TtsSpan(String type, PersistableBundle args) {
        this.mType = type;
        this.mArgs = args;
    }

    public TtsSpan(Parcel src) {
        this.mType = src.readString();
        this.mArgs = src.readPersistableBundle();
    }

    public String getType() {
        return this.mType;
    }

    public PersistableBundle getArgs() {
        return this.mArgs;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeString(this.mType);
        dest.writePersistableBundle(this.mArgs);
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 24;
    }
}
