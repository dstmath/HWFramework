package org.apache.xml.utils.res;

public class XResources_ko extends XResourceBundle {
    public Object[][] getContents() {
        r0 = new Object[14][];
        r0[0] = new Object[]{"ui_language", "ko"};
        r0[1] = new Object[]{"help_language", "ko"};
        r0[2] = new Object[]{"language", "ko"};
        r0[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{12593, 12596, 12599, 12601, 12609, 12610, 12613, 12615, 12616, 12618, 12619, 12620, 12621, 12622, 12623, 12625, 12627, 12629, 12631, 12635, 12636, 12640, 12641, 12643})};
        r0[4] = new Object[]{XResourceBundle.LANG_TRAD_ALPHABET, new CharArrayWrapper(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'})};
        r0[5] = new Object[]{XResourceBundle.LANG_ORIENTATION, "LeftToRight"};
        r0[6] = new Object[]{XResourceBundle.LANG_NUMBERING, XResourceBundle.LANG_MULT_ADD};
        r0[7] = new Object[]{XResourceBundle.MULT_ORDER, XResourceBundle.MULT_FOLLOWS};
        Object[] objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_NUMBERGROUPS;
        objArr[1] = new IntArrayWrapper(new int[]{1});
        r0[8] = objArr;
        r0[9] = new Object[]{"zero", new CharArrayWrapper(new char[0])};
        r0[10] = new Object[]{XResourceBundle.LANG_MULTIPLIER, new LongArrayWrapper(new long[]{100000000, 10000, 1000, 100, 10})};
        r0[11] = new Object[]{XResourceBundle.LANG_MULTIPLIER_CHAR, new CharArrayWrapper(new char[]{50613, 47564, 52380, 48177, 49901})};
        r0[12] = new Object[]{"digits", new CharArrayWrapper(new char[]{51068, 51060, 49340, 49324, 50724, 50977, 52832, 54036, 44396})};
        objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr[1] = new StringArrayWrapper(new String[]{"digits"});
        r0[13] = objArr;
        return r0;
    }
}
