package org.apache.xml.utils.res;

public class XResources_hy extends XResourceBundle {
    public Object[][] getContents() {
        Object[][] objArr = new Object[13][];
        objArr[0] = new Object[]{"ui_language", "hy"};
        objArr[1] = new Object[]{"help_language", "hy"};
        objArr[2] = new Object[]{"language", "hy"};
        objArr[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{1377, 1378, 1379, 1380, 1381, 1382, 1383, 1384, 1385, 1386, 1387, 1388, 1389, 1390, 1391, 1383, 1384, 1394, 1395, 1396, 1397, 1398, 1399, 1400, 1401, 1402, 1403, 1404, 1405, 1406, 1407, 1408, 1409, 1410, 1411, 1412})};
        objArr[4] = new Object[]{XResourceBundle.LANG_TRAD_ALPHABET, new CharArrayWrapper(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'})};
        objArr[5] = new Object[]{XResourceBundle.LANG_ORIENTATION, "LeftToRight"};
        objArr[6] = new Object[]{XResourceBundle.LANG_NUMBERING, XResourceBundle.LANG_ADDITIVE};
        objArr[7] = new Object[]{XResourceBundle.LANG_NUMBERGROUPS, new IntArrayWrapper(new int[]{1000, 100, 10, 1})};
        objArr[8] = new Object[]{"digits", new CharArrayWrapper(new char[]{1377, 1378, 1379, 1380, 1381, 1382, 1383, 1384, 1385})};
        objArr[9] = new Object[]{"tens", new CharArrayWrapper(new char[]{1386, 1387, 1388, 1389, 1390, 1391, 1383, 1384, 1394})};
        objArr[10] = new Object[]{"hundreds", new CharArrayWrapper(new char[]{1395, 1396, 1397, 1398, 1399, 1400, 1401, 1402, 1403})};
        objArr[11] = new Object[]{"thousands", new CharArrayWrapper(new char[]{1404, 1405, 1406, 1407, 1408, 1409, 1410, 1411, 1412})};
        Object[] objArr2 = new Object[2];
        objArr2[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr2[1] = new StringArrayWrapper(new String[]{"thousands", "hundreds", "tens", "digits"});
        objArr[12] = objArr2;
        return objArr;
    }
}
