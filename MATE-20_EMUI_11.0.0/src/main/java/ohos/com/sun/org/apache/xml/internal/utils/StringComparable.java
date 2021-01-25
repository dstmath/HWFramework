package ohos.com.sun.org.apache.xml.internal.utils;

import java.text.CollationElementIterator;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Locale;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;

public class StringComparable implements Comparable {
    public static final int LOWER_CASE = 2;
    public static final int UNKNOWN_CASE = -1;
    public static final int UPPER_CASE = 1;
    private String m_caseOrder;
    private RuleBasedCollator m_collator;
    private Locale m_locale;
    private int m_mask = -1;
    private String m_text;

    private static final int getMask(int i) {
        if (i != 0) {
            return i != 1 ? -1 : -256;
        }
        return -65536;
    }

    public StringComparable(String str, Locale locale, Collator collator, String str2) {
        this.m_text = str;
        this.m_locale = locale;
        this.m_collator = (RuleBasedCollator) collator;
        this.m_caseOrder = str2;
        this.m_mask = getMask(this.m_collator.getStrength());
    }

    public static final Comparable getComparator(String str, Locale locale, Collator collator, String str2) {
        if (str2 == null || str2.length() == 0) {
            return ((RuleBasedCollator) collator).getCollationKey(str);
        }
        return new StringComparable(str, locale, collator, str2);
    }

    @Override // java.lang.Object
    public final String toString() {
        return this.m_text;
    }

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        int i;
        String stringComparable = ((StringComparable) obj).toString();
        if (this.m_text.equals(stringComparable)) {
            return 0;
        }
        int strength = this.m_collator.getStrength();
        if (strength == 0 || strength == 1) {
            i = this.m_collator.compare(this.m_text, stringComparable);
        } else {
            this.m_collator.setStrength(1);
            i = this.m_collator.compare(this.m_text, stringComparable);
            this.m_collator.setStrength(strength);
        }
        if (i != 0) {
            return i;
        }
        int caseDiff = getCaseDiff(this.m_text, stringComparable);
        if (caseDiff != 0) {
            return caseDiff;
        }
        return this.m_collator.compare(this.m_text, stringComparable);
    }

    private final int getCaseDiff(String str, String str2) {
        int strength = this.m_collator.getStrength();
        int decomposition = this.m_collator.getDecomposition();
        this.m_collator.setStrength(2);
        this.m_collator.setDecomposition(1);
        int[] firstCaseDiff = getFirstCaseDiff(str, str2, this.m_locale);
        this.m_collator.setStrength(strength);
        this.m_collator.setDecomposition(decomposition);
        if (firstCaseDiff == null) {
            return 0;
        }
        if (this.m_caseOrder.equals(Constants.ATTRVAL_CASEORDER_UPPER)) {
            if (firstCaseDiff[0] == 1) {
                return -1;
            }
            return 1;
        } else if (firstCaseDiff[0] == 2) {
            return -1;
        } else {
            return 1;
        }
    }

    private final int[] getFirstCaseDiff(String str, String str2, Locale locale) {
        int i;
        CollationElementIterator collationElementIterator;
        int i2;
        int[] iArr;
        int i3;
        String str3 = str;
        String str4 = str2;
        CollationElementIterator collationElementIterator2 = this.m_collator.getCollationElementIterator(str3);
        CollationElementIterator collationElementIterator3 = this.m_collator.getCollationElementIterator(str4);
        int element = getElement(-1);
        int i4 = -1;
        int i5 = -1;
        int i6 = -1;
        int i7 = -1;
        int i8 = 1;
        int i9 = 1;
        int i10 = 0;
        int i11 = 0;
        while (true) {
            if (i8 != 0) {
                int offset = collationElementIterator3.getOffset();
                i10 = getElement(collationElementIterator3.next());
                i5 = collationElementIterator3.getOffset();
                i4 = offset;
            }
            if (i9 != 0) {
                int offset2 = collationElementIterator2.getOffset();
                int element2 = getElement(collationElementIterator2.next());
                i6 = offset2;
                i = collationElementIterator2.getOffset();
                i11 = element2;
            } else {
                i = i7;
            }
            if (i10 == element || i11 == element) {
                return null;
            }
            if (i11 == 0) {
                i7 = i;
                i8 = 0;
                i9 = 1;
            } else if (i10 == 0) {
                i7 = i;
                i8 = 1;
                i9 = 0;
            } else {
                if (i11 != i10 && i4 < i5 && i6 < i) {
                    String substring = str3.substring(i6, i);
                    String substring2 = str4.substring(i4, i5);
                    String upperCase = substring.toUpperCase(locale);
                    String upperCase2 = substring2.toUpperCase(locale);
                    if (this.m_collator.compare(upperCase, upperCase2) == 0) {
                        collationElementIterator = collationElementIterator2;
                        iArr = new int[]{-1, -1};
                        if (this.m_collator.compare(substring, upperCase) == 0) {
                            iArr[0] = 1;
                        } else if (this.m_collator.compare(substring, substring.toLowerCase(locale)) == 0) {
                            iArr[0] = 2;
                        }
                        if (this.m_collator.compare(substring2, upperCase2) == 0) {
                            i2 = 1;
                            iArr[1] = 1;
                        } else {
                            i2 = 1;
                            if (this.m_collator.compare(substring2, substring2.toLowerCase(locale)) == 0) {
                                i3 = 2;
                                iArr[1] = 2;
                                if ((iArr[0] == i2 && iArr[i2] == i3) || (iArr[0] == i3 && iArr[i2] == i2)) {
                                    break;
                                }
                                str3 = str;
                                i9 = i2;
                                i7 = i;
                                collationElementIterator2 = collationElementIterator;
                                i8 = i9;
                                str4 = str2;
                            }
                        }
                        i3 = 2;
                        break;
                    }
                }
                collationElementIterator = collationElementIterator2;
                i2 = 1;
                str3 = str;
                i9 = i2;
                i7 = i;
                collationElementIterator2 = collationElementIterator;
                i8 = i9;
                str4 = str2;
            }
        }
        return iArr;
    }

    private final int getElement(int i) {
        return this.m_mask & i;
    }
}
