package org.apache.xml.utils.res;

public class XResources_el extends XResourceBundle {
    public Object[][] getContents() {
        r0 = new Object[16][];
        r0[0] = new Object[]{"ui_language", "el"};
        r0[1] = new Object[]{"help_language", "el"};
        r0[2] = new Object[]{"language", "el"};
        r0[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{'\u03b1', '\u03b2', '\u03b3', '\u03b4', '\u03b5', '\u03b6', '\u03b7', '\u03b8', '\u03b9', '\u03ba', '\u03bb', '\u03bc', '\u03bd', '\u03be', '\u03bf', '\u03c0', '\u03c1', '\u03c2', '\u03c3', '\u03c4', '\u03c5', '\u03c6', '\u03c7', '\u03c8', '\u03c9'})};
        r0[4] = new Object[]{XResourceBundle.LANG_TRAD_ALPHABET, new CharArrayWrapper(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'})};
        r0[5] = new Object[]{XResourceBundle.LANG_ORIENTATION, "LeftToRight"};
        r0[6] = new Object[]{XResourceBundle.LANG_NUMBERING, XResourceBundle.LANG_MULT_ADD};
        r0[7] = new Object[]{XResourceBundle.MULT_ORDER, XResourceBundle.MULT_PRECEDES};
        r0[8] = new Object[]{XResourceBundle.LANG_NUMBERGROUPS, new IntArrayWrapper(new int[]{100, 10, 1})};
        Object[] objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_MULTIPLIER;
        objArr[1] = new LongArrayWrapper(new long[]{1000});
        r0[9] = objArr;
        objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_MULTIPLIER_CHAR;
        objArr[1] = new CharArrayWrapper(new char[]{'\u03d9'});
        r0[10] = objArr;
        r0[11] = new Object[]{"zero", new CharArrayWrapper(new char[0])};
        r0[12] = new Object[]{"digits", new CharArrayWrapper(new char[]{'\u03b1', '\u03b2', '\u03b3', '\u03b4', '\u03b5', '\u03db', '\u03b6', '\u03b7', '\u03b8'})};
        r0[13] = new Object[]{"tens", new CharArrayWrapper(new char[]{'\u03b9', '\u03ba', '\u03bb', '\u03bc', '\u03bd', '\u03be', '\u03bf', '\u03c0', '\u03df'})};
        r0[14] = new Object[]{"hundreds", new CharArrayWrapper(new char[]{'\u03c1', '\u03c2', '\u03c4', '\u03c5', '\u03c6', '\u03c7', '\u03c8', '\u03c9', '\u03e1'})};
        objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr[1] = new StringArrayWrapper(new String[]{"hundreds", "tens", "digits"});
        r0[15] = objArr;
        return r0;
    }
}
