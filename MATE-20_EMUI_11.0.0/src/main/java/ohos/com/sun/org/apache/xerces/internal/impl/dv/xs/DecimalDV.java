package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.locale.LanguageTag;

public class DecimalDV extends TypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public final short getAllowedFacets() {
        return 4088;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return new XDecimal(str);
        } catch (NumberFormatException unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_DECIMAL});
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public final int compare(Object obj, Object obj2) {
        return ((XDecimal) obj).compareTo((XDecimal) obj2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public final int getTotalDigits(Object obj) {
        return ((XDecimal) obj).totalDigits;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public final int getFractionDigits(Object obj) {
        return ((XDecimal) obj).fracDigits;
    }

    static final class XDecimal implements XSDecimal {
        private String canonical;
        int fracDigits = 0;
        String fvalue = "";
        int intDigits = 0;
        boolean integer = false;
        String ivalue = "";
        int sign = 1;
        int totalDigits = 0;

        XDecimal(String str) throws NumberFormatException {
            initD(str);
        }

        XDecimal(String str, boolean z) throws NumberFormatException {
            if (z) {
                initI(str);
            } else {
                initD(str);
            }
        }

        /* access modifiers changed from: package-private */
        public void initD(String str) throws NumberFormatException {
            int i;
            int length = str.length();
            if (length != 0) {
                int i2 = 1;
                if (str.charAt(0) != '+') {
                    if (str.charAt(0) == '-') {
                        this.sign = -1;
                    } else {
                        i2 = 0;
                    }
                }
                int i3 = i2;
                while (i3 < length && str.charAt(i3) == '0') {
                    i3++;
                }
                int i4 = i3;
                while (i4 < length && TypeValidator.isDigit(str.charAt(i4))) {
                    i4++;
                }
                if (i4 >= length) {
                    length = 0;
                    i = 0;
                } else if (str.charAt(i4) == '.') {
                    i = i4 + 1;
                } else {
                    throw new NumberFormatException();
                }
                if (i2 == i4 && i == length) {
                    throw new NumberFormatException();
                }
                while (length > i && str.charAt(length - 1) == '0') {
                    length--;
                }
                for (int i5 = i; i5 < length; i5++) {
                    if (!TypeValidator.isDigit(str.charAt(i5))) {
                        throw new NumberFormatException();
                    }
                }
                this.intDigits = i4 - i3;
                this.fracDigits = length - i;
                int i6 = this.intDigits;
                int i7 = this.fracDigits;
                this.totalDigits = i6 + i7;
                if (i6 > 0) {
                    this.ivalue = str.substring(i3, i4);
                    if (this.fracDigits > 0) {
                        this.fvalue = str.substring(i, length);
                    }
                } else if (i7 > 0) {
                    this.fvalue = str.substring(i, length);
                } else {
                    this.sign = 0;
                }
            } else {
                throw new NumberFormatException();
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x003f  */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x005f  */
        public void initI(String str) throws NumberFormatException {
            int i;
            int i2;
            int i3;
            int length = str.length();
            if (length != 0) {
                if (str.charAt(0) != '+') {
                    if (str.charAt(0) == '-') {
                        this.sign = -1;
                    } else {
                        i = 0;
                        i2 = i;
                        while (i2 < length && str.charAt(i2) == '0') {
                            i2++;
                        }
                        i3 = i2;
                        while (i3 < length && TypeValidator.isDigit(str.charAt(i3))) {
                            i3++;
                        }
                        if (i3 >= length) {
                            throw new NumberFormatException();
                        } else if (i != i3) {
                            this.intDigits = i3 - i2;
                            this.fracDigits = 0;
                            int i4 = this.intDigits;
                            this.totalDigits = i4;
                            if (i4 > 0) {
                                this.ivalue = str.substring(i2, i3);
                            } else {
                                this.sign = 0;
                            }
                            this.integer = true;
                            return;
                        } else {
                            throw new NumberFormatException();
                        }
                    }
                }
                i = 1;
                i2 = i;
                while (i2 < length) {
                    i2++;
                }
                i3 = i2;
                while (i3 < length) {
                    i3++;
                }
                if (i3 >= length) {
                }
            } else {
                throw new NumberFormatException();
            }
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof XDecimal)) {
                return false;
            }
            XDecimal xDecimal = (XDecimal) obj;
            int i = this.sign;
            if (i != xDecimal.sign) {
                return false;
            }
            if (i == 0) {
                return true;
            }
            return this.intDigits == xDecimal.intDigits && this.fracDigits == xDecimal.fracDigits && this.ivalue.equals(xDecimal.ivalue) && this.fvalue.equals(xDecimal.fvalue);
        }

        public int hashCode() {
            int i = this.sign;
            int i2 = 119 + i;
            if (i == 0) {
                return i2;
            }
            return (((((((i2 * 17) + this.intDigits) * 17) + this.fracDigits) * 17) + Objects.hashCode(this.ivalue)) * 17) + Objects.hashCode(this.fvalue);
        }

        public int compareTo(XDecimal xDecimal) {
            int i = this.sign;
            int i2 = xDecimal.sign;
            if (i != i2) {
                return i > i2 ? 1 : -1;
            }
            if (i == 0) {
                return 0;
            }
            return i * intComp(xDecimal);
        }

        private int intComp(XDecimal xDecimal) {
            int i = this.intDigits;
            int i2 = xDecimal.intDigits;
            if (i != i2) {
                return i > i2 ? 1 : -1;
            }
            int compareTo = this.ivalue.compareTo(xDecimal.ivalue);
            if (compareTo != 0) {
                return compareTo > 0 ? 1 : -1;
            }
            int compareTo2 = this.fvalue.compareTo(xDecimal.fvalue);
            if (compareTo2 == 0) {
                return 0;
            }
            return compareTo2 > 0 ? 1 : -1;
        }

        public synchronized String toString() {
            if (this.canonical == null) {
                makeCanonical();
            }
            return this.canonical;
        }

        private void makeCanonical() {
            int i = this.sign;
            if (i == 0) {
                if (this.integer) {
                    this.canonical = "0";
                } else {
                    this.canonical = "0.0";
                }
            } else if (!this.integer || i <= 0) {
                StringBuilder sb = new StringBuilder(this.totalDigits + 3);
                if (this.sign == -1) {
                    sb.append(LocaleUtility.IETF_SEPARATOR);
                }
                if (this.intDigits != 0) {
                    sb.append(this.ivalue);
                } else {
                    sb.append('0');
                }
                if (!this.integer) {
                    sb.append('.');
                    if (this.fracDigits != 0) {
                        sb.append(this.fvalue);
                    } else {
                        sb.append('0');
                    }
                }
                this.canonical = sb.toString();
            } else {
                this.canonical = this.ivalue;
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal
        public BigDecimal getBigDecimal() {
            if (this.sign == 0) {
                return new BigDecimal(BigInteger.ZERO);
            }
            return new BigDecimal(toString());
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal
        public BigInteger getBigInteger() throws NumberFormatException {
            if (this.fracDigits == 0) {
                int i = this.sign;
                if (i == 0) {
                    return BigInteger.ZERO;
                }
                if (i == 1) {
                    return new BigInteger(this.ivalue);
                }
                return new BigInteger(LanguageTag.SEP + this.ivalue);
            }
            throw new NumberFormatException();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal
        public long getLong() throws NumberFormatException {
            if (this.fracDigits == 0) {
                int i = this.sign;
                if (i == 0) {
                    return 0;
                }
                if (i == 1) {
                    return Long.parseLong(this.ivalue);
                }
                return Long.parseLong(LanguageTag.SEP + this.ivalue);
            }
            throw new NumberFormatException();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal
        public int getInt() throws NumberFormatException {
            if (this.fracDigits == 0) {
                int i = this.sign;
                if (i == 0) {
                    return 0;
                }
                if (i == 1) {
                    return Integer.parseInt(this.ivalue);
                }
                return Integer.parseInt(LanguageTag.SEP + this.ivalue);
            }
            throw new NumberFormatException();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal
        public short getShort() throws NumberFormatException {
            if (this.fracDigits == 0) {
                int i = this.sign;
                if (i == 0) {
                    return 0;
                }
                if (i == 1) {
                    return Short.parseShort(this.ivalue);
                }
                return Short.parseShort(LanguageTag.SEP + this.ivalue);
            }
            throw new NumberFormatException();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDecimal
        public byte getByte() throws NumberFormatException {
            if (this.fracDigits == 0) {
                int i = this.sign;
                if (i == 0) {
                    return 0;
                }
                if (i == 1) {
                    return Byte.parseByte(this.ivalue);
                }
                return Byte.parseByte(LanguageTag.SEP + this.ivalue);
            }
            throw new NumberFormatException();
        }
    }
}
