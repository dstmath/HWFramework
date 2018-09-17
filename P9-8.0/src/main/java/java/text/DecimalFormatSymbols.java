package java.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.concurrent.ConcurrentHashMap;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

public class DecimalFormatSymbols implements Cloneable, Serializable {
    private static final ConcurrentHashMap<Locale, Object[]> cachedLocaleData = new ConcurrentHashMap(3);
    private static final int currentSerialVersion = 3;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("currencySymbol", String.class), new ObjectStreamField("decimalSeparator", Character.TYPE), new ObjectStreamField("digit", Character.TYPE), new ObjectStreamField("exponential", Character.TYPE), new ObjectStreamField("exponentialSeparator", String.class), new ObjectStreamField("groupingSeparator", Character.TYPE), new ObjectStreamField("infinity", String.class), new ObjectStreamField("intlCurrencySymbol", String.class), new ObjectStreamField("minusSign", Character.TYPE), new ObjectStreamField("monetarySeparator", Character.TYPE), new ObjectStreamField("NaN", String.class), new ObjectStreamField("patternSeparator", Character.TYPE), new ObjectStreamField("percent", Character.TYPE), new ObjectStreamField("perMill", Character.TYPE), new ObjectStreamField("serialVersionOnStream", Integer.TYPE), new ObjectStreamField("zeroDigit", Character.TYPE), new ObjectStreamField("locale", Locale.class), new ObjectStreamField("minusSignStr", String.class), new ObjectStreamField("percentStr", String.class)};
    static final long serialVersionUID = 5772796243397350300L;
    private String NaN;
    private transient android.icu.text.DecimalFormatSymbols cachedIcuDFS = null;
    private transient Currency currency;
    private String currencySymbol;
    private char decimalSeparator;
    private char digit;
    private char exponential;
    private String exponentialSeparator;
    private char groupingSeparator;
    private String infinity;
    private String intlCurrencySymbol;
    private Locale locale;
    private char minusSign;
    private char monetarySeparator;
    private char patternSeparator;
    private char perMill;
    private char percent;
    private int serialVersionOnStream = 3;
    private char zeroDigit;

    public DecimalFormatSymbols() {
        initialize(Locale.getDefault(Category.FORMAT));
    }

    public DecimalFormatSymbols(Locale locale) {
        initialize(locale);
    }

    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableLocales();
    }

    public static final DecimalFormatSymbols getInstance() {
        return getInstance(Locale.getDefault(Category.FORMAT));
    }

    public static final DecimalFormatSymbols getInstance(Locale locale) {
        return new DecimalFormatSymbols(locale);
    }

    public char getZeroDigit() {
        return this.zeroDigit;
    }

    public void setZeroDigit(char zeroDigit) {
        this.zeroDigit = zeroDigit;
        this.cachedIcuDFS = null;
    }

    public char getGroupingSeparator() {
        return this.groupingSeparator;
    }

    public void setGroupingSeparator(char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
        this.cachedIcuDFS = null;
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
        this.cachedIcuDFS = null;
    }

    public char getPerMill() {
        return this.perMill;
    }

    public void setPerMill(char perMill) {
        this.perMill = perMill;
        this.cachedIcuDFS = null;
    }

    public char getPercent() {
        return this.percent;
    }

    public String getPercentString() {
        return String.valueOf(this.percent);
    }

    public void setPercent(char percent) {
        this.percent = percent;
        this.cachedIcuDFS = null;
    }

    public char getDigit() {
        return this.digit;
    }

    public void setDigit(char digit) {
        this.digit = digit;
        this.cachedIcuDFS = null;
    }

    public char getPatternSeparator() {
        return this.patternSeparator;
    }

    public void setPatternSeparator(char patternSeparator) {
        this.patternSeparator = patternSeparator;
        this.cachedIcuDFS = null;
    }

    public String getInfinity() {
        return this.infinity;
    }

    public void setInfinity(String infinity) {
        this.infinity = infinity;
        this.cachedIcuDFS = null;
    }

    public String getNaN() {
        return this.NaN;
    }

    public void setNaN(String NaN) {
        this.NaN = NaN;
        this.cachedIcuDFS = null;
    }

    public char getMinusSign() {
        return this.minusSign;
    }

    public String getMinusSignString() {
        return String.valueOf(this.minusSign);
    }

    public void setMinusSign(char minusSign) {
        this.minusSign = minusSign;
        this.cachedIcuDFS = null;
    }

    public String getCurrencySymbol() {
        return this.currencySymbol;
    }

    public void setCurrencySymbol(String currency) {
        this.currencySymbol = currency;
        this.cachedIcuDFS = null;
    }

    public String getInternationalCurrencySymbol() {
        return this.intlCurrencySymbol;
    }

    public void setInternationalCurrencySymbol(String currencyCode) {
        this.intlCurrencySymbol = currencyCode;
        this.currency = null;
        if (currencyCode != null) {
            try {
                this.currency = Currency.getInstance(currencyCode);
                this.currencySymbol = this.currency.getSymbol(this.locale);
            } catch (IllegalArgumentException e) {
            }
        }
        this.cachedIcuDFS = null;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException();
        }
        this.currency = currency;
        this.intlCurrencySymbol = currency.getCurrencyCode();
        this.currencySymbol = currency.getSymbol(this.locale);
        this.cachedIcuDFS = null;
    }

    public char getMonetaryDecimalSeparator() {
        return this.monetarySeparator;
    }

    public void setMonetaryDecimalSeparator(char sep) {
        this.monetarySeparator = sep;
        this.cachedIcuDFS = null;
    }

    char getExponentialSymbol() {
        return this.exponential;
    }

    public String getExponentSeparator() {
        return this.exponentialSeparator;
    }

    void setExponentialSymbol(char exp) {
        this.exponential = exp;
        this.cachedIcuDFS = null;
    }

    public void setExponentSeparator(String exp) {
        if (exp == null) {
            throw new NullPointerException();
        }
        this.exponentialSeparator = exp;
    }

    public Object clone() {
        try {
            return (DecimalFormatSymbols) super.clone();
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DecimalFormatSymbols other = (DecimalFormatSymbols) obj;
        if (this.zeroDigit == other.zeroDigit && this.groupingSeparator == other.groupingSeparator && this.decimalSeparator == other.decimalSeparator && this.percent == other.percent && this.perMill == other.perMill && this.digit == other.digit && this.minusSign == other.minusSign && this.patternSeparator == other.patternSeparator && this.infinity.equals(other.infinity) && this.NaN.equals(other.NaN) && this.currencySymbol.equals(other.currencySymbol) && this.intlCurrencySymbol.equals(other.intlCurrencySymbol) && this.currency == other.currency && this.monetarySeparator == other.monetarySeparator && this.exponentialSeparator.equals(other.exponentialSeparator)) {
            z = this.locale.equals(other.locale);
        }
        return z;
    }

    public int hashCode() {
        return (((((((((((((((((((((((((((((this.zeroDigit * 37) + this.groupingSeparator) * 37) + this.decimalSeparator) * 37) + this.percent) * 37) + this.perMill) * 37) + this.digit) * 37) + this.minusSign) * 37) + this.patternSeparator) * 37) + this.infinity.hashCode()) * 37) + this.NaN.hashCode()) * 37) + this.currencySymbol.hashCode()) * 37) + this.intlCurrencySymbol.hashCode()) * 37) + this.currency.hashCode()) * 37) + this.monetarySeparator) * 37) + this.exponentialSeparator.hashCode()) * 37) + this.locale.hashCode();
    }

    private void initialize(Locale locale) {
        this.locale = locale;
        boolean needCacheUpdate = false;
        Object[] data = (Object[]) cachedLocaleData.get(locale);
        if (data == null) {
            locale = LocaleData.mapInvalidAndNullLocales(locale);
            LocaleData localeData = LocaleData.get(locale);
            data = new Object[3];
            data[0] = new String[]{String.valueOf(localeData.decimalSeparator), String.valueOf(localeData.groupingSeparator), String.valueOf(localeData.patternSeparator), String.valueOf(localeData.percent), String.valueOf(localeData.zeroDigit), "#", localeData.minusSign, localeData.exponentSeparator, String.valueOf(localeData.perMill), localeData.infinity, localeData.NaN};
            needCacheUpdate = true;
        }
        String[] numberElements = data[0];
        this.decimalSeparator = numberElements[0].charAt(0);
        this.groupingSeparator = numberElements[1].charAt(0);
        this.patternSeparator = numberElements[2].charAt(0);
        this.percent = maybeStripMarkers(numberElements[3], '%');
        this.zeroDigit = numberElements[4].charAt(0);
        this.digit = numberElements[5].charAt(0);
        this.minusSign = maybeStripMarkers(numberElements[6], '-');
        this.exponential = numberElements[7].charAt(0);
        this.exponentialSeparator = numberElements[7];
        this.perMill = numberElements[8].charAt(0);
        this.infinity = numberElements[9];
        this.NaN = numberElements[10];
        if (!"".equals(locale.getCountry())) {
            try {
                this.currency = Currency.getInstance(locale);
            } catch (IllegalArgumentException e) {
            }
        }
        if (this.currency != null) {
            this.intlCurrencySymbol = this.currency.getCurrencyCode();
            if (data[1] == null || data[1] != this.intlCurrencySymbol) {
                this.currencySymbol = this.currency.getSymbol(locale);
                data[1] = this.intlCurrencySymbol;
                data[2] = this.currencySymbol;
                needCacheUpdate = true;
            } else {
                this.currencySymbol = (String) data[2];
            }
        } else {
            this.intlCurrencySymbol = "XXX";
            try {
                this.currency = Currency.getInstance(this.intlCurrencySymbol);
            } catch (IllegalArgumentException e2) {
            }
            this.currencySymbol = "¤";
        }
        this.monetarySeparator = this.decimalSeparator;
        if (needCacheUpdate) {
            cachedLocaleData.putIfAbsent(locale, data);
        }
    }

    public static char maybeStripMarkers(String symbol, char fallback) {
        int length = symbol.length();
        if (length >= 1) {
            boolean sawNonMarker = false;
            char nonMarker = 0;
            for (int i = 0; i < length; i++) {
                char c = symbol.charAt(i);
                if (!(c == 8206 || c == 8207 || c == 1564)) {
                    if (sawNonMarker) {
                        return fallback;
                    }
                    sawNonMarker = true;
                    nonMarker = c;
                }
            }
            if (sawNonMarker) {
                return nonMarker;
            }
        }
        return fallback;
    }

    protected android.icu.text.DecimalFormatSymbols getIcuDecimalFormatSymbols() {
        if (this.cachedIcuDFS != null) {
            return this.cachedIcuDFS;
        }
        this.cachedIcuDFS = new android.icu.text.DecimalFormatSymbols(this.locale);
        this.cachedIcuDFS.setZeroDigit(this.zeroDigit);
        this.cachedIcuDFS.setDigit(this.digit);
        this.cachedIcuDFS.setDecimalSeparator(this.decimalSeparator);
        this.cachedIcuDFS.setGroupingSeparator(this.groupingSeparator);
        this.cachedIcuDFS.setPatternSeparator(this.patternSeparator);
        this.cachedIcuDFS.setPercent(this.percent);
        this.cachedIcuDFS.setMonetaryDecimalSeparator(this.monetarySeparator);
        this.cachedIcuDFS.setMinusSign(this.minusSign);
        this.cachedIcuDFS.setInfinity(this.infinity);
        this.cachedIcuDFS.setNaN(this.NaN);
        this.cachedIcuDFS.setExponentSeparator(this.exponentialSeparator);
        try {
            this.cachedIcuDFS.setCurrency(android.icu.util.Currency.getInstance(this.currency.getCurrencyCode()));
        } catch (NullPointerException e) {
            this.currency = Currency.getInstance("XXX");
        }
        this.cachedIcuDFS.setCurrencySymbol(this.currencySymbol);
        this.cachedIcuDFS.setInternationalCurrencySymbol(this.intlCurrencySymbol);
        return this.cachedIcuDFS;
    }

    protected static DecimalFormatSymbols fromIcuInstance(android.icu.text.DecimalFormatSymbols dfs) {
        DecimalFormatSymbols result = new DecimalFormatSymbols(dfs.getLocale());
        result.setZeroDigit(dfs.getZeroDigit());
        result.setDigit(dfs.getDigit());
        result.setDecimalSeparator(dfs.getDecimalSeparator());
        result.setGroupingSeparator(dfs.getGroupingSeparator());
        result.setPatternSeparator(dfs.getPatternSeparator());
        result.setPercent(dfs.getPercent());
        result.setPerMill(dfs.getPerMill());
        result.setMonetaryDecimalSeparator(dfs.getMonetaryDecimalSeparator());
        result.setMinusSign(dfs.getMinusSign());
        result.setInfinity(dfs.getInfinity());
        result.setNaN(dfs.getNaN());
        result.setExponentSeparator(dfs.getExponentSeparator());
        try {
            if (dfs.getCurrency() != null) {
                result.setCurrency(Currency.getInstance(dfs.getCurrency().getCurrencyCode()));
            } else {
                result.setCurrency(Currency.getInstance("XXX"));
            }
        } catch (IllegalArgumentException e) {
            result.setCurrency(Currency.getInstance("XXX"));
        }
        result.setInternationalCurrencySymbol(dfs.getInternationalCurrencySymbol());
        result.setCurrencySymbol(dfs.getCurrencySymbol());
        return result;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        PutField fields = stream.putFields();
        fields.put("currencySymbol", this.currencySymbol);
        fields.put("decimalSeparator", getDecimalSeparator());
        fields.put("digit", getDigit());
        fields.put("exponential", this.exponentialSeparator.charAt(0));
        fields.put("exponentialSeparator", this.exponentialSeparator);
        fields.put("groupingSeparator", getGroupingSeparator());
        fields.put("infinity", this.infinity);
        fields.put("intlCurrencySymbol", this.intlCurrencySymbol);
        fields.put("monetarySeparator", getMonetaryDecimalSeparator());
        fields.put("NaN", this.NaN);
        fields.put("patternSeparator", getPatternSeparator());
        fields.put("perMill", getPerMill());
        fields.put("serialVersionOnStream", 3);
        fields.put("zeroDigit", getZeroDigit());
        fields.put("locale", this.locale);
        fields.put("minusSign", this.minusSign);
        fields.put("percent", this.percent);
        fields.put("minusSignStr", getMinusSignString());
        fields.put("percentStr", getPercentString());
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        GetField fields = stream.readFields();
        int serialVersionOnStream = fields.get("serialVersionOnStream", 0);
        this.currencySymbol = (String) fields.get("currencySymbol", (Object) "");
        setDecimalSeparator(fields.get("decimalSeparator", '.'));
        setDigit(fields.get("digit", '#'));
        setGroupingSeparator(fields.get("groupingSeparator", ','));
        this.infinity = (String) fields.get("infinity", (Object) "");
        this.intlCurrencySymbol = (String) fields.get("intlCurrencySymbol", (Object) "");
        this.NaN = (String) fields.get("NaN", (Object) "");
        setPatternSeparator(fields.get("patternSeparator", ';'));
        String minusSignStr = (String) fields.get("minusSignStr", null);
        if (minusSignStr != null) {
            this.minusSign = minusSignStr.charAt(0);
        } else {
            setMinusSign(fields.get("minusSign", '-'));
        }
        String percentStr = (String) fields.get("percentStr", null);
        if (percentStr != null) {
            this.percent = percentStr.charAt(0);
        } else {
            setPercent(fields.get("percent", '%'));
        }
        setPerMill(fields.get("perMill", 8240));
        setZeroDigit(fields.get("zeroDigit", '0'));
        this.locale = (Locale) fields.get("locale", null);
        if (serialVersionOnStream == 0) {
            setMonetaryDecimalSeparator(getDecimalSeparator());
        } else {
            setMonetaryDecimalSeparator(fields.get("monetarySeparator", '.'));
        }
        if (serialVersionOnStream == 0) {
            this.exponentialSeparator = "E";
        } else if (serialVersionOnStream < 3) {
            setExponentSeparator(String.valueOf(fields.get("exponential", 'E')));
        } else {
            setExponentSeparator((String) fields.get("exponentialSeparator", (Object) "E"));
        }
        try {
            this.currency = Currency.getInstance(this.intlCurrencySymbol);
        } catch (IllegalArgumentException e) {
            this.currency = null;
        }
    }
}
