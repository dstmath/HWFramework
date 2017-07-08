package org.apache.xml.utils.res;

public class XResources_ko extends XResourceBundle {
    public Object[][] getContents() {
        r0 = new Object[14][];
        r0[0] = new Object[]{"ui_language", "ko"};
        r0[1] = new Object[]{"help_language", "ko"};
        r0[2] = new Object[]{"language", "ko"};
        r0[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{'\u3131', '\u3134', '\u3137', '\u3139', '\u3141', '\u3142', '\u3145', '\u3147', '\u3148', '\u314a', '\u314b', '\u314c', '\u314d', '\u314e', '\u314f', '\u3151', '\u3153', '\u3155', '\u3157', '\u315b', '\u315c', '\u3160', '\u3161', '\u3163'})};
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
        r0[11] = new Object[]{XResourceBundle.LANG_MULTIPLIER_CHAR, new CharArrayWrapper(new char[]{'\uc5b5', '\ub9cc', '\ucc9c', '\ubc31', '\uc2ed'})};
        r0[12] = new Object[]{"digits", new CharArrayWrapper(new char[]{'\uc77c', '\uc774', '\uc0bc', '\uc0ac', '\uc624', '\uc721', '\uce60', '\ud314', '\uad6c'})};
        objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr[1] = new StringArrayWrapper(new String[]{"digits"});
        r0[13] = objArr;
        return r0;
    }
}
