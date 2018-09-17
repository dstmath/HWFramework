package org.apache.xml.utils.res;

public class XResources_el extends XResourceBundle {
    public Object[][] getContents() {
        r0 = new Object[16][];
        r0[0] = new Object[]{"ui_language", "el"};
        r0[1] = new Object[]{"help_language", "el"};
        r0[2] = new Object[]{"language", "el"};
        r0[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{945, 946, 947, 948, 949, 950, 951, 952, 953, 954, 955, 956, 957, 958, 959, 960, 961, 962, 963, 964, 965, 966, 967, 968, 969})};
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
        objArr[1] = new CharArrayWrapper(new char[]{985});
        r0[10] = objArr;
        r0[11] = new Object[]{"zero", new CharArrayWrapper(new char[0])};
        r0[12] = new Object[]{"digits", new CharArrayWrapper(new char[]{945, 946, 947, 948, 949, 987, 950, 951, 952})};
        r0[13] = new Object[]{"tens", new CharArrayWrapper(new char[]{953, 954, 955, 956, 957, 958, 959, 960, 991})};
        r0[14] = new Object[]{"hundreds", new CharArrayWrapper(new char[]{961, 962, 964, 965, 966, 967, 968, 969, 993})};
        objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr[1] = new StringArrayWrapper(new String[]{"hundreds", "tens", "digits"});
        r0[15] = objArr;
        return r0;
    }
}
