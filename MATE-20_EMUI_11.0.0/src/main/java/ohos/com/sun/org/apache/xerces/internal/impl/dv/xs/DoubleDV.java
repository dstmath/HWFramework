package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDouble;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.com.sun.org.apache.xpath.internal.XPath;

public class DoubleDV extends TypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 2552;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return new XDouble(str);
        } catch (NumberFormatException unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, "double"});
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public int compare(Object obj, Object obj2) {
        return ((XDouble) obj).compareTo((XDouble) obj2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public boolean isIdentical(Object obj, Object obj2) {
        if (obj2 instanceof XDouble) {
            return ((XDouble) obj).isIdentical((XDouble) obj2);
        }
        return false;
    }

    static boolean isPossibleFP(String str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (!((charAt >= '0' && charAt <= '9') || charAt == '.' || charAt == '-' || charAt == '+' || charAt == 'E' || charAt == 'e')) {
                return false;
            }
        }
        return true;
    }

    private static final class XDouble implements XSDouble {
        private String canonical;
        private final double value;

        public XDouble(String str) throws NumberFormatException {
            if (DoubleDV.isPossibleFP(str)) {
                this.value = Double.parseDouble(str);
            } else if (str.equals("INF")) {
                this.value = Double.POSITIVE_INFINITY;
            } else if (str.equals("-INF")) {
                this.value = Double.NEGATIVE_INFINITY;
            } else if (str.equals("NaN")) {
                this.value = Double.NaN;
            } else {
                throw new NumberFormatException(str);
            }
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof XDouble)) {
                return false;
            }
            double d = this.value;
            double d2 = ((XDouble) obj).value;
            if (d == d2) {
                return true;
            }
            return (d == d || d2 == d2) ? false : true;
        }

        public int hashCode() {
            double d = this.value;
            if (d == XPath.MATCH_SCORE_QNAME) {
                return 0;
            }
            long doubleToLongBits = Double.doubleToLongBits(d);
            return (int) (doubleToLongBits ^ (doubleToLongBits >>> 32));
        }

        public boolean isIdentical(XDouble xDouble) {
            if (xDouble == this) {
                return true;
            }
            double d = this.value;
            double d2 = xDouble.value;
            return d == d2 ? d != XPath.MATCH_SCORE_QNAME || Double.doubleToLongBits(d) == Double.doubleToLongBits(xDouble.value) : (d == d || d2 == d2) ? false : true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int compareTo(XDouble xDouble) {
            double d = xDouble.value;
            double d2 = this.value;
            if (d2 < d) {
                return -1;
            }
            if (d2 > d) {
                return 1;
            }
            if (d2 == d) {
                return 0;
            }
            return (d2 == d2 || d == d) ? 2 : 0;
        }

        public synchronized String toString() {
            int i;
            int i2;
            if (this.canonical == null) {
                if (this.value == Double.POSITIVE_INFINITY) {
                    this.canonical = "INF";
                } else if (this.value == Double.NEGATIVE_INFINITY) {
                    this.canonical = "-INF";
                } else if (this.value != this.value) {
                    this.canonical = "NaN";
                } else if (this.value == XPath.MATCH_SCORE_QNAME) {
                    this.canonical = "0.0E1";
                } else {
                    this.canonical = Double.toString(this.value);
                    if (this.canonical.indexOf(69) == -1) {
                        int length = this.canonical.length();
                        char[] cArr = new char[(length + 3)];
                        this.canonical.getChars(0, length, cArr, 0);
                        int i3 = cArr[0] == '-' ? 2 : 1;
                        if (this.value < 1.0d) {
                            if (this.value > -1.0d) {
                                int i4 = i3 + 1;
                                int i5 = i4;
                                while (cArr[i5] == '0') {
                                    i5++;
                                }
                                cArr[i3 - 1] = cArr[i5];
                                cArr[i3] = '.';
                                int i6 = i5 + 1;
                                int i7 = i4;
                                while (i6 < length) {
                                    cArr[i7] = cArr[i6];
                                    i6++;
                                    i7++;
                                }
                                int i8 = i5 - i3;
                                int i9 = length - i8;
                                if (i9 == i4) {
                                    cArr[i9] = '0';
                                    i9++;
                                }
                                int i10 = i9 + 1;
                                cArr[i9] = 'E';
                                int i11 = i10 + 1;
                                cArr[i10] = LocaleUtility.IETF_SEPARATOR;
                                i = i11 + 1;
                                cArr[i11] = (char) (i8 + 48);
                                this.canonical = new String(cArr, 0, i);
                            }
                        }
                        int indexOf = this.canonical.indexOf(46);
                        for (int i12 = indexOf; i12 > i3; i12--) {
                            cArr[i12] = cArr[i12 - 1];
                        }
                        cArr[i3] = '.';
                        while (true) {
                            i2 = length - 1;
                            if (cArr[i2] != '0') {
                                break;
                            }
                            length--;
                        }
                        if (cArr[i2] == '.') {
                            length++;
                        }
                        int i13 = length + 1;
                        cArr[length] = 'E';
                        i = i13 + 1;
                        cArr[i13] = (char) ((indexOf - i3) + 48);
                        this.canonical = new String(cArr, 0, i);
                    }
                }
            }
            return this.canonical;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDouble
        public double getValue() {
            return this.value;
        }
    }
}
