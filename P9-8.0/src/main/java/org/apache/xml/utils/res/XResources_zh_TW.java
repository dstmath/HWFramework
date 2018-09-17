package org.apache.xml.utils.res;

public class XResources_zh_TW extends XResourceBundle {
    public Object[][] getContents() {
        r0 = new Object[14][];
        r0[0] = new Object[]{"ui_language", "zh"};
        r0[1] = new Object[]{"help_language", "zh"};
        r0[2] = new Object[]{"language", "zh"};
        r0[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{65313, 65314, 65315, 65316, 65317, 65318, 65319, 65320, 65321, 65322, 65323, 65324, 65325, 65326, 65327, 65328, 65329, 65330, 65331, 65332, 65333, 65334, 65335, 65336, 65337, 65338})};
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
        objArr[1] = new CharArrayWrapper(new char[]{38646});
        r0[9] = objArr;
        r0[10] = new Object[]{XResourceBundle.LANG_MULTIPLIER, new LongArrayWrapper(new long[]{100000000, 10000, 1000, 100, 10})};
        r0[11] = new Object[]{XResourceBundle.LANG_MULTIPLIER_CHAR, new CharArrayWrapper(new char[]{20740, 33836, 20191, 20336, 25342})};
        r0[12] = new Object[]{"digits", new CharArrayWrapper(new char[]{22777, 36019, 21443, 32902, 20237, 38520, 26578, 25420, 29590})};
        objArr = new Object[2];
        objArr[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr[1] = new StringArrayWrapper(new String[]{"digits"});
        r0[13] = objArr;
        return r0;
    }
}
