package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

@Deprecated
public class RefinedSoundex implements StringEncoder {
    public static final RefinedSoundex US_ENGLISH = new RefinedSoundex();
    public static final char[] US_ENGLISH_MAPPING = "01360240043788015936020505".toCharArray();
    private char[] soundexMapping;

    public RefinedSoundex() {
        this(US_ENGLISH_MAPPING);
    }

    public RefinedSoundex(char[] mapping) {
        this.soundexMapping = mapping;
    }

    public int difference(String s1, String s2) throws EncoderException {
        return SoundexUtils.difference(this, s1, s2);
    }

    @Override // org.apache.commons.codec.Encoder
    public Object encode(Object pObject) throws EncoderException {
        if (pObject instanceof String) {
            return soundex((String) pObject);
        }
        throw new EncoderException("Parameter supplied to RefinedSoundex encode is not of type java.lang.String");
    }

    @Override // org.apache.commons.codec.StringEncoder
    public String encode(String pString) {
        return soundex(pString);
    }

    /* access modifiers changed from: package-private */
    public char getMappingCode(char c) {
        if (!Character.isLetter(c)) {
            return 0;
        }
        return this.soundexMapping[Character.toUpperCase(c) - 'A'];
    }

    public String soundex(String str) {
        if (str == null) {
            return null;
        }
        String str2 = SoundexUtils.clean(str);
        if (str2.length() == 0) {
            return str2;
        }
        StringBuffer sBuf = new StringBuffer();
        sBuf.append(str2.charAt(0));
        char last = '*';
        for (int i = 0; i < str2.length(); i++) {
            char current = getMappingCode(str2.charAt(i));
            if (current != last) {
                if (current != 0) {
                    sBuf.append(current);
                }
                last = current;
            }
        }
        return sBuf.toString();
    }
}
