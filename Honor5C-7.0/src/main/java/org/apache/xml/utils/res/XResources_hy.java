package org.apache.xml.utils.res;

public class XResources_hy extends XResourceBundle {
    public Object[][] getContents() {
        Object[][] objArr = new Object[13][];
        objArr[0] = new Object[]{"ui_language", "hy"};
        objArr[1] = new Object[]{"help_language", "hy"};
        objArr[2] = new Object[]{"language", "hy"};
        objArr[3] = new Object[]{XResourceBundle.LANG_ALPHABET, new CharArrayWrapper(new char[]{'\u0561', '\u0562', '\u0563', '\u0564', '\u0565', '\u0566', '\u0567', '\u0568', '\u0569', '\u056a', '\u056b', '\u056c', '\u056d', '\u056e', '\u056f', '\u0567', '\u0568', '\u0572', '\u0573', '\u0574', '\u0575', '\u0576', '\u0577', '\u0578', '\u0579', '\u057a', '\u057b', '\u057c', '\u057d', '\u057e', '\u057f', '\u0580', '\u0581', '\u0582', '\u0583', '\u0584'})};
        objArr[4] = new Object[]{XResourceBundle.LANG_TRAD_ALPHABET, new CharArrayWrapper(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'})};
        objArr[5] = new Object[]{XResourceBundle.LANG_ORIENTATION, "LeftToRight"};
        objArr[6] = new Object[]{XResourceBundle.LANG_NUMBERING, XResourceBundle.LANG_ADDITIVE};
        objArr[7] = new Object[]{XResourceBundle.LANG_NUMBERGROUPS, new IntArrayWrapper(new int[]{1000, 100, 10, 1})};
        objArr[8] = new Object[]{"digits", new CharArrayWrapper(new char[]{'\u0561', '\u0562', '\u0563', '\u0564', '\u0565', '\u0566', '\u0567', '\u0568', '\u0569'})};
        objArr[9] = new Object[]{"tens", new CharArrayWrapper(new char[]{'\u056a', '\u056b', '\u056c', '\u056d', '\u056e', '\u056f', '\u0567', '\u0568', '\u0572'})};
        objArr[10] = new Object[]{"hundreds", new CharArrayWrapper(new char[]{'\u0573', '\u0574', '\u0575', '\u0576', '\u0577', '\u0578', '\u0579', '\u057a', '\u057b'})};
        objArr[11] = new Object[]{"thousands", new CharArrayWrapper(new char[]{'\u057c', '\u057d', '\u057e', '\u057f', '\u0580', '\u0581', '\u0582', '\u0583', '\u0584'})};
        Object[] objArr2 = new Object[2];
        objArr2[0] = XResourceBundle.LANG_NUM_TABLES;
        objArr2[1] = new StringArrayWrapper(new String[]{"thousands", "hundreds", "tens", "digits"});
        objArr[12] = objArr2;
        return objArr;
    }
}
