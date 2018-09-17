package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

@Deprecated
public class Soundex implements StringEncoder {
    public static final Soundex US_ENGLISH = new Soundex();
    public static final char[] US_ENGLISH_MAPPING = US_ENGLISH_MAPPING_STRING.toCharArray();
    public static final String US_ENGLISH_MAPPING_STRING = "01230120022455012623010202";
    private int maxLength;
    private char[] soundexMapping;

    public int difference(String s1, String s2) throws EncoderException {
        return SoundexUtils.difference(this, s1, s2);
    }

    public Soundex() {
        this(US_ENGLISH_MAPPING);
    }

    public Soundex(char[] mapping) {
        this.maxLength = 4;
        setSoundexMapping(mapping);
    }

    public Object encode(Object pObject) throws EncoderException {
        if (pObject instanceof String) {
            return soundex((String) pObject);
        }
        throw new EncoderException("Parameter supplied to Soundex encode is not of type java.lang.String");
    }

    public String encode(String pString) {
        return soundex(pString);
    }

    private char getMappingCode(String str, int index) {
        char mappedChar = map(str.charAt(index));
        if (index > 1 && mappedChar != '0') {
            char hwChar = str.charAt(index - 1);
            if ('H' == hwChar || 'W' == hwChar) {
                char preHWChar = str.charAt(index - 2);
                if (map(preHWChar) == mappedChar || 'H' == preHWChar || 'W' == preHWChar) {
                    return 0;
                }
            }
        }
        return mappedChar;
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    private char[] getSoundexMapping() {
        return this.soundexMapping;
    }

    private char map(char ch) {
        int index = ch - 65;
        if (index >= 0 && index < getSoundexMapping().length) {
            return getSoundexMapping()[index];
        }
        throw new IllegalArgumentException("The character is not mapped: " + ch);
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    private void setSoundexMapping(char[] soundexMapping) {
        this.soundexMapping = soundexMapping;
    }

    public String soundex(String str) {
        if (str == null) {
            return null;
        }
        str = SoundexUtils.clean(str);
        if (str.length() == 0) {
            return str;
        }
        char[] out = new char[]{'0', '0', '0', '0'};
        int incount = 1;
        int count = 1;
        out[0] = str.charAt(0);
        char last = getMappingCode(str, 0);
        while (incount < str.length() && count < out.length) {
            int incount2 = incount + 1;
            char mapped = getMappingCode(str, incount);
            if (mapped != 0) {
                if (!(mapped == '0' || mapped == last)) {
                    int count2 = count + 1;
                    out[count] = mapped;
                    count = count2;
                }
                last = mapped;
            }
            incount = incount2;
        }
        return new String(out);
    }
}
