package org.apache.xml.utils.res;

public class XResources_ka extends XResourceBundle {
    public Object[][] getContents() {
        Object[][] objArr = new Object[13][];
        objArr[0] = new Object[]{"ui_language", "ka"};
        objArr[1] = new Object[]{"help_language", "ka"};
        objArr[2] = new Object[]{"language", "ka"};
        objArr[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{'\u10d0', '\u10d1', '\u10d2', '\u10d3', '\u10d4', '\u10d5', '\u10d6', '\u10f1', '\u10d7', '\u10d8', '\u10d9', '\u10da', '\u10db', '\u10dc', '\u10f2', '\u10dd', '\u10de', '\u10df', '\u10e0', '\u10e1', '\u10e2', '\u10e3', '\u10e4', '\u10e5', '\u10e6', '\u10e7', '\u10e8', '\u10e9', '\u10ea', '\u10eb', '\u10ec', '\u10ed', '\u10ee', '\u10f4', '\u10ef', '\u10f0'})};
        objArr[4] = new Object[]{XResourceBundle.LANG_TRAD_ALPHABET, new CharArrayWrapper(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'})};
        objArr[5] = new Object[]{XResourceBundle.LANG_ORIENTATION, "LeftToRight"};
        objArr[6] = new Object[]{XResourceBundle.LANG_NUMBERING, XResourceBundle.LANG_ADDITIVE};
        objArr[7] = new Object[]{XResourceBundle.LANG_NUMBERGROUPS, new IntArrayWrapper(new int[]{1000, 100, 10, 1})};
        objArr[8] = new Object[]{"digits", new CharArrayWrapper(new char[]{'\u10d0', '\u10d1', '\u10d2', '\u10d3', '\u10d4', '\u10d5', '\u10d6', '\u10f1', '\u10d7'})};
        objArr[9] = new Object[]{"tens", new CharArrayWrapper(new char[]{'\u10d8', '\u10d9', '\u10da', '\u10db', '\u10dc', '\u10f2', '\u10dd', '\u10de', '\u10df'})};
        objArr[10] = new Object[]{"hundreds", new CharArrayWrapper(new char[]{'\u10e0', '\u10e1', '\u10e2', '\u10e3', '\u10e4', '\u10e5', '\u10e6', '\u10e7', '\u10e8'})};
        objArr[11] = new Object[]{"thousands", new CharArrayWrapper(new char[]{'\u10e9', '\u10ea', '\u10eb', '\u10ec', '\u10ed', '\u10ee', '\u10f4', '\u10ef', '\u10f0'})};
        Object[] objArr2 = new Object[2];
        objArr2[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr2[1] = new StringArrayWrapper(new String[]{"thousands", "hundreds", "tens", "digits"});
        objArr[12] = objArr2;
        return objArr;
    }
}
