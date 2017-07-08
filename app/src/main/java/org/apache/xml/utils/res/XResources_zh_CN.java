package org.apache.xml.utils.res;

public class XResources_zh_CN extends XResourceBundle {
    public Object[][] getContents() {
        r0 = new Object[14][];
        r0[0] = new Object[]{"ui_language", "zh"};
        r0[1] = new Object[]{"help_language", "zh"};
        r0[2] = new Object[]{"language", "zh"};
        r0[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{'\uff21', '\uff22', '\uff23', '\uff24', '\uff25', '\uff26', '\uff27', '\uff28', '\uff29', '\uff2a', '\uff2b', '\uff2c', '\uff2d', '\uff2e', '\uff2f', '\uff30', '\uff31', '\uff32', '\uff33', '\uff34', '\uff35', '\uff36', '\uff37', '\uff38', '\uff39', '\uff3a'})};
        r0[4] = new Object[]{XResourceBundle.LANG_TRAD_ALPHABET, new CharArrayWrapper(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'})};
        r0[5] = new Object[]{XResourceBundle.LANG_ORIENTATION, "LeftToRight"};
        r0[6] = new Object[]{XResourceBundle.LANG_NUMBERING, XResourceBundle.LANG_MULT_ADD};
        r0[7] = new Object[]{XResourceBundle.MULT_ORDER, XResourceBundle.MULT_FOLLOWS};
        Object[] objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_NUMBERGROUPS;
        objArr[1] = new IntArrayWrapper(new int[]{1});
        r0[8] = objArr;
        objArr = new Object[2];
        objArr[0] = "zero";
        objArr[1] = new CharArrayWrapper(new char[]{'\u96f6'});
        r0[9] = objArr;
        r0[10] = new Object[]{XResourceBundle.LANG_MULTIPLIER, new LongArrayWrapper(new long[]{100000000, 10000, 1000, 100, 10})};
        r0[11] = new Object[]{XResourceBundle.LANG_MULTIPLIER_CHAR, new CharArrayWrapper(new char[]{'\u4ebf', '\u4e07', '\u5343', '\u767e', '\u5341'})};
        r0[12] = new Object[]{"digits", new CharArrayWrapper(new char[]{'\u4e00', '\u4e8c', '\u4e09', '\u56db', '\u4e94', '\u516d', '\u4e03', '\u516b', '\u4e5d'})};
        objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr[1] = new StringArrayWrapper(new String[]{"digits"});
        r0[13] = objArr;
        return r0;
    }
}
