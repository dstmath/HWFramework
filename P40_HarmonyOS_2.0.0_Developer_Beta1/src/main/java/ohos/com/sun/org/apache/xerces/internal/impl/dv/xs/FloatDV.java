package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSFloat;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;

public class FloatDV extends TypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 2552;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return new XFloat(str);
        } catch (NumberFormatException unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, "float"});
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public int compare(Object obj, Object obj2) {
        return ((XFloat) obj).compareTo((XFloat) obj2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public boolean isIdentical(Object obj, Object obj2) {
        if (obj2 instanceof XFloat) {
            return ((XFloat) obj).isIdentical((XFloat) obj2);
        }
        return false;
    }

    private static final class XFloat implements XSFloat {
        private String canonical;
        private final float value;

        public XFloat(String str) throws NumberFormatException {
            if (DoubleDV.isPossibleFP(str)) {
                this.value = Float.parseFloat(str);
            } else if (str.equals("INF")) {
                this.value = Float.POSITIVE_INFINITY;
            } else if (str.equals("-INF")) {
                this.value = Float.NEGATIVE_INFINITY;
            } else if (str.equals("NaN")) {
                this.value = Float.NaN;
            } else {
                throw new NumberFormatException(str);
            }
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof XFloat)) {
                return false;
            }
            float f = this.value;
            float f2 = ((XFloat) obj).value;
            if (f == f2) {
                return true;
            }
            return (f == f || f2 == f2) ? false : true;
        }

        public int hashCode() {
            float f = this.value;
            if (f == 0.0f) {
                return 0;
            }
            return Float.floatToIntBits(f);
        }

        public boolean isIdentical(XFloat xFloat) {
            if (xFloat == this) {
                return true;
            }
            float f = this.value;
            float f2 = xFloat.value;
            return f == f2 ? f != 0.0f || Float.floatToIntBits(f) == Float.floatToIntBits(xFloat.value) : (f == f || f2 == f2) ? false : true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int compareTo(XFloat xFloat) {
            float f = xFloat.value;
            float f2 = this.value;
            if (f2 < f) {
                return -1;
            }
            if (f2 > f) {
                return 1;
            }
            if (f2 == f) {
                return 0;
            }
            return (f2 == f2 || f == f) ? 2 : 0;
        }

        public synchronized String toString() {
            int i;
            int i2;
            if (this.canonical == null) {
                if (this.value == Float.POSITIVE_INFINITY) {
                    this.canonical = "INF";
                } else if (this.value == Float.NEGATIVE_INFINITY) {
                    this.canonical = "-INF";
                } else if (this.value != this.value) {
                    this.canonical = "NaN";
                } else if (this.value == 0.0f) {
                    this.canonical = "0.0E1";
                } else {
                    this.canonical = Float.toString(this.value);
                    if (this.canonical.indexOf(69) == -1) {
                        int length = this.canonical.length();
                        char[] cArr = new char[(length + 3)];
                        this.canonical.getChars(0, length, cArr, 0);
                        int i3 = cArr[0] == '-' ? 2 : 1;
                        if (this.value < 1.0f) {
                            if (this.value > -1.0f) {
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

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSFloat
        public float getValue() {
            return this.value;
        }
    }
}
