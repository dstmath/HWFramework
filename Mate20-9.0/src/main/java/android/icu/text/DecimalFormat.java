package android.icu.text;

import android.icu.impl.number.AffixUtils;
import android.icu.impl.number.DecimalFormatProperties;
import android.icu.impl.number.Padder;
import android.icu.impl.number.Parse;
import android.icu.impl.number.PatternStringParser;
import android.icu.impl.number.PatternStringUtils;
import android.icu.impl.number.Properties;
import android.icu.math.MathContext;
import android.icu.number.FormattedNumber;
import android.icu.number.LocalizedNumberFormatter;
import android.icu.number.NumberFormatter;
import android.icu.text.PluralRules;
import android.icu.util.Currency;
import android.icu.util.CurrencyAmount;
import android.icu.util.Measure;
import android.icu.util.ULocale;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;

public class DecimalFormat extends NumberFormat {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int PAD_AFTER_PREFIX = 1;
    public static final int PAD_AFTER_SUFFIX = 3;
    public static final int PAD_BEFORE_PREFIX = 0;
    public static final int PAD_BEFORE_SUFFIX = 2;
    private static final long serialVersionUID = 864413376551465018L;
    private static final ThreadLocal<DecimalFormatProperties> threadLocalProperties = new ThreadLocal<DecimalFormatProperties>() {
        /* access modifiers changed from: protected */
        public DecimalFormatProperties initialValue() {
            return new DecimalFormatProperties();
        }
    };
    volatile transient DecimalFormatProperties exportedProperties;
    volatile transient LocalizedNumberFormatter formatter;
    private transient int icuMathContextForm;
    transient DecimalFormatProperties properties;
    private final int serialVersionOnStream;
    volatile transient DecimalFormatSymbols symbols;

    @Deprecated
    public interface PropertySetter {
        @Deprecated
        void set(DecimalFormatProperties decimalFormatProperties);
    }

    public DecimalFormat() {
        this.serialVersionOnStream = 5;
        this.icuMathContextForm = 0;
        String pattern = getPattern(ULocale.getDefault(ULocale.Category.FORMAT), 0);
        this.symbols = getDefaultSymbols();
        this.properties = new DecimalFormatProperties();
        this.exportedProperties = new DecimalFormatProperties();
        setPropertiesFromPattern(pattern, 1);
        refreshFormatter();
    }

    public DecimalFormat(String pattern) {
        this.serialVersionOnStream = 5;
        this.icuMathContextForm = 0;
        this.symbols = getDefaultSymbols();
        this.properties = new DecimalFormatProperties();
        this.exportedProperties = new DecimalFormatProperties();
        setPropertiesFromPattern(pattern, 1);
        refreshFormatter();
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols2) {
        this.serialVersionOnStream = 5;
        this.icuMathContextForm = 0;
        this.symbols = (DecimalFormatSymbols) symbols2.clone();
        this.properties = new DecimalFormatProperties();
        this.exportedProperties = new DecimalFormatProperties();
        setPropertiesFromPattern(pattern, 1);
        refreshFormatter();
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols2, CurrencyPluralInfo infoInput, int style) {
        this(pattern, symbols2, style);
        this.properties.setCurrencyPluralInfo(infoInput);
        refreshFormatter();
    }

    DecimalFormat(String pattern, DecimalFormatSymbols symbols2, int choice) {
        this.serialVersionOnStream = 5;
        this.icuMathContextForm = 0;
        this.symbols = (DecimalFormatSymbols) symbols2.clone();
        this.properties = new DecimalFormatProperties();
        this.exportedProperties = new DecimalFormatProperties();
        if (choice == 1 || choice == 5 || choice == 7 || choice == 8 || choice == 9 || choice == 6) {
            setPropertiesFromPattern(pattern, 2);
        } else {
            setPropertiesFromPattern(pattern, 1);
        }
        refreshFormatter();
    }

    private static DecimalFormatSymbols getDefaultSymbols() {
        return DecimalFormatSymbols.getInstance();
    }

    public synchronized void applyPattern(String pattern) {
        setPropertiesFromPattern(pattern, 0);
        this.properties.setPositivePrefix(null);
        this.properties.setNegativePrefix(null);
        this.properties.setPositiveSuffix(null);
        this.properties.setNegativeSuffix(null);
        this.properties.setCurrencyPluralInfo(null);
        refreshFormatter();
    }

    public synchronized void applyLocalizedPattern(String localizedPattern) {
        applyPattern(PatternStringUtils.convertLocalized(localizedPattern, this.symbols, false));
    }

    public Object clone() {
        DecimalFormat other = (DecimalFormat) super.clone();
        other.symbols = (DecimalFormatSymbols) this.symbols.clone();
        other.properties = this.properties.clone();
        other.exportedProperties = new DecimalFormatProperties();
        other.refreshFormatter();
        return other;
    }

    private synchronized void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(0);
        oos.writeObject(this.properties);
        oos.writeObject(this.symbols);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fieldGetter = ois.readFields();
        ObjectStreamField[] serializedFields = fieldGetter.getObjectStreamClass().getFields();
        int serialVersion = fieldGetter.get("serialVersionOnStream", -1);
        if (serialVersion > 5) {
            throw new IOException("Cannot deserialize newer android.icu.text.DecimalFormat (v" + serialVersion + ")");
        } else if (serialVersion != 5) {
            this.properties = new DecimalFormatProperties();
            String ppp = null;
            String psp = null;
            String np = null;
            String npp = null;
            String ns = null;
            String nsp = null;
            int length = serializedFields.length;
            String ps = null;
            String pp = null;
            int i = 0;
            while (i < length) {
                ObjectStreamField field = serializedFields[i];
                String name = field.getName();
                ObjectStreamField[] serializedFields2 = serializedFields;
                if (name.equals("decimalSeparatorAlwaysShown")) {
                    ObjectStreamField objectStreamField = field;
                    setDecimalSeparatorAlwaysShown(fieldGetter.get("decimalSeparatorAlwaysShown", false));
                } else {
                    if (name.equals("exponentSignAlwaysShown")) {
                        setExponentSignAlwaysShown(fieldGetter.get("exponentSignAlwaysShown", false));
                    } else if (name.equals("formatWidth")) {
                        setFormatWidth(fieldGetter.get("formatWidth", 0));
                    } else if (name.equals("groupingSize")) {
                        setGroupingSize(fieldGetter.get("groupingSize", (byte) 3));
                    } else if (name.equals("groupingSize2")) {
                        setSecondaryGroupingSize(fieldGetter.get("groupingSize2", (byte) 0));
                    } else if (name.equals("maxSignificantDigits")) {
                        setMaximumSignificantDigits(fieldGetter.get("maxSignificantDigits", 6));
                    } else if (name.equals("minExponentDigits")) {
                        setMinimumExponentDigits(fieldGetter.get("minExponentDigits", (byte) 0));
                    } else if (name.equals("minSignificantDigits")) {
                        setMinimumSignificantDigits(fieldGetter.get("minSignificantDigits", 1));
                    } else if (name.equals("multiplier")) {
                        setMultiplier(fieldGetter.get("multiplier", 1));
                    } else if (name.equals("pad")) {
                        setPadCharacter(fieldGetter.get("pad", ' '));
                    } else if (name.equals("padPosition")) {
                        setPadPosition(fieldGetter.get("padPosition", 0));
                    } else if (name.equals("parseBigDecimal")) {
                        setParseBigDecimal(fieldGetter.get("parseBigDecimal", false));
                    } else if (name.equals("parseRequireDecimalPoint")) {
                        setDecimalPatternMatchRequired(fieldGetter.get("parseRequireDecimalPoint", false));
                    } else if (name.equals("roundingMode")) {
                        setRoundingMode(fieldGetter.get("roundingMode", 0));
                    } else if (name.equals("useExponentialNotation")) {
                        setScientificNotation(fieldGetter.get("useExponentialNotation", false));
                    } else if (name.equals("useSignificantDigits")) {
                        setSignificantDigitsUsed(fieldGetter.get("useSignificantDigits", false));
                    } else if (name.equals("currencyPluralInfo")) {
                        setCurrencyPluralInfo((CurrencyPluralInfo) fieldGetter.get("currencyPluralInfo", null));
                    } else if (name.equals("mathContext")) {
                        setMathContextICU((MathContext) fieldGetter.get("mathContext", null));
                    } else if (name.equals("negPrefixPattern")) {
                        npp = (String) fieldGetter.get("negPrefixPattern", null);
                    } else if (name.equals("negSuffixPattern")) {
                        nsp = (String) fieldGetter.get("negSuffixPattern", null);
                    } else if (name.equals("negativePrefix")) {
                        np = (String) fieldGetter.get("negativePrefix", null);
                    } else if (name.equals("negativeSuffix")) {
                        ns = (String) fieldGetter.get("negativeSuffix", null);
                    } else if (name.equals("posPrefixPattern")) {
                        ppp = (String) fieldGetter.get("posPrefixPattern", null);
                    } else if (name.equals("posSuffixPattern")) {
                        psp = (String) fieldGetter.get("posSuffixPattern", null);
                    } else if (name.equals("positivePrefix")) {
                        pp = (String) fieldGetter.get("positivePrefix", null);
                    } else if (name.equals("positiveSuffix")) {
                        ps = (String) fieldGetter.get("positiveSuffix", null);
                    } else if (name.equals("roundingIncrement")) {
                        setRoundingIncrement((BigDecimal) fieldGetter.get("roundingIncrement", null));
                    } else if (name.equals("symbols")) {
                        setDecimalFormatSymbols((DecimalFormatSymbols) fieldGetter.get("symbols", null));
                    }
                }
                i++;
                serializedFields = serializedFields2;
            }
            if (npp == null) {
                this.properties.setNegativePrefix(np);
            } else {
                this.properties.setNegativePrefixPattern(npp);
            }
            if (nsp == null) {
                this.properties.setNegativeSuffix(ns);
            } else {
                this.properties.setNegativeSuffixPattern(nsp);
            }
            if (ppp == null) {
                this.properties.setPositivePrefix(pp);
            } else {
                this.properties.setPositivePrefixPattern(ppp);
            }
            if (psp == null) {
                this.properties.setPositiveSuffix(ps);
            } else {
                this.properties.setPositiveSuffixPattern(psp);
            }
            try {
                Field getter = NumberFormat.class.getDeclaredField("groupingUsed");
                getter.setAccessible(true);
                setGroupingUsed(((Boolean) getter.get(this)).booleanValue());
                Field getter2 = NumberFormat.class.getDeclaredField("parseIntegerOnly");
                getter2.setAccessible(true);
                setParseIntegerOnly(((Boolean) getter2.get(this)).booleanValue());
                Field getter3 = NumberFormat.class.getDeclaredField("maximumIntegerDigits");
                getter3.setAccessible(true);
                setMaximumIntegerDigits(((Integer) getter3.get(this)).intValue());
                Field getter4 = NumberFormat.class.getDeclaredField("minimumIntegerDigits");
                getter4.setAccessible(true);
                setMinimumIntegerDigits(((Integer) getter4.get(this)).intValue());
                Field getter5 = NumberFormat.class.getDeclaredField("maximumFractionDigits");
                getter5.setAccessible(true);
                setMaximumFractionDigits(((Integer) getter5.get(this)).intValue());
                Field getter6 = NumberFormat.class.getDeclaredField("minimumFractionDigits");
                getter6.setAccessible(true);
                setMinimumFractionDigits(((Integer) getter6.get(this)).intValue());
                Field getter7 = NumberFormat.class.getDeclaredField("currency");
                getter7.setAccessible(true);
                setCurrency((Currency) getter7.get(this));
                Field getter8 = NumberFormat.class.getDeclaredField("parseStrict");
                getter8.setAccessible(true);
                setParseStrict(((Boolean) getter8.get(this)).booleanValue());
                if (this.symbols == null) {
                    this.symbols = getDefaultSymbols();
                }
                this.exportedProperties = new DecimalFormatProperties();
                refreshFormatter();
            } catch (IllegalArgumentException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e2) {
                throw new IOException(e2);
            } catch (NoSuchFieldException e3) {
                throw new IOException(e3);
            } catch (SecurityException e4) {
                throw new IOException(e4);
            }
        } else if (serializedFields.length <= 1) {
            ois.readInt();
            Object serializedProperties = ois.readObject();
            if (serializedProperties instanceof DecimalFormatProperties) {
                this.properties = (DecimalFormatProperties) serializedProperties;
            } else {
                this.properties = ((Properties) serializedProperties).getInstance();
            }
            this.symbols = (DecimalFormatSymbols) ois.readObject();
            this.exportedProperties = new DecimalFormatProperties();
            refreshFormatter();
            ObjectStreamField[] objectStreamFieldArr = serializedFields;
        } else {
            throw new IOException("Too many fields when reading serial version 5");
        }
    }

    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        FormattedNumber output = this.formatter.format(number);
        output.populateFieldPosition(fieldPosition, result.length());
        output.appendTo(result);
        return result;
    }

    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        FormattedNumber output = this.formatter.format(number);
        output.populateFieldPosition(fieldPosition, result.length());
        output.appendTo(result);
        return result;
    }

    public StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition) {
        FormattedNumber output = this.formatter.format((Number) number);
        output.populateFieldPosition(fieldPosition, result.length());
        output.appendTo(result);
        return result;
    }

    public StringBuffer format(BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        FormattedNumber output = this.formatter.format((Number) number);
        output.populateFieldPosition(fieldPosition, result.length());
        output.appendTo(result);
        return result;
    }

    public StringBuffer format(android.icu.math.BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        FormattedNumber output = this.formatter.format((Number) number);
        output.populateFieldPosition(fieldPosition, result.length());
        output.appendTo(result);
        return result;
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (obj instanceof Number) {
            return this.formatter.format((Number) obj).getFieldIterator();
        }
        throw new IllegalArgumentException();
    }

    public StringBuffer format(CurrencyAmount currAmt, StringBuffer toAppendTo, FieldPosition pos) {
        FormattedNumber output = this.formatter.format((Measure) currAmt);
        output.populateFieldPosition(pos, toAppendTo.length());
        output.appendTo(toAppendTo);
        return toAppendTo;
    }

    public Number parse(String text, ParsePosition parsePosition) {
        DecimalFormatProperties pprops = threadLocalProperties.get();
        synchronized (this) {
            pprops.copyFrom(this.properties);
        }
        Number result = Parse.parse(text, parsePosition, pprops, this.symbols);
        if (result instanceof BigDecimal) {
            return safeConvertBigDecimal((BigDecimal) result);
        }
        return result;
    }

    public CurrencyAmount parseCurrency(CharSequence text, ParsePosition parsePosition) {
        try {
            DecimalFormatProperties pprops = threadLocalProperties.get();
            synchronized (this) {
                pprops.copyFrom(this.properties);
            }
            CurrencyAmount result = Parse.parseCurrency(text, parsePosition, pprops, this.symbols);
            if (result == null) {
                return null;
            }
            Number number = result.getNumber();
            if (number instanceof BigDecimal) {
                result = new CurrencyAmount(safeConvertBigDecimal((BigDecimal) number), result.getCurrency());
            }
            return result;
        } catch (ParseException e) {
            return null;
        }
    }

    public synchronized DecimalFormatSymbols getDecimalFormatSymbols() {
        return (DecimalFormatSymbols) this.symbols.clone();
    }

    public synchronized void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        this.symbols = (DecimalFormatSymbols) newSymbols.clone();
        refreshFormatter();
    }

    public synchronized String getPositivePrefix() {
        return this.formatter.format(1).getPrefix();
    }

    public synchronized void setPositivePrefix(String prefix) {
        if (prefix != null) {
            this.properties.setPositivePrefix(prefix);
            refreshFormatter();
        } else {
            throw new NullPointerException();
        }
    }

    public synchronized String getNegativePrefix() {
        return this.formatter.format(-1).getPrefix();
    }

    public synchronized void setNegativePrefix(String prefix) {
        if (prefix != null) {
            this.properties.setNegativePrefix(prefix);
            refreshFormatter();
        } else {
            throw new NullPointerException();
        }
    }

    public synchronized String getPositiveSuffix() {
        return this.formatter.format(1).getSuffix();
    }

    public synchronized void setPositiveSuffix(String suffix) {
        if (suffix != null) {
            this.properties.setPositiveSuffix(suffix);
            refreshFormatter();
        } else {
            throw new NullPointerException();
        }
    }

    public synchronized String getNegativeSuffix() {
        return this.formatter.format(-1).getSuffix();
    }

    public synchronized void setNegativeSuffix(String suffix) {
        if (suffix != null) {
            this.properties.setNegativeSuffix(suffix);
            refreshFormatter();
        } else {
            throw new NullPointerException();
        }
    }

    @Deprecated
    public synchronized boolean getSignAlwaysShown() {
        return this.properties.getSignAlwaysShown();
    }

    @Deprecated
    public synchronized void setSignAlwaysShown(boolean value) {
        this.properties.setSignAlwaysShown(value);
        refreshFormatter();
    }

    public synchronized int getMultiplier() {
        if (this.properties.getMultiplier() != null) {
            return this.properties.getMultiplier().intValue();
        }
        return (int) Math.pow(10.0d, (double) this.properties.getMagnitudeMultiplier());
    }

    public synchronized void setMultiplier(int multiplier) {
        if (multiplier != 0) {
            int delta = 0;
            int value = multiplier;
            while (true) {
                if (multiplier == 1) {
                    break;
                }
                delta++;
                int temp = value / 10;
                if (temp * 10 != value) {
                    delta = -1;
                    break;
                }
                value = temp;
            }
            if (delta != -1) {
                this.properties.setMagnitudeMultiplier(delta);
            } else {
                this.properties.setMultiplier(BigDecimal.valueOf((long) multiplier));
            }
            refreshFormatter();
        } else {
            throw new IllegalArgumentException("Multiplier must be nonzero.");
        }
    }

    public synchronized BigDecimal getRoundingIncrement() {
        return this.exportedProperties.getRoundingIncrement();
    }

    public synchronized void setRoundingIncrement(BigDecimal increment) {
        if (increment != null) {
            if (increment.compareTo(BigDecimal.ZERO) == 0) {
                this.properties.setMaximumFractionDigits(Integer.MAX_VALUE);
                return;
            }
        }
        this.properties.setRoundingIncrement(increment);
        refreshFormatter();
    }

    public synchronized void setRoundingIncrement(android.icu.math.BigDecimal increment) {
        setRoundingIncrement(increment == null ? null : increment.toBigDecimal());
    }

    public synchronized void setRoundingIncrement(double increment) {
        if (increment == 0.0d) {
            try {
                setRoundingIncrement((BigDecimal) null);
            } catch (Throwable th) {
                throw th;
            }
        } else {
            setRoundingIncrement(BigDecimal.valueOf(increment));
        }
    }

    public synchronized int getRoundingMode() {
        RoundingMode mode;
        mode = this.exportedProperties.getRoundingMode();
        return mode == null ? 0 : mode.ordinal();
    }

    public synchronized void setRoundingMode(int roundingMode) {
        this.properties.setRoundingMode(RoundingMode.valueOf(roundingMode));
        refreshFormatter();
    }

    public synchronized java.math.MathContext getMathContext() {
        return this.exportedProperties.getMathContext();
    }

    public synchronized void setMathContext(java.math.MathContext mathContext) {
        this.properties.setMathContext(mathContext);
        refreshFormatter();
    }

    public synchronized MathContext getMathContextICU() {
        java.math.MathContext mathContext;
        mathContext = getMathContext();
        return new MathContext(mathContext.getPrecision(), this.icuMathContextForm, false, mathContext.getRoundingMode().ordinal());
    }

    public synchronized void setMathContextICU(MathContext mathContextICU) {
        java.math.MathContext mathContext;
        this.icuMathContextForm = mathContextICU.getForm();
        if (mathContextICU.getLostDigits()) {
            mathContext = new java.math.MathContext(mathContextICU.getDigits(), RoundingMode.UNNECESSARY);
        } else {
            mathContext = new java.math.MathContext(mathContextICU.getDigits(), RoundingMode.valueOf(mathContextICU.getRoundingMode()));
        }
        setMathContext(mathContext);
    }

    public synchronized int getMinimumIntegerDigits() {
        return this.exportedProperties.getMinimumIntegerDigits();
    }

    public synchronized void setMinimumIntegerDigits(int value) {
        int max = this.properties.getMaximumIntegerDigits();
        if (max >= 0 && max < value) {
            this.properties.setMaximumIntegerDigits(value);
        }
        this.properties.setMinimumIntegerDigits(value);
        refreshFormatter();
    }

    public synchronized int getMaximumIntegerDigits() {
        return this.exportedProperties.getMaximumIntegerDigits();
    }

    public synchronized void setMaximumIntegerDigits(int value) {
        int min = this.properties.getMinimumIntegerDigits();
        if (min >= 0 && min > value) {
            this.properties.setMinimumIntegerDigits(value);
        }
        this.properties.setMaximumIntegerDigits(value);
        refreshFormatter();
    }

    public synchronized int getMinimumFractionDigits() {
        return this.exportedProperties.getMinimumFractionDigits();
    }

    public synchronized void setMinimumFractionDigits(int value) {
        int max = this.properties.getMaximumFractionDigits();
        if (max >= 0 && max < value) {
            this.properties.setMaximumFractionDigits(value);
        }
        this.properties.setMinimumFractionDigits(value);
        refreshFormatter();
    }

    public synchronized int getMaximumFractionDigits() {
        return this.exportedProperties.getMaximumFractionDigits();
    }

    public synchronized void setMaximumFractionDigits(int value) {
        int min = this.properties.getMinimumFractionDigits();
        if (min >= 0 && min > value) {
            this.properties.setMinimumFractionDigits(value);
        }
        this.properties.setMaximumFractionDigits(value);
        refreshFormatter();
    }

    public synchronized boolean areSignificantDigitsUsed() {
        return (this.properties.getMinimumSignificantDigits() == -1 && this.properties.getMaximumSignificantDigits() == -1) ? false : true;
    }

    public synchronized void setSignificantDigitsUsed(boolean useSignificantDigits) {
        if (useSignificantDigits) {
            try {
                this.properties.setMinimumSignificantDigits(1);
                this.properties.setMaximumSignificantDigits(6);
            } catch (Throwable th) {
                throw th;
            }
        } else {
            this.properties.setMinimumSignificantDigits(-1);
            this.properties.setMaximumSignificantDigits(-1);
        }
        refreshFormatter();
    }

    public synchronized int getMinimumSignificantDigits() {
        return this.exportedProperties.getMinimumSignificantDigits();
    }

    public synchronized void setMinimumSignificantDigits(int value) {
        int max = this.properties.getMaximumSignificantDigits();
        if (max >= 0 && max < value) {
            this.properties.setMaximumSignificantDigits(value);
        }
        this.properties.setMinimumSignificantDigits(value);
        refreshFormatter();
    }

    public synchronized int getMaximumSignificantDigits() {
        return this.exportedProperties.getMaximumSignificantDigits();
    }

    public synchronized void setMaximumSignificantDigits(int value) {
        int min = this.properties.getMinimumSignificantDigits();
        if (min >= 0 && min > value) {
            this.properties.setMinimumSignificantDigits(value);
        }
        this.properties.setMaximumSignificantDigits(value);
        refreshFormatter();
    }

    public synchronized int getFormatWidth() {
        return this.properties.getFormatWidth();
    }

    public synchronized void setFormatWidth(int width) {
        this.properties.setFormatWidth(width);
        refreshFormatter();
    }

    public synchronized char getPadCharacter() {
        CharSequence paddingString = this.properties.getPadString();
        if (paddingString == null) {
            return '.';
        }
        return paddingString.charAt(0);
    }

    public synchronized void setPadCharacter(char padChar) {
        this.properties.setPadString(Character.toString(padChar));
        refreshFormatter();
    }

    public synchronized int getPadPosition() {
        Padder.PadPosition loc;
        loc = this.properties.getPadPosition();
        return loc == null ? 0 : loc.toOld();
    }

    public synchronized void setPadPosition(int padPos) {
        this.properties.setPadPosition(Padder.PadPosition.fromOld(padPos));
        refreshFormatter();
    }

    public synchronized boolean isScientificNotation() {
        return this.properties.getMinimumExponentDigits() != -1;
    }

    public synchronized void setScientificNotation(boolean useScientific) {
        if (useScientific) {
            try {
                this.properties.setMinimumExponentDigits(1);
            } catch (Throwable th) {
                throw th;
            }
        } else {
            this.properties.setMinimumExponentDigits(-1);
        }
        refreshFormatter();
    }

    public synchronized byte getMinimumExponentDigits() {
        return (byte) this.properties.getMinimumExponentDigits();
    }

    public synchronized void setMinimumExponentDigits(byte minExpDig) {
        this.properties.setMinimumExponentDigits(minExpDig);
        refreshFormatter();
    }

    public synchronized boolean isExponentSignAlwaysShown() {
        return this.properties.getExponentSignAlwaysShown();
    }

    public synchronized void setExponentSignAlwaysShown(boolean expSignAlways) {
        this.properties.setExponentSignAlwaysShown(expSignAlways);
        refreshFormatter();
    }

    public synchronized boolean isGroupingUsed() {
        return this.properties.getGroupingSize() > 0 || this.properties.getSecondaryGroupingSize() > 0;
    }

    public synchronized void setGroupingUsed(boolean enabled) {
        if (enabled) {
            try {
                this.properties.setGroupingSize(3);
            } catch (Throwable th) {
                throw th;
            }
        } else {
            this.properties.setGroupingSize(0);
            this.properties.setSecondaryGroupingSize(0);
        }
        refreshFormatter();
    }

    public synchronized int getGroupingSize() {
        return this.properties.getGroupingSize();
    }

    public synchronized void setGroupingSize(int width) {
        this.properties.setGroupingSize(width);
        refreshFormatter();
    }

    public synchronized int getSecondaryGroupingSize() {
        int grouping1 = this.properties.getGroupingSize();
        int grouping2 = this.properties.getSecondaryGroupingSize();
        if (grouping1 != grouping2) {
            if (grouping2 >= 0) {
                return this.properties.getSecondaryGroupingSize();
            }
        }
        return 0;
    }

    public synchronized void setSecondaryGroupingSize(int width) {
        this.properties.setSecondaryGroupingSize(width);
        refreshFormatter();
    }

    @Deprecated
    public synchronized int getMinimumGroupingDigits() {
        if (this.properties.getMinimumGroupingDigits() == 2) {
            return 2;
        }
        return 1;
    }

    @Deprecated
    public synchronized void setMinimumGroupingDigits(int number) {
        this.properties.setMinimumGroupingDigits(number);
        refreshFormatter();
    }

    public synchronized boolean isDecimalSeparatorAlwaysShown() {
        return this.properties.getDecimalSeparatorAlwaysShown();
    }

    public synchronized void setDecimalSeparatorAlwaysShown(boolean value) {
        this.properties.setDecimalSeparatorAlwaysShown(value);
        refreshFormatter();
    }

    public synchronized Currency getCurrency() {
        return this.properties.getCurrency();
    }

    public synchronized void setCurrency(Currency currency) {
        this.properties.setCurrency(currency);
        if (currency != null) {
            this.symbols.setCurrency(currency);
            this.symbols.setCurrencySymbol(currency.getName(this.symbols.getULocale(), 0, (boolean[]) null));
        }
        refreshFormatter();
    }

    public synchronized Currency.CurrencyUsage getCurrencyUsage() {
        Currency.CurrencyUsage usage;
        usage = this.properties.getCurrencyUsage();
        if (usage == null) {
            usage = Currency.CurrencyUsage.STANDARD;
        }
        return usage;
    }

    public synchronized void setCurrencyUsage(Currency.CurrencyUsage usage) {
        this.properties.setCurrencyUsage(usage);
        refreshFormatter();
    }

    public synchronized CurrencyPluralInfo getCurrencyPluralInfo() {
        return this.properties.getCurrencyPluralInfo();
    }

    public synchronized void setCurrencyPluralInfo(CurrencyPluralInfo newInfo) {
        this.properties.setCurrencyPluralInfo(newInfo);
        refreshFormatter();
    }

    public synchronized boolean isParseBigDecimal() {
        return this.properties.getParseToBigDecimal();
    }

    public synchronized void setParseBigDecimal(boolean value) {
        this.properties.setParseToBigDecimal(value);
    }

    @Deprecated
    public int getParseMaxDigits() {
        return 1000;
    }

    @Deprecated
    public void setParseMaxDigits(int maxDigits) {
    }

    public synchronized boolean isParseStrict() {
        return this.properties.getParseMode() == Parse.ParseMode.STRICT;
    }

    public synchronized void setParseStrict(boolean parseStrict) {
        Parse.ParseMode mode;
        if (parseStrict) {
            try {
                mode = Parse.ParseMode.STRICT;
            } catch (Throwable th) {
                throw th;
            }
        } else {
            mode = Parse.ParseMode.LENIENT;
        }
        this.properties.setParseMode(mode);
    }

    public synchronized boolean isParseIntegerOnly() {
        return this.properties.getParseIntegerOnly();
    }

    public synchronized void setParseIntegerOnly(boolean parseIntegerOnly) {
        this.properties.setParseIntegerOnly(parseIntegerOnly);
    }

    public synchronized boolean isDecimalPatternMatchRequired() {
        return this.properties.getDecimalPatternMatchRequired();
    }

    public synchronized void setDecimalPatternMatchRequired(boolean value) {
        this.properties.setDecimalPatternMatchRequired(value);
        refreshFormatter();
    }

    @Deprecated
    public synchronized boolean getParseNoExponent() {
        return this.properties.getParseNoExponent();
    }

    @Deprecated
    public synchronized void setParseNoExponent(boolean value) {
        this.properties.setParseNoExponent(value);
        refreshFormatter();
    }

    @Deprecated
    public synchronized boolean getParseCaseSensitive() {
        return this.properties.getParseCaseSensitive();
    }

    @Deprecated
    public synchronized void setParseCaseSensitive(boolean value) {
        this.properties.setParseCaseSensitive(value);
        refreshFormatter();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002b, code lost:
        return r0;
     */
    public synchronized boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DecimalFormat)) {
            return false;
        }
        DecimalFormat other = (DecimalFormat) obj;
        if (this.properties.equals(other.properties) && this.symbols.equals(other.symbols)) {
            z = true;
        }
    }

    public synchronized int hashCode() {
        return this.properties.hashCode() ^ this.symbols.hashCode();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getClass().getName());
        result.append("@");
        result.append(Integer.toHexString(hashCode()));
        result.append(" { symbols@");
        result.append(Integer.toHexString(this.symbols.hashCode()));
        synchronized (this) {
            this.properties.toStringBare(result);
        }
        result.append(" }");
        return result.toString();
    }

    public synchronized String toPattern() {
        DecimalFormatProperties tprops;
        tprops = threadLocalProperties.get().copyFrom(this.properties);
        if (useCurrency(this.properties)) {
            tprops.setMinimumFractionDigits(this.exportedProperties.getMinimumFractionDigits());
            tprops.setMaximumFractionDigits(this.exportedProperties.getMaximumFractionDigits());
            tprops.setRoundingIncrement(this.exportedProperties.getRoundingIncrement());
        }
        return PatternStringUtils.propertiesToPatternString(tprops);
    }

    public synchronized String toLocalizedPattern() {
        return PatternStringUtils.convertLocalized(toPattern(), this.symbols, true);
    }

    public LocalizedNumberFormatter toNumberFormatter() {
        return this.formatter;
    }

    @Deprecated
    public PluralRules.IFixedDecimal getFixedDecimal(double number) {
        return this.formatter.format(number).getFixedDecimal();
    }

    /* access modifiers changed from: package-private */
    public void refreshFormatter() {
        if (this.exportedProperties != null) {
            ULocale locale = getLocale(ULocale.ACTUAL_LOCALE);
            if (locale == null) {
                locale = this.symbols.getLocale(ULocale.ACTUAL_LOCALE);
            }
            if (locale == null) {
                locale = this.symbols.getULocale();
            }
            this.formatter = NumberFormatter.fromDecimalFormat(this.properties, this.symbols, this.exportedProperties).locale(locale);
        }
    }

    private Number safeConvertBigDecimal(BigDecimal number) {
        try {
            return new android.icu.math.BigDecimal(number);
        } catch (NumberFormatException e) {
            if (number.signum() > 0 && number.scale() < 0) {
                return Double.valueOf(Double.POSITIVE_INFINITY);
            }
            if (number.scale() < 0) {
                return Double.valueOf(Double.NEGATIVE_INFINITY);
            }
            if (number.signum() < 0) {
                return Double.valueOf(-0.0d);
            }
            return Double.valueOf(0.0d);
        }
    }

    private static boolean useCurrency(DecimalFormatProperties properties2) {
        return properties2.getCurrency() != null || properties2.getCurrencyPluralInfo() != null || properties2.getCurrencyUsage() != null || AffixUtils.hasCurrencySymbols(properties2.getPositivePrefixPattern()) || AffixUtils.hasCurrencySymbols(properties2.getPositiveSuffixPattern()) || AffixUtils.hasCurrencySymbols(properties2.getNegativePrefixPattern()) || AffixUtils.hasCurrencySymbols(properties2.getNegativeSuffixPattern());
    }

    /* access modifiers changed from: package-private */
    public void setPropertiesFromPattern(String pattern, int ignoreRounding) {
        if (pattern != null) {
            PatternStringParser.parseToExistingProperties(pattern, this.properties, ignoreRounding);
            return;
        }
        throw new NullPointerException();
    }

    @Deprecated
    public synchronized void setProperties(PropertySetter func) {
        func.set(this.properties);
        refreshFormatter();
    }
}
