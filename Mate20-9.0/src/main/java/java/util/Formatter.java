package java.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.Types;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Locale;
import libcore.icu.LocaleData;
import sun.misc.FormattedFloatingDecimal;

public final class Formatter implements Closeable, Flushable {
    private static final int MAX_FD_CHARS = 30;
    /* access modifiers changed from: private */
    public static double scaleUp;
    /* access modifiers changed from: private */
    public Appendable a;
    /* access modifiers changed from: private */
    public final Locale l;
    private IOException lastException;
    /* access modifiers changed from: private */
    public final char zero;

    public enum BigDecimalLayoutForm {
        SCIENTIFIC,
        DECIMAL_FLOAT
    }

    private static class Conversion {
        static final char BOOLEAN = 'b';
        static final char BOOLEAN_UPPER = 'B';
        static final char CHARACTER = 'c';
        static final char CHARACTER_UPPER = 'C';
        static final char DATE_TIME = 't';
        static final char DATE_TIME_UPPER = 'T';
        static final char DECIMAL_FLOAT = 'f';
        static final char DECIMAL_INTEGER = 'd';
        static final char GENERAL = 'g';
        static final char GENERAL_UPPER = 'G';
        static final char HASHCODE = 'h';
        static final char HASHCODE_UPPER = 'H';
        static final char HEXADECIMAL_FLOAT = 'a';
        static final char HEXADECIMAL_FLOAT_UPPER = 'A';
        static final char HEXADECIMAL_INTEGER = 'x';
        static final char HEXADECIMAL_INTEGER_UPPER = 'X';
        static final char LINE_SEPARATOR = 'n';
        static final char OCTAL_INTEGER = 'o';
        static final char PERCENT_SIGN = '%';
        static final char SCIENTIFIC = 'e';
        static final char SCIENTIFIC_UPPER = 'E';
        static final char STRING = 's';
        static final char STRING_UPPER = 'S';

        private Conversion() {
        }

        static boolean isValid(char c) {
            return isGeneral(c) || isInteger(c) || isFloat(c) || isText(c) || c == 't' || isCharacter(c);
        }

        static boolean isGeneral(char c) {
            if (c == 'B' || c == 'H' || c == 'S' || c == 'b' || c == 'h' || c == 's') {
                return true;
            }
            return false;
        }

        static boolean isCharacter(char c) {
            if (c == 'C' || c == 'c') {
                return true;
            }
            return false;
        }

        static boolean isInteger(char c) {
            if (c == 'X' || c == 'd' || c == 'o' || c == 'x') {
                return true;
            }
            return false;
        }

        static boolean isFloat(char c) {
            if (!(c == 'A' || c == 'E' || c == 'G' || c == 'a')) {
                switch (c) {
                    case 'e':
                    case 'f':
                    case 'g':
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }

        static boolean isText(char c) {
            if (c == '%' || c == 'n') {
                return true;
            }
            return false;
        }
    }

    private static class DateTime {
        static final char AM_PM = 'p';
        static final char CENTURY = 'C';
        static final char DATE = 'D';
        static final char DATE_TIME = 'c';
        static final char DAY_OF_MONTH = 'e';
        static final char DAY_OF_MONTH_0 = 'd';
        static final char DAY_OF_YEAR = 'j';
        static final char HOUR = 'l';
        static final char HOUR_0 = 'I';
        static final char HOUR_OF_DAY = 'k';
        static final char HOUR_OF_DAY_0 = 'H';
        static final char ISO_STANDARD_DATE = 'F';
        static final char MILLISECOND = 'L';
        static final char MILLISECOND_SINCE_EPOCH = 'Q';
        static final char MINUTE = 'M';
        static final char MONTH = 'm';
        static final char NAME_OF_DAY = 'A';
        static final char NAME_OF_DAY_ABBREV = 'a';
        static final char NAME_OF_MONTH = 'B';
        static final char NAME_OF_MONTH_ABBREV = 'b';
        static final char NAME_OF_MONTH_ABBREV_X = 'h';
        static final char NANOSECOND = 'N';
        static final char SECOND = 'S';
        static final char SECONDS_SINCE_EPOCH = 's';
        static final char TIME = 'T';
        static final char TIME_12_HOUR = 'r';
        static final char TIME_24_HOUR = 'R';
        static final char YEAR_2 = 'y';
        static final char YEAR_4 = 'Y';
        static final char ZONE = 'Z';
        static final char ZONE_NUMERIC = 'z';

        private DateTime() {
        }

        static boolean isValid(char c) {
            switch (c) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case Types.DATALINK:
                case 'H':
                case 'I':
                case 'L':
                case 'M':
                case 'N':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'h':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'p':
                case 'r':
                case 's':
                case 'y':
                case 'z':
                    return true;
                default:
                    return false;
            }
        }
    }

    private class FixedString implements FormatString {
        private String s;

        FixedString(String s2) {
            this.s = s2;
        }

        public int index() {
            return -2;
        }

        public void print(Object arg, Locale l) throws IOException {
            Formatter.this.a.append((CharSequence) this.s);
        }

        public String toString() {
            return this.s;
        }
    }

    private static class Flags {
        static final Flags ALTERNATE = new Flags(4);
        static final Flags GROUP = new Flags(64);
        static final Flags LEADING_SPACE = new Flags(16);
        static final Flags LEFT_JUSTIFY = new Flags(1);
        static final Flags NONE = new Flags(0);
        static final Flags PARENTHESES = new Flags(128);
        static final Flags PLUS = new Flags(8);
        static final Flags PREVIOUS = new Flags(256);
        static final Flags UPPERCASE = new Flags(2);
        static final Flags ZERO_PAD = new Flags(32);
        private int flags;

        private Flags(int f) {
            this.flags = f;
        }

        public int valueOf() {
            return this.flags;
        }

        public boolean contains(Flags f) {
            return (this.flags & f.valueOf()) == f.valueOf();
        }

        public Flags dup() {
            return new Flags(this.flags);
        }

        /* access modifiers changed from: private */
        public Flags add(Flags f) {
            this.flags |= f.valueOf();
            return this;
        }

        public Flags remove(Flags f) {
            this.flags &= ~f.valueOf();
            return this;
        }

        public static Flags parse(String s) {
            char[] ca = s.toCharArray();
            int i = 0;
            Flags f = new Flags(0);
            while (i < ca.length) {
                Flags v = parse(ca[i]);
                if (!f.contains(v)) {
                    f.add(v);
                    i++;
                } else {
                    throw new DuplicateFormatFlagsException(v.toString());
                }
            }
            return f;
        }

        private static Flags parse(char c) {
            if (c == ' ') {
                return LEADING_SPACE;
            }
            if (c == '#') {
                return ALTERNATE;
            }
            if (c == '(') {
                return PARENTHESES;
            }
            if (c == '0') {
                return ZERO_PAD;
            }
            if (c == '<') {
                return PREVIOUS;
            }
            switch (c) {
                case '+':
                    return PLUS;
                case ',':
                    return GROUP;
                case '-':
                    return LEFT_JUSTIFY;
                default:
                    throw new UnknownFormatFlagsException(String.valueOf(c));
            }
        }

        public static String toString(Flags f) {
            return f.toString();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (contains(LEFT_JUSTIFY)) {
                sb.append('-');
            }
            if (contains(UPPERCASE)) {
                sb.append('^');
            }
            if (contains(ALTERNATE)) {
                sb.append('#');
            }
            if (contains(PLUS)) {
                sb.append('+');
            }
            if (contains(LEADING_SPACE)) {
                sb.append(' ');
            }
            if (contains(ZERO_PAD)) {
                sb.append('0');
            }
            if (contains(GROUP)) {
                sb.append(',');
            }
            if (contains(PARENTHESES)) {
                sb.append('(');
            }
            if (contains(PREVIOUS)) {
                sb.append('<');
            }
            return sb.toString();
        }
    }

    private class FormatSpecifier implements FormatString {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private char c;
        private boolean dt = false;
        private Flags f = Flags.NONE;
        private int index = -1;
        private int precision;
        private int width;

        private class BigDecimalLayout {
            private boolean dot = false;
            private StringBuilder exp;
            private StringBuilder mant;
            private int scale;

            public BigDecimalLayout(BigInteger intVal, int scale2, BigDecimalLayoutForm form) {
                layout(intVal, scale2, form);
            }

            public boolean hasDot() {
                return this.dot;
            }

            public int scale() {
                return this.scale;
            }

            public char[] layoutChars() {
                StringBuilder sb = new StringBuilder((CharSequence) this.mant);
                if (this.exp != null) {
                    sb.append('E');
                    sb.append((CharSequence) this.exp);
                }
                return toCharArray(sb);
            }

            public char[] mantissa() {
                return toCharArray(this.mant);
            }

            public char[] exponent() {
                return toCharArray(this.exp);
            }

            private char[] toCharArray(StringBuilder sb) {
                if (sb == null) {
                    return null;
                }
                char[] result = new char[sb.length()];
                sb.getChars(0, result.length, result, 0);
                return result;
            }

            private void layout(BigInteger intVal, int scale2, BigDecimalLayoutForm form) {
                char[] coeff = intVal.toString().toCharArray();
                this.scale = scale2;
                this.mant = new StringBuilder(coeff.length + 14);
                if (scale2 == 0) {
                    int len = coeff.length;
                    if (len > 1) {
                        this.mant.append(coeff[0]);
                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {
                            this.mant.append('.');
                            this.dot = true;
                            this.mant.append(coeff, 1, len - 1);
                            this.exp = new StringBuilder("+");
                            if (len < 10) {
                                StringBuilder sb = this.exp;
                                sb.append("0");
                                sb.append(len - 1);
                            } else {
                                this.exp.append(len - 1);
                            }
                        } else {
                            this.mant.append(coeff, 1, len - 1);
                        }
                    } else {
                        this.mant.append(coeff);
                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {
                            this.exp = new StringBuilder("+00");
                        }
                    }
                    return;
                }
                long adjusted = (-((long) scale2)) + ((long) (coeff.length - 1));
                if (form == BigDecimalLayoutForm.DECIMAL_FLOAT) {
                    int pad = scale2 - coeff.length;
                    if (pad >= 0) {
                        this.mant.append("0.");
                        this.dot = true;
                        while (pad > 0) {
                            this.mant.append('0');
                            pad--;
                        }
                        this.mant.append(coeff);
                    } else if ((-pad) < coeff.length) {
                        this.mant.append(coeff, 0, -pad);
                        this.mant.append('.');
                        this.dot = true;
                        this.mant.append(coeff, -pad, scale2);
                    } else {
                        this.mant.append(coeff, 0, coeff.length);
                        for (int i = 0; i < (-scale2); i++) {
                            this.mant.append('0');
                        }
                        this.scale = 0;
                    }
                } else {
                    this.mant.append(coeff[0]);
                    if (coeff.length > 1) {
                        this.mant.append('.');
                        this.dot = true;
                        this.mant.append(coeff, 1, coeff.length - 1);
                    }
                    this.exp = new StringBuilder();
                    if (adjusted != 0) {
                        long abs = Math.abs(adjusted);
                        this.exp.append(adjusted < 0 ? '-' : '+');
                        if (abs < 10) {
                            this.exp.append('0');
                        }
                        this.exp.append(abs);
                    } else {
                        this.exp.append("+00");
                    }
                }
            }
        }

        static {
            Class<Formatter> cls = Formatter.class;
        }

        private int index(String s) {
            if (s != null) {
                try {
                    this.index = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                }
            } else {
                this.index = 0;
            }
            return this.index;
        }

        public int index() {
            return this.index;
        }

        private Flags flags(String s) {
            this.f = Flags.parse(s);
            if (this.f.contains(Flags.PREVIOUS)) {
                this.index = -1;
            }
            return this.f;
        }

        /* access modifiers changed from: package-private */
        public Flags flags() {
            return this.f;
        }

        private int width(String s) {
            this.width = -1;
            if (s != null) {
                try {
                    this.width = Integer.parseInt(s);
                    if (this.width < 0) {
                        throw new IllegalFormatWidthException(this.width);
                    }
                } catch (NumberFormatException e) {
                }
            }
            return this.width;
        }

        /* access modifiers changed from: package-private */
        public int width() {
            return this.width;
        }

        private int precision(String s) {
            this.precision = -1;
            if (s != null) {
                try {
                    this.precision = Integer.parseInt(s);
                    if (this.precision < 0) {
                        throw new IllegalFormatPrecisionException(this.precision);
                    }
                } catch (NumberFormatException e) {
                }
            }
            return this.precision;
        }

        /* access modifiers changed from: package-private */
        public int precision() {
            return this.precision;
        }

        private char conversion(String s) {
            this.c = s.charAt(0);
            if (!this.dt) {
                if (Conversion.isValid(this.c)) {
                    if (Character.isUpperCase(this.c)) {
                        Flags unused = this.f.add(Flags.UPPERCASE);
                    }
                    this.c = Character.toLowerCase(this.c);
                    if (Conversion.isText(this.c)) {
                        this.index = -2;
                    }
                } else {
                    throw new UnknownFormatConversionException(String.valueOf(this.c));
                }
            }
            return this.c;
        }

        private char conversion() {
            return this.c;
        }

        FormatSpecifier(String indexStr, String flagsStr, String widthStr, String precisionStr, String tTStr, String convStr) {
            index(indexStr);
            flags(flagsStr);
            width(widthStr);
            precision(precisionStr);
            if (tTStr != null) {
                this.dt = true;
                if (tTStr.equals("T")) {
                    Flags unused = this.f.add(Flags.UPPERCASE);
                }
            }
            conversion(convStr);
            if (this.dt) {
                checkDateTime();
            } else if (Conversion.isGeneral(this.c)) {
                checkGeneral();
            } else if (Conversion.isCharacter(this.c)) {
                checkCharacter();
            } else if (Conversion.isInteger(this.c)) {
                checkInteger();
            } else if (Conversion.isFloat(this.c)) {
                checkFloat();
            } else if (Conversion.isText(this.c)) {
                checkText();
            } else {
                throw new UnknownFormatConversionException(String.valueOf(this.c));
            }
        }

        public void print(Object arg, Locale l) throws IOException {
            if (this.dt) {
                printDateTime(arg, l);
                return;
            }
            char c2 = this.c;
            if (c2 != '%') {
                if (c2 != 'C') {
                    if (c2 != 's') {
                        if (c2 != 'x') {
                            switch (c2) {
                                case 'a':
                                case 'e':
                                case 'f':
                                case 'g':
                                    printFloat(arg, l);
                                    break;
                                case 'b':
                                    printBoolean(arg);
                                    break;
                                case 'c':
                                    break;
                                case 'd':
                                    break;
                                case 'h':
                                    printHashCode(arg);
                                    break;
                                default:
                                    switch (c2) {
                                        case 'n':
                                            Formatter.this.a.append((CharSequence) System.lineSeparator());
                                            break;
                                        case 'o':
                                            break;
                                    }
                            }
                        }
                        printInteger(arg, l);
                    } else {
                        printString(arg, l);
                    }
                }
                printCharacter(arg);
            } else {
                Formatter.this.a.append('%');
            }
        }

        private void printInteger(Object arg, Locale l) throws IOException {
            if (arg == null) {
                print("null");
            } else if (arg instanceof Byte) {
                print(((Byte) arg).byteValue(), l);
            } else if (arg instanceof Short) {
                print(((Short) arg).shortValue(), l);
            } else if (arg instanceof Integer) {
                print(((Integer) arg).intValue(), l);
            } else if (arg instanceof Long) {
                print(((Long) arg).longValue(), l);
            } else if (arg instanceof BigInteger) {
                print((BigInteger) arg, l);
            } else {
                failConversion(this.c, arg);
            }
        }

        private void printFloat(Object arg, Locale l) throws IOException {
            if (arg == null) {
                print("null");
            } else if (arg instanceof Float) {
                print(((Float) arg).floatValue(), l);
            } else if (arg instanceof Double) {
                print(((Double) arg).doubleValue(), l);
            } else if (arg instanceof BigDecimal) {
                print((BigDecimal) arg, l);
            } else {
                failConversion(this.c, arg);
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.util.Calendar} */
        /* JADX WARNING: Multi-variable type inference failed */
        private void printDateTime(Object arg, Locale l) throws IOException {
            if (arg == null) {
                print("null");
                return;
            }
            Calendar cal = null;
            if (arg instanceof Long) {
                cal = Calendar.getInstance(l == null ? Locale.US : l);
                cal.setTimeInMillis(((Long) arg).longValue());
            } else if (arg instanceof Date) {
                cal = Calendar.getInstance(l == null ? Locale.US : l);
                cal.setTime((Date) arg);
            } else if (arg instanceof Calendar) {
                cal = ((Calendar) arg).clone();
                cal.setLenient(true);
            } else if (arg instanceof TemporalAccessor) {
                print((TemporalAccessor) arg, this.c, l);
                return;
            } else {
                failConversion(this.c, arg);
            }
            print(cal, this.c, l);
        }

        private void printCharacter(Object arg) throws IOException {
            if (arg == null) {
                print("null");
                return;
            }
            String s = null;
            if (arg instanceof Character) {
                s = ((Character) arg).toString();
            } else if (arg instanceof Byte) {
                byte i = ((Byte) arg).byteValue();
                if (Character.isValidCodePoint(i)) {
                    s = new String(Character.toChars(i));
                } else {
                    throw new IllegalFormatCodePointException(i);
                }
            } else if ((arg instanceof Short) != 0) {
                short i2 = ((Short) arg).shortValue();
                if (Character.isValidCodePoint(i2)) {
                    s = new String(Character.toChars(i2));
                } else {
                    throw new IllegalFormatCodePointException(i2);
                }
            } else if ((arg instanceof Integer) != 0) {
                int i3 = ((Integer) arg).intValue();
                if (Character.isValidCodePoint(i3)) {
                    s = new String(Character.toChars(i3));
                } else {
                    throw new IllegalFormatCodePointException(i3);
                }
            } else {
                failConversion(this.c, arg);
            }
            print(s);
        }

        private void printString(Object arg, Locale l) throws IOException {
            if (arg instanceof Formattable) {
                Formatter fmt = Formatter.this;
                if (fmt.locale() != l) {
                    fmt = new Formatter(fmt.out(), l);
                }
                ((Formattable) arg).formatTo(fmt, this.f.valueOf(), this.width, this.precision);
                return;
            }
            if (this.f.contains(Flags.ALTERNATE)) {
                failMismatch(Flags.ALTERNATE, 's');
            }
            if (arg == null) {
                print("null");
            } else {
                print(arg.toString());
            }
        }

        private void printBoolean(Object arg) throws IOException {
            String s;
            if (arg == null) {
                s = Boolean.toString(false);
            } else if (arg instanceof Boolean) {
                s = ((Boolean) arg).toString();
            } else {
                s = Boolean.toString(true);
            }
            print(s);
        }

        private void printHashCode(Object arg) throws IOException {
            String s;
            if (arg == null) {
                s = "null";
            } else {
                s = Integer.toHexString(arg.hashCode());
            }
            print(s);
        }

        private void print(String s) throws IOException {
            if (this.precision != -1 && this.precision < s.length()) {
                s = s.substring(0, this.precision);
            }
            if (this.f.contains(Flags.UPPERCASE)) {
                s = s.toUpperCase(Formatter.this.l != null ? Formatter.this.l : Locale.getDefault());
            }
            Formatter.this.a.append((CharSequence) justify(s));
        }

        private String justify(String s) {
            if (this.width == -1) {
                return s;
            }
            StringBuilder sb = new StringBuilder();
            boolean pad = this.f.contains(Flags.LEFT_JUSTIFY);
            int sp = this.width - s.length();
            if (!pad) {
                for (int i = 0; i < sp; i++) {
                    sb.append(' ');
                }
            }
            sb.append(s);
            if (pad) {
                for (int i2 = 0; i2 < sp; i2++) {
                    sb.append(' ');
                }
            }
            return sb.toString();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("%");
            sb.append(this.f.dup().remove(Flags.UPPERCASE).toString());
            if (this.index > 0) {
                sb.append(this.index);
                sb.append('$');
            }
            if (this.width != -1) {
                sb.append(this.width);
            }
            if (this.precision != -1) {
                sb.append('.');
                sb.append(this.precision);
            }
            if (this.dt) {
                sb.append(this.f.contains(Flags.UPPERCASE) ? 'T' : 't');
            }
            sb.append(this.f.contains(Flags.UPPERCASE) ? Character.toUpperCase(this.c) : this.c);
            return sb.toString();
        }

        private void checkGeneral() {
            if ((this.c == 'b' || this.c == 'h') && this.f.contains(Flags.ALTERNATE)) {
                failMismatch(Flags.ALTERNATE, this.c);
            }
            if (this.width != -1 || !this.f.contains(Flags.LEFT_JUSTIFY)) {
                checkBadFlags(Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
                return;
            }
            throw new MissingFormatWidthException(toString());
        }

        private void checkDateTime() {
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            } else if (DateTime.isValid(this.c)) {
                checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
                if (this.width == -1 && this.f.contains(Flags.LEFT_JUSTIFY)) {
                    throw new MissingFormatWidthException(toString());
                }
            } else {
                throw new UnknownFormatConversionException("t" + this.c);
            }
        }

        private void checkCharacter() {
            if (this.precision == -1) {
                checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
                if (this.width == -1 && this.f.contains(Flags.LEFT_JUSTIFY)) {
                    throw new MissingFormatWidthException(toString());
                }
                return;
            }
            throw new IllegalFormatPrecisionException(this.precision);
        }

        private void checkInteger() {
            checkNumeric();
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            } else if (this.c == 'd') {
                checkBadFlags(Flags.ALTERNATE);
            } else if (this.c == 'o') {
                checkBadFlags(Flags.GROUP);
            } else {
                checkBadFlags(Flags.GROUP);
            }
        }

        private void checkBadFlags(Flags... badFlags) {
            for (int i = 0; i < badFlags.length; i++) {
                if (this.f.contains(badFlags[i])) {
                    failMismatch(badFlags[i], this.c);
                }
            }
        }

        private void checkFloat() {
            checkNumeric();
            if (this.c != 'f') {
                if (this.c == 'a') {
                    checkBadFlags(Flags.PARENTHESES, Flags.GROUP);
                } else if (this.c == 'e') {
                    checkBadFlags(Flags.GROUP);
                } else if (this.c == 'g') {
                    checkBadFlags(Flags.ALTERNATE);
                }
            }
        }

        private void checkNumeric() {
            if (this.width != -1 && this.width < 0) {
                throw new IllegalFormatWidthException(this.width);
            } else if (this.precision != -1 && this.precision < 0) {
                throw new IllegalFormatPrecisionException(this.precision);
            } else if (this.width == -1 && (this.f.contains(Flags.LEFT_JUSTIFY) || this.f.contains(Flags.ZERO_PAD))) {
                throw new MissingFormatWidthException(toString());
            } else if ((this.f.contains(Flags.PLUS) && this.f.contains(Flags.LEADING_SPACE)) || (this.f.contains(Flags.LEFT_JUSTIFY) && this.f.contains(Flags.ZERO_PAD))) {
                throw new IllegalFormatFlagsException(this.f.toString());
            }
        }

        private void checkText() {
            if (this.precision == -1) {
                char c2 = this.c;
                if (c2 != '%') {
                    if (c2 == 'n') {
                        if (this.width != -1) {
                            throw new IllegalFormatWidthException(this.width);
                        } else if (this.f.valueOf() != Flags.NONE.valueOf()) {
                            throw new IllegalFormatFlagsException(this.f.toString());
                        }
                    }
                } else if (this.f.valueOf() != Flags.LEFT_JUSTIFY.valueOf() && this.f.valueOf() != Flags.NONE.valueOf()) {
                    throw new IllegalFormatFlagsException(this.f.toString());
                } else if (this.width == -1 && this.f.contains(Flags.LEFT_JUSTIFY)) {
                    throw new MissingFormatWidthException(toString());
                }
            } else {
                throw new IllegalFormatPrecisionException(this.precision);
            }
        }

        private void print(byte value, Locale l) throws IOException {
            long v = (long) value;
            if (value < 0 && (this.c == 'o' || this.c == 'x')) {
                v += 256;
            }
            print(v, l);
        }

        private void print(short value, Locale l) throws IOException {
            long v = (long) value;
            if (value < 0 && (this.c == 'o' || this.c == 'x')) {
                v += 65536;
            }
            print(v, l);
        }

        private void print(int value, Locale l) throws IOException {
            long v = (long) value;
            if (value < 0 && (this.c == 'o' || this.c == 'x')) {
                v += 4294967296L;
            }
            print(v, l);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v9, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v1, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v13, resolved type: int} */
        /* JADX WARNING: Multi-variable type inference failed */
        private void print(long value, Locale l) throws IOException {
            int len;
            int i;
            char[] charArray;
            StringBuilder sb = new StringBuilder();
            int i2 = 0;
            if (this.c == 'd') {
                if (value < 0) {
                    i2 = 1;
                }
                boolean neg = i2;
                if (value < 0) {
                    charArray = Long.toString(value, 10).substring(1).toCharArray();
                } else {
                    charArray = Long.toString(value, 10).toCharArray();
                }
                char[] va = charArray;
                leadingSign(sb, neg);
                localizedMagnitude(sb, va, this.f, adjustWidth(this.width, this.f, neg), l);
                trailingSign(sb, neg);
            } else if (this.c == 'o') {
                checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE, Flags.PLUS);
                String s = Long.toOctalString(value);
                if (this.f.contains(Flags.ALTERNATE)) {
                    i = s.length() + 1;
                } else {
                    i = s.length();
                }
                int len2 = i;
                if (this.f.contains(Flags.ALTERNATE)) {
                    sb.append('0');
                }
                if (this.f.contains(Flags.ZERO_PAD)) {
                    while (i2 < this.width - len2) {
                        sb.append('0');
                        i2++;
                    }
                }
                sb.append(s);
            } else if (this.c == 'x') {
                checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE, Flags.PLUS);
                String s2 = Long.toHexString(value);
                if (this.f.contains(Flags.ALTERNATE)) {
                    len = s2.length() + 2;
                } else {
                    len = s2.length();
                }
                if (this.f.contains(Flags.ALTERNATE)) {
                    sb.append(this.f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                }
                if (this.f.contains(Flags.ZERO_PAD)) {
                    while (i2 < this.width - len) {
                        sb.append('0');
                        i2++;
                    }
                }
                if (this.f.contains(Flags.UPPERCASE)) {
                    s2 = s2.toUpperCase();
                }
                sb.append(s2);
            }
            Formatter.this.a.append((CharSequence) justify(sb.toString()));
        }

        private StringBuilder leadingSign(StringBuilder sb, boolean neg) {
            if (!neg) {
                if (this.f.contains(Flags.PLUS)) {
                    sb.append('+');
                } else if (this.f.contains(Flags.LEADING_SPACE)) {
                    sb.append(' ');
                }
            } else if (this.f.contains(Flags.PARENTHESES)) {
                sb.append('(');
            } else {
                sb.append('-');
            }
            return sb;
        }

        private StringBuilder trailingSign(StringBuilder sb, boolean neg) {
            if (neg && this.f.contains(Flags.PARENTHESES)) {
                sb.append(')');
            }
            return sb;
        }

        private void print(BigInteger value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean z = false;
            boolean neg = value.signum() == -1;
            BigInteger v = value.abs();
            leadingSign(sb, neg);
            if (this.c == 'd') {
                localizedMagnitude(sb, v.toString().toCharArray(), this.f, adjustWidth(this.width, this.f, neg), l);
            } else if (this.c == 'o') {
                String s = v.toString(8);
                int len = s.length() + sb.length();
                if (neg && this.f.contains(Flags.PARENTHESES)) {
                    len++;
                }
                if (this.f.contains(Flags.ALTERNATE)) {
                    len++;
                    sb.append('0');
                }
                if (this.f.contains(Flags.ZERO_PAD)) {
                    for (int i = 0; i < this.width - len; i++) {
                        sb.append('0');
                    }
                }
                sb.append(s);
            } else if (this.c == 'x') {
                String s2 = v.toString(16);
                int len2 = s2.length() + sb.length();
                if (neg && this.f.contains(Flags.PARENTHESES)) {
                    len2++;
                }
                if (this.f.contains(Flags.ALTERNATE)) {
                    len2 += 2;
                    sb.append(this.f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                }
                if (this.f.contains(Flags.ZERO_PAD)) {
                    for (int i2 = 0; i2 < this.width - len2; i2++) {
                        sb.append('0');
                    }
                }
                if (this.f.contains(Flags.UPPERCASE)) {
                    s2 = s2.toUpperCase();
                }
                sb.append(s2);
            }
            if (value.signum() == -1) {
                z = true;
            }
            trailingSign(sb, z);
            Formatter.this.a.append((CharSequence) justify(sb.toString()));
        }

        private void print(float value, Locale l) throws IOException {
            print((double) value, l);
        }

        private void print(double value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean neg = Double.compare(value, 0.0d) == -1;
            if (!Double.isNaN(value)) {
                double v = Math.abs(value);
                leadingSign(sb, neg);
                if (!Double.isInfinite(v)) {
                    print(sb, v, l, this.f, this.c, this.precision, neg);
                } else {
                    sb.append(this.f.contains(Flags.UPPERCASE) ? "INFINITY" : "Infinity");
                }
                trailingSign(sb, neg);
            } else {
                sb.append(this.f.contains(Flags.UPPERCASE) ? "NAN" : "NaN");
            }
            Formatter.this.a.append((CharSequence) justify(sb.toString()));
        }

        private void print(StringBuilder sb, double value, Locale l, Flags f2, char c2, int precision2, boolean neg) throws IOException {
            char[] exp;
            char[] exp2;
            int expRounded;
            int prec;
            String str;
            StringBuilder sb2 = sb;
            double d = value;
            Flags flags = f2;
            char c3 = c2;
            int i = precision2;
            boolean z = neg;
            int i2 = 6;
            if (c3 == 'e') {
                if (i != -1) {
                    i2 = i;
                }
                int prec2 = i2;
                FormattedFloatingDecimal fd = FormattedFloatingDecimal.valueOf(d, prec2, FormattedFloatingDecimal.Form.SCIENTIFIC);
                char[] mant = addZeros(fd.getMantissa(), prec2);
                if (flags.contains(Flags.ALTERNATE) && prec2 == 0) {
                    mant = addDot(mant);
                }
                char[] mant2 = mant;
                char[] exp3 = d == 0.0d ? new char[]{'+', '0', '0'} : fd.getExponent();
                int newW = this.width;
                if (this.width != -1) {
                    newW = adjustWidth((this.width - exp3.length) - 1, flags, z);
                }
                int i3 = prec2;
                char[] exp4 = exp3;
                FormattedFloatingDecimal formattedFloatingDecimal = fd;
                char[] cArr = mant2;
                localizedMagnitude(sb2, mant2, flags, newW, l);
                Locale separatorLocale = l != null ? l : Locale.getDefault();
                LocaleData localeData = LocaleData.get(separatorLocale);
                if (flags.contains(Flags.UPPERCASE)) {
                    str = localeData.exponentSeparator.toUpperCase(separatorLocale);
                } else {
                    str = localeData.exponentSeparator.toLowerCase(separatorLocale);
                }
                sb2.append(str);
                Flags flags2 = f2.dup().remove(Flags.GROUP);
                char sign = exp4[0];
                sb2.append(sign);
                char[] tmp = new char[(exp4.length - 1)];
                System.arraycopy((Object) exp4, 1, (Object) tmp, 0, exp4.length - 1);
                char[] cArr2 = tmp;
                char c4 = sign;
                LocaleData localeData2 = localeData;
                Locale locale = separatorLocale;
                sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, tmp, flags2, -1, l));
            } else if (c3 == 'f') {
                if (i != -1) {
                    i2 = i;
                }
                int prec3 = i2;
                char[] mant3 = addZeros(FormattedFloatingDecimal.valueOf(d, prec3, FormattedFloatingDecimal.Form.DECIMAL_FLOAT).getMantissa(), prec3);
                if (flags.contains(Flags.ALTERNATE) && prec3 == 0) {
                    mant3 = addDot(mant3);
                }
                char[] mant4 = mant3;
                int newW2 = this.width;
                if (this.width != -1) {
                    newW2 = adjustWidth(this.width, flags, z);
                }
                localizedMagnitude(sb2, mant4, flags, newW2, l);
            } else if (c3 == 'g') {
                int prec4 = i;
                if (i == -1) {
                    prec4 = 6;
                } else if (i == 0) {
                    prec4 = 1;
                }
                if (d == 0.0d) {
                    exp2 = new char[]{'0'};
                    exp = null;
                    expRounded = 0;
                } else {
                    FormattedFloatingDecimal fd2 = FormattedFloatingDecimal.valueOf(d, prec4, FormattedFloatingDecimal.Form.GENERAL);
                    char[] exp5 = fd2.getExponent();
                    char[] mant5 = fd2.getMantissa();
                    expRounded = fd2.getExponentRounded();
                    exp = exp5;
                    exp2 = mant5;
                }
                int expRounded2 = expRounded;
                if (exp != null) {
                    prec = prec4 - 1;
                } else {
                    prec = prec4 - (expRounded2 + 1);
                }
                int prec5 = prec;
                char[] mant6 = addZeros(exp2, prec5);
                if (flags.contains(Flags.ALTERNATE) && prec5 == 0) {
                    mant6 = addDot(mant6);
                }
                char[] mant7 = mant6;
                int newW3 = this.width;
                if (this.width != -1) {
                    if (exp != null) {
                        newW3 = adjustWidth((this.width - exp.length) - 1, flags, z);
                    } else {
                        newW3 = adjustWidth(this.width, flags, z);
                    }
                }
                int i4 = prec5;
                char[] exp6 = exp;
                localizedMagnitude(sb2, mant7, flags, newW3, l);
                if (exp6 != null) {
                    sb2.append(flags.contains(Flags.UPPERCASE) ? 'E' : 'e');
                    Flags flags3 = f2.dup().remove(Flags.GROUP);
                    char sign2 = exp6[0];
                    sb2.append(sign2);
                    char[] tmp2 = new char[(exp6.length - 1)];
                    System.arraycopy((Object) exp6, 1, (Object) tmp2, 0, exp6.length - 1);
                    char[] cArr3 = tmp2;
                    char c5 = sign2;
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, tmp2, flags3, -1, l));
                }
            } else if (c3 == 'a') {
                int prec6 = i;
                if (i == -1) {
                    prec6 = 0;
                } else if (i == 0) {
                    prec6 = 1;
                }
                String s = hexDouble(d, prec6);
                boolean upper = flags.contains(Flags.UPPERCASE);
                sb2.append(upper ? "0X" : "0x");
                if (flags.contains(Flags.ZERO_PAD)) {
                    for (int i5 = 0; i5 < (this.width - s.length()) - 2; i5++) {
                        sb2.append('0');
                    }
                }
                char c6 = 'p';
                int idx = s.indexOf(112);
                char[] va = s.substring(0, idx).toCharArray();
                if (upper) {
                    va = new String(va).toUpperCase(Locale.US).toCharArray();
                }
                sb2.append(prec6 != 0 ? addZeros(va, prec6) : va);
                if (upper) {
                    c6 = 'P';
                }
                sb2.append(c6);
                sb2.append(s.substring(idx + 1));
            }
        }

        private char[] addZeros(char[] v, int prec) {
            int i = 0;
            while (i < v.length && v[i] != '.') {
                i++;
            }
            boolean needDot = false;
            if (i == v.length) {
                needDot = true;
            }
            int i2 = 1;
            int outPrec = (v.length - i) - (needDot ? 0 : 1);
            if (outPrec == prec) {
                return v;
            }
            int length = (v.length + prec) - outPrec;
            if (!needDot) {
                i2 = 0;
            }
            char[] tmp = new char[(length + i2)];
            System.arraycopy((Object) v, 0, (Object) tmp, 0, v.length);
            int start = v.length;
            if (needDot) {
                tmp[v.length] = '.';
                start++;
            }
            for (int j = start; j < tmp.length; j++) {
                tmp[j] = '0';
            }
            return tmp;
        }

        private String hexDouble(double d, int prec) {
            double d2;
            int i = prec;
            if (!Double.isFinite(d) || d == 0.0d || i == 0 || i >= 13) {
                return Double.toHexString(d).substring(2);
            }
            int exponent = Math.getExponent(d);
            boolean subnormal = exponent == -1023;
            if (subnormal) {
                double unused = Formatter.scaleUp = Math.scalb(1.0d, 54);
                d2 = d * Formatter.scaleUp;
                exponent = Math.getExponent(d2);
            } else {
                d2 = d;
            }
            int precision2 = (i * 4) + 1;
            int shiftDistance = 53 - precision2;
            long doppel = Double.doubleToLongBits(d2);
            long newSignif = (Long.MAX_VALUE & doppel) >> shiftDistance;
            int i2 = precision2;
            long roundingBits = (~(-1 << shiftDistance)) & doppel;
            boolean leastZero = (newSignif & 1) == 0;
            boolean round = ((1 << (shiftDistance + -1)) & roundingBits) != 0;
            boolean sticky = shiftDistance > 1 && ((~(1 << (shiftDistance + -1))) & roundingBits) != 0;
            if ((leastZero && round && sticky) || (!leastZero && round)) {
                newSignif++;
            }
            boolean subnormal2 = subnormal;
            double result = Double.longBitsToDouble((Long.MIN_VALUE & doppel) | (newSignif << shiftDistance));
            if (Double.isInfinite(result)) {
                return "1.0p1024";
            }
            double d3 = d2;
            String res = Double.toHexString(result).substring(2);
            if (!subnormal2) {
                return res;
            }
            int idx = res.indexOf(112);
            if (idx == -1) {
                return null;
            }
            int i3 = exponent;
            StringBuilder sb = new StringBuilder();
            double d4 = result;
            sb.append(res.substring(0, idx));
            sb.append("p");
            sb.append(Integer.toString(Integer.parseInt(res.substring(idx + 1)) - 54));
            return sb.toString();
        }

        private void print(BigDecimal value, Locale l) throws IOException {
            if (this.c == 'a') {
                failConversion(this.c, value);
            }
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigDecimal v = value.abs();
            leadingSign(sb, neg);
            print(sb, v, l, this.f, this.c, this.precision, neg);
            trailingSign(sb, neg);
            Formatter.this.a.append((CharSequence) justify(sb.toString()));
        }

        private void print(StringBuilder sb, BigDecimal value, Locale l, Flags f2, char c2, int precision2, boolean neg) throws IOException {
            int compPrec;
            int newW;
            StringBuilder sb2 = sb;
            BigDecimal value2 = value;
            Flags flags = f2;
            char c3 = c2;
            int i = precision2;
            boolean z = neg;
            int i2 = 6;
            if (c3 == 'e') {
                if (i != -1) {
                    i2 = i;
                }
                int prec = i2;
                int scale = value.scale();
                int origPrec = value.precision();
                int nzeros = 0;
                if (prec > origPrec - 1) {
                    compPrec = origPrec;
                    nzeros = prec - (origPrec - 1);
                } else {
                    compPrec = prec + 1;
                }
                MathContext mc = new MathContext(compPrec);
                BigDecimal v = new BigDecimal(value.unscaledValue(), scale, mc);
                int i3 = compPrec;
                int i4 = scale;
                BigDecimalLayout bdl = new BigDecimalLayout(v.unscaledValue(), v.scale(), BigDecimalLayoutForm.SCIENTIFIC);
                char[] mant = bdl.mantissa();
                if ((origPrec == 1 || !bdl.hasDot()) && (nzeros > 0 || flags.contains(Flags.ALTERNATE))) {
                    mant = addDot(mant);
                }
                char[] mant2 = trailingZeros(mant, nzeros);
                char[] exp = bdl.exponent();
                int newW2 = this.width;
                int i5 = nzeros;
                if (this.width != -1) {
                    newW = adjustWidth((this.width - exp.length) - 1, flags, z);
                } else {
                    newW = newW2;
                }
                int i6 = origPrec;
                MathContext mathContext = mc;
                char[] exp2 = exp;
                int i7 = prec;
                localizedMagnitude(sb2, mant2, flags, newW, l);
                sb2.append(flags.contains(Flags.UPPERCASE) ? 'E' : 'e');
                Flags flags2 = f2.dup().remove(Flags.GROUP);
                char c4 = exp2[0];
                sb2.append(exp2[0]);
                char[] tmp = new char[(exp2.length - 1)];
                System.arraycopy((Object) exp2, 1, (Object) tmp, 0, exp2.length - 1);
                char[] cArr = tmp;
                sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, tmp, flags2, -1, l));
                return;
            }
            int i8 = 0;
            if (c3 == 'f') {
                if (i != -1) {
                    i2 = i;
                }
                int prec2 = i2;
                int scale2 = value.scale();
                if (scale2 > prec2) {
                    int compPrec2 = value.precision();
                    if (compPrec2 <= scale2) {
                        value2 = value2.setScale(prec2, RoundingMode.HALF_UP);
                    } else {
                        value2 = new BigDecimal(value.unscaledValue(), scale2, new MathContext(compPrec2 - (scale2 - prec2)));
                    }
                }
                BigDecimalLayout bdl2 = new BigDecimalLayout(value2.unscaledValue(), value2.scale(), BigDecimalLayoutForm.DECIMAL_FLOAT);
                char[] mant3 = bdl2.mantissa();
                if (bdl2.scale() < prec2) {
                    i8 = prec2 - bdl2.scale();
                }
                int nzeros2 = i8;
                if (bdl2.scale() == 0 && (flags.contains(Flags.ALTERNATE) || nzeros2 > 0)) {
                    mant3 = addDot(bdl2.mantissa());
                }
                int i9 = nzeros2;
                localizedMagnitude(sb2, trailingZeros(mant3, nzeros2), flags, adjustWidth(this.width, flags, z), l);
            } else if (c3 == 'g') {
                int prec3 = i;
                if (i == -1) {
                    prec3 = 6;
                } else if (i == 0) {
                    prec3 = 1;
                }
                int prec4 = prec3;
                BigDecimal tenToTheNegFour = BigDecimal.valueOf(1, 4);
                BigDecimal tenToThePrec = BigDecimal.valueOf(1, -prec4);
                if (value2.equals(BigDecimal.ZERO)) {
                    BigDecimal bigDecimal = tenToThePrec;
                    BigDecimal bigDecimal2 = tenToTheNegFour;
                } else if (value2.compareTo(tenToTheNegFour) == -1 || value2.compareTo(tenToThePrec) != -1) {
                    BigDecimal bigDecimal3 = tenToThePrec;
                    BigDecimal bigDecimal4 = tenToTheNegFour;
                    print(sb2, value2, l, flags, 'e', prec4 - 1, z);
                    return;
                } else {
                    BigDecimal bigDecimal5 = tenToThePrec;
                    BigDecimal bigDecimal6 = tenToTheNegFour;
                }
                print(sb2, value2, l, flags, 'f', (prec4 - ((-value.scale()) + (value.unscaledValue().toString().length() - 1))) - 1, z);
            } else {
                if (c3 == 'a') {
                }
            }
        }

        private int adjustWidth(int width2, Flags f2, boolean neg) {
            int newW = width2;
            if (newW == -1 || !neg || !f2.contains(Flags.PARENTHESES)) {
                return newW;
            }
            return newW - 1;
        }

        private char[] addDot(char[] mant) {
            char[] cArr = mant;
            char[] tmp = new char[(mant.length + 1)];
            System.arraycopy((Object) mant, 0, (Object) tmp, 0, mant.length);
            tmp[tmp.length - 1] = '.';
            return tmp;
        }

        private char[] trailingZeros(char[] mant, int nzeros) {
            char[] tmp = mant;
            if (nzeros > 0) {
                tmp = new char[(mant.length + nzeros)];
                System.arraycopy((Object) mant, 0, (Object) tmp, 0, mant.length);
                for (int i = mant.length; i < tmp.length; i++) {
                    tmp[i] = '0';
                }
            }
            return tmp;
        }

        private void print(Calendar t, char c2, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            print(sb, t, c2, l);
            String s = justify(sb.toString());
            if (this.f.contains(Flags.UPPERCASE)) {
                s = s.toUpperCase();
            }
            Formatter.this.a.append((CharSequence) s);
        }

        private Appendable print(StringBuilder sb, Calendar t, char c2, Locale l) throws IOException {
            StringBuilder sb2;
            int i;
            Flags flags;
            Locale locale;
            Flags flags2;
            Calendar calendar = t;
            char c3 = c2;
            Locale locale2 = l;
            if (sb == null) {
                sb2 = new StringBuilder();
            } else {
                sb2 = sb;
            }
            boolean z = false;
            boolean z2 = true;
            switch (c3) {
                case 'A':
                case 'a':
                    int i2 = calendar.get(7);
                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale2 == null ? Locale.US : locale2);
                    if (c3 != 'A') {
                        sb2.append(dfs.getShortWeekdays()[i2]);
                        break;
                    } else {
                        sb2.append(dfs.getWeekdays()[i2]);
                        break;
                    }
                case 'B':
                case 'b':
                case 'h':
                    int i3 = calendar.get(2);
                    DateFormatSymbols dfs2 = DateFormatSymbols.getInstance(locale2 == null ? Locale.US : locale2);
                    if (c3 != 'B') {
                        sb2.append(dfs2.getShortMonths()[i3]);
                        break;
                    } else {
                        sb2.append(dfs2.getMonths()[i3]);
                        break;
                    }
                case 'C':
                case 'Y':
                case 'y':
                    int i4 = calendar.get(1);
                    int size = 2;
                    if (c3 == 'C') {
                        i4 /= 100;
                    } else if (c3 == 'Y') {
                        size = 4;
                    } else if (c3 == 'y') {
                        i4 %= 100;
                    }
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) i4, Flags.ZERO_PAD, size, locale2));
                    break;
                case 'D':
                    print(sb2, calendar, 'm', locale2).append('/');
                    print(sb2, calendar, 'd', locale2).append('/');
                    print(sb2, calendar, 'y', locale2);
                    break;
                case Types.DATALINK:
                    print(sb2, calendar, 'Y', locale2).append('-');
                    print(sb2, calendar, 'm', locale2).append('-');
                    print(sb2, calendar, 'd', locale2);
                    break;
                case 'H':
                case 'I':
                case 'k':
                case 'l':
                    int i5 = calendar.get(11);
                    if (c3 == 'I' || c3 == 'l') {
                        if (i5 != 0) {
                            i = 12;
                            if (i5 != 12) {
                                i = i5 % 12;
                            }
                        } else {
                            i = 12;
                        }
                        i5 = i;
                    }
                    int i6 = i5;
                    if (c3 == 'H' || c3 == 'I') {
                        flags = Flags.ZERO_PAD;
                    } else {
                        flags = Flags.NONE;
                    }
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) i6, flags, 2, locale2));
                    break;
                case 'L':
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) calendar.get(14), Flags.ZERO_PAD, 3, locale2));
                    break;
                case 'M':
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) calendar.get(12), Flags.ZERO_PAD, 2, locale2));
                    break;
                case 'N':
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) (calendar.get(14) * 1000000), Flags.ZERO_PAD, 9, locale2));
                    break;
                case 'Q':
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, t.getTimeInMillis(), Flags.NONE, this.width, locale2));
                    break;
                case 'R':
                case 'T':
                    print(sb2, calendar, 'H', locale2).append(':');
                    print(sb2, calendar, 'M', locale2);
                    if (c3 == 'T') {
                        sb2.append(':');
                        print(sb2, calendar, 'S', locale2);
                        break;
                    }
                    break;
                case 'S':
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) calendar.get(13), Flags.ZERO_PAD, 2, locale2));
                    break;
                case 'Z':
                    TimeZone tz = t.getTimeZone();
                    if (calendar.get(16) == 0) {
                        z2 = false;
                    }
                    if (locale2 == null) {
                        locale = Locale.US;
                    } else {
                        locale = locale2;
                    }
                    sb2.append(tz.getDisplayName(z2, 0, locale));
                    break;
                case 'c':
                    print(sb2, calendar, 'a', locale2).append(' ');
                    print(sb2, calendar, 'b', locale2).append(' ');
                    print(sb2, calendar, 'd', locale2).append(' ');
                    print(sb2, calendar, 'T', locale2).append(' ');
                    print(sb2, calendar, 'Z', locale2).append(' ');
                    print(sb2, calendar, 'Y', locale2);
                    break;
                case 'd':
                case 'e':
                    int i7 = calendar.get(5);
                    if (c3 == 'd') {
                        flags2 = Flags.ZERO_PAD;
                    } else {
                        flags2 = Flags.NONE;
                    }
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) i7, flags2, 2, locale2));
                    break;
                case 'j':
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) calendar.get(6), Flags.ZERO_PAD, 3, locale2));
                    break;
                case 'm':
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) (calendar.get(2) + 1), Flags.ZERO_PAD, 2, locale2));
                    break;
                case 'p':
                    String[] ampm = {"AM", "PM"};
                    if (!(locale2 == null || locale2 == Locale.US)) {
                        ampm = DateFormatSymbols.getInstance(l).getAmPmStrings();
                    }
                    sb2.append(ampm[calendar.get(9)].toLowerCase(locale2 != null ? locale2 : Locale.US));
                    break;
                case 'r':
                    print(sb2, calendar, 'I', locale2).append(':');
                    print(sb2, calendar, 'M', locale2).append(':');
                    print(sb2, calendar, 'S', locale2).append(' ');
                    StringBuilder tsb = new StringBuilder();
                    print(tsb, calendar, 'p', locale2);
                    sb2.append(tsb.toString().toUpperCase(locale2 != null ? locale2 : Locale.US));
                    break;
                case 's':
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, t.getTimeInMillis() / 1000, Flags.NONE, this.width, locale2));
                    break;
                case 'z':
                    int i8 = calendar.get(15) + calendar.get(16);
                    if (i8 < 0) {
                        z = true;
                    }
                    boolean neg = z;
                    sb2.append(neg ? '-' : '+');
                    if (neg) {
                        i8 = -i8;
                    }
                    int min = i8 / 60000;
                    sb2.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) (((min / 60) * 100) + (min % 60)), Flags.ZERO_PAD, 4, locale2));
                    break;
            }
            return sb2;
        }

        private void print(TemporalAccessor t, char c2, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            print(sb, t, c2, l);
            String s = justify(sb.toString());
            if (this.f.contains(Flags.UPPERCASE)) {
                s = s.toUpperCase();
            }
            Formatter.this.a.append((CharSequence) s);
        }

        private Appendable print(StringBuilder sb, TemporalAccessor t, char c2, Locale l) throws IOException {
            Locale locale;
            Flags flags;
            if (sb == null) {
                sb = new StringBuilder();
            }
            boolean z = false;
            switch (c2) {
                case 'A':
                case 'a':
                    int i = (t.get(ChronoField.DAY_OF_WEEK) % 7) + 1;
                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(l == null ? Locale.US : l);
                    if (c2 != 'A') {
                        sb.append(dfs.getShortWeekdays()[i]);
                        break;
                    } else {
                        sb.append(dfs.getWeekdays()[i]);
                        break;
                    }
                case 'B':
                case 'b':
                case 'h':
                    int i2 = t.get(ChronoField.MONTH_OF_YEAR) - 1;
                    DateFormatSymbols dfs2 = DateFormatSymbols.getInstance(l == null ? Locale.US : l);
                    if (c2 != 'B') {
                        sb.append(dfs2.getShortMonths()[i2]);
                        break;
                    } else {
                        sb.append(dfs2.getMonths()[i2]);
                        break;
                    }
                case 'C':
                case 'Y':
                case 'y':
                    int i3 = t.get(ChronoField.YEAR_OF_ERA);
                    int size = 2;
                    if (c2 == 'C') {
                        i3 /= 100;
                    } else if (c2 == 'Y') {
                        size = 4;
                    } else if (c2 == 'y') {
                        i3 %= 100;
                    }
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) i3, Flags.ZERO_PAD, size, l));
                    break;
                case 'D':
                    print(sb, t, 'm', l).append('/');
                    print(sb, t, 'd', l).append('/');
                    print(sb, t, 'y', l);
                    break;
                case Types.DATALINK:
                    print(sb, t, 'Y', l).append('-');
                    print(sb, t, 'm', l).append('-');
                    print(sb, t, 'd', l);
                    break;
                case 'H':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.HOUR_OF_DAY), Flags.ZERO_PAD, 2, l));
                    break;
                case 'I':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.CLOCK_HOUR_OF_AMPM), Flags.ZERO_PAD, 2, l));
                    break;
                case 'L':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.MILLI_OF_SECOND), Flags.ZERO_PAD, 3, l));
                    break;
                case 'M':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.MINUTE_OF_HOUR), Flags.ZERO_PAD, 2, l));
                    break;
                case 'N':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) (t.get(ChronoField.MILLI_OF_SECOND) * 1000000), Flags.ZERO_PAD, 9, l));
                    break;
                case 'Q':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (t.getLong(ChronoField.INSTANT_SECONDS) * 1000) + t.getLong(ChronoField.MILLI_OF_SECOND), Flags.NONE, this.width, l));
                    break;
                case 'R':
                case 'T':
                    print(sb, t, 'H', l).append(':');
                    print(sb, t, 'M', l);
                    if (c2 == 'T') {
                        sb.append(':');
                        print(sb, t, 'S', l);
                        break;
                    }
                    break;
                case 'S':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.SECOND_OF_MINUTE), Flags.ZERO_PAD, 2, l));
                    break;
                case 'Z':
                    ZoneId zid = (ZoneId) t.query(TemporalQueries.zone());
                    if (zid != null) {
                        if (!(zid instanceof ZoneOffset) && t.isSupported(ChronoField.INSTANT_SECONDS)) {
                            Instant instant = Instant.from(t);
                            TimeZone timeZone = TimeZone.getTimeZone(zid.getId());
                            boolean isDaylightSavings = zid.getRules().isDaylightSavings(instant);
                            if (l == null) {
                                locale = Locale.US;
                            } else {
                                locale = l;
                            }
                            sb.append(timeZone.getDisplayName(isDaylightSavings, 0, locale));
                            break;
                        } else {
                            sb.append(zid.getId());
                            break;
                        }
                    } else {
                        throw new IllegalFormatConversionException(c2, t.getClass());
                    }
                    break;
                case 'c':
                    print(sb, t, 'a', l).append(' ');
                    print(sb, t, 'b', l).append(' ');
                    print(sb, t, 'd', l).append(' ');
                    print(sb, t, 'T', l).append(' ');
                    print(sb, t, 'Z', l).append(' ');
                    print(sb, t, 'Y', l);
                    break;
                case 'd':
                case 'e':
                    int i4 = t.get(ChronoField.DAY_OF_MONTH);
                    if (c2 == 'd') {
                        flags = Flags.ZERO_PAD;
                    } else {
                        flags = Flags.NONE;
                    }
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) i4, flags, 2, l));
                    break;
                case 'j':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.DAY_OF_YEAR), Flags.ZERO_PAD, 3, l));
                    break;
                case 'k':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.HOUR_OF_DAY), Flags.NONE, 2, l));
                    break;
                case 'l':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.CLOCK_HOUR_OF_AMPM), Flags.NONE, 2, l));
                    break;
                case 'm':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) t.get(ChronoField.MONTH_OF_YEAR), Flags.ZERO_PAD, 2, l));
                    break;
                case 'p':
                    String[] ampm = {"AM", "PM"};
                    if (!(l == null || l == Locale.US)) {
                        ampm = DateFormatSymbols.getInstance(l).getAmPmStrings();
                    }
                    sb.append(ampm[t.get(ChronoField.AMPM_OF_DAY)].toLowerCase(l != null ? l : Locale.US));
                    break;
                case 'r':
                    print(sb, t, 'I', l).append(':');
                    print(sb, t, 'M', l).append(':');
                    print(sb, t, 'S', l).append(' ');
                    StringBuilder tsb = new StringBuilder();
                    print(tsb, t, 'p', l);
                    sb.append(tsb.toString().toUpperCase(l != null ? l : Locale.US));
                    break;
                case 's':
                    sb.append((CharSequence) localizedMagnitude((StringBuilder) null, t.getLong(ChronoField.INSTANT_SECONDS), Flags.NONE, this.width, l));
                    break;
                case 'z':
                    try {
                        int i5 = t.get(ChronoField.OFFSET_SECONDS);
                        if (i5 < 0) {
                            z = true;
                        }
                        boolean neg = z;
                        sb.append(neg ? '-' : '+');
                        if (neg) {
                            i5 = -i5;
                        }
                        int min = i5 / 60;
                        sb.append((CharSequence) localizedMagnitude((StringBuilder) null, (long) (((min / 60) * 100) + (min % 60)), Flags.ZERO_PAD, 4, l));
                        break;
                    } catch (DateTimeException e) {
                        throw new IllegalFormatConversionException(c2, t.getClass());
                    }
            }
            return sb;
        }

        private void failMismatch(Flags f2, char c2) {
            throw new FormatFlagsConversionMismatchException(f2.toString(), c2);
        }

        private void failConversion(char c2, Object arg) {
            throw new IllegalFormatConversionException(c2, arg.getClass());
        }

        private char getZero(Locale l) {
            if (l == null || l.equals(Formatter.this.locale())) {
                return Formatter.this.zero;
            }
            return DecimalFormatSymbols.getInstance(l).getZeroDigit();
        }

        private StringBuilder localizedMagnitude(StringBuilder sb, long value, Flags f2, int width2, Locale l) {
            return localizedMagnitude(sb, Long.toString(value, 10).toCharArray(), f2, width2, l);
        }

        private StringBuilder localizedMagnitude(StringBuilder sb, char[] value, Flags f2, int width2, Locale l) {
            StringBuilder sb2;
            char[] cArr = value;
            Flags flags = f2;
            int i = width2;
            Locale locale = l;
            if (sb == null) {
                sb2 = new StringBuilder();
            } else {
                sb2 = sb;
            }
            int begin = sb2.length();
            char zero = getZero(locale);
            char grpSep = 0;
            int grpSize = -1;
            char decSep = 0;
            int len = cArr.length;
            int dot = len;
            int j = 0;
            while (true) {
                if (j >= len) {
                    break;
                } else if (cArr[j] == '.') {
                    dot = j;
                    break;
                } else {
                    j++;
                }
            }
            if (dot < len) {
                if (locale == null || locale.equals(Locale.US)) {
                    decSep = '.';
                } else {
                    decSep = DecimalFormatSymbols.getInstance(l).getDecimalSeparator();
                }
            }
            if (flags.contains(Flags.GROUP)) {
                if (locale == null || locale.equals(Locale.US)) {
                    grpSep = ',';
                    grpSize = 3;
                } else {
                    grpSep = DecimalFormatSymbols.getInstance(l).getGroupingSeparator();
                    DecimalFormat df = (DecimalFormat) NumberFormat.getIntegerInstance(l);
                    grpSize = df.getGroupingSize();
                    if (!df.isGroupingUsed() || df.getGroupingSize() == 0) {
                        grpSep = 0;
                    }
                }
            }
            char grpSep2 = grpSep;
            int j2 = 0;
            while (j2 < len) {
                if (j2 == dot) {
                    sb2.append(decSep);
                    grpSep2 = 0;
                } else {
                    sb2.append((char) ((cArr[j2] - '0') + zero));
                    if (!(grpSep2 == 0 || j2 == dot - 1 || (dot - j2) % grpSize != 1)) {
                        sb2.append(grpSep2);
                    }
                }
                j2++;
                cArr = value;
            }
            int len2 = sb2.length();
            if (i != -1 && flags.contains(Flags.ZERO_PAD)) {
                int k = 0;
                while (true) {
                    int k2 = k;
                    if (k2 >= i - len2) {
                        break;
                    }
                    sb2.insert(begin, zero);
                    k = k2 + 1;
                }
            }
            return sb2;
        }
    }

    private class FormatSpecifierParser {
        private static final String FLAGS = ",-(+# 0<";
        private String conv;
        private int cursor;
        private String flags;
        private final String format;
        private FormatSpecifier fs;
        private String index;
        private String precision;
        private String tT;
        private String width;

        public FormatSpecifierParser(String format2, int startIdx) {
            this.format = format2;
            this.cursor = startIdx;
            if (nextIsInt()) {
                String nint = nextInt();
                if (peek() == '$') {
                    this.index = nint;
                    advance();
                } else if (nint.charAt(0) == '0') {
                    back(nint.length());
                } else {
                    this.width = nint;
                }
            }
            this.flags = "";
            while (this.width == null && FLAGS.indexOf((int) peek()) >= 0) {
                this.flags += advance();
            }
            if (this.width == null && nextIsInt()) {
                this.width = nextInt();
            }
            if (peek() == '.') {
                advance();
                if (nextIsInt()) {
                    this.precision = nextInt();
                } else {
                    throw new IllegalFormatPrecisionException(peek());
                }
            }
            if (peek() == 't' || peek() == 'T') {
                this.tT = String.valueOf(advance());
            }
            this.conv = String.valueOf(advance());
            FormatSpecifier formatSpecifier = new FormatSpecifier(this.index, this.flags, this.width, this.precision, this.tT, this.conv);
            this.fs = formatSpecifier;
        }

        private String nextInt() {
            int strBegin = this.cursor;
            while (nextIsInt()) {
                advance();
            }
            return this.format.substring(strBegin, this.cursor);
        }

        private boolean nextIsInt() {
            return !isEnd() && Character.isDigit(peek());
        }

        private char peek() {
            if (!isEnd()) {
                return this.format.charAt(this.cursor);
            }
            throw new UnknownFormatConversionException("End of String");
        }

        private char advance() {
            if (!isEnd()) {
                String str = this.format;
                int i = this.cursor;
                this.cursor = i + 1;
                return str.charAt(i);
            }
            throw new UnknownFormatConversionException("End of String");
        }

        private void back(int len) {
            this.cursor -= len;
        }

        private boolean isEnd() {
            return this.cursor == this.format.length();
        }

        public FormatSpecifier getFormatSpecifier() {
            return this.fs;
        }

        public int getEndIdx() {
            return this.cursor;
        }
    }

    private interface FormatString {
        int index();

        void print(Object obj, Locale locale) throws IOException;

        String toString();
    }

    private static Charset toCharset(String csn) throws UnsupportedEncodingException {
        Objects.requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(csn);
        }
    }

    private static final Appendable nonNullAppendable(Appendable a2) {
        if (a2 == null) {
            return new StringBuilder();
        }
        return a2;
    }

    private Formatter(Locale l2, Appendable a2) {
        this.a = a2;
        this.l = l2;
        this.zero = getZero(l2);
    }

    private Formatter(Charset charset, Locale l2, File file) throws FileNotFoundException {
        this(l2, (Appendable) new BufferedWriter(new OutputStreamWriter((OutputStream) new FileOutputStream(file), charset)));
    }

    public Formatter() {
        this(Locale.getDefault(Locale.Category.FORMAT), (Appendable) new StringBuilder());
    }

    public Formatter(Appendable a2) {
        this(Locale.getDefault(Locale.Category.FORMAT), nonNullAppendable(a2));
    }

    public Formatter(Locale l2) {
        this(l2, (Appendable) new StringBuilder());
    }

    public Formatter(Appendable a2, Locale l2) {
        this(l2, nonNullAppendable(a2));
    }

    public Formatter(String fileName) throws FileNotFoundException {
        this(Locale.getDefault(Locale.Category.FORMAT), (Appendable) new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));
    }

    public Formatter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(fileName, csn, Locale.getDefault(Locale.Category.FORMAT));
    }

    public Formatter(String fileName, String csn, Locale l2) throws FileNotFoundException, UnsupportedEncodingException {
        this(toCharset(csn), l2, new File(fileName));
    }

    public Formatter(File file) throws FileNotFoundException {
        this(Locale.getDefault(Locale.Category.FORMAT), (Appendable) new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
    }

    public Formatter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(file, csn, Locale.getDefault(Locale.Category.FORMAT));
    }

    public Formatter(File file, String csn, Locale l2) throws FileNotFoundException, UnsupportedEncodingException {
        this(toCharset(csn), l2, file);
    }

    public Formatter(PrintStream ps) {
        this(Locale.getDefault(Locale.Category.FORMAT), (Appendable) Objects.requireNonNull(ps));
    }

    public Formatter(OutputStream os) {
        this(Locale.getDefault(Locale.Category.FORMAT), (Appendable) new BufferedWriter(new OutputStreamWriter(os)));
    }

    public Formatter(OutputStream os, String csn) throws UnsupportedEncodingException {
        this(os, csn, Locale.getDefault(Locale.Category.FORMAT));
    }

    public Formatter(OutputStream os, String csn, Locale l2) throws UnsupportedEncodingException {
        this(l2, (Appendable) new BufferedWriter(new OutputStreamWriter(os, csn)));
    }

    private static char getZero(Locale l2) {
        if (l2 == null || l2.equals(Locale.US)) {
            return '0';
        }
        return DecimalFormatSymbols.getInstance(l2).getZeroDigit();
    }

    public Locale locale() {
        ensureOpen();
        return this.l;
    }

    public Appendable out() {
        ensureOpen();
        return this.a;
    }

    public String toString() {
        ensureOpen();
        return this.a.toString();
    }

    public void flush() {
        ensureOpen();
        if (this.a instanceof Flushable) {
            try {
                ((Flushable) this.a).flush();
            } catch (IOException ioe) {
                this.lastException = ioe;
            }
        }
    }

    public void close() {
        if (this.a != null) {
            try {
                if (this.a instanceof Closeable) {
                    ((Closeable) this.a).close();
                }
            } catch (IOException ioe) {
                this.lastException = ioe;
            } catch (Throwable th) {
                this.a = null;
                throw th;
            }
            this.a = null;
        }
    }

    private void ensureOpen() {
        if (this.a == null) {
            throw new FormatterClosedException();
        }
    }

    public IOException ioException() {
        return this.lastException;
    }

    public Formatter format(String format, Object... args) {
        return format(this.l, format, args);
    }

    public Formatter format(Locale l2, String format, Object... args) {
        ensureOpen();
        int last = -1;
        int lasto = -1;
        FormatString[] fsa = parse(format);
        for (FormatString fs : fsa) {
            int index = fs.index();
            Object obj = null;
            switch (index) {
                case -2:
                    fs.print(null, l2);
                    break;
                case -1:
                    if (last >= 0 && (args == null || last <= args.length - 1)) {
                        if (args != null) {
                            obj = args[last];
                        }
                        fs.print(obj, l2);
                        break;
                    } else {
                        throw new MissingFormatArgumentException(fs.toString());
                    }
                case 0:
                    lasto++;
                    last = lasto;
                    if (args != null) {
                        if (lasto > args.length - 1) {
                            throw new MissingFormatArgumentException(fs.toString());
                        }
                    }
                    if (args != null) {
                        obj = args[lasto];
                    }
                    fs.print(obj, l2);
                    break;
                default:
                    last = index - 1;
                    if (args != null) {
                        try {
                            if (last > args.length - 1) {
                                throw new MissingFormatArgumentException(fs.toString());
                            }
                        } catch (IOException x) {
                            this.lastException = x;
                            break;
                        }
                    }
                    if (args != null) {
                        obj = args[last];
                    }
                    fs.print(obj, l2);
                    break;
            }
        }
        return this;
    }

    private FormatString[] parse(String s) {
        ArrayList<FormatString> al = new ArrayList<>();
        int i = 0;
        int len = s.length();
        while (i < len) {
            int nextPercent = s.indexOf(37, i);
            if (s.charAt(i) != '%') {
                int plainTextStart = i;
                int plainTextEnd = nextPercent == -1 ? len : nextPercent;
                al.add(new FixedString(s.substring(plainTextStart, plainTextEnd)));
                i = plainTextEnd;
            } else {
                FormatSpecifierParser fsp = new FormatSpecifierParser(s, i + 1);
                al.add(fsp.getFormatSpecifier());
                i = fsp.getEndIdx();
            }
        }
        return (FormatString[]) al.toArray(new FormatString[al.size()]);
    }
}
